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

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearCustomStrategyType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelRepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.calc.data.EmiAdjustment;
import org.apache.fineract.portfolio.loanproduct.calc.data.EmiChangeOperation;
import org.apache.fineract.portfolio.loanproduct.calc.data.InterestPeriod;
import org.apache.fineract.portfolio.loanproduct.calc.data.OutstandingDetails;
import org.apache.fineract.portfolio.loanproduct.calc.data.PeriodDueDetails;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanproduct.calc.data.RepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@RequiredArgsConstructor
public final class ProgressiveEMICalculator implements EMICalculator {

    private static final BigDecimal DIVISOR_100 = new BigDecimal("100");
    private static final BigDecimal ONE_WEEK_IN_DAYS = BigDecimal.valueOf(7);

    @Override
    @NotNull
    public ProgressiveLoanInterestScheduleModel generatePeriodInterestScheduleModel(@NotNull List<LoanScheduleModelRepaymentPeriod> periods,
            @NotNull LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail,
            List<LoanTermVariationsData> loanTermVariations, final Integer installmentAmountInMultiplesOf, final MathContext mc) {
        return generateInterestScheduleModel(periods, LoanScheduleModelRepaymentPeriod::periodFromDate,
                LoanScheduleModelRepaymentPeriod::periodDueDate, loanProductRelatedDetail, loanTermVariations,
                installmentAmountInMultiplesOf, mc);
    }

    @Override
    @NotNull
    public ProgressiveLoanInterestScheduleModel generateInstallmentInterestScheduleModel(
            @NotNull List<LoanRepaymentScheduleInstallment> installments,
            @NotNull LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail,
            List<LoanTermVariationsData> loanTermVariations, final Integer installmentAmountInMultiplesOf, final MathContext mc) {
        installments = installments.stream().filter(installment -> !installment.isDownPayment() && !installment.isAdditional()).toList();
        return generateInterestScheduleModel(installments, LoanRepaymentScheduleInstallment::getFromDate,
                LoanRepaymentScheduleInstallment::getDueDate, loanProductRelatedDetail, loanTermVariations, installmentAmountInMultiplesOf,
                mc);
    }

    @NotNull
    private <T> ProgressiveLoanInterestScheduleModel generateInterestScheduleModel(@NotNull List<T> periods, Function<T, LocalDate> from,
            Function<T, LocalDate> to, @NotNull LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail,
            List<LoanTermVariationsData> loanTermVariations, final Integer installmentAmountInMultiplesOf, final MathContext mc) {
        final Money zero = Money.zero(loanProductRelatedDetail.getCurrencyData(), mc);
        final AtomicReference<RepaymentPeriod> prev = new AtomicReference<>();
        List<RepaymentPeriod> repaymentPeriods = periods.stream().map(e -> {
            RepaymentPeriod rp = RepaymentPeriod.create(prev.get(), from.apply(e), to.apply(e), zero, mc, loanProductRelatedDetail);
            prev.set(rp);
            return rp;
        }).toList();
        return new ProgressiveLoanInterestScheduleModel(repaymentPeriods, loanProductRelatedDetail, loanTermVariations,
                installmentAmountInMultiplesOf, mc);
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
        LocalDate effectiveDueDate = scheduleModel.loanProductRelatedDetail().getInterestCalculationPeriodMethod() != null
                && scheduleModel.loanProductRelatedDetail().getInterestCalculationPeriodMethod().isSameAsRepaymentPeriod()
                && !scheduleModel.loanProductRelatedDetail().isAllowPartialPeriodInterestCalculation()
                        ? scheduleModel.repaymentPeriods().stream().filter(rp -> rp.getDueDate().isAfter(disbursementDueDate)).findFirst()
                                .map(RepaymentPeriod::getFromDate).orElse(disbursementDueDate)
                        : disbursementDueDate;
        addDisbursement(scheduleModel, EmiChangeOperation.disburse(effectiveDueDate, disbursedAmount));
    }

