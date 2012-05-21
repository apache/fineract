package org.mifosng.platform.loanproduct.service;

import static org.mifosng.platform.Specifications.productThatMatches;

import java.math.BigDecimal;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.command.LoanProductCommand;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.exceptions.PlatformResourceNotFoundException;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.InterestCalculationPeriodMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.LoanProduct;
import org.mifosng.platform.loan.domain.LoanProductRepository;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanProductWritePlatformServiceJpaRepositoryImpl implements LoanProductWritePlatformService {

	private final PlatformSecurityContext context;
	private final LoanProductRepository loanProductRepository;

	@Autowired
	public LoanProductWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final LoanProductRepository loanProductRepository) {
		this.context = context;
		this.loanProductRepository = loanProductRepository;
	}
	
	@Transactional
	@Override
	public EntityIdentifier createLoanProduct(final LoanProductCommand command) {

		AppUser currentUser = this.context.authenticatedUser();
		
		LoanProductCommandValidator validator = new LoanProductCommandValidator();
		validator.validateForCreate(command);

		// assemble LoanProduct from data
		InterestMethod interestMethod = InterestMethod.fromInt(command.getInterestMethod());
		InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(command.getInterestCalculationPeriodMethod());
		
		AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.getAmortizationMethod());

		PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType
				.fromInt(command.getRepaymentFrequency());
		
		PeriodFrequencyType interestFrequencyType = PeriodFrequencyType
				.fromInt(command.getInterestRateFrequencyMethod());

		MonetaryCurrency currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());

		// apr calculator
		BigDecimal annualInterestRate = BigDecimal.ZERO;
		switch (interestFrequencyType) {
		case DAYS:
			break;
		case WEEKS:
			annualInterestRate = command.getInterestRatePerPeriod().multiply(BigDecimal.valueOf(52));
			break;
		case MONTHS:
			annualInterestRate = command.getInterestRatePerPeriod().multiply(BigDecimal.valueOf(12));
			break;
		case YEARS:
			annualInterestRate = command.getInterestRatePerPeriod().multiply(BigDecimal.valueOf(1));
			break;
		case INVALID:
			break;
		}
		
		LoanProduct loanproduct = new LoanProduct(currentUser.getOrganisation(), command.getName(), command.getDescription(), 
				currency, command.getPrincipal(), 
				command.getInterestRatePerPeriod(), interestFrequencyType, annualInterestRate, interestMethod, interestCalculationPeriodMethod,
				command.getRepaymentEvery(), repaymentFrequencyType, command.getNumberOfRepayments(), amortizationMethod, command.getInArrearsToleranceAmount());
		 
		this.loanProductRepository.save(loanproduct);

		return new EntityIdentifier(loanproduct.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier updateLoanProduct(LoanProductCommand command) {
		
		AppUser currentUser = this.context.authenticatedUser();
		
		LoanProductCommandValidator validator = new LoanProductCommandValidator();
		validator.validateForUpdate(command);
		
		LoanProduct product = this.loanProductRepository.findOne(productThatMatches(currentUser.getOrganisation(), command.getId()));
		if (product == null) {
			throw new PlatformResourceNotFoundException("error.msg.loanproduct.id.invalid", "Loan product with identifier {0} does not exist.", command.getId());
		}
		
		product.update(command);
		
		this.loanProductRepository.save(product);
		
		return new EntityIdentifier(Long.valueOf(product.getId()));
	}
}