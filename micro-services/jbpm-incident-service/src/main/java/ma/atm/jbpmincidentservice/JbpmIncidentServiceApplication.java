package ma.atm.jbpmincidentservice;

import ma.atm.jbpmincidentservice.jbpm.IncidentProcessService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)

public class JbpmIncidentServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(JbpmIncidentServiceApplication.class, args);
    }
    /*
    @Bean
    public CommandLineRunner runIncidentProcess(IncidentProcessService service) {
        return args -> {
            service.startIncidentProcess("ATM101", "chi haja", "This is the first incident");
            List<Map<String, Object>> list = service.getTasksForPotentialOwners("helpdesk");

            for (Map<String, Object> task : list) {
                System.out.println("Task ID: " + task.get("task-id"));
                System.out.println("Task Name: " + task.get("task-name"));
                System.out.println("Task Description: " + task.get("description"));
                System.out.println("Task Status: " + task.get("status"));

                // Use the helper method
                Long taskId = service.convertToLong(task.get("task-id"));

                System.out.println("Task input: " + service.getTaskInputData(taskId));
                System.out.println("Task output: " + service.getTaskOutputData(taskId));
                service.completeProcessIncidentTask(taskId, "helpdesk","chi haja trat");
            }

            List<Map<String, Object>> list1 = service.getTasksForPotentialOwners("atm_monitoring");
            for (Map<String, Object> task : list1) {
                System.out.println("Task ID: " + task.get("task-id"));
                System.out.println("Task Name: " + task.get("task-name"));
                System.out.println("Task Description: " + task.get("description"));
                System.out.println("Task Status: " + task.get("status"));

                // Use the helper method
                Long taskId = service.convertToLong(task.get("task-id"));

                System.out.println("Task input: " + service.getTaskInputData(taskId));
                System.out.println("Task output: " + service.getTaskOutputData(taskId));
                service.completeAnalyzeIncidentTask(taskId, "atm_monitoring",IncidentProcessService.INCIDENT_TYPE_OUTSIDE_MAINTENANCE_UNDER_INSURANCE);
            }


            List<Map<String, Object>> list2 = service.getTasksForPotentialOwners("supplier");
            for (Map<String, Object> task : list2) {
                System.out.println("Task ID: " + task.get("task-id"));
                System.out.println("Task Name: " + task.get("task-name"));
                System.out.println("Task Description: " + task.get("description"));
                System.out.println("Task Status: " + task.get("status"));

                // Use the helper method
                Long taskId = service.convertToLong(task.get("task-id"));

                System.out.println("Task input: " + service.getTaskInputData(taskId));
                System.out.println("Task output: " + service.getTaskOutputData(taskId));
                service.completeAssessIncidentTask(taskId, "supplier", "chi haja trat f details" , "supp12548745");
            }


            List<Map<String, Object>> list3 = service.getTasksForPotentialOwners("insurance");
            for (Map<String, Object> task : list3) {
                System.out.println("Task ID: " + task.get("task-id"));
                System.out.println("Task Name: " + task.get("task-name"));
                System.out.println("Task Description: " + task.get("description"));
                System.out.println("Task Status: " + task.get("status"));

                // Use the helper method
                Long taskId = service.convertToLong(task.get("task-id"));

                System.out.println("Task input: " + service.getTaskInputData(taskId));
                service.completeApproveInsuranceTask(taskId, "supplier", "no reimbursement" );
                System.out.println("Task output: " + service.getTaskOutputData(taskId));

            }


            List<Map<String, Object>> list4 = service.getTasksForPotentialOwners("purchasing");
            for (Map<String, Object> task : list4) {
                System.out.println("Task ID: " + task.get("task-id"));
                System.out.println("Task Name: " + task.get("task-name"));
                System.out.println("Task Description: " + task.get("description"));
                System.out.println("Task Status: " + task.get("status"));

                // Use the helper method
                Long taskId = service.convertToLong(task.get("task-id"));

                System.out.println("Task input: " + service.getTaskInputData(taskId));
                service.completeProcureItemsTask(taskId, "purchasing", "chrinahom" );
                System.out.println("Task output: " + service.getTaskOutputData(taskId));

            }


            List<Map<String, Object>> list5 = service.getTasksForPotentialOwners("supplier");
            for (Map<String, Object> task : list5) {
                System.out.println("Task ID: " + task.get("task-id"));
                System.out.println("Task Name: " + task.get("task-name"));
                System.out.println("Task Description: " + task.get("description"));
                System.out.println("Task Status: " + task.get("status"));

                // Use the helper method
                Long taskId = service.convertToLong(task.get("task-id"));

                System.out.println("Task input: " + service.getTaskInputData(taskId));
                service.completeResolveIncidentTask(taskId, "purchasing", "chrinahom" , "supp12548745");
                System.out.println("Task output: " + service.getTaskOutputData(taskId));

            }

        List<Map<String, Object>> list6 = service.getTasksForPotentialOwners("atm_monitoring");
        for (Map<String, Object> task : list6) {
            System.out.println("Task ID: " + task.get("task-id"));
            System.out.println("Task Name: " + task.get("task-name"));
            System.out.println("Task Description: " + task.get("description"));
            System.out.println("Task Status: " + task.get("status"));

            // Use the helper method
            Long taskId = service.convertToLong(task.get("task-id"));

            System.out.println("Task input: " + service.getTaskInputData(taskId));
            service.completeCloseIncidentTask(taskId, "atm_monitoring", "salat" );
            System.out.println("Task output: " + service.getTaskOutputData(taskId));

        }
        };


    }*/

}
