/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;

public final class LoanApplicationTerms {

    private final ApplicationCurrency currency;
    private final Integer loanTermFrequency;
    private final PeriodFrequencyType loanTermPeriodFrequencyType;
    private final Integer numberOfRepayments;
    private final Integer repaymentEvery;
    private final PeriodFrequencyType repaymentPeriodFrequencyType;
    private final AmortizationMethod amortizationMethod;

    private final InterestMethod interestMethod;
    private final BigDecimal interestRatePerPeriod;
    private final PeriodFrequencyType interestRatePeriodFrequencyType;
    private final BigDecimal annualNominalInterestRate;
    private final InterestCalculationPeriodMethod interestCalculationPeriodMethod;

    private final Money principal;
    private final LocalDate expectedDisbursementDate;
    private final LocalDate repaymentsStartingFromDate;
    private final LocalDate calculatedRepaymentsStartingFromDate;
    /**
     * Integer representing the number of 'repayment frequencies' or
     * installments where 'grace' should apply to the principal component of a
     * loans repayment period (installment).
     */
    private final Integer principalGrace;

    /**
     * Integer representing the number of 'repayment frequencies' or
     * installments where 'grace' should apply to the payment of interest in a
     * loans repayment period (installment).
     * 
     * <b>Note:</b> Interest is still calculated taking into account the full
     * loan term, the interest is simply offset to a later period.
     */
    private final Integer interestPaymentGrace;

    /**
     * Integer representing the number of 'repayment frequencies' or
     * installments where 'grace' should apply to the charging of interest in a
     * loans repayment period (installment).
     * 
     * <b>Note:</b> The loan is <i>interest-free</i> for the period of time
     * indicated.
     */
    private final Integer interestChargingGrace;

    /**
     * Legacy method of support 'grace' on the charging of interest on a loan.
     * 
     * <p>
     * For the typical structured loan, its reasonable to use an integer to
     * indicate the number of 'repayment frequency' periods the 'grace' should
     * apply to but for slightly <b>irregular</b> loans where the period between
     * disbursement and the date of the 'first repayment period' isnt doest
     * match the 'repayment frequency' but can be less (15days instead of 1
     * month) or more (6 weeks instead of 1 month) - The idea was to use a date
     * to indicate from whence interest should be charged.
     * </p>
     */
    private final LocalDate interestChargedFromDate;
    private final Money inArrearsTolerance;

    public static LoanApplicationTerms assembleFrom(final ApplicationCurrency currency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, final Integer numberOfRepayments, final Integer repaymentEvery,
            final PeriodFrequencyType repaymentPeriodFrequencyType, final AmortizationMethod amortizationMethod,
            final InterestMethod interestMethod, final BigDecimal interestRatePerPeriod,
            final PeriodFrequencyType interestRatePeriodFrequencyType, final BigDecimal annualNominalInterestRate,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final Money principalMoney,
            final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate,
            final LocalDate calculatedRepaymentsStartingFromDate, final Integer graceOnPrincipalPayment,
            final Integer graceOnInterestPayment, final Integer graceOnInterestCharged, final LocalDate interestChargedFromDate,
            final Money inArrearsTolerance) {

        return new LoanApplicationTerms(currency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments, repaymentEvery,
                repaymentPeriodFrequencyType, amortizationMethod, interestMethod, interestRatePerPeriod, interestRatePeriodFrequencyType,
                annualNominalInterestRate, interestCalculationPeriodMethod, principalMoney, expectedDisbursementDate,
                repaymentsStartingFromDate, calculatedRepaymentsStartingFromDate, graceOnPrincipalPayment, graceOnInterestPayment,
                graceOnInterestCharged, interestChargedFromDate, inArrearsTolerance);
    }

