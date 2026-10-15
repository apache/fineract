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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.validator.ParseAndValidator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.payoffpreview.data.PayoffSchedulePreviewParameters;
import org.apache.fineract.portfolio.loanaccount.payoffpreview.data.PayoffSchedulePreviewPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayoffSchedulePreviewParseAndValidator extends ParseAndValidator {

    private static final String RESOURCE = "payoffSchedulePreview";
    private static final String AMOUNT_PARAM = "amount";
    private static final String TERM_PARAM = "term";
    private static final String FIXED_PAYMENT_AMOUNT_PARAM = "fixedPaymentAmount";
    private static final String FREQUENCY_PARAM = "frequency";
    private static final String ANNUAL_NOMINAL_INTEREST_RATE_PARAM = "annualNominalInterestRate";
    private static final String INTEREST_METHOD_PARAM = "interestMethod";
    private static final String CURRENCY_CODE_PARAM = "currencyCode";
    private static final String DECIMAL_PLACES_PARAM = "decimalPlaces";
    private static final String START_DATE_PARAM = "startDate";
    private static final String DATE_FORMAT_PARAM = "dateFormat";
    private static final String LOCALE_PARAM = "locale";
    private static final String DAYS_IN_YEAR_TYPE_PARAM = "daysInYearType";
    private static final String DAYS_IN_MONTH_TYPE_PARAM = "daysInMonthType";
    private static final List<String> SUPPORTED_PARAMETERS = List.of(AMOUNT_PARAM, TERM_PARAM, FIXED_PAYMENT_AMOUNT_PARAM, FREQUENCY_PARAM,
            ANNUAL_NOMINAL_INTEREST_RATE_PARAM, INTEREST_METHOD_PARAM, CURRENCY_CODE_PARAM, DECIMAL_PLACES_PARAM, START_DATE_PARAM,
            DATE_FORMAT_PARAM, LOCALE_PARAM, DAYS_IN_YEAR_TYPE_PARAM, DAYS_IN_MONTH_TYPE_PARAM);

    // At most four digits keeps the period arithmetic far from int overflow;
    // the configurable caps bound the schedule size.
    private static final String PERIOD_REGEX = "^P([1-9]\\d{0,3})([MWD])$";
    private static final Pattern PERIOD_PATTERN = Pattern.compile(PERIOD_REGEX);
    private static final int MAX_DECIMAL_PLACES = 6;

    private final FromJsonHelper jsonHelper;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final ApplicationCurrencyRepository applicationCurrencyRepository;

    public PayoffSchedulePreviewParameters validateAndParse(final String json) {
        validateRequestBody(json);
        final JsonElement parsed = jsonHelper.parse(json);
        if (!parsed.isJsonObject()) {
            throw new InvalidJsonException();
        }
        final JsonObject element = parsed.getAsJsonObject();
        jsonHelper.checkForUnsupportedParameters(element, SUPPORTED_PARAMETERS);

        final DataValidatorBuilder dataValidator = new DataValidatorBuilder(new ArrayList<>()).resource(RESOURCE);

        final BigDecimal amount = jsonHelper.extractBigDecimalWithLocaleNamed(AMOUNT_PARAM, element);
        dataValidator.reset().parameter(AMOUNT_PARAM).value(amount).notNull().positiveAmount();

        final String term = StringUtils.trimToNull(jsonHelper.extractStringNamed(TERM_PARAM, element));
        final BigDecimal fixedPaymentAmount = jsonHelper.extractBigDecimalWithLocaleNamed(FIXED_PAYMENT_AMOUNT_PARAM, element);
        if (term == null && fixedPaymentAmount == null) {
            dataValidator.reset().parameter(TERM_PARAM).failWithCode("or.fixedPaymentAmount.required");
        } else if (term != null && fixedPaymentAmount != null) {
            dataValidator.reset().parameter(FIXED_PAYMENT_AMOUNT_PARAM).value(fixedPaymentAmount)
                    .mustBeBlankWhenParameterProvided(TERM_PARAM, term);
        }
        dataValidator.reset().parameter(FIXED_PAYMENT_AMOUNT_PARAM).value(fixedPaymentAmount).ignoreIfNull().positiveAmount();

        final PayoffSchedulePreviewPeriod termPeriod = parsePeriod(dataValidator, TERM_PARAM, term, false);
        final PayoffSchedulePreviewPeriod frequency = parsePeriod(dataValidator, FREQUENCY_PARAM,
                StringUtils.trimToNull(jsonHelper.extractStringNamed(FREQUENCY_PARAM, element)), true);
        validateTermMatchesFrequency(dataValidator, termPeriod, frequency);

        final BigDecimal annualNominalInterestRate = jsonHelper.extractBigDecimalWithLocaleNamed(ANNUAL_NOMINAL_INTEREST_RATE_PARAM,
                element);
        dataValidator.reset().parameter(ANNUAL_NOMINAL_INTEREST_RATE_PARAM).value(annualNominalInterestRate).ignoreIfNull()
                .zeroOrPositiveAmount();

        // Only declining balance is in scope for the preview,
        // although the progressive engine also supports flat interest.
        final String interestMethod = jsonHelper.extractStringNamed(INTEREST_METHOD_PARAM, element);
        dataValidator.reset().parameter(INTEREST_METHOD_PARAM).value(interestMethod).ignoreIfNull()
                .isOneOfTheseStringValues(List.of(InterestMethod.DECLINING_BALANCE.name()));

        final Integer decimalPlaces = jsonHelper.extractIntegerSansLocaleNamed(DECIMAL_PLACES_PARAM, element);
        dataValidator.reset().parameter(DECIMAL_PLACES_PARAM).value(decimalPlaces).ignoreIfNull().inMinMaxRange(0, MAX_DECIMAL_PLACES);

        final Integer daysInYearType = jsonHelper.extractIntegerSansLocaleNamed(DAYS_IN_YEAR_TYPE_PARAM, element);
        dataValidator.reset().parameter(DAYS_IN_YEAR_TYPE_PARAM).value(daysInYearType).ignoreIfNull()
                .isOneOfTheseValues(DaysInYearType.integerValues());

        final Integer daysInMonthType = jsonHelper.extractIntegerSansLocaleNamed(DAYS_IN_MONTH_TYPE_PARAM, element);
        dataValidator.reset().parameter(DAYS_IN_MONTH_TYPE_PARAM).value(daysInMonthType).ignoreIfNull()
                .isOneOfTheseValues(DaysInMonthType.integerValues());

        final LocalDate startDate = jsonHelper.extractLocalDateNamed(START_DATE_PARAM, element);
        dataValidator.reset().parameter(START_DATE_PARAM).value(startDate).notNull();

        final CurrencyData currency = resolveCurrency(dataValidator,
                StringUtils.trimToNull(jsonHelper.extractStringNamed(CURRENCY_CODE_PARAM, element)));
        final Integer roundingScale = decimalPlaces != null ? decimalPlaces : currency == null ? null : currency.getDecimalPlaces();
        validateNotFinerThanRoundingScale(dataValidator, AMOUNT_PARAM, amount, roundingScale);
        validateNotFinerThanRoundingScale(dataValidator, FIXED_PAYMENT_AMOUNT_PARAM, fixedPaymentAmount, roundingScale);

        dataValidator.throwValidationErrors();

        final CurrencyData resolvedCurrency = new CurrencyData(currency.getCode(), roundingScale, currency.getInMultiplesOf());
        return new PayoffSchedulePreviewParameters(amount, termPeriod, fixedPaymentAmount, frequency,
                annualNominalInterestRate == null ? BigDecimal.ZERO : annualNominalInterestRate, resolvedCurrency, startDate,
                daysInYearType == null ? DaysInYearType.ACTUAL : DaysInYearType.fromInt(daysInYearType),
                daysInMonthType == null ? DaysInMonthType.ACTUAL : DaysInMonthType.fromInt(daysInMonthType));
    }

    private PayoffSchedulePreviewPeriod parsePeriod(final DataValidatorBuilder dataValidator, final String parameterName,
            final String value, final boolean mandatory) {
        final DataValidatorBuilder builder = dataValidator.reset().parameter(parameterName).value(value);
        if (mandatory) {
            builder.notBlank();
        } else {
            builder.ignoreIfNull();
        }
        builder.matchesRegularExpression(PERIOD_REGEX, "The parameter `" + parameterName
                + "` must be a single-unit ISO-8601 period of 1 to 9999 months, weeks or days, e.g. P6M, P2W or P10D.");

        final Matcher matcher = value == null ? null : PERIOD_PATTERN.matcher(value);
        if (matcher == null || !matcher.matches()) {
            return null;
        }
        final PeriodFrequencyType unit = switch (matcher.group(2)) {
            case "M" -> PeriodFrequencyType.MONTHS;
            case "W" -> PeriodFrequencyType.WEEKS;
            default -> PeriodFrequencyType.DAYS;
        };
        return new PayoffSchedulePreviewPeriod(value, unit, Integer.parseInt(matcher.group(1)));
    }

    // Same rule as the regular loan path (LoanScheduleValidator):
    // the term must use the repayment frequency's unit and be an exact multiple of it.
    private void validateTermMatchesFrequency(final DataValidatorBuilder dataValidator, final PayoffSchedulePreviewPeriod term,
            final PayoffSchedulePreviewPeriod frequency) {
        if (term == null || frequency == null) {
            return;
        }
        if (term.unit() != frequency.unit()) {
            dataValidator.reset().parameter(TERM_PARAM).value(term.iso()).failWithCode("not.the.same.unit.as.frequency");
        } else if (term.value() % frequency.value() != 0) {
            dataValidator.reset().parameter(TERM_PARAM).value(term.iso()).failWithCode("not.a.multiple.of.frequency");
        }
    }

    // Amounts are rounded to the schedule's scale, so a finer amount would change silently
    // (0.004 at two decimal places becomes zero). Reject it instead, as Working Capital products do.
    private void validateNotFinerThanRoundingScale(final DataValidatorBuilder dataValidator, final String parameterName,
            final BigDecimal value, final Integer roundingScale) {
        if (value == null || roundingScale == null || roundingScale < 0 || roundingScale > MAX_DECIMAL_PLACES) {
            return;
        }
        dataValidator.reset().parameter(parameterName).value(value).scaleNotGreaterThan(roundingScale);
    }

    private CurrencyData resolveCurrency(final DataValidatorBuilder dataValidator, final String currencyCode) {
        if (currencyCode != null) {
            final CurrencyData currency = applicationCurrencyRepository.findCurrencyDataByCode(currencyCode);
            if (currency == null) {
                dataValidator.reset().parameter(CURRENCY_CODE_PARAM).value(currencyCode).failWithCode("does.not.exist");
            }
            return currency;
        }
        final List<CurrencyData> organisationCurrencies = currencyReadPlatformService.retrieveAllowedCurrencies();
        if (organisationCurrencies.size() != 1) {
            // Only a single organisation currency is an unambiguous default; otherwise the caller has to choose one.
            dataValidator.reset().parameter(CURRENCY_CODE_PARAM).value(null).notBlank();
            return null;
        }
        return organisationCurrencies.getFirst();
    }
}
