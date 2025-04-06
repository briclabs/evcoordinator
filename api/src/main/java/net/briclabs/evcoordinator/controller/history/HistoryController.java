package net.briclabs.evcoordinator.controller.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.HistoryLogic;
import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.ParticipantLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.ReadController;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistoryWithLabels;
import net.briclabs.evcoordinator.generated.tables.records.DataHistoryRecord;
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
        methods = { RequestMethod.GET, RequestMethod.POST }
)
@EnableMethodSecurity
@Validated
@RequestMapping(ApiController.V1 + "/history")
public class HistoryController<P extends DataHistory> extends ApiController<
        DataHistoryRecord,
        DataHistory,
        net.briclabs.evcoordinator.generated.tables.DataHistory,
        HistoryLogic<P>
    > implements ReadController<DataHistoryWithLabels> {

    @Autowired
    public HistoryController(ObjectMapper objectMapper, DSLContext dslContext) {
        super(objectMapper, dslContext, new HistoryLogic<>(objectMapper, new ParticipantLogic<>(objectMapper, dslContext), dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    public ResponseEntity<DataHistoryWithLabels> findById(@PathVariable("id") Long id) {
        return logic.getDataHistoryWithLabelsLogic().fetchById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping(path = "/search")
    public ResponseEntity<ListWithCount<DataHistoryWithLabels>> search(@RequestBody SearchRequest searchRequest) {
        return ResponseEntity.ok(logic.getDataHistoryWithLabelsLogic().fetchByCriteria(
                searchRequest.searchConfiguration().exactMatch(),
                searchRequest.searchCriteria(),
                searchRequest.searchConfiguration().sortColumn(),
                searchRequest.searchConfiguration().sortAsc(),
                searchRequest.searchConfiguration().offset(),
                searchRequest.searchConfiguration().max()
        ));
    }
}
