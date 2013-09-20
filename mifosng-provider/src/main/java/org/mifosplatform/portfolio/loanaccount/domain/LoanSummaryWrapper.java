/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.springframework.stereotype.Component;

/**
 * A wrapper for dealing with side-effect free functionality related to a loans
 * transactions and repayment schedule.
 */
@Component
public final class LoanSummaryWrapper {

    public Money calculateTotalPrincipalRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getPrincipalCompleted(currency));
        }
        return total;
    }

    public Money calculateTotalPrincipalWrittenOff(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getPrincipalWrittenOff(currency));
        }
        return total;
    }

    public Money calculateTotalPrincipalOverdueOn(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency, final LocalDate overdueAsOf) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            if (installment.isOverdueOn(overdueAsOf)) {
                total = total.plus(installment.getPrincipalOutstanding(currency));
            }
        }
        return total;
    }

    public Money calculateTotalInterestCharged(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getInterestCharged(currency));
        }
        return total;
    }

    public Money calculateTotalInterestRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getInterestPaid(currency));
        }
        return total;
    }

    public Money calculateTotalInterestWaived(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getInterestWaived(currency));
        }
        return total;
    }

    public Money calculateTotalInterestWrittenOff(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getInterestWrittenOff(currency));
        }
        return total;
    }

    public Money calculateTotalInterestOverdueOn(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency, final LocalDate overdueAsOf) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            if (installment.isOverdueOn(overdueAsOf)) {
                total = total.plus(installment.getInterestOutstanding(currency));
            }
        }
        return total;
    }

    public Money calculateTotalFeeChargesCharged(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getFeeChargesCharged(currency));
        }
        return total;
    }

    public Money calculateTotalFeeChargesRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getFeeChargesPaid(currency));
        }
        return total;
    }

    public Money calculateTotalFeeChargesWaived(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getFeeChargesWaived(currency));
        }
        return total;
    }

    public Money calculateTotalFeeChargesWrittenOff(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getFeeChargesWrittenOff(currency));
        }
        return total;
    }

    public Money calculateTotalFeeChargesOverdueOn(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency, final LocalDate overdueAsOf) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            if (installment.isOverdueOn(overdueAsOf)) {
                total = total.plus(installment.getFeeChargesOutstanding(currency));
            }
        }
        return total;
    }

    public Money calculateTotalPenaltyChargesCharged(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getPenaltyChargesCharged(currency));
        }
        return total;
    }

    public Money calculateTotalPenaltyChargesRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getPenaltyChargesPaid(currency));
        }
        return total;
    }

    public Money calculateTotalPenaltyChargesWaived(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getPenaltyChargesWaived(currency));
        }
        return total;
    }

    public Money calculateTotalPenaltyChargesWrittenOff(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getPenaltyChargesWrittenOff(currency));
        }
        return total;
    }

    public Money calculateTotalPenaltyChargesOverdueOn(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency, final LocalDate overdueAsOf) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            if (installment.isOverdueOn(overdueAsOf)) {
                total = total.plus(installment.getPenaltyChargesOutstanding(currency));
            }
        }
        return total;
    }

    public Money calculateTotalOverdueOn(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency, final LocalDate overdueAsOf) {

        final Money principalOverdue = calculateTotalPrincipalOverdueOn(repaymentScheduleInstallments, currency, overdueAsOf);
        final Money interestOverdue = calculateTotalInterestOverdueOn(repaymentScheduleInstallments, currency, overdueAsOf);
        final Money feeChargesOverdue = calculateTotalFeeChargesOverdueOn(repaymentScheduleInstallments, currency, overdueAsOf);
        final Money penaltyChargesOverdue = calculateTotalPenaltyChargesOverdueOn(repaymentScheduleInstallments, currency, overdueAsOf);

        return principalOverdue.plus(interestOverdue).plus(feeChargesOverdue).plus(penaltyChargesOverdue);
    }

    public LocalDate determineOverdueSinceDateFrom(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency, final LocalDate from) {

        LocalDate overdueSince = null;
        final Money totalOverdue = calculateTotalOverdueOn(repaymentScheduleInstallments, currency, from);
        if (totalOverdue.isGreaterThanZero()) {
            for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
                if (installment.isOverdueOn(from)) {
                    if (overdueSince == null || overdueSince.isAfter(installment.getDueDate())) {
                        overdueSince = installment.getDueDate();
                    }
                }
            }
        }

        return overdueSince;
    }
}