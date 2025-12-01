package ng.com.createsoftware.fn_account_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountOfficerRequest {
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

