package ng.com.createsoftware.fn_payroll_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_payroll_service.event.AccountingPublisher;
import ng.com.createsoftware.fn_payroll_service.model.PayrollRun;
import ng.com.createsoftware.fn_payroll_service.model.Status;
import ng.com.createsoftware.fn_payroll_service.repository.PayrollRunRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollService {
    private final PayrollRunRepository payrollRunRepository;
    private final AccountingPublisher accountingPublisher;

    public PayrollRun runPayroll(){
        PayrollRun run = new PayrollRun();
        run.setRunDate(LocalDate.now());
        run.setStatus(Status.CREATED);

        PayrollRun saved = payrollRunRepository.save(run);
        accountingPublisher.publishPayrollExpense(saved.getId());

        saved.setStatus(Status.POSTED);
        return payrollRunRepository.save(saved);
    }
}
