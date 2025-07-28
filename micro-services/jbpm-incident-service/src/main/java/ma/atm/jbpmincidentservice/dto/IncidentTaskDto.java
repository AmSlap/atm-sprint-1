package ma.atm.jbpmincidentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.atm.jbpmincidentservice.model.enums.IncidentStatus;
import ma.atm.jbpmincidentservice.model.enums.TaskStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentTaskDto {
    private Long id;
    private Long taskInstanceId;
    private String taskName;
    private String taskDescription;
    private String assignedGroup;
    private String assignedUser;
    private TaskStatus status;
    private Integer priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime claimedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime dueDate;
    private String inputData;
    private String outputData;
    private String comments;
    private String incidentId;

    // Add incident context fields
    private String atmId;
    private String incidentDescription;
    private String errorType;
    private IncidentStatus incidentStatus;
    private String currentJbpmStatus; // Real-time status from jBPM

    // Add computed fields for frontend
    private String displayName; // e.g., "Process Incident - ATM001 - Network Error"
    private String contextSummary; // e.g., "ATM001: Network connectivity issue"
}
