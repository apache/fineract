package ng.com.createsoftware.fn_agency_banking_service.repository;

import ng.com.createsoftware.fn_agency_banking_service.model.Vault;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaultRepository extends JpaRepository<Vault, Long> {
}
