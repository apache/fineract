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
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanaccount.command.LoanStateTransitionCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link LoanStateTransitionCommand}'s.
 */
@Component
public final class LoanStateTransitionCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<LoanStateTransitionCommand> {

    /**
     * The parameters supported for this command.
     */
    final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("eventDate", "note", "locale", "dateFormat", "status",
            "approvedOnDate", "rejectedOnDate", "withdrawnOnDate", "disbursedOnDate", "expectedMaturityDate"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public LoanStateTransitionCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public LoanStateTransitionCommand commandFromApiJson(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = fromApiJsonHelper.parse(json);
        final LocalDate eventDate = fromApiJsonHelper.extractLocalDateNamed("eventDate", element);
        final String note = fromApiJsonHelper.extractStringNamed("note", element);

        final LocalDate approvedOnDate = fromApiJsonHelper.extractLocalDateNamed("approvedOnDate", element);
        final LocalDate rejectedOnDate = fromApiJsonHelper.extractLocalDateNamed("rejectedOnDate", element);
        final LocalDate withdrawnOnDate = fromApiJsonHelper.extractLocalDateNamed("withdrawnOnDate", element);
        final LocalDate disbursedOnDate = fromApiJsonHelper.extractLocalDateNamed("disbursedOnDate", element);

        return new LoanStateTransitionCommand(eventDate, note, approvedOnDate, rejectedOnDate, withdrawnOnDate, disbursedOnDate);
    }
}