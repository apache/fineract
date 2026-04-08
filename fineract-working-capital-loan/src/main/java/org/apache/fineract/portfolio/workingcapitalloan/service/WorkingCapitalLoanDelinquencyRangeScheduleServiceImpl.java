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
import java.math.MathContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyFrequencyType;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentPeriodAndRule;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentType;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanDelinquencyRangeScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanDelinquencyRangeScheduleMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class WorkingCapitalLoanDelinquencyRangeScheduleServiceImpl implements WorkingCapitalLoanDelinquencyRangeScheduleService {

    private final WorkingCapitalLoanDelinquencyRangeScheduleRepository repository;
    private final WorkingCapitalLoanDelinquencyRangeScheduleMapper mapper;

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
        BigDecimal expectedAmount = calculateExpectedAmount(loan, rule);

        WorkingCapitalLoanDelinquencyRangeSchedule period = new WorkingCapitalLoanDelinquencyRangeSchedule();
        period.setLoan(loan);
        period.setPeriodNumber(1);
        period.setFromDate(fromDate);
        period.setToDate(toDate);
        period.setExpectedAmount(expectedAmount);
        period.setPaidAmount(BigDecimal.ZERO);
        period.setOutstandingAmount(expectedAmount);
        period.setMinPaymentCriteriaMet(null);

        repository.saveAndFlush(period);
        log.debug("Generated initial delinquency range schedule period for WC loan {}", loan.getId());
    }

    @Override
    public boolean hasSchedule(Long loanId) {
        return repository.findTopByLoanIdOrderByPeriodNumberDesc(loanId).isPresent();
    }

    @Override
    public void generateNextPeriodIfNeeded(WorkingCapitalLoan loan, LocalDate businessDate) {
        DelinquencyMinimumPaymentPeriodAndRule rule = getMinimumPaymentRule(loan);
        if (rule == null) {
            return;
        }

        Optional<WorkingCapitalLoanDelinquencyRangeSchedule> latestPeriodOpt = repository
                .findTopByLoanIdOrderByPeriodNumberDesc(loan.getId());
        if (latestPeriodOpt.isEmpty()) {
            return;
        }

        WorkingCapitalLoanDelinquencyRangeSchedule latestPeriod = latestPeriodOpt.get();
        while (!latestPeriod.getToDate().isAfter(businessDate)) {
            LocalDate newFromDate = latestPeriod.getToDate().plusDays(1);
            LocalDate newToDate = calculateToDate(newFromDate, rule.getFrequency(), rule.getFrequencyType());
            BigDecimal expectedAmount = calculateExpectedAmount(loan, rule);

            WorkingCapitalLoanDelinquencyRangeSchedule nextPeriod = new WorkingCapitalLoanDelinquencyRangeSchedule();
            nextPeriod.setLoan(loan);
            nextPeriod.setPeriodNumber(latestPeriod.getPeriodNumber() + 1);
            nextPeriod.setFromDate(newFromDate);
            nextPeriod.setToDate(newToDate);
            nextPeriod.setExpectedAmount(expectedAmount);
            nextPeriod.setPaidAmount(BigDecimal.ZERO);
            nextPeriod.setOutstandingAmount(expectedAmount);
            nextPeriod.setMinPaymentCriteriaMet(null);

            latestPeriod = repository.saveAndFlush(nextPeriod);
            log.debug("Generated next delinquency range schedule period {} for WC loan {}", nextPeriod.getPeriodNumber(), loan.getId());
        }
    }

    @Override
    public void applyRepayment(Long loanId, LocalDate transactionDate, BigDecimal amount) {
        Optional<WorkingCapitalLoanDelinquencyRangeSchedule> periodOpt = repository
                .findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(loanId, transactionDate, transactionDate);
        if (periodOpt.isPresent()) {
            WorkingCapitalLoanDelinquencyRangeSchedule period = periodOpt.get();
            BigDecimal newPaidAmount = period.getPaidAmount().add(amount);
            period.setPaidAmount(newPaidAmount);
            period.setOutstandingAmount(period.getExpectedAmount().subtract(newPaidAmount).max(BigDecimal.ZERO));
            if (newPaidAmount.compareTo(period.getExpectedAmount()) >= 0) {
                period.setMinPaymentCriteriaMet(true);
            }
            repository.saveAndFlush(period);
            log.debug("Applied repayment of {} to delinquency range schedule period {} for WC loan {}", amount, period.getPeriodNumber(),
                    loanId);
        }
    }

    @Override
    public void evaluateExpiredPeriods(WorkingCapitalLoan loan, LocalDate businessDate) {
        List<WorkingCapitalLoanDelinquencyRangeSchedule> unevaluatedPeriods = repository
                .findByLoanIdAndToDateLessThanEqualAndMinPaymentCriteriaMetIsNull(loan.getId(), businessDate);
        for (WorkingCapitalLoanDelinquencyRangeSchedule period : unevaluatedPeriods) {
            boolean criteriaMet = period.getPaidAmount().compareTo(period.getExpectedAmount()) >= 0;
            period.setMinPaymentCriteriaMet(criteriaMet);
            repository.saveAndFlush(period);
            log.debug("Evaluated delinquency range schedule period {} for WC loan {}: criteriaMet={}", period.getPeriodNumber(),
                    loan.getId(), criteriaMet);
        }
    }

    @Override
    public List<WorkingCapitalLoanDelinquencyRangeScheduleData> retrieveRangeSchedule(Long loanId) {
        List<WorkingCapitalLoanDelinquencyRangeSchedule> periods = repository.findByLoanIdOrderByPeriodNumberAsc(loanId);
        return mapper.toDataList(periods);
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
        return bucket.getMinimumPaymentPeriodAndRule();
    }

    private LocalDate calculateToDate(LocalDate fromDate, Integer frequency, DelinquencyFrequencyType frequencyType) {
        return switch (frequencyType) {
            case DAYS -> fromDate.plusDays(frequency - 1);
            case WEEKS -> fromDate.plusWeeks(frequency).minusDays(1);
            case MONTHS -> fromDate.plusMonths(frequency).minusDays(1);
            case YEARS -> fromDate.plusYears(frequency).minusDays(1);
        };
    }

    private BigDecimal calculateExpectedAmount(WorkingCapitalLoan loan, DelinquencyMinimumPaymentPeriodAndRule rule) {
        BigDecimal minimumPayment = rule.getMinimumPayment();
        if (minimumPayment == null) {
            return BigDecimal.ZERO;
        }
        if (DelinquencyMinimumPaymentType.FLAT.equals(rule.getMinimumPaymentType())) {
            return minimumPayment;
        }
        BigDecimal principal = loan.getApprovedPrincipal();
        if (principal == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getDiscount() : null;
        BigDecimal base = discount != null ? principal.add(discount) : principal;
        return MathUtil.percentageOf(base, minimumPayment, MathContext.DECIMAL128);
    }

    @Override
    public void extendPeriodsForPause(WorkingCapitalLoan loan, LocalDate pauseStart, LocalDate pauseEnd) {
        long pauseDays = java.time.temporal.ChronoUnit.DAYS.between(pauseStart, pauseEnd);
        List<WorkingCapitalLoanDelinquencyRangeSchedule> periods = repository.findByLoanIdOrderByPeriodNumberAsc(loan.getId());
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
        repository.saveAll(periods);
        log.debug("Extended delinquency range schedule periods for WC loan {} by {} days due to pause [{} - {}]", loan.getId(), pauseDays,
                pauseStart, pauseEnd);
    }

}
