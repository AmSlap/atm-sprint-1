package ma.atm.atmregistryservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.client.RestTemplate;

import brave.propagation.Propagation;
import brave.propagation.B3Propagation;

@Configuration
public class TracingConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(10000);
        loggingFilter.setIncludeHeaders(true);
        return loggingFilter;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Explicitly select B3 propagation format
    @Bean
    @Primary
    public Propagation.Factory propagationFactory() {
        return B3Propagation.newFactoryBuilder()
                .injectFormat(B3Propagation.Format.MULTI)
                .build();
    }
}
