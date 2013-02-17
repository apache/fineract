/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.journalentry.api.JournalEntryJsonInputParams;
import org.mifosplatform.accounting.journalentry.command.JournalEntryCommand;
import org.mifosplatform.accounting.journalentry.command.SingleDebitOrCreditEntryCommand;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link JournalEntryCommand}'s.
 */
@Component
public final class JournalEntryCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<JournalEntryCommand> {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public JournalEntryCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonfromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonfromApiJsonHelper;
    }

    @Override
    public JournalEntryCommand commandFromApiJson(String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        Set<String> supportedParameters = JournalEntryJsonInputParams.getAllValues();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = fromApiJsonHelper.parse(json);

        final Long officeId = fromApiJsonHelper.extractLongNamed(JournalEntryJsonInputParams.OFFICE_ID.getValue(), element);
        final String comments = fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.COMMENTS.getValue(), element);
        final LocalDate entryDate = fromApiJsonHelper.extractLocalDateNamed(JournalEntryJsonInputParams.ENTRY_DATE.getValue(), element);

        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        Locale locale = fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

        SingleDebitOrCreditEntryCommand[] credits = null;
        SingleDebitOrCreditEntryCommand[] debits = null;
        if (element.isJsonObject()) {
            if (topLevelJsonElement.has(JournalEntryJsonInputParams.CREDITS.getValue())
                    && topLevelJsonElement.get(JournalEntryJsonInputParams.CREDITS.getValue()).isJsonArray()) {
                credits = populateCreditsOrDebitsArray(topLevelJsonElement, locale, credits,
                        JournalEntryJsonInputParams.CREDITS.getValue());
            }
            if (topLevelJsonElement.has(JournalEntryJsonInputParams.DEBITS.getValue())
                    && topLevelJsonElement.get(JournalEntryJsonInputParams.DEBITS.getValue()).isJsonArray()) {
                debits = populateCreditsOrDebitsArray(topLevelJsonElement, locale, debits,
                        JournalEntryJsonInputParams.DEBITS.getValue());
            }
        }
        return new JournalEntryCommand(officeId, entryDate, comments, credits, debits);
    }

    /**
     * @param comments
     * @param topLevelJsonElement
     * @param locale
     */
    private SingleDebitOrCreditEntryCommand[] populateCreditsOrDebitsArray(final JsonObject topLevelJsonElement, final Locale locale,
            SingleDebitOrCreditEntryCommand[] debitOrCredits, String paramName) {
        final JsonArray array = topLevelJsonElement.get(paramName).getAsJsonArray();
        debitOrCredits = new SingleDebitOrCreditEntryCommand[array.size()];
        for (int i = 0; i < array.size(); i++) {

            final JsonObject creditElement = array.get(i).getAsJsonObject();
            final Set<String> parametersPassedInForCreditsCommand = new HashSet<String>();

            final Long glAccountId = fromApiJsonHelper.extractLongNamed("glAccountId", creditElement);
            final String comments = fromApiJsonHelper.extractStringNamed("comments", creditElement);
            final BigDecimal amount = fromApiJsonHelper.extractBigDecimalNamed("amount", creditElement, locale);

            debitOrCredits[i] = new SingleDebitOrCreditEntryCommand(parametersPassedInForCreditsCommand, glAccountId, amount, comments);
        }
        return debitOrCredits;
    }
}