    public static LoanApplicationTerms assembleFrom(final ApplicationCurrency applicationCurrency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, final LocalDate expectedDisbursementDate,
            final LocalDate repaymentsStartingFromDate, final LocalDate calculatedRepaymentsStartingFromDate,
            final Money inArrearsTolerance, final LoanProductRelatedDetail loanProductRelatedDetail) {

        final Integer numberOfRepayments = loanProductRelatedDetail.getNumberOfRepayments();
        final Integer repaymentEvery = loanProductRelatedDetail.getRepayEvery();
        final PeriodFrequencyType repaymentPeriodFrequencyType = loanProductRelatedDetail.getRepaymentPeriodFrequencyType();
        final AmortizationMethod amortizationMethod = loanProductRelatedDetail.getAmortizationMethod();
        final InterestMethod interestMethod = loanProductRelatedDetail.getInterestMethod();
        final BigDecimal interestRatePerPeriod = loanProductRelatedDetail.getNominalInterestRatePerPeriod();
        final PeriodFrequencyType interestRatePeriodFrequencyType = loanProductRelatedDetail.getInterestPeriodFrequencyType();
        final BigDecimal annualNominalInterestRate = loanProductRelatedDetail.getAnnualNominalInterestRate();
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = loanProductRelatedDetail
                .getInterestCalculationPeriodMethod();
        final Money principalMoney = loanProductRelatedDetail.getPrincipal();

        //
        final Integer graceOnPrincipalPayment = 0;
        final Integer graceOnInterestPayment = 0;
        final Integer graceOnInterestCharged = 0;
        final LocalDate interestChargedFromDate = null;

        return new LoanApplicationTerms(applicationCurrency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentPeriodFrequencyType, amortizationMethod, interestMethod, interestRatePerPeriod,
                interestRatePeriodFrequencyType, annualNominalInterestRate, interestCalculationPeriodMethod, principalMoney,
                expectedDisbursementDate, repaymentsStartingFromDate, calculatedRepaymentsStartingFromDate, graceOnPrincipalPayment,
                graceOnInterestPayment, graceOnInterestCharged, interestChargedFromDate, inArrearsTolerance);
    }

    private LoanApplicationTerms(final ApplicationCurrency currency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, final Integer numberOfRepayments, final Integer repaymentEvery,
            final PeriodFrequencyType repaymentPeriodFrequencyType, final AmortizationMethod amortizationMethod,
            final InterestMethod interestMethod, final BigDecimal interestRatePerPeriod,
            final PeriodFrequencyType interestRatePeriodFrequencyType, final BigDecimal annualNominalInterestRate,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final Money principal,
            final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate,
            final LocalDate calculatedRepaymentsStartingFromDate, final Integer principalGrace, final Integer interestPaymentGrace,
            final Integer interestChargingGrace, final LocalDate interestChargedFromDate, final Money inArrearsTolerance) {
        this.currency = currency;
        this.loanTermFrequency = loanTermFrequency;
        this.loanTermPeriodFrequencyType = loanTermPeriodFrequencyType;
        this.numberOfRepayments = numberOfRepayments;
        this.repaymentEvery = repaymentEvery;
        this.repaymentPeriodFrequencyType = repaymentPeriodFrequencyType;
        this.amortizationMethod = amortizationMethod;

        this.interestMethod = interestMethod;
        this.interestRatePerPeriod = interestRatePerPeriod;
        this.interestRatePeriodFrequencyType = interestRatePeriodFrequencyType;
        this.annualNominalInterestRate = annualNominalInterestRate;
        this.interestCalculationPeriodMethod = interestCalculationPeriodMethod;

        this.principal = principal;
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.repaymentsStartingFromDate = repaymentsStartingFromDate;
        this.calculatedRepaymentsStartingFromDate = calculatedRepaymentsStartingFromDate;

        this.principalGrace = principalGrace;
        this.interestPaymentGrace = interestPaymentGrace;
        this.interestChargingGrace = interestChargingGrace;
        this.interestChargedFromDate = interestChargedFromDate;

        this.inArrearsTolerance = inArrearsTolerance;
    }

