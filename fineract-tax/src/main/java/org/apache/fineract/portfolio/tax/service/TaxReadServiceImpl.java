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

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.adapter.AccountMappingOptionsAdapter;
import org.apache.fineract.accounting.adapter.GLAccountTypeOptionsAdapter;
import org.apache.fineract.portfolio.tax.data.TaxComponentData;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepository;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepository;
import org.apache.fineract.portfolio.tax.exception.TaxComponentNotFoundException;
import org.apache.fineract.portfolio.tax.exception.TaxGroupNotFoundException;
import org.apache.fineract.portfolio.tax.mapper.TaxComponentMapper;
import org.apache.fineract.portfolio.tax.mapper.TaxGroupMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnMissingBean(value = TaxReadService.class, ignored = TaxReadServiceImpl.class)
public class TaxReadServiceImpl implements TaxReadService {

    private final AccountMappingOptionsAdapter accountMappingOptionsAdapter;
    private final GLAccountTypeOptionsAdapter glAccountTypeOptionsAdapter;
    private final TaxComponentRepository taxComponentRepository;
    private final TaxComponentMapper taxComponentMapper;
    private final TaxGroupRepository taxGroupRepository;
    private final TaxGroupMapper taxGroupMapper;

    @Override
    public List<TaxComponentData> retrieveAllTaxComponents() {
        return taxComponentMapper.map(taxComponentRepository.findAll());
    }

    @Override
    public TaxComponentData retrieveTaxComponentData(final Long id) {
        return taxComponentMapper.map(taxComponentRepository.findById(id).orElseThrow(() -> new TaxComponentNotFoundException(id)));
    }

    @Override
    public TaxComponentData retrieveTaxComponentTemplate() {
        return TaxComponentData.builder().glAccountOptions(accountMappingOptionsAdapter.retrieve())
                .glAccountTypeOptions(glAccountTypeOptionsAdapter.retrieve()).build();
    }

    @Override
    public List<TaxGroupData> retrieveAllTaxGroups() {
        return taxGroupMapper.map(taxGroupRepository.findAll());
    }

    @Override
    public TaxGroupData retrieveTaxGroupData(final Long id) {
        return taxGroupMapper.map(taxGroupRepository.findById(id).orElseThrow(() -> new TaxGroupNotFoundException(id)));
    }

    @Override
    public TaxGroupData retrieveTaxGroupWithTemplate(final Long id) {
        TaxGroupData taxGroupData = retrieveTaxGroupData(id);
        taxGroupData = TaxGroupData.builder().id(taxGroupData.getId()).name(taxGroupData.getName())
                .taxAssociations(taxGroupData.getTaxAssociations()).taxComponents(retrieveTaxComponentsForLookUp()).build();
        return taxGroupData;
    }

    @Override
    public TaxGroupData retrieveTaxGroupTemplate() {
        return TaxGroupData.builder().taxComponents(retrieveTaxComponentsForLookUp()).build();
    }

    private Collection<TaxComponentData> retrieveTaxComponentsForLookUp() {
        return taxComponentMapper.map(taxComponentRepository.findAll());
    }

    @Override
    public List<TaxGroupData> retrieveTaxGroupsForLookUp() {
        return taxGroupMapper.map(taxGroupRepository.findAll());
    }

}
