package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Configuration;
import net.briclabs.evcoordinator.generated.tables.records.ConfigurationRecord;
import org.jooq.DSLContext;
import org.jooq.JSONB;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.Tables.CONFIGURATION;

public class ConfigurationLogic<P extends Configuration> extends Logic<ConfigurationRecord, Configuration, net.briclabs.evcoordinator.generated.tables.Configuration> implements WriteLogic<P>  {
    public ConfigurationLogic(DSLContext jooq) {
        super(jooq, Configuration.class, CONFIGURATION, CONFIGURATION.ID);
    }

    @Override
    public boolean validateIsTrulyNew(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().CHARITY_NAME.getName(), pojo.getCharityName()),
                entry(getTable().CHARITY_URL.getName(), pojo.getCharityUrl()),
                entry(getTable().EVENT_GUIDELINES.getName(), pojo.getEventGuidelines().toString()),
                entry(getTable().FUND_PROCESSOR_INSTRUCTIONS.getName(), pojo.getFundProcessorInstructions().toString()),
                entry(getTable().FUND_PROCESSOR_NAME.getName(), pojo.getFundProcessorName()),
                entry(getTable().FUND_PROCESSOR_URL.getName(), pojo.getFundProcessorUrl()),
                entry(getTable().RECOMMENDED_DONATION.getName(), pojo.getRecommendedDonation().toString()));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    /**
     * Fetches the latest configuration entry from the database table based on the descending order
     * of the ID column. If no entries exist in the table, an empty {@code Optional} is returned.
     *
     * @return an {@code Optional<Configuration>} containing the latest configuration entry if present, or empty if no entries exist.
     */
    public Optional<Configuration> fetchLatest() {
        return jooq
                .selectFrom(getTable())
                .orderBy(getIdColumn().desc())
                .limit(1)
                .fetchOptionalInto(getRecordType());
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(getTable())
                .set(getTable().CHARITY_NAME, pojo.getCharityName())
                .set(getTable().CHARITY_URL, pojo.getCharityUrl())
                .set(getTable().EVENT_GUIDELINES, pojo.getEventGuidelines())
                .set(getTable().FUND_PROCESSOR_INSTRUCTIONS, pojo.getFundProcessorInstructions())
                .set(getTable().FUND_PROCESSOR_NAME, pojo.getFundProcessorName())
                .set(getTable().FUND_PROCESSOR_URL, pojo.getFundProcessorUrl())
                .set(getTable().RECOMMENDED_DONATION, pojo.getRecommendedDonation())
                .returning(getIdColumn())
                .fetchOptional()
                .map(ConfigurationRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq
                .update(getTable())
                .set(getTable().CHARITY_NAME, update.getCharityName())
                .set(getTable().CHARITY_URL, update.getCharityUrl())
                .set(getTable().EVENT_GUIDELINES, update.getEventGuidelines())
                .set(getTable().FUND_PROCESSOR_INSTRUCTIONS, update.getFundProcessorInstructions())
                .set(getTable().FUND_PROCESSOR_NAME, update.getFundProcessorName())
                .set(getTable().FUND_PROCESSOR_URL, update.getFundProcessorUrl())
                .set(getTable().RECOMMENDED_DONATION, update.getRecommendedDonation())
                .where(getTable().ID.eq(update.getId()))
                .and(
                        getTable().CHARITY_NAME.notEqual(update.getCharityName())
                                .or(getTable().CHARITY_URL.notEqual(update.getCharityUrl()))
                                .or(getTable().EVENT_GUIDELINES.cast(JSONB.class).notEqual(JSONB.valueOf(update.getEventGuidelines().data())))
                                .or(getTable().FUND_PROCESSOR_INSTRUCTIONS.cast(JSONB.class).notEqual(JSONB.valueOf(update.getFundProcessorInstructions().data())))
                                .or(getTable().FUND_PROCESSOR_NAME.notEqual(update.getFundProcessorName()))
                                .or(getTable().FUND_PROCESSOR_URL.notEqual(update.getFundProcessorUrl()))
                                .or(getTable().RECOMMENDED_DONATION.notEqual(update.getRecommendedDonation()))
                )
                .execute();
    }
}
