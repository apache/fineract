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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameter;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameterRepository;
import org.apache.fineract.infrastructure.springbatch.SpringBatchJobConstants;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;

@Slf4j
@RequiredArgsConstructor
public class InlineLoanCOBBuildExecutionContextTasklet implements Tasklet, InitializingBean {

    private final GoogleGsonSerializerHelper gsonFactory;
    private final COBBusinessStepService cobBusinessStepService;
    private final CustomJobParameterRepository customJobParameterRepository;

    private Gson gson;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.gson = gsonFactory.createSimpleGson();
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        ThreadLocalContextUtil.setActionContext(ActionContext.COB);
        TreeMap<Long, String> cobBusinessStepMap = cobBusinessStepService.getCOBBusinessStepMap(LoanCOBBusinessStep.class,
                LoanCOBConstant.LOAN_COB_JOB_NAME);
        contribution.getStepExecution().getExecutionContext().put(LoanCOBConstant.LOAN_IDS, getLoanIdsFromJobParameters(chunkContext));
        contribution.getStepExecution().getExecutionContext().put(LoanCOBConstant.BUSINESS_STEP_MAP, cobBusinessStepMap);

        return RepeatStatus.FINISHED;
    }

    private List<Long> getLoanIdsFromJobParameters(ChunkContext chunkContext) {
        Long customJobParameterId = (Long) chunkContext.getStepContext().getJobParameters()
                .get(SpringBatchJobConstants.CUSTOM_JOB_PARAMETER_ID_KEY);
        CustomJobParameter customJobParameter = customJobParameterRepository.findById(customJobParameterId)
                .orElseThrow(() -> new LoanNotFoundException(customJobParameterId));
        String parameterJson = customJobParameter.getParameterJson();
        return gson.fromJson(parameterJson, new TypeToken<ArrayList<Long>>() {}.getType());
    }
}
