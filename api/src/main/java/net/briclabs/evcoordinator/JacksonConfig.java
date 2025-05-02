package net.briclabs.evcoordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.briclabs.evcoordinator.generated.enums.EmergencyContactRelationshipType;
import net.briclabs.evcoordinator.generated.enums.ParticipantType;
import net.briclabs.evcoordinator.generated.enums.UsStateAbbreviations;
import org.jooq.JSON;
import org.jooq.JSONB;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(JSON.class, new JsonSerializer());
        module.addSerializer(JSONB.class, new JsonbSerializer());
        module.addDeserializer(JSON.class, new JsonObjectDeserializer());
        objectMapper.registerModule(module);

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // This is required because for preexisting Participants during registration, these fields are empty.
        objectMapper.coercionConfigFor(UsStateAbbreviations.class).setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
        objectMapper.coercionConfigFor(ParticipantType.class).setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
        objectMapper.coercionConfigFor(EmergencyContactRelationshipType.class).setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);

        return objectMapper;
    }
}
