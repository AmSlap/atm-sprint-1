package ma.atm.atmstateservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtmFullStateDto {
    private String atmId;
    private AtmStatusDto status;
    private AtmConfigurationDto configuration;
    private AtmCounterDto counters;
}