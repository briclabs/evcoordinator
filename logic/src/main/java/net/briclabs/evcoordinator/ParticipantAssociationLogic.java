package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.ParticipantAssociation;
import net.briclabs.evcoordinator.generated.tables.records.ParticipantAssociationRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.ParticipantAssociation.PARTICIPANT_ASSOCIATION;
import static net.briclabs.evcoordinator.generated.tables.PaymentInfo.PAYMENT_INFO;

public class ParticipantAssociationLogic<P extends ParticipantAssociation> extends Logic implements WriteLogic<P> {

    private static final Map<String, Field<?>> FIELDS = Map.ofEntries(
            Map.entry("ID", PARTICIPANT_ASSOCIATION.ID),
            Map.entry("SELF", PARTICIPANT_ASSOCIATION.SELF),
            Map.entry("ASSOCIATE", PARTICIPANT_ASSOCIATION.ASSOCIATE),
            Map.entry("ASSOCIATION", PARTICIPANT_ASSOCIATION.ASSOCIATION),
            Map.entry("TIME_RECORDED", PARTICIPANT_ASSOCIATION.TIME_RECORDED)
    );

    public ParticipantAssociationLogic(DSLContext jooq) {
        super(jooq);
    }

    static List<Condition> parseCriteriaIntoConditions(boolean exactCriteria, Map<String, String> searchCriteria) {
        stripOutUnknownFields(searchCriteria, PAYMENT_INFO);
        List<Condition> matchConditions = new ArrayList<>();
        searchCriteria.forEach((key, value) -> {
            addPossibleCondition(PARTICIPANT_ASSOCIATION.SELF, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(PARTICIPANT_ASSOCIATION.ASSOCIATE, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(PARTICIPANT_ASSOCIATION.ASSOCIATION, key, value, exactCriteria).ifPresent(matchConditions::add);
        });
        return matchConditions;
    }

    @Override
    public boolean validateIsTrulyNew(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(PARTICIPANT_ASSOCIATION.SELF.getName(), Long.toString(pojo.getSelf())),
                entry(PARTICIPANT_ASSOCIATION.ASSOCIATE.getName(), Long.toString(pojo.getAssociate())),
                entry(PARTICIPANT_ASSOCIATION.ASSOCIATION.getName(), pojo.getAssociation()));
        return fetchByCriteria(true, criteria, PARTICIPANT_ASSOCIATION.ID.getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<ParticipantAssociation> fetchById(Long id) {
        return jooq
                .selectFrom(PARTICIPANT_ASSOCIATION)
                .where(PARTICIPANT_ASSOCIATION.ID.eq(id))
                .fetchOptionalInto(ParticipantAssociation.class);
    }

    @Override
    public Field<?> resolveField(String columnName, Field<?> defaultField) {
        return FIELDS.getOrDefault(columnName, defaultField);
    }

    @Override
    public ListWithCount<ParticipantAssociation> fetchByCriteria(boolean exactCriteria, Map<String, String> searchCriteria, String sortColumn, Boolean sortAscending, int offset, int max) {
        List<Condition> conditions = parseCriteriaIntoConditions(exactCriteria, searchCriteria);

        List<ParticipantAssociation> results = jooq
                .selectFrom(PARTICIPANT_ASSOCIATION)
                .where(buildWhereClause(exactCriteria, conditions))
                .orderBy(sortAscending
                        ? resolveField(sortColumn, PARTICIPANT_ASSOCIATION.ID).asc()
                        : resolveField(sortColumn, PARTICIPANT_ASSOCIATION.ID).desc())
                .limit(offset, max)
                .fetchStreamInto(ParticipantAssociation.class)
                .toList();
        int count = jooq
                .selectCount()
                .from(PARTICIPANT_ASSOCIATION)
                .where(conditions)
                .fetchOptional(0, Integer.class)
                .orElse(0);
        return new ListWithCount<>(results, count);
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(PARTICIPANT_ASSOCIATION)
                .set(PARTICIPANT_ASSOCIATION.SELF, pojo.getSelf())
                .set(PARTICIPANT_ASSOCIATION.ASSOCIATE, pojo.getAssociate())
                .set(PARTICIPANT_ASSOCIATION.ASSOCIATION, pojo.getAssociation())
                .returning(PARTICIPANT_ASSOCIATION.ID)
                .fetchOptional()
                .map(ParticipantAssociationRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
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