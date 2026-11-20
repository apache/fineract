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

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanApplyOverdueChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanUpdateChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalcualtionAdditionalDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanOverdueInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.exception.LoanChargeDeactivationException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.DefaultScheduledDateGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.ScheduledDateGenerator;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentParameter;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentService;
import org.apache.fineract.portfolio.loanproduct.data.LoanOverdueDTO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies overdue installment penalties to loans and deactivates them.
 */
@Slf4j
@RequiredArgsConstructor
public class LoanOverdueChargeWritePlatformService {

    private final LoanAssembler loanAssembler;
    private final ChargeRepositoryWrapper chargeRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanAccountDomainService loanAccountDomainService;
    private final LoanChargeRepository loanChargeRepository;
    private final LoanWritePlatformService loanWritePlatformService;
    private final LoanUtilService loanUtilService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final FromJsonHelper fromApiJsonHelper;
    private final ConfigurationDomainService configurationDomainService;
    private final LoanChargeAssembler loanChargeAssembler;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;
    private final LoanScheduleService loanScheduleService;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final LoanAccountService loanAccountService;
    private final LoanAdjustmentService loanAdjustmentService;
    private final LoanChargeWriteHelper loanChargeWriteHelper;

    @Transactional
    public void applyOverdueChargesForLoan(final Long loanId, Collection<OverdueLoanScheduleData> overdueLoanScheduleDataList) {
        if (overdueLoanScheduleDataList.isEmpty()) {
            return;
        }
        Loan loan = this.loanAssembler.assembleFrom(loanId);
        if (loan.isChargedOff()) {
            log.warn("Adding charge to Loan: {} is not allowed. Loan Account is Charged-off", loanId);
            return;
        }
        Optional<Charge> optPenaltyCharge = loan.getLoanProduct().getCharges().stream()
                .filter((e) -> ChargeTimeType.OVERDUE_INSTALLMENT.getValue().equals(e.getChargeTimeType()) && e.isLoanCharge()).findFirst();
        if (optPenaltyCharge.isEmpty()) {
            return;
        }
        boolean runInterestRecalculation = false;
        LocalDate recalculateFrom = DateUtils.getBusinessLocalDate();
        LocalDate lastChargeDate = null;
        final List<LoanCharge> appliedCharges = new ArrayList<>();
        for (final OverdueLoanScheduleData overdueInstallment : overdueLoanScheduleDataList) {

            final JsonElement parsedCommand = this.fromApiJsonHelper.parse(overdueInstallment.toString());
            final JsonCommand command = JsonCommand.from(overdueInstallment.toString(), parsedCommand, this.fromApiJsonHelper, null, null,
                    null, null, null, loanId, null, null, null, null, null, null, null, null);

            LoanOverdueDTO overdueDTO = applyChargeToOverdueLoanInstallment(loan, overdueInstallment.getChargeId(),
                    overdueInstallment.getPeriodNumber(), command);

            loan = overdueDTO.getLoan();
            runInterestRecalculation = runInterestRecalculation || overdueDTO.isRunInterestRecalculation();
            if (DateUtils.isAfter(recalculateFrom, overdueDTO.getRecalculateFrom())) {
                recalculateFrom = overdueDTO.getRecalculateFrom();
            }
            if (lastChargeDate == null || DateUtils.isAfter(overdueDTO.getLastChargeAppliedDate(), lastChargeDate)) {
                lastChargeDate = overdueDTO.getLastChargeAppliedDate();
            }
            appliedCharges.addAll(overdueDTO.getAppliedCharges());
        }
        if (loan != null) {
            boolean reprocessRequired = true;
            LocalDate recalculatedTill = loan.fetchInterestRecalculateFromDate();
            if (DateUtils.isAfter(recalculateFrom, recalculatedTill)) {
                recalculateFrom = recalculatedTill;
            }

            if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
                if (runInterestRecalculation && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
                    loan = this.loanChargeWriteHelper.runScheduleRecalculation(loan, recalculateFrom);
                    reprocessRequired = false;
                }
                this.loanWritePlatformService.updateOriginalSchedule(loan);
            }

            if (reprocessRequired) {
                addInstallmentIfPenaltyAppliedAfterLastDueDate(loan, lastChargeDate);
                if (loan.isProgressiveSchedule()) {
                    final ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, null);
                    loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
                }
                reprocessLoanTransactionsService.reprocessTransactions(loan);
                loan = loanAccountService.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
            }
            loanAccrualsProcessingService.processRealTimeChargeAccruals(loan, appliedCharges);

