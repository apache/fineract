
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.batch.repeat.RepeatStatus.FINISHED;

import com.acme.fineract.loan.job.AcmeNoopJobTasklet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
public class AcmeNoopJobTaskletTest {

    @Mock
    private StepContribution stepContribution;
    @Mock
    private ChunkContext chunkContext;
    private RepeatStatus resultStatus;
    private AcmeNoopJobTasklet underTest;

    @BeforeEach
    public void setUp() {
        underTest = new AcmeNoopJobTasklet();
    }

    @Test
    public void testJobExecution() throws Exception {
        resultStatus = underTest.execute(stepContribution, chunkContext);
        assertEquals(FINISHED, resultStatus);
    }

}
