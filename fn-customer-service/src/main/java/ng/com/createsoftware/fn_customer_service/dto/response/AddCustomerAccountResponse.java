package ng.com.createsoftware.fn_customer_service.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddCustomerAccountResponse {
    @NotBlank
    private String productCode;
    @NotBlank
    private String accountNumber;
    private String currency = "NGN";
}
