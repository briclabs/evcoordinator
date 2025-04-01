package net.briclabs.evcoordinator.controller.participant;

import net.briclabs.evcoordinator.GuestLogic;
import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.DeleteEndpoint;
import net.briclabs.evcoordinator.controller.ReadController;
import net.briclabs.evcoordinator.controller.WriteController;
import net.briclabs.evcoordinator.generated.tables.pojos.Guest;
import net.briclabs.evcoordinator.generated.tables.pojos.GuestWithLabels;
import net.briclabs.evcoordinator.generated.tables.records.GuestRecord;
import net.briclabs.evcoordinator.model.SearchRequest;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.client.HttpClientErrorException;


@RestController
@CrossOrigin(
        origins = "${app.cors.origins}",
        allowedHeaders = "*",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT }
)
@EnableMethodSecurity
@Validated
@RequestMapping(ApiController.V1 + "/guest")
public class GuestController<P extends Guest> extends ApiController<
        GuestRecord,
        Guest,
        net.briclabs.evcoordinator.generated.tables.Guest,
        GuestLogic<P>
    > implements WriteController<P>, ReadController<GuestWithLabels>, DeleteEndpoint {

    @Autowired
    public GuestController(DSLContext dslContext) {
        super(dslContext, new GuestLogic<>(dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    public ResponseEntity<GuestWithLabels> findById(@PathVariable("id") Long id) {
        return logic.getGuestWithLabelsLogic().fetchById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping(path = "/search")
    public ResponseEntity<ListWithCount<GuestWithLabels>> search(@RequestBody SearchRequest searchRequest) {
        ListWithCount<GuestWithLabels> rawMatchingRegistrations = logic.getGuestWithLabelsLogic().fetchByCriteria(
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
    public ResponseEntity<Long> create(@RequestBody P guest) throws HttpClientErrorException {
        return logic.isAlreadyRecorded(guest)
                ? ResponseEntity.status(HttpStatus.FORBIDDEN).build()
                : logic.insertNew(guest).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.internalServerError().build());
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Integer> update(@RequestBody P guest) {
        int countOfRecordsUpdated = logic.updateExisting(guest);
        return countOfRecordsUpdated > 0 ? ResponseEntity.ok(countOfRecordsUpdated) : ResponseEntity.internalServerError().build();
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        logic.delete(id);
        return ResponseEntity.noContent().build();
    }
}