    public Money adjustPrincipalIfLastRepaymentPeriod(final Money principalForPeriod, final Money totalCumulativePrincipalToDate,
            final int periodNumber) {

        Money adjusted = principalForPeriod;

        final Money totalPrincipalRemaining = this.principal.minus(totalCumulativePrincipalToDate);
        if (totalPrincipalRemaining.isLessThanZero()) {
            // paid too much principal, subtract amount that overpays from
            // principal paid for period.
            adjusted = principalForPeriod.minus(totalPrincipalRemaining.abs());
        } else if (isLastRepaymentPeriod(this.numberOfRepayments, periodNumber)) {

            final Money difference = totalCumulativePrincipalToDate.minus(this.principal);
            if (difference.isLessThanZero()) {
                adjusted = principalForPeriod.plus(difference.abs());
            } else if (difference.isGreaterThanZero()) {
                adjusted = principalForPeriod.minus(difference.abs());
            }
        }

        return adjusted;
    }

    public Money adjustInterestIfLastRepaymentPeriod(final Money interestForThisPeriod, final Money totalCumulativeInterestToDate,
            final Money totalInterestDueForLoan, final int periodNumber) {

        Money adjusted = interestForThisPeriod;

        final Money totalInterestRemaining = totalInterestDueForLoan.minus(totalCumulativeInterestToDate);
        if (totalInterestRemaining.isLessThanZero()) {
            // paid too much interest, subtract amount that overpays from
            // interest paid for period.
            adjusted = interestForThisPeriod.minus(totalInterestRemaining.abs());
        } else if (isLastRepaymentPeriod(this.numberOfRepayments, periodNumber)) {
            final Money interestDifference = totalCumulativeInterestToDate.minus(totalInterestDueForLoan);
            if (interestDifference.isLessThanZero()) {
                adjusted = interestForThisPeriod.plus(interestDifference.abs());
            } else if (interestDifference.isGreaterThanZero()) {
                adjusted = interestForThisPeriod.minus(interestDifference.abs());
            }
        }
        if (adjusted.isLessThanZero()) {
            adjusted = adjusted.plus(adjusted);
        }
        return adjusted;
    }

    /**
     * Calculates the total interest to be charged on loan taking into account
     * grace settings.
     * 
     */
    public Money calculateTotalInterestCharged(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc) {

        Money totalInterestCharged = this.principal.zero();

        switch (this.interestMethod) {
            case FLAT:
                final Money totalInterestChargedForLoanTerm = calculateTotalInterestDueWithoutGrace(calculator, mc);

                final Money totalInterestPerInstallment = calculateTotalInterestPerInstallmentWithoutGrace(calculator, mc);

                final Money totalGraceOnInterestCharged = totalInterestPerInstallment.multiplyRetainScale(getInterestChargingGrace(),
                        mc.getRoundingMode());

                totalInterestCharged = totalInterestChargedForLoanTerm.minus(totalGraceOnInterestCharged);
            break;
            case DECLINING_BALANCE:
                final BigDecimal periodicInterestRateForRepaymentPeriod = periodicInterestRate(calculator, mc);

                final Money totalRepaymentDueForLoanTerm = calculateTotalDueForEqualInstallmentLoan(periodicInterestRateForRepaymentPeriod);

                totalInterestCharged = totalRepaymentDueForLoanTerm.minus(this.principal);
            break;
            case INVALID:
            break;
        }

        return totalInterestCharged;
    }

