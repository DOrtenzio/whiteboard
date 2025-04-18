package whiteboard.whiteboard.azioni;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Linea extends Elementi {
    private ArrayList<Double []> coordinate;
    private Color coloreLinea;
    private double spessoreLinea;

    public Linea() {
    }
    public Linea(double inizioX, double inizioY, Color coloreLinea, double spessoreLinea) {
        super("LINEA");
        coordinate=new ArrayList<Double[]>();

        addCoordinate(inizioX,inizioY);
        this.coloreLinea = coloreLinea;
        this.spessoreLinea = spessoreLinea;
    }

    // Metodo per estendere la linea a un nuovo punto
    public void continuaA(double x, double y) {
        addCoordinate(x,y);
    }

    //Metodo per aggiungere coordinate
    private void addCoordinate(double x, double y){
        Double [] punto={x,y};
        coordinate.add(punto);
    }

    //getter e setter
    public void setColoreLinea(Color coloreLinea) { this.coloreLinea = coloreLinea; }
    public void setSpessoreLinea(double spessoreLinea) { this.spessoreLinea = spessoreLinea; }
    public Color getColoreLinea() { return coloreLinea; }
    public double getSpessoreLinea() { return spessoreLinea; }
    public ArrayList<Double[]> getCoordinate() { return coordinate; }
    public void setCoordinate(ArrayList<Double[]> coordinate) { this.coordinate = coordinate; }

    //metodo disegna
    @Override
    public void disegna(GraphicsContext gc, Canvas lavagna){
        //inizio
        gc.beginPath();
        gc.moveTo(coordinate.getFirst()[0], coordinate.getFirst()[1]);
        gc.setStroke(coloreLinea);
        gc.setLineWidth(spessoreLinea);
        gc.stroke();

        //Continuo
        for (int i=1;i<coordinate.size();i++){
            gc.lineTo(coordinate.get(i)[0], coordinate.get(i)[1]);
            gc.stroke();
        }

        //Fine
        gc.closePath();
    }
}
