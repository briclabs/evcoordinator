package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Registration;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationRecord;
import org.jooq.DSLContext;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.Tables.REGISTRATION;

public class RegistrationLogic<P extends Registration> extends Logic<RegistrationRecord, Registration, net.briclabs.evcoordinator.generated.tables.Registration> implements WriteLogic<P> {

    public RegistrationLogic(DSLContext jooq) {
        super(jooq, Registration.class, REGISTRATION, REGISTRATION.ID);
    }

    @Override
    public boolean isAlreadyRecorded(P registrationPojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().EVENT_INFO_ID.getName(), Long.toString(registrationPojo.getEventInfoId())),
                entry(getTable().SIGNATURE.getName(), registrationPojo.getSignature()),
                entry(getTable().DONATION_PLEDGE.getName(), registrationPojo.getDonationPledge().toPlainString()),
                entry(getTable().PARTICIPANT_ID.getName(), Long.toString(registrationPojo.getParticipantId())));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(P registrationPojo) {
        return jooq
                .insertInto(getTable())
                .set(getTable().EVENT_INFO_ID, registrationPojo.getEventInfoId())
                .set(getTable().SIGNATURE, registrationPojo.getSignature())
                .set(getTable().DONATION_PLEDGE, registrationPojo.getDonationPledge())
                .set(getTable().PARTICIPANT_ID, registrationPojo.getParticipantId())
                .returning(getIdColumn())
                .fetchOptional()
                .map(RegistrationRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq
                .update(getTable())
                .set(getTable().EVENT_INFO_ID, update.getEventInfoId())
                .set(getTable().SIGNATURE, update.getSignature())
                .set(getTable().DONATION_PLEDGE, update.getDonationPledge())
                .set(getTable().PARTICIPANT_ID, update.getParticipantId())
                .where(getIdColumn().eq(update.getId()))
                .and(
                        getTable().EVENT_INFO_ID.notEqual(update.getEventInfoId())
                                .or(getTable().SIGNATURE.notEqual(update.getSignature()))
                                .or(getTable().DONATION_PLEDGE.notEqual(update.getDonationPledge()))
                                .or(getTable().PARTICIPANT_ID.notEqual(update.getParticipantId()))
                )
                .execute();
    }
}
