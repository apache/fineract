/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.impl;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.AbstractLoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;

/**
 * Heavensfamily style {@link LoanRepaymentScheduleTransactionProcessor}.
 * 
 * For standard transactions, pays off components in order of interest, then
 * principal.
 * 
 * If a transaction results in an advance payment or overpayment for a given
 * installment, the over paid amount is pay off on the principal component of
 * subsequent installments.
 * 
 * If the entire principal of an installment is paid in advance then the
 * interest component is waived.
 */
@SuppressWarnings("unused")
public class HeavensFamilyLoanRepaymentScheduleTransactionProcessor extends AbstractLoanRepaymentScheduleTransactionProcessor {

    /**
     * For late repayments, pay off in the same way as on-time payments,
     * interest first then principal.
     */
    @Override
    protected Money handleTransactionThatIsALateRepaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final List<LoanRepaymentScheduleInstallment> installments, final LoanTransaction loanTransaction,
            final Money transactionAmountUnprocessed) {

        return handleTransactionThatIsOnTimePaymentOfInstallment(currentInstallment, loanTransaction, transactionAmountUnprocessed);
    }

    @Override
    protected boolean isTransactionInAdvanceOfInstallment(final int currentInstallmentIndex,
            final List<LoanRepaymentScheduleInstallment> installments, final LocalDate transactionDate, final Money transactionAmount) {

        boolean isInAdvance = false;

        LocalDate lastInstallmentDueDate = null;
        int previousInstallmentIndex = 0;
        if (currentInstallmentIndex > 0) {
            previousInstallmentIndex = currentInstallmentIndex - 1;
        }

        final LoanRepaymentScheduleInstallment previousInstallment = installments.get(previousInstallmentIndex);
        lastInstallmentDueDate = previousInstallment.getDueDate();

        isInAdvance = !(transactionDate.isAfter(lastInstallmentDueDate) || (transactionDate.isEqual(lastInstallmentDueDate)));

        return isInAdvance;
    }

    /**
     * For early/'in advance' repayments, pays off principal component only.
     */
    @Override
    protected Money handleTransactionThatIsPaymentInAdvanceOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final List<LoanRepaymentScheduleInstallment> installments, final LoanTransaction loanTransaction,
            final LocalDate transactionDate, final Money paymentInAdvance) {

        final MonetaryCurrency currency = paymentInAdvance.getCurrency();
        Money transactionAmountRemaining = paymentInAdvance;
        Money principalPortion = Money.zero(currency);
        Money interestPortion = Money.zero(currency);
        Money feeChargesPortion = Money.zero(currency);
        Money penaltyChargesPortion = Money.zero(currency);

        if (loanTransaction.isInterestWaiver()) {
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
        } else {

            if (currentInstallment.isPrincipalNotCompleted(currency)) {
                principalPortion = currentInstallment.payPrincipalComponent(transactionDate, transactionAmountRemaining);
                if (currentInstallment.isPrincipalCompleted(currency)) {
                    // FIXME - KW - if auto waiving interest need to create
                    // another transaction to handle this.
                    currentInstallment.waiveInterestComponent(transactionDate, currentInstallment.getInterestCharged(currency));
                }

                loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);

                transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
            }

            // 1. pay of principal with over payment.
            principalPortion = currentInstallment.payPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);

            interestPortion = currentInstallment.payInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);

            feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);

            penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
        }

        loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
        return transactionAmountRemaining;
    }

    /**
     * For normal on-time repayments, pays off interest first, then principal.
     */
    @Override
    protected Money handleTransactionThatIsOnTimePaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final LoanTransaction loanTransaction, final Money transactionAmountUnprocessed) {

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
            loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
        } else if (loanTransaction.isChargePayment()) {
            if (loanTransaction.isPenaltyPayment()) {
                penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
            } else {
                feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
                transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
            }
        } else {
            // 1. pay of principal before interest.

            principalPortion = currentInstallment.payPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);

            interestPortion = currentInstallment.payInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);

            feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);

            penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
        }

        loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
        return transactionAmountRemaining;
    }

    @Override
    protected void onLoanOverpayment(final LoanTransaction loanTransaction, final Money loanOverPaymentAmount) {}
    
    @Override
    protected Money handleRefundTransactionPaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final LoanTransaction loanTransaction, final Money transactionAmountUnprocessed) {

        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        final MonetaryCurrency currency = transactionAmountUnprocessed.getCurrency();
        Money transactionAmountRemaining = transactionAmountUnprocessed;
        Money principalPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money interestPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money feeChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());
        Money penaltyChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());

        if (transactionAmountRemaining.isGreaterThanZero()) {
            penaltyChargesPortion = currentInstallment.unpayPenaltyChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
        }

        if (transactionAmountRemaining.isGreaterThanZero()) {
            feeChargesPortion = currentInstallment.unpayFeeChargesComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
        }

        if (transactionAmountRemaining.isGreaterThanZero()) {
            interestPortion = currentInstallment.unpayInterestComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);
        }

        if (transactionAmountRemaining.isGreaterThanZero()) {
            principalPortion = currentInstallment.unpayPrincipalComponent(transactionDate, transactionAmountRemaining);
            transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
        }

        loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);

        return transactionAmountRemaining;
    }
}