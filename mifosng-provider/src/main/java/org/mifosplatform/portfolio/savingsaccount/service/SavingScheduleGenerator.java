package org.mifosplatform.portfolio.savingsaccount.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingSchedulePeriodData;
import org.mifosplatform.portfolio.savingsaccount.domain.DepositScheduleDateGenerator;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingInterestCalculationMethod;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProduct;

public class SavingScheduleGenerator {

    private final DepositScheduleDateGenerator scheduledDateGenerator = new DefaultDepositScheduleDateGenerator();
    private final Integer monthsInYear = 12;

    public SavingScheduleData generate(final LocalDate scheduleStartDate, final BigDecimal depositAmountPerPeriod,
            final Integer depositFrequency, final SavingFrequencyType savingFrequencyType, final SavingProduct savingProduct,
            final Integer tenure, final ApplicationCurrency applicationCurrency, final BigDecimal interestRate, final SavingInterestCalculationMethod interestCalculationMethod) {

        LocalDate startDate = scheduleStartDate;
        int periodNumber = 1;

        Integer paymentPeriods = tenure / depositFrequency;
        final Collection<SavingSchedulePeriodData> periods = new ArrayList<SavingSchedulePeriodData>();
        final List<LocalDate> scheduledDates = this.scheduledDateGenerator.generate(startDate, paymentPeriods, depositFrequency,
        		savingFrequencyType);
        final MonetaryCurrency currency = savingProduct.getCurrency();
        final Money depositAmount = Money.of(currency, depositAmountPerPeriod);
        Money totalDeposit = Money.zero(currency);
        
                
        MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
        BigDecimal interestRateAsFraction = interestRate.divide(BigDecimal.valueOf(100), mc);
        Integer noofTimesInterestCompoundPerYear = determineIntValue(savingFrequencyType);
        BigDecimal interestRatePerPeriod = BigDecimal.valueOf(interestRateAsFraction.doubleValue()/ noofTimesInterestCompoundPerYear.doubleValue());
        BigDecimal onePlusInterestRatePerPeriod = BigDecimal.ONE.add(interestRatePerPeriod);
        BigDecimal cummulativeDepositPaid = BigDecimal.ZERO;
        BigDecimal cummulativeInterestAccured = BigDecimal.ZERO;
        Integer tempTenure = tenure;
        
        BigDecimal termAmount = BigDecimal.ZERO;
		BigDecimal totalInterest = BigDecimal.ZERO;
		BigDecimal closingBalance = BigDecimal.ZERO;
        
        for (LocalDate scheduleDate : scheduledDates) {
        	
        	if (interestCalculationMethod.isMonthlyCollection()) {
        		BigDecimal monthsExpressedInYears = BigDecimal.valueOf(tempTenure.doubleValue() / monthsInYear.doubleValue());
                BigDecimal timeforOneCalculationPeriod = BigDecimal.valueOf(noofTimesInterestCompoundPerYear * monthsExpressedInYears.doubleValue());
                BigDecimal amountPerPeriod = depositAmountPerPeriod.multiply(BigDecimal.valueOf(Math.pow(onePlusInterestRatePerPeriod.doubleValue(), timeforOneCalculationPeriod.doubleValue())));
                BigDecimal interestAccured = amountPerPeriod.subtract(depositAmountPerPeriod);
            	
                totalDeposit = totalDeposit.plus(depositAmount);
                cummulativeInterestAccured = cummulativeInterestAccured.add(interestAccured);
                SavingSchedulePeriodData installment = SavingSchedulePeriodData.addScheduleInformation(periodNumber, scheduleDate,
                        depositAmount.getAmount(), interestAccured);
                periods.add(installment);
                periodNumber++;
                tempTenure = tempTenure - depositFrequency;
			} else if (interestCalculationMethod.isAverageBalance()){
				
	    		BigDecimal amountPerPeriod = depositAmount.getAmount();
	    		Integer depositEveryOriginalValue = depositFrequency;
	    		BigDecimal amountPermonth = BigDecimal.valueOf(amountPerPeriod.doubleValue()/depositFrequency.doubleValue());
	    		
				while (depositEveryOriginalValue > 0) {
					closingBalance = closingBalance.add(amountPermonth);
					termAmount = termAmount.add(closingBalance);
					depositEveryOriginalValue--;
				}
				
				BigDecimal averageAmount = BigDecimal.valueOf(termAmount.doubleValue()/depositFrequency.doubleValue());
				BigDecimal interestRatePerTerm = BigDecimal.valueOf(interestRateAsFraction.doubleValue()/monthsInYear.doubleValue()*depositFrequency.doubleValue());
				BigDecimal interestAccured = averageAmount.multiply(interestRatePerTerm);
				totalInterest = totalInterest.add(interestAccured);
				depositEveryOriginalValue = depositFrequency;
				termAmount = BigDecimal.ZERO;
	        	
	            totalDeposit = totalDeposit.plus(depositAmount);
	            cummulativeInterestAccured = totalInterest;
	            SavingSchedulePeriodData installment = SavingSchedulePeriodData.addScheduleInformation(periodNumber, scheduleDate,
	                    depositAmount.getAmount(), interestAccured);
	            periods.add(installment);
	            periodNumber++;
			}
        }
        CurrencyData currencyData = new CurrencyData(applicationCurrency.getCode(), applicationCurrency.getName(),
                currency.getDigitsAfterDecimal(), applicationCurrency.getDisplaySymbol(), applicationCurrency.getNameCode());

        return new SavingScheduleData(currencyData, totalDeposit.getAmount(),cummulativeDepositPaid, cummulativeInterestAccured, periods);
    }

    private Integer determineIntValue(SavingFrequencyType frequencyType){
    	Integer noofTimesInterestCompoundPerYear = Integer.valueOf(0);
    	switch (frequencyType) {
        case MONTHLY:
            noofTimesInterestCompoundPerYear = monthsInYear;
        break;
        case QUATERLY:
            noofTimesInterestCompoundPerYear = monthsInYear / 3;
        break;
        case HALFYEARLY:
            noofTimesInterestCompoundPerYear = monthsInYear / 6;
        break;
        case YEARLY:
            noofTimesInterestCompoundPerYear = monthsInYear / 12;
        break;
        default:
            throw new RuntimeException("The specified frequency not supported");

    	}
    	return noofTimesInterestCompoundPerYear;
    }
}
