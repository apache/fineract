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
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.fineract.integrationtests.common.*;
import org.apache.fineract.integrationtests.common.loans.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * Tests loan schedule change based on group meeting changes and loan
 * rescheduling
 **/
@SuppressWarnings({ "rawtypes" })
@Ignore
public class DisbursalAndRepaymentScheduleTest {

    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpecForStatusCode403;
    private ResponseSpecification generalResponseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private LoanRescheduleRequestHelper loanRescheduleRequestHelper;
    private Integer loanRescheduleRequestId;
    private Integer clientId;
    private Integer groupId;
    private Integer groupCalendarId;
    private Integer loanProductId;
    private Integer loanId;
    private final String loanPrincipalAmount = "100000.00";
    private final String numberOfRepayments = "12";
    private final String interestRatePerPeriod = "18";

    private final SimpleDateFormat dateFormatterStandard = new SimpleDateFormat("dd MMMM yyyy");

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
    }

    @Test
    public void testRescheduleJLGLoanSynk() {
        System.out.println("---------------------------------STARTING RESCHEDULE JLG LOAN TEST ------------------------------------------");

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

        Calendar groupMeetingChangeCalendar = (Calendar) meetingCalendar.clone();

        meetingCalendar.add(Calendar.WEEK_OF_YEAR, -3);

        final String groupMeetingDate = this.dateFormatterStandard.format(meetingCalendar.getTime());

        final String disbursalDate = groupMeetingDate; // first meeting date
        // after group creation

        final String rescheduleSubmittedDate = this.dateFormatterStandard.format(new java.util.Date());

        final String loanType = "jlg";
        final String rescheduleInterestRate = "28.0";
        groupMeetingChangeCalendar.add(Calendar.DAY_OF_YEAR, 1);
        final String groupMeetingNewStartDate = this.dateFormatterStandard.format(groupMeetingChangeCalendar.getTime());
        // The date
        // from
        // which we
        // start the
        // new group
        // meeting
        // occasion,
        // this is a
        // tuesday.
        groupMeetingChangeCalendar.add(Calendar.WEEK_OF_YEAR, 2);
        final String rescheduleDate = this.dateFormatterStandard.format(groupMeetingChangeCalendar.getTime());

        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.generalResponseSpec = new ResponseSpecBuilder().build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        this.loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(this.requestSpec, this.responseSpec);
        System.out.println("---------------------------------CREATING ENTITIES AND JLG LOAN ------------------------------------------");
        // create all required entities
        this.createRequiredEntitiesForJLGLoanSync(groupMeetingDate);

        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(loanPrincipalAmount).withLoanTermFrequency("24")
                .withLoanTermFrequencyAsWeeks().withNumberOfRepayments("12").withRepaymentEveryAfter("2")
                .withRepaymentFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestCalculationPeriodTypeAsDays()
                .withInterestRatePerPeriod(interestRatePerPeriod).withRepaymentFrequencyTypeAsWeeks().withSubmittedOnDate(disbursalDate)
                .withExpectedDisbursementDate(disbursalDate).withLoanType(loanType).withSyncDisbursementWithMeetin()
                .withCalendarID(this.groupCalendarId.toString())
                .build(this.clientId.toString(), this.groupId.toString(), this.loanProductId.toString(), null);

        this.loanId = this.loanTransactionHelper.getLoanId(loanApplicationJSON);

        // Test for loan account is created
        Assert.assertNotNull(this.loanId);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, this.loanId);

        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        // Test for loan account is created, can be approved
        this.loanTransactionHelper.approveLoan(disbursalDate, this.loanId);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, this.loanId);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        // Test for loan account approved can be disbursed
        this.loanTransactionHelper.disburseLoan(disbursalDate, this.loanId);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, this.loanId);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        System.out.println("---------------------------------CHANGING GROUP MEETING DATE ------------------------------------------");
        CalendarHelper.updateMeetingCalendarForGroup(this.requestSpec, this.responseSpec, this.groupId, this.groupCalendarId.toString(),
                groupMeetingNewStartDate, "2", "2", "2"); // New meeting dates
                                                          // will be the tuesday
                                                          // after the
        // coming
        // monday

        ArrayList loanRepaymnetSchedule = this.loanTransactionHelper
                .getLoanRepaymentSchedule(requestSpec, generalResponseSpec, this.loanId);

        ArrayList dueDateLoanSchedule = (ArrayList) ((HashMap) loanRepaymnetSchedule.get(2)).get("dueDate");
        Calendar dueDateCalendar = Calendar.getInstance();
        dueDateCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        dueDateCalendar.set((Integer) dueDateLoanSchedule.get(0), (Integer) dueDateLoanSchedule.get(1) - 1,
                (Integer) dueDateLoanSchedule.get(2));
        assertEquals("AFTER MEETING CHANGE DATE THE NEXT REPAYMENT SHOULD BE ON TUESDAY", 3, dueDateCalendar.get(Calendar.DAY_OF_WEEK));

        System.out.println("---------------------------------CREATING LOAN RESCHEDULE REQUEST------------------------------------------");

        String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnInterest("2").updateGraceOnPrincipal("2")
                .updateNewInterestRate(rescheduleInterestRate).updateRescheduleFromDate(rescheduleDate)
                .updateSubmittedOnDate(rescheduleSubmittedDate).build(this.loanId.toString());

        this.loanRescheduleRequestId = this.loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
        this.loanRescheduleRequestHelper.verifyCreationOfLoanRescheduleRequest(this.loanRescheduleRequestId);

        loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec, this.loanId);
        dueDateLoanSchedule = (ArrayList) ((HashMap) loanRepaymnetSchedule.get(2)).get("dueDate");
        dueDateCalendar.set((Integer) dueDateLoanSchedule.get(0), (Integer) dueDateLoanSchedule.get(1) - 1,
                (Integer) dueDateLoanSchedule.get(2));
        assertEquals("AFTER MEETING CHANGE DATE THE NEXT REPAYMENT SHOULD BE ON TUESDAY, EVEN AFTER LOAN RESCHEDULE REQUEST WAS SENT", 3,
                dueDateCalendar.get(Calendar.DAY_OF_WEEK));

        System.out.println("Successfully created loan reschedule request (ID: " + this.loanRescheduleRequestId + ")");

        System.out.println("-----------------------------APPROVING LOAN RESCHEDULE REQUEST--------------------------");

        requestJSON = new LoanRescheduleRequestTestBuilder().updateSubmittedOnDate(rescheduleSubmittedDate)
                .getApproveLoanRescheduleRequestJSON();
        this.loanRescheduleRequestHelper.approveLoanRescheduleRequest(this.loanRescheduleRequestId, requestJSON);

        final HashMap response = (HashMap) this.loanRescheduleRequestHelper.getLoanRescheduleRequest(loanRescheduleRequestId, "statusEnum");
        assertTrue((Boolean) response.get("approved"));

        loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec, this.loanId);

        dueDateLoanSchedule = (ArrayList) ((HashMap) loanRepaymnetSchedule.get(2)).get("dueDate");
        dueDateCalendar.set((Integer) dueDateLoanSchedule.get(0), (Integer) dueDateLoanSchedule.get(1) - 1,
                (Integer) dueDateLoanSchedule.get(2));
        assertEquals("AFTER MEETING CHANGE DATE THE NEXT REPAYMENT SHOULD BE ON TUESDAY, EVEN AFTER RESCHEDULE", 3,
                dueDateCalendar.get(Calendar.DAY_OF_WEEK));
        System.out.println("Successfully changed group meeting date (CAELNDAR ID: " + this.groupCalendarId
                + ") and rescheduled loan (RESCHEDULE ID: " + this.loanRescheduleRequestId + ")");

        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpecForStatusCode403);
    }

    @Test
    public void testChangeGroupMeetingMaturedOnDate() {
        System.out
                .println("---------------------------------STARTING GROUP LOAN MEETING CHANGE DATE EXPECTED MATURED CHANGE------------------------------------------");

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

        Calendar groupMeetingChangeCalendar = (Calendar) meetingCalendar.clone();

        meetingCalendar.add(Calendar.WEEK_OF_YEAR, -3);

        final String groupMeetingDate = this.dateFormatterStandard.format(meetingCalendar.getTime());

        final String disbursalDate = groupMeetingDate; // first meeting date
                                                       // after group creation

        final String loanType = "jlg";
        groupMeetingChangeCalendar.add(Calendar.DAY_OF_YEAR, 1);
        final String groupMeetingNewStartDate = this.dateFormatterStandard.format(groupMeetingChangeCalendar.getTime());
        // The date
        // from
        // which we
        // start the
        // new group
        // meeting
        // occasion,
        // this is a
        // tuesday.

        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.generalResponseSpec = new ResponseSpecBuilder().build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        this.loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(this.requestSpec, this.responseSpec);
        System.out.println("---------------------------------CREATING ENTITIES AND JLG LOAN ------------------------------------------");
        // create all required entities
        this.createRequiredEntitiesForJLGLoanSync(groupMeetingDate);

        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(loanPrincipalAmount).withLoanTermFrequency("24")
                .withLoanTermFrequencyAsWeeks().withNumberOfRepayments("12").withRepaymentEveryAfter("2")
                .withRepaymentFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestCalculationPeriodTypeAsDays()
                .withInterestRatePerPeriod(interestRatePerPeriod).withRepaymentFrequencyTypeAsWeeks().withSubmittedOnDate(disbursalDate)
                .withExpectedDisbursementDate(disbursalDate).withLoanType(loanType).withSyncDisbursementWithMeetin()
                .withCalendarID(this.groupCalendarId.toString())
                .build(this.clientId.toString(), this.groupId.toString(), this.loanProductId.toString(), null);

        this.loanId = this.loanTransactionHelper.getLoanId(loanApplicationJSON);

        // Test for loan account is created
        Assert.assertNotNull(this.loanId);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, this.loanId);

        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        // Test for loan account is created, can be approved
        this.loanTransactionHelper.approveLoan(disbursalDate, this.loanId);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, this.loanId);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        // Test for loan account approved can be disbursed
        this.loanTransactionHelper.disburseLoan(disbursalDate, this.loanId);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, this.loanId);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        System.out.println("---------------------------------CHANGING GROUP MEETING DATE ------------------------------------------");
        CalendarHelper.updateMeetingCalendarForGroup(this.requestSpec, this.responseSpec, this.groupId, this.groupCalendarId.toString(),
                groupMeetingNewStartDate, "2", "2", "2"); // New meeting dates
                                                          // will be the tuesday
                                                          // after the
                                                          // coming
                                                          // monday

        Calendar expectedMaturityCalendar = Calendar.getInstance();
        expectedMaturityCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        ArrayList expectedMaturityDate = ((ArrayList) ((HashMap) this.loanTransactionHelper.getLoanDetail(requestSpec, generalResponseSpec,
                this.loanId, "timeline")).get("expectedMaturityDate"));

        expectedMaturityCalendar.set((Integer) expectedMaturityDate.get(0), (Integer) expectedMaturityDate.get(1) - 1,
                (Integer) expectedMaturityDate.get(2));

        assertEquals("AFTER MEETING CHANGE DATE THE EXPECTED MATURITY SHOULD BE ON TUESDAY", 3,
                expectedMaturityCalendar.get(Calendar.DAY_OF_WEEK));

        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpecForStatusCode403);
    }

    /**
     * entities for jlg loan
     **/
    private void createRequiredEntitiesForJLGLoanSync(final String groupActivationDate) {
        this.createGroupEntityWithCalendar("2", "2", "1", groupActivationDate);// frequency=2:Weekly
        // , interval=2:
        // Every two weeks ,
        // repeatsOnDay=1:Monday
        // groupActivationDate is decided by the current date
        this.createClientEntity();
        this.associateClientToGroup(this.groupId, this.clientId);
        this.createLoanProductEntity();

    }

    /*
     * Associate client to the group
     */

    private void associateClientToGroup(final Integer groupId, final Integer clientId) {
        GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupId.toString(), clientId.toString());
        GroupHelper.verifyGroupMembers(this.requestSpec, this.responseSpec, groupId, clientId);
    }

    private void createGroupEntityWithCalendar(final String frequency, final String interval, final String repeatsOnDay,
            final String groupActivationDate) {
        this.groupId = GroupHelper.createGroup(this.requestSpec, this.responseSpec, groupActivationDate);
        GroupHelper.verifyGroupCreatedOnServer(this.requestSpec, this.responseSpec, this.groupId);

        final String startDate = groupActivationDate;

        this.setGroupCalendarId(CalendarHelper.createMeetingCalendarForGroup(this.requestSpec, this.responseSpec, this.groupId, startDate,
                frequency, interval, repeatsOnDay));
    }

    /**
     * create a new client
     **/
    private void createClientEntity() {
        this.clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, this.clientId);
    }

    /**
     * create a new loan product
     **/
    private void createLoanProductEntity() {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(loanPrincipalAmount)
                .withNumberOfRepayments(numberOfRepayments).withinterestRatePerPeriod(interestRatePerPeriod)
                .withInterestRateFrequencyTypeAsYear().build(null);
        this.loanProductId = this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    public void setGroupCalendarId(Integer groupCalendarId) {
        this.groupCalendarId = groupCalendarId;
    }
}