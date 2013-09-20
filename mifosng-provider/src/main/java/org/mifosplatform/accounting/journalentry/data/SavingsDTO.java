/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.data;

import java.util.List;

public class SavingsDTO {

    private Long savingsId;
    private Long savingsProductId;
    private Long officeId;
    private String currencyCode;
    private boolean cashBasedAccountingEnabled;
    private boolean accrualBasedAccountingEnabled;
    private List<SavingsTransactionDTO> newSavingsTransactions;

    public SavingsDTO(final Long savingsId, final Long savingsProductId, final Long officeId, final String currencyCode,
            final boolean cashBasedAccountingEnabled, final boolean accrualBasedAccountingEnabled,
            final List<SavingsTransactionDTO> newSavingsTransactions) {
        this.savingsId = savingsId;
        this.savingsProductId = savingsProductId;
        this.officeId = officeId;
        this.cashBasedAccountingEnabled = cashBasedAccountingEnabled;
        this.accrualBasedAccountingEnabled = accrualBasedAccountingEnabled;
        this.newSavingsTransactions = newSavingsTransactions;
        this.currencyCode = currencyCode;
    }

    public Long getSavingsId() {
        return this.savingsId;
    }

    public void setSavingsId(final Long savingsId) {
        this.savingsId = savingsId;
    }

    public Long getSavingsProductId() {
        return this.savingsProductId;
    }

    public void setSavingsProductId(final Long savingsProductId) {
        this.savingsProductId = savingsProductId;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public void setOfficeId(final Long officeId) {
        this.officeId = officeId;
    }

    public boolean isCashBasedAccountingEnabled() {
        return this.cashBasedAccountingEnabled;
    }

    public void setCashBasedAccountingEnabled(final boolean cashBasedAccountingEnabled) {
        this.cashBasedAccountingEnabled = cashBasedAccountingEnabled;
    }

    public boolean isAccrualBasedAccountingEnabled() {
        return this.accrualBasedAccountingEnabled;
    }

    public void setAccrualBasedAccountingEnabled(final boolean accrualBasedAccountingEnabled) {
        this.accrualBasedAccountingEnabled = accrualBasedAccountingEnabled;
    }

    public List<SavingsTransactionDTO> getNewSavingsTransactions() {
        return this.newSavingsTransactions;
    }

    public void setNewSavingsTransactions(final List<SavingsTransactionDTO> newSavingsTransactions) {
        this.newSavingsTransactions = newSavingsTransactions;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public void setCurrencyCode(final String currencyCode) {
        this.currencyCode = currencyCode;
    }

}
