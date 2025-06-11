package ma.atm.atmstateservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CassetteDto {
    private String cassetteId;
    private Integer denomination;
    private String currency;
    private Integer notesRemaining;
    private String cassetteStatus;
}
