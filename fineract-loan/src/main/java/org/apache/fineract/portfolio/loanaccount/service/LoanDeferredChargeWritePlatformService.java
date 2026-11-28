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
package org.apache.fineract.portfolio.loanaccount.service;

import java.time.LocalDate;
import java.util.Optional;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;

/**
 * Creates {@code DEFERRED_CHARGE} transactions and deferred charge balances when an amortized loan charge becomes
 * effective.
 */
public interface LoanDeferredChargeWritePlatformService {

    /**
     * Creates a deferred charge transaction and balance for {@code loanCharge} when it is amortized and already
     * effective. No-op when the charge is not amortized, waived, a future-dated specified-due-date charge, or an open
     * balance already exists.
     *
     * @param transactionDate
     *            date of the DEFERRED_CHARGE transaction (disbursement date or charge due date)
     */
    void createForEffectiveCharge(Loan loan, LoanCharge loanCharge, LocalDate transactionDate);

    /**
     * Removes all deferred charge balances for the loan (e.g. after undo disbursement returns the loan to approved).
     */
    void resetBalance(Long loanId);

    /**
     * Reverses the {@code DEFERRED_CHARGE} transaction of the open deferred charge balance of the given loan charge and
     * soft-deletes the balance (e.g. a tranche charge deactivated on undo last disbursal).
     *
     * @return the reversed transaction, so the caller can post its journal entries; empty when the charge has no open
     *         balance
     */
    Optional<LoanTransaction> reverseForLoanCharge(Long loanChargeId);
}
