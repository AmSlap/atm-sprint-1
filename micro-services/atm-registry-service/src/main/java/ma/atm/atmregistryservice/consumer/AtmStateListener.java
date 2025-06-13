package ma.atm.atmregistryservice.consumer;


import lombok.extern.slf4j.Slf4j;
import ma.atm.atmregistryservice.consumer.event.AtmConfigurationChangedEvent;
import ma.atm.atmregistryservice.consumer.event.AtmCountersUpdatedEvent;
import ma.atm.atmregistryservice.consumer.event.AtmStatusUpdatedEvent;
import ma.atm.atmregistryservice.model.AtmInfo;
import ma.atm.atmregistryservice.service.AtmRegistryService;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.common.schema.SchemaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AtmStateListener {


    @Autowired
    private  AtmRegistryService atmRegistryService;


    @PulsarListener(
            topics = "${pulsar.consumer.configuration-topic}",
            subscriptionName = "${pulsar.consumer.subscription-name}",
            schemaType = SchemaType.JSON,
            subscriptionType = SubscriptionType.Exclusive
    )
    public void consumeAtmConfigurationMessage(AtmConfigurationChangedEvent event) {
        if (atmRegistryService.atmExists(event.getAtmId())){
            log.info("Received configuration update for ATM ID: {}", event.getAtmId());
        } else {
            AtmInfo atmInfo = new AtmInfo();
            atmInfo.setAtmId(event.getAtmId());
            atmRegistryService.createAtm(atmInfo, "AG001");
        }


    }

    @PulsarListener(
            topics = "${pulsar.consumer.status-topic}",
            subscriptionName = "${pulsar.consumer.subscription-name}",
            subscriptionType = SubscriptionType.Exclusive
    )
    public void consumeAtmStatusMessage(AtmStatusUpdatedEvent event) {
        if (atmRegistryService.atmExists(event.getAtmId())){
            log.info("Received configuration update for ATM ID: {}", event.getAtmId());
        } else {
            AtmInfo atmInfo = new AtmInfo();
            atmInfo.setAtmId(event.getAtmId());
            atmRegistryService.createAtm(atmInfo, "AG001");
        }
    }

    @PulsarListener(
            topics = "${pulsar.consumer.counter-topic}",
            subscriptionName = "${pulsar.consumer.subscription-name}",
            subscriptionType = SubscriptionType.Exclusive
    )
    public void consumeAtmCounterMessage(AtmCountersUpdatedEvent event) {
        log.info("Received ATM counter update: {}", event);
        if (atmRegistryService.atmExists(event.getAtmId())){
            log.info("Received configuration update for ATM ID: {}", event.getAtmId());
        } else {
            log.info("ATM ID {} does not exist, creating new ATM entry.", event.getAtmId());
            AtmInfo atmInfo = new AtmInfo();
            atmInfo.setAtmId(event.getAtmId());
            atmRegistryService.createAtm(atmInfo, "AG001");
        }
    }



}
