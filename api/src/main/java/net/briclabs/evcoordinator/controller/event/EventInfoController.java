package net.briclabs.evcoordinator.controller.event;

import net.briclabs.evcoordinator.EventInfoLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.pojos.EventInfo;
import net.briclabs.evcoordinator.generated.tables.records.EventInfoRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiController.V1 + "/event/info")
public class EventInfoController extends ApiController<EventInfoLogic> {

    public static final Class<EventInfo> POJO = EventInfo.class;

    @Autowired
    public EventInfoController(DSLContext dslContext) {
        super(dslContext, new EventInfoLogic(dslContext));
    }

    @GetMapping(value = "/{id}")
    public EventInfo findById(@PathVariable("id") Long id)
    {
        return logic.fetchById(id).map(r -> r.into(POJO)).orElse(null);
    }

    @GetMapping(value = "/{offset}/{max}")
    public List<EventInfo> findByCriteria(
            @PathVariable("offset") int offset,
            @PathVariable("max") int max,
            @RequestParam Map<String, String> criteria) {
        return logic.fetchByCriteria(criteria, offset, max).stream().map(result -> result.into(POJO)).collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody EventInfo eventInfo)
    {
        if (logic.isEntryAlreadyExists(eventInfo)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        return logic.insertNew(eventInfo).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Long update(@PathVariable("id") Long id, @RequestBody EventInfo updatedEventInfo) {
        Optional<Long> existingMatchingRecordId = logic.isUpdateRedundant(id, updatedEventInfo);

        if (existingMatchingRecordId.isEmpty()) {
            EventInfoRecord existingRecord = logic.fetchById(id).orElseThrow(() -> new HttpClientErrorException(HttpStatus.METHOD_NOT_ALLOWED));
            EventInfoRecord updateRecord = new EventInfoRecord(updatedEventInfo);
            return logic.updateExisting(updateRecord, existingRecord);
        }

        if (existingMatchingRecordId.get().equals(id)) {
            return id;
        } else {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }
    }
}
