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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.domain.HolidayRepository;
import org.apache.fineract.organisation.holiday.domain.HolidayStatusType;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.calendar.data.CalendarHistoryDataWrapper;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarHistory;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateDTO;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.apache.fineract.portfolio.floatingrates.exception.FloatingRateNotFoundException;
import org.apache.fineract.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;

@RequiredArgsConstructor
public class LoanUtilService {

    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final HolidayRepository holidayRepository;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final LoanScheduleGeneratorFactory loanScheduleFactory;
    private final FloatingRatesReadPlatformService floatingRatesReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;

    public ScheduleGeneratorDTO buildScheduleGeneratorDTO(final Loan loan, final LocalDate recalculateFrom) {
        final HolidayDetailDTO holidayDetailDTO = null;
        return buildScheduleGeneratorDTO(loan, recalculateFrom, holidayDetailDTO);
    }

    public ScheduleGeneratorDTO buildScheduleGeneratorDTO(final Loan loan, final LocalDate recalculateFrom,
            final HolidayDetailDTO holidayDetailDTO) {
        HolidayDetailDTO holidayDetails = holidayDetailDTO;
        if (holidayDetailDTO == null) {
            holidayDetails = constructHolidayDTO(loan);
        }
        final MonetaryCurrency currency = loan.getCurrency();
        ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        Calendar calendar = null;
        CalendarHistoryDataWrapper calendarHistoryDataWrapper = null;
        if (calendarInstance != null) {
            calendar = calendarInstance.getCalendar();
            Set<CalendarHistory> calendarHistory = calendar.getCalendarHistory();
            calendarHistoryDataWrapper = new CalendarHistoryDataWrapper(calendarHistory);
        }
        LocalDate calculatedRepaymentsStartingFromDate = this.getCalculatedRepaymentsStartingFromDate(loan.getDisbursementDate(), loan,
                calendarInstance, calendarHistoryDataWrapper);
        CalendarInstance restCalendarInstance = null;
        CalendarInstance compoundingCalendarInstance = null;
        Long overdurPenaltyWaitPeriod = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                    CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL.getValue());
            compoundingCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                    CalendarEntityType.LOAN_RECALCULATION_COMPOUNDING_DETAIL.getValue());
            overdurPenaltyWaitPeriod = this.configurationDomainService.retrievePenaltyWaitPeriod();
        }
        final Boolean isInterestChargedFromDateAsDisbursementDateEnabled = this.configurationDomainService
                .isInterestChargedFromDateSameAsDisbursementDate();
        FloatingRateDTO floatingRateDTO = constructFloatingRateDTO(loan);
        Boolean isSkipRepaymentOnFirstMonth = false;
        Integer numberOfDays = 0;
        boolean isSkipRepaymentOnFirstMonthEnabled = configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
        if (isSkipRepaymentOnFirstMonthEnabled) {
            isSkipRepaymentOnFirstMonth = isLoanRepaymentsSyncWithMeeting(loan.group(), calendar);
            if (isSkipRepaymentOnFirstMonth) {
                numberOfDays = configurationDomainService.retreivePeriodInNumberOfDaysForSkipMeetingDate().intValue();
            }
        }
        final Boolean isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled = this.configurationDomainService
                .isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled();

        boolean isFirstRepaymentDateAllowedOnHoliday = this.configurationDomainService
                .isFirstRepaymentDateAfterRescheduleAllowedOnHoliday();

        boolean isInterestToBeRecoveredFirstWhenGreaterThanEMI = this.configurationDomainService
                .isInterestToBeRecoveredFirstWhenGreaterThanEMI();
        boolean isPrincipalCompoundingDisabledForOverdueLoans = this.configurationDomainService
                .isPrincipalCompoundingDisabledForOverdueLoans();

        ScheduleGeneratorDTO scheduleGeneratorDTO = new ScheduleGeneratorDTO(loanScheduleFactory, applicationCurrency,
                calculatedRepaymentsStartingFromDate, holidayDetails, restCalendarInstance, compoundingCalendarInstance, recalculateFrom,
                overdurPenaltyWaitPeriod, floatingRateDTO, calendar, calendarHistoryDataWrapper,
                isInterestChargedFromDateAsDisbursementDateEnabled, numberOfDays, isSkipRepaymentOnFirstMonth,
                isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled, isFirstRepaymentDateAllowedOnHoliday,
                isInterestToBeRecoveredFirstWhenGreaterThanEMI, isPrincipalCompoundingDisabledForOverdueLoans);

        return scheduleGeneratorDTO;
    }

    public Boolean isLoanRepaymentsSyncWithMeeting(final Group group, final Calendar calendar) {
        Boolean isSkipRepaymentOnFirstMonth = false;
        Long entityId = null;
        Long entityTypeId = null;

        if (group != null) {
            if (group.getParent() != null) {
                entityId = group.getParent().getId();
                entityTypeId = CalendarEntityType.CENTERS.getValue().longValue();
            } else {
                entityId = group.getId();
                entityTypeId = CalendarEntityType.GROUPS.getValue().longValue();
            }
        }

        if (entityId == null || calendar == null) {
            return isSkipRepaymentOnFirstMonth;
        }
        isSkipRepaymentOnFirstMonth = this.calendarReadPlatformService.isCalendarAssociatedWithEntity(entityId, calendar.getId(),
                entityTypeId);
        return isSkipRepaymentOnFirstMonth;
    }

    public LocalDate getCalculatedRepaymentsStartingFromDate(final Loan loan) {
        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        final CalendarHistoryDataWrapper calendarHistoryDataWrapper = null;
        return this.getCalculatedRepaymentsStartingFromDate(loan.getDisbursementDate(), loan, calendarInstance, calendarHistoryDataWrapper);
    }

    private HolidayDetailDTO constructHolidayDTO(final Loan loan) {
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                loan.getDisbursementDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays, allowTransactionsOnHoliday,
                allowTransactionsOnNonWorkingDay);
        return holidayDetailDTO;
    }

    public HolidayDetailDTO constructHolidayDTO(final Long officeId, LocalDate localDate) {
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(officeId, localDate,
                HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        return new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays, allowTransactionsOnHoliday, allowTransactionsOnNonWorkingDay);
    }

    private FloatingRateDTO constructFloatingRateDTO(final Loan loan) {
        FloatingRateDTO floatingRateDTO = null;
        if (loan.loanProduct().isLinkedToFloatingInterestRate()) {
            boolean isFloatingInterestRate = loan.getIsFloatingInterestRate();
            BigDecimal interestRateDiff = loan.getInterestRateDifferential();
            List<FloatingRatePeriodData> baseLendingRatePeriods = null;
            try {
                baseLendingRatePeriods = this.floatingRatesReadPlatformService.retrieveBaseLendingRate().getRatePeriods();
            } catch (final FloatingRateNotFoundException ex) {
                // Do not do anything
            }

            floatingRateDTO = new FloatingRateDTO(isFloatingInterestRate, loan.getDisbursementDate(), interestRateDiff,
                    baseLendingRatePeriods);
        }
        return floatingRateDTO;
    }

    private LocalDate getCalculatedRepaymentsStartingFromDate(final LocalDate actualDisbursementDate, final Loan loan,
            final CalendarInstance calendarInstance, final CalendarHistoryDataWrapper calendarHistoryDataWrapper) {
        final Calendar calendar = calendarInstance == null ? null : calendarInstance.getCalendar();
        return calculateRepaymentStartingFromDate(actualDisbursementDate, loan, calendar, calendarHistoryDataWrapper);
    }

    public LocalDate getCalculatedRepaymentsStartingFromDate(final LocalDate actualDisbursementDate, final Loan loan,
            final Calendar calendar) {
        final CalendarHistoryDataWrapper calendarHistoryDataWrapper = null;
        if (calendar == null) {
            return getCalculatedRepaymentsStartingFromDate(loan);
        }
        return calculateRepaymentStartingFromDate(actualDisbursementDate, loan, calendar, calendarHistoryDataWrapper);

    }

    private LocalDate calculateRepaymentStartingFromDate(final LocalDate actualDisbursementDate, final Loan loan, final Calendar calendar,
            final CalendarHistoryDataWrapper calendarHistoryDataWrapper) {
        LocalDate calculatedRepaymentsStartingFromDate = loan.getExpectedFirstRepaymentOnDate();
        if (calendar != null) { // sync repayments

            if (calculatedRepaymentsStartingFromDate == null && !calendar.getCalendarHistory().isEmpty()
                    && calendarHistoryDataWrapper != null) {
                // generate the first repayment date based on calendar history
                calculatedRepaymentsStartingFromDate = generateCalculatedRepaymentStartDate(calendarHistoryDataWrapper,
                        actualDisbursementDate, loan);
                return calculatedRepaymentsStartingFromDate;
            }

            // TODO: AA - user provided first repayment date takes precedence
            // over recalculated meeting date
            if (calculatedRepaymentsStartingFromDate == null) {
                // FIXME: AA - Possibility of having next meeting date
                // immediately after disbursement date,
                // need to have minimum number of days gap between disbursement
                // and first repayment date.
                final LoanProductRelatedDetail repaymentScheduleDetails = loan.repaymentScheduleDetail();
                // Not expecting to be null
                if (repaymentScheduleDetails != null) {
                    final Integer repayEvery = repaymentScheduleDetails.getRepayEvery();
                    final String frequency = CalendarUtils
                            .getMeetingFrequencyFromPeriodFrequencyType(repaymentScheduleDetails.getRepaymentPeriodFrequencyType());
                    Boolean isSkipRepaymentOnFirstMonth = false;
                    Integer numberOfDays = 0;
                    boolean isSkipRepaymentOnFirstMonthEnabled = this.configurationDomainService
                            .isSkippingMeetingOnFirstDayOfMonthEnabled();
                    if (isSkipRepaymentOnFirstMonthEnabled) {
                        numberOfDays = configurationDomainService.retreivePeriodInNumberOfDaysForSkipMeetingDate().intValue();
                        isSkipRepaymentOnFirstMonth = isLoanRepaymentsSyncWithMeeting(loan.group(), calendar);
                    }
                    calculatedRepaymentsStartingFromDate = CalendarUtils.getFirstRepaymentMeetingDate(calendar, actualDisbursementDate,
                            repayEvery, frequency, isSkipRepaymentOnFirstMonth, numberOfDays);
                }
            }
        }
        return calculatedRepaymentsStartingFromDate;
    }

    private LocalDate generateCalculatedRepaymentStartDate(final CalendarHistoryDataWrapper calendarHistoryDataWrapper,
            LocalDate actualDisbursementDate, Loan loan) {
        final LoanProductRelatedDetail repaymentScheduleDetails = loan.repaymentScheduleDetail();
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        LocalDate calculatedRepaymentsStartingFromDate = null;

        List<CalendarHistory> historyList = calendarHistoryDataWrapper.getCalendarHistoryList();

        if (historyList != null && historyList.size() > 0) {
            if (repaymentScheduleDetails != null) {
                final Integer repayEvery = repaymentScheduleDetails.getRepayEvery();
                final String frequency = CalendarUtils
                        .getMeetingFrequencyFromPeriodFrequencyType(repaymentScheduleDetails.getRepaymentPeriodFrequencyType());
                Boolean isSkipRepaymentOnFirstMonth = false;
                Integer numberOfDays = 0;
                boolean isSkipRepaymentOnFirstMonthEnabled = this.configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
                if (isSkipRepaymentOnFirstMonthEnabled) {
                    numberOfDays = configurationDomainService.retreivePeriodInNumberOfDaysForSkipMeetingDate().intValue();
                    isSkipRepaymentOnFirstMonth = isLoanRepaymentsSyncWithMeeting(loan.group(), historyList.get(0).getCalendar());
                }
                calculatedRepaymentsStartingFromDate = CalendarUtils.getNextRepaymentMeetingDate(historyList.get(0).getRecurrence(),
                        historyList.get(0).getStartDate(), actualDisbursementDate, repayEvery, frequency, workingDays,
                        isSkipRepaymentOnFirstMonth, numberOfDays);
            }
        }
        return calculatedRepaymentsStartingFromDate;
    }

    public void validateRepaymentTransactionType(LoanTransactionType repaymentTransactionType) {
        if (!repaymentTransactionType.isRepaymentType()) {
            throw new PlatformServiceUnavailableException("error.msg.repaymentTransactionType.provided.not.a.repayment.type",
                    "Loan :" + repaymentTransactionType.getCode() + " Repayment Transaction Type provided is not a Repayment Type",
                    repaymentTransactionType.getCode());
        }
    }

}
