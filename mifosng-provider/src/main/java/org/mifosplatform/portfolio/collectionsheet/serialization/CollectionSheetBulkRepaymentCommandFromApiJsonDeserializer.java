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
import org.mifosplatform.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.mifosplatform.portfolio.collectionsheet.command.SingleRepaymentCommand;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetailAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link CollectionSheetBulkRepaymentCommand}'s.
 */
@Component
public final class CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer extends
        AbstractFromApiJsonDeserializer<CollectionSheetBulkRepaymentCommand> {

    private final FromJsonHelper fromApiJsonHelper;
    private final PaymentDetailAssembler paymentDetailAssembler;

    @Autowired
    public CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
            final PaymentDetailAssembler paymentDetailAssembler) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.paymentDetailAssembler = paymentDetailAssembler;
    }

    @Override
    public CollectionSheetBulkRepaymentCommand commandFromApiJson(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final PaymentDetail paymentDetail = this.paymentDetailAssembler.fetchPaymentDetail(element.getAsJsonObject());

        return commandFromApiJson(json, paymentDetail);
    }

    public CollectionSheetBulkRepaymentCommand commandFromApiJson(final String json, final PaymentDetail paymentDetail) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);

        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

        SingleRepaymentCommand[] loanRepaymentTransactions = null;

        if (element.isJsonObject()) {
            if (topLevelJsonElement.has("bulkRepaymentTransactions") && topLevelJsonElement.get("bulkRepaymentTransactions").isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get("bulkRepaymentTransactions").getAsJsonArray();
                loanRepaymentTransactions = new SingleRepaymentCommand[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject loanTransactionElement = array.get(i).getAsJsonObject();

                    final Long loanId = this.fromApiJsonHelper.extractLongNamed("loanId", loanTransactionElement);
                    final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalNamed("transactionAmount",
                            loanTransactionElement, locale);
                    PaymentDetail detail = paymentDetail;
                    if (paymentDetail == null) {
                        detail = this.paymentDetailAssembler.fetchPaymentDetail(loanTransactionElement);
                    }

                    loanRepaymentTransactions[i] = new SingleRepaymentCommand(loanId, transactionAmount, transactionDate, detail);
                }
            }
        }
        return new CollectionSheetBulkRepaymentCommand(note, transactionDate, loanRepaymentTransactions);
    }

}