/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.accounting.journalentry.api.JournalEntryJsonInputParams;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;

@Component
public class JournalEntryDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    public static final Set<String> RUNNING_BALANCE_UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(JournalEntryJsonInputParams.OFFICE_ID.getValue()));

    @Autowired
    public JournalEntryDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForUpdateRunningbalance(final JsonCommand command) {
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, command.json(), RUNNING_BALANCE_UPDATE_REQUEST_DATA_PARAMETERS);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GLJournalEntry");

        if (this.fromApiJsonHelper.parameterExists(JournalEntryJsonInputParams.OFFICE_ID.getValue(), command.parsedJson())) {
            final String officeId = this.fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.OFFICE_ID.getValue(),
                    command.parsedJson());
            baseDataValidator.reset().parameter(JournalEntryJsonInputParams.OFFICE_ID.getValue()).value(officeId).ignoreIfNull()
                    .longGreaterThanZero();
        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}
