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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Inline Savings COB places its locks with {@link LockOwner#SAVINGS_INLINE_COB_PROCESSING}, and lock errors are updated
 * by owner - so the inline listener must record errors under that owner, or the update matches no lock and the failure
 * is lost.
 */
class InlineCOBSavingsItemListenerTest {

    @Test
    void shouldRecordProcessingErrorAgainstTheInlineLockOwner() {
        LockingService lockingService = mock(LockingService.class);
        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(mock(TransactionStatus.class));
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
        SavingsAccount savingsAccount = mock(SavingsAccount.class);
        when(savingsAccount.getId()).thenReturn(5L);

        new InlineCOBSavingsItemListener(lockingService, transactionTemplate).onProcessError(savingsAccount, new RuntimeException("fail"));

        verify(lockingService).updateLockError(eq(5L), eq(LockOwner.SAVINGS_INLINE_COB_PROCESSING),
                eq("Savings (id: 5) processing is failed"), anyString());
    }
}
