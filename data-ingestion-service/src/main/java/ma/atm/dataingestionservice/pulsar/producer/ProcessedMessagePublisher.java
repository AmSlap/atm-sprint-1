package ma.atm.dataingestionservice.pulsar.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ma.atm.dataingestionservice.model.ConfigurationMessage;
import ma.atm.dataingestionservice.model.CounterMessage;
import ma.atm.dataingestionservice.model.StatusMessage;
import ma.atm.dataingestionservice.model.TransactionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProcessedMessagePublisher {

    @Autowired
    private PulsarTemplate<String> pulsarTemplate; // Only need String template

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


    // Method for StatusMessage
    public void publishStatusEvent(StatusMessage event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            pulsarTemplate.send(status_topic, jsonMessage);
            log.info("Published status event to topic {}: {}", status_topic, jsonMessage);
        } catch (Exception e) {
            log.error("Failed to publish status event to topic {}: {}", status_topic, e.getMessage(), e);
        }
    }

    // Method for ConfigurationMessage
    public void publishConfigurationEvent(ConfigurationMessage event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            pulsarTemplate.send(configuration_topic, jsonMessage);
            log.info("Published configuration event to topic {}: {}", configuration_topic, jsonMessage);
        } catch (Exception e) {
            log.error("Failed to publish configuration event to topic {}: {}", configuration_topic, e.getMessage(), e);
        }
    }

    // Method for CounterMessage
    public void publishCounterEvent(CounterMessage event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            pulsarTemplate.send(counter_topic, jsonMessage);
            log.info("Published counter event to topic {}: {}", counter_topic, jsonMessage);
        } catch (Exception e) {
            log.error("Failed to publish counter event to topic {}: {}", counter_topic, e.getMessage(), e);
        }
    }

    // Method for TransactionMessage
    public void publishTransactionEvent(TransactionMessage event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            pulsarTemplate.send(transaction_topic, jsonMessage);
            log.info("Published transaction event to topic {}: {}", transaction_topic, jsonMessage);
        } catch (Exception e) {
            log.error("Failed to publish transaction event to topic {}: {}", transaction_topic, e.getMessage(), e);
        }
    }
}