    private void addDisbursement(final ProgressiveLoanInterestScheduleModel scheduleModel, final EmiChangeOperation operation) {
        scheduleModel.repaymentPeriods().stream().filter(rp -> !operation.getSubmittedOnDate().isAfter(rp.getFromDate())).forEach(
                rp -> rp.setTotalDisbursedAmount(MathUtil.nullToZero(rp.getTotalDisbursedAmount()).add(operation.getAmount().getAmount())));

        scheduleModel
                .changeOutstandingBalanceAndUpdateInterestPeriods(operation.getSubmittedOnDate(), operation.getAmount(),
                        scheduleModel.zero(), scheduleModel.zero())
                .ifPresent((repaymentPeriod) -> calculateEMIValueAndRateFactors(
                        getEffectiveRepaymentDueDate(scheduleModel, repaymentPeriod, operation.getSubmittedOnDate()), scheduleModel,
                        operation));
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

    /**
     * Add capitalized income to Interest Period
     */
    @Override
    public void addCapitalizedIncome(final ProgressiveLoanInterestScheduleModel scheduleModel, final LocalDate transactionDueDate,
            final Money transactionAmount) {
        addCapitalizedIncome(scheduleModel, EmiChangeOperation.capitalizedIncome(transactionDueDate, transactionAmount));
    }

    private void addCapitalizedIncome(final ProgressiveLoanInterestScheduleModel scheduleModel, final EmiChangeOperation operation) {
        scheduleModel.repaymentPeriods().stream().filter(rp -> !operation.getSubmittedOnDate().isAfter(rp.getFromDate()))
                .forEach(rp -> rp.setTotalCapitalizedIncomeAmount(
                        MathUtil.nullToZero(rp.getTotalCapitalizedIncomeAmount()).add(operation.getAmount().getAmount())));

        scheduleModel.changeOutstandingBalanceAndUpdateInterestPeriods(operation.getSubmittedOnDate(), scheduleModel.zero(),
                scheduleModel.zero(), operation.getAmount()).ifPresent((repaymentPeriod) -> {
                    calculateEMIValueAndRateFactors(
                            getEffectiveRepaymentDueDate(scheduleModel, repaymentPeriod, operation.getSubmittedOnDate()), scheduleModel,
                            operation);
                });
    }

    @Override
    public void changeInterestRate(final ProgressiveLoanInterestScheduleModel scheduleModel, final LocalDate newInterestSubmittedOnDate,
            final BigDecimal newInterestRate) {
        changeInterestRate(scheduleModel, EmiChangeOperation.changeInterestRate(newInterestSubmittedOnDate, newInterestRate));
    }

    private void changeInterestRate(final ProgressiveLoanInterestScheduleModel scheduleModel, final EmiChangeOperation operation) {
        final LocalDate interestRateChangeEffectiveDate = operation.getSubmittedOnDate().minusDays(1);
        scheduleModel.addInterestRate(interestRateChangeEffectiveDate, operation.getInterestRate());
        scheduleModel
                .changeOutstandingBalanceAndUpdateInterestPeriods(interestRateChangeEffectiveDate, scheduleModel.zero(),
                        scheduleModel.zero(), scheduleModel.zero())
                .ifPresent(repaymentPeriod -> calculateEMIValueAndRateFactors(
                        getEffectiveRepaymentDueDate(scheduleModel, repaymentPeriod, interestRateChangeEffectiveDate), scheduleModel,
                        operation));
    }

    @Override
    public void addBalanceCorrection(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate balanceCorrectionDate,
            Money balanceCorrectionAmount) {
        scheduleModel.changeOutstandingBalanceAndUpdateInterestPeriods(balanceCorrectionDate, scheduleModel.zero(), balanceCorrectionAmount,
                scheduleModel.zero()).ifPresent(repaymentPeriod -> {
                    calculateRateFactorForRepaymentPeriod(repaymentPeriod, scheduleModel);
                    calculateOutstandingBalance(scheduleModel);
                    calculateLastUnpaidRepaymentPeriodEMI(scheduleModel, balanceCorrectionDate);
                });
    }

    @Override
    public void payInterest(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate repaymentPeriodDueDate, LocalDate transactionDate,
            Money interestAmount) {
        Optional<RepaymentPeriod> repaymentPeriod = findRepaymentPeriod(scheduleModel, repaymentPeriodDueDate);

        Optional<RepaymentPeriod> latestNotLastOpenRepaymentPeriodBeforeDate = getLatestNotLastOpenRepaymentPeriodBeforeDate(scheduleModel,
                transactionDate);
        if (latestNotLastOpenRepaymentPeriodBeforeDate.isPresent() && repaymentPeriod.equals(latestNotLastOpenRepaymentPeriodBeforeDate)) {
            calculateUnrecognizedInterestTillDateOnScheduleModelCopyAndDefer(scheduleModel,
                    latestNotLastOpenRepaymentPeriodBeforeDate.get(), transactionDate);
        }

        repaymentPeriod.ifPresent(rp -> rp.addPaidInterestAmount(interestAmount));
        calculateOutstandingBalance(scheduleModel);
        calculateLastUnpaidRepaymentPeriodEMI(scheduleModel, transactionDate);
    }

    @Override
    public void payPrincipal(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate repaymentPeriodDueDate,
            LocalDate transactionDate, Money principalAmount) {
        if (MathUtil.isEmpty(principalAmount)) {
            return;
        }
        Optional<RepaymentPeriod> repaymentPeriod = findRepaymentPeriod(scheduleModel, repaymentPeriodDueDate);
        boolean transactionDateIsBefore = transactionDate.isBefore(repaymentPeriod.get().getFromDate());
        repaymentPeriod.ifPresent(rp -> rp.addPaidPrincipalAmount(principalAmount));
        // If it is paid late, we need to calculate with the period due date
        LocalDate balanceCorrectionDate = DateUtils.isBefore(repaymentPeriodDueDate, transactionDate) ? repaymentPeriodDueDate
                : transactionDate;
        addBalanceCorrection(scheduleModel, balanceCorrectionDate, principalAmount.negated());
        if (scheduleModel.isEMIRecalculationEnabled()) {
            repaymentPeriod.ifPresent(rp -> {
                // If any period total paid > calculated EMI, then set EMI to total paid -> effectively it is marked as
                // fully paid
                if (transactionDateIsBefore
                        && rp.getTotalPaidAmount().isGreaterThan(rp.getEmiPlusCreditedAmountsPlusFutureUnrecognizedInterest())) {
                    rp.setEmi(rp.getTotalPaidAmount().minus(rp.getTotalCreditedAmount()));
                } else if (transactionDateIsBefore
                        && rp.getTotalPaidAmount().isEqualTo(rp.getOriginalEmi().add(rp.getTotalCreditedAmount()))) {
                    rp.setEmi(rp.getTotalPaidAmount().minus(rp.getTotalCreditedAmount()));
                }
                calculateLastUnpaidRepaymentPeriodEMI(scheduleModel, balanceCorrectionDate);
            });
        }
    }

    private Optional<RepaymentPeriod> getLatestNotLastOpenRepaymentPeriodBeforeDate(ProgressiveLoanInterestScheduleModel scheduleModel,
            LocalDate transactionDate) {
        List<RepaymentPeriod> unpaidRepaymentPeriods = scheduleModel.repaymentPeriods() //
                .stream() //
                .filter(rp -> !rp.isFullyPaid()) //
                .toList(); //

        if (CollectionUtils.isEmpty(unpaidRepaymentPeriods)
                || unpaidRepaymentPeriods.getLast().equals(scheduleModel.repaymentPeriods().getLast())) {
            return Optional.empty();
        }

        RepaymentPeriod latestNotLastOpenRepaymentPeriod = unpaidRepaymentPeriods.getLast();
        if (DateUtils.isBefore(transactionDate, latestNotLastOpenRepaymentPeriod.getDueDate())) {
            return Optional.empty();
        }

        return Optional.of(latestNotLastOpenRepaymentPeriod);
    }

    private void addCreditedAmountsToInterestPeriod(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate transactionDate,
            Money creditedPrincipalAmount, Money creditedInterestAmount) {
        scheduleModel.repaymentPeriods().stream().filter(checkRepaymentPeriodIsInCreditRange(scheduleModel, transactionDate)).findFirst()
                .flatMap(repaymentPeriod -> repaymentPeriod.getInterestPeriods().stream()
                        .filter(interestPeriod -> interestPeriod.getFromDate().equals(transactionDate)).reduce((v1, v2) -> v2))
                .ifPresent(interestPeriod -> {
                    interestPeriod.addCreditedPrincipalAmount(creditedPrincipalAmount);
                    interestPeriod.addCreditedInterestAmount(creditedInterestAmount);
                });
    }

    @Nonnull
    private static Predicate<RepaymentPeriod> checkRepaymentPeriodIsInCreditRange(ProgressiveLoanInterestScheduleModel scheduleModel,
            LocalDate transactionDate) {
        return repaymentPeriod -> scheduleModel.isLastRepaymentPeriod(repaymentPeriod)
                ? !transactionDate.isBefore(repaymentPeriod.getFromDate()) && !transactionDate.isAfter(repaymentPeriod.getDueDate())
                : !transactionDate.isBefore(repaymentPeriod.getFromDate()) && transactionDate.isBefore(repaymentPeriod.getDueDate());
    }

    @Override
    public void creditPrincipal(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate transactionDate,
            Money creditedPrincipalAmount) {
        addCredit(scheduleModel, transactionDate, creditedPrincipalAmount, scheduleModel.zero());
    }

    @Override
    public void creditInterest(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate transactionDate,
            Money creditedInterestAmount) {
        addCredit(scheduleModel, transactionDate, scheduleModel.zero(), creditedInterestAmount);
    }

    private void addCredit(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate transactionDate, Money creditedPrincipalAmount,
            Money creditedInterestAmount) {
        scheduleModel.changeOutstandingBalanceAndUpdateInterestPeriods(transactionDate, scheduleModel.zero(), creditedPrincipalAmount,
                scheduleModel.zero()).ifPresent(repaymentPeriod -> {
                    addCreditedAmountsToInterestPeriod(scheduleModel, transactionDate, creditedPrincipalAmount, creditedInterestAmount);
                    calculateRateFactorForRepaymentPeriod(repaymentPeriod, scheduleModel);
                    calculateOutstandingBalance(scheduleModel);
                    calculateLastUnpaidRepaymentPeriodEMI(scheduleModel, transactionDate);
                });
    }

    /**
     * This method gives back the maximum of the due principal and maximum of the due interest for a requested day.
     */
    @Override
    @NotNull
    public PeriodDueDetails getDueAmounts(@NotNull ProgressiveLoanInterestScheduleModel scheduleModel, @NotNull LocalDate periodDueDate,
            @NotNull LocalDate targetDate) {
        ProgressiveLoanInterestScheduleModel recalculatedScheduleModelTillDate = recalculateScheduleModelTillDate(scheduleModel,
                targetDate);
        RepaymentPeriod repaymentPeriod = recalculatedScheduleModelTillDate.findRepaymentPeriodByDueDate(periodDueDate).orElseThrow();
        long notFullyRepaidRepaymentPeriodCount = recalculatedScheduleModelTillDate.repaymentPeriods().stream()
                .filter(rp -> !rp.isFullyPaid()).count();
        boolean multiplePeriodIsUnpaid = notFullyRepaidRepaymentPeriodCount > 1L;
        boolean onePeriodIsUnpaid = notFullyRepaidRepaymentPeriodCount == 1L;
        if (!targetDate.isAfter(repaymentPeriod.getFromDate())) {
            if (multiplePeriodIsUnpaid) {
                repaymentPeriod.setEmi(repaymentPeriod.getOriginalEmi());
                Money totalOutstandingPrincipal = recalculatedScheduleModelTillDate.getTotalOutstandingPrincipal();
                Money outstandingPrincipal = repaymentPeriod.getOutstandingPrincipal();
                // If there are less outstanding principal than anticipated
                Money emiAdjustment = MathUtil.negativeToZero(outstandingPrincipal.minus(totalOutstandingPrincipal));
                repaymentPeriod.setEmi(repaymentPeriod.getEmi().minus(emiAdjustment));
            } else if (repaymentPeriod.isFullyPaid() && onePeriodIsUnpaid) {
                repaymentPeriod.setEmi(MathUtil.min(repaymentPeriod.getOriginalEmi(), //
                        recalculatedScheduleModelTillDate.getTotalDuePrincipal() //
                                .minus(recalculatedScheduleModelTillDate.getTotalPaidPrincipal()) //
                                .add(repaymentPeriod.getPaidPrincipal()) //
                                .add(repaymentPeriod.getDueInterest()),
                        false)); //
            }
        }

        return new PeriodDueDetails(repaymentPeriod.getEmi(), //
                repaymentPeriod.getDuePrincipal(), //
                repaymentPeriod.getDueInterest()); //
    }

    @Override
    @NotNull
    public Money getPeriodInterestTillDate(@NotNull ProgressiveLoanInterestScheduleModel scheduleModel, @NotNull LocalDate periodDueDate,
            @NotNull LocalDate targetDate, boolean includeCreditedInterest) {
        ProgressiveLoanInterestScheduleModel recalculatedScheduleModelTillDate = recalculateScheduleModelTillDate(scheduleModel,
                targetDate);
        RepaymentPeriod repaymentPeriod = recalculatedScheduleModelTillDate.findRepaymentPeriodByDueDate(periodDueDate).orElseThrow();
        return includeCreditedInterest ? repaymentPeriod.getCalculatedDueInterest()
                : repaymentPeriod.getCalculatedDueInterest().minus(repaymentPeriod.getCreditedInterest(),
                        recalculatedScheduleModelTillDate.mc());
    }

    @Override
    public Money getOutstandingLoanBalanceOfPeriod(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate targetDate) {
        ProgressiveLoanInterestScheduleModel recalculatedScheduleModelTillDate = recalculateScheduleModelTillDate(scheduleModel,
                targetDate);
        RepaymentPeriod repaymentPeriod = recalculatedScheduleModelTillDate.findRepaymentPeriod(targetDate).orElseGet(() -> {
            // If target date is after maturity date
            if (targetDate.isAfter(recalculatedScheduleModelTillDate.getLastRepaymentPeriod().getDueDate())) {
                return recalculatedScheduleModelTillDate.getLastRepaymentPeriod();
            } else {
                // if target date is before 1st disbursement date, we use 1st repayment period
                return recalculatedScheduleModelTillDate.repaymentPeriods().get(0);
            }
        });

        return repaymentPeriod.getOutstandingLoanBalance();
    }

    @Override
    public OutstandingDetails getOutstandingAmountsTillDate(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate targetDate) {
        MathContext mc = scheduleModel.mc();
        ProgressiveLoanInterestScheduleModel scheduleModelCopy = scheduleModel.deepCopy(mc);
        calculateRateFactorForScheduleTillDateInclusive(scheduleModelCopy, targetDate);
        calculateOutstandingBalance(scheduleModelCopy);
        calculateLastUnpaidRepaymentPeriodEMI(scheduleModelCopy, targetDate);

        Money totalOutstandingPrincipal = MathUtil
                .negativeToZero(scheduleModelCopy.getTotalDuePrincipal().minus(scheduleModelCopy.getTotalPaidPrincipal()));
        Money totalOutstandingInterest = MathUtil
                .negativeToZero(scheduleModelCopy.getTotalDueInterest().minus(scheduleModelCopy.getTotalPaidInterest()));
        return new OutstandingDetails(totalOutstandingPrincipal, totalOutstandingInterest);
    }

    @Override
    public void calculateRateFactorForRepaymentPeriod(final RepaymentPeriod repaymentPeriod,
            final ProgressiveLoanInterestScheduleModel scheduleModel) {
        repaymentPeriod.getInterestPeriods().forEach(interestPeriod -> {
            interestPeriod.setRateFactor(calculateRateFactorPerPeriod(scheduleModel, repaymentPeriod, interestPeriod.getFromDate(),
                    interestPeriod.getDueDate()));
            interestPeriod.setRateFactorTillPeriodDueDate(calculateRateFactorPerPeriodForInterest(scheduleModel, repaymentPeriod,
                    interestPeriod.getFromDate(), repaymentPeriod.getDueDate()));
        });
    }

    @NotNull
    private ProgressiveLoanInterestScheduleModel recalculateScheduleModelTillDate(
            @NotNull ProgressiveLoanInterestScheduleModel scheduleModel, @NotNull LocalDate targetDate) {
        MathContext mc = scheduleModel.mc();
        ProgressiveLoanInterestScheduleModel scheduleModelCopy = scheduleModel.deepCopy(mc);
        boolean isBeforeFirstDisbursement = targetDate.isBefore(scheduleModelCopy.repaymentPeriods().get(0).getFromDate());
        boolean isAfterMaturityDate = !targetDate.isBefore(scheduleModelCopy.getLastRepaymentPeriod().getDueDate());
        if (isBeforeFirstDisbursement) {
            scheduleModelCopy.repaymentPeriods().forEach(rp -> rp.getInterestPeriods().clear());
            return scheduleModelCopy;
        } else if (isAfterMaturityDate) {
            return scheduleModelCopy;
        } else {
            RepaymentPeriod repaymentPeriod = scheduleModelCopy.findRepaymentPeriod(targetDate).orElseThrow();

            scheduleModelCopy.repaymentPeriods().forEach(rp -> {
                if (rp.getDueDate().isAfter(targetDate)) {
                    if (rp.equals(repaymentPeriod)) {
                        rp.findInterestPeriod(targetDate).ifPresent(ip -> {
                            ip.setDueDate(targetDate);
                            int index = rp.getInterestPeriods().indexOf(ip);
                            int nextIdx = index + 1;
                            boolean thereIsInterestPeriodFromDateOnTargetDate = ip.getRepaymentPeriod().getInterestPeriods()
                                    .size() > nextIdx;
                            if (thereIsInterestPeriodFromDateOnTargetDate) {
                                // NOTE: If there is a next interest period with fromDate on the target date
                                // then the related credited amount comes from the next interest period too.
                                InterestPeriod nextInterestPeriod = ip.getRepaymentPeriod().getInterestPeriods().get(nextIdx);
                                ip.addCreditedPrincipalAmount(nextInterestPeriod.getCreditedPrincipal());
                                ip.addCreditedInterestAmount(nextInterestPeriod.getCreditedInterest());
                            }
                            ip.getRepaymentPeriod().getInterestPeriods()
                                    .subList(nextIdx, ip.getRepaymentPeriod().getInterestPeriods().size()).clear();
                        });
                    } else if (rp.getPrevious().isPresent() && rp.getPrevious().get().equals(repaymentPeriod)
                            && (rp.getInterestPeriods().get(0).getCreditedInterest().isGreaterThanZero()
                                    || rp.getInterestPeriods().get(0).getCreditedPrincipal().isGreaterThanZero())) {
                        // NOTE: we need to check whether there is credited on the 1st interest period of the next
                        // period
                        // if so, we need to retain that interest period, but need to update due date to match with from
                        // date -> 0 interest
                        rp.getInterestPeriods().get(0).setDueDate(rp.getInterestPeriods().get(0).getFromDate());
                        if (rp.getInterestPeriods().size() > 1) {
                            rp.getInterestPeriods().subList(1, rp.getInterestPeriods().size()).clear();
                        }
                    } else {
                        rp.getInterestPeriods().clear();
                    }
                }
            });
            calculateRateFactorForPeriods(scheduleModelCopy.repaymentPeriods(), scheduleModelCopy);
            calculateOutstandingBalance(scheduleModelCopy);
            calculateLastUnpaidRepaymentPeriodEMI(scheduleModelCopy, targetDate);
        }
        return scheduleModelCopy;
    }

    private void calculateEMIValueAndRateFactorsForFlatInterestMethod(final LocalDate calculateFromRepaymentPeriodDueDate,
            final ProgressiveLoanInterestScheduleModel scheduleModel, final EmiChangeOperation operation) {
        final List<RepaymentPeriod> relatedRepaymentPeriods = scheduleModel.getRelatedRepaymentPeriods(calculateFromRepaymentPeriodDueDate);
        calculateRateFactorForPeriods(relatedRepaymentPeriods, scheduleModel);
        if (relatedRepaymentPeriods.isEmpty()) {
            return;
        }
        calculateEMIOnActualModelWithFlatInterestMethod(relatedRepaymentPeriods, scheduleModel);
    }

    /**
     * Calculate Equal Monthly Installment value and Rate Factor -1 values for calculate Interest
     */
    private void calculateEMIValueAndRateFactors(final LocalDate calculateFromRepaymentPeriodDueDate,
            final ProgressiveLoanInterestScheduleModel scheduleModel, final EmiChangeOperation operation) {
        switch (scheduleModel.loanProductRelatedDetail().getInterestMethod()) {
            case FLAT ->
                calculateEMIValueAndRateFactorsForFlatInterestMethod(calculateFromRepaymentPeriodDueDate, scheduleModel, operation);
            case DECLINING_BALANCE -> calculateEMIValueAndRateFactorsForDecliningBalanceInterestMethod(calculateFromRepaymentPeriodDueDate,
                    scheduleModel, operation);
            default -> throw new UnsupportedOperationException(
                    "Unsupported interest method: " + scheduleModel.loanProductRelatedDetail().getInterestMethod());
        }
    }

    private void calculateEMIValueAndRateFactorsForDecliningBalanceInterestMethod(final LocalDate calculateFromRepaymentPeriodDueDate,
            final ProgressiveLoanInterestScheduleModel scheduleModel, final EmiChangeOperation operation) {
        final List<RepaymentPeriod> relatedRepaymentPeriods = scheduleModel.getRelatedRepaymentPeriods(calculateFromRepaymentPeriodDueDate);
        final boolean onlyOnActualModelShouldApply = scheduleModel.isEmpty()
                || operation.getAction() == EmiChangeOperation.Action.INTEREST_RATE_CHANGE || scheduleModel.isCopy();

        calculateRateFactorForPeriods(relatedRepaymentPeriods, scheduleModel);
        calculateOutstandingBalance(scheduleModel);
        if (onlyOnActualModelShouldApply) {
            calculateEMIOnActualModel(relatedRepaymentPeriods, scheduleModel);
        } else {
            calculateEMIOnNewModelAndMerge(relatedRepaymentPeriods, scheduleModel, operation);
        }
        calculateOutstandingBalance(scheduleModel);
        calculateLastUnpaidRepaymentPeriodEMI(scheduleModel, calculateFromRepaymentPeriodDueDate);
        if (onlyOnActualModelShouldApply && (scheduleModel.loanTermVariations() == null
                || scheduleModel.loanTermVariations().get(LoanTermVariationType.DUE_DATE) == null)) {
            checkAndAdjustEmiIfNeededOnRelatedRepaymentPeriods(scheduleModel, relatedRepaymentPeriods);
        }
    }

    private void calculateLastUnpaidRepaymentPeriodEMI(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate tillDate) {
        Optional<RepaymentPeriod> findLastUnpaidRepaymentPeriod = scheduleModel.repaymentPeriods().stream().filter(rp -> !rp.isFullyPaid())
                .reduce((first, second) -> second);

        findLastUnpaidRepaymentPeriod.ifPresent(repaymentPeriod -> {
            repaymentPeriod.setFutureUnrecognizedInterest(scheduleModel.zero());
            scheduleModel.repaymentPeriods().forEach(rp -> {
                rp.setInterestMoved(false);
            });

            MathContext mc = scheduleModel.mc();
            Money totalDueInterest = scheduleModel.repaymentPeriods().stream().map(RepaymentPeriod::getDueInterest)
                    .reduce(scheduleModel.zero(), (m1, m2) -> m1.plus(m2, mc)); // 1.46
            Money totalEMI = scheduleModel.repaymentPeriods().stream()
                    .map(RepaymentPeriod::getEmiPlusCreditedAmountsPlusFutureUnrecognizedInterest)
                    .reduce(scheduleModel.zero(), (m1, m2) -> m1.plus(m2, mc)); // 101.48
            Money totalDisbursedAmount = scheduleModel.repaymentPeriods().stream()
                    .flatMap(rp -> rp.getInterestPeriods().stream().map(InterestPeriod::getDisbursementAmount))
                    .reduce(scheduleModel.zero(), (m1, m2) -> m1.plus(m2, mc)); // 100
            Money totalCapitalizedIncome = scheduleModel.repaymentPeriods().stream()
                    .flatMap(rp -> rp.getInterestPeriods().stream().map(InterestPeriod::getCapitalizedIncomePrincipal))
                    .reduce(scheduleModel.zero(), (m1, m2) -> m1.plus(m2, mc)); // 100

            Money diff = totalDisbursedAmount.plus(totalCapitalizedIncome, mc).plus(scheduleModel.getTotalCreditedPrincipal(), mc)
                    .plus(totalDueInterest, mc).minus(totalEMI, mc);

            repaymentPeriod.setEmi(repaymentPeriod.getEmi().add(diff, mc));
            if (repaymentPeriod.getEmi()
                    .isLessThan(repaymentPeriod.getTotalPaidAmount().minus(repaymentPeriod.getTotalCreditedAmount(), mc))) {
                repaymentPeriod.setEmi(repaymentPeriod.getTotalPaidAmount().minus(repaymentPeriod.getTotalCreditedAmount(), mc));
                calculateLastUnpaidRepaymentPeriodEMI(scheduleModel, tillDate);
            }

            calculateUnrecognizedInterestTillDateOnScheduleModelCopyAndDefer(scheduleModel, repaymentPeriod, tillDate);
        });
    }

    private void calculateUnrecognizedInterestTillDateOnScheduleModelCopyAndDefer(ProgressiveLoanInterestScheduleModel scheduleModel,
            RepaymentPeriod repaymentPeriod, LocalDate tillDate) {
        MathContext mc = scheduleModel.mc();
        ProgressiveLoanInterestScheduleModel scheduleModelCopy = scheduleModel.deepCopy(mc);
        calculateRateFactorForScheduleTillDateInclusive(scheduleModelCopy, tillDate);
        Optional<RepaymentPeriod> futureUnrecognizedInterestPeriod = getPeriodWithUnrecognizedInterest(repaymentPeriod, scheduleModelCopy);

        futureUnrecognizedInterestPeriod.ifPresent(period -> {
            repaymentPeriod.setFutureUnrecognizedInterest(period.getUnrecognizedInterest());
            scheduleModel.repaymentPeriods().stream().filter(rp -> rp.getDueDate().isAfter(repaymentPeriod.getDueDate())) //
                    .forEach(rp -> {
                        rp.setInterestMoved(true);
                    });
        });
    }

    private void calculateOutstandingBalance(ProgressiveLoanInterestScheduleModel scheduleModel) {
        scheduleModel.repaymentPeriods().forEach(rp -> rp.getInterestPeriods().forEach(InterestPeriod::updateOutstandingLoanBalance));
    }

    private void checkAndAdjustEmiIfNeededOnRelatedRepaymentPeriods(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final List<RepaymentPeriod> relatedRepaymentPeriods) {
        MathContext mc = scheduleModel.mc();
        ProgressiveLoanInterestScheduleModel newScheduleModel = null;
        int adjustCounter = 1;
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
            calculateLastUnpaidRepaymentPeriodEMI(newScheduleModel, relatedPeriodsFirstDueDate);
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
        } while (adjustCounter <= 3);
    }

