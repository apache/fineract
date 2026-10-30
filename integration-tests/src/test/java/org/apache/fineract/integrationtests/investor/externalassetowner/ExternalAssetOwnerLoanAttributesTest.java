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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AuditData;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.ExternalAssetOwnerLoanAttributeData;
import org.apache.fineract.client.models.ExternalAssetOwnerLoanAttributesData;
import org.apache.fineract.client.models.ExternalTransferData;
import org.apache.fineract.client.models.ExternalTransferLoanProductAttributesData;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PageExternalTransferLoanProductAttributesData;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessStepHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalAssetOwnerHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignMakerCheckerHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The external asset owner attributes in force when a loan is first sold are copied to the loan and govern it from then
 * on, whatever later happens to the product. Ownership checks assert both surfaces (journal entry owner and event
 * externalOwnerId) for the same transaction, as ExternalAssetOwnerExcludedTransactionTypesTest does.
 */
public class ExternalAssetOwnerLoanAttributesTest extends ExternalAssetOwnerTransferTest {

    private static final String EXCLUDED_TRANSACTION_TYPES = "EXCLUDED_TRANSACTION_TYPES";
    private static final String BUY_DOWN_FEE_AMORTIZATION_STRATEGY = "BUY_DOWN_FEE_AMORTIZATION_STRATEGY";
    private static final String CAPITALIZED_INCOME_AMORTIZATION_STRATEGY = "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY";
    private static final String SETTLEMENT_MODEL = "SETTLEMENT_MODEL";
    private static final String BDF_TYPES = "BUY_DOWN_FEE,BUY_DOWN_FEE_ADJUSTMENT,BUY_DOWN_FEE_AMORTIZATION,BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT";
    private static final String IMMEDIATE = "IMMEDIATE";
    private static final String DEFERRED = "DEFERRED";
    private static final String DELAYED_SETTLEMENT = "DELAYED_SETTLEMENT";
    private static final String DEFAULT_SETTLEMENT = "DEFAULT_SETTLEMENT";

    private static final String INVALID_ATTRIBUTE_VALUE = "error.msg.externalAssetOwnerLoanProductAttribute.invalidAttributeValue";
    private static final String INVALID_SETTLEMENT_ATTRIBUTE = "error.msg.externalAssetOwnerLoanProductAttribute.invalidSettlementAttribute";
    private static final String UNSUPPORTED_PARAMETER = "error.msg.parameter.unsupported";
    private static final String LOAN_NOT_FOUND = "error.msg.loan.id.invalid";
    private static final String LOAN_EXTERNAL_ID_NOT_FOUND = "error.msg.loan.external.id.invalid";
    private static final String NOT_AUTHORIZED = "error.msg.not.authorized";
    private static final String NO_LOAN_LEVEL_COPY = "error.msg.externalAssetOwnerLoanAttribute.noLoanLevelCopy";
    private static final String SETTLEMENT_MODEL_TRANSFER_IN_PROGRESS = "error.msg.externalAssetOwnerLoanAttribute.settlementModelTransferInProgress";

    private static final String UPDATE_LOAN_ATTRIBUTE_PERMISSION = "UPDATE_EXTERNAL_ASSET_OWNER_LOAN_ATTRIBUTE";
    private static final String UPDATE_LOAN_PRODUCT_ATTRIBUTE_PERMISSION = "UPDATE_EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE";
    private static final String PASSWORD = "A1b2c3d4e5f$";

    private static final List<String> EVENTS_UNDER_TEST = List.of("LoanBuyDownFeeTransactionCreatedBusinessEvent",
            "LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent", "LoanTransactionMakeRepaymentPostBusinessEvent");

    private static Account assetAccount;
    private static Account incomeAccount;
    private static Account expenseAccount;
    private static Account liabilityAccount;

    @BeforeAll
    public static void createGlAccounts() {
        assetAccount = accountHelper.createAssetAccount(Utils.uniqueRandomStringGenerator("ASSET_", 5));
        incomeAccount = accountHelper.createIncomeAccount(Utils.uniqueRandomStringGenerator("INCOME_", 5));
        expenseAccount = accountHelper.createExpenseAccount(Utils.uniqueRandomStringGenerator("EXPENSE_", 5));
        liabilityAccount = accountHelper.createLiabilityAccount(Utils.uniqueRandomStringGenerator("LIABILITY_", 5));
    }

    @AfterAll
    public static void disableEventsUnderTest() {
        EVENTS_UNDER_TEST.forEach(externalEventHelper::disableBusinessEvent);
    }

