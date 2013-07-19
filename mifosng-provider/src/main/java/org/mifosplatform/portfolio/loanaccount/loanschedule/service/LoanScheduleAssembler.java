/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.holiday.domain.HolidayRepository;
import org.mifosplatform.organisation.holiday.service.HolidayUtil;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.organisation.workingdays.service.WorkingDaysUtil;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarRepository;
import org.mifosplatform.portfolio.calendar.exception.CalendarNotFoundException;
import org.mifosplatform.portfolio.calendar.exception.MeetingFrequencyMismatchException;
import org.mifosplatform.portfolio.calendar.service.CalendarHelper;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepositoryWrapper;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.exception.LoanApplicationDateException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.mifosplatform.portfolio.loanaccount.service.LoanChargeAssembler;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

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
    private final ClientRepositoryWrapper  clientRepository;
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
        final Long loanProductId = fromApiJsonHelper.extractLongNamed("productId", element);

        final LoanProduct loanProduct = this.loanProductRepository.findOne(loanProductId);
        if (loanProduct == null) { throw new LoanProductNotFoundException(loanProductId); }

        return assembleLoanApplicationTermsFrom(element, loanProduct);
    }

    private LoanApplicationTerms assembleLoanApplicationTermsFrom(final JsonElement element, final LoanProduct loanProduct) {

        final MonetaryCurrency currency = loanProduct.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        // loan terms
        final Integer loanTermFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed("loanTermFrequency", element);
        final Integer loanTermFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("loanTermFrequencyType", element);
        final PeriodFrequencyType loanTermPeriodFrequencyType = PeriodFrequencyType.fromInt(loanTermFrequencyType);

        final Integer numberOfRepayments = fromApiJsonHelper.extractIntegerWithLocaleNamed("numberOfRepayments", element);
        final Integer repaymentEvery = fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentEvery", element);
        final Integer repaymentFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentFrequencyType", element);
        final PeriodFrequencyType repaymentPeriodFrequencyType = PeriodFrequencyType.fromInt(repaymentFrequencyType);

        final Integer amortizationType = fromApiJsonHelper.extractIntegerWithLocaleNamed("amortizationType", element);
        final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(amortizationType);

        // interest terms
        final Integer interestType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestType", element);
        final InterestMethod interestMethod = InterestMethod.fromInt(interestType);

        final Integer interestCalculationPeriodType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestCalculationPeriodType",
                element);
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod
                .fromInt(interestCalculationPeriodType);

        final BigDecimal interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRatePerPeriod", element);
        final PeriodFrequencyType interestRatePeriodFrequencyType = loanProduct.getInterestPeriodFrequencyType();

        final BigDecimal annualNominalInterestRate = this.aprCalculator.calculateFrom(interestRatePeriodFrequencyType,
                interestRatePerPeriod);

        // disbursement details
        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        final Money principalMoney = Money.of(currency, principal);
        
        final LocalDate expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed("expectedDisbursementDate", element);
        final LocalDate repaymentsStartingFromDate = this.fromApiJsonHelper.extractLocalDateNamed("repaymentsStartingFromDate", element);
        LocalDate calculatedRepaymentsStartingFromDate = repaymentsStartingFromDate;
        final Boolean synchDisbursement = fromApiJsonHelper.extractBooleanNamed("syncDisbursementWithMeeting", element);
        final Long calendarId = this.fromApiJsonHelper.extractLongNamed("calendarId", element);
        Calendar calendar = null;
        if ((synchDisbursement != null && synchDisbursement.booleanValue()) || (calendarId != null && calendarId != 0)) {
            calendar = this.calendarRepository.findOne(calendarId);
            if (calendar == null) { throw new CalendarNotFoundException(calendarId); }

            // validate repayment frequency and interval with meeting frequency and interval
            PeriodFrequencyType meetingPeriodFrequency = CalendarHelper.getMeetingPeriodFrequencyType(calendar.getRecurrence());
            validateRepaymentFrequencyIsSameAsMeetingFrequency(meetingPeriodFrequency.getValue(), repaymentFrequencyType,
                    CalendarHelper.getInterval(calendar.getRecurrence()), repaymentEvery);
        }
        
        if (synchDisbursement != null && synchDisbursement.booleanValue()) {
            validateDisbursementDateWithMeetingDates(expectedDisbursementDate, calendar);
        }
	
        // if calendar is attached (not null) then reschedule repayment dates according to meeting
        if (null != calendar) {
            // TODO : AA - Is it require to reset repaymentsStartingFromDate or
            // set only if it is not provided (or null)
            // Currently provided repaymentsStartingFromDate takes precedence over system generated next meeting date
            if (calculatedRepaymentsStartingFromDate == null) {
                // FIXME: AA - Possibility of having next meeting date immediately after disbursement date,
                // need to have minimum number of days gap between disbursement and first repayment date.
                final String frequency = CalendarHelper.getMeetingFrequencyFromPeriodFrequencyType(repaymentPeriodFrequencyType);
                calculatedRepaymentsStartingFromDate = CalendarHelper.getFirstRepaymentMeetingDate(calendar, expectedDisbursementDate,
                        repaymentEvery, frequency);
            } else {// validate user provided repaymentsStartFromDate
                validateRepaymentsStartDateWithMeetingDates(repaymentsStartingFromDate, calendar);
            }
        }
        // grace details
        final Integer graceOnPrincipalPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnPrincipalPayment", element);
        final Integer graceOnInterestPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestPayment", element);
        final Integer graceOnInterestCharged = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestCharged", element);
        final LocalDate interestChargedFromDate = fromApiJsonHelper.extractLocalDateNamed("interestChargedFromDate", element);

        // other
        final BigDecimal inArrearsTolerance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("inArrearsTolerance", element);
        final Money inArrearsToleranceMoney = Money.of(currency, inArrearsTolerance);

        return LoanApplicationTerms.assembleFrom(applicationCurrency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentPeriodFrequencyType, amortizationMethod, interestMethod, interestRatePerPeriod,
                interestRatePeriodFrequencyType, annualNominalInterestRate, interestCalculationPeriodMethod, principalMoney,
                expectedDisbursementDate, repaymentsStartingFromDate, calculatedRepaymentsStartingFromDate, graceOnPrincipalPayment, graceOnInterestPayment,
                graceOnInterestCharged, interestChargedFromDate, inArrearsToleranceMoney);
    }
    
    private void validateRepaymentsStartDateWithMeetingDates(final LocalDate repaymentsStartingFromDate, final Calendar calendar) {
        if (repaymentsStartingFromDate != null
                && !CalendarHelper.isValidRedurringDate(calendar.getRecurrence(), calendar.getStartDateLocalDate(),
                        repaymentsStartingFromDate)) {
            final String errorMessage = "First repayment date '" + repaymentsStartingFromDate + "' do not fall on a meeting date";
            throw new LoanApplicationDateException("first.repayment.date.do.not.match.meeting.date", errorMessage,
                    repaymentsStartingFromDate);
        }
    }

    private void validateDisbursementDateWithMeetingDates(final LocalDate expectedDisbursementDate, final Calendar calendar) {
        // disbursement date should fall on a meeting date
        if (!CalendarHelper.isValidRedurringDate(calendar.getRecurrence(), calendar.getStartDateLocalDate(), expectedDisbursementDate)) {
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
             // repayment frequency is same as meeting frequency repayment interval should be same or multiple of meeting interval
                if (repaymentInterval % meetingInterval != 0) {
                    // throw exception: Loan product frequency/interval
                    throw new MeetingFrequencyMismatchException("loanapplication.repayment.interval",
                            "Loan repayment repaid every # must equal or multiple of meeting interval", repaymentInterval);
                }
            }
        }
    }
    
    public LoanProductRelatedDetail assembleLoanProductRelatedDetail(final JsonElement element) {
        LoanApplicationTerms loanApplicationTerms = assembleLoanTerms(element);
        return loanApplicationTerms.toLoanProductRelatedDetail();
    }

    public LoanScheduleModel assembleLoanScheduleFrom(final JsonElement element) {
        //This method is getting called from calculate loan schedule.
        final LoanApplicationTerms loanApplicationTerms = assembleLoanTerms(element);
        //Get holiday details
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        
        final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
        final Long groupId = fromApiJsonHelper.extractLongNamed("groupId", element);
        
        Client client = null;
        Group group = null;
        Long officeId = null;
        if(clientId != null){
            client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            officeId = client.getOffice().getId();
        }else if (groupId != null){
            group = this.groupRepository.findOneWithNotFoundDetection(groupId);
            officeId = group.getOffice().getId();
        }
        
        final LocalDate expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed("expectedDisbursementDate", element);
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(officeId, expectedDisbursementDate.toDate());
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

        final MonetaryCurrency currency = loanApplicationTerms.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        return loanScheduleGenerator.generate(mc, applicationCurrency, loanApplicationTerms, loanCharges, isHolidayEnabled, holidays, workingDays);
    }
    
    private void validateDisbursementDateIsOnNonWorkingDay(final LocalDate disbursementDate, final WorkingDays workingDays) {
        if(!WorkingDaysUtil.isWorkingDay(workingDays, disbursementDate)){
            final String errorMessage = "The expected disbursement date cannot be on a non working day";
            throw new LoanApplicationDateException("disbursement.date.on.non.working.day", errorMessage, disbursementDate);
        }
    }

    private void validateDisbursementDateIsOnHoliday(final LocalDate disbursementDate, final boolean isHolidayEnabled, final List<Holiday> holidays) {
        if (isHolidayEnabled) {
            if (HolidayUtil.isHoliday(disbursementDate, holidays)) {
                final String errorMessage = "The expected disbursement date cannot be on a holiday";
                throw new LoanApplicationDateException("disbursement.date.on.holiday", errorMessage, disbursementDate);
            }
        }
    }
}