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
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanNearBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.domain.WorkingCapitalNearBreach;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class WorkingCapitalLoanNearBreachEvaluationServiceImpl implements WorkingCapitalLoanNearBreachEvaluationService {

    private final WorkingCapitalLoanBreachScheduleRepository breachScheduleRepository;
    private final WorkingCapitalLoanBreachActionRepository breachActionRepository;

    @Override
    public void evaluateNearBreach(final WorkingCapitalLoan loan, final WorkingCapitalLoanNearBreachAction latestAction,
            final LocalDate effectiveDate) {
        final Optional<WorkingCapitalLoanBreachSchedule> relevantPeriod = breachScheduleRepository
                .findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(loan.getId(), effectiveDate, effectiveDate);
        if (relevantPeriod.isEmpty()) {
            return;
        }
        final WorkingCapitalLoanBreachSchedule period = relevantPeriod.get();
        if (period.getNearBreach() != null) {
            return;
        }
        if (isBreachEvaluationDisabled(loan.getId(), effectiveDate)) {
            log.debug("Skipping near breach evaluation for WC loan {} - breach evaluation is disabled as of {}", loan.getId(),
                    effectiveDate);
            return;
        }
        final WorkingCapitalNearBreach config = loan.getLoanProductRelatedDetails().getNearBreach();

        final BigDecimal effectiveThreshold;
        final Integer effectiveFrequency;
        final WorkingCapitalLoanPeriodFrequencyType effectiveFrequencyType;
        if (latestAction != null) {
            effectiveThreshold = latestAction.getThreshold();
            effectiveFrequency = latestAction.getFrequency();
            effectiveFrequencyType = latestAction.getFrequencyType();
        } else {
            effectiveThreshold = config.getThreshold();
            effectiveFrequency = config.getFrequency();
            effectiveFrequencyType = config.getFrequencyType();
        }
        if (evaluatePeriod(loan.getId(), period, effectiveThreshold, effectiveFrequency, effectiveFrequencyType, effectiveDate)) {
            breachScheduleRepository.saveAndFlush(period);
        }
    }

    private boolean evaluatePeriod(final Long loanId, final WorkingCapitalLoanBreachSchedule period, final BigDecimal threshold,
            final Integer frequency, final WorkingCapitalLoanPeriodFrequencyType frequencyType, final LocalDate effectiveDate) {
        if (period.getMinPaymentAmount() == null || period.getMinPaymentAmount().compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        final LocalDate firstEvalDate = addFrequency(period.getFromDate(), frequency, frequencyType);
        if (firstEvalDate.isAfter(period.getToDate())) {
            return false;
        }
        final List<LocalDate> evalDates = listEvalDates(period.getFromDate(), period.getToDate(), frequency, frequencyType);
        final int evalIndex = evalDates.indexOf(effectiveDate);
        if (evalIndex >= 0) {
            final MonetaryCurrency currency = period.getLoan().getLoanProductRelatedDetails().getCurrency();
            final BigDecimal thresholdFraction = threshold.divide(BigDecimal.valueOf(100), MoneyHelper.getMathContext());
            final Money requiredCumulative = calculateRequiredCumulative(currency, period.getMinPaymentAmount(), thresholdFraction,
                    evalIndex);
            final Money paidCumulative = Money.of(currency, period.getPaidAmount());
            if (paidCumulative.isLessThan(requiredCumulative)) {
                period.setNearBreach(true);
                log.debug("Near breach detected for period {} of WC loan {}: evalDate={} cumulativePaid={} requiredCumulative={}",
                        period.getPeriodNumber(), loanId, effectiveDate, paidCumulative, requiredCumulative);
                return true;
            }
        }
        if (!effectiveDate.isBefore(period.getToDate())) {
            period.setNearBreach(false);
            log.debug("No near breach for period {} of WC loan {} after all evaluation points", period.getPeriodNumber(), loanId);
            return true;
        }
        return false;
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

    private LocalDate addFrequency(final LocalDate date, final int amount, final WorkingCapitalLoanPeriodFrequencyType frequencyType) {
        return switch (frequencyType) {
            case DAYS -> date.plusDays(amount);
            case WEEKS -> date.plusWeeks(amount);
            case MONTHS -> date.plusMonths(amount);
            case YEARS -> date.plusYears(amount);
        };
    }

    private boolean isBreachEvaluationDisabled(final Long loanId, final LocalDate date) {
        return breachActionRepository.isBreachDisabledAsOf(loanId, date);
    }

}
