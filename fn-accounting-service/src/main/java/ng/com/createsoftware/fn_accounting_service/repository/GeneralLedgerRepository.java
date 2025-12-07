package ng.com.createsoftware.fn_accounting_service.repository;

import ng.com.createsoftware.fn_accounting_service.model.GeneralLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GeneralLedgerRepository extends JpaRepository<GeneralLedger, Long> {
    Optional<GeneralLedger> findByCode(String code);
}
