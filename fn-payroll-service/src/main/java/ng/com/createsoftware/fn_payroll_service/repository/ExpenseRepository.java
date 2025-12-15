package ng.com.createsoftware.fn_payroll_service.repository;

import ng.com.createsoftware.fn_payroll_service.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
