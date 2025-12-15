package ng.com.createsoftware.fn_customer_service.repository;

import ng.com.createsoftware.fn_customer_service.model.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Long> {
    List<CustomerAccount> findByCustomerId(Long customerId);
    Optional<CustomerAccount> findByAccountNumber(String accountNumber);
}
