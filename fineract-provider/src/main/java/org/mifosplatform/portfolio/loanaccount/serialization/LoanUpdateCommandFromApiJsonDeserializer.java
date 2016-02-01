/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.serialization;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanaccount.command.LoanUpdateCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class LoanUpdateCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<LoanUpdateCommand> {

    /**
     * The parameters supported for this command.
     */
    final Set<String> supportedParameters = new HashSet<>(Arrays.asList("unassignedDate", "locale", "dateFormat"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public LoanUpdateCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public LoanUpdateCommand commandFromApiJson(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate unassignedDate = this.fromApiJsonHelper.extractLocalDateNamed("unassignedDate", element);

        return new LoanUpdateCommand(unassignedDate);
    }
}