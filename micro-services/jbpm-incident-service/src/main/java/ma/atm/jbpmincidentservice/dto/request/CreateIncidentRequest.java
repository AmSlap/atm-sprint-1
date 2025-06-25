package ma.atm.jbpmincidentservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateIncidentRequest {

    @NotBlank(message = "ATM ID is required")
    @Size(max = 50, message = "ATM ID must not exceed 50 characters")
    private String atmId;

    @NotBlank(message = "Error type is required")
    @Size(max = 100, message = "Error type must not exceed 100 characters")
    private String errorType;

    @NotBlank(message = "Incident description is required")
    @Size(max = 1000, message = "Incident description must not exceed 1000 characters")
    private String incidentDescription;

    @NotBlank(message = "Created by is required")
    @Size(max = 50, message = "Created by must not exceed 50 characters")
    private String createdBy;
}