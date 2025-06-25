package ma.atm.jbpmincidentservice.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class ProcureItemsRequest {

    @NotBlank(message = "User is required")
    private String user;

    @NotBlank(message = "Procurement details are required")
    @Size(max = 1000, message = "Procurement details must not exceed 1000 characters")
    private String procurementDetails;
}