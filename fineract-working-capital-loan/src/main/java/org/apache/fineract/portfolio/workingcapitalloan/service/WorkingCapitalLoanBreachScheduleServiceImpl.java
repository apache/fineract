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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanBreachPastDueChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.TransactionDateAndAmountHolder;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachPauseUtils;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachScheduleEvaluationUtils;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPausePeriod;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPausePeriodUtils;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodBounds;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanBreachScheduleMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloanbreach.domain.WorkingCapitalBreach;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalBreachAmountCalculationType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanBreachStartType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class WorkingCapitalLoanBreachScheduleServiceImpl implements WorkingCapitalLoanBreachScheduleService {

    private final WorkingCapitalLoanBreachScheduleRepository repository;
    private final WorkingCapitalLoanBreachScheduleMapper mapper;
    private final WorkingCapitalLoanRepository loanRepository;
    private final WorkingCapitalLoanBreachActionRepository breachActionRepository;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanBalanceRepository balanceRepository;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public boolean generateInitialPeriod(final WorkingCapitalLoan loan) {
        final Optional<WorkingCapitalBreach> breachOpt = getBreachConfig(loan);
        if (breachOpt.isEmpty()) {
            return false;
        }

        final Optional<LocalDate> anchorDateOptional = resolveBreachAnchorDate(loan);
        if (anchorDateOptional.isEmpty()) {
            log.warn("No breach schedule anchor date found for WC loan {}, skipping initial breach schedule generation", loan.getId());
            return false;
        }

        final LocalDate fromDate = anchorDateOptional.get();
        final EffectiveBreachRescheduleParams params = resolveEffectiveRescheduleParams(loan.getId(), breachOpt.get());
        final LocalDate toDate = WorkingCapitalLoanBreachScheduleEvaluationUtils
                .calculateToDate(fromDate, params.frequency(), params.frequencyType()).plusDays(getBreachGraceDays(loan));
        final BigDecimal minPaymentAmount = calculateMinPaymentAmount(loan, params);

        final WorkingCapitalLoanBreachSchedule period = createPeriod(loan, 1, fromDate, toDate, minPaymentAmount);
        applyRecordedPauses(period, findEffectivePauses(loan.getId()));
        repository.saveAndFlush(period);
        log.debug("Generated initial breach schedule period for WC loan {}", loan.getId());
        return true;
    }

    @Override
    public boolean hasSchedule(final Long loanId) {
        return repository.existsByLoanId(loanId);
    }

    @Override
    public boolean generateNextPeriodIfNeeded(final WorkingCapitalLoan loan, final LocalDate businessDate) {
        final Optional<WorkingCapitalBreach> breachOpt = getBreachConfig(loan);
        if (breachOpt.isEmpty()) {
            return false;
        }

        final Optional<WorkingCapitalLoanBreachSchedule> latestPeriodOpt = repository.findTopByLoanIdOrderByPeriodNumberDesc(loan.getId());
        if (latestPeriodOpt.isEmpty() || latestPeriodOpt.get().getToDate().isAfter(businessDate)) {
            return false;
        }

        final EffectiveBreachRescheduleParams params = resolveEffectiveRescheduleParams(loan.getId(), breachOpt.get());
        final Integer effectiveFrequency = params.frequency();
        final WorkingCapitalLoanPeriodFrequencyType effectiveFreqType = params.frequencyType();
        final BigDecimal minPaymentAmount = calculateMinPaymentAmount(loan, params);
        final List<WorkingCapitalLoanPausePeriod> effectivePauses = findEffectivePauses(loan.getId());
        final List<WorkingCapitalLoanBreachSchedule> newPeriods = new ArrayList<>();

        WorkingCapitalLoanBreachSchedule latestPeriod = latestPeriodOpt.get();
        while (!latestPeriod.getToDate().isAfter(businessDate)) {
            final LocalDate newFromDate = latestPeriod.getToDate().plusDays(1);
            final LocalDate newToDate = WorkingCapitalLoanBreachScheduleEvaluationUtils.calculateToDate(newFromDate, effectiveFrequency,
                    effectiveFreqType);

            final WorkingCapitalLoanBreachSchedule nextPeriod = createPeriod(loan, latestPeriod.getPeriodNumber() + 1, newFromDate,
                    newToDate, minPaymentAmount);
            applyRecordedPauses(nextPeriod, effectivePauses);
            newPeriods.add(nextPeriod);
            latestPeriod = nextPeriod;
        }

        if (!newPeriods.isEmpty()) {
            repository.saveAllAndFlush(newPeriods);
            log.debug("Generated {} next breach schedule periods for WC loan {}", newPeriods.size(), loan.getId());
        }
        return !newPeriods.isEmpty();
    }

    @Override
    public boolean evaluateBreachOnDate(final WorkingCapitalLoanBreachSchedule period, final LocalDate businessDate) {
        final boolean canBreach = period.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0;
        if (canBreach) {
            if (!businessDate.isBefore(period.getToDate())) {
                period.setBreach(true);
            }
        } else {
            period.setBreach(false);
        }
        log.debug("Evaluated breach schedule period {} for WC loan {}: breach={}", period.getPeriodNumber(), period.getLoan().getId(),
                period.getBreach());
        return period.getBreach() != null;
    }

    @Override
    public void applyRepayment(final Long loanId, final LocalDate transactionDate, final BigDecimal amount) {
        if (isBreachEvaluationDisabled(loanId, DateUtils.getBusinessLocalDate())) {
            log.debug("Skipping breach schedule repayment update for WC loan {} - breach evaluation is disabled", loanId);
            return;
        }

        final Optional<WorkingCapitalLoanBreachSchedule> currentPeriod = repository
                .findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(loanId, transactionDate, transactionDate);
        if (currentPeriod.isEmpty()) {
            return;
        }
        applyRepayment(currentPeriod.get(), amount, loanId);
        recalculatePastDueAmountIfBackdated(loanId, currentPeriod.get().getToDate());
    }

    private void recalculatePastDueAmountIfBackdated(final Long loanId, final LocalDate toDate) {
        if (toDate.isBefore(DateUtils.getBusinessLocalDate())) {
            recalculatePastDueAmount(loanId);
        }
    }

    private void applyRepayment(final WorkingCapitalLoanBreachSchedule period, BigDecimal payAmount, Long loanId) {
        BigDecimal newPaidAmount = period.getPaidAmount().add(payAmount);
        period.setPaidAmount(newPaidAmount);
        period.setOutstandingAmount(MathUtil.subtract(period.getMinPaymentAmount(), period.getPaidAmount()).max(BigDecimal.ZERO));
        if (period.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
            period.setBreach(false);
        }
        log.debug("Applied repayment of {} to Breach Schedule period {} for WC loan {}", payAmount, period.getPeriodNumber(), loanId);
    }

    @Override
    public void applyRepaymentUndo(final Long loanId, final LocalDate transactionDate, final BigDecimal amount) {
        if (isBreachEvaluationDisabled(loanId, DateUtils.getBusinessLocalDate())) {
            log.debug("Skipping breach schedule repayment undo for WC loan {} - breach evaluation is disabled", loanId);
            return;
        }

        final Optional<WorkingCapitalLoanBreachSchedule> currentPeriod = repository
                .findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(loanId, transactionDate, transactionDate);
        if (currentPeriod.isEmpty()) {
            return;
        }
        applyRepaymentUndo(currentPeriod.get(), amount, loanId);
        recalculatePastDueAmountIfBackdated(loanId, currentPeriod.get().getToDate());
    }

    private void applyRepaymentUndo(final WorkingCapitalLoanBreachSchedule period, final BigDecimal payAmount, final Long loanId) {
        period.setPaidAmount(period.getPaidAmount().subtract(payAmount).max(BigDecimal.ZERO));
        period.setOutstandingAmount(MathUtil.subtract(period.getMinPaymentAmount(), period.getPaidAmount()).max(BigDecimal.ZERO));
        recomputeBreach(period, DateUtils.getBusinessLocalDate());
        log.debug("Undid repayment of {} from Breach Schedule period {} for WC loan {}", payAmount, period.getPeriodNumber(), loanId);
    }

    @Override
    public boolean evaluateBreach(final WorkingCapitalLoan loan, final LocalDate businessDate) {
        // Sweep every expired period, not just the one covering the business date: with a LOAN_CREATION anchor the
        // first period can expire before the first COB touches the loan, so a single-period lookup would leave it
        // unbreached.
        return evaluateExpiredBreaches(loan, businessDate);
    }

    @Override
    public List<WorkingCapitalLoanBreachScheduleData> retrieveBreachSchedule(final Long loanId) {
        if (!loanRepository.existsById(loanId)) {
            throw new WorkingCapitalLoanNotFoundException(loanId);
        }
        final List<WorkingCapitalLoanBreachSchedule> periods = repository.findByLoanIdOrderByPeriodNumberAsc(loanId);
        return mapper.toDataList(periods);
    }

    @Override
    public void rescheduleMinimumPayment(final WorkingCapitalLoan loan, final WorkingCapitalLoanBreachAction action) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        final Optional<WorkingCapitalBreach> breachOpt = getBreachConfig(loan);
        if (breachOpt.isEmpty()) {
            log.warn("No breach configuration found for WC loan {}, skipping reschedule", loan.getId());
            return;
        }
        final EffectiveBreachRescheduleParams params = resolveEffectiveRescheduleParams(loan.getId(), breachOpt.get());
        final BigDecimal newMinPaymentAmount = calculateMinPaymentAmount(loan, params);
        final Integer newFrequency = params.frequency();
        final WorkingCapitalLoanPeriodFrequencyType newFreqType = params.frequencyType();
        final boolean frequencyProvided = action.getFrequency() != null;

        repository.findCurrentOpenPeriod(loan.getId(), businessDate).ifPresent(currentPeriod -> {
            currentPeriod.setBaseMinPaymentAmount(newMinPaymentAmount);
            if (frequencyProvided) {
                final LocalDate newToDate = resolveRescheduledToDate(loan.getId(), currentPeriod, action.getFrequency(),
                        action.getFrequencyType());
                currentPeriod.setToDate(newToDate);
                currentPeriod.setNumberOfDays((int) ChronoUnit.DAYS.between(currentPeriod.getFromDate(), newToDate) + 1);
            }
            currentPeriod.setMinPaymentAmount(newMinPaymentAmount);
            currentPeriod.setOutstandingAmount(newMinPaymentAmount.subtract(currentPeriod.getPaidAmount()).max(BigDecimal.ZERO));
            currentPeriod.setNearBreach(null);
            repository.saveAndFlush(currentPeriod);

            final List<WorkingCapitalLoanBreachSchedule> futurePeriods = repository.findFuturePeriodsOrderByPeriodNumberAsc(loan.getId(),
                    businessDate);
            updateFuturePeriods(currentPeriod, futurePeriods, newMinPaymentAmount, newFrequency, newFreqType);
        });

        evaluateExpiredBreaches(loan, businessDate);
        recalculatePastDueAmount(loan);

        log.debug("Rescheduled breach schedule for WC loan {}: new minimumPayment={} {}, frequency={} {}", loan.getId(),
                params.minimumPayment(), params.minimumPaymentType(), newFrequency, newFreqType);
    }

    private LocalDate resolveRescheduledToDate(final Long loanId, final WorkingCapitalLoanBreachSchedule currentPeriod,
            final Integer frequency, final WorkingCapitalLoanPeriodFrequencyType frequencyType) {
        return WorkingCapitalLoanBreachScheduleEvaluationUtils.calculateRescheduledToDate(currentPeriod.getFromDate(), frequency,
                frequencyType, breachActionRepository.findByWorkingCapitalLoanIdOrderById(loanId));
    }

    @Override
    public void recalculatePeriodsForPauses(final WorkingCapitalLoan loan) {
        final Optional<WorkingCapitalBreach> breachOpt = getBreachConfig(loan);
        if (breachOpt.isEmpty()) {
            return;
        }
        final List<WorkingCapitalLoanBreachSchedule> periods = repository.findByLoanIdOrderByPeriodNumberAsc(loan.getId());
        if (periods.isEmpty()) {
            return;
        }
        final EffectiveBreachRescheduleParams params = resolveEffectiveRescheduleParams(loan.getId(), breachOpt.get());
        final Integer effectiveFrequency = params.frequency();
        final WorkingCapitalLoanPeriodFrequencyType effectiveFreqType = params.frequencyType();
        final List<WorkingCapitalLoanPausePeriod> effectivePauses = findEffectivePauses(loan.getId());
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        LocalDate fromDate = periods.getFirst().getFromDate();
        for (final WorkingCapitalLoanBreachSchedule period : periods) {
            period.setFromDate(fromDate);
            period.setToDate(
                    WorkingCapitalLoanBreachScheduleEvaluationUtils.calculateToDate(fromDate, effectiveFrequency, effectiveFreqType));
            applyRecordedPauses(period, effectivePauses);
            recomputeBreach(period, businessDate);
            fromDate = period.getToDate().plusDays(1);
        }
        repository.saveAll(periods);
        recalculatePastDueAmount(loan);
        log.debug("Recalculated breach schedule periods for WC loan {} by replaying {} effective pauses", loan.getId(),
                effectivePauses.size());
    }

    @Override
    public void reprocessBreachSchedule(final WorkingCapitalLoan loan) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        generateNextPeriodIfNeeded(loan, businessDate);
        List<WorkingCapitalLoanBreachSchedule> breachPeriods = resetAllPeriodsForReprocessing(loan.getId());

        final List<TransactionDateAndAmountHolder> transactionDateAndAmountHolderList = transactionRepository
                .fetchTransactionDateAndAmount(loan.getId(), LoanTransactionType.getRepaymentLikeTransactionTypes());
        if (transactionDateAndAmountHolderList.isEmpty()) {
            breachPeriods.forEach(period -> {
                recomputeBreach(period, businessDate);
            });
            recalculatePastDueAmount(loan);
            return;
        }

        breachPeriods.forEach(period -> {
            BigDecimal sumAmount = transactionDateAndAmountHolderList.stream().parallel()
                    .filter(holder -> DateUtils.isDateInRangeInclusive(holder.transactionDate(), period.getFromDate(), period.getToDate()))
                    .map(TransactionDateAndAmountHolder::transactionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            applyRepayment(period, sumAmount, loan.getId());
        });

        evaluateExpiredPeriods(breachPeriods, businessDate);
        recalculatePastDueAmount(loan);
    }

    private List<WorkingCapitalLoanBreachSchedule> resetAllPeriodsForReprocessing(final Long loanId) {
        final List<WorkingCapitalLoanBreachSchedule> periods = repository.findByLoanIdOrderByPeriodNumberAsc(loanId);
        for (final WorkingCapitalLoanBreachSchedule period : periods) {
            period.setPaidAmount(BigDecimal.ZERO);
            period.setOutstandingAmount(period.getMinPaymentAmount());
            period.setBreach(null);
        }
        return periods;
    }

    private void evaluateExpiredPeriods(List<WorkingCapitalLoanBreachSchedule> breachPeriods, LocalDate businessDate) {
        breachPeriods.stream() //
                .filter(period -> period.getBreach() == null) //
                .filter(period -> DateUtils.isBefore(period.getToDate(), businessDate)) //
                .toList().forEach(period -> {
                    boolean criteriaMet = period.getMinPaymentAmount().compareTo(period.getPaidAmount()) > 0;
                    period.setBreach(criteriaMet);
                });
    }

    private void recomputeBreach(final WorkingCapitalLoanBreachSchedule period, final LocalDate businessDate) {
        if (period.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
            period.setBreach(false);
        } else if (businessDate.isAfter(period.getToDate())) {
            period.setBreach(true);
        } else {
            period.setBreach(null);
        }
    }

    private List<WorkingCapitalLoanPausePeriod> findEffectivePauses(final Long loanId) {
        return WorkingCapitalLoanBreachPauseUtils.toEffectivePauses(breachActionRepository.findByWorkingCapitalLoanIdOrderById(loanId));
    }

    private void applyRecordedPauses(final WorkingCapitalLoanBreachSchedule period, final List<WorkingCapitalLoanPausePeriod> pauses) {
        final WorkingCapitalLoanPeriodBounds bounds = WorkingCapitalLoanPausePeriodUtils.applyPauses(period.getFromDate(),
                period.getToDate(), pauses);
        period.setFromDate(bounds.fromDate());
        period.setToDate(bounds.toDate());
        period.setNumberOfDays((int) ChronoUnit.DAYS.between(bounds.fromDate(), bounds.toDate()) + 1);
    }

    private WorkingCapitalLoanBreachSchedule createPeriod(final WorkingCapitalLoan loan, final int periodNumber, final LocalDate fromDate,
            final LocalDate toDate, final BigDecimal minPaymentAmount) {
        final int numberOfDays = (int) ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        final WorkingCapitalLoanBreachSchedule period = new WorkingCapitalLoanBreachSchedule();
        period.setLoan(loan);
        period.setPeriodNumber(periodNumber);
        period.setFromDate(fromDate);
        period.setToDate(toDate);
        period.setNumberOfDays(numberOfDays);
        period.setBaseMinPaymentAmount(minPaymentAmount);
        period.setMinPaymentAmount(minPaymentAmount);
        period.setPaidAmount(BigDecimal.ZERO);
        period.setOutstandingAmount(minPaymentAmount);
        period.setNearBreach(null);
        period.setBreach(null);
        return period;
    }

    private Optional<WorkingCapitalBreach> getBreachConfig(final WorkingCapitalLoan loan) {
        final WorkingCapitalLoanProductRelatedDetails details = loan.getLoanProductRelatedDetails();
        if (details == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(details.getBreach());
    }

    private Integer getBreachGraceDays(final WorkingCapitalLoan loan) {
        final WorkingCapitalLoanProductRelatedDetails details = loan.getLoanProductRelatedDetails();
        return (details == null || details.getBreachGraceDays() == null) ? 0 : details.getBreachGraceDays();
    }

    private Optional<LocalDate> resolveBreachAnchorDate(final WorkingCapitalLoan loan) {
        final WorkingCapitalLoanProductRelatedDetails details = loan.getLoanProductRelatedDetails();
        final WorkingCapitalLoanBreachStartType breachStartType = (details == null || details.getBreachStartType() == null)
                ? WorkingCapitalLoanBreachStartType.DISBURSEMENT
                : details.getBreachStartType();
        if (WorkingCapitalLoanBreachStartType.LOAN_CREATION.equals(breachStartType)) {
            return Optional.ofNullable(loan.getSubmittedOnDate());
        }
        return loanRepository.findFirstActualDisbursementDate(loan.getId());
    }

    private BigDecimal calculateMinPaymentAmount(final WorkingCapitalLoan loan, final EffectiveBreachRescheduleParams params) {
        final BigDecimal breachAmount = params.minimumPayment();
        if (breachAmount == null) {
            return BigDecimal.ZERO;
        }
        if (WorkingCapitalBreachAmountCalculationType.FLAT.equals(params.minimumPaymentType())) {
            return breachAmount;
        }
        final BigDecimal principal = loan.getApprovedPrincipal();
        if (principal == null) {
            return BigDecimal.ZERO;
        }
        final BigDecimal discount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getDiscount() : null;
        final BigDecimal base = discount != null ? principal.add(discount) : principal;
        final BigDecimal rawAmount = MathUtil.percentageOf(base, breachAmount, MoneyHelper.getMathContext());
        return Money.of(loan.getCurrency(), rawAmount).getAmount();
    }

    private EffectiveBreachRescheduleParams resolveEffectiveRescheduleParams(final Long loanId, final WorkingCapitalBreach breach) {
        final List<WorkingCapitalLoanBreachAction> reschedules = breachActionRepository
                .findByWorkingCapitalLoanIdAndActionOrderByIdDesc(loanId, WorkingCapitalLoanBreachActionType.RESCHEDULE);
        final Optional<WorkingCapitalLoanBreachAction> latestWithPayment = reschedules.stream()
                .filter(action -> action.getMinimumPayment() != null).findFirst();
        final Optional<WorkingCapitalLoanBreachAction> latestWithFrequency = reschedules.stream()
                .filter(action -> action.getFrequency() != null).findFirst();

        return new EffectiveBreachRescheduleParams(
                latestWithPayment.map(WorkingCapitalLoanBreachAction::getMinimumPayment).orElse(breach.getBreachAmount()),
                latestWithPayment.map(WorkingCapitalLoanBreachAction::getMinimumPaymentType)
                        .or(() -> Optional.ofNullable(breach.getBreachAmountCalculationType()))
                        .orElse(WorkingCapitalBreachAmountCalculationType.PERCENTAGE),
                latestWithFrequency.map(WorkingCapitalLoanBreachAction::getFrequency).orElse(breach.getBreachFrequency()),
                latestWithFrequency.map(WorkingCapitalLoanBreachAction::getFrequencyType).orElse(breach.getBreachFrequencyType()));
    }

    private boolean evaluateExpiredBreaches(final WorkingCapitalLoan loan, final LocalDate businessDate) {
        if (isBreachEvaluationDisabled(loan.getId(), businessDate)) {
            return false;
        }
        final List<WorkingCapitalLoanBreachSchedule> periods = repository
                .findByLoanIdAndBreachIsNullAndToDateLessThanEqualOrderByPeriodNumberAsc(loan.getId(), businessDate);
        boolean evaluated = false;
        for (final WorkingCapitalLoanBreachSchedule period : periods) {
            if (evaluateBreachOnDate(period, businessDate)) {
                repository.saveAndFlush(period);
                evaluated = true;
            }
        }
        return evaluated;
    }

    private boolean isBreachEvaluationDisabled(final Long loanId, final LocalDate date) {
        return breachActionRepository.isBreachDisabledAsOf(loanId, date);
    }

    private void updateFuturePeriods(final WorkingCapitalLoanBreachSchedule currentPeriod,
            final List<WorkingCapitalLoanBreachSchedule> existingFuturePeriods, final BigDecimal minPaymentAmount, final Integer frequency,
            final WorkingCapitalLoanPeriodFrequencyType frequencyType) {
        int periodNumber = currentPeriod.getPeriodNumber();
        LocalDate fromDate = currentPeriod.getToDate().plusDays(1);

        for (final WorkingCapitalLoanBreachSchedule period : existingFuturePeriods) {
            final LocalDate toDate = WorkingCapitalLoanBreachScheduleEvaluationUtils.calculateToDate(fromDate, frequency, frequencyType);
            periodNumber++;

            period.setPeriodNumber(periodNumber);
            period.setFromDate(fromDate);
            period.setToDate(toDate);
            period.setNumberOfDays((int) ChronoUnit.DAYS.between(fromDate, toDate) + 1);
            period.setBaseMinPaymentAmount(minPaymentAmount);
            period.setMinPaymentAmount(minPaymentAmount);
            period.setPaidAmount(BigDecimal.ZERO);
            period.setOutstandingAmount(minPaymentAmount);
            period.setNearBreach(null);
            period.setBreach(null);

            fromDate = toDate.plusDays(1);
        }
        repository.saveAll(existingFuturePeriods);
    }

    @Override
    public void recalculatePastDueAmount(final WorkingCapitalLoan loan) {
        recalculatePastDueAmount(loan.getId());
    }

    private void recalculatePastDueAmount(final Long loanId) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        if (isBreachEvaluationDisabled(loanId, businessDate)) {
            log.debug("Skipping breach past due recalculation for WC loan {} - breach evaluation is disabled", loanId);
            return;
        }
        final Optional<WorkingCapitalLoanBalance> balanceOpt = balanceRepository.findByWcLoan_Id(loanId);
        if (balanceOpt.isEmpty()) {
            return;
        }
        final List<WorkingCapitalLoanBreachSchedule> periods = repository.findByLoanIdOrderByPeriodNumberAsc(loanId);
        capLastPeriodToRemainingBalance(loanId, periods, balanceOpt.get(), businessDate);
        Optional<WorkingCapitalLoanBreachSchedule> lastResetPeriod = periods.stream().filter(WorkingCapitalLoanBreachSchedule::isReset)
                .reduce((a, b) -> b);
        final BigDecimal pastDueAmount;
        if (lastResetPeriod.isPresent()) {
            pastDueAmount = periods.subList(periods.indexOf(lastResetPeriod.get()), periods.size()).stream()
                    .filter(period -> !period.getToDate().isAfter(businessDate)).map(WorkingCapitalLoanBreachSchedule::getOutstandingAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add).max(BigDecimal.ZERO);
        } else {
            pastDueAmount = periods.stream().filter(period -> !period.getToDate().isAfter(businessDate))
                    .map(WorkingCapitalLoanBreachSchedule::getOutstandingAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                    .max(BigDecimal.ZERO);
        }

        final WorkingCapitalLoanBalance balance = balanceOpt.get();
        final boolean changed = !MathUtil.isEqualTo(balance.getBreachPastDueAmount(), pastDueAmount);
        balance.setBreachPastDueAmount(pastDueAmount);

        log.debug("Recalculated breach past due amount for WC loan {}: {}", loanId, pastDueAmount);
        if (changed) {
            balanceRepository.saveAndFlush(balance);
            businessEventNotifierService
                    .notifyPostBusinessEvent(new WorkingCapitalLoanBreachPastDueChangeBusinessEvent(balance.getWcLoan()));
        }
    }

    /**
     * Caps the open period's minimum payment at what the customer can still owe against it - what they have already
     * paid into it plus the loan's remaining principal. In the last period of a loan the calculated minimum payment can
     * exceed what is still owed, and a period must never demand more than the balance.
     *
     * <p>
     * The cap is always derived from {@code baseMinPaymentAmount}, never from the possibly already-capped
     * {@code minPaymentAmount}, so it is idempotent and lifts again on its own - up to the calculated minimum payment -
     * when the balance grows. The base is left untouched here; it is written only where the minimum payment is
     * calculated or rescheduled.
     *
     * <p>
     * The remaining principal includes any principal adjustment re-injected by an over-refunding credit balance refund,
     * which is how such an adjustment reaches the breach schedule. It is deliberately global: the adjustment is not
     * attributed to a period of its own.
     *
     * <p>
     * Only the still-open last period is capped. Once a period has expired its demand was settled by the balance of its
     * own time, so the cap it carried at expiry stands - re-capping it against today's balance would erase historical
     * breaches as soon as the loan is repaid.
     */
    private void capLastPeriodToRemainingBalance(final Long loanId, final List<WorkingCapitalLoanBreachSchedule> periods,
            final WorkingCapitalLoanBalance balance, final LocalDate businessDate) {
        if (periods.isEmpty()) {
            return;
        }
        final WorkingCapitalLoanBreachSchedule lastPeriod = periods.getLast();
        if (DateUtils.isBefore(lastPeriod.getToDate(), businessDate)) {
            return;
        }
        // A period predating the base column carries no base to restore from; its current minimum payment is the best
        // available stand-in. The Liquibase backfill means this only applies to rows written before the upgrade.
        final BigDecimal baseMinPaymentAmount = lastPeriod.getBaseMinPaymentAmount() != null ? lastPeriod.getBaseMinPaymentAmount()
                : MathUtil.nullToZero(lastPeriod.getMinPaymentAmount());
        final BigDecimal paidAmount = MathUtil.nullToZero(lastPeriod.getPaidAmount());
        final BigDecimal cap = MathUtil.add(paidAmount, MathUtil.nullToZero(balance.getPrincipalOutstanding()));
        final BigDecimal cappedMinPaymentAmount = baseMinPaymentAmount.min(cap);

        lastPeriod.setMinPaymentAmount(cappedMinPaymentAmount);
        lastPeriod.setOutstandingAmount(MathUtil.subtract(cappedMinPaymentAmount, paidAmount).max(BigDecimal.ZERO));
        recomputeBreach(lastPeriod, businessDate);
        if (cappedMinPaymentAmount.compareTo(baseMinPaymentAmount) < 0) {
            log.debug("Capped Breach Schedule period {} minimum payment from {} to {} for WC loan {} - remaining principal {}",
                    lastPeriod.getPeriodNumber(), baseMinPaymentAmount, cappedMinPaymentAmount, loanId, balance.getPrincipalOutstanding());
        }
    }
}