    /**
     * Convert Interest Percentage to fraction of 1
     *
     * @param interestRate
     *            Interest Rate in Percentage
     *
     * @return Rate Interest Rate in fraction format
     */
    private BigDecimal calcNominalInterestRatePercentage(final BigDecimal interestRate, MathContext mc) {
        return MathUtil.nullToZero(interestRate).divide(DIVISOR_100, mc);
    }

    /**
     * * Calculate rate factors from ONLY repayment periods
     */
    private void calculateRateFactorForPeriods(final List<RepaymentPeriod> repaymentPeriods,
            final ProgressiveLoanInterestScheduleModel scheduleModel) {
        repaymentPeriods.forEach(repaymentPeriod -> calculateRateFactorForRepaymentPeriod(repaymentPeriod, scheduleModel));
    }

    private boolean isPeriodContainsFeb29(final LocalDate repaymentPeriodFromDate, final LocalDate repaymentPeriodDueDate) {
        if (repaymentPeriodFromDate.isLeapYear()) {
            final LocalDate leapDay = LocalDate.of(repaymentPeriodFromDate.getYear(), 2, 29);
            return DateUtils.isDateInRangeFromExclusiveToInclusive(leapDay, repaymentPeriodFromDate, repaymentPeriodDueDate);
        } else {
            return false;
        }
    }

