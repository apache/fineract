package ng.com.createsoftware.fn_asset_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {
    private String accountNumber;
    private Long officeId;
    private Integer status;
    private LocalDate activationDate;
    private LocalDate officeJoiningDate;
    private String firstname;
    private String middleName;
    private String lastname;
    private String displayName;
    private String mobileNo;
    private String emailAddress;
    private boolean isStaff;

}
