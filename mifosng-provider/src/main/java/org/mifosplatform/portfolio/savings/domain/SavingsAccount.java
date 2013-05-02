/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.annualFeeAmountParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.annualFeeOnMonthDayParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.localeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.withdrawalFeeAmountParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.withdrawalFeeTypeParamName;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.RandomPasswordGenerator;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savings.api.SavingsApiConstants;
import org.mifosplatform.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_savings_account", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_no" }, name = "sa_account_no_UNIQUE"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "sa_external_id_UNIQUE") })
public class SavingsAccount extends AbstractPersistable<Long> {

    private final static Logger logger = LoggerFactory.getLogger(SavingsAccount.class);

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

    @Column(name = "status_enum", nullable = false)
    private Integer status;

    @Temporal(TemporalType.DATE)
    @Column(name = "activation_date", nullable = true)
    private Date activationDate;

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
     * A value from the {@link SavingsInterestPostingPeriodType} enumeration.
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

    @Column(name = "withdrawal_fee_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal withdrawalFeeAmount;

    @Column(name = "withdrawal_fee_type_enum", nullable = true)
    private Integer withdrawalFeeType;

    @Column(name = "annual_fee_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal annualFeeAmount;

    @Column(name = "annual_fee_on_month", nullable = true)
    private Integer annualFeeOnMonth;

    @Column(name = "annual_fee_on_day", nullable = true)
    private Integer annualFeeOnDay;

    @Temporal(TemporalType.DATE)
    @Column(name = "annual_fee_next_due_date", nullable = true)
    private Date annualFeeNextDueDate;

    @Embedded
    private SavingsAccountSummary summary;

    @OrderBy(value = "dateOf, id")
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "savingsAccount", orphanRemoval = true)
    private final List<SavingsAccountTransaction> transactions = new ArrayList<SavingsAccountTransaction>();

    @Transient
    private boolean accountNumberRequiresAutoGeneration = false;
    @Transient
    private SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper;

    protected SavingsAccount() {
        //
    }

    public static SavingsAccount createNewAccount(final Client client, final Group group, final SavingsProduct product,
            final String accountNo, final String externalId, final BigDecimal interestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsInterestPostingPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final BigDecimal withdrawalFeeAmount, final SavingsWithdrawalFeesType withdrawalFeeType, final BigDecimal annualFeeAmount,
            final MonthDay annualFeeOnMonthDay) {

        final SavingsAccountStatusType status = SavingsAccountStatusType.UNACTIVATED;
        final LocalDate activationDate = null;
        return new SavingsAccount(client, group, product, accountNo, externalId, status, activationDate, interestRate,
                interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeAmount, withdrawalFeeType,
                annualFeeAmount, annualFeeOnMonthDay);
    }

    private SavingsAccount(final Client client, final Group group, final SavingsProduct product, final String accountNo,
            final String externalId, final SavingsAccountStatusType status, final LocalDate activationDate,
            final BigDecimal nominalAnnualInterestRate, final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsInterestPostingPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final BigDecimal withdrawalFeeAmount, final SavingsWithdrawalFeesType withdrawalFeeType, final BigDecimal annualFeeAmount,
            final MonthDay annualFeeOnMonthDay) {
        this.client = client;
        this.group = group;
        this.product = product;
        if (StringUtils.isBlank(accountNo)) {
            this.accountNumber = new RandomPasswordGenerator(19).generate();
            this.accountNumberRequiresAutoGeneration = true;
        } else {
            this.accountNumber = accountNo;
        }
        this.currency = product.currency();
        this.externalId = externalId;
        this.status = status.getValue();
        if (activationDate != null) {
            this.activationDate = activationDate.toDate();
        }
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
        this.withdrawalFeeAmount = withdrawalFeeAmount;
        if (withdrawalFeeType != null) {
            this.withdrawalFeeType = withdrawalFeeType.getValue();
        }

        this.annualFeeAmount = annualFeeAmount;
        if (annualFeeOnMonthDay != null) {
            this.annualFeeOnMonth = annualFeeOnMonthDay.getMonthOfYear();
            this.annualFeeOnDay = annualFeeOnMonthDay.getDayOfMonth();

            updateAnnualFeeNextDueDate();
        }

        this.summary = new SavingsAccountSummary();
    }

    private void updateAnnualFeeNextDueDate() {
        LocalDate nextDueLocalDate = new LocalDate().withMonthOfYear(this.annualFeeOnMonth).withDayOfMonth(this.annualFeeOnDay);
        if (nextDueLocalDate.isBefore(DateUtils.getLocalDateOfTenant())) {
            nextDueLocalDate = nextDueLocalDate.plusYears(1);
        }

        this.annualFeeNextDueDate = nextDueLocalDate.toDate();
    }

    /**
     * Used after fetching/hydrating a {@link SavingsAccount} object to inject
     * helper services/components used for update summary details after
     * events/transactions on a {@link SavingsAccount}.
     */
    public void setHelpers(final SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper) {
        this.savingsAccountTransactionSummaryWrapper = savingsAccountTransactionSummaryWrapper;
    }

    public boolean isNotActive() {
        return !isActive();
    }

    public boolean isActive() {
        return SavingsAccountStatusType.fromInt(this.status).isActive();
    }

    public void activate(final DateTimeFormatter formatter, final LocalDate activationDate) {

        if (isActive()) {
            final String defaultUserMessage = "Cannot activate account. Account is already active.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.account.already.active",
                    defaultUserMessage, "activationDate", activationDate.toString(formatter));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (isDateInTheFuture(activationDate)) {

            final String defaultUserMessage = "Activation date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.in.the.future",
                    defaultUserMessage, "activationDate", activationDate);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        this.activationDate = activationDate.toDate();
        this.status = SavingsAccountStatusType.ACTIVE.getValue();

        this.lockedInUntilDate = calculateDateAccountIsLockedUntil(getActivationLocalDate());

        // auto enter deposit for minimum required opening balance when
        // activating account.
        final Money minRequiredOpeningBalance = Money.of(this.currency, this.minRequiredOpeningBalance);
        if (minRequiredOpeningBalance.isGreaterThanZero()) {
            deposit(formatter, activationDate, minRequiredOpeningBalance.getAmount());
        }
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

    public void postInterest(final LocalDate interestPostingUpToDate) {

        final SavingsInterestPostingPeriodType postingPeriodType = SavingsInterestPostingPeriodType.fromInt(this.interestPostingPeriodType);
        final List<LocalDate> postingLocalDates = determineInterestPostingDates(getActivationLocalDate(), interestPostingUpToDate,
                postingPeriodType);

        final List<InterestCompoundingPeriodSummary> compoundingPeriods = calculateInterest(interestPostingUpToDate);

        Money interestPostedToDate = Money.zero(this.currency);

        boolean recalucateDailyBalanceDetails = false;
        for (LocalDate postingDate : postingLocalDates) {
            Money interestEarnedToBePostedForPeriod = findInterestEarnedToBePostedOn(interestPostedToDate, postingDate, compoundingPeriods);

            interestPostedToDate = interestPostedToDate.plus(interestEarnedToBePostedForPeriod);

            SavingsAccountTransaction postingTransaction = findInterestPostingTransactionFor(postingDate);
            if (postingTransaction == null) {
                final SavingsAccountTransaction newPostingTransaction = SavingsAccountTransaction.interestPosting(this, postingDate,
                        interestEarnedToBePostedForPeriod);
                this.transactions.add(newPostingTransaction);
                recalucateDailyBalanceDetails = true;
            } else {
                boolean correctionRequired = postingTransaction.hasNotAmount(interestEarnedToBePostedForPeriod);
                if (correctionRequired) {
                    postingTransaction.reverse();
                    final SavingsAccountTransaction newPostingTransaction = SavingsAccountTransaction.interestPosting(this, postingDate,
                            interestEarnedToBePostedForPeriod);
                    this.transactions.add(newPostingTransaction);
                    recalucateDailyBalanceDetails = true;
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

        for (SavingsAccountTransaction transaction : this.transactions) {
            if (transaction.isInterestPosting() && transaction.occursOn(postingDate)) {
                postingTransation = transaction;
                break;
            }
        }

        return postingTransation;
    }

    private Money findInterestEarnedToBePostedOn(final Money interestPostedToDate, final LocalDate postingDate,
            final List<InterestCompoundingPeriodSummary> compoundingPeriods) {

        Money interestToBePosted = Money.zero(this.currency);

        BigDecimal lastRelevantInterest = BigDecimal.ZERO;
        for (InterestCompoundingPeriodSummary interestCompoundingPeriodSummary : compoundingPeriods) {
            if (interestCompoundingPeriodSummary.fallsBefore(postingDate)) {
                interestToBePosted = Money.of(this.currency, interestCompoundingPeriodSummary.compoundedInterest());
                lastRelevantInterest = interestCompoundingPeriodSummary.compoundedInterest();
            }
        }
        logger.info("Interest is: " + lastRelevantInterest + " :>> ");

        return interestToBePosted.minus(interestPostedToDate);
    }

    private List<LocalDate> determineInterestPostingDates(final LocalDate activationLocalDate, final LocalDate interestPostingUpToDate,
            final SavingsInterestPostingPeriodType postingPeriodType) {

        List<LocalDate> postingDates = new ArrayList<LocalDate>();

        LocalDate periodStartDate = activationLocalDate;
        LocalDate periodEndDate = periodStartDate;

        while (!periodStartDate.isAfter(interestPostingUpToDate) && !periodEndDate.isAfter(interestPostingUpToDate)) {

            final LocalDate interestPostingLocalDate = determineInterestPostingPeriodEndDateFrom(periodStartDate, postingPeriodType);
            if (!interestPostingLocalDate.isAfter(DateUtils.getLocalDateOfTenant())) {
                postingDates.add(interestPostingLocalDate);
            }

            periodEndDate = interestPostingLocalDate;
            periodStartDate = interestPostingLocalDate;
        }

        return postingDates;
    }

    /**
     * All interest calculation based on END-OF-DAY-BALANCE.
     * 
     * Interest calculation is performed on-the-fly over all account
     * transactions.
     */
    public List<InterestCompoundingPeriodSummary> calculateInterest(final LocalDate interestCalculationUpToDate) {

        // no openingBalance concept supported yet but probably will to allow
        // for migrations.
        final Money openingAccountBalance = Money.zero(this.currency);

        // update existing transactions so derived balance fields are
        // correct.
        recalculateDailyBalances(openingAccountBalance);

        final SavingsInterestCalculationDaysInYearType daysInYearType = SavingsInterestCalculationDaysInYearType
                .fromInt(this.interestCalculationDaysInYearType);
        final SavingsCompoundingInterestPeriodType compoundingPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(this.interestCompoundingPeriodType);
        final List<LocalDateInterval> interestCompoundingPeriods = determineInterestCompoundingPeriods(getActivationLocalDate(),
                interestCalculationUpToDate, compoundingPeriodType);

        // determine opening balance, daily interest rate to apply, interest
        // due, interest to compound for each period
        final List<InterestCompoundingPeriodSummary> compoundingPeriods = determineInterestCompoundingPeriodSummaries(
                openingAccountBalance, this.nominalAnnualInterestRate, daysInYearType, interestCompoundingPeriods);

        this.summary.updateFromInterestPeriodSummaries(currency, compoundingPeriods);

        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);

        return compoundingPeriods;
    }

    private List<InterestCompoundingPeriodSummary> determineInterestCompoundingPeriodSummaries(final Money openingAccountBalance,
            final BigDecimal annualInterestRate, final SavingsInterestCalculationDaysInYearType daysInYearType,
            final List<LocalDateInterval> interestCompoundingPeriods) {

        List<InterestCompoundingPeriodSummary> summaries = new ArrayList<InterestCompoundingPeriodSummary>();

        final MathContext mc = new MathContext(10, RoundingMode.HALF_EVEN);
        final BigDecimal percentageDivisor = BigDecimal.valueOf(100l);
        final BigDecimal periodsInOneYear = determinePeriodsInOneYear(daysInYearType);
        final BigDecimal periodsInOneYearAsFraction = BigDecimal.ONE.divide(periodsInOneYear, mc);
        final BigDecimal annualInterestRateAsFraction = annualInterestRate.divide(percentageDivisor, mc);

        final SavingsInterestCalculationType interestCalculationType = SavingsInterestCalculationType.fromInt(this.interestCalculationType);

        final SavingsInterestCalculatorFactory interestCalculatorFactory = new SavingsInterestCalculatorFactory();
        final SavingsCompoundInterestCalculator interestCalculator = interestCalculatorFactory.createFrom(interestCalculationType,
                periodsInOneYearAsFraction, annualInterestRateAsFraction);

        BigDecimal periodOpeningBalance = openingAccountBalance.copy().getAmount();
        BigDecimal compoundedInterestToDate = BigDecimal.ZERO;
        for (LocalDateInterval periodInterval : interestCompoundingPeriods) {

            InterestCompoundingPeriodSummary compoundingSummary = calculateInterestCompoundingPeriodSummary(periodInterval,
                    periodOpeningBalance, compoundedInterestToDate, interestCalculator);

            periodOpeningBalance = compoundingSummary.closingBalance();
            compoundedInterestToDate = compoundingSummary.compoundedInterest();

            summaries.add(compoundingSummary);
        }

        return summaries;
    }

    private InterestCompoundingPeriodSummary calculateInterestCompoundingPeriodSummary(final LocalDateInterval periodInterval,
            final BigDecimal periodOpeningBalance, final BigDecimal compoundedInterestToDate,
            final SavingsCompoundInterestCalculator interestCalculator) {

        BigDecimal periodClosingBalance = periodOpeningBalance;
        List<SavingsAccountDailyBalance> dailyBalances = new ArrayList<SavingsAccountDailyBalance>();
        for (SavingsAccountTransaction transaction : this.transactions) {

            if (transaction.isAcceptableForDailyBalance(periodInterval)) {
                SavingsAccountDailyBalance dailyBalance = transaction.toCompoundedDailyBalance(periodInterval.endDate(),
                        compoundedInterestToDate);
                dailyBalances.add(dailyBalance);

                periodClosingBalance = dailyBalance.endOfDayBalance();
            }
        }

        if (dailyBalances.isEmpty()) {
            SavingsAccountDailyBalance dailyBalance = SavingsAccountDailyBalance.createFrom(periodInterval, periodOpeningBalance,
                    compoundedInterestToDate);
            dailyBalances.add(dailyBalance);
        }

        final BigDecimal totalInterestEarnedUnrounded = interestCalculator.calculate(dailyBalances,
                periodInterval.daysInPeriodInclusiveOfEndDate());

        final MathContext mc = new MathContext(10, RoundingMode.HALF_EVEN);
        final BigDecimal cumulativeCompoundedInterestToDate = compoundedInterestToDate.add(totalInterestEarnedUnrounded, mc);

        return InterestCompoundingPeriodSummary.create(periodInterval, periodOpeningBalance, periodClosingBalance,
                totalInterestEarnedUnrounded, cumulativeCompoundedInterestToDate);
    }

    private List<LocalDateInterval> determineInterestCompoundingPeriods(final LocalDate activationLocalDate,
            final LocalDate interestCalculationEndDate, final SavingsCompoundingInterestPeriodType interestPeriodType) {
        LocalDate periodStartDate = activationLocalDate;
        LocalDate periodEndDate = periodStartDate;

        final List<LocalDateInterval> interestPeriods = new ArrayList<LocalDateInterval>();

        while (!periodStartDate.isAfter(interestCalculationEndDate) && !periodEndDate.isAfter(interestCalculationEndDate)) {

            periodEndDate = determineInterestPeriodEndDateFrom(periodStartDate, interestPeriodType);
            if (periodEndDate.isAfter(interestCalculationEndDate)) {
                periodEndDate = interestCalculationEndDate;
            }
            interestPeriods.add(LocalDateInterval.create(periodStartDate, periodEndDate));

            // move periodStartDate forward to day after this period
            periodStartDate = periodEndDate.plusDays(1);
        }

        return interestPeriods;
    }

    private LocalDate determineInterestPostingPeriodEndDateFrom(final LocalDate periodStartDate,
            final SavingsInterestPostingPeriodType interestPostingPeriodType) {

        LocalDate periodEndDate = DateUtils.getLocalDateOfTenant();

        switch (interestPostingPeriodType) {
            case INVALID:
            break;
            case MONTHLY:
                // produce period end date on last day of current month
                periodEndDate = periodStartDate.dayOfMonth().withMaximumValue();
            break;
            case QUATERLY:
                // jan 1st to mar 31st, 1st apr to jun 30, jul 1st to sept 30,
                // oct 1st to dec 31
                int year = periodStartDate.getYearOfEra();
                int monthofYear = periodStartDate.getMonthOfYear();
                if (monthofYear <= 3) {
                    periodEndDate = new DateTime().withDate(year, 3, 31).toLocalDate();
                } else if (monthofYear <= 6) {
                    periodEndDate = new DateTime().withDate(year, 6, 30).toLocalDate();
                } else if (monthofYear <= 9) {
                    periodEndDate = new DateTime().withDate(year, 9, 30).toLocalDate();
                } else if (monthofYear <= 12) {
                    periodEndDate = new DateTime().withDate(year, 12, 31).toLocalDate();
                }
            break;
            case BI_ANNUAL:
                // jan 1st to 30, jul 1st to dec 31
                year = periodStartDate.getYearOfEra();
                monthofYear = periodStartDate.getMonthOfYear();
                if (monthofYear <= 6) {
                    periodEndDate = new DateTime().withDate(year, 6, 30).toLocalDate();
                } else if (monthofYear <= 12) {
                    periodEndDate = new DateTime().withDate(year, 12, 31).toLocalDate();
                }
            break;
            case ANNUAL:
                periodEndDate = periodStartDate.monthOfYear().withMaximumValue();
                periodEndDate = periodStartDate.dayOfMonth().withMaximumValue();
            break;
        }

        // interest posting always occurs on next day after the period end date.
        periodEndDate = periodEndDate.plusDays(1);

        return periodEndDate;
    }

    private LocalDate determineInterestPeriodEndDateFrom(final LocalDate periodStartDate,
            final SavingsCompoundingInterestPeriodType interestPeriodType) {

        LocalDate periodEndDate = DateUtils.getLocalDateOfTenant();

        switch (interestPeriodType) {
            case INVALID:
            break;
            case DAILY:
                periodEndDate = periodStartDate;
            break;
            case WEEKLY:
                periodEndDate = periodStartDate.dayOfWeek().withMaximumValue();
            break;
            case BIWEEKLY:
                final LocalDate closestEndOfWeek = periodStartDate.dayOfWeek().withMaximumValue();
                periodEndDate = closestEndOfWeek.plusWeeks(1);
            break;
            case MONTHLY:
                // produce period end date on last day of current month
                periodEndDate = periodStartDate.dayOfMonth().withMaximumValue();
            break;
            case QUATERLY:
                // jan 1st to mar 31st, 1st apr to jun 30, jul 1st to sept 30,
                // oct 1st to dec 31
                int year = periodStartDate.getYearOfEra();
                int monthofYear = periodStartDate.getMonthOfYear();
                if (monthofYear <= 3) {
                    periodEndDate = new DateTime().withDate(year, 3, 31).toLocalDate();
                } else if (monthofYear <= 6) {
                    periodEndDate = new DateTime().withDate(year, 6, 30).toLocalDate();
                } else if (monthofYear <= 9) {
                    periodEndDate = new DateTime().withDate(year, 9, 30).toLocalDate();
                } else if (monthofYear <= 12) {
                    periodEndDate = new DateTime().withDate(year, 12, 31).toLocalDate();
                }
            break;
            case BI_ANNUAL:
                // jan 1st to 30, jul 1st to dec 31
                year = periodStartDate.getYearOfEra();
                monthofYear = periodStartDate.getMonthOfYear();
                if (monthofYear <= 6) {
                    periodEndDate = new DateTime().withDate(year, 6, 30).toLocalDate();
                } else if (monthofYear <= 12) {
                    periodEndDate = new DateTime().withDate(year, 12, 31).toLocalDate();
                }
            break;
            case ANNUAL:
                periodEndDate = periodStartDate.monthOfYear().withMaximumValue();
                periodEndDate = periodStartDate.dayOfMonth().withMaximumValue();
            break;
            case NO_COMPOUNDING_SIMPLE_INTEREST:
                periodEndDate = periodStartDate.monthOfYear().withMaximumValue();
                periodEndDate = periodStartDate.dayOfMonth().withMaximumValue();
            break;
        }

        return periodEndDate;
    }

    private BigDecimal determinePeriodsInOneYear(final SavingsInterestCalculationDaysInYearType daysInYearType) {

        BigDecimal periodsInOneYear = BigDecimal.ZERO;

        switch (daysInYearType) {
            case DAYS_360:
                periodsInOneYear = BigDecimal.valueOf(360l);
            break;
            case DAYS_365:
                periodsInOneYear = BigDecimal.valueOf(365l);
            break;
            case INVALID:
            break;
        }

        return periodsInOneYear;
    }

    private List<SavingsAccountTransaction> retreiveListOfTransactions() {
        List<SavingsAccountTransaction> listOfTransactionsSorted = new ArrayList<SavingsAccountTransaction>();
        for (SavingsAccountTransaction transaction : this.transactions) {
            listOfTransactionsSorted.add(transaction);
        }

        SavingsAccountTransactionComparator transactionComparator = new SavingsAccountTransactionComparator();
        Collections.sort(listOfTransactionsSorted, transactionComparator);

        return listOfTransactionsSorted;
    }

    private void recalculateDailyBalances(final Money openingAccountBalance) {

        Money runningBalance = openingAccountBalance.copy();

        List<SavingsAccountTransaction> accountTransactionsSorted = retreiveListOfTransactions();

        for (SavingsAccountTransaction transaction : accountTransactionsSorted) {
            if (transaction.isReversed()) {
                transaction.zeroBalanceFields();
            } else {
                Money transactionAmount = Money.zero(this.currency);
                if (transaction.isDeposit()) {
                    transactionAmount = transactionAmount.plus(transaction.getAmount(this.currency));
                } else if (transaction.isWithdrawal()) {
                    transactionAmount = transactionAmount.minus(transaction.getAmount(this.currency));
                } else if (transaction.isInterestPosting()) {
                    transactionAmount = transactionAmount.plus(transaction.getAmount(this.currency));
                }

                runningBalance = runningBalance.plus(transactionAmount);
                transaction.updateRunningBalance(runningBalance);
            }
        }

        // loop over transactions in reverse
        LocalDate endOfBalanceDate = DateUtils.getLocalDateOfTenant();
        for (int i = accountTransactionsSorted.size() - 1; i >= 0; i--) {
            SavingsAccountTransaction transaction = accountTransactionsSorted.get(i);
            transaction.updateCumulativeBalanceAndDates(this.currency, endOfBalanceDate);

            // this transactions transaction date is end of balance date for
            // previous transaction.
            endOfBalanceDate = transaction.transactionLocalDate().minusDays(1);
        }
    }

    public SavingsAccountTransaction deposit(final DateTimeFormatter formatter, final LocalDate transactionDate,
            final BigDecimal transactionAmount) {

        if (isNotActive()) {
            final String defaultUserMessage = "Transaction is not allowed. Account is not active.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.account.is.not.active",
                    defaultUserMessage, "transactionDate", transactionDate.toString(formatter));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (isDateInTheFuture(transactionDate)) {
            final String defaultUserMessage = "Transaction date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.in.the.future",
                    defaultUserMessage, "transactionDate", transactionDate.toString(formatter));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (transactionDate.isBefore(getActivationLocalDate())) {
            final Object[] defaultUserArgs = Arrays.asList(transactionDate.toString(formatter),
                    getActivationLocalDate().toString(formatter)).toArray();
            final String defaultUserMessage = "Transaction date cannot be before accounts activation date.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.before.activation.date",
                    defaultUserMessage, "transactionDate", defaultUserArgs);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        final Money amount = Money.of(this.currency, transactionAmount);
        final SavingsAccountTransaction transaction = SavingsAccountTransaction.deposit(this, transactionDate, amount);
        this.transactions.add(transaction);

        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);

        final LocalDate today = DateUtils.getLocalDateOfTenant();
        calculateInterest(today);

        return transaction;
    }

    private LocalDate getActivationLocalDate() {
        LocalDate activationLocalDate = null;
        if (this.activationDate != null) {
            activationLocalDate = new LocalDate(this.activationDate);
        }
        return activationLocalDate;
    }

    public SavingsAccountTransaction withdraw(final DateTimeFormatter formatter, final LocalDate transactionDate,
            final BigDecimal transactionAmount) {

        if (isNotActive()) {

            final String defaultUserMessage = "Transaction is not allowed. Account is not active.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.account.is.not.active",
                    defaultUserMessage, "transactionDate", transactionDate.toString(formatter));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (isDateInTheFuture(transactionDate)) {
            final String defaultUserMessage = "Transaction date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.in.the.future",
                    defaultUserMessage, "transactionDate", transactionDate.toString(formatter));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (transactionDate.isBefore(getActivationLocalDate())) {
            final Object[] defaultUserArgs = Arrays.asList(transactionDate.toString(formatter),
                    getActivationLocalDate().toString(formatter)).toArray();
            final String defaultUserMessage = "Transaction date cannot be before accounts activation date.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.before.activation.date",
                    defaultUserMessage, "transactionDate", defaultUserArgs);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (isAccountLocked(transactionDate)) {
            final String defaultUserMessage = "Withdrawal is not allowed. No withdrawals are allowed until after "
                    + getLockedInUntilLocalDate().toString(formatter);
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg.savingsaccount.transaction.withdrawals.blocked.during.lockin.period", defaultUserMessage, "transactionDate",
                    transactionDate.toString(formatter), getLockedInUntilLocalDate().toString(formatter));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        final Money transactionAmountMoney = Money.of(this.currency, transactionAmount);

        if (isNotEnoughFundsToWithdraw(transactionAmountMoney)) {
            //
            throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), transactionAmount);
        }

        final SavingsAccountTransaction transaction = SavingsAccountTransaction.withdrawal(this, transactionDate, transactionAmountMoney);
        this.transactions.add(transaction);

        if (isAutomaticWithdrawalFee()) {

            SavingsAccountTransaction withdrawalFeeTransaction = null;
            Money feeAmount = null;
            switch (SavingsWithdrawalFeesType.fromInt(this.withdrawalFeeType)) {
                case INVALID:
                break;
                case FLAT:
                    feeAmount = Money.of(this.currency, this.withdrawalFeeAmount);
                    withdrawalFeeTransaction = SavingsAccountTransaction.fee(this, transactionDate, feeAmount);
                    this.transactions.add(withdrawalFeeTransaction);
                break;
                case PERCENT_OF_AMOUNT:
                    final BigDecimal feeAmountDecimal = transactionAmount.multiply(this.withdrawalFeeAmount).divide(
                            BigDecimal.valueOf(100l));
                    feeAmount = Money.of(this.currency, feeAmountDecimal);
                    withdrawalFeeTransaction = SavingsAccountTransaction.fee(this, transactionDate, feeAmount);
                    this.transactions.add(withdrawalFeeTransaction);
                break;
            }
        }

        final Money annualFee = Money.of(this.currency, this.annualFeeAmount);
        SavingsAccountTransaction annualFeeTransaction = SavingsAccountTransaction.annualFee(this, transactionDate, annualFee);
        this.transactions.add(annualFeeTransaction);

        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);

        final LocalDate today = DateUtils.getLocalDateOfTenant();
        calculateInterest(today);

        return transaction;
    }

