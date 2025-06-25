package ma.atm.jbpmincidentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentStatistics {
    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private long totalDurationMinutes;
    private String currentStep;
    private double completionPercentage;
}