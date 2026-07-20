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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.UUID;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Test;

public class LoanAccountPaymentAllocationWithOverlappingDownPaymentInstallmentTest extends FeignLoanTestBase {

    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    @Test
    public void loanAccountWithEnableDownPaymentAndEnableAutoRepaymentForDownPaymentWithOverlappingInstallmentPaymentAllocationTest() {
        runAt("03 March 2023", () -> {

            // Test with
            // Enable Down Payment
            // Enable Auto Repayment For Down Payment
            // Payment Strategy DEFAULT payment allocation strategy "Penalties, Fees, Interest, Principal order"
            // Overlapping down payment and regular installment

            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = true;

            final Long clientId = createClient();

            // Loan Product creation with down-payment configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithEnableDownPaymentAndMultipleDisbursements(
                    enableDownPayment, "25", enableAutoRepaymentForDownPayment);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            // create loan account with DEFAULT payment allocation strategy "Penalties, Fees, Interest, Principal order"

            final Long loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr, LoanProductTestBuilder.DEFAULT_STRATEGY);

            // add charge PENALTY with due date as overlapping installment due date

            LocalDate targetDate = LocalDate.of(2023, 4, 3);
            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);

            Long penalty1LoanChargeId = addCharge(loanId, true, 10.0, penaltyCharge1AddedDate);

            assertNotNull(penalty1LoanChargeId);

            // add charge FEE with due date as overlapping installment due date
            Long fee = createLoanSpecifiedDueDateCharge(10.0);

