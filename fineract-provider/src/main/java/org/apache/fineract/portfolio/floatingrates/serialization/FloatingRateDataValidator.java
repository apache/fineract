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

import java.lang.reflect.Type;
import java.math.BigDecimal;
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
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class FloatingRateDataValidator {

	private final Set<String> supportedParametersForFloatingRates = new HashSet<>(
			Arrays.asList("name", "isBaseLendingRate", "isActive",
					"ratePeriods"));
	private final Set<String> supportedParametersForFloatingRatePeriods = new HashSet<>(
			Arrays.asList("fromDate", "interestRate",
					"isDifferentialToBaseLendingRate", "locale", "dateFormat"));

	private final FromJsonHelper fromApiJsonHelper;
	private final FloatingRateRepository floatingRateRepository;

	@Autowired
	public FloatingRateDataValidator(final FromJsonHelper fromApiJsonHelper,
			final FloatingRateRepository floatingRateRepository) {
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.floatingRateRepository = floatingRateRepository;
	}

	public void validateForCreate(String json) {
		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
				this.supportedParametersForFloatingRates);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors).resource("floatingrate");

		final JsonElement element = this.fromApiJsonHelper.parse(json);

		final String name = this.fromApiJsonHelper.extractStringNamed("name",
				element);
		baseDataValidator.reset().parameter("name").value(name).notBlank()
				.notExceedingLengthOf(200);

		Boolean isBaseLendingRate = null;
		if (this.fromApiJsonHelper
				.parameterExists("isBaseLendingRate", element)) {
			isBaseLendingRate = this.fromApiJsonHelper
					.extractBooleanNamed("isBaseLendingRate", element);

			baseDataValidator.reset().parameter("isBaseLendingRate")
					.value(isBaseLendingRate).notNull();
			if (isBaseLendingRate == null) {
				baseDataValidator.reset().parameter("isBaseLendingRate")
						.trueOrFalseRequired(false);
			} else if (isBaseLendingRate) {
				FloatingRate baseLendingRate = this.floatingRateRepository
						.retrieveBaseLendingRate();
				if (baseLendingRate != null) {
					baseDataValidator
							.reset()
							.parameter("isBaseLendingRate")
							.value(isBaseLendingRate)
							.failWithCode("baselendingrate.duplicate",
									"Base Lending Rate already exists");
				}
			}
		}

		if (this.fromApiJsonHelper.parameterExists("isActive", element)) {
			final Boolean isActive = this.fromApiJsonHelper
					.extractBooleanNamed("isActive", element);
			if (isActive == null) {
				baseDataValidator.reset().parameter("isActive")
						.trueOrFalseRequired(false);
			}
		}

		if(isBaseLendingRate == null){
			isBaseLendingRate = false;
		}
		validateRatePeriods(baseDataValidator, element, isBaseLendingRate, false);

		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	private void validateRatePeriods(DataValidatorBuilder baseDataValidator,
			JsonElement element, boolean isBaseLendingRate, boolean isBLRModifiedAsNonBLR) {
		if (this.fromApiJsonHelper.parameterExists("ratePeriods", element)) {
			final JsonArray ratePeriods = this.fromApiJsonHelper
					.extractJsonArrayNamed("ratePeriods", element);
			baseDataValidator.reset().parameter("ratePeriods")
					.value(ratePeriods).notBlank().jsonArrayNotEmpty();

			if (ratePeriods != null) {
				List<LocalDate> fromDates = new ArrayList<>();
				for (int i = 0; i < ratePeriods.size(); i++) {
					final JsonElement ratePeriod = ratePeriods.get(i);

					this.fromApiJsonHelper.checkForUnsupportedParameters(
							ratePeriod.getAsJsonObject(),
							this.supportedParametersForFloatingRatePeriods);

					final LocalDate fromDate = this.fromApiJsonHelper
							.extractLocalDateNamed("fromDate", ratePeriod);
					baseDataValidator
							.reset()
							.parameter("fromDate")
							.parameterAtIndexArray("fromDate", i + 1)
							.value(fromDate)
							.notBlank()
							.validateDateAfter(
									DateUtils.getLocalDateOfTenant()
											.plusDays(1));
					if (fromDate != null) {
						fromDates.add(fromDate);
					}

					final BigDecimal interestRatePerPeriod = this.fromApiJsonHelper
							.extractBigDecimalWithLocaleNamed("interestRate",
									ratePeriod);
					baseDataValidator.reset().parameter("interestRate")
							.parameterAtIndexArray("interestRate", i + 1)
							.value(interestRatePerPeriod).notNull()
							.zeroOrPositiveAmount();

					if (this.fromApiJsonHelper.parameterExists(
							"isDifferentialToBaseLendingRate", ratePeriod)) {
						final Boolean isDifferentialToBaseLendingRate = this.fromApiJsonHelper
								.extractBooleanNamed(
										"isDifferentialToBaseLendingRate",
										ratePeriod);
						if (isDifferentialToBaseLendingRate == null) {
							baseDataValidator
									.reset()
									.parameter(
											"isDifferentialToBaseLendingRate")
									.parameterAtIndexArray(
											"isDifferentialToBaseLendingRate",
											i + 1).trueOrFalseRequired(false);
						} else if (isDifferentialToBaseLendingRate) {
							FloatingRate baseLendingRate = this.floatingRateRepository
									.retrieveBaseLendingRate();
							if (baseLendingRate == null || isBLRModifiedAsNonBLR) {
								baseDataValidator
										.reset()
										.parameter(
												"isDifferentialToBaseLendingRate")
										.parameterAtIndexArray(
												"isDifferentialToBaseLendingRate",
												i + 1)
										.value(isDifferentialToBaseLendingRate)
										.failWithCode(
												"no.baselending.rate.defined",
												"Base Lending Rate doesn't exists");
							}
							
							if(isBaseLendingRate){
								baseDataValidator
								.reset()
								.parameter(
										"isDifferentialToBaseLendingRate")
								.parameterAtIndexArray(
										"isDifferentialToBaseLendingRate",
										i + 1)
								.value(isDifferentialToBaseLendingRate)
								.failWithCode(
										"cannot.be.true.for.baselendingrate",
										"isDifferentialToBaseLendingRate cannot be true for floating rate marked as Base Lending Rate.");
							}

						}
					}
				}
				Set<LocalDate> uniqueFromDates = new HashSet<>(fromDates);
				if (fromDates.size() != uniqueFromDates.size()) {
					baseDataValidator
							.reset()
							.parameter("fromDate")
							.failWithCode("multiple.same.date",
									"More than one entry in ratePeriods have same fromDate.");
				}

			}
		}
	}

	public void validateForUpdate(String json,
			FloatingRate floatingRateForUpdate) {
		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
				this.supportedParametersForFloatingRates);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors).resource("floatingrate");

		final JsonElement element = this.fromApiJsonHelper.parse(json);

		if (this.fromApiJsonHelper.parameterExists("name", element)) {
			final String name = this.fromApiJsonHelper.extractStringNamed(
					"name", element);
			baseDataValidator.reset().parameter("name").value(name).notBlank()
					.notExceedingLengthOf(200);
		}

		Boolean isBaseLendingRate = null;
		Boolean isBLRModifiedAsNonBLR = false;
		FloatingRate baseLendingRate = this.floatingRateRepository
				.retrieveBaseLendingRate();
		if (this.fromApiJsonHelper
				.parameterExists("isBaseLendingRate", element)) {
			isBaseLendingRate = this.fromApiJsonHelper
					.extractBooleanNamed("isBaseLendingRate", element);

			baseDataValidator.reset().parameter("isBaseLendingRate")
					.value(isBaseLendingRate).notNull();
			if (isBaseLendingRate == null) {
				baseDataValidator.reset().parameter("isBaseLendingRate")
						.trueOrFalseRequired(false);
			} else if (isBaseLendingRate) {
				if (baseLendingRate != null
						&& baseLendingRate.getId() != floatingRateForUpdate
								.getId()) {
					baseDataValidator
							.reset()
							.parameter("isBaseLendingRate")
							.value(isBaseLendingRate)
							.failWithCode("baselendingrate.duplicate",
									"Base Lending Rate already exists");
				}
			}
		}

		Boolean isActive = null;
		if (this.fromApiJsonHelper.parameterExists("isActive", element)) {
			isActive = this.fromApiJsonHelper
					.extractBooleanNamed("isActive", element);
			if (isActive == null) {
				baseDataValidator.reset().parameter("isActive")
						.trueOrFalseRequired(false);
			}
		}

		if(isBaseLendingRate == null){
			isBaseLendingRate = floatingRateForUpdate.isBaseLendingRate();
		}
		
		if(isActive == null){
			isActive = floatingRateForUpdate.isActive();
		}
		
		if(baseLendingRate != null
				&& baseLendingRate.getId() == floatingRateForUpdate
						.getId()){
			if(!isBaseLendingRate || !isActive){
				isBLRModifiedAsNonBLR = true;
			}
		}
		
		if(isBLRModifiedAsNonBLR){
			Collection<FloatingRate> floatingRates = this.floatingRateRepository.retrieveFloatingRatesLinkedToBLR();
			if(floatingRates != null && floatingRates.size() > 0){
				baseDataValidator
				.reset()
				.parameter("isBaseLendingRate")
				.value(isBaseLendingRate)
				.failWithCode("cannot.be.marked.non.baselendingrate",
						"There are floating rates linked to this Base Lending Rate, cannot be marked as non-Base Lending Rate.");
			}
		}


		validateRatePeriods(baseDataValidator, element, isBaseLendingRate, isBLRModifiedAsNonBLR);

		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	private void throwExceptionIfValidationWarningsExist(
			final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}

}
