package org.mifosplatform.portfolio.savingsaccount.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.savingsaccountproduct.data.SavingProductLookup;

/**
 * Immutable data object reprsenting a savings account.
 */
public class SavingAccountData {

    private final Long id;
    private final EnumOptionData status;
    private final String externalId;
    private final Long clientId;
    private final String clientName;
    private final Long productId;
    private final String productName;
    private final EnumOptionData productType;
    private final CurrencyData currencyData;
    private final BigDecimal savingsDepositAmountPerPeriod;
    private final EnumOptionData savingsFrequencyType;
    private final BigDecimal totalDepositAmount;
    private final BigDecimal recurringInterestRate;
    private final BigDecimal savingInterestRate;
    private final EnumOptionData interestType;
    private final EnumOptionData interestCalculationMethod;
    private final Integer tenure;
    private final EnumOptionData tenureType;
    private final LocalDate projectedCommencementDate;
    private final LocalDate actualCommencementDate;
    private final LocalDate maturesOnDate;
    private final BigDecimal projectedInterestAccuredOnMaturity;
    private final BigDecimal actualInterestAccured;
    private final BigDecimal projectedMaturityAmount;
    private final BigDecimal actualMaturityAmount;
    private final boolean preClosureAllowed;
    private final BigDecimal preClosureInterestRate;
    private final LocalDate withdrawnonDate;
    private final LocalDate rejectedonDate;
    private final LocalDate closedonDate;
    private final boolean isLockinPeriodAllowed;
    private final Integer lockinPeriod;
    private final EnumOptionData lockinPeriodType;
    private final Integer depositEvery;

    @SuppressWarnings("unused")
    private final List<SavingProductLookup> productOptions;
    @SuppressWarnings("unused")
    private final List<CurrencyData> currencyOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> savingsProductTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> tenureTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> savingFrequencyOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> savingsInterestTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> lockinPeriodTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> interestCalculationOptions;
    
    private final Collection<SavingScheduleData> savingScheduleDatas;
    private final SavingPermissionData permissions;
	private final BigDecimal outstandingAmount;
	private final BigDecimal dueAmount;
	private final SavingScheduleData savingScheduleData;
    
    public static SavingAccountData createFrom(final Long clientId, final String clientDisplayName) {
        return new SavingAccountData(clientId, clientDisplayName);
    }

    public SavingAccountData(Long id, EnumOptionData status, String externalId, Long clientId, String clientName, Long productId,
            String productName, EnumOptionData productType, CurrencyData currencyData, BigDecimal savingsDepositAmountPerPeriod,
            EnumOptionData savingsFrequencyType, BigDecimal totalDepositAmount, BigDecimal recurringInterestRate,
            BigDecimal savingInterestRate, EnumOptionData interestType, EnumOptionData interestCalculationMethod, Integer tenure,
            EnumOptionData tenureType, LocalDate projectedCommencementDate, LocalDate actualCommencementDate, LocalDate maturesOnDate,
            BigDecimal projectedInterestAccuredOnMaturity, BigDecimal actualInterestAccured, BigDecimal projectedMaturityAmount,
            BigDecimal actualMaturityAmount, boolean preClosureAllowed, BigDecimal preClosureInterestRate, LocalDate withdrawnonDate,
            LocalDate rejectedonDate, LocalDate closedonDate, boolean isLockinPeriodAllowed, Integer lockinPeriod,
            EnumOptionData lockinPeriodType, Integer depositEvery, BigDecimal outstandingAmount) {
        this.id = id;
        this.status = status;
        this.externalId = externalId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.productId = productId;
        this.productName = productName;
        this.productType = productType;
        this.currencyData = currencyData;
        this.savingsDepositAmountPerPeriod = savingsDepositAmountPerPeriod;
        this.savingsFrequencyType = savingsFrequencyType;
        this.totalDepositAmount = totalDepositAmount;
        this.recurringInterestRate = recurringInterestRate;
        this.savingInterestRate = savingInterestRate;
        this.interestType = interestType;
        this.interestCalculationMethod = interestCalculationMethod;
        this.tenure = tenure;
        this.tenureType = tenureType;
        this.projectedCommencementDate = projectedCommencementDate;
        this.actualCommencementDate = actualCommencementDate;
        this.maturesOnDate = maturesOnDate;
        this.projectedInterestAccuredOnMaturity = projectedInterestAccuredOnMaturity;
        this.actualInterestAccured = actualInterestAccured;
        this.projectedMaturityAmount = projectedMaturityAmount;
        this.actualMaturityAmount = actualMaturityAmount;
        this.preClosureAllowed = preClosureAllowed;
        this.preClosureInterestRate = preClosureInterestRate;
        this.withdrawnonDate = withdrawnonDate;
        this.rejectedonDate = rejectedonDate;
        this.closedonDate = closedonDate;
        this.isLockinPeriodAllowed = isLockinPeriodAllowed;
        this.lockinPeriod = lockinPeriod;
        this.lockinPeriodType = lockinPeriodType;
        this.depositEvery = depositEvery;
        this.productOptions = new ArrayList<SavingProductLookup>();
        this.currencyOptions = null;
        this.savingsProductTypeOptions = null;
        this.tenureTypeOptions = null;
        this.savingFrequencyOptions = null;
        this.savingsInterestTypeOptions = null;
        this.lockinPeriodTypeOptions = null;
        this.interestCalculationOptions = null;
        this.savingScheduleDatas = null;
        this.permissions = null;
        this.outstandingAmount = outstandingAmount;
        this.dueAmount = BigDecimal.ZERO;
        this.savingScheduleData = null;
    }

