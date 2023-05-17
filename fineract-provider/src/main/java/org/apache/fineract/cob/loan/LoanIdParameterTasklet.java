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
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class LoanIdParameterTasklet implements Tasklet {

    private final RetrieveLoanIdService retrieveLoanIdService;
    private final CustomJobParameterResolver customJobParameterResolver;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String businessDateParameter = (String) contribution.getStepExecution().getJobExecution().getExecutionContext()
                .get(LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME);
        LocalDate businessDate = LocalDate.parse(Objects.requireNonNull(businessDateParameter));
        LoanCOBParameter minAndMaxLoanId = retrieveLoanIdService.retrieveMinAndMaxLoanIdsNDaysBehind(LoanCOBConstant.NUMBER_OF_DAYS_BEHIND,
                businessDate, customJobParameterResolver.getCustomJobParameterById(chunkContext.getStepContext().getStepExecution(),
                        LoanCOBConstant.IS_CATCH_UP_PARAMETER_NAME).map(Boolean::parseBoolean).orElse(false));
        if (Objects.isNull(minAndMaxLoanId)
                || (Objects.isNull(minAndMaxLoanId.getMinLoanId()) && Objects.isNull(minAndMaxLoanId.getMaxLoanId()))) {
            contribution.getStepExecution().getJobExecution().getExecutionContext().put(LoanCOBConstant.LOAN_COB_PARAMETER,
                    new LoanCOBParameter(0L, 0L));
            return RepeatStatus.FINISHED;
        }

        contribution.getStepExecution().getJobExecution().getExecutionContext().put(LoanCOBConstant.LOAN_COB_PARAMETER, minAndMaxLoanId);

        return RepeatStatus.FINISHED;
    }
}
