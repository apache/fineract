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
package org.apache.fineract.infrastructure.campaigns.email.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaign;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaignRepository;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants;
import org.apache.fineract.portfolio.common.service.BusinessEventListener;
import org.apache.fineract.portfolio.common.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailCampaignDomainServiceImpl implements EmailCampaignDomainService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailCampaignDomainServiceImpl.class);
    private final BusinessEventNotifierService businessEventNotifierService;
    private final EmailCampaignWritePlatformService emailCampaignWritePlatformService;
    private final EmailCampaignRepository emailCampaignRepository;

    @Autowired
    public EmailCampaignDomainServiceImpl(BusinessEventNotifierService businessEventNotifierService,
            EmailCampaignWritePlatformService emailCampaignWritePlatformService, EmailCampaignRepository emailCampaignRepository) {
        this.businessEventNotifierService = businessEventNotifierService;
        this.emailCampaignWritePlatformService = emailCampaignWritePlatformService;
        this.emailCampaignRepository = emailCampaignRepository;
    }

    @PostConstruct
    public void addListeners() {
        this.businessEventNotifierService.addBusinessEventPostListeners(BusinessEventNotificationConstants.BusinessEvents.LOAN_APPROVED,
                new EmailCampaignDomainServiceImpl.SendEmailOnLoanApproved());
        this.businessEventNotifierService.addBusinessEventPostListeners(BusinessEventNotificationConstants.BusinessEvents.LOAN_REJECTED,
                new EmailCampaignDomainServiceImpl.SendEmailOnLoanRejected());
        this.businessEventNotifierService.addBusinessEventPostListeners(
                BusinessEventNotificationConstants.BusinessEvents.LOAN_MAKE_REPAYMENT,
                new EmailCampaignDomainServiceImpl.SendEmailOnLoanRepayment());
    }

    private class SendEmailOnLoanRepayment extends EmailBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEventNotificationConstants.BusinessEntity, Object> businessEventEntity) {
            Object entity = businessEventEntity.get(BusinessEventNotificationConstants.BusinessEntity.LOAN_TRANSACTION);
            if (entity instanceof LoanTransaction) {
                LoanTransaction loanTransaction = (LoanTransaction) entity;
                try {
                    notifyLoanOwner(loanTransaction, "Loan Repayment");
                } catch (IOException e) {
                    LOG.error("Exception when trying to send triggered email: {}", e.getMessage());
                }
            }
        }
    }

    private class SendEmailOnLoanRejected extends EmailBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEventNotificationConstants.BusinessEntity, Object> businessEventEntity) {
            Object entity = businessEventEntity.get(BusinessEventNotificationConstants.BusinessEntity.LOAN);
            if (entity instanceof Loan) {
                Loan loan = (Loan) entity;
                try {
                    notifyLoanOwner(loan, "Loan Rejected");
                } catch (IOException e) {
                    LOG.error("Exception when trying to send triggered email: {}", e.getMessage());
                }
            }
        }
    }

    private class SendEmailOnLoanApproved extends EmailBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEventNotificationConstants.BusinessEntity, Object> businessEventEntity) {
            Object entity = businessEventEntity.get(BusinessEventNotificationConstants.BusinessEntity.LOAN);
            if (entity instanceof Loan) {
                Loan loan = (Loan) entity;
                try {
                    notifyLoanOwner(loan, "Loan Approved");
                } catch (IOException e) {
                    LOG.error("Exception when trying to send triggered email: {}", e.getMessage());
                }
            }
        }
    }

    private void notifyLoanOwner(LoanTransaction loanTransaction, String paramValue) throws IOException {
        List<EmailCampaign> campaigns = this.retrieveEmailCampaigns(paramValue);
        for (EmailCampaign emailCampaign : campaigns) {
            HashMap<String, String> campaignParams = new ObjectMapper().readValue(emailCampaign.getParamValue(),
                    new TypeReference<HashMap<String, String>>() {});
            campaignParams.put("loanId", loanTransaction.getLoan().getId().toString());
            campaignParams.put("loanTransactionId", loanTransaction.getId().toString());
            this.emailCampaignWritePlatformService.insertDirectCampaignIntoEmailOutboundTable(loanTransaction.getLoan(), emailCampaign,
                    campaignParams);
        }
    }

    private void notifyLoanOwner(Loan loan, String paramValue) throws IOException {
        List<EmailCampaign> campaigns = this.retrieveEmailCampaigns(paramValue);
        for (EmailCampaign emailCampaign : campaigns) {
            HashMap<String, String> campaignParams = new ObjectMapper().readValue(emailCampaign.getParamValue(),
                    new TypeReference<HashMap<String, String>>() {});
            campaignParams.put("loanId", loan.getId().toString());
            this.emailCampaignWritePlatformService.insertDirectCampaignIntoEmailOutboundTable(loan, emailCampaign, campaignParams);
        }
    }

    private abstract static class EmailBusinessEventAdapter implements BusinessEventListener {

        @Override
        public void businessEventToBeExecuted(Map<BusinessEventNotificationConstants.BusinessEntity, Object> businessEventEntity) {
            // Nothing to do
        }
    }

    private List<EmailCampaign> retrieveEmailCampaigns(String paramValue) {
        List<EmailCampaign> emailCampaigns = emailCampaignRepository.findActiveEmailCampaigns("%" + paramValue + "%",
                SmsCampaignTriggerType.TRIGGERED.getValue());
        return emailCampaigns;
    }
}
