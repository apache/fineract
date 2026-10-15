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
package org.apache.fineract.portfolio.loanaccount.payoffpreview.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.DefaultScheduledDateGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.ProgressiveLoanScheduleGenerator;
import org.apache.fineract.portfolio.loanaccount.payoffpreview.data.PayoffScheduleData;
import org.apache.fineract.portfolio.loanaccount.payoffpreview.data.PayoffScheduleInstallmentData;
import org.apache.fineract.portfolio.loanaccount.payoffpreview.validator.PayoffSchedulePreviewParseAndValidator;
import org.apache.fineract.portfolio.loanaccount.service.InterestScheduleModelRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.calc.ProgressiveEMICalculator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class PayoffSchedulePreviewServiceImplTest {

    private final ConfigurationDomainService configurationDomainService = mock(ConfigurationDomainService.class);
    private PayoffSchedulePreviewService service;
    private MockedStatic<MoneyHelper> moneyHelper;

    @BeforeEach
    void setUp() {
        moneyHelper = Mockito.mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(19, RoundingMode.HALF_EVEN));
        when(configurationDomainService.retrievePayoffSchedulePreviewMaxTermPeriods()).thenReturn(1200L);
        when(configurationDomainService.retrievePayoffSchedulePreviewMaxPayoffPeriods()).thenReturn(1200L);

        final DefaultScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
        final ProgressiveEMICalculator emiCalculator = new ProgressiveEMICalculator(scheduledDateGenerator);
        final CurrencyReadPlatformService currencyReadPlatformService = mock(CurrencyReadPlatformService.class);
        final ApplicationCurrencyRepository applicationCurrencyRepository = mock(ApplicationCurrencyRepository.class);
        when(applicationCurrencyRepository.findCurrencyDataByCode("USD")).thenReturn(new CurrencyData("USD", 2, null));
        service = new PayoffSchedulePreviewServiceImpl(
                new PayoffSchedulePreviewParseAndValidator(new FromJsonHelper(), currencyReadPlatformService,
                        applicationCurrencyRepository),
                new ProgressiveLoanScheduleGenerator(scheduledDateGenerator, emiCalculator,
                        mock(InterestScheduleModelRepositoryWrapper.class)),
                emiCalculator, configurationDomainService);
    }

    @AfterEach
    void tearDown() {
        moneyHelper.close();
    }

    /**
     * Same inputs and expected values as {@code EmbeddableProgressiveLoanScheduleGeneratorTest}, which runs the regular
     * progressive schedule generator: 100 at 7% over six months, 360/30.
     */
    @Test
    void termScheduleMatchesTheRegularProgressiveScheduleGenerator() {
        final PayoffScheduleData result = service.generatePreview(
                termRequest("100", "7", "P6M", "P1M", LocalDate.of(2024, 1, 1), DaysInYearType.DAYS_360, DaysInMonthType.DAYS_30));

        assertEquals(6, result.getNumberOfInstallments());
        assertInstallment(result.getInstallments().get(0), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), "16.43", "0.58", "17.01",
                "83.57");
        assertInstallment(result.getInstallments().get(1), LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), "16.52", "0.49", "17.01",
                "67.05");
        assertInstallment(result.getInstallments().get(2), LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1), "16.62", "0.39", "17.01",
                "50.43");
        assertInstallment(result.getInstallments().get(3), LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1), "16.72", "0.29", "17.01",
                "33.71");
        assertInstallment(result.getInstallments().get(4), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1), "16.81", "0.20", "17.01",
                "16.90");
        assertInstallment(result.getInstallments().get(5), LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1), "16.90", "0.10", "17.00",
                "0");
        assertAmount("100", result.getTotalPrincipal());
        assertAmount("2.05", result.getTotalInterest());
        assertAmount("102.05", result.getTotalRepayment());
        assertEquals("P6M", result.getTerm());
        assertEquals("DECLINING_BALANCE", result.getInterestMethod());
    }

    @Test
    void termScheduleWithActualDayCounts() {
        final PayoffScheduleData result = service.generatePreview(
                termRequest("100", "7", "P6M", "P1M", LocalDate.of(2024, 1, 1), DaysInYearType.ACTUAL, DaysInMonthType.ACTUAL));

        assertEquals(6, result.getNumberOfInstallments());
        assertInstallment(result.getInstallments().get(0), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), "16.42", "0.59", "17.01",
                "83.58");
        assertAmount("2.04", result.getTotalInterest());
        assertAmount("102.04", result.getTotalRepayment());
    }

    @Test
    void termEqualToOneFrequencyGivesSingleInstallment() {
        final PayoffScheduleData result = service.generatePreview(
                termRequest("100", "0", "P1M", "P1M", LocalDate.of(2024, 1, 1), DaysInYearType.ACTUAL, DaysInMonthType.ACTUAL));

        assertEquals(1, result.getNumberOfInstallments());
        assertInstallment(result.getInstallments().get(0), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), "100", "0", "100", "0");
    }

    @Test
    void monthEndStartDateKeepsMonthEndDueDates() {
        final PayoffScheduleData result = service.generatePreview(
                termRequest("100", "0", "P3M", "P1M", LocalDate.of(2024, 1, 31), DaysInYearType.ACTUAL, DaysInMonthType.ACTUAL));

        assertEquals(List.of(LocalDate.of(2024, 2, 29), LocalDate.of(2024, 3, 31), LocalDate.of(2024, 4, 30)),
                result.getInstallments().stream().map(PayoffScheduleInstallmentData::getDueDate).toList());
    }

    @Test
    void termLongerThanTheConfiguredMaximumIsRejected() {
        when(configurationDomainService.retrievePayoffSchedulePreviewMaxTermPeriods()).thenReturn(5L);

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class, () -> service
                .generatePreview(termRequest("100", "7", "P6M", "P1M", LocalDate.of(2024, 1, 1), DaysInYearType.ACTUAL, DaysInMonthType.ACTUAL)));

        assertSingleError(exception, "validation.msg.payoffSchedulePreview.term.exceeds.max.term.periods", "term");
    }

    @Test
    void fixedPaymentWithoutInterestPaysOffExactly() {
        final PayoffScheduleData result = service.generatePreview(
                fixedPaymentRequest("100", "30", "0", "P1M", LocalDate.of(2024, 1, 1), DaysInYearType.ACTUAL, DaysInMonthType.ACTUAL));

        assertEquals(4, result.getNumberOfInstallments());
        assertNull(result.getTerm());
        assertInstallment(result.getInstallments().get(0), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), "30", "0", "30", "70");
        assertInstallment(result.getInstallments().get(3), LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1), "10", "0", "10", "0");
        assertAmount("100", result.getTotalPrincipal());
        assertAmount("100", result.getTotalRepayment());
    }

    @Test
    void fixedPaymentWithInterestOnThirtyDayMonths() {
        final PayoffScheduleData result = service.generatePreview(fixedPaymentRequest("1000", "200", "12", "P1M", LocalDate.of(2024, 1, 1),
                DaysInYearType.DAYS_360, DaysInMonthType.DAYS_30));

        assertEquals(6, result.getNumberOfInstallments());
        assertInstallment(result.getInstallments().get(0), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), "190", "10", "200", "810");
        assertInstallment(result.getInstallments().get(5), LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1), "30.81", "0.31", "31.12",
                "0");
        assertAmount("31.12", result.getTotalInterest());
    }

    /**
     * A payment that covers February's interest but not March's must be rejected instead of producing negative
     * principal.
     */
    @Test
    void fixedPaymentNotCoveringALaterPeriodsInterestIsRejected() {
        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> service.generatePreview(fixedPaymentRequest("1000", "10.10", "12", "P1M", LocalDate.of(2024, 2, 1),
                        DaysInYearType.ACTUAL, DaysInMonthType.ACTUAL)));

        assertSingleError(exception, "validation.msg.payoffSchedulePreview.fixedPaymentAmount.does.not.cover.interest",
                "fixedPaymentAmount");
        assertTrue(exception.getErrors().getFirst().getDefaultUserMessage().contains("installment 2"));
    }

    @Test
    void fixedPaymentExceedingTheConfiguredMaximumIsRejected() {
        when(configurationDomainService.retrievePayoffSchedulePreviewMaxPayoffPeriods()).thenReturn(5L);

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> service.generatePreview(
                        fixedPaymentRequest("100", "10", "0", "P1M", LocalDate.of(2024, 1, 1), DaysInYearType.ACTUAL, DaysInMonthType.ACTUAL)));

        assertSingleError(exception, "validation.msg.payoffSchedulePreview.fixedPaymentAmount.exceeds.max.payoff.periods",
                "fixedPaymentAmount");
    }

    @Test
    void missingMaximumConfigurationIsAConfigurationError() {
        when(configurationDomainService.retrievePayoffSchedulePreviewMaxTermPeriods()).thenReturn(null);
        when(configurationDomainService.retrievePayoffSchedulePreviewMaxPayoffPeriods()).thenReturn(null);

        final GeneralPlatformDomainRuleException termException = assertThrows(GeneralPlatformDomainRuleException.class, () -> service
                .generatePreview(termRequest("100", "0", "P10M", "P1M", LocalDate.of(2024, 1, 1), DaysInYearType.ACTUAL, DaysInMonthType.ACTUAL)));
        assertEquals("error.msg.configuration.payoff-schedule-preview-max-term-periods.value.is.not.set",
                termException.getGlobalisationMessageCode());

        final GeneralPlatformDomainRuleException fixedPaymentException = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> service.generatePreview(
                        fixedPaymentRequest("100", "10", "0", "P1M", LocalDate.of(2024, 1, 1), DaysInYearType.ACTUAL, DaysInMonthType.ACTUAL)));
        assertEquals("error.msg.configuration.payoff-schedule-preview-max-payoff-periods.value.is.not.set",
                fixedPaymentException.getGlobalisationMessageCode());
    }

    @Test
    void maximumOfZeroRejectsEverySchedule() {
        when(configurationDomainService.retrievePayoffSchedulePreviewMaxTermPeriods()).thenReturn(0L);
        when(configurationDomainService.retrievePayoffSchedulePreviewMaxPayoffPeriods()).thenReturn(0L);

        assertSingleError(assertThrows(PlatformApiDataValidationException.class, () -> service
                .generatePreview(termRequest("100", "0", "P1M", "P1M", LocalDate.of(2024, 1, 1), DaysInYearType.ACTUAL, DaysInMonthType.ACTUAL))),
                "validation.msg.payoffSchedulePreview.term.exceeds.max.term.periods", "term");
        assertSingleError(assertThrows(PlatformApiDataValidationException.class, () -> service.generatePreview(
                fixedPaymentRequest("100", "100", "0", "P1M", LocalDate.of(2024, 1, 1), DaysInYearType.ACTUAL, DaysInMonthType.ACTUAL))),
                "validation.msg.payoffSchedulePreview.fixedPaymentAmount.exceeds.max.payoff.periods", "fixedPaymentAmount");
    }

    /**
     * Paying the term schedule's installment amount as a fixed payment must reproduce the term schedule, so both modes
     * agree with the engine on dates and interest. Only the last term installment can differ: it absorbs the rounding
     * of the installment amount, so when it is larger the fixed payment leaves a small remainder for one more
     * installment.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("crossModeCases")
    void fixedPaymentOfTheTermInstallmentReproducesTheTermSchedule(final String name, final String rate, final String term,
            final String frequency, final LocalDate startDate, final DaysInYearType daysInYearType, final DaysInMonthType daysInMonthType) {
        final PayoffScheduleData termSchedule = service
                .generatePreview(termRequest("100000", rate, term, frequency, startDate, daysInYearType, daysInMonthType));
        final String installmentAmount = termSchedule.getInstallments().getFirst().getTotalDue().toPlainString();

        final PayoffScheduleData fixedPaymentSchedule = service.generatePreview(
                fixedPaymentRequest("100000", installmentAmount, rate, frequency, startDate, daysInYearType, daysInMonthType));

        final int lastTermIndex = termSchedule.getNumberOfInstallments() - 1;
        assertTrue(fixedPaymentSchedule.getNumberOfInstallments() - lastTermIndex == 1
                || fixedPaymentSchedule.getNumberOfInstallments() - lastTermIndex == 2, "number of installments");
        for (int i = 0; i <= lastTermIndex; i++) {
            final PayoffScheduleInstallmentData expected = termSchedule.getInstallments().get(i);
            final PayoffScheduleInstallmentData actual = fixedPaymentSchedule.getInstallments().get(i);
            final String installment = "installment " + (i + 1);
            assertEquals(expected.getFromDate(), actual.getFromDate(), installment);
            assertEquals(expected.getDueDate(), actual.getDueDate(), installment);
            assertAmount(expected.getInterestDue().toPlainString(), actual.getInterestDue());
            if (i < lastTermIndex) {
                assertAmount(expected.getPrincipalDue().toPlainString(), actual.getPrincipalDue());
                assertAmount(expected.getOutstandingPrincipalBalance().toPlainString(), actual.getOutstandingPrincipalBalance());
            }
        }
        final BigDecimal remainingPrincipal = fixedPaymentSchedule.getInstallments()
                .subList(lastTermIndex, fixedPaymentSchedule.getNumberOfInstallments()).stream()
                .map(PayoffScheduleInstallmentData::getPrincipalDue).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertAmount(termSchedule.getInstallments().get(lastTermIndex).getPrincipalDue().toPlainString(), remainingPrincipal);
    }

    private static Stream<Arguments> crossModeCases() {
        return Stream.of(
                Arguments.of("monthly, actual/actual", "7", "P6M", "P1M", LocalDate.of(2024, 1, 1), DaysInYearType.ACTUAL,
                        DaysInMonthType.ACTUAL),
                Arguments.of("monthly, 360/30", "7", "P6M", "P1M", LocalDate.of(2024, 1, 1), DaysInYearType.DAYS_360,
                        DaysInMonthType.DAYS_30),
                Arguments.of("monthly across a year boundary", "12", "P12M", "P1M", LocalDate.of(2023, 12, 15), DaysInYearType.ACTUAL,
                        DaysInMonthType.ACTUAL),
                Arguments.of("month-end start, actual/actual", "12", "P6M", "P1M", LocalDate.of(2024, 1, 31), DaysInYearType.ACTUAL,
                        DaysInMonthType.ACTUAL),
                Arguments.of("month-end start, 360/30", "12", "P6M", "P1M", LocalDate.of(2024, 1, 31), DaysInYearType.DAYS_360,
                        DaysInMonthType.DAYS_30),
                Arguments.of("bi-weekly across a year boundary", "9.5", "P12W", "P2W", LocalDate.of(2023, 12, 20), DaysInYearType.ACTUAL,
                        DaysInMonthType.ACTUAL),
                Arguments.of("ten-daily, 365", "9.5", "P90D", "P10D", LocalDate.of(2023, 12, 25), DaysInYearType.DAYS_365,
                        DaysInMonthType.ACTUAL),
                Arguments.of("weekly, 364", "36", "P52W", "P1W", LocalDate.of(2024, 3, 1), DaysInYearType.DAYS_364, DaysInMonthType.ACTUAL),
                Arguments.of("zero interest", "0", "P6M", "P1M", LocalDate.of(2024, 1, 1), DaysInYearType.ACTUAL, DaysInMonthType.ACTUAL));
    }

    private static String termRequest(final String amount, final String rate, final String term, final String frequency,
            final LocalDate startDate, final DaysInYearType daysInYearType, final DaysInMonthType daysInMonthType) {
        final Map<String, Object> body = requestBody(amount, rate, frequency, startDate, daysInYearType, daysInMonthType);
        body.put("term", term);
        return new Gson().toJson(body);
    }

    private static String fixedPaymentRequest(final String amount, final String fixedPaymentAmount, final String rate,
            final String frequency, final LocalDate startDate, final DaysInYearType daysInYearType, final DaysInMonthType daysInMonthType) {
        final Map<String, Object> body = requestBody(amount, rate, frequency, startDate, daysInYearType, daysInMonthType);
        body.put("fixedPaymentAmount", new BigDecimal(fixedPaymentAmount));
        return new Gson().toJson(body);
    }

    private static Map<String, Object> requestBody(final String amount, final String rate, final String frequency,
            final LocalDate startDate, final DaysInYearType daysInYearType, final DaysInMonthType daysInMonthType) {
        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", new BigDecimal(amount));
        body.put("frequency", frequency);
        body.put("annualNominalInterestRate", new BigDecimal(rate));
        body.put("currencyCode", "USD");
        body.put("startDate", startDate.toString());
        body.put("dateFormat", "yyyy-MM-dd");
        body.put("locale", "en");
        body.put("daysInYearType", daysInYearType.getValue());
        body.put("daysInMonthType", daysInMonthType.getValue());
        return body;
    }

    private static void assertInstallment(final PayoffScheduleInstallmentData installment, final LocalDate fromDate,
            final LocalDate dueDate, final String principal, final String interest, final String total, final String balance) {
        assertEquals(fromDate, installment.getFromDate());
        assertEquals(dueDate, installment.getDueDate());
        assertAmount(principal, installment.getPrincipalDue());
        assertAmount(interest, installment.getInterestDue());
        assertAmount(total, installment.getTotalDue());
        assertAmount(balance, installment.getOutstandingPrincipalBalance());
    }

    private static void assertAmount(final String expected, final BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }

    private static void assertSingleError(final PlatformApiDataValidationException exception, final String code, final String parameter) {
        assertEquals(1, exception.getErrors().size());
        final ApiParameterError error = exception.getErrors().getFirst();
        assertEquals(code, error.getUserMessageGlobalisationCode());
        assertEquals(parameter, error.getParameterName());
    }
}