    public SavingAccountData(Long clientId, String clientName) {
        this.id = null;
        this.status = null;
        this.externalId = null;
        this.clientId = clientId;
        this.clientName = clientName;
        this.productId = null;
        this.productName = null;
        this.productType = null;
        this.currencyData = new CurrencyData("USD", "US Dollar", 2, "$", "currency.USD");
        this.savingsDepositAmountPerPeriod = null;
        this.savingsFrequencyType = null;
        this.totalDepositAmount = null;
        this.recurringInterestRate = null;
        this.savingInterestRate = null;
        this.interestType = null;
        this.interestCalculationMethod = null;
        this.tenure = null;
        this.tenureType = null;
        this.projectedCommencementDate = null;
        this.actualCommencementDate = null;
        this.maturesOnDate = null;
        this.projectedInterestAccuredOnMaturity = null;
        this.actualInterestAccured = null;
        this.projectedMaturityAmount = null;
        this.actualMaturityAmount = null;
        this.preClosureAllowed = false;
        this.preClosureInterestRate = null;
        this.withdrawnonDate = null;
        this.rejectedonDate = null;
        this.closedonDate = null;
        this.isLockinPeriodAllowed = false;
        this.lockinPeriod = null;
        this.lockinPeriodType = null;
        this.depositEvery = null;
        this.productOptions = new ArrayList<SavingProductLookup>();
        this.currencyOptions = null;
        this.savingsProductTypeOptions = null;
        this.tenureTypeOptions = null;
        this.savingFrequencyOptions = null;
        this.savingsInterestTypeOptions = null;
        this.lockinPeriodTypeOptions = null;
        this.interestCalculationOptions = null;
        this.savingScheduleDatas = null;
        this.permissions = null;
        this.outstandingAmount = BigDecimal.ZERO;
        this.dueAmount =BigDecimal.ZERO;
        this.savingScheduleData = null;
    }

