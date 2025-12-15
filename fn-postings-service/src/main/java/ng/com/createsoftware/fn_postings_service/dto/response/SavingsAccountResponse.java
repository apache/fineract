package ng.com.createsoftware.fn_postings_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavingsAccountResponse {
    private Long id;
    private String name;
    private BigDecimal balance;
}
