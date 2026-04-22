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
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanBreachScheduleMapper;
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

        final int graceDays = getGraceDays(loan);
        final LocalDate fromDate = disbursementDateOptional.get().plusDays(graceDays);
        final WorkingCapitalBreach breach = breachOpt.get();
        final LocalDate toDate = calculateToDate(fromDate, breach.getBreachFrequency(), breach.getBreachFrequencyType());
        final BigDecimal minPaymentAmount = calculateMinPaymentAmount(loan, breach);

        final WorkingCapitalLoanBreachSchedule period = createPeriod(loan, 1, fromDate, toDate, minPaymentAmount);
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
        final BigDecimal minPaymentAmount = calculateMinPaymentAmount(loan, breach);
        final List<WorkingCapitalLoanBreachSchedule> newPeriods = new ArrayList<>();

        WorkingCapitalLoanBreachSchedule latestPeriod = latestPeriodOpt.get();
        while (!latestPeriod.getToDate().isAfter(businessDate)) {
            final LocalDate newFromDate = latestPeriod.getToDate().plusDays(1);
            final LocalDate newToDate = calculateToDate(newFromDate, breach.getBreachFrequency(), breach.getBreachFrequencyType());

            final WorkingCapitalLoanBreachSchedule nextPeriod = createPeriod(loan, latestPeriod.getPeriodNumber() + 1, newFromDate,
                    newToDate, minPaymentAmount);
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
            if (businessDate.isAfter(period.getToDate())) {
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
        if (period.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0 && period.getBreach() == null) {
            period.setBreach(false);
        }
        repository.saveAndFlush(period);
        log.debug("Applied repayment of {} to Breach Schedule period {} for WC loan {}", payAmount, period.getPeriodNumber(), loanId);
    }

    @Override
    public void evaluateExpiredPeriods(final WorkingCapitalLoan loan, final LocalDate businessDate) {
        final List<WorkingCapitalLoanBreachSchedule> unevaluatedPeriods = repository
                .findByLoanIdAndToDateLessThanEqualAndBreachIsNull(loan.getId(), businessDate);
        for (final WorkingCapitalLoanBreachSchedule period : unevaluatedPeriods) {
            evaluateBreachOnDate(period, businessDate);
        }
        if (!unevaluatedPeriods.isEmpty()) {
            repository.saveAllAndFlush(unevaluatedPeriods);
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

    private int getGraceDays(final WorkingCapitalLoan loan) {
        final WorkingCapitalLoanProductRelatedDetails details = loan.getLoanProductRelatedDetails();
        if (details == null || details.getDelinquencyGraceDays() == null) {
            return 0;
        }
        return details.getDelinquencyGraceDays();
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

    private BigDecimal calculateMinPaymentAmount(final WorkingCapitalLoan loan, final WorkingCapitalBreach breach) {
        final BigDecimal breachAmount = breach.getBreachAmount();
        if (breachAmount == null) {
            return BigDecimal.ZERO;
        }
        if (WorkingCapitalBreachAmountCalculationType.FLAT.equals(breach.getBreachAmountCalculationType())) {
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

}
