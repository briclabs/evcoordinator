package net.briclabs.evcoordinator.controller.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.RegistrationPacketLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.pojos.RegistrationPacketWithLabel;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationPacketWithLabelRecord;
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


@RestController
@CrossOrigin(
        origins = "${app.cors.origins}",
        allowedHeaders = "*",
        methods = { RequestMethod.POST }
)
@EnableMethodSecurity
@Validated
@RequestMapping(ApiController.V1 + "/registrationPacket")
public class  RegistrationPacketController extends ApiController<
        RegistrationPacketWithLabelRecord,
        RegistrationPacketWithLabel,
        net.briclabs.evcoordinator.generated.tables.RegistrationPacketWithLabel,
        RegistrationPacketLogic<RegistrationPacket>
        > {

    @Autowired
    public RegistrationPacketController(ObjectMapper objectMapper, DSLContext dslContext) {
        super(objectMapper, dslContext, new RegistrationPacketLogic<>(objectMapper, dslContext));
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
    public ResponseEntity<Long> register(@RequestBody RegistrationPacket registration) {
        return registration == null
                ? ResponseEntity.badRequest().build()
                : logic.register(getActorId(), registration).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.internalServerError().build());
    }
}
