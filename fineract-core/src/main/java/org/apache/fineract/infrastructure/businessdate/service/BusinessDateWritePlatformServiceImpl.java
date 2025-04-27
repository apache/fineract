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
package org.apache.fineract.infrastructure.businessdate.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.businessdate.data.BusinessDateResponse;
import org.apache.fineract.infrastructure.businessdate.data.BusinessDateUpdateRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDate;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateRepository;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.businessdate.exception.BusinessDateActionException;
import org.apache.fineract.infrastructure.businessdate.mapper.BusinessDateUpdateRequestMapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessDateWritePlatformServiceImpl implements BusinessDateWritePlatformService {

    private final BusinessDateRepository repository;
    private final ConfigurationDomainService configurationDomainService;
    private final BusinessDateUpdateRequestMapper updateRequestMapper;

    @Override
    public BusinessDateResponse updateBusinessDate(BusinessDateUpdateRequest request) {
        BusinessDateResponse businessDateDto = updateRequestMapper.map(request);

        adjustDate(businessDateDto);

        return businessDateDto;
    }

    @Override
    public void increaseDateByTypeByOneDay(BusinessDateType businessDateType) throws JobExecutionException {
        Optional<BusinessDate> businessDateEntity = repository.findByType(businessDateType);
        List<Throwable> exceptions = new ArrayList<>();

        LocalDate businessDate = businessDateEntity.map(BusinessDate::getDate).orElse(DateUtils.getLocalDateOfTenant());
        businessDate = businessDate.plusDays(1);
        try {
            BusinessDateResponse response = BusinessDateResponse.builder().type(businessDateType)
                    .description(businessDateType.getDescription()).date(businessDate).build();
            adjustDate(response);
        } catch (final PlatformApiDataValidationException e) {
            final List<ApiParameterError> errors = e.getErrors();
            for (final ApiParameterError error : errors) {
                log.error("Increasing {} by 1 day failed due to: {}", businessDateType.getDescription(), error.getDeveloperMessage());
            }
            exceptions.add(e);
        } catch (final AbstractPlatformDomainRuleException e) {
            log.error("Increasing {} by 1 day failed due to: {}", businessDateType.getDescription(), e.getDefaultUserMessage());
            exceptions.add(e);
        } catch (Exception e) {
            log.error("Increasing {} by 1 day failed due to: {}", businessDateType.getDescription(), e.getMessage());
            exceptions.add(e);
        }
        if (!exceptions.isEmpty()) {
            throw new JobExecutionException(exceptions);
        }
    }

    private void adjustDate(BusinessDateResponse response) {
        boolean isCOBDateAdjustmentEnabled = configurationDomainService.isCOBDateAdjustmentEnabled();
        boolean isBusinessDateEnabled = configurationDomainService.isBusinessDateEnabled();

        if (!isBusinessDateEnabled) {
            log.error("Business date functionality is not enabled!");
            throw new BusinessDateActionException("business.date.is.not.enabled", "Business date functionality is not enabled");
        }
        updateOrCreateBusinessDate(response);
        if (isCOBDateAdjustmentEnabled && BusinessDateType.BUSINESS_DATE.equals(response.getType())) {
            BusinessDateResponse res = BusinessDateResponse.builder().type(BusinessDateType.COB_DATE)
                    .description(BusinessDateType.COB_DATE.getDescription()).date(response.getDate().minusDays(1)).build();
            updateOrCreateBusinessDate(res);
            response.addAllChanges(res.getChanges());
        }
    }

    private void updateOrCreateBusinessDate(BusinessDateResponse businessDateDto) {
        BusinessDateType businessDateType = businessDateDto.getType();
        Optional<BusinessDate> businessDate = repository.findByType(businessDateType);

        if (businessDate.isEmpty()) {
            BusinessDate newBusinessDate = BusinessDate.instance(businessDateType, businessDateDto.getDate());
            repository.save(newBusinessDate);
            businessDateDto.addChange(businessDateType, newBusinessDate.getDate());
        } else {
            updateBusinessDate(businessDate.get(), businessDateDto);
        }
    }

    private void updateBusinessDate(BusinessDate businessDate, BusinessDateResponse businessDateDto) {
        if (DateUtils.isEqual(businessDate.getDate(), businessDateDto.getDate())) {
            return;
        }

        businessDate.setDate(businessDateDto.getDate());
        repository.save(businessDate);

        businessDateDto.addChange(businessDate.getType(), businessDateDto.getDate());
    }
}
