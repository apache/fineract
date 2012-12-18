package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;

/**
 * <p>
 * Declining balance can be amortized (see {@link AmortizationMethod}) in two ways
 * at present:
 * <ol>
 * <li>Equal principal payments</li>
 * <li>Equal installment payments</li>
 * </ol>
 * </p>
 * 
 * <p>
 * When amortized using <i>equal principal payments</i>, the <b>principal
 * component</b> of each installment is fixed and <b>interest due</b> is
 * calculated from the <b>outstanding principal balance</b> resulting in a
 * different <b>total payment due</b> for each installment.
 * </p>
 * 
 * <p>
 * When amortized using <i>equal installments</i>, the <b>total payment due</b>
 * for each installment is fixed and is calculated using the excel like
 * <code>pmt</code> function. The <b>interest due</b> is calculated from the
 * <b>outstanding principal balance</b> which results in a <b>principal
 * component</b> that is <b>total payment due</b> minus <b>interest due</b>.
 * </p>
 */
public class DecliningBalanceMethodLoanScheduleGenerator implements LoanScheduleGenerator {

    private final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
    private final PeriodicInterestRateCalculator periodicInterestRateCalculator = new PeriodicInterestRateCalculator();
    private final AmortizationLoanScheduleGeneratorFactory amortizationLoanScheduleGeneratorFactory = new AmortizationLoanScheduleGeneratorFactory();

    @Override
    public LoanScheduleData generate(final ApplicationCurrency applicationCurrency, final LoanProductRelatedDetail loanScheduleInfo,
            final Integer loanTermFrequency, final PeriodFrequencyType loanTermFrequencyType, final LocalDate disbursementDate,
            final LocalDate firstRepaymentDate, final LocalDate interestCalculatedFrom, final Set<LoanCharge> loanCharges) {

        // 1. generate valid set of 'due dates' based on some of the 'loan
        // attributes'
        final List<LocalDate> scheduledDates = this.scheduledDateGenerator.generate(loanScheduleInfo, disbursementDate, firstRepaymentDate);

        final LocalDate idealDisbursementDateBasedOnFirstRepaymentDate = this.scheduledDateGenerator
                .idealDisbursementDateBasedOnFirstRepaymentDate(loanScheduleInfo, scheduledDates);

        // 2. determine the 'periodic' interest rate based on the 'repayment
        // periods' so we can use
        final BigDecimal periodInterestRateForRepaymentPeriod = this.periodicInterestRateCalculator.calculateFrom(loanScheduleInfo);

        // Determine with 'amortisation' approach to use
        final AmortizationLoanScheduleGenerator generator = this.amortizationLoanScheduleGeneratorFactory.createGenerator(loanScheduleInfo
                .getAmortizationMethod());

        return generator.generate(applicationCurrency, loanScheduleInfo, disbursementDate, interestCalculatedFrom,
                periodInterestRateForRepaymentPeriod, idealDisbursementDateBasedOnFirstRepaymentDate, scheduledDates, loanCharges);
    }
}