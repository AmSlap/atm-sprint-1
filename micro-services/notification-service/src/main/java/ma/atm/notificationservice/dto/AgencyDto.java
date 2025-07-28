package ma.atm.notificationservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgencyDto {
    private String agencyCode; // e.g., "AG001"

    private String agencyName;

    private String region;

    private String address;

    private String contactPerson;

    private String contactEmail;

    private String contactPhone;

}
