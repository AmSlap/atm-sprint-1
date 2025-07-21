package ma.atm.jbpmincidentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
}
