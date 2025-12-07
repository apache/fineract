package ng.com.createsoftware.fn_accounting_service.repository;

import ng.com.createsoftware.fn_accounting_service.model.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountTypeRepository extends JpaRepository<AccountType, Long> {
    Optional<AccountType> findByName(String name);
}
