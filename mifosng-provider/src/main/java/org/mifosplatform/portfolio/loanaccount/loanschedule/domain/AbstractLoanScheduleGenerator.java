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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;
import org.mifosplatform.portfolio.loanaccount.data.DisbursementData;
import org.mifosplatform.portfolio.loanaccount.data.HolidayDetailDTO;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.domain.LoanSummary;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.loanschedule.exception.MultiDisbursementDisbursementDateException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.exception.MultiDisbursementEmiAmountException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.exception.MultiDisbursementOutstandingAmoutException;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleModel;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleModelRepaymentPeriod;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;

/**
 *
 */
public abstract class AbstractLoanScheduleGenerator implements LoanScheduleGenerator {

    private final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
    private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator = new DefaultPaymentPeriodsInOneYearCalculator();

    @Override
    public LoanScheduleModel generate(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            final Set<LoanCharge> loanCharges, final HolidayDetailDTO holidayDetailDTO) {
        final List<RecalculationDetail> diffAmt = null;
        final LocalDate prepayDate = null;
        final LocalDate lastTransactionDate = loanApplicationTerms.getExpectedDisbursementDate();
        return generate(mc, loanApplicationTerms, loanCharges, holidayDetailDTO, diffAmt, prepayDate, lastTransactionDate);
    }

