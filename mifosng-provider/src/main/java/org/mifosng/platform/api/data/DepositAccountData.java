package org.mifosng.platform.api.data;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.mifosplatform.infrastructure.configuration.data.CurrencyData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

/**
 * Immutable data object for deposit accounts.
 */
public class DepositAccountData {

	private final Long id;
	private final String externalId;
	private final EnumOptionData status;	
	private final Long clientId;
	private final String clientName;
	private final Long productId;
	private final String productName;
	
	private final CurrencyData currency;
	private final BigDecimal deposit;
	private final BigDecimal maturityInterestRate;
	
	private final Integer tenureInMonths;
	private final LocalDate projectedCommencementDate;
	private final LocalDate actualCommencementDate;
	private final LocalDate maturedOn;
	private final LocalDate withdrawnonDate;
	private final LocalDate rejectedonDate;
	private final LocalDate closedonDate;
	private final BigDecimal projectedInterestAccrued;
	private final BigDecimal actualInterestAccrued;
	private final BigDecimal projectedMaturityAmount;
	private final BigDecimal actualMaturityAmount;
	
	private final Integer interestCompoundedEvery;
	private final EnumOptionData interestCompoundedEveryPeriodType;
	private final boolean renewalAllowed;
	private final boolean preClosureAllowed;
	private final BigDecimal preClosureInterestRate;
	
	private final boolean isInterestWithdrawable;
	private final BigDecimal interestPaid;
	private final boolean interestCompoundingAllowed;
	
	private final List<EnumOptionData> interestCompoundedEveryPeriodTypeOptions;
	private final List<DepositProductLookup> productOptions;
	
	private final Collection<DepositAccountTransactionData> transactions;
	private final DepositPermissionData permissions;
	private final BigDecimal availableInterestForWithdrawal;
	private final BigDecimal availableWithdrawalAmount;
	private final LocalDate todaysDate;
	
	private final boolean isLockinPeriodAllowed;
	private final Integer lockinPeriod;
	private final EnumOptionData lockinPeriodType;
	
	/*
	 * used when returning account template data but only a clientId is passed, no selected product.
	 */
	public static DepositAccountData createFrom(final Long clientId, final String clientDisplayName) {
		return new DepositAccountData(clientId, clientDisplayName);
	}
	
	private DepositAccountData(
			final Long clientId, 
			final String clientName) {
		this.id=null;
		this.externalId = null;
		this.status = null;
		this.clientId = clientId;
		this.clientName = clientName;
		this.productId = null;
		this.productName = null;
		this.currency = null;
		this.deposit = null;
		this.maturityInterestRate=null;
		this.tenureInMonths = null;
		this.projectedCommencementDate = null;
		this.actualCommencementDate = null;
		this.maturedOn = null;
		this.projectedInterestAccrued = null;
		this.actualInterestAccrued = null;
		this.projectedMaturityAmount = null;
		this.actualMaturityAmount = null;
		this.interestCompoundedEvery = null;
		this.interestCompoundedEveryPeriodType = null;
		this.renewalAllowed = false;
		this.preClosureAllowed = false;
		this.preClosureInterestRate = null;
		
		this.interestCompoundedEveryPeriodTypeOptions = new ArrayList<EnumOptionData>();
		this.productOptions = new ArrayList<DepositProductLookup>();
		
		this.withdrawnonDate=null;
		this.closedonDate=null;
		this.rejectedonDate=null;
		
		this.transactions=null;
		this.permissions=null;
		
		this.interestPaid=null;
		this.isInterestWithdrawable=false;
		this.interestCompoundingAllowed = true;
		this.availableInterestForWithdrawal = new BigDecimal(0);
		this.availableWithdrawalAmount= new BigDecimal(0);
		this.todaysDate = new LocalDate();
		
		this.isLockinPeriodAllowed =true;
		this.lockinPeriod = null;
		this.lockinPeriodType = null;
	}

