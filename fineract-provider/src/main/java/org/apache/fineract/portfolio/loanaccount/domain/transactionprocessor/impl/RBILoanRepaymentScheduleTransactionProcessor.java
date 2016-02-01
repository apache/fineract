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

import java.util.List;

import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.AbstractLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.joda.time.LocalDate;

/**
 * Adhikar/RBI style {@link LoanRepaymentScheduleTransactionProcessor}.
 * 
 * From https://mifosforge.jira.com/browse/MIFOS-5636:
 * 
 * Per RBI regulations, all interest must be paid (both current and overdue)
 * before principal is paid.
 * 
 * For example on a loan with two installments due (one current and one overdue)
 * of 220 each (200 principal + 20 interest):
 * 
 * Partial Payment of 40 20 Payment to interest on Installment #1 (200 principal
 * remaining) 20 Payment to interest on Installment #2 (200 principal remaining)
 */
public class RBILoanRepaymentScheduleTransactionProcessor extends AbstractLoanRepaymentScheduleTransactionProcessor {

    /**
     * For creocore, early is defined as any date before the installment due
     * date
     */
    @SuppressWarnings("unused")
    @Override
    protected boolean isTransactionInAdvanceOfInstallment(final int currentInstallmentIndex,
            final List<LoanRepaymentScheduleInstallment> installments, final LocalDate transactionDate, final Money transactionAmount) {

        final LoanRepaymentScheduleInstallment currentInstallment = installments.get(currentInstallmentIndex);

        return transactionDate.isBefore(currentInstallment.getDueDate());
    }

