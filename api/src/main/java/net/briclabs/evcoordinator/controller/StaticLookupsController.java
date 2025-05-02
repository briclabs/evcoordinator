package net.briclabs.evcoordinator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.generated.enums.EmergencyContactRelationshipType;
import net.briclabs.evcoordinator.generated.enums.EventStatus;
import net.briclabs.evcoordinator.generated.enums.GuestRelationshipType;
import net.briclabs.evcoordinator.generated.enums.ParticipantType;
import net.briclabs.evcoordinator.generated.enums.TransactionInstrument;
import net.briclabs.evcoordinator.generated.enums.TransactionType;
import net.briclabs.evcoordinator.generated.enums.UsStateAbbreviations;
import org.jooq.DSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@CrossOrigin(
        origins = "${app.cors.origins}",
        allowedHeaders = "*",
        methods = { RequestMethod.GET }
)
@EnableMethodSecurity
@Validated
@RequestMapping(ApiController.V1 + "/staticLookups")
public class StaticLookupsController extends ApiController {

    public StaticLookupsController(ObjectMapper objectMapper, DSLContext dslContext) {
        super(objectMapper, dslContext);
    }

    /**
     * Retrieves all possible values for the emergency contact relationship types.
     * These values are derived from the {@code EmergencyContactRelationshipType} enum.
     *
     * @return a {@code ResponseEntity} containing an array of strings representing the literal values of all emergency contact relationship types.
     */
    @GetMapping(value = "/emergencyContactRelationshipType")
    public ResponseEntity<String[]> emergencyContactRelationshipType() {
        return ResponseEntity.ok().body(Arrays.stream(EmergencyContactRelationshipType.values()).map(EmergencyContactRelationshipType::getLiteral).toArray(String[]::new) );
    }

    /**
     * Retrieves all possible values for the event status.
     * These values are derived from the {@code EventStatus} enum.
     *
     * @return a {@code ResponseEntity} containing an array of strings representing the literal values of all event statuses.
     */
    @GetMapping(value = "/eventStatus")
    public ResponseEntity<String[]> eventStatus() {
        return ResponseEntity.ok().body(Arrays.stream(EventStatus.values()).map(EventStatus::getLiteral).toArray(String[]::new) );
    }

    /**
     * Retrieves all possible values for the guest relationship types.
     * These values are derived from the {@code GuestRelationshipType} enum.
     *
     * @return a {@code ResponseEntity} containing an array of strings representing the literal values of all guest relationship types.
     */
    @GetMapping(value = "/guestRelationshipType")
    public ResponseEntity<String[]> guestRelationshipType() {
        return ResponseEntity.ok().body(Arrays.stream(GuestRelationshipType.values()).map(GuestRelationshipType::getLiteral).toArray(String[]::new) );
    }

    /**
     * Retrieves all possible US state abbreviations.
     * These values are derived from the {@code UsStateAbbreviations} enum.
     *
     * @return a {@code ResponseEntity} containing an array of strings representing the literal values of all US state abbreviations.
     */
    @GetMapping(value = "/usStateAbbreviations")
    public ResponseEntity<String[]> usStateAbbreviations() {
        return ResponseEntity.ok().body(Arrays.stream(UsStateAbbreviations.values()).map(UsStateAbbreviations::getLiteral).toArray(String[]::new) );
    }

    /**
     * Retrieves all possible values for participant types.
     * These values are derived from the {@code ParticipantType} enum.
     *
     * @return a {@code ResponseEntity} containing an array of strings representing the literal values of all participant types.
     */
    @GetMapping(value = "/participantType")
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<String[]> participantType() {
        return ResponseEntity.ok().body(Arrays.stream(ParticipantType.values()).map(ParticipantType::getLiteral).toArray(String[]::new) );
    }

    /**
     * Retrieves all possible values for transaction instruments.
     * These values are derived from the {@code TransactionInstrument} enum.
     *
     * @return a {@code ResponseEntity} containing an array of strings representing the literal values of all transaction instruments.
     */
    @GetMapping(value = "/transactionInstrument")
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<String[]> transactionInstrument() {
        return ResponseEntity.ok().body(Arrays.stream(TransactionInstrument.values()).map(TransactionInstrument::getLiteral).toArray(String[]::new) );
    }

    /**
     * Retrieves all possible values for transaction types.
     * These values are derived from the {@code TransactionType} enum.
     *
     * @return a {@code ResponseEntity} containing an array of strings representing the literal values of all transaction types.
     */
    @GetMapping(value = "/transactionType")
    @PreAuthorize("hasAuthority('evcoordinator:admin')")
    public ResponseEntity<String[]> transactionType() {
        return ResponseEntity.ok().body(Arrays.stream(TransactionType.values()).map(TransactionType::getLiteral).toArray(String[]::new) );
    }

}
