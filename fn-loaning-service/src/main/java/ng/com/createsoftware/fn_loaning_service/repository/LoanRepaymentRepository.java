package ng.com.createsoftware.fn_loaning_service.repository;

import ng.com.createsoftware.fn_loaning_service.model.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {
    List<LoanRepayment> findByLoanId(Long loanId);
}
