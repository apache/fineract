/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.savings.SavingsTransactionBooleanValues;

public interface SavingsAccountDomainService {

    SavingsAccountTransaction handleWithdrawal(SavingsAccount account, DateTimeFormatter fmt, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail, SavingsTransactionBooleanValues transactionBooleanValues);

    SavingsAccountTransaction handleDeposit(SavingsAccount account, DateTimeFormatter fmt, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail, boolean isAccountTransfer, boolean isRegularTransaction);

    void postJournalEntries(SavingsAccount savingsAccount, Set<Long> existingTransactionIds, Set<Long> existingReversedTransactionIds);
}