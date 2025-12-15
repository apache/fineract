package ng.com.createsoftware.fn_loaning_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_loaning_service.client.FineractLoanClient;
import ng.com.createsoftware.fn_loaning_service.dto.request.LoanApplyRequest;
import ng.com.createsoftware.fn_loaning_service.model.LoanApplication;
import ng.com.createsoftware.fn_loaning_service.model.Status;
import ng.com.createsoftware.fn_loaning_service.repository.LoanApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanWorkFlowServiceImpl implements LoanWorkFlowService{
    private final LoanApplicationRepository loanApplicationRepository;
    private final FineractLoanClient fineract;

    @Override
    public LoanApplication apply(LoanApplyRequest request) {
        Map<String, Object> body = Map.of(
                "clientId", request.getCustomerId(),
                "productId", request.getProductId(),
                "principal", request.getAmount(),
                "loanTermFrequency", request.getTenureMonths(),
                "loanTermFrequencyType", 2
        );
        Map<String, Object> response = fineract.apply(body);

        LoanApplication loan = new LoanApplication();
        loan.setCustomerId(request.getCustomerId());
        loan.setFineractLoanId(
                Long.valueOf(response.get("resourceId").toString())
        );
        loan.setAmount(request.getAmount());
        loan.setTenureMonths(request.getTenureMonths());
        loan.setStatus(Status.PENDING);
        return loanApplicationRepository.save(loan);
    }

    @Override
    public void approve(Long loanId) {
        LoanApplication loan = loanApplicationRepository.findById(loanId).orElseThrow();
        fineract.command(loan.getFineractLoanId(), "approve", Map.of());
        loan.setStatus(Status.APPROVED);
        loanApplicationRepository.save(loan);
    }

    @Override
    public void disburse(Long loanId) {
        LoanApplication loan = loanApplicationRepository.findById(loanId).orElseThrow();
        fineract.command(loan.getFineractLoanId(), "disburse", Map.of());
        loan.setStatus(Status.DISBURSED);
        loanApplicationRepository.save(loan);
    }
}
