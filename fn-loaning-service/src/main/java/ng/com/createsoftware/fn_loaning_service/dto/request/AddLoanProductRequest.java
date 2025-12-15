package ng.com.createsoftware.fn_loaning_service.dto.request;

import lombok.Data;

@Data
public class AddLoanProductRequest {
    private String productCode;
    private String name;
    private Double interestRate;
    private Integer maxTenure;
    private Double maxAmount;
}
