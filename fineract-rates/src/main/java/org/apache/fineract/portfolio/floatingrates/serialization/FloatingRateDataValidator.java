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
package org.apache.fineract.portfolio.floatingrates.serialization;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateUpdateRequest;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FloatingRateDataValidator {

    public static final String NAME = "name";
    public static final String IS_BASE_LENDING_RATE = "isBaseLendingRate";
    public static final String IS_ACTIVE = "isActive";
    public static final String RATE_PERIODS = "ratePeriods";
    public static final String FROM_DATE = "fromDate";
    public static final String INTEREST_RATE = "interestRate";
    public static final String IS_DIFFERENTIAL_TO_BASE_LENDING_RATE = "isDifferentialToBaseLendingRate";
    public static final String FLOATINGRATE = "floatingrate";

    private final FloatingRateRepository floatingRateRepository;

    @Autowired
    public FloatingRateDataValidator(final FloatingRateRepository floatingRateRepository) {
        this.floatingRateRepository = floatingRateRepository;
    }

    public void validateForCreate(final FloatingRateRequest request) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(FLOATINGRATE);

        baseDataValidator.reset().parameter(NAME).value(request.getName()).notBlank().notExceedingLengthOf(200);

        Boolean isBaseLendingRate = request.getIsBaseLendingRate();
        if (isBaseLendingRate != null && isBaseLendingRate) {
            final FloatingRate baseLendingRate = this.floatingRateRepository.retrieveBaseLendingRate();
            if (baseLendingRate != null) {
                baseDataValidator.reset().parameter(IS_BASE_LENDING_RATE).value(isBaseLendingRate).failWithCode("baselendingrate.duplicate",
                        "Base Lending Rate already exists");
            }
        }

        if (isBaseLendingRate == null) {
            isBaseLendingRate = false;
        }
        validateRatePeriods(baseDataValidator, dataValidationErrors, request.getRatePeriods(), isBaseLendingRate, false);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final FloatingRateUpdateRequest request, final FloatingRate floatingRateForUpdate) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(FLOATINGRATE);

        if (request.getName() != null) {
            baseDataValidator.reset().parameter(NAME).value(request.getName()).notBlank().notExceedingLengthOf(200);
        }

        Boolean isBaseLendingRate = request.getIsBaseLendingRate();
        boolean isBLRModifiedAsNonBLR = false;
        final FloatingRate baseLendingRate = this.floatingRateRepository.retrieveBaseLendingRate();
        if (isBaseLendingRate != null && isBaseLendingRate && baseLendingRate != null
                && !baseLendingRate.getId().equals(floatingRateForUpdate.getId())) {
            baseDataValidator.reset().parameter(IS_BASE_LENDING_RATE).value(isBaseLendingRate).failWithCode("baselendingrate.duplicate",
                    "Base Lending Rate already exists");
        }

        Boolean isActive = request.getIsActive();
        if (isBaseLendingRate == null) {
            isBaseLendingRate = floatingRateForUpdate.isBaseLendingRate();
        }
        if (isActive == null) {
            isActive = floatingRateForUpdate.isActive();
        }

        if (baseLendingRate != null && baseLendingRate.getId().equals(floatingRateForUpdate.getId()) && (!isBaseLendingRate || !isActive)) {
            isBLRModifiedAsNonBLR = true;
        }

        if (isBLRModifiedAsNonBLR) {
            final Collection<FloatingRate> floatingRates = this.floatingRateRepository.retrieveFloatingRatesLinkedToBLR();
            if (floatingRates != null && !floatingRates.isEmpty()) {
                baseDataValidator.reset().parameter(IS_BASE_LENDING_RATE).value(isBaseLendingRate).failWithCode(
                        "cannot.be.marked.non.baselendingrate",
                        "There are floating rates linked to this Base Lending Rate, cannot be marked as non-Base Lending Rate.");
            }
        }

        validateRatePeriods(baseDataValidator, dataValidationErrors, request.getRatePeriods(), isBaseLendingRate, isBLRModifiedAsNonBLR);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateRatePeriods(final DataValidatorBuilder baseDataValidator, final List<ApiParameterError> dataValidationErrors,
            final List<FloatingRatePeriodRequest> ratePeriods, final boolean isBaseLendingRate, final boolean isBLRModifiedAsNonBLR) {
        if (ratePeriods == null) {
            return;
        }
        if (ratePeriods.isEmpty()) {
            dataValidationErrors
                    .add(ApiParameterError.parameterError("validation.msg." + FLOATINGRATE + "." + RATE_PERIODS + ".cannot.be.empty",
                            "The parameter `" + RATE_PERIODS + "` cannot be empty. You must select at least one.", RATE_PERIODS));
            return;
        }

        final List<LocalDate> fromDates = new ArrayList<>();
        for (int i = 0; i < ratePeriods.size(); i++) {
            final FloatingRatePeriodRequest ratePeriod = ratePeriods.get(i);

            final LocalDate fromDate = parseDate(ratePeriod.getFromDate(), ratePeriod.getDateFormat(), ratePeriod.getLocale());
            baseDataValidator.reset().parameter(FROM_DATE).parameterAtIndexArray(FROM_DATE, i + 1).value(fromDate).notBlank()
                    .validateDateAfter(DateUtils.getBusinessLocalDate().plusDays(1));
            if (fromDate != null) {
                fromDates.add(fromDate);
            }

            final BigDecimal interestRatePerPeriod = ratePeriod.getInterestRate();
            baseDataValidator.reset().parameter(INTEREST_RATE).parameterAtIndexArray(INTEREST_RATE, i + 1).value(interestRatePerPeriod)
                    .notNull().zeroOrPositiveAmount();

            final Boolean isDifferentialToBaseLendingRate = ratePeriod.getIsDifferentialToBaseLendingRate();
            if (isDifferentialToBaseLendingRate != null && isDifferentialToBaseLendingRate) {
                final FloatingRate baseLendingRate = this.floatingRateRepository.retrieveBaseLendingRate();
                if (baseLendingRate == null || isBLRModifiedAsNonBLR) {
                    baseDataValidator.reset().parameter(IS_DIFFERENTIAL_TO_BASE_LENDING_RATE)
                            .parameterAtIndexArray(IS_DIFFERENTIAL_TO_BASE_LENDING_RATE, i + 1).value(isDifferentialToBaseLendingRate)
                            .failWithCode("no.baselending.rate.defined", "Base Lending Rate doesn't exists");
                }
                if (isBaseLendingRate) {
                    baseDataValidator.reset().parameter(IS_DIFFERENTIAL_TO_BASE_LENDING_RATE)
                            .parameterAtIndexArray(IS_DIFFERENTIAL_TO_BASE_LENDING_RATE, i + 1).value(isDifferentialToBaseLendingRate)
                            .failWithCode("cannot.be.true.for.baselendingrate",
                                    "isDifferentialToBaseLendingRate cannot be true for floating rate marked as Base Lending Rate.");
                }
            }
        }
        final Set<LocalDate> uniqueFromDates = new HashSet<>(fromDates);
        if (fromDates.size() != uniqueFromDates.size()) {
            baseDataValidator.reset().parameter(FROM_DATE).failWithCode("multiple.same.date",
                    "More than one entry in ratePeriods have same fromDate.");
        }
    }

    private static LocalDate parseDate(final String value, final String dateFormat, final String locale) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (StringUtils.isBlank(dateFormat)) {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        }
        final Locale loc = StringUtils.isBlank(locale) ? Locale.getDefault() : Locale.forLanguageTag(locale.replace('_', '-'));
        return LocalDate.parse(value, new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(dateFormat).toFormatter(loc));
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
