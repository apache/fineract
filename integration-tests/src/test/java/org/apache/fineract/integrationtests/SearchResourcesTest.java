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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.AccountRequest;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetSearchResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostProductsTypeRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountTransferHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSearchHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignShareAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.modules.AccountTransferRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SearchResourcesTest extends FeignIntegrationTest {

    private static final String SAVINGS_ACCOUNT_TYPE = "2";
    private static final String CLIENT_ACTIVATION_DATE = "04 March 2011";
    private static final String SAVINGS_SUBMITTED_DATE = "08 January 2013";
    private static final String SAVINGS_APPROVED_DATE = "09 January 2013";
    private static final String SAVINGS_ACTIVATED_DATE = "01 March 2013";
    private static final String ACCOUNT_TRANSFER_DATE = "01 March 2013";

    private FeignSearchHelper searchHelper;
    private FeignClientHelper clientHelper;
    private FeignSavingsHelper savingsHelper;
    private FeignSavingsProductHelper savingsProductHelper;
    private FeignSavingsTransactionHelper savingsTransactionHelper;
    private FeignLoanHelper loanHelper;
    private FeignTransactionHelper transactionHelper;
    private FeignShareAccountHelper shareAccountHelper;
    private FeignAccountTransferHelper accountTransferHelper;

    @BeforeAll
    public void setup() {
        this.searchHelper = new FeignSearchHelper(fineractClient());
        this.clientHelper = new FeignClientHelper(fineractClient());
        this.savingsHelper = new FeignSavingsHelper(fineractClient());
        this.savingsProductHelper = new FeignSavingsProductHelper(fineractClient());
        this.savingsTransactionHelper = new FeignSavingsTransactionHelper(fineractClient());
        this.loanHelper = new FeignLoanHelper(fineractClient());
        this.transactionHelper = new FeignTransactionHelper(fineractClient());
        this.shareAccountHelper = new FeignShareAccountHelper(fineractClient());
        this.accountTransferHelper = new FeignAccountTransferHelper(fineractClient());
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

        final PostClientsResponse clientResponse = addClientAsPerson();
        final Long clientId = clientResponse.getClientId();
        final GetClientsClientIdResponse getClientResponse = clientHelper.getClient(clientId);
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

        final PostClientsResponse clientResponse = addClientAsPerson();
        final Long clientId = clientResponse.getClientId();

        final Long savingsId = openSavingsAccount(clientId, "1000");
        final String query = savingsHelper.getSavingsDetails(savingsId).getAccountNo();

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

        final PostClientsResponse clientsResponse = addClientAsPerson();
        final Long clientId = clientsResponse.getClientId();

        final Long productId = shareAccountHelper.createShareProduct(new PostProductsTypeRequest()
                .name(Utils.uniqueRandomStringGenerator("SHARE_PRODUCT_", 6)).shortName(Utils.uniqueRandomStringGenerator("", 4))
                .description(Utils.randomStringGenerator("", 20)).currencyCode("USD").locale("en_GB").digitsAfterDecimal(4).inMultiplesOf(0)
                .totalShares(10000).sharesIssued(10000).unitPrice(2).minimumShares(10).nominalShares(20).maximumShares(3000)
                .allowDividendCalculationForInactiveClients(true).accountingRule(1).minimumActivePeriodForDividends(1)
                .minimumactiveperiodFrequencyType(0).lockinPeriodFrequency(1).lockinPeriodFrequencyType(0));

        final Long savingsId = openSavingsAccount(clientId, "1000");

        final Long shareAccountId = shareAccountHelper.applyShareAccount(
                new AccountRequest().clientId(clientId).productId(productId).savingsAccountId(savingsId).submittedDate("01 January 2026")
                        .applicationDate("01 January 2026").requestedShares(10L).dateFormat("dd MMMM yyyy").locale("en"));

        shareAccountHelper.approve(shareAccountId);

        shareAccountHelper.activate(shareAccountId, "01 January 2026", "dd MMMM yyyy", "en");

        final String query = shareAccountHelper.getShareAccount(shareAccountId).getAccountNo();

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
        final Long clientId = addClientAsPerson().getClientId();
        final Long loanProductId = createLoanProduct();
        final Long loanId = createLoanAccount(clientId, loanProductId);

        final String disbursementExternalId = "disbursement-" + UUID.randomUUID();
        loanHelper.disburseLoanWithExternalId("01 January 2026", loanId, "1000", disbursementExternalId);

        final String repaymentExternalId = "repayment-" + UUID.randomUUID();
        final String checkNumber = "loan-check-" + UUID.randomUUID();
        final String routingCode = "loan-routing-" + UUID.randomUUID();
        final String receiptNumber = "loan-receipt-" + UUID.randomUUID();
        final PostLoansLoanIdTransactionsResponse repayment = transactionHelper.makeLoanRepayment(loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate("01 February 2026").dateFormat("dd MMMM yyyy").locale("en")
                        .transactionAmount(100.0).paymentTypeId(1L).checkNumber(checkNumber).routingCode(routingCode)
                        .receiptNumber(receiptNumber).externalId(repaymentExternalId));
        final Long repaymentTransactionId = repayment.getResourceId();

        assertEquals(0, searchHelper.search(disbursementExternalId, resources, Boolean.TRUE).size());

        GetSearchResponse result = assertSingleSearchResult(
                searchHelper.search(String.valueOf(repaymentTransactionId), resources, Boolean.TRUE));
        assertLoanTransactionSearchResult(result, clientId, loanId, repaymentTransactionId, repaymentExternalId);

        result = assertSingleSearchResult(searchHelper.search(repaymentExternalId, resources, Boolean.TRUE));
        assertLoanTransactionSearchResult(result, clientId, loanId, repaymentTransactionId, repaymentExternalId);

        result = assertSingleSearchResult(searchHelper.search(checkNumber, resources, Boolean.TRUE));
        assertLoanTransactionSearchResult(result, clientId, loanId, repaymentTransactionId, repaymentExternalId);

        result = assertSingleSearchResult(searchHelper.search(routingCode, resources, Boolean.TRUE));
        assertLoanTransactionSearchResult(result, clientId, loanId, repaymentTransactionId, repaymentExternalId);

        result = assertSingleSearchResult(searchHelper.search(receiptNumber, resources, Boolean.TRUE));
        assertLoanTransactionSearchResult(result, clientId, loanId, repaymentTransactionId, repaymentExternalId);

        final String partialExternalId = repaymentExternalId.substring(0, repaymentExternalId.length() - 4);
        result = assertSingleSearchResult(searchHelper.search(partialExternalId, resources, Boolean.FALSE));
        assertLoanTransactionSearchResult(result, clientId, loanId, repaymentTransactionId, repaymentExternalId);
        assertEquals(0, searchHelper.search(partialExternalId, resources, Boolean.TRUE).size());

        result = assertSingleSearchResult(
                searchHelper.search(checkNumber.substring(0, checkNumber.length() - 4), resources, Boolean.FALSE));
        assertLoanTransactionSearchResult(result, clientId, loanId, repaymentTransactionId, repaymentExternalId);

        assertEquals(0, searchHelper.search("unknown-loan-payment-detail-" + UUID.randomUUID(), resources, Boolean.TRUE).size());
    }

    @Test
    public void searchOverSavingsDepositTransactionResources() {
        final String resources = "savingsTransactions";
        final Long clientId = addClientAsPerson().getClientId();
        final Long savingsId = openSavingsAccount(clientId, "1000");
        final String externalId = "savings-deposit-" + UUID.randomUUID();
        final String checkNumber = "sd-check-" + UUID.randomUUID();
        final String routingCode = "sd-route-" + UUID.randomUUID();
        final String receiptNumber = "sd-receipt-" + UUID.randomUUID();
        final Long transactionId = createSavingsTransaction(savingsId, "deposit", "02 March 2013", "100", externalId, checkNumber,
                routingCode, receiptNumber);

        GetSearchResponse result = assertSingleSearchResult(searchHelper.search(String.valueOf(transactionId), resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId, transactionId, "deposit", externalId);

        result = assertSingleSearchResult(searchHelper.search(externalId, resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId, transactionId, "deposit", externalId);

        result = assertSingleSearchResult(searchHelper.search(checkNumber, resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId, transactionId, "deposit", externalId);

        result = assertSingleSearchResult(searchHelper.search(routingCode, resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId, transactionId, "deposit", externalId);

        result = assertSingleSearchResult(searchHelper.search(receiptNumber, resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId, transactionId, "deposit", externalId);

        final String refNo = result.getTransactionRefNo();
        assertNotNull(refNo);
        result = assertSingleSearchResult(searchHelper.search(refNo, resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId, transactionId, "deposit", externalId);

        result = assertSingleSearchResult(
                searchHelper.search(routingCode.substring(0, routingCode.length() - 4), resources, Boolean.FALSE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId, transactionId, "deposit", externalId);

        assertEquals(0, searchHelper.search("unknown-savings-deposit-payment-detail-" + UUID.randomUUID(), resources, Boolean.TRUE).size());
    }

    @Test
    public void searchOverSavingsWithdrawalTransactionResources() {
        final String resources = "savingsTransactions";
        final Long clientId = addClientAsPerson().getClientId();
        final Long savingsId = openSavingsAccount(clientId, "1000");
        savingsTransactionHelper.deposit(savingsId,
                new PostSavingsAccountTransactionsRequest().transactionDate("02 March 2013").dateFormat("dd MMMM yyyy").locale("en")
                        .transactionAmount(BigDecimal.valueOf(200)).paymentTypeId(1).externalId("savings-deposit-" + UUID.randomUUID()));

        final String externalId = "savings-withdrawal-" + UUID.randomUUID();
        final String checkNumber = "sw-check-" + UUID.randomUUID();
        final String routingCode = "sw-route-" + UUID.randomUUID();
        final String receiptNumber = "sw-receipt-" + UUID.randomUUID();
        final Long transactionId = createSavingsTransaction(savingsId, "withdrawal", "03 March 2013", "50", externalId, checkNumber,
                routingCode, receiptNumber);

        GetSearchResponse result = assertSingleSearchResult(searchHelper.search(String.valueOf(transactionId), resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId, transactionId, "withdrawal", externalId);

        result = assertSingleSearchResult(searchHelper.search(externalId, resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId, transactionId, "withdrawal", externalId);

        result = assertSingleSearchResult(searchHelper.search(checkNumber, resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId, transactionId, "withdrawal", externalId);

        result = assertSingleSearchResult(searchHelper.search(routingCode, resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId, transactionId, "withdrawal", externalId);

        result = assertSingleSearchResult(searchHelper.search(receiptNumber, resources, Boolean.TRUE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId, transactionId, "withdrawal", externalId);

        final String refNo = result.getTransactionRefNo();
        assertNotNull(refNo);
        result = assertSingleSearchResult(searchHelper.search(refNo.substring(0, refNo.length() - 4), resources, Boolean.FALSE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId, transactionId, "withdrawal", externalId);

        result = assertSingleSearchResult(
                searchHelper.search(receiptNumber.substring(0, receiptNumber.length() - 4), resources, Boolean.FALSE));
        assertSavingsTransactionSearchResult(result, clientId, savingsId, transactionId, "withdrawal", externalId);

        assertEquals(0,
                searchHelper.search("unknown-savings-withdrawal-payment-detail-" + UUID.randomUUID(), resources, Boolean.TRUE).size());
    }

    @Test
    public void searchOverSavingsTransferTransactionResources() {
        final String resources = "savingsTransactions";
        final Long fromClientId = addClientAsPerson().getClientId();
        final Long toClientId = addClientAsPerson().getClientId();
        final Long fromSavingsId = openSavingsAccount(fromClientId, "1000");
        final Long toSavingsId = openSavingsAccount(toClientId, "1000");
        savingsTransactionHelper.deposit(fromSavingsId,
                new PostSavingsAccountTransactionsRequest().transactionDate("02 March 2013").dateFormat("dd MMMM yyyy").locale("en")
                        .transactionAmount(BigDecimal.valueOf(200)).paymentTypeId(1).externalId("transfer-seed-" + UUID.randomUUID()));

        final String checkNumber = "tr-check-" + UUID.randomUUID();
        final String routingCode = "tr-route-" + UUID.randomUUID();
        final String receiptNumber = "tr-receipt-" + UUID.randomUUID();
        accountTransferHelper
                .createAccountTransfer(AccountTransferRequestBuilders.withPaymentDetails(
                        AccountTransferRequestBuilders.transfer(ACCOUNT_TRANSFER_DATE, fromClientId, fromSavingsId, SAVINGS_ACCOUNT_TYPE,
                                toClientId, toSavingsId, SAVINGS_ACCOUNT_TYPE, "50"),
                        1L, null, checkNumber, routingCode, receiptNumber, null));

        List<GetSearchResponse> results = searchHelper.search(checkNumber, resources, Boolean.TRUE);
        assertSavingsTransferTransactionSearchResults(results, fromClientId, fromSavingsId, toClientId, toSavingsId);

        results = searchHelper.search(routingCode, resources, Boolean.TRUE);
        assertSavingsTransferTransactionSearchResults(results, fromClientId, fromSavingsId, toClientId, toSavingsId);

        results = searchHelper.search(receiptNumber, resources, Boolean.TRUE);
        assertSavingsTransferTransactionSearchResults(results, fromClientId, fromSavingsId, toClientId, toSavingsId);

        results = searchHelper.search(checkNumber.substring(0, checkNumber.length() - 4), resources, Boolean.FALSE);
        assertSavingsTransferTransactionSearchResults(results, fromClientId, fromSavingsId, toClientId, toSavingsId);

        assertEquals(0, searchHelper.search("unknown-transfer-payment-detail-" + UUID.randomUUID(), resources, Boolean.TRUE).size());
    }

    private PostClientsResponse addClientAsPerson() {
        return clientHelper.createClient(ClientRequestBuilders.createActivePersonClient(CLIENT_ACTIVATION_DATE));
    }

    private Long openSavingsAccount(final Long clientId, final String minimumOpeningBalance) {
        final Long savingsProductId = savingsProductHelper.createSavingsProduct(SavingsRequestBuilders
                .savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY, SavingsTestData.InterestPostingPeriodType.MONTHLY,
                        SavingsTestData.InterestCalculationType.DAILY_BALANCE)
                .minRequiredOpeningBalance(new BigDecimal(minimumOpeningBalance))).getResourceId();
        assertNotNull(savingsProductId);
        final Long savingsId = savingsHelper.submitApplication(clientId, savingsProductId, SAVINGS_SUBMITTED_DATE).getSavingsId();
        assertEquals(Boolean.TRUE, savingsHelper.getSavingsStatus(savingsId).getSubmittedAndPendingApproval());
        savingsHelper.approveSavings(savingsId, SAVINGS_APPROVED_DATE);
        assertEquals(Boolean.TRUE, savingsHelper.getSavingsStatus(savingsId).getApproved());
        savingsHelper.activateSavings(savingsId, SAVINGS_ACTIVATED_DATE);
        assertEquals(Boolean.TRUE, savingsHelper.getSavingsStatus(savingsId).getActive());
        return savingsId;
    }

    private Long createLoanProduct() {
        return loanHelper.createLoanProduct(
                new LoanProductTestBuilder().withPrincipal("10000000.00").withNumberOfRepayments("24").withRepaymentAfterEvery("1")
                        .withRepaymentTypeAsMonth().withinterestRatePerPeriod("2").withInterestRateFrequencyTypeAsMonths()
                        .withRepaymentStrategy(LoanApplicationTestBuilder.DEFAULT_STRATEGY).withAmortizationTypeAsEqualPrincipalPayment()
                        .withInterestTypeAsDecliningBalance().currencyDetails("2", null).buildRequest())
                .getResourceId();
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId) {
        final Long loanId = loanHelper.applyForLoan(
                new PostLoansRequest().clientId(clientId).productId(loanProductId).loanType("individual").principal(new BigDecimal("1000"))
                        .loanTermFrequency(2).loanTermFrequencyType(2).numberOfRepayments(2).repaymentEvery(1).repaymentFrequencyType(2)
                        .interestRatePerPeriod(BigDecimal.ZERO).amortizationType(0).interestType(1).interestCalculationPeriodType(1)
                        .transactionProcessingStrategyCode(LoanApplicationTestBuilder.DEFAULT_STRATEGY)
                        .expectedDisbursementDate("01 January 2026").submittedOnDate("01 January 2026")
                        .maxOutstandingLoanBalance(new BigDecimal("36000")).dateFormat("dd MMMM yyyy").locale("en_GB"))
                .getLoanId();
        loanHelper.approveLoan("01 January 2026", loanId);
        return loanId;
    }

    private Long createSavingsTransaction(final Long savingsId, final String command, final String transactionDate,
            final String transactionAmount, final String externalId, final String checkNumber, final String routingCode,
            final String receiptNumber) {
        final PostSavingsAccountTransactionsRequest request = new PostSavingsAccountTransactionsRequest().dateFormat("dd MMMM yyyy")
                .locale("en").transactionDate(transactionDate).transactionAmount(new BigDecimal(transactionAmount)).paymentTypeId(1)
                .checkNumber(checkNumber).routingCode(routingCode).receiptNumber(receiptNumber).externalId(externalId);
        return ("deposit".equals(command) ? savingsTransactionHelper.deposit(savingsId, request)
                : savingsTransactionHelper.withdraw(savingsId, request)).getResourceId();
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

    private void assertSavingsTransferTransactionSearchResults(final List<GetSearchResponse> results, final Long fromClientId,
            final Long fromSavingsId, final Long toClientId, final Long toSavingsId) {
        assertNotNull(results);
        assertEquals(2, results.size());
        assertSavingsTransferTransactionSearchResult(results, fromClientId, fromSavingsId, "withdrawal");
        assertSavingsTransferTransactionSearchResult(results, toClientId, toSavingsId, "deposit");
    }

    private void assertSavingsTransferTransactionSearchResult(final List<GetSearchResponse> results, final Long clientId,
            final Long savingsId, final String transactionType) {
        final GetSearchResponse result = results.stream().filter(
                searchResult -> savingsId.equals(searchResult.getAccountId()) && transactionType.equals(searchResult.getTransactionType()))
                .findFirst().orElseThrow();
        assertEquals("SAVINGS_TRANSACTION", result.getEntityType());
        assertEquals(savingsId, result.getEntityId());
        assertEquals(clientId, result.getParentId());
        assertEquals("client", result.getParentType());
        assertNotNull(result.getTransactionId());
        assertEquals(savingsId, result.getAccountId());
        assertEquals("savings", result.getAccountType());
    }
}
