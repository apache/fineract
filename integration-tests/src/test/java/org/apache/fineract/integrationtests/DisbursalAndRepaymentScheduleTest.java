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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import org.apache.fineract.client.models.GetLoanRescheduleRequestResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.PostCreateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.CalendarHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanRescheduleRequestTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

/**
 * Tests loan schedule change based on group meeting changes and loan rescheduling
 **/
public class DisbursalAndRepaymentScheduleTest extends FeignLoanTestBase {

    private static final String LOAN_PRINCIPAL_AMOUNT = "100000.00";
    private static final String NUMBER_OF_REPAYMENTS = "12";
    private static final String INTEREST_RATE_PER_PERIOD = "18";
    private static final String JLG_LOAN_TYPE = "jlg";

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);

    private final FeignGroupHelper groupHelper = new FeignGroupHelper(FineractFeignClientHelper.getFineractFeignClient());

    private Long clientId;
    private Long groupId;
    private Long groupCalendarId;
    private Long loanProductId;

    @Test
    public void testRescheduleJLGLoanSynk() {
        final LocalDate comingMonday = comingMonday();
        final String groupMeetingDate = dateFormatter.format(comingMonday.minusWeeks(3));
        final String disbursalDate = groupMeetingDate; // first meeting date after group creation
        final String rescheduleSubmittedDate = dateFormatter.format(LocalDate.now(java.time.ZoneId.systemDefault()));
        final String rescheduleInterestRate = "28.0";

        // The new group meeting occasion starts the day after the coming monday, a tuesday.
        final LocalDate groupMeetingNewStart = comingMonday.plusDays(1);
        final String groupMeetingNewStartDate = dateFormatter.format(groupMeetingNewStart);
        final String rescheduleDate = dateFormatter.format(groupMeetingNewStart.plusWeeks(2));

        createRequiredEntitiesForJLGLoanSync(groupMeetingDate);

        final Long loanId = applyForJlgLoan(disbursalDate);
        assertNotNull(loanId);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanId, LoanRequestBuilders.approveLoan(Double.parseDouble(LOAN_PRINCIPAL_AMOUNT), disbursalDate));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);

        disburseWithNetDisbursalAmount(loanId, disbursalDate);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);

        CalendarHelper.updateMeetingCalendarForGroup(groupId, groupCalendarId.toString(), groupMeetingNewStartDate, "2", "2", "2");

        assertEquals(DayOfWeek.TUESDAY, thirdInstallmentDueDate(loanId),
                "AFTER MEETING CHANGE DATE THE NEXT REPAYMENT SHOULD BE ON TUESDAY");

        final PostCreateRescheduleLoansRequest createRequest = new LoanRescheduleRequestTestBuilder().updateGraceOnInterest("2")
                .updateGraceOnPrincipal("2").updateNewInterestRate(rescheduleInterestRate).updateRescheduleFromDate(rescheduleDate)
                .updateSubmittedOnDate(rescheduleSubmittedDate).buildRequest(loanId);

        final Long loanRescheduleRequestId = LoanRescheduleRequestHelper.createLoanRescheduleRequest(createRequest).getResourceId();
        assertNotNull(loanRescheduleRequestId, "ERROR IN CREATING LOAN RESCHEDULE REQUEST");

        assertEquals(DayOfWeek.TUESDAY, thirdInstallmentDueDate(loanId),
                "AFTER MEETING CHANGE DATE THE NEXT REPAYMENT SHOULD BE ON TUESDAY, EVEN AFTER LOAN RESCHEDULE REQUEST WAS SENT");

        final PostUpdateRescheduleLoansRequest approveRequest = new LoanRescheduleRequestTestBuilder()
                .updateSubmittedOnDate(rescheduleSubmittedDate).getApproveRequest();
        LoanRescheduleRequestHelper.approveLoanRescheduleRequest(loanRescheduleRequestId, approveRequest);

        final GetLoanRescheduleRequestResponse response = LoanRescheduleRequestHelper.readLoanRescheduleRequest(loanRescheduleRequestId,
                null);
        assertTrue(response.getStatusEnum().getApproved());

        assertEquals(DayOfWeek.TUESDAY, thirdInstallmentDueDate(loanId),
                "AFTER MEETING CHANGE DATE THE NEXT REPAYMENT SHOULD BE ON TUESDAY, EVEN AFTER RESCHEDULE");
    }

    @Test
    public void testChangeGroupMeetingMaturedOnDate() {
        final LocalDate comingMonday = comingMonday();
        final String groupMeetingDate = dateFormatter.format(comingMonday.minusWeeks(3));
        final String disbursalDate = groupMeetingDate; // first meeting date after group creation

        // The new group meeting occasion starts the day after the coming monday, a tuesday.
        final String groupMeetingNewStartDate = dateFormatter.format(comingMonday.plusDays(1));

        createRequiredEntitiesForJLGLoanSync(groupMeetingDate);

        final Long loanId = applyForJlgLoan(disbursalDate);
        assertNotNull(loanId);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanId, LoanRequestBuilders.approveLoan(Double.parseDouble(LOAN_PRINCIPAL_AMOUNT), disbursalDate));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);

        disburseWithNetDisbursalAmount(loanId, disbursalDate);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);

        CalendarHelper.updateMeetingCalendarForGroup(groupId, groupCalendarId.toString(), groupMeetingNewStartDate, "2", "2", "2");

        assertEquals(DayOfWeek.TUESDAY, getLoanDetails(loanId).getTimeline().getExpectedMaturityDate().getDayOfWeek(),
                "AFTER MEETING CHANGE DATE THE EXPECTED MATURITY SHOULD BE ON TUESDAY");
    }

    /** The coming monday; when today is a monday, the monday a week from now. */
    private LocalDate comingMonday() {
        return LocalDate.now(java.time.ZoneId.systemDefault()).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    private DayOfWeek thirdInstallmentDueDate(final Long loanId) {
        return getLoanDetails(loanId).getRepaymentSchedule().getPeriods().get(2).getDueDate().getDayOfWeek();
    }

    private void disburseWithNetDisbursalAmount(final Long loanId, final String disbursalDate) {
        disburseLoan(loanId, new PostLoansLoanIdRequest()//
                .actualDisbursementDate(disbursalDate)//
                .netDisbursalAmount(getLoanDetails(loanId).getNetDisbursalAmount())//
                .note("DISBURSE NOTE")//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN));
    }

    private Long applyForJlgLoan(final String disbursalDate) {
        final PostLoansRequest application = LoanRequestBuilders
                .applyLoan(clientId, loanProductId, disbursalDate, Double.parseDouble(LOAN_PRINCIPAL_AMOUNT),
                        Integer.parseInt(NUMBER_OF_REPAYMENTS))//
                .loanTermFrequency(24)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.WEEKS)//
                .repaymentEvery(2)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.WEEKS)//
                .interestRatePerPeriod(BigDecimal.valueOf(Double.parseDouble(INTEREST_RATE_PER_PERIOD)))//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .groupId(groupId)//
                .loanType(JLG_LOAN_TYPE)//
                .calendarId(groupCalendarId)//
                .syncDisbursementWithMeeting(true);

        return loanHelper.applyForLoan(application).getLoanId();
    }

    /**
     * entities for jlg loan
     **/
    private void createRequiredEntitiesForJLGLoanSync(final String groupActivationDate) {
        // frequency=2:Weekly, interval=2: Every two weeks, repeatsOnDay=1:Monday
        // groupActivationDate is decided by the current date
        createGroupEntityWithCalendar("2", "2", "1", groupActivationDate);
        clientId = createClientEntity();
        associateClientToGroup(groupId, clientId);
        loanProductId = createLoanProductEntity();
    }

    private void associateClientToGroup(final Long groupId, final Long clientId) {
        groupHelper.associateClient(groupId, clientId);
        assertTrue(groupHelper.retrieveGroupMemberIds(groupId).contains(clientId), "ERROR IN GROUP MEMBER");
    }

    private void createGroupEntityWithCalendar(final String frequency, final String interval, final String repeatsOnDay,
            final String groupActivationDate) {
        groupId = groupHelper.createActiveGroup(FeignGroupHelper.DEFAULT_OFFICE_ID, groupActivationDate).getGroupId();
        assertEquals(groupId, groupHelper.retrieveGroup(groupId).getId(), "ERROR IN CREATING THE GROUP");

        groupCalendarId = CalendarHelper.createMeetingCalendarForGroup(groupId, groupActivationDate, frequency, interval, repeatsOnDay)
                .getResourceId();
    }

    private Long createClientEntity() {
        final Long createdClientId = createClient();
        assertEquals(createdClientId, clientHelper.getClient(createdClientId).getId(), "ERROR IN CREATING THE CLIENT");
        return createdClientId;
    }

    private Long createLoanProductEntity() {
        return createLoanProduct(
                new LoanProductTestBuilder().withPrincipal(LOAN_PRINCIPAL_AMOUNT).withNumberOfRepayments(NUMBER_OF_REPAYMENTS)
                        .withinterestRatePerPeriod(INTEREST_RATE_PER_PERIOD).withInterestRateFrequencyTypeAsYear().buildRequest(null));
    }
}
