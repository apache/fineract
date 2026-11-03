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
package org.apache.fineract.integrationtests.common.savings;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.SavingsAccountData;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsTransactionHelper;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Slf4j
public class SavingsTestLifecycleExtension implements AfterAllCallback {

    private final FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();
    private final FeignSavingsHelper savingsHelper = new FeignSavingsHelper(fineractClient);
    private final FeignSavingsTransactionHelper savingsTransactionHelper = new FeignSavingsTransactionHelper(fineractClient);
    public static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final int CLEANUP_THREAD_COUNT = 10;
    private final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern(DATE_FORMAT).toFormatter();

    @Override
    public void afterAll(ExtensionContext context) {
        BusinessDateHelper.runAt(DateTimeFormatter.ofPattern(DATE_FORMAT).format(Utils.getLocalDateOfTenant()), () -> {
            String jobName = "Post Interest For Savings";
            SchedulerJobHelper.executeAndAwaitJob(jobName);
            // Close open savings accounts
            List<Long> savingsIds = ok(() -> fineractClient.defaultApi().getSavingsAccountsByStatus(300));
            runInParallel(savingsIds, this::closeSavingsAccount);
        });
    }

    private void closeSavingsAccount(Long savingsId) {
        try {
            SavingsAccountData savingsAccountData = savingsHelper.getSavingsDetails(savingsId);
            BigDecimal accountBalance = MathUtil.subtract(savingsAccountData.getSummary().getAvailableBalance(),
                    savingsAccountData.getMinRequiredBalance(), MathContext.DECIMAL64);
            if (accountBalance.compareTo(BigDecimal.ZERO) > 0) {
                closeSavingsAccount(savingsId, new PostSavingsAccountsAccountIdRequest().locale("en").dateFormat(DATE_FORMAT)
                        .closedOnDate(dateFormatter.format(Utils.getLocalDateOfTenant())).withdrawBalance(true));
            } else if (accountBalance.compareTo(BigDecimal.ZERO) < 0) {
                savingsTransactionHelper.deposit(savingsId,
                        new PostSavingsAccountTransactionsRequest().locale("en").dateFormat(DATE_FORMAT)
                                .transactionDate(dateFormatter.format(Utils.getLocalDateOfTenant())).transactionAmount(accountBalance.abs())
                                .paymentTypeId(1));
                closeSavingsAccount(savingsId, new PostSavingsAccountsAccountIdRequest().locale("en").dateFormat(DATE_FORMAT)
                        .closedOnDate(dateFormatter.format(Utils.getLocalDateOfTenant())));
            }
        } catch (Exception e) {
            log.warn("Unable to close savings account: {}, Reason: {}", savingsId, e.getMessage());
        }
    }

    private void closeSavingsAccount(Long savingsId, PostSavingsAccountsAccountIdRequest request) {
        ok(() -> fineractClient.savingsAccount().handleCommandsSavingsAccount(savingsId, request, "close"));
    }

    private void runInParallel(List<Long> ids, Consumer<Long> action) {
        if (ids.isEmpty()) {
            return;
        }
        try (ExecutorService executor = Executors.newFixedThreadPool(Math.min(CLEANUP_THREAD_COUNT, ids.size()))) {
            try {
                List<Future<?>> futures = new ArrayList<>();
                ids.forEach(id -> futures.add(executor.submit(() -> action.accept(id))));
                for (Future<?> future : futures) {
                    future.get();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted during parallel savings cleanup", e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("Parallel savings cleanup failed", e);
            }
        }
    }
}
