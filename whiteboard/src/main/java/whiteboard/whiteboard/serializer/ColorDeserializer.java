package whiteboard.whiteboard.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.paint.Color;

import java.io.IOException;

public class ColorDeserializer extends JsonDeserializer<Color> {
    @Override
    public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        double red = node.get("red").asDouble();
        double green = node.get("green").asDouble();
        double blue = node.get("blue").asDouble();
        double opacity = node.has("opacity") ? node.get("opacity").asDouble() : 1.0;
        return new Color(red, green, blue, opacity);
    }
}

