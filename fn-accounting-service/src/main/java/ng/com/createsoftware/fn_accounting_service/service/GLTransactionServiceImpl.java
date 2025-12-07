package ng.com.createsoftware.fn_accounting_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_accounting_service.dto.request.GLTransactionRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.GLTransactionResponse;
import ng.com.createsoftware.fn_accounting_service.events.AccountingEventPublisher;
import ng.com.createsoftware.fn_accounting_service.mapper.GLTransactionMapper;
import ng.com.createsoftware.fn_accounting_service.model.GLTransaction;
import ng.com.createsoftware.fn_accounting_service.model.GeneralLedger;
import ng.com.createsoftware.fn_accounting_service.model.TransactionType;
import ng.com.createsoftware.fn_accounting_service.repository.GLTransactionRepository;
import ng.com.createsoftware.fn_accounting_service.repository.GeneralLedgerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GLTransactionServiceImpl implements GLTransactionService{

    private final GLTransactionRepository glTransactionRepository;
    private final GeneralLedgerRepository generalLedgerRepository;
    private final PostingRulesService postingRulesService;
    private final AccountingEventPublisher accountingEventPublisher;

    @Transactional
    @Override
    public GLTransactionResponse addGLTransaction(GLTransactionRequest request) {

        postingRulesService.validatePosting(request.getLedgerId(), request.getAmount());

        GeneralLedger ledger = generalLedgerRepository.findById(request.getLedgerId()).orElseThrow(() -> new IllegalArgumentException("GL Transaction not found."));
//        BigDecimal signed = (TransactionType.DEBIT.equals(request.getType())) ?  (request.getAmount()).negate() : request.getAmount();
//        ledger.setBalance(ledger.getBalance().add(signed));

        if(request.getType() == TransactionType.DEBIT)
            ledger.setBalance(ledger.getBalance().subtract(request.getAmount()));
        else
            ledger.setBalance(ledger.getBalance().add(request.getAmount()));

        generalLedgerRepository.save(ledger);

        GLTransaction transaction = GLTransaction.builder()
                .ledger(ledger)
                .amount(request.getAmount())
                .type(TransactionType.valueOf(request.getType().name()))
                .reference(request.getReference())
                .narration(request.getNarration())
                .timestamp(LocalDateTime.now())
                .build();
        transaction = glTransactionRepository.save(transaction);
        //publish
        accountingEventPublisher.publishTransactionEvent(transaction);
        return GLTransactionMapper.gLTransactionToGLTransactionResponse(transaction);
    }

    @Override
    public List<GLTransactionResponse> getGLTransaction(Long ledgerId) {
        return glTransactionRepository.findByLedgerIdOrderByTimestampDesc(ledgerId)
                .stream().map(GLTransactionMapper::gLTransactionToGLTransactionResponse)
                .toList();
    }
}
