package ng.com.createsoftware.fn_accounting_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_accounting_service.dto.request.GeneralLedgerRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.GeneralLedgerResponse;
import ng.com.createsoftware.fn_accounting_service.mapper.GeneralLedgerMapper;
import ng.com.createsoftware.fn_accounting_service.model.AccountCategory;
import ng.com.createsoftware.fn_accounting_service.model.GeneralLedger;
import ng.com.createsoftware.fn_accounting_service.repository.AccountCategoryRepository;
import ng.com.createsoftware.fn_accounting_service.repository.GeneralLedgerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralLedgerServiceImpl implements GeneralLedgerService{

    private final GeneralLedgerRepository generalLedgerRepository;
    private final AccountCategoryRepository accountCategoryRepository;

    @Override
    public List<GeneralLedgerResponse> getGeneralLedgers() {
        return  generalLedgerRepository.findAll().stream()
                .map(GeneralLedgerMapper::generalLedgerToGeneralLedgerResponse)
                .toList();
    }

    @Override
    public GeneralLedgerResponse getGeneralLedger(Long generalLedgerId) {
        return generalLedgerRepository.findById(generalLedgerId).map(GeneralLedgerMapper::generalLedgerToGeneralLedgerResponse)
                .orElse(null);
    }

    @Transactional
    @Override
    public GeneralLedgerResponse addGeneralLedger(GeneralLedgerRequest request) {
        AccountCategory accountCategory = null;
        if(request.getCategoryId() != null)
            accountCategory = accountCategoryRepository.findById(request.getCategoryId()).orElse(null);
        GeneralLedger ledger = GeneralLedger.builder()
                .code(request.getCode())
                .name(request.getName())
                .category(accountCategory)
                .balance(request.getBalance() == null ? BigDecimal.ZERO : request.getBalance())
                .build();
        ledger = generalLedgerRepository.save(ledger);
        return GeneralLedgerMapper.generalLedgerToGeneralLedgerResponse(ledger);
    }

    @Transactional
    @Override
    public GeneralLedgerResponse generalLedgerFund(Long ledgerId, BigDecimal amount, String narration) {
        var ledger = generalLedgerRepository.findById(ledgerId).orElseThrow(() -> new IllegalArgumentException("General Ledger not found."));

        ledger.setBalance( (ledger.getBalance()).add( amount));
        generalLedgerRepository.save(ledger);
        return GeneralLedgerMapper.generalLedgerToGeneralLedgerResponse(ledger);
    }
}
