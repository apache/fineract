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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.service.TemporaryConfigurationServiceContainer;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanOverdueInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTrancheDisbursementCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.SingleLoanChargeRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;

@RequiredArgsConstructor
public class LoanChargeService {

    private final LoanChargeValidator loanChargeValidator;
    private final LoanTransactionProcessingService loanTransactionProcessingService;
    private final LoanLifecycleStateMachine loanLifecycleStateMachine;

    public void recalculateAllCharges(final Loan loan) {
        Set<LoanCharge> charges = loan.getActiveCharges();
        int penaltyWaitPeriod = 0;
        for (final LoanCharge loanCharge : charges) {
            recalculateLoanCharge(loan, loanCharge, penaltyWaitPeriod);
        }
        loan.updateSummaryWithTotalFeeChargesDueAtDisbursement(loan.deriveSumTotalOfChargesDueAtDisbursement());
    }

    public void recalculateLoanCharge(final Loan loan, final LoanCharge loanCharge, final int penaltyWaitPeriod) {
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal chargeAmt;
        BigDecimal totalChargeAmt = BigDecimal.ZERO;
        if (loanCharge.getChargeCalculation().isPercentageBased()) {
            if (loanCharge.isOverdueInstallmentCharge()) {
                amount = loan.calculateOverdueAmountPercentageAppliedTo(loanCharge, penaltyWaitPeriod);
            } else {
                amount = calculateAmountPercentageAppliedTo(loan, loanCharge);
            }
            chargeAmt = loanCharge.getPercentage();
            if (loanCharge.isInstalmentFee()) {
                totalChargeAmt = calculatePerInstallmentChargeAmount(loanCharge);
            }
        } else {
            chargeAmt = loanCharge.amountOrPercentage();
        }
        if (loanCharge.isActive()) {
            clearLoanInstallmentChargesBeforeRegeneration(loanCharge);
            loanCharge.update(chargeAmt, loanCharge.getDueLocalDate(), amount, loan.fetchNumberOfInstallmensAfterExceptions(),
                    totalChargeAmt);
            loanChargeValidator.validateChargeHasValidSpecifiedDateIfApplicable(loan, loanCharge, loan.getDisbursementDate());
        }
    }

    private void clearLoanInstallmentChargesBeforeRegeneration(final LoanCharge loanCharge) {
        /*
         * JW https://issues.apache.org/jira/browse/FINERACT-1557 For loan installment charges only : Clear down
         * installment charges from the loanCharge and from each of the repayment installments and allow them to be
         * recalculated fully anew. This patch is to avoid the 'merging' of existing and regenerated installment charges
         * which results in the installment charges being deleted on loan approval if the schedule is regenerated. Not
         * pretty. updateInstallmentCharges in LoanCharge.java: the merging looks like it will work but doesn't so this
         * patch simply hits the part which 'adds all' rather than merge. Possibly an ORM issue. The issue could be to
         * do with the fact that, on approval, the "recalculateLoanCharge" happens twice (probably 2 schedule
         * regenerations) whereas it only happens once on Submit and Disburse (and no problems with them)
         *
         * if (this.loanInstallmentCharge.isEmpty()) { this.loanInstallmentCharge.addAll(newChargeInstallments);
         */
        Loan loan = loanCharge.getLoan();
        if (!loan.isSubmittedAndPendingApproval() && !loan.isApproved()) {
            return;
        } // doing for both just in case status is not
          // updated at this points
        if (loanCharge.isInstalmentFee()) {
            loanCharge.clearLoanInstallmentCharges();
            for (final LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
                if (installment.isRecalculatedInterestComponent()) {
                    continue; // JW: does this in generateInstallmentLoanCharges - but don't understand it
                }
                installment.getInstallmentCharges().clear();
            }
        }
    }

