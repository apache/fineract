package ng.com.createsoftware.fn_accounting_service.dto.request;

public record GlToCustomerRequest( String fromGlCode,
                                   String customerAccountNumber,
                                   Double amount,
                                   String narration,
                                   String requestedBy
) {
}


