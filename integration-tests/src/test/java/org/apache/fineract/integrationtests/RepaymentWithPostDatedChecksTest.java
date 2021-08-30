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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RepaymentWithPostDatedChecksTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private final SimpleDateFormat dateFormatterStandard = new SimpleDateFormat("dd MMMM yyyy");
    private LoanTransactionHelper loanTransactionHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testRepaymentWithPostDatedChecks() {
        this.loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);

        // Calendar meetingCalendar = Calendar.getInstance();
        // meetingCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        // meetingCalendar.setTime(new java.util.Date());
        //
        // int today = meetingCalendar.get(Calendar.DAY_OF_WEEK);
        // // making sure that the meeting calendar is set for the coming monday.
        // if (today >= Calendar.MONDAY) {
        // meetingCalendar.add(Calendar.DAY_OF_YEAR, +(Calendar.MONDAY - today + 7));
        // } else {
        // meetingCalendar.add(Calendar.DAY_OF_YEAR, +(Calendar.MONDAY - today));
        // }
        //
        // meetingCalendar.add(Calendar.WEEK_OF_YEAR, -3);
        //
        // final String groupMeetingDate = this.dateFormatterStandard.format(meetingCalendar.getTime());
        //
        // final String disbursalDate = groupMeetingDate;

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build(null));
        Assertions.assertNotNull(loanProductID, "Could not create Loan Product");

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, "8000");
        Assertions.assertNotNull(loanID, "Could not create Loan Account");

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        // Test for loan account is created, can be approved
        this.loanTransactionHelper.approveLoan("02 April 2012", loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        // Get repayments
        final ArrayList<HashMap> installmentData = this.loanTransactionHelper.getRepayments(loanID);
        Assertions.assertNotNull(installmentData, "Empty Installment Data");
        List<HashMap> postDatedChecks = new ArrayList<>();
        Gson gson = new Gson();

        for (int i = 0; i < installmentData.size(); i++) {
            String result = gson.toJson(installmentData.get(i));
            JsonObject reportObject = JsonParser.parseString(result).getAsJsonObject();
            final Integer installmentId = reportObject.get("installmentId").getAsInt();
            final BigDecimal amount = reportObject.get("amount").getAsBigDecimal();
            postDatedChecks.add(postDatedCheck(installmentId, amount));
        }

        Assertions.assertNotNull(postDatedChecks);

        // Test for loan account approved can be disbursed
        this.loanTransactionHelper.disburseLoanWithPostDatedChecks("04 April 2012", loanID, BigDecimal.valueOf(8000), postDatedChecks);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, final String proposedAmount) {
        final String loanApplication = new LoanApplicationTestBuilder().withPrincipal(proposedAmount).withLoanTermFrequency("5")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("5").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withExpectedDisbursementDate("04 April 2012")
                .withSubmittedOnDate("02 April 2012").build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplication);
    }

    private HashMap<String, String> postDatedCheck(final Integer installmentId, final BigDecimal amount) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("installmentId", installmentId.toString());
        map.put("name", "AMANA BANK");
        map.put("amount", amount.toString());
        map.put("accountNo", "900400500621");
        map.put("checkNo", "200500600711");

        return map;
    }

}
