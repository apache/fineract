package ng.com.createsoftware.fn_customer_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateClientResponse {
    private String accountNumber;
    private Integer status;
    private String firstname;
    private String middlename;
    private String lastname;
    private String fullname;
    private String displayName;
    private String mobileNo;
    private boolean isStaff;
    private LocalDate dateOfBirth;
}
