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
package org.apache.fineract.portfolio.loanaccount.loanschedule.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.domain.HolidayRepository;
import org.apache.fineract.organisation.holiday.domain.HolidayStatusType;
import org.apache.fineract.organisation.holiday.service.HolidayUtil;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.organisation.workingdays.service.WorkingDaysUtil;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarType;
import org.apache.fineract.portfolio.calendar.exception.CalendarNotFoundException;
import org.apache.fineract.portfolio.calendar.exception.MeetingFrequencyMismatchException;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.common.domain.DayOfWeekType;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.NthDayType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateDTO;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.apache.fineract.portfolio.floatingrates.exception.FloatingRateNotFoundException;
import org.apache.fineract.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariations;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.exception.LoanApplicationDateException;
import org.apache.fineract.portfolio.loanaccount.exception.MinDaysBetweenDisbursalAndFirstRepaymentViolationException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleParams;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.serialization.VariableLoanScheduleFromApiJsonValidator;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductInterestRecalculationDetails;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductVariableInstallmentConfig;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class LoanScheduleAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final LoanProductRepository loanProductRepository;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final LoanChargeAssembler loanChargeAssembler;
    private final LoanScheduleGeneratorFactory loanScheduleFactory;
    private final AprCalculator aprCalculator;
    private final CalendarRepository calendarRepository;
    private final HolidayRepository holidayRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepositoryWrapper groupRepository;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final FloatingRatesReadPlatformService floatingRatesReadPlatformService;
    private final VariableLoanScheduleFromApiJsonValidator variableLoanScheduleFromApiJsonValidator;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final PlatformSecurityContext context;
    private final LoanUtilService loanUtilService;

    @Autowired
    public LoanScheduleAssembler(final FromJsonHelper fromApiJsonHelper, final LoanProductRepository loanProductRepository,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            final LoanScheduleGeneratorFactory loanScheduleFactory, final AprCalculator aprCalculator,
            final LoanChargeAssembler loanChargeAssembler, final CalendarRepository calendarRepository,
            final HolidayRepository holidayRepository, final ConfigurationDomainService configurationDomainService,
            final ClientRepositoryWrapper clientRepository, final GroupRepositoryWrapper groupRepository,
            final WorkingDaysRepositoryWrapper workingDaysRepository,
            final FloatingRatesReadPlatformService floatingRatesReadPlatformService,
            final VariableLoanScheduleFromApiJsonValidator variableLoanScheduleFromApiJsonValidator,
            final CalendarInstanceRepository calendarInstanceRepository, final PlatformSecurityContext context,
            final LoanUtilService loanUtilService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.loanProductRepository = loanProductRepository;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.loanScheduleFactory = loanScheduleFactory;
        this.aprCalculator = aprCalculator;
        this.loanChargeAssembler = loanChargeAssembler;
        this.calendarRepository = calendarRepository;
        this.holidayRepository = holidayRepository;
        this.configurationDomainService = configurationDomainService;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.workingDaysRepository = workingDaysRepository;
        this.floatingRatesReadPlatformService = floatingRatesReadPlatformService;
        this.variableLoanScheduleFromApiJsonValidator = variableLoanScheduleFromApiJsonValidator;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.context = context;
        this.loanUtilService = loanUtilService;
    }

    public LoanApplicationTerms assembleLoanTerms(final JsonElement element) {
        final Long loanProductId = this.fromApiJsonHelper.extractLongNamed("productId", element);

        final LoanProduct loanProduct = this.loanProductRepository.findOne(loanProductId);
        if (loanProduct == null) { throw new LoanProductNotFoundException(loanProductId); }

        return assembleLoanApplicationTermsFrom(element, loanProduct);
    }

    private LoanApplicationTerms assembleLoanApplicationTermsFrom(final JsonElement element, final LoanProduct loanProduct) {

        final MonetaryCurrency currency = loanProduct.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        // loan terms
        final Integer loanTermFrequency = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("loanTermFrequency", element);
        final Integer loanTermFrequencyType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("loanTermFrequencyType", element);
        final PeriodFrequencyType loanTermPeriodFrequencyType = PeriodFrequencyType.fromInt(loanTermFrequencyType);

        final Integer numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("numberOfRepayments", element);
        final Integer repaymentEvery = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentEvery", element);
        final Integer repaymentFrequencyType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentFrequencyType", element);
        final PeriodFrequencyType repaymentPeriodFrequencyType = PeriodFrequencyType.fromInt(repaymentFrequencyType);
        final Integer nthDay = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentFrequencyNthDayType", element);
        final Integer dayOfWeek = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentFrequencyDayOfWeekType", element);
        final DayOfWeekType weekDayType = DayOfWeekType.fromInt(dayOfWeek);

        final Integer amortizationType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("amortizationType", element);
        final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(amortizationType);
        
        boolean isEqualAmortization = false;
        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.isEqualAmortizationParam, element)) {
            isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isEqualAmortizationParam, element);
        }

        // interest terms
        final Integer interestType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("interestType", element);
        final InterestMethod interestMethod = InterestMethod.fromInt(interestType);

        final Integer interestCalculationPeriodType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("interestCalculationPeriodType",
                element);
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod
                .fromInt(interestCalculationPeriodType);
        Boolean allowPartialPeriodInterestCalcualtion = this.fromApiJsonHelper.extractBooleanNamed(
                LoanProductConstants.allowPartialPeriodInterestCalcualtionParamName, element);
        if (allowPartialPeriodInterestCalcualtion == null) {
            allowPartialPeriodInterestCalcualtion = loanProduct.getLoanProductRelatedDetail().isAllowPartialPeriodInterestCalcualtion();
        }

        final BigDecimal interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRatePerPeriod", element);
        final PeriodFrequencyType interestRatePeriodFrequencyType = loanProduct.getInterestPeriodFrequencyType();

        BigDecimal annualNominalInterestRate = BigDecimal.ZERO;
        if (interestRatePerPeriod != null) {
       annualNominalInterestRate = this.aprCalculator.calculateFrom(interestRatePeriodFrequencyType, interestRatePerPeriod, numberOfRepayments, repaymentEvery, repaymentPeriodFrequencyType);
        }

        // disbursement details
        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        final Money principalMoney = Money.of(currency, principal);

        final LocalDate expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed("expectedDisbursementDate", element);
        final LocalDate repaymentsStartingFromDate = this.fromApiJsonHelper.extractLocalDateNamed("repaymentsStartingFromDate", element);
        LocalDate calculatedRepaymentsStartingFromDate = repaymentsStartingFromDate;

        final Boolean synchDisbursement = this.fromApiJsonHelper.extractBooleanNamed("syncDisbursementWithMeeting", element);
        final Long calendarId = this.fromApiJsonHelper.extractLongNamed("calendarId", element);
        Calendar calendar = null;

        final String loanTypeParameterName = "loanType";
        final String loanTypeStr = this.fromApiJsonHelper.extractStringNamed(loanTypeParameterName, element);

        final AccountType loanType = AccountType.fromName(loanTypeStr);

        /*
         * If it is JLG loan/Group Loan then make sure loan frequency is same as
         * Group/Center meeting frequency or multiple of it. TODO: Check should
         * be either same frequency or loan freq is multiple of center/group
         * meeting freq multiples
         */
        if ((loanType.isJLGAccount() || loanType.isGroupAccount()) && calendarId != null) {
            calendar = this.calendarRepository.findOne(calendarId);
            if (calendar == null) { throw new CalendarNotFoundException(calendarId); }
            final PeriodFrequencyType meetingPeriodFrequency = CalendarUtils.getMeetingPeriodFrequencyType(calendar.getRecurrence());
            validateRepaymentFrequencyIsSameAsMeetingFrequency(meetingPeriodFrequency.getValue(), repaymentFrequencyType,
                    CalendarUtils.getInterval(calendar.getRecurrence()), repaymentEvery);
        } else {
            if (repaymentPeriodFrequencyType == PeriodFrequencyType.MONTHS && nthDay != null && nthDay != NthDayType.INVALID.getValue()) {
                LocalDate calendarStartDate = repaymentsStartingFromDate;
                if (calendarStartDate == null) calendarStartDate = expectedDisbursementDate;
                calendar = createLoanCalendar(calendarStartDate, repaymentEvery, CalendarFrequencyType.MONTHLY, dayOfWeek, nthDay);
            }
        }

        /*
         * If user has not passed the first repayments date then then derive the
         * same based on loan type.
         */
        if (calculatedRepaymentsStartingFromDate == null) {
            calculatedRepaymentsStartingFromDate = deriveFirstRepaymentDate(loanType, repaymentEvery, expectedDisbursementDate,
                    repaymentPeriodFrequencyType, loanProduct.getMinimumDaysBetweenDisbursalAndFirstRepayment(), calendar);
        }

        /*
         * If it is JLG loan/Group Loan synched with a meeting, then make sure
         * first repayment falls on meeting date
         */
        final Long groupId = this.fromApiJsonHelper.extractLongNamed("groupId", element);
        Group group = null;
        if(groupId != null){
            group = this.groupRepository.findOneWithNotFoundDetection(groupId);
        }
    
        Boolean isSkipMeetingOnFirstDay = false;
        Integer numberOfDays = 0;
        boolean isSkipRepaymentOnFirstMonthEnabled = configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
        if(isSkipRepaymentOnFirstMonthEnabled){
            isSkipMeetingOnFirstDay = this.loanUtilService.isLoanRepaymentsSyncWithMeeting(group, calendar);
            if(isSkipMeetingOnFirstDay) { numberOfDays = configurationDomainService.retreivePeroidInNumberOfDaysForSkipMeetingDate().intValue(); }  
        }
        if ((loanType.isJLGAccount() || loanType.isGroupAccount()) && calendar != null) {
            validateRepaymentsStartDateWithMeetingDates(calculatedRepaymentsStartingFromDate, calendar, isSkipMeetingOnFirstDay,
                    numberOfDays);

            /*
             * If disbursement is synced on meeting, make sure disbursement date
             * is on a meeting date
             */
            if (synchDisbursement != null && synchDisbursement.booleanValue()) {
                validateDisbursementDateWithMeetingDates(expectedDisbursementDate, calendar, isSkipMeetingOnFirstDay, numberOfDays);
            }
        }

        validateMinimumDaysBetweenDisbursalAndFirstRepayment(expectedDisbursementDate, calculatedRepaymentsStartingFromDate,
                loanProduct.getMinimumDaysBetweenDisbursalAndFirstRepayment());

        // grace details
        final Integer graceOnPrincipalPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnPrincipalPayment", element);
        final Integer recurringMoratoriumOnPrincipalPeriods = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("recurringMoratoriumOnPrincipalPeriods", element);
        final Integer graceOnInterestPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestPayment", element);
        final Integer graceOnInterestCharged = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestCharged", element);
        final LocalDate interestChargedFromDate = this.fromApiJsonHelper.extractLocalDateNamed("interestChargedFromDate", element);
        final Boolean isInterestChargedFromDateSameAsDisbursalDateEnabled = this.configurationDomainService
                .isInterestChargedFromDateSameAsDisbursementDate();

        final Integer graceOnArrearsAgeing = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(
                LoanProductConstants.graceOnArrearsAgeingParameterName, element);

        // other
        final BigDecimal inArrearsTolerance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("inArrearsTolerance", element);
        final Money inArrearsToleranceMoney = Money.of(currency, inArrearsTolerance);

        final BigDecimal emiAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.emiAmountParameterName,
                element);
        final BigDecimal maxOutstandingBalance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                LoanApiConstants.maxOutstandingBalanceParameterName, element);

        final List<DisbursementData> disbursementDatas = fetchDisbursementData(element.getAsJsonObject());

        /**
         * Interest recalculation settings copy from product definition
         */
        final DaysInMonthType daysInMonthType = loanProduct.fetchDaysInMonthType();

        final DaysInYearType daysInYearType = loanProduct.fetchDaysInYearType();

        final boolean isInterestRecalculationEnabled = loanProduct.isInterestRecalculationEnabled();
        RecalculationFrequencyType recalculationFrequencyType = null;
        CalendarInstance restCalendarInstance = null;
        RecalculationFrequencyType compoundingFrequencyType = null;
        CalendarInstance compoundingCalendarInstance = null;
        InterestRecalculationCompoundingMethod compoundingMethod = null;
        boolean allowCompoundingOnEod = false;
        if (isInterestRecalculationEnabled) {
            LoanProductInterestRecalculationDetails loanProductInterestRecalculationDetails = loanProduct
                    .getProductInterestRecalculationDetails();
            recalculationFrequencyType = loanProductInterestRecalculationDetails.getRestFrequencyType();
            Integer repeatsOnDay = null;
            Integer recalculationFrequencyNthDay = loanProductInterestRecalculationDetails.getRestFrequencyOnDay();
            if (recalculationFrequencyNthDay == null) {
                recalculationFrequencyNthDay = loanProductInterestRecalculationDetails.getRestFrequencyNthDay();
                repeatsOnDay = loanProductInterestRecalculationDetails.getRestFrequencyWeekday();
            }
            Integer frequency = loanProductInterestRecalculationDetails.getRestInterval();
            if (recalculationFrequencyType.isSameAsRepayment()) {
                restCalendarInstance = createCalendarForSameAsRepayment(repaymentEvery, repaymentPeriodFrequencyType,
                        expectedDisbursementDate);
            } else {
                LocalDate calendarStartDate = expectedDisbursementDate;
                restCalendarInstance = createInterestRecalculationCalendarInstance(calendarStartDate, recalculationFrequencyType,
                        frequency, recalculationFrequencyNthDay, repeatsOnDay);
            }
            compoundingMethod = InterestRecalculationCompoundingMethod.fromInt(loanProductInterestRecalculationDetails
                    .getInterestRecalculationCompoundingMethod());
            if (compoundingMethod.isCompoundingEnabled()) {
                Integer compoundingRepeatsOnDay = null;
                Integer recalculationCompoundingFrequencyNthDay = loanProductInterestRecalculationDetails.getCompoundingFrequencyOnDay();
                if (recalculationCompoundingFrequencyNthDay == null) {
                    recalculationCompoundingFrequencyNthDay = loanProductInterestRecalculationDetails.getCompoundingFrequencyNthDay();
                    compoundingRepeatsOnDay = loanProductInterestRecalculationDetails.getCompoundingFrequencyWeekday();
                }
                compoundingFrequencyType = loanProductInterestRecalculationDetails.getCompoundingFrequencyType();
                if (compoundingFrequencyType.isSameAsRepayment()) {
                    compoundingCalendarInstance = createCalendarForSameAsRepayment(repaymentEvery, repaymentPeriodFrequencyType,
                            expectedDisbursementDate);
                } else {
                    LocalDate calendarStartDate = expectedDisbursementDate;
                    compoundingCalendarInstance = createInterestRecalculationCalendarInstance(calendarStartDate, compoundingFrequencyType,
                            loanProductInterestRecalculationDetails.getCompoundingInterval(), recalculationCompoundingFrequencyNthDay,
                            compoundingRepeatsOnDay);
                }
                allowCompoundingOnEod = loanProductInterestRecalculationDetails.allowCompoundingOnEod();
            }
        }

        final BigDecimal principalThresholdForLastInstalment = loanProduct.getPrincipalThresholdForLastInstallment();

        final Integer installmentAmountInMultiplesOf = loanProduct.getInstallmentAmountInMultiplesOf();

        List<LoanTermVariationsData> loanTermVariations = new ArrayList<>();
        if (loanProduct.isLinkedToFloatingInterestRate()) {
            final BigDecimal interestRateDiff = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    LoanApiConstants.interestRateDifferentialParameterName, element);
            final Boolean isFloatingInterestRate = this.fromApiJsonHelper.extractBooleanNamed(
                    LoanApiConstants.isFloatingInterestRateParameterName, element);
            List<FloatingRatePeriodData> baseLendingRatePeriods = null;
            try {
                baseLendingRatePeriods = this.floatingRatesReadPlatformService.retrieveBaseLendingRate().getRatePeriods();
            } catch (final FloatingRateNotFoundException ex) {
                // Do not do anything
            }
            FloatingRateDTO floatingRateDTO = new FloatingRateDTO(isFloatingInterestRate, expectedDisbursementDate, interestRateDiff,
                    baseLendingRatePeriods);
            Collection<FloatingRatePeriodData> applicableRates = loanProduct.fetchInterestRates(floatingRateDTO);

            LocalDate interestRateStartDate = DateUtils.getLocalDateOfTenant();
            final LocalDate dateValue = null;
            final boolean isSpecificToInstallment = false;
            for (FloatingRatePeriodData periodData : applicableRates) {
                LoanTermVariationsData loanTermVariation = new LoanTermVariationsData(
                        LoanEnumerations.loanvariationType(LoanTermVariationType.INTEREST_RATE), periodData.getFromDateAsLocalDate(),
                        periodData.getInterestRate(), dateValue, isSpecificToInstallment);
                if (!interestRateStartDate.isBefore(periodData.getFromDateAsLocalDate())) {
                    interestRateStartDate = periodData.getFromDateAsLocalDate();
                    annualNominalInterestRate = periodData.getInterestRate();
                }
                loanTermVariations.add(loanTermVariation);
            }
        }

        final Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
        Client client = null;
        Long officeId = null;
        if (clientId != null) {
            client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            officeId = client.getOffice().getId();
        } else if (groupId != null) {
            group = this.groupRepository.findOneWithNotFoundDetection(groupId);
            officeId = group.getOffice().getId();
        }
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(officeId, expectedDisbursementDate.toDate(),
                HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        HolidayDetailDTO detailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
        
        
        return LoanApplicationTerms.assembleFrom(applicationCurrency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentPeriodFrequencyType, nthDay, weekDayType, amortizationMethod, interestMethod,
                interestRatePerPeriod, interestRatePeriodFrequencyType, annualNominalInterestRate, interestCalculationPeriodMethod,
                allowPartialPeriodInterestCalcualtion, principalMoney, expectedDisbursementDate, repaymentsStartingFromDate,
                calculatedRepaymentsStartingFromDate, graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged,
                interestChargedFromDate, inArrearsToleranceMoney, loanProduct.isMultiDisburseLoan(), emiAmount, disbursementDatas,
                maxOutstandingBalance, graceOnArrearsAgeing, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                recalculationFrequencyType, restCalendarInstance, compoundingMethod, compoundingCalendarInstance, compoundingFrequencyType,
                principalThresholdForLastInstalment, installmentAmountInMultiplesOf, loanProduct.preCloseInterestCalculationStrategy(),
                calendar, BigDecimal.ZERO, loanTermVariations, isInterestChargedFromDateSameAsDisbursalDateEnabled,numberOfDays, isSkipMeetingOnFirstDay, detailDTO,
                allowCompoundingOnEod, isEqualAmortization);
}

    private CalendarInstance createCalendarForSameAsRepayment(final Integer repaymentEvery,
            final PeriodFrequencyType repaymentPeriodFrequencyType, final LocalDate expectedDisbursementDate) {
        final Integer recalculationFrequencyNthDay = null;
        final Integer repeatsOnDay = expectedDisbursementDate.getDayOfWeek();
        CalendarInstance restCalendarInstance = createInterestRecalculationCalendarInstance(expectedDisbursementDate, repaymentEvery,
                CalendarFrequencyType.from(repaymentPeriodFrequencyType), recalculationFrequencyNthDay, repeatsOnDay);
        return restCalendarInstance;
    }

    private CalendarInstance createInterestRecalculationCalendarInstance(final LocalDate calendarStartDate,
            final RecalculationFrequencyType recalculationFrequencyType, final Integer frequency,
            final Integer recalculationFrequencyNthDay, final Integer repeatsOnDay) {

        CalendarFrequencyType calendarFrequencyType = CalendarFrequencyType.INVALID;
        switch (recalculationFrequencyType) {
            case DAILY:
                calendarFrequencyType = CalendarFrequencyType.DAILY;
            break;
            case MONTHLY:
                calendarFrequencyType = CalendarFrequencyType.MONTHLY;
            break;
            case WEEKLY:
                calendarFrequencyType = CalendarFrequencyType.WEEKLY;
            break;
            default:
            break;
        }

        return createInterestRecalculationCalendarInstance(calendarStartDate, frequency, calendarFrequencyType,
                recalculationFrequencyNthDay, repeatsOnDay);
    }

    private CalendarInstance createInterestRecalculationCalendarInstance(final LocalDate calendarStartDate, final Integer frequency,
            CalendarFrequencyType calendarFrequencyType, final Integer recalculationFrequencyNthDay, final Integer repeatsOnDay) {
        final String title = "loan_recalculation_detail";
        final Calendar calendar = Calendar.createRepeatingCalendar(title, calendarStartDate, CalendarType.COLLECTION.getValue(),
                calendarFrequencyType, frequency, repeatsOnDay, recalculationFrequencyNthDay);
        return CalendarInstance.from(calendar, null, CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL.getValue());
    }

    private Calendar createLoanCalendar(final LocalDate calendarStartDate, final Integer frequency,
            CalendarFrequencyType calendarFrequencyType, final Integer repeatsOnDay, final Integer repeatsOnNthDayOfMonth) {
        final String title = "loan_schedule";
        final Calendar calendar = Calendar.createRepeatingCalendar(title, calendarStartDate, CalendarType.COLLECTION.getValue(),
                calendarFrequencyType, frequency, repeatsOnDay, repeatsOnNthDayOfMonth);
        return calendar;
    }

    private List<DisbursementData> fetchDisbursementData(final JsonObject command) {
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(command);
        final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(command);
        List<DisbursementData> disbursementDatas = new ArrayList<>();
        if (command.has(LoanApiConstants.disbursementDataParameterName)) {
            final JsonArray disbursementDataArray = command.getAsJsonArray(LoanApiConstants.disbursementDataParameterName);
            if (disbursementDataArray != null && disbursementDataArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = disbursementDataArray.get(i).getAsJsonObject();
                    LocalDate expectedDisbursementDate = null;
                    BigDecimal principal = null;

                    if (jsonObject.has(LoanApiConstants.disbursementDateParameterName)) {
                        expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed(
                                LoanApiConstants.disbursementDateParameterName, jsonObject, dateFormat, locale);
                    }
                    if (jsonObject.has(LoanApiConstants.disbursementPrincipalParameterName)
                            && jsonObject.get(LoanApiConstants.disbursementPrincipalParameterName).isJsonPrimitive()
                            && StringUtils.isNotBlank((jsonObject.get(LoanApiConstants.disbursementPrincipalParameterName).getAsString()))) {
                        principal = jsonObject.getAsJsonPrimitive(LoanApiConstants.disbursementPrincipalParameterName).getAsBigDecimal();
                    }
                    BigDecimal waivedChargeAmount = null;
                    disbursementDatas.add(new DisbursementData(null, expectedDisbursementDate, null, principal, null, null, waivedChargeAmount));
                    i++;
                } while (i < disbursementDataArray.size());
            }
        }
        return disbursementDatas;
    }

    private void validateRepaymentsStartDateWithMeetingDates(final LocalDate repaymentsStartingFromDate, final Calendar calendar,
		boolean isSkipRepaymentOnFirstDayOfMonth, final Integer numberOfDays) {
        if (repaymentsStartingFromDate != null
                && !CalendarUtils.isValidRedurringDate(calendar.getRecurrence(), calendar.getStartDateLocalDate(),
                        repaymentsStartingFromDate, isSkipRepaymentOnFirstDayOfMonth, numberOfDays)) {
            final String errorMessage = "First repayment date '" + repaymentsStartingFromDate + "' do not fall on a meeting date";
            throw new LoanApplicationDateException("first.repayment.date.do.not.match.meeting.date", errorMessage,
                    repaymentsStartingFromDate);
        }
    }

    public void validateDisbursementDateWithMeetingDates(final LocalDate expectedDisbursementDate, final Calendar calendar, Boolean isSkipRepaymentOnFirstMonth, Integer numberOfDays) {
        // disbursement date should fall on a meeting date
        if (calendar != null && !calendar.isValidRecurringDate(expectedDisbursementDate, isSkipRepaymentOnFirstMonth, numberOfDays)) {
            final String errorMessage = "Expected disbursement date '" + expectedDisbursementDate + "' do not fall on a meeting date";
            throw new LoanApplicationDateException("disbursement.date.do.not.match.meeting.date", errorMessage, expectedDisbursementDate);
        }

    }

    private void validateRepaymentFrequencyIsSameAsMeetingFrequency(final Integer meetingFrequency, final Integer repaymentFrequency,
            final Integer meetingInterval, final Integer repaymentInterval) {
        // meeting with daily frequency should allow loan products with any
        // frequency.
        if (!PeriodFrequencyType.DAYS.getValue().equals(meetingFrequency)) {
            // repayment frequency must match with meeting frequency
            if (!meetingFrequency.equals(repaymentFrequency)) {
                throw new MeetingFrequencyMismatchException("loanapplication.repayment.frequency",
                        "Loan repayment frequency period must match that of meeting frequency period", repaymentFrequency);
            } else if (meetingFrequency.equals(repaymentFrequency)) {
                // repayment frequency is same as meeting frequency repayment
                // interval should be same or multiple of meeting interval
                if (repaymentInterval % meetingInterval != 0) {
                    // throw exception: Loan product frequency/interval
                    throw new MeetingFrequencyMismatchException("loanapplication.repayment.interval",
                            "Loan repayment repaid every # must equal or multiple of meeting interval " + meetingInterval, meetingInterval,
                            repaymentInterval);
                }
            }
        }
    }

    public LoanProductRelatedDetail assembleLoanProductRelatedDetail(final JsonElement element) {
        final LoanApplicationTerms loanApplicationTerms = assembleLoanTerms(element);
        return loanApplicationTerms.toLoanProductRelatedDetail();
    }

    public LoanScheduleModel assembleLoanScheduleFrom(final JsonElement element) {
        // This method is getting called from calculate loan schedule.
        final LoanApplicationTerms loanApplicationTerms = assembleLoanTerms(element);
        // Get holiday details
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();

        final Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
        final Long groupId = this.fromApiJsonHelper.extractLongNamed("groupId", element);

        Client client = null;
        Group group = null;
        Long officeId = null;
        if (clientId != null) {
            client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            officeId = client.getOffice().getId();
        } else if (groupId != null) {
            group = this.groupRepository.findOneWithNotFoundDetection(groupId);
            officeId = group.getOffice().getId();
        }

        final LocalDate expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed("expectedDisbursementDate", element);
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(officeId, expectedDisbursementDate.toDate(),
                HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();

        validateDisbursementDateIsOnNonWorkingDay(loanApplicationTerms.getExpectedDisbursementDate(), workingDays);
        validateDisbursementDateIsOnHoliday(loanApplicationTerms.getExpectedDisbursementDate(), isHolidayEnabled, holidays);

        List<LoanDisbursementDetails> loanDisbursementDetails = this.loanUtilService.fetchDisbursementData(element.getAsJsonObject());

        return assembleLoanScheduleFrom(loanApplicationTerms, isHolidayEnabled, holidays, workingDays, element, loanDisbursementDetails);
    }

    public LoanScheduleModel assembleLoanScheduleFrom(final LoanApplicationTerms loanApplicationTerms, final boolean isHolidayEnabled,
            final List<Holiday> holidays, final WorkingDays workingDays, final JsonElement element,
            List<LoanDisbursementDetails> disbursementDetails) {

        final Set<LoanCharge> loanCharges = this.loanChargeAssembler.fromParsedJson(element, disbursementDetails);
        

        final RoundingMode roundingMode = MoneyHelper.getRoundingMode();
        final MathContext mc = new MathContext(8, roundingMode);
        HolidayDetailDTO detailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
        
        LoanScheduleGenerator loanScheduleGenerator = this.loanScheduleFactory.create(loanApplicationTerms.getInterestMethod());
        if (loanApplicationTerms.isEqualAmortization()) {
            if (loanApplicationTerms.getInterestMethod().isDecliningBalnce()) {
                final LoanScheduleGenerator decliningLoanScheduleGenerator = this.loanScheduleFactory
                        .create(InterestMethod.DECLINING_BALANCE);
                LoanScheduleModel loanSchedule = decliningLoanScheduleGenerator.generate(mc, loanApplicationTerms, loanCharges, detailDTO);
                
                loanApplicationTerms.updateTotalInterestDue(Money.of(loanApplicationTerms.getCurrency(), loanSchedule.getTotalInterestCharged()));
                
            }
            loanScheduleGenerator = this.loanScheduleFactory.create(InterestMethod.FLAT);
        } else {
            loanScheduleGenerator = this.loanScheduleFactory.create(loanApplicationTerms.getInterestMethod());
        }

        return loanScheduleGenerator.generate(mc, loanApplicationTerms, loanCharges, detailDTO);
    }

    public LoanScheduleModel assembleForInterestRecalculation(final LoanApplicationTerms loanApplicationTerms, final Long officeId,
            Loan loan, final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor,
            final LocalDate rescheduleFrom) {
        final RoundingMode roundingMode = MoneyHelper.getRoundingMode();
        final MathContext mc = new MathContext(8, roundingMode);
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();

        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(officeId, loanApplicationTerms
                .getExpectedDisbursementDate().toDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();

        final LoanScheduleGenerator loanScheduleGenerator = this.loanScheduleFactory.create(loanApplicationTerms.getInterestMethod());
        HolidayDetailDTO detailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
        return loanScheduleGenerator.rescheduleNextInstallments(mc, loanApplicationTerms, loan, detailDTO,
                loanRepaymentScheduleTransactionProcessor, rescheduleFrom).getLoanScheduleModel();
    }

    public LoanRepaymentScheduleInstallment calculatePrepaymentAmount(MonetaryCurrency currency, LocalDate onDate,
            LoanApplicationTerms loanApplicationTerms, Loan loan, final Long officeId,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor) {
        final LoanScheduleGenerator loanScheduleGenerator = this.loanScheduleFactory.create(loanApplicationTerms.getInterestMethod());
        final RoundingMode roundingMode = MoneyHelper.getRoundingMode();
        final MathContext mc = new MathContext(8, roundingMode);

        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(officeId, loanApplicationTerms
                .getExpectedDisbursementDate().toDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);

        return loanScheduleGenerator.calculatePrepaymentAmount(currency, onDate, loanApplicationTerms, mc, loan, holidayDetailDTO,
                loanRepaymentScheduleTransactionProcessor);

    }

    public void assempleVariableScheduleFrom(final Loan loan, final String json) {
        this.variableLoanScheduleFromApiJsonValidator.validateSchedule(json, loan);

        List<LoanTermVariations> variations = loan.getLoanTermVariations();
        List<LoanTermVariations> newVariations = new ArrayList<>();
        extractLoanTermVariations(loan, json, newVariations);

        final Map<LocalDate, LocalDate> adjustDueDateVariations = new HashMap<>();

        if (!variations.isEmpty()) {
            List<LoanTermVariations> retainVariations = adjustExistingVariations(variations, newVariations, adjustDueDateVariations);
            newVariations = retainVariations;
        }
        variations.addAll(newVariations);
        //Collections.sort(variations, new LoanTermVariationsComparator());
        
        /*
         * List<LoanTermVariationsData> loanTermVariationsDatas = new
         * ArrayList<>();
         * loanTermVariationsDatas.addAll(loanApplicationTerms.getLoanTermVariations
         * ().getExceptionData()); loanApplicationTerms =
         * LoanApplicationTerms.assembleFrom(loanApplicationTerms,
         * loanTermVariationsDatas);
         */

        // date validations
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        Set<LocalDate> dueDates = new TreeSet<>();
        LocalDate graceApplicable = loan.getExpectedDisbursedOnLocalDate();
        Integer graceOnPrincipal = loan.getLoanProductRelatedDetail().graceOnPrincipalPayment();
        if (graceOnPrincipal == null) {
            graceOnPrincipal = 0;
        }
        LocalDate lastDate = loan.getExpectedDisbursedOnLocalDate();
        for (LoanRepaymentScheduleInstallment installment : installments) {
            dueDates.add(installment.getDueDate());
            if (lastDate.isBefore(installment.getDueDate())) {
                lastDate = installment.getDueDate();
            }
            if (graceOnPrincipal.equals(installment.getInstallmentNumber())) {
                graceApplicable = installment.getDueDate();
            }
        }
        Collection<LocalDate> keySet = adjustDueDateVariations.keySet();
        dueDates.addAll(keySet);
        for (final LocalDate date : keySet) {
            LocalDate removeDate = adjustDueDateVariations.get(date);
            if (removeDate != null) {
                dueDates.remove(removeDate);
            }
        }

        Set<LocalDate> actualDueDates = new TreeSet<>(dueDates);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        List<LocalDate> overlappings = new ArrayList<>();
        for (LoanTermVariations termVariations : variations) {
            switch (termVariations.getTermType()) {
                case INSERT_INSTALLMENT:
                    if (dueDates.contains(termVariations.fetchTermApplicaDate())) {
                        overlappings.add(termVariations.fetchTermApplicaDate());
                    } else {
                        dueDates.add(termVariations.fetchTermApplicaDate());
                    }
                    if (!graceApplicable.isBefore(termVariations.fetchTermApplicaDate())) {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                                "variable.schedule.insert.not.allowed.before.grace.period", "Loan schedule insert request invalid");
                    }
                    if (termVariations.fetchTermApplicaDate().isAfter(lastDate)) {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                                "variable.schedule.insert.not.allowed.after.last.period.date", "Loan schedule insert request invalid");
                    } else if (termVariations.fetchTermApplicaDate().isBefore(loan.getExpectedDisbursedOnLocalDate())) {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                                "variable.schedule.insert.not.allowed.before.disbursement.date", "Loan schedule insert request invalid");
                    }
                break;
                case DELETE_INSTALLMENT:
                    if (dueDates.contains(termVariations.fetchTermApplicaDate())) {
                        dueDates.remove(termVariations.fetchTermApplicaDate());
                    } else {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("variable.schedule.remove.date.invalid",
                                "Loan schedule remove request invalid");
                    }
                    if (termVariations.fetchTermApplicaDate().isEqual(lastDate)) {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                                "variable.schedule.delete.not.allowed.for.last.period.date", "Loan schedule remove request invalid");
                    }
                break;
                case DUE_DATE:
                    if (dueDates.contains(termVariations.fetchTermApplicaDate())) {

                        if (overlappings.contains(termVariations.fetchTermApplicaDate())) {
                            overlappings.remove(termVariations.fetchTermApplicaDate());
                        } else {
                            dueDates.remove(termVariations.fetchTermApplicaDate());
                        }
                    } else {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("variable.schedule.modify.date.invalid",
                                "Loan schedule modify due date request invalid");
                    }
                    if (dueDates.contains(termVariations.fetchDateValue())) {
                        overlappings.add(termVariations.fetchDateValue());
                    } else {
                        dueDates.add(termVariations.fetchDateValue());
                    }
                    if (termVariations.fetchDateValue().isBefore(loan.getExpectedDisbursedOnLocalDate())) {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                                "variable.schedule.insert.not.allowed.before.disbursement.date", "Loan schedule insert request invalid");
                    }
                    if (termVariations.fetchTermApplicaDate().isEqual(lastDate)) {
                        lastDate = termVariations.fetchDateValue();
                    }
                break;
                case PRINCIPAL_AMOUNT:
                case EMI_AMOUNT:
                    if (!graceApplicable.isBefore(termVariations.fetchTermApplicaDate())) {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                                "variable.schedule.amount.update.not.allowed.before.grace.period", "Loan schedule modify request invalid");
                    }
                    if (!dueDates.contains(termVariations.fetchTermApplicaDate())) {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                                "variable.schedule.amount.update.from.date.invalid", "Loan schedule modify request invalid");
                    }
                    if (termVariations.fetchTermApplicaDate().isEqual(lastDate)) {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                                "variable.schedule.amount.update.not.allowed.for.last.period", "Loan schedule modify request invalid");
                    }
                break;

                default:
                break;

            }

        }
        if (!overlappings.isEmpty()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("variable.schedule.modify.date.can.not.be.due.date",
                    overlappings);
        }
        LoanProductVariableInstallmentConfig installmentConfig = loan.loanProduct().loanProductVariableInstallmentConfig();
        final CalendarInstance loanCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        Calendar loanCalendar = null;
        if (loanCalendarInstance != null) {
            loanCalendar = loanCalendarInstance.getCalendar();
        }
        Boolean isSkipRepaymentOnFirstMonth = false;
        Integer numberOfDays = 0;
        boolean isSkipRepaymentOnFirstMonthEnabled = configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
        if(isSkipRepaymentOnFirstMonthEnabled){
            isSkipRepaymentOnFirstMonth = this.loanUtilService.isLoanRepaymentsSyncWithMeeting(loan.group(), loanCalendar);
            if(isSkipRepaymentOnFirstMonth) { numberOfDays = configurationDomainService.retreivePeroidInNumberOfDaysForSkipMeetingDate().intValue(); }
        }
        final Integer minGap = installmentConfig.getMinimumGap();
        final Integer maxGap = installmentConfig.getMaximumGap();

        LocalDate previousDate = loan.getDisbursementDate();
        for (LocalDate duedate : dueDates) {
            int gap = Days.daysBetween(previousDate, duedate).getDays();
            previousDate = duedate;
            if (gap < minGap || (maxGap != null && gap > maxGap)) {
                baseDataValidator
                        .reset()
                        .value(duedate)
                        .failWithCodeNoParameterAddedToErrorCode("variable.schedule.date.must.be.in.min.max.range",
                                "Loan schedule date invalid");
            } else if (loanCalendar != null && !actualDueDates.contains(duedate) && !loanCalendar.isValidRecurringDate(duedate, 
                    isSkipRepaymentOnFirstMonth, numberOfDays)) {
                baseDataValidator
                        .reset()
                        .value(duedate)
                        .failWithCodeNoParameterAddedToErrorCode("variable.schedule.date.not.meeting.date",
                                "Loan schedule date not in sync with meeting date");
            }
        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        if (loan.getExpectedFirstRepaymentOnDate() == null) {
            loan.setExpectedFirstRepaymentOnDate(loan.fetchRepaymentScheduleInstallment(1).getDueDate().toDate());
        }
        final LocalDate recalculateFrom = null;
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
        AppUser currentUser = this.context.getAuthenticatedUserIfPresent();
        loan.regenerateRepaymentSchedule(scheduleGeneratorDTO, currentUser);
    }

    private List<LoanTermVariations> adjustExistingVariations(List<LoanTermVariations> variations, List<LoanTermVariations> newVariations,
            final Map<LocalDate, LocalDate> adjustDueDateVariations) {
        Map<LocalDate, LoanTermVariations> amountVariations = new HashMap<>();
        Map<LocalDate, LoanTermVariations> dueDateVariations = new HashMap<>();
        Map<LocalDate, LoanTermVariations> insertVariations = new HashMap<>();
        
        for (LoanTermVariations loanTermVariations : variations) {
            switch (loanTermVariations.getTermType()) {
                case EMI_AMOUNT:
                case PRINCIPAL_AMOUNT:
                    amountVariations.put(loanTermVariations.fetchTermApplicaDate(), loanTermVariations);
                break;
                case DUE_DATE:
                    dueDateVariations.put(loanTermVariations.fetchDateValue(), loanTermVariations);
                    adjustDueDateVariations.put(loanTermVariations.fetchTermApplicaDate(), loanTermVariations.fetchDateValue());
                break;
                case INSERT_INSTALLMENT:
                    insertVariations.put(loanTermVariations.fetchTermApplicaDate(), loanTermVariations);
                    adjustDueDateVariations.put(loanTermVariations.fetchTermApplicaDate(), loanTermVariations.fetchTermApplicaDate());
                break;
                case DELETE_INSTALLMENT:
                    adjustDueDateVariations.put(loanTermVariations.fetchTermApplicaDate(), null);
                break;
                default:
                break;
            }
        }
        List<LoanTermVariations> retainVariations = new ArrayList<>();
        for (LoanTermVariations loanTermVariations : newVariations) {
            boolean retain = true;
            switch (loanTermVariations.getTermType()) {
                case DUE_DATE:
                    if (amountVariations.containsKey(loanTermVariations.fetchTermApplicaDate())) {
                        amountVariations.get(loanTermVariations.fetchTermApplicaDate()).setTermApplicableFrom(
                                loanTermVariations.getDateValue());
                    } else if (insertVariations.containsKey(loanTermVariations.fetchTermApplicaDate())) {
                        insertVariations.get(loanTermVariations.fetchTermApplicaDate()).setTermApplicableFrom(
                                loanTermVariations.getDateValue());
                        retain = false;
                    }
                    if (dueDateVariations.containsKey(loanTermVariations.fetchTermApplicaDate())) {
                        LoanTermVariations existingVariation = dueDateVariations.get(loanTermVariations.fetchTermApplicaDate());
                        if (existingVariation.fetchTermApplicaDate().isEqual(loanTermVariations.fetchDateValue())) {
                            variations.remove(existingVariation);
                        } else {
                            existingVariation.setTermApplicableFrom(loanTermVariations.getDateValue());
                        }
                        retain = false;
                    }
                break;
                case EMI_AMOUNT:
                case PRINCIPAL_AMOUNT:
                    if (amountVariations.containsKey(loanTermVariations.fetchTermApplicaDate())) {
                        amountVariations.get(loanTermVariations.fetchTermApplicaDate()).setDecimalValue(loanTermVariations.getTermValue());
                        retain = false;
                    } else if (insertVariations.containsKey(loanTermVariations.fetchTermApplicaDate())) {
                        insertVariations.get(loanTermVariations.fetchTermApplicaDate()).setDecimalValue(loanTermVariations.getTermValue());
                        retain = false;
                    }
                break;
                case DELETE_INSTALLMENT:
                    if (amountVariations.containsKey(loanTermVariations.fetchTermApplicaDate())) {
                        variations.remove(amountVariations.get(loanTermVariations.fetchTermApplicaDate()));

                    } else if (insertVariations.containsKey(loanTermVariations.fetchTermApplicaDate())) {
                        variations.remove(insertVariations.get(loanTermVariations.fetchTermApplicaDate()));
                        retain = false;
                    }
                    if (dueDateVariations.containsKey(loanTermVariations.fetchTermApplicaDate())) {
                        variations.remove(amountVariations.get(loanTermVariations.fetchTermApplicaDate()));
                    }
                break;
                default:
                break;
            }
            if (retain) {
                retainVariations.add(loanTermVariations);
            }
        }
        return retainVariations;
    }

    private void extractLoanTermVariations(final Loan loan, final String json, final List<LoanTermVariations> loanTermVariations) {
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        if (loan.loanProduct().allowVariabeInstallments()) {
            if (element.isJsonObject() && this.fromApiJsonHelper.parameterExists(LoanApiConstants.exceptionParamName, element)) {
                final JsonObject topLevelJsonElement = element.getAsJsonObject();
                final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
                final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
                final JsonObject exceptionObject = topLevelJsonElement.getAsJsonObject(LoanApiConstants.exceptionParamName);
                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.modifiedinstallmentsParamName, exceptionObject)
                        && exceptionObject.get(LoanApiConstants.modifiedinstallmentsParamName).isJsonArray()) {
                    final JsonArray modificationsArray = exceptionObject.get(LoanApiConstants.modifiedinstallmentsParamName)
                            .getAsJsonArray();
                    extractLoanTermVariations(loan, dateFormat, locale, modificationsArray, false, false, loanTermVariations);
                }
                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.newinstallmentsParamName, exceptionObject)
                        && exceptionObject.get(LoanApiConstants.newinstallmentsParamName).isJsonArray()) {
                    final JsonArray array = exceptionObject.get(LoanApiConstants.newinstallmentsParamName).getAsJsonArray();
                    extractLoanTermVariations(loan, dateFormat, locale, array, true, false, loanTermVariations);
                }
                if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.deletedinstallmentsParamName, exceptionObject)
                        && exceptionObject.get(LoanApiConstants.deletedinstallmentsParamName).isJsonArray()) {
                    final JsonArray array = exceptionObject.get(LoanApiConstants.deletedinstallmentsParamName).getAsJsonArray();
                    extractLoanTermVariations(loan, dateFormat, locale, array, false, true, loanTermVariations);
                }
            }
        }
    }

    private void extractLoanTermVariations(final Loan loan, final String dateFormat, final Locale locale,
            final JsonArray modificationsArray, final boolean isInsertInstallment, final boolean isDeleteInstallment,
            final List<LoanTermVariations> loanTermVariations) {
        for (int i = 1; i <= modificationsArray.size(); i++) {
            final JsonObject arrayElement = modificationsArray.get(i - 1).getAsJsonObject();
            BigDecimal decimalValue = null;
            LoanTermVariationType decimalValueVariationType = LoanTermVariationType.INVALID;
            if (loan.getLoanProductRelatedDetail().getAmortizationMethod().isEqualInstallment()
                    && loan.getLoanProductRelatedDetail().getInterestMethod().isDecliningBalnce()) {
                decimalValue = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.installmentAmountParamName, arrayElement,
                        locale);
                decimalValueVariationType = LoanTermVariationType.EMI_AMOUNT;
            } else {
                decimalValue = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.principalParamName, arrayElement, locale);
                decimalValueVariationType = LoanTermVariationType.PRINCIPAL_AMOUNT;
            }

            LocalDate duedateLocalDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.dueDateParamName, arrayElement,
                    dateFormat, locale);
            Date dueDate = duedateLocalDate.toDate();

            LocalDate modifiedDuedateLocalDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.modifiedDueDateParamName,
                    arrayElement, dateFormat, locale);
            Date modifiedDuedate = null;
            if (modifiedDuedateLocalDate != null) {
                modifiedDuedate = modifiedDuedateLocalDate.toDate();
            }
            boolean isSpecificToInstallment = true;
            if (isInsertInstallment) {
                LoanTermVariations data = new LoanTermVariations(LoanTermVariationType.INSERT_INSTALLMENT.getValue(), dueDate,
                        decimalValue, modifiedDuedate, isSpecificToInstallment, loan);
                loanTermVariations.add(data);
            } else if (isDeleteInstallment) {
                LoanTermVariations data = new LoanTermVariations(LoanTermVariationType.DELETE_INSTALLMENT.getValue(), dueDate,
                        decimalValue, modifiedDuedate, isSpecificToInstallment, loan);
                loanTermVariations.add(data);
            } else {
                if (modifiedDuedate != null) {
                    BigDecimal amountData = null;
                    LoanTermVariations data = new LoanTermVariations(LoanTermVariationType.DUE_DATE.getValue(), dueDate, amountData,
                            modifiedDuedate, isSpecificToInstallment, loan);
                    loanTermVariations.add(data);
                }
                if (decimalValue != null) {
                    if (modifiedDuedate == null) {
                        modifiedDuedate = dueDate;
                    }
                    Date date = null;
                    LoanTermVariations data = new LoanTermVariations(decimalValueVariationType.getValue(), modifiedDuedate, decimalValue,
                            date, isSpecificToInstallment, loan);
                    loanTermVariations.add(data);
                }
            }

        }
    }

    private void validateDisbursementDateIsOnNonWorkingDay(final LocalDate disbursementDate, final WorkingDays workingDays) {
        if (!WorkingDaysUtil.isWorkingDay(workingDays, disbursementDate)) {
            final String errorMessage = "The expected disbursement date cannot be on a non working day";
            throw new LoanApplicationDateException("disbursement.date.on.non.working.day", errorMessage, disbursementDate);
        }
    }

    private void validateDisbursementDateIsOnHoliday(final LocalDate disbursementDate, final boolean isHolidayEnabled,
            final List<Holiday> holidays) {
        if (isHolidayEnabled) {
            if (HolidayUtil.isHoliday(disbursementDate, holidays)) {
                final String errorMessage = "The expected disbursement date cannot be on a holiday";
                throw new LoanApplicationDateException("disbursement.date.on.holiday", errorMessage, disbursementDate);
            }
        }
    }

    private LocalDate deriveFirstRepaymentDate(final AccountType loanType, final Integer repaymentEvery,
            final LocalDate expectedDisbursementDate, final PeriodFrequencyType repaymentPeriodFrequencyType,
            final Integer minimumDaysBetweenDisbursalAndFirstRepayment, final Calendar calendar) {

        LocalDate derivedFirstRepayment = null;

        final LocalDate dateBasedOnMinimumDaysBetweenDisbursalAndFirstRepayment = expectedDisbursementDate
                .plusDays(minimumDaysBetweenDisbursalAndFirstRepayment);

        if (calendar != null) {

            final LocalDate refernceDateForCalculatingFirstRepaymentDate = expectedDisbursementDate;
            derivedFirstRepayment = deriveFirstRepaymentDateForLoans(repaymentEvery, expectedDisbursementDate,
                    refernceDateForCalculatingFirstRepaymentDate, repaymentPeriodFrequencyType,
                    minimumDaysBetweenDisbursalAndFirstRepayment, calendar);

        }/*** Individual or group account, or JLG not linked to a meeting ***/
        else {
            LocalDate dateBasedOnRepaymentFrequency;
            // Derive the first repayment date as greater date among
            // (disbursement date + plus frequency) or
            // (disbursement date + minimum between disbursal and first
            // repayment )
            if (repaymentPeriodFrequencyType.isDaily()) {
                dateBasedOnRepaymentFrequency = expectedDisbursementDate.plusDays(repaymentEvery);
            } else if (repaymentPeriodFrequencyType.isWeekly()) {
                dateBasedOnRepaymentFrequency = expectedDisbursementDate.plusWeeks(repaymentEvery);
            } else if (repaymentPeriodFrequencyType.isMonthly()) {
                dateBasedOnRepaymentFrequency = expectedDisbursementDate.plusMonths(repaymentEvery);
            }/** yearly loan **/
            else {
                dateBasedOnRepaymentFrequency = expectedDisbursementDate.plusYears(repaymentEvery);
            }
            derivedFirstRepayment = dateBasedOnRepaymentFrequency.isAfter(dateBasedOnMinimumDaysBetweenDisbursalAndFirstRepayment) ? dateBasedOnRepaymentFrequency
                    : dateBasedOnMinimumDaysBetweenDisbursalAndFirstRepayment;
        }

        return derivedFirstRepayment;
    }

    private LocalDate deriveFirstRepaymentDateForLoans(final Integer repaymentEvery, final LocalDate expectedDisbursementDate,
            final LocalDate refernceDateForCalculatingFirstRepaymentDate, final PeriodFrequencyType repaymentPeriodFrequencyType,
            final Integer minimumDaysBetweenDisbursalAndFirstRepayment, final Calendar calendar) {
        boolean isMeetingSkipOnFirstDayOfMonth = configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
        int numberOfDays = configurationDomainService.retreivePeroidInNumberOfDaysForSkipMeetingDate().intValue();
        final String frequency = CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(repaymentPeriodFrequencyType);
        final LocalDate derivedFirstRepayment = CalendarUtils.getFirstRepaymentMeetingDate(calendar,
                refernceDateForCalculatingFirstRepaymentDate, repaymentEvery, frequency, isMeetingSkipOnFirstDayOfMonth, numberOfDays);
        final LocalDate minimumFirstRepaymentDate = expectedDisbursementDate.plusDays(minimumDaysBetweenDisbursalAndFirstRepayment);
        return minimumFirstRepaymentDate.isBefore(derivedFirstRepayment) ? derivedFirstRepayment : deriveFirstRepaymentDateForLoans(
                repaymentEvery, expectedDisbursementDate, derivedFirstRepayment, repaymentPeriodFrequencyType,
                minimumDaysBetweenDisbursalAndFirstRepayment, calendar);
    }

    private void validateMinimumDaysBetweenDisbursalAndFirstRepayment(final LocalDate disbursalDate, final LocalDate firstRepaymentDate,
            final Integer minimumDaysBetweenDisbursalAndFirstRepayment) {

        final LocalDate minimumFirstRepaymentDate = disbursalDate.plusDays(minimumDaysBetweenDisbursalAndFirstRepayment);
        if (firstRepaymentDate.isBefore(minimumFirstRepaymentDate)) { throw new MinDaysBetweenDisbursalAndFirstRepaymentViolationException(
                disbursalDate, firstRepaymentDate, minimumDaysBetweenDisbursalAndFirstRepayment); }
    }
}
