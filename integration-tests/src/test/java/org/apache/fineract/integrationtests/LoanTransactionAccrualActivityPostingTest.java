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

import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.DEFAULT_STRATEGY;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostChargesRequest;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.inlinecob.InlineLoanCOBHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanTransactionAccrualActivityPostingTest extends BaseLoanIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanTransactionAccrualActivityPostingTest.class);
    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private static LoanTransactionHelper loanTransactionHelper;
    private static PostClientsResponse client;
    private static ChargesHelper chargesHelper;
    private static InlineLoanCOBHelper inlineLoanCOBHelper;
    private static BusinessStepHelper businessStepHelper;
    private static SchedulerJobHelper schedulerJobHelper;

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        schedulerJobHelper = new SchedulerJobHelper(requestSpec);
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
        chargesHelper = new ChargesHelper();
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
        businessStepHelper = new BusinessStepHelper();
        // setup COB Business Steps to prevent test failing due other integration test configurations
        businessStepHelper.updateSteps("LOAN_CLOSE_OF_BUSINESS", "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION",
                "CHECK_LOAN_REPAYMENT_DUE", "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "EXTERNAL_ASSET_OWNER_TRANSFER", "ACCRUAL_ACTIVITY_POSTING");
    }

    @Test
    public void testAccrualActivityPostingForMultiDisburseProgressiveLoan() {
        final String disbursementDay = "01 January 2023";
        final String disbursementDay2 = "02 February 2023";
        final String repaymentDate1 = "15 January 2023";
        final String repaymentPeriod1DueDate = "01 February 2023";
        final String repaymentPeriod1CloseDate = "02 February 2023";
        final String creationBusinessDay = "15 May 2023";
        AtomicReference<Long> loanId = new AtomicReference<>();
        AtomicReference<Long> repaymentId = new AtomicReference<>();
        runAt(creationBusinessDay, () -> {
            Long localLoanProductId = createLoanProductAccountingAccrualPeriodicAdvancedPaymentAllocation(true);
            loanId.set(applyForLoanApplicationAdvancedPaymentAllocation(client.getClientId(), localLoanProductId, BigDecimal.valueOf(1000),
                    disbursementDay, disbursementDay2));
            loanTransactionHelper.approveLoan(loanId.get(), new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate(disbursementDay).locale("en"));

            loanTransactionHelper.disburseLoan(loanId.get(), new PostLoansLoanIdRequest().actualDisbursementDate(disbursementDay)
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(500.0)).locale("en"));
            chargePenalty(loanId.get(), 30.0, repaymentPeriod1DueDate);
            chargeFee(loanId.get(), 40.0, repaymentPeriod1DueDate);
            repaymentId.set(addRepaymentForLoan(loanId.get(), 570.0, repaymentDate1));
            // verify that the Accrual Activity transaction is created
            verifyTransactions(loanId.get(),
                    transaction(500.0, "Disbursement", disbursementDay, 500.0, 0, 0, 0, 0, 0, 0, false),
                    transaction(570, "Repayment", repaymentDate1, 0.0, 500.0, 0, 40.0, 30.0, 0, 0, true),
                    transaction(70.0, "Accrual", repaymentDate1, 0, 0, 0.0, 40.0, 30.0, 0, 0, false)
            );
        });
        runAt(repaymentPeriod1CloseDate, () -> {
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.get()));
            // verify that the Accrual Activity transaction is not created
            verifyTransactions(loanId.get(),
                    transaction(500.0, "Disbursement", disbursementDay, 500.0, 0, 0, 0, 0, 0, 0, false),
                    transaction(570, "Repayment", repaymentDate1, 0.0, 500.0, 0, 40.0, 30.0, 0, 0, true),
                    transaction(70.0, "Accrual", repaymentDate1, 0, 0, 0.0, 40.0, 30.0, 0, 0, false)
            );
            loanTransactionHelper.disburseLoan(loanId.get(), new PostLoansLoanIdRequest().actualDisbursementDate(disbursementDay2)
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(500.0)).locale("en"));
            // verify that the Accrual Activity transaction on repayment date is not reverted
            // verify that the Accrual Activity transaction on 1st installment due date is not created
            verifyTransactions(loanId.get(),
                    transaction(500.0, "Disbursement", disbursementDay, 500.0, 0, 0, 0, 0, 0, 0, false),
                    transaction(570, "Repayment", repaymentDate1, 0.0, 500.0, 0, 40.0, 30.0, 0, 0, false),
                    transaction(70.0, "Accrual", repaymentDate1, 0, 0, 0.0, 40.0, 30.0, 0, 0, false),
                    transaction(500.0, "Disbursement", disbursementDay2, 500, 0, 0, 0, 0, 0, 0, false)
            );
        });
    }


    private void chargeFee(Long loanId, Double amount, String dueDate) {
        LOG.info("Charge FEE amount {} dueDate {}", amount, dueDate);
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
        LOG.info("Charge PENALTY amount {} dueDate {}", amount, dueDate);
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
        return createLoanProductAccountingAccrualPeriodicWithInterest(false);
    }

    private Long createLoanProductAccountingAccrualPeriodicWithInterest(boolean isMultiDisburse) {
        String name = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
        String shortName = Utils.uniqueRandomStringGenerator("", 4);
        Long resourceId = loanTransactionHelper.createLoanProduct(new PostLoanProductsRequest().name(name).shortName(shortName)
                .multiDisburseLoan(isMultiDisburse).maxTrancheCount(isMultiDisburse ? 2 : 1).interestType(isMultiDisburse ? 0 : 1)
                .interestCalculationPeriodType(isMultiDisburse ? 0 : 1).disallowExpectedDisbursements(isMultiDisburse)
                .description("Test loan description").currencyCode("USD").digitsAfterDecimal(2).daysInYearType(1).daysInMonthType(1)
                .interestRecalculationCompoundingMethod(0).recalculationRestFrequencyType(1).rescheduleStrategyMethod(1)
                .recalculationRestFrequencyInterval(0).isInterestRecalculationEnabled(false).interestRateFrequencyType(2).locale("en_GB")
                .numberOfRepayments(4).repaymentFrequencyType(2L).interestRatePerPeriod(2.0).repaymentEvery(1).minPrincipal(100.0)
                .principal(1000.0).maxPrincipal(10000000.0).amortizationType(1).dateFormat("dd MMMM yyyy")
                .transactionProcessingStrategyCode(DEFAULT_STRATEGY).accountingRule(3).enableAccrualActivityPosting(true)
                .fundSourceAccountId(fundSource.getAccountID().longValue())//
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
        LOG.info("Test Loan Product With Interest Id {} isMultiDisburse {} http://localhost:4200/#/products/loan-products/{1}/general",
                resourceId, isMultiDisburse);
        return resourceId;
    }

    private static Long applyForLoanApplicationWithInterest(final Long clientID, final Long loanProductID, BigDecimal principal,
                                                            String applicationDisbursementDate) {
        final PostLoansRequest loanRequest = new PostLoansRequest() //
                .loanTermFrequency(4).locale("en_GB").loanTermFrequencyType(2).numberOfRepayments(4).repaymentFrequencyType(2)
                .interestRatePerPeriod(BigDecimal.valueOf(2)).repaymentEvery(1).principal(principal).amortizationType(1).interestType(1)
                .interestCalculationPeriodType(1).dateFormat("dd MMMM yyyy").transactionProcessingStrategyCode(DEFAULT_STRATEGY)
                .loanType("individual").expectedDisbursementDate(applicationDisbursementDate).submittedOnDate(applicationDisbursementDate)
                .clientId(clientID).productId(loanProductID);
        Long loanId = loanTransactionHelper.applyLoan(loanRequest).getLoanId();
        LOG.info("Test Loan with Interest Id {2} http://localhost:4200/#/clients/{}/loans-accounts/{}/transactions", client.getClientId(),
                loanId);
        return loanId;
    }

    private static Long applyForLoanApplicationWithInterest(final Long clientID, final Long loanProductID, BigDecimal principal,
                                                            String applicationDisbursementDate, String applicationDisbursementDate2) {
        final PostLoansRequest loanRequest = new PostLoansRequest() //
                .loanTermFrequency(4).locale("en_GB").loanTermFrequencyType(2).numberOfRepayments(4).repaymentFrequencyType(2)
                .interestRatePerPeriod(BigDecimal.valueOf(2)).repaymentEvery(1).principal(principal).amortizationType(1).interestType(1)
                .interestCalculationPeriodType(0).dateFormat("dd MMMM yyyy").transactionProcessingStrategyCode(DEFAULT_STRATEGY)
                .loanType("individual").submittedOnDate(applicationDisbursementDate).expectedDisbursementDate(applicationDisbursementDate2)
                .clientId(clientID).productId(loanProductID);
        Long loanId = loanTransactionHelper.applyLoan(loanRequest).getLoanId();
        LOG.info("Test Loan with Interest Id MultiDisbursed {2} http://localhost:4200/#/clients/{}/loans-accounts/{}/transactions",
                client.getClientId(), loanId);
        return loanId;
    }

    private Long createLoanProductAccountingAccrualPeriodicAdvancedPaymentAllocation() {
        return createLoanProductAccountingAccrualPeriodicAdvancedPaymentAllocation(false);
    }

    private Long createLoanProductAccountingAccrualPeriodicAdvancedPaymentAllocation(boolean isMultiDisburse) {
        String name = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
        String shortName = Utils.uniqueRandomStringGenerator("", 4);
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        Long resourceId = loanTransactionHelper.createLoanProduct(new PostLoanProductsRequest().name(name).shortName(shortName)
                .multiDisburseLoan(isMultiDisburse).maxTrancheCount(isMultiDisburse ? 2 : 1).interestType(isMultiDisburse ? 0 : 1)
                .interestCalculationPeriodType(isMultiDisburse ? 0 : 1).disallowExpectedDisbursements(isMultiDisburse)
                .description("Test loan description").currencyCode("USD").digitsAfterDecimal(2).daysInYearType(1).daysInMonthType(1)
                .recalculationRestFrequencyType(1).rescheduleStrategyMethod(1).loanScheduleType(LoanScheduleType.PROGRESSIVE.name())
                .recalculationRestFrequencyInterval(0).isInterestRecalculationEnabled(false).locale("en_GB").numberOfRepayments(4)
                .repaymentFrequencyType(2L).repaymentEvery(1).minPrincipal(100.0).principal(1000.0).maxPrincipal(10000000.0)
                .amortizationType(1).interestRatePerPeriod(0.0).interestRateFrequencyType(1).dateFormat("dd MMMM yyyy")
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY).paymentAllocation(List.of(defaultAllocation))
                .accountingRule(3).enableAccrualActivityPosting(true).fundSourceAccountId(fundSource.getAccountID().longValue())//
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
        LOG.info("Test Progressive Loan Product Id {} isMultiDisburse {} http://localhost:4200/#/products/loan-products/{1}/general",
                resourceId, isMultiDisburse);
        return resourceId;
    }

    private static List<PaymentAllocationOrder> getPaymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocationTypes).map(pat -> {
            PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
            paymentAllocationOrder.setPaymentAllocationRule(pat.name());
            paymentAllocationOrder.setOrder(integer.getAndIncrement());
            return paymentAllocationOrder;
        }).collect(Collectors.toList());
    }

    private static AdvancedPaymentData createDefaultPaymentAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("DEFAULT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_PRINCIPAL, PaymentAllocationType.PAST_DUE_INTEREST,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_PRINCIPAL,
                PaymentAllocationType.DUE_INTEREST, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private static Long applyForLoanApplicationAdvancedPaymentAllocation(final Long clientID, final Long loanProductID,
                                                                         BigDecimal principal, String applicationDisbursementDate) {
        final PostLoansRequest loanRequest = new PostLoansRequest() //
                .loanTermFrequency(4).locale("en_GB").loanTermFrequencyType(2).numberOfRepayments(4).repaymentFrequencyType(2)
                .repaymentEvery(1).principal(principal).amortizationType(1).interestType(0).interestRatePerPeriod(BigDecimal.ZERO)
                .interestCalculationPeriodType(1).dateFormat("dd MMMM yyyy")
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY).loanType("individual")
                .expectedDisbursementDate(applicationDisbursementDate).submittedOnDate(applicationDisbursementDate).clientId(clientID)
                .productId(loanProductID);
        Long loanId = loanTransactionHelper.applyLoan(loanRequest).getLoanId();
        LOG.info("Test Progressive Loan Id {2} http://localhost:4200/#/clients/{}/loans-accounts/{}/transactions", client.getClientId(),
                loanId);
        return loanId;
    }

    private static Long applyForLoanApplicationAdvancedPaymentAllocation(final Long clientID, final Long loanProductID,
                                                                         BigDecimal principal, String applicationDisbursementDate, String applicationDisbursementDate2) {
        final PostLoansRequest loanRequest = new PostLoansRequest() //
                .loanTermFrequency(4).locale("en_GB").loanTermFrequencyType(2).numberOfRepayments(4).repaymentFrequencyType(2)
                .repaymentEvery(1).principal(principal).amortizationType(1).interestType(0).interestRatePerPeriod(BigDecimal.ZERO)
                .interestCalculationPeriodType(0).dateFormat("dd MMMM yyyy")
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY).loanType("individual")
                .submittedOnDate(applicationDisbursementDate).clientId(clientID).expectedDisbursementDate(applicationDisbursementDate2)
                .productId(loanProductID);
        Long loanId = loanTransactionHelper.applyLoan(loanRequest).getLoanId();
        LOG.info("Test Progressive Loan Multi Disbursed Id {2} http://localhost:4200/#/clients/{}/loans-accounts/{}/transactions",
                client.getClientId(), loanId);
        return loanId;
    }

}
