package whiteboard.whiteboard.azioni;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import whiteboard.whiteboard.client.Client;

import java.util.ArrayList;

public class Stato {
    private ArrayList<Elementi> salvataggiLavagna;
    @JsonIgnore
    private Client client;

    public Stato(){
        this.salvataggiLavagna = new ArrayList<>();
    }
    public Stato(Client client){
        salvataggiLavagna =new ArrayList<Elementi>();
        this.client=client;
    }

    //metodi
    public ArrayList<Elementi> getSalvataggiLavagna() { return salvataggiLavagna; }
    public void setSalvataggiLavagna(ArrayList<Elementi> salvataggiLavagna) { this.salvataggiLavagna = salvataggiLavagna; }
    public void setClient(Client client) { this.client = client; }

    public void add(Elementi elemento){
        salvataggiLavagna.add(elemento);
        inviaAggiornamento();
    }
    public void removeLast(){
        salvataggiLavagna.removeLast();
        inviaAggiornamento();
    }
    public void inviaAggiornamento(){
        client.inviaAggiornamentoStato();
    }
    public Elementi ottieniUltimoInserito(){ return salvataggiLavagna.getLast(); }
    public void disegnaStato(GraphicsContext contestoGrafico, Canvas lavagna){
        for (Elementi elemento: salvataggiLavagna) elemento.disegna(contestoGrafico,lavagna);
    }
    public void aggiornaSeDiverso(Stato altroStato, GraphicsContext contestoGrafico, Canvas lavagna) {
        if (!this.salvataggiLavagna.equals(altroStato.getSalvataggiLavagna())) {
            this.salvataggiLavagna = new ArrayList<>(altroStato.getSalvataggiLavagna());
            disegnaStato(contestoGrafico, lavagna);
        }
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }
}
