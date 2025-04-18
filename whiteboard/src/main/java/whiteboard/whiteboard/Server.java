package whiteboard.whiteboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import javafx.scene.paint.Color;
import whiteboard.whiteboard.azioni.*;
import whiteboard.whiteboard.serializer.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final ServerSocket serverSocket;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, String> nomiLavagne = new ConcurrentHashMap<>();
    private final Map<String, Stato> statiLavagne = new ConcurrentHashMap<>();
    private final Map<String, List<PrintWriter>> writersLavagne = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public Server(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);

        // Configurazione dell'ObjectMapper per supportare la serializzazione/deserializzazione di Color
        SimpleModule colorModule = new SimpleModule();
        colorModule.addSerializer(Color.class, new ColorSerializer());
        colorModule.addDeserializer(Color.class, new ColorDeserializer());
        mapper.registerModule(colorModule);

        System.out.println("[SERVER] Server avviato sulla porta " + port);
    }

    public void start() {
        System.out.println("[SERVER] In attesa di connessioni...");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] Nuova connessione da: " + clientSocket.getInetAddress().getHostAddress());
                executorService.submit(() -> gestisciClient(clientSocket));
            } catch (IOException e) {
                System.err.println("[SERVER] Errore durante l'accettazione di una nuova connessione: " + e.getMessage());
            }
        }
    }

    private void gestisciClient(Socket socket) {
        String clientAddress = socket.getInetAddress().getHostAddress();
        System.out.println("[SERVER][CLIENT " + clientAddress + "] Inizio gestione client.");
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)
        ) {
            Logs messaggioConnessione = new Logs("CONNECTION_ACCEPTED", null);
            String messaggioConnessioneJson = mapper.writeValueAsString(messaggioConnessione);
            out.println(messaggioConnessioneJson);
            System.out.println("[SERVER][CLIENT " + clientAddress + "] Messaggio inviato al client: " + messaggioConnessioneJson);

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("[SERVER][CLIENT " + clientAddress + "] Ricevuta richiesta: " + line);
                try {
                    Logs richiesta = mapper.readValue(line, Logs.class);
                    String comando = richiesta.getNomeDelComando();
                    System.out.println("[SERVER][CLIENT " + clientAddress + "] Comando ricevuto: " + comando);

                    if ("LAVAGNA_NEW".equals(comando)) {
                        String nomeLavagna = richiesta.getParametro1();
                        System.out.println("[SERVER][CLIENT " + clientAddress + "] Richiesta creazione nuova lavagna con nome: " + nomeLavagna);
                        creaNuovaLavagna(nomeLavagna, in, out, socket);
                    } else if ("LAVAGNA_OLD".equals(comando)) {
                        String idLavagna = richiesta.getParametro1();
                        System.out.println("[SERVER][CLIENT " + clientAddress + "] Richiesta connessione a lavagna esistente con ID: " + idLavagna);
                        connettiALavagnaEsistente(idLavagna, in, out, socket);
                    }  else if ("LAVAGNA_UPDATE".equals(comando)) {
                        String statoJson = richiesta.getParametro1();
                        Stato nuovoStato = mapper.readValue(statoJson, Stato.class);
                        statiLavagne.put(richiesta.getParametro1(), nuovoStato);
                        broadcastStato(richiesta.getParametro1(), nuovoStato, out);
                    }else {
                        System.out.println("[SERVER][CLIENT " + clientAddress + "] Comando non riconosciuto: " + comando);
                        Logs messaggioErrore = new Logs("COMANDO_NON_RICONOSCIUTO", null);
                        String messaggioErroreJson = mapper.writeValueAsString(messaggioErrore);
                        out.println(messaggioErroreJson);
                        System.out.println("[SERVER][CLIENT " + clientAddress + "] Inviato errore: " + messaggioErroreJson);
                    }
                } catch (IOException e) {
                    System.err.println("[SERVER][CLIENT " + clientAddress + "] Errore nella lettura della richiesta del client: " + e.getMessage());
                    break; // Interrompi la comunicazione con questo client in caso di errore di lettura
                }
            }
        } catch (IOException e) {
            System.err.println("[SERVER][CLIENT " + clientAddress + "] Errore durante la gestione del client: " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println("[SERVER][CLIENT " + clientAddress + "] Connessione chiusa.");
            } catch (IOException e) {
                System.err.println("[SERVER][CLIENT " + clientAddress + "] Errore durante la chiusura del socket del client: " + e.getMessage());
            }
        }
    }

    private void creaNuovaLavagna(String nomeLavagna, BufferedReader in, PrintWriter out, Socket socket) throws IOException {
        String clientAddress = socket.getInetAddress().getHostAddress();
        String idLavagna = UUID.randomUUID().toString();
        nomiLavagne.put(idLavagna, nomeLavagna);
        statiLavagne.put(idLavagna, new Stato(null));
        writersLavagne.put(idLavagna, new ArrayList<>());
        Logs messaggioIdLavagna = new Logs(null, idLavagna);
        String messaggioIdLavagnaJson = mapper.writeValueAsString(messaggioIdLavagna);
        out.println(messaggioIdLavagnaJson);
        System.out.println("[SERVER][CLIENT " + clientAddress + "] Creata nuova lavagna con ID: " + idLavagna + ", nome: " + nomeLavagna + ". Inviato ID al client: " + messaggioIdLavagnaJson);

        try {
            String statoInizialeJson = in.readLine();
            System.out.println("[SERVER][LAVAGNA " + idLavagna + "][CLIENT " + clientAddress + "] Ricevuto stato iniziale: " + statoInizialeJson);
            if (statoInizialeJson != null) {
                Stato statoIniziale = mapper.readValue(statoInizialeJson, Stato.class);
                statiLavagne.put(idLavagna, statoIniziale);
                aggiungiWriter(idLavagna, out);
                gestisciAggiornamenti(socket, in, idLavagna, out);
            } else {
                System.err.println("[SERVER][LAVAGNA " + idLavagna + "][CLIENT " + clientAddress + "] Nessuno stato iniziale ricevuto.");
                rimuoviLavagna(idLavagna); // Rimuovi la lavagna se non ricevi lo stato iniziale
                Logs messaggioErrore = new Logs("ERRORE_STATO_INIZIALE", null);
                String messaggioErroreJson = mapper.writeValueAsString(messaggioErrore);
                out.println(messaggioErroreJson);
                System.out.println("[SERVER][LAVAGNA " + idLavagna + "][CLIENT " + clientAddress + "] Inviato errore stato iniziale.");
            }
        } catch (IOException e) {
            System.err.println("[SERVER][LAVAGNA " + idLavagna + "][CLIENT " + clientAddress + "] Errore nella lettura dello stato iniziale: " + e.getMessage());
            rimuoviLavagna(idLavagna); // Rimuovi la lavagna in caso di errore
            Logs messaggioErrore = new Logs("ERRORE_STATO_INIZIALE", null);
            String messaggioErroreJson = mapper.writeValueAsString(messaggioErrore);
            out.println(messaggioErroreJson);
            System.out.println("[SERVER][LAVAGNA " + idLavagna + "][CLIENT " + clientAddress + "] Inviato errore stato iniziale.");
        }
    }

    private void connettiALavagnaEsistente(String idLavagna, BufferedReader in, PrintWriter out, Socket socket) throws IOException {
        String clientAddress = socket.getInetAddress().getHostAddress();
        System.out.println("[SERVER][CLIENT " + clientAddress + "] Tentativo di connessione alla lavagna con ID: " + idLavagna);
        if (nomiLavagne.containsKey(idLavagna)) {
            String nome = nomiLavagne.get(idLavagna);
            Stato stato = statiLavagne.get(idLavagna);
            aggiungiWriter(idLavagna, out);
            Logs messaggioNomeLavagna = new Logs(null, nome);
            String messaggioNomeLavagnaJson = mapper.writeValueAsString(messaggioNomeLavagna);
            out.println(messaggioNomeLavagnaJson);
            System.out.println("[SERVER][LAVAGNA " + idLavagna + "][CLIENT " + clientAddress + "] Inviato nome lavagna: " + messaggioNomeLavagnaJson);
            String statoJson = mapper.writeValueAsString(stato);
            out.println(statoJson);
            System.out.println("[SERVER][LAVAGNA " + idLavagna + "][CLIENT " + clientAddress + "] Inviato stato corrente: " + statoJson);
            gestisciAggiornamenti(socket, in, idLavagna, out);
        } else {
            System.out.println("[SERVER][CLIENT " + clientAddress + "] Lavagna con ID " + idLavagna + " non trovata.");
            Logs messaggioErrore = new Logs("LAVAGNA_NON_TROVATA", null);
            String messaggioErroreJson = mapper.writeValueAsString(messaggioErrore);
            out.println(messaggioErroreJson);
            System.out.println("[SERVER][CLIENT " + clientAddress + "] Inviato errore: " + messaggioErroreJson);
        }
    }

    private void gestisciAggiornamenti(Socket socket, BufferedReader in, String idLavagna, PrintWriter clientOut) {
        String clientAddress = socket.getInetAddress().getHostAddress();
        executorService.submit(() -> {
            try {
                String json;
                while ((json = in.readLine()) != null) {
                    System.out.println("[SERVER][LAVAGNA " + idLavagna + "][CLIENT " + clientAddress + "] Ricevuto aggiornamento: " + json);
                    try {
                        Stato nuovoStato = mapper.readValue(json, Stato.class);
                        statiLavagne.put(idLavagna, nuovoStato);

                        List<PrintWriter> writers = writersLavagne.get(idLavagna);
                        if (writers != null) {
                            synchronized (writers) {
                                for (PrintWriter writer : writers) {
                                    if (writer != clientOut) {
                                        try {
                                            String nuovoStatoJson = mapper.writeValueAsString(nuovoStato);
                                            writer.println(nuovoStatoJson);
                                            writer.flush();
                                            System.out.println("[SERVER][LAVAGNA " + idLavagna + "] Aggiornamento inviato a un altro client.");
                                        } catch (Exception e) {
                                            System.err.println("[SERVER][LAVAGNA " + idLavagna + "] Errore durante il broadcast: " + e.getMessage());
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("[SERVER][LAVAGNA " + idLavagna + "][CLIENT " + clientAddress + "] Errore nella lettura dell'aggiornamento: " + e.getMessage());
                        break; // Interrompi il loop di lettura se c'è un errore
                    }
                }
            } catch (IOException e) {
                System.out.println("[SERVER][LAVAGNA " + idLavagna + "][CLIENT " + clientAddress + "] Disconnesso.");
            } finally {
                rimuoviWriter(idLavagna, clientOut);
                System.out.println("[SERVER][LAVAGNA " + idLavagna + "][CLIENT " + clientAddress + "] PrintWriter rimosso.");
            }
        });
    }

    private void aggiungiWriter(String idLavagna, PrintWriter writer) {
        List<PrintWriter> writers = writersLavagne.computeIfAbsent(idLavagna, k -> new ArrayList<>());
        synchronized (writers) {
            writers.add(writer);
            System.out.println("[SERVER][LAVAGNA " + idLavagna + "] Writer aggiunto.");
        }
    }

    private void rimuoviWriter(String idLavagna, PrintWriter writer) {
        List<PrintWriter> writers = writersLavagne.get(idLavagna);
        if (writers != null) {
            synchronized (writers) {
                writers.remove(writer);
                System.out.println("[SERVER][LAVAGNA " + idLavagna + "] Writer rimosso.");
            }
        }
    }

    private void rimuoviLavagna(String idLavagna) {
        nomiLavagne.remove(idLavagna);
        statiLavagne.remove(idLavagna);
        writersLavagne.remove(idLavagna);
        System.out.println("[SERVER][LAVAGNA " + idLavagna + "] Rimossa.");
    }

    private void broadcastStato(String idLavagna, Stato nuovoStato, PrintWriter clientOut) throws IOException {
        List<PrintWriter> writers = writersLavagne.get(idLavagna);
        if (writers != null) {
            synchronized (writers) {
                for (PrintWriter writer : writers) {
                    if (writer != clientOut) {
                        try {
                            writer.println(mapper.writeValueAsString(new Logs("LAVAGNA_UPDATE", mapper.writeValueAsString(nuovoStato))));
                            writer.flush();
                        } catch (Exception e) {
                            System.err.println("[SERVER][LAVAGNA " + idLavagna + "] Errore durante il broadcast: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            new Server(9999).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}