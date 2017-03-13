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
package org.apache.fineract.infrastructure.creditbureau.serialization;

import java.lang.reflect.Type;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class CreditBureauLoanProductCommandFromApiJsonDeserializer {
	private final Set<String> supportedParameters = new HashSet<>(Arrays.asList("loan_product_id",
			"is_creditcheck_mandatory", "skip_creditcheck_in_failure", "stale_period", "is_active", "locale"));

	private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public CreditBureauLoanProductCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	public void validateForCreate(final String json, final Long cb_id) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
				.resource("CREDITBUREAU_LOANPRODUCT_MAPPING");

		final JsonElement element = this.fromApiJsonHelper.parse(json);

		baseDataValidator.reset().value(cb_id).notBlank().integerGreaterThanZero();

		final long loan_product_id = this.fromApiJsonHelper.extractLongNamed("loan_product_id", element);
		baseDataValidator.reset().parameter("loan_product_id").value(loan_product_id).notBlank()
				.integerGreaterThanZero();
		System.out.println("loan product id " + loan_product_id);
			
		if(this.fromApiJsonHelper.extractBooleanNamed("is_creditcheck_mandatory",element)!= null)
		{
			final boolean is_creditcheck_mandatory = this.fromApiJsonHelper.extractBooleanNamed("is_creditcheck_mandatory",
					element);
			baseDataValidator.reset().parameter("is_creditcheck_mandatory").value(is_creditcheck_mandatory).notBlank()
					.trueOrFalseRequired(is_creditcheck_mandatory);	
		}
		else
		{
			baseDataValidator.reset().parameter("is_creditcheck_mandatory").value(this.fromApiJsonHelper.extractBooleanNamed("is_creditcheck_mandatory",
					element)).notBlank()
			.trueOrFalseRequired(this.fromApiJsonHelper.extractBooleanNamed("is_creditcheck_mandatory",
					element));	
		}
		
		
		if( this.fromApiJsonHelper.extractBooleanNamed("skip_creditcheck_in_failure", element)!=null)
		{
			final boolean skip_creditcheck_in_failure = this.fromApiJsonHelper.extractBooleanNamed("skip_creditcheck_in_failure", element);
			baseDataValidator.reset().parameter("skip_creditcheck_in_failure").value(skip_creditcheck_in_failure).notBlank().trueOrFalseRequired(skip_creditcheck_in_failure);
				
		}
		else
		{
			baseDataValidator.reset().parameter("skip_creditcheck_in_failure").value(this.fromApiJsonHelper.extractBooleanNamed("skip_creditcheck_in_failure", element)).notBlank().trueOrFalseRequired(this.fromApiJsonHelper.extractBooleanNamed("skip_creditcheck_in_failure", element));	
		}
		

		if(this.fromApiJsonHelper.extractLongNamed("stale_period", element)!=null)
		{
			final long stale_period = this.fromApiJsonHelper.extractLongNamed("stale_period", element);
			baseDataValidator.reset().parameter("stale_period").value(stale_period).notBlank().integerGreaterThanZero();	
		}
		else
		{
			baseDataValidator.reset().parameter("stale_period").value(this.fromApiJsonHelper.extractLongNamed("stale_period", element)).notBlank().integerGreaterThanZero();		
		}

		
		if(this.fromApiJsonHelper.extractBooleanNamed("is_active", element)!=null)
		{
			Boolean is_active = this.fromApiJsonHelper.extractBooleanNamed("is_active", element);
			if (is_active == null) {
				is_active = false;
			} else {

				baseDataValidator.reset().parameter("is_active").value(is_active).notBlank().trueOrFalseRequired(is_active);
			}	
		}

	

		throwExceptionIfValidationWarningsExist(dataValidationErrors);

	}

	private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}
}
