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

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupCenterHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.CalendarHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanReschedulingWithinCenterTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(LoanReschedulingWithinCenterTest.class);
    private static final Long LEGAL_FORM_PERSON = 1L;

    private static FeignOfficeHelper officeHelper;

    @BeforeAll
    public static void setupOfficeHelper() {
        officeHelper = new FeignOfficeHelper(FineractFeignClientHelper.getFineractFeignClient());
    }

    @Test
    public void testCenterReschedulingLoansWithInterestRecalculationEnabled() {
        Long officeId = officeHelper.createOffice(LocalDate.of(2007, 7, 1)).getResourceId();
        String name = "TestFullCreation" + new Timestamp(new java.util.Date().getTime());
        String externalId = UUID.randomUUID().toString();
        int staffId = FeignGroupCenterHelper.createStaff(officeId.intValue()).intValue();
        long groupId = FeignGroupCenterHelper.createGroup(officeId.intValue());
        final String centerActivationDate = "01 July 2007";
        Long centerId = FeignGroupCenterHelper.createCenter(name, officeId.intValue(), externalId, staffId, new long[] { groupId },
                centerActivationDate);
        JsonObject center = FeignGroupCenterHelper.retrieveCenter(centerId);
        assertNotNull(center);
        assertEquals(staffId, center.get("staffId").getAsInt());
        assertTrue(center.get("active").getAsBoolean());

        Long calendarId = createCalendarMeeting(centerId);

        Long clientId = createClient(officeId.intValue(), "01 July 2014");

        FeignGroupCenterHelper.associateClientToGroup(groupId, clientId);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
        Calendar today = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        today.add(Calendar.DAY_OF_MONTH, -14);
        final String disbursalDate = dateFormat.format(today.getTime());
        final String recalculationRestFrequencyDate = "01 January 2012";
        final boolean isMultiTrancheLoan = false;

        Long collateralId = FeignGroupCenterHelper.createCollateralProduct();
        assertNotNull(collateralId);
        Long clientCollateralId = FeignGroupCenterHelper.createClientCollateral(clientId, collateralId);
        assertNotNull(clientCollateralId);

        List<HashMap> collaterals = new ArrayList<>();
        collaterals.add(collateral(clientCollateralId.intValue(), BigDecimal.valueOf(1)));

        Long loanProductId = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.RBI_INDIA_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "0", recalculationRestFrequencyDate,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, isMultiTrancheLoan);

        Long loanId = applyForLoanApplicationForInterestRecalculation(clientId, groupId, calendarId, loanProductId, disbursalDate,
                recalculationRestFrequencyDate, LoanApplicationTestBuilder.RBI_INDIA_STRATEGY, collaterals);

        assertNotNull(loanId);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanId, approveLoanRequest(10000.0, disbursalDate));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);

        GetLoansLoanIdResponse approvedLoan = getLoanDetails(loanId);
        disburseLoanWithNetDisbursalAmount(loanId, disbursalDate, approvedLoan.getNetDisbursalAmount().toPlainString());
        verifyLoanStatus(loanId, LoanStatus.ACTIVE);

        LOG.info("---------------------------------CHANGING GROUP MEETING DATE ------------------------------------------");
        Calendar rescheduledDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        rescheduledDate.add(Calendar.DAY_OF_MONTH, 14);
        String oldMeetingDate = dateFormat.format(rescheduledDate.getTime());
        rescheduledDate.add(Calendar.DAY_OF_MONTH, 1);
        final String centerMeetingNewStartDate = dateFormat.format(rescheduledDate.getTime());
        CalendarHelper.updateMeetingCalendarForCenter(centerId, calendarId.toString(), oldMeetingDate, centerMeetingNewStartDate);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        GetLoansLoanIdRepaymentPeriod installment = loanDetails.getRepaymentSchedule().getPeriods().get(2);
        assertEquals(toLocalDate(rescheduledDate), installment.getDueDate());
        assertEquals(0, new BigDecimal("90.82").compareTo(installment.getInterestDue()));
    }

    @Test
    public void testCenterReschedulingMultiTrancheLoansWithInterestRecalculationEnabled() {
        Long officeId = officeHelper.createOffice(LocalDate.of(2007, 7, 1)).getResourceId();
        String name = "TestFullCreation" + new Timestamp(new java.util.Date().getTime());
        String externalId = UUID.randomUUID().toString();
        int staffId = FeignGroupCenterHelper.createStaff(officeId.intValue()).intValue();
        long groupId = FeignGroupCenterHelper.createGroup(officeId.intValue());
        final String centerActivationDate = "01 July 2007";
        Long centerId = FeignGroupCenterHelper.createCenter(name, officeId.intValue(), externalId, staffId, new long[] { groupId },
                centerActivationDate);
        JsonObject center = FeignGroupCenterHelper.retrieveCenter(centerId);
        assertNotNull(center);
        assertEquals(staffId, center.get("staffId").getAsInt());
        assertTrue(center.get("active").getAsBoolean());

        Long calendarId = createCalendarMeeting(centerId);

        Long clientId = createClient(officeId.intValue(), "01 July 2014");

        FeignGroupCenterHelper.associateClientToGroup(groupId, clientId);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
        Calendar today = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        today.add(Calendar.DAY_OF_MONTH, -14);
        final String approveDate = dateFormat.format(today.getTime());
        final String expectedDisbursementDate = dateFormat.format(today.getTime());
        final String disbursementDate = dateFormat.format(today.getTime());
        final String recalculationRestFrequencyDate = "01 January 2012";
        final boolean isMultiTrancheLoan = true;

        Long loanProductId = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.RBI_INDIA_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "0", recalculationRestFrequencyDate,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, isMultiTrancheLoan);

        Calendar secondTrancheDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        secondTrancheDate.add(Calendar.DAY_OF_MONTH, -7);
        String secondDisbursement = dateFormat.format(secondTrancheDate.getTime());

        List<PostLoansDisbursementData> createTranches = List.of(LoanRequestBuilders.applyTrancheDetail(disbursementDate, 5000.0),
                LoanRequestBuilders.applyTrancheDetail(secondDisbursement, 5000.0));
        List<PostLoansDisbursementData> approveTranches = List.of(LoanRequestBuilders.applyTrancheDetail(disbursementDate, 5000.0),
                LoanRequestBuilders.applyTrancheDetail(secondDisbursement, 5000.0));

        Long collateralId = FeignGroupCenterHelper.createCollateralProduct();
        assertNotNull(collateralId);
        Long clientCollateralId = FeignGroupCenterHelper.createClientCollateral(clientId, collateralId);
        assertNotNull(clientCollateralId);

        List<HashMap> collaterals = new ArrayList<>();
        collaterals.add(collateral(clientCollateralId.intValue(), BigDecimal.valueOf(1)));

        Long loanId = applyForLoanApplicationForInterestRecalculation(clientId, groupId, calendarId, loanProductId, disbursementDate,
                recalculationRestFrequencyDate, LoanApplicationTestBuilder.RBI_INDIA_STRATEGY, createTranches, collaterals);

        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        approveLoanFromJson(loanId,
                LoanRequestBuilders.approveLoanWithTranchesJson(10000.0, approveDate, expectedDisbursementDate, approveTranches));
        GetLoansLoanIdResponse approvedLoan = getLoanDetails(loanId);
        verifyLoanStatus(approvedLoan, LoanStatus.APPROVED);
        verifyLoanStatus(approvedLoan, status -> Boolean.TRUE.equals(status.getWaitingForDisbursal()));

        disburseLoanWithNetDisbursalAmount(loanId, disbursementDate, "5000");

        LOG.info("---------------------------------CHANGING GROUP MEETING DATE ------------------------------------------");
        Calendar rescheduledDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        rescheduledDate.add(Calendar.DAY_OF_MONTH, 14);
        String oldMeetingDate = dateFormat.format(rescheduledDate.getTime());
        rescheduledDate.add(Calendar.DAY_OF_MONTH, 1);
        final String centerMeetingNewStartDate = dateFormat.format(rescheduledDate.getTime());
        CalendarHelper.updateMeetingCalendarForCenter(centerId, calendarId.toString(), oldMeetingDate, centerMeetingNewStartDate);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        GetLoansLoanIdRepaymentPeriod installment = loanDetails.getRepaymentSchedule().getPeriods().get(3);
        assertEquals(toLocalDate(rescheduledDate), installment.getDueDate());
        assertEquals(0, new BigDecimal("41.05").compareTo(installment.getInterestDue()));

        disburseLoanWithNetDisbursalAmount(loanId, secondDisbursement, "5000");
    }

    private Long createClient(int officeId, String activationDate) {
        PostClientsRequest request = new PostClientsRequest()//
                .officeId((long) officeId)//
                .legalFormId(LEGAL_FORM_PERSON)//
                .firstname(Utils.randomFirstNameGenerator())//
                .lastname(Utils.randomLastNameGenerator())//
                .externalId(Utils.randomStringGenerator("EXT_", 7))//
                .active(true)//
                .activationDate(activationDate)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
        return clientHelper.createClient(request).getClientId();
    }

    @SuppressWarnings("rawtypes")
    private HashMap collateral(Integer collateralId, BigDecimal amount) {
        HashMap collateral = new HashMap(2);
        collateral.put("clientCollateralId", collateralId.toString());
        collateral.put("amount", amount.toString());
        return collateral;
    }

    private Long createCalendarMeeting(Long centerId) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
        Calendar today = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        final String startDate = dateFormat.format(today.getTime());
        final String frequency = "2";
        final String interval = "2";
        Integer repeatsOnDay = today.get(Calendar.DAY_OF_WEEK) - 1;

        if (repeatsOnDay.intValue() == 0) {
            repeatsOnDay = 7;
        }

        Long calendarId = CalendarHelper.createMeetingForGroup(centerId, startDate, frequency, interval, repeatsOnDay.toString())
                .getResourceId();
        LOG.info("calendarId {}", calendarId);
        return calendarId;
    }

    private Long createLoanProductWithInterestRecalculation(final String repaymentStrategy,
            final String interestRecalculationCompoundingMethod, final String rescheduleStrategyMethod,
            final String recalculationRestFrequencyType, final String recalculationRestFrequencyInterval,
            final String recalculationRestFrequencyDate, final String preCloseInterestCalculationStrategy,
            final boolean isMultiTrancheLoan) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("10000.00").withNumberOfRepayments("12")
                .withRepaymentAfterEvery("2").withRepaymentTypeAsWeek().withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths().withTranches(isMultiTrancheLoan)
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withRepaymentStrategy(repaymentStrategy)
                .withInterestTypeAsDecliningBalance()
                .withInterestRecalculationDetails(interestRecalculationCompoundingMethod, rescheduleStrategyMethod,
                        preCloseInterestCalculationStrategy)
                .withInterestRecalculationRestFrequencyDetails(recalculationRestFrequencyType, recalculationRestFrequencyInterval, null,
                        null)
                .withInterestRecalculationCompoundingFrequencyDetails(null, null, null, null).build(null);
        return createLoanProductFromJson(loanProductJSON);
    }

    private Long applyForLoanApplicationForInterestRecalculation(final Long clientId, Long groupId, Long calendarId,
            final Long loanProductId, final String disbursementDate, final String restStartDate, final String repaymentStrategy,
            List<HashMap> collaterals) {
        return applyForLoanApplicationForInterestRecalculation(clientId, groupId, calendarId, loanProductId, disbursementDate,
                restStartDate, repaymentStrategy, null, collaterals);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Long applyForLoanApplicationForInterestRecalculation(final Long clientId, Long groupId, Long calendarId,
            final Long loanProductId, final String disbursementDate, final String restStartDate, final String repaymentStrategy,
            List<PostLoansDisbursementData> tranches, List<HashMap> collaterals) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        List<HashMap> trancheMaps = null;
        if (tranches != null) {
            trancheMaps = tranches.stream().map(tranche -> {
                HashMap map = new HashMap();
                map.put("expectedDisbursementDate", tranche.getExpectedDisbursementDate());
                map.put("principal", tranche.getPrincipal().toPlainString());
                return map;
            }).toList();
        }
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
                .withTranches(trancheMaps).withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeAsDays() //
                .withExpectedDisbursementDate(disbursementDate) //
                .withSubmittedOnDate(disbursementDate) //
                .withRepaymentStrategy(repaymentStrategy) //
                .withCollaterals(collaterals).withCharges(new ArrayList<>())//
                .build(clientId.toString(), groupId.toString(), loanProductId.toString(), null);
        return applyForLoanFromJson(loanApplicationJSON);
    }

    private static LocalDate toLocalDate(Calendar calendar) {
        return LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }
}
