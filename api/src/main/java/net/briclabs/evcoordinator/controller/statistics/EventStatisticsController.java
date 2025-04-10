package net.briclabs.evcoordinator.controller.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.EventStatisticsLogic;
import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.ReadController;
import net.briclabs.evcoordinator.generated.tables.pojos.EventStatistics;
import net.briclabs.evcoordinator.generated.tables.records.EventStatisticsRecord;
import net.briclabs.evcoordinator.model.SearchRequest;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(
        origins = "${app.cors.origins}",
        allowedHeaders = "*",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT }
)
@EnableMethodSecurity
@Validated
@RequestMapping(ApiController.V1 + "/statistics")
public class EventStatisticsController<P extends EventStatistics> extends ApiController<
        EventStatisticsRecord,
        EventStatistics,
        net.briclabs.evcoordinator.generated.tables.EventStatistics,
        EventStatisticsLogic
        > implements ReadController<EventStatistics> {

    @Autowired
    public EventStatisticsController(ObjectMapper objectMapper, DSLContext dslContext) {
        super(objectMapper, dslContext, new EventStatisticsLogic(objectMapper, dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    public ResponseEntity<EventStatistics> findById(@PathVariable("id") Long id) {
        return logic.fetchById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/latest")
    public ResponseEntity<EventStatistics> findLatest() {
        return logic.fetchLatest().map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping(path = "/search")
    public ResponseEntity<ListWithCount<EventStatistics>> search(@RequestBody SearchRequest searchRequest) {
        return ResponseEntity.ok(logic.fetchByCriteria(
                searchRequest.searchConfiguration().exactMatch(),
                searchRequest.searchCriteria(),
                searchRequest.searchConfiguration().sortColumn(),
                searchRequest.searchConfiguration().sortAsc(),
                searchRequest.searchConfiguration().offset(),
                searchRequest.searchConfiguration().max()
        ));
    }
}