	public DepositAccountData(
			final DepositAccountData account, 
			final List<EnumOptionData> interestCompoundedEveryPeriodTypeOptions,
			final Collection<DepositProductLookup> productOptions) {
		this.id = account.getId();
		this.externalId = account.getExternalId();
		this.status = account.getStatus();
		this.clientId = account.getClientId();
		this.clientName = account.getClientName();
		this.productId = account.getProductId();
		this.productName = account.getProductName();
		this.currency = account.getCurrency();
		this.deposit = account.getDeposit();
		this.maturityInterestRate = account.getMaturityInterestRate();
		this.tenureInMonths = account.getTenureInMonths();
		this.projectedCommencementDate = account.getProjectedCommencementDate();
		this.actualCommencementDate = account.getActualCommencementDate();
		this.maturedOn = account.getMaturedOn();
		this.projectedInterestAccrued = account.getProjectedInterestAccrued();
		this.actualInterestAccrued = account.getActualInterestAccrued();
		this.projectedMaturityAmount = account.getProjectedMaturityAmount();
		this.actualMaturityAmount = account.getActualMaturityAmount();
		this.interestCompoundedEvery = account.getInterestCompoundedEvery();
		this.interestCompoundedEveryPeriodType = account.getInterestCompoundedEveryPeriodType();
		this.renewalAllowed = account.isRenewalAllowed();
		this.preClosureAllowed = account.isPreClosureAllowed();
		this.preClosureInterestRate = account.getPreClosureInterestRate();
		
		this.interestCompoundedEveryPeriodTypeOptions = interestCompoundedEveryPeriodTypeOptions;
		this.productOptions = new ArrayList<DepositProductLookup>(productOptions);
		
		this.withdrawnonDate=account.getWithdrawnonDate();
		this.rejectedonDate=account.getRejectedonDate();
		this.closedonDate=account.getClosedonDate();
		
		this.transactions=account.getTransactions();
		this.permissions=account.getPermissions();
		this.interestPaid=account.getInterestPaid();
		this.isInterestWithdrawable=account.isInterestWithdrawable();
		this.interestCompoundingAllowed=account.isInterestCompoundingAllowed();
		this.availableInterestForWithdrawal=determineAvailableInterestForWithdrawal(account);
		this.availableWithdrawalAmount=determineAvalableWithdrawalAmount(account);
		this.todaysDate = new LocalDate();
		
		this.lockinPeriod = account.getLockinPeriod();
		this.lockinPeriodType = account.getLockinPeriodType();
		this.isLockinPeriodAllowed = account.isLockinPeriodAllowed();
	}
	
	public DepositAccountData(final DepositAccountData account, final DepositPermissionData permissions, final Collection<DepositAccountTransactionData> transactions) {
		this.id = account.getId();
		this.externalId = account.getExternalId();
		this.status = account.getStatus();
		this.clientId = account.getClientId();
		this.clientName = account.getClientName();
		this.productId = account.getProductId();
		this.productName = account.getProductName();
		this.currency = account.getCurrency();
		this.deposit = account.getDeposit();
		this.maturityInterestRate = account.getMaturityInterestRate();
		this.tenureInMonths = account.getTenureInMonths();
		this.projectedCommencementDate = account.getProjectedCommencementDate();
		this.actualCommencementDate = account.getActualCommencementDate();
		this.maturedOn = account.getMaturedOn();
		this.projectedInterestAccrued = account.getProjectedInterestAccrued();
		this.actualInterestAccrued = account.getActualInterestAccrued();
		this.projectedMaturityAmount = account.getProjectedMaturityAmount();
		this.actualMaturityAmount = account.getActualMaturityAmount();
		this.interestCompoundedEvery = account.getInterestCompoundedEvery();
		this.interestCompoundedEveryPeriodType = account.getInterestCompoundedEveryPeriodType();
		this.renewalAllowed = account.isRenewalAllowed();
		this.preClosureAllowed = account.isPreClosureAllowed();
		this.preClosureInterestRate = account.getPreClosureInterestRate();
		
		this.interestCompoundedEveryPeriodTypeOptions = account.getInterestCompoundedEveryPeriodTypeOptions();
		this.productOptions = account.getProductOptions();
		
		this.withdrawnonDate=account.getWithdrawnonDate();
		this.rejectedonDate=account.getRejectedonDate();
		this.closedonDate=account.getClosedonDate();
		
		this.transactions=transactions;
		this.permissions=permissions;
		this.interestPaid=account.getInterestPaid();
		this.isInterestWithdrawable=account.isInterestWithdrawable();
		this.interestCompoundingAllowed = account.isInterestCompoundingAllowed();
		this.availableInterestForWithdrawal=determineAvailableInterestForWithdrawal(account);
		this.availableWithdrawalAmount=determineAvalableWithdrawalAmount(account);
		this.todaysDate = new LocalDate();
		
		this.lockinPeriod = account.getLockinPeriod();
		this.lockinPeriodType = account.getLockinPeriodType();
		this.isLockinPeriodAllowed = account.isLockinPeriodAllowed();
	}
	
