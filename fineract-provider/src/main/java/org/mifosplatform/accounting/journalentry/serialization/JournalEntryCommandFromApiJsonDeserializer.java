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
    public JournalEntryCommand commandFromApiJson(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        final Set<String> supportedParameters = JournalEntryJsonInputParams.getAllValues();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long officeId = this.fromApiJsonHelper.extractLongNamed(JournalEntryJsonInputParams.OFFICE_ID.getValue(), element);
        final String currencyCode = this.fromApiJsonHelper
                .extractStringNamed(JournalEntryJsonInputParams.CURRENCY_CODE.getValue(), element);
        final String comments = this.fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.COMMENTS.getValue(), element);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(
                JournalEntryJsonInputParams.TRANSACTION_DATE.getValue(), element);
        final String referenceNumber = this.fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.REFERENCE_NUMBER.getValue(),
                element);
        final Long accountingRuleId = this.fromApiJsonHelper.extractLongNamed(JournalEntryJsonInputParams.ACCOUNTING_RULE.getValue(),
                element);
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed(JournalEntryJsonInputParams.AMOUNT.getValue(), element,
                locale);
        final Long paymentTypeId = this.fromApiJsonHelper.extractLongNamed(JournalEntryJsonInputParams.PAYMENT_TYPE_ID.getValue(), element);
        final String accountNumber = this.fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.ACCOUNT_NUMBER.getValue(),
                element);
        final String checkNumber = this.fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.CHECK_NUMBER.getValue(), element);
        final String receiptNumber = this.fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.RECEIPT_NUMBER.getValue(),
                element);
        final String bankNumber = this.fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.BANK_NUMBER.getValue(), element);
        final String routingCode = this.fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.ROUTING_CODE.getValue(), element);

        SingleDebitOrCreditEntryCommand[] credits = null;
        SingleDebitOrCreditEntryCommand[] debits = null;
        if (element.isJsonObject()) {
            if (topLevelJsonElement.has(JournalEntryJsonInputParams.CREDITS.getValue())
                    && topLevelJsonElement.get(JournalEntryJsonInputParams.CREDITS.getValue()).isJsonArray()) {
                credits = populateCreditsOrDebitsArray(topLevelJsonElement, locale, credits, JournalEntryJsonInputParams.CREDITS.getValue());
            }
            if (topLevelJsonElement.has(JournalEntryJsonInputParams.DEBITS.getValue())
                    && topLevelJsonElement.get(JournalEntryJsonInputParams.DEBITS.getValue()).isJsonArray()) {
                debits = populateCreditsOrDebitsArray(topLevelJsonElement, locale, debits, JournalEntryJsonInputParams.DEBITS.getValue());
            }
        }
        return new JournalEntryCommand(officeId, currencyCode, transactionDate, comments, credits, debits, referenceNumber,
                accountingRuleId, amount, paymentTypeId, accountNumber, checkNumber, receiptNumber, bankNumber, routingCode);
    }

    /**
     * @param comments
     * @param topLevelJsonElement
     * @param locale
     */
    private SingleDebitOrCreditEntryCommand[] populateCreditsOrDebitsArray(final JsonObject topLevelJsonElement, final Locale locale,
            SingleDebitOrCreditEntryCommand[] debitOrCredits, final String paramName) {
        final JsonArray array = topLevelJsonElement.get(paramName).getAsJsonArray();
        debitOrCredits = new SingleDebitOrCreditEntryCommand[array.size()];
        for (int i = 0; i < array.size(); i++) {

            final JsonObject creditElement = array.get(i).getAsJsonObject();
            final Set<String> parametersPassedInForCreditsCommand = new HashSet<>();

            final Long glAccountId = this.fromApiJsonHelper.extractLongNamed("glAccountId", creditElement);
            final String comments = this.fromApiJsonHelper.extractStringNamed("comments", creditElement);
            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed("amount", creditElement, locale);

            debitOrCredits[i] = new SingleDebitOrCreditEntryCommand(parametersPassedInForCreditsCommand, glAccountId, amount, comments);
        }
        return debitOrCredits;
    }
}