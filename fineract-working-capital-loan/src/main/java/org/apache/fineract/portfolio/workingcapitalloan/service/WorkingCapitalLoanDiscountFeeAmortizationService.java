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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.time.LocalDate;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;

public interface WorkingCapitalLoanDiscountFeeAmortizationService {

    void processDiscountFeeAmortization(WorkingCapitalLoan loan, LocalDate transactionDate);

    /**
     * Recognizes the entire unreleased discount-fee deferred income balance as of charge-off in one shot, crediting the
     * charge-off expense account instead of discount-fee income, and links the resulting transaction to
     * {@code chargeOffTransaction} so it can be found and reversed on undo. No-op if there is nothing left to
     * recognize.
     */
    void processFinalDiscountFeeAmortizationOnChargeOff(WorkingCapitalLoan loan, WorkingCapitalLoanTransaction chargeOffTransaction);

    /**
     * Reverses the discount-fee amortization transaction (and its journal entries) created by
     * {@link #processFinalDiscountFeeAmortizationOnChargeOff} for {@code chargeOffTransaction}, if any.
     */
    void undoDiscountFeeAmortizationOnChargeOff(WorkingCapitalLoan loan, WorkingCapitalLoanTransaction chargeOffTransaction);

    /**
     * Recomputes {@code realizedIncomeFromDiscountFee} on the loan balance from the database aggregate of non-reversed
     * amortization transactions. Callers must flush any pending amortization transaction posts or reversals before
     * invoking this method, otherwise the aggregate will not reflect them.
     */
    void recalculateRealizedIncome(WorkingCapitalLoan loan);
}
