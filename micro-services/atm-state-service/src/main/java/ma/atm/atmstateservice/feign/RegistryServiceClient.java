package ma.atm.atmstateservice.feign;

import ma.atm.atmstateservice.dto.AgencyDto;
import ma.atm.atmstateservice.dto.AtmInfoDto;
import ma.atm.atmstateservice.dto.AtmRegistryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
@FeignClient(name = "atm-registry-service") // This should match your registry service's spring.application.name
public interface RegistryServiceClient {

    @GetMapping("/api/registry/atms")
    List<AtmRegistryDto> getAllAtmRegistry();

    @GetMapping("/api/registry/atms/{atmId}")
    AtmRegistryDto getAtmRegistry(@PathVariable("atmId") String atmId);

    @GetMapping("/api/registry/agencies")
    List<AgencyDto> getAllAgencies();

    @GetMapping("/api/registry/agencies/{agencyCode}")
    AgencyDto getAgency(@PathVariable("agencyCode") String agencyCode);
}