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
package org.apache.fineract.interoperation.data;

import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_ACCOUNT_ID;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_AMOUNT;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_DATE_FORMAT;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_EXPIRATION;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_EXTENSION_LIST;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_FSP_COMMISSION;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_FSP_FEE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_LOCALE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_NOTE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_TRANSACTION_CODE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_TRANSACTION_ROLE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_TRANSACTION_TYPE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_TRANSFER_CODE;

import com.google.gson.JsonObject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.interoperation.domain.InteropTransactionRole;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;

public class InteropTransferRequestData extends InteropRequestData {

    static final String[] PARAMS = { PARAM_TRANSACTION_CODE, PARAM_ACCOUNT_ID, PARAM_AMOUNT, PARAM_TRANSACTION_ROLE, PARAM_TRANSACTION_TYPE,
            PARAM_NOTE, PARAM_EXPIRATION, PARAM_EXTENSION_LIST, PARAM_TRANSFER_CODE, PARAM_FSP_FEE, PARAM_FSP_COMMISSION, PARAM_LOCALE,
            PARAM_DATE_FORMAT };

    @NotNull
    private final String transferCode;

    // validation: what was specified in quotes step
    private MoneyData fspFee;

    private MoneyData fspCommission;

    public InteropTransferRequestData(@NotNull String transactionCode, @NotNull String accountId, @NotNull MoneyData amount,
            @NotNull InteropTransactionRole transactionRole, InteropTransactionTypeData transactionType, String note,
            LocalDateTime expiration, List<ExtensionData> extensionList, @NotNull String transferCode, MoneyData fspFee,
            MoneyData fspCommission) {
        super(transactionCode, null, accountId, amount, transactionRole, transactionType, note, null, expiration, extensionList);
        this.transferCode = transferCode;
        this.fspFee = fspFee;
        this.fspCommission = fspCommission;
    }

    public InteropTransferRequestData(@NotNull String transactionCode, @NotNull String transferCode, @NotNull String accountId,
            @NotNull MoneyData amount, @NotNull InteropTransactionRole transactionRole) {
        this(transactionCode, accountId, amount, transactionRole, null, null, null, null, transferCode, null, null);
    }

    private InteropTransferRequestData(InteropRequestData other, @NotNull String transferCode, MoneyData fspFee, MoneyData fspCommission) {
        this(other.getTransactionCode(), other.getAccountId(), other.getAmount(), other.getTransactionRole(), other.getTransactionType(),
                other.getNote(), other.getExpiration(), other.getExtensionList(), transferCode, fspFee, fspCommission);
    }

    public String getTransferCode() {
        return transferCode;
    }

    public MoneyData getFspFee() {
        return fspFee;
    }

    public MoneyData getFspCommission() {
        return fspCommission;
    }

    @Override
    public void normalizeAmounts(@NotNull MonetaryCurrency currency) {
        super.normalizeAmounts(currency);
        if (fspFee != null) {
            fspFee.normalizeAmount(currency);
        }
    }

    public static InteropTransferRequestData validateAndParse(final DataValidatorBuilder dataValidator, JsonObject element,
            FromJsonHelper jsonHelper) {
        if (element == null) {
            return null;
        }

        jsonHelper.checkForUnsupportedParameters(element, Arrays.asList(PARAMS));

        InteropRequestData interopRequestData = InteropRequestData.validateAndParse(dataValidator, element, jsonHelper);

        String transferCode = jsonHelper.extractStringNamed(PARAM_TRANSFER_CODE, element);
        DataValidatorBuilder dataValidatorCopy = dataValidator.reset().parameter(PARAM_TRANSFER_CODE).value(transferCode).notBlank();

        JsonObject fspFeeElement = jsonHelper.extractJsonObjectNamed(PARAM_FSP_FEE, element);
        dataValidator.merge(dataValidatorCopy);
        MoneyData fspFee = MoneyData.validateAndParse(dataValidator, fspFeeElement, jsonHelper);

        JsonObject fspCommissionElement = jsonHelper.extractJsonObjectNamed(PARAM_FSP_COMMISSION, element);
        dataValidator.merge(dataValidatorCopy);
        MoneyData fspCommission = MoneyData.validateAndParse(dataValidator, fspCommissionElement, jsonHelper);

        String transactionRoleString = jsonHelper.extractStringNamed(PARAM_TRANSACTION_ROLE, element);
        dataValidatorCopy = dataValidator.reset().parameter(PARAM_TRANSACTION_ROLE).value(transactionRoleString).notNull();

        dataValidator.merge(dataValidatorCopy);
        return dataValidator.hasError() ? null : new InteropTransferRequestData(interopRequestData, transferCode, fspFee, fspCommission);
    }
}