	public DepositAccountData(
			final Long id,
			final String externalId,
			final EnumOptionData status,
			final Long clientId, 
			final String clientName, 
			final Long productId, 
			final String productName, 
			final CurrencyData currency,
			final BigDecimal deposit, final BigDecimal interestRate, 
			final Integer tenureInMonths, 
			final LocalDate projectedCommencementDate, 
			final LocalDate actualCommencementDate, 
			final LocalDate maturedOn, 
			final BigDecimal projectedInterestAccrued, 
			final BigDecimal actualInterestAccrued, 
			final BigDecimal projectedMaturityAmount, 
			final BigDecimal actualMaturityAmount,
			final Integer interestCompoundedEvery, 
			final EnumOptionData interestCompoundedEveryPeriodType, 
			final boolean renewalAllowed, 
			final boolean preClosureAllowed, 
			final BigDecimal preClosureInterestRate,
			final LocalDate withdrawnonDate,
			final LocalDate rejectedonDate,
			final LocalDate closedonDate,
			final boolean isInterestWithdrawable,
			final BigDecimal interestPaid,
			final boolean interestCompoundingAllowed,
			final boolean isLockinPeriodAllowed,
			final Integer lockinPeriod,
			final EnumOptionData lockinPeriodType) {
		this.id=id;
		this.externalId = externalId;
		this.status = status;
		this.clientId = clientId;
		this.clientName = clientName;
		this.productId = productId;
		this.productName = productName;
		this.currency = currency;
		this.deposit = deposit;
		this.maturityInterestRate=interestRate;
		this.tenureInMonths = tenureInMonths;
		this.projectedCommencementDate = projectedCommencementDate;
		this.actualCommencementDate = actualCommencementDate;
		this.maturedOn = maturedOn;
		this.projectedInterestAccrued = projectedInterestAccrued;
		this.actualInterestAccrued = actualInterestAccrued;
		this.projectedMaturityAmount = projectedMaturityAmount;
		this.actualMaturityAmount = actualMaturityAmount;
		this.interestCompoundedEvery = interestCompoundedEvery;
		this.interestCompoundedEveryPeriodType = interestCompoundedEveryPeriodType;
		this.renewalAllowed = renewalAllowed;
		this.preClosureAllowed = preClosureAllowed;
		this.preClosureInterestRate = preClosureInterestRate;
		
		this.interestCompoundedEveryPeriodTypeOptions = new ArrayList<EnumOptionData>();
		this.productOptions = new ArrayList<DepositProductLookup>();
		
		this.withdrawnonDate = withdrawnonDate;
		this.rejectedonDate = rejectedonDate;
		this.closedonDate = closedonDate;
		
		this.transactions = null;
		this.permissions = null;
		
		this.interestPaid =interestPaid;
		this.isInterestWithdrawable=isInterestWithdrawable;
		this.interestCompoundingAllowed = interestCompoundingAllowed;
		this.availableInterestForWithdrawal = BigDecimal.ZERO;
		this.availableWithdrawalAmount = BigDecimal.ZERO;
		this.todaysDate = new LocalDate();
		
		this.isLockinPeriodAllowed = isLockinPeriodAllowed;
		this.lockinPeriod =lockinPeriod;
		this.lockinPeriodType = lockinPeriodType;
	}
	
