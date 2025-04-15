package net.briclabs.evcoordinator.controller.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.HistoryLogic;
import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.ReadController;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistoryWithLabels;
import net.briclabs.evcoordinator.generated.tables.records.DataHistoryWithLabelsRecord;
import net.briclabs.evcoordinator.model.SearchRequest;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
public class HistoryController extends ReadController<
        DataHistoryWithLabelsRecord,
        DataHistoryWithLabels,
        net.briclabs.evcoordinator.generated.tables.DataHistoryWithLabels,
        HistoryLogic.DataHistoryWithLabelsLogic> {

    @Autowired
    public HistoryController(ObjectMapper objectMapper, DSLContext dslContext) {
        super(objectMapper, dslContext, new HistoryLogic.DataHistoryWithLabelsLogic(objectMapper, dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<DataHistoryWithLabels> fetchById(@PathVariable("id") Long id) {
        return super.fetchById(id);
    }

    @Override
    @PostMapping(path = "/search")
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<ListWithCount<DataHistoryWithLabels>> search(@RequestBody SearchRequest searchRequest) {
        return super.search(searchRequest);
    }
}
