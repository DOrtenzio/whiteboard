package whiteboard.whiteboard.server;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import javafx.scene.paint.Color;
import whiteboard.whiteboard.azioni.*;
import whiteboard.whiteboard.serializer.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Server {
    // Variabili per socket
    private final ServerSocket serverSocket;
    private final ObjectMapper mapper = new ObjectMapper();

    // Strutture dati
    private final List<String> lavagneId = new ArrayList<>();
    private final List<String> lavagneNomi = new ArrayList<>();
    private final List<Stato> lavagneStati = new ArrayList<>();
    private final List<List<PrintWriter>> lavagneUtenti = new ArrayList<>();

    /*FASE INIZIALE*/
    public Server() throws IOException {
        this.serverSocket = new ServerSocket(9999);
        configuraMapper(); //Necessario dovuto l'uso di classi non già predefinite
        System.out.println("[SERVER] Server avviato sulla porta 9999");
    }

    private void configuraMapper() {
        SimpleModule moduloColore = new SimpleModule();
        moduloColore.addSerializer(Color.class, new ColorSerializer());
        moduloColore.addDeserializer(Color.class, new ColorDeserializer());
        mapper.registerModule(moduloColore);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void start() {
        System.out.println("[SERVER] In attesa di connessioni...");
        while (true) {
            try {
                Socket connessioneClient = serverSocket.accept();
                System.out.println("[SERVER] Nuova connessione");
                new Thread(() -> gestisciClientConnesso(connessioneClient)).start();
            } catch (IOException e) {
                System.err.println("[SERVER] Errore durante l'accettazione di una nuova connessione: " + e.getMessage());
            }
        }
    }

    /*GESTIONE FILE*/
    private Path ottieniPercorso(String nomeLavagna) {
        return Paths.get("src/main/java/whiteboard/whiteboard/data/statiLavagne/"+nomeLavagna + ".txt");
    }

    private void scriviStato(String nomeLavagna, Stato stato) {
        Path path = ottieniPercorso(nomeLavagna);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            String s = mapper.writeValueAsString(stato);
            writer.write(s);
        } catch (IOException e) {
            System.err.println("[SERVER][FILE] Errore scrittura file per la lavagna '" + nomeLavagna + "': " + e.getMessage());
        }
    }


    private Stato leggiStato(String boardName) {
        Path path = ottieniPercorso(boardName);
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            if (Files.exists(path)) {
                return mapper.readValue(reader, Stato.class);
            }
        } catch (IOException e) {
            System.err.println("[SERVER][FILE] Errore lettura file per la lavagna '" + boardName + "': " + e.getMessage());
        }
        return new Stato(null);
    }

    private ArrayList<String> idLavagneAccesso(String nomeUtente){
        ArrayList<String> idLavagne = new ArrayList<String>();

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/whiteboard/whiteboard/data/accessiUtenti.txt"))) {
            String s;
            while ((s = br.readLine()) != null) {
                String[] split = s.split(";");
                if (split.length == 2 && split[0].equals(nomeUtente)) { //Controllo anche se contiene almeno due campi
                    String[] lavagne = split[1].split(",");
                    idLavagne.addAll(Arrays.asList(lavagne)); //Converto in automatico in lista
                    break; // trovato l'utente, non serve continuare
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return idLavagne;
    }

    public void aggiungiAccesso(String nomeUtente, String idLavagna) {
        File file = new File("src/main/java/whiteboard/whiteboard/data/accessiUtenti.txt");
        List<String> contenutoFile = new ArrayList<>();
        boolean utenteTrovato = false;
        boolean lavagnaAggiunta = false;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String next;
            while ((next = reader.readLine()) != null) {
                String[] split = next.split(";");
                if (split.length == 2 && split[0].equals(nomeUtente)) {
                    utenteTrovato = true;
                    ArrayList<String> lavagne = new ArrayList<String>(Arrays.asList(split[1].split(","))); //Isolo e splitto solo la seconda parte che contiene gli id delle lavagne

                    if (!lavagne.contains(idLavagna)) {
                        lavagne.add(idLavagna);
                        lavagnaAggiunta = true;
                    }

                    contenutoFile.add(nomeUtente + ";" + String.join(",", lavagne));
                } else {
                    contenutoFile.add(next);
                }
            }
            reader.close();

            // Se utente non esiste, aggiungiamo la riga
            if (!utenteTrovato) {
                contenutoFile.add(nomeUtente + ";" + idLavagna);
            }

            // Riscrivi il file solo se è stato aggiunto qualcosa
            if (!lavagnaAggiunta || !utenteTrovato) {
                PrintWriter writer = new PrintWriter(new FileWriter(file));
                for (String riga : contenutoFile) {
                    writer.println(riga);
                }
                writer.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*GESTIONI CLIENTI*/
    private void gestisciClientConnesso(Socket clientSocket) {
        System.out.println("[SERVER][CLIENT] Inizio gestione client.");
        String idLavagnaAttuale = null;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {
             msgCONFERMA(out);

            //Aggiorno con le conoscenze delle lavagne attuali legate all'utente
            String nomeUtente=mapper.readValue(in.readLine(), Logs.class).getParametro1(); //ottengo il nome utente
            out.println(mapper.writeValueAsString(idLavagneAccesso(nomeUtente)));

            String richiestaRicevuta;
            while ((richiestaRicevuta = in.readLine()) != null) {
                System.out.println("[SERVER][CLIENT] Ricevuta richiesta: " + richiestaRicevuta);
                Logs richiesta = mapper.readValue(richiestaRicevuta, Logs.class);
                switch (richiesta.getNomeDelComando()) {
                    case "LAVAGNA_NEW":
                        idLavagnaAttuale = gestLAVAGNA_NEW(richiesta.getParametro1(), in, out);
                        aggiungiAccesso(nomeUtente,idLavagnaAttuale);
                        break;
                    case "LAVAGNA_OLD":
                        idLavagnaAttuale = richiesta.getParametro1();
                        gestLAVAGNA_OLD(idLavagnaAttuale, in, out);
                        aggiungiAccesso(nomeUtente,idLavagnaAttuale);
                        break;
                    case "LAVAGNA_UPDATE":
                        if (idLavagnaAttuale == null) {
                            msgErr(out, "ERR_LAVAGNA_NON_SELEZIONATA");
                        } else {
                            gestLAVAGNA_UPDATE(idLavagnaAttuale, richiesta.getParametro1(), out);
                        }
                        break;
                    default:
                        msgErr(out, "COMANDO_NON_RICONOSCIUTO");
                        break;
                }
            }

        } catch (IOException e) {
            System.err.println("[SERVER][CLIENT] Errore durante la gestione del client: " + e.getMessage());
        } finally {
            chiudiConnessione(clientSocket);
        }
    }

    private String gestLAVAGNA_NEW(String nomeLavagna, BufferedReader in, PrintWriter out) throws IOException {
        String lavagnaIdNuovo = UUID.randomUUID().toString();
        // Aggiunta alle liste di salvataggio
        lavagneId.add(lavagnaIdNuovo);
        lavagneNomi.add(nomeLavagna);
        Stato iniziale = new Stato(null);
        lavagneStati.add(iniziale);
        lavagneUtenti.add(new ArrayList<>());

        // Scrittura iniziale su file
        scriviStato(nomeLavagna, iniziale);

        out.println(mapper.writeValueAsString(new Logs(null, lavagnaIdNuovo)));
        System.out.println("[SERVER][CLIENT] Creata nuova lavagna con ID: " + lavagnaIdNuovo + ", nome: " + nomeLavagna + ". Inviato ID al client.");

        try {
            String statoRicevuto = in.readLine();
            System.out.println("[SERVER][LAVAGNA " + lavagnaIdNuovo + "][CLIENT] Ricevuto stato iniziale: " + statoRicevuto);
            if (statoRicevuto != null) {
                Stato statoIniziale = mapper.readValue(statoRicevuto, Stato.class);
                int index = ottieniIndiceLavagna(lavagnaIdNuovo);
                if (index != -1) {
                    lavagneStati.set(index, statoIniziale);
                    // Aggiorno il file con lo stato ricevuto
                    scriviStato(nomeLavagna, statoIniziale);
                }
                aggiungiUtenteAttivoAllaLavagna(lavagnaIdNuovo, out);
                gestUPDATE(in, lavagnaIdNuovo, out);
            } else {
                msgERR_STATO(out, lavagnaIdNuovo);
            }
        } catch (IOException e) {
            msgERR_STATO(out, lavagnaIdNuovo);
        }

        return lavagnaIdNuovo;
    }

    private void gestLAVAGNA_OLD(String lavagnaId, BufferedReader in, PrintWriter out) throws IOException {
        System.out.println("[SERVER][CLIENT] Tentativo di connessione alla lavagna con ID: " + lavagnaId);
        int index = ottieniIndiceLavagna(lavagnaId);
        if (index != -1) {
            String nomeLavagna = lavagneNomi.get(index);
            Stato statoFile = leggiStato(nomeLavagna);
            lavagneStati.set(index, statoFile);

            aggiungiUtenteAttivoAllaLavagna(lavagnaId, out);
            out.println(mapper.writeValueAsString(new Logs(null, nomeLavagna)));
            out.println(mapper.writeValueAsString(statoFile));

            gestUPDATE(in, lavagnaId, out);
        } else {
            msgERR_LAVAGNA_INTROVABILE(out, lavagnaId);
        }
    }

    private void gestLAVAGNA_UPDATE(String lavagnaId, String statoJSON, PrintWriter outClient) {
        try {
            Stato statoNuovo = mapper.readValue(statoJSON, Stato.class);
            int index = ottieniIndiceLavagna(lavagnaId);
            if (index != -1) {
                lavagneStati.set(index, statoNuovo);
                scriviStato(lavagneNomi.get(index), statoNuovo);
            }
            floodingUpdate(lavagnaId, statoNuovo, outClient);
        } catch (IOException e) {
            System.err.println("[SERVER][LAVAGNA " + lavagnaId + "] Errore nella lettura dell'aggiornamento: " + e.getMessage());
        }
    }

    private void gestUPDATE(BufferedReader in, String lavagnaId, PrintWriter out) {
        new Thread(() -> {
            try {
                String json;
                while ((json = in.readLine()) != null) {
                    System.out.println("[SERVER][LAVAGNA " + lavagnaId + "][CLIENT] Ricevuto aggiornamento: " + json);
                    try {
                        Stato nuovoStato = mapper.readValue(json, Stato.class);
                        int index = ottieniIndiceLavagna(lavagnaId);
                        if (index != -1) {
                            lavagneStati.set(index, nuovoStato);
                            scriviStato(lavagneNomi.get(index), nuovoStato);
                        }
                        floodingUpdate(lavagnaId, nuovoStato, out);
                    } catch (IOException e) {
                        System.err.println("[SERVER][LAVAGNA " + lavagnaId + "][CLIENT] Errore nella lettura dell'aggiornamento: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.out.println("[SERVER][LAVAGNA " + lavagnaId + "][CLIENT] Disconnesso.");
            } finally {
                rimuoviUtente(lavagnaId, out);
                System.out.println("[SERVER][LAVAGNA " + lavagnaId + "][CLIENT] PrintWriter rimosso.");
            }
        }).start();
    }

    private void aggiungiUtenteAttivoAllaLavagna(String lavagnaId, PrintWriter outUtente) {
        int indexLavagna = ottieniIndiceLavagna(lavagnaId);
        if (indexLavagna != -1) {
            List<PrintWriter> utenti = lavagneUtenti.get(indexLavagna);
            synchronized (utenti) { //Evitiamo possibili problemi dati dal multi client
                utenti.add(outUtente);
                System.out.println("[SERVER][LAVAGNA " + lavagnaId + "] Writer aggiunto.");
            }
        }
    }

    private void rimuoviUtente(String lavagnaId, PrintWriter utente) {
        int idx = ottieniIndiceLavagna(lavagnaId);
        if (idx != -1) {
            List<PrintWriter> utenti = lavagneUtenti.get(idx);
            synchronized (utenti) {
                utenti.remove(utente);
                System.out.println("[SERVER][LAVAGNA " + lavagnaId + "] Writer rimosso.");
            }
        }
    }

    private void cancellaLavagna(String lavagnaId) {
        int idx = ottieniIndiceLavagna(lavagnaId);
        if (idx != -1) {
            lavagneId.remove(idx);
            lavagneNomi.remove(idx);
            lavagneStati.remove(idx);
            lavagneUtenti.remove(idx);
            System.out.println("[SERVER][LAVAGNA " + lavagnaId + "] Rimossa.");
        }
    }

    private void floodingUpdate(String lavagnaId, Stato nuovoStato, PrintWriter utenteRicercato) throws IOException {
        int indexLavagna = ottieniIndiceLavagna(lavagnaId);
        if (indexLavagna != -1) { //Sempre se esiste
            List<PrintWriter> utenti = lavagneUtenti.get(indexLavagna); //Lista di out delle connessioni
            synchronized (utenti) {
                for (PrintWriter utente : utenti) {
                    if (utente != utenteRicercato) {
                        try {
                            utente.println(mapper.writeValueAsString(new Logs("LAVAGNA_UPDATE", mapper.writeValueAsString(nuovoStato))));
                            utente.flush();
                        } catch (Exception e) {
                            System.err.println("[SERVER][LAVAGNA " + lavagnaId + "] Errore durante il broadcast: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }


    private void chiudiConnessione(Socket connessione) {
        try {
            connessione.close();
            System.out.println("[SERVER][CLIENT] Connessione chiusa.");
        } catch (IOException e) {
            System.err.println("[SERVER][CLIENT] Errore chiusura socket: " + e.getMessage());
        }
    }

    private int ottieniIndiceLavagna(String lavagnaId) { //Serve per ricercare più facilmente gli elementi
        return lavagneId.indexOf(lavagnaId);
    }

    /*MESSAGGI*/
    private void msgErr(PrintWriter out, String errorMessageCode) throws IOException {
        Logs errorLog = new Logs(errorMessageCode, null);
        out.println(mapper.writeValueAsString(errorLog));
    }
    private void msgCONFERMA(PrintWriter out) throws IOException {
        out.println(mapper.writeValueAsString(new Logs("CONNECTION_ACCEPTED", null)));
    }
    private void msgERR_STATO(PrintWriter out, String boardId) throws IOException {
        System.err.println("[SERVER][LAVAGNA " + boardId + "][CLIENT] Nessuno stato iniziale ricevuto.");
        cancellaLavagna(boardId);
        msgErr(out, "ERRORE_STATO_INIZIALE");
        System.out.println("[SERVER][LAVAGNA " + boardId + "][CLIENT] Inviato errore stato iniziale.");
    }
    private void msgERR_LAVAGNA_INTROVABILE(PrintWriter out, String boardId) throws IOException {
        System.out.println("[SERVER][CLIENT] Lavagna con ID " + boardId + " non trovata.");
        msgErr(out, "LAVAGNA_NON_TROVATA");
        System.out.println("[SERVER][CLIENT] Inviato errore lavagna non trovata.");
    }

    /*MAIN*/
    public static void main(String[] args) {
        try {
            new Server().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/* PICCOLO RECAP DEL CODICE SOPRASTANTE
Il server riceve comandi in formato JSON, li deserializza in oggetti Logs e li gestisce tramite uno
switch su getNomeDelComando(), distinguendo tra creazione (LAVAGNA_NEW), accesso (LAVAGNA_OLD) e
aggiornamento (LAVAGNA_UPDATE) delle lavagne. Gli aggiornamenti dello stato vengono propagati a tutti
gli utenti connessi alla stessa lavagna tramite broadcast asincrono (floodingUpdate), garantendo la
sincronizzazione in tempo reale.
*/
