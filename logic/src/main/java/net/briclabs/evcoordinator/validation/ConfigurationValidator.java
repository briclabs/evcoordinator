package net.briclabs.evcoordinator.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.generated.tables.pojos.Configuration;
import net.briclabs.evcoordinator.generated.tables.records.ConfigurationRecord;
import org.apache.commons.validator.routines.UrlValidator;

public class ConfigurationValidator extends AbstractValidator<ConfigurationRecord, Configuration, net.briclabs.evcoordinator.generated.tables.Configuration> {

    private final ObjectMapper objectMapper;

    private ConfigurationValidator(Configuration configuration, ObjectMapper objectMapper) {
        super(net.briclabs.evcoordinator.generated.tables.Configuration.CONFIGURATION, configuration);
        this.objectMapper = objectMapper;
    }

    public static ConfigurationValidator of(Configuration configuration, ObjectMapper objectMapper) {
        return new ConfigurationValidator(configuration, objectMapper);
    }

    void validate() {
        if (pojo().getRecommendedDonation() < 0) {
            this.addMessage(table().RECOMMENDED_DONATION, MUST_BE_POSITIVE_NUMBER);
        }
        if (pojo().getCharityName().isBlank()) {
            this.addMessage(table().CHARITY_NAME, MUST_NOT_BE_BLANK);
        }
        if (UrlValidator.getInstance().isValid(pojo().getCharityUrl())) {
            this.addMessage(table().CHARITY_URL, MUST_BE_VALID_URL);
        }
        if (pojo().getFundProcessorName().isBlank()) {
            this.addMessage(table().FUND_PROCESSOR_NAME, MUST_NOT_BE_BLANK);
        }
        if (UrlValidator.getInstance().isValid(pojo().getFundProcessorUrl())) {
            this.addMessage(table().FUND_PROCESSOR_URL, MUST_BE_VALID_URL);
        }
        var fundProcessorInstructions = pojo().getFundProcessorInstructions();
        if (!fundProcessorInstructions.data().isBlank()) {
            try {
                this.objectMapper.readTree(fundProcessorInstructions.data());
            } catch (Exception e) {
                this.addMessage(table().FUND_PROCESSOR_INSTRUCTIONS, MUST_BE_VALID_JSON);
            }
        }
        var eventGuidelines = pojo().getEventGuidelines().data();
        if (!eventGuidelines.isBlank()) {
            try {
                this.objectMapper.readTree(eventGuidelines);
            } catch (Exception e) {
                this.addMessage(table().EVENT_GUIDELINES, MUST_BE_VALID_JSON);
            }
        }
    }
}
