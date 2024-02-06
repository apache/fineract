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
package org.apache.fineract.accounting.accrual.serialization;

import static org.apache.fineract.accounting.accrual.api.AccrualAccountingConstants.PERIODIC_ACCRUAL_ACCOUNTING_RESOURCE_NAME;
import static org.apache.fineract.accounting.accrual.api.AccrualAccountingConstants.accrueTillParamName;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.accrual.api.AccrualAccountingConstants;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class AccrualAccountingDataValidator {

    private static final Set<String> LOAN_PERIODIC_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(accrueTillParamName, AccrualAccountingConstants.localeParamName, AccrualAccountingConstants.dateFormatParamName));

    private final FromJsonHelper fromApiJsonHelper;

    public void validateLoanPeriodicAccrualData(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, LOAN_PERIODIC_REQUEST_DATA_PARAMETERS);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(PERIODIC_ACCRUAL_ACCOUNTING_RESOURCE_NAME);

        final LocalDate date = this.fromApiJsonHelper.extractLocalDateNamed(accrueTillParamName, element);
        baseDataValidator.reset().parameter(accrueTillParamName).value(date).notNull().validateDateBefore(DateUtils.getBusinessLocalDate());

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
