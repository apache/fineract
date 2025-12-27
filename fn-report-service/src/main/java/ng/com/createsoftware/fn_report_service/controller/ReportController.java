package ng.com.createsoftware.fn_report_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_report_service.dto.response.BalanceSheetResponse;
import ng.com.createsoftware.fn_report_service.dto.response.LoanBalanceResponse;
import ng.com.createsoftware.fn_report_service.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/balance-sheet")
    public ResponseEntity<BalanceSheetResponse> balanceSheetHandler(){
        return new ResponseEntity<>(reportService.balanceSheet(), HttpStatus.OK);
    }

    @GetMapping("/loan-balance")
    public ResponseEntity<List<LoanBalanceResponse>> loanBalanceHandler(){
        return new ResponseEntity<>(reportService.loanBalance(), HttpStatus.OK);
    }
}
