/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.savings.domain;

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.allowOverdraftParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.enforceMinRequiredBalanceParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lienAllowedParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.localeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.maxAllowedLienLimitParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.minOverdraftForInterestCalculationParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.minRequiredBalanceParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.overdraftLimitParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.withHoldTaxParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.withdrawalFeeForTransfersParamName;

import com.google.gson.JsonArray;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.configuration.service.TemporaryConfigurationServiceContainer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.security.service.RandomPasswordGenerator;
import org.apache.fineract.interoperation.domain.InteropIdentifier;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.charge.exception.SavingsAccountChargeNotFoundException;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.apache.fineract.portfolio.savings.domain.interest.SavingsAccountTransactionDetailsForPostingPeriod;
import org.apache.fineract.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountBlockedException;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountCreditsBlockedException;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountDebitsBlockedException;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountTransactionNotFoundException;
import org.apache.fineract.portfolio.savings.exception.SavingsActivityPriorToClientTransferException;
import org.apache.fineract.portfolio.savings.exception.SavingsOfficerAssignmentDateException;
import org.apache.fineract.portfolio.savings.exception.SavingsOfficerUnassignmentDateException;
import org.apache.fineract.portfolio.savings.exception.SavingsTransferTransactionsCannotBeUndoneException;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.apache.fineract.portfolio.tax.service.TaxUtils;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

