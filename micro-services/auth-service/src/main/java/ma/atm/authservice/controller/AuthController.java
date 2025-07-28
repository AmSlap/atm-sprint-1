package ma.atm.authservice.controller;

import lombok.extern.slf4j.Slf4j;
import ma.atm.authservice.service.AuthenticationService;
import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private Keycloak keycloakAdminClient;

    @Value("${keycloak.realm}")
    private String realm;

    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map> login(@RequestBody Map<String, String> credentials) {
        try {
            // Log the login attempt with timestamp
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            log.info("Login attempt at: {}", now.format(formatter));

            return authService.login(
                    credentials.get("username"),
                    credentials.get("password")
            );
        } catch (HttpClientErrorException e) {
            log.error("Login error: {}", e.getResponseBodyAsString());

            // Check for "Account is not fully set up" error
            if (e.getResponseBodyAsString().contains("Account is not fully set up")) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "SETUP_REQUIRED");
                response.put("error_description", "Account is not fully set up");
                response.put("username", credentials.get("username"));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Return the original error
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "error", e.getStatusCode().toString(),
                    "error_description", e.getResponseBodyAsString()
            ));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map> refreshToken(@RequestBody Map<String, String> tokenRequest) {
        return authService.refreshToken(tokenRequest.get("refresh_token"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> logoutRequest) {
        String refreshToken = logoutRequest.get("refresh_token");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Refresh token is required"));
        }

        return authService.logout(refreshToken);
    }

    @PostMapping("/check-user-profile")
    public ResponseEntity<?> checkUserProfile(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");

            if (username == null || username.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username is required"));
            }

            Map<String, Object> profileStatus = authService.checkUserProfile(username);
            return ResponseEntity.ok(profileStatus);
        } catch (Exception e) {
            log.error("Error checking user profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error checking user profile: " + e.getMessage()));
        }
    }

    @PostMapping("/setup-account")
    public ResponseEntity<?> setupAccount(@RequestBody Map<String, String> setupRequest) {
        try {
            // Log the request with timestamp
            log.info("Account setup request at: {}",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // Extract required fields
            String username = setupRequest.get("username");
            String currentPassword = setupRequest.get("currentPassword");
            String newPassword = setupRequest.get("newPassword");

            // Extract optional profile fields
            String email = setupRequest.get("email");
            String firstName = setupRequest.get("firstName");
            String lastName = setupRequest.get("lastName");

            // Validate request
            if (username == null || newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing required fields: username and newPassword"));
            }

            // Process the account setup
            Map<String, Object> result = authService.setupAccount(
                    username,
                    currentPassword,
                    newPassword,
                    email,
                    firstName,
                    lastName
            );

            log.info("Account setup successful for username: {}", username);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            // Handle validation errors
            log.warn("Account setup validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (HttpClientErrorException e) {
            // Handle Keycloak API errors
            log.error("Keycloak error during account setup: {}", e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", "Account setup failed",
                            "details", e.getResponseBodyAsString()));
        } catch (Exception e) {
            // Handle other errors
            log.error("Error during account setup", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Account setup failed: " + e.getMessage()));
        }
    }

    /**
     * Check if a user exists without validation
     */
    @PostMapping("/check-user-exists")
    public ResponseEntity<?> checkUserExists(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");

            if (username == null || username.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username is required"));
            }

            // Check if user exists in Keycloak
            String userId = authService.getUserIdByUsername(username);
            boolean exists = userId != null;

            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            log.error("Error checking if user exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error checking user: " + e.getMessage()));
        }
    }
    /**
     * Check if the current session is still valid
     * This endpoint specifically checks if the session has been terminated by an admin
     */
    @GetMapping("/session-status")
    public ResponseEntity<?> checkSessionStatus(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract the token
            String token = authHeader.replace("Bearer ", "");

            // Check if the session is still valid
            boolean isValid = isSessionValid(token);

            return ResponseEntity.ok(Map.of(
                    "valid", isValid,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("Error checking session status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "valid", false,
                            "error", "Session invalid: " + e.getMessage(),
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * Check if a session is still valid in Keycloak
     */
    private boolean isSessionValid(String token) {
        try {
            // Decode the token to get the session ID and user ID
            AccessToken accessToken = TokenVerifier.create(token, AccessToken.class).getToken();
            String sessionId = accessToken.getSessionId();
            String userId = accessToken.getSubject();

            if (sessionId == null || userId == null) {
                log.warn("Token doesn't contain session or user information");
                return false;
            }

            // Get the user's active sessions
            List<UserSessionRepresentation> sessions = keycloakAdminClient
                    .realm(realm)  // Use the injected realm value
                    .users()
                    .get(userId)
                    .getUserSessions();

            // If there are no sessions, it has been terminated
            if (sessions.isEmpty()) {
                log.info("No active sessions found for user {}", userId);
                return false;
            }

            // Check if the session ID exists in the active sessions
            boolean sessionFound = sessions.stream()
                    .anyMatch(session -> sessionId.equals(session.getId()));

            if (!sessionFound) {
                log.info("Session {} not found for user {}, it may have been terminated", sessionId, userId);
                return false;
            }

            // Session exists and is valid
            return true;
        } catch (Exception e) {
            log.error("Error validating session: {}", e.getMessage(), e);
            // Consider any error as an invalid session to be safe
            return false;
        }
    }
}
