/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.serialization;

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.mifosplatform.portfolio.collectionsheet.command.SingleDisbursalCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link CollectionSheetBulkDisbursalCommand}'s.
 */
@Component
public final class CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer extends
        AbstractFromApiJsonDeserializer<CollectionSheetBulkDisbursalCommand> {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public CollectionSheetBulkDisbursalCommand commandFromApiJson(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final JsonObject topLevelJsonElement = element.getAsJsonObject();

        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);

        SingleDisbursalCommand[] loanDisbursementTransactions = null;

        if (element.isJsonObject()) {
            if (topLevelJsonElement.has("bulkDisbursementTransactions")
                    && topLevelJsonElement.get("bulkDisbursementTransactions").isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get("bulkDisbursementTransactions").getAsJsonArray();
                loanDisbursementTransactions = new SingleDisbursalCommand[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject loanTransactionElement = array.get(i).getAsJsonObject();

                    final Long loanId = this.fromApiJsonHelper.extractLongNamed("loanId", loanTransactionElement);
                    final BigDecimal disbursementAmount = this.fromApiJsonHelper.extractBigDecimalNamed("transactionAmount",
                            loanTransactionElement, locale);
                    loanDisbursementTransactions[i] = new SingleDisbursalCommand(loanId, disbursementAmount, transactionDate);
                }
            }
        }
        return new CollectionSheetBulkDisbursalCommand(note, transactionDate, loanDisbursementTransactions);
    }
}