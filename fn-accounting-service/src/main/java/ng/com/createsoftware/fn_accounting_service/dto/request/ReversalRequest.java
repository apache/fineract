package ng.com.createsoftware.fn_accounting_service.dto.request;

public record ReversalRequest(
        Long originalTransactionId,
        String reason,
        String requestedBy
) {
}
