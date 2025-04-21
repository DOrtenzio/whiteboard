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
    private final ArrayList<String> lavagneIdAttive;
    private final ArrayList<ArrayList<PrintWriter>> lavagneUtentiAttivi;

    /*FASE INIZIALE*/
    public Server() throws IOException {
        this.lavagneIdAttive = new ArrayList<>();
        this.lavagneUtentiAttivi = new ArrayList<ArrayList<PrintWriter>>();

        this.serverSocket = new ServerSocket(9999);
        System.out.println("[SERVER] Server configurato ed avviato sulla porta 9999");
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
    private Path ottieniPercorso(String idLavagna) {
        return Paths.get("whiteboard/src/main/resources/whiteboard/whiteboard/data/statiLavagne/" + idLavagna + ".txt");
    }


    private void scriviStato(String idLavagna, Stato stato) {
        Path path = ottieniPercorso(idLavagna);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            String s = mapper.writeValueAsString(stato);
            writer.write(s);
        } catch (IOException e) {
            System.err.println("[SERVER][FILE] Errore scrittura file per la lavagna '" + idLavagna + "': " + e.getMessage());
        }
    }

    private Stato leggiStato(String idLavagna) {
        Path path = ottieniPercorso(idLavagna);
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            if (Files.exists(path)) {
                return mapper.readValue(reader, Stato.class);
            }
        } catch (IOException e) {
            System.err.println("[SERVER][FILE] Errore lettura file per la lavagna '" + idLavagna + "': " + e.getMessage());
        }
        return new Stato(null);
    }

    private int operazioniCont( boolean isInLettura) {
        if (isInLettura) {
            try (BufferedReader reader = new BufferedReader(new FileReader("whiteboard/src/main/resources/whiteboard/whiteboard/data/cont.txt"))) {
                return Integer.parseInt(reader.readLine());
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } else {
            try {
                BufferedReader reader = new BufferedReader(new FileReader("whiteboard/src/main/resources/whiteboard/whiteboard/data/cont.txt"));
                int cont=Integer.parseInt(reader.readLine());
                reader.close();

                PrintWriter writer = new PrintWriter(new FileWriter("whiteboard/src/main/resources/whiteboard/whiteboard/data/cont.txt"));
                cont++;
                writer.println(cont);
                writer.flush();
                writer.close();
                return cont+1;
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        return -1;
    }

    private ArrayList<String> idLavagneAccesso(String nomeUtente){
        ArrayList<String> idLavagne = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new FileReader("whiteboard/src/main/resources/whiteboard/whiteboard/data/accessiUtenti.txt"))) {
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
        File file = new File("whiteboard/src/main/resources/whiteboard/whiteboard/data/accessiUtenti.txt");
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
            if (lavagnaAggiunta || !utenteTrovato) {
                PrintWriter writer = new PrintWriter(new FileWriter(file));
                for (String riga : contenutoFile) {
                    writer.println(riga);
                }
                writer.flush();
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

            String nomeUtente = "";
            LogsLavagne lg=new LogsLavagne();
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
                    case "LAVAGNA_CLOSE":
                        if (idLavagnaAttuale == null) {
                            msgErr(out, "ERR_LAVAGNA_NON_SELEZIONATA");
                        } else {
                            gestLAVAGNA_CLOSE(richiesta.getParametro1(),out);
                        }
                        break;
                    case "USER_GETLAVAGNE":
                        //Aggiorno con le conoscenze delle lavagne attuali legate all'utente
                        nomeUtente=richiesta.getParametro1(); //ottengo il nome utente
                        lg.setIdLavagneSalvate(idLavagneAccesso(nomeUtente));
                        out.println(lg);
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
        String lavagnaIdNuovo = nomeLavagna+"£"+ operazioniCont(true);
        System.out.println("Il numero di lavagne ora è : "+operazioniCont(false));
        // Aggiunta alle liste di salvataggio
        lavagneIdAttive.add(lavagnaIdNuovo);
        Stato iniziale = new Stato(null);
        lavagneUtentiAttivi.add(new ArrayList<>());

        // Scrittura iniziale su file
        scriviStato(lavagnaIdNuovo, iniziale);

        out.println(mapper.writeValueAsString(new Logs(null, lavagnaIdNuovo)));
        System.out.println("[SERVER][CLIENT] Creata nuova lavagna con ID: " + lavagnaIdNuovo + ", nome: " + nomeLavagna + ". Inviato ID al client.");

        try {
            String statoRicevuto = in.readLine();
            System.out.println("[SERVER][LAVAGNA " + lavagnaIdNuovo + "][CLIENT] Ricevuto stato iniziale: " + statoRicevuto);
            if (statoRicevuto != null) {
                Stato statoIniziale = mapper.readValue(statoRicevuto, Stato.class);
                if (isIdValido(lavagnaIdNuovo)) {
                    // Aggiorno il file con lo stato ricevuto
                    scriviStato(lavagnaIdNuovo, statoIniziale);
                }
                aggiungiUtenteAttivoAllaLavagna(lavagnaIdNuovo, out);
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
        if (isIdValido(lavagnaId)) {
            if (!lavagneIdAttive.contains(lavagnaId)) {
                // aggiungo sia l’ID sia la lista vuota di utenti attivi
                lavagneIdAttive.add(lavagnaId);
                lavagneUtentiAttivi.add(new ArrayList<>());    // ← qui
                System.out.println("[SERVER] Aggiunta lavagna attiva con ID: " + lavagnaId);
            }
            aggiungiUtenteAttivoAllaLavagna(lavagnaId, out);
            out.println(mapper.writeValueAsString(new Logs(null, lavagnaId.split("£")[0])));
            out.println(mapper.writeValueAsString(leggiStato(lavagnaId)));
        } else {
            msgERR_LAVAGNA_INTROVABILE(out, lavagnaId);
        }
    }


    private void gestLAVAGNA_UPDATE(String lavagnaId, String statoJSON, PrintWriter outClient) {
        try {
            Stato statoNuovo = mapper.readValue(statoJSON, Stato.class);
            if (isIdValido(lavagnaId)) {
                scriviStato(lavagnaId, statoNuovo);
            }
            floodingUpdate(lavagnaId, statoNuovo, outClient);
        } catch (IOException e) {
            System.err.println("[SERVER][LAVAGNA " + lavagnaId + "] Errore nella lettura dell'aggiornamento: " + e.getMessage());
        }
    }

    private void gestLAVAGNA_CLOSE(String lavagnaId, PrintWriter utente) {
        if (isIdValido(lavagnaId)) {
            List<PrintWriter> utenti = lavagneUtentiAttivi.get(lavagneIdAttive.indexOf(lavagnaId));
            synchronized (utenti) {
                utenti.remove(utente);
                System.out.println("[SERVER][LAVAGNA " + lavagnaId + "] Writer rimosso.");
            }
        }
    }

    private void aggiungiUtenteAttivoAllaLavagna(String lavagnaId, PrintWriter outUtente) {
        int idx = lavagneIdAttive.indexOf(lavagnaId);
        if (idx == -1) return; // Se non c’è (ritorna -1), esco subito: niente da fare
        //    Se per qualche motivo manca, aggiungo nuove liste vuote
        while (lavagneUtentiAttivi.size() <= idx) {
            lavagneUtentiAttivi.add(new ArrayList<>());
        }
        List<PrintWriter> writers = lavagneUtentiAttivi.get(idx);
        synchronized (writers) {
            writers.add(outUtente);
            System.out.println("[SERVER][LAVAGNA " + lavagnaId + "] Writer aggiunto.");
        }
    }

    private void cancellaLavagna(String lavagnaId) {
        if (isIdValido(lavagnaId)) {
            int index=lavagneIdAttive.indexOf(lavagnaId);
            lavagneIdAttive.remove(index);
            lavagneUtentiAttivi.remove(index);
            System.out.println("[SERVER][LAVAGNA " + lavagnaId + "] Rimossa.");
        }
    }

    private void floodingUpdate(String lavagnaId, Stato nuovoStato, PrintWriter utenteRicercato) throws IOException {
        if (isIdValido(lavagnaId)) { //Sempre se esiste
            List<PrintWriter> utenti = lavagneUtentiAttivi.get(lavagneIdAttive.indexOf(lavagnaId)); //Lista di out delle connessioni
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

    public boolean isIdValido (String idDaCercare) {
        try (BufferedReader reader = new BufferedReader(new FileReader("whiteboard/src/main/resources/whiteboard/whiteboard/data/accessiUtenti.txt"))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                // Esempio riga: u1;id1,id2,id3
                String[] parti = linea.split(";");
                if (parti.length == 2) {
                    String[] idList = parti[1].split(",");
                    for (String id : idList) {
                        if (id.trim().equals(idDaCercare)) {
                            return true; // Trovato
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nella lettura del file: " + e.getMessage());
        }

        return false; // Non trovato
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
