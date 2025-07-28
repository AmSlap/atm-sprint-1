package ma.atm.jbpmincidentservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.atm.jbpmincidentservice.dto.IncidentDto;
import ma.atm.jbpmincidentservice.dto.IncidentReport;
import ma.atm.jbpmincidentservice.dto.IncidentTaskDto;
import ma.atm.jbpmincidentservice.dto.request.*;
import ma.atm.jbpmincidentservice.dto.response.ApiResponse;
import ma.atm.jbpmincidentservice.jbpm.IncidentProcessService;
import ma.atm.jbpmincidentservice.model.enums.IncidentStatus;
import ma.atm.jbpmincidentservice.model.enums.TaskStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.config.Task;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200") // Angular dev server
public class IncidentController {

    private final IncidentProcessService incidentProcessService;

    // ====================== INCIDENT MANAGEMENT ENDPOINTS ======================

    /**
     * Create a new incident
     * POST /api/incidents
     */
    @PostMapping
    public ResponseEntity<ApiResponse<IncidentDto>> createIncident(@Valid @RequestBody CreateIncidentRequest request) {
        try {
            log.info("Creating new incident for ATM: {}", request.getAtmId());

            IncidentDto incident = incidentProcessService.startIncidentProcess(
                    request.getAtmId(),
                    request.getErrorType(),
                    request.getIncidentDescription(),
                    request.getCreatedBy()
            );

            return ResponseEntity.ok(ApiResponse.success(incident, "Incident created successfully"));

        } catch (Exception e) {
            log.error("Error creating incident", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create incident: " + e.getMessage()));
        }
    }

    /**
     * Get all incidents with pagination
     * GET /api/incidents?page=0&size=10&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<IncidentDto>>> getAllIncidents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<IncidentDto> incidents = incidentProcessService.getAllIncidents(pageable);

            return ResponseEntity.ok(ApiResponse.success(incidents, "Incidents retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving incidents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve incidents: " + e.getMessage()));
        }
    }


    /**
     * Get incident by ID
     * GET /api/incidents/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentDto>> getIncidentById(@PathVariable Long id) {
        try {
            // You'll need to add this method to your service
            IncidentDto incident = incidentProcessService.getIncidentById(id);

            return ResponseEntity.ok(ApiResponse.success(incident, "Incident retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving incident: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Incident not found: " + e.getMessage()));
        }
    }

    /**
     * Get incident by process instance ID
     * GET /api/incidents/process/{processInstanceId}
     */
    @GetMapping("/process/{processInstanceId}")
    public ResponseEntity<ApiResponse<IncidentReport>> getIncidentByProcessId(@PathVariable Long processInstanceId) {
        try {
            IncidentReport report = incidentProcessService.getIncidentReport(processInstanceId);

            return ResponseEntity.ok(ApiResponse.success(report, "Incident report retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving incident report for process: {}", processInstanceId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Incident not found: " + e.getMessage()));
        }
    }

    /**
     * Get incidents by status
     * GET /api/incidents/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<IncidentDto>>> getIncidentsByStatus(@PathVariable IncidentStatus status) {
        try {
            List<IncidentDto> incidents = incidentProcessService.getIncidentsByStatus(status);

            return ResponseEntity.ok(ApiResponse.success(incidents, "Incidents retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving incidents by status: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve incidents: " + e.getMessage()));
        }
    }

    /**
     * Abort an incident
     * DELETE /api/incidents/{processInstanceId}
     */
    @DeleteMapping("/{processInstanceId}")
    public ResponseEntity<ApiResponse<Void>> abortIncident(@PathVariable Long processInstanceId) {
        try {
            incidentProcessService.abortProcessInstance(processInstanceId);

            return ResponseEntity.ok(ApiResponse.success(null, "Incident aborted successfully"));

        } catch (Exception e) {
            log.error("Error aborting incident: {}", processInstanceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to abort incident: " + e.getMessage()));
        }
    }

    // ====================== TASK MANAGEMENT ENDPOINTS ======================

    /**
     * Get task details
     * GET /api/incidents/tasks/{taskInstanceId}
     */
    @GetMapping("/tasks/{taskInstanceId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTaskDetails(@PathVariable Long taskInstanceId) {
        try {
            Map<String, Object> taskDetails = incidentProcessService.getTaskInstance(taskInstanceId);

            return ResponseEntity.ok(ApiResponse.success(taskDetails, "Task details retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving task details: {}", taskInstanceId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Task not found: " + e.getMessage()));
        }
    }

    /**
     * Get tasks for potential owners (by group)
     * GET /api/incidents/tasks/available?group=helpdesk
     */

    @GetMapping("/tasks/available")
    public ResponseEntity<ApiResponse<List<IncidentTaskDto>>> getAvailableTasks(
            @RequestParam(required = false) String group) {
        try {
            // Change return type from raw Map to IncidentTaskDto
            List<Map<String, Object>> jbpmTasks = incidentProcessService.getTasksForPotentialOwners(group);

            List<IncidentTaskDto> tasks = jbpmTasks.stream()
                    .map(incidentProcessService::convertJbpmTaskToDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(tasks, "Available tasks retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving available tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve tasks: " + e.getMessage()));
        }
    }

    /**
     * Get user's tasks
     * GET /api/incidents/tasks/my-tasks?user=AmSlap
     */
    @GetMapping("/tasks/my-tasks")
    public ResponseEntity<ApiResponse<List<IncidentTaskDto>>> getUserTasks(@RequestParam String user) {
        try {
            List<IncidentTaskDto> tasks = incidentProcessService.getUserTasksWithContext(user);


            return ResponseEntity.ok(ApiResponse.success(tasks, "User tasks retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving user tasks for: {}", user, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve user tasks: " + e.getMessage()));
        }
    }

    /**
     * Get group's tasks
     * GET /api/incidents/tasks/group-tasks?group=helpdesk
     */

    @GetMapping("/tasks/group-tasks")
    public ResponseEntity<ApiResponse<List<IncidentTaskDto>>> getGroupTasks(@RequestParam String group) {
        try {
            List<IncidentTaskDto> tasks = incidentProcessService.getTasksForPotentialOwnersWithContext(group);


            return ResponseEntity.ok(ApiResponse.success(tasks, "Group tasks retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving group tasks for: {}", group, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve group tasks: " + e.getMessage()));
        }
    }







    /**
     * Get task input data
     * GET /api/incidents/tasks/{taskInstanceId}/input
     */
    @GetMapping("/tasks/{taskInstanceId}/input")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTaskInputData(@PathVariable Long taskInstanceId) {
        try {
            Map<String, Object> inputData = incidentProcessService.getTaskInputData(taskInstanceId);

            return ResponseEntity.ok(ApiResponse.success(inputData, "Task input data retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving task input data: {}", taskInstanceId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Task input data not found: " + e.getMessage()));
        }
    }

    @GetMapping("/tasks/user-tasks-by-status")
    public ResponseEntity<ApiResponse<List<IncidentTaskDto>>> getUserTasksByStatus(
            @RequestParam String user,
            @RequestParam TaskStatus status) {
        try {
            List<IncidentTaskDto> tasks = incidentProcessService.getUserTasksByStatus(user, status);

            return ResponseEntity.ok(ApiResponse.success(tasks, "User tasks by status retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving user tasks by status: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve user tasks by status: " + e.getMessage()));
        }
    }

    // ====================== TASK COMPLETION ENDPOINTS ======================

    /**
     * Complete Process Incident task (Helpdesk)
     * POST /api/incidents/tasks/{taskInstanceId}/complete/process-incident
     */
    @PostMapping("/tasks/{taskInstanceId}/complete/process-incident")
    public ResponseEntity<ApiResponse<Void>> completeProcessIncidentTask(
            @PathVariable Long taskInstanceId,
            @Valid @RequestBody ProcessIncidentRequest request) {
        try {
            incidentProcessService.completeProcessIncidentTask(
                    taskInstanceId,
                    request.getUser(),
                    request.getInitialDiagnosis()
            );

            return ResponseEntity.ok(ApiResponse.success(null, "Process Incident task completed successfully"));

        } catch (Exception e) {
            log.error("Error completing Process Incident task: {}", taskInstanceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to complete task: " + e.getMessage()));
        }
    }

    /**
     * Complete Analyze Incident task (ATM Monitoring)
     * POST /api/incidents/tasks/{taskInstanceId}/complete/analyze-incident
     */
    @PostMapping("/tasks/{taskInstanceId}/complete/analyze-incident")
    public ResponseEntity<ApiResponse<Void>> completeAnalyzeIncidentTask(
            @PathVariable Long taskInstanceId,
            @Valid @RequestBody AnalyzeIncidentRequest request) {
        try {
            incidentProcessService.completeAnalyzeIncidentTask(
                    taskInstanceId,
                    request.getUser(),
                    request.getIncidentType()
            );

            return ResponseEntity.ok(ApiResponse.success(null, "Analyze Incident task completed successfully"));

        } catch (Exception e) {
            log.error("Error completing Analyze Incident task: {}", taskInstanceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to complete task: " + e.getMessage()));
        }
    }

    /**
     * Complete Assess Incident task (Supplier)
     * POST /api/incidents/tasks/{taskInstanceId}/complete/assess-incident
     */
    @PostMapping("/tasks/{taskInstanceId}/complete/assess-incident")
    public ResponseEntity<ApiResponse<Void>> completeAssessIncidentTask(
            @PathVariable Long taskInstanceId,
            @Valid @RequestBody AssessIncidentRequest request) {
        try {
            incidentProcessService.completeAssessIncidentTask(
                    taskInstanceId,
                    request.getUser(),
                    request.getAssessmentDetails(),
                    request.getSupplierTicketNumber()
            );

            return ResponseEntity.ok(ApiResponse.success(null, "Assess Incident task completed successfully"));

        } catch (Exception e) {
            log.error("Error completing Assess Incident task: {}", taskInstanceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to complete task: " + e.getMessage()));
        }
    }

    /**
     * Complete Approve Insurance task (Insurance)
     * POST /api/incidents/tasks/{taskInstanceId}/complete/approve-insurance
     */
    @PostMapping("/tasks/{taskInstanceId}/complete/approve-insurance")
    public ResponseEntity<ApiResponse<Void>> completeApproveInsuranceTask(
            @PathVariable Long taskInstanceId,
            @Valid @RequestBody ApproveInsuranceRequest request) {
        try {
            incidentProcessService.completeApproveInsuranceTask(
                    taskInstanceId,
                    request.getUser(),
                    request.getReimbursementDetails()
            );

            return ResponseEntity.ok(ApiResponse.success(null, "Approve Insurance task completed successfully"));

        } catch (Exception e) {
            log.error("Error completing Approve Insurance task: {}", taskInstanceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to complete task: " + e.getMessage()));
        }
    }

    /**
     * Complete Procure Items task (Purchasing)
     * POST /api/incidents/tasks/{taskInstanceId}/complete/procure-items
     */
    @PostMapping("/tasks/{taskInstanceId}/complete/procure-items")
    public ResponseEntity<ApiResponse<Void>> completeProcureItemsTask(
            @PathVariable Long taskInstanceId,
            @Valid @RequestBody ProcureItemsRequest request) {
        try {
            incidentProcessService.completeProcureItemsTask(
                    taskInstanceId,
                    request.getUser(),
                    request.getProcurementDetails()
            );

            return ResponseEntity.ok(ApiResponse.success(null, "Procure Items task completed successfully"));

        } catch (Exception e) {
            log.error("Error completing Procure Items task: {}", taskInstanceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to complete task: " + e.getMessage()));
        }
    }

    /**
     * Complete Resolve Incident Under Maintenance task (Supplier)
     * POST /api/incidents/tasks/{taskInstanceId}/complete/resolve-maintenance
     */
    @PostMapping("/tasks/{taskInstanceId}/complete/resolve-maintenance")
    public ResponseEntity<ApiResponse<Void>> completeResolveMaintenanceTask(
            @PathVariable Long taskInstanceId,
            @Valid @RequestBody ResolveIncidentRequest request) {
        try {
            incidentProcessService.completeResolveIncidentUnderMaintenanceTask(
                    taskInstanceId,
                    request.getUser(),
                    request.getResolutionDetails(),
                    request.getSupplierTicketNumber()
            );

            return ResponseEntity.ok(ApiResponse.success(null, "Resolve Maintenance task completed successfully"));

        } catch (Exception e) {
            log.error("Error completing Resolve Maintenance task: {}", taskInstanceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to complete task: " + e.getMessage()));
        }
    }

    /**
     * Complete Resolve Incident task (Supplier)
     * POST /api/incidents/tasks/{taskInstanceId}/complete/resolve-incident
     */
    @PostMapping("/tasks/{taskInstanceId}/complete/resolve-incident")
    public ResponseEntity<ApiResponse<Void>> completeResolveIncidentTask(
            @PathVariable Long taskInstanceId,
            @Valid @RequestBody ResolveIncidentRequest request) {
        try {
            incidentProcessService.completeResolveIncidentTask(
                    taskInstanceId,
                    request.getUser(),
                    request.getResolutionDetails(),
                    request.getSupplierTicketNumber()
            );

            return ResponseEntity.ok(ApiResponse.success(null, "Resolve Incident task completed successfully"));

        } catch (Exception e) {
            log.error("Error completing Resolve Incident task: {}", taskInstanceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to complete task: " + e.getMessage()));
        }
    }

    /**
     * Complete Close Incident task (ATM Monitoring)
     * POST /api/incidents/tasks/{taskInstanceId}/complete/close-incident
     */
    @PostMapping("/tasks/{taskInstanceId}/complete/close-incident")
    public ResponseEntity<ApiResponse<Void>> completeCloseIncidentTask(
            @PathVariable Long taskInstanceId,
            @Valid @RequestBody CloseIncidentRequest request) {
        try {
            incidentProcessService.completeCloseIncidentTask(
                    taskInstanceId,
                    request.getUser(),
                    request.getClosureDetails()
            );

            return ResponseEntity.ok(ApiResponse.success(null, "Close Incident task completed successfully"));

        } catch (Exception e) {
            log.error("Error completing Close Incident task: {}", taskInstanceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to complete task: " + e.getMessage()));
        }
    }

    // ====================== TASK ACTION ENDPOINTS ======================

    /**
     * Claim a task
     * POST /api/incidents/tasks/{taskInstanceId}/claim
     */
    @PostMapping("/tasks/{taskInstanceId}/claim")
    public ResponseEntity<ApiResponse<Void>> claimTask(
            @PathVariable Long taskInstanceId,
            @Valid @RequestBody ClaimTaskRequest request) {
        try {
            incidentProcessService.claimTask(taskInstanceId, request.getUser());

            return ResponseEntity.ok(ApiResponse.success(null, "Task claimed successfully"));

        } catch (Exception e) {
            log.error("Error claiming task: {}", taskInstanceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to claim task: " + e.getMessage()));
        }
    }

    /**
     * Release a task
     * POST /api/incidents/tasks/{taskInstanceId}/release
     */
    @PostMapping("/tasks/{taskInstanceId}/release")
    public ResponseEntity<ApiResponse<Void>> releaseTask(
            @PathVariable Long taskInstanceId,
            @Valid @RequestBody ReleaseTaskRequest request) {
        try {
            incidentProcessService.releaseTask(taskInstanceId, request.getUser());

            return ResponseEntity.ok(ApiResponse.success(null, "Task released successfully"));

        } catch (Exception e) {
            log.error("Error releasing task: {}", taskInstanceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to release task: " + e.getMessage()));
        }
    }

    @PostMapping("/tasks/{taskInstanceId}/start")
    public ResponseEntity<ApiResponse<Void>> startTask(
            @PathVariable Long taskInstanceId,
            @Valid @RequestBody StartTaskRequest request) {
        try {
            incidentProcessService.startTask(taskInstanceId, request.getUser());

            return ResponseEntity.ok(ApiResponse.success(null, "Task started successfully"));

        } catch (Exception e) {
            log.error("Error starting task: {}", taskInstanceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to start task: " + e.getMessage()));
        }
    }

    /**
     * Get process diagram as SVG
     * GET /api/incidents/process/{processInstanceId}/diagram
     */
    @GetMapping(value = "/process/{processInstanceId}/diagram", produces = "image/svg+xml")
    public ResponseEntity<String> getProcessDiagram(@PathVariable Long processInstanceId) {
        try {
            String svgDiagram = incidentProcessService.getProcessDiagram(processInstanceId);

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("image/svg+xml"))
                    .body(svgDiagram);

        } catch (Exception e) {
            log.error("Error retrieving process diagram for: {}", processInstanceId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("<svg><text>Diagram not found</text></svg>");
        }
    }


}
