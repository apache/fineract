/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain.interest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransaction;

public class PostingPeriod {

    @SuppressWarnings("unused")
    private final LocalDateInterval periodInterval;
    private final MonetaryCurrency currency;
    private final SavingsCompoundingInterestPeriodType interestCompoundingType;
    private final BigDecimal interestRateAsFraction;
    private final long daysInYear;
    private final List<CompoundingPeriod> compoundingPeriods;

    // interest posting details
    private final LocalDate dateOfPostingTransaction;
    private BigDecimal interestEarnedUnrounded;
    private Money interestEarnedRounded;

    // opening/closing details
    private final Money openingBalance;
    private final Money closingBalance;
    private final SavingsInterestCalculationType interestCalculationType;

    // include in compounding interest
    private boolean interestTransfered = false;

    // minimum balance for interest calculation
    private final Money minBalanceForInterestCalculation;

    // isInterestTransfer boolean is to identify newly created transaction is
    // interest transfer
    public static PostingPeriod createFrom(final LocalDateInterval periodInterval, final Money periodStartingBalance,
            final List<SavingsAccountTransaction> orderedListOfTransactions, final MonetaryCurrency currency,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsInterestCalculationType interestCalculationType, final BigDecimal interestRateAsFraction, final long daysInYear,
            final LocalDate upToInterestCalculationDate, Collection<Long> interestPostTransactions, boolean isInterestTransfer,
            final Money minBalanceForInterestCalculation, final boolean isSavingsInterestPostingAtCurrentPeriodEnd) {

        final List<EndOfDayBalance> accountEndOfDayBalances = new ArrayList<>();
        boolean interestTransfered = false;
        Money openingDayBalance = periodStartingBalance;
        Money closeOfDayBalance = openingDayBalance;
        for (final SavingsAccountTransaction transaction : orderedListOfTransactions) {

            if (transaction.fallsWithin(periodInterval)) {
                // the balance of the transaction falls entirely within this
                // period so no need to do any cropping/bounding
                final EndOfDayBalance endOfDayBalance = transaction.toEndOfDayBalance(openingDayBalance);
                accountEndOfDayBalances.add(endOfDayBalance);

                openingDayBalance = endOfDayBalance.closingBalance();

            } else if (transaction.spansAnyPortionOf(periodInterval)) {
                final EndOfDayBalance endOfDayBalance = transaction.toEndOfDayBalanceBoundedBy(openingDayBalance, periodInterval);
                accountEndOfDayBalances.add(endOfDayBalance);

                closeOfDayBalance = endOfDayBalance.closingBalance();
                openingDayBalance = closeOfDayBalance;
            }

            // this check is to make sure to add interest if withdrawal is
            // happened for already
            if (transaction.occursOn(periodInterval.endDate().plusDays(1))) {
                if (transaction.getId() == null) {
                    interestTransfered = isInterestTransfer;
                } else if (interestPostTransactions.contains(transaction.getId())) {
                    interestTransfered = true;
                }
            }

        }

        if (accountEndOfDayBalances.isEmpty()) {
            LocalDate balanceStartDate = periodInterval.startDate();
            LocalDate balanceEndDate = periodInterval.endDate();
            Integer numberOfDaysOfBalance = periodInterval.daysInPeriodInclusiveOfEndDate();

            if (balanceEndDate.isAfter(upToInterestCalculationDate)) {
                balanceEndDate = upToInterestCalculationDate;
                final LocalDateInterval spanOfBalance = LocalDateInterval.create(balanceStartDate, balanceEndDate);
                numberOfDaysOfBalance = spanOfBalance.daysInPeriodInclusiveOfEndDate();
            }

            final EndOfDayBalance endOfDayBalance = EndOfDayBalance.from(balanceStartDate, openingDayBalance, closeOfDayBalance,
                    numberOfDaysOfBalance);

            accountEndOfDayBalances.add(endOfDayBalance);

            closeOfDayBalance = endOfDayBalance.closingBalance();
            openingDayBalance = closeOfDayBalance;
        }

        final List<CompoundingPeriod> compoundingPeriods = compoundingPeriodsInPostingPeriod(periodInterval, interestCompoundingPeriodType,
                accountEndOfDayBalances, upToInterestCalculationDate);

        return new PostingPeriod(periodInterval, currency, periodStartingBalance, openingDayBalance, interestCompoundingPeriodType,
                interestCalculationType, interestRateAsFraction, daysInYear, compoundingPeriods, interestTransfered,
                minBalanceForInterestCalculation, isSavingsInterestPostingAtCurrentPeriodEnd);
    }

