package org.mifosplatform.portfolio.savingsaccount.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingInterestCalculationMethod;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;
import org.springframework.stereotype.Service;

@Service
public class ReccuringDepositInterestCalculator {
	
	private final Integer monthsInYear = 12;

    // FIXME - MADHUKAR - Are the unused field here needed?
    public Money calculateInterestOnMaturityFor(Money savingsDepositPerPeriod, Integer tenure, BigDecimal reccuringInterestRate,
            LocalDate commencementDate, TenureTypeEnum tenureTypeEnum, SavingFrequencyType savingFrequencyType,
            SavingInterestCalculationMethod savingInterestCalculationMethod, Integer depositEvery) {
    	
    	BigDecimal finalAmount = BigDecimal.ZERO;
    	MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
        

        BigDecimal interestRateAsFraction = reccuringInterestRate.divide(BigDecimal.valueOf(100), mc);
        Integer noofTimesInterestCompoundPerYear = determineIntValue(savingFrequencyType);
    	
        BigDecimal interestRatePerPeriod = BigDecimal.valueOf(interestRateAsFraction.doubleValue()
                / noofTimesInterestCompoundPerYear.doubleValue());
        Integer noOfTimestobeCalculated = tenure/depositEvery;
        
    	if(savingInterestCalculationMethod.isAverageBalance()){
    		
    		BigDecimal termAmount = BigDecimal.ZERO;
    		BigDecimal totalInterest = BigDecimal.ZERO;
    		BigDecimal closingBalance = BigDecimal.ZERO;
    		BigDecimal amountPerPeriod = savingsDepositPerPeriod.getAmount();
    		Integer depositEveryOriginalValue = depositEvery;
    		BigDecimal amountPermonth = BigDecimal.valueOf(amountPerPeriod.doubleValue()/depositEvery.doubleValue());
    		
    		while (noOfTimestobeCalculated > 0) {
				while (depositEvery > 0) {
					closingBalance = closingBalance.add(amountPermonth);
					termAmount = termAmount.add(closingBalance);
					depositEvery--;
				}
				
				BigDecimal averageAmount = BigDecimal.valueOf(termAmount.doubleValue()/depositEveryOriginalValue.doubleValue());
				BigDecimal interestRatePerTerm = BigDecimal.valueOf(interestRateAsFraction.doubleValue()/monthsInYear.doubleValue()*depositEveryOriginalValue.doubleValue());
				BigDecimal interestperTerm = averageAmount.multiply(interestRatePerTerm);
				totalInterest = totalInterest.add(interestperTerm);
				depositEvery = depositEveryOriginalValue;
				termAmount = BigDecimal.ZERO;
				noOfTimestobeCalculated--;
			}
    		finalAmount = closingBalance.add(totalInterest);
    			
    	} else if(savingInterestCalculationMethod.isMonthlyCollection()){
	        /*
	         * // Assuming tenure in months // see
	         * http://www.onemint.com/2012/04/03/
	         * how-to-calculate-interest-on-recurring-deposits/
	         * 
	         * A=P(1+r/n)^nt
	         * 
	         * A = final amount P = principal amount (initial investment) r = annual
	         * nominal interest rate (as a decimal, not in percentage) n = number of
	         * times the interest is compounded per year t = number of years
	         */
	
	        BigDecimal onePlusInterestRatePerPeriod = BigDecimal.ONE.add(interestRatePerPeriod);
	        BigDecimal monthsExpressedInYears;
	        BigDecimal timeforOneCalculationPeriod;
	        BigDecimal amountPerPeriod;
	        Integer tempTenure = tenure;
	        while (noOfTimestobeCalculated > 0) {
	            monthsExpressedInYears = BigDecimal.valueOf(tempTenure.doubleValue() / monthsInYear.doubleValue());
	            timeforOneCalculationPeriod = BigDecimal.valueOf(noofTimesInterestCompoundPerYear * monthsExpressedInYears.doubleValue());
	            amountPerPeriod = savingsDepositPerPeriod.getAmount().multiply(
	                    BigDecimal.valueOf(Math.pow(onePlusInterestRatePerPeriod.doubleValue(), timeforOneCalculationPeriod.doubleValue())));
	            finalAmount = finalAmount.add(amountPerPeriod);
	            noOfTimestobeCalculated--;
	            tempTenure = tempTenure-depositEvery;
	        }
    	} 
     return Money.of(savingsDepositPerPeriod.getCurrency(), finalAmount);
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