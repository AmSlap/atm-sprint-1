package ma.atm.jbpmincidentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentReport {
    private IncidentDto incident;
    private Map<String, Object> currentProcessVariables;
    private List<Map<String, Object>> processHistory;
    private List<IncidentTaskDto> tasks;
    private IncidentStatistics statistics;
}

