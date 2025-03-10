package net.briclabs.evcoordinator.controller.participant;

import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.ParticipantAssociationLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.WriteController;
import net.briclabs.evcoordinator.generated.tables.pojos.ParticipantAssociation;
import net.briclabs.evcoordinator.model.SearchRequest;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;


@RestController
@Validated
@RequestMapping(ApiController.V1 + "/participant/assoc")
public class ParticipantAssociationController<P extends ParticipantAssociation> extends ApiController<ParticipantAssociationLogic<P>> implements WriteController<P> {

    @Autowired
    public ParticipantAssociationController(DSLContext dslContext) {
        super(dslContext, new ParticipantAssociationLogic<>(dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    public ParticipantAssociation findById(@PathVariable("id") Long id) {
        return logic.fetchById(id).orElse(null);
    }

    @Override
    @PostMapping(path = "/search")
    public ListWithCount<ParticipantAssociation> search(@RequestBody SearchRequest searchRequest) {
        return logic.fetchByCriteria(
                searchRequest.searchConfiguration().exactMatch(),
                searchRequest.searchCriteria(),
                searchRequest.searchConfiguration().sortColumn(),
                searchRequest.searchConfiguration().sortAsc(),
                searchRequest.searchConfiguration().offset(),
                searchRequest.searchConfiguration().max()
        );
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody P participantAssociation) throws HttpClientErrorException {
        if (logic.validateIsTrulyNew(participantAssociation)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        return logic.insertNew(participantAssociation).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public int update(@RequestBody P updatedParticipantAssociation) {
        return logic.updateExisting(updatedParticipantAssociation);
    }
}
