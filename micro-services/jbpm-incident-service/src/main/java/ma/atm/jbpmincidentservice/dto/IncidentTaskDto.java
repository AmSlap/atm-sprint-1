package ma.atm.jbpmincidentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.atm.jbpmincidentservice.model.enums.IncidentStatus;
import ma.atm.jbpmincidentservice.model.enums.IncidentType;
import ma.atm.jbpmincidentservice.model.enums.TaskStatus;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentTaskDto {
    // Existing task fields
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

    // New incident context fields
    private Long incidentId;
    private String incidentNumber;
    private String atmId;
    private String errorType;
    private String incidentDescription;
    private IncidentStatus incidentStatus;
    private IncidentType incidentType;
    private LocalDateTime incidentCreatedAt;
    private String incidentCreatedBy;
}