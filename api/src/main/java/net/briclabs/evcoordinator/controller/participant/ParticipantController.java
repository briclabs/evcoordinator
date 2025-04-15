package net.briclabs.evcoordinator.controller.participant;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.ParticipantLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.controller.WriteController;
import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import net.briclabs.evcoordinator.generated.tables.records.ParticipantRecord;
import net.briclabs.evcoordinator.model.CreateResponse;
import net.briclabs.evcoordinator.model.SearchRequest;
import net.briclabs.evcoordinator.model.UpdateResponse;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT }
)
@EnableMethodSecurity
@Validated
@RequestMapping(ApiController.V1 + "/participant")
public class ParticipantController extends WriteController<
        ParticipantRecord,
        Participant,
        net.briclabs.evcoordinator.generated.tables.Participant,
        ParticipantLogic,
        ParticipantRecord,
        Participant,
        net.briclabs.evcoordinator.generated.tables.Participant,
        ParticipantLogic> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantController.class);

    @Autowired
    public ParticipantController(ObjectMapper objectMapper, DSLContext dslContext) {
        super(objectMapper, dslContext, new ParticipantLogic(objectMapper, dslContext), new ParticipantLogic(objectMapper, dslContext));
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<CreateResponse> create(@RequestBody Participant pojo) {
        return super.create(pojo);
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<UpdateResponse> update(@RequestBody Participant pojo) {
        return super.update(pojo);
    }

    @Override
    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<Participant> fetchById(@PathVariable("id") Long id) {
        return super.fetchById(id);
    }

    @Override
    @PostMapping(path = "/search")
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<ListWithCount<Participant>> search(@RequestBody SearchRequest searchRequest) {
        return super.search(searchRequest);
    }

    @PostMapping("/preexists")
    public ResponseEntity<Void> preexists(@RequestBody Participant participant) {
        var preexistingParticipant = readLogic.fetchAttendeeByNameAndEmail(participant.getNameFirst(), participant.getNameLast(), participant.getAddrEmail());

        LOGGER.info("preexistingParticipant: {}", preexistingParticipant);

        if (preexistingParticipant.count() > 0) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
