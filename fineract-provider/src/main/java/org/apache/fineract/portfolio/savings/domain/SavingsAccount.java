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

import static java.time.temporal.ChronoUnit.DAYS;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.allowOverdraftParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.dueAsOfDateParamName;
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
import java.time.YearMonth;
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
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
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
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.exception.SavingsAccountChargeNotFoundException;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
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
public class SavingsAccount extends AbstractAuditableWithUTCDateTimeCustom {

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
    protected boolean accountNumberRequiresAutoGeneration = false;
    @Transient
    protected SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper;
    @Transient
    protected SavingsHelper savingsHelper;
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

    @Column(name = "total_savings_amount_on_hold", scale = 6, precision = 19, nullable = true)
    private BigDecimal savingsOnHoldAmount;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "account", orphanRemoval = true, fetch = FetchType.LAZY)
    protected List<InteropIdentifier> identifiers = new ArrayList<>();

    public transient ConfigurationDomainService configurationDomainService;

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
            this.accountNumberRequiresAutoGeneration = true;
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

    /**
     * Used after fetching/hydrating a {@link SavingsAccount} object to inject helper services/components used for
     * update summary details after events/transactions on a {@link SavingsAccount}.
     */
    public void setHelpers(final SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper,
            final SavingsHelper savingsHelper) {
        this.savingsAccountTransactionSummaryWrapper = savingsAccountTransactionSummaryWrapper;
        this.savingsHelper = savingsHelper;
    }

    public void setSavingsAccountTransactions(final List<SavingsAccountTransaction> savingsAccountTransactions) {
        this.savingsAccountTransactions.addAll(savingsAccountTransactions);
    }

    public List<SavingsAccountTransaction> getSavingsAccountTransactionsWithPivotConfig() {
        return this.savingsAccountTransactions;
    }

    public ExternalId getExternalId() {
        return externalId;
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

    public List<InteropIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void postInterest(final MathContext mc, final LocalDate interestPostingUpToDate, final boolean isInterestTransfer,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth,
            final LocalDate postInterestOnDate, final boolean backdatedTxnsAllowedTill, final boolean postReversals) {
        final List<PostingPeriod> postingPeriods = calculateInterestUsing(mc, interestPostingUpToDate, isInterestTransfer,
                isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill,
                postReversals);
        if (postingPeriods.isEmpty()) {
            return;
        }

        Money interestPostedToDate = Money.zero(this.currency);

        if (backdatedTxnsAllowedTill) {
            interestPostedToDate = Money.of(this.currency, this.summary.getTotalInterestPosted());
        }

        boolean recalucateDailyBalanceDetails = false;
        boolean applyWithHoldTax = isWithHoldTaxApplicableForInterestPosting();
        final List<SavingsAccountTransaction> withholdTransactions = new ArrayList<>();

        if (backdatedTxnsAllowedTill) {
            withholdTransactions.addAll(findWithHoldSavingsTransactionsWithPivotConfig());
        } else {
            withholdTransactions.addAll(findWithHoldTransactions());
        }

        for (final PostingPeriod interestPostingPeriod : postingPeriods) {
            final LocalDate interestPostingTransactionDate = interestPostingPeriod.dateOfPostingTransaction();
            final Money interestEarnedToBePostedForPeriod = interestPostingPeriod.getInterestEarned();

            if (!DateUtils.isAfter(interestPostingTransactionDate, interestPostingUpToDate)) {
                interestPostedToDate = interestPostedToDate.plus(interestEarnedToBePostedForPeriod);

                SavingsAccountTransaction postingTransaction = null;
                if (backdatedTxnsAllowedTill) {
                    postingTransaction = findInterestPostingSavingsTransactionWithPivotConfig(interestPostingTransactionDate);
                } else {
                    postingTransaction = findInterestPostingTransactionFor(interestPostingTransactionDate);
                }
                if (postingTransaction == null) {
                    SavingsAccountTransaction newPostingTransaction;
                    if (interestEarnedToBePostedForPeriod.isGreaterThanOrEqualTo(Money.zero(currency))) {

                        newPostingTransaction = SavingsAccountTransaction.interestPosting(this, office(), interestPostingTransactionDate,
                                interestEarnedToBePostedForPeriod, interestPostingPeriod.isUserPosting());
                    } else {
                        newPostingTransaction = SavingsAccountTransaction.overdraftInterest(this, office(), interestPostingTransactionDate,
                                interestEarnedToBePostedForPeriod.negated(), interestPostingPeriod.isUserPosting());
                    }
                    if (backdatedTxnsAllowedTill) {
                        addTransactionToExisting(newPostingTransaction);
                    } else {
                        addTransaction(newPostingTransaction);
                    }
                    if (applyWithHoldTax) {
                        createWithHoldTransaction(interestEarnedToBePostedForPeriod.getAmount(), interestPostingTransactionDate,
                                backdatedTxnsAllowedTill);
                    }
                    recalucateDailyBalanceDetails = true;
                } else {
                    boolean correctionRequired = false;
                    if (postingTransaction.isInterestPostingAndNotReversed()) {
                        correctionRequired = postingTransaction.hasNotAmount(interestEarnedToBePostedForPeriod);
                    } else {
                        correctionRequired = postingTransaction.hasNotAmount(interestEarnedToBePostedForPeriod.negated());
                    }
                    if (correctionRequired) {
                        boolean applyWithHoldTaxForOldTransaction = false;
                        postingTransaction.reverse();
                        SavingsAccountTransaction reversal = null;
                        if (postReversals) {
                            reversal = SavingsAccountTransaction.reversal(postingTransaction);
                        }
                        final SavingsAccountTransaction withholdTransaction = findTransactionFor(interestPostingTransactionDate,
                                withholdTransactions);
                        if (withholdTransaction != null) {
                            withholdTransaction.reverse();
                            applyWithHoldTaxForOldTransaction = true;
                        }
                        SavingsAccountTransaction newPostingTransaction;
                        if (interestEarnedToBePostedForPeriod.isGreaterThanOrEqualTo(Money.zero(currency))) {
                            newPostingTransaction = SavingsAccountTransaction.interestPosting(this, office(),
                                    interestPostingTransactionDate, interestEarnedToBePostedForPeriod,
                                    interestPostingPeriod.isUserPosting());
                        } else {
                            newPostingTransaction = SavingsAccountTransaction.overdraftInterest(this, office(),
                                    interestPostingTransactionDate, interestEarnedToBePostedForPeriod.negated(),
                                    interestPostingPeriod.isUserPosting());
                        }
                        if (backdatedTxnsAllowedTill) {
                            addTransactionToExisting(newPostingTransaction);
                            if (reversal != null) {
                                addTransactionToExisting(reversal);
                            }
                        } else {
                            addTransaction(newPostingTransaction);
                            if (reversal != null) {
                                addTransaction(reversal);
                            }
                        }
                        if (applyWithHoldTaxForOldTransaction) {
                            createWithHoldTransaction(interestEarnedToBePostedForPeriod.getAmount(), interestPostingTransactionDate,
                                    backdatedTxnsAllowedTill);
                        }
                        recalucateDailyBalanceDetails = true;
                    }
                }
            }
        }

        if (recalucateDailyBalanceDetails) {
            // no openingBalance concept supported yet but probably will to
            // allow
            // for migrations.
            Money openingAccountBalance = Money.zero(this.currency);

            if (backdatedTxnsAllowedTill) {
                if (this.summary.getLastInterestCalculationDate() == null) {
                    openingAccountBalance = Money.zero(this.currency);
                } else {
                    openingAccountBalance = Money.of(this.currency, this.summary.getRunningBalanceOnPivotDate());
                }
            }

            // update existing transactions so derived balance fields are
            // correct.
            recalculateDailyBalances(openingAccountBalance, interestPostingUpToDate, backdatedTxnsAllowedTill, postReversals);
        }

        if (!backdatedTxnsAllowedTill) {
            this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);
        } else {
            this.summary.updateSummaryWithPivotConfig(this.currency, this.savingsAccountTransactionSummaryWrapper, null,
                    this.savingsAccountTransactions);
        }
    }

    protected List<SavingsAccountTransaction> findWithHoldTransactions() {
        final List<SavingsAccountTransaction> withholdTransactions = new ArrayList<>();
        List<SavingsAccountTransaction> trans = getTransactions();
        for (final SavingsAccountTransaction transaction : trans) {
            if (transaction.isWithHoldTaxAndNotReversed()) {
                withholdTransactions.add(transaction);
            }
        }
        return withholdTransactions;
    }

    protected List<SavingsAccountTransaction> findWithHoldSavingsTransactionsWithPivotConfig() {
        final List<SavingsAccountTransaction> withholdTransactions = new ArrayList<>();
        List<SavingsAccountTransaction> trans = getSavingsAccountTransactionsWithPivotConfig();
        for (final SavingsAccountTransaction transaction : trans) {
            if (transaction.isWithHoldTaxAndNotReversed() && !transaction.isReversalTransaction()) {
                withholdTransactions.add(transaction);
            }
        }
        return withholdTransactions;
    }

    private boolean isWithHoldTaxApplicableForInterestPosting() {
        return this.withHoldTax() && this.depositAccountType().isSavingsDeposit();
    }

    protected SavingsAccountTransaction findInterestPostingTransactionFor(final LocalDate postingDate) {
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

    protected SavingsAccountTransaction findInterestPostingSavingsTransactionWithPivotConfig(final LocalDate postingDate) {
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

    protected SavingsAccountTransaction findTransactionFor(final LocalDate postingDate,
            final List<SavingsAccountTransaction> transactions) {
        SavingsAccountTransaction transaction = null;
        for (final SavingsAccountTransaction savingsAccountTransaction : transactions) {
            if (savingsAccountTransaction.occursOn(postingDate)) {
                transaction = savingsAccountTransaction;
                break;
            }
        }
        return transaction;
    }

    protected boolean createWithHoldTransaction(final BigDecimal amount, final LocalDate date, final boolean backdatedTxnsAllowedTill) {
        boolean isTaxAdded = false;
        if (this.taxGroup != null && amount.compareTo(BigDecimal.ZERO) > 0) {
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
        if (this.taxGroup != null && amount.compareTo(BigDecimal.ZERO) > 0) {
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
            final LocalDate postInterestOnDate, final boolean backdatedTxnsAllowedTill, final boolean postReversals) {

        // no openingBalance concept supported yet but probably will to allow for migrations.
        // Check global configurations and 'pivot' date is null
        Money openingAccountBalance = backdatedTxnsAllowedTill ? Money.of(this.currency, this.summary.getRunningBalanceOnPivotDate())
                : Money.zero(this.currency);

        // update existing transactions so derived balance fields are correct.
        recalculateDailyBalances(openingAccountBalance, upToInterestCalculationDate, backdatedTxnsAllowedTill, postReversals);

        final List<PostingPeriod> allPostingPeriods = new ArrayList<>();
        if (hasInterestCalculation() || hasOverdraftInterestCalculation()) {
            // 1. default to calculate interest based on entire history OR
            // 2. determine latest 'posting period' and find interest credited to that period

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
            final List<LocalDateInterval> postingPeriodIntervals = this.savingsHelper.determineInterestPostingPeriods(
                    getStartInterestCalculationDate(), upToInterestCalculationDate, postingPeriodType, financialYearBeginningMonth,
                    postedAsOnDates);

            Money periodStartingBalance;
            if (this.startInterestCalculationDate != null && !this.getStartInterestCalculationDate().equals(this.getActivationDate())) {
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
            final Collection<Long> interestPostTransactions = this.savingsHelper.fetchPostInterestTransactionIds(getId());
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

            this.savingsHelper.calculateInterestForAllPostingPeriods(this.currency, allPostingPeriods, getLockedInUntilDate(),
                    isTransferInterestToOtherAccount());

            this.summary.updateFromInterestPeriodSummaries(this.currency, allPostingPeriods);
        }

        if (backdatedTxnsAllowedTill) {
            this.summary.updateSummaryWithPivotConfig(this.currency, this.savingsAccountTransactionSummaryWrapper, null,
                    this.savingsAccountTransactions);
        } else {
            this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);
        }

        return allPostingPeriods;
    }

    private BigDecimal getEffectiveOverdraftInterestRateAsFraction(MathContext mc) {
        return this.nominalAnnualInterestRateOverdraft.divide(BigDecimal.valueOf(100L), mc);
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

    protected void recalculateDailyBalances(final Money openingAccountBalance, final LocalDate interestPostingUpToDate,
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
                if (!calculateInterest || transaction.getId() == null) {
                    transaction.setOverdraftAmount(overdraftAmount);
                } else if (!MathUtil.isEqualTo(overdraftAmount, transaction.getOverdraftAmount(this.currency))) {
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

    public SavingsAccountTransaction deposit(final SavingsAccountTransactionDTO transactionDTO, final boolean backdatedTxnsAllowedTill,
            final Long relaxingDaysConfigForPivotDate, final String refNo) {
        return deposit(transactionDTO, SavingsAccountTransactionType.DEPOSIT, backdatedTxnsAllowedTill, relaxingDaysConfigForPivotDate,
                refNo);
    }

    public SavingsAccountTransaction dividendPayout(final SavingsAccountTransactionDTO transactionDTO,
            final boolean backdatedTxnsAllowedTill, final Long relaxingDaysConfigForPivotDate) {
        String refNo = null;
        return deposit(transactionDTO, SavingsAccountTransactionType.DIVIDEND_PAYOUT, backdatedTxnsAllowedTill,
                relaxingDaysConfigForPivotDate, refNo);
    }

    public SavingsAccountTransaction deposit(final SavingsAccountTransactionDTO transactionDTO,
            final SavingsAccountTransactionType savingsAccountTransactionType, final boolean backdatedTxnsAllowedTill,
            final Long relaxingDaysConfigForPivotDate, final String refNo) {
        final String resourceTypeName = depositAccountType().resourceName();
        if (isNotActive()) {
            final String defaultUserMessage = "Transaction is not allowed. Account is not active.";
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg." + resourceTypeName + ".transaction.account.is.not.active", defaultUserMessage, "transactionDate",
                    transactionDTO.getTransactionDate().format(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (DateUtils.isDateInTheFuture(transactionDTO.getTransactionDate())) {
            final String defaultUserMessage = "Transaction date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg." + resourceTypeName + ".transaction.in.the.future",
                    defaultUserMessage, "transactionDate", transactionDTO.getTransactionDate().format(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (DateUtils.isBefore(transactionDTO.getTransactionDate(), getActivationDate())) {
            final Object[] defaultUserArgs = Arrays.asList(transactionDTO.getTransactionDate().format(transactionDTO.getFormatter()),
                    getActivationDate().format(transactionDTO.getFormatter())).toArray();
            final String defaultUserMessage = "Transaction date cannot be before accounts activation date.";
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg." + resourceTypeName + ".transaction.before.activation.date", defaultUserMessage, "transactionDate",
                    defaultUserArgs);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        validatePivotDateTransaction(transactionDTO.getTransactionDate(), backdatedTxnsAllowedTill, relaxingDaysConfigForPivotDate,
                resourceTypeName);

        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_DEPOSIT, transactionDTO.getTransactionDate());

        final Money amount = Money.of(this.currency, transactionDTO.getTransactionAmount());

        final SavingsAccountTransaction transaction = SavingsAccountTransaction.deposit(this, office(), transactionDTO.getPaymentDetail(),
                transactionDTO.getTransactionDate(), amount, savingsAccountTransactionType, refNo);

        if (backdatedTxnsAllowedTill) {
            addTransactionToExisting(transaction);
        } else {
            addTransaction(transaction);
        }

        if (this.sub_status.equals(SavingsAccountSubStatusEnum.INACTIVE.getValue())
                || this.sub_status.equals(SavingsAccountSubStatusEnum.DORMANT.getValue())) {
            this.sub_status = SavingsAccountSubStatusEnum.NONE.getValue();
        }

        if (backdatedTxnsAllowedTill) {
            this.summary.updateSummaryWithPivotConfig(this.currency, this.savingsAccountTransactionSummaryWrapper, transaction,
                    this.savingsAccountTransactions);
        }

        return transaction;
    }

    public void validatePivotDateTransaction(LocalDate transactionDate, final boolean backdatedTxnsAllowedTill,
            final Long relaxingDaysConfigForPivotDate, final String resourceTypeName) {
        if (backdatedTxnsAllowedTill) {
            if (this.getSummary().getInterestPostedTillDate() != null && DateUtils.isBefore(transactionDate,
                    getSummary().getInterestPostedTillDate().minusDays(relaxingDaysConfigForPivotDate))) {
                final Object[] defaultUserArgs = Arrays.asList(transactionDate, getActivationDate()).toArray();
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

    public LocalDate getActivationDate() {
        return this.activatedOnDate;
    }

    public AppUser getActivatedBy() {
        return this.activatedBy;
    }

    public LocalDate getWithdrawnOnDate() {
        return this.withdrawnOnDate;
    }

    public AppUser getWithdrawnBy() {
        return this.withdrawnBy;
    }

    // startInterestCalculationDate is set during migration so that there is no
    // interference with interest posting of previous system
    public LocalDate getStartInterestCalculationDate() {
        LocalDate startInterestCalculationLocalDate = null;
        if (this.startInterestCalculationDate != null) {
            startInterestCalculationLocalDate = this.startInterestCalculationDate;
        } else {
            startInterestCalculationLocalDate = getActivationDate();
        }
        return startInterestCalculationLocalDate;
    }

    public void setStartInterestCalculationDate(LocalDate startInterestCalculationDate) {
        this.startInterestCalculationDate = startInterestCalculationDate;
    }

    public SavingsAccountTransaction withdraw(final SavingsAccountTransactionDTO transactionDTO, final boolean applyWithdrawFee,
            final boolean backdatedTxnsAllowedTill, final Long relaxingDaysConfigForPivotDate, String refNo) {
        if (!isTransactionsAllowed()) {

            final String defaultUserMessage = "Transaction is not allowed. Account is not active.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.account.is.not.active",
                    defaultUserMessage, "transactionDate", transactionDTO.getTransactionDate().format(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (DateUtils.isDateInTheFuture(transactionDTO.getTransactionDate())) {
            final String defaultUserMessage = "Transaction date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.in.the.future",
                    defaultUserMessage, "transactionDate", transactionDTO.getTransactionDate().format(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (DateUtils.isBefore(transactionDTO.getTransactionDate(), getActivationDate())) {
            final Object[] defaultUserArgs = Arrays.asList(transactionDTO.getTransactionDate().format(transactionDTO.getFormatter()),
                    getActivationDate().format(transactionDTO.getFormatter())).toArray();
            final String defaultUserMessage = "Transaction date cannot be before accounts activation date.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.before.activation.date",
                    defaultUserMessage, "transactionDate", defaultUserArgs);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (isAccountLocked(transactionDTO.getTransactionDate())) {
            final String defaultUserMessage = "Withdrawal is not allowed. No withdrawals are allowed until after "
                    + getLockedInUntilDate().format(transactionDTO.getFormatter());
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg.savingsaccount.transaction.withdrawals.blocked.during.lockin.period", defaultUserMessage, "transactionDate",
                    transactionDTO.getTransactionDate().format(transactionDTO.getFormatter()),
                    getLockedInUntilDate().format(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        validatePivotDateTransaction(transactionDTO.getTransactionDate(), backdatedTxnsAllowedTill, relaxingDaysConfigForPivotDate,
                "savingsaccount");
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_WITHDRAWAL, transactionDTO.getTransactionDate());

        if (applyWithdrawFee) {
            // auto pay withdrawal fee
            payWithdrawalFee(transactionDTO.getTransactionAmount(), transactionDTO.getTransactionDate(), transactionDTO.getPaymentDetail(),
                    backdatedTxnsAllowedTill, refNo);
        }

        final Money transactionAmountMoney = Money.of(this.currency, transactionDTO.getTransactionAmount());
        final SavingsAccountTransaction transaction = SavingsAccountTransaction.withdrawal(this, office(),
                transactionDTO.getPaymentDetail(), transactionDTO.getTransactionDate(), transactionAmountMoney, refNo);

        if (backdatedTxnsAllowedTill) {
            addTransactionToExisting(transaction);
        } else {
            addTransaction(transaction);
        }

        if (this.sub_status.equals(SavingsAccountSubStatusEnum.INACTIVE.getValue())
                || this.sub_status.equals(SavingsAccountSubStatusEnum.DORMANT.getValue())) {
            this.sub_status = SavingsAccountSubStatusEnum.NONE.getValue();
        }
        if (backdatedTxnsAllowedTill) {
            this.summary.updateSummaryWithPivotConfig(this.currency, this.savingsAccountTransactionSummaryWrapper, transaction,
                    this.savingsAccountTransactions);
        }
        return transaction;
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

    private void payWithdrawalFee(final BigDecimal transactionAmount, final LocalDate transactionDate, final PaymentDetail paymentDetail,
            final boolean backdatedTxnsAllowedTill, final String refNo) {
        for (SavingsAccountCharge charge : this.charges()) {
            if (charge.isWithdrawalFee() && charge.isActive()) {

                if (charge.getFreeWithdrawalCount() == null) {
                    charge.setFreeWithdrawalCount(0);
                }

                if (charge.isEnablePaymentType() && charge.isEnableFreeWithdrawal()) { // discount transaction to
                                                                                       // specific paymentType
                    if (paymentDetail.getPaymentType().getName().equals(charge.getCharge().getPaymentType().getName())) {
                        resetFreeChargeDaysCount(charge, transactionAmount, transactionDate, refNo);
                    }
                } else if (charge.isEnablePaymentType()) { // normal charge-transaction to specific paymentType
                    if (paymentDetail.getPaymentType().getName().equals(charge.getCharge().getPaymentType().getName())) {
                        charge.updateWithdralFeeAmount(transactionAmount);
                        this.payCharge(charge, charge.getAmountOutstanding(this.getCurrency()), transactionDate, backdatedTxnsAllowedTill,
                                refNo);
                    }
                } else if (!charge.isEnablePaymentType() && charge.isEnableFreeWithdrawal()) { // discount transaction
                                                                                               // irrespective of
                                                                                               // PaymentTypes.
                    resetFreeChargeDaysCount(charge, transactionAmount, transactionDate, refNo);

                } else { // normal-withdraw
                    charge.updateWithdralFeeAmount(transactionAmount);
                    this.payCharge(charge, charge.getAmountOutstanding(this.getCurrency()), transactionDate, backdatedTxnsAllowedTill,
                            refNo);
                }
            }
        }
    }

    private void resetFreeChargeDaysCount(SavingsAccountCharge charge, final BigDecimal transactionAmount, final LocalDate transactionDate,
            final String refNo) {
        LocalDate resetDate = charge.getResetChargeDate();

        Integer restartPeriod = charge.getRestartFrequency();
        if (charge.getRestartFrequencyEnum() == 2) { // calculation for months
            LocalDate localDate = DateUtils.getBusinessLocalDate();

            LocalDate resetLocalDate;
            if (resetDate == null) {
                resetLocalDate = this.activatedOnDate;
            } else {
                resetLocalDate = resetDate;
            }

            LocalDate gapIntervalMonth = resetLocalDate.plusMonths(restartPeriod);

            YearMonth gapYearMonth = YearMonth.from(gapIntervalMonth);
            YearMonth localYearMonth = YearMonth.from(localDate);
            if (localYearMonth.isBefore(gapYearMonth)) {
                countValidation(charge, transactionAmount, transactionDate, refNo);
            } else {
                discountCharge(1, charge);
            }
        } else { // calculation for days
            long completedDays;

            if (resetDate == null) {
                completedDays = DAYS.between(DateUtils.getBusinessLocalDate(), this.activatedOnDate);

            } else {
                completedDays = DAYS.between(DateUtils.getBusinessLocalDate(), resetDate);
            }

            int totalDays = (int) completedDays;

            if (totalDays < restartPeriod) {
                countValidation(charge, transactionAmount, transactionDate, refNo);
            } else {
                discountCharge(1, charge);
            }
        }
    }

    private void countValidation(SavingsAccountCharge charge, final BigDecimal transactionAmount, final LocalDate transactionDate,
            final String refNo) {
        boolean backdatedTxnsAllowedTill = false;
        if (charge.getFreeWithdrawalCount() < charge.getFrequencyFreeWithdrawalCharge()) {
            final Integer count = charge.getFreeWithdrawalCount() + 1;
            charge.setFreeWithdrawalCount(count);
            charge.updateNoWithdrawalFee();
        } else {
            charge.updateWithdralFeeAmount(transactionAmount);
            this.payCharge(charge, charge.getAmountOutstanding(this.getCurrency()), transactionDate, backdatedTxnsAllowedTill, refNo);
        }
    }

    private void discountCharge(Integer freeWithdrawalCount, SavingsAccountCharge charge) {
        charge.setFreeWithdrawalCount(freeWithdrawalCount);
        charge.setDiscountDueDate(DateUtils.getBusinessLocalDate());
        charge.updateNoWithdrawalFee();
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

    public void validateAccountBalanceDoesNotBecomeNegative(final BigDecimal transactionAmount, final boolean isException,
            final List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions, final boolean backdatedTxnsAllowedTill) {

        List<SavingsAccountTransaction> transactionsSortedByDate = null;

        if (backdatedTxnsAllowedTill) {
            transactionsSortedByDate = retrieveSortedTransactions();
        } else {
            transactionsSortedByDate = retrieveListOfTransactions();
        }

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

            /*
             * Loop through the onHold funds and see if we need to deduct or add to minimum required balance and the
             * point in time the transaction was made:
             */
            if (depositAccountOnHoldTransactions != null) {
                for (final DepositAccountOnHoldTransaction onHoldTransaction : depositAccountOnHoldTransactions) {
                    // Compare the balance of the on hold:
                    if (!DateUtils.isAfter(onHoldTransaction.getTransactionDate(), transaction.getTransactionDate())
                            && (lastSavingsDate == null || DateUtils.isAfter(onHoldTransaction.getTransactionDate(), lastSavingsDate))) {
                        if (onHoldTransaction.getTransactionType().isHold()) {
                            minRequiredBalance = minRequiredBalance.plus(onHoldTransaction.getAmount(this.currency));
                        } else {
                            minRequiredBalance = minRequiredBalance.minus(onHoldTransaction.getAmount(this.currency));
                        }
                    }
                }
            }

            // deal with potential minRequiredBalance and
            // enforceMinRequiredBalance
            if (!isException && transaction.canProcessBalanceCheck() && !isOverdraft()) {
                if (runningBalance.minus(minRequiredBalance).isLessThanZero()) {
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
            if (runningBalance.minus(minRequiredBalance).isLessThanZero()) {
                throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), withdrawalFee, transactionAmount);
            }
        }

        if (this.getSavingsHoldAmount().compareTo(BigDecimal.ZERO) > 0) {
            if (this.enforceMinRequiredBalance) {
                if (runningBalance.minus(minRequiredBalance.plus(this.getSavingsHoldAmount())).isLessThanZero()) {
                    throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), withdrawalFee,
                            transactionAmount);
                }
            } else {
                if (runningBalance.minus(this.getSavingsHoldAmount()).isLessThanZero()) {
                    throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), withdrawalFee,
                            transactionAmount);
                }
            }
        }
    }

    public void validateAccountBalanceDoesNotBecomeNegative(final String transactionAction,
            final List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions, final boolean backdatedTxnsAllowedTill) {

        List<SavingsAccountTransaction> transactionsSortedByDate = null;
        BigDecimal transactionAmount = null;
        if (backdatedTxnsAllowedTill) {
            transactionsSortedByDate = retrieveSortedTransactions();
        } else {
            transactionsSortedByDate = retrieveListOfTransactions();
        }
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

            /*
             * Loop through the onHold funds and see if we need to deduct or add to minimum required balance and the
             * point in time the transaction was made:
             */
            if (depositAccountOnHoldTransactions != null) {
                for (final DepositAccountOnHoldTransaction onHoldTransaction : depositAccountOnHoldTransactions) {
                    // Compare the balance of the on hold:
                    if (!DateUtils.isAfter(onHoldTransaction.getTransactionDate(), transaction.getTransactionDate())
                            && (lastSavingsDate == null || DateUtils.isAfter(onHoldTransaction.getTransactionDate(), lastSavingsDate))) {
                        if (onHoldTransaction.getTransactionType().isHold()) {
                            minRequiredBalance = minRequiredBalance.plus(onHoldTransaction.getAmount(this.currency));
                        } else {
                            minRequiredBalance = minRequiredBalance.minus(onHoldTransaction.getAmount(this.currency));
                        }
                    }
                }
            }

            // enforceMinRequiredBalance
            if (transaction.canProcessBalanceCheck()) {
                if (runningBalance.minus(minRequiredBalance).isLessThanZero()) {
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
            if (runningBalance.minus(minRequiredBalance).isLessThanZero()) {
                throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), withdrawalFee, transactionAmount);
            }
        }
    }

    public void validateAccountBalanceDoesNotViolateOverdraft(final List<SavingsAccountTransaction> savingsAccountTransaction,
            final BigDecimal amountPaid) {
        if (savingsAccountTransaction != null) {
            SavingsAccountTransaction savingsAccountTransactionFirst = savingsAccountTransaction.get(0);
            if (!this.allowOverdraft) {
                if (savingsAccountTransactionFirst.getRunningBalance(this.currency).minus(amountPaid).isLessThanZero()) {
                    throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), null, amountPaid);
                }
            }
        }
    }

    protected boolean isAccountLocked(final LocalDate transactionDate) {
        return DateUtils.isBefore(transactionDate, getLockedInUntilDate());
    }

    public LocalDate getLockedInUntilDate() {
        return this.lockedInUntilDate;
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

    public Staff getSavingsOfficer() {
        return this.savingsOfficer;
    }

    public Boolean getEnforceMinRequiredBalance() {
        return this.enforceMinRequiredBalance;
    }

    public Boolean getLienAllowed() {
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

    public GroupSavingsIndividualMonitoring getGsim() {
        return gsim;
    }

    public Long getSavingsProductId() {
        return this.savingsProduct().getId();
    }

    public void setGsim(GroupSavingsIndividualMonitoring gsim) {
        this.gsim = gsim;
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
        } else if (latestHistoryRecord != null && this.savingsOfficer.identifiedBy(newSavingsOfficer)) {
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

    public LocalDate getSubmittedOnDate() {
        return submittedOnDate;
    }

    public LocalDate getApprovedOnDate() {
        return approvedOnDate;
    }

    public LocalDate getRejectedOnDate() {
        return rejectedOnDate;
    }

    public AppUser getRejectedBy() {
        return this.rejectedBy;
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

    public MonetaryCurrency getCurrency() {
        return this.currency;
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

    public Client getClient() {
        return this.client;
    }

    public BigDecimal getNominalAnnualInterestRate() {
        return this.nominalAnnualInterestRate;
    }

    public Integer getInterestCompoundingPeriodType() {
        return this.interestCompoundingPeriodType;
    }

    public Integer getInterestPostingPeriodType() {
        return this.interestPostingPeriodType;
    }

    public Integer getInterestCalculationType() {
        return this.interestCalculationType;
    }

    public BigDecimal getNominalAnnualInterestRateOverdraft() {
        return this.nominalAnnualInterestRateOverdraft;
    }

    public Map<String, Object> approveApplication(final AppUser currentUser, final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.approvalAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.hasStateOf(currentStatus)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.approvedOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.submittedandpendingapproval.state");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        this.status = SavingsAccountStatusType.APPROVED.getValue();
        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(this.status));

        // only do below if status has changed in the 'approval' case
        final LocalDate approvedOn = command.localDateValueOfParameterNamed(SavingsApiConstants.approvedOnDateParamName);
        final String approvedOnDateChange = command.stringValueOfParameterNamed(SavingsApiConstants.approvedOnDateParamName);

        this.approvedOnDate = approvedOn;
        this.approvedBy = currentUser;
        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.approvedOnDateParamName, approvedOnDateChange);

        final LocalDate submittalDate = getSubmittedOnDate();
        if (DateUtils.isBefore(approvedOn, submittalDate)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String submittalDateAsString = formatter.format(submittalDate);

            baseDataValidator.reset().parameter(SavingsApiConstants.approvedOnDateParamName).value(submittalDateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.submittal.date");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        if (DateUtils.isAfterBusinessDate(approvedOn)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.approvedOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.a.future.date");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
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

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
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

    private LocalDate findLatestAnnualFeeTransactionDueDate() {
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

    public void validateAccountBalanceDoesNotBecomeNegativeMinimal(final BigDecimal transactionAmount, final boolean isException) {
        // final List<SavingsAccountTransaction> transactionsSortedByDate = retrieveListOfTransactions();
        Money runningBalance = this.summary.getAccountBalance(getCurrency());
        Money minRequiredBalance = minRequiredBalanceDerived(getCurrency());
        final BigDecimal withdrawalFee = null;

        // check last txn date

        // In overdraft cases, minRequiredBalance can be in violation after interest posting
        // and should be checked after processing all transactions
        if (!isOverdraft()) {
            if (runningBalance.minus(minRequiredBalance).isLessThanZero()) {
                throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), withdrawalFee, transactionAmount);
            }
        }

        if (this.getSavingsHoldAmount().compareTo(BigDecimal.ZERO) > 0) {
            if (runningBalance.minus(this.getSavingsHoldAmount()).minus(minRequiredBalance).isLessThanZero()) {
                throw new InsufficientAccountBalanceException("transactionAmount", getAccountBalance(), withdrawalFee, transactionAmount);
            }
        }
    }

    public Map<String, Object> rejectApplication(final AppUser currentUser, final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.rejectAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.rejectedOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.submittedandpendingapproval.state");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        this.status = SavingsAccountStatusType.REJECTED.getValue();
        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(this.status));

        final LocalDate rejectedOn = command.localDateValueOfParameterNamed(SavingsApiConstants.rejectedOnDateParamName);
        final String rejectedOnAsString = command.stringValueOfParameterNamed(SavingsApiConstants.rejectedOnDateParamName);

        this.rejectedOnDate = rejectedOn;
        this.rejectedBy = currentUser;
        this.withdrawnOnDate = null;
        this.withdrawnBy = null;
        this.closedOnDate = rejectedOn;
        this.closedBy = currentUser;

        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.rejectedOnDateParamName, rejectedOnAsString);
        actualChanges.put(SavingsApiConstants.closedOnDateParamName, rejectedOnAsString);

        final LocalDate submittalDate = getSubmittedOnDate();
        if (DateUtils.isBefore(rejectedOn, submittalDate)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String submittalDateAsString = formatter.format(submittalDate);

            baseDataValidator.reset().parameter(SavingsApiConstants.rejectedOnDateParamName).value(submittalDateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.submittal.date");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        if (DateUtils.isAfterBusinessDate(rejectedOn)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.rejectedOnDateParamName).value(rejectedOn)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.a.future.date");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_APPLICATION_REJECTED, rejectedOn);

        return actualChanges;
    }

    public Map<String, Object> applicantWithdrawsFromApplication(final AppUser currentUser, final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.withdrawnByApplicantAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.withdrawnOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.submittedandpendingapproval.state");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        this.status = SavingsAccountStatusType.WITHDRAWN_BY_APPLICANT.getValue();
        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(this.status));

        final LocalDate withdrawnOn = command.localDateValueOfParameterNamed(SavingsApiConstants.withdrawnOnDateParamName);
        final String withdrawnOnAsString = command.stringValueOfParameterNamed(SavingsApiConstants.withdrawnOnDateParamName);

        this.rejectedOnDate = null;
        this.rejectedBy = null;
        this.withdrawnOnDate = withdrawnOn;
        this.withdrawnBy = currentUser;
        this.closedOnDate = withdrawnOn;
        this.closedBy = currentUser;

        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.withdrawnOnDateParamName, withdrawnOnAsString);
        actualChanges.put(SavingsApiConstants.closedOnDateParamName, withdrawnOnAsString);

        final LocalDate submittalDate = getSubmittedOnDate();
        if (DateUtils.isBefore(withdrawnOn, submittalDate)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String submittalDateAsString = formatter.format(submittalDate);

            baseDataValidator.reset().parameter(SavingsApiConstants.withdrawnOnDateParamName).value(submittalDateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.submittal.date");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        if (DateUtils.isAfterBusinessDate(withdrawnOn)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.withdrawnOnDateParamName).value(withdrawnOn)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.a.future.date");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_APPLICATION_WITHDRAWAL_BY_CUSTOMER, withdrawnOn);

        return actualChanges;
    }

    public Map<String, Object> activate(final AppUser currentUser, final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(depositAccountType().resourceName() + SavingsApiConstants.activateAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.APPROVED.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.approved.state");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final LocalDate activationDate = command.localDateValueOfParameterNamed(SavingsApiConstants.activatedOnDateParamName);

        this.status = SavingsAccountStatusType.ACTIVE.getValue();
        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(this.status));
        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.activatedOnDateParamName, activationDate.format(fmt));

        this.rejectedOnDate = null;
        this.rejectedBy = null;
        this.withdrawnOnDate = null;
        this.withdrawnBy = null;
        this.closedOnDate = null;
        this.closedBy = null;
        this.activatedOnDate = activationDate;
        this.activatedBy = currentUser;
        this.lockedInUntilDate = calculateDateAccountIsLockedUntil(getActivationDate());

        /*
         * if (annualFeeSettingsSet()) { updateToNextAnnualFeeDueDateFrom(getActivationLocalDate()); }
         */
        if (this.client != null && this.client.isActivatedAfter(activationDate)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String dateAsString = formatter.format(this.client.getActivationDate());
            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName).value(dateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.client.activation.date");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (this.group != null && this.group.isActivatedAfter(activationDate)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String dateAsString = formatter.format(this.client.getActivationDate());
            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName).value(dateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.group.activation.date");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        final LocalDate approvalDate = getApprovedOnDate();
        if (DateUtils.isBefore(activationDate, approvalDate)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String dateAsString = formatter.format(approvalDate);

            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName).value(dateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.approval.date");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (DateUtils.isAfterBusinessDate(activationDate)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName).value(activationDate)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.a.future.date");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_ACTIVATE, activationDate);

        return actualChanges;
    }

    public void processAccountUponActivation(final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth) {
        // update annual fee due date
        for (SavingsAccountCharge charge : this.charges()) {
            charge.updateToNextDueDateFrom(getActivationDate());
        }

        // auto pay the activation time charges (No need of checking the pivot date config)
        this.payActivationCharges(isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, false);
        // TODO : AA add activation charges to actual changes list
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
        this.lockedInUntilDate = calculateDateAccountIsLockedUntil(getActivationDate());
    }

    private void payActivationCharges(final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth,
            final boolean backdatedTxnsAllowedTill) {
        boolean isSavingsChargeApplied = false;
        boolean postReversals = false;
        UUID refNo = UUID.randomUUID();
        for (SavingsAccountCharge savingsAccountCharge : this.charges()) {
            if (savingsAccountCharge.isSavingsActivation()) {
                isSavingsChargeApplied = true;
                payCharge(savingsAccountCharge, savingsAccountCharge.getAmountOutstanding(getCurrency()), getActivationDate(),
                        backdatedTxnsAllowedTill, refNo.toString());
            }
        }

        if (isSavingsChargeApplied) {
            final MathContext mc = MathContext.DECIMAL64;
            boolean isInterestTransfer = false;
            LocalDate postInterestAsOnDate = null;
            if (this.isBeforeLastPostingPeriod(getActivationDate(), backdatedTxnsAllowedTill)) {
                final LocalDate today = DateUtils.getBusinessLocalDate();
                this.postInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
                        postInterestAsOnDate, backdatedTxnsAllowedTill, postReversals);
            } else {
                final LocalDate today = DateUtils.getBusinessLocalDate();
                this.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                        financialYearBeginningMonth, postInterestAsOnDate, backdatedTxnsAllowedTill, postReversals);
            }
        }
    }

    public Map<String, Object> close(final AppUser currentUser, final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.closeAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.in.active.state");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final LocalDate closedDate = command.localDateValueOfParameterNamed(SavingsApiConstants.closedOnDateParamName);

        if (DateUtils.isBefore(closedDate, getActivationDate())) {
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
        if (savingsAccountTransactions.size() > 0) {
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

    protected void validateActivityNotBeforeClientOrGroupTransferDate(final SavingsEvent event, final LocalDate activityDate) {
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

    private LocalDate calculateDateAccountIsLockedUntil(final LocalDate activationLocalDate) {

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

    public boolean isWithdrawalFeeApplicableForTransfer() {
        return this.withdrawalFeeApplicableForTransfer;
    }

    public void activateAccountBasedOnBalance() {
        if (SavingsAccountStatusType.fromInt(this.status).isClosed() && !this.summary.getAccountBalance(getCurrency()).isZero()) {
            this.status = SavingsAccountStatusType.ACTIVE.getValue();
        }
    }

    public LocalDate getClosedOnDate() {
        return this.closedOnDate;
    }

    public AppUser getClosedBy() {
        return this.closedBy;
    }

    public SavingsAccountSummary getSummary() {
        return this.summary;
    }

    public List<SavingsAccountTransaction> getTransactions() {
        return this.transactions;
    }

    public void addTransaction(final SavingsAccountTransaction transaction) {
        this.transactions.add(transaction);
    }

    public void addTransactionToExisting(final SavingsAccountTransaction transaction) {
        this.savingsAccountTransactions.add(transaction);
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

    public void removeCharge(final SavingsAccountCharge charge) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        if (isClosed()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("delete.transaction.invalid.account.is.closed");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (isActive() || isApproved()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("delete.transaction.invalid.account.is.active");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        this.charges.remove(charge);
    }

    public void waiveCharge(final Long savingsAccountChargeId, final boolean backdatedTxnsAllowedTill) {
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

        final SavingsAccountCharge savingsAccountCharge = getCharge(savingsAccountChargeId);

        if (savingsAccountCharge.isNotActive()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("charge.is.not.active");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (savingsAccountCharge.isWithdrawalFee()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.waiver.of.withdrawal.fee.not.supported");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        // validate charge is not already paid or waived
        if (savingsAccountCharge.isWaived()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.charge.is.already.waived");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        } else if (savingsAccountCharge.isPaid()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.charge.is.paid");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        // waive charge
        final Money amountWaived = savingsAccountCharge.waive(getCurrency());
        handleWaiverChargeTransactions(savingsAccountCharge, amountWaived, backdatedTxnsAllowedTill);
    }

    public void addCharge(final DateTimeFormatter formatter, final SavingsAccountCharge savingsAccountCharge,
            final Charge chargeDefinition) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        if (isClosed()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.is.closed");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (!hasCurrencyCodeOf(chargeDefinition.getCurrencyCode())) {
            baseDataValidator.reset()
                    .failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.currency.and.charge.currency.not.same");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        final LocalDate chargeDueDate = savingsAccountCharge.getDueDate();

        if (savingsAccountCharge.isOnSpecifiedDueDate()) {
            if (DateUtils.isBefore(chargeDueDate, getActivationDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(getActivationDate().format(formatter))
                        .failWithCodeNoParameterAddedToErrorCode("before.activationDate");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            } else if (DateUtils.isBefore(chargeDueDate, getSubmittedOnDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(getSubmittedOnDate().format(formatter))
                        .failWithCodeNoParameterAddedToErrorCode("before.submittedOnDate");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (savingsAccountCharge.isSavingsActivation() && !(isSubmittedAndPendingApproval() || (isApproved() && isNotActive()))) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.valid.account.status.cannot.add.activation.time.charge");
            throw new PlatformApiDataValidationException(dataValidationErrors);
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
                savingsAccountCharge.updateToNextDueDateFrom(getActivationDate());
            } else if (isApproved()) {
                savingsAccountCharge.updateToNextDueDateFrom(getApprovedOnDate());
            }
        }

        // activation charge and withdrawal charges not required this validation
        if (savingsAccountCharge.isOnSpecifiedDueDate()) {
            validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_APPLY_CHARGE, chargeDueDate);
        }

        // add new charge to savings account
        this.charges.add(savingsAccountCharge);

    }

    private boolean isAnnualFeeExists() {
        for (SavingsAccountCharge charge : this.charges()) {
            if (charge.isAnnualFee()) {
                return true;
            }
        }
        return false;
    }

    public SavingsAccountTransaction payCharge(final SavingsAccountCharge savingsAccountCharge, final BigDecimal amountPaid,
            final LocalDate transactionDate, final DateTimeFormatter formatter, final boolean backdatedTxnsAllowedTill,
            final String refNo) {
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

        if (savingsAccountCharge.isNotActive()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("charge.is.not.active");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (DateUtils.isBefore(transactionDate, getActivationDate())) {
            baseDataValidator.reset().parameter(dueAsOfDateParamName).value(getActivationDate().format(formatter))
                    .failWithCodeNoParameterAddedToErrorCode("transaction.before.activationDate");
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (DateUtils.isDateInTheFuture(transactionDate)) {
            baseDataValidator.reset().parameter(dueAsOfDateParamName).value(transactionDate.format(formatter))
                    .failWithCodeNoParameterAddedToErrorCode("transaction.is.futureDate");
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (savingsAccountCharge.isSavingsActivation()) {
            baseDataValidator.reset()
                    .failWithCodeNoParameterAddedToErrorCode("transaction.not.valid.cannot.pay.activation.time.charge.is.automated");
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (savingsAccountCharge.isAnnualFee()) {
            final LocalDate annualFeeDueDate = savingsAccountCharge.getDueDate();
            if (annualFeeDueDate == null) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("no.annualfee.settings");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }

            if (!DateUtils.isEqual(annualFeeDueDate, transactionDate)) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("invalid.date");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }

            LocalDate currentAnnualFeeNextDueDate = findLatestAnnualFeeTransactionDueDate();
            if (DateUtils.isEqual(currentAnnualFeeNextDueDate, transactionDate)) {
                baseDataValidator.reset().parameter("dueDate").value(transactionDate.format(formatter))
                        .failWithCodeNoParameterAddedToErrorCode("transaction.exists.on.date");

                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        // validate charge is not already paid or waived
        if (savingsAccountCharge.isWaived()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.charge.is.already.waived");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        } else if (savingsAccountCharge.isPaid()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.charge.is.paid");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        final Money chargePaid = Money.of(currency, amountPaid);
        if (!savingsAccountCharge.getAmountOutstanding(getCurrency()).isGreaterThanOrEqualTo(chargePaid)) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.charge.amount.paid.in.access");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        return this.payCharge(savingsAccountCharge, chargePaid, transactionDate, backdatedTxnsAllowedTill, refNo);
    }

    public SavingsAccountTransaction payCharge(final SavingsAccountCharge savingsAccountCharge, final Money amountPaid,
            final LocalDate transactionDate, final boolean backdatedTxnsAllowedTill, String refNo) {
        savingsAccountCharge.pay(getCurrency(), amountPaid);
        return handlePayChargeTransactions(savingsAccountCharge, amountPaid, transactionDate, backdatedTxnsAllowedTill, refNo);
    }

    private SavingsAccountTransaction handlePayChargeTransactions(SavingsAccountCharge savingsAccountCharge, Money transactionAmount,
            final LocalDate transactionDate, final boolean backdatedTxnsAllowedTill, final String refNo) {
        SavingsAccountTransaction chargeTransaction;

        if (savingsAccountCharge.isWithdrawalFee()) {
            chargeTransaction = SavingsAccountTransaction.withdrawalFee(this, office(), transactionDate, transactionAmount, refNo);
        } else if (savingsAccountCharge.isAnnualFee()) {
            chargeTransaction = SavingsAccountTransaction.annualFee(this, office(), transactionDate, transactionAmount);
        } else {
            chargeTransaction = SavingsAccountTransaction.charge(this, office(), transactionDate, transactionAmount);
        }

        handleChargeTransactions(savingsAccountCharge, chargeTransaction, backdatedTxnsAllowedTill);
        return chargeTransaction;
    }

    private void handleWaiverChargeTransactions(SavingsAccountCharge savingsAccountCharge, Money transactionAmount,
            boolean backdatedTxnsAllowedTill) {
        final SavingsAccountTransaction chargeTransaction = SavingsAccountTransaction.waiver(this, office(),
                DateUtils.getBusinessLocalDate(), transactionAmount);
        handleChargeTransactions(savingsAccountCharge, chargeTransaction, backdatedTxnsAllowedTill);
    }

    private void handleChargeTransactions(final SavingsAccountCharge savingsAccountCharge, final SavingsAccountTransaction transaction,
            final boolean backdatedTxnsAllowedTill) {
        // Provide a link between transaction and savings charge for which
        // amount is waived.
        final SavingsAccountChargePaidBy chargePaidBy = SavingsAccountChargePaidBy.instance(transaction, savingsAccountCharge,
                transaction.getAmount(this.currency).getAmount());
        transaction.getSavingsAccountChargesPaid().add(chargePaidBy);
        if (backdatedTxnsAllowedTill) {
            this.savingsAccountTransactions.add(transaction);
            this.summary.updateSummaryWithPivotConfig(this.currency, this.savingsAccountTransactionSummaryWrapper, transaction,
                    this.savingsAccountTransactions);
        } else {
            this.transactions.add(transaction);
        }
    }

    private SavingsAccountCharge getCharge(final Long savingsAccountChargeId) {
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
        return getActivationDate() == null ? getSubmittedOnDate() : getActivationDate();
    }

    public DepositAccountType depositAccountType() {
        return DepositAccountType.fromInt(depositType);
    }

    protected boolean isTransferInterestToOtherAccount() {
        return false;
    }

    public boolean accountSubmittedAndActivationOnSameDate() {
        return getActivationDate() != null && DateUtils.isEqual(getActivationDate(), getSubmittedOnDate());

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

        SavingsPostingInterestPeriodType savingsPostingInterestPeriodType = SavingsPostingInterestPeriodType
                .fromInt(interestPostingPeriodType);
        SavingsCompoundingInterestPeriodType savingsCompoundingInterestPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(interestCompoundingPeriodType);

        if (postingtoCompoundMap.get(savingsPostingInterestPeriodType) == null) {
            baseDataValidator.failWithCodeNoParameterAddedToErrorCode("posting.period.type.is.less.than.compound.period.type",
                    savingsPostingInterestPeriodType.name(), savingsCompoundingInterestPeriodType.name());

        }
    }

    public AppUser getSubmittedBy() {
        return this.submittedBy;
    }

    public AppUser getApprovedBy() {
        return this.approvedBy;
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
        if (DateUtils.isDateInTheFuture(transactionDate) || DateUtils.isBefore(transactionDate, getActivationDate())) {
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

    public String getAccountNumber() {
        return this.accountNumber;
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

    public TaxGroup getTaxGroup() {
        return this.taxGroup;
    }

    public boolean withHoldTax() {
        return this.withHoldTax;
    }

    public void setWithHoldTax(boolean withHoldTax) {
        this.withHoldTax = withHoldTax;
    }

    protected boolean applyWithholdTaxForDepositAccounts(final LocalDate interestPostingUpToDate, boolean recalucateDailyBalance,
            final boolean backdatedTxnsAllowedTill) {
        final List<SavingsAccountTransaction> withholdTransactions = findWithHoldTransactions();
        SavingsAccountTransaction withholdTransaction = findTransactionFor(interestPostingUpToDate, withholdTransactions);
        final BigDecimal totalInterestPosted = this.savingsAccountTransactionSummaryWrapper.calculateTotalInterestPosted(this.currency,
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

    public void setSubStatusInactive(final boolean backdatedTxnsAllowedTill) {
        this.sub_status = SavingsAccountSubStatusEnum.INACTIVE.getValue();
        LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        for (SavingsAccountCharge charge : this.charges()) {
            if (charge.isSavingsNoActivity() && charge.isActive()) {
                charge.updateWithdralFeeAmount(this.getAccountBalance());
                UUID refNo = UUID.randomUUID();
                this.payCharge(charge, charge.getAmountOutstanding(this.getCurrency()), transactionDate, backdatedTxnsAllowedTill,
                        refNo.toString());
            }
        }
        boolean postReversals = false;
        recalculateDailyBalances(Money.zero(this.currency), transactionDate, backdatedTxnsAllowedTill, postReversals);
        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);
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
        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);
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
        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, transactions);
    }

    public void updateReason(final String reasonForBlock) {
        this.reasonForBlock = reasonForBlock;
    }

    public Map<String, Object> block() {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(depositAccountType().resourceName() + SavingsApiConstants.blockAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.statusParamName)
                    .failWithCodeNoParameterAddedToErrorCode(SavingsApiConstants.ERROR_MSG_SAVINGS_ACCOUNT_NOT_ACTIVE);

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        this.sub_status = SavingsAccountSubStatusEnum.BLOCK.getValue();
        actualChanges.put(SavingsApiConstants.subStatusParamName, SavingsEnumerations.subStatus(this.sub_status));

        return actualChanges;
    }

    public Map<String, Object> unblock() {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(depositAccountType().resourceName() + SavingsApiConstants.unblockAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.statusParamName)
                    .failWithCodeNoParameterAddedToErrorCode(SavingsApiConstants.ERROR_MSG_SAVINGS_ACCOUNT_NOT_ACTIVE);

        }

        final SavingsAccountSubStatusEnum currentSubStatus = SavingsAccountSubStatusEnum.fromInt(this.sub_status);
        if (!SavingsAccountSubStatusEnum.BLOCK.hasStateOf(currentSubStatus)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.subStatusParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.blocked.state");
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        this.sub_status = SavingsAccountSubStatusEnum.NONE.getValue();
        actualChanges.put(SavingsApiConstants.subStatusParamName, SavingsEnumerations.subStatus(this.sub_status));
        return actualChanges;
    }

    public Map<String, Object> blockCredits(Integer currentSubstatus) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(depositAccountType().resourceName() + SavingsApiConstants.blockCreditsAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.statusParamName)
                    .failWithCodeNoParameterAddedToErrorCode(SavingsApiConstants.ERROR_MSG_SAVINGS_ACCOUNT_NOT_ACTIVE);
        }
        if (SavingsAccountSubStatusEnum.BLOCK.hasStateOf(SavingsAccountSubStatusEnum.fromInt(currentSubstatus))) {

            baseDataValidator.reset().parameter(SavingsApiConstants.subStatusParamName)
                    .value(SavingsAccountSubStatusEnum.fromInt(currentSubstatus)).failWithCodeNoParameterAddedToErrorCode("currently.set");
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        if (SavingsAccountSubStatusEnum.BLOCK_DEBIT.hasStateOf(SavingsAccountSubStatusEnum.fromInt(currentSubstatus))) {
            this.sub_status = SavingsAccountSubStatusEnum.BLOCK.getValue();
        } else {
            this.sub_status = SavingsAccountSubStatusEnum.BLOCK_CREDIT.getValue();
        }
        actualChanges.put(SavingsApiConstants.subStatusParamName, SavingsEnumerations.subStatus(this.sub_status));

        return actualChanges;
    }

    public Map<String, Object> unblockCredits() {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(depositAccountType().resourceName() + SavingsApiConstants.unblockCreditsAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(SavingsApiConstants.ERROR_MSG_SAVINGS_ACCOUNT_NOT_ACTIVE);
        }

        final SavingsAccountSubStatusEnum currentSubStatus = SavingsAccountSubStatusEnum.fromInt(this.sub_status);
        if (!(SavingsAccountSubStatusEnum.BLOCK_CREDIT.hasStateOf(currentSubStatus)
                || SavingsAccountSubStatusEnum.BLOCK.hasStateOf(currentSubStatus))) {
            baseDataValidator.reset().parameter(SavingsApiConstants.statusParamName)
                    .failWithCodeNoParameterAddedToErrorCode("credits.are.not.blocked");
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        if (SavingsAccountSubStatusEnum.BLOCK.hasStateOf(currentSubStatus)) {
            this.sub_status = SavingsAccountSubStatusEnum.BLOCK_DEBIT.getValue();
        } else {
            this.sub_status = SavingsAccountSubStatusEnum.NONE.getValue();
        }
        actualChanges.put(SavingsApiConstants.subStatusParamName, SavingsEnumerations.subStatus(this.sub_status));
        return actualChanges;
    }

    public Map<String, Object> blockDebits(Integer currentSubstatus) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(depositAccountType().resourceName() + SavingsApiConstants.blockDebitsAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.statusParamName)
                    .failWithCodeNoParameterAddedToErrorCode(SavingsApiConstants.ERROR_MSG_SAVINGS_ACCOUNT_NOT_ACTIVE);

        }
        if (SavingsAccountSubStatusEnum.BLOCK.hasStateOf(SavingsAccountSubStatusEnum.fromInt(currentSubstatus))) {

            baseDataValidator.reset().parameter(SavingsApiConstants.subStatusParamName)
                    .value(SavingsAccountSubStatusEnum.fromInt(currentSubstatus)).failWithCodeNoParameterAddedToErrorCode("currently.set");
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        if (SavingsAccountSubStatusEnum.BLOCK_CREDIT.hasStateOf(SavingsAccountSubStatusEnum.fromInt(currentSubstatus))) {
            this.sub_status = SavingsAccountSubStatusEnum.BLOCK.getValue();
        } else {
            this.sub_status = SavingsAccountSubStatusEnum.BLOCK_DEBIT.getValue();
        }
        actualChanges.put(SavingsApiConstants.subStatusParamName, SavingsEnumerations.subStatus(this.sub_status));

        return actualChanges;
    }

    public Map<String, Object> unblockDebits() {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(depositAccountType().resourceName() + SavingsApiConstants.unblockDebitsAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.statusParamName)
                    .failWithCodeNoParameterAddedToErrorCode(SavingsApiConstants.ERROR_MSG_SAVINGS_ACCOUNT_NOT_ACTIVE);

        }

        final SavingsAccountSubStatusEnum currentSubStatus = SavingsAccountSubStatusEnum.fromInt(this.sub_status);
        if (!(SavingsAccountSubStatusEnum.BLOCK_DEBIT.hasStateOf(currentSubStatus)
                || SavingsAccountSubStatusEnum.BLOCK.hasStateOf(currentSubStatus))) {
            baseDataValidator.reset().parameter(SavingsApiConstants.subStatusParamName)
                    .failWithCodeNoParameterAddedToErrorCode("debits.are.not.blocked");
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        if (SavingsAccountSubStatusEnum.BLOCK.hasStateOf(currentSubStatus)) {
            this.sub_status = SavingsAccountSubStatusEnum.BLOCK_CREDIT.getValue();
        } else {
            this.sub_status = SavingsAccountSubStatusEnum.NONE.getValue();
        }
        actualChanges.put(SavingsApiConstants.subStatusParamName, SavingsEnumerations.subStatus(this.sub_status));
        return actualChanges;
    }

    public SavingsAccountStatusType getStatus() {
        return SavingsAccountStatusType.fromInt(status);
    }

    public Integer getSubStatus() {
        return this.sub_status;
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
        if (transactionsSortedByDate.size() > 0) {
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
        if (transactionsSortedByDate.size() > 0) {
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

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }

    private boolean isOverdraft() {
        return allowOverdraft;
    }

    public Long getGroupId() {
        return this.groupId();
    }

    public Integer getInterestCalculationDaysInYearType() {
        return this.interestCalculationDaysInYearType;
    }

    public BigDecimal getMinRequiredOpeningBalance() {
        return this.minRequiredOpeningBalance;
    }

    public Integer getLockinPeriodFrequency() {
        return this.lockinPeriodFrequency;
    }

    public Integer getLockinPeriodFrequencyType() {
        return this.lockinPeriodFrequencyType;
    }

    public boolean isWithdrawalFeeForTransfer() {
        return this.withdrawalFeeApplicableForTransfer;
    }

    public boolean isAllowOverdraft() {
        return this.allowOverdraft;
    }

    public BigDecimal getOverdraftLimit() {
        return this.overdraftLimit;
    }

    public BigDecimal getMinOverdraftForInterestCalculation() {
        return this.minOverdraftForInterestCalculation;
    }

    public Integer getDepositType() {
        return this.depositType;
    }

    public BigDecimal getMinRequiredBalance() {
        return this.minRequiredBalance;
    }

    public boolean isEnforceMinRequiredBalance() {
        return this.enforceMinRequiredBalance;
    }

    public BigDecimal getMaxAllowedLienLimit() {
        return this.maxAllowedLienLimit;
    }

    public boolean isLienAllowed() {
        return this.lienAllowed;
    }

    public BigDecimal getMinBalanceForInterestCalculation() {
        return this.minBalanceForInterestCalculation;
    }

    public int getVersion() {
        return this.version;
    }

    public boolean isWithHoldTax() {
        return this.withHoldTax;
    }

    public List<SavingsAccountTransactionDetailsForPostingPeriod> toSavingsAccountTransactionDetailsForPostingPeriodList(
            List<SavingsAccountTransaction> transactions) {
        return transactions.stream()
                .map(transaction -> transaction.toSavingsAccountTransactionDetailsForPostingPeriod(this.currency, this.allowOverdraft))
                .toList();
    }
}
