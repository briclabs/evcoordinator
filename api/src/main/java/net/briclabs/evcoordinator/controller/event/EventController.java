package net.briclabs.evcoordinator.controller.event;

import net.briclabs.evcoordinator.EventLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.pojos.Event;
import net.briclabs.evcoordinator.generated.tables.records.EventRecord;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping(ApiController.V1 + "/event")
public class EventController extends ApiController<EventLogic> {

    public static final Class<EventRecord> POJO = EventRecord.class;

    public EventController(DSLContext dslContext) {
        super(dslContext, new EventLogic(dslContext));
    }

    @GetMapping(value = "/{id}")
    public EventRecord findById(@PathVariable("id") Long id)
    {
        return logic.fetchById(id).map(r -> r.into(POJO)).orElse(null);
    }

    @GetMapping(value = "/{offset}/{max}")
    public List<EventRecord> findByCriteria(
            @PathVariable("offset") int offset,
            @PathVariable("max") int max,
            @RequestParam Map<String, String> criteria) {
        return logic.fetchByCriteria(criteria, offset, max).stream().map(result -> result.into(POJO)).collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody Event event)
    {
        if (logic.isEntryAlreadyExists(event)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        return logic.insertNew(event).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Long update(@PathVariable("id") Long id, @RequestBody Event updatedEvent)
    {
        Optional<Long> existingMatchingRecordId = logic.isUpdateRedundant(id, updatedEvent);

        if (existingMatchingRecordId.isEmpty()) {
            EventRecord existingRecord = logic.fetchById(id).orElseThrow(() -> new HttpClientErrorException(HttpStatus.METHOD_NOT_ALLOWED));
            EventRecord updateRecord = new EventRecord(updatedEvent);
            return logic.updateExisting(updateRecord, existingRecord);
        }

        if (existingMatchingRecordId.get().equals(id)) {
            return id;
        } else {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }
    }
}
