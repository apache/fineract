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
package org.apache.fineract.integrationtests.organization.teller;

import static org.hamcrest.Matchers.equalTo;

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.Map;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Covers the behaviour introduced for FINERACT-2724: a non-numeric {@code txnAmount} on the "allocate cash to cashier"
 * endpoint must be rejected with a field-specific "not a valid number" validation error (via
 * {@code HttpMessageNotReadableErrorController}) rather than the generic invalid-JSON error, and without requiring
 * {@code txnAmount} to be widened from {@code BigDecimal} to {@code String} on the API contract.
 */
public class AllocateCashToCashierValidationTest {

    private RequestSpecification requestSpecification;
    private ResponseSpecification responseSpecification;
    private Long tellerId;
    private Long cashierId;

    @BeforeAll
    public static void ensureCashierFinancialActivityAccountsExist() {
        Utils.initializeRESTAssured();

        final RequestSpecification requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        final ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        final AccountHelper accountHelper = new AccountHelper(requestSpec, responseSpec);
        final FinancialActivityAccountHelper financialActivityAccountHelper = new FinancialActivityAccountHelper(requestSpec);

        // Allocating cash to a cashier posts journal entries between these two financial-activity accounts; the
        // teller endpoint 404s if either mapping is missing, so tests must not rely on it being seeded already.
        ensureFinancialActivityAccountMapping(financialActivityAccountHelper, accountHelper,
                FinancialActivity.CASH_AT_MAINVAULT.getValue());
        ensureFinancialActivityAccountMapping(financialActivityAccountHelper, accountHelper, FinancialActivity.CASH_AT_TELLER.getValue());
    }

    private static void ensureFinancialActivityAccountMapping(final FinancialActivityAccountHelper financialActivityAccountHelper,
            final AccountHelper accountHelper, final Integer financialActivityId) {
        final boolean alreadyMapped = financialActivityAccountHelper.getAllFinancialActivityAccounts().stream()
                .anyMatch(mapping -> financialActivityId.equals(mapping.getFinancialActivityData().getId()));
        if (alreadyMapped) {
            return;
        }

        final Account assetAccount = accountHelper.createAssetAccount();
        financialActivityAccountHelper.createFinancialActivityAccount(new PostFinancialActivityAccountsRequest()
                .financialActivityId(financialActivityId.longValue()).glAccountId(assetAccount.getAccountID().longValue()));
    }

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();

        requestSpecification = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpecification.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpecification = new ResponseSpecBuilder().expectStatusCode(200).build();

        final Integer staffId = StaffHelper.createStaff(requestSpecification, responseSpecification);
        tellerId = Long.valueOf(CashierTransactionsHelper.createTeller(requestSpecification, responseSpecification));
        cashierId = Long.valueOf(
                CashierTransactionsHelper.createCashier(requestSpecification, responseSpecification, tellerId, staffId.longValue()));
    }

    @Test
    public void allocateCashWithNonNumericAmountReturnsFieldSpecificValidationError() {
        final ResponseSpecification invalidAmountResponseSpec = new ResponseSpecBuilder().expectStatusCode(400)
                .expectBody("userMessageGlobalisationCode", equalTo("validation.msg.invalid.decimal.format"))
                .expectBody("parameterName", equalTo("txnAmount")).expectBody("value", equalTo("not-a-number")).build();

        final Map<String, Object> requestMap = CashierTransactionsHelper.allocateCashToCashierRequestMap("not-a-number");
        final String json = new Gson().toJson(requestMap);

        CashierTransactionsHelper.allocateCashToCashierRaw(requestSpecification, invalidAmountResponseSpec, tellerId, cashierId, json);
    }

    @Test
    public void allocateCashWithValidNumericAmountIsNotRejectedAsInvalidNumber() {
        final Map<String, Object> requestMap = CashierTransactionsHelper.allocateCashToCashierRequestMap(100);
        final String json = new Gson().toJson(requestMap);

        CashierTransactionsHelper.allocateCashToCashierRaw(requestSpecification, responseSpecification, tellerId, cashierId, json);
    }

    @Test
    public void allocateCashWithMalformedJsonStillReturnsGenericInvalidJsonError() {
        final ResponseSpecification malformedJsonResponseSpec = new ResponseSpecBuilder().expectStatusCode(400)
                .expectBody("userMessageGlobalisationCode", equalTo("error.msg.invalid.json.data")).build();

        final String malformedJson = "{\"currencyCode\":\"USD\",\"txnAmount\":100,\"txnDate\":\"01 January 2023\"";

        CashierTransactionsHelper.allocateCashToCashierRaw(requestSpecification, malformedJsonResponseSpec, tellerId, cashierId,
                malformedJson);
    }

}
