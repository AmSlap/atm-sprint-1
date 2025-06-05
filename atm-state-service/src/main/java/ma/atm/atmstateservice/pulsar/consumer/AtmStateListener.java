package ma.atm.atmstateservice.pulsar.consumer;


import lombok.extern.slf4j.Slf4j;
import ma.atm.atmstateservice.event.AtmConfigurationChangedEvent;
import ma.atm.atmstateservice.event.AtmCountersUpdatedEvent;
import ma.atm.atmstateservice.event.AtmStatusUpdatedEvent;
import ma.atm.atmstateservice.integration.ConfigurationIntegrationEvent;
import ma.atm.atmstateservice.model.AtmConfiguration;
import ma.atm.atmstateservice.service.ConfigurationHandlerService;
import ma.atm.atmstateservice.service.CounterHandlerService;
import ma.atm.atmstateservice.service.StatusHandlerService;
import ma.atm.atmstateservice.test.ConfigurationMessage;
import org.apache.pulsar.client.api.SubscriptionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AtmStateListener {
    private final ConfigurationHandlerService configurationHandlerService;
    private final CounterHandlerService counterHandlerService;
    private final StatusHandlerService statusHandlerService;

    @Autowired
    private AtmStateListener(ConfigurationHandlerService configurationHandlerService, CounterHandlerService counterHandlerService, StatusHandlerService statusHandlerService) {
        this.configurationHandlerService = configurationHandlerService;
        this.counterHandlerService = counterHandlerService;
        this.statusHandlerService = statusHandlerService;
    }


    @PulsarListener(
            topics = "${pulsar.consumer.configuration-topic}",
            subscriptionName = "${pulsar.consumer.subscription-name}",
            subscriptionType = SubscriptionType.Exclusive
    )
    public void consumeAtmConfigurationMessage(AtmConfiguration event) {
        log.info("Received ATM Configuration change: {}", event);
        log.info("Processing ATM Configuration change for ATM ID: {}", event.getPeripheralDetails());

    }

    @PulsarListener(
            topics = "${pulsar.consumer.status-topic}",
            subscriptionName = "${pulsar.consumer.subscription-name}",
            subscriptionType = SubscriptionType.Exclusive
    )
    public void consumeAtmStatusMessage(AtmStatusUpdatedEvent event) {
        log.info("Received ATM status update: {}", event);
        try {
            statusHandlerService.processStatusUpdate(event);
        } catch (Exception e) {
            log.error("Error processing ATM Status change: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }

    @PulsarListener(
            topics = "${pulsar.consumer.counter-topic}",
            subscriptionName = "${pulsar.consumer.subscription-name}",
            subscriptionType = SubscriptionType.Exclusive
    )
    public void consumeAtmCounterMessage(AtmCountersUpdatedEvent event) {
        log.info("Received ATM counter update: {}", event);
        try {
            counterHandlerService.processCounterUpdate(event);
        } catch (Exception e) {
            log.error("Error processing ATM counter change: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }



}