    private PostingPeriod(final LocalDateInterval periodInterval, final MonetaryCurrency currency, final Money openingBalance,
            final Money closingBalance, final SavingsCompoundingInterestPeriodType interestCompoundingType,
            final SavingsInterestCalculationType interestCalculationType, final BigDecimal interestRateAsFraction, final long daysInYear,
            final List<CompoundingPeriod> compoundingPeriods, boolean interestTransfered, final Money minBalanceForInterestCalculation,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd) {
        this.periodInterval = periodInterval;
        this.currency = currency;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.interestCompoundingType = interestCompoundingType;
        this.interestCalculationType = interestCalculationType;
        this.interestRateAsFraction = interestRateAsFraction;
        this.daysInYear = daysInYear;
        this.compoundingPeriods = compoundingPeriods;

        if (isSavingsInterestPostingAtCurrentPeriodEnd)
            this.dateOfPostingTransaction = periodInterval.endDate();
        else
            this.dateOfPostingTransaction = periodInterval.endDate().plusDays(1);
        this.interestTransfered = interestTransfered;
        this.minBalanceForInterestCalculation = minBalanceForInterestCalculation;
    }

    public Money interest() {
        return this.interestEarnedRounded;
    }

    public LocalDate dateOfPostingTransaction() {
        return this.dateOfPostingTransaction;
    }

    public Money closingBalance() {
        return this.closingBalance;
    }

    public Money openingBalance() {
        return this.openingBalance;
    }

    public BigDecimal calculateInterest(final BigDecimal interestFromPreviousPostingPeriod) {
        BigDecimal interestEarned = BigDecimal.ZERO;

        // for each compounding period accumulate the amount of interest
        // to be applied to the balanced for interest calculation
        BigDecimal interestCompounded = interestFromPreviousPostingPeriod;
        for (final CompoundingPeriod compoundingPeriod : this.compoundingPeriods) {

            final BigDecimal interestUnrounded = compoundingPeriod.calculateInterest(this.interestCompoundingType,
                    this.interestCalculationType, interestCompounded, this.interestRateAsFraction, this.daysInYear,
                    this.minBalanceForInterestCalculation.getAmount());
            interestCompounded = interestCompounded.add(interestUnrounded);
            interestEarned = interestEarned.add(interestUnrounded);
        }

        this.interestEarnedUnrounded = interestEarned;
        this.interestEarnedRounded = Money.of(this.currency, this.interestEarnedUnrounded);

        return interestEarned;
    }

    public Money getInterestEarned() {
        return this.interestEarnedRounded;
    }

