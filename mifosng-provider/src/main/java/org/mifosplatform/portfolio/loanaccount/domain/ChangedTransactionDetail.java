package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.ArrayList;
import java.util.List;

public class ChangedTransactionDetail {

    private List<LoanTransaction> newTransactions = new ArrayList<LoanTransaction>();

    private List<LoanTransaction> reversedTransactions = new ArrayList<LoanTransaction>();

    public List<LoanTransaction> getNewTransactions() {
        return this.newTransactions;
    }

    public void setNewTransactions(List<LoanTransaction> newTransactions) {
        this.newTransactions = newTransactions;
    }

    public List<LoanTransaction> getReversedTransactions() {
        return this.reversedTransactions;
    }

    public void setReversedTransactions(List<LoanTransaction> reversedTransactions) {
        this.reversedTransactions = reversedTransactions;
    }

}
