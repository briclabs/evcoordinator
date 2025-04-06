package net.briclabs.evcoordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import net.briclabs.evcoordinator.generated.tables.records.ParticipantRecord;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.tools.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Participant.PARTICIPANT;

/**
 * ParticipantLogic is a class that provides logic and database interaction
 * for handling {@code Participant} entities. This class extends the generic
 * {@code Logic} class to implement operations specific to Participant records
 * in the database and adheres to the {@code WriteLogic} interface.
 *
 * @param <P> The Participant type parameter extending the {@code Participant} class.
 *
 * @implNote The Participant object in the data model cannot be deleted because of the impact this would have to the History table.
 */
public class ParticipantLogic<P extends Participant> extends Logic<ParticipantRecord, Participant, net.briclabs.evcoordinator.generated.tables.Participant> implements WriteLogic<P> {
    private final HistoryLogic<DataHistory> historyLogic;

    public ParticipantLogic(ObjectMapper objectMapper, DSLContext jooq) {
        super(objectMapper, jooq, Participant.class, PARTICIPANT, PARTICIPANT.ID);
        this.historyLogic = new HistoryLogic<>(objectMapper, this, jooq);
    }

    /**
     * Fetches preexisting attendees based on their first name, last name, and email address.
     * Keying off of first and last name in addition to email address provides a bit more security
     * against automated attempts to guess whether someone is a participant.
     *
     * @param nameFirst The first name of the attendee to search for.
     * @param nameLast The last name of the attendee to search for.
     * @param addrEmail The email address of the attendee to search for.
     * @return A list with count containing participants that match the provided criteria.
     */
    public ListWithCount<Participant> fetchAttendeeByNameAndEmail(String nameFirst, String nameLast, String addrEmail) {
        return fetchByCriteria(
                true,
                Map.of(net.briclabs.evcoordinator.generated.tables.Participant.PARTICIPANT.PARTICIPANT_TYPE.getName(), "ATTENDEE",
                        net.briclabs.evcoordinator.generated.tables.Participant.PARTICIPANT.NAME_FIRST.getName(), nameFirst,
                        net.briclabs.evcoordinator.generated.tables.Participant.PARTICIPANT.NAME_LAST.getName(), nameLast,
                        net.briclabs.evcoordinator.generated.tables.Participant.PARTICIPANT.ADDR_EMAIL.getName(), addrEmail),
                null,
                false,
                0,
                1);
    }

    /**
     * Fetches preexisting participants based on their email address.
     *
     * @param addrEmail The email address of the attendee to search for.
     * @return A list with count containing participants that match the provided criteria.
     */
    public ListWithCount<Participant> fetchParticipantByEmail(String addrEmail) {
        return fetchByCriteria(
                true,
                Map.of(net.briclabs.evcoordinator.generated.tables.Participant.PARTICIPANT.ADDR_EMAIL.getName(), addrEmail),
                null,
                false,
                0,
                1);
    }

