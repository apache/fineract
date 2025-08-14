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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.LoanApprovedAmountHistoryData;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutLoansApprovedAmountResponse;
import org.apache.fineract.client.models.PutLoansAvailableDisbursementAmountResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.externalevents.LoanBusinessEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoanModifyApprovedAmountTest extends BaseLoanIntegrationTest {

    @Test
    public void testValidLoanApprovedAmountModification() {
        BigDecimal sixHundred = BigDecimal.valueOf(600.0);
        BigDecimal thousand = BigDecimal.valueOf(1000.0);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");
            PutLoansApprovedAmountResponse putLoansApprovedAmountResponse = modifyLoanApprovedAmount(loanId, sixHundred);

            Assertions.assertEquals(loanId, putLoansApprovedAmountResponse.getResourceId());
            Assertions.assertNotNull(putLoansApprovedAmountResponse.getChanges());
            Assertions.assertNotNull(putLoansApprovedAmountResponse.getChanges().getNewApprovedAmount());
            Assertions.assertNotNull(putLoansApprovedAmountResponse.getChanges().getOldApprovedAmount());
            Assertions.assertEquals(sixHundred,
                    putLoansApprovedAmountResponse.getChanges().getNewApprovedAmount().setScale(1, RoundingMode.HALF_UP));
            Assertions.assertEquals(thousand,
                    putLoansApprovedAmountResponse.getChanges().getOldApprovedAmount().setScale(1, RoundingMode.HALF_UP));
        });
    }

    @Test
    public void testLoanApprovedAmountModificationEvent() {
        externalEventHelper.enableBusinessEvent("LoanApprovedAmountChangedBusinessEvent");
        BigDecimal sixHundred = BigDecimal.valueOf(600.0);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");

            deleteAllExternalEvents();
            modifyLoanApprovedAmount(loanId, sixHundred);

            verifyBusinessEvents(new LoanBusinessEvent("LoanApprovedAmountChangedBusinessEvent", "01 January 2024", 300, 100.0, 100.0));
        });
    }

    @Test
    public void testValidLoanApprovedAmountModificationInvalidRequest() {
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());

        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> modifyLoanApprovedAmount(loanId, null));

            assertEquals(400, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("validation.msg.loan.approved.amount.amount.cannot.be.blank"));
        });
    }

    @Test
    public void testValidLoanApprovedAmountModificationInvalidLoanStatus() {
        BigDecimal sixHundred = BigDecimal.valueOf(600.0);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applyLP2ProgressiveLoanRequest(client.getClientId(),
                    loanProductsResponse.getResourceId(), "1 January 2024", 1000.0, 10.0, 4, null));

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> modifyLoanApprovedAmount(postLoansResponse.getResourceId(), sixHundred));

            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage()
                    .contains("validation.msg.loan.approved.amount.loan.status.not.valid.for.approved.amount.modification"));
        });
    }

    @Test
    public void testModifyLoanApprovedAmountTooHigh() {
        BigDecimal twoThousand = BigDecimal.valueOf(2000.0);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> modifyLoanApprovedAmount(loanId, twoThousand));

            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage()
                    .contains("validation.msg.loan.approved.amount.amount.can't.be.greater.than.maximum.applied.loan.amount.calculation"));
        });
    }

    @Test
    public void testModifyLoanApprovedAmountHigherButInRange() {
        BigDecimal thousand = BigDecimal.valueOf(1000.0);
        BigDecimal fifteenHundred = BigDecimal.valueOf(1500.0);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            PutLoansApprovedAmountResponse putLoansApprovedAmountResponse = modifyLoanApprovedAmount(loanId, fifteenHundred);

            Assertions.assertEquals(loanId, putLoansApprovedAmountResponse.getResourceId());
            Assertions.assertNotNull(putLoansApprovedAmountResponse.getChanges());
            Assertions.assertNotNull(putLoansApprovedAmountResponse.getChanges().getNewApprovedAmount());
            Assertions.assertNotNull(putLoansApprovedAmountResponse.getChanges().getOldApprovedAmount());
            Assertions.assertEquals(fifteenHundred,
                    putLoansApprovedAmountResponse.getChanges().getNewApprovedAmount().setScale(1, RoundingMode.HALF_UP));
            Assertions.assertEquals(thousand,
                    putLoansApprovedAmountResponse.getChanges().getOldApprovedAmount().setScale(1, RoundingMode.HALF_UP));
        });
    }

    @Test
    public void testModifyLoanApprovedAmountWithNegativeAmount() {
        BigDecimal sixHundred = BigDecimal.valueOf(600.0);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> modifyLoanApprovedAmount(loanId, sixHundred.negate()));

            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("validation.msg.loan.approved.amount.amount.not.greater.than.zero"));
        });
    }

    @Test
    public void testModifyLoanApprovedAmountCapitalizedIncomeCountsAsPrincipal() {
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressiveWithCapitalizedIncome());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            disburseLoan(loanId, BigDecimal.valueOf(500), "1 January 2024");
            PostLoansLoanIdTransactionsResponse capitalizedIncomeResponse = loanTransactionHelper.addCapitalizedIncome(loanId,
                    "1 January 2024", 500.0);

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(500.0)));

            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage()
                    .contains("validation.msg.loan.approved.amount.amount.less.than.disbursed.principal.and.capitalized.income"));

            loanTransactionHelper.reverseLoanTransaction(capitalizedIncomeResponse.getLoanId(), capitalizedIncomeResponse.getResourceId(),
                    "1 January 2024");

            Assertions.assertDoesNotThrow(() -> modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(500.0)));
        });
    }

    @Test
    public void testModifyLoanApprovedAmountFutureExpectedDisbursementsCountAsPrincipal() {
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().disallowExpectedDisbursements(false).allowApprovedDisbursedAmountsOverApplied(null)
                        .overAppliedCalculationType(null).overAppliedNumber(null));
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 7.0, 6, (request) -> request.disbursementData(List.of(new PostLoansDisbursementData()
                            .expectedDisbursementDate("1 January 2024").principal(BigDecimal.valueOf(1000.0)))));

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(500.0)));

            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage()
                    .contains("validation.msg.loan.approved.amount.amount.less.than.disbursed.principal.and.capitalized.income"));
        });
    }

    @Test
    public void testModifyLoanApprovedAmountCreatesHistoryEntries() {
        BigDecimal fourHundred = BigDecimal.valueOf(400.0);
        BigDecimal sixHundred = BigDecimal.valueOf(600.0);
        BigDecimal eightHundred = BigDecimal.valueOf(800.0);
        BigDecimal thousand = BigDecimal.valueOf(1000.0);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(800.0));
            modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(600.0));
            modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(400.0));

            List<LoanApprovedAmountHistoryData> loanApprovedAmountHistory = getLoanApprovedAmountHistory(loanId);

            Assertions.assertNotNull(loanApprovedAmountHistory);
            Assertions.assertEquals(3, loanApprovedAmountHistory.size());

            Assertions.assertEquals(thousand, loanApprovedAmountHistory.get(0).getOldApprovedAmount().setScale(1, RoundingMode.HALF_UP));
            Assertions.assertEquals(eightHundred,
                    loanApprovedAmountHistory.get(0).getNewApprovedAmount().setScale(1, RoundingMode.HALF_UP));

            Assertions.assertEquals(eightHundred,
                    loanApprovedAmountHistory.get(1).getOldApprovedAmount().setScale(1, RoundingMode.HALF_UP));
            Assertions.assertEquals(sixHundred, loanApprovedAmountHistory.get(1).getNewApprovedAmount().setScale(1, RoundingMode.HALF_UP));

            Assertions.assertEquals(sixHundred, loanApprovedAmountHistory.get(2).getOldApprovedAmount().setScale(1, RoundingMode.HALF_UP));
            Assertions.assertEquals(fourHundred, loanApprovedAmountHistory.get(2).getNewApprovedAmount().setScale(1, RoundingMode.HALF_UP));
        });
    }

    @Test
    public void testDisbursementValidationAfterApprovedAmountReduction() {
        // Test that disbursement validation properly respects reduced approved amounts
        // Scenario: Reduce approved amount and verify disbursements are limited to new amount

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            // Create loan with applied amount $1000
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            // Reduce approved amount to $900
            PutLoansApprovedAmountResponse modifyResponse = modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(900.0));
            assertEquals(BigDecimal.valueOf(900.0), modifyResponse.getChanges().getNewApprovedAmount().setScale(1, RoundingMode.HALF_UP));

            // Disburse $100 (should work as it's within approved amount)
            Assertions.assertDoesNotThrow(() -> disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024"),
                    "Should be able to disburse $100 after reducing approved amount to $900");

            // Disburse additional $250 (total $350, should work as it's within proposed $1000 × 150% = $1350)
            Assertions.assertDoesNotThrow(() -> disburseLoan(loanId, BigDecimal.valueOf(250), "1 January 2024"),
                    "Should be able to disburse additional $250 (total $350) within allowed limit");

            // Try to disburse additional $1200 (total $1550, should fail as it exceeds $1000 × 150% = $1350)
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> disburseLoan(loanId, BigDecimal.valueOf(1200), "1 January 2024"));
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("amount.can't.be.greater.than.maximum.applied.loan.amount.calculation"),
                    "Should fail when total disbursements exceed modified approved amount × over-applied percentage");
        });
    }

    @Test
    public void testProgressiveDisbursementsWithDynamicApprovedAmountChanges() {
        // Test multiple disbursements with increasing and decreasing approved amount modifications
        // Validates that each disbursement respects the current approved amount limits

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            // Create loan with $1000 applied amount
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            // First disbursement: $300
            disburseLoan(loanId, BigDecimal.valueOf(300), "1 January 2024");

            // Increase approved amount to $1200
            PutLoansApprovedAmountResponse increaseResponse = modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(1200.0));
            assertEquals(BigDecimal.valueOf(1200.0),
                    increaseResponse.getChanges().getNewApprovedAmount().setScale(1, RoundingMode.HALF_UP));

            // Second disbursement: $400 (total $700, within $1200)
            Assertions.assertDoesNotThrow(() -> disburseLoan(loanId, BigDecimal.valueOf(400), "1 January 2024"));

            // Reduce approved amount to $800
            PutLoansApprovedAmountResponse reduceResponse = modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(800.0));
            assertEquals(BigDecimal.valueOf(800.0), reduceResponse.getChanges().getNewApprovedAmount().setScale(1, RoundingMode.HALF_UP));

            // Third disbursement: $100 (total $800, within proposed $1000 × 150% = $1500)
            Assertions.assertDoesNotThrow(() -> disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024"));

            // Fourth disbursement: $800 (total $1600, should fail as it exceeds $1000 × 150% = $1500)
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> disburseLoan(loanId, BigDecimal.valueOf(800), "1 January 2024"));
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("amount.can't.be.greater.than.maximum.applied.loan.amount.calculation"));
        });
    }

    @Test
    public void testApprovedAmountModificationWithCapitalizedIncomeScenario() {
        // Test approved amount modification interaction with capitalized income

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressiveWithCapitalizedIncome());
        runAt("1 January 2024", () -> {
            // Create loan with $1000 applied amount
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            // Disburse $300
            disburseLoan(loanId, BigDecimal.valueOf(300), "1 January 2024");

            // Add capitalized income of $200 (total disbursed equivalent: $500)
            loanTransactionHelper.addCapitalizedIncome(loanId, "1 January 2024", 200.0);

            // Try to reduce approved amount to $400 (should fail as disbursed + capitalized = $500)
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(400.0)));
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage()
                    .contains("validation.msg.loan.approved.amount.amount.less.than.disbursed.principal.and.capitalized.income"));

            // Should succeed with $500 (exactly matching disbursed + capitalized)
            Assertions.assertDoesNotThrow(() -> modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(500.0)));

            // Should succeed with $600 (above disbursed + capitalized)
            Assertions.assertDoesNotThrow(() -> modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(600.0)));
        });
    }

    @Test
    public void testUndoDisbursementAfterApprovedAmountReduction() {
        // Test undo disbursement functionality after approved amount reduction
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(800.0));
            disburseLoan(loanId, BigDecimal.valueOf(600), "1 January 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            if (loanDetails.getSummary() != null && loanDetails.getSummary().getPrincipalDisbursed() != null) {
                assertEquals(BigDecimal.valueOf(600.0), loanDetails.getSummary().getPrincipalDisbursed().setScale(1, RoundingMode.HALF_UP));
            }

            PostLoansLoanIdRequest undoRequest = new PostLoansLoanIdRequest().note("Undo disbursement for testing");
            Assertions.assertDoesNotThrow(() -> loanTransactionHelper.undoDisbursalLoan(loanId, undoRequest));

            GetLoansLoanIdResponse loanDetailsAfterUndo = loanTransactionHelper.getLoanDetails(loanId);
            BigDecimal activeDisbursedAmount = BigDecimal.ZERO;
            if (loanDetailsAfterUndo.getTransactions() != null && !loanDetailsAfterUndo.getTransactions().isEmpty()) {
                activeDisbursedAmount = loanDetailsAfterUndo.getTransactions().stream()
                        .filter(transaction -> transaction.getType() != null && "Disbursement".equals(transaction.getType().getValue()))
                        .filter(transaction -> !Boolean.TRUE.equals(transaction.getManuallyReversed()))
                        .map(GetLoansLoanIdTransactions::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            assertEquals(0, BigDecimal.ZERO.compareTo(activeDisbursedAmount));

            Assertions.assertDoesNotThrow(() -> modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(400.0)));

            GetLoansLoanIdResponse finalLoanDetails = loanTransactionHelper.getLoanDetails(loanId);
            assertEquals(BigDecimal.valueOf(400.0), finalLoanDetails.getApprovedPrincipal().setScale(1, RoundingMode.HALF_UP));
        });
    }

    @Test
    public void testUndoLastDisbursementWithMultipleDisbursements() {
        // Test undo last disbursement in multi-disbursement scenario with approved amount modifications
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            disburseLoan(loanId, BigDecimal.valueOf(300), "1 January 2024");
            modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(1200.0));
            disburseLoan(loanId, BigDecimal.valueOf(400), "1 January 2024");
            modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(800.0));
            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            if (loanDetails.getSummary() != null && loanDetails.getSummary().getPrincipalDisbursed() != null) {
                assertEquals(BigDecimal.valueOf(800.0), loanDetails.getSummary().getPrincipalDisbursed().setScale(1, RoundingMode.HALF_UP));
            }

            PostLoansLoanIdRequest undoLastRequest = new PostLoansLoanIdRequest().note("Undo last disbursement");
            Assertions.assertDoesNotThrow(() -> loanTransactionHelper.undoLastDisbursalLoan(loanId, undoLastRequest));

            GetLoansLoanIdResponse loanDetailsAfterUndo = loanTransactionHelper.getLoanDetails(loanId);
            BigDecimal activeDisbursedAmount = BigDecimal.ZERO;
            if (loanDetailsAfterUndo.getTransactions() != null && !loanDetailsAfterUndo.getTransactions().isEmpty()) {
                activeDisbursedAmount = loanDetailsAfterUndo.getTransactions().stream()
                        .filter(transaction -> transaction.getType() != null && "Disbursement".equals(transaction.getType().getValue()))
                        .filter(transaction -> !Boolean.TRUE.equals(transaction.getManuallyReversed()))
                        .map(GetLoansLoanIdTransactions::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            assertEquals(BigDecimal.valueOf(700.0), activeDisbursedAmount.setScale(1, RoundingMode.HALF_UP));

            Assertions.assertDoesNotThrow(() -> modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(700.0)));

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(600.0)));
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage()
                    .contains("validation.msg.loan.approved.amount.amount.less.than.disbursed.principal.and.capitalized.income"));
        });
    }

    @Test
    public void testDisbursementValidationAfterUndoWithReducedApprovedAmount() {
        // Test disbursement validation after undo disbursement with reduced approved amount
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            modifyLoanApprovedAmount(loanId, BigDecimal.valueOf(600.0));
            disburseLoan(loanId, BigDecimal.valueOf(500), "1 January 2024");

            PostLoansLoanIdRequest undoRequest = new PostLoansLoanIdRequest().note("Undo for testing validation");
            loanTransactionHelper.undoDisbursalLoan(loanId, undoRequest);

            Assertions.assertDoesNotThrow(() -> disburseLoan(loanId, BigDecimal.valueOf(700), "1 January 2024"));

            loanTransactionHelper.undoDisbursalLoan(loanId, undoRequest);

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> disburseLoan(loanId, BigDecimal.valueOf(1600), "1 January 2024"));
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("amount.can't.be.greater.than.maximum.applied.loan.amount.calculation"));
        });
    }

    @Test
    public void testValidLoanAvailableDisbursementAmountModification() {
        BigDecimal sixHundred = BigDecimal.valueOf(600.0);
        BigDecimal sevenHundred = BigDecimal.valueOf(700.0);
        BigDecimal nineHundred = BigDecimal.valueOf(900.0);
        BigDecimal thousand = BigDecimal.valueOf(1000.0);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");
            PutLoansAvailableDisbursementAmountResponse putLoansAvailableDisbursementAmountResponse = modifyLoanAvailableDisbursementAmount(
                    loanId, sixHundred);

            Assertions.assertEquals(loanId, putLoansAvailableDisbursementAmountResponse.getResourceId());
            Assertions.assertNotNull(putLoansAvailableDisbursementAmountResponse.getChanges());
            Assertions.assertNotNull(putLoansAvailableDisbursementAmountResponse.getChanges().getNewApprovedAmount());
            Assertions.assertNotNull(putLoansAvailableDisbursementAmountResponse.getChanges().getOldApprovedAmount());
            Assertions.assertNotNull(putLoansAvailableDisbursementAmountResponse.getChanges().getOldAvailableDisbursementAmount());
            Assertions.assertNotNull(putLoansAvailableDisbursementAmountResponse.getChanges().getNewAvailableDisbursementAmount());
            Assertions.assertEquals(sevenHundred,
                    putLoansAvailableDisbursementAmountResponse.getChanges().getNewApprovedAmount().setScale(1, RoundingMode.HALF_UP));
            Assertions.assertEquals(thousand,
                    putLoansAvailableDisbursementAmountResponse.getChanges().getOldApprovedAmount().setScale(1, RoundingMode.HALF_UP));
            Assertions.assertEquals(sixHundred, putLoansAvailableDisbursementAmountResponse.getChanges().getNewAvailableDisbursementAmount()
                    .setScale(1, RoundingMode.HALF_UP));
            Assertions.assertEquals(nineHundred, putLoansAvailableDisbursementAmountResponse.getChanges()
                    .getOldAvailableDisbursementAmount().setScale(1, RoundingMode.HALF_UP));
        });
    }

    @Test
    public void testLoanAvailableDisbursementAmountModificationEvent() {
        externalEventHelper.enableBusinessEvent("LoanApprovedAmountChangedBusinessEvent");
        BigDecimal sixHundred = BigDecimal.valueOf(600.0);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");

            deleteAllExternalEvents();
            modifyLoanAvailableDisbursementAmount(loanId, sixHundred);

            verifyBusinessEvents(new LoanBusinessEvent("LoanApprovedAmountChangedBusinessEvent", "01 January 2024", 300, 100.0, 100.0));
        });
    }

    @Test
    public void testValidLoanAvailableDisbursementAmountModificationInvalidRequest() {
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());

        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> modifyLoanAvailableDisbursementAmount(loanId, null));

            assertEquals(400, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("validation.msg.loan.available.disbursement.amount.amount.cannot.be.blank"));
        });
    }

    @Test
    public void testValidLoanAvailableDisbursementAmountModificationInvalidLoanStatus() {
        BigDecimal sixHundred = BigDecimal.valueOf(600.0);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applyLP2ProgressiveLoanRequest(client.getClientId(),
                    loanProductsResponse.getResourceId(), "1 January 2024", 1000.0, 10.0, 4, null));

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> modifyLoanAvailableDisbursementAmount(postLoansResponse.getResourceId(), sixHundred));

            assertEquals(403, exception.getResponse().code());
            assertTrue(
                    exception.getMessage().contains("validation.msg.loan.available.disbursement.amount.loan.must.be.approved.or.active"));
        });
    }

    @Test
    public void testModifyLoanAvailableDisbursementAmountHigherThanApprovedAmount() {
        BigDecimal twoThousand = BigDecimal.valueOf(2000.0);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> modifyLoanAvailableDisbursementAmount(loanId, twoThousand));

            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains(
                    "validation.msg.loan.available.disbursement.amount.amount.can't.be.greater.than.maximum.available.disbursement.amount.calculation"));
        });
    }

    @Test
    public void testModifyLoanAvailableDisbursementAmountWithNegativeAmount() {
        BigDecimal sixHundred = BigDecimal.valueOf(600.0);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> modifyLoanAvailableDisbursementAmount(loanId, sixHundred.negate()));

            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("validation.msg.loan.available.disbursement.amount.amount.not.zero.or.greater"));
        });
    }

    @Test
    public void testModifyLoanAvailableDisbursementAmountCapitalizedIncomeCountsAsPrincipal() {
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressiveWithCapitalizedIncome());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            disburseLoan(loanId, BigDecimal.valueOf(500), "1 January 2024");
            PostLoansLoanIdTransactionsResponse capitalizedIncomeResponse = loanTransactionHelper.addCapitalizedIncome(loanId,
                    "1 January 2024", 500.0);

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> modifyLoanAvailableDisbursementAmount(loanId, BigDecimal.valueOf(600.0)));

            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains(
                    "validation.msg.loan.available.disbursement.amount.amount.can't.be.greater.than.maximum.available.disbursement.amount.calculation"));

            loanTransactionHelper.reverseLoanTransaction(capitalizedIncomeResponse.getLoanId(), capitalizedIncomeResponse.getResourceId(),
                    "1 January 2024");

            Assertions.assertDoesNotThrow(() -> modifyLoanAvailableDisbursementAmount(loanId, BigDecimal.valueOf(600.0)));
        });
    }

    @Test
    public void testModifyLoanAvailableDisbursementAmountFutureExpectedDisbursementsCountAsPrincipal() {
        BigDecimal twoHundred = BigDecimal.valueOf(200.0);
        BigDecimal eightHundred = BigDecimal.valueOf(800.0);
        BigDecimal thousand = BigDecimal.valueOf(1000.0);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().disallowExpectedDisbursements(false).allowApprovedDisbursedAmountsOverApplied(null)
                        .overAppliedCalculationType(null).overAppliedNumber(null));
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 7.0, 6, (request) -> request.disbursementData(List.of(new PostLoansDisbursementData()
                            .expectedDisbursementDate("1 January 2024").principal(BigDecimal.valueOf(800.0)))));

            disburseLoan(loanId, BigDecimal.valueOf(800), "1 January 2024");

            PutLoansAvailableDisbursementAmountResponse putLoansAvailableDisbursementAmountResponse = modifyLoanAvailableDisbursementAmount(
                    loanId, BigDecimal.ZERO);

            Assertions.assertEquals(eightHundred,
                    putLoansAvailableDisbursementAmountResponse.getChanges().getNewApprovedAmount().setScale(1, RoundingMode.HALF_UP));
            Assertions.assertEquals(thousand,
                    putLoansAvailableDisbursementAmountResponse.getChanges().getOldApprovedAmount().setScale(1, RoundingMode.HALF_UP));
            Assertions.assertEquals(BigDecimal.ZERO,
                    putLoansAvailableDisbursementAmountResponse.getChanges().getNewAvailableDisbursementAmount());
            Assertions.assertEquals(twoHundred, putLoansAvailableDisbursementAmountResponse.getChanges().getOldAvailableDisbursementAmount()
                    .setScale(1, RoundingMode.HALF_UP));
        });
    }

    @Test
    public void testModifyLoanAvailableDisbursementAmountCreatesHistoryEntries() {
        BigDecimal fourHundred = BigDecimal.valueOf(400.0);
        BigDecimal sixHundred = BigDecimal.valueOf(600.0);
        BigDecimal eightHundred = BigDecimal.valueOf(800.0);
        BigDecimal thousand = BigDecimal.valueOf(1000.0);

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    1000.0, 10.0, 4, null);

            modifyLoanAvailableDisbursementAmount(loanId, BigDecimal.valueOf(800.0));
            modifyLoanAvailableDisbursementAmount(loanId, BigDecimal.valueOf(600.0));
            modifyLoanAvailableDisbursementAmount(loanId, BigDecimal.valueOf(400.0));

            List<LoanApprovedAmountHistoryData> loanApprovedAmountHistory = getLoanApprovedAmountHistory(loanId);

            Assertions.assertNotNull(loanApprovedAmountHistory);
            Assertions.assertEquals(3, loanApprovedAmountHistory.size());

            Assertions.assertEquals(thousand, loanApprovedAmountHistory.get(0).getOldApprovedAmount().setScale(1, RoundingMode.HALF_UP));
            Assertions.assertEquals(eightHundred,
                    loanApprovedAmountHistory.get(0).getNewApprovedAmount().setScale(1, RoundingMode.HALF_UP));

            Assertions.assertEquals(eightHundred,
                    loanApprovedAmountHistory.get(1).getOldApprovedAmount().setScale(1, RoundingMode.HALF_UP));
            Assertions.assertEquals(sixHundred, loanApprovedAmountHistory.get(1).getNewApprovedAmount().setScale(1, RoundingMode.HALF_UP));

            Assertions.assertEquals(sixHundred, loanApprovedAmountHistory.get(2).getOldApprovedAmount().setScale(1, RoundingMode.HALF_UP));
            Assertions.assertEquals(fourHundred, loanApprovedAmountHistory.get(2).getNewApprovedAmount().setScale(1, RoundingMode.HALF_UP));
        });
    }
}
