package whiteboard.whiteboard.azioni;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Figura extends Elementi {
    protected double x, y, dim1, dim2;
    protected Color coloreBordo, coloreRiempimento;
    protected int spessoreBordo;

    public Figura(double x, double y, double dim1, double dim2, Color coloreBordo, Color coloreRiempimento, int spessoreBordo) {
        super("FIGURA");
        this.x = x;
        this.y = y;
        this.dim1 = dim1;
        this.dim2 = dim2;
        this.coloreBordo = coloreBordo;
        this.coloreRiempimento = coloreRiempimento;
        this.spessoreBordo = spessoreBordo;
    }

    @Override
    public abstract void disegna(GraphicsContext gc, Canvas lavagna);

    public void impostaStili(GraphicsContext gc) {
        gc.setStroke(coloreBordo);
        gc.setLineWidth(spessoreBordo);
        gc.setFill(coloreRiempimento);
    }
}
