package ma.atm.atmstateservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtmStateSummaryDto {
    private String atmId;
    private String operationalState;
    private String overallHealth;
    private Boolean lowCashFlag;
    private OffsetDateTime lastUpdateTimestamp;
}