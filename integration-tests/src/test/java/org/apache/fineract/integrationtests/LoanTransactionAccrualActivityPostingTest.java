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

import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.DEFAULT_STRATEGY;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.models.PostChargesRequest;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.inlinecob.InlineLoanCOBHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanTransactionAccrualActivityPostingTest extends BaseLoanIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanTransactionAccrualActivityPostingTest.class);
    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private static BusinessDateHelper businessDateHelper;
    private static LoanTransactionHelper loanTransactionHelper;
    private static PostClientsResponse client;
    private static LoanRescheduleRequestHelper loanRescheduleRequestHelper;
    private static ChargesHelper chargesHelper;
    private static InlineLoanCOBHelper inlineLoanCOBHelper;

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        businessDateHelper = new BusinessDateHelper();
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
        loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(requestSpec, responseSpec);
        chargesHelper = new ChargesHelper();
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
    }

    // Create Loan with Interest and enabled Accrual Activity Posting
    // Approve and disburse loan
    // charge penalty with due date as 1st installment
    // charge fee with due date as 1st installment
    // charge penalty with due date as 3rd installment
    // charge fee with due date as 2nd installment
    @Test
    public void test() {
        final String disbursementDay = "01 January 2023";
        final String repaymentPeriod1DueDate = "01 February 2023";
        final String repaymentPeriod1CloseDate = "02 February 2023";
        final String repaymentPeriod1OneDayBeforeCloseDate = "01 February 2023";
        final String repaymentPeriod1OneDayAfterCloseDate = "03 February 2023";
        final String repaymentPeriod2DueDate = "01 March 2023";
        final String repaymentPeriod3DueDate = "01 April 2023";
        final String creationBusinessDay = "15 May 2023";
        AtomicReference<Long> loanId = new AtomicReference<>();
        runAt(creationBusinessDay, () -> {

            Long localLoanProductId = createLoanProductAccountingAccrualPeriodicWithInterest();
            loanId.set(applyForLoanApplicationWithInterest(client.getClientId(), localLoanProductId, BigDecimal.valueOf(40000),
                    disbursementDay));

            LOG.info("Test Loan Product Id {1} http://localhost:4200/#/products/loan-products/{}/general", localLoanProductId);
            LOG.info("Test Client Id {1} http://localhost:4200/#/clients/{}", client.getClientId());
            LOG.info("Test Loan Id {2} http://localhost:4200/#/clients/{}/loans-accounts/{}/transactions", client.getClientId(), loanId);

            loanTransactionHelper.approveLoan(loanId.get(), new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate(disbursementDay).locale("en"));

            loanTransactionHelper.disburseLoan(loanId.get(), new PostLoansLoanIdRequest().actualDisbursementDate(disbursementDay)
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            chargePenalty(loanId.get(), 30.0, repaymentPeriod1DueDate);
            chargePenalty(loanId.get(), 50.0, repaymentPeriod2DueDate);
            chargeFee(loanId.get(), 40.0, repaymentPeriod1DueDate);
            chargeFee(loanId.get(), 60.0, repaymentPeriod3DueDate);

        });
        runAt(repaymentPeriod1OneDayBeforeCloseDate, () -> {
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.get()));
            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(19.35, "Accrual", "31 January 2023", 0, 0, 19.35, 0, 0, 0.0, 0.0));
        });
        runAt(repaymentPeriod1CloseDate, () -> {
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.get()));
            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(19.35, "Accrual", "31 January 2023", 0, 0, 19.35, 0, 0, 0.0, 0.0),
                    transaction(70.65, "Accrual", "01 February 2023", 0, 0, 0.65, 40, 30, 0.0, 0.0),
                    transaction(90.0, "Accrual Activity", "01 February 2023", 0, 0, 20.0, 40.0, 30.0, 0.0, 0.0));

        });
        runAt(repaymentPeriod1OneDayAfterCloseDate, () -> {
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.get()));
            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(19.35, "Accrual", "31 January 2023", 0, 0, 19.35, 0, 0, 0.0, 0.0),
                    transaction(70.65, "Accrual", "01 February 2023", 0, 0, 0.65, 40, 30, 0.0, 0.0),
                    transaction(90.0, "Accrual Activity", "01 February 2023", 1000, 0, 20.0, 40.0, 30.0, 0.0, 0.0),
                    transaction(0.71, "Accrual", "02 February 2023", 0, 0, 0.71, 0, 0, 0.0, 0.0));

        });
    }

    private void chargeFee(Long loanId, Double amount, String dueDate) {
        PostChargesResponse feeCharge = chargesHelper.createCharges(new PostChargesRequest().penalty(false).amount(9.0)
                .chargeCalculationType(ChargeCalculationType.FLAT.getValue()).chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue())
                .chargePaymentMode(ChargePaymentMode.REGULAR.getValue()).currencyCode("USD")
                .name(Utils.randomStringGenerator("FEE_" + Calendar.getInstance().getTimeInMillis(), 5)).chargeAppliesTo(1).locale("en")
                .active(true));
        PostLoansLoanIdChargesResponse feeLoanChargeResult = loanTransactionHelper.addChargesForLoan(loanId,
                new PostLoansLoanIdChargesRequest().chargeId(feeCharge.getResourceId()).dateFormat(DATETIME_PATTERN).locale("en")
                        .amount(amount).dueDate(dueDate));
        assertNotNull(feeLoanChargeResult);
        assertNotNull(feeLoanChargeResult.getResourceId());
    }

    private void chargePenalty(Long loanId, Double amount, String dueDate) {
        PostChargesResponse penaltyCharge = chargesHelper.createCharges(new PostChargesRequest().penalty(true).amount(10.0)
                .chargeCalculationType(ChargeCalculationType.FLAT.getValue()).chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue())
                .chargePaymentMode(ChargePaymentMode.REGULAR.getValue()).currencyCode("USD")
                .name(Utils.randomStringGenerator("PENALTY_" + Calendar.getInstance().getTimeInMillis(), 5)).chargeAppliesTo(1).locale("en")
                .active(true));
        PostLoansLoanIdChargesResponse penaltyLoanChargeResult = loanTransactionHelper.addChargesForLoan(loanId,
                new PostLoansLoanIdChargesRequest().chargeId(penaltyCharge.getResourceId()).dateFormat(DATETIME_PATTERN).locale("en")
                        .amount(amount).dueDate(dueDate));
        assertNotNull(penaltyLoanChargeResult);
        assertNotNull(penaltyLoanChargeResult.getResourceId());
    }

    private Long createLoanProductAccountingAccrualPeriodicWithInterest() {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        String name = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
        String shortName = Utils.uniqueRandomStringGenerator("", 4);
        return loanTransactionHelper.createLoanProduct(new PostLoanProductsRequest().name(name).shortName(shortName)
                .description("Test loan description").currencyCode("USD").digitsAfterDecimal(2).daysInYearType(1).daysInMonthType(1)
                .interestRecalculationCompoundingMethod(0).recalculationRestFrequencyType(1).rescheduleStrategyMethod(1)
                .recalculationRestFrequencyInterval(0).isInterestRecalculationEnabled(false).interestRateFrequencyType(2).locale("en_GB")
                .numberOfRepayments(4).repaymentFrequencyType(2L).interestRatePerPeriod(2.0).repaymentEvery(1).minPrincipal(100.0)
                .principal(1000.0).maxPrincipal(10000000.0).amortizationType(1).interestType(1).interestCalculationPeriodType(1)
                .dateFormat("dd MMMM yyyy").transactionProcessingStrategyCode(DEFAULT_STRATEGY).accountingRule(3)
                .enableAccrualActivityPosting(true).fundSourceAccountId(fundSource.getAccountID().longValue())//
                .loanPortfolioAccountId(loansReceivableAccount.getAccountID().longValue())//
                .transfersInSuspenseAccountId(suspenseAccount.getAccountID().longValue())//
                .interestOnLoanAccountId(interestIncomeAccount.getAccountID().longValue())//
                .incomeFromFeeAccountId(feeIncomeAccount.getAccountID().longValue())//
                .incomeFromPenaltyAccountId(feeIncomeAccount.getAccountID().longValue())//
                .incomeFromRecoveryAccountId(recoveriesAccount.getAccountID().longValue())//
                .writeOffAccountId(writtenOffAccount.getAccountID().longValue())//
                .overpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue())//
                .receivableInterestAccountId(interestReceivableAccount.getAccountID().longValue())//
                .receivableFeeAccountId(feeReceivableAccount.getAccountID().longValue())//
                .receivablePenaltyAccountId(penaltyReceivableAccount.getAccountID().longValue())//
                .goodwillCreditAccountId(goodwillExpenseAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditInterestAccountId(goodwillIncomeAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditFeesAccountId(goodwillIncomeAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditPenaltyAccountId(goodwillIncomeAccount.getAccountID().longValue())//
                .incomeFromChargeOffInterestAccountId(interestIncomeChargeOffAccount.getAccountID().longValue())//
                .incomeFromChargeOffFeesAccountId(feeChargeOffAccount.getAccountID().longValue())//
                .chargeOffExpenseAccountId(chargeOffExpenseAccount.getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(chargeOffFraudExpenseAccount.getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(penaltyChargeOffAccount.getAccountID().longValue())//
        ).getResourceId();
    }

    private static Long applyForLoanApplicationWithInterest(final Long clientID, final Long loanProductID, BigDecimal principal,
            String applicationDisbursementDate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final PostLoansRequest loanRequest = new PostLoansRequest() //
                .loanTermFrequency(4).locale("en_GB").loanTermFrequencyType(2).numberOfRepayments(4).repaymentFrequencyType(2)
                .interestRatePerPeriod(BigDecimal.valueOf(2)).repaymentEvery(1).principal(principal).amortizationType(1).interestType(1)
                .interestCalculationPeriodType(1).dateFormat("dd MMMM yyyy").transactionProcessingStrategyCode(DEFAULT_STRATEGY)
                .loanType("individual").expectedDisbursementDate(applicationDisbursementDate).submittedOnDate(applicationDisbursementDate)
                .clientId(clientID).productId(loanProductID);
        return loanTransactionHelper.applyLoan(loanRequest).getLoanId();
    }

}
