package org.mifosplatform.accounting.journalentry.service;

import org.mifosplatform.accounting.journalentry.data.LoanDTO;

public interface AccountingProcessorForLoan {

    void createJournalEntriesForLoan(LoanDTO loanDTO);

}
