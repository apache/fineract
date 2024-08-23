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

import static org.apache.fineract.integrationtests.BaseLoanIntegrationTest.InterestRateFrequencyType.YEARS;
import static org.apache.fineract.integrationtests.BaseLoanIntegrationTest.RepaymentFrequencyType.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanRefundTransactionTest extends BaseLoanIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanRefundTransactionTest.class);
    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private static BusinessDateHelper businessDateHelper;
    private static LoanTransactionHelper loanTransactionHelper;
    private static AccountHelper accountHelper;
    private static LoanRescheduleRequestHelper loanRescheduleRequestHelper;

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        businessDateHelper = new BusinessDateHelper();
        accountHelper = new AccountHelper(requestSpec, responseSpec);
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
        loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(requestSpec, responseSpec);
    }

    // UC1: (Internal case) Generate a totalInterestRefund using Advanced payment allocation with Interest Refund
    // options and Apply
    // Interest Refund transaction
    // 1. Create a Loan product with Adv. Pment. Alloc. and with Accrual and Interest Refund
    // 2. Submit, Approve and Disburse a Loan account
    // 3. Apply the INTEREST_REFUND transaction to validate the Journal Entries generated
    @Test
    public void uc1() {
        final String operationDate = "1 January 2024";
        AtomicLong createdLoanId = new AtomicLong();
        runAt(operationDate, () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .interestRatePerPeriod(108.0).interestCalculationPeriodType(RepaymentFrequencyType.DAYS)
                    .interestRateFrequencyType(YEARS).daysInMonthType(DaysInMonthType.ACTUAL.getValue())
                    .daysInYearType(DaysInYearType.DAYS_360.getValue()).numberOfRepayments(4)//
                    .maxInterestRatePerPeriod((double) 110)//
                    .repaymentEvery(1)//
                    .repaymentFrequencyType(1L)//
                    .allowPartialPeriodInterestCalcualtion(false)//
                    .multiDisburseLoan(false)//
                    .disallowExpectedDisbursements(null)//
                    .allowApprovedDisbursedAmountsOverApplied(null)//
                    .overAppliedCalculationType(null)//
                    .overAppliedNumber(null)//
                    .installmentAmountInMultiplesOf(null)//
            ;//
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductResponse.getResourceId(), operationDate, 1000.0, 4)
                    .interestRatePerPeriod(BigDecimal.valueOf(108.0));

            applicationRequest = applicationRequest.interestCalculationPeriodType(DAYS)
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY);

            PostLoansResponse loanResponse = loanTransactionHelper.applyLoan(applicationRequest);
            createdLoanId.set(loanResponse.getLoanId());

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000.0)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("1 January 2024").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2024").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            // After Disbursement we are expecting zero interest refund
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            assertEquals(BigDecimal.ZERO, loanDetails.getSummary().getTotalInterestRefund().stripTrailingZeros());
        });

        runAt("10 February 2024", () -> {
            // After Interest refund transaction we are expecting non zero interest refund
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(createdLoanId.get());
            LOG.info("value {}", loanDetails.getSummary().getTotalInterestRefund().stripTrailingZeros());
            assertEquals(BigDecimal.ZERO, loanDetails.getSummary().getTotalInterestRefund().stripTrailingZeros());

            final BigDecimal inteterestRefund = BigDecimal.valueOf(11);
            final Long loanTransactionId = loanTransactionHelper.applyInterestRefundLoanTransaction(requestSpec, responseSpec,
                    createdLoanId.get(), buildJsonBody(BigDecimal.valueOf(5.0), BigDecimal.valueOf(3.0), BigDecimal.valueOf(2.0),
                            BigDecimal.valueOf(1.0), null));

            loanDetails = loanTransactionHelper.getLoanDetails(createdLoanId.get());
            assertEquals(inteterestRefund, loanDetails.getSummary().getTotalInterestRefund().stripTrailingZeros());

            verifyTRJournalEntries(loanTransactionId, journalEntry(5.0, loansReceivableAccount, "CREDIT"),
                    journalEntry(3.0, interestReceivableAccount, "CREDIT"), journalEntry(2.0, feeReceivableAccount, "CREDIT"),
                    journalEntry(1.0, penaltyReceivableAccount, "CREDIT"), journalEntry(11.0, overpaymentAccount, "CREDIT"),
                    journalEntry(22.0, interestIncomeAccount, "DEBIT"));
        });
    }

    // UC2: (Internal case) Generate a totalInterestRefund using Advanced payment allocation with Interest Refund
    // options and Apply
    // Interest Refund transaction when the Loan Account is ChargeBack
    // 1. Create a Loan product with Adv. Pment. Alloc. and with Accrual and Interest Refund
    // 2. Submit, Approve and Disburse a Loan account
    // 3. Apply the MERCHANT_ISSUED_REFUND transaction to validate totalInterestRefund different than Zero
    // 4- Apply the INTEREST_REFUND transaction to validate the Journal Entries generated
    @Test
    public void uc2() {
        final String operationDate = "1 January 2024";
        AtomicLong createdLoanId = new AtomicLong();
        runAt(operationDate, () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .interestRatePerPeriod(108.0).interestCalculationPeriodType(RepaymentFrequencyType.DAYS)
                    .interestRateFrequencyType(YEARS).daysInMonthType(DaysInMonthType.ACTUAL.getValue())
                    .daysInYearType(DaysInYearType.DAYS_360.getValue()).numberOfRepayments(4)//
                    .maxInterestRatePerPeriod((double) 110)//
                    .repaymentEvery(1)//
                    .repaymentFrequencyType(1L)//
                    .allowPartialPeriodInterestCalcualtion(false)//
                    .multiDisburseLoan(false)//
                    .disallowExpectedDisbursements(null)//
                    .allowApprovedDisbursedAmountsOverApplied(null)//
                    .overAppliedCalculationType(null)//
                    .overAppliedNumber(null)//
                    .installmentAmountInMultiplesOf(null)//
            ;//
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductResponse.getResourceId(), operationDate, 1000.0, 4)
                    .interestRatePerPeriod(BigDecimal.valueOf(108.0));

            applicationRequest = applicationRequest.interestCalculationPeriodType(DAYS)
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY);

            PostLoansResponse loanResponse = loanTransactionHelper.applyLoan(applicationRequest);
            createdLoanId.set(loanResponse.getLoanId());

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000.0)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("1 January 2024").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2024").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            // After Disbursement we are expecting zero interest refund
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            assertEquals(BigDecimal.ZERO, loanDetails.getSummary().getTotalInterestRefund().stripTrailingZeros());
        });

        runAt("1 February 2024", () -> {
            // After Interest refund transaction we are expecting non zero interest refund
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(createdLoanId.get());
            LOG.info("value {}", loanDetails.getSummary().getTotalInterestRefund().stripTrailingZeros());
            assertEquals(BigDecimal.ZERO, loanDetails.getSummary().getTotalInterestRefund().stripTrailingZeros());

            Long repayment1TransactionId = addRepaymentForLoan(createdLoanId.get(), 250.0, "1 February 2024");
            chargeOffLoan(createdLoanId.get(), "1 February 2024");
        });

        runAt("10 February 2024", () -> {
            // After Interest refund transaction we are expecting non zero interest refund
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(createdLoanId.get());
            LOG.info("value {}", loanDetails.getSummary().getTotalInterestRefund().stripTrailingZeros());
            LOG.info("chargedOffOnDate {}", loanDetails.getTimeline().getChargedOffOnDate());
            assertEquals(BigDecimal.ZERO, loanDetails.getSummary().getTotalInterestRefund().stripTrailingZeros());

            final BigDecimal inteterestRefund = BigDecimal.valueOf(11);
            final Long loanTransactionId = loanTransactionHelper.applyInterestRefundLoanTransaction(requestSpec, responseSpec,
                    createdLoanId.get(), buildJsonBody(BigDecimal.valueOf(5.0), BigDecimal.valueOf(3.0), BigDecimal.valueOf(2.0),
                            BigDecimal.valueOf(1.0), null));

            loanDetails = loanTransactionHelper.getLoanDetails(createdLoanId.get());
            assertEquals(inteterestRefund, loanDetails.getSummary().getTotalInterestRefund().stripTrailingZeros());

            verifyTRJournalEntries(loanTransactionId, journalEntry(11.0, interestIncomeChargeOffAccount, "CREDIT"),
                    journalEntry(11.0, overpaymentAccount, "CREDIT"), journalEntry(22.0, interestIncomeAccount, "DEBIT"));
        });
    }

    private String buildJsonBody(final BigDecimal principal, final BigDecimal interest, final BigDecimal feeCharges,
            final BigDecimal penaltyCharges, final BigDecimal overpayment) {
        final HashMap<String, BigDecimal> map = new HashMap<>();
        map.put("principal", principal);
        map.put("interest", interest);
        map.put("feeCharges", feeCharges);
        map.put("penaltyCharges", penaltyCharges);
        if (overpayment != null) {
            map.put("overpayment", overpayment);
        }

        return new Gson().toJson(map);
    }

}
