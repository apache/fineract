package ng.com.createsoftware.fn_loaning_service.service;

import ng.com.createsoftware.fn_loaning_service.dto.request.LoanApplyRequest;
import ng.com.createsoftware.fn_loaning_service.model.LoanApplication;

public interface LoanWorkFlowService {
    LoanApplication apply(LoanApplyRequest request);
    void approve (Long loanId);
    void disburse(Long loanId);
}
