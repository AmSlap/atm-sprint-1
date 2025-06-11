package ma.atm.atmapigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AtmApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtmApiGatewayApplication.class, args);
    }

}
