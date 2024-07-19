package net.briclabs.evcoordinator.controller.payment;

import net.briclabs.evcoordinator.PaymentLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.WriteController;
import net.briclabs.evcoordinator.generated.tables.pojos.Payment;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@Validated
@RequestMapping(ApiController.V1 + "/payment")
public class PaymentController<P extends Payment> extends ApiController<PaymentLogic<P>> implements WriteController<P> {

    @Autowired
    public PaymentController(DSLContext dslContext) {
        super(dslContext, new PaymentLogic<>(dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    public Payment findById(@PathVariable("id") Long id) {
        return logic.fetchById(id).orElse(null);
    }

    @Override
    @GetMapping(value = "/{offset}/{max}")
    public List<Payment> findByCriteria(@PathVariable("offset") int offset, @PathVariable("max") int max, @RequestParam Map<String, String> criteria) {
        return new ArrayList<>(logic.fetchByCriteria(criteria, offset, max));
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody P payment) throws HttpClientErrorException {
        if (logic.validateIsTrulyNew(payment)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        return logic.insertNew(payment).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public int update(@RequestBody P updatedPayment) {
        return logic.updateExisting(updatedPayment);
    }
}
