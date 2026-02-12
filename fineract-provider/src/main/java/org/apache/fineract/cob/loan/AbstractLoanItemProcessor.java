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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.service.ProgressiveLoanModelProcessingService;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractLoanItemProcessor implements ItemProcessor<Loan, Loan> {

    private final COBBusinessStepService cobBusinessStepService;
    private final ProgressiveLoanModelProcessingService progressiveLoanModelProcessingService;

    @Setter(AccessLevel.PROTECTED)
    private ExecutionContext executionContext;
    private LocalDate businessDate;

    @SuppressWarnings({ "unchecked" })
    @Override
    public Loan process(@NonNull Loan loan) throws Exception {
        if (needToRebuildModel(loan)) {
            progressiveLoanModelProcessingService.recalculateModelAndSave(loan.getId());
        }
        Set<BusinessStepNameAndOrder> businessSteps = (Set<BusinessStepNameAndOrder>) executionContext.get(LoanCOBConstant.BUSINESS_STEPS);
        if (businessSteps == null) {
            throw new IllegalStateException("No business steps found in the execution context");
        }
        TreeMap<Long, String> businessStepMap = getBusinessStepMap(businessSteps);

        Loan alreadyProcessedLoan = cobBusinessStepService.run(businessStepMap, loan);
        alreadyProcessedLoan.setLastClosedBusinessDate(businessDate);
        return alreadyProcessedLoan;
    }

    private boolean needToRebuildModel(Loan loan) {
        return loan.isProgressiveSchedule() && !progressiveLoanModelProcessingService.hasValidModel(loan.getId(),
                ProgressiveLoanInterestScheduleModel.getModelVersion());
    }

    private TreeMap<Long, String> getBusinessStepMap(Set<BusinessStepNameAndOrder> businessSteps) {
        Map<Long, String> businessStepMap = businessSteps.stream()
                .collect(Collectors.toMap(BusinessStepNameAndOrder::getStepOrder, BusinessStepNameAndOrder::getStepName));
        return new TreeMap<>(businessStepMap);
    }

    @AfterStep
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }

    protected void setBusinessDate(StepExecution stepExecution) {
        this.businessDate = LocalDate.parse(
                Objects.requireNonNull(
                        (String) stepExecution.getJobExecution().getExecutionContext().get(LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME)),
                DateTimeFormatter.ISO_DATE);
    }

}
