package ng.com.createsoftware.fn_accounting_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GLRequest {
//    private Long ledgerId;
    private BigDecimal amount;
   private String narration;
}
