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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.workingdays.data.AdjustedDateDetailsDTO;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleDTO;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleModelDownPaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleParams;
import org.apache.fineract.portfolio.loanaccount.loanschedule.exception.MultiDisbursementOutstandingAmoutException;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculationResult;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType;

public abstract class AbstractProgressiveLoanScheduleGenerator implements LoanScheduleGenerator {

    @Override
    public LoanScheduleModel generate(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            final Set<LoanCharge> loanCharges, final HolidayDetailDTO holidayDetailDTO) {
        // TODO: handle interest calculation
        final ApplicationCurrency applicationCurrency = loanApplicationTerms.getApplicationCurrency();
        // generate list of proposed schedule due dates
        LocalDate loanEndDate = getScheduledDateGenerator().getLastRepaymentDate(loanApplicationTerms, holidayDetailDTO);
        LoanTermVariationsData lastDueDateVariation = loanApplicationTerms.getLoanTermVariations()
                .fetchLoanTermDueDateVariationsData(loanEndDate);
        if (lastDueDateVariation != null) {
            loanEndDate = lastDueDateVariation.getDateValue();
        }
        loanApplicationTerms.updateLoanEndDate(loanEndDate);

        // determine the total charges due at time of disbursement
        final BigDecimal chargesDueAtTimeOfDisbursement = deriveTotalChargesDueAtTimeOfDisbursement(loanCharges);

        // setup variables for tracking important facts required for loan
        // schedule generation.

        final MonetaryCurrency currency = loanApplicationTerms.getCurrency();
        LocalDate periodStartDate = RepaymentStartDateType.DISBURSEMENT_DATE.equals(loanApplicationTerms.getRepaymentStartDateType())
                ? loanApplicationTerms.getExpectedDisbursementDate()
                : loanApplicationTerms.getSubmittedOnDate();

        LoanScheduleParams scheduleParams = LoanScheduleParams.createLoanScheduleParams(currency,
                Money.of(currency, chargesDueAtTimeOfDisbursement), periodStartDate, Money.zero(currency));

        List<LoanScheduleModelPeriod> periods = createNewLoanScheduleListWithDisbursementDetails(loanApplicationTerms, scheduleParams,
                chargesDueAtTimeOfDisbursement);

        EMICalculationResult emiCalculationResult = null;
        boolean isFirstRepayment = true;

        // charges which depends on total loan interest will be added to this
        // set and handled separately after all installments generated
        final Set<LoanCharge> nonCompoundingCharges = separateTotalCompoundingPercentageCharges(loanCharges);
        boolean isNextRepaymentAvailable = true;

        boolean thereIsDisbursementBeforeOrOnLoanEndDate = scheduleParams.getDisburseDetailMap().entrySet().stream()
                .anyMatch(d -> !d.getKey().isAfter(loanApplicationTerms.getLoanEndDate()));
        while (!scheduleParams.getOutstandingBalance().isZero() || thereIsDisbursementBeforeOrOnLoanEndDate) {
            scheduleParams.setActualRepaymentDate(getScheduledDateGenerator().generateNextRepaymentDate(
                    scheduleParams.getActualRepaymentDate(), loanApplicationTerms, isFirstRepayment, scheduleParams.getPeriodNumber()));
            AdjustedDateDetailsDTO adjustedDateDetailsDTO = getScheduledDateGenerator()
                    .adjustRepaymentDate(scheduleParams.getActualRepaymentDate(), loanApplicationTerms, holidayDetailDTO);
            scheduleParams.setActualRepaymentDate(adjustedDateDetailsDTO.getChangedActualRepaymentDate());
            LocalDate scheduledDueDate = adjustedDateDetailsDTO.getChangedScheduleDate();

            // Loan Schedule Exceptions that need to be applied for Loan Account
            LoanTermVariationParams termVariationParams = applyLoanTermVariations(loanApplicationTerms, scheduleParams, scheduledDueDate);
            scheduledDueDate = termVariationParams.scheduledDueDate();

            // Updates total days in term
            scheduleParams
                    .addLoanTermInDays(Math.toIntExact(ChronoUnit.DAYS.between(scheduleParams.getPeriodStartDate(), scheduledDueDate)));

            ScheduleCurrentPeriodParams currentPeriodParams = new ScheduleCurrentPeriodParams(currency, BigDecimal.ZERO);
            final boolean hasAnyProcessedDisbursement = processDisbursements(loanApplicationTerms, chargesDueAtTimeOfDisbursement,
                    scheduleParams, periods, scheduledDueDate);
            if (isFirstRepayment || hasAnyProcessedDisbursement) {
                final LocalDate startDate = isFirstRepayment ? loanApplicationTerms.getRepaymentStartDate()
                        : scheduleParams.getPeriodStartDate();
                List<PreGeneratedLoanSchedulePeriod> expectedRepaymentPeriods = getScheduledDateGenerator()
                        .generateRepaymentPeriods(startDate, loanApplicationTerms, holidayDetailDTO);
                emiCalculationResult = getEMICalculator().calculateEMIValueAndRateFactors(scheduleParams.getOutstandingBalanceAsPerRest(),
                        loanApplicationTerms.toLoanProductRelatedDetail(), expectedRepaymentPeriods, scheduleParams.getPeriodNumber(),
                        loanApplicationTerms.getNumberOfRepayments(), mc);
            }

            // 5 determine principal,interest of repayment period
            PrincipalInterest principalInterestForThisPeriod = getEMICalculator().calculatePrincipalInterestComponentsForPeriod(
                    emiCalculationResult, scheduleParams.getOutstandingBalanceAsPerRest(),
                    loanApplicationTerms.getInstallmentAmountInMultiplesOf(), scheduleParams.getPeriodNumber(),
                    loanApplicationTerms.getActualNoOfRepaymnets(), mc);

            // update cumulative fields for principal
            currentPeriodParams.setPrincipalForThisPeriod(principalInterestForThisPeriod.principal());
            currentPeriodParams.setInterestForThisPeriod(principalInterestForThisPeriod.interest());
            updateOutstandingBalance(scheduleParams, currentPeriodParams);

            if (scheduleParams.getOutstandingBalance().isLessThanZero() || !isNextRepaymentAvailable) {
                currentPeriodParams.plusPrincipalForThisPeriod(scheduleParams.getOutstandingBalance());
                scheduleParams.setOutstandingBalance(Money.zero(currency));
            }

            if (!isNextRepaymentAvailable) {
                scheduleParams.getDisburseDetailMap().clear();
            }

            // applies charges for the period
            applyChargesForCurrentPeriod(loanCharges, currency, scheduleParams, scheduledDueDate, currentPeriodParams, mc);

            // sum up real totalInstallmentDue from components
            final Money totalInstallmentDue = currentPeriodParams.fetchTotalAmountForPeriod();

            // if previous installment is last then add interest to same
            // installment
            if (currentPeriodParams.getLastInstallment() != null && currentPeriodParams.getPrincipalForThisPeriod().isZero()) {
                currentPeriodParams.getLastInstallment().addInterestAmount(currentPeriodParams.getInterestForThisPeriod());
                continue;
            }

            // create repayment period from parts
            LoanScheduleModelPeriod installment = LoanScheduleModelRepaymentPeriod.repayment(scheduleParams.getInstalmentNumber(),
                    scheduleParams.getPeriodStartDate(), scheduledDueDate, currentPeriodParams.getPrincipalForThisPeriod(),
                    scheduleParams.getOutstandingBalance(), currentPeriodParams.getInterestForThisPeriod(),
                    currentPeriodParams.getFeeChargesForInstallment(), currentPeriodParams.getPenaltyChargesForInstallment(),
                    totalInstallmentDue, false);
            if (principalInterestForThisPeriod.getRescheduleInterestPortion() != null) {
                installment.setRescheduleInterestPortion(principalInterestForThisPeriod.getRescheduleInterestPortion().getAmount());
            }
            addLoanRepaymentScheduleInstallment(scheduleParams.getInstallments(), installment);

            if (loanApplicationTerms.getCurrentPeriodFixedEmiAmount() != null) {
                installment.setEMIFixedSpecificToInstallmentTrue();
            }

            periods.add(installment);

            // Updates principal paid map with effective date for reducing
            // the amount from outstanding balance(interest calculation)
            updateAmountsWithEffectiveDate(loanApplicationTerms, holidayDetailDTO, scheduleParams, scheduledDueDate, currentPeriodParams,
                    installment);

            // handle cumulative fields

            scheduleParams.addTotalCumulativePrincipal(currentPeriodParams.getPrincipalForThisPeriod());
            scheduleParams.addTotalRepaymentExpected(totalInstallmentDue);
            scheduleParams.addTotalCumulativeInterest(currentPeriodParams.getInterestForThisPeriod());
            scheduleParams.setPeriodStartDate(scheduledDueDate);
            scheduleParams.incrementInstalmentNumber();
            scheduleParams.incrementPeriodNumber();
            // if (termVariationParams.isRecalculateAmounts()) {
            // loanApplicationTerms.setCurrentPeriodFixedEmiAmount(null);
            // loanApplicationTerms.setCurrentPeriodFixedPrincipalAmount(null);
            // adjustInstallmentOrPrincipalAmount(loanApplicationTerms, scheduleParams.getTotalCumulativePrincipal(),
            // scheduleParams.getPeriodNumber(), mc);
            // }
            thereIsDisbursementBeforeOrOnLoanEndDate = scheduleParams.getDisburseDetailMap().entrySet().stream()
                    .anyMatch(d -> !d.getKey().isAfter(loanApplicationTerms.getLoanEndDate()));
            isFirstRepayment = false;
        }

        // If the disbursement happened after maturity date
        if (loanApplicationTerms.isMultiDisburseLoan()) {
            processDisbursements(loanApplicationTerms, chargesDueAtTimeOfDisbursement, scheduleParams, periods,
                    DateUtils.getBusinessLocalDate().plusDays(1));
        }

        // determine fees and penalties for charges which depends on total
        // loan interest
        updatePeriodsWithCharges(currency, scheduleParams, periods, nonCompoundingCharges, mc);

        final BigDecimal totalPrincipalPaid = BigDecimal.ZERO;
        final BigDecimal totalOutstanding = BigDecimal.ZERO;

        return LoanScheduleModel.from(periods, applicationCurrency, scheduleParams.getLoanTermInDays(),
                scheduleParams.getPrincipalToBeScheduled().plus(loanApplicationTerms.getDownPaymentAmount()),
                scheduleParams.getTotalCumulativePrincipal().plus(loanApplicationTerms.getDownPaymentAmount()).getAmount(),
                totalPrincipalPaid, scheduleParams.getTotalCumulativeInterest().getAmount(),
                scheduleParams.getTotalFeeChargesCharged().getAmount(), scheduleParams.getTotalPenaltyChargesCharged().getAmount(),
                scheduleParams.getTotalRepaymentExpected().getAmount(), totalOutstanding);
    }

