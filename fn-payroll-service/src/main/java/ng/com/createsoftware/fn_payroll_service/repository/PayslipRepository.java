package ng.com.createsoftware.fn_payroll_service.repository;

import ng.com.createsoftware.fn_payroll_service.model.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {
}