    @BeforeEach
    public void beforeEach() {
        new FeignBusinessStepHelper(FineractFeignClientHelper.getFineractFeignClient()).updateSteps("LOAN_CLOSE_OF_BUSINESS",
                "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION", "CHECK_LOAN_REPAYMENT_DUE",
                "CHECK_LOAN_REPAYMENT_OVERDUE", "CHECK_DUE_INSTALLMENTS", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "ACCRUAL_ACTIVITY_POSTING", "CAPITALIZED_INCOME_AMORTIZATION", "BUY_DOWN_FEE_AMORTIZATION", "LOAN_INTEREST_RECALCULATION",
                "EXTERNAL_ASSET_OWNER_TRANSFER");
        EVENTS_UNDER_TEST.forEach(externalEventHelper::enableBusinessEvent);
        externalEventHelper.deleteAllExternalEvents();
    }

    @Test
    public void firstSaleCreatesLoanLevelCopyOfConfiguredProductAttributes() {
        final Long clientId = createClient();
        final Long productId = createProductWithAttributes(
                attributes(EXCLUDED_TRANSACTION_TYPES, BDF_TYPES, BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE));
        final String loanExternalId = UUID.randomUUID().toString();
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> saleTransferIdRef = new AtomicReference<>();

        runAt("01 July 2026", () -> {
            loanIdRef.set(createDisbursedLoan(clientId, productId, loanExternalId));
            assertNoLoanLevelCopy(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanIdRef.get()), loanIdRef.get());
            saleTransferIdRef.set(sell(loanIdRef.get(), UUID.randomUUID().toString(), "2026-07-01"));
        });

