package ng.com.createsoftware.fn_customer_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddCustomerRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String phone;
    private String email;
    private String bvn;
}
