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
//Locali
import whiteboard.whiteboard.azioni.*;
import whiteboard.whiteboard.azioni.figure.*;

import java.util.ArrayList;

public class LavagnaController {
    // Riferimenti agli elementi grafici (Quelli di lavagna-view.fxml)
    @FXML
    private Canvas lavagna;
    @FXML
    private AnchorPane lineaBox, gommaBox, figureBox;
    @FXML
    private ImageView pennaButton, gommaButton, figureButton, undoButton;
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

    //Stato Lavagna
    ArrayList<Elementi> salvataggiLavagna =new ArrayList<Elementi>();

    // Variabili
    private GraphicsContext contestoGrafico; // Contesto grafico
    private boolean isLineaActive = false; // Modalità penna disattiva all'inizio
    private boolean isGommaActive = false;     // Modalità gomma disattivata all'inizio
    private boolean isFigureActive = false;    // Modalità inserimento figure

    // Variabili per le figure
    private String figuraSelezionata = "Rettangolo";
    private int dim1S = 100;
    private int dim2S = 100;

    @FXML
    public void initialize() {
        // Inizializza il contesto grafico e le variabili
        contestoGrafico = lavagna.getGraphicsContext2D();
        sbiancaLavagna();
        inizializzaControlli();

        // Listener per azioni
        setListenerBox();
        pennaButton.setOnMouseClicked(e -> attivaScrittura());
        gommaButton.setOnMouseClicked(e -> attivaCancellazione());
        figureButton.setOnMouseClicked(e -> attivaFigure());
        undoButton.setOnMouseClicked(e -> undo());
        lavagna.setOnMousePressed(this::clickMouseLavagna);
        lavagna.setOnMouseDragged(this::trascinoMouseLavagna);
        lavagna.setOnMouseReleased(this::rilascioMouseLavagna);
    }

    private void sbiancaLavagna() {
        contestoGrafico.setFill(Color.WHITE);
        contestoGrafico.fillRect(0, 0, lavagna.getWidth(), lavagna.getHeight());
    }

