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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;

public class ScheduleCurrentPeriodParams {

    Money earlyPaidAmount;
    LoanScheduleModelPeriod lastInstallment;
    boolean skipCurrentLoop;
    Money interestForThisPeriod;
    Money principalForThisPeriod;
    Money feeChargesForInstallment;
    Money penaltyChargesForInstallment;
    // for adjusting outstandingBalances
    Money reducedBalance;
    boolean isEmiAmountChanged;
    BigDecimal interestCalculationGraceOnRepaymentPeriodFraction;

    ScheduleCurrentPeriodParams(final CurrencyData currency) {
        this(currency, BigDecimal.ZERO);
    }

    ScheduleCurrentPeriodParams(final CurrencyData currency, BigDecimal interestCalculationGraceOnRepaymentPeriodFraction) {
        this.earlyPaidAmount = Money.zero(currency);
        this.lastInstallment = null;
        this.skipCurrentLoop = false;
        this.interestForThisPeriod = Money.zero(currency);
        this.principalForThisPeriod = Money.zero(currency);
        this.reducedBalance = Money.zero(currency);
        this.feeChargesForInstallment = Money.zero(currency);
        this.penaltyChargesForInstallment = Money.zero(currency);
        this.isEmiAmountChanged = false;
        this.interestCalculationGraceOnRepaymentPeriodFraction = interestCalculationGraceOnRepaymentPeriodFraction;
    }

    public Money getEarlyPaidAmount() {
        return this.earlyPaidAmount;
    }

    public void plusEarlyPaidAmount(Money earlyPaidAmount) {
        this.earlyPaidAmount = this.earlyPaidAmount.plus(earlyPaidAmount);
    }

    public void minusEarlyPaidAmount(Money earlyPaidAmount) {
        this.earlyPaidAmount = this.earlyPaidAmount.minus(earlyPaidAmount);
    }

    public LoanScheduleModelPeriod getLastInstallment() {
        return this.lastInstallment;
    }

    public void setLastInstallment(LoanScheduleModelPeriod lastInstallment) {
        this.lastInstallment = lastInstallment;
    }

    public boolean isSkipCurrentLoop() {
        return this.skipCurrentLoop;
    }

    public void setSkipCurrentLoop(boolean skipCurrentLoop) {
        this.skipCurrentLoop = skipCurrentLoop;
    }

    public Money getInterestForThisPeriod() {
        return this.interestForThisPeriod;
    }

    public void setInterestForThisPeriod(Money interestForThisPeriod) {
        this.interestForThisPeriod = interestForThisPeriod;
    }

    public void minusInterestForThisPeriod(Money interestForThisPeriod) {
        this.interestForThisPeriod = this.interestForThisPeriod.minus(interestForThisPeriod);
    }

    public Money getPrincipalForThisPeriod() {
        return this.principalForThisPeriod;
    }

    public void setPrincipalForThisPeriod(Money principalForThisPeriod) {
        this.principalForThisPeriod = principalForThisPeriod;
    }

    public void plusPrincipalForThisPeriod(Money principalForThisPeriod) {
        this.principalForThisPeriod = this.principalForThisPeriod.plus(principalForThisPeriod);
    }

    public void minusPrincipalForThisPeriod(Money principalForThisPeriod) {
        this.principalForThisPeriod = this.principalForThisPeriod.minus(principalForThisPeriod);
    }

    public Money getReducedBalance() {
        return this.reducedBalance;
    }

    public void setReducedBalance(Money reducedBalance) {
        this.reducedBalance = reducedBalance;
    }

    public Money getFeeChargesForInstallment() {
        return this.feeChargesForInstallment;
    }

    public void setFeeChargesForInstallment(Money feeChargesForInstallment) {
        this.feeChargesForInstallment = feeChargesForInstallment;
    }

    public void minusFeeChargesForInstallment(Money feeChargesForInstallment) {
        this.feeChargesForInstallment = this.feeChargesForInstallment.minus(feeChargesForInstallment);
    }

    public Money getPenaltyChargesForInstallment() {
        return this.penaltyChargesForInstallment;
    }

    public void setPenaltyChargesForInstallment(Money penaltyChargesForInstallment) {
        this.penaltyChargesForInstallment = penaltyChargesForInstallment;
    }

    public void minusPenaltyChargesForInstallment(Money penaltyChargesForInstallment) {
        this.penaltyChargesForInstallment = this.penaltyChargesForInstallment.minus(penaltyChargesForInstallment);
    }

    public Money fetchTotalAmountForPeriod() {
        return this.principalForThisPeriod.plus(interestForThisPeriod).plus(feeChargesForInstallment).plus(penaltyChargesForInstallment);
    }

    public boolean isEmiAmountChanged() {
        return this.isEmiAmountChanged;
    }

    public void setEmiAmountChanged(boolean isEmiAmountChanged) {
        this.isEmiAmountChanged = isEmiAmountChanged;
    }

    public BigDecimal getInterestCalculationGraceOnRepaymentPeriodFraction() {
        return this.interestCalculationGraceOnRepaymentPeriodFraction;
    }

}
