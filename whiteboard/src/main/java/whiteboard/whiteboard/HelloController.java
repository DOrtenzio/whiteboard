package whiteboard.whiteboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.application.Application;
import java.io.IOException;

public class HelloController extends Application {

    @FXML
    private Canvas canvas;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private TextField textField;

    private GraphicsContext gc;
    private boolean isPenMode = true;
    private boolean isTextMode = false;
    private final double ERASER_SIZE = 20;
    private String selectedFont = "Arial";
    private double textSize = 20;


    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    @FXML
    private void handleMousePressed(MouseEvent e) {
        if (isPenMode) {
            gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(2);
            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke();
        } else if (!isPenMode && !isTextMode) {
            gc.clearRect(e.getX() - ERASER_SIZE / 2, e.getY() - ERASER_SIZE / 2, ERASER_SIZE, ERASER_SIZE);
        } else if (isTextMode) {
            addText(e.getX(), e.getY());
        }
    }

    @FXML
    private void handleMouseDragged(MouseEvent e) {
        if (isPenMode) {
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
        } else if (!isPenMode && !isTextMode) {
            gc.clearRect(e.getX() - ERASER_SIZE / 2, e.getY() - ERASER_SIZE / 2, ERASER_SIZE, ERASER_SIZE);
        }
    }

    @FXML
    private void handleMouseReleased(MouseEvent e) {
        if (isPenMode) {
            gc.closePath();
        }
    }

    @FXML
    private void enablePenMode() {
        isPenMode = true;
        isTextMode = false;
    }

    @FXML
    private void enableEraserMode() {
        isPenMode = false;
        isTextMode = false;
    }

    @FXML
    private void enableTextMode() {
        isTextMode = true;
        isPenMode = false;
    }

    private void addText(double x, double y) {
        Text text = new Text(x, y, textField.getText());
        text.setFont(new Font(selectedFont, textSize));
        text.setFill(colorPicker.getValue());

        text.setOnMousePressed(event -> {
            text.setUserData(new double[]{event.getX(), event.getY()});
        });

        text.setOnMouseDragged(event -> {
            double[] startPos = (double[]) text.getUserData();
            text.setX(text.getX() + (event.getX() - startPos[0]));
            text.setY(text.getY() + (event.getY() - startPos[1]));
            text.setUserData(new double[]{event.getX(), event.getY()});
        });

        ((AnchorPane) canvas.getParent()).getChildren().add(text);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
