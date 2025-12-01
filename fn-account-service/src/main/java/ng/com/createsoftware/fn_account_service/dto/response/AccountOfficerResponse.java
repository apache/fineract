package ng.com.createsoftware.fn_account_service.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountOfficerResponse {

    private Long id;
    @NotBlank
    private String code;
    @NotBlank
    private String firstName;
    private String lastName;
    private String phone;
    private String email;

    private String branchCode;
    private String status;
}

