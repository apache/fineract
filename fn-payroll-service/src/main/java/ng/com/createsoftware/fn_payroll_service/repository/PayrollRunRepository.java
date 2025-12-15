package ng.com.createsoftware.fn_payroll_service.repository;

import ng.com.createsoftware.fn_payroll_service.model.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {
}
