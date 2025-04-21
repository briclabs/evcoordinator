package net.briclabs.evcoordinator.validation;

import net.briclabs.evcoordinator.EventInfoLogic;
import net.briclabs.evcoordinator.generated.tables.pojos.EventInfo;
import net.briclabs.evcoordinator.generated.tables.records.EventInfoRecord;

import java.time.LocalDate;

public class EventInfoValidator extends AbstractValidator<EventInfoRecord, EventInfo, net.briclabs.evcoordinator.generated.tables.EventInfo> {

    private static final LocalDate NOW_DATE = LocalDate.now();

    private EventInfoValidator(EventInfo eventInfo) {
        super(net.briclabs.evcoordinator.generated.tables.EventInfo.EVENT_INFO, eventInfo);
    }

    public static EventInfoValidator of(EventInfo eventInfo) {
        return new EventInfoValidator(eventInfo);
    }

    @Override
    void validate() {
        if (pojo().getEventName().isBlank()) {
            addMessage(table().EVENT_NAME, MUST_NOT_BE_BLANK);
        }
        if (pojo().getEventTitle().isBlank()) {
            addMessage(table().EVENT_TITLE, MUST_NOT_BE_BLANK);
        }

        if (pojo().getDateStart() == null) {
            addMessage(table().DATE_START, MUST_NOT_BE_BLANK);
        }
        if (pojo().getDateEnd() == null) {
            addMessage(table().DATE_END, MUST_NOT_BE_BLANK);
        }
        if (pojo().getDateStart() != null && pojo().getDateEnd() != null && pojo().getDateStart().isAfter(pojo().getDateEnd())) {
            addMessage(table().DATE_START, MUST_BE_BEFORE_END);
        }

        var eventStatus = EventInfoLogic.EVENT_STATUS.fromString(pojo().getEventStatus());
        if (eventStatus.isEmpty()) {
            addMessage(table().EVENT_STATUS, MUST_NOT_BE_BLANK);
            return;
        }

        switch (eventStatus.get()) {
            case CURRENT -> {
                if (!pojo().getDateEnd().isEqual(NOW_DATE) && !pojo().getDateEnd().isAfter(NOW_DATE)) {
                    addMessage(table().DATE_END, MUST_BE_NOW_OR_FUTURE);
                }
            }
            case PAST -> {
                if (!pojo().getDateEnd().isBefore(NOW_DATE)) {
                    addMessage(table().DATE_END, MUST_BE_PAST);
                }
            }
        }
    }
}
