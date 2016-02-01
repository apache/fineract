/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.serialization;

import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.INDIVIDUAL_COLLECTIONSHEET_SUPPORTED_PARAMS;
import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.calendarIdParamName;
import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.dateFormatParamName;
import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.localeParamName;
import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.officeIdParamName;
import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.staffIdParamName;
import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.transactionDateParamName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class CollectionSheetGenerateCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    final Set<String> supportedParameters = new HashSet<>(Arrays.asList(transactionDateParamName, localeParamName, dateFormatParamName,
            calendarIdParamName));

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
