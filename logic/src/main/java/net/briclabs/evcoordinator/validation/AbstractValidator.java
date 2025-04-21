package net.briclabs.evcoordinator.validation;

import org.jooq.TableField;
import org.jooq.impl.TableImpl;
import org.jooq.impl.TableRecordImpl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract base class for validators. This class defines a contract for
 * validation by providing an abstract method that must be implemented by
 * concrete subclasses. It also provides a utility to collect and retrieve
 * validation error messages.
 */
public abstract class AbstractValidator<R extends TableRecordImpl<R>, P extends Serializable, T extends TableImpl<R>> implements Validator {

    public static final String MUST_BE_VALID_VALUE = "Must be a valid value.";
    public static final String MUST_NOT_BE_BLANK = "Must not be blank.";
    public static final String MUST_BE_EMPTY_OR_NOT_BE_BLANK = "Must be empty or not be blank.";
    public static final String MUST_BE_POSITIVE_NUMBER = "Must be a positive number.";
    public static final String MUST_BE_VALID_URL = "Must be a valid URL.";
    public static final String MUST_BE_VALID_JSON = "Must be a valid JSON object.";
    public static final String MUST_BE_BEFORE_END = "Must be before end date.";
    public static final String MUST_BE_NOW_OR_FUTURE = "Must be in the present or future.";
    public static final String MUST_BE_PAST = "Must be in the past.";
    public static final String MUST_BE_VALID_STATE_ABBR = "Must be a valid US state abbreviation.";
    public static final String MUST_BE_VALID_ZIP = "Must be a valid zip code.";
    public static final String MUST_BE_VALID_EMAIL = "Must be a valid email address.";
    public static final String MUST_BE_VALID_PHONE = "Must be a valid 10-digit number.";

    private final T table;
    private final P pojo;

    private final Map<String, String> messages = new HashMap<>();

    public AbstractValidator(T table, P pojo) {
        this.table = table;
        this.pojo = pojo;
    }

    /**
     * Validates the state or properties of an object or entity. This method is
     * intended to be implemented by subclasses of the containing class to provide
     * domain-specific validation logic. Typically, it checks for invalid or
     * inconsistent data and records messages as needed.
     * <p>
     * Implementing classes should ensure that all validation messages are captured
     * and stored in a way that they can be retrieved for diagnostic purposes.
     * <p>
     * This method does not return any values. Errors resulting from validation
     * should be stored in a member accessible through a separate method.
     */
    abstract void validate();

    /**
     * Adds a validation message associated with a specific field name to the
     * collection of validation messages.
     *
     * @param field   the name of the field that the validation message is associated with
     * @param message the validation message describing the issue with the specified field
     */
    public void addMessage(TableField<R, ?> field, String message) {
        if (this.messages.containsKey(field.getName())) {
            this.messages.compute(field.getName(), (key, existingValue) -> existingValue + " " + message);
        } else {
            this.messages.put(field.getName(), message);
        }
    }

    @Override
    public Map<String, String> getMessages() {
        validate();
        return Map.copyOf(messages);
    }

    /**
     * Retrieves the table instance associated with this validator.
     *
     * @return the table instance of type T associated with the validator
     */
    public T table() {
        return table;
    }

    /**
     * Retrieves the POJO (Plain Old Java Object) associated with this validator.
     *
     * @return the POJO object of type P associated with the validator
     */
    public P pojo() {
        return pojo;
    }
}
