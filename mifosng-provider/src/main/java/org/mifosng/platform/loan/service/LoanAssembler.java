package org.mifosng.platform.loan.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mifosng.platform.api.commands.LoanChargeCommand;
import org.mifosng.platform.api.commands.LoanApplicationCommand;
import org.mifosng.platform.api.data.LoanSchedule;
import org.mifosng.platform.api.data.MoneyData;
import org.mifosng.platform.api.data.ScheduledLoanInstallment;
import org.mifosng.platform.charge.domain.Charge;
import org.mifosng.platform.charge.domain.ChargeRepository;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.exceptions.ChargeIsNotActiveException;
import org.mifosng.platform.exceptions.ChargeNotFoundException;
import org.mifosng.platform.exceptions.ClientNotFoundException;
import org.mifosng.platform.exceptions.FundNotFoundException;
import org.mifosng.platform.exceptions.InvalidCurrencyException;
import org.mifosng.platform.exceptions.LoanProductNotFoundException;
import org.mifosng.platform.exceptions.LoanTransactionProcessingStrategyNotFoundException;
import org.mifosng.platform.exceptions.StaffNotFoundException;
import org.mifosng.platform.exceptions.StaffRoleException;
import org.mifosng.platform.fund.domain.Fund;
import org.mifosng.platform.fund.domain.FundRepository;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.DefaultLoanLifecycleStateMachine;
import org.mifosng.platform.loan.domain.InterestCalculationPeriodMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanCharge;
import org.mifosng.platform.loan.domain.LoanLifecycleStateMachine;
import org.mifosng.platform.loan.domain.LoanProduct;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;
import org.mifosng.platform.loan.domain.LoanProductRepository;
import org.mifosng.platform.loan.domain.LoanRepaymentScheduleInstallment;
import org.mifosng.platform.loan.domain.LoanStatus;
import org.mifosng.platform.loan.domain.LoanTransactionProcessingStrategy;
import org.mifosng.platform.loan.domain.LoanTransactionProcessingStrategyRepository;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.loanschedule.domain.AprCalculator;
import org.mifosng.platform.staff.domain.Staff;
import org.mifosng.platform.staff.domain.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class LoanAssembler {

	private final LoanProductRepository loanProductRepository;
	private final ClientRepository clientRepository;
	private final AprCalculator aprCalculator = new AprCalculator();
	private final FundRepository fundRepository;
    private final ChargeRepository chargeRepository;
	private final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository;
	private final StaffRepository staffRepository;
	
	@Autowired
	public LoanAssembler(
			final LoanProductRepository loanProductRepository,
			final ClientRepository clientRepository,
			final FundRepository fundRepository,
            final ChargeRepository chargeRepository,
			final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository,
	  		final StaffRepository staffRepository) {
		this.loanProductRepository = loanProductRepository;
		this.clientRepository = clientRepository;
		this.fundRepository = fundRepository;
        this.chargeRepository = chargeRepository;
		this.loanTransactionProcessingStrategyRepository = loanTransactionProcessingStrategyRepository;
		this.staffRepository = staffRepository;
	}
	
	public Loan assembleFrom(final LoanApplicationCommand command) {
		
		LoanProduct loanProduct = this.loanProductRepository.findOne(command.getProductId());
		if (loanProduct == null) {
			throw new LoanProductNotFoundException(command.getProductId());
		}

		Client client = this.clientRepository.findOne(command.getClientId());
		if (client == null || client.isDeleted()) {
			throw new ClientNotFoundException(command.getClientId());
		}
		
		MonetaryCurrency currency = loanProduct.getCurrency();
		final Integer loanTermFrequency = command.getLoanTermFrequency();
		final PeriodFrequencyType loanTermFrequencyType = PeriodFrequencyType.fromInt(command.getLoanTermFrequencyType());
		
		LoanProductRelatedDetail loanRepaymentScheduleDetail = assembleLoanProductRelatedDetailFrom(command, currency);

		LoanSchedule loanSchedule = command.getLoanSchedule();
		List<ScheduledLoanInstallment> loanRepaymentSchedule = loanSchedule.getScheduledLoanInstallments();
		
		// associating fund with loan product at creation is optional for now.
		Fund fund = findFundByIdIfProvided(command.getFundId());
		LoanTransactionProcessingStrategy loanTransactionProcessingStrategy = findStrategyByIdIfProvided(command.getTransactionProcessingStrategyId());

		//optionally associate a loan officer to the loan
		Staff loanOfficer= findLoanOfficerByIdIfProvided(command.getLoanOfficerId());

		Loan loan = Loan.createNew(fund,loanOfficer, loanTransactionProcessingStrategy, loanProduct, client, loanRepaymentScheduleDetail);
		loan.setExternalId(command.getExternalId());

        final Set<LoanCharge> charges = this.assembleSetOfCharges(command, loan, loanProduct, currency.getCode());
        loan.setCharges(charges);

		for (ScheduledLoanInstallment scheduledLoanInstallment : loanRepaymentSchedule) {

			MoneyData readPrincipalDue = scheduledLoanInstallment.getPrincipalDue();
			MoneyData readInterestDue = scheduledLoanInstallment.getInterestDue();

			LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(
					loan, scheduledLoanInstallment.getInstallmentNumber(),
					scheduledLoanInstallment.getPeriodEnd(), readPrincipalDue.getAmount(),
					readInterestDue.getAmount());
			loan.addRepaymentScheduleInstallment(installment);
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
	
	private Staff findLoanOfficerByIdIfProvided(final Long loanOfficerId) {
		Staff staff = null;
		if (loanOfficerId != null) {
			staff = this.staffRepository.findOne(loanOfficerId);
			if (staff == null) {
				throw new StaffNotFoundException(loanOfficerId);
			} else if (!staff.getLoanOfficerFlag()) {
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

    private Set<LoanCharge> assembleSetOfCharges(final LoanApplicationCommand command, final Loan loan,
                                                 final LoanProduct product, final String currencyCode) {

        Set<LoanCharge> charges = new HashSet<LoanCharge>();
        LoanChargeCommand[] loanChargesArray = command.getCharges();

        if (!ObjectUtils.isEmpty(loanChargesArray)) {
            for (LoanChargeCommand loanChargeCommand : loanChargesArray) {
                Long id = loanChargeCommand.getId();
                Charge charge = this.chargeRepository.findOne(id);
                if (charge == null || charge.isDeleted()) {
                    throw new ChargeNotFoundException(id);
                }
                if (!charge.isActive()){
                    throw new ChargeIsNotActiveException(id);
                }
                if (!currencyCode.equals(charge.getCurrencyCode())){
                    String errorMessage = "Charge and Loan must have the same currency.";
                    throw new InvalidCurrencyException("charge", "attach.to.loan", errorMessage);
                }
                charges.add(new LoanCharge(loan, charge, loanChargeCommand));
            }
        } else if (loanChargesArray == null) {
           for (Charge productCharge : product.getCharges()){
               charges.add(new LoanCharge(loan, productCharge));
           }
        }

        return charges;
    }
}