    public Money calculateTotalPrincipalForPeriod(final PaymentPeriodsInOneYearCalculator calculator, final int daysInPeriod,
            final Money outstandingBalance, final int periodNumber, final MathContext mc) {

        Money principalForInstallment = this.principal.zero();

        switch (this.interestMethod) {
            case FLAT:
                principalForInstallment = calculateTotalPrincipalPerPeriodWithoutGrace(mc);
                if (isPrincipalGraceApplicableForThisPeriod(periodNumber)) {
                    principalForInstallment = Money.zero(principalForInstallment.getCurrency());
                }
            break;
            case DECLINING_BALANCE:
                switch (this.amortizationMethod) {
                    case EQUAL_INSTALLMENTS:
                        // equal installments
                        final int periodsElapsed = periodNumber - 1;
                        final BigDecimal periodicInterestRateForRepaymentPeriod = periodicInterestRate(calculator, mc);

                        final Money totalPmtForThisInstallment = calculateTotalDueForEqualInstallmentRepaymentPeriod(
                                periodicInterestRateForRepaymentPeriod, outstandingBalance, periodsElapsed);

                        principalForInstallment = calculatePrincipalDueForInstallment(calculator, mc, periodNumber,
                                totalPmtForThisInstallment, daysInPeriod, outstandingBalance);
                    break;
                    case EQUAL_PRINCIPAL:
                        principalForInstallment = calculateEqualPrincipalDueForInstallment(mc, periodNumber);
                    break;
                    case INVALID:
                    break;
                }
            break;
            case INVALID:
            break;
        }

        return principalForInstallment;
    }

    public PrincipalInterest calculateTotalInterestForPeriod(final PaymentPeriodsInOneYearCalculator calculator,
            final double interestCalculationGraceOnRepaymentPeriodFraction, final int periodNumber, final MathContext mc,
            final Money cumulatingInterestPaymentDueToGrace, final int daysInPeriod, final Money outstandingBalance) {

        Money interestForInstallment = this.principal.zero();
        Money interestBroughtForwardDueToGrace = cumulatingInterestPaymentDueToGrace.copy();

        switch (this.interestMethod) {
            case FLAT:

                switch (this.amortizationMethod) {
                    case EQUAL_INSTALLMENTS:
                        // average out outstanding interest over remaining
                        // instalments where interest is applicable
                        interestForInstallment = calculateTotalFlatInterestForInstallmentAveragingOutGracePeriods(calculator, periodNumber,
                                mc);
                    break;
                    case EQUAL_PRINCIPAL:
                        // interest follows time-value of money and is brought
                        // forward to next applicable interest payment period
                        final PrincipalInterest result = calculateTotalFlatInterestForPeriod(calculator, periodNumber, mc,
                                interestBroughtForwardDueToGrace);
                        interestForInstallment = result.interest();
                        interestBroughtForwardDueToGrace = result.interestPaymentDueToGrace();
                    break;
                    case INVALID:
                    break;
                }
            break;
            case DECLINING_BALANCE:

                final Money interestForThisInstallmentBeforeGrace = calculateDecliningInterestDueForInstallmentBeforeApplyingGrace(
                        calculator, mc, daysInPeriod, outstandingBalance);

                final Money interestForThisInstallmentAfterGrace = calculateDecliningInterestDueForInstallmentAfterApplyingGrace(
                        calculator, interestCalculationGraceOnRepaymentPeriodFraction, mc, daysInPeriod, outstandingBalance, periodNumber);

                interestForInstallment = interestForThisInstallmentAfterGrace;
                if (interestForThisInstallmentAfterGrace.isGreaterThanZero()) {
                    interestForInstallment = interestBroughtForwardDueToGrace.plus(interestForThisInstallmentAfterGrace);
                    interestBroughtForwardDueToGrace = interestBroughtForwardDueToGrace.zero();
                } else if (isInterestFreeGracePeriod(periodNumber)) {
                    interestForInstallment = interestForInstallment.zero();
                } else if (isInterestFreeGracePeriodFromDate(interestCalculationGraceOnRepaymentPeriodFraction)) {
                    interestForInstallment = interestForThisInstallmentAfterGrace;
                } else {
                    interestBroughtForwardDueToGrace = interestBroughtForwardDueToGrace.plus(interestForThisInstallmentBeforeGrace);
                }
            break;
            case INVALID:
            break;
        }

        return new PrincipalInterest(null, interestForInstallment, interestBroughtForwardDueToGrace);
    }

    private final boolean isLastRepaymentPeriod(final int numberOfRepayments, final int periodNumber) {
        return periodNumber == numberOfRepayments;
    }

