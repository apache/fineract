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

package org.apache.fineract.infrastructure.jobs.handler;

import io.github.resilience4j.retry.annotation.Retry;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.infrastructure.jobs.data.JobExecuteRequest;
import org.apache.fineract.infrastructure.jobs.data.JobExecuteResponse;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;
import org.apache.fineract.infrastructure.jobs.service.JobRegisterService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobExecuteCommandHandler implements CommandHandler<JobExecuteRequest, JobExecuteResponse> {

    private final JobRegisterService jobRegisterService;

    @Retry(name = "commandJobExecute", fallbackMethod = "fallback")
    @Override
    public JobExecuteResponse handle(Command<JobExecuteRequest> command) {
        JobExecuteRequest req = command.getPayload();
        List<JobParameterDTO> params = req.getJobParameters();
        Set<JobParameterDTO> paramSet = (params == null || params.isEmpty()) ? Set.of() : new HashSet<>(params);
        jobRegisterService.executeJobWithParameters(req.getJobId(), paramSet);
        return JobExecuteResponse.builder().resourceId(req.getJobId()).build();
    }

    @Override
    public JobExecuteResponse fallback(Command<JobExecuteRequest> command, Throwable t) {
        return CommandHandler.super.fallback(command, t);
    }
}
