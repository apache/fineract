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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.math.MathContext;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.domain.ProjectedAmortizationLoanModel;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.ProjectedAmortizationLoanModelRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectedAmortizationScheduleRepositoryWrapperImpl implements ProjectedAmortizationScheduleRepositoryWrapper {

    private final ProjectedAmortizationLoanModelRepository repository;
    private final ProjectedAmortizationScheduleModelParserService parserService;

    @Override
    public Optional<ProjectedAmortizationScheduleModel> readModel(final Long loanId, @NonNull final MathContext mc,
            @NonNull final MonetaryCurrency currency) {
        return repository.findByLoanId(loanId) //
                .map(ProjectedAmortizationLoanModel::getJsonModel) //
                .map(json -> parserService.fromJson(json, mc, currency));
    }

    @Override
    @Transactional
    public void writeModel(@NonNull final WorkingCapitalLoan loan, @NonNull final ProjectedAmortizationScheduleModel model) {
        final String jsonModel = parserService.toJson(model);
        final ProjectedAmortizationLoanModel entity = repository.findByLoanId(loan.getId()).orElseGet(() -> {
            final ProjectedAmortizationLoanModel newEntity = new ProjectedAmortizationLoanModel();
            newEntity.setLoan(loan);
            return newEntity;
        });
        entity.setBusinessDate(ThreadLocalContextUtil.getBusinessDate());
        entity.setLastModifiedDate(DateUtils.getAuditOffsetDateTime());
        entity.setJsonModel(jsonModel);
        entity.setJsonModelVersion(ProjectedAmortizationScheduleModel.getModelVersion());
        repository.save(entity);
    }

}
