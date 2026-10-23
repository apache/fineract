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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.apache.fineract.client.models.BusinessDateUpdateRequest;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanOverdueChargeSnapshotTest extends FeignIntegrationTest {

    private static final String LOAN_START_DATE = "04 January 2024";
    private static final LocalDate FIRST_INSTALLMENT_DUE_DATE = LocalDate.of(2024, 2, 4);
    private static final DateTimeFormatter BUSINESS_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    private final GlobalConfigurationHelper globalConfigurationHelper = new GlobalConfigurationHelper();

    private FeignClientHelper clientHelper;
    private FeignLoanHelper loanHelper;
    private FeignChargesHelper chargesHelper;

    @BeforeEach
    public void setup() {
        clientHelper = new FeignClientHelper(fineractClient());
        loanHelper = new FeignLoanHelper(fineractClient());
        chargesHelper = new FeignChargesHelper(fineractClient());
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        updateBusinessDate(LocalDate.of(2024, 1, 4));
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }

    @Test
    public void existingLoanKeepsOverdueChargeAmountAcrossMultipleProductAmountChanges() {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        final Long overdueFeeChargeId = chargesHelper.createCharge(overdueFeeCharge()).getResourceId();
        Assertions.assertNotNull(overdueFeeChargeId);

        final Long loanProductID = createLoanProduct(overdueFeeChargeId.toString());
        final Long loanID = applyApproveAndDisburseLoan(clientID, loanProductID);

        updateBusinessDate(LocalDate.of(2024, 2, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");
        assertOverdueChargeAmount(loanID, overdueFeeChargeId, FIRST_INSTALLMENT_DUE_DATE, 100.0,
                "First overdue charge should use the charge amount that was in effect when the loan was created");

        chargesHelper.updateCharge(overdueFeeChargeId, new ChargeRequest().locale("en").amount(200.0));

        updateBusinessDate(LocalDate.of(2024, 4, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");
        assertOverdueChargeAmount(loanID, overdueFeeChargeId, FIRST_INSTALLMENT_DUE_DATE.plusMonths(2), 100.0,
                "Overdue charge amount for an existing loan must stay at the amount captured at loan creation time, "
                        + "even after the charge product's amount is changed once");

        chargesHelper.updateCharge(overdueFeeChargeId, new ChargeRequest().locale("en").amount(300.0));

        updateBusinessDate(LocalDate.of(2024, 6, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");
        assertOverdueChargeAmount(loanID, overdueFeeChargeId, FIRST_INSTALLMENT_DUE_DATE.plusMonths(4), 100.0,
                "Overdue charge amount for an existing loan must stay at the amount captured at loan creation time, "
                        + "even after the charge product's amount is changed repeatedly");
    }

    @Test
    public void existingLoanKeepsOverdueChargeAmountWhenAppliedViaCloseOfBusiness() {
        new BusinessStepHelper().updateSteps("LOAN_CLOSE_OF_BUSINESS", "APPLY_CHARGE_TO_OVERDUE_LOANS");

        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        final Long overdueFeeChargeId = chargesHelper.createCharge(overdueFeeCharge()).getResourceId();
        Assertions.assertNotNull(overdueFeeChargeId);

        final Long loanProductID = createLoanProduct(overdueFeeChargeId.toString());
        final Long loanID = applyApproveAndDisburseLoan(clientID, loanProductID);

        chargesHelper.updateCharge(overdueFeeChargeId, new ChargeRequest().locale("en").amount(200.0));

        updateBusinessDate(LocalDate.of(2024, 2, 8));
        SchedulerJobHelper.executeAndAwaitJob("Loan COB");
        assertOverdueChargeAmount(loanID, overdueFeeChargeId, FIRST_INSTALLMENT_DUE_DATE, 100.0,
                "Overdue charge applied via Close of Business must use the amount captured when the loan was created, "
                        + "not the charge product's current amount");
    }

    @Test
    public void existingLoanKeepsOverduePercentageChargeAfterProductPercentageIsChanged() {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        final Long overdueFeeChargeId = chargesHelper.createCharge(overdueFeePercentageCharge(2.0)).getResourceId();
        Assertions.assertNotNull(overdueFeeChargeId);

        final Long loanProductID = createLoanProduct(overdueFeeChargeId.toString());
        final Long loanID = applyApproveAndDisburseLoan(clientID, loanProductID);

        updateBusinessDate(LocalDate.of(2024, 2, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");
        final GetLoansLoanIdChargesChargeIdResponse firstInstallmentCharge = findOverdueCharge(loanID, overdueFeeChargeId,
                FIRST_INSTALLMENT_DUE_DATE);
        Assertions.assertEquals(Double.valueOf(2.0), firstInstallmentCharge.getPercentage(),
                "First overdue charge should use the percentage that was in effect when the loan was created");

        // The charge product's percentage is changed afterwards (2% -> 5%).
        chargesHelper.updateCharge(overdueFeeChargeId, new ChargeRequest().locale("en").amount(5.0));

        updateBusinessDate(LocalDate.of(2024, 3, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");
        final GetLoansLoanIdChargesChargeIdResponse secondInstallmentCharge = findOverdueCharge(loanID, overdueFeeChargeId,
                FIRST_INSTALLMENT_DUE_DATE.plusMonths(1));
        Assertions.assertEquals(Double.valueOf(2.0), secondInstallmentCharge.getPercentage(),
                "A later installment becoming overdue for the first time must still use the loan's original percentage snapshot, "
                        + "even after the charge product's percentage is changed");
    }

    @Test
    public void existingLoanKeepsFlatOverdueChargeAfterProductChargeSwitchesToPercentage() {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        final Long overdueFeeChargeId = chargesHelper.createCharge(overdueFeeCharge()).getResourceId();
        Assertions.assertNotNull(overdueFeeChargeId);

        final Long loanProductID = createLoanProduct(overdueFeeChargeId.toString());
        final Long loanID = applyApproveAndDisburseLoan(clientID, loanProductID);

        // The charge product's calculation type is switched afterwards (flat -> percentage of amount), keeping the
        // same numeric amount, so a flat 100 would become a 100% rate if the calculation type were not snapshotted.
        chargesHelper.updateCharge(overdueFeeChargeId,
                new ChargeRequest().locale("en").chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue()));

        updateBusinessDate(LocalDate.of(2024, 2, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");
        final GetLoansLoanIdChargesChargeIdResponse firstInstallmentCharge = findOverdueCharge(loanID, overdueFeeChargeId,
                FIRST_INSTALLMENT_DUE_DATE);
        Assertions.assertEquals(Double.valueOf(100.0), firstInstallmentCharge.getAmount(),
                "Overdue charge must stay flat using the calculation type captured when the loan was created, "
                        + "even after the charge product's calculation type is switched to percentage");
    }

    @Test
    public void existingLoanKeepsFlatOverdueChargeAfterProductChargeSwitchesToPercentageViaCloseOfBusiness() {
        new BusinessStepHelper().updateSteps("LOAN_CLOSE_OF_BUSINESS", "APPLY_CHARGE_TO_OVERDUE_LOANS");

        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        final Long overdueFeeChargeId = chargesHelper.createCharge(overdueFeeCharge()).getResourceId();
        Assertions.assertNotNull(overdueFeeChargeId);

        final Long loanProductID = createLoanProduct(overdueFeeChargeId.toString());
        final Long loanID = applyApproveAndDisburseLoan(clientID, loanProductID);

        chargesHelper.updateCharge(overdueFeeChargeId,
                new ChargeRequest().locale("en").chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue()));

        updateBusinessDate(LocalDate.of(2024, 2, 8));
        SchedulerJobHelper.executeAndAwaitJob("Loan COB");
        final GetLoansLoanIdChargesChargeIdResponse firstInstallmentCharge = findOverdueCharge(loanID, overdueFeeChargeId,
                FIRST_INSTALLMENT_DUE_DATE);
        Assertions.assertEquals(Double.valueOf(100.0), firstInstallmentCharge.getAmount(),
                "Overdue charge applied via Close of Business must stay flat using the calculation type captured "
                        + "when the loan was created, even after the charge product's calculation type is switched to percentage");
    }

    @Test
    public void loanApplicationProductChangeBeforeApprovalUpdatesOverdueChargeSnapshot() {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        final Long originalChargeId = chargesHelper.createCharge(overdueFeeCharge()).getResourceId();
        final Long originalLoanProductId = createLoanProduct(originalChargeId.toString());

        final Long newChargeId = chargesHelper.createCharge(overdueFeeCharge()).getResourceId();
        chargesHelper.updateCharge(newChargeId, new ChargeRequest().locale("en").amount(200.0));
        final Long newLoanProductId = createLoanProduct(newChargeId.toString());

        final Long loanID = submitLoanApplication(clientID, originalLoanProductId);
        switchLoanApplicationProduct(loanID, clientID, newLoanProductId);
        approveAndDisburseLoan(loanID);

        updateBusinessDate(LocalDate.of(2024, 2, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");

        assertOverdueChargeAmount(loanID, newChargeId, FIRST_INSTALLMENT_DUE_DATE, 200.0,
                "Switching the loan application's product before approval must update the overdue-charge snapshot "
                        + "to the newly-selected product's amount");
    }

    @Test
    public void loanWithoutOverdueChargeAtCreationUsesLiveProductAmountOnceChargeIsAttached() {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        final Long loanProductID = createLoanProduct(null);
        final Long loanID = applyApproveAndDisburseLoan(clientID, loanProductID);

        final Long overdueFeeChargeId = chargesHelper.createCharge(overdueFeeCharge()).getResourceId();
        loanHelper.updateLoanProduct(loanProductID, new PutLoanProductsProductIdRequest()
                .charges(List.of(new LoanProductChargeData().id(overdueFeeChargeId))).currencyCode("USD").locale("en"));

        updateBusinessDate(LocalDate.of(2024, 2, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");

        assertOverdueChargeAmount(loanID, overdueFeeChargeId, FIRST_INSTALLMENT_DUE_DATE, 100.0,
                "A loan whose product had no overdue charge at creation time never captured a snapshot, so it must "
                        + "fall back to the product's current amount once a charge is attached");
    }

    @Test
    public void newLoanUsesCurrentProductAmountAfterOverdueChargeIsChanged() {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        final Long overdueFeeChargeId = chargesHelper.createCharge(overdueFeeCharge()).getResourceId();
        Assertions.assertNotNull(overdueFeeChargeId);

        chargesHelper.updateCharge(overdueFeeChargeId, new ChargeRequest().locale("en").amount(200.0));

        final Long loanProductID = createLoanProduct(overdueFeeChargeId.toString());
        final Long loanID = applyApproveAndDisburseLoan(clientID, loanProductID);

        updateBusinessDate(LocalDate.of(2024, 2, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");

        assertOverdueChargeAmount(loanID, overdueFeeChargeId, FIRST_INSTALLMENT_DUE_DATE, 200.0,
                "A loan created after the charge product amount changed must use the new amount");
    }

    @Test
    public void overdueChargeSwappedInProductFallsBackToNewChargeAmount() {
        final Long clientID = clientHelper.createClient();
        Assertions.assertNotNull(clientID);

        final Long originalChargeId = chargesHelper.createCharge(overdueFeeCharge()).getResourceId();
        Assertions.assertNotNull(originalChargeId);

        final Long loanProductID = createLoanProduct(originalChargeId.toString());
        final Long loanID = applyApproveAndDisburseLoan(clientID, loanProductID);

        // The product's overdue-installment charge is swapped for a different charge definition (not just amended).
        final Long replacementChargeId = chargesHelper.createCharge(overdueFeeCharge()).getResourceId();
        chargesHelper.updateCharge(replacementChargeId, new ChargeRequest().locale("en").amount(200.0));
        loanHelper.updateLoanProduct(loanProductID, new PutLoanProductsProductIdRequest()
                .charges(List.of(new LoanProductChargeData().id(replacementChargeId))).currencyCode("USD").locale("en"));

        updateBusinessDate(LocalDate.of(2024, 2, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");

        assertOverdueChargeAmount(loanID, replacementChargeId, FIRST_INSTALLMENT_DUE_DATE, 200.0,
                "Once the product's overdue-installment charge is swapped for a different charge, the loan's stale "
                        + "snapshot (tied to the old charge id) must be ignored in favour of the new charge's amount");
    }

    private void assertOverdueChargeAmount(final Long loanID, final Long overdueFeeChargeId, final LocalDate expectedDueDate,
            final double expectedAmount, final String message) {
        final GetLoansLoanIdChargesChargeIdResponse charge = findOverdueCharge(loanID, overdueFeeChargeId, expectedDueDate);
        Assertions.assertEquals(Double.valueOf(expectedAmount), charge.getAmount(), message);
    }

    private GetLoansLoanIdChargesChargeIdResponse findOverdueCharge(final Long loanID, final Long overdueFeeChargeId,
            final LocalDate expectedDueDate) {
        final List<GetLoansLoanIdChargesChargeIdResponse> charges = loanHelper.getLoanCharges(loanID);
        final Optional<GetLoansLoanIdChargesChargeIdResponse> overdueCharge = charges.stream()
                .filter(charge -> overdueFeeChargeId.equals(charge.getChargeId()))
                .filter(charge -> expectedDueDate.equals(charge.getDueDate())).findFirst();
        Assertions.assertTrue(overdueCharge.isPresent(), "Expecting an overdue charge due on " + expectedDueDate);
        return overdueCharge.get();
    }

    private PostLoansRequest buildLoanApplication(final Long clientID, final Long loanProductID) {
        return new PostLoansRequest()//
                .clientId(clientID)//
                .productId(loanProductID)//
                .principal(BigDecimal.valueOf(15000))//
                .loanTermFrequency(4)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.ONE)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .transactionProcessingStrategyCode(LoanProductTestBuilder.DEFAULT_STRATEGY)//
                .loanType("individual")//
                .expectedDisbursementDate(LOAN_START_DATE)//
                .submittedOnDate(LOAN_START_DATE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
    }

    private Long submitLoanApplication(final Long clientID, final Long loanProductID) {
        final Long loanID = loanHelper.applyForLoan(buildLoanApplication(clientID, loanProductID)).getLoanId();
        Assertions.assertNotNull(loanID);
        return loanID;
    }

    private void switchLoanApplicationProduct(final Long loanID, final Long clientID, final Long newLoanProductID) {
        final PutLoansLoanIdRequest request = new PutLoansLoanIdRequest()//
                .clientId(clientID)//
                .productId(newLoanProductID)//
                .principal(15000L)//
                .loanTermFrequency(4)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.ONE)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .transactionProcessingStrategyCode(LoanProductTestBuilder.DEFAULT_STRATEGY)//
                .loanType("individual")//
                .expectedDisbursementDate(LOAN_START_DATE)//
                .submittedOnDate(LOAN_START_DATE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
        loanHelper.modifyLoanApplication(loanID, null, request);
    }

    private Long approveAndDisburseLoan(final Long loanID) {
        loanHelper.approveLoan(loanID, new PostLoansLoanIdRequest()//
                .approvedOnDate(LOAN_START_DATE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE));
        loanHelper.disburseLoan(LOAN_START_DATE, loanID, "15000");
        return loanID;
    }

    private Long applyApproveAndDisburseLoan(final Long clientID, final Long loanProductID) {
        return approveAndDisburseLoan(submitLoanApplication(clientID, loanProductID));
    }

    private static void updateBusinessDate(final LocalDate date) {
        BusinessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                .date(BUSINESS_DATE_FORMATTER.format(date)).dateFormat("dd MMMM yyyy").locale("en"));
    }

    private static ChargeRequest overdueFeeCharge() {
        return ChargeRequestBuilders.loanOverdueFee(100.0)//
                .feeFrequency(String.valueOf(ChargesHelper.CHARGE_FEE_FREQUENCY_MONTHS))//
                .feeInterval("2")//
                .feeOnMonthDay(ChargesHelper.FEE_ON_MONTH_DAY)//
                .monthDayFormat("dd MMM");
    }

    private static ChargeRequest overdueFeePercentageCharge(final double percentage) {
        return ChargeRequestBuilders.loanOverdueFeePercentageOfAmountAndInterest(percentage);
    }

    private Long createLoanProduct(final String chargeId) {
        final PostLoanProductsRequest request = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .buildRequest(chargeId);
        return loanHelper.createLoanProduct(request).getResourceId();
    }
}
