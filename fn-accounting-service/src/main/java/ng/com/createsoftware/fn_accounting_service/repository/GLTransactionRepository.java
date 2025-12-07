package ng.com.createsoftware.fn_accounting_service.repository;

import ng.com.createsoftware.fn_accounting_service.model.GLTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GLTransactionRepository extends JpaRepository<GLTransaction, Long> {
    List<GLTransaction> findByLedgerIdOrderByTimestampDesc(Long ledgerId);
}
