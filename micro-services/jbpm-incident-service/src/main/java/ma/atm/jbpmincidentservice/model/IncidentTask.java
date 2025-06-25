package ma.atm.jbpmincidentservice.model;


import jakarta.persistence.*;
import lombok.*;
import ma.atm.jbpmincidentservice.model.enums.TaskStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"incident"})
@ToString(exclude = {"incident"})
public class IncidentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(name = "task_instance_id", unique = true, nullable = false)
    private Long taskInstanceId;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(name = "task_description")
    private String taskDescription;

    @Column(name = "assigned_group")
    private String assignedGroup;

    @Column(name = "assigned_user")
    private String assignedUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.CREATED;

    @Column(name = "priority")
    private Integer priority;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData;

    @Column(name = "output_data", columnDefinition = "TEXT")
    private String outputData;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
}
