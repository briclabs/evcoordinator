package net.briclabs.evcoordinator.controller.registration;

import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.RegistrationPacketLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.WriteController;
import net.briclabs.evcoordinator.model.RegistrationPacket;
import net.briclabs.evcoordinator.model.SearchRequest;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
public class RegistrationPacketController<P extends RegistrationPacket> extends ApiController<RegistrationPacketLogic<P>> implements WriteController<P> {

    @Autowired
    public RegistrationPacketController(DSLContext dslContext) {
        super(dslContext, new RegistrationPacketLogic<>(dslContext));
    }

    @Override
    @GetMapping(value = "/byRegistrationId/{id}")
    public RegistrationPacket findById(@PathVariable("id") Long id) {
        return logic.fetchRegistrationPacketByRegistrationId(id).orElse(null);
    }

    @GetMapping(value = "/byEventInfoId/{id}")
    public RegistrationPacket findByEventInfoId(@PathVariable("id") Long id) {
        return logic.fetchRegistrationPacketByEventInfoId(id).orElse(null);
    }

    @GetMapping(value = "/byParticipantId/{id}")
    public RegistrationPacket findByParticipantId(@PathVariable("id") Long id) {
        return logic.fetchRegistrationPacketParticipantId(id).orElse(null);
    }

    @Override
    @PostMapping(path = "/search")
    public ListWithCount<RegistrationPacket> search(@RequestBody SearchRequest searchRequest) {
        throw new UnsupportedOperationException("Not implemented. Please use the targeted /by...Id/{id} endpoints instead.");
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody P registration) throws HttpClientErrorException {
        if (registration == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }

        return logic.insertNew(registration).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Override
    public int update(P pojo) {
        return 0;
    }
}
