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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBWorkerConfiguration;
import org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBWorkerItemReader;
import org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBWorkerItemWriter;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilder;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.backoff.BackOffExecution;

/**
 * Pins the COB worker steps to a single attempt per item.
 * <p>
 * Spring Batch 6 retries an item inside the chunk transaction it failed in, and a COB failure has by then marked that
 * transaction rollback-only. A retry that then succeeds hides the failure: scan mode never starts, and the chunk is
 * silently rolled back while being recorded as committed, leaving its accounts locked with no error. See
 * {@code StepRetryPolicyFactory}. This test fails if either worker step is given a policy that retries.
 */
class CobNoRetryTest {

    private RemotePartitioningWorkerStepBuilderFactory stepBuilderFactory;
    private PropertyService propertyService;

    @BeforeEach
    void setUp() {
        stepBuilderFactory = mock(RemotePartitioningWorkerStepBuilderFactory.class);
        RemotePartitioningWorkerStepBuilder workerStepBuilder = mock(RemotePartitioningWorkerStepBuilder.class);
        when(stepBuilderFactory.get(anyString())).thenReturn(workerStepBuilder);
        when(workerStepBuilder.inputChannel(any())).thenReturn(workerStepBuilder);
        doReturn(new StepBuilder("worker", mock(JobRepository.class)).chunk(10)).when(workerStepBuilder).chunk(anyInt());
        propertyService = mock(PropertyService.class);
        when(propertyService.getChunkSize(anyString())).thenReturn(10);
        when(propertyService.getSkipLimit(anyString())).thenReturn(11);
    }

    @Test
    void loanCobWorkerStepMakesASingleAttemptPerItem() throws Exception {
        LoanCOBWorkerConfiguration configuration = new LoanCOBWorkerConfiguration();
        for (Field field : LoanCOBWorkerConfiguration.class.getDeclaredFields()) {
            ReflectionTestUtils.setField(configuration, field.getName(), dependency(field.getType()));
        }

        assertMakesASingleAttemptPerItem(configuration.loanCOBWorkerStep());
    }

    @Test
    void workingCapitalLoanCobWorkerStepMakesASingleAttemptPerItem() throws Exception {
        Constructor<?> constructor = WorkingCapitalLoanCOBWorkerConfiguration.class.getDeclaredConstructors()[0];
        Object[] arguments = new Object[constructor.getParameterCount()];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = dependency(constructor.getParameterTypes()[i]);
        }
        WorkingCapitalLoanCOBWorkerConfiguration configuration = (WorkingCapitalLoanCOBWorkerConfiguration) constructor
                .newInstance(arguments);

        assertMakesASingleAttemptPerItem(configuration.workingCapitalLoanCOBWorkerStep(mock(WorkingCapitalLoanCOBWorkerItemReader.class),
                mock(WorkingCapitalLoanCOBWorkerItemWriter.class)));
    }

    private Object dependency(Class<?> type) {
        if (type == RemotePartitioningWorkerStepBuilderFactory.class) {
            return stepBuilderFactory;
        }
        if (type == PropertyService.class) {
            return propertyService;
        }
        return mock(type);
    }

    private static void assertMakesASingleAttemptPerItem(Step step) {
        assertThat(ReflectionTestUtils.getField(step, "faultTolerant")).as("the COB worker step must stay fault tolerant").isEqualTo(true);
        RetryPolicy retryPolicy = (RetryPolicy) ReflectionTestUtils.getField(step, "retryPolicy");
        assertThat(retryPolicy).isNotNull();
        boolean neverRetries = !retryPolicy.shouldRetry(new RuntimeException("failure"))
                || retryPolicy.getBackOff().start().nextBackOff() == BackOffExecution.STOP;
        assertThat(neverRetries).as("the COB worker step must not retry: Batch 6 would retry inside the doomed chunk transaction").isTrue();
    }
}
