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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.math.BigDecimal;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.services.InterOperationApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.InteropIdentifierAccountResponseData;
import org.apache.fineract.client.models.InteropIdentifierRequestData;
import org.apache.fineract.client.models.InteropQuoteRequestData;
import org.apache.fineract.client.models.InteropQuoteResponseData;
import org.apache.fineract.client.models.InteropTransactionRequestData;
import org.apache.fineract.client.models.InteropTransactionRequestResponseData;
import org.apache.fineract.client.models.InteropTransactionTypeData;
import org.apache.fineract.client.models.InteropTransferRequestData;
import org.apache.fineract.client.models.InteropTransferResponseData;
import org.apache.fineract.client.models.MoneyData;
import org.apache.fineract.interoperation.domain.InteropIdentifierType;
import org.apache.fineract.interoperation.domain.InteropTransactionRole;
import org.apache.fineract.interoperation.domain.InteropTransferActionType;

/**
 * Drives the interoperation API for one savings account, identified by its external id, within one interoperation
 * transaction.
 */
public class FeignInteropHelper {

    private static final String NOTE = "Integration test";

    private final FineractFeignClient fineractClient;
    private final String accountExternalId;
    private final String transactionCode;
    private final String currency;
    private final BigDecimal amount;
    private final BigDecimal fee;

    public FeignInteropHelper(FineractFeignClient fineractClient, String accountExternalId, String transactionCode) {
        this.fineractClient = fineractClient;
        this.accountExternalId = accountExternalId;
        this.transactionCode = transactionCode;
        this.currency = "TZS";
        this.amount = BigDecimal.TEN;
        this.fee = BigDecimal.ONE;
    }

    public String getAccountExternalId() {
        return accountExternalId;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public BigDecimal getTransferAmount() {
        return amount.add(fee);
    }

    public InteropIdentifierAccountResponseData registerParty(InteropIdentifierType idType, String idValue) {
        return ok(() -> interop().registerAccountIdentifier(idType.name(), idValue, partyRequest()));
    }

    public CallFailedRuntimeException registerPartyExpectingError(InteropIdentifierType idType, String idValue) {
        return fail(() -> interop().registerAccountIdentifier(idType.name(), idValue, partyRequest()));
    }

    public InteropIdentifierAccountResponseData getParty(InteropIdentifierType idType, String idValue) {
        return ok(() -> interop().getAccountByIdentifier(idType.name(), idValue));
    }

    public CallFailedRuntimeException getPartyExpectingError(InteropIdentifierType idType, String idValue) {
        return fail(() -> interop().getAccountByIdentifier(idType.name(), idValue));
    }

    public InteropIdentifierAccountResponseData deleteParty(InteropIdentifierType idType, String idValue) {
        return ok(() -> interop().deleteAccountIdentifier(idType.name(), idValue, partyRequest()));
    }

    private InteropIdentifierRequestData partyRequest() {
        return new InteropIdentifierRequestData().accountId(accountExternalId);
    }

    public InteropTransactionRequestResponseData createTransactionRequest(String requestCode, InteropTransactionRole role) {
        return ok(() -> interop().createTransactionRequest(transactionRequest(requestCode, role)));
    }

    /** A PAYEE cannot raise a transaction request, so the server rejects that role. */
    public CallFailedRuntimeException createTransactionRequestExpectingError(String requestCode, InteropTransactionRole role) {
        return fail(() -> interop().createTransactionRequest(transactionRequest(requestCode, role)));
    }

    private InteropTransactionRequestData transactionRequest(String requestCode, InteropTransactionRole role) {
        return new InteropTransactionRequestData()//
                .transactionCode(transactionCode)//
                .requestCode(requestCode)//
                .accountId(accountExternalId)//
                .transactionRole(InteropTransactionRequestData.TransactionRoleEnum.fromValue(role.name()))//
                .amount(money(amount))//
                .transactionType(transactionType(InteropTransactionTypeData.InitiatorEnum.PAYEE));
    }

    public InteropQuoteResponseData createQuote(String quoteCode, InteropTransactionRole role) {
        return ok(() -> interop().createQuote(new InteropQuoteRequestData()//
                .transactionCode(transactionCode)//
                .quoteCode(quoteCode)//
                .accountId(accountExternalId)//
                .transactionRole(InteropQuoteRequestData.TransactionRoleEnum.fromValue(role.name()))//
                .amountType(InteropQuoteRequestData.AmountTypeEnum.RECEIVE)//
                .note(NOTE)//
                .amount(money(amount))//
                .transactionType(transactionType(InteropTransactionTypeData.InitiatorEnum.PAYER))));
    }

    public InteropTransferResponseData prepareTransfer(String transferCode) {
        return performTransfer(transferCode, InteropTransferActionType.PREPARE, InteropTransactionRole.PAYER);
    }

    public InteropTransferResponseData createTransfer(String transferCode, InteropTransactionRole role) {
        return performTransfer(transferCode, InteropTransferActionType.CREATE, role);
    }

    private InteropTransferResponseData performTransfer(String transferCode, InteropTransferActionType action,
            InteropTransactionRole role) {
        return ok(() -> interop().performTransfer(transferRequest(transferCode, role), action.name()));
    }

    public CallFailedRuntimeException performTransferExpectingError(String transferCode, String action, InteropTransactionRole role) {
        return fail(() -> interop().performTransfer(transferRequest(transferCode, role), action));
    }

    public CallFailedRuntimeException performTransferWithoutActionExpectingError(String transferCode, InteropTransactionRole role) {
        return fail(() -> interop().performTransferUniversal(transferRequest(transferCode, role), Map.of()));
    }

    private InteropTransferRequestData transferRequest(String transferCode, InteropTransactionRole role) {
        InteropTransferRequestData request = new InteropTransferRequestData()//
                .transactionCode(transactionCode)//
                .transferCode(transferCode)//
                .accountId(accountExternalId)//
                .transactionRole(InteropTransferRequestData.TransactionRoleEnum.fromValue(role.name()))//
                .note(NOTE)//
                .amount(money(amount))//
                .transactionType(transactionType(InteropTransactionTypeData.InitiatorEnum.PAYER));
        if (role.isWithdraw()) {
            request.fspFee(money(fee));
        }
        return request;
    }

    private MoneyData money(BigDecimal value) {
        return new MoneyData().amount(value).currency(currency);
    }

    private static InteropTransactionTypeData transactionType(InteropTransactionTypeData.InitiatorEnum initiator) {
        return new InteropTransactionTypeData()//
                .scenario(InteropTransactionTypeData.ScenarioEnum.PAYMENT)//
                .initiator(initiator)//
                .initiatorType(InteropTransactionTypeData.InitiatorTypeEnum.CONSUMER);
    }

    private InterOperationApi interop() {
        return fineractClient.interOperation();
    }
}
