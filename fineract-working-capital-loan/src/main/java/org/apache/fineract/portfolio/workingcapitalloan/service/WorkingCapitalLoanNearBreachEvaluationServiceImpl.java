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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.workingcapitalloan.domain.NearBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachPauseUtils;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachScheduleEvaluationUtils;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNearBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class WorkingCapitalLoanNearBreachEvaluationServiceImpl implements WorkingCapitalLoanNearBreachEvaluationService {

    private static final Set<WorkingCapitalLoanBreachActionType> PAUSE_AND_RESET_ACTIONS = Set.of(WorkingCapitalLoanBreachActionType.PAUSE,
            WorkingCapitalLoanBreachActionType.RESUME, WorkingCapitalLoanBreachActionType.RESET,
            WorkingCapitalLoanBreachActionType.UNDO_RESET);

    private final WorkingCapitalLoanBreachScheduleRepository breachScheduleRepository;
    private final WorkingCapitalLoanBreachActionRepository breachActionRepository;
    private final WorkingCapitalLoanNearBreachActionRepository nearBreachActionRepository;
    private final WorkingCapitalLoanActiveBreachResetResolver activeBreachResetResolver;

    @Override
    public boolean evaluateNearBreachOnCob(final WorkingCapitalLoan loan, final LocalDate effectiveDate) {
        final Optional<EffectiveNearBreachRescheduleParams> params = resolveEffectiveRescheduleParams(loan);
        if (params.isEmpty()) {
            return false;
        }
        if (breachActionRepository.isBreachDisabled(loan.getId())) {
            log.debug("Skipping near breach evaluation for WC loan {} - breach evaluation is disabled", loan.getId());
            return false;
        }
        return breachScheduleRepository
                .findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(loan.getId(), effectiveDate, effectiveDate)
                .map(period -> rederiveNearBreach(List.of(period), nearBreachParameters(loan, params.get()), effectiveDate)).orElse(false);
    }

    @Override
    public Optional<NearBreachParameters> resolveParameters(final WorkingCapitalLoan loan) {
        return resolveEffectiveRescheduleParams(loan).map(params -> nearBreachParameters(loan, params));
    }

    @Override
    public boolean rederiveNearBreach(final List<WorkingCapitalLoanBreachSchedule> periods, final NearBreachParameters parameters,
            final LocalDate effectiveDate) {
        final List<WorkingCapitalLoanBreachSchedule> changed = periods.stream()
                .filter(period -> rederive(period, parameters, effectiveDate)).toList();
        if (changed.isEmpty()) {
            return false;
        }
        breachScheduleRepository.saveAll(changed);
        return true;
    }

    /**
     * The near breach of a period as of {@code effectiveDate}: whether the cumulative paid amount falls short of the
     * requirement at the latest elapsed checkpoint, and from the period end on the close-out value. Empty when there is
     * nothing to judge against: no demand, no checkpoint inside the period or none elapsed yet.
     */
    Optional<Boolean> resolveNearBreachValue(final WorkingCapitalLoanBreachSchedule period, final NearBreachParameters parameters,
            final LocalDate effectiveDate) {
        if (period.getMinPaymentAmount() == null || period.getMinPaymentAmount().compareTo(BigDecimal.ZERO) == 0) {
            return Optional.empty();
        }
        if (evalDate(period, parameters, 1).isAfter(period.getToDate())) {
            return Optional.empty();
        }
        final long elapsedEvalDates = countElapsedEvalDates(period, parameters, effectiveDate);
        if (elapsedEvalDates == 0) {
            return effectiveDate.isBefore(period.getToDate()) ? Optional.empty() : Optional.of(Boolean.FALSE);
        }
        final MonetaryCurrency currency = period.getLoan().getCurrency();
        final BigDecimal thresholdFraction = parameters.threshold().divide(BigDecimal.valueOf(100), MoneyHelper.getMathContext());
        final Money requiredCumulative = calculateRequiredCumulative(currency, period.getMinPaymentAmount(), thresholdFraction,
                elapsedEvalDates);
        return Optional.of(Money.of(currency, period.getPaidAmount()).isLessThan(requiredCumulative));
    }

    private boolean rederive(final WorkingCapitalLoanBreachSchedule period, final NearBreachParameters parameters,
            final LocalDate effectiveDate) {
        final Optional<Boolean> resolved = resolveNearBreachValue(period, parameters, effectiveDate);
        if (resolved.isEmpty() || resolved.get().equals(period.getNearBreach())) {
            return false;
        }
        final Boolean nearBreach = resolved.get();
        log.debug("Near breach of period {} of WC loan {} changed from {} to {} as of {}", period.getPeriodNumber(),
                period.getLoan().getId(), period.getNearBreach(), nearBreach, effectiveDate);
        period.setNearBreach(nearBreach);
        return true;
    }

    private Optional<EffectiveNearBreachRescheduleParams> resolveEffectiveRescheduleParams(final WorkingCapitalLoan loan) {
        if (!loan.isOpen()) {
            log.debug("Skipping near breach evaluation for WC loan {} - loan status is {}", loan.getId(), loan.getLoanStatus());
            return Optional.empty();
        }
        final Optional<EffectiveNearBreachRescheduleParams> params = nearBreachActionRepository
                .findTopByWorkingCapitalLoanIdAndActionOrderByIdDesc(loan.getId(), NearBreachActionType.RESCHEDULE)
                .map(action -> new EffectiveNearBreachRescheduleParams(action.getThreshold(), action.getFrequency(),
                        action.getFrequencyType()))
                .or(() -> productParams(loan));
        if (params.isEmpty()) {
            log.debug("Skipping near breach evaluation for WC loan {} - no near breach configuration", loan.getId());
        }
        return params;
    }

    private Optional<EffectiveNearBreachRescheduleParams> productParams(final WorkingCapitalLoan loan) {
        return Optional.ofNullable(loan.getLoanProductRelatedDetails()).map(WorkingCapitalLoanProductRelatedDetails::getNearBreach).map(
                config -> new EffectiveNearBreachRescheduleParams(config.getThreshold(), config.getFrequency(), config.getFrequencyType()));
    }

    private NearBreachParameters nearBreachParameters(final WorkingCapitalLoan loan, final EffectiveNearBreachRescheduleParams params) {
        final List<WorkingCapitalLoanBreachAction> pauseAndResetActions = breachActionRepository
                .findByWorkingCapitalLoanIdAndActionInOrderById(loan.getId(), PAUSE_AND_RESET_ACTIONS);
        return new NearBreachParameters(params.threshold(), params.frequency(), params.frequencyType(), getBreachGraceDays(loan),
                WorkingCapitalLoanBreachPauseUtils.toEffectivePauses(pauseAndResetActions),
                activeBreachResetResolver.activeRestartResetDates(pauseAndResetActions));
    }

    private Money calculateRequiredCumulative(final MonetaryCurrency currency, final BigDecimal minPaymentAmount,
            final BigDecimal thresholdFraction, final long elapsedEvalDates) {
        final BigDecimal rawAmount = thresholdFraction.multiply(BigDecimal.valueOf(elapsedEvalDates), MoneyHelper.getMathContext())
                .multiply(minPaymentAmount, MoneyHelper.getMathContext());
        return Money.of(currency, rawAmount);
    }

    private long countElapsedEvalDates(final WorkingCapitalLoanBreachSchedule period, final NearBreachParameters parameters,
            final LocalDate effectiveDate) {
        return Stream.iterate(1, multiplicator -> multiplicator + 1) //
                .map(multiplicator -> evalDate(period, parameters, multiplicator)) //
                .takeWhile(evalDate -> evalDate.isBefore(period.getToDate()) && !evalDate.isAfter(effectiveDate)) //
                .count();
    }

    private LocalDate evalDate(final WorkingCapitalLoanBreachSchedule period, final NearBreachParameters parameters,
            final int multiplicator) {
        final LocalDate evaluationStartDate = period.getFromDate().plusDays(parameters.breachGraceDays());
        final LocalDate unpausedEvalDate = WorkingCapitalLoanBreachScheduleEvaluationUtils.calculateToDate(evaluationStartDate,
                parameters.frequency() * multiplicator, parameters.frequencyType());
        return WorkingCapitalLoanBreachPauseUtils
                .applyPauses(period.getFromDate(), unpausedEvalDate, parameters.breachPauses(), parameters.restartResetDates()).toDate();
    }

    private Integer getBreachGraceDays(final WorkingCapitalLoan loan) {
        if (loan.getLoanProductRelatedDetails() == null || loan.getLoanProductRelatedDetails().getBreachGraceDays() == null) {
            return 0;
        }
        return loan.getLoanProductRelatedDetails().getBreachGraceDays();
    }

}
