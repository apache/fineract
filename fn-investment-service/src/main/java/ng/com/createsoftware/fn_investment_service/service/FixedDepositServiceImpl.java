package ng.com.createsoftware.fn_investment_service.service;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_investment_service.client.FineractSavingsClient;
import ng.com.createsoftware.fn_investment_service.dto.request.AddFixedDepositRequest;
import ng.com.createsoftware.fn_investment_service.model.FixedDepositAccount;
import ng.com.createsoftware.fn_investment_service.model.Status;
import ng.com.createsoftware.fn_investment_service.repository.FixedDepositRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FixedDepositServiceImpl implements FixedDepositService{
    private final FixedDepositRepository fixedDepositRepository;
    private final FineractSavingsClient fineract;
    @Override
    public FixedDepositAccount created(AddFixedDepositRequest request) {
        Map<String, Object> body = Map.of(
                "clientId", request.getCustomerId(),
                "productId", request.getProductId(),
                "principal", request.getAmount(),
                "tenure", request.getTenureMonths()
        );
        Map<String, Object> response = fineract.createAccount(body);
        FixedDepositAccount fixedDepositAccount = new FixedDepositAccount();
        fixedDepositAccount.setCustomerId(request.getCustomerId());
        fixedDepositAccount.setFineractAccountId(
                Long.valueOf(response.get("resourceId").toString())
        );
        fixedDepositAccount.setPrincipal(request.getAmount());
        fixedDepositAccount.setTenureMonths(request.getTenureMonths());
        fixedDepositAccount.setStatus(Status.ACTIVE);
        return fixedDepositRepository.save(fixedDepositAccount);
    }

    @Override
    public void liguidate(Long fixedDepositId) {
        FixedDepositAccount fixedDepositAccount = fixedDepositRepository.findById(fixedDepositId).orElseThrow();

        fineract.closeAccount(fixedDepositAccount.getFineractAccountId(), "close", Map.of("note", "Fixed Deposit Liquidation"));

        fixedDepositAccount.setStatus(Status.CLOSED);
        fixedDepositRepository.save(fixedDepositAccount);
    }
}
