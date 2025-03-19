package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Event;
import net.briclabs.evcoordinator.generated.tables.records.EventRecord;
import org.jooq.DSLContext;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Event.EVENT;

public class EventLogic<P extends Event> extends Logic<EventRecord, Event, net.briclabs.evcoordinator.generated.tables.Event> implements WriteLogic<P> {
    public EventLogic(DSLContext jooq) {
        super(jooq, Event.class, EVENT, EVENT.ID);
    }

    @Override
    public boolean validateIsTrulyNew(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().EVENT_INFO_ID.getName(), Long.toString(pojo.getEventInfoId())),
                entry(getTable().ACTION_TYPE.getName(), pojo.getActionType()),
                entry(getTable().SIGNATURE.getName(), pojo.getActionType()),
                entry(getTable().DONATION_PLEDGE.getName(), pojo.getDonationPledge().toPlainString()),
                entry(getTable().PARTICIPANT_ID.getName(), Long.toString(pojo.getParticipantId())));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(getTable())
                .set(getTable().EVENT_INFO_ID, pojo.getEventInfoId())
                .set(getTable().ACTION_TYPE, pojo.getActionType())
                .set(getTable().SIGNATURE, pojo.getActionType())
                .set(getTable().DONATION_PLEDGE, pojo.getDonationPledge())
                .set(getTable().PARTICIPANT_ID, pojo.getParticipantId())
                .returning(getIdColumn())
                .fetchOptional()
                .map(EventRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq
                .update(getTable())
                .set(getTable().EVENT_INFO_ID, update.getEventInfoId())
                .set(getTable().ACTION_TYPE, update.getActionType())
                .set(getTable().SIGNATURE, update.getActionType())
                .set(getTable().DONATION_PLEDGE, update.getDonationPledge())
                .set(getTable().PARTICIPANT_ID, update.getParticipantId())
                .where(getIdColumn().eq(update.getId()))
                .and(
                        getTable().EVENT_INFO_ID.notEqual(update.getEventInfoId())
                    .or(getTable().ACTION_TYPE.notEqual(update.getActionType()))
                    .or(getTable().SIGNATURE.notEqual(update.getActionType()))
                    .or(getTable().DONATION_PLEDGE.notEqual(update.getDonationPledge()))
                    .or(getTable().PARTICIPANT_ID.notEqual(update.getParticipantId()))
                )
                .execute();
    }
}
