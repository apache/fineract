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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.assertEqualBigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.models.BatchRequest;
import org.apache.fineract.client.models.BatchResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBatchHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Working Capital reads a text amount with the request's {@code locale}, as the loan modules do. The requests go
 * through the batch API, whose body is a plain string, so an amount can be sent as text.
 */
public class FeignWorkingCapitalLoanAmountLocaleTest extends FeignIntegrationTest {

    private static final String DATE = "2026-03-02";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private FeignBatchHelper batchHelper;
    private Long productId;

    private final List<Long> createdLoanIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(fineractClient());
        clientHelper = new FeignClientHelper(fineractClient());
        businessDateHelper = new FeignBusinessDateHelper(fineractClient());
        batchHelper = new FeignBatchHelper(fineractClient());
        productId = new WorkingCapitalLoanProductHelper().createWorkingCapitalLoanProduct(
                new WorkingCapitalLoanProductTestBuilder().withName("WCL Locale " + Utils.uniqueRandomStringGenerator("", 8))
                        .withShortName(Utils.uniqueRandomStringGenerator("", 4)).build())
                .getResourceId();
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
    }

    @Test
    @DisplayName("Text amounts with a decimal comma are read with a comma locale through the whole lifecycle")
    void commaLocaleTextIsReadWithTheLocale() {
        businessDateHelper.runAt(DATE, () -> {
            final Long clientId = clientHelper.createClient("02 March 2026");

            final Long loanId = resourceId(call("v1/working-capital-loans", """
                    {"clientId": %d, "productId": %d, "principalAmount": "5000,00", "periodPaymentRate": 18, \
                    "totalPaymentVolume": "100000,50", "submittedOnDate": "%s", "expectedDisbursementDate": "%s", \
                    "locale": "pl", "dateFormat": "yyyy-MM-dd"}""".formatted(clientId, productId, DATE, DATE)));
            createdLoanIds.add(loanId);
            assertEqualBigDecimal(new BigDecimal("100000.50"), loan(loanId).getTotalPaymentVolume(), "Total payment volume");

            call("v1/working-capital-loans/" + loanId + "?command=approve", """
                    {"approvedOnDate": "%s", "approvedLoanAmount": "5000,00", "expectedDisbursementDate": "%s", \
                    "locale": "pl", "dateFormat": "yyyy-MM-dd"}""".formatted(DATE, DATE));
            assertEqualBigDecimal(new BigDecimal("5000"), loan(loanId).getApprovedPrincipal(), "Approved principal");

            call("v1/working-capital-loans/" + loanId + "?command=disburse", """
                    {"actualDisbursementDate": "%s", "transactionAmount": "5000,00", "locale": "pl", "dateFormat": "yyyy-MM-dd"}"""
                    .formatted(DATE));

            final Long repaymentId = resourceId(repayment(loanId, "\"123,45\"", "pl"));
            assertEqualBigDecimal(new BigDecimal("123.45"), wcLoanHelper.getTransaction(loanId, repaymentId).getTransactionAmount(),
                    "Repayment amount");
        });
    }

    @Test
    @DisplayName("A decimal point under a comma locale is refused, as for loans")
    void decimalPointUnderACommaLocaleIsRefused() {
        businessDateHelper.runAt(DATE, () -> {
            final Long loanId = activeLoan();

            final BatchResponse response = repayment(loanId, "\"100.00\"", "pl");
            assertEquals(400, response.getStatusCode(), response.getBody());
            assertTrue(response.getBody().contains("validation.msg.invalid.decimal.format"), response.getBody());
        });
    }

    @Test
    @DisplayName("JSON numbers and English text are read as before")
    void jsonNumbersAndEnglishTextAreUnchanged() {
        businessDateHelper.runAt(DATE, () -> {
            final Long loanId = activeLoan();

            final Long numberId = resourceId(repayment(loanId, "10.5", "pl"));
            assertEqualBigDecimal(new BigDecimal("10.5"), wcLoanHelper.getTransaction(loanId, numberId).getTransactionAmount(),
                    "JSON number under pl");

            final Long englishId = resourceId(repayment(loanId, "\"123.45\"", "en"));
            assertEqualBigDecimal(new BigDecimal("123.45"), wcLoanHelper.getTransaction(loanId, englishId).getTransactionAmount(),
                    "Text under en");
        });
    }

    private Long activeLoan() {
        final Long clientId = clientHelper.createClient("02 March 2026");
        final Long loanId = resourceId(call("v1/working-capital-loans", """
                {"clientId": %d, "productId": %d, "principalAmount": 5000, "periodPaymentRate": 18, "totalPaymentVolume": 100000, \
                "submittedOnDate": "%s", "expectedDisbursementDate": "%s", "locale": "en", "dateFormat": "yyyy-MM-dd"}"""
                .formatted(clientId, productId, DATE, DATE)));
        createdLoanIds.add(loanId);
        call("v1/working-capital-loans/" + loanId + "?command=approve", """
                {"approvedOnDate": "%s", "approvedLoanAmount": 5000, "expectedDisbursementDate": "%s", "locale": "en", \
                "dateFormat": "yyyy-MM-dd"}""".formatted(DATE, DATE));
        call("v1/working-capital-loans/" + loanId + "?command=disburse", """
                {"actualDisbursementDate": "%s", "transactionAmount": 5000, "locale": "en", "dateFormat": "yyyy-MM-dd"}""".formatted(DATE));
        return loanId;
    }

    private BatchResponse repayment(final Long loanId, final String amountJson, final String locale) {
        return send("v1/working-capital-loans/" + loanId + "/transactions?command=repayment", """
                {"transactionDate": "%s", "transactionAmount": %s, "locale": "%s", "dateFormat": "yyyy-MM-dd"}""".formatted(DATE,
                amountJson, locale));
    }

    private BatchResponse call(final String relativeUrl, final String body) {
        final BatchResponse response = send(relativeUrl, body);
        assertEquals(200, response.getStatusCode(), relativeUrl + ": " + response.getBody());
        return response;
    }

    private BatchResponse send(final String relativeUrl, final String body) {
        final BatchRequest request = new BatchRequest().requestId(1L).relativeUrl(relativeUrl).method("POST").body(body);
        return batchHelper.executeWithoutEnclosingTransaction(List.of(request)).get(0);
    }

    private GetWorkingCapitalLoansLoanIdResponse loan(final Long loanId) {
        return wcLoanHelper.getLoanDetails(loanId);
    }

    private static Long resourceId(final BatchResponse response) {
        assertEquals(200, response.getStatusCode(), response.getBody());
        try {
            final JsonNode body = MAPPER.readTree(response.getBody());
            return body.get("resourceId").asLong();
        } catch (final Exception e) {
            throw new AssertionError("Unreadable response: " + response.getBody(), e);
        }
    }
}
