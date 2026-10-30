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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.LoanAccountLock;
import org.apache.fineract.client.models.PostGlClosuresRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(1)
/**
 * A business step that fails for one loan must not stop the other loans of its chunk. Here the periodic accrual of one
 * loan cannot be posted (the books are closed on the COB date); a loan without accounting in the same chunk must still
 * be closed for that date, and the failed loan's lock must carry the error so that it is retried.
 */
public class LoanCobBusinessStepFailureTest extends FeignLoanTestBase {

    private static final String STRATEGY = "mifos-standard-strategy";
    private static final String CHARGE_ACCRUAL_DATE_SUBMITTED = "submitted-date";
    private static final String CHARGE_ACCRUAL_DATE_DUE = "due-date";

    @Test
    public void oneLoansFailingStepDoesNotStopTheRestOfTheChunk() {
        final LocalDate cobDate = LocalDate.of(2023, 3, 3);
        try {
            enableSubmittedDateChargeAccrual(cobDate);
            final Long clientId = createClient();
            // posts a charge accrual dated the COB date, which the closure below refuses
            final Long failingLoanId = createSingleRepaymentLoan(clientId, createLoanProduct(singleRepaymentAccrualProduct()),
                    UUID.randomUUID().toString());
            addPenaltyCharge(failingLoanId, "10 March 2023", 10.0);
            addFeeCharge(failingLoanId, "14 March 2023", 10.0);
            // no accounting: nothing to post, so the closure does not concern it
            final Long healthyLoanId = createSingleRepaymentLoan(clientId,
                    createLoanProduct(singleRepaymentAccrualProduct().accountingRule(1)), UUID.randomUUID().toString());

            updateBusinessDate(Utils.dateFormatter.format(cobDate.plusDays(1)));
            final Long closureId = fineractClient().accountingClosure().createGLClosure(new PostGlClosuresRequest().officeId(1L)
                    .closingDate(cobDate).dateFormat("yyyy-MM-dd").locale("en").comments("closed books on the COB date")).getResourceId();
            try {
                schedulerHelper.executeAndAwaitJob("Loan COB");
            } finally {
                fineractClient().accountingClosure().deleteGLClosure(closureId);
            }

            assertNull(getLoanDetails(failingLoanId).getLastClosedBusinessDate(), "the loan whose accrual failed is not closed");
            final List<LoanAccountLock> locks = fineractClient().loanAccountLock().retrieveLockedAccounts(0, 1000).getContent();
            final LoanAccountLock failedLock = locks.stream().filter(lock -> failingLoanId.equals(lock.getLoanId())).findFirst()
                    .orElseThrow(() -> new AssertionError("the loan whose accrual failed keeps its lock"));
            assertNotNull(failedLock.getError(), "the lock of the loan whose accrual failed records the error, so it can be retried");
            assertTrue(locks.stream().noneMatch(lock -> healthyLoanId.equals(lock.getLoanId())), "the closed loan's lock is released");
            assertEquals(cobDate, getLoanDetails(healthyLoanId).getLastClosedBusinessDate(),
                    "a loan of the same chunk with nothing to post must still be closed for the COB date");
        } finally {
            resetChargeAccrualConfig();
        }
    }

    private void enableSubmittedDateChargeAccrual(LocalDate businessDate) {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        updateBusinessDate(Utils.dateFormatter.format(businessDate));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                new PutGlobalConfigurationsRequest().stringValue(CHARGE_ACCRUAL_DATE_SUBMITTED));
    }

    private void resetChargeAccrualConfig() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(false));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                new PutGlobalConfigurationsRequest().stringValue(CHARGE_ACCRUAL_DATE_DUE));
    }

    private void addPenaltyCharge(Long loanId, String dueDate, double amount) {
        Long chargeId = chargesHelper.createLoanSpecifiedDueDatePenalty(amount).getResourceId();
        assertNotNull(addChargesForLoan(loanId, loanCharge(chargeId, dueDate, amount)).getResourceId());
    }

    private void addFeeCharge(Long loanId, String dueDate, double amount) {
        Long chargeId = chargesHelper.createLoanSpecifiedDueDateCharge(amount).getResourceId();
        assertNotNull(addChargesForLoan(loanId, loanCharge(chargeId, dueDate, amount)).getResourceId());
    }

    private PostLoansLoanIdChargesRequest loanCharge(Long chargeId, String dueDate, double amount) {
        return new PostLoansLoanIdChargesRequest().chargeId(chargeId).dateFormat(DATETIME_PATTERN).locale("en").amount(amount)
                .dueDate(dueDate);
    }

    private PostLoanProductsRequest singleRepaymentAccrualProduct() {
        return create4Period1MonthLongWithoutInterestProduct(STRATEGY).numberOfRepayments(1).maxNumberOfRepayments(30);
    }

    private Long createSingleRepaymentLoan(Long clientId, Long loanProductId, String externalId) {
        Long loanId = applyForLoan(loanRequest(clientId, loanProductId, externalId, 1, LoanTestData.RepaymentFrequencyType.MONTHS, 1, 1,
                LoanTestData.RepaymentFrequencyType.MONTHS, LoanTestData.InterestType.FLAT, "03 March 2023"));
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "03 March 2023"));
        disburseLoanWithNetDisbursalAmount(loanId, "03 March 2023", "1000");
        return loanId;
    }

    private PostLoansRequest loanRequest(Long clientId, Long loanProductId, String externalId, int loanTermFrequency,
            Integer loanTermFrequencyType, int numberOfRepayments, int repaymentEvery, Integer repaymentFrequencyType, Integer interestType,
            String date) {
        return new PostLoansRequest().clientId(clientId).productId(loanProductId).externalId(externalId).principal(new BigDecimal("1000"))
                .loanTermFrequency(loanTermFrequency).loanTermFrequencyType(loanTermFrequencyType).numberOfRepayments(numberOfRepayments)
                .repaymentEvery(repaymentEvery).repaymentFrequencyType(repaymentFrequencyType).interestRatePerPeriod(BigDecimal.ZERO)
                .interestType(interestType)
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL).expectedDisbursementDate(date).submittedOnDate(date)
                .transactionProcessingStrategyCode(STRATEGY).loanType("individual").dateFormat(DATETIME_PATTERN).locale("en");
    }
}
