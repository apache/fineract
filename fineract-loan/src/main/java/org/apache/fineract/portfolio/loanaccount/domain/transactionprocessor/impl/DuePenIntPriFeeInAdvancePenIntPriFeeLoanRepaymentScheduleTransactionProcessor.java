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
package org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeEffectiveDueDateComparator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.AbstractLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;

/**
 * `First due/late penalty, interest, principal, fee, after in advance penalty, interest, principal, fee` style
 * {@link LoanRepaymentScheduleTransactionProcessor}.
 *
 * For ALL types of transactions, pays off components in order of: Due/late penalty, Due/late interest, Due/late
 * principal, Due/late fee, In advance penalty, In advance interest In advance principal, In advance fee
 */
@SuppressWarnings("unused")
public class DuePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessor
        extends AbstractLoanRepaymentScheduleTransactionProcessor {

    public static final String STRATEGY_CODE = "due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy";

    public static final String STRATEGY_NAME = "Due penalty, interest, principal, fee, In advance penalty, interest, principal, fee";

    @Override
    public String getCode() {
        return STRATEGY_CODE;
    }

    @Override
    public String getName() {
        return STRATEGY_NAME;
    }

    /**
     * For early/'in advance' repayments
     */
    @Override
    protected Money handleTransactionThatIsPaymentInAdvanceOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final List<LoanRepaymentScheduleInstallment> installments, final LoanTransaction loanTransaction, final Money inAdvancePayment,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Set<LoanCharge> charges) {
        final LocalDate transactionDate = loanTransaction.getTransactionDate();

        final MonetaryCurrency currency = inAdvancePayment.getCurrency();
        Money transactionAmountRemaining = inAdvancePayment;
        Money principalPortion = Money.zero(currency);
        Money interestPortion = Money.zero(currency);
        Money feeChargesPortion = Money.zero(currency);
        Money penaltyChargesPortion = Money.zero(currency);

        if (loanTransaction.isChargesWaiver()) {
            penaltyChargesPortion = currentInstallment.waivePenaltyChargesComponent(transactionDate,
                    loanTransaction.getPenaltyChargesPortion(currency));
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);

            feeChargesPortion = currentInstallment.waiveFeeChargesComponent(transactionDate,
                    loanTransaction.getFeeChargesPortion(currency));
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);

        } else if (loanTransaction.isInterestWaiver()) {
            interestPortion = currentInstallment.waiveInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);

            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
        } else if (loanTransaction.isChargePayment()) {
            if (loanTransaction.isPenaltyPayment()) {
                penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
            } else {
                feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
            }
            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
        } else {
            // Due penalty, interest, principal, fee, In advance penalty, interest, principal, fee
            boolean ignoreDueDateCheck = false;
            boolean rerun = false;

            List<LoanCharge> orderedLoanChargesByDueDate = charges.stream().filter(LoanCharge::isActive).filter(LoanCharge::isChargePending)
                    .filter(loanCharge -> loanCharge.getEffectiveDueDate() == null
                            || !DateUtils.isAfter(loanCharge.getEffectiveDueDate(), transactionDate))
                    .sorted(LoanChargeEffectiveDueDateComparator.INSTANCE).toList();
            Money calculatedPenaltyCharge = Money.zero(currency);
            Money calculatedFeeCharge = Money.zero(currency);
            // Calculate the amount of due charges
            for (LoanCharge charge : orderedLoanChargesByDueDate) {
                if (charge.isPenaltyCharge()) {
                    calculatedPenaltyCharge = calculatedPenaltyCharge.add(charge.getAmount(currency));
                } else {
                    calculatedFeeCharge = calculatedFeeCharge.add(charge.getAmount(currency));
                }
            }

            do {
                Money subPenaltyPortion;
                if (!ignoreDueDateCheck) {
                    if (calculatedPenaltyCharge.isGreaterThan(transactionAmountRemaining)) {
                        calculatedPenaltyCharge = transactionAmountRemaining;
                    }
                } else {
                    calculatedPenaltyCharge = transactionAmountRemaining;
                }
                subPenaltyPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, calculatedPenaltyCharge);
                transactionAmountRemaining = transactionAmountRemaining.minus(subPenaltyPortion);
                penaltyChargesPortion = penaltyChargesPortion.add(subPenaltyPortion);

                Money subInterestPortion;
                if (ignoreDueDateCheck || !DateUtils.isBefore(transactionDate, currentInstallment.getDueDate())) {
                    subInterestPortion = currentInstallment.payInterestComponent(transactionDate, transactionAmountRemaining);
                    transactionAmountRemaining = transactionAmountRemaining.minus(subInterestPortion);
                    interestPortion = interestPortion.add(subInterestPortion);

                    Money subPrincipalPortion = currentInstallment.payPrincipalComponent(transactionDate, transactionAmountRemaining);
                    transactionAmountRemaining = transactionAmountRemaining.minus(subPrincipalPortion);
                    principalPortion = principalPortion.add(subPrincipalPortion);
                }

                Money subFeePortion;
                if (!ignoreDueDateCheck) {
                    if (calculatedFeeCharge.isGreaterThan(transactionAmountRemaining)) {
                        calculatedFeeCharge = transactionAmountRemaining;
                    }
                } else {
                    calculatedFeeCharge = transactionAmountRemaining;
                }
                subFeePortion = currentInstallment.payFeeChargesComponent(transactionDate, calculatedFeeCharge);
                transactionAmountRemaining = transactionAmountRemaining.minus(subFeePortion);
                feeChargesPortion = feeChargesPortion.add(subFeePortion);

                // If the transactionAmountRemaining is greater than zero, rerun the allocation without due date check
                // to distribute the in advance portions
                if (transactionAmountRemaining.isGreaterThanZero()) {
                    ignoreDueDateCheck = true;
                }
                rerun = !rerun;
            } while (ignoreDueDateCheck && rerun);
            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
        }
        if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                    principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion));
        }
        return transactionAmountRemaining;
    }

    /**
     * For late repayments
     */
    @Override
    protected Money handleTransactionThatIsALateRepaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final List<LoanRepaymentScheduleInstallment> installments, final LoanTransaction loanTransaction, final Money latePayment,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Set<LoanCharge> charges) {

        return handleTransactionThatIsOnTimePaymentOfInstallment(currentInstallment, loanTransaction, latePayment, transactionMappings,
                charges);
    }

    /**
     * For normal on-time repayments
     */
    @Override
    protected Money handleTransactionThatIsOnTimePaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final LoanTransaction loanTransaction, final Money onTimePayment,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Set<LoanCharge> charges) {

        final LocalDate transactionDate = loanTransaction.getTransactionDate();

        final MonetaryCurrency currency = onTimePayment.getCurrency();
        Money transactionAmountRemaining = onTimePayment;
        Money principalPortion = Money.zero(currency);
        Money interestPortion = Money.zero(currency);
        Money feeChargesPortion = Money.zero(currency);
        Money penaltyChargesPortion = Money.zero(currency);

        if (loanTransaction.isChargesWaiver()) {
            penaltyChargesPortion = currentInstallment.waivePenaltyChargesComponent(transactionDate,
                    loanTransaction.getPenaltyChargesPortion(currency));
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);

            feeChargesPortion = currentInstallment.waiveFeeChargesComponent(transactionDate,
                    loanTransaction.getFeeChargesPortion(currency));
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);

        } else if (loanTransaction.isInterestWaiver()) {
            interestPortion = currentInstallment.waiveInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);

            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
        } else if (loanTransaction.isChargePayment()) {
            if (loanTransaction.isPenaltyPayment()) {
                penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
            } else {
                feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
            }
            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
        } else {
            Money subPenaltyPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(subPenaltyPortion);
            penaltyChargesPortion = penaltyChargesPortion.add(subPenaltyPortion);

            Money subInterestPortion = currentInstallment.payInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(subInterestPortion);
            interestPortion = interestPortion.add(subInterestPortion);

            Money subPrincipalPortion = currentInstallment.payPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(subPrincipalPortion);
            principalPortion = principalPortion.add(subPrincipalPortion);

            Money subFeePortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(subFeePortion);
            feeChargesPortion = feeChargesPortion.add(subFeePortion);

            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
        }
        if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                    principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion));
        }
        return transactionAmountRemaining;
    }

    @Override
    protected Money handleRefundTransactionPaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final LoanTransaction loanTransaction, final Money transactionAmountUnprocessed,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings) {

        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        final MonetaryCurrency currency = transactionAmountUnprocessed.getCurrency();
        Money transactionAmountRemaining = transactionAmountUnprocessed;
        Money principalPortion = Money.zero(currency);
        Money interestPortion = Money.zero(currency);
        Money feeChargesPortion = Money.zero(currency);
        Money penaltyChargesPortion = Money.zero(currency);

        principalPortion = currentInstallment.unpayPrincipalComponent(transactionDate, transactionAmountRemaining);
        transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);

        if (transactionAmountRemaining.isGreaterThanZero()) {
            interestPortion = currentInstallment.unpayInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);
        }

        if (transactionAmountRemaining.isGreaterThanZero()) {
            feeChargesPortion = currentInstallment.unpayFeeChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
        }

        if (transactionAmountRemaining.isGreaterThanZero()) {
            penaltyChargesPortion = currentInstallment.unpayPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
        }
        loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
        if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                    principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion));
        }
        return transactionAmountRemaining;
    }
}