	public DepositAccountData(
			final Long clientId, 
			final String clientName, 
			final Long productId, 
			final String productName, 
			final CurrencyData currency,
			final BigDecimal deposit, final BigDecimal interestRate, 
			final Integer tenureInMonths, 
			final Integer interestCompoundedEvery, 
			final EnumOptionData interestCompoundedEveryPeriodType, 
			final boolean renewalAllowed, 
			final boolean preClosureAllowed, 
			final BigDecimal preClosureInterestRate,
			final boolean interestCompoundingAllowed) {
		this.id=null;
		this.externalId = null;
		this.status = null;
		this.clientId = clientId;
		this.clientName = clientName;
		this.productId = productId;
		this.productName = productName;
		this.currency = currency;
		this.deposit = deposit;
		this.maturityInterestRate=interestRate;
		this.tenureInMonths = tenureInMonths;
		this.projectedCommencementDate = new LocalDate();
		this.actualCommencementDate = null;
		this.maturedOn = null;
		this.projectedInterestAccrued = null;
		this.actualInterestAccrued = null;
		this.projectedMaturityAmount = null;
		this.actualMaturityAmount = null;
		this.interestCompoundedEvery = interestCompoundedEvery;
		this.interestCompoundedEveryPeriodType = interestCompoundedEveryPeriodType;
		this.renewalAllowed = renewalAllowed;
		this.preClosureAllowed = preClosureAllowed;
		this.preClosureInterestRate = preClosureInterestRate;
		
		this.interestCompoundedEveryPeriodTypeOptions = new ArrayList<EnumOptionData>();
		this.productOptions = new ArrayList<DepositProductLookup>();
		
		this.withdrawnonDate=null;
		this.closedonDate=null;
		this.rejectedonDate=null;
		
		this.transactions = null;
		this.permissions = null;
		this.interestPaid=null;
		this.isInterestWithdrawable=false;
		this.interestCompoundingAllowed=interestCompoundingAllowed;
		this.availableInterestForWithdrawal= BigDecimal.ZERO;
		this.availableWithdrawalAmount = BigDecimal.ZERO;
		this.todaysDate = new LocalDate();
		
		//need to update in constructor's call method and update it
		this.lockinPeriod = Integer.valueOf(0);
		this.isLockinPeriodAllowed =true;
		this.lockinPeriodType = interestCompoundedEveryPeriodType;
	}

	public Long getId() {
		return id;
	}
	
	public String getExternalId() {
		return externalId;
	}
	
	public EnumOptionData getStatus() {
		return status;
	}

