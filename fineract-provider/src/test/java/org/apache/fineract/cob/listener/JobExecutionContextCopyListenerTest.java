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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;

@ExtendWith(MockitoExtension.class)
public class JobExecutionContextCopyListenerTest {

    private static final List<String> STEP_EXECUTION_KEYS = List.of("BusinessDate", "IS_CATCH_UP");

    private final JobExecutionContextCopyListener jobExecutionContextCopyListener = new JobExecutionContextCopyListener(
            List.of("BusinessDate", "IS_CATCH_UP"));

    @Test
    public void verifyJobExecutionContextMapIsCopiedToEmptyStepExecutionContextMap() {
        Map<String, Object> jobExecutionContextMap = createRandomHashmap();
        jobExecutionContextMap.put("BusinessDate", "value");
        jobExecutionContextMap.put("IS_CATCH_UP", "value");
        final JobExecution jobExecution = new JobExecution(1L);
        jobExecution.setExecutionContext(new ExecutionContext(jobExecutionContextMap));
        final StepExecution stepExecution = new StepExecution("someStep", jobExecution);

        jobExecutionContextCopyListener.beforeStep(stepExecution);

        assertEquals(stepExecution.getExecutionContext().size(), 2);
        stepExecution.getExecutionContext().toMap().forEach((key, value) -> {
            assertTrue(STEP_EXECUTION_KEYS.contains(key));
            assertEquals(value, jobExecutionContextMap.get(key));
        });
    }

    @Test
    public void verifyStepExecutionContextMapNotOverwritten() {
        Map<String, Object> jobExecutionContextMap = new HashMap<>();
        jobExecutionContextMap.put("BusinessDate", "BusinessDate value");
        jobExecutionContextMap.put("IS_CATCH_UP", "IS_CATCH_UP value");
        Map<String, Object> expectedStepExecutionContextMap = new HashMap<>();
        expectedStepExecutionContextMap.put("BusinessDate", "BusinessDate anotherValue");
        expectedStepExecutionContextMap.put("IS_CATCH_UP", "IS_CATCH_UP anotherValue");

        final JobExecution jobExecution = new JobExecution(1L);
        jobExecution.setExecutionContext(new ExecutionContext(jobExecutionContextMap));
        final StepExecution actualStepExecution = new StepExecution("someStep", jobExecution);
        actualStepExecution.setExecutionContext(new ExecutionContext(expectedStepExecutionContextMap));

        jobExecutionContextCopyListener.beforeStep(actualStepExecution);

        assertEquals(actualStepExecution.getExecutionContext().size(), STEP_EXECUTION_KEYS.size());
        actualStepExecution.getExecutionContext().toMap().forEach((key, value) -> {
            assertTrue(STEP_EXECUTION_KEYS.contains(key));
            assertEquals(value, expectedStepExecutionContextMap.get(key));
        });
    }

    /**
     * Helper method to create a random Hash Map.
     *
     * @return the random Hash Map.
     */
    private Map<String, Object> createRandomHashmap() {
        final Random random = new Random();
        final Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < random.nextInt(50); i++) {
            map.put(RandomStringUtils.randomAlphanumeric(5), RandomStringUtils.randomAlphanumeric(5));
        }

        return map;
    }
}
