package org.mifosplatform.organisation.staff.serialization;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromCommandJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.staff.command.StaffCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public final class StaffCommandFromCommandJsonDeserializer implements FromCommandJsonDeserializer<StaffCommand> {

    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public StaffCommandFromCommandJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromJsonHelper = fromApiJsonHelper;
    }

    @Override
    public StaffCommand commandFromCommandJson(final String commandAsJson) {
        return commandFromCommandJson(null, commandAsJson);
    }

    @Override
    public StaffCommand commandFromCommandJson(final Long staffId, final String commandAsJson) {
        return commandFromCommandJson(staffId, commandAsJson, false);
    }

    @Override
    public StaffCommand commandFromCommandJson(final Long staffId, final String commandAsJson, final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromJsonHelper.parse(commandAsJson);

        final String firstname = fromJsonHelper.extractStringNamed("firstname", element, parametersPassedInRequest);
        final String lastname = fromJsonHelper.extractStringNamed("lastname", element, parametersPassedInRequest);
        final Boolean isLoanOfficer = fromJsonHelper.extractBooleanNamed("isLoanOfficer", element, parametersPassedInRequest);
        final Long officeId = fromJsonHelper.extractLongNamed("officeId", element, parametersPassedInRequest);

        return new StaffCommand(parametersPassedInRequest, makerCheckerApproval, staffId, officeId, firstname, lastname, isLoanOfficer);
    }
}