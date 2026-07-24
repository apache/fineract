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
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.TransactionDateAndAmountHolder;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanDelinquencyRangeScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyPauseUtils;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanDelinquencyRangeScheduleMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanDelinquencyStartType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class WorkingCapitalLoanDelinquencyRangeScheduleServiceImpl implements WorkingCapitalLoanDelinquencyRangeScheduleService {

    private final WorkingCapitalLoanDelinquencyRangeScheduleRepository loanDelinquencyRangeScheduleRepository;
    private final WorkingCapitalLoanDelinquencyActionRepository loanDelinquencyActionRepository;
    private final WorkingCapitalLoanDelinquencyRangeScheduleMapper capitalLoanDelinquencyRangeScheduleMapper;
    private final DelinquencyMinimumPaymentPeriodAndRuleRepository minimumPaymentPeriodAndRuleRepository;
    private final WorkingCapitalLoanDelinquencyClassificationService delinquencyClassificationService;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;

    @Override
    public void generateInitialPeriod(WorkingCapitalLoan loan) {
        DelinquencyMinimumPaymentPeriodAndRule rule = getMinimumPaymentRule(loan);
        if (rule == null) {
            return;
        }

        LocalDate fromDate = resolveScheduleAnchorDate(loan);
        if (fromDate == null) {
            log.warn("No anchor date found for WC loan {}, skipping initial period generation", loan.getId());
            return;
        }

        final EffectiveDelinquencyRescheduleParams params = resolveEffectiveRescheduleParams(loan.getId(), rule);
        final LocalDate toDate = calculateToDate(fromDate, params.frequency(), params.frequencyType());
        final WorkingCapitalLoanDelinquencyRangeSchedule period = buildPeriod(loan, 1, fromDate, toDate,
                calculateExpectedAmount(loan, params));

        loanDelinquencyRangeScheduleRepository.saveAndFlush(period);
        log.debug("Generated initial delinquency range schedule period for WC loan {}", loan.getId());
    }

    @Override
    public boolean hasSchedule(Long loanId) {
        return loanDelinquencyRangeScheduleRepository.findTopByLoanIdOrderByPeriodNumberDesc(loanId).isPresent();
    }

    @Override
    public List<WorkingCapitalLoanDelinquencyRangeSchedule> generateNextPeriodIfNeeded(WorkingCapitalLoan loan, LocalDate businessDate) {
        List<WorkingCapitalLoanDelinquencyRangeSchedule> result = new ArrayList<>();
        final DelinquencyMinimumPaymentPeriodAndRule rule = getMinimumPaymentRule(loan);
        if (rule == null) {
            return result;
        }

        final Optional<WorkingCapitalLoanDelinquencyRangeSchedule> latestPeriodOpt = loanDelinquencyRangeScheduleRepository
                .findTopByLoanIdOrderByPeriodNumberDesc(loan.getId());
        if (latestPeriodOpt.isEmpty() || latestPeriodOpt.get().getToDate().isAfter(businessDate)) {
            return result;
        }

        final EffectiveDelinquencyRescheduleParams params = resolveEffectiveRescheduleParams(loan.getId(), rule);
        final BigDecimal expectedAmount = calculateExpectedAmount(loan, params);

        WorkingCapitalLoanDelinquencyRangeSchedule latestPeriod = latestPeriodOpt.get();
        while (!latestPeriod.getToDate().isAfter(businessDate)) {
            final LocalDate newFromDate = latestPeriod.getToDate().plusDays(1);
            final LocalDate newToDate = calculateToDate(newFromDate, params.frequency(), params.frequencyType());
            final WorkingCapitalLoanDelinquencyRangeSchedule nextPeriod = buildPeriod(loan, latestPeriod.getPeriodNumber() + 1, newFromDate,
                    newToDate, expectedAmount);
            applyRecordedPauses(nextPeriod, loan);

            latestPeriod = loanDelinquencyRangeScheduleRepository.saveAndFlush(nextPeriod);
            result.add(latestPeriod);
            log.debug("Generated next delinquency range schedule period {} for WC loan {}", nextPeriod.getPeriodNumber(), loan.getId());
        }
        return result;
    }

    @Override
    public void applyRepayment(WorkingCapitalLoan loan, LocalDate transactionDate, BigDecimal amount) {
        allocateRepayment(loan, transactionDate, amount);
        applyRemainingBalanceCap(loan);
        delinquencyClassificationService.instantClassifyDelinquency(loan, transactionDate);
    }

    @Override
    public void reprocessDelinquencySchedule(final WorkingCapitalLoan loan) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        generateNextPeriodIfNeeded(loan, businessDate);
        List<WorkingCapitalLoanDelinquencyRangeSchedule> delinquencyRangePeriods = resetAllPeriodsForReprocessing(loan.getId());

        final List<TransactionDateAndAmountHolder> transactionDateAndAmountHolderList = transactionRepository
                .fetchTransactionDateAndAmount(loan.getId(), LoanTransactionType.getRepaymentLikeTransactionTypes());
        if (!transactionDateAndAmountHolderList.isEmpty()) {
            List<TransactionDateAndAmountHolder> remappedTransactionDateAndAmountHolderList = new ArrayList<>();
            delinquencyRangePeriods.forEach(period -> {
                BigDecimal sumAmount = transactionDateAndAmountHolderList.stream().parallel().filter(
                        holder -> DateUtils.isDateInRangeInclusive(holder.transactionDate(), period.getFromDate(), period.getToDate()))
                        .map(TransactionDateAndAmountHolder::transactionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                remappedTransactionDateAndAmountHolderList.add(new TransactionDateAndAmountHolder(period.getToDate(), sumAmount));
            });

            remappedTransactionDateAndAmountHolderList.forEach(transactionDateAndAmountHolder -> {
                allocateRepayment(loan, transactionDateAndAmountHolder.transactionDate(),
                        transactionDateAndAmountHolder.transactionAmount());
            });
        }

        // One cap pass suffices here: the cascade pays oldest-first, so only the trailing period(s) can
        // still exceed the balance once the whole replay is done.
        applyRemainingBalanceCap(loan);
        evaluateExpiredPeriods(loan, businessDate);
        delinquencyClassificationService.classifyDelinquency(loan, businessDate);
    }

    private List<WorkingCapitalLoanDelinquencyRangeSchedule> resetAllPeriodsForReprocessing(final Long loanId) {
        final List<WorkingCapitalLoanDelinquencyRangeSchedule> periods = loanDelinquencyRangeScheduleRepository
                .findByLoanIdOrderByPeriodNumberAsc(loanId);
        for (final WorkingCapitalLoanDelinquencyRangeSchedule period : periods) {
            // Reset to the uncapped base first so the cap re-derives cleanly from the replayed balance
            // (null only for periods created before this column existed).
            if (period.getBaseExpectedAmount() != null) {
                period.setExpectedAmount(period.getBaseExpectedAmount());
            }
            period.setPaidAmount(period.getReset() ? null : BigDecimal.ZERO);
            period.setOutstandingAmount(period.getExpectedAmount());
            period.setMinPaymentCriteriaMet(null);
            period.setDelinquentAmount(null);
            period.setDelinquentDays(null);
        }
        return periods;
    }

    private void allocateRepayment(final WorkingCapitalLoan loan, final LocalDate transactionDate, final BigDecimal amount) {
        final Long loanId = loan.getId();
        // While delinquency evaluation is disabled the payment bookkeeping (paid/outstanding/criteria met) still
        // happens, but the delinquency data (delinquentAmount/delinquentDays) is frozen; it is recomputed when the
        // disable is reversed.
        final boolean delinquencyDisabled = delinquencyClassificationService.isDelinquencyDisabled(loan, transactionDate);
        List<WorkingCapitalLoanDelinquencyRangeSchedule> pastOpenPeriods = loanDelinquencyRangeScheduleRepository
                .findPastOpenPeriodsForRepayment(loanId, transactionDate);
        Optional<WorkingCapitalLoanDelinquencyRangeSchedule> currentPeriod = loanDelinquencyRangeScheduleRepository
                .findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(loanId, transactionDate, transactionDate);
        BigDecimal transactionAmount = amount;
        for (WorkingCapitalLoanDelinquencyRangeSchedule period : pastOpenPeriods) {
            BigDecimal payAmount = MathUtil.min(transactionAmount, period.getOutstandingAmount(), true);
            transactionAmount = transactionAmount.subtract(payAmount);
            period.setPaidAmount(MathUtil.nullToZero(period.getPaidAmount()).add(payAmount));
            period.setOutstandingAmount(MathUtil.nullToZero(period.getOutstandingAmount()).subtract(payAmount));
            if (period.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
                period.setMinPaymentCriteriaMet(true);
                if (!delinquencyDisabled) {
                    period.setDelinquentAmount(BigDecimal.ZERO);
                    period.setDelinquentDays(0L);
                }
            }
            loanDelinquencyRangeScheduleRepository.saveAndFlush(period);
            log.debug("Applied repayment of {} to delinquency range schedule period {} for WC loan {}", payAmount, period.getPeriodNumber(),
                    loanId);
            if (transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
        }
        if (currentPeriod.isPresent() && !currentPeriod.get().getReset()) {
            WorkingCapitalLoanDelinquencyRangeSchedule period = currentPeriod.get();
            BigDecimal newPaidAmount = MathUtil.nullToZero(period.getPaidAmount()).add(transactionAmount);
            period.setPaidAmount(newPaidAmount);
            period.setOutstandingAmount(MathUtil.nullToZero(period.getExpectedAmount()).subtract(newPaidAmount).max(BigDecimal.ZERO));
            if (newPaidAmount.compareTo(MathUtil.nullToZero(period.getExpectedAmount())) >= 0) {
                period.setMinPaymentCriteriaMet(true);
                if (!delinquencyDisabled) {
                    period.setDelinquentAmount(BigDecimal.ZERO);
                    period.setDelinquentDays(0L);
                }
            }
            loanDelinquencyRangeScheduleRepository.saveAndFlush(period);
            log.debug("Applied repayment of {} to delinquency range schedule period {} for WC loan {}", amount, period.getPeriodNumber(),
                    loanId);
        }
        // No classification here: applyRepayment classifies after the balance cap with the real transaction date, and
        // the reprocess replay classifies once at the end with the real business date. Classifying inside the replay
        // would use the synthetic per-period dates (transactions are remapped to the period toDate, possibly in the
        // future) and lift tags with a wrong date.
    }

    private BigDecimal unpayPeriod(WorkingCapitalLoanDelinquencyRangeSchedule period, BigDecimal transactionAmount, boolean isCurrentPeriod,
            boolean delinquencyDisabled) {
        BigDecimal unpayAmount = period.getPaidAmount().min(transactionAmount);
        period.setPaidAmount(period.getPaidAmount().subtract(unpayAmount));
        period.setOutstandingAmount(period.getExpectedAmount().subtract(period.getPaidAmount()).max(BigDecimal.ZERO));
        if (period.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0) {
            if (!isCurrentPeriod) {
                period.setMinPaymentCriteriaMet(false);
                if (!delinquencyDisabled) {
                    period.setDelinquentAmount(period.getOutstandingAmount());
                    period.setDelinquentDays(DateUtils.getDifferenceInDays(period.getToDate(), DateUtils.getBusinessLocalDate()));
                }
            } else {
                period.setMinPaymentCriteriaMet(null);
                if (!delinquencyDisabled) {
                    period.setDelinquentAmount(null);
                    period.setDelinquentDays(null);
                }
            }
        }
        return unpayAmount;
    }

    @Override
    public void applyRepaymentUndo(WorkingCapitalLoan loan, LocalDate businessDate, BigDecimal amount) {
        final Long loanId = loan.getId();
        // See applyRepayment: delinquency data is frozen while a delinquency disable is in effect.
        final boolean delinquencyDisabled = delinquencyClassificationService.isDelinquencyDisabled(loan, businessDate);
        Optional<WorkingCapitalLoanDelinquencyRangeSchedule> currentPeriod = loanDelinquencyRangeScheduleRepository
                .findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(loanId, businessDate, businessDate);
        BigDecimal transactionAmount = amount;
        if (currentPeriod.isPresent()) {
            WorkingCapitalLoanDelinquencyRangeSchedule period = currentPeriod.get();
            BigDecimal unpayAmount = unpayPeriod(period, transactionAmount, true, delinquencyDisabled);
            transactionAmount = transactionAmount.subtract(unpayAmount);
        }

        if (transactionAmount.compareTo(BigDecimal.ZERO) > 0) {
            // Unpay newest-first (the mirror of repayment, which pays oldest-first): the query is ordered by period
            // number ascending, so reversing it yields a deterministic newest-to-oldest iteration.
            List<WorkingCapitalLoanDelinquencyRangeSchedule> pastPeriods = loanDelinquencyRangeScheduleRepository
                    .findByLoanIdAndToDateIsBeforeOrderByPeriodNumberAsc(loanId, businessDate);
            for (WorkingCapitalLoanDelinquencyRangeSchedule period : pastPeriods.reversed()) {
                BigDecimal unpayAmount = unpayPeriod(period, transactionAmount, false, delinquencyDisabled);
                transactionAmount = transactionAmount.subtract(unpayAmount);
                if (transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
            }
        }
        delinquencyClassificationService.instantClassifyDelinquency(loan, businessDate);
    }

    @Override
    public void evaluateExpiredPeriods(WorkingCapitalLoan loan, LocalDate businessDate) {
        List<WorkingCapitalLoanDelinquencyRangeSchedule> unevaluatedPeriods = loanDelinquencyRangeScheduleRepository
                .findByLoanIdAndToDateLessThanEqualAndMinPaymentCriteriaMetIsNull(loan.getId(), businessDate);
        for (WorkingCapitalLoanDelinquencyRangeSchedule period : unevaluatedPeriods) {
            if (period.getReset()) {
                continue;
            }
            capPeriodToRemainingBalance(period, loan);
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

    @Override
    public void rescheduleMinimumPayment(final WorkingCapitalLoan loan) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        final DelinquencyMinimumPaymentPeriodAndRule rule = getMinimumPaymentRule(loan);
        if (rule == null) {
            log.warn("No minimum payment rule found for WC loan {}, skipping reschedule", loan.getId());
            return;
        }
        final EffectiveDelinquencyRescheduleParams params = resolveEffectiveRescheduleParams(loan.getId(), rule);
        final BigDecimal newExpectedAmount = calculateExpectedAmount(loan, params);

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
                period.setBaseExpectedAmount(newExpectedAmount);
            } else if (isFuture) {
                futurePeriods.add(period);
            }
        }

        if (currentPeriod != null) {
            loanDelinquencyRangeScheduleRepository.saveAndFlush(currentPeriod);
            updateFuturePeriods(currentPeriod, futurePeriods, newExpectedAmount, params.frequency(), params.frequencyType());
        }

        log.debug("Rescheduled delinquency range schedule for WC loan {}: new minimumPayment={} {}, frequency={} {}", loan.getId(),
                params.minimumPayment(), params.minimumPaymentType(), params.frequency(), params.frequencyType());
    }

    @Override
    public void resumeActivePause(final WorkingCapitalLoan loan, final WorkingCapitalLoanDelinquencyAction activePause,
            final WorkingCapitalLoanDelinquencyAction resumeAction) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        final LocalDate originalPauseEnd = activePause.getEndDate();
        final LocalDate resumeDate = resumeAction.getStartDate();

        final WorkingCapitalLoanDelinquencyAction saved = loanDelinquencyActionRepository.saveAndFlush(activePause);
        log.debug("Resumed WC loan delinquency pause {} for loan {}: shortened pause end from {} to {}", saved.getId(), loan.getId(),
                originalPauseEnd, resumeDate);

        shrinkPeriodsForPause(loan, activePause.getStartDate(), originalPauseEnd, resumeDate);
        recalculateDelinquencyAfterPauseResume(loan, businessDate);
    }

    @Override
    public void resetPeriods(WorkingCapitalLoan loan, WorkingCapitalLoanDelinquencyAction action) {
        LocalDate resetDate = action.getStartDate();

        final List<WorkingCapitalLoanDelinquencyRangeSchedule> periods = loanDelinquencyRangeScheduleRepository
                .findByLoanIdOrderByPeriodNumberAsc(loan.getId());
        if (action.getStartNewPeriod() != null && action.getStartNewPeriod()
                && !periods.getLast().getFromDate().isEqual(action.getStartDate())) {
            periods.getLast().setToDate(action.getStartDate().minusDays(1));
            List<WorkingCapitalLoanDelinquencyRangeSchedule> newPeriods = generateNextPeriodIfNeeded(loan, action.getStartDate());
            periods.addAll(newPeriods);
        }

        periods.stream().filter(p -> !Objects.equals(p.getReset(), true) && p.getToDate().isBefore(action.getStartDate()))
                .forEach(p1 -> resetPeriod(p1, resetDate));
    }

    private void resetPeriod(WorkingCapitalLoanDelinquencyRangeSchedule period, LocalDate resetDate) {
        period.reset();
        delinquencyClassificationService.applyDelinquencyTagForRange(period.getLoan(), period, null, resetDate);
    }

    @Override
    public void undoResetPeriods(WorkingCapitalLoan loan, WorkingCapitalLoanDelinquencyAction action,
            List<WorkingCapitalLoanDelinquencyAction> byWorkingCapitalLoanIdOrderById) {

        List<WorkingCapitalLoanDelinquencyAction> activeResets = byWorkingCapitalLoanIdOrderById.stream()
                .filter(a -> DelinquencyAction.RESET.equals(a.getAction()) && a.getEndDate() == null).toList();
        if (!activeResets.isEmpty()) {
            activeResets.getLast().setEndDate(action.getStartDate());
        }
        LocalDate lastActiveResetStartDate = activeResets.size() >= 2 ? activeResets.get(activeResets.size() - 2).getStartDate() : null;

        if (lastActiveResetStartDate == null) {
            loanDelinquencyRangeScheduleRepository.clearResetBeforeActionStartDate(loan.getId(), action.getStartDate());
        } else {
            loanDelinquencyRangeScheduleRepository.clearResetBeforeActionStartDateFromLastActiveReset(loan.getId(), action.getStartDate(),
                    lastActiveResetStartDate);
        }
        reprocessDelinquencySchedule(loan);
    }

    private void shrinkPeriodsForPause(final WorkingCapitalLoan loan, final LocalDate pauseStart, final LocalDate originalPauseEnd,
            final LocalDate newPauseEnd) {
        final long daysToRemove = WorkingCapitalLoanDelinquencyPauseUtils.calculateDaysRemovedOnResume(pauseStart, newPauseEnd,
                originalPauseEnd);
        if (daysToRemove <= 0) {
            return;
        }
        final List<WorkingCapitalLoanDelinquencyRangeSchedule> periods = loanDelinquencyRangeScheduleRepository
                .findByLoanIdOrderByPeriodNumberAsc(loan.getId());
        for (final WorkingCapitalLoanDelinquencyRangeSchedule period : periods) {
            if (period.getMinPaymentCriteriaMet() != null) {
                continue;
            }
            if (!period.getToDate().isBefore(pauseStart)) {
                period.setToDate(period.getToDate().minusDays(daysToRemove));
            }
            if (period.getFromDate().isAfter(pauseStart)) {
                period.setFromDate(period.getFromDate().minusDays(daysToRemove));
            }
        }
        loanDelinquencyRangeScheduleRepository.saveAll(periods);
        log.debug("Shortened delinquency range schedule periods for WC loan {} by {} days due to pause resume [{} - {} -> {}]",
                loan.getId(), daysToRemove, pauseStart, originalPauseEnd, newPauseEnd);
    }

    private void recalculateDelinquencyAfterPauseResume(final WorkingCapitalLoan loan, final LocalDate businessDate) {
        evaluateExpiredPeriods(loan, businessDate);
        delinquencyClassificationService.classifyDelinquency(loan, businessDate);
    }

    /**
     * Resolves the date the delinquency clock starts ticking, based on the loan's configured
     * {@link WorkingCapitalLoanDelinquencyStartType}.
     *
     * <ul>
     * <li>{@code LOAN_CREATION}: the loan submitted-on date is used as the basis.</li>
     * <li>{@code DISBURSEMENT} (or unset): the first actual disbursement date is used as the basis.</li>
     * </ul>
     *
     * The configured {@code delinquencyGraceDays} are not applied here; they are added when the derived
     * {@code delinquencyStartDate} is computed at read time.
     */
    private LocalDate resolveScheduleAnchorDate(final WorkingCapitalLoan loan) {
        final WorkingCapitalLoanProductRelatedDetails details = loan.getLoanProductRelatedDetails();
        final WorkingCapitalLoanDelinquencyStartType startType = details != null ? details.getDelinquencyStartType() : null;
        if (WorkingCapitalLoanDelinquencyStartType.LOAN_CREATION.equals(startType)) {
            return loan.getSubmittedOnDate();
        }
        return loan.getDisbursementDetails().stream().map(WorkingCapitalLoanDisbursementDetails::getActualDisbursementDate)
                .filter(Objects::nonNull).findFirst().orElse(null);
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

    private BigDecimal calculateExpectedAmount(final WorkingCapitalLoan loan, final EffectiveDelinquencyRescheduleParams params) {
        final BigDecimal principal = loan.getApprovedPrincipal();
        if (principal == null || params.minimumPayment() == null) {
            return BigDecimal.ZERO;
        }

        final BigDecimal rawAmount;
        if (DelinquencyMinimumPaymentType.FLAT.equals(params.minimumPaymentType())) {
            rawAmount = params.minimumPayment();
        } else {
            final BigDecimal discount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getDiscount()
                    : null;
            final BigDecimal base = discount != null ? principal.add(discount) : principal;
            rawAmount = MathUtil.percentageOf(base, params.minimumPayment(), MoneyHelper.getMathContext());
        }
        return Money.of(loan.getLoanProductRelatedDetails().getCurrency(), rawAmount).getAmount();
    }

    private WorkingCapitalLoanDelinquencyRangeSchedule buildPeriod(final WorkingCapitalLoan loan, final int periodNumber,
            final LocalDate fromDate, final LocalDate toDate, final BigDecimal expectedAmount) {
        final WorkingCapitalLoanDelinquencyRangeSchedule period = new WorkingCapitalLoanDelinquencyRangeSchedule();
        period.setLoan(loan);
        period.setPeriodNumber(periodNumber);
        period.setFromDate(fromDate);
        period.setToDate(toDate);
        resetPeriodExpectation(period, expectedAmount);
        capPeriodToRemainingBalance(period, loan);
        return period;
    }

    private void resetPeriodExpectation(final WorkingCapitalLoanDelinquencyRangeSchedule period, final BigDecimal expectedAmount) {
        period.setBaseExpectedAmount(expectedAmount);
        period.setExpectedAmount(expectedAmount);
        period.setPaidAmount(BigDecimal.ZERO);
        period.setOutstandingAmount(expectedAmount);
        period.setMinPaymentCriteriaMet(null);
    }

    private BigDecimal principalOutstanding(final WorkingCapitalLoan loan) {
        final WorkingCapitalLoanBalance balance = loan.getBalance();
        return balance != null ? balance.getPrincipalOutstanding() : null;
    }

    private void capPeriodToRemainingBalance(final WorkingCapitalLoanDelinquencyRangeSchedule period, final WorkingCapitalLoan loan) {
        final BigDecimal principalOutstanding = principalOutstanding(loan);
        if (principalOutstanding != null) {
            capPeriodToRemainingBalance(period, principalOutstanding);
        }
    }

    private void applyRemainingBalanceCap(final WorkingCapitalLoan loan) {
        final BigDecimal principalOutstanding = principalOutstanding(loan);
        if (principalOutstanding == null) {
            return;
        }
        final List<WorkingCapitalLoanDelinquencyRangeSchedule> cappedPeriods = loanDelinquencyRangeScheduleRepository
                .findOpenPeriodsExceedingRemainingBalanceCap(loan.getId(), principalOutstanding);
        if (cappedPeriods.isEmpty()) {
            return;
        }
        cappedPeriods.forEach(period -> capPeriodToRemainingBalance(period, principalOutstanding));
        loanDelinquencyRangeScheduleRepository.saveAll(cappedPeriods);
    }

    /**
     * Shrinks the period's expected amount to at most what the customer can still owe against it
     * ({@code paidAmount + principalOutstanding}). Reaching zero outstanding this way counts as the minimum payment
     * criteria being met (the remaining balance is fully paid) - including for a period the repayment cascade skipped
     * because the debt ran out before reaching it.
     */
    private boolean capPeriodToRemainingBalance(final WorkingCapitalLoanDelinquencyRangeSchedule period,
            final BigDecimal principalOutstanding) {
        if (Boolean.TRUE.equals(period.getMinPaymentCriteriaMet()) || period.getExpectedAmount() == null) {
            return false;
        }
        final BigDecimal paidAmount = MathUtil.nullToZero(period.getPaidAmount());
        final BigDecimal cap = paidAmount.add(principalOutstanding);
        if (period.getExpectedAmount().compareTo(cap) <= 0) {
            return false;
        }
        period.setExpectedAmount(cap);
        final BigDecimal outstanding = cap.subtract(paidAmount).max(BigDecimal.ZERO);
        period.setOutstandingAmount(outstanding);
        if (outstanding.signum() == 0) {
            period.setMinPaymentCriteriaMet(true);
            period.setDelinquentAmount(BigDecimal.ZERO);
            period.setDelinquentDays(0L);
        }
        return true;
    }

    private EffectiveDelinquencyRescheduleParams resolveEffectiveRescheduleParams(final Long loanId,
            final DelinquencyMinimumPaymentPeriodAndRule rule) {
        final List<WorkingCapitalLoanDelinquencyAction> reschedules = loanDelinquencyActionRepository
                .findByWorkingCapitalLoanIdAndActionOrderByIdDesc(loanId, DelinquencyAction.RESCHEDULE);
        final Optional<WorkingCapitalLoanDelinquencyAction> latestWithPayment = reschedules.stream()
                .filter(action -> action.getMinimumPayment() != null).findFirst();
        final Optional<WorkingCapitalLoanDelinquencyAction> latestWithFrequency = reschedules.stream()
                .filter(action -> action.getFrequency() != null).findFirst();

        return new EffectiveDelinquencyRescheduleParams(
                latestWithPayment.map(WorkingCapitalLoanDelinquencyAction::getMinimumPayment).orElse(rule.getMinimumPayment()),
                latestWithPayment.map(WorkingCapitalLoanDelinquencyAction::getMinimumPaymentType)
                        .or(() -> Optional.ofNullable(rule.getMinimumPaymentType())).orElse(DelinquencyMinimumPaymentType.PERCENTAGE),
                latestWithFrequency.map(WorkingCapitalLoanDelinquencyAction::getFrequency).orElse(rule.getFrequency()),
                latestWithFrequency.map(WorkingCapitalLoanDelinquencyAction::getFrequencyType).orElse(rule.getFrequencyType()));
    }

    private List<WorkingCapitalLoanDelinquencyAction> findAllActions(final Long loanId) {
        return loanDelinquencyActionRepository.findByWorkingCapitalLoanIdOrderById(loanId);
    }

    private void applyRecordedPauses(final WorkingCapitalLoanDelinquencyRangeSchedule period, final WorkingCapitalLoan loan) {
        final List<WorkingCapitalLoanDelinquencyAction> recordedActions = findAllActions(loan.getId());
        if (period == null || recordedActions == null || recordedActions.isEmpty()) {
            return;
        }
        final LocalDate periodFromDate = period.getFromDate();
        final LocalDate periodToDate = period.getToDate();
        if (periodFromDate == null || periodToDate == null) {
            return;
        }
        // Only pauses shift period dates. A delinquency disable is NOT treated as a pause (matching breach): while
        // disabled, evaluation is suppressed, but the disabled days are not excluded from the period dates.
        final List<WorkingCapitalLoanDelinquencyAction> pauseActions = recordedActions.stream()
                .filter(action -> action != null && DelinquencyAction.PAUSE.equals(action.getAction())).toList();
        for (final WorkingCapitalLoanDelinquencyAction pause : pauseActions) {
            final LocalDate pauseStart = pause.getStartDate();
            final LocalDate pauseEnd = WorkingCapitalLoanDelinquencyPauseUtils.resolveEffectivePauseEnd(pause, recordedActions);
            if (pauseStart == null || pauseEnd == null || pauseEnd.isBefore(pauseStart)) {
                continue;
            }
            // Apply only if the pause overlaps this period's date range
            if (!pauseEnd.isBefore(periodFromDate) && !pauseStart.isAfter(periodToDate)) {
                final long pauseDays = WorkingCapitalLoanDelinquencyPauseUtils.calculatePauseExtensionDays(pauseStart, pauseEnd);
                period.setToDate(period.getToDate().plusDays(pauseDays));
                if (period.getFromDate().isAfter(pauseStart)) {
                    period.setFromDate(period.getFromDate().plusDays(pauseDays));
                }
            }
        }
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
            resetPeriodExpectation(period, expectedAmount);

            fromDate = toDate.plusDays(1);
        }
        loanDelinquencyRangeScheduleRepository.saveAll(existingFuturePeriods);
    }

    @Override
    public void extendPeriodsForPause(final WorkingCapitalLoan loan, final LocalDate pauseStart, final LocalDate pauseEnd) {
        final long pauseDays = WorkingCapitalLoanDelinquencyPauseUtils.calculatePauseExtensionDays(pauseStart, pauseEnd);
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
