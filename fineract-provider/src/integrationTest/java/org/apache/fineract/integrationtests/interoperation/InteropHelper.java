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
package org.apache.fineract.integrationtests.interoperation;

import com.google.gson.Gson;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.interoperation.domain.InteropAmountType;
import org.apache.fineract.interoperation.domain.InteropIdentifierType;
import org.apache.fineract.interoperation.domain.InteropInitiatorType;
import org.apache.fineract.interoperation.domain.InteropTransactionRole;
import org.apache.fineract.interoperation.domain.InteropTransactionScenario;
import org.apache.fineract.interoperation.domain.InteropTransferActionType;
import org.apache.fineract.interoperation.util.InteropUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes" })
public class InteropHelper {

    private final static Logger log = LoggerFactory.getLogger(InteropHelper.class);

    private static final String BASE_URL = "/fineract-provider/api/v1/interoperation";
    private static final String HEALTH_URL = BASE_URL + "/health";
    private static final String PARTIES_URL = BASE_URL + "/parties";
    private static final String TRANSACTIONS_URL = BASE_URL + "/transactions";
    private static final String REQUESTS_URL_PARAM = "requests";
    private static final String REQUESTS_URL = BASE_URL + '/' + REQUESTS_URL_PARAM;
    private static final String QUOTES_URL_PARAM = "quotes";
    private static final String QUOTES_URL = BASE_URL + '/' + QUOTES_URL_PARAM;
    private static final String TRANSFERS_URL_PARAM = "transfers";
    private static final String TRANSFERS_URL = BASE_URL + '/' + TRANSFERS_URL_PARAM;
    private static final String PARAM_TRANSFER_ACTION = "action";
    static final String PARAM_ACTION_STATE = "state";
    static final String PARAM_ACCOUNT_BALANCE = "accountBalance";

    private static final String NOTE = "Integration test";

    private final RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private final String tenantId;
    private final String accountExternalId;
    private final String transactionCode;
    private final String currency;
    private final BigDecimal amount;
    private final BigDecimal fee;

