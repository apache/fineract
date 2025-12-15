package ng.com.createsoftware.fn_loaning_service.repository;

import ng.com.createsoftware.fn_loaning_service.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
}
