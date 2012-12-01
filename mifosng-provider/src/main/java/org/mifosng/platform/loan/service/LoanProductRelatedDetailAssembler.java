package org.mifosng.platform.loan.service;

import java.math.BigDecimal;

import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;
import org.mifosng.platform.loanschedule.domain.AprCalculator;
import org.mifosplatform.infrastructure.configuration.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanProductRelatedDetailAssembler {
	
	private final LoanProductRepository loanProductRepository;
	private final AprCalculator aprCalculator;

	@Autowired
	public LoanProductRelatedDetailAssembler(
			final LoanProductRepository loanProductRepository,
			final AprCalculator aprCalculator) {
		this.loanProductRepository = loanProductRepository;
		this.aprCalculator = aprCalculator;
	}

	public LoanProductRelatedDetail assembleFrom(final CalculateLoanScheduleCommand command) {
		
		final BigDecimal principalAmount = command.getPrincipal();
		final BigDecimal defaultNominalInterestRatePerPeriod = command.getInterestRatePerPeriod();
		final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.fromInt(command.getInterestRateFrequencyType());
		final InterestMethod interestMethod = InterestMethod.fromInt(command.getInterestType());
		final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(command.getInterestCalculationPeriodType());
		
		final Integer defaultNumberOfInstallments = command.getNumberOfRepayments();
		final Integer repayEvery = command.getRepaymentEvery();
		final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.fromInt(command.getRepaymentFrequencyType());
		
		final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.getAmortizationType());
		
		final BigDecimal defaultAnnualNominalInterestRate = this.aprCalculator.calculateFrom(interestPeriodFrequencyType, defaultNominalInterestRatePerPeriod);
		
		final LoanProduct loanProduct = this.loanProductRepository.findOne(command.getProductId());
		if (loanProduct == null) {
			throw new LoanProductNotFoundException(command.getProductId());
		}
		
		final MonetaryCurrency currency = loanProduct.getCurrency();
		
		// in arrerars tolerance isnt relevant when auto-calculating a loan schedule 
		final BigDecimal inArrearsTolerance = BigDecimal.ZERO;
				
		return new LoanProductRelatedDetail(currency, principalAmount,
				defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType, defaultAnnualNominalInterestRate, 
				interestMethod, interestCalculationPeriodMethod,
				repayEvery, repaymentFrequencyType, defaultNumberOfInstallments, amortizationMethod, 
				inArrearsTolerance);
	}
}