/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.provisioning.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.provisioning.constant.ProvisioningEntriesApiConstants;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.provisioning.exception.ProvisioningCriteriaCannotBeCreatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class ProvisioningEntriesDefinitionJsonDeserializer implements ProvisioningEntriesApiConstants {

    private final FromJsonHelper fromApiJsonHelper;

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
