package ng.com.createsoftware.fn_payroll_service.event;

import org.springframework.stereotype.Service;

@Service
public class AccountingPublisher {
    public void publishPayrollExpense(Long payrollRunId){
        System.out.println("Payroll expense posted: " + payrollRunId);
    }
}
