package ma.atm.atmstateservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtmConfigurationDto {
    private String atmId;
    private String overallHealth;
    private Map<String,Object> peripheralDetails; // Expose the detailed JSON
    private OffsetDateTime lastUpdateTimestamp;
}