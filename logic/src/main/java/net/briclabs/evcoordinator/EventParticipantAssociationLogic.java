package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.EventParticipantAssociation;
import net.briclabs.evcoordinator.generated.tables.records.EventParticipantAssociationRecord;
import org.jooq.DSLContext;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.EventParticipantAssociation.EVENT_PARTICIPANT_ASSOCIATION;

public class EventParticipantAssociationLogic<P extends EventParticipantAssociation> extends Logic<EventParticipantAssociationRecord, EventParticipantAssociation, net.briclabs.evcoordinator.generated.tables.EventParticipantAssociation> implements WriteLogic<P> {
    public EventParticipantAssociationLogic(DSLContext jooq) {
        super(jooq, EventParticipantAssociation.class, EVENT_PARTICIPANT_ASSOCIATION, EVENT_PARTICIPANT_ASSOCIATION.ID);
    }

    @Override
    public boolean validateIsTrulyNew(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().EVENT_ID.getName(), Long.toString(pojo.getEventId())),
                entry(getTable().PARTICIPANT_ASSOCIATION_ID.getName(), Long.toString(pojo.getParticipantAssociationId())));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(getTable())
                .set(getTable().EVENT_ID, pojo.getEventId())
                .set(getTable().PARTICIPANT_ASSOCIATION_ID, pojo.getParticipantAssociationId())
                .returning(getIdColumn())
                .fetchOptional()
                .map(EventParticipantAssociationRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq
                .update(getTable())
                .set(getTable().EVENT_ID, update.getEventId())
                .set(getTable().PARTICIPANT_ASSOCIATION_ID, update.getParticipantAssociationId())
                .where(getIdColumn().eq(update.getId()))
                .and(
                        getTable().EVENT_ID.notEqual(update.getEventId())
                    .or(getTable().PARTICIPANT_ASSOCIATION_ID.notEqual(update.getParticipantAssociationId()))
                ).execute();
    }
}