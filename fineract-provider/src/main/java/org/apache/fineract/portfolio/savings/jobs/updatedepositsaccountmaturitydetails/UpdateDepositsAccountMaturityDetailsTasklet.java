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
package org.apache.fineract.portfolio.savings.jobs.updatedepositsaccountmaturitydetails;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.DepositAccountData;
import org.apache.fineract.portfolio.savings.service.DepositAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.DepositAccountWritePlatformService;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@RequiredArgsConstructor
public class UpdateDepositsAccountMaturityDetailsTasklet implements Tasklet {

    private final DepositAccountReadPlatformService depositAccountReadPlatformService;
    private final DepositAccountWritePlatformService depositAccountWritePlatformService;
    // Each account gets its own transaction. Otherwise every account is saved in the step transaction, and a failed
    // save has already marked it rollback-only by the time the catch below runs, so every account's maturity is lost
    // at commit.
    private final TransactionTemplate requiresNewTransactionTemplate;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final Collection<DepositAccountData> depositAccounts = depositAccountReadPlatformService.retrieveForMaturityUpdate();
        // Keyed by the error, valued by the account number, so the job's failure message names the accounts.
        final Map<Throwable, List<String>> failures = new LinkedHashMap<>();

        for (final DepositAccountData depositAccount : depositAccounts) {
            try {
                final DepositAccountType depositAccountType = DepositAccountType
                        .fromInt(depositAccount.getDepositType().getId().intValue());
                requiresNewTransactionTemplate.executeWithoutResult(
                        status -> depositAccountWritePlatformService.updateMaturityDetails(depositAccount.getId(), depositAccountType));
            } catch (final PlatformApiDataValidationException e) {
                final List<ApiParameterError> errors = e.getErrors();
                for (final ApiParameterError error : errors) {
                    log.error("Update maturity details failed for account: {} with message {}", depositAccount.getAccountNo(),
                            error.getDeveloperMessage());
                }
                failures.put(e, List.of(depositAccount.getAccountNo()));
            } catch (final Exception ex) {
                log.error("Update maturity details failed for account: {}", depositAccount.getAccountNo(), ex);
                failures.put(ex, List.of(depositAccount.getAccountNo()));
            }
        }

        log.debug("{}: Records affected by updateMaturityDetailsOfDepositAccounts: {}", ThreadLocalContextUtil.getTenant().getName(),
                depositAccounts.size());
        // The other accounts are already committed; failing the job keeps the accounts that did not mature visible.
        JobExecutionException.throwErrors(failures);
        return RepeatStatus.FINISHED;
    }
}
