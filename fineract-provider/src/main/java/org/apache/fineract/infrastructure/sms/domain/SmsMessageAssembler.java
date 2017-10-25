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
package org.apache.fineract.infrastructure.sms.domain;

import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaignRepository;
import org.apache.fineract.infrastructure.campaigns.sms.exception.SmsCampaignNotFound;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.sms.SmsApiConstants;
import org.apache.fineract.infrastructure.sms.exception.SmsNotFoundException;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public class SmsMessageAssembler {

    private final SmsMessageRepository smsMessageRepository;
    private final GroupRepositoryWrapper groupRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final StaffRepositoryWrapper staffRepository;
    private final SmsCampaignRepository smsCampaignRepository;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SmsMessageAssembler(final SmsMessageRepository smsMessageRepository, final GroupRepositoryWrapper groupRepositoryWrapper,
            final ClientRepositoryWrapper clientRepository, final StaffRepositoryWrapper staffRepository,
            final FromJsonHelper fromApiJsonHelper, final SmsCampaignRepository smsCampaignRepository) {
        this.smsMessageRepository = smsMessageRepository;
        this.groupRepository = groupRepositoryWrapper;
        this.clientRepository = clientRepository;
        this.staffRepository = staffRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.smsCampaignRepository = smsCampaignRepository;
    }

    public SmsMessage assembleFromJson(final JsonCommand command) {

        final JsonElement element = command.parsedJson();

        String mobileNo = null;
        Group group = null;
        String externalId = null;
        if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.groupIdParamName, element)) {
            final Long groupId = this.fromApiJsonHelper.extractLongNamed(SmsApiConstants.groupIdParamName, element);
            group = this.groupRepository.findOneWithNotFoundDetection(groupId);
        }

        SmsCampaign smsCampaign = null;
        boolean isNotification = false;
        if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.campaignIdParamName, element)) {
            final Long campaignId = this.fromApiJsonHelper.extractLongNamed(SmsApiConstants.campaignIdParamName, element);
            smsCampaign = this.smsCampaignRepository.findOne(campaignId);
            if (smsCampaign == null) { throw new SmsCampaignNotFound(campaignId); }
            isNotification = smsCampaign.isNotification();
        }

        Client client = null;
        if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.clientIdParamName, element)) {
            final Long clientId = this.fromApiJsonHelper.extractLongNamed(SmsApiConstants.clientIdParamName, element);
            client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            mobileNo = client.mobileNo();
        }

        Staff staff = null;
        if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.staffIdParamName, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(SmsApiConstants.staffIdParamName, element);
            staff = this.staffRepository.findOneWithNotFoundDetection(staffId);
            mobileNo = staff.mobileNo();
        }

        final String message = this.fromApiJsonHelper.extractStringNamed(SmsApiConstants.messageParamName, element);

        return SmsMessage.pendingSms(externalId, group, client, staff, message, mobileNo, smsCampaign, isNotification);
    }

    public SmsMessage assembleFromResourceId(final Long resourceId) {
        final SmsMessage sms = this.smsMessageRepository.findOne(resourceId);
        if (sms == null) { throw new SmsNotFoundException(resourceId); }
        return sms;
    }
}