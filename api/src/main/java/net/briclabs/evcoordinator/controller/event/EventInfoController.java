package net.briclabs.evcoordinator.controller.event;

import net.briclabs.evcoordinator.EventInfoLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.pojos.EventInfo;
import net.briclabs.evcoordinator.generated.tables.records.EventInfoRecord;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiController.V1 + "/event/info")
public class EventInfoController extends ApiController {

    public final EventInfoLogic logic;

    public EventInfoController(DSLContext dslContext) {
        super(dslContext);
        logic = new EventInfoLogic(dslContext);
    }

    @GetMapping(value = "/{id}")
    public EventInfo findById(@PathVariable("id") Long id)
    {
        return logic.fetchById(id).map(eventInfoRecord -> eventInfoRecord.into(EventInfo.class)).orElse(null);
    }

    @GetMapping(value = "/{offset}/{max}")
    public List<EventInfo> findByCriteria(
            @PathVariable("offset") int offset,
            @PathVariable("max") int max,
            @RequestParam Map<String, String> criteria) {
        return logic.fetchByCriteria(criteria, offset, max).stream().map(result -> result.into(EventInfo.class)).collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody EventInfo eventInfo)
    {
        if (logic.fetchById(eventInfo.getId()).isPresent()) { // TODO - add fetch by criteria search here
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        Optional<Long> insertedRecord = logic.insertNew(eventInfo);
        if (insertedRecord.isPresent()) {
            return insertedRecord.get();
        }
        throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Long update(@PathVariable("id") Long id, @RequestBody EventInfo updatedEventInfo)
    {
        EventInfoRecord existingEvent = logic.fetchById(id).orElseThrow(() -> new HttpClientErrorException(HttpStatus.METHOD_NOT_ALLOWED));
        return logic.updateExisting(new EventInfoRecord(updatedEventInfo), existingEvent);
    }
}