    private Money calculateTotalDueForEqualInstallmentLoan(final BigDecimal periodicInterestRate) {

        final int periodsElapsed = 0;
        final double paymentPerRepaymentPeriod = paymentPerPeriod(periodicInterestRate, this.principal, periodsElapsed);
        final double totalRepayment = paymentPerRepaymentPeriod * this.numberOfRepayments;

        return Money.of(this.principal.getCurrency(), BigDecimal.valueOf(totalRepayment));
    }

    /**
     * general method to calculate totalInterestDue discounting any grace
     * settings
     */
    private Money calculateTotalInterestDueWithoutGrace(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc) {

        Money totalInterestDue = this.principal.zero();

        switch (this.interestMethod) {
            case FLAT:
                final BigDecimal interestRateForLoanTerm = calculateFlatInterestRateForLoanTerm(calculator, mc);

                totalInterestDue = this.principal.multiplyRetainScale(interestRateForLoanTerm, mc.getRoundingMode());
            break;
            case DECLINING_BALANCE:
            break;
            case INVALID:
            break;
        }

        return totalInterestDue;
    }

    private BigDecimal calculateFlatInterestRateForLoanTerm(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc) {

        final BigDecimal divisor = BigDecimal.valueOf(Double.valueOf("100.0"));

        final long loanTermPeriodsInOneYear = calculatePeriodsInOneYear(calculator);
        final BigDecimal loanTermPeriodsInYearBigDecimal = BigDecimal.valueOf(loanTermPeriodsInOneYear);

        final BigDecimal loanTermFrequencyBigDecimal = BigDecimal.valueOf(this.loanTermFrequency);

        return this.annualNominalInterestRate.divide(loanTermPeriodsInYearBigDecimal, mc).divide(divisor, mc)
                .multiply(loanTermFrequencyBigDecimal);
    }

    private Money calculateTotalInterestPerInstallmentWithoutGrace(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc) {

        final Money totalInterestForLoanTerm = calculateTotalInterestDueWithoutGrace(calculator, mc);

        return totalInterestForLoanTerm.dividedBy(Long.valueOf(this.numberOfRepayments), mc.getRoundingMode());
    }

    private Money calculateTotalPrincipalPerPeriodWithoutGrace(final MathContext mc) {
        final int totalRepaymentsWithCapitalPayment = calculateNumberOfRepaymentsWithPrincipalPayment();
        return this.principal.dividedBy(totalRepaymentsWithCapitalPayment, mc.getRoundingMode());
    }

    private PrincipalInterest calculateTotalFlatInterestForPeriod(final PaymentPeriodsInOneYearCalculator calculator,
            final int periodNumber, final MathContext mc, final Money cumulatingInterestPaymentDueToGrace) {

        Money interestBroughtForwardDueToGrace = cumulatingInterestPaymentDueToGrace.copy();

        Money interestForInstallment = calculateTotalInterestPerInstallmentWithoutGrace(calculator, mc);
        if (isInterestPaymentGraceApplicableForThisPeriod(periodNumber)) {
            interestBroughtForwardDueToGrace = interestBroughtForwardDueToGrace.plus(interestForInstallment);
            interestForInstallment = interestForInstallment.zero();
        } else if (isInterestFreeGracePeriod(periodNumber)) {
            interestForInstallment = interestForInstallment.zero();
        } else if (isFirstPeriodAfterInterestPaymentGracePeriod(periodNumber)) {
            interestForInstallment = cumulatingInterestPaymentDueToGrace.plus(interestForInstallment);
            interestBroughtForwardDueToGrace = interestBroughtForwardDueToGrace.zero();
        }

        return new PrincipalInterest(null, interestForInstallment, interestBroughtForwardDueToGrace);
    }

