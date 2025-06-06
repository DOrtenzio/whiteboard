package whiteboard.whiteboard.azioni.figure;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import whiteboard.whiteboard.azioni.Figura;

public class Triangolo extends Figura {
    public Triangolo() {
    }
    public Triangolo(double x, double y, double dim1, double dim2, Color coloreBordo, Color coloreRiempimento, int spessoreBordo) {
        super(x, y, dim1, dim2, coloreBordo, coloreRiempimento, spessoreBordo);
    }

    @Override
    public void disegna(GraphicsContext gc, Canvas lavagna) {
        impostaStili(gc);
        double[] xPoints = {x + dim1 / 2, x, x + dim1};
        double[] yPoints = {y, y + dim2, y + dim2};
        gc.fillPolygon(xPoints, yPoints, 3);
        gc.strokePolygon(xPoints, yPoints, 3);
    }
}


