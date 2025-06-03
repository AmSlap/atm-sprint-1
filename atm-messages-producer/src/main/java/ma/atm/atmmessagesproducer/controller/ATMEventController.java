package ma.atm.atmmessagesproducer.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.atm.atmmessagesproducer.model.StatusMessage;
import ma.atm.atmmessagesproducer.producer.ATMEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/atm-events")
@Slf4j
public class ATMEventController {


    private final ATMEventPublisher atmEventPublisher;

    @PostMapping("/publish")
    private void publishEvent(@RequestBody String event) {
        // Create an ATMEvent object and publish it
        // Example: ATMEvent event = new ATMEvent(...);
        log.info("(Controller) Publishing event: {}", event);
        atmEventPublisher.publishEvent(event);
    }
}
