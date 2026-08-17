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
package org.apache.fineract.portfolio.savings.jobs.payduesavingscharges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.savings.data.SavingsAccountAnnualFeeData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class PayDueSavingsChargesTasklet implements Tasklet {

    private final SavingsAccountChargeReadPlatformService savingsAccountChargeReadPlatformService;
    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final Collection<SavingsAccountAnnualFeeData> chargesDueData = savingsAccountChargeReadPlatformService.retrieveChargesWithDue();
        List<Throwable> exceptions = new ArrayList<>();
        for (final SavingsAccountAnnualFeeData savingsAccountReference : chargesDueData) {
            try {
                savingsAccountWritePlatformService.applyChargeDue(savingsAccountReference.getId(), savingsAccountReference.getAccountId());
            } catch (final PlatformApiDataValidationException e) {
                exceptions.add(e);
                final List<ApiParameterError> errors = e.getErrors();
                for (final ApiParameterError error : errors) {
                    log.error("Apply Charges due for savings failed for account {} with message: {}",
                            savingsAccountReference.getAccountNo(), error.getDeveloperMessage(), e);
                }
            } catch (final Exception ex) {
                exceptions.add(ex);
                log.error("Apply Charges due for savings failed for account: {}", savingsAccountReference.getAccountNo(), ex);
            }
        }
        log.debug("{}: Records affected by applyDueChargesForSavings: {}", ThreadLocalContextUtil.getTenant().getName(),
                chargesDueData.size());
        if (!exceptions.isEmpty()) {
            throw new JobExecutionException(exceptions);
        }
        return RepeatStatus.FINISHED;
    }
}
