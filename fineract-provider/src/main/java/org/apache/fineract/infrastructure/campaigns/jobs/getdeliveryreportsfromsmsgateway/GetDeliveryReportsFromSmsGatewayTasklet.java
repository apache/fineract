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
package org.apache.fineract.infrastructure.campaigns.jobs.getdeliveryreportsfromsmsgateway;

import com.google.gson.Gson;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.fineract.infrastructure.campaigns.helper.SmsConfigUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.sms.data.SmsMessageDeliveryReportData;
import org.apache.fineract.infrastructure.sms.domain.SmsMessage;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageStatusType;
import org.apache.fineract.infrastructure.sms.service.SmsReadPlatformService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
public class GetDeliveryReportsFromSmsGatewayTasklet implements Tasklet {

    private final SmsReadPlatformService smsReadPlatformService;
    private final SmsConfigUtils smsConfigUtils;
    private final SmsMessageRepository smsMessageRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        int page = 0;
        int totalRecords;
        Integer limit = 200;
        do {
            Page<Long> smsMessageInternalIds = smsReadPlatformService.retrieveAllWaitingForDeliveryReport(limit);
            try {
                if (!CollectionUtils.isEmpty(smsMessageInternalIds.getPageItems())) {
                    Map<String, Object> hostConfig = smsConfigUtils.getMessageGateWayRequestURI("sms/report",
                            new Gson().toJson(smsMessageInternalIds.getPageItems()));
                    URI uri = (URI) hostConfig.get("uri");
                    HttpEntity<?> entity = (HttpEntity<?>) hostConfig.get("entity");
                    ResponseEntity<Collection<SmsMessageDeliveryReportData>> responseOne = restTemplate.exchange(uri, HttpMethod.POST,
                            entity, new ParameterizedTypeReference<>() {});

                    Collection<SmsMessageDeliveryReportData> smsMessageDeliveryReportDataCollection = responseOne.getBody();
                    if (!CollectionUtils.isEmpty(smsMessageDeliveryReportDataCollection)) {
                        for (SmsMessageDeliveryReportData smsMessageDeliveryReportData : smsMessageDeliveryReportDataCollection) {
                            Integer deliveryStatus = smsMessageDeliveryReportData.getDeliveryStatus();

                            if (!smsMessageDeliveryReportData.getHasError() && deliveryStatus != 100) {
                                SmsMessage smsMessage = smsMessageRepository.findById(smsMessageDeliveryReportData.getId()).orElse(null);
                                Integer statusType = switch (deliveryStatus) {
                                    case 0 -> SmsMessageStatusType.INVALID.getValue();
                                    case 150 -> SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue();
                                    case 200 -> SmsMessageStatusType.SENT.getValue();
                                    case 300 -> SmsMessageStatusType.DELIVERED.getValue();
                                    case 400 -> SmsMessageStatusType.FAILED.getValue();
                                    default -> smsMessage.getStatusType();
                                };

                                boolean statusChanged = !statusType.equals(smsMessage.getStatusType());
                                smsMessage.setStatusType(statusType);
                                smsMessage.setExternalId(smsMessageDeliveryReportData.getExternalId());
                                smsMessageRepository.save(smsMessage);

                                if (statusChanged) {
                                    log.debug("Status of SMS message id: {} successfully changed to {}", smsMessage.getId(), statusType);
                                }
                            }
                        }
                    }
                    if (!CollectionUtils.isEmpty(smsMessageDeliveryReportDataCollection)) {
                        log.debug("{} delivery report(s) successfully received from the intermediate gateway - sms",
                                smsMessageDeliveryReportDataCollection.size());
                    }
                }
            }

            catch (Exception e) {
                log.error("Error occurred.", e);
            }
            page++;
            totalRecords = smsMessageInternalIds.getTotalFilteredRecords();
        } while (page < totalRecords);
        return RepeatStatus.FINISHED;
    }
}