    private static List<CompoundingPeriod> compoundingPeriodsInPostingPeriod(final LocalDateInterval postingPeriodInterval,
            final SavingsCompoundingInterestPeriodType interestPeriodType, final List<EndOfDayBalance> allEndOfDayBalances,
            final LocalDate upToInterestCalculationDate) {

        final List<CompoundingPeriod> compoundingPeriods = new ArrayList<>();

        CompoundingPeriod compoundingPeriod = null;
        switch (interestPeriodType) {
            case INVALID:
            break;
            case DAILY:
                compoundingPeriod = DailyCompoundingPeriod.create(postingPeriodInterval, allEndOfDayBalances, upToInterestCalculationDate);
                compoundingPeriods.add(compoundingPeriod);
            break;
            case MONTHLY:

                final LocalDate postingPeriodEndDate = postingPeriodInterval.endDate();

                LocalDate periodStartDate = postingPeriodInterval.startDate();
                LocalDate periodEndDate = periodStartDate;

                while (!periodStartDate.isAfter(postingPeriodEndDate) && !periodEndDate.isAfter(postingPeriodEndDate)) {

                    periodEndDate = determineInterestPeriodEndDateFrom(periodStartDate, interestPeriodType, upToInterestCalculationDate);
                    if (periodEndDate.isAfter(postingPeriodEndDate)) {
                        periodEndDate = postingPeriodEndDate;
                    }

                    final LocalDateInterval compoundingPeriodInterval = LocalDateInterval.create(periodStartDate, periodEndDate);
                    if (postingPeriodInterval.contains(compoundingPeriodInterval)) {

                        compoundingPeriod = MonthlyCompoundingPeriod.create(compoundingPeriodInterval, allEndOfDayBalances,
                                upToInterestCalculationDate);
                        compoundingPeriods.add(compoundingPeriod);
                    }

                    // move periodStartDate forward to day after this period
                    periodStartDate = periodEndDate.plusDays(1);
                }
            break;
            // case WEEKLY:
            // break;
            // case BIWEEKLY:
            // break;
            case QUATERLY:
                final LocalDate qPostingPeriodEndDate = postingPeriodInterval.endDate();

                periodStartDate = postingPeriodInterval.startDate();
                periodEndDate = periodStartDate;

                while (!periodStartDate.isAfter(qPostingPeriodEndDate) && !periodEndDate.isAfter(qPostingPeriodEndDate)) {

                    periodEndDate = determineInterestPeriodEndDateFrom(periodStartDate, interestPeriodType, upToInterestCalculationDate);
                    if (periodEndDate.isAfter(qPostingPeriodEndDate)) {
                        periodEndDate = qPostingPeriodEndDate;
                    }

                    final LocalDateInterval compoundingPeriodInterval = LocalDateInterval.create(periodStartDate, periodEndDate);
                    if (postingPeriodInterval.contains(compoundingPeriodInterval)) {

                        compoundingPeriod = QuarterlyCompoundingPeriod.create(compoundingPeriodInterval, allEndOfDayBalances,
                                upToInterestCalculationDate);
                        compoundingPeriods.add(compoundingPeriod);
                    }

                    // move periodStartDate forward to day after this period
                    periodStartDate = periodEndDate.plusDays(1);
                }
            break;
            case BI_ANNUAL:
                final LocalDate bPostingPeriodEndDate = postingPeriodInterval.endDate();

                periodStartDate = postingPeriodInterval.startDate();
                periodEndDate = periodStartDate;

                while (!periodStartDate.isAfter(bPostingPeriodEndDate) && !periodEndDate.isAfter(bPostingPeriodEndDate)) {

                    periodEndDate = determineInterestPeriodEndDateFrom(periodStartDate, interestPeriodType, upToInterestCalculationDate);
                    if (periodEndDate.isAfter(bPostingPeriodEndDate)) {
                        periodEndDate = bPostingPeriodEndDate;
                    }

                    final LocalDateInterval compoundingPeriodInterval = LocalDateInterval.create(periodStartDate, periodEndDate);
                    if (postingPeriodInterval.contains(compoundingPeriodInterval)) {

                        compoundingPeriod = BiAnnualCompoundingPeriod.create(compoundingPeriodInterval, allEndOfDayBalances,
                                upToInterestCalculationDate);
                        compoundingPeriods.add(compoundingPeriod);
                    }

                    // move periodStartDate forward to day after this period
                    periodStartDate = periodEndDate.plusDays(1);
                }
            break;
            case ANNUAL:
                final LocalDate aPostingPeriodEndDate = postingPeriodInterval.endDate();

                periodStartDate = postingPeriodInterval.startDate();
                periodEndDate = periodStartDate;

                while (!periodStartDate.isAfter(aPostingPeriodEndDate) && !periodEndDate.isAfter(aPostingPeriodEndDate)) {

                    periodEndDate = determineInterestPeriodEndDateFrom(periodStartDate, interestPeriodType, upToInterestCalculationDate);
                    if (periodEndDate.isAfter(aPostingPeriodEndDate)) {
                        periodEndDate = aPostingPeriodEndDate;
                    }

                    final LocalDateInterval compoundingPeriodInterval = LocalDateInterval.create(periodStartDate, periodEndDate);
                    if (postingPeriodInterval.contains(compoundingPeriodInterval)) {

                        compoundingPeriod = AnnualCompoundingPeriod.create(compoundingPeriodInterval, allEndOfDayBalances,
                                upToInterestCalculationDate);
                        compoundingPeriods.add(compoundingPeriod);
                    }

                    // move periodStartDate forward to day after this period
                    periodStartDate = periodEndDate.plusDays(1);
                }
            break;
        // case NO_COMPOUNDING_SIMPLE_INTEREST:
        // break;
        }

        return compoundingPeriods;
    }

