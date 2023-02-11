package net.briclabs.evcoordinator.controller.history;

import net.briclabs.evcoordinator.HistoryLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
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
    @GetMapping(value = "/{offset}/{max}")
    public List<DataHistory> findByCriteria(@PathVariable("offset") int offset, @PathVariable("max") int max, @RequestParam Map<String, String> criteria) {
        return new ArrayList<>(logic.fetchByCriteria(criteria, offset, max));
    }

}
