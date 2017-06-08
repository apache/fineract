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
package org.apache.fineract.accounting.provisioning.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.accounting.provisioning.constant.ProvisioningEntriesApiConstants;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.provisioning.exception.ProvisioningCriteriaCannotBeCreatedException;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class ProvisioningEntriesDefinitionJsonDeserializer implements ProvisioningEntriesApiConstants {

    private final FromJsonHelper fromApiJsonHelper;
	private static final Set<String> supportedParameters = new HashSet<>(
			Arrays.asList(JSON_DATE_PARAM, JSON_DATEFORMAT_PARAM, JSON_LOCALE_PARAM, JSON_CREATEJOURNALENTRIES_PARAM));


    @Autowired
    public ProvisioningEntriesDefinitionJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new ProvisioningCriteriaCannotBeCreatedException(
                "error.msg.provisioningentry.cannot.be.created",
                "locale, dateformat, date, createjournalentries params are missing in the request");

        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("provisioningcriteria");
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        baseDataValidator.reset().parameter(JSON_DATEFORMAT_PARAM).value(locale).notNull();
        final String dateformat = this.fromApiJsonHelper.extractDateFormatParameter(element.getAsJsonObject());
        baseDataValidator.reset().parameter(JSON_DATEFORMAT_PARAM).value(dateformat).notBlank();
        LocalDate localDate = this.fromApiJsonHelper.extractLocalDateNamed(JSON_DATE_PARAM, element) ;
        baseDataValidator.reset().parameter(JSON_DATE_PARAM).value(localDate).notBlank();
        baseDataValidator.reset().parameter(JSON_DATE_PARAM).value(localDate).validateDateBeforeOrEqual(DateUtils.getLocalDateOfTenant()) ;
        if(this.fromApiJsonHelper.parameterExists(JSON_CREATEJOURNALENTRIES_PARAM, element)) {
            Boolean bool = this.fromApiJsonHelper.extractBooleanNamed(JSON_CREATEJOURNALENTRIES_PARAM, element) ;
            baseDataValidator.reset().parameter(JSON_CREATEJOURNALENTRIES_PARAM).value(bool).validateForBooleanValue() ;
        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}