    public void makeChargePayment(final Loan loan, final Long chargeId, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, final LoanTransaction paymentTransaction, final Integer installmentNumber) {
        loanChargeValidator.validateChargePaymentNotInFuture(paymentTransaction);
        existingTransactionIds.addAll(loan.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
        LoanCharge charge = null;
        for (final LoanCharge loanCharge : loan.getCharges()) {
            if (loanCharge.isActive() && chargeId.equals(loanCharge.getId())) {
                charge = loanCharge;
            }
        }
        handleChargePaidTransaction(loan, charge, paymentTransaction, installmentNumber);
    }

    public void handleChargePaidTransaction(final Loan loan, final LoanCharge charge, final LoanTransaction chargesPayment,
            final Integer installmentNumber) {
        chargesPayment.updateLoan(loan);
        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(chargesPayment, charge,
                chargesPayment.getAmount(loan.getCurrency()).getAmount(), installmentNumber);
        chargesPayment.getLoanChargesPaid().add(loanChargePaidBy);
        loan.addLoanTransaction(chargesPayment);
        loanLifecycleStateMachine.transition(LoanEvent.LOAN_CHARGE_PAYMENT, loan);

        final List<LoanRepaymentScheduleInstallment> chargePaymentInstallments = new ArrayList<>();
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper
                .fetchFirstNormalInstallmentNumber(loan.getRepaymentScheduleInstallments());
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            boolean isFirstInstallment = installment.getInstallmentNumber().equals(firstNormalInstallmentNumber);
            if (installment.getInstallmentNumber().equals(installmentNumber) || (installmentNumber == null
                    && charge.isDueInPeriod(installment.getFromDate(), installment.getDueDate(), isFirstInstallment))) {
                chargePaymentInstallments.add(installment);
                break;
            }
        }
        final Set<LoanCharge> loanCharges = new HashSet<>(1);
        loanCharges.add(charge);
        loanTransactionProcessingService.processLatestTransaction(loan.getTransactionProcessingStrategyCode(), chargesPayment,
                new TransactionCtx(loan.getCurrency(), chargePaymentInstallments, loanCharges,
                        new MoneyHolder(loan.getTotalOverpaidAsMoney()), null));

        loanLifecycleStateMachine.determineAndTransition(loan, chargesPayment.getTransactionDate());
    }

    /**
     * Creates a loanTransaction for "Apply Charge Event" with transaction date set to "suppliedTransactionDate". The
     * newly created transaction is also added to the Loan on which this method is called.
     *
     * If "suppliedTransactionDate" is not passed Id, the transaction date is set to the loans due date if the due date
     * is lesser than today's date. If not, the transaction date is set to today's date
     */
    public LoanTransaction handleChargeAppliedTransaction(final Loan loan, final LoanCharge loanCharge,
            final LocalDate suppliedTransactionDate) {
        if (loan.isProgressiveSchedule()) {
            return null;
        }

        return createChargeAppliedTransaction(loan, loanCharge, suppliedTransactionDate);
    }

    public LoanTransaction createChargeAppliedTransaction(final Loan loan, final LoanCharge loanCharge,
            final LocalDate suppliedTransactionDate) {
        final Money chargeAmount = loanCharge.getAmount(loan.getCurrency());
        Money feeCharges = chargeAmount;
        Money penaltyCharges = Money.zero(loan.getCurrency());
        if (loanCharge.isPenaltyCharge()) {
            penaltyCharges = chargeAmount;
            feeCharges = Money.zero(loan.getCurrency());
        }

        LocalDate transactionDate;
        if (suppliedTransactionDate != null) {
            transactionDate = suppliedTransactionDate;
        } else {
            transactionDate = loanCharge.getDueLocalDate();
            final LocalDate currentDate = DateUtils.getBusinessLocalDate();

            // if loan charge is to be applied on a future date, the loan transaction would show today's date as applied
            // date
            if (transactionDate == null || DateUtils.isAfter(transactionDate, currentDate)) {
                transactionDate = currentDate;
            }
        }
        ExternalId externalId = ExternalId.empty();
        if (TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }
        final LoanTransaction applyLoanChargeTransaction = LoanTransaction.accrueLoanCharge(loan, loan.getOffice(), chargeAmount,
                transactionDate, feeCharges, penaltyCharges, externalId);

        Integer installmentNumber = null;
        final LoanRepaymentScheduleInstallment installmentForCharge = loan.getRelatedRepaymentScheduleInstallment(loanCharge.getDueDate());
        if (installmentForCharge != null) {
            installmentForCharge.updateAccrualPortion(installmentForCharge.getInterestAccrued(loan.getCurrency()),
                    installmentForCharge.getFeeAccrued(loan.getCurrency()).add(feeCharges),
                    installmentForCharge.getPenaltyAccrued(loan.getCurrency()).add(penaltyCharges));
            installmentNumber = installmentForCharge.getInstallmentNumber();
        }
        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(applyLoanChargeTransaction, loanCharge,
                loanCharge.getAmount(loan.getCurrency()).getAmount(), installmentNumber);
        applyLoanChargeTransaction.getLoanChargesPaid().add(loanChargePaidBy);
        loan.addLoanTransaction(applyLoanChargeTransaction);
        return applyLoanChargeTransaction;
    }

