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

import static java.time.temporal.ChronoUnit.DAYS;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.data.OutstandingAmountsDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleDTO;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleModelDownPaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleParams;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestRepaymentModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.exception.MultiDisbursementOutstandingAmoutException;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProgressiveLoanScheduleGenerator implements LoanScheduleGenerator {

    private final ScheduledDateGenerator scheduledDateGenerator;
    private final EMICalculator emiCalculator;

    @Override
    public LoanScheduleModel generate(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            final Set<LoanCharge> loanCharges, final HolidayDetailDTO holidayDetailDTO) {

        final ApplicationCurrency applicationCurrency = loanApplicationTerms.getApplicationCurrency();
        // generate list of proposed schedule due dates
        LocalDate loanEndDate = scheduledDateGenerator.getLastRepaymentDate(loanApplicationTerms, holidayDetailDTO);
        LoanTermVariationsData lastDueDateVariation = loanApplicationTerms.getLoanTermVariations()
                .fetchLoanTermDueDateVariationsData(loanEndDate);
        if (lastDueDateVariation != null) {
            loanEndDate = lastDueDateVariation.getDateValue();
        }

        // determine the total charges due at time of disbursement
        final BigDecimal chargesDueAtTimeOfDisbursement = deriveTotalChargesDueAtTimeOfDisbursement(loanCharges);

        // setup variables for tracking important facts required for loan
        // schedule generation.

        final MonetaryCurrency currency = loanApplicationTerms.getCurrency();
        LocalDate periodStartDate = RepaymentStartDateType.DISBURSEMENT_DATE.equals(loanApplicationTerms.getRepaymentStartDateType())
                ? loanApplicationTerms.getExpectedDisbursementDate()
                : loanApplicationTerms.getSubmittedOnDate();

        final LoanScheduleParams scheduleParams = LoanScheduleParams.createLoanScheduleParams(currency,
                Money.of(currency, chargesDueAtTimeOfDisbursement), periodStartDate, Money.zero(currency));

        // charges which depends on total loan interest will be added to this
        // set and handled separately after all installments generated
        final Set<LoanCharge> nonCompoundingCharges = separateTotalCompoundingPercentageCharges(loanCharges);

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = scheduledDateGenerator
                .generateRepaymentPeriods(periodStartDate, loanApplicationTerms, holidayDetailDTO);
        final ProgressiveLoanInterestScheduleModel interestScheduleModel = emiCalculator.generateInterestScheduleModel(
                expectedRepaymentPeriods, loanApplicationTerms.toLoanProductRelatedDetail(),
                loanApplicationTerms.getInstallmentAmountInMultiplesOf(), mc);
        final List<LoanScheduleModelPeriod> periods = new ArrayList<>(expectedRepaymentPeriods.size() + 2);

        prepareDisbursementsOnLoanApplicationTerms(loanApplicationTerms);

        final ArrayList<DisbursementData> disbursementDataList = new ArrayList<>(loanApplicationTerms.getDisbursementDatas());
        disbursementDataList.sort(Comparator.comparing(DisbursementData::disbursementDate));

        for (LoanScheduleModelRepaymentPeriod repaymentPeriod : expectedRepaymentPeriods) {
            scheduleParams.setPeriodStartDate(repaymentPeriod.getFromDate());
            scheduleParams.setActualRepaymentDate(repaymentPeriod.getDueDate());

            processDisbursements(loanApplicationTerms, disbursementDataList, scheduleParams, interestScheduleModel, periods,
                    chargesDueAtTimeOfDisbursement);
            repaymentPeriod.setPeriodNumber(scheduleParams.getInstalmentNumber());

            for (var interestRateChange : loanApplicationTerms.getLoanTermVariations().getInterestRateFromInstallment()) {
                final LocalDate interestRateChangeEffectiveDate = interestRateChange.getTermVariationApplicableFrom().minusDays(1);
                final BigDecimal newInterestRate = interestRateChange.getDecimalValue();
                if (interestRateChangeEffectiveDate.isAfter(repaymentPeriod.getFromDate())
                        && !interestRateChangeEffectiveDate.isAfter(repaymentPeriod.getDueDate())) {
                    emiCalculator.changeInterestRate(interestScheduleModel, interestRateChangeEffectiveDate, newInterestRate);
                }
            }

            emiCalculator.findInterestRepaymentPeriod(interestScheduleModel, repaymentPeriod.getDueDate())
                    .ifPresent(interestRepaymentPeriod -> {
                        final Money principalDue = interestRepaymentPeriod.getPrincipalDue();
                        final Money interestDue = interestRepaymentPeriod.getInterestDue();

                        repaymentPeriod.addPrincipalAmount(principalDue);
                        repaymentPeriod.addInterestAmount(interestDue);
                        repaymentPeriod.setOutstandingLoanBalance(interestRepaymentPeriod.getRemainingBalance());

                        scheduleParams.addTotalCumulativePrincipal(principalDue);
                        scheduleParams.addTotalCumulativeInterest(interestDue);
                        // add everything
                        scheduleParams.addTotalRepaymentExpected(principalDue.plus(interestDue));
                    });

            applyChargesForCurrentPeriod(repaymentPeriod, loanCharges, scheduleParams, currency, mc);
            periods.add(repaymentPeriod);

            scheduleParams.incrementInstalmentNumber();
            scheduleParams.incrementPeriodNumber();
        }

        if (loanApplicationTerms.isMultiDisburseLoan()) {
            processDisbursements(loanApplicationTerms, disbursementDataList, scheduleParams, null, periods, chargesDueAtTimeOfDisbursement);
        }

        // determine fees and penalties for charges which depends on total
        // loan interest
        updatePeriodsWithCharges(currency, scheduleParams, periods, nonCompoundingCharges, mc);

        final BigDecimal totalPrincipalPaid = BigDecimal.ZERO;
        final BigDecimal totalOutstanding = BigDecimal.ZERO;

        return LoanScheduleModel.from(periods, applicationCurrency, interestScheduleModel.getLoanTermInDays(),
                scheduleParams.getPrincipalToBeScheduled().plus(loanApplicationTerms.getDownPaymentAmount()),
                scheduleParams.getTotalCumulativePrincipal().plus(loanApplicationTerms.getDownPaymentAmount()).getAmount(),
                totalPrincipalPaid, scheduleParams.getTotalCumulativeInterest().getAmount(),
                scheduleParams.getTotalFeeChargesCharged().getAmount(), scheduleParams.getTotalPenaltyChargesCharged().getAmount(),
                scheduleParams.getTotalRepaymentExpected().getAmount(), totalOutstanding);
    }

    private void prepareDisbursementsOnLoanApplicationTerms(final LoanApplicationTerms loanApplicationTerms) {
        if (loanApplicationTerms.getDisbursementDatas().isEmpty()) {
            loanApplicationTerms.getDisbursementDatas()
                    .add(new DisbursementData(1L, loanApplicationTerms.getExpectedDisbursementDate(),
                            loanApplicationTerms.getExpectedDisbursementDate(), loanApplicationTerms.getPrincipal().getAmount(), null, null,
                            null, null));
        }
    }

    private void processDisbursements(final LoanApplicationTerms loanApplicationTerms,
            final ArrayList<DisbursementData> disbursementDataList, final LoanScheduleParams scheduleParams,
            final ProgressiveLoanInterestScheduleModel interestScheduleModel, final List<LoanScheduleModelPeriod> periods,
            final BigDecimal chargesDueAtTimeOfDisbursement) {

        for (DisbursementData disbursementData : disbursementDataList) {
            final LocalDate disbursementDate = disbursementData.disbursementDate();
            final LocalDate periodFromDate = scheduleParams.getPeriodStartDate();
            final LocalDate periodDueDate = scheduleParams.getActualRepaymentDate();

            boolean hasDisbursementAfterLastRepaymentPeriod = interestScheduleModel == null && !disbursementDate.isBefore(periodDueDate);
            boolean hasDisbursementInCurrentRepaymentPeriod = interestScheduleModel != null && !disbursementDate.isBefore(periodFromDate)
                    && disbursementDate.isBefore(periodDueDate);
            if (!hasDisbursementAfterLastRepaymentPeriod && !hasDisbursementInCurrentRepaymentPeriod) {
                continue;
            }

            Money outstandingBalance = emiCalculator.findInterestRepaymentPeriod(interestScheduleModel, periodDueDate)
                    .map(ProgressiveLoanInterestRepaymentModel::getOutstandingBalance)
                    .orElse(Money.zero(loanApplicationTerms.getCurrency()));

            final Money disbursedAmount = Money.of(loanApplicationTerms.getCurrency(), disbursementData.getPrincipal());
            final LoanScheduleModelDisbursementPeriod disbursementPeriod = LoanScheduleModelDisbursementPeriod
                    .disbursement(disbursementData.disbursementDate(), disbursedAmount, chargesDueAtTimeOfDisbursement);
            periods.add(disbursementPeriod);

            // validation check for amount not exceeds specified max
            // amount as per the configuration
            if (loanApplicationTerms.isMultiDisburseLoan() && loanApplicationTerms.getMaxOutstandingBalance() != null) {
                Money maxOutstandingBalance = loanApplicationTerms.getMaxOutstandingBalanceMoney();
                if (outstandingBalance.plus(disbursedAmount).isGreaterThan(maxOutstandingBalance)) {
                    String errorMsg = "Outstanding balance must not exceed the amount: " + maxOutstandingBalance;
                    throw new MultiDisbursementOutstandingAmoutException(errorMsg, loanApplicationTerms.getMaxOutstandingBalance(),
                            disbursedAmount);
                }
            }

            Money downPaymentAmount = Money.zero(loanApplicationTerms.getCurrency());
            if (loanApplicationTerms.isDownPaymentEnabled()) {
                downPaymentAmount = Money.of(loanApplicationTerms.getCurrency(), MathUtil.percentageOf(disbursedAmount.getAmount(),
                        loanApplicationTerms.getDisbursedAmountPercentageForDownPayment(), 19));
                if (loanApplicationTerms.getInstallmentAmountInMultiplesOf() != null) {
                    downPaymentAmount = Money.roundToMultiplesOf(downPaymentAmount,
                            loanApplicationTerms.getInstallmentAmountInMultiplesOf());
                }

                LoanScheduleModelDownPaymentPeriod downPaymentPeriod = LoanScheduleModelDownPaymentPeriod.downPayment(
                        scheduleParams.getInstalmentNumber(), disbursementDate, downPaymentAmount,
                        outstandingBalance.plus(disbursedAmount).minus(downPaymentAmount));
                periods.add(downPaymentPeriod);

                scheduleParams.addTotalRepaymentExpected(downPaymentAmount);
                scheduleParams.incrementInstalmentNumber();
            }

            final Money disbursementRemainingBalance = disbursedAmount.minus(downPaymentAmount);
            scheduleParams.addPrincipalToBeScheduled(disbursementRemainingBalance);
            emiCalculator.addDisbursement(interestScheduleModel, disbursementDate, disbursementRemainingBalance);
        }
    }

    @Override
    public LoanScheduleDTO rescheduleNextInstallments(MathContext mc, LoanApplicationTerms loanApplicationTerms, Loan loan,
            HolidayDetailDTO holidayDetailDTO, LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor,
            LocalDate rescheduleFrom) {
        LoanScheduleModel model = generate(mc, loanApplicationTerms, loan.getActiveCharges(), holidayDetailDTO);
        return LoanScheduleDTO.from(null, model);
    }

    @Override
    public OutstandingAmountsDTO calculatePrepaymentAmount(MonetaryCurrency currency, LocalDate onDate,
            LoanApplicationTerms loanApplicationTerms, MathContext mc, Loan loan, HolidayDetailDTO holidayDetailDTO,
            LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor) {
        return switch (loanApplicationTerms.getPreClosureInterestCalculationStrategy()) {
            case TILL_PRE_CLOSURE_DATE -> {
                log.debug("calculating prepayment amount till pre closure date (Strategy A)");
                OutstandingAmountsDTO outstandingAmounts = new OutstandingAmountsDTO(currency);
                AtomicBoolean firstAfterPayoff = new AtomicBoolean(true);
                loan.getRepaymentScheduleInstallments().forEach(installment -> {
                    boolean isInstallmentAfterPayoff = installment.getDueDate().isAfter(onDate);

                    outstandingAmounts.plusPrincipal(installment.getPrincipalOutstanding(currency));
                    if (isInstallmentAfterPayoff) {
                        if (firstAfterPayoff.getAndSet(false)) {
                            outstandingAmounts.plusInterest(calculatePayableInterest(loan, installment, onDate));
                        } else {
                            log.debug("Installment {} - {} is after payoff, not counting interest", installment.getFromDate(),
                                    installment.getDueDate());
                        }
                    } else {
                        log.debug("adding interest for {} - {}: {}", installment.getFromDate(), installment.getDueDate(),
                                installment.getInterestOutstanding(currency));
                        outstandingAmounts.plusInterest(installment.getInterestOutstanding(currency));
                    }
                    outstandingAmounts.plusFeeCharges(installment.getFeeChargesOutstanding(currency));
                    outstandingAmounts.plusPenaltyCharges(installment.getPenaltyChargesOutstanding(currency));
                });
                yield outstandingAmounts;
            }

            case TILL_REST_FREQUENCY_DATE -> {
                log.debug("calculating prepayment amount till rest frequency date (Strategy B)");
                OutstandingAmountsDTO outstandingAmounts = new OutstandingAmountsDTO(currency);
                loan.getRepaymentScheduleInstallments().forEach(installment -> {
                    boolean isPayoffBeforeInstallment = installment.getFromDate().isBefore(onDate);

                    outstandingAmounts.plusPrincipal(installment.getPrincipalOutstanding(currency));
                    if (isPayoffBeforeInstallment) {
                        outstandingAmounts.plusInterest(installment.getInterestOutstanding(currency));
                    } else {
                        log.debug("Payoff after installment {}, not counting interest", installment.getDueDate());
                    }
                    outstandingAmounts.plusFeeCharges(installment.getFeeChargesOutstanding(currency));
                    outstandingAmounts.plusPenaltyCharges(installment.getPenaltyChargesOutstanding(currency));
                });

                yield outstandingAmounts;
            }
            case NONE -> throw new UnsupportedOperationException("Pre-closure interest calculation strategy not supported");
        };
    }

    private Money calculatePayableInterest(Loan loan, LoanRepaymentScheduleInstallment installment, LocalDate onDate) {
        RoundingMode roundingMode = MoneyHelper.getRoundingMode();
        MonetaryCurrency currency = loan.getCurrency();
        Money originalInterest = installment.getInterestCharged(currency);
        log.debug("calculating interest for {} from {} to {}", originalInterest, installment.getFromDate(), installment.getDueDate());

        LocalDate start = installment.getFromDate();
        Money payableInterest = Money.zero(currency);

        while (!start.isEqual(onDate)) {
            long between = DAYS.between(start, installment.getDueDate());
            Money dailyInterest = originalInterest.minus(payableInterest).dividedBy(between, roundingMode);
            log.debug("Daily interest is {}: {} / {}, total: {}", dailyInterest, originalInterest.minus(payableInterest), between,
                    payableInterest.add(dailyInterest));
            payableInterest = payableInterest.add(dailyInterest);
            start = start.plusDays(1);
        }

        payableInterest = payableInterest.minus(installment.getInterestPaid(currency).minus(installment.getInterestWaived(currency)));

        log.debug("Payable interest is {}", payableInterest);
        return payableInterest;
    }

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

    private void applyChargesForCurrentPeriod(final LoanScheduleModelRepaymentPeriod repaymentPeriod, final Set<LoanCharge> loanCharges,
            final LoanScheduleParams scheduleParams, final MonetaryCurrency currency, final MathContext mc) {
        final PrincipalInterest principalInterest = new PrincipalInterest(repaymentPeriod.getPrincipalDue(),
                repaymentPeriod.getInterestDue(), null);

        final Money fees = cumulativeFeeChargesDueWithin(repaymentPeriod.getFromDate(), repaymentPeriod.getDueDate(), loanCharges, currency,
                principalInterest, scheduleParams.getPrincipalToBeScheduled(), scheduleParams.getTotalCumulativeInterest(), true,
                scheduleParams.isFirstPeriod(), mc);
        final Money penalties = cumulativePenaltyChargesDueWithin(repaymentPeriod.getFromDate(), repaymentPeriod.getDueDate(), loanCharges,
                currency, principalInterest, scheduleParams.getPrincipalToBeScheduled(), scheduleParams.getTotalCumulativeInterest(), true,
                scheduleParams.isFirstPeriod(), mc);

        repaymentPeriod.addLoanCharges(fees.getAmount(), penalties.getAmount());
        scheduleParams.addTotalFeeChargesCharged(fees);
        scheduleParams.addTotalPenaltyChargesCharged(penalties);
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

}
