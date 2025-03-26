package net.briclabs.evcoordinator.controller.participant;

import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.ParticipantAssociationLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.WriteController;
import net.briclabs.evcoordinator.generated.tables.pojos.ParticipantAssociation;
import net.briclabs.evcoordinator.generated.tables.records.ParticipantAssociationRecord;
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
@RequestMapping(ApiController.V1 + "/participant/assoc")
public class ParticipantAssociationController<P extends ParticipantAssociation> extends ApiController<
        ParticipantAssociationRecord,
        ParticipantAssociation,
        net.briclabs.evcoordinator.generated.tables.ParticipantAssociation,
        ParticipantAssociationLogic<P>
    > implements WriteController<P> {

    @Autowired
    public ParticipantAssociationController(DSLContext dslContext) {
        super(dslContext, new ParticipantAssociationLogic<>(dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    public ResponseEntity<ParticipantAssociation> findById(@PathVariable("id") Long id) {
        return logic.fetchById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping(path = "/search")
    public ResponseEntity<ListWithCount<ParticipantAssociation>> search(@RequestBody SearchRequest searchRequest) {
        return ResponseEntity.ok(logic.fetchByCriteria(
                searchRequest.searchConfiguration().exactMatch(),
                searchRequest.searchCriteria(),
                searchRequest.searchConfiguration().sortColumn(),
                searchRequest.searchConfiguration().sortAsc(),
                searchRequest.searchConfiguration().offset(),
                searchRequest.searchConfiguration().max()
        ));
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> create(@RequestBody P participantAssociation) throws HttpClientErrorException {
        return logic.isAlreadyRecorded(participantAssociation)
                ? ResponseEntity.status(HttpStatus.FORBIDDEN).build()
                : logic.insertNew(participantAssociation).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.internalServerError().build());
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Integer> update(@RequestBody P updatedParticipantAssociation) {
        int countOfRecordsUpdated = logic.updateExisting(updatedParticipantAssociation);
        return countOfRecordsUpdated > 0 ? ResponseEntity.ok(countOfRecordsUpdated) : ResponseEntity.internalServerError().build();
    }
}
