package net.briclabs.evcoordinator.controller.payment;

import net.briclabs.evcoordinator.PaymentLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.pojos.Payment;
import net.briclabs.evcoordinator.generated.tables.records.PaymentRecord;
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
@RequestMapping(ApiController.V1 + "/payment")
public class PaymentController extends ApiController<PaymentLogic> {

    public static final Class<Payment> POJO = Payment.class;

    @Autowired
    public PaymentController(DSLContext dslContext) {
        super(dslContext, new PaymentLogic(dslContext));
    }

    @GetMapping(value = "/{id}")
    public Payment findById(@PathVariable("id") Long id)
    {
        return logic.fetchById(id).map(r -> r.into(POJO)).orElse(null);
    }

    @GetMapping(value = "/{offset}/{max}")
    public List<Payment> findByCriteria(
            @PathVariable("offset") int offset,
            @PathVariable("max") int max,
            @RequestParam Map<String, String> criteria) {
        return logic.fetchByCriteria(criteria, offset, max).stream().map(result -> result.into(POJO)).collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody Payment payment)
    {
        if (logic.isEntryAlreadyExists(payment)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        return logic.insertNew(payment).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Long update(@PathVariable("id") Long id, @RequestBody Payment updatedPayment)
    {
        Optional<Long> existingMatchingRecordId = logic.isUpdateRedundant(id, updatedPayment);

        if (existingMatchingRecordId.isEmpty()) {
            PaymentRecord existingRecord = logic.fetchById(id).orElseThrow(() -> new HttpClientErrorException(HttpStatus.METHOD_NOT_ALLOWED));
            PaymentRecord updateRecord = new PaymentRecord(updatedPayment);
            return logic.updateExisting(updateRecord, existingRecord);
        }

        if (existingMatchingRecordId.get().equals(id)) {
            return id;
        } else {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }
    }
}
