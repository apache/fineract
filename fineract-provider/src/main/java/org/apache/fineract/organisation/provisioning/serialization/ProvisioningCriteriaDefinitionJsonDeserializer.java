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
package org.apache.fineract.organisation.provisioning.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.provisioning.constants.ProvisioningCriteriaConstants;
import org.apache.fineract.organisation.provisioning.exception.ProvisioningCriteriaCannotBeCreatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public class ProvisioningCriteriaDefinitionJsonDeserializer implements ProvisioningCriteriaConstants {

	private final FromJsonHelper fromApiJsonHelper;

	private final static Set<String> supportedParametersForCreate = new HashSet<>(Arrays.asList(JSON_LOCALE_PARAM,
			JSON_CRITERIANAME_PARAM, JSON_LOANPRODUCTS_PARAM, JSON_PROVISIONING_DEFINITIONS_PARAM));

	private final static Set<String> supportedParametersForUpdate = new HashSet<>(Arrays.asList(JSON_CRITERIAID_PARAM,
			JSON_LOCALE_PARAM, JSON_CRITERIANAME_PARAM, JSON_LOANPRODUCTS_PARAM, JSON_PROVISIONING_DEFINITIONS_PARAM));

	private final static Set<String> loanProductSupportedParams = new HashSet<>(Arrays
			.asList(JSON_LOAN_PRODUCT_ID_PARAM, JSON_LOAN_PRODUCTNAME_PARAM, JSON_LOAN_PRODUCT_BORROWERCYCLE_PARAM));

	private final static Set<String> provisioningcriteriaSupportedParams = new HashSet<>(
			Arrays.asList(JSON_CATEOGRYID_PARAM, JSON_CATEOGRYNAME_PARAM, JSON_MINIMUM_AGE_PARAM,
					JSON_MAXIMUM_AGE_PARAM, JSON_MINIMUM_AGE_PARAM, JSON_PROVISIONING_PERCENTAGE_PARAM,
					JSON_EXPENSE_ACCOUNT_PARAM, JSON_LIABILITY_ACCOUNT_PARAM));

    @Autowired
    public ProvisioningCriteriaDefinitionJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new ProvisioningCriteriaCannotBeCreatedException(
                "error.msg.provisioningcriteria.cannot.be.created",
                "criterianame, loanproducts[], provisioningcriteria[] params are missing in the request");

        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParametersForCreate);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("provisioningcriteria");
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        final String name = this.fromApiJsonHelper.extractStringNamed(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM, element);
        baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM).value(name).notBlank()
                .notExceedingLengthOf(200);

        // if the param present, then we should have the loan product ids. If
        // not we will load all loan products
        if (this.fromApiJsonHelper.parameterExists(ProvisioningCriteriaConstants.JSON_LOANPRODUCTS_PARAM, element)) {
            JsonArray jsonloanProducts = this.fromApiJsonHelper.extractJsonArrayNamed(ProvisioningCriteriaConstants.JSON_LOANPRODUCTS_PARAM,
                    element);
            baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_LOANPRODUCTS_PARAM).value(jsonloanProducts)
                    .jsonArrayNotEmpty();
            // check for unsupported params
            int i = 0 ;
            for (JsonElement obj : jsonloanProducts) {
                this.fromApiJsonHelper.checkForUnsupportedParameters(obj.getAsJsonObject() , loanProductSupportedParams);
                Long productId = this.fromApiJsonHelper.extractLongNamed("id", obj.getAsJsonObject());
                baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_LOANPRODUCTS_PARAM)
                .parameterAtIndexArray(ProvisioningCriteriaConstants.JSON_LOAN_PRODUCT_ID_PARAM, i + 1).value(productId).notNull()
                .longGreaterThanZero();
                i++ ;
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ProvisioningCriteriaConstants.JSON_PROVISIONING_DEFINITIONS_PARAM, element)) {
            JsonArray jsonProvisioningCriteria = this.fromApiJsonHelper.extractJsonArrayNamed(
                    ProvisioningCriteriaConstants.JSON_PROVISIONING_DEFINITIONS_PARAM, element);
            baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_PROVISIONING_DEFINITIONS_PARAM)
                    .value(jsonProvisioningCriteria).jsonArrayNotEmpty();
            for (int i = 0; i < jsonProvisioningCriteria.size(); i++) {
                JsonObject jsonObject = jsonProvisioningCriteria.get(i).getAsJsonObject();
                this.fromApiJsonHelper.checkForUnsupportedParameters(jsonObject , provisioningcriteriaSupportedParams);
                final Long categoryId = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_CATEOGRYID_PARAM, jsonObject);
                baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_PROVISIONING_DEFINITIONS_PARAM)
                        .parameterAtIndexArray(ProvisioningCriteriaConstants.JSON_CATEOGRYID_PARAM, i + 1).value(categoryId).notNull()
                        .longGreaterThanZero();

                Long minimumAge = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_MINIMUM_AGE_PARAM, jsonObject);
                baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_MINIMUM_AGE_PARAM)
                        .parameterAtIndexArray(ProvisioningCriteriaConstants.JSON_MINIMUM_AGE_PARAM, i + 1).value(minimumAge).notNull()
                        .longZeroOrGreater() ;

                Long maximumAge = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_MAXIMUM_AGE_PARAM, jsonObject);
                baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_MAXIMUM_AGE_PARAM)
                        .parameterAtIndexArray(ProvisioningCriteriaConstants.JSON_MAXIMUM_AGE_PARAM, i + 1).value(maximumAge).notNull()
                        .longGreaterThanNumber(ProvisioningCriteriaConstants.JSON_MINIMUM_AGE_PARAM, minimumAge, (i+1));

                
                BigDecimal provisioningpercentage = this.fromApiJsonHelper.extractBigDecimalNamed(
                        ProvisioningCriteriaConstants.JSON_PROVISIONING_PERCENTAGE_PARAM, jsonObject, locale);
                baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_PROVISIONING_PERCENTAGE_PARAM)
                        .parameterAtIndexArray(ProvisioningCriteriaConstants.JSON_PROVISIONING_PERCENTAGE_PARAM, i + 1).value(provisioningpercentage)
                        .notNull().zeroOrPositiveAmount();

                Long liabilityAccountId = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_LIABILITY_ACCOUNT_PARAM,
                        jsonObject);
                baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_LIABILITY_ACCOUNT_PARAM)
                        .parameterAtIndexArray(ProvisioningCriteriaConstants.JSON_LIABILITY_ACCOUNT_PARAM, i + 1).value(liabilityAccountId)
                        .notNull().longGreaterThanZero() ;

                Long expenseAccountId = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_EXPENSE_ACCOUNT_PARAM,
                        jsonObject);
                baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_EXPENSE_ACCOUNT_PARAM)
                        .parameterAtIndexArray(ProvisioningCriteriaConstants.JSON_EXPENSE_ACCOUNT_PARAM, i + 1).value(expenseAccountId).notNull()
                        .longGreaterThanZero() ;
            }
        }else {
            baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_PROVISIONING_DEFINITIONS_PARAM).jsonArrayNotEmpty() ;
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new ProvisioningCriteriaCannotBeCreatedException(
                "error.msg.provisioningcriteria.cannot.be.updated",
                "update params are missing in the request");

        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParametersForUpdate);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("provisioningcriteria");
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        
        if(this.fromApiJsonHelper.parameterExists(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM, element);
            baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM).value(name).notBlank()
                    .notExceedingLengthOf(200);    
        }
        

        // if the param present, then we should have the loan product ids. If
        // not we will load all loan products
        if (this.fromApiJsonHelper.parameterExists(ProvisioningCriteriaConstants.JSON_LOANPRODUCTS_PARAM, element)) {
            JsonArray jsonloanProducts = this.fromApiJsonHelper.extractJsonArrayNamed(ProvisioningCriteriaConstants.JSON_LOANPRODUCTS_PARAM,
                    element);
            baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_LOANPRODUCTS_PARAM).value(jsonloanProducts)
                    .jsonArrayNotEmpty();
            // check for unsupported params
            int i = 0 ;
            for (JsonElement obj : jsonloanProducts) {
                Long productId = this.fromApiJsonHelper.extractLongNamed("id", obj.getAsJsonObject());
                baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_LOANPRODUCTS_PARAM)
                .parameterAtIndexArray(ProvisioningCriteriaConstants.JSON_LOAN_PRODUCT_ID_PARAM, i + 1).value(productId).notNull()
                .longGreaterThanZero();
                i++ ;
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ProvisioningCriteriaConstants.JSON_PROVISIONING_DEFINITIONS_PARAM, element)) {
            JsonArray jsonProvisioningCriteria = this.fromApiJsonHelper.extractJsonArrayNamed(
                    ProvisioningCriteriaConstants.JSON_PROVISIONING_DEFINITIONS_PARAM, element);
            baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_PROVISIONING_DEFINITIONS_PARAM)
                    .value(jsonProvisioningCriteria).jsonArrayNotEmpty();
            for (int i = 0; i < jsonProvisioningCriteria.size(); i++) {
                // check for unsupported params
                JsonObject jsonObject = jsonProvisioningCriteria.get(i).getAsJsonObject();
                final Long categoryId = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_CATEOGRYID_PARAM, jsonObject);
                baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_PROVISIONING_DEFINITIONS_PARAM)
                        .parameterAtIndexArray(ProvisioningCriteriaConstants.JSON_CATEOGRYID_PARAM, i + 1).value(categoryId).notNull()
                        .longGreaterThanZero();

                Long minimumAge = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_MINIMUM_AGE_PARAM, jsonObject);
                baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_PROVISIONING_DEFINITIONS_PARAM)
                        .parameterAtIndexArray(ProvisioningCriteriaConstants.JSON_MINIMUM_AGE_PARAM, i + 1).value(minimumAge).notNull()
                        .longZeroOrGreater() ;

                Long maximumAge = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_MAXIMUM_AGE_PARAM, jsonObject);
                baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_MAXIMUM_AGE_PARAM)
                .parameterAtIndexArray(ProvisioningCriteriaConstants.JSON_MAXIMUM_AGE_PARAM, i + 1).value(maximumAge).notNull()
                .longGreaterThanNumber(ProvisioningCriteriaConstants.JSON_MINIMUM_AGE_PARAM, minimumAge, (i+1));

                BigDecimal provisioningpercentage = this.fromApiJsonHelper.extractBigDecimalNamed(
                        ProvisioningCriteriaConstants.JSON_PROVISIONING_PERCENTAGE_PARAM, jsonObject, locale);
                baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_PROVISIONING_DEFINITIONS_PARAM)
                        .parameterAtIndexArray(ProvisioningCriteriaConstants.JSON_PROVISIONING_PERCENTAGE_PARAM, i + 1).value(provisioningpercentage)
                        .notNull().zeroOrPositiveAmount();

                Long liabilityAccountId = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_LIABILITY_ACCOUNT_PARAM,
                        jsonObject);
                baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_PROVISIONING_DEFINITIONS_PARAM)
                        .parameterAtIndexArray(ProvisioningCriteriaConstants.JSON_LIABILITY_ACCOUNT_PARAM, i + 1).value(liabilityAccountId)
                        .notNull().longGreaterThanZero() ;

                Long expenseAccountId = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_EXPENSE_ACCOUNT_PARAM,
                        jsonObject);
                baseDataValidator.reset().parameter(ProvisioningCriteriaConstants.JSON_PROVISIONING_DEFINITIONS_PARAM)
                        .parameterAtIndexArray(ProvisioningCriteriaConstants.JSON_EXPENSE_ACCOUNT_PARAM, i + 1).value(expenseAccountId).notNull()
                        .longGreaterThanZero() ;
            }
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    
    }

}
