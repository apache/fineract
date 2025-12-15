package ng.com.createsoftware.fn_customer_service.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddCustomerResponse {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String phone;
    private String email;
    private String bvn;
}
