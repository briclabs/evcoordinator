package net.briclabs.evcoordinator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.ParticipantLogic;
import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import org.jooq.DSLContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

public abstract class ApiController {

    public static final String V1 = "v1";

    protected final DSLContext jooq;

    private final ParticipantLogic participantLogic;

    public ApiController(ObjectMapper objectMapper, DSLContext dslContext) {
        this.jooq = dslContext;
        this.participantLogic = new ParticipantLogic(objectMapper, jooq);
    }

    /**
     * Retrieves the ID of the authenticated participant.
     * If no authenticated participant is found or the ID is null,
     * the method returns 0.
     *
     * @return the ID of the authenticated participant if present; otherwise, 0.
     */
    protected long getActorId() {
        var participant = getAuthenticatedParticipant();
        return participant.isEmpty() || participant.get().getId() == null ? 0 : participant.get().getId();
    }

    /**
     * Retrieves the authenticated participant based on the current security context.
     * If the user is authenticated and their email address is present in the authentication token,
     * it fetches the participant associated with that email address.
     *
     * @return an {@code Optional} containing the authenticated participant if present;
     *         otherwise, an empty {@code Optional}.
     */
    private Optional<Participant> getAuthenticatedParticipant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            var authenticatedUserEmailAddress = jwtAuthenticationToken.getTokenAttributes().get("email").toString();
            var fetchedParticipants = participantLogic.fetchParticipantByEmail(authenticatedUserEmailAddress);
            if (fetchedParticipants.count() > 0) {
                return Optional.of(fetchedParticipants.list().get(0));
            }
        }
        return Optional.empty();
    }
}
