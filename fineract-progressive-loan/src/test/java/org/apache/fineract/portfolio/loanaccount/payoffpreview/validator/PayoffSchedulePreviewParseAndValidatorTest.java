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
package org.apache.fineract.portfolio.loanaccount.payoffpreview.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.payoffpreview.data.PayoffSchedulePreviewParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PayoffSchedulePreviewParseAndValidatorTest {

    private static final CurrencyData USD = new CurrencyData("USD", 2, null);
    private static final CurrencyData EUR = new CurrencyData("EUR", 2, null);
    private static final CurrencyData JPY = new CurrencyData("JPY", 0, null);

    private final CurrencyReadPlatformService currencyReadPlatformService = mock(CurrencyReadPlatformService.class);
    private final ApplicationCurrencyRepository applicationCurrencyRepository = mock(ApplicationCurrencyRepository.class);
    private final PayoffSchedulePreviewParseAndValidator validator = new PayoffSchedulePreviewParseAndValidator(new FromJsonHelper(),
            currencyReadPlatformService, applicationCurrencyRepository);

    @BeforeEach
    void setUp() {
        when(currencyReadPlatformService.retrieveAllowedCurrencies()).thenReturn(List.of(USD));
        List.of(USD, EUR, JPY).forEach(currency -> when(applicationCurrencyRepository.findCurrencyDataByCode(currency.getCode())).thenReturn(currency));
    }

    @Test
    void termRequestIsParsedWithDefaults() {
        final Map<String, Object> body = termBody();
        body.remove("annualNominalInterestRate");

        final PayoffSchedulePreviewParameters parameters = validator.validateAndParse(json(body));

        assertEquals(0, new BigDecimal("1000").compareTo(parameters.amount()));
        assertEquals(PeriodFrequencyType.MONTHS, parameters.term().unit());
        assertEquals(6, parameters.term().value());
        assertEquals("P1M", parameters.frequency().iso());
        assertNull(parameters.fixedPaymentAmount());
        assertEquals(0, BigDecimal.ZERO.compareTo(parameters.annualNominalInterestRate()));
        assertEquals("USD", parameters.currency().getCode());
        assertEquals(2, parameters.currency().getDecimalPlaces());
        assertEquals(LocalDate.of(2024, 1, 1), parameters.startDate());
        assertEquals(DaysInYearType.ACTUAL, parameters.daysInYearType());
        assertEquals(DaysInMonthType.ACTUAL, parameters.daysInMonthType());
    }

    @Test
    void fixedPaymentRequestIsParsed() {
        final Map<String, Object> body = termBody();
        body.remove("term");
        body.put("fixedPaymentAmount", 200);
        body.put("daysInYearType", 360);
        body.put("daysInMonthType", 30);

        final PayoffSchedulePreviewParameters parameters = validator.validateAndParse(json(body));

        assertNull(parameters.term());
        assertEquals(0, new BigDecimal("200").compareTo(parameters.fixedPaymentAmount()));
        assertEquals(DaysInYearType.DAYS_360, parameters.daysInYearType());
        assertEquals(DaysInMonthType.DAYS_30, parameters.daysInMonthType());
    }

    @Test
    void termEqualToOneFrequencyIsAccepted() {
        final Map<String, Object> body = termBody();
        body.put("term", "P1M");

        assertEquals(1, validator.validateAndParse(json(body)).term().value());
    }

    @Test
    void interestMethodIsCaseInsensitive() {
        final Map<String, Object> body = termBody();
        body.put("interestMethod", "declining_balance");

        validator.validateAndParse(json(body));
    }

    @Test
    void allValidationErrorsAreReportedTogether() {
        final Map<String, Object> body = termBody();
        body.put("amount", 0);
        body.put("decimalPlaces", 9);
        body.put("interestMethod", "FLAT");
        body.remove("startDate");

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.amount.not.greater.than.zero",
                "validation.msg.payoffSchedulePreview.interestMethod.is.not.one.of.expected.enumerations",
                "validation.msg.payoffSchedulePreview.decimalPlaces.is.not.within.expected.range",
                "validation.msg.payoffSchedulePreview.startDate.cannot.be.blank");
    }

    /**
     * Small integer fields are read without the locale, as the loan path reads its type codes, so a request without a
     * locale still gets every validation error rather than a single missing-locale error.
     */
    @Test
    void allValidationErrorsAreReportedWithoutALocale() {
        final Map<String, Object> body = termBody();
        body.put("amount", 0);
        body.put("decimalPlaces", 9);
        body.put("daysInYearType", 999);
        body.remove("startDate");
        body.remove("dateFormat");
        body.remove("locale");

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.amount.not.greater.than.zero",
                "validation.msg.payoffSchedulePreview.decimalPlaces.is.not.within.expected.range",
                "validation.msg.payoffSchedulePreview.daysInYearType.is.not.one.of.expected.enumerations",
                "validation.msg.payoffSchedulePreview.startDate.cannot.be.blank");
    }

    @Test
    void missingAmountIsReportedAsMandatory() {
        final Map<String, Object> body = termBody();
        body.remove("amount");

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.amount.cannot.be.blank");
    }

    @Test
    void termOrFixedPaymentAmountIsRequired() {
        final Map<String, Object> body = termBody();
        body.remove("term");

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.term.or.fixedPaymentAmount.required");
    }

    @Test
    void termAndFixedPaymentAmountAreMutuallyExclusive() {
        final Map<String, Object> body = termBody();
        body.put("fixedPaymentAmount", 200);

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.fixedPaymentAmount.cannot.also.be.provided.when.term.is.populated");
    }

    @Test
    void fixedPaymentAmountMustBePositive() {
        final Map<String, Object> body = termBody();
        body.remove("term");
        body.put("fixedPaymentAmount", 0);

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.fixedPaymentAmount.not.greater.than.zero");
    }

    @ParameterizedTest
    @ValueSource(strings = { "P0D", "P0M", "P10000M", "P99999999999W", "P1Y", "P1M10D", "1M", "P1.5M" })
    void frequencyMustBeAPositiveSingleUnitPeriod(final String frequency) {
        final Map<String, Object> body = termBody();
        body.put("frequency", frequency);

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.frequency.does.not.match.regexp");
    }

    @Test
    void missingFrequencyIsReportedAsMandatory() {
        final Map<String, Object> body = termBody();
        body.remove("frequency");

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.frequency.cannot.be.blank");
    }

    @Test
    void termMustUseTheFrequencyUnit() {
        final Map<String, Object> body = termBody();
        body.put("frequency", "P10D");

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.term.not.the.same.unit.as.frequency");
    }

    @ParameterizedTest
    @ValueSource(strings = { "P5M", "P1M" })
    void termMustBeAnExactMultipleOfTheFrequency(final String term) {
        final Map<String, Object> body = termBody();
        body.put("term", term);
        body.put("frequency", "P2M");

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.term.not.a.multiple.of.frequency");
    }

    @Test
    void negativeInterestRateIsRejected() {
        final Map<String, Object> body = termBody();
        body.put("annualNominalInterestRate", -1);

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.annualNominalInterestRate.not.zero.or.greater");
    }

    @Test
    void unknownDayCountTypesAreRejected() {
        final Map<String, Object> body = termBody();
        body.put("daysInYearType", 999);
        body.put("daysInMonthType", 31);

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.daysInYearType.is.not.one.of.expected.enumerations",
                "validation.msg.payoffSchedulePreview.daysInMonthType.is.not.one.of.expected.enumerations");
    }

    @Test
    void fractionalDecimalPlacesAreRejected() {
        final Map<String, Object> body = termBody();
        body.put("decimalPlaces", 2.5);

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateAndParse(json(body)));
        assertEquals("decimalPlaces", exception.getErrors().getFirst().getParameterName());
    }

    @Test
    void amountFinerThanTheCurrencyIsRejected() {
        final Map<String, Object> body = termBody();
        body.put("amount", new BigDecimal("0.004"));

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.amount.scale.is.greater.than.2");
    }

    @Test
    void fixedPaymentAmountFinerThanTheDecimalPlacesIsRejected() {
        final Map<String, Object> body = termBody();
        body.remove("term");
        body.put("fixedPaymentAmount", new BigDecimal("10.5"));
        body.put("decimalPlaces", 0);

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.fixedPaymentAmount.scale.is.greater.than.0");
    }

    @Test
    void amountFinerThanTheRequestedCurrencyIsRejected() {
        final Map<String, Object> body = termBody();
        body.put("currencyCode", "JPY");
        body.remove("decimalPlaces");
        body.put("amount", new BigDecimal("1000.5"));

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.amount.scale.is.greater.than.0");
    }

    @Test
    void trailingZerosDoNotCountAsDecimalPlaces() {
        final Map<String, Object> body = termBody();
        body.put("amount", new BigDecimal("1000.000"));

        assertEquals(0, new BigDecimal("1000").compareTo(validator.validateAndParse(json(body)).amount()));
    }

    @Test
    void numbersSentAsStringsAreParsedWithTheRequestLocale() {
        final Map<String, Object> body = termBody();
        body.put("amount", "1.000,50");
        body.put("locale", "de");

        assertEquals(0, new BigDecimal("1000.50").compareTo(validator.validateAndParse(json(body)).amount()));
    }

    @Test
    void jsonNumbersAreNotAffectedByTheLocale() {
        final Map<String, Object> body = termBody();
        body.put("amount", new BigDecimal("1000.5"));
        body.put("locale", "de");

        assertEquals(0, new BigDecimal("1000.5").compareTo(validator.validateAndParse(json(body)).amount()));
    }

    @Test
    void unknownParameterIsRejected() {
        final Map<String, Object> body = termBody();
        body.put("daysInYeartype", 360);

        final UnsupportedParameterException exception = assertThrows(UnsupportedParameterException.class,
                () -> validator.validateAndParse(json(body)));
        assertEquals(List.of("daysInYeartype"), exception.getUnsupportedParameters());
    }

    @Test
    void localeIsRequiredWithAStartDate() {
        final Map<String, Object> body = termBody();
        body.remove("locale");

        assertErrorCodes(body, "validation.msg.missing.locale.parameter");
    }

    @Test
    void dateFormatIsRequiredWithAStartDate() {
        final Map<String, Object> body = termBody();
        body.remove("dateFormat");

        assertErrorCodes(body, "validation.msg.missing.dateFormat.parameter");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "[]", "5" })
    void bodyMustBeAJsonObject(final String json) {
        assertThrows(InvalidJsonException.class, () -> validator.validateAndParse(json));
    }

    @Test
    void unknownCurrencyIsRejected() {
        final Map<String, Object> body = termBody();
        body.put("currencyCode", "XYZ");

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.currencyCode.does.not.exist");
    }

    @Test
    void currencyIsRequiredWhenTheOrganisationHasSeveral() {
        when(currencyReadPlatformService.retrieveAllowedCurrencies()).thenReturn(List.of(EUR, USD));
        final Map<String, Object> body = termBody();
        body.remove("currencyCode");

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.currencyCode.cannot.be.blank");
    }

    @Test
    void currencyDefaultsToTheOnlyOrganisationCurrency() {
        when(currencyReadPlatformService.retrieveAllowedCurrencies()).thenReturn(List.of(EUR));
        final Map<String, Object> body = termBody();
        body.remove("currencyCode");

        assertEquals("EUR", validator.validateAndParse(json(body)).currency().getCode());
    }

    @Test
    void currencyIsRequiredWithoutOrganisationCurrencies() {
        when(currencyReadPlatformService.retrieveAllowedCurrencies()).thenReturn(List.of());
        final Map<String, Object> body = termBody();
        body.remove("currencyCode");

        assertErrorCodes(body, "validation.msg.payoffSchedulePreview.currencyCode.cannot.be.blank");
    }

    @Test
    void decimalPlacesDefaultToTheRequestedCurrency() {
        final Map<String, Object> body = termBody();
        body.put("currencyCode", "JPY");
        body.remove("decimalPlaces");

        assertEquals(0, validator.validateAndParse(json(body)).currency().getDecimalPlaces());
    }

    @Test
    void decimalPlacesOverrideTheCurrency() {
        final Map<String, Object> body = termBody();
        body.put("currencyCode", "JPY");
        body.put("decimalPlaces", 3);

        assertEquals(3, validator.validateAndParse(json(body)).currency().getDecimalPlaces());
    }

    private void assertErrorCodes(final Map<String, Object> body, final String... expectedCodes) {
        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateAndParse(json(body)));
        assertEquals(List.of(expectedCodes),
                exception.getErrors().stream().map(ApiParameterError::getUserMessageGlobalisationCode).toList());
    }

    private static Map<String, Object> termBody() {
        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", 1000);
        body.put("term", "P6M");
        body.put("frequency", "P1M");
        body.put("annualNominalInterestRate", 7);
        body.put("interestMethod", "DECLINING_BALANCE");
        body.put("currencyCode", "USD");
        body.put("decimalPlaces", 2);
        body.put("startDate", "2024-01-01");
        body.put("dateFormat", "yyyy-MM-dd");
        body.put("locale", "en");
        return body;
    }

    private static String json(final Map<String, Object> body) {
        return new Gson().toJson(body);
    }
}
