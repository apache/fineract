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
package org.apache.fineract.integrationtests.investor.externalassetowner;

import static org.apache.fineract.client.models.ExternalTransferData.StatusEnum.ACTIVE;
import static org.apache.fineract.client.models.ExternalTransferData.StatusEnum.CANCELLED;
import static org.apache.fineract.client.models.ExternalTransferData.StatusEnum.DECLINED;
import static org.apache.fineract.client.models.ExternalTransferData.StatusEnum.PENDING;
import static org.apache.fineract.client.models.ExternalTransferData.SubStatusEnum.BALANCE_ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.UUID;
import org.apache.fineract.client.models.ExternalTransferData;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for owner-to-owner (external-to-external) asset transfer functionality.
 *
 * Tests the flow where a loan currently owned by External Owner A is sold directly to External Owner B without a
 * buyback-then-sale cycle.
 */
public class ExternalAssetOwnerToOwnerTransferTest extends ExternalAssetOwnerTransferTest {

    @Test
    public void saleFromOwnerAToOwnerBWithoutDelayedSettlement() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(java.time.LocalDate.parse("2020-03-02"));

            // Step 1: Create client and loan
            Integer clientID = createClient();
            Integer loanID = createLoanForClient(clientID, "02 March 2020");
            addPenaltyForLoan(loanID, "10");

            // Step 2: Sell loan to Owner A
            String ownerAExternalId = UUID.randomUUID().toString();
            String saleATransferExternalId = UUID.randomUUID().toString();
            PostInitiateTransferResponse saleAResponse = createSaleTransfer(loanID, "2020-03-02", saleATransferExternalId,
                    UUID.randomUUID().toString(), ownerAExternalId, "1.0");
            validateResponse(saleAResponse, loanID);

            // Step 3: Execute COB to activate Owner A's transfer
            updateBusinessDateAndExecuteCOBJob("2020-03-03");

            // Verify Owner A is now ACTIVE
            getAndValidateExternalAssetOwnerTransferByLoan(loanID,
                    ExpectedExternalTransferData.expected(PENDING, saleATransferExternalId, "2020-03-02", "2020-03-02", "2020-03-02", false,
                            new BigDecimal("15767.420000"), new BigDecimal("15000.000000"), new BigDecimal("757.420000"),
                            new BigDecimal("10.000000"), new BigDecimal("0.000000"), new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(ACTIVE, saleATransferExternalId, "2020-03-02", "2020-03-03", "9999-12-31", true,
                            new BigDecimal("15767.420000"), new BigDecimal("15000.000000"), new BigDecimal("757.420000"),
                            new BigDecimal("10.000000"), new BigDecimal("0.000000"), new BigDecimal("0.000000")));
            getAndValidateThereIsActiveMapping(loanID);

            // Step 4: Sell loan from Owner A to Owner B (owner-to-owner transfer)
            // Settlement date = current business date (2020-03-03), COB will process it on next day
            String ownerBExternalId = UUID.randomUUID().toString();
            String saleBTransferExternalId = UUID.randomUUID().toString();
            PostInitiateTransferResponse saleBResponse = createSaleTransfer(loanID, "2020-03-03", saleBTransferExternalId,
                    UUID.randomUUID().toString(), ownerBExternalId, "1.0");
            validateResponse(saleBResponse, loanID);

            // Verify the new PENDING transfer was created for Owner B
            PageExternalTransferData transfers = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanID.longValue());
            assertNotNull(transfers.getContent());

            // Find the new PENDING transfer for Owner B
            ExternalTransferData pendingTransferB = transfers.getContent().stream()
                    .filter(t -> saleBTransferExternalId.equals(t.getTransferExternalId()) && PENDING.equals(t.getStatus())).findFirst()
                    .orElse(null);
            assertNotNull(pendingTransferB, "PENDING transfer for Owner B should exist");

            // Step 5: Execute COB to activate Owner B's transfer (expires Owner A, activates Owner B)
            updateBusinessDateAndExecuteCOBJob("2020-03-04");

