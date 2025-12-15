package ng.com.createsoftware.fn_payroll_service.repository;

import ng.com.createsoftware.fn_payroll_service.model.PayrollTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollTemplateRepository extends JpaRepository<PayrollTemplate, Long> {
}
