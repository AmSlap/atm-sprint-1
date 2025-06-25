package ma.atm.jbpmincidentservice.jbpm;



import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

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

    public IncidentProcessService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // ====================== PROCESS OPERATIONS ======================

    /**
     * Starts a new incident management process
     * POST /server/containers/{containerId}/processes/{processId}/instances
     */
    public Long startIncidentProcess(String atmId, String errorType, String incidentDescription) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/processes/" + PROCESS_ID + "/instances";

            Map<String, Object> processVariables = Map.of(
                    "atmId", atmId,
                    "errorType", errorType,
                    "incidentDescription", incidentDescription
            );

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(processVariables, createHeaders());
            Long processInstanceId = restTemplate.postForObject(url, requestEntity, Long.class);

            LOGGER.info("Started incident process for ATM: {}, Process Instance ID: {}", atmId, processInstanceId);
            return processInstanceId;

        } catch (Exception e) {
            LOGGER.error("Error starting incident process for ATM: {}", atmId, e);
            throw new RuntimeException("Failed to start incident process", e);
        }
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
     * Aborts a process instance
     * DELETE /server/containers/{containerId}/processes/instances/{processInstanceId}
     */
    public void abortProcessInstance(Long processInstanceId) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/processes/instances/" + processInstanceId;

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);

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
    public List<Map<String, Object>> getTasksForPotentialOwners(String group) {
        try {
            StringBuilder urlBuilder = new StringBuilder(KIE_SERVER_URL + "/queries/tasks/instances/pot-owners");

            boolean hasParams = false;

            if (group != null && !group.isEmpty()) {
                urlBuilder.append("?groups=").append(group);
                hasParams = true;
            }

            // Actually use the page and pageSize parameters
           /* if (page >= 0) {
                urlBuilder.append(hasParams ? "&" : "?").append("page=").append(page);
                hasParams = true;
            }

            if (pageSize > 0) {
                urlBuilder.append(hasParams ? "&" : "?").append("pageSize=").append(pageSize);
            }*/

            String url = urlBuilder.toString();
            System.out.println("Final URL: " + url);

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) responseBody.get("task-summary");

            LOGGER.info("Retrieved {} tasks for potential owners", tasks != null ? tasks.size() : 0);
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

    // ====================== TASK INSTANCE OPERATIONS ======================

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

            LOGGER.info("Retrieved task instance: {}", taskInstanceId);
            return response.getBody();

        } catch (Exception e) {
            LOGGER.error("Error retrieving task instance: {}", taskInstanceId, e);
            throw new RuntimeException("Failed to retrieve task instance", e);
        }
    }

    /**
     * Claim a task
     * PUT /server/containers/{containerId}/tasks/{taskInstanceId}/states/claimed
     */
    public void claimTask(Long taskInstanceId, String user) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/tasks/" + taskInstanceId + "/states/claimed?user=" + user;

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

            LOGGER.info("Claimed task {} by user: {}", taskInstanceId, user);

        } catch (Exception e) {
            LOGGER.error("Error claiming task {} by user: {}", taskInstanceId, user, e);
            throw new RuntimeException("Failed to claim task", e);
        }
    }

    /**
     * Start a task
     * PUT /server/containers/{containerId}/tasks/{taskInstanceId}/states/started
     */
    public void startTask(Long taskInstanceId, String user) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/tasks/" + taskInstanceId + "/states/started?user=" + user;

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

            LOGGER.info("Started task {} by user: {}", taskInstanceId, user);

        } catch (Exception e) {
            LOGGER.error("Error starting task {} by user: {}", taskInstanceId, user, e);
            throw new RuntimeException("Failed to start task", e);
        }
    }

    /**
     * Complete a task with output data
     * PUT /server/containers/{containerId}/tasks/{taskInstanceId}/states/completed
     */
    public void completeTask(Long taskInstanceId, String user, Map<String, Object> outputData) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/tasks/" + taskInstanceId + "/states/completed?user=" + user;

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(outputData, createHeaders());
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

            LOGGER.info("Completed task {} by user: {} with output data", taskInstanceId, user);

        } catch (Exception e) {
            LOGGER.error("Error completing task {} by user: {}", taskInstanceId, user, e);
            throw new RuntimeException("Failed to complete task", e);
        }
    }

    /**
     * Release a task (unclaim)
     * PUT /server/containers/{containerId}/tasks/{taskInstanceId}/states/released
     */
    public void releaseTask(Long taskInstanceId, String user) {
        try {
            String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID + "/tasks/" + taskInstanceId + "/states/released?user=" + user;

            HttpEntity<?> requestEntity = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

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












    // ====================== WORKFLOW-SPECIFIC TASK COMPLETION METHODS ======================

    /**
     * Complete "Process Incident" task (Helpdesk)
     * Input: incidentNumber, atmId, errorType, incidentDescription
     * Output: taskInitialDiagnosis
     */
    public void completeProcessIncidentTask(Long taskInstanceId, String user, String initialDiagnosis) {
        try {
            Map<String, Object> outputData = Map.of(
                    "taskInitialDiagnosis", initialDiagnosis
            );

            claimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Process Incident task {} with diagnosis: {}", taskInstanceId, initialDiagnosis);

        } catch (Exception e) {
            LOGGER.error("Error completing Process Incident task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Process Incident task", e);
        }
    }

    /**
     * Complete "Analyze Incident" task (ATM Monitoring)
     * Input: incidentNumber, atmId, errorType, incidentDescription, taskInitialDiagnosis
     * Output: taskIncidentType (CRITICAL - this determines the gateway routing)
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

            claimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Analyze Incident task {} with incident type: {}", taskInstanceId, incidentType);

        } catch (Exception e) {
            LOGGER.error("Error completing Analyze Incident task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Analyze Incident task", e);
        }
    }

    /**
     * Complete "Assess Incident" task (Supplier - for non-maintenance incidents)
     * Input: incidentNumber, atmId, errorType, incidentDescription, taskInitialDiagnosis, taskIncidentType
     * Output: taskAssessmentDetails, taskSupplierTicketNumber
     */
    public void completeAssessIncidentTask(Long taskInstanceId, String user, String assessmentDetails, String supplierTicketNumber) {
        try {
            Map<String, Object> outputData = Map.of(
                    "taskAssessmentDetails", assessmentDetails,
                    "taskSupplierTicketNumber", supplierTicketNumber
            );

            claimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Assess Incident task {} with supplier ticket: {}", taskInstanceId, supplierTicketNumber);

        } catch (Exception e) {
            LOGGER.error("Error completing Assess Incident task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Assess Incident task", e);
        }
    }

    /**
     * Complete "Approve Insurance" task (Insurance team)
     * Input: incidentNumber, atmId, errorType, incidentDescription, taskInitialDiagnosis, taskIncidentType, taskAssessmentDetails, taskSupplierTicketNumber
     * Output: taskReimbursementDetails
     */
    public void completeApproveInsuranceTask(Long taskInstanceId, String user, String reimbursementDetails) {
        try {
            Map<String, Object> outputData = Map.of(
                    "taskReimbursementDetails", reimbursementDetails
            );

            claimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Approve Insurance task {} with reimbursement: {}", taskInstanceId, reimbursementDetails);

        } catch (Exception e) {
            LOGGER.error("Error completing Approve Insurance task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Approve Insurance task", e);
        }
    }

    /**
     * Complete "Procure Items" task (Purchasing team)
     * Input: All previous variables plus taskReimbursementDetails (if from insurance path)
     * Output: taskProcurementDetails
     */
    public void completeProcureItemsTask(Long taskInstanceId, String user, String procurementDetails) {
        try {
            Map<String, Object> outputData = Map.of(
                    "taskProcurementDetails", procurementDetails
            );

            claimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Procure Items task {} with procurement: {}", taskInstanceId, procurementDetails);

        } catch (Exception e) {
            LOGGER.error("Error completing Procure Items task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Procure Items task", e);
        }
    }

    /**
     * Complete "Resolve Incident Under Maintenance" task (Supplier - maintenance path)
     * Input: incidentNumber, atmId, errorType, incidentDescription, taskInitialDiagnosis, taskIncidentType
     * Output: taskResolutionDetails, taskSupplierTicketNumber
     */
    public void completeResolveIncidentUnderMaintenanceTask(Long taskInstanceId, String user, String resolutionDetails, String supplierTicketNumber) {
        try {
            Map<String, Object> outputData = Map.of(
                    "taskResolutionDetails", resolutionDetails,
                    "taskSupplierTicketNumber", supplierTicketNumber
            );

            claimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Resolve Incident Under Maintenance task {} with supplier ticket: {}", taskInstanceId, supplierTicketNumber);

        } catch (Exception e) {
            LOGGER.error("Error completing Resolve Incident Under Maintenance task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Resolve Incident Under Maintenance task", e);
        }
    }

    /**
     * Complete "Resolve Incident" task (Supplier - non-maintenance path after procurement)
     * Input: All previous variables including taskProcurementDetails
     * Output: taskResolutionDetails, taskSupplierTicketNumber (updated)
     */
    public void completeResolveIncidentTask(Long taskInstanceId, String user, String resolutionDetails, String supplierTicketNumber) {
        try {
            Map<String, Object> outputData = Map.of(
                    "taskResolutionDetails", resolutionDetails,
                    "taskSupplierTicketNumber", supplierTicketNumber
            );

            claimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Resolve Incident task {} with supplier ticket: {}", taskInstanceId, supplierTicketNumber);

        } catch (Exception e) {
            LOGGER.error("Error completing Resolve Incident task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Resolve Incident task", e);
        }
    }

    /**
     * Complete "Close Incident" task (ATM Monitoring)
     * Input: All previous variables including taskResolutionDetails
     * Output: taskClosureDetails
     */
    public void completeCloseIncidentTask(Long taskInstanceId, String user, String closureDetails) {
        try {
            Map<String, Object> outputData = Map.of(
                    "taskClosureDetails", closureDetails
            );

            claimStartAndCompleteTask(taskInstanceId, user, outputData);
            LOGGER.info("Completed Close Incident task {} with closure: {}", taskInstanceId, closureDetails);

        } catch (Exception e) {
            LOGGER.error("Error completing Close Incident task {}", taskInstanceId, e);
            throw new RuntimeException("Failed to complete Close Incident task", e);
        }
    }

// ====================== HELPER METHODS ======================

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

    /**
     * Enhanced method to claim, start and complete a task
     */
    public void claimStartAndCompleteTask(Long taskInstanceId, String user, Map<String, Object> outputData) {
        try {
            claimTask(taskInstanceId, user);
            startTask(taskInstanceId, user);
            completeTask(taskInstanceId, user, outputData);

            LOGGER.info("Claimed, started and completed task {} by user: {}", taskInstanceId, user);

        } catch (Exception e) {
            LOGGER.error("Error in claim-start-complete flow for task {} by user: {}", taskInstanceId, user, e);
            throw new RuntimeException("Failed to complete task flow", e);
        }
    }

    // ====================== CONSTANTS FOR INCIDENT TYPES ======================
    public static final String INCIDENT_TYPE_UNDER_MAINTENANCE = "under_maintenance";
    public static final String INCIDENT_TYPE_OUTSIDE_MAINTENANCE_UNDER_INSURANCE = "outside_maintenance_under_insurance";
    public static final String INCIDENT_TYPE_OUTSIDE_MAINTENANCE_OUTSIDE_INSURANCE = "outside_maintenance_outside_insurance";

    // ====================== HELPER METHODS ======================

    private HttpHeaders createHeaders() {
        String credentials = USERNAME + ":" + PASSWORD;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + encodedCredentials);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
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



}