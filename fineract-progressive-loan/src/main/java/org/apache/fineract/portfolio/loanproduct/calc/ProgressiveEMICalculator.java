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
package org.apache.fineract.portfolio.loanproduct.calc;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Year;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.EmiAdjustment;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.InterestPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OutstandingDetails;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.PeriodDueDetails;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.RepaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelRepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class ProgressiveEMICalculator implements EMICalculator {

    private static final BigDecimal DIVISOR_100 = new BigDecimal("100");
    private static final BigDecimal ONE_WEEK_IN_DAYS = BigDecimal.valueOf(7);

    @Override
    @NotNull
    public ProgressiveLoanInterestScheduleModel generatePeriodInterestScheduleModel(@NotNull List<LoanScheduleModelRepaymentPeriod> periods,
            @NotNull LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail,
            final Integer installmentAmountInMultiplesOf, final MathContext mc) {
        return generateInterestScheduleModel(periods, LoanScheduleModelRepaymentPeriod::periodFromDate,
                LoanScheduleModelRepaymentPeriod::periodDueDate, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);
    }

    @Override
    @NotNull
    public ProgressiveLoanInterestScheduleModel generateInstallmentInterestScheduleModel(
            @NotNull List<LoanRepaymentScheduleInstallment> installments,
            @NotNull LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail,
            final Integer installmentAmountInMultiplesOf, final MathContext mc) {
        installments = installments.stream().filter(installment -> !installment.isDownPayment() && !installment.isAdditional()).toList();
        return generateInterestScheduleModel(installments, LoanRepaymentScheduleInstallment::getFromDate,
                LoanRepaymentScheduleInstallment::getDueDate, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);
    }

    @NotNull
    private <T> ProgressiveLoanInterestScheduleModel generateInterestScheduleModel(@NotNull List<T> periods, Function<T, LocalDate> from,
            Function<T, LocalDate> to, @NotNull LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail,
            final Integer installmentAmountInMultiplesOf, final MathContext mc) {
        final Money zero = Money.zero(loanProductRelatedDetail.getCurrencyData(), mc);
        final AtomicReference<RepaymentPeriod> prev = new AtomicReference<>();
        List<RepaymentPeriod> repaymentPeriods = periods.stream().map(e -> {
            RepaymentPeriod rp = new RepaymentPeriod(prev.get(), from.apply(e), to.apply(e), zero, mc);
            prev.set(rp);
            return rp;
        }).toList();
        return new ProgressiveLoanInterestScheduleModel(repaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);
    }

    @Override
    public Optional<RepaymentPeriod> findRepaymentPeriod(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final LocalDate repaymentPeriodDueDate) {
        if (scheduleModel == null) {
            return Optional.empty();
        }
        return scheduleModel.findRepaymentPeriodByDueDate(repaymentPeriodDueDate);
    }

    /**
     * Add disbursement to Interest Period
     */
    @Override
    public void addDisbursement(final ProgressiveLoanInterestScheduleModel scheduleModel, final LocalDate disbursementDueDate,
            final Money disbursedAmount) {
        scheduleModel.changeOutstandingBalanceAndUpdateInterestPeriods(disbursementDueDate, disbursedAmount, scheduleModel.zero())
                .ifPresent((repaymentPeriod) -> calculateEMIValueAndRateFactors(
                        getEffectiveRepaymentDueDate(scheduleModel, repaymentPeriod, disbursementDueDate), scheduleModel));
    }

    private LocalDate getEffectiveRepaymentDueDate(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final RepaymentPeriod changedRepaymentPeriod, final LocalDate operationDueDate) {
        final boolean isRelatedToNextRepaymentPeriod = changedRepaymentPeriod.getDueDate().isEqual(operationDueDate);
        if (isRelatedToNextRepaymentPeriod) {
            final Optional<RepaymentPeriod> nextRepaymentPeriod = scheduleModel.repaymentPeriods().stream()
                    .filter(repaymentPeriod -> changedRepaymentPeriod.equals(repaymentPeriod.getPrevious().orElse(null))).findFirst();
            if (nextRepaymentPeriod.isPresent()) {
                return nextRepaymentPeriod.get().getDueDate();
            }
            // Currently N+1 scenario is not supported. Disbursement on Last Repayment due date affects the last
            // repayment period.
        }
        return changedRepaymentPeriod.getDueDate();
    }

    @Override
    public void changeInterestRate(final ProgressiveLoanInterestScheduleModel scheduleModel, final LocalDate newInterestSubmittedOnDate,
            final BigDecimal newInterestRate) {
        final LocalDate interestRateChangeEffectiveDate = newInterestSubmittedOnDate.minusDays(1);
        scheduleModel.addInterestRate(interestRateChangeEffectiveDate, newInterestRate);
        scheduleModel
                .changeOutstandingBalanceAndUpdateInterestPeriods(interestRateChangeEffectiveDate, scheduleModel.zero(),
                        scheduleModel.zero())
                .ifPresent(repaymentPeriod -> calculateEMIValueAndRateFactors(
                        getEffectiveRepaymentDueDate(scheduleModel, repaymentPeriod, interestRateChangeEffectiveDate), scheduleModel));
    }

    @Override
    public void addBalanceCorrection(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate balanceCorrectionDate,
            Money balanceCorrectionAmount) {
        scheduleModel.changeOutstandingBalanceAndUpdateInterestPeriods(balanceCorrectionDate, scheduleModel.zero(), balanceCorrectionAmount)
                .ifPresent(repaymentPeriod -> {
                    calculateRateFactorForRepaymentPeriod(repaymentPeriod, scheduleModel);
                    calculateOutstandingBalance(scheduleModel);
                    calculateLastUnpaidRepaymentPeriodEMI(scheduleModel);
                });
    }

    @Override
    public void payInterest(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate repaymentPeriodDueDate, LocalDate transactionDate,
            Money interestAmount) {
        findRepaymentPeriod(scheduleModel, repaymentPeriodDueDate).ifPresent(rp -> rp.addPaidInterestAmount(interestAmount));
        calculateOutstandingBalance(scheduleModel);
        calculateLastUnpaidRepaymentPeriodEMI(scheduleModel);
    }

    @Override
    public void payPrincipal(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate repaymentPeriodDueDate,
            LocalDate transactionDate, Money principalAmount) {
        if (MathUtil.isEmpty(principalAmount)) {
            return;
        }
        Optional<RepaymentPeriod> repaymentPeriod = findRepaymentPeriod(scheduleModel, repaymentPeriodDueDate);
        repaymentPeriod.ifPresent(rp -> rp.addPaidPrincipalAmount(principalAmount));
        // If it is paid late, we need to calculate with the period due date
        LocalDate balanceCorrectionDate = DateUtils.isBefore(repaymentPeriodDueDate, transactionDate) ? repaymentPeriodDueDate
                : transactionDate;
        addBalanceCorrection(scheduleModel, balanceCorrectionDate, principalAmount.negated());

        repaymentPeriod.ifPresent(rp -> {
            // If any period total paid > calculated EMI, then set EMI to total paid -> effectively it is marked as
            // fully paid
            if (rp.getTotalPaidAmount().compareTo(rp.getEmi()) > 0) {
                rp.setEmi(rp.getTotalPaidAmount());
                calculateLastUnpaidRepaymentPeriodEMI(scheduleModel);
            }
        });
    }

    @Override
    @NotNull
    public PeriodDueDetails getDueAmounts(@NotNull ProgressiveLoanInterestScheduleModel scheduleModel, @NotNull LocalDate periodDueDate,
            @NotNull LocalDate targetDate) {
        ProgressiveLoanInterestScheduleModel recalculatedScheduleModelTillDate = recalculateScheduleModelTillDate(scheduleModel,
                periodDueDate, targetDate);
        RepaymentPeriod repaymentPeriod = recalculatedScheduleModelTillDate.findRepaymentPeriodByDueDate(periodDueDate).orElseThrow();
        boolean multiplePeriodIsUnpaid = recalculatedScheduleModelTillDate.repaymentPeriods().stream().filter(rp -> !rp.isFullyPaid())
                .count() > 1L;
        if (multiplePeriodIsUnpaid && !targetDate.isAfter(repaymentPeriod.getFromDate())) {
            repaymentPeriod.setEmi(repaymentPeriod.getOriginalEmi());
        }

        return new PeriodDueDetails(repaymentPeriod.getEmi(), //
                repaymentPeriod.getDuePrincipal(), //
                repaymentPeriod.getDueInterest()); //
    }

    @Override
    @NotNull
    public Money getPeriodInterestTillDate(@NotNull ProgressiveLoanInterestScheduleModel scheduleModel, @NotNull LocalDate periodDueDate,
            @NotNull LocalDate targetDate) {
        ProgressiveLoanInterestScheduleModel recalculatedScheduleModelTillDate = recalculateScheduleModelTillDate(scheduleModel,
                periodDueDate, targetDate);
        RepaymentPeriod repaymentPeriod = recalculatedScheduleModelTillDate.findRepaymentPeriodByDueDate(periodDueDate).orElseThrow();
        return repaymentPeriod.getCalculatedDueInterest();
    }

    @Override
    public Money getOutstandingLoanBalanceOfPeriod(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate periodDueDate,
            LocalDate targetDate) {
        ProgressiveLoanInterestScheduleModel recalculatedScheduleModelTillDate = recalculateScheduleModelTillDate(scheduleModel,
                periodDueDate, targetDate);
        RepaymentPeriod repaymentPeriod = recalculatedScheduleModelTillDate.findRepaymentPeriodByDueDate(periodDueDate).orElseThrow();

        return repaymentPeriod.getOutstandingLoanBalance();
    }

    @Override
    public OutstandingDetails getOutstandingAmountsTillDate(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate targetDate) {
        MathContext mc = scheduleModel.mc();
        ProgressiveLoanInterestScheduleModel scheduleModelCopy = scheduleModel.deepCopy(mc);
        // TODO use findInterestPeriod
        scheduleModelCopy.repaymentPeriods().stream()//
                .filter(rp -> targetDate.isAfter(rp.getFromDate()) && !targetDate.isAfter(rp.getDueDate())).findFirst()//
                .flatMap(rp -> rp.getInterestPeriods().stream()//
                        .filter(ip -> targetDate.isAfter(ip.getFromDate()) && !targetDate.isAfter(ip.getDueDate())) //
                        .reduce((one, two) -> two))
                .ifPresent(ip -> ip.setDueDate(targetDate)); //

        calculateRateFactorForPeriods(scheduleModelCopy.repaymentPeriods(), scheduleModelCopy);
        scheduleModelCopy.repaymentPeriods()
                .forEach(rp -> rp.getInterestPeriods().stream().filter(ip -> targetDate.isBefore(ip.getDueDate())).forEach(ip -> {
                    ip.setRateFactor(BigDecimal.ZERO);
                    ip.setRateFactorTillPeriodDueDate(BigDecimal.ZERO);
                }));
        calculateOutstandingBalance(scheduleModelCopy);
        calculateLastUnpaidRepaymentPeriodEMI(scheduleModelCopy);

        Money totalOutstandingPrincipal = MathUtil
                .negativeToZero(scheduleModelCopy.getTotalDuePrincipal().minus(scheduleModelCopy.getTotalPaidPrincipal()));
        Money totalOutstandingInterest = MathUtil
                .negativeToZero(scheduleModelCopy.getTotalDueInterest().minus(scheduleModelCopy.getTotalPaidInterest()));
        return new OutstandingDetails(totalOutstandingPrincipal, totalOutstandingInterest);
    }

    @NotNull
    private ProgressiveLoanInterestScheduleModel recalculateScheduleModelTillDate(
            @NotNull ProgressiveLoanInterestScheduleModel scheduleModel, @NotNull LocalDate periodDueDate, @NotNull LocalDate targetDate) {
        MathContext mc = scheduleModel.mc();
        ProgressiveLoanInterestScheduleModel scheduleModelCopy = scheduleModel.deepCopy(mc);
        RepaymentPeriod repaymentPeriod = scheduleModelCopy.findRepaymentPeriodByDueDate(periodDueDate).orElseThrow();

        LocalDate adjustedTargetDate = targetDate;
        InterestPeriod interestPeriod;
        if (!targetDate.isAfter(repaymentPeriod.getFromDate())) {
            interestPeriod = repaymentPeriod.getFirstInterestPeriod();
            adjustedTargetDate = repaymentPeriod.getFromDate();
        } else if (targetDate.isAfter(repaymentPeriod.getDueDate())) {
            interestPeriod = repaymentPeriod.getLastInterestPeriod();
            adjustedTargetDate = repaymentPeriod.getDueDate();
        } else {
            // TODO use findInterestPeriod
            interestPeriod = repaymentPeriod.getInterestPeriods().stream()
                    .filter(ip -> targetDate.isAfter(ip.getFromDate()) && !targetDate.isAfter(ip.getDueDate())).findFirst().orElseThrow();
        }
        // TODO use findInterestPeriod
        scheduleModelCopy.repaymentPeriods().stream()//
                .filter(rp -> targetDate.isAfter(rp.getFromDate()) && !targetDate.isAfter(rp.getDueDate())).findFirst()//
                .flatMap(rp -> rp.getInterestPeriods().stream()//
                        .filter(ip -> targetDate.isAfter(ip.getFromDate()) && !targetDate.isAfter(ip.getDueDate())) //
                        .reduce((one, two) -> two))
                .ifPresent(ip -> ip.setDueDate(targetDate)); //
        interestPeriod.setDueDate(adjustedTargetDate);
        int index = repaymentPeriod.getInterestPeriods().indexOf(interestPeriod);
        repaymentPeriod.getInterestPeriods().subList(index + 1, repaymentPeriod.getInterestPeriods().size()).clear();
        scheduleModelCopy.repaymentPeriods().forEach(rp -> rp.getInterestPeriods().removeIf(ip -> ip.getDueDate().isAfter(targetDate)));
        calculateRateFactorForPeriods(scheduleModelCopy.repaymentPeriods(), scheduleModelCopy);
        calculateOutstandingBalance(scheduleModelCopy);
        calculateLastUnpaidRepaymentPeriodEMI(scheduleModelCopy);

        return scheduleModelCopy;
    }

    /**
     * Calculate Equal Monthly Installment value and Rate Factor -1 values for calculate Interest
     */
    void calculateEMIValueAndRateFactors(final LocalDate calculateFromRepaymentPeriodDueDate,
            final ProgressiveLoanInterestScheduleModel scheduleModel) {
        final List<RepaymentPeriod> relatedRepaymentPeriods = scheduleModel.getRelatedRepaymentPeriods(calculateFromRepaymentPeriodDueDate);
        calculateRateFactorForPeriods(relatedRepaymentPeriods, scheduleModel);
        calculateOutstandingBalance(scheduleModel);
        calculateEMIOnPeriods(relatedRepaymentPeriods, scheduleModel);
        calculateOutstandingBalance(scheduleModel);
        calculateLastUnpaidRepaymentPeriodEMI(scheduleModel);
        checkAndAdjustEmiIfNeededOnRelatedRepaymentPeriods(scheduleModel, relatedRepaymentPeriods);
    }

    private void calculateLastUnpaidRepaymentPeriodEMI(ProgressiveLoanInterestScheduleModel scheduleModel) {
        MathContext mc = scheduleModel.mc();
        Money totalDueInterest = scheduleModel.repaymentPeriods().stream().map(RepaymentPeriod::getDueInterest).reduce(scheduleModel.zero(),
                (m1, m2) -> m1.plus(m2, mc)); // 1.46
        Money totalEMI = scheduleModel.repaymentPeriods().stream().map(RepaymentPeriod::getEmi).reduce(scheduleModel.zero(),
                (m1, m2) -> m1.plus(m2, mc)); // 101.48
        Money totalDisbursedAmount = scheduleModel.repaymentPeriods().stream()
                .flatMap(rp -> rp.getInterestPeriods().stream().map(InterestPeriod::getDisbursementAmount))
                .reduce(scheduleModel.zero(), (m1, m2) -> m1.plus(m2, mc)); // 100

        Money diff = totalDisbursedAmount.plus(totalDueInterest, mc).minus(totalEMI, mc);
        Optional<RepaymentPeriod> findLastUnpaidRepaymentPeriod = scheduleModel.repaymentPeriods().stream().filter(rp -> !rp.isFullyPaid())
                .reduce((first, second) -> second);
        findLastUnpaidRepaymentPeriod.ifPresent(repaymentPeriod -> repaymentPeriod.setEmi(repaymentPeriod.getEmi().add(diff, mc)));
    }

    private void calculateOutstandingBalance(ProgressiveLoanInterestScheduleModel scheduleModel) {
        scheduleModel.repaymentPeriods().forEach(rp -> rp.getInterestPeriods().forEach(InterestPeriod::updateOutstandingLoanBalance));
    }

    private void checkAndAdjustEmiIfNeededOnRelatedRepaymentPeriods(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final List<RepaymentPeriod> relatedRepaymentPeriods) {
        MathContext mc = scheduleModel.mc();
        ProgressiveLoanInterestScheduleModel newScheduleModel = null;
        int adjustCounter = 0;
        EmiAdjustment emiAdjustment;

        do {
            emiAdjustment = getEmiAdjustment(relatedRepaymentPeriods);
            if (!emiAdjustment.shouldBeAdjusted()) {
                break;
            }
            Money adjustedEqualMonthlyInstallmentValue = applyInstallmentAmountInMultiplesOf(scheduleModel, emiAdjustment.adjustedEmi());
            if (adjustedEqualMonthlyInstallmentValue.isEqualTo(emiAdjustment.originalEmi())) {
                break;
            }
            if (newScheduleModel == null) {
                newScheduleModel = scheduleModel.deepCopy(mc);
            }
            final LocalDate relatedPeriodsFirstDueDate = relatedRepaymentPeriods.get(0).getDueDate();
            newScheduleModel.repaymentPeriods().forEach(period -> {
                if (!period.getDueDate().isBefore(relatedPeriodsFirstDueDate)
                        && !adjustedEqualMonthlyInstallmentValue.isLessThan(period.getTotalPaidAmount())) {
                    period.setEmi(adjustedEqualMonthlyInstallmentValue);
                    period.setOriginalEmi(adjustedEqualMonthlyInstallmentValue);
                }
            });
            calculateOutstandingBalance(newScheduleModel);
            calculateLastUnpaidRepaymentPeriodEMI(newScheduleModel);
            if (!getEmiAdjustment(newScheduleModel.repaymentPeriods()).hasLessEmiDifference(emiAdjustment)) {
                break;
            }

            final Iterator<RepaymentPeriod> relatedPeriodFromNewModelIterator = newScheduleModel.repaymentPeriods().stream()//
                    .filter(period -> !period.getDueDate().isBefore(relatedPeriodsFirstDueDate))//
                    .toList().iterator();//

            relatedRepaymentPeriods.forEach(relatedRepaymentPeriod -> {
                if (!relatedPeriodFromNewModelIterator.hasNext()) {
                    return;
                }
                final RepaymentPeriod newRepaymentPeriod = relatedPeriodFromNewModelIterator.next();
                relatedRepaymentPeriod.setEmi(newRepaymentPeriod.getEmi());
                relatedRepaymentPeriod.setOriginalEmi(newRepaymentPeriod.getEmi());
            });
            calculateOutstandingBalance(scheduleModel);
            adjustCounter++;
        } while (emiAdjustment.hasUncountablePeriods() && adjustCounter < 3);
    }

    /**
     * Convert Interest Percentage to fraction of 1
     *
     * @param interestRate
     *            Interest Rate in Percentage
     *
     * @return Rate Interest Rate in fraction format
     */
    BigDecimal calcNominalInterestRatePercentage(final BigDecimal interestRate, MathContext mc) {
        return MathUtil.nullToZero(interestRate).divide(DIVISOR_100, mc);
    }

    /**
     * * Calculate rate factors from ONLY repayment periods
     */
    void calculateRateFactorForPeriods(final List<RepaymentPeriod> repaymentPeriods,
            final ProgressiveLoanInterestScheduleModel scheduleModel) {
        repaymentPeriods.forEach(repaymentPeriod -> calculateRateFactorForRepaymentPeriod(repaymentPeriod, scheduleModel));
    }

    void calculateRateFactorForRepaymentPeriod(final RepaymentPeriod repaymentPeriod,
            final ProgressiveLoanInterestScheduleModel scheduleModel) {
        repaymentPeriod.getInterestPeriods().forEach(interestPeriod -> {
            interestPeriod.setRateFactor(calculateRateFactorPerPeriod(scheduleModel, repaymentPeriod, interestPeriod.getFromDate(),
                    interestPeriod.getDueDate()));
            interestPeriod.setRateFactorTillPeriodDueDate(calculateRateFactorPerPeriod(scheduleModel, repaymentPeriod,
                    interestPeriod.getFromDate(), repaymentPeriod.getDueDate()));
        });
    }

    /**
     * Calculate Rate Factor for an exact Period
     */
    BigDecimal calculateRateFactorPerPeriod(final ProgressiveLoanInterestScheduleModel scheduleModel, final RepaymentPeriod repaymentPeriod,
            final LocalDate interestPeriodFromDate, final LocalDate interestPeriodDueDate) {
        final MathContext mc = scheduleModel.mc();
        final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail = scheduleModel.loanProductRelatedDetail();
        final BigDecimal interestRate = calcNominalInterestRatePercentage(scheduleModel.getInterestRate(interestPeriodFromDate),
                scheduleModel.mc());
        final DaysInYearType daysInYearType = DaysInYearType.fromInt(loanProductRelatedDetail.getDaysInYearType());
        final DaysInMonthType daysInMonthType = DaysInMonthType.fromInt(loanProductRelatedDetail.getDaysInMonthType());
        final PeriodFrequencyType repaymentFrequency = loanProductRelatedDetail.getRepaymentPeriodFrequencyType();
        final BigDecimal repaymentEvery = BigDecimal.valueOf(loanProductRelatedDetail.getRepayEvery());

        final BigDecimal daysInMonth = BigDecimal.valueOf(daysInMonthType.getNumberOfDays(interestPeriodFromDate));
        final BigDecimal daysInYear = BigDecimal.valueOf(daysInYearType.getNumberOfDays(interestPeriodFromDate));
        final BigDecimal actualDaysInPeriod = BigDecimal
                .valueOf(DateUtils.getDifferenceInDays(interestPeriodFromDate, interestPeriodDueDate));
        final BigDecimal calculatedDaysInPeriod = BigDecimal
                .valueOf(DateUtils.getDifferenceInDays(repaymentPeriod.getFromDate(), repaymentPeriod.getDueDate()));
        final int numberOfYearsDifferenceInPeriod = interestPeriodDueDate.getYear() - interestPeriodFromDate.getYear();
        final boolean partialPeriodCalculationNeeded = daysInYearType == DaysInYearType.ACTUAL && numberOfYearsDifferenceInPeriod > 0;

        // TODO check: loanApplicationTerms.calculatePeriodsBetweenDates(startDate, endDate); // calculate period data
        // TODO review: (repayment frequency: days, weeks, years; validation day is month fix 30)
        // TODO refactor this logic to represent in interest period
        if (partialPeriodCalculationNeeded) {
            final BigDecimal cumulatedPeriodFractions = calculatePeriodFractions(interestPeriodFromDate, interestPeriodDueDate, mc);
            return rateFactorByRepaymentPartialPeriod(interestRate, repaymentEvery, cumulatedPeriodFractions, BigDecimal.ONE,
                    BigDecimal.ONE, mc);
        }

        return calculateRateFactorPerPeriodBasedOnRepaymentFrequency(interestRate, repaymentFrequency, repaymentEvery, daysInMonth,
                daysInYear, actualDaysInPeriod, calculatedDaysInPeriod, mc);
    }

    /**
     * Calculate Period fractions part based on how much year has in the period
     *
     * @param interestPeriodFromDate
     * @param interestPeriodDueDate
     * @return
     */
    BigDecimal calculatePeriodFractions(final LocalDate interestPeriodFromDate, final LocalDate interestPeriodDueDate, MathContext mc) {
        BigDecimal cumulatedRateFactor = BigDecimal.ZERO;
        int actualYear = interestPeriodFromDate.getYear();
        int endYear = interestPeriodDueDate.getYear();
        LocalDate actualDate = interestPeriodFromDate;
        LocalDate endOfActualYear;

        while (actualYear <= endYear) {
            endOfActualYear = actualYear == endYear ? interestPeriodDueDate : LocalDate.of(actualYear, 12, 31);
            BigDecimal numberOfDaysInYear = BigDecimal.valueOf(Year.of(actualYear).length());
            BigDecimal calculatedDaysInActualYear = BigDecimal.valueOf(DateUtils.getDifferenceInDays(actualDate, endOfActualYear));
            cumulatedRateFactor = cumulatedRateFactor.add(calculatedDaysInActualYear.divide(numberOfDaysInYear, mc), mc);
            actualDate = endOfActualYear;
            actualYear++;
        }
        return cumulatedRateFactor;
    }

    /**
     * Calculate Rate Factor based on Repayment Frequency Type
     *
     * @param interestRate
     * @param repaymentFrequency
     * @param repaymentEvery
     * @param daysInMonth
     * @param daysInYear
     * @param actualDaysInPeriod
     * @param calculatedDaysInPeriod
     * @return
     */
    BigDecimal calculateRateFactorPerPeriodBasedOnRepaymentFrequency(final BigDecimal interestRate,
            final PeriodFrequencyType repaymentFrequency, final BigDecimal repaymentEvery, final BigDecimal daysInMonth,
            final BigDecimal daysInYear, final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod,
            final MathContext mc) {
        return switch (repaymentFrequency) {
            case DAYS ->
                rateFactorByRepaymentEveryDay(interestRate, repaymentEvery, daysInYear, actualDaysInPeriod, calculatedDaysInPeriod, mc);
            case WEEKS ->
                rateFactorByRepaymentEveryWeek(interestRate, repaymentEvery, daysInYear, actualDaysInPeriod, calculatedDaysInPeriod, mc);
            case MONTHS -> rateFactorByRepaymentEveryMonth(interestRate, repaymentEvery, daysInMonth, daysInYear, actualDaysInPeriod,
                    calculatedDaysInPeriod, mc);
            default -> throw new UnsupportedOperationException("Invalid repayment frequency"); // not supported yet
        };
    }

    void calculateEMIOnPeriods(final List<RepaymentPeriod> repaymentPeriods, final ProgressiveLoanInterestScheduleModel scheduleModel) {
        if (repaymentPeriods.isEmpty()) {
            return;
        }
        final MathContext mc = scheduleModel.mc();
        final BigDecimal rateFactorN = MathUtil.stripTrailingZeros(calculateRateFactorPlus1N(repaymentPeriods, mc));
        final BigDecimal fnResult = MathUtil.stripTrailingZeros(calculateFnResult(repaymentPeriods, mc));
        final RepaymentPeriod startPeriod = repaymentPeriods.get(0);
        // TODO: double check
        final Money outstandingBalance = startPeriod.getInitialBalanceForEmiRecalculation();

        final Money equalMonthlyInstallment = Money.of(outstandingBalance.getCurrencyData(),
                calculateEMIValue(rateFactorN, outstandingBalance.getAmount(), fnResult, mc), mc);
        final Money finalEqualMonthlyInstallment = applyInstallmentAmountInMultiplesOf(scheduleModel, equalMonthlyInstallment);

        repaymentPeriods.forEach(period -> {
            if (!finalEqualMonthlyInstallment.isLessThan(period.getTotalPaidAmount())) {
                period.setEmi(finalEqualMonthlyInstallment);
                period.setOriginalEmi(finalEqualMonthlyInstallment);
            }
        });
    }

    Money applyInstallmentAmountInMultiplesOf(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final Money equalMonthlyInstallment) {
        return scheduleModel.installmentAmountInMultiplesOf() != null
                ? Money.roundToMultiplesOf(equalMonthlyInstallment, scheduleModel.installmentAmountInMultiplesOf())
                : equalMonthlyInstallment;
    }

    public EmiAdjustment getEmiAdjustment(final List<RepaymentPeriod> repaymentPeriods) {
        for (int idx = repaymentPeriods.size() - 1; idx > 0; --idx) {
            RepaymentPeriod lastPeriod = repaymentPeriods.get(idx);
            RepaymentPeriod penultimatePeriod = repaymentPeriods.get(idx - 1);
            if (!lastPeriod.isFullyPaid() && !penultimatePeriod.isFullyPaid()) {
                Money emiDifference = lastPeriod.getEmi().minus(penultimatePeriod.getEmi());
                return new EmiAdjustment(penultimatePeriod.getEmi(), emiDifference, repaymentPeriods,
                        getUncountablePeriods(repaymentPeriods, penultimatePeriod.getEmi()));
            }
        }
        return new EmiAdjustment(repaymentPeriods.get(0).getEmi(), repaymentPeriods.get(0).getEmi().copy(0.0), repaymentPeriods, 0);
    }

    /**
     * Calculate Rate Factor Product from rate factors
     */
    BigDecimal calculateRateFactorPlus1N(final List<RepaymentPeriod> periods, MathContext mc) {
        return periods.stream().map(RepaymentPeriod::getRateFactorPlus1).reduce(BigDecimal.ONE,
                (BigDecimal acc, BigDecimal value) -> acc.multiply(value, mc));
    }

    /**
     * Summarize Fn values
     */
    BigDecimal calculateFnResult(final List<RepaymentPeriod> periods, final MathContext mc) {
        return periods.stream()//
                .skip(1)//
                .map(RepaymentPeriod::getRateFactorPlus1)//
                .reduce(BigDecimal.ONE, (previousFnValue, currentRateFactor) -> fnValue(previousFnValue, currentRateFactor, mc));//
    }

    /**
     * Calculate the EMI (Equal Monthly Installment) value
     */
    BigDecimal calculateEMIValue(final BigDecimal rateFactorPlus1N, final BigDecimal outstandingBalanceForRest, final BigDecimal fnResult,
            MathContext mc) {
        return rateFactorPlus1N.multiply(outstandingBalanceForRest, mc).divide(fnResult, mc);
    }

    /**
     * To calculate the daily payment, we first need to calculate something called the Rate Factor. We're going to be
     * using simple interest. The Rate Factor for simple interest is calculated by the following formula:
     *
     * Rate factor = 1 + (rate of interest * (repaid every / days in year) * actual days in period / calculated days in
     * period ) Where
     *
     * @param interestRate
     *            Rate of Interest
     *
     * @param repaymentEvery
     *            Repaid Every
     *
     * @param daysInYear
     *            Days is Year based on DaysInYear enum
     *
     * @param actualDaysInPeriod
     *            Always the actual number of days in the actual period
     *
     * @param calculatedDaysInPeriod
     *            Calculated days in Period (It has importance related to Reschedule)
     *
     * @return Rate Factor for period
     */
    BigDecimal rateFactorByRepaymentEveryDay(final BigDecimal interestRate, final BigDecimal repaymentEvery, final BigDecimal daysInYear,
            final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod, MathContext mc) {
        return rateFactorByRepaymentPeriod(interestRate, BigDecimal.ONE, repaymentEvery, daysInYear, actualDaysInPeriod,
                calculatedDaysInPeriod, mc);
    }

    /**
     * To calculate the weekly payment, we first need to calculate something called the Rate Factor. We're going to be
     * using simple interest. The Rate Factor for simple interest is calculated by the following formula:
     *
     * Rate factor = 1 + (rate of interest * (7 * repaid every / days in year) * actual days in period / calculated days
     * in period ) Where
     *
     * @param interestRate
     *            Rate of Interest
     *
     * @param repaymentEvery
     *            Repaid Every
     *
     * @param daysInYear
     *            Days is Year based on DaysInYear enum
     *
     * @param actualDaysInPeriod
     *            Always the actual number of days in the actual period
     *
     * @param calculatedDaysInPeriod
     *            Calculated days in Period (It has importance related to Reschedule)
     *
     * @return Rate Factor for period
     */
    BigDecimal rateFactorByRepaymentEveryWeek(final BigDecimal interestRate, final BigDecimal repaymentEvery, final BigDecimal daysInYear,
            final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod, MathContext mc) {
        return rateFactorByRepaymentPeriod(interestRate, ONE_WEEK_IN_DAYS, repaymentEvery, daysInYear, actualDaysInPeriod,
                calculatedDaysInPeriod, mc);
    }

    /**
     * To calculate the monthly payment, we first need to calculate something called the Rate Factor. We're going to be
     * using simple interest. The Rate Factor for simple interest is calculated by the following formula:
     *
     * Rate factor = 1 + (rate of interest * (days in month * repaid every / days in year) * actual days in period /
     * calculated days in period ) Where
     *
     * @param interestRate
     *            Rate of Interest
     *
     * @param repaymentEvery
     *            Repaid Every
     *
     * @param daysInMonth
     *            Days in Month based on DaysInMonth enum
     *
     * @param daysInYear
     *            Days is Year based on DaysInYear enum
     *
     * @param actualDaysInPeriod
     *            Always the actual number of days in the actual period
     *
     * @param calculatedDaysInPeriod
     *            Calculated days in Period (It has importance related to Reschedule)
     *
     * @return Rate Factor for period
     */
    BigDecimal rateFactorByRepaymentEveryMonth(final BigDecimal interestRate, final BigDecimal repaymentEvery, final BigDecimal daysInMonth,
            final BigDecimal daysInYear, final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod,
            final MathContext mc) {
        return rateFactorByRepaymentPeriod(interestRate, daysInMonth, repaymentEvery, daysInYear, actualDaysInPeriod,
                calculatedDaysInPeriod, mc);
    }

    /**
     * To calculate installment period payment. We're going to be using simple interest. The Rate Factor for simple
     * interest is calculated by the following formula:
     *
     * Rate factor = 1 + (rate of interest * ( repayment period multiplier in days * repaid every * days in month / days
     * in year) * actual days in period / calculated days in period ) Where
     *
     * @param interestRate
     *            Rate of Interest
     *
     * @param repaymentPeriodMultiplierInDays
     *            Multiplier number in days of the repayment every parameter
     *
     * @param repaymentEvery
     *            Repaid Every
     *
     * @param daysInYear
     *            Days is Year based on DaysInYear enum
     *
     * @param actualDaysInPeriod
     *            Always the actual number of days in the actual period
     *
     * @param calculatedDaysInPeriod
     *            Calculated days in Period (It has importance related to Reschedule)
     *
     * @return Rate Factor for period
     */
    BigDecimal rateFactorByRepaymentPeriod(final BigDecimal interestRate, final BigDecimal repaymentPeriodMultiplierInDays,
            final BigDecimal repaymentEvery, final BigDecimal daysInYear, final BigDecimal actualDaysInPeriod,
            final BigDecimal calculatedDaysInPeriod, final MathContext mc) {
        if (MathUtil.isZero(calculatedDaysInPeriod)) {
            return BigDecimal.ZERO;
        }
        final BigDecimal interestFractionPerPeriod = repaymentPeriodMultiplierInDays//
                .multiply(repaymentEvery, mc)//
                .divide(daysInYear, mc);//
        return interestRate//
                .multiply(interestFractionPerPeriod, mc)//
                .multiply(actualDaysInPeriod, mc)//
                .divide(calculatedDaysInPeriod, mc).setScale(mc.getPrecision(), mc.getRoundingMode());//
    }

    /**
     * Calculate Rate Factor based on Partial Period
     *
     */
    BigDecimal rateFactorByRepaymentPartialPeriod(final BigDecimal interestRate, final BigDecimal repaymentEvery,
            final BigDecimal cumulatedPeriodRatio, final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod,
            final MathContext mc) {
        if (MathUtil.isZero(calculatedDaysInPeriod)) {
            return BigDecimal.ZERO;
        }
        final BigDecimal interestFractionPerPeriod = repaymentEvery.multiply(cumulatedPeriodRatio);
        return interestRate//
                .multiply(interestFractionPerPeriod, mc)//
                .multiply(actualDaysInPeriod, mc)//
                .divide(calculatedDaysInPeriod, mc).setScale(mc.getPrecision(), mc.getRoundingMode());//
    }

    /**
     * To calculate the function value for each period, we are going to use the next formula:
     *
     * fn = 1 + fnValueFrom * rateFactorEnd
     *
     * @param previousFnValue
     *
     * @param currentRateFactor
     *
     */
    BigDecimal fnValue(final BigDecimal previousFnValue, final BigDecimal currentRateFactor, final MathContext mc) {
        return BigDecimal.ONE.add(previousFnValue.multiply(currentRateFactor, mc), mc);
    }

    /**
     * Calculates the sum of due interests on interest periods.
     *
     * @param scheduleModel
     *            schedule model
     * @param subjectDate
     *            the date to calculate the interest for.
     * @return sum of due interests
     */
    @Override
    public Money getSumOfDueInterestsOnDate(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate subjectDate) {
        return scheduleModel.repaymentPeriods().stream().map(RepaymentPeriod::getDueDate) //
                .map(repaymentPeriodDueDate -> getDueAmounts(scheduleModel, repaymentPeriodDueDate, subjectDate) //
                        .getDueInterest()) //
                .reduce(scheduleModel.zero(), Money::add); //
    }

    private long getUncountablePeriods(final List<RepaymentPeriod> relatedRepaymentPeriods, final Money originalEmi) {
        return relatedRepaymentPeriods.stream() //
                .filter(repaymentPeriod -> originalEmi.isLessThan(repaymentPeriod.getTotalPaidAmount())) //
                .count(); //
    }
}
