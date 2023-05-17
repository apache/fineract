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
package org.apache.fineract.cob.loan;

import java.time.LocalDate;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.common.CustomJobParameterResolver;
import org.apache.fineract.cob.data.LoanCOBParameter;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class LockLoanTasklet implements Tasklet {

    private final LoanLockingService loanLockingService;
    private final CustomJobParameterResolver customJobParameterResolver;

    @Override
    public RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) throws Exception {
        String businessDateParameter = (String) contribution.getStepExecution().getJobExecution().getExecutionContext()
                .get(LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME);
        LocalDate lastClosedBusinessDate = LocalDate.parse(Objects.requireNonNull(businessDateParameter))
                .minusDays(LoanCOBConstant.NUMBER_OF_DAYS_BEHIND);
        LoanCOBParameter loanCOBParameter = (LoanCOBParameter) contribution.getStepExecution().getJobExecution().getExecutionContext()
                .get(LoanCOBConstant.LOAN_COB_PARAMETER);
        if (Objects.isNull(loanCOBParameter)
                || (Objects.isNull(loanCOBParameter.getMinLoanId()) && Objects.isNull(loanCOBParameter.getMaxLoanId()))) {
            loanCOBParameter = new LoanCOBParameter(0L, 0L);
        }
        loanLockingService.applySoftLock(lastClosedBusinessDate, loanCOBParameter,
                customJobParameterResolver
                        .getCustomJobParameterById(contribution.getStepExecution(), LoanCOBConstant.IS_CATCH_UP_PARAMETER_NAME)
                        .map(Boolean::parseBoolean).orElse(false));

        return RepeatStatus.FINISHED;
    }
}
