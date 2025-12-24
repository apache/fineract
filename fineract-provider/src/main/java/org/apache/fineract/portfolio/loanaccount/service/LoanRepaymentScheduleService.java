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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.LoanSchedulePeriodDataWrapper;
import org.apache.fineract.portfolio.loanaccount.data.RepaymentScheduleRelatedLoanData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepaymentPeriodData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanRepaymentScheduleService {

    private final LoanRepaymentScheduleInstallmentRepository loanRepaymentScheduleInstallmentRepository;

    public LoanScheduleData findLoanScheduleData(final Long loanId, final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData,
            Collection<DisbursementData> disbursementData, Collection<LoanTransactionRepaymentPeriodData> capitalizedIncomeData,
            boolean isInterestRecalculationEnabled, LoanScheduleType loanScheduleType) {
        final List<LoanRepaymentScheduleInstallment> installments = this.loanRepaymentScheduleInstallmentRepository.findByLoanId(loanId);

        return extractLoanScheduleData(installments, repaymentScheduleRelatedLoanData, disbursementData, capitalizedIncomeData,
                isInterestRecalculationEnabled, loanScheduleType);
    }

    public LoanScheduleData extractLoanScheduleData(final List<LoanRepaymentScheduleInstallment> installments,
            final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData, Collection<DisbursementData> disbursementData,
            Collection<LoanTransactionRepaymentPeriodData> capitalizedIncomeData, boolean isInterestRecalculationEnabled,
            LoanScheduleType loanScheduleType) {

        final CurrencyData currency = repaymentScheduleRelatedLoanData.getCurrency();
        final DisbursementData disbursement = repaymentScheduleRelatedLoanData.disbursementData();
        final BigDecimal totalFeeChargesDueAtDisbursement = repaymentScheduleRelatedLoanData.getTotalFeeChargesAtDisbursement();
        LocalDate lastDueDate = disbursement.disbursementDate();
        BigDecimal outstandingLoanPrincipalBalance = disbursement.getPrincipal();
        boolean excludePastUnDisbursed = LoanScheduleType.PROGRESSIVE.equals(loanScheduleType) && isInterestRecalculationEnabled;
        BigDecimal waivedChargeAmount = BigDecimal.ZERO;
        for (DisbursementData disbursementDetail : disbursementData) {
            waivedChargeAmount = waivedChargeAmount.add(disbursementDetail.getWaivedChargeAmount());
        }
        final LoanSchedulePeriodData disbursementPeriod = LoanSchedulePeriodData.disbursementOnlyPeriod(disbursement.disbursementDate(),
                disbursement.getPrincipal(), totalFeeChargesDueAtDisbursement, disbursement.isDisbursed());

        final List<LoanSchedulePeriodData> periods = new ArrayList<>();
        final MonetaryCurrency monCurrency = new MonetaryCurrency(currency.getCode(), currency.getDecimalPlaces(),
                currency.getInMultiplesOf());
        BigDecimal totalPrincipalDisbursed = BigDecimal.ZERO;
        BigDecimal disbursementChargeAmount = totalFeeChargesDueAtDisbursement;
        if (disbursementData.isEmpty()) {
            periods.add(disbursementPeriod);
            totalPrincipalDisbursed = Money.of(monCurrency, disbursement.getPrincipal()).getAmount();
        } else {
            if (!disbursement.isDisbursed()) {
                excludePastUnDisbursed = false;
            }
            for (DisbursementData data : disbursementData) {
                if (data.getChargeAmount() != null) {
                    disbursementChargeAmount = disbursementChargeAmount.subtract(data.getChargeAmount());
                }
            }
            outstandingLoanPrincipalBalance = BigDecimal.ZERO;
        }

        Money totalPrincipalExpected = Money.zero(monCurrency);
        Money totalPrincipalPaid = Money.zero(monCurrency);
        Money totalInterestCharged = Money.zero(monCurrency);
        Money totalFeeChargesCharged = Money.zero(monCurrency);
        Money totalPenaltyChargesCharged = Money.zero(monCurrency);
        Money totalWaived = Money.zero(monCurrency);
        Money totalWrittenOff = Money.zero(monCurrency);
        Money totalRepaymentExpected = Money.zero(monCurrency);
        Money totalRepayment = Money.zero(monCurrency);
        Money totalPaidInAdvance = Money.zero(monCurrency);
        Money totalPaidLate = Money.zero(monCurrency);
        Money totalOutstanding = Money.zero(monCurrency);
        Money totalCredits = Money.zero(monCurrency);

        // update totals with details of fees charged during disbursement
        totalFeeChargesCharged = totalFeeChargesCharged.plus(disbursementPeriod.getFeeChargesDue().subtract(waivedChargeAmount));
        totalRepaymentExpected = totalRepaymentExpected.plus(disbursementPeriod.getFeeChargesDue()).minus(waivedChargeAmount);
        totalRepayment = totalRepayment.plus(disbursementPeriod.getFeeChargesPaid()).minus(waivedChargeAmount);
        totalOutstanding = totalOutstanding.plus(disbursementPeriod.getFeeChargesDue()).minus(disbursementPeriod.getFeeChargesPaid());

        Integer loanTermInDays = 0;
        Set<Long> disbursementPeriodIds = new HashSet<>();

        for (LoanRepaymentScheduleInstallment installment : installments) {
            final Integer period = installment.getInstallmentNumber();
            LocalDate fromDate = installment.getFromDate();
            final LocalDate dueDate = installment.getDueDate();
            final LocalDate obligationsMetOnDate = installment.getObligationsMetOnDate();
            final boolean complete = installment.isObligationsMet();

            List<LoanSchedulePeriodDataWrapper> combinedDataList = new ArrayList<>();
            combinedDataList.addAll(collectEligibleDisbursementData(loanScheduleType, disbursementData, fromDate, dueDate,
                    disbursementPeriodIds, disbursement, excludePastUnDisbursed));
            combinedDataList.addAll(collectEligibleCapitalizedIncomeData(capitalizedIncomeData, fromDate, dueDate, disbursementPeriodIds));
            combinedDataList.sort(this::sortPeriodDataHolders);
            outstandingLoanPrincipalBalance = fillLoanSchedulePeriodData(periods, combinedDataList, disbursementChargeAmount,
                    waivedChargeAmount, outstandingLoanPrincipalBalance);

            BigDecimal disbursedAmount = calculateDisbursedAmount(combinedDataList);

            // Add the Charge back or Credits to the initial amount to avoid negative balance
            final BigDecimal principalCredits = installment.getCreditedPrincipal() != null ? installment.getCreditedPrincipal()
                    : BigDecimal.ZERO;
            final BigDecimal feeCredits = installment.getCreditedFee() != null ? installment.getCreditedFee() : BigDecimal.ZERO;
            final BigDecimal penaltyCredits = installment.getCreditedPenalty() != null ? installment.getCreditedPenalty() : BigDecimal.ZERO;
            final BigDecimal credits = principalCredits.add(feeCredits).add(penaltyCredits);
            outstandingLoanPrincipalBalance = outstandingLoanPrincipalBalance.add(principalCredits);

            totalPrincipalDisbursed = totalPrincipalDisbursed.add(disbursedAmount);

            Integer daysInPeriod = 0;
            if (fromDate != null) {
                daysInPeriod = DateUtils.getExactDifferenceInDays(fromDate, dueDate);
                loanTermInDays = loanTermInDays + daysInPeriod;
            }

            final BigDecimal principalDue = installment.getPrincipal() != null ? installment.getPrincipal() : BigDecimal.ZERO;
            totalPrincipalExpected = totalPrincipalExpected.plus(principalDue);
            final BigDecimal principalPaid = installment.getPrincipalCompleted() != null ? installment.getPrincipalCompleted()
                    : BigDecimal.ZERO;
            totalPrincipalPaid = totalPrincipalPaid.plus(principalPaid);
            final BigDecimal principalWrittenOff = installment.getPrincipalWrittenOff() != null ? installment.getPrincipalWrittenOff()
                    : BigDecimal.ZERO;

            final BigDecimal principalOutstanding = principalDue.subtract(principalPaid).subtract(principalWrittenOff);

            final BigDecimal interestExpectedDue = installment.getInterestCharged() != null ? installment.getInterestCharged()
                    : BigDecimal.ZERO;
            totalInterestCharged = totalInterestCharged.plus(interestExpectedDue);
            final BigDecimal interestPaid = installment.getInterestPaid() != null ? installment.getInterestPaid() : BigDecimal.ZERO;
            final BigDecimal interestWaived = installment.getInterestWaived() != null ? installment.getInterestWaived() : BigDecimal.ZERO;
            final BigDecimal interestWrittenOff = installment.getInterestWrittenOff() != null ? installment.getInterestWrittenOff()
                    : BigDecimal.ZERO;
            final BigDecimal accrualInterest = installment.getInterestAccrued() != null ? installment.getInterestAccrued()
                    : BigDecimal.ZERO;

            final BigDecimal interestActualDue = interestExpectedDue.subtract(interestWaived).subtract(interestWrittenOff);
            final BigDecimal interestOutstanding = interestActualDue.subtract(interestPaid);

            final BigDecimal feeChargesExpectedDue = installment.getFeeChargesCharged() != null ? installment.getFeeChargesCharged()
                    : BigDecimal.ZERO;
            totalFeeChargesCharged = totalFeeChargesCharged.plus(feeChargesExpectedDue);
            final BigDecimal feeChargesPaid = installment.getFeeChargesPaid() != null ? installment.getFeeChargesPaid() : BigDecimal.ZERO;
            final BigDecimal feeChargesWaived = installment.getFeeChargesWaived() != null ? installment.getFeeChargesWaived()
                    : BigDecimal.ZERO;
            final BigDecimal feeChargesWrittenOff = installment.getFeeChargesWrittenOff() != null ? installment.getFeeChargesWrittenOff()
                    : BigDecimal.ZERO;

            final BigDecimal feeChargesActualDue = feeChargesExpectedDue.subtract(feeChargesWaived).subtract(feeChargesWrittenOff);
            final BigDecimal feeChargesOutstanding = feeChargesActualDue.subtract(feeChargesPaid);

            final BigDecimal penaltyChargesExpectedDue = installment.getPenaltyCharges() != null ? installment.getPenaltyCharges()
                    : BigDecimal.ZERO;
            totalPenaltyChargesCharged = totalPenaltyChargesCharged.plus(penaltyChargesExpectedDue);
            final BigDecimal penaltyChargesPaid = installment.getPenaltyChargesPaid() != null ? installment.getPenaltyChargesPaid()
                    : BigDecimal.ZERO;
            final BigDecimal penaltyChargesWaived = installment.getPenaltyChargesWaived() != null ? installment.getPenaltyChargesWaived()
                    : BigDecimal.ZERO;
            final BigDecimal penaltyChargesWrittenOff = installment.getPenaltyChargesWrittenOff() != null
                    ? installment.getPenaltyChargesWrittenOff()
                    : BigDecimal.ZERO;

            final BigDecimal totalPaidInAdvanceForPeriod = installment.getTotalPaidInAdvance() != null ? installment.getTotalPaidInAdvance()
                    : BigDecimal.ZERO;
            final BigDecimal totalPaidLateForPeriod = installment.getTotalPaidLate() != null ? installment.getTotalPaidLate()
                    : BigDecimal.ZERO;

            final BigDecimal penaltyChargesActualDue = penaltyChargesExpectedDue.subtract(penaltyChargesWaived)
                    .subtract(penaltyChargesWrittenOff);
            final BigDecimal penaltyChargesOutstanding = penaltyChargesActualDue.subtract(penaltyChargesPaid);

            final BigDecimal totalExpectedCostOfLoanForPeriod = interestExpectedDue.add(feeChargesExpectedDue)
                    .add(penaltyChargesExpectedDue);

            final BigDecimal totalDueForPeriod = principalDue.add(totalExpectedCostOfLoanForPeriod);
            final BigDecimal totalPaidForPeriod = principalPaid.add(interestPaid).add(feeChargesPaid).add(penaltyChargesPaid);
            final BigDecimal totalWaivedForPeriod = interestWaived.add(feeChargesWaived).add(penaltyChargesWaived);
            totalWaived = totalWaived.plus(totalWaivedForPeriod);
            final BigDecimal totalWrittenOffForPeriod = principalWrittenOff.add(interestWrittenOff).add(feeChargesWrittenOff)
                    .add(penaltyChargesWrittenOff);
            totalWrittenOff = totalWrittenOff.plus(totalWrittenOffForPeriod);

            final BigDecimal totalOutstandingForPeriod = principalOutstanding.add(interestOutstanding).add(feeChargesOutstanding)
                    .add(penaltyChargesOutstanding);

            totalRepaymentExpected = totalRepaymentExpected.plus(totalDueForPeriod);
            totalRepayment = totalRepayment.plus(totalPaidForPeriod);
            totalPaidInAdvance = totalPaidInAdvance.plus(totalPaidInAdvanceForPeriod);
            totalPaidLate = totalPaidLate.plus(totalPaidLateForPeriod);
            totalOutstanding = totalOutstanding.plus(totalOutstandingForPeriod);
            totalCredits = totalCredits.add(credits);

            if (fromDate == null) {
                fromDate = lastDueDate;
            }

            BigDecimal outstandingPrincipalBalanceOfLoan = outstandingLoanPrincipalBalance.subtract(principalDue);

            // update based on current period values
            lastDueDate = dueDate;
            outstandingLoanPrincipalBalance = outstandingLoanPrincipalBalance.subtract(principalDue);

            final boolean isDownPayment = installment.isDownPayment();

            LoanSchedulePeriodData periodData;

            periodData = LoanSchedulePeriodData.periodWithPayments(period, fromDate, dueDate, obligationsMetOnDate, complete, principalDue,
                    principalPaid, principalWrittenOff, principalOutstanding, outstandingPrincipalBalanceOfLoan, interestExpectedDue,
                    interestPaid, interestWaived, interestWrittenOff, interestOutstanding, feeChargesExpectedDue, feeChargesPaid,
                    feeChargesWaived, feeChargesWrittenOff, feeChargesOutstanding, penaltyChargesExpectedDue, penaltyChargesPaid,
                    penaltyChargesWaived, penaltyChargesWrittenOff, penaltyChargesOutstanding, totalPaidForPeriod,
                    totalPaidInAdvanceForPeriod, totalPaidLateForPeriod, totalWaivedForPeriod, totalWrittenOffForPeriod, credits,
                    isDownPayment, accrualInterest);

            periods.add(periodData);
        }

        return new LoanScheduleData(currency, periods, loanTermInDays, totalPrincipalDisbursed, totalPrincipalExpected.getAmount(),
                totalPrincipalPaid.getAmount(), totalInterestCharged.getAmount(), totalFeeChargesCharged.getAmount(),
                totalPenaltyChargesCharged.getAmount(), totalWaived.getAmount(), totalWrittenOff.getAmount(),
                totalRepaymentExpected.getAmount(), totalRepayment.getAmount(), totalPaidInAdvance.getAmount(), totalPaidLate.getAmount(),
                totalOutstanding.getAmount(), totalCredits.getAmount());
    }

    private List<LoanSchedulePeriodDataWrapper> collectEligibleDisbursementData(LoanScheduleType loanScheduleType,
            Collection<DisbursementData> disbursementData, LocalDate fromDate, LocalDate dueDate, Set<Long> disbursementPeriodIds,
            DisbursementData mainDisbursement, boolean excludePastUnDisbursed) {
        List<LoanSchedulePeriodDataWrapper> disbursementDataList = new ArrayList<>();

        boolean hasMultipleTranchesOnSameDate = hasMultipleTranchesOnSameDate(disbursementData);

        if (hasMultipleTranchesOnSameDate) {
            Map<LocalDate, List<DisbursementData>> disbursementsByDate = new HashMap<>();

            for (final DisbursementData data : disbursementData) {
                boolean isDueForDisbursement = data.isDueForDisbursement(loanScheduleType, fromDate, dueDate);
                boolean isEligible = ((fromDate.equals(mainDisbursement.disbursementDate()) && data.disbursementDate().equals(fromDate))
                        || (fromDate.equals(dueDate) && data.disbursementDate().equals(fromDate))
                        || canAddDisbursementData(data, isDueForDisbursement, excludePastUnDisbursed))
                        && !disbursementPeriodIds.contains(data.getId());

                if (isEligible) {
                    disbursementsByDate.computeIfAbsent(data.disbursementDate(), k -> new ArrayList<>()).add(data);
                    disbursementPeriodIds.add(data.getId());
                }
            }

            for (Map.Entry<LocalDate, List<DisbursementData>> entry : disbursementsByDate.entrySet()) {
                List<DisbursementData> sameDateDisbursements = entry.getValue();

                if (sameDateDisbursements.size() > 1) {
                    List<DisbursementData> disbursedTranches = sameDateDisbursements.stream().filter(DisbursementData::isDisbursed)
                            .collect(Collectors.toList());

                    if (!disbursedTranches.isEmpty()) {
                        for (DisbursementData data : disbursedTranches) {
                            disbursementDataList
                                    .add(new LoanSchedulePeriodDataWrapper(data, data.disbursementDate(), true, data.isDisbursed()));
                        }
                    } else {
                        for (DisbursementData data : sameDateDisbursements) {
                            disbursementDataList
                                    .add(new LoanSchedulePeriodDataWrapper(data, data.disbursementDate(), true, data.isDisbursed()));
                        }
                    }
                } else {
                    DisbursementData data = sameDateDisbursements.get(0);
                    disbursementDataList.add(new LoanSchedulePeriodDataWrapper(data, data.disbursementDate(), true, data.isDisbursed()));
                }
            }
        } else {
            for (final DisbursementData data : disbursementData) {
                boolean isDueForDisbursement = data.isDueForDisbursement(loanScheduleType, fromDate, dueDate);
                boolean isEligible = ((fromDate.equals(mainDisbursement.disbursementDate()) && data.disbursementDate().equals(fromDate))
                        || (fromDate.equals(dueDate) && data.disbursementDate().equals(fromDate))
                        || canAddDisbursementData(data, isDueForDisbursement, excludePastUnDisbursed))
                        && !disbursementPeriodIds.contains(data.getId());

                if (isEligible) {
                    disbursementDataList.add(new LoanSchedulePeriodDataWrapper(data, data.disbursementDate(), true, data.isDisbursed()));
                    disbursementPeriodIds.add(data.getId());
                }
            }
        }

        return disbursementDataList;
    }

    private boolean hasMultipleTranchesOnSameDate(Collection<DisbursementData> disbursementData) {
        if (disbursementData == null || disbursementData.size() <= 1) {
            return false;
        }
        return disbursementData.stream().collect(Collectors.groupingBy(DisbursementData::disbursementDate, Collectors.counting())).values()
                .stream().anyMatch(count -> count > 1);
    }

    private List<LoanSchedulePeriodDataWrapper> collectEligibleCapitalizedIncomeData(
            Collection<LoanTransactionRepaymentPeriodData> capitalizedIncomeData, LocalDate fromDate, LocalDate dueDate,
            Set<Long> disbursementPeriodIds) {
        List<LoanSchedulePeriodDataWrapper> capitalizedIncomeDataList = new ArrayList<>();
        // Collect eligible capitalized income data
        for (LoanTransactionRepaymentPeriodData data : capitalizedIncomeData) {
            boolean isEligible = canAddCapitalizedIncomeData(data, fromDate, dueDate)
                    && !disbursementPeriodIds.contains(data.getTransactionId());

            if (isEligible) {
                capitalizedIncomeDataList.add(new LoanSchedulePeriodDataWrapper(data, data.getDate(), false, false));
                disbursementPeriodIds.add(data.getTransactionId());
            }
        }
        return capitalizedIncomeDataList;
    }

    private BigDecimal fillLoanSchedulePeriodData(List<LoanSchedulePeriodData> periods,
            List<LoanSchedulePeriodDataWrapper> combinedDataList, BigDecimal disbursementChargeAmount, BigDecimal waivedChargeAmount,
            BigDecimal outstandingLoanPrincipalBalance) {
        // Process all collected data in chronological order
        for (LoanSchedulePeriodDataWrapper dataItem : combinedDataList) {
            LoanSchedulePeriodData periodData;
            if (dataItem.isDisbursement()) {
                // Process disbursement data
                DisbursementData data = (DisbursementData) dataItem.getData();
                periodData = createLoanSchedulePeriodData(data, disbursementChargeAmount, waivedChargeAmount);
            } else {
                // Process capitalized income data
                LoanTransactionRepaymentPeriodData data = (LoanTransactionRepaymentPeriodData) dataItem.getData();
                periodData = createLoanSchedulePeriodData(data);
            }

            // Common processing for both data types
            periods.add(periodData);
            outstandingLoanPrincipalBalance = outstandingLoanPrincipalBalance.add(periodData.getPrincipalDisbursed());
        }
        return outstandingLoanPrincipalBalance;
    }

    private BigDecimal calculateDisbursedAmount(List<LoanSchedulePeriodDataWrapper> combinedDataList) {
        BigDecimal disbursedAmount = BigDecimal.ZERO;
        for (LoanSchedulePeriodDataWrapper dataItem : combinedDataList) {
            if (dataItem.isDisbursement()) {
                DisbursementData data = (DisbursementData) dataItem.getData();
                disbursedAmount = disbursedAmount.add(data.getPrincipal());
            }
        }
        return disbursedAmount;
    }

    private int sortPeriodDataHolders(LoanSchedulePeriodDataWrapper item1, LoanSchedulePeriodDataWrapper item2) {
        int dateComparison = item1.getDate().compareTo(item2.getDate());
        if (dateComparison == 0 && item1.isDisbursement() != item2.isDisbursement()) {
            // If dates are equal, prioritize disbursement data
            return item1.isDisbursement() ? -1 : 1;
        }
        return dateComparison;
    }

    private LoanSchedulePeriodData createLoanSchedulePeriodData(final DisbursementData data, BigDecimal disbursementChargeAmount,
            BigDecimal waivedChargeAmount) {
        BigDecimal chargeAmount = data.getChargeAmount() == null ? disbursementChargeAmount
                : disbursementChargeAmount.add(data.getChargeAmount()).subtract(waivedChargeAmount);
        return LoanSchedulePeriodData.disbursementOnlyPeriod(data.disbursementDate(), data.getPrincipal(), chargeAmount,
                data.isDisbursed());
    }

    private LoanSchedulePeriodData createLoanSchedulePeriodData(final LoanTransactionRepaymentPeriodData data) {
        BigDecimal feeCharges = Objects.isNull(data.getFeeChargesPortion()) ? BigDecimal.ZERO : data.getFeeChargesPortion();
        return LoanSchedulePeriodData.disbursementOnlyPeriod(data.getDate(), data.getAmount(), feeCharges, !data.isReversed());
    }

    private boolean canAddDisbursementData(DisbursementData data, boolean isDueForDisbursement, boolean excludePastUnDisbursed) {
        return (!excludePastUnDisbursed || data.isDisbursed() || !DateUtils.isBeforeBusinessDate(data.disbursementDate()))
                && isDueForDisbursement;
    }

    private boolean canAddCapitalizedIncomeData(LoanTransactionRepaymentPeriodData data, LocalDate fromDate, LocalDate dueDate) {
        return !data.isReversed() && DateUtils.isDateInRangeFromInclusiveToExclusive(fromDate, dueDate, data.getDate());
    }

}
