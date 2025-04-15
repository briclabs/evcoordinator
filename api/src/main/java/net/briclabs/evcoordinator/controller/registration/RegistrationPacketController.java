package net.briclabs.evcoordinator.controller.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.RegistrationPacketLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.model.CreateResponse;
import net.briclabs.evcoordinator.model.RegistrationPacket;
import org.jooq.DSLContext;
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CreateResponse(null, errors));
        }

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(new CreateResponse(writeLogic.register(getActorId(), registration), null));
        } catch (RegistrationPacketLogic.RegistrationPacketException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CreateResponse(null, Map.ofEntries(e.getPublicMessage())));
        }
    }
}
