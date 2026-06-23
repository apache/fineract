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

import java.time.format.DateTimeFormatter;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;

/**
 * Orchestrates the activation-time processing of a {@link SavingsAccount}: rolling charge due dates forward and paying
 * the activation charges (which may in turn trigger interest posting). This logic was previously embedded in the entity
 * ({@code SavingsAccount.processAccountUponActivation} / {@code payActivationCharges}); it has been extracted so the
 * domain entity stays free of process orchestration and service collaborators.
 */
public interface SavingsAccountActivationService {

    void processAccountUponActivation(SavingsAccount account, boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            Integer financialYearBeginningMonth);

    /**
     * Recurring-deposit specific activation step: posts the mandatory minimum opening balance as a deposit and
     * recalculates the running balances. Extracted from {@code RecurringDepositAccount.processAccountUponActivation}.
     */
    void processRecurringDepositActivation(RecurringDepositAccount account, DateTimeFormatter fmt, boolean postReversals,
            Long relaxingDaysConfigForPivotDate);

}
