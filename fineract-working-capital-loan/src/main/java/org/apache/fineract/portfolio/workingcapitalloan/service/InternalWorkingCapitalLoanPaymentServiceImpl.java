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

import static org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants.ENABLE_INSTANT_DELINQUENCY_CALCULATION;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalWorkingCapitalLoanPaymentServiceImpl implements InternalWorkingCapitalLoanPaymentService {

    private final WorkingCapitalLoanRepository loanRepository;
    private final WorkingCapitalLoanDelinquencyRangeScheduleService delinquencyRangeScheduleService;
    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final WorkingCapitalLoanDelinquencyClassificationService delinquencyClassificationService;
    private final WorkingCapitalLoanBreachScheduleService breachScheduleService;

    @Override
    public void makePayment(Long loanId, BigDecimal amount, LocalDate transactionDate) {
        delinquencyRangeScheduleService.applyRepayment(loanId, transactionDate, amount);
        breachScheduleService.applyRepayment(loanId, transactionDate, amount);
        if (globalConfigurationRepository.findOneByNameWithNotFoundDetection(ENABLE_INSTANT_DELINQUENCY_CALCULATION).isEnabled()) {
            WorkingCapitalLoan workingCapitalLoan = loanRepository.findById(loanId).orElseThrow();
            if (workingCapitalLoan.getLoanProductRelatedDetails() != null
                    && workingCapitalLoan.getLoanProductRelatedDetails().getDelinquencyBucket() != null) {
                delinquencyClassificationService.classifyDelinquency(workingCapitalLoan, transactionDate,
                        workingCapitalLoan.getLoanProductRelatedDetails().getDelinquencyBucket());
            }
        }
    }
}
