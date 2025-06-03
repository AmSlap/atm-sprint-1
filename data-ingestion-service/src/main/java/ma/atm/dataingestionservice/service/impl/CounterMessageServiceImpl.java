package ma.atm.dataingestionservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import ma.atm.dataingestionservice.model.CounterMessage;
import ma.atm.dataingestionservice.pulsar.producer.ProcessedMessagePublisher;
import ma.atm.dataingestionservice.service.CounterMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Concrete implementation for processing ATM counter messages.
 */
@Service
@Slf4j
public class CounterMessageServiceImpl implements CounterMessageService {

    @Autowired
    private ProcessedMessagePublisher processedMessagePublisher;



    @Override
    public void process(CounterMessage message) {
        log.info("Processing Counter Message for ATM ID: {}", message.getAtmId());
        log.debug("Counter Message Details: {}", message);

        if (message.getAtmId() == null || message.getAtmId().isEmpty()) {
            log.error("Invalid Counter Message: missing ATM ID.");
            throw new IllegalArgumentException("ATM ID is required.");
        }
        if (message.getCassettes() == null || message.getRejectBin() == null) {
            log.error("Invalid Counter Message: missing cassette or reject bin data for ATM {}.", message.getAtmId());
            throw new IllegalArgumentException("Cassette and reject bin data are required.");
        }

        processedMessagePublisher.publishCounterEvent(message);
    }
}