@Entity
@Table(name = "m_savings_account", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_no" }, name = "sa_account_no_UNIQUE"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "sa_external_id_UNIQUE") })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "deposit_type_enum", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("100")
@SuppressWarnings({ "MemberName" })
@Getter
@Setter
public class SavingsAccount extends AbstractAuditableWithUTCDateTimeCustom<Long> implements IDepositAccountType {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsAccount.class);

    @Version
    int version;

    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    protected String accountNumber;

    @Column(name = "external_id", nullable = true)
    protected ExternalId externalId;

    @ManyToOne(optional = true)
    @JoinColumn(name = "client_id", nullable = true)
    protected Client client;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = true)
    protected Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gsim_id", nullable = true)
    private GroupSavingsIndividualMonitoring gsim;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    protected SavingsProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_officer_id", nullable = true)
    protected Staff savingsOfficer;

    @Column(name = "status_enum", nullable = false)
    protected Integer status;

    @Column(name = "sub_status_enum", nullable = false)
    protected Integer sub_status = 0;

    @Column(name = "account_type_enum", nullable = false)
    protected Integer accountType;

    @Column(name = "submittedon_date", nullable = true)
    protected LocalDate submittedOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "submittedon_userid", nullable = true)
    protected AppUser submittedBy;

    @Column(name = "rejectedon_date")
    protected LocalDate rejectedOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "rejectedon_userid", nullable = true)
    protected AppUser rejectedBy;

    @Column(name = "withdrawnon_date")
    protected LocalDate withdrawnOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "withdrawnon_userid", nullable = true)
    protected AppUser withdrawnBy;

    @Column(name = "approvedon_date")
    protected LocalDate approvedOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "approvedon_userid", nullable = true)
    protected AppUser approvedBy;

    @Column(name = "activatedon_date", nullable = true)
    protected LocalDate activatedOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "activatedon_userid", nullable = true)
    protected AppUser activatedBy;

    @Column(name = "closedon_date")
    protected LocalDate closedOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "closedon_userid", nullable = true)
    protected AppUser closedBy;

    @Column(name = "reason_for_block", nullable = true)
    protected String reasonForBlock;

    @Embedded
    protected MonetaryCurrency currency;

    @Column(name = "nominal_annual_interest_rate", scale = 6, precision = 19, nullable = false)
    protected BigDecimal nominalAnnualInterestRate;

    /**
     * The interest period is the span of time at the end of which savings in a client's account earn interest.
     *
     * A value from the {@link SavingsCompoundingInterestPeriodType} enumeration.
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
     * A value from the {@link SavingsInterestCalculationDaysInYearType} enumeration.
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
     * When account becomes <code>active</code> this field is derived if <code>lockinPeriodFrequency</code> and
     * <code>lockinPeriodFrequencyType</code> details are present.
     */
    @Column(name = "lockedin_until_date_derived", nullable = true)
    protected LocalDate lockedInUntilDate;

    @Column(name = "withdrawal_fee_for_transfer", nullable = true)
    protected boolean withdrawalFeeApplicableForTransfer;

    @Column(name = "allow_overdraft")
    private boolean allowOverdraft;

    @Column(name = "overdraft_limit", scale = 6, precision = 19, nullable = true)
    private BigDecimal overdraftLimit;

    @Column(name = "nominal_annual_interest_rate_overdraft", scale = 6, precision = 19, nullable = true)
    protected BigDecimal nominalAnnualInterestRateOverdraft;

    @Column(name = "min_overdraft_for_interest_calculation", scale = 6, precision = 19, nullable = true)
    private BigDecimal minOverdraftForInterestCalculation;

    @Column(name = "enforce_min_required_balance")
    private boolean enforceMinRequiredBalance;

    @Column(name = "min_required_balance", scale = 6, precision = 19, nullable = true)
    private BigDecimal minRequiredBalance;

    @Column(name = "is_lien_allowed", nullable = false)
    private boolean lienAllowed;

    @Column(name = "max_allowed_lien_limit", scale = 6, precision = 19, nullable = true)
    private BigDecimal maxAllowedLienLimit;

    @Column(name = "on_hold_funds_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal onHoldFunds;

    @Column(name = "start_interest_calculation_date")
    protected LocalDate startInterestCalculationDate;

    @Embedded
    protected SavingsAccountSummary summary;

    @OrderBy(value = "dateOf, createdDate, id")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "savingsAccount", orphanRemoval = true, fetch = FetchType.LAZY)
    protected List<SavingsAccountTransaction> transactions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "savingsAccount", orphanRemoval = true, fetch = FetchType.LAZY)
    protected Set<SavingsAccountCharge> charges = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "savingsAccount", orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<SavingsOfficerAssignmentHistory> savingsOfficerHistory = new HashSet<>();

    @Transient
    protected List<SavingsAccountTransaction> savingsAccountTransactions = new ArrayList<>();

    @Column(name = "deposit_type_enum", insertable = false, updatable = false)
    private Integer depositType;

    @Column(name = "min_balance_for_interest_calculation", scale = 6, precision = 19, nullable = true)
    private BigDecimal minBalanceForInterestCalculation;

    @Column(name = "withhold_tax", nullable = false)
    protected boolean withHoldTax;

    @ManyToOne
    @JoinColumn(name = "tax_group_id")
    private TaxGroup taxGroup;

    @Column(name = "accrued_till_date")
    private LocalDate accruedTillDate;

    @Column(name = "last_closed_business_date")
    private LocalDate lastClosedBusinessDate;

    @Column(name = "total_savings_amount_on_hold", scale = 6, precision = 19, nullable = true)
    private BigDecimal savingsOnHoldAmount;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "account", orphanRemoval = true, fetch = FetchType.LAZY)
    protected List<InteropIdentifier> identifiers = new ArrayList<>();

    protected SavingsAccount() {
        //
    }

    public static SavingsAccount createNewApplicationForSubmittal(final Client client, final Group group, final SavingsProduct product,
            final Staff fieldOfficer, final String accountNo, final ExternalId externalId, final AccountType accountType,
            final LocalDate submittedOnDate, final AppUser submittedBy, final BigDecimal interestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final Set<SavingsAccountCharge> savingsAccountCharges,
            final boolean allowOverdraft, final BigDecimal overdraftLimit, final boolean enforceMinRequiredBalance,
            final BigDecimal minRequiredBalance, final BigDecimal maxAllowedLienLimit, final boolean lienAllowed,
            final BigDecimal nominalAnnualInterestRateOverdraft, final BigDecimal minOverdraftForInterestCalculation,
            final boolean withHoldTax) {

        final SavingsAccountStatusType status = SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL;
        return new SavingsAccount(client, group, product, fieldOfficer, accountNo, externalId, status, accountType, submittedOnDate,
                submittedBy, interestRate, interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType,
                interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType,
                withdrawalFeeApplicableForTransfer, savingsAccountCharges, allowOverdraft, overdraftLimit, enforceMinRequiredBalance,
                minRequiredBalance, maxAllowedLienLimit, lienAllowed, nominalAnnualInterestRateOverdraft,
                minOverdraftForInterestCalculation, withHoldTax);
    }

    protected SavingsAccount(final Client client, final Group group, final SavingsProduct product, final Staff fieldOfficer,
            final String accountNo, final ExternalId externalId, final SavingsAccountStatusType status, final AccountType accountType,
            final LocalDate submittedOnDate, final AppUser submittedBy, final BigDecimal nominalAnnualInterestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final Set<SavingsAccountCharge> savingsAccountCharges,
            final boolean allowOverdraft, final BigDecimal overdraftLimit, boolean withHoldTax) {
        this(client, group, product, fieldOfficer, accountNo, externalId, status, accountType, submittedOnDate, submittedBy,
                nominalAnnualInterestRate, interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType,
                interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType,
                withdrawalFeeApplicableForTransfer, savingsAccountCharges, allowOverdraft, overdraftLimit, false, null, null, false, null,
                null, withHoldTax);
    }

    protected SavingsAccount(final Client client, final Group group, final SavingsProduct product, final Staff savingsOfficer,
            final String accountNo, final ExternalId externalId, final SavingsAccountStatusType status, final AccountType accountType,
            final LocalDate submittedOnDate, final AppUser submittedBy, final BigDecimal nominalAnnualInterestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final Set<SavingsAccountCharge> savingsAccountCharges,
            final boolean allowOverdraft, final BigDecimal overdraftLimit, final boolean enforceMinRequiredBalance,
            final BigDecimal minRequiredBalance, final BigDecimal maxAllowedLienLimit, final boolean lienAllowed,
            final BigDecimal nominalAnnualInterestRateOverdraft, final BigDecimal minOverdraftForInterestCalculation, boolean withHoldTax) {
        this.client = client;
        this.group = group;
        this.product = product;
        this.savingsOfficer = savingsOfficer;
        if (StringUtils.isBlank(accountNo)) {
            this.accountNumber = new RandomPasswordGenerator(19).generate();
        } else {
            this.accountNumber = accountNo;
        }

        this.currency = product.currency();
        this.externalId = externalId;
        this.status = status.getValue();
        this.accountType = accountType.getValue();
        this.submittedOnDate = submittedOnDate;
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
        this.nominalAnnualInterestRateOverdraft = nominalAnnualInterestRateOverdraft;
        this.minOverdraftForInterestCalculation = minOverdraftForInterestCalculation;
        esnureOverdraftLimitsSetForOverdraftAccounts();

        this.enforceMinRequiredBalance = enforceMinRequiredBalance;
        this.minRequiredBalance = minRequiredBalance;
        this.lienAllowed = lienAllowed;
        this.maxAllowedLienLimit = maxAllowedLienLimit;
        this.minBalanceForInterestCalculation = product.minBalanceForInterestCalculation();
        // this.savingsOfficerHistory = null;
        this.withHoldTax = withHoldTax;
        this.taxGroup = product.getTaxGroup();
    }

    public void setSavingsAccountTransactions(final List<SavingsAccountTransaction> savingsAccountTransactions) {
        this.savingsAccountTransactions.addAll(savingsAccountTransactions);
    }

    public List<SavingsAccountTransaction> getSavingsAccountTransactionsWithPivotConfig() {
        return this.savingsAccountTransactions;
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

    public List<SavingsAccountTransaction> findWithHoldTransactions() {
        final List<SavingsAccountTransaction> withholdTransactions = new ArrayList<>();
        List<SavingsAccountTransaction> trans = getTransactions();
        for (final SavingsAccountTransaction transaction : trans) {
            if (transaction.isWithHoldTaxAndNotReversed()) {
                withholdTransactions.add(transaction);
            }
        }
        return withholdTransactions;
    }

    public List<SavingsAccountTransaction> findWithHoldSavingsTransactionsWithPivotConfig() {
        final List<SavingsAccountTransaction> withholdTransactions = new ArrayList<>();
        List<SavingsAccountTransaction> trans = getSavingsAccountTransactionsWithPivotConfig();
        for (final SavingsAccountTransaction transaction : trans) {
            if (transaction.isWithHoldTaxAndNotReversed() && !transaction.isReversalTransaction()) {
                withholdTransactions.add(transaction);
            }
        }
        return withholdTransactions;
    }

    public boolean isWithHoldTaxApplicableForInterestPosting() {
        return this.withHoldTax() && this.depositAccountType() == DepositAccountType.SAVINGS_DEPOSIT;
    }

    public SavingsAccountTransaction findInterestPostingTransactionFor(final LocalDate postingDate) {
        SavingsAccountTransaction postingTransation = null;
        List<SavingsAccountTransaction> trans = getTransactions();
        for (final SavingsAccountTransaction transaction : trans) {
            if ((transaction.isInterestPostingAndNotReversed() || transaction.isOverdraftInterestAndNotReversed())
                    && transaction.occursOn(postingDate) && !transaction.isReversalTransaction()) {
                postingTransation = transaction;
                break;
            }
        }
        return postingTransation;
    }

    public SavingsAccountTransaction findInterestPostingSavingsTransactionWithPivotConfig(final LocalDate postingDate) {
        SavingsAccountTransaction postingTransation = null;
        List<SavingsAccountTransaction> trans = getSavingsAccountTransactionsWithPivotConfig();
        for (final SavingsAccountTransaction transaction : trans) {
            if ((transaction.isInterestPostingAndNotReversed() || transaction.isOverdraftInterestAndNotReversed())
                    && transaction.occursOn(postingDate) && !transaction.isReversalTransaction()) {
                postingTransation = transaction;
                break;
            }
        }
        return postingTransation;
    }

    public SavingsAccountTransaction findTransactionFor(final LocalDate postingDate, final List<SavingsAccountTransaction> transactions) {
        SavingsAccountTransaction transaction = null;
        for (final SavingsAccountTransaction savingsAccountTransaction : transactions) {
            if (savingsAccountTransaction.occursOn(postingDate)) {
                transaction = savingsAccountTransaction;
                break;
            }
        }
        return transaction;
    }

    public boolean createWithHoldTransaction(final BigDecimal amount, final LocalDate date, final boolean backdatedTxnsAllowedTill) {
        boolean isTaxAdded = false;
        if (this.taxGroup != null && amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            Map<TaxComponent, BigDecimal> taxSplit = TaxUtils.splitTax(amount, date, this.taxGroup.getTaxGroupMappings(), amount.scale());
            BigDecimal totalTax = TaxUtils.totalTaxAmount(taxSplit);
            if (totalTax.compareTo(BigDecimal.ZERO) > 0) {
                SavingsAccountTransaction withholdTransaction = SavingsAccountTransaction.withHoldTax(this, office(), date,
                        Money.of(currency, totalTax), taxSplit);
                if (backdatedTxnsAllowedTill) {
                    addTransactionToExisting(withholdTransaction);
                } else {
                    addTransaction(withholdTransaction);
                }
                isTaxAdded = true;
            }
        }
        return isTaxAdded;
    }

    protected boolean updateWithHoldTransaction(final BigDecimal amount, final SavingsAccountTransaction withholdTransaction) {
        boolean isTaxAdded = false;
        if (this.taxGroup != null && amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            Map<TaxComponent, BigDecimal> taxSplit = TaxUtils.splitTax(amount, withholdTransaction.getTransactionDate(),
                    this.taxGroup.getTaxGroupMappings(), amount.scale());
            BigDecimal totalTax = TaxUtils.totalTaxAmount(taxSplit);
            if (totalTax.compareTo(BigDecimal.ZERO) > 0) {
                if (withholdTransaction.getId() == null) {
                    withholdTransaction.setAmount(Money.of(currency, totalTax));
                    withholdTransaction.getTaxDetails().clear();
                    SavingsAccountTransaction.updateTaxDetails(taxSplit, withholdTransaction);
                    isTaxAdded = true;
                } else if (totalTax.compareTo(withholdTransaction.getAmount()) != 0) {
                    withholdTransaction.reverse();
                    SavingsAccountTransaction newWithholdTransaction = SavingsAccountTransaction.withHoldTax(this, office(),
                            withholdTransaction.getTransactionDate(), Money.of(currency, totalTax), taxSplit);
                    addTransaction(newWithholdTransaction);
                    isTaxAdded = true;
                }
            }
        }
        return isTaxAdded;
    }

    // Determine the last transaction for given day
    protected SavingsAccountTransaction findLastTransaction(final LocalDate date) {

        SavingsAccountTransaction savingsTransaction = null;
        List<SavingsAccountTransaction> trans = getTransactions();
        for (final SavingsAccountTransaction transaction : trans) {
            if (transaction.isNotReversed() && !transaction.isReversalTransaction() && transaction.occursOn(date)) {
                savingsTransaction = transaction;
                break;
            }
        }

        return savingsTransaction;
    }

    protected SavingsAccountTransaction findLastFilteredTransactionWithPivotConfig(final LocalDate date) {
        SavingsAccountTransaction savingsTransaction = null;
        List<SavingsAccountTransaction> trans = getSavingsAccountTransactionsWithPivotConfig();
        for (final SavingsAccountTransaction transaction : trans) {
            if (transaction.isNotReversed() && !transaction.isReversalTransaction() && transaction.occursOn(date)) {
                savingsTransaction = transaction;
                break;
            }
        }
        return savingsTransaction;
    }

    public List<LocalDate> getManualPostingDates() {
        List<LocalDate> transactions = new ArrayList<>();
        for (SavingsAccountTransaction trans : this.transactions) {
            if (trans.isInterestPosting() && trans.isNotReversed() && !trans.isReversalTransaction() && trans.isManualTransaction()) {
                transactions.add(trans.getTransactionDate());
            }
        }
        return transactions;
    }

    public List<LocalDate> getManualPostingDatesWithPivotConfig() {
        List<LocalDate> transactions = new ArrayList<>();
        for (SavingsAccountTransaction trans : this.savingsAccountTransactions) {
            if (trans.isInterestPosting() && trans.isNotReversed() && trans.isManualTransaction()) {
                transactions.add(trans.getTransactionDate());
            }
        }
        return transactions;
    }

    /**
     * Resolves the effective "up to" date for interest posting/calculation given a posting date. For a regular savings
     * account this is the posting date itself; deposit account subtypes may cap it at the maturity date (see
     * {@code RecurringDepositAccount}). Extracted from the former {@code postInterest} overloads so the
     * {@code SavingsAccountPostInterestService} can resolve the date polymorphically.
     */
    public LocalDate interestPostingUpToDate(final LocalDate postingDate) {
        return postingDate;
    }

    /**
     * All interest calculation based on END-OF-DAY-BALANCE.
     *
     * Interest calculation is performed on-the-fly over all account transactions.
     *
     *
     * 1. Calculate Interest From Beginning Of Account 1a. determine the 'crediting' periods that exist for this savings
     * acccount 1b. determine the 'compounding' periods that exist within each 'crediting' period calculate the amount
     * of interest due at the end of each 'crediting' period check if an existing 'interest posting' transaction exists
     * for date and matches the amount posted
     *
     * @param isInterestTransfer
     *            TODO
     */

    public List<PostingPeriod> calculateInterestUsing(final MathContext mc, final LocalDate upToInterestCalculationDate,
            boolean isInterestTransfer, final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth,
            final LocalDate postInterestOnDate, final boolean backdatedTxnsAllowedTill, final boolean postReversals,
            final Collection<Long> interestPostTransactions) {

        // no openingBalance concept supported yet but probably will to allow for
        // migrations.
        // Check global configurations and 'pivot' date is null
        Money openingAccountBalance = backdatedTxnsAllowedTill ? Money.of(this.currency, this.summary.getRunningBalanceOnPivotDate())
                : Money.zero(this.currency);

        // update existing transactions so derived balance fields are correct.
        recalculateDailyBalances(openingAccountBalance, upToInterestCalculationDate, backdatedTxnsAllowedTill, postReversals);

        final List<PostingPeriod> allPostingPeriods = new ArrayList<>();
        if (hasInterestCalculation() || hasOverdraftInterestCalculation()) {
            // 1. default to calculate interest based on entire history OR
            // 2. determine latest 'posting period' and find interest credited to that
            // period

            // A generate list of EndOfDayBalances (not including interest postings)
            final SavingsPostingInterestPeriodType postingPeriodType = SavingsPostingInterestPeriodType
                    .fromInt(this.interestPostingPeriodType);

            final SavingsCompoundingInterestPeriodType compoundingPeriodType = SavingsCompoundingInterestPeriodType
                    .fromInt(this.interestCompoundingPeriodType);

            final SavingsInterestCalculationDaysInYearType daysInYearType = SavingsInterestCalculationDaysInYearType
                    .fromInt(this.interestCalculationDaysInYearType);
            List<LocalDate> postedAsOnDates = null;
            if (backdatedTxnsAllowedTill) {
                postedAsOnDates = getManualPostingDatesWithPivotConfig();
            } else {
                postedAsOnDates = getManualPostingDates();
            }
            if (postInterestOnDate != null) {
                postedAsOnDates.add(postInterestOnDate);
            }
            final List<LocalDateInterval> postingPeriodIntervals = SavingsHelper.determineInterestPostingPeriods(
                    getStartInterestCalculationDate(), upToInterestCalculationDate, postingPeriodType, financialYearBeginningMonth,
                    postedAsOnDates);

            Money periodStartingBalance;
            if (this.startInterestCalculationDate != null && !this.getStartInterestCalculationDate().equals(this.getActivatedOnDate())) {
                LocalDate startInterestCalculationDate = this.startInterestCalculationDate;
                SavingsAccountTransaction transaction = null;
                if (backdatedTxnsAllowedTill) {
                    transaction = findLastFilteredTransactionWithPivotConfig(startInterestCalculationDate);
                } else {
                    transaction = findLastTransaction(startInterestCalculationDate);
                }

                if (transaction == null) {
                    periodStartingBalance = Money.zero(this.currency);
                } else {
                    periodStartingBalance = Money.of(this.currency, this.summary.getRunningBalanceOnPivotDate());
                }
            } else {
                periodStartingBalance = Money.zero(this.currency);
            }

            final SavingsInterestCalculationType interestCalculationType = SavingsInterestCalculationType
                    .fromInt(this.interestCalculationType);
            final BigDecimal interestRateAsFraction = getEffectiveInterestRateAsFraction(mc, upToInterestCalculationDate);
            final BigDecimal overdraftInterestRateAsFraction = getEffectiveOverdraftInterestRateAsFraction(mc);
            final Money minBalanceForInterestCalculation = Money.of(getCurrency(), minBalanceForInterestCalculation());
            final Money minOverdraftForInterestCalculation = Money.of(getCurrency(), this.minOverdraftForInterestCalculation);

            for (final LocalDateInterval periodInterval : postingPeriodIntervals) {

                boolean isUserPosting = false;
                if (postedAsOnDates.contains(periodInterval.endDate().plusDays(1))) {
                    isUserPosting = true;
                }

                PostingPeriod postingPeriod = null;
                List<SavingsAccountTransaction> orderedNonInterestPostingTransactions = null;
                if (backdatedTxnsAllowedTill) {
                    orderedNonInterestPostingTransactions = retreiveOrderedNonInterestPostingSavingsTransactionsWithPivotConfig();
                } else {
                    orderedNonInterestPostingTransactions = retreiveOrderedNonInterestPostingTransactions();
                }

                List<SavingsAccountTransactionDetailsForPostingPeriod> savingsAccountTransactionDetailsForPostingPeriod = toSavingsAccountTransactionDetailsForPostingPeriodList(
                        orderedNonInterestPostingTransactions);

                postingPeriod = PostingPeriod.createFrom(periodInterval, periodStartingBalance,
                        savingsAccountTransactionDetailsForPostingPeriod, this.currency, compoundingPeriodType, interestCalculationType,
                        interestRateAsFraction, daysInYearType.getValue(), upToInterestCalculationDate, interestPostTransactions,
                        isInterestTransfer, minBalanceForInterestCalculation, isSavingsInterestPostingAtCurrentPeriodEnd,
                        overdraftInterestRateAsFraction, minOverdraftForInterestCalculation, isUserPosting, financialYearBeginningMonth);

                periodStartingBalance = postingPeriod.closingBalance();

                allPostingPeriods.add(postingPeriod);
            }

            SavingsHelper.calculateInterestForAllPostingPeriods(this.currency, allPostingPeriods, getLockedInUntilDate(),
                    isTransferInterestToOtherAccount());

            this.summary.updateFromInterestPeriodSummaries(this.currency, allPostingPeriods);
        }

        if (backdatedTxnsAllowedTill) {
            this.summary.updateSummaryWithPivotConfig(this.currency, null, this.savingsAccountTransactions);
        } else {
            this.summary.updateSummary(this.currency, this.transactions);
        }

        return allPostingPeriods;
    }

    private BigDecimal getEffectiveOverdraftInterestRateAsFraction(MathContext mc) {
        return this.nominalAnnualInterestRateOverdraft.divide(BigDecimal.valueOf(100L), mc);
    }

    public BigDecimal getEffectiveInterestRateAsFractionAccrual(final MathContext mc, final LocalDate upToInterestCalculationDate) {
        return this.nominalAnnualInterestRate.divide(BigDecimal.valueOf(100L), mc);
    }

    @SuppressWarnings("unused")
    protected BigDecimal getEffectiveInterestRateAsFraction(final MathContext mc, final LocalDate upToInterestCalculationDate) {
        return this.nominalAnnualInterestRate.divide(BigDecimal.valueOf(100L), mc);
    }

    private boolean hasInterestCalculation() {
        return !MathUtil.isEmpty(nominalAnnualInterestRate);
    }

    private boolean hasOverdraftInterestCalculation() {
        return isAllowOverdraft() && !MathUtil.isEmpty(getOverdraftLimit()) && !MathUtil.isEmpty(nominalAnnualInterestRateOverdraft);
    }

    public List<SavingsAccountTransaction> retrieveOrderedAccrualTransactions() {
        return retrieveListOfTransactions().stream().filter(SavingsAccountTransaction::isAccrual)
                .sorted(new SavingsAccountTransactionComparator()).collect(Collectors.toList());
    }

    protected List<SavingsAccountTransaction> retreiveOrderedNonInterestPostingTransactions() {
        final List<SavingsAccountTransaction> listOfTransactionsSorted = retrieveListOfTransactions();

        final List<SavingsAccountTransaction> orderedNonInterestPostingTransactions = new ArrayList<>();

        for (final SavingsAccountTransaction transaction : listOfTransactionsSorted) {
            if (!(transaction.isInterestPostingAndNotReversed() || transaction.isOverdraftInterestAndNotReversed())
                    && transaction.isNotReversed() && !transaction.isReversalTransaction()) {
                orderedNonInterestPostingTransactions.add(transaction);
            }
        }
        orderedNonInterestPostingTransactions.sort(new SavingsAccountTransactionComparator());
        return orderedNonInterestPostingTransactions;
    }

    protected List<SavingsAccountTransaction> retreiveOrderedNonInterestPostingSavingsTransactionsWithPivotConfig() {
        final List<SavingsAccountTransaction> listOfTransactionsSorted = retrieveSortedTransactions();

        final List<SavingsAccountTransaction> orderedNonInterestPostingTransactions = new ArrayList<>();

        for (final SavingsAccountTransaction transaction : listOfTransactionsSorted) {
            if (!(transaction.isInterestPostingAndNotReversed() || transaction.isOverdraftInterestAndNotReversed())
                    && transaction.isNotReversed() && !transaction.isReversalTransaction()) {
                orderedNonInterestPostingTransactions.add(transaction);
            }
        }
        orderedNonInterestPostingTransactions.sort(new SavingsAccountTransactionComparator());
        return orderedNonInterestPostingTransactions;
    }

    protected List<SavingsAccountTransaction> retrieveSortedTransactions() {
        final List<SavingsAccountTransaction> listOfTransactionsSorted = new ArrayList<>();
        listOfTransactionsSorted.addAll(this.savingsAccountTransactions);
        final SavingsAccountTransactionComparator transactionComparator = new SavingsAccountTransactionComparator();
        Collections.sort(listOfTransactionsSorted, transactionComparator);
        return listOfTransactionsSorted;
    }

    protected List<SavingsAccountTransaction> retrieveListOfTransactions() {
        final List<SavingsAccountTransaction> listOfTransactionsSorted = new ArrayList<>();
        listOfTransactionsSorted.addAll(this.transactions);

        final SavingsAccountTransactionComparator transactionComparator = new SavingsAccountTransactionComparator();
        Collections.sort(listOfTransactionsSorted, transactionComparator);
        return listOfTransactionsSorted;
    }

    public void recalculateDailyBalances(final Money openingAccountBalance, final LocalDate interestPostingUpToDate,
            final boolean backdatedTxnsAllowedTill, boolean postReversals) {
        Money runningBalance = openingAccountBalance;
        boolean calculateInterest = hasInterestCalculation() || hasOverdraftInterestCalculation();

        List<SavingsAccountTransaction> accountTransactionsSorted = null;

        if (backdatedTxnsAllowedTill) {
            accountTransactionsSorted = retrieveSortedTransactions();
        } else {
            accountTransactionsSorted = retrieveListOfTransactions();
        }

        boolean isTransactionsModified = false;
        for (final SavingsAccountTransaction transaction : accountTransactionsSorted) {
            if (transaction.isReversed() || transaction.isReversalTransaction()) {
                transaction.zeroBalanceFields();
            } else {
                Money overdraftAmount = Money.zero(this.currency);
                Money transactionAmount = Money.zero(this.currency);
                if ((transaction.isCredit() || transaction.isAmountRelease())) {
                    if (runningBalance.isLessThanZero()) {
                        Money diffAmount = transaction.getAmount(this.currency).plus(runningBalance);
                        if (diffAmount.isGreaterThanZero()) {
                            overdraftAmount = transaction.getAmount(this.currency).minus(diffAmount);
                        } else {
                            overdraftAmount = transaction.getAmount(this.currency);
                        }
                    }
                    transactionAmount = transactionAmount.plus(transaction.getAmount(this.currency));
                } else if (transaction.isDebit() || transaction.isAmountOnHold()) {
                    if (runningBalance.isLessThanZero()) {
                        overdraftAmount = transaction.getAmount(this.currency);
                    }
                    transactionAmount = transactionAmount.minus(transaction.getAmount(this.currency));
                }

                runningBalance = runningBalance.plus(transactionAmount);
                transaction.setRunningBalance(runningBalance);

                if (MathUtil.isEmpty(overdraftAmount) && runningBalance.isLessThanZero() && !transaction.isAmountOnHold()) {
                    overdraftAmount = runningBalance.negated();
                }
                if (!calculateInterest || transaction.getId() == null || transaction.getOverdraftAmount(this.currency).isZero()) {
                    transaction.setOverdraftAmount(overdraftAmount);
                } else if (!MathUtil.isEqualTo(overdraftAmount, transaction.getOverdraftAmount(this.currency))
                        && !transaction.isAccrual()) {
                    SavingsAccountTransaction accountTransaction = SavingsAccountTransaction.copyTransaction(transaction);
                    if (transaction.isChargeTransaction()) {
                        Set<SavingsAccountChargePaidBy> chargesPaidBy = transaction.getSavingsAccountChargesPaid();
                        final Set<SavingsAccountChargePaidBy> newChargePaidBy = new HashSet<>();
                        chargesPaidBy.forEach(x -> newChargePaidBy
                                .add(SavingsAccountChargePaidBy.instance(accountTransaction, x.getSavingsAccountCharge(), x.getAmount())));
                        accountTransaction.getSavingsAccountChargesPaid().addAll(newChargePaidBy);
                    }
                    SavingsAccountTransaction reversal = null;
                    transaction.reverse();
                    if (postReversals) {
                        reversal = SavingsAccountTransaction.reversal(transaction);
                    }
                    if (MathUtil.isGreaterThanZero(overdraftAmount)) {
                        accountTransaction.setOverdraftAmount(overdraftAmount);
                    }
                    accountTransaction.setRunningBalance(runningBalance);
                    if (backdatedTxnsAllowedTill) {
                        addTransactionToExisting(accountTransaction);
                        if (reversal != null) {
                            addTransactionToExisting(reversal);
                        }
                    } else {
                        addTransaction(accountTransaction);
                        if (reversal != null) {
                            addTransaction(reversal);
                        }
                    }
                    isTransactionsModified = true;
                }

            }
        }

        if (isTransactionsModified) {
            if (backdatedTxnsAllowedTill) {
                accountTransactionsSorted = retrieveSortedTransactions();
            } else {
                accountTransactionsSorted = retrieveListOfTransactions();
            }
        }
        resetAccountTransactionsEndOfDayBalances(accountTransactionsSorted, interestPostingUpToDate);
    }

    protected void resetAccountTransactionsEndOfDayBalances(final List<SavingsAccountTransaction> accountTransactionsSorted,
            final LocalDate interestPostingUpToDate) {
        // loop over transactions in reverse
        LocalDate endOfBalanceDate = interestPostingUpToDate;
        for (int i = accountTransactionsSorted.size() - 1; i >= 0; i--) {
            final SavingsAccountTransaction transaction = accountTransactionsSorted.get(i);
            if (transaction.isNotReversed() && !transaction.isReversalTransaction()
                    && !(transaction.isInterestPostingAndNotReversed() || transaction.isOverdraftInterestAndNotReversed())) {
                transaction.updateCumulativeBalanceAndDates(this.currency, endOfBalanceDate);
                // this transactions transaction date is end of balance date for
                // previous transaction.
                endOfBalanceDate = transaction.getTransactionDate().minusDays(1);
            }
        }
    }

    /**
     * Sub-type specific validation hook run before a deposit is recorded by the transaction service. The base savings
     * account imposes no extra rules; {@code RecurringDepositAccount} overrides this to enforce its maturity and
     * deposit-start-date constraints. Kept on the entity (rather than the service) so the polymorphic rule lives with
     * the type that owns it.
     */
    public void validateDepositTransaction(final SavingsAccountTransactionDTO transactionDTO) {
        // no-op for plain savings accounts
    }

    public void validatePivotDateTransaction(LocalDate transactionDate, final boolean backdatedTxnsAllowedTill,
            final Long relaxingDaysConfigForPivotDate, final String resourceTypeName) {
        if (backdatedTxnsAllowedTill) {
            if (this.getSummary().getInterestPostedTillDate() != null && DateUtils.isBefore(transactionDate,
                    getSummary().getInterestPostedTillDate().minusDays(relaxingDaysConfigForPivotDate))) {
                final Object[] defaultUserArgs = Arrays.asList(transactionDate, getActivatedOnDate()).toArray();
                final String defaultUserMessage = "Transaction date cannot be before transactions pivot date.";
                final ApiParameterError error = ApiParameterError.parameterError(
                        "error.msg." + resourceTypeName + ".transaction.before.pivot.date", defaultUserMessage, "transactionDate",
                        defaultUserArgs);

                final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                dataValidationErrors.add(error);

                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
    }

    // startInterestCalculationDate is set during migration so that there is no
    // interference with interest posting of previous system
    public LocalDate getStartInterestCalculationDate() {
        LocalDate startInterestCalculationLocalDate = null;
        if (this.startInterestCalculationDate != null) {
            startInterestCalculationLocalDate = this.startInterestCalculationDate;
        } else {
            startInterestCalculationLocalDate = getActivatedOnDate();
        }
        return startInterestCalculationLocalDate;
    }

    public BigDecimal calculateWithdrawalFee(final BigDecimal transactionAmount) {
        BigDecimal result = BigDecimal.ZERO;
        if (isWithdrawalFeeApplicableForTransfer()) {
            for (SavingsAccountCharge charge : this.charges()) {
                if (charge.isWithdrawalFee() && charge.isActive()) {
                    result = result.add(charge.calculateWithdralFeeAmount(transactionAmount), MoneyHelper.getMathContext());
                }
            }
        }
        return result;
    }

    public boolean isBeforeLastPostingPeriod(final LocalDate transactionDate, final boolean backdatedTxnsAllowedTill) {

        boolean transactionBeforeLastInterestPosting = false;

        if (!backdatedTxnsAllowedTill) {
            for (final SavingsAccountTransaction transaction : retrieveListOfTransactions()) {
                if ((transaction.isInterestPostingAndNotReversed() || transaction.isOverdraftInterestAndNotReversed())
                        && transaction.isAfter(transactionDate) && !transaction.isReversalTransaction()) {
                    transactionBeforeLastInterestPosting = true;
                    break;
                }
            }
        } else {
            if (this.summary.getInterestPostedTillDate() == null) {
                return false;
            }
            transactionBeforeLastInterestPosting = DateUtils.isBefore(transactionDate, this.summary.getInterestPostedTillDate());
        }

        return transactionBeforeLastInterestPosting;
    }

    public void validateAccountBalanceConstraints(final BigDecimal transactionAmount, final boolean isException,
            final List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions, final boolean backdatedTxnsAllowedTill,
            final boolean isForceWithdrawal, final Long forceWithdrawalLimit) {

        List<SavingsAccountTransaction> transactionsSortedByDate = backdatedTxnsAllowedTill ? retrieveSortedTransactions()
                : retrieveListOfTransactions();

        Money runningBalance = Money.zero(this.currency);
        if (backdatedTxnsAllowedTill) {
            runningBalance = Money.of(this.currency, this.summary.getRunningBalanceOnPivotDate());
        }

        Money minRequiredBalance = minRequiredBalanceDerived(getCurrency());
        LocalDate lastSavingsDate = null;
        final BigDecimal withdrawalFee = null;
        for (final SavingsAccountTransaction transaction : transactionsSortedByDate) {
            if (transaction.isNotReversed() && transaction.isCredit() && !transaction.isReversalTransaction()) {
                runningBalance = runningBalance.plus(transaction.getAmount(this.currency));
            } else if (transaction.isNotReversed() && transaction.isDebit() && !transaction.isReversalTransaction()) {
                runningBalance = runningBalance.minus(transaction.getAmount(this.currency));
            } else {
                continue;
            }

            minRequiredBalance = applyOnHoldAdjustments(minRequiredBalance, depositAccountOnHoldTransactions, lastSavingsDate,
                    transaction.getTransactionDate());

            // deal with potential minRequiredBalance and
            // enforceMinRequiredBalance
            if (!isException && transaction.canProcessBalanceCheck() && !isOverdraft()) {
                if (violatesMinRequiredBalance(runningBalance, minRequiredBalance)
                        && !isForceWithdrawalAllowed(isForceWithdrawal, runningBalance, forceWithdrawalLimit)) {
                    throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), withdrawalFee,
                            transactionAmount);
                }
            }
            lastSavingsDate = transaction.getTransactionDate();

        }

        // In overdraft cases, minRequiredBalance can be in violation after
        // interest posting
        // and should be checked after processing all transactions
        if (isOverdraft()) {
            if (violatesMinRequiredBalance(runningBalance, minRequiredBalance)
                    && !isForceWithdrawalAllowed(isForceWithdrawal, runningBalance, forceWithdrawalLimit)) {
                throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), withdrawalFee, transactionAmount);
            }
        }

        if (violatesMinBalanceWithHold(runningBalance, minRequiredBalance, this.getSavingsHoldAmount(), this.enforceMinRequiredBalance)) {
            throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), withdrawalFee, transactionAmount);
        }

    }

    private boolean violatesMinRequiredBalance(Money runningBalance, Money minRequiredBalance) {
        return runningBalance.minus(minRequiredBalance).isLessThanZero();
    }

    private Money applyOnHoldAdjustments(Money minRequiredBalance, List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions,
            LocalDate lastSavingsDate, LocalDate transactionDate) {
        /*
         * Loop through the onHold funds and see if we need to deduct or add to minimum required balance and the point
         * in time the transaction was made:
         */
        if (depositAccountOnHoldTransactions != null) {
            for (final DepositAccountOnHoldTransaction onHoldTransaction : depositAccountOnHoldTransactions) {
                // Compare the balance of the on hold:
                if (!DateUtils.isAfter(onHoldTransaction.getTransactionDate(), transactionDate)
                        && (lastSavingsDate == null || DateUtils.isAfter(onHoldTransaction.getTransactionDate(), lastSavingsDate))) {
                    if (onHoldTransaction.getTransactionType().isHold()) {
                        minRequiredBalance = minRequiredBalance.plus(onHoldTransaction.getAmount(this.currency));
                    } else {
                        minRequiredBalance = minRequiredBalance.minus(onHoldTransaction.getAmount(this.currency));
                    }
                }
            }
        }
        return minRequiredBalance;
    }

    private boolean violatesMinBalanceWithHold(Money runningBalance, Money minRequiredBalance, BigDecimal savingsHoldAmount,
            boolean enforceMinRequiredBalance) {
        // do not move or add logic before this !
        if (savingsHoldAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (enforceMinRequiredBalance) {
            return runningBalance.minus(minRequiredBalance.plus(savingsHoldAmount)).isLessThanZero();
        }

        return runningBalance.minus(savingsHoldAmount).isLessThanZero();
    }

    /**
     * Checks whether a force withdrawal is allowed based on the global configuration and the configured negative
     * balance limit.
     *
     * @param isForceWithdrawal
     *            whether the current transaction is a force withdrawal
     * @param runningBalance
     *            the current running balance of the account
     * @param forceWithdrawalLimit
     *            the configured negative balance limit when force withdrawal on savings accounts is enabled, or
     *            {@code null} when it is disabled; resolved by the caller so the entity stays free of configuration
     *            lookups
     * @return true if force withdrawal is enabled and the running balance is within the allowed negative limit
     */
    private boolean isForceWithdrawalAllowed(final boolean isForceWithdrawal, final Money runningBalance, final Long forceWithdrawalLimit) {
        if (!isForceWithdrawal || forceWithdrawalLimit == null) {
            return false;
        }
        BigDecimal limitBd = BigDecimal.valueOf(forceWithdrawalLimit);
        if (limitBd.compareTo(BigDecimal.ZERO) > 0) {
            limitBd = limitBd.negate();
        }
        return runningBalance.getAmount().compareTo(limitBd) >= 0;
    }

    public void validateAccountBalanceConstraints(final String transactionAction,
            final List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions, final boolean backdatedTxnsAllowedTill) {

        List<SavingsAccountTransaction> transactionsSortedByDate = backdatedTxnsAllowedTill ? retrieveSortedTransactions()
                : retrieveListOfTransactions();
        BigDecimal transactionAmount = null;

        Money runningBalance = Money.zero(this.currency);

        if (backdatedTxnsAllowedTill) {
            runningBalance = Money.of(this.currency, this.summary.getRunningBalanceOnPivotDate());
        }

        Money minRequiredBalance = minRequiredBalanceDerived(getCurrency());
        LocalDate lastSavingsDate = null;
        for (final SavingsAccountTransaction transaction : transactionsSortedByDate) {

            transactionAmount = transaction.getAmount();

            if (transaction.isNotReversed() && transaction.isCredit()) {
                runningBalance = runningBalance.plus(transaction.getAmount(this.currency));
            } else if (transaction.isNotReversed() && transaction.isDebit()) {
                runningBalance = runningBalance.minus(transaction.getAmount(this.currency));
            }

            minRequiredBalance = applyOnHoldAdjustments(minRequiredBalance, depositAccountOnHoldTransactions, lastSavingsDate,
                    transaction.getTransactionDate());

            // enforceMinRequiredBalance
            if (transaction.canProcessBalanceCheck()) {
                if (violatesMinRequiredBalance(runningBalance, minRequiredBalance)) {
                    final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                    final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                            .resource(depositAccountType().resourceName() + transactionAction);
                    if (!this.allowOverdraft) {
                        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("results.in.balance.going.negative");
                    }
                    if (!dataValidationErrors.isEmpty()) {
                        throw new PlatformApiDataValidationException(dataValidationErrors);
                    }
                }
            }
            lastSavingsDate = transaction.getTransactionDate();
        }

        BigDecimal withdrawalFee = null;
        if (isOverdraft()) {
            if (violatesMinRequiredBalance(runningBalance, minRequiredBalance)) {
                throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), withdrawalFee, transactionAmount);
            }
        }
    }

    public void validateAccountBalanceDoesNotViolateOverdraft(final List<SavingsAccountTransaction> savingsAccountTransaction,
            final BigDecimal amountPaid) {
        if (savingsAccountTransaction != null && !savingsAccountTransaction.isEmpty()) {
            SavingsAccountTransaction savingsAccountTransactionFirst = savingsAccountTransaction.get(0);
            if (!this.allowOverdraft) {
                if (savingsAccountTransactionFirst.getRunningBalance(this.currency).minus(amountPaid).isLessThanZero()) {
                    throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), null, amountPaid);
                }
            }

        } else {
            if (!this.allowOverdraft) {
                throw new InsufficientAccountBalanceException("transactionAmount", BigDecimal.ZERO, null, amountPaid);
            }
        }
    }

    public boolean isAccountLocked(final LocalDate transactionDate) {
        return DateUtils.isBefore(transactionDate, getLockedInUntilDate());
    }

    public BigDecimal getAccountBalance() {
        return this.summary.getAccountBalance(this.currency).getAmount();
    }

    public void modifyApplication(final JsonCommand command, final Map<String, Object> actualChanges) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.modifyApplicationAction);
        this.modifyApplication(command, actualChanges, baseDataValidator);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
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

        if (command.isChangeInLocalDateParameterNamed(SavingsApiConstants.submittedOnDateParamName, getSubmittedOnDate())) {
            final String newValueAsString = command.stringValueOfParameterNamed(SavingsApiConstants.submittedOnDateParamName);
            actualChanges.put(SavingsApiConstants.submittedOnDateParamName, newValueAsString);
            actualChanges.put(SavingsApiConstants.localeParamName, localeAsInput);
            actualChanges.put(SavingsApiConstants.dateFormatParamName, dateFormat);
            this.submittedOnDate = command.localDateValueOfParameterNamed(SavingsApiConstants.submittedOnDateParamName);
        }
        if (command.isChangeInStringParameterNamed(SavingsApiConstants.accountNoParamName, this.accountNumber)) {
            final String newValue = command.stringValueOfParameterNamed(SavingsApiConstants.accountNoParamName);
            actualChanges.put(SavingsApiConstants.accountNoParamName, newValue);
            this.accountNumber = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInStringParameterNamed(SavingsApiConstants.externalIdParamName, this.externalId.getValue())) {
            final String newValue = command.stringValueOfParameterNamed(SavingsApiConstants.externalIdParamName);
            actualChanges.put(SavingsApiConstants.externalIdParamName, newValue);
            this.externalId = ExternalIdFactory.produce(newValue);
            if (this.externalId.isEmpty() && TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
                this.externalId = ExternalId.generate();
            }
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
            this.nominalAnnualInterestRate = newValue;
        }
        if (command.isChangeInIntegerParameterNamed(SavingsApiConstants.interestCompoundingPeriodTypeParamName,
                this.interestCompoundingPeriodType)) {
            final Integer newValue = command.integerValueOfParameterNamed(SavingsApiConstants.interestCompoundingPeriodTypeParamName);
            this.interestCompoundingPeriodType = newValue != null ? SavingsCompoundingInterestPeriodType.fromInt(newValue).getValue()
                    : newValue;
            actualChanges.put(SavingsApiConstants.interestCompoundingPeriodTypeParamName, this.interestCompoundingPeriodType);
        }
        if (command.isChangeInIntegerParameterNamed(SavingsApiConstants.interestPostingPeriodTypeParamName,
                this.interestPostingPeriodType)) {
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
            this.interestCalculationDaysInYearType = newValue != null
                    ? SavingsInterestCalculationDaysInYearType.fromInt(newValue).getValue()
                    : newValue;
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

        if (command.isChangeInIntegerParameterNamed(SavingsApiConstants.lockinPeriodFrequencyTypeParamName,
                this.lockinPeriodFrequencyType)) {
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

        if (command.isChangeInBigDecimalParameterNamedDefaultingZeroToNull(nominalAnnualInterestRateOverdraftParamName,
                this.nominalAnnualInterestRateOverdraft)) {
            final BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamedDefaultToNullIfZero(nominalAnnualInterestRateOverdraftParamName);
            actualChanges.put(nominalAnnualInterestRateOverdraftParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.nominalAnnualInterestRateOverdraft = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamedDefaultingZeroToNull(minOverdraftForInterestCalculationParamName,
                this.minOverdraftForInterestCalculation)) {
            final BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamedDefaultToNullIfZero(minOverdraftForInterestCalculationParamName);
            actualChanges.put(minOverdraftForInterestCalculationParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.minOverdraftForInterestCalculation = newValue;
        }

        if (!this.allowOverdraft) {
            this.overdraftLimit = null;
            this.nominalAnnualInterestRateOverdraft = null;
            this.minOverdraftForInterestCalculation = null;
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
        if (command.isChangeInBooleanParameterNamed(lienAllowedParamName, this.lienAllowed)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(lienAllowedParamName);
            actualChanges.put(lienAllowedParamName, newValue);
            this.lienAllowed = newValue;
        }
        if (command.isChangeInBigDecimalParameterNamedDefaultingZeroToNull(maxAllowedLienLimitParamName, this.maxAllowedLienLimit)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero(maxAllowedLienLimitParamName);
            actualChanges.put(maxAllowedLienLimitParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.maxAllowedLienLimit = newValue;
        }
        if (command.isChangeInBooleanParameterNamed(withHoldTaxParamName, this.withHoldTax)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(withHoldTaxParamName);
            actualChanges.put(withHoldTaxParamName, newValue);
            this.withHoldTax = newValue;
            if (this.withHoldTax && this.taxGroup == null) {
                baseDataValidator.reset().parameter(withHoldTaxParamName).failWithCode("not.supported.for.this.account");
            }
        }
        validateLockinDetails(baseDataValidator);
        esnureOverdraftLimitsSetForOverdraftAccounts();
    }

    /**
     * If overdrafts are allowed and the overdraft limit is not set, set the same to Zero
     **/
    private void esnureOverdraftLimitsSetForOverdraftAccounts() {

        this.overdraftLimit = this.overdraftLimit == null ? BigDecimal.ZERO : this.overdraftLimit;
        this.nominalAnnualInterestRateOverdraft = this.nominalAnnualInterestRateOverdraft == null ? BigDecimal.ZERO
                : this.nominalAnnualInterestRateOverdraft;
        this.minOverdraftForInterestCalculation = this.minOverdraftForInterestCalculation == null ? BigDecimal.ZERO
                : this.minOverdraftForInterestCalculation;
    }

    private void validateLockinDetails(final DataValidatorBuilder baseDataValidator) {

        /*
         * final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>(); final
         * DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
         * .resource(resourceName);
         */

        if (this.lockinPeriodFrequency == null) {
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(this.lockinPeriodFrequencyType).ignoreIfNull()
                    .inMinMaxRange(0, 3);

            if (this.lockinPeriodFrequencyType != null) {
                baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(this.lockinPeriodFrequency).ignoreIfNull()
                        .integerZeroOrGreater();
            }
        } else {
            baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(this.lockinPeriodFrequencyType)
                    .integerZeroOrGreater();
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(this.lockinPeriodFrequencyType).ignoreIfNull()
                    .inMinMaxRange(0, 3);
        }
    }

    public Map<String, Object> deriveAccountingBridgeData(final String currencyCode, final Set<Long> existingTransactionIds,
            final Set<Long> existingReversedTransactionIds, boolean isAccountTransfer, final boolean backdatedTxnsAllowedTill) {

        final Map<String, Object> accountingBridgeData = new LinkedHashMap<>();
        accountingBridgeData.put("savingsId", getId());
        accountingBridgeData.put("savingsProductId", productId());
        accountingBridgeData.put("currencyCode", currencyCode);
        accountingBridgeData.put("officeId", officeId());
        accountingBridgeData.put("cashBasedAccountingEnabled", isCashBasedAccountingEnabledOnSavingsProduct());
        accountingBridgeData.put("accrualBasedAccountingEnabled", isAccrualBasedAccountingEnabledOnSavingsProduct());
        accountingBridgeData.put("isAccountTransfer", isAccountTransfer);

        final List<Map<String, Object>> newSavingsTransactions = new ArrayList<>();

        List<SavingsAccountTransaction> trans = null;

        if (backdatedTxnsAllowedTill) {
            trans = getSavingsAccountTransactionsWithPivotConfig();
        } else {
            trans = getTransactions();
        }

        // Adding new transactions to the array
        for (final SavingsAccountTransaction transaction : trans) {
            if (transaction.isReversed() && !existingReversedTransactionIds.contains(transaction.getId())) {
                newSavingsTransactions.add(transaction.toMapData(currencyCode));
            } else if (!existingTransactionIds.contains(transaction.getId())) {
                newSavingsTransactions.add(transaction.toMapData(currencyCode));
            }
        }

        accountingBridgeData.put("newSavingsTransactions", newSavingsTransactions);
        return accountingBridgeData;
    }

    public Collection<Long> findExistingTransactionIds() {
        final Collection<Long> ids = new ArrayList<>();
        List<SavingsAccountTransaction> trans = getTransactions();
        for (final SavingsAccountTransaction transaction : trans) {
            ids.add(transaction.getId());
        }
        return ids;
    }

    public Collection<Long> findCurrentTransactionIdsWithPivotDateConfig() {

        final Collection<Long> ids = new ArrayList<>();
        List<SavingsAccountTransaction> trans = getSavingsAccountTransactionsWithPivotConfig();
        for (final SavingsAccountTransaction transaction : trans) {
            ids.add(transaction.getId());
        }
        return ids;
    }

    public Collection<Long> findExistingReversedTransactionIds() {

        final Collection<Long> ids = new ArrayList<>();
        List<SavingsAccountTransaction> trans = getTransactions();
        // time consuming
        for (final SavingsAccountTransaction transaction : trans) {
            if (transaction.isReversed()) {
                ids.add(transaction.getId());
            }
        }

        return ids;
    }

    public Collection<Long> findCurrentReversedTransactionIdsWithPivotDateConfig() {
        final Collection<Long> ids = new ArrayList<>();
        List<SavingsAccountTransaction> trans = getSavingsAccountTransactionsWithPivotConfig();
        // time consuming
        for (final SavingsAccountTransaction transaction : trans) {
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
        } else if (this.group != null) {
            officeId = this.group.officeId();
        }
        return officeId;
    }

    public Office office() {
        Office office = null;
        if (this.client != null) {
            office = this.client.getOffice();
        } else if (this.group != null) {
            office = this.group.getOffice();
        }
        return office;
    }

    public Boolean getEnforceMinRequiredBalance() {
        return this.enforceMinRequiredBalance;
    }

    // Kept explicitly: the getXxx() accessor above makes Lombok @Getter skip the isXxx() form for these boolean fields.
    public boolean isEnforceMinRequiredBalance() {
        return this.enforceMinRequiredBalance;
    }

    public Boolean getLienAllowed() {
        return this.lienAllowed;
    }

    public boolean isLienAllowed() {
        return this.lienAllowed;
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

    public Long getSavingsProductId() {
        return this.savingsProduct().getId();
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
            matchesCurrentSavingsOfficer = this.savingsOfficer.getId().equals(fromSavingsOfficer.getId());
        } else {
            matchesCurrentSavingsOfficer = fromSavingsOfficer == null;
        }
        return matchesCurrentSavingsOfficer;
    }

    public void reassignSavingsOfficer(final Staff newSavingsOfficer, final LocalDate assignmentDate) {
        final SavingsOfficerAssignmentHistory latestHistoryRecord = findLatestIncompleteHistoryRecord();
        final SavingsOfficerAssignmentHistory lastAssignmentRecord = findLastAssignmentHistoryRecord(newSavingsOfficer);

        // assignment date should not be less than savings account submitted date
        if (DateUtils.isBefore(assignmentDate, getSubmittedOnDate())) {
            final String errorMessage = "The Savings Officer assignment date (" + assignmentDate.toString()
                    + ") cannot be before savings submitted date (" + getSubmittedOnDate().toString() + ").";
            throw new SavingsOfficerAssignmentDateException("cannot.be.before.savings.submitted.date", errorMessage, assignmentDate,
                    getSubmittedOnDate());
        } else if (lastAssignmentRecord != null && lastAssignmentRecord.isBeforeEndDate(assignmentDate)) {
            final String errorMessage = "The Savings Officer assignment date (" + assignmentDate
                    + ") cannot be before previous Savings Officer unassigned date (" + lastAssignmentRecord.getEndDate() + ").";
            throw new SavingsOfficerAssignmentDateException("cannot.be.before.previous.unassignement.date", errorMessage, assignmentDate,
                    lastAssignmentRecord.getEndDate());
        } else if (DateUtils.isDateInTheFuture(assignmentDate)) {
            final String errorMessage = "The Savings Officer assignment date (" + assignmentDate + ") cannot be in the future.";
            throw new SavingsOfficerAssignmentDateException("cannot.be.a.future.date", errorMessage, assignmentDate);
        } else if (latestHistoryRecord != null && this.savingsOfficer.getId().equals(newSavingsOfficer.getId())) {
            latestHistoryRecord.setStartDate(assignmentDate);
        } else if (latestHistoryRecord != null && latestHistoryRecord.matchesStartDateOf(assignmentDate)) {
            latestHistoryRecord.setSavingsOfficer(newSavingsOfficer);
            this.savingsOfficer = newSavingsOfficer;
        } else if (latestHistoryRecord != null && latestHistoryRecord.isBeforeStartDate(assignmentDate)) {
            final String errorMessage = "Savings account with identifier " + getId() + " was already assigned before date "
                    + assignmentDate;
            throw new SavingsOfficerAssignmentDateException("is.before.last.assignment.date", errorMessage, getId(), assignmentDate);
        } else {
            if (latestHistoryRecord != null) {
                // savings officer correctly changed from previous savings
                // officer to
                // new savings officer
                latestHistoryRecord.setEndDate(assignmentDate);
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
            } else if (historyRecord.isBeforeEndDate(lastAssignmentRecordLatestEndDate.getEndDate())
                    && !historyRecord.isSameSavingsOfficer(newSavingsOfficer)) {
                lastAssignmentRecordLatestEndDate = historyRecord;
            }
        }
        return lastAssignmentRecordLatestEndDate;
    }

    public void removeSavingsOfficer(final LocalDate unassignDate) {
        final SavingsOfficerAssignmentHistory latestHistoryRecord = findLatestIncompleteHistoryRecord();

        if (latestHistoryRecord != null) {
            validateUnassignDate(latestHistoryRecord, unassignDate);
            latestHistoryRecord.setEndDate(unassignDate);
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
        if (DateUtils.isBefore(unassignDate, latestHistoryRecord.getStartDate())) {
            final String errorMessage = "The Savings officer Unassign date(" + unassignDate + ") cannot be before its assignment date ("
                    + latestHistoryRecord.getStartDate() + ").";
            throw new SavingsOfficerUnassignmentDateException("cannot.be.before.assignment.date", errorMessage, getId(),
                    getSavingsOfficer().getId(), latestHistoryRecord.getStartDate(), unassignDate);
        } else if (DateUtils.isDateInTheFuture(unassignDate)) {
            final String errorMessage = "The Savings Officer Unassign date (" + unassignDate + ") cannot be in the future.";
            throw new SavingsOfficerUnassignmentDateException("cannot.be.a.future.date", errorMessage, unassignDate);
        }
    }

    public void validateNewApplicationState(final String resourceName) {
        // validateWithdrawalFeeDetails();
        // validateAnnualFeeDetails();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(resourceName + SavingsApiConstants.summitalAction);

        validateLockinDetails(baseDataValidator);
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        final LocalDate submittedOn = getSubmittedOnDate();
        if (DateUtils.isDateInTheFuture(submittedOn)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.submittedOnDateParamName).value(submittedOn)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.a.future.date");
        }

        if (this.client != null && this.client.isActivatedAfter(submittedOn)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.submittedOnDateParamName).value(this.client.getActivationDate())
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.client.activation.date");
        } else if (this.group != null && this.group.isActivatedAfter(submittedOn)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.submittedOnDateParamName).value(this.group.getActivationDate())
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.client.activation.date");
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void undoTransaction(final Long transactionId) {

        SavingsAccountTransaction transactionToUndo = null;
        for (final SavingsAccountTransaction transaction : this.transactions) {
            if (transaction.isIdentifiedBy(transactionId)) {
                transactionToUndo = transaction;
            }
        }

        if (transactionToUndo == null) {
            throw new SavingsAccountTransactionNotFoundException(this.getId(), transactionId);
        }

        validateAttemptToUndoTransferRelatedTransactions(transactionToUndo);
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_UNDO_TRANSACTION, transactionToUndo.getTransactionDate());
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

    public void undoSavingsTransaction(final Long transactionId) {

        SavingsAccountTransaction transactionToUndo = null;
        for (final SavingsAccountTransaction transaction : this.savingsAccountTransactions) {
            if (transaction.isIdentifiedBy(transactionId)) {
                transactionToUndo = transaction;
            }
        }

        if (transactionToUndo == null) {
            throw new SavingsAccountTransactionNotFoundException(this.getId(), transactionId);
        }

        validateAttemptToUndoTransferRelatedTransactions(transactionToUndo);
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_UNDO_TRANSACTION, transactionToUndo.getTransactionDate());
        transactionToUndo.reverse();
        if (transactionToUndo.isChargeTransaction() || transactionToUndo.isWaiveCharge()) {
            // undo charge
            final Set<SavingsAccountChargePaidBy> chargesPaidBy = transactionToUndo.getSavingsAccountChargesPaid();
            for (final SavingsAccountChargePaidBy savingsAccountChargePaidBy : chargesPaidBy) {
                final SavingsAccountCharge chargeToUndo = savingsAccountChargePaidBy.getSavingsAccountCharge();
                if (transactionToUndo.isChargeTransaction()) {
                    chargeToUndo.undoPayment(this.getCurrency(), transactionToUndo.getAmount(this.currency));
                } else if (transactionToUndo.isWaiveCharge()) {
                    chargeToUndo.undoWaiver(this.getCurrency(), transactionToUndo.getAmount(this.currency));
                }
            }
        }
    }

    public void undoTransaction(final SavingsAccountTransaction transactionToUndo) {

        if (transactionToUndo.isReversed()) {
            throw new SavingsAccountTransactionNotFoundException(this.getId(), transactionToUndo.getId());
        }

        validateAttemptToUndoTransferRelatedTransactions(transactionToUndo);
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_UNDO_TRANSACTION, transactionToUndo.getTransactionDate());
        transactionToUndo.reverse();
        if (transactionToUndo.isChargeTransaction() || transactionToUndo.isWaiveCharge()) {
            // undo charge
            final Set<SavingsAccountChargePaidBy> chargesPaidBy = transactionToUndo.getSavingsAccountChargesPaid();
            for (final SavingsAccountChargePaidBy savingsAccountChargePaidBy : chargesPaidBy) {
                final SavingsAccountCharge chargeToUndo = savingsAccountChargePaidBy.getSavingsAccountCharge();
                if (transactionToUndo.isChargeTransaction()) {
                    chargeToUndo.undoPayment(this.getCurrency(), transactionToUndo.getAmount(this.currency));
                } else if (transactionToUndo.isWaiveCharge()) {
                    chargeToUndo.undoWaiver(this.getCurrency(), transactionToUndo.getAmount(this.currency));
                }
            }
        }
    }

    public LocalDate findLatestAnnualFeeTransactionDueDate() {
        LocalDate nextDueDate = null;

        LocalDate lastAnnualFeeTransactionDate = null;
        for (final SavingsAccountTransaction transaction : retreiveOrderedNonInterestPostingTransactions()) {
            if (transaction.isAnnualFeeAndNotReversed()) {
                LocalDate transactionDate = transaction.getTransactionDate();
                if (lastAnnualFeeTransactionDate == null) {
                    lastAnnualFeeTransactionDate = transactionDate;
                    nextDueDate = lastAnnualFeeTransactionDate;
                }
                if (DateUtils.isAfter(transactionDate, lastAnnualFeeTransactionDate)) {
                    lastAnnualFeeTransactionDate = transactionDate;
                    nextDueDate = lastAnnualFeeTransactionDate;
                }
            }
        }
        return nextDueDate;
    }

    public void validateAccountBalanceConstraintsMinimal(final BigDecimal transactionAmount, final boolean isException) {
        // final List<SavingsAccountTransaction> transactionsSortedByDate =
        // retrieveListOfTransactions();
        Money runningBalance = this.summary.getAccountBalance(getCurrency());
        Money minRequiredBalance = minRequiredBalanceDerived(getCurrency());
        final BigDecimal withdrawalFee = null;

        // check last txn date

        // In overdraft cases, minRequiredBalance can be in violation after interest
        // posting
        // and should be checked after processing all transactions
        if (!isOverdraft()) {
            if (violatesMinRequiredBalance(runningBalance, minRequiredBalance)) {
                throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), withdrawalFee, transactionAmount);
            }
        }

        if (violatesMinBalanceWithHold(runningBalance, minRequiredBalance, this.getSavingsHoldAmount(), this.enforceMinRequiredBalance)) {
            throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), withdrawalFee, transactionAmount);
        }

    }

    public Money activateWithBalance() {
        return Money.of(this.currency, this.minRequiredOpeningBalance);
    }

    public void approveAndActivateApplication(final LocalDate appliedonDate, final AppUser appliedBy) {
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
        this.lockedInUntilDate = calculateDateAccountIsLockedUntil(getActivatedOnDate());
    }

    public Map<String, Object> close(final AppUser currentUser, final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.closeAction);

        final SavingsAccountStatusType currentStatus = getStatus();
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.in.active.state");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final LocalDate closedDate = command.localDateValueOfParameterNamed(SavingsApiConstants.closedOnDateParamName);

        if (DateUtils.isBefore(closedDate, getActivatedOnDate())) {
            baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                    .failWithCode("must.be.after.activation.date");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        if (DateUtils.isAfterBusinessDate(closedDate)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                    .failWithCode("cannot.be.a.future.date");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        final List<SavingsAccountTransaction> savingsAccountTransactions = retrieveListOfTransactions();
        if (!savingsAccountTransactions.isEmpty()) {
            final SavingsAccountTransaction accountTransaction = savingsAccountTransactions.get(savingsAccountTransactions.size() - 1);
            if (accountTransaction.isAfter(closedDate)) {
                baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                        .failWithCode("must.be.after.last.transaction.date");
                if (!dataValidationErrors.isEmpty()) {
                    throw new PlatformApiDataValidationException(dataValidationErrors);
                }
            }
        }
        if (getAccountBalance().doubleValue() != 0) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("results.in.balance.not.zero");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_CLOSE_ACCOUNT, closedDate);
        this.status = SavingsAccountStatusType.CLOSED.getValue();
        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(this.status));
        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.closedOnDateParamName, closedDate.format(fmt));

        this.rejectedOnDate = null;
        this.rejectedBy = null;
        this.withdrawnOnDate = null;
        this.withdrawnBy = null;
        this.closedOnDate = closedDate;
        this.closedBy = currentUser;

        return actualChanges;
    }

    public void validateActivityNotBeforeClientOrGroupTransferDate(final SavingsEvent event, final LocalDate activityDate) {
        if (this.client != null) {
            final LocalDate clientOfficeJoiningDate = this.client.getOfficeJoiningDate();
            if (DateUtils.isBefore(activityDate, clientOfficeJoiningDate)) {
                throw new SavingsActivityPriorToClientTransferException(event.toString(), clientOfficeJoiningDate);
            }
        }
    }

    private void validateAttemptToUndoTransferRelatedTransactions(final SavingsAccountTransaction savingsAccountTransaction) {
        if (savingsAccountTransaction.isTransferRelatedTransaction()) {
            throw new SavingsTransferTransactionsCannotBeUndoneException(savingsAccountTransaction.getId());
        }
    }

    public LocalDate calculateDateAccountIsLockedUntil(final LocalDate activationLocalDate) {

        LocalDate lockedInUntilLocalDate = null;
        final PeriodFrequencyType lockinPeriodFrequencyType = PeriodFrequencyType.fromInt(this.lockinPeriodFrequencyType);
        switch (lockinPeriodFrequencyType) {
            case INVALID:
            break;
            case DAYS:
                lockedInUntilLocalDate = activationLocalDate.plusDays(this.lockinPeriodFrequency);
            break;
            case WEEKS:
                lockedInUntilLocalDate = activationLocalDate.plusWeeks(this.lockinPeriodFrequency);
            break;
            case MONTHS:
                lockedInUntilLocalDate = activationLocalDate.plusMonths(this.lockinPeriodFrequency);
            break;
            case YEARS:
                lockedInUntilLocalDate = activationLocalDate.plusYears(this.lockinPeriodFrequency);
            break;
            case WHOLE_TERM:
                LOG.error("TODO Implement calculateDateAccountIsLockedUntil for WHOLE_TERM");
            break;
        }

        return lockedInUntilLocalDate;
    }

    public Group group() {
        return this.group;
    }

    public void activateAccountBasedOnBalance() {
        if (getStatus().isClosed() && !this.summary.getAccountBalance(getCurrency()).isZero()) {
            this.status = SavingsAccountStatusType.ACTIVE.getValue();
        }
    }

    public void addTransaction(final SavingsAccountTransaction transaction) {
        this.transactions.add(transaction);
    }

    public void addTransactionToExisting(final SavingsAccountTransaction transaction) {
        this.savingsAccountTransactions.add(transaction);
    }

    private Set<SavingsAccountCharge> associateChargesWithThisSavingsAccount(final Set<SavingsAccountCharge> savingsAccountCharges) {
        for (final SavingsAccountCharge savingsAccountCharge : savingsAccountCharges) {
            savingsAccountCharge.update(this);
        }
        return savingsAccountCharges;
    }

    public boolean update(final Set<SavingsAccountCharge> newSavingsAccountCharges) {
        if (newSavingsAccountCharges == null) {
            return false;
        }

        if (this.charges == null) {
            this.charges = new HashSet<>();
        }
        this.charges.clear();
        this.charges.addAll(associateChargesWithThisSavingsAccount(newSavingsAccountCharges));
        return true;
    }

    public boolean hasCurrencyCodeOf(final String matchingCurrencyCode) {
        if (this.currency == null) {
            return false;
        }
        return this.currency.getCode().equalsIgnoreCase(matchingCurrencyCode);
    }

    public SavingsAccountCharge getCharge(final Long savingsAccountChargeId) {
        SavingsAccountCharge charge = null;
        for (final SavingsAccountCharge existingCharge : this.charges) {
            if (existingCharge.getId().equals(savingsAccountChargeId)) {
                charge = existingCharge;
                break;
            }
        }

        if (charge == null) {
            throw new SavingsAccountChargeNotFoundException(savingsAccountChargeId, getId());
        }

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
                && this.overdraftLimit.compareTo(this.product.overdraftLimit()) > 0) {
            baseDataValidator.reset().parameter(SavingsApiConstants.overdraftLimitParamName).value(this.overdraftLimit)
                    .failWithCode("cannot.exceed.product.value");
        }

        validateInterestPostingAndCompoundingPeriodTypes(baseDataValidator);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public boolean allowOverdraft() {
        return this.allowOverdraft;
    }

    public LocalDate accountSubmittedOrActivationDate() {
        return getActivatedOnDate() == null ? getSubmittedOnDate() : getActivatedOnDate();
    }

    protected boolean isTransferInterestToOtherAccount() {
        return false;
    }

    public boolean accountSubmittedAndActivationOnSameDate() {
        return getActivatedOnDate() != null && DateUtils.isEqual(getActivatedOnDate(), getSubmittedOnDate());

    }

    public void validateInterestPostingAndCompoundingPeriodTypes(final DataValidatorBuilder baseDataValidator) {
        Map<SavingsPostingInterestPeriodType, List<SavingsCompoundingInterestPeriodType>> postingtoCompoundMap = new HashMap<>();

        postingtoCompoundMap.put(SavingsPostingInterestPeriodType.DAILY, Arrays.asList(SavingsCompoundingInterestPeriodType.DAILY));

        postingtoCompoundMap.put(SavingsPostingInterestPeriodType.MONTHLY,
                Arrays.asList(SavingsCompoundingInterestPeriodType.DAILY, SavingsCompoundingInterestPeriodType.MONTHLY));

        postingtoCompoundMap.put(SavingsPostingInterestPeriodType.QUATERLY, Arrays.asList(SavingsCompoundingInterestPeriodType.DAILY,
                SavingsCompoundingInterestPeriodType.MONTHLY, SavingsCompoundingInterestPeriodType.QUATERLY));

        postingtoCompoundMap.put(SavingsPostingInterestPeriodType.BIANNUAL,
                Arrays.asList(SavingsCompoundingInterestPeriodType.DAILY, SavingsCompoundingInterestPeriodType.MONTHLY,
                        SavingsCompoundingInterestPeriodType.QUATERLY, SavingsCompoundingInterestPeriodType.BI_ANNUAL));

        postingtoCompoundMap.put(SavingsPostingInterestPeriodType.ANNUAL,
                Arrays.asList(SavingsCompoundingInterestPeriodType.DAILY, SavingsCompoundingInterestPeriodType.MONTHLY,
                        SavingsCompoundingInterestPeriodType.QUATERLY, SavingsCompoundingInterestPeriodType.BI_ANNUAL,
                        SavingsCompoundingInterestPeriodType.ANNUAL));

        postingtoCompoundMap.put(SavingsPostingInterestPeriodType.ANNIVERSARY_MONTHLY,
                Arrays.asList(SavingsCompoundingInterestPeriodType.DAILY, SavingsCompoundingInterestPeriodType.MONTHLY));

        postingtoCompoundMap.put(SavingsPostingInterestPeriodType.ANNIVERSARY_QUARTERLY,
                Arrays.asList(SavingsCompoundingInterestPeriodType.DAILY, SavingsCompoundingInterestPeriodType.MONTHLY,
                        SavingsCompoundingInterestPeriodType.QUATERLY));

        postingtoCompoundMap.put(SavingsPostingInterestPeriodType.ANNIVERSARY_BIANNUAL,
                Arrays.asList(SavingsCompoundingInterestPeriodType.DAILY, SavingsCompoundingInterestPeriodType.MONTHLY,
                        SavingsCompoundingInterestPeriodType.QUATERLY, SavingsCompoundingInterestPeriodType.BI_ANNUAL));

        postingtoCompoundMap.put(SavingsPostingInterestPeriodType.ANNIVERSARY_ANNUAL,
                Arrays.asList(SavingsCompoundingInterestPeriodType.DAILY, SavingsCompoundingInterestPeriodType.MONTHLY,
                        SavingsCompoundingInterestPeriodType.QUATERLY, SavingsCompoundingInterestPeriodType.BI_ANNUAL,
                        SavingsCompoundingInterestPeriodType.ANNUAL));

        SavingsPostingInterestPeriodType savingsPostingInterestPeriodType = SavingsPostingInterestPeriodType
                .fromInt(interestPostingPeriodType);
        SavingsCompoundingInterestPeriodType savingsCompoundingInterestPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(interestCompoundingPeriodType);

        if (postingtoCompoundMap.get(savingsPostingInterestPeriodType) == null) {
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

    public boolean isTransactionAllowed(SavingsAccountTransactionType transactionType, LocalDate transactionDate) {
        if (!isTransactionsAllowed()) {
            return false;
        }

        Client client = getClient();
        if (client != null && !client.isActive()) {
            return false;
        }
        Group group = group();
        if (group != null && !group.isActive()) {
            return false;
        }

        if (transactionDate == null) {
            return true;
        }
        if (DateUtils.isDateInTheFuture(transactionDate) || DateUtils.isBefore(transactionDate, getActivatedOnDate())) {
            return false;
        }
        if (transactionType.isCredit()) {
            return true;
        }
        return !isAccountLocked(transactionDate);
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
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (isNotActive()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.is.not.active");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
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

    private Money minRequiredBalanceDerived(final MonetaryCurrency currency) {
        Money minReqBalance = Money.zero(currency);
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
        return getAccountBalance().subtract(minRequiredBalanceDerived(getCurrency()).getAmount()).subtract(this.getOnHoldFunds())
                .subtract(this.getSavingsHoldAmount());
    }

    public BigDecimal getWithdrawableBalanceWithLien() {
        return getAccountBalance().subtract(minRequiredBalanceDerived(getCurrency()).getAmount()).subtract(this.getOnHoldFunds())
                .subtract(this.getSavingsHoldAmount());
    }

    public BigDecimal getWithdrawableBalanceWithoutMinimumBalance() {
        return getAccountBalance().subtract(this.getOnHoldFunds()).subtract(this.getSavingsHoldAmount());
    }

    public boolean withHoldTax() {
        return this.withHoldTax;
    }

    protected boolean applyWithholdTaxForDepositAccounts(final LocalDate interestPostingUpToDate, boolean recalucateDailyBalance,
            final boolean backdatedTxnsAllowedTill) {
        final List<SavingsAccountTransaction> withholdTransactions = findWithHoldTransactions();
        SavingsAccountTransaction withholdTransaction = findTransactionFor(interestPostingUpToDate, withholdTransactions);
        final BigDecimal totalInterestPosted = SavingsAccountTransactionSummaryWrapper.calculateTotalInterestPosted(this.currency,
                this.transactions);
        if (withholdTransaction == null && this.withHoldTax()) {
            boolean isWithholdTaxAdded = createWithHoldTransaction(totalInterestPosted, interestPostingUpToDate, backdatedTxnsAllowedTill);
            recalucateDailyBalance = recalucateDailyBalance || isWithholdTaxAdded;
        } else {
            boolean isWithholdTaxAdded = updateWithHoldTransaction(totalInterestPosted, withholdTransaction);
            recalucateDailyBalance = recalucateDailyBalance || isWithholdTaxAdded;
        }

        return recalucateDailyBalance;
    }

    /**
     * Flags the account as inactive (dormant-charge bearing). The charge processing and balance recalculation that
     * accompany the transition are orchestrated by the service layer, so the entity only owns the status mutation.
     */
    public void markSubStatusInactive() {
        this.sub_status = SavingsAccountSubStatusEnum.INACTIVE.getValue();
    }

    /**
     * Clears a previously set inactive/dormant sub-status when activity resumes on the account (e.g. a deposit or
     * withdrawal). No-op when the account is not in one of those sub-statuses.
     */
    public void resetDormancySubStatusOnTransaction() {
        if (this.sub_status.equals(SavingsAccountSubStatusEnum.INACTIVE.getValue())
                || this.sub_status.equals(SavingsAccountSubStatusEnum.DORMANT.getValue())) {
            this.sub_status = SavingsAccountSubStatusEnum.NONE.getValue();
        }
    }

    public void setSubStatusDormant() {
        this.sub_status = SavingsAccountSubStatusEnum.DORMANT.getValue();
    }

    public void escheat(AppUser appUser) {
        this.status = SavingsAccountStatusType.CLOSED.getValue();
        this.sub_status = SavingsAccountSubStatusEnum.ESCHEAT.getValue();
        this.closedOnDate = DateUtils.getBusinessLocalDate();
        this.closedBy = appUser;
        boolean postInterestAsOnDate = false;
        boolean postReversals = false;
        LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        if (this.getSummary().getAccountBalance(this.getCurrency()).isGreaterThanZero()) {
            SavingsAccountTransaction transaction = SavingsAccountTransaction.escheat(this, transactionDate, postInterestAsOnDate);
            this.transactions.add(transaction);
        }
        recalculateDailyBalances(Money.zero(this.currency), transactionDate, false, postReversals);
        this.summary.updateSummary(this.currency, this.transactions);
    }

    public void loadLazyCollections() {
        transactions.size();
        charges.size();
        savingsOfficerHistory.size();
        if (group != null) {
            Office dummyOffice = group.getOffice();
        } // Ensure lazy loading of group if set
    }

    public void updateSavingsAccountSummary(final List<SavingsAccountTransaction> transactions) {
        this.summary.updateSummary(this.currency, transactions);
    }

    public void updateReason(final String reasonForBlock) {
        this.reasonForBlock = reasonForBlock;
    }

    public SavingsAccountStatusType getStatus() {
        return SavingsAccountStatusType.fromInt(status);
    }

    public Integer getSubStatus() {
        return this.sub_status;
    }

    public void setSubStatus(final Integer subStatus) {
        this.sub_status = subStatus;
    }

    public void validateForAccountBlock() {
        final SavingsAccountSubStatusEnum currentSubStatus = SavingsAccountSubStatusEnum.fromInt(this.getSubStatus());
        if (SavingsAccountSubStatusEnum.BLOCK.hasStateOf(currentSubStatus)) {
            throw new SavingsAccountBlockedException(this.getId());
        }
    }

    public void validateForDebitBlock() {
        final SavingsAccountSubStatusEnum currentSubStatus = SavingsAccountSubStatusEnum.fromInt(this.getSubStatus());
        if (SavingsAccountSubStatusEnum.BLOCK_DEBIT.hasStateOf(currentSubStatus)) {
            throw new SavingsAccountDebitsBlockedException(this.getId());
        }
    }

    public void validateForCreditBlock() {
        final SavingsAccountSubStatusEnum currentSubStatus = SavingsAccountSubStatusEnum.fromInt(this.getSubStatus());
        if (SavingsAccountSubStatusEnum.BLOCK_CREDIT.hasStateOf(currentSubStatus)) {
            throw new SavingsAccountCreditsBlockedException(this.getId());
        }
    }

    public LocalDate retrieveLastTransactionDate() {
        final List<SavingsAccountTransaction> transactionsSortedByDate = retrieveListOfTransactions();
        SavingsAccountTransaction lastTransaction = null;
        if (!transactionsSortedByDate.isEmpty()) {
            lastTransaction = transactionsSortedByDate.get(transactionsSortedByDate.size() - 1);
        }
        LocalDate lastransactionDate = null;
        if (lastTransaction != null) {
            lastransactionDate = lastTransaction.getTransactionDate();
        }
        return lastransactionDate;
    }

    public LocalDate retrieveLastTransactionDateWithPivotConfig() {
        final List<SavingsAccountTransaction> transactionsSortedByDate = retrieveSortedTransactions();
        SavingsAccountTransaction lastTransaction = null;
        if (!transactionsSortedByDate.isEmpty()) {
            lastTransaction = transactionsSortedByDate.get(transactionsSortedByDate.size() - 1);
        }
        LocalDate lastransactionDate = null;
        if (lastTransaction != null) {
            lastransactionDate = lastTransaction.getTransactionDate();
        }
        return lastransactionDate;
    }

    public BigDecimal getSavingsHoldAmount() {
        return this.savingsOnHoldAmount == null ? BigDecimal.ZERO : this.savingsOnHoldAmount;
    }

    public void holdAmount(BigDecimal amount) {
        this.savingsOnHoldAmount = getSavingsHoldAmount().add(amount);
    }

    public void releaseOnHoldAmount(BigDecimal amount) {
        this.savingsOnHoldAmount = getSavingsHoldAmount().subtract(amount);
    }

    public AccountType getAccountType() {
        return AccountType.fromInt(accountType);
    }

    public Integer getAccountTypes() {
        return accountType;
    }

    private boolean isOverdraft() {
        return allowOverdraft;
    }

    public Long getGroupId() {
        return this.groupId();
    }

    public boolean isWithdrawalFeeForTransfer() {
        return this.withdrawalFeeApplicableForTransfer;
    }

    public int getVersion() {
        return this.version;
    }

    public List<SavingsAccountTransactionDetailsForPostingPeriod> toSavingsAccountTransactionDetailsForPostingPeriodList(
            List<SavingsAccountTransaction> transactions) {
        return transactions.stream()
                .map(transaction -> transaction.toSavingsAccountTransactionDetailsForPostingPeriod(this.currency, this.allowOverdraft))
                .toList();
    }

    public void accrualsForSavingsReverse(SavingsAccountTransactionDTO transactionDTO, final boolean backdatedTxnsAllowedTill) {
        List<SavingsAccountTransaction> accountTransactionsSorted = null;

        if (backdatedTxnsAllowedTill) {
            accountTransactionsSorted = retrieveSortedTransactions();
        } else {
            accountTransactionsSorted = retrieveListOfTransactions();
        }
        for (final SavingsAccountTransaction transaction : accountTransactionsSorted) {
            boolean typeTransaccionValidation = transaction.getTransactionType() == SavingsAccountTransactionType.ACCRUAL;
            if (typeTransaccionValidation && (transaction.getDateOf().isAfter(transactionDTO.getTransactionDate())
                    || transaction.getDateOf().isEqual(transactionDTO.getTransactionDate()))) {
                transaction.reverse();
            }
        }
    }

    public List<SavingsAccountTransactionDetailsForPostingPeriod> toSavingsAccountTransactionDetailsForPostingPeriodList() {
        return retreiveOrderedNonInterestPostingTransactions().stream()
                .map(transaction -> transaction.toSavingsAccountTransactionDetailsForPostingPeriod(this.currency, this.allowOverdraft))
                .toList();
    }

    @Override
    public DepositAccountType depositAccountType() {
        return DepositAccountType.fromInt(100);
    }
}
