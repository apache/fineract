package ng.com.createsoftware.fn_accounting_service.service;

import ng.com.createsoftware.fn_accounting_service.dto.response.BalanceSheetRow;
import ng.com.createsoftware.fn_accounting_service.dto.response.GLSummaryRow;
import ng.com.createsoftware.fn_accounting_service.dto.response.IncomeStatement;
import ng.com.createsoftware.fn_accounting_service.dto.response.TrialBalanceRow;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {
    List<TrialBalanceRow> trialBalance();
    List<GLSummaryRow> glSummary(LocalDateTime from, LocalDateTime to);
    List<BalanceSheetRow> balanceSheet();
    IncomeStatement incomeStatement(LocalDateTime from, LocalDateTime to);
}
