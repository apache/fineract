package org.mifosng.platform.loanproduct.service;

import java.math.BigDecimal;

import org.mifosng.platform.api.commands.LoanProductCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.exceptions.FundNotFoundException;
import org.mifosng.platform.exceptions.LoanProductNotFoundException;
import org.mifosng.platform.fund.domain.Fund;
import org.mifosng.platform.fund.domain.FundRepository;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.InterestCalculationPeriodMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.LoanProduct;
import org.mifosng.platform.loan.domain.LoanProductRepository;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.loanschedule.domain.AprCalculator;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanProductWritePlatformServiceJpaRepositoryImpl implements LoanProductWritePlatformService {

	private final PlatformSecurityContext context;
	private final LoanProductRepository loanProductRepository;
	private final AprCalculator aprCalculator;
	private final FundRepository fundRepository;

	@Autowired
	public LoanProductWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final LoanProductRepository loanProductRepository,  
			final AprCalculator aprCalculator, final FundRepository fundRepository) {
		this.context = context;
		this.loanProductRepository = loanProductRepository;
		this.aprCalculator = aprCalculator;
		this.fundRepository = fundRepository;
	}
	
	@Transactional
	@Override
	public EntityIdentifier createLoanProduct(final LoanProductCommand command) {

		this.context.authenticatedUser();
		
		LoanProductCommandValidator validator = new LoanProductCommandValidator(command);
		validator.validateForCreate();

		// assemble LoanProduct from data
		InterestMethod interestMethod = InterestMethod.fromInt(command.getInterestType());
		InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(command.getInterestCalculationPeriodType());
		
		AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.getAmortizationType());

		PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType
				.fromInt(command.getRepaymentFrequencyType());
		
		PeriodFrequencyType interestFrequencyType = PeriodFrequencyType
				.fromInt(command.getInterestRateFrequencyType());

		MonetaryCurrency currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());
		
		BigDecimal annualInterestRate = this.aprCalculator.calculateFrom(interestFrequencyType, command.getInterestRatePerPeriod());
		
		// associating fund with loan product at creation is optional for now.
		Fund fund = findFundByIdIfProvided(command.getFundId());
		
		LoanProduct loanproduct = new LoanProduct(fund, command.getName(), command.getDescription(), 
				currency, command.getPrincipal(), 
				command.getInterestRatePerPeriod(), interestFrequencyType, annualInterestRate, interestMethod, interestCalculationPeriodMethod,
				command.getRepaymentEvery(), repaymentFrequencyType, command.getNumberOfRepayments(), 
				amortizationMethod, command.getInArrearsTolerance());
		 
		this.loanProductRepository.save(loanproduct);

		return new EntityIdentifier(loanproduct.getId());
	}
	
	private Fund findFundByIdIfProvided(final Long fundId) {
		Fund fund = null;
		if (fundId != null) {
			fund = this.fundRepository.findOne(fundId);
			if (fund == null) {
				throw new FundNotFoundException(fundId);
			}
		}
		return fund;
	}

	@Transactional
	@Override
	public EntityIdentifier updateLoanProduct(final LoanProductCommand command) {
		
		this.context.authenticatedUser();
		
		LoanProductCommandValidator validator = new LoanProductCommandValidator(command);
		validator.validateForUpdate();
		
		LoanProduct product = this.loanProductRepository.findOne(command.getId());
		if (product == null) {
			throw new LoanProductNotFoundException(command.getId());
		}
		
		// associating fund with loan product at creation is optional for now.
		Fund fund = findFundByIdIfProvided(command.getFundId());
		
		product.update(command, fund);
		
		this.loanProductRepository.save(product);
		
		return new EntityIdentifier(Long.valueOf(product.getId()));
	}
}