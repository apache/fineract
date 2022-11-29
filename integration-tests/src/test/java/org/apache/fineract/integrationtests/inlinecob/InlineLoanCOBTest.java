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
package org.apache.fineract.integrationtests.inlinecob;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetDelinquencyRangesResponse;
import org.apache.fineract.client.models.GetDelinquencyTagHistoryResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyRangesHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class InlineLoanCOBTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private InlineLoanCOBHelper inlineLoanCOBHelper;
    private LoanTransactionHelper loanTransactionHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
    }

    @Test
    public void testInlineCOB() {
        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 2));
        GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec, "10", "0");
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientID);

        Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("1"));
        Assertions.assertNotNull(overdueFeeChargeId);

        final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
        Assertions.assertNotNull(loanProductID);
        HashMap loanStatusHashMap;

        final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null, "10 January 2020");

        Assertions.assertNotNull(loanID);

        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = loanTransactionHelper.approveLoan("01 March 2020", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
        loanStatusHashMap = loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.COB_DATE, LocalDate.of(2020, 3, 2));
        inlineLoanCOBHelper.executeInlineCOB(List.of(loanID.longValue()));
        GetLoansLoanIdResponse loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        Assertions.assertEquals(LocalDate.of(2020, 3, 2), loan.getLastClosedBusinessDate());

        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.COB_DATE, LocalDate.of(2020, 3, 3));
        inlineLoanCOBHelper.executeInlineCOB(List.of(loanID.longValue()));

        loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        Assertions.assertEquals(LocalDate.of(2020, 3, 3), loan.getLastClosedBusinessDate());

        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.COB_DATE, LocalDate.of(2020, 3, 10));
        inlineLoanCOBHelper.executeInlineCOB(List.of(loanID.longValue()));

        loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        Assertions.assertEquals(LocalDate.of(2020, 3, 10), loan.getLastClosedBusinessDate());

        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
    }

    @Test
    public void testInlineCOBCatchUpLoans() {
        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientID);

        Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("1"));
        Assertions.assertNotNull(overdueFeeChargeId);

        ArrayList<Integer> rangeIds = new ArrayList<>();
        // First Range
        String jsonRange = DelinquencyRangesHelper.getAsJSON(1, 3);
        PostDelinquencyRangeResponse delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec,
                jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        jsonRange = DelinquencyRangesHelper.getAsJSON(4, 60);

        GetDelinquencyRangesResponse range = DelinquencyRangesHelper.getDelinquencyRange(requestSpec, responseSpec,
                delinquencyRangeResponse.getResourceId());

        // Second Range
        delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec, jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());

        range = DelinquencyRangesHelper.getDelinquencyRange(requestSpec, responseSpec, delinquencyRangeResponse.getResourceId());
        final String classificationExpected = range.getClassification();
        log.info("Expected Delinquency Range classification after Disbursement {}", classificationExpected);

        String jsonBucket = DelinquencyBucketsHelper.getAsJSON(rangeIds);
        PostDelinquencyBucketResponse delinquencyBucketResponse = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec,
                responseSpec, jsonBucket);
        assertNotNull(delinquencyBucketResponse);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketResponse.getResourceId());

        final Integer loanProductID = createLoanProduct(loanTransactionHelper, delinquencyBucket.getId());

        Assertions.assertNotNull(loanProductID);
        HashMap loanStatusHashMap;

        final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null, "10 January 2020");

        Assertions.assertNotNull(loanID);

        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = loanTransactionHelper.approveLoan("01 March 2020", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
        loanStatusHashMap = loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.COB_DATE, LocalDate.of(2020, 3, 2));
        inlineLoanCOBHelper.executeInlineCOB(List.of(loanID.longValue()));
        GetLoansLoanIdResponse loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        ArrayList<GetDelinquencyTagHistoryResponse> loanDelinquencyTags = loanTransactionHelper.getLoanDelinquencyTags(requestSpec,
                responseSpec, loanID);
        Assertions.assertTrue(loanDelinquencyTags.isEmpty());
        Assertions.assertEquals(LocalDate.of(2020, 3, 2), loan.getLastClosedBusinessDate());

        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.COB_DATE, LocalDate.of(2020, 4, 5));
        inlineLoanCOBHelper.executeInlineCOB(List.of(loanID.longValue()));

        loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        loanDelinquencyTags = loanTransactionHelper.getLoanDelinquencyTags(requestSpec, responseSpec, loanID);
        Assertions.assertEquals(LocalDate.of(2020, 4, 5), loan.getLastClosedBusinessDate());
        Assertions.assertEquals(1, loanDelinquencyTags.size());
        Assertions.assertEquals(LocalDate.of(2020, 4, 3), loanDelinquencyTags.get(0).getAddedOnDate());

        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.COB_DATE, LocalDate.of(2020, 4, 10));
        inlineLoanCOBHelper.executeInlineCOB(List.of(loanID.longValue()));

        loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        loanDelinquencyTags = loanTransactionHelper.getLoanDelinquencyTags(requestSpec, responseSpec, loanID);
        Assertions.assertEquals(LocalDate.of(2020, 4, 10), loan.getLastClosedBusinessDate());
        Assertions.assertEquals(2, loanDelinquencyTags.size());
        Assertions.assertEquals(LocalDate.of(2020, 4, 3), loanDelinquencyTags.get(1).getAddedOnDate());
        Assertions.assertEquals(LocalDate.of(2020, 4, 6), loanDelinquencyTags.get(0).getAddedOnDate());

        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
    }

    private Integer createLoanProduct(final String chargeId) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .build(chargeId);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer createLoanProduct(final LoanTransactionHelper loanTransactionHelper, final Integer delinquencyBucketId) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, delinquencyBucketId);
        return loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
    }

    private Integer applyForLoanApplication(final String clientID, final String loanProductID, final String savingsID, final String date) {

        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec, clientID,
                collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("15,000.00").withLoanTermFrequency("4")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("4").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withAmortizationTypeAsEqualInstallments()
                .withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(date).withSubmittedOnDate(date).withCollaterals(collaterals)
                .build(clientID, loanProductID, savingsID);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private void addCollaterals(List<HashMap> collaterals, Integer collateralId, BigDecimal quantity) {
        collaterals.add(collaterals(collateralId, quantity));
    }

    private HashMap<String, String> collaterals(Integer collateralId, BigDecimal quantity) {
        HashMap<String, String> collateral = new HashMap<>(2);
        collateral.put("clientCollateralId", collateralId.toString());
        collateral.put("quantity", quantity.toString());
        return collateral;
    }
}
