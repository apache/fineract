package ng.com.createsoftware.fn_report_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_report_service.dto.response.BalanceSheetResponse;
import ng.com.createsoftware.fn_report_service.dto.response.LoanBalanceResponse;
import ng.com.createsoftware.fn_report_service.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ReportRepository repository;

    public BalanceSheetResponse balanceSheet(){
        return repository.balanceSheet();
    }

    public List<LoanBalanceResponse> loanBalance(){
        return repository.loanBalances();
    }
}
