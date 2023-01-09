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

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;

public class LoanItemReader extends AbstractLoanItemReader {

    public LoanItemReader(LoanRepository loanRepository) {
        super(loanRepository);
    }

    @BeforeStep
    @SuppressWarnings({ "unchecked" })
    public void beforeStep(@NotNull StepExecution stepExecution) {

        ExecutionContext executionContext = stepExecution.getExecutionContext();
        ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
        List<Long> loanIds = (List<Long>) executionContext.get(LoanCOBConstant.LOAN_IDS);
        setAlreadyLockedOrProcessedAccounts(
                (List<Long>) jobExecutionContext.get(LoanCOBConstant.ALREADY_LOCKED_BY_INLINE_COB_OR_PROCESSED_LOAN_IDS));
        setRemainingData(new ArrayList<>(loanIds));
    }
}
