package ng.com.createsoftware.fn_investment_service.repository;

import ng.com.createsoftware.fn_investment_service.model.FixedDepositAccount;
import ng.com.createsoftware.fn_investment_service.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FixedDepositRepository  extends JpaRepository<FixedDepositAccount, Long> {
    List<FixedDepositAccount> findByStatus(Status status);
}