    public SavingAccountData(SavingAccountData account, Collection<SavingProductLookup> productOptions, List<CurrencyData> currencyOptions,
            List<EnumOptionData> savingsProductTypeOptions, List<EnumOptionData> tenureTypeOptions,
            List<EnumOptionData> savingFrequencyOptions, List<EnumOptionData> savingsInterestTypeOptions,
            List<EnumOptionData> lockinPeriodTypeOptions, List<EnumOptionData> interestCalculationOptions,BigDecimal dueAmount) {
        this.id = account.id;
        this.status = account.status;
        this.externalId = account.externalId;
        this.clientId = account.clientId;
        this.clientName = account.clientName;
        this.productId = account.productId;
        this.productName = account.productName;
        this.productType = account.productType;
        this.currencyData = account.currencyData;
        this.savingsDepositAmountPerPeriod = account.savingsDepositAmountPerPeriod;
        this.savingsFrequencyType = account.savingsFrequencyType;
        this.totalDepositAmount = account.totalDepositAmount;
        this.recurringInterestRate = account.recurringInterestRate;
        this.savingInterestRate = account.savingInterestRate;
        this.interestType = account.interestType;
        this.interestCalculationMethod = account.interestCalculationMethod;
        this.tenure = account.tenure;
        this.tenureType = account.tenureType;
        this.projectedCommencementDate = account.projectedCommencementDate;
        this.actualCommencementDate = account.actualCommencementDate;
        this.maturesOnDate = account.maturesOnDate;
        this.projectedInterestAccuredOnMaturity = account.projectedInterestAccuredOnMaturity;
        this.actualInterestAccured = account.actualInterestAccured;
        this.projectedMaturityAmount = account.projectedMaturityAmount;
        this.actualMaturityAmount = account.actualMaturityAmount;
        this.preClosureAllowed = account.preClosureAllowed;
        this.preClosureInterestRate = account.preClosureInterestRate;
        this.withdrawnonDate = account.withdrawnonDate;
        this.rejectedonDate = account.rejectedonDate;
        this.closedonDate = account.closedonDate;
        this.isLockinPeriodAllowed = account.isLockinPeriodAllowed;
        this.lockinPeriod = account.lockinPeriod;
        this.lockinPeriodType = account.lockinPeriodType;
        this.depositEvery = account.depositEvery;

        this.productOptions = (List<SavingProductLookup>) productOptions;
        this.currencyOptions = currencyOptions;
        this.savingsProductTypeOptions = savingsProductTypeOptions;
        this.tenureTypeOptions = tenureTypeOptions;
        this.savingFrequencyOptions = savingFrequencyOptions;
        this.savingsInterestTypeOptions = savingsInterestTypeOptions;
        this.lockinPeriodTypeOptions = lockinPeriodTypeOptions;
        this.interestCalculationOptions = interestCalculationOptions;
        this.dueAmount = dueAmount;
        
        this.savingScheduleDatas = account.savingScheduleDatas;
        this.permissions = account.permissions;
        this.outstandingAmount = account.outstandingAmount;
        this.savingScheduleData = account.savingScheduleData;
    }

    // FIXME - Madhukar - unused variables been passed into construction - why?
    public SavingAccountData(Long clientId, String clientName, Long productId, String productName, CurrencyData currency,
            BigDecimal interestRate, BigDecimal savingsDepositAmountPerPeriod, EnumOptionData productType, EnumOptionData tenureType,
            Integer tenure, EnumOptionData savingsFrequencyType, EnumOptionData interestType, EnumOptionData interestCalculationMethod,
            BigDecimal minimumBalanceForWithdrawal, boolean partialDepositAllowed, boolean lockinPeriodAllowed, Integer lockinPeriod,
            EnumOptionData lockinPeriodType, Integer depositEvery) {

        this.id = null;
        this.status = null;
        this.externalId = null;
        this.clientId = clientId;
        this.clientName = clientName;
        this.productId = productId;
        this.productName = productName;
        this.productType = productType;
        this.currencyData = currency;
        this.savingsDepositAmountPerPeriod = savingsDepositAmountPerPeriod;
        this.savingsFrequencyType = savingsFrequencyType;
        this.totalDepositAmount = null;
        this.recurringInterestRate = interestRate;
        this.savingInterestRate = interestRate;
        this.interestType = interestType;
        this.interestCalculationMethod = interestCalculationMethod;
        this.tenure = tenure;
        this.tenureType = tenureType;
        this.projectedCommencementDate = new LocalDate();
        this.actualCommencementDate = null;
        this.maturesOnDate = null;
        this.projectedInterestAccuredOnMaturity = null;
        this.actualInterestAccured = null;
        this.projectedMaturityAmount = null;
        this.actualMaturityAmount = null;
        this.preClosureAllowed = true;
        this.preClosureInterestRate = null;
        this.withdrawnonDate = null;
        this.rejectedonDate = null;
        this.closedonDate = null;
        this.isLockinPeriodAllowed = false;
        this.lockinPeriod = lockinPeriod;
        this.lockinPeriodType = lockinPeriodType;
        this.depositEvery = depositEvery;
        this.productOptions = null;
        this.currencyOptions = null;
        this.savingsProductTypeOptions = null;
        this.tenureTypeOptions = null;
        this.savingFrequencyOptions = null;
        this.savingsInterestTypeOptions = null;
        this.lockinPeriodTypeOptions = null;
        this.interestCalculationOptions = null;
        this.savingScheduleDatas = null;
        this.permissions = null;
        this.outstandingAmount = BigDecimal.ZERO;
        this.dueAmount = BigDecimal.ZERO;
        this.savingScheduleData = null;
    }

