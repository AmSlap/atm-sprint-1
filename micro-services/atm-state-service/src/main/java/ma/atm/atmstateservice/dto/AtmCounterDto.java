package ma.atm.atmstateservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtmCounterDto {
    private String atmId;
    private Double totalCashAvailable;
    private Boolean lowCashFlag;
    private Integer rejectBinPercentageFull;
    private List<CassetteDto> cassettes;
    private OffsetDateTime lastUpdateTimestamp;
}
