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
package org.apache.fineract.portfolio.collectionsheet.serialization;

import static org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants.calendarIdParamName;
import static org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants.dateFormatParamName;
import static org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants.localeParamName;
import static org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants.officeIdParamName;
import static org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants.staffIdParamName;
import static org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants.transactionDateParamName;

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
public class CollectionSheetGenerateCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
	final Set<String> supportedParameters = new HashSet<>(
			Arrays.asList(transactionDateParamName, localeParamName, dateFormatParamName, calendarIdParamName));

	private static final Set<String> INDIVIDUAL_COLLECTIONSHEET_SUPPORTED_PARAMS = new HashSet<>(Arrays.asList(
			transactionDateParamName, localeParamName, dateFormatParamName, officeIdParamName, staffIdParamName));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CollectionSheetGenerateCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForGenerateCollectionSheet(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("collectionsheet");

        final String transactionDateStr = this.fromApiJsonHelper.extractStringNamed(transactionDateParamName, element);
        baseDataValidator.reset().parameter(transactionDateParamName).value(transactionDateStr).notBlank();

        if (!StringUtils.isBlank(transactionDateStr)) {
            final LocalDate dueDate = this.fromApiJsonHelper.extractLocalDateNamed(transactionDateParamName, element);
            baseDataValidator.reset().parameter(transactionDateParamName).value(dueDate).notNull();
        }

        final Long calendarId = this.fromApiJsonHelper.extractLongNamed(calendarIdParamName, element);
        baseDataValidator.reset().parameter(calendarIdParamName).value(calendarId).notNull();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForGenerateCollectionSheetOfIndividuals(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, INDIVIDUAL_COLLECTIONSHEET_SUPPORTED_PARAMS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("collectionsheet");

        final String transactionDateStr = this.fromApiJsonHelper.extractStringNamed(transactionDateParamName, element);
        baseDataValidator.reset().parameter(transactionDateParamName).value(transactionDateStr).notBlank();

        if (!StringUtils.isBlank(transactionDateStr)) {
            final LocalDate dueDate = this.fromApiJsonHelper.extractLocalDateNamed(transactionDateParamName, element);
            baseDataValidator.reset().parameter(transactionDateParamName).value(dueDate).notNull();
        }

        final Long officeId = this.fromApiJsonHelper.extractLongNamed(officeIdParamName, element);
        baseDataValidator.reset().parameter(officeIdParamName).value(officeId).longGreaterThanZero();

        final Long staffId = this.fromApiJsonHelper.extractLongNamed(staffIdParamName, element);
        baseDataValidator.reset().parameter(staffIdParamName).value(staffId).longGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}