    public InteropHelper(RequestSpecification requestSpec, ResponseSpecification responseSpec, String tenantId,
                         String accountExternalId, String transactionCode, String currency, BigDecimal amount, BigDecimal fee) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
        this.tenantId = tenantId;
        this.accountExternalId = accountExternalId;
        this.transactionCode = transactionCode;
        this.currency = currency;
        this.amount = amount;
        this.fee = fee;
    }

    public InteropHelper(RequestSpecification requestSpec, ResponseSpecification responseSpec, String accountExternalId, String transactionCode) {
        this(requestSpec, responseSpec, Utils.DEFAULT_TENANT, accountExternalId, transactionCode, "TZS", BigDecimal.TEN, BigDecimal.ONE);
    }

    public InteropHelper(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        this(requestSpec, responseSpec, UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    public RequestSpecification getRequestSpec() {
        return requestSpec;
    }

    public ResponseSpecification getResponseSpec() {
        return responseSpec;
    }

    void setResponseSpec(ResponseSpecification responseSpec) {
        this.responseSpec = responseSpec;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getAccountExternalId() {
        return accountExternalId;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getFee() {
        return fee;
    }

    BigDecimal getTransferAmount() {
        return fee == null ? amount : amount.add(fee);
    }

    private String buildUrl(String url) {
        return url + '?' + Utils.TENANT_PARAM_NAME + '=' + tenantId;
    }

    /**
     * @return response json
     */
    public String getHealth() {
        String url = buildUrl(HEALTH_URL);
        log.debug("Calling Interoperable GET Health: {}", url);

        String response = Utils.performServerGet(requestSpec, responseSpec, url, null);
        log.debug("Response Interoperable GET Health: {}", response);
        return response;
    }

    /**
     * @return response 'accountId' attribute
     */
    public String getParty(InteropIdentifierType idType, String idValue) {
        String url = buildUrl(PARTIES_URL + '/' + idType + '/' + idValue);
        log.debug("Calling Interoperable GET Party: {}", url);

        String response = Utils.performServerGet(requestSpec, responseSpec, url, null);
        log.debug("Response Interoperable GET Party: {}", response);
        return getJsonAttribute(response, InteropUtil.PARAM_ACCOUNT_ID);
    }

    /**
     * @return response 'accountId' attribute
     */
    public String postParty(InteropIdentifierType idType, String idValue) {
        String url = buildUrl(PARTIES_URL + '/' + idType + '/' + idValue);
        String request = buildPartiesJson();
        log.debug("Calling Interoperable POST Party: {}, body: {}", url, request);

        String response = Utils.performServerPost(requestSpec, responseSpec, url, request, null);
        log.debug("Response Interoperable POST Party: {}", response);
        return getJsonAttribute(response, InteropUtil.PARAM_ACCOUNT_ID);
    }

    /**
     * @return response 'accountId' attribute
     */
    public String deleteParty(InteropIdentifierType idType, String idValue) {
        String url = buildUrl(PARTIES_URL + '/' + idType + '/' + idValue);
        String request = buildPartiesJson();
        log.debug("Calling Interoperable DELETE Party: {}, body: {}", url, request);

        String response = Utils.performServerDelete(requestSpec, responseSpec, url, request, null);
        log.debug("Response Interoperable DELETE Party: {}", response);
        return getJsonAttribute(response, InteropUtil.PARAM_ACCOUNT_ID);
    }

    private String buildPartiesJson() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(InteropUtil.PARAM_ACCOUNT_ID, accountExternalId);
        return new Gson().toJson(map);
    }

    /**
     * @return response 'requestCode' attribute
     */
    public String getTransactionRequest(String requestCode) {
        String url = buildUrl(TRANSACTIONS_URL + '/' + transactionCode + '/' + REQUESTS_URL_PARAM + '/' + requestCode);
        log.debug("Calling Interoperable GET Request: {}", url);

        String response = Utils.performServerGet(requestSpec, responseSpec, url, null);
        log.debug("Response Interoperable GET Request: {}", response);
        return getJsonAttribute(response, InteropUtil.PARAM_REQUEST_CODE);
    }

    /**
     * @param role PAYEE role is not valid for transaction request
     * @return response json
     */
    public String postTransactionRequest(String requestCode, InteropTransactionRole role) {
        String url = buildUrl(REQUESTS_URL);
        String request = buildTransactionRequestJson(requestCode, role);
        log.debug("Calling Interoperable POST Request: {}, body: {}", url, request);

        String response = Utils.performServerPost(requestSpec, responseSpec, url, request, null);
        log.debug("Response Interoperable POST Request: {}", response);
        return response;
    }

    private String buildTransactionRequestJson(String requestCode, InteropTransactionRole role) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(InteropUtil.PARAM_TRANSACTION_CODE, transactionCode);
        map.put(InteropUtil.PARAM_REQUEST_CODE, requestCode);
        map.put(InteropUtil.PARAM_ACCOUNT_ID, accountExternalId);
        map.put(InteropUtil.PARAM_TRANSACTION_ROLE, role);

        HashMap<String, Object> amountMap = new HashMap<>();
        amountMap.put(InteropUtil.PARAM_AMOUNT, amount.toString());
        amountMap.put(InteropUtil.PARAM_CURRENCY, currency);
        map.put(InteropUtil.PARAM_AMOUNT, amountMap);

        HashMap<String, Object> typeMap = new HashMap<>();
        typeMap.put(InteropUtil.PARAM_SCENARIO, InteropTransactionScenario.PAYMENT);
        typeMap.put(InteropUtil.PARAM_INITIATOR, InteropTransactionRole.PAYEE);
        typeMap.put(InteropUtil.PARAM_INITIATOR_TYPE, InteropInitiatorType.CONSUMER);
        map.put(InteropUtil.PARAM_TRANSACTION_TYPE, typeMap);

        return new Gson().toJson(map);
    }

    /**
     * @return response 'quoteCode' attribute
     */
    public String getQuote(String quoteCode) {
        String url = buildUrl(TRANSACTIONS_URL + '/' + transactionCode + '/' + QUOTES_URL_PARAM + '/' + quoteCode);
        log.debug("Calling Interoperable GET Quote: {}", url);

        String response = Utils.performServerGet(requestSpec, responseSpec, url, null);
        log.debug("Response Interoperable GET Quote: {}", response);
        return getJsonAttribute(response, InteropUtil.PARAM_QUOTE_CODE);
    }

    /**
     * @return response json
     */
    public String postQuote(String quoteCode, InteropTransactionRole role) {
        String url = buildUrl(QUOTES_URL);
        String request = buildQuoteJson(quoteCode, role);
        log.debug("Calling Interoperable POST Quote: {}, body: {}", url, request);

        String response = Utils.performServerPost(requestSpec, responseSpec, url, request, null);
        log.debug("Response Interoperable POST Quote: {}", response);
        return response;
    }

    private String buildQuoteJson(String quoteCode, InteropTransactionRole role) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(InteropUtil.PARAM_TRANSACTION_CODE, transactionCode);
        map.put(InteropUtil.PARAM_QUOTE_CODE, quoteCode);
        map.put(InteropUtil.PARAM_ACCOUNT_ID, accountExternalId);
        map.put(InteropUtil.PARAM_TRANSACTION_ROLE, role);
        map.put(InteropUtil.PARAM_AMOUNT_TYPE, InteropAmountType.RECEIVE);
        map.put(InteropUtil.PARAM_NOTE, NOTE);

        HashMap<String, Object> amountMap = new HashMap<>();
        amountMap.put(InteropUtil.PARAM_AMOUNT, amount.toString());
        amountMap.put(InteropUtil.PARAM_CURRENCY, currency);
        map.put(InteropUtil.PARAM_AMOUNT, amountMap);

        HashMap<String, Object> typeMap = new HashMap<>();
        typeMap.put(InteropUtil.PARAM_SCENARIO, InteropTransactionScenario.PAYMENT);
        typeMap.put(InteropUtil.PARAM_INITIATOR, InteropTransactionRole.PAYER);
        typeMap.put(InteropUtil.PARAM_INITIATOR_TYPE, InteropInitiatorType.CONSUMER);
        map.put(InteropUtil.PARAM_TRANSACTION_TYPE, typeMap);

        return new Gson().toJson(map);
    }

    /**
     * @return response 'transferCode' attribute
     */
    public String getTransfer(String transferCode) {
        String url = buildUrl(TRANSACTIONS_URL + '/' + transactionCode + '/' + TRANSFERS_URL_PARAM + '/' + transferCode);
        log.debug("Calling Interoperable GET Transfer: {}", url);

        String response = Utils.performServerGet(requestSpec, responseSpec, url, null);
        log.debug("Response Interoperable GET Transfer: {}", response);
        return getJsonAttribute(response, InteropUtil.PARAM_TRANSFER_CODE);
    }

    /**
     * @return response json
     */
    public String prepareTransfer(String transferCode) {
        return postTransfer(transferCode, InteropTransferActionType.PREPARE, InteropTransactionRole.PAYER);
    }

    /**
     * @return response json
     */
    public String createTransfer(String transferCode, InteropTransactionRole role) {
        return postTransfer(transferCode, InteropTransferActionType.CREATE, role);
    }

    /**
     * @return response json
     */
    public String postTransfer(String transferCode, InteropTransferActionType action, InteropTransactionRole role) {
        String url = buildUrl(TRANSFERS_URL) + '&' + PARAM_TRANSFER_ACTION + '=' + action;
        String request = buildTransferJson(transferCode, role);
        log.debug("Calling Interoperable POST Transfer: {}, body: {}", url, request);

        String response = Utils.performServerPost(requestSpec, responseSpec, url, request, null);
        log.debug("Response Interoperable POST Transfer: {}", response);
        return response;
    }

    private String buildTransferJson(String transferCode, InteropTransactionRole role) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(InteropUtil.PARAM_TRANSACTION_CODE, transactionCode);
        map.put(InteropUtil.PARAM_TRANSFER_CODE, transferCode);
        map.put(InteropUtil.PARAM_ACCOUNT_ID, accountExternalId);
        map.put(InteropUtil.PARAM_TRANSACTION_ROLE, role);
        map.put(InteropUtil.PARAM_NOTE, NOTE);

        HashMap<String, Object> amountMap = new HashMap<>();
        amountMap.put(InteropUtil.PARAM_AMOUNT, amount.toString());
        amountMap.put(InteropUtil.PARAM_CURRENCY, currency);
        map.put(InteropUtil.PARAM_AMOUNT, amountMap);

        if (role.isWithdraw()) {
            HashMap<String, Object> feeMap = new HashMap<>();
            feeMap.put(InteropUtil.PARAM_AMOUNT, fee.toString());
            feeMap.put(InteropUtil.PARAM_CURRENCY, currency);
            map.put(InteropUtil.PARAM_FSP_FEE, feeMap);
        }

        HashMap<String, Object> typeMap = new HashMap<>();
        typeMap.put(InteropUtil.PARAM_SCENARIO, InteropTransactionScenario.PAYMENT);
        typeMap.put(InteropUtil.PARAM_INITIATOR, InteropTransactionRole.PAYER);
        typeMap.put(InteropUtil.PARAM_INITIATOR_TYPE, InteropInitiatorType.CONSUMER);
        map.put(InteropUtil.PARAM_TRANSACTION_TYPE, typeMap);

        return new Gson().toJson(map);
    }

    public static String getJsonAttribute(String json, String attrName) {
        return JsonPath.from(json).get(attrName);
    }
}