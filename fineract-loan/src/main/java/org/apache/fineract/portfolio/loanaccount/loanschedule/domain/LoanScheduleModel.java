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
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;

/**
 * Domain representation of a Loan Schedule (not used for persistence)
 */
@Getter
public final class LoanScheduleModel {

    @Getter
    private final List<LoanScheduleModelPeriod> periods;
    private final CurrencyData currency;
    private final int loanTermInDays;
    private final Money totalPrincipalDisbursed;
    private final BigDecimal totalPrincipalExpected;
    private final BigDecimal totalPrincipalPaid;
    @Getter
    private final BigDecimal totalInterestCharged;
    private final BigDecimal totalFeeChargesCharged;
    @Getter
    private final BigDecimal totalPenaltyChargesCharged;
    private final BigDecimal totalRepaymentExpected;
    private final BigDecimal totalOutstanding;

    public static LoanScheduleModel from(final List<LoanScheduleModelPeriod> periods, final CurrencyData currency, final int loanTermInDays,
            final Money principalDisbursed, final BigDecimal totalPrincipalExpected, final BigDecimal totalPrincipalPaid,
            final BigDecimal totalInterestCharged, final BigDecimal totalFeeChargesCharged, final BigDecimal totalPenaltyChargesCharged,
            final BigDecimal totalRepaymentExpected, final BigDecimal totalOutstanding) {

        return new LoanScheduleModel(periods, currency, loanTermInDays, principalDisbursed, totalPrincipalExpected, totalPrincipalPaid,
                totalInterestCharged, totalFeeChargesCharged, totalPenaltyChargesCharged, totalRepaymentExpected, totalOutstanding);
    }

    public static LoanScheduleModel withOverdueChargeUpdation(final List<LoanScheduleModelPeriod> periods,
            final LoanScheduleModel loanScheduleModel, final BigDecimal totalPenaltyChargesCharged) {

        return new LoanScheduleModel(periods, loanScheduleModel.currency, loanScheduleModel.loanTermInDays,
                loanScheduleModel.totalPrincipalDisbursed, loanScheduleModel.totalPrincipalExpected, loanScheduleModel.totalPrincipalPaid,
                loanScheduleModel.totalInterestCharged, loanScheduleModel.totalFeeChargesCharged, totalPenaltyChargesCharged,
                loanScheduleModel.totalRepaymentExpected, loanScheduleModel.totalOutstanding);
    }

    public static LoanScheduleModel withLoanScheduleModelPeriods(final List<LoanScheduleModelPeriod> periods,
            final LoanScheduleModel loanScheduleModel) {

        return new LoanScheduleModel(periods, loanScheduleModel.currency, loanScheduleModel.loanTermInDays,
                loanScheduleModel.totalPrincipalDisbursed, loanScheduleModel.totalPrincipalExpected, loanScheduleModel.totalPrincipalPaid,
                loanScheduleModel.totalInterestCharged, loanScheduleModel.totalFeeChargesCharged,
                loanScheduleModel.totalPenaltyChargesCharged, loanScheduleModel.totalRepaymentExpected, loanScheduleModel.totalOutstanding);
    }

    private LoanScheduleModel(final List<LoanScheduleModelPeriod> periods, final CurrencyData currency, final int loanTermInDays,
            final Money principalDisbursed, final BigDecimal totalPrincipalExpected, final BigDecimal totalPrincipalPaid,
            final BigDecimal totalInterestCharged, final BigDecimal totalFeeChargesCharged, final BigDecimal totalPenaltyChargesCharged,
            final BigDecimal totalRepaymentExpected, final BigDecimal totalOutstanding) {
        this.periods = periods;
        this.currency = currency;
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

    public LoanScheduleData toData() {
        final BigDecimal totalCredits = BigDecimal.ZERO;

        final List<LoanSchedulePeriodData> periodsData = new ArrayList<>();
        for (final LoanScheduleModelPeriod modelPeriod : this.periods) {
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
                totalPaidInAdvance, totalPaidLate, this.totalOutstanding, totalCredits);
    }

}
