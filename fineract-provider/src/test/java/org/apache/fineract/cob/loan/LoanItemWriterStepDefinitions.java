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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.cucumber.java8.En;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.cob.domain.LoanAccountLockRepository;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.mockito.Mockito;

public class LoanItemWriterStepDefinitions implements En {

    private LoanAccountLockRepository accountLockRepository = mock(LoanAccountLockRepository.class);
    private LoanRepository loanRepository = mock(LoanRepository.class);

    private LoanItemWriter loanItemWriter = new LoanItemWriter(accountLockRepository);

    private List<Loan> items;

    public LoanItemWriterStepDefinitions() {
        Given("/^The LoanItemWriter.write method with action (.*)$/", (String action) -> {

            Loan loan = mock(Loan.class);
            lenient().when(loan.getId()).thenReturn(1L);
            if (action.equals("error")) {
                this.items = Collections.emptyList();
                lenient().doThrow(new RuntimeException("fail")).when(this.accountLockRepository)
                        .deleteByLoanIdInAndLockOwner(Collections.emptyList(), LockOwner.LOAN_COB_CHUNK_PROCESSING);
            } else {
                this.items = Collections.singletonList(loan);
                lenient().doNothing().when(this.accountLockRepository).deleteByLoanIdInAndLockOwner(Mockito.anyList(), Mockito.any());
            }
            this.loanItemWriter.setRepository(loanRepository);
        });

        When("LoanItemWriter.write method executed", () -> {
            this.loanItemWriter.write(items);
        });

        Then("LoanItemWriter.write result should match", () -> {
            verify(this.accountLockRepository, Mockito.times(1)).deleteByLoanIdInAndLockOwner(Mockito.any(), Mockito.any());
        });

        Then("throw exception LoanItemWriter.write method", () -> {
            assertThrows(RuntimeException.class, () -> {
                this.loanItemWriter.write(items);
            });
        });
    }
}
