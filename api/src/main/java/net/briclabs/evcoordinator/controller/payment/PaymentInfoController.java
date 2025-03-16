package net.briclabs.evcoordinator.controller.payment;

import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.PaymentInfoLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.WriteController;
import net.briclabs.evcoordinator.generated.tables.pojos.PaymentInfo;
import net.briclabs.evcoordinator.model.SearchRequest;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@RequestMapping(ApiController.V1 + "/payment/info")
public class PaymentInfoController<P extends PaymentInfo> extends ApiController<PaymentInfoLogic<P>> implements WriteController<P> {

    @Autowired
    public PaymentInfoController(DSLContext dslContext) {
        super(dslContext, new PaymentInfoLogic<>(dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    public PaymentInfo findById(@PathVariable("id") Long id) {
        return logic.fetchById(id).orElse(null);
    }

    @Override
    @PostMapping(path = "/search")
    public ListWithCount<PaymentInfo> search(@RequestBody SearchRequest searchRequest) {
        return logic.fetchByCriteria(
                searchRequest.searchConfiguration().exactMatch(),
                searchRequest.searchCriteria(),
                searchRequest.searchConfiguration().sortColumn(),
                searchRequest.searchConfiguration().sortAsc(),
                searchRequest.searchConfiguration().offset(),
                searchRequest.searchConfiguration().max()
        );
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody P paymentInfo) throws HttpClientErrorException {
        if (logic.validateIsTrulyNew(paymentInfo)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }
        return logic.insertNew(paymentInfo).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public int update(@RequestBody P updatedPaymentInfo) {
        return logic.updateExisting(updatedPaymentInfo);
    }
}
