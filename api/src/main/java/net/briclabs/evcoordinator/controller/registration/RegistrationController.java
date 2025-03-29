package net.briclabs.evcoordinator.controller.registration;

import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.RegistrationLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.ReadController;
import net.briclabs.evcoordinator.controller.WriteController;
import net.briclabs.evcoordinator.generated.tables.pojos.Registration;
import net.briclabs.evcoordinator.generated.tables.pojos.RegistrationWithLabels;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationRecord;
import net.briclabs.evcoordinator.model.SearchRequest;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT }
)
@EnableMethodSecurity
@Validated
@RequestMapping(ApiController.V1 + "/registration")
public class RegistrationController<P extends Registration> extends ApiController<
        RegistrationRecord,
        Registration,
        net.briclabs.evcoordinator.generated.tables.Registration,
        RegistrationLogic<P>
    > implements WriteController<P>, ReadController<RegistrationWithLabels> {

    @Autowired
    public RegistrationController(DSLContext dslContext) {
        super(dslContext, new RegistrationLogic<>(dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    public ResponseEntity<RegistrationWithLabels> findById(@PathVariable("id") Long id) {
        return logic.getRegistrationWithLabelsLogic().fetchById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping(path = "/search")
    public ResponseEntity<ListWithCount<RegistrationWithLabels>> search(@RequestBody SearchRequest searchRequest) {
        ListWithCount<RegistrationWithLabels> rawMatchingRegistrations = logic.getRegistrationWithLabelsLogic().fetchByCriteria(
                searchRequest.searchConfiguration().exactMatch(),
                searchRequest.searchCriteria(),
                searchRequest.searchConfiguration().sortColumn(),
                searchRequest.searchConfiguration().sortAsc(),
                searchRequest.searchConfiguration().offset(),
                searchRequest.searchConfiguration().max()
        );
        return ResponseEntity.ok(rawMatchingRegistrations);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> create(@RequestBody P registration) throws HttpClientErrorException {
        return logic.isAlreadyRecorded(registration)
                ? ResponseEntity.status(HttpStatus.FORBIDDEN).build()
                : logic.insertNew(registration).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.internalServerError().build());
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Integer> update(@RequestBody P updatedRegistration) {
        int countOfRecordsUpdated = logic.updateExisting(updatedRegistration);
        return countOfRecordsUpdated > 0 ? ResponseEntity.ok(countOfRecordsUpdated) : ResponseEntity.internalServerError().build();
    }
}
