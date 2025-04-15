package net.briclabs.evcoordinator.controller.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.EventInfoLogic;
import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.WriteWithDeleteController;
import net.briclabs.evcoordinator.generated.tables.pojos.EventInfo;
import net.briclabs.evcoordinator.generated.tables.records.EventInfoRecord;
import net.briclabs.evcoordinator.model.CreateResponse;
import net.briclabs.evcoordinator.model.DeleteResponse;
import net.briclabs.evcoordinator.model.SearchRequest;
import net.briclabs.evcoordinator.model.UpdateResponse;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin(
        origins = "${app.cors.origins}",
        allowedHeaders = "*",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT }
)
@EnableMethodSecurity
@Validated
@RequestMapping(ApiController.V1 + "/event/info")
public class EventInfoController extends WriteWithDeleteController<
        EventInfoRecord,
        EventInfo,
        net.briclabs.evcoordinator.generated.tables.EventInfo,
        EventInfoLogic,
        EventInfoRecord,
        EventInfo,
        net.briclabs.evcoordinator.generated.tables.EventInfo,
        EventInfoLogic> {

    @Autowired
    public EventInfoController(ObjectMapper objectMapper, DSLContext dslContext) {
        super(objectMapper, dslContext, new EventInfoLogic(objectMapper, dslContext), new EventInfoLogic(objectMapper, dslContext));
    }

    @Override
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<DeleteResponse> delete(@PathVariable("id") Long id) {
        return super.delete(id);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<CreateResponse> create(@RequestBody EventInfo pojo) {
        return super.create(pojo);
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<UpdateResponse> update(@RequestBody EventInfo pojo) {
        return super.update(pojo);
    }

    @Override
    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<EventInfo> fetchById(@PathVariable("id") Long id) {
        return super.fetchById(id);
    }

    @Override
    @PostMapping(path = "/search")
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<ListWithCount<EventInfo>> search(@RequestBody SearchRequest searchRequest) {
        return super.search(searchRequest);
    }

    @GetMapping(value = "/latest")
    public ResponseEntity<EventInfo> findLatest() {
        return readLogic.fetchLatest().map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
