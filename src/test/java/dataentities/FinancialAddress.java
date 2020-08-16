package dataentities;

import lombok.Data;

@Data
public class FinancialAddress {
    public FinancialAddress() {
    }

    public FinancialAddress(String type, String address) {
        this.type = type;
        this.address = address;
    }

    private String type;
    private String address;
}
