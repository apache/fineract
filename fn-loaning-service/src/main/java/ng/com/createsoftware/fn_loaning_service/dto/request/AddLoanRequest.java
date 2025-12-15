package ng.com.createsoftware.fn_loaning_service.dto.request;

import lombok.Data;

@Data
public class AddLoanRequest {
    private Long customerId;
    private String productCode;
    private Double amount;
    private Integer tenure;
}
