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
package org.apache.fineract.portfolio.paymenttype.data;

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
import org.apache.fineract.portfolio.paymenttype.api.PaymentTypeApiResourceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class PaymentTypeDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
	private static final Set<String> CREATE_PAYMENT_TYPE_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(PaymentTypeApiResourceConstants.NAME, PaymentTypeApiResourceConstants.DESCRIPTION,
					PaymentTypeApiResourceConstants.ISCASHPAYMENT, PaymentTypeApiResourceConstants.POSITION));

	private static final Set<String> UPDATE_PAYMENT_TYPE_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(PaymentTypeApiResourceConstants.NAME, PaymentTypeApiResourceConstants.DESCRIPTION,
					PaymentTypeApiResourceConstants.ISCASHPAYMENT, PaymentTypeApiResourceConstants.POSITION));

    @Autowired
    public PaymentTypeDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CREATE_PAYMENT_TYPE_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(PaymentTypeApiResourceConstants.resourceNameForPermissions);

        if (this.fromApiJsonHelper.parameterExists(PaymentTypeApiResourceConstants.NAME, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(PaymentTypeApiResourceConstants.NAME, element);
            baseDataValidator.reset().parameter(PaymentTypeApiResourceConstants.NAME).value(name).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(PaymentTypeApiResourceConstants.DESCRIPTION, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(PaymentTypeApiResourceConstants.DESCRIPTION, element);
            baseDataValidator.reset().parameter(PaymentTypeApiResourceConstants.DESCRIPTION).value(description).ignoreIfNull().notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.parameterExists(PaymentTypeApiResourceConstants.ISCASHPAYMENT, element)) {
            final Boolean isCashPayment = this.fromApiJsonHelper
                    .extractBooleanNamed(PaymentTypeApiResourceConstants.ISCASHPAYMENT, element);
            baseDataValidator.reset().parameter(PaymentTypeApiResourceConstants.ISCASHPAYMENT).value(isCashPayment).validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(PaymentTypeApiResourceConstants.POSITION, element)) {
            final Long position = this.fromApiJsonHelper.extractLongNamed(PaymentTypeApiResourceConstants.POSITION, element);
            baseDataValidator.reset().parameter(PaymentTypeApiResourceConstants.POSITION).value(position).ignoreIfNull()
                    .integerZeroOrGreater();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
				UPDATE_PAYMENT_TYPE_REQUEST_DATA_PARAMETERS);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(PaymentTypeApiResourceConstants.resourceNameForPermissions);

        if (this.fromApiJsonHelper.parameterExists(PaymentTypeApiResourceConstants.NAME, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(PaymentTypeApiResourceConstants.NAME, element);
            baseDataValidator.reset().parameter(PaymentTypeApiResourceConstants.NAME).value(name);
        }

        if (this.fromApiJsonHelper.parameterExists(PaymentTypeApiResourceConstants.DESCRIPTION, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(PaymentTypeApiResourceConstants.DESCRIPTION, element);
            baseDataValidator.reset().parameter(PaymentTypeApiResourceConstants.DESCRIPTION).value(description).ignoreIfNull().notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.parameterExists(PaymentTypeApiResourceConstants.ISCASHPAYMENT, element)) {
            final Boolean isCashPayment = this.fromApiJsonHelper
                    .extractBooleanNamed(PaymentTypeApiResourceConstants.ISCASHPAYMENT, element);
            baseDataValidator.reset().parameter(PaymentTypeApiResourceConstants.ISCASHPAYMENT).value(isCashPayment).validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(PaymentTypeApiResourceConstants.POSITION, element)) {
            final Long position = this.fromApiJsonHelper.extractLongNamed(PaymentTypeApiResourceConstants.POSITION, element);
            baseDataValidator.reset().parameter(PaymentTypeApiResourceConstants.POSITION).value(position).ignoreIfNull()
                    .integerZeroOrGreater();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }
}
