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
package org.apache.fineract.portfolio.savings.service;

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
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class SavingsAccountApplicationTransitionApiJsonValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SavingsAccountApplicationTransitionApiJsonValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public void validateApproval(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> disbursementParameters = new HashSet<>(Arrays.asList("approvedOnDate", "note", "locale", "dateFormat"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("savingsaccountapplication");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate approvedOnDate = this.fromApiJsonHelper.extractLocalDateNamed("approvedOnDate", element);
        baseDataValidator.reset().parameter("approvedOnDate").value(approvedOnDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateRejection(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> disbursementParameters = new HashSet<>(Arrays.asList("rejectedOnDate", "note", "locale", "dateFormat"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("savingsaccountapplication");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate rejectedOnDate = this.fromApiJsonHelper.extractLocalDateNamed("rejectedOnDate", element);
        baseDataValidator.reset().parameter("rejectedOnDate").value(rejectedOnDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateApplicantWithdrawal(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> disbursementParameters = new HashSet<>(Arrays.asList("withdrawnOnDate", "note", "locale", "dateFormat"));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("savingsaccountapplication");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate withdrawnOnDate = this.fromApiJsonHelper.extractLocalDateNamed("withdrawnOnDate", element);
        baseDataValidator.reset().parameter("withdrawnOnDate").value(withdrawnOnDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);
        baseDataValidator.reset().parameter("note").value(note).notExceedingLengthOf(1000);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUndo(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> undoSupportedParameters = new HashSet<>(Arrays.asList("note"));
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, undoSupportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("savingsaccountapplication.undo");
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String note = "note";
        if (this.fromApiJsonHelper.parameterExists(note, element)) {
            final String noteText = this.fromApiJsonHelper.extractStringNamed(note, element);
            baseDataValidator.reset().parameter(note).value(noteText).notExceedingLengthOf(1000);
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}