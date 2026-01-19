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

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.data.LoanPointInTimeData;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.arrears.LoanArrearsData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanPointInTimeServiceImpl implements LoanPointInTimeService {

    private final LoanUtilService loanUtilService;
    private final LoanScheduleService loanScheduleService;
    private final LoanAssembler loanAssembler;
    private final LoanPointInTimeData.Mapper dataMapper;
    private final EntityManager entityManager;
    private final LoanArrearsAgingService arrearsAgingService;

    @Override
    public LoanPointInTimeData retrieveAt(Long loanId, LocalDate date) {
        entityManager.setFlushMode(FlushModeType.COMMIT);
        validateSingularRetrieval(loanId, date);

        // Note: since everything is running in a readOnly transaction
        // whatever we modify on the loan is not going to be propagated to the DB
        // Note2: Interest is always calculated against the current date of the system so we need to roll time back
        HashMap<BusinessDateType, LocalDate> originalBDs = ThreadLocalContextUtil.getBusinessDates();
        try {
            ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, date)));

            Loan loan = loanAssembler.assembleFrom(loanId);

            int txCount = loan.getLoanTransactions().size();
            int chargeCount = loan.getCharges().size();
            removeAfterDateTransactions(loan, date);
            removeAfterDateCharges(loan, date);
            int afterRemovalTxCount = loan.getLoanTransactions().size();
            int afterRemovalChargeCount = loan.getCharges().size();

            boolean needsScheduleRegeneration = txCount != afterRemovalTxCount || chargeCount != afterRemovalChargeCount;

            if (needsScheduleRegeneration) {
                ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, null, null);
                loanScheduleService.regenerateScheduleWithReprocessingTransactions(loan, scheduleGeneratorDTO);
                recalculateSummaryForInstallmentsUpToDate(loan, date);
            } else if (!loan.isClosed()) {
                recalculateSummaryForInstallmentsUpToDate(loan, date);
            }

            LoanArrearsData arrearsData = arrearsAgingService.calculateArrearsForLoan(loan);

            LoanPointInTimeData result = dataMapper.map(loan);
            result.setArrears(arrearsData);
            return result;
        } finally {
            entityManager.clear();
            TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
            ThreadLocalContextUtil.setBusinessDates(originalBDs);
        }
    }

    private void removeAfterDateCharges(Loan loan, LocalDate date) {
        // Don't remove installment fees based on effectiveDueDate since they span multiple installments
        // For installment fees, effectiveDueDate returns the first UNPAID installment's due date,
        // which would incorrectly remove the entire fee even if some installments are already paid/due
        // The recalculateSummaryForInstallmentsUpToDate method handles installment fee adjustments separately
        loan.removeCharges(c -> !c.isInstalmentFee() && DateUtils.isAfter(c.getEffectiveDueDate(), date));
    }

    private void recalculateSummaryForInstallmentsUpToDate(Loan loan, LocalDate date) {
        var currency = loan.getCurrency();
        var summary = loan.getSummary();

        // Calculate fee charged based only on charges due by the specified date.
        // This excludes after-date charges and only includes installment fee portions for installments due by the date.
        Money feeChargedFromRemainingCharges = calculateTotalFeeChargedFromCharges(loan, date, currency);

        // Include fees due at disbursement which are always included regardless of date
        Money adjustedFeeCharged = feeChargedFromRemainingCharges.plus(summary.getTotalFeeChargesDueAtDisbursement(currency));

        // Only proceed with adjustment if the fee charged differs from summary
        if (adjustedFeeCharged.getAmount().compareTo(summary.getTotalFeeChargesCharged()) == 0) {
            return;
        }

        // Delegate to domain to recalculate all derived totals consistently
        summary.recalculateDerivedTotalsForAdjustedFeeCharged(adjustedFeeCharged.getAmount());
    }

    private Money calculateTotalFeeChargedFromCharges(Loan loan, LocalDate date, MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (LoanCharge charge : loan.getCharges()) {
            if (charge.isActive() && !charge.isPenaltyCharge() && !charge.isDueAtDisbursement()) {
                // For installment fees, calculate the portion up to the date
                if (charge.isInstalmentFee()) {
                    Money installmentTotal = calculateInstallmentFeeUpToDate(charge, date, currency);
                    total = total.plus(installmentTotal);
                } else {
                    // For one-time charges, include only if due on or before the date
                    LocalDate chargeDueDate = charge.getEffectiveDueDate();
                    if (chargeDueDate != null && !DateUtils.isAfter(chargeDueDate, date)) {
                        total = total.plus(charge.getAmount(currency));
                    }
                }
            }
        }
        return total;
    }

    private Money calculateInstallmentFeeUpToDate(LoanCharge charge, LocalDate date, MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (var installmentCharge : charge.installmentCharges()) {
            var installment = installmentCharge.getInstallment();
            if (installment != null && !DateUtils.isAfter(installment.getDueDate(), date)) {
                total = total.plus(installmentCharge.getAmount(currency));
            }
        }
        return total;
    }

    private void removeAfterDateTransactions(Loan loan, LocalDate date) {
        loan.removeLoanTransactions(tx -> DateUtils.isAfter(tx.getTransactionDate(), date));
    }

    private void validateSingularRetrieval(Long loanId, LocalDate date) {
        List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        baseDataValidator.reset().parameter("loanId").value(loanId).notNull();
        baseDataValidator.reset().parameter("date").value(date).notNull().notBlank();
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    @Override
    public List<LoanPointInTimeData> retrieveAt(List<Long> loanIds, LocalDate date) {
        validateBulkRetrieval(loanIds, date);
        List<LoanPointInTimeData> result = loanIds.stream().map(loanId -> retrieveAt(loanId, date)).toList();
        TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
        return result;
    }

    private void validateBulkRetrieval(List<Long> loanIds, LocalDate date) {
        List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        baseDataValidator.reset().parameter("loanIds").value(loanIds).notNull().listNotEmpty();
        baseDataValidator.reset().parameter("date").value(date).notNull().notBlank();
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
