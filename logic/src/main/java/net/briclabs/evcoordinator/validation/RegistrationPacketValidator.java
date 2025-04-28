package net.briclabs.evcoordinator.validation;

import net.briclabs.evcoordinator.ParticipantLogic;
import net.briclabs.evcoordinator.RegistrationPacketLogic;
import net.briclabs.evcoordinator.model.RegistrationPacket;

import java.util.HashMap;
import java.util.Map;

public class RegistrationPacketValidator implements Validator {

    private final RegistrationPacket pojo;
    private final ParticipantLogic participantLogic;

    private final Map<String, String> messages = new HashMap<>();

    private RegistrationPacketValidator(RegistrationPacket pojo, ParticipantLogic participantLogic) {
        this.pojo = pojo;
        this.participantLogic = participantLogic;
        validate();
    }

    public static RegistrationPacketValidator of(RegistrationPacket registrationPacket, ParticipantLogic participantLogic) {
        return new RegistrationPacketValidator(registrationPacket, participantLogic);
    }

    void validate() {
        if (pojo.participant() == null || pojo.guests() == null || pojo.registration() == null) {
            this.addMessageOverallIncomplete();
            return;
        }
        if (!participantLogic.attendeePreexists(pojo.participant().getNameFirst(), pojo.participant().getNameLast(), pojo.participant().getAddrEmail())) {
            this.addMessage(RegistrationPacketLogic.PACKET_SECTION.PARTICIPANT, ParticipantValidator.of(pojo.participant(), true).getMessages());
        }
        addIndexedGuestsMessages();
        this.addMessage(RegistrationPacketLogic.PACKET_SECTION.REGISTRATION, RegistrationValidator.of(pojo.registration(), true).getMessages());
    }

    private void addIndexedGuestsMessages() {
        var guests = pojo.guests();
        for (int i = 0; i < guests.length; i++) {
            Map<String, String> updatedGuestMessages = new HashMap<>();
            for (var entry : GuestValidator.of(guests[i], true).getMessages().entrySet()) {
                updatedGuestMessages.put(i + "|" + entry.getKey(), entry.getValue());
            }
            this.addMessage(RegistrationPacketLogic.PACKET_SECTION.GUESTS, updatedGuestMessages);
        }
    }

    private void addMessageOverallIncomplete() {
        this.messages.put(RegistrationPacketLogic.PACKET_SECTION.OVERALL.toString(), "Registration packet is incomplete.");
    }

    private void addMessage(RegistrationPacketLogic.PACKET_SECTION packetSection, Map<String, String> messages) {
        messages.forEach((key, value) -> this.messages.put(packetSection.toString() + "|" + key, value));
    }

    @Override
    public Map<String, String> getMessages() {
        return Map.copyOf(messages);
    }
}
