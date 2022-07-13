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
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.TimezoneChangeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiTimezoneEnvironmentIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(MultiTimezoneEnvironmentIntegrationTest.class);

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;

    private ClientHelper clientHelper;

    @BeforeEach
    public void initialize() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testDates() {
        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
        try {
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, "12 June 2021", "BUSINESS_DATE");
            List<String> timeZones = getTimeZones();
            for (String tz : timeZones) {
                TimezoneChangeHelper.updateTimeZone(requestSpec, responseSpec, tz);
                Integer clientId = initClient();
                GetClientsClientIdResponse clientDetails = ClientHelper.getClient(requestSpec, responseSpec, clientId);
                assertEquals(clientDetails.getActivationDate(), LocalDate.of(2021, 6, 10), tz);
                assertEquals(clientDetails.getTimeline().getSubmittedOnDate(), LocalDate.of(2021, 6, 10), tz);
                Integer loanId = initLoan(clientId);
                GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetailsAsObject(requestSpec, responseSpec, loanId);
                assertEquals(loanDetails.getTimeline().getSubmittedOnDate(), LocalDate.of(2021, 6, 10), tz);
                assertEquals(loanDetails.getTimeline().getApprovedOnDate(), LocalDate.of(2021, 6, 10), tz);
                assertEquals(loanDetails.getTimeline().getActualDisbursementDate(), LocalDate.of(2021, 6, 10), tz);
                assertEquals(loanDetails.getTimeline().getExpectedMaturityDate(), LocalDate.of(2021, 11, 10), tz);

                HashMap loanTransaction = loanTransactionHelper.makeRepayment("11 June 2021", 1.0f, loanId);
                HashMap loanTransactionDetails = loanTransactionHelper.getLoanTransactionDetails(loanId,
                        (Integer) loanTransaction.get("resourceId"));
                assertEquals(2021, ((List) loanTransactionDetails.get("date")).get(0), tz);
                assertEquals(6, ((List) loanTransactionDetails.get("date")).get(1), tz);
                assertEquals(11, ((List) loanTransactionDetails.get("date")).get(2), tz);
                assertEquals(2021, ((List) loanTransactionDetails.get("submittedOnDate")).get(0), tz);
                assertEquals(6, ((List) loanTransactionDetails.get("submittedOnDate")).get(1), tz);
                assertEquals(12, ((List) loanTransactionDetails.get("submittedOnDate")).get(2), tz);
            }
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    private Integer initClient() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "10 June 2021");
        LOG.info("---------------------------------CLIENT CREATED WITH ID--------------------------------------------------- {}", clientID);
        return clientID;
    }

    private Integer initLoan(Integer clientId) {
        final String proposedAmount = "5000";
        final String approveDate = "10 June 2021";
        final String disbursalDate = "10 June 2021";

        // CREATE LOAN PRODUCT
        final Integer loanProductID = this.loanTransactionHelper
                .getLoanProductId(new LoanProductTestBuilder().withSyncExpectedWithDisbursementDate(true).build(null));
        LOG.info("----------------------------------LOAN PRODUCT CREATED WITH ID------------------------------------------- {}",
                loanProductID);

        // APPLY FOR LOAN
        final Integer loanID = applyForLoanApplication(clientId, loanProductID, proposedAmount);
        LOG.info("-----------------------------------LOAN CREATED WITH LOANID------------------------------------------------- {}", loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN STATUS
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(approveDate, loanID);

        // VALIDATE THE LOAN IS APPROVED
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        // DISBURSE A LOAN
        HashMap disbursalError = this.loanTransactionHelper.disburseLoan(disbursalDate, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        return loanID;
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, final String proposedAmount) {
        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));
        final String loanApplication = new LoanApplicationTestBuilder().withPrincipal(proposedAmount).withLoanTermFrequency("5")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("5").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withExpectedDisbursementDate("10 June 2021")
                .withCollaterals(collaterals).withSubmittedOnDate("10 June 2021")
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplication);
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

    // Subset of the world's timezones, (hopefully contains enough variation)
    private List<String> getTimeZones() {
        List<String> timeZones = new ArrayList<>();
        timeZones.add("Africa/Abidjan"); // 0
        timeZones.add("Pacific/Apia"); // -13
        timeZones.add("Pacific/Midway"); // -11
        timeZones.add("America/Adak"); // -10
        timeZones.add("Pacific/Marquesas"); // -9.5
        timeZones.add("America/Anchorage"); // -9
        timeZones.add("America/Los_Angeles"); // -8
        timeZones.add("America/Boise"); // -7
        timeZones.add("America/Belize"); // -6
        timeZones.add("America/Detroit"); // -5
        timeZones.add("America/Caracas"); // -4.5
        timeZones.add("America/Anguilla"); // -4
        timeZones.add("America/St_Johns"); // -3.5
        timeZones.add("America/Araguaina"); // -3
        timeZones.add("Atlantic/South_Georgia"); // -2
        timeZones.add("America/Scoresbysund"); // -1
        timeZones.add("Africa/Abidjan"); // 0
        timeZones.add("Africa/Algiers"); // +1
        timeZones.add("Europe/Budapest"); // +2
        timeZones.add("Europe/Bucharest"); // +2
        timeZones.add("Africa/Nairobi"); // +3
        timeZones.add("Asia/Riyadh"); // +3
        timeZones.add("Asia/Tehran"); // +3.5
        timeZones.add("Asia/Baku"); // +4
        timeZones.add("Asia/Kabul"); // +4.5
        timeZones.add("Indian/Maldives"); // +5
        timeZones.add("Asia/Kolkata"); // +5.5
        timeZones.add("Asia/Colombo"); // +5.5
        timeZones.add("Asia/Kathmandu"); // +5.75
        timeZones.add("Antarctica/Vostok"); // +6
        timeZones.add("Asia/Rangoon"); // +6.5
        timeZones.add("Asia/Jakarta"); // +7
        timeZones.add("Asia/Hong_Kong"); // +8
        timeZones.add("Australia/Eucla"); // +8.75
        timeZones.add("Asia/Seoul"); // +9
        timeZones.add("Australia/Adelaide"); // +9.5
        timeZones.add("Australia/Brisbane"); // +10
        timeZones.add("Australia/Lord_Howe"); // +10.5
        timeZones.add("Asia/Vladivostok"); // +11
        timeZones.add("Pacific/Norfolk"); // +11.5
        timeZones.add("Pacific/Chatham"); // +12.75
        timeZones.add("Antarctica/South_Pole"); // +12
        timeZones.add("Pacific/Kiritimati"); // +14

        return timeZones;
    }
}
