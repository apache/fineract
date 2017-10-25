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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.fineract.infrastructure.campaigns.constants.CampaignType;
import org.apache.fineract.infrastructure.campaigns.helper.SmsConfigUtils;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignEnumerations;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType;
import org.apache.fineract.infrastructure.campaigns.sms.data.SmsProviderData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.calendar.domain.CalendarWeekDaysType;
import org.apache.fineract.portfolio.calendar.service.CalendarEnumerations;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SmsCampaignDropdownReadPlatformServiceImpl implements SmsCampaignDropdownReadPlatformService {

    private final RestTemplate restTemplate;

    private final SmsConfigUtils smsConfigUtils ;
    
    @Autowired
    public SmsCampaignDropdownReadPlatformServiceImpl(final SmsConfigUtils smsConfigUtils) {
        this.restTemplate = new RestTemplate();
        this.smsConfigUtils = smsConfigUtils ;
    }

    @Override
    public Collection<EnumOptionData> retrieveCampaignTriggerTypes() {
        final List<EnumOptionData> triggerTypeCodeValues = Arrays.asList( //
                SmsCampaignEnumerations.smscampaignTriggerType(SmsCampaignTriggerType.DIRECT), //
                SmsCampaignEnumerations.smscampaignTriggerType(SmsCampaignTriggerType.SCHEDULE), //
                SmsCampaignEnumerations.smscampaignTriggerType(SmsCampaignTriggerType.TRIGGERED) //
                );

        return triggerTypeCodeValues;
    }

    @Override
    public Collection<SmsProviderData> retrieveSmsProviders() {
        Collection<SmsProviderData> smsProviderOptions = new ArrayList<>();
        String hostName = "" ;
        try {
            Map<String, Object> hostConfig = this.smsConfigUtils.getMessageGateWayRequestURI("smsbridges", null);
            URI uri = (URI) hostConfig.get("uri");
            hostName = uri.getHost() ;
            HttpEntity<?> entity = (HttpEntity<?>) hostConfig.get("entity");
            ResponseEntity<Collection<SmsProviderData>> responseOne = restTemplate.exchange(uri, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<Collection<SmsProviderData>>() {});
            smsProviderOptions = responseOne.getBody();
            if (!responseOne.getStatusCode().equals(HttpStatus.OK)) {
            }
        } catch (Exception e) {
        }
        return smsProviderOptions;
    }

    @Override
    public Collection<EnumOptionData> retrieveCampaignTypes() {
        final List<EnumOptionData> campaignTypeCodeValues = Arrays.asList( //
                SmsCampaignEnumerations.smscampaignType(CampaignType.SMS)//
                );
        return campaignTypeCodeValues;
    }

    @Override
    public Collection<EnumOptionData> retrieveMonths() {
        Collection<EnumOptionData> monthsList = SmsCampaignEnumerations.calendarMonthType();
        return monthsList;
    }

    @Override
    public Collection<EnumOptionData> retrieveWeeks() {
        Collection<EnumOptionData> weeksList = CalendarEnumerations.calendarWeekDaysType(CalendarWeekDaysType.values());
        return weeksList;
    }

    @Override
    public Collection<EnumOptionData> retrivePeriodFrequencyTypes() {
        Collection<EnumOptionData> periodFrequencyTypes = SmsCampaignEnumerations
                .calendarPeriodFrequencyTypes(PeriodFrequencyType.values());
        return periodFrequencyTypes;
    }
}
