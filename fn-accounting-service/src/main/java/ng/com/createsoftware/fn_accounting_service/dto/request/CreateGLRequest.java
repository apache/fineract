package ng.com.createsoftware.fn_accounting_service.dto.request;

import lombok.Data;

@Data
public class CreateGLRequest {
    private Integer accountClassification;
    private String searchParam;
    private Integer usage;
    private boolean manualTransactionsAllowed;
    private boolean disabled;
}
