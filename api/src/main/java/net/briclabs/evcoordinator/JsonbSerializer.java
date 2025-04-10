package net.briclabs.evcoordinator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jooq.JSONB;

import java.io.IOException;

public class JsonbSerializer extends StdSerializer<JSONB> {
    public JsonbSerializer() {
        super(JSONB.class);
    }

    @Override
    public void serialize(JSONB value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            // Write the JSON value as a string
            gen.writeRawValue(value.data());
        }
    }
}
