package ng.com.createsoftware.fn_accounting_service.repository;

import ng.com.createsoftware.fn_accounting_service.model.Capital;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CapitalRepository extends JpaRepository<Capital, Long> {
}
