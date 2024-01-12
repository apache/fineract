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
package org.apache.fineract.portfolio.loanaccount.jobs.applyholidaystoloans;

import static org.apache.fineract.infrastructure.core.service.DateUtils.isDateWithinRange;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanRescheduledDueHolidayBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.DefaultScheduledDateGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ApplyHolidaysToLoansTasklet implements Tasklet {

    private final ConfigurationDomainService configurationDomainService;
    private final HolidayRepositoryWrapper holidayRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanUtilService loanUtilService;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final boolean isHolidayEnabled = configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();

        if (!isHolidayEnabled) {
            return RepeatStatus.FINISHED;
        }

        final Collection<Integer> loanStatuses = new ArrayList<>(Arrays.asList(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(),
                LoanStatus.APPROVED.getValue(), LoanStatus.ACTIVE.getValue()));
        final List<Holiday> holidays = holidayRepository.findUnprocessed();

        for (final Holiday holiday : holidays) {
            final Set<Office> offices = holiday.getOffices();
            final Collection<Long> officeIds = new ArrayList<>(offices.size());
            for (final Office office : offices) {
                officeIds.add(office.getId());
            }

            final List<Loan> loans = new ArrayList<>();
            loans.addAll(loanRepositoryWrapper.findByClientOfficeIdsAndLoanStatus(officeIds, loanStatuses));
            // FIXME: AA optimize to get all client and group loans belongs to an office id
            loans.addAll(loanRepositoryWrapper.findByGroupOfficeIdsAndLoanStatus(officeIds, loanStatuses));

            for (final Loan loan : loans) {
                applyHolidayToRepaymentScheduleDates(loan, holiday);
            }
            loanRepositoryWrapper.save(loans);
            holiday.setProcessed(true);
        }
        holidayRepository.save(holidays);
        return RepeatStatus.FINISHED;
    }

    public void applyHolidayToRepaymentScheduleDates(Loan loan, Holiday holiday) {
        LocalDate adjustedRescheduleToDate = null;
        boolean isResheduleToNextRepaymentDate = holiday.getReScheduleType().isResheduleToNextRepaymentDate();
        if (holiday.getReScheduleType().isResheduleToNextRepaymentDate()) {
            adjustedRescheduleToDate = getNextRepaymentDate(loan, holiday);
        } else {
            adjustedRescheduleToDate = holiday.getRepaymentsRescheduledTo();
        }

        if (isRepaymentScheduleAdjustmentNeeded(adjustedRescheduleToDate)) {
            if (isResheduleToNextRepaymentDate) {
                adjustAllRepaymentSchedules(loan, holiday, adjustedRescheduleToDate);
            } else {
                adjustRepaymentSchedules(loan, holiday, adjustedRescheduleToDate);
            }
            businessEventNotifierService.notifyPostBusinessEvent(new LoanRescheduledDueHolidayBusinessEvent(loan));
        }
    }

    private boolean isRepaymentScheduleAdjustmentNeeded(LocalDate adjustedRescheduleToDate) {
        return adjustedRescheduleToDate != null;
    }

    private void adjustRepaymentSchedules(Loan loan, Holiday holiday, LocalDate adjustedRescheduleToDate) {
        final DefaultScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
        ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, holiday.getFromDate());
        final LoanApplicationTerms loanApplicationTerms = loan.constructLoanApplicationTerms(scheduleGeneratorDTO);

        // first repayment's from date is same as disbursement date.
        LocalDate tmpFromDate = loan.getDisbursementDate();

        // Loop through all loanRepayments
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : installments) {
            final LocalDate oldDueDate = loanRepaymentScheduleInstallment.getDueDate();

            // update from date if it's not same as previous installment's due
            // date.
            if (!DateUtils.isEqual(tmpFromDate, loanRepaymentScheduleInstallment.getFromDate())) {
                loanRepaymentScheduleInstallment.updateFromDate(tmpFromDate);
            }

            if (isDateWithinRange(oldDueDate, holiday.getFromDate(), holiday.getToDate())) {
                // FIXME: AA do we need to apply non-working days.
                // Assuming holiday's repayment reschedule to date cannot be
                // created on a non-working day.

                adjustedRescheduleToDate = scheduledDateGenerator.generateNextRepaymentDateWhenHolidayApply(adjustedRescheduleToDate,
                        loanApplicationTerms);
                loanRepaymentScheduleInstallment.updateDueDate(adjustedRescheduleToDate);
            }
            tmpFromDate = loanRepaymentScheduleInstallment.getDueDate();
        }
    }

    private void adjustAllRepaymentSchedules(Loan loan, Holiday holiday, LocalDate adjustedRescheduleToDate) {
        final DefaultScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
        ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, holiday.getFromDate());
        final LoanApplicationTerms loanApplicationTerms = loan.constructLoanApplicationTerms(scheduleGeneratorDTO);

        // first repayment's from date is same as disbursement date.
        LocalDate tmpFromDate = loan.getDisbursementDate();

        // Loop through all loanRepayments
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : installments) {
            final LocalDate oldDueDate = loanRepaymentScheduleInstallment.getDueDate();

            // update from date if it's not same as previous installment's due
            // date.
            if (!DateUtils.isEqual(tmpFromDate, loanRepaymentScheduleInstallment.getFromDate())) {
                loanRepaymentScheduleInstallment.updateFromDate(tmpFromDate);
            }

            if (!DateUtils.isBefore(oldDueDate, holiday.getFromDate())) {
                // FIXME: AA do we need to apply non-working days.
                // Assuming holiday's repayment reschedule to date cannot be
                // created on a non-working day.

                adjustedRescheduleToDate = scheduledDateGenerator.generateNextRepaymentDate(adjustedRescheduleToDate, loanApplicationTerms,
                        false);
                loanRepaymentScheduleInstallment.updateDueDate(adjustedRescheduleToDate);
            }
            tmpFromDate = loanRepaymentScheduleInstallment.getDueDate();
        }
    }

    private LocalDate getNextRepaymentDate(Loan loan, Holiday holiday) {
        LocalDate adjustedRescheduleToDate = null;
        final LocalDate rescheduleToDate = holiday.getToDate();
        for (final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : loan.getRepaymentScheduleInstallments()) {
            if (DateUtils.isEqual(rescheduleToDate, loanRepaymentScheduleInstallment.getDueDate())) {
                adjustedRescheduleToDate = rescheduleToDate;
                break;
            } else {
                adjustedRescheduleToDate = doStandardMonthlyCheck(adjustedRescheduleToDate, rescheduleToDate,
                        loanRepaymentScheduleInstallment);
            }
        }
        return adjustedRescheduleToDate;
    }

    private LocalDate doStandardMonthlyCheck(LocalDate adjustedRescheduleToDate, LocalDate rescheduleToDate,
            LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment) {
        // Standard Monthly Loan Holiday check
        LocalDate dueDate = loanRepaymentScheduleInstallment.getDueDate();
        if (DateUtils.isAfter(rescheduleToDate, dueDate) && DateUtils.isBefore(rescheduleToDate, dueDate.plusDays(30))) {
            adjustedRescheduleToDate = dueDate;
        }
        return adjustedRescheduleToDate;
    }
}
