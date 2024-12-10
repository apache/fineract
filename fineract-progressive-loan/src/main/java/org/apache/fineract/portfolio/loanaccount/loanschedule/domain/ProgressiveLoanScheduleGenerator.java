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

import static org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper.findInPeriod;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.OutstandingAmountsDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleDTO;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleModelDownPaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleParams;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlan;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OutstandingDetails;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.exception.MultiDisbursementOutstandingAmoutException;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProgressiveLoanScheduleGenerator implements LoanScheduleGenerator {

    private final ScheduledDateGenerator scheduledDateGenerator;
    private final EMICalculator emiCalculator;

    public LoanSchedulePlan generate(final MathContext mc, final LoanRepaymentScheduleModelData modelData) {
        LoanApplicationTerms loanApplicationTerms = LoanApplicationTerms.assembleFrom(modelData, mc);
        return LoanSchedulePlan.from(generate(mc, loanApplicationTerms, null, null));
    }

    @Override
    public LoanScheduleModel generate(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            final Set<LoanCharge> loanCharges, final HolidayDetailDTO holidayDetailDTO) {

        // determine the total charges due at time of disbursement
        final BigDecimal chargesDueAtTimeOfDisbursement = deriveTotalChargesDueAtTimeOfDisbursement(loanCharges);

        final CurrencyData currency = loanApplicationTerms.getCurrency();
        LocalDate periodStartDate = RepaymentStartDateType.DISBURSEMENT_DATE.equals(loanApplicationTerms.getRepaymentStartDateType())
                ? loanApplicationTerms.getExpectedDisbursementDate()
                : loanApplicationTerms.getSubmittedOnDate();

        final LoanScheduleParams scheduleParams = LoanScheduleParams.createLoanScheduleParams(currency,
                Money.of(currency, chargesDueAtTimeOfDisbursement, mc), periodStartDate, Money.zero(currency, mc), mc);

        // charges which depends on total loan interest will be added to this
        // set and handled separately after all installments generated
        final Set<LoanCharge> nonCompoundingCharges = separateTotalCompoundingPercentageCharges(loanCharges);

        // generate list of proposed schedule due dates
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = scheduledDateGenerator.generateRepaymentPeriods(mc,
                periodStartDate, loanApplicationTerms, holidayDetailDTO);
        final ProgressiveLoanInterestScheduleModel interestScheduleModel = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanApplicationTerms.toLoanProductRelatedDetailMinimumData(),
                loanApplicationTerms.getInstallmentAmountInMultiplesOf(), mc);
        final List<LoanScheduleModelPeriod> periods = new ArrayList<>(expectedRepaymentPeriods.size());

        prepareDisbursementsOnLoanApplicationTerms(loanApplicationTerms);
        final List<DisbursementData> disbursementDataList = getSortedDisbursementList(loanApplicationTerms);

        for (LoanScheduleModelRepaymentPeriod repaymentPeriod : expectedRepaymentPeriods) {
            scheduleParams.setPeriodStartDate(repaymentPeriod.getFromDate());
            scheduleParams.setActualRepaymentDate(repaymentPeriod.getDueDate());
            // in same repayment period the logic firstly applies interest rate changes and just after the disbursements
            applyInterestRateChangesOnPeriod(loanApplicationTerms, repaymentPeriod, interestScheduleModel);
            processDisbursements(loanApplicationTerms, disbursementDataList, scheduleParams, interestScheduleModel, periods,
                    chargesDueAtTimeOfDisbursement, false, mc);
            repaymentPeriod.setPeriodNumber(scheduleParams.getInstalmentNumber());

            emiCalculator.findRepaymentPeriod(interestScheduleModel, repaymentPeriod.getDueDate()).ifPresent(interestRepaymentPeriod -> {
                final Money principalDue = interestRepaymentPeriod.getDuePrincipal();
                final Money interestDue = interestRepaymentPeriod.getDueInterest();

                repaymentPeriod.addPrincipalAmount(principalDue);
                repaymentPeriod.addInterestAmount(interestDue);
                repaymentPeriod.setOutstandingLoanBalance(interestRepaymentPeriod.getOutstandingLoanBalance());

                scheduleParams.addTotalCumulativePrincipal(principalDue);
                scheduleParams.addTotalCumulativeInterest(interestDue);
                // add everything
                scheduleParams.addTotalRepaymentExpected(principalDue.plus(interestDue, mc));
            });

            applyChargesForCurrentPeriod(repaymentPeriod, loanCharges, scheduleParams, currency, mc);
            periods.add(repaymentPeriod);

            scheduleParams.incrementInstalmentNumber();
            scheduleParams.incrementPeriodNumber();
        }

        if (loanApplicationTerms.isMultiDisburseLoan()) {
            processDisbursements(loanApplicationTerms, disbursementDataList, scheduleParams, interestScheduleModel, periods,
                    chargesDueAtTimeOfDisbursement, true, mc);
        }

        // determine fees and penalties for charges which depends on total
        // loan interest
        updatePeriodsWithCharges(currency, scheduleParams, periods, nonCompoundingCharges, mc);

        final BigDecimal totalPrincipalPaid = BigDecimal.ZERO;
        final BigDecimal totalOutstanding = BigDecimal.ZERO;

        return LoanScheduleModel.from(periods, currency, interestScheduleModel.getLoanTermInDays(),
                scheduleParams.getPrincipalToBeScheduled().plus(loanApplicationTerms.getDownPaymentAmount(), mc),
                scheduleParams.getTotalCumulativePrincipal().plus(loanApplicationTerms.getDownPaymentAmount(), mc).getAmount(),
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
    public OutstandingAmountsDTO calculatePrepaymentAmount(MonetaryCurrency currency, LocalDate onDate,
            LoanApplicationTerms loanApplicationTerms, MathContext mc, Loan loan, HolidayDetailDTO holidayDetailDTO,
            LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor) {
        if (!(loanRepaymentScheduleTransactionProcessor instanceof AdvancedPaymentScheduleTransactionProcessor processor)) {
            throw new IllegalStateException("Expected an AdvancedPaymentScheduleTransactionProcessor");
        }

        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        LoanRepaymentScheduleInstallment actualInstallment = findInPeriod(onDate, installments).orElse(installments.get(0));

        LocalDate transactionDate = switch (loanApplicationTerms.getPreClosureInterestCalculationStrategy()) {
            case TILL_PRE_CLOSURE_DATE -> onDate;
            case TILL_REST_FREQUENCY_DATE -> actualInstallment.getDueDate(); // due date of current installment
            case NONE -> throw new IllegalStateException("Unexpected PreClosureInterestCalculationStrategy: NONE");
        };

        ProgressiveLoanInterestScheduleModel model = processor.calculateInterestScheduleModel(loan.getId(), onDate);
        OutstandingDetails outstandingAmounts = emiCalculator.getOutstandingAmountsTillDate(model, transactionDate);
        // TODO: We should add all the past due outstanding amounts as well
        OutstandingAmountsDTO result = new OutstandingAmountsDTO(currency) //
                .principal(outstandingAmounts.getOutstandingPrincipal()) //
                .interest(outstandingAmounts.getOutstandingInterest());//

        installments.forEach(installment -> result //
                .plusFeeCharges(installment.getFeeChargesOutstanding(currency))
                .plusPenaltyCharges(installment.getPenaltyChargesOutstanding(currency)));

        return result;
    }

    @Override
    public Money getPeriodInterestTillDate(@NotNull LoanRepaymentScheduleInstallment installment, @NotNull LocalDate targetDate) {
        Loan loan = installment.getLoan();
        LoanRepaymentScheduleTransactionProcessor transactionProcessor = loan.getTransactionProcessor();
        if (!(transactionProcessor instanceof AdvancedPaymentScheduleTransactionProcessor processor)) {
            throw new IllegalStateException("Expected an AdvancedPaymentScheduleTransactionProcessor");
        }
        if (installment.isAdditional() || installment.isDownPayment() || installment.isReAged()) {
            return Money.zero(loan.getCurrency());
        }
        ProgressiveLoanInterestScheduleModel model = processor.calculateInterestScheduleModel(loan.getId(), targetDate);
        return emiCalculator.getPeriodInterestTillDate(model, installment.getDueDate(), targetDate);
    }

    // Private, internal methods

    private List<DisbursementData> getSortedDisbursementList(LoanApplicationTerms loanApplicationTerms) {
        final List<DisbursementData> disbursementDataList = new ArrayList<>(loanApplicationTerms.getDisbursementDatas());
        disbursementDataList.sort(Comparator.comparing(DisbursementData::disbursementDate));
        return disbursementDataList;
    }

    private void applyInterestRateChangesOnPeriod(final LoanApplicationTerms loanApplicationTerms,
            final LoanScheduleModelRepaymentPeriod repaymentPeriod, final ProgressiveLoanInterestScheduleModel interestScheduleModel) {
        if (loanApplicationTerms.getLoanTermVariations() != null) {
            for (var interestRateChange : loanApplicationTerms.getLoanTermVariations().getInterestRateFromInstallment()) {
                final LocalDate interestRateSubmittedOnDate = interestRateChange.getTermVariationApplicableFrom();
                final BigDecimal newInterestRate = interestRateChange.getDecimalValue();
                if (interestRateSubmittedOnDate.isAfter(repaymentPeriod.getFromDate())
                        && !interestRateSubmittedOnDate.isAfter(repaymentPeriod.getDueDate())) {
                    emiCalculator.changeInterestRate(interestScheduleModel, interestRateSubmittedOnDate, newInterestRate);
                }
            }
        }
    }

    private void prepareDisbursementsOnLoanApplicationTerms(final LoanApplicationTerms loanApplicationTerms) {
        if (loanApplicationTerms.getDisbursementDatas().isEmpty()) {
            loanApplicationTerms.getDisbursementDatas()
                    .add(new DisbursementData(1L, loanApplicationTerms.getExpectedDisbursementDate(),
                            loanApplicationTerms.getExpectedDisbursementDate(), loanApplicationTerms.getPrincipal().getAmount(), null, null,
                            null, null));
        }
    }

    private void processDisbursements(final LoanApplicationTerms loanApplicationTerms, final List<DisbursementData> disbursementDataList,
            final LoanScheduleParams scheduleParams, final ProgressiveLoanInterestScheduleModel interestScheduleModel,
            final List<LoanScheduleModelPeriod> periods, final BigDecimal chargesDueAtTimeOfDisbursement,
            final boolean includeDisbursementsAfterMaturityDate, final MathContext mc) {

        for (DisbursementData disbursementData : disbursementDataList) {
            final LocalDate disbursementDate = disbursementData.disbursementDate();
            final LocalDate periodFromDate = scheduleParams.getPeriodStartDate();
            final LocalDate periodDueDate = scheduleParams.getActualRepaymentDate();

            final LocalDate maturityDate = interestScheduleModel.getMaturityDate();
            boolean hasDisbursementAfterLastRepaymentPeriod = includeDisbursementsAfterMaturityDate
                    && !disbursementDate.isBefore(maturityDate);
            boolean hasDisbursementInCurrentRepaymentPeriod = !includeDisbursementsAfterMaturityDate
                    && !disbursementDate.isBefore(periodFromDate) && disbursementDate.isBefore(periodDueDate);
            if (!hasDisbursementAfterLastRepaymentPeriod && !hasDisbursementInCurrentRepaymentPeriod) {
                continue;
            }

            Money outstandingBalance = emiCalculator.getOutstandingLoanBalanceOfPeriod(interestScheduleModel, periodDueDate,
                    disbursementDate);

            final Money disbursedAmount = Money.of(loanApplicationTerms.getCurrency(), disbursementData.getPrincipal(), mc);
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

            Money downPaymentAmount = Money.zero(loanApplicationTerms.getCurrency(), mc);
            if (loanApplicationTerms.isDownPaymentEnabled()) {
                downPaymentAmount = Money.of(loanApplicationTerms.getCurrency(), MathUtil.percentageOf(disbursedAmount.getAmount(),
                        loanApplicationTerms.getDisbursedAmountPercentageForDownPayment(), mc), mc);
                if (loanApplicationTerms.getInstallmentAmountInMultiplesOf() != null) {
                    downPaymentAmount = Money.roundToMultiplesOf(downPaymentAmount,
                            loanApplicationTerms.getInstallmentAmountInMultiplesOf(), mc);
                }

                LoanScheduleModelDownPaymentPeriod downPaymentPeriod = LoanScheduleModelDownPaymentPeriod.downPayment(
                        scheduleParams.getInstalmentNumber(), disbursementDate, downPaymentAmount,
                        outstandingBalance.plus(disbursedAmount, mc).minus(downPaymentAmount, mc));
                periods.add(downPaymentPeriod);

                scheduleParams.addTotalRepaymentExpected(downPaymentAmount);
                scheduleParams.incrementInstalmentNumber();
            }

            final Money disbursementRemainingBalance = disbursedAmount.minus(downPaymentAmount, mc);
            scheduleParams.addPrincipalToBeScheduled(disbursementRemainingBalance);
            emiCalculator.addDisbursement(interestScheduleModel, disbursementDate, disbursementRemainingBalance);
        }
    }

    private BigDecimal deriveTotalChargesDueAtTimeOfDisbursement(final Set<LoanCharge> loanCharges) {
        BigDecimal chargesDueAtTimeOfDisbursement = BigDecimal.ZERO;
        if (loanCharges != null) {
            for (final LoanCharge loanCharge : loanCharges) {
                if (loanCharge.isDueAtDisbursement()) {
                    chargesDueAtTimeOfDisbursement = chargesDueAtTimeOfDisbursement.add(loanCharge.amount());
                }
            }
        }
        return chargesDueAtTimeOfDisbursement;
    }

    private void applyChargesForCurrentPeriod(final LoanScheduleModelRepaymentPeriod repaymentPeriod, final Set<LoanCharge> loanCharges,
            final LoanScheduleParams scheduleParams, final CurrencyData currency, final MathContext mc) {
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
            final CurrencyData currency, final PrincipalInterest principalInterestForThisPeriod, final Money principalDisbursed,
            final Money totalInterestChargedForFullLoanTerm, boolean isInstallmentChargeApplicable, final boolean isFirstPeriod,
            final MathContext mc) {
        Money cumulative = Money.zero(currency, mc);
        if (loanCharges != null) {
            for (final LoanCharge loanCharge : loanCharges) {
                if (!loanCharge.isDueAtDisbursement() && loanCharge.isFeeCharge()) {
                    cumulative = getCumulativeAmountOfCharge(periodStart, periodEnd, principalInterestForThisPeriod, principalDisbursed,
                            totalInterestChargedForFullLoanTerm, isInstallmentChargeApplicable, isFirstPeriod, loanCharge, cumulative, mc);
                }
            }
        }
        return cumulative;
    }

    private Money getCumulativeAmountOfCharge(LocalDate periodStart, LocalDate periodEnd, PrincipalInterest principalInterestForThisPeriod,
            Money principalDisbursed, Money totalInterestChargedForFullLoanTerm, boolean isInstallmentChargeApplicable,
            boolean isFirstPeriod, LoanCharge loanCharge, Money cumulative, MathContext mc) {
        boolean isDue = loanCharge.isDueInPeriod(periodStart, periodEnd, isFirstPeriod);
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
            final Set<LoanCharge> loanCharges, final CurrencyData currency, final PrincipalInterest principalInterestForThisPeriod,
            final Money principalDisbursed, final Money totalInterestChargedForFullLoanTerm, boolean isInstallmentChargeApplicable,
            final boolean isFirstPeriod, final MathContext mc) {
        Money cumulative = Money.zero(currency, mc);
        if (loanCharges != null) {
            for (final LoanCharge loanCharge : loanCharges) {
                if (loanCharge.isPenaltyCharge()) {
                    cumulative = getCumulativeAmountOfCharge(periodStart, periodEnd, principalInterestForThisPeriod, principalDisbursed,
                            totalInterestChargedForFullLoanTerm, isInstallmentChargeApplicable, isFirstPeriod, loanCharge, cumulative, mc);
                }
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

    private void updatePeriodsWithCharges(final CurrencyData currency, LoanScheduleParams scheduleParams,
            final Collection<LoanScheduleModelPeriod> periods, final Set<LoanCharge> nonCompoundingCharges, MathContext mc) {
        for (LoanScheduleModelPeriod loanScheduleModelPeriod : periods) {
            if (loanScheduleModelPeriod.isRepaymentPeriod()) {
                PrincipalInterest principalInterest = new PrincipalInterest(Money.of(currency, loanScheduleModelPeriod.principalDue(), mc),
                        Money.of(currency, loanScheduleModelPeriod.interestDue(), mc), null);
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
        if (loanCharges != null) {
            for (final LoanCharge loanCharge : loanCharges) {
                if (loanCharge.isSpecifiedDueDate() && (loanCharge.getChargeCalculation().isPercentageOfInterest()
                        || loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest())) {
                    interestCharges.add(loanCharge);
                }
            }
            loanCharges.removeAll(interestCharges);
        }
        return interestCharges;
    }
}
