package org.mifosplatform.accounting.service;

import java.util.List;

import org.mifosplatform.accounting.api.commands.GLJournalEntryCommand;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.springframework.security.access.prepost.PreAuthorize;

public interface GLJournalEntryWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_JOURNAL_ENTRY')")
    String createJournalEntry(GLJournalEntryCommand journalEntryCommand);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'REVERT_JOURNAL_ENTRY')")
    String revertJournalEntry(String transactionId);

    void createJournalEntriesForLoan(Loan loan, List<LoanTransaction> loanTransactions);
    
    void createJournalEntriesForLoan(Loan loan, LoanTransaction loanTransaction);

}
