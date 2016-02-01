/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.accounting.journalentry.data;

import java.util.List;

public class LoanDTO {

    private Long loanId;
    private Long loanProductId;
    private Long officeId;

    private String currencyCode;
    private boolean cashBasedAccountingEnabled;
    final boolean upfrontAccrualBasedAccountingEnabled;
    final boolean periodicAccrualBasedAccountingEnabled;
    private List<LoanTransactionDTO> newLoanTransactions;

    public LoanDTO(final Long loanId, final Long loanProductId, final Long officeId, final String currencyCode,
            final boolean cashBasedAccountingEnabled, final boolean upfrontAccrualBasedAccountingEnabled,
            final boolean periodicAccrualBasedAccountingEnabled, final List<LoanTransactionDTO> newLoanTransactions) {
        this.loanId = loanId;
        this.loanProductId = loanProductId;
        this.officeId = officeId;
        this.cashBasedAccountingEnabled = cashBasedAccountingEnabled;
        this.newLoanTransactions = newLoanTransactions;
        this.currencyCode = currencyCode;
        this.upfrontAccrualBasedAccountingEnabled = upfrontAccrualBasedAccountingEnabled;
        this.periodicAccrualBasedAccountingEnabled = periodicAccrualBasedAccountingEnabled;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public void setLoanId(final Long loanId) {
        this.loanId = loanId;
    }

    public Long getLoanProductId() {
        return this.loanProductId;
    }

    public void setLoanProductId(final Long loanProductId) {
        this.loanProductId = loanProductId;
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

    public boolean isUpfrontAccrualBasedAccountingEnabled() {
        return this.upfrontAccrualBasedAccountingEnabled;
    }

    public boolean isPeriodicAccrualBasedAccountingEnabled() {
        return this.periodicAccrualBasedAccountingEnabled;
    }

    public List<LoanTransactionDTO> getNewLoanTransactions() {
        return this.newLoanTransactions;
    }

    public void setNewLoanTransactions(final List<LoanTransactionDTO> newLoanTransactions) {
        this.newLoanTransactions = newLoanTransactions;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public void setCurrencyCode(final String currencyCode) {
        this.currencyCode = currencyCode;
    }

}