	public SavingAccountData(SavingAccountData account,	SavingPermissionData permissions) {
		this.id = account.id;
        this.status = account.status;
        this.externalId = account.externalId;
        this.clientId = account.clientId;
        this.clientName = account.clientName;
        this.productId = account.productId;
        this.productName = account.productName;
        this.productType = account.productType;
        this.currencyData = account.currencyData;
        this.savingsDepositAmountPerPeriod = account.savingsDepositAmountPerPeriod;
        this.savingsFrequencyType = account.savingsFrequencyType;
        this.totalDepositAmount = account.totalDepositAmount;
        this.recurringInterestRate = account.recurringInterestRate;
        this.savingInterestRate = account.savingInterestRate;
        this.interestType = account.interestType;
        this.interestCalculationMethod = account.interestCalculationMethod;
        this.tenure = account.tenure;
        this.tenureType = account.tenureType;
        this.projectedCommencementDate = account.projectedCommencementDate;
        this.actualCommencementDate = account.actualCommencementDate;
        this.maturesOnDate = account.maturesOnDate;
        this.projectedInterestAccuredOnMaturity = account.projectedInterestAccuredOnMaturity;
        this.actualInterestAccured = account.actualInterestAccured;
        this.projectedMaturityAmount = account.projectedMaturityAmount;
        this.actualMaturityAmount = account.actualMaturityAmount;
        this.preClosureAllowed = account.preClosureAllowed;
        this.preClosureInterestRate = account.preClosureInterestRate;
        this.withdrawnonDate = account.withdrawnonDate;
        this.rejectedonDate = account.rejectedonDate;
        this.closedonDate = account.closedonDate;
        this.isLockinPeriodAllowed = account.isLockinPeriodAllowed;
        this.lockinPeriod = account.lockinPeriod;
        this.lockinPeriodType = account.lockinPeriodType;
        this.depositEvery = account.depositEvery;
        
        this.productOptions = account.productOptions;
        this.currencyOptions = account.currencyOptions;
        this.savingsProductTypeOptions = account.savingsProductTypeOptions;
        this.tenureTypeOptions = account.tenureTypeOptions;
        this.savingFrequencyOptions = account.savingFrequencyOptions;
        this.savingsInterestTypeOptions = account.savingsInterestTypeOptions;
        this.lockinPeriodTypeOptions = account.lockinPeriodTypeOptions;
        this.interestCalculationOptions = account.interestCalculationOptions;
        this.savingScheduleDatas = account.savingScheduleDatas;
        this.permissions = permissions;
        this.outstandingAmount = account.outstandingAmount;
        this.dueAmount = account.dueAmount;
        this.savingScheduleData = account.savingScheduleData;
	}

	public SavingAccountData(SavingAccountData account, SavingScheduleData savingScheduleData) {
		this.id = account.id;
        this.status = account.status;
        this.externalId = account.externalId;
        this.clientId = account.clientId;
        this.clientName = account.clientName;
        this.productId = account.productId;
        this.productName = account.productName;
        this.productType = account.productType;
        this.currencyData = account.currencyData;
        this.savingsDepositAmountPerPeriod = account.savingsDepositAmountPerPeriod;
        this.savingsFrequencyType = account.savingsFrequencyType;
        this.totalDepositAmount = account.totalDepositAmount;
        this.recurringInterestRate = account.recurringInterestRate;
        this.savingInterestRate = account.savingInterestRate;
        this.interestType = account.interestType;
        this.interestCalculationMethod = account.interestCalculationMethod;
        this.tenure = account.tenure;
        this.tenureType = account.tenureType;
        this.projectedCommencementDate = account.projectedCommencementDate;
        this.actualCommencementDate = account.actualCommencementDate;
        this.maturesOnDate = account.maturesOnDate;
        this.projectedInterestAccuredOnMaturity = account.projectedInterestAccuredOnMaturity;
        this.actualInterestAccured = account.actualInterestAccured;
        this.projectedMaturityAmount = account.projectedMaturityAmount;
        this.actualMaturityAmount = account.actualMaturityAmount;
        this.preClosureAllowed = account.preClosureAllowed;
        this.preClosureInterestRate = account.preClosureInterestRate;
        this.withdrawnonDate = account.withdrawnonDate;
        this.rejectedonDate = account.rejectedonDate;
        this.closedonDate = account.closedonDate;
        this.isLockinPeriodAllowed = account.isLockinPeriodAllowed;
        this.lockinPeriod = account.lockinPeriod;
        this.lockinPeriodType = account.lockinPeriodType;
        this.depositEvery = account.depositEvery;
        
        this.productOptions = account.productOptions;
        this.currencyOptions = account.currencyOptions;
        this.savingsProductTypeOptions = account.savingsProductTypeOptions;
        this.tenureTypeOptions = account.tenureTypeOptions;
        this.savingFrequencyOptions = account.savingFrequencyOptions;
        this.savingsInterestTypeOptions = account.savingsInterestTypeOptions;
        this.lockinPeriodTypeOptions = account.lockinPeriodTypeOptions;
        this.interestCalculationOptions = account.interestCalculationOptions;
        this.savingScheduleDatas = account.savingScheduleDatas;
        this.permissions = account.permissions;
        this.outstandingAmount = account.outstandingAmount;
        this.dueAmount = account.dueAmount;
        this.savingScheduleData = savingScheduleData;
	}

