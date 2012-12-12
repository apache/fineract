package org.mifosplatform.accounting.service;

import org.mifosplatform.accounting.api.commands.GLJournalEntryCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface GLJournalEntryWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'CREATE_JOURNAL_ENTRY')")
    String createJournalEntry(GLJournalEntryCommand journalEntryCommand);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'REVERT_JOURNAL_ENTRY')")
    String revertJournalEntry(String transactionId);

}
