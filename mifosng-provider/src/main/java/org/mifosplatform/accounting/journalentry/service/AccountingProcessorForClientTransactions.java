package org.mifosplatform.accounting.journalentry.service;

import org.mifosplatform.accounting.journalentry.data.ClientTransactionDTO;

public interface AccountingProcessorForClientTransactions {

    void createJournalEntriesForClientTransaction(ClientTransactionDTO clientTransactionDTO);
}
