package net.briclabs.evcoordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.generated.tables.pojos.EventStatistics;
import net.briclabs.evcoordinator.generated.tables.records.EventStatisticsRecord;
import org.jooq.DSLContext;

import java.util.Optional;

import static net.briclabs.evcoordinator.generated.tables.EventStatistics.EVENT_STATISTICS;

public class EventStatisticsLogic  extends Logic<EventStatisticsRecord, EventStatistics, net.briclabs.evcoordinator.generated.tables.EventStatistics> {

    public EventStatisticsLogic(ObjectMapper objectMapper, DSLContext jooq) {
        super(objectMapper, jooq, EventStatistics.class, EVENT_STATISTICS, EVENT_STATISTICS.EVENT_ID);
    }

    /**
     * Fetches the latest event statistics marked with a "CURRENT" status.
     * The result is determined by ordering event records in descending order
     * by start date and end date, and retrieving the most recent one.
     *
     * @return an Optional containing the latest EventStatistics if available, or an empty Optional if no records are found.
     */
    public Optional<EventStatistics> fetchLatest() {
        return jooq
                .selectFrom(getTable())
                .where(getTable().EVENT_STATUS.eq("CURRENT"))
                .orderBy(getTable().DATE_START.desc(), getTable().DATE_END.desc())
                .limit(1)
                .fetchOptionalInto(getRecordType());
    }
}