    /*
     * calculates the interest that should be due for a given scheduled loan
     * repayment period. It takes into account GRACE periods and calculates how
     * much interest is due per period by averaging the number of periods where
     * interest is due and should be paid against the total known interest that
     * is due without grace.
     */
    private Money calculateTotalFlatInterestForInstallmentAveragingOutGracePeriods(final PaymentPeriodsInOneYearCalculator calculator,
            final int periodNumber, final MathContext mc) {

        Money interestForInstallment = calculateTotalInterestPerInstallmentWithoutGrace(calculator, mc);
        if (isInterestPaymentGraceApplicableForThisPeriod(periodNumber)) {
            interestForInstallment = interestForInstallment.zero();
        } else if (isInterestFreeGracePeriod(periodNumber)) {
            interestForInstallment = interestForInstallment.zero();
        } else {

            final Money totalInterestForLoanTerm = calculateTotalInterestDueWithoutGrace(calculator, mc);

            final Money interestPerGracePeriod = calculateTotalInterestPerInstallmentWithoutGrace(calculator, mc);

            final Money totalInterestFree = interestPerGracePeriod.multipliedBy(getInterestChargingGrace());
            final Money realTotalInterestForLoan = totalInterestForLoanTerm.minus(totalInterestFree);

            final Integer interestPaymentDuePeriods = calculateNumberOfRepaymentPeriodsWhereInterestPaymentIsDue(this.numberOfRepayments);

            interestForInstallment = realTotalInterestForLoan
                    .dividedBy(BigDecimal.valueOf(interestPaymentDuePeriods), mc.getRoundingMode());
        }

        return interestForInstallment;
    }

    private BigDecimal periodicInterestRate(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc) {

        final long loanTermPeriodsInOneYear = calculatePeriodsInOneYear(calculator);

        final BigDecimal divisor = BigDecimal.valueOf(Double.valueOf("100.0"));
        final BigDecimal loanTermPeriodsInYearBigDecimal = BigDecimal.valueOf(loanTermPeriodsInOneYear);

        final BigDecimal loanTermFrequencyBigDecimal = BigDecimal.valueOf(this.repaymentEvery);

        return this.annualNominalInterestRate.divide(loanTermPeriodsInYearBigDecimal, mc).divide(divisor, mc)
                .multiply(loanTermFrequencyBigDecimal);
    }

    private long calculatePeriodsInOneYear(final PaymentPeriodsInOneYearCalculator calculator) {
        return calculator.calculate(this.repaymentPeriodFrequencyType).longValue();
    }

    private int calculateNumberOfRepaymentsWithPrincipalPayment() {
        return this.numberOfRepayments - getPrincipalGrace();
    }

    private Integer calculateNumberOfRepaymentPeriodsWhereInterestPaymentIsDue(final Integer totalNumberOfRepaymentPeriods) {
        return totalNumberOfRepaymentPeriods - Math.max(getInterestChargingGrace(), getInterestPaymentGrace());
    }

    private Integer calculateNumberOfPrincipalPaymentPeriods(final Integer totalNumberOfRepaymentPeriods) {
        return totalNumberOfRepaymentPeriods - getPrincipalGrace();
    }

    private boolean isPrincipalGraceApplicableForThisPeriod(final int periodNumber) {
        return periodNumber > 0 && periodNumber <= getPrincipalGrace();
    }

    private boolean isInterestPaymentGraceApplicableForThisPeriod(final int periodNumber) {
        return periodNumber > 0 && periodNumber <= getInterestPaymentGrace();
    }

    private boolean isFirstPeriodAfterInterestPaymentGracePeriod(final int periodNumber) {
        return periodNumber > 0 && periodNumber == getInterestPaymentGrace() + 1;
    }

    private boolean isInterestFreeGracePeriod(final int periodNumber) {
        return periodNumber > 0 && periodNumber <= getInterestChargingGrace();
    }

    private Integer getPrincipalGrace() {
        Integer graceOnPrincipalPayments = Integer.valueOf(0);
        if (this.principalGrace != null) {
            graceOnPrincipalPayments = this.principalGrace;
        }
        return graceOnPrincipalPayments;
    }

    private Integer getInterestPaymentGrace() {
        Integer graceOnInterestPayments = Integer.valueOf(0);
        if (this.interestPaymentGrace != null) {
            graceOnInterestPayments = this.interestPaymentGrace;
        }
        return graceOnInterestPayments;
    }

