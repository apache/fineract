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
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.models.PostFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
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
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanInterestRefundChargePaymentTest extends FeignLoanTestBase {

    private static FeignSavingsHelper savingsHelper;
    private static FeignSavingsProductHelper savingsProductHelper;
    private static FeignSavingsTransactionHelper savingsTransactionHelper;
    private static FinancialActivityAccountHelper financialActivityAccountHelper;

    @BeforeAll
    public static void setupSavingsHelpers() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        savingsHelper = new FeignSavingsHelper(client);
        savingsProductHelper = new FeignSavingsProductHelper(client);
        savingsTransactionHelper = new FeignSavingsTransactionHelper(client);
        financialActivityAccountHelper = new FinancialActivityAccountHelper(null);
    }

    @AfterAll
    public static void tearDownFinancialActivityAccounts() {
        financialActivityAccountHelper.getAllFinancialActivityAccounts()
                .forEach(mapping -> financialActivityAccountHelper.deleteFinancialActivityAccount(mapping.getId()));
    }

    @Test
    public void verifyInterestRefundCreatedForMerchantIssuedRefundWithChargePayment() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        AtomicReference<Long> loanChargeIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            Long clientId = createClient();

            Long savingsProductId = savingsProductHelper.createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct())
                    .getResourceId();
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
                    transaction(5.63, "Interest Refund", "22 January 2021") //
            );
        });
    }

    private void mapLiabilityTransferFinancialActivity() {
        financialActivityAccountHelper.getAllFinancialActivityAccounts()
                .forEach(mapping -> financialActivityAccountHelper.deleteFinancialActivityAccount(mapping.getId()));
        PostFinancialActivityAccountsResponse response = financialActivityAccountHelper
                .createFinancialActivityAccount(new PostFinancialActivityAccountsRequest()
                        .financialActivityId((long) LIABILITY_TRANSFER.getValue()).glAccountId(getLiabilityAccountId("overpayment")));
        Assertions.assertNotNull(response.getResourceId());
    }
}
