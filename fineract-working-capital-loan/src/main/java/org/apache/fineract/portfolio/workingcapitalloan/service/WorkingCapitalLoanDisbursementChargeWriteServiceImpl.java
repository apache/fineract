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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanRepaymentAtDisbursementTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanChargePaidBy;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelation;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargePaidByRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionAllocationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanDisbursementChargeWriteServiceImpl implements WorkingCapitalLoanDisbursementChargeWriteService {

    private final WorkingCapitalLoanChargeRepository chargeRepository;
    private final WorkingCapitalLoanChargePaidByRepository chargePaidByRepository;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    private final WorkingCapitalLoanChargePaymentHandler chargePaymentHandler;
    private final WorkingCapitalLoanChargeAmountResolver amountResolver;
    private final WorkingCapitalLoanAccountingProcessor accountingProcessor;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final WorkingCapitalLoanBalanceRepository balanceRepository;
    private final ExternalIdFactory externalIdFactory;

    @Transactional
    @Override
    public WorkingCapitalLoanTransaction settleChargesAtDisbursement(final WorkingCapitalLoan loan,
            final WorkingCapitalLoanTransaction disbursementTransaction, final BigDecimal disbursedAmount, final LocalDate disbursementDate,
            final PaymentDetail paymentDetail) {
        final List<WorkingCapitalLoanCharge> charges = materializeAndResolve(loan, disbursedAmount, disbursementDate);

        final BigDecimal total = charges.stream().map(WorkingCapitalLoanCharge::getAmount).map(MathUtil::nullToZero).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        if (total.compareTo(disbursedAmount) >= 0) {
            throw new GeneralPlatformDomainRuleException("error.msg.wc.loan.disbursement.charges.exceed.disbursed.amount",
                    "The charges due at disbursement (" + total + ") must be lower than the disbursed amount (" + disbursedAmount + ").",
                    total, disbursedAmount);
        }
        loan.setNetDisbursalAmount(disbursedAmount.subtract(total));
        if (!MathUtil.isGreaterThanZero(total)) {
            return null;
        }

        // Auto-generated when the platform is configured to do so, exactly like the other transactions.
        final WorkingCapitalLoanTransaction settlement = WorkingCapitalLoanTransaction.repaymentAtDisbursement(loan, total, paymentDetail,
                disbursementDate, externalIdFactory.create());
        settlement.getLoanTransactionRelations().add(
                new WorkingCapitalLoanTransactionRelation(settlement, disbursementTransaction, LoanTransactionRelationTypeEnum.RELATED));
        transactionRepository.saveAndFlush(settlement);

        BigDecimal feeTotal = BigDecimal.ZERO;
        BigDecimal penaltyTotal = BigDecimal.ZERO;
        for (final WorkingCapitalLoanCharge charge : charges) {
            final WorkingCapitalLoanChargePaidBy paidBy = chargePaymentHandler.applyChargePayment(settlement, charge, charge.getAmount());
            if (paidBy == null) {
                continue;
            }
            chargePaidByRepository.save(paidBy);
            chargeRepository.save(charge);
            if (charge.isPenaltyCharge()) {
                penaltyTotal = penaltyTotal.add(charge.getAmount());
            } else {
                feeTotal = feeTotal.add(charge.getAmount());
            }
        }
        chargePaidByRepository.flush();
        chargeRepository.flush();

        final WorkingCapitalLoanTransactionAllocation allocation = WorkingCapitalLoanTransactionAllocation.forPortions(settlement,
                BigDecimal.ZERO, feeTotal, penaltyTotal, BigDecimal.ZERO);
        allocationRepository.saveAndFlush(allocation);

        // The summary reports the charges as charged and paid, like a term loan does: they never become outstanding.
        final WorkingCapitalLoanBalance balance = balanceRepository.findByWcLoan_Id(loan.getId())
                .orElseGet(() -> WorkingCapitalLoanBalance.createFor(loan));
        balance.setFee(MathUtil.nullToZero(balance.getFee()).add(feeTotal));
        balance.setFeePaid(MathUtil.nullToZero(balance.getFeePaid()).add(feeTotal));
        balance.setPenalty(MathUtil.nullToZero(balance.getPenalty()).add(penaltyTotal));
        balance.setPenaltyPaid(MathUtil.nullToZero(balance.getPenaltyPaid()).add(penaltyTotal));
        balanceRepository.saveAndFlush(balance);

        if (loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization()) {
            accountingProcessor.postJournalEntries(loan, settlement, allocation, false);
        }
        businessEventNotifierService
                .notifyPostBusinessEvent(new WorkingCapitalLoanRepaymentAtDisbursementTransactionBusinessEvent(settlement, loan.getId()));
        return settlement;
    }

    /**
     * The loan's active disbursement charges, every amount re-resolved against the disbursed amount and every due date
     * set to the disbursement date. Pure term-loan model: only charges the application (or the account endpoint) put on
     * the loan are settled; the product catalogue is a default offered to the client application, never inherited here.
     */
    private List<WorkingCapitalLoanCharge> materializeAndResolve(final WorkingCapitalLoan loan, final BigDecimal disbursedAmount,
            final LocalDate disbursementDate) {
        final List<WorkingCapitalLoanCharge> charges = new ArrayList<>(
                chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(loan.getId()).stream()
                        .filter(WorkingCapitalLoanCharge::isDisbursementCharge).toList());
        for (final WorkingCapitalLoanCharge charge : charges) {
            if (charge.isPercentageBased()) {
                charge.setAmount(amountResolver.resolve(charge.getChargeCalculationType(), charge.getPercentage(), disbursedAmount));
            }
            charge.setDueDate(disbursementDate);
        }
        chargeRepository.saveAllAndFlush(charges);
        return charges;
    }

    @Transactional
    @Override
    public void reverseChargesOnUndoDisbursal(final WorkingCapitalLoan loan) {
        final List<WorkingCapitalLoanTransaction> settlements = transactionRepository
                .findByWcLoan_IdOrderByTransactionDateAscIdAsc(loan.getId()).stream()
                .filter(txn -> txn.getTypeOf() == LoanTransactionType.REPAYMENT_AT_DISBURSEMENT).toList();
        loan.setNetDisbursalAmount(null);
        if (settlements.isEmpty()) {
            return;
        }
        final List<WorkingCapitalLoanChargePaidBy> paidLines = chargePaidByRepository
                .findByTransactionIdIn(settlements.stream().map(WorkingCapitalLoanTransaction::getId).toList());
        final WorkingCapitalLoanBalance balance = balanceRepository.findByWcLoan_Id(loan.getId())
                .orElseGet(() -> WorkingCapitalLoanBalance.createFor(loan));
        for (final WorkingCapitalLoanChargePaidBy line : paidLines) {
            final WorkingCapitalLoanCharge charge = line.getWcLoanCharge();
            charge.setAmountPaid(MathUtil.nullToZero(charge.getAmountPaid()).subtract(line.getAmount()).max(BigDecimal.ZERO));
            charge.setPaid(false);
            charge.setDueDate(null);
            chargeRepository.save(charge);
            if (charge.isPenaltyCharge()) {
                balance.setPenalty(MathUtil.nullToZero(balance.getPenalty()).subtract(line.getAmount()).max(BigDecimal.ZERO));
                balance.setPenaltyPaid(MathUtil.nullToZero(balance.getPenaltyPaid()).subtract(line.getAmount()).max(BigDecimal.ZERO));
            } else {
                balance.setFee(MathUtil.nullToZero(balance.getFee()).subtract(line.getAmount()).max(BigDecimal.ZERO));
                balance.setFeePaid(MathUtil.nullToZero(balance.getFeePaid()).subtract(line.getAmount()).max(BigDecimal.ZERO));
            }
        }
        balanceRepository.saveAndFlush(balance);
        chargePaidByRepository.deleteAll(paidLines);
        chargePaidByRepository.flush();
        chargeRepository.flush();

        // Only the settlements that still had paid lines were live until this undo; an older, already-undone one has no
        // lines left and its journal entries were mirrored back then - mirroring them again would double the reversal.
        final Set<Long> liveSettlementIds = paidLines.stream().map(line -> line.getWcLoanTransaction().getId()).collect(Collectors.toSet());
        if (loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization()) {
            settlements.stream().filter(settlement -> settlement.isReversed() && liveSettlementIds.contains(settlement.getId()))
                    .forEach(settlement -> accountingProcessor.postReversalJournalEntries(loan, settlement));
        }
    }

    @Override
    public List<WorkingCapitalLoanChargePaidBy> reapplyOnReprocess(final WorkingCapitalLoan loan, final WorkingCapitalLoanBalance balance,
            final List<WorkingCapitalLoanCharge> charges, final List<WorkingCapitalLoanTransaction> allTransactions) {
        final List<WorkingCapitalLoanTransaction> settlements = allTransactions.stream()
                .filter(txn -> !txn.isReversed() && txn.getTypeOf() == LoanTransactionType.REPAYMENT_AT_DISBURSEMENT).toList();
        if (settlements.isEmpty()) {
            return List.of();
        }
        // A settlement pays every disbursement charge on the loan, in full, at disbursement. Once disbursed no
        // disbursement charge can be added or removed, so "the active disbursement charges" is exactly what it paid.
        final WorkingCapitalLoanTransaction settlement = settlements.get(settlements.size() - 1);
        final List<WorkingCapitalLoanChargePaidBy> lines = new ArrayList<>();
        for (final WorkingCapitalLoanCharge charge : charges) {
            if (!charge.isDisbursementCharge()) {
                continue;
            }
            final WorkingCapitalLoanChargePaidBy paidBy = chargePaymentHandler.applyChargePayment(settlement, charge, charge.getAmount());
            if (paidBy == null) {
                continue;
            }
            lines.add(paidBy);
            if (charge.isPenaltyCharge()) {
                balance.setPenaltyPaid(MathUtil.nullToZero(balance.getPenaltyPaid()).add(charge.getAmount()));
            } else {
                balance.setFeePaid(MathUtil.nullToZero(balance.getFeePaid()).add(charge.getAmount()));
            }
        }
        return lines;
    }
}
