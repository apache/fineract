/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;
import org.mifosplatform.portfolio.loanaccount.data.DisbursementData;
import org.mifosplatform.portfolio.loanaccount.data.HolidayDetailDTO;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.domain.LoanSummary;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.loanschedule.exception.MultiDisbursementEmiAmountException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.exception.MultiDisbursementOutstandingAmoutException;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleModel;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleModelRepaymentPeriod;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;

/**
 *
 */
public abstract class AbstractLoanScheduleGenerator implements LoanScheduleGenerator {

    private final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
    private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator = new DefaultPaymentPeriodsInOneYearCalculator();

    @Override
    public LoanScheduleModel generate(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            final Set<LoanCharge> loanCharges, final HolidayDetailDTO holidayDetailDTO) {
        final Collection<RecalculationDetail> diffAmt = null;
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = null;
        final LocalDate lastTransactionDate = loanApplicationTerms.getExpectedDisbursementDate();
        return generate(mc, loanApplicationTerms, loanCharges, holidayDetailDTO, diffAmt, lastTransactionDate,
                loanRepaymentScheduleTransactionProcessor);
    }

    private LoanScheduleModel generate(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            final Set<LoanCharge> loanCharges, final HolidayDetailDTO holidayDetailDTO, final Collection<RecalculationDetail> transactions,
            final LocalDate lastTransactionDate, final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor) {

        final ApplicationCurrency applicationCurrency = loanApplicationTerms.getApplicationCurrency();
        // 1. generate list of proposed schedule due dates
        final LocalDate loanEndDate = this.scheduledDateGenerator.getLastRepaymentDate(loanApplicationTerms, holidayDetailDTO);
        loanApplicationTerms.updateLoanEndDate(loanEndDate);

        // 2. determine the total charges due at time of disbursement
        final BigDecimal chargesDueAtTimeOfDisbursement = deriveTotalChargesDueAtTimeOfDisbursement(loanCharges);

        // 3. setup variables for tracking important facts required for loan
        // schedule generation.
        Money principalDisbursed = loanApplicationTerms.getPrincipal();
        final MonetaryCurrency currency = principalDisbursed.getCurrency();
        final int numberOfRepayments = loanApplicationTerms.getNumberOfRepayments();

        // variables for cumulative totals
        int loanTermInDays = Integer.valueOf(0);
        final BigDecimal totalPrincipalPaid = BigDecimal.ZERO;
        BigDecimal totalFeeChargesCharged = chargesDueAtTimeOfDisbursement;
        BigDecimal totalPenaltyChargesCharged = BigDecimal.ZERO;
        BigDecimal totalRepaymentExpected = chargesDueAtTimeOfDisbursement;
        final BigDecimal totalOutstanding = BigDecimal.ZERO;
        Money totalCumulativePrincipal = principalDisbursed.zero();
        Money totalCumulativeInterest = principalDisbursed.zero();
        Money totalOutstandingInterestPaymentDueToGrace = principalDisbursed.zero();

        final Collection<LoanScheduleModelPeriod> periods = createNewLoanScheduleListWithDisbursementDetails(numberOfRepayments,
                loanApplicationTerms, chargesDueAtTimeOfDisbursement);

        // 4. Determine the total interest owed over the full loan for FLAT
        // interest method .
        Money totalInterestChargedForFullLoanTerm = loanApplicationTerms.calculateTotalInterestCharged(
                this.paymentPeriodsInOneYearCalculator, mc);

        LocalDate periodStartDate = loanApplicationTerms.getExpectedDisbursementDate();
        LocalDate actualRepaymentDate = periodStartDate;
        boolean isFirstRepayment = true;
        LocalDate firstRepaymentdate = this.scheduledDateGenerator.generateNextRepaymentDate(periodStartDate, loanApplicationTerms,
                isFirstRepayment);
        final LocalDate idealDisbursementDate = this.scheduledDateGenerator.idealDisbursementDateBasedOnFirstRepaymentDate(
                loanApplicationTerms.getLoanTermPeriodFrequencyType(), loanApplicationTerms.getRepaymentEvery(), firstRepaymentdate);

        LocalDate periodStartDateApplicableForInterest = periodStartDate;

        // Actual period Number as per the schedule
        int periodNumber = 1;
        // Actual period Number and interest only repayments
        int instalmentNumber = 1;

        Money outstandingBalance = principalDisbursed;
        // disbursement map for tranche details(will added to outstanding
        // balance as per the start date)
        final Map<LocalDate, Money> disburseDetailMap = new HashMap<>();
        if (loanApplicationTerms.isMultiDisburseLoan()) {
            // fetches the first tranche amount and also updates other tranche
            // details to map
            BigDecimal disburseAmt = getDisbursementAmount(loanApplicationTerms, periodStartDate, periods, chargesDueAtTimeOfDisbursement,
                    disburseDetailMap, isInterestRecalculationRequired(loanApplicationTerms, transactions));
            principalDisbursed = principalDisbursed.zero().plus(disburseAmt);
            loanApplicationTerms.setPrincipal(loanApplicationTerms.getPrincipal().zero().plus(disburseAmt));
            outstandingBalance = outstandingBalance.zero().plus(disburseAmt);
        }

        // charges which depends on total loan interest will be added to this
        // set and handled separately after all installments generated
        final Set<LoanCharge> nonCompoundingCharges = seperateTotalCompoundingPercentageCharges(loanCharges);

        // total outstanding balance as per rest for interest calculation.
        Money outstandingBalanceAsPerRest = outstandingBalance;
        // early payments will be added here and as per the selected strategy
        // action will be performed on this value
        Money reducePrincipal = totalCumulativePrincipal.zero();

        // principal changes will be added along with date(after applying rest)
        // from when these amounts will effect the outstanding balance for
        // interest calculation
        final Map<LocalDate, Money> principalPortionMap = new HashMap<>();
        // compounding(interest and fee) amounts will be added along with
        // date(after applying compounding frequency)
        // from when these amounts will effect the outstanding balance for
        // interest calculation
        final Map<LocalDate, Money> latePaymentMap = new HashMap<>();
        final List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>();
        LocalDate currentDate = DateUtils.getLocalDateOfTenant();
        while (!outstandingBalance.isZero() || !disburseDetailMap.isEmpty()) {

            actualRepaymentDate = this.scheduledDateGenerator.generateNextRepaymentDate(actualRepaymentDate, loanApplicationTerms,
                    isFirstRepayment);
            isFirstRepayment = false;
            LocalDate scheduledDueDate = this.scheduledDateGenerator.adjustRepaymentDate(actualRepaymentDate, loanApplicationTerms,
                    holidayDetailDTO);

            // calculated interest start date for the period
            periodStartDateApplicableForInterest = calculateInterestStartDateForPeriod(loanApplicationTerms, periodStartDate,
                    idealDisbursementDate, periodStartDateApplicableForInterest);
            int daysInPeriodApplicableForInterest = Days.daysBetween(periodStartDateApplicableForInterest, scheduledDueDate).getDays();

            // populates the collection with transactions till the due date of
            // the period for interest recalculation enabled loans
            Collection<RecalculationDetail> applicableTransactions = getApplicableTransactionsForPeriod(loanApplicationTerms,
                    scheduledDueDate, transactions);

            double interestCalculationGraceOnRepaymentPeriodFraction = this.paymentPeriodsInOneYearCalculator
                    .calculatePortionOfRepaymentPeriodInterestChargingGrace(periodStartDateApplicableForInterest, scheduledDueDate,
                            loanApplicationTerms.getInterestChargedFromLocalDate(), loanApplicationTerms.getLoanTermPeriodFrequencyType(),
                            loanApplicationTerms.getRepaymentEvery());
            if (loanApplicationTerms.isMultiDisburseLoan()) {
                // Updates fixed emi amount as the date if multiple amounts
                // provided
                loanApplicationTerms.setFixedEmiAmountForPeriod(scheduledDueDate);

                for (Map.Entry<LocalDate, Money> disburseDetail : disburseDetailMap.entrySet()) {
                    if (disburseDetail.getKey().isAfter(periodStartDate) && !disburseDetail.getKey().isAfter(scheduledDueDate)) {
                        // validation check for amount not exceeds specified max
                        // amount as per the configuration
                        if (loanApplicationTerms.getMaxOutstandingBalance() != null
                                && outstandingBalance.plus(disburseDetail.getValue()).isGreaterThan(
                                        loanApplicationTerms.getMaxOutstandingBalance())) {
                            String errorMsg = "Outstanding balance must not exceed the amount: "
                                    + loanApplicationTerms.getMaxOutstandingBalance();
                            throw new MultiDisbursementOutstandingAmoutException(errorMsg, loanApplicationTerms.getMaxOutstandingBalance()
                                    .getAmount(), disburseDetail.getValue());
                        }

                        // creates and add disbursement detail to the repayments
                        // period
                        final LoanScheduleModelDisbursementPeriod disbursementPeriod = LoanScheduleModelDisbursementPeriod.disbursement(
                                disburseDetail.getKey(), disburseDetail.getValue(), chargesDueAtTimeOfDisbursement);
                        periods.add(disbursementPeriod);
                        // updates actual outstanding balance with new
                        // disbursement detail
                        outstandingBalance = outstandingBalance.plus(disburseDetail.getValue());
                        principalDisbursed = principalDisbursed.plus(disburseDetail.getValue());
                        loanApplicationTerms.setPrincipal(loanApplicationTerms.getPrincipal().plus(disburseDetail.getValue()));
                    }
                }
            }

            // Adds new interest repayment to the schedule as per the repayment
            // transaction processor configuration
            // will be added only if there is a loan repayment between the
            // period for interest first repayment strategies
            Money earlyPaidAmount = Money.zero(currency);
            LoanScheduleModelPeriod lastInstallment = null;
            if (isInterestRecalculationRequired(loanApplicationTerms, transactions)) {
                boolean checkForOutstanding = true;
                List<RecalculationDetail> unprocessedTransactions = new ArrayList<>();
                LoanScheduleModelPeriod installment = null;
                for (RecalculationDetail detail : applicableTransactions) {
                    if (detail.isProcessed()) {
                        continue;
                    }
                    if (detail.getTransactionDate().isBefore(scheduledDueDate)) {
                        if (loanRepaymentScheduleTransactionProcessor != null
                                && loanRepaymentScheduleTransactionProcessor.isInterestFirstRepaymentScheduleTransactionProcessor()) {
                            List<LoanTransaction> currentTransactions = createCurrentTransactionList(detail);
                            if (!detail.getTransactionDate().isEqual(periodStartDate)) {
                                int periodDays = Days.daysBetween(periodStartDate, detail.getTransactionDate()).getDays();
                                // calculates period start date for interest
                                // calculation as per the configuration
                                periodStartDateApplicableForInterest = calculateInterestStartDateForPeriod(loanApplicationTerms,
                                        periodStartDate, idealDisbursementDate, periodStartDateApplicableForInterest);

                                int daysInPeriodApplicable = Days.daysBetween(periodStartDateApplicableForInterest,
                                        detail.getTransactionDate()).getDays();
                                Money interestForThisinstallment = Money.zero(currency);
                                if (daysInPeriodApplicable > 0) {
                                    // 5 determine interest till the transaction
                                    // date
                                    PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                                            this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction,
                                            totalCumulativePrincipal.minus(reducePrincipal), totalCumulativeInterest,
                                            totalInterestChargedForFullLoanTerm, totalOutstandingInterestPaymentDueToGrace,
                                            outstandingBalanceAsPerRest, loanApplicationTerms, periodNumber, mc,
                                            mergeLateAndPaymentMaps(principalPortionMap, latePaymentMap, disburseDetailMap),
                                            periodStartDateApplicableForInterest, detail.getTransactionDate(),
                                            daysInPeriodApplicableForInterest);
                                    interestForThisinstallment = principalInterestForThisPeriod.interest();

                                    totalOutstandingInterestPaymentDueToGrace = principalInterestForThisPeriod.interestPaymentDueToGrace();

                                }

                                Money principalForThisPeriod = principalDisbursed.zero();

                                // applies all the applicable charges to the
                                // newly
                                // created installment
                                PrincipalInterest principalInterest = new PrincipalInterest(principalForThisPeriod,
                                        interestForThisinstallment, null);
                                Money feeChargesForInstallment = cumulativeFeeChargesDueWithin(periodStartDate,
                                        detail.getTransactionDate(), loanCharges, currency, principalInterest, principalDisbursed,
                                        totalCumulativeInterest, numberOfRepayments, true, lastTransactionDate);
                                Money penaltyChargesForInstallment = cumulativePenaltyChargesDueWithin(periodStartDate,
                                        detail.getTransactionDate(), loanCharges, currency, principalInterest, principalDisbursed,
                                        totalCumulativeInterest, numberOfRepayments, true, lastTransactionDate);

                                // 8. sum up real totalInstallmentDue from
                                // components
                                final Money totalInstallmentDue = principalForThisPeriod.plus(interestForThisinstallment)
                                        .plus(feeChargesForInstallment).plus(penaltyChargesForInstallment);
                                // 9. create repayment period from parts
                                installment = LoanScheduleModelRepaymentPeriod.repayment(instalmentNumber, periodStartDate,
                                        detail.getTransactionDate(), principalForThisPeriod, outstandingBalance,
                                        interestForThisinstallment, feeChargesForInstallment, penaltyChargesForInstallment,
                                        totalInstallmentDue, true);
                                periods.add(installment);

                                // update outstanding balance for interest
                                // calculation as per the rest
                                outstandingBalanceAsPerRest = updateBalanceForInterestCalculation(principalPortionMap,
                                        detail.getTransactionDate(), outstandingBalanceAsPerRest, false);
                                outstandingBalanceAsPerRest = updateBalanceForInterestCalculation(disburseDetailMap,
                                        detail.getTransactionDate(), outstandingBalanceAsPerRest, true);

                                // handle cumulative fields
                                loanTermInDays += periodDays;
                                totalRepaymentExpected = totalRepaymentExpected.add(totalInstallmentDue.getAmount());
                                totalCumulativeInterest = totalCumulativeInterest.plus(interestForThisinstallment);
                                totalFeeChargesCharged = totalFeeChargesCharged.add(feeChargesForInstallment.getAmount());
                                totalPenaltyChargesCharged = totalPenaltyChargesCharged.add(penaltyChargesForInstallment.getAmount());

                                periodStartDate = detail.getTransactionDate();
                                periodStartDateApplicableForInterest = periodStartDate;
                                instalmentNumber++;
                                // creates and insert Loan repayment schedule
                                // for
                                // the period
                                addLoanRepaymentScheduleInstallment(installments, installment);
                            } else if (installment == null) {
                                installment = ((List<LoanScheduleModelPeriod>) periods).get(periods.size() - 1);
                            }
                            // applies the transaction as per transaction
                            // strategy
                            // on scheduled installments to identify the
                            // unprocessed(early payment ) amounts
                            Money unprocessed = loanRepaymentScheduleTransactionProcessor.handleRepaymentSchedule(currentTransactions,
                                    currency, installments);
                            if (unprocessed.isGreaterThanZero()) {

                                if (loanApplicationTerms.getPreClosureInterestCalculationStrategy().calculateTillRestFrequencyEnabled()) {
                                    LocalDate applicableDate = getNextRestScheduleDate(detail.getTransactionDate().minusDays(1),
                                            loanApplicationTerms, holidayDetailDTO);
                                    checkForOutstanding = detail.getTransactionDate().isEqual(applicableDate);

                                }
                                // reduces actual outstanding balance
                                outstandingBalance = outstandingBalance.minus(unprocessed);
                                // if outstanding balance becomes less than zero
                                // then adjusts the princiapal
                                Money addToPrinciapal = Money.zero(currency);
                                if (!outstandingBalance.isGreaterThanZero()) {
                                    addToPrinciapal = addToPrinciapal.plus(outstandingBalance);
                                    outstandingBalance = outstandingBalance.zero();
                                    lastInstallment = installment;
                                }
                                // updates principal portion map with the early
                                // payment amounts and applicable date as per
                                // rest
                                updatePrincipalPaidPortionToMap(loanApplicationTerms, holidayDetailDTO, principalPortionMap, installment,
                                        detail, unprocessed.plus(addToPrinciapal), installments);
                                totalRepaymentExpected = totalRepaymentExpected.add(unprocessed.plus(addToPrinciapal).getAmount());
                                totalCumulativePrincipal = totalCumulativePrincipal.plus(unprocessed.plus(addToPrinciapal));

                                // method applies early payment strategy
                                reducePrincipal = reducePrincipal.plus(unprocessed);
                                reducePrincipal = applyEarlyPaymentStrategy(loanApplicationTerms, reducePrincipal);
                            }
                            // identify late payments and add compounding
                            // details to
                            // map for interest calculation
                            if (!reducePrincipal.isZero() || !installment.periodDueDate().isBefore(currentDate)) {
                                latePaymentMap.clear();
                            } else {
                                updateLatePaymentsToMap(loanApplicationTerms, holidayDetailDTO, currency, latePaymentMap, scheduledDueDate,
                                        installments, true);
                            }
                        } else if (loanRepaymentScheduleTransactionProcessor != null) {
                            LocalDate applicableDate = getNextRestScheduleDate(detail.getTransactionDate().minusDays(1),
                                    loanApplicationTerms, holidayDetailDTO);
                            if (applicableDate.isBefore(scheduledDueDate)) {
                                List<LoanTransaction> currentTransactions = createCurrentTransactionList(detail);
                                Money unprocessed = loanRepaymentScheduleTransactionProcessor.handleRepaymentSchedule(currentTransactions,
                                        currency, installments);
                                Money arrears = detail.getTransaction().getAmount(currency);
                                if (unprocessed.isGreaterThanZero()) {
                                    arrears = getTotalAmount(latePaymentMap, currency);
                                    updateMapWithAmount(principalPortionMap, unprocessed, applicableDate);
                                    earlyPaidAmount = earlyPaidAmount.plus(unprocessed);

                                    // this check is to identify pre-closure and
                                    // apply interest calculation as per
                                    // configuration
                                    if (!outstandingBalance.isGreaterThan(unprocessed)
                                            && !loanApplicationTerms.getPreClosureInterestCalculationStrategy()
                                                    .calculateTillRestFrequencyEnabled()) {

                                        LocalDate calculateTill = detail.getTransactionDate();
                                        PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                                                this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction,
                                                totalCumulativePrincipal.minus(reducePrincipal), totalCumulativeInterest,
                                                totalInterestChargedForFullLoanTerm, totalOutstandingInterestPaymentDueToGrace,
                                                outstandingBalanceAsPerRest, loanApplicationTerms, periodNumber, mc,
                                                mergeLateAndPaymentMaps(principalPortionMap, latePaymentMap, disburseDetailMap),
                                                periodStartDateApplicableForInterest, calculateTill, daysInPeriodApplicableForInterest);
                                        if (!principalInterestForThisPeriod.interest()
                                                .plus(principalInterestForThisPeriod.interestPaymentDueToGrace()).plus(outstandingBalance)
                                                .isGreaterThan(unprocessed)) {
                                            earlyPaidAmount = earlyPaidAmount.minus(unprocessed);
                                            updateMapWithAmount(principalPortionMap, unprocessed.negated(), applicableDate);
                                            LoanTransaction loanTransaction = LoanTransaction.repayment(null, unprocessed, null,
                                                    detail.getTransactionDate(), null, DateUtils.getLocalDateTimeOfTenant(), null);
                                            RecalculationDetail recalculationDetail = new RecalculationDetail(detail.getTransactionDate(),
                                                    loanTransaction);
                                            unprocessedTransactions.add(recalculationDetail);
                                            break;
                                        }
                                    }
                                    LoanTransaction loanTransaction = LoanTransaction.repayment(null, unprocessed, null, scheduledDueDate,
                                            null, DateUtils.getLocalDateTimeOfTenant(), null);
                                    RecalculationDetail recalculationDetail = new RecalculationDetail(scheduledDueDate, loanTransaction);
                                    unprocessedTransactions.add(recalculationDetail);
                                    checkForOutstanding = false;

                                    outstandingBalance = outstandingBalance.minus(unprocessed);
                                    // if outstanding balance becomes less than
                                    // zero
                                    // then adjusts the princiapal
                                    Money addToPrinciapal = Money.zero(currency);
                                    if (outstandingBalance.isLessThanZero()) {
                                        addToPrinciapal = addToPrinciapal.plus(outstandingBalance);
                                        outstandingBalance = outstandingBalance.zero();
                                        updateMapWithAmount(principalPortionMap, addToPrinciapal, applicableDate);
                                        earlyPaidAmount = earlyPaidAmount.plus(addToPrinciapal);
                                    }

                                }
                                if (arrears.isGreaterThanZero() && applicableDate.isBefore(currentDate)) {
                                    updateMapWithAmount(latePaymentMap, arrears.negated(), applicableDate);
                                    updateMapWithAmount(latePaymentMap, arrears, currentDate);
                                }
                            }

                        }
                    }

                }
                applicableTransactions.addAll(unprocessedTransactions);
                if (checkForOutstanding && outstandingBalance.isZero() && disburseDetailMap.isEmpty()) {
                    continue;
                }
            }

