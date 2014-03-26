/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.portfolio.loanaccount.data.DisbursementData;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.loanschedule.exception.MultiDisbursementDisbursementDateException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.exception.MultiDisbursementEmiAmountException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.exception.MultiDisbursementOutstandingAmoutException;

/**
 *
 */
public abstract class AbstractLoanScheduleGenerator implements LoanScheduleGenerator {

    private final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
    private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator = new DefaultPaymentPeriodsInOneYearCalculator();

    @Override
    public LoanScheduleModel generate(final MathContext mc, final ApplicationCurrency applicationCurrency,
            final LoanApplicationTerms loanApplicationTerms, final Set<LoanCharge> loanCharges, final boolean isHolidayEnabled,
            final List<Holiday> holidays, final WorkingDays workingDays) {

        // 1. generate list of proposed schedule due dates
        final LocalDate loanEndDate = this.scheduledDateGenerator.getLastRepaymentDate(loanApplicationTerms, isHolidayEnabled, holidays,
                workingDays);
        loanApplicationTerms.updateLoanEndDate(loanEndDate);

        // 2. determine the total charges due at time of disbursement
        final BigDecimal chargesDueAtTimeOfDisbursement = deriveTotalChargesDueAtTimeOfDisbursement(loanCharges);

        // 3. setup variables for tracking important facts required for loan
        // schedule generation.
        Money principalDisbursed = loanApplicationTerms.getPrincipal();
        final Money expectedPrincipalDisburse = loanApplicationTerms.getPrincipal();
        final MonetaryCurrency currency = principalDisbursed.getCurrency();
        final int numberOfRepayments = loanApplicationTerms.getNumberOfRepayments();

        // variables for cumulative totals
        int loanTermInDays = Integer.valueOf(0);
        BigDecimal totalPrincipalExpected = BigDecimal.ZERO;
        final BigDecimal totalPrincipalPaid = BigDecimal.ZERO;
        BigDecimal totalInterestCharged = BigDecimal.ZERO;
        BigDecimal totalFeeChargesCharged = chargesDueAtTimeOfDisbursement;
        BigDecimal totalPenaltyChargesCharged = BigDecimal.ZERO;
        BigDecimal totalRepaymentExpected = chargesDueAtTimeOfDisbursement;
        final BigDecimal totalOutstanding = BigDecimal.ZERO;

        final Collection<LoanScheduleModelPeriod> periods = createNewLoanScheduleListWithDisbursementDetails(numberOfRepayments,
                loanApplicationTerms, chargesDueAtTimeOfDisbursement);

        // 4. Determine the total interest owed over the full loan.
        final Money totalInterestChargedForFullLoanTerm = loanApplicationTerms.calculateTotalInterestCharged(
                this.paymentPeriodsInOneYearCalculator, mc);

        LocalDate periodStartDate = loanApplicationTerms.getExpectedDisbursementDate();
        LocalDate actualRepaymentDate = periodStartDate;
        boolean isFirstRepayment = true;
        LocalDate firstRepaymentdate = this.scheduledDateGenerator.generateNextRepaymentDate(periodStartDate, loanApplicationTerms,
                isFirstRepayment);
        final LocalDate idealDisbursementDate = this.scheduledDateGenerator.idealDisbursementDateBasedOnFirstRepaymentDate(
                loanApplicationTerms.getLoanTermPeriodFrequencyType(), loanApplicationTerms.getRepaymentEvery(), firstRepaymentdate);

        LocalDate periodStartDateApplicableForInterest = periodStartDate;

        int periodNumber = 1;
        Money totalCumulativePrincipal = principalDisbursed.zero();
        Money totalCumulativeInterest = principalDisbursed.zero();
        Money totalOutstandingInterestPaymentDueToGrace = principalDisbursed.zero();
        Money outstandingBalance = principalDisbursed;
        if (loanApplicationTerms.isMultiDisburseLoan()) {
            BigDecimal disburseAmt = getDisbursementAmount(loanApplicationTerms, periodStartDate, periods, chargesDueAtTimeOfDisbursement);
            principalDisbursed = principalDisbursed.zero().plus(disburseAmt);
            loanApplicationTerms.setPrincipal(loanApplicationTerms.getPrincipal().zero().plus(disburseAmt));
            outstandingBalance = outstandingBalance.zero().plus(disburseAmt);
        }
        while (!outstandingBalance.isZero()) {
            actualRepaymentDate = this.scheduledDateGenerator.generateNextRepaymentDate(actualRepaymentDate, loanApplicationTerms,
                    isFirstRepayment);
            isFirstRepayment = false;
            LocalDate scheduledDueDate = this.scheduledDateGenerator.adjustRepaymentDate(actualRepaymentDate, loanApplicationTerms,
                    isHolidayEnabled, holidays, workingDays);
            final int daysInPeriod = Days.daysBetween(periodStartDate, scheduledDueDate).getDays();
            if (loanApplicationTerms.isMultiDisburseLoan()) {
                loanApplicationTerms.setFixedEmiAmountForPeriod(scheduledDueDate);
                BigDecimal disburseAmt = disbursementForPeriod(loanApplicationTerms, periodStartDate, scheduledDueDate, periods,
                        BigDecimal.ZERO);
                principalDisbursed = principalDisbursed.plus(disburseAmt);
                loanApplicationTerms.setPrincipal(loanApplicationTerms.getPrincipal().plus(disburseAmt));
                outstandingBalance = outstandingBalance.plus(disburseAmt);
                if (loanApplicationTerms.getMaxOutstandingBalance() != null
                        && outstandingBalance.isGreaterThan(loanApplicationTerms.getMaxOutstandingBalance())) {
                    String errorMsg = "Outstanding balance must not exceed the amount: " + loanApplicationTerms.getMaxOutstandingBalance();
                    throw new MultiDisbursementOutstandingAmoutException(errorMsg, loanApplicationTerms.getMaxOutstandingBalance()
                            .getAmount(), disburseAmt);
                }
            }
            int daysInPeriodApplicableForInterest = daysInPeriod;

            if (periodStartDate.isBefore(idealDisbursementDate)) {
                if (loanApplicationTerms.getInterestChargedFromLocalDate() != null) {
                    periodStartDateApplicableForInterest = loanApplicationTerms.getInterestChargedFromLocalDate();
                } else {
                    periodStartDateApplicableForInterest = idealDisbursementDate;
                }
                daysInPeriodApplicableForInterest = Days.daysBetween(periodStartDateApplicableForInterest, scheduledDueDate).getDays();
            }

            final double interestCalculationGraceOnRepaymentPeriodFraction = this.paymentPeriodsInOneYearCalculator
                    .calculatePortionOfRepaymentPeriodInterestChargingGrace(periodStartDateApplicableForInterest, scheduledDueDate,
                            loanApplicationTerms.getInterestChargedFromLocalDate(), loanApplicationTerms.getLoanTermPeriodFrequencyType(),
                            loanApplicationTerms.getRepaymentEvery());

            // 5 determine principal,interest of repayment period
            final PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                    this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction, totalCumulativePrincipal,
                    totalCumulativeInterest, totalInterestChargedForFullLoanTerm, totalOutstandingInterestPaymentDueToGrace,
                    daysInPeriodApplicableForInterest, outstandingBalance, loanApplicationTerms, periodNumber, mc);

            if (loanApplicationTerms.getFixedEmiAmount() != null
                    && loanApplicationTerms.getFixedEmiAmount().compareTo(principalInterestForThisPeriod.interest().getAmount()) != 1) {
                String errorMsg = "EMI amount must be greter than : " + principalInterestForThisPeriod.interest().getAmount();
                throw new MultiDisbursementEmiAmountException(errorMsg, principalInterestForThisPeriod.interest().getAmount(),
                        loanApplicationTerms.getFixedEmiAmount());
            }
            // update cumulative fields for principal & interest
            totalCumulativePrincipal = totalCumulativePrincipal.plus(principalInterestForThisPeriod.principal());
            totalCumulativeInterest = totalCumulativeInterest.plus(principalInterestForThisPeriod.interest());
            totalOutstandingInterestPaymentDueToGrace = principalInterestForThisPeriod.interestPaymentDueToGrace();

            // 6. update outstandingLoanBlance using correct 'principalDue'
            outstandingBalance = outstandingBalance.minus(principalInterestForThisPeriod.principal());

            Money feeChargesForInstallment = principalDisbursed.zero();
            Money penaltyChargesForInstallment = principalDisbursed.zero();
            if (!(loanApplicationTerms.isMultiDisburseLoan() && loanApplicationTerms.getFixedEmiAmount() != null)) {
                // 7. determine fees and penalties
                feeChargesForInstallment = cumulativeFeeChargesDueWithin(periodStartDate, scheduledDueDate, loanCharges, currency,
                        principalInterestForThisPeriod, principalDisbursed, totalInterestChargedForFullLoanTerm, numberOfRepayments);
                penaltyChargesForInstallment = cumulativePenaltyChargesDueWithin(periodStartDate, scheduledDueDate, loanCharges, currency,
                        principalInterestForThisPeriod, principalDisbursed, totalInterestChargedForFullLoanTerm, numberOfRepayments);
            }
            // 8. sum up real totalInstallmentDue from components
            final Money totalInstallmentDue = principalInterestForThisPeriod.principal() //
                    .plus(principalInterestForThisPeriod.interest()) //
                    .plus(feeChargesForInstallment) //
                    .plus(penaltyChargesForInstallment);

            // 9. create repayment period from parts
            final LoanScheduleModelPeriod installment = LoanScheduleModelRepaymentPeriod.repayment(periodNumber, periodStartDate,
                    scheduledDueDate, principalInterestForThisPeriod.principal(), outstandingBalance,
                    principalInterestForThisPeriod.interest(), feeChargesForInstallment, penaltyChargesForInstallment, totalInstallmentDue);
            periods.add(installment);

            // handle cumulative fields
            loanTermInDays += daysInPeriod;
            totalPrincipalExpected = totalPrincipalExpected.add(principalInterestForThisPeriod.principal().getAmount());
            totalInterestCharged = totalInterestCharged.add(principalInterestForThisPeriod.interest().getAmount());
            totalFeeChargesCharged = totalFeeChargesCharged.add(feeChargesForInstallment.getAmount());
            totalPenaltyChargesCharged = totalPenaltyChargesCharged.add(penaltyChargesForInstallment.getAmount());
            totalRepaymentExpected = totalRepaymentExpected.add(totalInstallmentDue.getAmount());
            periodStartDate = scheduledDueDate;
            periodStartDateApplicableForInterest = periodStartDate;

            periodNumber++;
        }

