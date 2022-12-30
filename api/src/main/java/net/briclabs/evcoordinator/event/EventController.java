package net.briclabs.evcoordinator.event;

import net.briclabs.evcoordinator.ApiController;
import net.briclabs.evcoordinator.ApiVersion;
import net.briclabs.evcoordinator.generated.tables.daos.EventRecordDao;
import net.briclabs.evcoordinator.generated.tables.pojos.EventRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping(ApiVersion.V1 + "/event")
public class EventController extends ApiController {

    @Autowired
    private EventRecordDao dao;

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
    public void update(@PathVariable("id") Long id, @RequestBody EventRecord event)
    {
        Optional<EventRecord> existingEvent = dao.findOptionalById(id);
        if (existingEvent.isEmpty()) {
            create(event);
            return;
        }
        if (!existingEvent.get().equals(event)) {
            dao.update(event);
        }
    }
}