        runAt("02 July 2026", () -> {
            executeInlineCOB(loanIdRef.get());

            final ExternalAssetOwnerLoanAttributesData byId = EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanIdRef.get());
            assertLoanLevelCopy(byId, loanIdRef.get(), saleTransferIdRef.get(),
                    attributes(EXCLUDED_TRANSACTION_TYPES, BDF_TYPES, BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE));
            assertEquals(loanExternalId, byId.getLoanExternalId());

            final ExternalAssetOwnerLoanAttributesData byExternalId = EXTERNAL_ASSET_OWNER_HELPER
                    .retrieveLoanAttributesByLoanExternalId(loanExternalId);
            assertEquals(byId, byExternalId, "GET by loan external id must return the same body as GET by loan id");
        });
    }

    @Test
    public void productChangeAfterSaleDoesNotAffectLoanWithCopy() {
        final Long clientId = createClient();
        final Long productId = createProductWithAttributes(
                attributes(EXCLUDED_TRANSACTION_TYPES, BDF_TYPES, BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE));
        final String ownerExternalId = UUID.randomUUID().toString();
        final String controlOwnerExternalId = UUID.randomUUID().toString();
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> controlLoanIdRef = new AtomicReference<>();
        final AtomicReference<Long> controlSaleTransferIdRef = new AtomicReference<>();

        runAt("01 July 2026", () -> {
            loanIdRef.set(createDisbursedLoan(clientId, productId, null));
            controlLoanIdRef.set(createDisbursedLoan(clientId, productId, null));
            sell(loanIdRef.get(), ownerExternalId, "2026-07-01");
        });

        runAt("02 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            deleteProductAttribute(productId, EXCLUDED_TRANSACTION_TYPES);
            updateProductAttribute(productId, BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED);

            final Long buyDownFeeTxId = makeLoanBuyDownFee(loanId, "02 July 2026", 50.0).getResourceId();
            assertUntaggedOnBothSurfaces(buyDownFeeTxId);
            final Long amortizationTxId = assertSingleBuyDownFeeAmortization(loanId, LocalDate.of(2026, 7, 2), 50.0);
            assertUntaggedOnBothSurfaces(amortizationTxId);

            final Long repaymentTxId = makeLoanRepayment(loanId, "repayment", "02 July 2026", 100.0).getResourceId();
            assertTaggedOnBothSurfaces(repaymentTxId, ownerExternalId);

            controlSaleTransferIdRef.set(sell(controlLoanIdRef.get(), controlOwnerExternalId, "2026-07-02"));
        });

        runAt("03 July 2026", () -> {
            final Long controlLoanId = controlLoanIdRef.get();
            executeInlineCOB(controlLoanId);

            final Long controlBuyDownFeeTxId = makeLoanBuyDownFee(controlLoanId, "03 July 2026", 50.0).getResourceId();
            assertTaggedOnBothSurfaces(controlBuyDownFeeTxId, controlOwnerExternalId);
            assertNoBuyDownFeeAmortization(controlLoanId);

            assertLoanLevelCopy(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(controlLoanId), controlLoanId,
                    controlSaleTransferIdRef.get(), attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED));
        });
    }

    @Test
    public void resaleAndBuybackKeepTheOriginalCopy() {
        final Long clientId = createClient();
        final Long productId = createProductWithAttributes(
                attributes(EXCLUDED_TRANSACTION_TYPES, BDF_TYPES, BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE));
        final Map<String, String> originalCopy = attributes(EXCLUDED_TRANSACTION_TYPES, BDF_TYPES, BUY_DOWN_FEE_AMORTIZATION_STRATEGY,
                IMMEDIATE);
        final String ownerBExternalId = UUID.randomUUID().toString();
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> saleATransferIdRef = new AtomicReference<>();

        runAt("01 July 2026", () -> {
            loanIdRef.set(createDisbursedLoan(clientId, productId, null));
            saleATransferIdRef.set(sell(loanIdRef.get(), UUID.randomUUID().toString(), "2026-07-01"));
        });

        runAt("02 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            updateProductAttribute(productId, EXCLUDED_TRANSACTION_TYPES, "REPAYMENT");
            sell(loanId, ownerBExternalId, "2026-07-02");
        });

        runAt("03 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            assertEquals(ownerBExternalId, EXTERNAL_ASSET_OWNER_HELPER.retrieveActiveTransferByLoanId(loanId).getOwner().getExternalId(),
                    "owner B must hold the loan after the resale");

            final Long repaymentTxId = makeLoanRepayment(loanId, "repayment", "03 July 2026", 100.0).getResourceId();
            assertTaggedOnBothSurfaces(repaymentTxId, ownerBExternalId);

            assertLoanLevelCopy(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanId), loanId, saleATransferIdRef.get(), originalCopy);
            createBuybackTransfer(loanId, "2026-07-03");
        });

        runAt("04 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            assertNull(EXTERNAL_ASSET_OWNER_HELPER.retrieveActiveTransferByLoanId(loanId), "the buyback must end the ownership");

            assertLoanLevelCopy(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanId), loanId, saleATransferIdRef.get(), originalCopy);
        });
    }

    @Test
    public void saleBuybackAndNewSaleReuseTheOriginalCopy() {
        final Long clientId = createClient();
        final Long productId = createProductWithAttributes(
                attributes(EXCLUDED_TRANSACTION_TYPES, BDF_TYPES, BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE));
        final String ownerBExternalId = UUID.randomUUID().toString();
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> saleATransferIdRef = new AtomicReference<>();

        runAt("01 July 2026", () -> {
            loanIdRef.set(createDisbursedLoan(clientId, productId, null));
            saleATransferIdRef.set(sell(loanIdRef.get(), UUID.randomUUID().toString(), "2026-07-01"));
        });

        runAt("02 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            updateProductAttribute(productId, BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED);
            deleteProductAttribute(productId, EXCLUDED_TRANSACTION_TYPES);
            createBuybackTransfer(loanId, "2026-07-02");
        });

        runAt("03 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            assertNull(EXTERNAL_ASSET_OWNER_HELPER.retrieveActiveTransferByLoanId(loanId), "the buyback must end the ownership");
            sell(loanId, ownerBExternalId, "2026-07-03");
        });

        runAt("04 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            assertEquals(ownerBExternalId, EXTERNAL_ASSET_OWNER_HELPER.retrieveActiveTransferByLoanId(loanId).getOwner().getExternalId(),
                    "owner B must hold the loan after the new sale");

            final Long buyDownFeeTxId = makeLoanBuyDownFee(loanId, "04 July 2026", 50.0).getResourceId();
            assertUntaggedOnBothSurfaces(buyDownFeeTxId);
            final Long amortizationTxId = assertSingleBuyDownFeeAmortization(loanId, LocalDate.of(2026, 7, 4), 50.0);
            assertUntaggedOnBothSurfaces(amortizationTxId);

            assertLoanLevelCopy(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanId), loanId, saleATransferIdRef.get(),
                    attributes(EXCLUDED_TRANSACTION_TYPES, BDF_TYPES, BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE));
        });
    }

    @Test
    public void loanWithoutCopyGetsOneFromTheProductValuesAtSaleExecution() {
        final Long clientId = createClient();
        final Long productId = createProductWithAttributes(attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED));
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> saleTransferIdRef = new AtomicReference<>();

        runAt("01 July 2026", () -> {
            loanIdRef.set(createDisbursedLoan(clientId, productId, null));
            assertNoLoanLevelCopy(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanIdRef.get()), loanIdRef.get());
            updateProductAttribute(productId, BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE);
            saleTransferIdRef.set(sell(loanIdRef.get(), UUID.randomUUID().toString(), "2026-07-01"));
        });

        runAt("02 July 2026", () -> {
            executeInlineCOB(loanIdRef.get());
            assertLoanLevelCopy(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanIdRef.get()), loanIdRef.get(),
                    saleTransferIdRef.get(), attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE));
        });
    }

    // The intermediary leg freezes DELAYED_SETTLEMENT, so a product flip to DEFAULT_SETTLEMENT does not break the
    // investor leg.
    @Test
    public void delayedSettlementIsFrozenOnTheLoanAtTheIntermediarySale() {
        final Long clientId = createClient();
        final Long productId = createProductWithAttributes(attributes(SETTLEMENT_MODEL, DELAYED_SETTLEMENT));
        final String intermediaryExternalId = UUID.randomUUID().toString();
        final String investorExternalId = UUID.randomUUID().toString();
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> intermediarySaleTransferIdRef = new AtomicReference<>();

        runAt("01 July 2026", () -> {
            loanIdRef.set(createDisbursedLoan(clientId, productId, null));
            intermediarySaleTransferIdRef
                    .set(createIntermediarySaleTransfer(loanIdRef.get(), "2026-07-01", intermediaryExternalId).getResourceId());
        });

        runAt("02 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            assertEquals(ExternalTransferData.StatusEnum.ACTIVE_INTERMEDIATE,
                    EXTERNAL_ASSET_OWNER_HELPER.retrieveActiveTransferByLoanId(loanId).getStatus());

            updateProductAttribute(productId, SETTLEMENT_MODEL, DEFAULT_SETTLEMENT);
            assertDoesNotThrow(() -> createSaleTransfer(loanId, "2026-07-02", UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                    investorExternalId, "1.0"), "the investor-leg sale must follow the frozen DELAYED_SETTLEMENT, not the product");
        });

        runAt("03 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            final List<ExternalTransferData> transfers = transfersInCreationOrder(loanId);
            assertEquals(
                    List.of(ExternalTransferData.StatusEnum.PENDING_INTERMEDIATE, ExternalTransferData.StatusEnum.ACTIVE_INTERMEDIATE,
                            ExternalTransferData.StatusEnum.PENDING, ExternalTransferData.StatusEnum.ACTIVE),
                    transfers.stream().map(ExternalTransferData::getStatus).toList());
            final ExternalTransferData active = transfers.getLast();
            assertEquals(investorExternalId, active.getOwner().getExternalId());
            assertEquals(intermediaryExternalId, active.getPreviousOwner().getExternalId(),
                    "the investor leg must record the intermediary as the previous owner");

            assertLoanLevelCopy(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanId), loanId, intermediarySaleTransferIdRef.get(),
                    attributes(SETTLEMENT_MODEL, DELAYED_SETTLEMENT));
        });
    }

    @Test
    public void loanLevelCorrectionAffectsOnlyThisLoanAndOnlyLaterActivity() {
        final Long clientId = createClient();
        final Long productId = createProductWithAttributes(attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED));
        final String ownerAExternalId = UUID.randomUUID().toString();
        final String ownerBExternalId = UUID.randomUUID().toString();
        final AtomicReference<Long> loanARef = new AtomicReference<>();
        final AtomicReference<Long> loanBRef = new AtomicReference<>();
        final AtomicReference<Long> saleBTransferIdRef = new AtomicReference<>();

        runAt("01 July 2026", () -> {
            loanARef.set(createDisbursedLoan(clientId, productId, null));
            loanBRef.set(createDisbursedLoan(clientId, productId, null));
            sell(loanARef.get(), ownerAExternalId, "2026-07-01");
            saleBTransferIdRef.set(sell(loanBRef.get(), ownerBExternalId, "2026-07-01"));
        });

        runAt("02 July 2026", () -> {
            final Long loanA = loanARef.get();
            final Long loanB = loanBRef.get();
            executeInlineCOB(List.of(loanA, loanB));

            final Long firstRepaymentTxId = makeLoanRepayment(loanA, "repayment", "02 July 2026", 100.0).getResourceId();
            assertTaggedOnBothSurfaces(firstRepaymentTxId, ownerAExternalId);

            final CommandProcessingResult result = EXTERNAL_ASSET_OWNER_HELPER.updateLoanAttribute(loanA, EXCLUDED_TRANSACTION_TYPES,
                    "REPAYMENT , BUY_DOWN_FEE");
            assertEquals(loanA, result.getResourceId());
            assertLoanLevelAttributes(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanA),
                    attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED, EXCLUDED_TRANSACTION_TYPES, "REPAYMENT,BUY_DOWN_FEE"));

            final Long secondRepaymentTxId = makeLoanRepayment(loanA, "repayment", "02 July 2026", 50.0).getResourceId();
            assertUntaggedOnBothSurfaces(secondRepaymentTxId);
            assertJournalEntriesTagged(firstRepaymentTxId, ownerAExternalId);

            assertProductAttributes(productId, attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED));

            final Long siblingRepaymentTxId = makeLoanRepayment(loanB, "repayment", "02 July 2026", 100.0).getResourceId();
            assertTaggedOnBothSurfaces(siblingRepaymentTxId, ownerBExternalId);
            assertLoanLevelCopy(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanB), loanB, saleBTransferIdRef.get(),
                    attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED));

            EXTERNAL_ASSET_OWNER_HELPER.updateLoanAttribute(loanA, CAPITALIZED_INCOME_AMORTIZATION_STRATEGY, "immediate");
            assertLoanLevelAttributes(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanA),
                    attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED, EXCLUDED_TRANSACTION_TYPES, "REPAYMENT,BUY_DOWN_FEE",
                            CAPITALIZED_INCOME_AMORTIZATION_STRATEGY, IMMEDIATE));
        });
    }

    @Test
    public void loanLevelCorrectionIsValidatedLikeTheProductAttributeAndNeedsACopy() {
        final Long clientId = createClient();
        final Long productId = createProductWithAttributes(attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED));
        final AtomicReference<Long> soldLoanRef = new AtomicReference<>();
        final AtomicReference<Long> unsoldLoanRef = new AtomicReference<>();

        runAt("01 July 2026", () -> {
            soldLoanRef.set(createDisbursedLoan(clientId, productId, null));
            unsoldLoanRef.set(createDisbursedLoan(clientId, productId, null));
            sell(soldLoanRef.get(), UUID.randomUUID().toString(), "2026-07-01");
        });

        runAt("02 July 2026", () -> {
            final Long soldLoan = soldLoanRef.get();
            final Long unsoldLoan = unsoldLoanRef.get();
            executeInlineCOB(List.of(soldLoan, unsoldLoan));

            assertRejected(
                    EXTERNAL_ASSET_OWNER_HELPER.updateLoanAttributeFailure(soldLoan, BUY_DOWN_FEE_AMORTIZATION_STRATEGY, "SOMETIMES"), 403,
                    INVALID_ATTRIBUTE_VALUE);
            assertRejected(EXTERNAL_ASSET_OWNER_HELPER.updateLoanAttributeFailure(soldLoan, "NOT_AN_ATTRIBUTE", IMMEDIATE), 403,
                    INVALID_SETTLEMENT_ATTRIBUTE);
            final Map<String, Object> unsupportedBody = FeignExternalAssetOwnerHelper
                    .loanAttributeRequest(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE);
            unsupportedBody.put("unsupportedParameter", "x");
            assertRejected(EXTERNAL_ASSET_OWNER_HELPER.updateLoanAttributeFailure(soldLoan, unsupportedBody), 400, UNSUPPORTED_PARAMETER);
            assertLoanLevelAttributes(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(soldLoan),
                    attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED));

            assertRejected(
                    EXTERNAL_ASSET_OWNER_HELPER.updateLoanAttributeFailure(unsoldLoan, BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE), 403,
                    NO_LOAN_LEVEL_COPY);
            assertNoLoanLevelCopy(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(unsoldLoan), unsoldLoan);

            assertRejected(
                    EXTERNAL_ASSET_OWNER_HELPER.updateLoanAttributeFailure(Long.MAX_VALUE, BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE),
                    404, LOAN_NOT_FOUND);
        });
    }

    @Test
    public void loanLevelCorrectionNeedsTheDedicatedPermissionAndSupportsMakerChecker() {
        final Long clientId = createClient();
        final Long productId = createProductWithAttributes(attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED));
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();

        runAt("01 July 2026", () -> {
            loanIdRef.set(createDisbursedLoan(clientId, productId, null));
            sell(loanIdRef.get(), UUID.randomUUID().toString(), "2026-07-01");
        });

        runAt("02 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            final String productUsername = Utils.uniqueRandomStringGenerator("pau", 8);
            createUser(productUsername, UPDATE_LOAN_PRODUCT_ATTRIBUTE_PERMISSION);
            final FineractFeignClient productAttributeUser = FineractFeignClientHelper.createNewFineractFeignClient(productUsername,
                    PASSWORD);
            assertRejected(EXTERNAL_ASSET_OWNER_HELPER.updateLoanAttributeAsFailure(productAttributeUser, loanId,
                    BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE), 403, NOT_AUTHORIZED);

            final String correctionUsername = Utils.uniqueRandomStringGenerator("lau", 8);
            createUser(correctionUsername, UPDATE_LOAN_ATTRIBUTE_PERMISSION);
            final FineractFeignClient correctionUser = FineractFeignClientHelper.createNewFineractFeignClient(correctionUsername, PASSWORD);
            EXTERNAL_ASSET_OWNER_HELPER.updateLoanAttributeAs(correctionUser, loanId, BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE);
            assertLoanLevelAttributes(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanId),
                    attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE));

            final String makerUsername = Utils.uniqueRandomStringGenerator("mkr", 8);
            final Long makerId = createUser(makerUsername, UPDATE_LOAN_ATTRIBUTE_PERMISSION);
            final String checkerUsername = Utils.uniqueRandomStringGenerator("ckr", 8);
            createUser(checkerUsername, UPDATE_LOAN_ATTRIBUTE_PERMISSION + "_CHECKER");
            final FineractFeignClient maker = FineractFeignClientHelper.createNewFineractFeignClient(makerUsername, PASSWORD);
            final FineractFeignClient checker = FineractFeignClientHelper.createNewFineractFeignClient(checkerUsername, PASSWORD);

            final MakerCheckerSettings settingsBefore = currentMakerCheckerSettings();
            try {
                applyMakerCheckerSettings(new MakerCheckerSettings(true, true, false));

                final CommandProcessingResult queued = EXTERNAL_ASSET_OWNER_HELPER.updateLoanAttributeAs(maker, loanId,
                        BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED);
                final List<AuditData> pending = pendingLoanAttributeUpdates(loanId, makerId);
                assertEquals(1, pending.size(), "the maker's correction must wait for approval");
                assertEquals(pending.getFirst().getId(), queued.getCommandId());
                assertLoanLevelAttributes(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanId),
                        attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE));

                new FeignMakerCheckerHelper(checker).approve(pending.getFirst().getId());
                assertLoanLevelAttributes(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanId),
                        attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED));

                final AuditData audit = MAKER_CHECKER_HELPER.retrieveAuditEntry(pending.getFirst().getId());
                assertEquals("UPDATE", audit.getActionName());
                assertEquals("EXTERNAL_ASSET_OWNER_LOAN_ATTRIBUTE", audit.getEntityName());
                assertEquals(loanId, audit.getResourceId());
                assertEquals(makerUsername, audit.getMaker());
                assertEquals(checkerUsername, audit.getChecker());
                assertEquals("Processed", audit.getProcessingResult());
            } finally {
                applyMakerCheckerSettings(settingsBefore);
            }
        });
    }

    @Test
    public void settlementModelCorrectionIsRejectedWhileDelayedSettlementIsInFlight() {
        final Long clientId = createClient();
        final Long productId = createProductWithAttributes(attributes(SETTLEMENT_MODEL, DELAYED_SETTLEMENT));
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> intermediarySaleTransferIdRef = new AtomicReference<>();

        runAt("01 July 2026", () -> {
            loanIdRef.set(createDisbursedLoan(clientId, productId, null));
            intermediarySaleTransferIdRef
                    .set(createIntermediarySaleTransfer(loanIdRef.get(), "2026-07-01", UUID.randomUUID().toString()).getResourceId());
        });

        runAt("02 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            assertEquals(ExternalTransferData.StatusEnum.ACTIVE_INTERMEDIATE,
                    EXTERNAL_ASSET_OWNER_HELPER.retrieveActiveTransferByLoanId(loanId).getStatus());

            assertRejected(EXTERNAL_ASSET_OWNER_HELPER.updateLoanAttributeFailure(loanId, SETTLEMENT_MODEL, DEFAULT_SETTLEMENT), 403,
                    SETTLEMENT_MODEL_TRANSFER_IN_PROGRESS);
            assertLoanLevelCopy(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanId), loanId, intermediarySaleTransferIdRef.get(),
                    attributes(SETTLEMENT_MODEL, DELAYED_SETTLEMENT));
        });
    }

    @Test
    public void productAttributeAddedAfterTheSaleIsIgnoredByTheSoldLoan() {
        final Long clientId = createClient();
        final Long productId = createProductWithAttributes(attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED));
        final String ownerExternalId = UUID.randomUUID().toString();
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> saleTransferIdRef = new AtomicReference<>();

        runAt("01 July 2026", () -> {
            loanIdRef.set(createDisbursedLoan(clientId, productId, null));
            saleTransferIdRef.set(sell(loanIdRef.get(), ownerExternalId, "2026-07-01"));
        });

        runAt("02 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            EXTERNAL_ASSET_OWNER_HELPER.createLoanProductAttribute(productId, EXCLUDED_TRANSACTION_TYPES, BDF_TYPES);
            assertProductAttributes(productId,
                    attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED, EXCLUDED_TRANSACTION_TYPES, BDF_TYPES));

            final Long buyDownFeeTxId = makeLoanBuyDownFee(loanId, "02 July 2026", 50.0).getResourceId();
            assertTaggedOnBothSurfaces(buyDownFeeTxId, ownerExternalId);
            assertNoBuyDownFeeAmortization(loanId);

            assertLoanLevelCopy(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanId), loanId, saleTransferIdRef.get(),
                    attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED));
        });
    }

    @Test
    public void settlementModelCorrectionIsRejectedWhileADefaultSettlementSaleIsPendingButOtherKeysAreCorrected() {
        final Long clientId = createClient();
        final Long productId = createProductWithAttributes(attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED));
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> saleTransferIdRef = new AtomicReference<>();

        runAt("01 July 2026", () -> {
            loanIdRef.set(createDisbursedLoan(clientId, productId, null));
            saleTransferIdRef.set(sell(loanIdRef.get(), UUID.randomUUID().toString(), "2026-07-01"));
        });

        runAt("02 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            sell(loanId, UUID.randomUUID().toString(), "2026-07-02");
            assertEquals(
                    List.of(ExternalTransferData.StatusEnum.PENDING, ExternalTransferData.StatusEnum.ACTIVE,
                            ExternalTransferData.StatusEnum.PENDING),
                    transfersInCreationOrder(loanId).stream().map(ExternalTransferData::getStatus).toList());

            assertRejected(EXTERNAL_ASSET_OWNER_HELPER.updateLoanAttributeFailure(loanId, SETTLEMENT_MODEL, DELAYED_SETTLEMENT), 403,
                    SETTLEMENT_MODEL_TRANSFER_IN_PROGRESS);
            assertLoanLevelCopy(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanId), loanId, saleTransferIdRef.get(),
                    attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, DEFERRED));

            EXTERNAL_ASSET_OWNER_HELPER.updateLoanAttribute(loanId, BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE);
            assertLoanLevelCopy(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributes(loanId), loanId, saleTransferIdRef.get(),
                    attributes(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, IMMEDIATE));
        });
    }

    @Test
    public void loanAttributesOfAnUnknownLoanAreNotFound() {
        assertRejected(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributesFailure(Long.MAX_VALUE), 404, LOAN_NOT_FOUND);
        assertRejected(EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanAttributesByLoanExternalIdFailure(UUID.randomUUID().toString()), 404,
                LOAN_EXTERNAL_ID_NOT_FOUND);
    }

    private void assertNoLoanLevelCopy(final ExternalAssetOwnerLoanAttributesData data, final Long loanId) {
        assertEquals(loanId, data.getLoanId());
        assertEquals(Boolean.FALSE, data.getHasLoanLevelCopy(), "loan " + loanId + " must not have a loan-level copy yet");
        assertNull(data.getCopiedFromTransferId());
        assertEquals(List.of(), data.getAttributes());
    }

    private void assertLoanLevelCopy(final ExternalAssetOwnerLoanAttributesData data, final Long loanId, final Long copiedFromTransferId,
            final Map<String, String> expectedAttributes) {
        assertEquals(loanId, data.getLoanId());
        assertEquals(copiedFromTransferId, data.getCopiedFromTransferId(), "the copy must record the transfer that took it");
        assertLoanLevelAttributes(data, expectedAttributes);
    }

    private void assertLoanLevelAttributes(final ExternalAssetOwnerLoanAttributesData data, final Map<String, String> expectedAttributes) {
        assertEquals(Boolean.TRUE, data.getHasLoanLevelCopy(), "loan " + data.getLoanId() + " must have a loan-level copy");
        assertNotNull(data.getAttributes());
        assertEquals(expectedAttributes,
                data.getAttributes().stream()
                        .collect(Collectors.toMap(ExternalAssetOwnerLoanAttributeData::getAttributeKey,
                                ExternalAssetOwnerLoanAttributeData::getAttributeValue, (a, b) -> a, LinkedHashMap::new)),
                "loan-level attributes of loan " + data.getLoanId());
        assertEquals(expectedAttributes.size(), data.getAttributes().size(), "every key must appear exactly once");
    }

    private void assertProductAttributes(final Long productId, final Map<String, String> expectedAttributes) {
        final PageExternalTransferLoanProductAttributesData page = EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanProductAttributes(productId,
                null);
        assertNotNull(page.getPageItems());
        assertEquals(expectedAttributes,
                page.getPageItems().stream()
                        .collect(Collectors.toMap(ExternalTransferLoanProductAttributesData::getAttributeKey,
                                ExternalTransferLoanProductAttributesData::getAttributeValue)),
                "product attributes must be untouched by a loan-level correction");
    }

    private static void assertRejected(final CallFailedRuntimeException failure, final int expectedStatus, final String errorCode) {
        assertEquals(expectedStatus, failure.getStatus(), "unexpected status, body: " + failure.getResponseBody());
        assertEquals(errorCode, extractErrorGlobalisationCode(failure), "unexpected error, body: " + failure.getResponseBody());
    }

    private Long assertSingleBuyDownFeeAmortization(final Long loanId, final LocalDate expectedDate, final double expectedAmount) {
        final List<GetLoansLoanIdTransactions> amortizations = activeBuyDownFeeAmortizations(loanId);
        assertEquals(1, amortizations.size(), "expected exactly one immediate buy down fee amortization on loan " + loanId);
        final GetLoansLoanIdTransactions amortization = amortizations.getFirst();
        assertEquals(expectedDate, amortization.getDate());
        assertEquals(0, BigDecimal.valueOf(expectedAmount).compareTo(amortization.getAmount()),
                "immediate amortization must recognise the whole fee, was " + amortization.getAmount());
        return amortization.getId();
    }

    private void assertNoBuyDownFeeAmortization(final Long loanId) {
        assertEquals(List.of(), activeBuyDownFeeAmortizations(loanId).stream().map(GetLoansLoanIdTransactions::getId).toList(),
                "a DEFERRED copy must not amortize the fee on the day it is posted");
    }

    private List<GetLoansLoanIdTransactions> activeBuyDownFeeAmortizations(final Long loanId) {
        final List<GetLoansLoanIdTransactions> transactions = getLoanDetails(loanId).getTransactions();
        assertNotNull(transactions);
        return transactions.stream().filter(
                transaction -> transaction.getType() != null && Boolean.TRUE.equals(transaction.getType().getBuyDownFeeAmortization()))
                .filter(transaction -> !Boolean.TRUE.equals(transaction.getManuallyReversed())).toList();
    }

    private static Map<String, String> attributes(final String... keyValues) {
        final Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            result.put(keyValues[i], keyValues[i + 1]);
        }
        return result;
    }

    /** Every test configures its own product before any loan exists, so the per-node product cache never matters. */
    private Long createProductWithAttributes(final Map<String, String> productAttributes) {
        final Long productId = createLoanProduct(
                buyDownFeeProduct("EAO_LOAN_ATTR_", assetAccount, incomeAccount, expenseAccount, liabilityAccount));
        productAttributes.forEach((key, value) -> EXTERNAL_ASSET_OWNER_HELPER.createLoanProductAttribute(productId, key, value));
        return productId;
    }

    private Long productAttributeId(final Long productId, final String attributeKey) {
        final PageExternalTransferLoanProductAttributesData page = EXTERNAL_ASSET_OWNER_HELPER.retrieveLoanProductAttributes(productId,
                attributeKey);
        assertEquals(1, page.getTotalFilteredRecords());
        return page.getPageItems().getFirst().getAttributeId();
    }

    private void updateProductAttribute(final Long productId, final String attributeKey, final String attributeValue) {
        EXTERNAL_ASSET_OWNER_HELPER.updateLoanProductAttribute(productId, productAttributeId(productId, attributeKey), attributeKey,
                attributeValue);
    }

    private void deleteProductAttribute(final Long productId, final String attributeKey) {
        EXTERNAL_ASSET_OWNER_HELPER.deleteLoanProductAttribute(productId, productAttributeId(productId, attributeKey));
    }

    private Long createDisbursedLoan(final Long clientId, final Long productId, final String loanExternalId) {
        final Long loanId = applyAndApproveProgressiveLoan(clientId, productId, "01 July 2026", 1000.0, 10.0, 6,
                request -> request.externalId(loanExternalId));
        disburseLoan(loanId, BigDecimal.valueOf(1000.0), "01 July 2026");
        return loanId;
    }

    /** Requests a sale and returns the id of the transfer it created. */
    private Long sell(final Long loanId, final String ownerExternalId, final String settlementDate) {
        return createSaleTransfer(loanId, settlementDate, UUID.randomUUID().toString(), UUID.randomUUID().toString(), ownerExternalId,
                "1.0").getResourceId();
    }

    private List<ExternalTransferData> transfersInCreationOrder(final Long loanId) {
        final List<ExternalTransferData> content = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId).getContent();
        assertNotNull(content);
        return content.stream().sorted(Comparator.comparing(ExternalTransferData::getTransferId)).toList();
    }

    private Long createUser(final String username, final String permission) {
        final Long roleId = FeignRoleHelper.createRole();
        FeignRoleHelper.addPermissionsToRole(roleId, Map.of(permission, true, "READ_LOAN", true));
        return FeignUserHelper.createUser(roleId, null, username, PASSWORD).getResourceId();
    }

    private record MakerCheckerSettings(boolean loanAttributePermission, boolean global, boolean sameMakerChecker) {
    }

    private MakerCheckerSettings currentMakerCheckerSettings() {
        return new MakerCheckerSettings(MAKER_CHECKER_HELPER.isMakerCheckerEnabled(UPDATE_LOAN_ATTRIBUTE_PERMISSION),
                isConfigurationEnabled(GlobalConfigurationConstants.MAKER_CHECKER),
                isConfigurationEnabled(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER));
    }

    private boolean isConfigurationEnabled(final String configName) {
        return Boolean.TRUE.equals(globalConfigurationHelper.getGlobalConfigurationByName(configName).getEnabled());
    }

    private void applyMakerCheckerSettings(final MakerCheckerSettings settings) {
        MAKER_CHECKER_HELPER.setMakerCheckerEnabled(UPDATE_LOAN_ATTRIBUTE_PERMISSION, settings.loanAttributePermission());
        globalConfigurationHelper.updateConfigurationByName(GlobalConfigurationConstants.MAKER_CHECKER, settings.global());
        globalConfigurationHelper.updateConfigurationByName(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER,
                settings.sameMakerChecker());
    }

    private List<AuditData> pendingLoanAttributeUpdates(final Long loanId, final Long makerId) {
        return MAKER_CHECKER_HELPER.pendingCommands("UPDATE", "EXTERNAL_ASSET_OWNER_LOAN_ATTRIBUTE", loanId, makerId);
    }
}
