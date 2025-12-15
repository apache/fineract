package ng.com.createsoftware.fn_agency_banking_service.repository;

import ng.com.createsoftware.fn_agency_banking_service.model.TillTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TillTransactionRepository extends JpaRepository<TillTransaction, Long> {
}
