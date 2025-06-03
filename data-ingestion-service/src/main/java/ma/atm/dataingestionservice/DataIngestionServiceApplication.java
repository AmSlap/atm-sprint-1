package ma.atm.dataingestionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.pulsar.annotation.EnablePulsar;

@SpringBootApplication
@EnablePulsar
public class DataIngestionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataIngestionServiceApplication.class, args);
	}

}