    public SavingsAccountTransaction addAnnualFee(final DateTimeFormatter formatter, final LocalDate transactionDate) {

        if (isNotActive()) {

            final String defaultUserMessage = "Transaction is not allowed. Account is not active.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.account.is.not.active",
                    defaultUserMessage, "transactionDate", transactionDate.toString(formatter));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (isDateInTheFuture(transactionDate)) {
            final String defaultUserMessage = "Transaction date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.in.the.future",
                    defaultUserMessage, "transactionDate", transactionDate.toString(formatter));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (transactionDate.isBefore(getActivationLocalDate())) {
            final Object[] defaultUserArgs = Arrays.asList(transactionDate.toString(formatter),
                    getActivationLocalDate().toString(formatter)).toArray();
            final String defaultUserMessage = "Transaction date cannot be before accounts activation date.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.before.activation.date",
                    defaultUserMessage, "transactionDate", defaultUserArgs);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (isAccountLocked(transactionDate)) {
            final String defaultUserMessage = "Withdrawal is not allowed. No withdrawals are allowed until after "
                    + getLockedInUntilLocalDate().toString(formatter);
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg.savingsaccount.transaction.withdrawals.blocked.during.lockin.period", defaultUserMessage, "transactionDate",
                    transactionDate.toString(formatter), getLockedInUntilLocalDate().toString(formatter));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        final Money annualFee = Money.of(this.currency, this.annualFeeAmount);
        SavingsAccountTransaction annualFeeTransaction = SavingsAccountTransaction.annualFee(this, transactionDate, annualFee);
        this.transactions.add(annualFeeTransaction);

        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);

