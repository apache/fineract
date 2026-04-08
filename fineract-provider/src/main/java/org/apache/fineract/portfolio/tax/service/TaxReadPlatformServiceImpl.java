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
import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.portfolio.tax.data.TaxComponentData;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepository;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepositoryWrapper;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepository;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.apache.fineract.portfolio.tax.mapper.TaxComponentMapper;
import org.apache.fineract.portfolio.tax.mapper.TaxGroupMapper;

@RequiredArgsConstructor
public class TaxReadPlatformServiceImpl implements TaxReadPlatformService {

    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;
    private final TaxComponentRepository taxComponentRepository;
    private final TaxComponentRepositoryWrapper taxComponentRepositoryWrapper;
    private final TaxComponentMapper taxComponentMapper;
    private final TaxGroupRepository taxGroupRepository;
    private final TaxGroupRepositoryWrapper taxGroupRepositoryWrapper;
    private final TaxGroupMapper taxGroupMapper;

    @Override
    public List<TaxComponentData> retrieveAllTaxComponents() {
        return taxComponentMapper.map(taxComponentRepository.findAll());
    }

    @Override
    public TaxComponentData retrieveTaxComponentData(final Long id) {
        return taxComponentMapper.map(taxComponentRepositoryWrapper.findOneWithNotFoundDetection(id));
    }

    @Override
    public TaxComponentData retrieveTaxComponentTemplate() {
        return TaxComponentData.template(this.accountingDropdownReadPlatformService.retrieveAccountMappingOptions(),
                this.accountingDropdownReadPlatformService.retrieveGLAccountTypeOptions());
    }

    @Override
    public List<TaxGroupData> retrieveAllTaxGroups() {
        return taxGroupMapper.map(taxGroupRepository.findAll());
    }

    @Override
    public TaxGroupData retrieveTaxGroupData(final Long id) {
        return taxGroupMapper.map(taxGroupRepositoryWrapper.findOneWithNotFoundDetection(id));
    }

    @Override
    public TaxGroupData retrieveTaxGroupWithTemplate(final Long id) {
        TaxGroupData taxGroupData = retrieveTaxGroupData(id);
        taxGroupData = TaxGroupData.template(taxGroupData, retrieveTaxComponentsForLookUp());
        return taxGroupData;
    }

    @Override
    public TaxGroupData retrieveTaxGroupTemplate() {
        return TaxGroupData.template(retrieveTaxComponentsForLookUp());
    }

    private Collection<TaxComponentData> retrieveTaxComponentsForLookUp() {
        return taxComponentMapper.map(taxComponentRepository.findAll());
    }

    @Override
    public List<TaxGroupData> retrieveTaxGroupsForLookUp() {
        return taxGroupMapper.map(taxGroupRepository.findAll());
    }

}
