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
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanBreachScheduleMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloanbreach.domain.WorkingCapitalBreach;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalBreachAmountCalculationType;
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

    @Override
    public void generateInitialPeriod(final WorkingCapitalLoan loan) {
        final Optional<WorkingCapitalBreach> breachOpt = getBreachConfig(loan);
        if (breachOpt.isEmpty()) {
            return;
        }

        final Optional<LocalDate> disbursementDateOptional = loanRepository.findFirstActualDisbursementDate(loan.getId());
        if (disbursementDateOptional.isEmpty()) {
            log.warn("No actual disbursement date found for WC loan {}, skipping initial breach schedule generation", loan.getId());
            return;
        }

        final LocalDate fromDate = disbursementDateOptional.get().plusDays(getBreachGraceDays(loan));
        final WorkingCapitalBreach breach = breachOpt.get();
        final Optional<WorkingCapitalLoanBreachAction> latestReschedule = findLatestRescheduleAction(loan.getId());
        final Integer effectiveFrequency = resolveFrequency(latestReschedule.orElse(null), breach);
        final WorkingCapitalLoanPeriodFrequencyType effectiveFreqType = resolveFrequencyType(latestReschedule.orElse(null), breach);
        final LocalDate toDate = calculateToDate(fromDate, effectiveFrequency, effectiveFreqType);
        final BigDecimal minPaymentAmount = calculateMinPaymentAmount(loan, breach, latestReschedule.orElse(null));

        final WorkingCapitalLoanBreachSchedule period = createPeriod(loan, 1, fromDate, toDate, minPaymentAmount);
        applyRecordedPauses(period, findRecordedPauses(loan.getId()));
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
        if (latestPeriodOpt.isEmpty()) {
            return;
        }

        final WorkingCapitalBreach breach = breachOpt.get();
        final Optional<WorkingCapitalLoanBreachAction> latestReschedule = findLatestRescheduleAction(loan.getId());
        final Integer effectiveFrequency = resolveFrequency(latestReschedule.orElse(null), breach);
        final WorkingCapitalLoanPeriodFrequencyType effectiveFreqType = resolveFrequencyType(latestReschedule.orElse(null), breach);
        final BigDecimal minPaymentAmount = calculateMinPaymentAmount(loan, breach, latestReschedule.orElse(null));
        final List<WorkingCapitalLoanBreachAction> recordedPauses = findRecordedPauses(loan.getId());
        final List<WorkingCapitalLoanBreachSchedule> newPeriods = new ArrayList<>();

        WorkingCapitalLoanBreachSchedule latestPeriod = latestPeriodOpt.get();
        while (!latestPeriod.getToDate().isAfter(businessDate)) {
            final LocalDate newFromDate = latestPeriod.getToDate().plusDays(1);
            final LocalDate newToDate = calculateToDate(newFromDate, effectiveFrequency, effectiveFreqType);

            final WorkingCapitalLoanBreachSchedule nextPeriod = createPeriod(loan, latestPeriod.getPeriodNumber() + 1, newFromDate,
                    newToDate, minPaymentAmount);
            applyRecordedPauses(nextPeriod, recordedPauses);
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
    public void applyRepayment(Long loanId, LocalDate transactionDate, BigDecimal amount) {
        Optional<WorkingCapitalLoanBreachSchedule> currentPeriod = repository
                .findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(loanId, transactionDate, transactionDate);
        currentPeriod.ifPresent(period -> applyRepayment(period, amount, loanId));
    }

    private void applyRepayment(final WorkingCapitalLoanBreachSchedule period, BigDecimal payAmount, Long loanId) {
        BigDecimal newPaidAmount = period.getPaidAmount().add(payAmount);
        period.setPaidAmount(newPaidAmount);
        period.setOutstandingAmount(period.getOutstandingAmount().subtract(payAmount).max(BigDecimal.ZERO));
        if (period.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
            period.setBreach(false);
        }
        repository.saveAndFlush(period);
        log.debug("Applied repayment of {} to Breach Schedule period {} for WC loan {}", payAmount, period.getPeriodNumber(), loanId);
    }

    @Override
    public void evaluateBreach(final WorkingCapitalLoan loan, final LocalDate businessDate) {
        final Optional<WorkingCapitalLoanBreachSchedule> relevantPeriod = repository
                .findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(loan.getId(), businessDate, businessDate);
        if (relevantPeriod.isEmpty()) {
            return;
        }
        final WorkingCapitalLoanBreachSchedule period = relevantPeriod.get();
        if (period.getBreach() != null) {
            return;
        }
        if (evaluateBreachOnDate(period, businessDate)) {
            repository.saveAndFlush(period);
        }
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
    public void rescheduleMinimumPayment(final WorkingCapitalLoan loan, final WorkingCapitalLoanBreachAction rescheduleAction) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        final Optional<WorkingCapitalBreach> breachOpt = getBreachConfig(loan);
        if (breachOpt.isEmpty()) {
            log.warn("No breach configuration found for WC loan {}, skipping reschedule", loan.getId());
            return;
        }
        final WorkingCapitalBreach breach = breachOpt.get();
        final BigDecimal newMinPaymentAmount = calculateMinPaymentAmount(loan, breach, rescheduleAction);
        final Integer newFrequency = resolveFrequency(rescheduleAction, breach);
        final WorkingCapitalLoanPeriodFrequencyType newFreqType = resolveFrequencyType(rescheduleAction, breach);

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

        log.debug("Rescheduled breach schedule for WC loan {}: new minimumPayment={} {}, frequency={} {}", loan.getId(),
                rescheduleAction.getMinimumPayment(), rescheduleAction.getMinimumPaymentType(), newFrequency, newFreqType);
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
        final WorkingCapitalBreach breach = breachOpt.get();
        final Optional<WorkingCapitalLoanBreachAction> latestReschedule = findLatestRescheduleAction(loan.getId());
        final Integer effectiveFrequency = resolveFrequency(latestReschedule.orElse(null), breach);
        final WorkingCapitalLoanPeriodFrequencyType effectiveFreqType = resolveFrequencyType(latestReschedule.orElse(null), breach);
        final List<WorkingCapitalLoanBreachAction> recordedPauses = findRecordedPauses(loan.getId());
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        LocalDate fromDate = periods.getFirst().getFromDate();
        for (final WorkingCapitalLoanBreachSchedule period : periods) {
            period.setFromDate(fromDate);
            period.setToDate(calculateToDate(fromDate, effectiveFrequency, effectiveFreqType));
            applyRecordedPauses(period, recordedPauses);
            recomputeBreach(period, businessDate);
            fromDate = period.getToDate().plusDays(1);
        }
        repository.saveAll(periods);
        log.debug("Recalculated breach schedule periods for WC loan {} by replaying {} recorded pauses", loan.getId(),
                recordedPauses.size());
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

    private List<WorkingCapitalLoanBreachAction> findRecordedPauses(final Long loanId) {
        return breachActionRepository.findByWorkingCapitalLoanIdOrderById(loanId).stream()
                .filter(action -> WorkingCapitalLoanBreachActionType.PAUSE.equals(action.getAction()))
                .sorted(Comparator.comparing(WorkingCapitalLoanBreachAction::getStartDate)).toList();
    }

    private void applyRecordedPauses(final WorkingCapitalLoanBreachSchedule period,
            final List<WorkingCapitalLoanBreachAction> pauseActions) {
        for (final WorkingCapitalLoanBreachAction pause : pauseActions) {
            final LocalDate pauseStart = pause.getStartDate();
            final LocalDate pauseEnd = pause.getEndDate();
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

    private LocalDate calculateToDate(final LocalDate fromDate, final Integer frequency,
            final WorkingCapitalLoanPeriodFrequencyType frequencyType) {
        return switch (frequencyType) {
            case DAYS -> fromDate.plusDays(frequency - 1);
            case WEEKS -> fromDate.plusWeeks(frequency).minusDays(1);
            case MONTHS -> fromDate.plusMonths(frequency).minusDays(1);
            case YEARS -> fromDate.plusYears(frequency).minusDays(1);
        };
    }

    private BigDecimal calculateMinPaymentAmount(final WorkingCapitalLoan loan, final WorkingCapitalBreach breach,
            final WorkingCapitalLoanBreachAction rescheduleOverride) {
        final BigDecimal effectiveBreachAmount = resolveBreachAmount(rescheduleOverride, breach);
        if (effectiveBreachAmount == null) {
            return BigDecimal.ZERO;
        }
        final WorkingCapitalBreachAmountCalculationType effectiveCalculationType = resolveBreachAmountCalculationType(rescheduleOverride,
                breach);
        if (WorkingCapitalBreachAmountCalculationType.FLAT.equals(effectiveCalculationType)) {
            return effectiveBreachAmount;
        }
        final BigDecimal principal = loan.getApprovedPrincipal();
        if (principal == null) {
            return BigDecimal.ZERO;
        }
        final BigDecimal discount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getDiscount() : null;
        final BigDecimal base = discount != null ? principal.add(discount) : principal;
        final BigDecimal rawAmount = MathUtil.percentageOf(base, effectiveBreachAmount, MoneyHelper.getMathContext());
        return Money.of(loan.getLoanProductRelatedDetails().getCurrency(), rawAmount).getAmount();
    }

    private Optional<WorkingCapitalLoanBreachAction> findLatestRescheduleAction(final Long loanId) {
        return breachActionRepository.findTopByWorkingCapitalLoanIdAndActionOrderByIdDesc(loanId,
                WorkingCapitalLoanBreachActionType.RESCHEDULE);
    }

    private Integer resolveFrequency(final WorkingCapitalLoanBreachAction rescheduleOverride, final WorkingCapitalBreach breach) {
        if (rescheduleOverride != null && rescheduleOverride.getFrequency() != null) {
            return rescheduleOverride.getFrequency();
        }
        return breach.getBreachFrequency();
    }

    private WorkingCapitalLoanPeriodFrequencyType resolveFrequencyType(final WorkingCapitalLoanBreachAction rescheduleOverride,
            final WorkingCapitalBreach breach) {
        if (rescheduleOverride != null && rescheduleOverride.getFrequencyType() != null) {
            return rescheduleOverride.getFrequencyType();
        }
        return breach.getBreachFrequencyType();
    }

    private BigDecimal resolveBreachAmount(final WorkingCapitalLoanBreachAction rescheduleOverride, final WorkingCapitalBreach breach) {
        if (rescheduleOverride != null && rescheduleOverride.getMinimumPayment() != null) {
            return rescheduleOverride.getMinimumPayment();
        }
        return breach.getBreachAmount();
    }

    private WorkingCapitalBreachAmountCalculationType resolveBreachAmountCalculationType(
            final WorkingCapitalLoanBreachAction rescheduleOverride, final WorkingCapitalBreach breach) {
        if (rescheduleOverride != null && rescheduleOverride.getMinimumPaymentType() != null) {
            return rescheduleOverride.getMinimumPaymentType();
        }
        return breach.getBreachAmountCalculationType() != null ? breach.getBreachAmountCalculationType()
                : WorkingCapitalBreachAmountCalculationType.PERCENTAGE;
    }

    private void evaluateExpiredBreaches(final WorkingCapitalLoan loan, final LocalDate businessDate) {
        final List<WorkingCapitalLoanBreachSchedule> periods = repository.findByLoanIdOrderByPeriodNumberAsc(loan.getId());
        for (final WorkingCapitalLoanBreachSchedule period : periods) {
            if (period.getBreach() != null) {
                continue;
            }
            if (!period.getToDate().isAfter(businessDate) && evaluateBreachOnDate(period, businessDate)) {
                repository.saveAndFlush(period);
            }
        }
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

}
