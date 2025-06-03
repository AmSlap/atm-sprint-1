package ma.atm.dataingestionservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import ma.atm.dataingestionservice.model.StatusMessage;
import ma.atm.dataingestionservice.pulsar.producer.ProcessedMessagePublisher;
import ma.atm.dataingestionservice.service.StatusMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Concrete implementation for processing ATM status messages.
 */
@Service
@Slf4j
public class StatusMessageServiceImpl implements StatusMessageService {

    @Autowired
    private ProcessedMessagePublisher processedMessagePublisher;

    @Override
    public void process(StatusMessage message) {
        log.info("Processing Status Message for ATM ID: {}", message.getAtmId());
        log.debug("Status Message Details: {}", message);

        if (message.getAtmId() == null || message.getAtmId().isEmpty()) {
            log.error("Invalid Status Message: missing ATM ID.");
            throw new IllegalArgumentException("ATM ID is required.");
        }
        if (message.getOperationalState() == null || message.getOperationalState().isEmpty()) {
            log.error("Invalid Status Message: missing status for ATM {}.", message.getAtmId());
            throw new IllegalArgumentException("Status is required.");
        }
        processedMessagePublisher.publishStatusEvent(message);


    }
}
