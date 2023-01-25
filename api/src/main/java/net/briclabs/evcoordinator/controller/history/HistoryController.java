package net.briclabs.evcoordinator.controller.history;

import net.briclabs.evcoordinator.HistoryLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import org.jooq.DSLContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiController.V1 + "/history")
public class HistoryController extends ApiController<HistoryLogic> {
// TODO this should only have objects created via TRIGGER...

    public static final Class<DataHistory> POJO = DataHistory.class;

    public HistoryController(DSLContext dslContext) {
        super(dslContext, new HistoryLogic(dslContext));
    }

    @GetMapping(value = "/{id}")
    public DataHistory findById(@PathVariable("id") Long id) {
        return logic.fetchById(id).map(r -> r.into(POJO)).orElse(null);
    }

}
