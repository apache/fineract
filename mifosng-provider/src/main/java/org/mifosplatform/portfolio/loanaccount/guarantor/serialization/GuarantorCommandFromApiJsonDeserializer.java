package org.mifosplatform.portfolio.loanaccount.guarantor.serialization;

import java.lang.reflect.Type;
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
    public GuarantorCommand commandFromApiJson(String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        Set<String> supportedParameters = GUARANTOR_JSON_INPUT_PARAMS.getAllValues();
        supportedParameters.add("locale");
        supportedParameters.add("dateFormat");
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = fromApiJsonHelper.parse(json);

        Long loanId = fromApiJsonHelper.extractLongNamed(GUARANTOR_JSON_INPUT_PARAMS.LOAN_ID.getValue(), element);
        Integer guarantorTypeId = fromApiJsonHelper.extractIntegerSansLocaleNamed(GUARANTOR_JSON_INPUT_PARAMS.GUARANTOR_TYPE_ID.getValue(),
                element);
        Long entityId = fromApiJsonHelper.extractLongNamed(GUARANTOR_JSON_INPUT_PARAMS.ENTITY_ID.getValue(), element);
        String firstname = fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.FIRSTNAME.getValue(), element);
        String lastname = fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.LASTNAME.getValue(), element);
        String addressLine1 = fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.ADDRESS_LINE_1.getValue(), element);
        String addressLine2 = fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.ADDRESS_LINE_2.getValue(), element);
        String city = fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.CITY.getValue(), element);
        String state = fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.STATE.getValue(), element);
        String zip = fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.ZIP.getValue(), element);
        String country = fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.COUNTRY.getValue(), element);
        String mobileNumber = fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.MOBILE_NUMBER.getValue(), element);
        String housePhoneNumber = fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.PHONE_NUMBER.getValue(), element);
        String comment = fromApiJsonHelper.extractStringNamed(GUARANTOR_JSON_INPUT_PARAMS.COMMENT.getValue(), element);
        LocalDate dob = fromApiJsonHelper.extractLocalDateNamed(GUARANTOR_JSON_INPUT_PARAMS.DATE_OF_BIRTH.getValue(), element);

        return new GuarantorCommand(loanId, guarantorTypeId, entityId, firstname, lastname, addressLine1, addressLine2, city, state, zip,
                country, mobileNumber, housePhoneNumber, comment, dob);
    }
}