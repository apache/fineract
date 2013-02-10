package org.mifosplatform.accounting.service;

import java.util.Date;
import java.util.List;

import org.mifosplatform.accounting.api.data.GLJournalEntryData;

public interface GLJournalEntryReadPlatformService {

    List<GLJournalEntryData> retrieveAllGLJournalEntries(Long officeId, Long glAccountId, Boolean portfolioGenerated, Date fromDate,
            Date toDate);

    List<GLJournalEntryData> retrieveRelatedJournalEntries(String transactionId);

    GLJournalEntryData retrieveGLJournalEntryById(long glJournalEntryId);

}
