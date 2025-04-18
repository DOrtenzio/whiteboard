package whiteboard.whiteboard.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import javafx.scene.paint.Color;

import java.io.IOException;

public class ColorSerializer extends JsonSerializer<Color> {
    @Override
    public void serialize(Color color, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("red", color.getRed());
        gen.writeNumberField("green", color.getGreen());
        gen.writeNumberField("blue", color.getBlue());
        gen.writeNumberField("opacity", color.getOpacity());
        gen.writeEndObject();
    }
}

