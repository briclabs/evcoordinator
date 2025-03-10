package net.briclabs.evcoordinator.controller.history;

import net.briclabs.evcoordinator.HistoryLogic;
import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import net.briclabs.evcoordinator.model.SearchRequest;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiController.V1 + "/history")
public class HistoryController extends ApiController<HistoryLogic> {
// TODO this should only have objects created via TRIGGER...

    public static final Class<DataHistory> POJO = DataHistory.class;

    @Autowired
    public HistoryController(DSLContext dslContext) {
        super(dslContext, new HistoryLogic(dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    public DataHistory findById(@PathVariable("id") Long id) {
        return logic.fetchById(id).orElse(null);
    }

    @Override
    @PostMapping(path = "/search")
    public ListWithCount<DataHistory> search(@RequestBody SearchRequest searchRequest) {
        return logic.fetchByCriteria(
                searchRequest.searchConfiguration().exactMatch(),
                searchRequest.searchCriteria(),
                searchRequest.searchConfiguration().sortColumn(),
                searchRequest.searchConfiguration().sortAsc(),
                searchRequest.searchConfiguration().offset(),
                searchRequest.searchConfiguration().max()
        );
    }
}
