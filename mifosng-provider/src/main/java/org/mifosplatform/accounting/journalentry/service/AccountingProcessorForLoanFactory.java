package org.mifosplatform.accounting.journalentry.service;

import org.mifosplatform.accounting.journalentry.data.LoanDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AccountingProcessorForLoanFactory {

    private ApplicationContext applicationContext;

    @Autowired
    public AccountingProcessorForLoanFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public AccountingProcessorForLoan determineProcessor(final LoanDTO loanDTO) {

        AccountingProcessorForLoan accountingProcessorForLoan = null;

        if (loanDTO.isCashBasedAccountingEnabled()) {
            accountingProcessorForLoan = applicationContext
                    .getBean("cashBasedAccountingProcessorForLoan", AccountingProcessorForLoan.class);
        }

        else if (loanDTO.isAccrualBasedAccountingEnabled()) {
            accountingProcessorForLoan = applicationContext.getBean("accrualBasedAccountingProcessorForLoan",
                    AccountingProcessorForLoan.class);
        }

        return accountingProcessorForLoan;
    }

}
