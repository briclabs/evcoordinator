package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import net.briclabs.evcoordinator.generated.tables.records.ParticipantRecord;
import org.jooq.DSLContext;
import org.jooq.tools.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Participant.PARTICIPANT;

public class ParticipantLogic<P extends Participant> extends Logic<ParticipantRecord, Participant, net.briclabs.evcoordinator.generated.tables.Participant> implements WriteLogic<P> {
    public ParticipantLogic(DSLContext jooq) {
        super(jooq, Participant.class, PARTICIPANT, PARTICIPANT.ID);
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
                entry(getTable().PHONE_DIGITS.getName(), pojo.getPhoneDigits().toString()));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false, 0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
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
                .set(getTable().PHONE_DIGITS, pojo.getPhoneDigits())
                .returning(getIdColumn())
                .fetchOptional()
                .map(ParticipantRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq.update(getTable())
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
                        .or(getTable().PHONE_DIGITS.notEqual(update.getPhoneDigits()))
                ).execute();
    }
}
