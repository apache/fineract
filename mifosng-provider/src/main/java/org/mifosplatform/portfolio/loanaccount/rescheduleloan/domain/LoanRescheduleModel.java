/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleHistory;

public final class LoanRescheduleModel {

    private final Collection<LoanRescheduleModelRepaymentPeriod> periods;
    private final Collection<LoanRepaymentScheduleHistory> oldPeriods;
    private final ApplicationCurrency applicationCurrency;
    private final int loanTermInDays;
    private final Money totalPrincipalDisbursed;
    private final BigDecimal totalPrincipalExpected;
    private final BigDecimal totalPrincipalPaid;
    private final BigDecimal totalInterestCharged;
    private final BigDecimal totalFeeChargesCharged;
    private final BigDecimal totalPenaltyChargesCharged;
    private final BigDecimal totalRepaymentExpected;
    private final BigDecimal totalOutstanding;

    private LoanRescheduleModel(final Collection<LoanRescheduleModelRepaymentPeriod> periods,
            final Collection<LoanRepaymentScheduleHistory> oldPeriods, final ApplicationCurrency applicationCurrency,
            final int loanTermInDays, final Money principalDisbursed, final BigDecimal totalPrincipalExpected,
            final BigDecimal totalPrincipalPaid, final BigDecimal totalInterestCharged, final BigDecimal totalFeeChargesCharged,
            final BigDecimal totalPenaltyChargesCharged, final BigDecimal totalRepaymentExpected, final BigDecimal totalOutstanding) {
        this.periods = periods;
        this.oldPeriods = oldPeriods;
        this.applicationCurrency = applicationCurrency;
        this.loanTermInDays = loanTermInDays;
        this.totalPrincipalDisbursed = principalDisbursed;
        this.totalPrincipalExpected = totalPrincipalExpected;
        this.totalPrincipalPaid = totalPrincipalPaid;
        this.totalInterestCharged = totalInterestCharged;
        this.totalFeeChargesCharged = totalFeeChargesCharged;
        this.totalPenaltyChargesCharged = totalPenaltyChargesCharged;
        this.totalRepaymentExpected = totalRepaymentExpected;
        this.totalOutstanding = totalOutstanding;
    }

    public static LoanRescheduleModel instance(final Collection<LoanRescheduleModelRepaymentPeriod> periods,
            final Collection<LoanRepaymentScheduleHistory> oldPeriods, final ApplicationCurrency applicationCurrency,
            final int loanTermInDays, final Money principalDisbursed, final BigDecimal totalPrincipalExpected,
            final BigDecimal totalPrincipalPaid, final BigDecimal totalInterestCharged, final BigDecimal totalFeeChargesCharged,
            final BigDecimal totalPenaltyChargesCharged, final BigDecimal totalRepaymentExpected, final BigDecimal totalOutstanding) {

        return new LoanRescheduleModel(periods, oldPeriods, applicationCurrency, loanTermInDays, principalDisbursed,
                totalPrincipalExpected, totalPrincipalPaid, totalInterestCharged, totalFeeChargesCharged, totalPenaltyChargesCharged,
                totalRepaymentExpected, totalOutstanding);
    }

    public static LoanRescheduleModel createWithSchedulehistory(LoanRescheduleModel loanRescheduleModel,
            final Collection<LoanRepaymentScheduleHistory> oldPeriods) {

        return new LoanRescheduleModel(loanRescheduleModel.periods, oldPeriods, loanRescheduleModel.applicationCurrency,
                loanRescheduleModel.loanTermInDays, loanRescheduleModel.totalPrincipalDisbursed,
                loanRescheduleModel.totalPrincipalExpected, loanRescheduleModel.totalPrincipalPaid,
                loanRescheduleModel.totalInterestCharged, loanRescheduleModel.totalFeeChargesCharged,
                loanRescheduleModel.totalPenaltyChargesCharged, loanRescheduleModel.totalRepaymentExpected,
                loanRescheduleModel.totalOutstanding);
    }

    public LoanScheduleData toData() {

        final int decimalPlaces = this.totalPrincipalDisbursed.getCurrencyDigitsAfterDecimal();
        final Integer inMultiplesOf = this.totalPrincipalDisbursed.getCurrencyInMultiplesOf();
        final CurrencyData currency = this.applicationCurrency.toData(decimalPlaces, inMultiplesOf);

        final Collection<LoanSchedulePeriodData> periodsData = new ArrayList<>();
        for (final LoanRescheduleModalPeriod modelPeriod : this.periods) {
            periodsData.add(modelPeriod.toData());
        }

        final BigDecimal totalWaived = null;
        final BigDecimal totalWrittenOff = null;
        final BigDecimal totalRepayment = null;
        final BigDecimal totalPaidInAdvance = null;
        final BigDecimal totalPaidLate = null;

        return new LoanScheduleData(currency, periodsData, this.loanTermInDays, this.totalPrincipalDisbursed.getAmount(),
                this.totalPrincipalExpected, this.totalPrincipalPaid, this.totalInterestCharged, this.totalFeeChargesCharged,
                this.totalPenaltyChargesCharged, totalWaived, totalWrittenOff, this.totalRepaymentExpected, totalRepayment,
                totalPaidInAdvance, totalPaidLate, this.totalOutstanding);
    }

    public Collection<LoanRescheduleModelRepaymentPeriod> getPeriods() {
        return this.periods;
    }

    public Collection<LoanRepaymentScheduleHistory> getOldPeriods() {
        return this.oldPeriods;
    }
}
