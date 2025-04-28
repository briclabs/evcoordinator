package net.briclabs.evcoordinator.controller.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.Logic;
import net.briclabs.evcoordinator.RegistrationPacketLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.model.CreateResponse;
import net.briclabs.evcoordinator.model.RegistrationPacket;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;


@RestController
@CrossOrigin(
        origins = "${app.cors.origins}",
        allowedHeaders = "*",
        methods = { RequestMethod.POST }
)
@EnableMethodSecurity
@Validated
@RequestMapping(ApiController.V1 + "/registrationPacket")
public class  RegistrationPacketController extends ApiController {

    protected final RegistrationPacketLogic writeLogic;
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationPacketController.class);

    @Autowired
    public RegistrationPacketController(ObjectMapper objectMapper, DSLContext dslContext) {
        super(objectMapper, dslContext);
        this.writeLogic = new RegistrationPacketLogic(objectMapper, dslContext);
    }

    /**
     * Creates a new registration with the provided registration details.
     *
     * @param registration the RegistrationPacket object containing the details of the new profile to be created
     * @return a ResponseEntity containing the ID of the newly created profile if successful,
     *         or an appropriate error status if the operation fails
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateResponse> register(@RequestBody RegistrationPacket registration) {
        var errors = writeLogic.validate(registration);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CreateResponse(-1L, errors));
        }

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(new CreateResponse(writeLogic.register(getActorId(), registration), Collections.emptyMap()));
        } catch (RegistrationPacketLogic.RegistrationPacketException e) {
            LOGGER.error("Failed to process registration packet.", e);
            return ResponseEntity.internalServerError().body(new CreateResponse(-1L, Map.ofEntries(e.getPublicMessage())));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new CreateResponse(-1L, Map.of("%s|%s".formatted(Logic.GENERAL_MESSAGE_KEY, Logic.GENERAL_MESSAGE_KEY), "An internal server error occurred. Please try again. If the problem persists, please contact the administrator.")));
        }
    }
}
