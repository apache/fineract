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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsResponse;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.CalendarHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.GroupHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unchecked" })

@ExtendWith(LoanTestLifecycleExtension.class)
public class SkipRepaymentOnMonthFirstTest {

    private static final Logger LOG = LoggerFactory.getLogger(SkipRepaymentOnMonthFirstTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private GlobalConfigurationHelper globalConfigurationHelper;
    private LoanTransactionHelper loanTransactionHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.globalConfigurationHelper = new GlobalConfigurationHelper();
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }

    public void testSkippingRepaymentOnFirstDayOfMonth() {
        boolean newBooleanValue = true;
        PutGlobalConfigurationsResponse response = globalConfigurationHelper.updateGlobalConfiguration(
                GlobalConfigurationConstants.SKIP_REPAYMENT_ON_FIRST_DAY_OF_MONTH,
                new PutGlobalConfigurationsRequest().enabled(newBooleanValue));
        Assertions.assertNotNull(response);
    }

    @Test
    public void checkRepaymentSkipOnFirstDayOfMonth() {
        testSkippingRepaymentOnFirstDayOfMonth();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        final String startDate = "15 September 2011";
        final String frequency = "3"; // Monthly
        final String interval = "1"; // Every One Moth
        Integer calendarID = CalendarHelper.createMeetingForGroup(requestSpec, responseSpec, groupID, startDate, frequency, interval, null);
        LOG.info("caladerId -------------------- {}", calendarID);
        final Integer loanProductID = createLoanProduct();
        final Integer loanID = applyForLoanApplication(groupID, loanProductID, calendarID, clientID);
        LOG.info("loanID---- {}", loanID);
        final ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec,
                loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

    }

    private Integer createLoanProduct() {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("12,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer groupID, final Integer loanProductID, Integer calendarID, Integer clientID) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("12,000.00") //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("01 October 2011") //
                .withCalendarID(calendarID.toString()) //
                .withSubmittedOnDate("01 October 2011") //
                .withCollaterals(collaterals).withLoanType("jlg")
                .build(clientID.toString(), groupID.toString(), loanProductID.toString(), null);
        LOG.info(loanApplicationJSON);
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

    private void verifyLoanRepaymentSchedule(final ArrayList<HashMap> loanSchedule) {
        LOG.info("--------------------VERIFYING THE REPAYMENT DATE--------------------------");
        assertEquals(new ArrayList<>(Arrays.asList(2011, 10, 15)), loanSchedule.get(1).get("dueDate"),
                "Checking for Repayment Date for 1st Month");
        LOG.info("Repayment Date for 1st Month-- {}", loanSchedule.get(1).get("dueDate"));

        assertEquals(new ArrayList<>(Arrays.asList(2011, 11, 15)), loanSchedule.get(2).get("dueDate"),
                "Checking for Repayment Date for 2nd Month");
        LOG.info("Repayment Date for 2nd Month-- {}", loanSchedule.get(2).get("dueDate"));

        assertEquals(new ArrayList<>(Arrays.asList(2011, 12, 15)), loanSchedule.get(3).get("dueDate"),
                "Checking for  Repayment Date for 3rd Month");
        LOG.info("Repayment Date for 3rd Month-- {}", loanSchedule.get(3).get("dueDate"));

        assertEquals(new ArrayList<>(Arrays.asList(2012, 1, 15)), loanSchedule.get(4).get("dueDate"),
                "Checking for  Repayment Date for 4th Month");
        LOG.info("Repayment Date for 4th Month-- {}", loanSchedule.get(4).get("dueDate"));
    }

}
