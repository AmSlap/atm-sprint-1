package ma.atm.atmregistryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.pulsar.annotation.EnablePulsar;

@SpringBootApplication
@EnableDiscoveryClient
@EnablePulsar
@EnableFeignClients
public class AtmRegistryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AtmRegistryServiceApplication.class, args);
	}

}
