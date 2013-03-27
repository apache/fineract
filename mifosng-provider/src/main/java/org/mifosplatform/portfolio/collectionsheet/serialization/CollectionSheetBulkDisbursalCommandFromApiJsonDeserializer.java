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
import org.mifosplatform.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.mifosplatform.portfolio.collectionsheet.command.SingleDisbursalCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link CollectionSheetBulkDisbursalCommand}'s.
 */
@Component
public final class CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<CollectionSheetBulkDisbursalCommand> {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer(FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public CollectionSheetBulkDisbursalCommand commandFromApiJson(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        //TODO: AA need to refactor transactionDate and actualDisbursementDate
        //actualDisbursementDate used in Loan.disburse()
        final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("transactionDate", "actualDisbursementDate", "bulkRepaymentTransactions", "bulkDisbursementTransactions", "note",
                "locale", "dateFormat"));
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate transactionDate = fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);

        final String note = fromApiJsonHelper.extractStringNamed("note", element);
        
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

        SingleDisbursalCommand[] loanDisbursementTransactions = null;

        if (element.isJsonObject()) {
            if (topLevelJsonElement.has("bulkDisbursementTransactions")
                    && topLevelJsonElement.get("bulkDisbursementTransactions").isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get("bulkDisbursementTransactions").getAsJsonArray();
                loanDisbursementTransactions = new SingleDisbursalCommand[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject loanTransactionElement = array.get(i).getAsJsonObject();
                    
                    final Long loanId = this.fromApiJsonHelper.extractLongNamed("loanId", loanTransactionElement);
                    final BigDecimal disbursementAmount = this.fromApiJsonHelper.extractBigDecimalNamed("transactionAmount", loanTransactionElement, locale);
                    loanDisbursementTransactions[i] = new SingleDisbursalCommand(loanId, disbursementAmount, transactionDate);
                }
            }
        }
        return new CollectionSheetBulkDisbursalCommand(note, transactionDate, loanDisbursementTransactions);
    }
    
    public void validateBulkDisbursalTransaction(final CollectionSheetBulkDisbursalCommand bulkDisburseCommand) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.bulk.disbursement.transaction");

        baseDataValidator.reset().parameter("transactionDate").value(bulkDisburseCommand.getTransactionDate()).notNull();
        
        if (StringUtils.isNotBlank(bulkDisburseCommand.getNote())) {
            baseDataValidator.reset().parameter("note").value(bulkDisburseCommand.getNote()).notExceedingLengthOf(1000);
        }
        final SingleDisbursalCommand[] loanDisbursements = bulkDisburseCommand.getDisburseTransactions();
        if (loanDisbursements != null) {
            for (int i = 0; i < loanDisbursements.length; i++) {
                SingleDisbursalCommand singleLoanDisburseCommand = loanDisbursements[i];

                baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].loan.id").value(singleLoanDisburseCommand.getLoanId())
                        .notNull().integerGreaterThanZero();
                baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].disbursement.amount")
                        .value(singleLoanDisburseCommand.getTransactionAmount()).notNull().zeroOrPositiveAmount();
            }
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}