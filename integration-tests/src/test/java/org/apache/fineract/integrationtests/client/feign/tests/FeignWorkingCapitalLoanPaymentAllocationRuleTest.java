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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetBalance;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostPaymentAllocation;
import org.apache.fineract.client.models.WorkingCapitalLoanChargeData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FeignWorkingCapitalLoanPaymentAllocationRuleTest extends FeignIntegrationTest {

    // Fee/penalty-before-principal — the product-level DEFAULT_PAYMENT_ALLOCATION_TYPES order.
    private static final List<String> FEE_BEFORE_PRINCIPAL_ORDER = List.of("DUE_PENALTY", "DUE_FEE", "DUE_PRINCIPAL", "IN_ADVANCE_PENALTY",
            "IN_ADVANCE_FEE", "IN_ADVANCE_PRINCIPAL");
    // Principal-before-fee/penalty — a custom order only configured for GOODWILL_CREDIT.
    private static final List<String> PRINCIPAL_BEFORE_FEE_ORDER = List.of("DUE_PRINCIPAL", "DUE_PENALTY", "DUE_FEE",
            "IN_ADVANCE_PRINCIPAL", "IN_ADVANCE_PENALTY", "IN_ADVANCE_FEE");

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private WorkingCapitalLoanProductHelper productHelper;

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(fineractClient());
        clientHelper = new FeignClientHelper(fineractClient());
        businessDateHelper = new FeignBusinessDateHelper(fineractClient());
        productHelper = new WorkingCapitalLoanProductHelper();
    }

    @AfterAll
    void cleanupEntities() {
        for (final Long loanId : createdLoanIds) {
            if (loanId == null) {
                continue;
            }
            try {
                wcLoanHelper.undoDisbursal(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
            try {
                wcLoanHelper.undoApproval(loanId, WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveRequest());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
            try {
                wcLoanHelper.delete(loanId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdLoanIds.clear();
        for (final Long productId : createdProductIds) {
            if (productId == null) {
                continue;
            }
            try {
                productHelper.deleteWorkingCapitalLoanProductById(productId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdProductIds.clear();
    }

    @Test
    void testGoodwillCreditUsesItsOwnConfiguredAllocationOrderInsteadOfRepaymentOrder() {
        final Long[] loanIdHolder = new Long[1];
        final Long[] feeLoanChargeIdHolder = new Long[1];
        businessDateHelper.runAt("2026-02-01", () -> {
            Long client = clientHelper.createClient("01 February 2026");
            loanIdHolder[0] = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 February 2026");
            feeLoanChargeIdHolder[0] = addFeeCharge(loanIdHolder[0], 100, "01 February 2026");
        });
        businessDateHelper.runAt("2026-02-10", () -> {
            // REPAYMENT follows the product's DEFAULT order (fee before principal): a 60 repayment is fully
            // absorbed by the 100 fee, leaving principal untouched.

            wcLoanHelper.makeRepayment(loanIdHolder[0],
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(60), "10 February 2026"));

            GetWorkingCapitalLoanTransactionIdResponse repayment = findTransaction(wcLoanHelper.getTransactions(loanIdHolder[0]),
                    "loanTransactionType.repayment", LocalDate.of(2026, Month.FEBRUARY, 10), BigDecimal.valueOf(60));
            assertEqualBigDecimal(BigDecimal.valueOf(60), repayment.getFeeChargesPortion());
            assertEqualBigDecimal(BigDecimal.ZERO, repayment.getPrincipalPortion());

            GetBalance afterRepayment = balanceOf(loanIdHolder[0]);
            assertEqualBigDecimal(BigDecimal.valueOf(60), afterRepayment.getFeePaid());
            assertEqualBigDecimal(BigDecimal.ZERO, afterRepayment.getPrincipalPaid());
            assertEqualBigDecimal(BigDecimal.valueOf(9000), afterRepayment.getPrincipalOutstanding());
        });
        businessDateHelper.runAt("2026-02-15", () -> {
            // GOODWILL_CREDIT follows its own configured order (principal before fee/penalty): an identical 60
            // amount is instead fully absorbed by principal, leaving the remaining 40 fee outstanding untouched.

            wcLoanHelper.makeGoodwillCredit(loanIdHolder[0],
                    WorkingCapitalLoanRequestBuilders.goodwillCredit(BigDecimal.valueOf(60), "15 February 2026"));

            GetWorkingCapitalLoanTransactionIdResponse goodwillCredit = findTransaction(wcLoanHelper.getTransactions(loanIdHolder[0]),
                    "loanTransactionType.goodwillCredit", LocalDate.of(2026, Month.FEBRUARY, 15), BigDecimal.valueOf(60));
            assertEqualBigDecimal(BigDecimal.valueOf(60), goodwillCredit.getPrincipalPortion());
            assertEqualBigDecimal(BigDecimal.ZERO, goodwillCredit.getFeeChargesPortion());

            GetBalance afterGoodwillCredit = balanceOf(loanIdHolder[0]);
            assertEqualBigDecimal(BigDecimal.valueOf(60), afterGoodwillCredit.getPrincipalPaid());
            assertEqualBigDecimal(BigDecimal.valueOf(8940), afterGoodwillCredit.getPrincipalOutstanding());
            assertEqualBigDecimal(BigDecimal.valueOf(60), afterGoodwillCredit.getFeePaid());

            WorkingCapitalLoanChargeData feeCharge = findCharge(wcLoanHelper.getCharges(loanIdHolder[0]), feeLoanChargeIdHolder[0]);
            assertEqualBigDecimal(BigDecimal.valueOf(60), feeCharge.getAmountPaid());
            assertEqualBigDecimal(BigDecimal.valueOf(40), feeCharge.getAmountOutstanding());
        });
    }

    @Test
    void testChargeAdjustmentUsesItsOwnConfiguredAllocationOrderInsteadOfDefaultOrder() {
        final Long[] loanIdHolder = new Long[1];
        final Long[] feeLoanChargeIdHolder = new Long[1];
        businessDateHelper.runAt("2026-03-01", () -> {
            Long client = clientHelper.createClient("01 March 2026");
            loanIdHolder[0] = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 March 2026",
                    createProductWithChargeAdjustmentOverride());
            feeLoanChargeIdHolder[0] = addFeeCharge(loanIdHolder[0], 100, "01 March 2026");
        });
        businessDateHelper.runAt("2026-03-05", () -> {
            wcLoanHelper.adjustCharge(loanIdHolder[0], feeLoanChargeIdHolder[0],
                    WorkingCapitalLoanRequestBuilders.chargeAdjustment(BigDecimal.valueOf(60), "05 March 2026"));

            GetWorkingCapitalLoanTransactionIdResponse chargeAdjustment = findTransaction(wcLoanHelper.getTransactions(loanIdHolder[0]),
                    "loanTransactionType.chargeAdjustment", LocalDate.of(2026, Month.MARCH, 5), BigDecimal.valueOf(60));
            assertEqualBigDecimal(BigDecimal.valueOf(60), chargeAdjustment.getPrincipalPortion());
            assertEqualBigDecimal(BigDecimal.ZERO, chargeAdjustment.getFeeChargesPortion());

            GetBalance balance = balanceOf(loanIdHolder[0]);
            assertEqualBigDecimal(BigDecimal.valueOf(60), balance.getPrincipalPaid());
            assertEqualBigDecimal(BigDecimal.valueOf(8940), balance.getPrincipalOutstanding());
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getFeePaid());

            WorkingCapitalLoanChargeData feeCharge = findCharge(wcLoanHelper.getCharges(loanIdHolder[0]), feeLoanChargeIdHolder[0]);
            assertEqualBigDecimal(BigDecimal.ZERO, feeCharge.getAmountPaid());
            assertEqualBigDecimal(BigDecimal.valueOf(100), feeCharge.getAmountOutstanding());
        });
    }

    private Long addFeeCharge(Long loanId, double amount, String dueDate) {
        Long chargeId = wcLoanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, amount));
        return wcLoanHelper.addCharge(loanId, WorkingCapitalLoanRequestBuilders.addCharge(chargeId, amount, dueDate));
    }

    private GetBalance balanceOf(Long loanId) {
        GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
        assertNotNull(loan.getBalance(), "Balance should exist");
        return loan.getBalance();
    }

    private Long createAndDisburseLoanOnDate(Long clientIdParam, BigDecimal principal, String date) {
        return createAndDisburseLoanOnDate(clientIdParam, principal, date, createProductWithGoodwillCreditOverride());
    }

    private Long createAndDisburseLoanOnDate(Long clientIdParam, BigDecimal principal, String date, Long productId) {
        Long loanId = submitAndTrack(clientIdParam, productId, principal, date);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(date, principal, date));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(date, principal));
        return loanId;
    }

    private Long submitAndTrack(Long clientIdParam, Long productId, BigDecimal principal, String date) {
        Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientIdParam, productId,
                principal, BigDecimal.valueOf(18), date, date));
        createdLoanIds.add(loanId);
        return loanId;
    }

    private Long createProductWithGoodwillCreditOverride() {
        String uniqueName = "WCL PayAlloc " + Utils.uniqueRandomStringGenerator("", 8);
        String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName)
                                .withPaymentAllocationTypes(FEE_BEFORE_PRINCIPAL_ORDER).withPaymentAllocationForTransactionType(
                                        PostPaymentAllocation.TransactionTypeEnum.GOODWILL_CREDIT, PRINCIPAL_BEFORE_FEE_ORDER)
                                .build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createProductWithChargeAdjustmentOverride() {
        String uniqueName = "WCL PayAlloc " + Utils.uniqueRandomStringGenerator("", 8);
        String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName)
                                .withPaymentAllocationTypes(FEE_BEFORE_PRINCIPAL_ORDER).withPaymentAllocationForTransactionType(
                                        PostPaymentAllocation.TransactionTypeEnum.CHARGE_ADJUSTMENT, PRINCIPAL_BEFORE_FEE_ORDER)
                                .build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private static GetWorkingCapitalLoanTransactionIdResponse findTransaction(List<GetWorkingCapitalLoanTransactionIdResponse> transactions,
            String transactionCode, LocalDate transactionDate, BigDecimal amount) {
        return transactions.stream().filter(txn -> transactionDate.equals(txn.getTransactionDate()))
                .filter(txn -> Objects.nonNull(txn.getType()) && StringUtils.equals(transactionCode, txn.getType().getCode()))
                .filter(txn -> txn.getTransactionAmount() != null && amount.compareTo(txn.getTransactionAmount()) == 0).findFirst()
                .orElseThrow(() -> new AssertionError("Transaction not found on " + transactionDate + " with amount " + amount));
    }

    private static WorkingCapitalLoanChargeData findCharge(List<WorkingCapitalLoanChargeData> charges, Long loanChargeId) {
        return charges.stream().filter(charge -> loanChargeId.equals(charge.getId())).findFirst()
                .orElseThrow(() -> new AssertionError("Loan charge not found with id " + loanChargeId));
    }

    private static void assertEqualBigDecimal(BigDecimal expected, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, expected.compareTo(actual));
    }
}
