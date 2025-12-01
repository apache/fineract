package ng.com.createsoftware.fn_account_service.repository;

import ng.com.createsoftware.fn_account_service.model.AccountOfficer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountOfficerRepository extends JpaRepository<AccountOfficer, Long> {
    Optional<AccountOfficer> findByCode(String code);
    List<AccountOfficer> findByBranchCode(String branchCode);
}

