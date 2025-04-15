package net.briclabs.evcoordinator.controller.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.RegistrationLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.WriteWithDeleteController;
import net.briclabs.evcoordinator.generated.tables.pojos.Registration;
import net.briclabs.evcoordinator.generated.tables.pojos.RegistrationWithLabels;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationRecord;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationWithLabelsRecord;
import net.briclabs.evcoordinator.model.CreateResponse;
import net.briclabs.evcoordinator.model.DeleteResponse;
import net.briclabs.evcoordinator.model.SearchRequest;
import net.briclabs.evcoordinator.model.UpdateResponse;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin(
        origins = "${app.cors.origins}",
        allowedHeaders = "*",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT }
)
@EnableMethodSecurity
@Validated
@RequestMapping(ApiController.V1 + "/registration")
public class RegistrationController extends WriteWithDeleteController<
        RegistrationWithLabelsRecord,
        RegistrationWithLabels,
        net.briclabs.evcoordinator.generated.tables.RegistrationWithLabels,
        RegistrationLogic.RegistrationWithLabelsLogic,
        RegistrationRecord,
        Registration,
        net.briclabs.evcoordinator.generated.tables.Registration,
        RegistrationLogic> {

    @Autowired
    public RegistrationController(ObjectMapper objectMapper, DSLContext dslContext) {
        super(objectMapper, dslContext, new RegistrationLogic.RegistrationWithLabelsLogic(objectMapper, dslContext), new RegistrationLogic(objectMapper, dslContext));
    }

    @Override
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<DeleteResponse> delete(@PathVariable("id") Long id) {
        return super.delete(id);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<CreateResponse> create(@RequestBody Registration pojo) {
        return super.create(pojo);
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<UpdateResponse> update(@RequestBody Registration pojo) {
        return super.update(pojo);
    }

    @Override
    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<RegistrationWithLabels> fetchById(@PathVariable("id") Long id) {
        return super.fetchById(id);
    }

    @Override
    @PostMapping(path = "/search")
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<ListWithCount<RegistrationWithLabels>> search(@RequestBody SearchRequest searchRequest) {
        return super.search(searchRequest);
    }
}
