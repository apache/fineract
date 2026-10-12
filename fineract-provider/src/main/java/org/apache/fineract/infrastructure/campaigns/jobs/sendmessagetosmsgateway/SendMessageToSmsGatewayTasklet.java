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
package org.apache.fineract.infrastructure.campaigns.jobs.sendmessagetosmsgateway;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.fineract.infrastructure.campaigns.helper.SmsConfigUtils;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignConstants;
import org.apache.fineract.infrastructure.campaigns.sms.exception.ConnectionFailureException;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.gcm.service.NotificationSenderService;
import org.apache.fineract.infrastructure.sms.data.SmsMessageApiQueueResourceData;
import org.apache.fineract.infrastructure.sms.domain.SmsMessage;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageStatusType;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
public class SendMessageToSmsGatewayTasklet implements Tasklet {

    private final SmsMessageRepository smsMessageRepository;
    private final NotificationSenderService notificationSenderService;
    private final SmsConfigUtils smsConfigUtils;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        int pageLimit = 200;
        int page = 0;
        int totalRecords;
        do {
            PageRequest pageRequest = PageRequest.of(0, pageLimit);
            org.springframework.data.domain.Page<SmsMessage> pendingMessages = smsMessageRepository
                    .findByStatusType(SmsMessageStatusType.PENDING.getValue(), pageRequest);
            List<SmsMessage> toSaveMessages = new ArrayList<>();
            List<SmsMessage> toSendNotificationMessages = new ArrayList<>();
            try {
                if (!CollectionUtils.isEmpty(pendingMessages.getContent())) {
                    final String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
                    Iterator<SmsMessage> pendingMessageIterator = pendingMessages.iterator();
                    Collection<SmsMessageApiQueueResourceData> apiQueueResourceDataCollection = new ArrayList<>();
                    while (pendingMessageIterator.hasNext()) {
                        SmsMessage smsData = pendingMessageIterator.next();
                        if (smsData.isNotification()) {
                            smsData.setStatusType(SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue());
                            toSendNotificationMessages.add(smsData);
                        } else {
                            SmsMessageApiQueueResourceData apiQueueResourceData = SmsMessageApiQueueResourceData.instance(smsData.getId(),
                                    tenantIdentifier, null, null, smsData.getMobileNo(), smsData.getMessage(),
                                    smsData.getSmsCampaign().getProviderId());
                            apiQueueResourceDataCollection.add(apiQueueResourceData);
                            smsData.setStatusType(SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue());
                            toSaveMessages.add(smsData);
                        }
                    }
                    if (!toSaveMessages.isEmpty()) {
                        smsMessageRepository.saveAll(toSaveMessages);
                        smsMessageRepository.flush();
                        taskExecutor.execute(new SmsTask(ThreadLocalContextUtil.getTenant(), apiQueueResourceDataCollection));
                    }
                    if (!toSendNotificationMessages.isEmpty()) {
                        notificationSenderService.sendNotification(toSendNotificationMessages);
                    }
                }
            } catch (Exception e) {
                throw new ConnectionFailureException(SmsCampaignConstants.SMS, e);
            }
            page++;
            totalRecords = pendingMessages.getTotalPages();
        } while (page < totalRecords);
        return RepeatStatus.FINISHED;
    }

    class SmsTask implements Runnable, ApplicationListener<ContextClosedEvent> {

        private final FineractPlatformTenant tenant;
        private final Collection<SmsMessageApiQueueResourceData> apiQueueResourceDatas;

        SmsTask(final FineractPlatformTenant tenant, final Collection<SmsMessageApiQueueResourceData> apiQueueResourceDatas) {
            this.tenant = tenant;
            this.apiQueueResourceDatas = apiQueueResourceDatas;
        }

        @Override
        public void run() {
            ThreadLocalContextUtil.setTenant(tenant);
            connectAndSendToIntermediateServer(apiQueueResourceDatas);
        }

        @Override
        public void onApplicationEvent(ContextClosedEvent event) {
            taskExecutor.shutdown();
            log.info("Shutting down the ExecutorService");
        }
    }

    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    private void connectAndSendToIntermediateServer(Collection<SmsMessageApiQueueResourceData> apiQueueResourceDatas) {
        Map<String, Object> hostConfig = smsConfigUtils.getMessageGateWayRequestURI("sms",
                SmsMessageApiQueueResourceData.toJsonString(apiQueueResourceDatas));
        URI uri = (URI) hostConfig.get("uri");
        HttpEntity<?> entity = (HttpEntity<?>) hostConfig.get("entity");
        ResponseEntity<String> responseOne = restTemplate.exchange(uri, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});
        if (!responseOne.getStatusCode().equals(HttpStatus.ACCEPTED)) {
            log.debug("{}", responseOne.getStatusCode().value());
            throw new ConnectionFailureException(SmsCampaignConstants.SMS);
        }
    }
}
