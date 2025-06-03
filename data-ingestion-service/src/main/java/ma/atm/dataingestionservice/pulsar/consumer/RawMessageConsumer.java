package ma.atm.dataingestionservice.pulsar.consumer;


import lombok.extern.slf4j.Slf4j;
import ma.atm.dataingestionservice.exception.MessageProcessingException;
import ma.atm.dataingestionservice.service.MessageDispatcherService;
import org.apache.pulsar.client.api.SubscriptionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RawMessageConsumer {

    @Autowired
    private MessageDispatcherService messageDispatcherService;




    @PulsarListener(
            topics = "${pulsar.consumer.topic}",
            subscriptionName = "${pulsar.consumer.subscription-name}",
            subscriptionType = SubscriptionType.Exclusive
    )
    public void consumeAtmEvent(String event) throws MessageProcessingException {
        log.info("Received ATM event: {}", event);
        try {
            messageDispatcherService.dispatch(event);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }




    }


}