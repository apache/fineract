package ng.com.createsoftware.fn_customer_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse {
    private Long id;
    private  String firstName;
    private  String lastName;
    private  String phone;
    private  String email;
    private  String bvn;
    private  String status;
}
