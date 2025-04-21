# **Documentazione Whiteboard**

Questo progetto implementa un'applicazione di whiteboard collaborativa in tempo reale, permettendo a più utenti di disegnare e interagire sulla stessa lavagna virtuale. L'applicazione è strutturata secondo un'architettura client-server, utilizzando JavaFX per l'interfaccia utente e la libreria Jackson per la serializzazione e deserializzazione JSON dei dati scambiati tra client e server.

## Indice:
* [componenti-principali-componenti-principali](#componenti-principali-componenti-principali)
* [file-di-dati-file-di-dati](#file-di-dati-file-di-dati)
* [flusso-di-funzionamento-generale-flusso-di-esecuzione-semplificato](#flusso-di-funzionamento-generale-flusso-di-esecuzione-semplificato)
* [scaletta-di-esecuzione-flusso-di-esecuzione-approfondito](#scaletta-di-esecuzione-flusso-di-esecuzione-approfondito)
  * [scenario-1-utente-accede-e-crea-una-nuova-lavagna-crea-lavagna](#scenario-1-utente-accede-e-crea-una-nuova-lavagna-crea-lavagna)
  * [scenario-2-utente-accede-a-una-lavagna-esistente-accedi-a-lavagna](#scenario-2-utente-accede-a-una-lavagna-esistente-accedi-a-lavagna)

## **Componenti Principali:** {#componenti-principali}

1.  **`whiteboard.whiteboard.client` (Lato Client)**

    *   **`ClientController.java`:**
        *   Questo controller gestisce la **vista iniziale del client**, ovvero la schermata di accesso.
        *   Contiene elementi UI come un `TextField` per l'inserimento del nome utente e un `Button` per l'accesso.
        *   Il metodo `firstInitialize()` crea dinamicamente questi elementi UI.
        *   L'azione del `Button` "Accedi" crea un'istanza della classe `Client`, imposta il nome utente e passa alla schermata successiva (`postAccesso()`).
        *   `postAccesso()` si occupa di richiedere al server l'elenco delle lavagne associate all'utente tramite `client.firstConfiguration()`.
        *   Il metodo `backToHome()` e `creaGrigliaHome()` gestiscono la visualizzazione delle **lavagne disponibili** sotto forma di una griglia di bottoni. Questi bottoni possono rappresentare lavagne esistenti o le opzioni per crearne una nuova ("+") o aggiungerne una tramite codice ("Aggiungi con codice").
        *   La creazione di una nuova lavagna o l'aggiunta tramite codice comporta la presentazione di un nuovo `TextField` per l'inserimento del nome o del codice e un bottone per confermare, che a sua volta avvia il client per quella specifica lavagna tramite il metodo `startClient()`.
        *   `allBoardView()` mostra una **lista scorrevole di tutte le lavagne** a cui l'utente ha accesso. Ogni lavagna è rappresentata da un bottone contenente il nome e l'ID.
        *   Il metodo chiave `cambiaLavagnaView()` si occupa di **caricare la vista della lavagna vera e propria** (`lavagna-view.fxml`) e il suo controller (`LavagnaController`). Imposta la scena, gestisce la chiusura della finestra con un alert di conferma e passa le informazioni della lavagna (nome, ID, stato) al `LavagnaController`.

    *   **`HelloApplication.java`:**
        *   Questa è la **classe principale dell'applicazione client**, estendendo `javafx.application.Application`.
        *   Il metodo `start(Stage stage)` carica il file FXML per la vista iniziale del client (`client-view.fxml`) e lo imposta come scena principale.
        *   Imposta il titolo della finestra ("SketchIt"), la rende non ridimensionabile e aggiunge un'icona.
        *   Gestisce l'**evento di chiusura della finestra** con un alert di conferma, assicurandosi che la connessione del client venga chiusa correttamente.
        *   Il metodo `main()` lancia l'applicazione JavaFX.

    *   **`Client.java`:**
        *   Questa classe gestisce la **logica di connessione del client al server** e la comunicazione.
        *   Mantiene informazioni locali come `nomeUtente`, `nomeLavagna`, `idLavagna` e lo stato della lavagna (`Stato`).
        *   Possiede riferimenti ai controller (`ClientController`, `LavagnaController`).
        *   Le variabili per la connessione includono `Socket`, `PrintWriter` (output), `BufferedReader` (input) e un `ObjectMapper` per la gestione JSON.
        *   Il costruttore inizializza l'`ObjectMapper` e registra un modulo personalizzato (`ColorModule`) per serializzare e deserializzare oggetti `javafx.scene.paint.Color`.
        *   `firstConfiguration()` stabilisce la **connessione con il server** (su `localhost:9999`), invia il nome utente e riceve l'elenco delle lavagne associate.
        *   Il metodo `run(String nomeLavagna, String idLavagna)` viene eseguito in un thread separato e gestisce la **configurazione iniziale della lavagna**, sia essa nuova o esistente, comunicando con il server.
        *   Dopo la configurazione iniziale, avvia il cambio della vista della lavagna tramite `Platform.runLater()` per assicurare che l'aggiornamento dell'UI avvenga nel thread corretto.
        *   Entra in un **ciclo continuo per ricevere aggiornamenti sulla lavagna** dal server (`LAVAGNA_UPDATE`) e aggiorna lo stato locale e l'interfaccia utente. Gestisce anche la notifica di chiusura della lavagna (`LAVAGNA_CLOSE_ACK`).
        *   `chiudiConnessione()` chiude i flussi di input/output e la socket di connessione con il server.
        *   `inviaAggiornamentoStato()` serializza e invia lo stato corrente della lavagna al server.
        *   `inviaMessaggio()` è un metodo privato per inviare stringhe (generalmente JSON) al server.
        *   `chiudiLavagna()` notifica il server che il client ha chiuso la lavagna attuale.
        *   `mostraErrore()` visualizza un popup di errore in caso di comunicazione fallita o errori riportati dal server.

    *   **`LavagnaController.java`:**
        *   Questo controller gestisce l'**interfaccia utente della lavagna**.
        *   Ha riferimenti a elementi grafici come `Canvas`, `AnchorPane` (per i box degli strumenti), `ImageView` (per i bottoni), `ColorPicker`, `Spinner`, `ChoiceBox`, `CheckBox` e `Label`.
        *   Mantiene lo `Stato` corrente della lavagna, un flag per la condivisione (`isShareBoxActive`) e un riferimento al `Client`.
        *   `initialize()` inizializza il `GraphicsContext` del canvas, sbianca la lavagna, imposta i valori predefiniti dei controlli e aggiunge i listener per le interazioni dell'utente (click sui bottoni, modifiche ai color picker, ecc.).
        *   I metodi come `attivaScrittura()`, `attivaCancellazione()` e `attivaFigure()` gestiscono il **cambiamento delle modalità di disegno**, mostrando/nascondendo i pannelli degli strumenti pertinenti e aggiornando le variabili di stato.
        *   Le funzioni `clickMouseLavagna()`, `trascinoMouseLavagna()` e `rilascioMouseLavagna()` gestiscono gli **eventi del mouse sul canvas**, avviando, continuando e terminando il disegno di linee, l'applicazione della gomma e il disegno di figure.
        *   Metodi come `iniziaLinea()`, `continuaLinea()`, `cancella()`, `disegnaFigura()` e `sbiancaLavagna()` eseguono le **operazioni di disegno effettive sul canvas** e aggiornano lo `Stato` della lavagna.
        *   `undo()` implementa la **funzionalità di annullamento dell'ultima azione**.
        *   `operateShare()` gestisce la **visibilità del pannello di condivisione** (anche se la sua funzionalità specifica non è dettagliata nel codice fornito).
        *   `concludi()` notifica la chiusura della lavagna al server e ritorna alla vista principale del client.
        *   I metodi `setLavagnaNome()`, `setLavagnaId()` e `setStatoLavagna()` sono usati per **impostare le informazioni della lavagna** visualizzate e per disegnare lo stato iniziale ricevuto dal server.
        *   `getCanvas()` e `getContestoGrafico()` forniscono accesso al canvas e al suo contesto grafico.
        *   I metodi `setColoreTratto()`, `setSpessoreLinea()`, ecc., impostano le **proprietà grafiche** in base alle selezioni dell'utente.
        *   Le animazioni per l'entrata e l'uscita dei pannelli degli strumenti sono gestite da `entrataPannello()` e `uscitaPannello()`, che utilizzano `TranslateTransition`.

2.  **`whiteboard.whiteboard.server` (Lato Server)**

    *   **`Server.java`:**
        *   Questa classe rappresenta il **server dell'applicazione**.
        *   Contiene una `ServerSocket` per accettare connessioni dai client e un `ObjectMapper` per la gestione JSON.
        *   Utilizza `ArrayList` per tenere traccia delle **lavagne attive** (`lavagneIdAttive`) e degli **utenti attivi su ciascuna lavagna** (`lavagneUtentiAttivi`).
        *   Il costruttore `Server()` inizializza le strutture dati, configura l'`ObjectMapper` (registrando anche `ColorSerializer` e `ColorDeserializer`) e crea la `ServerSocket` in ascolto sulla porta 9999.
        *   `start()` è il metodo principale che entra in un **ciclo infinito per accettare nuove connessioni client** e avvia un nuovo thread per gestire ciascun client tramite `gestisciClientConnesso()`.
        *   I metodi `ottieniPercorso()`, `scriviStato()` e `leggiStato()` gestiscono la **persistenza dello stato delle lavagne su file**. Ogni lavagna ha un file di stato separato.
        *   `operazioniCont()` gestisce un **contatore per generare ID univoci per le nuove lavagne**, salvato nel file `cont.txt`.
        *   `idLavagneAccesso()` legge dal file `accessiUtenti.txt` gli **ID delle lavagne a cui un determinato utente ha avuto accesso**.
        *   `aggiungiAccesso()` aggiunge una **nuova associazione tra un utente e un ID lavagna** nel file `accessiUtenti.txt`.
        *   `gestisciClientConnesso()` è il cuore della gestione di ciascun client. Legge le richieste dal client, le deserializza in oggetti `Logs` e le gestisce tramite uno `switch` sul nome del comando.
            *   `LAVAGNA_NEW`: Crea una nuova lavagna, genera un ID, salva lo stato iniziale e associa l'utente alla lavagna.
            *   `LAVAGNA_OLD`: Gestisce l'accesso a una lavagna esistente, recuperando lo stato dal file.
            *   `LAVAGNA_UPDATE`: Riceve e salva gli aggiornamenti dello stato della lavagna e li propaga agli altri client connessi alla stessa lavagna tramite `floodingUpdate()`.
            *   `LAVAGNA_CLOSE`: Rimuove il client dalla lista degli utenti attivi sulla lavagna.
            *   `USER_GETLAVAGNE`: Restituisce al client l'elenco delle lavagne a cui ha accesso.
        *   `aggiungiUtenteAttivoAllaLavagna()` aggiunge il `PrintWriter` di un client alla lista degli utenti attivi su una specifica lavagna.
        *   `cancellaLavagna()` rimuove una lavagna dalle liste attive (anche se non sembra esserci una logica esplicita per quando questo avvenga, a parte un errore di stato iniziale).
        *   `floodingUpdate()` **invia l'aggiornamento dello stato a tutti gli altri client connessi alla stessa lavagna**, implementando la collaborazione in tempo reale.
        *   `chiudiConnessione()` chiude la socket di un client.
        *   `isIdValido()` verifica se un dato ID lavagna esiste nel file `accessiUtenti.txt`.
        *   I metodi `msgErr()`, `msgCONFERMA()`, `msgERR_STATO()` e `msgERR_LAVAGNA_INTROVABILE()` inviano **messaggi di errore o di conferma** al client.
        *   Il metodo `main()` avvia l'istanza del server.
        *   Il commento finale riassume brevemente il funzionamento del server.

3.  **`whiteboard.whiteboard.azioni` (Azioni e Stati)**

    *   Questo package (non esaminato nel dettaglio nei sorgenti forniti, ma menzionato negli import) conterrebbe le classi che rappresentano le **azioni eseguite sulla whiteboard** (ad esempio, disegno di linee, aggiunta di figure, cancellazione) e la classe `Stato`, che probabilmente **mantiene l'elenco di tutte le azioni** che compongono lo stato attuale della lavagna.
    *   Si deduce che la classe `Logs` viene utilizzata per incapsulare i **comandi e i dati scambiati tra client e server** in formato JSON.
    *   La classe `LogsLavagne` sembra contenere l'**elenco degli ID delle lavagne** associate a un utente.
    *   Il sottocpackage `figure` conterrebbe le **classi per le diverse figure geometriche** disegnabili (Rettangolo, Cerchio, Triangolo, Parallelogramma, Rombo), ognuna con la propria logica di disegno.
    *   La classe `Stato` conterrebbe probabilmente un elenco di queste azioni/figure e un metodo per "disegnare" l'intero stato della lavagna su un `GraphicsContext`.

4.  **`whiteboard.whiteboard.serializer` (Serializzatori)**

    *   Questo package contiene classi personalizzate (`ColorSerializer` e `ColorDeserializer`) per gestire la **serializzazione e deserializzazione degli oggetti `javafx.scene.paint.Color`** in formato JSON utilizzando Jackson. Questo è necessario poiché Jackson non supporta nativamente la serializzazione di questa classe.

### **File di Dati:** {#file-di-dati}

Il server utilizza dei file per la persistenza dei dati:

*   `cont.txt`: Contiene un intero che viene incrementato per generare nuovi ID univoci per le lavagne.
*   `accessiUtenti.txt`: Mantiene l'associazione tra nomi utente e gli ID delle lavagne a cui hanno avuto accesso. Ogni riga ha il formato `nomeUtente;idLavagna1,idLavagna2,...`.
*   `whiteboard/src/main/resources/whiteboard/whiteboard/data/statiLavagne/`: Questa directory contiene un file per ogni lavagna, denominato con l'ID della lavagna seguito da `.txt`. Ogni file contiene la serializzazione JSON dello stato corrente della lavagna.

## **Flusso di Funzionamento Generale:** {#flusso-di-esecuzione-semplificato}

1.  **Avvio Client:** L'utente avvia l'applicazione client (`HelloApplication`), che carica la schermata di login (`client-view.fxml` e `ClientController`).
2.  **Login:** L'utente inserisce il proprio nome e fa clic su "Accedi". Viene creata un'istanza di `Client`, che tenta di connettersi al server.
3.  **Recupero Lavagne:** Il client invia il nome utente al server, che risponde con l'elenco degli ID delle lavagne a cui l'utente ha accesso. Questi vengono visualizzati nella schermata principale del client.
4.  **Creazione/Accesso Lavagna:**
    *   **Nuova Lavagna:** L'utente può creare una nuova lavagna fornendo un nome. Il client invia una richiesta al server, che crea un nuovo ID univoco, salva uno stato iniziale e restituisce l'ID al client.
    *   **Lavagna Esistente:** L'utente può selezionare una lavagna esistente dalla lista o accedere tramite un codice. Il client invia l'ID al server, che recupera lo stato corrente dal file e lo invia al client.
5.  **Interazione Whiteboard:** Una volta connesso a una lavagna, l'interfaccia utente passa a `lavagna-view.fxml` gestita da `LavagnaController`. L'utente può disegnare, cancellare, aggiungere figure, cambiare colori e spessori.
6.  **Aggiornamenti in Tempo Reale:** Ogni azione eseguita dall'utente sul canvas viene registrata nello `Stato` locale del client. Questo stato (o una rappresentazione dell'azione) viene serializzato e inviato al server. Il server riceve l'aggiornamento, lo persiste su file e lo propaga a tutti gli altri client connessi alla stessa lavagna.
7.  **Sincronizzazione:** Gli altri client ricevono l'aggiornamento dal server, deserializzano lo stato e lo applicano al loro canvas, garantendo la sincronizzazione in tempo reale.
8.  **Chiusura Lavagna/Applicazione:** Quando un utente chiude la lavagna o l'intera applicazione, il client notifica il server per interrompere gli aggiornamenti e chiude la connessione. Il server aggiorna le liste degli utenti attivi.

## Scaletta di esecuzione {#flusso-di-esecuzione-approfondito}
> ### **Scenario 1: Utente accede e crea una nuova lavagna** {#crea-lavagna}

1.  **Avvio dell'applicazione client:**
    *   Viene eseguito il metodo `main()` nella classe `HelloApplication`.
    *   Il metodo `start()` in `HelloApplication` carica l'interfaccia utente iniziale (`client-view.fxml`) tramite `FXMLLoader`.
    *   Viene creato un `ClientController` e chiamato il metodo `firstInitialize()`.
    *   **`ClientController.firstInitialize()`**: Questo metodo inizializza gli elementi dell'interfaccia utente per l'accesso, creando un `TextField` per l'username e un `Button` per l'accesso. Quando il pulsante viene cliccato, viene creato un nuovo oggetto `Client` e viene chiamata la funzione `postAccesso()`.

2.  **Autenticazione e caricamento della home page:**
    *   **`ClientController.postAccesso()`**: Questo metodo avvia la connessione al server chiamando `client.firstConfiguration()` e poi chiama `backToHome()` per mostrare la griglia delle lavagne.
    *   **`Client.firstConfiguration()`**:
        *   Stabilisce una connessione socket con il server all'indirizzo "localhost" sulla porta 9999.
        *   Crea flussi di input (`BufferedReader`) e output (`PrintWriter`) per comunicare con il server.
        *   Verifica la connessione ricevendo un messaggio di conferma dal server (`"CONNECTION_ACCEPTED"`).
        *   Invia il nome utente al server tramite un oggetto `Logs` con comando `"USER_GETLAVAGNE"`.
        *   Riceve dal server un oggetto `LogsLavagne` contenente gli ID delle lavagne a cui l'utente ha accesso.
        *   Restituisce l'oggetto `LogsLavagne`.
    *   **`ClientController.backToHome()`**: Chiama `creaGrigliaHome()` per visualizzare le lavagne disponibili.
    *   **`ClientController.creaGrigliaHome(Client client, LogsLavagne lgv)`**: Questo metodo crea dinamicamente bottoni per ogni lavagna a cui l'utente ha accesso, inclusi i pulsanti "+" per creare una nuova lavagna e "Aggiungi con codice" per accedere tramite ID.

3.  **Creazione di una nuova lavagna (azione utente):**
    *   L'utente clicca sul bottone "+".
    *   L'interfaccia viene aggiornata in `ClientController.creaGrigliaHome()` per mostrare un `TextField` per il nome della nuova lavagna e un bottone "Crea".
    *   L'utente inserisce un nome per la lavagna e clicca su "Crea".
    *   Viene chiamato il metodo `startClient()` in `ClientController` con il nome della lavagna e `null` come ID.
    *   **`ClientController.startClient(String nomeLavagna, String idLavagna)`**: Crea un nuovo thread ed esegue il metodo `client.run()`.
    *   **`Client.run(String nomeLavagna, String idLavagna)` (con `nomeLavagna` non nullo):**
        *   Invia al server un messaggio `Logs` con comando `"LAVAGNA_NEW"` e il nome della lavagna.
        *   Riceve dal server un messaggio `Logs` contenente il nuovo ID della lavagna.
        *   Crea un nuovo oggetto `Stato` per la lavagna.
        *   Invia lo stato iniziale della lavagna al server.
        *   Chiama `Platform.runLater()` per eseguire sulla thread UI il cambio di vista alla lavagna tramite `clientController.cambiaLavagnaView()`.
        *   Avvia un ciclo per ricevere aggiornamenti dalla lavagna dal server.

4.  **Gestione della richiesta di creazione lavagna sul server:**
    *   **`Server.gestisciClientConnesso(Socket clientSocket)`**: Questo metodo gestisce la comunicazione con il client in un thread separato. Riceve richieste dal client.
    *   Quando riceve un `Logs` con `nomeDelComando` uguale a `"LAVAGNA_NEW"`:
        *   Viene chiamato **`Server.gestLAVAGNA_NEW(String nomeLavagna, BufferedReader in, PrintWriter out)`**:
            *   Genera un nuovo ID per la lavagna combinando il nome e un contatore (`operazioniCont()`).
            *   Aggiunge il nuovo ID a `lavagneIdAttive` e una nuova lista vuota a `lavagneUtentiAttivi`.
            *   Crea uno stato iniziale vuoto per la lavagna.
            *   Scrive lo stato iniziale su file tramite **`Server.scriviStato(String idLavagna, Stato stato)`**.
                *   **`Server.ottieniPercorso(String idLavagna)`**: Genera il percorso del file per la lavagna specificata.
                *   Utilizza `ObjectMapper` per serializzare l'oggetto `Stato` in formato JSON.
                *   Scrive la stringa JSON nel file.
            *   Invia al client un messaggio `Logs` con comando `"LAVAGNA_NEW"` e il nuovo ID.
            *   Riceve dal client lo stato iniziale della lavagna.
            *   Aggiorna il file con lo stato iniziale ricevuto chiamando nuovamente `scriviStato()`.
            *   Aggiunge l'utente attivo alla lavagna tramite **`Server.aggiungiUtenteAttivoAllaLavagna(String lavagnaId, PrintWriter outUtente)`**.
                *   Ottiene l'indice della lavagna in `lavagneIdAttive`.
                *   Se necessario, aggiunge nuove liste vuote a `lavagneUtentiAttivi` per corrispondere all'indice.
                *   Aggiunge l'oggetto `PrintWriter` del client alla lista degli utenti attivi per quella lavagna.
            *   Restituisce il nuovo ID della lavagna.
        *   Viene chiamato `aggiungiAccesso(nomeUtente, idLavagnaAttuale)` per registrare l'accesso dell'utente alla nuova lavagna.
            *   **`Server.aggiungiAccesso(String nomeUtente, String idLavagna)`**: Legge il contenuto del file `accessiUtenti.txt`, verifica se l'utente esiste già, aggiunge l'ID della lavagna alla lista degli ID associati all'utente (se non già presente), e riscrive il file con le informazioni aggiornate.

5.  **Visualizzazione della nuova lavagna sul client:**
    *   **`ClientController.cambiaLavagnaView(String lavagnaNome, String idLavagna, Stato statoLavagna)`**:
        *   Carica l'interfaccia della lavagna (`lavagna-view.fxml`) tramite `FXMLLoader`.
        *   Crea una nuova `Scene` con l'interfaccia caricata.
        *   Ottiene il `LavagnaController` associato all'interfaccia.
        *   Imposta il nome e l'ID della lavagna nel `LavagnaController`.
        *   Imposta l'oggetto `Stato` nel `LavagnaController`, che potrebbe essere lo stato iniziale vuoto.
        *   Imposta il riferimento al `Client` nel `LavagnaController`.
        *   Imposta la `Scene` nello `Stage` e la mostra.
    *   **`LavagnaController.initialize()`**: Questo metodo viene chiamato all'avvio dell'interfaccia della lavagna. Inizializza il `GraphicsContext` per disegnare sulla `Canvas`, imposta i listener per gli eventi del mouse e dei controlli (penna, gomma, figure, colori, ecc.). Se lo stato iniziale non è vuoto, chiama `statoLavagna.disegnaStato()` per visualizzare il contenuto esistente (in questo caso è vuoto).

> ### **Scenario 2: Utente accede a una lavagna esistente** {#accedi-a-lavagna}

1.  **Passi 1 e 2:** Come nel caso della creazione, l'utente avvia l'applicazione, si autentica e visualizza la home page con la lista delle lavagne disponibili.

2.  **Selezione di una lavagna esistente (azione utente):**
    *   L'utente clicca su uno dei bottoni rappresentanti una lavagna esistente nella `creaGrigliaHome()`.
    *   Viene chiamato il metodo `startClient()` in `ClientController` con `null` come nome della lavagna e l'ID della lavagna selezionata.
    *   **`ClientController.startClient(String nomeLavagna, String idLavagna)` (con `nomeLavagna` nullo):**
        *   Crea un nuovo thread ed esegue il metodo `client.run()`.
        *   **`Client.run(String nomeLavagna, String idLavagna)` (con `nomeLavagna` nullo):**
            *   Invia al server un messaggio `Logs` con comando `"LAVAGNA_OLD"` e l'ID della lavagna.
            *   Riceve dal server un messaggio `Logs` contenente il nome della lavagna (senza l'identificativo numerico).
            *   Riceve dal server l'oggetto `Stato` corrente della lavagna.
            *   Imposta il client nello `Stato` ricevuto.
            *   Salva l'ID della lavagna.
            *   Chiama `Platform.runLater()` per eseguire sulla thread UI il cambio di vista alla lavagna tramite `clientController.cambiaLavagnaView()`.
            *   Avvia un ciclo per ricevere aggiornamenti dalla lavagna dal server.

3.  **Gestione della richiesta di accesso alla lavagna sul server:**
    *   **`Server.gestisciClientConnesso(Socket clientSocket)`**: Come nel caso precedente, gestisce la comunicazione con il client.
    *   Quando riceve un `Logs` con `nomeDelComando` uguale a `"LAVAGNA_OLD"`:
        *   Viene chiamato **`Server.gestLAVAGNA_OLD(String lavagnaId, BufferedReader in, PrintWriter out)`**:
            *   Verifica se l'ID della lavagna è valido tramite **`Server.isIdValido(String idDaCercare)`**.
                *   **`Server.isIdValido(String idDaCercare)`**: Legge il file `accessiUtenti.txt` e verifica se l'ID fornito è presente nella lista degli ID associati a qualche utente.
            *   Se l'ID è valido e la lavagna non è ancora considerata attiva sul server (`lavagneIdAttive` non contiene l'ID`):
                *   Aggiunge l'ID a `lavagneIdAttive` e una nuova lista vuota a `lavagneUtentiAttivi`.
            *   Aggiunge l'utente attivo alla lavagna chiamando **`Server.aggiungiUtenteAttivoAllaLavagna(String lavagnaId, PrintWriter outUtente)`**.
            *   Invia al client un messaggio `Logs` con comando `"LAVAGNA_OLD"` e il nome della lavagna (senza l'identificativo numerico).
            *   Legge lo stato corrente della lavagna dal file tramite **`Server.leggiStato(String idLavagna)`**.
                *   **`Server.ottieniPercorso(String idLavagna)`**: Genera il percorso del file.
                *   Verifica se il file esiste.
                *   Utilizza `ObjectMapper` per deserializzare il contenuto JSON del file in un oggetto `Stato`.
                *   Restituisce l'oggetto `Stato` letto o un nuovo `Stato` vuoto se il file non esiste o si verifica un errore.
            *   Invia lo stato corrente della lavagna al client.
            *   Restituisce `true` per indicare che l'accesso ha avuto successo.
        *   Se `gestLAVAGNA_OLD` restituisce `true`, viene chiamato `aggiungiAccesso(nomeUtente, idLavagnaAttuale)` per registrare l'accesso dell'utente alla lavagna.

4.  **Visualizzazione della lavagna esistente sul client:**
    *   **`ClientController.cambiaLavagnaView(String lavagnaNome, String idLavagna, Stato statoLavagna)`**: Come nel caso della creazione, carica l'interfaccia della lavagna e imposta i dati. In questo caso, l'oggetto `Stato` passato conterrà lo stato corrente della lavagna.
    *   **`LavagnaController.initialize()`**: Viene chiamato e, grazie alla chiamata a `setStatoLavagna()`, il metodo `statoLavagna.disegnaStato()` verrà eseguito, disegnando sulla `Canvas` gli elementi grafici precedentemente salvati.

Questa scaletta fornisce una panoramica dettagliata dei metodi coinvolti nel processo di creazione o accesso a una lavagna da parte di un utente, evidenziando i passaggi chiave sia sul lato client che sul lato server.
