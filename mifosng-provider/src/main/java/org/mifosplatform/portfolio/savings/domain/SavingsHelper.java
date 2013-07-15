package org.mifosplatform.portfolio.savings.domain;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;
import org.mifosplatform.portfolio.savings.domain.interest.CompoundInterestHelper;
import org.mifosplatform.portfolio.savings.domain.interest.PostingPeriod;

public final class SavingsHelper {

    private final CompoundInterestHelper compoundInterestHelper = new CompoundInterestHelper();

    public List<LocalDateInterval> determineInterestPostingPeriods(final LocalDate activationLocalDate,
            final LocalDate interestPostingUpToDate, final SavingsPostingInterestPeriodType postingPeriodType) {

        List<LocalDateInterval> postingPeriods = new ArrayList<LocalDateInterval>();

        LocalDate periodStartDate = activationLocalDate;
        LocalDate periodEndDate = periodStartDate;

        while (!periodStartDate.isAfter(interestPostingUpToDate) && !periodEndDate.isAfter(interestPostingUpToDate)) {

            final LocalDate interestPostingLocalDate = determineInterestPostingPeriodEndDateFrom(periodStartDate, postingPeriodType);
            periodEndDate = interestPostingLocalDate.minusDays(1);

            if (!interestPostingLocalDate.isAfter(DateUtils.getLocalDateOfTenant())) {
                postingPeriods.add(LocalDateInterval.create(periodStartDate, periodEndDate));
            } else {
                postingPeriods.add(LocalDateInterval.create(periodStartDate, periodEndDate));
            }

            periodEndDate = interestPostingLocalDate;
            periodStartDate = interestPostingLocalDate;
        }

        return postingPeriods;
    }

    private LocalDate determineInterestPostingPeriodEndDateFrom(final LocalDate periodStartDate,
            final SavingsPostingInterestPeriodType interestPostingPeriodType) {

        LocalDate periodEndDate = DateUtils.getLocalDateOfTenant();

        switch (interestPostingPeriodType) {
            case INVALID:
            break;
            case MONTHLY:
                // produce period end date on last day of current month
                periodEndDate = periodStartDate.dayOfMonth().withMaximumValue();
            break;
            case QUATERLY:
                // jan 1st to mar 31st, 1st apr to jun 30, jul 1st to sept 30,
                // oct 1st to dec 31
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
            case ANNUAL:
                periodEndDate = periodStartDate.monthOfYear().withMaximumValue();
                periodEndDate = periodEndDate.dayOfMonth().withMaximumValue();
            break;
        }

        // interest posting always occurs on next day after the period end date.
        periodEndDate = periodEndDate.plusDays(1);

        return periodEndDate;
    }

    public Money calculateInterestForAllPostingPeriods(final MonetaryCurrency currency, final List<PostingPeriod> allPeriods) {
        return this.compoundInterestHelper.calculateInterestForAllPostingPeriods(currency, allPeriods);
    }
}