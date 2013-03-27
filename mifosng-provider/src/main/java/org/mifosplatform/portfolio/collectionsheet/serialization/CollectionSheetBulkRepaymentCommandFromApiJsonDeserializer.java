/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.mifosplatform.portfolio.collectionsheet.command.SingleRepaymentCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link CollectionSheetBulkRepaymentCommand}'s.
 */
@Component
public final class CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<CollectionSheetBulkRepaymentCommand> {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer(FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public CollectionSheetBulkRepaymentCommand commandFromApiJson(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("transactionDate", "actualDisbursementDate", "bulkRepaymentTransactions", "bulkDisbursementTransactions", "note",
                "locale", "dateFormat"));
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate transactionDate = fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);

        final String note = fromApiJsonHelper.extractStringNamed("note", element);
        
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

        SingleRepaymentCommand[] loanRepaymentTransactions = null;

        if (element.isJsonObject()) {
            if (topLevelJsonElement.has("bulkRepaymentTransactions")
                    && topLevelJsonElement.get("bulkRepaymentTransactions").isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get("bulkRepaymentTransactions").getAsJsonArray();
                loanRepaymentTransactions = new SingleRepaymentCommand[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject loanTransactionElement = array.get(i).getAsJsonObject();
                    
                    final Long loanId = this.fromApiJsonHelper.extractLongNamed("loanId", loanTransactionElement);
                    final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalNamed("transactionAmount", loanTransactionElement, locale);
                    loanRepaymentTransactions[i] = new SingleRepaymentCommand(loanId, transactionAmount, transactionDate);
                }
            }
        }
        return new CollectionSheetBulkRepaymentCommand(note, transactionDate, loanRepaymentTransactions);
    }
    
    public void validateBulkRepaymentTransaction(final CollectionSheetBulkRepaymentCommand bulkRepaymentCommand) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.bulk.repayment.transaction");
        
        baseDataValidator.reset().parameter("transactionDate").value(bulkRepaymentCommand.getTransactionDate()).notNull();
        
        if (StringUtils.isNotBlank(bulkRepaymentCommand.getNote())) {
            baseDataValidator.reset().parameter("note").value(bulkRepaymentCommand.getNote()).notExceedingLengthOf(1000);
        }

        final SingleRepaymentCommand[] loanRepayments = bulkRepaymentCommand.getLoanTransactions();
        if (loanRepayments != null) {
            for (int i = 0; i < loanRepayments.length; i++) {
                SingleRepaymentCommand singleLoanRepaymentCommand = loanRepayments[i];
                baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].loan.id")
                        .value(singleLoanRepaymentCommand.getLoanId()).notNull().integerGreaterThanZero();
                baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].transaction.amount")
                        .value(singleLoanRepaymentCommand.getTransactionAmount()).notNull().zeroOrPositiveAmount();
            }
        }        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
        
    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}