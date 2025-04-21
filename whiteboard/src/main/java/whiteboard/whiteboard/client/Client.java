package whiteboard.whiteboard.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import whiteboard.whiteboard.azioni.*;
import whiteboard.whiteboard.serializer.*;
import java.io.*;
import java.net.*;

public class Client {
    // Variabili locali
    private String nomeUtente, nomeLavagna, idLavagna;
    private boolean isLavagnaOn;
    private LogsLavagne logsLavagne; //lavagne a cui posso accedere perchè create in precedenza o a cui ho già fatto accesso

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
    public LogsLavagne firstConfiguration(){
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

            //Invio al server le mie info (Nome utente) e questo mi restituisce tutti gli id delle lavagne a me associate
            inviaMessaggio(mapper.writeValueAsString(new Logs("USER_GETLAVAGNE", this.nomeUtente)));
            logsLavagne=mapper.readValue(in.readLine(),LogsLavagne.class);
            return logsLavagne; //Aggiorno anche l'interfacciamento grafico
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(String nomeLavagna, String idLavagna) {
        boolean isOkToCOntinue=false;
        try {
            // Configurazione iniziale della lavagna
            if (nomeLavagna == null) { // Se la lavagna esiste già
                inviaMessaggio(mapper.writeValueAsString(new Logs("LAVAGNA_OLD", idLavagna)));
                Logs json=mapper.readValue(in.readLine(), Logs.class);
                if (json.getNomeDelComando().startsWith("ERR_"))
                    mostraErrore(json.getNomeDelComando());
                else{
                    this.nomeLavagna =json.getParametro1();
                    statoLavagna = mapper.readValue(in.readLine(), Stato.class);
                    statoLavagna.setClient(this);
                    this.idLavagna = idLavagna;
                    isOkToCOntinue=true;
                }
            } else { // Se la lavagna è nuova
                this.nomeLavagna=nomeLavagna;
                inviaMessaggio(mapper.writeValueAsString(new Logs("LAVAGNA_NEW", nomeLavagna)));
                Logs json=mapper.readValue(in.readLine(), Logs.class);
                if (json.getNomeDelComando().startsWith("ERR_"))
                    mostraErrore(json.getNomeDelComando());
                else{
                    this.idLavagna =json.getParametro1();
                    statoLavagna = new Stato(this);
                    inviaMessaggio(mapper.writeValueAsString(statoLavagna)); // Aggiorna il server con lo stato della lavagna
                    isOkToCOntinue=true;
                }
            }

            if (isOkToCOntinue){
                // Esegui il cambio della vista della lavagna nel thread UI
                Platform.runLater(() -> {
                    try {
                        lavagnaController = clientController.cambiaLavagnaView(this.nomeLavagna,this.idLavagna,statoLavagna);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                isLavagnaOn = true;

                // Ciclo per ricevere aggiornamenti sulla lavagna
                while (connessione.isConnected() && isLavagnaOn) {
                    String linea = in.readLine();
                    if (linea != null) {
                        Logs log = mapper.readValue(linea, Logs.class);
                        String cmd = log.getNomeDelComando();

                        switch (cmd) {
                            case "LAVAGNA_UPDATE":
                                // Aggiorna lo stato grafico
                                Stato nuovoStato = mapper.readValue(log.getParametro1(), Stato.class);
                                statoLavagna.aggiornaSeDiverso( nuovoStato, lavagnaController.getContestoGrafico(), lavagnaController.getCanvas());
                                break;

                            case "LAVAGNA_CLOSE_ACK":
                                inviaMessaggio(mapper.writeValueAsString(new Logs("USER_GETLAVAGNE", this.nomeUtente)));
                                LogsLavagne lg=mapper.readValue(in.readLine(),LogsLavagne.class);
                                Platform.runLater(() ->  this.clientController.creaGrigliaHome(this,lg));
                                isLavagnaOn = false;  // esco dal loop
                                break;

                            default:
                                if (cmd.startsWith("ERR_")) {
                                    mostraErrore(cmd);
                                }
                                // altrimenti ignoro comandi sconosciuti
                        }
                    }
                }
            }
        } catch (UnknownHostException unknownHost) {
            System.err.println(unknownHost.getMessage()); // Errore se l'host non è valido
        } catch (IOException ioException) {
            System.err.println(ioException.getMessage()); // Errore di I/O
        }
    }

    public void chiudiConnessione(){
        try {
            // Chiusura delle risorse
            in.close();
            out.close();
            connessione.close();
        } catch (Exception ioException) {
            System.err.println(ioException); // Errore durante la chiusura della connessione
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

    /*METODI PER LA GESTIONE DI EVENTI*/

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

    // Metodo per fermare la lavagna
    public void chiudiLavagna() {
        try {
            //CHIEDO DI INTERROMPERE IL FLOODING
            inviaMessaggio(mapper.writeValueAsString(new Logs("LAVAGNA_CLOSE", idLavagna)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void mostraErrore(String codiceErrore) {
        // Mostra popup in caso di errore
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText(codiceErrore);
            //Imposto l'icona della "finestra di alert mantenendola in tema"
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image((getClass().getResource("/whiteboard/whiteboard/img/logo.png").toString())));

            alert.showAndWait(); //Modale
        });
    }


    //getter e setter
    public String getNomeUtente() { return nomeUtente; }
    public void setClientController(ClientController clientController) { this.clientController = clientController; }
}
