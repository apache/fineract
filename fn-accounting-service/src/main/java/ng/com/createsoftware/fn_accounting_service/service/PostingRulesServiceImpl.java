package ng.com.createsoftware.fn_accounting_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_accounting_service.model.GeneralLedger;
import ng.com.createsoftware.fn_accounting_service.repository.GeneralLedgerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostingRulesServiceImpl implements PostingRulesService{
    private final GeneralLedgerRepository generalLedgerRepository;
    private final LedgerLockService lockService;
    private boolean preventNegativeBalances = true;
    @Override
    public void validatePosting(long glId, BigDecimal signedChange) {
        Optional<GeneralLedger> ledgerOptional = generalLedgerRepository.findById(glId);
        if(ledgerOptional.isEmpty()) throw new IllegalArgumentException("GL not found: " + glId);
        GeneralLedger ledger = ledgerOptional.get();

        if(lockService.isLocked(glId)) throw new IllegalStateException("Ledger is locked for posting: " + glId);

        BigDecimal newBalance = ledger.getBalance().add(signedChange);

        if(preventNegativeBalances && newBalance.doubleValue() < 0)
            throw new IllegalArgumentException("Posting would create negative balance for GL " + ledger.getCode());
    }

    @Override
    public void setPreventNegativeBalances(boolean prevent) {
        this.preventNegativeBalances = prevent;
    }

    @Override
    public boolean isPreventNegativeBalance() {
        return preventNegativeBalances;
    }
}
