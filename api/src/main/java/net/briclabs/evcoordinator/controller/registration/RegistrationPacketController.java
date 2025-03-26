package net.briclabs.evcoordinator.controller.registration;

import net.briclabs.evcoordinator.RegistrationPacketLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.model.RegistrationPacket;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin(
        origins = "${app.cors.origins}",
        allowedHeaders = "*",
        methods = { RequestMethod.POST }
)
@EnableMethodSecurity
@Validated
@RequestMapping(ApiController.V1 + "/registration")
public class RegistrationPacketController {

    private final RegistrationPacketLogic<RegistrationPacket> logic;

    @Autowired
    public RegistrationPacketController(DSLContext dslContext) {
        this.logic = new RegistrationPacketLogic<>(dslContext);
    }

    /**
     * Retrieves a RegistrationPacket by the given registration ID.
     *
     * @param id the unique identifier of the registration to be fetched
     * @return a ResponseEntity containing the RegistrationPacket if found, or a not found
     *         status if no matching registration exists
     */
    @GetMapping(value = "/byRegistrationId/{id}")
    public ResponseEntity<RegistrationPacket> findById(@PathVariable("id") Long id) {
        return logic.fetchRegistrationPacketByRegistrationId(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a RegistrationPacket based on the given event info ID.
     *
     * @param id the unique identifier of the event information to be fetched
     * @return a ResponseEntity containing the RegistrationPacket if found, or a not found
     *         status if no matching event information exists
     */
    @GetMapping(value = "/byEventInfoId/{id}")
    public ResponseEntity<RegistrationPacket> findByEventInfoId(@PathVariable("id") Long id) {
        return logic.fetchRegistrationPacketByEventInfoId(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a RegistrationPacket using the provided participant ID.
     *
     * @param id the unique identifier of the participant whose registration packet is to be fetched
     * @return a ResponseEntity containing the RegistrationPacket if found, or a not found
     *         status if no matching participant exists
     */
    @GetMapping(value = "/byParticipantId/{id}")
    public ResponseEntity<RegistrationPacket> findByParticipantId(@PathVariable("id") Long id) {
        return logic.fetchRegistrationPacketParticipantId(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new registration with the provided registration details.
     *
     * @param registration the RegistrationPacket object containing the details of the new profile to be created
     * @return a ResponseEntity containing the ID of the newly created profile if successful,
     *         or an appropriate error status if the operation fails
     */
    @PostMapping(value = "/newProfile")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> createNew(@RequestBody RegistrationPacket registration) {
        return registration == null
                ? ResponseEntity.badRequest().build()
                : logic.insertNew(registration).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.internalServerError().build());
    }

    /**
     * Creates a new registration using a preexisting profile with the provided registration details.
     *
     * @param registration the RegistrationPacket object containing the details of the registration to be created
     * @return a ResponseEntity containing the ID of the newly created registration if successful,
     *         or an internal server error status if the operation fails
     */
    @PostMapping(value = "/preExisting")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> createPreexisting(@RequestBody RegistrationPacket registration) {
        return registration == null
                ? ResponseEntity.badRequest().build()
                : logic.insertNewRegistrationWithPreexistingProfile(registration).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.internalServerError().build());
    }
}
