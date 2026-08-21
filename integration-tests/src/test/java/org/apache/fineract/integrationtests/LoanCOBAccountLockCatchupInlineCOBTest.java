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

import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanCOBCatchUpHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanAccountLockHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(1)
public class LoanCOBAccountLockCatchupInlineCOBTest extends FeignLoanTestBase {

    private static final String CUMULATIVE_STRATEGY = "mifos-standard-strategy";
    private static final String INLINE_COB_LOCK_OWNER = "LOAN_INLINE_COB_PROCESSING";
    private static final String COB_CHUNK_LOCK_OWNER = "LOAN_COB_CHUNK_PROCESSING";
    private static final String LOAN_COB_JOB = "Loan COB";

    private FeignLoanCOBCatchUpHelper loanCOBCatchUpHelper() {
        return new FeignLoanCOBCatchUpHelper(FineractFeignClientHelper.getFineractFeignClient());
    }

    @Test
    public void testCatchUpInLockedInstanceLastCOBDateIsNull() {
        FeignLoanCOBCatchUpHelper loanCOBCatchUpHelper = loanCOBCatchUpHelper();
        try {
            enableBusinessDate();
            Long loanId = createOverdueLoan();

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "02 March 2020");
            LoanAccountLockHelper.placeSoftLockOnLoanAccount(loanId, INLINE_COB_LOCK_OWNER, "Sample error");
            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), "05 March 2020");

            loanCOBCatchUpHelper.executeLoanCOBCatchUp();
            Utils.conditionalSleepWithMaxWait(30, 5, loanCOBCatchUpHelper::isLoanCOBCatchUpRunning);

            verifyLastClosedBusinessDate(loanId, "04 March 2020");
        } finally {
            disableBusinessDate();
        }
    }

    @Test
    public void testInlineCOBInLockedInstanceLastCOBDateIsNull() {
        try {
            enableBusinessDate();
            Long loanId = createOverdueLoan();

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "02 March 2020");
            LoanAccountLockHelper.placeSoftLockOnLoanAccount(loanId, COB_CHUNK_LOCK_OWNER, "Sample error");
            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), "05 March 2020");

            executeInlineCOB(List.of(loanId));
            verifyLastClosedBusinessDate(loanId, "04 March 2020");
        } finally {
            disableBusinessDate();
        }
    }

    @Test
    public void testCatchUpInLockedInstanceLastCOBDateIsNotNull() {
        FeignLoanCOBCatchUpHelper loanCOBCatchUpHelper = loanCOBCatchUpHelper();
        try {
            enableBusinessDate();
            Long loanId = createOverdueLoan();

            // update business date 2020-03-02
            updateBusinessDate(BusinessDateType.COB_DATE.name(), "02 March 2020");
            // execute inline cob for the loan
            executeInlineCOB(List.of(loanId));
            verifyLastClosedBusinessDate(loanId, "02 March 2020");

            // update business date to 2020-03-05
            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), "05 March 2020");
            // apply lock on the loan
            LoanAccountLockHelper.placeSoftLockOnLoanAccount(loanId, INLINE_COB_LOCK_OWNER, "Sample error");

            // execute catchup which sets the last cob date first 2020-03-04 and then 2020-03-05, as this loan is two
            // days behind
            loanCOBCatchUpHelper.executeLoanCOBCatchUp();
            Awaitility.await().atMost(Duration.ofMinutes(2)).with().pollInterval(Duration.ofSeconds(5)).pollDelay(Duration.ofSeconds(5))
                    .until(() -> loanCOBCatchUpHelper.isLoanCOBCatchUpFinishedFor(LocalDate.of(2020, 3, 4)));

            verifyLastClosedBusinessDate(loanId, "04 March 2020");
        } finally {
            disableBusinessDate();
        }
    }

    @Test
    public void testInlineCOBInLockedInstanceLastCOBDateIsNotNull() {
        try {
            enableBusinessDate();
            Long loanId = createOverdueLoan();

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "02 March 2020");
            executeInlineCOB(List.of(loanId));
            verifyLastClosedBusinessDate(loanId, "02 March 2020");

            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), "05 March 2020");
            LoanAccountLockHelper.placeSoftLockOnLoanAccount(loanId, COB_CHUNK_LOCK_OWNER, "Sample error");

            executeInlineCOB(List.of(loanId));
            verifyLastClosedBusinessDate(loanId, "04 March 2020");
        } finally {
            disableBusinessDate();
        }
    }

    @Test
    public void testLoanCOBNoLock() {
        try {
            enableBusinessDate();
            Long loanId = createOverdueLoan();

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "02 March 2020");
            schedulerHelper.executeAndAwaitJob(LOAN_COB_JOB);

            verifyLastClosedBusinessDate(loanId, "02 March 2020");
        } finally {
            disableBusinessDate();
        }
    }

    @Test
    public void testLoanCOBWithLoanAccountLockedWithInlineCOB() {
        try {
            enableBusinessDate();
            Long loanId = createOverdueLoan();

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "02 March 2020");
            LoanAccountLockHelper.placeSoftLockOnLoanAccount(loanId, INLINE_COB_LOCK_OWNER);

            schedulerHelper.executeAndAwaitJob(LOAN_COB_JOB);

            assertNull(getLoanDetails(loanId).getLastClosedBusinessDate());
        } finally {
            disableBusinessDate();
        }
    }

    private void enableBusinessDate() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), "02 March 2020");
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.PENALTY_WAIT_PERIOD,
                new PutGlobalConfigurationsRequest().value(0L));
    }

    private void disableBusinessDate() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(false));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.PENALTY_WAIT_PERIOD,
                new PutGlobalConfigurationsRequest().value(2L));
    }

    private Long createOverdueLoan() {
        // create client
        Long clientId = createClient();

        ChargeRequest overdueFeeCharge = ChargeRequestBuilders.loanOverdueFee(1.0)
                .chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getValue());
        Long overdueFeeChargeId = chargesHelper.createCharge(overdueFeeCharge).getResourceId();

        PostLoanProductsRequest product = create4Period1MonthLongWithoutInterestProduct(CUMULATIVE_STRATEGY).principal(15000.0)
                .charges(List.of(new LoanProductChargeData().id(overdueFeeChargeId)));
        // create loan product
        Long loanProductId = createLoanProduct(product);

        PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 March 2020", 15000.0, 4)//
                .repaymentEvery(1)//
                .loanTermFrequency(4)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS);
        Long loanId = applyForLoan(applicationRequest);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        // approve loan
        approveLoan(loanId, approveLoanRequest(15000.0, "01 March 2020"));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        // disburse loan
        disburseLoan(loanId, BigDecimal.valueOf(15000.0), "02 March 2020");
        verifyLoanStatus(loanId, LoanStatus.ACTIVE);
        return loanId;
    }
}
