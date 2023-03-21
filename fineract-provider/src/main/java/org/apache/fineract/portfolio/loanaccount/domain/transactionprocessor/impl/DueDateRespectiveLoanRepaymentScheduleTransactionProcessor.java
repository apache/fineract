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
 * `First due/late charges, interest, principal, after in advance principal, charges, interest` style
 * {@link LoanRepaymentScheduleTransactionProcessor}.
 *
 * For ALL types of transactions, pays off components in order of: Due/late penalty Due/late Fee Due/late interest
 * Due/late principal In advance principal In advance penalty In advance fee In advance interest
 */
@SuppressWarnings("unused")
public class DueDateRespectiveLoanRepaymentScheduleTransactionProcessor extends AbstractLoanRepaymentScheduleTransactionProcessor {

    private static final String STRATEGY_CODE = "due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy";

    private static final String STRATEGY_NAME = "Due penalty, fee, interest, principal, In advance principal, penalty, fee, interest";

    @Override
    public String getCode() {
        return STRATEGY_CODE;
    }

    @Override
    public String getName() {
        return STRATEGY_NAME;
    }

    @Override
    protected boolean isTransactionInAdvanceOfInstallment(final int currentInstallmentIndex,
            final List<LoanRepaymentScheduleInstallment> installments, final LocalDate transactionDate) {

        final LoanRepaymentScheduleInstallment currentInstallment = installments.get(currentInstallmentIndex);

        return transactionDate.isBefore(currentInstallment.getDueDate());
    }

    /**
     * For early/'in advance' repayments
     */
    @Override
    protected Money handleTransactionThatIsPaymentInAdvanceOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final List<LoanRepaymentScheduleInstallment> installments, final LoanTransaction loanTransaction, final Money paymentInAdvance,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Set<LoanCharge> charges) {

        return handleTransactionThatIsOnTimePaymentOfInstallment(currentInstallment, loanTransaction, paymentInAdvance, transactionMappings,
                charges);
    }

    /**
     * For late repayments
     */
    @Override
    protected Money handleTransactionThatIsALateRepaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final List<LoanRepaymentScheduleInstallment> installments, final LoanTransaction loanTransaction,
            final Money transactionAmountUnprocessed, List<LoanTransactionToRepaymentScheduleMapping> transactionMappings,
            Set<LoanCharge> charges) {

        return handleTransactionThatIsOnTimePaymentOfInstallment(currentInstallment, loanTransaction, transactionAmountUnprocessed,
                transactionMappings, charges);
    }

    /**
     * For normal on-time repayments
     */
    @Override
    protected Money handleTransactionThatIsOnTimePaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final LoanTransaction loanTransaction, final Money transactionAmountUnprocessed,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Set<LoanCharge> charges) {

        final LocalDate transactionDate = loanTransaction.getTransactionDate();

        final MonetaryCurrency currency = transactionAmountUnprocessed.getCurrency();
        Money transactionAmountRemaining = transactionAmountUnprocessed;
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
            boolean ignoreDueDateCheck = false;
            boolean rerun = false;

            List<LoanCharge> orderedLoanChargesByDueDate = charges.stream().filter(LoanCharge::isActive).filter(LoanCharge::isNotFullyPaid)
                    .filter(loanCharge -> loanCharge.getEffectiveDueDate() == null
                            || !loanCharge.getEffectiveDueDate().isAfter(transactionDate))
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
                    calculatedPenaltyCharge = transactionAmountUnprocessed;
                }
                subPenaltyPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, calculatedPenaltyCharge);
                transactionAmountRemaining = transactionAmountRemaining.minus(subPenaltyPortion);
                penaltyChargesPortion = penaltyChargesPortion.add(subPenaltyPortion);

                Money subFeePortion;

                if (!ignoreDueDateCheck) {
                    if (calculatedFeeCharge.isGreaterThan(transactionAmountRemaining)) {
                        calculatedFeeCharge = transactionAmountRemaining;
                    }
                } else {
                    calculatedFeeCharge = transactionAmountUnprocessed;
                }
                subFeePortion = currentInstallment.payFeeChargesComponent(transactionDate, calculatedFeeCharge);
                transactionAmountRemaining = transactionAmountRemaining.minus(subFeePortion);
                feeChargesPortion = feeChargesPortion.add(subFeePortion);

                if (ignoreDueDateCheck || !transactionDate.isBefore(currentInstallment.getDueDate())) {
                    interestPortion = currentInstallment.payInterestComponent(transactionDate, transactionAmountRemaining);
                    transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);
                }

                principalPortion = principalPortion
                        .add(currentInstallment.payPrincipalComponent(transactionDate, transactionAmountRemaining));
                transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
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

    @Override
    protected void onLoanOverpayment(final LoanTransaction loanTransaction, final Money loanOverPaymentAmount) {
        // TODO - KW - dont do anything with loan over-payment for now
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
