package ng.com.createsoftware.fn_loaning_service.repository;

import ng.com.createsoftware.fn_loaning_service.model.LoanApplication;
import ng.com.createsoftware.fn_loaning_service.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByStatus(Status status);
}