            if (loan.isInterestBearingAndInterestRecalculationEnabled() && runInterestRecalculation
                    && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
                loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan, true, true);
            }
            this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());
        }
    }

    @Transactional
    public CommandProcessingResult deactivateOverdueLoanCharge(Long loanId, JsonCommand command) {
        LocalDate fromDueDate = command.dateValueOfParameterNamed("dueDate");

        List<LoanCharge> loanCharges = loanChargeRepository.findByLoanIdAndFromDueDate(loanId, fromDueDate);
        loanCharges.forEach(this::inactivateOverdueLoanCharge);

        Loan loan = loanAssembler.assembleFromInUserScope(loanId);
        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = loan
                .getRepaymentScheduleInstallments(si -> DateUtils.isDateInRangeInclusive(fromDueDate, si.getFromDate(), si.getDueDate())
                        || DateUtils.isAfter(si.getFromDate(), fromDueDate));
        repaymentScheduleInstallments.forEach(si -> si.setPenaltyAccrued(null));
        List<LoanTransaction> accrualsToReverse = loan.getLoanTransactions(
                tx -> tx.isNotReversed() && DateUtils.isAfterInclusive(tx.getTransactionDate(), fromDueDate) && tx.isAccrualRelated());
        accrualsToReverse.forEach(tx -> loanAdjustmentService.adjustLoanTransaction(loan, tx,
                LoanAdjustmentParameter.builder().transactionDate(tx.getTransactionDate()).build(), null, new HashMap<>()));

        loanRepositoryWrapper.saveAndFlush(loan);

        final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();
        return commandProcessingResultBuilder.withLoanId(loanId) //
                .withEntityId(loanId) //
                .withEntityExternalId(loan.getExternalId()) //
                .build();
    }

    private void inactivateOverdueLoanCharge(LoanCharge loanCharge) {
        if (!loanCharge.getChargeTimeType().isOverdueInstallment()) {
            throw new LoanChargeDeactivationException("loan.charge.deactivate.invalid.charge.type",
                    "Loan charge is not an overdue installment charge");
        }

        if (!loanCharge.isActive()) {
            throw new LoanChargeDeactivationException("loan.charge.deactivate.invalid.status", "Loan charge is not active");
        }

        loanCharge.setActive(false);
        loanChargeRepository.saveAndFlush(loanCharge);

        businessEventNotifierService.notifyPostBusinessEvent(new LoanUpdateChargeBusinessEvent(loanCharge));
    }

    private LoanOverdueDTO applyChargeToOverdueLoanInstallment(final Loan loan, final Long loanChargeId, final Integer periodNumber,
            final JsonCommand command) {
        boolean runInterestRecalculation = false;
        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(loanChargeId);

        Collection<Integer> frequencyNumbers = loanChargeReadPlatformService.retrieveOverdueInstallmentChargeFrequencyNumber(loan,
                chargeDefinition, periodNumber);

        Integer feeFrequency = chargeDefinition.feeFrequency();
        final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
        Map<Integer, LocalDate> scheduleDates = new HashMap<>();
        final Long penaltyWaitPeriodValue = this.configurationDomainService.retrievePenaltyWaitPeriod();
        final Long penaltyPostingWaitPeriodValue = this.configurationDomainService.retrieveGraceOnPenaltyPostingPeriod();
        final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
        long diff = penaltyWaitPeriodValue + 1 - penaltyPostingWaitPeriodValue;
        if (diff < 1) {
            diff = 1L;
        }
        LocalDate startDate = dueDate.plusDays(penaltyWaitPeriodValue + 1L);
        int frequencyNumber = 1;
        if (feeFrequency == null) {
            scheduleDates.put(frequencyNumber++, startDate.minusDays(diff));
        } else {
            while (!DateUtils.isDateInTheFuture(startDate)) {
                scheduleDates.put(frequencyNumber++, startDate.minusDays(diff));

                startDate = scheduledDateGenerator.getRepaymentPeriodDate(PeriodFrequencyType.fromInt(feeFrequency),
                        chargeDefinition.feeInterval(), startDate);
            }
        }

        for (Integer frequency : frequencyNumbers) {
            scheduleDates.remove(frequency);
        }

        LoanRepaymentScheduleInstallment installment = null;
        LocalDate lastChargeAppliedDate = dueDate;
        final List<LoanCharge> appliedCharges = new ArrayList<>();
        LocalDate recalculateFrom = DateUtils.getBusinessLocalDate();
        if (!scheduleDates.isEmpty()) {
            installment = loan.fetchRepaymentScheduleInstallment(periodNumber);
            lastChargeAppliedDate = installment.getDueDate();
            businessEventNotifierService.notifyPreBusinessEvent(new LoanApplyOverdueChargeBusinessEvent(loan));

            for (Map.Entry<Integer, LocalDate> entry : scheduleDates.entrySet()) {

                final LoanCharge loanCharge = loanChargeAssembler.createNewFromJson(loan, chargeDefinition, command, entry.getValue());

                if (BigDecimal.ZERO.compareTo(loanCharge.amount()) == 0) {
                    continue;
                }
                LoanOverdueInstallmentCharge overdueInstallmentCharge = new LoanOverdueInstallmentCharge(loanCharge, installment,
                        entry.getKey());
                loanCharge.updateOverdueInstallmentCharge(overdueInstallmentCharge);

                boolean isAppliedOnBackDate = this.loanChargeWriteHelper.addCharge(loan, chargeDefinition, loanCharge);
                appliedCharges.add(loanCharge);
                runInterestRecalculation = runInterestRecalculation || isAppliedOnBackDate;
                if (DateUtils.isBefore(entry.getValue(), recalculateFrom)) {
                    recalculateFrom = entry.getValue();
                }
                if (DateUtils.isAfter(entry.getValue(), lastChargeAppliedDate)) {
                    lastChargeAppliedDate = entry.getValue();
                }
            }
            businessEventNotifierService.notifyPostBusinessEvent(new LoanApplyOverdueChargeBusinessEvent(loan));
            businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        }

        return new LoanOverdueDTO(loan, runInterestRecalculation, recalculateFrom, lastChargeAppliedDate, appliedCharges);
    }

    private void addInstallmentIfPenaltyAppliedAfterLastDueDate(Loan loan, LocalDate lastChargeDate) {
        if (lastChargeDate != null) {
            List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
            LoanRepaymentScheduleInstallment lastInstallment = loan.fetchRepaymentScheduleInstallment(installments.size());
            if (DateUtils.isAfter(lastChargeDate, lastInstallment.getDueDate())) {
                if (lastInstallment.isRecalculatedInterestComponent()) {
                    installments.remove(lastInstallment);
                    lastInstallment = loan.fetchRepaymentScheduleInstallment(installments.size());
                }
                boolean recalculatedInterestComponent = true;
                BigDecimal principal = BigDecimal.ZERO;
                BigDecimal interest = BigDecimal.ZERO;
                BigDecimal feeCharges = BigDecimal.ZERO;
                BigDecimal penaltyCharges = BigDecimal.ONE;
                final Set<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails = null;
                LoanRepaymentScheduleInstallment newEntry = new LoanRepaymentScheduleInstallment(loan, installments.size() + 1,
                        lastInstallment.getDueDate(), lastChargeDate, principal, interest, feeCharges, penaltyCharges,
                        recalculatedInterestComponent, compoundingDetails);
                loan.addLoanRepaymentScheduleInstallment(newEntry);
            }
        }
    }
}
