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

import static java.lang.Double.parseDouble;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdDisbursementDetails;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentSchedule;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PutLoansLoanIdResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@SuppressWarnings({ "rawtypes", "unchecked" })
@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanDisbursementDetailsIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private Integer loanId;
    private Integer disbursementId;
    final String approveDate = "01 March 2014";
    final String expectedDisbursementDate = "01 March 2014";
    final String proposedAmount = "5000";
    final String approvalAmount = "5000";

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void createAndValidateMultiDisburseLoansBasedOnEmi() {
        List<HashMap> createTranches = new ArrayList<>();
        String id = null;
        String installmentAmount = "800";
        String withoutInstallmentAmount = "";
        String proposedAmount = "10000";
        createTranches.add(this.loanTransactionHelper.createTrancheDetail(id, "01 June 2015", "5000"));
        createTranches.add(this.loanTransactionHelper.createTrancheDetail(id, "01 September 2015", "5000"));

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2014");
        log.info("---------------------------------CLIENT CREATED WITH ID---------------------------------------------------{}", clientID);

        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder()
                .withInterestTypeAsDecliningBalance().withMoratorium("", "").withAmortizationTypeAsEqualInstallments().withTranches(true)
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).build(null));
        log.info("----------------------------------LOAN PRODUCT CREATED WITH ID------------------------------------------- {}",
                loanProductID);

        final Integer loanIDWithEmi = applyForLoanApplicationWithEmiAmount(clientID, loanProductID, proposedAmount, createTranches,
                installmentAmount);

        log.info("-----------------------------------LOAN CREATED WITH EMI LOANID------------------------------------------------- {}",
                loanIDWithEmi);

        HashMap repaymentScheduleWithEmi = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec,
                loanIDWithEmi, "repaymentSchedule");

        ArrayList<HashMap> periods = (ArrayList<HashMap>) repaymentScheduleWithEmi.get("periods");
        assertEquals(15, periods.size());

        this.validateRepaymentScheduleWithEMI(periods);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanIDWithEmi);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        log.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount("01 June 2015", "01 June 2015", "10000", loanIDWithEmi,
                createTranches);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        log.info(
                "-----------------------------------MULTI DISBURSAL LOAN WITH EMI APPROVED SUCCESSFULLY---------------------------------------");

        final Integer loanIDWithoutEmi = applyForLoanApplicationWithEmiAmount(clientID, loanProductID, proposedAmount, createTranches,
                withoutInstallmentAmount);

        this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanIDWithoutEmi, "repaymentSchedule");

        ArrayList<HashMap> periods1 = (ArrayList<HashMap>) repaymentScheduleWithEmi.get("periods");
        assertEquals(15, periods1.size());

        log.info("-----------------------------------LOAN CREATED WITHOUT EMI LOANID------------------------------------------------- {}",
                loanIDWithoutEmi);

        /* To be uncommented once issue MIFOSX-2006 is closed. */
        // this.validateRepaymentScheduleWithoutEMI(periods1);

        HashMap loanStatusMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanIDWithoutEmi);
        LoanStatusChecker.verifyLoanIsPending(loanStatusMap);

        log.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount("01 June 2015", "01 June 2015", "10000",
                loanIDWithoutEmi, createTranches);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        log.info(
                "-----------------------------------MULTI DISBURSAL LOAN WITHOUT EMI APPROVED SUCCESSFULLY---------------------------------------");

    }

    private void validateRepaymentScheduleWithEMI(ArrayList<HashMap> periods) {
        LoanDisbursementTestBuilder expectedRepaymentSchedule0 = new LoanDisbursementTestBuilder("[2015, 6, 1]", 0.0f, 0.0f, null, null,
                5000.0f, null, null, null);

        LoanDisbursementTestBuilder expectedRepaymentSchedule1 = new LoanDisbursementTestBuilder("[2015, 7, 1]", 800f, 800.0f, 50.0f,
                750.0f, 4250.0f, 750.0f, 750.0f, "[2015, 6, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule2 = new LoanDisbursementTestBuilder("[2015, 8, 1]", 800.0f, 800.0f, 42.5f,
                757.5f, 3492.5f, 757.5f, 757.5f, "[2015, 7, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule3 = new LoanDisbursementTestBuilder("[2015, 9, 1]", 0.0f, 0.0f, null, null,
                5000.0f, null, null, null);

        LoanDisbursementTestBuilder expectedRepaymentSchedule4 = new LoanDisbursementTestBuilder("[2015, 9, 1]", 800.0f, 800.0f, 34.92f,
                765.08f, 7727.42f, 765.08f, 765.08f, "[2015, 8, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule5 = new LoanDisbursementTestBuilder("[2015, 10, 1]", 800.0f, 800.0f, 77.27f,
                722.73f, 7004.69f, 722.73f, 722.73f, "[2015, 9, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule6 = new LoanDisbursementTestBuilder("[2015, 11, 1]", 800.0f, 800.0f, 70.05f,
                729.95f, 6274.74f, 729.95f, 729.95f, "[2015, 10, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule7 = new LoanDisbursementTestBuilder("[2015, 12, 1]", 800.0f, 800.0f, 62.75f,
                737.25f, 5537.49f, 737.25f, 737.25f, "[2015, 11, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule8 = new LoanDisbursementTestBuilder("[2016, 1, 1]", 800.0f, 800.0f, 55.37f,
                744.63f, 4792.86f, 744.63f, 744.63f, "[2015, 12, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule9 = new LoanDisbursementTestBuilder("[2016, 2, 1]", 800.0f, 800.0f, 47.93f,
                752.07f, 4040.79f, 752.07f, 752.07f, "[2016, 1, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule10 = new LoanDisbursementTestBuilder("[2016, 3, 1]", 800.0f, 800.0f, 40.41f,
                759.59f, 3281.2f, 759.59f, 759.59f, "[2016, 2, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule11 = new LoanDisbursementTestBuilder("[2016, 4, 1]", 800.0f, 800.0f, 32.81f,
                767.19f, 2514.01f, 767.19f, 767.19f, "[2016, 3, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule12 = new LoanDisbursementTestBuilder("[2016, 5, 1]", 800.0f, 800.0f, 25.14f,
                774.86f, 1739.15f, 774.86f, 774.86f, "[2016, 4, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule13 = new LoanDisbursementTestBuilder("[2016, 6, 1]", 800.0f, 800.0f, 17.39f,
                782.61f, 956.54f, 782.61f, 782.61f, "[2016, 5, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule14 = new LoanDisbursementTestBuilder("[2016, 7, 1]", 966.11f, 966.11f, 9.57f,
                956.54f, 0.0f, 956.54f, 956.54f, "[2016, 6, 1]");

        ArrayList<LoanDisbursementTestBuilder> list = new ArrayList<LoanDisbursementTestBuilder>();
        list.add(expectedRepaymentSchedule0);
        list.add(expectedRepaymentSchedule1);
        list.add(expectedRepaymentSchedule2);
        list.add(expectedRepaymentSchedule3);
        list.add(expectedRepaymentSchedule4);
        list.add(expectedRepaymentSchedule5);
        list.add(expectedRepaymentSchedule6);
        list.add(expectedRepaymentSchedule7);
        list.add(expectedRepaymentSchedule8);
        list.add(expectedRepaymentSchedule9);
        list.add(expectedRepaymentSchedule10);
        list.add(expectedRepaymentSchedule11);
        list.add(expectedRepaymentSchedule12);
        list.add(expectedRepaymentSchedule13);
        list.add(expectedRepaymentSchedule14);

        for (int i = 0; i < list.size(); i++) {
            log.info("values {} {} {}", i, periods.get(i), list.get(i));
            this.assertRepaymentScheduleValuesWithEMI(periods.get(i), list.get(i), i);
        }
    }

    private void assertRepaymentScheduleValuesWithEMI(HashMap period, LoanDisbursementTestBuilder expectedRepaymentSchedule, int position) {

        assertEquals(period.get("dueDate").toString(), expectedRepaymentSchedule.getDueDate());
        assertEquals(period.get("principalLoanBalanceOutstanding"), expectedRepaymentSchedule.getPrincipalLoanBalanceOutstanding());
        log.info("{}", period.get("totalOriginalDueForPeriod").toString());
        assertEquals(Float.parseFloat(period.get("totalOriginalDueForPeriod").toString()),
                expectedRepaymentSchedule.getTotalOriginalDueForPeriod().floatValue(), 0.0f);

        assertEquals(Float.parseFloat(period.get("totalOutstandingForPeriod").toString()),
                expectedRepaymentSchedule.getTotalOutstandingForPeriod(), 0.0f);

        if (position != 0 && position != 3) {

            assertEquals(Float.parseFloat(period.get("interestOutstanding").toString()), expectedRepaymentSchedule.getInterestOutstanding(),
                    0.0f);
            assertEquals(Float.parseFloat(period.get("principalOutstanding").toString()),
                    expectedRepaymentSchedule.getPrincipalOutstanding(), 0.0f);
            assertEquals(Float.parseFloat(period.get("principalDue").toString()), expectedRepaymentSchedule.getPrincipalDue(), 0.0f);
            assertEquals(Float.parseFloat(period.get("principalOriginalDue").toString()),
                    expectedRepaymentSchedule.getPrincipalOriginalDue(), 0.0f);
            assertEquals(period.get("fromDate").toString(), expectedRepaymentSchedule.getFromDate());
        }
    }

    private Integer applyForLoanApplicationWithEmiAmount(final Integer clientId, final Integer loanProductId, final String proposedAmount,
            List<HashMap> tranches, final String installmentAmount) {

        log.info("----------------APPLYING FOR LOAN APPLICATION");
        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                clientId.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(proposedAmount) //
                .withLoanTermFrequency("12") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("12") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("1") //
                .withExpectedDisbursementDate("01 June 2015") //
                .withTranches(tranches) //
                .withFixedEmiAmount(installmentAmount) //
                .withInterestTypeAsDecliningBalance() //
                .withSubmittedOnDate("01 June 2015") //
                .withAmortizationTypeAsEqualInstallments() //
                .withCollaterals(collaterals).build(clientId.toString(), loanProductId.toString(), null);

        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);

    }

    @Test
    public void validateEqualInstallmentsForMultiTrancheLoan() {
        final String operationDate = "01 January 2014";
        final String principal = "1000";
        final String disbursedPrincipal = "900";

        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, operationDate);
        log.info("-----------------CLIENT CREATED WITH ID------------------- {}", clientId);

        final String loanProductJSON = new LoanProductTestBuilder().withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance().withMoratorium("", "").withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .withInterestTypeAsDecliningBalance() //
                .withMultiDisburse() //
                .withDisallowExpectedDisbursements(true) //
                .build(null);
        log.info("Product {}", loanProductJSON);
        final Integer loanProductId = this.loanTransactionHelper.getLoanProductId(loanProductJSON);
        log.info("------------------LOAN PRODUCT CREATED WITH ID----------- {}", loanProductId);

        final Integer loanId = applyForMultiTrancheLoanApplication(clientId.toString(), loanProductId.toString(), principal, operationDate);

        log.info("-------------------LOAN CREATED WITH loanId----------------- {}", loanId);

        this.loanTransactionHelper.approveLoanWithApproveAmount(operationDate, expectedDisbursementDate, principal, loanId, null);
        log.info("-------------------MULTI DISBURSAL LOAN APPROVED SUCCESSFULLY-------");

        GetLoansLoanIdResponse getLoansLoanIdResponse = this.loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        this.loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        loanTransactionHelper.disburseLoanWithTransactionAmount(operationDate, loanId, disbursedPrincipal);
        log.info("-------------------MULTI DISBURSAL LOAN DISBURSED SUCCESSFULLY-------");
        getLoansLoanIdResponse = this.loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        this.loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);
        final Double limit = 2.0;
        evaluateEqualInstallmentsForRepaymentSchedule(getLoansLoanIdResponse.getRepaymentSchedule(), limit);
        log.info("-----------MULTI DISBURSAL LOAN EQUAL INSTALLMENTS SUCCESSFULLY-------");
    }

    @Test
    public void disburseLoanWithExceededOverAppliedAmountFails() {
        final String operationDate = "01 January 2014";
        final String principal = "1000";
        final String firstDisbursedPrincipal = "900";
        final String secondDisbursedPrincipal = "1101";

        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, operationDate);
        log.info("-----------------CLIENT CREATED WITH ID------------------- {}", clientId);

        final String loanProductJSON = new LoanProductTestBuilder().withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance().withMoratorium("", "").withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .withInterestTypeAsDecliningBalance() //
                .withMultiDisburse() //
                .withDisallowExpectedDisbursements(true) //
                .build(null);
        log.info("Product {}", loanProductJSON);
        final Integer loanProductId = this.loanTransactionHelper.getLoanProductId(loanProductJSON);
        log.info("------------------LOAN PRODUCT CREATED WITH ID----------- {}", loanProductId);

        final Integer loanId = applyForMultiTrancheLoanApplication(clientId.toString(), loanProductId.toString(), principal, operationDate);

        log.info("-------------------LOAN CREATED WITH loanId----------------- {}", loanId);

        this.loanTransactionHelper.approveLoanWithApproveAmount(operationDate, expectedDisbursementDate, principal, loanId, null);
        log.info("-------------------MULTI DISBURSAL LOAN APPROVED SUCCESSFULLY-------");

        GetLoansLoanIdResponse getLoansLoanIdResponse = this.loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        this.loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        loanTransactionHelper.disburseLoanWithTransactionAmount(operationDate, loanId, firstDisbursedPrincipal);
        log.info("-------------------MULTI DISBURSAL LOAN DISBURSED SUCCESSFULLY-------");

        loanTransactionHelper.disburseLoanWithTransactionAmount(operationDate, loanId, secondDisbursedPrincipal,
                overAppliedAmountFailedResponseSpec());
        log.info("-------------------MULTI DISBURSAL LOAN DISBURSEMENT FAILED-------");
    }

    @Test
    public void disburseLoanWithExceededOverAppliedAmountSucceed() {
        final String operationDate = "01 January 2014";
        final String principal = "1000";
        final String firstDisbursedPrincipal = "900";
        final String secondDisbursedPrincipal = "1100";

        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, operationDate);
        log.info("-----------------CLIENT CREATED WITH ID------------------- {}", clientId);

        final String loanProductJSON = new LoanProductTestBuilder().withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance().withMoratorium("", "").withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .withInterestTypeAsDecliningBalance() //
                .withMultiDisburse() //
                .withDisallowExpectedDisbursements(true) //
                .build(null);
        log.info("Product {}", loanProductJSON);
        final Integer loanProductId = this.loanTransactionHelper.getLoanProductId(loanProductJSON);
        log.info("------------------LOAN PRODUCT CREATED WITH ID----------- {}", loanProductId);

        final Integer loanId = applyForMultiTrancheLoanApplication(clientId.toString(), loanProductId.toString(), principal, operationDate);

        log.info("-------------------LOAN CREATED WITH loanId----------------- {}", loanId);

        this.loanTransactionHelper.approveLoanWithApproveAmount(operationDate, expectedDisbursementDate, principal, loanId, null);
        log.info("-------------------MULTI DISBURSAL LOAN APPROVED SUCCESSFULLY-------");

        GetLoansLoanIdResponse getLoansLoanIdResponse = this.loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        this.loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        loanTransactionHelper.disburseLoanWithTransactionAmount(operationDate, loanId, firstDisbursedPrincipal);
        log.info("-------------------MULTI DISBURSAL LOAN DISBURSED SUCCESSFULLY-FIRST-------");

        loanTransactionHelper.disburseLoanWithTransactionAmount(operationDate, loanId, secondDisbursedPrincipal);
        log.info("-------------------MULTI DISBURSAL LOAN DISBURSED SUCCESSFULLY-SECOND-------");

        double disbursementPrincipalSum = this.loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId).getDisbursementDetails()
                .stream().map(GetLoansLoanIdDisbursementDetails::getPrincipal).mapToDouble(p -> p).sum();
        assertEquals(parseDouble(firstDisbursedPrincipal) + parseDouble(secondDisbursedPrincipal), disbursementPrincipalSum);
    }

    @Test
    public void createApproveAndValidateMultiDisburseLoan() throws ParseException {

        List<HashMap> createTranches = new ArrayList<>();
        String id = null;
        createTranches.add(this.loanTransactionHelper.createTrancheDetail(id, "01 March 2014", "1000"));

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2014");
        log.info("---------------------------------CLIENT CREATED WITH ID--------------------------------------------------- {}", clientID);

        final Integer loanProductID = this.loanTransactionHelper
                .getLoanProductId(new LoanProductTestBuilder().withInterestTypeAsDecliningBalance().withTranches(true)
                        .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).build(null));
        log.info("----------------------------------LOAN PRODUCT CREATED WITH ID------------------------------------------- {}",
                loanProductID);

        this.loanId = applyForLoanApplicationWithTranches(clientID, loanProductID, proposedAmount, createTranches);
        log.info("-----------------------------------LOAN CREATED WITH LOANID------------------------------------------------- {}",
                this.loanId);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, this.loanId);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        log.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount(approveDate, expectedDisbursementDate, approvalAmount,
                this.loanId, createTranches);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        log.info("-----------------------------------MULTI DISBURSAL LOAN APPROVED SUCCESSFULLY---------------------------------------");
        ArrayList<HashMap> disbursementDetails = (ArrayList<HashMap>) this.loanTransactionHelper.getLoanDetail(this.requestSpec,
                this.responseSpec, this.loanId, "disbursementDetails");
        this.disbursementId = (Integer) disbursementDetails.get(0).get("id");
        this.editLoanDisbursementDetails();
    }

    @Test
    public void allowModifyLoanApplicationAfterUndoDisbursalWithTranches() throws ParseException {
        final String operationDate = this.approveDate;
        List<HashMap> tranches = new ArrayList<>();
        String principal = "1000";
        final List<HashMap> collaterals = new ArrayList<>();

        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, operationDate);
        log.info("---------------------------------CLIENT CREATED WITH ID--------------------------------------------------- {}", clientId);

        final Integer loanProductId = this.loanTransactionHelper
                .getLoanProductId(new LoanProductTestBuilder().withInterestTypeAsDecliningBalance().withTranches(true)
                        .withDisallowExpectedDisbursements(true).withInterestCalculationPeriodTypeAsRepaymentPeriod(true).build(null));
        log.info("----------------------------------LOAN PRODUCT CREATED WITH ID------------------------------------------- {}",
                loanProductId);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = this.loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductIdResponse);
        log.info("Loan Product Id {} with DisallowExpectectedDisbursements in {}", loanProductId,
                getLoanProductsProductIdResponse.getDisallowExpectedDisbursements());
        assertEquals(true, getLoanProductsProductIdResponse.getDisallowExpectedDisbursements());

        final Integer loanId = applyForLoanApplicationWithTranches(clientId, loanProductId, proposedAmount, tranches);
        log.info("-----------------------------------LOAN CREATED WITH LOANID------------------------------------------------- {}", loanId);

        loanTransactionHelper.approveLoanWithApproveAmount(operationDate, operationDate, approvalAmount, loanId, tranches);
        GetLoansLoanIdResponse getLoansLoanIdResponse = this.loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        log.info("Loan Id {} with Status {} with Disbursement details {}", getLoansLoanIdResponse.getId(),
                getLoansLoanIdResponse.getStatus().getCode(), getLoansLoanIdResponse.getDisbursementDetails().size());
        log.info("-------------------MULTI DISBURSAL LOAN APPROVED SUCCESSFULLY-------");
        assertEquals(0, getLoansLoanIdResponse.getDisbursementDetails().size(), "Disbursement details items");

        loanTransactionHelper.disburseLoanWithTransactionAmount(operationDate, loanId, principal);

        getLoansLoanIdResponse = this.loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        log.info("Loan Id {} with Status {} with Disbursement details {}", getLoansLoanIdResponse.getId(),
                getLoansLoanIdResponse.getStatus().getCode(), getLoansLoanIdResponse.getDisbursementDetails().size());
        log.info("-------------------MULTI DISBURSAL LOAN DISBURSED SUCCESSFULLY-------");
        assertEquals(1, getLoansLoanIdResponse.getDisbursementDetails().size(), "Disbursement details items");

        PostLoansLoanIdResponse postLoansLoanIdResponse = this.loanTransactionHelper.applyLoanCommand(loanId, "undoDisbursal");
        assertNotNull(postLoansLoanIdResponse);
        log.info("-------------------UNDO DISBURSAL LOAN SUCCESSFULLY-------");
        getLoansLoanIdResponse = this.loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        log.info("Loan Id {} with Status {} with Disbursement details {}", getLoansLoanIdResponse.getId(),
                getLoansLoanIdResponse.getStatus().getCode(), getLoansLoanIdResponse.getDisbursementDetails().size());
        assertEquals(0, getLoansLoanIdResponse.getDisbursementDetails().size(), "Disbursement details items");

        getLoansLoanIdResponse = this.loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        log.info("Loan Id {} with Status {} with Disbursement details {}", getLoansLoanIdResponse.getId(),
                getLoansLoanIdResponse.getStatus().getCode(), getLoansLoanIdResponse.getDisbursementDetails().size());
        assertEquals(0, getLoansLoanIdResponse.getDisbursementDetails().size(), "Disbursement details items");

        postLoansLoanIdResponse = this.loanTransactionHelper.applyLoanCommand(loanId, "undoApproval");
        assertNotNull(postLoansLoanIdResponse);
        log.info("-------------------UNDO APPROVAL LOAN SUCCESSFULLY-------");

        getLoansLoanIdResponse = this.loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        log.info("Loan Id {} with Status {} with Disbursement details {}", getLoansLoanIdResponse.getId(),
                getLoansLoanIdResponse.getStatus().getCode(), getLoansLoanIdResponse.getDisbursementDetails().size());

        principal = "10000";
        final String loanApplicationJSON = buildLoanApplicationJSON(clientId, loanProductId, principal, tranches, operationDate,
                collaterals);
        log.info("Modify Loan Application: {}", loanApplicationJSON);
        PutLoansLoanIdResponse putLoansLoanIdResponse = this.loanTransactionHelper.modifyLoanApplication(loanId, loanApplicationJSON);
        assertNotNull(putLoansLoanIdResponse);

        getLoansLoanIdResponse = this.loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        log.info("Loan Id {} with Status {} with Disbursement details {} and Principal {}", getLoansLoanIdResponse.getId(),
                getLoansLoanIdResponse.getStatus().getCode(), getLoansLoanIdResponse.getDisbursementDetails().size(),
                getLoansLoanIdResponse.getPrincipal());

        // ReDo the Approval and Disbursement
        loanTransactionHelper.approveLoanWithApproveAmount(operationDate, operationDate, approvalAmount, loanId, null);
        getLoansLoanIdResponse = this.loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        log.info("Loan Id {} with Status {} with Disbursement details {}", getLoansLoanIdResponse.getId(),
                getLoansLoanIdResponse.getStatus().getCode(), getLoansLoanIdResponse.getDisbursementDetails().size());

        loanTransactionHelper.disburseLoanWithTransactionAmount(operationDate, loanId, principal);

        getLoansLoanIdResponse = this.loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        log.info("Loan Id {} with Status {} with Disbursement details {}", getLoansLoanIdResponse.getId(),
                getLoansLoanIdResponse.getStatus().getCode(), getLoansLoanIdResponse.getDisbursementDetails().size());
        log.info("-------------------MULTI DISBURSAL LOAN DISBURSED SUCCESSFULLY-------");
        assertEquals(1, getLoansLoanIdResponse.getDisbursementDetails().size(), "Disbursement details items");
    }

    private void editLoanDisbursementDetails() throws ParseException {
        this.editDateAndPrincipalOfExistingTranche();
        this.addNewDisbursementDetails();
        this.deleteDisbursmentDetails();
    }

    private void addNewDisbursementDetails() throws ParseException {
        List<HashMap> addTranches = new ArrayList<>();
        ArrayList<HashMap> disbursementDetails = (ArrayList<HashMap>) this.loanTransactionHelper.getLoanDetail(this.requestSpec,
                this.responseSpec, this.loanId, "disbursementDetails");
        ArrayList expectedDisbursementDate = (ArrayList) disbursementDetails.get(0).get("expectedDisbursementDate");
        String date = formatExpectedDisbursementDate(expectedDisbursementDate.toString());

        String id = null;
        addTranches.add(this.loanTransactionHelper.createTrancheDetail(disbursementDetails.get(0).get("id").toString(), date,
                disbursementDetails.get(0).get("principal").toString()));
        addTranches.add(this.loanTransactionHelper.createTrancheDetail(id, "03 March 2014", "2000"));
        addTranches.add(this.loanTransactionHelper.createTrancheDetail(id, "04 March 2014", "500"));

        /* Add disbursement detail */
        this.loanTransactionHelper.addAndDeleteDisbursementDetail(this.loanId, this.approvalAmount, this.expectedDisbursementDate,
                addTranches, "");
    }

    private void deleteDisbursmentDetails() throws ParseException {
        List<HashMap> deleteTranches = new ArrayList<>();
        ArrayList<HashMap> disbursementDetails = (ArrayList<HashMap>) this.loanTransactionHelper.getLoanDetail(this.requestSpec,
                this.responseSpec, this.loanId, "disbursementDetails");
        /* Delete the last tranche */
        for (int i = 0; i < disbursementDetails.size() - 1; i++) {
            ArrayList expectedDisbursementDate = (ArrayList) disbursementDetails.get(i).get("expectedDisbursementDate");
            String disbursementDate = formatExpectedDisbursementDate(expectedDisbursementDate.toString());
            deleteTranches.add(this.loanTransactionHelper.createTrancheDetail(disbursementDetails.get(i).get("id").toString(),
                    disbursementDate, disbursementDetails.get(i).get("principal").toString()));
        }

        /* Add disbursement detail */
        this.loanTransactionHelper.addAndDeleteDisbursementDetail(this.loanId, this.approvalAmount, this.expectedDisbursementDate,
                deleteTranches, "");
    }

    private void editDateAndPrincipalOfExistingTranche() throws ParseException {
        String updatedExpectedDisbursementDate = "01 March 2014";
        String updatedPrincipal = "900";
        /* Update */
        this.loanTransactionHelper.editDisbursementDetail(this.loanId, this.disbursementId, this.approvalAmount,
                this.expectedDisbursementDate, updatedExpectedDisbursementDate, updatedPrincipal, "");
        /* Validate Edit */
        ArrayList<HashMap> disbursementDetails = (ArrayList<HashMap>) this.loanTransactionHelper.getLoanDetail(this.requestSpec,
                this.responseSpec, this.loanId, "disbursementDetails");
        assertEquals(Float.parseFloat(updatedPrincipal), disbursementDetails.get(0).get("principal"));
        ArrayList expectedDisbursementDate = (ArrayList) disbursementDetails.get(0).get("expectedDisbursementDate");
        String date = formatExpectedDisbursementDate(expectedDisbursementDate.toString());
        assertEquals(updatedExpectedDisbursementDate, date);

    }

    private String formatExpectedDisbursementDate(String expectedDisbursementDate) throws ParseException {
        SimpleDateFormat source = new SimpleDateFormat("[yyyy, MM, dd]");
        SimpleDateFormat target = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        String date = target.format(source.parse(expectedDisbursementDate));

        return date;
    }

    private String buildLoanApplicationJSON(final Integer clientId, final Integer loanProductId, String principal, List<HashMap> tranches,
            final String operationDate, List<HashMap> collaterals) {

        return new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("5") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("5") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withExpectedDisbursementDate("01 March 2014") //
                .withTranches(tranches) //
                .withInterestTypeAsDecliningBalance() //
                .withSubmittedOnDate("01 March 2014") //
                .withCollaterals(collaterals).build(clientId.toString(), loanProductId.toString(), null);
    }

    private Integer applyForLoanApplicationWithTranches(final Integer clientId, final Integer loanProductId, String principal,
            List<HashMap> tranches) {
        log.info("----------------APPLYING FOR LOAN APPLICATION");
        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                clientId.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));
        final String loanApplicationJSON = buildLoanApplicationJSON(clientId, loanProductId, principal, tranches, "01 March 2014",
                collaterals);

        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private Integer applyForMultiTrancheLoanApplication(final String clientId, final String loanProductId, String principal,
            String operationDate) {
        log.info("----------------APPLYING FOR MULTI TRANCHE LOAN APPLICATION");
        List<HashMap> emptyData = new ArrayList<>();
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("3") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("3") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("0") //
                .withExpectedDisbursementDate(operationDate) //
                .withTranches(emptyData) //
                .withInterestTypeAsDecliningBalance() //
                .withSubmittedOnDate(operationDate) //
                .withCollaterals(emptyData) //
                .build(clientId, loanProductId, null);

        log.info("Loan account {}", loanApplicationJSON);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private void addCollaterals(List<HashMap> collaterals, Integer collateralId, BigDecimal quantity) {
        collaterals.add(collaterals(collateralId, quantity));
    }

    private HashMap<String, String> collaterals(Integer collateralId, BigDecimal quantity) {
        HashMap<String, String> collateral = new HashMap<String, String>(2);
        collateral.put("clientCollateralId", collateralId.toString());
        collateral.put("quantity", quantity.toString());
        return collateral;
    }

    public void evaluateEqualInstallmentsForRepaymentSchedule(GetLoansLoanIdRepaymentSchedule getLoanRepaymentSchedule, Double limit) {
        Double totalOutstandingForPeriod = 0.0;
        Double totalInstallmentAmountForPeriod = 0.0;
        if (getLoanRepaymentSchedule != null) {
            log.info("Loan with {} periods", getLoanRepaymentSchedule.getPeriods().size());
            for (GetLoansLoanIdRepaymentPeriod period : getLoanRepaymentSchedule.getPeriods()) {
                if (period.getPeriod() != null) {
                    log.info("Period number {} for due date {} and outstanding {} {}", period.getPeriod(), period.getDueDate(),
                            period.getTotalOutstandingForPeriod(), period.getTotalInstallmentAmountForPeriod());
                    if (period.getPeriod() == 1) {
                        totalOutstandingForPeriod = period.getTotalOutstandingForPeriod();
                        totalInstallmentAmountForPeriod = period.getTotalInstallmentAmountForPeriod();
                    } else {
                        assertTrue(Math.abs(period.getTotalOutstandingForPeriod() - totalOutstandingForPeriod) <= limit);
                        assertTrue(Math.abs(period.getTotalInstallmentAmountForPeriod() - totalInstallmentAmountForPeriod) <= limit);
                    }
                }
            }
        }
    }

    private ResponseSpecification overAppliedAmountFailedResponseSpec() {
        return new ResponseSpecBuilder().expectBody("userMessageGlobalisationCode", equalTo("validation.msg.domain.rule.violation"))
                .expectBody("errors[0].userMessageGlobalisationCode",
                        equalTo("error.msg.loan.disbursal.amount.can't.be.greater.than.maximum.applied.loan.amount.calculation"))
                .expectStatusCode(403).build();
    }
}
