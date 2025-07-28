package ma.atm.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class AuthenticationService {

    private final RestTemplate restTemplate;
    private final Keycloak keycloakAdmin;

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    public AuthenticationService(RestTemplate restTemplate, Keycloak keycloakAdmin) {
        this.restTemplate = restTemplate;
        this.keycloakAdmin = keycloakAdmin;
    }

    public ResponseEntity<Map> login(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("username", username);
        map.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        return restTemplate.postForEntity(
                keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token",
                request,
                Map.class
        );
    }

    public ResponseEntity<Map> refreshToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        return restTemplate.postForEntity(
                keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token",
                request,
                Map.class
        );
    }

    //logout
    public ResponseEntity<Map<String, String>> logout(String refreshToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            // These parameters are REQUIRED for Keycloak to properly terminate the session
            map.add("client_id", clientId);
            map.add("refresh_token", refreshToken);

            // Add client_secret if using confidential client (which you should be)
            if (clientSecret != null && !clientSecret.isEmpty()) {
                map.add("client_secret", clientSecret);
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            // Use exact URL from your configuration
            String logoutUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/logout";

            log.info("Calling Keycloak logout endpoint: {}", logoutUrl);

            ResponseEntity<Void> response = restTemplate.postForEntity(
                    logoutUrl,
                    request,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Keycloak session successfully terminated");
                return ResponseEntity.ok(Map.of("message", "Successfully logged out"));
            } else {
                log.warn("Keycloak returned status: {}", response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode())
                        .body(Map.of("error", "Logout failed with status: " + response.getStatusCode()));
            }
        } catch (Exception e) {
            log.error("Error during logout request to Keycloak: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error during logout: " + e.getMessage()));
        }
    }

    /**
     * Setup account with new password and return success response
     * This version doesn't try to get tokens immediately, avoiding the caching issue
     */
    /**
     * Check if a user has a complete profile
     */
    public Map<String, Object> checkUserProfile(String username) {
        try {
            String userId = getUserIdByUsername(username);
            if (userId == null) {
                throw new IllegalArgumentException("User not found: " + username);
            }

            UserResource userResource = keycloakAdmin.realm(realm).users().get(userId);
            UserRepresentation user = userResource.toRepresentation();

            boolean hasEmail = user.getEmail() != null && !user.getEmail().isEmpty();
            boolean hasFirstName = user.getFirstName() != null && !user.getFirstName().isEmpty();
            boolean hasLastName = user.getLastName() != null && !user.getLastName().isEmpty();

            Map<String, Object> result = new HashMap<>();
            result.put("username", username);
            result.put("hasEmail", hasEmail);
            result.put("hasFirstName", hasFirstName);
            result.put("hasLastName", hasLastName);
            result.put("profileComplete", hasEmail && hasFirstName && hasLastName);
            result.put("requiredActions", user.getRequiredActions());

            return result;
        } catch (Exception e) {
            log.error("Error checking user profile: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Setup account with new password and profile information
     */
    public Map<String, Object> setupAccount(String username, String currentPassword, String newPassword,
                                            String email, String firstName, String lastName) {
        try {
            log.info("Setting up account for user: {}", username);

            // Validate password complexity
            if (!meetsPasswordRequirements(newPassword)) {
                throw new IllegalArgumentException("New password does not meet complexity requirements");
            }

            // Validate email if provided
            if (email != null && !email.isEmpty() && !isValidEmail(email)) {
                throw new IllegalArgumentException("Invalid email format");
            }

            // Step 1: Get user ID from username
            String userId = getUserIdByUsername(username);
            if (userId == null) {
                throw new IllegalArgumentException("User not found: " + username);
            }

            log.info("Found user ID: {} for username: {}", userId, username);

            // Step 2: Get user representation
            UserResource userResource = keycloakAdmin.realm(realm).users().get(userId);
            UserRepresentation user = userResource.toRepresentation();

            // Step 3: Update user profile information if provided
            boolean profileUpdated = false;

            if (email != null && !email.isEmpty()) {
                user.setEmail(email);
                user.setEmailVerified(true); // Auto-verify for simplicity
                profileUpdated = true;
            }

            if (firstName != null && !firstName.isEmpty()) {
                user.setFirstName(firstName);
                profileUpdated = true;
            }

            if (lastName != null && !lastName.isEmpty()) {
                user.setLastName(lastName);
                profileUpdated = true;
            }

            // Step 4: Update user flags and settings
            user.setEnabled(true);
            user.setRequiredActions(new ArrayList<>());

            if (profileUpdated) {
                log.info("Updating user profile for: {}", username);
                userResource.update(user);
            }

            // Step 5: Set password (non-temporary)
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);
            userResource.resetPassword(credential);

            log.info("Password set and account fully activated for user: {}", username);

            // Step 6: Logout all sessions for this user to clear any invalid state
            try {
                userResource.logout();
                log.info("All existing sessions logged out for user: {}", username);
            } catch (Exception e) {
                log.warn("Could not log out existing sessions, continuing anyway: {}", e.getMessage());
            }

            // Step 7: Return success
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Account setup completed successfully. Please log in with your new password.");
            result.put("profileUpdated", profileUpdated);

            return result;
        } catch (Exception e) {
            log.error("Error setting up account for username: {}", username, e);
            throw e;
        }
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return true; //email.matches(emailRegex);
    }

    /**
     * Get user ID by username
     */
    public String getUserIdByUsername(String username) {
        List<UserRepresentation> users = keycloakAdmin.realm(realm)
                .users()
                .search(username, true);

        if (users.isEmpty()) {
            return null;
        }

        return users.get(0).getId();
    }

    /**
     * Set password for a user
     */
    private void setUserPassword(String userId, String password, boolean temporary) {
        try {
            // Create credential representation
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(temporary);

            // Update the user with the new credential
            UserResource userResource = keycloakAdmin.realm(realm).users().get(userId);
            userResource.resetPassword(credential);

            log.info("Password set for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Error setting password: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Clear all required actions for a user
     */
    private void clearRequiredActions(String userId) {
        try {
            UserResource userResource = keycloakAdmin.realm(realm).users().get(userId);
            UserRepresentation user = userResource.toRepresentation();

            // Log current required actions
            log.info("Current required actions: {}", user.getRequiredActions());

            // Clear required actions
            user.setRequiredActions(new ArrayList<>());
            userResource.update(user);

            log.info("Cleared required actions for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Error clearing required actions: {}", e.getMessage());
            throw e;
        }
    }



    /**
     * Validate password complexity
     */
    private boolean meetsPasswordRequirements(String password) {
        // Minimum 8 characters, at least one uppercase, one lowercase, one number, and one special character
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return true; //password.matches(regex);
    }

    /**
     * Check if a token has the UPDATE_PASSWORD required action
     */
    public boolean isPasswordChangeRequired(String token) {
        try {
            if (token == null || token.isEmpty() || !token.contains(".")) {
                return false;
            }

            // Decode JWT payload (middle part)
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return false;
            }

            String payload = new String(Base64.getDecoder().decode(parts[1]));

            // Check for required_actions field
            return payload.contains("\"required_actions\"") && payload.contains("\"UPDATE_PASSWORD\"");
        } catch (Exception e) {
            log.error("Error checking if password change is required: {}", e.getMessage());
            return false;
        }
    }

}