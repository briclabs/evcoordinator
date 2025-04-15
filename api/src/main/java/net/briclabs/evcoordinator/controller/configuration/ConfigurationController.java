package net.briclabs.evcoordinator.controller.configuration;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.ConfigurationLogic;
import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.WriteController;
import net.briclabs.evcoordinator.generated.tables.pojos.Configuration;
import net.briclabs.evcoordinator.generated.tables.records.ConfigurationRecord;
import net.briclabs.evcoordinator.model.CreateResponse;
import net.briclabs.evcoordinator.model.SearchRequest;
import net.briclabs.evcoordinator.model.UpdateResponse;
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

@RestController
@CrossOrigin(
        origins = "${app.cors.origins}",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT}
)
@EnableMethodSecurity
@Validated
@RequestMapping(ApiController.V1 + "/configuration")
public class ConfigurationController extends WriteController<
        ConfigurationRecord,
        Configuration,
        net.briclabs.evcoordinator.generated.tables.Configuration,
        ConfigurationLogic,
        ConfigurationRecord,
        Configuration,
        net.briclabs.evcoordinator.generated.tables.Configuration,
        ConfigurationLogic> {

    @Autowired
    public ConfigurationController(ObjectMapper objectMapper, DSLContext dslContext) {
        super(objectMapper, dslContext, new ConfigurationLogic(objectMapper, dslContext), new ConfigurationLogic(objectMapper, dslContext));
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<CreateResponse> create(@RequestBody Configuration pojo) {
        return super.create(pojo);
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<UpdateResponse> update(@RequestBody Configuration pojo) {
        return super.update(pojo);
    }

    @Override
    @GetMapping(value = "/{id}")
    public ResponseEntity<Configuration> fetchById(@PathVariable("id") Long id) {
        return super.fetchById(id);
    }

    @Override
    @PostMapping(path = "/search")
    public ResponseEntity<ListWithCount<Configuration>> search(@RequestBody SearchRequest searchRequest) {
        return super.search(searchRequest);
    }

    @GetMapping(value = "/latest")
    public ResponseEntity<Configuration> findLatest() {
        return readLogic.fetchLatest().map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