    private Integer numberOfDaysFeb29PeriodOnly(final LocalDate repaymentPeriodFromDate, final LocalDate repaymentPeriodDueDate) {
        return isPeriodContainsFeb29(repaymentPeriodFromDate, repaymentPeriodDueDate) ? 366 : 365;
    }

    private BigDecimal getNumberOfDays(DaysInYearType daysInYearType, DaysInYearCustomStrategyType customStrategy,
            LocalDate interestPeriodFromDate, LocalDate repaymentPeriodFromDate, LocalDate repaymentPeriodDueDate) {
        Integer numberOfDays = daysInYearType.getNumberOfDays(interestPeriodFromDate);
        if (numberOfDays == 366 && DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY.equals(customStrategy)) {
            numberOfDays = numberOfDaysFeb29PeriodOnly(repaymentPeriodFromDate, repaymentPeriodDueDate);
        }
        return BigDecimal.valueOf(numberOfDays);
    }

    BigDecimal calculateRateFactorPerPeriodForInterest(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final RepaymentPeriod repaymentPeriod, final LocalDate interestPeriodFromDate, final LocalDate interestPeriodDueDate) {
        final MathContext mc = scheduleModel.mc();
        final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail = scheduleModel.loanProductRelatedDetail();
        final BigDecimal interestRate = calcNominalInterestRatePercentage(scheduleModel.getInterestRate(interestPeriodFromDate),
                scheduleModel.mc());
        final DaysInYearType daysInYearType = DaysInYearType.fromInt(loanProductRelatedDetail.getDaysInYearType());
        final DaysInMonthType daysInMonthType = DaysInMonthType.fromInt(loanProductRelatedDetail.getDaysInMonthType());
        final PeriodFrequencyType repaymentFrequency = loanProductRelatedDetail.getRepaymentPeriodFrequencyType();
        DaysInYearCustomStrategyType daysInYearCustomStrategy = loanProductRelatedDetail.getDaysInYearCustomStrategy();
        BigDecimal daysInYear = getNumberOfDays(daysInYearType, daysInYearCustomStrategy, interestPeriodFromDate,
                repaymentPeriod.getFromDate(), repaymentPeriod.getDueDate());
        final BigDecimal actualDaysInPeriod = BigDecimal
                .valueOf(DateUtils.getDifferenceInDays(interestPeriodFromDate, interestPeriodDueDate));
        final BigDecimal calculatedDaysInPeriod = BigDecimal
                .valueOf(DateUtils.getDifferenceInDays(repaymentPeriod.getFromDate(), repaymentPeriod.getDueDate()));
        final int numberOfYearsDifferenceInPeriod = interestPeriodDueDate.getYear() - interestPeriodFromDate.getYear();
        final boolean partialPeriodCalculationNeeded = daysInYearType == DaysInYearType.ACTUAL && numberOfYearsDifferenceInPeriod > 0
                && (!DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY.equals(daysInYearCustomStrategy)
                        || isPeriodContainsFeb29(repaymentPeriod.getFromDate(), repaymentPeriod.getDueDate()));
        final BigDecimal repaymentEvery = BigDecimal.valueOf(loanProductRelatedDetail.getRepayEvery());

        if (loanProductRelatedDetail.getInterestCalculationPeriodMethod() != null
                && loanProductRelatedDetail.getInterestCalculationPeriodMethod().isSameAsRepaymentPeriod()) {

            if (loanProductRelatedDetail.getRepaymentPeriodFrequencyType().isMonthly()) {
                return rateFactorByRepaymentPeriod(interestRate, BigDecimal.ONE, repaymentEvery, BigDecimal.valueOf(12), actualDaysInPeriod,
                        calculatedDaysInPeriod, mc);
            }
            if (loanProductRelatedDetail.getRepaymentPeriodFrequencyType().isWeekly()) {
                return rateFactorByRepaymentPeriod(interestRate, BigDecimal.ONE, repaymentEvery, BigDecimal.valueOf(52), actualDaysInPeriod,
                        calculatedDaysInPeriod, mc);
            }
        }

        // TODO check: loanApplicationTerms.calculatePeriodsBetweenDates(startDate, endDate); // calculate period data
        // TODO review: (repayment frequency: days, weeks, years; validation day is month fix 30)
        // TODO refactor this logic to represent in interest period
        if (partialPeriodCalculationNeeded) {
            final BigDecimal cumulatedPeriodFractions = calculatePeriodFractions(scheduleModel, interestPeriodFromDate,
                    interestPeriodDueDate, mc);
            return rateFactorByRepaymentPartialPeriod(interestRate, BigDecimal.ONE, cumulatedPeriodFractions, BigDecimal.ONE,
                    BigDecimal.ONE, mc);
        }

        if (daysInMonthType.equals(DaysInMonthType.ACTUAL)) {
            return rateFactorByRepaymentPeriod(interestRate, actualDaysInPeriod, BigDecimal.ONE, daysInYear, BigDecimal.ONE, BigDecimal.ONE,
                    mc);
        } else if (daysInMonthType.isDaysInMonth_30()) {
            BigDecimal periodRatio = switch (repaymentFrequency) {
                case YEARS -> calculatePeriodRatio(scheduleModel, repaymentPeriod, ChronoUnit.YEARS, mc);
                case MONTHS -> calculatePeriodRatio(scheduleModel, repaymentPeriod, ChronoUnit.MONTHS, mc);
                case WEEKS -> calculatePeriodRatio(scheduleModel, repaymentPeriod, ChronoUnit.WEEKS, mc);
                case DAYS -> calculatePeriodRatio(scheduleModel, repaymentPeriod, ChronoUnit.DAYS, mc);
                default -> throw new UnsupportedOperationException("Unsupported repayment frequency: " + repaymentFrequency);
            };

            return calculateRateFactorPerPeriodBasedOnRepaymentFrequency(interestRate, repaymentFrequency, periodRatio,
                    BigDecimal.valueOf(30), daysInYear, actualDaysInPeriod, calculatedDaysInPeriod, mc);
        }
        throw new UnsupportedOperationException(
                "Unsupported combination: Days in year: " + daysInYearType + ", days in month: " + daysInMonthType);
    }

