package org.mifosng.platform.loanproduct.service;

import static org.mifosng.platform.Specifications.productThatMatches;

import java.math.BigDecimal;

import org.mifosng.platform.api.commands.LoanProductCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.exceptions.LoanProductNotFoundException;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.InterestCalculationPeriodMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.LoanProduct;
import org.mifosng.platform.loan.domain.LoanProductRepository;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.loanschedule.domain.AprCalculator;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanProductWritePlatformServiceJpaRepositoryImpl implements LoanProductWritePlatformService {

	private final PlatformSecurityContext context;
	private final LoanProductRepository loanProductRepository;
	private final AprCalculator aprCalculator;

	@Autowired
	public LoanProductWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final LoanProductRepository loanProductRepository,  final AprCalculator aprCalculator) {
		this.context = context;
		this.loanProductRepository = loanProductRepository;
		this.aprCalculator = aprCalculator;
	}
	
	@Transactional
	@Override
	public EntityIdentifier createLoanProduct(final LoanProductCommand command) {

		AppUser currentUser = this.context.authenticatedUser();
		
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

		MonetaryCurrency currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimalValue());
		
		BigDecimal annualInterestRate = this.aprCalculator.calculateFrom(interestFrequencyType, command.getInterestRatePerPeriodValue());
		
		LoanProduct loanproduct = new LoanProduct(currentUser.getOrganisation(), command.getName(), command.getDescription(), 
				currency, command.getPrincipalValue(), 
				command.getInterestRatePerPeriodValue(), interestFrequencyType, annualInterestRate, interestMethod, interestCalculationPeriodMethod,
				command.getRepaymentEveryValue(), repaymentFrequencyType, command.getNumberOfRepaymentsValue(), 
				amortizationMethod, command.getInArrearsToleranceValue());
		 
		this.loanProductRepository.save(loanproduct);

		return new EntityIdentifier(loanproduct.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier updateLoanProduct(LoanProductCommand command) {
		
		AppUser currentUser = this.context.authenticatedUser();
		
		LoanProductCommandValidator validator = new LoanProductCommandValidator(command);
		validator.validateForUpdate();
		
		LoanProduct product = this.loanProductRepository.findOne(productThatMatches(currentUser.getOrganisation(), command.getId()));
		if (product == null) {
			throw new LoanProductNotFoundException(command.getId());
		}
		
		product.update(command);
		
		this.loanProductRepository.save(product);
		
		return new EntityIdentifier(Long.valueOf(product.getId()));
	}
}