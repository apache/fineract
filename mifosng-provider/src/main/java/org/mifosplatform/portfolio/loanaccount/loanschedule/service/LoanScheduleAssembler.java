/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.holiday.domain.HolidayRepository;
import org.mifosplatform.organisation.holiday.domain.HolidayStatusType;
import org.mifosplatform.organisation.holiday.service.HolidayUtil;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.mifosplatform.organisation.workingdays.service.WorkingDaysUtil;
import org.mifosplatform.portfolio.accountdetails.domain.AccountType;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarFrequencyType;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.domain.CalendarRepository;
import org.mifosplatform.portfolio.calendar.domain.CalendarType;
import org.mifosplatform.portfolio.calendar.exception.CalendarNotFoundException;
import org.mifosplatform.portfolio.calendar.exception.MeetingFrequencyMismatchException;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.common.domain.DayOfWeekType;
import org.mifosplatform.portfolio.common.domain.DaysInMonthType;
import org.mifosplatform.portfolio.common.domain.DaysInYearType;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepositoryWrapper;
import org.mifosplatform.portfolio.loanaccount.api.LoanApiConstants;
import org.mifosplatform.portfolio.loanaccount.data.DisbursementData;
import org.mifosplatform.portfolio.loanaccount.data.HolidayDetailDTO;
import org.mifosplatform.portfolio.loanaccount.data.LoanTermVariationsData;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTermVariationType;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.exception.LoanApplicationDateException;
import org.mifosplatform.portfolio.loanaccount.exception.MinDaysBetweenDisbursalAndFirstRepaymentViolationException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.mifosplatform.portfolio.loanaccount.service.LoanChargeAssembler;
import org.mifosplatform.portfolio.loanproduct.LoanProductConstants;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
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

    @Autowired
    public LoanScheduleAssembler(final FromJsonHelper fromApiJsonHelper, final LoanProductRepository loanProductRepository,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            final LoanScheduleGeneratorFactory loanScheduleFactory, final AprCalculator aprCalculator,
            final LoanChargeAssembler loanChargeAssembler, final CalendarRepository calendarRepository,
            final HolidayRepository holidayRepository, final ConfigurationDomainService configurationDomainService,
            final ClientRepositoryWrapper clientRepository, final GroupRepositoryWrapper groupRepository,
            final WorkingDaysRepositoryWrapper workingDaysRepository) {
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

        // interest terms
        final Integer interestType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("interestType", element);
        final InterestMethod interestMethod = InterestMethod.fromInt(interestType);

        final Integer interestCalculationPeriodType = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("interestCalculationPeriodType",
                element);
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod
                .fromInt(interestCalculationPeriodType);

        final BigDecimal interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRatePerPeriod", element);
        final PeriodFrequencyType interestRatePeriodFrequencyType = loanProduct.getInterestPeriodFrequencyType();

        final BigDecimal annualNominalInterestRate = this.aprCalculator.calculateFrom(interestRatePeriodFrequencyType,
                interestRatePerPeriod);

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
        if ((loanType.isJLGAccount() || loanType.isGroupAccount()) && calendar != null) {
            validateRepaymentsStartDateWithMeetingDates(calculatedRepaymentsStartingFromDate, calendar);
            /*
             * If disbursement is synced on meeting, make sure disbursement date
             * is on a meeting date
             */
            if (synchDisbursement != null && synchDisbursement.booleanValue()) {
                validateDisbursementDateWithMeetingDates(expectedDisbursementDate, calendar);
            }
        }

        validateMinimumDaysBetweenDisbursalAndFirstRepayment(expectedDisbursementDate, calculatedRepaymentsStartingFromDate,
                loanProduct.getMinimumDaysBetweenDisbursalAndFirstRepayment());

        // grace details
        final Integer graceOnPrincipalPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnPrincipalPayment", element);
        final Integer graceOnInterestPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestPayment", element);
        final Integer graceOnInterestCharged = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestCharged", element);
        final LocalDate interestChargedFromDate = this.fromApiJsonHelper.extractLocalDateNamed("interestChargedFromDate", element);

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

        final List<LoanTermVariationsData> loanVariationTermsData = new ArrayList<>();
        LoanTermVariationsData data = new LoanTermVariationsData(null,
                LoanEnumerations.loanvariationType(LoanTermVariationType.EMI_AMOUNT), expectedDisbursementDate, emiAmount);
        loanVariationTermsData.add(data);

        /**
         * Interest recalculation settings copy from product definition
         */
        final DaysInMonthType daysInMonthType = loanProduct.fetchDaysInMonthType();

        final DaysInYearType daysInYearType = loanProduct.fetchDaysInYearType();

        final boolean isInterestRecalculationEnabled = loanProduct.isInterestRecalculationEnabled();
        RecalculationFrequencyType recalculationFrequencyType = null;
        CalendarInstance restCalendarInstance = null;

        if (isInterestRecalculationEnabled) {
            recalculationFrequencyType = loanProduct.getProductInterestRecalculationDetails().getRestFrequencyType();
            if (!recalculationFrequencyType.isSameAsRepayment()) {
                LocalDate calendarStartDate = this.fromApiJsonHelper.extractLocalDateNamed(
                        LoanProductConstants.recalculationRestFrequencyDateParamName, element);
                if (calendarStartDate == null) {
                    calendarStartDate = expectedDisbursementDate;
                }
                restCalendarInstance = createInterestRecalculationCalendarInstance(calendarStartDate, recalculationFrequencyType,
                        loanProduct.getProductInterestRecalculationDetails().getRestInterval());
            }
        }

        final BigDecimal principalThresholdForLastInstalment = loanProduct.getPrincipalThresholdForLastInstallment();

        final Integer installmentAmountInMultiplesOf = loanProduct.getInstallmentAmountInMultiplesOf();

        return LoanApplicationTerms.assembleFrom(applicationCurrency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentPeriodFrequencyType, nthDay, weekDayType, amortizationMethod, interestMethod,
                interestRatePerPeriod, interestRatePeriodFrequencyType, annualNominalInterestRate, interestCalculationPeriodMethod,
                principalMoney, expectedDisbursementDate, repaymentsStartingFromDate, calculatedRepaymentsStartingFromDate,
                graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged, interestChargedFromDate, inArrearsToleranceMoney,
                loanProduct.isMultiDisburseLoan(), emiAmount, disbursementDatas, maxOutstandingBalance, loanVariationTermsData,
                graceOnArrearsAgeing, daysInMonthType, daysInYearType, isInterestRecalculationEnabled, recalculationFrequencyType,
                restCalendarInstance, principalThresholdForLastInstalment, installmentAmountInMultiplesOf,
                loanProduct.preCloseInterestCalculationStrategy());
    }

    private CalendarInstance createInterestRecalculationCalendarInstance(final LocalDate calendarStartDate,
            final RecalculationFrequencyType recalculationFrequencyType, final Integer frequency) {
        final Integer repeatsOnDay = calendarStartDate.getDayOfWeek();

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

        final String title = "loan_recalculation_detail";
        final Calendar calendar = Calendar.createRepeatingCalendar(title, calendarStartDate, CalendarType.COLLECTION.getValue(),
                calendarFrequencyType, frequency, repeatsOnDay);
        return CalendarInstance.from(calendar, null, CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());
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
                    disbursementDatas.add(new DisbursementData(null, expectedDisbursementDate, null, principal));
                    i++;
                } while (i < disbursementDataArray.size());
            }
        }
        return disbursementDatas;
    }

    private void validateRepaymentsStartDateWithMeetingDates(final LocalDate repaymentsStartingFromDate, final Calendar calendar) {
        if (repaymentsStartingFromDate != null
                && !CalendarUtils.isValidRedurringDate(calendar.getRecurrence(), calendar.getStartDateLocalDate(),
                        repaymentsStartingFromDate)) {
            final String errorMessage = "First repayment date '" + repaymentsStartingFromDate + "' do not fall on a meeting date";
            throw new LoanApplicationDateException("first.repayment.date.do.not.match.meeting.date", errorMessage,
                    repaymentsStartingFromDate);
        }
    }

    public void validateDisbursementDateWithMeetingDates(final LocalDate expectedDisbursementDate, final Calendar calendar) {
        // disbursement date should fall on a meeting date
        if (!calendar.isValidRecurringDate(expectedDisbursementDate)) {
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

        return assembleLoanScheduleFrom(loanApplicationTerms, isHolidayEnabled, holidays, workingDays, element);
    }

    public LoanScheduleModel assembleLoanScheduleFrom(final LoanApplicationTerms loanApplicationTerms, final boolean isHolidayEnabled,
            final List<Holiday> holidays, final WorkingDays workingDays, final JsonElement element) {

        final Set<LoanCharge> loanCharges = this.loanChargeAssembler.fromParsedJson(element);

        final LoanScheduleGenerator loanScheduleGenerator = this.loanScheduleFactory.create(loanApplicationTerms.getInterestMethod());

        final RoundingMode roundingMode = RoundingMode.HALF_EVEN;
        final MathContext mc = new MathContext(8, roundingMode);

        HolidayDetailDTO detailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);

        return loanScheduleGenerator.generate(mc, loanApplicationTerms, loanCharges, detailDTO);
    }

    public LoanScheduleModel assembleForInterestRecalculation(final LoanApplicationTerms loanApplicationTerms, final Long officeId,
            List<LoanTransaction> transactions, final Set<LoanCharge> loanCharges,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor,
            LocalDate recalculateDueDateChargesFrom) {
        final RoundingMode roundingMode = RoundingMode.HALF_EVEN;
        final MathContext mc = new MathContext(8, roundingMode);
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();

        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(officeId, loanApplicationTerms
                .getExpectedDisbursementDate().toDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();

        final LoanScheduleGenerator loanScheduleGenerator = this.loanScheduleFactory.create(loanApplicationTerms.getInterestMethod());
        HolidayDetailDTO detailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);
        return loanScheduleGenerator.rescheduleNextInstallments(mc, loanApplicationTerms, loanCharges, detailDTO, transactions,
                loanRepaymentScheduleTransactionProcessor, recalculateDueDateChargesFrom);
    }

    public LoanRepaymentScheduleInstallment calculatePrepaymentAmount(List<LoanRepaymentScheduleInstallment> installments,
            MonetaryCurrency currency, LocalDate onDate, LoanApplicationTerms loanApplicationTerms, final Set<LoanCharge> loanCharges,
            final Long officeId) {
        final LoanScheduleGenerator loanScheduleGenerator = this.loanScheduleFactory.create(loanApplicationTerms.getInterestMethod());
        final RoundingMode roundingMode = RoundingMode.HALF_EVEN;
        final MathContext mc = new MathContext(8, roundingMode);

        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(officeId, loanApplicationTerms
                .getExpectedDisbursementDate().toDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays);

        return loanScheduleGenerator.calculatePrepaymentAmount(installments, currency, onDate, loanApplicationTerms, mc, loanCharges,
                holidayDetailDTO);

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

        if ((loanType.isJLGAccount() || loanType.isGroupAccount()) && calendar != null) {

            final LocalDate refernceDateForCalculatingFirstRepaymentDate = expectedDisbursementDate;
            derivedFirstRepayment = deriveFirstRepaymentDateForJLGLoans(repaymentEvery, expectedDisbursementDate,
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

    private LocalDate deriveFirstRepaymentDateForJLGLoans(final Integer repaymentEvery, final LocalDate expectedDisbursementDate,
            final LocalDate refernceDateForCalculatingFirstRepaymentDate, final PeriodFrequencyType repaymentPeriodFrequencyType,
            final Integer minimumDaysBetweenDisbursalAndFirstRepayment, final Calendar calendar) {

        final String frequency = CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(repaymentPeriodFrequencyType);
        final LocalDate derivedFirstRepayment = CalendarUtils.getFirstRepaymentMeetingDate(calendar,
                refernceDateForCalculatingFirstRepaymentDate, repaymentEvery, frequency);
        final LocalDate minimumFirstRepaymentDate = expectedDisbursementDate.plusDays(minimumDaysBetweenDisbursalAndFirstRepayment);
        return minimumFirstRepaymentDate.isBefore(derivedFirstRepayment) ? derivedFirstRepayment : deriveFirstRepaymentDateForJLGLoans(
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