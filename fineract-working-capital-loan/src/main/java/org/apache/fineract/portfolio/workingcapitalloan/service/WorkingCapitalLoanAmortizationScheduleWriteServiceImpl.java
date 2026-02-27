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
import org.apache.fineract.portfolio.workingcapitalloan.data.ProjectedAmortizationScheduleGenerateRequest;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO: This is a temporary testing implementation. In the real flow, the amortization schedule
// will be generated and saved as part of the loan lifecycle (approve/disburse) — not via a
// standalone endpoint. The parameters will come from the loan entity + product, not from the
// request body. Replace this once the full WCL lifecycle is implemented.
@Service
@RequiredArgsConstructor
@Transactional
public class WorkingCapitalLoanAmortizationScheduleWriteServiceImpl implements WorkingCapitalLoanAmortizationScheduleWriteService {

    // TODO: currency should come from loan product once WCL lifecycle is implemented
    private static final MonetaryCurrency DEFAULT_CURRENCY = new MonetaryCurrency("USD", 2, null);

    private final WorkingCapitalLoanRepository loanRepository;
    private final ProjectedAmortizationScheduleRepositoryWrapper scheduleRepositoryWrapper;

    @Override
    public void generateAndSaveAmortizationSchedule(final Long loanId, final ProjectedAmortizationScheduleGenerateRequest request) {
        final WorkingCapitalLoan loan = loanRepository.findById(loanId).orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        final MathContext mc = MoneyHelper.getMathContext();

        final ProjectedAmortizationScheduleModel model = ProjectedAmortizationScheduleModel.generate(//
                request.getOriginationFeeAmount(), //
                request.getNetDisbursementAmount(), //
                request.getTotalPaymentValue(), //
                request.getPeriodPaymentRate(), //
                request.getNpvDayCount(), //
                request.getExpectedDisbursementDate(), //
                mc, DEFAULT_CURRENCY);

        scheduleRepositoryWrapper.writeModel(loan, model);
    }
}
