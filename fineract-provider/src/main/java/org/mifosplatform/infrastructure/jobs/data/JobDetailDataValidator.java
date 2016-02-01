/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.jobs.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.jobs.api.SchedulerJobApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class JobDetailDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public JobDetailDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        boolean atLeastOneParameterPassedForUpdate = false;
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SchedulerJobApiConstants.JOB_UPDATE_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SchedulerJobApiConstants.JOB_RESOURCE_NAME);
        if (this.fromApiJsonHelper.parameterExists(SchedulerJobApiConstants.displayNameParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String displayName = this.fromApiJsonHelper.extractStringNamed(SchedulerJobApiConstants.displayNameParamName, element);
            baseDataValidator.reset().parameter(SchedulerJobApiConstants.displayNameParamName).value(displayName).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(SchedulerJobApiConstants.cronExpressionParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String cronExpression = this.fromApiJsonHelper.extractStringNamed(SchedulerJobApiConstants.cronExpressionParamName,
                    element);
            baseDataValidator.reset().parameter(SchedulerJobApiConstants.cronExpressionParamName).value(cronExpression).notBlank()
                    .validateCronExpression();
        }
        if (this.fromApiJsonHelper.parameterExists(SchedulerJobApiConstants.jobActiveStatusParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String status = this.fromApiJsonHelper.extractStringNamed(SchedulerJobApiConstants.jobActiveStatusParamName, element);
            baseDataValidator.reset().parameter(SchedulerJobApiConstants.jobActiveStatusParamName).value(status).notBlank()
                    .validateForBooleanValue();
        }

        if (!atLeastOneParameterPassedForUpdate) {
            final Object forceError = null;
            baseDataValidator.reset().anyOfNotNull(forceError);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

}
