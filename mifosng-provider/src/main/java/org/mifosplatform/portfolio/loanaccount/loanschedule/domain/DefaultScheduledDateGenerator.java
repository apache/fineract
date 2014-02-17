/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.holiday.service.HolidayUtil;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.organisation.workingdays.service.WorkingDaysUtil;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;

public class DefaultScheduledDateGenerator implements ScheduledDateGenerator {

    @Override
    public LocalDate getLastRepaymentDate(final LoanApplicationTerms loanApplicationTerms, final boolean isHolidayEnabled,
            final List<Holiday> holidays, final WorkingDays workingDays) {

        final int numberOfRepayments = loanApplicationTerms.getNumberOfRepayments();

        LocalDate lastRepaymentDate = loanApplicationTerms.getExpectedDisbursementDate();

        for (int repaymentPeriod = 1; repaymentPeriod <= numberOfRepayments; repaymentPeriod++) {
            lastRepaymentDate = generateNextRepaymentDate(lastRepaymentDate, loanApplicationTerms);
         }
        lastRepaymentDate = adjustRepaymentDate(lastRepaymentDate, loanApplicationTerms, isHolidayEnabled, holidays, workingDays);
        return lastRepaymentDate;
    }

    
    @Override
    public LocalDate generateNextRepaymentDate(final LocalDate lastRepaymentDate,final LoanApplicationTerms loanApplicationTerms){
        final LocalDate firstRepaymentPeriodDate = loanApplicationTerms.getCalculatedRepaymentsStartingFromLocalDate();
        LocalDate dueRepaymentPeriodDate = null;
        if(firstRepaymentPeriodDate !=null && firstRepaymentPeriodDate.isAfter(lastRepaymentDate)){
            dueRepaymentPeriodDate = firstRepaymentPeriodDate;
        }else{
            dueRepaymentPeriodDate = getRepaymentPeriodDate(loanApplicationTerms, lastRepaymentDate);
        }
        return dueRepaymentPeriodDate;
    }
    
    @Override
    public LocalDate adjustRepaymentDate(final LocalDate dueRepaymentPeriodDate,final LoanApplicationTerms loanApplicationTerms,final boolean isHolidayEnabled,
            final List<Holiday> holidays, final WorkingDays workingDays){
        LocalDate adjustedDate = dueRepaymentPeriodDate;
        final LocalDate nextDueRepaymentPeriodDate = getRepaymentPeriodDate(loanApplicationTerms, adjustedDate);
        adjustedDate = WorkingDaysUtil.getOffSetDateIfNonWorkingDay(adjustedDate, nextDueRepaymentPeriodDate,
                workingDays);
        if (isHolidayEnabled) {
            adjustedDate = HolidayUtil.getRepaymentRescheduleDateToIfHoliday(adjustedDate, holidays);
        }
        return adjustedDate;
    }
    
    
    private LocalDate getRepaymentPeriodDate(final LoanApplicationTerms loanApplicationTerms, final LocalDate startDate) {
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
            final int repaidEvery, final LocalDate firstRepaymentDate) {

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