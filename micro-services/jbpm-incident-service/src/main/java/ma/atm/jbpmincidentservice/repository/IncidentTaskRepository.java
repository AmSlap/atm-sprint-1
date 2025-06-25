package ma.atm.jbpmincidentservice.repository;

import ma.atm.jbpmincidentservice.model.IncidentTask;
import ma.atm.jbpmincidentservice.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentTaskRepository extends JpaRepository<IncidentTask, Long> {

    Optional<IncidentTask> findByTaskInstanceId(Long taskInstanceId);

    List<IncidentTask> findByIncidentId(Long incidentId);

    List<IncidentTask> findByAssignedUser(String assignedUser);

    List<IncidentTask> findByAssignedGroup(String assignedGroup);

    List<IncidentTask> findByStatus(TaskStatus status);

    List<IncidentTask> findByTaskName(String taskName);

    @Query("SELECT t FROM IncidentTask t WHERE t.assignedUser = :user AND t.status IN :statuses")
    List<IncidentTask> findByAssignedUserAndStatusIn(@Param("user") String user,
                                                     @Param("statuses") List<TaskStatus> statuses);

    @Query("SELECT t FROM IncidentTask t WHERE t.assignedGroup = :group AND t.status IN :statuses")
    List<IncidentTask> findByAssignedGroupAndStatusIn(@Param("group") String group,
                                                      @Param("statuses") List<TaskStatus> statuses);

    @Query("SELECT t FROM IncidentTask t WHERE t.incident.processInstanceId = :processInstanceId ORDER BY t.createdAt")
    List<IncidentTask> findByProcessInstanceIdOrderByCreatedAt(@Param("processInstanceId") Long processInstanceId);

    @Query("SELECT COUNT(t) FROM IncidentTask t WHERE t.assignedGroup = :group AND t.status = :status")
    Long countByAssignedGroupAndStatus(@Param("group") String group, @Param("status") TaskStatus status);
}
