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
package org.apache.fineract.portfolio.account.domain;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.junit.jupiter.api.Test;

class AccountTransferTransactionTest {

    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2026, 3, 1);

    @Test
    void transferFactoriesAcceptPaymentDetailsForEveryDirection() {
        PaymentDetail paymentDetail = mock(PaymentDetail.class);
        AccountTransferDetails transferDetails = mock(AccountTransferDetails.class);
        SavingsAccountTransaction savingsTransaction = mock(SavingsAccountTransaction.class);
        LoanTransaction loanTransaction = mock(LoanTransaction.class);
        Money transactionAmount = transactionAmount();

        assertSame(paymentDetail, AccountTransferTransaction.savingsToSavingsTransfer(transferDetails, savingsTransaction,
                savingsTransaction, TRANSACTION_DATE, transactionAmount, "savings to savings", paymentDetail).getPaymentDetail());
        assertSame(paymentDetail, AccountTransferTransaction.savingsToLoanTransfer(transferDetails, savingsTransaction, loanTransaction,
                TRANSACTION_DATE, transactionAmount, "savings to loan", paymentDetail).getPaymentDetail());
        assertSame(paymentDetail, AccountTransferTransaction.loanTosavingsTransfer(transferDetails, savingsTransaction, loanTransaction,
                TRANSACTION_DATE, transactionAmount, "loan to savings", paymentDetail).getPaymentDetail());
        assertSame(paymentDetail, AccountTransferTransaction.loanToLoanTransfer(transferDetails, loanTransaction, loanTransaction,
                TRANSACTION_DATE, transactionAmount, "loan to loan", paymentDetail).getPaymentDetail());
    }

    @Test
    void existingTransferFactoriesRemainPaymentDetailOptional() {
        AccountTransferDetails transferDetails = mock(AccountTransferDetails.class);
        SavingsAccountTransaction savingsTransaction = mock(SavingsAccountTransaction.class);
        LoanTransaction loanTransaction = mock(LoanTransaction.class);
        Money transactionAmount = transactionAmount();

        assertNull(AccountTransferTransaction.savingsToSavingsTransfer(transferDetails, savingsTransaction, savingsTransaction,
                TRANSACTION_DATE, transactionAmount, "savings to savings").getPaymentDetail());
        assertNull(AccountTransferTransaction.savingsToLoanTransfer(transferDetails, savingsTransaction, loanTransaction, TRANSACTION_DATE,
                transactionAmount, "savings to loan").getPaymentDetail());
        assertNull(AccountTransferTransaction.loanTosavingsTransfer(transferDetails, savingsTransaction, loanTransaction, TRANSACTION_DATE,
                transactionAmount, "loan to savings").getPaymentDetail());
        assertNull(AccountTransferTransaction
                .loanToLoanTransfer(transferDetails, loanTransaction, loanTransaction, TRANSACTION_DATE, transactionAmount, "loan to loan")
                .getPaymentDetail());
    }

    private Money transactionAmount() {
        Money transactionAmount = mock(Money.class);
        when(transactionAmount.getCurrency()).thenReturn(new MonetaryCurrency("USD", 2, null));
        when(transactionAmount.getAmountDefaultedToNullIfZero()).thenReturn(BigDecimal.TEN);
        return transactionAmount;
    }
}
