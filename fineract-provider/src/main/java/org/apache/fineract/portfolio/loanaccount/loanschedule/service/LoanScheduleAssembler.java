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

import static org.apache.fineract.portfolio.loanaccount.domain.Loan.APPROVED_ON_DATE;
import static org.apache.fineract.portfolio.loanaccount.domain.Loan.DATE_FORMAT;
import static org.apache.fineract.portfolio.loanaccount.domain.Loan.EVENT_DATE;
import static org.apache.fineract.portfolio.loanaccount.domain.Loan.EXPECTED_DISBURSEMENT_DATE;
import static org.apache.fineract.portfolio.loanaccount.domain.Loan.LOCALE;
import static org.apache.fineract.portfolio.loanaccount.domain.Loan.PARAM_STATUS;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
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
import org.apache.fineract.portfolio.loanaccount.data.OutstandingAmountsDTO;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanOfficerAssignmentHistory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariations;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.exception.LoanApplicationDateException;
import org.apache.fineract.portfolio.loanaccount.exception.MinDaysBetweenDisbursalAndFirstRepaymentViolationException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanaccount.serialization.VariableLoanScheduleFromApiJsonValidator;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualsProcessingService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanDisbursementDetailsAssembler;
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
import org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
    private final LoanUtilService loanUtilService;
    private final LoanDisbursementDetailsAssembler loanDisbursementDetailsAssembler;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanLifecycleStateMachine defaultLoanLifecycleStateMachine;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;

    public LoanApplicationTerms assembleLoanTerms(final JsonElement element) {
        final Long loanProductId = this.fromApiJsonHelper.extractLongNamed("productId", element);

        final LoanProduct loanProduct = this.loanProductRepository.findById(loanProductId)
                .orElseThrow(() -> new LoanProductNotFoundException(loanProductId));
        return assembleLoanApplicationTermsFrom(element, loanProduct);
    }

    private LoanApplicationTerms assembleLoanApplicationTermsFrom(final JsonElement element, final LoanProduct loanProduct) {

        final Boolean allowOverridingAmortization = loanProduct.getLoanConfigurableAttributes().getAmortizationBoolean();
        final Boolean allowOverridingArrearsTolerance = loanProduct.getLoanConfigurableAttributes().getArrearsToleranceBoolean();
        final Boolean allowOverridingGraceOnArrearsAging = loanProduct.getLoanConfigurableAttributes().getGraceOnArrearsAgingBoolean();
        final Boolean allowOverridingInterestCalcPeriod = loanProduct.getLoanConfigurableAttributes().getInterestCalcPeriodBoolean();
        final Boolean allowOverridingInterestMethod = loanProduct.getLoanConfigurableAttributes().getInterestMethodBoolean();
        final Boolean allowOverridingGraceOnPrincipalAndInterestPayment = loanProduct.getLoanConfigurableAttributes()
                .getGraceOnPrincipalAndInterestPaymentBoolean();
        final Boolean allowOverridingRepaymentEvery = loanProduct.getLoanConfigurableAttributes().getRepaymentEveryBoolean();

        final MonetaryCurrency currency = loanProduct.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        // loan terms
        final Integer loanTermFrequency = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("loanTermFrequency", element);
        final Integer loanTermFrequencyType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("loanTermFrequencyType", element);
        final PeriodFrequencyType loanTermPeriodFrequencyType = PeriodFrequencyType.fromInt(loanTermFrequencyType);

        final Integer numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("numberOfRepayments", element);
        final Integer repaymentEvery = allowOverridingRepaymentEvery
                ? this.fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentEvery", element)
                : loanProduct.getLoanProductRelatedDetail().getRepayEvery();
        final Integer repaymentFrequencyType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentFrequencyType", element);
        final PeriodFrequencyType repaymentPeriodFrequencyType = PeriodFrequencyType.fromInt(repaymentFrequencyType);
        final Integer nthDay = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentFrequencyNthDayType", element);
        final Integer dayOfWeek = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentFrequencyDayOfWeekType", element);
        final DayOfWeekType weekDayType = DayOfWeekType.fromInt(dayOfWeek);

        final Integer amortizationType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("amortizationType", element);
        final AmortizationMethod amortizationMethod = allowOverridingAmortization ? AmortizationMethod.fromInt(amortizationType)
                : loanProduct.getLoanProductRelatedDetail().getAmortizationMethod();

        boolean isEqualAmortization = false;
        if (this.fromApiJsonHelper.parameterExists(LoanApiConstants.isEqualAmortizationParam, element)) {
            isEqualAmortization = this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isEqualAmortizationParam, element);
        }

        BigDecimal fixedPrincipalPercentagePerInstallment = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName, element);

        // interest terms
        final Integer interestType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("interestType", element);
        final InterestMethod interestMethod = allowOverridingInterestMethod ? InterestMethod.fromInt(interestType)
                : loanProduct.getLoanProductRelatedDetail().getInterestMethod();

        final Integer interestCalculationPeriodType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("interestCalculationPeriodType",
                element);
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = allowOverridingInterestCalcPeriod
                ? InterestCalculationPeriodMethod.fromInt(interestCalculationPeriodType)
                : loanProduct.getLoanProductRelatedDetail().getInterestCalculationPeriodMethod();
        Boolean allowPartialPeriodInterestCalcualtion = this.fromApiJsonHelper
                .extractBooleanNamed(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME, element);
        if (allowPartialPeriodInterestCalcualtion == null) {
            allowPartialPeriodInterestCalcualtion = loanProduct.getLoanProductRelatedDetail().isAllowPartialPeriodInterestCalcualtion();
        }

        final BigDecimal interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRatePerPeriod", element);
        final PeriodFrequencyType interestRatePeriodFrequencyType = loanProduct.getInterestPeriodFrequencyType();

        BigDecimal annualNominalInterestRate = BigDecimal.ZERO;
        if (interestRatePerPeriod != null) {
            annualNominalInterestRate = this.aprCalculator.calculateFrom(interestRatePeriodFrequencyType, interestRatePerPeriod,
                    numberOfRepayments, repaymentEvery, repaymentPeriodFrequencyType);
        }

        // disbursement details
        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        final Money principalMoney = Money.of(currency, principal);

        final LocalDate expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed("expectedDisbursementDate", element);
        LocalDate repaymentsStartingFromDate = this.fromApiJsonHelper.extractLocalDateNamed("repaymentsStartingFromDate", element);
        final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed("submittedOnDate", element);

        final RepaymentStartDateType repaymentStartDateType = loanProduct.getRepaymentStartDateType();

        LocalDate calculatedRepaymentsStartingFromDate = repaymentsStartingFromDate;

        final Long calendarId = this.fromApiJsonHelper.extractLongNamed("calendarId", element);
        Calendar calendar = null;

        final String loanTypeParameterName = "loanType";
        final String loanTypeStr = this.fromApiJsonHelper.extractStringNamed(loanTypeParameterName, element);

        final AccountType loanType = AccountType.fromName(loanTypeStr);

        /*
         * If it is JLG loan/Group Loan then make sure loan frequency is same as Group/Center meeting frequency or
         * multiple of it. TODO: Check should be either same frequency or loan freq is multiple of center/group meeting
         * freq multiples
         */
        if ((loanType.isJLGAccount() || loanType.isGroupAccount()) && calendarId != null) {
            calendar = this.calendarRepository.findById(calendarId).orElseThrow(() -> new CalendarNotFoundException(calendarId));
            final PeriodFrequencyType meetingPeriodFrequency = CalendarUtils.getMeetingPeriodFrequencyType(calendar.getRecurrence());
            validateRepaymentFrequencyIsSameAsMeetingFrequency(meetingPeriodFrequency.getValue(), repaymentFrequencyType,
                    CalendarUtils.getInterval(calendar.getRecurrence()), repaymentEvery);
        } else {
            if (repaymentPeriodFrequencyType == PeriodFrequencyType.MONTHS && nthDay != null
                    && !nthDay.equals(NthDayType.INVALID.getValue())) {
                LocalDate calendarStartDate = repaymentsStartingFromDate;
                if (calendarStartDate == null) {
                    calendarStartDate = RepaymentStartDateType.DISBURSEMENT_DATE.equals(repaymentStartDateType) ? expectedDisbursementDate
                            : submittedOnDate;
                }
                calendar = createLoanCalendar(calendarStartDate, repaymentEvery, CalendarFrequencyType.MONTHLY, dayOfWeek, nthDay);
            }
        }

        /*
         * If user has not passed the first repayments date then then derive the same based on loan type.
         */
        if (calculatedRepaymentsStartingFromDate == null) {
            LocalDate tmpCalculatedRepaymentsStartingFromDate = deriveFirstRepaymentDate(loanType, repaymentEvery, expectedDisbursementDate,
                    repaymentPeriodFrequencyType, 0, calendar, submittedOnDate, repaymentStartDateType);
            calculatedRepaymentsStartingFromDate = deriveFirstRepaymentDate(loanType, repaymentEvery, expectedDisbursementDate,
                    repaymentPeriodFrequencyType, loanProduct.getMinimumDaysBetweenDisbursalAndFirstRepayment(), calendar, submittedOnDate,
                    repaymentStartDateType);
            // If calculated repayment start date does not match due to minimum days between disbursal and first
            // repayment rule, we set repaymentsStartingFromDate (which will be used as seed date later)
            if (!tmpCalculatedRepaymentsStartingFromDate.equals(calculatedRepaymentsStartingFromDate)) {
                repaymentsStartingFromDate = calculatedRepaymentsStartingFromDate;
            }
        }

        /*
         * If it is JLG loan/Group Loan synched with a meeting, then make sure first repayment falls on meeting date
         */
        final Long groupId = this.fromApiJsonHelper.extractLongNamed("groupId", element);
        Group group = null;
        if (groupId != null) {
            group = this.groupRepository.findOneWithNotFoundDetection(groupId);
        }

        Boolean isSkipMeetingOnFirstDay = false;
        Integer numberOfDays = 0;
        boolean isSkipRepaymentOnFirstMonthEnabled = configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
        if (isSkipRepaymentOnFirstMonthEnabled) {
            isSkipMeetingOnFirstDay = this.loanUtilService.isLoanRepaymentsSyncWithMeeting(group, calendar);
            if (isSkipMeetingOnFirstDay) {
                numberOfDays = configurationDomainService.retreivePeriodInNumberOfDaysForSkipMeetingDate().intValue();
            }
        }
        if ((loanType.isJLGAccount() || loanType.isGroupAccount()) && calendar != null) {
            validateRepaymentsStartDateWithMeetingDates(calculatedRepaymentsStartingFromDate, calendar, isSkipMeetingOnFirstDay,
                    numberOfDays);
        }

        if (RepaymentStartDateType.DISBURSEMENT_DATE.equals(repaymentStartDateType)) {
            validateMinimumDaysBetweenDisbursalAndFirstRepayment(expectedDisbursementDate, calculatedRepaymentsStartingFromDate,
                    loanProduct.getMinimumDaysBetweenDisbursalAndFirstRepayment());
        }

        // grace details
        final Integer graceOnPrincipalPayment = allowOverridingGraceOnPrincipalAndInterestPayment
                ? this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnPrincipalPayment", element)
                : loanProduct.getLoanProductRelatedDetail().getGraceOnPrincipalPayment();
        final Integer recurringMoratoriumOnPrincipalPeriods = this.fromApiJsonHelper
                .extractIntegerWithLocaleNamed("recurringMoratoriumOnPrincipalPeriods", element);
        final Integer graceOnInterestPayment = allowOverridingGraceOnPrincipalAndInterestPayment
                ? this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestPayment", element)
                : loanProduct.getLoanProductRelatedDetail().getGraceOnInterestPayment();
        final Integer graceOnInterestCharged = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestCharged", element);
        final LocalDate interestChargedFromDate = this.fromApiJsonHelper.extractLocalDateNamed("interestChargedFromDate", element);
        final Boolean isInterestChargedFromDateSameAsDisbursalDateEnabled = this.configurationDomainService
                .isInterestChargedFromDateSameAsDisbursementDate();

        final Integer graceOnArrearsAgeing = allowOverridingGraceOnArrearsAging
                ? this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME, element)
                : loanProduct.getLoanProductRelatedDetail().getGraceOnArrearsAgeing();

        // other
        final BigDecimal inArrearsTolerance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("inArrearsTolerance", element);
        final Money inArrearsToleranceMoney = allowOverridingArrearsTolerance ? Money.of(currency, inArrearsTolerance)
                : loanProduct.getLoanProductRelatedDetail().getInArrearsTolerance();

        final BigDecimal emiAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.fixedEmiAmountParameterName,
                element);
        final BigDecimal maxOutstandingBalance = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.maxOutstandingBalanceParameterName, element);

        final List<DisbursementData> disbursementDatas = fetchDisbursementData(element.getAsJsonObject());

        /**
         * Interest recalculation settings copy from product definition
         */
        final DaysInMonthType daysInMonthType = loanProduct.fetchDaysInMonthType();

        DaysInYearType daysInYearType = null;
        final Integer daysInYearTypeIntFromApplication = this.fromApiJsonHelper
                .extractIntegerNamed(LoanApiConstants.daysInYearTypeParameterName, element, Locale.getDefault());
        if (daysInYearTypeIntFromApplication != null) {
            daysInYearType = DaysInYearType.fromInt(daysInYearTypeIntFromApplication);
        } else {
            daysInYearType = loanProduct.fetchDaysInYearType();
        }

        final boolean isInterestRecalculationEnabled = loanProduct.isInterestRecalculationEnabled();
        RecalculationFrequencyType recalculationFrequencyType = null;
        CalendarInstance restCalendarInstance = null;
        RecalculationFrequencyType compoundingFrequencyType = null;
        CalendarInstance compoundingCalendarInstance = null;
        InterestRecalculationCompoundingMethod compoundingMethod = null;
        boolean allowCompoundingOnEod = false;
        final Boolean isFloatingInterestRate = this.fromApiJsonHelper
                .extractBooleanNamed(LoanApiConstants.isFloatingInterestRateParameterName, element);
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
                restCalendarInstance = createInterestRecalculationCalendarInstance(calendarStartDate, recalculationFrequencyType, frequency,
                        recalculationFrequencyNthDay, repeatsOnDay);
            }
            compoundingMethod = InterestRecalculationCompoundingMethod
                    .fromInt(loanProductInterestRecalculationDetails.getInterestRecalculationCompoundingMethod());
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
            final BigDecimal interestRateDiff = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(LoanApiConstants.interestRateDifferentialParameterName, element);

            List<FloatingRatePeriodData> baseLendingRatePeriods = null;
            try {
                baseLendingRatePeriods = this.floatingRatesReadPlatformService.retrieveBaseLendingRate().getRatePeriods();
            } catch (final FloatingRateNotFoundException ex) {
                // Do not do anything
            }
            FloatingRateDTO floatingRateDTO = new FloatingRateDTO(isFloatingInterestRate, expectedDisbursementDate, interestRateDiff,
                    baseLendingRatePeriods);
            Collection<FloatingRatePeriodData> applicableRates = loanProduct.fetchInterestRates(floatingRateDTO);

            LocalDate interestRateStartDate = DateUtils.getBusinessLocalDate();
            final LocalDate dateValue = null;
            final boolean isSpecificToInstallment = false;
            for (FloatingRatePeriodData periodData : applicableRates) {
                LoanTermVariationsData loanTermVariation = new LoanTermVariationsData(
                        LoanEnumerations.loanVariationType(LoanTermVariationType.INTEREST_RATE), periodData.getFromDateAsLocalDate(),
                        periodData.getInterestRate(), dateValue, isSpecificToInstallment);
                if (!DateUtils.isBefore(interestRateStartDate, periodData.getFromDateAsLocalDate())) {
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
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(officeId, expectedDisbursementDate,
                HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        HolidayDetailDTO detailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
        final boolean isInterestToBeRecoveredFirstWhenGreaterThanEMI = this.configurationDomainService
                .isInterestToBeRecoveredFirstWhenGreaterThanEMI();
        final boolean isPrincipalCompoundingDisabledForOverdueLoans = this.configurationDomainService
                .isPrincipalCompoundingDisabledForOverdueLoans();

        boolean isDownPaymentEnabled = loanProduct.getLoanProductRelatedDetail().isEnableDownPayment();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ENABLE_DOWN_PAYMENT, element)) {
            isDownPaymentEnabled = this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ENABLE_DOWN_PAYMENT, element);
        }

        BigDecimal disbursedAmountPercentageForDownPayment = null;
        boolean isAutoRepaymentForDownPaymentEnabled = false;
        if (isDownPaymentEnabled) {
            isAutoRepaymentForDownPaymentEnabled = loanProduct.getLoanProductRelatedDetail().isEnableAutoRepaymentForDownPayment();
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT, element)) {
                isAutoRepaymentForDownPaymentEnabled = this.fromApiJsonHelper
                        .extractBooleanNamed(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT, element);
            }

            disbursedAmountPercentageForDownPayment = loanProduct.getLoanProductRelatedDetail()
                    .getDisbursedAmountPercentageForDownPayment();
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT, element)) {
                disbursedAmountPercentageForDownPayment = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT, element);
            }
        }

        LoanScheduleType loanScheduleType = loanProduct.getLoanProductRelatedDetail().getLoanScheduleType();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOAN_SCHEDULE_TYPE, element)) {
            loanScheduleType = LoanScheduleType
                    .valueOf(this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.LOAN_SCHEDULE_TYPE, element));
        }

        LoanScheduleProcessingType loanScheduleProcessingType = loanProduct.getLoanProductRelatedDetail().getLoanScheduleProcessingType();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE, element)) {
            loanScheduleProcessingType = LoanScheduleProcessingType
                    .valueOf(this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE, element));
        }

        Integer fixedLength = loanProduct.getLoanProductRelatedDetail().getFixedLength();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.FIXED_LENGTH, element)) {
            fixedLength = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(LoanProductConstants.FIXED_LENGTH, element);
        }

        return LoanApplicationTerms.assembleFrom(applicationCurrency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentPeriodFrequencyType, nthDay, weekDayType, amortizationMethod, interestMethod,
                interestRatePerPeriod, interestRatePeriodFrequencyType, annualNominalInterestRate, interestCalculationPeriodMethod,
                allowPartialPeriodInterestCalcualtion, principalMoney, expectedDisbursementDate, repaymentsStartingFromDate,
                calculatedRepaymentsStartingFromDate, graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods,
                graceOnInterestPayment, graceOnInterestCharged, interestChargedFromDate, inArrearsToleranceMoney,
                loanProduct.isMultiDisburseLoan(), emiAmount, disbursementDatas, maxOutstandingBalance, graceOnArrearsAgeing,
                daysInMonthType, daysInYearType, isInterestRecalculationEnabled, recalculationFrequencyType, restCalendarInstance,
                compoundingMethod, compoundingCalendarInstance, compoundingFrequencyType, principalThresholdForLastInstalment,
                installmentAmountInMultiplesOf, loanProduct.preCloseInterestCalculationStrategy(), calendar, BigDecimal.ZERO,
                loanTermVariations, isInterestChargedFromDateSameAsDisbursalDateEnabled, numberOfDays, isSkipMeetingOnFirstDay, detailDTO,
                allowCompoundingOnEod, isEqualAmortization, isInterestToBeRecoveredFirstWhenGreaterThanEMI,
                fixedPrincipalPercentagePerInstallment, isPrincipalCompoundingDisabledForOverdueLoans, isDownPaymentEnabled,
                disbursedAmountPercentageForDownPayment, isAutoRepaymentForDownPaymentEnabled, repaymentStartDateType, submittedOnDate,
                loanScheduleType, loanScheduleProcessingType, fixedLength,
                loanProduct.getLoanProductRelatedDetail().isEnableAccrualActivityPosting(),
                loanProduct.getLoanProductRelatedDetail().getSupportedInterestRefundTypes());
    }

    private CalendarInstance createCalendarForSameAsRepayment(final Integer repaymentEvery,
            final PeriodFrequencyType repaymentPeriodFrequencyType, final LocalDate expectedDisbursementDate) {
        final Integer recalculationFrequencyNthDay = null;
        final Integer repeatsOnDay = expectedDisbursementDate.get(ChronoField.DAY_OF_WEEK);
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
                    BigDecimal netDisbursalAmount = null;

                    if (jsonObject.has(LoanApiConstants.expectedDisbursementDateParameterName)) {
                        expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed(
                                LoanApiConstants.expectedDisbursementDateParameterName, jsonObject, dateFormat, locale);
                    }
                    if (jsonObject.has(LoanApiConstants.disbursementPrincipalParameterName)
                            && jsonObject.get(LoanApiConstants.disbursementPrincipalParameterName).isJsonPrimitive()
                            && StringUtils.isNotBlank(jsonObject.get(LoanApiConstants.disbursementPrincipalParameterName).getAsString())) {
                        principal = jsonObject.getAsJsonPrimitive(LoanApiConstants.disbursementPrincipalParameterName).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanApiConstants.disbursementNetDisbursalAmountParameterName)
                            && jsonObject.get(LoanApiConstants.disbursementNetDisbursalAmountParameterName).isJsonPrimitive()
                            && StringUtils.isNotBlank(
                                    jsonObject.get(LoanApiConstants.disbursementNetDisbursalAmountParameterName).getAsString())) {
                        netDisbursalAmount = jsonObject.getAsJsonPrimitive(LoanApiConstants.disbursementNetDisbursalAmountParameterName)
                                .getAsBigDecimal();
                    }
                    BigDecimal waivedChargeAmount = null;
                    disbursementDatas.add(new DisbursementData(null, expectedDisbursementDate, null, principal, netDisbursalAmount, null,
                            null, waivedChargeAmount));
                    i++;
                } while (i < disbursementDataArray.size());
            }
        }
        return disbursementDatas;
    }

    private void validateRepaymentsStartDateWithMeetingDates(final LocalDate repaymentsStartingFromDate, final Calendar calendar,
            boolean isSkipRepaymentOnFirstDayOfMonth, final Integer numberOfDays) {
        if (repaymentsStartingFromDate != null && !CalendarUtils.isValidRecurringDate(calendar.getRecurrence(),
                calendar.getStartDateLocalDate(), repaymentsStartingFromDate, isSkipRepaymentOnFirstDayOfMonth, numberOfDays)) {
            final String errorMessage = "First repayment date '" + repaymentsStartingFromDate + "' do not fall on a meeting date";
            throw new LoanApplicationDateException("first.repayment.date.do.not.match.meeting.date", errorMessage,
                    repaymentsStartingFromDate);
        }
    }

    private void validateRepaymentFrequencyIsSameAsMeetingFrequency(final Integer meetingFrequency, final Integer repaymentFrequency,
            final Integer meetingInterval, final Integer repaymentInterval) {
        // meeting with daily frequency should allow loan products with any frequency.
        if (!PeriodFrequencyType.DAYS.getValue().equals(meetingFrequency)) {
            // repayment frequency must match with meeting frequency
            if (!meetingFrequency.equals(repaymentFrequency)) {
                throw new MeetingFrequencyMismatchException("loanapplication.repayment.frequency",
                        "Loan repayment frequency period must match that of meeting frequency period", repaymentFrequency);
            } else {
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

    public LoanProductRelatedDetail assembleLoanProductRelatedDetail(final JsonElement element, final LoanProduct loanProduct) {
        final LoanApplicationTerms loanApplicationTerms = assembleLoanApplicationTermsFrom(element, loanProduct);
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
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(officeId, expectedDisbursementDate,
                HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();

        validateDisbursementDateIsOnNonWorkingDay(loanApplicationTerms.getExpectedDisbursementDate(), workingDays);
        validateDisbursementDateIsOnHoliday(loanApplicationTerms.getExpectedDisbursementDate(), isHolidayEnabled, holidays);

        List<LoanDisbursementDetails> loanDisbursementDetails = this.loanDisbursementDetailsAssembler
                .fetchDisbursementData(element.getAsJsonObject());

        return assembleLoanScheduleFrom(loanApplicationTerms, isHolidayEnabled, holidays, workingDays, element, loanDisbursementDetails);
    }

    public LoanScheduleModel assembleLoanScheduleFrom(final LoanApplicationTerms loanApplicationTerms, final boolean isHolidayEnabled,
            final List<Holiday> holidays, final WorkingDays workingDays, final JsonElement element,
            List<LoanDisbursementDetails> disbursementDetails) {

        final Set<LoanCharge> loanCharges = this.loanChargeAssembler.fromParsedJson(element, disbursementDetails);

        final MathContext mc = MoneyHelper.getMathContext();
        HolidayDetailDTO detailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);

        LoanScheduleGenerator loanScheduleGenerator = this.loanScheduleFactory.create(loanApplicationTerms.getLoanScheduleType(),
                loanApplicationTerms.getInterestMethod());
        if (loanApplicationTerms.isEqualAmortization()) {
            if (loanApplicationTerms.getInterestMethod().isDecliningBalance()) {
                final LoanScheduleGenerator decliningLoanScheduleGenerator = this.loanScheduleFactory
                        .create(loanApplicationTerms.getLoanScheduleType(), InterestMethod.DECLINING_BALANCE);
                LoanScheduleModel loanSchedule = decliningLoanScheduleGenerator.generate(mc, loanApplicationTerms, loanCharges, detailDTO);

                loanApplicationTerms
                        .updateTotalInterestDue(Money.of(loanApplicationTerms.getCurrency(), loanSchedule.getTotalInterestCharged()));

            }
            loanScheduleGenerator = this.loanScheduleFactory.create(loanApplicationTerms.getLoanScheduleType(), InterestMethod.FLAT);
        } else {
            loanScheduleGenerator = this.loanScheduleFactory.create(loanApplicationTerms.getLoanScheduleType(),
                    loanApplicationTerms.getInterestMethod());
        }

        return loanScheduleGenerator.generate(mc, loanApplicationTerms, loanCharges, detailDTO);
    }

    public LoanScheduleModel assembleForInterestRecalculation(final LoanApplicationTerms loanApplicationTerms, final Long officeId,
            Loan loan, final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor,
            final LocalDate rescheduleFrom) {

        final MathContext mc = MoneyHelper.getMathContext();
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();

        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(officeId,
                loanApplicationTerms.getExpectedDisbursementDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();

        final LoanScheduleGenerator loanScheduleGenerator = this.loanScheduleFactory.create(loanApplicationTerms.getLoanScheduleType(),
                loanApplicationTerms.getInterestMethod());
        HolidayDetailDTO detailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
        return loanScheduleGenerator.rescheduleNextInstallments(mc, loanApplicationTerms, loan, detailDTO,
                loanRepaymentScheduleTransactionProcessor, rescheduleFrom).getLoanScheduleModel();
    }

    public OutstandingAmountsDTO calculatePrepaymentAmount(MonetaryCurrency currency, LocalDate onDate,
            LoanApplicationTerms loanApplicationTerms, Loan loan, final Long officeId,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor) {
        final LoanScheduleGenerator loanScheduleGenerator = this.loanScheduleFactory.create(loanApplicationTerms.getLoanScheduleType(),
                loanApplicationTerms.getInterestMethod());

        final MathContext mc = MoneyHelper.getMathContext();

        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(officeId,
                loanApplicationTerms.getExpectedDisbursementDate(), HolidayStatusType.ACTIVE.getValue());
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
        // Collections.sort(variations, new LoanTermVariationsComparator());

        /*
         * List<LoanTermVariationsData> loanTermVariationsDatas = new ArrayList<>();
         * loanTermVariationsDatas.addAll(loanApplicationTerms. getLoanTermVariations ().getExceptionData());
         * loanApplicationTerms = LoanApplicationTerms.assembleFrom(loanApplicationTerms, loanTermVariationsDatas);
         */

        // date validations
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        Set<LocalDate> dueDates = new TreeSet<>();
        LocalDate graceApplicable = loan.getExpectedDisbursedOnLocalDate();
        Integer graceOnPrincipal = loan.getLoanProductRelatedDetail().getGraceOnPrincipalPayment();
        if (graceOnPrincipal == null) {
            graceOnPrincipal = 0;
        }
        LocalDate lastDate = loan.getExpectedDisbursedOnLocalDate();
        for (LoanRepaymentScheduleInstallment installment : installments) {
            dueDates.add(installment.getDueDate());
            if (DateUtils.isBefore(lastDate, installment.getDueDate())) {
                lastDate = installment.getDueDate();
            }
            if (graceOnPrincipal.equals(installment.getInstallmentNumber())) {
                graceApplicable = installment.getDueDate();
            }
        }
        dueDates.addAll(adjustDueDateVariations.keySet());
        for (Map.Entry<LocalDate, LocalDate> entry : adjustDueDateVariations.entrySet()) {
            LocalDate removeDate = entry.getValue();
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
                    if (!DateUtils.isBefore(graceApplicable, termVariations.fetchTermApplicaDate())) {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                                "variable.schedule.insert.not.allowed.before.grace.period", "Loan schedule insert request invalid");
                    }
                    if (DateUtils.isAfter(termVariations.fetchTermApplicaDate(), lastDate)) {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                                "variable.schedule.insert.not.allowed.after.last.period.date", "Loan schedule insert request invalid");
                    } else if (DateUtils.isBefore(termVariations.fetchTermApplicaDate(), loan.getExpectedDisbursedOnLocalDate())) {
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
                    if (DateUtils.isEqual(lastDate, termVariations.fetchTermApplicaDate())) {
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
                    if (DateUtils.isBefore(termVariations.fetchDateValue(), loan.getExpectedDisbursedOnLocalDate())) {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                                "variable.schedule.insert.not.allowed.before.disbursement.date", "Loan schedule insert request invalid");
                    }
                    if (DateUtils.isEqual(lastDate, termVariations.fetchTermApplicaDate())) {
                        lastDate = termVariations.fetchDateValue();
                    }
                break;
                case PRINCIPAL_AMOUNT:
                case EMI_AMOUNT:
                    if (!DateUtils.isBefore(graceApplicable, termVariations.fetchTermApplicaDate())) {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                                "variable.schedule.amount.update.not.allowed.before.grace.period", "Loan schedule modify request invalid");
                    }
                    if (!dueDates.contains(termVariations.fetchTermApplicaDate())) {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                                "variable.schedule.amount.update.from.date.invalid", "Loan schedule modify request invalid");
                    }
                    if (DateUtils.isEqual(termVariations.fetchTermApplicaDate(), lastDate)) {
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
        if (isSkipRepaymentOnFirstMonthEnabled) {
            isSkipRepaymentOnFirstMonth = this.loanUtilService.isLoanRepaymentsSyncWithMeeting(loan.group(), loanCalendar);
            if (isSkipRepaymentOnFirstMonth) {
                numberOfDays = configurationDomainService.retreivePeriodInNumberOfDaysForSkipMeetingDate().intValue();
            }
        }
        final Integer minGap = installmentConfig.getMinimumGap();
        final Integer maxGap = installmentConfig.getMaximumGap();

        LocalDate previousDate = loan.getDisbursementDate();
        for (LocalDate duedate : dueDates) {
            int gap = Math.toIntExact(ChronoUnit.DAYS.between(previousDate, duedate));
            previousDate = duedate;
            if (gap < minGap || (maxGap != null && gap > maxGap)) {
                baseDataValidator.reset().value(duedate).failWithCodeNoParameterAddedToErrorCode(
                        "variable.schedule.date.must.be.in.min.max.range", "Loan schedule date invalid");
            } else if (loanCalendar != null && !actualDueDates.contains(duedate)
                    && !loanCalendar.isValidRecurringDate(duedate, isSkipRepaymentOnFirstMonth, numberOfDays)) {
                baseDataValidator.reset().value(duedate).failWithCodeNoParameterAddedToErrorCode("variable.schedule.date.not.meeting.date",
                        "Loan schedule date not in sync with meeting date");
            }
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        if (loan.getExpectedFirstRepaymentOnDate() == null) {
            loan.setExpectedFirstRepaymentOnDate(loan.fetchRepaymentScheduleInstallment(1).getDueDate());
        }
        final LocalDate recalculateFrom = null;
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
        loan.regenerateRepaymentSchedule(scheduleGeneratorDTO);
        loanAccrualsProcessingService.reprocessExistingAccruals(loan);

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
                        amountVariations.get(loanTermVariations.fetchTermApplicaDate())
                                .setTermApplicableFrom(loanTermVariations.getDateValue());
                    } else if (insertVariations.containsKey(loanTermVariations.fetchTermApplicaDate())) {
                        insertVariations.get(loanTermVariations.fetchTermApplicaDate())
                                .setTermApplicableFrom(loanTermVariations.getDateValue());
                        retain = false;
                    }
                    if (dueDateVariations.containsKey(loanTermVariations.fetchTermApplicaDate())) {
                        LoanTermVariations existingVariation = dueDateVariations.get(loanTermVariations.fetchTermApplicaDate());
                        if (DateUtils.isEqual(existingVariation.fetchTermApplicaDate(), loanTermVariations.fetchDateValue())) {
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
        if (loan.loanProduct().isAllowVariabeInstallments()) {
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
                    && loan.getLoanProductRelatedDetail().getInterestMethod().isDecliningBalance()) {
                decimalValue = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.installmentAmountParamName, arrayElement,
                        locale);
                decimalValueVariationType = LoanTermVariationType.EMI_AMOUNT;
            } else {
                decimalValue = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.principalParamName, arrayElement, locale);
                decimalValueVariationType = LoanTermVariationType.PRINCIPAL_AMOUNT;
            }

            LocalDate dueDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.dueDateParamName, arrayElement, dateFormat,
                    locale);

            LocalDate modifiedDuedateLocalDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.modifiedDueDateParamName,
                    arrayElement, dateFormat, locale);
            LocalDate modifiedDuedate = null;
            if (modifiedDuedateLocalDate != null) {
                modifiedDuedate = modifiedDuedateLocalDate;
            }
            boolean isSpecificToInstallment = true;
            if (isInsertInstallment) {
                LoanTermVariations data = new LoanTermVariations(LoanTermVariationType.INSERT_INSTALLMENT.getValue(), dueDate, decimalValue,
                        modifiedDuedate, isSpecificToInstallment, loan);
                loanTermVariations.add(data);
            } else if (isDeleteInstallment) {
                LoanTermVariations data = new LoanTermVariations(LoanTermVariationType.DELETE_INSTALLMENT.getValue(), dueDate, decimalValue,
                        modifiedDuedate, isSpecificToInstallment, loan);
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
                    LocalDate date = null;
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
            final Integer minimumDaysBetweenDisbursalAndFirstRepayment, final Calendar calendar, final LocalDate submittedOnDate,
            final RepaymentStartDateType repaymentStartDateType) {
        LocalDate derivedFirstRepayment = null;

        final LocalDate dateBasedOnMinimumDaysBetweenDisbursalAndFirstRepayment = expectedDisbursementDate
                .plusDays(minimumDaysBetweenDisbursalAndFirstRepayment);
        final LocalDate seedDate = repaymentStartDateType.isDisbursementDate() ? expectedDisbursementDate : submittedOnDate;
        if (calendar != null) {
            derivedFirstRepayment = deriveFirstRepaymentDateForLoans(repaymentEvery, expectedDisbursementDate, seedDate,
                    repaymentPeriodFrequencyType, minimumDaysBetweenDisbursalAndFirstRepayment, calendar, submittedOnDate);
        } else { // Individual or group account, or JLG not linked to a meeting
            LocalDate dateBasedOnRepaymentFrequency;
            // Derive the first repayment date as greater date among
            // (disbursement date + plus frequency) or
            // (disbursement date + minimum between disbursal and first
            // repayment )
            if (repaymentPeriodFrequencyType.isDaily()) {
                dateBasedOnRepaymentFrequency = seedDate.plusDays(repaymentEvery);
            } else if (repaymentPeriodFrequencyType.isWeekly()) {
                dateBasedOnRepaymentFrequency = seedDate.plusWeeks(repaymentEvery);
            } else if (repaymentPeriodFrequencyType.isMonthly()) {
                dateBasedOnRepaymentFrequency = seedDate.plusMonths(repaymentEvery);
            } else { // yearly loan
                dateBasedOnRepaymentFrequency = seedDate.plusYears(repaymentEvery);
            }
            derivedFirstRepayment = DateUtils.isAfter(dateBasedOnRepaymentFrequency,
                    dateBasedOnMinimumDaysBetweenDisbursalAndFirstRepayment) ? dateBasedOnRepaymentFrequency
                            : dateBasedOnMinimumDaysBetweenDisbursalAndFirstRepayment;
        }

        return derivedFirstRepayment;
    }

    private LocalDate deriveFirstRepaymentDateForLoans(final Integer repaymentEvery, final LocalDate expectedDisbursementDate,
            final LocalDate refernceDateForCalculatingFirstRepaymentDate, final PeriodFrequencyType repaymentPeriodFrequencyType,
            final Integer minimumDaysBetweenDisbursalAndFirstRepayment, final Calendar calendar, final LocalDate submittedOnDate) {
        boolean isMeetingSkipOnFirstDayOfMonth = configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
        int numberOfDays = configurationDomainService.retreivePeriodInNumberOfDaysForSkipMeetingDate().intValue();
        final String frequency = CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(repaymentPeriodFrequencyType);
        final LocalDate derivedFirstRepayment = CalendarUtils.getFirstRepaymentMeetingDate(calendar,
                refernceDateForCalculatingFirstRepaymentDate, repaymentEvery, frequency, isMeetingSkipOnFirstDayOfMonth, numberOfDays);
        final LocalDate minimumFirstRepaymentDate = expectedDisbursementDate.plusDays(minimumDaysBetweenDisbursalAndFirstRepayment);
        return DateUtils.isBefore(minimumFirstRepaymentDate, derivedFirstRepayment) ? derivedFirstRepayment
                : deriveFirstRepaymentDateForLoans(repaymentEvery, expectedDisbursementDate, derivedFirstRepayment,
                        repaymentPeriodFrequencyType, minimumDaysBetweenDisbursalAndFirstRepayment, calendar, submittedOnDate);
    }

    private void validateMinimumDaysBetweenDisbursalAndFirstRepayment(final LocalDate disbursalDate, final LocalDate firstRepaymentDate,
            final Integer minimumDaysBetweenDisbursalAndFirstRepayment) {
        final LocalDate minimumFirstRepaymentDate = disbursalDate.plusDays(minimumDaysBetweenDisbursalAndFirstRepayment);
        if (DateUtils.isBefore(firstRepaymentDate, minimumFirstRepaymentDate)) {
            throw new MinDaysBetweenDisbursalAndFirstRepaymentViolationException(disbursalDate, firstRepaymentDate,
                    minimumDaysBetweenDisbursalAndFirstRepayment);
        }
    }

    public void updateProductRelatedDetails(LoanProductRelatedDetail productRelatedDetail, Loan loan) {
        final Boolean amortization = loan.loanProduct().getLoanConfigurableAttributes().getAmortizationBoolean();
        final Boolean arrearsTolerance = loan.loanProduct().getLoanConfigurableAttributes().getArrearsToleranceBoolean();
        final Boolean graceOnArrearsAging = loan.loanProduct().getLoanConfigurableAttributes().getGraceOnArrearsAgingBoolean();
        final Boolean interestCalcPeriod = loan.loanProduct().getLoanConfigurableAttributes().getInterestCalcPeriodBoolean();
        final Boolean interestMethod = loan.loanProduct().getLoanConfigurableAttributes().getInterestMethodBoolean();
        final Boolean graceOnPrincipalAndInterestPayment = loan.loanProduct().getLoanConfigurableAttributes()
                .getGraceOnPrincipalAndInterestPaymentBoolean();
        final Boolean repaymentEvery = loan.loanProduct().getLoanConfigurableAttributes().getRepaymentEveryBoolean();

        if (!amortization) {
            productRelatedDetail.setAmortizationMethod(loan.loanProduct().getLoanProductRelatedDetail().getAmortizationMethod());
        }
        if (!arrearsTolerance) {
            productRelatedDetail
                    .setInArrearsTolerance(loan.loanProduct().getLoanProductRelatedDetail().getInArrearsTolerance().getAmount());
        }
        if (!graceOnArrearsAging) {
            productRelatedDetail.setGraceOnArrearsAgeing(loan.loanProduct().getLoanProductRelatedDetail().getGraceOnArrearsAgeing());
        }
        if (!interestCalcPeriod) {
            productRelatedDetail.setInterestCalculationPeriodMethod(
                    loan.loanProduct().getLoanProductRelatedDetail().getInterestCalculationPeriodMethod());
        }
        if (!interestMethod) {
            productRelatedDetail.setInterestMethod(loan.loanProduct().getLoanProductRelatedDetail().getInterestMethod());
        }
        if (!graceOnPrincipalAndInterestPayment) {
            productRelatedDetail.setGraceOnInterestPayment(loan.loanProduct().getLoanProductRelatedDetail().getGraceOnInterestPayment());
            productRelatedDetail.setGraceOnPrincipalPayment(loan.loanProduct().getLoanProductRelatedDetail().getGraceOnPrincipalPayment());
        }
        if (!repaymentEvery) {
            productRelatedDetail.setRepayEvery(loan.loanProduct().getLoanProductRelatedDetail().getRepayEvery());
        }
    }

    public void updateLoanApplicationAttributes(JsonCommand command, Loan loan, Map<String, Object> changes) {
        final String localeAsInput = command.locale();

        final String principalParamName = "principal";
        LoanProductRelatedDetail loanProductRelatedDetail = loan.getLoanRepaymentScheduleDetail();
        if (command.isChangeInBigDecimalParameterNamed(principalParamName, loanProductRelatedDetail.getPrincipal().getAmount())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(principalParamName);
            changes.put(principalParamName, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setPrincipal(newValue);
        }

        final String repaymentEveryParamName = "repaymentEvery";
        if (command.isChangeInIntegerParameterNamed(repaymentEveryParamName, loanProductRelatedDetail.getRepayEvery())) {
            final Integer newValue = command.integerValueOfParameterNamed(repaymentEveryParamName);
            changes.put(repaymentEveryParamName, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setRepayEvery(newValue);
        }

        final String repaymentFrequencyTypeParamName = "repaymentFrequencyType";
        if (command.isChangeInIntegerParameterNamed(repaymentFrequencyTypeParamName,
                loanProductRelatedDetail.getRepaymentPeriodFrequencyType().getValue())) {
            Integer newValue = command.integerValueOfParameterNamed(repaymentFrequencyTypeParamName);
            changes.put(repaymentFrequencyTypeParamName, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setRepaymentPeriodFrequencyType(PeriodFrequencyType.fromInt(newValue));
        }
        if (PeriodFrequencyType.MONTHS.equals(loanProductRelatedDetail.getRepaymentPeriodFrequencyType())) {
            final String repaymentFrequencyNthDayTypeParamName = "repaymentFrequencyNthDayType";
            Integer newValue = command.integerValueOfParameterNamed(repaymentFrequencyNthDayTypeParamName);
            changes.put(repaymentFrequencyNthDayTypeParamName, newValue);

            final String repaymentFrequencyDayOfWeekTypeParamName = "repaymentFrequencyDayOfWeekType";
            newValue = command.integerValueOfParameterNamed(repaymentFrequencyDayOfWeekTypeParamName);
            changes.put(repaymentFrequencyDayOfWeekTypeParamName, newValue);
            changes.put("locale", localeAsInput);
        }

        final String numberOfRepaymentsParamName = "numberOfRepayments";
        if (command.isChangeInIntegerParameterNamed(numberOfRepaymentsParamName, loanProductRelatedDetail.getNumberOfRepayments())) {
            final Integer newValue = command.integerValueOfParameterNamed(numberOfRepaymentsParamName);
            changes.put(numberOfRepaymentsParamName, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setNumberOfRepayments(newValue);
        }

        final String amortizationTypeParamName = "amortizationType";
        if (command.isChangeInIntegerParameterNamed(amortizationTypeParamName,
                loanProductRelatedDetail.getAmortizationMethod().getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(amortizationTypeParamName);
            changes.put(amortizationTypeParamName, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setAmortizationMethod(AmortizationMethod.fromInt(newValue));
        }

        final String inArrearsToleranceParamName = "inArrearsTolerance";
        if (command.isChangeInBigDecimalParameterNamed(inArrearsToleranceParamName,
                loanProductRelatedDetail.getInArrearsTolerance().getAmount())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(inArrearsToleranceParamName);
            changes.put(inArrearsToleranceParamName, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setInArrearsTolerance(newValue);
        }

        final String interestRatePerPeriodParamName = "interestRatePerPeriod";
        if (command.isChangeInBigDecimalParameterNamed(interestRatePerPeriodParamName,
                loanProductRelatedDetail.getNominalInterestRatePerPeriod())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(interestRatePerPeriodParamName);
            changes.put(interestRatePerPeriodParamName, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setNominalInterestRatePerPeriod(newValue);
            loanProductRelatedDetail.updateInterestRateDerivedFields(aprCalculator);
        }

        final String interestRateFrequencyTypeParamName = "interestRateFrequencyType";
        final int interestPeriodFrequencyType = loanProductRelatedDetail.getInterestPeriodFrequencyType() == null
                ? PeriodFrequencyType.INVALID.getValue()
                : loanProductRelatedDetail.getInterestPeriodFrequencyType().getValue();
        if (command.isChangeInIntegerParameterNamed(interestRateFrequencyTypeParamName, interestPeriodFrequencyType)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestRateFrequencyTypeParamName);
            changes.put(interestRateFrequencyTypeParamName, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setInterestPeriodFrequencyType(PeriodFrequencyType.fromInt(newValue));
            loanProductRelatedDetail.updateInterestRateDerivedFields(aprCalculator);
        }

        final String interestTypeParamName = "interestType";
        if (command.isChangeInIntegerParameterNamed(interestTypeParamName, loanProductRelatedDetail.getInterestMethod().getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(interestTypeParamName);
            changes.put(interestTypeParamName, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setInterestMethod(InterestMethod.fromInt(newValue));
        }

        final String interestCalculationPeriodTypeParamName = "interestCalculationPeriodType";
        if (command.isChangeInIntegerParameterNamed(interestCalculationPeriodTypeParamName,
                loanProductRelatedDetail.getInterestCalculationPeriodMethod().getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCalculationPeriodTypeParamName);
            changes.put(interestCalculationPeriodTypeParamName, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setInterestCalculationPeriodMethod(InterestCalculationPeriodMethod.fromInt(newValue));
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME,
                loanProductRelatedDetail.isAllowPartialPeriodInterestCalcualtion())) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME);
            changes.put(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME, newValue);
            loanProductRelatedDetail.setAllowPartialPeriodInterestCalcualtion(newValue);
        }

        if (loanProductRelatedDetail.getInterestCalculationPeriodMethod().isDaily()) {
            loanProductRelatedDetail.setAllowPartialPeriodInterestCalcualtion(false);
        }

        final String graceOnPrincipalPaymentParamName = "graceOnPrincipalPayment";
        if (command.isChangeInIntegerParameterNamed(graceOnPrincipalPaymentParamName,
                loanProductRelatedDetail.getGraceOnPrincipalPayment())) {
            final Integer newValue = command.integerValueOfParameterNamed(graceOnPrincipalPaymentParamName);
            changes.put(graceOnPrincipalPaymentParamName, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setGraceOnPrincipalPayment(newValue);
        }

        final String recurringMoratoriumOnPrincipalPeriodsParamName = "recurringMoratoriumOnPrincipalPeriods";
        if (command.isChangeInIntegerParameterNamed(recurringMoratoriumOnPrincipalPeriodsParamName,
                loanProductRelatedDetail.getRecurringMoratoriumOnPrincipalPeriods())) {
            final Integer newValue = command.integerValueOfParameterNamed(recurringMoratoriumOnPrincipalPeriodsParamName);
            changes.put(recurringMoratoriumOnPrincipalPeriodsParamName, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setRecurringMoratoriumOnPrincipalPeriods(newValue);
        }

        final String graceOnInterestPaymentParamName = "graceOnInterestPayment";
        if (command.isChangeInIntegerParameterNamed(graceOnInterestPaymentParamName,
                loanProductRelatedDetail.getGraceOnInterestPayment())) {
            final Integer newValue = command.integerValueOfParameterNamed(graceOnInterestPaymentParamName);
            changes.put(graceOnInterestPaymentParamName, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setGraceOnInterestPayment(newValue);
        }

        final String graceOnInterestChargedParamName = "graceOnInterestCharged";
        if (command.isChangeInIntegerParameterNamed(graceOnInterestChargedParamName,
                loanProductRelatedDetail.getGraceOnInterestCharged())) {
            final Integer newValue = command.integerValueOfParameterNamed(graceOnInterestChargedParamName);
            changes.put(graceOnInterestChargedParamName, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setGraceOnInterestCharged(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME,
                loanProductRelatedDetail.getGraceOnArrearsAgeing())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME);
            changes.put(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setGraceOnArrearsAgeing(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME,
                loanProductRelatedDetail.fetchDaysInMonthType().getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME);
            changes.put(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setDaysInMonthType(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME,
                loanProductRelatedDetail.fetchDaysInYearType().getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME);
            changes.put(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME, newValue);
            changes.put("locale", localeAsInput);
            loanProductRelatedDetail.setDaysInYearType(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME,
                loanProductRelatedDetail.isInterestRecalculationEnabled())) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME);
            changes.put(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME, newValue);
            loanProductRelatedDetail.setInterestRecalculationEnabled(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM,
                loanProductRelatedDetail.isEqualAmortization())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM);
            changes.put(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, newValue);
            loanProductRelatedDetail.setEqualAmortization(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ENABLE_DOWN_PAYMENT,
                loanProductRelatedDetail.isEnableDownPayment())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ENABLE_DOWN_PAYMENT);
            changes.put(LoanProductConstants.ENABLE_DOWN_PAYMENT, newValue);
            loanProductRelatedDetail.setEnableDownPayment(newValue);
            if (!newValue) {
                loanProductRelatedDetail.setEnableAutoRepaymentForDownPayment(false);
                loanProductRelatedDetail.setDisbursedAmountPercentageForDownPayment(null);
            }
        }

        if (loanProductRelatedDetail.isEnableDownPayment()) {
            Boolean enableAutoRepaymentForDownPayment = loan.loanProduct().getLoanProductRelatedDetail()
                    .isEnableAutoRepaymentForDownPayment();
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT, command.parsedJson())) {
                if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT,
                        loanProductRelatedDetail.isEnableAutoRepaymentForDownPayment())) {
                    enableAutoRepaymentForDownPayment = command
                            .booleanObjectValueOfParameterNamed(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT);
                    changes.put(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT, enableAutoRepaymentForDownPayment);
                }
            }
            loanProductRelatedDetail.setEnableAutoRepaymentForDownPayment(enableAutoRepaymentForDownPayment);

            BigDecimal disbursedAmountPercentageDownPayment = loan.loanProduct().getLoanProductRelatedDetail()
                    .getDisbursedAmountPercentageForDownPayment();
            if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT,
                    command.parsedJson())) {
                if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT,
                        loanProductRelatedDetail.getDisbursedAmountPercentageForDownPayment())) {
                    disbursedAmountPercentageDownPayment = command
                            .bigDecimalValueOfParameterNamed(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT);
                    changes.put(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT, disbursedAmountPercentageDownPayment);
                }
            }
            loanProductRelatedDetail.setDisbursedAmountPercentageForDownPayment(disbursedAmountPercentageDownPayment);
        }
    }

    public Pair<Loan, Map<String, Object>> assembleLoanApproval(AppUser currentUser, JsonCommand command, Long loanId) {
        final JsonArray disbursementDataArray = command.arrayOfParameterNamed(LoanApiConstants.disbursementDataParameterName);
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);

        final Map<String, Object> actualChanges = new HashMap<>();
        defaultLoanLifecycleStateMachine.transition(LoanEvent.LOAN_APPROVED, loan);
        actualChanges.put(PARAM_STATUS, LoanEnumerations.status(loan.getStatus()));

        LocalDate approvedOn = command.localDateValueOfParameterNamed(APPROVED_ON_DATE);
        String approvedOnDateChange = command.stringValueOfParameterNamed(APPROVED_ON_DATE);
        if (approvedOn == null) {
            approvedOn = command.localDateValueOfParameterNamed(EVENT_DATE);
            approvedOnDateChange = command.stringValueOfParameterNamed(EVENT_DATE);
        }

        LocalDate expectedDisbursementDate = command.localDateValueOfParameterNamed(EXPECTED_DISBURSEMENT_DATE);

        BigDecimal approvedLoanAmount = command.bigDecimalValueOfParameterNamed(LoanApiConstants.approvedLoanAmountParameterName);
        if (approvedLoanAmount != null) {
            /*
             * All the calculations are done based on the principal amount, so it is necessary to set principal amount
             * to approved amount
             */
            loan.setApprovedPrincipal(approvedLoanAmount);
            loan.getLoanRepaymentScheduleDetail().setPrincipal(approvedLoanAmount);
            actualChanges.put(LoanApiConstants.approvedLoanAmountParameterName, approvedLoanAmount);
            actualChanges.put(LoanApiConstants.disbursementPrincipalParameterName, approvedLoanAmount);
            actualChanges.put(LoanApiConstants.disbursementNetDisbursalAmountParameterName, loan.getNetDisbursalAmount());

            if (disbursementDataArray != null) {
                loan.updateDisbursementDetails(command, actualChanges);
            }
        }

        loan.recalculateAllCharges();

        loan.setApprovedOnDate(approvedOn);
        loan.setApprovedBy(currentUser);

        actualChanges.put(LOCALE, command.locale());
        actualChanges.put(DATE_FORMAT, command.dateFormat());
        actualChanges.put(APPROVED_ON_DATE, approvedOnDateChange);

        if (expectedDisbursementDate != null) {
            loan.setExpectedDisbursementDate(expectedDisbursementDate);
            actualChanges.put(EXPECTED_DISBURSEMENT_DATE, expectedDisbursementDate);
        }

        if (loan.getLoanOfficer() != null) {
            final LoanOfficerAssignmentHistory loanOfficerAssignmentHistory = LoanOfficerAssignmentHistory.createNew(loan,
                    loan.getLoanOfficer(), approvedOn);
            loan.getLoanOfficerHistory().add(loanOfficerAssignmentHistory);
        }

        loan.adjustNetDisbursalAmount(loan.getApprovedPrincipal());

        if (!actualChanges.isEmpty()) {
            if (actualChanges.containsKey(LoanApiConstants.approvedLoanAmountParameterName)
                    || actualChanges.containsKey("recalculateLoanSchedule") || actualChanges.containsKey("expectedDisbursementDate")) {
                loan.regenerateRepaymentSchedule(loanUtilService.buildScheduleGeneratorDTO(loan, null));
                loanAccrualsProcessingService.reprocessExistingAccruals(loan);
            }
        }

        return Pair.of(loan, actualChanges);
    }

}
