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
package org.apache.fineract.portfolio.savings.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;

public interface SavingsAccountDomainService {

    SavingsAccountTransaction handleWithdrawal(SavingsAccount account, DateTimeFormatter fmt, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail, SavingsTransactionBooleanValues transactionBooleanValues,
            boolean backdatedTxnsAllowedTill);

    SavingsAccountTransaction handleDeposit(SavingsAccount account, DateTimeFormatter fmt, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail, boolean isAccountTransfer, boolean isRegularTransaction,
            boolean backdatedTxnsAllowedTill);

    void postJournalEntries(SavingsAccount savingsAccount, Set<Long> existingTransactionIds, Set<Long> existingReversedTransactionIds,
            boolean backdatedTxnsAllowedTill);

    SavingsAccountTransaction handleDividendPayout(SavingsAccount account, LocalDate transactionDate, BigDecimal transactionAmount,
            boolean backdatedTxnsAllowedTill);

    SavingsAccountTransaction handleReversal(SavingsAccount account, List<SavingsAccountTransaction> savingsAccountTransactions,
            boolean backdatedTxnsAllowedTill);

    SavingsAccountTransaction handleHold(SavingsAccount account, BigDecimal amount, LocalDate transactionDate, Boolean lienAllowed);
}
