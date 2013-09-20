package org.mifosplatform.portfolio.savings.domain.interest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
import org.mifosplatform.infrastructure.core.service.DateUtils;
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
    @SuppressWarnings("unused")
    private final Money openingBalance;
    private final Money closingBalance;
    private final SavingsInterestCalculationType interestCalculationType;

    public static PostingPeriod createFrom(final LocalDateInterval periodInterval, final Money periodStartingBalance,
            final List<SavingsAccountTransaction> orderedListOfTransactions, final MonetaryCurrency currency,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsInterestCalculationType interestCalculationType, final BigDecimal interestRateAsFraction, final long daysInYear) {

        final List<EndOfDayBalance> accountEndOfDayBalances = new ArrayList<EndOfDayBalance>();

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
        }

        if (accountEndOfDayBalances.isEmpty()) {
            final EndOfDayBalance endOfDayBalance = EndOfDayBalance.from(periodInterval.startDate(), openingDayBalance, closeOfDayBalance,
                    periodInterval.daysInPeriodInclusiveOfEndDate());
            accountEndOfDayBalances.add(endOfDayBalance);

            closeOfDayBalance = endOfDayBalance.closingBalance();
            openingDayBalance = closeOfDayBalance;
        }

        final List<CompoundingPeriod> compoundingPeriods = compoundingPeriodsInPostingPeriod(periodInterval, interestCompoundingPeriodType,
                accountEndOfDayBalances);

        return new PostingPeriod(periodInterval, currency, periodStartingBalance, openingDayBalance, interestCompoundingPeriodType,
                interestCalculationType, interestRateAsFraction, daysInYear, compoundingPeriods);
    }

    private PostingPeriod(final LocalDateInterval periodInterval, final MonetaryCurrency currency, final Money openingBalance,
            final Money closingBalance, final SavingsCompoundingInterestPeriodType interestCompoundingType,
            final SavingsInterestCalculationType interestCalculationType, final BigDecimal interestRateAsFraction, final long daysInYear,
            final List<CompoundingPeriod> compoundingPeriods) {
        this.periodInterval = periodInterval;
        this.currency = currency;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.interestCompoundingType = interestCompoundingType;
        this.interestCalculationType = interestCalculationType;
        this.interestRateAsFraction = interestRateAsFraction;
        this.daysInYear = daysInYear;
        this.compoundingPeriods = compoundingPeriods;

        this.dateOfPostingTransaction = periodInterval.endDate().plusDays(1);
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

    public BigDecimal calculateInterest(final BigDecimal interestFromPreviousPostingPeriod) {
        BigDecimal interestEarned = BigDecimal.ZERO;

        // for each compounding period accumulate the amount of interest
        // to be applied to the balanced for interest calculation
        BigDecimal interestCompounded = interestFromPreviousPostingPeriod;
        for (final CompoundingPeriod compoundingPeriod : this.compoundingPeriods) {

            final BigDecimal interestUnrounded = compoundingPeriod.calculateInterest(this.interestCompoundingType,
                    this.interestCalculationType, interestCompounded, this.interestRateAsFraction, this.daysInYear);
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
            final SavingsCompoundingInterestPeriodType interestPeriodType, final List<EndOfDayBalance> allEndOfDayBalances) {

        final List<CompoundingPeriod> compoundingPeriods = new ArrayList<CompoundingPeriod>();

        CompoundingPeriod compoundingPeriod = null;
        switch (interestPeriodType) {
            case INVALID:
            break;
            case DAILY:
                compoundingPeriod = DailyCompoundingPeriod.create(postingPeriodInterval, allEndOfDayBalances);
                compoundingPeriods.add(compoundingPeriod);
            break;
            case MONTHLY:

                final LocalDate postingPeriodEndDate = postingPeriodInterval.endDate();

                LocalDate periodStartDate = postingPeriodInterval.startDate();
                LocalDate periodEndDate = periodStartDate;

                while (!periodStartDate.isAfter(postingPeriodEndDate) && !periodEndDate.isAfter(postingPeriodEndDate)) {

                    periodEndDate = determineInterestPeriodEndDateFrom(periodStartDate, interestPeriodType);
                    if (periodEndDate.isAfter(postingPeriodEndDate)) {
                        periodEndDate = postingPeriodEndDate;
                    }

                    final LocalDateInterval compoundingPeriodInterval = LocalDateInterval.create(periodStartDate, periodEndDate);
                    if (postingPeriodInterval.contains(compoundingPeriodInterval)) {

                        compoundingPeriod = MonthlyCompoundingPeriod.create(compoundingPeriodInterval, allEndOfDayBalances);
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
        // case QUATERLY:
        // break;
        // case BI_ANNUAL:
        // break;
        // case ANNUAL:
        // break;
        // case NO_COMPOUNDING_SIMPLE_INTEREST:
        // break;
        }

        return compoundingPeriods;
    }

    private static LocalDate determineInterestPeriodEndDateFrom(final LocalDate periodStartDate,
            final SavingsCompoundingInterestPeriodType interestPeriodType) {

        LocalDate periodEndDate = DateUtils.getLocalDateOfTenant();

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
        // case QUATERLY:
        // // jan 1st to mar 31st, 1st apr to jun 30, jul 1st to sept 30,
        // // oct 1st to dec 31
        // int year = periodStartDate.getYearOfEra();
        // int monthofYear = periodStartDate.getMonthOfYear();
        // if (monthofYear <= 3) {
        // periodEndDate = new DateTime().withDate(year, 3, 31).toLocalDate();
        // } else if (monthofYear <= 6) {
        // periodEndDate = new DateTime().withDate(year, 6, 30).toLocalDate();
        // } else if (monthofYear <= 9) {
        // periodEndDate = new DateTime().withDate(year, 9, 30).toLocalDate();
        // } else if (monthofYear <= 12) {
        // periodEndDate = new DateTime().withDate(year, 12, 31).toLocalDate();
        // }
        // break;
        // case BI_ANNUAL:
        // // jan 1st to 30, jul 1st to dec 31
        // year = periodStartDate.getYearOfEra();
        // monthofYear = periodStartDate.getMonthOfYear();
        // if (monthofYear <= 6) {
        // periodEndDate = new DateTime().withDate(year, 6, 30).toLocalDate();
        // } else if (monthofYear <= 12) {
        // periodEndDate = new DateTime().withDate(year, 12, 31).toLocalDate();
        // }
        // break;
        // case ANNUAL:
        // periodEndDate = periodStartDate.monthOfYear().withMaximumValue();
        // periodEndDate = periodEndDate.dayOfMonth().withMaximumValue();
        // break;
        // case NO_COMPOUNDING_SIMPLE_INTEREST:
        // periodEndDate = periodStartDate.monthOfYear().withMaximumValue();
        // periodEndDate = periodEndDate.dayOfMonth().withMaximumValue();
        // break;
        }

        return periodEndDate;
    }
}