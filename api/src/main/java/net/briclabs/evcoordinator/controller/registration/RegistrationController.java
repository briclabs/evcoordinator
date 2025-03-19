package net.briclabs.evcoordinator.controller.registration;

import net.briclabs.evcoordinator.RegistrationLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.model.RegistrationRequest;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;


@RestController
@CrossOrigin(
        origins = "${app.cors.origins}",
        allowedHeaders = "*",
        methods = { RequestMethod.POST }
)
@EnableMethodSecurity
@Validated
@RequestMapping(ApiController.V1 + "/registration")
public class RegistrationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationController.class);

    private final RegistrationLogic logic;

    @Autowired
    public RegistrationController(DSLContext dslContext) {
        logic = new RegistrationLogic(dslContext);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody RegistrationRequest registration) throws HttpClientErrorException {
        if (registration == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }
        LOGGER.info("Received registration request: {}", registration);

        var participant = registration.participant();
        var associations = registration.associations();
        var eventInfoId = registration.eventInfoId();
        var donationPledge = registration.donationPledge();
        var signature = registration.signature();

        if (logic.validateIsTrulyNew(participant, associations, eventInfoId, donationPledge, signature)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }
        return logic.insertNew(participant, associations, eventInfoId, donationPledge, signature).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
