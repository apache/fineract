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


import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.fineract.integrationtests.common.CalendarHelper;
import org.apache.fineract.integrationtests.common.CenterDomain;
import org.apache.fineract.integrationtests.common.CenterHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GroupHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;

import com.google.gson.Gson;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class LoanReschedulingWithinCenterTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private ResponseSpecification generalResponseSpec;
    private LoanApplicationApprovalTest loanApplicationApprovalTest;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.loanApplicationApprovalTest = new LoanApplicationApprovalTest();
        this.generalResponseSpec = new ResponseSpecBuilder().build();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testCenterReschedulingLoansWithInterestRecalculationEnabled() {

        Integer officeId = new OfficeHelper(requestSpec, responseSpec).createOffice("01 July 2007");
        String name = "TestFullCreation" + new Timestamp(new java.util.Date().getTime());
        String externalId = Utils.randomStringGenerator("ID_", 7, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        int staffId = StaffHelper.createStaff(requestSpec, responseSpec);
        int[] groupMembers = generateGroupMembers(1, officeId);
        final String centerActivationDate = "01 July 2007";
        Integer centerId = CenterHelper.createCenter(name, officeId, externalId, staffId, groupMembers, centerActivationDate, requestSpec,
                responseSpec);
        CenterDomain center = CenterHelper.retrieveByID(centerId, requestSpec, responseSpec);
        Integer groupId = groupMembers[0];
        Assert.assertNotNull(center);
        Assert.assertTrue(center.getStaffId() == staffId);
        Assert.assertTrue(center.isActive() == true);

        Integer calendarId = createCalendarMeeting(centerId);

        Integer clientId = createClient(officeId);

        associateClientsToGroup(groupId, clientId);
        
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
        Calendar today = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        today.add(Calendar.DAY_OF_MONTH, -14);
        // CREATE A LOAN PRODUCT
        final String disbursalDate = dateFormat.format(today.getTime());
        final String recalculationRestFrequencyDate = "01 January 2012";
        final boolean isMultiTrancheLoan = false;

        // CREATE LOAN MULTIDISBURSAL PRODUCT WITH INTEREST RECALCULATION
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.RBI_INDIA_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "0", recalculationRestFrequencyDate,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, isMultiTrancheLoan, 
                null, null);

        // APPLY FOR TRANCHE LOAN WITH INTEREST RECALCULATION
        final Integer loanId = applyForLoanApplicationForInterestRecalculation(clientId, groupId, calendarId, loanProductID, disbursalDate,
                recalculationRestFrequencyDate, LoanApplicationTestBuilder.RBI_INDIA_STRATEGY, new ArrayList<HashMap>(0), null);

        // Test for loan account is created
        Assert.assertNotNull(loanId);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanId);

        // Test for loan account is created, can be approved
        this.loanTransactionHelper.approveLoan(disbursalDate, loanId);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanId);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        // Test for loan account approved can be disbursed
        this.loanTransactionHelper.disburseLoan(disbursalDate, loanId);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanId);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        System.out.println("---------------------------------CHANGING GROUP MEETING DATE ------------------------------------------");
        Calendar todaysdate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysdate.add(Calendar.DAY_OF_MONTH, 14);
        String oldMeetingDate = dateFormat.format(todaysdate.getTime());
        todaysdate.add(Calendar.DAY_OF_MONTH, 1);
        final String centerMeetingNewStartDate = dateFormat.format(todaysdate.getTime());
        CalendarHelper.updateMeetingCalendarForCenter(this.requestSpec, this.responseSpec, centerId, calendarId.toString(), oldMeetingDate,
                centerMeetingNewStartDate);

        ArrayList loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec, loanId);
        // VERIFY RESCHEDULED DATE
        ArrayList dueDateLoanSchedule = (ArrayList) ((HashMap) loanRepaymnetSchedule.get(2)).get("dueDate");
        assertEquals(getDateAsArray(todaysdate, 0), dueDateLoanSchedule);

        // VERIFY THE INTEREST
        Float interestDue = (Float) ((HashMap) loanRepaymnetSchedule.get(2)).get("interestDue");
        assertEquals(String.valueOf(interestDue), "90.82");

    }
    
    private void associateClientsToGroup(Integer groupId, Integer clientId) {
        // Associate client to the group
        GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupId.toString(), clientId.toString());
        GroupHelper.verifyGroupMembers(this.requestSpec, this.responseSpec, groupId, clientId);
    }

    private Integer createClient(Integer officeId) {
        // CREATE CLIENT
        final String clientActivationDate = "01 July 2014";
        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, clientActivationDate, officeId.toString());
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);
        return clientId;
    }

    private Integer createCalendarMeeting(Integer centerId) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
        Calendar today = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String startDate = dateFormat.format(today.getTime());
        final String frequency = "2"; // 2:Weekly
        final String interval = "2"; // Every one week
        final Integer repeatsOnDay = today.get(Calendar.DAY_OF_WEEK) - 1;

        Integer calendarId = CalendarHelper.createMeetingForGroup(this.requestSpec, this.responseSpec, centerId, startDate, frequency,
                interval, repeatsOnDay.toString());
        System.out.println("calendarId " + calendarId);
        return calendarId;
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testCenterReschedulingMultiTrancheLoansWithInterestRecalculationEnabled() {

        Integer officeId = new OfficeHelper(requestSpec, responseSpec).createOffice("01 July 2007");
        String name = "TestFullCreation" + new Timestamp(new java.util.Date().getTime());
        String externalId = Utils.randomStringGenerator("ID_", 7, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        int staffId = StaffHelper.createStaff(requestSpec, responseSpec);
        int[] groupMembers = generateGroupMembers(1, officeId);
        final String centerActivationDate = "01 July 2007";
        Integer centerId = CenterHelper.createCenter(name, officeId, externalId, staffId, groupMembers, centerActivationDate, requestSpec,
                responseSpec);
        CenterDomain center = CenterHelper.retrieveByID(centerId, requestSpec, responseSpec);
        Integer groupId = groupMembers[0];
        Assert.assertNotNull(center);
        Assert.assertTrue(center.getStaffId() == staffId);
        Assert.assertTrue(center.isActive() == true);

        Integer calendarId = createCalendarMeeting(centerId);

        Integer clientId = createClient(officeId);

        associateClientsToGroup(groupId, clientId);

        // CREATE A LOAN PRODUCT
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
        Calendar today = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        today.add(Calendar.DAY_OF_MONTH, -14);
        // CREATE A LOAN PRODUCT
        final String approveDate = dateFormat.format(today.getTime());
        final String expectedDisbursementDate = dateFormat.format(today.getTime());
        final String disbursementDate = dateFormat.format(today.getTime());
        final String approvalAmount = "10000";
        final String recalculationRestFrequencyDate = "01 January 2012";
        final boolean isMultiTrancheLoan = true;

        // CREATE LOAN MULTIDISBURSAL PRODUCT WITH INTEREST RECALCULATION
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.RBI_INDIA_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "0", recalculationRestFrequencyDate,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, isMultiTrancheLoan, 
                null, null);

        Calendar seondTrancheDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        seondTrancheDate.add(Calendar.MONTH, 1);
        String secondDisbursement = dateFormat.format(seondTrancheDate.getTime()) ;
        
        // CREATE TRANCHES
        List<HashMap> createTranches = new ArrayList<>();
        createTranches.add(this.loanApplicationApprovalTest.createTrancheDetail(disbursementDate, "5000"));
        createTranches.add(this.loanApplicationApprovalTest.createTrancheDetail(secondDisbursement, "5000"));

        // APPROVE TRANCHES
        List<HashMap> approveTranches = new ArrayList<>();
        approveTranches.add(this.loanApplicationApprovalTest.createTrancheDetail(disbursementDate, "5000"));
        approveTranches.add(this.loanApplicationApprovalTest.createTrancheDetail(secondDisbursement, "5000"));

        // APPLY FOR TRANCHE LOAN WITH INTEREST RECALCULATION
        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientId, groupId, calendarId, loanProductID,
                disbursementDate, recalculationRestFrequencyDate, LoanApplicationTestBuilder.RBI_INDIA_STRATEGY, new ArrayList<HashMap>(0),
                createTranches);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN STATUS
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount(approveDate, expectedDisbursementDate, approvalAmount,
                loanID, approveTranches);

        // VALIDATE THE LOAN IS APPROVED
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE A FIRST TRANCHE
        this.loanTransactionHelper.disburseLoan(disbursementDate, loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        System.out.println("---------------------------------CHANGING GROUP MEETING DATE ------------------------------------------");
        Calendar todaysdate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysdate.add(Calendar.DAY_OF_MONTH, 14);
        String oldMeetingDate = dateFormat.format(todaysdate.getTime());
        todaysdate.add(Calendar.DAY_OF_MONTH, 1);
        final String centerMeetingNewStartDate = dateFormat.format(todaysdate.getTime());
        CalendarHelper.updateMeetingCalendarForCenter(this.requestSpec, this.responseSpec, centerId, calendarId.toString(), oldMeetingDate,
                centerMeetingNewStartDate);

        ArrayList loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec, loanID);
        // VERIFY RESCHEDULED DATE
        ArrayList dueDateLoanSchedule = (ArrayList) ((HashMap) loanRepaymnetSchedule.get(2)).get("dueDate");
        assertEquals(getDateAsArray(todaysdate, 0), dueDateLoanSchedule);

        // VERIFY THE INTEREST
        Float interestDue = (Float) ((HashMap) loanRepaymnetSchedule.get(2)).get("interestDue");
        assertEquals(String.valueOf(interestDue), "41.05");

    }

    private Integer createLoanProductWithInterestRecalculation(final String repaymentStrategy,
            final String interestRecalculationCompoundingMethod, final String rescheduleStrategyMethod,
            final String recalculationRestFrequencyType, final String recalculationRestFrequencyInterval,
            final String recalculationRestFrequencyDate, final String preCloseInterestCalculationStrategy, final Account[] accounts,
            final boolean isMultiTrancheLoan, final Integer recalculationRestFrequencyOnDayType, final Integer recalculationRestFrequencyDayOfWeekType) {
        final String recalculationCompoundingFrequencyType = null;
        final String recalculationCompoundingFrequencyInterval = null;
        final String recalculationCompoundingFrequencyDate = null;
        final Integer recalculationCompoundingFrequencyOnDayType = null;
        final Integer recalculationCompoundingFrequencyDayOfWeekType = null;
        return createLoanProductWithInterestRecalculation(repaymentStrategy, interestRecalculationCompoundingMethod,
                rescheduleStrategyMethod, recalculationRestFrequencyType, recalculationRestFrequencyInterval,
                recalculationRestFrequencyDate, recalculationCompoundingFrequencyType, recalculationCompoundingFrequencyInterval,
                recalculationCompoundingFrequencyDate, preCloseInterestCalculationStrategy, accounts, null, false, isMultiTrancheLoan, 
                recalculationCompoundingFrequencyOnDayType, recalculationCompoundingFrequencyDayOfWeekType, recalculationRestFrequencyOnDayType, 
                recalculationRestFrequencyDayOfWeekType);
    }

    private Integer createLoanProductWithInterestRecalculation(final String repaymentStrategy,
            final String interestRecalculationCompoundingMethod, final String rescheduleStrategyMethod,
            final String recalculationRestFrequencyType, final String recalculationRestFrequencyInterval,
            final String recalculationRestFrequencyDate, final String recalculationCompoundingFrequencyType,
            final String recalculationCompoundingFrequencyInterval, final String recalculationCompoundingFrequencyDate,
            final String preCloseInterestCalculationStrategy, final Account[] accounts, final String chargeId,
            boolean isArrearsBasedOnOriginalSchedule, final boolean isMultiTrancheLoan, 
            final Integer recalculationCompoundingFrequencyOnDayType,
            final Integer recalculationCompoundingFrequencyDayOfWeekType, final Integer recalculationRestFrequencyOnDayType,
            final Integer recalculationRestFrequencyDayOfWeekType) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder()
                .withPrincipal("10000.00")
                .withNumberOfRepayments("12")
                .withRepaymentAfterEvery("2")
                .withRepaymentTypeAsWeek()
                .withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths()
                .withTranches(isMultiTrancheLoan)
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .withRepaymentStrategy(repaymentStrategy)
                .withInterestTypeAsDecliningBalance()
                .withInterestRecalculationDetails(interestRecalculationCompoundingMethod, rescheduleStrategyMethod,
                        preCloseInterestCalculationStrategy)
                .withInterestRecalculationRestFrequencyDetails(recalculationRestFrequencyType, recalculationRestFrequencyInterval,
                        recalculationRestFrequencyOnDayType, recalculationRestFrequencyDayOfWeekType)
                .withInterestRecalculationCompoundingFrequencyDetails(recalculationCompoundingFrequencyType,
                        recalculationCompoundingFrequencyInterval, recalculationCompoundingFrequencyOnDayType,
                        recalculationCompoundingFrequencyDayOfWeekType);
        if (accounts != null) {
            builder = builder.withAccountingRulePeriodicAccrual(accounts);
        }

        if (isArrearsBasedOnOriginalSchedule) builder = builder.withArrearsConfiguration();

        final String loanProductJSON = builder.build(chargeId);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    @SuppressWarnings("rawtypes")
    private Integer applyForLoanApplicationForInterestRecalculation(final Integer clientID, Integer groupId, Integer calendarId,
            final Integer loanProductID, final String disbursementDate, final String restStartDate, final String repaymentStrategy,
            final List<HashMap> charges, List<HashMap> tranches) {
        final String graceOnInterestPayment = null;
        final String compoundingStartDate = null;
        final String graceOnPrincipalPayment = null;
        return applyForLoanApplicationForInterestRecalculation(clientID, groupId, calendarId, loanProductID, disbursementDate,
                restStartDate, compoundingStartDate, repaymentStrategy, charges, graceOnInterestPayment, graceOnPrincipalPayment, tranches);
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    private Integer applyForLoanApplicationForInterestRecalculation(final Integer clientID, Integer groupId, Integer calendarId,
            final Integer loanProductID, final String disbursementDate, final String restStartDate, final String compoundingStartDate,
            final String repaymentStrategy, final List<HashMap> charges, final String graceOnInterestPayment,
            final String graceOnPrincipalPayment, List<HashMap> tranches) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("10000.00") //
                .withLoanTermFrequency("24") //
                .withLoanTermFrequencyAsWeeks() //
                .withNumberOfRepayments("12") //
                .withRepaymentEveryAfter("2") //
                .withRepaymentFrequencyTypeAsWeeks() //
                .withInterestRatePerPeriod("2").withLoanType("jlg") //
                .withCalendarID(calendarId.toString()).withAmortizationTypeAsEqualInstallments() //
                .withFixedEmiAmount("") //
                .withTranches(tranches).withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeAsDays() //
                .withInterestCalculationPeriodTypeAsDays() //
                .withExpectedDisbursementDate(disbursementDate) //
                .withSubmittedOnDate(disbursementDate) //
                .withwithRepaymentStrategy(repaymentStrategy) //
                .withCharges(charges)//
                .build(clientID.toString(), groupId.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private int[] generateGroupMembers(int size, int officeId) {
        int[] groupMembers = new int[size];
        for (int i = 0; i < groupMembers.length; i++) {
            final HashMap<String, String> map = new HashMap<>();
            map.put("officeId", "" + officeId);
            map.put("name", Utils.randomStringGenerator("Group_Name_", 5));
            map.put("externalId", Utils.randomStringGenerator("ID_", 7, "ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
            map.put("dateFormat", "dd MMMM yyyy");
            map.put("locale", "en");
            map.put("active", "true");
            map.put("activationDate", "04 March 2011");

            groupMembers[i] = Utils.performServerPost(requestSpec, responseSpec, "/fineract-provider/api/v1/groups?"
                    + Utils.TENANT_IDENTIFIER, new Gson().toJson(map), "groupId");
        }
        return groupMembers;
    }

    private List getDateAsArray(Calendar date, int addPeriod) {
        return getDateAsArray(date, addPeriod, Calendar.DAY_OF_MONTH);
    }

    private List getDateAsArray(Calendar date, int addvalue, int type) {
        date.add(type, addvalue);
        return new ArrayList<>(Arrays.asList(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH)));
    }

}