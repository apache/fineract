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

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadService;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanCommandTemplateData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanTransactionReadPlatformServiceImpl implements WorkingCapitalLoanTransactionReadPlatformService {

    private final WorkingCapitalLoanRepository repository;
    private final PaymentTypeReadService paymentTypeReadPlatformService;

    @Override
    public WorkingCapitalLoanCommandTemplateData retrieveLoanTransactionTemplate(final Long loanId, final String command) {
        final WorkingCapitalLoan wcLoan = retrieveWorkingCapitalLoan(loanId);

        final LocalDate expectedDisbursementDate = wcLoan.getDisbursementDetails().get(0).getExpectedDisbursementDate();
        if (WorkingCapitalLoanConstants.APPROVE_LOAN_COMMAND.equals(command)) {
            return WorkingCapitalLoanCommandTemplateData.builder().approvalAmount(wcLoan.getProposedPrincipal())
                    .approvalDate(expectedDisbursementDate).expectedDisbursementDate(expectedDisbursementDate)
                    .currency(wcLoan.getLoanProduct().getCurrency().toData()).build();
        } else if (WorkingCapitalLoanConstants.DISBURSE_LOAN_COMMAND.equals(command)) {
            return WorkingCapitalLoanCommandTemplateData.builder().expectedAmount(wcLoan.getApprovedPrincipal())
                    .expectedDisbursementDate(expectedDisbursementDate).currency(wcLoan.getLoanProduct().getCurrency().toData())
                    .paymentTypeOptions(paymentTypeReadPlatformService.retrieveAllPaymentTypes()).build();
        }
        return null;
    }

    private WorkingCapitalLoan retrieveWorkingCapitalLoan(final Long loanId) {
        return repository.findByIdWithFullDetails(loanId).orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
    }

}
