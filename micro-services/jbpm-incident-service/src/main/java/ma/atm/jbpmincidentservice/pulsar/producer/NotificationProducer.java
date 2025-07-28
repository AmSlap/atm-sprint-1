package ma.atm.jbpmincidentservice.pulsar.producer;


import lombok.extern.slf4j.Slf4j;
import ma.atm.jbpmincidentservice.model.Incident;
import ma.atm.jbpmincidentservice.model.IncidentMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationProducer {

    @Autowired
    private PulsarTemplate<Incident> pulsarTemplate;

    @Value("${pulsar.producer.notification-topic}")
    private String notificationTopic;

    public void publishNotification(Incident incidentMessage) {
        try {
            pulsarTemplate.send(notificationTopic, incidentMessage);
            log.info("Notification sent to topic {}: {}", notificationTopic, incidentMessage);
        } catch (Exception e) {
            log.error("Failed to send notification to topic {}: {}", notificationTopic, e.getMessage(), e);
        }
    }
}
