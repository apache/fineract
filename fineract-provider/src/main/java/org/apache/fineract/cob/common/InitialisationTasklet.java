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
package org.apache.fineract.cob.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.loan.LoanCOBConstant;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@RequiredArgsConstructor
public class InitialisationTasklet implements Tasklet {

    private final AppUserRepositoryWrapper userRepository;

    @Override
    public RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) throws Exception {
        HashMap<BusinessDateType, LocalDate> businessDates = ThreadLocalContextUtil.getBusinessDates();
        AppUser user = userRepository.fetchSystemUser();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        ThreadLocalContextUtil.setActionContext(ActionContext.COB);
        String businessDateString = Objects.requireNonNull((String) chunkContext.getStepContext().getStepExecution().getJobExecution()
                .getExecutionContext().get(LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME));
        LocalDate businessDate = LocalDate.parse(businessDateString, DateTimeFormatter.ISO_DATE);
        businessDates.put(BusinessDateType.COB_DATE, businessDate);
        businessDates.put(BusinessDateType.BUSINESS_DATE, businessDate.plusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);
        log.debug("Initialisation with Business Date [{}], COB Date [{}] and Action Context [{}]", businessDate.plusDays(1), businessDate,
                ThreadLocalContextUtil.getActionContext());
        return RepeatStatus.FINISHED;
    }
}
