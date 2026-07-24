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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.TransactionDateAndAmountHolder;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
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

    @Override
    public void generateInitialPeriod(final WorkingCapitalLoan loan) {
        final Optional<WorkingCapitalBreach> breachOpt = getBreachConfig(loan);
        if (breachOpt.isEmpty()) {
            return;
        }

        final Optional<LocalDate> anchorDateOptional = resolveBreachAnchorDate(loan);
        if (anchorDateOptional.isEmpty()) {
            log.warn("No breach schedule anchor date found for WC loan {}, skipping initial breach schedule generation", loan.getId());
            return;
        }

        final LocalDate fromDate = anchorDateOptional.get().plusDays(getBreachGraceDays(loan));
        final EffectiveBreachRescheduleParams params = resolveEffectiveRescheduleParams(loan.getId(), breachOpt.get());
        final LocalDate toDate = calculateToDate(fromDate, params.frequency(), params.frequencyType());
        final BigDecimal minPaymentAmount = calculateMinPaymentAmount(loan, params);

        final WorkingCapitalLoanBreachSchedule period = createPeriod(loan, 1, fromDate, toDate, minPaymentAmount);
        applyRecordedPauses(period, findEffectivePauses(loan.getId()));
        repository.saveAndFlush(period);
        log.debug("Generated initial breach schedule period for WC loan {}", loan.getId());
    }

    @Override
    public boolean hasSchedule(final Long loanId) {
        return repository.existsByLoanId(loanId);
    }

    @Override
    public void generateNextPeriodIfNeeded(final WorkingCapitalLoan loan, final LocalDate businessDate) {
        final Optional<WorkingCapitalBreach> breachOpt = getBreachConfig(loan);
        if (breachOpt.isEmpty()) {
            return;
        }

        final Optional<WorkingCapitalLoanBreachSchedule> latestPeriodOpt = repository.findTopByLoanIdOrderByPeriodNumberDesc(loan.getId());
        if (latestPeriodOpt.isEmpty() || latestPeriodOpt.get().getToDate().isAfter(businessDate)) {
            return;
        }

        final EffectiveBreachRescheduleParams params = resolveEffectiveRescheduleParams(loan.getId(), breachOpt.get());
        final Integer effectiveFrequency = params.frequency();
        final WorkingCapitalLoanPeriodFrequencyType effectiveFreqType = params.frequencyType();
        final BigDecimal minPaymentAmount = calculateMinPaymentAmount(loan, params);
        final List<EffectivePause> effectivePauses = findEffectivePauses(loan.getId());
        final List<WorkingCapitalLoanBreachSchedule> newPeriods = new ArrayList<>();

        WorkingCapitalLoanBreachSchedule latestPeriod = latestPeriodOpt.get();
        while (!latestPeriod.getToDate().isAfter(businessDate)) {
            final LocalDate newFromDate = latestPeriod.getToDate().plusDays(1);
            final LocalDate newToDate = calculateToDate(newFromDate, effectiveFrequency, effectiveFreqType);

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
        recalculatePastDueAmount(loanId);
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
        recalculatePastDueAmount(loanId);
    }

    private void applyRepaymentUndo(final WorkingCapitalLoanBreachSchedule period, final BigDecimal payAmount, final Long loanId) {
        period.setPaidAmount(period.getPaidAmount().subtract(payAmount).max(BigDecimal.ZERO));
        period.setOutstandingAmount(MathUtil.subtract(period.getMinPaymentAmount(), period.getPaidAmount()).max(BigDecimal.ZERO));
        recomputeBreach(period, DateUtils.getBusinessLocalDate());
        log.debug("Undid repayment of {} from Breach Schedule period {} for WC loan {}", payAmount, period.getPeriodNumber(), loanId);
    }

    @Override
    public void evaluateBreach(final WorkingCapitalLoan loan, final LocalDate businessDate) {
        // Sweep every expired period, not just the one covering the business date: with a LOAN_CREATION anchor the
        // first period can expire before the first COB touches the loan, so a single-period lookup would leave it
        // unbreached.
        evaluateExpiredBreaches(loan, businessDate);
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
    public void rescheduleMinimumPayment(final WorkingCapitalLoan loan) {
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

        final List<WorkingCapitalLoanBreachSchedule> periods = repository.findByLoanIdOrderByPeriodNumberAsc(loan.getId());

        WorkingCapitalLoanBreachSchedule currentPeriod = null;
        final List<WorkingCapitalLoanBreachSchedule> futurePeriods = new ArrayList<>();

        for (final WorkingCapitalLoanBreachSchedule period : periods) {
            if (period.getBreach() != null) {
                continue;
            }
            final boolean isCurrent = !period.getFromDate().isAfter(businessDate) && !period.getToDate().isBefore(businessDate);
            final boolean isFuture = period.getFromDate().isAfter(businessDate);

            if (isCurrent) {
                currentPeriod = period;
                period.setMinPaymentAmount(newMinPaymentAmount);
                period.setOutstandingAmount(newMinPaymentAmount.subtract(period.getPaidAmount()).max(BigDecimal.ZERO));
                period.setNearBreach(null);
            } else if (isFuture) {
                futurePeriods.add(period);
            }
        }

        if (currentPeriod != null) {
            repository.saveAndFlush(currentPeriod);
            updateFuturePeriods(currentPeriod, futurePeriods, newMinPaymentAmount, newFrequency, newFreqType);
        }

        evaluateExpiredBreaches(loan, businessDate);
        recalculatePastDueAmount(loan);

        log.debug("Rescheduled breach schedule for WC loan {}: new minimumPayment={} {}, frequency={} {}", loan.getId(),
                params.minimumPayment(), params.minimumPaymentType(), newFrequency, newFreqType);
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
        final List<EffectivePause> effectivePauses = findEffectivePauses(loan.getId());
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        LocalDate fromDate = periods.getFirst().getFromDate();
        for (final WorkingCapitalLoanBreachSchedule period : periods) {
            period.setFromDate(fromDate);
            period.setToDate(calculateToDate(fromDate, effectiveFrequency, effectiveFreqType));
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

    private List<EffectivePause> findEffectivePauses(final Long loanId) {
        final List<WorkingCapitalLoanBreachAction> actions = breachActionRepository.findByWorkingCapitalLoanIdOrderById(loanId);
        final List<WorkingCapitalLoanBreachAction> resumes = actions.stream()
                .filter(action -> WorkingCapitalLoanBreachActionType.RESUME.equals(action.getAction())).toList();
        return actions.stream().filter(action -> WorkingCapitalLoanBreachActionType.PAUSE.equals(action.getAction()))
                .sorted(Comparator.comparing(WorkingCapitalLoanBreachAction::getStartDate))
                .map(pause -> new EffectivePause(pause.getStartDate(), effectivePauseEnd(pause, resumes))).toList();
    }

    private LocalDate effectivePauseEnd(final WorkingCapitalLoanBreachAction pause, final List<WorkingCapitalLoanBreachAction> resumes) {
        // Resume shortens the pause to end on the resume date. Pause start and end dates are inclusive, so the resume
        // date itself becomes the effective (inclusive) end and is still treated as a paused day.
        return resumes.stream()
                .filter(resume -> !pause.getStartDate().isAfter(resume.getStartDate())
                        && !resume.getStartDate().isAfter(pause.getEndDate()))
                .map(WorkingCapitalLoanBreachAction::getStartDate).min(Comparator.naturalOrder()).orElse(pause.getEndDate());
    }

    private void applyRecordedPauses(final WorkingCapitalLoanBreachSchedule period, final List<EffectivePause> pauses) {
        for (final EffectivePause pause : pauses) {
            final LocalDate pauseStart = pause.startDate();
            final LocalDate pauseEnd = pause.endDate();
            // Apply only if the pause overlaps this period's date range
            if (!pauseEnd.isBefore(period.getFromDate()) && !pauseStart.isAfter(period.getToDate())) {
                final long pauseDays = ChronoUnit.DAYS.between(pauseStart, pauseEnd) + 1;
                period.setToDate(period.getToDate().plusDays(pauseDays));
                if (period.getFromDate().isAfter(pauseStart)) {
                    period.setFromDate(period.getFromDate().plusDays(pauseDays));
                }
            }
        }
        period.setNumberOfDays((int) ChronoUnit.DAYS.between(period.getFromDate(), period.getToDate()) + 1);
    }

    private record EffectivePause(LocalDate startDate, LocalDate endDate) {
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

    private LocalDate calculateToDate(final LocalDate fromDate, final Integer frequency,
            final WorkingCapitalLoanPeriodFrequencyType frequencyType) {
        return switch (frequencyType) {
            case DAYS -> fromDate.plusDays(frequency - 1);
            case WEEKS -> fromDate.plusWeeks(frequency).minusDays(1);
            case MONTHS -> fromDate.plusMonths(frequency).minusDays(1);
            case YEARS -> fromDate.plusYears(frequency).minusDays(1);
        };
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
        return Money.of(loan.getLoanProductRelatedDetails().getCurrency(), rawAmount).getAmount();
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

    private void evaluateExpiredBreaches(final WorkingCapitalLoan loan, final LocalDate businessDate) {
        if (isBreachEvaluationDisabled(loan.getId(), businessDate)) {
            return;
        }
        final List<WorkingCapitalLoanBreachSchedule> periods = repository
                .findByLoanIdAndBreachIsNullAndToDateLessThanEqualOrderByPeriodNumberAsc(loan.getId(), businessDate);
        for (final WorkingCapitalLoanBreachSchedule period : periods) {
            if (evaluateBreachOnDate(period, businessDate)) {
                repository.saveAndFlush(period);
            }
        }
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
            final LocalDate toDate = calculateToDate(fromDate, frequency, frequencyType);
            periodNumber++;

            period.setPeriodNumber(periodNumber);
            period.setFromDate(fromDate);
            period.setToDate(toDate);
            period.setNumberOfDays((int) ChronoUnit.DAYS.between(fromDate, toDate) + 1);
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
        balance.setBreachPastDueAmount(pastDueAmount);

        log.debug("Recalculated breach past due amount for WC loan {}: {}", loanId, pastDueAmount);
    }
}
