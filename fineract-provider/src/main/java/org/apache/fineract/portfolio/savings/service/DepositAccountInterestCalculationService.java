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
import java.math.MathContext;
import java.time.LocalDate;
import org.apache.fineract.portfolio.savings.domain.FixedDepositAccount;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositAccount;

/**
 * Calculates maturity and interest of fixed and recurring deposit accounts. This calculation was previously spread over
 * methods of the {@link FixedDepositAccount} and {@link RecurringDepositAccount} entities; it has been extracted so the
 * domain entities no longer call calculation helpers and only store the results handed to them.
 */
public interface DepositAccountInterestCalculationService {

    /**
     * Calculates and stores the maturity date and amount of a fixed deposit that is not active yet, projecting the
     * deposit amount as a single deposit on the submitted (or activation) date.
     */
    void updateMaturityDateAndAmountBeforeAccountActivation(FixedDepositAccount account, MathContext mc, boolean isPreMatureClosure,
            boolean isSavingsInterestPostingAtCurrentPeriodEnd, Integer financialYearBeginningMonth);

    /**
     * Calculates and stores the maturity date and amount of a fixed deposit from its existing transactions.
     */
    void updateMaturityDateAndAmount(FixedDepositAccount account, MathContext mc, boolean isPreMatureClosure,
            boolean isSavingsInterestPostingAtCurrentPeriodEnd, Integer financialYearBeginningMonth);

    /**
     * Calculates and stores the maturity date and amount of a recurring deposit from its existing transactions and,
     * when it has a maturity date, its pending schedule installments.
     */
    void updateMaturityDateAndAmount(RecurringDepositAccount account, MathContext mc, boolean isPreMatureClosure,
            boolean isSavingsInterestPostingAtCurrentPeriodEnd, Integer financialYearBeginningMonth);

    /**
     * Marks a fixed deposit as matured when due and, if so, posts its maturity interest.
     */
    void updateMaturityStatus(FixedDepositAccount account, boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            Integer financialYearBeginningMonth);

    /**
     * Marks a recurring deposit as matured when due and, if so, posts its maturity interest as of the business date.
     */
    void updateMaturityStatus(RecurringDepositAccount account, boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            Integer financialYearBeginningMonth, boolean postReversals);

    /**
     * Calculates and posts the interest of a fixed deposit up to its maturity date.
     */
    void postMaturityInterest(FixedDepositAccount account, boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            Integer financialYearBeginningMonth);

    /**
     * Sets the closed-on date of a recurring deposit, then calculates and posts its interest up to the maturity date
     * or, for an open-ended deposit, up to {@code closeDate}.
     */
    void postMaturityInterest(RecurringDepositAccount account, boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            Integer financialYearBeginningMonth, LocalDate closeDate, boolean postReversals);

    /**
     * Calculates the interest of a fixed deposit up to the day before {@code accountCloseDate} and posts the part not
     * yet posted.
     */
    void postPreMaturityInterest(FixedDepositAccount account, LocalDate accountCloseDate, boolean isPreMatureClosure,
            boolean isSavingsInterestPostingAtCurrentPeriodEnd, Integer financialYearBeginningMonth);

    /**
     * Calculates the interest of a recurring deposit up to the day before {@code accountCloseDate} and posts the part
     * not yet posted.
     */
    void postPreMaturityInterest(RecurringDepositAccount account, LocalDate accountCloseDate, boolean isPreMatureClosure,
            boolean isSavingsInterestPostingAtCurrentPeriodEnd, Integer financialYearBeginningMonth, boolean postReversals);

    /**
     * Calculates the amount a fixed deposit would pay out if closed prematurely on {@code preMatureDate}.
     */
    BigDecimal calculatePreMatureAmount(FixedDepositAccount account, LocalDate preMatureDate, boolean isPreMatureClosure,
            boolean isSavingsInterestPostingAtCurrentPeriodEnd, Integer financialYearBeginningMonth);

    /**
     * Calculates the amount a recurring deposit would pay out if closed prematurely on {@code preMatureDate}.
     */
    BigDecimal calculatePreMatureAmount(RecurringDepositAccount account, LocalDate preMatureDate, boolean isPreMatureClosure,
            boolean isSavingsInterestPostingAtCurrentPeriodEnd, Integer financialYearBeginningMonth);

}
