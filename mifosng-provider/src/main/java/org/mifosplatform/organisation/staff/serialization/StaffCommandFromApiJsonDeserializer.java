package org.mifosplatform.organisation.staff.serialization;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.staff.command.StaffCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link StaffCommand}'s.
 */
@Component
public final class StaffCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<StaffCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("firstname", "lastname", "officeId", "isLoanOfficer"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public StaffCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public StaffCommand commandFromApiJson(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = fromApiJsonHelper.parse(json);

        final String firstname = fromApiJsonHelper.extractStringNamed("firstname", element);
        final String lastname = fromApiJsonHelper.extractStringNamed("lastname", element);
        final Boolean isLoanOfficer = fromApiJsonHelper.extractBooleanNamed("isLoanOfficer", element);
        final Long officeId = fromApiJsonHelper.extractLongNamed("officeId", element);

        return new StaffCommand(officeId, firstname, lastname, isLoanOfficer);
    }
}