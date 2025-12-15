package ng.com.createsoftware.fn_customer_service.dto.response;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ng.com.createsoftware.fn_customer_service.model.Customer;
import ng.com.createsoftware.fn_customer_service.model.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerAccountResponse {
    private Long id;
    private String accountNumber;
    private String productCode; //eg SAVINGS-001
    private String currency = "NGN";
    private BigDecimal balance = BigDecimal.ZERO;
    private String status;
    private String customerFirstName;
}
