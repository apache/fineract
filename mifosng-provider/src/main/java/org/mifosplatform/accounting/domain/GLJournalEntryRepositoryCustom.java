package org.mifosplatform.accounting.domain;

import java.util.List;

public interface GLJournalEntryRepositoryCustom {

    List<GLJournalEntry> findFirstJournalEntryForAccount(long glAccountId);
}