    @Override
    public LoanScheduleDTO rescheduleNextInstallments(MathContext mc, LoanApplicationTerms loanApplicationTerms, Loan loan,
            HolidayDetailDTO holidayDetailDTO, LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor,
            LocalDate rescheduleFrom) {

        LoanScheduleModel model = generate(mc, loanApplicationTerms, loan.getActiveCharges(), holidayDetailDTO);
        return LoanScheduleDTO.from(null, model);
    }

    @Override
    public LoanRepaymentScheduleInstallment calculatePrepaymentAmount(MonetaryCurrency currency, LocalDate onDate,
            LoanApplicationTerms loanApplicationTerms, MathContext mc, Loan loan, HolidayDetailDTO holidayDetailDTO,
            LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor) {
        return null;
    }

    // Abstract methods
    public abstract ScheduledDateGenerator getScheduledDateGenerator();

    public abstract PaymentPeriodsInOneYearCalculator getPaymentPeriodsInOneYearCalculator();

    protected abstract EMICalculator getEMICalculator();

    // Private, internal methods
    private BigDecimal deriveTotalChargesDueAtTimeOfDisbursement(final Set<LoanCharge> loanCharges) {
        BigDecimal chargesDueAtTimeOfDisbursement = BigDecimal.ZERO;
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueAtDisbursement()) {
                chargesDueAtTimeOfDisbursement = chargesDueAtTimeOfDisbursement.add(loanCharge.amount());
            }
        }
        return chargesDueAtTimeOfDisbursement;
    }

    private List<LoanScheduleModelPeriod> createNewLoanScheduleListWithDisbursementDetails(final LoanApplicationTerms loanApplicationTerms,
            final LoanScheduleParams loanScheduleParams, final BigDecimal chargesDueAtTimeOfDisbursement) {
        List<LoanScheduleModelPeriod> periods = new ArrayList<>();

        // In case of `disallowExpectedDisbursementDetails = true`, anyway at least 1 disbursement details must exist
        if (loanApplicationTerms.getDisbursementDatas().isEmpty()) {
            loanApplicationTerms.getDisbursementDatas()
                    .add(new DisbursementData(1L, loanApplicationTerms.getExpectedDisbursementDate(),
                            loanApplicationTerms.getExpectedDisbursementDate(), loanApplicationTerms.getPrincipal().getAmount(), null, null,
                            null, null));
        }
        for (DisbursementData disbursementData : loanApplicationTerms.getDisbursementDatas()) {
            Money principalMoney = Money.of(loanApplicationTerms.getCurrency(), disbursementData.getPrincipal());
            if (disbursementData.disbursementDate().equals(loanScheduleParams.getPeriodStartDate())) {
                final LoanScheduleModelDisbursementPeriod disbursementPeriod = LoanScheduleModelDisbursementPeriod.disbursement(
                        disbursementData.disbursementDate(), Money.of(loanScheduleParams.getCurrency(), disbursementData.getPrincipal()),
                        chargesDueAtTimeOfDisbursement);
                periods.add(disbursementPeriod);
                loanScheduleParams.addOutstandingBalance(principalMoney);
                loanScheduleParams.addOutstandingBalanceAsPerRest(principalMoney);
                loanScheduleParams.addPrincipalToBeScheduled(principalMoney);
                loanApplicationTerms.resetFixedEmiAmount();
                if (loanApplicationTerms.isDownPaymentEnabled()) {
                    final LoanScheduleModelDownPaymentPeriod downPaymentPeriod = createDownPaymentPeriod(loanApplicationTerms,
                            loanScheduleParams, loanApplicationTerms.getExpectedDisbursementDate(), disbursementData.getPrincipal());
                    periods.add(downPaymentPeriod);
                }
            } else {
                Money disbursedAmount = loanScheduleParams.getDisburseDetailMap().getOrDefault(disbursementData.disbursementDate(),
                        Money.zero(loanApplicationTerms.getCurrency()));
                loanScheduleParams.getDisburseDetailMap().put(disbursementData.disbursementDate(), disbursedAmount.add(principalMoney));
            }
        }

        return periods;
    }

    private LoanScheduleModelDownPaymentPeriod createDownPaymentPeriod(LoanApplicationTerms loanApplicationTerms,
            LoanScheduleParams scheduleParams, LocalDate date, BigDecimal periodBaseAmount) {
        Money downPaymentAmount = Money.of(loanApplicationTerms.getCurrency(),
                MathUtil.percentageOf(periodBaseAmount, loanApplicationTerms.getDisbursedAmountPercentageForDownPayment(), 19));
        if (loanApplicationTerms.getInstallmentAmountInMultiplesOf() != null) {
            downPaymentAmount = Money.roundToMultiplesOf(downPaymentAmount, loanApplicationTerms.getInstallmentAmountInMultiplesOf());
        }
        LoanScheduleModelDownPaymentPeriod installment = LoanScheduleModelDownPaymentPeriod.downPayment(
                scheduleParams.getInstalmentNumber(), date, downPaymentAmount,
                scheduleParams.getOutstandingBalance().minus(downPaymentAmount));

        addLoanRepaymentScheduleInstallment(scheduleParams.getInstallments(), installment);

        scheduleParams.incrementInstalmentNumber();
        scheduleParams.addTotalRepaymentExpected(downPaymentAmount);
        scheduleParams.reduceOutstandingBalance(downPaymentAmount);
        scheduleParams.reduceOutstandingBalanceAsPerRest(downPaymentAmount);
        scheduleParams.setPrincipalToBeScheduled(scheduleParams.getPrincipalToBeScheduled().minus(downPaymentAmount));
        loanApplicationTerms.setPrincipal(loanApplicationTerms.getPrincipal().minus(downPaymentAmount));
        loanApplicationTerms.resetFixedEmiAmount();
        return installment;
    }

    private LoanRepaymentScheduleInstallment addLoanRepaymentScheduleInstallment(final List<LoanRepaymentScheduleInstallment> installments,
            final LoanScheduleModelPeriod scheduledLoanInstallment) {
        LoanRepaymentScheduleInstallment installment = null;
        if (scheduledLoanInstallment.isRepaymentPeriod() || scheduledLoanInstallment.isDownPaymentPeriod()) {
            installment = new LoanRepaymentScheduleInstallment(null, scheduledLoanInstallment.periodNumber(),
                    scheduledLoanInstallment.periodFromDate(), scheduledLoanInstallment.periodDueDate(),
                    scheduledLoanInstallment.principalDue(), scheduledLoanInstallment.interestDue(),
                    scheduledLoanInstallment.feeChargesDue(), scheduledLoanInstallment.penaltyChargesDue(),
                    scheduledLoanInstallment.isRecalculatedInterestComponent(), scheduledLoanInstallment.getLoanCompoundingDetails(),
                    scheduledLoanInstallment.rescheduleInterestPortion(), scheduledLoanInstallment.isDownPaymentPeriod());
            installments.add(installment);
        }
        return installment;
    }

    /**
     * Method add extra disbursement periods (if applicable) and update the schedule params
     */
    private boolean processDisbursements(final LoanApplicationTerms loanApplicationTerms, final BigDecimal chargesDueAtTimeOfDisbursement,
            LoanScheduleParams scheduleParams, final Collection<LoanScheduleModelPeriod> periods, final LocalDate scheduledDueDate) {
        Iterator<Map.Entry<LocalDate, Money>> iter = scheduleParams.getDisburseDetailMap().entrySet().iterator();
        boolean hasProcessedDisbursement = false;
        while (iter.hasNext()) {
            Map.Entry<LocalDate, Money> disburseDetail = iter.next();
            if ((disburseDetail.getKey().isEqual(scheduleParams.getPeriodStartDate())
                    || disburseDetail.getKey().isAfter(scheduleParams.getPeriodStartDate()))
                    && disburseDetail.getKey().isBefore(scheduledDueDate)) {
                // validation check for amount not exceeds specified max
                // amount as per the configuration
                if (loanApplicationTerms.isMultiDisburseLoan() && loanApplicationTerms.getMaxOutstandingBalance() != null) {
                    Money maxOutstandingBalance = loanApplicationTerms.getMaxOutstandingBalanceMoney();
                    if (scheduleParams.getOutstandingBalance().plus(disburseDetail.getValue()).isGreaterThan(maxOutstandingBalance)) {
                        String errorMsg = "Outstanding balance must not exceed the amount: " + maxOutstandingBalance;
                        throw new MultiDisbursementOutstandingAmoutException(errorMsg, loanApplicationTerms.getMaxOutstandingBalance(),
                                disburseDetail.getValue());
                    }
                }

                // creates and add disbursement detail to the repayments
                // period
                final LoanScheduleModelDisbursementPeriod disbursementPeriod = LoanScheduleModelDisbursementPeriod
                        .disbursement(disburseDetail.getKey(), disburseDetail.getValue(), chargesDueAtTimeOfDisbursement);
                periods.add(disbursementPeriod);

                BigDecimal downPaymentAmt = BigDecimal.ZERO;
                // updates actual outstanding balance with new
                // disbursement detail
                scheduleParams.addOutstandingBalance(disburseDetail.getValue());
                scheduleParams.addOutstandingBalanceAsPerRest(disburseDetail.getValue());
                scheduleParams.addPrincipalToBeScheduled(disburseDetail.getValue());
                loanApplicationTerms.setPrincipal(loanApplicationTerms.getPrincipal().plus(disburseDetail.getValue()));
                loanApplicationTerms.resetFixedEmiAmount();
                if (loanApplicationTerms.isDownPaymentEnabled()) {
                    // get list of disbursements done on same day and create down payment periods
                    List<DisbursementData> disbursementsOnSameDate = loanApplicationTerms.getDisbursementDatas().stream()
                            .filter(disbursementData -> DateUtils.isEqual(disbursementData.disbursementDate(), disburseDetail.getKey()))
                            .toList();
                    for (DisbursementData disbursementData : disbursementsOnSameDate) {
                        final LoanScheduleModelDownPaymentPeriod downPaymentPeriod = createDownPaymentPeriod(loanApplicationTerms,
                                scheduleParams, disbursementData.disbursementDate(), disbursementData.getPrincipal());
                        periods.add(downPaymentPeriod);
                        downPaymentAmt = downPaymentAmt.add(downPaymentPeriod.principalDue());
                    }
                }
                iter.remove();
                hasProcessedDisbursement = true;
            }
        }
        return hasProcessedDisbursement;
    }

    private void applyChargesForCurrentPeriod(final Set<LoanCharge> loanCharges, final MonetaryCurrency currency,
            LoanScheduleParams scheduleParams, LocalDate scheduledDueDate, ScheduleCurrentPeriodParams currentPeriodParams,
            final MathContext mc) {
        PrincipalInterest principalInterest = new PrincipalInterest(currentPeriodParams.getPrincipalForThisPeriod(),
                currentPeriodParams.getInterestForThisPeriod(), null);
        currentPeriodParams.setFeeChargesForInstallment(cumulativeFeeChargesDueWithin(scheduleParams.getPeriodStartDate(), scheduledDueDate,
                loanCharges, currency, principalInterest, scheduleParams.getPrincipalToBeScheduled(),
                scheduleParams.getTotalCumulativeInterest(), true, scheduleParams.isFirstPeriod(), mc));
        currentPeriodParams.setPenaltyChargesForInstallment(cumulativePenaltyChargesDueWithin(scheduleParams.getPeriodStartDate(),
                scheduledDueDate, loanCharges, currency, principalInterest, scheduleParams.getPrincipalToBeScheduled(),
                scheduleParams.getTotalCumulativeInterest(), true, scheduleParams.isFirstPeriod(), mc));
        scheduleParams.addTotalFeeChargesCharged(currentPeriodParams.getFeeChargesForInstallment());
        scheduleParams.addTotalPenaltyChargesCharged(currentPeriodParams.getPenaltyChargesForInstallment());
    }

    private Money cumulativeFeeChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd, final Set<LoanCharge> loanCharges,
            final MonetaryCurrency monetaryCurrency, final PrincipalInterest principalInterestForThisPeriod, final Money principalDisbursed,
            final Money totalInterestChargedForFullLoanTerm, boolean isInstallmentChargeApplicable, final boolean isFirstPeriod,
            final MathContext mc) {
        Money cumulative = Money.zero(monetaryCurrency);
        for (final LoanCharge loanCharge : loanCharges) {
            if (!loanCharge.isDueAtDisbursement() && loanCharge.isFeeCharge()) {
                cumulative = getCumulativeAmountOfCharge(periodStart, periodEnd, principalInterestForThisPeriod, principalDisbursed,
                        totalInterestChargedForFullLoanTerm, isInstallmentChargeApplicable, isFirstPeriod, loanCharge, cumulative, mc);
            }
        }
        return cumulative;
    }

    private Money getCumulativeAmountOfCharge(LocalDate periodStart, LocalDate periodEnd, PrincipalInterest principalInterestForThisPeriod,
            Money principalDisbursed, Money totalInterestChargedForFullLoanTerm, boolean isInstallmentChargeApplicable,
            boolean isFirstPeriod, LoanCharge loanCharge, Money cumulative, MathContext mc) {
        boolean isDue = isFirstPeriod ? loanCharge.isDueForCollectionFromIncludingAndUpToAndIncluding(periodStart, periodEnd)
                : loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd);
        if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
            cumulative = calculateInstallmentCharge(principalInterestForThisPeriod, cumulative, loanCharge, mc);
        } else if (loanCharge.isOverdueInstallmentCharge() && isDue && loanCharge.getChargeCalculation().isPercentageBased()) {
            cumulative = cumulative.plus(loanCharge.chargeAmount());
        } else if (isDue && loanCharge.getChargeCalculation().isPercentageBased()) {
            cumulative = calculateSpecificDueDateChargeWithPercentage(principalDisbursed, totalInterestChargedForFullLoanTerm, cumulative,
                    loanCharge, mc);
        } else if (isDue) {
            cumulative = cumulative.plus(loanCharge.amount());
        }
        return cumulative;
    }

    private Money cumulativePenaltyChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency monetaryCurrency,
            final PrincipalInterest principalInterestForThisPeriod, final Money principalDisbursed,
            final Money totalInterestChargedForFullLoanTerm, boolean isInstallmentChargeApplicable, final boolean isFirstPeriod,
            final MathContext mc) {
        Money cumulative = Money.zero(monetaryCurrency);
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                cumulative = getCumulativeAmountOfCharge(periodStart, periodEnd, principalInterestForThisPeriod, principalDisbursed,
                        totalInterestChargedForFullLoanTerm, isInstallmentChargeApplicable, isFirstPeriod, loanCharge, cumulative, mc);
            }
        }
        return cumulative;
    }

    private Money calculateInstallmentCharge(final PrincipalInterest principalInterestForThisPeriod, Money cumulative,
            final LoanCharge loanCharge, final MathContext mc) {
        if (loanCharge.getChargeCalculation().isPercentageBased()) {
            BigDecimal amount = BigDecimal.ZERO;
            if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
                amount = amount.add(principalInterestForThisPeriod.principal().getAmount())
                        .add(principalInterestForThisPeriod.interest().getAmount());
            } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
                amount = amount.add(principalInterestForThisPeriod.interest().getAmount());
            } else {
                amount = amount.add(principalInterestForThisPeriod.principal().getAmount());
            }
            BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100), mc);
            cumulative = cumulative.plus(loanChargeAmt);
        } else {
            cumulative = cumulative.plus(loanCharge.amountOrPercentage());
        }
        return cumulative;
    }

    private Money calculateSpecificDueDateChargeWithPercentage(final Money principalDisbursed,
            final Money totalInterestChargedForFullLoanTerm, Money cumulative, final LoanCharge loanCharge, final MathContext mc) {
        BigDecimal amount = BigDecimal.ZERO;
        if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
            amount = amount.add(principalDisbursed.getAmount()).add(totalInterestChargedForFullLoanTerm.getAmount());
        } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
            amount = amount.add(totalInterestChargedForFullLoanTerm.getAmount());
        } else {
            amount = amount.add(principalDisbursed.getAmount());
        }
        BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100), mc);
        cumulative = cumulative.plus(loanChargeAmt);
        return cumulative;
    }

    private void updateAmountsWithEffectiveDate(final LoanApplicationTerms loanApplicationTerms, final HolidayDetailDTO holidayDetailDTO,
            LoanScheduleParams scheduleParams, LocalDate scheduledDueDate, ScheduleCurrentPeriodParams currentPeriodParams,
            LoanScheduleModelPeriod installment) {
        LocalDate amountApplicableDate = installment.periodDueDate();
        if (loanApplicationTerms.isInterestRecalculationEnabled()) {
            amountApplicableDate = getNextRestScheduleDate(installment.periodDueDate().minusDays(1), loanApplicationTerms,
                    holidayDetailDTO);
        }
        updateMapWithAmount(scheduleParams.getPrincipalPortionMap(),
                currentPeriodParams.getPrincipalForThisPeriod().minus(currentPeriodParams.getReducedBalance()), amountApplicableDate);

        // update outstanding balance for interest calculation
        updateOutstandingBalanceAsPerRest(scheduleParams, scheduledDueDate);
    }

    private LocalDate getNextRestScheduleDate(LocalDate startDate, LoanApplicationTerms loanApplicationTerms,
            final HolidayDetailDTO holidayDetailDTO) {
        LocalDate nextScheduleDate;
        if (loanApplicationTerms.getRecalculationFrequencyType().isSameAsRepayment()) {
            nextScheduleDate = getScheduledDateGenerator().generateNextScheduleDateStartingFromDisburseDateOrRescheduleDate(startDate,
                    loanApplicationTerms, holidayDetailDTO);
        } else {
            CalendarInstance calendarInstance = loanApplicationTerms.getRestCalendarInstance();
            nextScheduleDate = CalendarUtils.getNextScheduleDate(calendarInstance.getCalendar(), startDate);
        }

        return nextScheduleDate;
    }

    private void updateMapWithAmount(final Map<LocalDate, Money> map, final Money amount, final LocalDate amountApplicableDate) {
        Money principalPaid = amount;
        if (map.containsKey(amountApplicableDate)) {
            principalPaid = map.get(amountApplicableDate).plus(principalPaid);
        }
        map.put(amountApplicableDate, principalPaid);

    }

    private void updateOutstandingBalanceAsPerRest(final LoanScheduleParams scheduleParams, final LocalDate scheduledDueDate) {
        scheduleParams.setOutstandingBalanceAsPerRest(updateBalanceForInterestCalculation(scheduleParams.getPrincipalPortionMap(),
                scheduledDueDate, scheduleParams.getOutstandingBalanceAsPerRest(), false));
    }

    /**
     * Identifies all the past date principal changes and apply them on outstanding balance for future calculations
     */
    private Money updateBalanceForInterestCalculation(final Map<LocalDate, Money> principalPortionMap, final LocalDate scheduledDueDate,
            final Money outstandingBalanceAsPerRest, boolean addMapDetails) {
        List<LocalDate> removeFromPrincipalPortionMap = new ArrayList<>();
        Money outstandingBalance = outstandingBalanceAsPerRest;
        for (Map.Entry<LocalDate, Money> principal : principalPortionMap.entrySet()) {
            if (!principal.getKey().isAfter(scheduledDueDate)) {
                if (addMapDetails) {
                    outstandingBalance = outstandingBalance.plus(principal.getValue());
                } else {
                    outstandingBalance = outstandingBalance.minus(principal.getValue());
                }
                removeFromPrincipalPortionMap.add(principal.getKey());
            }
        }
        for (LocalDate date : removeFromPrincipalPortionMap) {
            principalPortionMap.remove(date);
        }
        return outstandingBalance;
    }

    private void updatePeriodsWithCharges(final MonetaryCurrency currency, LoanScheduleParams scheduleParams,
            final Collection<LoanScheduleModelPeriod> periods, final Set<LoanCharge> nonCompoundingCharges, MathContext mc) {
        for (LoanScheduleModelPeriod loanScheduleModelPeriod : periods) {
            if (loanScheduleModelPeriod.isRepaymentPeriod()) {
                PrincipalInterest principalInterest = new PrincipalInterest(Money.of(currency, loanScheduleModelPeriod.principalDue()),
                        Money.of(currency, loanScheduleModelPeriod.interestDue()), null);
                Money feeChargesForInstallment = cumulativeFeeChargesDueWithin(loanScheduleModelPeriod.periodFromDate(),
                        loanScheduleModelPeriod.periodDueDate(), nonCompoundingCharges, currency, principalInterest,
                        scheduleParams.getPrincipalToBeScheduled(), scheduleParams.getTotalCumulativeInterest(),
                        !loanScheduleModelPeriod.isRecalculatedInterestComponent(), scheduleParams.isFirstPeriod(), mc);
                Money penaltyChargesForInstallment = cumulativePenaltyChargesDueWithin(loanScheduleModelPeriod.periodFromDate(),
                        loanScheduleModelPeriod.periodDueDate(), nonCompoundingCharges, currency, principalInterest,
                        scheduleParams.getPrincipalToBeScheduled(), scheduleParams.getTotalCumulativeInterest(),
                        !loanScheduleModelPeriod.isRecalculatedInterestComponent(), scheduleParams.isFirstPeriod(), mc);
                scheduleParams.addTotalFeeChargesCharged(feeChargesForInstallment);
                scheduleParams.addTotalPenaltyChargesCharged(penaltyChargesForInstallment);
                scheduleParams.addTotalRepaymentExpected(feeChargesForInstallment.plus(penaltyChargesForInstallment));
                loanScheduleModelPeriod.addLoanCharges(feeChargesForInstallment.getAmount(), penaltyChargesForInstallment.getAmount());
            }
        }
    }

    private Set<LoanCharge> separateTotalCompoundingPercentageCharges(final Set<LoanCharge> loanCharges) {
        Set<LoanCharge> interestCharges = new HashSet<>();
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isSpecifiedDueDate() && (loanCharge.getChargeCalculation().isPercentageOfInterest()
                    || loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest())) {
                interestCharges.add(loanCharge);
            }
        }
        loanCharges.removeAll(interestCharges);
        return interestCharges;
    }

    private void updateOutstandingBalance(LoanScheduleParams scheduleParams, ScheduleCurrentPeriodParams currentPeriodParams) {
        // update outstandingLoanBlance using current period
        // 'principalDue'
        scheduleParams
                .reduceOutstandingBalance(currentPeriodParams.getPrincipalForThisPeriod().minus(currentPeriodParams.getReducedBalance()));
    }

    /**
     * @param loanApplicationTerms
     * @param scheduleParams
     * @param scheduledDueDate
     * @return
     */
    private LoanTermVariationParams applyLoanTermVariations(final LoanApplicationTerms loanApplicationTerms,
            final LoanScheduleParams scheduleParams, final LocalDate scheduledDueDate) {
        boolean skipPeriod = false;
        boolean recalculateAmounts = false;
        LocalDate modifiedScheduledDueDate = scheduledDueDate;
        ArrayList<LoanTermVariationsData> variationsData = null;

        // due date changes should be applied only for that dueDate
        if (loanApplicationTerms.getLoanTermVariations().hasDueDateVariation(scheduledDueDate)) {
            LoanTermVariationsData loanTermVariationsData = loanApplicationTerms.getLoanTermVariations().nextDueDateVariation();
            if (DateUtils.isEqual(modifiedScheduledDueDate, loanTermVariationsData.getTermVariationApplicableFrom())) {
                modifiedScheduledDueDate = loanTermVariationsData.getDateValue();
                loanApplicationTerms.updateVariationDays(DateUtils.getDifferenceInDays(scheduledDueDate, modifiedScheduledDueDate));
                if (!loanTermVariationsData.isSpecificToInstallment()) {
                    scheduleParams.setActualRepaymentDate(modifiedScheduledDueDate);
                    loanApplicationTerms.setNewScheduledDueDateStart(modifiedScheduledDueDate);
                }
                loanTermVariationsData.setProcessed(true);
            }
        }

        return new LoanTermVariationParams(skipPeriod, recalculateAmounts, modifiedScheduledDueDate, variationsData);
    }
}
