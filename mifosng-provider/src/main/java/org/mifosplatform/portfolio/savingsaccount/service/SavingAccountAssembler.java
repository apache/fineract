package org.mifosplatform.portfolio.savingsaccount.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsaccount.command.CalculateSavingScheduleCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountApprovalCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountWithdrawalCommand;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingSchedulePeriodData;
import org.mifosplatform.portfolio.savingsaccount.domain.ReccuringDepositInterestCalculator;
import org.mifosplatform.portfolio.savingsaccount.domain.SavingAccount;
import org.mifosplatform.portfolio.savingsaccount.domain.SavingScheduleInstallments;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingInterestCalculationMethod;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProduct;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductRepository;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingsInterestType;
import org.mifosplatform.portfolio.savingsaccountproduct.exception.SavingProductNotFoundException;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccountStatus;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositLifecycleStateMachine;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositLifecycleStateMachineImpl;
import org.mifosplatform.portfolio.savingsdepositaccount.exception.DepositAccountTransactionsException;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SavingAccountAssembler {

    private final ClientRepository clientRepository;
    private final SavingProductRepository savingProductRepository;
    private final ReccuringDepositInterestCalculator reccuringDepositInterestCalculator;
    private final CalculateSavingSchedule calculateSavingSchedule;

    @Autowired
    public SavingAccountAssembler(final ClientRepository clientRepository, final SavingProductRepository savingProductRepository,
            final ReccuringDepositInterestCalculator reccuringDepositInterestCalculator,
            final CalculateSavingSchedule calculateSavingSchedule) {
        this.clientRepository = clientRepository;
        this.savingProductRepository = savingProductRepository;
        this.reccuringDepositInterestCalculator = reccuringDepositInterestCalculator;
        this.calculateSavingSchedule = calculateSavingSchedule;
    }

    // minimumBalanceForWithdrawal,isPartialDepositAllowed
    public SavingAccount assembleFrom(SavingAccountCommand command) {

        Client client = this.clientRepository.findOne(command.getClientId());
        if (client == null || client.isDeleted()) { throw new ClientNotFoundException(command.getClientId()); }

        SavingProduct product = this.savingProductRepository.findOne(command.getProductId());
        if (product == null || product.isDeleted()) { throw new SavingProductNotFoundException(command.getProductId()); }

        MonetaryCurrency currency = product.getCurrency();
        if (command.getCurrencyCode() != null && command.getDigitsAfterDecimal() != null) {
            currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());
        }

        Money savingsDeposit = Money.of(currency, command.getSavingsDepositAmount());

        Integer tenure = product.getSavingProductRelatedDetail().getTenure();
        if (command.getTenure() != null) {
            tenure = command.getTenure();
        }

        TenureTypeEnum tenureTypeEnum = TenureTypeEnum.fromInt(product.getSavingProductRelatedDetail().getTenureType());
        if (command.getTenureType() != null) {
            tenureTypeEnum = TenureTypeEnum.fromInt(command.getTenureType());
        }

        SavingProductType savingProductType = SavingProductType.fromInt(product.getSavingProductRelatedDetail().getSavingProductType());

        SavingFrequencyType savingFrequencyType = SavingFrequencyType.fromInt(product.getSavingProductRelatedDetail().getFrequency());
        if (command.getFrequency() != null) {
            savingFrequencyType = SavingFrequencyType.fromInt(command.getFrequency());
        }

        SavingInterestCalculationMethod savingInterestCalculationMethod = SavingInterestCalculationMethod.fromInt(product.getSavingProductRelatedDetail()
                .getInterestCalculationMethod());
        if (command.getInterestCalculationMethod() != null) {
            savingInterestCalculationMethod = SavingInterestCalculationMethod.fromInt(command.getInterestCalculationMethod());
        }

        SavingsInterestType interestType = SavingsInterestType.fromInt(product.getSavingProductRelatedDetail().getInterestType());
        if (command.getInterestType() != null) {
            interestType = SavingsInterestType.fromInt(command.getInterestType());
        }

        BigDecimal reccuringInterestRate = command.getRecurringInterestRate();
        BigDecimal savingInterestRate = product.getInterestRate();
        if (command.getSavingInterestRate() != null) savingInterestRate = command.getSavingInterestRate();

        boolean isLockinPeriodAllowed = product.getSavingProductRelatedDetail().isLockinPeriodAllowed();
        if (command.isLockinPeriodChanged()) {
            isLockinPeriodAllowed = command.isLockinPeriodAllowed();
        }

        Integer lockinPeriod = product.getSavingProductRelatedDetail().getLockinPeriod();
        if (command.getLockinPeriod() != null) {
            lockinPeriod = command.getLockinPeriod();
        }

        PeriodFrequencyType lockinPeriodType = PeriodFrequencyType.fromInt(product.getSavingProductRelatedDetail().getLockinPeriodType());
        if (command.getLockinPeriodType() != null) {
            lockinPeriodType = PeriodFrequencyType.fromInt(command.getLockinPeriodType());
        }
        
        Integer depositEvery = command.getDepositEvery();
        
        Integer interestPostEvery = command.getInterestPostEvery();

        Integer interestPostFrequency =command.getInterestPostFrequency();

        SavingAccount account = SavingAccount.openNew(client, product, command.getExternalId(), savingsDeposit, reccuringInterestRate,
                savingInterestRate, tenure, command.getCommencementDate(), tenureTypeEnum, savingProductType, savingFrequencyType,
                interestType, savingInterestCalculationMethod, isLockinPeriodAllowed, lockinPeriod, lockinPeriodType,
                this.reccuringDepositInterestCalculator, defaultDepositLifecycleStateMachine(),depositEvery,interestPostEvery, interestPostFrequency);

        SavingScheduleData savingScheduleData = this.calculateSavingSchedule.calculateSavingSchedule(command
                .toCalculateSavingScheduleCommand());
        for (SavingSchedulePeriodData savingSchedulePeriodData : savingScheduleData.getPeriods()) {

            final SavingScheduleInstallments installment = new SavingScheduleInstallments(account, savingSchedulePeriodData.getDueDate()
                    .toDate(), savingSchedulePeriodData.getPeriod(), savingSchedulePeriodData.getDepositDue(), savingSchedulePeriodData.getInterestAccured());
            account.addSavingScheduleInstallment(installment);
        }

        return account;
    }

    public void assembleFrom(SavingAccountCommand command, SavingAccount account) {

        SavingProduct product = account.getProduct();
        if (command.isProductIdChanged()) {
            if (product.getId() != command.getProductId()) {
                product = this.savingProductRepository.findOne(command.getProductId());
                if (product == null || product.isDeleted()) throw new SavingProductNotFoundException(command.getProductId());
            }
        }

        String externalId = account.getExternalId();
        if (command.isExternalIdChanged()) {
            externalId = command.getExternalId();
        }

        Money savingsDeposit = Money.of(product.getCurrency(), account.getSavingsDepositAmountPerPeriod());
        if (command.isDepositAmountChanged()) {
            savingsDeposit = Money.of(product.getCurrency(), command.getSavingsDepositAmount());
        }
        Integer tenure = account.getTenure();
        if (command.isTenureInMonthsChanged()) {
            tenure = command.getTenure();
        }

        LocalDate commencementDate = new LocalDate(account.getProjectedCommencementDate());
        if (command.isCommencementDateChanged()) {
            commencementDate = command.getCommencementDate();
        }

        TenureTypeEnum tenureTypeEnum = TenureTypeEnum.fromInt(account.getTenureType());
        if (command.isTenureTypeEnumChanged()) {
            tenureTypeEnum = TenureTypeEnum.fromInt(command.getTenureType());
        }

        SavingProductType savingProductType = SavingProductType.fromInt(account.getSavingProductType());

        SavingFrequencyType savingFrequencyType = SavingFrequencyType.fromInt(account.getFrequency());
        if (command.isSavingFrequencyTypeChanged()) {
            savingFrequencyType = SavingFrequencyType.fromInt(command.getFrequency());
        }

        SavingInterestCalculationMethod savingInterestCalculationMethod = SavingInterestCalculationMethod.fromInt(account.getInterestCalculationMethod());
        if (command.isSavingInterestCalculationMethodChanged()) {
            savingInterestCalculationMethod = SavingInterestCalculationMethod.fromInt(command.getInterestCalculationMethod());
        }

        BigDecimal reccuringInterestRate = account.getReccuringInterestRate();
        if (command.isReccuringInterestRateChanged()) {
            reccuringInterestRate = command.getRecurringInterestRate();
        }

        BigDecimal savingInterestRate = account.getSavingInterestRate();
        if (command.isSavingInterestRateChanged()) {
            savingInterestRate = command.getSavingInterestRate();
        }

        boolean isLockinPeriodAllowed = account.isLockinPeriodAllowed();
        if (command.isLockinPeriodAllowedChanged()) {
            isLockinPeriodAllowed = command.isLockinPeriodAllowed();
        }

        Integer lockinPeriod = account.getLockinPeriod();
        if (command.isLockinPeriodChanged()) {
            lockinPeriod = command.getLockinPeriod();
        }

        PeriodFrequencyType lockinPeriodType = PeriodFrequencyType.fromInt(account.getLockinPeriodType());
        if (command.isLockinPeriodTypeChanged()) {
            lockinPeriodType = PeriodFrequencyType.fromInt(command.getLockinPeriodType());
        }
        
        Integer payEvery = account.getPayEvery();
        if(command.isDepositEveryChanged()){
        	payEvery = command.getDepositEvery();
        }
        
        SavingsInterestType interestType = SavingsInterestType.fromInt(product.getSavingProductRelatedDetail().getInterestType());
        if (command.getInterestType() != null) {
            interestType = SavingsInterestType.fromInt(command.getInterestType());
        }
        
        Integer interestPostEvery = command.getInterestPostEvery();

        Integer interestPostFrequency =command.getInterestPostFrequency();
        
        account.modifyAccount(product, externalId, savingsDeposit, reccuringInterestRate, savingInterestRate, tenure, commencementDate,
                tenureTypeEnum, savingProductType, savingFrequencyType, savingInterestCalculationMethod, isLockinPeriodAllowed,
                lockinPeriod, lockinPeriodType, this.reccuringDepositInterestCalculator,payEvery, interestType, interestPostEvery, interestPostFrequency);
        
        SavingScheduleData savingScheduleData = this.calculateSavingSchedule.calculateSavingSchedule(command
                .toCalculateSavingScheduleCommand());
        for (SavingSchedulePeriodData savingSchedulePeriodData : savingScheduleData.getPeriods()) {

            final SavingScheduleInstallments installment = new SavingScheduleInstallments(account, savingSchedulePeriodData.getDueDate()
                    .toDate(), savingSchedulePeriodData.getPeriod(), savingSchedulePeriodData.getDepositDue(), savingSchedulePeriodData.getInterestAccured());
            account.addSavingScheduleInstallment(installment);
        }

    }

    private DepositLifecycleStateMachine defaultDepositLifecycleStateMachine() {
        List<DepositAccountStatus> allowedDepositStatuses = Arrays.asList(DepositAccountStatus.values());
        return new DepositLifecycleStateMachineImpl(allowedDepositStatuses);
    }

	public void approveSavingAccount(SavingAccountApprovalCommand command, SavingAccount account) {
		
		LocalDate approvalDate = account.projectedCommencementDate();
		if (command.getApprovalDate() != null) {
			approvalDate = command.getApprovalDate();
		}
		
		BigDecimal savingsDepositAmountPerPeriod=account.getSavingsDepositAmountPerPeriod();
		if (command.getDepositAmountPerPeriod() != null) {
			savingsDepositAmountPerPeriod = command.getDepositAmountPerPeriod();
		}
		
		BigDecimal recurringInterestRate = account.getReccuringInterestRate();
		if (command.getRecurringInterestRate() != null) {
			recurringInterestRate = command.getRecurringInterestRate();
		}
		
		BigDecimal savingInterestRate = account.getSavingInterestRate();
		if (command.getSavingInterestRate() != null) {
			savingInterestRate = command.getSavingInterestRate(); 
		}
		
		Integer interestType = account.getInterestType();
		if (command.getInterestType() != null) {
			interestType = SavingsInterestType.fromInt(command.getInterestType()).getValue();
		}
		
		Integer tenure = account.getTenure();
		if (command.getTenure() != null) {
			tenure = command.getTenure();
		}
		
		Integer tenureType = account.getTenureType();
		if (command.getTenureType() != null) {
			tenureType = TenureTypeEnum.fromInt(command.getTenureType()).getValue();
		}
		
		Integer frequency = account.getFrequency();
		if (command.getDepositFrequencyType() != null) {
			frequency = SavingFrequencyType.fromInt(command.getDepositFrequencyType()).getValue();
		}
		
		Integer payEvery = account.getPayEvery();
		if (command.getDepositEvery() != null) {
			payEvery = command.getDepositEvery();
		}
		
		Integer interestPostEvery = account.getInterestPostEvery();
		if (command.getInterestPostEvery() != null) {
			interestPostEvery = command.getInterestPostEvery();
		}
		Integer interestPostFrequency =account.getInterestPostFrequency();
		if (command.getInterestPostFrequency() != null) {
			interestPostFrequency = command.getInterestPostFrequency();
		}
		
		account.approveSavingAccount(approvalDate, savingsDepositAmountPerPeriod, recurringInterestRate, savingInterestRate, interestType,
				tenure, tenureType, frequency, payEvery,defaultDepositLifecycleStateMachine(),this.reccuringDepositInterestCalculator, interestPostEvery, interestPostFrequency);
		
		
		CalculateSavingScheduleCommand calculateSavingScheduleCommand = new CalculateSavingScheduleCommand(account.getProduct().getId(), savingsDepositAmountPerPeriod, payEvery,
				frequency, recurringInterestRate, approvalDate, tenure,tenureType,interestPostEvery,interestPostFrequency,account.getInterestCalculationMethod());
		
		SavingScheduleData savingScheduleData = this.calculateSavingSchedule.calculateSavingSchedule(calculateSavingScheduleCommand);
        for (SavingSchedulePeriodData savingSchedulePeriodData : savingScheduleData.getPeriods()) {

            final SavingScheduleInstallments installment = new SavingScheduleInstallments(account, savingSchedulePeriodData.getDueDate()
                    .toDate(), savingSchedulePeriodData.getPeriod(), savingSchedulePeriodData.getDepositDue(),savingSchedulePeriodData.getInterestAccured());
            account.addSavingScheduleInstallment(installment);
        }
	}

	public void withdrawSavingAccountMoney(	SavingAccountWithdrawalCommand command, SavingAccount account) {
		
		if (command.getAmount().doubleValue() > account.getOutstandingAmount().doubleValue()) {
			throw new DepositAccountTransactionsException("deposit.transaction.interest.withdrawal.exceed", "You can Withdraw "
                    + account.getOutstandingAmount() + " only"); 
		}
		
		account.withdrawAmount(command.getAmount(),command.getTransactionDate());
		
	}

	public void postInterest(SavingAccount account) {
		account.postInterest();
	}

}