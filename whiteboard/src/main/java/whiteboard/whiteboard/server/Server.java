package whiteboard.whiteboard.server;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import javafx.scene.paint.Color;
import whiteboard.whiteboard.azioni.*;
import whiteboard.whiteboard.serializer.*;
import java.io.*;
import java.net.*;
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

    /*FASE GESTIONALE DEL CLIENTE*/
    private void gestisciClientConnesso(Socket clientSocket) {
        System.out.println("[SERVER][CLIENT] Inizio gestione client.");
        String idLavagnaAttuale = null;

        try ( BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));  PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true)) {
            msgCONFERMA(out);

            String richiestaRicevuta;
            while ((richiestaRicevuta = in.readLine()) != null) {
                System.out.println("[SERVER][CLIENT] Ricevuta richiesta: " + richiestaRicevuta);
                Logs richiesta = mapper.readValue(richiestaRicevuta, Logs.class);
                //Azioni a seconda delle richieste pervenute
                switch (richiesta.getNomeDelComando()) {
                    case "LAVAGNA_NEW": //Devo creare una nuovo lavagna
                        idLavagnaAttuale = gestLAVAGNA_NEW(richiesta.getParametro1(), in, out);
                        break;
                    case "LAVAGNA_OLD": //Il bro si vuole connettere a una lavagna già creata
                        idLavagnaAttuale = richiesta.getParametro1();
                        gestLAVAGNA_OLD(idLavagnaAttuale, in, out);
                        break;
                    case "LAVAGNA_UPDATE":
                        if (idLavagnaAttuale == null) { //Impossibile vista la gestione grafica (CODICE FATTO PRIMA DELLA GESTIONE GRAFICA COMPLETA)
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

    /* GESTIONI */
    private String gestLAVAGNA_NEW(String nomeLavagna, BufferedReader in, PrintWriter out) throws IOException {
        String lavagnaIdNuovo = UUID.randomUUID().toString(); //TODO: CHANGE

        // Aggiunta alle liste di salvataggio
        lavagneId.add(lavagnaIdNuovo);
        lavagneNomi.add(nomeLavagna);
        lavagneStati.add(new Stato(null));
        lavagneUtenti.add(new ArrayList<>());

        out.println(mapper.writeValueAsString(new Logs(null, lavagnaIdNuovo))); //Aggiornamento del client con il nuovo ID

        System.out.println("[SERVER][CLIENT] Creata nuova lavagna con ID: " + lavagnaIdNuovo + ", nome: " + nomeLavagna + ". Inviato ID al client.");

        /*RICEZIONE DELLO STATO DELLA LAVAGNA (ANCHE SE MOLTO PROBABILMENTE VUOTO)*/
        try {
            String statoRicevuto = in.readLine();
            System.out.println("[SERVER][LAVAGNA " + lavagnaIdNuovo + "][CLIENT] Ricevuto stato iniziale: " + statoRicevuto);
            if (statoRicevuto != null) { //Quindi la mappa è stata creata senza errori
                Stato statoIniziale = mapper.readValue(statoRicevuto, Stato.class);
                //Controllo se la lavagna esiste ed in caso associo
                int index = ottieniIndiceLavagna(lavagnaIdNuovo);
                if (index != -1) lavagneStati.set(index, statoIniziale); //Passaggio in più, essendo come prima il codice stato scritto prima del passaggio grafico
                //Termine
                aggiungiUtenteAttivoAllaLavagna(lavagnaIdNuovo, out);
                gestUPDATE(in, lavagnaIdNuovo, out);
            } else {
                msgERR_STATO(out, lavagnaIdNuovo);
            }
        } catch (IOException e) {
            try {
                msgERR_STATO(out, lavagnaIdNuovo);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        return lavagnaIdNuovo;
    }

    private void gestLAVAGNA_OLD(String lavagnaId, BufferedReader in, PrintWriter out) throws IOException {
        System.out.println("[SERVER][CLIENT] Tentativo di connessione alla lavagna con ID: " + lavagnaId);
        int index = ottieniIndiceLavagna(lavagnaId); //Cosi da accedere alle altre info nelle liste parallele
        if (index != -1) { //Se esiste
            aggiungiUtenteAttivoAllaLavagna(lavagnaId, out);
            //Invio info all'utente
            out.println(mapper.writeValueAsString(new Logs(null, lavagneNomi.get(index))));
            out.println(mapper.writeValueAsString(lavagneStati.get(index)));
            //Gestione update
            gestUPDATE(in, lavagnaId, out);
        } else {
            msgERR_LAVAGNA_INTROVABILE(out, lavagnaId);
        }
    }

    private void gestLAVAGNA_UPDATE(String lavagnaId, String statoJSON, PrintWriter outClient) {
        try {
            Stato statoNuovo = mapper.readValue(statoJSON, Stato.class);
            int index = ottieniIndiceLavagna(lavagnaId); //Controllo della lavagna
            if (index != -1) lavagneStati.set(index, statoNuovo);
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
                        Stato newState = mapper.readValue(json, Stato.class);
                        int idx = ottieniIndiceLavagna(lavagnaId);
                        if (idx != -1) lavagneStati.set(idx, newState);
                        floodingUpdate(lavagnaId, newState, out);
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
