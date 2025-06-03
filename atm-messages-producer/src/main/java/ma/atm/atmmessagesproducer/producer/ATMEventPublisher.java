package ma.atm.atmmessagesproducer.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ma.atm.atmmessagesproducer.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ATMEventPublisher {

    @Autowired
    private PulsarTemplate<String> pulsarTemplate; // Only need String template

    @Autowired
    private ObjectMapper objectMapper; // Add ObjectMapper for JSON conversion

    @Value("${pulsar.topic.name}")
    private String topic;

    // Fixed version - don't serialize a string that's already JSON
    public void publishEvent(String event) {
        try {
            // If event is already a JSON string, send it directly
            pulsarTemplate.send(topic, event);
            log.info("Published event to topic {}: {}", topic, event);
        } catch (Exception e) {
            log.error("Failed to publish event to topic {}: {}", topic, e.getMessage(), e);
        }
    }
}