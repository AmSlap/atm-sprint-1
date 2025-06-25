package ma.atm.jbpmincidentservice.model;


import jakarta.persistence.*;
import lombok.*;
import ma.atm.jbpmincidentservice.model.enums.IncidentStatus;
import ma.atm.jbpmincidentservice.model.enums.IncidentType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"tasks"})
@ToString(exclude = {"tasks"})
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_number", unique = true, nullable = false)
    private String incidentNumber;

    @Column(name = "process_instance_id", unique = true, nullable = false)
    private Long processInstanceId;

    @Column(name = "atm_id", nullable = false)
    private String atmId;

    @Column(name = "error_type", nullable = false)
    private String errorType;

    @Column(name = "incident_description", columnDefinition = "TEXT")
    private String incidentDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private IncidentStatus status = IncidentStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type")
    @Builder.Default
    private IncidentType incidentType = IncidentType.NOT_CLASSIFIED;

    @Column(name = "initial_diagnosis", columnDefinition = "TEXT")
    private String initialDiagnosis;

    @Column(name = "assessment_details", columnDefinition = "TEXT")
    private String assessmentDetails;

    @Column(name = "supplier_ticket_number")
    private String supplierTicketNumber;

    @Column(name = "reimbursement_details", columnDefinition = "TEXT")
    private String reimbursementDetails;

    @Column(name = "procurement_details", columnDefinition = "TEXT")
    private String procurementDetails;

    @Column(name = "resolution_details", columnDefinition = "TEXT")
    private String resolutionDetails;

    @Column(name = "closure_details", columnDefinition = "TEXT")
    private String closureDetails;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "assigned_to")
    private String assignedTo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<IncidentTask> tasks = new ArrayList<>();

    // Helper methods
    public void addTask(IncidentTask task) {
        tasks.add(task);
        task.setIncident(this);
    }

    public void removeTask(IncidentTask task) {
        tasks.remove(task);
        task.setIncident(null);
    }
}
