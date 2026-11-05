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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostClientsClientIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostSavingsAccountBulkReversalTransactionsRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutPermissionsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ClientChargeSavingsPaymentTest extends FeignSavingsTestBase {

    private static final String DATE = "05 November 2026";

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void concurrentPaymentAndCompetingSettlementCannotOverpay(boolean competeWithWaiver) {
        runAt(DATE, () -> {
            for (int round = 0; round < 3; round++) {
                Long clientId = createClient(DATE);
                Long productId = createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().nominalAnnualInterestRate(0.0))
                        .getResourceId();
                Long savingsId = createApproveActivateSavings(clientId, productId, DATE);
                deposit(savingsId, "100", DATE);
                Long definitionId = chargesHelper.createClientSpecifiedDueDateCharge(50.0).getResourceId();
                Long chargeId = chargesHelper
                        .addClientCharge(clientId, ChargeRequestBuilders.clientCharge(definitionId, DATE, BigDecimal.valueOf(50)))
                        .getResourceId();
                Callable<Boolean> payment = () -> accepted(() -> chargesHelper.payClientCharge(clientId, chargeId,
                        ChargeRequestBuilders.payClientCharge(DATE, 50).savingsAccountId(savingsId)));
                Callable<Boolean> competitor = competeWithWaiver
                        ? () -> accepted(
                                () -> chargesHelper.waiveClientCharge(clientId, chargeId, ChargeRequestBuilders.waiveClientCharge(50)))
                        : payment;
                var results = race(payment, competitor);
                assertEquals(1, results.stream().filter(Boolean::booleanValue).count());
                assertAmount("0", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());
                assertAmount(competeWithWaiver && !results.getFirst() ? "100" : "50",
                        getSavingsDetails(savingsId).getSummary().getAccountBalance());
                long withdrawals = savingsTransactionHelper.getTransactions(savingsId).stream()
                        .filter(transaction -> Boolean.TRUE.equals(transaction.getTransactionType().getWithdrawal())).count();
                assertEquals(competeWithWaiver && !results.getFirst() ? 0 : 1, withdrawals);
            }
        });
    }

    @Test
    void concurrentUndoRestoresFundingOnlyOnce() {
        runAt(DATE, () -> {
            for (int round = 0; round < 3; round++) {
                Long clientId = createClient(DATE);
                Long productId = createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().nominalAnnualInterestRate(0.0))
                        .getResourceId();
                Long savingsId = createApproveActivateSavings(clientId, productId, DATE);
                deposit(savingsId, "100", DATE);
                Long definitionId = chargesHelper.createClientSpecifiedDueDateCharge(50.0).getResourceId();
                Long chargeId = chargesHelper
                        .addClientCharge(clientId, ChargeRequestBuilders.clientCharge(definitionId, DATE, BigDecimal.valueOf(50)))
                        .getResourceId();
                var payment = chargesHelper.payClientCharge(clientId, chargeId,
                        ChargeRequestBuilders.payClientCharge(DATE, 50).savingsAccountId(savingsId));
                Callable<Boolean> undo = () -> accepted(() -> clientHelper.undoClientTransaction(clientId, payment.getTransactionId()));
                assertEquals(1, race(undo, undo).stream().filter(Boolean::booleanValue).count());
                assertAmount("100", getSavingsDetails(savingsId).getSummary().getAccountBalance());
                assertAmount("50", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());
                assertTrue(clientHelper.getClientTransaction(clientId, payment.getTransactionId()).getReversed());
            }
        });
    }

    private boolean accepted(Callable<?> operation) throws Exception {
        try {
            operation.call();
            return true;
        } catch (CallFailedRuntimeException failure) {
            assertTrue(java.util.List.of(400, 403, 409, 503).contains(failure.getStatus()), failure.getResponseBody());
            return false;
        }
    }

    private java.util.List<Boolean> race(Callable<Boolean> first, Callable<Boolean> second) {
        try (var executor = Executors.newFixedThreadPool(2)) {
            var ready = new CountDownLatch(2);
            var start = new CountDownLatch(1);
            var results = new java.util.ArrayList<java.util.concurrent.Future<Boolean>>();
            for (var operation : java.util.List.of(first, second)) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(30, TimeUnit.SECONDS)) {
                        throw new AssertionError("Concurrent requests did not start");
                    }
                    return operation.call();
                }));
            }
            assertTrue(ready.await(30, TimeUnit.SECONDS));
            start.countDown();
            return java.util.List.of(results.get(0).get(60, TimeUnit.SECONDS), results.get(1).get(60, TimeUnit.SECONDS));
        } catch (Exception failure) {
            throw new AssertionError("Concurrent charge operations failed", failure);
        }
    }

    @Test
    void makerCheckerQueuesApprovesAndRejectsWithoutPrematureFunding() {
        runAt(DATE, () -> {
            String configuration = "maker-checker";
            boolean previousGlobal = globalConfigurationHelper.getGlobalConfigurationByName(configuration).getEnabled();
            var permissions = ok(() -> fineractClient().permissions().retrieveAllPermissionsUniversal(Map.of("makerCheckerable", true)));
            Map<String, Boolean> previousPermissions = new java.util.HashMap<>();
            for (String code : java.util.List.of("PAY_CLIENTCHARGE", "UNDOTRANSACTION_CLIENT")) {
                previousPermissions.put(code, permissions.stream().filter(permission -> code.equals(permission.getCode())).findFirst()
                        .orElseThrow().getSelected());
            }
            Long makerRole = FeignRoleHelper.createRole();
            Long checkerRole = FeignRoleHelper.createRole();
            FeignRoleHelper.addPermissionsToRole(makerRole, Map.of("PAY_CLIENTCHARGE", true, "UNDOTRANSACTION_CLIENT", true,
                    "WITHDRAWAL_SAVINGSACCOUNT", true, "UNDOTRANSACTION_SAVINGSACCOUNT", true));
            FeignRoleHelper.addPermissionsToRole(checkerRole,
                    Map.of("PAY_CLIENTCHARGE_CHECKER", true, "UNDOTRANSACTION_CLIENT_CHECKER", true));
            Long staffId = new FeignStaffHelper(fineractClient()).createStaff().getResourceId();
            String makerName = Utils.uniqueRandomStringGenerator("chargemaker", 8);
            String checkerName = Utils.uniqueRandomStringGenerator("chargechecker", 8);
            String password = "A1b2c3d4e5f$";
            Long makerId = FeignUserHelper.createUser(makerRole, staffId, makerName, password).getResourceId();
            Long checkerId = FeignUserHelper.createUser(checkerRole, staffId, checkerName, password).getResourceId();
            var maker = FineractFeignClientHelper.createNewFineractFeignClient(makerName, password);
            var checker = FineractFeignClientHelper.createNewFineractFeignClient(checkerName, password);
            try {
                Long clientId = createClient(DATE);
                Long productId = createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().nominalAnnualInterestRate(0.0))
                        .getResourceId();
                Long savingsId = createApproveActivateSavings(clientId, productId, DATE);
                deposit(savingsId, "100", DATE);
                Long definitionId = chargesHelper.createClientSpecifiedDueDateCharge(50.0).getResourceId();
                Long chargeId = chargesHelper
                        .addClientCharge(clientId, ChargeRequestBuilders.clientCharge(definitionId, DATE, BigDecimal.valueOf(50)))
                        .getResourceId();
                globalConfigurationHelper.updateGlobalConfiguration(configuration, new PutGlobalConfigurationsRequest().enabled(true));
                ok(() -> fineractClient().permissions().updatePermissions(new PutPermissionsRequest()
                        .putPermissionsItem("PAY_CLIENTCHARGE", true).putPermissionsItem("UNDOTRANSACTION_CLIENT", true)));
                var request = ChargeRequestBuilders.payClientCharge(DATE, 20).savingsAccountId(savingsId);
                ok(() -> maker.clientCharges().payOrWaiveClientCharge(clientId, chargeId, request, "paycharge"));
                assertAmount("100", getSavingsDetails(savingsId).getSummary().getAccountBalance());
                assertAmount("50", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());
                Long pendingPayment = pendingCommand(clientId, "PAY");
                var checkerFailure = fail(
                        () -> checker.makerCheckerOr4EyeFunctionality().approveMakerCheckerEntry(pendingPayment, "approve"));
                assertEquals(403, checkerFailure.getStatus());
                assertTrue(checkerFailure.getResponseBody().contains("WITHDRAWAL_SAVINGSACCOUNT"));
                assertAmount("100", getSavingsDetails(savingsId).getSummary().getAccountBalance());
                FeignRoleHelper.addPermissionsToRole(checkerRole,
                        Map.of("WITHDRAWAL_SAVINGSACCOUNT", true, "UNDOTRANSACTION_SAVINGSACCOUNT", true));
                ok(() -> maker.clientCharges().payOrWaiveClientCharge(clientId, chargeId, request, "paycharge"));
                Long authorizedPayment = pendingCommand(clientId, "PAY");
                var approval = ok(() -> checker.makerCheckerOr4EyeFunctionality().approveMakerCheckerEntry(authorizedPayment, "approve"));
                assertAmount("80", getSavingsDetails(savingsId).getSummary().getAccountBalance());
                assertAmount("30", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());
                assertNotNull(approval.getTransactionId());
                Long transactionId = Long.valueOf(approval.getTransactionId());
                ok(() -> maker.clientTransaction().undoClientTransaction(clientId, transactionId, "undo"));
                assertAmount("80", getSavingsDetails(savingsId).getSummary().getAccountBalance());
                Long pendingUndo = pendingCommand(clientId, "UNDOTRANSACTION");
                ok(() -> checker.makerCheckerOr4EyeFunctionality().approveMakerCheckerEntry(pendingUndo, "approve"));
                assertAmount("100", getSavingsDetails(savingsId).getSummary().getAccountBalance());
                assertAmount("50", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());
                ok(() -> maker.clientCharges().payOrWaiveClientCharge(clientId, chargeId, request, "paycharge"));
                Long rejectedPayment = pendingCommand(clientId, "PAY");
                ok(() -> checker.makerCheckerOr4EyeFunctionality().approveMakerCheckerEntry(rejectedPayment, "reject"));
                assertAmount("100", getSavingsDetails(savingsId).getSummary().getAccountBalance());
                assertAmount("50", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());
            } finally {
                ok(() -> fineractClient().permissions().updatePermissions(new PutPermissionsRequest().permissions(previousPermissions)));
                globalConfigurationHelper.updateGlobalConfiguration(configuration,
                        new PutGlobalConfigurationsRequest().enabled(previousGlobal));
                FeignUserHelper.deleteUser(makerId);
                FeignUserHelper.deleteUser(checkerId);
                FeignRoleHelper.deleteRole(makerRole);
                FeignRoleHelper.deleteRole(checkerRole);
            }
        });
    }

    private Long pendingCommand(Long clientId, String action) {
        var commands = ok(() -> fineractClient().makerCheckerOr4EyeFunctionality()
                .retrieveCommandsUniversal(Map.of("clientId", clientId, "actionName", action)));
        assertEquals(1, commands.size());
        return commands.getFirst().getId();
    }

    @Test
    void requiresSavingsPermissionsForNonSuperuserPaymentAndUndo() {
        runAt(DATE, () -> {
            Long roleId = FeignRoleHelper.createRole();
            FeignRoleHelper.addPermissionsToRole(roleId, Map.of("PAY_CLIENTCHARGE", true, "UNDOTRANSACTION_CLIENT", true));
            String username = Utils.uniqueRandomStringGenerator("chargeoperator", 8);
            String password = "A1b2c3d4e5f$";
            Long staffId = new FeignStaffHelper(fineractClient()).createStaff().getResourceId();
            Long userId = FeignUserHelper.createUser(roleId, staffId, username, password).getResourceId();
            var operator = FineractFeignClientHelper.createNewFineractFeignClient(username, password);
            try {
                Long clientId = createClient(DATE);
                Long productId = createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().nominalAnnualInterestRate(0.0))
                        .getResourceId();
                Long savingsId = createApproveActivateSavings(clientId, productId, DATE);
                deposit(savingsId, "100", DATE);
                Long definitionId = chargesHelper.createClientSpecifiedDueDateCharge(50.0).getResourceId();
                Long chargeId = chargesHelper
                        .addClientCharge(clientId, ChargeRequestBuilders.clientCharge(definitionId, DATE, BigDecimal.valueOf(50)))
                        .getResourceId();
                var request = ChargeRequestBuilders.payClientCharge(DATE, 20).savingsAccountId(savingsId);
                var paymentFailure = fail(() -> operator.clientCharges().payOrWaiveClientCharge(clientId, chargeId, request, "paycharge"));
                assertEquals(403, paymentFailure.getStatus());
                assertTrue(paymentFailure.getResponseBody().contains("WITHDRAWAL_SAVINGSACCOUNT"));
                assertAmount("100", getSavingsDetails(savingsId).getSummary().getAccountBalance());
                assertAmount("50", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());

                FeignRoleHelper.addPermissionsToRole(roleId, Map.of("WITHDRAWAL_SAVINGSACCOUNT", true));
                var payment = ok(() -> operator.clientCharges().payOrWaiveClientCharge(clientId, chargeId, request, "paycharge"));
                var undoFailure = fail(
                        () -> operator.clientTransaction().undoClientTransaction(clientId, payment.getTransactionId(), "undo"));
                assertEquals(403, undoFailure.getStatus());
                assertTrue(undoFailure.getResponseBody().contains("UNDOTRANSACTION_SAVINGSACCOUNT"));
                assertAmount("80", getSavingsDetails(savingsId).getSummary().getAccountBalance());
                assertAmount("30", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());

                FeignRoleHelper.addPermissionsToRole(roleId, Map.of("UNDOTRANSACTION_SAVINGSACCOUNT", true));
                ok(() -> operator.clientTransaction().undoClientTransaction(clientId, payment.getTransactionId(), "undo"));
                assertAmount("100", getSavingsDetails(savingsId).getSummary().getAccountBalance());
                assertAmount("50", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());
            } finally {
                FeignUserHelper.deleteUser(userId);
                FeignRoleHelper.deleteRole(roleId);
            }
        });
    }

    @Test
    void rejectsIndependentFundingWithdrawalMutations() {
        runAt(DATE, () -> {
            Long clientId = createClient(DATE);
            Long productId = createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().nominalAnnualInterestRate(0.0))
                    .getResourceId();
            Long savingsId = createApproveActivateSavings(clientId, productId, DATE);
            deposit(savingsId, "100", DATE);
            Long definitionId = chargesHelper.createClientSpecifiedDueDateCharge(50.0).getResourceId();
            Long chargeId = chargesHelper
                    .addClientCharge(clientId, ChargeRequestBuilders.clientCharge(definitionId, DATE, BigDecimal.valueOf(50)))
                    .getResourceId();
            var payment = chargesHelper.payClientCharge(clientId, chargeId,
                    ChargeRequestBuilders.payClientCharge(DATE, 20).savingsAccountId(savingsId));
            Long withdrawalId = clientHelper.getClientTransaction(clientId, payment.getTransactionId()).getSavingsTransactionId();

            for (String command : java.util.List.of("undo", "reverse", "modify")) {
                var request = new PostSavingsAccountBulkReversalTransactionsRequest().locale("en").dateFormat("dd MMMM yyyy")
                        .transactionDate(DATE).transactionAmount(BigDecimal.TEN);
                var failure = fail(() -> fineractClient().savingsAccountTransactions().adjustSavingsAccountTransaction(savingsId,
                        withdrawalId, request, command));
                assertEquals(503, failure.getStatus());
                assertTrue(failure.getResponseBody().contains("error.msg.saving.account.transfer.transaction.update.not.allowed"));
                assertAmount("80", getSavingsDetails(savingsId).getSummary().getAccountBalance());
                assertAmount("30", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());
            }

            clientHelper.undoClientTransaction(clientId, payment.getTransactionId());
            assertAmount("100", getSavingsDetails(savingsId).getSummary().getAccountBalance());
            assertAmount("50", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void followsTransferWithdrawalFeePolicyAndUndoesFee(boolean chargeWithdrawalFee) {
        runAt(DATE, () -> {
            Long clientId = createClient(DATE);
            Long productId = createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().nominalAnnualInterestRate(0.0)
                    .withdrawalFeeForTransfers(chargeWithdrawalFee)).getResourceId();
            Long savingsId = savingsHelper.submitApplication(SavingsRequestBuilders.submitSavingsApplication(clientId, productId, DATE)
                    .withdrawalFeeForTransfers(chargeWithdrawalFee)).getResourceId();
            approveSavings(savingsId, DATE);
            activateSavings(savingsId, DATE);
            Long withdrawalFeeId = chargesHelper.createCharge(SavingsRequestBuilders.savingsWithdrawalFeeCharge().amount(5.0))
                    .getResourceId();
            savingsChargeHelper.addChargeToSavings(savingsId, withdrawalFeeId, 5.0f);
            deposit(savingsId, "100", DATE);
            Long definitionId = chargesHelper.createClientSpecifiedDueDateCharge(50.0).getResourceId();
            Long chargeId = chargesHelper
                    .addClientCharge(clientId, ChargeRequestBuilders.clientCharge(definitionId, DATE, BigDecimal.valueOf(50)))
                    .getResourceId();

            var payment = chargesHelper.payClientCharge(clientId, chargeId,
                    ChargeRequestBuilders.payClientCharge(DATE, 20).savingsAccountId(savingsId));

            assertAmount(chargeWithdrawalFee ? "75" : "80", getSavingsDetails(savingsId).getSummary().getAccountBalance());
            assertAmount("30", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());

            if (chargeWithdrawalFee) {
                Long feeTransactionId = savingsTransactionHelper.getTransactions(savingsId).stream()
                        .filter(transaction -> Boolean.TRUE.equals(transaction.getTransactionType().getWithdrawalFee())).findFirst()
                        .orElseThrow().getId();
                var failure = fail(() -> fineractClient().savingsAccountTransactions().adjustSavingsAccountTransaction(savingsId,
                        feeTransactionId, new PostSavingsAccountBulkReversalTransactionsRequest().isBulk("true"), "reverse"));
                assertEquals(503, failure.getStatus());
                assertTrue(failure.getResponseBody().contains("error.msg.saving.account.transfer.transaction.update.not.allowed"));
                assertAmount("75", getSavingsDetails(savingsId).getSummary().getAccountBalance());
                assertAmount("30", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());
                assertEquals(false, clientHelper.getClientTransaction(clientId, payment.getTransactionId()).getReversed());
            }

            clientHelper.undoClientTransaction(clientId, payment.getTransactionId());

            assertAmount("100", getSavingsDetails(savingsId).getSummary().getAccountBalance());
            assertAmount("50", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());
        });
    }

    @Test
    void postsThroughClearingAndReversesBothJournalLegs() {
        runAt(DATE, () -> {
            var mappingHelper = new FeignFinancialActivityAccountHelper(fineractClient());
            int activityId = FinancialActivity.LIABILITY_TRANSFER.getValue();
            var existingMapping = mappingHelper.getAllMappings().stream()
                    .filter(mapping -> mapping.getFinancialActivityData().getId().intValue() == activityId).findFirst();
            var clearing = accountHelper.createLiabilityAccount();
            Long temporaryMappingId = existingMapping.isEmpty() ? mappingHelper.createMapping(activityId, clearing).getResourceId() : null;
            Long clearingId = existingMapping.map(mapping -> mapping.getGlAccountData().getId())
                    .orElse(clearing.getAccountID().longValue());
            try {
                var asset = accountHelper.createAssetAccount();
                var control = accountHelper.createLiabilityAccount();
                var income = accountHelper.createIncomeAccount();
                var expense = accountHelper.createExpenseAccount();
                Long clientId = createClient(DATE);
                Long productId = createSavingsProduct(SavingsRequestBuilders.withCashBasedAccounting(
                        SavingsRequestBuilders.defaultSavingsProduct().nominalAnnualInterestRate(0.0), asset, control, income, expense))
                        .getResourceId();
                Long savingsId = createApproveActivateSavings(clientId, productId, DATE);
                deposit(savingsId, "100", DATE);
                Long definitionId = chargesHelper
                        .createCharge(
                                ChargeRequestBuilders.clientSpecifiedDueDateFee(50.0).incomeAccountId(income.getAccountID().longValue()))
                        .getResourceId();
                Long chargeId = chargesHelper
                        .addClientCharge(clientId, ChargeRequestBuilders.clientCharge(definitionId, DATE, BigDecimal.valueOf(50)))
                        .getResourceId();
                var payment = chargesHelper.payClientCharge(clientId, chargeId,
                        ChargeRequestBuilders.payClientCharge(DATE, 50).savingsAccountId(savingsId));
                var transaction = clientHelper.getClientTransaction(clientId, payment.getTransactionId());
                String savingsJournalId = "S" + transaction.getSavingsTransactionId();
                String clientJournalId = "C" + payment.getTransactionId();

                assertJournalPair(savingsJournalId, control.getAccountID().longValue(), clearingId);
                assertJournalPair(clientJournalId, clearingId, income.getAccountID().longValue());
                assertAmount("50", getSavingsDetails(savingsId).getSummary().getAccountBalance());
                assertAmount("0", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());

                clientHelper.undoClientTransaction(clientId, payment.getTransactionId());

                assertReversedJournalPair(savingsJournalId, control.getAccountID().longValue(), clearingId);
                assertReversedJournalPair(clientJournalId, clearingId, income.getAccountID().longValue());
                assertAmount("100", getSavingsDetails(savingsId).getSummary().getAccountBalance());
                assertAmount("50", chargesHelper.getClientCharge(clientId, chargeId).getAmountOutstanding());
            } finally {
                if (temporaryMappingId != null) {
                    mappingHelper.deleteMapping(temporaryMappingId);
                }
            }
        });
    }

    private void assertJournalPair(String transactionId, Long debitAccountId, Long creditAccountId) {
        var entries = journalEntryHelper.getJournalEntries(transactionId).getPageItems();
        assertEquals(2, entries.size());
        assertTrue(entries.stream().anyMatch(entry -> debitAccountId.equals(entry.getGlAccountId())
                && "DEBIT".equalsIgnoreCase(entry.getEntryType().getValue()) && Double.valueOf(50).equals(entry.getAmount())));
        assertTrue(entries.stream().anyMatch(entry -> creditAccountId.equals(entry.getGlAccountId())
                && "CREDIT".equalsIgnoreCase(entry.getEntryType().getValue()) && Double.valueOf(50).equals(entry.getAmount())));
    }

    private void assertReversedJournalPair(String transactionId, Long firstAccountId, Long secondAccountId) {
        var entries = journalEntryHelper.getJournalEntries(transactionId).getPageItems();
        assertEquals(4, entries.size());
        for (Long accountId : java.util.List.of(firstAccountId, secondAccountId)) {
            var accountEntries = entries.stream().filter(entry -> accountId.equals(entry.getGlAccountId())).toList();
            assertEquals(2, accountEntries.size());
            assertEquals(0.0,
                    accountEntries.stream().mapToDouble(
                            entry -> "DEBIT".equalsIgnoreCase(entry.getEntryType().getValue()) ? entry.getAmount() : -entry.getAmount())
                            .sum());
        }
    }

    @Test
    void paysPartiallyFromSavingsAndUndoesBothLegs() {
        runAt(DATE, () -> {
            Long clientId = createClient(DATE);
            Long productId = createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().nominalAnnualInterestRate(0.0))
                    .getResourceId();
            Long savingsId = createApproveActivateSavings(clientId, productId, DATE);
            deposit(savingsId, "100", DATE);
            Long definitionId = chargesHelper.createClientSpecifiedDueDateCharge(50.0).getResourceId();
            Long chargeId = chargesHelper
                    .addClientCharge(clientId, ChargeRequestBuilders.clientCharge(definitionId, DATE, BigDecimal.valueOf(50)))
                    .getResourceId();

            PostClientsClientIdChargesChargeIdResponse payment = chargesHelper.payClientCharge(clientId, chargeId,
                    ChargeRequestBuilders.payClientCharge(DATE, 20).savingsAccountId(savingsId));

            assertEquals(savingsId, payment.getSavingsId());
            assertAmount("80", getSavingsDetails(savingsId).getSummary().getAccountBalance());
            var charge = chargesHelper.getClientCharges(clientId).getPageItems().iterator().next();
            assertAmount("30", charge.getAmountOutstanding());
            var transaction = clientHelper.getClientTransaction(clientId, payment.getTransactionId());
            assertEquals(savingsId, transaction.getSavingsAccountId());
            assertNotNull(transaction.getSavingsTransactionId());

            clientHelper.undoClientTransaction(clientId, payment.getTransactionId());

            assertAmount("100", getSavingsDetails(savingsId).getSummary().getAccountBalance());
            assertAmount("50", chargesHelper.getClientCharges(clientId).getPageItems().iterator().next().getAmountOutstanding());
            var reversed = clientHelper.getClientTransaction(clientId, payment.getTransactionId());
            assertTrue(reversed.getReversed());
            assertEquals(transaction.getSavingsTransactionId(), reversed.getSavingsTransactionId());
        });
    }

    @Test
    void insufficientFundsLeaveClientChargeUnpaid() {
        runAt(DATE, () -> {
            Long clientId = createClient(DATE);
            Long productId = createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().nominalAnnualInterestRate(0.0))
                    .getResourceId();
            Long savingsId = createApproveActivateSavings(clientId, productId, DATE);
            Long definitionId = chargesHelper.createClientSpecifiedDueDateCharge(50.0).getResourceId();
            Long chargeId = chargesHelper
                    .addClientCharge(clientId, ChargeRequestBuilders.clientCharge(definitionId, DATE, BigDecimal.valueOf(50)))
                    .getResourceId();

            var failure = chargesHelper.payClientChargeExpectingError(clientId, chargeId,
                    ChargeRequestBuilders.payClientCharge(DATE, 20).savingsAccountId(savingsId));

            assertEquals(403, failure.getStatus());
            assertTrue(failure.getResponseBody().contains("error.msg.savingsaccount.transaction.insufficient.account.balance"));
            assertAmount("50", chargesHelper.getClientCharges(clientId).getPageItems().iterator().next().getAmountOutstanding());
            assertAmount("0", getSavingsDetails(savingsId).getSummary().getAccountBalance());
        });
    }

    private void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
