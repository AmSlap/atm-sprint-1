package ma.atm.notificationservice.pulsar.consumer;

import lombok.extern.slf4j.Slf4j;
import ma.atm.notificationservice.model.Incident;
import ma.atm.notificationservice.service.NotificationService;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.common.schema.SchemaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumer {

    @Autowired
    private NotificationService notificationService;



    @PulsarListener(
            topics = "${pulsar.consumer.notification-topic}",
            subscriptionName = "${pulsar.consumer.subscription-name}",
            schemaType = SchemaType.JSON,
            subscriptionType = SubscriptionType.Exclusive
    )
    public void consumeNotificationEvent(Incident notificationMessage) {
        log.info("Received notification message: {}", notificationMessage);
        try {
            notificationService.notifyIncident(notificationMessage);
            log.info("Notification sent successfully for incident: {}", notificationMessage.getIncidentNumber());
        } catch (Exception e) {
            log.error("Failed to send notification for incident: {}. Error: {}", notificationMessage.getIncidentNumber(), e.getMessage());
        }
    }
}
