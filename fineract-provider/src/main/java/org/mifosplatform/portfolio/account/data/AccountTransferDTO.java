/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.data;

import java.math.BigDecimal;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.account.domain.AccountTransferDetails;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;

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
        this.toSavingsAccount = toSavingsAccount;
        this.fromSavingsAccount = fromSavingsAccount;
        this.isRegularTransaction = isRegularTransaction;
        this.isExceptionForBalanceCheck = isExceptionForBalanceCheck;
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
