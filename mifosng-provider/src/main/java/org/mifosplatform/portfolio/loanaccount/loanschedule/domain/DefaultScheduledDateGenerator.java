/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.holiday.service.HolidayUtil;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.organisation.workingdays.service.WorkingDaysUtil;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;

public class DefaultScheduledDateGenerator implements ScheduledDateGenerator {

    @Override
    public List<LocalDate> generate(final LoanApplicationTerms loanApplicationTerms, final boolean isHolidayEnabled,
            final List<Holiday> holidays, final WorkingDays workingDays) {

        final int numberOfRepayments = loanApplicationTerms.getNumberOfRepayments();

        final List<LocalDate> dueRepaymentPeriodDates = new ArrayList<LocalDate>(numberOfRepayments);

        LocalDate startDate = loanApplicationTerms.getExpectedDisbursementDate();
        final LocalDate firstRepaymentPeriodDate = loanApplicationTerms.getCalculatedRepaymentsStartingFromLocalDate();

        for (int repaymentPeriod = 1; repaymentPeriod <= numberOfRepayments; repaymentPeriod++) {

            LocalDate dueRepaymentPeriodDate = startDate;
            if (repaymentPeriod == 1 && firstRepaymentPeriodDate != null) {
                dueRepaymentPeriodDate = firstRepaymentPeriodDate;
            } else {
                dueRepaymentPeriodDate = getRepaymentPeriodDate(loanApplicationTerms, startDate);
            }
            
            startDate = dueRepaymentPeriodDate;
            LocalDate nextDueRepaymentPeriodDate = getRepaymentPeriodDate(loanApplicationTerms, dueRepaymentPeriodDate);
            
            dueRepaymentPeriodDate = WorkingDaysUtil.getOffSetDateIfNonWorkingDay(dueRepaymentPeriodDate, nextDueRepaymentPeriodDate, workingDays);
            
            if(isHolidayEnabled){
                dueRepaymentPeriodDate = HolidayUtil.getRepaymentRescheduleDateToIfHoliday(dueRepaymentPeriodDate, holidays);
            }

            dueRepaymentPeriodDates.add(dueRepaymentPeriodDate);
        }
        
        return dueRepaymentPeriodDates;
    }

    private LocalDate getRepaymentPeriodDate(final LoanApplicationTerms loanApplicationTerms, LocalDate startDate) {
        final int repaidEvery = loanApplicationTerms.getRepaymentEvery();
        LocalDate dueRepaymentPeriodDate = startDate;
        switch (loanApplicationTerms.getRepaymentPeriodFrequencyType()) {
            case DAYS:
                dueRepaymentPeriodDate = startDate.plusDays(repaidEvery);
            break;
            case WEEKS:
                dueRepaymentPeriodDate = startDate.plusWeeks(repaidEvery);
            break;
            case MONTHS:
                dueRepaymentPeriodDate = startDate.plusMonths(repaidEvery);
            break;
            case YEARS:
                dueRepaymentPeriodDate = startDate.plusYears(repaidEvery);
            break;
            case INVALID:
            break;
        }
        return dueRepaymentPeriodDate;
    }

    @Override
    public LocalDate idealDisbursementDateBasedOnFirstRepaymentDate(final PeriodFrequencyType repaymentPeriodFrequencyType,
            final int repaidEvery, final List<LocalDate> scheduledDates) {

        LocalDate firstRepaymentDate = scheduledDates.get(0);

        LocalDate idealDisbursementDate = null;

        switch (repaymentPeriodFrequencyType) {
            case DAYS:
                idealDisbursementDate = firstRepaymentDate.minusDays(repaidEvery);
            break;
            case WEEKS:
                idealDisbursementDate = firstRepaymentDate.minusWeeks(repaidEvery);
            break;
            case MONTHS:
                idealDisbursementDate = firstRepaymentDate.minusMonths(repaidEvery);
            break;
            case YEARS:
                idealDisbursementDate = firstRepaymentDate.minusYears(repaidEvery);
            break;
            case INVALID:
            break;
        }

        return idealDisbursementDate;
    }
}