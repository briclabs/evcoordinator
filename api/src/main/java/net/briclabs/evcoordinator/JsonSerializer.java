package net.briclabs.evcoordinator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jooq.JSON;

import java.io.IOException;

public class JsonSerializer extends StdSerializer<JSON> {
    public JsonSerializer() {
        super(JSON.class);
    }

    @Override
    public void serialize(JSON value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            // Write the JSON value as a string
            gen.writeRawValue(value.data());
        }
    }
}
