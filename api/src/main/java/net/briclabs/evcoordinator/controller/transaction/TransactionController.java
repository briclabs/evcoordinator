package net.briclabs.evcoordinator.controller.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.TransactionLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.DeleteEndpoint;
import net.briclabs.evcoordinator.controller.ReadController;
import net.briclabs.evcoordinator.controller.WriteController;
import net.briclabs.evcoordinator.generated.tables.pojos.TransactionWithLabels;
import net.briclabs.evcoordinator.generated.tables.pojos.Transaction_;
import net.briclabs.evcoordinator.generated.tables.records.Transaction_Record;
import net.briclabs.evcoordinator.model.SearchRequest;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.client.HttpClientErrorException;


@RestController
@CrossOrigin(
        origins = "${app.cors.origins}",
        allowedHeaders = "*",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT }
)
@EnableMethodSecurity
@Validated
@RequestMapping(ApiController.V1 + "/transactions")
public class TransactionController<P extends Transaction_> extends ApiController<
        Transaction_Record,
        Transaction_,
        net.briclabs.evcoordinator.generated.tables.Transaction_,
        TransactionLogic<P>
    > implements WriteController<P>, ReadController<TransactionWithLabels>, DeleteEndpoint {

    @Autowired
    public TransactionController(ObjectMapper objectMapper, DSLContext dslContext) {
        super(objectMapper, dslContext, new TransactionLogic<>(objectMapper, dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    public ResponseEntity<TransactionWithLabels> findById(@PathVariable("id") Long id) {
        return logic.getTransactionWithLabelsLogic().fetchById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping(path = "/search")
    public ResponseEntity<ListWithCount<TransactionWithLabels>> search(@RequestBody SearchRequest searchRequest) {
        return ResponseEntity.ok(logic.getTransactionWithLabelsLogic().fetchByCriteria(
                searchRequest.searchConfiguration().exactMatch(),
                searchRequest.searchCriteria(),
                searchRequest.searchConfiguration().sortColumn(),
                searchRequest.searchConfiguration().sortAsc(),
                searchRequest.searchConfiguration().offset(),
                searchRequest.searchConfiguration().max()
        ));
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> create(@RequestBody P transaction) throws HttpClientErrorException {
        return logic.isAlreadyRecorded(transaction)
                ? ResponseEntity.status(HttpStatus.FORBIDDEN).build()
                : logic.insertNew(getActorId(), transaction).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.internalServerError().build());
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Integer> update(@RequestBody P updatedTransaction) {
        int countOfRecordsUpdated = logic.updateExisting(getActorId(), updatedTransaction);
        return countOfRecordsUpdated > 0 ? ResponseEntity.ok(countOfRecordsUpdated) : ResponseEntity.internalServerError().build();
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        logic.delete(getActorId(), id);
        return ResponseEntity.noContent().build();
    }
}
