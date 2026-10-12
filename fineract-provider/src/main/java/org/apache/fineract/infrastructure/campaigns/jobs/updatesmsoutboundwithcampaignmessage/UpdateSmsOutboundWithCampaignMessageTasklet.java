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
package org.apache.fineract.infrastructure.campaigns.jobs.updatesmsoutboundwithcampaignmessage;

import java.time.LocalDateTime;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignStatus;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaignRepository;
import org.apache.fineract.infrastructure.campaigns.sms.exception.SmsCampaignNotFound;
import org.apache.fineract.infrastructure.campaigns.sms.service.SmsCampaignWritePlatformService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class UpdateSmsOutboundWithCampaignMessageTasklet implements Tasklet {

    private final SmsCampaignRepository smsCampaignRepository;
    private final SmsCampaignWritePlatformService smsCampaignWritePlatformService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final Collection<SmsCampaign> smsCampaignDataCollection = smsCampaignRepository
                .findByTriggerTypeAndStatus(SmsCampaignTriggerType.SCHEDULE.getValue(), SmsCampaignStatus.ACTIVE.getValue());
        if (!CollectionUtils.isEmpty(smsCampaignDataCollection)) {
            for (SmsCampaign smsCampaign : smsCampaignDataCollection) {
                LocalDateTime tenantDateNow = DateUtils.getLocalDateTimeOfTenant();
                LocalDateTime nextTriggerDate = smsCampaign.getNextTriggerDate();

                log.debug("tenant time {} trigger time {} {}", tenantDateNow, nextTriggerDate,
                        JobName.UPDATE_SMS_OUTBOUND_WITH_CAMPAIGN_MESSAGE.name());
                if (DateUtils.isBefore(nextTriggerDate, tenantDateNow)) {
                    smsCampaignWritePlatformService.insertDirectCampaignIntoSmsOutboundTable(smsCampaign);
                    updateTriggerDates(smsCampaign.getId());
                }
            }
        }
        return RepeatStatus.FINISHED;
    }

    private void updateTriggerDates(Long campaignId) {
        final SmsCampaign smsCampaign = this.smsCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new SmsCampaignNotFound(campaignId));
        LocalDateTime nextTriggerDate = smsCampaign.getNextTriggerDate();
        smsCampaign.setLastTriggerDate(nextTriggerDate);
        LocalDateTime nextRuntime = CalendarUtils.getNextRecurringDate(smsCampaign.getRecurrence(), nextTriggerDate, nextTriggerDate);
        LocalDateTime tenantDateTime = DateUtils.getLocalDateTimeOfTenant();
        if (DateUtils.isBefore(nextRuntime, tenantDateTime)) {
            nextRuntime = CalendarUtils.getNextRecurringDate(smsCampaign.getRecurrence(), nextTriggerDate, tenantDateTime);
        }

        smsCampaign.setNextTriggerDate(nextRuntime);
        this.smsCampaignRepository.saveAndFlush(smsCampaign);
    }
}
