package whiteboard.whiteboard.azioni;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public  abstract class Elementi {
    private String tipo; // Tipo di azione: "LINEA", "FIGURA", "GOMMA", "GOMMA_TOTALE"

    public Elementi(String tipo) {
        this.tipo = tipo;
    }

    // Getter e Setter
    public String getTipo() { return tipo; }

    //metodo disegna
    public abstract void disegna(GraphicsContext gc, Canvas lavagna);
}
