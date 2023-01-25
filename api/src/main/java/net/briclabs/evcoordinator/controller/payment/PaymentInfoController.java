package net.briclabs.evcoordinator.controller.payment;

import net.briclabs.evcoordinator.PaymentInfoLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.pojos.PaymentInfo;
import net.briclabs.evcoordinator.generated.tables.records.PaymentInfoRecord;
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
@RequestMapping(ApiController.V1 + "/payment/info")
public class PaymentInfoController extends ApiController<PaymentInfoLogic> {

    public static final Class<PaymentInfo> POJO = PaymentInfo.class;

    @Autowired
    public PaymentInfoController(DSLContext dslContext) {
        super(dslContext, new PaymentInfoLogic(dslContext));
    }

    @GetMapping(value = "/{id}")
    public PaymentInfo findById(@PathVariable("id") Long id)
    {
        return logic.fetchById(id).map(r -> r.into(POJO)).orElse(null);
    }

    @GetMapping(value = "/{offset}/{max}")
    public List<PaymentInfo> findByCriteria(
            @PathVariable("offset") int offset,
            @PathVariable("max") int max,
            @RequestParam Map<String, String> criteria) {
        return logic.fetchByCriteria(criteria, offset, max).stream().map(result -> result.into(POJO)).collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody PaymentInfo paymentInfo)
    {
        if (logic.isEntryAlreadyExists(paymentInfo)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        return logic.insertNew(paymentInfo).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Long update(@PathVariable("id") Long id, @RequestBody PaymentInfo updatedPaymentInfo)
    {
        Optional<Long> existingMatchingRecordId = logic.isUpdateRedundant(id, updatedPaymentInfo);

        if (existingMatchingRecordId.isEmpty()) {
            PaymentInfoRecord existingRecord = logic.fetchById(id).orElseThrow(() -> new HttpClientErrorException(HttpStatus.METHOD_NOT_ALLOWED));
            PaymentInfoRecord updateRecord = new PaymentInfoRecord(updatedPaymentInfo);
            return logic.updateExisting(updateRecord, existingRecord);
        }

        if (existingMatchingRecordId.get().equals(id)) {
            return id;
        } else {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }
    }
}
