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
package org.apache.fineract.portfolio.loanaccount.loanschedule.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalcualtionAdditionalDetails;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelPeriod;

public final class LoanScheduleModelDownPaymentPeriod implements LoanScheduleModelPeriod {

    private final int periodNumber;
    private final LocalDate periodDate;
    private Money principalDue;
    private final Money outstandingLoanBalance;

    public static LoanScheduleModelDownPaymentPeriod downPayment(final int periodNumber, final LocalDate periodDate,
            final Money principalDue, final Money outstandingLoanBalance) {

        return new LoanScheduleModelDownPaymentPeriod(periodNumber, periodDate, principalDue, outstandingLoanBalance);
    }

    private LoanScheduleModelDownPaymentPeriod(int periodNumber, LocalDate periodDate, Money principalDue, Money outstandingLoanBalance) {
        this.periodNumber = periodNumber;
        this.periodDate = periodDate;
        this.principalDue = principalDue;
        this.outstandingLoanBalance = outstandingLoanBalance;
    }

    @Override
    public LoanSchedulePeriodData toData() {
        return LoanSchedulePeriodData.downPaymentOnlyPeriod(this.periodNumber, this.periodDate, this.principalDue.getAmount(),
                this.outstandingLoanBalance.getAmount());
    }

    @Override
    public boolean isRepaymentPeriod() {
        return false;
    }

    @Override
    public boolean isDownPaymentPeriod() {
        return true;
    }

    @Override
    public Integer periodNumber() {
        return this.periodNumber;
    }

    @Override
    public LocalDate periodFromDate() {
        return this.periodDate;
    }

    @Override
    public LocalDate periodDueDate() {
        return this.periodDate;
    }

    @Override
    public BigDecimal principalDue() {
        BigDecimal value = BigDecimal.ZERO;
        if (this.principalDue != null) {
            value = this.principalDue.getAmount();
        }

        return value;
    }

    @Override
    public BigDecimal interestDue() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal feeChargesDue() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal penaltyChargesDue() {
        return BigDecimal.ZERO;
    }

    @Override
    public void addLoanCharges(BigDecimal feeCharge, BigDecimal penaltyCharge) {

    }

    @Override
    public boolean isRecalculatedInterestComponent() {
        return false;
    }

    @Override
    public void addPrincipalAmount(Money principalDue) {

    }

    @Override
    public void addInterestAmount(Money interestDue) {

    }

    @Override
    public Set<LoanInterestRecalcualtionAdditionalDetails> getLoanCompoundingDetails() {
        return null;
    }

    @Override
    public void setEMIFixedSpecificToInstallmentTrue() {

    }

    @Override
    public boolean isEMIFixedSpecificToInstallment() {
        return false;
    }

    @Override
    public BigDecimal rescheduleInterestPortion() {
        return null;
    }

    @Override
    public void setRescheduleInterestPortion(BigDecimal rescheduleInterestPortion) {

    }
}
