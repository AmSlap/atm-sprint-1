package ma.atm.dataingestionservice.service.impl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ma.atm.dataingestionservice.exception.MessageProcessingException;
import ma.atm.dataingestionservice.model.ConfigurationMessage;
import ma.atm.dataingestionservice.pulsar.producer.ProcessedMessagePublisher;
import ma.atm.dataingestionservice.service.ConfigurationMessageService;
import ma.atm.dataingestionservice.service.MessageDispatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Concrete implementation for processing ATM configuration messages.
 */
@Service
@Slf4j
public class ConfigurationMessageServiceImpl implements ConfigurationMessageService {
    @Autowired
    private ProcessedMessagePublisher processedMessagePublisher;



    @Override
    public void process(ConfigurationMessage message) throws MessageProcessingException {

        log.info("Processing Configuration Message for ATM ID: {}", message.getAtmId());
        log.info("Configuration Message Details: {}", message);
        // Validate required fields
        if (message.getAtmId() == null || message.getAtmId().isEmpty()) {
            log.error("Invalid Configuration Message: missing ATM ID.");
            throw new IllegalArgumentException("ATM ID is required.");
        }
        if (message.getPeripherals() == null) {
            log.error("Invalid Configuration Message: missing peripheral data for ATM {}.", message.getAtmId());
            throw new IllegalArgumentException("Peripheral data is required.");
        }





            // Publish to the internal Pulsar topic
        processedMessagePublisher.publishConfigurationEvent(message);




        // TODO: Add validation logic for peripheral data
        // TODO: Publish processed event to internal Pulsar topic (e.g., internal-atm-configuration-events)
    }
}
