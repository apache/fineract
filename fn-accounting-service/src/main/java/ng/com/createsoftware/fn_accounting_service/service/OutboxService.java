package ng.com.createsoftware.fn_accounting_service.service;

import ng.com.createsoftware.fn_accounting_service.dto.request.OutboxRequest;

public interface OutboxService {
    void saveEvent(OutboxRequest request) throws Exception;
    void publishPending();
}
