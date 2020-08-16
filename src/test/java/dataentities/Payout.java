package dataentities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Payout {

    public Payout() {
    }

    public Payout(String payoutId, String amount,String currency,FinancialAddress recipient, String correspondent, String country, String statementDescription, LocalDateTime customerTimestamp) {
        this.payoutId = payoutId;
        this.amount = amount;
        this.currency = currency;
        this.recipient = recipient;
        this.correspondent = correspondent;
        this.country = country;
        this.statementDescription = statementDescription;
        this.customerTimestamp = customerTimestamp;
    }

    private String payoutId;

    private String amount;
    private String currency;
    private FinancialAddress recipient;
    private String correspondent;
    private String country;

    private String statementDescription;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime customerTimestamp;

}
