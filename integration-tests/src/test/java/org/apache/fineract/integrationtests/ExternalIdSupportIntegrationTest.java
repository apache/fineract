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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.DeleteLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdChargesTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PutChargeTransactionChangesRequest;
import org.apache.fineract.client.models.PutChargeTransactionChangesResponse;
import org.apache.fineract.client.models.PutLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ExternalIdSupportIntegrationTest extends IntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    private ClientHelper clientHelper;
    private AccountHelper accountHelper;
    private LoanTransactionHelper loanTransactionHelper;

    private DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    @BeforeAll
    public void init() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void test() {
        GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(requestSpec, responseSpec, 50, true);
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account assetFeeAndPenaltyAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

        Integer penalty2 = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true, 1));

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccountingRulePeriodicAccrual(new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount })
                .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0")
                .withFeeAndPenaltyAssetAccount(assetFeeAndPenaltyAccount).build(null);
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJSON);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        String loanExternalIdStr = UUID.randomUUID().toString();
        final HashMap loan = applyForLoanApplication(client.getClientId().intValue(), loanProductID, loanExternalIdStr);
        Integer loanId = (Integer) loan.get("resourceId");

        this.loanTransactionHelper.approveLoan("02 September 2022", loanId);
        String txnExternalIdStr = UUID.randomUUID().toString();
        final HashMap disbursedLoanResult = this.loanTransactionHelper.disburseLoan("03 September 2022", loanId, "1000", txnExternalIdStr);

        // Check whether the provided external id was retrieved
        assertEquals(txnExternalIdStr, disbursedLoanResult.get("resourceExternalId"));

        LocalDate targetDate = LocalDate.of(2022, 9, 7);
        final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

        String penalty1LoanChargeExternalId = UUID.randomUUID().toString();
        Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10",
                        penalty1LoanChargeExternalId));

        // Get loan charges
        List<GetLoansLoanIdChargesChargeIdResponse> loanChargesResult = loanTransactionHelper.getLoanCharges((long) loanId);
        assertEquals(penalty1LoanChargeExternalId, loanChargesResult.get(0).getExternalId());
        loanChargesResult = loanTransactionHelper.getLoanCharges(loanExternalIdStr);
        assertEquals(penalty1LoanChargeExternalId, loanChargesResult.get(0).getExternalId());

        // Get loan charge template
        GetLoansLoanIdChargesTemplateResponse loanChargeTemplateResult = loanTransactionHelper.getLoanChargeTemplate((long) loanId);
        assertNotNull(loanChargeTemplateResult);
        loanChargeTemplateResult = loanTransactionHelper.getLoanChargeTemplate(loanExternalIdStr);
        assertNotNull(loanChargeTemplateResult);

        // Get loan charge
        GetLoansLoanIdChargesChargeIdResponse loanChargeResult = loanTransactionHelper.getLoanCharge((long) loanId,
                (long) penalty1LoanChargeId);
        assertEquals(penalty1LoanChargeExternalId, loanChargeResult.getExternalId());
        loanChargeResult = loanTransactionHelper.getLoanCharge(loanExternalIdStr, (long) penalty1LoanChargeId);
        assertEquals(penalty1LoanChargeExternalId, loanChargeResult.getExternalId());
        loanChargeResult = loanTransactionHelper.getLoanCharge((long) loanId, penalty1LoanChargeExternalId);
        assertEquals(penalty1LoanChargeExternalId, loanChargeResult.getExternalId());
        loanChargeResult = loanTransactionHelper.getLoanCharge(loanExternalIdStr, penalty1LoanChargeExternalId);
        assertEquals(penalty1LoanChargeExternalId, loanChargeResult.getExternalId());

        PostLoansLoanIdChargesResponse penalty2Result = this.loanTransactionHelper.addLoanCharge(loanExternalIdStr,
                new PostLoansLoanIdChargesRequest().chargeId((long) penalty).amount(10.0).dueDate(penaltyCharge1AddedDate)
                        .dateFormat("dd MMMM yyyy").locale("en"));
        assertNotNull(penalty2Result.getResourceExternalId());

        // Check whether we can fetch transaction templates with proper result http code (HTTP 200..300)
        loanTransactionHelper.retrieveTransactionTemplate(loanExternalIdStr, "repayment", null, null, null);
        loanTransactionHelper.retrieveTransactionTemplate(loanExternalIdStr, "payoutRefund", null, null, null);
        loanTransactionHelper.retrieveTransactionTemplate(loanExternalIdStr, "waiveinterest", null, null, null);
        loanTransactionHelper.retrieveTransactionTemplate(loanExternalIdStr, "close-rescheduled", null, null, null);
        loanTransactionHelper.retrieveTransactionTemplate(loanExternalIdStr, "disburse", null, null, null);
        loanTransactionHelper.retrieveTransactionTemplate(loanExternalIdStr, "recoverypayment", null, null, null);
        loanTransactionHelper.retrieveTransactionTemplate(loanExternalIdStr, "refundbycash", null, null, null);
        loanTransactionHelper.retrieveTransactionTemplate(loanExternalIdStr, "foreclosure", null, null, null);
        loanTransactionHelper.retrieveTransactionTemplate(loanExternalIdStr, "creditBalanceRefund", null, null, null);

        // Check whether an external id was generated
        String waiveChargeExternalIdStr = UUID.randomUUID().toString();
        PostLoansLoanIdChargesChargeIdResponse waiveLoanChargeResult = loanTransactionHelper.waiveLoanCharge((long) loanId,
                (long) penalty1LoanChargeId, new PostLoansLoanIdChargesChargeIdRequest().externalId(waiveChargeExternalIdStr));
        assertEquals(waiveChargeExternalIdStr, waiveLoanChargeResult.getSubResourceExternalId());
        assertEquals(penalty1LoanChargeExternalId, waiveLoanChargeResult.getResourceExternalId());

        GetLoansLoanIdTransactionsTransactionIdResponse response = loanTransactionHelper.getLoanTransactionDetails((long) loanId,
                waiveChargeExternalIdStr);
        assertEquals(waiveChargeExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, waiveLoanChargeResult.getSubResourceExternalId());
        assertEquals(waiveChargeExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, waiveChargeExternalIdStr);
        assertEquals(waiveChargeExternalIdStr, response.getExternalId());

        // Check whether an external id was generated
        PutChargeTransactionChangesResponse undoWaiveLoanChargeResult = loanTransactionHelper.undoWaiveLoanCharge((long) loanId,
                waiveChargeExternalIdStr, new PutChargeTransactionChangesRequest());
        assertEquals(waiveChargeExternalIdStr, undoWaiveLoanChargeResult.getSubResourceExternalId());

        response = loanTransactionHelper.getLoanTransactionDetails((long) loanId, waiveChargeExternalIdStr);
        assertEquals(waiveChargeExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, undoWaiveLoanChargeResult.getSubResourceId());
        assertEquals(waiveChargeExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, waiveChargeExternalIdStr);
        assertEquals(waiveChargeExternalIdStr, response.getExternalId());

        // Check whether an external id was generated
        waiveLoanChargeResult = loanTransactionHelper.waiveLoanCharge(loanExternalIdStr, (long) penalty1LoanChargeId,
                new PostLoansLoanIdChargesChargeIdRequest());
        assertNotNull(waiveLoanChargeResult.getSubResourceExternalId());
        assertEquals(penalty1LoanChargeExternalId, waiveLoanChargeResult.getResourceExternalId());

        // Check whether an external id was generated
        undoWaiveLoanChargeResult = loanTransactionHelper.undoWaiveLoanCharge(loanExternalIdStr, waiveLoanChargeResult.getSubResourceId(),
                new PutChargeTransactionChangesRequest());
        assertNotNull(undoWaiveLoanChargeResult.getSubResourceExternalId());

        // Check whether an external id was generated
        waiveChargeExternalIdStr = UUID.randomUUID().toString();
        waiveLoanChargeResult = loanTransactionHelper.waiveLoanCharge(loanExternalIdStr, penalty1LoanChargeExternalId,
                new PostLoansLoanIdChargesChargeIdRequest().externalId(waiveChargeExternalIdStr));
        assertEquals(waiveChargeExternalIdStr, waiveLoanChargeResult.getSubResourceExternalId());
        assertEquals(penalty1LoanChargeExternalId, waiveLoanChargeResult.getResourceExternalId());

        // Check whether an external id was generated
        undoWaiveLoanChargeResult = loanTransactionHelper.undoWaiveLoanCharge(loanExternalIdStr,
                waiveLoanChargeResult.getSubResourceExternalId(), new PutChargeTransactionChangesRequest());
        assertEquals(waiveChargeExternalIdStr, undoWaiveLoanChargeResult.getSubResourceExternalId());

        // Check whether an external id was generated
        final PostLoansLoanIdTransactionsResponse repaymentResult = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                        .transactionAmount(5.0));
        assertNotNull(repaymentResult.getResourceExternalId());

        String repaymentExternalId = repaymentResult.getResourceExternalId();
        response = loanTransactionHelper.getLoanTransactionDetails((long) loanId, repaymentExternalId);
        assertEquals(repaymentExternalId, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, repaymentResult.getResourceId());
        assertEquals(repaymentExternalId, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, repaymentExternalId);
        assertEquals(repaymentExternalId, response.getExternalId());

        // Check whether the provided external id was retrieved
        String transactionExternalIdStr = UUID.randomUUID().toString();
        final PostLoansLoanIdTransactionsResponse repaymentResultWithExternalId = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                        .transactionAmount(5.0).externalId(transactionExternalIdStr));
        assertEquals(transactionExternalIdStr, repaymentResultWithExternalId.getResourceExternalId());

        // Check whether an external id was generated
        final PostLoansLoanIdTransactionsResponse merchantIssuedRefundResult = loanTransactionHelper
                .makeMerchantIssuedRefund(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                        .transactionDate("06 September 2022").locale("en").transactionAmount(5.0));
        assertNotNull(merchantIssuedRefundResult.getResourceExternalId());

        // Check whether the provided external id was retrieved
        transactionExternalIdStr = UUID.randomUUID().toString();
        final PostLoansLoanIdTransactionsResponse merchantIssuedRefundResultWithExternalId = loanTransactionHelper
                .makeMerchantIssuedRefund(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                        .transactionDate("06 September 2022").locale("en").transactionAmount(5.0).externalId(transactionExternalIdStr));
        assertEquals(transactionExternalIdStr, merchantIssuedRefundResultWithExternalId.getResourceExternalId());

        response = loanTransactionHelper.getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr,
                merchantIssuedRefundResultWithExternalId.getResourceId());
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());

        // Check whether an external id was generated
        final PostLoansLoanIdTransactionsResponse payoutRefundResult = loanTransactionHelper.makePayoutRefund(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                        .transactionAmount(5.0));
        assertNotNull(payoutRefundResult.getResourceExternalId());

        // Check whether the provided external id was retrieved
        transactionExternalIdStr = UUID.randomUUID().toString();
        final PostLoansLoanIdTransactionsResponse payoutRefundResultWithExternalId = loanTransactionHelper
                .makePayoutRefund(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                        .transactionDate("06 September 2022").locale("en").transactionAmount(5.0).externalId(transactionExternalIdStr));
        assertEquals(transactionExternalIdStr, payoutRefundResultWithExternalId.getResourceExternalId());

        response = loanTransactionHelper.getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, payoutRefundResultWithExternalId.getResourceId());
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());

        // Check whether an external id was generated
        final PostLoansLoanIdTransactionsResponse goodWillCreditResult = loanTransactionHelper.makeGoodwillCredit(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                        .transactionAmount(5.0));
        assertNotNull(goodWillCreditResult.getResourceExternalId());

        // Check whether the provided external id was retrieved
        transactionExternalIdStr = UUID.randomUUID().toString();
        final PostLoansLoanIdTransactionsResponse goodWillCreditResultWithExternalId = loanTransactionHelper
                .makeGoodwillCredit(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                        .transactionDate("06 September 2022").locale("en").transactionAmount(5.0).externalId(transactionExternalIdStr));
        assertEquals(transactionExternalIdStr, goodWillCreditResultWithExternalId.getResourceExternalId());

        response = loanTransactionHelper.getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, goodWillCreditResultWithExternalId.getResourceId());
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());

        // Check whether an external id was generated
        final PostLoansLoanIdTransactionsResponse writeoffResult = loanTransactionHelper.makeWriteoff(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en"));
        assertNotNull(writeoffResult.getResourceExternalId());

        transactionExternalIdStr = writeoffResult.getResourceExternalId();
        response = loanTransactionHelper.getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, writeoffResult.getResourceId());
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());

        // Check whether an external id was generated
        final PostLoansLoanIdTransactionsResponse makeRecoveryPaymentResult = loanTransactionHelper.makeRecoveryPayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                        .transactionAmount(5.0));
        assertNotNull(makeRecoveryPaymentResult.getResourceExternalId());

        // Check whether the provided external id was retrieved
        transactionExternalIdStr = UUID.randomUUID().toString();
        final PostLoansLoanIdTransactionsResponse makeRecoveryPaymentResultWithExternalId = loanTransactionHelper
                .makeRecoveryPayment(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                        .transactionDate("06 September 2022").locale("en").transactionAmount(5.0).externalId(transactionExternalIdStr));
        assertEquals(transactionExternalIdStr, makeRecoveryPaymentResultWithExternalId.getResourceExternalId());

        response = loanTransactionHelper.getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr,
                makeRecoveryPaymentResultWithExternalId.getResourceId());
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());

        // Check whether an external id was generated
        final PostLoansLoanIdTransactionsResponse undoWriteoffResult = loanTransactionHelper.makeUndoWriteoff(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest());
        assertNotNull(undoWriteoffResult.getResourceExternalId());

        // Check whether the provided external id was retrieved
        transactionExternalIdStr = UUID.randomUUID().toString();
        final PostLoansLoanIdTransactionsResponse writeoffResultWithExternalId = loanTransactionHelper.makeWriteoff(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                        .externalId(transactionExternalIdStr));
        assertEquals(transactionExternalIdStr, writeoffResultWithExternalId.getResourceExternalId());

        // Check whether the provided external id was retrieved
        final PostLoansLoanIdTransactionsResponse undoWriteoffResultWithExternalId = loanTransactionHelper
                .makeUndoWriteoff(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest());
        assertEquals(transactionExternalIdStr, undoWriteoffResultWithExternalId.getResourceExternalId());

        response = loanTransactionHelper.getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, undoWriteoffResultWithExternalId.getResourceId());
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());

        // Overpay the account
        transactionExternalIdStr = UUID.randomUUID().toString();
        final PostLoansLoanIdTransactionsResponse overpaymentResultWithExternalId = loanTransactionHelper
                .makeLoanRepayment(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                        .transactionDate("06 September 2022").locale("en").transactionAmount(5000.0).externalId(transactionExternalIdStr));
        assertEquals(transactionExternalIdStr, overpaymentResultWithExternalId.getResourceExternalId());

        // Check whether an external id was generated
        final PostLoansLoanIdTransactionsResponse makeCreditBalanceRefundResult = loanTransactionHelper
                .makeCreditBalanceRefund(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                        .transactionDate("06 September 2022").locale("en").transactionAmount(5.0));
        assertNotNull(makeCreditBalanceRefundResult.getResourceExternalId());

        // Check whether the provided external id was retrieved
        transactionExternalIdStr = UUID.randomUUID().toString();
        final PostLoansLoanIdTransactionsResponse makeCreditBalanceRefundResultWithExternalId = loanTransactionHelper
                .makeCreditBalanceRefund(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                        .transactionDate("06 September 2022").locale("en").transactionAmount(5.0).externalId(transactionExternalIdStr));
        assertEquals(transactionExternalIdStr, makeCreditBalanceRefundResultWithExternalId.getResourceExternalId());

        response = loanTransactionHelper.getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr,
                makeCreditBalanceRefundResultWithExternalId.getResourceId());
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());

        // Check whether an external id was generated
        final PostLoansLoanIdTransactionsResponse chargeRefundResult = loanTransactionHelper.makeChargeRefund(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").locale("en").loanChargeId(penalty1LoanChargeId)
                        .transactionAmount(1.0));
        assertNotNull(chargeRefundResult.getResourceExternalId());

        // Check whether the provided external id was retrieved
        transactionExternalIdStr = UUID.randomUUID().toString();
        final PostLoansLoanIdTransactionsResponse chargeRefundResultWithExternalId = loanTransactionHelper
                .makeChargeRefund(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").locale("en")
                        .loanChargeId(penalty1LoanChargeId).transactionAmount(1.0).externalId(transactionExternalIdStr));
        assertEquals(transactionExternalIdStr, chargeRefundResultWithExternalId.getResourceExternalId());

        response = loanTransactionHelper.getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, chargeRefundResultWithExternalId.getResourceId());
        assertEquals(transactionExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
        assertEquals(transactionExternalIdStr, response.getExternalId());

        // Create a loan with interest and test the rest of the transactions

        final String loanProductWithInterestJSON = new LoanProductTestBuilder().withPrincipal("10000.0").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("2").withNumberOfRepayments("5").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccounting("1", null).build(null);
        final Integer loanProductWithInterestID = this.loanTransactionHelper.getLoanProductId(loanProductWithInterestJSON);

        LocalDate aMonthBefore = Utils.getLocalDateOfTenant().minusMonths(1);
        String formattedDate = dateFormatter.format(aMonthBefore);

        final Integer savingsId = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec,
                client.getClientId().intValue(), "10000.0");

        loanExternalIdStr = UUID.randomUUID().toString();
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("10000.0").withLoanTermFrequency("10")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("5").withRepaymentEveryAfter("2")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("1").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(formattedDate).withSubmittedOnDate(formattedDate).withLoanType("individual")
                .withExternalId(loanExternalIdStr)
                .build(client.getClientId().toString(), loanProductWithInterestID.toString(), savingsId.toString());
        final HashMap loanWithInterest = this.loanTransactionHelper.getLoanId(loanApplicationJSON, "");
        Integer loanWithInterestId = (Integer) loanWithInterest.get("resourceId");

        String chargeExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdChargesResponse loanChargeForApprovedLoanResult = this.loanTransactionHelper.addLoanCharge(loanExternalIdStr,
                new PostLoansLoanIdChargesRequest().externalId(chargeExternalId).amount(1.0).chargeId((long) penalty)
                        .dateFormat("dd MMMM yyyy").locale("en").dueDate(formattedDate));

        PutLoansLoanIdChargesChargeIdResponse updatedLoanChargeForApprovedLoanResult = this.loanTransactionHelper.updateLoanCharge(
                (long) loanWithInterestId, loanChargeForApprovedLoanResult.getResourceId(),
                new PutLoansLoanIdChargesChargeIdRequest().amount(2.0));
        assertEquals(loanChargeForApprovedLoanResult.getResourceId(), updatedLoanChargeForApprovedLoanResult.getResourceId());
        assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(),
                updatedLoanChargeForApprovedLoanResult.getResourceExternalId());

        DeleteLoansLoanIdChargesChargeIdResponse deleteLoanChargeResult = this.loanTransactionHelper
                .deleteLoanCharge((long) loanWithInterestId, loanChargeForApprovedLoanResult.getResourceId());
        assertEquals(loanChargeForApprovedLoanResult.getResourceId(), deleteLoanChargeResult.getResourceId());
        assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(), deleteLoanChargeResult.getResourceExternalId());

        chargeExternalId = UUID.randomUUID().toString();
        loanChargeForApprovedLoanResult = this.loanTransactionHelper.addLoanCharge(loanExternalIdStr,
                new PostLoansLoanIdChargesRequest().externalId(chargeExternalId).amount(1.0).chargeId((long) penalty)
                        .dateFormat("dd MMMM yyyy").locale("en").dueDate(formattedDate));

        updatedLoanChargeForApprovedLoanResult = this.loanTransactionHelper.updateLoanCharge((long) loanWithInterestId,
                loanChargeForApprovedLoanResult.getResourceExternalId(), new PutLoansLoanIdChargesChargeIdRequest().amount(1.0));
        assertEquals(loanChargeForApprovedLoanResult.getResourceId(), updatedLoanChargeForApprovedLoanResult.getResourceId());
        assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(),
                updatedLoanChargeForApprovedLoanResult.getResourceExternalId());

        deleteLoanChargeResult = this.loanTransactionHelper.deleteLoanCharge((long) loanWithInterestId,
                loanChargeForApprovedLoanResult.getResourceExternalId());
        assertEquals(loanChargeForApprovedLoanResult.getResourceId(), deleteLoanChargeResult.getResourceId());
        assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(), deleteLoanChargeResult.getResourceExternalId());

        chargeExternalId = UUID.randomUUID().toString();
        loanChargeForApprovedLoanResult = this.loanTransactionHelper.addLoanCharge(loanExternalIdStr,
                new PostLoansLoanIdChargesRequest().externalId(chargeExternalId).amount(1.0).chargeId((long) penalty)
                        .dateFormat("dd MMMM yyyy").locale("en").dueDate(formattedDate));

        updatedLoanChargeForApprovedLoanResult = this.loanTransactionHelper.updateLoanCharge(loanExternalIdStr,
                loanChargeForApprovedLoanResult.getResourceId(), new PutLoansLoanIdChargesChargeIdRequest().amount(1.0));
        assertEquals(loanChargeForApprovedLoanResult.getResourceId(), updatedLoanChargeForApprovedLoanResult.getResourceId());
        assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(),
                updatedLoanChargeForApprovedLoanResult.getResourceExternalId());

        deleteLoanChargeResult = this.loanTransactionHelper.deleteLoanCharge(loanExternalIdStr,
                loanChargeForApprovedLoanResult.getResourceId());
        assertEquals(loanChargeForApprovedLoanResult.getResourceId(), deleteLoanChargeResult.getResourceId());
        assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(), deleteLoanChargeResult.getResourceExternalId());

        chargeExternalId = UUID.randomUUID().toString();
        loanChargeForApprovedLoanResult = this.loanTransactionHelper.addLoanCharge(loanExternalIdStr,
                new PostLoansLoanIdChargesRequest().externalId(chargeExternalId).amount(1.0).chargeId((long) penalty)
                        .dateFormat("dd MMMM yyyy").locale("en").dueDate(formattedDate));

        updatedLoanChargeForApprovedLoanResult = this.loanTransactionHelper.updateLoanCharge(loanExternalIdStr,
                loanChargeForApprovedLoanResult.getResourceExternalId(), new PutLoansLoanIdChargesChargeIdRequest().amount(2.0));
        assertEquals(loanChargeForApprovedLoanResult.getResourceId(), updatedLoanChargeForApprovedLoanResult.getResourceId());
        assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(),
                updatedLoanChargeForApprovedLoanResult.getResourceExternalId());

        deleteLoanChargeResult = this.loanTransactionHelper.deleteLoanCharge(loanExternalIdStr,
                loanChargeForApprovedLoanResult.getResourceExternalId());
        assertEquals(loanChargeForApprovedLoanResult.getResourceId(), deleteLoanChargeResult.getResourceId());
        assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(), deleteLoanChargeResult.getResourceExternalId());

        this.loanTransactionHelper.approveLoan(formattedDate, loanWithInterestId);

        final HashMap disbursedLoanWithInterestResult = this.loanTransactionHelper.disburseLoan(formattedDate, loanWithInterestId, "1000",
                null);
        // Check whether an external id was generated
        assertNotNull(disbursedLoanWithInterestResult.get("resourceExternalId"));
        LocalDate aMonthBeforePlus3Days = aMonthBefore.plusDays(3);
        formattedDate = dateFormatter.format(aMonthBeforePlus3Days);

        String penalty3LoanChargeExternalId = UUID.randomUUID().toString();
        Integer penalty3LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanWithInterestId, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), formattedDate, "10", penalty3LoanChargeExternalId));

        Integer penalty4LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanWithInterestId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty2), formattedDate, "1000"));

        String penalty5LoanChargeExternalId = UUID.randomUUID().toString();
        Integer penalty5LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanWithInterestId, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty2), formattedDate, "1000", penalty5LoanChargeExternalId));

        // Check whether an external id was generated
        final PostLoansLoanIdTransactionsResponse waiveInterestResult = loanTransactionHelper.makeWaiveInterest(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(formattedDate).locale("en")
                        .transactionAmount(5.0));
        assertNotNull(waiveInterestResult.getResourceExternalId());

        // Check whether the provided external id was retrieved
        String waiveInterestTxnExternalIdStr = UUID.randomUUID().toString();
        final PostLoansLoanIdTransactionsResponse waiveInterestResultWithExternalId = loanTransactionHelper
                .makeWaiveInterest(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                        .transactionDate(formattedDate).locale("en").transactionAmount(1.0).externalId(waiveInterestTxnExternalIdStr));
        assertEquals(waiveInterestTxnExternalIdStr, waiveInterestResultWithExternalId.getResourceExternalId());

        response = loanTransactionHelper.getLoanTransactionDetails((long) loanWithInterestId, waiveInterestTxnExternalIdStr);
        assertEquals(waiveInterestTxnExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, waiveInterestResultWithExternalId.getResourceId());
        assertEquals(waiveInterestTxnExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, waiveInterestTxnExternalIdStr);
        assertEquals(waiveInterestTxnExternalIdStr, response.getExternalId());

        String inAdvanceRepaymentTxnExternalIdStr = UUID.randomUUID().toString();
        final PostLoansLoanIdTransactionsResponse inAdvanceRepaymentResult = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(formattedDate).locale("en")
                        .transactionAmount(500.0).externalId(inAdvanceRepaymentTxnExternalIdStr));

        String inAdvanceRepayment2TxnExternalIdStr = UUID.randomUUID().toString();
        final PostLoansLoanIdTransactionsResponse inAdvanceRepayment2Result = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(formattedDate).locale("en")
                        .transactionAmount(50.0).externalId(inAdvanceRepayment2TxnExternalIdStr));

        // Check whether an external id was generated
        final PostLoansLoanIdTransactionsResponse makeRefundByCashResult = loanTransactionHelper.makeRefundByCash(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(formattedDate).locale("en")
                        .transactionAmount(1.0));
        assertNotNull(makeRefundByCashResult.getResourceExternalId());

        // Check whether the provided external id was retrieved
        String makeRefundTxnExternalIdStr = UUID.randomUUID().toString();
        final PostLoansLoanIdTransactionsResponse makeRefundByCashResultWithExternalId = loanTransactionHelper
                .makeRefundByCash(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                        .transactionDate(formattedDate).locale("en").transactionAmount(5.0).externalId(makeRefundTxnExternalIdStr));
        assertEquals(makeRefundTxnExternalIdStr, makeRefundByCashResultWithExternalId.getResourceExternalId());

        response = loanTransactionHelper.getLoanTransactionDetails((long) loanWithInterestId, makeRefundTxnExternalIdStr);
        assertEquals(makeRefundTxnExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, makeRefundByCashResultWithExternalId.getResourceId());
        assertEquals(makeRefundTxnExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, makeRefundTxnExternalIdStr);
        assertEquals(makeRefundTxnExternalIdStr, response.getExternalId());

        PostLoansLoanIdTransactionsResponse adjustmentResult = loanTransactionHelper.reverseLoanTransaction((long) loanWithInterestId,
                inAdvanceRepayment2TxnExternalIdStr, new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(formattedDate)
                        .locale("en").dateFormat("dd MMMM yyyy").transactionAmount(0.0));
        assertEquals(inAdvanceRepayment2TxnExternalIdStr, adjustmentResult.getResourceExternalId());

        response = loanTransactionHelper.getLoanTransactionDetails((long) loanWithInterestId, inAdvanceRepayment2TxnExternalIdStr);
        assertEquals(inAdvanceRepayment2TxnExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, adjustmentResult.getResourceId());
        assertEquals(inAdvanceRepayment2TxnExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, inAdvanceRepayment2TxnExternalIdStr);
        assertEquals(inAdvanceRepayment2TxnExternalIdStr, response.getExternalId());

        adjustmentResult = loanTransactionHelper.reverseLoanTransaction(loanExternalIdStr, inAdvanceRepaymentResult.getResourceId(),
                new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(formattedDate).locale("en").dateFormat("dd MMMM yyyy")
                        .transactionAmount(0.0));
        assertEquals(inAdvanceRepaymentTxnExternalIdStr, adjustmentResult.getResourceExternalId());

        String adjustTransactionExternalId = UUID.randomUUID().toString();
        adjustmentResult = loanTransactionHelper.adjustLoanTransaction(loanExternalIdStr, waiveInterestTxnExternalIdStr,
                new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(formattedDate).locale("en").dateFormat("dd MMMM yyyy")
                        .transactionAmount(2.0).externalId(adjustTransactionExternalId));
        assertEquals(adjustTransactionExternalId, adjustmentResult.getResourceExternalId());

        response = loanTransactionHelper.getLoanTransactionDetails((long) loanWithInterestId, adjustTransactionExternalId);
        assertEquals(adjustTransactionExternalId, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, adjustmentResult.getResourceId());
        assertEquals(adjustTransactionExternalId, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, adjustTransactionExternalId);
        assertEquals(adjustTransactionExternalId, response.getExternalId());

        adjustmentResult = loanTransactionHelper.adjustLoanTransaction(loanExternalIdStr, adjustmentResult.getResourceExternalId(),
                new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(formattedDate).locale("en").dateFormat("dd MMMM yyyy")
                        .transactionAmount(1.0));
        assertNotNull(adjustmentResult.getResourceExternalId());

        String repaymentForChargeback = UUID.randomUUID().toString();
        final PostLoansLoanIdTransactionsResponse repaymentForChargebackResult = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(formattedDate).locale("en")
                        .transactionAmount(5.0).externalId(repaymentForChargeback));

        String chargebackTransactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse chargebackResult = loanTransactionHelper.chargebackLoanTransaction(loanExternalIdStr,
                repaymentForChargebackResult.getResourceExternalId(), new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en")
                        .transactionAmount(2.0).externalId(chargebackTransactionExternalId).paymentTypeId(1L));
        assertEquals(chargebackTransactionExternalId, chargebackResult.getResourceExternalId());

        response = loanTransactionHelper.getLoanTransactionDetails((long) loanWithInterestId, chargebackTransactionExternalId);
        assertEquals(chargebackTransactionExternalId, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, chargebackResult.getResourceId());
        assertEquals(chargebackTransactionExternalId, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, chargebackTransactionExternalId);
        assertEquals(chargebackTransactionExternalId, response.getExternalId());

        chargebackResult = loanTransactionHelper.chargebackLoanTransaction(loanExternalIdStr,
                repaymentForChargebackResult.getResourceExternalId(),
                new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en").transactionAmount(2.0).paymentTypeId(2L));
        assertNotNull(chargebackResult.getResourceExternalId());

        // Check whether the provided external id was retrieved
        String chargeAdjustmentExternalIdStr = UUID.randomUUID().toString();
        PostLoansLoanIdChargesChargeIdResponse chargeAdjustmentResult = loanTransactionHelper.chargeAdjustment((long) loanWithInterestId,
                (long) penalty3LoanChargeId,
                new PostLoansLoanIdChargesChargeIdRequest().externalId(chargeAdjustmentExternalIdStr).amount(1.0).locale("en"));
        assertEquals(chargeAdjustmentExternalIdStr, chargeAdjustmentResult.getSubResourceExternalId());
        assertEquals(penalty3LoanChargeExternalId, chargeAdjustmentResult.getResourceExternalId());

        response = loanTransactionHelper.getLoanTransactionDetails((long) loanWithInterestId, chargeAdjustmentExternalIdStr);
        assertEquals(chargeAdjustmentExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, chargeAdjustmentResult.getSubResourceExternalId());
        assertEquals(chargeAdjustmentExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, chargeAdjustmentExternalIdStr);
        assertEquals(chargeAdjustmentExternalIdStr, response.getExternalId());

        // Check whether an external id was generated
        chargeAdjustmentResult = loanTransactionHelper.chargeAdjustment(loanExternalIdStr, penalty3LoanChargeExternalId,
                new PostLoansLoanIdChargesChargeIdRequest().amount(1.0).locale("en"));
        assertNotNull(chargeAdjustmentResult.getSubResourceExternalId());
        assertEquals(penalty3LoanChargeExternalId, chargeAdjustmentResult.getResourceExternalId());

        PostLoansLoanIdChargesChargeIdResponse payChargeResult = this.loanTransactionHelper.payLoanCharge(loanExternalIdStr,
                (long) penalty4LoanChargeId,
                new PostLoansLoanIdChargesChargeIdRequest().locale("en").dateFormat("dd MMMM yyyy").transactionDate(formattedDate));
        assertNotNull(payChargeResult.getSubResourceExternalId());
        assertNotNull(payChargeResult.getResourceExternalId());

        String payChargeExternalIdStr = UUID.randomUUID().toString();
        payChargeResult = this.loanTransactionHelper.payLoanCharge(loanExternalIdStr, penalty5LoanChargeExternalId,
                new PostLoansLoanIdChargesChargeIdRequest().locale("en").dateFormat("dd MMMM yyyy").transactionDate(formattedDate)
                        .externalId(payChargeExternalIdStr));
        assertEquals(payChargeExternalIdStr, payChargeResult.getSubResourceExternalId());
        assertEquals(penalty5LoanChargeExternalId, payChargeResult.getResourceExternalId());

        response = loanTransactionHelper.getLoanTransactionDetails((long) loanWithInterestId, payChargeExternalIdStr);
        assertEquals(payChargeExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, payChargeResult.getSubResourceId());
        assertEquals(payChargeExternalIdStr, response.getExternalId());
        response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, payChargeExternalIdStr);
        assertEquals(payChargeExternalIdStr, response.getExternalId());

        GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(requestSpec, responseSpec, 50, false);
    }

    @Test
    public void negativeTest() {
        // INIT
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account assetFeeAndPenaltyAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccountingRulePeriodicAccrual(new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount })
                .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0")
                .withFeeAndPenaltyAssetAccount(assetFeeAndPenaltyAccount).build(null);
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJSON);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        String loanExternalIdStr = UUID.randomUUID().toString();
        final HashMap loan = applyForLoanApplication(client.getClientId().intValue(), loanProductID, loanExternalIdStr);
        Integer loanId = (Integer) loan.get("resourceId");

        this.loanTransactionHelper.approveLoan("02 September 2022", loanId);
        String txnExternalIdStr = UUID.randomUUID().toString();
        final HashMap disbursedLoanResult = this.loanTransactionHelper.disburseLoan("03 September 2022", loanId, "1000", txnExternalIdStr);

        // Check whether the provided external id was retrieved
        assertEquals(txnExternalIdStr, disbursedLoanResult.get("resourceExternalId"));

        // Second loan
        final HashMap loan2 = applyForLoanApplication(client.getClientId().intValue(), loanProductID, null);
        Integer loan2Id = (Integer) loan2.get("resourceId");
        this.loanTransactionHelper.approveLoan("02 September 2022", loan2Id);
        final HashMap disbursedLoan2Result = this.loanTransactionHelper.disburseLoan("03 September 2022", loan2Id, "1000", null);

        Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

        LocalDate targetDate = LocalDate.of(2022, 9, 7);
        final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

        String penalty1LoanChargeExternalId = UUID.randomUUID().toString();
        Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loan2Id,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10",
                        penalty1LoanChargeExternalId));

        // NEGATIVE SCENARIOS

        // GET
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> this.loanTransactionHelper
                .retrieveTransactionTemplate("randomNonExistingLoanExternalId", "disburse", null, null, null));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

        exception = assertThrows(CallFailedRuntimeException.class, () -> this.loanTransactionHelper
                .getLoanTransactionDetails("randomNonExistingLoanExternalId", "randomNonExistingLoanTransactionExternalId"));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

        exception = assertThrows(CallFailedRuntimeException.class, () -> this.loanTransactionHelper
                .getLoanTransactionDetails(loanExternalIdStr, "randomNonExistingLoanTransactionExternalId"));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.transaction.external.id.invalid"));

        // POST
        exception = assertThrows(CallFailedRuntimeException.class, () -> this.loanTransactionHelper
                .makeChargeRefund("randomNonExistingLoanExternalId", new PostLoansLoanIdTransactionsRequest()));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

        exception = assertThrows(CallFailedRuntimeException.class,
                () -> this.loanTransactionHelper.adjustLoanTransaction("randomNonExistingLoanExternalId",
                        "randomNonExistingLoanTransactionExternalId", new PostLoansLoanIdTransactionsTransactionIdRequest()));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

        exception = assertThrows(CallFailedRuntimeException.class, () -> this.loanTransactionHelper.adjustLoanTransaction(loanExternalIdStr,
                "randomNonExistingLoanTransactionExternalId", new PostLoansLoanIdTransactionsTransactionIdRequest()));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.transaction.external.id.invalid"));

        // PUT
        exception = assertThrows(CallFailedRuntimeException.class,
                () -> this.loanTransactionHelper.undoWaiveLoanCharge("randomNonExistingLoanExternalId",
                        "randomNonExistingLoanTransactionExternalId", new PutChargeTransactionChangesRequest()));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

        exception = assertThrows(CallFailedRuntimeException.class, () -> this.loanTransactionHelper.undoWaiveLoanCharge(loanExternalIdStr,
                "randomNonExistingLoanTransactionExternalId", new PutChargeTransactionChangesRequest()));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.transaction.external.id.invalid"));

        exception = assertThrows(CallFailedRuntimeException.class,
                () -> this.loanTransactionHelper.getLoanCharges("randomNonExistingLoanExternalId"));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

        exception = assertThrows(CallFailedRuntimeException.class,
                () -> this.loanTransactionHelper.getLoanChargeTemplate("randomNonExistingLoanExternalId"));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

        exception = assertThrows(CallFailedRuntimeException.class,
                () -> this.loanTransactionHelper.getLoanCharge("randomNonExistingLoanExternalId", "randomNonExistingLoanChargeExternalId"));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

        exception = assertThrows(CallFailedRuntimeException.class,
                () -> this.loanTransactionHelper.getLoanCharge(-1L, (long) penalty1LoanChargeId));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loanCharge.id.invalid.for.given.loan"));

        exception = assertThrows(CallFailedRuntimeException.class,
                () -> this.loanTransactionHelper.getLoanCharge(loanExternalIdStr, (long) penalty1LoanChargeId));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loanCharge.id.invalid.for.given.loan"));

        exception = assertThrows(CallFailedRuntimeException.class,
                () -> this.loanTransactionHelper.getLoanCharge(loanExternalIdStr, "randomNonExistingLoanChargeExternalId"));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loanCharge.external.id.invalid"));

        exception = assertThrows(CallFailedRuntimeException.class,
                () -> this.loanTransactionHelper.payLoanCharge("randomNonExistingLoanExternalId", "randomNonExistingLoanChargeExternalId",
                        new PostLoansLoanIdChargesChargeIdRequest()));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

        exception = assertThrows(CallFailedRuntimeException.class, () -> this.loanTransactionHelper.payLoanCharge(loanExternalIdStr,
                "randomNonExistingLoanChargeExternalId", new PostLoansLoanIdChargesChargeIdRequest()));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loanCharge.external.id.invalid"));

        exception = assertThrows(CallFailedRuntimeException.class,
                () -> this.loanTransactionHelper.updateLoanCharge("randomNonExistingLoanExternalId",
                        "randomNonExistingLoanChargeExternalId", new PutLoansLoanIdChargesChargeIdRequest()));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

        exception = assertThrows(CallFailedRuntimeException.class, () -> this.loanTransactionHelper.updateLoanCharge(loanExternalIdStr,
                "randomNonExistingLoanChargeExternalId", new PutLoansLoanIdChargesChargeIdRequest()));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loanCharge.external.id.invalid"));

        exception = assertThrows(CallFailedRuntimeException.class, () -> this.loanTransactionHelper
                .deleteLoanCharge("randomNonExistingLoanExternalId", "randomNonExistingLoanChargeExternalId"));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

        exception = assertThrows(CallFailedRuntimeException.class,
                () -> this.loanTransactionHelper.deleteLoanCharge(loanExternalIdStr, "randomNonExistingLoanChargeExternalId"));
        assertEquals(404, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loanCharge.external.id.invalid"));
    }

    private HashMap applyForLoanApplication(final Integer clientID, final Integer loanProductID, final String externalId) {
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);
        return (HashMap) this.loanTransactionHelper.createLoanAccount(loanApplicationJSON, "");
    }
}
