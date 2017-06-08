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
package org.apache.fineract.portfolio.search.data;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class AdHocQueryDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
	private static final Set<String> AD_HOC_SEARCH_QUERY_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(AdHocQuerySearchConstants.entitiesParamName, AdHocQuerySearchConstants.loanStatusParamName,
					AdHocQuerySearchConstants.loanProductsParamName, AdHocQuerySearchConstants.officesParamName,
					AdHocQuerySearchConstants.loanDateOptionParamName, AdHocQuerySearchConstants.loanFromDateParamName,
					AdHocQuerySearchConstants.loanToDateParamName,
					AdHocQuerySearchConstants.includeOutStandingAmountPercentageParamName,
					AdHocQuerySearchConstants.outStandingAmountPercentageConditionParamName,
					AdHocQuerySearchConstants.minOutStandingAmountPercentageParamName,
					AdHocQuerySearchConstants.maxOutStandingAmountPercentageParamName,
					AdHocQuerySearchConstants.outStandingAmountPercentageParamName,
					AdHocQuerySearchConstants.includeOutstandingAmountParamName,
					AdHocQuerySearchConstants.outstandingAmountConditionParamName,
					AdHocQuerySearchConstants.minOutstandingAmountParamName,
					AdHocQuerySearchConstants.maxOutstandingAmountParamName,
					AdHocQuerySearchConstants.outstandingAmountParamName, AdHocQuerySearchConstants.localeParamName,
					AdHocQuerySearchConstants.dateFormatParamName));

    private static final Set<String> AD_HOC_SEARCH_QUERY_CONDITIONS = new HashSet<>(
            Arrays.asList("between", "<=", ">=", "<", ">", "="));

    private static final Object[] loanDateOptions = { AdHocQuerySearchConstants.approvalDateOption,
            AdHocQuerySearchConstants.createDateOption,
            AdHocQuerySearchConstants.disbursalDateOption };

	private static final Object[] loanStatusOptions = {AdHocQuerySearchConstants.allLoanStatusOption,
			AdHocQuerySearchConstants.activeLoanStatusOption, AdHocQuerySearchConstants.overpaidLoanStatusOption,
			AdHocQuerySearchConstants.arrearsLoanStatusOption, AdHocQuerySearchConstants.closedLoanStatusOption,
			AdHocQuerySearchConstants.writeoffLoanStatusOption};

    @Autowired
    public AdHocQueryDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateAdHocQueryParameters(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, AD_HOC_SEARCH_QUERY_REQUEST_DATA_PARAMETERS);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(AdHocQuerySearchConstants.AD_HOC_SEARCH_QUERY_RESOURCE_NAME);

        final String[] entities = this.fromApiJsonHelper.extractArrayNamed(AdHocQuerySearchConstants.entitiesParamName, element);
        baseDataValidator.reset().parameter(AdHocQuerySearchConstants.entitiesParamName).value(entities).arrayNotEmpty();

        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.loanStatusParamName, element)) {
            final String[] loanStatus = this.fromApiJsonHelper.extractArrayNamed(AdHocQuerySearchConstants.loanStatusParamName, element);
            baseDataValidator.reset().parameter(AdHocQuerySearchConstants.loanStatusParamName).value(loanStatus).arrayNotEmpty();
            if (loanStatus != null && loanStatus.length > 0) {
                for (String status : loanStatus) {
                    baseDataValidator.reset().parameter(AdHocQuerySearchConstants.loanStatusParamName).value(status)
                            .isOneOfTheseValues(loanStatusOptions);
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.loanProductsParamName, element)) {
            final String[] loanProducts = this.fromApiJsonHelper
                    .extractArrayNamed(AdHocQuerySearchConstants.loanProductsParamName, element);
            baseDataValidator.reset().parameter(AdHocQuerySearchConstants.loanProductsParamName).value(loanProducts).arrayNotEmpty();
        }

        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.officesParamName, element)) {
            final String[] offices = this.fromApiJsonHelper.extractArrayNamed(AdHocQuerySearchConstants.officesParamName, element);
            baseDataValidator.reset().parameter(AdHocQuerySearchConstants.officesParamName).value(offices).arrayNotEmpty();
        }

        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.loanDateOptionParamName, element)) {
            final String loanDateOption = this.fromApiJsonHelper.extractStringNamed(AdHocQuerySearchConstants.loanDateOptionParamName,
                    element);
            baseDataValidator.reset().parameter(AdHocQuerySearchConstants.loanDateOptionParamName).value(loanDateOption)
                    .isOneOfTheseValues(loanDateOptions);
        }

        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.loanFromDateParamName, element)) {
            final LocalDate loanFromDate = this.fromApiJsonHelper.extractLocalDateNamed(AdHocQuerySearchConstants.loanFromDateParamName,
                    element);
            baseDataValidator.reset().parameter(AdHocQuerySearchConstants.loanFromDateParamName).value(loanFromDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.loanToDateParamName, element)) {
            final LocalDate loanToDate = this.fromApiJsonHelper.extractLocalDateNamed(AdHocQuerySearchConstants.loanToDateParamName,
                    element);
            baseDataValidator.reset().parameter(AdHocQuerySearchConstants.loanToDateParamName).value(loanToDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.includeOutStandingAmountPercentageParamName, element)) {
            final boolean includeOutStandingAmountPercentage = this.fromApiJsonHelper.extractBooleanNamed(
                    AdHocQuerySearchConstants.includeOutStandingAmountPercentageParamName, element);
            baseDataValidator.reset().parameter(AdHocQuerySearchConstants.includeOutStandingAmountPercentageParamName)
                    .value(includeOutStandingAmountPercentage).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.outStandingAmountPercentageConditionParamName, element)) {
            final String outStandingAmountPercentageCondition = this.fromApiJsonHelper.extractStringNamed(
                    AdHocQuerySearchConstants.outStandingAmountPercentageConditionParamName, element);
            baseDataValidator.reset().parameter(AdHocQuerySearchConstants.outStandingAmountPercentageConditionParamName)
                    .value(outStandingAmountPercentageCondition)
                    .isNotOneOfTheseValues(AD_HOC_SEARCH_QUERY_CONDITIONS);
            if (outStandingAmountPercentageCondition.equals("between")) {
                final BigDecimal minOutStandingAmountPercentage = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                        AdHocQuerySearchConstants.minOutStandingAmountPercentageParamName, element);
                baseDataValidator.reset().parameter(AdHocQuerySearchConstants.minOutStandingAmountPercentageParamName)
                        .value(minOutStandingAmountPercentage).notNull().notLessThanMin(BigDecimal.ZERO);
                final BigDecimal maxOutStandingAmountPercentage = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                        AdHocQuerySearchConstants.maxOutStandingAmountPercentageParamName, element);
                baseDataValidator.reset().parameter(AdHocQuerySearchConstants.maxOutStandingAmountPercentageParamName)
                        .value(maxOutStandingAmountPercentage).notNull().notLessThanMin(BigDecimal.ZERO);
                baseDataValidator.reset().comapareMinAndMaxOfTwoBigDecmimalNos(minOutStandingAmountPercentage,
                        maxOutStandingAmountPercentage);
            } else {
                if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.outStandingAmountPercentageParamName, element)) {
                    final BigDecimal outStandingAmountPercentage = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                            AdHocQuerySearchConstants.outStandingAmountPercentageParamName, element);
                    baseDataValidator.reset().parameter(AdHocQuerySearchConstants.outStandingAmountPercentageParamName)
                            .value(outStandingAmountPercentage).notNull().notLessThanMin(BigDecimal.ZERO);
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.includeOutstandingAmountParamName, element)) {
            final Boolean includeOutstandingAmountParamName = this.fromApiJsonHelper.extractBooleanNamed(
                    AdHocQuerySearchConstants.includeOutstandingAmountParamName, element);
            baseDataValidator.reset().parameter(AdHocQuerySearchConstants.includeOutstandingAmountParamName)
                    .value(includeOutstandingAmountParamName).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.outstandingAmountConditionParamName, element)) {
            final String outstandingAmountCondition = this.fromApiJsonHelper.extractStringNamed(
                    AdHocQuerySearchConstants.outstandingAmountConditionParamName, element);
            baseDataValidator.reset().parameter(AdHocQuerySearchConstants.outstandingAmountConditionParamName)
                    .value(outstandingAmountCondition).isNotOneOfTheseValues(AD_HOC_SEARCH_QUERY_CONDITIONS);
            if (outstandingAmountCondition.equals("between")) {
                final BigDecimal minOutstandingAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                        AdHocQuerySearchConstants.minOutstandingAmountParamName, element);
                baseDataValidator.reset().parameter(AdHocQuerySearchConstants.minOutstandingAmountParamName).value(minOutstandingAmount)
                        .notNull().notLessThanMin(BigDecimal.ZERO);
                final BigDecimal maxOutstandingAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                        AdHocQuerySearchConstants.maxOutstandingAmountParamName, element);
                baseDataValidator.reset().parameter(AdHocQuerySearchConstants.maxOutstandingAmountParamName).value(maxOutstandingAmount)
                        .notNull().notLessThanMin(BigDecimal.ZERO);
                baseDataValidator.reset().comapareMinAndMaxOfTwoBigDecmimalNos(minOutstandingAmount, maxOutstandingAmount);
            } else {
                final BigDecimal outstandingAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                        AdHocQuerySearchConstants.outstandingAmountParamName, element);
                baseDataValidator.reset().parameter(AdHocQuerySearchConstants.outstandingAmountParamName).value(outstandingAmount)
                        .notNull().notLessThanMin(BigDecimal.ZERO);
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public AdHocQuerySearchConditions retrieveSearchConditions(String json) {

        validateAdHocQueryParameters(json);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        List<String> loanStatus = null;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.loanStatusParamName, element)) {
            loanStatus = Arrays.asList(this.fromApiJsonHelper.extractArrayNamed(AdHocQuerySearchConstants.loanStatusParamName, element));
        }

        List<Long> loanProducts = null;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.loanProductsParamName, element)) {
            loanProducts = extractLongValuesList(Arrays.asList(this.fromApiJsonHelper.extractArrayNamed(
                    AdHocQuerySearchConstants.loanProductsParamName, element)));
        }

        List<Long> offices = null;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.officesParamName, element)) {
            offices = extractLongValuesList(Arrays.asList(this.fromApiJsonHelper.extractArrayNamed(
                    AdHocQuerySearchConstants.officesParamName, element)));
        }

        String loanDateOption = null;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.loanDateOptionParamName, element)) {
            loanDateOption = this.fromApiJsonHelper.extractStringNamed(AdHocQuerySearchConstants.loanDateOptionParamName, element);
        }

        LocalDate loanFromDate = null;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.loanFromDateParamName, element)) {
            loanFromDate = this.fromApiJsonHelper.extractLocalDateNamed(AdHocQuerySearchConstants.loanFromDateParamName, element);
        }

        LocalDate loanToDate = null;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.loanToDateParamName, element)) {
            loanToDate = this.fromApiJsonHelper.extractLocalDateNamed(AdHocQuerySearchConstants.loanToDateParamName, element);
        }

        Boolean includeOutStandingAmountPercentage = false;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.includeOutStandingAmountPercentageParamName, element)) {
            includeOutStandingAmountPercentage = this.fromApiJsonHelper.extractBooleanNamed(
                    AdHocQuerySearchConstants.includeOutStandingAmountPercentageParamName, element);
        }

        String outStandingAmountPercentageCondition = null;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.outStandingAmountPercentageConditionParamName, element)) {
            outStandingAmountPercentageCondition = this.fromApiJsonHelper.extractStringNamed(
                    AdHocQuerySearchConstants.outStandingAmountPercentageConditionParamName, element);
        }

        BigDecimal minOutStandingAmountPercentage = null;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.minOutStandingAmountPercentageParamName, element)) {
            minOutStandingAmountPercentage = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    AdHocQuerySearchConstants.minOutStandingAmountPercentageParamName, element);
        }

        BigDecimal maxOutStandingAmountPercentage = null;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.maxOutStandingAmountPercentageParamName, element)) {
            maxOutStandingAmountPercentage = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    AdHocQuerySearchConstants.maxOutStandingAmountPercentageParamName, element);
        }

        BigDecimal outStandingAmountPercentage = null;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.outStandingAmountPercentageParamName, element)) {
            outStandingAmountPercentage = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    AdHocQuerySearchConstants.outStandingAmountPercentageParamName, element);
        }

        Boolean includeOutstandingAmountParamName = false;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.includeOutstandingAmountParamName, element)) {
            includeOutstandingAmountParamName = this.fromApiJsonHelper.extractBooleanNamed(
                    AdHocQuerySearchConstants.includeOutstandingAmountParamName, element);
        }

        String outstandingAmountCondition = null;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.outstandingAmountConditionParamName, element)) {
            outstandingAmountCondition = this.fromApiJsonHelper.extractStringNamed(
                    AdHocQuerySearchConstants.outstandingAmountConditionParamName, element);
        }

        BigDecimal minOutstandingAmount = null;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.minOutstandingAmountParamName, element)) {
            minOutstandingAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    AdHocQuerySearchConstants.minOutstandingAmountParamName, element);
        }

        BigDecimal maxOutstandingAmount = null;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.maxOutstandingAmountParamName, element)) {
            maxOutstandingAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    AdHocQuerySearchConstants.maxOutstandingAmountParamName, element);
        }

        BigDecimal outstandingAmount = null;
        if (this.fromApiJsonHelper.parameterExists(AdHocQuerySearchConstants.outstandingAmountParamName, element)) {
            outstandingAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    AdHocQuerySearchConstants.outstandingAmountParamName, element);
        }

        return AdHocQuerySearchConditions.instance(loanStatus, loanProducts, offices, loanDateOption, loanFromDate, loanToDate,
                includeOutStandingAmountPercentage, outStandingAmountPercentageCondition, minOutStandingAmountPercentage,
                maxOutStandingAmountPercentage, outStandingAmountPercentage, includeOutstandingAmountParamName, outstandingAmountCondition,
                minOutstandingAmount, maxOutstandingAmount, outstandingAmount);

    }

    private List<Long> extractLongValuesList(List<String> listTobeConverted) {
        List<Long> tempList = new ArrayList<>();
        for (String temp : listTobeConverted) {
            tempList.add(Long.valueOf(temp));
        }
        return tempList;
    }

}
