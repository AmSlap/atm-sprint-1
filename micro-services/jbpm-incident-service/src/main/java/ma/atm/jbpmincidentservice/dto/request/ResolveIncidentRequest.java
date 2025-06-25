package ma.atm.jbpmincidentservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResolveIncidentRequest {

    @NotBlank(message = "User is required")
    private String user;

    @NotBlank(message = "Resolution details are required")
    @Size(max = 1000, message = "Resolution details must not exceed 1000 characters")
    private String resolutionDetails;

    @NotBlank(message = "Supplier ticket number is required")
    @Size(max = 100, message = "Supplier ticket number must not exceed 100 characters")
    private String supplierTicketNumber;
}