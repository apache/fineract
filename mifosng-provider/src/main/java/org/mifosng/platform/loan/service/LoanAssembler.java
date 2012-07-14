package org.mifosng.platform.loan.service;

import java.math.BigDecimal;
import java.util.List;

import org.mifosng.platform.api.commands.SubmitLoanApplicationCommand;
import org.mifosng.platform.api.data.LoanSchedule;
import org.mifosng.platform.api.data.MoneyData;
import org.mifosng.platform.api.data.ScheduledLoanInstallment;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.exceptions.ClientNotFoundException;
import org.mifosng.platform.exceptions.FundNotFoundException;
import org.mifosng.platform.exceptions.LoanProductNotFoundException;
import org.mifosng.platform.fund.domain.Fund;
import org.mifosng.platform.fund.domain.FundRepository;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.DefaultLoanLifecycleStateMachine;
import org.mifosng.platform.loan.domain.InterestCalculationPeriodMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanLifecycleStateMachine;
import org.mifosng.platform.loan.domain.LoanProduct;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;
import org.mifosng.platform.loan.domain.LoanProductRepository;
import org.mifosng.platform.loan.domain.LoanRepaymentScheduleInstallment;
import org.mifosng.platform.loan.domain.LoanStatus;
import org.mifosng.platform.loan.domain.LoanStatusRepository;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.loanschedule.domain.AprCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanAssembler {

	private final LoanStatusRepository loanStatusRepository;
	private final LoanProductRepository loanProductRepository;
	private final ClientRepository clientRepository;
	private final AprCalculator aprCalculator = new AprCalculator();
	private final FundRepository fundRepository;
	
	@Autowired
	public LoanAssembler(final LoanStatusRepository loanStatusRepository,
			final LoanProductRepository loanProductRepository,
			final ClientRepository clientRepository, final FundRepository fundRepository) {
		this.loanStatusRepository = loanStatusRepository;
		this.loanProductRepository = loanProductRepository;
		this.clientRepository = clientRepository;
		this.fundRepository = fundRepository;
	}
	
	public Loan assembleFrom(SubmitLoanApplicationCommand command) {
		
		LoanProduct loanProduct = this.loanProductRepository.findOne(command.getProductId());
		if (loanProduct == null) {
			throw new LoanProductNotFoundException(command.getProductId());
		}

		Client client = this.clientRepository.findOne(command.getClientId());
		if (client == null) {
			throw new ClientNotFoundException(command.getClientId());
		}
		
		MonetaryCurrency currency = loanProduct.getCurrency();
		final BigDecimal defaultNominalInterestRatePerPeriod = command.getInterestRatePerPeriodValue();
		final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.fromInt(command.getInterestRateFrequencyType());
		
		BigDecimal defaultAnnualNominalInterestRate = aprCalculator.calculateFrom(interestPeriodFrequencyType, command.getInterestRatePerPeriodValue());
		
		final InterestMethod interestMethod = InterestMethod.fromInt(command.getInterestType());
		final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(command.getInterestCalculationPeriodType());
		
		final Integer repayEvery = command.getRepaymentEveryValue();
		final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.fromInt(command.getRepaymentFrequencyType());
		final Integer defaultNumberOfInstallments = command.getNumberOfRepaymentsValue();
		final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.getAmortizationType());
		
		LoanProductRelatedDetail loanRepaymentScheduleDetail = new LoanProductRelatedDetail(currency,
				command.getPrincipalValue(), defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType, defaultAnnualNominalInterestRate, 
				interestMethod, interestCalculationPeriodMethod,
				repayEvery, repaymentFrequencyType, defaultNumberOfInstallments, amortizationMethod, command.getInArrearsToleranceValue());

		LoanSchedule loanSchedule = command.getLoanSchedule();
		List<ScheduledLoanInstallment> loanRepaymentSchedule = loanSchedule.getScheduledLoanInstallments();
		
		// associating fund with loan product at creation is optional for now.
		Fund fund = findFundByIdIfProvided(command.getFundId());

		Loan loan = Loan.createNew(fund, loanProduct, client, loanRepaymentScheduleDetail);
		loan.setExternalId(command.getExternalId());
		
		for (ScheduledLoanInstallment scheduledLoanInstallment : loanRepaymentSchedule) {

			MoneyData readPrincipalDue = scheduledLoanInstallment.getPrincipalDue();
			MoneyData readInterestDue = scheduledLoanInstallment.getInterestDue();

			LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(
					loan, scheduledLoanInstallment.getInstallmentNumber(),
//					scheduledLoanInstallment.getPeriodStart(),
					scheduledLoanInstallment.getPeriodEnd(), readPrincipalDue.getAmount(),
					readInterestDue.getAmount());
			loan.addRepaymentScheduleInstallment(installment);
		}
		
		loan.submitApplication(command.getSubmittedOnLocalDate(), command.getExpectedDisbursementLocalDate(), command.getRepaymentsStartingFromLocalDate(), command.getInterestChargedFromLocalDate(), defaultLoanLifecycleStateMachine());
		
		return loan;
	}

	private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
		List<LoanStatus> allowedLoanStatuses = this.loanStatusRepository.findAll();
		return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
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
}
