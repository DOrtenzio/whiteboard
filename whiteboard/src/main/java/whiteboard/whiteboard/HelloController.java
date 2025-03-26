package whiteboard.whiteboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.application.Application;
import java.io.IOException;

public class HelloController  {

    @FXML
    private Canvas canvas;
    @FXML
    private ImageView mapView12;   // Penna
    @FXML
    private ImageView mapView121;  // Gomma
    @FXML
    private ImageView mapView12111; // Testo

    private GraphicsContext gc;
    private boolean isPenMode = true;
    private boolean isTextMode = false;
    private boolean isEraserMode = false;
    private final double ERASER_SIZE = 20;

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Associazione degli eventi alle immagini (pulsanti)
        mapView12.setOnMouseClicked(e -> activatePenMode());
        mapView121.setOnMouseClicked(e -> activateEraserMode());
        mapView12111.setOnMouseClicked(e -> activateTextMode());

        // Gestione eventi del Canvas
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
    }

    private void activatePenMode() {
        isPenMode = true;
        isEraserMode = false;
        isTextMode = false;
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
    }

    private void activateEraserMode() {
        isPenMode = false;
        isEraserMode = true;
        isTextMode = false;
        gc.setStroke(Color.TRANSPARENT); // La gomma deve rimuovere i pixel, non colorarli
    }

    private void activateTextMode() {
        isPenMode = false;
        isEraserMode = false;
        isTextMode = true;
    }

    private void handleMousePressed(MouseEvent e) {
        if (isPenMode) {
            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke();
        } else if (isEraserMode) {
            gc.clearRect(e.getX() - ERASER_SIZE / 2, e.getY() - ERASER_SIZE / 2, ERASER_SIZE, ERASER_SIZE);
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (isPenMode) {
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
        } else if (isEraserMode) {
            gc.clearRect(e.getX() - ERASER_SIZE / 2, e.getY() - ERASER_SIZE / 2, ERASER_SIZE, ERASER_SIZE);
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        if (isPenMode) {
            gc.closePath();
        }
    }

}
