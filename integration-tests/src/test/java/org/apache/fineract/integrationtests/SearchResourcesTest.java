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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetSearchResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsResponse;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSearchHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.shares.ShareAccountHelper;
import org.apache.fineract.integrationtests.common.shares.ShareAccountTransactionHelper;
import org.apache.fineract.integrationtests.common.shares.ShareProductHelper;
import org.apache.fineract.integrationtests.common.shares.ShareProductTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

public class SearchResourcesTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private FeignSearchHelper searchHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.searchHelper = new FeignSearchHelper(FineractFeignClientHelper.getFineractFeignClient());
    }

    @Test
    public void searchAnyValueOverAllResources() {
        final String resources = "clients,clientIdentifiers,groups,savings,shares,loans";

        final String query = Utils.randomStringGenerator("C", 12);
        final List<GetSearchResponse> searchResponse = searchHelper.search(query, resources, Boolean.TRUE);
        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.size());
    }

    @Test
    public void searchAnyValueOverClientResources() {
        final String resources = "clients";

        final String query = Utils.randomStringGenerator("C", 12);
        final List<GetSearchResponse> searchResponse = searchHelper.search(query, resources, Boolean.TRUE);
        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.size());
    }

    @Test
    public void searchOverClientResources() {
        final String resources = "clients";

        final PostClientsResponse clientResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, null);
        final Long clientId = clientResponse.getClientId();
        final GetClientsClientIdResponse getClientResponse = ClientHelper.getClient(requestSpec, responseSpec, clientId.intValue());
        final String query = getClientResponse.getAccountNo();

        final List<GetSearchResponse> searchResponse = searchHelper.search(query, resources, Boolean.FALSE);
        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.size());
        assertEquals(getClientResponse.getDisplayName(), searchResponse.get(0).getEntityName(), "Client name comparation");
    }

    @Test
    public void searchAnyValueOverLoanResources() {
        final String resources = "loans";

        final String query = Utils.randomStringGenerator("L", 12);
        final List<GetSearchResponse> searchResponse = searchHelper.search(query, resources, Boolean.TRUE);
        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.size());
    }

    @Test
    public void searchOverSavingsResources() {
        final String resources = "savings";

        final PostClientsResponse clientResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, null);
        final Long clientId = clientResponse.getClientId();

        final Integer savingsId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId.intValue(), "1000");
        final SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(requestSpec, responseSpec);
        final String query = (String) savingsAccountHelper.getSavingsAccountDetail(savingsId, "accountNo");

        final List<GetSearchResponse> searchResponse = searchHelper.search(query, resources, Boolean.FALSE);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.size());

        final GetSearchResponse result = searchResponse.getFirst();

        assertEquals("SAVING", result.getEntityType());
        assertNotNull(result.getEntityStatus());
        assertNotNull(result.getEntityStatus().getId());
        assertNotNull(result.getEntityStatus().getCode());
        assertNotNull(result.getEntityStatus().getValue());
    }

    @Test
    public void searchOverSharesResources() {
        final String resources = "shares";

        final PostClientsResponse clientsResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, null);
        final Long clientId = clientsResponse.getClientId();

        final ShareProductHelper shareProductHelper = new ShareProductHelper();
        final Integer productId = ShareProductTransactionHelper.createShareProduct(shareProductHelper.build(), requestSpec, responseSpec);

        final Integer savingsId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId.intValue(), "1000");

        final String shareJson = new ShareAccountHelper().withClientId(String.valueOf(clientId)).withProductId(String.valueOf(productId))
                .withSavingsAccountId(String.valueOf(savingsId)).withSubmittedDate("01 January 2026").withApplicationDate("01 January 2026")
                .withRequestedShares("10").build();

        final Integer shareAccountId = ShareAccountTransactionHelper.createShareAccount(shareJson, requestSpec, responseSpec);

        final String approveJson = "{}";
        ShareAccountTransactionHelper.postCommand("approve", shareAccountId, approveJson, requestSpec, responseSpec);

        final String activateJson = """
                {
                  "activatedDate": "01 January 2026",
                  "dateFormat": "dd MMMM yyyy",
                  "locale": "en"
                }
                """;
        ShareAccountTransactionHelper.postCommand("activate", shareAccountId, activateJson, requestSpec, responseSpec);

        final Map<String, Object> shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec,
                responseSpec);
        final String query = (String) shareAccountData.get("accountNo");

        final List<GetSearchResponse> searchResponse = searchHelper.search(query, resources, Boolean.FALSE);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.size());

        final GetSearchResponse result = searchResponse.getFirst();

        assertEquals("SHARE", result.getEntityType());
        assertNotNull(result.getEntityStatus());
        assertNotNull(result.getEntityStatus().getId());
        assertNotNull(result.getEntityStatus().getCode());
        assertNotNull(result.getEntityStatus().getValue());
    }

    @Test
    public void searchOverLoanTransactionResources() {
        final String resources = "loanTransactions";
        final Long clientId = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID, ClientHelper.LEGALFORM_ID_PERSON, null)
                .getClientId();
        final LoanTransactionHelper loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        final Integer loanProductId = loanTransactionHelper.createLoanProduct(null, "2", LoanApplicationTestBuilder.DEFAULT_STRATEGY, "1");
        final Integer loanId = createLoanAccount(clientId, loanProductId, loanTransactionHelper);

        final String disbursementExternalId = "disbursement-" + UUID.randomUUID();
        loanTransactionHelper.disburseLoan("01 January 2026", loanId, "1000", disbursementExternalId);

        final String repaymentExternalId = "repayment-" + UUID.randomUUID();
        final PostLoansLoanIdTransactionsResponse repayment = loanTransactionHelper.makeLoanRepayment(loanId.longValue(),
                new PostLoansLoanIdTransactionsRequest().transactionDate("01 February 2026").dateFormat("dd MMMM yyyy").locale("en")
                        .transactionAmount(100.0).externalId(repaymentExternalId));
        final Long repaymentTransactionId = repayment.getResourceId();

        assertEquals(0, searchHelper.search(disbursementExternalId, resources, Boolean.TRUE).size());

        GetSearchResponse result = assertSingleSearchResult(
                searchHelper.search(String.valueOf(repaymentTransactionId), resources, Boolean.TRUE));
        assertLoanTransactionSearchResult(result, clientId, loanId.longValue(), repaymentTransactionId, repaymentExternalId);

        result = assertSingleSearchResult(searchHelper.search(repaymentExternalId, resources, Boolean.TRUE));
        assertLoanTransactionSearchResult(result, clientId, loanId.longValue(), repaymentTransactionId, repaymentExternalId);

        final String partialExternalId = repaymentExternalId.substring(0, repaymentExternalId.length() - 4);
        result = assertSingleSearchResult(searchHelper.search(partialExternalId, resources, Boolean.FALSE));
        assertLoanTransactionSearchResult(result, clientId, loanId.longValue(), repaymentTransactionId, repaymentExternalId);
        assertEquals(0, searchHelper.search(partialExternalId, resources, Boolean.TRUE).size());
    }

    @Test
    public void searchOverSavingsDepositTransactionResources() {
        final String resources = "savingsTransactions";
        final Long clientId = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID, ClientHelper.LEGALFORM_ID_PERSON, null)
                .getClientId();
        final Integer savingsId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId.intValue(), "1000");
        final SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(requestSpec, responseSpec);
        final String externalId = "savings-deposit-" + UUID.randomUUID();
        final PostSavingsAccountTransactionsResponse deposit = executeSavingsTransaction(
                savingsAccountHelper.depositIntoSavingsAccount(savingsId.longValue(),
                        new PostSavingsAccountTransactionsRequest().transactionDate("02 March 2013").dateFormat("dd MMMM yyyy").locale("en")
                                .transactionAmount(BigDecimal.valueOf(100)).paymentTypeId(1).externalId(externalId)));
        final Long transactionId = deposit.getResourceId();

        GetSearchResponse result = assertSingleSearchResult(searchHelper.search(String.valueOf(transactionId), resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId.longValue(), transactionId, "deposit", externalId);

        result = assertSingleSearchResult(searchHelper.search(externalId, resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId.longValue(), transactionId, "deposit", externalId);

        final String refNo = result.getTransactionRefNo();
        assertNotNull(refNo);
        result = assertSingleSearchResult(searchHelper.search(refNo, resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId.longValue(), transactionId, "deposit", externalId);
    }

    @Test
    public void searchOverSavingsWithdrawalTransactionResources() {
        final String resources = "savingsTransactions";
        final Long clientId = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID, ClientHelper.LEGALFORM_ID_PERSON, null)
                .getClientId();
        final Integer savingsId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId.intValue(), "1000");
        final SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(requestSpec, responseSpec);
        executeSavingsTransaction(savingsAccountHelper.depositIntoSavingsAccount(savingsId.longValue(),
                new PostSavingsAccountTransactionsRequest().transactionDate("02 March 2013").dateFormat("dd MMMM yyyy").locale("en")
                        .transactionAmount(BigDecimal.valueOf(200)).paymentTypeId(1).externalId("savings-deposit-" + UUID.randomUUID())));

        final String externalId = "savings-withdrawal-" + UUID.randomUUID();
        final PostSavingsAccountTransactionsResponse withdrawal = executeSavingsTransaction(
                savingsAccountHelper.withdrawalFromSavingsAccount(savingsId.longValue(),
                        new PostSavingsAccountTransactionsRequest().transactionDate("03 March 2013").dateFormat("dd MMMM yyyy").locale("en")
                                .transactionAmount(BigDecimal.valueOf(50)).paymentTypeId(1).externalId(externalId)));
        final Long transactionId = withdrawal.getResourceId();

        GetSearchResponse result = assertSingleSearchResult(searchHelper.search(String.valueOf(transactionId), resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId.longValue(), transactionId, "withdrawal", externalId);

        result = assertSingleSearchResult(searchHelper.search(externalId, resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId.longValue(), transactionId, "withdrawal", externalId);

        final String refNo = result.getTransactionRefNo();
        assertNotNull(refNo);
        result = assertSingleSearchResult(searchHelper.search(refNo.substring(0, refNo.length() - 4), resources, Boolean.FALSE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId.longValue(), transactionId, "withdrawal", externalId);
    }

    private Integer createLoanAccount(final Long clientId, final Integer loanProductId, final LoanTransactionHelper loanTransactionHelper) {
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("2")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("2").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("01 January 2026")
                .withSubmittedOnDate("01 January 2026").withRepaymentStrategy(LoanApplicationTestBuilder.DEFAULT_STRATEGY)
                .build(clientId.toString(), loanProductId.toString(), null);
        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("01 January 2026", loanId);
        return loanId;
    }

    private PostSavingsAccountTransactionsResponse executeSavingsTransaction(
            final Response<PostSavingsAccountTransactionsResponse> response) {
        assertEquals(200, response.code());
        assertNotNull(response.body());
        return response.body();
    }

    private GetSearchResponse assertSingleSearchResult(final List<GetSearchResponse> searchResponse) {
        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.size());
        return searchResponse.getFirst();
    }

    private void assertLoanTransactionSearchResult(final GetSearchResponse result, final Long clientId, final Long loanId,
            final Long transactionId, final String transactionExternalId) {
        assertEquals("LOAN_TRANSACTION", result.getEntityType());
        assertEquals(loanId, result.getEntityId());
        assertEquals(clientId, result.getParentId());
        assertEquals("client", result.getParentType());
        assertEquals(transactionId, result.getTransactionId());
        assertEquals("repayment", result.getTransactionType());
        assertEquals(transactionExternalId, result.getTransactionExternalId());
        assertEquals(loanId, result.getAccountId());
        assertEquals("loan", result.getAccountType());
    }

    private void assertSavingsTransactionSearchResult(final GetSearchResponse result, final Long clientId, final Long savingsId,
            final Long transactionId, final String transactionType, final String transactionExternalId) {
        assertEquals("SAVINGS_TRANSACTION", result.getEntityType());
        assertEquals(savingsId, result.getEntityId());
        assertEquals(clientId, result.getParentId());
        assertEquals("client", result.getParentType());
        assertEquals(transactionId, result.getTransactionId());
        assertEquals(transactionType, result.getTransactionType());
        assertEquals(transactionExternalId, result.getTransactionExternalId());
        assertNotNull(result.getTransactionRefNo());
        assertEquals(savingsId, result.getAccountId());
        assertEquals("savings", result.getAccountType());
    }
}
