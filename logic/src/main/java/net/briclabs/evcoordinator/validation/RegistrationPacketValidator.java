package net.briclabs.evcoordinator.validation;

import net.briclabs.evcoordinator.RegistrationPacketLogic;
import net.briclabs.evcoordinator.model.RegistrationPacket;

import java.util.HashMap;
import java.util.Map;

public class RegistrationPacketValidator implements Validator {

    private final RegistrationPacket pojo;

    private final Map<String, String> messages = new HashMap<>();

    private RegistrationPacketValidator(RegistrationPacket pojo) {
        this.pojo = pojo;
        validate();
    }

    public static RegistrationPacketValidator of(RegistrationPacket registrationPacket) {
        return new RegistrationPacketValidator(registrationPacket);
    }

    void validate() {
        if (pojo.participant() == null || pojo.guests() == null || pojo.registration() == null) {
            this.addMessageOverallIncomplete();
            return;
        }
        this.addMessage(RegistrationPacketLogic.PACKET_SECTION.PARTICIPANT, ParticipantValidator.of(pojo.participant()).getMessages());
        for (var guest : pojo.guests()) {
            this.addMessage(RegistrationPacketLogic.PACKET_SECTION.GUESTS, GuestValidator.of(guest).getMessages());
        }
        this.addMessage(RegistrationPacketLogic.PACKET_SECTION.REGISTRATION, RegistrationValidator.of(pojo.registration()).getMessages());
    }

    private void addMessageOverallIncomplete() {
        this.messages.put(RegistrationPacketLogic.PACKET_SECTION.OVERALL.toString(), "Registration packet is incomplete.");
    }

    private void addMessage(RegistrationPacketLogic.PACKET_SECTION packetSection, Map<String, String> messages) {
        messages.forEach((key, value) -> this.messages.put(packetSection.toString() + "." + key, value));
    }

    @Override
    public Map<String, String> getMessages() {
        return Map.copyOf(messages);
    }
}
