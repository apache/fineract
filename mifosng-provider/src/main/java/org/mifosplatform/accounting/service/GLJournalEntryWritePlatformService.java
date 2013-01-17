package org.mifosplatform.accounting.service;

import org.mifosplatform.accounting.api.commands.GLJournalEntryCommand;
import org.mifosplatform.accounting.api.data.LoanDTO;
import org.springframework.security.access.prepost.PreAuthorize;

public interface GLJournalEntryWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_JOURNAL_ENTRY')")
    String createJournalEntry(GLJournalEntryCommand journalEntryCommand);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'REVERT_JOURNAL_ENTRY')")
    String revertJournalEntry(String transactionId);

    void createJournalEntriesForLoan(LoanDTO loanDTO);
    

}
