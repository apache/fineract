package ng.com.createsoftware.fn_report_service.repository;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_report_service.dto.response.BalanceSheetResponse;
import ng.com.createsoftware.fn_report_service.dto.response.LoanBalanceResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportRepository {

    private final JdbcTemplate jdbc;

    public BalanceSheetResponse balanceSheet() {
        return jdbc.queryForObject("""
      SELECT
        SUM(CASE WHEN account_type='ASSET' THEN balance ELSE 0 END) assets,
        SUM(CASE WHEN account_type='LIABILITY' THEN balance ELSE 0 END) liabilities,
        SUM(CASE WHEN account_type='EQUITY' THEN balance ELSE 0 END) equity
      FROM acc_gl_account
    """, (rs, i) -> {
            BalanceSheetResponse dto = new BalanceSheetResponse();
            dto.setAssets(rs.getBigDecimal("assets"));
            dto.setLiabilities(rs.getBigDecimal("liabilities"));
            dto.setEquity(rs.getBigDecimal("equity"));
            return dto;
        });
    }

    public List<LoanBalanceResponse> loanBalances() {
        return jdbc.query("""
      SELECT id loanId, outstanding_loan_balance outstanding
      FROM m_loan
    """, (rs, i) -> {
            LoanBalanceResponse dto = new LoanBalanceResponse();
            dto.setLoanId(rs.getLong("loanId"));
            dto.setOutstanding(rs.getBigDecimal("outstanding"));
            return dto;
        });
    }
}