            targetDate = LocalDate.of(2023, 4, 3);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);

            Long feeLoanChargeId = addLoanCharge(loanId, fee, feeCharge1AddedDate, 5.15).getResourceId();

            assertNotNull(feeLoanChargeId);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // first disbursement
            disburseLoanWithAmount(loanId, "03 March 2023", 500.0);

            // verify down-payment transaction created
            checkDownPaymentTransaction(disbursementDate, 125.0f, 0.0f, 0.0f, 0.0f, loanId);

            // verify loan schedule

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 500.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 125.0, 125.0, 125.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // regular installment [2]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 2, 187.5, 0.0, 0.0, 202.65, 5.15, 0.0, 10.0, 0.0,
                    false, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 3, 187.5, 0.0, 0.0, 187.5, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // second disbursement with overlapping installment i.e same due date as regular repayment due date

            disbursementDate = LocalDate.of(2023, 4, 3);
            updateBusinessDate("03 April 2023");
            disburseLoanWithAmount(loanId, "03 April 2023", 1000.0);

            checkDownPaymentTransaction(disbursementDate, 250.0f, 0.0f, 0.0f, 0.0f, loanId);

            // verify loan schedule

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(6, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 500.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 125.0, 125.0, 125.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // disbursement period [2]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(2), LocalDate.of(2023, 4, 3), 1000.0);
            // down payment period [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 2, 250.0, 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 3, 562.5, 0.0, 0.0, 577.65, 5.15, 0.0, 10.0, 0.0,
                    false, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 4, 562.5, 0.0, 0.0, 562.5, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // make repayment for fully paying and verify that regular installment gets fully paid on 3rd april

            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(577.65));

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(6, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 500.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 125.0, 125.0, 125.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // disbursement period [2]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(2), LocalDate.of(2023, 4, 3), 1000.0);
            // down payment period [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 2, 250.0, 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 3, 562.5, 577.65, 562.5, 0.0, 5.15, 5.15, 10.0,
                    10.0, true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 4, 562.5, 0.0, 0.0, 562.5, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

        });
    }

    @Test
    public void loanAccountWithEnableDownPaymentAndDisableAutoRepaymentForDownPaymentWithOverlappingInstallmentPaymentAllocationTest() {
        runAt("03 March 2023", () -> {

            // Test with
            // Enable Down Payment
            // Disable Auto Repayment For Down Payment
            // Payment Strategy INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY =
            // "interest-principal-penalties-fees-order-strategy"
            // Overlapping down payment and regular installment

            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Long clientId = createClient();

            // Loan Product creation with down-payment configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithEnableDownPaymentAndMultipleDisbursements(
                    enableDownPayment, "25", enableAutoRepaymentForDownPayment);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            // create loan account with "interest-principal-penalties-fees-order-strategy"

            final Long loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr, LoanProductTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY);

            // add charge PENALTY with due date as overlapping installment due date

            LocalDate targetDate = LocalDate.of(2023, 4, 3);
            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);

            Long penalty1LoanChargeId = addCharge(loanId, true, 10.0, penaltyCharge1AddedDate);

            assertNotNull(penalty1LoanChargeId);

            // add charge FEE with due date as overlapping installment due date
            Long fee = createLoanSpecifiedDueDateCharge(10.0);

            targetDate = LocalDate.of(2023, 4, 3);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);

            Long feeLoanChargeId = addLoanCharge(loanId, fee, feeCharge1AddedDate, 5.15).getResourceId();

            assertNotNull(feeLoanChargeId);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // first disbursement
            disburseLoanWithAmount(loanId, "03 March 2023", 500.0);

            // make repayment on 3rd March
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 March 2023").locale("en")
                            .transactionAmount(125.0));

            // verify loan schedule

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 500.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 125.0, 125.0, 125.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // regular installment [2]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 2, 187.5, 0.0, 0.0, 202.65, 5.15, 0.0, 10.0, 0.0,
                    false, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 3, 187.5, 0.0, 0.0, 187.5, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // second disbursement with overlapping installment i.e same due date as regular repayment due date

            disbursementDate = LocalDate.of(2023, 4, 3);
            updateBusinessDate("03 April 2023");
            disburseLoanWithAmount(loanId, "03 April 2023", 1000.0);

            // make repayment on 3rd April
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(250.0));

            // verify loan schedule

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(6, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 500.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 125.0, 125.0, 125.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // disbursement period [2]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(2), LocalDate.of(2023, 4, 3), 1000.0);
            // down payment period [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 2, 250.0, 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 3, 562.5, 0.0, 0.0, 577.65, 5.15, 0.0, 10.0, 0.0,
                    false, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 4, 562.5, 0.0, 0.0, 562.5, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // make repayment for fully paying and verify that regular installment gets fully paid on 3rd april

            final PostLoansLoanIdTransactionsResponse repaymentTransaction_2 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(577.65));

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(6, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 500.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 125.0, 125.0, 125.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // disbursement period [2]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(2), LocalDate.of(2023, 4, 3), 1000.0);
            // down payment period [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 2, 250.0, 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 3, 562.5, 577.65, 562.5, 0.0, 5.15, 5.15, 10.0,
                    10.0, true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 4, 562.5, 0.0, 0.0, 562.5, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

        });
    }

    @Test
    public void loanAccountWithEnableDownPaymentAndDisableAutoRepaymentForDownPaymentWithOverlappingInstallmentForMultipleDisbursementsOnSameDayTest() {
        runAt("03 March 2023", () -> {

            // Test with
            // Enable Down Payment
            // Disable Auto Repayment For Down Payment
            // Overlapping down payment and regular installment with multiple disbursements on same day

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Long clientId = createClient();

            // Loan Product creation with down-payment configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithEnableDownPaymentAndMultipleDisbursements(
                    enableDownPayment, "25", enableAutoRepaymentForDownPayment);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            // create loan account

            final Long loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr, LoanProductTestBuilder.DEFAULT_STRATEGY);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // first disbursement
            disburseLoanWithAmount(loanId, "03 March 2023", 200.0);

            // make repayment on 3rd March
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 March 2023").locale("en")
                            .transactionAmount(50.0));

            // verify loan schedule

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // regular installment [2]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 2, 75.0, 0.0, 0.0, 75.0, 0.0, 0.0, 0.0, 0.0, false,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 3, 75.0, 0.0, 0.0, 75.0, 0.0, 0.0, 0.0, 0.0, false,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // second disbursement with overlapping installment i.e same due date as regular repayment due date

            updateBusinessDate("03 April 2023");
            disburseLoanWithAmount(loanId, "03 April 2023", 200.0);

            // make repayment on 3rd April
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(50.0));

            // verify loan schedule

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(6, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // disbursement period [2]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(2), LocalDate.of(2023, 4, 3), 200.0);
            // down payment period [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 2, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 3, 150.0, 0.0, 0.0, 150.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 4, 150.0, 0.0, 0.0, 150.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // same day third disbursement with overlapping installment i.e same due date as regular repayment due date
            // 3-April
            disburseLoanWithAmount(loanId, "03 April 2023", 200.0);

            // make repayment on 3rd April
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_2 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(50.0));

            // verify loan schedule

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(8, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // disbursement period [2]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(2), LocalDate.of(2023, 4, 3), 200.0);
            // disbursement period [3]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(3), LocalDate.of(2023, 4, 3), 200.0);
            // down payment period [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 2, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // down payment period [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 3, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [6]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(6), 4, 225.0, 0.0, 0.0, 225.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [7]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(7), 5, 225.0, 0.0, 0.0, 225.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // make repayment for fully paying and verify that regular installment gets fully paid on 3rd april
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_3 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(225.0));

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(8, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // disbursement period [2]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(2), LocalDate.of(2023, 4, 3), 200.0);
            // disbursement period [3]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(3), LocalDate.of(2023, 4, 3), 200.0);
            // down payment period [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 2, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // down payment period [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 3, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [6]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(6), 4, 225.0, 225.0, 225.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [7]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(7), 5, 225.0, 0.0, 0.0, 225.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

        });
    }

    @Test
    public void loanAccountWithEnableDownPaymentWithAdvancedPaymentAllocationWithProgressiveScheduleGenerationMultipleDisbursementsOnSameDayTest() {
        runAt("03 March 2023", () -> {

            // Test with
            // Enable Down Payment
            // Disable Auto Repayment For Down Payment
            // Overlapping down payment and regular installment with multiple disbursements on same day
            // Progressive Schedule generation with Advanced Payment Allocation

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Long clientId = createClient();

            String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
            AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

            // Loan Product creation with down-payment configuration and progressive schedule
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithAdvancedPaymentStrategyAndProgressiveLoanSchedule(
                    enableDownPayment, "25", enableAutoRepaymentForDownPayment, defaultAllocation);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            // create loan account

            final Long loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr, "advanced-payment-allocation-strategy");

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // first disbursement
            disburseLoanWithAmount(loanId, "03 March 2023", 200.0);

            // make repayment on 3rd March
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 March 2023").locale("en")
                            .transactionAmount(50.0));

            // verify loan schedule

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // regular installment [2]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 2, 75.0, 0.0, 0.0, 75.0, 0.0, 0.0, 0.0, 0.0, false,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 3, 75.0, 0.0, 0.0, 75.0, 0.0, 0.0, 0.0, 0.0, false,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // second disbursement with overlapping installment i.e same due date as regular repayment due date

            updateBusinessDate("03 April 2023");
            disburseLoanWithAmount(loanId, "03 April 2023", 200.0);

            // make repayment on 3rd April
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(50.0));

            // verify loan schedule

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(6, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // regular installment [2]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 2, 75.0, 50.0, 50.0, 25.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // disbursement period [3]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(3), LocalDate.of(2023, 4, 3), 200.0);
            // down payment period [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 3, 50.0, 0.0, 0.0, 50.0, 0.0, 0.0, 0.0, 0.0, false,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 4, 225.0, 0.0, 0.0, 225.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // same day third disbursement with overlapping installment i.e same due date as regular repayment due date
            // 3-April
            disburseLoanWithAmount(loanId, "03 April 2023", 200.0);

            // make repayment on 3rd April
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_2 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(50.0));

            // verify loan schedule

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(8, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // regular installment [2]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 2, 75.0, 75.0, 75.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // disbursement period [3]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(3), LocalDate.of(2023, 4, 3), 200.0);
            // disbursement period [4]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(4), LocalDate.of(2023, 4, 3), 200.0);
            // down payment period [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 3, 50.0, 25.0, 25.0, 25.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // down payment period [6]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(6), 4, 50.0, 0.0, 0.0, 50.0, 0.0, 0.0, 0.0, 0.0, false,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [7]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(7), 5, 375.0, 0.0, 0.0, 375.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // make repayment for fully paying and verify that regular installment gets fully paid on 3rd april
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_3 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(225.0));

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(8, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // regular installment [2]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 2, 75.0, 75.0, 75.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // disbursement period [3]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(3), LocalDate.of(2023, 4, 3), 200.0);
            // disbursement period [4]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(4), LocalDate.of(2023, 4, 3), 200.0);
            // down payment period [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 3, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // down payment period [6]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(6), 4, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [7]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(7), 5, 375.0, 150.0, 150.0, 225.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

        });
    }

    private void verifyDisbursementPeriod(GetLoansLoanIdRepaymentPeriod period, LocalDate disbursementDate, double disbursedAmount) {
        assertEquals(disbursementDate, period.getDueDate());
        assertEquals(disbursedAmount, Utils.getDoubleValue(period.getPrincipalLoanBalanceOutstanding()));
    }

    private void verifyPeriodDetails(GetLoansLoanIdRepaymentPeriod period, Integer periodNumber, double periodAmount,
            double periodAmountPaid, double principalPaid, double outstandingAmount, double feeDue, double feePaid, double penaltyDue,
            double penaltyPaid, boolean isComplete, LocalDate periodFromDate, LocalDate periodDueDate) {
        assertEquals(periodNumber, period.getPeriod());
        assertEquals(periodFromDate, period.getFromDate());
        assertEquals(periodDueDate, period.getDueDate());
        assertEquals(periodAmount, Utils.getDoubleValue(period.getTotalInstallmentAmountForPeriod()));
        assertEquals(periodAmountPaid, Utils.getDoubleValue(period.getTotalPaidForPeriod()));
        assertEquals(principalPaid, Utils.getDoubleValue(period.getPrincipalPaid()));
        assertEquals(outstandingAmount, Utils.getDoubleValue(period.getTotalOutstandingForPeriod()));
        assertEquals(feeDue, Utils.getDoubleValue(period.getFeeChargesDue()));
        assertEquals(feePaid, Utils.getDoubleValue(period.getFeeChargesPaid()));
        assertEquals(penaltyDue, Utils.getDoubleValue(period.getPenaltyChargesDue()));
        assertEquals(penaltyPaid, Utils.getDoubleValue(period.getPenaltyChargesPaid()));
        assertEquals(isComplete, period.getComplete());
    }

    private Long createLoanAccountMultipleRepaymentsDisbursement(final Long clientId, final Long loanProductId, final String externalId,
            final String repaymentStrategy) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("2")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("2").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsDecliningBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 March 2023").withSubmittedOnDate("03 March 2023").withLoanType("individual")
                .withExternalId(externalId).withRepaymentStrategy(repaymentStrategy)
                .build(clientId.toString(), loanProductId.toString(), null);

        final Long loanId = applyForLoanFromJson(loanApplicationJSON);
        approveLoan(loanId, approveLoanRequest(1000.0, "03 March 2023"));
        return loanId;
    }

    private GetLoanProductsProductIdResponse createLoanProductWithEnableDownPaymentAndMultipleDisbursements(Boolean enableDownPayment,
            String disbursedAmountPercentageForDownPayment, boolean enableAutoRepaymentForDownPayment) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("2").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .build(null);
        final Long loanProductId = createLoanProductFromJson(loanProductJSON);
        return retrieveLoanProduct(loanProductId);
    }

    private GetLoanProductsProductIdResponse createLoanProductWithAdvancedPaymentStrategyAndProgressiveLoanSchedule(
            Boolean enableDownPayment, String disbursedAmountPercentageForDownPayment, boolean enableAutoRepaymentForDownPayment,
            AdvancedPaymentData... advancedPaymentData) {

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("2").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .addAdvancedPaymentAllocation(advancedPaymentData).withLoanScheduleType(LoanScheduleType.PROGRESSIVE).build(null);
        final Long loanProductId = createLoanProductFromJson(loanProductJSON);
        return retrieveLoanProduct(loanProductId);
    }

    private void checkDownPaymentTransaction(final LocalDate transactionDate, final Float principalPortion, final Float interestPortion,
            final Float feePortion, final Float penaltyPortion, final Long loanId) {
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails.getTransactions());
        boolean isTransactionFound = false;
        for (GetLoansLoanIdTransactions transaction : loanDetails.getTransactions()) {
            if ("Down Payment".equals(transaction.getType().getValue())) {
                if (DateUtils.isEqual(transactionDate, transaction.getDate())) {
                    isTransactionFound = true;
                    assertEquals(principalPortion, Float.valueOf(String.valueOf(transaction.getPrincipalPortion())),
                            "Mismatch in transaction amounts");
                    assertEquals(interestPortion, Float.valueOf(String.valueOf(transaction.getInterestPortion())),
                            "Mismatch in transaction amounts");
                    assertEquals(feePortion, Float.valueOf(String.valueOf(transaction.getFeeChargesPortion())),
                            "Mismatch in transaction amounts");
                    assertEquals(penaltyPortion, Float.valueOf(String.valueOf(transaction.getPenaltyChargesPortion())),
                            "Mismatch in transaction amounts");
                    break;
                }
            }
        }
        assertTrue(isTransactionFound, "No Down Payment entries are posted");
    }
}
