/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.account.service.AccountTransfersReadPlatformService;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;
import org.mifosplatform.portfolio.savings.domain.interest.CompoundInterestHelper;
import org.mifosplatform.portfolio.savings.domain.interest.PostingPeriod;

public final class SavingsHelper {

    AccountTransfersReadPlatformService accountTransfersReadPlatformService = null;

    public SavingsHelper(AccountTransfersReadPlatformService accountTransfersReadPlatformService) {
        this.accountTransfersReadPlatformService = accountTransfersReadPlatformService;
    }

    private final CompoundInterestHelper compoundInterestHelper = new CompoundInterestHelper();

    public List<LocalDateInterval> determineInterestPostingPeriods(final LocalDate startInterestCalculationLocalDate,
            final LocalDate interestPostingUpToDate, final SavingsPostingInterestPeriodType postingPeriodType,
            final Integer financialYearBeginningMonth) {

        final List<LocalDateInterval> postingPeriods = new ArrayList<>();

        LocalDate periodStartDate = startInterestCalculationLocalDate;
        LocalDate periodEndDate = periodStartDate;

        while (!periodStartDate.isAfter(interestPostingUpToDate) && !periodEndDate.isAfter(interestPostingUpToDate)) {

            final LocalDate interestPostingLocalDate = determineInterestPostingPeriodEndDateFrom(periodStartDate, postingPeriodType,
                    interestPostingUpToDate, financialYearBeginningMonth);
            periodEndDate = interestPostingLocalDate.minusDays(1);

            postingPeriods.add(LocalDateInterval.create(periodStartDate, periodEndDate));

            periodEndDate = interestPostingLocalDate;
            periodStartDate = interestPostingLocalDate;
        }

        return postingPeriods;
    }

    private LocalDate determineInterestPostingPeriodEndDateFrom(final LocalDate periodStartDate,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final LocalDate interestPostingUpToDate,
            Integer financialYearBeginningMonth) {

        LocalDate periodEndDate = interestPostingUpToDate;
        final Integer monthOfYear = periodStartDate.getMonthOfYear();
        financialYearBeginningMonth--;
        if (financialYearBeginningMonth == 0) financialYearBeginningMonth = 12;

        final ArrayList<LocalDate> quarterlyDates = new ArrayList<>();
        quarterlyDates.add(periodStartDate.withMonthOfYear(financialYearBeginningMonth).dayOfMonth().withMaximumValue());
        quarterlyDates.add(periodStartDate.withMonthOfYear(financialYearBeginningMonth).plusMonths(3).withYear(periodStartDate.getYear())
                .dayOfMonth().withMaximumValue());
        quarterlyDates.add(periodStartDate.withMonthOfYear(financialYearBeginningMonth).plusMonths(6).withYear(periodStartDate.getYear())
                .dayOfMonth().withMaximumValue());
        quarterlyDates.add(periodStartDate.withMonthOfYear(financialYearBeginningMonth).plusMonths(9).withYear(periodStartDate.getYear())
                .dayOfMonth().withMaximumValue());
        Collections.sort(quarterlyDates);

        final ArrayList<LocalDate> biannualDates = new ArrayList<>();
        biannualDates.add(periodStartDate.withMonthOfYear(financialYearBeginningMonth).dayOfMonth().withMaximumValue());
        biannualDates.add(periodStartDate.withMonthOfYear(financialYearBeginningMonth).plusMonths(6).withYear(periodStartDate.getYear())
                .dayOfMonth().withMaximumValue());
        Collections.sort(biannualDates);

        boolean isEndDateSet = false;

        switch (interestPostingPeriodType) {
            case INVALID:
            break;
            case MONTHLY:
                // produce period end date on last day of current month
                periodEndDate = periodStartDate.dayOfMonth().withMaximumValue();
            break;
            case QUATERLY:
                for (LocalDate quarterlyDate : quarterlyDates) {
                    if (quarterlyDate.isAfter(periodStartDate)) {
                        periodEndDate = quarterlyDate;
                        isEndDateSet = true;
                        break;
                    }
                }

                if (!isEndDateSet) periodEndDate = quarterlyDates.get(0).plusYears(1).dayOfMonth().withMaximumValue();
            break;
            case BIANNUAL:
                for (LocalDate biannualDate : biannualDates) {
                    if (biannualDate.isAfter(periodStartDate)) {
                        periodEndDate = biannualDate;
                        isEndDateSet = true;
                        break;
                    }
                }

                if (!isEndDateSet) periodEndDate = biannualDates.get(0).plusYears(1).dayOfMonth().withMaximumValue();
            break;
            case ANNUAL:
                if (financialYearBeginningMonth < monthOfYear) {
                    periodEndDate = periodStartDate.withMonthOfYear(financialYearBeginningMonth);
                    periodEndDate = periodEndDate.plusYears(1);
                } else {
                    periodEndDate = periodStartDate.withMonthOfYear(financialYearBeginningMonth);
                }
                periodEndDate = periodEndDate.dayOfMonth().withMaximumValue();
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
}