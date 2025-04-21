package net.briclabs.evcoordinator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.jooq.JSON;

import java.io.IOException;

public class JsonObjectDeserializer extends JsonDeserializer<JSON> {

    @Override
    public JSON deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return JSON.valueOf(p.readValueAsTree().toString());
    }
}
