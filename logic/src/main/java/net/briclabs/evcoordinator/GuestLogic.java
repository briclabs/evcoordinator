package net.briclabs.evcoordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import net.briclabs.evcoordinator.generated.tables.pojos.Guest;
import net.briclabs.evcoordinator.generated.tables.pojos.GuestWithLabels;
import net.briclabs.evcoordinator.generated.tables.records.GuestRecord;
import net.briclabs.evcoordinator.generated.tables.records.GuestWithLabelsRecord;
import net.briclabs.evcoordinator.validation.GuestValidator;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.impl.DSL;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Guest.GUEST;
import static net.briclabs.evcoordinator.generated.tables.GuestWithLabels.GUEST_WITH_LABELS;

public class GuestLogic extends WriteAndDeleteLogic<GuestRecord, Guest, net.briclabs.evcoordinator.generated.tables.Guest> {
    private final HistoryLogic historyLogic;

    public GuestLogic(ObjectMapper objectMapper, DSLContext jooq) {
        super(objectMapper, jooq, Guest.class, GUEST, GUEST.ID);
        this.historyLogic = new HistoryLogic(objectMapper, jooq);
    }

    @Override
    public boolean isAlreadyRecorded(Guest pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().REGISTRATION_ID.getName(), Long.toString(pojo.getRegistrationId())),
                entry(getTable().GUEST_PROFILE_ID.getName(), Optional.ofNullable(pojo.getGuestProfileId()).map(value -> Long.toString(value)).orElse("")),
                entry(getTable().RAW_GUEST_NAME.getName(), pojo.getRawGuestName()),
                entry(getTable().RELATIONSHIP.getName(), pojo.getRelationship()));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(long actorId, Guest pojo) {
        Optional<Long> insertedId = jooq
                .insertInto(getTable())
                .set(getTable().REGISTRATION_ID, pojo.getRegistrationId())
                .set(getTable().GUEST_PROFILE_ID, pojo.getGuestProfileId())
                .set(getTable().RAW_GUEST_NAME, pojo.getRawGuestName())
                .set(getTable().RELATIONSHIP, pojo.getRelationship())
                .returning(getIdColumn())
                .fetchOptional()
                .map(GuestRecord::getId);
        if (insertedId.isPresent()) {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    HistoryLogic.ActionType.INSERTED.name(),
                    getTable().getName().toUpperCase(),
                    convertToJson(pojo),
                    JSON.json("{}"),
                    null
            ));
        }
        return insertedId;
    }

    @Override
    public int updateExisting(long actorId, Guest update) throws GuestException {
        if (update.getId() == null) {
            throw new GuestException(
                    new AbstractMap.SimpleImmutableEntry<>(getIdColumn().getName(), "ID to update was missing."),
                    "ID %d to update was missing.".formatted(update.getId()));
        }
        var originalRecord = fetchById(update.getId()).orElseThrow(() -> new GuestException(
                new AbstractMap.SimpleImmutableEntry<>(getTable().getName(), "Record to update was not found."),
                "Record %d to update was not found.".formatted(update.getId())));
        int updatedRecords = jooq
                .update(getTable())
                .set(getTable().REGISTRATION_ID, update.getRegistrationId())
                .set(getTable().GUEST_PROFILE_ID, update.getGuestProfileId())
                .set(getTable().RAW_GUEST_NAME, update.getRawGuestName())
                .set(getTable().RELATIONSHIP, update.getRelationship())
                .where(getIdColumn().eq(update.getId()))
                .and(
                        getTable().REGISTRATION_ID.notEqual(update.getRegistrationId())
                                .or(DSL.coalesce(getTable().GUEST_PROFILE_ID, 0).notEqual(update.getGuestProfileId()))
                                .or(getTable().RAW_GUEST_NAME.notEqual(update.getRawGuestName()))
                                .or(getTable().RELATIONSHIP.notEqual(update.getRelationship()))
                ).execute();
        if (updatedRecords > 0) {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    HistoryLogic.ActionType.UPDATED.name(),
                    getTable().getName().toUpperCase(),
                    convertToJson(update),
                    convertToJson(originalRecord),
                    null
            ));
        }
        return updatedRecords;
    }

    @Override
    public void delete(long actorId, long idToDelete) throws GuestException {
        var originalRecord = fetchById(idToDelete).orElseThrow(() -> new GuestException(
                new AbstractMap.SimpleImmutableEntry<>(getTable().getName(), "The guest to be deleted was not found."),
                String.format("The guest with ID %d to be deleted was not found.", idToDelete)));
        var deletedRecords = jooq.deleteFrom(getTable()).where(getTable().ID.eq(idToDelete)).execute();
        if (deletedRecords > 0) {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    HistoryLogic.ActionType.DELETED.name(),
                    getTable().getName().toUpperCase(),
                    JSON.json("{}"),
                    convertToJson(originalRecord),
                    null
            ));
        }
    }

    @Override
    public Map<String, String> validate(Guest pojo) {
        return GuestValidator.of(pojo).getMessages();
    }

    /**
     * Retrieves a list of guest IDs that are associated with the provided registration IDs.
     *
     * @param registrationIds a list of registration IDs for which associated guest IDs need to be fetched.
     * @return a list of guest IDs corresponding to the given registration IDs.
     */
    public List<Long> getGuestIdsByRegistrationIds(List<Long> registrationIds) {
        return jooq.select(getIdColumn()).from(getTable()).where(getTable().REGISTRATION_ID.in(registrationIds)).fetchInto(Long.class);
    }

    /**
     * Special logic class that works with a view instead of the raw table. The view provides useful label information for rows which, in the table, are simply FKs.
     */
    public static class GuestWithLabelsLogic extends Logic<GuestWithLabelsRecord, GuestWithLabels, net.briclabs.evcoordinator.generated.tables.GuestWithLabels> {
        public GuestWithLabelsLogic(ObjectMapper objectMapper, DSLContext jooq) {
            super(objectMapper, jooq, GuestWithLabels.class, GUEST_WITH_LABELS, GUEST_WITH_LABELS.ID);
        }
    }

    public static class GuestException extends LogicException {
        public GuestException(Map.Entry<String, String> publicMessage, String troubleshootingMessage) {
            super(publicMessage, troubleshootingMessage);
        }
    }
}