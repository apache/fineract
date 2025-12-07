package ng.com.createsoftware.fn_accounting_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_accounting_service.dto.response.BalanceSheetRow;
import ng.com.createsoftware.fn_accounting_service.dto.response.GLSummaryRow;
import ng.com.createsoftware.fn_accounting_service.dto.response.IncomeStatement;
import ng.com.createsoftware.fn_accounting_service.dto.response.TrialBalanceRow;
import ng.com.createsoftware.fn_accounting_service.model.GLTransaction;
import ng.com.createsoftware.fn_accounting_service.model.GeneralLedger;
import ng.com.createsoftware.fn_accounting_service.repository.GLTransactionRepository;
import ng.com.createsoftware.fn_accounting_service.repository.GeneralLedgerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService{

    private final GeneralLedgerRepository generalLedgerRepository;
    private final GLTransactionRepository glTransactionRepository;

    @Override
    public List<TrialBalanceRow> trialBalance() {
         List<GeneralLedger> generalLedgerList = generalLedgerRepository.findAll();
         return generalLedgerList.stream()
                 .map(g -> new TrialBalanceRow(g.getCode(), g.getName(), g.getBalance()))
                 .sorted(Comparator.comparing(TrialBalanceRow::code))
                 .toList();
    }

    @Override
    public List<GLSummaryRow> glSummary(LocalDateTime from, LocalDateTime to) {
        //get all transactions and group by generalLedger
        List<GLTransaction> glTransactionList  = glTransactionRepository.findAll();
        Map<Long, List<GLTransaction>> glMap = glTransactionList.stream()
                .filter(t -> !t.getTimestamp().isBefore(from) && !t.getTimestamp().isAfter(to))
                .collect(Collectors.groupingBy(t-> t.getLedger().getId()));

        List<GLSummaryRow> rows = new ArrayList<>();
        for(Map.Entry<Long, List<GLTransaction>> e : glMap.entrySet()){
            GeneralLedger ledger = generalLedgerRepository.findById(e.getKey()).orElse(null);
            if(ledger == null) continue;
            BigDecimal debits = e.getValue().stream()
                    .filter(t -> "DEBIT".equalsIgnoreCase(t.getType().name()))
                    .map(GLTransaction::getAmount)                 // returns BigDecimal
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal credits = e.getValue().stream()
                    .filter(t -> "CREDIT".equalsIgnoreCase(t.getType().name()))
                    .map(GLTransaction::getAmount)                 // returns BigDecimal
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            rows.add(new GLSummaryRow(ledger.getCode(), ledger.getName(), debits, credits, ledger.getBalance()));
        }
        rows.sort(Comparator.comparing(GLSummaryRow::getCode));
        return rows;
    }

    //simplified balance sheet
    @Override
    public List<BalanceSheetRow> balanceSheet() {
         List<GeneralLedger> generalLedgerList = generalLedgerRepository.findAll();
         Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
         for(GeneralLedger ledger : generalLedgerList){
             String category = (ledger.getCategory() != null ? ledger.getCategory().getName() : "UNCATEGORIZED");
             BigDecimal total = byCategory.getOrDefault(category, BigDecimal.ZERO).add(ledger.getBalance() == null ? BigDecimal.ZERO: ledger.getBalance());
             byCategory.put(category,total);
         }
         return byCategory.entrySet().stream()
                 .map(e -> new BalanceSheetRow(e.getKey(), e.getValue()))
                 .toList();
    }

    //simplified income sheet
    @Override
    public IncomeStatement incomeStatement(LocalDateTime from, LocalDateTime to) {
        List<GLSummaryRow> glSummaryRowList = glSummary(from,to);
        BigDecimal income = glSummaryRowList.stream()
                .filter(r -> r.getCode().startsWith("4") || r.getCode().toLowerCase().contains("income"))
                .map(GLSummaryRow::getCredits)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expense = glSummaryRowList.stream()
                .filter(r -> r.getCode().startsWith("5") || r.getCode().toLowerCase().contains("expense"))
                .map(GLSummaryRow::getDebits)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new IncomeStatement(income, expense, income.subtract(expense));
    }
}
