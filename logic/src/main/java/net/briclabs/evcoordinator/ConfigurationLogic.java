package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Configuration;
import net.briclabs.evcoordinator.generated.tables.records.ConfigurationRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Configuration.CONFIGURATION;

public class ConfigurationLogic<P extends Configuration> extends Logic implements WriteLogic<P>  {

    static List<Condition> parseCriteriaIntoConditions(Map<String, String> searchCriteria) {
        stripOutUnknownFields(searchCriteria, CONFIGURATION);
        List<Condition> matchConditions = new ArrayList<>();
        searchCriteria.forEach((key, value) -> {
            addPossibleCondition(CONFIGURATION.ID, key, value);
            addPossibleCondition(CONFIGURATION.CHARITY_NAME, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(CONFIGURATION.CHARITY_URL, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(CONFIGURATION.EVENT_GUIDELINES, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(CONFIGURATION.FUND_PROCESSOR_INSTRUCTIONS, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(CONFIGURATION.FUND_PROCESSOR_NAME, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(CONFIGURATION.FUND_PROCESSOR_URL, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(CONFIGURATION.RECOMMENDED_DONATION, key, value).ifPresent(matchConditions::add);
        });
        return matchConditions;
    }

    public ConfigurationLogic(DSLContext jooq) {
        super(jooq);
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
        return fetchByCriteria(criteria, 0, 1).size() > 0;
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
    public List<Configuration> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max) {
        return jooq
                .selectFrom(CONFIGURATION)
                .where(parseCriteriaIntoConditions(searchCriteria))
                .orderBy(CONFIGURATION.ID)
                .limit(offset, max)
                .fetchStreamInto(Configuration.class)
                .toList();
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