    private LoanScheduleModel generate(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            final Set<LoanCharge> loanCharges, final HolidayDetailDTO holidayDetailDTO, List<RecalculationDetail> diffAmt,
            final LocalDate prepayDate, final LocalDate lastTransactionDate) {

        final ApplicationCurrency applicationCurrency = loanApplicationTerms.getApplicationCurrency();
        // 1. generate list of proposed schedule due dates
        final LocalDate loanEndDate = this.scheduledDateGenerator.getLastRepaymentDate(loanApplicationTerms, holidayDetailDTO);
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
        int instalmentNumber = 1;
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
        int daysCalcForInstallmentNumber = 0;
        LocalDate scheduleStartDateAsPerFrequency = periodStartDate;
        while (!outstandingBalance.isZero()) {
            // to insert a new schedule between actual schedule to collect
            // interest(till the transaction date) first on a payment
            boolean recalculatedInterestComponent = false;
            RecalculationDetail interestonlyPeriodDetail = null;
            if (diffAmt != null) {
                for (RecalculationDetail recalculationDetail : diffAmt) {
                    if (recalculationDetail.isInterestompound() && recalculationDetail.getStartDate().isEqual(periodStartDate)) {
                        interestonlyPeriodDetail = recalculationDetail;
                        break;
                    }
                }
            }
            LocalDate scheduledDueDate = null;
            LocalDate scheduledDueDateAsPerFrequency = null;

            int daysInPeriodApplicableInFullInstallment = 0;
            // will change the schedule dates as per the interest only Period
            if (interestonlyPeriodDetail == null) {
                actualRepaymentDate = this.scheduledDateGenerator.generateNextRepaymentDate(actualRepaymentDate, loanApplicationTerms,
                        isFirstRepayment);
                isFirstRepayment = false;
                scheduledDueDate = this.scheduledDateGenerator.adjustRepaymentDate(actualRepaymentDate, loanApplicationTerms,
                        holidayDetailDTO);
                scheduledDueDateAsPerFrequency = scheduledDueDate;
            } else {
                recalculatedInterestComponent = true;
                scheduledDueDate = interestonlyPeriodDetail.getToDate();
                daysCalcForInstallmentNumber = periodNumber;
                LocalDate actualRepayment = this.scheduledDateGenerator.generateNextRepaymentDate(actualRepaymentDate,
                        loanApplicationTerms, isFirstRepayment);
                scheduledDueDateAsPerFrequency = this.scheduledDateGenerator.adjustRepaymentDate(actualRepayment, loanApplicationTerms,
                        holidayDetailDTO);
            }

            final int daysInPeriod = Days.daysBetween(periodStartDate, scheduledDueDate).getDays();
            int daysInPeriodApplicableForInterest = daysInPeriod;

            if (periodStartDate.isBefore(idealDisbursementDate)) {
                if (loanApplicationTerms.getInterestChargedFromLocalDate() != null) {
                    periodStartDateApplicableForInterest = loanApplicationTerms.getInterestChargedFromLocalDate();
                } else {
                    periodStartDateApplicableForInterest = idealDisbursementDate;
                }
                daysInPeriodApplicableForInterest = Days.daysBetween(periodStartDateApplicableForInterest, scheduledDueDate).getDays();
            }

            // this block identifies full payment period as per the
            // schedule(excluding interest payments) for interest calculation
            daysInPeriodApplicableInFullInstallment = daysInPeriodApplicableForInterest;
            if (daysCalcForInstallmentNumber == periodNumber) {
                periodStartDateApplicableForInterest = scheduleStartDateAsPerFrequency;
                if (scheduleStartDateAsPerFrequency.isBefore(idealDisbursementDate)) {
                    if (loanApplicationTerms.getInterestChargedFromLocalDate() != null) {
                        periodStartDateApplicableForInterest = loanApplicationTerms.getInterestChargedFromLocalDate();
                    } else {
                        periodStartDateApplicableForInterest = idealDisbursementDate;
                    }
                }

                daysInPeriodApplicableInFullInstallment = Days.daysBetween(periodStartDateApplicableForInterest,
                        scheduledDueDateAsPerFrequency).getDays();
            }
            double interestCalculationGraceOnRepaymentPeriodFraction = this.paymentPeriodsInOneYearCalculator
                    .calculatePortionOfRepaymentPeriodInterestChargingGrace(periodStartDateApplicableForInterest,
                            scheduledDueDateAsPerFrequency, loanApplicationTerms.getInterestChargedFromLocalDate(),
                            loanApplicationTerms.getLoanTermPeriodFrequencyType(), loanApplicationTerms.getRepaymentEvery());
            BigDecimal interestToBeAdded = BigDecimal.ZERO;
            BigDecimal disburseAmt = BigDecimal.ZERO;
            if (loanApplicationTerms.isMultiDisburseLoan()) {
                loanApplicationTerms.setFixedEmiAmountForPeriod(scheduledDueDate);
                final Collection<DisbursementData> disbursementDatas = new ArrayList<>();
                LocalDate tillDate = scheduledDueDate;
                if (prepayDate != null && prepayDate.isBefore(tillDate)) {
                    tillDate = prepayDate;
                }
                disburseAmt = disbursementForPeriod(loanApplicationTerms, periodStartDate, tillDate, disbursementDatas, diffAmt != null);
                principalDisbursed = principalDisbursed.plus(disburseAmt);
                loanApplicationTerms.setPrincipal(loanApplicationTerms.getPrincipal().plus(disburseAmt));
                outstandingBalance = outstandingBalance.plus(disburseAmt);
                if (loanApplicationTerms.getMaxOutstandingBalance() != null
                        && outstandingBalance.isGreaterThan(loanApplicationTerms.getMaxOutstandingBalance())) {
                    String errorMsg = "Outstanding balance must not exceed the amount: " + loanApplicationTerms.getMaxOutstandingBalance();
                    throw new MultiDisbursementOutstandingAmoutException(errorMsg, loanApplicationTerms.getMaxOutstandingBalance()
                            .getAmount(), disburseAmt);
                }
                for (DisbursementData disbursementData : disbursementDatas) {
                    Money disbursedAmt = Money.of(currency, disbursementData.amount());
                    final LoanScheduleModelDisbursementPeriod disbursementPeriod = LoanScheduleModelDisbursementPeriod.disbursement(
                            disbursementData.disbursementDate(), disbursedAmt, chargesDueAtTimeOfDisbursement);
                    periods.add(disbursementPeriod);

                    if (disbursementData.disbursementDate().isAfter(periodStartDateApplicableForInterest)
                            && (prepayDate == null || prepayDate.isAfter(disbursementData.disbursementDate()) || disbursementData
                                    .isDisbursed())) {
                        interestToBeAdded = interestToBeAdded.add(calculateInterestForSpecificDays(mc, loanApplicationTerms, periodNumber,
                                totalOutstandingInterestPaymentDueToGrace, daysInPeriodApplicableInFullInstallment,
                                interestCalculationGraceOnRepaymentPeriodFraction, disbursedAmt, disbursementData.disbursementDate(),
                                tillDate));
                    }
                }
            }

            Money balanceForcalculation = outstandingBalance.minus(disburseAmt);
            // reduce principal is the early payment, will processed as per the
            // reschedule strategy
            if (reducePrincipal.isGreaterThanZero()) {
                switch (loanApplicationTerms.getRescheduleStrategyMethod()) {
                    case REDUCE_EMI_AMOUNT:
                        if (!loanApplicationTerms.isMultiDisburseLoan()) {
                            loanApplicationTerms.setFixedEmiAmount(null);
                        }
                        outstandingBalance = outstandingBalance.minus(reducePrincipal);
                        balanceForcalculation = balanceForcalculation.minus(reducePrincipal);
                        totalCumulativePrincipal = totalCumulativePrincipal.plus(reducePrincipal);
                        reducePrincipal = reducePrincipal.zero();
                    break;
                    case REDUCE_NUMBER_OF_INSTALLMENTS:
                        outstandingBalance = outstandingBalance.minus(reducePrincipal);
                        balanceForcalculation = balanceForcalculation.minus(reducePrincipal);
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
            PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                    this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction, totalCumulativePrincipal,
                    totalCumulativeInterest, totalInterestChargedForFullLoanTerm, totalOutstandingInterestPaymentDueToGrace,
                    daysInPeriodApplicableInFullInstallment, balanceForcalculation, loanApplicationTerms, periodNumber, mc);

            if (loanApplicationTerms.getFixedEmiAmount() != null
                    && loanApplicationTerms.getFixedEmiAmount().compareTo(principalInterestForThisPeriod.interest().getAmount()) != 1) {
                String errorMsg = "EMI amount must be greter than : " + principalInterestForThisPeriod.interest().getAmount();
                throw new MultiDisbursementEmiAmountException(errorMsg, principalInterestForThisPeriod.interest().getAmount(),
                        loanApplicationTerms.getFixedEmiAmount());
            }
            // update cumulative fields for principal & interest
            Money interestForThisinstallment = principalInterestForThisPeriod.interest();

            if (daysCalcForInstallmentNumber == periodNumber) {
                interestForThisinstallment = interestForThisinstallment.zero().plus(
                        calculateInterestForDays(daysInPeriodApplicableInFullInstallment, interestForThisinstallment.getAmount(),
                                daysInPeriodApplicableForInterest));
            }
            totalOutstandingInterestPaymentDueToGrace = principalInterestForThisPeriod.interestPaymentDueToGrace();

            if (interestForThisinstallment.isGreaterThanZero()) {
                interestForThisinstallment = interestForThisinstallment.plus(interestToBeAdded);
            } else if (principalInterestForThisPeriod.interestPaymentDueToGrace().isGreaterThanZero()) {
                totalOutstandingInterestPaymentDueToGrace = totalOutstandingInterestPaymentDueToGrace.minus(interestToBeAdded);
            }

            Money principalForThisPeriod = principalDisbursed.zero();
            Money outstandingForThePeriod = outstandingBalance;
            Money reducePrincipalForCurrentInstallment = reducePrincipal;

            totalCumulativeInterest = totalCumulativeInterest.plus(interestForThisinstallment);

            Money feeChargesForInstallment = principalDisbursed.zero();
            Money penaltyChargesForInstallment = principalDisbursed.zero();

            Money extraPrincipal = reducePrincipal.zero();
            Money canBeReducedFromReducePrincipal = reducePrincipal.zero();
            // this block is to identify interest based on late/early payment
            if (diffAmt != null && !diffAmt.isEmpty()) {
                BigDecimal interestDueToLatePayment = BigDecimal.ZERO;
                BigDecimal interestReducedDueToEarlyPayment = BigDecimal.ZERO;

                for (RecalculationDetail detail : diffAmt) {
                    if (!detail.isInterestompound()) {
                        // will increase the principal portion and reduces
                        // interest
                        // as per the number of days. and also identifies reduce
                        // principal for reschedule strategy
                        if (!detail.isLatePayment() && detail.getStartDate().isAfter(periodStartDate)
                                && !detail.getStartDate().isAfter(scheduledDueDate)) {
                            reducePrincipal = reducePrincipal.plus(detail.getAmount());
                            extraPrincipal = extraPrincipal.plus(detail.getAmount());
                            int diffDays = Days.daysBetween(detail.getStartDate(), scheduledDueDate).getDays();
                            if (diffDays > 0) {
                                canBeReducedFromReducePrincipal = canBeReducedFromReducePrincipal.plus(detail.getAmount());
                            }

                            LocalDate startDate = detail.getStartDate();
                            if (loanApplicationTerms.getInterestChargedFromLocalDate() != null
                                    && startDate.isBefore(loanApplicationTerms.getInterestChargedFromLocalDate())) {
                                startDate = loanApplicationTerms.getInterestChargedFromLocalDate();
                            }
                            LocalDate endDate = scheduledDueDate;
                            if (startDate.isBefore(endDate)) {
                                Money amountForInterestCalculation = detail.getAmount();
                                Money balanceDiff = outstandingForThePeriod.minus(extraPrincipal);
                                if (balanceDiff.isLessThanZero()) {
                                    amountForInterestCalculation = amountForInterestCalculation.plus(balanceDiff);
                                }
                                interestReducedDueToEarlyPayment = interestReducedDueToEarlyPayment.add(calculateInterestForSpecificDays(
                                        mc, loanApplicationTerms, periodNumber, totalOutstandingInterestPaymentDueToGrace,
                                        daysInPeriodApplicableInFullInstallment, interestCalculationGraceOnRepaymentPeriodFraction,
                                        amountForInterestCalculation, startDate, endDate));
                            }

                        }
                        // calculates the interest for late payment and increase
                        // the interest for the installment
                        else if (detail.isLatePayment() && detail.isOverlapping(periodStartDate, scheduledDueDate)
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
                            if (fromDate.isAfter(LocalDate.now())) {
                                fromDate = LocalDate.now();
                            }
                            if (loanApplicationTerms.getInterestChargedFromLocalDate() != null
                                    && fromDate.isBefore(loanApplicationTerms.getInterestChargedFromLocalDate())) {
                                fromDate = loanApplicationTerms.getInterestChargedFromLocalDate();
                            }
                            if (fromDate.isBefore(toDate)) {
                                interestDueToLatePayment = interestDueToLatePayment.add(calculateInterestForSpecificDays(mc,
                                        loanApplicationTerms, periodNumber, totalOutstandingInterestPaymentDueToGrace,
                                        daysInPeriodApplicableInFullInstallment, interestCalculationGraceOnRepaymentPeriodFraction,
                                        detail.getAmount(), fromDate, toDate));
                            }
                        }
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

            // Exclude principal portion for interest only installments
            if (!recalculatedInterestComponent) {
                principalForThisPeriod = principalInterestForThisPeriod.principal();
                if (((diffAmt != null && !diffAmt.isEmpty()) || interestToBeAdded.compareTo(BigDecimal.ZERO) == 1)
                        && loanApplicationTerms.getAmortizationMethod().isEqualInstallment()) {
                    Money principalToBeAdjust = Money.of(currency, loanApplicationTerms.getFixedEmiAmount()).minus(principalForThisPeriod)
                            .minus(interestForThisinstallment);
                    principalForThisPeriod = principalForThisPeriod.plus(principalToBeAdjust);
                    principalForThisPeriod = loanApplicationTerms.adjustPrincipalIfLastRepaymentPeriod(principalForThisPeriod,
                            totalCumulativePrincipal.plus(principalForThisPeriod), periodNumber);
                }
                totalCumulativePrincipal = totalCumulativePrincipal.plus(principalForThisPeriod);

                // 6. update outstandingLoanBlance using correct 'principalDue'
                outstandingBalance = outstandingBalance.minus(principalForThisPeriod);
                if (outstandingBalance.isLessThanZero()) {
                    principalForThisPeriod = principalForThisPeriod.plus(outstandingBalance);
                    outstandingBalance = outstandingBalance.zero();
                }
            }

            reducePrincipal = reducePrincipal.minus(reducePrincipalForCurrentInstallment);

            if (principalForThisPeriod.isGreaterThan(reducePrincipalForCurrentInstallment)) {
                principalForThisPeriod = principalForThisPeriod.minus(reducePrincipalForCurrentInstallment);
                reducePrincipalForCurrentInstallment = reducePrincipalForCurrentInstallment.zero();
            } else {
                reducePrincipalForCurrentInstallment = reducePrincipalForCurrentInstallment.minus(principalForThisPeriod);
                principalForThisPeriod = principalForThisPeriod.zero();
            }

            Money actualOutstandingbalance = outstandingBalance;
            if (diffAmt != null && !diffAmt.isEmpty()) {
                reducePrincipal = reducePrincipal.minus(canBeReducedFromReducePrincipal);
                Money pricipalAfterCurrentPrincipal = canBeReducedFromReducePrincipal.minus(principalForThisPeriod);
                if (pricipalAfterCurrentPrincipal.isGreaterThanZero()) {
                    reducePrincipal = reducePrincipal.plus(pricipalAfterCurrentPrincipal);
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
            }

            reducePrincipal = reducePrincipal.plus(reducePrincipalForCurrentInstallment);

            actualOutstandingbalance = actualOutstandingbalance.minus(reducePrincipal);
            if (actualOutstandingbalance.isLessThanZero()) {
                actualOutstandingbalance = actualOutstandingbalance.zero();
            }

            // 8. sum up real totalInstallmentDue from components
            final Money totalInstallmentDue = principalForThisPeriod//
                    .plus(interestForThisinstallment) //
                    .plus(feeChargesForInstallment) //
                    .plus(penaltyChargesForInstallment);

            // 9. create repayment period from parts
            final LoanScheduleModelPeriod installment = LoanScheduleModelRepaymentPeriod.repayment(instalmentNumber, periodStartDate,
                    scheduledDueDate, principalForThisPeriod, actualOutstandingbalance, interestForThisinstallment,
                    feeChargesForInstallment, penaltyChargesForInstallment, totalInstallmentDue, recalculatedInterestComponent);
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

            if (!recalculatedInterestComponent) {
                periodNumber++;
                scheduleStartDateAsPerFrequency = periodStartDate;
            }
            instalmentNumber++;
        }

        // 7. determine fees and penalties
        for (LoanScheduleModelPeriod loanScheduleModelPeriod : periods) {
            if (loanScheduleModelPeriod.isRepaymentPeriod()) {
                PrincipalInterest principalInterest = new PrincipalInterest(Money.of(currency, loanScheduleModelPeriod.principalDue()),
                        Money.of(currency, loanScheduleModelPeriod.interestDue()), null);
                Money feeChargesForInstallment = cumulativeFeeChargesDueWithin(loanScheduleModelPeriod.periodFromDate(),
                        loanScheduleModelPeriod.periodDueDate(), loanCharges, currency, principalInterest, principalDisbursed,
                        totalCumulativeInterest, numberOfRepayments, !loanScheduleModelPeriod.isRecalculatedInterestComponent(),
                        lastTransactionDate);
                Money penaltyChargesForInstallment = cumulativePenaltyChargesDueWithin(loanScheduleModelPeriod.periodFromDate(),
                        loanScheduleModelPeriod.periodDueDate(), loanCharges, currency, principalInterest, principalDisbursed,
                        totalCumulativeInterest, numberOfRepayments, !loanScheduleModelPeriod.isRecalculatedInterestComponent(),
                        lastTransactionDate);
                totalFeeChargesCharged = totalFeeChargesCharged.add(feeChargesForInstallment.getAmount());
                totalPenaltyChargesCharged = totalPenaltyChargesCharged.add(penaltyChargesForInstallment.getAmount());
                loanScheduleModelPeriod.addLoanCharges(feeChargesForInstallment.getAmount(), penaltyChargesForInstallment.getAmount());
            }
        }

        // this block is to add extra re-payment schedules with interest portion
        // if the last payment is missed
        if (diffAmt != null && !diffAmt.isEmpty() && !periodStartDate.isAfter(LocalDate.now())) {
            boolean recalculatedInterestComponent = true;
            Map<LocalDate, RecalculationDetail> processDetails = new TreeMap<>();
            for (RecalculationDetail detail : diffAmt) {
                if (detail.isLatePayment() && periodStartDate.isAfter(detail.getStartDate()) && detail.getToDate().isAfter(periodStartDate)) {
                    detail.updateStartDate(periodStartDate);
                }
                if (!periodStartDate.isAfter(detail.getStartDate()) && detail.isLatePayment() && !detail.isInterestompound()) {
                    if (processDetails.containsKey(detail.getToDate())) {
                        RecalculationDetail recalculationDetail = processDetails.get(detail.getToDate());
                        RecalculationDetail updatedDetail = new RecalculationDetail(recalculationDetail.isLatePayment(),
                                recalculationDetail.getStartDate(), recalculationDetail.getToDate(), recalculationDetail.getAmount().plus(
                                        detail.getAmount()), detail.isInterestompound());
                        processDetails.put(updatedDetail.getToDate(), updatedDetail);
                    } else {
                        processDetails.put(detail.getToDate(), detail);
                    }
                }
            }

            for (RecalculationDetail detail : processDetails.values()) {
                LocalDate fromDate = detail.getStartDate();
                LocalDate toDate = detail.getToDate();
                if (fromDate.isBefore(LocalDate.now())) {
                    BigDecimal interestForLatePayment = loanApplicationTerms.interestRateFor(this.paymentPeriodsInOneYearCalculator, mc,
                            detail.getAmount(), fromDate, toDate);
                    Money interestDueToLatePayment = Money.of(detail.getAmount().getCurrency(), interestForLatePayment);
                    if (interestDueToLatePayment.isGreaterThanZero()) {
                        totalInterestCharged = totalInterestCharged.add(interestDueToLatePayment.getAmount());
                        totalRepaymentExpected = totalRepaymentExpected.add(interestDueToLatePayment.getAmount());

                        final LoanScheduleModelPeriod installment = LoanScheduleModelRepaymentPeriod.repayment(instalmentNumber, fromDate,
                                toDate, interestDueToLatePayment.zero(), interestDueToLatePayment.zero(), interestDueToLatePayment,
                                interestDueToLatePayment.zero(), interestDueToLatePayment.zero(), interestDueToLatePayment,
                                recalculatedInterestComponent);
                        periods.add(installment);
                        instalmentNumber++;
                    }
                }
            }
        }

        loanApplicationTerms.resetFixedEmiAmount();

        return LoanScheduleModel.from(periods, applicationCurrency, loanTermInDays, principalDisbursed, totalPrincipalExpected,
                totalPrincipalPaid, totalInterestCharged, totalFeeChargesCharged, totalPenaltyChargesCharged, totalRepaymentExpected,
                totalOutstanding);
    }

    @Override
    public LoanRescheduleModel reschedule(final MathContext mathContext, final LoanRescheduleRequest loanRescheduleRequest,
            final ApplicationCurrency applicationCurrency, final HolidayDetailDTO holidayDetailDTO) {

        final Loan loan = loanRescheduleRequest.getLoan();
        final LoanSummary loanSummary = loan.getSummary();
        final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail = loan.getLoanRepaymentScheduleDetail();
        final MonetaryCurrency currency = loanProductRelatedDetail.getCurrency();

        // create an archive of the current loan schedule installments
        Collection<LoanRepaymentScheduleHistory> loanRepaymentScheduleHistoryList = null;

        // get the initial list of repayment installments
        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = loan.getRepaymentScheduleInstallments();

        // sort list by installment number in ASC order
        Collections.sort(repaymentScheduleInstallments, LoanRepaymentScheduleInstallment.installmentNumberComparator);

        final Collection<LoanRescheduleModelRepaymentPeriod> periods = new ArrayList<>();

        Money outstandingLoanBalance = loan.getPrincpal();

        for (LoanRepaymentScheduleInstallment repaymentScheduleInstallment : repaymentScheduleInstallments) {

            Integer oldPeriodNumber = repaymentScheduleInstallment.getInstallmentNumber();
            LocalDate fromDate = repaymentScheduleInstallment.getFromDate();
            LocalDate dueDate = repaymentScheduleInstallment.getDueDate();
            Money principalDue = repaymentScheduleInstallment.getPrincipal(currency);
            Money interestDue = repaymentScheduleInstallment.getInterestCharged(currency);
            Money feeChargesDue = repaymentScheduleInstallment.getFeeChargesCharged(currency);
            Money penaltyChargesDue = repaymentScheduleInstallment.getPenaltyChargesCharged(currency);
            Money totalDue = principalDue.plus(interestDue).plus(feeChargesDue).plus(penaltyChargesDue);

            outstandingLoanBalance = outstandingLoanBalance.minus(principalDue);

            LoanRescheduleModelRepaymentPeriod period = LoanRescheduleModelRepaymentPeriod
                    .instance(oldPeriodNumber, oldPeriodNumber, fromDate, dueDate, principalDue, outstandingLoanBalance, interestDue,
                            feeChargesDue, penaltyChargesDue, totalDue, false);

            periods.add(period);
        }

        Money outstandingBalance = loan.getPrincpal();
        Money totalCumulativePrincipal = Money.zero(currency);
        Money totalCumulativeInterest = Money.zero(currency);
        Money actualTotalCumulativeInterest = Money.zero(currency);
        Money totalOutstandingInterestPaymentDueToGrace = Money.zero(currency);
        Money totalPrincipalBeforeReschedulePeriod = Money.zero(currency);

        LocalDate installmentDueDate = null;
        LocalDate adjustedInstallmentDueDate = null;
        LocalDate installmentFromDate = null;
        Integer rescheduleFromInstallmentNo = defaultToZeroIfNull(loanRescheduleRequest.getRescheduleFromInstallment());
        Integer installmentNumber = rescheduleFromInstallmentNo;
        Integer graceOnPrincipal = defaultToZeroIfNull(loanRescheduleRequest.getGraceOnPrincipal());
        Integer graceOnInterest = defaultToZeroIfNull(loanRescheduleRequest.getGraceOnInterest());
        Integer extraTerms = defaultToZeroIfNull(loanRescheduleRequest.getExtraTerms());
        final boolean recalculateInterest = loanRescheduleRequest.getRecalculateInterest();
        Integer numberOfRepayments = repaymentScheduleInstallments.size();
        Integer rescheduleNumberOfRepayments = numberOfRepayments;
        final Money principal = loan.getPrincpal();
        final Money totalPrincipalOutstanding = Money.of(currency, loanSummary.getTotalPrincipalOutstanding());
        LocalDate adjustedDueDate = loanRescheduleRequest.getAdjustedDueDate();
        BigDecimal newInterestRate = loanRescheduleRequest.getInterestRate();
        int loanTermInDays = Integer.valueOf(0);

        if (rescheduleFromInstallmentNo > 0) {
            // this will hold the loan repayment installment that is before the
            // reschedule start installment
            // (rescheduleFrominstallment)
            LoanRepaymentScheduleInstallment previousInstallment = null;

            // get the install number of the previous installment
            int previousInstallmentNo = rescheduleFromInstallmentNo - 1;

            // only fetch the installment if the number is greater than 0
            if (previousInstallmentNo > 0) {
                previousInstallment = loan.fetchRepaymentScheduleInstallment(previousInstallmentNo);
            }

            LoanRepaymentScheduleInstallment firstInstallment = loan.fetchRepaymentScheduleInstallment(1);

            // the "installment from date" is equal to the due date of the
            // previous installment, if it exists
            if (previousInstallment != null) {
                installmentFromDate = previousInstallment.getDueDate();
            }

            else {
                installmentFromDate = firstInstallment.getFromDate();
            }

            installmentDueDate = installmentFromDate;
            LocalDate periodStartDateApplicableForInterest = installmentFromDate;
            Integer periodNumber = 1;
            outstandingLoanBalance = loan.getPrincpal();

            for (LoanRescheduleModelRepaymentPeriod period : periods) {

                if (period.periodDueDate().isBefore(loanRescheduleRequest.getRescheduleFromDate())) {

                    totalPrincipalBeforeReschedulePeriod = totalPrincipalBeforeReschedulePeriod.plus(period.principalDue());
                    actualTotalCumulativeInterest = actualTotalCumulativeInterest.plus(period.interestDue());
                    rescheduleNumberOfRepayments--;
                    outstandingLoanBalance = outstandingLoanBalance.minus(period.principalDue());
                    outstandingBalance = outstandingBalance.minus(period.principalDue());
                }
            }

            while (graceOnPrincipal > 0 || graceOnInterest > 0) {

                LoanRescheduleModelRepaymentPeriod period = LoanRescheduleModelRepaymentPeriod.instance(0, 0, new LocalDate(),
                        new LocalDate(), Money.zero(currency), Money.zero(currency), Money.zero(currency), Money.zero(currency),
                        Money.zero(currency), Money.zero(currency), true);

                periods.add(period);

                if (graceOnPrincipal > 0) {
                    graceOnPrincipal--;
                }

                if (graceOnInterest > 0) {
                    graceOnInterest--;
                }

                rescheduleNumberOfRepayments++;
                numberOfRepayments++;
            }

            while (extraTerms > 0) {

                LoanRescheduleModelRepaymentPeriod period = LoanRescheduleModelRepaymentPeriod.instance(0, 0, new LocalDate(),
                        new LocalDate(), Money.zero(currency), Money.zero(currency), Money.zero(currency), Money.zero(currency),
                        Money.zero(currency), Money.zero(currency), true);

                periods.add(period);

                extraTerms--;
                rescheduleNumberOfRepayments++;
                numberOfRepayments++;
            }

            // get the loan application terms from the Loan object
            final LoanApplicationTerms loanApplicationTerms = loan.getLoanApplicationTerms(applicationCurrency);

            // update the number of repayments
            loanApplicationTerms.updateNumberOfRepayments(numberOfRepayments);

            LocalDate loanEndDate = this.scheduledDateGenerator.getLastRepaymentDate(loanApplicationTerms, holidayDetailDTO);
            loanApplicationTerms.updateLoanEndDate(loanEndDate);

            if (newInterestRate != null) {
                loanApplicationTerms.updateAnnualNominalInterestRate(newInterestRate);
                loanApplicationTerms.updateInterestRatePerPeriod(newInterestRate);
            }

            graceOnPrincipal = defaultToZeroIfNull(loanRescheduleRequest.getGraceOnPrincipal());
            graceOnInterest = defaultToZeroIfNull(loanRescheduleRequest.getGraceOnInterest());

            loanApplicationTerms.updateInterestPaymentGrace(graceOnInterest);
            loanApplicationTerms.updatePrincipalGrace(graceOnPrincipal);

            loanApplicationTerms.setPrincipal(totalPrincipalOutstanding);
            loanApplicationTerms.updateNumberOfRepayments(rescheduleNumberOfRepayments);
            loanApplicationTerms.updateLoanTermFrequency(rescheduleNumberOfRepayments);
            loanApplicationTerms.updateInterestChargedFromDate(periodStartDateApplicableForInterest);

            Money totalInterestChargedForFullLoanTerm = loanApplicationTerms.calculateTotalInterestCharged(
                    this.paymentPeriodsInOneYearCalculator, mathContext);

            if (!recalculateInterest && newInterestRate == null) {
                totalInterestChargedForFullLoanTerm = Money.of(currency, loanSummary.getTotalInterestCharged());
                totalInterestChargedForFullLoanTerm = totalInterestChargedForFullLoanTerm.minus(actualTotalCumulativeInterest);

                loanApplicationTerms.updateTotalInterestDue(totalInterestChargedForFullLoanTerm);
            }

            for (LoanRescheduleModelRepaymentPeriod period : periods) {

                if (period.periodDueDate().isEqual(loanRescheduleRequest.getRescheduleFromDate())
                        || period.periodDueDate().isAfter(loanRescheduleRequest.getRescheduleFromDate()) || period.isNew()) {

                    installmentDueDate = this.scheduledDateGenerator.generateNextRepaymentDate(installmentDueDate, loanApplicationTerms,
                            false);

                    if (adjustedDueDate != null && periodNumber == 1) {
                        installmentDueDate = adjustedDueDate;
                    }

                    adjustedInstallmentDueDate = this.scheduledDateGenerator.adjustRepaymentDate(installmentDueDate, loanApplicationTerms,
                            holidayDetailDTO);

                    final int daysInInstallment = Days.daysBetween(installmentFromDate, adjustedInstallmentDueDate).getDays();

                    period.updatePeriodNumber(installmentNumber);
                    period.updatePeriodFromDate(installmentFromDate);
                    period.updatePeriodDueDate(adjustedInstallmentDueDate);

                    double interestCalculationGraceOnRepaymentPeriodFraction = this.paymentPeriodsInOneYearCalculator
                            .calculatePortionOfRepaymentPeriodInterestChargingGrace(periodStartDateApplicableForInterest,
                                    adjustedInstallmentDueDate, periodStartDateApplicableForInterest,
                                    loanApplicationTerms.getLoanTermPeriodFrequencyType(), loanApplicationTerms.getRepaymentEvery());

                    // ========================= Calculate the interest due
                    // ========================================

                    // change the principal to => Principal Disbursed - Total
                    // Principal Paid
                    // interest calculation is always based on the total
                    // principal outstanding
                    loanApplicationTerms.setPrincipal(totalPrincipalOutstanding);

                    // determine the interest & principal for the period
                    PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                            this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction,
                            totalCumulativePrincipal, totalCumulativeInterest, totalInterestChargedForFullLoanTerm,
                            totalOutstandingInterestPaymentDueToGrace, daysInInstallment, outstandingBalance, loanApplicationTerms,
                            periodNumber, mathContext);

                    // update the interest due for the period
                    period.updateInterestDue(principalInterestForThisPeriod.interest());

                    // =============================================================================================

                    // ========================== Calculate the principal due
                    // ======================================

                    // change the principal to => Principal Disbursed - Total
                    // cumulative Principal Amount before the reschedule
                    // installment
                    loanApplicationTerms.setPrincipal(principal.minus(totalPrincipalBeforeReschedulePeriod));

                    principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(this.paymentPeriodsInOneYearCalculator,
                            interestCalculationGraceOnRepaymentPeriodFraction, totalCumulativePrincipal, totalCumulativeInterest,
                            totalInterestChargedForFullLoanTerm, totalOutstandingInterestPaymentDueToGrace, daysInInstallment,
                            outstandingBalance, loanApplicationTerms, periodNumber, mathContext);

                    period.updatePrincipalDue(principalInterestForThisPeriod.principal());

                    // ==============================================================================================

                    outstandingLoanBalance = outstandingLoanBalance.minus(period.principalDue());
                    period.updateOutstandingLoanBalance(outstandingLoanBalance);

                    Money principalDue = Money.of(currency, period.principalDue());
                    Money interestDue = Money.of(currency, period.interestDue());

                    if (principalDue.isZero() && interestDue.isZero()) {
                        period.updateFeeChargesDue(Money.zero(currency));
                        period.updatePenaltyChargesDue(Money.zero(currency));
                    }

                    Money feeChargesDue = Money.of(currency, period.feeChargesDue());
                    Money penaltyChargesDue = Money.of(currency, period.penaltyChargesDue());

                    Money totalDue = principalDue.plus(interestDue).plus(feeChargesDue).plus(penaltyChargesDue);

                    period.updateTotalDue(totalDue);

                    // update cumulative fields for principal & interest
                    totalCumulativePrincipal = totalCumulativePrincipal.plus(period.principalDue());
                    totalCumulativeInterest = totalCumulativeInterest.plus(period.interestDue());
                    actualTotalCumulativeInterest = actualTotalCumulativeInterest.plus(period.interestDue());
                    totalOutstandingInterestPaymentDueToGrace = principalInterestForThisPeriod.interestPaymentDueToGrace();

                    installmentFromDate = adjustedInstallmentDueDate;
                    installmentNumber++;
                    periodNumber++;
                    loanTermInDays += daysInInstallment;

                    outstandingBalance = outstandingBalance.minus(period.principalDue());
                }
            }
        }

        final Money totalRepaymentExpected = principal // get the loan Principal
                                                       // amount
                .plus(actualTotalCumulativeInterest) // add the actual total
                                                     // cumulative interest
                .plus(loanSummary.getTotalFeeChargesCharged()) // add the total
                                                               // fees charged
                .plus(loanSummary.getTotalPenaltyChargesCharged()); // finally
                                                                    // add the
                                                                    // total
                                                                    // penalty
                                                                    // charged

        return LoanRescheduleModel.instance(periods, loanRepaymentScheduleHistoryList, applicationCurrency, loanTermInDays,
                loan.getPrincpal(), loan.getPrincpal().getAmount(), loanSummary.getTotalPrincipalRepaid(),
                actualTotalCumulativeInterest.getAmount(), loanSummary.getTotalFeeChargesCharged(),
                loanSummary.getTotalPenaltyChargesCharged(), totalRepaymentExpected.getAmount(), loanSummary.getTotalOutstanding());
    }

    private BigDecimal calculateInterestForSpecificDays(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            int periodNumber, Money totalOutstandingInterestPaymentDueToGrace, int daysInPeriodApplicableForInterest,
            double interestCalculationGraceOnRepaymentPeriodFraction, Money amount, LocalDate startDate, LocalDate endDate) {
        PrincipalInterest principalInterest = loanApplicationTerms.calculateTotalInterestForPeriod(this.paymentPeriodsInOneYearCalculator,
                interestCalculationGraceOnRepaymentPeriodFraction, periodNumber, mc, totalOutstandingInterestPaymentDueToGrace.zero(),
                daysInPeriodApplicableForInterest, amount);

        int days = Days.daysBetween(startDate, endDate).getDays();
        double interest = calculateInterestForDays(daysInPeriodApplicableForInterest, principalInterest.interest().getAmount(), days);

        return BigDecimal.valueOf(interest);
    }

    private double calculateInterestForDays(int daysInPeriodApplicableForInterest, BigDecimal interest, int days) {
        if (interest.doubleValue() == 0) { return 0; }
        return ((interest.doubleValue()) / daysInPeriodApplicableForInterest) * days;
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
            final Collection<DisbursementData> disbursementDatas, boolean excludePastUndisbursed) {
        BigDecimal principal = BigDecimal.ZERO;
        for (DisbursementData disbursementData : loanApplicationTerms.getDisbursementDatas()) {
            if (!excludePastUndisbursed
                    || (excludePastUndisbursed && (disbursementData.isDisbursed() || !disbursementData.disbursementDate().isBefore(
                            LocalDate.now())))) {
                if (disbursementData.isDueForDisbursement(startDate, endDate)) {
                    disbursementDatas.add(disbursementData);
                    principal = principal.add(disbursementData.amount());
                }
            }
        }
        return principal;
    }

    private BigDecimal disbursementAfterPeriod(final LoanApplicationTerms loanApplicationTerms, LocalDate endDate,
            LocalDate lastinstallmentDueDate) {
        BigDecimal principal = BigDecimal.ZERO;
        for (DisbursementData disbursementData : loanApplicationTerms.getDisbursementDatas()) {

            if (disbursementData.disbursementDate().isAfter(endDate) && lastinstallmentDueDate.isAfter(disbursementData.disbursementDate())) {
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
            final Money principalDisbursed, final Money totalInterestChargedForFullLoanTerm, int numberOfRepayments,
            boolean isInstallmentChargeApplicable, final LocalDate lastTransactionDate) {

        Money cumulative = Money.zero(monetaryCurrency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isFeeCharge()) {
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
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
                        && loanCharge.getChargeCalculation().isPercentageBased()
                        && !lastTransactionDate.isAfter(loanCharge.getDueLocalDate())) {
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
            final Money totalInterestChargedForFullLoanTerm, int numberOfRepayments, boolean isInstallmentChargeApplicable,
            final LocalDate lastTransactionDate) {

        Money cumulative = Money.zero(monetaryCurrency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
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
                        && loanCharge.getChargeCalculation().isPercentageBased()
                        && !lastTransactionDate.isAfter(loanCharge.getDueLocalDate())) {
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

    /**
     * Method calls schedule regeneration by passing transactions one after
     * another(this is done mainly to handle the scenario where interest or fee
     * of over due installment should be collected before collecting principal )
     */
    @Override
    public LoanScheduleModel rescheduleNextInstallments(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            final Set<LoanCharge> loanCharges, final HolidayDetailDTO holidayDetailDTO, final List<LoanTransaction> transactions,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor,
            final List<LoanRepaymentScheduleInstallment> previousSchedule, LocalDate recalculateFrom, final LocalDate lastTransactionDate,
            final int penaltyWaitPeriod) {

        LoanScheduleModel loanScheduleModel = null;
        List<LoanRepaymentScheduleInstallment> installments = previousSchedule;
        List<LoanRepaymentScheduleInstallment> removeInstallments = new ArrayList<>();
        MonetaryCurrency currency = loanApplicationTerms.getPrincipal().getCurrency();
        LoanTransaction preCloseTransaction = getPreclosureTransaction(transactions, installments, currency, loanApplicationTerms, mc,
                loanCharges);
        Integer updatedInstallmentNumber = 1;
        // temp fix to calculate from start need to fix properly
        recalculateFrom = loanApplicationTerms.getExpectedDisbursementDate();
        for (LoanRepaymentScheduleInstallment installment : installments) {
            if (installment.isRecalculatedInterestComponent() && recalculateFrom != null
                    && !recalculateFrom.isAfter(installment.getDueDate())) {
                removeInstallments.add(installment);
            } else {
                installment.updateInstallmentNumber(updatedInstallmentNumber++);
            }
        }
        installments.removeAll(removeInstallments);
        if (previousSchedule == null) {
            loanScheduleModel = generate(mc, loanApplicationTerms, loanCharges, holidayDetailDTO);
            installments = retrieveRepaymentSchedule(loanScheduleModel);
        }

        if (recalculateFrom == null) {
            recalculateFrom = loanApplicationTerms.getExpectedDisbursementDate();
        }

        final List<RecalculationDetail> recalculationDetails = new ArrayList<>();
        final Map<LocalDate, RecalculationDetail> retainRecalculationDetails = new HashMap<>();
        LocalDate processTransactionsForInterestCompound = loanApplicationTerms.getExpectedDisbursementDate();
        while (recalculateFrom.isAfter(processTransactionsForInterestCompound) && !transactions.isEmpty()
                && loanRepaymentScheduleTransactionProcessor.isInterestFirstRepaymentScheduleTransactionProcessor()) {

            int installmentNumber = findLastProcessedInstallmentNumber(installments, processTransactionsForInterestCompound);
            List<LoanTransaction> processTransactions = processTransactions(transactions, processTransactionsForInterestCompound);
            if (processTransactions.size() > 0) {
                loanScheduleModel = createInterestOnlyRecalculationDetails(mc, loanApplicationTerms, loanCharges, holidayDetailDTO,
                        loanRepaymentScheduleTransactionProcessor, previousSchedule, loanScheduleModel, installments, currency,
                        recalculationDetails, retainRecalculationDetails, false, installmentNumber, processTransactions,
                        preCloseTransaction, lastTransactionDate, penaltyWaitPeriod, transactions);
            }
            recalculationDetails.retainAll(retainRecalculationDetails.values());
            processTransactionsForInterestCompound = getNextRecalculateFromDate(transactions, processTransactionsForInterestCompound);
        }

        while (!recalculateFrom.isAfter(LocalDate.now())) {

            int installmentNumber = findLastProcessedInstallmentNumber(installments, recalculateFrom);
            List<LoanTransaction> processTransactions = processTransactions(transactions, recalculateFrom);
            if (loanRepaymentScheduleTransactionProcessor.isInterestFirstRepaymentScheduleTransactionProcessor()
                    && processTransactions.size() > 0) {
                loanScheduleModel = createInterestOnlyRecalculationDetails(mc, loanApplicationTerms, loanCharges, holidayDetailDTO,
                        loanRepaymentScheduleTransactionProcessor, previousSchedule, loanScheduleModel, installments, currency,
                        recalculationDetails, retainRecalculationDetails, true, installmentNumber, processTransactions,
                        preCloseTransaction, lastTransactionDate, penaltyWaitPeriod, transactions);
                if (loanScheduleModel != null) {
                    installments = retrieveRepaymentSchedule(loanScheduleModel);
                }
            }
            final List<RecalculationDetail> earlypayments = new ArrayList<>();
            for (RecalculationDetail detail : recalculationDetails) {
                if (!detail.isLatePayment()) {
                    earlypayments.add(detail);
                }
            }

            recalculationDetails.retainAll(retainRecalculationDetails.values());
            recalculationDetails.addAll(earlypayments);
            loanScheduleModel = recalculateInstallment(mc, loanApplicationTerms, loanCharges, holidayDetailDTO,
                    loanRepaymentScheduleTransactionProcessor, previousSchedule, loanScheduleModel, installments, processTransactions,
                    installmentNumber, recalculationDetails, preCloseTransaction, lastTransactionDate, penaltyWaitPeriod, transactions);
            if (loanScheduleModel != null) {
                installments = retrieveRepaymentSchedule(loanScheduleModel);
            }
            recalculateFrom = getNextRecalculateFromDate(transactions, recalculateFrom);

        }
        return loanScheduleModel;

    }

    /**
     * Method identifies interest only repayment periods
     */
    private LoanScheduleModel createInterestOnlyRecalculationDetails(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            final Set<LoanCharge> loanCharges, final HolidayDetailDTO holidayDetailDTO,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor,
            final List<LoanRepaymentScheduleInstallment> previousSchedule, LoanScheduleModel loanScheduleModel,
            List<LoanRepaymentScheduleInstallment> installments, MonetaryCurrency currency,
            final List<RecalculationDetail> recalculationDetails, final Map<LocalDate, RecalculationDetail> retainRecalculationDetails,
            boolean recalculate, int installmentNumber, List<LoanTransaction> processTransactions, LoanTransaction preCloseTransaction,
            final LocalDate lastTransactionDate, final int penaltyWaitPeriod, final List<LoanTransaction> actualTransactions) {
        LoanTransaction loanTransaction = processTransactions.remove(processTransactions.size() - 1);
        if (!(loanTransaction.isRepayment() || loanTransaction.isInterestWaiver())) {
            processTransactions.add(loanTransaction);
            return loanScheduleModel;
        }
        LocalDate processTransactionsForInterestCompound = loanTransaction.getTransactionDate();
        List<LoanRepaymentScheduleInstallment> scheduleInstallments = getInstallmentsForInterestCompound(installments,
                processTransactionsForInterestCompound, retainRecalculationDetails.keySet());

        if (!scheduleInstallments.isEmpty()) {
            for (LoanRepaymentScheduleInstallment installment : installments) {
                installment.resetDerivedComponents();
                installment.updateDerivedFields(currency, loanApplicationTerms.getExpectedDisbursementDate());
            }
            loanRepaymentScheduleTransactionProcessor.applyTransaction(processTransactions, currency, installments);

            Money principalUnprocessed = Money.zero(currency);
            Money interestUnprocessed = Money.zero(currency);
            Money feeUnprocessed = Money.zero(currency);
            boolean isBeforeFirstInstallment = false;
            if (scheduleInstallments.size() == 1) {
                LoanRepaymentScheduleInstallment installment = scheduleInstallments.get(0);
                if (installment.getInstallmentNumber() == 1
                        && (installment.getDueDate().isAfter(processTransactionsForInterestCompound) || (installment
                                .isRecalculatedInterestComponent() && installment.getDueDate().isEqual(
                                processTransactionsForInterestCompound)))) {
                    isBeforeFirstInstallment = true;
                }
            }
            if (!isBeforeFirstInstallment) {
                for (LoanRepaymentScheduleInstallment installment : scheduleInstallments) {
                    principalUnprocessed = principalUnprocessed.plus(installment.getPrincipalOutstanding(currency));
                    interestUnprocessed = interestUnprocessed.plus(installment.getInterestOutstanding(currency));
                    feeUnprocessed = feeUnprocessed.plus(installment.getFeeChargesOutstanding(currency));
                    feeUnprocessed = feeUnprocessed.plus(installment.getPenaltyChargesOutstanding(currency));
                }
            }
            if (interestUnprocessed.isLessThan(loanTransaction.getAmount(currency))) {
                LoanRepaymentScheduleInstallment lastProcessedInstallment = scheduleInstallments.get(scheduleInstallments.size() - 1);
                LocalDate startDate = lastProcessedInstallment.getDueDate();
                if (isBeforeFirstInstallment) {
                    startDate = loanApplicationTerms.getExpectedDisbursementDate();
                }
                RecalculationDetail recalculationDetail = new RecalculationDetail(false, startDate, processTransactionsForInterestCompound,
                        null, true);
                retainRecalculationDetails.put(processTransactionsForInterestCompound, recalculationDetail);
                recalculationDetails.add(recalculationDetail);
                if (recalculate) {
                    recalculationDetails.retainAll(retainRecalculationDetails.values());
                    loanScheduleModel = recalculateInstallment(mc, loanApplicationTerms, loanCharges, holidayDetailDTO,
                            loanRepaymentScheduleTransactionProcessor, previousSchedule, loanScheduleModel, installments,
                            processTransactions, installmentNumber, recalculationDetails, preCloseTransaction, lastTransactionDate,
                            penaltyWaitPeriod, actualTransactions);

                }
            }
        }
        processTransactions.add(loanTransaction);
        return loanScheduleModel;
    }

    private LocalDate getNextRecalculateFromDate(List<LoanTransaction> processTransactions, LocalDate preCalculationDate) {
        LocalDate recalculateFrom = null;
        for (LoanTransaction loanTransaction : processTransactions) {
            if (preCalculationDate.isBefore(loanTransaction.getTransactionDate())
                    && (recalculateFrom == null || recalculateFrom.isAfter(loanTransaction.getTransactionDate()))) {
                recalculateFrom = loanTransaction.getTransactionDate();
            }
        }
        if (recalculateFrom == null) {
            recalculateFrom = LocalDate.now().plusDays(1);
        }
        return recalculateFrom;
    }

    /**
     * Method calls regenerate schedule for a particular set of transactions
     * till all the installments are processed to identify early or late
     * payments and will be used in regeneration of schedule
     * 
     * @param preCloseTransaction
     *            TODO
     */
    private LoanScheduleModel recalculateInstallment(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            final Set<LoanCharge> loanCharges, final HolidayDetailDTO holidayDetailDTO,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor,
            final List<LoanRepaymentScheduleInstallment> previousSchedule, LoanScheduleModel loanScheduleModel,
            List<LoanRepaymentScheduleInstallment> installments, List<LoanTransaction> processTransactions, final int skipRecalculation,
            final List<RecalculationDetail> recalculationDetails, LoanTransaction preCloseTransaction, final LocalDate lastTransactionDate,
            final int penaltyWaitPeriod, final List<LoanTransaction> actualTransactions) {
        int processInstallmentsFrom = 0;
        Integer lastInstallmentNumber = previousSchedule.size();
        while (processInstallmentsFrom < lastInstallmentNumber) {
            RecalculatedSchedule recalculatedSchedule = recalculateInterest(mc, loanApplicationTerms, loanCharges, holidayDetailDTO,
                    processTransactions, loanRepaymentScheduleTransactionProcessor, processInstallmentsFrom, installments,
                    recalculationDetails, skipRecalculation, preCloseTransaction, lastTransactionDate, penaltyWaitPeriod,
                    actualTransactions);
            processInstallmentsFrom = recalculatedSchedule.getInstallmentNumber();
            if (recalculatedSchedule.getLoanScheduleModel() != null) {
                loanScheduleModel = recalculatedSchedule.getLoanScheduleModel();
                installments = retrieveRepaymentSchedule(loanScheduleModel);
                lastInstallmentNumber = installments.size();
            }
        }
        return loanScheduleModel;
    }

    /**
     * Method identifies late or early payments for schedule recalculation.
     * 
     * @param preCloseTransaction
     *            TODO
     */
    private RecalculatedSchedule recalculateInterest(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            final Set<LoanCharge> loanCharges, final HolidayDetailDTO holidayDetailDTO, final List<LoanTransaction> transactions,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor, final int installmentNumber,
            final List<LoanRepaymentScheduleInstallment> installments, final List<RecalculationDetail> recalculationDetails,
            final int skipRecalculation, final LoanTransaction preCloseTransaction, final LocalDate lastTransactionDate,
            final int penaltyWaitPeriod, final List<LoanTransaction> actualTransactions) {
        boolean processRecalculate = false;
        int processedInstallmentNumber = installmentNumber;

        final List<RecalculationDetail> diffAmt = new ArrayList<>();
        Money unpaidPricipal = loanApplicationTerms.getPrincipal();

        MonetaryCurrency currency = loanApplicationTerms.getPrincipal().getCurrency();
        final List<LoanRepaymentScheduleInstallment> processinstallmets = new ArrayList<>();
        for (LoanRepaymentScheduleInstallment installment : installments) {
            installment.resetDerivedComponents();
            installment.updateDerivedFields(currency, loanApplicationTerms.getExpectedDisbursementDate());
        }
        for (LoanRepaymentScheduleInstallment installment : installments) {
            processinstallmets.add(installment);
            unpaidPricipal = unpaidPricipal.minus(installment.getPrincipal(currency));
            if (installment.getInstallmentNumber() <= installmentNumber) {
                continue;
            }
            processedInstallmentNumber = installment.getInstallmentNumber();
            if (installment.getInstallmentNumber() == installments.size()) {
                processRecalculate = true;
            }

            List<LoanTransaction> transactionsForInstallment = new ArrayList<>();
            Map<LocalDate, LocalDate> recalculationDates = new HashMap<>();
            LocalDate transactionsDate = getNextRestScheduleDate(installment.getDueDate().minusDays(1), loanApplicationTerms,
                    holidayDetailDTO);
            for (LoanTransaction loanTransaction : transactions) {
                LocalDate loantransactionDate = loanTransaction.getTransactionDate();
                if (!loantransactionDate.isAfter(transactionsDate)) {
                    transactionsForInstallment.add(loanTransaction);
                    recalculationDates.put(loantransactionDate,
                            getNextRestScheduleDate(loantransactionDate.minusDays(1), loanApplicationTerms, holidayDetailDTO));
                }
            }

            /*
             * if (installment.isRecalculatedInterestComponent() &&
             * installment.getPrincipal(currency).isGreaterThanZero()) {
             * diffAmt.add(new RecalculationDetail(false,
             * installment.getDueDate(), null,
             * installment.getPrincipal(currency), false)); }
             */
            List<RecalculationDetail> earlypaymentDetail = loanRepaymentScheduleTransactionProcessor.handleRepaymentSchedule(
                    transactionsForInstallment, currency, processinstallmets, installment, recalculationDates, preCloseTransaction);

            // this block is to create early payment entries for schedule
            // generation
            for (RecalculationDetail recalculationDetail : earlypaymentDetail) {
                if (!recalculationDetail.getStartDate().isAfter(installment.getDueDate())
                        && recalculationDetail.getStartDate().isAfter(installment.getFromDate())) {
                    diffAmt.add(recalculationDetail);
                }
            }

            if (installment.getDueDate().isBefore(LocalDate.now())) {
                LocalDate startDate = installment.getDueDate();
                Money totalOutstanding = installment.getTotalOutstanding(currency);
                boolean reduceStartDate = false;

                // this block is to identify late payment based on the rest
                // calculation date
                while (totalOutstanding.isGreaterThanZero() && startDate.isBefore(LocalDate.now())) {
                    LocalDate recalculateFrom = getNextRestScheduleDate(startDate.minusDays(1), loanApplicationTerms, holidayDetailDTO);
                    LocalDate recalcualteTill = getNextRestScheduleDate(recalculateFrom, loanApplicationTerms, holidayDetailDTO);
                    if (reduceStartDate) {
                        startDate = startDate.minusDays(1);
                    }
                    applyRest(transactions, startDate, recalculateFrom, installment, loanRepaymentScheduleTransactionProcessor, currency,
                            loanApplicationTerms, holidayDetailDTO, installments, preCloseTransaction);
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
                                latepaymentoutstanding, false);
                        diffAmt.add(recalculationDetail);
                    }
                    startDate = recalculateFrom.plusDays(1);
                    reduceStartDate = true;
                }
            }

            if ((!diffAmt.isEmpty() && skipRecalculation < installment.getInstallmentNumber())
                    || skipRecalculation == installment.getInstallmentNumber()) {
                processRecalculate = true;
            }
            break;

        }
        LoanScheduleModel model = null;
        recalculationDetails.addAll(diffAmt);
        if (processRecalculate) {
            LocalDate prepayDate = null;
            if (preCloseTransaction != null) {
                prepayDate = preCloseTransaction.getTransactionDate();
            }
            model = generate(mc, loanApplicationTerms, loanCharges, holidayDetailDTO, recalculationDetails, prepayDate, lastTransactionDate);
            model = updateOverDueCharges(loanCharges, model, lastTransactionDate, currency, loanRepaymentScheduleTransactionProcessor,
                    loanApplicationTerms.getExpectedDisbursementDate(), penaltyWaitPeriod, actualTransactions);
        }
        return new RecalculatedSchedule(model, processedInstallmentNumber);
    }

    private LoanScheduleModel updateOverDueCharges(final Set<LoanCharge> loanCharges, final LoanScheduleModel model,
            LocalDate lastTransactionDate, final MonetaryCurrency currency,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor, LocalDate disbursementDate,
            final int penaltyWaitPeriod, final List<LoanTransaction> transactions) {
        Set<LoanCharge> chargesForUpdate = new HashSet<>();
        LoanScheduleModel loanScheduleModel = model;
        for (LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isActive() && loanCharge.isOverdueInstallmentCharge() && loanCharge.isPenaltyCharge()
                    && loanCharge.getChargeCalculation().isPercentageBased() && !lastTransactionDate.isAfter(loanCharge.getDueLocalDate())) {
                chargesForUpdate.add(loanCharge);
            }
        }

        if (!chargesForUpdate.isEmpty()) {
            LocalDate graceDate = LocalDate.now().minusDays(penaltyWaitPeriod);
            Collection<LoanScheduleModelPeriod> periods = model.getPeriods();
            BigDecimal totalPenaltyChargesCharged = model.getTotalPenaltyChargesCharged();
            for (LoanScheduleModelPeriod loanScheduleModelPeriod : periods) {
                if (loanScheduleModelPeriod.isRepaymentPeriod()) {
                    Money overdueChargeForInstallment = overDuePenaltyChargesDueWithin(loanScheduleModelPeriod.periodFromDate(),
                            loanScheduleModelPeriod.periodDueDate(), chargesForUpdate, currency);
                    totalPenaltyChargesCharged = totalPenaltyChargesCharged.subtract(overdueChargeForInstallment.getAmount());
                    BigDecimal feeChargesForInstallment = BigDecimal.ZERO;
                    loanScheduleModelPeriod.addLoanCharges(feeChargesForInstallment, overdueChargeForInstallment.negated().getAmount());
                }
            }

            List<LoanRepaymentScheduleInstallment> installments = retrieveRepaymentSchedule(model);
            List<LoanTransaction> loanTransactions = copyTransactions(transactions);
            loanRepaymentScheduleTransactionProcessor.populateDerivedFeildsWithoutReprocess(disbursementDate, loanTransactions, currency,
                    installments, loanCharges, lastTransactionDate);
            Map<Integer, LoanRepaymentScheduleInstallment> installmentMap = getRepaymentsAsMap(installments);

            for (LoanScheduleModelPeriod loanScheduleModelPeriod : periods) {
                if (loanScheduleModelPeriod.isRepaymentPeriod()) {
                    LoanRepaymentScheduleInstallment installment = installmentMap.get(loanScheduleModelPeriod.periodNumber());
                    Money overdueChargeForInstallment = Money.zero(currency);
                    overdueChargeForInstallment = cumulativeOverDuePenaltyChargesDueWithin(loanScheduleModelPeriod.periodFromDate(),
                            loanScheduleModelPeriod.periodDueDate(), loanCharges, currency, installment.getPrincipalOutstanding(currency)
                                    .getAmount(), installment.getInterestOutstanding(currency).getAmount(), graceDate.isAfter(installment
                                    .getDueDate()));
                    totalPenaltyChargesCharged = totalPenaltyChargesCharged.add(overdueChargeForInstallment.getAmount());
                    BigDecimal feeChargesForInstallment = BigDecimal.ZERO;
                    loanScheduleModelPeriod.addLoanCharges(feeChargesForInstallment, overdueChargeForInstallment.getAmount());
                }
            }
            loanScheduleModel = LoanScheduleModel.withOverdueChargeUpdation(periods, model, totalPenaltyChargesCharged);
        }
        return loanScheduleModel;
    }

    private Map<Integer, LoanRepaymentScheduleInstallment> getRepaymentsAsMap(List<LoanRepaymentScheduleInstallment> installments) {
        Map<Integer, LoanRepaymentScheduleInstallment> installmentMap = new HashMap<>();
        for (LoanRepaymentScheduleInstallment installment : installments) {
            installmentMap.put(installment.getInstallmentNumber(), installment);
        }
        return installmentMap;
    }

    private List<LoanTransaction> copyTransactions(List<LoanTransaction> transactions) {
        List<LoanTransaction> loanTransactions = new ArrayList<>();
        for (LoanTransaction loanTransaction : transactions) {
            if (loanTransaction.isNotReversed() && !loanTransaction.isAccrual()) {
                LoanTransaction transaction = LoanTransaction.copyTransactionProperties(loanTransaction);
                loanTransactions.add(transaction);
            }
        }
        return loanTransactions;
    }

    private Money cumulativeOverDuePenaltyChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency monetaryCurrency, final BigDecimal principalOverdue,
            final BigDecimal interestOverdue, final boolean recalculate) {

        Money cumulative = Money.zero(monetaryCurrency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    BigDecimal amount = BigDecimal.ZERO;
                    if (recalculate) {
                        switch (loanCharge.getChargeCalculation()) {
                            case PERCENT_OF_AMOUNT:
                                amount = amount.add(principalOverdue);
                            break;
                            case PERCENT_OF_AMOUNT_AND_INTEREST:
                                amount = amount.add(principalOverdue).add(interestOverdue);
                            break;
                            case PERCENT_OF_INTEREST:
                                amount = amount.add(interestOverdue);
                            break;
                            default:
                            break;
                        }
                        BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
                        cumulative = cumulative.plus(loanChargeAmt);
                    }
                }
            }
        }

        return cumulative;
    }

    private Money overDuePenaltyChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd, final Set<LoanCharge> loanCharges,
            final MonetaryCurrency monetaryCurrency) {
        Money cumulative = Money.zero(monetaryCurrency);
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.chargeAmount());
                }
            }
        }
        return cumulative;
    }

    private List<LoanRepaymentScheduleInstallment> retrieveRepaymentSchedule(LoanScheduleModel model) {
        final List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>();
        for (final LoanScheduleModelPeriod scheduledLoanInstallment : model.getPeriods()) {
            if (scheduledLoanInstallment.isRepaymentPeriod()) {
                final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(null,
                        scheduledLoanInstallment.periodNumber(), scheduledLoanInstallment.periodFromDate(),
                        scheduledLoanInstallment.periodDueDate(), scheduledLoanInstallment.principalDue(),
                        scheduledLoanInstallment.interestDue(), scheduledLoanInstallment.feeChargesDue(),
                        scheduledLoanInstallment.penaltyChargesDue(), scheduledLoanInstallment.isRecalculatedInterestComponent());
                installments.add(installment);
            }
        }
        return installments;
    }

    /**
     * Method to identify which transaction did the payment for current
     * installment
     * 
     * @param preCloseTransaction
     *            TODO
     */
    private void applyRest(List<LoanTransaction> loanTransactions, LocalDate from, LocalDate to,
            LoanRepaymentScheduleInstallment installment,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor, MonetaryCurrency currency,
            final LoanApplicationTerms loanApplicationTerms, final HolidayDetailDTO holidayDetailDTO,
            final List<LoanRepaymentScheduleInstallment> installments, LoanTransaction preCloseTransaction) {
        List<LoanTransaction> transactions = new ArrayList<>();
        Map<LocalDate, LocalDate> recalculationDates = new HashMap<>();
        for (LoanTransaction transaction : loanTransactions) {
            LocalDate loantransactionDate = transaction.getTransactionDate();
            if (loantransactionDate.isAfter(from) && !transaction.getTransactionDate().isAfter(to)) {
                transactions.add(transaction);
                recalculationDates.put(loantransactionDate,
                        getNextRestScheduleDate(loantransactionDate.minusDays(1), loanApplicationTerms, holidayDetailDTO));
            }
        }

        loanRepaymentScheduleTransactionProcessor.handleRepaymentSchedule(transactions, currency, installments, installment,
                recalculationDates, preCloseTransaction);
    }

    private LocalDate getNextRestScheduleDate(LocalDate startDate, LoanApplicationTerms loanApplicationTerms,
            final HolidayDetailDTO holidayDetailDTO) {
        LocalDate nextScheduleDate = null;
        if (loanApplicationTerms.getRecalculationFrequencyType().isSameAsRepayment()) {
            nextScheduleDate = this.scheduledDateGenerator.generateNextScheduleDateStartingFromDisburseDate(startDate,
                    loanApplicationTerms, holidayDetailDTO);
        } else {
            CalendarInstance calendarInstance = loanApplicationTerms.getRestCalendarInstance();
            nextScheduleDate = CalendarUtils.getNextScheduleDate(calendarInstance.getCalendar(), startDate);
        }

        return nextScheduleDate;
    }

    /**
     * Method returns the amount payable to close the loan account as of today.
     */
    @Override
    public LoanRepaymentScheduleInstallment calculatePrepaymentAmount(final List<LoanRepaymentScheduleInstallment> installments,
            MonetaryCurrency currency, final LocalDate onDate, LocalDate interestChargedFromLocalDate,
            LoanApplicationTerms loanApplicationTerms, MathContext mc, Set<LoanCharge> charges) {
        Money feeCharges = Money.zero(currency);
        Money penaltyCharges = Money.zero(currency);
        Money totalPrincipal = Money.zero(currency);
        Money totalInterest = Money.zero(currency);
        LocalDate calculateInterestFrom = onDate;
        LocalDate dueDate = onDate;
        LocalDate repaymentDueDate = null;
        LocalDate periodStartDate = onDate;
        boolean isInterestRepayment = false;
        Money inerestForCurrentInstallment = Money.zero(currency);
        Money principalOutstanding = Money.zero(currency);
        Integer periodNumber = 0;
        LocalDate lastDueDate = loanApplicationTerms.getExpectedDisbursementDate();
        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            if (!currentInstallment.getDueDate().isAfter(LocalDate.now())) {
                calculateInterestFrom = currentInstallment.getFromDate();
                if (currentInstallment.isRecalculatedInterestComponent()) {
                    isInterestRepayment = true;
                } else {
                    isInterestRepayment = false;
                }
            }
            if (currentInstallment.isNotFullyPaidOff()) {
                if (!currentInstallment.getDueDate().isAfter(onDate)) {
                    totalPrincipal = totalPrincipal.plus(currentInstallment.getPrincipalOutstanding(currency));
                    totalInterest = totalInterest.plus(currentInstallment.getInterestOutstanding(currency));
                    feeCharges = feeCharges.plus(currentInstallment.getFeeChargesOutstanding(currency));
                    penaltyCharges = penaltyCharges.plus(currentInstallment.getPenaltyChargesOutstanding(currency));
                } else {
                    principalOutstanding = principalOutstanding.plus(currentInstallment.getPrincipal(currency));
                    totalPrincipal = totalPrincipal.minus(currentInstallment.getPrincipalCompleted(currency));
                    totalInterest = totalInterest.minus(currentInstallment.getInterestPaid(currency)).minus(
                            currentInstallment.getInterestWaived(currency));
                    if (currentInstallment.getFromDate().isBefore(periodStartDate)) {
                        if (!isInterestRepayment) {
                            calculateInterestFrom = currentInstallment.getFromDate();
                        }
                        periodStartDate = currentInstallment.getFromDate();
                        dueDate = currentInstallment.getDueDate();
                        inerestForCurrentInstallment = currentInstallment.getInterestCharged(currency);
                        if (currentInstallment.isRecalculatedInterestComponent()) {
                            isInterestRepayment = true;
                        } else {
                            isInterestRepayment = false;
                        }
                        repaymentDueDate = null;
                        periodNumber = currentInstallment.getInstallmentNumber();
                    }
                    if (isInterestRepayment && repaymentDueDate == null && !currentInstallment.isRecalculatedInterestComponent()) {
                        repaymentDueDate = currentInstallment.getDueDate();
                    }

                }
            }
            if (!currentInstallment.isRecalculatedInterestComponent() && currentInstallment.getDueDate().isAfter(lastDueDate)) {
                lastDueDate = currentInstallment.getDueDate();
            }
        }
        if (interestChargedFromLocalDate != null && calculateInterestFrom.isBefore(interestChargedFromLocalDate)) {
            calculateInterestFrom = interestChargedFromLocalDate;
        }
        Money interest = Money.zero(currency);
        LocalDate firstRepaymentdate = this.scheduledDateGenerator.generateNextRepaymentDate(
                loanApplicationTerms.getExpectedDisbursementDate(), loanApplicationTerms, true);
        final LocalDate idealDisbursementDate = this.scheduledDateGenerator.idealDisbursementDateBasedOnFirstRepaymentDate(
                loanApplicationTerms.getLoanTermPeriodFrequencyType(), loanApplicationTerms.getRepaymentEvery(), firstRepaymentdate);
        if (calculateInterestFrom.isBefore(onDate) && !periodStartDate.isBefore(idealDisbursementDate)) {
            boolean useDailyInterest = false;
            if (repaymentDueDate == null) {
                if (isInterestRepayment) {
                    useDailyInterest = true;
                }
                repaymentDueDate = dueDate;
            }
            int daysInPeriodApplicableForInterest = Days.daysBetween(calculateInterestFrom, repaymentDueDate).getDays();
            int days = Days.daysBetween(periodStartDate, onDate).getDays();
            int actualPeriodDays = Days.daysBetween(periodStartDate, repaymentDueDate).getDays();
            BigDecimal interestForInstallment = BigDecimal.ZERO;
            if (useDailyInterest) {
                interestForInstallment = loanApplicationTerms.interestRateFor(this.paymentPeriodsInOneYearCalculator, mc,
                        principalOutstanding, calculateInterestFrom, repaymentDueDate);
                interest = interest.plus(inerestForCurrentInstallment.minus(interestForInstallment));
                interest = interest.plus(calculateInterestForDays(daysInPeriodApplicableForInterest, interestForInstallment, days));

            } else {

                LocalDate periodStartDateApplicableForInterest = calculateInterestFrom;
                if (calculateInterestFrom.isBefore(idealDisbursementDate)) {
                    if (loanApplicationTerms.getInterestChargedFromLocalDate() != null) {
                        periodStartDateApplicableForInterest = loanApplicationTerms.getInterestChargedFromLocalDate();
                    } else {
                        periodStartDateApplicableForInterest = idealDisbursementDate;
                    }
                    daysInPeriodApplicableForInterest = Days.daysBetween(periodStartDateApplicableForInterest, repaymentDueDate).getDays();
                }
                Money totalOutstandingInterestPaymentDueToGrace = Money.zero(currency);
                double interestCalculationGraceOnRepaymentPeriodFraction = this.paymentPeriodsInOneYearCalculator
                        .calculatePortionOfRepaymentPeriodInterestChargingGrace(periodStartDateApplicableForInterest, repaymentDueDate,
                                loanApplicationTerms.getInterestChargedFromLocalDate(),
                                loanApplicationTerms.getLoanTermPeriodFrequencyType(), loanApplicationTerms.getRepaymentEvery());
                Money calculateInterestOnPrincipal = principalOutstanding;
                final Collection<DisbursementData> disbursementDatas = new ArrayList<>();
                if (loanApplicationTerms.isMultiDisburseLoan()) {
                    BigDecimal reducePrincipal = disbursementAfterPeriod(loanApplicationTerms, repaymentDueDate, lastDueDate);
                    principalOutstanding = principalOutstanding.minus(reducePrincipal);
                    calculateInterestOnPrincipal = principalOutstanding;
                    BigDecimal currentPeriodDisbursal = disbursementForPeriod(loanApplicationTerms, periodStartDate, repaymentDueDate,
                            disbursementDatas, true);
                    calculateInterestOnPrincipal = calculateInterestOnPrincipal.minus(currentPeriodDisbursal);
                }

                PrincipalInterest principalInterest = loanApplicationTerms.calculateTotalInterestForPeriod(
                        this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction, periodNumber, mc,
                        totalOutstandingInterestPaymentDueToGrace.zero(), daysInPeriodApplicableForInterest, calculateInterestOnPrincipal);
                interestForInstallment = BigDecimal.valueOf(calculateInterestForDays(daysInPeriodApplicableForInterest, principalInterest
                        .interest().getAmount(), actualPeriodDays));
                BigDecimal interestToBeAdded = BigDecimal.ZERO;
                BigDecimal interestToBeReduced = BigDecimal.ZERO;
                if (loanApplicationTerms.isMultiDisburseLoan()) {
                    for (DisbursementData disbursementData : disbursementDatas) {
                        Money disbursedAmt = Money.of(currency, disbursementData.amount());
                        LocalDate calculateTill = disbursementData.disbursementDate();
                        if (!onDate.isAfter(calculateTill)) {
                            principalOutstanding = principalOutstanding.minus(disbursedAmt);
                        } else {
                            interestToBeAdded = interestToBeAdded.add(calculateInterestForSpecificDays(mc, loanApplicationTerms,
                                    periodNumber, totalOutstandingInterestPaymentDueToGrace, daysInPeriodApplicableForInterest,
                                    interestCalculationGraceOnRepaymentPeriodFraction, disbursedAmt, calculateTill, onDate));
                        }

                        if (disbursementData.disbursementDate().isAfter(periodStartDateApplicableForInterest)) {
                            interestToBeReduced = interestToBeReduced.add(calculateInterestForSpecificDays(mc, loanApplicationTerms,
                                    periodNumber, totalOutstandingInterestPaymentDueToGrace, daysInPeriodApplicableForInterest,
                                    interestCalculationGraceOnRepaymentPeriodFraction, disbursedAmt, calculateTill, repaymentDueDate));
                        }

                    }
                }
                interest = interest.plus(inerestForCurrentInstallment.minus(interestForInstallment).minus(interestToBeReduced));
                BigDecimal interestForCloseDate = BigDecimal.valueOf(calculateInterestForDays(actualPeriodDays, interestForInstallment,
                        days));
                interest = interest.plus(interestForCloseDate).plus(interestToBeAdded);
            }

        }

        totalInterest = totalInterest.plus(interest);
        totalPrincipal = totalPrincipal.plus(principalOutstanding);

        for (LoanCharge loanCharge : charges) {
            if (loanCharge.isActive() && loanCharge.isNotFullyPaid() && !loanCharge.isWaived()
                    && loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStartDate, onDate)) {
                if (loanCharge.isFeeCharge()) {
                    feeCharges = feeCharges.plus(loanCharge.amountOutstanding());
                } else {
                    penaltyCharges = penaltyCharges.plus(loanCharge.amountOutstanding());
                }
            }
        }

        return new LoanRepaymentScheduleInstallment(null, 0, onDate, onDate, totalPrincipal.getAmount(), totalInterest.getAmount(),
                feeCharges.getAmount(), penaltyCharges.getAmount(), false);
    }

    private Integer findLastProcessedInstallmentNumber(final List<LoanRepaymentScheduleInstallment> installments, LocalDate date) {
        int installmentNumber = 0;
        for (LoanRepaymentScheduleInstallment installment : installments) {
            if (!installment.getDueDate().isAfter(date) && installmentNumber < installment.getInstallmentNumber()) {
                installmentNumber = installment.getInstallmentNumber();
            }
        }

        return installmentNumber;
    }

    private List<LoanRepaymentScheduleInstallment> getInstallmentsForInterestCompound(
            final List<LoanRepaymentScheduleInstallment> installments, final LocalDate date, Collection<LocalDate> processedDates) {
        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = new ArrayList<>();
        for (LoanRepaymentScheduleInstallment installment : installments) {
            if (installment.getDueDate().isEqual(date)
                    && (!installment.isRecalculatedInterestComponent() || (processedDates.contains(date) && installment
                            .isRecalculatedInterestComponent()))) {
                repaymentScheduleInstallments.clear();
                break;
            } else if (installment.getDueDate().isBefore(date)) {
                repaymentScheduleInstallments.add(installment);
            } else {
                if (installment.getInstallmentNumber() == 1) {
                    repaymentScheduleInstallments.add(installment);
                }
                break;
            }
        }
        return repaymentScheduleInstallments;
    }

    private List<LoanTransaction> processTransactions(final List<LoanTransaction> transactions, final LocalDate tillDate) {
        List<LoanTransaction> toProcess = new ArrayList<>();
        for (LoanTransaction loanTransaction : transactions) {
            if (!loanTransaction.getTransactionDate().isAfter(tillDate)) {
                toProcess.add(loanTransaction);
            }
        }
        return toProcess;
    }

    private LoanTransaction getPreclosureTransaction(List<LoanTransaction> loanTransactions,
            List<LoanRepaymentScheduleInstallment> installments, MonetaryCurrency currency, final LoanApplicationTerms applicationTerms,
            MathContext mc, final Set<LoanCharge> loanCharges) {
        LoanTransaction precloseTransaction = null;
        Money collectedPrincipal = Money.zero(currency);
        for (LoanTransaction loanTransaction : loanTransactions) {
            if (precloseTransaction == null
                    || (!precloseTransaction.getTransactionDate().isAfter(loanTransaction.getTransactionDate()) && (loanTransaction.getId() == null || (precloseTransaction
                            .getId() != null && loanTransaction.getId().compareTo(precloseTransaction.getId()) == 1)))) {
                precloseTransaction = loanTransaction;
            }
            collectedPrincipal = collectedPrincipal.plus(loanTransaction.getPrincipalPortion());
        }
        if (precloseTransaction != null) {
            LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = calculatePrepaymentAmount(installments, currency,
                    precloseTransaction.getTransactionDate(), applicationTerms.getInterestChargedFromLocalDate(), applicationTerms, mc,
                    loanCharges);
            Money pendingPrinciapl = applicationTerms.getPrincipal().minus(collectedPrincipal)
                    .plus(precloseTransaction.getPrincipalPortion());
            if (pendingPrinciapl.isGreaterThan(loanRepaymentScheduleInstallment.getPrincipal(currency))
                    || precloseTransaction.getAmount(currency).isLessThan(loanRepaymentScheduleInstallment.getTotalOutstanding(currency))) {
                precloseTransaction = null;
            }
        }

        return precloseTransaction;

    }

    /**
     * set the value to zero if the provided value is null
     * 
     * @return integer value equal/greater than 0
     **/
    private Integer defaultToZeroIfNull(Integer value) {

        return (value != null) ? value : 0;
    }
}