        if (principalDisbursed.isNotEqualTo(expectedPrincipalDisburse)) {
            final String errorMsg = "One of the Disbursement date is not falling on Loan Schedule";
            throw new MultiDisbursementDisbursementDateException(errorMsg);
        }

        if (loanApplicationTerms.isMultiDisburseLoan() && loanApplicationTerms.getFixedEmiAmount() != null && !loanCharges.isEmpty()) {
            // 7. determine fees and penalties
            for (LoanScheduleModelPeriod loanScheduleModelPeriod : periods) {
                if (loanScheduleModelPeriod.isRepaymentPeriod()) {
                    PrincipalInterest principalInterest = new PrincipalInterest(Money.of(currency, loanScheduleModelPeriod.principalDue()),
                            Money.of(currency, loanScheduleModelPeriod.interestDue()), null);
                    Money feeChargesForInstallment = cumulativeFeeChargesDueWithin(loanScheduleModelPeriod.periodFromDate(),
                            loanScheduleModelPeriod.periodDueDate(), loanCharges, currency, principalInterest, principalDisbursed,
                            totalCumulativeInterest, numberOfRepayments);
                    Money penaltyChargesForInstallment = cumulativePenaltyChargesDueWithin(loanScheduleModelPeriod.periodFromDate(),
                            loanScheduleModelPeriod.periodDueDate(), loanCharges, currency, principalInterest, principalDisbursed,
                            totalCumulativeInterest, numberOfRepayments);
                    totalFeeChargesCharged = totalFeeChargesCharged.add(feeChargesForInstallment.getAmount());
                    totalPenaltyChargesCharged = totalPenaltyChargesCharged.add(penaltyChargesForInstallment.getAmount());
                    loanScheduleModelPeriod.addLoanCharges(feeChargesForInstallment.getAmount(), penaltyChargesForInstallment.getAmount());
                }
            }
        }

