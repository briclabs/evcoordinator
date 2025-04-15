package net.briclabs.evcoordinator.validation;

import net.briclabs.evcoordinator.EventInfoLogic;
import net.briclabs.evcoordinator.generated.tables.pojos.EventInfo;
import net.briclabs.evcoordinator.generated.tables.records.EventInfoRecord;

import java.time.LocalDate;

public class EventInfoValidator extends AbstractValidator<EventInfoRecord, EventInfo, net.briclabs.evcoordinator.generated.tables.EventInfo> {

    private EventInfoValidator(EventInfo eventInfo) {
        super(net.briclabs.evcoordinator.generated.tables.EventInfo.EVENT_INFO, eventInfo);
    }

    public static EventInfoValidator of(EventInfo eventInfo) {
        return new EventInfoValidator(eventInfo);
    }

    void validate() {
        if (pojo().getEventName().isBlank()) {
            addMessage(table().EVENT_NAME, MUST_NOT_BE_BLANK);
        }
        if (pojo().getEventTitle().isBlank()) {
            addMessage(table().EVENT_TITLE, MUST_NOT_BE_BLANK);
        }
        if (pojo().getDateStart().isAfter(pojo().getDateEnd())) {
            addMessage(table().DATE_START, MUST_BE_BEFORE_END);
        }
        if (pojo().getEventStatus().isBlank()) {
            addMessage(table().EVENT_STATUS, MUST_NOT_BE_BLANK);
        }
        var eventStatus = pojo().getEventStatus();
        if (eventStatus.equalsIgnoreCase(EventInfoLogic.EVENT_STATUS.CURRENT.toString()) && pojo().getDateEnd().isAfter(LocalDate.now())) {
            addMessage(table().DATE_END, MUST_BE_FUTURE);
        } else if (eventStatus.equalsIgnoreCase(EventInfoLogic.EVENT_STATUS.PAST.toString()) && pojo().getDateEnd().isBefore(LocalDate.now())) {
            addMessage(table().DATE_END, MUST_BE_PAST);
        }
    }
}
