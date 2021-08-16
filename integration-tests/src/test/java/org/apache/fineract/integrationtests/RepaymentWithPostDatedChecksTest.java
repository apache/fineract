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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepaymentWithPostDatedChecksTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private static final Logger LOG = LoggerFactory.getLogger(RepaymentWithPostDatedChecksTest.class);
    private LoanTransactionHelper loanTransactionHelper;
    private Integer loanID;
    private Integer disbursementId;
    final String approveDate = "01 March 2014";
    final String expectedDisbursementDate = "01 March 2014";
    final String proposedAmount = "5000";
    final String approvalAmount = "5000";
    private final SimpleDateFormat dateFormatterStandard = new SimpleDateFormat("dd MMMM yyyy");

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testRepaymentWithPostDatedChecks() {

        Calendar meetingCalendar = Calendar.getInstance();
        meetingCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        meetingCalendar.setTime(new java.util.Date());

        int today = meetingCalendar.get(Calendar.DAY_OF_WEEK);
        // making sure that the meeting calendar is set for the coming monday.
        if (today >= Calendar.MONDAY) {
            meetingCalendar.add(Calendar.DAY_OF_YEAR, +(Calendar.MONDAY - today + 7));
        } else {
            meetingCalendar.add(Calendar.DAY_OF_YEAR, +(Calendar.MONDAY - today));
        }

        meetingCalendar.add(Calendar.WEEK_OF_YEAR, -3);

        final String groupMeetingDate = this.dateFormatterStandard.format(meetingCalendar.getTime());

        final String disbursalDate = groupMeetingDate;

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(false, "1");

        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("100000") //
                .withLoanTermFrequency("2") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("2") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("20 September 2011") //
                .withSubmittedOnDate("20 September 2011") //
                .build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = this.loanTransactionHelper.getLoanId(loanApplicationJSON);

        // Test for loan account is created
        Assertions.assertNotNull(loanId);

        /**
         * TODO: Add Post Dated Check Data.
         */
        final ArrayList<HashMap> installmentData = this.loanTransactionHelper.getRepayments(loanId);
        List<HashMap> postDatedChecks = new ArrayList<>();
        Gson gson = new Gson();

        for (int i = 0; i < installmentData.size(); i++) {
            String result = gson.toJson(installmentData.get(i));
            JsonObject reportObject = JsonParser.parseString(result).getAsJsonObject();
            final Integer installmentId = reportObject.get("installmentId").getAsInt();
            final BigDecimal amount = reportObject.get("amount").getAsBigDecimal();
            final String date = reportObject.get("date").getAsString();
            postDatedChecks.add(postDatedCheck(installmentId, amount, date));
        }

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanId);

        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        // Test for loan account is created, can be approved
        this.loanTransactionHelper.approveLoan(disbursalDate, loanId);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanId);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        // Test for loan account approved can be disbursed
        this.loanTransactionHelper.disburseLoanWithPostDatedChecks(disbursalDate, loanId, BigDecimal.valueOf(100000), postDatedChecks);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanId);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
    }

    private HashMap<String, String> postDatedCheck(final Integer installmentId, final BigDecimal amount, final String date) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("installmentId", installmentId.toString());
        map.put("name", "AMANA BANK");
        map.put("amount", amount.toString());
        map.put("accountNo", "900400500600");
        map.put("checkNo", "200500600700");
        map.put("date", date);

        return map;
    }

    private Integer createLoanProduct(final boolean multiDisburseLoan, final String accountingRule, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder() //
                .withPrincipal("12,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withTranches(multiDisburseLoan) //
                .withAccounting(accountingRule, accounts);
        if (multiDisburseLoan) {
            builder = builder.withInterestCalculationPeriodTypeAsRepaymentPeriod(true);
        }
        final String loanProductJSON = builder.build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

}
