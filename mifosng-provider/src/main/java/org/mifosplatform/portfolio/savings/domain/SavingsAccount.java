/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.allowOverdraftParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.dueAsOfDateParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.enforceMinRequiredBalanceParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.localeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.minRequiredBalanceParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.overdraftLimitParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.withdrawalFeeForTransfersParamName;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.RandomPasswordGenerator;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.accountdetails.domain.AccountType;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.exception.SavingsAccountChargeNotFoundException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.SavingsApiConstants;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.mifosplatform.portfolio.savings.domain.interest.PostingPeriod;
import org.mifosplatform.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.mifosplatform.portfolio.savings.exception.SavingsAccountTransactionNotFoundException;
import org.mifosplatform.portfolio.savings.exception.SavingsActivityPriorToClientTransferException;
import org.mifosplatform.portfolio.savings.exception.SavingsOfficerAssignmentDateException;
import org.mifosplatform.portfolio.savings.exception.SavingsOfficerUnassignmentDateException;
import org.mifosplatform.portfolio.savings.exception.SavingsTransferTransactionsCannotBeUndoneException;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonArray;

@Entity
@Table(name = "m_savings_account", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_no" }, name = "sa_account_no_UNIQUE"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "sa_external_id_UNIQUE") })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "deposit_type_enum", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("100")
public class SavingsAccount extends AbstractPersistable<Long> {

    @Version
    int version;

    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    protected String accountNumber;

    @Column(name = "external_id", nullable = true)
    protected String externalId;

    @ManyToOne(optional = true)
    @JoinColumn(name = "client_id", nullable = true)
    protected Client client;

    @ManyToOne(optional = true)
    @JoinColumn(name = "group_id", nullable = true)
    protected Group group;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    protected SavingsProduct product;

    @ManyToOne
    @JoinColumn(name = "field_officer_id", nullable = true)
    protected Staff savingsOfficer;

    @Column(name = "status_enum", nullable = false)
    protected Integer status;

    @Column(name = "account_type_enum", nullable = false)
    protected Integer accountType;

    @Temporal(TemporalType.DATE)
    @Column(name = "submittedon_date", nullable = true)
    protected Date submittedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "submittedon_userid", nullable = true)
    protected AppUser submittedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "rejectedon_date")
    protected Date rejectedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "rejectedon_userid", nullable = true)
    protected AppUser rejectedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "withdrawnon_date")
    protected Date withdrawnOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "withdrawnon_userid", nullable = true)
    protected AppUser withdrawnBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "approvedon_date")
    protected Date approvedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "approvedon_userid", nullable = true)
    protected AppUser approvedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "activatedon_date", nullable = true)
    protected Date activatedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "activatedon_userid", nullable = true)
    protected AppUser activatedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "closedon_date")
    protected Date closedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "closedon_userid", nullable = true)
    protected AppUser closedBy;

    @Embedded
    protected MonetaryCurrency currency;

    @Column(name = "nominal_annual_interest_rate", scale = 6, precision = 19, nullable = false)
    protected BigDecimal nominalAnnualInterestRate;

    /**
     * The interest period is the span of time at the end of which savings in a
     * client's account earn interest.
     * 
     * A value from the {@link SavingsCompoundingInterestPeriodType}
     * enumeration.
     */
    @Column(name = "interest_compounding_period_enum", nullable = false)
    protected Integer interestCompoundingPeriodType;

    /**
     * A value from the {@link SavingsPostingInterestPeriodType} enumeration.
     */
    @Column(name = "interest_posting_period_enum", nullable = false)
    protected Integer interestPostingPeriodType;

    /**
     * A value from the {@link SavingsInterestCalculationType} enumeration.
     */
    @Column(name = "interest_calculation_type_enum", nullable = false)
    protected Integer interestCalculationType;

    /**
     * A value from the {@link SavingsInterestCalculationDaysInYearType}
     * enumeration.
     */
    @Column(name = "interest_calculation_days_in_year_type_enum", nullable = false)
    protected Integer interestCalculationDaysInYearType;

    @Column(name = "min_required_opening_balance", scale = 6, precision = 19, nullable = true)
    protected BigDecimal minRequiredOpeningBalance;

    @Column(name = "lockin_period_frequency", nullable = true)
    protected Integer lockinPeriodFrequency;

    @Column(name = "lockin_period_frequency_enum", nullable = true)
    protected Integer lockinPeriodFrequencyType;

    /**
     * When account becomes <code>active</code> this field is derived if
     * <code>lockinPeriodFrequency</code> and
     * <code>lockinPeriodFrequencyType</code> details are present.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "lockedin_until_date_derived", nullable = true)
    protected Date lockedInUntilDate;

    @Column(name = "withdrawal_fee_for_transfer", nullable = true)
    protected boolean withdrawalFeeApplicableForTransfer;

    @Column(name = "allow_overdraft")
    private boolean allowOverdraft;

    @Column(name = "overdraft_limit", scale = 6, precision = 19, nullable = true)
    private BigDecimal overdraftLimit;

    @Column(name = "enforce_min_required_balance")
    private boolean enforceMinRequiredBalance;

    @Column(name = "min_required_balance", scale = 6, precision = 19, nullable = true)
    private BigDecimal minRequiredBalance;

    @Column(name = "on_hold_funds_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal onHoldFunds;

    @Temporal(TemporalType.DATE)
    @Column(name = "start_interest_calculation_date")
    protected Date startInterestCalculationDate;

    @Embedded
    protected SavingsAccountSummary summary;

    @OrderBy(value = "dateOf, createdDate, id")
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "savingsAccount", orphanRemoval = true)
    protected final List<SavingsAccountTransaction> transactions = new ArrayList<>();

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "savingsAccount", orphanRemoval = true)
    protected Set<SavingsAccountCharge> charges = new HashSet<>();

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "savingsAccount", orphanRemoval = true)
    private Set<SavingsOfficerAssignmentHistory> savingsOfficerHistory;

    @Transient
    protected boolean accountNumberRequiresAutoGeneration = false;
    @Transient
    protected SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper;
    @Transient
    protected SavingsHelper savingsHelper;

    @Column(name = "deposit_type_enum", insertable = false, updatable = false)
    private Integer depositType;

    @Column(name = "min_balance_for_interest_calculation", scale = 6, precision = 19, nullable = true)
    private BigDecimal minBalanceForInterestCalculation;

    protected SavingsAccount() {
        //
    }

    public static SavingsAccount createNewApplicationForSubmittal(final Client client, final Group group, final SavingsProduct product,
            final Staff fieldOfficer, final String accountNo, final String externalId, final AccountType accountType,
            final LocalDate submittedOnDate, final AppUser submittedBy, final BigDecimal interestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final Set<SavingsAccountCharge> savingsAccountCharges,
            final boolean allowOverdraft, final BigDecimal overdraftLimit, final boolean enforceMinRequiredBalance,
            final BigDecimal minRequiredBalance) {

        final SavingsAccountStatusType status = SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL;
        return new SavingsAccount(client, group, product, fieldOfficer, accountNo, externalId, status, accountType, submittedOnDate,
                submittedBy, interestRate, interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType,
                interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType,
                withdrawalFeeApplicableForTransfer, savingsAccountCharges, allowOverdraft, overdraftLimit, enforceMinRequiredBalance,
                minRequiredBalance);
    }

    protected SavingsAccount(final Client client, final Group group, final SavingsProduct product, final Staff fieldOfficer,
            final String accountNo, final String externalId, final SavingsAccountStatusType status, final AccountType accountType,
            final LocalDate submittedOnDate, final AppUser submittedBy, final BigDecimal nominalAnnualInterestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final Set<SavingsAccountCharge> savingsAccountCharges,
            final boolean allowOverdraft, final BigDecimal overdraftLimit) {
        this(client, group, product, fieldOfficer, accountNo, externalId, status, accountType, submittedOnDate, submittedBy,
                nominalAnnualInterestRate, interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType,
                interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType,
                withdrawalFeeApplicableForTransfer, savingsAccountCharges, allowOverdraft, overdraftLimit, false, null);
    }

    protected SavingsAccount(final Client client, final Group group, final SavingsProduct product, final Staff savingsOfficer,
            final String accountNo, final String externalId, final SavingsAccountStatusType status, final AccountType accountType,
            final LocalDate submittedOnDate, final AppUser submittedBy, final BigDecimal nominalAnnualInterestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final Set<SavingsAccountCharge> savingsAccountCharges,
            final boolean allowOverdraft, final BigDecimal overdraftLimit, final boolean enforceMinRequiredBalance,
            final BigDecimal minRequiredBalance) {
        this.client = client;
        this.group = group;
        this.product = product;
        this.savingsOfficer = savingsOfficer;
        if (StringUtils.isBlank(accountNo)) {
            this.accountNumber = new RandomPasswordGenerator(19).generate();
            this.accountNumberRequiresAutoGeneration = true;
        } else {
            this.accountNumber = accountNo;
        }

        this.currency = product.currency();
        this.externalId = externalId;
        this.status = status.getValue();
        this.accountType = accountType.getValue();
        this.submittedOnDate = submittedOnDate.toDate();
        this.submittedBy = submittedBy;
        this.nominalAnnualInterestRate = nominalAnnualInterestRate;
        this.interestCompoundingPeriodType = interestCompoundingPeriodType.getValue();
        this.interestPostingPeriodType = interestPostingPeriodType.getValue();
        this.interestCalculationType = interestCalculationType.getValue();
        this.interestCalculationDaysInYearType = interestCalculationDaysInYearType.getValue();
        this.minRequiredOpeningBalance = minRequiredOpeningBalance;
        this.lockinPeriodFrequency = lockinPeriodFrequency;
        if (lockinPeriodFrequencyType != null) {
            this.lockinPeriodFrequencyType = lockinPeriodFrequencyType.getValue();
        }
        this.withdrawalFeeApplicableForTransfer = withdrawalFeeApplicableForTransfer;

        if (!CollectionUtils.isEmpty(savingsAccountCharges)) {
            this.charges = associateChargesWithThisSavingsAccount(savingsAccountCharges);
        }

        this.summary = new SavingsAccountSummary();
        this.allowOverdraft = allowOverdraft;
        this.overdraftLimit = overdraftLimit;

        this.enforceMinRequiredBalance = enforceMinRequiredBalance;
        this.minRequiredBalance = minRequiredBalance;
        this.minBalanceForInterestCalculation = product.minBalanceForInterestCalculation();
        this.savingsOfficerHistory = null;
    }

    /**
     * Used after fetching/hydrating a {@link SavingsAccount} object to inject
     * helper services/components used for update summary details after
     * events/transactions on a {@link SavingsAccount}.
     */
    public void setHelpers(final SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper,
            final SavingsHelper savingsHelper) {
        this.savingsAccountTransactionSummaryWrapper = savingsAccountTransactionSummaryWrapper;
        this.savingsHelper = savingsHelper;
    }

    public boolean isNotActive() {
        return !isActive();
    }

    public boolean isActive() {
        return SavingsAccountStatusType.fromInt(this.status).isActive();
    }

    public boolean isNotSubmittedAndPendingApproval() {
        return !isSubmittedAndPendingApproval();
    }

    public boolean isSubmittedAndPendingApproval() {
        return SavingsAccountStatusType.fromInt(this.status).isSubmittedAndPendingApproval();
    }

    public boolean isApproved() {
        return SavingsAccountStatusType.fromInt(this.status).isApproved();
    }

    public boolean isActivated() {
        boolean isActive = false;
        if (this.activatedOnDate != null) {
            isActive = true;
        }
        return isActive;
    }

    public boolean isClosed() {
        return SavingsAccountStatusType.fromInt(this.status).isClosed();
    }

    public void postInterest(final MathContext mc, final LocalDate interestPostingUpToDate, final boolean isInterestTransfer,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {

        final List<PostingPeriod> postingPeriods = calculateInterestUsing(mc, interestPostingUpToDate, isInterestTransfer,
                isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);

        Money interestPostedToDate = Money.zero(this.currency);

        boolean recalucateDailyBalanceDetails = false;

        for (final PostingPeriod interestPostingPeriod : postingPeriods) {

            final LocalDate interestPostingTransactionDate = interestPostingPeriod.dateOfPostingTransaction();
            final Money interestEarnedToBePostedForPeriod = interestPostingPeriod.getInterestEarned();

            if (!interestPostingTransactionDate.isAfter(interestPostingUpToDate)) {

                interestPostedToDate = interestPostedToDate.plus(interestEarnedToBePostedForPeriod);

                final SavingsAccountTransaction postingTransaction = findInterestPostingTransactionFor(interestPostingTransactionDate);
                if (postingTransaction == null) {
                    final SavingsAccountTransaction newPostingTransaction = SavingsAccountTransaction.interestPosting(this, office(),
                            interestPostingTransactionDate, interestEarnedToBePostedForPeriod);
                    this.transactions.add(newPostingTransaction);
                    recalucateDailyBalanceDetails = true;
                } else {
                    final boolean correctionRequired = postingTransaction.hasNotAmount(interestEarnedToBePostedForPeriod);
                    if (correctionRequired) {
                        postingTransaction.reverse();
                        final SavingsAccountTransaction newPostingTransaction = SavingsAccountTransaction.interestPosting(this, office(),
                                interestPostingTransactionDate, interestEarnedToBePostedForPeriod);
                        this.transactions.add(newPostingTransaction);
                        recalucateDailyBalanceDetails = true;
                    }
                }
            }
        }

        if (recalucateDailyBalanceDetails) {
            // no openingBalance concept supported yet but probably will to
            // allow
            // for migrations.
            final Money openingAccountBalance = Money.zero(this.currency);

            // update existing transactions so derived balance fields are
            // correct.
            recalculateDailyBalances(openingAccountBalance, interestPostingUpToDate);
        }

        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);
    }

    protected SavingsAccountTransaction findInterestPostingTransactionFor(final LocalDate postingDate) {

        SavingsAccountTransaction postingTransation = null;

        for (final SavingsAccountTransaction transaction : this.transactions) {
            if (transaction.isInterestPostingAndNotReversed() && transaction.occursOn(postingDate)) {
                postingTransation = transaction;
                break;
            }
        }

        return postingTransation;
    }

    // Determine the last transaction for given day
    protected SavingsAccountTransaction findLastTransaction(final LocalDate date) {

        SavingsAccountTransaction savingsTransaction = null;

        for (final SavingsAccountTransaction transaction : this.transactions) {
            if (transaction.isNotReversed() && transaction.occursOn(date)) {
                savingsTransaction = transaction;
                break;
            }
        }

        return savingsTransaction;
    }

    /**
     * All interest calculation based on END-OF-DAY-BALANCE.
     * 
     * Interest calculation is performed on-the-fly over all account
     * transactions.
     * 
     * 
     * 1. Calculate Interest From Beginning Of Account 1a. determine the
     * 'crediting' periods that exist for this savings acccount 1b. determine
     * the 'compounding' periods that exist within each 'crediting' period
     * calculate the amount of interest due at the end of each 'crediting'
     * period check if an existing 'interest posting' transaction exists for
     * date and matches the amount posted
     * 
     * @param isInterestTransfer
     *            TODO
     */
    public List<PostingPeriod> calculateInterestUsing(final MathContext mc, final LocalDate upToInterestCalculationDate,
            boolean isInterestTransfer, final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {

        // no openingBalance concept supported yet but probably will to allow
        // for migrations.
        final Money openingAccountBalance = Money.zero(this.currency);

        // update existing transactions so derived balance fields are
        // correct.
        recalculateDailyBalances(openingAccountBalance, upToInterestCalculationDate);

        // 1. default to calculate interest based on entire history OR
        // 2. determine latest 'posting period' and find interest credited to
        // that period

        // A generate list of EndOfDayBalances (not including interest postings)
        final SavingsPostingInterestPeriodType postingPeriodType = SavingsPostingInterestPeriodType.fromInt(this.interestPostingPeriodType);

        final SavingsCompoundingInterestPeriodType compoundingPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(this.interestCompoundingPeriodType);

        final SavingsInterestCalculationDaysInYearType daysInYearType = SavingsInterestCalculationDaysInYearType
                .fromInt(this.interestCalculationDaysInYearType);

        final List<LocalDateInterval> postingPeriodIntervals = this.savingsHelper.determineInterestPostingPeriods(
                getStartInterestCalculationDate(), upToInterestCalculationDate, postingPeriodType, financialYearBeginningMonth);

        final List<PostingPeriod> allPostingPeriods = new ArrayList<>();

        Money periodStartingBalance;
        if (this.startInterestCalculationDate != null) {
            LocalDate startInterestCalculationDate = new LocalDate(this.startInterestCalculationDate);
            final SavingsAccountTransaction transaction = findLastTransaction(startInterestCalculationDate);

            if (transaction == null) {
                final String defaultUserMessage = "No transactions were found on the specified date "
                        + getStartInterestCalculationDate().toString() + " for account number " + this.accountNumber.toString()
                        + " and resource id " + getId();

                final ApiParameterError error = ApiParameterError.parameterError(
                        "error.msg.savingsaccount.transaction.incorrect.start.interest.calculation.date", defaultUserMessage,
                        "transactionDate", getStartInterestCalculationDate().toString());

                final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                dataValidationErrors.add(error);

                throw new PlatformApiDataValidationException(dataValidationErrors);
            }

            periodStartingBalance = transaction.getRunningBalance(this.currency);
        } else
            periodStartingBalance = Money.zero(this.currency);

        final SavingsInterestCalculationType interestCalculationType = SavingsInterestCalculationType.fromInt(this.interestCalculationType);
        final BigDecimal interestRateAsFraction = getEffectiveInterestRateAsFraction(mc, upToInterestCalculationDate);
        final Collection<Long> interestPostTransactions = this.savingsHelper.fetchPostInterestTransactionIds(getId());
        final Money minBalanceForInterestCalculation = Money.of(getCurrency(), minBalanceForInterestCalculation());

        for (final LocalDateInterval periodInterval : postingPeriodIntervals) {

            final PostingPeriod postingPeriod = PostingPeriod.createFrom(periodInterval, periodStartingBalance,
                    retreiveOrderedNonInterestPostingTransactions(), this.currency, compoundingPeriodType, interestCalculationType,
                    interestRateAsFraction, daysInYearType.getValue(), upToInterestCalculationDate, interestPostTransactions,
                    isInterestTransfer, minBalanceForInterestCalculation, isSavingsInterestPostingAtCurrentPeriodEnd);

            periodStartingBalance = postingPeriod.closingBalance();

            allPostingPeriods.add(postingPeriod);
        }

        this.savingsHelper.calculateInterestForAllPostingPeriods(this.currency, allPostingPeriods, getLockedInUntilLocalDate(),
                isTransferInterestToOtherAccount());

        this.summary.updateFromInterestPeriodSummaries(this.currency, allPostingPeriods);
        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);

        return allPostingPeriods;
    }

    @SuppressWarnings("unused")
    protected BigDecimal getEffectiveInterestRateAsFraction(final MathContext mc, final LocalDate upToInterestCalculationDate) {
        return this.nominalAnnualInterestRate.divide(BigDecimal.valueOf(100l), mc);
    }

    protected List<SavingsAccountTransaction> retreiveOrderedNonInterestPostingTransactions() {
        final List<SavingsAccountTransaction> listOfTransactionsSorted = retreiveListOfTransactions();

        final List<SavingsAccountTransaction> orderedNonInterestPostingTransactions = new ArrayList<>();

        for (final SavingsAccountTransaction transaction : listOfTransactionsSorted) {
            if (!transaction.isInterestPostingAndNotReversed() && transaction.isNotReversed()) {
                orderedNonInterestPostingTransactions.add(transaction);
            }
        }

        return orderedNonInterestPostingTransactions;
    }

    protected List<SavingsAccountTransaction> retreiveListOfTransactions() {
        final List<SavingsAccountTransaction> listOfTransactionsSorted = new ArrayList<>();
        listOfTransactionsSorted.addAll(this.transactions);

        final SavingsAccountTransactionComparator transactionComparator = new SavingsAccountTransactionComparator();
        Collections.sort(listOfTransactionsSorted, transactionComparator);
        return listOfTransactionsSorted;
    }

    protected void recalculateDailyBalances(final Money openingAccountBalance, final LocalDate interestPostingUpToDate) {

        Money runningBalance = openingAccountBalance.copy();

        List<SavingsAccountTransaction> accountTransactionsSorted = retreiveListOfTransactions();
        boolean isTransactionsModified = false;
        for (final SavingsAccountTransaction transaction : accountTransactionsSorted) {
            if (transaction.isReversed()) {
                transaction.zeroBalanceFields();
            } else {
                Money overdraftAmount = Money.zero(this.currency);
                Money transactionAmount = Money.zero(this.currency);
                if (transaction.isCredit()) {
                    if (runningBalance.isLessThanZero()) {
                        Money diffAmount = transaction.getAmount(this.currency).plus(runningBalance);
                        if (diffAmount.isGreaterThanZero()) {
                            overdraftAmount = transaction.getAmount(this.currency).minus(diffAmount);
                        } else {
                            overdraftAmount = transaction.getAmount(this.currency);
                        }
                    }
                    transactionAmount = transactionAmount.plus(transaction.getAmount(this.currency));
                } else if (transaction.isDebit()) {
                    if (runningBalance.isLessThanZero()) {
                        overdraftAmount = transaction.getAmount(this.currency);
                    }
                    transactionAmount = transactionAmount.minus(transaction.getAmount(this.currency));
                }

                runningBalance = runningBalance.plus(transactionAmount);
                transaction.updateRunningBalance(runningBalance);
                if (overdraftAmount.isZero() && runningBalance.isLessThanZero()) {
                    overdraftAmount = overdraftAmount.plus(runningBalance.getAmount().negate());
                }
                if (transaction.getId() == null && overdraftAmount.isGreaterThanZero()) {
                    transaction.updateOverdraftAmount(overdraftAmount.getAmount());
                } else if (overdraftAmount.isNotEqualTo(transaction.getOverdraftAmount(getCurrency()))) {
                    SavingsAccountTransaction accountTransaction = SavingsAccountTransaction.copyTransaction(transaction);
                    transaction.reverse();
                    if (overdraftAmount.isGreaterThanZero()) {
                        accountTransaction.updateOverdraftAmount(overdraftAmount.getAmount());
                    }
                    accountTransaction.updateRunningBalance(runningBalance);
                    this.transactions.add(accountTransaction);
                    isTransactionsModified = true;
                }

            }
        }

        if (isTransactionsModified) {
            accountTransactionsSorted = retreiveListOfTransactions();
        }
        resetAccountTransactionsEndOfDayBalances(accountTransactionsSorted, interestPostingUpToDate);
    }

    protected void resetAccountTransactionsEndOfDayBalances(final List<SavingsAccountTransaction> accountTransactionsSorted,
            final LocalDate interestPostingUpToDate) {
        // loop over transactions in reverse
        LocalDate endOfBalanceDate = interestPostingUpToDate;
        for (int i = accountTransactionsSorted.size() - 1; i >= 0; i--) {
            final SavingsAccountTransaction transaction = accountTransactionsSorted.get(i);
            if (transaction.isNotReversed() && !transaction.isInterestPostingAndNotReversed()) {
                transaction.updateCumulativeBalanceAndDates(this.currency, endOfBalanceDate);
                // this transactions transaction date is end of balance date for
                // previous transaction.
                endOfBalanceDate = transaction.transactionLocalDate().minusDays(1);
            }
        }
    }

    public SavingsAccountTransaction deposit(final SavingsAccountTransactionDTO transactionDTO) {
        final String resourceTypeName = depositAccountType().resourceName();
        if (isNotActive()) {
            final String defaultUserMessage = "Transaction is not allowed. Account is not active.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg." + resourceTypeName
                    + ".transaction.account.is.not.active", defaultUserMessage, "transactionDate", transactionDTO.getTransactionDate()
                    .toString(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (isDateInTheFuture(transactionDTO.getTransactionDate())) {
            final String defaultUserMessage = "Transaction date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg." + resourceTypeName + ".transaction.in.the.future", defaultUserMessage, "transactionDate", transactionDTO
                            .getTransactionDate().toString(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (transactionDTO.getTransactionDate().isBefore(getActivationLocalDate())) {
            final Object[] defaultUserArgs = Arrays.asList(transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()),
                    getActivationLocalDate().toString(transactionDTO.getFormatter())).toArray();
            final String defaultUserMessage = "Transaction date cannot be before accounts activation date.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg." + resourceTypeName
                    + ".transaction.before.activation.date", defaultUserMessage, "transactionDate", defaultUserArgs);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_DEPOSIT, transactionDTO.getTransactionDate());

        final Money amount = Money.of(this.currency, transactionDTO.getTransactionAmount());

        final SavingsAccountTransaction transaction = SavingsAccountTransaction.deposit(this, office(), transactionDTO.getPaymentDetail(),
                transactionDTO.getTransactionDate(), amount, transactionDTO.getCreatedDate(), transactionDTO.getAppUser());
        this.transactions.add(transaction);

        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);

        return transaction;
    }

    public LocalDate getActivationLocalDate() {
        LocalDate activationLocalDate = null;
        if (this.activatedOnDate != null) {
            activationLocalDate = new LocalDate(this.activatedOnDate);
        }
        return activationLocalDate;
    }

    // startInterestCalculationDate is set during migration so that there is no
    // interference with interest posting of previous system
    public LocalDate getStartInterestCalculationDate() {
        LocalDate startInterestCalculationLocalDate = null;
        if (this.startInterestCalculationDate != null) {
            startInterestCalculationLocalDate = new LocalDate(this.startInterestCalculationDate);
        } else
            startInterestCalculationLocalDate = getActivationLocalDate();
        return startInterestCalculationLocalDate;
    }

    public SavingsAccountTransaction withdraw(final SavingsAccountTransactionDTO transactionDTO, final boolean applyWithdrawFee) {

        if (!isTransactionsAllowed()) {

            final String defaultUserMessage = "Transaction is not allowed. Account is not active.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.account.is.not.active",
                    defaultUserMessage, "transactionDate", transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (isDateInTheFuture(transactionDTO.getTransactionDate())) {
            final String defaultUserMessage = "Transaction date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.in.the.future",
                    defaultUserMessage, "transactionDate", transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (transactionDTO.getTransactionDate().isBefore(getActivationLocalDate())) {
            final Object[] defaultUserArgs = Arrays.asList(transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()),
                    getActivationLocalDate().toString(transactionDTO.getFormatter())).toArray();
            final String defaultUserMessage = "Transaction date cannot be before accounts activation date.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.before.activation.date",
                    defaultUserMessage, "transactionDate", defaultUserArgs);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (isAccountLocked(transactionDTO.getTransactionDate())) {
            final String defaultUserMessage = "Withdrawal is not allowed. No withdrawals are allowed until after "
                    + getLockedInUntilLocalDate().toString(transactionDTO.getFormatter());
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg.savingsaccount.transaction.withdrawals.blocked.during.lockin.period", defaultUserMessage, "transactionDate",
                    transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()),
                    getLockedInUntilLocalDate().toString(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_WITHDRAWAL, transactionDTO.getTransactionDate());

        final Money transactionAmountMoney = Money.of(this.currency, transactionDTO.getTransactionAmount());
        final SavingsAccountTransaction transaction = SavingsAccountTransaction.withdrawal(this, office(),
                transactionDTO.getPaymentDetail(), transactionDTO.getTransactionDate(), transactionAmountMoney,
                transactionDTO.getCreatedDate(), transactionDTO.getAppUser());
        this.transactions.add(transaction);

        if (applyWithdrawFee) {
            // auto pay withdrawal fee
            payWithdrawalFee(transactionDTO.getTransactionAmount(), transactionDTO.getTransactionDate(), transactionDTO.getAppUser());
        }
        return transaction;
    }

    private void payWithdrawalFee(final BigDecimal transactionAmoount, final LocalDate transactionDate, final AppUser user) {
        for (SavingsAccountCharge charge : this.charges()) {
            if (charge.isWithdrawalFee() && charge.isActive()) {
                charge.updateWithdralFeeAmount(transactionAmoount);
                this.payCharge(charge, charge.getAmountOutstanding(this.getCurrency()), transactionDate, user);
            }
        }
    }

    public boolean isBeforeLastPostingPeriod(final LocalDate transactionDate) {

        boolean transactionBeforeLastInterestPosting = false;

        for (final SavingsAccountTransaction transaction : retreiveListOfTransactions()) {
            if (transaction.isInterestPostingAndNotReversed() && transaction.isAfter(transactionDate)) {
                transactionBeforeLastInterestPosting = true;
                break;
            }
        }

        return transactionBeforeLastInterestPosting;
    }

    public void validateAccountBalanceDoesNotBecomeNegative(final BigDecimal transactionAmount, final boolean isException) {
        final List<SavingsAccountTransaction> transactionsSortedByDate = retreiveListOfTransactions();
        Money runningBalance = Money.zero(this.currency);
        Money minRequiredBalance = minRequiredBalanceDerived(getCurrency());
        for (final SavingsAccountTransaction transaction : transactionsSortedByDate) {
            if (transaction.isNotReversed() && transaction.isCredit()) {
                runningBalance = runningBalance.plus(transaction.getAmount(this.currency));
            } else if (transaction.isNotReversed() && transaction.isDebit()) {
                runningBalance = runningBalance.minus(transaction.getAmount(this.currency));
            } else {
                continue;
            }

            final BigDecimal withdrawalFee = null;
            // deal with potential minRequiredBalance and
            // enforceMinRequiredBalance
            if (!isException && transaction.canProcessBalanceCheck()) {
                if (runningBalance.minus(minRequiredBalance).isLessThanZero()) { throw new InsufficientAccountBalanceException(
                        "transactionAmount", getAccountBalance(), withdrawalFee, transactionAmount); }
            }

        }
    }

    public void validateAccountBalanceDoesNotBecomeNegative(final String transactionAction) {

        final List<SavingsAccountTransaction> transactionsSortedByDate = retreiveListOfTransactions();
        Money runningBalance = Money.zero(this.currency);
        Money minRequiredBalance = minRequiredBalanceDerived(getCurrency());
        for (final SavingsAccountTransaction transaction : transactionsSortedByDate) {
            if (transaction.isNotReversed() && transaction.isCredit()) {
                runningBalance = runningBalance.plus(transaction.getAmount(this.currency));
            } else if (transaction.isNotReversed() && transaction.isDebit()) {
                runningBalance = runningBalance.minus(transaction.getAmount(this.currency));
            }

            // enforceMinRequiredBalance
            if (transaction.canProcessBalanceCheck()) {
                if (runningBalance.minus(minRequiredBalance).isLessThanZero()) {
                    final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                    final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                            .resource(depositAccountType().resourceName() + transactionAction);
                    if (this.allowOverdraft) {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("results.in.balance.exceeding.overdraft.limit");
                    } else {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("results.in.balance.going.negative");
                    }
                    if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
                }

            }
        }
    }

    protected boolean isAccountLocked(final LocalDate transactionDate) {
        boolean isLocked = false;
        final boolean accountHasLockedInSetting = this.lockedInUntilDate != null;
        if (accountHasLockedInSetting) {
            isLocked = getLockedInUntilLocalDate().isAfter(transactionDate);
        }
        return isLocked;
    }

    protected LocalDate getLockedInUntilLocalDate() {
        LocalDate lockedInUntilLocalDate = null;
        if (this.lockedInUntilDate != null) {
            lockedInUntilLocalDate = new LocalDate(this.lockedInUntilDate);
        }
        return lockedInUntilLocalDate;
    }

    private boolean isDateInTheFuture(final LocalDate transactionDate) {
        return transactionDate.isAfter(DateUtils.getLocalDateOfTenant());
    }

    protected BigDecimal getAccountBalance() {
        return this.summary.getAccountBalance(this.currency).getAmount();
    }

    public void modifyApplication(final JsonCommand command, final Map<String, Object> actualChanges) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.modifyApplicationAction);
        this.modifyApplication(command, actualChanges, baseDataValidator);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public void modifyApplication(final JsonCommand command, final Map<String, Object> actualChanges,
            final DataValidatorBuilder baseDataValidator) {

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.hasStateOf(currentStatus)) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.in.submittedandpendingapproval.state");
            return;
        }

        final String localeAsInput = command.locale();
        final String dateFormat = command.dateFormat();

        if (command.isChangeInLocalDateParameterNamed(SavingsApiConstants.submittedOnDateParamName, getSubmittedOnLocalDate())) {
            final LocalDate newValue = command.localDateValueOfParameterNamed(SavingsApiConstants.submittedOnDateParamName);
            final String newValueAsString = command.stringValueOfParameterNamed(SavingsApiConstants.submittedOnDateParamName);
            actualChanges.put(SavingsApiConstants.submittedOnDateParamName, newValueAsString);
            actualChanges.put(SavingsApiConstants.localeParamName, localeAsInput);
            actualChanges.put(SavingsApiConstants.dateFormatParamName, dateFormat);
            this.submittedOnDate = newValue.toDate();
        }

        if (command.isChangeInStringParameterNamed(SavingsApiConstants.accountNoParamName, this.accountNumber)) {
            final String newValue = command.stringValueOfParameterNamed(SavingsApiConstants.accountNoParamName);
            actualChanges.put(SavingsApiConstants.accountNoParamName, newValue);
            this.accountNumber = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInStringParameterNamed(SavingsApiConstants.externalIdParamName, this.externalId)) {
            final String newValue = command.stringValueOfParameterNamed(SavingsApiConstants.externalIdParamName);
            actualChanges.put(SavingsApiConstants.externalIdParamName, newValue);
            this.externalId = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInLongParameterNamed(SavingsApiConstants.clientIdParamName, clientId())) {
            final Long newValue = command.longValueOfParameterNamed(SavingsApiConstants.clientIdParamName);
            actualChanges.put(SavingsApiConstants.clientIdParamName, newValue);
        }

        if (command.isChangeInLongParameterNamed(SavingsApiConstants.groupIdParamName, groupId())) {
            final Long newValue = command.longValueOfParameterNamed(SavingsApiConstants.groupIdParamName);
            actualChanges.put(SavingsApiConstants.groupIdParamName, newValue);
        }

        if (command.isChangeInLongParameterNamed(SavingsApiConstants.productIdParamName, this.product.getId())) {
            final Long newValue = command.longValueOfParameterNamed(SavingsApiConstants.productIdParamName);
            actualChanges.put(SavingsApiConstants.productIdParamName, newValue);
        }

        if (command.isChangeInLongParameterNamed(SavingsApiConstants.fieldOfficerIdParamName, hasSavingsOfficerId())) {
            final Long newValue = command.longValueOfParameterNamed(SavingsApiConstants.fieldOfficerIdParamName);
            actualChanges.put(SavingsApiConstants.fieldOfficerIdParamName, newValue);
        }

        if (command.isChangeInBigDecimalParameterNamed(SavingsApiConstants.nominalAnnualInterestRateParamName,
                this.nominalAnnualInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(SavingsApiConstants.nominalAnnualInterestRateParamName);
            actualChanges.put(SavingsApiConstants.nominalAnnualInterestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.nominalAnnualInterestRate = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(SavingsApiConstants.interestCompoundingPeriodTypeParamName,
                this.interestCompoundingPeriodType)) {
            final Integer newValue = command.integerValueOfParameterNamed(SavingsApiConstants.interestCompoundingPeriodTypeParamName);
            this.interestCompoundingPeriodType = newValue != null ? SavingsCompoundingInterestPeriodType.fromInt(newValue).getValue()
                    : newValue;
            actualChanges.put(SavingsApiConstants.interestCompoundingPeriodTypeParamName, this.interestCompoundingPeriodType);
        }

        if (command.isChangeInIntegerParameterNamed(SavingsApiConstants.interestPostingPeriodTypeParamName, this.interestPostingPeriodType)) {
            final Integer newValue = command.integerValueOfParameterNamed(SavingsApiConstants.interestPostingPeriodTypeParamName);
            this.interestPostingPeriodType = newValue != null ? SavingsPostingInterestPeriodType.fromInt(newValue).getValue() : newValue;
            actualChanges.put(SavingsApiConstants.interestPostingPeriodTypeParamName, this.interestPostingPeriodType);
        }

        if (command.isChangeInIntegerParameterNamed(SavingsApiConstants.interestCalculationTypeParamName, this.interestCalculationType)) {
            final Integer newValue = command.integerValueOfParameterNamed(SavingsApiConstants.interestCalculationTypeParamName);
            this.interestCalculationType = newValue != null ? SavingsInterestCalculationType.fromInt(newValue).getValue() : newValue;
            actualChanges.put(SavingsApiConstants.interestCalculationTypeParamName, this.interestCalculationType);
        }

        if (command.isChangeInIntegerParameterNamed(SavingsApiConstants.interestCalculationDaysInYearTypeParamName,
                this.interestCalculationDaysInYearType)) {
            final Integer newValue = command.integerValueOfParameterNamed(SavingsApiConstants.interestCalculationDaysInYearTypeParamName);
            this.interestCalculationDaysInYearType = newValue != null ? SavingsInterestCalculationDaysInYearType.fromInt(newValue)
                    .getValue() : newValue;
            actualChanges.put(SavingsApiConstants.interestCalculationDaysInYearTypeParamName, this.interestCalculationDaysInYearType);
        }

        if (command.isChangeInBigDecimalParameterNamedDefaultingZeroToNull(SavingsApiConstants.minRequiredOpeningBalanceParamName,
                this.minRequiredOpeningBalance)) {
            final BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamedDefaultToNullIfZero(SavingsApiConstants.minRequiredOpeningBalanceParamName);
            actualChanges.put(SavingsApiConstants.minRequiredOpeningBalanceParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.minRequiredOpeningBalance = Money.of(this.currency, newValue).getAmount();
        }

        if (command.isChangeInIntegerParameterNamedDefaultingZeroToNull(SavingsApiConstants.lockinPeriodFrequencyParamName,
                this.lockinPeriodFrequency)) {
            final Integer newValue = command
                    .integerValueOfParameterNamedDefaultToNullIfZero(SavingsApiConstants.lockinPeriodFrequencyParamName);
            actualChanges.put(SavingsApiConstants.lockinPeriodFrequencyParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.lockinPeriodFrequency = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(SavingsApiConstants.lockinPeriodFrequencyTypeParamName, this.lockinPeriodFrequencyType)) {
            final Integer newValue = command.integerValueOfParameterNamed(SavingsApiConstants.lockinPeriodFrequencyTypeParamName);
            actualChanges.put(SavingsApiConstants.lockinPeriodFrequencyTypeParamName, newValue);
            this.lockinPeriodFrequencyType = newValue != null ? SavingsPeriodFrequencyType.fromInt(newValue).getValue() : newValue;
        }

        // set period type to null if frequency is null
        if (this.lockinPeriodFrequency == null) {
            this.lockinPeriodFrequencyType = null;
        }

        if (command.isChangeInBooleanParameterNamed(withdrawalFeeForTransfersParamName, this.withdrawalFeeApplicableForTransfer)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(withdrawalFeeForTransfersParamName);
            actualChanges.put(withdrawalFeeForTransfersParamName, newValue);
            this.withdrawalFeeApplicableForTransfer = newValue;
        }

        // charges
        final String chargesParamName = "charges";
        if (command.hasParameter(chargesParamName)) {
            final JsonArray jsonArray = command.arrayOfParameterNamed(chargesParamName);
            if (jsonArray != null) {
                actualChanges.put(chargesParamName, command.jsonFragment(chargesParamName));
            }
        }

        if (command.isChangeInBooleanParameterNamed(allowOverdraftParamName, this.allowOverdraft)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(allowOverdraftParamName);
            actualChanges.put(allowOverdraftParamName, newValue);
            this.allowOverdraft = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamedDefaultingZeroToNull(overdraftLimitParamName, this.overdraftLimit)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero(overdraftLimitParamName);
            actualChanges.put(overdraftLimitParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.overdraftLimit = newValue;
        }

        if (!this.allowOverdraft) {
            this.overdraftLimit = null;
        }

        if (command.isChangeInBooleanParameterNamed(enforceMinRequiredBalanceParamName, this.enforceMinRequiredBalance)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(enforceMinRequiredBalanceParamName);
            actualChanges.put(enforceMinRequiredBalanceParamName, newValue);
            this.enforceMinRequiredBalance = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamedDefaultingZeroToNull(minRequiredBalanceParamName, this.minRequiredBalance)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero(minRequiredBalanceParamName);
            actualChanges.put(minRequiredBalanceParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.minRequiredBalance = newValue;
        }

        validateLockinDetails(baseDataValidator);
    }

    private void validateLockinDetails(final DataValidatorBuilder baseDataValidator) {

        /*
         * final List<ApiParameterError> dataValidationErrors = new
         * ArrayList<ApiParameterError>(); final DataValidatorBuilder
         * baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
         * .resource(resourceName);
         */

        if (this.lockinPeriodFrequency == null) {
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(this.lockinPeriodFrequencyType).ignoreIfNull()
                    .inMinMaxRange(0, 3);

            if (this.lockinPeriodFrequencyType != null) {
                baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(this.lockinPeriodFrequency).notNull()
                        .integerZeroOrGreater();
            }
        } else {
            baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(this.lockinPeriodFrequencyType)
                    .integerZeroOrGreater();
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(this.lockinPeriodFrequencyType).notNull()
                    .inMinMaxRange(0, 3);
        }
    }

    public Map<String, Object> deriveAccountingBridgeData(final CurrencyData currencyData, final Set<Long> existingTransactionIds,
            final Set<Long> existingReversedTransactionIds, boolean isAccountTransfer) {

        final Map<String, Object> accountingBridgeData = new LinkedHashMap<>();
        accountingBridgeData.put("savingsId", getId());
        accountingBridgeData.put("savingsProductId", productId());
        accountingBridgeData.put("currency", currencyData);
        accountingBridgeData.put("officeId", officeId());
        accountingBridgeData.put("cashBasedAccountingEnabled", isCashBasedAccountingEnabledOnSavingsProduct());
        accountingBridgeData.put("accrualBasedAccountingEnabled", isAccrualBasedAccountingEnabledOnSavingsProduct());
        accountingBridgeData.put("isAccountTransfer", isAccountTransfer);

        final List<Map<String, Object>> newSavingsTransactions = new ArrayList<>();
        for (final SavingsAccountTransaction transaction : this.transactions) {
            if (transaction.isReversed() && !existingReversedTransactionIds.contains(transaction.getId())) {
                newSavingsTransactions.add(transaction.toMapData(currencyData));
            } else if (!existingTransactionIds.contains(transaction.getId())) {
                newSavingsTransactions.add(transaction.toMapData(currencyData));
            }
        }

        accountingBridgeData.put("newSavingsTransactions", newSavingsTransactions);
        return accountingBridgeData;
    }

    public Collection<Long> findExistingTransactionIds() {

        final Collection<Long> ids = new ArrayList<>();

        for (final SavingsAccountTransaction transaction : this.transactions) {
            ids.add(transaction.getId());
        }

        return ids;
    }

    public Collection<Long> findExistingReversedTransactionIds() {

        final Collection<Long> ids = new ArrayList<>();

        for (final SavingsAccountTransaction transaction : this.transactions) {
            if (transaction.isReversed()) {
                ids.add(transaction.getId());
            }
        }

        return ids;
    }

    public void update(final Client client) {
        this.client = client;
    }

    public void update(final Group group) {
        this.group = group;
    }

    public void update(final SavingsProduct product) {
        this.product = product;
        this.minBalanceForInterestCalculation = product.minBalanceForInterestCalculation();
    }

    public void update(final Staff savingsOfficer) {
        this.savingsOfficer = savingsOfficer;
    }

    public void updateAccountNo(final String newAccountNo) {
        this.accountNumber = newAccountNo;
        this.accountNumberRequiresAutoGeneration = false;
    }

    public boolean isAccountNumberRequiresAutoGeneration() {
        return this.accountNumberRequiresAutoGeneration;
    }

    public Long productId() {
        return this.product.getId();
    }

    public SavingsProduct savingsProduct() {
        return this.product;
    }

    private Boolean isCashBasedAccountingEnabledOnSavingsProduct() {
        return this.product.isCashBasedAccountingEnabled();
    }

    private Boolean isAccrualBasedAccountingEnabledOnSavingsProduct() {
        return this.product.isAccrualBasedAccountingEnabled();
    }

    public Long officeId() {
        Long officeId = null;
        if (this.client != null) {
            officeId = this.client.officeId();
        } else {
            officeId = this.group.officeId();
        }
        return officeId;
    }

    public Office office() {
        Office office = null;
        if (this.client != null) {
            office = this.client.getOffice();
        } else {
            office = this.group.getOffice();
        }
        return office;
    }

    public Staff getSavingsOfficer() {
        return this.savingsOfficer;
    }

    public void unassignSavingsOfficer() {
        this.savingsOfficer = null;
    }

    public void assignSavingsOfficer(final Staff fieldOfficer) {
        this.savingsOfficer = fieldOfficer;
    }

    public Long clientId() {
        Long id = null;
        if (this.client != null) {
            id = this.client.getId();
        }
        return id;
    }

    public Long groupId() {
        Long id = null;
        if (this.group != null) {
            id = this.group.getId();
        }
        return id;
    }

    public Long hasSavingsOfficerId() {
        Long id = null;
        if (this.savingsOfficer != null) {
            id = this.savingsOfficer.getId();
        }
        return id;
    }

    public boolean hasSavingsOfficer(final Staff fromSavingsOfficer) {

        boolean matchesCurrentSavingsOfficer = false;
        if (this.savingsOfficer != null) {
            matchesCurrentSavingsOfficer = this.savingsOfficer.identifiedBy(fromSavingsOfficer);
        } else {
            matchesCurrentSavingsOfficer = fromSavingsOfficer == null;
        }
        return matchesCurrentSavingsOfficer;
    }

    public void reassignSavingsOfficer(final Staff newSavingsOfficer, final LocalDate assignmentDate) {
        final SavingsOfficerAssignmentHistory latestHistoryRecord = findLatestIncompleteHistoryRecord();
        final SavingsOfficerAssignmentHistory lastAssignmentRecord = findLastAssignmentHistoryRecord(newSavingsOfficer);

        // assignment date should not be less than savings account submitted
        // date
        if (isSubmittedOnDateAfter(assignmentDate)) {

            final String errorMessage = "The Savings Officer assignment date (" + assignmentDate.toString()
                    + ") cannot be before savings submitted date (" + getSubmittedOnDate().toString() + ").";

            throw new SavingsOfficerAssignmentDateException("cannot.be.before.savings.submitted.date", errorMessage, assignmentDate,
                    getSubmittedOnDate());

        } else if (lastAssignmentRecord != null && lastAssignmentRecord.isEndDateAfter(assignmentDate)) {

            final String errorMessage = "The Savings Officer assignment date (" + assignmentDate
                    + ") cannot be before previous Savings Officer unassigned date (" + lastAssignmentRecord.getEndDate() + ").";

            throw new SavingsOfficerAssignmentDateException("cannot.be.before.previous.unassignement.date", errorMessage, assignmentDate,
                    lastAssignmentRecord.getEndDate());

        } else if (DateUtils.getLocalDateOfTenant().isBefore(assignmentDate)) {

            final String errorMessage = "The Savings Officer assignment date (" + assignmentDate + ") cannot be in the future.";

            throw new SavingsOfficerAssignmentDateException("cannot.be.a.future.date", errorMessage, assignmentDate);

        } else if (latestHistoryRecord != null && this.savingsOfficer.identifiedBy(newSavingsOfficer)) {
            latestHistoryRecord.updateStartDate(assignmentDate);
        } else if (latestHistoryRecord != null && latestHistoryRecord.matchesStartDateOf(assignmentDate)) {
            latestHistoryRecord.updateSavingsOfficer(newSavingsOfficer);
            this.savingsOfficer = newSavingsOfficer;
        } else if (latestHistoryRecord != null && latestHistoryRecord.hasStartDateBefore(assignmentDate)) {
            final String errorMessage = "Savings account with identifier " + getId() + " was already assigned before date "
                    + assignmentDate;
            throw new SavingsOfficerAssignmentDateException("is.before.last.assignment.date", errorMessage, getId(), assignmentDate);
        } else {
            if (latestHistoryRecord != null) {
                // savings officer correctly changed from previous savings
                // officer to
                // new savings officer
                latestHistoryRecord.updateEndDate(assignmentDate);
            }
            this.savingsOfficer = newSavingsOfficer;
            if (isNotSubmittedAndPendingApproval()) {
                final SavingsOfficerAssignmentHistory savingsOfficerAssignmentHistory = SavingsOfficerAssignmentHistory.createNew(this,
                        this.savingsOfficer, assignmentDate);
                this.savingsOfficerHistory.add(savingsOfficerAssignmentHistory);
            }
        }
    }

    private SavingsOfficerAssignmentHistory findLastAssignmentHistoryRecord(final Staff newSavingsOfficer) {

        SavingsOfficerAssignmentHistory lastAssignmentRecordLatestEndDate = null;
        for (final SavingsOfficerAssignmentHistory historyRecord : this.savingsOfficerHistory) {

            if (historyRecord.isCurrentRecord() && !historyRecord.isSameSavingsOfficer(newSavingsOfficer)) {
                lastAssignmentRecordLatestEndDate = historyRecord;
                break;
            }

            if (lastAssignmentRecordLatestEndDate == null) {
                lastAssignmentRecordLatestEndDate = historyRecord;
            } else if (historyRecord.isEndDateAfter(lastAssignmentRecordLatestEndDate.getEndDate())
                    && !historyRecord.isSameSavingsOfficer(newSavingsOfficer)) {
                lastAssignmentRecordLatestEndDate = historyRecord;
            }
        }
        return lastAssignmentRecordLatestEndDate;
    }

    public boolean isSubmittedOnDateAfter(final LocalDate compareDate) {
        return this.submittedOnDate == null ? false : new LocalDate(this.submittedOnDate).isAfter(compareDate);
    }

    public LocalDate getSubmittedOnDate() {
        return (LocalDate) ObjectUtils.defaultIfNull(new LocalDate(this.submittedOnDate), null);
    }

    public void removeSavingsOfficer(final LocalDate unassignDate) {

        final SavingsOfficerAssignmentHistory latestHistoryRecord = findLatestIncompleteHistoryRecord();

        if (latestHistoryRecord != null) {
            validateUnassignDate(latestHistoryRecord, unassignDate);
            latestHistoryRecord.updateEndDate(unassignDate);
        }
        this.savingsOfficer = null;
    }

    private SavingsOfficerAssignmentHistory findLatestIncompleteHistoryRecord() {

        SavingsOfficerAssignmentHistory latestRecordWithNoEndDate = null;
        for (final SavingsOfficerAssignmentHistory historyRecord : this.savingsOfficerHistory) {
            if (historyRecord.isCurrentRecord()) {
                latestRecordWithNoEndDate = historyRecord;
                break;
            }
        }
        return latestRecordWithNoEndDate;
    }

    private void validateUnassignDate(final SavingsOfficerAssignmentHistory latestHistoryRecord, final LocalDate unassignDate) {

        final LocalDate today = DateUtils.getLocalDateOfTenant();

        if (latestHistoryRecord.getStartDate().isAfter(unassignDate)) {

            final String errorMessage = "The Savings officer Unassign date(" + unassignDate + ") cannot be before its assignment date ("
                    + latestHistoryRecord.getStartDate() + ").";

            throw new SavingsOfficerUnassignmentDateException("cannot.be.before.assignment.date", errorMessage, getId(),
                    getSavingsOfficer().getId(), latestHistoryRecord.getStartDate(), unassignDate);

        } else if (unassignDate.isAfter(today)) {

            final String errorMessage = "The Savings Officer Unassign date (" + unassignDate + ") cannot be in the future.";

            throw new SavingsOfficerUnassignmentDateException("cannot.be.a.future.date", errorMessage, unassignDate);
        }
    }

    public MonetaryCurrency getCurrency() {
        return this.currency;
    }

    public void validateNewApplicationState(final LocalDate todayDateOfTenant, final String resourceName) {

        // validateWithdrawalFeeDetails();
        // validateAnnualFeeDetails();

        final LocalDate submittedOn = getSubmittedOnLocalDate();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(resourceName
                + SavingsApiConstants.summitalAction);

        validateLockinDetails(baseDataValidator);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

        if (submittedOn.isAfter(todayDateOfTenant)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.submittedOnDateParamName).value(submittedOn)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.a.future.date");
        }

        if (this.client != null && this.client.isActivatedAfter(submittedOn)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.submittedOnDateParamName).value(this.client.getActivationLocalDate())
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.client.activation.date");
        } else if (this.group != null && this.group.isActivatedAfter(submittedOn)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.submittedOnDateParamName).value(this.group.getActivationLocalDate())
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.client.activation.date");
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    protected LocalDate getSubmittedOnLocalDate() {
        LocalDate submittedOn = null;
        if (this.submittedOnDate != null) {
            submittedOn = new LocalDate(this.submittedOnDate);
        }
        return submittedOn;
    }

    private LocalDate getApprovedOnLocalDate() {
        LocalDate approvedOnLocalDate = null;
        if (this.approvedOnDate != null) {
            approvedOnLocalDate = new LocalDate(this.approvedOnDate);
        }
        return approvedOnLocalDate;
    }

    public Client getClient() {
        return this.client;
    }

    public BigDecimal getNominalAnnualInterestRate() {
        return this.nominalAnnualInterestRate;
    }

    public Map<String, Object> approveApplication(final AppUser currentUser, final JsonCommand command, final LocalDate tenantsTodayDate) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.approvalAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.hasStateOf(currentStatus)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.approvedOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.submittedandpendingapproval.state");

            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        this.status = SavingsAccountStatusType.APPROVED.getValue();
        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(this.status));

        // only do below if status has changed in the 'approval' case
        final LocalDate approvedOn = command.localDateValueOfParameterNamed(SavingsApiConstants.approvedOnDateParamName);
        final String approvedOnDateChange = command.stringValueOfParameterNamed(SavingsApiConstants.approvedOnDateParamName);

        this.approvedOnDate = approvedOn.toDate();
        this.approvedBy = currentUser;
        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.approvedOnDateParamName, approvedOnDateChange);

        final LocalDate submittalDate = getSubmittedOnLocalDate();
        if (approvedOn.isBefore(submittalDate)) {

            final DateTimeFormatter formatter = DateTimeFormat.forPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String submittalDateAsString = formatter.print(submittalDate);

            baseDataValidator.reset().parameter(SavingsApiConstants.approvedOnDateParamName).value(submittalDateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.submittal.date");

            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (approvedOn.isAfter(tenantsTodayDate)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.approvedOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.a.future.date");

            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_APPLICATION_APPROVED, approvedOn);

        // FIXME - kw - support field officer history for savings accounts
        // if (this.fieldOfficer != null) {
        // final LoanOfficerAssignmentHistory loanOfficerAssignmentHistory =
        // LoanOfficerAssignmentHistory.createNew(this,
        // this.fieldOfficer, approvedOn);
        // this.loanOfficerHistory.add(loanOfficerAssignmentHistory);
        // }
        if (this.savingsOfficer != null) {
            final SavingsOfficerAssignmentHistory savingsOfficerAssignmentHistory = SavingsOfficerAssignmentHistory.createNew(this,
                    this.savingsOfficer, approvedOn);
            this.savingsOfficerHistory.add(savingsOfficerAssignmentHistory);
        }
        return actualChanges;
    }

    public Map<String, Object> undoApplicationApproval() {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.undoApprovalAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.APPROVED.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.approvedOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.approved.state");

            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        this.status = SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue();
        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(this.status));

        this.approvedOnDate = null;
        this.approvedBy = null;
        this.rejectedOnDate = null;
        this.rejectedBy = null;
        this.withdrawnOnDate = null;
        this.withdrawnBy = null;
        this.closedOnDate = null;
        this.closedBy = null;
        actualChanges.put(SavingsApiConstants.approvedOnDateParamName, "");

        // FIXME - kw - support field officer history for savings accounts
        // this.loanOfficerHistory.clear();

        return actualChanges;
    }

    public void undoTransaction(final Long transactionId) {

        SavingsAccountTransaction transactionToUndo = null;
        for (final SavingsAccountTransaction transaction : this.transactions) {
            if (transaction.isIdentifiedBy(transactionId)) {
                transactionToUndo = transaction;
            }
        }

        if (transactionToUndo == null) { throw new SavingsAccountTransactionNotFoundException(this.getId(), transactionId); }

        validateAttemptToUndoTransferRelatedTransactions(transactionToUndo);
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_UNDO_TRANSACTION, transactionToUndo.transactionLocalDate());
        transactionToUndo.reverse();
        if (transactionToUndo.isChargeTransaction() || transactionToUndo.isWaiveCharge()) {
            // undo charge
            final Set<SavingsAccountChargePaidBy> chargesPaidBy = transactionToUndo.getSavingsAccountChargesPaid();
            for (final SavingsAccountChargePaidBy savingsAccountChargePaidBy : chargesPaidBy) {
                final SavingsAccountCharge chargeToUndo = savingsAccountChargePaidBy.getSavingsAccountCharge();
                if (transactionToUndo.isChargeTransaction()) {
                    chargeToUndo.undoPayment(this.getCurrency(), transactionToUndo.getAmount(this.getCurrency()));
                } else if (transactionToUndo.isWaiveCharge()) {
                    chargeToUndo.undoWaiver(this.getCurrency(), transactionToUndo.getAmount(this.getCurrency()));
                }
            }
        }
    }

    private Date findLatestAnnualFeeTransactionDueDate() {

        Date nextDueDate = null;

        LocalDate lastAnnualFeeTransactionDate = null;
        for (final SavingsAccountTransaction transaction : retreiveOrderedNonInterestPostingTransactions()) {
            if (transaction.isAnnualFeeAndNotReversed()) {
                if (lastAnnualFeeTransactionDate == null) {
                    lastAnnualFeeTransactionDate = transaction.transactionLocalDate();
                    nextDueDate = lastAnnualFeeTransactionDate.toDate();
                }

                if (transaction.transactionLocalDate().isAfter(lastAnnualFeeTransactionDate)) {
                    lastAnnualFeeTransactionDate = transaction.transactionLocalDate();
                    nextDueDate = lastAnnualFeeTransactionDate.toDate();
                }
            }
        }

        return nextDueDate;
    }

    public Map<String, Object> rejectApplication(final AppUser currentUser, final JsonCommand command, final LocalDate tenantsTodayDate) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.rejectAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.rejectedOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.submittedandpendingapproval.state");

            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        this.status = SavingsAccountStatusType.REJECTED.getValue();
        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(this.status));

        final LocalDate rejectedOn = command.localDateValueOfParameterNamed(SavingsApiConstants.rejectedOnDateParamName);
        final String rejectedOnAsString = command.stringValueOfParameterNamed(SavingsApiConstants.rejectedOnDateParamName);

        this.rejectedOnDate = rejectedOn.toDate();
        this.rejectedBy = currentUser;
        this.withdrawnOnDate = null;
        this.withdrawnBy = null;
        this.closedOnDate = rejectedOn.toDate();
        this.closedBy = currentUser;

        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.rejectedOnDateParamName, rejectedOnAsString);
        actualChanges.put(SavingsApiConstants.closedOnDateParamName, rejectedOnAsString);

        final LocalDate submittalDate = getSubmittedOnLocalDate();

        if (rejectedOn.isBefore(submittalDate)) {

            final DateTimeFormatter formatter = DateTimeFormat.forPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String submittalDateAsString = formatter.print(submittalDate);

            baseDataValidator.reset().parameter(SavingsApiConstants.rejectedOnDateParamName).value(submittalDateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.submittal.date");

            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (rejectedOn.isAfter(tenantsTodayDate)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.rejectedOnDateParamName).value(rejectedOn)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.a.future.date");

            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_APPLICATION_REJECTED, rejectedOn);

        return actualChanges;
    }

    public Map<String, Object> applicantWithdrawsFromApplication(final AppUser currentUser, final JsonCommand command,
            final LocalDate tenantsTodayDate) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.withdrawnByApplicantAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.withdrawnOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.submittedandpendingapproval.state");

            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        this.status = SavingsAccountStatusType.WITHDRAWN_BY_APPLICANT.getValue();
        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(this.status));

        final LocalDate withdrawnOn = command.localDateValueOfParameterNamed(SavingsApiConstants.withdrawnOnDateParamName);
        final String withdrawnOnAsString = command.stringValueOfParameterNamed(SavingsApiConstants.withdrawnOnDateParamName);

        this.rejectedOnDate = null;
        this.rejectedBy = null;
        this.withdrawnOnDate = withdrawnOn.toDate();
        this.withdrawnBy = currentUser;
        this.closedOnDate = withdrawnOn.toDate();
        this.closedBy = currentUser;

        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.withdrawnOnDateParamName, withdrawnOnAsString);
        actualChanges.put(SavingsApiConstants.closedOnDateParamName, withdrawnOnAsString);

        final LocalDate submittalDate = getSubmittedOnLocalDate();
        if (withdrawnOn.isBefore(submittalDate)) {

            final DateTimeFormatter formatter = DateTimeFormat.forPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String submittalDateAsString = formatter.print(submittalDate);

            baseDataValidator.reset().parameter(SavingsApiConstants.withdrawnOnDateParamName).value(submittalDateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.submittal.date");

            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (withdrawnOn.isAfter(tenantsTodayDate)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.withdrawnOnDateParamName).value(withdrawnOn)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.a.future.date");

            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_APPLICATION_WITHDRAWAL_BY_CUSTOMER, withdrawnOn);

        return actualChanges;
    }

    public Map<String, Object> activate(final AppUser currentUser, final JsonCommand command, final LocalDate tenantsTodayDate) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(depositAccountType()
                .resourceName() + SavingsApiConstants.activateAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.APPROVED.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.approved.state");

            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        final LocalDate activationDate = command.localDateValueOfParameterNamed(SavingsApiConstants.activatedOnDateParamName);

        this.status = SavingsAccountStatusType.ACTIVE.getValue();
        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(this.status));
        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.activatedOnDateParamName, activationDate.toString(fmt));

        this.rejectedOnDate = null;
        this.rejectedBy = null;
        this.withdrawnOnDate = null;
        this.withdrawnBy = null;
        this.closedOnDate = null;
        this.closedBy = null;
        this.activatedOnDate = activationDate.toDate();
        this.activatedBy = currentUser;
        this.lockedInUntilDate = calculateDateAccountIsLockedUntil(getActivationLocalDate());

        /*
         * if (annualFeeSettingsSet()) {
         * updateToNextAnnualFeeDueDateFrom(getActivationLocalDate()); }
         */
        if (this.client != null && this.client.isActivatedAfter(activationDate)) {
            final DateTimeFormatter formatter = DateTimeFormat.forPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String dateAsString = formatter.print(this.client.getActivationLocalDate());
            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName).value(dateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.client.activation.date");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (this.group != null && this.group.isActivatedAfter(activationDate)) {
            final DateTimeFormatter formatter = DateTimeFormat.forPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String dateAsString = formatter.print(this.client.getActivationLocalDate());
            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName).value(dateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.group.activation.date");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        final LocalDate approvalDate = getApprovedOnLocalDate();
        if (activationDate.isBefore(approvalDate)) {

            final DateTimeFormatter formatter = DateTimeFormat.forPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String dateAsString = formatter.print(approvalDate);

            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName).value(dateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.approval.date");

            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (activationDate.isAfter(tenantsTodayDate)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName).value(activationDate)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.a.future.date");

            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_ACTIVATE, activationDate);

        return actualChanges;
    }

    public void processAccountUponActivation(final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth, final AppUser user) {

        // update annual fee due date
        for (SavingsAccountCharge charge : this.charges()) {
            charge.updateToNextDueDateFrom(getActivationLocalDate());
        }

        // auto pay the activation time charges
        this.payActivationCharges(isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, user);
        // TODO : AA add activation charges to actual changes list
    }

    public Money activateWithBalance() {
        return Money.of(this.currency, this.minRequiredOpeningBalance);
    }

    public void approveAndActivateApplication(final Date appliedonDate, final AppUser appliedBy) {
        this.status = SavingsAccountStatusType.ACTIVE.getValue();
        this.approvedOnDate = appliedonDate;
        this.approvedBy = appliedBy;
        this.rejectedOnDate = null;
        this.rejectedBy = null;
        this.withdrawnOnDate = null;
        this.withdrawnBy = null;
        this.closedOnDate = null;
        this.closedBy = null;
        this.activatedOnDate = appliedonDate;
        this.activatedBy = appliedBy;
        this.lockedInUntilDate = calculateDateAccountIsLockedUntil(getActivationLocalDate());
    }

    private void payActivationCharges(final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth,
            final AppUser user) {
        boolean isSavingsChargeApplied = false;
        for (SavingsAccountCharge savingsAccountCharge : this.charges()) {
            if (savingsAccountCharge.isSavingsActivation()) {
                isSavingsChargeApplied = true;
                payCharge(savingsAccountCharge, savingsAccountCharge.getAmountOutstanding(getCurrency()), getActivationLocalDate(), user);
            }
        }

        if (isSavingsChargeApplied) {
            final MathContext mc = MathContext.DECIMAL64;
            boolean isInterestTransfer = false;
            if (this.isBeforeLastPostingPeriod(getActivationLocalDate())) {
                final LocalDate today = DateUtils.getLocalDateOfTenant();
                this.postInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);
            } else {
                final LocalDate today = DateUtils.getLocalDateOfTenant();
                this.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                        financialYearBeginningMonth);
            }
        }
    }

    public Map<String, Object> close(final AppUser currentUser, final JsonCommand command, final LocalDate tenantsTodayDate) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.closeAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.in.active.state");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        final LocalDate closedDate = command.localDateValueOfParameterNamed(SavingsApiConstants.closedOnDateParamName);

        if (closedDate.isBefore(getActivationLocalDate())) {
            baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                    .failWithCode("must.be.after.activation.date");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }
        if (closedDate.isAfter(tenantsTodayDate)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                    .failWithCode("cannot.be.a.future.date");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }
        final List<SavingsAccountTransaction> savingsAccountTransactions = retreiveListOfTransactions();
        if (savingsAccountTransactions.size() > 0) {
            final SavingsAccountTransaction accountTransaction = savingsAccountTransactions.get(savingsAccountTransactions.size() - 1);
            if (accountTransaction.isAfter(closedDate)) {
                baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                        .failWithCode("must.be.after.last.transaction.date");
                if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
            }
        }
        if (getAccountBalance().doubleValue() != 0) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("results.in.balance.not.zero");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_CLOSE_ACCOUNT, closedDate);
        this.status = SavingsAccountStatusType.CLOSED.getValue();
        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(this.status));
        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.closedOnDateParamName, closedDate.toString(fmt));

        this.rejectedOnDate = null;
        this.rejectedBy = null;
        this.withdrawnOnDate = null;
        this.withdrawnBy = null;
        this.closedOnDate = closedDate.toDate();
        this.closedBy = currentUser;

        return actualChanges;
    }

    protected void validateActivityNotBeforeClientOrGroupTransferDate(final SavingsEvent event, final LocalDate activityDate) {
        if (this.client != null && this.client.getOfficeJoiningLocalDate() != null) {
            final LocalDate clientOfficeJoiningDate = this.client.getOfficeJoiningLocalDate();
            if (activityDate.isBefore(clientOfficeJoiningDate)) { throw new SavingsActivityPriorToClientTransferException(event.toString(),
                    clientOfficeJoiningDate); }
        }
    }

    private void validateAttemptToUndoTransferRelatedTransactions(final SavingsAccountTransaction savingsAccountTransaction) {
        if (savingsAccountTransaction.isTransferRelatedTransaction()) { throw new SavingsTransferTransactionsCannotBeUndoneException(
                savingsAccountTransaction.getId()); }
    }

    private Date calculateDateAccountIsLockedUntil(final LocalDate activationLocalDate) {

        Date lockedInUntilLocalDate = null;
        final PeriodFrequencyType lockinPeriodFrequencyType = PeriodFrequencyType.fromInt(this.lockinPeriodFrequencyType);
        switch (lockinPeriodFrequencyType) {
            case INVALID:
            break;
            case DAYS:
                lockedInUntilLocalDate = activationLocalDate.plusDays(this.lockinPeriodFrequency).toDate();
            break;
            case WEEKS:
                lockedInUntilLocalDate = activationLocalDate.plusWeeks(this.lockinPeriodFrequency).toDate();
            break;
            case MONTHS:
                lockedInUntilLocalDate = activationLocalDate.plusMonths(this.lockinPeriodFrequency).toDate();
            break;
            case YEARS:
                lockedInUntilLocalDate = activationLocalDate.plusYears(this.lockinPeriodFrequency).toDate();
            break;
        }

        return lockedInUntilLocalDate;
    }

    public Group group() {
        return this.group;
    }

    public boolean isWithdrawalFeeApplicableForTransfer() {
        return this.withdrawalFeeApplicableForTransfer;
    }

    public void activateAccountBasedOnBalance() {
        if (SavingsAccountStatusType.fromInt(this.status).isClosed() && !this.summary.getAccountBalance(getCurrency()).isZero()) {
            this.status = SavingsAccountStatusType.ACTIVE.getValue();
        }
    }

    public LocalDate getClosedOnDate() {
        return (LocalDate) ObjectUtils.defaultIfNull(new LocalDate(this.closedOnDate), null);
    }

    public SavingsAccountSummary getSummary() {
        return this.summary;
    }

    public List<SavingsAccountTransaction> getTransactions() {
        return this.transactions;
    }

    public void setStatus(final Integer status) {
        this.status = status;
    }

    private Set<SavingsAccountCharge> associateChargesWithThisSavingsAccount(final Set<SavingsAccountCharge> savingsAccountCharges) {
        for (final SavingsAccountCharge savingsAccountCharge : savingsAccountCharges) {
            savingsAccountCharge.update(this);
        }
        return savingsAccountCharges;
    }

    public boolean update(final Set<SavingsAccountCharge> newSavingsAccountCharges) {
        if (newSavingsAccountCharges == null) { return false; }

        if (this.charges == null) {
            this.charges = new HashSet<>();
        }
        this.charges.clear();
        this.charges.addAll(associateChargesWithThisSavingsAccount(newSavingsAccountCharges));
        return true;
    }

    public boolean hasCurrencyCodeOf(final String matchingCurrencyCode) {
        if (this.currency == null) { return false; }
        return this.currency.getCode().equalsIgnoreCase(matchingCurrencyCode);
    }

    public void removeCharge(final SavingsAccountCharge charge) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        if (isClosed()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("delete.transaction.invalid.account.is.closed");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (isActive() || isApproved()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("delete.transaction.invalid.account.is.active");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        this.charges.remove(charge);
    }

    public void waiveCharge(final Long savingsAccountChargeId, final AppUser user) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        if (isClosed()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.is.closed");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (isNotActive()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.is.not.active");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        final SavingsAccountCharge savingsAccountCharge = getCharge(savingsAccountChargeId);

        if (savingsAccountCharge.isNotActive()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("charge.is.not.active");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (savingsAccountCharge.isWithdrawalFee()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.waiver.of.withdrawal.fee.not.supported");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        // validate charge is not already paid or waived
        if (savingsAccountCharge.isWaived()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.charge.is.already.waived");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        } else if (savingsAccountCharge.isPaid()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.charge.is.paid");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        // waive charge
        final Money amountWaived = savingsAccountCharge.waive(getCurrency());
        handleWaiverChargeTransactions(savingsAccountCharge, amountWaived, user);

    }

    public void addCharge(final DateTimeFormatter formatter, final SavingsAccountCharge savingsAccountCharge, final Charge chargeDefinition) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        if (isClosed()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.is.closed");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (!hasCurrencyCodeOf(chargeDefinition.getCurrencyCode())) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                    "transaction.invalid.account.currency.and.charge.currency.not.same");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        final LocalDate chargeDueDate = savingsAccountCharge.getDueLocalDate();

        if (savingsAccountCharge.isOnSpecifiedDueDate()) {
            if (getActivationLocalDate() != null && chargeDueDate.isBefore(getActivationLocalDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(getActivationLocalDate().toString(formatter))
                        .failWithCodeNoParameterAddedToErrorCode("before.activationDate");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            } else if (getSubmittedOnLocalDate() != null && chargeDueDate.isBefore(getSubmittedOnLocalDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(getSubmittedOnLocalDate().toString(formatter))
                        .failWithCodeNoParameterAddedToErrorCode("before.submittedOnDate");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (savingsAccountCharge.isSavingsActivation() && !(isSubmittedAndPendingApproval() || (isApproved() && isNotActive()))) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.valid.account.status.cannot.add.activation.time.charge");
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        // Only one withdrawal fee is supported per account
        if (savingsAccountCharge.isWithdrawalFee()) {
            if (this.isWithDrawalFeeExists()) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("multiple.withdrawal.fee.per.account.not.supported");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        // Only one annual fee is supported per account
        if (savingsAccountCharge.isAnnualFee()) {
            if (this.isAnnualFeeExists()) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("multiple.annual.fee.per.account.not.supported");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }

        }

        if (savingsAccountCharge.isAnnualFee() || savingsAccountCharge.isMonthlyFee() || savingsAccountCharge.isWeeklyFee()) {
            // update due date
            if (isActive()) {
                savingsAccountCharge.updateToNextDueDateFrom(getActivationLocalDate());
            } else if (isApproved()) {
                savingsAccountCharge.updateToNextDueDateFrom(getApprovedOnLocalDate());
            }
        }

        // activation charge and withdrawal charges not required this validation
        if (savingsAccountCharge.isOnSpecifiedDueDate()) {
            validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_APPLY_CHARGE, chargeDueDate);
        }

        // add new charge to savings account
        charges().add(savingsAccountCharge);

    }

    private boolean isWithDrawalFeeExists() {
        for (SavingsAccountCharge charge : this.charges()) {
            if (charge.isWithdrawalFee()) return true;
        }
        return false;
    }

    private boolean isAnnualFeeExists() {
        for (SavingsAccountCharge charge : this.charges()) {
            if (charge.isAnnualFee()) return true;
        }
        return false;
    }

    public void payCharge(final SavingsAccountCharge savingsAccountCharge, final BigDecimal amountPaid, final LocalDate transactionDate,
            final DateTimeFormatter formatter, final AppUser user) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        if (isClosed()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.is.closed");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (isNotActive()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.is.not.active");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (savingsAccountCharge.isNotActive()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("charge.is.not.active");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (getActivationLocalDate() != null && transactionDate.isBefore(getActivationLocalDate())) {
            baseDataValidator.reset().parameter(dueAsOfDateParamName).value(getActivationLocalDate().toString(formatter))
                    .failWithCodeNoParameterAddedToErrorCode("transaction.before.activationDate");
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (DateUtils.isDateInTheFuture(transactionDate)) {
            baseDataValidator.reset().parameter(dueAsOfDateParamName).value(getSubmittedOnLocalDate().toString(formatter))
                    .failWithCodeNoParameterAddedToErrorCode("transaction.is.futureDate");
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (savingsAccountCharge.isSavingsActivation()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                    "transaction.not.valid.cannot.pay.activation.time.charge.is.automated");
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (savingsAccountCharge.isAnnualFee()) {
            final LocalDate annualFeeDueDate = savingsAccountCharge.getDueLocalDate();
            if (annualFeeDueDate == null) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("no.annualfee.settings");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }

            if (!annualFeeDueDate.equals(transactionDate)) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("invalid.date");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }

            Date currentAnnualFeeNextDueDate = findLatestAnnualFeeTransactionDueDate();
            if (currentAnnualFeeNextDueDate != null && new LocalDate(currentAnnualFeeNextDueDate).isEqual(transactionDate)) {
                baseDataValidator.reset().parameter("dueDate").value(transactionDate.toString(formatter))
                        .failWithCodeNoParameterAddedToErrorCode("transaction.exists.on.date");

                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        // validate charge is not already paid or waived
        if (savingsAccountCharge.isWaived()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.charge.is.already.waived");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        } else if (savingsAccountCharge.isPaid()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.charge.is.paid");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        final Money chargePaid = Money.of(currency, amountPaid);
        if (!savingsAccountCharge.getAmountOutstanding(getCurrency()).isGreaterThanOrEqualTo(chargePaid)) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.charge.amount.paid.in.access");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        this.payCharge(savingsAccountCharge, chargePaid, transactionDate, user);
    }

    public void payCharge(final SavingsAccountCharge savingsAccountCharge, final Money amountPaid, final LocalDate transactionDate,
            final AppUser user) {
        savingsAccountCharge.pay(getCurrency(), amountPaid);
        handlePayChargeTransactions(savingsAccountCharge, amountPaid, transactionDate, user);
    }

    private void handlePayChargeTransactions(SavingsAccountCharge savingsAccountCharge, Money transactionAmount,
            final LocalDate transactionDate, final AppUser user) {
        SavingsAccountTransaction chargeTransaction = null;

        if (savingsAccountCharge.isWithdrawalFee()) {
            chargeTransaction = SavingsAccountTransaction.withdrawalFee(this, office(), transactionDate, transactionAmount, user);
        } else if (savingsAccountCharge.isAnnualFee()) {
            chargeTransaction = SavingsAccountTransaction.annualFee(this, office(), transactionDate, transactionAmount, user);
        } else {
            chargeTransaction = SavingsAccountTransaction.charge(this, office(), transactionDate, transactionAmount, user);
        }

        handleChargeTransactions(savingsAccountCharge, chargeTransaction);
    }

    private void handleWaiverChargeTransactions(SavingsAccountCharge savingsAccountCharge, Money transactionAmount, AppUser user) {
        final SavingsAccountTransaction chargeTransaction = SavingsAccountTransaction.waiver(this, office(),
                DateUtils.getLocalDateOfTenant(), transactionAmount, user);
        handleChargeTransactions(savingsAccountCharge, chargeTransaction);
    }

    private void handleChargeTransactions(final SavingsAccountCharge savingsAccountCharge, final SavingsAccountTransaction transaction) {
        // Provide a link between transaction and savings charge for which
        // amount is waived.
        final SavingsAccountChargePaidBy chargePaidBy = SavingsAccountChargePaidBy.instance(transaction, savingsAccountCharge, transaction
                .getAmount(this.getCurrency()).getAmount());
        transaction.getSavingsAccountChargesPaid().add(chargePaidBy);
        this.getTransactions().add(transaction);
    }

    private SavingsAccountCharge getCharge(final Long savingsAccountChargeId) {
        SavingsAccountCharge charge = null;
        for (final SavingsAccountCharge existingCharge : this.charges) {
            if (existingCharge.getId().equals(savingsAccountChargeId)) {
                charge = existingCharge;
                break;
            }
        }

        if (charge == null) { throw new SavingsAccountChargeNotFoundException(savingsAccountChargeId, getId()); }

        return charge;
    }

    public Set<SavingsAccountCharge> charges() {
        return (this.charges == null) ? new HashSet<SavingsAccountCharge>() : this.charges;
    }

    public void validateAccountValuesWithProduct() {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        if (this.overdraftLimit != null && this.product.overdraftLimit() != null
                && this.overdraftLimit.compareTo(this.product.overdraftLimit()) == 1) {
            baseDataValidator.reset().parameter(SavingsApiConstants.overdraftLimitParamName).value(this.overdraftLimit)
                    .failWithCode("cannot.exceed.product.value");
        }

        validateInterestPostingAndCompoundingPeriodTypes(baseDataValidator);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public boolean allowOverdraft() {
        return this.allowOverdraft;
    }

    public LocalDate accountSubmittedOrActivationDate() {
        return getActivationLocalDate() == null ? getSubmittedOnLocalDate() : getActivationLocalDate();
    }

    public DepositAccountType depositAccountType() {
        return DepositAccountType.fromInt(depositType);
    }

    protected boolean isTransferInterestToOtherAccount() {
        return false;
    }

    public boolean accountSubmittedAndActivationOnSameDate() {
        if (getSubmittedOnLocalDate() == null || getActivationLocalDate() == null) { return false; }
        return getActivationLocalDate().isEqual(getSubmittedOnLocalDate());

    }

    public void validateInterestPostingAndCompoundingPeriodTypes(final DataValidatorBuilder baseDataValidator) {
        Map<SavingsPostingInterestPeriodType, List<SavingsCompoundingInterestPeriodType>> postingtoCompoundMap = new HashMap<>();
        postingtoCompoundMap.put(
                SavingsPostingInterestPeriodType.MONTHLY,
                Arrays.asList(new SavingsCompoundingInterestPeriodType[] { SavingsCompoundingInterestPeriodType.DAILY,
                        SavingsCompoundingInterestPeriodType.MONTHLY }));

        postingtoCompoundMap.put(
                SavingsPostingInterestPeriodType.QUATERLY,
                Arrays.asList(new SavingsCompoundingInterestPeriodType[] { SavingsCompoundingInterestPeriodType.DAILY,
                        SavingsCompoundingInterestPeriodType.MONTHLY, SavingsCompoundingInterestPeriodType.QUATERLY }));

        postingtoCompoundMap.put(
                SavingsPostingInterestPeriodType.BIANNUAL,
                Arrays.asList(new SavingsCompoundingInterestPeriodType[] { SavingsCompoundingInterestPeriodType.DAILY,
                        SavingsCompoundingInterestPeriodType.MONTHLY, SavingsCompoundingInterestPeriodType.QUATERLY,
                        SavingsCompoundingInterestPeriodType.BI_ANNUAL }));

        postingtoCompoundMap.put(
                SavingsPostingInterestPeriodType.ANNUAL,
                Arrays.asList(new SavingsCompoundingInterestPeriodType[] { SavingsCompoundingInterestPeriodType.DAILY,
                        SavingsCompoundingInterestPeriodType.MONTHLY, SavingsCompoundingInterestPeriodType.QUATERLY,
                        SavingsCompoundingInterestPeriodType.BI_ANNUAL, SavingsCompoundingInterestPeriodType.ANNUAL }));

        SavingsPostingInterestPeriodType savingsPostingInterestPeriodType = SavingsPostingInterestPeriodType
                .fromInt(interestPostingPeriodType);
        SavingsCompoundingInterestPeriodType savingsCompoundingInterestPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(interestCompoundingPeriodType);

        if (postingtoCompoundMap.get(savingsPostingInterestPeriodType) == null
                || !postingtoCompoundMap.get(savingsPostingInterestPeriodType).contains(savingsCompoundingInterestPeriodType)) {
            baseDataValidator.failWithCodeNoParameterAddedToErrorCode("posting.period.type.is.less.than.compound.period.type",
                    savingsPostingInterestPeriodType.name(), savingsCompoundingInterestPeriodType.name());

        }
    }

    public boolean allowDeposit() {
        return true;
    }

    public boolean allowWithdrawal() {
        return true;
    }

    public boolean allowModify() {
        return true;
    }

    public boolean isTransactionsAllowed() {
        return isActive();
    }

    public BigDecimal minBalanceForInterestCalculation() {
        return this.minBalanceForInterestCalculation;
    }

    public void inactivateCharge(SavingsAccountCharge savingsAccountCharge, LocalDate inactivationOnDate) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        if (isClosed()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.is.closed");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (isNotActive()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.is.not.active");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }
        savingsAccountCharge.inactiavateCharge(inactivationOnDate);
    }

    public SavingsAccountCharge getUpdatedChargeDetails(SavingsAccountCharge savingsAccountCharge) {
        for (final SavingsAccountCharge charge : this.charges) {
            if (charge.equals(savingsAccountCharge)) {
                savingsAccountCharge = charge;
                break;
            }
        }
        return savingsAccountCharge;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    private Money minRequiredBalanceDerived(final MonetaryCurrency currency) {
        Money minReqBalance = Money.of(currency, this.onHoldFunds);
        if (this.enforceMinRequiredBalance) {
            minReqBalance = minReqBalance.plus(this.minRequiredBalance);
        }
        if (this.allowOverdraft) {
            minReqBalance = minReqBalance.minus(this.overdraftLimit);
        }
        return minReqBalance;
    }

    public BigDecimal getOnHoldFunds() {
        return this.onHoldFunds == null ? BigDecimal.ZERO : this.onHoldFunds;
    }

    public void holdFunds(BigDecimal onHoldFunds) {
        this.onHoldFunds = getOnHoldFunds().add(onHoldFunds);
    }

    public void releaseFunds(BigDecimal onHoldFunds) {
        this.onHoldFunds = getOnHoldFunds().subtract(onHoldFunds);
    }

    public BigDecimal getWithdrawableBalance() {
        return getAccountBalance().subtract(minRequiredBalanceDerived(getCurrency()).getAmount());
    }
}