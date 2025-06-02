package com.foodmarketplace.user.event;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class UserEventProducer {

    @Inject
    @Channel("user-events")
    Emitter<String> eventEmitter;

    public void publishUserRegistered(UUID userId, String email, String role) {
        String event = createEvent("USER_REGISTERED", userId, 
            String.format("{\"email\":\"%s\",\"role\":\"%s\"}", email, role));
        eventEmitter.send(event);
    }

    public void publishUserStatusUpdated(UUID userId, String status) {
        String event = createEvent("USER_STATUS_UPDATED", userId, 
            String.format("{\"status\":\"%s\"}", status));
        eventEmitter.send(event);
    }

    public void publishUserVerificationUpdated(UUID userId, String verificationStatus) {
        String event = createEvent("USER_VERIFICATION_UPDATED", userId, 
            String.format("{\"verificationStatus\":\"%s\"}", verificationStatus));
        eventEmitter.send(event);
    }

    private String createEvent(String eventType, UUID userId, String data) {
        return String.format(
            "{\"eventType\":\"%s\",\"userId\":\"%s\",\"timestamp\":\"%s\",\"data\":%s}",
            eventType, userId, LocalDateTime.now(), data
        );
    }
}
