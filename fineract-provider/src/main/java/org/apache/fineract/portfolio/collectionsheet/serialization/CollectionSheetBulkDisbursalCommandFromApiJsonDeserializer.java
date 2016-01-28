/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.collectionsheet.serialization;

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.apache.fineract.portfolio.collectionsheet.command.SingleDisbursalCommand;
import org.joda.time.LocalDate;
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