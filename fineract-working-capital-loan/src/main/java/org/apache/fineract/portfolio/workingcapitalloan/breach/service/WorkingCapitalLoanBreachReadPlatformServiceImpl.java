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
package org.apache.fineract.portfolio.workingcapitalloan.breach.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.ApiFacingEnum;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.portfolio.workingcapitalloan.breach.data.WorkingCapitalLoanBreachData;
import org.apache.fineract.portfolio.workingcapitalloan.breach.data.WorkingCapitalLoanBreachTemplateResponse;
import org.apache.fineract.portfolio.workingcapitalloan.breach.domain.WorkingCapitalLoanBreach;
import org.apache.fineract.portfolio.workingcapitalloan.breach.exception.WorkingCapitalLoanBreachNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.breach.repository.WorkingCapitalLoanBreachRepository;
import org.apache.fineract.portfolio.workingcapitalloan.loan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.product.domain.WorkingCapitalLoanBreachAmountCalculationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorkingCapitalLoanBreachReadPlatformServiceImpl implements WorkingCapitalLoanBreachReadPlatformService {

    private final WorkingCapitalLoanBreachRepository repository;

    @Override
    public WorkingCapitalLoanBreachTemplateResponse retrieveTemplate() {
        final List<StringEnumOptionData> breachFrequencyTypeOptions = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(WorkingCapitalLoanPeriodFrequencyType.class);
        final List<StringEnumOptionData> breachAmountCalculationTypeOptions = WorkingCapitalLoanBreachAmountCalculationType
                .toStringEnumOptions();
        return new WorkingCapitalLoanBreachTemplateResponse(breachFrequencyTypeOptions, breachAmountCalculationTypeOptions);
    }

    @Override
    public List<WorkingCapitalLoanBreachData> retrieveAll() {
        return repository.findAll().stream().map(this::map).toList();
    }

    @Override
    public WorkingCapitalLoanBreachData retrieveOne(final Long breachId) {
        return repository.findById(breachId).map(this::map).orElseThrow(() -> new WorkingCapitalLoanBreachNotFoundException(breachId));
    }

    private WorkingCapitalLoanBreachData map(final WorkingCapitalLoanBreach item) {
        return WorkingCapitalLoanBreachData.builder().id(item.getId()).name(item.getName()).breachFrequency(item.getBreachFrequency())
                .breachFrequencyType(item.getBreachFrequencyType() != null ? item.getBreachFrequencyType().toStringEnumOptionData() : null)
                .breachAmountCalculationType(item.getBreachAmountCalculationType() != null
                        ? item.getBreachAmountCalculationType().getValueAsStringEnumOptionData()
                        : null)
                .breachAmount(item.getBreachAmount()).build();
    }
}
