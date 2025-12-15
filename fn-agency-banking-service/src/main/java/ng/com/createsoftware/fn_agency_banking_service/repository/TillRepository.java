package ng.com.createsoftware.fn_agency_banking_service.repository;

import ng.com.createsoftware.fn_agency_banking_service.model.Till;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TillRepository extends JpaRepository<Till, Long> {
}