    private static LocalDate determineInterestPeriodEndDateFrom(final LocalDate periodStartDate,
            final SavingsCompoundingInterestPeriodType interestPeriodType, final LocalDate upToInterestCalculationDate) {

        LocalDate periodEndDate = upToInterestCalculationDate;

        switch (interestPeriodType) {
            case INVALID:
            break;
            case DAILY:
                periodEndDate = periodStartDate;
            break;
            // case WEEKLY:
            // periodEndDate = periodStartDate.dayOfWeek().withMaximumValue();
            // break;
            // case BIWEEKLY:
            // final LocalDate closestEndOfWeek =
            // periodStartDate.dayOfWeek().withMaximumValue();
            // periodEndDate = closestEndOfWeek.plusWeeks(1);
            // break;
            case MONTHLY:
                // produce period end date on last day of current month
                periodEndDate = periodStartDate.dayOfMonth().withMaximumValue();
            break;
            case QUATERLY:
                // // jan 1st to mar 31st, 1st apr to jun 30, jul 1st to sept
                // 30,
                // // oct 1st to dec 31
                int year = periodStartDate.getYearOfEra();
                int monthofYear = periodStartDate.getMonthOfYear();
                if (monthofYear <= 3) {
                    periodEndDate = new DateTime().withDate(year, 3, 31).toLocalDate();
                } else if (monthofYear <= 6) {
                    periodEndDate = new DateTime().withDate(year, 6, 30).toLocalDate();
                } else if (monthofYear <= 9) {
                    periodEndDate = new DateTime().withDate(year, 9, 30).toLocalDate();
                } else if (monthofYear <= 12) {
                    periodEndDate = new DateTime().withDate(year, 12, 31).toLocalDate();
                }
            break;
            case BI_ANNUAL:
                // // jan 1st to 30, jul 1st to dec 31
                year = periodStartDate.getYearOfEra();
                monthofYear = periodStartDate.getMonthOfYear();
                if (monthofYear <= 6) {
                    periodEndDate = new DateTime().withDate(year, 6, 30).toLocalDate();
                } else if (monthofYear <= 12) {
                    periodEndDate = new DateTime().withDate(year, 12, 31).toLocalDate();
                }
            break;
            case ANNUAL:
                periodEndDate = periodStartDate.monthOfYear().withMaximumValue();
                periodEndDate = periodEndDate.dayOfMonth().withMaximumValue();
            break;

        // case NO_COMPOUNDING_SIMPLE_INTEREST:
        // periodEndDate = periodStartDate.monthOfYear().withMaximumValue();
        // periodEndDate = periodEndDate.dayOfMonth().withMaximumValue();
        // break;
        }

        return periodEndDate;
    }

    public boolean isInterestTransfered() {
        return this.interestTransfered;
    }
}