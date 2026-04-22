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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
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
    public static final String LOCALE = "locale";
    public static final String DATE_FORMAT = "dateFormat";
    public static final String FLOATINGRATE = "floatingrate";
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_FLOATING_RATES = new HashSet<>(
            Arrays.asList(NAME, IS_BASE_LENDING_RATE, IS_ACTIVE, RATE_PERIODS));
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_FLOATING_RATE_PERIODS = new HashSet<>(
            Arrays.asList(FROM_DATE, INTEREST_RATE, IS_DIFFERENTIAL_TO_BASE_LENDING_RATE, LOCALE, DATE_FORMAT));
    private final FromJsonHelper fromApiJsonHelper;
    private final FloatingRateRepository floatingRateRepository;

    @Autowired
    public FloatingRateDataValidator(final FromJsonHelper fromApiJsonHelper, final FloatingRateRepository floatingRateRepository) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.floatingRateRepository = floatingRateRepository;
    }

    public void validateForCreate(String json) {
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS_FOR_FLOATING_RATES);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(FLOATINGRATE);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed(NAME, element);
        baseDataValidator.reset().parameter(NAME).value(name).notBlank().notExceedingLengthOf(200);

        Boolean isBaseLendingRate = null;
        if (this.fromApiJsonHelper.parameterExists(IS_BASE_LENDING_RATE, element)) {
            isBaseLendingRate = this.fromApiJsonHelper.extractBooleanNamed(IS_BASE_LENDING_RATE, element);

            baseDataValidator.reset().parameter(IS_BASE_LENDING_RATE).value(isBaseLendingRate).notNull();
            if (isBaseLendingRate == null) {
                baseDataValidator.reset().parameter(IS_BASE_LENDING_RATE).trueOrFalseRequired(false);
            } else if (isBaseLendingRate) {
                FloatingRate baseLendingRate = this.floatingRateRepository.retrieveBaseLendingRate();
                if (baseLendingRate != null) {
                    baseDataValidator.reset().parameter(IS_BASE_LENDING_RATE).value(isBaseLendingRate)
                            .failWithCode("baselendingrate.duplicate", "Base Lending Rate already exists");
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(IS_ACTIVE, element)) {
            final Boolean isActive = this.fromApiJsonHelper.extractBooleanNamed(IS_ACTIVE, element);
            if (isActive == null) {
                baseDataValidator.reset().parameter(IS_ACTIVE).trueOrFalseRequired(false);
            }
        }

        if (isBaseLendingRate == null) {
            isBaseLendingRate = false;
        }
        validateRatePeriods(baseDataValidator, element, isBaseLendingRate, false);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateRatePeriods(DataValidatorBuilder baseDataValidator, JsonElement element, boolean isBaseLendingRate,
            boolean isBLRModifiedAsNonBLR) {
        if (this.fromApiJsonHelper.parameterExists(RATE_PERIODS, element)) {
            final JsonArray ratePeriods = this.fromApiJsonHelper.extractJsonArrayNamed(RATE_PERIODS, element);
            baseDataValidator.reset().parameter(RATE_PERIODS).value(ratePeriods).notBlank().jsonArrayNotEmpty();

            if (ratePeriods != null) {
                List<LocalDate> fromDates = new ArrayList<>();
                for (int i = 0; i < ratePeriods.size(); i++) {
                    final JsonElement ratePeriod = ratePeriods.get(i);

                    this.fromApiJsonHelper.checkForUnsupportedParameters(ratePeriod.getAsJsonObject(),
                            SUPPORTED_PARAMETERS_FOR_FLOATING_RATE_PERIODS);

                    final LocalDate fromDate = this.fromApiJsonHelper.extractLocalDateNamed(FROM_DATE, ratePeriod);
                    baseDataValidator.reset().parameter(FROM_DATE).parameterAtIndexArray(FROM_DATE, i + 1).value(fromDate).notBlank()
                            .validateDateAfter(DateUtils.getBusinessLocalDate().plusDays(1));
                    if (fromDate != null) {
                        fromDates.add(fromDate);
                    }

                    final BigDecimal interestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(INTEREST_RATE,
                            ratePeriod);
                    baseDataValidator.reset().parameter(INTEREST_RATE).parameterAtIndexArray(INTEREST_RATE, i + 1)
                            .value(interestRatePerPeriod).notNull().zeroOrPositiveAmount();

                    if (this.fromApiJsonHelper.parameterExists(IS_DIFFERENTIAL_TO_BASE_LENDING_RATE, ratePeriod)) {
                        final Boolean isDifferentialToBaseLendingRate = this.fromApiJsonHelper
                                .extractBooleanNamed(IS_DIFFERENTIAL_TO_BASE_LENDING_RATE, ratePeriod);
                        if (isDifferentialToBaseLendingRate == null) {
                            baseDataValidator.reset().parameter(IS_DIFFERENTIAL_TO_BASE_LENDING_RATE)
                                    .parameterAtIndexArray(IS_DIFFERENTIAL_TO_BASE_LENDING_RATE, i + 1).trueOrFalseRequired(false);
                        } else if (isDifferentialToBaseLendingRate) {
                            FloatingRate baseLendingRate = this.floatingRateRepository.retrieveBaseLendingRate();
                            if (baseLendingRate == null || isBLRModifiedAsNonBLR) {
                                baseDataValidator.reset().parameter(IS_DIFFERENTIAL_TO_BASE_LENDING_RATE)
                                        .parameterAtIndexArray(IS_DIFFERENTIAL_TO_BASE_LENDING_RATE, i + 1)
                                        .value(isDifferentialToBaseLendingRate)
                                        .failWithCode("no.baselending.rate.defined", "Base Lending Rate doesn't exists");
                            }

                            if (isBaseLendingRate) {
                                baseDataValidator.reset().parameter(IS_DIFFERENTIAL_TO_BASE_LENDING_RATE)
                                        .parameterAtIndexArray(IS_DIFFERENTIAL_TO_BASE_LENDING_RATE, i + 1)
                                        .value(isDifferentialToBaseLendingRate).failWithCode("cannot.be.true.for.baselendingrate",
                                                "isDifferentialToBaseLendingRate cannot be true for floating rate marked as Base Lending Rate.");
                            }

                        }
                    }
                }
                Set<LocalDate> uniqueFromDates = new HashSet<>(fromDates);
                if (fromDates.size() != uniqueFromDates.size()) {
                    baseDataValidator.reset().parameter(FROM_DATE).failWithCode("multiple.same.date",
                            "More than one entry in ratePeriods have same fromDate.");
                }

            }
        }
    }

    public void validateForUpdate(String json, FloatingRate floatingRateForUpdate) {
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS_FOR_FLOATING_RATES);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(FLOATINGRATE);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(NAME, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(NAME, element);
            baseDataValidator.reset().parameter(NAME).value(name).notBlank().notExceedingLengthOf(200);
        }

        Boolean isBaseLendingRate = null;
        Boolean isBLRModifiedAsNonBLR = false;
        FloatingRate baseLendingRate = this.floatingRateRepository.retrieveBaseLendingRate();
        if (this.fromApiJsonHelper.parameterExists(IS_BASE_LENDING_RATE, element)) {
            isBaseLendingRate = this.fromApiJsonHelper.extractBooleanNamed(IS_BASE_LENDING_RATE, element);

            baseDataValidator.reset().parameter(IS_BASE_LENDING_RATE).value(isBaseLendingRate).notNull();
            if (isBaseLendingRate == null) {
                baseDataValidator.reset().parameter(IS_BASE_LENDING_RATE).trueOrFalseRequired(false);
            } else if (isBaseLendingRate && baseLendingRate != null && !baseLendingRate.getId().equals(floatingRateForUpdate.getId())) { // NOSONAR
                baseDataValidator.reset().parameter(IS_BASE_LENDING_RATE).value(isBaseLendingRate).failWithCode("baselendingrate.duplicate",
                        "Base Lending Rate already exists");
            }
        }

        Boolean isActive = null;
        if (this.fromApiJsonHelper.parameterExists(IS_ACTIVE, element)) {
            isActive = this.fromApiJsonHelper.extractBooleanNamed(IS_ACTIVE, element);
            if (isActive == null) {
                baseDataValidator.reset().parameter(IS_ACTIVE).trueOrFalseRequired(false);
            }
        }

        if (isBaseLendingRate == null) {
            isBaseLendingRate = floatingRateForUpdate.isBaseLendingRate();
        }

        if (isActive == null) {
            isActive = floatingRateForUpdate.isActive();
        }

        if (baseLendingRate != null && baseLendingRate.getId().equals(floatingRateForUpdate.getId()) && (!isBaseLendingRate || !isActive)) { // NOSONAR
            isBLRModifiedAsNonBLR = true;
        }

        if (isBLRModifiedAsNonBLR) {
            Collection<FloatingRate> floatingRates = this.floatingRateRepository.retrieveFloatingRatesLinkedToBLR();
            if (floatingRates != null && !floatingRates.isEmpty()) {
                baseDataValidator.reset().parameter(IS_BASE_LENDING_RATE).value(isBaseLendingRate).failWithCode(
                        "cannot.be.marked.non.baselendingrate",
                        "There are floating rates linked to this Base Lending Rate, cannot be marked as non-Base Lending Rate.");
            }
        }

        validateRatePeriods(baseDataValidator, element, isBaseLendingRate, isBLRModifiedAsNonBLR);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

}
