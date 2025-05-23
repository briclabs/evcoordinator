package net.briclabs.evcoordinator.model;

import net.briclabs.evcoordinator.generated.tables.pojos.Guest;
import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import net.briclabs.evcoordinator.generated.tables.pojos.Registration;

import java.io.Serializable;

/**
 * Represents a registration request containing required details about a participant
 * and their associated event registration information.
 * <br>
 * <br>
 * The RegistrationPacket encapsulates essential data such as the participant's
 * information, guests, event ID, pledged monetary amount, and a cryptographic
 * signature to validate the transaction.
 * <br>
 * <br>
 * This is an immutable record class.
 * <br>
 * <br>
 * Fields:<br>
 * - participant: The participant registering for the event.<br>
 * - guests: An array of guests related to the participant's registration.<br>
 * - eventInfoId: The unique identifier of the event info being registered for.<br>
 * - donationPledge: The monetary amount pledged by the participant, if any.<br>
 * - signature: A signature verifying the registration request.
 */
public record RegistrationPacket(Participant participant, Guest[] guests, Registration registration) implements Serializable {
}
