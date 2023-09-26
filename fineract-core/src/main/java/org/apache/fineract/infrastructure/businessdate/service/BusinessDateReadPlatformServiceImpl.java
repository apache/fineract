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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.businessdate.data.BusinessDateData;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDate;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateRepository;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.businessdate.exception.BusinessDateNotFoundException;
import org.apache.fineract.infrastructure.businessdate.mapper.BusinessDateMapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessDateReadPlatformServiceImpl implements BusinessDateReadPlatformService {

    private final BusinessDateRepository repository;
    private final BusinessDateMapper mapper;
    private final ConfigurationDomainService configurationDomainService;

    @Override
    public List<BusinessDateData> findAll() {
        List<BusinessDate> businessDateList = repository.findAll();
        return mapper.map(businessDateList);
    }

    @Override
    public BusinessDateData findByType(String type) {
        BusinessDateType businessDateType;
        try {
            businessDateType = BusinessDateType.valueOf(type);
        } catch (IllegalArgumentException e) {
            log.error("Provided business date type cannot be found: {}", type);
            throw BusinessDateNotFoundException.notExist(type, e);
        }
        Optional<BusinessDate> businessDate = repository.findByType(businessDateType);
        if (businessDate.isEmpty()) {
            log.error("Business date with the provided type cannot be found {}", type);
            throw BusinessDateNotFoundException.notFound(type);
        }
        return mapper.map(businessDate.get());
    }

    @Override
    public HashMap<BusinessDateType, LocalDate> getBusinessDates() {
        HashMap<BusinessDateType, LocalDate> businessDateMap = new HashMap<>();
        LocalDate tenantDate = DateUtils.getLocalDateOfTenant();
        businessDateMap.put(BusinessDateType.BUSINESS_DATE, tenantDate);
        businessDateMap.put(BusinessDateType.COB_DATE, tenantDate);
        if (configurationDomainService.isBusinessDateEnabled()) {
            final List<BusinessDateData> businessDateDataList = this.findAll();
            for (BusinessDateData businessDateData : businessDateDataList) {
                businessDateMap.put(BusinessDateType.valueOf(businessDateData.getType()), businessDateData.getDate());
            }
        }
        return businessDateMap;
    }
}
