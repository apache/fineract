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
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.workingcapitalloan.domain.NearBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNearBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.domain.WorkingCapitalNearBreach;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class WorkingCapitalLoanNearBreachEvaluationServiceImpl implements WorkingCapitalLoanNearBreachEvaluationService {

    private final WorkingCapitalLoanBreachScheduleRepository breachScheduleRepository;
    private final WorkingCapitalLoanBreachActionRepository breachActionRepository;
    private final WorkingCapitalLoanNearBreachActionRepository nearBreachActionRepository;

    @Override
    public boolean evaluateNearBreachOnCob(final WorkingCapitalLoan loan, final LocalDate effectiveDate) {
        final Optional<NearBreachParameters> parameters = resolveParameters(loan);
        if (parameters.isEmpty()) {
            return false;
        }
        return breachScheduleRepository
                .findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(loan.getId(), effectiveDate, effectiveDate)
                .map(period -> rederiveNearBreach(List.of(period), parameters.get(), effectiveDate)).orElse(false);
    }

    @Override
    public Optional<NearBreachParameters> resolveParameters(final WorkingCapitalLoan loan) {
        final Optional<NearBreachParameters> parameters = resolveParametersWithBreachEvaluationEnabled(loan);
        if (parameters.isPresent() && breachActionRepository.isBreachDisabled(loan.getId())) {
            log.debug("Skipping near breach evaluation for WC loan {} - breach evaluation is disabled", loan.getId());
            return Optional.empty();
        }
        return parameters;
    }

    @Override
    public Optional<NearBreachParameters> resolveParametersWithBreachEvaluationEnabled(final WorkingCapitalLoan loan) {
        if (!loan.isOpen()) {
            log.debug("Skipping near breach evaluation for WC loan {} - loan status is {}", loan.getId(), loan.getLoanStatus());
            return Optional.empty();
        }
        final Optional<NearBreachParameters> parameters = nearBreachActionRepository
                .findTopByWorkingCapitalLoanIdAndActionOrderByIdDesc(loan.getId(), NearBreachActionType.RESCHEDULE)
                .map(action -> new NearBreachParameters(action.getThreshold(), action.getFrequency(), action.getFrequencyType(),
                        getBreachGraceDays(loan)))
                .or(() -> productParameters(loan));
        if (parameters.isEmpty()) {
            log.debug("Skipping near breach evaluation for WC loan {} - no near breach configuration", loan.getId());
        }
        return parameters;
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
        final LocalDate evaluationStartDate = period.getFromDate().plusDays(parameters.breachGraceDays());
        final LocalDate firstEvalDate = addFrequency(evaluationStartDate, parameters.frequency(), parameters.frequencyType());
        if (firstEvalDate.isAfter(period.getToDate())) {
            return Optional.empty();
        }
        final List<LocalDate> evalDates = listEvalDates(evaluationStartDate, period.getToDate(), parameters.frequency(),
                parameters.frequencyType());
        final int evalIndex = latestEvaluationIndex(evalDates, effectiveDate);
        if (evalIndex < 0) {
            return effectiveDate.isBefore(period.getToDate()) ? Optional.empty() : Optional.of(Boolean.FALSE);
        }
        final MonetaryCurrency currency = period.getLoan().getCurrency();
        final BigDecimal thresholdFraction = parameters.threshold().divide(BigDecimal.valueOf(100), MoneyHelper.getMathContext());
        final Money requiredCumulative = calculateRequiredCumulative(currency, period.getMinPaymentAmount(), thresholdFraction, evalIndex);
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

    private Optional<NearBreachParameters> productParameters(final WorkingCapitalLoan loan) {
        final WorkingCapitalLoanProductRelatedDetails details = loan.getLoanProductRelatedDetails();
        if (details == null || details.getNearBreach() == null) {
            return Optional.empty();
        }
        final WorkingCapitalNearBreach config = details.getNearBreach();
        return Optional.of(new NearBreachParameters(config.getThreshold(), config.getFrequency(), config.getFrequencyType(),
                getBreachGraceDays(loan)));
    }

    private Money calculateRequiredCumulative(final MonetaryCurrency currency, final BigDecimal minPaymentAmount,
            final BigDecimal thresholdFraction, final int evalIndex) {
        final BigDecimal rawAmount = thresholdFraction.multiply(BigDecimal.valueOf(evalIndex + 1L), MoneyHelper.getMathContext())
                .multiply(minPaymentAmount, MoneyHelper.getMathContext());
        return Money.of(currency, rawAmount);
    }

    private List<LocalDate> listEvalDates(final LocalDate fromDate, final LocalDate toDate, final Integer frequency,
            final WorkingCapitalLoanPeriodFrequencyType frequencyType) {
        final List<LocalDate> dates = new ArrayList<>();
        for (int multiplicator = 1;; multiplicator++) {
            final LocalDate evalDate = addFrequency(fromDate, frequency * multiplicator, frequencyType);
            if (!evalDate.isBefore(toDate)) {
                break;
            }
            dates.add(evalDate);
        }
        return dates;
    }

    private int latestEvaluationIndex(final List<LocalDate> evalDates, final LocalDate effectiveDate) {
        int latestIndex = -1;
        for (int index = 0; index < evalDates.size(); index++) {
            if (evalDates.get(index).isAfter(effectiveDate)) {
                break;
            }
            latestIndex = index;
        }
        return latestIndex;
    }

    private LocalDate addFrequency(final LocalDate date, final int amount, final WorkingCapitalLoanPeriodFrequencyType frequencyType) {
        return switch (frequencyType) {
            case DAYS -> date.plusDays(amount - 1L);
            case WEEKS -> date.plusWeeks(amount).minusDays(1);
            case MONTHS -> date.plusMonths(amount).minusDays(1);
            case YEARS -> date.plusYears(amount).minusDays(1);
        };
    }

    private Integer getBreachGraceDays(final WorkingCapitalLoan loan) {
        if (loan.getLoanProductRelatedDetails() == null || loan.getLoanProductRelatedDetails().getBreachGraceDays() == null) {
            return 0;
        }
        return loan.getLoanProductRelatedDetails().getBreachGraceDays();
    }

}