    /**
     * Checks if the email address / participant type combo is already recorded in the database.
     *
     * @param emailAddressToSearchFor The email address to check for existence.
     * @param participantTypeToSearchFor The participant type to check for existence.
     * @return true if the email address / participant type combo is already recorded, false otherwise.
     */
    public boolean isEmailAddressAlreadyRecorded(String emailAddressToSearchFor, String participantTypeToSearchFor) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().ADDR_EMAIL.getName(), emailAddressToSearchFor),
                entry(getTable().PARTICIPANT_TYPE.getName(), participantTypeToSearchFor)
        );
        return fetchByCriteria(false, criteria, getIdColumn().getName(), false, 0, 1).count() > 0;
    }

    @Override
    public boolean isAlreadyRecorded(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().PARTICIPANT_TYPE.getName(), pojo.getParticipantType()),
                entry(getTable().SPONSOR.getName(), pojo.getParticipantType()),
                entry(getTable().NAME_FIRST.getName(), pojo.getNameFirst()),
                entry(getTable().NAME_LAST.getName(), pojo.getNameLast()),
                entry(getTable().NAME_NICK.getName(), StringUtils.defaultString(pojo.getNameNick(), "")),
                entry(getTable().DOB.getName(), pojo.getDob().format(DateTimeFormatter.ISO_DATE)),
                entry(getTable().ADDR_STREET_1.getName(), pojo.getAddrStreet_1()),
                entry(getTable().ADDR_STREET_2.getName(), StringUtils.defaultString(pojo.getAddrStreet_2(), "")),
                entry(getTable().ADDR_CITY.getName(), pojo.getAddrCity()),
                entry(getTable().ADDR_STATE_ABBR.getName(), pojo.getAddrStateAbbr()),
                entry(getTable().ADDR_ZIP.getName(), pojo.getAddrZip().toString()),
                entry(getTable().ADDR_EMAIL.getName(), pojo.getAddrEmail()),
                entry(getTable().NAME_EMERGENCY.getName(), pojo.getNameEmergency()),
                entry(getTable().PHONE_EMERGENCY.getName(), pojo.getPhoneEmergency().toString()),
                entry(getTable().EMERGENCY_CONTACT_RELATIONSHIP_TYPE.getName(), pojo.getEmergencyContactRelationshipType()),
                entry(getTable().PHONE_DIGITS.getName(), pojo.getPhoneDigits().toString()));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false, 0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(long actorId, P pojo) {
        Optional<Long> insertedId = jooq
                .insertInto(getTable())
                .set(getTable().PARTICIPANT_TYPE, pojo.getParticipantType())
                .set(getTable().SPONSOR, pojo.getSponsor())
                .set(getTable().NAME_FIRST, pojo.getNameFirst())
                .set(getTable().NAME_LAST, pojo.getNameLast())
                .set(getTable().NAME_NICK, pojo.getNameNick())
                .set(getTable().DOB, pojo.getDob())
                .set(getTable().ADDR_STREET_1, pojo.getAddrStreet_1())
                .set(getTable().ADDR_STREET_2, pojo.getAddrStreet_2())
                .set(getTable().ADDR_CITY, pojo.getAddrCity())
                .set(getTable().ADDR_STATE_ABBR, pojo.getAddrStateAbbr())
                .set(getTable().ADDR_ZIP, pojo.getAddrZip())
                .set(getTable().ADDR_EMAIL, pojo.getAddrEmail())
                .set(getTable().NAME_EMERGENCY, pojo.getNameEmergency())
                .set(getTable().PHONE_EMERGENCY, pojo.getPhoneEmergency())
                .set(getTable().EMERGENCY_CONTACT_RELATIONSHIP_TYPE, pojo.getEmergencyContactRelationshipType())
                .set(getTable().PHONE_DIGITS, pojo.getPhoneDigits())
                .returning(getIdColumn())
                .fetchOptional()
                .map(ParticipantRecord::getId);
        if (insertedId.isPresent()) {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    HistoryLogic.ActionType.INSERTED.name(),
                    getTable().getName(),
                    convertToJson(pojo),
                    JSON.json("{}"),
                    null
            ));
        }
        return insertedId;
    }

    @Override
    public int updateExisting(long actorId, P update) {
        var originalRecord = fetchById(update.getId()).orElseThrow(() -> new RuntimeException("Participant not found: " + update.getId()));
        int updatedRecords = jooq.update(getTable())
                .set(getTable().PARTICIPANT_TYPE, update.getParticipantType())
                .set(getTable().SPONSOR, update.getSponsor())
                .set(getTable().NAME_FIRST, update.getNameFirst())
                .set(getTable().NAME_LAST, update.getNameLast())
                .set(getTable().NAME_NICK, update.getNameNick())
                .set(getTable().DOB, update.getDob())
                .set(getTable().ADDR_STREET_1, update.getAddrStreet_1())
                .set(getTable().ADDR_STREET_2, update.getAddrStreet_2())
                .set(getTable().ADDR_CITY, update.getAddrCity())
                .set(getTable().ADDR_STATE_ABBR, update.getAddrStateAbbr())
                .set(getTable().ADDR_ZIP, update.getAddrZip())
                .set(getTable().ADDR_EMAIL, update.getAddrEmail())
                .set(getTable().NAME_EMERGENCY, update.getNameEmergency())
                .set(getTable().PHONE_EMERGENCY, update.getPhoneEmergency())
                .set(getTable().EMERGENCY_CONTACT_RELATIONSHIP_TYPE, update.getEmergencyContactRelationshipType())
                .set(getTable().PHONE_DIGITS, update.getPhoneDigits())
                .where(getIdColumn().eq(update.getId()))
                .and(
                        getTable().PARTICIPANT_TYPE.notEqual(update.getParticipantType())
                                .or(getTable().SPONSOR.notEqual(update.getSponsor()))
                                .or(getTable().NAME_FIRST.notEqual(update.getNameFirst()))
                                .or(getTable().NAME_LAST.notEqual(update.getNameLast()))
                                .or(getTable().NAME_NICK.notEqual(update.getNameNick()))
                                .or(getTable().DOB.notEqual(update.getDob()))
                                .or(getTable().ADDR_STREET_1.notEqual(update.getAddrStreet_1()))
                                .or(getTable().ADDR_STREET_2.notEqual(update.getAddrStreet_2()))
                                .or(getTable().ADDR_CITY.notEqual(update.getAddrCity()))
                                .or(getTable().ADDR_STATE_ABBR.notEqual(update.getAddrStateAbbr()))
                                .or(getTable().ADDR_ZIP.notEqual(update.getAddrZip()))
                                .or(getTable().ADDR_EMAIL.notEqual(update.getAddrEmail()))
                                .or(getTable().NAME_EMERGENCY.notEqual(update.getNameEmergency()))
                                .or(getTable().PHONE_EMERGENCY.notEqual(update.getPhoneEmergency()))
                                .or(getTable().EMERGENCY_CONTACT_RELATIONSHIP_TYPE.notEqual(update.getEmergencyContactRelationshipType()))
                                .or(getTable().PHONE_DIGITS.notEqual(update.getPhoneDigits()))
                ).execute();
        if (updatedRecords > 0) {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    HistoryLogic.ActionType.UPDATED.name(),
                    getTable().getName(),
                    convertToJson(update),
                    convertToJson(originalRecord),
                    null
            ));
        }
        return updatedRecords;
    }
}
