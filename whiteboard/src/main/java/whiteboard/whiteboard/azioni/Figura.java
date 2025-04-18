package whiteboard.whiteboard.azioni;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import whiteboard.whiteboard.azioni.figure.*;

@JsonSubTypes({
        @JsonSubTypes.Type(value = Rettangolo.class, name = "RETTANGOLO"),
        @JsonSubTypes.Type(value = Cerchio.class, name = "CERCHIO"),
        @JsonSubTypes.Type(value = Triangolo.class, name = "TRIANGOLO"),
        @JsonSubTypes.Type(value = Parallelogramma.class, name = "PARALLELOGRAMMA"),
        @JsonSubTypes.Type(value = Rombo.class, name = "ROMBO")
})

public abstract class Figura extends Elementi {
    protected double x, y, dim1, dim2;
    protected Color coloreBordo, coloreRiempimento;
    protected int spessoreBordo;

    public Figura() {
    }
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

    //getter and setter
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getDim1() {
        return dim1;
    }

    public void setDim1(double dim1) {
        this.dim1 = dim1;
    }

    public double getDim2() {
        return dim2;
    }

    public void setDim2(double dim2) {
        this.dim2 = dim2;
    }

    public Color getColoreBordo() {
        return coloreBordo;
    }

    public void setColoreBordo(Color coloreBordo) {
        this.coloreBordo = coloreBordo;
    }

    public Color getColoreRiempimento() {
        return coloreRiempimento;
    }

    public void setColoreRiempimento(Color coloreRiempimento) {
        this.coloreRiempimento = coloreRiempimento;
    }

    public int getSpessoreBordo() {
        return spessoreBordo;
    }

    public void setSpessoreBordo(int spessoreBordo) {
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
