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
package org.apache.fineract.portfolio.workingcapitalloan.loan.service;

import java.math.MathContext;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.workingcapitalloan.loan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.loan.data.ProjectedAmortizationScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.loan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.loan.exception.ProjectedAmortizationScheduleNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.loan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.loan.mapper.ProjectedAmortizationScheduleMapper;
import org.apache.fineract.portfolio.workingcapitalloan.loan.repository.WorkingCapitalLoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkingCapitalLoanAmortizationScheduleReadServiceImpl implements WorkingCapitalLoanAmortizationScheduleReadService {

    private final WorkingCapitalLoanRepository loanRepository;
    private final ProjectedAmortizationScheduleRepositoryWrapper scheduleRepositoryWrapper;
    private final ProjectedAmortizationScheduleMapper mapper;

    @Override
    public ProjectedAmortizationScheduleData retrieveAmortizationSchedule(final Long loanId) {
        final WorkingCapitalLoan loan = loanRepository.findById(loanId).orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        final MathContext mc = MoneyHelper.getMathContext();
        final CurrencyData currency = WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan);
        final ProjectedAmortizationScheduleModel model = scheduleRepositoryWrapper.readModel(loanId, mc, currency)
                .orElseThrow(() -> new ProjectedAmortizationScheduleNotFoundException(loanId));

        return mapper.toData(model);
    }
}
