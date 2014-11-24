/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanaccount.guarantor.GuarantorConstants.GUARANTOR_JSON_INPUT_PARAMS;
import org.mifosplatform.portfolio.loanaccount.guarantor.command.GuarantorCommand;
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