    private Integer getInterestChargingGrace() {
        Integer graceOnInterestCharged = Integer.valueOf(0);
        if (this.interestChargingGrace != null) {
            graceOnInterestCharged = this.interestChargingGrace;
        }
        return graceOnInterestCharged;
    }

    private double paymentPerPeriod(final BigDecimal periodicInterestRate, final Money balance, final int periodsElapsed) {
        final double futureValue = 0;
        final double principalDouble = balance.getAmount().multiply(BigDecimal.valueOf(-1)).doubleValue();

        final Integer periodsRemaining = this.numberOfRepayments - periodsElapsed;

        return FinanicalFunctions.pmt(periodicInterestRate.doubleValue(), periodsRemaining.doubleValue(), principalDouble, futureValue,
                false);
    }

    private Money calculateDecliningInterestDueForInstallmentBeforeApplyingGrace(final PaymentPeriodsInOneYearCalculator calculator,
            final MathContext mc, final int daysInPeriod, final Money outstandingBalance) {

        Money interestDue = Money.zero(outstandingBalance.getCurrency());

        switch (this.interestCalculationPeriodMethod) {
            case DAILY:
                final BigDecimal dailyInterestRate = this.annualNominalInterestRate.divide(BigDecimal.valueOf(Long.valueOf(365)), mc)
                        .divide(BigDecimal.valueOf(Double.valueOf("100.0")), mc);

                final BigDecimal equivalentInterestRateForPeriod = dailyInterestRate
                        .multiply(BigDecimal.valueOf(Long.valueOf(daysInPeriod)));

                interestDue = outstandingBalance.multiplyRetainScale(equivalentInterestRateForPeriod, mc.getRoundingMode());
            break;
            case SAME_AS_REPAYMENT_PERIOD:
                final BigDecimal periodicInterestRate = periodicInterestRate(calculator, mc);
                interestDue = outstandingBalance.multiplyRetainScale(periodicInterestRate, mc.getRoundingMode());
            break;
            case INVALID:
            break;
        }

        return interestDue;
    }

    private Money calculateDecliningInterestDueForInstallmentAfterApplyingGrace(final PaymentPeriodsInOneYearCalculator calculator,
            final double interestCalculationGraceOnRepaymentPeriodFraction, final MathContext mc, final int daysInPeriod,
            final Money outstandingBalance, final int periodNumber) {

        Money interest = calculateDecliningInterestDueForInstallmentBeforeApplyingGrace(calculator, mc, daysInPeriod, outstandingBalance);

        if (isInterestPaymentGraceApplicableForThisPeriod(periodNumber)) {
            interest = interest.zero();
        }

        Double fraction = interestCalculationGraceOnRepaymentPeriodFraction;

        if (isInterestFreeGracePeriod(periodNumber)) {
            interest = interest.zero();
        } else if (isInterestFreeGracePeriodFromDate(interestCalculationGraceOnRepaymentPeriodFraction)) {

            if (interestCalculationGraceOnRepaymentPeriodFraction >= Integer.valueOf(1).doubleValue()) {
                interest = interest.zero();
                fraction = fraction - Integer.valueOf(1).doubleValue();

            } else if (interestCalculationGraceOnRepaymentPeriodFraction > Double.valueOf("0.25")
                    && interestCalculationGraceOnRepaymentPeriodFraction < Integer.valueOf(1).doubleValue()) {

                final Money graceOnInterestForRepaymentPeriod = interest.multipliedBy(interestCalculationGraceOnRepaymentPeriodFraction);
                interest = interest.minus(graceOnInterestForRepaymentPeriod);
                fraction = Double.valueOf("0");
            }
        }

        return interest;
    }

    private boolean isInterestFreeGracePeriodFromDate(final double interestCalculationGraceOnRepaymentPeriodFraction) {
        return this.interestChargedFromDate != null && interestCalculationGraceOnRepaymentPeriodFraction > Double.valueOf("0.0");
    }

