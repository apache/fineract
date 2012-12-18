package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;

/**
 * Domain representation of a Loan Schedule (not used for persistence)
 */
public final class LoanSchedule {

    private final LoanScheduleGenerator loanScheduleGenerator;
    private final ApplicationCurrency applicationCurrency;
    private final BigDecimal principal;
    private final BigDecimal nominalInterestRatePerPeriod;
    private final PeriodFrequencyType interestRatePeriodFrequencyType;
    private final BigDecimal nominalAnnualInterestRate;
    private final InterestMethod interestMethod;
    private final InterestCalculationPeriodMethod interestCalculationPeriodMethod;
    private final Integer repaymentEvery;
    private final PeriodFrequencyType repaymentPeriodFrequencyType;
    private final Integer numberOfRepayments;
    private final AmortizationMethod amortizationMethod;
    private final Integer loanTermFrequency;
    private final PeriodFrequencyType loanTermPeriodFrequencyType;
    private final LocalDate disbursementDate;
    private final LocalDate repaymentStartFromDate;
    private final LocalDate interestChargedFromDate;
    private final BigDecimal inArrearsTolerance;

    private final Set<LoanCharge> loanCharges;

    public LoanSchedule(final LoanScheduleGenerator loanScheduleGenerator, final ApplicationCurrency applicationCurrency,
            final BigDecimal principal, final BigDecimal nominalInterestRatePerPeriod,
            final PeriodFrequencyType interestRatePeriodFrequencyType, final BigDecimal nominalAnnualInterestRate,
            final InterestMethod interestMethod, final InterestCalculationPeriodMethod interestCalculationPeriodMethod,
            final Integer repaymentEvery, final PeriodFrequencyType repaymentPeriodFrequencyType, final Integer numberOfRepayments,
            final AmortizationMethod amortizationMethod, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, final Set<LoanCharge> loanCharges, final LocalDate disbursementDate,
            final LocalDate repaymentStartFromDate, final LocalDate interestChargedFromDate, 
            final BigDecimal inArrearsTolerance) {
        this.loanScheduleGenerator = loanScheduleGenerator;
        this.applicationCurrency = applicationCurrency;
        this.principal = principal;
        this.nominalInterestRatePerPeriod = nominalInterestRatePerPeriod;
        this.interestRatePeriodFrequencyType = interestRatePeriodFrequencyType;
        this.nominalAnnualInterestRate = nominalAnnualInterestRate;
        this.interestMethod = interestMethod;
        this.interestCalculationPeriodMethod = interestCalculationPeriodMethod;
        this.repaymentEvery = repaymentEvery;
        this.repaymentPeriodFrequencyType = repaymentPeriodFrequencyType;
        this.numberOfRepayments = numberOfRepayments;
        this.amortizationMethod = amortizationMethod;
        this.loanTermFrequency = loanTermFrequency;
        this.loanTermPeriodFrequencyType = loanTermPeriodFrequencyType;
        this.loanCharges = loanCharges;
        this.disbursementDate = disbursementDate;
        this.repaymentStartFromDate = repaymentStartFromDate;
        this.interestChargedFromDate = interestChargedFromDate;
        this.inArrearsTolerance = inArrearsTolerance;
    }

    public LoanScheduleData generate() {
        return loanScheduleGenerator.generate(applicationCurrency, loanProductRelatedDetail(), loanTermFrequency,
                loanTermPeriodFrequencyType, disbursementDate, repaymentStartFromDate, interestChargedFromDate, loanCharges);
    }

    public LoanProductRelatedDetail loanProductRelatedDetail() {
        final MonetaryCurrency currency = new MonetaryCurrency(applicationCurrency.getCode(), applicationCurrency.getDecimalPlaces());

        return LoanProductRelatedDetail.createFrom(currency, principal, nominalInterestRatePerPeriod, interestRatePeriodFrequencyType,
                nominalAnnualInterestRate, interestMethod, interestCalculationPeriodMethod, repaymentEvery, repaymentPeriodFrequencyType,
                numberOfRepayments, amortizationMethod, this.inArrearsTolerance);
    }

    public Integer getLoanTermFrequency() {
        return this.loanTermFrequency;
    }

    public PeriodFrequencyType getLoanTermPeriodFrequencyType() {
        return this.loanTermPeriodFrequencyType;
    }
    
    public LocalDate getDisbursementDate() {
        return this.disbursementDate;
    }

    public LocalDate getRepaymentStartFromDate() {
        return this.repaymentStartFromDate;
    }

    public LocalDate getInterestChargedFromDate() {
        return this.interestChargedFromDate;
    }
}