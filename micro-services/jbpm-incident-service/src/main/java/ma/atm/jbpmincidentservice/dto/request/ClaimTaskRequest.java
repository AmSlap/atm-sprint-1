package ma.atm.jbpmincidentservice.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ClaimTaskRequest {

    @NotBlank(message = "User is required")
    private String user;
}