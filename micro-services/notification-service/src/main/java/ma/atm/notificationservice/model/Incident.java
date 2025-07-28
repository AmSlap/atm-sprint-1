package ma.atm.notificationservice.model;


import jakarta.persistence.*;
import lombok.*;
import ma.atm.notificationservice.model.enums.IncidentStatus;
import ma.atm.notificationservice.model.enums.IncidentType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {
    private Long id;

    private String incidentNumber;

    private Long processInstanceId;

    private String atmId;

    private String errorType;

    private String incidentDescription;

    private IncidentStatus status = IncidentStatus.CREATED;

    @Enumerated(EnumType.STRING)
    private IncidentType incidentType = IncidentType.NOT_CLASSIFIED;

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
}
