package ma.atm.jbpmincidentservice.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class AssessIncidentRequest {

    @NotBlank(message = "User is required")
    private String user;

    @NotBlank(message = "Assessment details are required")
    @Size(max = 1000, message = "Assessment details must not exceed 1000 characters")
    private String assessmentDetails;

    @NotBlank(message = "Supplier ticket number is required")
    @Size(max = 100, message = "Supplier ticket number must not exceed 100 characters")
    private String supplierTicketNumber;
}