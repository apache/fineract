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
import org.mifosplatform.organisation.workingdays.domain.RepaymentRescheduleType;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
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
        final LocalDate scheduleTillDate = null;
        return generate(mc, loanApplicationTerms, loanCharges, holidayDetailDTO, diffAmt, loanRepaymentScheduleTransactionProcessor,
                scheduleTillDate);
    }

    private LoanScheduleModel generate(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            final Set<LoanCharge> loanCharges, final HolidayDetailDTO holidayDetailDTO, final Collection<RecalculationDetail> transactions,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor, final LocalDate scheduleTillDate) {

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
        // compounding(principal) amounts will be added along with
        // date(after applying compounding frequency)
        // from when these amounts will effect the outstanding balance for
        // interest calculation
        final Map<LocalDate, Money> latePaymentMap = new HashMap<>();
        final List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>();
        LocalDate currentDate = DateUtils.getLocalDateOfTenant();
        LocalDate lastRestDate = currentDate;
        if (loanApplicationTerms.getRestCalendarInstance() != null) {
            lastRestDate = getNextRestScheduleDate(currentDate.minusDays(1), loanApplicationTerms, holidayDetailDTO);
        }
        // compounding(interest/Fee) amounts will be added along with
        // date(after applying compounding frequency)
        // from when these amounts will effect the outstanding balance for
        // interest calculation
        final TreeMap<LocalDate, Money> compoundingMap = new TreeMap<>();
        final Map<LocalDate, TreeMap<LocalDate, Money>> compoundingDateVariations = new HashMap<>();
        boolean isNextRepaymentAvailable = true;
        Boolean extendTermForDailyRepayments = false;
        
        if(holidayDetailDTO.getWorkingDays().getExtendTermForDailyRepayments() == true &&
                loanApplicationTerms.getRepaymentPeriodFrequencyType() == PeriodFrequencyType.DAYS &&
                loanApplicationTerms.getRepaymentEvery() == 1){
            holidayDetailDTO.getWorkingDays().setRepaymentReschedulingType(RepaymentRescheduleType.MOVE_TO_NEXT_WORKING_DAY.getValue());
            extendTermForDailyRepayments = true;
        }
        while (!outstandingBalance.isZero() || !disburseDetailMap.isEmpty()) {

            actualRepaymentDate = this.scheduledDateGenerator.generateNextRepaymentDate(actualRepaymentDate, loanApplicationTerms,
                    isFirstRepayment);
            isFirstRepayment = false;            
            LocalDate scheduledDueDate = this.scheduledDateGenerator.adjustRepaymentDate(actualRepaymentDate, loanApplicationTerms,
                    holidayDetailDTO);

            if (!latePaymentMap.isEmpty()) {
                populateCompoundingDatesInPeriod(periodStartDate, scheduledDueDate, currentDate, loanApplicationTerms, holidayDetailDTO,
                        compoundingMap, loanCharges, currency);
                compoundingDateVariations.put(periodStartDate, new TreeMap<>(compoundingMap));
            }
            
            if(extendTermForDailyRepayments){
                actualRepaymentDate = scheduledDueDate;
            }

            // calculated interest start date for the period
            periodStartDateApplicableForInterest = calculateInterestStartDateForPeriod(loanApplicationTerms, periodStartDate,
                    idealDisbursementDate, periodStartDateApplicableForInterest);
            int daysInPeriodApplicableForInterest = Days.daysBetween(periodStartDateApplicableForInterest, scheduledDueDate).getDays();

            if (scheduleTillDate != null && !scheduledDueDate.isBefore(scheduleTillDate)) {
                scheduledDueDate = scheduleTillDate;
                isNextRepaymentAvailable = false;
            }

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
                    boolean updateLatePaymentMap = false;
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
                                    if (!compoundingDateVariations.containsKey(periodStartDateApplicableForInterest)) {
                                        compoundingDateVariations.put(periodStartDateApplicableForInterest, new TreeMap<>(compoundingMap));
                                    }
                                    PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                                            this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction,
                                            totalCumulativePrincipal.minus(reducePrincipal), totalCumulativeInterest,
                                            totalInterestChargedForFullLoanTerm, totalOutstandingInterestPaymentDueToGrace,
                                            outstandingBalanceAsPerRest, loanApplicationTerms, periodNumber, mc,
                                            mergeVariationsToMap(principalPortionMap, latePaymentMap, disburseDetailMap, compoundingMap),
                                            compoundingMap, periodStartDateApplicableForInterest, detail.getTransactionDate(),
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
                                        totalCumulativeInterest, numberOfRepayments, true);
                                Money penaltyChargesForInstallment = cumulativePenaltyChargesDueWithin(periodStartDate,
                                        detail.getTransactionDate(), loanCharges, currency, principalInterest, principalDisbursed,
                                        totalCumulativeInterest, numberOfRepayments, true);

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
                                updateLatePaymentMap = true;
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
                            updateLatePaidAmountsToPrincipalMap(principalPortionMap, detail.getTransaction(), latePaymentMap,
                                    compoundingMap, loanApplicationTerms, currency, holidayDetailDTO, lastRestDate);
                            compoundingDateVariations.put(periodStartDateApplicableForInterest, new TreeMap<>(compoundingMap));
                            if (updateLatePaymentMap) {
                                updateLatePaymentsToMap(loanApplicationTerms, holidayDetailDTO, currency, latePaymentMap, scheduledDueDate,
                                        installments, true, lastRestDate, compoundingMap);
                            }
                        } else if (loanRepaymentScheduleTransactionProcessor != null) {
                            LocalDate applicableDate = getNextRestScheduleDate(detail.getTransactionDate().minusDays(1),
                                    loanApplicationTerms, holidayDetailDTO);
                            if (applicableDate.isBefore(scheduledDueDate)) {
                                List<LoanTransaction> currentTransactions = createCurrentTransactionList(detail);
                                Money unprocessed = loanRepaymentScheduleTransactionProcessor.handleRepaymentSchedule(currentTransactions,
                                        currency, installments);
                                Money arrears = fetchCompoundedArrears(loanApplicationTerms, currency, detail.getTransaction());
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
                                        if (!compoundingDateVariations.containsKey(periodStartDateApplicableForInterest)) {
                                            compoundingDateVariations.put(periodStartDateApplicableForInterest, new TreeMap<>(
                                                    compoundingMap));
                                        }
                                        PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                                                this.paymentPeriodsInOneYearCalculator,
                                                interestCalculationGraceOnRepaymentPeriodFraction,
                                                totalCumulativePrincipal.minus(reducePrincipal),
                                                totalCumulativeInterest,
                                                totalInterestChargedForFullLoanTerm,
                                                totalOutstandingInterestPaymentDueToGrace,
                                                outstandingBalanceAsPerRest,
                                                loanApplicationTerms,
                                                periodNumber,
                                                mc,
                                                mergeVariationsToMap(principalPortionMap, latePaymentMap, disburseDetailMap, compoundingMap),
                                                compoundingMap, periodStartDateApplicableForInterest, calculateTill,
                                                daysInPeriodApplicableForInterest);
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
                                LocalDate tillDate = getNextRestScheduleDate(currentDate.minusDays(1), loanApplicationTerms,
                                        holidayDetailDTO);
                                if (arrears.isGreaterThanZero() && applicableDate.isBefore(tillDate)) {
                                    updateLatePaidAmountsToPrincipalMap(principalPortionMap, detail.getTransaction(), latePaymentMap,
                                            compoundingMap, loanApplicationTerms, currency, holidayDetailDTO, lastRestDate);
                                    compoundingDateVariations.put(periodStartDateApplicableForInterest, new TreeMap<>(compoundingMap));
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

            // backup for pre-close transaction
            if (compoundingDateVariations.containsKey(periodStartDateApplicableForInterest)) {
                compoundingMap.clear();
                compoundingMap.putAll(compoundingDateVariations.get(periodStartDateApplicableForInterest));
            } else {
                compoundingDateVariations.put(periodStartDateApplicableForInterest, new TreeMap<>(compoundingMap));
            }
            // 5 determine principal,interest of repayment period
            PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                    this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction,
                    totalCumulativePrincipal.minus(reducePrincipal), totalCumulativeInterest, totalInterestChargedForFullLoanTerm,
                    totalOutstandingInterestPaymentDueToGrace, outstandingBalanceAsPerRest, loanApplicationTerms, periodNumber, mc,
                    mergeVariationsToMap(principalPortionMap, latePaymentMap, disburseDetailMap, compoundingMap), compoundingMap,
                    periodStartDateApplicableForInterest, scheduledDueDate, daysInPeriodApplicableForInterest);

            if (loanApplicationTerms.getFixedEmiAmount() != null
                    && loanApplicationTerms.getFixedEmiAmount().compareTo(principalInterestForThisPeriod.interest().getAmount()) != 1) {
                String errorMsg = "EMI amount must be greter than : " + principalInterestForThisPeriod.interest().getAmount();
                throw new MultiDisbursementEmiAmountException(errorMsg, principalInterestForThisPeriod.interest().getAmount(),
                        loanApplicationTerms.getFixedEmiAmount());
            }

            // update cumulative fields for principal & interest
            Money interestForThisinstallment = principalInterestForThisPeriod.interest();
            Money lastTotalOutstandingInterestPaymentDueToGrace = totalOutstandingInterestPaymentDueToGrace;
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

            if (outstandingBalance.isLessThanZero() || !isNextRepaymentAvailable) {
                principalForThisPeriod = principalForThisPeriod.plus(outstandingBalance);
                outstandingBalance = outstandingBalance.zero();
            }
            
            if(!isNextRepaymentAvailable){
            	disburseDetailMap.clear();
            }

            // applies charges for the period
            PrincipalInterest principalInterest = new PrincipalInterest(principalForThisPeriod, interestForThisinstallment, null);
            Money feeChargesForInstallment = cumulativeFeeChargesDueWithin(periodStartDate, scheduledDueDate, loanCharges, currency,
                    principalInterest, principalDisbursed, totalCumulativeInterest, numberOfRepayments, true);
            Money penaltyChargesForInstallment = cumulativePenaltyChargesDueWithin(periodStartDate, scheduledDueDate, loanCharges,
                    currency, principalInterest, principalDisbursed, totalCumulativeInterest, numberOfRepayments, true);
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
                Money principalProcessed = Money.zero(currency);
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
                                if (compoundingDateVariations.containsKey(periodStartDateApplicableForInterest)) {
                                    compoundingMap.clear();
                                    compoundingMap.putAll(compoundingDateVariations.get(periodStartDateApplicableForInterest));
                                }
                                PrincipalInterest interestTillDate = calculatePrincipalInterestComponentsForPeriod(
                                        this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction,
                                        totalCumulativePrincipal, totalCumulativeInterest, totalInterestChargedForFullLoanTerm,
                                        lastTotalOutstandingInterestPaymentDueToGrace, outstandingBalanceAsPerRest, loanApplicationTerms,
                                        periodNumber, mc,
                                        mergeVariationsToMap(principalPortionMap, latePaymentMap, disburseDetailMap, compoundingMap),
                                        compoundingMap, periodStartDateApplicableForInterest, calculateTill,
                                        daysInPeriodApplicableForInterest);
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
                                    totalOutstandingInterestPaymentDueToGrace = interestTillDate.interestPaymentDueToGrace();
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
                            principalForThisPeriod = principalForThisPeriod.plus(unprocessed.plus(addToPrinciapal));
                            principalProcessed = principalProcessed.plus(unprocessed.plus(addToPrinciapal));
                        }
                    }
                }
                updateLatePaymentsToMap(loanApplicationTerms, holidayDetailDTO, currency, latePaymentMap, scheduledDueDate, installments,
                        true, lastRestDate, compoundingMap);
                principalForThisPeriod = principalForThisPeriod.minus(principalProcessed);
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
            compoundingDateVariations.clear();
        }

        // this condition is to add the interest from grace period if not
        // already applied.
        if (totalOutstandingInterestPaymentDueToGrace.isGreaterThanZero()) {
            LoanScheduleModelPeriod installment = ((List<LoanScheduleModelPeriod>) periods).get(periods.size() - 1);
            installment.addInterestAmount(totalOutstandingInterestPaymentDueToGrace);
            totalRepaymentExpected = totalRepaymentExpected.add(totalOutstandingInterestPaymentDueToGrace.getAmount());
            totalCumulativeInterest = totalCumulativeInterest.plus(totalOutstandingInterestPaymentDueToGrace);
            totalOutstandingInterestPaymentDueToGrace = totalOutstandingInterestPaymentDueToGrace.zero();
        }

        // 7. determine fees and penalties for charges which depends on total
        // loan interest
        for (LoanScheduleModelPeriod loanScheduleModelPeriod : periods) {
            if (loanScheduleModelPeriod.isRepaymentPeriod()) {
                PrincipalInterest principalInterest = new PrincipalInterest(Money.of(currency, loanScheduleModelPeriod.principalDue()),
                        Money.of(currency, loanScheduleModelPeriod.interestDue()), null);
                Money feeChargesForInstallment = cumulativeFeeChargesDueWithin(loanScheduleModelPeriod.periodFromDate(),
                        loanScheduleModelPeriod.periodDueDate(), nonCompoundingCharges, currency, principalInterest, principalDisbursed,
                        totalCumulativeInterest, numberOfRepayments, !loanScheduleModelPeriod.isRecalculatedInterestComponent());
                Money penaltyChargesForInstallment = cumulativePenaltyChargesDueWithin(loanScheduleModelPeriod.periodFromDate(),
                        loanScheduleModelPeriod.periodDueDate(), nonCompoundingCharges, currency, principalInterest, principalDisbursed,
                        totalCumulativeInterest, numberOfRepayments, !loanScheduleModelPeriod.isRecalculatedInterestComponent());
                totalFeeChargesCharged = totalFeeChargesCharged.add(feeChargesForInstallment.getAmount());
                totalPenaltyChargesCharged = totalPenaltyChargesCharged.add(penaltyChargesForInstallment.getAmount());
                totalRepaymentExpected = totalRepaymentExpected.add(feeChargesForInstallment.getAmount()).add(
                        penaltyChargesForInstallment.getAmount());
                loanScheduleModelPeriod.addLoanCharges(feeChargesForInstallment.getAmount(), penaltyChargesForInstallment.getAmount());
            }
        }

        // this block is to add extra re-payment schedules with interest portion
        // if the loan not paid with in loan term

        if (scheduleTillDate != null) {
            currentDate = scheduleTillDate;
        }
        if (isInterestRecalculationRequired(loanApplicationTerms, transactions) && latePaymentMap.size() > 0
                && currentDate.isAfter(periodStartDate)) {
            Money totalInterest = addInterestOnlyRepaymentScheduleForCurrentdate(mc, loanApplicationTerms, holidayDetailDTO, currency,
                    periods, periodStartDate, actualRepaymentDate, instalmentNumber, latePaymentMap, currentDate,
                    loanRepaymentScheduleTransactionProcessor, principalPortionMap, compoundingMap, transactions, installments, loanCharges);
            totalCumulativeInterest = totalCumulativeInterest.plus(totalInterest);
        }

        loanApplicationTerms.resetFixedEmiAmount();

        return LoanScheduleModel.from(periods, applicationCurrency, loanTermInDays, principalDisbursed,
                totalCumulativePrincipal.getAmount(), totalPrincipalPaid, totalCumulativeInterest.getAmount(), totalFeeChargesCharged,
                totalPenaltyChargesCharged, totalRepaymentExpected, totalOutstanding);
    }

    private Money fetchCompoundedArrears(final LoanApplicationTerms loanApplicationTerms, final MonetaryCurrency currency,
            final LoanTransaction transaction) {
        Money arrears = transaction.getPrincipalPortion(currency);
        if (loanApplicationTerms.getInterestRecalculationCompoundingMethod().isInterestCompoundingEnabled()) {
            arrears = arrears.plus(transaction.getInterestPortion(currency));
        }

        if (loanApplicationTerms.getInterestRecalculationCompoundingMethod().isFeeCompoundingEnabled()) {
            arrears = arrears.plus(transaction.getFeeChargesPortion(currency)).plus(transaction.getPenaltyChargesPortion(currency));
        }
        return arrears;
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
     * @param compoundingMap
     *            TODO
     * @param loanCharges
     *            TODO
     * @param principalPortioMap
     *            TODO
     * 
     */
    private Money addInterestOnlyRepaymentScheduleForCurrentdate(final MathContext mc, final LoanApplicationTerms loanApplicationTerms,
            final HolidayDetailDTO holidayDetailDTO, final MonetaryCurrency currency, final Collection<LoanScheduleModelPeriod> periods,
            LocalDate periodStartDate, LocalDate actualRepaymentDate, int instalmentNumber, Map<LocalDate, Money> latePaymentMap,
            final LocalDate currentDate, LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor,
            final Map<LocalDate, Money> principalPortionMap, TreeMap<LocalDate, Money> compoundingMap,
            final Collection<RecalculationDetail> transactions, final List<LoanRepaymentScheduleInstallment> installments,
            Set<LoanCharge> loanCharges) {
        boolean isFirstRepayment = false;
        LocalDate startDate = periodStartDate;
        Money outstanding = Money.zero(currency);
        Money totalInterest = Money.zero(currency);
        Money totalCumulativeInterest = Money.zero(currency);
        Map<LocalDate, Money> disburseDetailsMap = new HashMap<>();
        double interestCalculationGraceOnRepaymentPeriodFraction = Double.valueOf(0);
        int periodNumberTemp = 1;
        LocalDate lastRestDate = getNextRestScheduleDate(currentDate.minusDays(1), loanApplicationTerms, holidayDetailDTO);
        do {

            actualRepaymentDate = this.scheduledDateGenerator.generateNextRepaymentDate(actualRepaymentDate, loanApplicationTerms,
                    isFirstRepayment);
            int daysInPeriod = Days.daysBetween(periodStartDate, actualRepaymentDate).getDays();
            if (actualRepaymentDate.isAfter(currentDate)) {
                actualRepaymentDate = currentDate;
            }
            outstanding = updateOutstandingFromLatePayment(periodStartDate, latePaymentMap, outstanding);

            Collection<RecalculationDetail> applicableTransactions = getApplicableTransactionsForPeriod(loanApplicationTerms,
                    actualRepaymentDate, transactions);

            if (!latePaymentMap.isEmpty()) {
                populateCompoundingDatesInPeriod(periodStartDate, actualRepaymentDate, currentDate, loanApplicationTerms, holidayDetailDTO,
                        compoundingMap, loanCharges, currency);
            }

            for (RecalculationDetail detail : applicableTransactions) {
                if (detail.isProcessed()) {
                    continue;
                }
                List<LoanTransaction> currentTransactions = createCurrentTransactionList(detail);

                if (!periodStartDate.isEqual(detail.getTransactionDate())) {
                    PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                            this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction,
                            totalInterest.zero(), totalInterest.zero(), totalInterest.zero(), totalInterest.zero(), outstanding,
                            loanApplicationTerms, periodNumberTemp, mc,
                            mergeVariationsToMap(principalPortionMap, latePaymentMap, disburseDetailsMap, compoundingMap), compoundingMap,
                            periodStartDate, detail.getTransactionDate(), daysInPeriod);

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
                updateLatePaymentsToMap(loanApplicationTerms, holidayDetailDTO, currency, latePaymentMap, currentDate, installments, false,
                        lastRestDate, compoundingMap);
                outstanding = outstanding.zero();
                outstanding = updateOutstandingFromLatePayment(periodStartDate, latePaymentMap, outstanding);
                outstanding = updateBalanceForInterestCalculation(principalPortionMap, periodStartDate, outstanding, false);
                if (latePaymentMap.isEmpty() && !outstanding.isGreaterThanZero()) {
                    break;
                }
            }

            if (outstanding.isGreaterThanZero()) {
                PrincipalInterest principalInterestForThisPeriod = calculatePrincipalInterestComponentsForPeriod(
                        this.paymentPeriodsInOneYearCalculator, interestCalculationGraceOnRepaymentPeriodFraction, totalInterest.zero(),
                        totalInterest.zero(), totalInterest.zero(), totalInterest.zero(), outstanding, loanApplicationTerms,
                        periodNumberTemp, mc,
                        mergeVariationsToMap(principalPortionMap, latePaymentMap, disburseDetailsMap, compoundingMap), compoundingMap,
                        periodStartDate, actualRepaymentDate, daysInPeriod);
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

    private Money updateOutstandingFromLatePayment(LocalDate periodStartDate, Map<LocalDate, Money> latePaymentMap, Money outstanding) {
        Map<LocalDate, Money> retainEntries = new HashMap<>();
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

    // this is to make sure even paid late payments(principal and compounded
    // interest/fee) should be reduced as per rest date
    private void updateLatePaidAmountsToPrincipalMap(final Map<LocalDate, Money> principalVariationMap,
            final LoanTransaction loanTransaction, final Map<LocalDate, Money> latePaymentsMap, final Map<LocalDate, Money> compoundingMap,
            final LoanApplicationTerms applicationTerms, final MonetaryCurrency currency, final HolidayDetailDTO holidayDetailDTO,
            final LocalDate lastRestDate) {
        LocalDate applicableDate = getNextRestScheduleDate(loanTransaction.getTransactionDate().minusDays(1), applicationTerms,
                holidayDetailDTO);

        Money principalPortion = loanTransaction.getPrincipalPortion(currency);
        Money compoundedLatePayments = Money.zero(currency);
        if (applicationTerms.getInterestRecalculationCompoundingMethod().isInterestCompoundingEnabled()) {
            compoundedLatePayments = compoundedLatePayments.plus(loanTransaction.getInterestPortion(currency));
        }
        if (applicationTerms.getInterestRecalculationCompoundingMethod().isFeeCompoundingEnabled()) {
            compoundedLatePayments = compoundedLatePayments.plus(loanTransaction.getFeeChargesPortion(currency)).plus(
                    loanTransaction.getPenaltyChargesPortion(currency));
        }

        updateCompoundingAmount(principalVariationMap, latePaymentsMap, currency, lastRestDate, principalPortion, applicableDate);
        updateCompoundingAmount(principalVariationMap, compoundingMap, currency, lastRestDate, compoundedLatePayments, applicableDate);
    }

    private void updateCompoundingAmount(final Map<LocalDate, Money> principalVariationMap,
            final Map<LocalDate, Money> latePaymentCompoundingMap, final MonetaryCurrency currency, final LocalDate lastRestDate,
            Money compoundedPortion, final LocalDate applicableDate) {
        Money appliedOnPrincipalVariationMap = Money.zero(currency);
        Map<LocalDate, Money> temp = new HashMap<>();
        for (LocalDate date : latePaymentCompoundingMap.keySet()) {
            if (date.isBefore(lastRestDate)) {
                Money money = latePaymentCompoundingMap.get(date);
                appliedOnPrincipalVariationMap = appliedOnPrincipalVariationMap.plus(money);
                if (appliedOnPrincipalVariationMap.isLessThan(compoundedPortion)) {
                    if (date.isBefore(applicableDate)) {
                        updateMapWithAmount(principalVariationMap, money.negated(), date);
                        updateMapWithAmount(principalVariationMap, money, applicableDate);
                    }
                } else if (temp.isEmpty()) {
                    Money diff = money.minus(appliedOnPrincipalVariationMap.minus(compoundedPortion));
                    updateMapWithAmount(principalVariationMap, diff.negated(), date);
                    updateMapWithAmount(principalVariationMap, diff, applicableDate);
                    updateMapWithAmount(temp, money.minus(diff), date);
                    updateMapWithAmount(temp, money.minus(diff).negated(), lastRestDate);
                } else {
                    updateMapWithAmount(temp, money, date);
                    updateMapWithAmount(temp, money.negated(), lastRestDate);
                }
            }
        }
        latePaymentCompoundingMap.clear();
        latePaymentCompoundingMap.putAll(temp);
    }

    /**
     * this Method updates late/ not paid installment components to Map with
     * effective date as per REST(for principal portion ) and compounding
     * (interest or fee or interest and fee portions) frequency
     * 
     * @param lastRestDate
     *            TODO
     * @param compoundingMap
     *            TODO
     * 
     */
    private void updateLatePaymentsToMap(final LoanApplicationTerms loanApplicationTerms, final HolidayDetailDTO holidayDetailDTO,
            final MonetaryCurrency currency, final Map<LocalDate, Money> latePaymentMap, final LocalDate scheduledDueDate,
            List<LoanRepaymentScheduleInstallment> installments, boolean applyRestFrequencyForPrincipal, final LocalDate lastRestDate,
            final TreeMap<LocalDate, Money> compoundingMap) {
        latePaymentMap.clear();
        LocalDate currentDate = DateUtils.getLocalDateOfTenant();

        Money totalCompoundingAmount = Money.zero(currency);
        Money compoundedMoney = Money.zero(currency);
        if (!compoundingMap.isEmpty()) {
            compoundedMoney = compoundingMap.get(lastRestDate);
        }
        boolean clearCompoundingMap = true;
        for (LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : installments) {
            if (loanRepaymentScheduleInstallment.isNotFullyPaidOff()
                    && !loanRepaymentScheduleInstallment.getDueDate().isAfter(scheduledDueDate)
                    && !loanRepaymentScheduleInstallment.isRecalculatedInterestComponent()) {
                LocalDate principalEffectiveDate = loanRepaymentScheduleInstallment.getDueDate();
                if (applyRestFrequencyForPrincipal) {
                    principalEffectiveDate = getNextRestScheduleDate(loanRepaymentScheduleInstallment.getDueDate().minusDays(1),
                            loanApplicationTerms, holidayDetailDTO);
                }
                if (principalEffectiveDate.isBefore(currentDate)) {
                    updateMapWithAmount(latePaymentMap, loanRepaymentScheduleInstallment.getPrincipalOutstanding(currency),
                            principalEffectiveDate);
                    totalCompoundingAmount = totalCompoundingAmount
                            .plus(loanRepaymentScheduleInstallment.getPrincipalOutstanding(currency));
                }

                final Money changedCompoundedMoney = updateMapWithCompoundingDetails(loanApplicationTerms, holidayDetailDTO, currency,
                        compoundingMap, loanRepaymentScheduleInstallment, lastRestDate, compoundedMoney, scheduledDueDate);
                if (compoundedMoney.isZero() || !compoundedMoney.isEqualTo(changedCompoundedMoney)) {
                    compoundedMoney = changedCompoundedMoney;
                    clearCompoundingMap = false;
                }
            }
        }
        if (totalCompoundingAmount.isGreaterThanZero()) {
            updateMapWithAmount(latePaymentMap, totalCompoundingAmount.negated(), lastRestDate);
        }
        if (clearCompoundingMap) {
            compoundingMap.clear();
        }
    }

    private Money updateMapWithCompoundingDetails(final LoanApplicationTerms loanApplicationTerms, final HolidayDetailDTO holidayDetailDTO,
            final MonetaryCurrency currency, final TreeMap<LocalDate, Money> compoundingMap,
            final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment, final LocalDate lastRestDate,
            final Money compoundedMoney, final LocalDate scheduledDueDate) {
        Money ignoreMoney = compoundedMoney;
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
                if (compoundingEffectiveDate.isBefore(scheduledDueDate)) {
                    ignoreMoney = ignoreMoney.plus(amount);
                    if (ignoreMoney.isGreaterThanZero()) {
                        updateMapWithAmount(compoundingMap, ignoreMoney, compoundingEffectiveDate);
                        updateMapWithAmount(compoundingMap, ignoreMoney.negated(), lastRestDate);
                        ignoreMoney = ignoreMoney.zero();
                    }
                } else {
                    if (ignoreMoney.isLessThanZero()) {
                        LocalDate firstKey = compoundingMap.firstKey();
                        updateMapWithAmount(compoundingMap, ignoreMoney, firstKey);
                        updateMapWithAmount(compoundingMap, ignoreMoney.negated(), lastRestDate);
                        ignoreMoney = ignoreMoney.zero();
                    }
                    updateMapWithAmount(compoundingMap, amount, compoundingEffectiveDate);
                    updateMapWithAmount(compoundingMap, amount.negated(), lastRestDate);
                }
            }
        }
        return ignoreMoney;
    }

    private void populateCompoundingDatesInPeriod(final LocalDate startDate, final LocalDate endDate, final LocalDate currentDate,
            final LoanApplicationTerms loanApplicationTerms, final HolidayDetailDTO holidayDetailDTO,
            final Map<LocalDate, Money> compoundingMap, final Set<LoanCharge> charges, MonetaryCurrency currency) {
        if (loanApplicationTerms.getInterestRecalculationCompoundingMethod().isCompoundingEnabled()) {
            LocalDate lastCompoundingDate = startDate;
            LocalDate compoundingDate = startDate;
            while (compoundingDate.isBefore(endDate) && compoundingDate.isBefore(currentDate)) {
                compoundingDate = getNextCompoundScheduleDate(compoundingDate, loanApplicationTerms, holidayDetailDTO);
                if (!compoundingDate.isBefore(currentDate)) {
                    break;
                } else if (compoundingDate.isAfter(endDate)) {
                    updateMapWithAmount(compoundingMap, Money.zero(currency), compoundingDate);
                } else {
                    Money feeChargesForInstallment = cumulativeFeeChargesDueWithin(lastCompoundingDate, compoundingDate, charges, currency,
                            null, loanApplicationTerms.getPrincipal(), null, loanApplicationTerms.getNumberOfRepayments(), false);
                    Money penaltyChargesForInstallment = cumulativePenaltyChargesDueWithin(lastCompoundingDate, compoundingDate, charges,
                            currency, null, loanApplicationTerms.getPrincipal(), null, loanApplicationTerms.getNumberOfRepayments(), false);
                    updateMapWithAmount(compoundingMap, feeChargesForInstallment.plus(penaltyChargesForInstallment), compoundingDate);
                }
                lastCompoundingDate = compoundingDate;
            }
        }
    }

    protected void clearMapDetails(final LocalDate startDate, final Map<LocalDate, Money> compoundingMap) {
        Map<LocalDate, Money> temp = new HashMap<>();
        for (LocalDate date : compoundingMap.keySet()) {
            if (!date.isBefore(startDate)) {
                temp.put(date, compoundingMap.get(date));
            }
        }
        compoundingMap.clear();
        compoundingMap.putAll(temp);
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
     * merges all the applicable amounts(compounding dates, disbursements, late
     * payment compounding and principal change as per rest) changes to single
     * map for interest calculation
     * 
     * @param compoundingDates
     *            TODO
     */
    private TreeMap<LocalDate, Money> mergeVariationsToMap(final Map<LocalDate, Money> princiaplPaidMap,
            final Map<LocalDate, Money> latePaymentMap, final Map<LocalDate, Money> disburseDetailsMap,
            final Map<LocalDate, Money> compoundingDates) {
        TreeMap<LocalDate, Money> map = new TreeMap<>();
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

        for (Map.Entry<LocalDate, Money> mapEntry : compoundingDates.entrySet()) {
            Money value = mapEntry.getValue();
            if (!map.containsKey(mapEntry.getKey())) {
                map.put(mapEntry.getKey(), value.zero());
            }
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
            final CalendarInstance restCalendarInstance, final CalendarInstance compoundingCalendarInstance) {

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
            final LoanApplicationTerms loanApplicationTerms = loan.getLoanApplicationTerms(applicationCurrency, restCalendarInstance,
                    compoundingCalendarInstance);

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
                            null, null, installmentFromDate, adjustedInstallmentDueDate, daysInInstallment);

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
                            loanApplicationTerms, periodNumber, mathContext, null, null, installmentFromDate, adjustedInstallmentDueDate,
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
            LoanApplicationTerms loanApplicationTerms, int periodNumber, MathContext mc, TreeMap<LocalDate, Money> principalVariation,
            Map<LocalDate, Money> compoundingMap, LocalDate periodStartDate, LocalDate periodEndDate, int daysForInterestInFullPeriod);

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
            boolean isInstallmentChargeApplicable) {

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
                        && loanCharge.getChargeCalculation().isPercentageBased()) {
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
            final Money totalInterestChargedForFullLoanTerm, int numberOfRepayments, boolean isInstallmentChargeApplicable) {

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
                        && loanCharge.getChargeCalculation().isPercentageBased()) {
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
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor) {

        Collection<RecalculationDetail> recalculationDetails = new ArrayList<>();
        for (LoanTransaction loanTransaction : transactions) {
            recalculationDetails.add(new RecalculationDetail(loanTransaction.getTransactionDate(), LoanTransaction
                    .copyTransactionProperties(loanTransaction)));
        }
        final LocalDate scheduleTillDate = null;
        return generate(mc, loanApplicationTerms, loanCharges, holidayDetailDTO, recalculationDetails,
                loanRepaymentScheduleTransactionProcessor, scheduleTillDate);
    }

    private List<LoanRepaymentScheduleInstallment> fetchInstallmentsFromScheduleModel(final LoanScheduleModel loanScheduleModel) {
        final List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>();
        for (LoanScheduleModelPeriod loanScheduleModelPeriod : loanScheduleModel.getPeriods()) {
            addLoanRepaymentScheduleInstallment(installments, loanScheduleModelPeriod);
        }
        return installments;
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
        if (!loanApplicationTerms.getInterestRecalculationCompoundingMethod().isCompoundingEnabled()) { return null; }
        if (loanApplicationTerms.getCompoundingFrequencyType().isSameAsRepayment()) {
            nextScheduleDate = this.scheduledDateGenerator.generateNextScheduleDateStartingFromDisburseDate(startDate,
                    loanApplicationTerms, holidayDetailDTO);
        } else {
            CalendarInstance calendarInstance = loanApplicationTerms.getCompoundingCalendarInstance();
            nextScheduleDate = CalendarUtils.getNextScheduleDate(calendarInstance.getCalendar(), startDate);
        }

        return nextScheduleDate;
    }

    /**
     * Method returns the amount payable to close the loan account as of today.
     */
    @Override
    public LoanRepaymentScheduleInstallment calculatePrepaymentAmount(List<LoanRepaymentScheduleInstallment> installments,
            MonetaryCurrency currency, final LocalDate onDate, LoanApplicationTerms loanApplicationTerms, MathContext mc,
            Set<LoanCharge> charges, final HolidayDetailDTO holidayDetailDTO, final List<LoanTransaction> loanTransactions,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor) {

        LocalDate calculateTill = onDate;
        if (loanApplicationTerms.getPreClosureInterestCalculationStrategy().calculateTillRestFrequencyEnabled()) {
            calculateTill = getNextRestScheduleDate(onDate.minusDays(1), loanApplicationTerms, holidayDetailDTO);
        }
        Collection<RecalculationDetail> recalculationDetails = new ArrayList<>();
        for (LoanTransaction loanTransaction : loanTransactions) {
            recalculationDetails.add(new RecalculationDetail(loanTransaction.getTransactionDate(), LoanTransaction
                    .copyTransactionProperties(loanTransaction)));
        }
        LoanScheduleModel loanScheduleModel = generate(mc, loanApplicationTerms, charges, holidayDetailDTO, recalculationDetails,
                loanRepaymentScheduleTransactionProcessor, calculateTill);
        installments = fetchInstallmentsFromScheduleModel(loanScheduleModel);
        loanRepaymentScheduleTransactionProcessor.handleTransaction(loanApplicationTerms.getExpectedDisbursementDate(), loanTransactions,
                currency, installments, charges);
        Money feeCharges = Money.zero(currency);
        Money penaltyCharges = Money.zero(currency);
        Money totalPrincipal = Money.zero(currency);
        Money totalInterest = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            if (currentInstallment.isNotFullyPaidOff()) {
                totalPrincipal = totalPrincipal.plus(currentInstallment.getPrincipalOutstanding(currency));
                totalInterest = totalInterest.plus(currentInstallment.getInterestOutstanding(currency));
                feeCharges = feeCharges.plus(currentInstallment.getFeeChargesOutstanding(currency));
                penaltyCharges = penaltyCharges.plus(currentInstallment.getPenaltyChargesOutstanding(currency));
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