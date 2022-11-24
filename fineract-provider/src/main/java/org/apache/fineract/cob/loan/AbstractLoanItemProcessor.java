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
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.TreeMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractLoanItemProcessor implements ItemProcessor<Loan, Loan> {

    private final COBBusinessStepService cobBusinessStepService;

    @Setter(AccessLevel.PROTECTED)
    private ExecutionContext executionContext;
    private LocalDate businessDate;

    @Override
    public Loan process(@NotNull Loan item) throws Exception {
        TreeMap<Long, String> businessStepMap = (TreeMap<Long, String>) executionContext.get(LoanCOBConstant.BUSINESS_STEP_MAP);

        Loan alreadyProcessedLoan = cobBusinessStepService.run(businessStepMap, item);
        alreadyProcessedLoan.setLastClosedBusinessDate(businessDate);
        return alreadyProcessedLoan;
    }

    @AfterStep
    public ExitStatus afterStep(@NotNull StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }

    protected void setBusinessDate(StepExecution stepExecution) {
        this.businessDate = LocalDate.parse(
                Objects.requireNonNull(
                        (String) stepExecution.getJobExecution().getExecutionContext().get(LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME)),
                DateTimeFormatter.ISO_DATE);
    }

}
