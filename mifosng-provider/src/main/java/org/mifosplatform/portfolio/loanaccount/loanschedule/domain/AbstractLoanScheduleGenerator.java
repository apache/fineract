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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;
import org.mifosplatform.portfolio.loanaccount.data.DisbursementData;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
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
        final List<RecalculationDetail> diffAmt = null;
        return generate(mc, applicationCurrency, loanApplicationTerms, loanCharges, isHolidayEnabled, holidays, workingDays, diffAmt);
    }

    private LoanScheduleModel generate(final MathContext mc, final ApplicationCurrency applicationCurrency,
            final LoanApplicationTerms loanApplicationTerms, final Set<LoanCharge> loanCharges, final boolean isHolidayEnabled,
            final List<Holiday> holidays, final WorkingDays workingDays, List<RecalculationDetail> diffAmt) {

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

        // 4. Determine the total interest owed over the full loan for FLAT
        // interest method .
        Money totalInterestChargedForFullLoanTerm = loanApplicationTerms.calculateTotalInterestCharged(
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
        Money reducePrincipal = totalCumulativePrincipal.zero();
        Money fixedEmiAmount = totalCumulativePrincipal.zero();
        while (!outstandingBalance.isZero()) {
            isFirstRepayment = false;
            actualRepaymentDate = this.scheduledDateGenerator.generateNextRepaymentDate(actualRepaymentDate, loanApplicationTerms,
                    isFirstRepayment);
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
            Money balanceForcalculation = outstandingBalance;
            if (reducePrincipal.isGreaterThanZero()) {
                switch (loanApplicationTerms.getRescheduleStrategyMethod()) {
                    case REDUCE_EMI_AMOUNT:
                        loanApplicationTerms.setFixedEmiAmount(null);
                        outstandingBalance = outstandingBalance.minus(reducePrincipal);
                        balanceForcalculation = outstandingBalance;
                        totalCumulativePrincipal = totalCumulativePrincipal.plus(reducePrincipal);
                        reducePrincipal = reducePrincipal.zero();
                    break;
                    case REDUCE_NUMBER_OF_INSTALLMENTS:
                        loanApplicationTerms.setFixedEmiAmount(fixedEmiAmount.getAmount());
                        outstandingBalance = outstandingBalance.minus(reducePrincipal);
                        balanceForcalculation = outstandingBalance;
                        totalCumulativePrincipal = totalCumulativePrincipal.plus(reducePrincipal);
                        reducePrincipal = reducePrincipal.zero();
                    break;
                    case RESCHEDULE_NEXT_REPAYMENTS:
                        balanceForcalculation = balanceForcalculation.minus(reducePrincipal);
                    break;
                    default:
                    break;
                }
            }

            if (!balanceForcalculation.isGreaterThanZero()) {
                break;
            }

            // 5 determine principal,interest of repayment period
            final PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                    this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction, totalCumulativePrincipal,
                    totalCumulativeInterest, totalInterestChargedForFullLoanTerm, totalOutstandingInterestPaymentDueToGrace,
                    daysInPeriodApplicableForInterest, balanceForcalculation, loanApplicationTerms, periodNumber, mc);

            if (loanApplicationTerms.getFixedEmiAmount() != null
                    && loanApplicationTerms.getFixedEmiAmount().compareTo(principalInterestForThisPeriod.interest().getAmount()) != 1) {
                String errorMsg = "EMI amount must be greter than : " + principalInterestForThisPeriod.interest().getAmount();
                throw new MultiDisbursementEmiAmountException(errorMsg, principalInterestForThisPeriod.interest().getAmount(),
                        loanApplicationTerms.getFixedEmiAmount());
            }
            // update cumulative fields for principal & interest
            Money interestForThisinstallment = principalInterestForThisPeriod.interest();
            totalCumulativePrincipal = totalCumulativePrincipal.plus(principalInterestForThisPeriod.principal());
            totalCumulativeInterest = totalCumulativeInterest.plus(interestForThisinstallment);
            totalOutstandingInterestPaymentDueToGrace = principalInterestForThisPeriod.interestPaymentDueToGrace();

            // 6. update outstandingLoanBlance using correct 'principalDue'
            outstandingBalance = outstandingBalance.minus(principalInterestForThisPeriod.principal());
            Money actualOutstandingbalance = outstandingBalance;

            Money feeChargesForInstallment = principalDisbursed.zero();
            Money penaltyChargesForInstallment = principalDisbursed.zero();
            Money principalForThisPeriod = principalInterestForThisPeriod.principal();
            if (principalForThisPeriod.isGreaterThan(reducePrincipal)) {
                principalForThisPeriod = principalForThisPeriod.minus(reducePrincipal);
                reducePrincipal = reducePrincipal.zero();
            } else {
                reducePrincipal = reducePrincipal.minus(principalForThisPeriod);
                principalForThisPeriod = principalForThisPeriod.zero();
            }

            if (periodNumber == 1) {
                fixedEmiAmount = principalForThisPeriod.plus(interestForThisinstallment);
            }

            if (diffAmt != null && !diffAmt.isEmpty()) {
                Money interestDueToLatePayment = totalCumulativeInterest.zero();
                Money interestReducedDueToEarlyPayment = totalCumulativeInterest.zero();
                for (RecalculationDetail detail : diffAmt) {
                    if (!detail.isLatePayment() && detail.getStartDate().isAfter(periodStartDate)
                            && !detail.getStartDate().isAfter(scheduledDueDate)) {
                        reducePrincipal = reducePrincipal.plus(detail.getAmount());
                        int diffDays = Days.daysBetween(detail.getStartDate(), scheduledDueDate).getDays();
                        if (diffDays > 0) {
                            reducePrincipal = reducePrincipal.minus(principalForThisPeriod);
                        }
                        if (reducePrincipal.isLessThanZero()) {
                            reducePrincipal = reducePrincipal.zero();
                        } else {
                            Money actualOutstanding = outstandingBalance.minus(reducePrincipal);
                            principalForThisPeriod = reducePrincipal.plus(principalForThisPeriod);
                            if (actualOutstanding.isLessThanZero()) {
                                principalForThisPeriod = principalForThisPeriod.plus(actualOutstanding);
                            }
                        }
                        interestReducedDueToEarlyPayment = interestReducedDueToEarlyPayment.plus(loanApplicationTerms.interestRateFor(
                                this.paymentPeriodsInOneYearCalculator, mc, detail.getAmount(), detail.getStartDate(), scheduledDueDate));

                    } else if (detail.isLatePayment() && detail.isOverlapping(periodStartDate, scheduledDueDate)
                            && periodStartDate.isBefore(LocalDate.now())) {
                        LocalDate fromDate = periodStartDate;
                        LocalDate toDate = scheduledDueDate;
                        if (!detail.getStartDate().isBefore(periodStartDate)) {
                            fromDate = detail.getStartDate();
                        }
                        if (!detail.getToDate().isAfter(scheduledDueDate)) {
                            toDate = detail.getToDate();
                        }
                        if (toDate.isAfter(LocalDate.now())) {
                            toDate = LocalDate.now();
                        }
                        interestDueToLatePayment = interestDueToLatePayment.plus(loanApplicationTerms.interestRateFor(
                                this.paymentPeriodsInOneYearCalculator, mc, detail.getAmount(), fromDate, toDate));
                    }
                }

                if (totalOutstandingInterestPaymentDueToGrace.isGreaterThanZero()) {
                    totalOutstandingInterestPaymentDueToGrace = totalOutstandingInterestPaymentDueToGrace.plus(interestDueToLatePayment)
                            .minus(interestReducedDueToEarlyPayment);
                } else {
                    totalCumulativeInterest = totalCumulativeInterest.plus(interestDueToLatePayment)
                            .minus(interestReducedDueToEarlyPayment);
                    interestForThisinstallment = interestForThisinstallment.plus(interestDueToLatePayment).minus(
                            interestReducedDueToEarlyPayment);
                }

            }
            actualOutstandingbalance = actualOutstandingbalance.minus(reducePrincipal);

            // 8. sum up real totalInstallmentDue from components
            final Money totalInstallmentDue = principalForThisPeriod//
                    .plus(interestForThisinstallment) //
                    .plus(feeChargesForInstallment) //
                    .plus(penaltyChargesForInstallment);

            // 9. create repayment period from parts
            final LoanScheduleModelPeriod installment = LoanScheduleModelRepaymentPeriod.repayment(periodNumber, periodStartDate,
                    scheduledDueDate, principalForThisPeriod, actualOutstandingbalance, interestForThisinstallment,
                    feeChargesForInstallment, penaltyChargesForInstallment, totalInstallmentDue);
            periods.add(installment);

            // handle cumulative fields
            loanTermInDays += daysInPeriod;
            totalPrincipalExpected = totalPrincipalExpected.add(principalInterestForThisPeriod.principal().getAmount());
            totalInterestCharged = totalInterestCharged.add(interestForThisinstallment.getAmount());
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

        if (diffAmt != null && !diffAmt.isEmpty() && !periodStartDate.isAfter(LocalDate.now())) {
            Map<LocalDate, RecalculationDetail> processDetails = new TreeMap<>();
            for (RecalculationDetail detail : diffAmt) {
                if (!periodStartDate.isAfter(detail.getStartDate()) && detail.isLatePayment()) {
                    if (processDetails.containsKey(detail.getToDate())) {
                        RecalculationDetail recalculationDetail = processDetails.get(detail.getToDate());
                        RecalculationDetail updatedDetail = new RecalculationDetail(recalculationDetail.isLatePayment(),
                                recalculationDetail.getStartDate(), recalculationDetail.getToDate(), recalculationDetail.getAmount().plus(
                                        detail.getAmount()));
                        processDetails.put(updatedDetail.getToDate(), updatedDetail);
                    } else {
                        processDetails.put(detail.getToDate(), detail);
                    }
                }
            }

            for (RecalculationDetail detail : processDetails.values()) {
                LocalDate fromDate = detail.getStartDate();
                LocalDate toDate = detail.getToDate();
                if (!toDate.isAfter(LocalDate.now())) {
                    Money interestDueToLatePayment = loanApplicationTerms.interestRateFor(this.paymentPeriodsInOneYearCalculator, mc,
                            detail.getAmount(), fromDate, toDate);

                    totalInterestCharged = totalInterestCharged.add(interestDueToLatePayment.getAmount());
                    totalRepaymentExpected = totalRepaymentExpected.add(interestDueToLatePayment.getAmount());

                    final LoanScheduleModelPeriod installment = LoanScheduleModelRepaymentPeriod.repayment(periodNumber, fromDate, toDate,
                            interestDueToLatePayment.zero(), interestDueToLatePayment.zero(), interestDueToLatePayment,
                            interestDueToLatePayment.zero(), interestDueToLatePayment.zero(), interestDueToLatePayment);
                    periods.add(installment);
                    periodNumber++;
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
            periods = new ArrayList<>(numberOfRepayments + loanApplicationTerms.getDisbursementDatas().size());
        } else {
            periods = new ArrayList<>(numberOfRepayments + 1);
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

    @Override
    public LoanScheduleModel rescheduleNextInstallments(final MathContext mc, final ApplicationCurrency applicationCurrency,
            final LoanApplicationTerms loanApplicationTerms, final Set<LoanCharge> loanCharges, final boolean isHolidayEnabled,
            final List<Holiday> holidays, final WorkingDays workingDays, final List<LoanTransaction> transactions,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor) {

        int installmentNumber = 0;
        final List<RecalculationDetail> recalculationDetails = new ArrayList<>();
        LoanScheduleModel loanScheduleModel = generate(mc, applicationCurrency, loanApplicationTerms, loanCharges, isHolidayEnabled,
                holidays, workingDays);

        while (installmentNumber < loanScheduleModel.getPeriods().size() - 1) {
            RecalculatedSchedule recalculatedSchedule = recalculateInterest(mc, applicationCurrency, loanApplicationTerms, loanCharges,
                    isHolidayEnabled, holidays, workingDays, transactions, loanRepaymentScheduleTransactionProcessor, installmentNumber,
                    loanScheduleModel, recalculationDetails);
            installmentNumber = recalculatedSchedule.getInstallmentNumber();
            loanScheduleModel = recalculatedSchedule.getLoanScheduleModel();
        }
        return loanScheduleModel;

    }

    private RecalculatedSchedule recalculateInterest(final MathContext mc, final ApplicationCurrency applicationCurrency,
            final LoanApplicationTerms loanApplicationTerms, final Set<LoanCharge> loanCharges, final boolean isHolidayEnabled,
            final List<Holiday> holidays, final WorkingDays workingDays, final List<LoanTransaction> transactions,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor, final int installmentNumber,
            LoanScheduleModel model, final List<RecalculationDetail> recalculationDetails) {
        boolean processRecalculate = false;
        int processedInstallmentNumber = installmentNumber;
        final List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>();
        final List<RecalculationDetail> diffAmt = new ArrayList<>();
        Money unpaidPricipal = loanApplicationTerms.getPrincipal();
        for (final LoanScheduleModelPeriod scheduledLoanInstallment : model.getPeriods()) {
            if (scheduledLoanInstallment.isRepaymentPeriod()) {
                final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(null,
                        scheduledLoanInstallment.periodNumber(), scheduledLoanInstallment.periodFromDate(),
                        scheduledLoanInstallment.periodDueDate(), scheduledLoanInstallment.principalDue(),
                        scheduledLoanInstallment.interestDue(), scheduledLoanInstallment.feeChargesDue(),
                        scheduledLoanInstallment.penaltyChargesDue());
                installments.add(installment);
            }
        }
        MonetaryCurrency currency = loanApplicationTerms.getPrincipal().getCurrency();
        final List<LoanRepaymentScheduleInstallment> processinstallmets = new ArrayList<>();

        for (LoanRepaymentScheduleInstallment installment : installments) {
            processinstallmets.add(installment);
            unpaidPricipal = unpaidPricipal.minus(installment.getPrincipal(currency));
            if (installment.getInstallmentNumber() <= installmentNumber) {
                continue;
            }
            processedInstallmentNumber = installment.getInstallmentNumber();
            if (installment.getDueDate().isAfter(LocalDate.now())) {
                continue;
            }
            List<LoanTransaction> transactionsForInstallment = new ArrayList<>();
            Map<LocalDate, LocalDate> recalculationDates = new HashMap<>();
            LocalDate transactionsDate = getNextRestScheduleDate(installment.getDueDate().minusDays(1), loanApplicationTerms,
                    isHolidayEnabled, holidays, workingDays);
            for (LoanTransaction loanTransaction : transactions) {
                LocalDate loantransactionDate = loanTransaction.getTransactionDate();
                if (!loantransactionDate.isAfter(transactionsDate) && !loanTransaction.isAccrual()) {
                    transactionsForInstallment.add(loanTransaction);
                    recalculationDates.put(
                            loantransactionDate,
                            getNextRestScheduleDate(loantransactionDate.minusDays(1), loanApplicationTerms, isHolidayEnabled, holidays,
                                    workingDays));
                }
            }
            Map<LocalDate, Money> earlyPaymentMap = loanRepaymentScheduleTransactionProcessor.handleRecalculation(
                    loanApplicationTerms.getExpectedDisbursementDate(), transactionsForInstallment, currency, processinstallmets,
                    installment, recalculationDates);

            for (Map.Entry<LocalDate, Money> entry : earlyPaymentMap.entrySet()) {
                LocalDate startDate = entry.getKey();
                if (unpaidPricipal.isGreaterThan(entry.getValue())) {
                    startDate = getNextRestScheduleDate(startDate.minusDays(1), loanApplicationTerms, isHolidayEnabled, holidays,
                            workingDays);
                }
                RecalculationDetail recalculationDetail = new RecalculationDetail(false, startDate, null, entry.getValue());
                if (!entry.getKey().isAfter(installment.getDueDate()) && entry.getKey().isAfter(installment.getFromDate())) {
                    diffAmt.add(recalculationDetail);
                }
            }

            if (installment.getDueDate().isBefore(LocalDate.now())) {
                LocalDate startDate = installment.getDueDate();
                Money totalOutstanding = installment.getTotalOutstanding(currency);
                boolean reduceStartDate = false;

                while (totalOutstanding.isGreaterThanZero() && startDate.isBefore(LocalDate.now())) {
                    LocalDate recalculateFrom = getNextRestScheduleDate(startDate.minusDays(1), loanApplicationTerms, isHolidayEnabled,
                            holidays, workingDays);
                    LocalDate recalcualteTill = getNextRestScheduleDate(recalculateFrom, loanApplicationTerms, isHolidayEnabled, holidays,
                            workingDays);
                    if (reduceStartDate) {
                        startDate = startDate.minusDays(1);
                    }
                    applyRest(transactions, startDate, recalculateFrom, installment, loanRepaymentScheduleTransactionProcessor, currency,
                            loanApplicationTerms, isHolidayEnabled, holidays, workingDays, installments);
                    totalOutstanding = installment.getTotalOutstanding(currency);
                    if (totalOutstanding.isGreaterThanZero()) {
                        Money latepaymentoutstanding = installment.getPrincipalOutstanding(currency);
                        switch (loanApplicationTerms.getInterestRecalculationCompoundingMethod()) {
                            case INTEREST:
                                latepaymentoutstanding = latepaymentoutstanding.plus(installment.getInterestOutstanding(currency));
                            break;
                            case INTEREST_AND_FEE:
                                latepaymentoutstanding = latepaymentoutstanding.plus(installment.getInterestOutstanding(currency))
                                        .plus(installment.getFeeChargesOutstanding(currency))
                                        .plus(installment.getPenaltyChargesOutstanding(currency));
                            break;
                            case FEE:
                                latepaymentoutstanding = latepaymentoutstanding.plus(installment.getFeeChargesOutstanding(currency)).plus(
                                        installment.getPenaltyChargesOutstanding(currency));
                            break;

                            default:
                            break;
                        }
                        RecalculationDetail recalculationDetail = new RecalculationDetail(true, recalculateFrom, recalcualteTill,
                                latepaymentoutstanding);
                        diffAmt.add(recalculationDetail);
                    }
                    startDate = recalculateFrom.plusDays(1);
                    reduceStartDate = true;
                }
            }

            if (!diffAmt.isEmpty()) {
                processRecalculate = true;
            }
            break;

        }
        if (processRecalculate) {
            recalculationDetails.addAll(diffAmt);
            model = generate(mc, applicationCurrency, loanApplicationTerms, loanCharges, isHolidayEnabled, holidays, workingDays,
                    recalculationDetails);
        }
        return new RecalculatedSchedule(model, processedInstallmentNumber);
    }

    private void applyRest(List<LoanTransaction> loanTransactions, LocalDate from, LocalDate to,
            LoanRepaymentScheduleInstallment installment,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor, MonetaryCurrency currency,
            final LoanApplicationTerms loanApplicationTerms, final boolean isHolidayEnabled, final List<Holiday> holidays,
            final WorkingDays workingDays, final List<LoanRepaymentScheduleInstallment> installments) {
        List<LoanTransaction> transactions = new ArrayList<>();
        Map<LocalDate, LocalDate> recalculationDates = new HashMap<>();
        for (LoanTransaction transaction : loanTransactions) {
            LocalDate loantransactionDate = transaction.getTransactionDate();
            if (loantransactionDate.isAfter(from) && !transaction.getTransactionDate().isAfter(to) && !transaction.isAccrual()) {
                transactions.add(transaction);
                recalculationDates.put(
                        loantransactionDate,
                        getNextRestScheduleDate(loantransactionDate.minusDays(1), loanApplicationTerms, isHolidayEnabled, holidays,
                                workingDays));
            }
        }

        loanRepaymentScheduleTransactionProcessor.handleRepaymentSchedule(transactions, currency, installments, installment,
                recalculationDates);
    }

    @SuppressWarnings("unused")
    private LocalDate getNextRestScheduleDate(LocalDate startDate, LoanApplicationTerms loanApplicationTerms,
            final boolean isHolidayEnabled, final List<Holiday> holidays, final WorkingDays workingDays) {
        CalendarInstance calendarInstance = loanApplicationTerms.getRestCalendarInstance();
        LocalDate nextScheduleDate = CalendarUtils.getNextScheduleDate(calendarInstance.getCalendar(), startDate);
        /*
         * nextScheduleDate =
         * this.scheduledDateGenerator.adjustRepaymentDate(nextScheduleDate,
         * loanApplicationTerms, isHolidayEnabled, holidays, workingDays);
         */
        return nextScheduleDate;
    }

    @Override
    public Money fetchPrepaymentAmount(final List<LoanRepaymentScheduleInstallment> installments, MonetaryCurrency currency,
            final LoanApplicationTerms applicationTerms, final MathContext mc) {
        Money prepaymentAmount = Money.zero(currency);
        Money amount = Money.zero(currency);
        LocalDate calculateInterestFrom = LocalDate.now();
        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            if (currentInstallment.isNotFullyPaidOff()) {
                if (!currentInstallment.getDueDate().isAfter(LocalDate.now())) {
                    amount = amount.plus(currentInstallment.getTotalOutstanding(currency));
                } else {
                    prepaymentAmount = prepaymentAmount.plus(currentInstallment.getPrincipalOutstanding(currency));
                    if (currentInstallment.getFromDate().isBefore(calculateInterestFrom)) {
                        calculateInterestFrom = currentInstallment.getFromDate();
                    }

                }
            }
        }

        Money interest = applicationTerms.interestRateFor(this.paymentPeriodsInOneYearCalculator, mc, prepaymentAmount,
                calculateInterestFrom, LocalDate.now());
        prepaymentAmount = prepaymentAmount.plus(interest).plus(amount);
        return prepaymentAmount;
    }

}