    public void addLoanCharge(final Loan loan, final LoanCharge loanCharge) {
        loanCharge.update(loan);

        final BigDecimal amount = calculateAmountPercentageAppliedTo(loan, loanCharge);
        BigDecimal chargeAmt;
        BigDecimal totalChargeAmt = BigDecimal.ZERO;
        if (loanCharge.getChargeCalculation().isPercentageBased()) {
            chargeAmt = loanCharge.getPercentage();
            if (loanCharge.isInstalmentFee()) {
                totalChargeAmt = calculatePerInstallmentChargeAmount(loanCharge);
            } else if (loanCharge.isOverdueInstallmentCharge()) {
                totalChargeAmt = loanCharge.amountOutstanding();
            }
        } else {
            chargeAmt = loanCharge.amountOrPercentage();
        }
        loanCharge.update(chargeAmt, loanCharge.getDueLocalDate(), amount, loan.fetchNumberOfInstallmensAfterExceptions(), totalChargeAmt);

        // NOTE: must add new loan charge to set of loan charges before
        // reprocessing the repayment schedule.
        if (loan.getLoanCharges() == null) {
            loan.setCharges(new HashSet<>());
        }
        loan.getLoanCharges().add(loanCharge);
        loan.setSummary(loan.updateSummaryWithTotalFeeChargesDueAtDisbursement(loan.deriveSumTotalOfChargesDueAtDisbursement()));

        // store Id's of existing loan transactions and existing reversed loan transactions
        final SingleLoanChargeRepaymentScheduleProcessingWrapper wrapper = new SingleLoanChargeRepaymentScheduleProcessingWrapper();
        wrapper.reprocess(loan.getCurrency(), loan.getDisbursementDate(), loan.getRepaymentScheduleInstallments(), loanCharge);
        loan.updateLoanSummaryDerivedFields();

        loanLifecycleStateMachine.transition(LoanEvent.LOAN_CHARGE_ADDED, loan);
    }

    public void updateLoanCharges(final Loan loan, final Set<LoanCharge> loanCharges) {
        List<Long> existingCharges = fetchAllLoanChargeIds(loan);

        /* Process new and updated charges **/
        for (final LoanCharge loanCharge : loanCharges) {
            LoanCharge charge = loanCharge;
            // add new charges
            if (loanCharge.getId() == null) {
                LoanTrancheDisbursementCharge loanTrancheDisbursementCharge;
                loanCharge.update(loan);
                if (loan.getLoanProduct().isMultiDisburseLoan() && loanCharge.isTrancheDisbursementCharge()) {
                    loanCharge.getTrancheDisbursementCharge().getloanDisbursementDetails().updateLoan(loan);
                    for (final LoanDisbursementDetails loanDisbursementDetails : loan.getDisbursementDetails()) {
                        if (loanCharge.getTrancheDisbursementCharge().getloanDisbursementDetails().getId() == null
                                && loanCharge.getTrancheDisbursementCharge().getloanDisbursementDetails().equals(loanDisbursementDetails)) {
                            loanTrancheDisbursementCharge = new LoanTrancheDisbursementCharge(loanCharge, loanDisbursementDetails);
                            loanCharge.updateLoanTrancheDisbursementCharge(loanTrancheDisbursementCharge);
                        }
                    }
                }
                loan.addCharge(loanCharge);

            } else {
                charge = fetchLoanChargesById(loan, charge.getId());
                if (charge != null) {
                    existingCharges.remove(charge.getId());
                }
            }
            final BigDecimal amount = calculateAmountPercentageAppliedTo(loan, loanCharge);
            BigDecimal chargeAmt;
            BigDecimal totalChargeAmt = BigDecimal.ZERO;
            if (loanCharge.getChargeCalculation().isPercentageBased()) {
                chargeAmt = loanCharge.getPercentage();
                if (loanCharge.isInstalmentFee()) {
                    totalChargeAmt = calculatePerInstallmentChargeAmount(loanCharge);
                }
            } else {
                chargeAmt = loanCharge.amountOrPercentage();
            }
            if (charge != null) {
                charge.update(chargeAmt, loanCharge.getDueLocalDate(), amount, loan.fetchNumberOfInstallmensAfterExceptions(),
                        totalChargeAmt);
            }

        }

        /* Updated deleted charges **/
        for (Long id : existingCharges) {
            fetchLoanChargesById(loan, id).setActive(false);
        }
        loan.updateSummaryWithTotalFeeChargesDueAtDisbursement(loan.deriveSumTotalOfChargesDueAtDisbursement());
    }

