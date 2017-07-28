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
package org.apache.fineract.portfolio.account.data;

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

public class AccountTransferDTO {

    private final LocalDate transactionDate;
    private final BigDecimal transactionAmount;
    private final PortfolioAccountType fromAccountType;
    private final PortfolioAccountType toAccountType;
    private final Long fromAccountId;
    private final Long toAccountId;
    private final String description;
    private final Locale locale;
    private final DateTimeFormatter fmt;
    private final PaymentDetail paymentDetail;
    private final Integer fromTransferType;
    private final Integer toTransferType;
    private final Long chargeId;
    private final Integer loanInstallmentNumber;
    private final Integer transferType;
    private final AccountTransferDetails accountTransferDetails;
    private final String noteText;
    private final String txnExternalId;
    private final Loan loan;
    private final Loan fromLoan;
    private final Loan toLoan;
    private final SavingsAccount toSavingsAccount;
    private final SavingsAccount fromSavingsAccount;
    private final Boolean isRegularTransaction;
    private final Boolean isExceptionForBalanceCheck;

    public AccountTransferDTO(final LocalDate transactionDate, final BigDecimal transactionAmount,
            final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType, final Long fromAccountId,
            final Long toAccountId, final String description, final Locale locale, final DateTimeFormatter fmt,
            final PaymentDetail paymentDetail, final Integer fromTransferType, final Integer toTransferType, final Long chargeId,
            Integer loanInstallmentNumber, Integer transferType, final AccountTransferDetails accountTransferDetails,
            final String noteText, final String txnExternalId, final Loan loan, SavingsAccount toSavingsAccount,
            final SavingsAccount fromSavingsAccount, final Boolean isRegularTransaction, Boolean isExceptionForBalanceCheck) {
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.fromAccountType = fromAccountType;
        this.toAccountType = toAccountType;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.description = description;
        this.locale = locale;
        this.fmt = fmt;
        this.paymentDetail = paymentDetail;
        this.fromTransferType = fromTransferType;
        this.toTransferType = toTransferType;
        this.chargeId = chargeId;
        this.loanInstallmentNumber = loanInstallmentNumber;
        this.transferType = transferType;
        this.accountTransferDetails = accountTransferDetails;
        this.noteText = noteText;
        this.txnExternalId = txnExternalId;
        this.loan = loan;
        this.fromLoan = null;
        this.toLoan = null;
        this.toSavingsAccount = toSavingsAccount;
        this.fromSavingsAccount = fromSavingsAccount;
        this.isRegularTransaction = isRegularTransaction;
        this.isExceptionForBalanceCheck = isExceptionForBalanceCheck;
    }

    public AccountTransferDTO(final LocalDate transactionDate, final BigDecimal transactionAmount,
            final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType, final Long fromAccountId,
            final Long toAccountId, final String description, final Locale locale, final DateTimeFormatter fmt,
            final Integer fromTransferType, final Integer toTransferType, final String txnExternalId,
            final Loan fromLoan, final Loan toLoan) {
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.fromAccountType = fromAccountType;
        this.toAccountType = toAccountType;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.description = description;
        this.locale = locale;
        this.fmt = fmt;
        this.paymentDetail = null;
        this.fromTransferType = fromTransferType;
        this.toTransferType = toTransferType;
        this.chargeId = null;
        this.loanInstallmentNumber = null;
        this.transferType = null;
        this.accountTransferDetails = null;
        this.noteText = null;
        this.txnExternalId = txnExternalId;
        this.fromLoan = fromLoan;
        this.toLoan = toLoan;
        this.loan = null;
        this.toSavingsAccount = null;
        this.fromSavingsAccount = null;
        this.isRegularTransaction = null;
        this.isExceptionForBalanceCheck = null;
    }

    public LocalDate getTransactionDate() {
        return this.transactionDate;
    }

    public BigDecimal getTransactionAmount() {
        return this.transactionAmount;
    }

    public PortfolioAccountType getFromAccountType() {
        return this.fromAccountType;
    }

    public PortfolioAccountType getToAccountType() {
        return this.toAccountType;
    }

    public Long getFromAccountId() {
        return this.fromAccountId;
    }

    public Long getToAccountId() {
        return this.toAccountId;
    }

    public String getDescription() {
        return this.description;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public DateTimeFormatter getFmt() {
        return this.fmt;
    }

    public PaymentDetail getPaymentDetail() {
        return this.paymentDetail;
    }

    public Integer getFromTransferType() {
        return this.fromTransferType;
    }

    public Integer getToTransferType() {
        return this.toTransferType;
    }

    public Long getChargeId() {
        return this.chargeId;
    }

    public Integer getLoanInstallmentNumber() {
        return this.loanInstallmentNumber;
    }

    public Integer getTransferType() {
        return this.transferType;
    }

    public AccountTransferDetails getAccountTransferDetails() {
        return this.accountTransferDetails;
    }

    public String getNoteText() {
        return this.noteText;
    }

    public String getTxnExternalId() {
        return this.txnExternalId;
    }

    public Loan getLoan() {
        return this.loan;
    }

    public Loan getFromLoan() {
        return this.fromLoan;
    }

    public Loan getToLoan() {
        return this.toLoan;
    }

    public SavingsAccount getToSavingsAccount() {
        return this.toSavingsAccount;
    }

    public SavingsAccount getFromSavingsAccount() {
        return this.fromSavingsAccount;
    }

    public Boolean isRegularTransaction() {
        return this.isRegularTransaction;
    }

    public Boolean isExceptionForBalanceCheck() {
        return this.isExceptionForBalanceCheck;
    }

}
