package net.briclabs.evcoordinator.controller.event;

import net.briclabs.evcoordinator.EventLogic;
import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.WriteController;
import net.briclabs.evcoordinator.generated.tables.pojos.Event;
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
@RequestMapping(ApiController.V1 + "/event")
public class EventController<P extends Event> extends ApiController<EventLogic<P>> implements WriteController<P> {

    @Autowired
    public EventController(DSLContext dslContext) {
        super(dslContext, new EventLogic<>(dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    public Event findById(@PathVariable("id") Long id) {
        return logic.fetchById(id).orElse(null);
    }

    @Override
    @PostMapping(path = "/search")
    public ListWithCount<Event> search(@RequestBody SearchRequest searchRequest) {
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
    public Long create(@RequestBody P event) throws HttpClientErrorException {
        if (logic.validateIsTrulyNew(event)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        return logic.insertNew(event).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public int update(@RequestBody P updatedEvent) {
        return logic.updateExisting(updatedEvent);
    }
}
