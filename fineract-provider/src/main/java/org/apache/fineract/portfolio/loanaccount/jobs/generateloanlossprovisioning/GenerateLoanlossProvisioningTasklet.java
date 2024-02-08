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
package org.apache.fineract.portfolio.loanaccount.jobs.generateloanlossprovisioning;

import java.time.LocalDate;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.fineract.accounting.provisioning.exception.ProvisioningEntryAlreadyCreatedException;
import org.apache.fineract.accounting.provisioning.service.ProvisioningEntriesWritePlatformService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.provisioning.data.ProvisioningCriteriaData;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCriteriaReadPlatformService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class GenerateLoanlossProvisioningTasklet implements Tasklet {

    private final ProvisioningCriteriaReadPlatformService provisioningCriteriaReadPlatformService;
    private final ProvisioningEntriesWritePlatformService provisioningEntriesWritePlatformService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LocalDate currentDate = DateUtils.getBusinessLocalDate();
        boolean addJournalEntries = true;
        try {
            Collection<ProvisioningCriteriaData> criteriaCollection = provisioningCriteriaReadPlatformService
                    .retrieveAllProvisioningCriterias();
            if (CollectionUtils.isNotEmpty(criteriaCollection)) {
                provisioningEntriesWritePlatformService.createProvisioningEntry(currentDate, addJournalEntries);
            }
        } catch (ProvisioningEntryAlreadyCreatedException e) {
            log.error("Provisioning entry already created", e);
        } catch (Exception e) {
            log.error("Problem occurred when generating provisioning entries", e);
        }
        return RepeatStatus.FINISHED;
    }
}
