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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.data.ProjectedAmortizationScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.exception.ProjectedAmortizationScheduleNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.ProjectedAmortizationScheduleMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkingCapitalLoanAmortizationScheduleReadServiceImpl implements WorkingCapitalLoanAmortizationScheduleReadService {

    // TODO: currency should come from loan product once WCL lifecycle is implemented
    private static final MonetaryCurrency DEFAULT_CURRENCY = new MonetaryCurrency("USD", 2, null);

    private final WorkingCapitalLoanRepository loanRepository;
    private final ProjectedAmortizationScheduleRepositoryWrapper scheduleRepositoryWrapper;
    private final ProjectedAmortizationScheduleMapper mapper;

    @Override
    public ProjectedAmortizationScheduleData retrieveAmortizationSchedule(final Long loanId) {
        if (!loanRepository.existsById(loanId)) {
            throw new WorkingCapitalLoanNotFoundException(loanId);
        }

        final MathContext mc = MoneyHelper.getMathContext();
        final ProjectedAmortizationScheduleModel model = scheduleRepositoryWrapper.readModel(loanId, mc, DEFAULT_CURRENCY)
                .orElseThrow(() -> new ProjectedAmortizationScheduleNotFoundException(loanId));

        return mapper.toData(model);
    }
}
