package whiteboard.whiteboard.azioni;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Gomma extends Elementi {
    private double x,y;
    private double spessoreGomma;

    public Gomma(String tipo, double x, double y, double spessoreGomma) {
        super(tipo); //GOMMA o GOMMA_TOTAL
        this.x=x;
        this.y=y;
        this.spessoreGomma = spessoreGomma;
    }

    //getter e setter
    public void setSpessoreGomma(double spessoreGomma) { this.spessoreGomma = spessoreGomma; }
    public double getSpessoreGomma() { return spessoreGomma; }

    //metodo disegna
    public void disegna(GraphicsContext gc, Canvas lavagna){
        if (super.getTipo().equalsIgnoreCase("GOMMA")) {
            gc.setFill(Color.WHITE);
            gc.fillRect(x - spessoreGomma / 2, y - spessoreGomma / 2, spessoreGomma, spessoreGomma);
        }else{
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, lavagna.getWidth(), lavagna.getHeight());
        }
    }
}
