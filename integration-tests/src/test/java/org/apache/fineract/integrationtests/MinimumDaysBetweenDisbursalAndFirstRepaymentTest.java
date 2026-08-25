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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.CalendarHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

/**
 * Test the creation, approval and rejection of a loan reschedule request
 **/
public class MinimumDaysBetweenDisbursalAndFirstRepaymentTest extends FeignLoanTestBase {

    private static final String LOAN_PRINCIPAL_AMOUNT = "100000.00";
    private static final String NUMBER_OF_REPAYMENTS = "12";
    private static final String INTEREST_RATE_PER_PERIOD = "18";
    private static final String GROUP_ACTIVATION_DATE = "01 August 2014";
    private static final String MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT = "7";
    private static final String DISBURSAL_DATE = "04 September 2014";

    private final FeignGroupHelper groupHelper = new FeignGroupHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignCollateralHelper collateralHelper = new FeignCollateralHelper(FineractFeignClientHelper.getFineractFeignClient());

    private Long clientId;
    private Long loanProductId;

    /*
     * MinimumDaysBetweenDisbursalAndFirstRepayment is set to 7 days and days between disbursal date and first repayment
     * is set as 7. system should allow to create this loan and allow to disburse
     */
    @Test
    public void createLoanEntity_WITH_DAY_BETWEEN_DISB_DATE_AND_REPAY_START_DATE_GREATER_THAN_MIN_DAY_CRITERIA() {
        createRequiredEntities();

        final Long loanId = applyForLoan("11 September 2014");
        assertNotNull(loanId);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanId, LoanRequestBuilders.approveLoan(Double.parseDouble(LOAN_PRINCIPAL_AMOUNT), DISBURSAL_DATE));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);

        disburseLoan(loanId, new PostLoansLoanIdRequest()//
                .actualDisbursementDate(DISBURSAL_DATE)//
                .netDisbursalAmount(getLoanDetails(loanId).getNetDisbursalAmount())//
                .note("DISBURSE NOTE")//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN));
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);
    }

    /*
     * MinimumDaysBetweenDisbursalAndFirstRepayment is set to 7 days and days between disbursal date and first repayment
     * is set as 1. system should reject the loan application
     */
    @Test
    public void createLoanEntity_WITH_DAY_BETWEEN_DISB_DATE_AND_REPAY_START_DATE_LESS_THAN_MIN_DAY_CRITERIA() {
        createRequiredEntities();

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> applyForLoan("05 September 2014"));

        assertEquals(403, exception.getStatus());
        assertErrorGlobalisationCode(exception, "error.msg.loan.days.between.first.repayment.and.disbursal.are.less.than.minimum.allowed");
    }

    private Long applyForLoan(final String firstRepaymentDate) {
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        assertNotNull(clientCollateralId);

        final PostLoansRequest application = LoanRequestBuilders
                .applyLoan(clientId, loanProductId, DISBURSAL_DATE, Double.parseDouble(LOAN_PRINCIPAL_AMOUNT),
                        Integer.parseInt(NUMBER_OF_REPAYMENTS))//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.WEEKS)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.WEEKS)//
                .interestRatePerPeriod(BigDecimal.valueOf(Double.parseDouble(INTEREST_RATE_PER_PERIOD)))//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .graceOnPrincipalPayment(2)//
                .graceOnInterestPayment(2)//
                .repaymentsStartingFromDate(firstRepaymentDate)//
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)));

        return loanHelper.applyForLoan(application).getLoanId();
    }

    /**
     * Creates the group with its meeting calendar, the client and the loan product.
     **/
    private void createRequiredEntities() {
        final Long groupId = createGroupEntityWithCalendar();
        clientId = createClientEntity();
        associateClientToGroup(groupId, clientId);
        loanProductId = createLoanProductEntity(MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT);
    }

    private void associateClientToGroup(final Long groupId, final Long clientId) {
        groupHelper.associateClient(groupId, clientId);
        assertTrue(groupHelper.retrieveGroupMemberIds(groupId).contains(clientId), "ERROR IN GROUP MEMBER");
    }

    private Long createGroupEntityWithCalendar() {
        final Long groupId = groupHelper.createActiveGroup(FeignGroupHelper.DEFAULT_OFFICE_ID, GROUP_ACTIVATION_DATE).getGroupId();
        assertEquals(groupId, groupHelper.retrieveGroup(groupId).getId(), "ERROR IN CREATING THE GROUP");

        final String weeklyFrequency = "2";
        final String everyOneWeek = "1";
        final String repeatsOnMonday = "1";
        CalendarHelper.createMeetingCalendarForGroup(groupId, GROUP_ACTIVATION_DATE, weeklyFrequency, everyOneWeek, repeatsOnMonday);
        return groupId;
    }

    private Long createClientEntity() {
        final Long createdClientId = createClient();
        assertEquals(createdClientId, clientHelper.getClient(createdClientId).getId(), "ERROR IN CREATING THE CLIENT");
        return createdClientId;
    }

    private Long createLoanProductEntity(final String minimumDaysBetweenDisbursalAndFirstRepayment) {
        return createLoanProduct(
                new LoanProductTestBuilder().withPrincipal(LOAN_PRINCIPAL_AMOUNT).withNumberOfRepayments(NUMBER_OF_REPAYMENTS)
                        .withinterestRatePerPeriod(INTEREST_RATE_PER_PERIOD).withInterestRateFrequencyTypeAsYear()
                        .withMinimumDaysBetweenDisbursalAndFirstRepayment(minimumDaysBetweenDisbursalAndFirstRepayment).buildRequest(null));
    }
}
