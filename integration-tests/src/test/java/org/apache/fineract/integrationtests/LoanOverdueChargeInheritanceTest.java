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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.apache.fineract.client.models.BusinessDateUpdateRequest;
import org.apache.fineract.client.models.GetLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanOverdueChargeInheritanceTest extends IntegrationTest {

    private static final String LOAN_START_DATE = "04 January 2024";
    private static final LocalDate FIRST_INSTALLMENT_DUE_DATE = LocalDate.of(2024, 2, 4);
    private static final DateTimeFormatter BUSINESS_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private LoanTransactionHelper loanTransactionHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
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
        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientID);

        final Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanOverdueFeeJSON());
        Assertions.assertNotNull(overdueFeeChargeId);

        final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
        final Integer loanID = applyApproveAndDisburseLoan(clientID.toString(), loanProductID.toString());

        updateBusinessDate(LocalDate.of(2024, 2, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");
        assertOverdueChargeAmount(loanID, overdueFeeChargeId, FIRST_INSTALLMENT_DUE_DATE, 100.0,
                "First overdue charge should use the charge amount that was in effect when the loan was created");

        ChargesHelper.updateCharges(requestSpec, responseSpec, overdueFeeChargeId, modifyChargeAmountJSON(200));

        updateBusinessDate(LocalDate.of(2024, 4, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");
        assertOverdueChargeAmount(loanID, overdueFeeChargeId, FIRST_INSTALLMENT_DUE_DATE.plusMonths(2), 100.0,
                "Overdue charge amount for an existing loan must stay at the amount captured at loan creation time, "
                        + "even after the charge product's amount is changed once");

        ChargesHelper.updateCharges(requestSpec, responseSpec, overdueFeeChargeId, modifyChargeAmountJSON(300));

        updateBusinessDate(LocalDate.of(2024, 6, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");
        assertOverdueChargeAmount(loanID, overdueFeeChargeId, FIRST_INSTALLMENT_DUE_DATE.plusMonths(4), 100.0,
                "Overdue charge amount for an existing loan must stay at the amount captured at loan creation time, "
                        + "even after the charge product's amount is changed repeatedly");
    }

    @Test
    public void existingLoanKeepsOverdueChargeAmountWhenAppliedViaCloseOfBusiness() {
        new BusinessStepHelper().updateSteps("LOAN_CLOSE_OF_BUSINESS", "APPLY_CHARGE_TO_OVERDUE_LOANS");

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientID);

        final Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanOverdueFeeJSON());
        Assertions.assertNotNull(overdueFeeChargeId);

        final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
        final Integer loanID = applyApproveAndDisburseLoan(clientID.toString(), loanProductID.toString());

        ChargesHelper.updateCharges(requestSpec, responseSpec, overdueFeeChargeId, modifyChargeAmountJSON(200));

        updateBusinessDate(LocalDate.of(2024, 2, 8));
        SchedulerJobHelper.executeAndAwaitJob("Loan COB");
        assertOverdueChargeAmount(loanID, overdueFeeChargeId, FIRST_INSTALLMENT_DUE_DATE, 100.0,
                "Overdue charge applied via Close of Business must use the amount captured when the loan was created, "
                        + "not the charge product's current amount");
    }

    @Test
    public void existingLoanKeepsOverduePercentageChargeAfterProductPercentageIsChanged() {
        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientID);

        final Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("2"));
        Assertions.assertNotNull(overdueFeeChargeId);

        final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
        final Integer loanID = applyApproveAndDisburseLoan(clientID.toString(), loanProductID.toString());

        updateBusinessDate(LocalDate.of(2024, 2, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");
        final GetLoansLoanIdChargesChargeIdResponse firstInstallmentCharge = findOverdueCharge(loanID, overdueFeeChargeId,
                FIRST_INSTALLMENT_DUE_DATE);
        Assertions.assertEquals(Double.valueOf(2.0), firstInstallmentCharge.getPercentage(),
                "First overdue charge should use the percentage that was in effect when the loan was created");

        // The charge product's percentage is changed afterwards (2% -> 5%).
        ChargesHelper.updateCharges(requestSpec, responseSpec, overdueFeeChargeId, modifyChargeAmountJSON(5));

        updateBusinessDate(LocalDate.of(2024, 3, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");
        final GetLoansLoanIdChargesChargeIdResponse secondInstallmentCharge = findOverdueCharge(loanID, overdueFeeChargeId,
                FIRST_INSTALLMENT_DUE_DATE.plusMonths(1));
        Assertions.assertEquals(Double.valueOf(2.0), secondInstallmentCharge.getPercentage(),
                "A later installment becoming overdue for the first time must still use the loan's original percentage snapshot, "
                        + "even after the charge product's percentage is changed");
    }

    @Test
    public void loanApplicationProductChangeBeforeApprovalUpdatesOverdueChargeSnapshot() {
        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientID);

        final Integer originalChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanOverdueFeeJSON());
        final Integer originalLoanProductId = createLoanProduct(originalChargeId.toString());

        final Integer newChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanOverdueFeeJSON());
        ChargesHelper.updateCharges(requestSpec, responseSpec, newChargeId, modifyChargeAmountJSON(200));
        final Integer newLoanProductId = createLoanProduct(newChargeId.toString());

        final Integer loanID = submitLoanApplication(clientID.toString(), originalLoanProductId.toString());
        switchLoanApplicationProduct(loanID, clientID.toString(), newLoanProductId.toString());
        approveAndDisburseLoan(loanID);

        updateBusinessDate(LocalDate.of(2024, 2, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");

        assertOverdueChargeAmount(loanID, newChargeId, FIRST_INSTALLMENT_DUE_DATE, 200.0,
                "Switching the loan application's product before approval must update the overdue-charge snapshot "
                        + "to the newly-selected product's amount");
    }

    @Test
    public void loanWithoutOverdueChargeAtCreationUsesLiveProductAmountOnceChargeIsAttached() {
        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientID);

        final Integer loanProductID = createLoanProduct(null);
        final Integer loanID = applyApproveAndDisburseLoan(clientID.toString(), loanProductID.toString());

        final Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanOverdueFeeJSON());
        loanTransactionHelper.updateLoanProduct(loanProductID.longValue(), new PutLoanProductsProductIdRequest()
                .charges(List.of(new LoanProductChargeData().id(overdueFeeChargeId.longValue()))).currencyCode("USD").locale("en"));

        updateBusinessDate(LocalDate.of(2024, 2, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");

        assertOverdueChargeAmount(loanID, overdueFeeChargeId, FIRST_INSTALLMENT_DUE_DATE, 100.0,
                "A loan whose product had no overdue charge at creation time never captured a snapshot, so it must "
                        + "fall back to the product's current amount once a charge is attached");
    }

    @Test
    public void newLoanUsesCurrentProductAmountAfterOverdueChargeIsChanged() {
        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientID);

        final Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanOverdueFeeJSON());
        Assertions.assertNotNull(overdueFeeChargeId);

        ChargesHelper.updateCharges(requestSpec, responseSpec, overdueFeeChargeId, modifyChargeAmountJSON(200));

        final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
        final Integer loanID = applyApproveAndDisburseLoan(clientID.toString(), loanProductID.toString());

        updateBusinessDate(LocalDate.of(2024, 2, 7));
        SchedulerJobHelper.executeAndAwaitJob("Apply penalty to overdue loans");

        assertOverdueChargeAmount(loanID, overdueFeeChargeId, FIRST_INSTALLMENT_DUE_DATE, 200.0,
                "A loan created after the charge product amount changed must use the new amount");
    }

    private void assertOverdueChargeAmount(final Integer loanID, final Integer overdueFeeChargeId, final LocalDate expectedDueDate,
            final double expectedAmount, final String message) {
        final GetLoansLoanIdChargesChargeIdResponse charge = findOverdueCharge(loanID, overdueFeeChargeId, expectedDueDate);
        Assertions.assertEquals(Double.valueOf(expectedAmount), charge.getAmount(), message);
    }

    private GetLoansLoanIdChargesChargeIdResponse findOverdueCharge(final Integer loanID, final Integer overdueFeeChargeId,
            final LocalDate expectedDueDate) {
        final List<GetLoansLoanIdChargesChargeIdResponse> charges = loanTransactionHelper.getLoanCharges(loanID.longValue());
        final Optional<GetLoansLoanIdChargesChargeIdResponse> overdueCharge = charges.stream()
                .filter(charge -> overdueFeeChargeId.longValue() == charge.getChargeId())
                .filter(charge -> expectedDueDate.equals(charge.getDueDate())).findFirst();
        Assertions.assertTrue(overdueCharge.isPresent(), "Expecting an overdue charge due on " + expectedDueDate);
        return overdueCharge.get();
    }

    private String buildLoanApplicationJson(final String clientID, final String loanProductID) {
        return new LoanApplicationTestBuilder().withPrincipal("15,000.00").withLoanTermFrequency("4").withLoanTermFrequencyAsMonths()
                .withNumberOfRepayments("4").withRepaymentEveryAfter("1").withRepaymentFrequencyTypeAsMonths()
                .withInterestRatePerPeriod("1").withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate(LOAN_START_DATE)
                .withSubmittedOnDate(LOAN_START_DATE).build(clientID, loanProductID, null);
    }

    private Integer submitLoanApplication(final String clientID, final String loanProductID) {
        final Integer loanID = loanTransactionHelper.getLoanId(buildLoanApplicationJson(clientID, loanProductID));
        Assertions.assertNotNull(loanID);
        return loanID;
    }

    private void switchLoanApplicationProduct(final Integer loanID, final String clientID, final String newLoanProductID) {
        loanTransactionHelper.updateLoan(loanID, buildLoanApplicationJson(clientID, newLoanProductID));
    }

    private Integer approveAndDisburseLoan(final Integer loanID) {
        loanTransactionHelper.approveLoan(LOAN_START_DATE, loanID);
        final String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount(LOAN_START_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        return loanID;
    }

    private Integer applyApproveAndDisburseLoan(final String clientID, final String loanProductID) {
        return approveAndDisburseLoan(submitLoanApplication(clientID, loanProductID));
    }

    private static void updateBusinessDate(final LocalDate date) {
        BusinessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                .date(BUSINESS_DATE_FORMATTER.format(date)).dateFormat("dd MMMM yyyy").locale("en"));
    }

    private static String modifyChargeAmountJSON(final double amount) {
        return "{\"locale\":\"en\",\"amount\":" + amount + "}";
    }

    private Integer createLoanProduct(final String chargeId) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .build(chargeId);
        return loanTransactionHelper.getLoanProductId(loanProductJSON);
    }
}
