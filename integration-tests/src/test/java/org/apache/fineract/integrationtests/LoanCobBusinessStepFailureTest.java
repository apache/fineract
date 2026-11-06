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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.fineract.client.models.LoanAccountLock;
import org.apache.fineract.client.models.PostGlClosuresRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanCOBCatchUpHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

/**
 * One loan whose COB business step fails must not take the rest of its chunk down with it (FINERACT-3004).
 * <p>
 * Two loans of one chunk: the first posts an accrual journal entry on the COB date, which a GL closure on that date
 * rejects; the second has no accounting, so it posts nothing and is not concerned by the closure. The Loan COB job must
 * complete, close the second loan, and leave the first locked with its error, so that a later run can take it over.
 * <p>
 * The scenario - a GL closure on the COB date failing one loan's accrual, beside a loan with no accounting in the same
 * chunk - is Piotr Wargulak's, from his {@code LoanCobBusinessStepFailureTest} in apache/fineract#6753.
 */
public class LoanCobBusinessStepFailureTest extends FeignLoanTestBase {

    private static final String STRATEGY = "mifos-standard-strategy";
    private static final LocalDate COB_DATE = LocalDate.of(2023, 3, 3);
    private static final Long HEAD_OFFICE_ID = 1L;
    private static final int ACCOUNTING_NONE = 1;

    @Test
    public void aFailingLoanIsSkippedAndTheRestOfItsChunkIsClosed() {
        String previousChargeAccrualDate = globalConfigurationHelper
                .retrieveConfigurationByName(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE).getStringValue();
        try {
            setChargeAccrualDate("submitted-date");
            runAt("03 March 2023", this::runCobWithOneFailingLoan);
        } finally {
            setChargeAccrualDate(previousChargeAccrualDate);
        }
    }

    private void runCobWithOneFailingLoan() {
        final Long clientId = createClient();
        final Long failingLoanId = createSingleRepaymentLoan(clientId, createLoanProduct(accrualProduct()));
        addFeeCharge(failingLoanId, "14 March 2023");
        final Long healthyLoanId = createSingleRepaymentLoan(clientId, createLoanProduct(accrualProduct().accountingRule(ACCOUNTING_NONE)));
        final Long closureId = ok(() -> fineractClient().accountingClosure().createGLClosure(new PostGlClosuresRequest()
                .officeId(HEAD_OFFICE_ID).closingDate(COB_DATE).dateFormat("yyyy-MM-dd").locale("en").comments("FINERACT-3004")))
                .getResourceId();
        boolean closureDeleted = false;
        try {
            updateBusinessDate(Utils.dateFormatter.format(COB_DATE.plusDays(1)));
            // Fails the test unless the job run is recorded as a success.
            schedulerHelper.executeAndAwaitJob("Loan COB");

            assertThat(getLoanDetails(healthyLoanId).getLastClosedBusinessDate()).as("the healthy loan of the chunk must be closed")
                    .isEqualTo(COB_DATE);
            assertThat(lockOf(healthyLoanId)).as("the healthy loan must be unlocked").isEmpty();
            assertThat(getLoanDetails(failingLoanId).getLastClosedBusinessDate()).as("the failing loan must not be closed")
                    .isNotEqualTo(COB_DATE);
            LoanAccountLock lock = lockOf(failingLoanId).orElseThrow(() -> new AssertionError("the failing loan must stay locked"));
            assertThat(lock.getError()).isEqualTo("Loan (id: " + failingLoanId + ") writing is failed");
            assertThat(lock.getStacktrace()).as("the lock must carry the business step's own failure")
                    .contains("JournalEntryInvalidException");

            executeVoid(() -> fineractClient().accountingClosure().deleteGLClosure(closureId));
            closureDeleted = true;
            runCobCatchUp();

            assertThat(getLoanDetails(failingLoanId).getLastClosedBusinessDate()).as("a later run must take the failing loan over")
                    .isEqualTo(COB_DATE);
            assertThat(lockOf(failingLoanId)).as("and release its lock").isEmpty();
        } finally {
            if (!closureDeleted) {
                executeVoid(() -> fineractClient().accountingClosure().deleteGLClosure(closureId));
                // Catch-up takes over the error lock left by the failed run, so nothing leaks into later classes.
                runCobCatchUp();
            }
        }
    }

    private void runCobCatchUp() {
        FeignLoanCOBCatchUpHelper catchUpHelper = new FeignLoanCOBCatchUpHelper(fineractClient());
        catchUpHelper.executeLoanCOBCatchUp();
        Awaitility.await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(1))
                .until(() -> !catchUpHelper.isLoanCOBCatchUpRunning());
    }

    private Optional<LoanAccountLock> lockOf(Long loanId) {
        List<LoanAccountLock> locks = ok(() -> fineractClient().loanAccountLock().retrieveLockedAccounts(0, 1000)).getContent();
        return locks == null ? Optional.empty() : locks.stream().filter(lock -> loanId.equals(lock.getLoanId())).findFirst();
    }

    private void setChargeAccrualDate(String value) {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                new PutGlobalConfigurationsRequest().stringValue(value));
    }

    private void addFeeCharge(Long loanId, String dueDate) {
        Long chargeId = chargesHelper.createLoanSpecifiedDueDateCharge(10.0).getResourceId();
        addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().chargeId(chargeId).dateFormat(DATETIME_PATTERN).locale("en")
                .amount(10.0).dueDate(dueDate));
    }

    private PostLoanProductsRequest accrualProduct() {
        return create4Period1MonthLongWithoutInterestProduct(STRATEGY).numberOfRepayments(1).maxNumberOfRepayments(30);
    }

    private Long createSingleRepaymentLoan(Long clientId, Long loanProductId) {
        Long loanId = applyForLoan(new PostLoansRequest().clientId(clientId).productId(loanProductId)
                .externalId(UUID.randomUUID().toString()).principal(new BigDecimal("1000")).loanTermFrequency(1)
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS).numberOfRepayments(1).repaymentEvery(1)
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS).interestRatePerPeriod(BigDecimal.ZERO)
                .interestType(LoanTestData.InterestType.FLAT)
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL).expectedDisbursementDate("03 March 2023")
                .submittedOnDate("03 March 2023").transactionProcessingStrategyCode(STRATEGY).loanType("individual")
                .dateFormat(DATETIME_PATTERN).locale("en"));
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "03 March 2023"));
        disburseLoanWithNetDisbursalAmount(loanId, "03 March 2023", "1000");
        return loanId;
    }
}
