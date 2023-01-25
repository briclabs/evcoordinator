package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.Tables;
import net.briclabs.evcoordinator.generated.tables.pojos.ParticipantAssociation;
import net.briclabs.evcoordinator.generated.tables.records.ParticipantAssociationRecord;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.tools.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.briclabs.evcoordinator.generated.tables.ParticipantAssociation.PARTICIPANT_ASSOCIATION;

public class ParticipantAssociationLogic extends Logic {
    public ParticipantAssociationLogic(DSLContext jooq) {
        super(jooq);
    }

    public Optional<ParticipantAssociationRecord> fetchById(Long id) {
        return jooq.selectFrom(PARTICIPANT_ASSOCIATION).where(PARTICIPANT_ASSOCIATION.ID.eq(id)).fetchOptional();
    }

    public List<ParticipantAssociationRecord> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max) {

        List<Condition> matchConditions = new ArrayList<>();

        // Strip out all map entries with keys not applicable to this table, and all map entries with blank values.
        searchCriteria.entrySet().stream()
                .filter(e -> StringUtils.isBlank(e.getValue().trim()) || !PARTICIPANT_ASSOCIATION.fieldStream().map(Field::getName).toList().contains(e.getKey()))
                .map(Map.Entry::getKey)
                .forEach(searchCriteria::remove);

        for (Map.Entry<String, String> searchParam : searchCriteria.entrySet()) {
            String key = searchParam.getKey();
            String value = searchParam.getValue();

            conditionallyAddMatchConditions(PARTICIPANT_ASSOCIATION.ID, key, Long.parseLong(value), matchConditions);
            conditionallyAddMatchConditions(PARTICIPANT_ASSOCIATION.SELF, key, Long.parseLong(value), matchConditions);
            conditionallyAddMatchConditions(PARTICIPANT_ASSOCIATION.ASSOCIATE, key, Long.parseLong(value), matchConditions);
            conditionallyAddMatchConditions(PARTICIPANT_ASSOCIATION.ASSOCIATION, key, value, matchConditions);
        }

        return jooq.selectFrom(PARTICIPANT_ASSOCIATION).where(matchConditions).orderBy(PARTICIPANT_ASSOCIATION.ID).limit(offset, max).stream().toList();
    }

    public Optional<Long> insertNew(ParticipantAssociation pojo) {
        return jooq.insertInto(PARTICIPANT_ASSOCIATION)
                .set(PARTICIPANT_ASSOCIATION.SELF, pojo.getSelf())
                .set(PARTICIPANT_ASSOCIATION.ASSOCIATE, pojo.getAssociate())
                .set(PARTICIPANT_ASSOCIATION.ASSOCIATION, pojo.getAssociation())
                .set(PARTICIPANT_ASSOCIATION.TIME_RECORDED, Logic.getNow())
                .returning(PARTICIPANT_ASSOCIATION.ID)
                .fetchOptional()
                .map(ParticipantAssociationRecord::getId);
    }

    public Long updateExisting(ParticipantAssociationRecord update, ParticipantAssociationRecord existing) {
        if (!isUnchanged(update, existing)) {
            jooq.update(PARTICIPANT_ASSOCIATION)
                    .set(PARTICIPANT_ASSOCIATION.SELF, update.getSelf())
                    .set(PARTICIPANT_ASSOCIATION.ASSOCIATE, update.getAssociate())
                    .set(PARTICIPANT_ASSOCIATION.ASSOCIATION, update.getAssociation())
                    .set(PARTICIPANT_ASSOCIATION.TIME_RECORDED, Logic.getNow())
                    .where(PARTICIPANT_ASSOCIATION.ID.eq(existing.getId()))
                    .execute();
        }
        return update.getId();
    }

    public Optional<Long> isUpdateRedundant(Long id, ParticipantAssociation update) {
        List<ParticipantAssociationRecord> matchingEvents = fetchByCriteria(getCriteriaFromPojo(update), 0, 1);

        if (!matchingEvents.isEmpty() && matchingEvents.get(0).get(Tables.PARTICIPANT_ASSOCIATION.ID).equals(id)) {
            // No change needed; just return the ID.
            return Optional.of(id);
        }

        if (!matchingEvents.isEmpty()) {
            // An update would create a record with the same mutable field values.
            return Optional.of(matchingEvents.get(0).get(Tables.PARTICIPANT_ASSOCIATION.ID));
        }
        return Optional.empty();
    }

    public boolean isUnchanged(ParticipantAssociationRecord update, ParticipantAssociationRecord existing) {
        return new EqualsBuilder()
                .append(update.getSelf(), existing.getSelf())
                .append(update.getAssociate(), existing.getAssociate())
                .append(update.getAssociation(), existing.getAssociation())
                .build();
    }

    public boolean isEntryAlreadyExists(ParticipantAssociation pojo) {
        return fetchByCriteria(getCriteriaFromPojo(pojo), 0, 1).size() > 0;
    }

    public Map<String, String> getCriteriaFromPojo(ParticipantAssociation pojo) {
        return Map.of(
                PARTICIPANT_ASSOCIATION.SELF.getName(), Long.toString(pojo.getSelf()),
                PARTICIPANT_ASSOCIATION.ASSOCIATE.getName(), Long.toString(pojo.getAssociate()),
                PARTICIPANT_ASSOCIATION.ASSOCIATION.getName(), pojo.getAssociation());
    }
}