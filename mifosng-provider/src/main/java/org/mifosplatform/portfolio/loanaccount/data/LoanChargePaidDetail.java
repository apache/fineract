package org.mifosplatform.portfolio.loanaccount.data;

import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;


public class LoanChargePaidDetail {

    private final Money amount;
    private final LoanRepaymentScheduleInstallment installment;
    private final boolean isFeeCharge;
    
    public LoanChargePaidDetail( Money amount,LoanRepaymentScheduleInstallment installment,boolean isFeeCharge){
        this.amount = amount;
        this.installment = installment;
        this.isFeeCharge = isFeeCharge;
    }

    
    public Money getAmount() {
        return this.amount;
    }

    
    public LoanRepaymentScheduleInstallment getInstallment() {
        return this.installment;
    }

    
    public boolean isFeeCharge() {
        return this.isFeeCharge;
    }
}
