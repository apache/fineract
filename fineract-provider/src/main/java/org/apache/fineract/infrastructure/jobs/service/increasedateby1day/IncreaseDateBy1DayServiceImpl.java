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
package org.apache.fineract.infrastructure.jobs.service.increasedateby1day;

import java.time.LocalDate;
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
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateWritePlatformService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncreaseDateBy1DayServiceImpl implements IncreaseDateBy1DayService {

    private final BusinessDateRepository businessDateRepository;
    private final BusinessDateWritePlatformService businessDateWritePlatformService;

    @Override
    public void increaseDateByTypeByOneDay(BusinessDateType businessDateType) {
        Map<String, Object> changes = new HashMap<>();
        Optional<BusinessDate> businessDateEntity = businessDateRepository.findByType(businessDateType);
        LocalDate businessDate = businessDateEntity.map(BusinessDate::getDate).orElse(DateUtils.getLocalDateOfTenant());
        businessDate = businessDate.plusDays(1);
        try {
            BusinessDateData businessDateData = BusinessDateData.instance(businessDateType, businessDate);
            businessDateWritePlatformService.adjustDate(businessDateData, changes);
        } catch (final PlatformApiDataValidationException e) {
            final List<ApiParameterError> errors = e.getErrors();
            for (final ApiParameterError error : errors) {
                log.error("Increasing {} by 1 day failed due to: {}", businessDateType.getDescription(), error.getDeveloperMessage());
            }
        } catch (final AbstractPlatformDomainRuleException e) {
            log.error("Increasing {} by 1 day failed due to: {}", businessDateType.getDescription(), e.getDefaultUserMessage());
        } catch (Exception e) {
            log.error("Increasing {} by 1 day failed due to: {}", businessDateType.getDescription(), e.getMessage());
        }
    }
}
