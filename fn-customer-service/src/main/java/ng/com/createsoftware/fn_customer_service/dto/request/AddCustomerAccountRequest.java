package ng.com.createsoftware.fn_customer_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddCustomerAccountRequest {
    private String accountNumber;
    private String productCode; //eg SAVINGS-001
    private String currency = "NGN";
    private BigDecimal balance = BigDecimal.ZERO;
    private String status;
    private String customerFirstName;
}