        final LocalDate today = DateUtils.getLocalDateOfTenant();
        calculateInterest(today);

        updateAnnualFeeNextDueDate();

        return annualFeeTransaction;
    }

    private boolean isAutomaticWithdrawalFee() {
        return this.withdrawalFeeType != null;
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

    private boolean isNotEnoughFundsToWithdraw(final Money amount) {
        return !isEnoughFundsToWithdraw(amount);
    }

    private boolean isEnoughFundsToWithdraw(final Money amount) {
        return this.summary.isLessThanOrEqualToAccountBalance(amount);
    }

    public void update(final JsonCommand command, final Map<String, Object> actualChanges) {

        final String localeAsInput = command.locale();

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

        if (command.isChangeInBigDecimalParameterNamed(SavingsApiConstants.nominalAnnualInterestRateParamName,
                this.nominalAnnualInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(SavingsApiConstants.nominalAnnualInterestRateParamName);
            actualChanges.put(SavingsApiConstants.nominalAnnualInterestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.nominalAnnualInterestRate = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(SavingsApiConstants.minRequiredOpeningBalanceParamName,
                this.minRequiredOpeningBalance)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(SavingsApiConstants.minRequiredOpeningBalanceParamName);
            actualChanges.put(SavingsApiConstants.minRequiredOpeningBalanceParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.minRequiredOpeningBalance = Money.of(this.currency, newValue).getAmount();
        }

        if (command.isChangeInIntegerParameterNamed(SavingsApiConstants.lockinPeriodFrequencyParamName, this.lockinPeriodFrequency)) {
            final Integer newValue = command.integerValueOfParameterNamed(SavingsApiConstants.lockinPeriodFrequencyParamName);
            actualChanges.put(SavingsApiConstants.lockinPeriodFrequencyParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.lockinPeriodFrequency = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(SavingsApiConstants.lockinPeriodFrequencyTypeParamName, this.lockinPeriodFrequencyType)) {
            final Integer newValue = command.integerValueOfParameterNamed(SavingsApiConstants.lockinPeriodFrequencyTypeParamName);
            actualChanges.put(SavingsApiConstants.lockinPeriodFrequencyTypeParamName, newValue);
            if (newValue != null) {
                this.lockinPeriodFrequencyType = PeriodFrequencyType.fromInt(newValue).getValue();
            } else {
                this.lockinPeriodFrequencyType = null;
            }
        }

        if (command.isChangeInBigDecimalParameterNamed(withdrawalFeeAmountParamName, this.withdrawalFeeAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(withdrawalFeeAmountParamName);
            actualChanges.put(withdrawalFeeAmountParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.withdrawalFeeAmount = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(withdrawalFeeTypeParamName, this.withdrawalFeeType)) {
            final Integer newValue = command.integerValueOfParameterNamed(withdrawalFeeTypeParamName);
            actualChanges.put(withdrawalFeeTypeParamName, newValue);
            this.withdrawalFeeType = SavingsWithdrawalFeesType.fromInt(newValue).getValue();
        }

        if (command.isChangeInBigDecimalParameterNamed(annualFeeAmountParamName, this.annualFeeAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(annualFeeAmountParamName);
            actualChanges.put(annualFeeAmountParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.annualFeeAmount = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(annualFeeOnMonthDayParamName, this.annualFeeOnDay)) {
            final MonthDay monthDay = command.extractMonthDayNamed(annualFeeOnMonthDayParamName);
            final String actualValueEntered = command.stringValueOfParameterNamed(annualFeeOnMonthDayParamName);
            final Integer newValue = monthDay.getDayOfMonth();
            actualChanges.put(annualFeeOnMonthDayParamName, actualValueEntered);
            actualChanges.put(localeParamName, localeAsInput);
            this.annualFeeOnDay = newValue;

            updateAnnualFeeNextDueDate();
        }

        if (command.isChangeInIntegerParameterNamed(annualFeeOnMonthDayParamName, this.annualFeeOnMonth)) {
            final MonthDay monthDay = command.extractMonthDayNamed(annualFeeOnMonthDayParamName);
            final String actualValueEntered = command.stringValueOfParameterNamed(annualFeeOnMonthDayParamName);
            final Integer newValue = monthDay.getMonthOfYear();
            actualChanges.put(annualFeeOnMonthDayParamName, actualValueEntered);
            actualChanges.put(localeParamName, localeAsInput);
            this.annualFeeOnMonth = newValue;

            updateAnnualFeeNextDueDate();
        }
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

    public void updateAccountNo(final String newAccountNo) {
        this.accountNumber = newAccountNo;
        this.accountNumberRequiresAutoGeneration = false;
    }

    public boolean isAccountNumberRequiresAutoGeneration() {
        return this.accountNumberRequiresAutoGeneration;
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
}
