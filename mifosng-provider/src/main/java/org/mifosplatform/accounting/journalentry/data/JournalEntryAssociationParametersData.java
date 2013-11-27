package org.mifosplatform.accounting.journalentry.data;


public class JournalEntryAssociationParametersData {

    private final boolean transactionDetailsRequired;
    private final boolean runningBalanceRequired;
    
    
    public JournalEntryAssociationParametersData() {
        this.transactionDetailsRequired = false;
        this.runningBalanceRequired = false;
    }
    
    public JournalEntryAssociationParametersData(final boolean transactionDetailsRequired , final boolean runningBalanceRequired) {
        this.transactionDetailsRequired = transactionDetailsRequired;
        this.runningBalanceRequired = runningBalanceRequired;
    }

    
    public boolean isTransactionDetailsRequired() {
        return this.transactionDetailsRequired;
    }

    
    public boolean isRunningBalanceRequired() {
        return this.runningBalanceRequired;
    }
}
