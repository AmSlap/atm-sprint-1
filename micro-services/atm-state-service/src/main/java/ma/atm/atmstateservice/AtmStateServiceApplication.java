package ma.atm.atmstateservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.pulsar.annotation.EnablePulsar;

@SpringBootApplication
@EnablePulsar
@EnableDiscoveryClient
public class AtmStateServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AtmStateServiceApplication.class, args);
	}

}
