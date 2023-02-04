package net.briclabs.evcoordinator.controller.event;

import net.briclabs.evcoordinator.EventLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.pojos.Event;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(ApiController.V1 + "/event")
public class EventController extends ApiController<EventLogic> {

    @Autowired
    public EventController(DSLContext dslContext) {
        super(dslContext, new EventLogic(dslContext));
    }

    @GetMapping(value = "/{id}")
    public Event findById(@PathVariable("id") Long id) {
        return logic.fetchById(id).orElse(null);
    }

    @GetMapping(value = "/{offset}/{max}")
    public List<Event> findByCriteria(@PathVariable("offset") int offset, @PathVariable("max") int max, @RequestParam Map<String, String> criteria) {
        return new ArrayList<>(logic.fetchByCriteria(criteria, offset, max));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody Event event) {
        if (logic.validateIsTrulyNew(event)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        return logic.insertNew(event).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public int update(@RequestBody Event updatedEvent) {
        return logic.updateExisting(updatedEvent);
    }
}
