package ma.atm.dataingestionservice.controller;


import lombok.extern.slf4j.Slf4j;
import ma.atm.dataingestionservice.exception.MessageProcessingException;
import ma.atm.dataingestionservice.service.MessageDispatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/events")
@Slf4j
public class EventController {

    @Autowired
    private MessageDispatcherService messageDispatcherService;

    @PostMapping("/dispatch")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<String> dispatchEvent(@RequestBody String event) throws MessageProcessingException {
        try{
            messageDispatcherService.dispatch(event);
        }
        catch (Exception e) {
            throw new MessageProcessingException("Failed to dispatch event: " + e.getMessage(), e);
        }
        return ResponseEntity.ok("Event dispatched successfully");
    }

}
