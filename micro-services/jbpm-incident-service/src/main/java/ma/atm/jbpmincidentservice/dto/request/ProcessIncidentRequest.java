package ma.atm.jbpmincidentservice.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class ProcessIncidentRequest {

    @NotBlank(message = "User is required")
    private String user;

    @NotBlank(message = "Initial diagnosis is required")
    @Size(max = 1000, message = "Initial diagnosis must not exceed 1000 characters")
    private String initialDiagnosis;
}