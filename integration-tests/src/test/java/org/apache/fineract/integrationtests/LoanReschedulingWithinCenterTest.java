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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.ClientCollateralCreateRequest;
import org.apache.fineract.client.models.CollateralProductCreateRequest;
import org.apache.fineract.client.models.GetCentersCenterIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.client.models.PostLoansLoanIdDisbursementData;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCenterHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
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
    private static final String STAFF_JOINING_DATE = "20 September 2011";
    private static final String GROUP_ACTIVATION_DATE = "04 March 2011";
    private static final BigDecimal COLLATERAL_PCT_TO_BASE = new BigDecimal("40");
    private static final BigDecimal COLLATERAL_BASE_PRICE = new BigDecimal("100000000");
    private static final BigDecimal CLIENT_COLLATERAL_QUANTITY = new BigDecimal("100");

    private static FineractFeignClient fineractClient;
    private static FeignOfficeHelper officeHelper;
    private static FeignStaffHelper staffHelper;
    private static FeignGroupHelper groupHelper;
    private static FeignCenterHelper centerHelper;

    @BeforeAll
    public static void setupHelpers() {
        fineractClient = FineractFeignClientHelper.getFineractFeignClient();
        officeHelper = new FeignOfficeHelper(fineractClient);
        staffHelper = new FeignStaffHelper(fineractClient);
        groupHelper = new FeignGroupHelper(fineractClient);
        centerHelper = new FeignCenterHelper(fineractClient);
    }

    @Test
    public void testCenterReschedulingLoansWithInterestRecalculationEnabled() {
        Long officeId = officeHelper.createOffice(LocalDate.of(2007, 7, 1)).getResourceId();
        String name = "TestFullCreation" + new Timestamp(new java.util.Date().getTime());
        String externalId = UUID.randomUUID().toString();
        int staffId = staffHelper.createStaff(officeId, STAFF_JOINING_DATE).getResourceId().intValue();
        long groupId = groupHelper.createActiveGroup(officeId, GROUP_ACTIVATION_DATE).getResourceId();
        final String centerActivationDate = "01 July 2007";
        Long centerId = centerHelper.createCenter(name, officeId, externalId, Long.valueOf(staffId), List.of(groupId), centerActivationDate)
                .getResourceId();
        GetCentersCenterIdResponse center = centerHelper.retrieveCenter(centerId);
        assertNotNull(center);
        assertEquals(staffId, center.getStaffId().intValue());
        assertTrue(center.getActive());

        Long calendarId = createCalendarMeeting(centerId);

        Long clientId = createClient(officeId.intValue(), "01 July 2014");

        groupHelper.associateClient(groupId, clientId);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
        Calendar today = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        today.add(Calendar.DAY_OF_MONTH, -14);
        final String disbursalDate = dateFormat.format(today.getTime());
        final String recalculationRestFrequencyDate = "01 January 2012";
        final boolean isMultiTrancheLoan = false;

        Long collateralId = createCollateralProduct();
        assertNotNull(collateralId);
        Long clientCollateralId = createClientCollateral(clientId, collateralId);
        assertNotNull(clientCollateralId);

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();
        collaterals.add(collateral(clientCollateralId, BigDecimal.valueOf(1)));

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
        int staffId = staffHelper.createStaff(officeId, STAFF_JOINING_DATE).getResourceId().intValue();
        long groupId = groupHelper.createActiveGroup(officeId, GROUP_ACTIVATION_DATE).getResourceId();
        final String centerActivationDate = "01 July 2007";
        Long centerId = centerHelper.createCenter(name, officeId, externalId, Long.valueOf(staffId), List.of(groupId), centerActivationDate)
                .getResourceId();
        GetCentersCenterIdResponse center = centerHelper.retrieveCenter(centerId);
        assertNotNull(center);
        assertEquals(staffId, center.getStaffId().intValue());
        assertTrue(center.getActive());

        Long calendarId = createCalendarMeeting(centerId);

        Long clientId = createClient(officeId.intValue(), "01 July 2014");

        groupHelper.associateClient(groupId, clientId);

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
        List<PostLoansLoanIdDisbursementData> approveTranches = List.of(LoanRequestBuilders.approveTrancheDetail(disbursementDate, 5000.0),
                LoanRequestBuilders.approveTrancheDetail(secondDisbursement, 5000.0));

        Long collateralId = createCollateralProduct();
        assertNotNull(collateralId);
        Long clientCollateralId = createClientCollateral(clientId, collateralId);
        assertNotNull(clientCollateralId);

        List<PostLoansRequestCollateralData> collaterals = new ArrayList<>();
        collaterals.add(collateral(clientCollateralId, BigDecimal.valueOf(1)));

        Long loanId = applyForLoanApplicationForInterestRecalculation(clientId, groupId, calendarId, loanProductId, disbursementDate,
                recalculationRestFrequencyDate, LoanApplicationTestBuilder.RBI_INDIA_STRATEGY, createTranches, collaterals);

        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        approveLoan(loanId, LoanRequestBuilders.approveLoanWithTranches(10000.0, approveDate, expectedDisbursementDate, approveTranches));
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
    /**
     * The server only reads collateral for individual accounts (LoanApplicationValidator guards on
     * {@code loanType.isIndividualAccount()}), so for the jlg loans here it is accepted and ignored.
     */
    private PostLoansRequestCollateralData collateral(Long collateralId, BigDecimal quantity) {
        return new PostLoansRequestCollateralData().clientCollateralId(collateralId).quantity(quantity);
    }

    private static Long createCollateralProduct() {
        CollateralProductCreateRequest request = new CollateralProductCreateRequest()//
                .name(Utils.randomStringGenerator("COLLATERAL_PRODUCT", 5))//
                .currency("USD")//
                .unitType("acre")//
                .quality("agriculture")//
                .pctToBase(COLLATERAL_PCT_TO_BASE)//
                .basePrice(COLLATERAL_BASE_PRICE)//
                .locale(LoanTestData.LOCALE);
        return FeignCalls.ok(() -> fineractClient.collateralManagement().createCollateral1(request)).getResourceId();
    }

    private static Long createClientCollateral(Long clientId, Long collateralId) {
        ClientCollateralCreateRequest request = new ClientCollateralCreateRequest()//
                .collateralId(collateralId)//
                .quantity(CLIENT_COLLATERAL_QUANTITY)//
                .locale(LoanTestData.LOCALE);
        return FeignCalls.ok(() -> fineractClient.clientCollateralManagement().addClientCollateral(clientId, request)).getResourceId();
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
        final PostLoanProductsRequest loanProductRequest = new LoanProductTestBuilder().withPrincipal("10000.00")
                .withNumberOfRepayments("12").withRepaymentAfterEvery("2").withRepaymentTypeAsWeek().withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths().withTranches(isMultiTrancheLoan)
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withRepaymentStrategy(repaymentStrategy)
                .withInterestTypeAsDecliningBalance()
                .withInterestRecalculationDetails(interestRecalculationCompoundingMethod, rescheduleStrategyMethod,
                        preCloseInterestCalculationStrategy)
                .withInterestRecalculationRestFrequencyDetails(recalculationRestFrequencyType, recalculationRestFrequencyInterval, null,
                        null)
                .withInterestRecalculationCompoundingFrequencyDetails(null, null, null, null).buildRequest(null);
        return createLoanProduct(loanProductRequest);
    }

    private Long applyForLoanApplicationForInterestRecalculation(final Long clientId, Long groupId, Long calendarId,
            final Long loanProductId, final String disbursementDate, final String restStartDate, final String repaymentStrategy,
            List<PostLoansRequestCollateralData> collaterals) {
        return applyForLoanApplicationForInterestRecalculation(clientId, groupId, calendarId, loanProductId, disbursementDate,
                restStartDate, repaymentStrategy, null, collaterals);
    }

    private Long applyForLoanApplicationForInterestRecalculation(final Long clientId, Long groupId, Long calendarId,
            final Long loanProductId, final String disbursementDate, final String restStartDate, final String repaymentStrategy,
            List<PostLoansDisbursementData> tranches, List<PostLoansRequestCollateralData> collaterals) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoan(new PostLoansRequest()//
                .clientId(clientId)//
                .groupId(groupId)//
                .productId(loanProductId)//
                .principal(new BigDecimal("10000.00"))//
                .loanTermFrequency(24)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.WEEKS)//
                .numberOfRepayments(12)//
                .repaymentEvery(2)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.WEEKS)//
                .interestRatePerPeriod(new BigDecimal("2"))//
                .loanType("jlg")//
                .calendarId(calendarId)//
                .syncDisbursementWithMeeting(false)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .disbursementData(tranches)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .expectedDisbursementDate(disbursementDate)//
                .submittedOnDate(disbursementDate)//
                .transactionProcessingStrategyCode(repaymentStrategy)//
                .collateral(collaterals)//
                .charges(List.of())//
                .maxOutstandingLoanBalance(new BigDecimal("36000"))//
                .locale("en_GB")//
                .dateFormat(LoanTestData.DATETIME_PATTERN));
    }

    private static LocalDate toLocalDate(Calendar calendar) {
        return LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }
}
