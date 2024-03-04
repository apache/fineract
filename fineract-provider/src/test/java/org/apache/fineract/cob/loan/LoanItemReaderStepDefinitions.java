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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.google.common.base.Splitter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.cucumber.java8.En;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.fineract.cob.common.CustomJobParameterResolver;
import org.apache.fineract.cob.data.LoanCOBParameter;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.exceptions.LoanReadException;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;

@SuppressFBWarnings(value = "RV_EXCEPTION_NOT_THROWN", justification = "False positive")
public class LoanItemReaderStepDefinitions implements En {

    private LoanRepository loanRepository = mock(LoanRepository.class);

    private RetrieveLoanIdService retrieveLoanIdService = mock(RetrieveLoanIdService.class);

    private CustomJobParameterResolver customJobParameterResolver = mock(CustomJobParameterResolver.class);

    private LoanLockingService lockingService = mock(LoanLockingService.class);

    private LoanItemReader loanItemReader = new LoanItemReader(loanRepository, retrieveLoanIdService, customJobParameterResolver,
            lockingService);

    private Loan loan = mock(Loan.class);

    private Loan resultItem;

    public LoanItemReaderStepDefinitions() {
        Given("/^The LoanItemReader.read method with loanIds (.*)$/", (String loanIds) -> {
            JobExecution jobExecution = new JobExecution(1L);
            ExecutionContext jobExecutionContext = new ExecutionContext();
            jobExecution.setExecutionContext(jobExecutionContext);
            StepExecution stepExecution = new StepExecution("test", jobExecution);
            ExecutionContext stepExecutionContext = new ExecutionContext();
            Long minLoanId = null;
            Long maxLoanId = null;
            List<Long> splitAccounts = new ArrayList<>();
            if (!loanIds.isEmpty()) {
                List<String> splitStr = Splitter.on(',').splitToList(loanIds);
                splitAccounts = splitStr.stream().map(Long::parseLong).collect(Collectors.toList());
                minLoanId = splitAccounts.get(0);
                maxLoanId = splitAccounts.get(splitAccounts.size() - 1);
            }
            LoanCOBParameter loanCOBParameter = new LoanCOBParameter(minLoanId, maxLoanId);
            stepExecutionContext.put(LoanCOBConstant.LOAN_COB_PARAMETER, loanCOBParameter);
            stepExecution.setExecutionContext(stepExecutionContext);

            lenient().when(
                    this.retrieveLoanIdService.retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(loanCOBParameter, false))
                    .thenReturn(splitAccounts);

            HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
            LocalDate businessDate = LocalDate.now(ZoneId.systemDefault());
            businessDates.put(BusinessDateType.BUSINESS_DATE, businessDate);
            businessDates.put(BusinessDateType.COB_DATE, businessDate.minusDays(1));
            ThreadLocalContextUtil.setBusinessDates(businessDates);
            LoanAccountLock loanAccountLock = new LoanAccountLock(1L, LockOwner.LOAN_COB_CHUNK_PROCESSING, businessDate.minusDays(1));
            LoanAccountLock loanAccountLockNegativeNumberTest = new LoanAccountLock(-1L, LockOwner.LOAN_COB_CHUNK_PROCESSING,
                    businessDate.minusDays(1));
            lenient().when(customJobParameterResolver.getCustomJobParameterSet(any())).thenReturn(Optional.empty());
            lenient().when(lockingService.findAllByLoanIdInAndLockOwner(List.of(1L), LockOwner.LOAN_COB_CHUNK_PROCESSING))
                    .thenReturn(List.of(loanAccountLock));
            lenient().when(lockingService.findAllByLoanIdInAndLockOwner(List.of(1L, 2L), LockOwner.LOAN_COB_CHUNK_PROCESSING))
                    .thenReturn(List.of(loanAccountLock));
            lenient().when(lockingService.findAllByLoanIdInAndLockOwner(List.of(-1L), LockOwner.LOAN_COB_CHUNK_PROCESSING))
                    .thenReturn(List.of(loanAccountLockNegativeNumberTest));

            loanItemReader.beforeStep(stepExecution);

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
