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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadService;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanCommandTemplateData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanTransactionNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanTransactionMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkingCapitalLoanTransactionReadPlatformServiceImpl implements WorkingCapitalLoanTransactionReadPlatformService {

    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanRepository workingCapitalLoanRepository;
    private final PaymentTypeReadService paymentTypeReadPlatformService;
    private final WorkingCapitalLoanTransactionMapper transactionMapper;

    @Override
    public WorkingCapitalLoanCommandTemplateData retrieveLoanTransactionTemplate(final Long loanId, final String command) {
        final WorkingCapitalLoan wcLoan = retrieveWorkingCapitalLoan(loanId);

        final LocalDate expectedDisbursementDate = wcLoan.getDisbursementDetails().getFirst().getExpectedDisbursementDate();
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

    @Override
    public Page<WorkingCapitalLoanTransactionData> retrieveTransactions(final Long loanId, final Pageable pageable) {
        ensureLoanExists(loanId);
        final Page<WorkingCapitalLoanTransaction> page = this.transactionRepository.findByWcLoan_IdOrderByTransactionDateAscIdAsc(loanId,
                pageable);
        final List<WorkingCapitalLoanTransactionData> content = page.getContent().stream().map(this.transactionMapper::toData).toList();
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    @Override
    public Page<WorkingCapitalLoanTransactionData> retrieveTransactions(final ExternalId loanExternalId, final Pageable pageable) {
        final Long loanId = getResolvedLoanId(loanExternalId);
        if (loanId == null) {
            throw new WorkingCapitalLoanNotFoundException(loanExternalId);
        }
        return retrieveTransactions(loanId, pageable);
    }

    @Override
    public WorkingCapitalLoanTransactionData retrieveTransaction(final Long loanId, final Long transactionId) {
        ensureLoanExists(loanId);
        final WorkingCapitalLoanTransaction txn = this.transactionRepository.findByIdAndWcLoan_Id(transactionId, loanId)
                .orElseThrow(() -> new WorkingCapitalLoanTransactionNotFoundException(transactionId, loanId));
        return this.transactionMapper.toData(txn);
    }

    @Override
    public WorkingCapitalLoanTransactionData retrieveTransaction(final ExternalId loanExternalId, final Long transactionId) {
        final Long loanId = getResolvedLoanId(loanExternalId);
        if (loanId == null) {
            throw new WorkingCapitalLoanNotFoundException(loanExternalId);
        }
        return retrieveTransaction(loanId, transactionId);
    }

    @Override
    public WorkingCapitalLoanTransactionData retrieveTransaction(final Long loanId, final ExternalId transactionExternalId) {
        ensureLoanExists(loanId);
        final WorkingCapitalLoanTransaction txn = this.transactionRepository.findByWcLoan_IdAndExternalId(loanId, transactionExternalId)
                .orElseThrow(() -> new WorkingCapitalLoanTransactionNotFoundException(transactionExternalId));
        return this.transactionMapper.toData(txn);
    }

    @Override
    public WorkingCapitalLoanTransactionData retrieveTransaction(final ExternalId loanExternalId, final ExternalId transactionExternalId) {
        final Long loanId = getResolvedLoanId(loanExternalId);
        if (loanId == null) {
            throw new WorkingCapitalLoanNotFoundException(loanExternalId);
        }
        return retrieveTransaction(loanId, transactionExternalId);
    }

    private Long getResolvedLoanId(final ExternalId externalId) {
        return this.workingCapitalLoanRepository.findByExternalId(externalId).map(WorkingCapitalLoan::getId).orElse(null);
    }

    private void ensureLoanExists(final Long loanId) {
        if (!this.workingCapitalLoanRepository.existsById(loanId)) {
            throw new WorkingCapitalLoanNotFoundException(loanId);
        }
    }

    private WorkingCapitalLoan retrieveWorkingCapitalLoan(final Long loanId) {
        return workingCapitalLoanRepository.findByIdWithFullDetails(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
    }
}