    private void inizializzaControlli() {
        colorPicker.setValue(Color.BLACK);
        grandezzaLinea.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        grandezzaGomma.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        dim1.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 500, 100));
        dim2.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 500, 100));
        grandezzaBordo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 10));
    }

    @FXML
    private void setListenerBox() {
        // textBox
        colorPicker.setOnAction(e -> setColoreTratto(colorPicker.getValue()));
        grandezzaLinea.valueProperty().addListener((obs, oldValue, newValue) -> setSpessoreLinea(newValue));
        // gommaBox
        grandezzaGomma.valueProperty().addListener((obs, oldValue, newValue) -> setSpessoreGomma(newValue));
        cBott.setOnMouseMoved(event -> cBott.setStyle("-fx-background-color: #1A80E4; -fx-border-color: #E9EEF4; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-width: 1.5; -fx-font-weight: 700;"));
        cBott.setOnMouseExited(event -> cBott.setStyle("-fx-background-color: #1A80E4; -fx-border-color: #1A80E4; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-width: 1.5; -fx-font-weight: 700;"));
        // figureBox
        choiceFigure.getItems().addAll("Rettangolo", "Cerchio", "Triangolo", "Parallelogramma", "Rombo");
        choiceFigure.setValue("Rettangolo"); // Predefinito
        choiceFigure.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            figuraSelezionata = newValue;
            dim2.setDisable("Cerchio".equals(newValue) || "Triangolo".equals(newValue));
        });
        dim1.valueProperty().addListener((obs, oldValue, newValue) -> dim1S = newValue);
        dim2.valueProperty().addListener((obs, oldValue, newValue) -> dim2S = newValue);
        colorPickerBordo.setOnAction(e -> setColoreBordo(colorPickerBordo.getValue()));
        colorPickerRiempimento.setOnAction(e -> setColoreRiempimento(colorPickerRiempimento.getValue()));
        grandezzaBordo.valueProperty().addListener((obs, oldValue, newValue) -> setSpessoreBordo(newValue));
        isTrasparente.selectedProperty().addListener((observable, oldValue, newValue) -> colorPickerRiempimento.setDisable(newValue));
    }

    /* Metodi per impostare le proprietà del contesto grafico */
    //Linee
    private void setColoreTratto(Color colore) { contestoGrafico.setStroke(colore); }
    private void setSpessoreLinea(int spessore) { contestoGrafico.setLineWidth(spessore); }
    private Color getColoreTratto(){ return colorPicker.getValue(); }
    private int getSpessoreLinea(){ return grandezzaLinea.getValue(); }

    //Gomma
    private void setSpessoreGomma(int spessore) { contestoGrafico.setLineWidth(spessore); }
    private int getSpessoreGomma(){ return grandezzaGomma.getValue(); }

    //Figure
    private void setColoreBordo(Color colore) { contestoGrafico.setStroke(colore); }
    private void setColoreRiempimento(Color colore) { contestoGrafico.setFill(colore); }
    private void setSpessoreBordo(int spessore) { contestoGrafico.setLineWidth(spessore); }
    private Color getColoreBordo(){ return colorPickerBordo.getValue(); }
    private Color getColoreRiempimento(){ return colorPickerRiempimento.getValue(); }
    private int getSpessoreRiempimento(){ return grandezzaBordo.getValue(); }


    /* MODALITA' GRAFICHE (Ovvero settaggi grafici basati sulle diverse azioni) */
    private void attivaModalita(ImageView bottoneAttivato, AnchorPane pannelloDaMostrare) {
        chiudiModalitaPrecedenti();
        resetStiliBottoni();
        evidenziaBottone(bottoneAttivato);
        entrataPannello(pannelloDaMostrare); //Cioè l'anchor box associata alla modalità
    }
    private void chiudiModalitaPrecedenti() {
        if (isLineaActive) {
            uscitaPannello(lineaBox);
        } else if (isGommaActive) {
            uscitaPannello(gommaBox);
        } else if (isFigureActive) {
            uscitaPannello(figureBox);
        }
    }

    private void attivaScrittura() {
        attivaModalita(pennaButton, lineaBox);
        isLineaActive = true;
        isGommaActive = false;
        isFigureActive = false;
        setColoreTratto(colorPicker.getValue());
        setSpessoreLinea(grandezzaLinea.getValue());
    }
    private void attivaCancellazione() {
        attivaModalita(gommaButton, gommaBox);
        isLineaActive = false;
        isGommaActive = true;
        isFigureActive = false;
        setSpessoreGomma(grandezzaGomma.getValue());
    }
    private void attivaFigure() {
        attivaModalita(figureButton, figureBox);
        isLineaActive = false;
        isGommaActive = false;
        isFigureActive = true;
        figuraSelezionata = choiceFigure.getValue();
        dim1S = dim1.getValue();
        dim2S = dim2.getValue();
    }

    //Azioni Grafiche ed animazioni
    private void resetStiliBottoni() {
        pennaButton.setStyle("-fx-background-color: #E2EAE3;");
        gommaButton.setStyle("-fx-background-color: #E2EAE3;");
        figureButton.setStyle("-fx-background-color: #E2EAE3;");
    }
    private void evidenziaBottone(ImageView bottone) { bottone.setStyle("-fx-background-color: E9EEF4; -fx-border-color: E9EEF4; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-border-width: 2;"); }

    private void entrataPannello(AnchorPane pannello) {
        pannello.setVisible(true);
        pannello.setDisable(false);
        entrataAnchor(pannello, -300, 0);
    }
    private void uscitaPannello(AnchorPane pannello) {
        entrataAnchor(pannello, 0, -300);
        pannello.setVisible(false);
        pannello.setDisable(true);
    }

    /* Gestione del canvas ovvero Disegno effettivo*/
    private void clickMouseLavagna(MouseEvent mouse) {
        if (isLineaActive) {
            iniziaLinea(mouse.getX(), mouse.getY());
        } else if (isGommaActive) {
            cancella(mouse.getX(), mouse.getY());
        } else if (isFigureActive) {
            disegnaFigura(mouse.getX(), mouse.getY());
        }
    }

    private void iniziaLinea(double x, double y) {
        contestoGrafico.beginPath();
        contestoGrafico.moveTo(x, y); //Imposto il punto di partenza del path non muovo nulla
        contestoGrafico.stroke(); //NB x Diego (Che non si ricorda una minchia) : Questo fa solo il contorno non il riempimento

        salvataggiLavagna.add(new Linea(x,y,getColoreTratto(),getSpessoreLinea()));
    }
    private void disegnaFigura(double x, double y) {
        impostaStiliFigura(); // Impostiamo i colori e i bordi
        Figura figura = null;
        Color colore;

        if (isTrasparente.isSelected()) colore=Color.TRANSPARENT;
        else colore=getColoreRiempimento();

        if ("Triangolo".equals(figuraSelezionata)) {
            figura = new Triangolo(x, y, dim1S, dim2S, getColoreBordo(), colore, getSpessoreRiempimento());
        } else if ("Parallelogramma".equals(figuraSelezionata)) {
            figura = new Parallelogramma(x, y, dim1S, dim2S, getColoreBordo(), colore, getSpessoreRiempimento());
        } else if ("Rombo".equals(figuraSelezionata)) {
            figura = new Rombo(x, y, dim1S, dim2S, getColoreBordo(), colore, getSpessoreRiempimento());
        } else if ("Rettangolo".equals(figuraSelezionata)) {
            figura = new Rettangolo(x, y, dim1S, dim2S, getColoreBordo(), colore, getSpessoreRiempimento());
        } else if ("Cerchio".equals(figuraSelezionata)) {
            figura = new Cerchio(x, y, dim1S, getColoreBordo(), colore, getSpessoreRiempimento());
        }

        if (figura != null) { //Si potrebbe evitare ma meglio così no?!
            figura.disegna(contestoGrafico,lavagna);
            salvataggiLavagna.add(figura);
        }
    }
    private void impostaStiliFigura() {
        setColoreBordo(colorPickerBordo.getValue());
        if (isTrasparente.isSelected()) setColoreRiempimento(Color.TRANSPARENT);
        else setColoreRiempimento(colorPickerRiempimento.getValue());
        setSpessoreBordo(grandezzaBordo.getValue());
    }

    // Evento di trascinamento del mouse sul canvas
    @FXML
    private void trascinoMouseLavagna(MouseEvent mouse) {
        if (isLineaActive) {
            continuaLinea(mouse.getX(), mouse.getY());
        } else if (isGommaActive) {
            cancella(mouse.getX(), mouse.getY());
        } else if (isFigureActive) {
            //anteprimaDisegnoFigura(mouse.getX(), mouse.getY());
        }
    }

    private void continuaLinea(double x, double y) {
        contestoGrafico.lineTo(x, y);
        contestoGrafico.stroke();

        Linea linea=(Linea) salvataggiLavagna.getLast();
        linea.continuaA(x,y);
    }

    // Evento al rilascio del mouse sul canvas
    @FXML
    private void rilascioMouseLavagna(MouseEvent mouse) {
        if (isLineaActive) {
            terminaDisegno();
        } else if (isFigureActive) {
            //finalizzaDisegnoFigura(mouse.getX(), mouse.getY());
        }
    }

    private void terminaDisegno() {
        contestoGrafico.closePath();
    }

    // Funzione per cancellare una parte del disegno
    @FXML
    private void cancella(double x, double y) {
        contestoGrafico.setFill(Color.WHITE);
        contestoGrafico.fillRect(x - (double) grandezzaGomma.getValue() / 2,
                y - (double) grandezzaGomma.getValue() / 2,
                grandezzaGomma.getValue(), grandezzaGomma.getValue());
        salvataggiLavagna.add(new Gomma("GOMMA",x,y,(double) grandezzaGomma.getValue()));
    }

    @FXML
    protected void cancellaTutto() {
        sbiancaLavagna();
        salvataggiLavagna.add(new Gomma("GOMMA_TOTALE",0,0,0));
    }

    //Undo
    @FXML
    private void undo(){
        sbiancaLavagna(); //Pulisci la lavagna
        salvataggiLavagna.removeLast();
        for (Elementi elemento: salvataggiLavagna) elemento.disegna(contestoGrafico,lavagna);
    }

    // UTILITÀ
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