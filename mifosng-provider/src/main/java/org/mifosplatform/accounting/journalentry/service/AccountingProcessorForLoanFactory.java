/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.service;

import org.mifosplatform.accounting.journalentry.data.LoanDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AccountingProcessorForLoanFactory {

    private final ApplicationContext applicationContext;

    @Autowired
    public AccountingProcessorForLoanFactory(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public AccountingProcessorForLoan determineProcessor(final LoanDTO loanDTO) {

        AccountingProcessorForLoan accountingProcessorForLoan = null;

        if (loanDTO.isCashBasedAccountingEnabled()) {
            accountingProcessorForLoan = this.applicationContext.getBean("cashBasedAccountingProcessorForLoan",
                    AccountingProcessorForLoan.class);
        } else if (loanDTO.isUpfrontAccrualBasedAccountingEnabled()) {
            accountingProcessorForLoan = this.applicationContext.getBean("accrualBasedAccountingProcessorForLoan",
                    AccountingProcessorForLoan.class);
        } else if (loanDTO.isPeriodicAccrualBasedAccountingEnabled()) {
            accountingProcessorForLoan = this.applicationContext.getBean("accrualBasedAccountingProcessorForLoan",
                    AccountingProcessorForLoan.class);
        }

        return accountingProcessorForLoan;
    }

}
