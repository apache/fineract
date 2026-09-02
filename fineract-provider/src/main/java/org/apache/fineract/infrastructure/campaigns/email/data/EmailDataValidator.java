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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.campaigns.email.EmailApiConstants;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class EmailDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private static final String EMAIL_REGEX = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Autowired
    public EmailDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    /**
     * validate the request to create a new email message
     *
     * @param jsonCommand
     *            -- the JSON command object (instance of the JsonCommand class)
     * @return None
     **/
    public void validateCreateRequest(final JsonCommand jsonCommand) {
        final String jsonString = jsonCommand.json();

        if (StringUtils.isBlank(jsonString)) {
            throw new InvalidJsonException();
        }

        final Type typeToken = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeToken, jsonString, EmailApiConstants.CREATE_REQUEST_DATA_PARAMETERS);

        final JsonElement element = this.fromApiJsonHelper.parse(jsonString);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(StringUtils.lowerCase(EmailApiConstants.RESOURCE_NAME));

        final String emailSubject = this.fromApiJsonHelper.extractStringNamed(EmailApiConstants.subjectParamName, element);
        baseDataValidator.reset().parameter(EmailApiConstants.subjectParamName).value(emailSubject).notBlank().notExceedingLengthOf(50);

        final String emailMessage = this.fromApiJsonHelper.extractStringNamed(EmailApiConstants.messageParamName, element);
        baseDataValidator.reset().parameter(EmailApiConstants.messageParamName).value(emailMessage).notBlank();

        final Long clientId = this.fromApiJsonHelper.extractLongNamed(EmailApiConstants.clientIdParamName, element);
        final Long staffId = this.fromApiJsonHelper.extractLongNamed(EmailApiConstants.staffIdParamName, element);
        if (clientId == null && staffId == null) {
            baseDataValidator.reset().parameter(EmailApiConstants.clientIdParamName).value(clientId)
                    .failWithCode("either.clientId.or.staffId.must.be.provided", "Either `" + EmailApiConstants.clientIdParamName + "` or `"
                            + EmailApiConstants.staffIdParamName + "` must be provided.");
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    /**
     * validate the request to update an email message
     *
     * @param jsonCommand
     *            -- the JSON command object (instance of the JsonCommand class)
     * @return None
     **/
    public void validateUpdateRequest(final JsonCommand jsonCommand) {
        final String jsonString = jsonCommand.json();

        if (StringUtils.isBlank(jsonString)) {
            throw new InvalidJsonException();
        }

        final Type typeToken = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeToken, jsonString, EmailApiConstants.UPDATE_REQUEST_DATA_PARAMETERS);
    }

    /**
     * check if string is a valid email address
     *
     * @param email
     *            -- string to be validated
     * @return true if string is a valid email address
     **/
    public boolean isValidEmail(String email) {
        // this is the easiest check
        if (email == null) {
            return false;
        }

        // this is another easy check
        if (email.endsWith(".")) {
            return false;
        }

        // Check the whole email address structure
        Matcher emailMatcher = EMAIL_PATTERN.matcher(email);

        // check if the Matcher matches the email pattern
        return emailMatcher.matches();
    }

    /**
     * Validate the email recipients string
     *
     * @param emailRecipients
     *            -- the email recipients string to be validated
     * @return a hashset containing valid email addresses
     **/
    public Set<String> validateEmailRecipients(String emailRecipients) {
        Set<String> emailRecipientsSet = new HashSet<>();

        if (emailRecipients != null) {
            Iterable<String> split = Splitter.on(',').split(emailRecipients);

            for (String emailAddress : split) {
                emailAddress = emailAddress.trim();

                if (this.isValidEmail(emailAddress)) {
                    emailRecipientsSet.add(emailAddress);
                }
            }
        }

        return emailRecipientsSet;
    }

    /**
     * validate the stretchy report param json string
     *
     * @param stretchyReportParamMap
     *            -- json string to be validated
     * @return if string is valid or empty, a HashMap object, else null
     **/
    public HashMap<String, String> validateStretchyReportParamMap(String stretchyReportParamMap) {
        HashMap<String, String> stretchyReportParamHashMap = new HashMap<>();

        if (!StringUtils.isEmpty(stretchyReportParamMap)) {
            try {
                stretchyReportParamHashMap = new ObjectMapper().readValue(stretchyReportParamMap,
                        new TypeReference<HashMap<String, String>>() {});
            }

            catch (Exception e) {
                stretchyReportParamHashMap = null;
            }
        }

        return stretchyReportParamHashMap;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
