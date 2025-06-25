package ma.atm.jbpmincidentservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.atm.jbpmincidentservice.model.enums.IncidentStatus;
import ma.atm.jbpmincidentservice.model.enums.IncidentType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentDto {
    private Long id;
    private String incidentNumber;
    private Long processInstanceId;
    private String atmId;
    private String errorType;
    private String incidentDescription;
    private IncidentStatus status;
    private IncidentType incidentType;
    private String initialDiagnosis;
    private String assessmentDetails;
    private String supplierTicketNumber;
    private String reimbursementDetails;
    private String procurementDetails;
    private String resolutionDetails;
    private String closureDetails;
    private String createdBy;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private List<IncidentTaskDto> tasks;
}
