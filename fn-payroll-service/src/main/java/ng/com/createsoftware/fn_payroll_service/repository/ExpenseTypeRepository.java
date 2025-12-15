package ng.com.createsoftware.fn_payroll_service.repository;

import ng.com.createsoftware.fn_payroll_service.model.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseTypeRepository extends JpaRepository<ExpenseType, Long> {
}
