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
package org.apache.fineract.infrastructure.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.scope.context.SynchronizationManagerSupport;
import org.springframework.batch.core.step.StepExecution;

/**
 * Reproduces FINERACT-2194 / spring-batch #4774 at the mechanism level.
 * <p>
 * Spring Batch keeps the running step contexts in a JVM-wide map keyed by {@link StepExecution}, whose equality is
 * based on the database-generated ids only. Fineract keeps the batch metadata tables per tenant, so two tenants running
 * the same job at the same time produce executions with identical ids. Without a tenant-aware key the second tenant is
 * handed the first tenant's context - and its in-memory {@link StepExecution} - which is what surfaces as
 * {@code OptimisticLockingFailureException: Attempt to update step execution id=1 with wrong version}.
 * <p>
 * The first test shows the collision with Spring's registry as shipped; the second shows that the shadowed
 * {@link StepSynchronizationManager} in fineract-core keeps the tenants apart.
 */
class StepSynchronizationManagerMultiTenantTest {

    private static final String STEP_NAME = "SEND_ASYNCHRONOUS_EVENTS_STEP";
    private static final String JOB_NAME = "SEND_ASYNCHRONOUS_EVENTS";

    private ExecutorService tenantThreads;

    @BeforeEach
    void setUp() {
        tenantThreads = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() {
        tenantThreads.shutdownNow();
    }

    @Test
    void springRegistryHandsTheSecondTenantTheFirstTenantsContext() throws Exception {
        // Spring's own registry, exactly as SynchronizationManagerSupport ships it: keyed by StepExecution equality.
        SynchronizationManagerSupport<StepExecution, StepContext> springRegistry = new SynchronizationManagerSupport<>() {

            @Override
            protected StepContext createNewContext(StepExecution execution) {
                return new StepContext(execution);
            }

            @Override
            protected void close(StepContext context) {
                context.close();
            }
        };

        StepExecution alphaExecution = stepExecutionWithId1();
        StepExecution betaExecution = stepExecutionWithId1();
        assertNotSame(alphaExecution, betaExecution);
        assertEquals(alphaExecution, betaExecution, "same ids => equal, which is the root of the collision");

        Registration alpha = registerConcurrently("alpha", alphaExecution, springRegistry::register, springRegistry::close);
        Registration beta = registerConcurrently("beta", betaExecution, springRegistry::register, springRegistry::close);

        assertSame(alpha.context(), beta.context(), "both tenants share one StepContext");
        assertSame(alphaExecution, beta.context().getStepExecution(), "tenant beta works on tenant alpha's StepExecution");
    }

    @Test
    void fineractRegistryKeepsTenantsApart() throws Exception {
        StepExecution alphaExecution = stepExecutionWithId1();
        StepExecution betaExecution = stepExecutionWithId1();

        Registration alpha = registerConcurrently("alpha", alphaExecution, StepSynchronizationManager::register,
                StepSynchronizationManager::close);
        Registration beta = registerConcurrently("beta", betaExecution, StepSynchronizationManager::register,
                StepSynchronizationManager::close);

        assertNotSame(alpha.context(), beta.context(), "each tenant gets its own StepContext");
        // The registered execution is a CGLIB wrapper; its state must still be the tenant's own object.
        assertSame(alphaExecution.getJobExecution(), alpha.context().getStepExecution().getJobExecution());
        assertSame(betaExecution.getJobExecution(), beta.context().getStepExecution().getJobExecution());
    }

    @Test
    void fineractRegistryKeepsSixTenantsApart() throws Exception {
        tenantThreads.shutdownNow();
        tenantThreads = Executors.newFixedThreadPool(6);
        List<StepExecution> executions = new ArrayList<>();
        List<Registration> registrations = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            StepExecution execution = stepExecutionWithId1();
            executions.add(execution);
            registrations.add(
                    registerConcurrently("tenant" + i, execution, StepSynchronizationManager::register, StepSynchronizationManager::close));
        }

        Set<StepContext> distinctContexts = Collections.newSetFromMap(new IdentityHashMap<>());
        registrations.forEach(registration -> distinctContexts.add(registration.context()));
        assertEquals(6, distinctContexts.size(), "one StepContext per tenant");
        for (int i = 0; i < 6; i++) {
            assertSame(executions.get(i).getJobExecution(), registrations.get(i).context().getStepExecution().getJobExecution(),
                    "tenant" + (i + 1) + " keeps its own execution");
        }
    }

    private static StepExecution stepExecutionWithId1() {
        JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, JOB_NAME), new JobParameters());
        return new StepExecution(1L, STEP_NAME, jobExecution);
    }

    private record Registration(StepContext context, CountDownLatch release, Future<?> thread) {
    }

    /**
     * Registers {@code execution} on a dedicated thread that stays registered until the test ends, mirroring two Quartz
     * workers that run the same job for two tenants at the same moment. The tenant is set on the thread first, the way
     * {@code JobStarter} does it.
     */
    private Registration registerConcurrently(String tenantIdentifier, StepExecution execution,
            Function<StepExecution, StepContext> register, Runnable close) throws Exception {
        CountDownLatch registered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        StepContext[] holder = new StepContext[1];
        Future<?> thread = tenantThreads.submit(() -> {
            ThreadLocalContextUtil.setTenant(FineractPlatformTenant.builder().id(1L).tenantIdentifier(tenantIdentifier).build());
            try {
                holder[0] = register.apply(execution);
                registered.countDown();
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                close.run();
                ThreadLocalContextUtil.reset();
            }
            return null;
        });
        registered.await(10, TimeUnit.SECONDS);
        return new Registration(holder[0], release, thread);
    }
}
