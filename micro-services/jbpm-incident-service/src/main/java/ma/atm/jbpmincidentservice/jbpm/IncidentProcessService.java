package ma.atm.jbpmincidentservice.jbpm;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import ma.atm.jbpmincidentservice.dto.IncidentDto;
import ma.atm.jbpmincidentservice.dto.IncidentReport;
import ma.atm.jbpmincidentservice.dto.IncidentStatistics;
import ma.atm.jbpmincidentservice.dto.IncidentTaskDto;
import ma.atm.jbpmincidentservice.model.Incident;
import ma.atm.jbpmincidentservice.model.IncidentTask;
import ma.atm.jbpmincidentservice.model.enums.IncidentStatus;
import ma.atm.jbpmincidentservice.model.enums.IncidentType;
import ma.atm.jbpmincidentservice.model.enums.TaskStatus;
import ma.atm.jbpmincidentservice.repository.IncidentRepository;
import ma.atm.jbpmincidentservice.repository.IncidentTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IncidentProcessService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IncidentProcessService.class);

    @Value("${kie.server.url}")
    private String KIE_SERVER_URL;
    @Value("${kie.server.username}")
    private String USERNAME;
    @Value("${kie.server.password}")
    private String PASSWORD;
    @Value("${kie.container.id}")
    private String CONTAINER_ID;
    @Value("${kie.process.id}")
    private String PROCESS_ID;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Database repositories
    private final IncidentRepository incidentRepository;
    private final IncidentTaskRepository taskRepository;

    public IncidentProcessService(IncidentRepository incidentRepository, IncidentTaskRepository taskRepository) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.incidentRepository = incidentRepository;
        this.taskRepository = taskRepository;
    }

    // ====================== CONSTANTS FOR INCIDENT TYPES ======================
    public static final String INCIDENT_TYPE_UNDER_MAINTENANCE = "under_maintenance";
    public static final String INCIDENT_TYPE_OUTSIDE_MAINTENANCE_UNDER_INSURANCE = "outside_maintenance_under_insurance";
    public static final String INCIDENT_TYPE_OUTSIDE_MAINTENANCE_OUTSIDE_INSURANCE = "outside_maintenance_outside_insurance";

    // ====================== PROCESS OPERATIONS WITH PERSISTENCE ======================

    /**
     * Starts a new incident management process with database tracking
     * POST /server/containers/{containerId}/processes/{processId}/instances
     */
    public IncidentDto startIncidentProcess(String atmId, String errorType, String incidentDescription) {
        return startIncidentProcess(atmId, errorType, incidentDescription, "system");
    }

    public IncidentDto startIncidentProcess(String atmId, String errorType, String incidentDescription, String createdBy) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/processes/" + PROCESS_ID + "/instances";
            String incidentNumber = generateIncidentNumber();
            Map<String, Object> processVariables = Map.of(
                    "atmId", atmId,
                    "errorType", errorType,
                    "incidentDescription", incidentDescription,
                    "incidentNumber", incidentNumber
            );

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(processVariables, createHeaders());
            Long processInstanceId = restTemplate.postForObject(url, requestEntity, Long.class);

            // Create database record
            Incident incident = Incident.builder()
                    .incidentNumber(incidentNumber)
                    .processInstanceId(processInstanceId)
                    .atmId(atmId)
                    .errorType(errorType)
                    .incidentDescription(incidentDescription)
                    .status(IncidentStatus.CREATED)
                    .createdBy(createdBy)
                    .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                    .build();

            incident = incidentRepository.save(incident);

            LOGGER.info("Started incident process for ATM: {}, Process Instance ID: {}, Incident Number: {}",
                    atmId, processInstanceId, incident.getIncidentNumber());

            return convertToDto(incident);

        } catch (Exception e) {
            LOGGER.error("Error starting incident process for ATM: {}", atmId, e);
            throw new RuntimeException("Failed to start incident process", e);
        }
    }
    /**
     * Get incident by ID
     */
    @Transactional
    public IncidentDto getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + id));
        return convertToDto(incident);
    }

    /**
     * Gets process instance information
     * GET /server/containers/{containerId}/processes/instances/{processInstanceId}
     */
    public Map<String, Object> getProcessInstance(Long processInstanceId) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/processes/instances/" + processInstanceId;

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            LOGGER.info("Retrieved process instance: {}", processInstanceId);
            return response.getBody();

        } catch (Exception e) {
            LOGGER.error("Error retrieving process instance: {}", processInstanceId, e);
            throw new RuntimeException("Failed to retrieve process instance", e);
        }
    }

    /**
     * Aborts a process instance with database update
     * DELETE /server/containers/{containerId}/processes/instances/{processInstanceId}
     */
    public void abortProcessInstance(Long processInstanceId) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/processes/instances/" + processInstanceId;

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);

            // Update database
            incidentRepository.findByProcessInstanceId(processInstanceId)
                    .ifPresent(incident -> {
                        incident.setStatus(IncidentStatus.ABORTED);
                        incidentRepository.save(incident);
                    });

            LOGGER.info("Aborted process instance: {}", processInstanceId);

        } catch (Exception e) {
            LOGGER.error("Error aborting process instance: {}", processInstanceId, e);
            throw new RuntimeException("Failed to abort process instance", e);
        }
    }

    // ====================== TASK QUERY OPERATIONS ======================

    /**
     * Get tasks for potential owners (tasks available to claim)
     * GET /server/queries/tasks/instances/pot-owners
     */
    @Transactional
    public List<Map<String, Object>> getTasksForPotentialOwners(String group) {
        try {
            StringBuilder urlBuilder = new StringBuilder(KIE_SERVER_URL + "/queries/tasks/instances/pot-owners");

            if (group != null && !group.isEmpty()) {
                urlBuilder.append("?groups=").append(group);
            }

            String url = urlBuilder.toString();

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) responseBody.get("task-summary");

            // Create/update task tracking records
            if (tasks != null) {
                for (Map<String, Object> taskData : tasks) {
                    trackTaskIfNotExists(taskData,group);
                }
            }

            LOGGER.info("Retrieved {} tasks for potential owners", tasks != null ? tasks.size() : 0);
            LOGGER.info("Tasks for potential owners: {}", tasks);
            return tasks;

        } catch (Exception e) {
            LOGGER.error("Error retrieving tasks for potential owners", e);
            throw new RuntimeException("Failed to retrieve tasks for potential owners", e);
        }
    }

    /**
     * Get tasks owned by user
     * GET /server/queries/tasks/instances/owners
     */
    public List<Map<String, Object>> getTasksOwnedByUser(String user, int page, int pageSize) {
        try {
            String url = KIE_SERVER_URL + "/queries/tasks/instances/owners"
                    + "?page=" + page + "&pageSize=" + pageSize + "&user=" + user;

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) responseBody.get("task-summary");

            LOGGER.info("Retrieved {} tasks owned by user: {}", tasks != null ? tasks.size() : 0, user);
            return tasks;

        } catch (Exception e) {
            LOGGER.error("Error retrieving tasks owned by user: {}", user, e);
            throw new RuntimeException("Failed to retrieve tasks owned by user", e);
        }
    }

    /**
     * Get tasks for a specific process instance
     * GET /server/queries/tasks/instances/process/{processInstanceId}
     */
    public List<Map<String, Object>> getTasksForProcessInstance(Long processInstanceId) {
        try {
            String url = KIE_SERVER_URL + "/queries/tasks/instances/process/" + processInstanceId;

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) responseBody.get("task-summary");

            LOGGER.info("Retrieved {} tasks for process instance: {}", tasks != null ? tasks.size() : 0, processInstanceId);
            return tasks;

        } catch (Exception e) {
            LOGGER.error("Error retrieving tasks for process instance: {}", processInstanceId, e);
            throw new RuntimeException("Failed to retrieve tasks for process instance", e);
        }
    }

    // ====================== TASK INSTANCE OPERATIONS WITH PERSISTENCE ======================

    /**
     * Get task instance details
     * GET /server/containers/{containerId}/tasks/{taskInstanceId}
     */
    public Map<String, Object> getTaskInstance(Long taskInstanceId) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/tasks/" + taskInstanceId;

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            LOGGER.info("Retrieved task instance: {}", response.getBody());
            return response.getBody();

        } catch (Exception e) {
            LOGGER.error("Error retrieving task instance: {}", taskInstanceId, e);
            throw new RuntimeException("Failed to retrieve task instance", e);
        }
    }

    /**
     * Claim a task with database tracking
     * PUT /server/containers/{containerId}/tasks/{taskInstanceId}/states/claimed
     */
    public void claimTask(Long taskInstanceId, String user) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/tasks/" + taskInstanceId + "/states/claimed?user=" + user;

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

            // Update database tracking
            updateTaskStatus(taskInstanceId, TaskStatus.RESERVED, user);

            LOGGER.info("Claimed task {} by user: {}", taskInstanceId, user);

        } catch (Exception e) {
            LOGGER.error("Error claiming task {} by user: {}", taskInstanceId, user, e);
            throw new RuntimeException("Failed to claim task", e);
        }
    }

    /**
     * Start a task with database tracking
     * PUT /server/containers/{containerId}/tasks/{taskInstanceId}/states/started
     */
    public void startTask(Long taskInstanceId, String user) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/tasks/" + taskInstanceId + "/states/started?user=" + user;

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

            // Update database tracking
            updateTaskStatus(taskInstanceId, TaskStatus.IN_PROGRESS, user);

            LOGGER.info("Started task {} by user: {}", taskInstanceId, user);

        } catch (Exception e) {
            LOGGER.error("Error starting task {} by user: {}", taskInstanceId, user, e);
            throw new RuntimeException("Failed to start task", e);
        }
    }

    /**
     * Complete a task with output data and database tracking
     * PUT /server/containers/{containerId}/tasks/{taskInstanceId}/states/completed
     */
    public void completeTask(Long taskInstanceId, String user, Map<String, Object> outputData) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/tasks/" + taskInstanceId + "/states/completed?user=" + user;

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(outputData, createHeaders());
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

            // Update database tracking
            updateTaskTracking(taskInstanceId, TaskStatus.COMPLETED, outputData, user);
            updateIncidentFromTask(taskInstanceId, outputData);

            LOGGER.info("Completed task {} by user: {} with output data", taskInstanceId, user);

        } catch (Exception e) {
            LOGGER.error("Error completing task {} by user: {}", taskInstanceId, user, e);
            throw new RuntimeException("Failed to complete task", e);
        }
    }

    /**
     * Release a task (unclaim) with database tracking
     * PUT /server/containers/{containerId}/tasks/{taskInstanceId}/states/released
     */
    public void releaseTask(Long taskInstanceId, String user) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/tasks/" + taskInstanceId + "/states/released?user=" + user;

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

            // Update database tracking
            updateTaskStatus(taskInstanceId, TaskStatus.READY, null);

            LOGGER.info("Released task {} by user: {}", taskInstanceId, user);

        } catch (Exception e) {
            LOGGER.error("Error releasing task {} by user: {}", taskInstanceId, user, e);
            throw new RuntimeException("Failed to release task", e);
        }
    }

    /**
     * Get task input data
     * GET /server/containers/{containerId}/tasks/{taskInstanceId}/contents/input
     */
    public Map<String, Object> getTaskInputData(Long taskInstanceId) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/tasks/" + taskInstanceId + "/contents/input";

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            LOGGER.info("Retrieved input data for task: {}", taskInstanceId);
            return response.getBody();

        } catch (Exception e) {
            LOGGER.error("Error retrieving input data for task: {}", taskInstanceId, e);
            throw new RuntimeException("Failed to retrieve task input data", e);
        }
    }

    /**
     * Get task output data
     * GET /server/containers/{containerId}/tasks/{taskInstanceId}/contents/output
     */
    public Map<String, Object> getTaskOutputData(Long taskInstanceId) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/tasks/" + taskInstanceId + "/contents/output";

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            LOGGER.info("Retrieved output data for task: {}", taskInstanceId);
            return response.getBody();

        } catch (Exception e) {
            LOGGER.error("Error retrieving output data for task: {}", taskInstanceId, e);
            throw new RuntimeException("Failed to retrieve task output data", e);
        }
    }

    public String getProcessDiagram(Long processInstanceId) {
        String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID +
                "/images/processes/instances/" + processInstanceId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("wbadmin", "wbadmin");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    // ====================== WORKFLOW-SPECIFIC TASK COMPLETION METHODS WITH PERSISTENCE ======================

    /**
     * Complete "Process Incident" task (Helpdesk) with database tracking
     */
    public void completeProcessIncidentTask(Long taskInstanceId, String user, String initialDiagnosis) {
        try {
            Map<String, Object> outputData = Map.of(
                    "taskInitialDiagnosis", initialDiagnosis
            );

            smartClaimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Process Incident task {} with diagnosis: {}", taskInstanceId, initialDiagnosis);

        } catch (Exception e) {
            LOGGER.error("Error completing Process Incident task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Process Incident task", e);
        }
    }

    /**
     * Complete "Analyze Incident" task (ATM Monitoring) with database tracking
     */
    public void completeAnalyzeIncidentTask(Long taskInstanceId, String user, String incidentType) {
        try {
            // Validate the incident type matches the expected values
            if (!isValidIncidentType(incidentType)) {
                throw new IllegalArgumentException("Invalid incident type: " + incidentType +
                        ". Must be one of: under_maintenance, outside_maintenance_under_insurance, outside_maintenance_outside_insurance");
            }

            Map<String, Object> outputData = Map.of(
                    "taskIncidentType", incidentType
            );

            smartClaimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Analyze Incident task {} with incident type: {}", taskInstanceId, incidentType);

        } catch (Exception e) {
            LOGGER.error("Error completing Analyze Incident task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Analyze Incident task", e);
        }
    }

    /**
     * Complete "Assess Incident" task (Supplier) with database tracking
     */
    public void completeAssessIncidentTask(Long taskInstanceId, String user, String assessmentDetails, String supplierTicketNumber) {
        try {
            Map<String, Object> outputData = Map.of(
                    "taskAssessmentDetails", assessmentDetails,
                    "taskSupplierTicketNumber", supplierTicketNumber
            );

            smartClaimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Assess Incident task {} with supplier ticket: {}", taskInstanceId, supplierTicketNumber);

        } catch (Exception e) {
            LOGGER.error("Error completing Assess Incident task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Assess Incident task", e);
        }
    }

    /**
     * Complete "Approve Insurance" task (Insurance team) with database tracking
     */
    public void completeApproveInsuranceTask(Long taskInstanceId, String user, String reimbursementDetails) {
        try {
            Map<String, Object> outputData = Map.of(
                    "taskReimbursementDetails", reimbursementDetails
            );

            smartClaimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Approve Insurance task {} with reimbursement: {}", taskInstanceId, reimbursementDetails);

        } catch (Exception e) {
            LOGGER.error("Error completing Approve Insurance task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Approve Insurance task", e);
        }
    }

    /**
     * Complete "Procure Items" task (Purchasing team) with database tracking
     */
    public void completeProcureItemsTask(Long taskInstanceId, String user, String procurementDetails) {
        try {
            Map<String, Object> outputData = Map.of(
                    "taskProcurementDetails", procurementDetails
            );

            smartClaimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Procure Items task {} with procurement: {}", taskInstanceId, procurementDetails);

        } catch (Exception e) {
            LOGGER.error("Error completing Procure Items task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Procure Items task", e);
        }
    }

    /**
     * Complete "Resolve Incident Under Maintenance" task (Supplier) with database tracking
     */
    public void completeResolveIncidentUnderMaintenanceTask(Long taskInstanceId, String user, String resolutionDetails, String supplierTicketNumber) {
        try {
            Map<String, Object> outputData = Map.of(
                    "taskResolutionDetails", resolutionDetails,
                    "taskSupplierTicketNumber", supplierTicketNumber
            );

            smartClaimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Resolve Incident Under Maintenance task {} with supplier ticket: {}", taskInstanceId, supplierTicketNumber);

        } catch (Exception e) {
            LOGGER.error("Error completing Resolve Incident Under Maintenance task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Resolve Incident Under Maintenance task", e);
        }
    }

    /**
     * Complete "Resolve Incident" task (Supplier) with database tracking
     */
    public void completeResolveIncidentTask(Long taskInstanceId, String user, String resolutionDetails, String supplierTicketNumber) {
        try {
            Map<String, Object> outputData = Map.of(
                    "taskResolutionDetails", resolutionDetails,
                    "taskSupplierTicketNumber", supplierTicketNumber
            );

            smartClaimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Resolve Incident task {} with supplier ticket: {}", taskInstanceId, supplierTicketNumber);

        } catch (Exception e) {
            LOGGER.error("Error completing Resolve Incident task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Resolve Incident task", e);
        }
    }

    /**
     * Complete "Close Incident" task (ATM Monitoring) with database tracking
     */
    public void completeCloseIncidentTask(Long taskInstanceId, String user, String closureDetails) {
        try {
            Map<String, Object> outputData = Map.of(
                    "taskClosureDetails", closureDetails
            );

            smartClaimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Close Incident task {} with closure: {}", taskInstanceId, closureDetails);

        } catch (Exception e) {
            LOGGER.error("Error completing Close Incident task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Close Incident task", e);
        }
    }

    // ====================== DATABASE TRACKING AND REPORTING METHODS ======================

    /**
     * Get comprehensive incident report
     */
    @Transactional
    public IncidentReport getIncidentReport(Long processInstanceId) {
        Incident incident = incidentRepository.findByProcessInstanceId(processInstanceId)
                .orElseThrow(() -> new RuntimeException("Incident not found for process: " + processInstanceId));

        // Get live data from jBPM
        Map<String, Object> processVars = getProcessInstance(processInstanceId);

        // Calculate statistics
        IncidentStatistics stats = calculateStatistics(incident);

        return IncidentReport.builder()
                .incident(convertToDto(incident))
                .currentProcessVariables(processVars)
                .tasks(incident.getTasks().stream().map(this::convertToTaskDto).collect(Collectors.toList()))
                .statistics(stats)
                .build();
    }

    /**
     * Get all incidents with filtering
     */
    @Transactional
    public Page<IncidentDto> getAllIncidents(Pageable pageable) {
        return incidentRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    /**
     * Get incidents by status
     */
    @Transactional
    public List<IncidentDto> getIncidentsByStatus(IncidentStatus status) {
        return incidentRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get user's tasks from database
     */
    @Transactional
    public List<IncidentTaskDto> getUserTasks(String user) {
        List<TaskStatus> activeStatuses = List.of(TaskStatus.READY, TaskStatus.RESERVED, TaskStatus.IN_PROGRESS);
        return taskRepository.findByAssignedUserAndStatusIn(user, activeStatuses).stream()
                .map(this::convertToTaskDto)
                .collect(Collectors.toList());
    }@Transactional
    public List<IncidentTaskDto> getUserTasksByStatus(String user,TaskStatus status) {
        List<TaskStatus> taskStatus = List.of(status);
        return taskRepository.findByAssignedUserAndStatusIn(user, taskStatus).stream()
                .map(this::convertToTaskDto)
                .collect(Collectors.toList());
    }

    /**
     * Get group's tasks from database
     */
    @Transactional
    public List<IncidentTaskDto> getGroupTasks(String group) {
        List<TaskStatus> activeStatuses = List.of(TaskStatus.READY, TaskStatus.RESERVED, TaskStatus.IN_PROGRESS);
        return taskRepository.findByAssignedGroupAndStatusIn(group, activeStatuses).stream()
                .map(this::convertToTaskDto)
                .collect(Collectors.toList());
    }

    // ====================== HELPER METHODS ======================

    /**
     * Enhanced method to claim, start and complete a task with database tracking
     */
    public void smartClaimStartAndCompleteTask(Long taskInstanceId, String user, Map<String, Object> outputData) {
        try {
            // Get current task state first
            Map<String, Object> taskInfo = getTaskInstance(taskInstanceId);
            String currentStatus = (String) taskInfo.get("task-status");
            String currentOwner = (String) taskInfo.get("task-actual-owner");

            // Only claim if not already claimed by this user
            if ("ready".equalsIgnoreCase(currentStatus)) {
                claimTask(taskInstanceId, user);
                startTask(taskInstanceId, user);
            } else if ("reserved".equalsIgnoreCase(currentStatus) && user.equals(currentOwner)) {
                startTask(taskInstanceId, user);
            } else if ("inprogress".equalsIgnoreCase(currentStatus) && user.equals(currentOwner)) {
                // Already started, just complete
            } else {
                throw new RuntimeException("Cannot complete task - invalid state or wrong user");
            }

            completeTask(taskInstanceId, user, outputData);

        } catch (Exception e) {
            LOGGER.error("Error in claim-start-complete flow for task {} by user: {}", taskInstanceId, user, e);
            throw new RuntimeException("Failed to complete task flow", e);
        }
    }

    /**
     * Validates incident type values
     */
    private boolean isValidIncidentType(String incidentType) {
        return incidentType != null && (
                "under_maintenance".equals(incidentType) ||
                        "outside_maintenance_under_insurance".equals(incidentType) ||
                        "outside_maintenance_outside_insurance".equals(incidentType)
        );
    }

    // Helper method to safely convert Integer/Long
    public Long convertToLong(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else {
            throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to Long");
        }
    }

    private HttpHeaders createHeaders() {
        String credentials = USERNAME + ":" + PASSWORD;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + encodedCredentials);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    // ====================== PRIVATE DATABASE TRACKING METHODS ======================

    private void trackTaskIfNotExists(Map<String, Object> taskData, String group) {
        Long taskInstanceId = convertToLong(taskData.get("task-id"));
        Long processInstanceId = convertToLong(taskData.get("task-proc-inst-id"));

        if (!taskRepository.findByTaskInstanceId(taskInstanceId).isPresent()) {
            Incident incident = incidentRepository.findByProcessInstanceId(processInstanceId)
                    .orElse(null);

            if (incident != null) {
                IncidentTask task = IncidentTask.builder()
                        .taskInstanceId(taskInstanceId)
                        .taskName((String) taskData.get("task-name"))
                        .taskDescription((String) taskData.get("task-description"))
                        .assignedGroup(group != null ? group : "unknown")
                        .status(mapJbpmStatusToTaskStatus((String) taskData.get("task-status")))
                        .priority((Integer) taskData.get("task-priority"))
                        .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                        .build();
                // Get and store clean input data
                storeTaskInputData(task);
                incident.addTask(task);
                taskRepository.save(task);

                updateIncidentStatusBasedOnTask(incident, task.getTaskName());

                LOGGER.debug("Tracked new task: {} for incident: {}", task.getTaskName(), incident.getIncidentNumber());
            }
        }
    }
    private void storeTaskInputData(IncidentTask task) {
        try {
            Map<String, Object> inputData = getTaskInputData(task.getTaskInstanceId());

            if (inputData != null && !inputData.isEmpty()) {
                // Filter out jBPM metadata and keep only business data
                Map<String, Object> cleanInputData = inputData.entrySet().stream()
                        .filter(entry -> isBusinessData(entry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                if (!cleanInputData.isEmpty()) {
                    task.setInputData(objectMapper.writeValueAsString(cleanInputData));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error storing input data for task: {}", task.getTaskInstanceId(), e);
        }
    }
    private boolean isBusinessData(String key) {
        // Filter out jBPM internal fields
        return !key.equals("TaskName") &&
                !key.equals("NodeName") &&
                !key.equals("Skippable") &&
                !key.equals("GroupId") &&
                !key.equals("ActorId") &&
                !key.startsWith("_") &&
                key.startsWith("task") || // Keep task variables
                key.startsWith("incident") || // Keep incident variables
                key.startsWith("atm"); // Keep ATM variables
    }

    private String extractAssignedGroup(Map<String, Object> taskData) {
        // Extract group from potential owners
        Object potentialOwners = taskData.get("task-pot-owners");
        if (potentialOwners instanceof List) {
            List<?> owners = (List<?>) potentialOwners;
            for (Object owner : owners) {
                if (owner instanceof String) {
                    String ownerStr = (String) owner;
                    // Assuming groups don't contain @ symbol (simple heuristic)
                    if (!ownerStr.contains("@")) {
                        return ownerStr;
                    }
                }
            }
        }
        return "unknown";
    }

    private TaskStatus mapJbpmStatusToTaskStatus(String jbpmStatus) {
        if (jbpmStatus == null) return TaskStatus.CREATED;

        return switch (jbpmStatus.toLowerCase()) {
            case "ready" -> TaskStatus.READY;
            case "reserved" -> TaskStatus.RESERVED;
            case "inprogress" -> TaskStatus.IN_PROGRESS;
            case "completed" -> TaskStatus.COMPLETED;
            case "suspended" -> TaskStatus.SUSPENDED;
            case "failed" -> TaskStatus.FAILED;
            case "error" -> TaskStatus.ERROR;
            case "exited" -> TaskStatus.EXITED;
            case "obsolete" -> TaskStatus.OBSOLETE;
            default -> TaskStatus.CREATED;
        };
    }

    private void updateTaskStatus(Long taskInstanceId, TaskStatus status, String user) {
        taskRepository.findByTaskInstanceId(taskInstanceId)
                .ifPresent(task -> {
                    task.setStatus(status);
                    if (user != null) {
                        task.setAssignedUser(user);
                    }

                    LocalDateTime now = LocalDateTime.now();
                    switch (status) {
                        case RESERVED:
                            task.setClaimedAt(now);
                            break;
                        case IN_PROGRESS:
                            task.setStartedAt(now);
                            break;
                        case COMPLETED:
                            task.setCompletedAt(now);
                            break;
                    }

                    taskRepository.save(task);
                });
    }

    private void updateTaskTracking(Long taskInstanceId, TaskStatus status, Map<String, Object> outputData, String user) {
        IncidentTask task = taskRepository.findByTaskInstanceId(taskInstanceId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskInstanceId));

        task.setStatus(status);
        task.setAssignedUser(user);

        if (status == TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        }

        if (outputData != null && !outputData.isEmpty()) {
            try {
                task.setOutputData(objectMapper.writeValueAsString(outputData));
            } catch (JsonProcessingException e) {
                LOGGER.error("Error serializing output data", e);
            }
        }

        taskRepository.save(task);
    }

    private void updateIncidentFromTask(Long taskInstanceId, Map<String, Object> outputData) {
        IncidentTask task = taskRepository.findByTaskInstanceId(taskInstanceId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskInstanceId));

        // Get the incident ID and fetch it separately to avoid proxy issues
        Long incidentId = task.getIncident().getId();
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));

        // Update incident based on task output
        outputData.forEach((key, value) -> {
            switch (key) {
                case "taskInitialDiagnosis":
                    incident.setInitialDiagnosis((String) value);
                    incident.setStatus(IncidentStatus.IN_PROGRESS);
                    break;
                case "taskIncidentType":
                    incident.setIncidentType(mapStringToIncidentType((String) value));
                    break;
                case "taskAssessmentDetails":
                    incident.setAssessmentDetails((String) value);
                    incident.setStatus(IncidentStatus.WAITING_FOR_INSURANCE);
                    break;
                case "taskSupplierTicketNumber":
                    incident.setSupplierTicketNumber((String) value);
                    break;
                case "taskReimbursementDetails":
                    incident.setReimbursementDetails((String) value);
                    incident.setStatus(IncidentStatus.WAITING_FOR_PROCUREMENT);
                    break;
                case "taskProcurementDetails":
                    incident.setProcurementDetails((String) value);
                    incident.setStatus(IncidentStatus.WAITING_FOR_RESOLUTION);
                    break;
                case "taskResolutionDetails":
                    incident.setResolutionDetails((String) value);
                    incident.setStatus(IncidentStatus.RESOLVED);
                    incident.setResolvedAt(LocalDateTime.now());
                    break;
                case "taskClosureDetails":
                    incident.setClosureDetails((String) value);
                    incident.setStatus(IncidentStatus.CLOSED);
                    incident.setClosedAt(LocalDateTime.now());
                    break;
            }
        });

        incidentRepository.save(incident);
    }

    private void updateIncidentStatusBasedOnTask(Incident incident, String taskName) {
        // Ensure we have a fresh copy from the database
        Incident freshIncident = incidentRepository.findById(incident.getId())
                .orElse(incident);

        switch (taskName) {
            case "Process Incident":
                freshIncident.setStatus(IncidentStatus.IN_PROGRESS);
                break;
            case "Analyze Incident":
                freshIncident.setStatus(IncidentStatus.IN_PROGRESS);
                break;
            case "Assess Incident":
                freshIncident.setStatus(IncidentStatus.WAITING_FOR_ASSESSMENT);
                break;
            case "Approve Insurance":
                freshIncident.setStatus(IncidentStatus.WAITING_FOR_INSURANCE);
                break;
            case "Procure Items":
                freshIncident.setStatus(IncidentStatus.WAITING_FOR_PROCUREMENT);
                break;
            case "Resolve Incident":
            case "Resolve Incident Under Maintenance":
                freshIncident.setStatus(IncidentStatus.WAITING_FOR_RESOLUTION);
                break;
            case "Close Incident":
                freshIncident.setStatus(IncidentStatus.RESOLVED);
                break;
        }
        incidentRepository.save(freshIncident);
    }

    private IncidentType mapStringToIncidentType(String type) {
        if (type == null) return IncidentType.NOT_CLASSIFIED;

        return switch (type) {
            case "under_maintenance" -> IncidentType.UNDER_MAINTENANCE;
            case "outside_maintenance_under_insurance" -> IncidentType.OUTSIDE_MAINTENANCE_UNDER_INSURANCE;
            case "outside_maintenance_outside_insurance" -> IncidentType.OUTSIDE_MAINTENANCE_OUTSIDE_INSURANCE;
            default -> IncidentType.NOT_CLASSIFIED;
        };
    }

    private IncidentStatistics calculateStatistics(Incident incident) {
        List<IncidentTask> tasks = incident.getTasks();
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .mapToLong(task -> task.getStatus() == TaskStatus.COMPLETED ? 1 : 0)
                .sum();
        long pendingTasks = totalTasks - completedTasks;

        long totalDurationMinutes = 0;
        if (incident.getCreatedAt() != null) {
            LocalDateTime endTime = incident.getClosedAt() != null ?
                    incident.getClosedAt() : LocalDateTime.now();
            totalDurationMinutes = ChronoUnit.MINUTES.between(incident.getCreatedAt(), endTime);
        }

        double completionPercentage = totalTasks > 0 ?
                (double) completedTasks / totalTasks * 100 : 0;

        String currentStep = tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .findFirst()
                .map(IncidentTask::getTaskName)
                .orElse("Completed");

        return IncidentStatistics.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .totalDurationMinutes(totalDurationMinutes)
                .currentStep(currentStep)
                .completionPercentage(completionPercentage)
                .build();
    }

    private String generateIncidentNumber() {
        return "INC-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private IncidentDto convertToDto(Incident incident) {
        return IncidentDto.builder()
                .id(incident.getId())
                .incidentNumber(incident.getIncidentNumber())
                .processInstanceId(incident.getProcessInstanceId())
                .atmId(incident.getAtmId())
                .errorType(incident.getErrorType())
                .incidentDescription(incident.getIncidentDescription())
                .status(incident.getStatus())
                .incidentType(incident.getIncidentType())
                .initialDiagnosis(incident.getInitialDiagnosis())
                .assessmentDetails(incident.getAssessmentDetails())
                .supplierTicketNumber(incident.getSupplierTicketNumber())
                .reimbursementDetails(incident.getReimbursementDetails())
                .procurementDetails(incident.getProcurementDetails())
                .resolutionDetails(incident.getResolutionDetails())
                .closureDetails(incident.getClosureDetails())
                .createdBy(incident.getCreatedBy())
                .assignedTo(incident.getAssignedTo())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .resolvedAt(incident.getResolvedAt())
                .closedAt(incident.getClosedAt())
                .build();
    }

    private IncidentTaskDto convertToTaskDto(IncidentTask task) {
        return IncidentTaskDto.builder()
                .id(task.getId())
                .taskInstanceId(task.getTaskInstanceId())
                .taskName(task.getTaskName())
                .taskDescription(task.getTaskDescription())
                .assignedGroup(task.getAssignedGroup())
                .assignedUser(task.getAssignedUser())
                .status(task.getStatus())
                .priority(task.getPriority())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .claimedAt(task.getClaimedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .dueDate(task.getDueDate())
                .inputData(task.getInputData())
                .outputData(task.getOutputData())
                .comments(task.getComments())
                .build();
    }


    /**
     * Convert jBPM task data to IncidentTaskDto
     */
    public IncidentTaskDto convertJbpmTaskToDto(Map<String, Object> jbpmTask) {
        try {
            Long taskInstanceId = convertToLong(jbpmTask.get("task-id"));
            Long processInstanceId = convertToLong(jbpmTask.get("task-proc-inst-id"));

            // Parse jBPM date format
            LocalDateTime createdAt = parseJbpmDate(jbpmTask.get("task-created-on"));
            LocalDateTime activationTime = parseJbpmDate(jbpmTask.get("task-activation-time"));
            LocalDateTime expirationTime = parseJbpmDate(jbpmTask.get("task-expiration-time"));

            return IncidentTaskDto.builder()
                    .taskInstanceId(taskInstanceId)
                    .taskName((String) jbpmTask.get("task-name"))
                    .taskDescription((String) jbpmTask.get("task-description"))
                    .assignedUser((String) jbpmTask.get("task-actual-owner"))
                    .assignedGroup(extractPotentialGroup(jbpmTask))
                    .status(mapJbpmStatusToTaskStatus((String) jbpmTask.get("task-status")))
                    .priority((Integer) jbpmTask.get("task-priority"))
                    .createdAt(createdAt)
                    .dueDate(expirationTime)
                    .comments((String) jbpmTask.get("task-subject"))
                    .build();

        } catch (Exception e) {
            LOGGER.error("Error converting jBPM task to DTO: {}", jbpmTask, e);
            throw new RuntimeException("Failed to convert jBPM task data", e);
        }
    }

    /**
     * Parse jBPM date format {java.util.Date=timestamp}
     */
    private LocalDateTime parseJbpmDate(Object dateObj) {
        if (dateObj == null) return null;

        try {
            if (dateObj instanceof Map) {
                Map<String, Object> dateMap = (Map<String, Object>) dateObj;
                Object timestamp = dateMap.get("java.util.Date");
                if (timestamp instanceof Number) {
                    return LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(((Number) timestamp).longValue()),
                            ZoneOffset.UTC
                    );
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Could not parse jBPM date: {}", dateObj);
        }

        return null;
    }

    /**
     * Extract potential group from jBPM task data
     */
    private String extractPotentialGroup(Map<String, Object> taskData) {
        // Since jBPM doesn't directly expose potential owners in the summary,
        // we'll derive it from task name or use a default
        String taskName = (String) taskData.get("task-name");

        if ("Process Incident".equals(taskName)) {
            return "helpdesk";
        } else if ("Analyze Incident".equals(taskName) || "Close Incident".equals(taskName)) {
            return "atm_monitoring";
        } else if ("Assess Incident".equals(taskName) || "Resolve Incident".equals(taskName) || "Resolve Incident Under Maintenance".equals(taskName)) {
            return "supplier";
        } else if ("Approve Insurance".equals(taskName)) {
            return "insurance";
        } else if ("Procure Parts/Services".equals(taskName)) {
            return "purchasing";
        }

        return "unknown";
    }


}