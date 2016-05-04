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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateDTO;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.apache.fineract.portfolio.floatingrates.exception.FloatingRateNotFoundException;
import org.apache.fineract.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargePaidByData;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.DefaultLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalcualtionAdditionalDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummary;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummaryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleHistory;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleHistoryRepository;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.RescheduleLoansApiConstants;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestDataValidator;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.DefaultLoanReschedulerFactory;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleModel;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleModelRepaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequestRepository;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.exception.LoanRescheduleRequestNotFoundException;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanRescheduleRequestWritePlatformServiceImpl implements LoanRescheduleRequestWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(LoanRescheduleRequestWritePlatformServiceImpl.class);

    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    private final PlatformSecurityContext platformSecurityContext;
    private final LoanRescheduleRequestDataValidator loanRescheduleRequestDataValidator;
    private final LoanRescheduleRequestRepository loanRescheduleRequestRepository;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final HolidayRepositoryWrapper holidayRepository;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final LoanRepaymentScheduleHistoryRepository loanRepaymentScheduleHistoryRepository;
    private final LoanScheduleHistoryWritePlatformService loanScheduleHistoryWritePlatformService;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final LoanRepository loanRepository;
    private final LoanAssembler loanAssembler;
    private final FloatingRatesReadPlatformService floatingRatesReadPlatformService;
    private final LoanUtilService loanUtilService;

    /**
     * LoanRescheduleRequestWritePlatformServiceImpl constructor
     * 
     * @return void
     **/
    @Autowired
    public LoanRescheduleRequestWritePlatformServiceImpl(LoanRepositoryWrapper loanRepositoryWrapper,
            CodeValueRepositoryWrapper codeValueRepositoryWrapper, PlatformSecurityContext platformSecurityContext,
            LoanRescheduleRequestDataValidator loanRescheduleRequestDataValidator,
            LoanRescheduleRequestRepository loanRescheduleRequestRepository,
            ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository, ConfigurationDomainService configurationDomainService,
            HolidayRepositoryWrapper holidayRepository, WorkingDaysRepositoryWrapper workingDaysRepository,
            LoanRepaymentScheduleHistoryRepository loanRepaymentScheduleHistoryRepository,
            final LoanScheduleHistoryWritePlatformService loanScheduleHistoryWritePlatformService,
            final CalendarInstanceRepository calendarInstanceRepository, final LoanChargeReadPlatformService loanChargeReadPlatformService,
            final LoanTransactionRepository loanTransactionRepository,
            final JournalEntryWritePlatformService journalEntryWritePlatformService, final LoanRepository loanRepository,
            final LoanAssembler loanAssembler, final FloatingRatesReadPlatformService floatingRatesReadPlatformService,
            final LoanUtilService loanUtilService) {
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.codeValueRepositoryWrapper = codeValueRepositoryWrapper;
        this.platformSecurityContext = platformSecurityContext;
        this.loanRescheduleRequestDataValidator = loanRescheduleRequestDataValidator;
        this.loanRescheduleRequestRepository = loanRescheduleRequestRepository;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.configurationDomainService = configurationDomainService;
        this.holidayRepository = holidayRepository;
        this.workingDaysRepository = workingDaysRepository;
        this.loanRepaymentScheduleHistoryRepository = loanRepaymentScheduleHistoryRepository;
        this.loanScheduleHistoryWritePlatformService = loanScheduleHistoryWritePlatformService;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.loanChargeReadPlatformService = loanChargeReadPlatformService;
        this.loanTransactionRepository = loanTransactionRepository;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.loanRepository = loanRepository;
        this.loanAssembler = loanAssembler;
        this.floatingRatesReadPlatformService = floatingRatesReadPlatformService;
        this.loanUtilService = loanUtilService;
    }

    /**
     * create a new instance of the LoanRescheduleRequest object from the
     * JsonCommand object and persist
     * 
     * @return CommandProcessingResult object
     **/
    @Override
    @Transactional
    public CommandProcessingResult create(JsonCommand jsonCommand) {

        try {
            // get the loan id from the JsonCommand object
            final Long loanId = jsonCommand.longValueOfParameterNamed(RescheduleLoansApiConstants.loanIdParamName);

            // use the loan id to get a Loan entity object
            final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);

            // validate the request in the JsonCommand object passed as
            // parameter
            this.loanRescheduleRequestDataValidator.validateForCreateAction(jsonCommand, loan);

            // get the reschedule reason code value id from the JsonCommand
            // object
            final Long rescheduleReasonId = jsonCommand.longValueOfParameterNamed(RescheduleLoansApiConstants.rescheduleReasonIdParamName);

            // use the reschedule reason code value id to get a CodeValue entity
            // object
            final CodeValue rescheduleReasonCodeValue = this.codeValueRepositoryWrapper.findOneWithNotFoundDetection(rescheduleReasonId);

            // get the grace on principal integer value from the JsonCommand
            // object
            final Integer graceOnPrincipal = jsonCommand
                    .integerValueOfParameterNamed(RescheduleLoansApiConstants.graceOnPrincipalParamName);

            // get the grace on interest integer value from the JsonCommand
            // object
            final Integer graceOnInterest = jsonCommand.integerValueOfParameterNamed(RescheduleLoansApiConstants.graceOnInterestParamName);

            // get the extra terms to be added at the end of the new schedule
            // from the JsonCommand object
            final Integer extraTerms = jsonCommand.integerValueOfParameterNamed(RescheduleLoansApiConstants.extraTermsParamName);

            // get the new interest rate that would be applied to the new loan
            // schedule
            final BigDecimal interestRate = jsonCommand
                    .bigDecimalValueOfParameterNamed(RescheduleLoansApiConstants.newInterestRateParamName);

            // get the reschedule reason comment text from the JsonCommand
            // object
            final String rescheduleReasonComment = jsonCommand
                    .stringValueOfParameterNamed(RescheduleLoansApiConstants.rescheduleReasonCommentParamName);

            // get the recalculate interest option
            final Boolean recalculateInterest = jsonCommand
                    .booleanObjectValueOfParameterNamed(RescheduleLoansApiConstants.recalculateInterestParamName);

            // initialize set the value to null
            Date submittedOnDate = null;

            // check if the parameter is in the JsonCommand object
            if (jsonCommand.hasParameter(RescheduleLoansApiConstants.submittedOnDateParamName)) {
                // create a LocalDate object from the "submittedOnDate" Date
                // string
                LocalDate localDate = jsonCommand.localDateValueOfParameterNamed(RescheduleLoansApiConstants.submittedOnDateParamName);

                if (localDate != null) {
                    // update the value of the "submittedOnDate" variable
                    submittedOnDate = localDate.toDate();
                }
            }

            // initially set the value to null
            Date rescheduleFromDate = null;

            // start point of the rescheduling exercise
            Integer rescheduleFromInstallment = null;

            // initially set the value to null
            Date adjustedDueDate = null;

            // check if the parameter is in the JsonCommand object
            if (jsonCommand.hasParameter(RescheduleLoansApiConstants.rescheduleFromDateParamName)) {
                // create a LocalDate object from the "rescheduleFromDate" Date
                // string
                LocalDate localDate = jsonCommand.localDateValueOfParameterNamed(RescheduleLoansApiConstants.rescheduleFromDateParamName);

                if (localDate != null) {
                    // get installment by due date
                    LoanRepaymentScheduleInstallment installment = loan.getRepaymentScheduleInstallment(localDate);
                    rescheduleFromInstallment = installment.getInstallmentNumber();

                    // update the value of the "rescheduleFromDate" variable
                    rescheduleFromDate = localDate.toDate();
                }
            }

            if (jsonCommand.hasParameter(RescheduleLoansApiConstants.adjustedDueDateParamName)) {
                // create a LocalDate object from the "adjustedDueDate" Date
                // string
                LocalDate localDate = jsonCommand.localDateValueOfParameterNamed(RescheduleLoansApiConstants.adjustedDueDateParamName);

                if (localDate != null) {
                    // update the value of the "adjustedDueDate"variable
                    adjustedDueDate = localDate.toDate();
                }
            }

            final LoanRescheduleRequest loanRescheduleRequest = LoanRescheduleRequest.instance(loan,
                    LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(), rescheduleFromInstallment, graceOnPrincipal, graceOnInterest,
                    rescheduleFromDate, adjustedDueDate, extraTerms, recalculateInterest, interestRate, rescheduleReasonCodeValue,
                    rescheduleReasonComment, submittedOnDate, this.platformSecurityContext.authenticatedUser(), null, null, null, null);

            // create a new entry in the m_loan_reschedule_request table
            this.loanRescheduleRequestRepository.save(loanRescheduleRequest);

            return new CommandProcessingResultBuilder().withCommandId(jsonCommand.commandId()).withEntityId(loanRescheduleRequest.getId())
                    .withLoanId(loan.getId()).build();
        }

        catch (final DataIntegrityViolationException dve) {
            // handle the data integrity violation
            handleDataIntegrityViolation(dve);

            // return an empty command processing result object
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult approve(JsonCommand jsonCommand) {

        try {
            final Long loanRescheduleRequestId = jsonCommand.entityId();

            final LoanRescheduleRequest loanRescheduleRequest = this.loanRescheduleRequestRepository.findOne(loanRescheduleRequestId);

            if (loanRescheduleRequest == null) { throw new LoanRescheduleRequestNotFoundException(loanRescheduleRequestId); }

            // validate the request in the JsonCommand object passed as
            // parameter
            this.loanRescheduleRequestDataValidator.validateForApproveAction(jsonCommand, loanRescheduleRequest);

            final AppUser appUser = this.platformSecurityContext.authenticatedUser();
            final Map<String, Object> changes = new LinkedHashMap<>();

            LocalDate approvedOnDate = jsonCommand.localDateValueOfParameterNamed("approvedOnDate");
            final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(jsonCommand.dateFormat()).withLocale(
                    jsonCommand.extractLocale());

            changes.put("locale", jsonCommand.locale());
            changes.put("dateFormat", jsonCommand.dateFormat());
            changes.put("approvedOnDate", approvedOnDate.toString(dateTimeFormatter));
            changes.put("approvedByUserId", appUser.getId());

            if (!changes.isEmpty()) {
                Loan loan = loanRescheduleRequest.getLoan();
                final LoanSummary loanSummary = loan.getSummary();

                final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
                final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan
                        .getDisbursementDate().toDate());
                final WorkingDays workingDays = this.workingDaysRepository.findOne();
                final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail = loan.getLoanRepaymentScheduleDetail();
                final MonetaryCurrency currency = loanProductRelatedDetail.getCurrency();
                final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

                final InterestMethod interestMethod = loan.getLoanRepaymentScheduleDetail().getInterestMethod();
                final RoundingMode roundingMode = MoneyHelper.getRoundingMode();
                final MathContext mathContext = new MathContext(8, roundingMode);

                Collection<LoanRepaymentScheduleHistory> loanRepaymentScheduleHistoryList = this.loanScheduleHistoryWritePlatformService
                        .createLoanScheduleArchive(loan.getRepaymentScheduleInstallments(), loan, loanRescheduleRequest);

                HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
                CalendarInstance restCalendarInstance = null;
                CalendarInstance compoundingCalendarInstance = null;
                if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                    restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(
                            loan.loanInterestRecalculationDetailId(), CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL.getValue());
                    compoundingCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(
                            loan.loanInterestRecalculationDetailId(), CalendarEntityType.LOAN_RECALCULATION_COMPOUNDING_DETAIL.getValue());
                }
                final CalendarInstance loanCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                        CalendarEntityType.LOANS.getValue());
                Calendar loanCalendar = null;
                if (loanCalendarInstance != null) {
                    loanCalendar = loanCalendarInstance.getCalendar();
                }
                FloatingRateDTO floatingRateDTO = constructFloatingRateDTO(loan);
                Boolean isSkipRepaymentOnFirstMonth = false;
                Integer numberOfDays = 0;
                boolean isSkipRepaymentOnFirstMonthEnabled = this.configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
                if(isSkipRepaymentOnFirstMonthEnabled){
                    isSkipRepaymentOnFirstMonth = this.loanUtilService.isLoanRepaymentsSyncWithMeeting(loan.group(), loanCalendar);
                    if(isSkipRepaymentOnFirstMonth) { numberOfDays = configurationDomainService.retreivePeroidInNumberOfDaysForSkipMeetingDate().intValue(); }
                    
                }
                LoanRescheduleModel loanRescheduleModel = new DefaultLoanReschedulerFactory().reschedule(mathContext, interestMethod,
                        loanRescheduleRequest, applicationCurrency, holidayDetailDTO, restCalendarInstance, compoundingCalendarInstance,
                        loanCalendar, floatingRateDTO, isSkipRepaymentOnFirstMonth, numberOfDays);

                final Collection<LoanRescheduleModelRepaymentPeriod> periods = loanRescheduleModel.getPeriods();
                List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = loan.getRepaymentScheduleInstallments();
                Collection<LoanCharge> waiveLoanCharges = new ArrayList<>();
                final List<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails = null;
                for (LoanRescheduleModelRepaymentPeriod period : periods) {

                    if (period.isNew()) {
                        LoanRepaymentScheduleInstallment repaymentScheduleInstallment = new LoanRepaymentScheduleInstallment(loan,
                                period.periodNumber(), period.periodFromDate(), period.periodDueDate(), period.principalDue(),
                                period.interestDue(), BigDecimal.ZERO, BigDecimal.ZERO, false, compoundingDetails);

                        repaymentScheduleInstallments.add(repaymentScheduleInstallment);
                    }

                    else {
                        for (LoanRepaymentScheduleInstallment repaymentScheduleInstallment : repaymentScheduleInstallments) {

                            if (repaymentScheduleInstallment.getInstallmentNumber().equals(period.oldPeriodNumber())) {

                                LocalDate periodDueDate = repaymentScheduleInstallment.getDueDate();
                                Money zeroAmount = Money.of(currency, new BigDecimal(0));

                                repaymentScheduleInstallment.updateInstallmentNumber(period.periodNumber());
                                repaymentScheduleInstallment.updateFromDate(period.periodFromDate());
                                repaymentScheduleInstallment.updateDueDate(period.periodDueDate());
                                repaymentScheduleInstallment.updatePrincipal(period.principalDue());
                                repaymentScheduleInstallment.updateInterestCharged(period.interestDue());

                                if (Money.of(currency, period.principalDue()).isZero() && Money.of(currency, period.interestDue()).isZero()
                                        && repaymentScheduleInstallment.isNotFullyPaidOff()) {

                                    if (repaymentScheduleInstallment.getPenaltyChargesOutstanding(currency).isGreaterThan(zeroAmount)
                                            || repaymentScheduleInstallment.getFeeChargesOutstanding(currency).isGreaterThan(zeroAmount)) {

                                        waiveLoanCharges.addAll(loan.getLoanCharges(periodDueDate));
                                    }
                                }

                                break;
                            }
                        }
                    }
                }

                for (LoanRepaymentScheduleHistory loanRepaymentScheduleHistory : loanRepaymentScheduleHistoryList) {
                    this.loanRepaymentScheduleHistoryRepository.save(loanRepaymentScheduleHistory);
                }

                loan.updateRescheduledByUser(appUser);
                loan.updateRescheduledOnDate(new LocalDate());

                // waive all loan charges of zero instalments
                waiveLoanCharges(loan, waiveLoanCharges);

                // update the Loan summary
                loanSummary
                        .updateSummary(currency, loan.getPrincpal(), repaymentScheduleInstallments, new LoanSummaryWrapper(), true, null);

                // update the total number of schedule repayments
                loan.updateNumberOfRepayments(periods.size());

                // update the loan term frequency (loan term frequency = number
                // of repayments)
                loan.updateTermFrequency(periods.size());

                // update the status of the request
                loanRescheduleRequest.approve(appUser, approvedOnDate);

                // update the derived fields of each loan repayments schedule
                // instalments
                for (final LoanRepaymentScheduleInstallment repaymentScheduleInstallment : repaymentScheduleInstallments) {
                    repaymentScheduleInstallment.updateDerivedFields(currency, new LocalDate());
                }

                // updates maturity date
                loan.updateLoanScheduleDependentDerivedFields();
                // update the loan object
                this.loanRepository.save(loan);
            }

            return new CommandProcessingResultBuilder().withCommandId(jsonCommand.commandId()).withEntityId(loanRescheduleRequestId)
                    .withLoanId(loanRescheduleRequest.getLoan().getId()).with(changes).build();
        }

        catch (final DataIntegrityViolationException dve) {
            // handle the data integrity violation
            handleDataIntegrityViolation(dve);

            // return an empty command processing result object
            return CommandProcessingResult.empty();
        }
    }

    /**
     * waive all charges in the collection
     * 
     * @param loan
     *            Loan object
     * @param loanCharges
     *            collection of LoanCharge objects
     * @return void
     **/
    private void waiveLoanCharges(Loan loan, Collection<LoanCharge> loanCharges) {
        AppUser currentUser = this.platformSecurityContext.authenticatedUser();
        this.loanAssembler.setHelpers(loan);

        for (LoanCharge loanCharge : loanCharges) {

            if (loanCharge.isChargePending()) {
                Integer loanInstallmentNumber = null;

                if (loanCharge.isInstalmentFee()) {
                    LoanInstallmentCharge chargePerInstallment = loanCharge.getUnpaidInstallmentLoanCharge();

                    if (chargePerInstallment != null) {
                        loanInstallmentNumber = chargePerInstallment.getRepaymentInstallment().getInstallmentNumber();
                    }
                }

                final Map<String, Object> changes = new LinkedHashMap<>(3);

                final List<Long> existingTransactionIds = new ArrayList<>();
                final List<Long> existingReversedTransactionIds = new ArrayList<>();
                LocalDate recalculateFrom = null;
                if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                    recalculateFrom = DateUtils.getLocalDateOfTenant();
                }

                ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

                Money accruedCharge = Money.zero(loan.getCurrency());
                if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
                    Collection<LoanChargePaidByData> chargePaidByDatas = this.loanChargeReadPlatformService.retriveLoanChargesPaidBy(
                            loanCharge.getId(), LoanTransactionType.ACCRUAL, loanInstallmentNumber);
                    for (LoanChargePaidByData chargePaidByData : chargePaidByDatas) {
                        accruedCharge = accruedCharge.plus(chargePaidByData.getAmount());
                    }
                }

                final LoanTransaction loanTransaction = loan.waiveLoanCharge(loanCharge, defaultLoanLifecycleStateMachine(), changes,
                        existingTransactionIds, existingReversedTransactionIds, loanInstallmentNumber, scheduleGeneratorDTO, accruedCharge,
                        currentUser);

                this.loanTransactionRepository.save(loanTransaction);

                postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
            }
        }
    }

    private void postJournalEntries(Loan loan, List<Long> existingTransactionIds, List<Long> existingReversedTransactionIds) {
        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        boolean isAccountTransfer = false;
        final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(applicationCurrency.toData(),
                existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        final List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
    }

    @Override
    @Transactional
    public CommandProcessingResult reject(JsonCommand jsonCommand) {

        try {
            final Long loanRescheduleRequestId = jsonCommand.entityId();

            final LoanRescheduleRequest loanRescheduleRequest = loanRescheduleRequestRepository.findOne(loanRescheduleRequestId);

            if (loanRescheduleRequest == null) { throw new LoanRescheduleRequestNotFoundException(loanRescheduleRequestId); }

            // validate the request in the JsonCommand object passed as
            // parameter
            this.loanRescheduleRequestDataValidator.validateForRejectAction(jsonCommand, loanRescheduleRequest);

            final AppUser appUser = this.platformSecurityContext.authenticatedUser();
            final Map<String, Object> changes = new LinkedHashMap<>();

            LocalDate rejectedOnDate = jsonCommand.localDateValueOfParameterNamed("rejectedOnDate");
            final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(jsonCommand.dateFormat()).withLocale(
                    jsonCommand.extractLocale());

            changes.put("locale", jsonCommand.locale());
            changes.put("dateFormat", jsonCommand.dateFormat());
            changes.put("rejectedOnDate", rejectedOnDate.toString(dateTimeFormatter));
            changes.put("rejectedByUserId", appUser.getId());

            if (!changes.isEmpty()) {
                loanRescheduleRequest.reject(appUser, rejectedOnDate);
            }

            return new CommandProcessingResultBuilder().withCommandId(jsonCommand.commandId()).withEntityId(loanRescheduleRequestId)
                    .withLoanId(loanRescheduleRequest.getLoan().getId()).with(changes).build();
        }

        catch (final DataIntegrityViolationException dve) {
            // handle the data integrity violation
            handleDataIntegrityViolation(dve);

            // return an empty command processing result object
            return CommandProcessingResult.empty();
        }
    }

    /**
     * handles the data integrity violation exception for loan reschedule write
     * services
     *
     * @param dve
     *            data integrity violation exception
     * @return void
     **/
    private void handleDataIntegrityViolation(final DataIntegrityViolationException dve) {

        logger.error(dve.getMessage(), dve);

        throw new PlatformDataIntegrityException("error.msg.loan.reschedule.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
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
}
