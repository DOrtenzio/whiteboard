package whiteboard.whiteboard.azioni.figure;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import whiteboard.whiteboard.azioni.Figura;

public class Cerchio extends Figura {
    public Cerchio(double x, double y, double dim1, Color coloreBordo, Color coloreRiempimento, int spessoreBordo) {
        super(x, y, dim1, dim1, coloreBordo, coloreRiempimento, spessoreBordo); // Il diametro è uguale per x e y
    }

    @Override
    public void disegna(GraphicsContext gc, Canvas lavagna) {
        impostaStili(gc);
        gc.fillOval(x, y, dim1, dim1);
        gc.strokeOval(x, y, dim1, dim1);
    }
}

