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

import static org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants.ENABLE_INSTANT_DELINQUENCY_CALCULATION;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanDelinquencyRangeChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRange;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeScheduleTagHistory;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class WorkingCapitalLoanDelinquencyClassificationServiceImpl implements WorkingCapitalLoanDelinquencyClassificationService {

    private final WorkingCapitalLoanDelinquencyRangeScheduleRepository delinquencyRangeScheduleRepository;
    private final WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository delinquencyRangeScheduleTagHistoryRepository;
    private final WorkingCapitalLoanDelinquencyActionRepository delinquencyActionRepository;
    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final BusinessEventNotifierService businessEventNotifierService;

    /**
     * If ENABLE_INSTANT_DELINQUENCY_CALCULATION is set to true in global config, classifies the delinquency of a loan
     * based on the delinquency bucket and the business date.
     *
     * @param loan
     *            the loan for which the delinquency range tag should be applied
     * @param businessDate
     *            the date on which the tagging operation is performed
     */
    @Override
    public void instantClassifyDelinquency(WorkingCapitalLoan loan, LocalDate businessDate) {
        if (instantDelinquencyClassificationIsEnabled()) {
            classifyDelinquency(loan, businessDate);
        }
    }

    /**
     * Classifies the delinquency of a loan based on the delinquency bucket and the business date.
     *
     * @param loan
     *            the loan for which the delinquency range tag should be applied
     * @param businessDate
     *            the date on which the tagging operation is performed
     */
    @Override
    public void classifyDelinquency(WorkingCapitalLoan loan, LocalDate businessDate) {
        if (loan.getLoanProductRelatedDetails() == null || loan.getLoanProductRelatedDetails().getDelinquencyBucket() == null) {
            log.debug("Skipping... Delinquency bucket is not configured for Working Capital Loan {}.", loan.getId());
            return;
        }
        if (isDelinquencyDisabled(loan)) {
            log.debug("Skipping... Delinquency evaluation is disabled for Working Capital Loan {}.", loan.getId());
            return;
        }
        log.debug("Evaluate {} Working Capital Delinquency bucket", loan.getLoanProductRelatedDetails().getDelinquencyBucket());

        List<WorkingCapitalLoanDelinquencyRangeSchedule> delinquencyRangeScheduleList = delinquencyRangeScheduleRepository
                .findByLoanIdOrderByPeriodNumberAsc(loan.getId());

        boolean delinquencyRangeChanged = false;
        for (WorkingCapitalLoanDelinquencyRangeSchedule range : delinquencyRangeScheduleList) {
            if (!Objects.equals(range.getReset(), true) && range.getToDate().isBefore(businessDate)) {
                long rangeDelinquentDays = range.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0
                        ? DateUtils.getDifferenceInDays(range.getToDate(), businessDate)
                        : 0L;
                boolean isDelinquent = rangeDelinquentDays > 0;

                if (isDelinquent) {
                    range.setDelinquentAmount(range.getOutstandingAmount());
                    range.setDelinquentDays(rangeDelinquentDays);
                    Optional<DelinquencyRange> delinquencyRangeByDays = findDelinquencyRangeByDays(
                            loan.getLoanProductRelatedDetails().getDelinquencyBucket(), (int) rangeDelinquentDays);
                    // No matching range (gap day, or past a bounded last range): still replay ranges already left so
                    // tag history matches daily COB; the lift lands on each range's exit, not on businessDate.
                    delinquencyRangeChanged |= applyReachedDelinquencyTags(loan, range, delinquencyRangeByDays.orElse(null), businessDate);
                } else {
                    range.setDelinquentAmount(BigDecimal.ZERO);
                    range.setDelinquentDays(0L);
                    delinquencyRangeChanged |= applyDelinquencyTagForRange(loan, range, null, businessDate);
                }
            }
        }

        if (delinquencyRangeChanged) {
            delinquencyRangeScheduleTagHistoryRepository.flush();
            businessEventNotifierService.notifyPostBusinessEvent(new WorkingCapitalLoanDelinquencyRangeChangeBusinessEvent(loan));
        }
    }

    @Override
    public void classifyDelinquencyOnFinalState(final WorkingCapitalLoan loan, final WorkingCapitalLoanDelinquencyClassificationMode mode) {
        switch (mode) {
            case ALWAYS -> classifyDelinquency(loan, DateUtils.getBusinessLocalDate());
            case INSTANT -> {
                if (leftOutOfCobScope(loan)) {
                    // The COB does not process a closed or overpaid loan, so leaving the classification to it
                    // would keep the loan's tags as they were until a later transaction classifies it again.
                    classifyDelinquency(loan, DateUtils.getBusinessLocalDate());
                } else {
                    instantClassifyDelinquency(loan, DateUtils.getBusinessLocalDate());
                }
            }
            case NONE -> log.debug("Skipping... No delinquency classification is needed for Working Capital Loan {}.", loan.getId());
        }
    }

    private boolean leftOutOfCobScope(final WorkingCapitalLoan loan) {
        final LoanStatus status = loan.getLoanStatus();
        return status != null && (status.isClosedObligationsMet() || status.isOverpaid());
    }

    /**
     * Returns true while the loan has an active Delinquency Disable action (one that has not yet been reversed by an
     * enable). While disabled, delinquency is neither evaluated nor updated; the enable recalculates everything.
     */
    @Override
    public boolean isDelinquencyDisabled(final WorkingCapitalLoan loan) {
        return delinquencyActionRepository.isDelinquencyDisabled(loan.getId());
    }

    /**
     * Lifts every active delinquency classification tag of the loan as of the given date. Used when delinquency
     * evaluation is disabled: the loan must not remain classified while the disable is in effect; classification is
     * recomputed when the disable is reversed.
     */
    @Override
    public void liftDelinquencyClassification(final WorkingCapitalLoan loan, final LocalDate businessDate) {
        final List<WorkingCapitalLoanDelinquencyRangeScheduleTagHistory> activeTags = delinquencyRangeScheduleTagHistoryRepository
                .findByLoanIdAndLiftedOnDateIsNull(loan.getId());
        if (activeTags.isEmpty()) {
            return;
        }
        activeTags.forEach(tag -> tag.setLiftedOnDate(businessDate));
        delinquencyRangeScheduleTagHistoryRepository.saveAll(activeTags);
        log.debug("Lifted {} active delinquency classification tag(s) for WC loan {} as of {}", activeTags.size(), loan.getId(),
                businessDate);
    }

    public boolean instantDelinquencyClassificationIsEnabled() {
        return globalConfigurationRepository.findOneByNameWithNotFoundDetection(ENABLE_INSTANT_DELINQUENCY_CALCULATION).isEnabled();
    }

    /**
     * Finds the delinquency range for a given delinquency bucket and number of days.
     *
     * @param delinquencyBucket
     *            the delinquency bucket to search within
     * @param delinquentDays
     *            the number of days the loan is delinquent
     * @return an Optional containing the matching delinquency range, or empty if not found
     */
    private Optional<DelinquencyRange> findDelinquencyRangeByDays(final DelinquencyBucket delinquencyBucket, final Integer delinquentDays) {
        return delinquencyBucket.getRanges().stream().filter(dr -> dr.getMinimumAgeDays() <= delinquentDays)
                .filter(dr -> dr.getMaximumAgeDays() == null || dr.getMaximumAgeDays() >= delinquentDays).findAny();
    }

    /**
     * Tags the period with every range it has passed through since its classification was last known, each on the day
     * the period entered that range ({@code toDate + minimumAgeDays}). An evaluation that comes late - the first COB
     * long after disbursement, or an instant classification after days without COB - therefore leaves the same tag
     * history a daily COB would have. Days already known are never re-dated: see
     * {@link #resolveClassificationKnownUntil}.
     * <p>
     * Ranges may have gaps ({@code max + 1 < next.min}): daily COB lifts the tag on {@code toDate + maximumAgeDays + 1}
     * and leaves the gap days untagged. The replay does the same, so the tag chain breaks across the gap and
     * {@code delinquentDate} starts at the current range's entry - not at the earlier range. The same replay runs when
     * {@code currentRange} is null (evaluation day in a gap, or past a bounded last range): every range already entered
     * is written and lifted on its exit, so history still matches daily COB even though nothing is active on that day.
     */
    private boolean applyReachedDelinquencyTags(final WorkingCapitalLoan loan, final WorkingCapitalLoanDelinquencyRangeSchedule range,
            final DelinquencyRange currentRange, final LocalDate businessDate) {
        final LocalDate knownUntil = resolveClassificationKnownUntil(loan, range, businessDate);
        final List<DelinquencyRange> bucketRanges = loan.getLoanProductRelatedDetails().getDelinquencyBucket().getRanges().stream()
                .sorted(Comparator.comparing(DelinquencyRange::getMinimumAgeDays)).toList();
        final List<DelinquencyRange> reachedRanges = currentRange != null
                ? bucketRanges.stream().filter(dr -> dr.getMinimumAgeDays() <= currentRange.getMinimumAgeDays()).toList()
                : bucketRanges.stream().filter(dr -> !rangeEntryDate(range, dr).isAfter(businessDate)).toList();
        if (reachedRanges.isEmpty()) {
            return applyDelinquencyTagForRange(loan, range, null, businessDate);
        }

        boolean tagHistoryChanged = false;
        for (int i = 0; i < reachedRanges.size(); i++) {
            final DelinquencyRange delinquencyRange = reachedRanges.get(i);
            final boolean isCurrentRange = currentRange != null && i == reachedRanges.size() - 1;
            final LocalDate entryDate = rangeEntryDate(range, delinquencyRange);
            final LocalDate lastDayInRange = rangeLastDayInRange(range, delinquencyRange);
            final LocalDate exitDate = lastDayInRange == null ? null : lastDayInRange.plusDays(1);
            final boolean shouldLiftOnExit = exitDate != null && !isCurrentRange
                    && (i + 1 >= reachedRanges.size() || !exitDate.equals(rangeEntryDate(range, reachedRanges.get(i + 1))));

            final LocalDate tagDate = laterDate(entryDate, knownUntil);
            // Already left by knownUntil: do not re-tag it. If its own tag is still active and a gap follows it, lift
            // that tag on the exit, as daily COB did. Only this range's tag: a later range's tag may already be active.
            if (!isCurrentRange && exitDate != null && !tagDate.isBefore(exitDate)) {
                if (shouldLiftOnExit) {
                    tagHistoryChanged |= liftActiveTagOfRange(range, delinquencyRange, exitDate);
                }
                continue;
            }
            tagHistoryChanged |= applyDelinquencyTagForRange(loan, range, delinquencyRange, tagDate);

            // Left this range (gap before next, or past a bounded last with no current match): lift on exit, as daily
            // COB.
            if (shouldLiftOnExit) {
                tagHistoryChanged |= liftActiveTagOfRange(range, delinquencyRange, exitDate);
            }
        }
        if (currentRange == null) {
            // No range holds the period on businessDate, so nothing stays active (e.g. a tag of a range since removed
            // from the bucket), as with daily COB.
            tagHistoryChanged |= applyDelinquencyTagForRange(loan, range, null, businessDate);
        }
        return tagHistoryChanged;
    }

    /**
     * Lifts the period's active tag of the given range, leaving active tags of other ranges in place.
     *
     * @return true if a tag was lifted
     */
    private boolean liftActiveTagOfRange(final WorkingCapitalLoanDelinquencyRangeSchedule range, final DelinquencyRange delinquencyRange,
            final LocalDate liftedOnDate) {
        final List<WorkingCapitalLoanDelinquencyRangeScheduleTagHistory> activeTagsOfRange = delinquencyRangeScheduleTagHistoryRepository
                .findByRangeScheduleAndLiftedOnDateOrderByAddedOnDateAsc(range, null).stream()
                .filter(tag -> Objects.equals(tag.getDelinquencyRange().getId(), delinquencyRange.getId())).toList();
        if (activeTagsOfRange.isEmpty()) {
            return false;
        }
        activeTagsOfRange.forEach(tag -> tag.setLiftedOnDate(liftedOnDate));
        delinquencyRangeScheduleTagHistoryRepository.saveAll(activeTagsOfRange);
        return true;
    }

    /**
     * The last day the period's classification is already known: its last tag change, or the day the last COB
     * classified the loan as of ({@code lastClosedBusinessDate + 1}), whichever is later. No range can be entered
     * before {@code toDate + 1}, so the period end serves as the floor.
     * <p>
     * When the previous classification already covered {@code businessDate - 1}, nothing before {@code businessDate} is
     * left to replay, so the result is {@code businessDate} and the tag-history query is skipped. This is the daily COB
     * path: the step for COB date X classifies as of X + 1 while {@code lastClosedBusinessDate} is still X - 1 (it is
     * set after the business steps), so {@code lastClosedBusinessDate + 1} is {@code businessDate - 1}. It also covers
     * an instant classification after that COB, where {@code lastClosedBusinessDate + 1} is {@code businessDate}.
     */
    private LocalDate resolveClassificationKnownUntil(final WorkingCapitalLoan loan, final WorkingCapitalLoanDelinquencyRangeSchedule range,
            final LocalDate businessDate) {
        final LocalDate lastClassifiedByCob = loan.getLastClosedBusinessDate() == null ? null
                : loan.getLastClosedBusinessDate().plusDays(1);
        if (lastClassifiedByCob != null && !lastClassifiedByCob.isBefore(businessDate.minusDays(1))) {
            return businessDate;
        }
        final LocalDate knownUntil = Stream
                .of(range.getToDate(), delinquencyRangeScheduleTagHistoryRepository.findLatestTagChangeDate(range), lastClassifiedByCob)
                .filter(Objects::nonNull).max(Comparator.naturalOrder()).orElseThrow();
        return DateUtils.min(knownUntil, businessDate);
    }

    private static LocalDate rangeEntryDate(final WorkingCapitalLoanDelinquencyRangeSchedule range,
            final DelinquencyRange delinquencyRange) {
        return range.getToDate().plusDays(delinquencyRange.getMinimumAgeDays());
    }

    /**
     * Last calendar day the period still belongs to the range ({@code toDate + maximumAgeDays}), or {@code null} when
     * the range is open-ended.
     */
    private static LocalDate rangeLastDayInRange(final WorkingCapitalLoanDelinquencyRangeSchedule range,
            final DelinquencyRange delinquencyRange) {
        return delinquencyRange.getMaximumAgeDays() == null ? null : range.getToDate().plusDays(delinquencyRange.getMaximumAgeDays());
    }

    private static LocalDate laterDate(final LocalDate first, final LocalDate second) {
        return first.isAfter(second) ? first : second;
    }

    /**
     * Keeps at most one active delinquency tag on the range schedule period: every active tag of another range is
     * lifted on the given date, and a tag for the current range is added on the same date unless one is already active.
     * The active tag carries the period's delinquent amount.
     * <p>
     * Contract: an escalation lifts the previous tag on the same date the new tag is added. The read side derives a
     * period's delinquency start by walking this chain of lifted tags, so it relies on that date equality.
     *
     * @param loan
     *            the loan for which the delinquency range tag should be applied
     * @param range
     *            the delinquency range schedule associated with the tagging operation
     * @param currentRange
     *            the current delinquency range to be applied; can be null to lift all previous tags
     * @param businessDate
     *            the date on which the tagging operation is performed
     * @return true if the tag history was changed (a tag added or lifted), false if nothing changed or only the amount
     *         of the active tag was refreshed
     */
    @Override
    public boolean applyDelinquencyTagForRange(final WorkingCapitalLoan loan, final WorkingCapitalLoanDelinquencyRangeSchedule range,
            final DelinquencyRange currentRange, final LocalDate businessDate) {
        final List<WorkingCapitalLoanDelinquencyRangeScheduleTagHistory> activeTags = delinquencyRangeScheduleTagHistoryRepository
                .findByRangeScheduleAndLiftedOnDateOrderByAddedOnDateAsc(range, null);
        final Optional<WorkingCapitalLoanDelinquencyRangeScheduleTagHistory> activeTagOfCurrentRange = activeTags.stream()
                .filter(tag -> currentRange != null && Objects.equals(tag.getDelinquencyRange().getId(), currentRange.getId())).findFirst();

        final List<WorkingCapitalLoanDelinquencyRangeScheduleTagHistory> updatedList = new ArrayList<>(activeTags);
        activeTagOfCurrentRange.ifPresent(updatedList::remove);
        updatedList.forEach(tag -> tag.setLiftedOnDate(businessDate));
        final boolean liftedAnyTag = !updatedList.isEmpty();
        final boolean addedNewTag = currentRange != null && activeTagOfCurrentRange.isEmpty();

        if (currentRange != null) {
            final WorkingCapitalLoanDelinquencyRangeScheduleTagHistory tag = activeTagOfCurrentRange
                    .orElseGet(() -> newTag(loan, range, currentRange, businessDate));
            tag.setOutstandingAmount(range.getDelinquentAmount());
            updatedList.add(tag);
        }

        if (!updatedList.isEmpty()) {
            delinquencyRangeScheduleTagHistoryRepository.saveAll(updatedList);
        }
        return liftedAnyTag || addedNewTag;
    }

    private WorkingCapitalLoanDelinquencyRangeScheduleTagHistory newTag(final WorkingCapitalLoan loan,
            final WorkingCapitalLoanDelinquencyRangeSchedule range, final DelinquencyRange currentRange, final LocalDate businessDate) {
        final WorkingCapitalLoanDelinquencyRangeScheduleTagHistory tag = new WorkingCapitalLoanDelinquencyRangeScheduleTagHistory();
        tag.setLoan(loan);
        tag.setDelinquencyRange(currentRange);
        tag.setRangeSchedule(range);
        tag.setAddedOnDate(businessDate);
        tag.setLiftedOnDate(null);
        return tag;
    }

}