    private static BigDecimal calculatePeriodRatio(ProgressiveLoanInterestScheduleModel scheduleModel, RepaymentPeriod repaymentPeriod,
            ChronoUnit chronoUnit, MathContext mc) {

        LocalDate seedDate = calculateSeedDate(scheduleModel, repaymentPeriod);
        int numberOfPeriodBetweenSeedDateAndActualRepaymentPeriod = switch (chronoUnit) {
            case DAYS, WEEKS, YEARS -> DateUtils.getExactDifference(seedDate, repaymentPeriod.getFromDate(), chronoUnit);
            case MONTHS -> {
                int seedDateDay = seedDate.getDayOfMonth();
                int targetDateDay = repaymentPeriod.getFromDate().getDayOfMonth();
                int targetDateLastDay = ((LocalDate) TemporalAdjusters.lastDayOfMonth().adjustInto(repaymentPeriod.getFromDate()))
                        .getDayOfMonth();
                // In case target date is the last day of the month and the seed date day is later than the target date
                // day, we need to move it by 1 days
                if (targetDateLastDay == targetDateDay && seedDateDay > targetDateDay) {
                    yield DateUtils.getExactDifference(seedDate, repaymentPeriod.getFromDate().plusDays(1), chronoUnit);
                } else {
                    yield DateUtils.getExactDifference(seedDate, repaymentPeriod.getFromDate(), chronoUnit);
                }
            }
            default -> throw new UnsupportedOperationException("Unsupported chrono unit: " + chronoUnit);
        };

        int multiplicator = numberOfPeriodBetweenSeedDateAndActualRepaymentPeriod + 1;
        LocalDate fromDate = repaymentPeriod.getFromDate();
        while (fromDate.isBefore(repaymentPeriod.getDueDate())) {
            fromDate = seedDate.plus(multiplicator, chronoUnit);
            if (!fromDate.isAfter(repaymentPeriod.getDueDate())) {
                multiplicator++;
            } else {
                LocalDate fullPeriodDate = fromDate;
                multiplicator = multiplicator - numberOfPeriodBetweenSeedDateAndActualRepaymentPeriod - 1;
                fromDate = seedDate.plus(multiplicator, chronoUnit);
                final long differenceInDays = DateUtils.getDifferenceInDays(fromDate, repaymentPeriod.getDueDate());
                final long fullPeriodDifferenceInDays = DateUtils.getDifferenceInDays(fromDate, fullPeriodDate);
                return BigDecimal.valueOf(differenceInDays).divide(BigDecimal.valueOf(fullPeriodDifferenceInDays), mc)
                        .add(BigDecimal.valueOf(multiplicator));
            }
        }
        multiplicator = multiplicator - numberOfPeriodBetweenSeedDateAndActualRepaymentPeriod - 1;
        return BigDecimal.valueOf(multiplicator);
    }

