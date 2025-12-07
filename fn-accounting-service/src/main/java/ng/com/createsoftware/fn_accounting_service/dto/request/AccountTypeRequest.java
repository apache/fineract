package ng.com.createsoftware.fn_accounting_service.dto.request;

import lombok.Data;

@Data
public class AccountTypeRequest {
    private Long id;
    private String name;
    private String description;
}
