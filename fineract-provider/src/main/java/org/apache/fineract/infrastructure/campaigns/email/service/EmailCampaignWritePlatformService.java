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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.campaigns.email.data.PreviewCampaignMessage;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaign;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;

public interface EmailCampaignWritePlatformService {

    CommandProcessingResult create(JsonCommand command);

    CommandProcessingResult update(Long resourceId, JsonCommand command);

    CommandProcessingResult delete(Long resourceId);

    CommandProcessingResult activateEmailCampaign(Long campaignId, JsonCommand command);

    CommandProcessingResult closeEmailCampaign(Long campaignId, JsonCommand command);

    CommandProcessingResult reactivateEmailCampaign(Long campaignId, JsonCommand command);

    @SuppressWarnings({ "unused", "rawtypes" })
    List<HashMap<String, Object>> getRunReportByServiceImpl(String reportName, Map<String, String> queryParams) throws IOException;

    PreviewCampaignMessage previewMessage(JsonQuery query);

    void insertDirectCampaignIntoEmailOutboundTable(Loan loan, EmailCampaign emailCampaign, HashMap<String, String> campaignParams);

}
