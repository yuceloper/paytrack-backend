package com.yuceloper.paytrack.auth.infrastructure;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleIdentityVerifier {

    private final String clientId;

    public GoogleIdentityVerifier(@Value("${paytrack.auth.google-client-id:}") String clientId) {
        this.clientId = clientId == null ? "" : clientId.trim();
    }

    public GoogleIdentity verify(String idTokenString) {
        if (clientId.isBlank()) {
            throw new IllegalStateException("Google authentication is not configured");
        }
        if (idTokenString == null || idTokenString.isBlank()) {
            throw new IllegalArgumentException("idToken is required");
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance()
            ).setAudience(Collections.singletonList(clientId)).build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new IllegalArgumentException("Google email is not verified");
            }

            String subject = payload.getSubject();
            String email = payload.getEmail();
            String name = payload.get("name") instanceof String value ? value : email;
            if (subject == null || subject.isBlank() || email == null || email.isBlank()) {
                throw new IllegalArgumentException("Google identity is incomplete");
            }
            return new GoogleIdentity(subject, email, name);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Google ID token could not be verified", e);
        }
    }

    public record GoogleIdentity(String subject, String email, String name) {}
}