	public Long getId() {
		return this.id;
	}

	public EnumOptionData getStatus() {
		return this.status;
	}

	public String getExternalId() {
		return this.externalId;
	}

	public Long getClientId() {
		return this.clientId;
	}

	public String getClientName() {
		return this.clientName;
	}

	public Long getProductId() {
		return this.productId;
	}

	public String getProductName() {
		return this.productName;
	}

	public EnumOptionData getProductType() {
		return this.productType;
	}

	public CurrencyData getCurrencyData() {
		return this.currencyData;
	}

	public BigDecimal getSavingsDepostiAmountPerPeriod() {
		return this.savingsDepositAmountPerPeriod;
	}

	public EnumOptionData getSavingsFrequencyType() {
		return this.savingsFrequencyType;
	}

	public BigDecimal getTotalDepositAmount() {
		return this.totalDepositAmount;
	}

	public BigDecimal getReccuringInterestRate() {
		return this.recurringInterestRate;
	}

	public BigDecimal getSavingInterestRate() {
		return this.savingInterestRate;
	}

	public EnumOptionData getInterestType() {
		return this.interestType;
	}

	public EnumOptionData getInterestCalculationMethod() {
		return this.interestCalculationMethod;
	}

	public Integer getTenure() {
		return this.tenure;
	}

	public EnumOptionData getTenureType() {
		return this.tenureType;
	}

	public LocalDate getProjectedCommencementDate() {
		return this.projectedCommencementDate;
	}

	public LocalDate getActualCommencementDate() {
		return this.actualCommencementDate;
	}

	public LocalDate getMaturesOnDate() {
		return this.maturesOnDate;
	}

	public BigDecimal getProjectedInterestAccuredOnMaturity() {
		return this.projectedInterestAccuredOnMaturity;
	}

	public BigDecimal getActualInterestAccured() {
		return this.actualInterestAccured;
	}

	public BigDecimal getProjectedMaturityAmount() {
		return this.projectedMaturityAmount;
	}

	public BigDecimal getActualMaturityAmount() {
		return this.actualMaturityAmount;
	}

	public boolean isPreClosureAllowed() {
		return this.preClosureAllowed;
	}

	public BigDecimal getPreClosureInterestRate() {
		return this.preClosureInterestRate;
	}

	public LocalDate getWithdrawnonDate() {
		return this.withdrawnonDate;
	}

	public LocalDate getRejectedonDate() {
		return this.rejectedonDate;
	}

	public LocalDate getClosedonDate() {
		return this.closedonDate;
	}

	public boolean isLockinPeriodAllowed() {
		return this.isLockinPeriodAllowed;
	}

	public Integer getLockinPeriod() {
		return this.lockinPeriod;
	}

	public EnumOptionData getLockinPeriodType() {
		return this.lockinPeriodType;
	}

	public Integer getDepositEvery() {
		return this.depositEvery;
	}

	public List<SavingProductLookup> getProductOptions() {
		return this.productOptions;
	}

	public List<CurrencyData> getCurrencyOptions() {
		return this.currencyOptions;
	}

	public List<EnumOptionData> getSavingsProductTypeOptions() {
		return this.savingsProductTypeOptions;
	}

	public List<EnumOptionData> getTenureTypeOptions() {
		return this.tenureTypeOptions;
	}

	public List<EnumOptionData> getSavingFrequencyOptions() {
		return this.savingFrequencyOptions;
	}

	public List<EnumOptionData> getSavingsInterestTypeOptions() {
		return this.savingsInterestTypeOptions;
	}

	public List<EnumOptionData> getLockinPeriodTypeOptions() {
		return this.lockinPeriodTypeOptions;
	}

	public List<EnumOptionData> getInterestCalculationOptions() {
		return this.interestCalculationOptions;
	}

	public BigDecimal getOutstandingAmount() {
		return this.outstandingAmount;
	}
	
}