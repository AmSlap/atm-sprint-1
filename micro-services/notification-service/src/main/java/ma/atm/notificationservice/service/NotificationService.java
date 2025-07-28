package ma.atm.notificationservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import ma.atm.notificationservice.dto.AgencyDto;
import ma.atm.notificationservice.dto.AtmInfoDto;
import ma.atm.notificationservice.model.Incident;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class NotificationService {

    private final String ATM_REGISTRY_BRANCH_URL = "http://localhost:8082/api/registry/agencies/";
    private final String ATM_REGISTRY_ATM_URL = "http://localhost:8082/api/registry/atms/";

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * This method sends an email notification about an incident reported on an ATM.
     * It fetches the ATM details and the agency contact email from the ATM registry service.
     *
     * @param incidentDetails Details about the incident.
     */
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void notifyIncident(Incident incidentDetails) throws MessagingException {
        // Fetch ATM details from the ATM registry service
        String atmUrl = ATM_REGISTRY_ATM_URL + incidentDetails.getAtmId();
        AtmInfoDto atmInfo = restTemplate.getForObject(atmUrl, AtmInfoDto.class);
        String agencyCode = atmInfo.getAgencyCode();
        AgencyDto agency = restTemplate.getForObject(ATM_REGISTRY_BRANCH_URL + agencyCode, AgencyDto.class);
        String recipientEmail = agency.getContactEmail();
        String contactName = agency.getContactPerson() != null ? agency.getContactPerson() : "Agency Contact";

        // Format date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createdAt = incidentDetails.getCreatedAt() != null ? incidentDetails.getCreatedAt().format(formatter) : "N/A";
        String createdBy = incidentDetails.getCreatedBy() != null ? incidentDetails.getCreatedBy() : "System";
        String processInstanceId = incidentDetails.getProcessInstanceId() != null ? incidentDetails.getProcessInstanceId().toString() : "N/A";
        String incidentDescription = incidentDetails.getIncidentDescription() != null ? incidentDetails.getIncidentDescription() : "No description provided";


        // Create MIME message for HTML and plain text
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        // Set email properties
        helper.setTo(recipientEmail);
        helper.setSubject("New Incident Reported: " + incidentDetails.getIncidentNumber());
        helper.setFrom("amslap.as@gmail.com");

        // Plain text content
        String plainText = String.format(
                "Dear %s,\n\n" +
                        "A new incident has been reported for one of your ATMs. Please review the details below and take appropriate action.\n\n" +
                        "Incident Details:\n" +
                        "- Incident Number: %s\n" +
                        "- ATM ID: %s\n" +
                        "- Incident Type: %s\n" +
                        "- Status: %s\n" +
                        "- Description: %s\n" +
                        "- Created By: %s\n" +
                        "- Created At: %s\n" +
                        "- Process Instance ID: %s\n\n" +
                        "For more details, please access the ATM Management System. \n\n" +
                        "Thank you,\n" +
                        "ATM Management Team\n" +
                        "GTS\n" +
                        "rabat Agdal\n\n" +
                        "This is an automated notification. Please do not reply directly to this email.",
                contactName,
                incidentDetails.getIncidentNumber(),
                incidentDetails.getAtmId(),
                incidentDetails.getErrorType(),
                incidentDetails.getStatus(),
                incidentDescription,
                createdBy,
                createdAt,
                processInstanceId,
                incidentDetails.getId()
        );

        // HTML content
        String htmlContent = String.format(
                "<!DOCTYPE html>" +
                        "<html lang=\"en\">" +
                        "<head>" +
                        "<meta charset=\"UTF-8\">" +
                        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                        "<style>" +
                        "body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; margin: 0; padding: 0; }" +
                        ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
                        ".header { background-color: #1976d2; color: #fff; padding: 10px 20px; text-align: center; }" +
                        ".header h1 { margin: 0; font-size: 24px; }" +
                        ".content { padding: 20px; background-color: #fff; border-radius: 5px; }" +
                        ".content h2 { color: #1976d2; font-size: 18px; margin-top: 0; }" +
                        ".details { width: 100%%; border-collapse: collapse; margin: 20px 0; }" +
                        ".details th, .details td { padding: 8px; border: 1px solid #ddd; text-align: left; }" +
                        ".details th { background-color: #f2f2f2; }" +
                        ".footer { text-align: center; font-size: 12px; color: #666; margin-top: 20px; }" +
                        ".footer a { color: #1976d2; text-decoration: none; }" +
                        ".button { display: inline-block; padding: 10px 20px; background-color: #1976d2; color: #fff; text-decoration: none; border-radius: 5px; margin: 10px 0; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class=\"container\">" +
                        "<div class=\"header\">" +
                        "<h1>New Incident Reported: %s</h1>" +
                        "</div>" +
                        "<div class=\"content\">" +
                        "<p>Dear %s,</p>" +
                        "<p>A new incident has been reported for one of your ATMs. Please review the details below and take appropriate action.</p>" +
                        "<h2>Incident Details</h2>" +
                        "<table class=\"details\">" +
                        "<tr><th>Incident Number</th><td>%s</td></tr>" +
                        "<tr><th>ATM ID</th><td>%s</td></tr>" +
                        "<tr><th>Incident Type</th><td>%s</td></tr>" +
                        "<tr><th>Status</th><td>%s</td></tr>" +
                        "<tr><th>Description</th><td>%s</td></tr>" +
                        "<tr><th>Created By</th><td>%s</td></tr>" +
                        "<tr><th>Created At</th><td>%s</td></tr>" +
                        "<tr><th>Process Instance ID</th><td>%s</td></tr>" +
                        "</table>" +
                        "<p>" +
//                        "<a href=\"http://your-app-url/incidents/%d\" class=\"button\">View Incident Details</a>" +
                        "</p>" +
                        "</div>" +
                        "<div class=\"footer\">" +
                        "<p>ATM Management Team<br>" +
                        "GTS<br>" +
                        "Rabat Agdal</p>" +
              //          "<p><a href=\"http://your-app-url/unsubscribe?email=%s\">Unsubscribe</a> | This is an automated notification. Please do not reply directly.</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                incidentDetails.getIncidentNumber(),
                contactName,
                incidentDetails.getIncidentNumber(),
                incidentDetails.getAtmId(),
                incidentDetails.getErrorType(),
                incidentDetails.getStatus(),
                incidentDescription,
                createdBy,
                createdAt,
                processInstanceId,
                incidentDetails.getId(),
                recipientEmail
        );

        // Set email content
        helper.setText(plainText, htmlContent);


        try {
            mailSender.send(mimeMessage);
            log.info("Sent notification for incident {} to {}", incidentDetails.getIncidentNumber(), recipientEmail);
        } catch (MailException e) {
            log.error("Failed to send notification for incident {}: {}", incidentDetails.getIncidentNumber(), e.getMessage());
            throw e;
        }
    }





}