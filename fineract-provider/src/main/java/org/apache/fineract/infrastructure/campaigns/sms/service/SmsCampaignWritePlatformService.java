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
package org.apache.fineract.infrastructure.campaigns.sms.service;

import java.util.Map;

import org.apache.fineract.infrastructure.campaigns.sms.data.CampaignPreviewData;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;

public interface SmsCampaignWritePlatformService {

    CommandProcessingResult create(JsonCommand command);

    CommandProcessingResult update(Long resourceId, JsonCommand command);

    CommandProcessingResult delete(Long resourceId);

    CommandProcessingResult activateSmsCampaign(Long campaignId, JsonCommand command);

    CommandProcessingResult closeSmsCampaign(Long campaignId, JsonCommand command);

    CommandProcessingResult reactivateSmsCampaign(Long campaignId, JsonCommand command);

    void insertDirectCampaignIntoSmsOutboundTable(Loan loan, SmsCampaign smsCampaign);

    String compileSmsTemplate(String textMessageTemplate, String campaignName, Map<String, Object> smsParams);

    CampaignPreviewData previewMessage(JsonQuery query);

    public void storeTemplateMessageIntoSmsOutBoundTable() throws JobExecutionException;

    public void insertDirectCampaignIntoSmsOutboundTable(final Client client, final SmsCampaign smsCampaign) ;
    
    public void insertDirectCampaignIntoSmsOutboundTable(final SavingsAccount savingsAccount, final SmsCampaign smsCampaign) ;

}