    private List<Long> fetchAllLoanChargeIds(final Loan loan) {
        List<Long> list = new ArrayList<>();
        for (LoanCharge loanCharge : loan.getCharges()) {
            list.add(loanCharge.getId());
        }
        return list;
    }

    public BigDecimal calculateAmountPercentageAppliedTo(final Loan loan, final LoanCharge loanCharge) {
        if (loanCharge.isOverdueInstallmentCharge()) {
            return loanCharge.getAmountPercentageAppliedTo();
        }

        return switch (loanCharge.getChargeCalculation()) {
            case PERCENT_OF_AMOUNT -> getDerivedAmountForCharge(loan, loanCharge);
            case PERCENT_OF_AMOUNT_AND_INTEREST -> {
                final BigDecimal totalInterestCharged = loan.getTotalInterest();
                if (loan.isMultiDisburmentLoan() && loanCharge.isDisbursementCharge()) {
                    yield getTotalAllTrancheDisbursementAmount(loan).getAmount().add(totalInterestCharged);
                } else {
                    yield loan.getPrincipal().getAmount().add(totalInterestCharged);
                }
            }
            case PERCENT_OF_INTEREST -> loan.getTotalInterest();
            case PERCENT_OF_DISBURSEMENT_AMOUNT -> {
                if (loanCharge.getTrancheDisbursementCharge() != null) {
                    yield loanCharge.getTrancheDisbursementCharge().getloanDisbursementDetails().principal();
                } else {
                    yield loan.getPrincipal().getAmount();
                }
            }
            case INVALID, FLAT -> BigDecimal.ZERO;
        };
    }

    private BigDecimal getDerivedAmountForCharge(final Loan loan, final LoanCharge loanCharge) {
        BigDecimal amount = BigDecimal.ZERO;
        if (loan.isMultiDisburmentLoan() && loanCharge.getCharge().getChargeTimeType().equals(ChargeTimeType.DISBURSEMENT.getValue())) {
            amount = loan.getApprovedPrincipal();
        } else {
            // If charge type is specified due date and loan is multi disburment loan.
            // Then we need to get as of this loan charge due date how much amount disbursed.
            if (loanCharge.isSpecifiedDueDate() && loan.isMultiDisburmentLoan()) {
                for (final LoanDisbursementDetails loanDisbursementDetails : loan.getDisbursementDetails()) {
                    if (!DateUtils.isAfter(loanDisbursementDetails.expectedDisbursementDate(), loanCharge.getDueDate())) {
                        amount = amount.add(loanDisbursementDetails.principal());
                    }
                }
            } else {
                amount = loan.getPrincipal().getAmount();
            }
        }
        return amount;
    }

    private Money getTotalAllTrancheDisbursementAmount(final Loan loan) {
        Money amount = Money.zero(loan.getCurrency());
        if (loan.isMultiDisburmentLoan()) {
            for (final LoanDisbursementDetails loanDisbursementDetail : loan.getDisbursementDetails()) {
                amount = amount.plus(loanDisbursementDetail.principal());
            }
        }
        return amount;
    }

    private BigDecimal calculatePerInstallmentChargeAmount(final LoanCharge loanCharge) {
        return loanCharge.calculatePerInstallmentChargeAmount();
    }

    public LoanCharge fetchLoanChargesById(final Loan loan, final Long id) {
        LoanCharge charge = null;
        for (LoanCharge loanCharge : loan.getCharges()) {
            if (id.equals(loanCharge.getId())) {
                charge = loanCharge;
                break;
            }
        }
        return charge;
    }

    /**
     * Update interest recalculation settings if product configuration changes
     */
    public void updateOverdueScheduleInstallment(final Loan loan, final LoanCharge loanCharge) {
        if (loanCharge.isOverdueInstallmentCharge() && loanCharge.isActive()) {
            LoanOverdueInstallmentCharge overdueInstallmentCharge = loanCharge.getOverdueInstallmentCharge();
            if (overdueInstallmentCharge != null) {
                Integer installmentNumber = overdueInstallmentCharge.getInstallment().getInstallmentNumber();
                LoanRepaymentScheduleInstallment installment = loan.fetchRepaymentScheduleInstallment(installmentNumber);
                overdueInstallmentCharge.updateLoanRepaymentScheduleInstallment(installment);
            }
        }
    }

}
