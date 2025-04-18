package whiteboard.whiteboard;

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
    //Var locali
    private String idClient, nomeLavagna, idLavagna;
    private boolean isLavagnaOn;

    //Var controller
    private ClientController clientController;
    private LavagnaController lavagnaController;
    private Stato statoLavagna;

    //Var per connection
    private Socket connessione;
    private PrintWriter out;
    private BufferedReader in;
    private ObjectMapper mapper;

    //Metodi e costruttori
    public Client(String idClient, ClientController clientController) {
        this.mapper = new ObjectMapper();
        this.idClient=idClient;
        this.clientController=clientController;
        this.isLavagnaOn=false;

        // Configurazione dell'ObjectMapper per supportare la serializzazione/deserializzazione di Color
        SimpleModule colorModule = new SimpleModule();
        colorModule.addSerializer(Color.class, new ColorSerializer());
        colorModule.addDeserializer(Color.class, new ColorDeserializer());
        mapper.registerModule(colorModule);
    }

    public void run(String nomeLavagna, String idLavagna) {
        try {
            //Connessione con il server
            connessione = new Socket("localhost", 9999);
            System.out.println("CONNECTION_REQUEST");
            out = new PrintWriter(connessione.getOutputStream());
            out.flush();
            in = new BufferedReader(new InputStreamReader(connessione.getInputStream()));
            System.out.println(mapper.readValue(in.readLine(), Logs.class).getNomeDelComando()); //Sarà "CONNECTION_ACCEPTED" o "CONNECTION_REFUSED"

            //Fase iniziale di configuramento della lavagna su cui lavorare
            if (nomeLavagna==null) { //Lavagna già preesistente
                inviaMessaggio(mapper.writeValueAsString(new Logs("LAVAGNA_OLD",idLavagna)));
                this.nomeLavagna= mapper.readValue(in.readLine(), Logs.class).getParametro1();
                statoLavagna= mapper.readValue(in.readLine(), Stato.class);
            } else { //lavagna Nuova
                inviaMessaggio(mapper.writeValueAsString(new Logs("LAVAGNA_NEW",nomeLavagna)));
                this.idLavagna= mapper.readValue(in.readLine(), Logs.class).getParametro1();
                statoLavagna=new Stato(this);
                inviaMessaggio(mapper.writeValueAsString(statoLavagna)); //Aggiorno anche il server che non lavora con gli oggetti client ma solo con gli id
            }

            Platform.runLater(() -> {
                try {
                    lavagnaController=clientController.cambiaLavagnaView(nomeLavagna,statoLavagna);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            isLavagnaOn=true;
            //Al termine di quest'operazione abbiamo avviato la lavagna di qui abbiamo id e nome

            //CICLO di aggiornamenti continui
            do {
                String linea=in.readLine();
                if (linea!=null) {
                    Logs log = mapper.readValue(linea, Logs.class);
                    if ("LAVAGNA_UPDATE".equals(log.getNomeDelComando())) {
                        Stato nuovoStato = mapper.readValue(log.getParametro1(), Stato.class);
                        statoLavagna.aggiornaSeDiverso(nuovoStato, lavagnaController.getContestoGrafico(), lavagnaController.getCanvas());
                    }
                }
            } while(connessione.isConnected() && isLavagnaOn);

        } catch (UnknownHostException unknownHost) {
            System.err.println(unknownHost.getMessage());
        } catch (IOException ioException) {
            System.err.println(ioException.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                connessione.close();
            } catch (Exception ioException) {
                System.err.println(ioException);
            }
        }
    }

    public void inviaAggiornamentoStato(){
        try {
            inviaMessaggio(mapper.writeValueAsString(new Logs("LAVAGNA_UPDATE", mapper.writeValueAsString(statoLavagna))));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void inviaMessaggio(String msg) {
        try {
            PrintWriter pw = new PrintWriter(out);
            pw.println(msg);
            pw.flush();
        } catch (Exception ioException) {
            ioException.printStackTrace();
        }
    }

    public void concludi() { isLavagnaOn = false; }
}
