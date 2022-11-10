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

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.google.common.base.Splitter;
import io.cucumber.java8.En;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.cob.exceptions.LoanReadException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;

public class LoanItemReaderStepDefinitions implements En {

    private LoanRepository loanRepository = mock(LoanRepository.class);

    private LoanItemReader loanItemReader = new LoanItemReader(loanRepository);

    private Loan loan = mock(Loan.class);

    private Loan resultItem;

    public LoanItemReaderStepDefinitions() {
        Given("/^The LoanItemReader.read method with loanIds (.*), lockedAccounts (.*)$/", (String loanIds, String lockedAccounts) -> {
            JobExecution jobExecution = new JobExecution(1L);
            ExecutionContext jobExecutionContext = new ExecutionContext();
            List<Long> splitLockedAccounts;
            if (lockedAccounts.isEmpty()) {
                splitLockedAccounts = new ArrayList<>();
            } else {
                List<String> splitLockedAccountsStr = Splitter.on(',').splitToList(lockedAccounts);
                splitLockedAccounts = splitLockedAccountsStr.stream().map(Long::parseLong).toList();
            }
            jobExecutionContext.put(LoanCOBConstant.ALREADY_LOCKED_BY_INLINE_COB_OR_PROCESSED_LOAN_IDS,
                    new ArrayList<>(splitLockedAccounts));
            jobExecution.setExecutionContext(jobExecutionContext);
            StepExecution stepExecution = new StepExecution("test", jobExecution);
            ExecutionContext stepExecutionContext = new ExecutionContext();
            List<Long> splitAccounts;
            if (loanIds.isEmpty()) {
                splitAccounts = new ArrayList<>();
            } else {
                List<String> splitStr = Splitter.on(',').splitToList(loanIds);
                splitAccounts = splitStr.stream().map(Long::parseLong).toList();
            }
            stepExecutionContext.put(LoanCOBConstant.LOAN_IDS, new ArrayList<>(splitAccounts));
            stepExecution.setExecutionContext(stepExecutionContext);
            loanItemReader.beforeStep(stepExecution);

            lenient().when(this.loanRepository.findById(0L)).thenReturn(Optional.empty());
            lenient().when(this.loanRepository.findById(1L)).thenReturn(Optional.of(loan));
            lenient().when(this.loanRepository.findById(-1L)).thenThrow(new RuntimeException("fail"));

        });

        When("LoanItemReader.read method executed", () -> {
            resultItem = this.loanItemReader.read();
        });

        Then("The LoanItemReader.read result should match", () -> {
            assertEquals(loan, resultItem);
        });

        Then("The LoanItemReader.read result null", () -> {
            assertNull(resultItem);
        });

        Then("throw exception LoanItemReader.read method", () -> {
            assertThrows(LoanReadException.class, () -> {
                resultItem = this.loanItemReader.read();
            });
        });
    }
}
