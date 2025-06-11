package ma.atm.dataingestionservice.controller;


import lombok.extern.slf4j.Slf4j;
import ma.atm.dataingestionservice.exception.MessageProcessingException;
import ma.atm.dataingestionservice.service.MessageDispatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/events")
@Slf4j
public class EventController {

    @Autowired
    private MessageDispatcherService messageDispatcherService;

    @PostMapping("/dispatch")
    public String dispatchEvent(String event) throws MessageProcessingException {
        try{
            messageDispatcherService.dispatch(event);
            log.info("Event dispatched successfully: {}", event);
        }
        catch (Exception e) {
            throw new MessageProcessingException("Failed to dispatch event: " + e.getMessage(), e);
        }
        return "Event dispatched successfully";
    }

}
