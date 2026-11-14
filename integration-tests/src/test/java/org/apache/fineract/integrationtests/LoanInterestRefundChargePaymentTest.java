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

import static org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity.LIABILITY_TRANSFER;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.models.PostFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInMonthType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInYearType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.RecalculationRestFrequencyType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.SupportedInterestRefundTypesItem;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanInterestRefundChargePaymentTest extends FeignLoanTestBase {

    private static FeignSavingsHelper savingsHelper;
    private static FeignSavingsProductHelper savingsProductHelper;
    private static FeignSavingsTransactionHelper savingsTransactionHelper;
    private static FeignFinancialActivityAccountHelper financialActivityAccountHelper;

    @BeforeAll
    public static void setupSavingsHelpers() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        savingsHelper = new FeignSavingsHelper(client);
        savingsProductHelper = new FeignSavingsProductHelper(client);
        savingsTransactionHelper = new FeignSavingsTransactionHelper(client);
        financialActivityAccountHelper = new FeignFinancialActivityAccountHelper(client);
    }

    @AfterAll
    public static void tearDownFinancialActivityAccounts() {
        financialActivityAccountHelper.getAllMappings().forEach(mapping -> financialActivityAccountHelper.deleteMapping(mapping.getId()));
    }

    @Test
    public void verifyInterestRefundCreatedForMerchantIssuedRefundWithChargePayment() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        AtomicReference<Long> loanChargeIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            Long clientId = createClient();

            Long savingsProductId = savingsProductHelper
                    .createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().currencyCode("EUR")).getResourceId();
            Long savingsId = savingsHelper.createApproveActivateSavings(clientId, savingsProductId, "1 January 2021");
            savingsTransactionHelper.deposit(savingsId, "10000", "1 January 2021");

            Long loanProduct = createLoanProduct(create4IProgressive() //
                    .daysInMonthType(DaysInMonthType.ACTUAL) //
                    .daysInYearType(DaysInYearType.ACTUAL) //
                    .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProduct, "1 January 2021", 1000.0, 9.99, 12,
                    request -> request.linkAccountId(savingsId));
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");

            mapLiabilityTransferFinancialActivity();

            Long chargeId = chargesHelper
                    .createCharge(ChargeRequestBuilders.loanSpecifiedDueDateAccountTransferFee(50.0, false).currencyCode("EUR"))
                    .getResourceId();
            Long loanChargeId = addLoanCharge(loanId, chargeId, "1 February 2021", 50.0).getResourceId();
            loanChargeIdRef.set(loanChargeId);
        });
        runAt("10 January 2021", () -> {
            Long loanId = loanIdRef.get();
            payLoanCharge(loanId, loanChargeIdRef.get(),
                    new PostLoansLoanIdChargesChargeIdRequest().transactionDate("10 January 2021").dateFormat("dd MMMM yyyy").locale("en"));
        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = makeLoanRepayment(loanId, "MerchantIssuedRefund",
                    "22 January 2021", 1000.0);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(50.0, "Charge Payment", "10 January 2021"), //
                    transaction(1000.0, "Merchant Issued Refund", "22 January 2021"), //
                    transaction(5.75, "Interest Refund", "22 January 2021"), //
                    transaction(55.75, "Accrual", "22 January 2021") //
            );

            GetLoansLoanIdResponse loanDetails = loanHelper.getLoanDetails(loanId);
            Assertions.assertTrue(loanDetails.getStatus().getClosedObligationsMet());
            Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(loanDetails.getSummary().getTotalOutstanding()));
        });
    }

    @Test
    public void verifyNoInterestRefundIsCreatedWhenInterestRecalculationIsDisabled() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            Long clientId = createClient();

            Long loanProduct = createLoanProduct(create4IProgressive() //
                    .daysInMonthType(DaysInMonthType.ACTUAL) //
                    .daysInYearType(DaysInYearType.ACTUAL) //
                    .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
                    .isInterestRecalculationEnabled(false) //
                    .interestRecalculationCompoundingMethod(null) //
                    .rescheduleStrategyMethod(null) //
                    .recalculationRestFrequencyType(null) //
                    .recalculationRestFrequencyInterval(null) //
            );
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProduct, "1 January 2021", 1000.0, 9.99, 12, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");
        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse response = makeLoanRepayment(loanId, "MerchantIssuedRefund", "22 January 2021", 1000.0);
            Assertions.assertNotNull(response);
            Assertions.assertNotNull(response.getResourceId());

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(1000.0, "Merchant Issued Refund", "22 January 2021") //
            );
        });
    }

    /**
     * Same scenario as above, but on a product with a 25% down payment enabled. The down-payment installment is sized
     * off the full disbursement amount, independently of how much of the amortizable principal a later merchant-issued
     * refund removes - otherwise the excess would flow through the in-advance allocation order and eat a fee a real
     * charge payment already covered, distorting the refund (see FINERACT-3014).
     */
    @Test
    public void verifyInterestRefundWithDownPaymentAndPartialRefund() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> loanIdRef.set(createDownPaymentLoan(false).loanId()));
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse response = makeLoanRepayment(loanId, "MerchantIssuedRefund", "22 January 2021", 400.0);
            Assertions.assertNotNull(response);
            Assertions.assertNotNull(response.getResourceId());

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(250.0, "Down Payment", "01 January 2021"), //
                    transaction(400.0, "Merchant Issued Refund", "22 January 2021"), //
                    transaction(2.30, "Interest Refund", "22 January 2021") //
            );

            GetLoansLoanIdResponse loanDetails = loanHelper.getLoanDetails(loanId);
            Assertions.assertFalse(loanDetails.getStatus().getClosedObligationsMet());
        });
    }

    /**
     * Same as above, but the fee is paid separately through a charge payment on 10 January: a fee paid separately must
     * not change the interest refund.
     */
    @Test
    public void verifyInterestRefundWithDownPaymentAndPartialRefundAndChargePayment() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        AtomicReference<Long> loanChargeIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            LoanSetup setup = createDownPaymentLoan(true);
            loanIdRef.set(setup.loanId());
            loanChargeIdRef.set(setup.loanChargeId());
        });
        runAt("10 January 2021", () -> payLoanCharge(loanIdRef.get(), loanChargeIdRef.get(),
                new PostLoansLoanIdChargesChargeIdRequest().transactionDate("10 January 2021").dateFormat("dd MMMM yyyy").locale("en")));
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse response = makeLoanRepayment(loanId, "MerchantIssuedRefund", "22 January 2021", 400.0);
            Assertions.assertNotNull(response);
            Assertions.assertNotNull(response.getResourceId());

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(250.0, "Down Payment", "01 January 2021"), //
                    transaction(50.0, "Charge Payment", "10 January 2021"), //
                    transaction(400.0, "Merchant Issued Refund", "22 January 2021"), //
                    transaction(2.30, "Interest Refund", "22 January 2021") //
            );

            GetLoansLoanIdResponse loanDetails = loanHelper.getLoanDetails(loanId);
            Assertions.assertFalse(loanDetails.getStatus().getClosedObligationsMet());
        });
    }

    /**
     * A full refund of the remaining amortizable principal must close the loan and post the closure accrual, exactly
     * like the no-down-payment scenario above.
     */
    @Test
    public void verifyInterestRefundWithDownPaymentAndFullRefund() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> loanIdRef.set(createDownPaymentLoan(false).loanId()));
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse response = makeLoanRepayment(loanId, "MerchantIssuedRefund", "22 January 2021", 750.0);
            Assertions.assertNotNull(response);
            Assertions.assertNotNull(response.getResourceId());

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(250.0, "Down Payment", "01 January 2021"), //
                    transaction(750.0, "Merchant Issued Refund", "22 January 2021"), //
                    transaction(4.31, "Interest Refund", "22 January 2021"), //
                    transaction(4.31, "Accrual", "22 January 2021") //
            );

            GetLoansLoanIdResponse loanDetails = loanHelper.getLoanDetails(loanId);
            Assertions.assertTrue(loanDetails.getStatus().getClosedObligationsMet());
            Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(loanDetails.getSummary().getTotalOutstanding()));
        });
    }

    /**
     * Same as above, but with the fee paid separately on 10 January. Before the down-payment fix this left 0.12 of
     * principal outstanding and the loan never closed (see the PR review).
     */
    @Test
    public void verifyInterestRefundWithDownPaymentAndFullRefundAndChargePayment() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        AtomicReference<Long> loanChargeIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            LoanSetup setup = createDownPaymentLoan(true);
            loanIdRef.set(setup.loanId());
            loanChargeIdRef.set(setup.loanChargeId());
        });
        runAt("10 January 2021", () -> payLoanCharge(loanIdRef.get(), loanChargeIdRef.get(),
                new PostLoansLoanIdChargesChargeIdRequest().transactionDate("10 January 2021").dateFormat("dd MMMM yyyy").locale("en")));
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse response = makeLoanRepayment(loanId, "MerchantIssuedRefund", "22 January 2021", 750.0);
            Assertions.assertNotNull(response);
            Assertions.assertNotNull(response.getResourceId());

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(250.0, "Down Payment", "01 January 2021"), //
                    transaction(50.0, "Charge Payment", "10 January 2021"), //
                    transaction(750.0, "Merchant Issued Refund", "22 January 2021"), //
                    transaction(4.31, "Interest Refund", "22 January 2021"), //
                    transaction(54.31, "Accrual", "22 January 2021") //
            );

            GetLoansLoanIdResponse loanDetails = loanHelper.getLoanDetails(loanId);
            Assertions.assertTrue(loanDetails.getStatus().getClosedObligationsMet());
            Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(loanDetails.getSummary().getTotalOutstanding()));
        });
    }

    private record LoanSetup(Long loanId, Long loanChargeId) {
    }

    private LoanSetup createDownPaymentLoan(boolean withChargePayment) {
        Long clientId = createClient();

        Long savingsId = null;
        if (withChargePayment) {
            Long savingsProductId = savingsProductHelper
                    .createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().currencyCode("EUR")).getResourceId();
            savingsId = savingsHelper.createApproveActivateSavings(clientId, savingsProductId, "1 January 2021");
            savingsTransactionHelper.deposit(savingsId, "10000", "1 January 2021");
        }

        Long loanProduct = createLoanProduct(create4IProgressive() //
                .daysInMonthType(DaysInMonthType.ACTUAL) //
                .daysInYearType(DaysInYearType.ACTUAL) //
                .enableDownPayment(true) //
                .disbursedAmountPercentageForDownPayment(BigDecimal.valueOf(25)) //
                .enableAutoRepaymentForDownPayment(true) //
                .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
                .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
        );
        final Long savingsIdForCustomizer = savingsId;
        Consumer<PostLoansRequest> customizer = savingsIdForCustomizer == null ? null
                : request -> request.linkAccountId(savingsIdForCustomizer);
        Long loanId = applyAndApproveProgressiveLoan(clientId, loanProduct, "1 January 2021", 1000.0, 9.99, 12, customizer);
        disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");

        Long loanChargeId = null;
        if (withChargePayment) {
            mapLiabilityTransferFinancialActivity();
            Long chargeId = chargesHelper
                    .createCharge(ChargeRequestBuilders.loanSpecifiedDueDateAccountTransferFee(50.0, false).currencyCode("EUR"))
                    .getResourceId();
            loanChargeId = addLoanCharge(loanId, chargeId, "1 February 2021", 50.0).getResourceId();
        }
        return new LoanSetup(loanId, loanChargeId);
    }

    private void mapLiabilityTransferFinancialActivity() {
        financialActivityAccountHelper.getAllMappings().forEach(mapping -> financialActivityAccountHelper.deleteMapping(mapping.getId()));
        PostFinancialActivityAccountsResponse response = financialActivityAccountHelper
                .createMapping(new PostFinancialActivityAccountsRequest().financialActivityId((long) LIABILITY_TRANSFER.getValue())
                        .glAccountId(getLiabilityAccountId("overpayment")));
        Assertions.assertNotNull(response.getResourceId());
    }
}
