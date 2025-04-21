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

    @Override
    void validate() {
        if (pojo().getRecommendedDonation() < 0) {
            this.addMessage(table().RECOMMENDED_DONATION, MUST_BE_POSITIVE_NUMBER);
        }
        if (pojo().getCharityName().isBlank()) {
            this.addMessage(table().CHARITY_NAME, MUST_NOT_BE_BLANK);
        }
        if (pojo().getCharityUrl().isBlank() || !UrlValidator.getInstance().isValid(pojo().getCharityUrl())) {
            this.addMessage(table().CHARITY_URL, MUST_BE_VALID_URL);
        }
        if (pojo().getFundProcessorName().isBlank()) {
            this.addMessage(table().FUND_PROCESSOR_NAME, MUST_NOT_BE_BLANK);
        }
        if (pojo().getFundProcessorUrl().isBlank() || !UrlValidator.getInstance().isValid(pojo().getFundProcessorUrl())) {
            this.addMessage(table().FUND_PROCESSOR_URL, MUST_BE_VALID_URL);
        }
        validateFundProcessorInstructions();
        validateEventGuidelines();
    }

    private void validateFundProcessorInstructions() {
        var fundProcessorInstructions = pojo().getFundProcessorInstructions().data();

        if (fundProcessorInstructions.isBlank()) {
            this.addMessage(table().FUND_PROCESSOR_INSTRUCTIONS, MUST_BE_VALID_JSON);
            return;
        }

        try {
            var parsedInstructions = this.objectMapper.readTree(fundProcessorInstructions);

            if (!parsedInstructions.isObject()) {
                this.addMessage(table().EVENT_GUIDELINES, MUST_BE_VALID_JSON);
                return;
            }

            if (parsedInstructions.has("instructions")) {
                var parsedInstructionsArray = parsedInstructions.get("instructions");
                if (!parsedInstructionsArray.isArray() || parsedInstructionsArray.size() < 2) {
                    this.addMessage(table().FUND_PROCESSOR_INSTRUCTIONS, "Must contain an 'instructions' array with at least two values.");
                }
            } else {
                this.addMessage(table().FUND_PROCESSOR_INSTRUCTIONS, "Must contain a 'instructions' field.");
            }

            if (parsedInstructions.has("donationAddress")) {
                var parsedDonationAddress = parsedInstructions.get("donationAddress");
                if (!parsedDonationAddress.has("to") || !parsedDonationAddress.get("to").isTextual() || parsedDonationAddress.get("to").asText().isBlank()) {
                    this.addMessage(table().FUND_PROCESSOR_INSTRUCTIONS, "The 'donationAddress' field must have a valid 'to' text value.");
                }
                if (parsedDonationAddress.has("co") && (!parsedDonationAddress.get("co").isTextual() || parsedDonationAddress.get("co").asText().isBlank())) {
                    this.addMessage(table().FUND_PROCESSOR_INSTRUCTIONS, "'co' must either be absent from the 'donationAddress' field or be a valid text value.");
                }
                if (!parsedDonationAddress.has("street") || !parsedDonationAddress.get("street").isTextual() || parsedDonationAddress.get("street").asText().isBlank()) {
                    this.addMessage(table().FUND_PROCESSOR_INSTRUCTIONS, "The 'donationAddress' field must have a valid 'street' text value.");
                }
                if (!parsedDonationAddress.has("city") || !parsedDonationAddress.get("city").isTextual() || parsedDonationAddress.get("city").asText().isBlank()) {
                    this.addMessage(table().FUND_PROCESSOR_INSTRUCTIONS, "The 'donationAddress' field must have a valid 'city' text value.");
                }
                if (!parsedDonationAddress.has("state") || !parsedDonationAddress.get("state").isTextual() || parsedDonationAddress.get("state").asText().isBlank()) {
                    this.addMessage(table().FUND_PROCESSOR_INSTRUCTIONS, "The 'donationAddress' field must have a valid 'state' text value.");
                }
                if (!parsedDonationAddress.has("zip") || !parsedDonationAddress.get("zip").isTextual() || parsedDonationAddress.get("zip").asText().isBlank() || parsedDonationAddress.get("zip").asText().length() < 5) {
                    this.addMessage(table().FUND_PROCESSOR_INSTRUCTIONS, "The 'donationAddress' field must have a valid 'zip' text value.");
                }
            } else {
                this.addMessage(table().FUND_PROCESSOR_INSTRUCTIONS, "Must contain a 'donationAddress' field.");
            }
        } catch (Exception e) {
            this.addMessage(table().FUND_PROCESSOR_INSTRUCTIONS, MUST_BE_VALID_JSON);
        }

    }

    private void validateEventGuidelines() {
        var eventGuidelines = pojo().getEventGuidelines().data();

        if (eventGuidelines.isBlank()) {
            this.addMessage(table().EVENT_GUIDELINES, MUST_BE_VALID_JSON);
            return;
        }

        try {
            var parsedEventGuidelines = this.objectMapper.readTree(eventGuidelines);

            if (!parsedEventGuidelines.isObject()) {
                this.addMessage(table().EVENT_GUIDELINES, MUST_BE_VALID_JSON);
                return;
            }

            if (parsedEventGuidelines.isEmpty()) {
                this.addMessage(table().EVENT_GUIDELINES, "Must contain at least one section.");
            }

            parsedEventGuidelines.fieldNames().forEachRemaining(section -> {
                var valueNode = parsedEventGuidelines.get(section);

                if (!valueNode.isArray()) {
                    this.addMessage(
                            table().EVENT_GUIDELINES,
                            "'%s' must be an array.".formatted(section)
                    );
                }

                if (valueNode.isEmpty()) {
                    this.addMessage(
                            table().EVENT_GUIDELINES,
                            "'%s' must contain at least one string.".formatted(section)
                    );
                }

                for (var element : valueNode) {
                    if (!element.isTextual()) {
                        this.addMessage(
                                table().EVENT_GUIDELINES,
                                "'%s' must only contain strings.".formatted(section)
                        );
                    }
                }
            });

        } catch (Exception e) {
            this.addMessage(table().EVENT_GUIDELINES, MUST_BE_VALID_JSON);
        }
    }
}
