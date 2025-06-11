package ma.atm.dataingestionservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Represents a transaction message from an ATM.
 * Contains details about a financial transaction.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionMessage extends BaseAtmMessage {
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("transactionType")
    private String transactionType;
    
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("cardType")
    private String cardType;
    
    @JsonProperty("responseCode")
    private String responseCode;
    
    @JsonProperty("responseTime")
    private Double responseTime;
    
    @JsonProperty("dispensedDenominations")
    private List<DispensedDenomination> dispensedDenominations;
    
    @JsonProperty("journalReference")
    private String journalReference;
    
    /**
     * Represents the denominations dispensed during a withdrawal.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DispensedDenomination {
        @JsonProperty("denomination")
        private Integer denomination;
        
        @JsonProperty("count")
        private Integer count;
    }
}