	public Long getClientId() {
		return clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public Long getProductId() {
		return productId;
	}

	public String getProductName() {
		return productName;
	}

	public CurrencyData getCurrency() {
		return currency;
	}

	public BigDecimal getDeposit() {
		return deposit;
	}
	
	public BigDecimal getMaturityInterestRate() {
		return maturityInterestRate;
	}

	public Integer getTenureInMonths() {
		return tenureInMonths;
	}

	public LocalDate getProjectedCommencementDate() {
		return projectedCommencementDate;
	}

	public LocalDate getActualCommencementDate() {
		return actualCommencementDate;
	}

	public LocalDate getMaturedOn() {
		return maturedOn;
	}

	public BigDecimal getProjectedInterestAccrued() {
		return projectedInterestAccrued;
	}

	public BigDecimal getActualInterestAccrued() {
		return actualInterestAccrued;
	}

	public BigDecimal getProjectedMaturityAmount() {
		return projectedMaturityAmount;
	}

	public BigDecimal getActualMaturityAmount() {
		return actualMaturityAmount;
	}
	
	public Integer getInterestCompoundedEvery() {
		return interestCompoundedEvery;
	}

	public EnumOptionData getInterestCompoundedEveryPeriodType() {
		return interestCompoundedEveryPeriodType;
	}

	public boolean isRenewalAllowed() {
		return renewalAllowed;
	}

	public boolean isPreClosureAllowed() {
		return preClosureAllowed;
	}

	public BigDecimal getPreClosureInterestRate() {
		return preClosureInterestRate;
	}

	public List<EnumOptionData> getInterestCompoundedEveryPeriodTypeOptions() {
		return interestCompoundedEveryPeriodTypeOptions;
	}

	public List<DepositProductLookup> getProductOptions() {
		return productOptions;
	}

	public LocalDate getWithdrawnonDate() {
		return withdrawnonDate;
	}

	public LocalDate getRejectedonDate() {
		return rejectedonDate;
	}

	public LocalDate getClosedonDate() {
		return closedonDate;
	}

	public Collection<DepositAccountTransactionData> getTransactions() {
		return transactions;
	}
	
	public DepositPermissionData getPermissions() {
		return permissions;
	}

	public boolean isInterestWithdrawable() {
		return isInterestWithdrawable;
	}

	public BigDecimal getInterestPaid() {
		return interestPaid;
	}

	public boolean isInterestCompoundingAllowed() {
		return interestCompoundingAllowed;
	}

	public BigDecimal getAvailableInterestForWithdrawal() {
		return availableInterestForWithdrawal;
	}

	public BigDecimal getAvailableWithdrawalAmount() {
		return availableWithdrawalAmount;
	}

	public LocalDate getTodaysDate() {
		return todaysDate;
	}

	public boolean isLockinPeriodAllowed() {
		return isLockinPeriodAllowed;
	}

	public Integer getLockinPeriod() {
		return lockinPeriod;
	}

	public EnumOptionData getLockinPeriodType() {
		return lockinPeriodType;
	}

	private BigDecimal determineAvailableInterestForWithdrawal(final DepositAccountData account) {
		BigDecimal availableInterestForWithdrawal = BigDecimal.ZERO;
		
		if(this.status!=null){
		if (account.getStatus().getId() == 300) {
			BigDecimal interestGettingForPeriod = BigDecimal.valueOf(account.getActualInterestAccrued().doubleValue()/ new Double(account.getTenureInMonths()));
			LocalDate interestWithdrawingDate= new LocalDate().isBefore(getMaturedOn())?new LocalDate():getMaturedOn().plusDays(1);
			Integer noOfMonthsforInterestCal = Months.monthsBetween(account.getActualCommencementDate(), interestWithdrawingDate).getMonths();
			Integer noOfPeriods = noOfMonthsforInterestCal/ account.getInterestCompoundedEvery();

			availableInterestForWithdrawal = BigDecimal.valueOf(interestGettingForPeriod.multiply(new BigDecimal(noOfPeriods)).doubleValue()- account.getInterestPaid().doubleValue());
		}else {
			return BigDecimal.ZERO;
		}
		}else return BigDecimal.ZERO;
		
		return availableInterestForWithdrawal;
	}
	
	private BigDecimal determineAvalableWithdrawalAmount(final DepositAccountData account) {
		
		BigDecimal avalablePreclosureWithdrawalAmount = BigDecimal.ZERO;
		if(this.status != null){
			if (account.getStatus().getId() == 300) {
				if(new LocalDate().isBefore(account.getMaturedOn()) || new LocalDate().isEqual(account.getMaturedOn())){
					MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
					Integer monthsInYear = 12;
					BigDecimal interestRateAsFraction = account.getPreClosureInterestRate().divide(BigDecimal.valueOf(100), mc);
					BigDecimal interestRateForOneMonth = interestRateAsFraction.divide(BigDecimal.valueOf(monthsInYear.doubleValue()), mc);
					Integer noOfMonthsforInterestCal = Months.monthsBetween(account.getActualCommencementDate(), new LocalDate()).getMonths();
					Integer days = Days.daysBetween(account.getActualCommencementDate().plusMonths(noOfMonthsforInterestCal), new LocalDate()).getDays();
					BigDecimal interest = BigDecimal.valueOf(account.getDeposit().doubleValue()*new Double(noOfMonthsforInterestCal)*interestRateForOneMonth.doubleValue()); 
					avalablePreclosureWithdrawalAmount = interest.add(account.getDeposit()).subtract(account.getInterestPaid()).add(determineRemainInerestAmount(account.getDeposit(), account.getPreClosureInterestRate(), days));
				}else if(new LocalDate().isAfter(account.getMaturedOn())){
					Integer days = Days.daysBetween(account.getMaturedOn(), new LocalDate()).getDays();
					avalablePreclosureWithdrawalAmount=determineAvailableInterestForWithdrawal(account).add(account.getDeposit()).add(determineRemainInerestAmount(account.getDeposit(), account.getPreClosureInterestRate(), days));
				}
			}else{
				avalablePreclosureWithdrawalAmount=account.getDeposit();
			}
		}
		return avalablePreclosureWithdrawalAmount;
	}
	
	private BigDecimal determineRemainInerestAmount(final BigDecimal depsoitAmount, final BigDecimal interstRateApplicable, final Integer days){

		MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
		Integer daysInYear = 365;
		BigDecimal interestRateAsFraction = interstRateApplicable.divide(BigDecimal.valueOf(100), mc);
		BigDecimal interestRateForOneDay = interestRateAsFraction.divide(BigDecimal.valueOf(daysInYear.doubleValue()), mc);
		return BigDecimal.valueOf(depsoitAmount.doubleValue()*days.doubleValue()*interestRateForOneDay.doubleValue());
		
	}

}