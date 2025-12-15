package ng.com.createsoftware.fn_accounting_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_accounting_service.dto.request.CustomerToGlRequest;
import ng.com.createsoftware.fn_accounting_service.dto.request.GlToCustomerRequest;
import ng.com.createsoftware.fn_accounting_service.dto.request.GlToGlRequest;
import ng.com.createsoftware.fn_accounting_service.dto.request.ReversalRequest;
import ng.com.createsoftware.fn_accounting_service.repository.GLTransactionRepository;
import ng.com.createsoftware.fn_accounting_service.repository.GeneralLedgerRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountingPostingServiceImpl implements AccountingPostingService{
    private final GeneralLedgerRepository generalLedgerRepository;
    private final GLTransactionRepository glTransactionRepository;
//    private final CustomerAccountRepository
    private final CustomerRe
    @Override
    public Map<String, Object> postGlToGl(GlToGlRequest request) {
        return Map.of();
    }

    @Override
    public Map<String, Object> postGlToCustomer(GlToCustomerRequest request) {
        return Map.of();
    }

    @Override
    public Map<String, Object> postCustomerToGl(CustomerToGlRequest request) {
        return Map.of();
    }

    @Override
    public Map<String, Object> reverseTransaction(ReversalRequest request) {
        return Map.of();
    }
}
