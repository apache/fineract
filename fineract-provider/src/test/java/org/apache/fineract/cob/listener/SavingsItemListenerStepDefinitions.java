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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cucumber.java8.En;
import java.util.List;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.exceptions.LockedReadException;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.mockito.Mockito;
import org.springframework.batch.item.Chunk;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class SavingsItemListenerStepDefinitions implements En {

    private final LockingService savingsLockingService = mock(LockingService.class);
    private final TransactionTemplate batchJdbcTransactionTemplate = spy(TransactionTemplate.class);

    private final ChunkProcessingSavingsItemListener savingsItemListener = new ChunkProcessingSavingsItemListener(savingsLockingService,
            batchJdbcTransactionTemplate);

    private Exception exception;
    private final SavingsAccount savingsAccount = mock(SavingsAccount.class);

    public SavingsItemListenerStepDefinitions() {
        Given("/^The SavingsItemListener.onReadError method (.*)$/", (String action) -> {
            ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
            exception = new LockedReadException(1L, new RuntimeException("fail"));
            batchJdbcTransactionTemplate.setTransactionManager(mock(PlatformTransactionManager.class));
            when(savingsAccount.getId()).thenReturn(1L);
        });

        When("SavingsItemListener.onReadError method executed", () -> {
            try {
                savingsItemListener.onReadError(exception);
            } finally {
                ThreadLocalContextUtil.reset();
            }
        });

        Then("SavingsItemListener.onReadError result should match", () -> {
            verify(batchJdbcTransactionTemplate, Mockito.times(1)).execute(any());
            verify(savingsLockingService, Mockito.times(1)).updateLockError(eq(1L), eq(LockOwner.SAVINGS_COB_CHUNK_PROCESSING),
                    eq("Savings (id: 1) reading is failed"), anyString());
        });

        Given("/^The SavingsItemListener.onProcessError method (.*)$/", (String action) -> {
            ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
            exception = new LockedReadException(1L, new RuntimeException("fail"));
            when(savingsAccount.getId()).thenReturn(2L);
            batchJdbcTransactionTemplate.setTransactionManager(mock(PlatformTransactionManager.class));
        });

        When("SavingsItemListener.onProcessError method executed", () -> {
            try {
                savingsItemListener.onProcessError(savingsAccount, exception);
            } finally {
                ThreadLocalContextUtil.reset();
            }
        });

        Then("SavingsItemListener.onProcessError result should match", () -> {
            verify(batchJdbcTransactionTemplate, Mockito.times(1)).execute(any());
            verify(savingsLockingService, Mockito.times(1)).updateLockError(eq(2L), eq(LockOwner.SAVINGS_COB_CHUNK_PROCESSING),
                    eq("Savings (id: 2) processing is failed"), anyString());
        });

        Given("/^The SavingsItemListener.onWriteError method (.*)$/", (String action) -> {
            ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
            exception = new LockedReadException(3L, new RuntimeException("fail"));
            when(savingsAccount.getId()).thenReturn(3L);
            batchJdbcTransactionTemplate.setTransactionManager(mock(PlatformTransactionManager.class));
        });

        When("SavingsItemListener.onWriteError method executed", () -> {
            try {
                savingsItemListener.onWriteError(exception, new Chunk<>(List.of(savingsAccount)));
            } finally {
                ThreadLocalContextUtil.reset();
            }
        });

        Then("SavingsItemListener.onWriteError result should match", () -> {
            verify(batchJdbcTransactionTemplate, Mockito.times(1)).execute(any());
            verify(savingsLockingService, Mockito.times(1)).updateLockError(eq(3L), eq(LockOwner.SAVINGS_COB_CHUNK_PROCESSING),
                    eq("Savings (id: 3) writing is failed"), anyString());
        });
    }
}
