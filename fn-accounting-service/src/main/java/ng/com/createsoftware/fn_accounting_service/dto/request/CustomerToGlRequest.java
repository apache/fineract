package ng.com.createsoftware.fn_accounting_service.dto.request;

public record CustomerToGlRequest ( String customerAccountNumber,
        String toGlCode,
        Double amount,
        String narration,
        String requestedBy
) {
        }


