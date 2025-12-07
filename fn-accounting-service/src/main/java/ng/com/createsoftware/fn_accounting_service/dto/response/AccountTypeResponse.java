package ng.com.createsoftware.fn_accounting_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTypeResponse {
    private Long id;
    private String name;
    private String description;
}
