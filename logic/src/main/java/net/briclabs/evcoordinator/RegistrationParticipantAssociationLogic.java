package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.RegistrationParticipantAssociation;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationParticipantAssociationRecord;
import org.jooq.DSLContext;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.Tables.REGISTRATION_PARTICIPANT_ASSOCIATION;

public class RegistrationParticipantAssociationLogic<P extends RegistrationParticipantAssociation> extends Logic<RegistrationParticipantAssociationRecord, RegistrationParticipantAssociation, net.briclabs.evcoordinator.generated.tables.RegistrationParticipantAssociation> implements WriteLogic<P> {
    public RegistrationParticipantAssociationLogic(DSLContext jooq) {
        super(jooq, RegistrationParticipantAssociation.class, REGISTRATION_PARTICIPANT_ASSOCIATION, REGISTRATION_PARTICIPANT_ASSOCIATION.ID);
    }

    @Override
    public boolean isAlreadyRecorded(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().REGISTRATION_ID.getName(), Long.toString(pojo.getRegistrationId())),
                entry(getTable().PARTICIPANT_ASSOCIATION_ID.getName(), Long.toString(pojo.getParticipantAssociationId())));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(getTable())
                .set(getTable().REGISTRATION_ID, pojo.getRegistrationId())
                .set(getTable().PARTICIPANT_ASSOCIATION_ID, pojo.getParticipantAssociationId())
                .returning(getIdColumn())
                .fetchOptional()
                .map(RegistrationParticipantAssociationRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq
                .update(getTable())
                .set(getTable().REGISTRATION_ID, update.getRegistrationId())
                .set(getTable().PARTICIPANT_ASSOCIATION_ID, update.getParticipantAssociationId())
                .where(getIdColumn().eq(update.getId()))
                .and(
                        getTable().REGISTRATION_ID.notEqual(update.getRegistrationId())
                    .or(getTable().PARTICIPANT_ASSOCIATION_ID.notEqual(update.getParticipantAssociationId()))
                ).execute();
    }
}