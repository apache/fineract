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

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.models.BatchResponse;
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
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(1)
public class LoanCatchUpIntegrationTest extends FeignLoanTestBase {

    private static final String CUMULATIVE_STRATEGY = "mifos-standard-strategy";
    private static final String INLINE_COB_LOCK_OWNER = "LOAN_INLINE_COB_PROCESSING";
    private static final Long REPAYMENT_BATCH_REQUEST_ID = 4730L;

    @Test
    public void testCatchUpInLockedInstance() {
        FeignLoanCOBCatchUpHelper loanCOBCatchUpHelper = new FeignLoanCOBCatchUpHelper(FineractFeignClientHelper.getFineractFeignClient());
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), "02 March 2020");
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.PENALTY_WAIT_PERIOD,
                    new PutGlobalConfigurationsRequest().value(0L));

            Long clientId = createClient();

            ChargeRequest overdueFeeCharge = ChargeRequestBuilders.loanOverdueFee(1.0)
                    .chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getValue());
            Long overdueFeeChargeId = chargesHelper.createCharge(overdueFeeCharge).getResourceId();

            PostLoanProductsRequest product = create4Period1MonthLongWithoutInterestProduct(CUMULATIVE_STRATEGY).principal(15000.0)
                    .charges(List.of(new LoanProductChargeData().id(overdueFeeChargeId)));
            Long loanProductId = createLoanProduct(product);

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 March 2020", 15000.0, 4)//
                    .repaymentEvery(1)//
                    .loanTermFrequency(4)//
                    .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                    .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS);
            Long loanId = applyForLoan(applicationRequest);
            verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
            approveLoan(loanId, approveLoanRequest(15000.0, "01 March 2020"));
            verifyLoanStatus(loanId, LoanStatus.APPROVED);
            disburseLoan(loanId, BigDecimal.valueOf(15000.0), "02 March 2020");
            verifyLoanStatus(loanId, LoanStatus.ACTIVE);

            updateBusinessDate(BusinessDateType.COB_DATE.name(), "02 March 2020");
            LoanAccountLockHelper.placeSoftLockOnLoanAccount(loanId, INLINE_COB_LOCK_OWNER, "Sample error");

            updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), "05 March 2020");

            loanCOBCatchUpHelper.executeLoanCOBCatchUp();
            Utils.conditionalSleepWithMaxWait(30, 5, loanCOBCatchUpHelper::isLoanCOBCatchUpRunning);

            verifyLastClosedBusinessDate(loanId, "04 March 2020");

            AtomicReference<List<BatchResponse>> responseRef = new AtomicReference<>();
            runAsNonByPass(() -> responseRef.set(batchRequest().repayLoan(REPAYMENT_BATCH_REQUEST_ID, loanId, "10", "05 March 2020")
                    .executeWithoutEnclosingTransaction()));
            assertEquals(HttpStatus.SC_OK, responseRef.get().get(0).getStatusCode().intValue(), "Verify Status Code 200 for Repayment");

            verifyLastClosedBusinessDate(loanId, "04 March 2020");
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.PENALTY_WAIT_PERIOD,
                    new PutGlobalConfigurationsRequest().value(2L));
        }
    }
}
