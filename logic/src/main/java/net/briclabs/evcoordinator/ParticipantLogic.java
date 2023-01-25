package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.Tables;
import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import net.briclabs.evcoordinator.generated.tables.records.ParticipantRecord;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.tools.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Participant.PARTICIPANT;

public class ParticipantLogic extends Logic {
    public ParticipantLogic(DSLContext jooq) {
        super(jooq);
    }

    public Optional<ParticipantRecord> fetchById(Long id) {
        return jooq.selectFrom(PARTICIPANT).where(PARTICIPANT.ID.eq(id)).fetchOptional();
    }

    public List<ParticipantRecord> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max) {

        List<Condition> matchConditions = new ArrayList<>();

        // Strip out all map entries with keys not applicable to this table, and all map entries with blank values.
        searchCriteria.entrySet().stream()
                .filter(e -> StringUtils.isBlank(e.getValue().trim()) || !PARTICIPANT.fieldStream().map(Field::getName).toList().contains(e.getKey()))
                .map(Map.Entry::getKey)
                .forEach(searchCriteria::remove);

        for (Map.Entry<String, String> searchParam : searchCriteria.entrySet()) {
            String key = searchParam.getKey();
            String value = searchParam.getValue();

            conditionallyAddMatchConditions(PARTICIPANT.ID, key, Long.parseLong(value), matchConditions);
            conditionallyAddMatchConditions(PARTICIPANT.PARTICIPANT_TYPE, key, value, matchConditions);
            conditionallyAddMatchConditions(PARTICIPANT.NAME_FIRST, key, value, matchConditions);
            conditionallyAddMatchConditions(PARTICIPANT.NAME_LAST, key, value, matchConditions);
            conditionallyAddMatchConditions(PARTICIPANT.DOB, key, LocalDate.parse(value), matchConditions);
            conditionallyAddMatchConditions(PARTICIPANT.ADDR_STREET_1, key, value, matchConditions);
            conditionallyAddMatchConditions(PARTICIPANT.ADDR_STREET_2, key, value, matchConditions);
            conditionallyAddMatchConditions(PARTICIPANT.ADDR_CITY, key, value, matchConditions);
            conditionallyAddMatchConditions(PARTICIPANT.ADDR_STATE_ABBR, key, value, matchConditions);
            conditionallyAddMatchConditions(PARTICIPANT.ADDR_ZIP, key, Integer.parseInt(value), matchConditions);
            conditionallyAddMatchConditions(PARTICIPANT.ADDR_EMAIL, key, value, matchConditions);
            conditionallyAddMatchConditions(PARTICIPANT.PHONE_DIGITS, key, Integer.parseInt(value), matchConditions);
        }

        return jooq.selectFrom(PARTICIPANT).where(matchConditions).orderBy(PARTICIPANT.ID).limit(offset, max).stream().toList();
    }

    public Optional<Long> insertNew(Participant pojo) {
        return jooq.insertInto(PARTICIPANT)
                .set(PARTICIPANT.PARTICIPANT_TYPE, pojo.getParticipantType())
                .set(PARTICIPANT.NAME_FIRST, pojo.getNameFirst())
                .set(PARTICIPANT.NAME_LAST, pojo.getNameLast())
                .set(PARTICIPANT.DOB, pojo.getDob())
                .set(PARTICIPANT.ADDR_STREET_1, pojo.getAddrStreet_1())
                .set(PARTICIPANT.ADDR_STREET_2, pojo.getAddrStreet_2())
                .set(PARTICIPANT.ADDR_CITY, pojo.getAddrCity())
                .set(PARTICIPANT.ADDR_STATE_ABBR, pojo.getAddrStateAbbr())
                .set(PARTICIPANT.ADDR_ZIP, pojo.getAddrZip())
                .set(PARTICIPANT.ADDR_EMAIL, pojo.getAddrEmail())
                .set(PARTICIPANT.PHONE_DIGITS, pojo.getPhoneDigits())
                .set(PARTICIPANT.TIME_RECORDED, Logic.getNow())
                .returning(PARTICIPANT.ID)
                .fetchOptional()
                .map(ParticipantRecord::getId);
    }

    public Long updateExisting(ParticipantRecord update, ParticipantRecord existing) {
        if (!isUnchanged(update, existing)) {
            jooq.update(PARTICIPANT)
                    .set(PARTICIPANT.PARTICIPANT_TYPE, update.getParticipantType())
                    .set(PARTICIPANT.NAME_FIRST, update.getNameFirst())
                    .set(PARTICIPANT.NAME_LAST, update.getNameLast())
                    .set(PARTICIPANT.DOB, update.getDob())
                    .set(PARTICIPANT.ADDR_STREET_1, update.getAddrStreet_1())
                    .set(PARTICIPANT.ADDR_STREET_2, update.getAddrStreet_2())
                    .set(PARTICIPANT.ADDR_CITY, update.getAddrCity())
                    .set(PARTICIPANT.ADDR_STATE_ABBR, update.getAddrStateAbbr())
                    .set(PARTICIPANT.ADDR_ZIP, update.getAddrZip())
                    .set(PARTICIPANT.ADDR_EMAIL, update.getAddrEmail())
                    .set(PARTICIPANT.PHONE_DIGITS, update.getPhoneDigits())
                    .set(PARTICIPANT.TIME_RECORDED, Logic.getNow())
                    .where(PARTICIPANT.ID.eq(existing.getId()))
                    .execute();
        }
        return update.getId();
    }

    public Optional<Long> isUpdateRedundant(Long id, Participant update) {
        List<ParticipantRecord> matchingEvents = fetchByCriteria(getCriteriaFromPojo(update), 0, 1);

        if (!matchingEvents.isEmpty() && matchingEvents.get(0).get(Tables.PARTICIPANT.ID).equals(id)) {
            // No change needed; just return the ID.
            return Optional.of(id);
        }

        if (!matchingEvents.isEmpty()) {
            // An update would create a record with the same mutable field values.
            return Optional.of(matchingEvents.get(0).get(Tables.PARTICIPANT.ID));
        }
        return Optional.empty();
    }

    public boolean isUnchanged(ParticipantRecord update, ParticipantRecord existing) {
        return new EqualsBuilder()
                .append(update.getParticipantType(), existing.getParticipantType())
                .append(update.getNameFirst(), existing.getNameFirst())
                .append(update.getNameLast(), existing.getNameLast())
                .append(update.getDob(), existing.getDob())
                .append(update.getAddrStreet_1(), existing.getAddrStreet_1())
                .append(update.getAddrStreet_2(), existing.getAddrStreet_2())
                .append(update.getAddrCity(), existing.getAddrCity())
                .append(update.getAddrStateAbbr(), existing.getAddrStateAbbr())
                .append(update.getAddrZip(), existing.getAddrZip())
                .append(update.getAddrEmail(), existing.getAddrEmail())
                .append(update.getPhoneDigits(), existing.getPhoneDigits())
                .build();
    }

    public boolean isEntryAlreadyExists(Participant pojo) {
        return fetchByCriteria(getCriteriaFromPojo(pojo), 0, 1).size() > 0;
    }

    public Map<String, String> getCriteriaFromPojo(Participant pojo) {
        return Map.ofEntries(
                entry(PARTICIPANT.PARTICIPANT_TYPE.getName(), pojo.getParticipantType()),
                entry(PARTICIPANT.NAME_FIRST.getName(), pojo.getNameFirst()),
                entry(PARTICIPANT.NAME_LAST.getName(), pojo.getNameLast()),
                entry(PARTICIPANT.DOB.getName(), pojo.getDob().format(DateTimeFormatter.ISO_DATE)),
                entry(PARTICIPANT.ADDR_STREET_1.getName(), pojo.getAddrStreet_1()),
                entry(PARTICIPANT.ADDR_STREET_2.getName(), StringUtils.defaultString(pojo.getAddrStreet_2(), "")),
                entry(PARTICIPANT.ADDR_CITY.getName(), pojo.getAddrCity()),
                entry(PARTICIPANT.ADDR_STATE_ABBR.getName(), pojo.getAddrStateAbbr()),
                entry(PARTICIPANT.ADDR_ZIP.getName(), pojo.getAddrZip().toString()),
                entry(PARTICIPANT.ADDR_EMAIL.getName(), pojo.getAddrEmail()),
                entry(PARTICIPANT.PHONE_DIGITS.getName(), pojo.getPhoneDigits().toString()));
    }
}
