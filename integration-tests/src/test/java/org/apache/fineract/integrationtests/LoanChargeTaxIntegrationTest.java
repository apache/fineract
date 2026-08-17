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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.ChargeCreateRequest;
import org.apache.fineract.client.models.ChargeCreateResponse;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.GetLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.GetTaxesGroupResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostTaxesComponentsRequest;
import org.apache.fineract.client.models.PostTaxesComponentsResponse;
import org.apache.fineract.client.models.PostTaxesGroupRequest;
import org.apache.fineract.client.models.PostTaxesGroupResponse;
import org.apache.fineract.client.models.PostTaxesGroupTaxComponents;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.TaxComponentHelper;
import org.apache.fineract.integrationtests.common.TaxGroupHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.PeriodicAccrualAccountingHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanChargeTaxIntegrationTest extends BaseLoanIntegrationTest {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String LOAN_DATE = "01 January 2023";
    private static final String DUE_DATE = "15 January 2023";
    private static final String TAX_START_DATE = "01 January 2013";

    // -----------------------------------------------------------------------
    // 1. TaxGroup setup helpers
    // -----------------------------------------------------------------------

    /**
     * Creates a TaxComponent with the given percentage and a fixed start date in the past so it is always active during
     * integration test runs.
     */
    private PostTaxesComponentsResponse createTaxComponent(float percentage) {
        PostTaxesComponentsRequest request = new PostTaxesComponentsRequest().name(Utils.uniqueRandomStringGenerator("TAX_COMP_", 6))
                .percentage(percentage).startDate(TAX_START_DATE).dateFormat(DATE_FORMAT).locale(LOCALE);
        PostTaxesComponentsResponse response = TaxComponentHelper.createTaxComponent(request);
        assertNotNull(response);
        assertNotNull(response.getResourceId());
        return response;
    }

    /**
     * Wraps a list of already-created TaxComponent IDs into a TaxGroup.
     */
    private PostTaxesGroupResponse createTaxGroup(Long... taxComponentIds) {
        Set<PostTaxesGroupTaxComponents> components = new HashSet<>();
        for (Long id : taxComponentIds) {
            components.add(new PostTaxesGroupTaxComponents().taxComponentId(id).startDate(TAX_START_DATE));
        }
        PostTaxesGroupRequest request = new PostTaxesGroupRequest().name(Utils.uniqueRandomStringGenerator("TAX_GRP_", 6))
                .taxComponents(components).dateFormat(DATE_FORMAT).locale(LOCALE);
        PostTaxesGroupResponse response = TaxGroupHelper.createTaxGroup(request);
        assertNotNull(response);
        assertNotNull(response.getResourceId());
        return response;
    }

    /**
     * Creates a TaxComponent with the given percentage, linked to the provided GL account as credit account (tax
     * liability). The credit account determines where the tax portion is posted in accounting.
     */
    private PostTaxesComponentsResponse createTaxComponent(float percentage, Long creditAccountId) {
        PostTaxesComponentsRequest request = new PostTaxesComponentsRequest().name(Utils.uniqueRandomStringGenerator("TAX_COMP_", 6))
                .percentage(percentage).startDate(TAX_START_DATE).dateFormat(DATE_FORMAT).locale(LOCALE).creditAccountId(creditAccountId)
                .creditAccountType(2);
        PostTaxesComponentsResponse response = TaxComponentHelper.createTaxComponent(request);
        assertNotNull(response);
        assertNotNull(response.getResourceId());
        return response;
    }

    /**
     * Creates a one-period, 30-day loan product using Cash-based accounting (accountingRule=2). Only the minimum set of
     * GL accounts required for cash accounting is mapped.
     */
    private PostLoanProductsRequest createCashBasedLoanProduct() {
        return new PostLoanProductsRequest().name(Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_CASH_", 6))
                .shortName(Utils.uniqueRandomStringGenerator("", 4)).description("Cash-based loan product for tax tests")
                .includeInBorrowerCycle(false).currencyCode("USD").digitsAfterDecimal(2).inMultiplesOf(0).installmentAmountInMultiplesOf(1)
                .useBorrowerCycle(false).minPrincipal(100.0).principal(1000.0).maxPrincipal(100000.0).minNumberOfRepayments(1)
                .numberOfRepayments(1).maxNumberOfRepayments(30).isLinkedToFloatingInterestRates(false).minInterestRatePerPeriod((double) 0)
                .interestRatePerPeriod(0.0).maxInterestRatePerPeriod((double) 100).interestRateFrequencyType(2).repaymentEvery(30)
                .repaymentFrequencyType(0L).amortizationType(1).interestType(0).isEqualAmortization(false).interestCalculationPeriodType(1)
                .transactionProcessingStrategyCode(
                        LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY)
                .daysInYearType(1).daysInMonthType(1).canDefineInstallmentAmount(true).graceOnArrearsAgeing(3).overdueDaysForNPA(179)
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false).principalThresholdForLastInstallment(50)
                .allowVariableInstallments(false).canUseForTopup(false).isInterestRecalculationEnabled(false).holdGuaranteeFunds(false)
                .multiDisburseLoan(true).maxTrancheCount(10).outstandingLoanBalance(10000.0).charges(Collections.emptyList())
                .accountingRule(2) // Cash-based
                .fundSourceAccountId(fundSource.getAccountID().longValue())
                .loanPortfolioAccountId(loansReceivableAccount.getAccountID().longValue())
                .transfersInSuspenseAccountId(suspenseAccount.getAccountID().longValue())
                .interestOnLoanAccountId(interestIncomeAccount.getAccountID().longValue())
                .incomeFromFeeAccountId(feeIncomeAccount.getAccountID().longValue())
                .incomeFromPenaltyAccountId(penaltyIncomeAccount.getAccountID().longValue())
                .incomeFromRecoveryAccountId(recoveriesAccount.getAccountID().longValue())
                .writeOffAccountId(writtenOffAccount.getAccountID().longValue())
                .overpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue()).dateFormat(DATETIME_PATTERN).locale("en_GB")
                .disallowExpectedDisbursements(true).allowApprovedDisbursedAmountsOverApplied(true).overAppliedCalculationType("percentage")
                .overAppliedNumber(50);
    }

    /**
     * Creates a FLAT, SPECIFIED_DUE_DATE charge definition linked to the given {@code taxGroupId}. Passing {@code null}
     * for {@code taxGroupId} creates a charge with no tax.
     */
    private ChargeCreateResponse createFlatLoanCharge(double baseAmount, Long taxGroupId) {
        ChargesHelper chargesHelper = new ChargesHelper();
        ChargeCreateRequest request = new ChargeCreateRequest().penalty(false).amount(baseAmount)
                .chargeCalculationType(ChargeCalculationType.FLAT.getValue()).chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue())
                .chargePaymentMode(ChargePaymentMode.REGULAR.getValue()).currencyCode("USD")
                .name(Utils.uniqueRandomStringGenerator("CHARGE_", 6)).chargeAppliesTo(1).locale(LOCALE).active(true);
        if (taxGroupId != null) {
            request.taxGroupId(taxGroupId);
        }
        ChargeCreateResponse response = chargesHelper.createCharges(request);
        assertNotNull(response);
        assertNotNull(response.getResourceId());
        return response;
    }

    // -----------------------------------------------------------------------
    // 2. Happy path – single tax component
    // -----------------------------------------------------------------------

    /**
     * A loan charge with a 10 % TaxGroup applied to a flat charge of 100 USD must keep its amount at 100 USD. The 10
     * USD tax is stored separately as {@code taxAmount} and is not added to the charge amount.
     *
     * <pre>
     *   base = 100, tax = 10 % → amount = 100, taxAmount = 10
     * </pre>
     */
    @Test
    public void testLoanChargeAmount_remainsUnchanged_whenTaxGroupIsConfigured() {
        runAt(LOAN_DATE, () -> {
            // Given – tax infrastructure
            PostTaxesComponentsResponse taxComponent = createTaxComponent(10.0f);
            PostTaxesGroupResponse taxGroup = createTaxGroup(taxComponent.getResourceId());

            // Given – charge definition with 10 % tax group, base = 100
            ChargeCreateResponse chargeResponse = createFlatLoanCharge(100.0, taxGroup.getResourceId());
            Long chargeDefinitionId = chargeResponse.getResourceId();

            // Given – a disbursed loan
            Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = loanProductHelper.createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct())
                    .getResourceId();
            Long loanId = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), LOAN_DATE);

            // When – the taxed charge is added to the loan
            PostLoansLoanIdChargesResponse addResult = loanTransactionHelper.addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest()
                    .chargeId(chargeDefinitionId).amount(100.0).dueDate(DUE_DATE).dateFormat(DATE_FORMAT).locale(LOCALE));
            assertNotNull(addResult);
            Long loanChargeId = addResult.getResourceId();
            assertNotNull(loanChargeId);

            // Then – amount stays at 100; tax (10) is stored separately as taxAmount
            GetLoansLoanIdChargesChargeIdResponse loanCharge = loanTransactionHelper.getLoanCharge(loanId, loanChargeId);
            assertNotNull(loanCharge);
            assertEquals(100.0, loanCharge.getAmount(), 0.01, "Charge amount must remain unchanged; tax stored separately as taxAmount=10");
            assertEquals(100.0, loanCharge.getAmountOutstanding(), 0.01,
                    "Outstanding amount must equal the original charge amount, not the charge+tax amount");
        });
    }

    // -----------------------------------------------------------------------
    // 3. Happy path – multiple tax components
    // -----------------------------------------------------------------------

    /**
     * A TaxGroup with two components (10 % + 5 %) applied to a flat charge of 200 USD must keep the loan-charge amount
     * at 200 USD. The taxes (20 + 10 = 30) are stored separately as {@code taxAmount}.
     */
    @Test
    public void testLoanChargeAmount_remainsUnchanged_multipleComponents() {
        runAt(LOAN_DATE, () -> {
            // Given – two tax components in the same group
            PostTaxesComponentsResponse comp1 = createTaxComponent(10.0f);
            PostTaxesComponentsResponse comp2 = createTaxComponent(5.0f);
            PostTaxesGroupResponse taxGroup = createTaxGroup(comp1.getResourceId(), comp2.getResourceId());

            ChargeCreateResponse chargeResponse = createFlatLoanCharge(200.0, taxGroup.getResourceId());

            Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = loanProductHelper.createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct())
                    .getResourceId();
            Long loanId = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), LOAN_DATE);

            // When
            PostLoansLoanIdChargesResponse addResult = loanTransactionHelper.addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest()
                    .chargeId(chargeResponse.getResourceId()).amount(200.0).dueDate(DUE_DATE).dateFormat(DATE_FORMAT).locale(LOCALE));
            Long loanChargeId = addResult.getResourceId();

            // Then – amount stays 200; taxes (20+10=30) stored separately as taxAmount
            GetLoansLoanIdChargesChargeIdResponse loanCharge = loanTransactionHelper.getLoanCharge(loanId, loanChargeId);
            assertEquals(200.0, loanCharge.getAmount(), 0.01,
                    "Charge amount must remain 200; taxes (20+10=30) stored separately as taxAmount");
        });
    }

    // -----------------------------------------------------------------------
    // 4. No TaxGroup – charge amount unchanged
    // -----------------------------------------------------------------------

    /**
     * A charge without a TaxGroup must be added to the loan at its original base amount without any modification.
     */
    @Test
    public void testLoanChargeAmount_noTaxGroup_isNotModified() {
        runAt(LOAN_DATE, () -> {
            // Given – charge with NO tax group
            ChargeCreateResponse chargeResponse = createFlatLoanCharge(100.0, null);

            Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = loanProductHelper.createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct())
                    .getResourceId();
            Long loanId = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), LOAN_DATE);

            // When
            PostLoansLoanIdChargesResponse addResult = loanTransactionHelper.addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest()
                    .chargeId(chargeResponse.getResourceId()).amount(100.0).dueDate(DUE_DATE).dateFormat(DATE_FORMAT).locale(LOCALE));
            Long loanChargeId = addResult.getResourceId();

            // Then – amount must stay at 100 (no tax)
            GetLoansLoanIdChargesChargeIdResponse loanCharge = loanTransactionHelper.getLoanCharge(loanId, loanChargeId);
            assertEquals(100.0, loanCharge.getAmount(), 0.01, "Charge without tax group must keep original amount");
        });
    }

    // -----------------------------------------------------------------------
    // 5. Charge definition carries TaxGroup metadata
    // -----------------------------------------------------------------------

    /**
     * After creating a charge definition that references a TaxGroup, retrieving the charge via GET /charges/{id} must
     * return the charge with the correct {@code taxGroup} attribute populated.
     */
    @Test
    public void testChargeDefinition_hasTaxGroupPopulated() {
        // Given – tax infrastructure (no loan needed for this test)
        PostTaxesComponentsResponse taxComponent = createTaxComponent(16.0f);
        PostTaxesGroupResponse taxGroup = createTaxGroup(taxComponent.getResourceId());

        // When – create charge linked to the tax group
        ChargeCreateResponse chargeResponse = createFlatLoanCharge(50.0, taxGroup.getResourceId());

        // Then – retrieve and verify taxGroup is set
        ChargesHelper chargesHelper = new ChargesHelper();
        ChargeData chargeData = chargesHelper.retrieveCharge(chargeResponse.getResourceId());
        assertNotNull(chargeData);
        assertNotNull(chargeData.getTaxGroup(), "Charge must expose taxGroup in GET response");
        assertEquals(taxGroup.getResourceId(), chargeData.getTaxGroup().getId(),
                "Charge taxGroup id must match the one used during creation");
    }

    // -----------------------------------------------------------------------
    // 6. TaxGroup retrievable with its components
    // -----------------------------------------------------------------------

    /**
     * A TaxGroup created with one TaxComponent must be retrievable via GET /taxes/group/{id} and include the correct
     * component in the mappings list.
     */
    @Test
    public void testTaxGroupRetrieval_includesExpectedComponent() {
        // Given
        PostTaxesComponentsResponse taxComponent = createTaxComponent(12.0f);
        PostTaxesGroupResponse taxGroup = createTaxGroup(taxComponent.getResourceId());

        // When
        GetTaxesGroupResponse retrieved = TaxGroupHelper.retrieveTaxGroup(taxGroup.getResourceId());

        // Then
        assertNotNull(retrieved);
        assertEquals(taxGroup.getResourceId(), retrieved.getId());
        assertNotNull(retrieved.getTaxAssociations(), "TaxGroup must expose its component associations list");
        boolean componentPresent = retrieved.getTaxAssociations().stream()
                .anyMatch(m -> taxComponent.getResourceId().equals(m.getTaxComponent().getId()));
        assertEquals(true, componentPresent, "TaxGroup must contain the TaxComponent used during creation");
    }

    // -----------------------------------------------------------------------
    // 7. TaxGroup appears in retrieveAll list
    // -----------------------------------------------------------------------

    /**
     * A newly created TaxGroup must be visible in the paginated list returned by GET /taxes/group.
     */
    @Test
    public void testTaxGroup_appearsInRetrieveAllList() {
        // Given
        PostTaxesComponentsResponse taxComponent = createTaxComponent(8.0f);
        PostTaxesGroupResponse taxGroup = createTaxGroup(taxComponent.getResourceId());

        // When
        List<GetTaxesGroupResponse> allGroups = TaxGroupHelper.retrieveAllTaxGroups();

        // Then
        assertNotNull(allGroups);
        boolean found = allGroups.stream().anyMatch(g -> taxGroup.getResourceId().equals(g.getId()));
        assertEquals(true, found, "Newly created TaxGroup must appear in the full tax group list");
    }

    // -----------------------------------------------------------------------
    // 8. Multiple loans – tax applied independently per loan charge
    // -----------------------------------------------------------------------

    /**
     * Adding the same taxed charge definition to two different loans must result in independent charge records each
     * keeping the original base amount. Verifies that per-loan charge state is isolated.
     */
    @Test
    public void testLoanChargeTax_appliedIndependentlyToEachLoan() {
        runAt(LOAN_DATE, () -> {
            // Given – shared charge definition with 10 % tax
            PostTaxesComponentsResponse taxComponent = createTaxComponent(10.0f);
            PostTaxesGroupResponse taxGroup = createTaxGroup(taxComponent.getResourceId());
            ChargeCreateResponse chargeResponse = createFlatLoanCharge(100.0, taxGroup.getResourceId());
            Long chargeDefinitionId = chargeResponse.getResourceId();

            // Given – two separate loans
            Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = loanProductHelper.createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct())
                    .getResourceId();

            Long loanId1 = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId1, BigDecimal.valueOf(1000.0), LOAN_DATE);

            Long loanId2 = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId2, BigDecimal.valueOf(1000.0), LOAN_DATE);

            // When – the same charge is added to both loans
            PostLoansLoanIdChargesResponse add1 = loanTransactionHelper.addChargesForLoan(loanId1, new PostLoansLoanIdChargesRequest()
                    .chargeId(chargeDefinitionId).amount(100.0).dueDate(DUE_DATE).dateFormat(DATE_FORMAT).locale(LOCALE));
            PostLoansLoanIdChargesResponse add2 = loanTransactionHelper.addChargesForLoan(loanId2, new PostLoansLoanIdChargesRequest()
                    .chargeId(chargeDefinitionId).amount(100.0).dueDate(DUE_DATE).dateFormat(DATE_FORMAT).locale(LOCALE));

            // Then – both loan charges must independently show the original base amount (tax stored separately)
            GetLoansLoanIdChargesChargeIdResponse charge1 = loanTransactionHelper.getLoanCharge(loanId1, add1.getResourceId());
            GetLoansLoanIdChargesChargeIdResponse charge2 = loanTransactionHelper.getLoanCharge(loanId2, add2.getResourceId());

            assertEquals(100.0, charge1.getAmount(), 0.01, "Loan 1 charge amount must remain 100; tax stored separately");
            assertEquals(100.0, charge2.getAmount(), 0.01, "Loan 2 charge amount must remain 100; tax stored separately");
        });
    }

    // -----------------------------------------------------------------------
    // 10. Cash-based accounting: fee with tax splits income vs. tax liability
    // -----------------------------------------------------------------------

    /**
     * Cash-based accounting: when a loan charge with a 10 % TaxGroup (base = 100 USD) is repaid, the accounting must
     * split the fee income credit:
     *
     * <pre>
     *   DR  Fund Source         1100.00
     *   CR  Loan Portfolio      1000.00
     *   CR  Income from Fees      90.00  (net = base − tax)
     *   CR  Tax Liability         10.00  (TaxComponent.creditAccount)
     * </pre>
     *
     * The disbursement journal entries (DR Loan Portfolio / CR Fund Source) are also verified.
     */
    @Test
    public void testCashAccounting_journalEntries_feeWithTaxSplitsIncomeAndLiability() {
        runAt(LOAN_DATE, () -> {
            // Given – a liability account to receive the tax portion
            Account taxLiabilityAccount = accountHelper.createLiabilityAccount("taxLiability_cash");

            // Given – tax infrastructure linked to the tax liability account
            PostTaxesComponentsResponse taxComponent = createTaxComponent(10.0f, taxLiabilityAccount.getAccountID().longValue());
            PostTaxesGroupResponse taxGroup = createTaxGroup(taxComponent.getResourceId());

            // Given – flat charge of 100 with 10 % tax, due on the loan start date
            ChargeCreateResponse chargeResponse = createFlatLoanCharge(100.0, taxGroup.getResourceId());

            // Given – a cash-based loan (principal = 1000, no interest)
            Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = loanProductHelper.createLoanProduct(createCashBasedLoanProduct() //
                    .multiDisburseLoan(false).disallowExpectedDisbursements(null) //
            ).getResourceId();
            Long loanId = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), LOAN_DATE);

            // When – add the taxed charge (due on same date) and repay principal + fee in one transaction
            loanTransactionHelper.addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().chargeId(chargeResponse.getResourceId())
                    .amount(100.0).dueDate(LOAN_DATE).dateFormat(DATE_FORMAT).locale(LOCALE));

            addRepaymentForLoan(loanId, 1100.0, LOAN_DATE);

            // Then – verify journal entries:
            // Disbursement: DR Loan Portfolio 1000 / CR Fund Source 1000
            // Repayment: DR Fund Source 1100 / CR Loan Portfolio 1000 / CR Income from Fees 90 / CR Tax Liability 10
            verifyJournalEntries(loanId,
                    // disbursement
                    debit(loansReceivableAccount, 1000.0), credit(fundSource, 1000.0),
                    // repayment
                    debit(fundSource, 1100.0), credit(loansReceivableAccount, 1000.0), credit(feeIncomeAccount, 90.0),
                    credit(taxLiabilityAccount, 10.0));
        });
    }

    // -----------------------------------------------------------------------
    // 11. Accrual accounting: accrual and repayment with tax on fee charge
    // -----------------------------------------------------------------------

    /**
     * Periodic-accrual accounting: when a taxed fee charge is accrued the income and liability are recognised
     * immediately; at repayment time the receivable is cleared in full with no further tax entries.
     *
     * <pre>
     * Accrual (charge application):
     *   DR  Fees Receivable      100.00
     *   CR  Income from Fees      90.00
     *   CR  Tax Liability         10.00
     *
     * Repayment:
     *   DR  Fund Source          1100.00
     *   CR  Loan Portfolio       1000.00
     *   CR  Fees Receivable       100.00
     * </pre>
     *
     * The disbursement journal entries are also verified.
     */
    @Test
    public void testAccrualAccounting_journalEntries_feeWithTaxSplitsIncomeAndLiabilityAtAccrual() {
        runAt(LOAN_DATE, () -> {
            // Given – a liability account to receive the tax portion
            Account taxLiabilityAccount = accountHelper.createLiabilityAccount("taxLiability_accrual");

            // Given – tax infrastructure linked to the tax liability account
            PostTaxesComponentsResponse taxComponent = createTaxComponent(10.0f, taxLiabilityAccount.getAccountID().longValue());
            PostTaxesGroupResponse taxGroup = createTaxGroup(taxComponent.getResourceId());

            // Given – flat charge of 100 with 10 % tax, due on the loan start date
            ChargeCreateResponse chargeResponse = createFlatLoanCharge(100.0, taxGroup.getResourceId());

            // Given – a periodic-accrual loan (principal = 1000, no interest)
            Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = loanProductHelper.createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct())
                    .getResourceId();
            Long loanId = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), LOAN_DATE);

            // When – add the taxed charge and run periodic accrual on the same date
            loanTransactionHelper.addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().chargeId(chargeResponse.getResourceId())
                    .amount(100.0).dueDate(LOAN_DATE).dateFormat(DATE_FORMAT).locale(LOCALE));

            PeriodicAccrualAccountingHelper.runPeriodicAccrualAccounting(LOAN_DATE);

            // Make a full repayment (principal 1000 + fee 100 = 1100)
            addRepaymentForLoan(loanId, 1100.0, LOAN_DATE);

            // Then – verify ALL journal entries:
            // Disbursement: DR Loan Portfolio 1000 / CR Fund Source 1000
            // Accrual: DR Fees Receivable 100 / CR Income from Fees 90 / CR Tax Liability 10
            // Repayment: DR Fund Source 1100 / CR Loan Portfolio 1000 / CR Fees Receivable 100
            verifyJournalEntries(loanId,
                    // disbursement
                    debit(loansReceivableAccount, 1000.0), credit(fundSource, 1000.0),
                    // accrual
                    debit(feeReceivableAccount, 100.0), credit(feeIncomeAccount, 90.0), credit(taxLiabilityAccount, 10.0),
                    // repayment
                    debit(fundSource, 1100.0), credit(loansReceivableAccount, 1000.0), credit(feeReceivableAccount, 100.0));
        });
    }

    // -----------------------------------------------------------------------
    // 9. Full charge list on a loan reflects tax-inflated amounts
    // -----------------------------------------------------------------------

    /**
     * GET /loans/{loanId}/charges must return the taxed charge with the original base amount (tax stored separately)
     * when listing all charges for the loan.
     */
    @Test
    public void testLoanChargeList_containsOriginalAmount() {
        runAt(LOAN_DATE, () -> {
            // Given
            PostTaxesComponentsResponse taxComponent = createTaxComponent(10.0f);
            PostTaxesGroupResponse taxGroup = createTaxGroup(taxComponent.getResourceId());
            ChargeCreateResponse chargeResponse = createFlatLoanCharge(100.0, taxGroup.getResourceId());

            Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = loanProductHelper.createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct())
                    .getResourceId();
            Long loanId = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), LOAN_DATE);

            PostLoansLoanIdChargesResponse addResult = loanTransactionHelper.addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest()
                    .chargeId(chargeResponse.getResourceId()).amount(100.0).dueDate(DUE_DATE).dateFormat(DATE_FORMAT).locale(LOCALE));
            Long loanChargeId = addResult.getResourceId();

            // When – retrieve the full charge list
            List<GetLoansLoanIdChargesChargeIdResponse> charges = loanTransactionHelper.getLoanCharges(loanId);

            // Then – the taxed charge must appear in the list with the correct amount
            assertNotNull(charges);
            GetLoansLoanIdChargesChargeIdResponse found = charges.stream().filter(c -> loanChargeId.equals(c.getId())).findFirst()
                    .orElse(null);
            assertNotNull(found, "The added loan charge must appear in the charge list");
            assertEquals(100.0, found.getAmount(), 0.01,
                    "Listed charge amount must remain 100; the 10 % tax is stored separately as taxAmount");
        });
    }
}
