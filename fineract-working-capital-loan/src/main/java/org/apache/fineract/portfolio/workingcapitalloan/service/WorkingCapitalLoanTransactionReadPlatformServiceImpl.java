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
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAsOfBalanceData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanChargePaidByData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanCommandTemplateData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionTemplateData;
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
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final WorkingCapitalLoanTransactionMapper transactionMapper;
    private final WorkingCapitalLoanChargePaidByReadService chargePaidByReadService;
    private final WorkingCapitalLoanAsOfBalanceReadService asOfBalanceReadService;

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
        final LocalDate quoteDate = transactionDate != null ? transactionDate : DateUtils.getBusinessLocalDate();
        // Resolved once, for every command rather than only for the prepayment quote, so that asking for today's
        // balance explicitly and asking for it by omission go down the same path and cannot drift apart.
        final Optional<WorkingCapitalLoanAsOfBalanceData> balance = asOfBalanceReadService.retrieveAsOf(loanId, wcLoan.getBalance(),
                quoteDate);
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
            return template.expectedAmount(balance.map(WorkingCapitalLoanAsOfBalanceData::principalOutstanding).orElse(null))
                    .paymentTypeOptions(paymentTypeReadPlatformService.retrieveAllPaymentTypes())
                    .classificationOptions(codeValueReadPlatformService
                            .retrieveCodeValuesByCode(WorkingCapitalLoanConstants.REPAYMENT_CLASSIFICATION_CODE_NAME))
                    .build();
        } else if (WorkingCapitalLoanConstants.CREDIT_BALANCE_REFUND_COMMAND.equals(command)) {
            // The overpayment is a paid-side figure, so unlike the buckets above it does not vary with the date.
            final BigDecimal overpaymentAmount = balance.map(WorkingCapitalLoanAsOfBalanceData::overpaymentAmount).orElse(null);
            return template.expectedAmount(overpaymentAmount != null ? overpaymentAmount : BigDecimal.ZERO)
                    .paymentTypeOptions(paymentTypeReadPlatformService.retrieveAllPaymentTypes())
                    .classificationOptions(codeValueReadPlatformService
                            .retrieveCodeValuesByCode(WorkingCapitalLoanConstants.CREDIT_BALANCE_REFUND_CLASSIFICATION_CODE_NAME))
                    .build();
        } else if (WorkingCapitalLoanConstants.RECOVERY_PAYMENT_LOAN_COMMAND.equals(command)) {
            // The amount to pre-fill is what is still recoverable, NOT the gross amount written off: a recovery may
            // not exceed it, so offering the gross figure after a partial recovery would pre-fill a value the API
            // rejects. Term loan pre-fills the gross figure and has that problem. Both figures behind it are
            // paid-side, so this does not vary with the date either.
            return template.expectedAmount(balance.map(WorkingCapitalLoanAsOfBalanceData::writtenOffOutstanding).orElse(BigDecimal.ZERO))
                    .paymentTypeOptions(paymentTypeReadPlatformService.retrieveAllPaymentTypes()).build();
        } else if (WorkingCapitalLoanConstants.DISCOUNT_FEE_LOAN_COMMAND.equals(command)
                || WorkingCapitalLoanConstants.DISCOUNT_FEE_ADJUSTMENT_LOAN_COMMAND.equals(command)) {
            return template.classificationOptions(codeValueReadPlatformService
                    .retrieveCodeValuesByCode(WorkingCapitalLoanConstants.DISCOUNT_FEE_CLASSIFICATION_CODE_NAME)).build();
        } else if (WorkingCapitalLoanConstants.CHARGE_OFF_LOAN_COMMAND.equals(command)) {
            // Charge-off amount is the auto-calculated outstanding balance as of the quote date. chargeOffDate stays
            // the business date and is deliberately NOT the quote date: the e2e suite reads it back as a probe for the
            // server's own business date, and echoing the caller's date would turn that check into a tautology.
            return template.expectedAmount(balance.map(WorkingCapitalLoanAsOfBalanceData::totalOutstanding).orElse(BigDecimal.ZERO))
                    .chargeOffDate(DateUtils.getBusinessLocalDate()).chargeOffReasonOptions(
                            codeValueReadPlatformService.retrieveCodeValuesByCode(WorkingCapitalLoanConstants.CHARGE_OFF_REASONS))
                    .build();
        } else if (WorkingCapitalLoanConstants.PREPAY_LOAN_COMMAND.equals(command)) {
            return prePaymentTemplate(template, balance, quoteDate);
        }
        return null;
    }

    /**
     * The payoff quote: what has to be paid on {@code quoteDate} to close the loan.
     *
     * <p>
     * The amount is the outstanding balance as of that date, so anything that increased what is owed after it - the
     * disbursement, a charge added later, a discount fee or its adjustment - is left out. Payments are not filtered the
     * same way: the quote stays net of every payment ever made, which is what keeps a prepayment backdated behind an
     * existing payment from closing the loan and then overpaying it.
     *
     * <p>
     * A loan with no balance row is quoted with absent amounts rather than amounts of zero, so a caller can tell a
     * missing balance from a loan with nothing left to pay.
     *
     * <p>
     * It is typed as a plain Repayment because that is what the caller posts it back as, through the ordinary repayment
     * command, with the repayment allocation and accounting treatment that comes with it.
     */
    private WorkingCapitalLoanTransactionTemplateData prePaymentTemplate(
            final WorkingCapitalLoanTransactionTemplateData.WorkingCapitalLoanTransactionTemplateDataBuilder template,
            final Optional<WorkingCapitalLoanAsOfBalanceData> balance, final LocalDate quoteDate) {
        return template.transactionDate(quoteDate)
                .expectedAmount(balance.map(WorkingCapitalLoanAsOfBalanceData::totalOutstanding).orElse(null))
                .type(LoanEnumerations.transactionType(LoanTransactionType.REPAYMENT))
                .principalPortion(balance.map(WorkingCapitalLoanAsOfBalanceData::principalOutstanding).orElse(null))
                .feeChargesPortion(balance.map(WorkingCapitalLoanAsOfBalanceData::feeOutstanding).orElse(null))
                .penaltyChargesPortion(balance.map(WorkingCapitalLoanAsOfBalanceData::penaltyOutstanding).orElse(null))
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