            // Verify final state: Owner A's ACTIVE transfer is expired, Owner B is now ACTIVE
            transfers = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanID.longValue());
            assertNotNull(transfers.getContent());

            // Owner A's ACTIVE transfer should now have effectiveDateTo = 2020-03-03 (settlement date of Owner B)
            ExternalTransferData expiredOwnerATransfer = transfers.getContent().stream()
                    .filter(t -> saleATransferExternalId.equals(t.getTransferExternalId()) && ACTIVE.equals(t.getStatus())).findFirst()
                    .orElse(null);
            assertNotNull(expiredOwnerATransfer, "Expired ACTIVE transfer for Owner A should exist");
            assertEquals(java.time.LocalDate.parse("2020-03-03"), expiredOwnerATransfer.getEffectiveTo(),
                    "Owner A's ACTIVE transfer should be expired to the settlement date of Owner B's transfer");

            // Owner B should have an ACTIVE transfer with effectiveDateTo = 9999-12-31
            ExternalTransferData activeOwnerBTransfer = transfers.getContent().stream()
                    .filter(t -> saleBTransferExternalId.equals(t.getTransferExternalId()) && ACTIVE.equals(t.getStatus())).findFirst()
                    .orElse(null);
            assertNotNull(activeOwnerBTransfer, "ACTIVE transfer for Owner B should exist");
            assertEquals(java.time.LocalDate.parse("9999-12-31"), activeOwnerBTransfer.getEffectiveTo(),
                    "Owner B's ACTIVE transfer should have open-ended effectiveTo");
            assertNotNull(activeOwnerBTransfer.getDetails(), "ACTIVE transfer should have details");

            // Verify active mapping now points to Owner B (use direct API, not getAndValidateThereIsActiveMapping
            // which assumes only one ACTIVE transfer)
            ExternalTransferData activeTransfer = EXTERNAL_ASSET_OWNER_HELPER.retrieveActiveTransferByLoanId(loanID.longValue());
            assertNotNull(activeTransfer, "There should be an active transfer mapping");
            assertEquals(saleBTransferExternalId, activeTransfer.getTransferExternalId(),
                    "Active mapping should point to Owner B's transfer");
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void saleFromOwnerAToOwnerBToOwnerCChainedTransfers() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(java.time.LocalDate.parse("2020-03-02"));

            // Setup
            Integer clientID = createClient();
            Integer loanID = createLoanForClient(clientID, "02 March 2020");
            addPenaltyForLoan(loanID, "10");

            // Sell to Owner A and activate via COB
            String ownerAExternalId = UUID.randomUUID().toString();
            String saleAExternalId = UUID.randomUUID().toString();
            createSaleTransfer(loanID, "2020-03-02", saleAExternalId, UUID.randomUUID().toString(), ownerAExternalId, "1.0");
            updateBusinessDateAndExecuteCOBJob("2020-03-03");
            getAndValidateThereIsActiveMapping(loanID);

            // Sell from Owner A to Owner B (settlementDate = current business date 2020-03-03)
            String ownerBExternalId = UUID.randomUUID().toString();
            String saleBExternalId = UUID.randomUUID().toString();
            createSaleTransfer(loanID, "2020-03-03", saleBExternalId, UUID.randomUUID().toString(), ownerBExternalId, "1.0");
            updateBusinessDateAndExecuteCOBJob("2020-03-04");

            // Verify Owner B is active
            ExternalTransferData activeTransfer = EXTERNAL_ASSET_OWNER_HELPER.retrieveActiveTransferByLoanId(loanID.longValue());
            assertEquals(saleBExternalId, activeTransfer.getTransferExternalId());

            // Sell from Owner B to Owner C (settlementDate = current business date 2020-03-04)
            String ownerCExternalId = UUID.randomUUID().toString();
            String saleCExternalId = UUID.randomUUID().toString();
            createSaleTransfer(loanID, "2020-03-04", saleCExternalId, UUID.randomUUID().toString(), ownerCExternalId, "1.0");
            updateBusinessDateAndExecuteCOBJob("2020-03-05");

            // Verify Owner C is now active
            activeTransfer = EXTERNAL_ASSET_OWNER_HELPER.retrieveActiveTransferByLoanId(loanID.longValue());
            assertEquals(saleCExternalId, activeTransfer.getTransferExternalId(),
                    "Owner C should be the active owner after chained transfers");

            // Verify Owner B's transfer is expired
            PageExternalTransferData allTransfers = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanID.longValue());
            ExternalTransferData ownerBActive = allTransfers.getContent().stream()
                    .filter(t -> saleBExternalId.equals(t.getTransferExternalId()) && ACTIVE.equals(t.getStatus())).findFirst()
                    .orElse(null);
            assertNotNull(ownerBActive);
            assertEquals(java.time.LocalDate.parse("2020-03-04"), ownerBActive.getEffectiveTo(),
                    "Owner B's ACTIVE transfer should be expired to Owner C's settlement date");
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void ownerToOwnerPendingCancelledBeforeCOBKeepsOriginalOwnerIntact() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(java.time.LocalDate.parse("2020-03-02"));

            // Setup: create loan and sell to Owner A
            Integer clientID = createClient();
            Integer loanID = createLoanForClient(clientID, "02 March 2020");
            addPenaltyForLoan(loanID, "10");

            String ownerAExternalId = UUID.randomUUID().toString();
            String saleAExternalId = UUID.randomUUID().toString();
            createSaleTransfer(loanID, "2020-03-02", saleAExternalId, UUID.randomUUID().toString(), ownerAExternalId, "1.0");
            updateBusinessDateAndExecuteCOBJob("2020-03-03");
            getAndValidateThereIsActiveMapping(loanID);

            // Initiate owner-to-owner sale to Owner B
            String ownerBExternalId = UUID.randomUUID().toString();
            String saleBExternalId = UUID.randomUUID().toString();
            PostInitiateTransferResponse saleBResponse = createSaleTransfer(loanID, "2020-03-03", saleBExternalId,
                    UUID.randomUUID().toString(), ownerBExternalId, "1.0");
            validateResponse(saleBResponse, loanID);

            // Cancel the PENDING transfer for Owner B before COB runs
            EXTERNAL_ASSET_OWNER_HELPER.cancelTransferByTransferExternalId(saleBExternalId);

            // Verify Owner A is still the active owner — ACTIVE transfer with effectiveDateTo = 9999-12-31
            ExternalTransferData activeTransfer = EXTERNAL_ASSET_OWNER_HELPER.retrieveActiveTransferByLoanId(loanID.longValue());
            assertNotNull(activeTransfer, "Owner A should still be the active owner after cancel");
            assertEquals(saleAExternalId, activeTransfer.getTransferExternalId(),
                    "Active mapping should still point to Owner A after cancel");

            // Verify Owner A's ACTIVE transfer is untouched (effectiveDateTo = 9999-12-31)
            PageExternalTransferData transfers = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanID.longValue());
            ExternalTransferData ownerAActive = transfers.getContent().stream()
                    .filter(t -> saleAExternalId.equals(t.getTransferExternalId()) && ACTIVE.equals(t.getStatus())
                            && java.time.LocalDate.parse("9999-12-31").equals(t.getEffectiveTo()))
                    .findFirst().orElse(null);
            assertNotNull(ownerAActive, "Owner A's ACTIVE transfer should still have open-ended effectiveTo");

            // Verify Owner B's transfer is CANCELLED
            ExternalTransferData ownerBCancelled = transfers.getContent().stream()
                    .filter(t -> saleBExternalId.equals(t.getTransferExternalId()) && CANCELLED.equals(t.getStatus())).findFirst()
                    .orElse(null);
            assertNotNull(ownerBCancelled, "Owner B's transfer should be CANCELLED");
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void ownerToOwnerPendingDeclinedInCOBKeepsOriginalOwnerIntact() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(java.time.LocalDate.parse("2020-03-02"));

            // Setup: create loan and sell to Owner A
            Integer clientID = createClient();
            Integer loanID = createLoanForClient(clientID, "02 March 2020");
            addPenaltyForLoan(loanID, "10");

            String ownerAExternalId = UUID.randomUUID().toString();
            String saleAExternalId = UUID.randomUUID().toString();
            createSaleTransfer(loanID, "2020-03-02", saleAExternalId, UUID.randomUUID().toString(), ownerAExternalId, "1.0");
            updateBusinessDateAndExecuteCOBJob("2020-03-03");
            getAndValidateThereIsActiveMapping(loanID);

            // Initiate owner-to-owner sale to Owner B with future settlement date
            String ownerBExternalId = UUID.randomUUID().toString();
            String saleBExternalId = UUID.randomUUID().toString();
            createSaleTransfer(loanID, "2020-03-06", saleBExternalId, UUID.randomUUID().toString(), ownerBExternalId, "1.0");

            // Write off the loan — this will trigger decline when COB processes the PENDING transfer
            updateBusinessDateAndExecuteCOBJob("2020-03-04");
            LOAN_TRANSACTION_HELPER.writeOffLoan("04 March 2020", loanID);

            // Verify Owner A is still the active owner (PENDING for Owner B should be declined, not yet settled)
            ExternalTransferData activeTransfer = EXTERNAL_ASSET_OWNER_HELPER.retrieveActiveTransferByLoanId(loanID.longValue());
            assertNotNull(activeTransfer, "Owner A should still be the active owner before settlement date");
            assertEquals(saleAExternalId, activeTransfer.getTransferExternalId(), "Active mapping should still point to Owner A");

            // Verify Owner B's PENDING transfer is declined
            PageExternalTransferData transfers = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanID.longValue());
            ExternalTransferData ownerBDeclined = transfers.getContent().stream()
                    .filter(t -> saleBExternalId.equals(t.getTransferExternalId()) && DECLINED.equals(t.getStatus())).findFirst()
                    .orElse(null);
            assertNotNull(ownerBDeclined, "Owner B's transfer should be DECLINED");
            assertEquals(BALANCE_ZERO, ownerBDeclined.getSubStatus(), "Decline reason should be BALANCE_ZERO");

            // Verify Owner A's ACTIVE transfer is still open-ended
            ExternalTransferData ownerAActive = transfers.getContent().stream()
                    .filter(t -> saleAExternalId.equals(t.getTransferExternalId()) && ACTIVE.equals(t.getStatus())
                            && java.time.LocalDate.parse("9999-12-31").equals(t.getEffectiveTo()))
                    .findFirst().orElse(null);
            assertNotNull(ownerAActive, "Owner A's ACTIVE transfer should still have open-ended effectiveTo after decline");
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }
}
