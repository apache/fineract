/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.dueAsOfDateParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.withdrawalFeeForTransfersParamName;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

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
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savings.SavingsApiConstants;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.mifosplatform.portfolio.savings.domain.interest.PostingPeriod;
import org.mifosplatform.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.mifosplatform.portfolio.savings.exception.SavingsActivityPriorToClientTransferException;
import org.mifosplatform.portfolio.savings.exception.SavingsTransferTransactionsCannotBeUndoneException;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonArray;

@Entity
@Table(name = "m_savings_account", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_no" }, name = "sa_account_no_UNIQUE"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "sa_external_id_UNIQUE") })
public class SavingsAccount extends AbstractPersistable<Long> {

    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "external_id", nullable = true)
    private String externalId;

    @ManyToOne(optional = true)
    @JoinColumn(name = "client_id", nullable = true)
    private Client client;

    @ManyToOne(optional = true)
    @JoinColumn(name = "group_id", nullable = true)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private SavingsProduct product;

    @ManyToOne
    @JoinColumn(name = "field_officer_id", nullable = true)
    private Staff fieldOfficer;

    @Column(name = "status_enum", nullable = false)
    private Integer status;

    @Column(name = "account_type_enum", nullable = false)
    private Integer accountType;

    @Temporal(TemporalType.DATE)
    @Column(name = "submittedon_date", nullable = true)
    private Date submittedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "submittedon_userid", nullable = true)
    private AppUser submittedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "rejectedon_date")
    private Date rejectedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "rejectedon_userid", nullable = true)
    private AppUser rejectedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "withdrawnon_date")
    private Date withdrawnOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "withdrawnon_userid", nullable = true)
    private AppUser withdrawnBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "approvedon_date")
    private Date approvedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "approvedon_userid", nullable = true)
    private AppUser approvedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "activatedon_date", nullable = true)
    private Date activatedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "activatedon_userid", nullable = true)
    private AppUser activatedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "closedon_date")
    private Date closedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "closedon_userid", nullable = true)
    private AppUser closedBy;

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "nominal_annual_interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal nominalAnnualInterestRate;

    /**
     * The interest period is the span of time at the end of which savings in a
     * client's account earn interest.
     * 
     * A value from the {@link SavingsCompoundingInterestPeriodType}
     * enumeration.
     */
    @Column(name = "interest_compounding_period_enum", nullable = false)
    private Integer interestCompoundingPeriodType;

    /**
     * A value from the {@link SavingsPostingInterestPeriodType} enumeration.
     */
    @Column(name = "interest_posting_period_enum", nullable = false)
    private Integer interestPostingPeriodType;

    /**
     * A value from the {@link SavingsInterestCalculationType} enumeration.
     */
    @Column(name = "interest_calculation_type_enum", nullable = false)
    private Integer interestCalculationType;

    /**
     * A value from the {@link SavingsInterestCalculationDaysInYearType}
     * enumeration.
     */
    @Column(name = "interest_calculation_days_in_year_type_enum", nullable = false)
    private Integer interestCalculationDaysInYearType;

    @Column(name = "min_required_opening_balance", scale = 6, precision = 19, nullable = true)
    private BigDecimal minRequiredOpeningBalance;

    @Column(name = "lockin_period_frequency", nullable = true)
    private Integer lockinPeriodFrequency;

    @Column(name = "lockin_period_frequency_enum", nullable = true)
    private Integer lockinPeriodFrequencyType;

    /**
     * When account becomes <code>active</code> this field is derived if
     * <code>lockinPeriodFrequency</code> and
     * <code>lockinPeriodFrequencyType</code> details are present.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "lockedin_until_date_derived", nullable = true)
    private Date lockedInUntilDate;

    @Column(name = "withdrawal_fee_for_transfer", nullable = true)
    private boolean withdrawalFeeApplicableForTransfer;

    @Embedded
    private SavingsAccountSummary summary;

    @OrderBy(value = "dateOf, id")
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "savingsAccount", orphanRemoval = true)
    private final List<SavingsAccountTransaction> transactions = new ArrayList<SavingsAccountTransaction>();

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "savingsAccount", orphanRemoval = true)
    private Set<SavingsAccountCharge> charges = new HashSet<SavingsAccountCharge>();

    @Transient
    private boolean accountNumberRequiresAutoGeneration = false;
    @Transient
    private SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper;
    @Transient
    private SavingsHelper savingsHelper;

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
            final boolean withdrawalFeeApplicableForTransfer, final Set<SavingsAccountCharge> savingsAccountCharges) {

        final SavingsAccountStatusType status = SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL;
        return new SavingsAccount(client, group, product, fieldOfficer, accountNo, externalId, status, accountType, submittedOnDate, submittedBy,
                interestRate, interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType,
                interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType,
                withdrawalFeeApplicableForTransfer, savingsAccountCharges);
    }

    private SavingsAccount(final Client client, final Group group, final SavingsProduct product, final Staff fieldOfficer,
            final String accountNo, final String externalId, final SavingsAccountStatusType status, final AccountType accountType,
            final LocalDate submittedOnDate, final AppUser submittedBy, final BigDecimal nominalAnnualInterestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final Set<SavingsAccountCharge> savingsAccountCharges) {
        this.client = client;
        this.group = group;
        this.product = product;
        this.fieldOfficer = fieldOfficer;
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

    public boolean isClosed() {
        return SavingsAccountStatusType.fromInt(this.status).isClosed();
    }

    public void postInterest(final MathContext mc, final LocalDate interestPostingUpToDate) {

        final List<PostingPeriod> postingPeriods = calculateInterestUsing(mc, interestPostingUpToDate);

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
            recalculateDailyBalances(openingAccountBalance);
        }

        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);
    }

    private SavingsAccountTransaction findInterestPostingTransactionFor(final LocalDate postingDate) {

        SavingsAccountTransaction postingTransation = null;

        for (final SavingsAccountTransaction transaction : this.transactions) {
            if (transaction.isInterestPostingAndNotReversed() && transaction.occursOn(postingDate)) {
                postingTransation = transaction;
                break;
            }
        }

        return postingTransation;
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
     */
    public List<PostingPeriod> calculateInterestUsing(final MathContext mc, final LocalDate upToInterestCalculationDate) {

        // no openingBalance concept supported yet but probably will to allow
        // for migrations.
        final Money openingAccountBalance = Money.zero(this.currency);

        // update existing transactions so derived balance fields are
        // correct.
        recalculateDailyBalances(openingAccountBalance);

        // 1. default to calculate interest based on entire history OR
        // 2. determine latest 'posting period' and find interest credited to
        // that period

        // A generate list of EndOfDayBalances (not including interest postings)
        final SavingsPostingInterestPeriodType postingPeriodType = SavingsPostingInterestPeriodType.fromInt(this.interestPostingPeriodType);

        final SavingsCompoundingInterestPeriodType compoundingPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(this.interestCompoundingPeriodType);

        final SavingsInterestCalculationDaysInYearType daysInYearType = SavingsInterestCalculationDaysInYearType
                .fromInt(this.interestCalculationDaysInYearType);

        final List<LocalDateInterval> postingPeriodIntervals = this.savingsHelper.determineInterestPostingPeriods(getActivationLocalDate(),
                upToInterestCalculationDate, postingPeriodType);

        final List<PostingPeriod> allPostingPeriods = new ArrayList<PostingPeriod>();

        Money periodStartingBalance = Money.zero(this.currency);

        final SavingsInterestCalculationType interestCalculationType = SavingsInterestCalculationType.fromInt(this.interestCalculationType);
        final BigDecimal interestRateAsFraction = this.nominalAnnualInterestRate.divide(BigDecimal.valueOf(100l), mc);
        for (final LocalDateInterval periodInterval : postingPeriodIntervals) {

            final PostingPeriod postingPeriod = PostingPeriod.createFrom(periodInterval, periodStartingBalance,
                    retreiveOrderedListOfTransactions(), this.currency, compoundingPeriodType, interestCalculationType,
                    interestRateAsFraction, daysInYearType.getValue());

            periodStartingBalance = postingPeriod.closingBalance();

            allPostingPeriods.add(postingPeriod);
        }

        this.savingsHelper.calculateInterestForAllPostingPeriods(this.currency, allPostingPeriods);

        this.summary.updateFromInterestPeriodSummaries(this.currency, allPostingPeriods);
        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);

        return allPostingPeriods;
    }

    private List<SavingsAccountTransaction> retreiveOrderedListOfTransactions() {
        final List<SavingsAccountTransaction> listOfTransactionsSorted = retreiveListOfTransactions();

        final List<SavingsAccountTransaction> orderedNonInterestPostingTransactions = new ArrayList<SavingsAccountTransaction>();

        for (final SavingsAccountTransaction transaction : listOfTransactionsSorted) {
            if (!transaction.isInterestPostingAndNotReversed() && transaction.isNotReversed()) {
                orderedNonInterestPostingTransactions.add(transaction);
            }
        }

        return orderedNonInterestPostingTransactions;
    }

    private List<SavingsAccountTransaction> retreiveListOfTransactions() {
        final List<SavingsAccountTransaction> listOfTransactionsSorted = new ArrayList<SavingsAccountTransaction>();
        for (final SavingsAccountTransaction transaction : this.transactions) {
            listOfTransactionsSorted.add(transaction);
        }

        final SavingsAccountTransactionComparator transactionComparator = new SavingsAccountTransactionComparator();
        Collections.sort(listOfTransactionsSorted, transactionComparator);

        return listOfTransactionsSorted;
    }

    private void recalculateDailyBalances(final Money openingAccountBalance) {

        Money runningBalance = openingAccountBalance.copy();

        final List<SavingsAccountTransaction> accountTransactionsSorted = retreiveListOfTransactions();

        for (final SavingsAccountTransaction transaction : accountTransactionsSorted) {
            if (transaction.isReversed()) {
                transaction.zeroBalanceFields();
            } else {
                Money transactionAmount = Money.zero(this.currency);
                if (transaction.isCredit()) {
                    transactionAmount = transactionAmount.plus(transaction.getAmount(this.currency));
                } else if (transaction.isDebit()) {
                    transactionAmount = transactionAmount.minus(transaction.getAmount(this.currency));
                }

                runningBalance = runningBalance.plus(transactionAmount);
                transaction.updateRunningBalance(runningBalance);
            }
        }

        // loop over transactions in reverse
        LocalDate endOfBalanceDate = DateUtils.getLocalDateOfTenant();
        for (int i = accountTransactionsSorted.size() - 1; i >= 0; i--) {
            final SavingsAccountTransaction transaction = accountTransactionsSorted.get(i);
            if (transaction.isNotReversed()) {
                transaction.updateCumulativeBalanceAndDates(this.currency, endOfBalanceDate);

                // this transactions transaction date is end of balance date for
                // previous transaction.
                endOfBalanceDate = transaction.transactionLocalDate().minusDays(1);
            }
        }
    }

    public SavingsAccountTransaction deposit(final SavingsAccountTransactionDTO transactionDTO) {

        if (isNotActive()) {
            final String defaultUserMessage = "Transaction is not allowed. Account is not active.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.account.is.not.active",
                    defaultUserMessage, "transactionDate", transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (isDateInTheFuture(transactionDTO.getTransactionDate())) {
            final String defaultUserMessage = "Transaction date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.in.the.future",
                    defaultUserMessage, "transactionDate", transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (transactionDTO.getTransactionDate().isBefore(getActivationLocalDate())) {
            final Object[] defaultUserArgs = Arrays.asList(transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()),
                    getActivationLocalDate().toString(transactionDTO.getFormatter())).toArray();
            final String defaultUserMessage = "Transaction date cannot be before accounts activation date.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.before.activation.date",
                    defaultUserMessage, "transactionDate", defaultUserArgs);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_DEPOSIT, transactionDTO.getTransactionDate());

        final Money amount = Money.of(this.currency, transactionDTO.getTransactionAmount());

        final SavingsAccountTransaction transaction = SavingsAccountTransaction.deposit(this, office(), transactionDTO.getPaymentDetail(),
                transactionDTO.getTransactionDate(), amount);
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

    public SavingsAccountTransaction withdraw(final SavingsAccountTransactionDTO transactionDTO, final boolean applyWithdrawFee) {

        if (isNotActive()) {

            final String defaultUserMessage = "Transaction is not allowed. Account is not active.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.account.is.not.active",
                    defaultUserMessage, "transactionDate", transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (isDateInTheFuture(transactionDTO.getTransactionDate())) {
            final String defaultUserMessage = "Transaction date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.in.the.future",
                    defaultUserMessage, "transactionDate", transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (transactionDTO.getTransactionDate().isBefore(getActivationLocalDate())) {
            final Object[] defaultUserArgs = Arrays.asList(transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()),
                    getActivationLocalDate().toString(transactionDTO.getFormatter())).toArray();
            final String defaultUserMessage = "Transaction date cannot be before accounts activation date.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.before.activation.date",
                    defaultUserMessage, "transactionDate", defaultUserArgs);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
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

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_WITHDRAWAL, transactionDTO.getTransactionDate());

        final Money transactionAmountMoney = Money.of(this.currency, transactionDTO.getTransactionAmount());
        final SavingsAccountTransaction transaction = SavingsAccountTransaction.withdrawal(this, office(),
                transactionDTO.getPaymentDetail(), transactionDTO.getTransactionDate(), transactionAmountMoney);
        this.transactions.add(transaction);

        if (applyWithdrawFee){
            //auto pay withdrawal fee
            payWithdrawalFee(transactionDTO.getTransactionAmount(), transactionDTO.getTransactionDate());
        }
        return transaction;
    }
    
    private void payWithdrawalFee(final BigDecimal transactionAmoount, final LocalDate transactionDate){
        for (SavingsAccountCharge charge : this.charges()) {
            if (charge.isWithdrawalFee()){
                charge.updateWithdralFeeAmount(transactionAmoount);
                this.payCharge(charge, charge.getAmountOutstanding(this.getCurrency()), transactionDate);
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

    public void validateAccountBalanceDoesNotBecomeNegative(final BigDecimal transactionAmount) {
        final List<SavingsAccountTransaction> transactionsSortedByDate = retreiveListOfTransactions();
        Money runningBalance = Money.zero(this.currency);

        for (final SavingsAccountTransaction transaction : transactionsSortedByDate) {
            if (transaction.isNotReversed() && transaction.isCredit()) {
                runningBalance = runningBalance.plus(transaction.getAmount(this.currency));
            } else if (transaction.isNotReversed() && transaction.isDebit()) {
                runningBalance = runningBalance.minus(transaction.getAmount(this.currency));
            }

            if (runningBalance.isLessThanZero()) {
                //
                final BigDecimal withdrawalFee = null;
                throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), withdrawalFee, transactionAmount);
            }
        }
    }

    public void validateAccountBalanceDoesNotBecomeNegative(final String transactionAction) {

        final List<SavingsAccountTransaction> transactionsSortedByDate = retreiveListOfTransactions();
        Money runningBalance = Money.zero(this.currency);

        for (final SavingsAccountTransaction transaction : transactionsSortedByDate) {
            if (transaction.isNotReversed() && transaction.isCredit()) {
                runningBalance = runningBalance.plus(transaction.getAmount(this.currency));
            } else if (transaction.isNotReversed() && transaction.isDebit()) {
                runningBalance = runningBalance.minus(transaction.getAmount(this.currency));
            }

            if (runningBalance.isLessThanZero()) {
                //
                final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
                final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                        .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + transactionAction);

                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("results.in.balance.going.negative");

                if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
            }
        }
    }
    
    private boolean isAccountLocked(final LocalDate transactionDate) {
        boolean isLocked = false;
        final boolean accountHasLockedInSetting = this.lockedInUntilDate != null;
        if (accountHasLockedInSetting) {
            isLocked = getLockedInUntilLocalDate().isAfter(transactionDate);
        }
        return isLocked;
    }

    private LocalDate getLockedInUntilLocalDate() {
        LocalDate lockedInUntilLocalDate = null;
        if (this.lockedInUntilDate != null) {
            lockedInUntilLocalDate = new LocalDate(this.lockedInUntilDate);
        }
        return lockedInUntilLocalDate;
    }

    private boolean isDateInTheFuture(final LocalDate transactionDate) {
        return transactionDate.isAfter(DateUtils.getLocalDateOfTenant());
    }

    private BigDecimal getAccountBalance() {
        return this.summary.getAccountBalance(this.currency).getAmount();
    }

    public void modifyApplication(final JsonCommand command, final Map<String, Object> actualChanges) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.modifyApplicationAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.hasStateOf(currentStatus)) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.in.submittedandpendingapproval.state");

            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
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

        if (command.isChangeInLongParameterNamed(SavingsApiConstants.fieldOfficerIdParamName, fieldOfficerId())) {
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

        validateLockinDetails();
        
    }

    private void validateLockinDetails() {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

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

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public Map<String, Object> deriveAccountingBridgeData(final CurrencyData currencyData, final Set<Long> existingTransactionIds,
            final Set<Long> existingReversedTransactionIds) {

        final Map<String, Object> accountingBridgeData = new LinkedHashMap<String, Object>();
        accountingBridgeData.put("savingsId", getId());
        accountingBridgeData.put("savingsProductId", productId());
        accountingBridgeData.put("currency", currencyData);
        accountingBridgeData.put("officeId", officeId());
        accountingBridgeData.put("cashBasedAccountingEnabled", isCashBasedAccountingEnabledOnSavingsProduct());
        accountingBridgeData.put("accrualBasedAccountingEnabled", isAccrualBasedAccountingEnabledOnSavingsProduct());

        final List<Map<String, Object>> newLoanTransactions = new ArrayList<Map<String, Object>>();
        for (final SavingsAccountTransaction transaction : this.transactions) {
            if (transaction.isReversed() && !existingReversedTransactionIds.contains(transaction.getId())) {
                newLoanTransactions.add(transaction.toMapData(currencyData));
            } else if (!existingTransactionIds.contains(transaction.getId())) {
                newLoanTransactions.add(transaction.toMapData(currencyData));
            }
        }

        accountingBridgeData.put("newSavingsTransactions", newLoanTransactions);
        return accountingBridgeData;
    }

    public Collection<Long> findExistingTransactionIds() {

        final Collection<Long> ids = new ArrayList<Long>();

        for (final SavingsAccountTransaction transaction : this.transactions) {
            ids.add(transaction.getId());
        }

        return ids;
    }

    public Collection<Long> findExistingReversedTransactionIds() {

        final Collection<Long> ids = new ArrayList<Long>();

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
    }

    public void update(final Staff fieldOfficer) {
        this.fieldOfficer = fieldOfficer;
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

    public Long fieldOfficerId() {
        Long id = null;
        if (this.fieldOfficer != null) {
            id = this.fieldOfficer.getId();
        }
        return id;
    }

    public MonetaryCurrency getCurrency() {
        return this.currency;
    }

    public void validateNewApplicationState(final LocalDate todayDateOfTenant) {

        validateLockinDetails();
        //validateWithdrawalFeeDetails();
        //validateAnnualFeeDetails();

        final LocalDate submittedOn = getSubmittedOnLocalDate();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.summitalAction);

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

    private LocalDate getSubmittedOnLocalDate() {
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

    public Map<String, Object> approveApplication(final AppUser currentUser, final JsonCommand command, final LocalDate tenantsTodayDate) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
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

        return actualChanges;
    }

    public Map<String, Object> undoApplicationApproval() {
        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
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

        if (transactionToUndo == null) {
            // throw non found exception

        } else {
            validateAttemptToUndoTransferRelatedTransactions(transactionToUndo);
            validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_UNDO_TRANSACTION,
                    transactionToUndo.transactionLocalDate());
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
    }

    private Date findLatestAnnualFeeTransactionDueDate() {

        Date nextDueDate = null;

        LocalDate lastAnnualFeeTransactionDate = null;
        for (final SavingsAccountTransaction transaction : retreiveOrderedListOfTransactions()) {
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

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
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
        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
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

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.activateAction);

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

        /*if (annualFeeSettingsSet()) {
            updateToNextAnnualFeeDueDateFrom(getActivationLocalDate());
        }
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

        // auto enter deposit for minimum required opening balance when
        // activating account.
        final Money minRequiredOpeningBalance = Money.of(this.currency, this.minRequiredOpeningBalance);
        if (minRequiredOpeningBalance.isGreaterThanZero()) {
            final SavingsAccountTransactionDTO transactionDTO = new SavingsAccountTransactionDTO(fmt, activationDate,
                    minRequiredOpeningBalance.getAmount(), null);
            deposit(transactionDTO);

            // no openingBalance concept supported yet but probably will to
            // allow
            // for migrations.
            final Money openingAccountBalance = Money.zero(this.currency);

            // update existing transactions so derived balance fields are
            // correct.
            recalculateDailyBalances(openingAccountBalance);
        }
        
        
        //update annual fee due date
        for (SavingsAccountCharge charge : this.charges()) {
            charge.updateToNextDueDateFrom(getActivationLocalDate());
        }
        
        //auto pay the activation time charges
        this.payActivationCharges();
        //TODO : AA add activation charges to actual changes list
        
        return actualChanges;
    }
        
    private void payActivationCharges() {
        for (SavingsAccountCharge savingsAccountCharge : this.charges()) {
            if (savingsAccountCharge.isSavingsActivation()) {
                payCharge(savingsAccountCharge, savingsAccountCharge.getAmountOutstanding(getCurrency()), getActivationLocalDate());
            }
        }
    }

    public Map<String, Object> close(final AppUser currentUser, final JsonCommand command, final LocalDate tenantsTodayDate) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
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
        if (getAccountBalance().doubleValue() > 0) {
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

    private void validateActivityNotBeforeClientOrGroupTransferDate(final SavingsEvent event, final LocalDate activityDate) {
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
            this.charges = new HashSet<SavingsAccountCharge>();
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
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
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

    public void waiveCharge(final Long savingsAccountChargeId) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
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

        if (savingsAccountCharge.isWithdrawalFee()){
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
        handleWaiverChargeTransactions(savingsAccountCharge, amountWaived);
      
    }

    public void addCharge(final DateTimeFormatter formatter, final SavingsAccountCharge savingsAccountCharge, final Charge chargeDefinition) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
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
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                    "not.valid.account.status.cannot.add.activation.time.charge");
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

      //Only one withdrawal fee is supported per account
        if(savingsAccountCharge.isWithdrawalFee()){
            if(this.isWithDrawalFeeExists()){
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                        "multiple.withdrawal.fee.per.account.not.supported");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        
      //Only one annual fee is supported per account
        if(savingsAccountCharge.isAnnualFee()){
            if(this.isAnnualFeeExists()){
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                        "multiple.annual.fee.per.account.not.supported");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
            
        }
        
        if(savingsAccountCharge.isAnnualFee() || savingsAccountCharge.isMonthlyFee()){
            //update due date
            if(isActive()){
                savingsAccountCharge.updateToNextDueDateFrom(getActivationLocalDate());
            }else if(isApproved()){
                savingsAccountCharge.updateToNextDueDateFrom(getApprovedOnLocalDate());
            }          
        }
        
        //activation charge and withdrawal charges not required this validation
        if(savingsAccountCharge.isOnSpecifiedDueDate()){
            validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_APPLY_CHARGE, chargeDueDate);
        }
                
        // add new charge to savings account
        charges().add(savingsAccountCharge);

    }

    private boolean isWithDrawalFeeExists() {
        for (SavingsAccountCharge charge : this.charges()) {
            if(charge.isWithdrawalFee()) return true;
        }
        return false;
    }
    
    private boolean isAnnualFeeExists() {
        for (SavingsAccountCharge charge : this.charges()) {
            if(charge.isAnnualFee()) return true;
        }
        return false;
    }

    public void payCharge(final SavingsAccountCharge savingsAccountCharge, final BigDecimal amountPaid, final LocalDate transactionDate,
            final DateTimeFormatter formatter) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
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
        
        if (savingsAccountCharge.isAnnualFee()){
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

        this.payCharge(savingsAccountCharge, chargePaid, transactionDate);
    }

    public void payCharge(final SavingsAccountCharge savingsAccountCharge, final Money amountPaid, final LocalDate transactionDate) {
        savingsAccountCharge.pay(getCurrency(), amountPaid);
        handlePayChargeTransactions(savingsAccountCharge, amountPaid, transactionDate);
    }
    
    private void handlePayChargeTransactions(SavingsAccountCharge savingsAccountCharge, Money transactionAmount,
            final LocalDate transactionDate) {
        SavingsAccountTransaction chargeTransaction = null;
        
        if (savingsAccountCharge.isWithdrawalFee()){
            chargeTransaction = SavingsAccountTransaction.withdrawalFee(this, office(), transactionDate, transactionAmount);
        }else if (savingsAccountCharge.isAnnualFee()){
            chargeTransaction = SavingsAccountTransaction.annualFee(this, office(), transactionDate, transactionAmount);
        }else {
            chargeTransaction = SavingsAccountTransaction.charge(this, office(), transactionDate, transactionAmount);
        }
        
        handleChargeTransactions(savingsAccountCharge, chargeTransaction);
    }

    private void handleWaiverChargeTransactions(SavingsAccountCharge savingsAccountCharge, Money transactionAmount) {
        final SavingsAccountTransaction chargeTransaction = SavingsAccountTransaction.waiver(this, office(),
                DateUtils.getLocalDateOfTenant(), transactionAmount);
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
}