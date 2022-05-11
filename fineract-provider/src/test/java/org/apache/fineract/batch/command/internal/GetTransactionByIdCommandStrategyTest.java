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
package org.apache.fineract.batch.command.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionsApiResource;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GetTransactionByIdCommandStrategyTest {

    /**
     * Test {@link GetTransactionByIdCommandStrategy#execute} happy path scenario.
     */
    @Test
    public void testExecuteSuccessScenario() {
        // given
        final TestContext testContext = new TestContext();

        final Long loanId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final Long transactionId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final BatchRequest request = getBatchRequest(loanId, transactionId);
        final String responseBody = "{\"id\":12,\"officeId\":1,\"officeName\":\"Head Office\",\"type\":{\"id\":10,\"code\":"
                + "\"loanTransactionType.accrual\",\"value\":\"Accrual\",\"disbursement\":false,\"repaymentAtDisbursement\":false,"
                + "\"repayment\":false,\"contra\":false,\"waiveInterest\":false,\"waiveCharges\":false,\"accrual\":true,\"writeOff\":false,"
                + "\"recoveryRepayment\":false,\"initiateTransfer\":false,\"approveTransfer\":false,\"withdrawTransfer\":false,"
                + "\"rejectTransfer\":false,\"chargePayment\":false,\"refund\":false,\"refundForActiveLoans\":false},\"date\":[2022,3,29],"
                + "\"currency\":{\"code\":\"EUR\",\"name\":\"Euro\",\"decimalPlaces\":2,\"inMultiplesOf\":0,\"displaySymbol\":\"€\","
                + "\"nameCode\":\"currency.EUR\",\"displayLabel\":\"Euro (€)\"},\"amount\":0.000000,\"netDisbursalAmount\":200.000000,"
                + "\"principalPortion\":0,\"interestPortion\":0.000000,\"feeChargesPortion\":0,\"penaltyChargesPortion\":0,\"overpaymentPortion\":0,"
                + "\"unrecognizedIncomePortion\":0,\"outstandingLoanBalance\":0,\"submittedOnDate\":[2022,3,29],\"manuallyReversed\":false,"
                + "\"loanChargePaidByList\":[],\"numberOfRepayments\":0}";

        given(testContext.loanTransactionsApiResource.retrieveTransaction(loanId, transactionId, testContext.uriInfo))
                .willReturn(responseBody);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(response.getRequestId()).isEqualTo(request.getRequestId());
        assertThat(response.getHeaders()).isEqualTo(request.getHeaders());
        assertThat(response.getBody()).isEqualTo(responseBody);
    }

    /**
     * Test {@link GetTransactionByIdCommandStrategy#execute} for internal server error.
     */
    @Test
    public void testExecuteForInternalServerError() {
        // given
        final TestContext testContext = new TestContext();
        final Long loanId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final Long transactionId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final BatchRequest request = getBatchRequest(loanId, transactionId);

        given(testContext.loanTransactionsApiResource.retrieveTransaction(loanId, transactionId, testContext.uriInfo))
                .willThrow(new RuntimeException("Some error"));

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertThat(response.getRequestId()).isEqualTo(request.getRequestId());
        assertThat(response.getHeaders()).isEqualTo(request.getHeaders());
        assertThat(response.getBody()).isEqualTo("{\"Exception\": java.lang.RuntimeException: Some error}");
    }

    /**
     * Test {@link GetTransactionByIdCommandStrategy#execute} for loan not found exception.
     */
    @Test
    public void testExecuteForLoanNotFoundException() {
        // given
        final TestContext testContext = new TestContext();
        final Long loanId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final Long transactionId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final BatchRequest request = getBatchRequest(loanId, transactionId);

        given(testContext.loanTransactionsApiResource.retrieveTransaction(loanId, transactionId, testContext.uriInfo))
                .willThrow(new LoanNotFoundException(loanId));

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_NOT_FOUND);
        assertThat(response.getRequestId()).isEqualTo(request.getRequestId());
        assertThat(response.getHeaders()).isEqualTo(request.getHeaders());
        assertThat(response.getBody()).isEqualTo(build404NotFoundError("Loan", loanId));
    }

    /**
     * Test {@link GetTransactionByIdCommandStrategy#execute} for transaction not found exception.
     */
    @Test
    public void testExecuteForTransactionNotFoundException() {
        final TestContext testContext = new TestContext();
        final Long loanId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final Long transactionId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final BatchRequest request = getBatchRequest(loanId, transactionId);

        when(testContext.loanTransactionsApiResource.retrieveTransaction(loanId, transactionId, testContext.uriInfo))
                .thenThrow(new LoanTransactionNotFoundException(transactionId));

        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusCode());
        assertEquals(build404NotFoundError("Transaction", transactionId), response.getBody());
        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(request.getHeaders(), response.getHeaders());
    }

    /**
     * Creates and returns a request with the given loan id and transaction id.
     *
     * @param loanId
     *            the loan id
     * @param transactionId
     *            the transaction id
     * @return BatchRequest
     */
    private BatchRequest getBatchRequest(final Long loanId, final Long transactionId) {

        final BatchRequest br = new BatchRequest();
        String relativeUrl = "loans/" + loanId + "/transactions/" + transactionId;

        br.setRequestId(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.GET);
        br.setReference(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setBody("{}");

        return br;
    }

    /**
     * Builds the 404 not found error.
     *
     * @param field
     *            the field name
     * @param id
     *            the id
     */
    private String build404NotFoundError(final String field, final Long id) {
        return String.format("{\n" + "  \"developerMessage\": \"The requested resource is not available.\",\n"
                + "  \"httpStatusCode\": \"404\",\n" + "  \"defaultUserMessage\": \"The requested resource is not available.\",\n"
                + "  \"userMessageGlobalisationCode\": \"error.msg.resource.not.found\",\n" + "  \"errors\": [\n" + "    {\n"
                + "      \"developerMessage\": \"%s with identifier %s does not exist\",\n"
                + "      \"defaultUserMessage\": \"%s with identifier %s does not exist\",\n"
                + "      \"userMessageGlobalisationCode\": \"error.msg.loan.id.invalid\",\n" + "      \"parameterName\": \"id\",\n"
                + "      \"args\": [\n" + "        {\n" + "          \"value\": %s\n" + "        }\n" + "      ]\n" + "    }\n" + "  ]\n"
                + "}", field, id, field, id, id);
    }

    /**
     * Private test context class used since testng runs in parallel to avoid state between tests
     */
    private static class TestContext {

        @Mock
        private UriInfo uriInfo;

        @Mock
        private LoanTransactionsApiResource loanTransactionsApiResource;

        private final GetTransactionByIdCommandStrategy subjectToTest;

        TestContext() {
            MockitoAnnotations.openMocks(this);
            subjectToTest = new GetTransactionByIdCommandStrategy(loanTransactionsApiResource);
        }
    }
}
