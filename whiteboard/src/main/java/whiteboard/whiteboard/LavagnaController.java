package whiteboard.whiteboard;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class LavagnaController {
    // Riferimenti agli elementi grafici (Quelli di lavagna-view.fxml)
    @FXML
    private Canvas lavagna;
    @FXML
    private AnchorPane textBox,gommaBox;
    @FXML
    private ImageView pennaButton;
    @FXML
    private ImageView gommaButton;
    @FXML
    private ImageView testoButton;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Spinner<Integer> grandezzaLinea, grandezzaGomma;

    //Variabili
    private GraphicsContext contestoGrafico; // Contesto grafico
    /* PIERO ANGELA CODING'S MOMENT:
    Un contesto grafico (GraphicsContext) in JavaFX è un oggetto che fornisce metodi
    per disegnare su un Canvas. È come un "pennello virtuale" che permette di
    disegnare linee, forme, immagini e testo sulla superficie del Canvas, quindi della lavagna.
    */

    private boolean isDisegnoActive = false; // Modalità penna attiva all'inizio, poi con il metodo
    private boolean isTestoActive = false; // Modalità testo disattivata all'inizio
    private boolean isGommaActive = false; // Modalità gomma disattivata all'inizio

    @FXML
    public void initialize() {
        // Inizializza il contesto grafico e le variabili
        contestoGrafico = lavagna.getGraphicsContext2D(); //Restituisce un'istanza di GraphicsContext, che è l'oggetto responsabile del disegno su quel Canvas
        contestoGrafico.setFill(Color.WHITE); //Imposta il colore di riempimento a bianco
        contestoGrafico.fillRect(0, 0, lavagna.getWidth(), lavagna.getHeight()); // Riempi il canvas di bianco, partendo dall'angolo in alto 0,0

        colorPicker.setValue(Color.BLACK);
        grandezzaLinea.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2)); // Valore tra 1 e 20, predefinito 2
        grandezzaGomma.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));

        //Listener per azioni
        setListenerBox();
        pennaButton.setOnMouseClicked(e -> attivaScrittura());
        gommaButton.setOnMouseClicked(e -> attivaCancellazione());
        testoButton.setOnMouseClicked(e -> attivaTesto());
        lavagna.setOnMousePressed(this::clickMouseLavagna);
        lavagna.setOnMouseDragged(this::trascinoMouseLavagna);
        lavagna.setOnMouseReleased(this::rilascioMouseLavagna);

        //Attivo scrittura all'inizio
        attivaScrittura();
    }
    @FXML
    private void setListenerBox(){
        //textBox
        colorPicker.setOnAction(e -> contestoGrafico.setStroke(colorPicker.getValue()));// Imposta il colore della penna
        grandezzaLinea.valueProperty().addListener((obs, oldValue, newValue) -> contestoGrafico.setLineWidth(newValue)); // Imposta la larghezza della linea
        //gommaBox
        grandezzaGomma.valueProperty().addListener((obs, oldValue, newValue) -> contestoGrafico.setLineWidth(newValue)); // Imposta la larghezza della linea
    }

    //MODALITA'
    private void attivaScrittura() {
        chiudiModalitaPrecedenti();
        //Parametri iniziali
        isDisegnoActive = true;
        isGommaActive = false;
        isTestoActive = false;

        pennaButton.setStyle("-fx-background-color: E9EEF4; -fx-border-color: E9EEF4; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-border-width: 2;");//coloro immaginina
        //Box associata in questo caso textBox
        textBox.setVisible(true);
        textBox.setDisable(false);
        entrataAnchor(textBox,-300,0);
        //Elementi penna
        contestoGrafico.setStroke(colorPicker.getValue());
        contestoGrafico.setLineWidth(grandezzaLinea.getValue());
    }

    private void attivaCancellazione() {
        chiudiModalitaPrecedenti();
        //Parametri iniziali
        isDisegnoActive = false;
        isGommaActive = true;
        isTestoActive = false;

        gommaButton.setStyle("-fx-background-color: E9EEF4; -fx-border-color: E9EEF4; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-border-width: 2;");//coloro immaginina
        //Box associata in questo caso gommaBox
        gommaBox.setVisible(true);
        gommaBox.setDisable(false);
        entrataAnchor(gommaBox,-300,0);
        //Elementi gomma
        contestoGrafico.setLineWidth(grandezzaGomma.getValue()); // Imposta la dimensione della gomma
    }

    // Attiva la modalità testo
    private void attivaTesto() {
        chiudiModalitaPrecedenti();
        //Parametri iniziali
        isDisegnoActive = false;
        isGommaActive = false;
        isTestoActive = true;
    }

    //CHIUSURA MODALITA'
    private void chiudiModalitaPrecedenti(){
        if (isDisegnoActive){
            entrataAnchor(textBox,0,-300);
            textBox.setVisible(false);
            textBox.setDisable(true);
            pennaButton.setStyle("-fx-background-color: #E2EAE3;");
        } else if (isGommaActive){
            entrataAnchor(gommaBox,0,-300);
            gommaBox.setVisible(false);
            gommaBox.setDisable(true);
            gommaButton.setStyle("-fx-background-color: #E2EAE3;");
        }
    }

    //EVENTI
    // Evento quando il mouse viene premuto sul canvas
    private void clickMouseLavagna(MouseEvent mouse) {
        if (isDisegnoActive) {
            contestoGrafico.beginPath(); // Inizia un nuovo percorso di disegno
            contestoGrafico.moveTo(mouse.getX(), mouse.getY()); // Sposta il punto iniziale alla posizione del mouse
            contestoGrafico.stroke(); // Disegna il primo punto (Solo il contorno) ----> https://docs.oracle.com/javase/8/javafx/api/javafx/scene/canvas/GraphicsContext.html#stroke--
        } else if (isGommaActive) {
            cancella(mouse.getX(), mouse.getY()); // Cancella alla posizione del mouse
        }
    }

    // Evento quando il mouse viene trascinato sul canvas
    @FXML
    private void trascinoMouseLavagna(MouseEvent mouse) {
        if (isDisegnoActive) {
            contestoGrafico.lineTo(mouse.getX(), mouse.getY()); // Disegna una linea fino alla nuova posizione
            contestoGrafico.stroke(); // Applica il tratto (Solo il contorno)
        } else if (isGommaActive) {
            cancella(mouse.getX(), mouse.getY()); // Cancella alla posizione del mouse
        }
    }

    // Evento quando il mouse viene rilasciato sul canvas
    @FXML
    private void rilascioMouseLavagna(MouseEvent mouse) {
        if (isDisegnoActive) {
            contestoGrafico.closePath(); // Chiude il percorso di disegno
        }
    }

    // Funzione per cancellare una parte del disegno
    @FXML
    private void cancella(double x, double y) {
        contestoGrafico.setFill(Color.WHITE);
        contestoGrafico.fillRect(x - (double) grandezzaGomma.getValue() / 2, y - (double) grandezzaGomma.getValue() / 2, grandezzaGomma.getValue(), grandezzaGomma.getValue());
        //Perchè ho spostato di m - (n/2), perchè così il punto cliccato sarà il centro del "rettangolo di cancellazione"
    }

    //UTILITA'
    @FXML
    private static void entrataAnchor(AnchorPane anchorPane, int xIn, int xFin) {
        anchorPane.setTranslateX(xIn);

        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(1), anchorPane);
        translateTransition.setFromX(xIn);
        translateTransition.setToX(xFin);
        translateTransition.setCycleCount(1);
        translateTransition.setAutoReverse(false);
        translateTransition.play();
    }
}


/*
Step per il disegno di un tratto:
    1)  Mouse premuto: Inizia un nuovo percorso di disegno, posizionando il punto iniziale e disegnando il primo tratto.
    2)  Mouse trascinato: Estende la linea seguendo la posizione del mouse, rendendo il disegno visibile.
    3)  Mouse rilasciato: Chiude il percorso di disegno, terminando la linea.
    Il metodo stroke() è fondamentale per applicare il disegno e renderlo visibile.
*/
