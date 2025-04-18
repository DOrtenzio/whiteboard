package whiteboard.whiteboard.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import whiteboard.whiteboard.azioni.*;
import whiteboard.whiteboard.serializer.*;
import java.io.*;
import java.net.*;

public class Client {
    // Variabili locali
    private String nomeUtente, nomeLavagna, idLavagna;
    private boolean isLavagnaOn;

    // Variabili per controller
    private ClientController clientController;
    private LavagnaController lavagnaController;
    private Stato statoLavagna;

    // Variabili per la connessione
    private Socket connessione;
    private PrintWriter out;
    private BufferedReader in;
    private ObjectMapper mapper;

    // Costruttore
    public Client(String nomeUtente, ClientController clientController) {
        this.mapper = new ObjectMapper(); // Creazione dell'ObjectMapper per la serializzazione/deserializzazione
        this.nomeUtente = nomeUtente;
        this.clientController = clientController;
        this.isLavagnaOn = false;

        // Configurazione dell'ObjectMapper per supportare la serializzazione/deserializzazione di Color
        SimpleModule colorModule = new SimpleModule();
        colorModule.addSerializer(Color.class, new ColorSerializer());
        colorModule.addDeserializer(Color.class, new ColorDeserializer());
        mapper.registerModule(colorModule);
    }

    // Metodo per avviare la connessione e gestire la lavagna
    public void run(String nomeLavagna, String idLavagna) {
        try {
            // Connessione al server
            connessione = new Socket("localhost", 9999);
            System.out.println("CONNECTION_REQUEST");

            // Flusso di output e input
            out = new PrintWriter(connessione.getOutputStream());
            out.flush();
            in = new BufferedReader(new InputStreamReader(connessione.getInputStream()));

            // Verifica della connessione
            System.out.println(mapper.readValue(in.readLine(), Logs.class).getNomeDelComando()); // Restituisce "CONNECTION_ACCEPTED" o "CONNECTION_REFUSED"

            // Configurazione iniziale della lavagna
            if (nomeLavagna == null) { // Se la lavagna esiste già
                inviaMessaggio(mapper.writeValueAsString(new Logs("LAVAGNA_OLD", idLavagna)));
                this.nomeLavagna = mapper.readValue(in.readLine(), Logs.class).getParametro1();
                statoLavagna = mapper.readValue(in.readLine(), Stato.class);
                statoLavagna.setClient(this);
            } else { // Se la lavagna è nuova
                inviaMessaggio(mapper.writeValueAsString(new Logs("LAVAGNA_NEW", nomeLavagna)));
                this.idLavagna = mapper.readValue(in.readLine(), Logs.class).getParametro1();
                statoLavagna = new Stato(this);
                inviaMessaggio(mapper.writeValueAsString(statoLavagna)); // Aggiorna il server con lo stato della lavagna
            }

            // Esegui il cambio della vista della lavagna nel thread UI
            Platform.runLater(() -> {
                try {
                    lavagnaController = clientController.cambiaLavagnaView(nomeLavagna, statoLavagna);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            isLavagnaOn = true;

            // Ciclo per ricevere aggiornamenti sulla lavagna
            do {
                String linea = in.readLine();
                if (linea != null) {
                    Logs log = mapper.readValue(linea, Logs.class);
                    if ("LAVAGNA_UPDATE".equals(log.getNomeDelComando())) {
                        Stato nuovoStato = mapper.readValue(log.getParametro1(), Stato.class);
                        statoLavagna.aggiornaSeDiverso(nuovoStato, lavagnaController.getContestoGrafico(), lavagnaController.getCanvas());
                    }
                }
            } while (connessione.isConnected() && isLavagnaOn);

        } catch (UnknownHostException unknownHost) {
            System.err.println(unknownHost.getMessage()); // Errore se l'host non è valido
        } catch (IOException ioException) {
            System.err.println(ioException.getMessage()); // Errore di I/O
        } finally {
            try {
                // Chiusura delle risorse
                in.close();
                out.close();
                connessione.close();
            } catch (Exception ioException) {
                System.err.println(ioException); // Errore durante la chiusura della connessione
            }
        }
    }

    // Metodo per inviare un aggiornamento dello stato della lavagna
    public void inviaAggiornamentoStato() {
        try {
            inviaMessaggio(mapper.writeValueAsString(new Logs("LAVAGNA_UPDATE", mapper.writeValueAsString(statoLavagna))));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); // Errore durante la serializzazione
        }
    }

    // Metodo per inviare un messaggio al server
    private void inviaMessaggio(String msg) {
        try {
            PrintWriter pw = new PrintWriter(out);
            pw.println(msg);
            pw.flush();
        } catch (Exception ioException) {
            ioException.printStackTrace(); // Errore durante l'invio del messaggio
        }
    }

    // Metodo per concludere la connessione e fermare la lavagna
    public void concludi() {
        isLavagnaOn = false; //TODO: Implementa con la chiusura (Domani ora non ho più voglia)
    }
}
