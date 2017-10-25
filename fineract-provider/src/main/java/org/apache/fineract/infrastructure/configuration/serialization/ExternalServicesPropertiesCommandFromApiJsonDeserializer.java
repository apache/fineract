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
package org.apache.fineract.infrastructure.configuration.serialization;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.configuration.exception.ExternalServiceConfigurationNotFoundException;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesConstants.NOTIFICATION_JSON_INPUT_PARAMS;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesConstants.S3_JSON_INPUT_PARAMS;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesConstants.SMS_JSON_INPUT_PARAMS;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesConstants.SMTP_JSON_INPUT_PARAMS;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;

@Component
public class ExternalServicesPropertiesCommandFromApiJsonDeserializer {

    private final Set<String> S3SupportedParameters = S3_JSON_INPUT_PARAMS.getAllValues();
    private final Set<String> SMTPSupportedParameters = SMTP_JSON_INPUT_PARAMS.getAllValues();
    private final Set<String> SMSSupportedParameters = SMS_JSON_INPUT_PARAMS.getAllValues();
    private final Set<String> NotificationSupportedParameters = NOTIFICATION_JSON_INPUT_PARAMS.getAllValues();
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

            case "SMS":
                this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.SMSSupportedParameters);
            break;

            case "NOTIFICATION":
                this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.NotificationSupportedParameters);
            break;

            default:
                throw new ExternalServiceConfigurationNotFoundException(externalServiceName);
        }

    }

    public Set<String> getNameKeys(final String json) {
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, String> jsonMap = this.fromApiJsonHelper.extractDataMap(typeOfMap, json);
        Set<String> keyNames = jsonMap.keySet();
        return keyNames;
    }
}
