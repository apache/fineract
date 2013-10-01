package org.mifosplatform.accounting.journalentry.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface JournalEntryRunningBalanceUpdateService {

    void updateRunningBalance();

    CommandProcessingResult updateOfficeRunningBalance(JsonCommand command);

}
