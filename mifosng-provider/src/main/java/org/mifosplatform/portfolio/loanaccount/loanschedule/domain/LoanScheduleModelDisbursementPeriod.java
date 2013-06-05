/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;

/**
 * Domain representation of a Loan Schedule Disbursement Period (not used for
 * persistence)
 */
public final class LoanScheduleModelDisbursementPeriod implements LoanScheduleModelPeriod {

    @SuppressWarnings("unused")
    private final int periodNumber;
    private final LocalDate disbursementDate;
    private final Money principalDisbursed;
    private final BigDecimal chargesDueAtTimeOfDisbursement;

    public static LoanScheduleModelDisbursementPeriod disbursement(final LoanApplicationTerms loanApplicationTerms,
            final BigDecimal chargesDueAtTimeOfDisbursement) {

        final int periodNumber = 0;
        return new LoanScheduleModelDisbursementPeriod(periodNumber, loanApplicationTerms, chargesDueAtTimeOfDisbursement);
    }

    private LoanScheduleModelDisbursementPeriod(final int periodNumber, final LoanApplicationTerms loanApplicationTerms,
            final BigDecimal chargesDueAtTimeOfDisbursement) {
        this.periodNumber = periodNumber;
        this.disbursementDate = loanApplicationTerms.getExpectedDisbursementDate();
        this.principalDisbursed = loanApplicationTerms.getPrincipal();
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
}