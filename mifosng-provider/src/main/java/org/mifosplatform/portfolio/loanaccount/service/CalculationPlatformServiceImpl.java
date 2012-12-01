package org.mifosplatform.portfolio.loanaccount.service;

import java.util.Set;

import org.mifosplatform.infrastructure.configuration.domain.ApplicationCurrency;
import org.mifosplatform.infrastructure.configuration.domain.ApplicationCurrencyRepository;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.loanaccount.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.loanschedule.command.CalculateLoanScheduleCommand;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.DefaultLoanScheduleGeneratorFactory;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalculationPlatformServiceImpl implements CalculationPlatformService {

	private final LoanScheduleGeneratorFactory loanScheduleFactory;
	private final ApplicationCurrencyRepository applicationCurrencyRepository;
	private final PlatformSecurityContext context;
	private final LoanProductRelatedDetailAssembler loanProductRelatedDetailAssembler;
	private final LoanChargeAssembler loanChargeAssembler;
	
	@Autowired
	public CalculationPlatformServiceImpl(
			final PlatformSecurityContext context,
			final ApplicationCurrencyRepository applicationCurrencyRepository, 
			final LoanProductRelatedDetailAssembler loanProductRelatedDetailAssembler,
			final LoanChargeAssembler loanChargeAssembler) {
		this.context = context;
		this.applicationCurrencyRepository = applicationCurrencyRepository;
		this.loanScheduleFactory = new DefaultLoanScheduleGeneratorFactory();
		this.loanProductRelatedDetailAssembler = loanProductRelatedDetailAssembler;
		this.loanChargeAssembler = loanChargeAssembler;
	}
	
	@Override
	public LoanScheduleData calculateLoanSchedule(final CalculateLoanScheduleCommand command) {
		
		context.authenticatedUser();
		
		final CalculateLoanScheduleCommandValidator validator = new CalculateLoanScheduleCommandValidator(command);
		validator.validate();
		
		final LoanProductRelatedDetail loanScheduleRelatedDetails = this.loanProductRelatedDetailAssembler.assembleFrom(command);
		
		final Integer loanTermFrequency = command.getLoanTermFrequency();
		final PeriodFrequencyType loanTermFrequencyType = PeriodFrequencyType.fromInt(command.getLoanTermFrequencyType());
		final InterestMethod interestMethod = InterestMethod.fromInt(command.getInterestType());
		
		final LoanScheduleGenerator loanScheduleGenerator = this.loanScheduleFactory.create(interestMethod);

		final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneByCode(loanScheduleRelatedDetails.getCurrency().getCode());
		
		final Set<LoanCharge> loanCharges = this.loanChargeAssembler.assembleFrom(command.getCharges(), loanScheduleRelatedDetails.getPrincipal().getAmount());
		
		return loanScheduleGenerator.generate(applicationCurrency,
				loanScheduleRelatedDetails, loanTermFrequency,
				loanTermFrequencyType, command.getExpectedDisbursementDate(),
				command.getRepaymentsStartingFromDate(),
				command.getInterestChargedFromDate(), loanCharges);
	}
}