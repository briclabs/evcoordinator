package net.briclabs.evcoordinator.controller.event;

import net.briclabs.evcoordinator.EventInfoLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.WriteController;
import net.briclabs.evcoordinator.generated.tables.pojos.EventInfo;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
@Validated
@RequestMapping(ApiController.V1 + "/event/info")
public class EventInfoController<P extends EventInfo> extends ApiController<EventInfoLogic<P>> implements WriteController<P> {

    @Autowired
    public EventInfoController(DSLContext dslContext) {
        super(dslContext, new EventInfoLogic<>(dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    public EventInfo findById(@PathVariable("id") Long id) {
        return logic.fetchById(id).orElse(null);
    }

    @Override
    @GetMapping(value = "/{offset}/{max}")
    public List<EventInfo> findByCriteria(@PathVariable("offset") int offset, @PathVariable("max") int max, @RequestParam Map<String, String> criteria) {
        return new ArrayList<>(logic.fetchByCriteria(criteria, offset, max));
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody P eventInfo) throws HttpClientErrorException {
        if (logic.validateIsTrulyNew(eventInfo)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        return logic.insertNew(eventInfo).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public int update(@RequestBody P updatedEventInfo) {
        return logic.updateExisting(updatedEventInfo);
    }
}
