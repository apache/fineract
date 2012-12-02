package org.mifosplatform.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.mifosplatform.infrastructure.configuration.domain.MonetaryCurrency;
import org.mifosplatform.infrastructure.staff.domain.Staff;
import org.mifosplatform.infrastructure.staff.domain.StaffRepository;
import org.mifosplatform.infrastructure.staff.exception.StaffNotFoundException;
import org.mifosplatform.infrastructure.staff.exception.StaffRoleException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.fund.domain.FundRepository;
import org.mifosplatform.portfolio.fund.exception.FundNotFoundException;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepository;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.mifosplatform.portfolio.loanaccount.command.LoanApplicationCommand;
import org.mifosplatform.portfolio.loanaccount.domain.DefaultLoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.domain.LoanStatus;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionProcessingStrategyRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanTransactionProcessingStrategyNotFoundException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.service.CalculationPlatformService;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanAssembler {

	private final LoanProductRepository loanProductRepository;
	private final ClientRepository clientRepository;
	private final GroupRepository groupRepository;
	private final AprCalculator aprCalculator = new AprCalculator();
	private final FundRepository fundRepository;
	private final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository;
	private final StaffRepository staffRepository;
	private final CalculationPlatformService calculationPlatformService;
	private final LoanChargeAssembler loanChargeAssembler;
	
	@Autowired
	public LoanAssembler(
			final LoanProductRepository loanProductRepository,
			final ClientRepository clientRepository,
			final GroupRepository groupRepository,
			final FundRepository fundRepository,
			final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository,
	  		final StaffRepository staffRepository,
	  		final CalculationPlatformService calculationPlatformService,
	  		final LoanChargeAssembler loanChargeAssembler) {
		this.loanProductRepository = loanProductRepository;
		this.clientRepository = clientRepository;
		this.groupRepository = groupRepository;
		this.fundRepository = fundRepository;
		this.loanTransactionProcessingStrategyRepository = loanTransactionProcessingStrategyRepository;
		this.staffRepository = staffRepository;
		this.calculationPlatformService = calculationPlatformService;
		this.loanChargeAssembler = loanChargeAssembler;
	}
	
	public Loan assembleFrom(final LoanApplicationCommand command) {
		
		LoanProduct loanProduct = this.loanProductRepository.findOne(command.getProductId());
		if (loanProduct == null) {
			throw new LoanProductNotFoundException(command.getProductId());
		}

		Client client = null;
		if (command.getClientId() != null) {
			client = this.clientRepository.findOne(command.getClientId());
			if (client == null || client.isDeleted()) {
				throw new ClientNotFoundException(command.getClientId());
			}
		} 
		
		Group group = null;
		if (command.getGroupId() != null) {
			group = this.groupRepository.findOne(command.getGroupId());
			if (group == null || group.isDeleted()) {
				throw new GroupNotFoundException(command.getGroupId());
			}
		}
		
		final MonetaryCurrency currency = loanProduct.getCurrency();
		final Integer loanTermFrequency = command.getLoanTermFrequency();
		final PeriodFrequencyType loanTermFrequencyType = PeriodFrequencyType.fromInt(command.getLoanTermFrequencyType());
		
		final LoanProductRelatedDetail loanRepaymentScheduleDetail = assembleLoanProductRelatedDetailFrom(command, currency);

		// associating fund with loan product at creation is optional for now.
		final Fund fund = findFundByIdIfProvided(command.getFundId());
		final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy = findStrategyByIdIfProvided(command.getTransactionProcessingStrategyId());

		//optionally associate a loan officer to the loan
		final Staff loanOfficer= findLoanOfficerByIdIfProvided(command.getLoanOfficerId());
		
		// optionally, see if charges are associated with loan on creation (through loan product or by being directly added)
		final Set<LoanCharge> loanCharges = this.loanChargeAssembler.assembleFrom(command.getCharges(), loanProduct.getCharges(), command.getPrincipal());

		Loan loan = null;
		if (group != null) {
			loan = Loan.createNew(fund,loanOfficer, loanTransactionProcessingStrategy, loanProduct, group, loanRepaymentScheduleDetail, loanCharges);
			loan.setExternalId(command.getExternalId());
		}
		
		if (client != null) {
			loan = Loan.createNew(fund,loanOfficer, loanTransactionProcessingStrategy, loanProduct, client, loanRepaymentScheduleDetail, loanCharges);
			loan.setExternalId(command.getExternalId());
		}
		
		if (loan == null) {
			// FIXME - kw - put in appropriate exception here.
			throw new RuntimeException();
		}

		final LoanScheduleData loanSchedule = this.calculationPlatformService.calculateLoanSchedule(command.toCalculateLoanScheduleCommand());
		
		for (LoanSchedulePeriodData scheduledLoanInstallment : loanSchedule.getPeriods()) {
			if (scheduledLoanInstallment.isRepaymentPeriod()) {
				
				final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(
						loan, 
						scheduledLoanInstallment.periodNumber(),
						scheduledLoanInstallment.periodFromDate(),
						scheduledLoanInstallment.periodDueDate(), 
						scheduledLoanInstallment.principalDue(),
						scheduledLoanInstallment.interestDue(),
						scheduledLoanInstallment.feeChargesDue(),
						scheduledLoanInstallment.penaltyChargesDue());
				
				loan.addRepaymentScheduleInstallment(installment);
			}
		}

		loan.submitApplication(loanTermFrequency, loanTermFrequencyType, 
				command.getSubmittedOnDate(), command.getExpectedDisbursementDate(), 
				command.getRepaymentsStartingFromDate(), command.getInterestChargedFromDate(), 
				defaultLoanLifecycleStateMachine());
		
		return loan;
	}

	public LoanProductRelatedDetail assembleLoanProductRelatedDetailFrom(final LoanApplicationCommand command, MonetaryCurrency currency) {
		final BigDecimal defaultNominalInterestRatePerPeriod = command.getInterestRatePerPeriod();
		final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.fromInt(command.getInterestRateFrequencyType());
		
		BigDecimal defaultAnnualNominalInterestRate = aprCalculator.calculateFrom(interestPeriodFrequencyType, command.getInterestRatePerPeriod());
		
		final InterestMethod interestMethod = InterestMethod.fromInt(command.getInterestType());
		final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(command.getInterestCalculationPeriodType());
		
		
		final Integer defaultNumberOfInstallments = command.getNumberOfRepayments();
		final Integer repayEvery = command.getRepaymentEvery();
		final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.fromInt(command.getRepaymentFrequencyType());
		
		final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.getAmortizationType());
		
		LoanProductRelatedDetail loanRepaymentScheduleDetail = new LoanProductRelatedDetail(currency,
				command.getPrincipal(), defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType, defaultAnnualNominalInterestRate, 
				interestMethod, interestCalculationPeriodMethod,
				repayEvery, repaymentFrequencyType, defaultNumberOfInstallments, amortizationMethod, command.getInArrearsTolerance());
		return loanRepaymentScheduleDetail;
	}

	private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
		List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
		return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
	}
	
	public Fund findFundByIdIfProvided(final Long fundId) {
		Fund fund = null;
		if (fundId != null) {
			fund = this.fundRepository.findOne(fundId);
			if (fund == null) {
				throw new FundNotFoundException(fundId);
			}
		}
		return fund;
	}
	
	public Staff findLoanOfficerByIdIfProvided(final Long loanOfficerId) {
		Staff staff = null;
		if (loanOfficerId != null) {
			staff = this.staffRepository.findOne(loanOfficerId);
			if (staff == null) {
				throw new StaffNotFoundException(loanOfficerId);
			} else if (staff.isNotLoanOfficer()) {
				throw new StaffRoleException(loanOfficerId,
						StaffRoleException.STAFF_ROLE.LOAN_OFFICER);
			}
		}
		return staff;
	}
	
	public LoanTransactionProcessingStrategy findStrategyByIdIfProvided(final Long transactionProcessingStrategyId) {
		LoanTransactionProcessingStrategy strategy = null;
		if (transactionProcessingStrategyId != null) {
			strategy = this.loanTransactionProcessingStrategyRepository.findOne(transactionProcessingStrategyId);
			if (strategy == null) {
				throw new LoanTransactionProcessingStrategyNotFoundException(transactionProcessingStrategyId);
			}
		}
		return strategy;
	}
}