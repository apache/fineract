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
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyFrequencyType;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentPeriodAndRule;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentPeriodAndRuleRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentType;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanDelinquencyRangeScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanDelinquencyRangeScheduleMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class WorkingCapitalLoanDelinquencyRangeScheduleServiceImpl implements WorkingCapitalLoanDelinquencyRangeScheduleService {

    private final WorkingCapitalLoanDelinquencyRangeScheduleRepository loanDelinquencyRangeScheduleRepository;
    private final WorkingCapitalLoanDelinquencyActionRepository loanDelinquencyActionRepository;
    private final WorkingCapitalLoanDelinquencyRangeScheduleMapper capitalLoanDelinquencyRangeScheduleMapper;
    private final DelinquencyMinimumPaymentPeriodAndRuleRepository minimumPaymentPeriodAndRuleRepository;

    @Override
    public void generateInitialPeriod(WorkingCapitalLoan loan) {
        DelinquencyMinimumPaymentPeriodAndRule rule = getMinimumPaymentRule(loan);
        if (rule == null) {
            return;
        }

        LocalDate fromDate = loan.getDisbursementDetails().stream().map(WorkingCapitalLoanDisbursementDetails::getActualDisbursementDate)
                .filter(Objects::nonNull).findFirst().orElse(null);
        if (fromDate == null) {
            log.warn("No actual disbursement date found for WC loan {}, skipping initial period generation", loan.getId());
            return;
        }
        LocalDate toDate = calculateToDate(fromDate, rule.getFrequency(), rule.getFrequencyType());
        BigDecimal expectedAmount = calculateExpectedAmount(loan, rule, null);

        WorkingCapitalLoanDelinquencyRangeSchedule period = new WorkingCapitalLoanDelinquencyRangeSchedule();
        period.setLoan(loan);
        period.setPeriodNumber(1);
        period.setFromDate(fromDate);
        period.setToDate(toDate);
        period.setExpectedAmount(expectedAmount);
        period.setPaidAmount(BigDecimal.ZERO);
        period.setOutstandingAmount(expectedAmount);
        period.setMinPaymentCriteriaMet(null);

        loanDelinquencyRangeScheduleRepository.saveAndFlush(period);
        log.debug("Generated initial delinquency range schedule period for WC loan {}", loan.getId());
    }

    @Override
    public boolean hasSchedule(Long loanId) {
        return loanDelinquencyRangeScheduleRepository.findTopByLoanIdOrderByPeriodNumberDesc(loanId).isPresent();
    }

    @Override
    public void generateNextPeriodIfNeeded(WorkingCapitalLoan loan, LocalDate businessDate) {
        final DelinquencyMinimumPaymentPeriodAndRule rule = getMinimumPaymentRule(loan);
        if (rule == null) {
            return;
        }

        final Optional<WorkingCapitalLoanDelinquencyRangeSchedule> latestPeriodOpt = loanDelinquencyRangeScheduleRepository
                .findTopByLoanIdOrderByPeriodNumberDesc(loan.getId());
        if (latestPeriodOpt.isEmpty()) {
            return;
        }

        final Optional<WorkingCapitalLoanDelinquencyAction> latestReschedule = findLatestRescheduleAction(loan.getId());
        final Integer effectiveFrequency = latestReschedule.map(WorkingCapitalLoanDelinquencyAction::getFrequency)
                .orElse(rule.getFrequency());
        final DelinquencyFrequencyType effectiveFreqType = latestReschedule.map(WorkingCapitalLoanDelinquencyAction::getFrequencyType)
                .orElse(rule.getFrequencyType());

        WorkingCapitalLoanDelinquencyRangeSchedule latestPeriod = latestPeriodOpt.get();
        while (!latestPeriod.getToDate().isAfter(businessDate)) {
            final LocalDate newFromDate = latestPeriod.getToDate().plusDays(1);
            final LocalDate newToDate = calculateToDate(newFromDate, effectiveFrequency, effectiveFreqType);
            final BigDecimal expectedAmount = calculateExpectedAmount(loan, rule, latestReschedule.orElse(null));

            final WorkingCapitalLoanDelinquencyRangeSchedule nextPeriod = new WorkingCapitalLoanDelinquencyRangeSchedule();
            nextPeriod.setLoan(loan);
            nextPeriod.setPeriodNumber(latestPeriod.getPeriodNumber() + 1);
            nextPeriod.setFromDate(newFromDate);
            nextPeriod.setToDate(newToDate);
            nextPeriod.setExpectedAmount(expectedAmount);
            nextPeriod.setPaidAmount(BigDecimal.ZERO);
            nextPeriod.setOutstandingAmount(expectedAmount);
            nextPeriod.setMinPaymentCriteriaMet(null);

            latestPeriod = loanDelinquencyRangeScheduleRepository.saveAndFlush(nextPeriod);
            log.debug("Generated next delinquency range schedule period {} for WC loan {}", nextPeriod.getPeriodNumber(), loan.getId());
        }
    }

    @Override
    public void applyRepayment(Long loanId, LocalDate transactionDate, BigDecimal amount) {
        List<WorkingCapitalLoanDelinquencyRangeSchedule> pastOpenPeriods = loanDelinquencyRangeScheduleRepository
                .findByLoanIdAndToDateIsBeforeAndMinPaymentCriteriaMet(loanId, transactionDate, false);
        Optional<WorkingCapitalLoanDelinquencyRangeSchedule> currentPeriod = loanDelinquencyRangeScheduleRepository
                .findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(loanId, transactionDate, transactionDate);
        BigDecimal transactionAmount = amount;
        for (WorkingCapitalLoanDelinquencyRangeSchedule period : pastOpenPeriods) {
            BigDecimal payAmount = MathUtil.min(amount, period.getOutstandingAmount(), true);
            transactionAmount = transactionAmount.subtract(payAmount);
            period.setPaidAmount(period.getPaidAmount().add(payAmount));
            period.setOutstandingAmount(period.getOutstandingAmount().subtract(payAmount));
            if (period.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
                period.setMinPaymentCriteriaMet(true);
                period.setDelinquentAmount(BigDecimal.ZERO);
                period.setDelinquentDays(0L);
            }
            loanDelinquencyRangeScheduleRepository.saveAndFlush(period);
            log.debug("Applied repayment of {} to delinquency range schedule period {} for WC loan {}", payAmount, period.getPeriodNumber(),
                    loanId);
            if (transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
        }
        if (currentPeriod.isPresent()) {
            WorkingCapitalLoanDelinquencyRangeSchedule period = currentPeriod.get();
            BigDecimal newPaidAmount = period.getPaidAmount().add(transactionAmount);
            period.setPaidAmount(newPaidAmount);
            period.setOutstandingAmount(period.getExpectedAmount().subtract(newPaidAmount).max(BigDecimal.ZERO));
            if (newPaidAmount.compareTo(period.getExpectedAmount()) >= 0) {
                period.setMinPaymentCriteriaMet(true);
                period.setDelinquentAmount(BigDecimal.ZERO);
                period.setDelinquentDays(0L);
            }
            loanDelinquencyRangeScheduleRepository.saveAndFlush(period);
            log.debug("Applied repayment of {} to delinquency range schedule period {} for WC loan {}", amount, period.getPeriodNumber(),
                    loanId);
        }
    }

    @Override
    public void evaluateExpiredPeriods(WorkingCapitalLoan loan, LocalDate businessDate) {
        List<WorkingCapitalLoanDelinquencyRangeSchedule> unevaluatedPeriods = loanDelinquencyRangeScheduleRepository
                .findByLoanIdAndToDateLessThanEqualAndMinPaymentCriteriaMetIsNull(loan.getId(), businessDate);
        for (WorkingCapitalLoanDelinquencyRangeSchedule period : unevaluatedPeriods) {
            boolean criteriaMet = period.getPaidAmount().compareTo(period.getExpectedAmount()) >= 0;
            period.setMinPaymentCriteriaMet(criteriaMet);
            loanDelinquencyRangeScheduleRepository.saveAndFlush(period);
            log.debug("Evaluated delinquency range schedule period {} for WC loan {}: criteriaMet={}", period.getPeriodNumber(),
                    loan.getId(), criteriaMet);
        }
    }

    @Override
    public List<WorkingCapitalLoanDelinquencyRangeScheduleData> retrieveRangeSchedule(Long loanId) {
        List<WorkingCapitalLoanDelinquencyRangeSchedule> periods = loanDelinquencyRangeScheduleRepository
                .findByLoanIdOrderByPeriodNumberAsc(loanId);
        return capitalLoanDelinquencyRangeScheduleMapper.toDataList(periods);
    }

    private DelinquencyMinimumPaymentPeriodAndRule getMinimumPaymentRule(WorkingCapitalLoan loan) {
        WorkingCapitalLoanProduct product = loan.getLoanProduct();
        if (product == null) {
            return null;
        }
        DelinquencyBucket bucket = product.getDelinquencyBucket();
        if (bucket == null) {
            return null;
        }
        return minimumPaymentPeriodAndRuleRepository.findByBucketId(bucket.getId()).orElse(null);
    }

    private LocalDate calculateToDate(LocalDate fromDate, Integer frequency, DelinquencyFrequencyType frequencyType) {
        return switch (frequencyType) {
            case DAYS -> fromDate.plusDays(frequency - 1);
            case WEEKS -> fromDate.plusWeeks(frequency).minusDays(1);
            case MONTHS -> fromDate.plusMonths(frequency).minusDays(1);
            case YEARS -> fromDate.plusYears(frequency).minusDays(1);
        };
    }

    private BigDecimal calculateExpectedAmount(final WorkingCapitalLoan loan, final DelinquencyMinimumPaymentPeriodAndRule rule,
            final WorkingCapitalLoanDelinquencyAction rescheduleOverride) {
        final BigDecimal principal = loan.getApprovedPrincipal();
        if (principal == null) {
            return BigDecimal.ZERO;
        }

        final BigDecimal effectiveMinimumPayment = resolveMinimumPayment(rescheduleOverride, rule);
        final DelinquencyMinimumPaymentType effectivePaymentType = resolveMinimumPaymentType(rescheduleOverride, rule);
        if (effectiveMinimumPayment == null) {
            return BigDecimal.ZERO;
        }

        final BigDecimal rawAmount;
        if (DelinquencyMinimumPaymentType.FLAT.equals(effectivePaymentType)) {
            rawAmount = effectiveMinimumPayment;
        } else {
            final BigDecimal discount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getDiscount()
                    : null;
            final BigDecimal base = discount != null ? principal.add(discount) : principal;
            rawAmount = MathUtil.percentageOf(base, effectiveMinimumPayment, MoneyHelper.getMathContext());
        }
        return Money.of(loan.getLoanProductRelatedDetails().getCurrency(), rawAmount).getAmount();
    }

    private BigDecimal resolveMinimumPayment(final WorkingCapitalLoanDelinquencyAction rescheduleOverride,
            final DelinquencyMinimumPaymentPeriodAndRule rule) {
        if (rescheduleOverride != null && rescheduleOverride.getMinimumPayment() != null) {
            return rescheduleOverride.getMinimumPayment();
        }
        return rule != null ? rule.getMinimumPayment() : null;
    }

    private DelinquencyMinimumPaymentType resolveMinimumPaymentType(final WorkingCapitalLoanDelinquencyAction rescheduleOverride,
            final DelinquencyMinimumPaymentPeriodAndRule rule) {
        if (rescheduleOverride != null && rescheduleOverride.getMinimumPaymentType() != null) {
            return rescheduleOverride.getMinimumPaymentType();
        }
        return rule != null && rule.getMinimumPaymentType() != null ? rule.getMinimumPaymentType()
                : DelinquencyMinimumPaymentType.PERCENTAGE;
    }

    private Optional<WorkingCapitalLoanDelinquencyAction> findLatestRescheduleAction(final Long loanId) {
        return loanDelinquencyActionRepository.findTopByWorkingCapitalLoanIdAndActionOrderByIdDesc(loanId, DelinquencyAction.RESCHEDULE);
    }

    @Override
    public void rescheduleMinimumPayment(final WorkingCapitalLoan loan, final WorkingCapitalLoanDelinquencyAction rescheduleAction) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        final DelinquencyMinimumPaymentPeriodAndRule rule = getMinimumPaymentRule(loan);
        if (rule == null) {
            log.warn("No minimum payment rule found for WC loan {}, skipping reschedule", loan.getId());
            return;
        }
        final BigDecimal newExpectedAmount = calculateExpectedAmount(loan, rule, rescheduleAction);
        final Integer newFrequency = rescheduleAction.getFrequency() != null ? rescheduleAction.getFrequency() : rule.getFrequency();
        final DelinquencyFrequencyType newFreqType = rescheduleAction.getFrequencyType() != null ? rescheduleAction.getFrequencyType()
                : rule.getFrequencyType();

        final List<WorkingCapitalLoanDelinquencyRangeSchedule> periods = loanDelinquencyRangeScheduleRepository
                .findByLoanIdOrderByPeriodNumberAsc(loan.getId());

        WorkingCapitalLoanDelinquencyRangeSchedule currentPeriod = null;
        final List<WorkingCapitalLoanDelinquencyRangeSchedule> futurePeriods = new ArrayList<>();

        for (final WorkingCapitalLoanDelinquencyRangeSchedule period : periods) {
            if (period.getMinPaymentCriteriaMet() != null) {
                continue;
            }
            final boolean isCurrent = !period.getFromDate().isAfter(businessDate) && !period.getToDate().isBefore(businessDate);
            final boolean isFuture = period.getFromDate().isAfter(businessDate);

            if (isCurrent) {
                currentPeriod = period;
                period.setExpectedAmount(newExpectedAmount);
                period.setOutstandingAmount(newExpectedAmount.subtract(period.getPaidAmount()).max(BigDecimal.ZERO));
            } else if (isFuture) {
                futurePeriods.add(period);
            }
        }

        if (currentPeriod != null) {
            loanDelinquencyRangeScheduleRepository.saveAndFlush(currentPeriod);
            updateFuturePeriods(currentPeriod, futurePeriods, newExpectedAmount, newFrequency, newFreqType);
        }

        evaluateExpiredPeriods(loan, businessDate);

        log.debug("Rescheduled delinquency range schedule for WC loan {}: new minimumPayment={}%, frequency={} {}", loan.getId(),
                rescheduleAction.getMinimumPayment(), newFrequency, newFreqType);
    }

    private void updateFuturePeriods(final WorkingCapitalLoanDelinquencyRangeSchedule currentPeriod,
            final List<WorkingCapitalLoanDelinquencyRangeSchedule> existingFuturePeriods, final BigDecimal expectedAmount,
            final Integer frequency, final DelinquencyFrequencyType frequencyType) {
        int periodNumber = currentPeriod.getPeriodNumber();
        LocalDate fromDate = currentPeriod.getToDate().plusDays(1);

        for (final WorkingCapitalLoanDelinquencyRangeSchedule period : existingFuturePeriods) {
            final LocalDate toDate = calculateToDate(fromDate, frequency, frequencyType);
            periodNumber++;

            period.setPeriodNumber(periodNumber);
            period.setFromDate(fromDate);
            period.setToDate(toDate);
            period.setExpectedAmount(expectedAmount);
            period.setPaidAmount(BigDecimal.ZERO);
            period.setOutstandingAmount(expectedAmount);
            period.setMinPaymentCriteriaMet(null);

            fromDate = toDate.plusDays(1);
        }
        loanDelinquencyRangeScheduleRepository.saveAll(existingFuturePeriods);
    }

    @Override
    public void extendPeriodsForPause(final WorkingCapitalLoan loan, final LocalDate pauseStart, final LocalDate pauseEnd) {
        final long pauseDays = ChronoUnit.DAYS.between(pauseStart, pauseEnd);
        List<WorkingCapitalLoanDelinquencyRangeSchedule> periods = loanDelinquencyRangeScheduleRepository
                .findByLoanIdOrderByPeriodNumberAsc(loan.getId());
        for (WorkingCapitalLoanDelinquencyRangeSchedule period : periods) {
            if (period.getMinPaymentCriteriaMet() != null) {
                continue;
            }
            if (!period.getToDate().isBefore(pauseStart)) {
                period.setToDate(period.getToDate().plusDays(pauseDays));
            }
            if (period.getFromDate().isAfter(pauseStart)) {
                period.setFromDate(period.getFromDate().plusDays(pauseDays));
            }
        }
        loanDelinquencyRangeScheduleRepository.saveAll(periods);
        log.debug("Extended delinquency range schedule periods for WC loan {} by {} days due to pause [{} - {}]", loan.getId(), pauseDays,
                pauseStart, pauseEnd);
    }

}