    private Money calculateEqualPrincipalDueForInstallment(final MathContext mc, final int periodNumber) {

        final Integer numberOfPrincipalPaymentPeriods = calculateNumberOfPrincipalPaymentPeriods(this.numberOfRepayments);

        Money principal = this.principal.dividedBy(numberOfPrincipalPaymentPeriods, mc.getRoundingMode());
        if (isPrincipalGraceApplicableForThisPeriod(periodNumber)) {
            principal = principal.zero();
        }
        return principal;
    }

    private Money calculatePrincipalDueForInstallment(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc,
            final int periodNumber, final Money totalDuePerInstallment, final int daysInPeriod, final Money outstandingBalance) {

        final Money interestForThisInstallmentBeforeGrace = calculateDecliningInterestDueForInstallmentBeforeApplyingGrace(calculator, mc,
                daysInPeriod, outstandingBalance);

        Money principal = totalDuePerInstallment.minus(interestForThisInstallmentBeforeGrace);
        if (isPrincipalGraceApplicableForThisPeriod(periodNumber)) {
            principal = principal.zero();
        }
        return principal;
    }

    private Money calculateTotalDueForEqualInstallmentRepaymentPeriod(final BigDecimal periodicInterestRate, final Money balance,
            final int periodsElapsed) {

        final double paymentPerRepaymentPeriod = paymentPerPeriod(periodicInterestRate, balance, periodsElapsed);

        return Money.of(balance.getCurrency(), BigDecimal.valueOf(paymentPerRepaymentPeriod));
    }

    public LoanProductRelatedDetail toLoanProductRelatedDetail() {
        final MonetaryCurrency currency = new MonetaryCurrency(this.currency.getCode(), this.currency.getDecimalPlaces(),
                this.currency.getCurrencyInMultiplesOf());

        return LoanProductRelatedDetail.createFrom(currency, this.principal.getAmount(), this.interestRatePerPeriod,
                this.interestRatePeriodFrequencyType, this.annualNominalInterestRate, this.interestMethod,
                this.interestCalculationPeriodMethod, this.repaymentEvery, this.repaymentPeriodFrequencyType, this.numberOfRepayments,
                this.principalGrace, this.interestPaymentGrace, this.interestChargingGrace, this.amortizationMethod,
                this.inArrearsTolerance.getAmount());
    }

    public Integer getLoanTermFrequency() {
        return this.loanTermFrequency;
    }

    public PeriodFrequencyType getLoanTermPeriodFrequencyType() {
        return this.loanTermPeriodFrequencyType;
    }

    public Integer getRepaymentEvery() {
        return this.repaymentEvery;
    }

    public PeriodFrequencyType getRepaymentPeriodFrequencyType() {
        return this.repaymentPeriodFrequencyType;
    }

    public Date getRepaymentStartFromDate() {
        Date dateValue = null;
        if (this.repaymentsStartingFromDate != null) {
            dateValue = this.repaymentsStartingFromDate.toDate();
        }
        return dateValue;
    }

    public Date getInterestChargedFromDate() {
        Date dateValue = null;
        if (this.interestChargedFromDate != null) {
            dateValue = this.interestChargedFromDate.toDate();
        }
        return dateValue;
    }

    public LocalDate getInterestChargedFromLocalDate() {
        return this.interestChargedFromDate;
    }

    public InterestMethod getInterestMethod() {
        return this.interestMethod;
    }

    public AmortizationMethod getAmortizationMethod() {
        return this.amortizationMethod;
    }

    public MonetaryCurrency getCurrency() {
        return this.principal.getCurrency();
    }

    public Integer getNumberOfRepayments() {
        return this.numberOfRepayments;
    }

    public LocalDate getExpectedDisbursementDate() {
        return this.expectedDisbursementDate;
    }

    public LocalDate getRepaymentsStartingFromLocalDate() {
        return this.repaymentsStartingFromDate;
    }

    public LocalDate getCalculatedRepaymentsStartingFromLocalDate() {
        return this.calculatedRepaymentsStartingFromDate;
    }

    public Money getPrincipal() {
        return this.principal;
    }
}