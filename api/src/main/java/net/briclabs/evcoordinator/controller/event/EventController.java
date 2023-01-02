package net.briclabs.evcoordinator.controller.event;

import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.daos.EventRecordDao;
import net.briclabs.evcoordinator.generated.tables.pojos.EventRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping(ApiController.V1 + "/event")
public class EventController extends ApiController {

    @Autowired
    private EventRecordDao dao;

    public EventController(DSLContext dslContext) {
        super(dslContext);
    }

    @GetMapping(value = "/{id}")
    public EventRecord findById(@PathVariable("id") Long id)
    {
        return dao.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody EventRecord event)
    {
        if (event != null && dao.findOptionalById(event.getId()).isEmpty()) {
            dao.insert(event);
        }
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") Long id, @RequestBody EventRecord updatedEvent)
    {
        Optional<EventRecord> existingEvent = dao.findOptionalById(id);
        if (existingEvent.isEmpty()) {
            create(updatedEvent);
            return;
        }
        if (!existingEvent.get().equals(updatedEvent)) {
            dao.update(updatedEvent);
        }
    }
}