    private static LocalDate calculateSeedDate(ProgressiveLoanInterestScheduleModel scheduleModel, RepaymentPeriod repaymentPeriod) {
        LocalDate seedDate = scheduleModel.getStartDate();
        LocalDate calculatedDate;
        int multiplicator = 1;
        ChronoUnit chronoUnit = switch (scheduleModel.loanProductRelatedDetail().getRepaymentPeriodFrequencyType()) {
            case YEARS -> ChronoUnit.YEARS;
            case MONTHS -> ChronoUnit.MONTHS;
            case WEEKS -> ChronoUnit.WEEKS;
            case DAYS -> ChronoUnit.DAYS;
            default -> throw new UnsupportedOperationException(
                    "Unsupported repayment frequency: " + scheduleModel.loanProductRelatedDetail().getRepaymentPeriodFrequencyType());
        };
        do {
            calculatedDate = seedDate.plus(multiplicator, chronoUnit);
            multiplicator++;
        } while (calculatedDate.isBefore(repaymentPeriod.getDueDate()));
        return calculatedDate.equals(repaymentPeriod.getDueDate()) ? seedDate : repaymentPeriod.getFromDate();
    }

    /**
     * Calculate Rate Factor for an exact Period
     */
    private BigDecimal calculateRateFactorPerPeriod(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final RepaymentPeriod repaymentPeriod, final LocalDate interestPeriodFromDate, final LocalDate interestPeriodDueDate) {
        final MathContext mc = scheduleModel.mc();
        final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail = scheduleModel.loanProductRelatedDetail();
        final BigDecimal interestRate = calcNominalInterestRatePercentage(scheduleModel.getInterestRate(interestPeriodFromDate),
                scheduleModel.mc());
        final DaysInYearType daysInYearType = DaysInYearType.fromInt(loanProductRelatedDetail.getDaysInYearType());
        final DaysInMonthType daysInMonthType = DaysInMonthType.fromInt(loanProductRelatedDetail.getDaysInMonthType());
        final PeriodFrequencyType repaymentFrequency = loanProductRelatedDetail.getRepaymentPeriodFrequencyType();
        final BigDecimal repaymentEvery = BigDecimal.valueOf(loanProductRelatedDetail.getRepayEvery());

        DaysInYearCustomStrategyType daysInYearCustomStrategy = loanProductRelatedDetail.getDaysInYearCustomStrategy();
        BigDecimal daysInYear = getNumberOfDays(daysInYearType, daysInYearCustomStrategy, interestPeriodFromDate,
                repaymentPeriod.getFromDate(), repaymentPeriod.getDueDate());
        final BigDecimal actualDaysInPeriod = BigDecimal
                .valueOf(DateUtils.getDifferenceInDays(interestPeriodFromDate, interestPeriodDueDate));
        final BigDecimal calculatedDaysInRepaymentPeriod = BigDecimal
                .valueOf(DateUtils.getDifferenceInDays(repaymentPeriod.getFromDate(), repaymentPeriod.getDueDate()));
        final int numberOfYearsDifferenceInPeriod = interestPeriodDueDate.getYear() - interestPeriodFromDate.getYear();
        final boolean partialPeriodCalculationNeeded = daysInYearType == DaysInYearType.ACTUAL && numberOfYearsDifferenceInPeriod > 0
                && (!DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY.equals(daysInYearCustomStrategy)
                        || isPeriodContainsFeb29(repaymentPeriod.getFromDate(), repaymentPeriod.getDueDate()));
        final BigDecimal daysInMonth = daysInMonthType.isDaysInMonth_30() ? BigDecimal.valueOf(30) : calculatedDaysInRepaymentPeriod;

        if (loanProductRelatedDetail.getInterestCalculationPeriodMethod() != null
                && loanProductRelatedDetail.getInterestCalculationPeriodMethod().isSameAsRepaymentPeriod()) {

            if (loanProductRelatedDetail.getRepaymentPeriodFrequencyType().isMonthly()) {
                return rateFactorByRepaymentPeriod(interestRate, BigDecimal.ONE, repaymentEvery, BigDecimal.valueOf(12), actualDaysInPeriod,
                        calculatedDaysInRepaymentPeriod, mc);
            }
            if (loanProductRelatedDetail.getRepaymentPeriodFrequencyType().isWeekly()) {
                return rateFactorByRepaymentPeriod(interestRate, BigDecimal.ONE, repaymentEvery, BigDecimal.valueOf(52), actualDaysInPeriod,
                        calculatedDaysInRepaymentPeriod, mc);
            }
        }

        // TODO check: loanApplicationTerms.calculatePeriodsBetweenDates(startDate, endDate); // calculate period data
        // TODO review: (repayment frequency: days, weeks, years; validation day is month fix 30)
        // TODO refactor this logic to represent in interest period
        if (partialPeriodCalculationNeeded) {
            final BigDecimal cumulatedPeriodFractions = calculatePeriodFractions(scheduleModel, interestPeriodFromDate,
                    interestPeriodDueDate, mc);
            return rateFactorByRepaymentPartialPeriod(interestRate, BigDecimal.ONE, cumulatedPeriodFractions, BigDecimal.ONE,
                    BigDecimal.ONE, mc);
        }

        return switch (daysInMonthType) {
            case ACTUAL -> rateFactorByRepaymentPeriod(interestRate, actualDaysInPeriod, BigDecimal.ONE, daysInYear, BigDecimal.ONE,
                    BigDecimal.ONE, mc);
            case DAYS_30 -> calculateRateFactorPerPeriodBasedOnRepaymentFrequency(interestRate, repaymentFrequency, repaymentEvery,
                    daysInMonth, daysInYear, actualDaysInPeriod, calculatedDaysInRepaymentPeriod, mc);
            default -> throw new UnsupportedOperationException("Unsupported combination: Days in month: " + daysInMonthType);
        };
    }

