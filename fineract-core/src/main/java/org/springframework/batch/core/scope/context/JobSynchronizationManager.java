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
package org.springframework.batch.core.scope.context;

import org.apache.fineract.infrastructure.jobs.TenantAwareEqualsHashCodeAdvice;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.lang.Nullable;

// Temporary solution until spring-batch fixes the concurrency issue
// https://github.com/spring-projects/spring-batch/issues/4774
// Mostly copy from spring-batch
@SuppressWarnings({ "HideUtilityClassConstructor" })
public class JobSynchronizationManager {

    private static final SynchronizationManagerSupport<JobExecution, JobContext> manager = new SynchronizationManagerSupport<>() {

        @Override
        protected JobContext createNewContext(JobExecution execution) {
            return new JobContext(execution);
        }

        @Override
        protected void close(JobContext context) {
            context.close();
        }
    };

    @Nullable
    public static JobContext getContext() {
        return manager.getContext();
    }

    public static JobContext register(JobExecution jobExecution) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(JobExecution.class);
        enhancer.setCallback(new TenantAwareEqualsHashCodeAdvice(jobExecution));
        return manager.register((JobExecution) enhancer.create(new Class[] { JobInstance.class, Long.class, JobParameters.class },
                new Object[] { jobExecution.getJobInstance(), jobExecution.getId(), jobExecution.getJobParameters() }));
    }

    public static void close() {
        manager.close();
    }

    public static void release() {
        manager.release();
    }
}
