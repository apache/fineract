package ng.com.createsoftware.fn_customer_service.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateClientRequest {
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
