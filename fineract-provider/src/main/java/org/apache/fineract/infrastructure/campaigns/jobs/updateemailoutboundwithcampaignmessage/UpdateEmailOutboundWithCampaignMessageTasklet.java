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
package org.apache.fineract.infrastructure.campaigns.jobs.updateemailoutboundwithcampaignmessage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailCampaignData;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaign;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaignRepository;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessage;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessageRepository;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailCampaignNotFound;
import org.apache.fineract.infrastructure.campaigns.email.service.EmailCampaignReadPlatformService;
import org.apache.fineract.infrastructure.campaigns.email.service.EmailCampaignWritePlatformService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class UpdateEmailOutboundWithCampaignMessageTasklet implements Tasklet {

    private final EmailCampaignReadPlatformService emailCampaignReadPlatformService;
    private final EmailCampaignRepository emailCampaignRepository;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailCampaignWritePlatformService emailCampaignWritePlatformService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final Collection<EmailCampaignData> emailCampaignDataCollection = emailCampaignReadPlatformService
                .retrieveAllScheduleActiveCampaign();
        if (emailCampaignDataCollection != null) {
            for (EmailCampaignData emailCampaignData : emailCampaignDataCollection) {
                LocalDateTime tenantDateNow = DateUtils.getLocalDateTimeOfTenant();
                LocalDateTime nextTriggerDate = emailCampaignData.getNextTriggerDate().toLocalDateTime();

                log.debug("tenant time {} trigger time {}", tenantDateNow, nextTriggerDate);
                if (DateUtils.isBefore(nextTriggerDate, tenantDateNow)) {
                    insertDirectCampaignIntoEmailOutboundTable(emailCampaignData.getParamValue(), emailCampaignData.getEmailSubject(),
                            emailCampaignData.getEmailMessage(), emailCampaignData.getCampaignName(), emailCampaignData.getId());
                    updateTriggerDates(emailCampaignData.getId());
                }
            }
        }
        return RepeatStatus.FINISHED;
    }

    private void insertDirectCampaignIntoEmailOutboundTable(final String emailParams, final String emailSubject,
            final String messageTemplate, final String campaignName, final Long campaignId) {
        try {
            HashMap<String, String> campaignParams = new ObjectMapper().readValue(emailParams, new TypeReference<>() {});
            HashMap<String, String> queryParamForRunReport = new ObjectMapper().readValue(emailParams, new TypeReference<>() {});
            List<HashMap<String, Object>> runReportObject = emailCampaignWritePlatformService
                    .getRunReportByServiceImpl(campaignParams.get("reportName"), queryParamForRunReport);
            if (runReportObject != null) {
                for (HashMap<String, Object> entry : runReportObject) {
                    String message = compileEmailTemplate(messageTemplate, campaignName, entry);
                    Integer clientId = (Integer) entry.get("id");
                    EmailCampaign emailCampaign = emailCampaignRepository.findById(campaignId).orElse(null);
                    Client client = clientRepositoryWrapper.findOneWithNotFoundDetection(clientId.longValue());
                    String emailAddress = client.emailAddress();

                    if (emailAddress != null && isValidEmail(emailAddress)) {
                        EmailMessage emailMessage = EmailMessage.pendingEmail(null, client, null, emailCampaign, emailSubject, message,
                                emailAddress, campaignName);
                        emailMessageRepository.save(emailMessage);
                    }
                }
            }
        } catch (IOException e) {
            throw new EmailParamMappingException(e);
        }
    }

    private void updateTriggerDates(Long campaignId) {
        final EmailCampaign emailCampaign = emailCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new EmailCampaignNotFound(campaignId));
        LocalDateTime nextTriggerDate = emailCampaign.getNextTriggerDate();
        emailCampaign.setLastTriggerDate(nextTriggerDate);
        LocalDateTime newTriggerDateWithTime = CalendarUtils.getNextRecurringDate(emailCampaign.getRecurrence(), nextTriggerDate,
                nextTriggerDate);
        LocalDateTime tenantDateTime = DateUtils.getLocalDateTimeOfTenant();
        if (DateUtils.isBefore(newTriggerDateWithTime, tenantDateTime)) {
            newTriggerDateWithTime = CalendarUtils.getNextRecurringDate(emailCampaign.getRecurrence(), nextTriggerDate, tenantDateTime);
        }
        emailCampaign.setNextTriggerDate(newTriggerDateWithTime);
        emailCampaignRepository.saveAndFlush(emailCampaign);
    }

    private String compileEmailTemplate(final String textMessageTemplate, final String campaignName,
            final Map<String, Object> emailParams) {
        final MustacheFactory mf = new DefaultMustacheFactory();
        final Mustache mustache = mf.compile(new StringReader(textMessageTemplate), campaignName);

        final StringWriter stringWriter = new StringWriter();
        mustache.execute(stringWriter, emailParams);

        return stringWriter.toString();
    }

    public static boolean isValidEmail(String email) {
        boolean isValid = true;
        try {
            InternetAddress emailO = new InternetAddress(email);
            emailO.validate();
        } catch (AddressException ex) {
            isValid = false;
        }
        return isValid;
    }
}
