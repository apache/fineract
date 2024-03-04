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
package org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;

public interface LoanRepaymentScheduleTransactionProcessor {

    @Data
    @AllArgsConstructor
    class TransactionCtx {

        private final MonetaryCurrency currency;
        private final List<LoanRepaymentScheduleInstallment> installments;
        private final Set<LoanCharge> charges;
        private final MoneyHolder overpaymentHolder;
        private final ChangedTransactionDetail changedTransactionDetail;

        public TransactionCtx(MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges,
                MoneyHolder overpaymentHolder) {
            this(currency, installments, charges, overpaymentHolder, null);
        }
    }

    String getCode();

    String getName();

    boolean accept(String s);

    /**
     * Provides support for processing the latest transaction (which should be the latest transaction) against the loan
     * schedule.
     */
    void processLatestTransaction(LoanTransaction loanTransaction, TransactionCtx ctx);

    /**
     * Provides support for passing all {@link LoanTransaction}'s so it will completely re-process the entire loan
     * schedule. This is required in cases where the {@link LoanTransaction} being processed is in the past and falls
     * before existing transactions or and adjustment is made to an existing in which case the entire loan schedule
     * needs to be re-processed.
     */
    ChangedTransactionDetail reprocessLoanTransactions(LocalDate disbursementDate, List<LoanTransaction> repaymentsOrWaivers,
            MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Set<LoanCharge> charges);

    Money handleRepaymentSchedule(List<LoanTransaction> transactionsPostDisbursement, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> loanCharges);

    /**
     * Used in interest recalculation to introduce new interest only installment.
     */
    boolean isInterestFirstRepaymentScheduleTransactionProcessor();
}
