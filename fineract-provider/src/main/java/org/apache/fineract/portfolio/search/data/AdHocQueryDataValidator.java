/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.search.data;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class AdHocQueryDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public AdHocQueryDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateAdHocQueryParameters(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                AdHocQuerySearchConstants.AD_HOC_SEARCH_QUERY_REQUEST_DATA_PARAMETERS);

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
                            .isOneOfTheseValues(AdHocQuerySearchConstants.loanStatusOptions);
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
                    .isOneOfTheseValues(AdHocQuerySearchConstants.loanDateOptions);
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
                    .isNotOneOfTheseValues(AdHocQuerySearchConstants.AD_HOC_SEARCH_QUERY_CONDITIONS);
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
                    .value(outstandingAmountCondition).isNotOneOfTheseValues(AdHocQuerySearchConstants.AD_HOC_SEARCH_QUERY_CONDITIONS);
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