    /**
     * For early/'in advance' repayments, pays off principal component only.
     */
    @SuppressWarnings("unused")
    @Override
    protected Money handleTransactionThatIsPaymentInAdvanceOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final List<LoanRepaymentScheduleInstallment> installments, final LoanTransaction loanTransaction,
            final LocalDate transactionDate, final Money paymentInAdvance,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings) {

        return handleTransactionThatIsOnTimePaymentOfInstallment(currentInstallment, loanTransaction, paymentInAdvance, transactionMappings);
    }

    /**
     * For late repayments, pay off in the same way as on-time payments,
     * interest first then principal.
     */
    @Override
    protected Money handleTransactionThatIsALateRepaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final List<LoanRepaymentScheduleInstallment> installments, final LoanTransaction loanTransaction,
            final Money transactionAmountUnprocessed, List<LoanTransactionToRepaymentScheduleMapping> transactionMappings) {

        // pay of overdue and current interest due given transaction date
        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        final MonetaryCurrency currency = transactionAmountUnprocessed.getCurrency();
        Money transactionAmountRemaining = transactionAmountUnprocessed;
        Money interestWaivedPortion = Money.zero(currency);
        Money feeChargesPortion = Money.zero(currency);
        Money penaltyChargesPortion = Money.zero(currency);

        if (loanTransaction.isInterestWaiver()) {
            interestWaivedPortion = currentInstallment.waiveInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestWaivedPortion);

            final Money principalPortion = Money.zero(transactionAmountRemaining.getCurrency());
            loanTransaction.updateComponents(principalPortion, interestWaivedPortion, feeChargesPortion, penaltyChargesPortion);
            if (interestWaivedPortion.isGreaterThanZero()) {
                transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(currentInstallment, principalPortion,
                        interestWaivedPortion, feeChargesPortion, penaltyChargesPortion));
            }
        } else if (loanTransaction.isChargePayment()) {
            final Money principalPortion = Money.zero(currency);
            final Money interestPortion = Money.zero(currency);
            if (loanTransaction.isPenaltyPayment()) {
                penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
            } else {
                feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
            }
            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
            if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
                transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(currentInstallment, principalPortion,
                        interestPortion, feeChargesPortion, penaltyChargesPortion));
            }
        } else {

            final LoanRepaymentScheduleInstallment currentInstallmentBasedOnTransactionDate = nearestInstallment(
                    loanTransaction.getTransactionDate(), installments);

            for (final LoanRepaymentScheduleInstallment installment : installments) {
                if ((installment.isInterestDue(currency) || installment.getFeeChargesOutstanding(currency).isGreaterThanZero() || installment
                        .getPenaltyChargesOutstanding(currency).isGreaterThanZero())
                        && (installment.isOverdueOn(loanTransaction.getTransactionDate()) || installment.getInstallmentNumber().equals(
                                currentInstallmentBasedOnTransactionDate.getInstallmentNumber()))) {
                    penaltyChargesPortion = installment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
                    transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);

                    feeChargesPortion = installment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
                    transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);

                    final Money interestPortion = installment.payInterestComponent(transactionDate, transactionAmountRemaining);
                    transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);

                    final Money principalPortion = Money.zero(currency);
                    loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
                    if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
                        transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(installment, principalPortion,
                                interestPortion, feeChargesPortion, penaltyChargesPortion));
                    }
                }
            }

            // With whatever is remaining, pay off principal components of
            // installments
            for (final LoanRepaymentScheduleInstallment installment : installments) {
                if (installment.isPrincipalNotCompleted(currency) && transactionAmountRemaining.isGreaterThanZero()) {
                    final Money principalPortion = installment.payPrincipalComponent(transactionDate, transactionAmountRemaining);
                    transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);

                    final Money interestPortion = Money.zero(currency);
                    loanTransaction.updateComponents(principalPortion, interestPortion, Money.zero(currency), Money.zero(currency));
                    boolean isMappingUpdated = false;
                    for (LoanTransactionToRepaymentScheduleMapping repaymentScheduleMapping : transactionMappings) {
                        if (repaymentScheduleMapping.getLoanRepaymentScheduleInstallment().getDueDate().equals(installment.getDueDate())) {
                            repaymentScheduleMapping.updateComponents(principalPortion, principalPortion.zero(), principalPortion.zero(),
                                    principalPortion.zero());
                            isMappingUpdated = true;
                            break;
                        }
                    }
                    if (!isMappingUpdated
                            && principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion)
                                    .isGreaterThanZero()) {
                        transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(installment, principalPortion,
                                interestPortion, feeChargesPortion, penaltyChargesPortion));
                    }
                }
            }
        }

        return transactionAmountRemaining;
    }

    private LoanRepaymentScheduleInstallment nearestInstallment(final LocalDate transactionDate,
            final List<LoanRepaymentScheduleInstallment> installments) {

        LoanRepaymentScheduleInstallment nearest = installments.get(0);
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            if (installment.getDueDate().isBefore(transactionDate) || installment.getDueDate().isEqual(transactionDate)) {
                nearest = installment;
            } else if (installment.getDueDate().isAfter(transactionDate)) {
                break;
            }
        }
        return nearest;
    }

    /**
     * For normal on-time repayments, pays off interest first, then principal.
     */
    @Override
    protected Money handleTransactionThatIsOnTimePaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final LoanTransaction loanTransaction, final Money transactionAmountUnprocessed,
            final List<LoanTransactionToRepaymentScheduleMapping> transactionMappings) {

        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        final MonetaryCurrency currency = transactionAmountUnprocessed.getCurrency();
        Money transactionAmountRemaining = transactionAmountUnprocessed;
        Money principalPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money interestPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money feeChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money penaltyChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());

        if (loanTransaction.isChargesWaiver()) {

            penaltyChargesPortion = currentInstallment.waivePenaltyChargesComponent(transactionDate,
                    loanTransaction.getPenaltyChargesPortion(currency));
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);

            feeChargesPortion = currentInstallment
                    .waiveFeeChargesComponent(transactionDate, loanTransaction.getFeeChargesPortion(currency));
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);

        } else if (loanTransaction.isInterestWaiver()) {
            interestPortion = currentInstallment.waiveInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);
        } else if (loanTransaction.isChargePayment()) {
            if (loanTransaction.isPenaltyPayment()) {
                penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
            } else {
                feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
            }
        } else {

            penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);

            feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);

            interestPortion = currentInstallment.payInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);

            principalPortion = currentInstallment.payPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
        }

        loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
        if (principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion).isGreaterThanZero()) {
            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(currentInstallment, principalPortion,
                    interestPortion, feeChargesPortion, penaltyChargesPortion));
        }
        return transactionAmountRemaining;
    }

    @SuppressWarnings("unused")
    @Override
    protected void onLoanOverpayment(final LoanTransaction loanTransaction, final Money loanOverPaymentAmount) {
        // dont do anything for with loan over-payment
    }

    @Override
    public boolean isInterestFirstRepaymentScheduleTransactionProcessor() {
        return true;
    }

    @Override
    protected Money handleRefundTransactionPaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final LoanTransaction loanTransaction, final Money transactionAmountUnprocessed,
            final List<LoanTransactionToRepaymentScheduleMapping> transactionMappings) {

        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        // final MonetaryCurrency currency =
        // transactionAmountUnprocessed.getCurrency();
        Money transactionAmountRemaining = transactionAmountUnprocessed;
        Money principalPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money interestPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money feeChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money penaltyChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());

        if (transactionAmountRemaining.isGreaterThanZero()) {
            principalPortion = currentInstallment.unpayPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
        }

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
            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(currentInstallment, principalPortion,
                    interestPortion, feeChargesPortion, penaltyChargesPortion));
        }
        return transactionAmountRemaining;
    }
}