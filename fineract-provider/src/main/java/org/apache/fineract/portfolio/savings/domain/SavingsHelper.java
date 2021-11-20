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
package org.apache.fineract.portfolio.savings.domain;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.domain.interest.CompoundInterestHelper;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class SavingsHelper {

    AccountTransfersReadPlatformService accountTransfersReadPlatformService = null;

    @Autowired
    public SavingsHelper(AccountTransfersReadPlatformService accountTransfersReadPlatformService) {
        this.accountTransfersReadPlatformService = accountTransfersReadPlatformService;
    }

    private final CompoundInterestHelper compoundInterestHelper = new CompoundInterestHelper();

    public List<LocalDateInterval> determineInterestPostingPeriods(final LocalDate startInterestCalculationLocalDate,
            final LocalDate interestPostingUpToDate, final SavingsPostingInterestPeriodType postingPeriodType,
            final Integer financialYearBeginningMonth, List<LocalDate> postInterestAsOn) {

        final List<LocalDateInterval> postingPeriods = new ArrayList<>();
        LocalDate periodStartDate = startInterestCalculationLocalDate;
        LocalDate periodEndDate = periodStartDate;
        LocalDate actualPeriodStartDate = periodStartDate;

        while (!periodStartDate.isAfter(interestPostingUpToDate) && !periodEndDate.isAfter(interestPostingUpToDate)) {

            final LocalDate interestPostingLocalDate = determineInterestPostingPeriodEndDateFrom(periodStartDate, postingPeriodType,
                    interestPostingUpToDate, financialYearBeginningMonth);

            periodEndDate = interestPostingLocalDate.minusDays(1);

            if (!postInterestAsOn.isEmpty()) {
                for (LocalDate transactiondate : postInterestAsOn) {
                    if (periodStartDate.isBefore(transactiondate)
                            && (periodEndDate.isAfter(transactiondate) || periodEndDate.isEqual(transactiondate))) {
                        periodEndDate = transactiondate.minusDays(1);
                        actualPeriodStartDate = periodEndDate;
                        break;
                    }
                }
            }

            postingPeriods.add(LocalDateInterval.create(periodStartDate, periodEndDate));

            if (actualPeriodStartDate.isEqual(periodEndDate)) {
                periodEndDate = actualPeriodStartDate.plusDays(1);
                periodStartDate = actualPeriodStartDate.plusDays(1);
            } else {
                periodEndDate = interestPostingLocalDate;
                periodStartDate = interestPostingLocalDate;
            }
        }

        return postingPeriods;
    }

    private LocalDate determineInterestPostingPeriodEndDateFrom(final LocalDate periodStartDate,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final LocalDate interestPostingUpToDate,
            Integer financialYearBeginningMonth) {

        LocalDate periodEndDate = interestPostingUpToDate;
        final Integer monthOfYear = periodStartDate.getMonthValue();
        financialYearBeginningMonth--;
        if (financialYearBeginningMonth == 0) {
            financialYearBeginningMonth = 12;
        }

        final ArrayList<LocalDate> quarterlyDates = new ArrayList<>();
        quarterlyDates.add(periodStartDate.withMonth(financialYearBeginningMonth).with(TemporalAdjusters.lastDayOfMonth()));
        quarterlyDates.add(periodStartDate.withMonth(financialYearBeginningMonth).plusMonths(3).withYear(periodStartDate.getYear())
                .with(TemporalAdjusters.lastDayOfMonth()));
        quarterlyDates.add(periodStartDate.withMonth(financialYearBeginningMonth).plusMonths(6).withYear(periodStartDate.getYear())
                .with(TemporalAdjusters.lastDayOfMonth()));
        quarterlyDates.add(periodStartDate.withMonth(financialYearBeginningMonth).plusMonths(9).withYear(periodStartDate.getYear())
                .with(TemporalAdjusters.lastDayOfMonth()));
        Collections.sort(quarterlyDates);

        final ArrayList<LocalDate> biannualDates = new ArrayList<>();
        biannualDates.add(periodStartDate.withMonth(financialYearBeginningMonth).with(TemporalAdjusters.lastDayOfMonth()));
        biannualDates.add(periodStartDate.withMonth(financialYearBeginningMonth).plusMonths(6).withYear(periodStartDate.getYear())
                .with(TemporalAdjusters.lastDayOfMonth()));
        Collections.sort(biannualDates);
        boolean isEndDateSet = false;

        switch (interestPostingPeriodType) {
            case INVALID:
            break;
            case DAILY:
                // produce period end date on current day
                periodEndDate = periodStartDate;
            break;
            case MONTHLY:
                // produce period end date on last day of current month
                periodEndDate = periodStartDate.with(TemporalAdjusters.lastDayOfMonth());
            break;
            case QUATERLY:
                for (LocalDate quarterlyDate : quarterlyDates) {
                    if (quarterlyDate.isAfter(periodStartDate)) {
                        periodEndDate = quarterlyDate;
                        isEndDateSet = true;
                        break;
                    }
                }

                if (!isEndDateSet) {
                    periodEndDate = quarterlyDates.get(0).plusYears(1).with(TemporalAdjusters.lastDayOfMonth());
                }
            break;
            case BIANNUAL:
                for (LocalDate biannualDate : biannualDates) {
                    if (biannualDate.isAfter(periodStartDate)) {
                        periodEndDate = biannualDate;
                        isEndDateSet = true;
                        break;
                    }
                }

                if (!isEndDateSet) {
                    periodEndDate = biannualDates.get(0).plusYears(1).with(TemporalAdjusters.lastDayOfMonth());
                }
            break;
            case ANNUAL:
                if (financialYearBeginningMonth < monthOfYear) {
                    periodEndDate = periodStartDate.withMonth(financialYearBeginningMonth);
                    periodEndDate = periodEndDate.plusYears(1);
                } else {
                    periodEndDate = periodStartDate.withMonth(financialYearBeginningMonth);
                }
                periodEndDate = periodEndDate.with(TemporalAdjusters.lastDayOfMonth());
            break;
        }
        // interest posting always occurs on next day after the period end date.
        periodEndDate = periodEndDate.plusDays(1);
        return periodEndDate;
    }

    public Money calculateInterestForAllPostingPeriods(final MonetaryCurrency currency, final List<PostingPeriod> allPeriods,
            LocalDate accountLockedUntil, Boolean immediateWithdrawalOfInterest) {
        return this.compoundInterestHelper.calculateInterestForAllPostingPeriods(currency, allPeriods, accountLockedUntil,
                immediateWithdrawalOfInterest);
    }

    public Collection<Long> fetchPostInterestTransactionIds(Long accountId) {
        return this.accountTransfersReadPlatformService.fetchPostInterestTransactionIds(accountId);
    }

    public Collection<Long> fetchPostInterestTransactionIds(Long accountId, Date pivotDate) {
        return this.accountTransfersReadPlatformService.fetchPostInterestTransactionIdsWithPivotDate(accountId, pivotDate);
    }

}