    /**
     * Calculate Period fractions part based on how much year has in the period
     *
     * @param scheduleModel
     * @param interestPeriodFromDate
     * @param interestPeriodDueDate
     * @return
     */
    public BigDecimal calculatePeriodFractions(ProgressiveLoanInterestScheduleModel scheduleModel, final LocalDate interestPeriodFromDate,
            final LocalDate interestPeriodDueDate, MathContext mc) {
        BigDecimal cumulatedRateFactor = BigDecimal.ZERO;
        int actualYear = interestPeriodFromDate.getYear();
        int endYear = interestPeriodDueDate.getYear();
        LocalDate actualDate = interestPeriodFromDate;
        LocalDate fractionPeriodDueDate;

        while (actualYear <= endYear) {
            fractionPeriodDueDate = actualYear == endYear ? interestPeriodDueDate
                    : getFractionPeriodDueDateForEndOfYear(scheduleModel, actualYear);
            BigDecimal numberOfDaysInYear = BigDecimal.valueOf(Year.of(actualYear).length());
            BigDecimal calculatedDaysInActualYear = BigDecimal.valueOf(DateUtils.getDifferenceInDays(actualDate, fractionPeriodDueDate));
            cumulatedRateFactor = cumulatedRateFactor.add(calculatedDaysInActualYear.divide(numberOfDaysInYear, mc), mc);
            actualDate = fractionPeriodDueDate;
            actualYear++;
        }
        return cumulatedRateFactor;
    }

