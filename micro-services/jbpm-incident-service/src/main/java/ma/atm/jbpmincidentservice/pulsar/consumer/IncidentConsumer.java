package ma.atm.jbpmincidentservice.pulsar.consumer;


import lombok.extern.slf4j.Slf4j;
import ma.atm.jbpmincidentservice.jbpm.IncidentProcessService;
import ma.atm.jbpmincidentservice.model.IncidentMessage;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.common.schema.SchemaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IncidentConsumer {

    @Autowired
    private IncidentProcessService incidentProcessService;

    @PulsarListener(
            topics = "${pulsar.consumer.incident-topic}",
            subscriptionName = "${pulsar.consumer.subscription-name}",
            schemaType = SchemaType.JSON,
            subscriptionType = SubscriptionType.Exclusive

    )
    public void consumeIncidentEvent(IncidentMessage event) {

        log.info("Received incident event: {}", event);
        incidentProcessService.startIncidentProcess(event.getAtmId(),event.getErrorType(),event.getIncidentDescription());
    }

}
