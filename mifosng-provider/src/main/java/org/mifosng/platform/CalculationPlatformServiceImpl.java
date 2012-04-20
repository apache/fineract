package org.mifosng.platform;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosng.data.CurrencyData;
import org.mifosng.data.LoanPayoffReadModel;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.MoneyData;
import org.mifosng.data.command.CalculateLoanScheduleCommand;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.currency.domain.ApplicationCurrencyRepository;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanPayoffSummary;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;
import org.mifosng.platform.loan.domain.LoanRepository;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.loanschedule.domain.DefaultLoanScheduleGeneratorFactory;
import org.mifosng.platform.loanschedule.domain.LoanScheduleGenerator;
import org.mifosng.platform.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalculationPlatformServiceImpl implements
		CalculationPlatformService {

	private final LoanScheduleGeneratorFactory loanScheduleFactory;
	private final LoanRepository loanRepository;
	private final ApplicationCurrencyRepository applicationCurrencyRepository;

	@Autowired
	public CalculationPlatformServiceImpl(final LoanRepository loanRepository, final ApplicationCurrencyRepository applicationCurrencyRepository) {
		this.loanRepository = loanRepository;
		this.applicationCurrencyRepository = applicationCurrencyRepository;
		this.loanScheduleFactory = new DefaultLoanScheduleGeneratorFactory();
	}

	@Override
	public LoanSchedule calculateLoanSchedule(final CalculateLoanScheduleCommand command) {
		
		CalculateLoanScheduleCommandValidator validator = new CalculateLoanScheduleCommandValidator(command);
		validator.validate();
		
		ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneByCode(command.getCurrencyCode());
		CurrencyData currencyData = new CurrencyData(applicationCurrency.getCode(), applicationCurrency.getName(), command.getDigitsAfterDecimal(),
				applicationCurrency.getDisplaySymbol(), applicationCurrency.getNameCode());
		
		MonetaryCurrency currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());
		
		final BigDecimal principalAmount = BigDecimal.valueOf(command.getPrincipal()
				.doubleValue());
		
		final BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(command
				.getInterestRatePerPeriod().doubleValue());
		final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.fromInt(command.getInterestRateFrequencyMethod());
		
		final InterestMethod interestMethod = InterestMethod.fromInt(command.getInterestMethod());
		
		final Integer repayEvery = command.getRepaymentEvery();
		final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType
				.fromInt(command.getRepaymentFrequency());
		final Integer defaultNumberOfInstallments = command.getNumberOfRepayments();
		final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.getAmortizationMethod());
		
		final boolean flexibleRepaymentSchedule = command.isFlexibleRepaymentSchedule();
		final boolean interestRebateAllowed = command.isInterestRebateAllowed();
		
		// apr calculator
		BigDecimal defaultAnnualNominalInterestRate = BigDecimal.ZERO;
		switch (interestPeriodFrequencyType) {
		case DAYS:
			break;
		case WEEKS:
			defaultAnnualNominalInterestRate = command
					.getInterestRatePerPeriod()
					.multiply(BigDecimal.valueOf(52));
			break;
		case MONTHS:
			defaultAnnualNominalInterestRate = command
					.getInterestRatePerPeriod()
					.multiply(BigDecimal.valueOf(12));
			break;
		case YEARS:
			defaultAnnualNominalInterestRate = command
					.getInterestRatePerPeriod().multiply(BigDecimal.valueOf(1));
			break;
		case INVALID:
			break;
		}
		
		LoanProductRelatedDetail loanScheduleInfo = new LoanProductRelatedDetail(currency, principalAmount,
				defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType, defaultAnnualNominalInterestRate, interestMethod,
				repayEvery, repaymentFrequencyType, defaultNumberOfInstallments, amortizationMethod, BigDecimal.ZERO, flexibleRepaymentSchedule, interestRebateAllowed);
		
		LoanScheduleGenerator loanScheduleGenerator = this.loanScheduleFactory
				.create(interestMethod, amortizationMethod);

		return loanScheduleGenerator.generate(loanScheduleInfo, new LocalDate(
				command.getExpectedDisbursementDate()), command
				.getRepaymentsStartingFromDate(), command.getInterestCalculatedFromDate(), currencyData);
	}

	@Override
	public LoanPayoffReadModel calculatePayoffOn(final Long loanId,
			final LocalDate payoffDate) {
		
		// use jdbc approach to calculating payoff if possible.
		final Loan loan = this.loanRepository.findOne(loanId);

		final LoanPayoffSummary payoffSummary = loan.getPayoffSummaryOn(payoffDate);

		final String currencyCode = payoffSummary.getTotalPaidToDate().getCurrencyCode();
		
		ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneByCode(currencyCode);
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