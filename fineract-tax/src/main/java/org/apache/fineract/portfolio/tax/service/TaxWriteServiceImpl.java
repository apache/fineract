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
package org.apache.fineract.portfolio.tax.service;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.tax.data.TaxComponentCreateRequest;
import org.apache.fineract.portfolio.tax.data.TaxComponentCreateResponse;
import org.apache.fineract.portfolio.tax.data.TaxComponentUpdateRequest;
import org.apache.fineract.portfolio.tax.data.TaxComponentUpdateResponse;
import org.apache.fineract.portfolio.tax.data.TaxGroupCreateRequest;
import org.apache.fineract.portfolio.tax.data.TaxGroupCreateResponse;
import org.apache.fineract.portfolio.tax.data.TaxGroupUpdateRequest;
import org.apache.fineract.portfolio.tax.data.TaxGroupUpdateResponse;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxComponentHistory;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepository;
import org.apache.fineract.portfolio.tax.domain.TaxGroupMappings;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepository;
import org.apache.fineract.portfolio.tax.exception.TaxComponentNotFoundException;
import org.apache.fineract.portfolio.tax.exception.TaxGroupNotFoundException;
import org.apache.fineract.portfolio.tax.mapper.TaxComponentCreateRequestMapper;
import org.apache.fineract.portfolio.tax.mapper.TaxGroupCreateRequestMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnMissingBean(value = TaxWriteService.class, ignored = TaxWriteServiceImpl.class)
public class TaxWriteServiceImpl implements TaxWriteService {

    private final TaxComponentRepository taxComponentRepository;
    private final TaxGroupRepository taxGroupRepository;
    private final TaxGroupCreateRequestMapper taxGroupCreateRequestMapper;

    private final TaxComponentCreateRequestMapper taxComponentCreateRequestMapper;

    @Override
    public TaxComponentCreateResponse createTaxComponent(@Valid TaxComponentCreateRequest request) {
        var taxComponent = taxComponentCreateRequestMapper.map(request);

        taxComponentRepository.saveAndFlush(taxComponent);

        return TaxComponentCreateResponse.builder().resourceId(taxComponent.getId()).build();
    }

    @Override
    public TaxComponentUpdateResponse updateTaxComponent(TaxComponentUpdateRequest request) {
        var taxComponent = taxComponentRepository.findById(request.getId())
                .orElseThrow(() -> new TaxComponentNotFoundException(request.getId()));

        final Map<String, Object> changes = new HashMap<>();

        if (!Objects.equals(taxComponent.getName(), request.getName())) {
            taxComponent.setName(request.getName());

            changes.put(TaxComponent.Fields.startDate, request.getName());
        }

        if (Objects.compare(taxComponent.getPercentage(), request.getPercentage(), (o1, o2) -> {
            if (o1 == null) {
                o1 = BigDecimal.ZERO;
            }
            if (o2 == null) {
                o2 = BigDecimal.ZERO;
            }

            return o1.compareTo(o2);
        }) != 0) {
            taxComponent.setPercentage(request.getPercentage());

            changes.put(TaxComponent.Fields.percentage, request.getPercentage());

            var oldStartDate = taxComponent.getStartDate();
            var newStartDate = StringUtils.isNotEmpty(request.getStartDate())
                    ? DateUtils.toLocalDate(request.getLocale(), request.getStartDate(), request.getDateFormat())
                    : DateUtils.getBusinessLocalDate();

            changes.put(TaxComponent.Fields.startDate, newStartDate);

            var history = new TaxComponentHistory();
            history.setPercentage(request.getPercentage());
            history.setStartDate(oldStartDate);
            history.setStartDate(newStartDate);

            if (taxComponent.getTaxComponentHistories() != null) {
                taxComponent.getTaxComponentHistories().add(history);
            } else {
                taxComponent.setTaxComponentHistories(Set.of(history));
            }
        }

        taxComponentRepository.saveAndFlush(taxComponent);

        return TaxComponentUpdateResponse.builder().resourceId(taxComponent.getId()).changes(changes).build();
    }

    @Override
    public TaxGroupCreateResponse createTaxGroup(@Valid TaxGroupCreateRequest request) {
        var taxGroup = taxGroupCreateRequestMapper.map(request);

        taxGroupRepository.saveAndFlush(taxGroup);

        return TaxGroupCreateResponse.builder().resourceId(taxGroup.getId()).build();
    }

    @Override
    public TaxGroupUpdateResponse updateTaxGroup(@Valid TaxGroupUpdateRequest request) {
        var taxGroup = taxGroupRepository.findById(request.getId()).orElseThrow(() -> new TaxGroupNotFoundException(request.getId()));

        // var groupMappings =
        request.getTaxComponents().stream()
                .map(taxGroupComponentData -> taxComponentRepository.findById(taxGroupComponentData.getTaxComponentId())
                        .orElseThrow(() -> new TaxComponentNotFoundException(taxGroupComponentData.getTaxComponentId())))
                .map(taxComponent -> new TaxGroupMappings().setTaxComponent(taxComponent)).collect(Collectors.toSet());

        final Map<String, Object> changes = new HashMap<>();

        if (!Objects.equals(taxGroup.getName(), request.getName())) {
            taxGroup.setName(request.getName());

            changes.put(TaxComponent.Fields.name, request.getName());
        }

        // TODO: history

        taxGroupRepository.saveAndFlush(taxGroup);

        return TaxGroupUpdateResponse.builder().resourceId(taxGroup.getId()).changes(changes).build();
    }
}
