package ma.atm.jbpmincidentservice.repository;


import ma.atm.jbpmincidentservice.model.Incident;
import ma.atm.jbpmincidentservice.model.enums.IncidentStatus;
import ma.atm.jbpmincidentservice.model.enums.IncidentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findByProcessInstanceId(Long processInstanceId);

    Optional<Incident> findByIncidentNumber(String incidentNumber);

    List<Incident> findByAtmId(String atmId);

    List<Incident> findByStatus(IncidentStatus status);

    List<Incident> findByIncidentType(IncidentType incidentType);

    List<Incident> findByCreatedBy(String createdBy);

    List<Incident> findByAssignedTo(String assignedTo);

    Page<Incident> findByStatusIn(List<IncidentStatus> statuses, Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE i.createdAt BETWEEN :startDate AND :endDate")
    List<Incident> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT i FROM Incident i WHERE i.atmId = :atmId AND i.status IN :statuses")
    List<Incident> findByAtmIdAndStatusIn(@Param("atmId") String atmId,
                                          @Param("statuses") List<IncidentStatus> statuses);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.status = :status")
    Long countByStatus(@Param("status") IncidentStatus status);

    @Query("SELECT i FROM Incident i WHERE i.errorType = :errorType AND i.createdAt >= :since")
    List<Incident> findRecentIncidentsByErrorType(@Param("errorType") String errorType,
                                                  @Param("since") LocalDateTime since);
}
