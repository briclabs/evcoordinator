package net.briclabs.evcoordinator.controller.participant;

import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.ParticipantLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.WriteController;
import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
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
@RequestMapping(ApiController.V1 + "/participant")
public class ParticipantController<P extends Participant> extends ApiController<ParticipantLogic<P>> implements WriteController<P> {

    @Autowired
    public ParticipantController(DSLContext dslContext) {
        super(dslContext, new ParticipantLogic<>(dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    public Participant findById(@PathVariable("id") Long id) {
        return logic.fetchById(id).orElse(null);
    }

    @Override
    @PostMapping(path = "/search")
    public ListWithCount<Participant> search(@RequestBody SearchRequest searchRequest) {
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
    public Long create(@RequestBody P participant) throws HttpClientErrorException {
        if (logic.validateIsTrulyNew(participant)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        return logic.insertNew(participant).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public int update(@RequestBody P updatedParticipant) {
        return logic.updateExisting(updatedParticipant);
    }
}
