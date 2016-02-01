/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
