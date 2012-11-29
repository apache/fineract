package org.mifosng.platform.loanproduct.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.mifosng.platform.api.commands.LoanProductCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.exceptions.ChargeIsNotActiveException;
import org.mifosng.platform.exceptions.ChargeNotFoundException;
import org.mifosng.platform.exceptions.FundNotFoundException;
import org.mifosng.platform.exceptions.InvalidCurrencyException;
import org.mifosng.platform.exceptions.LoanProductNotFoundException;
import org.mifosng.platform.exceptions.LoanTransactionProcessingStrategyNotFoundException;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.InterestCalculationPeriodMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.LoanProduct;
import org.mifosng.platform.loan.domain.LoanProductRepository;
import org.mifosng.platform.loan.domain.LoanTransactionProcessingStrategy;
import org.mifosng.platform.loan.domain.LoanTransactionProcessingStrategyRepository;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.loanschedule.domain.AprCalculator;
import org.mifosplatform.infrastructure.configuration.domain.MonetaryCurrency;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepository;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.fund.domain.FundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
public class LoanProductWritePlatformServiceJpaRepositoryImpl implements LoanProductWritePlatformService {

	private final PlatformSecurityContext context;
	private final LoanProductRepository loanProductRepository;
	private final AprCalculator aprCalculator;
	private final FundRepository fundRepository;
	private final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository;
    private final ChargeRepository chargeRepository;

	@Autowired
	public LoanProductWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final LoanProductRepository loanProductRepository,  
			final AprCalculator aprCalculator, final FundRepository fundRepository, final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository,
            final ChargeRepository chargeRepository) {
		this.context = context;
		this.loanProductRepository = loanProductRepository;
		this.aprCalculator = aprCalculator;
		this.fundRepository = fundRepository;
		this.loanTransactionProcessingStrategyRepository = loanTransactionProcessingStrategyRepository;
        this.chargeRepository = chargeRepository;
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
		LoanTransactionProcessingStrategy loanTransactionProcessingStrategy = findStrategyByIdIfProvided(command.getTransactionProcessingStrategyId());

        final Set<Charge> charges = this.assembleSetOfCharges(command, null);

		LoanProduct loanproduct = new LoanProduct(fund, loanTransactionProcessingStrategy, 
				command.getName(), command.getDescription(), 
				currency, command.getPrincipal(), 
				command.getInterestRatePerPeriod(), interestFrequencyType, annualInterestRate, interestMethod, interestCalculationPeriodMethod,
				command.getRepaymentEvery(), repaymentFrequencyType, command.getNumberOfRepayments(), 
				amortizationMethod, command.getInArrearsTolerance(), charges);
		 
		this.loanProductRepository.save(loanproduct);

		return new EntityIdentifier(loanproduct.getId());
	}
	
	private LoanTransactionProcessingStrategy findStrategyByIdIfProvided(final Long transactionProcessingStrategyId) {
		LoanTransactionProcessingStrategy strategy = null;
		if (transactionProcessingStrategyId != null) {
			strategy = this.loanTransactionProcessingStrategyRepository.findOne(transactionProcessingStrategyId);
			if (strategy == null) {
				throw new LoanTransactionProcessingStrategyNotFoundException(transactionProcessingStrategyId);
			}
		}
		return strategy;
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
		Fund fund = null;
		if (command.isFundChanged()) { 
			fund = findFundByIdIfProvided(command.getFundId());
		}
		
		LoanTransactionProcessingStrategy loanTransactionProcessingStrategy = null;
		if (command.isTransactionProcessingStrategyChanged()) {
			loanTransactionProcessingStrategy = findStrategyByIdIfProvided(command.getTransactionProcessingStrategyId());
		}

        final Set<Charge> charges = this.assembleSetOfCharges(command, product.getCurrency().getCode());

		product.update(command, fund, loanTransactionProcessingStrategy, charges);
		
		this.loanProductRepository.save(product);
		
		return new EntityIdentifier(Long.valueOf(product.getId()));
	}

    private Set<Charge> assembleSetOfCharges(final LoanProductCommand command, final String currencyCode) {

        Set<Charge> charges = new HashSet<Charge>();
        String[] chargesArray = command.getCharges();

        String loanProductCurrencyCode = command.getCurrencyCode();
        if (loanProductCurrencyCode == null){
            loanProductCurrencyCode = currencyCode;
        }

        if (command.isChargesChanged() && !ObjectUtils.isEmpty(chargesArray)) {
            for (String chargeId : chargesArray) {
                Long id = Long.valueOf(chargeId);
                Charge charge = this.chargeRepository.findOne(id);
                if (charge == null || charge.isDeleted()) {
                    throw new ChargeNotFoundException(id);
                }
                if (!charge.isActive()){
                    throw new ChargeIsNotActiveException(id, charge.getName());
                }
                if (!loanProductCurrencyCode.equals(charge.getCurrencyCode())){
                    String errorMessage = "Charge and Loan Product must have the same currency.";
                    throw new InvalidCurrencyException("charge", "attach.to.loan.product", errorMessage);
                }
                charges.add(charge);
            }
        }

        return charges;
    }

}