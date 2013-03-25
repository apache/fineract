/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.joda.time.LocalDate;
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
import org.springframework.data.jpa.domain.AbstractPersistable;

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

    @Column(name = "status_enum", nullable = false)
    private Integer status;

    @Temporal(TemporalType.DATE)
    @Column(name = "activation_date", nullable = true)
    private Date activationDate;

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "nominal_interest_rate_per_period", scale = 6, precision = 19, nullable = false)
    private BigDecimal interestRate;

    @Column(name = "nominal_interest_rate_period_frequency_enum", nullable = false)
    private Integer interestRatePeriodFrequencyType;

    @SuppressWarnings("unused")
    @Column(name = "annual_nominal_interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal annualInterestRate;

    @Column(name = "min_required_opening_balance", scale = 6, precision = 19, nullable = false)
    private BigDecimal minRequiredOpeningBalance;

    @Column(name = "lockin_period_frequency", nullable = false)
    private Integer lockinPeriodFrequency;

    @Column(name = "lockin_period_frequency_enum", nullable = false)
    private Integer lockinPeriodFrequencyType;

    /**
     * When account is becomes <code>active</code> this field is derived if
     * <code>lockinPeriodFrequency</code> and
     * <code>lockinPeriodFrequencyType</code> details are present.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "lockedin_until_date_derived", nullable = true)
    private Date lockedInUntilDate;

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
            final String accountNo, final String externalId, final boolean active, final LocalDate activationDate,
            final BigDecimal interestRate, final PeriodFrequencyType interestRatePeriodFrequencyType, final BigDecimal annualInterestRate,
            final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency,
            final PeriodFrequencyType lockinPeriodFrequencyType) {

        SavingsAccountStatusType status = SavingsAccountStatusType.UNACTIVATED;
        if (active) {
            status = SavingsAccountStatusType.ACTIVE;
        }

        return new SavingsAccount(client, group, product, accountNo, externalId, status, activationDate, interestRate,
                interestRatePeriodFrequencyType, annualInterestRate, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType);
    }

    private SavingsAccount(final Client client, final Group group, final SavingsProduct product, final String accountNo,
            final String externalId, final SavingsAccountStatusType status, final LocalDate activationDate, final BigDecimal interestRate,
            final PeriodFrequencyType interestRatePeriodFrequencyType, final BigDecimal annualInterestRate,
            final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency,
            final PeriodFrequencyType lockinPeriodFrequencyType) {
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
        this.interestRate = interestRate;
        this.interestRatePeriodFrequencyType = interestRatePeriodFrequencyType.getValue();
        this.annualInterestRate = annualInterestRate;
        this.minRequiredOpeningBalance = minRequiredOpeningBalance;
        this.lockinPeriodFrequency = lockinPeriodFrequency;
        if (lockinPeriodFrequencyType != null) {
            this.lockinPeriodFrequencyType = lockinPeriodFrequencyType.getValue();
        }
        this.summary = new SavingsAccountSummary();
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

        final Money amount = Money.of(this.currency, transactionAmount);

        if (isNotEnoughFundsToWithdraw(amount)) {
            //
            throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), transactionAmount);
        }

        final SavingsAccountTransaction transaction = SavingsAccountTransaction.withdrawal(this, transactionDate, amount);
        this.transactions.add(transaction);

        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);

        return transaction;
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

        if (command.isChangeInBigDecimalParameterNamed(SavingsApiConstants.interestRateParamName, this.interestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(SavingsApiConstants.interestRateParamName);
            actualChanges.put(SavingsApiConstants.interestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.interestRate = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(SavingsApiConstants.interestRatePeriodFrequencyTypeParamName,
                this.interestRatePeriodFrequencyType)) {
            final Integer newValue = command.integerValueOfParameterNamed(SavingsApiConstants.interestRatePeriodFrequencyTypeParamName);
            actualChanges.put(SavingsApiConstants.interestRatePeriodFrequencyTypeParamName, newValue);
            this.interestRatePeriodFrequencyType = PeriodFrequencyType.fromInt(newValue).getValue();
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