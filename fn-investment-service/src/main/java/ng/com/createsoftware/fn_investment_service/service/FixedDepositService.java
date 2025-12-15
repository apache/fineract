package ng.com.createsoftware.fn_investment_service.service;

import ng.com.createsoftware.fn_investment_service.dto.request.AddFixedDepositRequest;
import ng.com.createsoftware.fn_investment_service.model.FixedDepositAccount;

public interface FixedDepositService {
    FixedDepositAccount created(AddFixedDepositRequest request);

    void liguidate(Long fixedDepositId);
}
