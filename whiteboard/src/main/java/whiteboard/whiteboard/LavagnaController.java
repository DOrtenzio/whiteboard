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
    private AnchorPane textBox, gommaBox, figureBox;
    @FXML
    private ImageView pennaButton, gommaButton, testoButton, figureButton;
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

    // Variabili
    private GraphicsContext contestoGrafico; // Contesto grafico
    private boolean isDisegnoActive = false; // Modalità penna attiva all'inizio
    private boolean isTestoActive = false;   // Modalità testo disattivata all'inizio
    private boolean isGommaActive = false;     // Modalità gomma disattivata all'inizio
    private boolean isFigureActive = false;    // Modalità inserimento figure

    // Variabili per le figure
    private String figuraSelezionata = "Rettangolo";
    private int dim1S = 100;
    private int dim2S = 100;
    private double inizialeXF, inizialeYF; // Coordinate iniziali per il disegno
    private ArrayList<Shape> figureInserite = new ArrayList<>(); // Lista delle forme disegnate

    @FXML
    public void initialize() {
        // Inizializza il contesto grafico e le variabili
        contestoGrafico = lavagna.getGraphicsContext2D();
        contestoGrafico.setFill(Color.WHITE);
        contestoGrafico.fillRect(0, 0, lavagna.getWidth(), lavagna.getHeight());

        colorPicker.setValue(Color.BLACK);
        grandezzaLinea.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        grandezzaGomma.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        dim1.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 500, 100));
        dim2.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 500, 100));
        grandezzaBordo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 10));

        // Listener per azioni
        setListenerBox();
        pennaButton.setOnMouseClicked(e -> attivaScrittura());
        gommaButton.setOnMouseClicked(e -> attivaCancellazione());
        testoButton.setOnMouseClicked(e -> attivaTesto());
        figureButton.setOnMouseClicked(e -> attivaFigure());
        lavagna.setOnMousePressed(this::clickMouseLavagna);
        lavagna.setOnMouseDragged(this::trascinoMouseLavagna);
        lavagna.setOnMouseReleased(this::rilascioMouseLavagna);

        // Attivo scrittura all'inizio
        attivaScrittura();
    }

    @FXML
    private void setListenerBox() {
        // textBox
        colorPicker.setOnAction(e -> contestoGrafico.setStroke(colorPicker.getValue()));
        grandezzaLinea.valueProperty().addListener((obs, oldValue, newValue) -> contestoGrafico.setLineWidth(newValue));
        // gommaBox
        grandezzaGomma.valueProperty().addListener((obs, oldValue, newValue) -> contestoGrafico.setLineWidth(newValue));
        cBott.setOnMouseMoved(event -> cBott.setStyle("-fx-background-color: #1A80E4; -fx-border-color: #E9EEF4; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-width: 1.5; -fx-font-weight: 700;"));
        cBott.setOnMouseExited(event -> cBott.setStyle("-fx-background-color: #1A80E4; -fx-border-color: #1A80E4; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-width: 1.5; -fx-font-weight: 700;"));
        // figureBox
        // Aggiungo tutte le figure disponibili
        choiceFigure.getItems().addAll("Rettangolo", "Cerchio", "Triangolo", "Parallelogramma", "Rombo");
        choiceFigure.setValue("Rettangolo"); // Predefinito
        choiceFigure.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            figuraSelezionata = newValue;
            // Se si seleziona Cerchio o Triangolo, disabilito dim2
            if ("Cerchio".equals(newValue) || "Triangolo".equals(newValue))
                dim2.setDisable(true);
            else
                dim2.setDisable(false);
        });
        dim1.valueProperty().addListener((obs, oldValue, newValue) -> { dim1S = newValue; });
        dim2.valueProperty().addListener((obs, oldValue, newValue) -> { dim2S = newValue; });
        colorPickerBordo.setOnAction(e -> contestoGrafico.setStroke(colorPickerBordo.getValue()));
        colorPickerRiempimento.setOnAction(e -> contestoGrafico.setStroke(colorPickerRiempimento.getValue()));
        grandezzaBordo.valueProperty().addListener((obs, oldValue, newValue) -> contestoGrafico.setLineWidth(newValue));
        isTrasparente.selectedProperty().addListener((observable, oldValue, newValue) -> {
            colorPickerRiempimento.setDisable(newValue);
        });
    }

    // MODALITÀ
    private void attivaScrittura() {
        chiudiModalitaPrecedenti();
        isDisegnoActive = true;
        isGommaActive = false;
        isTestoActive = false;
        isFigureActive = false;

        pennaButton.setStyle("-fx-background-color: E9EEF4; -fx-border-color: E9EEF4; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-border-width: 2;");
        textBox.setVisible(true);
        textBox.setDisable(false);
        entrataAnchor(textBox, -300, 0);
        contestoGrafico.setStroke(colorPicker.getValue());
        contestoGrafico.setLineWidth(grandezzaLinea.getValue());
    }

    private void attivaCancellazione() {
        chiudiModalitaPrecedenti();
        isDisegnoActive = false;
        isGommaActive = true;
        isTestoActive = false;
        isFigureActive = false;

        gommaButton.setStyle("-fx-background-color: E9EEF4; -fx-border-color: E9EEF4; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-border-width: 2;");
        gommaBox.setVisible(true);
        gommaBox.setDisable(false);
        entrataAnchor(gommaBox, -300, 0);
        contestoGrafico.setLineWidth(grandezzaGomma.getValue());
    }

    // Attiva la modalità testo
    private void attivaTesto() {
        chiudiModalitaPrecedenti();
        isDisegnoActive = false;
        isGommaActive = false;
        isTestoActive = true;
        isFigureActive = false;
    }

    // Attiva la modalità figure
    private void attivaFigure() {
        chiudiModalitaPrecedenti();
        isDisegnoActive = false;
        isGommaActive = false;
        isTestoActive = false;
        isFigureActive = true;

        figureButton.setStyle("-fx-background-color: E9EEF4; -fx-border-color: E9EEF4; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-border-width: 2;");
        figureBox.setVisible(true);
        figureBox.setDisable(false);
        entrataAnchor(figureBox, -300, 0);
        figuraSelezionata = choiceFigure.getValue();
        dim1S = dim1.getValue();
        dim2S = dim2.getValue();
    }

    // CHIUSURA MODALITÀ
    private void chiudiModalitaPrecedenti() {
        if (isDisegnoActive) {
            entrataAnchor(textBox, 0, -300);
            textBox.setVisible(false);
            textBox.setDisable(true);
            pennaButton.setStyle("-fx-background-color: #E2EAE3;");
        } else if (isGommaActive) {
            entrataAnchor(gommaBox, 0, -300);
            gommaBox.setVisible(false);
            gommaBox.setDisable(true);
            gommaButton.setStyle("-fx-background-color: #E2EAE3;");
        } else if (isFigureActive) {
            entrataAnchor(figureBox, 0, -300);
            figureBox.setVisible(false);
            figureBox.setDisable(true);
            figureButton.setStyle("-fx-background-color: #E2EAE3;");
        }
    }

    // EVENTI
    // Evento al click sul canvas
    private void clickMouseLavagna(MouseEvent mouse) {
        if (isDisegnoActive) {
            contestoGrafico.beginPath();
            contestoGrafico.moveTo(mouse.getX(), mouse.getY());
            contestoGrafico.stroke();
        } else if (isGommaActive) {
            cancella(mouse.getX(), mouse.getY());
        } else if (isFigureActive) {
            contestoGrafico.setStroke(colorPickerBordo.getValue());
            if (isTrasparente.isSelected()) contestoGrafico.setFill(Color.TRANSPARENT);
            else contestoGrafico.setFill(colorPickerRiempimento.getValue());
            // Per le figure, calcoliamo le coordinate iniziali centrando la forma attorno al punto cliccato
            if ("Triangolo".equals(figuraSelezionata)) {
                double lato = dim1S;
                // Calcola l'altezza del triangolo equilatero usando la formula: altezza = (√3 / 2) * lato
                double altezza = Math.sqrt(3) / 2 * lato;
                // Per centrare il triangolo, si sottrae metà del lato (per la X) e metà dell'altezza (per la Y)
                inizialeXF = mouse.getX() - lato / 2;
                inizialeYF = mouse.getY() - altezza / 2;
                // Calcolo dei punti:
                // Il vertice superiore (centro della base): si trova a metà del lato in X, sulla linea superiore in Y
                // Gli altri due punti si trovano agli estremi sinistro e destro, spostati lungo l'altezza
                double[] xPoints = {
                        inizialeXF + lato / 2, // Vertice superiore: centro in X
                        inizialeXF,            // Vertice sinistro
                        inizialeXF + lato      // Vertice destro
                };
                double[] yPoints = {
                        inizialeYF,            // Vertice superiore: posizione Y iniziale
                        inizialeYF + altezza,  // Vertice sinistro: in basso
                        inizialeYF + altezza   // Vertice destro: in basso
                };

                contestoGrafico.fillPolygon(xPoints, yPoints, 3);
                contestoGrafico.strokePolygon(xPoints, yPoints, 3);
            } else if ("Parallelogramma".equals(figuraSelezionata)) {
                // Per centrare il parallelepipedo, sottraiamo metà di dim1S (larghezza) e metà di dim2S (altezza)
                inizialeXF = mouse.getX() - dim1S / 2;
                inizialeYF = mouse.getY() - dim2S / 2;
                // L'offset serve per dare un effetto "3D" spostando leggermente la parte superiore
                double offset = dim1S / 4.0;
                // Calcolo dei quattro punti:
                // Punto in alto a sinistra: inizialeXF + offset (X) e inizialeYF (Y)
                // Punto in alto a destra: inizialeXF + dim1S + offset (X) e inizialeYF (Y)
                // Punto in basso a destra: inizialeXF + dim1S (X) e inizialeYF + dim2S (Y)
                // Punto in basso a sinistra: inizialeXF (X) e inizialeYF + dim2S (Y)
                double[] xPoints = {
                        inizialeXF + offset,
                        inizialeXF + dim1S + offset,
                        inizialeXF + dim1S,
                        inizialeXF
                };
                double[] yPoints = {
                        inizialeYF,
                        inizialeYF,
                        inizialeYF + dim2S,
                        inizialeYF + dim2S
                };

                contestoGrafico.fillPolygon(xPoints, yPoints, 4);
                contestoGrafico.strokePolygon(xPoints, yPoints, 4);
            } else if ("Rombo".equals(figuraSelezionata)) {
                // Per centrare il rombo, si usano le dimensioni dim1S e dim2S
                inizialeXF = mouse.getX() - dim1S / 2;
                inizialeYF = mouse.getY() - dim2S / 2;
                // Calcolo dei punti:
                // Il primo punto è il vertice superiore, a metà della larghezza
                // Il secondo punto è il vertice destro, a metà dell'altezza in basso
                // Il terzo punto è il vertice inferiore, a metà della larghezza
                // Il quarto punto è il vertice sinistro, a metà dell'altezza in alto
                double[] xPoints = {
                        inizialeXF + dim1S / 2, // Vertice superiore
                        inizialeXF + dim1S,     // Vertice destro
                        inizialeXF + dim1S / 2, // Vertice inferiore
                        inizialeXF             // Vertice sinistro
                };
                double[] yPoints = {
                        inizialeYF,               // Vertice superiore
                        inizialeYF + dim2S / 2,     // Vertice destro
                        inizialeYF + dim2S,         // Vertice inferiore
                        inizialeYF + dim2S / 2      // Vertice sinistro
                };

                contestoGrafico.fillPolygon(xPoints, yPoints, 4);
                contestoGrafico.strokePolygon(xPoints, yPoints, 4);
            } else if ("Rettangolo".equals(figuraSelezionata)) {
                // Centriamo il rettangolo sottraendo metà della larghezza (dim1S) e metà dell'altezza (dim2S)
                inizialeXF = mouse.getX() - dim1S / 2;
                inizialeYF = mouse.getY() - dim2S / 2;

                contestoGrafico.fillRect(inizialeXF, inizialeYF, dim1S, dim2S);
                contestoGrafico.strokeRect(inizialeXF, inizialeYF, dim1S, dim2S);
            } else if ("Cerchio".equals(figuraSelezionata)) {
                // Per il cerchio, consideriamo dim1S come diametro, centrando sottraendo metà del diametro
                inizialeXF = mouse.getX() - dim1S / 2;
                inizialeYF = mouse.getY() - dim1S / 2;

                contestoGrafico.fillOval(inizialeXF, inizialeYF, dim1S, dim1S);
                contestoGrafico.strokeOval(inizialeXF, inizialeYF, dim1S, dim1S);
            }
        }
    }

    // Evento di trascinamento del mouse sul canvas
    @FXML
    private void trascinoMouseLavagna(MouseEvent mouse) {
        if (isDisegnoActive) {
            contestoGrafico.lineTo(mouse.getX(), mouse.getY());
            contestoGrafico.stroke();
        } else if (isGommaActive) {
            cancella(mouse.getX(), mouse.getY());
        } else if (isFigureActive) {
            contestoGrafico.setStroke(colorPickerBordo.getValue());
            if (isTrasparente.isSelected()) contestoGrafico.setFill(Color.TRANSPARENT);
            else contestoGrafico.setFill(colorPickerRiempimento.getValue());
            // Aggiorniamo il disegno in tempo reale durante il trascinamento
            if ("Triangolo".equals(figuraSelezionata)) {
                double lato = dim1S;
                double altezza = Math.sqrt(3) / 2 * lato;
                // Calcolo delle coordinate correnti centrate sul cursore
                double correnteXF = mouse.getX() - lato / 2;
                double correnteYF = mouse.getY() - altezza / 2;
                double[] xPoints = {
                        correnteXF + lato / 2, // Vertice superiore
                        correnteXF,            // Vertice sinistro
                        correnteXF + lato      // Vertice destro
                };
                double[] yPoints = {
                        correnteYF,            // Vertice superiore
                        correnteYF + altezza,  // Vertice sinistro
                        correnteYF + altezza   // Vertice destro
                };

                contestoGrafico.fillPolygon(xPoints, yPoints, 3);
                contestoGrafico.strokePolygon(xPoints, yPoints, 3);
            } else if ("Parallelogramma".equals(figuraSelezionata)) {
                double correnteXF = mouse.getX() - dim1S / 2;
                double correnteYF = mouse.getY() - dim2S / 2;
                double offset = dim1S / 4.0;
                double[] xPoints = {
                        correnteXF + offset,
                        correnteXF + dim1S + offset,
                        correnteXF + dim1S,
                        correnteXF
                };
                double[] yPoints = {
                        correnteYF,
                        correnteYF,
                        correnteYF + dim2S,
                        correnteYF + dim2S
                };

                contestoGrafico.fillPolygon(xPoints, yPoints, 4);
                contestoGrafico.strokePolygon(xPoints, yPoints, 4);
            } else if ("Rombo".equals(figuraSelezionata)) {
                double correnteXF = mouse.getX() - dim1S / 2;
                double correnteYF = mouse.getY() - dim2S / 2;
                double[] xPoints = {
                        correnteXF + dim1S / 2, // Vertice superiore
                        correnteXF + dim1S,     // Vertice destro
                        correnteXF + dim1S / 2, // Vertice inferiore
                        correnteXF             // Vertice sinistro
                };
                double[] yPoints = {
                        correnteYF,               // Vertice superiore
                        correnteYF + dim2S / 2,     // Vertice destro
                        correnteYF + dim2S,         // Vertice inferiore
                        correnteYF + dim2S / 2      // Vertice sinistro
                };

                contestoGrafico.fillPolygon(xPoints, yPoints, 4);
                contestoGrafico.strokePolygon(xPoints, yPoints, 4);
            } else if ("Rettangolo".equals(figuraSelezionata)) {
                double correnteXF = mouse.getX() - dim1S / 2;
                double correnteYF = mouse.getY() - dim2S / 2;

                contestoGrafico.fillRect(correnteXF, correnteYF, dim1S, dim2S);
                contestoGrafico.strokeRect(correnteXF, correnteYF, dim1S, dim2S);
            } else if ("Cerchio".equals(figuraSelezionata)) {
                double correnteXF = mouse.getX() - dim1S / 2;
                double correnteYF = mouse.getY() - dim1S / 2;

                contestoGrafico.fillOval(correnteXF, correnteYF, dim1S, dim1S);
                contestoGrafico.strokeOval(correnteXF, correnteYF, dim1S, dim1S);
            }
        }
    }

    // Evento al rilascio del mouse sul canvas
    @FXML
    private void rilascioMouseLavagna(MouseEvent mouse) {
        if (isDisegnoActive) {
            contestoGrafico.closePath();
        } else if (isFigureActive) {
            contestoGrafico.setStroke(colorPickerBordo.getValue());
            if (isTrasparente.isSelected()) contestoGrafico.setFill(Color.TRANSPARENT);
            else contestoGrafico.setFill(colorPickerRiempimento.getValue());
            // Disegno finale, usando le coordinate finali in base al punto in cui il mouse viene rilasciato
            Shape figura = null;
            if ("Triangolo".equals(figuraSelezionata)) {
                double lato = dim1S;
                double altezza = Math.sqrt(3) / 2 * lato;
                // Le coordinate finali centrano il triangolo sul punto di rilascio del mouse
                double correnteX = mouse.getX() - lato / 2;
                double correnteY = mouse.getY() - altezza / 2;
                double[] xPoints = {
                        correnteX + lato / 2, // Vertice superiore
                        correnteX,            // Vertice sinistro
                        correnteX + lato      // Vertice destro
                };
                double[] yPoints = {
                        correnteY,            // Vertice superiore
                        correnteY + altezza,  // Vertice sinistro
                        correnteY + altezza   // Vertice destro
                };
                Polygon triangolo = new Polygon();
                triangolo.getPoints().addAll(xPoints[0], yPoints[0], xPoints[1], yPoints[1], xPoints[2], yPoints[2]);
                figura = triangolo;
                contestoGrafico.fillPolygon(xPoints, yPoints, 3);
                contestoGrafico.strokePolygon(xPoints, yPoints, 3);
            } else if ("Parallelogramma".equals(figuraSelezionata)) {
                double correnteX = mouse.getX() - dim1S / 2;
                double correnteY = mouse.getY() - dim2S / 2;
                double offset = dim1S / 4.0;
                double[] xPoints = {
                        correnteX + offset,
                        correnteX + dim1S + offset,
                        correnteX + dim1S,
                        correnteX
                };
                double[] yPoints = {
                        correnteY,
                        correnteY,
                        correnteY + dim2S,
                        correnteY + dim2S
                };
                Polygon parallelepipedo = new Polygon();
                parallelepipedo.getPoints().addAll(xPoints[0], yPoints[0], xPoints[1], yPoints[1],
                        xPoints[2], yPoints[2], xPoints[3], yPoints[3]);
                figura = parallelepipedo;
                contestoGrafico.fillPolygon(xPoints, yPoints, 4);
                contestoGrafico.strokePolygon(xPoints, yPoints, 4);
            } else if ("Rombo".equals(figuraSelezionata)) {
                double correnteX = mouse.getX() - dim1S / 2;
                double correnteY = mouse.getY() - dim2S / 2;
                double[] xPoints = {
                        correnteX + dim1S / 2, // Vertice superiore
                        correnteX + dim1S,     // Vertice destro
                        correnteX + dim1S / 2, // Vertice inferiore
                        correnteX             // Vertice sinistro
                };
                double[] yPoints = {
                        correnteY,
                        correnteY + dim2S / 2,
                        correnteY + dim2S,
                        correnteY + dim2S / 2
                };
                Polygon rombo = new Polygon();
                rombo.getPoints().addAll(xPoints[0], yPoints[0], xPoints[1], yPoints[1],
                        xPoints[2], yPoints[2], xPoints[3], yPoints[3]);
                figura = rombo;
                contestoGrafico.fillPolygon(xPoints, yPoints, 4);
                contestoGrafico.strokePolygon(xPoints, yPoints, 4);
            } else if ("Rettangolo".equals(figuraSelezionata)) {
                double correnteX = mouse.getX() - dim1S / 2;
                double correnteY = mouse.getY() - dim2S / 2;
                figura = new Rectangle(correnteX, correnteY, dim1S, dim2S);
                // Qui si usa la coordinata iniziale precedentemente calcolata per il fill
                contestoGrafico.fillRect(inizialeXF, inizialeYF, dim1S, dim2S);
                contestoGrafico.strokeRect(correnteX, correnteY, dim1S, dim2S);
            } else if ("Cerchio".equals(figuraSelezionata)) {
                double correnteX = mouse.getX() - dim1S / 2;
                double correnteY = mouse.getY() - dim1S / 2;
                figura = new Circle(correnteX, correnteY, dim1S);
                // Anche qui, si usa la coordinata iniziale per il disegno dell'oval
                contestoGrafico.fillOval(inizialeXF, inizialeYF, dim1S, dim1S);
                contestoGrafico.strokeOval(correnteX, correnteY, dim1S, dim1S);
            }
            // Se la figura è stata creata, la aggiungiamo alla lista delle figure inserite
            if (figura != null) {
                figureInserite.add(figura);
            }
        }
    }


    // Funzione per cancellare una parte del disegno
    @FXML
    private void cancella(double x, double y) {
        contestoGrafico.setFill(Color.WHITE);
        contestoGrafico.fillRect(x - (double) grandezzaGomma.getValue() / 2,
                y - (double) grandezzaGomma.getValue() / 2,
                grandezzaGomma.getValue(), grandezzaGomma.getValue());
    }

    @FXML
    protected void cancellaTutto() {
        contestoGrafico.setFill(Color.WHITE);
        contestoGrafico.fillRect(0, 0, lavagna.getWidth(), lavagna.getHeight());
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

/*
Step per il disegno di un tratto:
    1) Mouse premuto: Inizia un nuovo percorso, posizionando il punto iniziale.
    2) Mouse trascinato: Disegna la linea seguendo il movimento del mouse.
    3) Mouse rilasciato: Termina il percorso.
*/
