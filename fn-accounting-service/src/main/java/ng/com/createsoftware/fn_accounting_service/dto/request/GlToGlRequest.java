package ng.com.createsoftware.fn_accounting_service.dto.request;

public record GlToGlRequest(
        String fromGlCode,
        String toGlCode,
        Double amount,
        String narration,
        String requestedBy
) {
}
