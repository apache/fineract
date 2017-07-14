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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.fineract.infrastructure.campaigns.helper.SmsConfigUtils;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignConstants;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.campaigns.sms.exception.ConnectionFailureException;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.sms.data.SmsMessageApiQueueResourceData;
import org.apache.fineract.infrastructure.sms.data.SmsMessageDeliveryReportData;
import org.apache.fineract.infrastructure.sms.domain.SmsMessage;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageStatusType;
import org.apache.fineract.infrastructure.sms.service.SmsReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

/**
 * Scheduled job services that send SMS messages and get delivery reports for
 * the sent SMS messages
 **/
@Service
public class SmsMessageScheduledJobServiceImpl implements SmsMessageScheduledJobService {

    private final SmsMessageRepository smsMessageRepository;
    private final SmsReadPlatformService smsReadPlatformService;
    private static final Logger logger = LoggerFactory.getLogger(SmsMessageScheduledJobServiceImpl.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private  ExecutorService genericExecutorService ;
    private ExecutorService triggeredExecutorService ;
    private final SmsConfigUtils smsConfigUtils ;
    
    
    /**
     * SmsMessageScheduledJobServiceImpl constructor
     **/
    @Autowired
    public SmsMessageScheduledJobServiceImpl(SmsMessageRepository smsMessageRepository, SmsReadPlatformService smsReadPlatformService,
            final SmsConfigUtils smsConfigUtils) {
        this.smsMessageRepository = smsMessageRepository;
        this.smsReadPlatformService = smsReadPlatformService;
        this.smsConfigUtils = smsConfigUtils ;
    }

    @PostConstruct
    public void initializeExecutorService() {
        genericExecutorService = Executors.newSingleThreadExecutor();
        triggeredExecutorService = Executors.newSingleThreadExecutor() ;
    }

    /**
     * Send batches of SMS messages to the SMS gateway (or intermediate gateway)
     **/
    @Override
    @Transactional
    @CronTarget(jobName = JobName.SEND_MESSAGES_TO_SMS_GATEWAY)
    public void sendMessagesToGateway() {
        Integer pageLimit = 200;
        Integer page = 0;
        int totalRecords = 0;
        do {
            PageRequest pageRequest = new PageRequest(0, pageLimit);
            org.springframework.data.domain.Page<SmsMessage> pendingMessages = this.smsMessageRepository.findByStatusType(
                    SmsMessageStatusType.PENDING.getValue(), pageRequest);
            List<SmsMessage> toSaveMessages = new ArrayList<>() ;
            try {

                if (pendingMessages.getContent().size() > 0) {
                    final String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
                    Iterator<SmsMessage> pendingMessageIterator = pendingMessages.iterator();
                    Collection<SmsMessageApiQueueResourceData> apiQueueResourceDatas = new ArrayList<>();
                    while (pendingMessageIterator.hasNext()) {
                        SmsMessage smsData = pendingMessageIterator.next();

                        SmsMessageApiQueueResourceData apiQueueResourceData = SmsMessageApiQueueResourceData.instance(smsData.getId(),
                                tenantIdentifier, null, null, smsData.getMobileNo(), smsData.getMessage(), smsData.getSmsCampaign()
                                        .getProviderId());
                        apiQueueResourceDatas.add(apiQueueResourceData);
                        smsData.setStatusType(SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue());
                        toSaveMessages.add(smsData) ;
                    }
                    this.smsMessageRepository.save(toSaveMessages);
                    this.smsMessageRepository.flush();
                    this.genericExecutorService.execute(new SmsTask(ThreadLocalContextUtil.getTenant(), apiQueueResourceDatas));

//                    new MyThread(ThreadLocalContextUtil.getTenant(), apiQueueResourceDatas).start();
                }
            } catch (Exception e) {
                throw new ConnectionFailureException(SmsCampaignConstants.SMS);
            }
            page ++;
            totalRecords = pendingMessages.getTotalPages();
        } while (page < totalRecords);
    }

    class SmsTask implements Runnable, ApplicationListener<ContextClosedEvent> {

        private final FineractPlatformTenant tenant;
        private final Collection<SmsMessageApiQueueResourceData> apiQueueResourceDatas;

        public SmsTask(final FineractPlatformTenant tenant, final Collection<SmsMessageApiQueueResourceData> apiQueueResourceDatas) {
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
            genericExecutorService.shutdown();
            logger.info("Shutting down the ExecutorService");
        }
    }

    private void connectAndSendToIntermediateServer(Collection<SmsMessageApiQueueResourceData> apiQueueResourceDatas) {
    	Map<String, Object> hostConfig = this.smsConfigUtils.getMessageGateWayRequestURI("sms", SmsMessageApiQueueResourceData.toJsonString(apiQueueResourceDatas)) ;
        URI uri = (URI)hostConfig.get("uri") ;
        HttpEntity<?> entity = (HttpEntity<?>)hostConfig.get("entity") ;
        ResponseEntity<String> responseOne = restTemplate.exchange(uri, HttpMethod.POST, entity,
                new ParameterizedTypeReference<String>() {});
        if (responseOne != null) {
//            String smsResponse = responseOne.getBody();
            if (!responseOne.getStatusCode().equals(HttpStatus.ACCEPTED)) {
            	logger.debug(responseOne.getStatusCode().name());
                throw new ConnectionFailureException(SmsCampaignConstants.SMS);
            }
        }
    }

    @Override
    public void sendTriggeredMessages(Map<SmsCampaign, Collection<SmsMessage>> smsDataMap) {
        try {
            if (!smsDataMap.isEmpty()) {
                for (Entry<SmsCampaign, Collection<SmsMessage>> entry : smsDataMap.entrySet()) {
                    Iterator<SmsMessage> smsMessageIterator = entry.getValue().iterator();
                    Collection<SmsMessageApiQueueResourceData> apiQueueResourceDatas = new ArrayList<>();
                    StringBuilder request = new StringBuilder();
                    while (smsMessageIterator.hasNext()) {
                        SmsMessage smsMessage = smsMessageIterator.next();
                        SmsMessageApiQueueResourceData apiQueueResourceData = SmsMessageApiQueueResourceData.instance(smsMessage.getId(),
                                null, null, null, smsMessage.getMobileNo(), smsMessage.getMessage(), entry.getKey().getProviderId());
                        apiQueueResourceDatas.add(apiQueueResourceData);
                        smsMessage.setStatusType(SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue());
                    }
                    this.smsMessageRepository.save(entry.getValue()) ;
                    request.append(SmsMessageApiQueueResourceData.toJsonString(apiQueueResourceDatas));
                    logger.info("Sending triggered SMS with request - " + request.toString());
                    this.triggeredExecutorService.execute(new SmsTask(ThreadLocalContextUtil.getTenant(), apiQueueResourceDatas));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void sendTriggeredMessage(Collection<SmsMessage> smsMessages, long providerId) {
        try {
            Collection<SmsMessageApiQueueResourceData> apiQueueResourceDatas = new ArrayList<>();
            StringBuilder request = new StringBuilder();
            for(SmsMessage smsMessage : smsMessages) {
                SmsMessageApiQueueResourceData apiQueueResourceData =
                        SmsMessageApiQueueResourceData.instance(smsMessage.getId(), null,
                                null, null, smsMessage.getMobileNo(),
                                smsMessage.getMessage(), providerId);
                apiQueueResourceDatas.add(apiQueueResourceData);
                smsMessage.setStatusType(SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue());
            }
            this.smsMessageRepository.save(smsMessages);
            request.append(SmsMessageApiQueueResourceData.toJsonString(apiQueueResourceDatas));
            logger.info("Sending triggered SMS to specific provider with request - " + request.toString());
            this.triggeredExecutorService.execute(new SmsTask(ThreadLocalContextUtil.getTenant(),
                    apiQueueResourceDatas));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * get SMS message delivery reports from the SMS gateway (or intermediate
     * gateway)
     **/
    @Override
    @Transactional
    @CronTarget(jobName = JobName.GET_DELIVERY_REPORTS_FROM_SMS_GATEWAY)
    public void getDeliveryReports() {
        int page = 0;
        int totalRecords = 0;
        Integer limit = 200;
        do {
            Page<Long> smsMessageInternalIds = this.smsReadPlatformService.retrieveAllWaitingForDeliveryReport(limit);
            // only proceed if there are sms message with status type enum 300
            try {

                if (smsMessageInternalIds.getPageItems().size() > 0) {
                    // make request
                	Map<String, Object> hostConfig = this.smsConfigUtils.getMessageGateWayRequestURI("sms/report", new Gson().toJson(smsMessageInternalIds.getPageItems())) ;
                    URI uri = (URI)hostConfig.get("uri") ;
                    HttpEntity<?> entity = (HttpEntity<?>)hostConfig.get("entity") ;
                    ResponseEntity<Collection<SmsMessageDeliveryReportData>> responseOne = restTemplate.exchange(uri, HttpMethod.POST, entity,
                            new ParameterizedTypeReference<Collection<SmsMessageDeliveryReportData>>() {});

                    Collection<SmsMessageDeliveryReportData> smsMessageDeliveryReportDatas = responseOne.getBody();
                    Iterator<SmsMessageDeliveryReportData> responseReportIterator = smsMessageDeliveryReportDatas.iterator();
                    while (responseReportIterator.hasNext()) {
                        SmsMessageDeliveryReportData smsMessageDeliveryReportData = responseReportIterator.next();
                        Integer deliveryStatus = smsMessageDeliveryReportData.getDeliveryStatus();

                        if (!smsMessageDeliveryReportData.getHasError()
                                && (deliveryStatus != 100)) {
                            SmsMessage smsMessage = this.smsMessageRepository.findOne(smsMessageDeliveryReportData.getId());
                            Integer statusType = smsMessage.getStatusType();
                            boolean statusChanged = false;

                            switch (deliveryStatus) {
                                case 0:
                                    statusType = SmsMessageStatusType.INVALID.getValue();
                                break;
                                case 150:
                                    statusType = SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue();
                                break;
                                case 200:
                                    statusType = SmsMessageStatusType.SENT.getValue();
                                break;
                                case 300:
                                    statusType = SmsMessageStatusType.DELIVERED.getValue();
                                break;

                                case 400:
                                    statusType = SmsMessageStatusType.FAILED.getValue();
                                break;

                                default:
                                    statusType = smsMessage.getStatusType();
                                break;
                            }

                            statusChanged = !statusType.equals(smsMessage.getStatusType());

                            // update the status Type enum
                            smsMessage.setStatusType(statusType);

                            // update the externalId
                            smsMessage.setExternalId(smsMessageDeliveryReportData.getExternalId());

                            // save the SmsMessage entity
                            this.smsMessageRepository.save(smsMessage);

                            if (statusChanged) {
                                logger.info("Status of SMS message id: " + smsMessage.getId() + " successfully changed to " + statusType);
                            }
                        }
                    }

                    if (smsMessageDeliveryReportDatas.size() > 0) {
                        logger.info(smsMessageDeliveryReportDatas.size() + " "
                                + "delivery report(s) successfully received from the intermediate gateway - sms");
                    }
                }
            }

            catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            page ++;
            totalRecords = smsMessageInternalIds.getTotalFilteredRecords();
        } while (page < totalRecords);
    }
}
