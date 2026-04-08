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

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.campaigns.sms.data.SmsBusinessRulesData;
import org.apache.fineract.infrastructure.campaigns.sms.data.SmsCampaignData;
import org.apache.fineract.infrastructure.campaigns.sms.data.SmsProviderData;
import org.apache.fineract.infrastructure.campaigns.sms.exception.SmsCampaignNotFound;
import org.apache.fineract.infrastructure.campaigns.sms.mapper.BusinessRuleMapper;
import org.apache.fineract.infrastructure.campaigns.sms.mapper.SmsCampaignMapper;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.portfolio.calendar.service.CalendarDropdownReadPlatformService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsCampaignReadPlatformServiceImpl implements SmsCampaignReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final SmsCampaignDropdownReadPlatformService smsCampaignDropdownReadPlatformService;
    private final CalendarDropdownReadPlatformService calendarDropdownReadPlatformService;
    private final PaginationHelper paginationHelper;
    private final BusinessRuleMapper businessRuleMapper;
    private final SmsCampaignMapper smsCampaignMapper;

    @Override
    public SmsCampaignData retrieveOne(Long campaignId) {
        final boolean isVisible = true;
        try {
            final String sql = "select " + this.smsCampaignMapper.schema() + " where sc.id = ? and sc.is_visible = ?";
            return this.jdbcTemplate.queryForObject(sql, this.smsCampaignMapper, campaignId, isVisible); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new SmsCampaignNotFound(campaignId, e);
        }
    }

    @Override
    public Page<SmsCampaignData> retrieveAll(final SearchParameters searchParameters) {
        final boolean visible = true;
        final StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select ").append(sqlGenerator.calcFoundRows()).append(" ").append(this.smsCampaignMapper.schema())
                .append(" where sc.is_visible = ? ");

        if (searchParameters.hasLimit()) {
            sqlBuilder.append(" ");
            if (searchParameters.hasOffset()) {
                sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit(), searchParameters.getOffset()));
            } else {
                sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit()));
            }
        }
        return this.paginationHelper.fetchPage(jdbcTemplate, sqlBuilder.toString(), new Object[] { visible }, this.smsCampaignMapper);
    }

    @Override
    public SmsCampaignData retrieveTemplate(final String reportType) {
        final StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select ").append(this.businessRuleMapper.schema());
        if (StringUtils.isNotBlank(reportType)) {
            sqlBuilder.append(" where sr.report_type = ?");
        }
        final Collection<SmsBusinessRulesData> businessRulesOptions = this.jdbcTemplate.query(sqlBuilder.toString(),
                this.businessRuleMapper, // NOSONAR
                reportType);
        final Collection<SmsProviderData> smsProviderOptions = this.smsCampaignDropdownReadPlatformService.retrieveSmsProviders();
        final Collection<EnumOptionData> campaignTypeOptions = this.smsCampaignDropdownReadPlatformService.retrieveCampaignTypes();
        final Collection<EnumOptionData> campaignTriggerTypeOptions = this.smsCampaignDropdownReadPlatformService
                .retrieveCampaignTriggerTypes();
        final Collection<EnumOptionData> months = this.smsCampaignDropdownReadPlatformService.retrieveMonths();
        final Collection<EnumOptionData> weekDays = this.smsCampaignDropdownReadPlatformService.retrieveWeeks();
        final Collection<EnumOptionData> frequencyTypeOptions = this.calendarDropdownReadPlatformService
                .retrieveCalendarFrequencyTypeOptions();
        final Collection<EnumOptionData> periodFrequencyOptions = this.smsCampaignDropdownReadPlatformService.retrivePeriodFrequencyTypes();
        return SmsCampaignData.template(smsProviderOptions, campaignTypeOptions, businessRulesOptions, campaignTriggerTypeOptions, months,
                weekDays, frequencyTypeOptions, periodFrequencyOptions);
    }
}
