package ng.com.createsoftware.fn_accounting_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_accounting_service.dto.response.BalanceSheetRow;
import ng.com.createsoftware.fn_accounting_service.dto.response.GLSummaryRow;
import ng.com.createsoftware.fn_accounting_service.dto.response.IncomeStatement;
import ng.com.createsoftware.fn_accounting_service.dto.response.TrialBalanceRow;
import ng.com.createsoftware.fn_accounting_service.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/trial-balance")
    public ResponseEntity<List<TrialBalanceRow>> trialBalance(){
        return new ResponseEntity<>(reportService.trialBalance(), HttpStatus.OK);
    }

    @GetMapping("/gl-summary")
    public ResponseEntity<List<GLSummaryRow>> glSummaryHandler(
            @RequestParam("from") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)LocalDateTime from,
            @RequestParam("to") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)LocalDateTime to
            ){
        return new ResponseEntity<>(reportService.glSummary(from, to), HttpStatus.OK);
    }

    @GetMapping("/balance-sheet")
    public ResponseEntity<List<BalanceSheetRow>> balanceSheetHandler(){
        return new ResponseEntity<>(reportService.balanceSheet(), HttpStatus.OK);
    }

    @GetMapping("/income-statement")
    public ResponseEntity<IncomeStatement> incomeStatementHandler(
            @RequestParam("from") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)LocalDateTime from,
            @RequestParam("to") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)LocalDateTime to
            ){
        return new ResponseEntity<>(reportService.incomeStatement(from, to), HttpStatus.OK);
    }
}
