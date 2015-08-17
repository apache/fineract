/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.configuration.exception.ExternalServiceConfigurationNotFoundException;
import org.mifosplatform.infrastructure.configuration.service.ExternalServicesConstants;
import org.mifosplatform.infrastructure.configuration.service.ExternalServicesConstants.EXTERNALSERVICEPROPERTIES_JSON_INPUT_PARAMS;
import org.mifosplatform.infrastructure.configuration.service.ExternalServicesConstants.S3_JSON_INPUT_PARAMS;
import org.mifosplatform.infrastructure.configuration.service.ExternalServicesConstants.SMTP_JSON_INPUT_PARAMS;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class ExternalServicesPropertiesCommandFromApiJsonDeserializer {

    private final Set<String> supportedParameters = EXTERNALSERVICEPROPERTIES_JSON_INPUT_PARAMS.getAllValues();
    private final Set<String> S3SupportedParameters = S3_JSON_INPUT_PARAMS.getAllValues();
    private final Set<String> SMTPSupportedParameters = SMTP_JSON_INPUT_PARAMS.getAllValues();
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ExternalServicesPropertiesCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForUpdate(final String json, final String externalServiceName) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        switch (externalServiceName) {
            case "S3":
                this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.S3SupportedParameters);
            break;

            case "SMTP":
                this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.SMTPSupportedParameters);
            break;

            default:
                throw new ExternalServiceConfigurationNotFoundException(externalServiceName);
        }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("external.service");
        final JsonElement element = this.fromApiJsonHelper.parse(json);

    }

    public Set<String> getNameKeys(final String json) {
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, String> jsonMap = this.fromApiJsonHelper.extractDataMap(typeOfMap, json);
        Set<String> keyNames = jsonMap.keySet();
        return keyNames;
    }
}
