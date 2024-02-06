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
package org.apache.fineract.cob.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cucumber.java8.En;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.exceptions.LoanReadException;
import org.apache.fineract.cob.loan.LoanLockingService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.mockito.Mockito;
import org.springframework.batch.item.Chunk;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

public class LoanItemListenerStepDefinitions implements En {

    private LoanLockingService loanLockingService = mock(LoanLockingService.class);
    private TransactionTemplate transactionTemplate = spy(TransactionTemplate.class);

    private ChunkProcessingLoanItemListener loanItemListener = new ChunkProcessingLoanItemListener(loanLockingService, transactionTemplate);

    private Exception exception;

    private LoanAccountLock loanAccountLock;
    private final Loan loan = mock(Loan.class);

    public LoanItemListenerStepDefinitions() {
        Given("/^The LoanItemListener.onReadError method (.*)$/", (String action) -> {
            ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
            exception = new LoanReadException(1L, new RuntimeException("fail"));
            loanAccountLock = new LoanAccountLock(1L, LockOwner.LOAN_COB_CHUNK_PROCESSING, LocalDate.now(ZoneId.systemDefault()));
            when(loanLockingService.findByLoanIdAndLockOwner(1L, LockOwner.LOAN_COB_CHUNK_PROCESSING)).thenReturn(loanAccountLock);
            transactionTemplate.setTransactionManager(mock(PlatformTransactionManager.class));
            when(loan.getId()).thenReturn(1L);
        });

        When("LoanItemListener.onReadError method executed", () -> {
            try {
                loanItemListener.onReadError(exception);
            } finally {
                ThreadLocalContextUtil.reset();
            }
        });

        Then("LoanItemListener.onReadError result should match", () -> {
            verify(transactionTemplate, Mockito.times(1)).setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            verify(transactionTemplate, Mockito.times(1)).execute(any());
            verify(loanLockingService, Mockito.times(1)).findByLoanIdAndLockOwner(1L, LockOwner.LOAN_COB_CHUNK_PROCESSING);
            assertEquals("Loan (id: 1) reading is failed", loanAccountLock.getError());
            assertNotNull(loanAccountLock.getStacktrace());
        });

        Given("/^The LoanItemListener.onProcessError method (.*)$/", (String action) -> {
            ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
            exception = new LoanReadException(1L, new RuntimeException("fail"));
            loanAccountLock = new LoanAccountLock(2L, LockOwner.LOAN_COB_CHUNK_PROCESSING, LocalDate.now(ZoneId.systemDefault()));
            when(loanLockingService.findByLoanIdAndLockOwner(2L, LockOwner.LOAN_COB_CHUNK_PROCESSING)).thenReturn(loanAccountLock);
            when(loan.getId()).thenReturn(2L);
            transactionTemplate.setTransactionManager(mock(PlatformTransactionManager.class));
        });

        When("LoanItemListener.onProcessError method executed", () -> {
            try {
                loanItemListener.onProcessError(loan, exception);
            } finally {
                ThreadLocalContextUtil.reset();
            }
        });

        Then("LoanItemListener.onProcessError result should match", () -> {
            verify(transactionTemplate, Mockito.times(1)).setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            verify(transactionTemplate, Mockito.times(1)).execute(any());
            verify(loanLockingService, Mockito.times(1)).findByLoanIdAndLockOwner(2L, LockOwner.LOAN_COB_CHUNK_PROCESSING);
            assertEquals("Loan (id: 2) processing is failed", loanAccountLock.getError());
            assertNotNull(loanAccountLock.getStacktrace());
        });

        Given("/^The LoanItemListener.onWriteError method (.*)$/", (String action) -> {
            ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
            exception = new LoanReadException(3L, new RuntimeException("fail"));
            loanAccountLock = new LoanAccountLock(3L, LockOwner.LOAN_COB_CHUNK_PROCESSING, LocalDate.now(ZoneId.systemDefault()));
            when(loanLockingService.findByLoanIdAndLockOwner(3L, LockOwner.LOAN_COB_CHUNK_PROCESSING)).thenReturn(loanAccountLock);
            when(loan.getId()).thenReturn(3L);
            transactionTemplate.setTransactionManager(mock(PlatformTransactionManager.class));
        });

        When("LoanItemListener.onWriteError method executed", () -> {
            try {
                loanItemListener.onWriteError(exception, new Chunk<>(List.of(loan)));
            } finally {
                ThreadLocalContextUtil.reset();
            }
        });

        Then("LoanItemListener.onWriteError result should match", () -> {
            verify(transactionTemplate, Mockito.times(1)).setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            verify(transactionTemplate, Mockito.times(1)).execute(any());
            verify(loanLockingService, Mockito.times(1)).findByLoanIdAndLockOwner(3L, LockOwner.LOAN_COB_CHUNK_PROCESSING);
            assertEquals("Loan (id: 3) writing is failed", loanAccountLock.getError());
            assertNotNull(loanAccountLock.getStacktrace());
        });
    }
}
