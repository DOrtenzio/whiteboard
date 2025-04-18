package whiteboard.whiteboard.azioni.figure;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import whiteboard.whiteboard.azioni.Figura;

public class Rettangolo extends Figura {
    public Rettangolo() {
    }

    public Rettangolo(double x, double y, double dim1, double dim2, Color coloreBordo, Color coloreRiempimento, int spessoreBordo) {
        super(x, y, dim1, dim2, coloreBordo, coloreRiempimento, spessoreBordo);
    }

    @Override
    public void disegna(GraphicsContext gc, Canvas lavagna) {
        impostaStili(gc);
        gc.fillRect(x, y, dim1, dim2);
        gc.strokeRect(x, y, dim1, dim2);
    }
}

