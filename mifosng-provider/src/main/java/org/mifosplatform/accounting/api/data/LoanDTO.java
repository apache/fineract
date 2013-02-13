/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.api.data;

import java.util.List;

public class LoanDTO {

    private Long loanId;
    private Long loanProductId;
    private Long officeId;
    private boolean cashBasedAccountingEnabled;
    private boolean accrualBasedAccountingEnabled;
    private List<LoanTransactionDTO> newLoanTransactions;

    public LoanDTO(Long loanId, Long loanProductId, Long officeId, boolean cashBasedAccountingEnabled,
            boolean accrualBasedAccountingEnabled, List<LoanTransactionDTO> newLoanTransactions) {
        this.loanId = loanId;
        this.loanProductId = loanProductId;
        this.officeId = officeId;
        this.cashBasedAccountingEnabled = cashBasedAccountingEnabled;
        this.accrualBasedAccountingEnabled = accrualBasedAccountingEnabled;
        this.newLoanTransactions = newLoanTransactions;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public Long getLoanProductId() {
        return this.loanProductId;
    }

    public void setLoanProductId(Long loanProductId) {
        this.loanProductId = loanProductId;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }

    public boolean isCashBasedAccountingEnabled() {
        return this.cashBasedAccountingEnabled;
    }

    public void setCashBasedAccountingEnabled(boolean cashBasedAccountingEnabled) {
        this.cashBasedAccountingEnabled = cashBasedAccountingEnabled;
    }

    public boolean isAccrualBasedAccountingEnabled() {
        return this.accrualBasedAccountingEnabled;
    }

    public void setAccrualBasedAccountingEnabled(boolean accrualBasedAccountingEnabled) {
        this.accrualBasedAccountingEnabled = accrualBasedAccountingEnabled;
    }

    public List<LoanTransactionDTO> getNewLoanTransactions() {
        return this.newLoanTransactions;
    }

    public void setNewLoanTransactions(List<LoanTransactionDTO> newLoanTransactions) {
        this.newLoanTransactions = newLoanTransactions;
    }

}
