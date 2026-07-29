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

import java.math.MathContext;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;

/**
 * Calculates the interest of a {@link SavingsAccount} entity. This calculation was previously a method on the entity
 * itself ({@code SavingsAccount.calculateInterestUsing}); it has been extracted so the domain entity no longer calls
 * calculation helpers and only stores the results handed to it.
 */
public interface SavingsAccountInterestCalculationService {

    /**
     * Recalculates the daily balances of the account and calculates the interest earned per posting period up to
     * {@code upToInterestCalculationDate}. The effective up-to date is resolved polymorphically via
     * {@link SavingsAccount#interestCalculationUpToDate(LocalDate)} (identity for regular savings, maturity-capped for
     * fixed and recurring deposits). The interest totals and the transaction summary of the account are updated with
     * the result.
     */
    List<PostingPeriod> calculateInterestUsing(SavingsAccount account, MathContext mc, LocalDate upToInterestCalculationDate,
            boolean isInterestTransfer, boolean isSavingsInterestPostingAtCurrentPeriodEnd, Integer financialYearBeginningMonth,
            LocalDate postInterestOnDate, boolean backdatedTxnsAllowedTill, boolean postReversals);

}