            int periodDays = Days.daysBetween(periodStartDate, scheduledDueDate).getDays();
            periodStartDateApplicableForInterest = calculateInterestStartDateForPeriod(loanApplicationTerms, periodStartDate,
                    idealDisbursementDate, periodStartDateApplicableForInterest);

            // 5 determine principal,interest of repayment period
            PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                    this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction,
                    totalCumulativePrincipal.minus(reducePrincipal), totalCumulativeInterest, totalInterestChargedForFullLoanTerm,
                    totalOutstandingInterestPaymentDueToGrace, outstandingBalanceAsPerRest, loanApplicationTerms, periodNumber, mc,
                    mergeLateAndPaymentMaps(principalPortionMap, latePaymentMap, disburseDetailMap), periodStartDateApplicableForInterest,
                    scheduledDueDate, daysInPeriodApplicableForInterest);

            if (loanApplicationTerms.getFixedEmiAmount() != null
                    && loanApplicationTerms.getFixedEmiAmount().compareTo(principalInterestForThisPeriod.interest().getAmount()) != 1) {
                String errorMsg = "EMI amount must be greter than : " + principalInterestForThisPeriod.interest().getAmount();
                throw new MultiDisbursementEmiAmountException(errorMsg, principalInterestForThisPeriod.interest().getAmount(),
                        loanApplicationTerms.getFixedEmiAmount());
            }

            // update cumulative fields for principal & interest
            Money interestForThisinstallment = principalInterestForThisPeriod.interest();

            totalOutstandingInterestPaymentDueToGrace = principalInterestForThisPeriod.interestPaymentDueToGrace();
            Money principalForThisPeriod = principalInterestForThisPeriod.principal();

            if (principalForThisPeriod.isZero()) {
                loanApplicationTerms.resetFixedEmiAmount();
            }

            // applies early payments on principal portion
            if (principalForThisPeriod.isGreaterThan(reducePrincipal)) {
                principalForThisPeriod = principalForThisPeriod.minus(reducePrincipal);
                reducePrincipal = reducePrincipal.zero();
            } else {
                reducePrincipal = reducePrincipal.minus(principalForThisPeriod);
                principalForThisPeriod = principalForThisPeriod.zero();
            }

            // earlyPaidAmount is already subtracted from balancereducePrincipal
            // reducePrincipal.plus(unprocessed);
            Money reducedBalance = earlyPaidAmount;
            earlyPaidAmount = earlyPaidAmount.minus(principalForThisPeriod);
            if (earlyPaidAmount.isGreaterThanZero()) {
                reducePrincipal = reducePrincipal.plus(earlyPaidAmount);
                reducePrincipal = applyEarlyPaymentStrategy(loanApplicationTerms, reducePrincipal);
                principalForThisPeriod = principalForThisPeriod.plus(earlyPaidAmount);
            }

            // 6. update outstandingLoanBlance using current period
            // 'principalDue'
            outstandingBalance = outstandingBalance.minus(principalForThisPeriod.minus(reducedBalance));

            if (outstandingBalance.isLessThanZero()) {
                principalForThisPeriod = principalForThisPeriod.plus(outstandingBalance);
                outstandingBalance = outstandingBalance.zero();
            }

            // applies charges for the period
            PrincipalInterest principalInterest = new PrincipalInterest(principalForThisPeriod, interestForThisinstallment, null);
            Money feeChargesForInstallment = cumulativeFeeChargesDueWithin(periodStartDate, scheduledDueDate, loanCharges, currency,
                    principalInterest, principalDisbursed, totalCumulativeInterest, numberOfRepayments, true, lastTransactionDate);
            Money penaltyChargesForInstallment = cumulativePenaltyChargesDueWithin(periodStartDate, scheduledDueDate, loanCharges,
                    currency, principalInterest, principalDisbursed, totalCumulativeInterest, numberOfRepayments, true, lastTransactionDate);
            totalFeeChargesCharged = totalFeeChargesCharged.add(feeChargesForInstallment.getAmount());
            totalPenaltyChargesCharged = totalPenaltyChargesCharged.add(penaltyChargesForInstallment.getAmount());

            // 8. sum up real totalInstallmentDue from components
            final Money totalInstallmentDue = principalForThisPeriod.plus(interestForThisinstallment).plus(feeChargesForInstallment)
                    .plus(penaltyChargesForInstallment);

            // if previous installment is last then add interest to same
            // installment
            if (lastInstallment != null && principalForThisPeriod.isZero()) {
                lastInstallment.addInterestAmount(interestForThisinstallment);
                continue;
            }

            // 9. create repayment period from parts
            LoanScheduleModelPeriod installment = LoanScheduleModelRepaymentPeriod.repayment(instalmentNumber, periodStartDate,
                    scheduledDueDate, principalForThisPeriod, outstandingBalance, interestForThisinstallment, feeChargesForInstallment,
                    penaltyChargesForInstallment, totalInstallmentDue, false);

            // apply loan transactions on installments to identify early/late
            // payments for interest recalculation
            if (isInterestRecalculationRequired(loanApplicationTerms, transactions) && loanRepaymentScheduleTransactionProcessor != null) {
                addLoanRepaymentScheduleInstallment(installments, installment);
                for (RecalculationDetail detail : applicableTransactions) {
                    if (!detail.isProcessed()) {
                        List<LoanTransaction> currentTransactions = new ArrayList<>(2);
                        currentTransactions.add(detail.getTransaction());
                        // applies the transaction as per transaction strategy
                        // on scheduled installments to identify the
                        // unprocessed(early payment ) amounts
                        Money unprocessed = loanRepaymentScheduleTransactionProcessor.handleRepaymentSchedule(currentTransactions,
                                currency, installments);

                        if (unprocessed.isGreaterThanZero()) {
                            outstandingBalance = outstandingBalance.minus(unprocessed);
                            // pre closure check and processing
                            if (outstandingBalance.isLessThan(interestForThisinstallment)
                                    && !scheduledDueDate.equals(detail.getTransactionDate())) {
                                LocalDate calculateTill = detail.getTransactionDate();
                                if (loanApplicationTerms.getPreClosureInterestCalculationStrategy().calculateTillRestFrequencyEnabled()) {
                                    calculateTill = getNextRestScheduleDate(calculateTill.minusDays(1), loanApplicationTerms,
                                            holidayDetailDTO);
                                }
                                PrincipalInterest interestTillDate = calculatePrincipalInterestComponentsForPeriod(
                                        this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction,
                                        totalCumulativePrincipal, totalCumulativeInterest, totalInterestChargedForFullLoanTerm,
                                        totalOutstandingInterestPaymentDueToGrace, outstandingBalanceAsPerRest, loanApplicationTerms,
                                        periodNumber, mc, mergeLateAndPaymentMaps(principalPortionMap, latePaymentMap, disburseDetailMap),
                                        periodStartDateApplicableForInterest, calculateTill, daysInPeriodApplicableForInterest);
                                Money diff = interestForThisinstallment.minus(interestTillDate.interest());
                                if (!outstandingBalance.minus(diff).isGreaterThanZero()) {
                                    outstandingBalance = outstandingBalance.minus(diff);
                                    interestForThisinstallment = interestForThisinstallment.minus(diff);
                                    principalForThisPeriod = principalForThisPeriod.plus(diff);
                                    final Money totalDue = principalForThisPeriod//
                                            .plus(interestForThisinstallment);

                                    // 9. create and replaces repayment period
                                    // from parts
                                    installment = LoanScheduleModelRepaymentPeriod.repayment(instalmentNumber, periodStartDate,
                                            detail.getTransactionDate(), principalForThisPeriod, outstandingBalance,
                                            interestForThisinstallment, feeChargesForInstallment, penaltyChargesForInstallment, totalDue,
                                            false);
                                }

                            }
                            Money addToPrinciapal = Money.zero(currency);
                            if (outstandingBalance.isLessThanZero()) {
                                addToPrinciapal = addToPrinciapal.plus(outstandingBalance);
                                outstandingBalance = outstandingBalance.zero();
                            }
                            // updates principal portion map with the early
                            // payment amounts and applicable date as per rest
                            updatePrincipalPaidPortionToMap(loanApplicationTerms, holidayDetailDTO, principalPortionMap, installment,
                                    detail, unprocessed.plus(addToPrinciapal), installments);
                            totalRepaymentExpected = totalRepaymentExpected.add(unprocessed.plus(addToPrinciapal).getAmount());
                            totalCumulativePrincipal = totalCumulativePrincipal.plus(unprocessed.plus(addToPrinciapal));

                            reducePrincipal = reducePrincipal.plus(unprocessed);
                            reducePrincipal = applyEarlyPaymentStrategy(loanApplicationTerms, reducePrincipal);

                        }
                    }

                }

                // identify late payments and adds compounding details to map
                if (!reducePrincipal.isZero() || !installment.periodDueDate().isBefore(currentDate)) {
                    latePaymentMap.clear();
                } else {
                    updateLatePaymentsToMap(loanApplicationTerms, holidayDetailDTO, currency, latePaymentMap, scheduledDueDate,
                            installments, true);
                }
            }

            periods.add(installment);

            // Updates principal paid map with efective date for reducing
            // the amount from outstanding balance(interest calculation)
            LocalDate amountApplicableDate = installment.periodDueDate();
            if (loanApplicationTerms.isInterestRecalculationEnabled()) {
                amountApplicableDate = getNextRestScheduleDate(installment.periodDueDate().minusDays(1), loanApplicationTerms,
                        holidayDetailDTO);
            }
            updateMapWithAmount(principalPortionMap, principalForThisPeriod.minus(reducedBalance), amountApplicableDate);

            // update outstanding balance for interest calculation
            outstandingBalanceAsPerRest = updateBalanceForInterestCalculation(principalPortionMap, scheduledDueDate,
                    outstandingBalanceAsPerRest, false);
            outstandingBalanceAsPerRest = updateBalanceForInterestCalculation(disburseDetailMap, scheduledDueDate,
                    outstandingBalanceAsPerRest, true);

            // handle cumulative fields
            loanTermInDays += periodDays;
            totalCumulativePrincipal = totalCumulativePrincipal.plus(principalForThisPeriod);
            totalCumulativeInterest = totalCumulativeInterest.plus(interestForThisinstallment);
            totalRepaymentExpected = totalRepaymentExpected.add(totalInstallmentDue.getAmount());
            periodStartDate = scheduledDueDate;
            periodStartDateApplicableForInterest = periodStartDate;
            instalmentNumber++;
            periodNumber++;
        }

        // 7. determine fees and penalties for charges which depends on total
        // loan interest
        for (LoanScheduleModelPeriod loanScheduleModelPeriod : periods) {
            if (loanScheduleModelPeriod.isRepaymentPeriod()) {
                PrincipalInterest principalInterest = new PrincipalInterest(Money.of(currency, loanScheduleModelPeriod.principalDue()),
                        Money.of(currency, loanScheduleModelPeriod.interestDue()), null);
                Money feeChargesForInstallment = cumulativeFeeChargesDueWithin(loanScheduleModelPeriod.periodFromDate(),
                        loanScheduleModelPeriod.periodDueDate(), nonCompoundingCharges, currency, principalInterest, principalDisbursed,
                        totalCumulativeInterest, numberOfRepayments, !loanScheduleModelPeriod.isRecalculatedInterestComponent(),
                        lastTransactionDate);
                Money penaltyChargesForInstallment = cumulativePenaltyChargesDueWithin(loanScheduleModelPeriod.periodFromDate(),
                        loanScheduleModelPeriod.periodDueDate(), nonCompoundingCharges, currency, principalInterest, principalDisbursed,
                        totalCumulativeInterest, numberOfRepayments, !loanScheduleModelPeriod.isRecalculatedInterestComponent(),
                        lastTransactionDate);
                totalFeeChargesCharged = totalFeeChargesCharged.add(feeChargesForInstallment.getAmount());
                totalPenaltyChargesCharged = totalPenaltyChargesCharged.add(penaltyChargesForInstallment.getAmount());
                totalRepaymentExpected = totalRepaymentExpected.add(feeChargesForInstallment.getAmount()).add(
                        penaltyChargesForInstallment.getAmount());
                loanScheduleModelPeriod.addLoanCharges(feeChargesForInstallment.getAmount(), penaltyChargesForInstallment.getAmount());
            }
        }

        // this block is to add extra re-payment schedules with interest portion
        // if the loan not paid with in loan term

        if (isInterestRecalculationRequired(loanApplicationTerms, transactions) && latePaymentMap.size() > 0
                && currentDate.isAfter(periodStartDate)) {
            Money totalInterest = addInterestOnlyRepaymentScheduleForCurrentdate(mc, loanApplicationTerms, holidayDetailDTO, currency,
                    periods, periodStartDate, actualRepaymentDate, instalmentNumber, latePaymentMap, currentDate,
                    loanRepaymentScheduleTransactionProcessor, transactions, installments);
            totalCumulativeInterest = totalCumulativeInterest.plus(totalInterest);
        }

        loanApplicationTerms.resetFixedEmiAmount();

        return LoanScheduleModel.from(periods, applicationCurrency, loanTermInDays, principalDisbursed,
                totalCumulativePrincipal.getAmount(), totalPrincipalPaid, totalCumulativeInterest.getAmount(), totalFeeChargesCharged,
                totalPenaltyChargesCharged, totalRepaymentExpected, totalOutstanding);
    }

    private boolean isInterestRecalculationRequired(final LoanApplicationTerms loanApplicationTerms,
            Collection<RecalculationDetail> transactions) {
        return loanApplicationTerms.isInterestRecalculationEnabled() && transactions != null;
    }

    /**
     * Method calculates interest on not paid outstanding principal and interest
     * (if compounding is enabled) till current date and adds new repayment
     * schedule detail
     * 
     */
    private Money addInterestOnlyRepaymentScheduleForCurrentdate(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            final HolidayDetailDTO holidayDetailDTO, final MonetaryCurrency currency, final Collection<LoanScheduleModelPeriod> periods,
            LocalDate periodStartDate, LocalDate actualRepaymentDate, int instalmentNumber, Map<LocalDate, Money> latePaymentMap,
            LocalDate currentDate, LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor,
            final Collection<RecalculationDetail> transactions, List<LoanRepaymentScheduleInstallment> installments) {
        boolean isFirstRepayment = false;
        LocalDate startDate = periodStartDate;
        updateLatePaymentsToMap(loanApplicationTerms, holidayDetailDTO, currency, latePaymentMap, currentDate, installments, false);
        Money outstanding = Money.zero(currency);
        Money totalInterest = Money.zero(currency);
        Money totalCumulativeInterest = Money.zero(currency);
        Map<LocalDate, Money> retainEntries = new HashMap<>();
        double interestCalculationGraceOnRepaymentPeriodFraction = Double.valueOf(0);
        int periodNumberTemp = 1;
        do {

            actualRepaymentDate = this.scheduledDateGenerator.generateNextRepaymentDate(actualRepaymentDate, loanApplicationTerms,
                    isFirstRepayment);
            int daysInPeriod = Days.daysBetween(periodStartDate, actualRepaymentDate).getDays();
            if (actualRepaymentDate.isAfter(currentDate)) {
                actualRepaymentDate = currentDate;
            }
            outstanding = updateOutstandingFromLatePayment(periodStartDate, latePaymentMap, outstanding, retainEntries);

            Collection<RecalculationDetail> applicableTransactions = getApplicableTransactionsForPeriod(loanApplicationTerms,
                    actualRepaymentDate, transactions);

            for (RecalculationDetail detail : applicableTransactions) {
                if (detail.isProcessed()) {
                    continue;
                }
                List<LoanTransaction> currentTransactions = createCurrentTransactionList(detail);

                if (!periodStartDate.isEqual(detail.getTransactionDate())) {
                    PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                            this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction,
                            totalInterest.zero(), totalInterest.zero(), totalInterest.zero(), totalInterest.zero(), outstanding,
                            loanApplicationTerms, periodNumberTemp, mc, latePaymentMap, periodStartDate, detail.getTransactionDate(),
                            daysInPeriod);

                    Money interest = principalInterestForThisPeriod.interest();
                    totalInterest = totalInterest.plus(interest);

                    LoanScheduleModelRepaymentPeriod installment = LoanScheduleModelRepaymentPeriod.repayment(instalmentNumber++,
                            startDate, detail.getTransactionDate(), totalInterest.zero(), totalInterest.zero(), totalInterest,
                            totalInterest.zero(), totalInterest.zero(), totalInterest, true);
                    periods.add(installment);
                    totalCumulativeInterest = totalCumulativeInterest.plus(totalInterest);
                    totalInterest = totalInterest.zero();
                    addLoanRepaymentScheduleInstallment(installments, installment);
                    periodStartDate = detail.getTransactionDate();
                    startDate = detail.getTransactionDate();
                }
                loanRepaymentScheduleTransactionProcessor.handleRepaymentSchedule(currentTransactions, currency, installments);
                updateLatePaymentsToMap(loanApplicationTerms, holidayDetailDTO, currency, latePaymentMap, currentDate, installments, false);
                outstanding = outstanding.zero();
                outstanding = updateOutstandingFromLatePayment(periodStartDate, latePaymentMap, outstanding, retainEntries);
                if (latePaymentMap.isEmpty() && !outstanding.isGreaterThanZero()) {
                    break;
                }
            }

            if (outstanding.isGreaterThanZero()) {
                PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                        this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction, totalInterest.zero(),
                        totalInterest.zero(), totalInterest.zero(), totalInterest.zero(), outstanding, loanApplicationTerms,
                        periodNumberTemp, mc, latePaymentMap, periodStartDate, actualRepaymentDate, daysInPeriod);
                Money interest = principalInterestForThisPeriod.interest();
                totalInterest = totalInterest.plus(interest);
                if (loanApplicationTerms.getInterestRecalculationCompoundingMethod().isInterestCompoundingEnabled()) {
                    LocalDate compoundingEffectiveDate = getNextCompoundScheduleDate(actualRepaymentDate.minusDays(1),
                            loanApplicationTerms, holidayDetailDTO);
                    latePaymentMap.put(compoundingEffectiveDate, interest);

                }
            }
            periodStartDate = actualRepaymentDate;
        } while (actualRepaymentDate.isBefore(currentDate) && outstanding.isGreaterThanZero());

        if (totalInterest.isGreaterThanZero()) {
            LoanScheduleModelRepaymentPeriod installment = LoanScheduleModelRepaymentPeriod.repayment(instalmentNumber++, startDate,
                    actualRepaymentDate, totalInterest.zero(), totalInterest.zero(), totalInterest, totalInterest.zero(),
                    totalInterest.zero(), totalInterest, true);
            periods.add(installment);
            totalCumulativeInterest = totalCumulativeInterest.plus(totalInterest);
        }
        return totalCumulativeInterest;
    }

    private Collection<RecalculationDetail> getApplicableTransactionsForPeriod(final LoanApplicationTerms loanApplicationTerms,
            LocalDate repaymentDate, final Collection<RecalculationDetail> transactions) {
        Collection<RecalculationDetail> applicableTransactions = new ArrayList<>();
        if (isInterestRecalculationRequired(loanApplicationTerms, transactions)) {
            for (RecalculationDetail detail : transactions) {
                if (!detail.getTransactionDate().isAfter(repaymentDate)) {
                    applicableTransactions.add(detail);
                }
            }
            transactions.removeAll(applicableTransactions);
        }
        return applicableTransactions;
    }

    private List<LoanTransaction> createCurrentTransactionList(RecalculationDetail detail) {
        List<LoanTransaction> currentTransactions = new ArrayList<>(2);
        currentTransactions.add(detail.getTransaction());
        detail.setProcessed(true);
        return currentTransactions;
    }

    private Money updateOutstandingFromLatePayment(LocalDate periodStartDate, Map<LocalDate, Money> latePaymentMap, Money outstanding,
            Map<LocalDate, Money> retainEntries) {
        for (Map.Entry<LocalDate, Money> mapEntry : latePaymentMap.entrySet()) {
            if (!mapEntry.getKey().isAfter(periodStartDate)) {
                outstanding = outstanding.plus(mapEntry.getValue());
            } else {
                retainEntries.put(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        latePaymentMap.clear();
        latePaymentMap.putAll(retainEntries);
        retainEntries.clear();
        return outstanding;
    }

    /**
     * method applies early payment strategy as per the configurations provided
     */
    private Money applyEarlyPaymentStrategy(final LoanApplicationTerms loanApplicationTerms, Money reducePrincipal) {
        if (reducePrincipal.isGreaterThanZero()) {
            switch (loanApplicationTerms.getRescheduleStrategyMethod()) {
                case REDUCE_EMI_AMOUNT:
                    // in this case emi amount will be reduced but number of
                    // installments won't change
                    if (!loanApplicationTerms.isMultiDisburseLoan()) {
                        loanApplicationTerms.setFixedEmiAmount(null);
                    }
                    reducePrincipal = reducePrincipal.zero();
                break;
                case REDUCE_NUMBER_OF_INSTALLMENTS:
                    // number of installments will reduce but emi amount won't
                    // get effected
                    reducePrincipal = reducePrincipal.zero();
                break;
                case RESCHEDULE_NEXT_REPAYMENTS:
                // will reduce principal from the reduce Principal for each
                // installment(means installments will have less emi amount)
                // until this
                // amount becomes zero
                break;
                default:
                break;
            }
        }
        return reducePrincipal;
    }

    /**
     * Identifies all the past date principal changes and apply them on
     * outstanding balance for future calculations
     */
    private Money updateBalanceForInterestCalculation(final Map<LocalDate, Money> principalPortionMap, final LocalDate scheduledDueDate,
            final Money outstandingBalanceAsPerRest, boolean addMapDetails) {
        List<LocalDate> removeFromprincipalPortionMap = new ArrayList<>();
        Money outstandingBalance = outstandingBalanceAsPerRest;
        for (Map.Entry<LocalDate, Money> principal : principalPortionMap.entrySet()) {
            if (!principal.getKey().isAfter(scheduledDueDate)) {
                if (addMapDetails) {
                    outstandingBalance = outstandingBalance.plus(principal.getValue());
                } else {
                    outstandingBalance = outstandingBalance.minus(principal.getValue());
                }
                removeFromprincipalPortionMap.add(principal.getKey());
            }
        }
        for (LocalDate date : removeFromprincipalPortionMap) {
            principalPortionMap.remove(date);
        }
        return outstandingBalance;
    }

    /**
     * this Method updates late/ not paid installment components to Map with
     * effective date as per REST(for principal portion ) and compounding
     * (interest or fee or interest and fee portions) frequency
     * 
     */
    private void updateLatePaymentsToMap(final LoanApplicationTerms loanApplicationTerms, final HolidayDetailDTO holidayDetailDTO,
            final MonetaryCurrency currency, final Map<LocalDate, Money> latePaymentMap, final LocalDate scheduledDueDate,
            List<LoanRepaymentScheduleInstallment> installments, boolean applyRestFrequencyForPrincipal) {
        latePaymentMap.clear();
        Money totalCompoundingAmount = Money.zero(currency);
        for (LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : installments) {
            if (loanRepaymentScheduleInstallment.isNotFullyPaidOff()
                    && !loanRepaymentScheduleInstallment.getDueDate().isAfter(scheduledDueDate)
                    && !loanRepaymentScheduleInstallment.isRecalculatedInterestComponent()) {
                LocalDate principalEffectiveDate = loanRepaymentScheduleInstallment.getDueDate();
                if (applyRestFrequencyForPrincipal) {
                    principalEffectiveDate = getNextRestScheduleDate(loanRepaymentScheduleInstallment.getDueDate().minusDays(1),
                            loanApplicationTerms, holidayDetailDTO);
                }
                if (principalEffectiveDate.isBefore(DateUtils.getLocalDateOfTenant())) {
                    updateMapWithAmount(latePaymentMap, loanRepaymentScheduleInstallment.getPrincipalOutstanding(currency),
                            principalEffectiveDate);
                    totalCompoundingAmount = totalCompoundingAmount
                            .plus(loanRepaymentScheduleInstallment.getPrincipalOutstanding(currency));
                }
                totalCompoundingAmount = updateMapWithCompoundingDetails(loanApplicationTerms, holidayDetailDTO, currency, latePaymentMap,
                        totalCompoundingAmount, loanRepaymentScheduleInstallment);
            }
        }

        if (totalCompoundingAmount.isGreaterThanZero()) {
            updateMapWithAmount(latePaymentMap, totalCompoundingAmount.negated(), DateUtils.getLocalDateOfTenant());
        }
    }

    private Money updateMapWithCompoundingDetails(final LoanApplicationTerms loanApplicationTerms, final HolidayDetailDTO holidayDetailDTO,
            final MonetaryCurrency currency, final Map<LocalDate, Money> latePaymentMap, Money totalCompoundingAmount,
            LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment) {
        if (loanApplicationTerms.getInterestRecalculationCompoundingMethod().isCompoundingEnabled()) {
            LocalDate compoundingEffectiveDate = getNextCompoundScheduleDate(loanRepaymentScheduleInstallment.getDueDate().minusDays(1),
                    loanApplicationTerms, holidayDetailDTO);
            if (compoundingEffectiveDate.isBefore(DateUtils.getLocalDateOfTenant())) {
                Money amount = Money.zero(currency);
                switch (loanApplicationTerms.getInterestRecalculationCompoundingMethod()) {
                    case INTEREST:
                        amount = amount.plus(loanRepaymentScheduleInstallment.getInterestOutstanding(currency));
                    break;
                    case FEE:
                        amount = amount.plus(loanRepaymentScheduleInstallment.getFeeChargesOutstanding(currency));
                        amount = amount.plus(loanRepaymentScheduleInstallment.getPenaltyChargesOutstanding(currency));
                    break;
                    case INTEREST_AND_FEE:
                        amount = amount.plus(loanRepaymentScheduleInstallment.getInterestOutstanding(currency));
                        amount = amount.plus(loanRepaymentScheduleInstallment.getFeeChargesOutstanding(currency));
                        amount = amount.plus(loanRepaymentScheduleInstallment.getPenaltyChargesOutstanding(currency));
                    break;
                    default:
                    break;
                }
                updateMapWithAmount(latePaymentMap, amount, compoundingEffectiveDate);
                totalCompoundingAmount = totalCompoundingAmount.plus(amount);
            }
        }
        return totalCompoundingAmount;
    }

    /**
     * This Method updates principal paid component to map with effective date
     * as per the REST
     * 
     */
    private void updatePrincipalPaidPortionToMap(final LoanApplicationTerms loanApplicationTerms, final HolidayDetailDTO holidayDetailDTO,
            Map<LocalDate, Money> principalPortionMap, final LoanScheduleModelPeriod installment, final RecalculationDetail detail,
            final Money unprocessed, final List<LoanRepaymentScheduleInstallment> installments) {
        LocalDate applicableDate = getNextRestScheduleDate(detail.getTransactionDate().minusDays(1), loanApplicationTerms, holidayDetailDTO);
        updateMapWithAmount(principalPortionMap, unprocessed, applicableDate);
        installment.addPrincipalAmount(unprocessed);
        LoanRepaymentScheduleInstallment lastInstallment = installments.get(installments.size() - 1);
        lastInstallment.updatePrincipal(lastInstallment.getPrincipal(unprocessed.getCurrency()).plus(unprocessed).getAmount());
        lastInstallment.payPrincipalComponent(detail.getTransactionDate(), unprocessed);
    }

    /**
     * merges all the applicable amounts(disbursements, late payment compounding
     * and principal change as per rest) changes to single map for interest
     * calculation
     */
    private Map<LocalDate, Money> mergeLateAndPaymentMaps(final Map<LocalDate, Money> princiaplPaidMap,
            final Map<LocalDate, Money> latePaymentMap, Map<LocalDate, Money> disburseDetailsMap) {
        Map<LocalDate, Money> map = new TreeMap<>();
        map.putAll(latePaymentMap);

        for (Map.Entry<LocalDate, Money> mapEntry : disburseDetailsMap.entrySet()) {
            Money value = mapEntry.getValue();
            if (map.containsKey(mapEntry.getKey())) {
                value = value.plus(map.get(mapEntry.getKey()));
            }
            map.put(mapEntry.getKey(), value);
        }

        for (Map.Entry<LocalDate, Money> mapEntry : princiaplPaidMap.entrySet()) {
            Money value = mapEntry.getValue().negated();
            if (map.containsKey(mapEntry.getKey())) {
                value = value.plus(map.get(mapEntry.getKey()));
            }
            map.put(mapEntry.getKey(), value);
        }
        return map;
    }

    /**
     * calculates Interest stating date as per the settings
     */
    private LocalDate calculateInterestStartDateForPeriod(final LoanApplicationTerms loanApplicationTerms, LocalDate periodStartDate,
            final LocalDate idealDisbursementDate, LocalDate periodStartDateApplicableForInterest) {
        if (periodStartDate.isBefore(idealDisbursementDate)) {
            if (loanApplicationTerms.getInterestChargedFromLocalDate() != null) {
                periodStartDateApplicableForInterest = loanApplicationTerms.getInterestChargedFromLocalDate();
            } else {
                periodStartDateApplicableForInterest = idealDisbursementDate;
            }
        }
        return periodStartDateApplicableForInterest;
    }

    private void updateMapWithAmount(final Map<LocalDate, Money> map, final Money amount, final LocalDate amountApplicableDate) {
        Money principalPaid = amount;
        if (map.containsKey(amountApplicableDate)) {
            principalPaid = map.get(amountApplicableDate).plus(principalPaid);
        }
        map.put(amountApplicableDate, principalPaid);
    }

    private Money getTotalAmount(final Map<LocalDate, Money> map, final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (Map.Entry<LocalDate, Money> mapEntry : map.entrySet()) {
            if (mapEntry.getKey().isBefore(DateUtils.getLocalDateOfTenant())) {
                total = total.plus(mapEntry.getValue());
            }
        }
        return total;
    }

    @Override
    public LoanRescheduleModel reschedule(final MathContext mathContext, final LoanRescheduleRequest loanRescheduleRequest,
            final ApplicationCurrency applicationCurrency, final HolidayDetailDTO holidayDetailDTO,
            final CalendarInstance restCalendarInstance) {

        final Loan loan = loanRescheduleRequest.getLoan();
        final LoanSummary loanSummary = loan.getSummary();
        final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail = loan.getLoanRepaymentScheduleDetail();
        final MonetaryCurrency currency = loanProductRelatedDetail.getCurrency();

        // create an archive of the current loan schedule installments
        Collection<LoanRepaymentScheduleHistory> loanRepaymentScheduleHistoryList = null;

        // get the initial list of repayment installments
        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = loan.getRepaymentScheduleInstallments();

        // sort list by installment number in ASC order
        Collections.sort(repaymentScheduleInstallments, LoanRepaymentScheduleInstallment.installmentNumberComparator);

        final Collection<LoanRescheduleModelRepaymentPeriod> periods = new ArrayList<>();

        Money outstandingLoanBalance = loan.getPrincpal();

        for (LoanRepaymentScheduleInstallment repaymentScheduleInstallment : repaymentScheduleInstallments) {

            Integer oldPeriodNumber = repaymentScheduleInstallment.getInstallmentNumber();
            LocalDate fromDate = repaymentScheduleInstallment.getFromDate();
            LocalDate dueDate = repaymentScheduleInstallment.getDueDate();
            Money principalDue = repaymentScheduleInstallment.getPrincipal(currency);
            Money interestDue = repaymentScheduleInstallment.getInterestCharged(currency);
            Money feeChargesDue = repaymentScheduleInstallment.getFeeChargesCharged(currency);
            Money penaltyChargesDue = repaymentScheduleInstallment.getPenaltyChargesCharged(currency);
            Money totalDue = principalDue.plus(interestDue).plus(feeChargesDue).plus(penaltyChargesDue);

            outstandingLoanBalance = outstandingLoanBalance.minus(principalDue);

            LoanRescheduleModelRepaymentPeriod period = LoanRescheduleModelRepaymentPeriod
                    .instance(oldPeriodNumber, oldPeriodNumber, fromDate, dueDate, principalDue, outstandingLoanBalance, interestDue,
                            feeChargesDue, penaltyChargesDue, totalDue, false);

            periods.add(period);
        }

        Money outstandingBalance = loan.getPrincpal();
        Money totalCumulativePrincipal = Money.zero(currency);
        Money totalCumulativeInterest = Money.zero(currency);
        Money actualTotalCumulativeInterest = Money.zero(currency);
        Money totalOutstandingInterestPaymentDueToGrace = Money.zero(currency);
        Money totalPrincipalBeforeReschedulePeriod = Money.zero(currency);

        LocalDate installmentDueDate = null;
        LocalDate adjustedInstallmentDueDate = null;
        LocalDate installmentFromDate = null;
        Integer rescheduleFromInstallmentNo = defaultToZeroIfNull(loanRescheduleRequest.getRescheduleFromInstallment());
        Integer installmentNumber = rescheduleFromInstallmentNo;
        Integer graceOnPrincipal = defaultToZeroIfNull(loanRescheduleRequest.getGraceOnPrincipal());
        Integer graceOnInterest = defaultToZeroIfNull(loanRescheduleRequest.getGraceOnInterest());
        Integer extraTerms = defaultToZeroIfNull(loanRescheduleRequest.getExtraTerms());
        final boolean recalculateInterest = loanRescheduleRequest.getRecalculateInterest();
        Integer numberOfRepayments = repaymentScheduleInstallments.size();
        Integer rescheduleNumberOfRepayments = numberOfRepayments;
        final Money principal = loan.getPrincpal();
        final Money totalPrincipalOutstanding = Money.of(currency, loanSummary.getTotalPrincipalOutstanding());
        LocalDate adjustedDueDate = loanRescheduleRequest.getAdjustedDueDate();
        BigDecimal newInterestRate = loanRescheduleRequest.getInterestRate();
        int loanTermInDays = Integer.valueOf(0);

        if (rescheduleFromInstallmentNo > 0) {
            // this will hold the loan repayment installment that is before the
            // reschedule start installment
            // (rescheduleFrominstallment)
            LoanRepaymentScheduleInstallment previousInstallment = null;

            // get the install number of the previous installment
            int previousInstallmentNo = rescheduleFromInstallmentNo - 1;

            // only fetch the installment if the number is greater than 0
            if (previousInstallmentNo > 0) {
                previousInstallment = loan.fetchRepaymentScheduleInstallment(previousInstallmentNo);
            }

            LoanRepaymentScheduleInstallment firstInstallment = loan.fetchRepaymentScheduleInstallment(1);

            // the "installment from date" is equal to the due date of the
            // previous installment, if it exists
            if (previousInstallment != null) {
                installmentFromDate = previousInstallment.getDueDate();
            }

            else {
                installmentFromDate = firstInstallment.getFromDate();
            }

            installmentDueDate = installmentFromDate;
            LocalDate periodStartDateApplicableForInterest = installmentFromDate;
            Integer periodNumber = 1;
            outstandingLoanBalance = loan.getPrincpal();

            for (LoanRescheduleModelRepaymentPeriod period : periods) {

                if (period.periodDueDate().isBefore(loanRescheduleRequest.getRescheduleFromDate())) {

                    totalPrincipalBeforeReschedulePeriod = totalPrincipalBeforeReschedulePeriod.plus(period.principalDue());
                    actualTotalCumulativeInterest = actualTotalCumulativeInterest.plus(period.interestDue());
                    rescheduleNumberOfRepayments--;
                    outstandingLoanBalance = outstandingLoanBalance.minus(period.principalDue());
                    outstandingBalance = outstandingBalance.minus(period.principalDue());
                }
            }

            while (graceOnPrincipal > 0 || graceOnInterest > 0) {

                LoanRescheduleModelRepaymentPeriod period = LoanRescheduleModelRepaymentPeriod.instance(0, 0, new LocalDate(),
                        new LocalDate(), Money.zero(currency), Money.zero(currency), Money.zero(currency), Money.zero(currency),
                        Money.zero(currency), Money.zero(currency), true);

                periods.add(period);

                if (graceOnPrincipal > 0) {
                    graceOnPrincipal--;
                }

                if (graceOnInterest > 0) {
                    graceOnInterest--;
                }

                rescheduleNumberOfRepayments++;
                numberOfRepayments++;
            }

            while (extraTerms > 0) {

                LoanRescheduleModelRepaymentPeriod period = LoanRescheduleModelRepaymentPeriod.instance(0, 0, new LocalDate(),
                        new LocalDate(), Money.zero(currency), Money.zero(currency), Money.zero(currency), Money.zero(currency),
                        Money.zero(currency), Money.zero(currency), true);

                periods.add(period);

                extraTerms--;
                rescheduleNumberOfRepayments++;
                numberOfRepayments++;
            }

            // get the loan application terms from the Loan object
            final LoanApplicationTerms loanApplicationTerms = loan.getLoanApplicationTerms(applicationCurrency, restCalendarInstance);

            // update the number of repayments
            loanApplicationTerms.updateNumberOfRepayments(numberOfRepayments);

            LocalDate loanEndDate = this.scheduledDateGenerator.getLastRepaymentDate(loanApplicationTerms, holidayDetailDTO);
            loanApplicationTerms.updateLoanEndDate(loanEndDate);

            if (newInterestRate != null) {
                loanApplicationTerms.updateAnnualNominalInterestRate(newInterestRate);
                loanApplicationTerms.updateInterestRatePerPeriod(newInterestRate);
            }

            graceOnPrincipal = defaultToZeroIfNull(loanRescheduleRequest.getGraceOnPrincipal());
            graceOnInterest = defaultToZeroIfNull(loanRescheduleRequest.getGraceOnInterest());

            loanApplicationTerms.updateInterestPaymentGrace(graceOnInterest);
            loanApplicationTerms.updatePrincipalGrace(graceOnPrincipal);

            loanApplicationTerms.setPrincipal(totalPrincipalOutstanding);
            loanApplicationTerms.updateNumberOfRepayments(rescheduleNumberOfRepayments);
            loanApplicationTerms.updateLoanTermFrequency(rescheduleNumberOfRepayments);
            loanApplicationTerms.updateInterestChargedFromDate(periodStartDateApplicableForInterest);

            Money totalInterestChargedForFullLoanTerm = loanApplicationTerms.calculateTotalInterestCharged(
                    this.paymentPeriodsInOneYearCalculator, mathContext);

            if (!recalculateInterest && newInterestRate == null) {
                totalInterestChargedForFullLoanTerm = Money.of(currency, loanSummary.getTotalInterestCharged());
                totalInterestChargedForFullLoanTerm = totalInterestChargedForFullLoanTerm.minus(actualTotalCumulativeInterest);

                loanApplicationTerms.updateTotalInterestDue(totalInterestChargedForFullLoanTerm);
            }

            for (LoanRescheduleModelRepaymentPeriod period : periods) {

                if (period.periodDueDate().isEqual(loanRescheduleRequest.getRescheduleFromDate())
                        || period.periodDueDate().isAfter(loanRescheduleRequest.getRescheduleFromDate()) || period.isNew()) {

                    installmentDueDate = this.scheduledDateGenerator.generateNextRepaymentDate(installmentDueDate, loanApplicationTerms,
                            false);

                    if (adjustedDueDate != null && periodNumber == 1) {
                        installmentDueDate = adjustedDueDate;
                    }

                    adjustedInstallmentDueDate = this.scheduledDateGenerator.adjustRepaymentDate(installmentDueDate, loanApplicationTerms,
                            holidayDetailDTO);

                    final int daysInInstallment = Days.daysBetween(installmentFromDate, adjustedInstallmentDueDate).getDays();

                    period.updatePeriodNumber(installmentNumber);
                    period.updatePeriodFromDate(installmentFromDate);
                    period.updatePeriodDueDate(adjustedInstallmentDueDate);

                    double interestCalculationGraceOnRepaymentPeriodFraction = this.paymentPeriodsInOneYearCalculator
                            .calculatePortionOfRepaymentPeriodInterestChargingGrace(periodStartDateApplicableForInterest,
                                    adjustedInstallmentDueDate, periodStartDateApplicableForInterest,
                                    loanApplicationTerms.getLoanTermPeriodFrequencyType(), loanApplicationTerms.getRepaymentEvery());

                    // ========================= Calculate the interest due
                    // ========================================

                    // change the principal to => Principal Disbursed - Total
                    // Principal Paid
                    // interest calculation is always based on the total
                    // principal outstanding
                    loanApplicationTerms.setPrincipal(totalPrincipalOutstanding);

                    // determine the interest & principal for the period
                    PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                            this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction,
                            totalCumulativePrincipal, totalCumulativeInterest, totalInterestChargedForFullLoanTerm,
                            totalOutstandingInterestPaymentDueToGrace, outstandingBalance, loanApplicationTerms, periodNumber, mathContext,
                            null, installmentFromDate, adjustedInstallmentDueDate, daysInInstallment);

                    // update the interest due for the period
                    period.updateInterestDue(principalInterestForThisPeriod.interest());

                    // =============================================================================================

                    // ========================== Calculate the principal due
                    // ======================================

                    // change the principal to => Principal Disbursed - Total
                    // cumulative Principal Amount before the reschedule
                    // installment
                    loanApplicationTerms.setPrincipal(principal.minus(totalPrincipalBeforeReschedulePeriod));

                    principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(this.paymentPeriodsInOneYearCalculator,
                            interestCalculationGraceOnRepaymentPeriodFraction, totalCumulativePrincipal, totalCumulativeInterest,
                            totalInterestChargedForFullLoanTerm, totalOutstandingInterestPaymentDueToGrace, outstandingBalance,
                            loanApplicationTerms, periodNumber, mathContext, null, installmentFromDate, adjustedInstallmentDueDate,
                            daysInInstallment);

                    period.updatePrincipalDue(principalInterestForThisPeriod.principal());

                    // ==============================================================================================

                    outstandingLoanBalance = outstandingLoanBalance.minus(period.principalDue());
                    period.updateOutstandingLoanBalance(outstandingLoanBalance);

                    Money principalDue = Money.of(currency, period.principalDue());
                    Money interestDue = Money.of(currency, period.interestDue());

                    if (principalDue.isZero() && interestDue.isZero()) {
                        period.updateFeeChargesDue(Money.zero(currency));
                        period.updatePenaltyChargesDue(Money.zero(currency));
                    }

                    Money feeChargesDue = Money.of(currency, period.feeChargesDue());
                    Money penaltyChargesDue = Money.of(currency, period.penaltyChargesDue());

                    Money totalDue = principalDue.plus(interestDue).plus(feeChargesDue).plus(penaltyChargesDue);

                    period.updateTotalDue(totalDue);

                    // update cumulative fields for principal & interest
                    totalCumulativePrincipal = totalCumulativePrincipal.plus(period.principalDue());
                    totalCumulativeInterest = totalCumulativeInterest.plus(period.interestDue());
                    actualTotalCumulativeInterest = actualTotalCumulativeInterest.plus(period.interestDue());
                    totalOutstandingInterestPaymentDueToGrace = principalInterestForThisPeriod.interestPaymentDueToGrace();

                    installmentFromDate = adjustedInstallmentDueDate;
                    installmentNumber++;
                    periodNumber++;
                    loanTermInDays += daysInInstallment;

                    outstandingBalance = outstandingBalance.minus(period.principalDue());
                }
            }
        }

        final Money totalRepaymentExpected = principal // get the loan Principal
                                                       // amount
                .plus(actualTotalCumulativeInterest) // add the actual total
                                                     // cumulative interest
                .plus(loanSummary.getTotalFeeChargesCharged()) // add the total
                                                               // fees charged
                .plus(loanSummary.getTotalPenaltyChargesCharged()); // finally
                                                                    // add the
                                                                    // total
                                                                    // penalty
                                                                    // charged

        return LoanRescheduleModel.instance(periods, loanRepaymentScheduleHistoryList, applicationCurrency, loanTermInDays,
                loan.getPrincpal(), loan.getPrincpal().getAmount(), loanSummary.getTotalPrincipalRepaid(),
                actualTotalCumulativeInterest.getAmount(), loanSummary.getTotalFeeChargesCharged(),
                loanSummary.getTotalPenaltyChargesCharged(), totalRepaymentExpected.getAmount(), loanSummary.getTotalOutstanding());
    }

    protected double calculateInterestForDays(int daysInPeriodApplicableForInterest, BigDecimal interest, int days) {
        if (interest.doubleValue() == 0 || days == 0) { return 0; }
        return ((interest.doubleValue()) / daysInPeriodApplicableForInterest) * days;
    }

    public abstract PrincipalInterest calculatePrincipalInterestComponentsForPeriod(PaymentPeriodsInOneYearCalculator calculator,
            double interestCalculationGraceOnRepaymentPeriodFraction, Money totalCumulativePrincipal, Money totalCumulativeInterest,
            Money totalInterestDueForLoan, Money cumulatingInterestPaymentDueToGrace, Money outstandingBalance,
            LoanApplicationTerms loanApplicationTerms, int periodNumber, MathContext mc, Map<LocalDate, Money> principalVariation,
            LocalDate periodStartDate, LocalDate periodEndDate, int daysForInterestInFullPeriod);

    protected final boolean isLastRepaymentPeriod(final int numberOfRepayments, final int periodNumber) {
        return periodNumber == numberOfRepayments;
    }

    private BigDecimal deriveTotalChargesDueAtTimeOfDisbursement(final Set<LoanCharge> loanCharges) {
        BigDecimal chargesDueAtTimeOfDisbursement = BigDecimal.ZERO;
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueAtDisbursement()) {
                chargesDueAtTimeOfDisbursement = chargesDueAtTimeOfDisbursement.add(loanCharge.amount());
            }
        }
        return chargesDueAtTimeOfDisbursement;
    }

    private BigDecimal getDisbursementAmount(final LoanApplicationTerms loanApplicationTerms, LocalDate disbursementDate,
            final Collection<LoanScheduleModelPeriod> periods, final BigDecimal chargesDueAtTimeOfDisbursement,
            final Map<LocalDate, Money> disurseDetail, final boolean excludePastUndisbursed) {
        BigDecimal principal = BigDecimal.ZERO;
        MonetaryCurrency currency = loanApplicationTerms.getPrincipal().getCurrency();
        for (DisbursementData disbursementData : loanApplicationTerms.getDisbursementDatas()) {
            if (disbursementData.disbursementDate().equals(disbursementDate)) {
                final LoanScheduleModelDisbursementPeriod disbursementPeriod = LoanScheduleModelDisbursementPeriod.disbursement(
                        disbursementData.disbursementDate(), Money.of(currency, disbursementData.amount()), chargesDueAtTimeOfDisbursement);
                periods.add(disbursementPeriod);
                principal = principal.add(disbursementData.amount());
            } else if (!excludePastUndisbursed || disbursementData.isDisbursed()
                    || !disbursementData.disbursementDate().isBefore(DateUtils.getLocalDateOfTenant())) {
                disurseDetail.put(disbursementData.disbursementDate(), Money.of(currency, disbursementData.amount()));
            }
        }
        return principal;
    }

    private Collection<LoanScheduleModelPeriod> createNewLoanScheduleListWithDisbursementDetails(final int numberOfRepayments,
            final LoanApplicationTerms loanApplicationTerms, final BigDecimal chargesDueAtTimeOfDisbursement) {

        Collection<LoanScheduleModelPeriod> periods = null;
        if (loanApplicationTerms.isMultiDisburseLoan()) {
            periods = new ArrayList<>(numberOfRepayments + loanApplicationTerms.getDisbursementDatas().size());
        } else {
            periods = new ArrayList<>(numberOfRepayments + 1);
            final LoanScheduleModelDisbursementPeriod disbursementPeriod = LoanScheduleModelDisbursementPeriod.disbursement(
                    loanApplicationTerms, chargesDueAtTimeOfDisbursement);
            periods.add(disbursementPeriod);
        }

        return periods;
    }

    private Set<LoanCharge> seperateTotalCompoundingPercentageCharges(final Set<LoanCharge> loanCharges) {
        Set<LoanCharge> interestCharges = new HashSet<>();
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isSpecifiedDueDate()
                    && (loanCharge.getChargeCalculation().isPercentageOfInterest() || loanCharge.getChargeCalculation()
                            .isPercentageOfAmountAndInterest())) {
                interestCharges.add(loanCharge);
            }
        }
        loanCharges.removeAll(interestCharges);
        return interestCharges;
    }

    private Money cumulativeFeeChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd, final Set<LoanCharge> loanCharges,
            final MonetaryCurrency monetaryCurrency, final PrincipalInterest principalInterestForThisPeriod,
            final Money principalDisbursed, final Money totalInterestChargedForFullLoanTerm, int numberOfRepayments,
            boolean isInstallmentChargeApplicable, final LocalDate lastTransactionDate) {

        Money cumulative = Money.zero(monetaryCurrency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isFeeCharge()) {
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    cumulative = calculateInstallmentCharge(principalInterestForThisPeriod, numberOfRepayments, cumulative, loanCharge);
                } else if (loanCharge.isOverdueInstallmentCharge()
                        && loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
                        && loanCharge.getChargeCalculation().isPercentageBased()) {
                    cumulative = cumulative.plus(loanCharge.chargeAmount());
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
                        && loanCharge.getChargeCalculation().isPercentageBased()
                        && !lastTransactionDate.isAfter(loanCharge.getDueLocalDate())) {
                    cumulative = calculateSpecificDueDateChargeWithPercentage(principalDisbursed, totalInterestChargedForFullLoanTerm,
                            cumulative, loanCharge);
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.amount());
                }
            }
        }

        return cumulative;
    }

    private Money calculateSpecificDueDateChargeWithPercentage(final Money principalDisbursed,
            final Money totalInterestChargedForFullLoanTerm, Money cumulative, final LoanCharge loanCharge) {
        BigDecimal amount = BigDecimal.ZERO;
        if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
            amount = amount.add(principalDisbursed.getAmount()).add(totalInterestChargedForFullLoanTerm.getAmount());
        } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
            amount = amount.add(totalInterestChargedForFullLoanTerm.getAmount());
        } else {
            amount = amount.add(principalDisbursed.getAmount());
        }
        BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
        cumulative = cumulative.plus(loanChargeAmt);
        return cumulative;
    }

    private Money calculateInstallmentCharge(final PrincipalInterest principalInterestForThisPeriod, int numberOfRepayments,
            Money cumulative, final LoanCharge loanCharge) {
        if (loanCharge.getChargeCalculation().isPercentageBased()) {
            BigDecimal amount = BigDecimal.ZERO;
            if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
                amount = amount.add(principalInterestForThisPeriod.principal().getAmount()).add(
                        principalInterestForThisPeriod.interest().getAmount());
            } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
                amount = amount.add(principalInterestForThisPeriod.interest().getAmount());
            } else {
                amount = amount.add(principalInterestForThisPeriod.principal().getAmount());
            }
            BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
            cumulative = cumulative.plus(loanChargeAmt);
        } else {
            cumulative = cumulative.plus(loanCharge.amount().divide(BigDecimal.valueOf(numberOfRepayments)));
        }
        return cumulative;
    }

    private Money cumulativePenaltyChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency monetaryCurrency,
            final PrincipalInterest principalInterestForThisPeriod, final Money principalDisbursed,
            final Money totalInterestChargedForFullLoanTerm, int numberOfRepayments, boolean isInstallmentChargeApplicable,
            final LocalDate lastTransactionDate) {

        Money cumulative = Money.zero(monetaryCurrency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    cumulative = calculateInstallmentCharge(principalInterestForThisPeriod, numberOfRepayments, cumulative, loanCharge);
                } else if (loanCharge.isOverdueInstallmentCharge()
                        && loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
                        && loanCharge.getChargeCalculation().isPercentageBased()) {
                    cumulative = cumulative.plus(loanCharge.chargeAmount());
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
                        && loanCharge.getChargeCalculation().isPercentageBased()
                        && !lastTransactionDate.isAfter(loanCharge.getDueLocalDate())) {
                    cumulative = calculateSpecificDueDateChargeWithPercentage(principalDisbursed, totalInterestChargedForFullLoanTerm,
                            cumulative, loanCharge);
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.amount());
                }
            }
        }

        return cumulative;
    }

    /**
     * Method calls schedule regeneration by passing transactions one after
     * another(this is done mainly to handle the scenario where interest or fee
     * of over due installment should be collected before collecting principal )
     */
    @Override
    public LoanScheduleModel rescheduleNextInstallments(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            final Set<LoanCharge> loanCharges, final HolidayDetailDTO holidayDetailDTO, final List<LoanTransaction> transactions,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor, final LocalDate lastTransactionDate) {

        Collection<RecalculationDetail> recalculationDetails = new ArrayList<>();
        for (LoanTransaction loanTransaction : transactions) {
            recalculationDetails.add(new RecalculationDetail(loanTransaction.getTransactionDate(), loanTransaction));
        }
        return generate(mc, loanApplicationTerms, loanCharges, holidayDetailDTO, recalculationDetails, lastTransactionDate,
                loanRepaymentScheduleTransactionProcessor);
    }

    private void addLoanRepaymentScheduleInstallment(final List<LoanRepaymentScheduleInstallment> installments,
            final LoanScheduleModelPeriod scheduledLoanInstallment) {
        if (scheduledLoanInstallment.isRepaymentPeriod()) {
            final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(null,
                    scheduledLoanInstallment.periodNumber(), scheduledLoanInstallment.periodFromDate(),
                    scheduledLoanInstallment.periodDueDate(), scheduledLoanInstallment.principalDue(),
                    scheduledLoanInstallment.interestDue(), scheduledLoanInstallment.feeChargesDue(),
                    scheduledLoanInstallment.penaltyChargesDue(), scheduledLoanInstallment.isRecalculatedInterestComponent());
            installments.add(installment);
        }
    }

    private LocalDate getNextRestScheduleDate(LocalDate startDate, LoanApplicationTerms loanApplicationTerms,
            final HolidayDetailDTO holidayDetailDTO) {
        LocalDate nextScheduleDate = null;
        if (loanApplicationTerms.getRecalculationFrequencyType().isSameAsRepayment()) {
            nextScheduleDate = this.scheduledDateGenerator.generateNextScheduleDateStartingFromDisburseDate(startDate,
                    loanApplicationTerms, holidayDetailDTO);
        } else {
            CalendarInstance calendarInstance = loanApplicationTerms.getRestCalendarInstance();
            nextScheduleDate = CalendarUtils.getNextScheduleDate(calendarInstance.getCalendar(), startDate);
        }

        return nextScheduleDate;
    }

    private LocalDate getNextCompoundScheduleDate(LocalDate startDate, LoanApplicationTerms loanApplicationTerms,
            final HolidayDetailDTO holidayDetailDTO) {
        LocalDate nextScheduleDate = null;
        if (loanApplicationTerms.getRecalculationFrequencyType().isSameAsRepayment()) {
            nextScheduleDate = this.scheduledDateGenerator.generateNextScheduleDateStartingFromDisburseDate(startDate,
                    loanApplicationTerms, holidayDetailDTO);
        } else {
            CalendarInstance calendarInstance = loanApplicationTerms.getRestCalendarInstance();
            nextScheduleDate = CalendarUtils.getNextScheduleDate(calendarInstance.getCalendar(), startDate);
        }

        return nextScheduleDate;
    }

    /**
     * Method returns the amount payable to close the loan account as of today.
     */
    @Override
    public LoanRepaymentScheduleInstallment calculatePrepaymentAmount(final List<LoanRepaymentScheduleInstallment> installments,
            MonetaryCurrency currency, final LocalDate onDate, LoanApplicationTerms loanApplicationTerms, MathContext mc,
            Set<LoanCharge> charges, final HolidayDetailDTO holidayDetailDTO) {
        Money feeCharges = Money.zero(currency);
        Money penaltyCharges = Money.zero(currency);
        Money totalCompoundingAmount = Money.zero(currency);
        Money totalPrincipal = Money.zero(currency);
        Money totalInterest = Money.zero(currency);
        Money reducePrincipalAsperRest = Money.zero(currency);
        LocalDate reducePrincipalRestDate = onDate;
        LocalDate calculateInterestFrom = onDate;
        LocalDate periodStartDate = onDate;
        LocalDate currentDate = DateUtils.getLocalDateOfTenant();
        Money principalOutstanding = Money.zero(currency);
        Integer periodNumber = 1;
        // for multi disburse loan to identify all disbursed principal for
        // interest calculation
        Map<LocalDate, Money> differenceMap = new TreeMap<>();
        Map<LocalDate, Money> disburseAmtMap = new TreeMap<>();
        if (loanApplicationTerms.isMultiDisburseLoan()) {
            for (DisbursementData disbursementData : loanApplicationTerms.getDisbursementDatas()) {
                if (disbursementData.isDisbursed()) {
                    principalOutstanding = principalOutstanding.plus(disbursementData.amount());
                    disburseAmtMap.put(disbursementData.disbursementDate(), Money.of(currency, disbursementData.amount()));
                }
            }
        } else {
            principalOutstanding = loanApplicationTerms.getPrincipal();
        }

        // adds all the unpaid principal till date and identifies the total
        // outstanding for interest calculation
        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            if (currentInstallment.isNotFullyPaidOff()) {
                if (!currentInstallment.getDueDate().isAfter(onDate)) {
                    totalPrincipal = totalPrincipal.plus(currentInstallment.getPrincipalOutstanding(currency));
                    totalInterest = totalInterest.plus(currentInstallment.getInterestOutstanding(currency));
                    feeCharges = feeCharges.plus(currentInstallment.getFeeChargesOutstanding(currency));
                    penaltyCharges = penaltyCharges.plus(currentInstallment.getPenaltyChargesOutstanding(currency));
                    principalOutstanding = principalOutstanding.minus(currentInstallment.getPrincipal(currency));
                    LocalDate restDate = getNextRestScheduleDate(currentInstallment.getDueDate().minusDays(1), loanApplicationTerms,
                            holidayDetailDTO);
                    if (restDate.isAfter(currentDate)) {
                        reducePrincipalRestDate = restDate;
                        reducePrincipalAsperRest = reducePrincipalAsperRest.plus(currentInstallment.getPrincipalOutstanding(currency));
                    }

                    updateMapWithAmount(differenceMap, currentInstallment.getPrincipalOutstanding(currency),
                            currentInstallment.getDueDate());
                    totalCompoundingAmount = updateMapWithCompoundingDetails(loanApplicationTerms, holidayDetailDTO, currency,
                            differenceMap, totalCompoundingAmount, currentInstallment);
                } else {
                    totalPrincipal = totalPrincipal.minus(currentInstallment.getPrincipalCompleted(currency));
                    totalInterest = totalInterest.minus(currentInstallment.getInterestPaid(currency)).minus(
                            currentInstallment.getInterestWaived(currency));
                    if (!currentInstallment.getFromDate().isAfter(periodStartDate)) {
                        if (!currentInstallment.isRecalculatedInterestComponent()) {
                            periodStartDate = currentInstallment.getFromDate();
                        }
                        calculateInterestFrom = currentInstallment.getFromDate();

                    }

                }
            } else {
                principalOutstanding = principalOutstanding.minus(currentInstallment.getPrincipal(currency));
            }
        }
        Money arrears = totalPrincipal;
        totalPrincipal = totalPrincipal.plus(principalOutstanding);

        for (Map.Entry<LocalDate, Money> disburseDetail : disburseAmtMap.entrySet()) {
            if (disburseDetail.getKey().isAfter(periodStartDate)) {
                differenceMap.put(disburseDetail.getKey(), disburseDetail.getValue());
                principalOutstanding = principalOutstanding.minus(disburseDetail.getValue());
            }
        }

        // interest calculation on outstanding amount till on date from the
        // period start of installment where on date falls
        Money interest = Money.zero(currency);
        LocalDate firstRepaymentdate = this.scheduledDateGenerator.generateNextRepaymentDate(
                loanApplicationTerms.getExpectedDisbursementDate(), loanApplicationTerms, true);
        final LocalDate idealDisbursementDate = this.scheduledDateGenerator.idealDisbursementDateBasedOnFirstRepaymentDate(
                loanApplicationTerms.getLoanTermPeriodFrequencyType(), loanApplicationTerms.getRepaymentEvery(), firstRepaymentdate);
        if (calculateInterestFrom.isBefore(onDate) && !periodStartDate.isBefore(idealDisbursementDate)) {

            boolean isFirstRepayment = false;
            if (periodStartDate.isBefore(firstRepaymentdate)) {
                isFirstRepayment = true;
            }
            LocalDate scheduledDueDate = this.scheduledDateGenerator.generateNextRepaymentDate(periodStartDate, loanApplicationTerms,
                    isFirstRepayment);
            isFirstRepayment = false;

            LocalDate periodStartDateApplicableForInterest = calculateInterestStartDateForPeriod(loanApplicationTerms, periodStartDate,
                    idealDisbursementDate, periodStartDate);
            int daysInPeriodApplicableForInterest = Days.daysBetween(periodStartDateApplicableForInterest, scheduledDueDate).getDays();

            if (calculateInterestFrom.isBefore(periodStartDateApplicableForInterest)) {
                calculateInterestFrom = periodStartDateApplicableForInterest;
            }
            LocalDate calculateTill = onDate;
            if (loanApplicationTerms.getPreClosureInterestCalculationStrategy().calculateTillRestFrequencyEnabled()) {
                LocalDate applicableDate = getNextRestScheduleDate(onDate.minusDays(1), loanApplicationTerms, holidayDetailDTO);
                calculateTill = applicableDate;
            }
            if (arrears.isGreaterThanZero()) {
                updateMapWithAmount(differenceMap, arrears.minus(reducePrincipalAsperRest).plus(totalCompoundingAmount).negated(),
                        currentDate);
                updateMapWithAmount(differenceMap, reducePrincipalAsperRest.negated(), reducePrincipalRestDate);
            }
            if (calculateInterestFrom.isBefore(onDate)) {
                double interestCalculationGraceOnRepaymentPeriodFraction = Double.valueOf(0);
                PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                        this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction, totalInterest.zero(),
                        totalInterest.zero(), totalInterest.zero(), totalInterest.zero(), principalOutstanding, loanApplicationTerms,
                        periodNumber, mc, differenceMap, calculateInterestFrom, calculateTill, daysInPeriodApplicableForInterest);
                interest = interest.plus(principalInterestForThisPeriod.interest());

            }
        }

        totalInterest = totalInterest.plus(interest);
        PrincipalInterest principalInterest = new PrincipalInterest(principalOutstanding, interest, null);

        // apply changes applicable in the period
        for (LoanCharge loanCharge : charges) {
            if (loanCharge.isActive() && loanCharge.isNotFullyPaid() && !loanCharge.isWaived()
                    && loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStartDate, onDate)) {
                if (loanCharge.isFeeCharge()) {
                    feeCharges = feeCharges.plus(loanCharge.amountOutstanding());
                } else {
                    penaltyCharges = penaltyCharges.plus(loanCharge.amountOutstanding());

                }
            } else if (loanCharge.isActive() && interest.isGreaterThanZero() && loanCharge.isInstalmentFee()) {
                if (loanCharge.isFeeCharge()) {
                    feeCharges = feeCharges.plus(calculateInstallmentCharge(principalInterest,
                            loanApplicationTerms.getNumberOfRepayments(), feeCharges.zero(), loanCharge));
                } else {
                    penaltyCharges = penaltyCharges.plus(calculateInstallmentCharge(principalInterest,
                            loanApplicationTerms.getNumberOfRepayments(), feeCharges.zero(), loanCharge));
                }
            }
        }

        return new LoanRepaymentScheduleInstallment(null, 0, onDate, onDate, totalPrincipal.getAmount(), totalInterest.getAmount(),
                feeCharges.getAmount(), penaltyCharges.getAmount(), false);
    }

    /**
     * set the value to zero if the provided value is null
     * 
     * @return integer value equal/greater than 0
     **/
    private Integer defaultToZeroIfNull(Integer value) {

        return (value != null) ? value : 0;
    }
}