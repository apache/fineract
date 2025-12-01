package ng.com.createsoftware.fn_branch_service.repository;

import ng.com.createsoftware.fn_branch_service.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, Long> {
}
