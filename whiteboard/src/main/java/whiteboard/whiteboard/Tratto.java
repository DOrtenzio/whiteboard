package whiteboard.whiteboard;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Tratto {
    private List<Double> xPoints;  // Lista delle coordinate X dei punti
    private List<Double> yPoints;  // Lista delle coordinate Y dei punti
    private Color coloreBordo;
    private double spessoreLinea;

    // Costruttore
    public Tratto(double startX, double startY, Color coloreBordo, double spessoreLinea) {
        this.xPoints = new ArrayList<>();
        this.yPoints = new ArrayList<>();
        this.xPoints.add(startX);
        this.yPoints.add(startY);
        this.coloreBordo = coloreBordo;
        this.spessoreLinea = spessoreLinea;
    }

    // Aggiungi un punto alla lista dei punti
    public void addPoint(double x, double y) {
        this.xPoints.add(x);
        this.yPoints.add(y);
    }

    // Disegna il tratto sul contesto grafico
    public void draw(GraphicsContext gc) {
        gc.setStroke(coloreBordo);
        gc.setLineWidth(spessoreLinea);

        for (int i = 1; i < xPoints.size(); i++) {
            gc.strokeLine(xPoints.get(i - 1), yPoints.get(i - 1), xPoints.get(i), yPoints.get(i));
        }
    }

    // Ottieni i punti
    public List<Double> getXPoints() {
        return xPoints;
    }

    public List<Double> getYPoints() {
        return yPoints;
    }

    public Color getColoreBordo() {
        return coloreBordo;
    }

    public double getSpessoreLinea() {
        return spessoreLinea;
    }
}
