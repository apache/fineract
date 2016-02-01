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
package org.apache.fineract.portfolio.loanaccount.guarantor.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanaccount.guarantor.GuarantorConstants.GUARANTOR_JSON_INPUT_PARAMS;
import org.apache.fineract.portfolio.loanaccount.guarantor.command.GuarantorCommand;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link GuarantorCommand}'s.
 */
@Component
public final class GuarantorCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<GuarantorCommand> {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public GuarantorCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public GuarantorCommand commandFromApiJson(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        final Set<String> supportedParameters = GUARANTOR_JSON_INPUT_PARAMS.getAllValues();
        supportedParameters.add("locale");
        supportedParameters.add("dateFormat");
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(element.getAsJsonObject());

        return extractGuarantorCommand(element, locale, dateFormat);
    }

    private GuarantorCommand extractGuarantorCommand(final JsonElement element, final Locale locale, final String dateFormat) {
        final Long clientRelationshipTypeId = this.fromApiJsonHelper.extractLongNamed(
                GUARANTOR_JSON_INPUT_PARAMS.CLIENT_RELATIONSHIP_TYPE_ID.getValue(), element);
        final Integer guarantorTypeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                GUARANTOR_JSON_INPUT_PARAMS.GUARANTOR_TYPE_ID.getValue(), element);
        final Long entityId = this.fromApiJsonHelper.extractLongNamed(GUARANTOR_JSON_INPUT_PARAMS.ENTITY_ID.getValue(), element);
        final String firstname = this.fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.FIRSTNAME.getValue(), element);
        final String lastname = this.fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.LASTNAME.getValue(), element);
        final String addressLine1 = this.fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.ADDRESS_LINE_1.getValue(),
                element);
        final String addressLine2 = this.fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.ADDRESS_LINE_2.getValue(),
                element);
        final String city = this.fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.CITY.getValue(), element);
        final String state = this.fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.STATE.getValue(), element);
        final String zip = this.fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.ZIP.getValue(), element);
        final String country = this.fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.COUNTRY.getValue(), element);
        final String mobileNumber = this.fromApiJsonHelper
                .extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.MOBILE_NUMBER.getValue(), element);
        final String housePhoneNumber = this.fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.PHONE_NUMBER.getValue(),
                element);
        final String comment = this.fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.COMMENT.getValue(), element);
        final LocalDate dob = this.fromApiJsonHelper.extractLocalDateNamed(GUARANTOR_JSON_INPUT_PARAMS.DATE_OF_BIRTH.getValue(), element,
                dateFormat, locale);
        final Long savingsId = this.fromApiJsonHelper.extractLongNamed(GUARANTOR_JSON_INPUT_PARAMS.SAVINGS_ID.getValue(), element);
        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed(GUARANTOR_JSON_INPUT_PARAMS.AMOUNT.getValue(), element,
                locale);

        return new GuarantorCommand(clientRelationshipTypeId, guarantorTypeId, entityId, firstname, lastname, addressLine1, addressLine2,
                city, state, zip, country, mobileNumber, housePhoneNumber, comment, dob, savingsId, amount);
    }

}