package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import net.briclabs.evcoordinator.generated.tables.records.ParticipantRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.tools.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Participant.PARTICIPANT;

public class ParticipantLogic<P extends Participant> extends Logic implements WriteLogic<P> {

    static List<Condition> parseCriteriaIntoConditions(Map<String, String> searchCriteria) {
        stripOutUnknownFields(searchCriteria, PARTICIPANT);
        List<Condition> matchConditions = new ArrayList<>();
        searchCriteria.forEach((key, value) -> {
            addPossibleCondition(PARTICIPANT.ID, key, value);
            addPossibleCondition(PARTICIPANT.PARTICIPANT_TYPE, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(PARTICIPANT.NAME_FIRST, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(PARTICIPANT.NAME_LAST, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(PARTICIPANT.DOB, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(PARTICIPANT.ADDR_STREET_1, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(PARTICIPANT.ADDR_STREET_2, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(PARTICIPANT.ADDR_CITY, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(PARTICIPANT.ADDR_STATE_ABBR, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(PARTICIPANT.ADDR_ZIP, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(PARTICIPANT.ADDR_EMAIL, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(PARTICIPANT.PHONE_DIGITS, key, value).ifPresent(matchConditions::add);
        });
        return matchConditions;
    }

    public ParticipantLogic(DSLContext jooq) {
        super(jooq);
    }

    @Override
    public boolean validateIsTrulyNew(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
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
        return fetchByCriteria(criteria, 0, 1).size() > 0;
    }

    @Override
    public Optional<Participant> fetchById(Long id) {
        return jooq
                .selectFrom(PARTICIPANT)
                .where(PARTICIPANT.ID.eq(id))
                .fetchOptionalInto(Participant.class);
    }

    @Override
    public List<Participant> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max) {
        return jooq
                .selectFrom(PARTICIPANT)
                .where(parseCriteriaIntoConditions(searchCriteria))
                .orderBy(PARTICIPANT.ID)
                .limit(offset, max)
                .fetchStreamInto(Participant.class)
                .toList();
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(PARTICIPANT)
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
                .returning(PARTICIPANT.ID)
                .fetchOptional()
                .map(ParticipantRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq.update(PARTICIPANT)
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
                .where(PARTICIPANT.ID.eq(update.getId()))
                .and(
                        PARTICIPANT.PARTICIPANT_TYPE.notEqual(update.getParticipantType())
                        .or(PARTICIPANT.NAME_FIRST.notEqual(update.getNameFirst()))
                        .or(PARTICIPANT.NAME_LAST.notEqual(update.getNameLast()))
                        .or(PARTICIPANT.DOB.notEqual(update.getDob()))
                        .or(PARTICIPANT.ADDR_STREET_1.notEqual(update.getAddrStreet_1()))
                        .or(PARTICIPANT.ADDR_STREET_2.notEqual(update.getAddrStreet_2()))
                        .or(PARTICIPANT.ADDR_CITY.notEqual(update.getAddrCity()))
                        .or(PARTICIPANT.ADDR_STATE_ABBR.notEqual(update.getAddrStateAbbr()))
                        .or(PARTICIPANT.ADDR_ZIP.notEqual(update.getAddrZip()))
                        .or(PARTICIPANT.ADDR_EMAIL.notEqual(update.getAddrEmail()))
                        .or(PARTICIPANT.PHONE_DIGITS.notEqual(update.getPhoneDigits()))
                ).execute();
    }
}
