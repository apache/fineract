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
package org.apache.fineract.infrastructure.core.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class AfterTransactionWorkTest {

    private static final FineractPlatformTenant TENANT_A = new FineractPlatformTenant(1L, "tenant-a", "Tenant A", "UTC", null);
    private static final FineractPlatformTenant TENANT_B = new FineractPlatformTenant(2L, "tenant-b", "Tenant B", "UTC", null);

    @AfterEach
    void cleanUp() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        AfterTransactionWork.runReadyWork();
        ThreadLocalContextUtil.reset();
    }

    @Test
    void runsAtOnceWithoutTransactionSynchronization() {
        List<String> ran = new ArrayList<>();

        AfterTransactionWork.runAfterCompletion(() -> ran.add("work"));

        assertEquals(List.of("work"), ran);
    }

    @Test
    void waitsForTheTransactionToCompleteAndForTheReadyWorkToBeRun() {
        List<String> ran = new ArrayList<>();
        TransactionSynchronizationManager.initSynchronization();

        AfterTransactionWork.runAfterCompletion(() -> ran.add("work"));
        assertTrue(ran.isEmpty());

        completeTransaction();
        assertTrue(ran.isEmpty());

        AfterTransactionWork.runReadyWork();
        assertEquals(List.of("work"), ran);
    }

    @Test
    void runsInTheTenantContextItWasRegisteredUnderAndRestoresTheCurrentOne() {
        List<FineractPlatformTenant> tenantsSeen = new ArrayList<>();
        List<String> dataSourceContextsSeen = new ArrayList<>();
        ThreadLocalContextUtil.setTenant(TENANT_A);
        ThreadLocalContextUtil.setDataSourceContext("context-a");
        TransactionSynchronizationManager.initSynchronization();
        AfterTransactionWork.runAfterCompletion(() -> {
            tenantsSeen.add(ThreadLocalContextUtil.getTenant());
            dataSourceContextsSeen.add(ThreadLocalContextUtil.getDataSourceContext());
        });
        completeTransaction();

        // The pooled thread has moved on to another tenant by the time the work is drained.
        ThreadLocalContextUtil.setTenant(TENANT_B);
        ThreadLocalContextUtil.setDataSourceContext("context-b");
        AfterTransactionWork.runReadyWork();

        assertEquals(List.of(TENANT_A), tenantsSeen);
        assertEquals(List.of("context-a"), dataSourceContextsSeen);
        assertSame(TENANT_B, ThreadLocalContextUtil.getTenant());
        assertEquals("context-b", ThreadLocalContextUtil.getDataSourceContext());
    }

    @Test
    void aFailingPieceOfWorkDoesNotStopTheOthers() {
        List<String> ran = new ArrayList<>();
        TransactionSynchronizationManager.initSynchronization();
        AfterTransactionWork.runAfterCompletion(() -> {
            throw new IllegalStateException("boom");
        });
        AfterTransactionWork.runAfterCompletion(() -> ran.add("second"));
        completeTransaction();

        AfterTransactionWork.runReadyWork();

        assertEquals(List.of("second"), ran);
    }

    private static void completeTransaction() {
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        TransactionSynchronizationManager.clearSynchronization();
        synchronizations.forEach(synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
    }
}
