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
package org.apache.fineract.infrastructure.sms.scheduler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.campaigns.helper.SmsConfigUtils;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignConstants;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.campaigns.sms.exception.ConnectionFailureException;
import org.apache.fineract.infrastructure.core.config.TaskExecutorConstant;
import org.apache.fineract.infrastructure.core.domain.FineractContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.gcm.service.NotificationSenderService;
import org.apache.fineract.infrastructure.sms.data.SmsMessageApiQueueResourceData;
import org.apache.fineract.infrastructure.sms.domain.SmsMessage;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageStatusType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Scheduled job services that send SMS messages and get delivery reports for the sent SMS messages
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class SmsMessageScheduledJobServiceImpl implements SmsMessageScheduledJobService {

    private final SmsMessageRepository smsMessageRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final SmsConfigUtils smsConfigUtils;
    private final NotificationSenderService notificationSenderService;
    @Qualifier(TaskExecutorConstant.DEFAULT_TASK_EXECUTOR_BEAN_NAME)
    private final ThreadPoolTaskExecutor taskExecutor;

    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    private void connectAndSendToIntermediateServer(Collection<SmsMessageApiQueueResourceData> apiQueueResourceDatas) {
        Map<String, Object> hostConfig = this.smsConfigUtils.getMessageGateWayRequestURI("sms",
                SmsMessageApiQueueResourceData.toJsonString(apiQueueResourceDatas));
        URI uri = (URI) hostConfig.get("uri");
        HttpEntity<?> entity = (HttpEntity<?>) hostConfig.get("entity");
        ResponseEntity<String> responseOne = restTemplate.exchange(uri, HttpMethod.POST, entity, new ParameterizedTypeReference<String>() {

        });
        if (responseOne != null) {
            // String smsResponse = responseOne.getBody();
            if (!responseOne.getStatusCode().equals(HttpStatus.ACCEPTED)) {
                log.debug("{}", responseOne.getStatusCode().value());
                throw new ConnectionFailureException(SmsCampaignConstants.SMS);
            }
        }
    }

    @Override
    public void sendTriggeredMessages(Map<SmsCampaign, Collection<SmsMessage>> smsDataMap) {
        try {
            if (!smsDataMap.isEmpty()) {
                List<SmsMessage> toSaveMessages = new ArrayList<>();
                List<SmsMessage> toSendNotificationMessages = new ArrayList<>();
                for (Map.Entry<SmsCampaign, Collection<SmsMessage>> entry : smsDataMap.entrySet()) {
                    Iterator<SmsMessage> smsMessageIterator = entry.getValue().iterator();
                    Collection<SmsMessageApiQueueResourceData> apiQueueResourceDatas = new ArrayList<>();
                    while (smsMessageIterator.hasNext()) {
                        SmsMessage smsMessage = smsMessageIterator.next();
                        if (smsMessage.isNotification()) {
                            smsMessage.setStatusType(SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue());
                            toSendNotificationMessages.add(smsMessage);
                        } else {
                            SmsMessageApiQueueResourceData apiQueueResourceData = SmsMessageApiQueueResourceData.instance(
                                    smsMessage.getId(), null, null, null, smsMessage.getMobileNo(), smsMessage.getMessage(),
                                    entry.getKey().getProviderId());
                            apiQueueResourceDatas.add(apiQueueResourceData);
                            smsMessage.setStatusType(SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue());
                            toSaveMessages.add(smsMessage);
                        }
                    }
                    if (toSaveMessages.size() > 0) {
                        this.smsMessageRepository.saveAll(toSaveMessages);
                        this.smsMessageRepository.flush();
                        this.taskExecutor.execute(new SmsTask(apiQueueResourceDatas, ThreadLocalContextUtil.getContext()));
                    }
                    if (!toSendNotificationMessages.isEmpty()) {
                        this.notificationSenderService.sendNotification(toSendNotificationMessages);
                    }

                }
            }
        } catch (Exception e) {
            log.error("Error occured.", e);
        }
    }

    @Override
    public void sendTriggeredMessage(Collection<SmsMessage> smsMessages, long providerId) {
        try {
            Collection<SmsMessageApiQueueResourceData> apiQueueResourceDatas = new ArrayList<>();
            StringBuilder request = new StringBuilder();
            for (SmsMessage smsMessage : smsMessages) {
                SmsMessageApiQueueResourceData apiQueueResourceData = SmsMessageApiQueueResourceData.instance(smsMessage.getId(), null,
                        null, null, smsMessage.getMobileNo(), smsMessage.getMessage(), providerId);
                apiQueueResourceDatas.add(apiQueueResourceData);
                smsMessage.setStatusType(SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue());
            }
            this.smsMessageRepository.saveAll(smsMessages);
            request.append(SmsMessageApiQueueResourceData.toJsonString(apiQueueResourceDatas));
            log.debug("Sending triggered SMS to specific provider with request - {}", request);
            this.taskExecutor.execute(new SmsTask(apiQueueResourceDatas, ThreadLocalContextUtil.getContext()));
        } catch (Exception e) {
            log.error("Error occured.", e);
        }
    }

    class SmsTask implements Runnable, ApplicationListener<ContextClosedEvent> {

        private final FineractContext context;
        private final Collection<SmsMessageApiQueueResourceData> apiQueueResourceDatas;

        SmsTask(final Collection<SmsMessageApiQueueResourceData> apiQueueResourceDatas, final FineractContext context) {
            this.context = context;
            this.apiQueueResourceDatas = apiQueueResourceDatas;
        }

        @Override
        public void run() {
            ThreadLocalContextUtil.init(context);
            connectAndSendToIntermediateServer(apiQueueResourceDatas);
        }

        @Override
        public void onApplicationEvent(ContextClosedEvent event) {
            taskExecutor.shutdown();
            log.info("Shutting down the ExecutorService");
        }
    }
}
