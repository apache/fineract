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
package org.apache.fineract.integrationtests.cob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.COBPartition;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessStepHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCobHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalAssetOwnerHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class CobPartitioningTest extends FeignLoanTestBase {

    public static final int N = 10;
    private static final String CUMULATIVE_STRATEGY = "mifos-standard-strategy";
    private static LocalDate todaysDate;

    private final FeignExternalAssetOwnerHelper externalAssetOwnerHelper = new FeignExternalAssetOwnerHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    @BeforeAll
    public static void setupBusinessStep() {
        todaysDate = Utils.getLocalDateOfTenant();
        new FeignBusinessStepHelper(FineractFeignClientHelper.getFineractFeignClient()).updateSteps("LOAN_CLOSE_OF_BUSINESS",
                "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION", "CHECK_LOAN_REPAYMENT_DUE",
                "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "EXTERNAL_ASSET_OWNER_TRANSFER");
    }

    @Test
    public void testLoanCOBPartitioningQuery() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            // The EXTERNAL_ASSET_OWNER_TRANSFER COB step registered above needs an ASSET_TRANSFER -> GL mapping to run.
            // Created here rather than in @BeforeAll because accountHelper is not initialised at static-init time.
            Account transferAccount = accountHelper.createAssetAccount(Utils.uniqueRandomStringGenerator("TRANSFER_", 5));
            externalAssetOwnerHelper.setProperFinancialActivity(transferAccount);
            setInitialBusinessDate("02 March 2020");

            List<Long> loanIds = new CopyOnWriteArrayList<>();

            // Let's create 1, 2, ..., N-1, N loans
            final CountDownLatch createLatch = new CountDownLatch(N - 1);
            Long loanProductId = createLoanProduct();
            List<Future<?>> futures = new ArrayList<>();
            // Warm up (EclipseLink sometimes fails if JPQL cache is not warm up but concurrent queries are executed)
            Long clientId = createClient();
            Long loanId = createLoanForClient(clientId, loanProductId);
            loanIds.add(loanId);
            for (int i = 1; i < N; i++) {
                futures.add(executorService.submit(() -> {
                    Long internalClientId = createClient();
                    Long internalLoanId = createLoanForClient(internalClientId, loanProductId);
                    loanIds.add(internalLoanId);
                    createLatch.countDown();
                }));
            }
            waitForFutures(futures, createLatch);
            futures.clear();

            // Force close loans 3, 4, ... , N-3, N-2
            Collections.sort(loanIds);
            final CountDownLatch closeLatch = new CountDownLatch(N - 5);
            // Warm up (EclipseLink sometimes fails if JPQL cache is not warm up but concurrent queries are executed)
            forecloseLoan(loanIds.get(2), LoanRequestBuilders.forecloseLoan("02 March 2020"));
            for (int i = 3; i < N - 2; i++) {
                final int idx = i;
                futures.add(executorService.submit(() -> {
                    forecloseLoan(loanIds.get(idx), LoanRequestBuilders.forecloseLoan("02 March 2020"));
                    closeLatch.countDown();
                }));
            }
            waitForFutures(futures, closeLatch);

            // Let's retrieve the partitions
            List<COBPartition> cobPartitions = new FeignCobHelper(FineractFeignClientHelper.getFineractFeignClient()).getCobPartitions(3);
            log.info("\nLoans created: {},\nRetrieved partitions: {}", loanIds, cobPartitions);
            assertEquals(2, cobPartitions.size());

            assertEquals(loanIds.get(0), cobPartitions.get(0).getMinId());
            assertEquals(loanIds.get(8), cobPartitions.get(0).getMaxId());

            assertEquals(loanIds.get(9), cobPartitions.get(1).getMinId());
            assertEquals(loanIds.get(9), cobPartitions.get(1).getMaxId());
        } finally {
            executorService.shutdown();
            cleanUpAndRestoreBusinessDate();
        }
    }

    private static void waitForFutures(List<Future<?>> futures, CountDownLatch latch) throws InterruptedException {
        futures.forEach(future -> {
            try {
                future.get(); // turn any possible async failures into errors
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        latch.await();
    }

    private void setInitialBusinessDate(String date) {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), date);
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.PENALTY_WAIT_PERIOD,
                new PutGlobalConfigurationsRequest().value(0L));
    }

    private void cleanUpAndRestoreBusinessDate() {
        updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), todaysDate.format(dateTimeFormatter));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(false));
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
    }

    private Long createLoanProduct() {
        ChargeRequest overdueFeeCharge = ChargeRequestBuilders.loanOverdueFee(1.0)
                .chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getValue());
        Long overdueFeeChargeId = chargesHelper.createCharge(overdueFeeCharge).getResourceId();

        PostLoanProductsRequest product = create4Period1MonthLongWithoutInterestProduct(CUMULATIVE_STRATEGY).principal(15000.0)
                .charges(List.of(new LoanProductChargeData().id(overdueFeeChargeId)));
        return createLoanProduct(product);
    }

    private Long createLoanForClient(Long clientId, Long loanProductId) {
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
        return loanId;
    }
}
