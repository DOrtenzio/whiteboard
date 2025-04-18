package whiteboard.whiteboard.azioni;

import com.fasterxml.jackson.annotation.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "tipo"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Linea.class, name = "LINEA"),
        @JsonSubTypes.Type(value = Figura.class, name = "FIGURA"),
        @JsonSubTypes.Type(value = Gomma.class, name = "GOMMA")
})

public abstract class Elementi {
    private String tipo; // Tipo di azione: "LINEA", "FIGURA", "GOMMA", "GOMMA_TOTALE"

    public Elementi() {
    }
    public Elementi(String tipo) {
        this.tipo = tipo;
    }

    // Getter e Setter
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    //metodo disegna
    public abstract void disegna(GraphicsContext gc, Canvas lavagna);
}
