package whiteboard.whiteboard.azioni;

import java.util.ArrayList;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LogsLavagne {
    private ArrayList<String> idLavagneSalvate;

    // Costruttore, getter e setter
    public LogsLavagne() {
        this.idLavagneSalvate = new ArrayList<String>();
    }

    public ArrayList<String> getIdLavagneSalvate() {
        return idLavagneSalvate;
    }

    public void setIdLavagneSalvate(ArrayList<String> idLavagneSalvate) {
        this.idLavagneSalvate = idLavagneSalvate;
    }

    // Altri metodi
    public void add(String idLavagna) {
        this.idLavagneSalvate.add(idLavagna);
    }

    public String getSingolaLavagna(int index) {
        if (index < 0 || index >= idLavagneSalvate.size())
            return "NOT_FOUND";
        else
            return idLavagneSalvate.get(index);
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
