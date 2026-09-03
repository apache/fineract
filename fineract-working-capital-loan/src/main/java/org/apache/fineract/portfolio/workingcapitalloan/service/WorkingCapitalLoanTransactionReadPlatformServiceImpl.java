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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadService;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanChargePaidByData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanCommandTemplateData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionTemplateData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
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
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final WorkingCapitalLoanTransactionMapper transactionMapper;
    private final WorkingCapitalLoanChargePaidByReadService chargePaidByReadService;

    @Override
    public WorkingCapitalLoanCommandTemplateData retrieveLoanActionTemplate(final Long loanId, final String command) {
        final WorkingCapitalLoan wcLoan = retrieveWorkingCapitalLoan(loanId);

        final LocalDate expectedDisbursementDate = wcLoan.getDisbursementDetails().getFirst().getExpectedDisbursementDate();
        if (WorkingCapitalLoanConstants.APPROVE_LOAN_COMMAND.equals(command)) {
            return WorkingCapitalLoanCommandTemplateData.builder().approvalAmount(wcLoan.getProposedPrincipal())
                    .approvalDate(expectedDisbursementDate).expectedDisbursementDate(expectedDisbursementDate)
                    .discountAmount(wcLoan.getLoanProductRelatedDetails().getDiscountProposed())
                    .overrideDiscountDisabled(!wcLoan.getLoanProduct().getConfigurableAttributes().isDiscountDefaultOverridable())
                    .currency(wcLoan.getLoanProduct().getCurrency().toData()).build();
        }
        return null;
    }

    @Override
    public WorkingCapitalLoanTransactionTemplateData retrieveTransactionTemplate(final Long loanId, final String command,
            final LocalDate transactionDate) {
        final WorkingCapitalLoan wcLoan = retrieveWorkingCapitalLoan(loanId);
        final WorkingCapitalLoanBalance balance = wcLoan.getBalance();
        final WorkingCapitalLoanTransactionTemplateData.WorkingCapitalLoanTransactionTemplateDataBuilder template = WorkingCapitalLoanTransactionTemplateData
                .builder().wcLoanId(loanId).currency(wcLoan.getLoanProduct().getCurrency().toData());

        if (WorkingCapitalLoanConstants.DISBURSE_LOAN_COMMAND.equals(command)) {
            return template.expectedAmount(wcLoan.getApprovedPrincipal())
                    .expectedDisbursementDate(wcLoan.getDisbursementDetails().getFirst().getExpectedDisbursementDate())
                    .discountAmount(wcLoan.getLoanProductRelatedDetails().getDiscountApproved())
                    .overrideDiscountDisabled(!wcLoan.getLoanProduct().getConfigurableAttributes().isDiscountDefaultOverridable())
                    .paymentTypeOptions(paymentTypeReadPlatformService.retrieveAllPaymentTypes())
                    .classificationOptions(codeValueReadPlatformService
                            .retrieveCodeValuesByCode(WorkingCapitalLoanConstants.DISBURSEMENT_CLASSIFICATION_CODE_NAME))
                    .build();
        } else if (WorkingCapitalLoanConstants.REPAYMENT_LOAN_COMMAND.equals(command)
                || WorkingCapitalLoanConstants.GOODWILL_CREDIT_LOAN_COMMAND.equals(command)) {
            return template.expectedAmount(balance != null ? balance.getPrincipalOutstanding() : null)
                    .paymentTypeOptions(paymentTypeReadPlatformService.retrieveAllPaymentTypes())
                    .classificationOptions(codeValueReadPlatformService
                            .retrieveCodeValuesByCode(WorkingCapitalLoanConstants.REPAYMENT_CLASSIFICATION_CODE_NAME))
                    .build();
        } else if (WorkingCapitalLoanConstants.CREDIT_BALANCE_REFUND_COMMAND.equals(command)) {
            final BigDecimal overpaymentAmount = balance != null ? balance.getOverpaymentAmount() : null;
            return template.expectedAmount(overpaymentAmount != null ? overpaymentAmount : BigDecimal.ZERO)
                    .paymentTypeOptions(paymentTypeReadPlatformService.retrieveAllPaymentTypes())
                    .classificationOptions(codeValueReadPlatformService
                            .retrieveCodeValuesByCode(WorkingCapitalLoanConstants.CREDIT_BALANCE_REFUND_CLASSIFICATION_CODE_NAME))
                    .build();
        } else if (WorkingCapitalLoanConstants.RECOVERY_PAYMENT_LOAN_COMMAND.equals(command)) {
            // The amount to pre-fill is what is still recoverable, NOT the gross amount written off: a recovery may
            // not exceed it, so offering the gross figure after a partial recovery would pre-fill a value the API
            // rejects. Term loan pre-fills the gross figure and has that problem.
            return template.expectedAmount(balance != null ? balance.getWrittenOffOutstanding() : BigDecimal.ZERO)
                    .paymentTypeOptions(paymentTypeReadPlatformService.retrieveAllPaymentTypes()).build();
        } else if (WorkingCapitalLoanConstants.DISCOUNT_FEE_LOAN_COMMAND.equals(command)
                || WorkingCapitalLoanConstants.DISCOUNT_FEE_ADJUSTMENT_LOAN_COMMAND.equals(command)) {
            return template.classificationOptions(codeValueReadPlatformService
                    .retrieveCodeValuesByCode(WorkingCapitalLoanConstants.DISCOUNT_FEE_CLASSIFICATION_CODE_NAME)).build();
        } else if (WorkingCapitalLoanConstants.CHARGE_OFF_LOAN_COMMAND.equals(command)) {
            // Charge-off amount is the auto-calculated outstanding balance; the date defaults to the business date.
            return template.chargeOffAmount(balance != null ? balance.getTotalOutstanding() : BigDecimal.ZERO)
                    .chargeOffDate(DateUtils.getBusinessLocalDate()).chargeOffReasonOptions(
                            codeValueReadPlatformService.retrieveCodeValuesByCode(WorkingCapitalLoanConstants.CHARGE_OFF_REASONS))
                    .build();
        } else if (WorkingCapitalLoanConstants.PREPAY_LOAN_COMMAND.equals(command)) {
            return prePaymentTemplate(template, balance, transactionDate);
        }
        return null;
    }

    /**
     * The payoff quote is read straight off the current balance: a Working Capital loan accrues nothing over time - the
     * whole discount is loaded into principal at disbursement and its amortization only moves income recognition - so
     * the outstanding buckets move on explicit commands alone and the snapshot is already the payoff for
     * {@code transactionDate}. The date is carried into the response so callers can echo back the date they quoted for.
     * <p>
     * It is typed as a plain Repayment because that is what the caller posts it back as, through the ordinary repayment
     * command, with the repayment allocation and accounting treatment that comes with it.
     */
    private WorkingCapitalLoanTransactionTemplateData prePaymentTemplate(
            final WorkingCapitalLoanTransactionTemplateData.WorkingCapitalLoanTransactionTemplateDataBuilder template,
            final WorkingCapitalLoanBalance balance, final LocalDate transactionDate) {
        // The balance row is created when the application is submitted, so every loan reached through the normal flow
        // has one and the amounts below are real. Reading it defensively anyway reports a missing balance as absent
        // amounts rather than as a payoff of zero.
        final Optional<WorkingCapitalLoanBalance> present = Optional.ofNullable(balance);
        return template.transactionDate(transactionDate != null ? transactionDate : DateUtils.getBusinessLocalDate())
                .transactionAmount(present.map(WorkingCapitalLoanBalance::getTotalOutstanding).orElse(null))
                .type(LoanEnumerations.transactionType(LoanTransactionType.REPAYMENT))
                .principalPortion(present.map(WorkingCapitalLoanBalance::getPrincipalOutstanding).orElse(null))
                .feeChargesPortion(present.map(WorkingCapitalLoanBalance::getFeeOutstanding).orElse(null))
                .penaltyChargesPortion(present.map(WorkingCapitalLoanBalance::getPenaltyOutstanding).orElse(null))
                .paymentTypeOptions(paymentTypeReadPlatformService.retrieveAllPaymentTypes())
                .classificationOptions(codeValueReadPlatformService
                        .retrieveCodeValuesByCode(WorkingCapitalLoanConstants.REPAYMENT_CLASSIFICATION_CODE_NAME))
                .build();
    }

    @Override
    public Page<WorkingCapitalLoanTransactionData> retrieveTransactions(final Long loanId, final Pageable pageable) {
        ensureLoanExists(loanId);
        final Page<WorkingCapitalLoanTransaction> page = this.transactionRepository.findByWcLoan_IdOrderByTransactionDateAscIdAsc(loanId,
                pageable);
        final List<WorkingCapitalLoanTransactionData> content = page.getContent().stream().map(this.transactionMapper::toData).toList();

        // One query for the whole page rather than one per transaction.
        final Map<Long, List<WorkingCapitalLoanChargePaidByData>> chargePaidByByTxnId = this.chargePaidByReadService
                .fetchByTransactionIdsGrouped(content.stream().map(WorkingCapitalLoanTransactionData::getId).toList());
        content.forEach(txn -> txn.setChargePaidByList(chargePaidByByTxnId.getOrDefault(txn.getId(), List.of())));

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
        return toDataWithChargePaidBy(txn);
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
        return toDataWithChargePaidBy(txn);
    }

    private WorkingCapitalLoanTransactionData toDataWithChargePaidBy(final WorkingCapitalLoanTransaction transaction) {
        final WorkingCapitalLoanTransactionData data = this.transactionMapper.toData(transaction);
        data.setChargePaidByList(this.chargePaidByReadService.fetchByTransactionId(transaction.getId()));
        return data;
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
