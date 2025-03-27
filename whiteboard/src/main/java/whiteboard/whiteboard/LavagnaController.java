package whiteboard.whiteboard;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.shape.*;

import java.util.ArrayList;

public class LavagnaController {
    // Riferimenti agli elementi grafici (Quelli di lavagna-view.fxml)
    @FXML
    private Canvas lavagna;
    @FXML
    private AnchorPane textBox,gommaBox, figureBox;
    @FXML
    private ImageView pennaButton,gommaButton,testoButton,figureButton;
    @FXML
    private ColorPicker colorPicker, colorPickerBordo, colorPickerRiempimento;
    @FXML
    private Spinner<Integer> grandezzaLinea, grandezzaGomma, dim1, dim2, grandezzaBordo;
    @FXML
    private ChoiceBox<String> choiceFigure;
    @FXML
    private CheckBox isTrasparente;
    @FXML
    private Button cBott;

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
    private boolean isFigureActive=false; //Modalità inserimento figure

    //X figure
    private String figuraSelezionata="Rettangolo";
    private int dim1S =100;
    private int dim2S =100;
    private double inizialeXF, inizialeYF; // Coordinate iniziali per il disegno
    private ArrayList<Shape> figureInserite = new ArrayList<>(); // Lista delle forme disegnate

    @FXML
    public void initialize() {
        // Inizializza il contesto grafico e le variabili
        contestoGrafico = lavagna.getGraphicsContext2D(); //Restituisce un'istanza di GraphicsContext, che è l'oggetto responsabile del disegno su quel Canvas
        contestoGrafico.setFill(Color.WHITE); //Imposta il colore di riempimento a bianco
        contestoGrafico.fillRect(0, 0, lavagna.getWidth(), lavagna.getHeight()); // Riempi il canvas di bianco, partendo dall'angolo in alto 0,0

        colorPicker.setValue(Color.BLACK);
        grandezzaLinea.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2)); // Valore tra 1 e 20, predefinito 2
        grandezzaGomma.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        dim1.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 500, 100));
        dim2.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 500, 100));
        grandezzaBordo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 10));

        //Listener per azioni
        setListenerBox();
        pennaButton.setOnMouseClicked(e -> attivaScrittura());
        gommaButton.setOnMouseClicked(e -> attivaCancellazione());
        testoButton.setOnMouseClicked(e -> attivaTesto());
        figureButton.setOnMouseClicked(e-> attivaFigure());
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
        cBott.setOnMouseMoved(event -> cBott.setStyle("-fx-background-color: #1A80E4; -fx-border-color: #E9EEF4; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-width: 1.5; -fx-font-weight: 700;"));
        cBott.setOnMouseExited(event -> cBott.setStyle("-fx-background-color: #1A80E4; -fx-border-color: #1A80E4; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-width: 1.5; -fx-font-weight: 700;"));
        // figureBox
        choiceFigure.getItems().addAll("Rettangolo", "Cerchio");
        choiceFigure.setValue("Rettangolo"); //Predefinito
        choiceFigure.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> { figuraSelezionata = newValue; });// Aggiorna la forma selezionata
        dim1.valueProperty().addListener((obs, oldValue, newValue) -> { dim1S =newValue; });
        dim2.valueProperty().addListener((obs, oldValue, newValue) -> { dim2S =newValue; });
        colorPickerBordo.setOnAction(e -> contestoGrafico.setStroke(colorPickerBordo.getValue()));
        colorPickerRiempimento.setOnAction(e -> contestoGrafico.setStroke(colorPickerRiempimento.getValue()));
        grandezzaBordo.valueProperty().addListener((obs, oldValue, newValue) -> contestoGrafico.setLineWidth(newValue));
        //per modifiche in strutture
        choiceFigure.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if ("Cerchio".equals(newValue)) {
                dim2.setDisable(true);
            }else{
                dim2.setDisable(false);
            }
        });
        isTrasparente.selectedProperty().addListener((observable, oldValue, newValue) -> { //Se checko il trasparente disabilito la scelta dello sfondo
            colorPickerRiempimento.setDisable(newValue);
        });
    }

    //MODALITA'
    private void attivaScrittura() {
        chiudiModalitaPrecedenti();
        //Parametri iniziali
        isDisegnoActive = true;
        isGommaActive = false;
        isTestoActive = false;
        isFigureActive=false;

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
        isFigureActive=false;

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
        isFigureActive=false;
    }

    //attiva figure
    private void attivaFigure() {
        chiudiModalitaPrecedenti();
        //Parametri iniziali
        isDisegnoActive = false;
        isGommaActive = false;
        isTestoActive = false;
        isFigureActive=true;

        figureButton.setStyle("-fx-background-color: E9EEF4; -fx-border-color: E9EEF4; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-border-width: 2;");//coloro immaginina
        //Box associata in questo caso figureBox
        figureBox.setVisible(true);
        figureBox.setDisable(false);
        entrataAnchor(figureBox,-300,0);
        //Elementi figure
        figuraSelezionata=choiceFigure.getValue();
        dim1S =dim1.getValue();
        dim2S=dim2.getValue();
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
        } else if (isFigureActive) {
            entrataAnchor(figureBox,0,-300);
            figureBox.setVisible(false);
            figureBox.setDisable(true);
            figureButton.setStyle("-fx-background-color: #E2EAE3;");
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
        } else if (isFigureActive) {
            inizialeXF = mouse.getX() - dim1S / 2;;
            inizialeYF = mouse.getY() - dim2S / 2;;

            // In base alla forma selezionata, disegniamo la figura
            if ("Rettangolo".equals(figuraSelezionata)) {
                contestoGrafico.setStroke(colorPickerBordo.getValue());  // Imposta il colore del bordo della figura
                if (isTrasparente.isSelected()) contestoGrafico.setFill(Color.TRANSPARENT); // Imposta il colore di riempimento (o trasparente)
                else contestoGrafico.setFill(colorPickerRiempimento.getValue());

                contestoGrafico.fillRect(inizialeXF, inizialeYF, dim1S, dim2S); //Riempie
                contestoGrafico.strokeRect(inizialeXF, inizialeYF, dim1S, dim2S);  // Disegna il rettangolo
            } else if ("Cerchio".equals(figuraSelezionata)) {
                contestoGrafico.setStroke(colorPickerBordo.getValue());
                if (isTrasparente.isSelected()) contestoGrafico.setFill(Color.TRANSPARENT);
                else contestoGrafico.setFill(colorPickerRiempimento.getValue());

                contestoGrafico.fillOval(inizialeXF, inizialeYF, dim1S, dim2S); //Riempie
                contestoGrafico.strokeOval(inizialeXF, inizialeYF, dim1S, dim2S);  // Disegna il cerchio
            }
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
        } else if (isFigureActive) { // Ripeti il disegno delle forme
            double correnteXF = mouse.getX()- dim1S / 2;
            double correnteYF = mouse.getY()- dim2S / 2;
            if ("Rettangolo".equals(figuraSelezionata)) {
                contestoGrafico.setStroke(colorPickerBordo.getValue());
                if (isTrasparente.isSelected()) contestoGrafico.setFill(Color.TRANSPARENT);
                else contestoGrafico.setFill(colorPickerRiempimento.getValue());

                contestoGrafico.fillRect(correnteXF, correnteYF, dim1S, dim2S);
                contestoGrafico.strokeRect(correnteXF, correnteYF, dim1S, dim2S);
            } else if ("Cerchio".equals(figuraSelezionata)) {
                contestoGrafico.setStroke(colorPickerBordo.getValue());
                if (isTrasparente.isSelected()) contestoGrafico.setFill(Color.TRANSPARENT);
                else contestoGrafico.setFill(colorPickerRiempimento.getValue());

                contestoGrafico.fillOval(correnteXF, correnteYF, dim1S, dim2S);
                contestoGrafico.strokeOval(correnteXF, correnteYF, dim1S, dim2S);
            }
        }
    }

    // Evento quando il mouse viene rilasciato sul canvas
    @FXML
    private void rilascioMouseLavagna(MouseEvent mouse) {
        if (isDisegnoActive) {
            contestoGrafico.closePath(); // Chiude il percorso di disegno
        }else if (isFigureActive) {
            double correnteX = mouse.getX()- dim1S / 2;
            double correnteY = mouse.getY()- dim2S / 2;
            // Definisci le dimensioni
            double width = dim1S;
            double height=dim1S;
            if (figuraSelezionata.equals("Cerchio")) height = dim2S;

            // Crea la forma in base alla selezione dell'utente
            Shape figura = null;
            if (figuraSelezionata.equals("Rettangolo")) {
                figura = new Rectangle(correnteX, correnteY, dim1S, dim2S);
            } else if (figuraSelezionata.equals("Cerchio")) {
                figura = new Circle(correnteX, correnteY, dim1S);
            }
            if (figura != null) {
                figureInserite.add(figura); // Aggiungi la forma alla lista delle forme
                // Disegna la forma
                if (figura instanceof Rectangle) { //Utile per il trascinamento conclusivo
                    contestoGrafico.fillRect(inizialeXF, inizialeYF, dim1S, dim2S);
                    contestoGrafico.strokeRect(correnteX, correnteY, width, height);
                } else if (figura instanceof Circle) {
                    contestoGrafico.fillOval(inizialeXF, inizialeYF, dim1S, dim2S);
                    contestoGrafico.strokeOval(correnteX, correnteY, width, height);
                }
            }
        }
    }

    // Funzione per cancellare una parte del disegno
    @FXML
    private void cancella(double x, double y) {
        contestoGrafico.setFill(Color.WHITE);
        contestoGrafico.fillRect(x - (double) grandezzaGomma.getValue() / 2, y - (double) grandezzaGomma.getValue() / 2, grandezzaGomma.getValue(), grandezzaGomma.getValue());
        //Perchè ho spostato di m - (n/2), perchè così il punto cliccato sarà il centro del "rettangolo di cancellazione"
    }

    @FXML
    protected void cancellaTutto(){
        contestoGrafico.setFill(Color.WHITE);
        contestoGrafico.fillRect(0, 0, lavagna.getWidth(), lavagna.getHeight()); // Riempi il canvas di bianco, partendo dall'angolo in alto 0,0
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
