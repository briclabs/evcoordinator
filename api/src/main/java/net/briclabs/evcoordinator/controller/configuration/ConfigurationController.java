package net.briclabs.evcoordinator.controller.configuration;


import net.briclabs.evcoordinator.ConfigurationLogic;
import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.WriteController;
import net.briclabs.evcoordinator.generated.tables.pojos.Configuration;
import net.briclabs.evcoordinator.model.SearchRequest;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Validated
@EnableMethodSecurity
@RequestMapping(ApiController.V1 + "/configuration")
public class ConfigurationController<P extends Configuration> extends ApiController<ConfigurationLogic<P>> implements WriteController<P> {

    @Autowired
    public ConfigurationController(DSLContext dslContext) {
        super(dslContext, new ConfigurationLogic<>(dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    public Configuration findById(@PathVariable("id") Long id) {
        return logic.fetchById(id).orElse(null);
    }

    @Override
    @PostMapping(path = "/search")
    public ListWithCount<Configuration> search(@RequestBody SearchRequest searchRequest) {
        return logic.fetchByCriteria(
                searchRequest.searchConfiguration().exactMatch(),
                searchRequest.searchCriteria(),
                searchRequest.searchConfiguration().sortColumn(),
                searchRequest.searchConfiguration().sortAsc(),
                searchRequest.searchConfiguration().offset(),
                searchRequest.searchConfiguration().max()
        );
    }

    @GetMapping(value = "/latest")
    public Configuration findLatest() {
        return logic.fetchLatest().orElse(null);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public Long create(@RequestBody P participant) throws HttpClientErrorException {
        if (logic.validateIsTrulyNew(participant)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        return logic.insertNew(participant).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public int update(@RequestBody P updatedParticipant) {
        return logic.updateExisting(updatedParticipant);
    }
}
