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
import java.time.LocalDate;
import java.util.Set;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalcualtionAdditionalDetails;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;

/**
 * Domain representation of a Loan Schedule Disbursement Period (not used for persistence)
 */
public final class LoanScheduleModelDisbursementPeriod implements LoanScheduleModelPeriod {

    @SuppressWarnings("unused")
    private final Integer periodNumber;
    private final LocalDate disbursementDate;
    private final Money principalDisbursed;
    private final BigDecimal chargesDueAtTimeOfDisbursement;
    private boolean isEMIFixedSpecificToInstallment = false;

    public static LoanScheduleModelDisbursementPeriod disbursement(final LoanApplicationTerms loanApplicationTerms,
            final BigDecimal chargesDueAtTimeOfDisbursement) {

        final int periodNumber = 0;
        return new LoanScheduleModelDisbursementPeriod(periodNumber, loanApplicationTerms.getExpectedDisbursementDate(),
                loanApplicationTerms.getPrincipal(), chargesDueAtTimeOfDisbursement);
    }

    public static LoanScheduleModelDisbursementPeriod disbursement(final LocalDate disbursementDate, final Money principalDisbursed,
            final BigDecimal chargesDueAtTimeOfDisbursement) {
        return new LoanScheduleModelDisbursementPeriod(null, disbursementDate, principalDisbursed, chargesDueAtTimeOfDisbursement);
    }

    private LoanScheduleModelDisbursementPeriod(final Integer periodNumber, final LocalDate disbursementDate,
            final Money principalDisbursed, final BigDecimal chargesDueAtTimeOfDisbursement) {
        this.periodNumber = periodNumber;
        this.disbursementDate = disbursementDate;
        this.principalDisbursed = principalDisbursed;
        this.chargesDueAtTimeOfDisbursement = chargesDueAtTimeOfDisbursement;
    }

    @Override
    public LoanSchedulePeriodData toData() {
        return LoanSchedulePeriodData.disbursementOnlyPeriod(this.disbursementDate, this.principalDisbursed.getAmount(),
                this.chargesDueAtTimeOfDisbursement, false);
    }

    @Override
    public boolean isRepaymentPeriod() {
        return false;
    }

    @Override
    public boolean isDownPaymentPeriod() {
        return false;
    }

    @Override
    public Integer periodNumber() {
        return null;
    }

    @Override
    public LocalDate periodFromDate() {
        return null;
    }

    @Override
    public LocalDate periodDueDate() {
        return null;
    }

    @Override
    public BigDecimal principalDue() {
        return null;
    }

    @Override
    public BigDecimal interestDue() {
        return null;
    }

    @Override
    public BigDecimal feeChargesDue() {
        return null;
    }

    @Override
    public BigDecimal penaltyChargesDue() {
        return null;
    }

    @Override
    public void addLoanCharges(@SuppressWarnings("unused") BigDecimal feeCharge, @SuppressWarnings("unused") BigDecimal penaltyCharge) {
        return;
    }

    @Override
    public boolean isRecalculatedInterestComponent() {
        return false;
    }

    @Override
    public void addPrincipalAmount(@SuppressWarnings("unused") Money principalDue) {
        return;
    }

    @Override
    public void addInterestAmount(@SuppressWarnings("unused") Money principalDue) {
        return;
    }

    @Override
    public Set<LoanInterestRecalcualtionAdditionalDetails> getLoanCompoundingDetails() {
        return null;
    }

    @Override
    public void setEMIFixedSpecificToInstallmentTrue() {
        this.isEMIFixedSpecificToInstallment = true;
    }

    @Override
    public boolean isEMIFixedSpecificToInstallment() {
        return isEMIFixedSpecificToInstallment;
    }

    @Override
    public BigDecimal rescheduleInterestPortion() {
        return null;
    }

    @Override
    public void setRescheduleInterestPortion(BigDecimal rescheduleInterestPortion) {
        return;
    }
}
