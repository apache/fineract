package org.mifosng.platform.loan.service;

import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.LoanScheduleNewData;
import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.MoneyData;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.currency.domain.ApplicationCurrencyRepository;
import org.mifosng.platform.exceptions.CurrencyNotFoundException;
import org.mifosng.platform.exceptions.LoanNotFoundException;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanCharge;
import org.mifosng.platform.loan.domain.LoanPayoffSummary;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;
import org.mifosng.platform.loan.domain.LoanRepository;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.loanschedule.domain.DefaultLoanScheduleGeneratorFactory;
import org.mifosng.platform.loanschedule.domain.LoanScheduleGenerator;
import org.mifosng.platform.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalculationPlatformServiceImpl implements CalculationPlatformService {

	private final LoanScheduleGeneratorFactory loanScheduleFactory;
	private final LoanRepository loanRepository;
	private final ApplicationCurrencyRepository applicationCurrencyRepository;
	private final PlatformSecurityContext context;
	private final LoanProductRelatedDetailAssembler loanProductRelatedDetailAssembler;
	private final LoanChargeAssembler loanChargeAssembler;
	
	@Autowired
	public CalculationPlatformServiceImpl(
			final PlatformSecurityContext context,
			final LoanRepository loanRepository, 
			final ApplicationCurrencyRepository applicationCurrencyRepository, 
			final LoanProductRelatedDetailAssembler loanProductRelatedDetailAssembler,
			final LoanChargeAssembler loanChargeAssembler) {
		this.context = context;
		this.loanRepository = loanRepository;
		this.applicationCurrencyRepository = applicationCurrencyRepository;
		this.loanScheduleFactory = new DefaultLoanScheduleGeneratorFactory();
		this.loanProductRelatedDetailAssembler = loanProductRelatedDetailAssembler;
		this.loanChargeAssembler = loanChargeAssembler;
	}
	
	@Override
	public LoanScheduleNewData calculateLoanScheduleNew(final CalculateLoanScheduleCommand command) {
		
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

	@Override
	public LoanPayoffReadModel calculatePayoffOn(final Long loanId, final LocalDate payoffDate) {
		
		final Loan loan = this.loanRepository.findOne(loanId);
		if (loan == null) {
			throw new LoanNotFoundException(loanId);
		}

		final LoanPayoffSummary payoffSummary = loan.getPayoffSummaryOn(payoffDate);

		final String currencyCode = payoffSummary.getTotalPaidToDate().getCurrencyCode();
		
		ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneByCode(currencyCode);
		if (applicationCurrency == null) {
			throw new CurrencyNotFoundException(currencyCode);
		}
		final int currencyDigitsAfterDecimal = payoffSummary.getTotalPaidToDate().getCurrencyDigitsAfterDecimal();

		CurrencyData currency = new CurrencyData(currencyCode, applicationCurrency.getName(), currencyDigitsAfterDecimal, applicationCurrency.getDisplaySymbol(), applicationCurrency.getNameCode());
		MoneyData totalPaidToDate = MoneyData.of(currency, payoffSummary.getTotalPaidToDate()
				.getAmount());
				
		MoneyData totalInterestOutstandingBasedOnExpectedMaturityDate = MoneyData.of(currency, payoffSummary.getTotalOutstandingBasedOnExpectedMaturityDate()
				.getAmount());

		MoneyData totalInterestOutstandingBasedOnPayoffDate = MoneyData.of(currency, payoffSummary.getTotalOutstandingBasedOnPayoffDate()
				.getAmount());
				
		MoneyData interestRebateOwed = MoneyData.of(currency, payoffSummary.getRebateOwed().getAmount());
				
		return new LoanPayoffReadModel(payoffSummary.getReference().toString(),
				payoffSummary.getAcutalDisbursementDate(),
				payoffSummary.getExpectedMaturityDate(),
				payoffSummary.getProjectedMaturityDate(),
				payoffSummary.getExpectedLoanTermInDays(),
				payoffSummary.getProjectedLoanTermInDays(), totalPaidToDate,
				totalInterestOutstandingBasedOnExpectedMaturityDate,
				totalInterestOutstandingBasedOnPayoffDate, interestRebateOwed);
	}

}