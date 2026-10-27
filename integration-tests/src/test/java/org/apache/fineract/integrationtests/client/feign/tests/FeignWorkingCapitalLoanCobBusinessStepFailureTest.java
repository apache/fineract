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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.PostGlClosuresRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest.AccountingRuleEnum;
import org.apache.fineract.client.models.WorkingCapitalLoanAccountLock;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.Test;

/**
 * A business step that fails for one Working Capital loan must not stop the other loans of its chunk. Here the discount
 * fee amortization of one loan cannot be posted (the books are closed on the COB date); a loan without accounting in
 * the same chunk must still be closed for that date, and the failed loan's lock must carry the error so that it is
 * retried.
 */
public class FeignWorkingCapitalLoanCobBusinessStepFailureTest extends FeignIntegrationTest {

    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(9000);
    private static final BigDecimal DISCOUNT = BigDecimal.valueOf(1000);

    @Test
    void oneLoansFailingStepDoesNotStopTheRestOfTheChunk() {
        final var feign = fineractClient();
        final FeignWorkingCapitalLoanHelper wcLoanHelper = new FeignWorkingCapitalLoanHelper(feign);
        final FeignClientHelper clientHelper = new FeignClientHelper(feign);
        final FeignBusinessDateHelper businessDateHelper = new FeignBusinessDateHelper(feign);
        final FeignAccountHelper a = new FeignAccountHelper(feign);
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long productId = new WorkingCapitalLoanProductHelper()
                    .createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder()
                            .withName("WCL StepFail " + Utils.uniqueRandomStringGenerator("", 8))
                            .withShortName(Utils.uniqueRandomStringGenerator("", 4)).withRepaymentEvery(1)
                            .withRepaymentFrequencyType("DAYS").withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE))
                            .withAccountingRule(AccountingRuleEnum.ACC_DEF_REV_AM)
                            .withFundSourceAccountId(a.createLiabilityAccount("sfFund").getAccountID().longValue())
                            .withLoanPortfolioAccountId(a.createAssetAccount("sfPort").getAccountID().longValue())
                            .withTransfersInSuspenseAccountId(a.createAssetAccount("sfSusp").getAccountID().longValue())
                            .withIncomeFromDiscountFeeAccountId(a.createIncomeAccount("sfDisc").getAccountID().longValue())
                            .withReceivableFeeAccountId(a.createAssetAccount("sfFeeR").getAccountID().longValue())
                            .withReceivablePenaltyAccountId(a.createAssetAccount("sfPenR").getAccountID().longValue())
                            .withIncomeFromFeeAccountId(a.createIncomeAccount("sfFee").getAccountID().longValue())
                            .withIncomeFromPenaltyAccountId(a.createIncomeAccount("sfPen").getAccountID().longValue())
                            .withIncomeFromRecoveryAccountId(a.createIncomeAccount("sfRec").getAccountID().longValue())
                            .withWriteOffAccountId(a.createExpenseAccount("sfWo").getAccountID().longValue())
                            .withOverpaymentLiabilityAccountId(a.createLiabilityAccount("sfOver").getAccountID().longValue())
                            .withDeferredIncomeLiabilityAccountId(a.createLiabilityAccount("sfDef").getAccountID().longValue()).build())
                    .getResourceId();
            final Long clientId = clientHelper.createClient("01 January 2026");
            // no accounting: nothing to post, so the closure does not concern it
            final Long healthyProductId = new WorkingCapitalLoanProductHelper().createWorkingCapitalLoanProduct(
                    new WorkingCapitalLoanProductTestBuilder().withName("WCL Healthy " + Utils.uniqueRandomStringGenerator("", 8))
                            .withShortName(Utils.uniqueRandomStringGenerator("", 4)).build())
                    .getResourceId();
            final Long healthyLoanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientId,
                    healthyProductId, PRINCIPAL, WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT,
                    "01 January 2026", "01 January 2026"));
            wcLoanHelper.approve(healthyLoanId, WorkingCapitalLoanRequestBuilders.approve("01 January 2026", PRINCIPAL, "01 January 2026"));
            wcLoanHelper.disburse(healthyLoanId, WorkingCapitalLoanRequestBuilders.disburse("01 January 2026", PRINCIPAL));
            // amortizes the discount on the COB date, which the closure below refuses
            final Long failingLoanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders
                    .submitApplication(clientId, productId, PRINCIPAL,
                            WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT, "01 January 2026", "01 January 2026")
                    .discount(DISCOUNT));
            wcLoanHelper.approve(failingLoanId,
                    WorkingCapitalLoanRequestBuilders.approveWithDiscount("01 January 2026", PRINCIPAL, "01 January 2026", DISCOUNT));
            wcLoanHelper.disburse(failingLoanId,
                    WorkingCapitalLoanRequestBuilders.disburseWithDiscount("01 January 2026", PRINCIPAL, DISCOUNT));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            wcLoanHelper.makeRepayment(failingLoanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(50), "02 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-03");

            final Long closureId = feign.accountingClosure().createGLClosure(new PostGlClosuresRequest().officeId(1L)
                    .closingDate(LocalDate.of(2026, 1, 2)).dateFormat("yyyy-MM-dd").locale("en").comments("closed books on the COB date"))
                    .getResourceId();
            try {
                SchedulerJobHelper.executeAndAwaitJob("Working Capital Loan COB");
            } finally {
                feign.accountingClosure().deleteGLClosure(closureId);
            }
            assertNull(wcLoanHelper.getLoanDetails(failingLoanId).getLastClosedBusinessDate(),
                    "the loan whose amortization failed is not closed");
            assertEquals(LocalDate.of(2026, 1, 2), wcLoanHelper.getLoanDetails(healthyLoanId).getLastClosedBusinessDate(),
                    "a loan of the same chunk with nothing to post must still be closed for the COB date");

            final List<WorkingCapitalLoanAccountLock> locks = feign.workingCapitalLoanAccountLock()
                    .retrieveLockedWorkingCapitalLoanAccounts();
            final WorkingCapitalLoanAccountLock failedLock = locks.stream().filter(lock -> failingLoanId.equals(lock.getLoanId()))
                    .findFirst().orElseThrow(() -> new AssertionError("the loan whose amortization failed keeps its lock"));
            assertNotNull(failedLock.getError(), "the lock of the loan whose amortization failed records the error, so it can be retried");
            assertTrue(locks.stream().noneMatch(lock -> healthyLoanId.equals(lock.getLoanId())), "the closed loan's lock is released");
        });
    }
}
