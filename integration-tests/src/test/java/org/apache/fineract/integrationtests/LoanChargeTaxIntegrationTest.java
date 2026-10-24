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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetChargesResponse;
import org.apache.fineract.client.models.GetLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.GetTaxesGroupResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostTaxesComponentsRequest;
import org.apache.fineract.client.models.PostTaxesComponentsResponse;
import org.apache.fineract.client.models.PostTaxesGroupRequest;
import org.apache.fineract.client.models.PostTaxesGroupResponse;
import org.apache.fineract.client.models.PostTaxesGroupTaxComponents;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTaxComponentHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTaxGroupHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanChargeTaxIntegrationTest extends FeignLoanTestBase {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String LOAN_DATE = "01 January 2023";
    private static final String DUE_DATE = "15 January 2023";
    private static final String TAX_START_DATE = "01 January 2013";
    private static final String AFTER_MATURITY = "15 February 2023";
    // about 23/123 of the fee, a 23 % VAT already inside it. The client's percentage is a Float, and no float is stored
    // as 18.699187: 18.699186 is the value the expected amounts are computed from
    private static final float TAX_RATE_23_IN_123 = 18.699186f;

    private final FeignTaxComponentHelper taxComponentHelper = new FeignTaxComponentHelper(
            FineractFeignClientHelper.getFineractFeignClient());
    private final FeignTaxGroupHelper taxGroupHelper = new FeignTaxGroupHelper(FineractFeignClientHelper.getFineractFeignClient());

    // -----------------------------------------------------------------------
    // 1. TaxGroup setup helpers
    // -----------------------------------------------------------------------

    /**
     * Creates a TaxComponent with the given percentage and a fixed start date in the past so it is always active during
     * integration test runs.
     */
    private PostTaxesComponentsResponse createTaxComponent(float percentage) {
        PostTaxesComponentsRequest request = new PostTaxesComponentsRequest().name(Utils.uniqueRandomStringGenerator("TAX_COMP_", 6))
                .percentage(percentage).startDate(TAX_START_DATE).dateFormat(DATE_FORMAT).locale(LoanTestData.LOCALE);
        PostTaxesComponentsResponse response = taxComponentHelper.createTaxComponent(request);
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
                .taxComponents(components).dateFormat(DATE_FORMAT).locale(LoanTestData.LOCALE);
        PostTaxesGroupResponse response = taxGroupHelper.createTaxGroup(request);
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
                .percentage(percentage).startDate(TAX_START_DATE).dateFormat(DATE_FORMAT).locale(LoanTestData.LOCALE)
                .creditAccountId(creditAccountId).creditAccountType(2);
        PostTaxesComponentsResponse response = taxComponentHelper.createTaxComponent(request);
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
                .fundSourceAccountId(getAccounts().getFundSource().getAccountID().longValue())
                .loanPortfolioAccountId(getAccounts().getLoansReceivableAccount().getAccountID().longValue())
                .transfersInSuspenseAccountId(getAccounts().getSuspenseAccount().getAccountID().longValue())
                .interestOnLoanAccountId(getAccounts().getInterestIncomeAccount().getAccountID().longValue())
                .incomeFromFeeAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())
                .incomeFromPenaltyAccountId(getAccounts().getPenaltyIncomeAccount().getAccountID().longValue())
                .incomeFromRecoveryAccountId(getAccounts().getRecoveriesAccount().getAccountID().longValue())
                .writeOffAccountId(getAccounts().getWrittenOffAccount().getAccountID().longValue())
                .overpaymentLiabilityAccountId(getAccounts().getOverpaymentAccount().getAccountID().longValue())
                .dateFormat(DATETIME_PATTERN).locale("en_GB").disallowExpectedDisbursements(true)
                .allowApprovedDisbursedAmountsOverApplied(true).overAppliedCalculationType("percentage").overAppliedNumber(50);
    }

    /**
     * Creates a FLAT, SPECIFIED_DUE_DATE charge definition linked to the given {@code taxGroupId}. Passing {@code null}
     * for {@code taxGroupId} creates a charge with no tax.
     */
    private PostChargesResponse createFlatLoanCharge(double baseAmount, Long taxGroupId) {
        ChargeRequest request = new ChargeRequest().penalty(false).amount(baseAmount)
                .chargeCalculationType(ChargeCalculationType.FLAT.getValue()).chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue())
                .chargePaymentMode(ChargePaymentMode.REGULAR.getValue()).currencyCode("USD")
                .name(Utils.uniqueRandomStringGenerator("CHARGE_", 6)).chargeAppliesTo(1).locale(LoanTestData.LOCALE).active(true);
        if (taxGroupId != null) {
            request.taxGroupId(taxGroupId);
        }
        PostChargesResponse response = chargesHelper.createCharge(request);
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
            PostChargesResponse chargeResponse = createFlatLoanCharge(100.0, taxGroup.getResourceId());
            Long chargeDefinitionId = chargeResponse.getResourceId();

            Long clientId = createClient();
            // Given – a disbursed loan
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());
            Long loanId = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), LOAN_DATE);

            // When – the taxed charge is added to the loan
            PostLoansLoanIdChargesResponse addResult = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest()
                    .chargeId(chargeDefinitionId).amount(100.0).dueDate(DUE_DATE).dateFormat(DATE_FORMAT).locale(LoanTestData.LOCALE));
            assertNotNull(addResult);
            Long loanChargeId = addResult.getResourceId();
            assertNotNull(loanChargeId);

            // Then – amount stays at 100; tax (10) is stored separately as taxAmount
            GetLoansLoanIdChargesChargeIdResponse loanCharge = getLoanCharge(loanId, loanChargeId);
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

            PostChargesResponse chargeResponse = createFlatLoanCharge(200.0, taxGroup.getResourceId());

            Long clientId = createClient();
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());
            Long loanId = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), LOAN_DATE);

            // When
            PostLoansLoanIdChargesResponse addResult = addChargesForLoan(loanId,
                    new PostLoansLoanIdChargesRequest().chargeId(chargeResponse.getResourceId()).amount(200.0).dueDate(DUE_DATE)
                            .dateFormat(DATE_FORMAT).locale(LoanTestData.LOCALE));
            Long loanChargeId = addResult.getResourceId();

            // Then – amount stays 200; taxes (20+10=30) stored separately as taxAmount
            GetLoansLoanIdChargesChargeIdResponse loanCharge = getLoanCharge(loanId, loanChargeId);
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
            PostChargesResponse chargeResponse = createFlatLoanCharge(100.0, null);

            Long clientId = createClient();
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());
            Long loanId = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), LOAN_DATE);

            // When
            PostLoansLoanIdChargesResponse addResult = addChargesForLoan(loanId,
                    new PostLoansLoanIdChargesRequest().chargeId(chargeResponse.getResourceId()).amount(100.0).dueDate(DUE_DATE)
                            .dateFormat(DATE_FORMAT).locale(LoanTestData.LOCALE));
            Long loanChargeId = addResult.getResourceId();

            // Then – amount must stay at 100 (no tax)
            GetLoansLoanIdChargesChargeIdResponse loanCharge = getLoanCharge(loanId, loanChargeId);
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
        PostChargesResponse chargeResponse = createFlatLoanCharge(50.0, taxGroup.getResourceId());

        // Then – retrieve and verify taxGroup is set
        GetChargesResponse chargeData = chargesHelper.getCharge(chargeResponse.getResourceId());
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
        GetTaxesGroupResponse retrieved = taxGroupHelper.retrieveTaxGroup(taxGroup.getResourceId());

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
        List<GetTaxesGroupResponse> allGroups = taxGroupHelper.retrieveAllTaxGroups();

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
            PostChargesResponse chargeResponse = createFlatLoanCharge(100.0, taxGroup.getResourceId());
            Long chargeDefinitionId = chargeResponse.getResourceId();

            Long clientId = createClient();
            // Given – two separate loans
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());

            Long loanId1 = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId1, BigDecimal.valueOf(1000.0), LOAN_DATE);

            Long loanId2 = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId2, BigDecimal.valueOf(1000.0), LOAN_DATE);

            // When – the same charge is added to both loans
            PostLoansLoanIdChargesResponse add1 = addChargesForLoan(loanId1, new PostLoansLoanIdChargesRequest()
                    .chargeId(chargeDefinitionId).amount(100.0).dueDate(DUE_DATE).dateFormat(DATE_FORMAT).locale(LoanTestData.LOCALE));
            PostLoansLoanIdChargesResponse add2 = addChargesForLoan(loanId2, new PostLoansLoanIdChargesRequest()
                    .chargeId(chargeDefinitionId).amount(100.0).dueDate(DUE_DATE).dateFormat(DATE_FORMAT).locale(LoanTestData.LOCALE));

            GetLoansLoanIdChargesChargeIdResponse charge1 = getLoanCharge(loanId1, add1.getResourceId());
            // Then – both loan charges must independently show the original base amount (tax stored separately)
            GetLoansLoanIdChargesChargeIdResponse charge2 = getLoanCharge(loanId2, add2.getResourceId());

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
            PostChargesResponse chargeResponse = createFlatLoanCharge(100.0, taxGroup.getResourceId());

            Long clientId = createClient();
            // Given – a cash-based loan (principal = 1000, no interest)
            Long loanProductId = createLoanProduct(
                    createCashBasedLoanProduct().multiDisburseLoan(false).disallowExpectedDisbursements(null));
            Long loanId = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), LOAN_DATE);

            // When – add the taxed charge (due on same date) and repay principal + fee in one transaction
            addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().chargeId(chargeResponse.getResourceId()).amount(100.0)
                    .dueDate(LOAN_DATE).dateFormat(DATE_FORMAT).locale(LoanTestData.LOCALE));

            addRepaymentForLoan(loanId, 1100.0, LOAN_DATE);

            // Then – verify journal entries:
            // Disbursement: DR Loan Portfolio 1000 / CR Fund Source 1000
            // Repayment: DR Fund Source 1100 / CR Loan Portfolio 1000 / CR Income from Fees 90 / CR Tax Liability 10
            verifyJournalEntries(loanId,
                    // disbursement
                    debit(getAccounts().getLoansReceivableAccount(), 1000.0), credit(getAccounts().getFundSource(), 1000.0),
                    // repayment
                    debit(getAccounts().getFundSource(), 1100.0), credit(getAccounts().getLoansReceivableAccount(), 1000.0),
                    credit(getAccounts().getFeeIncomeAccount(), 90.0), credit(taxLiabilityAccount, 10.0));
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
            PostChargesResponse chargeResponse = createFlatLoanCharge(100.0, taxGroup.getResourceId());

            Long clientId = createClient();
            // Given – a periodic-accrual loan (principal = 1000, no interest)
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());
            Long loanId = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), LOAN_DATE);

            // When – add the taxed charge and run periodic accrual on the same date
            addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().chargeId(chargeResponse.getResourceId()).amount(100.0)
                    .dueDate(LOAN_DATE).dateFormat(DATE_FORMAT).locale(LoanTestData.LOCALE));

            runPeriodicAccrualAccounting(LOAN_DATE);

            // Make a full repayment (principal 1000 + fee 100 = 1100)
            addRepaymentForLoan(loanId, 1100.0, LOAN_DATE);

            // Then – verify ALL journal entries:
            // Disbursement: DR Loan Portfolio 1000 / CR Fund Source 1000
            // Accrual: DR Fees Receivable 100 / CR Income from Fees 90 / CR Tax Liability 10
            // Repayment: DR Fund Source 1100 / CR Loan Portfolio 1000 / CR Fees Receivable 100
            verifyJournalEntries(loanId,
                    // disbursement
                    debit(getAccounts().getLoansReceivableAccount(), 1000.0), credit(getAccounts().getFundSource(), 1000.0),
                    // accrual
                    debit(getAccounts().getFeeReceivableAccount(), 100.0), credit(getAccounts().getFeeIncomeAccount(), 90.0),
                    credit(taxLiabilityAccount, 10.0),
                    // repayment
                    debit(getAccounts().getFundSource(), 1100.0), credit(getAccounts().getLoansReceivableAccount(), 1000.0),
                    credit(getAccounts().getFeeReceivableAccount(), 100.0));
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
            PostChargesResponse chargeResponse = createFlatLoanCharge(100.0, taxGroup.getResourceId());

            Long clientId = createClient();
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());
            Long loanId = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), LOAN_DATE);

            PostLoansLoanIdChargesResponse addResult = addChargesForLoan(loanId,
                    new PostLoansLoanIdChargesRequest().chargeId(chargeResponse.getResourceId()).amount(100.0).dueDate(DUE_DATE)
                            .dateFormat(DATE_FORMAT).locale(LoanTestData.LOCALE));
            Long loanChargeId = addResult.getResourceId();

            // When – retrieve the full charge list
            List<GetLoansLoanIdChargesChargeIdResponse> charges = getLoanCharges(loanId);

            // Then – the taxed charge must appear in the list with the correct amount
            assertNotNull(charges);
            GetLoansLoanIdChargesChargeIdResponse found = charges.stream().filter(c -> loanChargeId.equals(c.getId())).findFirst()
                    .orElse(null);
            assertNotNull(found, "The added loan charge must appear in the charge list");
            assertEquals(100.0, found.getAmount(), 0.01,
                    "Listed charge amount must remain 100; the 10 % tax is stored separately as taxAmount");
        });
    }

    // -----------------------------------------------------------------------
    // 12. Partial payments: prorated tax rounded to the currency's scale
    // -----------------------------------------------------------------------

    /**
     * A taxed fee paid in three parts. Each tax line is rounded to the currency's scale, cumulatively, so the lines sum
     * to the fee's tax exactly and a payment too small to carry any tax posts no tax line.
     *
     * <pre>
     *   fee 2952.00, tax 551.999971 (as stored; 552.00 rounded)
     *   pay    0.01: tax   0.00 (no line), income    0.01
     *   pay 1000.00: tax 186.99,            income  813.01
     *   pay 1951.99: tax 365.01,            income 1586.98   (tax total 552.00)
     * </pre>
     */
    @Test
    public void testCashAccounting_partialPaymentsOfTaxedFee_taxLinesAreRoundedCumulatively() {
        Account taxLiabilityAccount = accountHelper.createLiabilityAccount("taxLiability_partial");
        Long loanId = createCashLoanWithTaxedFee(taxLiabilityAccount, 2952.0);

        runAt(AFTER_MATURITY, () -> {
            repayPrincipal(loanId);
            Long first = addRepaymentForLoan(loanId, 0.01, AFTER_MATURITY);
            Long second = addRepaymentForLoan(loanId, 1000.0, AFTER_MATURITY);
            Long third = addRepaymentForLoan(loanId, 1951.99, AFTER_MATURITY);

            verifyTaxAndFeeIncome(loanId, first, taxLiabilityAccount, null, "0.01");
            verifyTaxAndFeeIncome(loanId, second, taxLiabilityAccount, "186.99", "813.01");
            verifyTaxAndFeeIncome(loanId, third, taxLiabilityAccount, "365.01", "1586.98");
            assertEquals(0, new BigDecimal("552.00").compareTo(netCredit(loanId, taxLiabilityAccount)));
            verifyEveryJournalLineIsAtCurrencyScale(loanId);
        });
    }

    /**
     * A tax that does not divide evenly: fee 1000.00, tax 186.991860 (as stored). Paid 400.00 then 600.00, the tax
     * lines are 74.80 and 112.19 (sum 186.99), not 74.796744 and 112.195116.
     */
    @Test
    public void testCashAccounting_taxThatDoesNotDivideEvenly_linesSumToTheRoundedTax() {
        Account taxLiabilityAccount = accountHelper.createLiabilityAccount("taxLiability_uneven");
        Long loanId = createCashLoanWithTaxedFee(taxLiabilityAccount, 1000.0);

        runAt(AFTER_MATURITY, () -> {
            repayPrincipal(loanId);
            Long first = addRepaymentForLoan(loanId, 400.0, AFTER_MATURITY);
            Long second = addRepaymentForLoan(loanId, 600.0, AFTER_MATURITY);

            verifyTaxAndFeeIncome(loanId, first, taxLiabilityAccount, "74.80", "325.20");
            verifyTaxAndFeeIncome(loanId, second, taxLiabilityAccount, "112.19", "487.81");
            verifyEveryJournalLineIsAtCurrencyScale(loanId);
        });
    }

    /**
     * Undoing a partial payment reverses its lines. The tax still posted, 0.00 + 365.01, here equals the rounded tax of
     * the payments left (0.01 + 1951.99 of a 2952.00 fee carrying 551.999971: round(551.999971 * 1952 / 2952) =
     * 365.01).
     */
    @Test
    public void testCashAccounting_undoPartialPayment_remainingTaxIsRounded() {
        Account taxLiabilityAccount = accountHelper.createLiabilityAccount("taxLiability_undo");
        Long loanId = createCashLoanWithTaxedFee(taxLiabilityAccount, 2952.0);

        runAt(AFTER_MATURITY, () -> {
            repayPrincipal(loanId);
            addRepaymentForLoan(loanId, 0.01, AFTER_MATURITY);
            Long second = addRepaymentForLoan(loanId, 1000.0, AFTER_MATURITY);
            addRepaymentForLoan(loanId, 1951.99, AFTER_MATURITY);

            undoRepayment(loanId, second, AFTER_MATURITY);

            assertEquals(0, new BigDecimal("365.01").compareTo(netCredit(loanId, taxLiabilityAccount)));
            verifyEveryJournalLineIsAtCurrencyScale(loanId);
        });
    }

    /**
     * Periodic accrual of a taxed fee whose tax does not divide evenly: the accrual posts the tax rounded (186.99), and
     * the net income takes the rest (813.01).
     */
    @Test
    public void testAccrualAccounting_accrualOfTaxedFee_taxLineIsRounded() {
        Account taxLiabilityAccount = accountHelper.createLiabilityAccount("taxLiability_accrual_round");
        runAt(LOAN_DATE, () -> {
            PostChargesResponse charge = createTaxedFee(taxLiabilityAccount, 1000.0);
            Long clientId = createClient();
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());
            Long loanId = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), LOAN_DATE);
            addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().chargeId(charge.getResourceId()).amount(1000.0).dueDate(LOAN_DATE)
                    .dateFormat(DATE_FORMAT).locale(LoanTestData.LOCALE));

            runPeriodicAccrualAccounting(LOAN_DATE);

            assertEquals(0, new BigDecimal("186.99").compareTo(netCredit(loanId, taxLiabilityAccount)));
            assertEquals(0, new BigDecimal("813.01").compareTo(netCredit(loanId, getAccounts().getFeeIncomeAccount())));
            verifyEveryJournalLineIsAtCurrencyScale(loanId);
        });
    }

    private PostChargesResponse createTaxedFee(Account taxLiabilityAccount, double amount) {
        PostTaxesComponentsResponse taxComponent = createTaxComponent(TAX_RATE_23_IN_123, taxLiabilityAccount.getAccountID().longValue());
        PostTaxesGroupResponse taxGroup = createTaxGroup(taxComponent.getResourceId());
        return createFlatLoanCharge(amount, taxGroup.getResourceId());
    }

    /** A disbursed cash-based loan of 1000.00 with a taxed fee due on the disbursement date. */
    private Long createCashLoanWithTaxedFee(Account taxLiabilityAccount, double feeAmount) {
        Long[] loanId = new Long[1];
        runAt(LOAN_DATE, () -> {
            PostChargesResponse charge = createTaxedFee(taxLiabilityAccount, feeAmount);
            Long clientId = createClient();
            Long loanProductId = createLoanProduct(
                    createCashBasedLoanProduct().multiDisburseLoan(false).disallowExpectedDisbursements(null));
            loanId[0] = applyAndApproveLoan(clientId, loanProductId, LOAN_DATE, 1000.0);
            disburseLoan(loanId[0], BigDecimal.valueOf(1000.0), LOAN_DATE);
            addChargesForLoan(loanId[0], new PostLoansLoanIdChargesRequest().chargeId(charge.getResourceId()).amount(feeAmount)
                    .dueDate(LOAN_DATE).dateFormat(DATE_FORMAT).locale(LoanTestData.LOCALE));
        });
        return loanId[0];
    }

    /** The product pays principal before fees, so the principal goes first and the payments after it reach the fee. */
    private void repayPrincipal(Long loanId) {
        addRepaymentForLoan(loanId, 1000.0, AFTER_MATURITY);
    }

    /** Exact amounts: the journal helper's own matching accepts any difference below 0.01. */
    private void verifyTaxAndFeeIncome(Long loanId, Long transactionId, Account taxLiabilityAccount, String expectedTax,
            String expectedFeeIncome) {
        List<JournalEntryTransactionItem> lines = journalLines(loanId, transactionId);
        BigDecimal tax = sumCredits(lines, taxLiabilityAccount);
        if (expectedTax == null) {
            assertNull(tax, "no tax line expected on transaction " + transactionId);
        } else {
            assertNotNull(tax, "tax line expected on transaction " + transactionId);
            assertEquals(0, new BigDecimal(expectedTax).compareTo(tax), "tax on transaction " + transactionId + ": " + tax);
        }
        BigDecimal income = sumCredits(lines, getAccounts().getFeeIncomeAccount());
        assertNotNull(income, "fee income line expected on transaction " + transactionId);
        assertEquals(0, new BigDecimal(expectedFeeIncome).compareTo(income), "fee income on transaction " + transactionId + ": " + income);
    }

    private void verifyEveryJournalLineIsAtCurrencyScale(Long loanId) {
        for (JournalEntryTransactionItem line : journalHelper.getJournalEntriesForLoan(loanId).getPageItems()) {
            BigDecimal amount = BigDecimal.valueOf(line.getAmount()).stripTrailingZeros();
            assertTrue(amount.scale() <= 2, "journal line " + line.getId() + " is not at the currency's scale: " + amount);
        }
    }

    private List<JournalEntryTransactionItem> journalLines(Long loanId, Long transactionId) {
        return journalHelper.getJournalEntriesForLoan(loanId).getPageItems().stream()
                .filter(line -> ("L" + transactionId).equals(line.getTransactionId())).toList();
    }

    private static BigDecimal sumCredits(List<JournalEntryTransactionItem> lines, Account account) {
        return lines.stream().filter(line -> isOn(line, account) && "CREDIT".equals(line.getEntryType().getValue()))
                .map(line -> BigDecimal.valueOf(line.getAmount())).reduce(BigDecimal::add).orElse(null);
    }

    /** Credits less debits on the account over all the loan's journal lines, reversals included. */
    private BigDecimal netCredit(Long loanId, Account account) {
        return journalHelper.getJournalEntriesForLoan(loanId).getPageItems().stream().filter(line -> isOn(line, account))
                .map(line -> "CREDIT".equals(line.getEntryType().getValue()) ? BigDecimal.valueOf(line.getAmount())
                        : BigDecimal.valueOf(line.getAmount()).negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static boolean isOn(JournalEntryTransactionItem line, Account account) {
        return Long.valueOf(account.getAccountID().longValue()).equals(line.getGlAccountId());
    }
}
