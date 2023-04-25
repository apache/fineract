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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentSchedule;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostChargesRequest;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
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
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("rawtypes")
@Slf4j
public class LoanDisbursementChargeTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private ChargesHelper chargesHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.chargesHelper = new ChargesHelper();
    }

    @Test
    public void testLoanRepaymentRescheduleAtDisbursementWithDisbursementCharge() {
        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);

        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 2));
        GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec, "10", "0");
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientID);

        Double chargeAmount = 10.0;
        PostChargesResponse charge = chargesHelper.createCharges(getPostChargeRequest(chargeAmount));

        final Integer loanProductID = createLoanProduct(charge.getResourceId().toString());
        Assertions.assertNotNull(loanProductID);
        HashMap loanStatusHashMap;

        final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null, "10 January 2020");

        Assertions.assertNotNull(loanID);

        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = loanTransactionHelper.approveLoan("01 March 2020", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);

        PostLoansLoanIdChargesRequest chargeRequest = getPostLoanLoanIdChargeRequest(charge.getResourceId());
        PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = loanTransactionHelper.addLoanCharge(loanID.longValue(),
                chargeRequest);
        Assertions.assertNotNull(postLoansLoanIdChargesResponse.getResourceId());

        loanStatusHashMap = loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        GetLoansLoanIdResponse loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        GetLoansLoanIdRepaymentSchedule repaymentSchedule = loan.getRepaymentSchedule();
        Assertions.assertTrue(repaymentSchedule.getPeriods().stream().anyMatch(period -> period.getFeeChargesDue().equals(chargeAmount)
                && period.getFeeChargesPaid().equals(chargeAmount) && period.getPeriod() != null && period.getPeriod().equals(0)));
    }

    private PostLoansLoanIdChargesRequest getPostLoanLoanIdChargeRequest(Long chargeId) {
        PostLoansLoanIdChargesRequest request = new PostLoansLoanIdChargesRequest();
        request.setAmount(10.0);
        request.setChargeId(chargeId);
        request.setDateFormat("dd MMMM yyyy");
        request.setLocale("en");
        request.setDueDate("02 March 2020");
        return request;
    }

    private Integer createLoanProduct(final String chargeId) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .build(chargeId);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
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

    private PostChargesRequest getPostChargeRequest(Double chargeAmount) {
        PostChargesRequest request = new PostChargesRequest();
        request.setName(Utils.randomStringGenerator("D_CH_", 5));
        request.setActive(true);
        request.setAmount(chargeAmount);
        request.setPenalty(false);
        request.setCurrencyCode("USD");
        request.setLocale("en");
        request.setMonthDayFormat("dd MMM");
        request.setChargeAppliesTo(ChargeAppliesTo.LOAN.getValue());
        request.setChargeCalculationType(ChargeCalculationType.FLAT.getValue());
        request.setChargeTimeType(ChargeTimeType.DISBURSEMENT.getValue());
        request.setChargePaymentMode(ChargePaymentMode.REGULAR.getValue());
        return request;
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
}
