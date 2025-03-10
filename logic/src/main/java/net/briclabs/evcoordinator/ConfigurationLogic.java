package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Configuration;
import net.briclabs.evcoordinator.generated.tables.records.ConfigurationRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSONB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Configuration.CONFIGURATION;

public class ConfigurationLogic<P extends Configuration> extends Logic implements WriteLogic<P>  {

    private static final Map<String, Field<?>> FIELDS = Map.ofEntries(
            Map.entry("ID", CONFIGURATION.ID),
            Map.entry("RECOMMENDED_DONATION", CONFIGURATION.RECOMMENDED_DONATION),
            Map.entry("CHARITY_NAME", CONFIGURATION.CHARITY_NAME),
            Map.entry("CHARITY_URL", CONFIGURATION.CHARITY_URL),
            Map.entry("FUND_PROCESSOR_NAME", CONFIGURATION.FUND_PROCESSOR_NAME),
            Map.entry("FUND_PROCESSOR_URL", CONFIGURATION.FUND_PROCESSOR_URL),
            Map.entry("FUND_PROCESSOR_INSTRUCTIONS", CONFIGURATION.FUND_PROCESSOR_INSTRUCTIONS),
            Map.entry("EVENT_GUIDELINES", CONFIGURATION.EVENT_GUIDELINES)
    );

    public ConfigurationLogic(DSLContext jooq) {
        super(jooq);
    }

    static List<Condition> parseCriteriaIntoConditions(boolean exactCriteria, Map<String, String> searchCriteria) {
        stripOutUnknownFields(searchCriteria, CONFIGURATION);
        List<Condition> matchConditions = new ArrayList<>();
        searchCriteria.forEach((key, value) -> {
            addPossibleCondition(CONFIGURATION.CHARITY_NAME, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(CONFIGURATION.CHARITY_URL, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(CONFIGURATION.EVENT_GUIDELINES, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(CONFIGURATION.FUND_PROCESSOR_INSTRUCTIONS, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(CONFIGURATION.FUND_PROCESSOR_NAME, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(CONFIGURATION.FUND_PROCESSOR_URL, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(CONFIGURATION.RECOMMENDED_DONATION, key, value, exactCriteria).ifPresent(matchConditions::add);
        });
        return matchConditions;
    }

    @Override
    public boolean validateIsTrulyNew(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(CONFIGURATION.CHARITY_NAME.getName(), pojo.getCharityName()),
                entry(CONFIGURATION.CHARITY_URL.getName(), pojo.getCharityUrl()),
                entry(CONFIGURATION.EVENT_GUIDELINES.getName(), pojo.getEventGuidelines().toString()),
                entry(CONFIGURATION.FUND_PROCESSOR_INSTRUCTIONS.getName(), pojo.getFundProcessorInstructions().toString()),
                entry(CONFIGURATION.FUND_PROCESSOR_NAME.getName(), pojo.getFundProcessorName()),
                entry(CONFIGURATION.FUND_PROCESSOR_URL.getName(), pojo.getFundProcessorUrl()),
                entry(CONFIGURATION.RECOMMENDED_DONATION.getName(), pojo.getRecommendedDonation().toString()));
        return fetchByCriteria(true, criteria, CONFIGURATION.ID.getName(), false,0, 1).count() > 0;
    }

    public Optional<Configuration> fetchLatest() {
        return jooq
                .selectFrom(CONFIGURATION)
                .orderBy(CONFIGURATION.ID.desc())
                .limit(1)
                .fetchOptionalInto(Configuration.class);
    }

    @Override
    public Optional<Configuration> fetchById(Long id) {
        return jooq
                .selectFrom(CONFIGURATION)
                .where(CONFIGURATION.ID.eq(id))
                .fetchOptionalInto(Configuration.class);
    }

    @Override
    public Field<?> resolveField(String columnName, Field<?> defaultField) {
        return FIELDS.getOrDefault(columnName, defaultField);
    }

    @Override
    public ListWithCount<Configuration> fetchByCriteria(boolean exactCriteria, Map<String, String> searchCriteria, String sortColumn, Boolean sortAscending, int offset, int max) {
        List<Condition> conditions = parseCriteriaIntoConditions(exactCriteria, searchCriteria);

        List<Configuration> results = jooq
                .selectFrom(CONFIGURATION)
                .where(buildWhereClause(exactCriteria, conditions))
                .orderBy(sortAscending
                        ? resolveField(sortColumn, CONFIGURATION.ID).asc()
                        : resolveField(sortColumn, CONFIGURATION.ID).desc())
                .limit(offset, max)
                .fetchStreamInto(Configuration.class)
                .toList();
        int count = jooq
                .selectCount()
                .from(CONFIGURATION)
                .where(conditions)
                .fetchOptional(0, Integer.class)
                .orElse(0);
        return new ListWithCount<>(results, count);
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(CONFIGURATION)
                .set(CONFIGURATION.CHARITY_NAME, pojo.getCharityName())
                .set(CONFIGURATION.CHARITY_URL, pojo.getCharityUrl())
                .set(CONFIGURATION.EVENT_GUIDELINES, pojo.getEventGuidelines())
                .set(CONFIGURATION.FUND_PROCESSOR_INSTRUCTIONS, pojo.getFundProcessorInstructions())
                .set(CONFIGURATION.FUND_PROCESSOR_NAME, pojo.getFundProcessorName())
                .set(CONFIGURATION.FUND_PROCESSOR_URL, pojo.getFundProcessorUrl())
                .set(CONFIGURATION.RECOMMENDED_DONATION, pojo.getRecommendedDonation())
                .returning(CONFIGURATION.ID)
                .fetchOptional()
                .map(ConfigurationRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq
                .update(CONFIGURATION)
                .set(CONFIGURATION.CHARITY_NAME, update.getCharityName())
                .set(CONFIGURATION.CHARITY_URL, update.getCharityUrl())
                .set(CONFIGURATION.EVENT_GUIDELINES, update.getEventGuidelines())
                .set(CONFIGURATION.FUND_PROCESSOR_INSTRUCTIONS, update.getFundProcessorInstructions())
                .set(CONFIGURATION.FUND_PROCESSOR_NAME, update.getFundProcessorName())
                .set(CONFIGURATION.FUND_PROCESSOR_URL, update.getFundProcessorUrl())
                .set(CONFIGURATION.RECOMMENDED_DONATION, update.getRecommendedDonation())
                .where(CONFIGURATION.ID.eq(update.getId()))
                .and(
                                    CONFIGURATION.CHARITY_NAME.notEqual(update.getCharityName())
                                .or(CONFIGURATION.CHARITY_URL.notEqual(update.getCharityUrl()))
                                .or(CONFIGURATION.EVENT_GUIDELINES.cast(JSONB.class).notEqual(JSONB.valueOf(update.getEventGuidelines().data())))
                                .or(CONFIGURATION.FUND_PROCESSOR_INSTRUCTIONS.cast(JSONB.class).notEqual(JSONB.valueOf(update.getFundProcessorInstructions().data())))
                                .or(CONFIGURATION.FUND_PROCESSOR_NAME.notEqual(update.getFundProcessorName()))
                                .or(CONFIGURATION.FUND_PROCESSOR_URL.notEqual(update.getFundProcessorUrl()))
                                .or(CONFIGURATION.RECOMMENDED_DONATION.notEqual(update.getRecommendedDonation()))
                )
                .execute();
    }
}
