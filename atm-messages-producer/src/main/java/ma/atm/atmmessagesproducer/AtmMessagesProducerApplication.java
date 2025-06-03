package ma.atm.atmmessagesproducer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.pulsar.annotation.EnablePulsar;

@SpringBootApplication
@EnablePulsar
public class AtmMessagesProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtmMessagesProducerApplication.class, args);
    }

}
