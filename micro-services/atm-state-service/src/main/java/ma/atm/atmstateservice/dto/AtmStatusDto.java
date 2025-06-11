package ma.atm.atmstateservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtmStatusDto {
    private String atmId;
    private String operationalState;
    private Boolean maintenanceMode;
    private OffsetDateTime lastSuccessfulConnection;
    private OffsetDateTime lastSuccessfulTransaction;
    private OffsetDateTime lastUpdateTimestamp;
}