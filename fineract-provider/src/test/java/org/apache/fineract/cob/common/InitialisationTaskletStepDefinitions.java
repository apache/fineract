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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.cucumber.java8.En;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import org.apache.fineract.cob.loan.LoanCOBConstant;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.security.core.context.SecurityContextHolder;

@SuppressFBWarnings(value = "RV_EXCEPTION_NOT_THROWN", justification = "False positive")
public class InitialisationTaskletStepDefinitions implements En {

    private static final LocalDate TODAY = LocalDate.now(ZoneId.systemDefault());

    private AppUserRepositoryWrapper userRepository = mock(AppUserRepositoryWrapper.class);

    private InitialisationTasklet initialisationTasklet = new InitialisationTasklet(userRepository);

    private AppUser appUser = mock(AppUser.class);
    private RepeatStatus resultItem;
    private ChunkContext chunkContext;

    public InitialisationTaskletStepDefinitions() {
        Given("/^The InitialisationTasklet.execute method with action (.*)$/", (String action) -> {

            if ("error".equals(action)) {
                lenient().when(this.userRepository.fetchSystemUser()).thenThrow(new RuntimeException("fail"));
            } else {
                lenient().when(this.userRepository.fetchSystemUser()).thenReturn(appUser);
            }
            HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
            LocalDate businessDate = TODAY;
            LocalDate cobBusinessDate = businessDate.minusDays(1);
            businessDates.put(BusinessDateType.BUSINESS_DATE, businessDate);
            businessDates.put(BusinessDateType.COB_DATE, cobBusinessDate);
            ThreadLocalContextUtil.setBusinessDates(businessDates);
            JobExecution jobExecution = new JobExecution(1L);
            jobExecution.getExecutionContext().put(LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME,
                    cobBusinessDate.format(DateTimeFormatter.ISO_DATE));
            StepExecution stepExecution = new StepExecution("step", jobExecution);
            StepContext stepContext = new StepContext(stepExecution);
            chunkContext = new ChunkContext(stepContext);
        });

        When("InitialisationTasklet.execute method executed", () -> {
            resultItem = this.initialisationTasklet.execute(null, chunkContext);
        });

        Then("InitialisationTasklet.execute result should match", () -> {
            assertEquals(RepeatStatus.FINISHED, resultItem);
            assertEquals(appUser, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            assertEquals(TODAY, ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.BUSINESS_DATE));
            assertEquals(TODAY.minusDays(1), ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE));
            ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        });

        Then("throw exception InitialisationTasklet.execute method", () -> {
            assertThrows(RuntimeException.class, () -> {
                resultItem = this.initialisationTasklet.execute(null, null);
            });
            assertEquals(TODAY, ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.BUSINESS_DATE));
            assertEquals(TODAY.minusDays(1), ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE));
            ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        });
    }
}
