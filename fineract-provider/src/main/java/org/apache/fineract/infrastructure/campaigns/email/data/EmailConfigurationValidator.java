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
package org.apache.fineract.infrastructure.campaigns.email.data;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.campaigns.email.EmailApiConstants;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailConfigurationSMTPUsernameNotValid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EmailConfigurationValidator {

    private final FromJsonHelper fromApiJsonHelper;

    private static final String EMAIL_REGEX = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static final Set<String> supportedParams = new HashSet<String>(Arrays.asList(EmailApiConstants.SMTP_PORT,EmailApiConstants.SMTP_PASSWORD,
            EmailApiConstants.SMTP_USERNAME,EmailApiConstants.SMTP_SERVER));

    @Autowired
    private EmailConfigurationValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }



    public void validateUpdateConfiguration(String json){

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, EmailConfigurationValidator.supportedParams);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(EmailApiConstants.RESOURCE_NAME);

        final String smtpUsername = this.fromApiJsonHelper.extractStringNamed(EmailApiConstants.SMTP_USERNAME,element);
        baseDataValidator.reset().parameter(EmailApiConstants.SMTP_USERNAME).value(smtpUsername).notBlank().notExceedingLengthOf(150);


        final String smtpPassword = this.fromApiJsonHelper.extractStringNamed(EmailApiConstants.SMTP_PASSWORD,element);
        baseDataValidator.reset().parameter(EmailApiConstants.SMTP_PASSWORD).value(smtpPassword).notBlank().notExceedingLengthOf(50);

        final String smtpServer = this.fromApiJsonHelper.extractStringNamed(EmailApiConstants.SMTP_SERVER,element);
        baseDataValidator.reset().parameter(EmailApiConstants.SMTP_SERVER).value(smtpServer).notBlank().notExceedingLengthOf(100);

        final Long smtpPort = this.fromApiJsonHelper.extractLongNamed(EmailApiConstants.SMTP_PORT,element);
        baseDataValidator.reset().parameter(EmailApiConstants.SMTP_PORT).value(smtpPort).notNull().integerGreaterThanZero();

        this.throwExceptionIfValidationWarningsExist(dataValidationErrors);



    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public boolean isValidEmail(String email) {
        // this is the easiest check
        if (email == null) {
            return false;
        }else if (email.endsWith(".")) {
            return false;
        }

        // Check the whole email address structure
        Matcher emailMatcher = EMAIL_PATTERN.matcher(email);

        // check if the Matcher matches the email pattern
        if (!emailMatcher.matches()) {
            return false;
        }

        return true;
    }
}
