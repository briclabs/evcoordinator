package net.briclabs.evcoordinator.controller.configuration;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.ConfigurationLogic;
import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.ReadController;
import net.briclabs.evcoordinator.controller.WriteController;
import net.briclabs.evcoordinator.generated.tables.pojos.Configuration;
import net.briclabs.evcoordinator.generated.tables.records.ConfigurationRecord;
import net.briclabs.evcoordinator.model.SearchRequest;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT}
)
@EnableMethodSecurity
@Validated
@RequestMapping(ApiController.V1 + "/configuration")
public class ConfigurationController<P extends Configuration> extends ApiController<
        ConfigurationRecord,
        Configuration,
        net.briclabs.evcoordinator.generated.tables.Configuration,
        ConfigurationLogic<P>
    > implements WriteController<P>, ReadController<Configuration> {

    @Autowired
    public ConfigurationController(ObjectMapper objectMapper, DSLContext dslContext) {
        super(objectMapper, dslContext, new ConfigurationLogic<>(objectMapper, dslContext));
    }

    @Override
    @GetMapping(value = "/{id}")
    public ResponseEntity<Configuration> findById(@PathVariable("id") Long id) {
        return logic.fetchById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping(path = "/search")
    public ResponseEntity<ListWithCount<Configuration>> search(@RequestBody SearchRequest searchRequest) {
        return ResponseEntity.ok(logic.fetchByCriteria(
                searchRequest.searchConfiguration().exactMatch(),
                searchRequest.searchCriteria(),
                searchRequest.searchConfiguration().sortColumn(),
                searchRequest.searchConfiguration().sortAsc(),
                searchRequest.searchConfiguration().offset(),
                searchRequest.searchConfiguration().max()
        ));
    }

    @GetMapping(value = "/latest")
    public ResponseEntity<Configuration> findLatest() {
        return logic.fetchLatest().map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<Long> create(@RequestBody P configuration) throws HttpClientErrorException {
        return logic.isAlreadyRecorded(configuration)
                ? ResponseEntity.status(HttpStatus.FORBIDDEN).build()
                : logic.insertNew(getActorId(), configuration).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.internalServerError().build());
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<Integer> update(@RequestBody P updatedConfiguration) {
        int countOfRecordsUpdated = logic.updateExisting(getActorId(), updatedConfiguration);
        return countOfRecordsUpdated > 0 ? ResponseEntity.ok(countOfRecordsUpdated) : ResponseEntity.internalServerError().build();
    }
}
