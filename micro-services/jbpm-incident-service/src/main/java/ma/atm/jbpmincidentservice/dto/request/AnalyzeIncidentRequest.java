package ma.atm.jbpmincidentservice.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
public class AnalyzeIncidentRequest {

    @NotBlank(message = "User is required")
    private String user;

    @NotBlank(message = "Incident type is required")
    @Pattern(regexp = "under_maintenance|outside_maintenance_under_insurance|outside_maintenance_outside_insurance",
            message = "Invalid incident type")
    private String incidentType;
}
