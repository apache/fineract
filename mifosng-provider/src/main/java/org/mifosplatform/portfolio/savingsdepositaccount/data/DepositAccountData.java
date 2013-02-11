package org.mifosplatform.portfolio.savingsdepositaccount.data;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.savingsdepositproduct.data.DepositProductLookup;

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
    
    private final BigDecimal availableInterest;
	private final BigDecimal interestPostedAmount;
	private final LocalDate lastInterestPostedDate; 
	private final LocalDate nextInterestPostedDate;
	
	private final String imageKey;
	private String printFDdetailsLocation;
	private final String fatherName;
	private final String address;

    /*
     * used when returning account template data but only a clientId is passed,
     * no selected product.
     */
    public static DepositAccountData createFrom(final Long clientId, final String clientDisplayName) {
        return new DepositAccountData(clientId, clientDisplayName);
    }

    private DepositAccountData(final Long clientId, final String clientName) {
        this.id = null;
        this.externalId = null;
        this.status = null;
        this.clientId = clientId;
        this.clientName = clientName;
        this.productId = null;
        this.productName = null;
        this.currency = null;
        this.deposit = null;
        this.maturityInterestRate = null;
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
        this.productOptions = null;

        this.withdrawnonDate = null;
        this.closedonDate = null;
        this.rejectedonDate = null;

        this.transactions = null;
        this.permissions = null;

        this.interestPaid = null;
        this.isInterestWithdrawable = false;
        this.interestCompoundingAllowed = false;
        this.availableInterestForWithdrawal = null;
        this.availableWithdrawalAmount = null;
        this.todaysDate = null;

        this.isLockinPeriodAllowed = false;
        this.lockinPeriod = null;
        this.lockinPeriodType = null;

        this.availableInterest = null;
        this.interestPostedAmount = null;
        this.lastInterestPostedDate = null;
        this.nextInterestPostedDate = null;
        
        this.imageKey = null;
		this.printFDdetailsLocation = null;
		this.fatherName = null;
		this.address = null;

    }

    public DepositAccountData(final DepositAccountData account, final List<EnumOptionData> interestCompoundedEveryPeriodTypeOptions,
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
        this.productOptions = defaultIfEmpty(productOptions);

        this.withdrawnonDate = account.getWithdrawnonDate();
        this.rejectedonDate = account.getRejectedonDate();
        this.closedonDate = account.getClosedonDate();

        this.transactions = account.getTransactions();
        this.permissions = account.getPermissions();
        this.interestPaid = account.getInterestPaid();
        this.isInterestWithdrawable = account.isInterestWithdrawable();
        this.interestCompoundingAllowed = account.isInterestCompoundingAllowed();
        this.availableInterestForWithdrawal = determineAvailableInterestForWithdrawal(account);
        this.availableWithdrawalAmount = determineAvalableWithdrawalAmount(account);
        this.todaysDate = new LocalDate();

        this.lockinPeriod = account.getLockinPeriod();
        this.lockinPeriodType = account.getLockinPeriodType();
        this.isLockinPeriodAllowed = account.isLockinPeriodAllowed();

        this.availableInterest = account.getAvailableInterest();
        this.interestPostedAmount = account.getInterestPostedAmount();
        this.lastInterestPostedDate = account.getLastInterestPostedDate();
        this.nextInterestPostedDate = account.getNextInterestPostedDate();
        
        this.imageKey = account.getImageKey();
        this.printFDdetailsLocation = account.getPrintFDdetailsLocation();
        this.fatherName=account.getFatherName();
        this.address=account.getAddress();
    }

    public DepositAccountData(final DepositAccountData account, final DepositPermissionData permissions,
            final Collection<DepositAccountTransactionData> transactions) {
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

        this.withdrawnonDate = account.getWithdrawnonDate();
        this.rejectedonDate = account.getRejectedonDate();
        this.closedonDate = account.getClosedonDate();

        this.transactions = transactions;
        this.permissions = permissions;
        this.interestPaid = account.getInterestPaid();
        this.isInterestWithdrawable = account.isInterestWithdrawable();
        this.interestCompoundingAllowed = account.isInterestCompoundingAllowed();
        this.availableInterestForWithdrawal = determineAvailableInterestForWithdrawal(account);
        this.availableWithdrawalAmount = determineAvalableWithdrawalAmount(account);
        this.todaysDate = new LocalDate();

        this.lockinPeriod = account.getLockinPeriod();
        this.lockinPeriodType = account.getLockinPeriodType();
        this.isLockinPeriodAllowed = account.isLockinPeriodAllowed();

        this.availableInterest = account.getAvailableInterest();
        this.interestPostedAmount = account.getInterestPostedAmount();
        this.lastInterestPostedDate = account.getLastInterestPostedDate();
        this.nextInterestPostedDate = account.getNextInterestPostedDate();
        this.imageKey = account.getImageKey();
        this.printFDdetailsLocation = account.getPrintFDdetailsLocation();
        this.fatherName=account.getFatherName();
        this.address=account.getAddress();
    }

    public DepositAccountData(final Long id, final String externalId, final EnumOptionData status, final Long clientId,
            final String clientName, final Long productId, final String productName, final CurrencyData currency, final BigDecimal deposit,
            final BigDecimal interestRate, final Integer tenureInMonths, final LocalDate projectedCommencementDate,
            final LocalDate actualCommencementDate, final LocalDate maturedOn, final BigDecimal projectedInterestAccrued,
            final BigDecimal actualInterestAccrued, final BigDecimal projectedMaturityAmount, final BigDecimal actualMaturityAmount,
            final Integer interestCompoundedEvery, final EnumOptionData interestCompoundedEveryPeriodType, final boolean renewalAllowed,
            final boolean preClosureAllowed, final BigDecimal preClosureInterestRate, final LocalDate withdrawnonDate,
            final LocalDate rejectedonDate, final LocalDate closedonDate, final boolean isInterestWithdrawable,
            final BigDecimal interestPaid, final boolean interestCompoundingAllowed, final boolean isLockinPeriodAllowed,
            final Integer lockinPeriod, final EnumOptionData lockinPeriodType, final BigDecimal availableInterest,
            final BigDecimal interestPostedAmount, final LocalDate lastInterestPostedDate, final LocalDate nextInterestPostedDate,
            final String fatherName, final String address, final String imageKey) {
        this.id = id;
        this.externalId = externalId;
        this.status = status;
        this.clientId = clientId;
        this.clientName = clientName;
        this.productId = productId;
        this.productName = productName;
        this.currency = currency;
        this.deposit = deposit;
        this.maturityInterestRate = interestRate;
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

        this.interestPaid = interestPaid;
        this.isInterestWithdrawable = isInterestWithdrawable;
        this.interestCompoundingAllowed = interestCompoundingAllowed;
        this.availableInterestForWithdrawal = BigDecimal.ZERO;
        this.availableWithdrawalAmount = BigDecimal.ZERO;
        this.todaysDate = new LocalDate();

        this.isLockinPeriodAllowed = isLockinPeriodAllowed;
        this.lockinPeriod = lockinPeriod;
        this.lockinPeriodType = lockinPeriodType;

        this.availableInterest = availableInterest;
        this.interestPostedAmount = interestPostedAmount;
        this.lastInterestPostedDate = lastInterestPostedDate;
        this.nextInterestPostedDate = nextInterestPostedDate;
        
        this.imageKey = imageKey;
        this.fatherName = fatherName;
        this.address = address;
        this.printFDdetailsLocation = null;
    }

    public DepositAccountData(final Long clientId, final String clientName, final Long productId, final String productName,
            final CurrencyData currency, final BigDecimal deposit, final BigDecimal interestRate, final Integer tenureInMonths,
            final Integer interestCompoundedEvery, final EnumOptionData interestCompoundedEveryPeriodType, final boolean renewalAllowed,
            final boolean preClosureAllowed, final BigDecimal preClosureInterestRate, final boolean interestCompoundingAllowed, Integer lockinPeriod,
			EnumOptionData lockinPeriodType, boolean lockinPeriodAllowed) {
        this.id = null;
        this.externalId = null;
        this.status = null;
        this.clientId = clientId;
        this.clientName = clientName;
        this.productId = productId;
        this.productName = productName;
        this.currency = currency;
        this.deposit = deposit;
        this.maturityInterestRate = interestRate;
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

        this.withdrawnonDate = null;
        this.closedonDate = null;
        this.rejectedonDate = null;

        this.transactions = null;
        this.permissions = null;
        this.interestPaid = null;
        this.isInterestWithdrawable = false;
        this.interestCompoundingAllowed = interestCompoundingAllowed;
        this.availableInterestForWithdrawal = BigDecimal.ZERO;
        this.availableWithdrawalAmount = BigDecimal.ZERO;
        this.todaysDate = null;

        this.lockinPeriod = lockinPeriod;
        this.isLockinPeriodAllowed = lockinPeriodAllowed;
        this.lockinPeriodType = lockinPeriodType;

        this.availableInterest = BigDecimal.ZERO;
        this.interestPostedAmount = BigDecimal.ZERO;
        this.lastInterestPostedDate = null;
        this.nextInterestPostedDate = null;
        
        this.imageKey = null;
        this.printFDdetailsLocation = null;
        this.fatherName=null;
        this.address=null;
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

    public BigDecimal getAvailableInterest() {
        return this.availableInterest;
    }

    public BigDecimal getInterestPostedAmount() {
        return this.interestPostedAmount;
    }

    public LocalDate getLastInterestPostedDate() {
        return this.lastInterestPostedDate;
    }

    public LocalDate getNextInterestPostedDate() {
        return this.nextInterestPostedDate;
    }

    public String getPrintFDdetailsLocation() {
		return this.printFDdetailsLocation;
	}

	public void setPrintFDdetailsLocation(String printFDdetailsLocation) {
		this.printFDdetailsLocation = printFDdetailsLocation;
	}

	public String getImageKey() {
		return this.imageKey;
	}

	public String getFatherName() {
		return this.fatherName;
	}

	public String getAddress() {
		return this.address;
	}

	private BigDecimal determineAvailableInterestForWithdrawal(final DepositAccountData account) {
        BigDecimal availableInterestForWithdrawal = BigDecimal.ZERO;

        if (this.status != null) {
            if (account.getStatus().getId() == 300) {
                BigDecimal interestGettingForPeriod = BigDecimal.valueOf(account.getActualInterestAccrued().doubleValue()
                        / new Double(account.getTenureInMonths()));
                LocalDate interestWithdrawingDate = new LocalDate().isBefore(getMaturedOn()) ? new LocalDate() : getMaturedOn().plusDays(1);
                Integer noOfMonthsforInterestCal = Months.monthsBetween(account.getActualCommencementDate(), interestWithdrawingDate)
                        .getMonths();
                Integer noOfPeriods = noOfMonthsforInterestCal / account.getInterestCompoundedEvery();

                availableInterestForWithdrawal = BigDecimal.valueOf(interestGettingForPeriod.multiply(new BigDecimal(noOfPeriods))
                        .doubleValue() - account.getInterestPaid().doubleValue());
            } else {
                return BigDecimal.ZERO;
            }
        } else
            return null;

        return availableInterestForWithdrawal;
    }

    private BigDecimal determineAvalableWithdrawalAmount(final DepositAccountData account) {

        BigDecimal avalablePreclosureWithdrawalAmount = null;
        if (this.status != null) {
            if (account.getStatus().getId() == 300) {
                // if(new LocalDate().isBefore(account.getMaturedOn()) || new
                // LocalDate().isEqual(account.getMaturedOn())){
                Integer days = Days.daysBetween(account.getLastInterestPostedDate(), new LocalDate()).getDays();
                avalablePreclosureWithdrawalAmount = account.getAvailableInterest().add(account.getDeposit())
                        .add(determineRemainInerestAmount(account.getDeposit(), account.getPreClosureInterestRate(), days));

                // }else if(new LocalDate().isAfter(account.getMaturedOn())){
                // Integer days =
                // Days.daysBetween(account.getLastInterestPostedDate(), new
                // LocalDate()).getDays();
                // avalablePreclosureWithdrawalAmount=account.getAvailableInterest().add(account.getDeposit()).add(determineRemainInerestAmount(account.getDeposit(),
                // account.getPreClosureInterestRate(), days));
                // }
            } else {
                avalablePreclosureWithdrawalAmount = account.getDeposit();
            }
        }
        return avalablePreclosureWithdrawalAmount;
    }

    private BigDecimal determineRemainInerestAmount(final BigDecimal depsoitAmount, final BigDecimal interstRateApplicable,
            final Integer days) {

        MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
        Integer daysInYear = 365;
        BigDecimal interestRateAsFraction = interstRateApplicable.divide(BigDecimal.valueOf(100), mc);
        BigDecimal interestRateForOneDay = interestRateAsFraction.divide(BigDecimal.valueOf(daysInYear.doubleValue()), mc);
        return BigDecimal.valueOf(depsoitAmount.doubleValue() * days.doubleValue() * interestRateForOneDay.doubleValue());

    }
    
    private List<DepositProductLookup> defaultIfEmpty(final Collection<DepositProductLookup> collection) {
        List<DepositProductLookup> returnCollection = null;
        if (collection != null && !collection.isEmpty()) {
            returnCollection = new ArrayList<DepositProductLookup>(collection);
        }
        return returnCollection;
    }

}