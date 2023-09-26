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

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.businessdate.data.BusinessDateData;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDate;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateRepository;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.businessdate.exception.BusinessDateActionException;
import org.apache.fineract.infrastructure.businessdate.validator.BusinessDateDataParserAndValidator;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessDateWritePlatformServiceImpl implements BusinessDateWritePlatformService {

    private final BusinessDateDataParserAndValidator dataValidator;
    private final BusinessDateRepository repository;
    private final ConfigurationDomainService configurationDomainService;

    @Override
    public CommandProcessingResult updateBusinessDate(@NotNull final JsonCommand command) {
        BusinessDateData data = dataValidator.validateAndParseUpdate(command);
        Map<String, Object> changes = new HashMap<>();
        adjustDate(data, changes);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).with(changes).build();

    }

    @Override
    public void adjustDate(BusinessDateData data, Map<String, Object> changes) {
        boolean isCOBDateAdjustmentEnabled = configurationDomainService.isCOBDateAdjustmentEnabled();
        boolean isBusinessDateEnabled = configurationDomainService.isBusinessDateEnabled();

        if (!isBusinessDateEnabled) {
            log.error("Business date functionality is not enabled!");
            throw new BusinessDateActionException("business.date.is.not.enabled", "Business date functionality is not enabled");
        }
        updateOrCreateBusinessDate(data.getType(), data.getDate(), changes);
        if (isCOBDateAdjustmentEnabled && BusinessDateType.BUSINESS_DATE.name().equals(data.getType())) {
            updateOrCreateBusinessDate(BusinessDateType.COB_DATE.getName(), data.getDate().minus(1, ChronoUnit.DAYS), changes);
        }
    }

    @Override
    public void increaseCOBDateByOneDay() throws JobExecutionException {
        increaseDateByTypeByOneDay(BusinessDateType.COB_DATE);
    }

    @Override
    public void increaseBusinessDateByOneDay() throws JobExecutionException {
        increaseDateByTypeByOneDay(BusinessDateType.BUSINESS_DATE);
    }

    private void increaseDateByTypeByOneDay(BusinessDateType businessDateType) throws JobExecutionException {
        Map<String, Object> changes = new HashMap<>();
        Optional<BusinessDate> businessDateEntity = repository.findByType(businessDateType);
        List<Throwable> exceptions = new ArrayList<>();

        LocalDate businessDate = businessDateEntity.map(BusinessDate::getDate).orElse(DateUtils.getLocalDateOfTenant());
        businessDate = businessDate.plusDays(1);
        try {
            BusinessDateData businessDateData = BusinessDateData.instance(businessDateType, businessDate);
            adjustDate(businessDateData, changes);
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

    private void updateOrCreateBusinessDate(String type, LocalDate newDate, Map<String, Object> changes) {
        BusinessDateType businessDateType = BusinessDateType.valueOf(type);
        Optional<BusinessDate> businessDate = repository.findByType(businessDateType);

        if (businessDate.isEmpty()) {
            BusinessDate newBusinessDate = BusinessDate.instance(businessDateType, newDate);
            repository.save(newBusinessDate);
            changes.put(type, newBusinessDate.getDate());
        } else {
            updateBusinessDate(businessDate.get(), newDate, changes);
        }
    }

    private void updateBusinessDate(BusinessDate businessDate, LocalDate newDate, Map<String, Object> changes) {
        LocalDate oldDate = businessDate.getDate();

        if (DateUtils.isEqual(oldDate, newDate)) {
            return;
        }
        businessDate.setDate(newDate);
        repository.save(businessDate);
        changes.put(businessDate.getType().name(), newDate);
    }
}
