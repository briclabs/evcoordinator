package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.ParticipantAssociation;
import net.briclabs.evcoordinator.generated.tables.records.ParticipantAssociationRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.ParticipantAssociation.PARTICIPANT_ASSOCIATION;
import static net.briclabs.evcoordinator.generated.tables.PaymentInfo.PAYMENT_INFO;

public class ParticipantAssociationLogic extends Logic {

    static List<Condition> parseCriteriaIntoConditions(Map<String, String> searchCriteria) {
        stripOutUnknownFields(searchCriteria, PAYMENT_INFO);
        List<Condition> matchConditions = new ArrayList<>();
        searchCriteria.forEach((key, value) -> {
            buildPossibleCondition(PARTICIPANT_ASSOCIATION.ID, key, Long.parseLong(value)).ifPresent(matchConditions::add);
            buildPossibleCondition(PARTICIPANT_ASSOCIATION.SELF, key, Long.parseLong(value)).ifPresent(matchConditions::add);
            buildPossibleCondition(PARTICIPANT_ASSOCIATION.ASSOCIATE, key, Long.parseLong(value)).ifPresent(matchConditions::add);
            buildPossibleCondition(PARTICIPANT_ASSOCIATION.ASSOCIATION, key, value).ifPresent(matchConditions::add);
        });
        return matchConditions;
    }

    public ParticipantAssociationLogic(DSLContext jooq) {
        super(jooq);
    }

    public boolean validateIsTrulyNew(ParticipantAssociation pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(PARTICIPANT_ASSOCIATION.SELF.getName(), Long.toString(pojo.getSelf())),
                entry(PARTICIPANT_ASSOCIATION.ASSOCIATE.getName(), Long.toString(pojo.getAssociate())),
                entry(PARTICIPANT_ASSOCIATION.ASSOCIATION.getName(), pojo.getAssociation()));
        return fetchByCriteria(criteria, 0, 1).size() > 0;
    }

    public Optional<ParticipantAssociation> fetchById(Long id) {
        return jooq
                .selectFrom(PARTICIPANT_ASSOCIATION)
                .where(PARTICIPANT_ASSOCIATION.ID.eq(id))
                .fetchOptionalInto(ParticipantAssociation.class);
    }

    public List<ParticipantAssociation> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max) {
        return jooq
                .selectFrom(PARTICIPANT_ASSOCIATION)
                .where(parseCriteriaIntoConditions(searchCriteria))
                .orderBy(PARTICIPANT_ASSOCIATION.ID)
                .limit(offset, max)
                .fetchStreamInto(ParticipantAssociation.class)
                .toList();
    }

    public Optional<Long> insertNew(ParticipantAssociation pojo) {
        return jooq
                .insertInto(PARTICIPANT_ASSOCIATION)
                .set(PARTICIPANT_ASSOCIATION.SELF, pojo.getSelf())
                .set(PARTICIPANT_ASSOCIATION.ASSOCIATE, pojo.getAssociate())
                .set(PARTICIPANT_ASSOCIATION.ASSOCIATION, pojo.getAssociation())
                .returning(PARTICIPANT_ASSOCIATION.ID)
                .fetchOptional()
                .map(ParticipantAssociationRecord::getId);
    }

    public int updateExisting(ParticipantAssociation update) {
        return jooq
                .update(PARTICIPANT_ASSOCIATION)
                .set(PARTICIPANT_ASSOCIATION.SELF, update.getSelf())
                .set(PARTICIPANT_ASSOCIATION.ASSOCIATE, update.getAssociate())
                .set(PARTICIPANT_ASSOCIATION.ASSOCIATION, update.getAssociation())
                .where(PARTICIPANT_ASSOCIATION.ID.eq(update.getId()))
                .and(
                    PARTICIPANT_ASSOCIATION.SELF.notEqual(update.getSelf())
                    .or(PARTICIPANT_ASSOCIATION.ASSOCIATE.notEqual(update.getAssociate()))
                    .or(PARTICIPANT_ASSOCIATION.ASSOCIATION.notEqual(update.getAssociation()))
                ).execute();
    }
}