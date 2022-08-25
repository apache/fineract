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

import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanItemProcessor implements ItemProcessor<Loan, Loan>, StepExecutionListener {

    private final COBBusinessStepService cobBusinessStepService;
    private StepExecution stepExecution;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public Loan process(Loan item) throws Exception {
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        TreeMap<Long, String> businessStepMap = (TreeMap<Long, String>) executionContext.get("BusinessStepMap");
        return cobBusinessStepService.run(businessStepMap, item);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
