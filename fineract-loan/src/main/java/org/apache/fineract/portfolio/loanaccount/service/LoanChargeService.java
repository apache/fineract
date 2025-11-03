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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.service.TemporaryConfigurationServiceContainer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.exception.LoanChargeWithoutMandatoryFieldException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
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
    private final LoanBalanceService loanBalanceService;

    public void recalculateAllCharges(final Loan loan) {
        Set<LoanCharge> charges = loan.getActiveCharges();
        int penaltyWaitPeriod = 0;
        for (final LoanCharge loanCharge : charges) {
            recalculateLoanCharge(loan, loanCharge, penaltyWaitPeriod);
        }
        loan.updateSummaryWithTotalFeeChargesDueAtDisbursement(loan.deriveSumTotalOfChargesDueAtDisbursement());
    }

    public void recalculateParticularChargesAfterTransactionOccurs(final Loan loan, final List<LoanCharge> loanCharges,
            final LocalDate transactionDate) {
        for (final LoanCharge loanCharge : loanCharges) {
            recalculateLoanCharge(loan, loanCharge, 0, transactionDate);
        }
        loan.updateSummaryWithTotalFeeChargesDueAtDisbursement(loan.deriveSumTotalOfChargesDueAtDisbursement());
    }

    public void recalculateLoanCharge(final Loan loan, final LoanCharge loanCharge, final int penaltyWaitPeriod) {
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal chargeAmt;
        BigDecimal totalChargeAmt = BigDecimal.ZERO;
        if (loanCharge.getChargeCalculation().isPercentageBased()) {
            if (loanCharge.isOverdueInstallmentCharge()) {
                amount = calculateOverdueAmountPercentageAppliedTo(loan, loanCharge, penaltyWaitPeriod);
            } else {
                amount = calculateAmountPercentageAppliedTo(loan, loanCharge);
            }
            chargeAmt = loanCharge.getPercentage();
            if (loanCharge.isInstalmentFee()) {
                totalChargeAmt = calculatePerInstallmentChargeAmount(loan, loanCharge);
            }
        } else {
            chargeAmt = loanCharge.amountOrPercentage();
        }
        if (loanCharge.isActive()) {
            clearLoanInstallmentChargesBeforeRegeneration(loanCharge);
            update(loanCharge, chargeAmt, loanCharge.getDueLocalDate(), amount, loan.fetchNumberOfInstallmentsAfterExceptions(),
                    totalChargeAmt);
            loanChargeValidator.validateChargeHasValidSpecifiedDateIfApplicable(loan, loanCharge, loan.getDisbursementDate());
        }
    }

    public void recalculateLoanCharge(final Loan loan, final LoanCharge loanCharge, final int penaltyWaitPeriod,
            final LocalDate transactionDate) {
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal chargeAmt;
        BigDecimal totalChargeAmt = BigDecimal.ZERO;
        if (loanCharge.getChargeCalculation().isPercentageBased()) {
            if (loanCharge.isOverdueInstallmentCharge()) {
                amount = calculateOverdueAmountPercentageAppliedTo(loan, loanCharge, penaltyWaitPeriod);
            } else {
                amount = calculateAmountPercentageAppliedTo(loan, loanCharge);
            }
            chargeAmt = loanCharge.getPercentage();
            if (loanCharge.isInstalmentFee()) {
                totalChargeAmt = calculatePerInstallmentChargeAmount(loan, loanCharge);
            }
        } else {
            chargeAmt = loanCharge.amountOrPercentage();
        }
        if (loanCharge.isActive()) {
            clearLoanInstallmentChargesBeforeRegeneration(loanCharge);
            update(loanCharge, chargeAmt, loanCharge.getDueLocalDate(), amount, loan.fetchNumberOfInstallmentsAfterExceptions(),
                    totalChargeAmt, transactionDate);
            loanChargeValidator.validateChargeHasValidSpecifiedDateIfApplicable(loan, loanCharge, loan.getDisbursementDate());
        }
    }

    public void makeChargePayment(final Loan loan, final Long chargeId, final LoanTransaction paymentTransaction,
            final Integer installmentNumber) {
        loanChargeValidator.validateChargePaymentNotInFuture(paymentTransaction);
        LoanCharge charge = null;
        for (final LoanCharge loanCharge : loan.getCharges()) {
            if (loanCharge.isActive() && chargeId.equals(loanCharge.getId())) {
                charge = loanCharge;
            }
        }
        handleChargePaidTransaction(loan, charge, paymentTransaction, installmentNumber);
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
                totalChargeAmt = calculatePerInstallmentChargeAmount(loan, loanCharge);
            } else if (loanCharge.isOverdueInstallmentCharge()) {
                totalChargeAmt = loanCharge.amountOutstanding();
            }
        } else {
            chargeAmt = loanCharge.amountOrPercentage();
        }
        update(loanCharge, chargeAmt, loanCharge.getDueLocalDate(), amount, loan.fetchNumberOfInstallmentsAfterExceptions(),
                totalChargeAmt);

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
        loanBalanceService.updateLoanSummaryDerivedFields(loan);

        loanLifecycleStateMachine.transition(LoanEvent.LOAN_CHARGE_ADDED, loan);
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
                loan.getLoanCharges().add(loanCharge);
            } else {
                charge = loan.fetchLoanChargesById(charge.getId());
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
                    totalChargeAmt = calculatePerInstallmentChargeAmount(loan, loanCharge);
                }
            } else {
                chargeAmt = loanCharge.amountOrPercentage();
            }
            if (charge != null) {
                update(charge, chargeAmt, loanCharge.getDueLocalDate(), amount, loan.fetchNumberOfInstallmentsAfterExceptions(),
                        totalChargeAmt);
            }
        }

        /* Updated deleted charges **/
        for (Long id : existingCharges) {
            loan.fetchLoanChargesById(id).setActive(false);
        }
        loan.updateSummaryWithTotalFeeChargesDueAtDisbursement(loan.deriveSumTotalOfChargesDueAtDisbursement());
    }

    public BigDecimal calculatePerInstallmentChargeAmount(final Loan loan, final ChargeCalculationType calculationType,
            final BigDecimal percentage) {
        Money amount = Money.zero(loan.getCurrency());
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            amount = amount.plus(calculateInstallmentChargeAmount(loan, calculationType, percentage, installment));
        }
        return amount.getAmount();
    }

    public Map<String, Object> update(final JsonCommand command, final BigDecimal amount, final LoanCharge loanCharge) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        final String dueDateParamName = "dueDate";
        if (command.isChangeInLocalDateParameterNamed(dueDateParamName, loanCharge.getDueLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(dueDateParamName);
            actualChanges.put(dueDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            loanCharge.setDueDate(command.localDateValueOfParameterNamed(dueDateParamName));
        }

        final String amountParamName = "amount";
        if (command.isChangeInBigDecimalParameterNamed(amountParamName, loanCharge.getAmount())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(amountParamName);
            BigDecimal loanChargeAmount;
            actualChanges.put(amountParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            switch (loanCharge.getChargeCalculation()) {
                case INVALID:
                break;
                case FLAT:
                    if (loanCharge.isInstalmentFee()) {
                        loanCharge.setAmount(
                                newValue.multiply(BigDecimal.valueOf(loanCharge.getLoan().fetchNumberOfInstallmentsAfterExceptions())));
                    } else {
                        loanCharge.setAmount(newValue);
                    }
                    loanCharge.setAmountOutstanding(loanCharge.calculateOutstanding());
                break;
                case PERCENT_OF_AMOUNT:
                case PERCENT_OF_AMOUNT_AND_INTEREST:
                case PERCENT_OF_INTEREST:
                case PERCENT_OF_DISBURSEMENT_AMOUNT:
                    loanCharge.setPercentage(newValue);
                    loanCharge.setAmountPercentageAppliedTo(amount);
                    loanChargeAmount = BigDecimal.ZERO;
                    if (loanCharge.isInstalmentFee()) {
                        loanChargeAmount = calculatePerInstallmentChargeAmount(loanCharge.getLoan(), loanCharge.getChargeCalculation(),
                                loanCharge.getPercentage());
                    }
                    if (loanChargeAmount.compareTo(BigDecimal.ZERO) == 0) {
                        loanChargeAmount = loanCharge.percentageOf(loanCharge.getAmountPercentageAppliedTo());
                    }
                    loanCharge.setAmount(loanCharge.minimumAndMaximumCap(loanChargeAmount));
                    loanCharge.setAmountOutstanding(loanCharge.calculateOutstanding());
                break;
            }
            loanCharge.setAmountOrPercentage(newValue);
            if (loanCharge.isInstalmentFee()) {
                updateInstallmentCharges(loanCharge);
            }
        }
        return actualChanges;
    }

    public void populateDerivedFields(final LoanCharge loanCharge, final BigDecimal amountPercentageAppliedTo,
            final BigDecimal chargeAmount, Integer numberOfRepayments, BigDecimal loanChargeAmount) {
        switch (loanCharge.getChargeCalculation()) {
            case INVALID:
                loanCharge.setPercentage(null);
                loanCharge.setAmount(null);
                loanCharge.setAmountPercentageAppliedTo(null);
                loanCharge.setAmountPaid(null);
                loanCharge.setAmountOutstanding(BigDecimal.ZERO);
                loanCharge.setAmountWaived(null);
                loanCharge.setAmountWrittenOff(null);
            break;
            case FLAT:
                loanCharge.setPercentage(null);
                loanCharge.setAmountPercentageAppliedTo(null);
                loanCharge.setAmountPaid(null);
                if (loanCharge.isInstalmentFee()) {
                    if (numberOfRepayments == null) {
                        numberOfRepayments = loanCharge.getLoan().fetchNumberOfInstallmentsAfterExceptions();
                    }
                    loanCharge.setAmount(chargeAmount.multiply(BigDecimal.valueOf(numberOfRepayments)));
                } else {
                    loanCharge.setAmount(chargeAmount);
                }
                loanCharge.setAmountOutstanding(loanCharge.getAmount());
                loanCharge.setAmountWaived(null);
                loanCharge.setAmountWrittenOff(null);
            break;
            case PERCENT_OF_AMOUNT:
            case PERCENT_OF_AMOUNT_AND_INTEREST:
            case PERCENT_OF_INTEREST:
            case PERCENT_OF_DISBURSEMENT_AMOUNT:
                loanCharge.setPercentage(chargeAmount);
                loanCharge.setAmountPercentageAppliedTo(amountPercentageAppliedTo);
                if (loanChargeAmount.compareTo(BigDecimal.ZERO) == 0) {
                    loanChargeAmount = loanCharge.percentageOf(loanCharge.getAmountPercentageAppliedTo());
                }
                loanCharge.setAmount(loanCharge.minimumAndMaximumCap(loanChargeAmount));
                loanCharge.setAmountPaid(null);
                loanCharge.setAmountOutstanding(loanCharge.calculateOutstanding());
                loanCharge.setAmountWaived(null);
                loanCharge.setAmountWrittenOff(null);
            break;
        }
        loanCharge.setAmountOrPercentage(chargeAmount);
        if (loanCharge.getLoan() != null && loanCharge.isInstalmentFee()) {
            updateInstallmentCharges(loanCharge);
        }
    }

    public void update(final LoanCharge loanCharge, final BigDecimal amount, final LocalDate dueDate, final Integer numberOfRepayments) {
        BigDecimal amountPercentageAppliedTo = BigDecimal.ZERO;
        if (loanCharge.getLoan() != null) {
            switch (loanCharge.getChargeCalculation()) {
                case PERCENT_OF_AMOUNT:
                    // If charge type is specified due date and loan is multi disburment loan.
                    // Then we need to get as of this loan charge due date how much amount disbursed.
                    if (loanCharge.getLoan().isMultiDisburmentLoan() && loanCharge.isSpecifiedDueDate()) {
                        for (final LoanDisbursementDetails loanDisbursementDetails : loanCharge.getLoan().getDisbursementDetails()) {
                            if (!DateUtils.isAfter(loanDisbursementDetails.expectedDisbursementDate(), loanCharge.getDueDate())) {
                                amountPercentageAppliedTo = amountPercentageAppliedTo.add(loanDisbursementDetails.principal());
                            }
                        }
                    } else {
                        amountPercentageAppliedTo = loanCharge.getLoan().getPrincipal().getAmount();
                    }
                break;
                case PERCENT_OF_AMOUNT_AND_INTEREST:
                    amountPercentageAppliedTo = loanCharge.getLoan().getPrincipal().getAmount()
                            .add(loanCharge.getLoan().getTotalInterest());
                break;
                case PERCENT_OF_INTEREST:
                    amountPercentageAppliedTo = loanCharge.getLoan().getTotalInterest();
                break;
                case PERCENT_OF_DISBURSEMENT_AMOUNT:
                    LoanTrancheDisbursementCharge loanTrancheDisbursementCharge = loanCharge.getLoanTrancheDisbursementCharge();
                    amountPercentageAppliedTo = loanTrancheDisbursementCharge.getloanDisbursementDetails().principal();
                break;
                default:
                break;
            }
        }
        update(loanCharge, amount, dueDate, amountPercentageAppliedTo, numberOfRepayments, BigDecimal.ZERO);
    }

    public LoanCharge create(final Loan loan, final Charge chargeDefinition, final BigDecimal loanPrincipal, final BigDecimal amount,
            final ChargeTimeType chargeTime, final ChargeCalculationType chargeCalculation, final LocalDate dueDate,
            final ChargePaymentMode chargePaymentMode, final Integer numberOfRepayments, final BigDecimal loanChargeAmount,
            final ExternalId externalId) {
        final LoanCharge loanCharge = new LoanCharge();
        loanCharge.setLoan(loan);
        loanCharge.setCharge(chargeDefinition);
        loanCharge.setSubmittedOnDate(DateUtils.getBusinessLocalDate());
        loanCharge.setPenaltyCharge(chargeDefinition.isPenalty());
        loanCharge.setMinCap(chargeDefinition.getMinCap());
        loanCharge.setMaxCap(chargeDefinition.getMaxCap());
        loanCharge.setChargeTime(chargeTime == null ? chargeDefinition.getChargeTimeType() : chargeTime.getValue());

        if (loanCharge.getChargeTimeType().equals(ChargeTimeType.SPECIFIED_DUE_DATE)
                || loanCharge.getChargeTimeType().equals(ChargeTimeType.OVERDUE_INSTALLMENT)) {

            if (dueDate == null) {
                final String defaultUserMessage = "Loan charge is missing due date.";
                throw new LoanChargeWithoutMandatoryFieldException("loanChargeAmount", "dueDate", defaultUserMessage,
                        chargeDefinition.getId(), chargeDefinition.getName());
            }

            loanCharge.setDueDate(dueDate);
        } else {
            loanCharge.setDueDate(null);
        }

        loanCharge.setChargeCalculation(chargeCalculation == null ? chargeDefinition.getChargeCalculation() : chargeCalculation.getValue());

        BigDecimal chargeAmount = chargeDefinition.getAmount();
        if (amount != null) {
            chargeAmount = amount;
        }

        loanCharge.setChargePaymentMode(chargePaymentMode == null ? chargeDefinition.getChargePaymentMode() : chargePaymentMode.getValue());

        populateDerivedFields(loanCharge, loanPrincipal, chargeAmount, numberOfRepayments, loanChargeAmount);

        loanCharge.setPaid(loanCharge.determineIfFullyPaid());
        loanCharge.setExternalId(externalId);

        return loanCharge;
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

    private void handleChargePaidTransaction(final Loan loan, final LoanCharge charge, final LoanTransaction chargesPayment,
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

    private BigDecimal calculatePerInstallmentChargeAmount(final Loan loan, final LoanCharge loanCharge) {
        return calculatePerInstallmentChargeAmount(loan, loanCharge.getChargeCalculation(), loanCharge.getPercentage());
    }

    public void updateInstallmentCharges(final LoanCharge loanCharge) {
        final List<LoanInstallmentCharge> newChargeInstallments = generateInstallmentLoanCharges(loanCharge.getLoan(), loanCharge);

        if (loanCharge.getLoanInstallmentCharge().isEmpty()) {
            loanCharge.getLoanInstallmentCharge().addAll(newChargeInstallments);
        } else {
            final Map<Integer, LoanInstallmentCharge> newChargeMap = new HashMap<>();
            for (final LoanInstallmentCharge newCharge : newChargeInstallments) {
                if (newCharge.getInstallment() != null && newCharge.getInstallment().getInstallmentNumber() != null) {
                    newChargeMap.put(newCharge.getInstallment().getInstallmentNumber(), newCharge);
                }
            }

            final Collection<LoanInstallmentCharge> chargesToRemoveFromLoanCharge = new HashSet<>();
            final Collection<LoanInstallmentCharge> chargesToAddIntoLoanCharge = new HashSet<>();

            for (final LoanInstallmentCharge oldCharge : loanCharge.getLoanInstallmentCharge()) {
                final Integer oldInstallmentNumber = oldCharge.getInstallment().getInstallmentNumber();

                if (newChargeMap.containsKey(oldInstallmentNumber)) {
                    chargesToRemoveFromLoanCharge.add(oldCharge);
                    oldCharge.getInstallment().getInstallmentCharges().remove(oldCharge);
                    chargesToAddIntoLoanCharge.add(newChargeMap.get(oldInstallmentNumber));
                    newChargeMap.remove(oldInstallmentNumber);
                } else {
                    chargesToRemoveFromLoanCharge.add(oldCharge);
                    oldCharge.getInstallment().getInstallmentCharges().remove(oldCharge);
                }
            }

            chargesToAddIntoLoanCharge.addAll(newChargeMap.values());

            loanCharge.getLoanInstallmentCharge().removeAll(chargesToRemoveFromLoanCharge);
            loanCharge.getLoanInstallmentCharge().addAll(chargesToAddIntoLoanCharge);
        }

        Money totalAmount = Money.zero(loanCharge.getLoan().getCurrency());
        for (LoanInstallmentCharge charge : loanCharge.getLoanInstallmentCharge()) {
            totalAmount = totalAmount.plus(charge.getAmount());
        }
        loanCharge.setAmount(totalAmount.getAmount());
    }

    private List<LoanInstallmentCharge> generateInstallmentLoanCharges(final Loan loan, final LoanCharge loanCharge) {
        final List<LoanInstallmentCharge> loanChargePerInstallments = new ArrayList<>();
        if (loanCharge.isInstalmentFee()) {
            final List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments().stream()
                    .filter(i -> !i.isDownPayment() && !i.isAdditional() && !i.isReAged()).toList();
            for (final LoanRepaymentScheduleInstallment installment : installments) {
                BigDecimal amount;
                if (loanCharge.getChargeCalculation().isFlat()) {
                    amount = loanCharge.amountOrPercentage();
                } else {
                    amount = calculateInstallmentChargeAmount(loan, loanCharge.getChargeCalculation(), loanCharge.getPercentage(),
                            installment).getAmount();
                }
                final LoanInstallmentCharge loanInstallmentCharge = new LoanInstallmentCharge(amount, loanCharge, installment);
                installment.getInstallmentCharges().add(loanInstallmentCharge);
                loanChargePerInstallments.add(loanInstallmentCharge);
            }
        }
        return loanChargePerInstallments;
    }

    private List<LoanInstallmentCharge> generateInstallmentLoanCharges(final Loan loan, final LoanCharge loanCharge,
            final LocalDate transactionDate) {
        final List<LoanInstallmentCharge> loanChargePerInstallments = new ArrayList<>();
        if (loanCharge.isInstalmentFee()) {
            final List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments().stream()
                    .filter(i -> i != null && i.isNotFullyPaidOff() && i.getDueDate() != null && !i.getDueDate().isBefore(transactionDate))
                    .toList();
            for (final LoanRepaymentScheduleInstallment installment : installments) {
                BigDecimal amount;
                if (loanCharge.getChargeCalculation().isFlat()) {
                    amount = loanCharge.amountOrPercentage();
                } else {
                    amount = calculateInstallmentChargeAmount(loan, loanCharge.getChargeCalculation(), loanCharge.getPercentage(),
                            installment).getAmount();
                }
                final LoanInstallmentCharge loanInstallmentCharge = new LoanInstallmentCharge(amount, loanCharge, installment);
                installment.getInstallmentCharges().add(loanInstallmentCharge);
                loanChargePerInstallments.add(loanInstallmentCharge);
            }
        }
        return loanChargePerInstallments;
    }

    public void updateInstallmentCharges(final LoanCharge loanCharge, final LocalDate transactionDate) {
        final List<LoanInstallmentCharge> newChargeInstallments = generateInstallmentLoanCharges(loanCharge.getLoan(), loanCharge,
                transactionDate);

        if (loanCharge.getLoanInstallmentCharge().isEmpty()) {
            loanCharge.getLoanInstallmentCharge().addAll(newChargeInstallments);
        } else {
            final List<LoanInstallmentCharge> oldLoanInstallmentCharges = loanCharge
                    .getLoanInstallmentCharge().stream().filter(i -> i != null && !i.isPaid() && i.getInstallment() != null
                            && i.getInstallment().getDueDate() != null && !i.getInstallment().getDueDate().isBefore(transactionDate))
                    .toList();

            final Map<Integer, LoanInstallmentCharge> newChargeMap = new HashMap<>();
            for (final LoanInstallmentCharge newCharge : newChargeInstallments) {
                if (newCharge.getInstallment() != null && newCharge.getInstallment().getInstallmentNumber() != null) {
                    newChargeMap.put(newCharge.getInstallment().getInstallmentNumber(), newCharge);
                }
            }

            final Collection<LoanInstallmentCharge> chargesToRemoveFromLoanCharge = new HashSet<>();
            final Collection<LoanInstallmentCharge> chargesToAddIntoLoanCharge = new HashSet<>();

            for (final LoanInstallmentCharge oldCharge : oldLoanInstallmentCharges) {
                final Integer oldInstallmentNumber = oldCharge.getInstallment().getInstallmentNumber();

                if (newChargeMap.containsKey(oldInstallmentNumber)) {
                    chargesToRemoveFromLoanCharge.add(oldCharge);
                    oldCharge.getInstallment().getInstallmentCharges().remove(oldCharge);
                    chargesToAddIntoLoanCharge.add(newChargeMap.get(oldInstallmentNumber));
                    newChargeMap.remove(oldInstallmentNumber);
                } else {
                    chargesToRemoveFromLoanCharge.add(oldCharge);
                    oldCharge.getInstallment().getInstallmentCharges().remove(oldCharge);
                }
            }

            chargesToAddIntoLoanCharge.addAll(newChargeMap.values());

            loanCharge.getLoanInstallmentCharge().removeAll(chargesToRemoveFromLoanCharge);
            loanCharge.getLoanInstallmentCharge().addAll(chargesToAddIntoLoanCharge);
        }

        Money totalAmount = Money.zero(loanCharge.getLoan().getCurrency());
        for (LoanInstallmentCharge charge : loanCharge.getLoanInstallmentCharge()) {
            totalAmount = totalAmount.plus(charge.getAmount());
        }
        loanCharge.setAmount(totalAmount.getAmount());
    }

    private BigDecimal calculateOverdueAmountPercentageAppliedTo(final Loan loan, final LoanCharge loanCharge,
            final int penaltyWaitPeriod) {
        LoanRepaymentScheduleInstallment installment = loanCharge.getOverdueInstallmentCharge().getInstallment();
        LocalDate graceDate = DateUtils.getBusinessLocalDate().minusDays(penaltyWaitPeriod);
        Money amount = Money.zero(loan.getCurrency());

        if (DateUtils.isAfter(graceDate, installment.getDueDate())) {
            amount = calculateOverdueAmountPercentageAppliedTo(loan, installment, loanCharge.getChargeCalculation());
            if (!amount.isGreaterThanZero()) {
                loanCharge.setActive(false);
            }
        } else {
            loanCharge.setActive(false);
        }
        return amount.getAmount();
    }

    private Money calculateOverdueAmountPercentageAppliedTo(final Loan loan, final LoanRepaymentScheduleInstallment installment,
            final ChargeCalculationType calculationType) {
        return switch (calculationType) {
            case PERCENT_OF_AMOUNT -> installment.getPrincipalOutstanding(loan.getCurrency());
            case PERCENT_OF_AMOUNT_AND_INTEREST ->
                installment.getPrincipalOutstanding(loan.getCurrency()).plus(installment.getInterestOutstanding(loan.getCurrency()));
            case PERCENT_OF_INTEREST -> installment.getInterestOutstanding(loan.getCurrency());
            default -> Money.zero(loan.getCurrency());
        };
    }

    private void update(final LoanCharge loanCharge, final BigDecimal amount, final LocalDate dueDate, final BigDecimal loanPrincipal,
            Integer numberOfRepayments, BigDecimal loanChargeAmount) {
        if (dueDate != null) {
            loanCharge.setDueDate(dueDate);
        }

        if (amount != null) {
            switch (loanCharge.getChargeCalculation()) {
                case INVALID:
                break;
                case FLAT:
                    if (loanCharge.isInstalmentFee()) {
                        if (numberOfRepayments == null) {
                            numberOfRepayments = loanCharge.getLoan().fetchNumberOfInstallmentsAfterExceptions();
                        }
                        loanCharge.setAmount(amount.multiply(BigDecimal.valueOf(numberOfRepayments)));
                    } else {
                        loanCharge.setAmount(amount);
                    }
                break;
                case PERCENT_OF_AMOUNT:
                case PERCENT_OF_AMOUNT_AND_INTEREST:
                case PERCENT_OF_INTEREST:
                case PERCENT_OF_DISBURSEMENT_AMOUNT:
                    loanCharge.setPercentage(amount);
                    loanCharge.setAmountPercentageAppliedTo(loanPrincipal);
                    if (loanChargeAmount.compareTo(BigDecimal.ZERO) == 0) {
                        loanChargeAmount = loanCharge.percentageOf(loanCharge.getAmountPercentageAppliedTo());
                    }
                    loanCharge.setAmount(loanCharge.minimumAndMaximumCap(loanChargeAmount));
                break;
            }
            loanCharge.setAmountOrPercentage(amount);
            loanCharge.setAmountOutstanding(loanCharge.calculateOutstanding());
            if (loanCharge.getLoan() != null && loanCharge.isInstalmentFee()) {
                updateInstallmentCharges(loanCharge);
            }
        }
    }

    private void update(final LoanCharge loanCharge, final BigDecimal amount, final LocalDate dueDate, final BigDecimal loanPrincipal,
            Integer numberOfRepayments, BigDecimal loanChargeAmount, final LocalDate transactionDate) {
        if (dueDate != null) {
            loanCharge.setDueDate(dueDate);
        }

        if (amount != null) {
            switch (loanCharge.getChargeCalculation()) {
                case INVALID:
                break;
                case FLAT:
                    if (loanCharge.isInstalmentFee()) {
                        if (numberOfRepayments == null) {
                            numberOfRepayments = loanCharge.getLoan().fetchNumberOfInstallmentsAfterExceptions();
                        }
                        loanCharge.setAmount(amount.multiply(BigDecimal.valueOf(numberOfRepayments)));
                    } else {
                        loanCharge.setAmount(amount);
                    }
                break;
                case PERCENT_OF_AMOUNT:
                case PERCENT_OF_AMOUNT_AND_INTEREST:
                case PERCENT_OF_INTEREST:
                case PERCENT_OF_DISBURSEMENT_AMOUNT:
                    loanCharge.setPercentage(amount);
                    loanCharge.setAmountPercentageAppliedTo(loanPrincipal);
                    if (loanChargeAmount.compareTo(BigDecimal.ZERO) == 0) {
                        loanChargeAmount = loanCharge.percentageOf(loanCharge.getAmountPercentageAppliedTo());
                    }
                    loanCharge.setAmount(loanCharge.minimumAndMaximumCap(loanChargeAmount));
                break;
            }
            loanCharge.setAmountOrPercentage(amount);
            loanCharge.setAmountOutstanding(loanCharge.calculateOutstanding());
            if (loanCharge.getLoan() != null && loanCharge.isInstalmentFee()) {
                updateInstallmentCharges(loanCharge, transactionDate);
            }
        }
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

    private List<Long> fetchAllLoanChargeIds(final Loan loan) {
        List<Long> list = new ArrayList<>();
        for (LoanCharge loanCharge : loan.getLoanCharges()) {
            list.add(loanCharge.getId());
        }
        return list;
    }

    private Money calculateInstallmentChargeAmount(final Loan loan, final ChargeCalculationType calculationType,
            final BigDecimal percentage, final LoanRepaymentScheduleInstallment installment) {
        Money percentOf = switch (calculationType) {
            case PERCENT_OF_AMOUNT -> installment.getPrincipal(loan.getCurrency());
            case PERCENT_OF_AMOUNT_AND_INTEREST ->
                installment.getPrincipal(loan.getCurrency()).plus(installment.getInterestCharged(loan.getCurrency()));
            case PERCENT_OF_INTEREST -> installment.getInterestCharged(loan.getCurrency());
            case PERCENT_OF_DISBURSEMENT_AMOUNT, INVALID, FLAT -> Money.zero(loan.getCurrency());

        };
        return Money.zero(loan.getCurrency()) //
                .plus(LoanCharge.percentageOf(percentOf.getAmount(), percentage));
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

}
