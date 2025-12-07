package ng.com.createsoftware.fn_accounting_service.service;

import ng.com.createsoftware.fn_accounting_service.dto.response.AuditPayload;

public interface AccountingAuditConsumerService {
    void listen(AuditPayload payload);
}
