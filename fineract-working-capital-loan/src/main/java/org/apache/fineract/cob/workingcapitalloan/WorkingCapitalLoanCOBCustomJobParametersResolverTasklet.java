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
package org.apache.fineract.cob.workingcapitalloan;

import static org.apache.fineract.cob.COBConstant.BUSINESS_DATE_PARAMETER_NAME;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.common.CustomJobParameterResolver;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
public class WorkingCapitalLoanCOBCustomJobParametersResolverTasklet implements Tasklet {

    private final CustomJobParameterResolver customJobParameterResolver;

    @Nullable
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        customJobParameterResolver.resolve(contribution, chunkContext, BUSINESS_DATE_PARAMETER_NAME, BUSINESS_DATE_PARAMETER_NAME);
        return RepeatStatus.FINISHED;
    }
}
