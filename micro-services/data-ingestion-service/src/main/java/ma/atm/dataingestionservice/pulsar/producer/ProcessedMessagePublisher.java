package ma.atm.dataingestionservice.pulsar.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ma.atm.dataingestionservice.integration.ConfigurationIntegrationEvent;
import ma.atm.dataingestionservice.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProcessedMessagePublisher {

    @Autowired
    private PulsarTemplate<ConfigurationMessage> configTemplate;
    @Autowired
    private PulsarTemplate<StatusMessage> statusTemplate;
    @Autowired
    private PulsarTemplate<CounterMessage> counterTemplate;
    @Autowired
    private PulsarTemplate<TransactionMessage> transactionTemplate; // Only need String template
    @Autowired
    private PulsarTemplate<IncidentMessage> incidentTemplate; // Assuming you have a PulsarTemplate for IncidentMessage

    @Autowired
    private ObjectMapper objectMapper; // Add ObjectMapper for JSON conversion

    @Value("${pulsar.producer.status-topic}")
    private String status_topic;

    @Value("${pulsar.producer.configuration-topic}")
    private String configuration_topic;

    @Value("${pulsar.producer.counter-topic}")
    private String counter_topic;

    @Value("${pulsar.producer.transaction-topic}")
    private String transaction_topic;

    @Value("${pulsar.producer.incident-topic}")
    private String incident_topic;


    // Method for StatusMessage
    public void publishStatusEvent(StatusMessage event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            statusTemplate.send(status_topic, event);
            log.info("Published status event to topic {}: {}", status_topic, jsonMessage);
        } catch (Exception e) {
            log.error("Failed to publish status event to topic {}: {}", status_topic, e.getMessage(), e);
        }
    }

    // Method for ConfigurationMessage
    public void publishConfigurationEvent(ConfigurationMessage event) {
        try {
            // Convert the message object to JSON string
            String jsonPayload = objectMapper.writeValueAsString(event);

            // Log the exact JSON string being published
            log.info("Publishing JSON to internal-atm-configuration-events: {}", jsonPayload);

            configTemplate.send(configuration_topic, event);
            log.info("Published configuration event to topic {}: {}", configuration_topic, event);
        } catch (Exception e) {
            log.error("Failed to publish configuration event to topic {}: {}", configuration_topic, e.getMessage(), e);
        }
    }

    // Method for CounterMessage
    public void publishCounterEvent(CounterMessage event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            counterTemplate.send(counter_topic, event);
            log.info("Published counter event to topic {}: {}", counter_topic, jsonMessage);
        } catch (Exception e) {
            log.error("Failed to publish counter event to topic {}: {}", counter_topic, e.getMessage(), e);
        }
    }

    // Method for TransactionMessage
    public void publishTransactionEvent(TransactionMessage event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            transactionTemplate.send(transaction_topic, event);
            log.info("Published transaction event to topic {}: {}", transaction_topic, jsonMessage);
        } catch (Exception e) {
            log.error("Failed to publish transaction event to topic {}: {}", transaction_topic, e.getMessage(), e);
        }
    }

    public void publishIncidentEvent(IncidentMessage message) {
        log.info("dkhlt ");
        try {
            log.info("Publishing incident message to topic {}: {}", incident_topic, message);
            // Assuming you have a PulsarTemplate for IncidentMessage
            incidentTemplate.send(incident_topic, message);
            // incidentTemplate.send(incident_topic, message);
            log.info("Published incident event to topic {}: {}", incident_topic, message);
        } catch (Exception e) {
            log.error("Failed to publish incident event to topic {}: {}", incident_topic, e.getMessage(), e);
        }
    }
}
