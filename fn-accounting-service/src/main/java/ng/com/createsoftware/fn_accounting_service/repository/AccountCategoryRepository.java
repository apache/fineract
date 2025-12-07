package ng.com.createsoftware.fn_accounting_service.repository;

import ng.com.createsoftware.fn_accounting_service.model.AccountCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountCategoryRepository extends JpaRepository<AccountCategory, Long> {
}
