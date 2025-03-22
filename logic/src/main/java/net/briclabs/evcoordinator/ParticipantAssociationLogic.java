package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.ParticipantAssociation;
import net.briclabs.evcoordinator.generated.tables.records.ParticipantAssociationRecord;
import org.jooq.DSLContext;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.ParticipantAssociation.PARTICIPANT_ASSOCIATION;

public class ParticipantAssociationLogic<P extends ParticipantAssociation> extends Logic<ParticipantAssociationRecord, ParticipantAssociation, net.briclabs.evcoordinator.generated.tables.ParticipantAssociation> implements WriteLogic<P> {
    public ParticipantAssociationLogic(DSLContext jooq) {
        super(jooq, ParticipantAssociation.class, PARTICIPANT_ASSOCIATION, PARTICIPANT_ASSOCIATION.ID);
    }

    @Override
    public boolean isAlreadyRecorded(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().SELF.getName(), Long.toString(pojo.getSelf())),
                entry(getTable().ASSOCIATE.getName(), Optional.ofNullable(pojo.getAssociate()).map(value -> Long.toString(value)).orElse("")),
                entry(getTable().RAW_ASSOCIATE_NAME.getName(), pojo.getRawAssociateName()),
                entry(getTable().ASSOCIATION.getName(), pojo.getAssociation()));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(getTable())
                .set(getTable().SELF, pojo.getSelf())
                .set(getTable().ASSOCIATE, pojo.getAssociate())
                .set(getTable().RAW_ASSOCIATE_NAME, pojo.getRawAssociateName())
                .set(getTable().ASSOCIATION, pojo.getAssociation())
                .returning(getIdColumn())
                .fetchOptional()
                .map(ParticipantAssociationRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq
                .update(getTable())
                .set(getTable().SELF, update.getSelf())
                .set(getTable().ASSOCIATE, update.getAssociate())
                .set(getTable().RAW_ASSOCIATE_NAME, update.getRawAssociateName())
                .set(getTable().ASSOCIATION, update.getAssociation())
                .where(getIdColumn().eq(update.getId()))
                .and(
                        getTable().SELF.notEqual(update.getSelf())
                    .or(getTable().ASSOCIATE.notEqual(update.getAssociate()))
                    .or(getTable().RAW_ASSOCIATE_NAME.notEqual(update.getRawAssociateName()))
                    .or(getTable().ASSOCIATION.notEqual(update.getAssociation()))
                ).execute();
    }
}