    /**
     * Determines the last date of the year for interest calculation depending on the
     * isInterestRecognitionOnDisbursementDate flag.
     *
     * @param scheduleModel
     * @param year
     * @return
     */
    private LocalDate getFractionPeriodDueDateForEndOfYear(ProgressiveLoanInterestScheduleModel scheduleModel, int year) {
        if (scheduleModel.loanProductRelatedDetail().isInterestRecognitionOnDisbursementDate()) {
            return LocalDate.of(year + 1, 1, 1);
        } else {
            return LocalDate.of(year, 12, 31);
        }
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
    private BigDecimal calculateRateFactorPerPeriodBasedOnRepaymentFrequency(final BigDecimal interestRate,
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

    private void calculateEMIOnActualModelWithFlatInterestMethod(List<RepaymentPeriod> repaymentPeriods,
            ProgressiveLoanInterestScheduleModel scheduleModel) {

        final MathContext mc = scheduleModel.mc();
        final CurrencyData currency = scheduleModel.loanProductRelatedDetail().getCurrencyData();
        RepaymentPeriod first = repaymentPeriods.getFirst();
        RepaymentPeriod last = repaymentPeriods.getLast();
        Money sumOfInterest = Money.zero(currency);
        for (RepaymentPeriod rp : repaymentPeriods) {
            Money interest = rp.calculateCalculatedDueInterest();
            sumOfInterest = sumOfInterest.add(interest);
            rp.setEmi(interest);
        }

        // already repaid principals should be subtracted from total disbursed amount to calculate correct EMI.
        BigDecimal alreadyRepaidPrincipals = first.getPrevious()
                .map(rp -> rp.calculateTotalDisbursedAndCapitalizedIncomeAmountTillGivenPeriod(null)
                        .subtract(rp.getOutstandingLoanBalance().getAmount()))
                .orElse(BigDecimal.ZERO);
        Money total = Money
                .of(currency, first.calculateTotalDisbursedAndCapitalizedIncomeAmountTillGivenPeriod(first.getLastInterestPeriod()))
                .plus(sumOfInterest).minus(alreadyRepaidPrincipals);

        Money periodEmi = total.dividedBy(repaymentPeriods.size(), mc);
        Money periodEmiInMultiplesOf = applyInstallmentAmountInMultiplesOf(scheduleModel, periodEmi);
        Money remainder = total.minus(periodEmiInMultiplesOf.multipliedBy(repaymentPeriods.size(), mc));

        repaymentPeriods.forEach(rp -> {
            Money emi = rp.equals(last) ? periodEmiInMultiplesOf.add(remainder) : periodEmiInMultiplesOf;
            rp.setEmi(emi);
            rp.setOriginalEmi(emi);
            rp.getInterestPeriods().forEach(InterestPeriod::updateOutstandingLoanBalance);
        });
    }

    private void calculateEMIOnActualModel(List<RepaymentPeriod> repaymentPeriods, ProgressiveLoanInterestScheduleModel scheduleModel) {
        if (repaymentPeriods.isEmpty()) {
            return;
        }
        switch (scheduleModel.loanProductRelatedDetail().getInterestMethod()) {
            case FLAT -> calculateEMIOnActualModelWithFlatInterestMethod(repaymentPeriods, scheduleModel);
            case DECLINING_BALANCE -> calculateEMIOnActualModelWithDecliningBalanceInterestMethod(repaymentPeriods, scheduleModel);
            default -> throw new UnsupportedOperationException("Unsupported interest method");
        }
    }

    private void calculateEMIOnActualModelWithDecliningBalanceInterestMethod(List<RepaymentPeriod> repaymentPeriods,
            ProgressiveLoanInterestScheduleModel scheduleModel) {
        final MathContext mc = scheduleModel.mc();
        final BigDecimal rateFactorN = MathUtil.stripTrailingZeros(calculateRateFactorPlus1N(repaymentPeriods, mc));
        final BigDecimal fnResult = MathUtil.stripTrailingZeros(calculateFnResult(repaymentPeriods, mc));
        final RepaymentPeriod startPeriod = repaymentPeriods.get(0);

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

    private void calculateEMIOnNewModelAndMerge(List<RepaymentPeriod> repaymentPeriods, ProgressiveLoanInterestScheduleModel scheduleModel,
            final EmiChangeOperation operation) {
        if (repaymentPeriods.isEmpty()) {
            return;
        }
        final ProgressiveLoanInterestScheduleModel scheduleModelCopy = scheduleModel.copyWithoutPaidAmounts();

        addDisbursement(scheduleModelCopy, operation.withZeroAmount());
        addCapitalizedIncome(scheduleModelCopy, operation.withZeroAmount());

        final LocalDate firstDueDate = repaymentPeriods.get(0).getDueDate();
        scheduleModel.copyPeriodsFrom(firstDueDate, scheduleModelCopy.repaymentPeriods(), (newRepaymentPeriod, actualRepaymentPeriod) -> {
            actualRepaymentPeriod.setEmi(newRepaymentPeriod.getEmi());
            actualRepaymentPeriod.setOriginalEmi(newRepaymentPeriod.getOriginalEmi());
        });
    }

    private Money applyInstallmentAmountInMultiplesOf(final ProgressiveLoanInterestScheduleModel scheduleModel,
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

    private void calculateRateFactorForScheduleTillDateInclusive(ProgressiveLoanInterestScheduleModel scheduleModelCopy,
            LocalDate targetDate) {
        scheduleModelCopy.findRepaymentPeriod(targetDate).flatMap(rp -> rp.findInterestPeriod(targetDate))
                .ifPresent(ip -> ip.setDueDate(targetDate));

        calculateRateFactorForPeriods(scheduleModelCopy.repaymentPeriods(), scheduleModelCopy);

        scheduleModelCopy.repaymentPeriods()
                .forEach(rp -> rp.getInterestPeriods().stream().filter(ip -> targetDate.isBefore(ip.getDueDate())).forEach(ip -> {
                    ip.setRateFactor(BigDecimal.ZERO);
                    ip.setRateFactorTillPeriodDueDate(BigDecimal.ZERO);
                }));
    }

    private Optional<RepaymentPeriod> getPeriodWithUnrecognizedInterest(RepaymentPeriod lastUnpaidRepaymentPeriod,
            ProgressiveLoanInterestScheduleModel scheduleModelCopy) {
        for (RepaymentPeriod period : scheduleModelCopy.repaymentPeriods().reversed()) {
            if (MathUtil.isGreaterThanZero(period.getUnrecognizedInterest())
                    && period.getDueDate().isAfter(lastUnpaidRepaymentPeriod.getDueDate())) {
                return Optional.of(period);
            }
        }
        return Optional.empty();
    }

    /**
     * Calculate Rate Factor Product from rate factors
     */
    private BigDecimal calculateRateFactorPlus1N(final List<RepaymentPeriod> periods, MathContext mc) {
        return periods.stream().map(RepaymentPeriod::getRateFactorPlus1).reduce(BigDecimal.ONE,
                (BigDecimal acc, BigDecimal value) -> acc.multiply(value, mc));
    }

    /**
     * Summarize Fn values
     */
    private BigDecimal calculateFnResult(final List<RepaymentPeriod> periods, final MathContext mc) {
        return periods.stream()//
                .skip(1)//
                .map(RepaymentPeriod::getRateFactorPlus1)//
                .reduce(BigDecimal.ONE, (previousFnValue, currentRateFactor) -> fnValue(previousFnValue, currentRateFactor, mc));//
    }

    /**
     * Calculate the EMI (Equal Monthly Installment) value
     */
    private BigDecimal calculateEMIValue(final BigDecimal rateFactorPlus1N, final BigDecimal outstandingBalanceForRest,
            final BigDecimal fnResult, MathContext mc) {
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
    private BigDecimal rateFactorByRepaymentEveryDay(final BigDecimal interestRate, final BigDecimal repaymentEvery,
            final BigDecimal daysInYear, final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod, MathContext mc) {
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
    private BigDecimal rateFactorByRepaymentEveryWeek(final BigDecimal interestRate, final BigDecimal repaymentEvery,
            final BigDecimal daysInYear, final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod, MathContext mc) {
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
    private BigDecimal rateFactorByRepaymentPeriod(final BigDecimal interestRate, final BigDecimal repaymentPeriodMultiplierInDays,
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
    private BigDecimal rateFactorByRepaymentPartialPeriod(final BigDecimal interestRate, final BigDecimal repaymentEvery,
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

    @Override
    public void applyInterestPause(final ProgressiveLoanInterestScheduleModel scheduleModel, final LocalDate fromDate,
            final LocalDate endDate) {
        scheduleModel.updateInterestPeriodsForInterestPause(fromDate, endDate)
                .ifPresent(repaymentPeriod -> calculateRateFactorsForInterestPause(scheduleModel, repaymentPeriod.getFromDate()));
    }

    private void calculateRateFactorsForInterestPause(final ProgressiveLoanInterestScheduleModel scheduleModel, final LocalDate startDate) {
        final List<RepaymentPeriod> relatedRepaymentPeriods = scheduleModel.getRelatedRepaymentPeriods(startDate);
        calculateRateFactorForPeriods(relatedRepaymentPeriods, scheduleModel);
        calculateOutstandingBalance(scheduleModel);
        calculateLastUnpaidRepaymentPeriodEMI(scheduleModel, startDate);
    }

    private long getUncountablePeriods(final List<RepaymentPeriod> relatedRepaymentPeriods, final Money originalEmi) {
        return relatedRepaymentPeriods.stream() //
                .filter(repaymentPeriod -> originalEmi.isLessThan(repaymentPeriod.getTotalPaidAmount())) //
                .count(); //
    }
}