        return LoanScheduleModel.from(periods, applicationCurrency, loanTermInDays, principalDisbursed, totalPrincipalExpected,
                totalPrincipalPaid, totalInterestCharged, totalFeeChargesCharged, totalPenaltyChargesCharged, totalRepaymentExpected,
                totalOutstanding);
    }

    public abstract PrincipalInterest calculatePrincipalInterestComponentsForPeriod(PaymentPeriodsInOneYearCalculator calculator,
            double interestCalculationGraceOnRepaymentPeriodFraction, Money totalCumulativePrincipal, Money totalCumulativeInterest,
            Money totalInterestDueForLoan, Money cumulatingInterestPaymentDueToGrace, int daysInPeriodApplicableForInterest,
            Money outstandingBalance, LoanApplicationTerms loanApplicationTerms, int periodNumber, MathContext mc);

    protected final boolean isLastRepaymentPeriod(final int numberOfRepayments, final int periodNumber) {
        return periodNumber == numberOfRepayments;
    }

    private BigDecimal deriveTotalChargesDueAtTimeOfDisbursement(final Set<LoanCharge> loanCharges) {
        BigDecimal chargesDueAtTimeOfDisbursement = BigDecimal.ZERO;
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueAtDisbursement()) {
                chargesDueAtTimeOfDisbursement = chargesDueAtTimeOfDisbursement.add(loanCharge.amount());
            }
        }
        return chargesDueAtTimeOfDisbursement;
    }

    private BigDecimal disbursementForPeriod(final LoanApplicationTerms loanApplicationTerms, LocalDate startDate, LocalDate endDate,
            final Collection<LoanScheduleModelPeriod> periods, final BigDecimal chargesDueAtTimeOfDisbursement) {
        BigDecimal principal = BigDecimal.ZERO;
        MonetaryCurrency currency = loanApplicationTerms.getPrincipal().getCurrency();
        for (DisbursementData disbursementData : loanApplicationTerms.getDisbursementDatas()) {
            if (disbursementData.isDueForDisbursement(startDate, endDate)) {
                final LoanScheduleModelDisbursementPeriod disbursementPeriod = LoanScheduleModelDisbursementPeriod.disbursement(
                        disbursementData.disbursementDate(), Money.of(currency, disbursementData.amount()), chargesDueAtTimeOfDisbursement);
                periods.add(disbursementPeriod);
                principal = principal.add(disbursementData.amount());
            }
        }
        return principal;
    }

    private BigDecimal getDisbursementAmount(final LoanApplicationTerms loanApplicationTerms, LocalDate disbursementDate,
            final Collection<LoanScheduleModelPeriod> periods, final BigDecimal chargesDueAtTimeOfDisbursement) {
        BigDecimal principal = BigDecimal.ZERO;
        MonetaryCurrency currency = loanApplicationTerms.getPrincipal().getCurrency();
        for (DisbursementData disbursementData : loanApplicationTerms.getDisbursementDatas()) {
            if (disbursementData.disbursementDate().equals(disbursementDate)) {
                final LoanScheduleModelDisbursementPeriod disbursementPeriod = LoanScheduleModelDisbursementPeriod.disbursement(
                        disbursementData.disbursementDate(), Money.of(currency, disbursementData.amount()), chargesDueAtTimeOfDisbursement);
                periods.add(disbursementPeriod);
                principal = principal.add(disbursementData.amount());
            }
        }
        return principal;
    }

    private Collection<LoanScheduleModelPeriod> createNewLoanScheduleListWithDisbursementDetails(final int numberOfRepayments,
            final LoanApplicationTerms loanApplicationTerms, final BigDecimal chargesDueAtTimeOfDisbursement) {

        Collection<LoanScheduleModelPeriod> periods = null;
        if (loanApplicationTerms.isMultiDisburseLoan()) {
            periods = new ArrayList<LoanScheduleModelPeriod>(numberOfRepayments + loanApplicationTerms.getDisbursementDatas().size());
        } else {
            periods = new ArrayList<LoanScheduleModelPeriod>(numberOfRepayments + 1);
            final LoanScheduleModelDisbursementPeriod disbursementPeriod = LoanScheduleModelDisbursementPeriod.disbursement(
                    loanApplicationTerms, chargesDueAtTimeOfDisbursement);
            periods.add(disbursementPeriod);
        }

        return periods;
    }

    private Money cumulativeFeeChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd, final Set<LoanCharge> loanCharges,
            final MonetaryCurrency monetaryCurrency, final PrincipalInterest principalInterestForThisPeriod,
            final Money principalDisbursed, final Money totalInterestChargedForFullLoanTerm, int numberOfRepayments) {

        Money cumulative = Money.zero(monetaryCurrency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isFeeCharge()) {
                if (loanCharge.isInstalmentFee()) {
                    if (loanCharge.getChargeCalculation().isPercentageBased()) {
                        BigDecimal amount = BigDecimal.ZERO;
                        if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
                            amount = amount.add(principalInterestForThisPeriod.principal().getAmount()).add(
                                    principalInterestForThisPeriod.interest().getAmount());
                        } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
                            amount = amount.add(principalInterestForThisPeriod.interest().getAmount());
                        } else {
                            amount = amount.add(principalInterestForThisPeriod.principal().getAmount());
                        }
                        BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
                        cumulative = cumulative.plus(loanChargeAmt);
                    } else {
                        cumulative = cumulative.plus(loanCharge.amount().divide(BigDecimal.valueOf(numberOfRepayments)));
                    }
                } else if (loanCharge.isOverdueInstallmentCharge()
                        && loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
                        && loanCharge.getChargeCalculation().isPercentageBased()) {
                    cumulative = cumulative.plus(loanCharge.chargeAmount());
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
                        && loanCharge.getChargeCalculation().isPercentageBased()) {
                    BigDecimal amount = BigDecimal.ZERO;
                    if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
                        amount = amount.add(principalDisbursed.getAmount()).add(totalInterestChargedForFullLoanTerm.getAmount());
                    } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
                        amount = amount.add(totalInterestChargedForFullLoanTerm.getAmount());
                    } else {
                        amount = amount.add(principalDisbursed.getAmount());
                    }
                    BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
                    cumulative = cumulative.plus(loanChargeAmt);
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.amount());
                }
            }
        }

        return cumulative;
    }

    private Money cumulativePenaltyChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency monetaryCurrency,
            final PrincipalInterest principalInterestForThisPeriod, final Money principalDisbursed,
            final Money totalInterestChargedForFullLoanTerm, int numberOfRepayments) {

        Money cumulative = Money.zero(monetaryCurrency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                if (loanCharge.isInstalmentFee()) {
                    if (loanCharge.getChargeCalculation().isPercentageBased()) {
                        BigDecimal amount = BigDecimal.ZERO;
                        if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
                            amount = amount.add(principalInterestForThisPeriod.principal().getAmount()).add(
                                    principalInterestForThisPeriod.interest().getAmount());
                        } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
                            amount = amount.add(principalInterestForThisPeriod.interest().getAmount());
                        } else {
                            amount = amount.add(principalInterestForThisPeriod.principal().getAmount());
                        }
                        BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
                        cumulative = cumulative.plus(loanChargeAmt);
                    } else {
                        cumulative = cumulative.plus(loanCharge.amount().divide(BigDecimal.valueOf(numberOfRepayments)));
                    }
                } else if (loanCharge.isOverdueInstallmentCharge()
                        && loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
                        && loanCharge.getChargeCalculation().isPercentageBased()) {
                    cumulative = cumulative.plus(loanCharge.chargeAmount());
                }else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
                        && loanCharge.getChargeCalculation().isPercentageBased()) {
                    BigDecimal amount = BigDecimal.ZERO;
                    if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
                        amount = amount.add(principalDisbursed.getAmount()).add(totalInterestChargedForFullLoanTerm.getAmount());
                    } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
                        amount = amount.add(totalInterestChargedForFullLoanTerm.getAmount());
                    } else {
                        amount = amount.add(principalDisbursed.getAmount());
                    }
                    BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
                    cumulative = cumulative.plus(loanChargeAmt);
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.amount());
                }
            }
        }

        return cumulative;
    }
}