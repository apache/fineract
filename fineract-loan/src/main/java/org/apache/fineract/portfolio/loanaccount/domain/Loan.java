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
package org.apache.fineract.portfolio.loanaccount.domain;

import static org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType.PROGRESSIVE;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.configuration.service.TemporaryConfigurationServiceContainer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.security.service.RandomPasswordGenerator;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.service.HolidayUtil;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.calendar.data.CalendarHistoryDataWrapper;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarHistory;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarWeekDaysType;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.collateral.domain.LoanCollateral;
import org.apache.fineract.portfolio.common.domain.DayOfWeekType;
import org.apache.fineract.portfolio.common.domain.NthDayType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateDTO;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.data.OutstandingAmountsDTO;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleDTO;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanSupportedInterestRefundTypes;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.rate.domain.Rate;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecks;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Table(name = "m_loan", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_no" }, name = "loan_account_no_UNIQUE"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "loan_externalid_UNIQUE") })
@Getter
public class Loan extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    public static final String RECALCULATE_LOAN_SCHEDULE = "recalculateLoanSchedule";
    public static final String EXTERNAL_ID = "externalId";
    public static final String DATE_FORMAT = "dateFormat";
    public static final String LOCALE = "locale";
    public static final String EXPECTED_DISBURSEMENT_DATE = "expectedDisbursementDate";
    public static final String PARAM_STATUS = "status";
    public static final String REJECTED_ON_DATE = "rejectedOnDate";
    public static final String CLOSED_ON_DATE = "closedOnDate";
    public static final String EVENT_DATE = "eventDate";
    public static final String WITHDRAWN_ON_DATE = "withdrawnOnDate";
    public static final String APPROVED_ON_DATE = "approvedOnDate";
    public static final String ACTUAL_DISBURSEMENT_DATE = "actualDisbursementDate";
    public static final String INTEREST = "interest";
    public static final String PENALTY = "penalty";
    public static final String TRANSACTION_DATE = "transactionDate";
    public static final String WRITTEN_OFF_ON_DATE = "writtenOffOnDate";
    public static final String FEE = "fee";
    public static final String PENALTIES = "penalties";
    public static final String EARLIEST_UNPAID_DATE = "earliest-unpaid-date";
    public static final String NEXT_UNPAID_DUE_DATE = "next-unpaid-due-date";

    @Version
    int version;

    @Setter()
    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    private String accountNumber;

    @Setter()
    @Column(name = "external_id")
    private ExternalId externalId;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Setter()
    @ManyToOne
    @JoinColumn(name = "glim_id")
    private GroupLoanIndividualMonitoringAccount glim;

    @Column(name = "loan_type_enum", nullable = false)
    private Integer loanType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "fund_id")
    private Fund fund;

    @Setter
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loan_officer_id")
    private Staff loanOfficer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loanpurpose_cv_id")
    private CodeValue loanPurpose;

    @Column(name = "loan_transaction_strategy_code", nullable = false)
    private String transactionProcessingStrategyCode;

    @Column(name = "loan_transaction_strategy_name")
    private String transactionProcessingStrategyName;

    // TODO FINERACT-1932-Fineract modularization: Move to fineract-progressive-loan module after removing association
    // from Loan entity
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LoanPaymentAllocationRule> paymentAllocationRules = new ArrayList<>();

    // TODO FINERACT-1932-Fineract modularization: Move to fineract-progressive-loan module after removing association
    // from Loan entity
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LoanCreditAllocationRule> creditAllocationRules = new ArrayList<>();

    @Embedded
    private LoanProductRelatedDetail loanRepaymentScheduleDetail;

    @Setter()
    @Column(name = "term_frequency", nullable = false)
    private Integer termFrequency;

    @Setter()
    @Column(name = "term_period_frequency_enum", nullable = false)
    private Integer termPeriodFrequencyType;

    @Setter(AccessLevel.PACKAGE)
    @Column(name = "loan_status_id", nullable = false)
    private Integer loanStatus;

    @Setter()
    @Column(name = "sync_disbursement_with_meeting")
    private Boolean syncDisbursementWithMeeting;

    // loan application states
    @Setter()
    @Column(name = "submittedon_date")
    private LocalDate submittedOnDate;

    @Setter()
    @Column(name = "rejectedon_date")
    private LocalDate rejectedOnDate;

    @Setter()
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "rejectedon_userid")
    private AppUser rejectedBy;

    @Setter()
    @Column(name = "withdrawnon_date")
    private LocalDate withdrawnOnDate;

    @Setter()
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "withdrawnon_userid")
    private AppUser withdrawnBy;

    @Setter()
    @Column(name = "approvedon_date")
    private LocalDate approvedOnDate;

    @Setter()
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "approvedon_userid")
    private AppUser approvedBy;

    @Setter()
    @Column(name = "expected_disbursedon_date")
    private LocalDate expectedDisbursementDate;

    @Setter()
    @Column(name = "disbursedon_date")
    private LocalDate actualDisbursementDate;

    @Setter()
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursedon_userid")
    private AppUser disbursedBy;

    @Setter()
    @Column(name = "closedon_date")
    private LocalDate closedOnDate;

    @Setter()
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "closedon_userid")
    private AppUser closedBy;

    @Setter
    @Column(name = "writtenoffon_date")
    private LocalDate writtenOffOnDate;

    @Setter
    @Column(name = "rescheduledon_date")
    private LocalDate rescheduledOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "rescheduledon_userid")
    private AppUser rescheduledByUser;

    @Column(name = "expected_maturedon_date")
    private LocalDate expectedMaturityDate;

    @Setter()
    @Column(name = "maturedon_date")
    private LocalDate actualMaturityDate;

    @Setter()
    @Column(name = "expected_firstrepaymenton_date")
    private LocalDate expectedFirstRepaymentOnDate;

    @Setter()
    @Column(name = "interest_calculated_from_date")
    private LocalDate interestChargedFromDate;

    @Column(name = "total_overpaid_derived", scale = 6, precision = 19)
    private BigDecimal totalOverpaid;

    @Setter()
    @Column(name = "overpaidon_date")
    private LocalDate overpaidOnDate;

    @Column(name = "loan_counter")
    private Integer loanCounter;

    @Column(name = "loan_product_counter")
    private Integer loanProductCounter;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<LoanCharge> charges = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<LoanTrancheCharge> trancheCharges = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<LoanCollateral> collateral = null;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<LoanCollateralManagement> loanCollateralManagements = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<LoanOfficerAssignmentHistory> loanOfficerHistory;

    @OrderBy(value = "installmentNumber")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = new ArrayList<>();

    @OrderBy(value = "dateOf, createdDate, id")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LoanTransaction> loanTransactions = new ArrayList<>();

    @Embedded
    private LoanSummary summary;

    @Transient
    private boolean accountNumberRequiresAutoGeneration;
    @Transient
    private LoanRepaymentScheduleTransactionProcessorFactory transactionProcessorFactory;

    @Transient
    private LoanLifecycleStateMachine loanLifecycleStateMachine;

    @Setter()
    @Column(name = "principal_amount_proposed", scale = 6, precision = 19, nullable = false)
    private BigDecimal proposedPrincipal;

    @Setter()
    @Column(name = "approved_principal", scale = 6, precision = 19, nullable = false)
    private BigDecimal approvedPrincipal;

    @Setter()
    @Column(name = "net_disbursal_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal netDisbursalAmount;

    @Setter()
    @Column(name = "fixed_emi_amount", scale = 6, precision = 19)
    private BigDecimal fixedEmiAmount;

    @Setter()
    @Column(name = "max_outstanding_loan_balance", scale = 6, precision = 19)
    private BigDecimal maxOutstandingLoanBalance;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy(value = "expectedDisbursementDate, id")
    private List<LoanDisbursementDetails> disbursementDetails = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PostDatedChecks> postDatedChecks = new ArrayList<>();

    @OrderBy(value = "termApplicableFrom, id")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LoanTermVariations> loanTermVariations = new ArrayList<>();

    @Column(name = "total_recovered_derived", scale = 6, precision = 19)
    private BigDecimal totalRecovered;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loan", optional = true, orphanRemoval = true, fetch = FetchType.LAZY)
    private LoanInterestRecalculationDetails loanInterestRecalculationDetails;

    @Column(name = "is_npa", nullable = false)
    private boolean isNpa;

    @Setter()
    @Column(name = "accrued_till")
    private LocalDate accruedTill;

    @Setter()
    @Column(name = "create_standing_instruction_at_disbursement")
    private Boolean createStandingInstructionAtDisbursement;

    @Column(name = "guarantee_amount_derived", scale = 6, precision = 19)
    private BigDecimal guaranteeAmountDerived;

    @Setter
    @Column(name = "interest_recalcualated_on")
    private LocalDate interestRecalculatedOn;

    @Setter()
    @Column(name = "is_floating_interest_rate")
    private Boolean isFloatingInterestRate;

    @Setter()
    @Column(name = "interest_rate_differential", scale = 6, precision = 19)
    private BigDecimal interestRateDifferential;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writeoff_reason_cv_id")
    private CodeValue writeOffReason;

    @Setter
    @Column(name = "loan_sub_status_id")
    private Integer loanSubStatus;

    @Column(name = "is_topup", nullable = false)
    private boolean isTopup = false;

    @Column(name = "is_fraud", nullable = false)
    private boolean fraud = false;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loan", optional = true, orphanRemoval = true, fetch = FetchType.LAZY)
    private LoanTopupDetails loanTopupDetails;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "m_loan_rate", joinColumns = @JoinColumn(name = "loan_id"), inverseJoinColumns = @JoinColumn(name = "rate_id"))
    private List<Rate> rates;

    @Setter()
    @Column(name = "fixed_principal_percentage_per_installment", scale = 2, precision = 5)
    private BigDecimal fixedPrincipalPercentagePerInstallment;

    @Setter()
    @Column(name = "last_closed_business_date")
    private LocalDate lastClosedBusinessDate;

    @Column(name = "is_charged_off", nullable = false)
    private boolean chargedOff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_off_reason_cv_id")
    private CodeValue chargeOffReason;

    @Column(name = "charged_off_on_date")
    private LocalDate chargedOffOnDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charged_off_by_userid")
    private AppUser chargedOffBy;

    @Column(name = "enable_installment_level_delinquency", nullable = false)
    private boolean enableInstallmentLevelDelinquency = false;

    public static Loan newIndividualLoanApplication(final String accountNo, final Client client, final AccountType loanType,
            final LoanProduct loanProduct, final Fund fund, final Staff officer, final CodeValue loanPurpose,
            final LoanRepaymentScheduleTransactionProcessor transactionProcessingStrategy,
            final LoanProductRelatedDetail loanRepaymentScheduleDetail, final Set<LoanCharge> loanCharges,
            final Set<LoanCollateralManagement> collateral, final BigDecimal fixedEmiAmount,
            final List<LoanDisbursementDetails> disbursementDetails, final BigDecimal maxOutstandingLoanBalance,
            final Boolean createStandingInstructionAtDisbursement, final Boolean isFloatingInterestRate,
            final BigDecimal interestRateDifferential, final List<Rate> rates, final BigDecimal fixedPrincipalPercentagePerInstallment,
            final ExternalId externalId, final LoanApplicationTerms loanApplicationTerms, final LoanScheduleModel loanScheduleModel,
            final Boolean enableInstallmentLevelDelinquency, final LocalDate submittedOnDate) {
        return new Loan(accountNo, client, null, loanType, fund, officer, loanPurpose, transactionProcessingStrategy, loanProduct,
                loanRepaymentScheduleDetail, null, loanCharges, collateral, null, fixedEmiAmount, disbursementDetails,
                maxOutstandingLoanBalance, createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential, rates,
                fixedPrincipalPercentagePerInstallment, externalId, loanApplicationTerms, loanScheduleModel,
                enableInstallmentLevelDelinquency, submittedOnDate);
    }

    public static Loan newGroupLoanApplication(final String accountNo, final Group group, final AccountType loanType,
            final LoanProduct loanProduct, final Fund fund, final Staff officer, final CodeValue loanPurpose,
            final LoanRepaymentScheduleTransactionProcessor transactionProcessingStrategy,
            final LoanProductRelatedDetail loanRepaymentScheduleDetail, final Set<LoanCharge> loanCharges,
            final Boolean syncDisbursementWithMeeting, final BigDecimal fixedEmiAmount,
            final List<LoanDisbursementDetails> disbursementDetails, final BigDecimal maxOutstandingLoanBalance,
            final Boolean createStandingInstructionAtDisbursement, final Boolean isFloatingInterestRate,
            final BigDecimal interestRateDifferential, final List<Rate> rates, final BigDecimal fixedPrincipalPercentagePerInstallment,
            final ExternalId externalId, final LoanApplicationTerms loanApplicationTerms, final LoanScheduleModel loanScheduleModel,
            final Boolean enableInstallmentLevelDelinquency, final LocalDate submittedOnDate) {
        return new Loan(accountNo, null, group, loanType, fund, officer, loanPurpose, transactionProcessingStrategy, loanProduct,
                loanRepaymentScheduleDetail, null, loanCharges, null, syncDisbursementWithMeeting, fixedEmiAmount, disbursementDetails,
                maxOutstandingLoanBalance, createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential, rates,
                fixedPrincipalPercentagePerInstallment, externalId, loanApplicationTerms, loanScheduleModel,
                enableInstallmentLevelDelinquency, submittedOnDate);
    }

    public static Loan newIndividualLoanApplicationFromGroup(final String accountNo, final Client client, final Group group,
            final AccountType loanType, final LoanProduct loanProduct, final Fund fund, final Staff officer, final CodeValue loanPurpose,
            final LoanRepaymentScheduleTransactionProcessor transactionProcessingStrategy,
            final LoanProductRelatedDetail loanRepaymentScheduleDetail, final Set<LoanCharge> loanCharges,
            final Boolean syncDisbursementWithMeeting, final BigDecimal fixedEmiAmount,
            final List<LoanDisbursementDetails> disbursementDetails, final BigDecimal maxOutstandingLoanBalance,
            final Boolean createStandingInstructionAtDisbursement, final Boolean isFloatingInterestRate,
            final BigDecimal interestRateDifferential, final List<Rate> rates, final BigDecimal fixedPrincipalPercentagePerInstallment,
            final ExternalId externalId, final LoanApplicationTerms loanApplicationTerms, final LoanScheduleModel loanScheduleModel,
            final Boolean enableInstallmentLevelDelinquency, final LocalDate submittedOnDate) {
        return new Loan(accountNo, client, group, loanType, fund, officer, loanPurpose, transactionProcessingStrategy, loanProduct,
                loanRepaymentScheduleDetail, null, loanCharges, null, syncDisbursementWithMeeting, fixedEmiAmount, disbursementDetails,
                maxOutstandingLoanBalance, createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential, rates,
                fixedPrincipalPercentagePerInstallment, externalId, loanApplicationTerms, loanScheduleModel,
                enableInstallmentLevelDelinquency, submittedOnDate);
    }

    protected Loan() {
        // empty
    }

    private Loan(final String accountNo, final Client client, final Group group, final AccountType loanType, final Fund fund,
            final Staff loanOfficer, final CodeValue loanPurpose,
            final LoanRepaymentScheduleTransactionProcessor transactionProcessingStrategy, final LoanProduct loanProduct,
            final LoanProductRelatedDetail loanRepaymentScheduleDetail, final LoanStatus loanStatus, final Set<LoanCharge> loanCharges,
            final Set<LoanCollateralManagement> collateral, final Boolean syncDisbursementWithMeeting, final BigDecimal fixedEmiAmount,
            final List<LoanDisbursementDetails> disbursementDetails, final BigDecimal maxOutstandingLoanBalance,
            final Boolean createStandingInstructionAtDisbursement, final Boolean isFloatingInterestRate,
            final BigDecimal interestRateDifferential, final List<Rate> rates, final BigDecimal fixedPrincipalPercentagePerInstallment,
            final ExternalId externalId, final LoanApplicationTerms loanApplicationTerms, final LoanScheduleModel loanScheduleModel,
            final Boolean enableInstallmentLevelDelinquency, final LocalDate submittedOnDate) {
        this.loanRepaymentScheduleDetail = loanRepaymentScheduleDetail;

        this.isFloatingInterestRate = isFloatingInterestRate;
        this.interestRateDifferential = interestRateDifferential;

        if (StringUtils.isBlank(accountNo)) {
            this.accountNumber = new RandomPasswordGenerator(19).generate();
            this.accountNumberRequiresAutoGeneration = true;
        } else {
            this.accountNumber = accountNo;
        }
        this.client = client;
        this.group = group;
        this.loanType = loanType.getValue();
        this.fund = fund;
        this.loanOfficer = loanOfficer;
        this.loanPurpose = loanPurpose;

        this.transactionProcessingStrategyCode = transactionProcessingStrategy.getCode();
        this.transactionProcessingStrategyName = transactionProcessingStrategy.getName();

        this.loanProduct = loanProduct;
        if (loanStatus != null) {
            this.loanStatus = loanStatus.getValue();
        } else {
            this.loanStatus = null;
        }
        if (loanCharges != null && !loanCharges.isEmpty()) {
            this.charges = associateChargesWithThisLoan(loanCharges);
            this.summary = updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
        } else {
            this.charges = null;
            this.summary = new LoanSummary();
        }

        if (loanType.isIndividualAccount() && collateral != null && !collateral.isEmpty()) {
            this.loanCollateralManagements = associateWithThisLoan(collateral);
        } else {
            this.loanCollateralManagements = null;
        }
        this.loanOfficerHistory = null;

        this.syncDisbursementWithMeeting = syncDisbursementWithMeeting;
        this.fixedEmiAmount = fixedEmiAmount;
        this.maxOutstandingLoanBalance = maxOutstandingLoanBalance;
        if (disbursementDetails != null) {
            this.disbursementDetails = disbursementDetails;
            for (final LoanDisbursementDetails loanDisbursementDetails : disbursementDetails) {
                loanDisbursementDetails.updateLoan(this);
            }
        }
        this.approvedPrincipal = this.loanRepaymentScheduleDetail.getPrincipal().getAmount();
        this.createStandingInstructionAtDisbursement = createStandingInstructionAtDisbursement;

        /*
         * During loan origination stage and before loan is approved principal_amount, approved_principal and
         * principal_amount_demanded will same amount and that amount is same as applicant loan demanded amount.
         */

        this.proposedPrincipal = this.loanRepaymentScheduleDetail.getPrincipal().getAmount();

        // rates added here
        this.rates = rates;
        this.fixedPrincipalPercentagePerInstallment = fixedPrincipalPercentagePerInstallment;

        // Add net get net disbursal amount from charges and principal
        this.netDisbursalAmount = this.approvedPrincipal.subtract(deriveSumTotalOfChargesDueAtDisbursement());
        this.loanStatus = LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue();
        this.externalId = externalId;
        this.termFrequency = loanApplicationTerms.getLoanTermFrequency();
        this.termPeriodFrequencyType = loanApplicationTerms.getLoanTermPeriodFrequencyType().getValue();
        this.expectedDisbursementDate = loanApplicationTerms.getExpectedDisbursementDate();
        this.expectedFirstRepaymentOnDate = loanApplicationTerms.getRepaymentStartFromDate();
        this.interestChargedFromDate = loanApplicationTerms.getInterestChargedFromDate();
        this.submittedOnDate = submittedOnDate != null ? submittedOnDate : DateUtils.getBusinessLocalDate();

        updateLoanSchedule(loanScheduleModel);

        updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());

        // Copy interest recalculation settings if interest recalculation is enabled
        if (this.loanRepaymentScheduleDetail.isInterestRecalculationEnabled()) {
            this.loanInterestRecalculationDetails = LoanInterestRecalculationDetails
                    .createFrom(this.loanProduct.getProductInterestRecalculationDetails());
            this.loanInterestRecalculationDetails.updateLoan(this);
        }
        this.enableInstallmentLevelDelinquency = enableInstallmentLevelDelinquency;
        this.getLoanProductRelatedDetail()
                .setEnableAccrualActivityPosting(loanProduct.getLoanProductRelatedDetail().isEnableAccrualActivityPosting());
    }

    public Integer getNumberOfRepayments() {
        return this.loanRepaymentScheduleDetail.getNumberOfRepayments();
    }

    public LoanSummary updateSummaryWithTotalFeeChargesDueAtDisbursement(final BigDecimal feeChargesDueAtDisbursement) {
        if (this.summary == null) {
            this.summary = LoanSummary.create(feeChargesDueAtDisbursement);
        } else {
            this.summary.updateTotalFeeChargesDueAtDisbursement(feeChargesDueAtDisbursement);
        }
        return this.summary;
    }

    public void updateLoanSummaryForUndoWaiveCharge(final BigDecimal amountWaived, final boolean isPenalty) {
        if (isPenalty) {
            this.summary.updatePenaltyChargesWaived(this.summary.getTotalPenaltyChargesWaived().subtract(amountWaived));
            this.summary.updatePenaltyChargeOutstanding(this.summary.getTotalPenaltyChargesOutstanding().add(amountWaived));
        } else {
            this.summary.updateFeeChargesWaived(this.summary.getTotalFeeChargesWaived().subtract(amountWaived));
            this.summary.updateFeeChargeOutstanding(this.summary.getTotalFeeChargesOutstanding().add(amountWaived));
        }
        this.summary.updateTotalOutstanding(this.summary.getTotalOutstanding().add(amountWaived));
        this.summary.updateTotalWaived(this.summary.getTotalWaived().subtract(amountWaived));
    }

    public BigDecimal deriveSumTotalOfChargesDueAtDisbursement() {
        return getActiveCharges().stream() //
                .filter(LoanCharge::isDueAtDisbursement) //
                .map(LoanCharge::amount) //
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Set<LoanCharge> associateChargesWithThisLoan(final Set<LoanCharge> loanCharges) {
        for (final LoanCharge loanCharge : loanCharges) {
            loanCharge.update(this);
            if (loanCharge.getTrancheDisbursementCharge() != null) {
                addTrancheLoanCharge(loanCharge.getCharge());
            }
        }
        return loanCharges;
    }

    private Set<LoanCollateralManagement> associateWithThisLoan(final Set<LoanCollateralManagement> collateral) {
        for (final LoanCollateralManagement item : collateral) {
            item.setLoan(this);
        }
        return collateral;
    }

    public void addLoanCharge(final LoanCharge loanCharge) {
        loanCharge.update(this);

        final BigDecimal amount = calculateAmountPercentageAppliedTo(loanCharge);
        BigDecimal chargeAmt;
        BigDecimal totalChargeAmt = BigDecimal.ZERO;
        if (loanCharge.getChargeCalculation().isPercentageBased()) {
            chargeAmt = loanCharge.getPercentage();
            if (loanCharge.isInstalmentFee()) {
                totalChargeAmt = calculatePerInstallmentChargeAmount(loanCharge);
            } else if (loanCharge.isOverdueInstallmentCharge()) {
                totalChargeAmt = loanCharge.amountOutstanding();
            }
        } else {
            chargeAmt = loanCharge.amountOrPercentage();
        }
        loanCharge.update(chargeAmt, loanCharge.getDueLocalDate(), amount, fetchNumberOfInstallmensAfterExceptions(), totalChargeAmt);

        // NOTE: must add new loan charge to set of loan charges before
        // reprocessing the repayment schedule.
        if (this.charges == null) {
            this.charges = new HashSet<>();
        }
        this.charges.add(loanCharge);
        this.summary = updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());

        // store Id's of existing loan transactions and existing reversed loan transactions
        final SingleLoanChargeRepaymentScheduleProcessingWrapper wrapper = new SingleLoanChargeRepaymentScheduleProcessingWrapper();
        wrapper.reprocess(getCurrency(), getDisbursementDate(), getRepaymentScheduleInstallments(), loanCharge);
        updateLoanSummaryDerivedFields();

        loanLifecycleStateMachine.transition(LoanEvent.LOAN_CHARGE_ADDED, this);
    }

    public ChangedTransactionDetail reprocessTransactions() {
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = getTransactionProcessor();
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsForReprocessing();
        ChangedTransactionDetail changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(
                getDisbursementDate(), allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(),
                getActiveCharges());
        for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
            mapEntry.getValue().updateLoan(this);
        }
        this.loanTransactions.addAll(changedTransactionDetail.getNewTransactionMappings().values());
        updateLoanSummaryDerivedFields();
        return changedTransactionDetail;
    }

    public ChangedTransactionDetail reprocessTransactionsWithPostTransactionChecks(LocalDate transactionDate) {
        ChangedTransactionDetail changedDetail = reprocessTransactions();
        doPostLoanTransactionChecks(transactionDate, loanLifecycleStateMachine);
        return changedDetail;
    }

    /**
     * Creates a loanTransaction for "Apply Charge Event" with transaction date set to "suppliedTransactionDate". The
     * newly created transaction is also added to the Loan on which this method is called.
     *
     * If "suppliedTransactionDate" is not passed Id, the transaction date is set to the loans due date if the due date
     * is lesser than todays date. If not, the transaction date is set to todays date
     */
    public LoanTransaction handleChargeAppliedTransaction(final LoanCharge loanCharge, final LocalDate suppliedTransactionDate) {
        if (isProgressiveSchedule()) {
            return null;
        }
        final Money chargeAmount = loanCharge.getAmount(getCurrency());
        Money feeCharges = chargeAmount;
        Money penaltyCharges = Money.zero(getCurrency());
        if (loanCharge.isPenaltyCharge()) {
            penaltyCharges = chargeAmount;
            feeCharges = Money.zero(getCurrency());
        }

        LocalDate transactionDate;
        if (suppliedTransactionDate != null) {
            transactionDate = suppliedTransactionDate;
        } else {
            transactionDate = loanCharge.getDueLocalDate();
            final LocalDate currentDate = DateUtils.getBusinessLocalDate();

            // if loan charge is to be applied on a future date, the loan transaction would show today's date as applied
            // date
            if (transactionDate == null || DateUtils.isAfter(transactionDate, currentDate)) {
                transactionDate = currentDate;
            }
        }
        ExternalId externalId = ExternalId.empty();
        if (TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }
        final LoanTransaction applyLoanChargeTransaction = LoanTransaction.accrueLoanCharge(this, getOffice(), chargeAmount,
                transactionDate, feeCharges, penaltyCharges, externalId);

        Integer installmentNumber = null;
        final LoanRepaymentScheduleInstallment installmentForCharge = this.getRelatedRepaymentScheduleInstallment(loanCharge.getDueDate());
        if (installmentForCharge != null) {
            installmentForCharge.updateAccrualPortion(installmentForCharge.getInterestAccrued(this.getCurrency()),
                    installmentForCharge.getFeeAccrued(this.getCurrency()).add(feeCharges),
                    installmentForCharge.getPenaltyAccrued(this.getCurrency()).add(penaltyCharges));
            installmentNumber = installmentForCharge.getInstallmentNumber();
        }
        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(applyLoanChargeTransaction, loanCharge,
                loanCharge.getAmount(getCurrency()).getAmount(), installmentNumber);
        applyLoanChargeTransaction.getLoanChargesPaid().add(loanChargePaidBy);
        addLoanTransaction(applyLoanChargeTransaction);
        return applyLoanChargeTransaction;
    }

    public void handleChargePaidTransaction(final LoanCharge charge, final LoanTransaction chargesPayment,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final Integer installmentNumber) {
        chargesPayment.updateLoan(this);
        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(chargesPayment, charge,
                chargesPayment.getAmount(getCurrency()).getAmount(), installmentNumber);
        chargesPayment.getLoanChargesPaid().add(loanChargePaidBy);
        addLoanTransaction(chargesPayment);
        loanLifecycleStateMachine.transition(LoanEvent.LOAN_CHARGE_PAYMENT, this);

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = getTransactionProcessor();
        final List<LoanRepaymentScheduleInstallment> chargePaymentInstallments = new ArrayList<>();
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper
                .fetchFirstNormalInstallmentNumber(repaymentScheduleInstallments);
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            boolean isFirstInstallment = installment.getInstallmentNumber().equals(firstNormalInstallmentNumber);
            if (installment.getInstallmentNumber().equals(installmentNumber) || (installmentNumber == null
                    && charge.isDueInPeriod(installment.getFromDate(), installment.getDueDate(), isFirstInstallment))) {
                chargePaymentInstallments.add(installment);
                break;
            }
        }
        final Set<LoanCharge> loanCharges = new HashSet<>(1);
        loanCharges.add(charge);
        loanRepaymentScheduleTransactionProcessor.processLatestTransaction(chargesPayment, new TransactionCtx(getCurrency(),
                chargePaymentInstallments, loanCharges, new MoneyHolder(getTotalOverpaidAsMoney()), null));

        updateLoanSummaryDerivedFields();
        doPostLoanTransactionChecks(chargesPayment.getTransactionDate(), loanLifecycleStateMachine);
    }

    public LocalDate getLastRepaymentPeriodDueDate(final boolean includeRecalculatedInterestComponent) {
        LocalDate lastRepaymentDate = getDisbursementDate();
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (LoanRepaymentScheduleInstallment installment : installments) {
            if ((includeRecalculatedInterestComponent || !installment.isRecalculatedInterestComponent())
                    && DateUtils.isAfter(installment.getDueDate(), lastRepaymentDate)) {
                lastRepaymentDate = installment.getDueDate();
            }
        }
        return lastRepaymentDate;
    }

    public void removeLoanCharge(final LoanCharge loanCharge) {
        final boolean removed = loanCharge.isActive();
        if (removed) {
            loanCharge.setActive(false);
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(getCurrency(), getDisbursementDate(), getRepaymentScheduleInstallments(), getActiveCharges());
            updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
        }

        removeOrModifyTransactionAssociatedWithLoanChargeIfDueAtDisbursement(loanCharge);

        if (!loanCharge.isDueAtDisbursement() && loanCharge.isPaidOrPartiallyPaid(getCurrency())) {
            /*
             * TODO Vishwas Currently we do not allow removing a loan charge after a loan is approved (hence there is no
             * need to adjust any loan transactions).
             *
             * Consider removing this block of code or logically completing it for the future by getting the list of
             * affected Transactions
             */
            reprocessTransactions();
        }
        this.charges.remove(loanCharge);
        updateLoanSummaryDerivedFields();
    }

    private void removeOrModifyTransactionAssociatedWithLoanChargeIfDueAtDisbursement(final LoanCharge loanCharge) {
        if (loanCharge.isDueAtDisbursement()) {
            LoanTransaction transactionToRemove = null;
            List<LoanTransaction> transactions = getLoanTransactions();
            for (final LoanTransaction transaction : transactions) {
                if (transaction.isRepaymentAtDisbursement()
                        && doesLoanChargePaidByContainLoanCharge(transaction.getLoanChargesPaid(), loanCharge)) {
                    final MonetaryCurrency currency = getCurrency();
                    final Money chargeAmount = Money.of(currency, loanCharge.amount());
                    if (transaction.isGreaterThan(chargeAmount)) {
                        final Money principalPortion = Money.zero(currency);
                        final Money interestPortion = Money.zero(currency);
                        final Money penaltychargesPortion = Money.zero(currency);

                        transaction.updateComponentsAndTotal(principalPortion, interestPortion, chargeAmount, penaltychargesPortion);

                    } else {
                        transactionToRemove = transaction;
                    }
                }
            }

            if (transactionToRemove != null) {
                this.loanTransactions.remove(transactionToRemove);
            }
        }
    }

    public void removeDisbursementDetails(final long id) {
        this.disbursementDetails.remove(fetchLoanDisbursementsById(id));
    }

    public LoanDisbursementDetails addLoanDisbursementDetails(final LocalDate expectedDisbursementDate, final BigDecimal principal) {
        final LocalDate actualDisbursementDate = null;
        final LoanDisbursementDetails disbursementDetails = new LoanDisbursementDetails(expectedDisbursementDate, actualDisbursementDate,
                principal, this.netDisbursalAmount, false);
        disbursementDetails.updateLoan(this);
        this.disbursementDetails.add(disbursementDetails);
        return disbursementDetails;
    }

    private boolean doesLoanChargePaidByContainLoanCharge(Set<LoanChargePaidBy> loanChargePaidBys, LoanCharge loanCharge) {
        return loanChargePaidBys.stream() //
                .anyMatch(loanChargePaidBy -> loanChargePaidBy.getLoanCharge().equals(loanCharge));
    }

    public Map<String, Object> updateLoanCharge(final LoanCharge loanCharge, final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(3);

        if (getActiveCharges().contains(loanCharge)) {
            final BigDecimal amount = calculateAmountPercentageAppliedTo(loanCharge);
            final Map<String, Object> loanChargeChanges = loanCharge.update(command, amount);
            actualChanges.putAll(loanChargeChanges);
            updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
        }

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = getTransactionProcessor();
        if (!loanCharge.isDueAtDisbursement()) {
            /*
             * TODO Vishwas Currently we do not allow waiving updating loan charge after a loan is approved (hence there
             * is no need to adjust any loan transactions).
             *
             * Consider removing this block of code or logically completing it for the future by getting the list of
             * affected Transactions
             */
            reprocessTransactions();
        } else {
            // reprocess loan schedule based on charge been waived.
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(getCurrency(), getDisbursementDate(), getRepaymentScheduleInstallments(), getActiveCharges());
        }

        updateLoanSummaryDerivedFields();

        return actualChanges;
    }

    public BigDecimal calculateAmountPercentageAppliedTo(final LoanCharge loanCharge) {
        if (loanCharge.isOverdueInstallmentCharge()) {
            return loanCharge.getAmountPercentageAppliedTo();
        }

        return switch (loanCharge.getChargeCalculation()) {
            case PERCENT_OF_AMOUNT -> getDerivedAmountForCharge(loanCharge);
            case PERCENT_OF_AMOUNT_AND_INTEREST -> {
                final BigDecimal totalInterestCharged = getTotalInterest();
                if (isMultiDisburmentLoan() && loanCharge.isDisbursementCharge()) {
                    yield getTotalAllTrancheDisbursementAmount().getAmount().add(totalInterestCharged);
                } else {
                    yield getPrincipal().getAmount().add(totalInterestCharged);
                }
            }
            case PERCENT_OF_INTEREST -> getTotalInterest();
            case PERCENT_OF_DISBURSEMENT_AMOUNT -> {
                if (loanCharge.getTrancheDisbursementCharge() != null) {
                    yield loanCharge.getTrancheDisbursementCharge().getloanDisbursementDetails().principal();
                } else {
                    yield getPrincipal().getAmount();
                }
            }
            case INVALID, FLAT -> BigDecimal.ZERO;
        };
    }

    private Money getTotalAllTrancheDisbursementAmount() {
        Money amount = Money.zero(getCurrency());
        if (isMultiDisburmentLoan()) {
            for (final LoanDisbursementDetails loanDisbursementDetail : getDisbursementDetails()) {
                amount = amount.plus(loanDisbursementDetail.principal());
            }
        }
        return amount;
    }

    public BigDecimal getTotalInterest() {
        return this.summary.calculateTotalInterestCharged(getRepaymentScheduleInstallments(), getCurrency()).getAmount();
    }

    public BigDecimal calculatePerInstallmentChargeAmount(final LoanCharge loanCharge) {
        return calculatePerInstallmentChargeAmount(loanCharge.getChargeCalculation(), loanCharge.getPercentage());
    }

    public BigDecimal calculatePerInstallmentChargeAmount(final ChargeCalculationType calculationType, final BigDecimal percentage) {
        Money amount = Money.zero(getCurrency());
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            amount = amount.plus(calculateInstallmentChargeAmount(calculationType, percentage, installment));
        }
        return amount.getAmount();
    }

    public BigDecimal getTotalWrittenOff() {
        return this.summary.getTotalWrittenOff();
    }

    /**
     * @param calculationType
     * @param percentage
     * @param installment
     * @return
     */
    private Money calculateInstallmentChargeAmount(final ChargeCalculationType calculationType, final BigDecimal percentage,
            final LoanRepaymentScheduleInstallment installment) {
        Money percentOf = switch (calculationType) {
            case PERCENT_OF_AMOUNT -> installment.getPrincipal(getCurrency());
            case PERCENT_OF_AMOUNT_AND_INTEREST ->
                installment.getPrincipal(getCurrency()).plus(installment.getInterestCharged(getCurrency()));
            case PERCENT_OF_INTEREST -> installment.getInterestCharged(getCurrency());
            case PERCENT_OF_DISBURSEMENT_AMOUNT, INVALID, FLAT -> Money.zero(getCurrency());

        };
        return Money.zero(getCurrency()) //
                .plus(LoanCharge.percentageOf(percentOf.getAmount(), percentage));
    }

    public Client client() {
        return this.client;
    }

    public LoanProduct loanProduct() {
        return this.loanProduct;
    }

    public LoanProductRelatedDetail repaymentScheduleDetail() {
        return this.loanRepaymentScheduleDetail;
    }

    public void updateClient(final Client client) {
        this.client = client;
    }

    public void updateLoanProduct(final LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }

    public void updateAccountNo(final String newAccountNo) {
        this.accountNumber = newAccountNo;
        this.accountNumberRequiresAutoGeneration = false;
    }

    public void updateFund(final Fund fund) {
        this.fund = fund;
    }

    public void updateLoanPurpose(final CodeValue loanPurpose) {
        this.loanPurpose = loanPurpose;
    }

    public void updateTransactionProcessingStrategy(final String transactionProcessingStrategyCode,
            final String transactionProcessingStrategyName) {
        this.transactionProcessingStrategyCode = transactionProcessingStrategyCode;
        this.transactionProcessingStrategyName = transactionProcessingStrategyName;
    }

    public void updateLoanCharges(final Set<LoanCharge> loanCharges) {
        List<Long> existingCharges = fetchAllLoanChargeIds();

        /* Process new and updated charges **/
        for (final LoanCharge loanCharge : loanCharges) {
            LoanCharge charge = loanCharge;
            // add new charges
            if (loanCharge.getId() == null) {
                LoanTrancheDisbursementCharge loanTrancheDisbursementCharge;
                loanCharge.update(this);
                if (this.loanProduct.isMultiDisburseLoan() && loanCharge.isTrancheDisbursementCharge()) {
                    loanCharge.getTrancheDisbursementCharge().getloanDisbursementDetails().updateLoan(this);
                    for (final LoanDisbursementDetails loanDisbursementDetails : getDisbursementDetails()) {
                        if (loanCharge.getTrancheDisbursementCharge().getloanDisbursementDetails().getId() == null
                                && loanCharge.getTrancheDisbursementCharge().getloanDisbursementDetails().equals(loanDisbursementDetails)) {
                            loanTrancheDisbursementCharge = new LoanTrancheDisbursementCharge(loanCharge, loanDisbursementDetails);
                            loanCharge.updateLoanTrancheDisbursementCharge(loanTrancheDisbursementCharge);
                        }
                    }
                }
                this.charges.add(loanCharge);

            } else {
                charge = fetchLoanChargesById(charge.getId());
                if (charge != null) {
                    existingCharges.remove(charge.getId());
                }
            }
            final BigDecimal amount = calculateAmountPercentageAppliedTo(loanCharge);
            BigDecimal chargeAmt;
            BigDecimal totalChargeAmt = BigDecimal.ZERO;
            if (loanCharge.getChargeCalculation().isPercentageBased()) {
                chargeAmt = loanCharge.getPercentage();
                if (loanCharge.isInstalmentFee()) {
                    totalChargeAmt = calculatePerInstallmentChargeAmount(loanCharge);
                }
            } else {
                chargeAmt = loanCharge.amountOrPercentage();
            }
            if (charge != null) {
                charge.update(chargeAmt, loanCharge.getDueLocalDate(), amount, fetchNumberOfInstallmensAfterExceptions(), totalChargeAmt);
            }

        }

        /* Updated deleted charges **/
        for (Long id : existingCharges) {
            fetchLoanChargesById(id).setActive(false);
        }
        updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
    }

    public void updateLoanCollateral(final Set<LoanCollateralManagement> loanCollateral) {
        if (this.loanCollateralManagements == null) {
            this.loanCollateralManagements = new HashSet<>();
        }
        this.loanCollateralManagements.clear();
        this.loanCollateralManagements.addAll(associateWithThisLoan(loanCollateral));
    }

    public void updateLoanRates(final List<Rate> loanRates) {
        if (this.rates == null) {
            this.rates = new ArrayList<>();
        }
        this.rates.clear();
        this.rates.addAll(loanRates);
    }

    public void updateLoanSchedule(final LoanScheduleModel modifiedLoanSchedule) {
        this.repaymentScheduleInstallments.clear();
        for (final LoanScheduleModelPeriod scheduledLoanInstallment : modifiedLoanSchedule.getPeriods()) {

            if (scheduledLoanInstallment.isRepaymentPeriod() || scheduledLoanInstallment.isDownPaymentPeriod()) {
                final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(this,
                        scheduledLoanInstallment.periodNumber(), scheduledLoanInstallment.periodFromDate(),
                        scheduledLoanInstallment.periodDueDate(), scheduledLoanInstallment.principalDue(),
                        scheduledLoanInstallment.interestDue(), scheduledLoanInstallment.feeChargesDue(),
                        scheduledLoanInstallment.penaltyChargesDue(), scheduledLoanInstallment.isRecalculatedInterestComponent(),
                        scheduledLoanInstallment.getLoanCompoundingDetails(), scheduledLoanInstallment.rescheduleInterestPortion(),
                        scheduledLoanInstallment.isDownPaymentPeriod());
                addLoanRepaymentScheduleInstallment(installment);
            }
        }
        updateLoanScheduleDependentDerivedFields();
        updateLoanSummaryDerivedFields();
    }

    public void updateLoanSchedule(final Collection<LoanRepaymentScheduleInstallment> installments) {
        List<LoanRepaymentScheduleInstallment> existingInstallments = new ArrayList<>(this.repaymentScheduleInstallments);
        repaymentScheduleInstallments.clear();
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            LoanRepaymentScheduleInstallment existingInstallment = findByInstallmentNumber(existingInstallments,
                    installment.getInstallmentNumber());
            if (existingInstallment != null) {
                Set<LoanInstallmentCharge> existingCharges = existingInstallment.getInstallmentCharges();
                installment.getInstallmentCharges().addAll(existingCharges);
                existingCharges.forEach(c -> c.setInstallment(installment));
                existingInstallment.getInstallmentCharges().clear();
            }
            addLoanRepaymentScheduleInstallment(installment);
        }
        updateLoanScheduleDependentDerivedFields();
        updateLoanSummaryDerivedFields();
    }

    private LoanRepaymentScheduleInstallment findByInstallmentNumber(Collection<LoanRepaymentScheduleInstallment> installments,
            Integer installmentNumber) {
        for (LoanRepaymentScheduleInstallment installment : installments) {
            if (Objects.equals(installment.getInstallmentNumber(), installmentNumber)) {
                return installment;
            }
        }
        return null;
    }

    public void updateLoanScheduleDependentDerivedFields() {
        if (this.getLoanRepaymentScheduleInstallmentsSize() > 0) {
            this.expectedMaturityDate = determineExpectedMaturityDate();
            this.actualMaturityDate = determineExpectedMaturityDate();
        }
    }

    public void updateLoanSummaryDerivedFields() {
        if (isNotDisbursed()) {
            if (this.summary != null) {
                this.summary.zeroFields();
            }
            this.totalOverpaid = null;
        } else {
            final Money overpaidBy = calculateTotalOverpayment();
            this.totalOverpaid = null;
            if (!overpaidBy.isLessThanZero()) {
                this.totalOverpaid = overpaidBy.getAmountDefaultedToNullIfZero();
            }

            final Money recoveredAmount = calculateTotalRecoveredPayments();
            this.totalRecovered = recoveredAmount.getAmountDefaultedToNullIfZero();

            final Money principal = this.loanRepaymentScheduleDetail.getPrincipal();
            this.summary.updateSummary(getCurrency(), principal, getRepaymentScheduleInstallments(), this.charges);
            updateLoanOutstandingBalances();
        }
    }

    public void updateLoanSummaryAndStatus() {
        updateLoanSummaryDerivedFields();
        doPostLoanTransactionChecks(getLastUserTransactionDate(), loanLifecycleStateMachine);
    }

    private boolean isInterestRecalculationEnabledForProduct() {
        return this.loanProduct.isInterestRecalculationEnabled();
    }

    public boolean isMultiDisburmentLoan() {
        return this.loanProduct.isMultiDisburseLoan();
    }

    /**
     * Update interest recalculation settings if product configuration changes
     */
    public void updateOverdueScheduleInstallment(final LoanCharge loanCharge) {
        if (loanCharge.isOverdueInstallmentCharge() && loanCharge.isActive()) {
            LoanOverdueInstallmentCharge overdueInstallmentCharge = loanCharge.getOverdueInstallmentCharge();
            if (overdueInstallmentCharge != null) {
                Integer installmentNumber = overdueInstallmentCharge.getInstallment().getInstallmentNumber();
                LoanRepaymentScheduleInstallment installment = fetchRepaymentScheduleInstallment(installmentNumber);
                overdueInstallmentCharge.updateLoanRepaymentScheduleInstallment(installment);
            }
        }
    }

    public void clearLoanInstallmentChargesBeforeRegeneration(final LoanCharge loanCharge) {
        /*
         * JW https://issues.apache.org/jira/browse/FINERACT-1557 For loan installment charges only : Clear down
         * installment charges from the loanCharge and from each of the repayment installments and allow them to be
         * recalculated fully anew. This patch is to avoid the 'merging' of existing and regenerated installment charges
         * which results in the installment charges being deleted on loan approval if the schedule is regenerated. Not
         * pretty. updateInstallmentCharges in LoanCharge.java: the merging looks like it will work but doesn't so this
         * patch simply hits the part which 'adds all' rather than merge. Possibly an ORM issue. The issue could be to
         * do with the fact that, on approval, the "recalculateLoanCharge" happens twice (probably 2 schedule
         * regenerations) whereas it only happens once on Submit and Disburse (and no problems with them)
         *
         * if (this.loanInstallmentCharge.isEmpty()) { this.loanInstallmentCharge.addAll(newChargeInstallments);
         */
        Loan loan = loanCharge.getLoan();
        if (!loan.isSubmittedAndPendingApproval() && !loan.isApproved()) {
            return;
        } // doing for both just in case status is not
          // updated at this points
        if (loanCharge.isInstalmentFee()) {
            loanCharge.clearLoanInstallmentCharges();
            for (final LoanRepaymentScheduleInstallment installment : getRepaymentScheduleInstallments()) {
                if (installment.isRecalculatedInterestComponent()) {
                    continue; // JW: does this in generateInstallmentLoanCharges - but don't understand it
                }
                installment.getInstallmentCharges().clear();
            }
        }
    }

    public BigDecimal calculateOverdueAmountPercentageAppliedTo(final LoanCharge loanCharge, final int penaltyWaitPeriod) {
        LoanRepaymentScheduleInstallment installment = loanCharge.getOverdueInstallmentCharge().getInstallment();
        LocalDate graceDate = DateUtils.getBusinessLocalDate().minusDays(penaltyWaitPeriod);
        Money amount = Money.zero(getCurrency());

        if (DateUtils.isAfter(graceDate, installment.getDueDate())) {
            amount = calculateOverdueAmountPercentageAppliedTo(installment, loanCharge.getChargeCalculation());
            if (!amount.isGreaterThanZero()) {
                loanCharge.setActive(false);
            }
        } else {
            loanCharge.setActive(false);
        }
        return amount.getAmount();
    }

    private Money calculateOverdueAmountPercentageAppliedTo(LoanRepaymentScheduleInstallment installment,
            ChargeCalculationType calculationType) {
        return switch (calculationType) {
            case PERCENT_OF_AMOUNT -> installment.getPrincipalOutstanding(getCurrency());
            case PERCENT_OF_AMOUNT_AND_INTEREST ->
                installment.getPrincipalOutstanding(getCurrency()).plus(installment.getInterestOutstanding(getCurrency()));
            case PERCENT_OF_INTEREST -> installment.getInterestOutstanding(getCurrency());
            default -> Money.zero(getCurrency());
        };
    }

    // This method returns date format and locale if present in the JsonCommand
    public Map<String, String> getDateFormatAndLocale(final JsonCommand jsonCommand) {
        Map<String, String> returnObject = new HashMap<>();
        JsonElement jsonElement = jsonCommand.parsedJson();
        if (jsonElement.isJsonObject()) {
            JsonObject topLevel = jsonElement.getAsJsonObject();
            if (topLevel.has(LoanApiConstants.dateFormatParameterName)
                    && topLevel.get(LoanApiConstants.dateFormatParameterName).isJsonPrimitive()) {
                final JsonPrimitive primitive = topLevel.get(LoanApiConstants.dateFormatParameterName).getAsJsonPrimitive();
                returnObject.put(LoanApiConstants.dateFormatParameterName, primitive.getAsString());
            }
            if (topLevel.has(LoanApiConstants.localeParameterName)
                    && topLevel.get(LoanApiConstants.localeParameterName).isJsonPrimitive()) {
                final JsonPrimitive primitive = topLevel.get(LoanApiConstants.localeParameterName).getAsJsonPrimitive();
                String localeString = primitive.getAsString();
                returnObject.put(LoanApiConstants.localeParameterName, localeString);
            }
        }
        return returnObject;
    }

    public Map<String, Object> parseDisbursementDetails(final JsonObject jsonObject, String dateFormat, Locale locale) {
        Map<String, Object> returnObject = new HashMap<>();
        if (jsonObject.get(LoanApiConstants.expectedDisbursementDateParameterName) != null
                && jsonObject.get(LoanApiConstants.expectedDisbursementDateParameterName).isJsonPrimitive()) {
            final JsonPrimitive primitive = jsonObject.get(LoanApiConstants.expectedDisbursementDateParameterName).getAsJsonPrimitive();
            final String valueAsString = primitive.getAsString();
            if (StringUtils.isNotBlank(valueAsString)) {
                LocalDate date = JsonParserHelper.convertFrom(valueAsString, LoanApiConstants.expectedDisbursementDateParameterName,
                        dateFormat, locale);
                if (date != null) {
                    returnObject.put(LoanApiConstants.expectedDisbursementDateParameterName, date);
                }
            }
        }

        if (jsonObject.get(LoanApiConstants.disbursementPrincipalParameterName).isJsonPrimitive()
                && StringUtils.isNotBlank(jsonObject.get(LoanApiConstants.disbursementPrincipalParameterName).getAsString())) {
            BigDecimal principal = jsonObject.getAsJsonPrimitive(LoanApiConstants.disbursementPrincipalParameterName).getAsBigDecimal();
            returnObject.put(LoanApiConstants.disbursementPrincipalParameterName, principal);
        }

        if (jsonObject.has(LoanApiConstants.disbursementIdParameterName)
                && jsonObject.get(LoanApiConstants.disbursementIdParameterName).isJsonPrimitive()
                && StringUtils.isNotBlank(jsonObject.get(LoanApiConstants.disbursementIdParameterName).getAsString())) {
            Long id = jsonObject.getAsJsonPrimitive(LoanApiConstants.disbursementIdParameterName).getAsLong();
            returnObject.put(LoanApiConstants.disbursementIdParameterName, id);
        }

        if (jsonObject.has(LoanApiConstants.loanChargeIdParameterName)
                && jsonObject.get(LoanApiConstants.loanChargeIdParameterName).isJsonPrimitive()
                && StringUtils.isNotBlank(jsonObject.get(LoanApiConstants.loanChargeIdParameterName).getAsString())) {
            returnObject.put(LoanApiConstants.loanChargeIdParameterName,
                    jsonObject.getAsJsonPrimitive(LoanApiConstants.loanChargeIdParameterName).getAsString());
        }
        return returnObject;
    }

    public List<Long> fetchLoanTrancheChargeIds() {
        return getCharges().stream()//
                .filter(charge -> charge.isTrancheDisbursementCharge() && charge.isActive()) //
                .map(LoanCharge::getId) //
                .collect(Collectors.toList());
    }

    public LoanDisbursementDetails fetchLoanDisbursementsById(Long id) {
        return getDisbursementDetails().stream() //
                .filter(disbursementDetail -> id.equals(disbursementDetail.getId())) //
                .findFirst() //
                .orElse(null);
    }

    public List<Long> fetchDisbursementIds() {
        return getDisbursementDetails().stream() //
                .map(LoanDisbursementDetails::getId) //
                .collect(Collectors.toList());
    }

    private LocalDate determineExpectedMaturityDate() {
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments().stream()
                .filter(i -> !i.isDownPayment() && !i.isAdditional()).toList();
        final int numberOfInstallments = installments.size();
        LocalDate maturityDate = installments.get(numberOfInstallments - 1).getDueDate();
        ListIterator<LoanRepaymentScheduleInstallment> iterator = installments.listIterator(numberOfInstallments);
        while (iterator.hasPrevious()) {
            LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = iterator.previous();
            if (!loanRepaymentScheduleInstallment.isRecalculatedInterestComponent()) {
                maturityDate = loanRepaymentScheduleInstallment.getDueDate();
                break;
            }
        }
        return maturityDate;
    }

    public Map<String, Object> undoApproval(final LoanLifecycleStateMachine loanLifecycleStateMachine) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final LoanStatus currentStatus = getStatus();
        final LoanStatus statusEnum = loanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_APPROVAL_UNDO, this);
        if (!statusEnum.hasStateOf(currentStatus)) {
            loanLifecycleStateMachine.transition(LoanEvent.LOAN_APPROVAL_UNDO, this);
            actualChanges.put(PARAM_STATUS, LoanEnumerations.status(this.loanStatus));

            this.approvedOnDate = null;
            this.approvedBy = null;

            if (this.approvedPrincipal.compareTo(this.proposedPrincipal) != 0) {
                this.approvedPrincipal = this.proposedPrincipal;
                this.loanRepaymentScheduleDetail.setPrincipal(this.proposedPrincipal);

                actualChanges.put(LoanApiConstants.approvedLoanAmountParameterName, this.proposedPrincipal);
                actualChanges.put(LoanApiConstants.disbursementPrincipalParameterName, this.proposedPrincipal);

            }

            actualChanges.put(APPROVED_ON_DATE, "");

            this.loanOfficerHistory.clear();
        }

        return actualChanges;
    }

    public List<Long> findExistingTransactionIds() {
        return getLoanTransactions().stream() //
                .filter(loanTransaction -> loanTransaction.getId() != null) //
                .map(LoanTransaction::getId) //
                .collect(Collectors.toList());
    }

    public List<Long> findExistingReversedTransactionIds() {
        return getLoanTransactions().stream() //
                .filter(LoanTransaction::isReversed) //
                .filter(loanTransaction -> loanTransaction.getId() != null) //
                .map(LoanTransaction::getId) //
                .collect(Collectors.toList());
    }

    public List<LoanDisbursementDetails> getDisbursedLoanDisbursementDetails() {
        return getDisbursementDetails().stream() //
                .filter(it -> it.actualDisbursementDate() != null) //
                .collect(Collectors.toList());
    }

    public boolean canDisburse() {
        final LoanStatus statusEnum = this.loanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_DISBURSED, this);

        boolean isMultiTrancheDisburse = false;
        LoanStatus actualLoanStatus = getStatus();
        if ((actualLoanStatus.isActive() || actualLoanStatus.isClosedObligationsMet() || actualLoanStatus.isOverpaid())
                && isAllTranchesNotDisbursed()) {
            isMultiTrancheDisburse = true;
        }
        return !statusEnum.hasStateOf(actualLoanStatus) || isMultiTrancheDisburse;
    }

    public Collection<LoanDisbursementDetails> fetchUndisbursedDetail() {
        Collection<LoanDisbursementDetails> disbursementDetails = new ArrayList<>();
        LocalDate date = null;
        for (LoanDisbursementDetails disbursementDetail : getDisbursementDetails()) {
            if (disbursementDetail.actualDisbursementDate() == null) {
                LocalDate expectedDate = disbursementDetail.expectedDisbursementDate();
                if (date == null || DateUtils.isEqual(expectedDate, date)) {
                    disbursementDetails.add(disbursementDetail);
                    date = expectedDate;
                } else if (DateUtils.isBefore(expectedDate, date)) {
                    disbursementDetails.clear();
                    disbursementDetails.add(disbursementDetail);
                    date = expectedDate;
                }
            }
        }
        return disbursementDetails;
    }

    private LoanDisbursementDetails fetchLastDisburseDetail() {
        LoanDisbursementDetails details = null;
        LocalDate date = this.actualDisbursementDate;
        if (date != null) {
            for (LoanDisbursementDetails disbursementDetail : getDisbursementDetails()) {
                LocalDate actualDate = disbursementDetail.actualDisbursementDate();
                if (!DateUtils.isBefore(actualDate, date)) {
                    date = actualDate;
                    details = disbursementDetail;
                }
            }
        }
        return details;
    }

    public boolean isDisbursementMissed() {
        return getDisbursementDetails().stream() //
                .anyMatch(disbursementDetail -> disbursementDetail.actualDisbursementDate() == null
                        && DateUtils.isBeforeBusinessDate(disbursementDetail.expectedDisbursementDateAsLocalDate()));
    }

    public BigDecimal getDisbursedAmount() {
        BigDecimal principal = BigDecimal.ZERO;
        for (LoanDisbursementDetails disbursementDetail : getDisbursementDetails()) {
            if (disbursementDetail.actualDisbursementDate() != null) {
                principal = principal.add(disbursementDetail.principal());
            }
        }
        return principal;
    }

    public void removeDisbursementDetail() {
        getDisbursementDetails().removeIf(it -> it.actualDisbursementDate() == null);
    }

    public boolean isDisbursementAllowed() {
        List<LoanDisbursementDetails> disbursementDetails = getDisbursementDetails();
        boolean isSingleDisburseLoanDisbursementAllowed = disbursementDetails == null || disbursementDetails.isEmpty()
                || disbursementDetails.stream().anyMatch(it -> it.actualDisbursementDate() == null);
        boolean isMultiDisburseLoanDisbursementAllowed = isMultiDisburmentLoan()
                && (disbursementDetails == null || disbursementDetails.stream().filter(it -> it.actualDisbursementDate() != null)
                        .count() < loanProduct.getLoanProductTrancheDetails().maxTrancheCount());
        return isSingleDisburseLoanDisbursementAllowed || isMultiDisburseLoanDisbursementAllowed;
    }

    public boolean atLeastOnceDisbursed() {
        return getDisbursementDetails().stream().anyMatch(it -> it.actualDisbursementDate() != null);
    }

    public void updateLoanRepaymentPeriodsDerivedFields(final LocalDate actualDisbursementDate) {
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment repaymentPeriod : installments) {
            repaymentPeriod.updateObligationsMet(getCurrency(), actualDisbursementDate);
        }
    }

    public LoanScheduleModel regenerateScheduleModel(final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        final MathContext mc = MoneyHelper.getMathContext();

        final LoanApplicationTerms loanApplicationTerms = constructLoanApplicationTerms(scheduleGeneratorDTO);
        LoanScheduleGenerator loanScheduleGenerator;
        if (loanApplicationTerms.isEqualAmortization()) {
            if (loanApplicationTerms.getInterestMethod().isDecliningBalance()) {
                final LoanScheduleGenerator decliningLoanScheduleGenerator = scheduleGeneratorDTO.getLoanScheduleFactory()
                        .create(loanApplicationTerms.getLoanScheduleType(), InterestMethod.DECLINING_BALANCE);
                Set<LoanCharge> loanCharges = getActiveCharges();
                LoanScheduleModel loanSchedule = decliningLoanScheduleGenerator.generate(mc, loanApplicationTerms, loanCharges,
                        scheduleGeneratorDTO.getHolidayDetailDTO());

                loanApplicationTerms
                        .updateTotalInterestDue(Money.of(loanApplicationTerms.getCurrency(), loanSchedule.getTotalInterestCharged()));

            }
            loanScheduleGenerator = scheduleGeneratorDTO.getLoanScheduleFactory().create(loanApplicationTerms.getLoanScheduleType(),
                    InterestMethod.FLAT);
        } else {
            loanScheduleGenerator = scheduleGeneratorDTO.getLoanScheduleFactory().create(loanApplicationTerms.getLoanScheduleType(),
                    loanApplicationTerms.getInterestMethod());
        }

        return loanScheduleGenerator.generate(mc, loanApplicationTerms, getActiveCharges(), scheduleGeneratorDTO.getHolidayDetailDTO());
    }

    private BigDecimal constructFloatingInterestRates(final BigDecimal annualNominalInterestRate, final FloatingRateDTO floatingRateDTO,
            final List<LoanTermVariationsData> loanTermVariations) {
        final LocalDate dateValue = null;
        final boolean isSpecificToInstallment = false;
        BigDecimal interestRate = annualNominalInterestRate;
        if (loanProduct.isLinkedToFloatingInterestRate()) {
            floatingRateDTO.resetInterestRateDiff();
            Collection<FloatingRatePeriodData> applicableRates = loanProduct.fetchInterestRates(floatingRateDTO);
            LocalDate interestRateStartDate = DateUtils.getBusinessLocalDate();
            for (FloatingRatePeriodData periodData : applicableRates) {
                LoanTermVariationsData loanTermVariation = new LoanTermVariationsData(
                        LoanEnumerations.loanVariationType(LoanTermVariationType.INTEREST_RATE), periodData.getFromDateAsLocalDate(),
                        periodData.getInterestRate(), dateValue, isSpecificToInstallment);
                if (!DateUtils.isBefore(interestRateStartDate, periodData.getFromDateAsLocalDate())) {
                    interestRateStartDate = periodData.getFromDateAsLocalDate();
                    interestRate = periodData.getInterestRate();
                }
                loanTermVariations.add(loanTermVariation);
            }
        }
        return interestRate;
    }

    public boolean isAutoRepaymentForDownPaymentEnabled() {
        return this.loanRepaymentScheduleDetail.isEnableDownPayment()
                && this.loanRepaymentScheduleDetail.isEnableAutoRepaymentForDownPayment();
    }

    public void handlePayDisbursementTransaction(final Long chargeId, final LoanTransaction chargesPayment,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());
        LoanCharge charge = null;
        for (final LoanCharge loanCharge : this.charges) {
            if (loanCharge.isActive() && chargeId.equals(loanCharge.getId())) {
                charge = loanCharge;
            }
        }
        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(chargesPayment, charge, charge.amount(), null);
        chargesPayment.getLoanChargesPaid().add(loanChargePaidBy);
        final Money zero = Money.zero(getCurrency());
        chargesPayment.updateComponents(zero, zero, charge.getAmount(getCurrency()), zero);
        chargesPayment.updateLoan(this);
        addLoanTransaction(chargesPayment);
        updateLoanOutstandingBalances();
        charge.markAsFullyPaid();
    }

    public void removePostDatedChecks() {
        this.postDatedChecks = new ArrayList<>();
    }

    public List<LoanTransaction> retrieveListOfTransactionsForReprocessing() {
        return getLoanTransactions().stream().filter(loanTransactionForReprocessingPredicate()).sorted(LoanTransactionComparator.INSTANCE)
                .collect(Collectors.toList());
    }

    private static Predicate<LoanTransaction> loanTransactionForReprocessingPredicate() {
        return transaction -> transaction.isNotReversed() && (transaction.isChargeOff() || transaction.isReAge()
                || transaction.isAccrualActivity() || transaction.isReAmortize() || !transaction.isNonMonetaryTransaction());
    }

    public List<LoanTransaction> retrieveListOfTransactionsExcludeAccruals() {
        final List<LoanTransaction> repaymentsOrWaivers = new ArrayList<>();
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isNotReversed() && !transaction.isNonMonetaryTransaction()) {
                repaymentsOrWaivers.add(transaction);
            }
        }
        repaymentsOrWaivers.sort(LoanTransactionComparator.INSTANCE);
        return repaymentsOrWaivers;
    }

    public List<LoanTransaction> retrieveListOfTransactionsByType(final LoanTransactionType transactionType) {
        return this.loanTransactions.stream()
                .filter(transaction -> transaction.isNotReversed() && transaction.getTypeOf().equals(transactionType))
                .sorted(LoanTransactionComparator.INSTANCE).collect(Collectors.toList());
    }

    public boolean doPostLoanTransactionChecks(final LocalDate transactionDate, final LoanLifecycleStateMachine loanLifecycleStateMachine) {
        boolean statusChanged = false;
        boolean isOverpaid = MathUtil.isGreaterThanZero(totalOverpaid);
        if (isOverpaid) {
            // FIXME - kw - update account balance to negative amount.
            handleLoanOverpayment(transactionDate, loanLifecycleStateMachine);
            statusChanged = true;
        } else if (this.summary.isRepaidInFull(getCurrency())) {
            handleLoanRepaymentInFull(transactionDate, loanLifecycleStateMachine);
            statusChanged = true;
        } else {
            loanLifecycleStateMachine.transition(LoanEvent.LOAN_REPAYMENT_OR_WAIVER, this);
        }
        if (MathUtil.isEmpty(totalOverpaid)) {
            this.overpaidOnDate = null;
        }
        return statusChanged;
    }

    private void handleLoanRepaymentInFull(final LocalDate transactionDate, final LoanLifecycleStateMachine loanLifecycleStateMachine) {
        boolean isAllChargesPaid = this.charges.stream() //
                .allMatch(charge -> !charge.isActive() || charge.amount().compareTo(BigDecimal.ZERO) <= 0 || charge.isPaid()
                        || charge.isWaived());

        if (isAllChargesPaid) {
            this.closedOnDate = transactionDate;
            this.actualMaturityDate = transactionDate;
            loanLifecycleStateMachine.transition(LoanEvent.REPAID_IN_FULL, this);

        } else if (getStatus().isOverpaid()) {
            if (MathUtil.isEmpty(totalOverpaid)) {
                this.overpaidOnDate = null;
            }
            loanLifecycleStateMachine.transition(LoanEvent.LOAN_REPAYMENT_OR_WAIVER, this);
        }
    }

    private void handleLoanOverpayment(LocalDate transactionDate, final LoanLifecycleStateMachine loanLifecycleStateMachine) {
        this.overpaidOnDate = transactionDate;
        loanLifecycleStateMachine.transition(LoanEvent.LOAN_OVERPAYMENT, this);
        this.closedOnDate = null;
        this.actualMaturityDate = null;
    }

    public boolean isChronologicallyLatestRepaymentOrWaiver(final LoanTransaction loanTransaction) {
        boolean isChronologicallyLatestRepaymentOrWaiver = true;

        final LocalDate currentTransactionDate = loanTransaction.getTransactionDate();
        for (final LoanTransaction previousTransaction : loanTransactions) {
            if (!previousTransaction.isDisbursement() && previousTransaction.isNotReversed()
                    && (DateUtils.isBefore(currentTransactionDate, previousTransaction.getTransactionDate())
                            || (DateUtils.isEqual(currentTransactionDate, previousTransaction.getTransactionDate())
                                    && ((loanTransaction.getId() == null && previousTransaction.getId() == null)
                                            || (loanTransaction.getId() != null && (previousTransaction.getId() == null
                                                    || loanTransaction.getId().compareTo(previousTransaction.getId()) < 0)))))) {
                isChronologicallyLatestRepaymentOrWaiver = false;
                break;
            }
        }
        return isChronologicallyLatestRepaymentOrWaiver;
    }

    public boolean isAfterLastRepayment(final LoanTransaction loanTransaction, final List<LoanTransaction> loanTransactions) {
        return loanTransactions.stream() //
                .filter(t -> t.isRepaymentLikeType() && t.isNotReversed()) //
                .noneMatch(t -> DateUtils.isBefore(loanTransaction.getTransactionDate(), t.getTransactionDate()));
    }

    public boolean isChronologicallyLatestTransaction(final LoanTransaction loanTransaction, final List<LoanTransaction> loanTransactions) {
        return loanTransactions.stream() //
                .filter(LoanTransaction::isNotReversed) //
                .allMatch(t -> DateUtils.isAfter(loanTransaction.getTransactionDate(), t.getTransactionDate()));
    }

    public LocalDate possibleNextRepaymentDate(final String nextPaymentDueDateConfig) {
        if (nextPaymentDueDateConfig == null) {
            return null;
        }
        return switch (nextPaymentDueDateConfig.toLowerCase()) {
            case EARLIEST_UNPAID_DATE -> getEarliestUnpaidInstallmentDate();
            case NEXT_UNPAID_DUE_DATE -> getNextUnpaidInstallmentDueDate();
            default -> null;
        };
    }

    private LocalDate getNextUnpaidInstallmentDueDate() {
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        LocalDate currentBusinessDate = DateUtils.getBusinessLocalDate();
        LocalDate expectedMaturityDate = determineExpectedMaturityDate();
        LocalDate nextUnpaidInstallmentDate = expectedMaturityDate;

        for (final LoanRepaymentScheduleInstallment installment : installments) {
            boolean isCurrentDateBeforeInstallmentAndLoanPeriod = DateUtils.isBefore(currentBusinessDate, installment.getDueDate())
                    && DateUtils.isBefore(currentBusinessDate, expectedMaturityDate);
            if (installment.isDownPayment()) {
                isCurrentDateBeforeInstallmentAndLoanPeriod = DateUtils.isEqual(currentBusinessDate, installment.getDueDate())
                        && DateUtils.isBefore(currentBusinessDate, expectedMaturityDate);
            }
            if (isCurrentDateBeforeInstallmentAndLoanPeriod) {
                if (installment.isNotFullyPaidOff()) {
                    nextUnpaidInstallmentDate = installment.getDueDate();
                    break;
                }
            }
        }
        return nextUnpaidInstallmentDate;
    }

    private LocalDate getEarliestUnpaidInstallmentDate() {
        LocalDate earliestUnpaidInstallmentDate = DateUtils.getBusinessLocalDate();
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            if (installment.isNotFullyPaidOff()) {
                earliestUnpaidInstallmentDate = installment.getDueDate();
                break;
            }
        }

        LocalDate lastTransactionDate = null;
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isRepaymentLikeType() && transaction.isGreaterThanZero()) {
                lastTransactionDate = transaction.getTransactionDate();
            }
        }

        LocalDate possibleNextRepaymentDate = earliestUnpaidInstallmentDate;
        if (DateUtils.isAfter(lastTransactionDate, earliestUnpaidInstallmentDate)) {
            possibleNextRepaymentDate = lastTransactionDate;
        }

        return possibleNextRepaymentDate;
    }

    public LoanTransaction deriveDefaultInterestWaiverTransaction() {
        final Money totalInterestOutstanding = getTotalInterestOutstandingOnLoan();
        Money possibleInterestToWaive = totalInterestOutstanding.copy();
        LocalDate transactionDate = DateUtils.getBusinessLocalDate();

        if (totalInterestOutstanding.isGreaterThanZero()) {
            // find earliest known instance of overdue interest and default to
            // that
            List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
            for (final LoanRepaymentScheduleInstallment scheduledRepayment : installments) {

                final Money outstandingForPeriod = scheduledRepayment.getInterestOutstanding(getCurrency());
                if (scheduledRepayment.isOverdueOn(DateUtils.getBusinessLocalDate()) && scheduledRepayment.isNotFullyPaidOff()
                        && outstandingForPeriod.isGreaterThanZero()) {
                    transactionDate = scheduledRepayment.getDueDate();
                    possibleInterestToWaive = outstandingForPeriod;
                    break;
                }
            }
        }

        return LoanTransaction.waiver(getOffice(), this, possibleInterestToWaive, transactionDate, possibleInterestToWaive,
                possibleInterestToWaive.zero(), ExternalId.empty());
    }

    public LoanTransaction findWriteOffTransaction() {
        return this.loanTransactions.stream() //
                .filter(transaction -> !transaction.isReversed() && transaction.isWriteOff()) //
                .findFirst() //
                .orElse(null);
    }

    public boolean isOverPaid() {
        return calculateTotalOverpayment().isGreaterThanZero();
    }

    public Money calculateTotalOverpayment() {
        Money totalPaidInRepayments = getTotalPaidInRepayments();

        final MonetaryCurrency currency = getCurrency();
        Money cumulativeTotalPaidOnInstallments = Money.zero(currency);
        Money cumulativeTotalWaivedOnInstallments = Money.zero(currency);
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment scheduledRepayment : installments) {
            cumulativeTotalPaidOnInstallments = cumulativeTotalPaidOnInstallments
                    .plus(scheduledRepayment.getPrincipalCompleted(currency).plus(scheduledRepayment.getInterestPaid(currency)))
                    .plus(scheduledRepayment.getFeeChargesPaid(currency)).plus(scheduledRepayment.getPenaltyChargesPaid(currency));

            cumulativeTotalWaivedOnInstallments = cumulativeTotalWaivedOnInstallments.plus(scheduledRepayment.getInterestWaived(currency));
        }

        for (final LoanTransaction loanTransaction : this.loanTransactions) {
            if (loanTransaction.isReversed()) {
                continue;
            }
            if (loanTransaction.isRefund() || loanTransaction.isRefundForActiveLoan()) {
                totalPaidInRepayments = totalPaidInRepayments.minus(loanTransaction.getAmount(currency));
            } else if (loanTransaction.isCreditBalanceRefund()) {
                if (loanTransaction.getPrincipalPortion(currency).isZero()) {
                    totalPaidInRepayments = totalPaidInRepayments.minus(loanTransaction.getOverPaymentPortion(currency));
                }
            } else if (loanTransaction.isChargeback()) {
                if (loanTransaction.getPrincipalPortion(currency).isZero() && getCreditAllocationRules().stream() //
                        .filter(car -> car.getTransactionType().equals(CreditAllocationTransactionType.CHARGEBACK)) //
                        .findAny() //
                        .isEmpty()) {
                    totalPaidInRepayments = totalPaidInRepayments.minus(loanTransaction.getOverPaymentPortion(currency));
                }
            }
        }

        // if total paid in transactions doesnt match repayment schedule then there's an overpayment.
        return totalPaidInRepayments.minus(cumulativeTotalPaidOnInstallments);
    }

    public Money calculateTotalRecoveredPayments() {
        // in case logic for reversing recovered payment is implemented handle subtraction from totalRecoveredPayments
        return getTotalRecoveredPayments();
    }

    public MonetaryCurrency loanCurrency() {
        return this.loanRepaymentScheduleDetail.getCurrency();
    }

    public LocalDate getLatestTransactionDate() {
        LoanTransaction oneOfTheLatestTxn = this.loanTransactions.stream() //
                .filter(loanTransaction -> !loanTransaction.isReversed()) //
                .max(Comparator.comparing(LoanTransaction::getTransactionDate)) //
                .orElse(null);
        return oneOfTheLatestTxn != null ? oneOfTheLatestTxn.getTransactionDate() : null;
    }

    public boolean isNotSubmittedAndPendingApproval() {
        return !isSubmittedAndPendingApproval();
    }

    public LoanStatus getStatus() {
        return this.loanStatus == null ? null : LoanStatus.fromInt(this.loanStatus);
    }

    public Integer getPlainStatus() {
        return this.loanStatus;
    }

    public boolean isSubmittedAndPendingApproval() {
        return getStatus().isSubmittedAndPendingApproval();
    }

    public boolean isApproved() {
        return getStatus().isApproved();
    }

    public boolean isNotDisbursed() {
        return !isDisbursed();
    }

    public boolean isChargesAdditionAllowed() {
        return this.loanProduct.isMultiDisburseLoan() ? !isDisbursementAllowed() : hasDisbursementTransaction();
    }

    public boolean isDisbursed() {
        return hasDisbursementTransaction();
    }

    public boolean isClosed() {
        return getStatus().isClosed() || isCancelled();
    }

    public boolean isClosedObligationsMet() {
        return getStatus().isClosedObligationsMet();
    }

    public boolean isClosedWrittenOff() {
        return getStatus().isClosedWrittenOff();
    }

    public boolean isClosedWithOutstandingAmountMarkedForReschedule() {
        return getStatus().isClosedWithOutsandingAmountMarkedForReschedule();
    }

    public boolean isCancelled() {
        return isRejected() || isWithdrawn();
    }

    private boolean isWithdrawn() {
        return getStatus().isWithdrawnByClient();
    }

    private boolean isRejected() {
        return getStatus().isRejected();
    }

    public boolean isOpen() {
        return getStatus().isActive();
    }

    public boolean isAllTranchesNotDisbursed() {
        LoanStatus actualLoanStatus = getStatus();
        boolean isInRightStatus = actualLoanStatus.isActive() || actualLoanStatus.isApproved() || actualLoanStatus.isClosedObligationsMet()
                || actualLoanStatus.isOverpaid();
        return this.loanProduct.isMultiDisburseLoan() && isInRightStatus && isDisbursementAllowed();
    }

    private boolean hasDisbursementTransaction() {
        return this.loanTransactions.stream()
                .anyMatch(loanTransaction -> loanTransaction.isDisbursement() && loanTransaction.isNotReversed());

    }

    public boolean isSubmittedOnDateAfter(final LocalDate compareDate) {
        return DateUtils.isAfter(this.submittedOnDate, compareDate);
    }

    public LocalDate getExpectedDisbursedOnLocalDate() {
        return this.expectedDisbursementDate;
    }

    public LocalDate getDisbursementDate() {
        LocalDate disbursementDate = getExpectedDisbursedOnLocalDate();
        if (this.actualDisbursementDate != null) {
            disbursementDate = this.actualDisbursementDate;
        }
        return disbursementDate;
    }

    public LocalDate getExpectedDisbursedOnLocalDateForTemplate() {
        LocalDate expectedDisbursementDate = null;
        if (this.expectedDisbursementDate != null) {
            expectedDisbursementDate = this.expectedDisbursementDate;
        }

        Collection<LoanDisbursementDetails> details = fetchUndisbursedDetail();
        if (!details.isEmpty()) {
            for (LoanDisbursementDetails disbursementDetails : details) {
                expectedDisbursementDate = disbursementDetails.expectedDisbursementDate();
            }
        }
        return expectedDisbursementDate;
    }

    public BigDecimal getDisburseAmountForTemplate() {
        BigDecimal principal = this.loanRepaymentScheduleDetail.getPrincipal().getAmount();
        Collection<LoanDisbursementDetails> details = fetchUndisbursedDetail();
        if (!details.isEmpty()) {
            principal = BigDecimal.ZERO;
            for (LoanDisbursementDetails disbursementDetails : details) {
                principal = principal.add(disbursementDetails.principal());
            }
        }
        return principal;
    }

    public boolean isActualDisbursedOnDateEarlierOrLaterThanExpected(final LocalDate actualDisbursedOnDate) {
        boolean isRegenerationRequired = false;
        if (this.loanProduct.isMultiDisburseLoan()) {
            LoanDisbursementDetails details = fetchLastDisburseDetail();
            if (details != null && !DateUtils.isEqual(details.expectedDisbursementDate(), details.actualDisbursementDate())) {
                isRegenerationRequired = true;
            }
        }
        return isRegenerationRequired || !DateUtils.isEqual(actualDisbursedOnDate, this.expectedDisbursementDate);
    }

    public Money getTotalPaidInRepayments() {
        Money cumulativePaid = Money.zero(getCurrency());

        for (final LoanTransaction repayment : this.loanTransactions) {
            if (repayment.isRepaymentLikeType() && !repayment.isReversed()) {
                cumulativePaid = cumulativePaid.plus(repayment.getAmount(getCurrency()));
            }
        }

        return cumulativePaid;
    }

    public Money getTotalRecoveredPayments() {
        Money cumulativePaid = Money.zero(getCurrency());

        for (final LoanTransaction recoveredPayment : this.loanTransactions) {
            if (recoveredPayment.isRecoveryRepayment()) {
                cumulativePaid = cumulativePaid.plus(recoveredPayment.getAmount(getCurrency()));
            }
        }
        return cumulativePaid;
    }

    public Money getTotalPrincipalOutstandingUntil(LocalDate date) {
        return getRepaymentScheduleInstallments().stream()
                .filter(installment -> installment.getDueDate().isBefore(date) || installment.getDueDate().isEqual(date))
                .map(installment -> installment.getPrincipalOutstanding(getCurrency())).reduce(Money.zero(getCurrency()), Money::add);

    }

    public Money getTotalInterestOutstandingOnLoan() {
        Money cumulativeInterest = Money.zero(getCurrency());

        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment scheduledRepayment : installments) {
            cumulativeInterest = cumulativeInterest.plus(scheduledRepayment.getInterestOutstanding(getCurrency()));
        }

        return cumulativeInterest;
    }

    @SuppressWarnings("unused")
    private Money getTotalInterestOverdueOnLoan() {
        Money cumulativeInterestOverdue = Money.zero(this.loanRepaymentScheduleDetail.getPrincipal().getCurrency());
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment scheduledRepayment : installments) {

            final Money interestOutstandingForPeriod = scheduledRepayment.getInterestOutstanding(getCurrency());
            if (scheduledRepayment.isOverdueOn(DateUtils.getBusinessLocalDate())) {
                cumulativeInterestOverdue = cumulativeInterestOverdue.plus(interestOutstandingForPeriod);
            }
        }

        return cumulativeInterestOverdue;
    }

    public Money getInArrearsTolerance() {
        return this.loanRepaymentScheduleDetail.getInArrearsTolerance();
    }

    public boolean hasIdentifyOf(final Long loanId) {
        return loanId.equals(getId());
    }

    public boolean hasLoanOfficer(final Staff fromLoanOfficer) {
        if (this.loanOfficer != null) {
            return this.loanOfficer.identifiedBy(fromLoanOfficer);
        } else {
            return fromLoanOfficer == null;
        }
    }

    public Money getPrincipal() {
        return this.loanRepaymentScheduleDetail.getPrincipal();
    }

    public boolean hasCurrencyCodeOf(final String matchingCurrencyCode) {
        return getCurrencyCode().equalsIgnoreCase(matchingCurrencyCode);
    }

    public String getCurrencyCode() {
        return this.loanRepaymentScheduleDetail.getPrincipal().getCurrencyCode();
    }

    public MonetaryCurrency getCurrency() {
        return this.loanRepaymentScheduleDetail.getCurrency();
    }

    public void removeLoanOfficer(final LocalDate unassignDate) {
        findLatestIncompleteHistoryRecord()
                .ifPresent(loanOfficerAssignmentHistory -> loanOfficerAssignmentHistory.updateEndDate(unassignDate));

        this.loanOfficer = null;
    }

    public Optional<LoanOfficerAssignmentHistory> findLatestIncompleteHistoryRecord() {
        return this.loanOfficerHistory.stream().filter(LoanOfficerAssignmentHistory::isCurrentRecord).findFirst();
    }

    public LoanOfficerAssignmentHistory findLastAssignmentHistoryRecord(final Staff newLoanOfficer) {
        LoanOfficerAssignmentHistory lastAssignmentRecordLatestEndDate = null;
        for (final LoanOfficerAssignmentHistory historyRecord : this.loanOfficerHistory) {
            if (historyRecord.isCurrentRecord() && !historyRecord.isSameLoanOfficer(newLoanOfficer)) {
                lastAssignmentRecordLatestEndDate = historyRecord;
                break;
            }

            if (lastAssignmentRecordLatestEndDate == null) {
                lastAssignmentRecordLatestEndDate = historyRecord;
            } else if (historyRecord.isEndDateAfter(lastAssignmentRecordLatestEndDate.getEndDate())
                    && !historyRecord.isSameLoanOfficer(newLoanOfficer)) {
                lastAssignmentRecordLatestEndDate = historyRecord;
            }
        }
        return lastAssignmentRecordLatestEndDate;
    }

    public Long getClientId() {
        return this.client == null ? null : this.client.getId();
    }

    public Long getGroupId() {
        return this.group == null ? null : this.group.getId();
    }

    public Long getGlimId() {
        return this.glim == null ? null : this.glim.getId();
    }

    public Long getOfficeId() {
        return this.client != null ? this.client.officeId() : this.group.officeId();
    }

    public Office getOffice() {
        return this.client != null ? this.client.getOffice() : this.group.getOffice();
    }

    private Boolean isCashBasedAccountingEnabledOnLoanProduct() {
        return this.loanProduct.isCashBasedAccountingEnabled();
    }

    public Boolean isUpfrontAccrualAccountingEnabledOnLoanProduct() {
        return this.loanProduct.isUpfrontAccrualAccountingEnabled();
    }

    public Boolean isAccountingDisabledOnLoanProduct() {
        return this.loanProduct.isAccountingDisabled();
    }

    public Boolean isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct() {
        return isCashBasedAccountingEnabledOnLoanProduct() || isUpfrontAccrualAccountingEnabledOnLoanProduct()
                || isAccountingDisabledOnLoanProduct();
    }

    public Boolean isPeriodicAccrualAccountingEnabledOnLoanProduct() {
        return this.loanProduct.isPeriodicAccrualAccountingEnabled();
    }

    public Long productId() {
        return this.loanProduct.getId();
    }

    public List<Map<String, Object>> deriveAccountingBridgeDataForChargeOff(final String currencyCode,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds, boolean isAccountTransfer) {

        final List<Map<String, Object>> accountingBridgeData = new ArrayList<>();
        final List<Map<String, Object>> newLoanTransactionsBeforeChargeOff = new ArrayList<>();
        final List<Map<String, Object>> newLoanTransactionsAfterChargeOff = new ArrayList<>();
        // get map before charge-off
        final Map<String, Object> accountingBridgeDataBeforeChargeOff = buildAccountingMapForChargeOffDateCriteria(currencyCode,
                isAccountTransfer, true);
        // get map after charge-off
        final Map<String, Object> accountingBridgeDataAfterChargeOff = buildAccountingMapForChargeOffDateCriteria(currencyCode,
                isAccountTransfer, false);

        // split the transactions according charge-off date
        classifyTransactionsBasedOnChargeOffDate(newLoanTransactionsBeforeChargeOff, newLoanTransactionsAfterChargeOff,
                existingTransactionIds, existingReversedTransactionIds, currencyCode);

        accountingBridgeDataBeforeChargeOff.put("newLoanTransactions", newLoanTransactionsBeforeChargeOff);
        accountingBridgeData.add(accountingBridgeDataBeforeChargeOff);

        accountingBridgeDataAfterChargeOff.put("newLoanTransactions", newLoanTransactionsAfterChargeOff);
        accountingBridgeData.add(accountingBridgeDataAfterChargeOff);

        return accountingBridgeData;
    }

    private void classifyTransactionsBasedOnChargeOffDate(List<Map<String, Object>> newLoanTransactionsBeforeChargeOff,
            List<Map<String, Object>> newLoanTransactionsAfterChargeOff, List<Long> existingTransactionIds,
            List<Long> existingReversedTransactionIds, String currencyCode) {
        // Before
        filterTransactionsByChargeOffDate(newLoanTransactionsBeforeChargeOff, currencyCode, existingTransactionIds,
                existingReversedTransactionIds, transaction -> DateUtils.isBefore(transaction.getTransactionDate(), getChargedOffOnDate()));
        // On
        filterTransactionsByChargeOffDate(newLoanTransactionsBeforeChargeOff, newLoanTransactionsAfterChargeOff, currencyCode,
                existingTransactionIds, existingReversedTransactionIds,
                transaction -> DateUtils.isEqual(transaction.getTransactionDate(), getChargedOffOnDate()));
        // After
        filterTransactionsByChargeOffDate(newLoanTransactionsAfterChargeOff, currencyCode, existingTransactionIds,
                existingReversedTransactionIds, transaction -> DateUtils.isAfter(transaction.getTransactionDate(), getChargedOffOnDate()));
    }

    private Map<String, Object> getAccountingBridgeDataGenericAttributes(final String currencyCode, boolean isAccountTransfer) {
        final Map<String, Object> accountingBridgeDataGenericAttributes = new LinkedHashMap<>();
        accountingBridgeDataGenericAttributes.put("loanId", getId());
        accountingBridgeDataGenericAttributes.put("loanProductId", productId());
        accountingBridgeDataGenericAttributes.put("officeId", getOfficeId());
        accountingBridgeDataGenericAttributes.put("currencyCode", currencyCode);
        accountingBridgeDataGenericAttributes.put("calculatedInterest", this.summary.getTotalInterestCharged());
        accountingBridgeDataGenericAttributes.put("cashBasedAccountingEnabled", isCashBasedAccountingEnabledOnLoanProduct());
        accountingBridgeDataGenericAttributes.put("upfrontAccrualBasedAccountingEnabled", isUpfrontAccrualAccountingEnabledOnLoanProduct());
        accountingBridgeDataGenericAttributes.put("periodicAccrualBasedAccountingEnabled",
                isPeriodicAccrualAccountingEnabledOnLoanProduct());
        accountingBridgeDataGenericAttributes.put("isAccountTransfer", isAccountTransfer);
        return accountingBridgeDataGenericAttributes;
    }

    private Map<String, Object> buildAccountingMapForChargeOffDateCriteria(final String currencyCode, boolean isAccountTransfer,
            boolean isBeforeChargeOffDate) {
        final Map<String, Object> accountingBridgeDataChargeOff = new LinkedHashMap<>(
                getAccountingBridgeDataGenericAttributes(currencyCode, isAccountTransfer));
        if (isBeforeChargeOffDate) {
            accountingBridgeDataChargeOff.put("isChargeOff", false);
            accountingBridgeDataChargeOff.put("isFraud", isFraud());
        } else {
            accountingBridgeDataChargeOff.put("isChargeOff", isChargedOff());
            accountingBridgeDataChargeOff.put("isFraud", isFraud());
        }
        return accountingBridgeDataChargeOff;
    }

    private void filterTransactionsByChargeOffDate(List<Map<String, Object>> filteredTransactions, final String currencyCode,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds,
            Predicate<LoanTransaction> chargeOffDateCriteria) {
        filteredTransactions.addAll(this.loanTransactions.stream() //
                .filter(chargeOffDateCriteria) //
                .filter(transaction -> {
                    boolean isExistingTransaction = existingTransactionIds.contains(transaction.getId());
                    boolean isExistingReversedTransaction = existingReversedTransactionIds.contains(transaction.getId());

                    if (transaction.isReversed() && isExistingTransaction && !isExistingReversedTransaction) {
                        return true;
                    } else {
                        return !isExistingTransaction;
                    }
                }) //
                .map(transaction -> transaction.toMapData(currencyCode)).toList());
    }

    private void filterTransactionsByChargeOffDate(List<Map<String, Object>> newLoanTransactionsBeforeChargeOff,
            List<Map<String, Object>> newLoanTransactionsAfterChargeOff, String currencyCode, List<Long> existingTransactionIds,
            List<Long> existingReversedTransactionIds, Predicate<LoanTransaction> chargeOffDateCriteria) {

        LoanTransaction chargeOffTransaction = this.loanTransactions.stream() //
                .filter(LoanTransaction::isChargeOff) //
                .filter(LoanTransaction::isNotReversed) //
                .findFirst().get();

        LoanTransaction originalChargeOffTransaction = getOriginalTransactionIfReverseReplayed(chargeOffTransaction);

        this.loanTransactions.stream().filter(chargeOffDateCriteria).forEach(transaction -> {
            boolean isExistingTransaction = existingTransactionIds.contains(transaction.getId());
            boolean isExistingReversedTransaction = existingReversedTransactionIds.contains(transaction.getId());
            List<Map<String, Object>> targetList = null;
            if ((transaction.isReversed() && isExistingTransaction && !isExistingReversedTransaction)) {
                // reversed transactions
                LoanTransaction originalTransaction = getOriginalTransactionIfReverseReplayed(transaction);
                targetList = originalTransaction.happenedBefore(originalChargeOffTransaction) ? newLoanTransactionsBeforeChargeOff
                        : newLoanTransactionsAfterChargeOff;

            } else if (!isExistingTransaction) {
                // new and replayed transactions
                targetList = transaction.happenedBefore(chargeOffTransaction) ? newLoanTransactionsBeforeChargeOff
                        : newLoanTransactionsAfterChargeOff;
            }
            if (targetList != null) {
                targetList.add(transaction.toMapData(currencyCode));
            }
        });
    }

    private LoanTransaction getOriginalTransactionIfReverseReplayed(LoanTransaction loanTransaction) {
        if (!loanTransaction.getLoanTransactionRelations().isEmpty()) {
            return loanTransaction.getLoanTransactionRelations().stream()
                    .filter(tr -> LoanTransactionRelationTypeEnum.REPLAYED.equals(tr.getRelationType())).map(tr -> tr.getToTransaction())
                    .collect(Collectors.toList()).stream().sorted(Comparator.comparingLong(LoanTransaction::getId)).findFirst()
                    .orElse(loanTransaction);
        }
        return loanTransaction;
    }

    public Map<String, Object> deriveAccountingBridgeData(final String currencyCode, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, boolean isAccountTransfer) {

        final Map<String, Object> accountingBridgeData = new LinkedHashMap<>();
        accountingBridgeData.put("loanId", getId());
        accountingBridgeData.put("loanProductId", productId());
        accountingBridgeData.put("officeId", getOfficeId());
        accountingBridgeData.put("currencyCode", currencyCode);
        accountingBridgeData.put("calculatedInterest", this.summary.getTotalInterestCharged());
        accountingBridgeData.put("cashBasedAccountingEnabled", isCashBasedAccountingEnabledOnLoanProduct());
        accountingBridgeData.put("upfrontAccrualBasedAccountingEnabled", isUpfrontAccrualAccountingEnabledOnLoanProduct());
        accountingBridgeData.put("periodicAccrualBasedAccountingEnabled", isPeriodicAccrualAccountingEnabledOnLoanProduct());
        accountingBridgeData.put("isAccountTransfer", isAccountTransfer);
        accountingBridgeData.put("isChargeOff", isChargedOff());
        accountingBridgeData.put("isFraud", isFraud());

        final List<Map<String, Object>> newLoanTransactions = new ArrayList<>();
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isReversed() && existingTransactionIds.contains(transaction.getId())
                    && !existingReversedTransactionIds.contains(transaction.getId())) {
                newLoanTransactions.add(transaction.toMapData(currencyCode));
            } else if (!existingTransactionIds.contains(transaction.getId())) {
                newLoanTransactions.add(transaction.toMapData(currencyCode));
            }
        }

        accountingBridgeData.put("newLoanTransactions", newLoanTransactions);
        return accountingBridgeData;
    }

    public Money getReceivableInterest(final LocalDate tillDate) {
        Money receivableInterest = Money.zero(getCurrency());
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isNotReversed() && !transaction.isRepaymentAtDisbursement() && !transaction.isDisbursement()
                    && !DateUtils.isAfter(transaction.getTransactionDate(), tillDate)) {
                if (transaction.isAccrual()) {
                    receivableInterest = receivableInterest.plus(transaction.getInterestPortion(getCurrency()));
                } else if (transaction.isRepaymentLikeType() || transaction.isInterestWaiver() || transaction.isAccrualAdjustment()) {
                    receivableInterest = receivableInterest.minus(transaction.getInterestPortion(getCurrency()));
                }
            }
            if (receivableInterest.isLessThanZero()) {
                receivableInterest = receivableInterest.zero();
            }
            /*
             * if (transaction.getTransactionDate().isAfter(tillDate) && transaction.isAccrual()) { final String
             * errorMessage = "The date on which a loan is interest waived cannot be in after accrual transactions." ;
             * throw new InvalidLoanStateTransitionException("waive", "cannot.be.after.accrual.date", errorMessage,
             * tillDate); }
             */
        }
        return receivableInterest;
    }

    public void setHelpers(final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final LoanRepaymentScheduleTransactionProcessorFactory transactionProcessorFactory) {
        this.loanLifecycleStateMachine = loanLifecycleStateMachine;
        this.transactionProcessorFactory = transactionProcessorFactory;
    }

    public boolean isSyncDisbursementWithMeeting() {
        return this.syncDisbursementWithMeeting != null && this.syncDisbursementWithMeeting;
    }

    public void updateLoanRepaymentScheduleDates(final String recurringRule, final boolean isHolidayEnabled, final List<Holiday> holidays,
            final WorkingDays workingDays, final LocalDate presentMeetingDate, final LocalDate newMeetingDate,
            final boolean isSkipRepaymentOnFirstDayOfMonth, final Integer numberOfDays) {
        // first repayment's from date is same as disbursement date.
        // meetingStartDate is used as seedDate Capture the seedDate from user and use the seedDate as meetingStart date

        LocalDate tmpFromDate = getDisbursementDate();
        final PeriodFrequencyType repaymentPeriodFrequencyType = this.loanRepaymentScheduleDetail.getRepaymentPeriodFrequencyType();
        final Integer loanRepaymentInterval = this.loanRepaymentScheduleDetail.getRepayEvery();
        final String frequency = CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(repaymentPeriodFrequencyType);

        LocalDate newRepaymentDate;
        boolean isFirstTime = true;
        LocalDate latestRepaymentDate = null;
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : installments) {
            LocalDate oldDueDate = loanRepaymentScheduleInstallment.getDueDate();
            if (!DateUtils.isBefore(oldDueDate, presentMeetingDate)) {
                if (isFirstTime) {
                    isFirstTime = false;
                    newRepaymentDate = newMeetingDate;
                } else {
                    // tmpFromDate.plusDays(1) is done to make sure
                    // getNewRepaymentMeetingDate method returns next meeting
                    // date and not the same as tmpFromDate
                    newRepaymentDate = CalendarUtils.getNewRepaymentMeetingDate(recurringRule, tmpFromDate, tmpFromDate.plusDays(1),
                            loanRepaymentInterval, frequency, workingDays, isSkipRepaymentOnFirstDayOfMonth, numberOfDays);
                }

                if (isHolidayEnabled) {
                    newRepaymentDate = HolidayUtil.getRepaymentRescheduleDateToIfHoliday(newRepaymentDate, holidays);
                }
                if (DateUtils.isBefore(latestRepaymentDate, newRepaymentDate)) {
                    latestRepaymentDate = newRepaymentDate;
                }
                loanRepaymentScheduleInstallment.updateDueDate(newRepaymentDate);
                // reset from date to get actual daysInPeriod

                loanRepaymentScheduleInstallment.updateFromDate(tmpFromDate);

                tmpFromDate = newRepaymentDate;// update with new repayment date
            } else {
                tmpFromDate = oldDueDate;
            }
        }
        if (latestRepaymentDate != null) {
            this.expectedMaturityDate = latestRepaymentDate;
        }
    }

    public void updateLoanRepaymentScheduleDates(final LocalDate meetingStartDate, final String recuringRule,
            final boolean isHolidayEnabled, final List<Holiday> holidays, final WorkingDays workingDays,
            final boolean isSkipRepaymentonfirstdayofmonth, final Integer numberofDays) {
        // first repayment's from date is same as disbursement date.
        LocalDate tmpFromDate = getDisbursementDate();
        final PeriodFrequencyType repaymentPeriodFrequencyType = this.loanRepaymentScheduleDetail.getRepaymentPeriodFrequencyType();
        final Integer loanRepaymentInterval = this.loanRepaymentScheduleDetail.getRepayEvery();
        final String frequency = CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(repaymentPeriodFrequencyType);

        LocalDate newRepaymentDate = null;
        LocalDate seedDate = meetingStartDate;
        LocalDate latestRepaymentDate = null;
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : installments) {
            LocalDate oldDueDate = loanRepaymentScheduleInstallment.getDueDate();

            // FIXME: AA this won't update repayment dates before current date.

            if (DateUtils.isAfter(oldDueDate, seedDate) && DateUtils.isDateInTheFuture(oldDueDate)) {
                newRepaymentDate = CalendarUtils.getNewRepaymentMeetingDate(recuringRule, seedDate, oldDueDate, loanRepaymentInterval,
                        frequency, workingDays, isSkipRepaymentonfirstdayofmonth, numberofDays);

                final LocalDate maxDateLimitForNewRepayment = getMaxDateLimitForNewRepayment(repaymentPeriodFrequencyType,
                        loanRepaymentInterval, tmpFromDate);

                if (DateUtils.isAfter(newRepaymentDate, maxDateLimitForNewRepayment)) {
                    newRepaymentDate = CalendarUtils.getNextRepaymentMeetingDate(recuringRule, seedDate, tmpFromDate, loanRepaymentInterval,
                            frequency, workingDays, isSkipRepaymentonfirstdayofmonth, numberofDays);
                }

                if (isHolidayEnabled) {
                    newRepaymentDate = HolidayUtil.getRepaymentRescheduleDateToIfHoliday(newRepaymentDate, holidays);
                }
                if (DateUtils.isBefore(latestRepaymentDate, newRepaymentDate)) {
                    latestRepaymentDate = newRepaymentDate;
                }

                loanRepaymentScheduleInstallment.updateDueDate(newRepaymentDate);
                // reset from date to get actual daysInPeriod
                loanRepaymentScheduleInstallment.updateFromDate(tmpFromDate);
                tmpFromDate = newRepaymentDate;// update with new repayment date
            } else {
                tmpFromDate = oldDueDate;
            }
        }
        if (latestRepaymentDate != null) {
            this.expectedMaturityDate = latestRepaymentDate;
        }
    }

    private LocalDate getMaxDateLimitForNewRepayment(final PeriodFrequencyType periodFrequencyType, final Integer loanRepaymentInterval,
            final LocalDate startDate) {
        LocalDate dueRepaymentPeriodDate = startDate;
        final int repaidEvery = 2 * loanRepaymentInterval;
        switch (periodFrequencyType) {
            case DAYS -> dueRepaymentPeriodDate = startDate.plusDays(repaidEvery);
            case WEEKS -> dueRepaymentPeriodDate = startDate.plusWeeks(repaidEvery);
            case MONTHS -> dueRepaymentPeriodDate = startDate.plusMonths(repaidEvery);
            case YEARS -> dueRepaymentPeriodDate = startDate.plusYears(repaidEvery);
            case INVALID, WHOLE_TERM -> {
            }
        }
        return dueRepaymentPeriodDate.minusDays(1);// get 2n-1 range date from startDate
    }

    public Group group() {
        return this.group;
    }

    public void updateGroup(final Group newGroup) {
        this.group = newGroup;
    }

    public Integer getCurrentLoanCounter() {
        return this.loanCounter;
    }

    public Integer getLoanProductLoanCounter() {
        if (this.loanProductCounter == null) {
            return 0;
        }
        return this.loanProductCounter;
    }

    public void updateClientLoanCounter(final Integer newLoanCounter) {
        this.loanCounter = newLoanCounter;
    }

    public void updateLoanProductLoanCounter(final Integer newLoanProductLoanCounter) {
        this.loanProductCounter = newLoanProductLoanCounter;
    }

    public boolean isGroupLoan() {
        return AccountType.fromInt(this.loanType).isGroupAccount();
    }

    public boolean isJLGLoan() {
        return AccountType.fromInt(this.loanType).isJLGAccount();
    }

    public void updateInterestRateFrequencyType() {
        this.loanRepaymentScheduleDetail.setInterestPeriodFrequencyType(this.loanProduct.getInterestPeriodFrequencyType());
    }

    public void addLoanTransaction(final LoanTransaction loanTransaction) {
        this.loanTransactions.add(loanTransaction);
    }

    public LocalDate getLastUserTransactionDate() {
        LocalDate currentTransactionDate = getDisbursementDate();
        for (final LoanTransaction previousTransaction : this.loanTransactions) {
            if (!(previousTransaction.isReversed() || previousTransaction.isAccrualRelated() || previousTransaction.isIncomePosting())
                    && DateUtils.isBefore(currentTransactionDate, previousTransaction.getTransactionDate())) {
                currentTransactionDate = previousTransaction.getTransactionDate();
            }
        }
        return currentTransactionDate;
    }

    public LocalDate getLastRepaymentDate() {
        LocalDate currentTransactionDate = getDisbursementDate();
        for (final LoanTransaction previousTransaction : this.loanTransactions) {
            if (previousTransaction.isRepaymentLikeType()
                    && DateUtils.isBefore(currentTransactionDate, previousTransaction.getTransactionDate())) {
                currentTransactionDate = previousTransaction.getTransactionDate();
            }
        }
        return currentTransactionDate;
    }

    public LoanTransaction getLastTransactionForReprocessing() {
        return loanTransactions.stream() //
                .filter(Loan.loanTransactionForReprocessingPredicate()) //
                .reduce((first, second) -> second) //
                .orElse(null);
    }

    public LoanTransaction getLastPaymentTransaction() {
        return loanTransactions.stream() //
                .filter(loanTransaction -> !loanTransaction.isReversed()) //
                .filter(LoanTransaction::isRepaymentLikeType) //
                .reduce((first, second) -> second) //
                .orElse(null);
    }

    public LoanTransaction getLastRepaymentOrDownPaymentTransaction() {
        return loanTransactions.stream() //
                .filter(loanTransaction -> !loanTransaction.isReversed()) //
                .filter(loanTransaction -> loanTransaction.isRepayment() || loanTransaction.isDownPayment()) //
                .reduce((first, second) -> second) //
                .orElse(null);
    }

    public Set<LoanCharge> getActiveCharges() {
        return this.charges == null ? new HashSet<>() : this.charges.stream().filter(LoanCharge::isActive).collect(Collectors.toSet());
    }

    public List<LoanInstallmentCharge> generateInstallmentLoanCharges(final LoanCharge loanCharge) {
        final List<LoanInstallmentCharge> loanChargePerInstallments = new ArrayList<>();
        if (loanCharge.isInstalmentFee()) {
            List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
            for (final LoanRepaymentScheduleInstallment installment : installments) {
                if (installment.isRecalculatedInterestComponent()) {
                    continue;
                }
                BigDecimal amount;
                if (loanCharge.getChargeCalculation().isFlat()) {
                    amount = loanCharge.amountOrPercentage();
                } else {
                    amount = calculateInstallmentChargeAmount(loanCharge.getChargeCalculation(), loanCharge.getPercentage(), installment)
                            .getAmount();
                }
                final LoanInstallmentCharge loanInstallmentCharge = new LoanInstallmentCharge(amount, loanCharge, installment);
                installment.getInstallmentCharges().add(loanInstallmentCharge);
                loanChargePerInstallments.add(loanInstallmentCharge);
            }
        }
        return loanChargePerInstallments;
    }

    public LoanCharge fetchLoanChargesById(Long id) {
        LoanCharge charge = null;
        for (LoanCharge loanCharge : this.charges) {
            if (id.equals(loanCharge.getId())) {
                charge = loanCharge;
                break;
            }
        }
        return charge;
    }

    private List<Long> fetchAllLoanChargeIds() {
        List<Long> list = new ArrayList<>();
        for (LoanCharge loanCharge : this.charges) {
            list.add(loanCharge.getId());
        }
        return list;
    }

    public List<LoanDisbursementDetails> getAllDisbursementDetails() {
        return this.disbursementDetails;
    }

    public List<LoanDisbursementDetails> getDisbursementDetails() {
        List<LoanDisbursementDetails> currentDisbursementDetails = new ArrayList<>();
        for (LoanDisbursementDetails disbursementDetail : this.disbursementDetails) {
            if (!disbursementDetail.isReversed()) {
                currentDisbursementDetails.add(disbursementDetail);
            }
        }
        return currentDisbursementDetails;
    }

    public void clearDisbursementDetails() {
        this.disbursementDetails.clear();
    }

    public LoanDisbursementDetails getDisbursementDetails(final LocalDate transactionDate, final BigDecimal transactionAmount) {
        for (LoanDisbursementDetails disbursementDetail : this.disbursementDetails) {
            if (!disbursementDetail.isReversed() && disbursementDetail.getDisbursementDate().equals(transactionDate)
                    && (disbursementDetail.principal().compareTo(transactionAmount) == 0)) {
                return disbursementDetail;
            }
        }
        return null;
    }

    public BigDecimal getPrincipalAmountForRepaymentSchedule() {
        BigDecimal principalAmount = BigDecimal.ZERO;

        if (isMultiDisburmentLoan() && isDisbursed()) {
            Collection<LoanDisbursementDetails> loanDisburseDetails = this.getDisbursementDetails();
            for (LoanDisbursementDetails details : loanDisburseDetails) {
                if (details.actualDisbursementDate() != null) {
                    principalAmount = principalAmount.add(details.principal());
                }
            }
        } else if (isApproved()) {
            principalAmount = getApprovedPrincipal();
        } else {
            principalAmount = getPrincipal().getAmount();
        }

        return principalAmount;
    }

    public BigDecimal retriveLastEmiAmount() {
        BigDecimal emiAmount = this.fixedEmiAmount;
        LocalDate startDate = this.getDisbursementDate();
        for (LoanTermVariations loanTermVariations : this.loanTermVariations) {
            if (loanTermVariations.getTermType().isEMIAmountVariation()
                    && !DateUtils.isAfter(startDate, loanTermVariations.getTermApplicableFrom())) {
                startDate = loanTermVariations.getTermApplicableFrom();
                emiAmount = loanTermVariations.getTermValue();
            }
        }
        return emiAmount;
    }

    public Money getTotalOverpaidAsMoney() {
        return Money.of(this.repaymentScheduleDetail().getCurrency(), this.totalOverpaid);
    }

    public void updateIsInterestRecalculationEnabled() {
        this.loanRepaymentScheduleDetail.setInterestRecalculationEnabled(isInterestRecalculationEnabledForProduct());
    }

    public LoanInterestRecalculationDetails loanInterestRecalculationDetails() {
        return this.loanInterestRecalculationDetails;
    }

    public Long loanInterestRecalculationDetailId() {
        if (loanInterestRecalculationDetails() != null) {
            return this.loanInterestRecalculationDetails.getId();
        }
        return null;
    }

    public boolean isInterestBearing() {
        return BigDecimal.ZERO.compareTo(getLoanRepaymentScheduleDetail().getAnnualNominalInterestRate()) < 0;
    }

    public boolean isInterestRecalculationEnabled() {
        return this.loanRepaymentScheduleDetail.isInterestRecalculationEnabled();
    }

    public LocalDate getMaturityDate() {
        return this.actualMaturityDate;
    }

    public ChangedTransactionDetail processTransactions() {
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = getTransactionProcessor();
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsForReprocessing();
        ChangedTransactionDetail changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(
                getDisbursementDate(), allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(),
                getActiveCharges());
        for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
            mapEntry.getValue().updateLoan(this);
        }
        /*
         * Commented since throwing exception if external id present for one of the transactions. for this need to save
         * the reversed transactions first and then new transactions.
         */
        this.loanTransactions.addAll(changedTransactionDetail.getNewTransactionMappings().values());
        updateLoanSummaryDerivedFields();

        return changedTransactionDetail;
    }

    public void processPostDisbursementTransactions() {
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = getTransactionProcessor();
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsForReprocessing();
        final List<LoanTransaction> copyTransactions = new ArrayList<>();
        if (!allNonContraTransactionsPostDisbursement.isEmpty()) {
            for (LoanTransaction loanTransaction : allNonContraTransactionsPostDisbursement) {
                copyTransactions.add(LoanTransaction.copyTransactionProperties(loanTransaction));
            }
            loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(getDisbursementDate(), copyTransactions, getCurrency(),
                    getRepaymentScheduleInstallments(), getActiveCharges());

            updateLoanSummaryDerivedFields();
        }
    }

    public LoanScheduleDTO getRecalculatedSchedule(final ScheduleGeneratorDTO generatorDTO) {
        if (!this.repaymentScheduleDetail().isEnableDownPayment()
                && (!this.repaymentScheduleDetail().isInterestRecalculationEnabled() || isNpa || isChargedOff())) {
            return null;
        }
        final InterestMethod interestMethod = this.loanRepaymentScheduleDetail.getInterestMethod();
        final LoanScheduleGenerator loanScheduleGenerator = generatorDTO.getLoanScheduleFactory()
                .create(this.loanRepaymentScheduleDetail.getLoanScheduleType(), interestMethod);

        final MathContext mc = MoneyHelper.getMathContext();

        final LoanApplicationTerms loanApplicationTerms = constructLoanApplicationTerms(generatorDTO);

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = getTransactionProcessor();

        return loanScheduleGenerator.rescheduleNextInstallments(mc, loanApplicationTerms, this, generatorDTO.getHolidayDetailDTO(),
                loanRepaymentScheduleTransactionProcessor, generatorDTO.getRecalculateFrom());
    }

    public OutstandingAmountsDTO fetchPrepaymentDetail(final ScheduleGeneratorDTO scheduleGeneratorDTO, final LocalDate onDate) {
        OutstandingAmountsDTO outstandingAmounts;

        if (this.loanRepaymentScheduleDetail.isInterestRecalculationEnabled()) {
            final MathContext mc = MoneyHelper.getMathContext();

            final InterestMethod interestMethod = this.loanRepaymentScheduleDetail.getInterestMethod();
            final LoanApplicationTerms loanApplicationTerms = constructLoanApplicationTerms(scheduleGeneratorDTO);

            final LoanScheduleGenerator loanScheduleGenerator = scheduleGeneratorDTO.getLoanScheduleFactory()
                    .create(loanApplicationTerms.getLoanScheduleType(), interestMethod);
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = getTransactionProcessor();
            outstandingAmounts = loanScheduleGenerator.calculatePrepaymentAmount(getCurrency(), onDate, loanApplicationTerms, mc, this,
                    scheduleGeneratorDTO.getHolidayDetailDTO(), loanRepaymentScheduleTransactionProcessor);
        } else {
            outstandingAmounts = this.getTotalOutstandingOnLoan();
        }
        return outstandingAmounts;
    }

    public LoanApplicationTerms constructLoanApplicationTerms(final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        final Integer loanTermFrequency = this.termFrequency;
        final PeriodFrequencyType loanTermPeriodFrequencyType = PeriodFrequencyType.fromInt(this.termPeriodFrequencyType);
        NthDayType nthDayType = null;
        DayOfWeekType dayOfWeekType = null;
        final List<DisbursementData> disbursementData = new ArrayList<>();
        for (LoanDisbursementDetails disbursementDetails : getDisbursementDetails()) {
            disbursementData.add(disbursementDetails.toData());
        }

        Calendar calendar = scheduleGeneratorDTO.getCalendar();
        if (calendar != null) {
            nthDayType = CalendarUtils.getRepeatsOnNthDayOfMonth(calendar.getRecurrence());
            dayOfWeekType = DayOfWeekType.fromInt(CalendarUtils.getRepeatsOnDay(calendar.getRecurrence()).getValue());
        }
        HolidayDetailDTO holidayDetailDTO = scheduleGeneratorDTO.getHolidayDetailDTO();
        CalendarInstance restCalendarInstance = null;
        CalendarInstance compoundingCalendarInstance = null;
        RecalculationFrequencyType recalculationFrequencyType = null;
        InterestRecalculationCompoundingMethod compoundingMethod = null;
        RecalculationFrequencyType compoundingFrequencyType = null;
        LoanRescheduleStrategyMethod rescheduleStrategyMethod = null;
        CalendarHistoryDataWrapper calendarHistoryDataWrapper;
        RepaymentStartDateType repaymentStartDateType = this.getLoanProduct().getRepaymentStartDateType();
        boolean allowCompoundingOnEod = false;
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = scheduleGeneratorDTO.getCalendarInstanceForInterestRecalculation();
            compoundingCalendarInstance = scheduleGeneratorDTO.getCompoundingCalendarInstance();
            recalculationFrequencyType = this.loanInterestRecalculationDetails.getRestFrequencyType();
            compoundingMethod = this.loanInterestRecalculationDetails.getInterestRecalculationCompoundingMethod();
            compoundingFrequencyType = this.loanInterestRecalculationDetails.getCompoundingFrequencyType();
            rescheduleStrategyMethod = this.loanInterestRecalculationDetails.getRescheduleStrategyMethod();
            allowCompoundingOnEod = this.loanInterestRecalculationDetails.allowCompoundingOnEod();
        }
        calendar = scheduleGeneratorDTO.getCalendar();
        calendarHistoryDataWrapper = scheduleGeneratorDTO.getCalendarHistoryDataWrapper();

        BigDecimal annualNominalInterestRate = this.loanRepaymentScheduleDetail.getAnnualNominalInterestRate();
        FloatingRateDTO floatingRateDTO = scheduleGeneratorDTO.getFloatingRateDTO();
        List<LoanTermVariationsData> loanTermVariations = new ArrayList<>();
        annualNominalInterestRate = constructLoanTermVariations(floatingRateDTO, annualNominalInterestRate, loanTermVariations);
        LocalDate interestChargedFromDate = getInterestChargedFromDate();
        if (interestChargedFromDate == null && scheduleGeneratorDTO.isInterestChargedFromDateAsDisbursementDateEnabled()) {
            interestChargedFromDate = getDisbursementDate();
        }

        return LoanApplicationTerms.assembleFrom(scheduleGeneratorDTO.getCurrency(), loanTermFrequency, loanTermPeriodFrequencyType,
                nthDayType, dayOfWeekType, getDisbursementDate(), getExpectedFirstRepaymentOnDate(),
                scheduleGeneratorDTO.getCalculatedRepaymentsStartingFromDate(), getInArrearsTolerance(), this.loanRepaymentScheduleDetail,
                this.loanProduct.isMultiDisburseLoan(), this.fixedEmiAmount, disbursementData, this.maxOutstandingLoanBalance,
                interestChargedFromDate, this.loanProduct.getPrincipalThresholdForLastInstallment(),
                this.loanProduct.getInstallmentAmountInMultiplesOf(), recalculationFrequencyType, restCalendarInstance, compoundingMethod,
                compoundingCalendarInstance, compoundingFrequencyType, this.loanProduct.preCloseInterestCalculationStrategy(),
                rescheduleStrategyMethod, calendar, getApprovedPrincipal(), annualNominalInterestRate, loanTermVariations,
                calendarHistoryDataWrapper, scheduleGeneratorDTO.getNumberOfdays(), scheduleGeneratorDTO.isSkipRepaymentOnFirstDayofMonth(),
                holidayDetailDTO, allowCompoundingOnEod, scheduleGeneratorDTO.isFirstRepaymentDateAllowedOnHoliday(),
                scheduleGeneratorDTO.isInterestToBeRecoveredFirstWhenGreaterThanEMI(), this.fixedPrincipalPercentagePerInstallment,
                scheduleGeneratorDTO.isPrincipalCompoundingDisabledForOverdueLoans(), repaymentStartDateType, getSubmittedOnDate());
    }

    public BigDecimal constructLoanTermVariations(FloatingRateDTO floatingRateDTO, BigDecimal annualNominalInterestRate,
            List<LoanTermVariationsData> loanTermVariations) {
        for (LoanTermVariations variationTerms : this.loanTermVariations) {
            if (variationTerms.isActive()) {
                loanTermVariations.add(variationTerms.toData());
            }
        }
        annualNominalInterestRate = constructFloatingInterestRates(annualNominalInterestRate, floatingRateDTO, loanTermVariations);
        return annualNominalInterestRate;
    }

    private OutstandingAmountsDTO getTotalOutstandingOnLoan() {
        Money totalPrincipal = Money.zero(getCurrency());
        Money totalInterest = Money.zero(getCurrency());
        Money feeCharges = Money.zero(getCurrency());
        Money penaltyCharges = Money.zero(getCurrency());
        final Set<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails = null;
        List<LoanRepaymentScheduleInstallment> repaymentSchedule = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment scheduledRepayment : repaymentSchedule) {
            totalPrincipal = totalPrincipal.plus(scheduledRepayment.getPrincipalOutstanding(getCurrency()));
            totalInterest = totalInterest.plus(scheduledRepayment.getInterestOutstanding(getCurrency()));
            feeCharges = feeCharges.plus(scheduledRepayment.getFeeChargesOutstanding(getCurrency()));
            penaltyCharges = penaltyCharges.plus(scheduledRepayment.getPenaltyChargesOutstanding(getCurrency()));
        }
        return new OutstandingAmountsDTO(totalPrincipal.getCurrency()).principal(totalPrincipal).interest(totalInterest)
                .feeCharges(feeCharges).penaltyCharges(penaltyCharges);
    }

    public LocalDate fetchInterestRecalculateFromDate() {
        LocalDate recalculatedOn;
        if (this.interestRecalculatedOn == null) {
            recalculatedOn = getDisbursementDate();
        } else {
            recalculatedOn = this.interestRecalculatedOn;
        }
        return recalculatedOn;
    }

    public void updateLoanOutstandingBalances() {
        Money outstanding = Money.zero(getCurrency());
        List<LoanTransaction> loanTransactions = retrieveListOfTransactionsExcludeAccruals();
        for (LoanTransaction loanTransaction : loanTransactions) {
            if (loanTransaction.isDisbursement() || loanTransaction.isIncomePosting()) {
                outstanding = outstanding.plus(loanTransaction.getAmount(getCurrency()))
                        .minus(loanTransaction.getOverPaymentPortion(getCurrency()));
                loanTransaction.updateOutstandingLoanBalance(MathUtil.negativeToZero(outstanding.getAmount()));
            } else if (loanTransaction.isChargeback() || loanTransaction.isCreditBalanceRefund()) {
                Money transactionOutstanding = loanTransaction.getPrincipalPortion(getCurrency());
                if (loanTransaction.isOverPaid()) {
                    // in case of advanced payment strategy and creditAllocations the full amount is recognized first
                    if (this.getCreditAllocationRules() != null && this.getCreditAllocationRules().size() > 0) {
                        Money payedPrincipal = loanTransaction.getLoanTransactionToRepaymentScheduleMappings().stream() //
                                .map(mapping -> mapping.getPrincipalPortion(getCurrency())) //
                                .reduce(Money.zero(getCurrency()), Money::plus);
                        transactionOutstanding = loanTransaction.getPrincipalPortion(getCurrency()).minus(payedPrincipal);
                    } else {
                        // in case legacy payment strategy
                        transactionOutstanding = loanTransaction.getAmount(getCurrency())
                                .minus(loanTransaction.getOverPaymentPortion(getCurrency()));
                    }
                    if (transactionOutstanding.isLessThanZero()) {
                        transactionOutstanding = Money.zero(getCurrency());
                    }
                }
                outstanding = outstanding.plus(transactionOutstanding);
                loanTransaction.updateOutstandingLoanBalance(MathUtil.negativeToZero(outstanding.getAmount()));
            } else if (!loanTransaction.isAccrualActivity()) {
                if (this.loanInterestRecalculationDetails != null
                        && this.loanInterestRecalculationDetails.isCompoundingToBePostedAsTransaction()
                        && !loanTransaction.isRepaymentAtDisbursement()) {
                    outstanding = outstanding.minus(loanTransaction.getAmount(getCurrency()));
                } else {
                    outstanding = outstanding.minus(loanTransaction.getPrincipalPortion(getCurrency()));
                }
                loanTransaction.updateOutstandingLoanBalance(MathUtil.negativeToZero(outstanding.getAmount()));
            }
        }
    }

    public String transactionProcessingStrategy() {
        return this.transactionProcessingStrategyCode;
    }

    public boolean isNpa() {
        return this.isNpa;
    }

    public Integer getLoanRepaymentScheduleInstallmentsSize() {
        return this.repaymentScheduleInstallments.size();
    }

    public void addLoanRepaymentScheduleInstallment(final LoanRepaymentScheduleInstallment installment) {
        installment.updateLoan(this);
        this.repaymentScheduleInstallments.add(installment);
    }

    /**
     * @param date
     * @return a schedule installment is related to the provided date
     **/
    public LoanRepaymentScheduleInstallment getRelatedRepaymentScheduleInstallment(LocalDate date) {
        // TODO first installment should be fromInclusive
        return getRepaymentScheduleInstallment(e -> DateUtils.isDateInRangeFromExclusiveToInclusive(date, e.getFromDate(), e.getDueDate()));
    }

    public LoanRepaymentScheduleInstallment fetchRepaymentScheduleInstallment(final Integer installmentNumber) {
        return getRepaymentScheduleInstallment(e -> e.getInstallmentNumber().equals(installmentNumber));
    }

    /**
     * @param dueDate
     *            the due date of the installment
     * @return a schedule installment with similar due date to the one provided
     **/
    public LoanRepaymentScheduleInstallment fetchLoanRepaymentScheduleInstallmentByDueDate(LocalDate dueDate) {
        return getRepaymentScheduleInstallment(e -> DateUtils.isEqual(dueDate, e.getDueDate()));
    }

    /**
     * @param predicate
     *            filter of the installments
     * @return the first installment matching the filter
     **/
    public LoanRepaymentScheduleInstallment getRepaymentScheduleInstallment(
            @NotNull Predicate<LoanRepaymentScheduleInstallment> predicate) {
        return getRepaymentScheduleInstallments().stream().filter(predicate).findFirst().orElse(null);
    }

    /**
     * @param predicate
     *            filter of the installments
     * @return the installments matching the filter
     **/
    public List<LoanRepaymentScheduleInstallment> getRepaymentScheduleInstallments(
            @NotNull Predicate<LoanRepaymentScheduleInstallment> predicate) {
        return getRepaymentScheduleInstallments().stream().filter(predicate).toList();
    }

    /**
     * @return loan disbursement data
     **/
    public List<DisbursementData> getDisbursementData() {
        Iterator<LoanDisbursementDetails> iterator = this.getDisbursementDetails().iterator();
        List<DisbursementData> disbursementData = new ArrayList<>();

        while (iterator.hasNext()) {
            LoanDisbursementDetails loanDisbursementDetails = iterator.next();

            LocalDate expectedDisbursementDate = null;
            LocalDate actualDisbursementDate = null;

            if (loanDisbursementDetails.expectedDisbursementDate() != null) {
                expectedDisbursementDate = loanDisbursementDetails.expectedDisbursementDate();
            }

            if (loanDisbursementDetails.actualDisbursementDate() != null) {
                actualDisbursementDate = loanDisbursementDetails.actualDisbursementDate();
            }
            BigDecimal waivedChargeAmount = null;
            disbursementData.add(new DisbursementData(loanDisbursementDetails.getId(), expectedDisbursementDate, actualDisbursementDate,
                    loanDisbursementDetails.principal(), this.netDisbursalAmount, null, null, waivedChargeAmount));
        }

        return disbursementData;
    }

    /**
     * @return application terms of the Loan object
     **/
    @SuppressWarnings({ "unused" })
    public LoanApplicationTerms getLoanApplicationTerms(final ApplicationCurrency applicationCurrency,
            final CalendarInstance restCalendarInstance, CalendarInstance compoundingCalendarInstance, final Calendar loanCalendar,
            final FloatingRateDTO floatingRateDTO, final boolean isSkipRepaymentonmonthFirst, final Integer numberofdays,
            final HolidayDetailDTO holidayDetailDTO) {
        LoanProduct loanProduct = loanProduct();
        final MonetaryCurrency currency = this.loanRepaymentScheduleDetail.getCurrency();

        final Integer loanTermFrequency = getTermFrequency();
        final PeriodFrequencyType loanTermPeriodFrequencyType = this.loanRepaymentScheduleDetail.getInterestPeriodFrequencyType();
        NthDayType nthDayType = null;
        DayOfWeekType dayOfWeekType = null;
        if (loanCalendar != null) {
            nthDayType = CalendarUtils.getRepeatsOnNthDayOfMonth(loanCalendar.getRecurrence());
            CalendarWeekDaysType getRepeatsOnDay = CalendarUtils.getRepeatsOnDay(loanCalendar.getRecurrence());
            Integer getRepeatsOnDayValue = null;
            if (getRepeatsOnDay != null) {
                getRepeatsOnDayValue = getRepeatsOnDay.getValue();
            }
            if (getRepeatsOnDayValue != null) {
                dayOfWeekType = DayOfWeekType.fromInt(getRepeatsOnDayValue);
            }
        }

        final Integer numberOfRepayments = this.loanRepaymentScheduleDetail.getNumberOfRepayments();
        final Integer repaymentEvery = this.loanRepaymentScheduleDetail.getRepayEvery();
        final PeriodFrequencyType repaymentPeriodFrequencyType = this.loanRepaymentScheduleDetail.getRepaymentPeriodFrequencyType();

        final AmortizationMethod amortizationMethod = this.loanRepaymentScheduleDetail.getAmortizationMethod();

        final InterestMethod interestMethod = this.loanRepaymentScheduleDetail.getInterestMethod();
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = this.loanRepaymentScheduleDetail
                .getInterestCalculationPeriodMethod();

        final BigDecimal interestRatePerPeriod = this.loanRepaymentScheduleDetail.getNominalInterestRatePerPeriod();
        final PeriodFrequencyType interestRatePeriodFrequencyType = this.loanRepaymentScheduleDetail.getInterestPeriodFrequencyType();

        BigDecimal annualNominalInterestRate = this.loanRepaymentScheduleDetail.getAnnualNominalInterestRate();
        final Money principalMoney = this.loanRepaymentScheduleDetail.getPrincipal();

        final LocalDate expectedDisbursementDate = getExpectedDisbursedOnLocalDate();
        final LocalDate repaymentsStartingFromDate = getExpectedFirstRepaymentOnDate();
        LocalDate calculatedRepaymentsStartingFromDate = repaymentsStartingFromDate;

        // TODO get calender linked to loan if any exist. As of 17-07-2014,
        // could not find a link in the database.
        // skip for now and set the Calender object to null
        // Calendar loanCalendar = null;
        // The calendar instance might be null if the loan is not connected
        // To a calendar object
        // if (loanCalendarInstance != null) {
        // loanCalendar = loanCalendarInstance.getCalendar();
        // }

        final Integer graceOnPrincipalPayment = this.loanRepaymentScheduleDetail.getGraceOnPrincipalPayment();
        final Integer graceOnInterestPayment = this.loanRepaymentScheduleDetail.getGraceOnInterestPayment();
        final Integer graceOnInterestCharged = this.loanRepaymentScheduleDetail.getGraceOnInterestCharged();
        final LocalDate interestChargedFromDate = getInterestChargedFromDate();
        final Integer graceOnArrearsAgeing = this.loanRepaymentScheduleDetail.getGraceOnArrearsAgeing();

        final Money inArrearsToleranceMoney = this.loanRepaymentScheduleDetail.getInArrearsTolerance();
        final BigDecimal emiAmount = getFixedEmiAmount();
        final BigDecimal maxOutstandingBalance = getMaxOutstandingLoanBalance();

        final List<DisbursementData> disbursementData = getDisbursementData();

        CalendarHistoryDataWrapper calendarHistoryDataWrapper = null;
        if (loanCalendar != null) {
            Set<CalendarHistory> calendarHistory = loanCalendar.getCalendarHistory();
            calendarHistoryDataWrapper = new CalendarHistoryDataWrapper(calendarHistory);
        }

        RecalculationFrequencyType recalculationFrequencyType = null;
        InterestRecalculationCompoundingMethod compoundingMethod = null;
        RecalculationFrequencyType compoundingFrequencyType = null;
        LoanRescheduleStrategyMethod rescheduleStrategyMethod = null;
        RepaymentStartDateType repaymentStartDateType = loanProduct.getRepaymentStartDateType();
        boolean allowCompoundingOnEod = false;
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            recalculationFrequencyType = this.loanInterestRecalculationDetails.getRestFrequencyType();
            compoundingMethod = this.loanInterestRecalculationDetails.getInterestRecalculationCompoundingMethod();
            compoundingFrequencyType = this.loanInterestRecalculationDetails.getCompoundingFrequencyType();
            rescheduleStrategyMethod = this.loanInterestRecalculationDetails.getRescheduleStrategyMethod();
            allowCompoundingOnEod = this.loanInterestRecalculationDetails.allowCompoundingOnEod();
        }

        List<LoanTermVariationsData> loanTermVariations = new ArrayList<>();
        annualNominalInterestRate = constructFloatingInterestRates(annualNominalInterestRate, floatingRateDTO, loanTermVariations);

        return LoanApplicationTerms.assembleFrom(applicationCurrency.toData(), loanTermFrequency, loanTermPeriodFrequencyType, nthDayType,
                dayOfWeekType, expectedDisbursementDate, repaymentsStartingFromDate, calculatedRepaymentsStartingFromDate,
                inArrearsToleranceMoney, this.loanRepaymentScheduleDetail, loanProduct.isMultiDisburseLoan(), emiAmount, disbursementData,
                maxOutstandingBalance, interestChargedFromDate, this.loanProduct.getPrincipalThresholdForLastInstallment(),
                this.loanProduct.getInstallmentAmountInMultiplesOf(), recalculationFrequencyType, restCalendarInstance, compoundingMethod,
                compoundingCalendarInstance, compoundingFrequencyType, this.loanProduct.preCloseInterestCalculationStrategy(),
                rescheduleStrategyMethod, loanCalendar, getApprovedPrincipal(), annualNominalInterestRate, loanTermVariations,
                calendarHistoryDataWrapper, numberofdays, isSkipRepaymentonmonthFirst, holidayDetailDTO, allowCompoundingOnEod, false,
                false, this.fixedPrincipalPercentagePerInstallment, false, repaymentStartDateType, getSubmittedOnDate());
    }

    public void updateRescheduledByUser(AppUser rescheduledByUser) {
        this.rescheduledByUser = rescheduledByUser;
    }

    public LoanProductRelatedDetail getLoanProductRelatedDetail() {
        return this.loanRepaymentScheduleDetail;
    }

    public void updateRescheduledOnDate(LocalDate rescheduledOnDate) {

        if (rescheduledOnDate != null) {
            this.rescheduledOnDate = rescheduledOnDate;
        }
    }

    public boolean isFeeCompoundingEnabledForInterestRecalculation() {
        boolean isEnabled = false;
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            isEnabled = this.loanInterestRecalculationDetails.getInterestRecalculationCompoundingMethod().isFeeCompoundingEnabled();
        }
        return isEnabled;
    }

    public Boolean shouldCreateStandingInstructionAtDisbursement() {
        return this.createStandingInstructionAtDisbursement != null && this.createStandingInstructionAtDisbursement;
    }

    public List<LoanCharge> getLoanChargesByDueDate(LocalDate dueDate) {
        return getLoanCharges(
                loanCharge -> loanCharge.getDueLocalDate() != null && DateUtils.isEqual(loanCharge.getDueLocalDate(), dueDate));
    }

    /**
     * @param predicate
     *            filter of the charges
     * @return the loan charges matching the filter
     **/
    public List<LoanCharge> getLoanCharges(@NotNull Predicate<LoanCharge> predicate) {
        return getLoanCharges().stream().filter(predicate).toList();
    }

    public void setGuaranteeAmount(BigDecimal guaranteeAmountDerived) {
        this.guaranteeAmountDerived = guaranteeAmountDerived;
    }

    public void updateGuaranteeAmount(BigDecimal guaranteeAmount) {
        this.guaranteeAmountDerived = getGuaranteeAmount().add(guaranteeAmount);
    }

    public BigDecimal getGuaranteeAmount() {
        return this.guaranteeAmountDerived == null ? BigDecimal.ZERO : this.guaranteeAmountDerived;
    }

    public LocalDate possibleNextRefundDate() {
        final LocalDate now = DateUtils.getBusinessLocalDate();

        LocalDate lastTransactionDate = null;
        for (final LoanTransaction transaction : this.loanTransactions) {
            if ((transaction.isRepaymentLikeType() || transaction.isRefundForActiveLoan() || transaction.isCreditBalanceRefund())
                    && transaction.isGreaterThanZero() && transaction.isNotReversed()) {
                lastTransactionDate = transaction.getTransactionDate();
            }
        }

        return lastTransactionDate == null ? now : lastTransactionDate;
    }

    public LocalDate getActualDisbursementDate(final LoanCharge loanCharge) {
        LocalDate actualDisbursementDate = this.actualDisbursementDate;
        if (loanCharge.isDueAtDisbursement() && loanCharge.isActive()) {
            LoanTrancheDisbursementCharge trancheDisbursementCharge = loanCharge.getTrancheDisbursementCharge();
            if (trancheDisbursementCharge != null) {
                LoanDisbursementDetails details = trancheDisbursementCharge.getloanDisbursementDetails();
                actualDisbursementDate = details.actualDisbursementDate();
            }
        }
        return actualDisbursementDate;
    }

    public void addTrancheLoanCharge(final Charge charge) {
        final List<Charge> appliedCharges = new ArrayList<>();
        for (final LoanTrancheCharge loanTrancheCharge : this.trancheCharges) {
            appliedCharges.add(loanTrancheCharge.getCharge());
        }
        if (!appliedCharges.contains(charge)) {
            this.trancheCharges.add(new LoanTrancheCharge(charge, this));
        }
    }

    public void updateLoanToLastDisbursalState(LoanDisbursementDetails disbursementDetail) {
        for (final LoanCharge charge : getActiveCharges()) {
            if (charge.isOverdueInstallmentCharge()) {
                charge.setActive(false);
            } else if (charge.isTrancheDisbursementCharge() && disbursementDetail.getDisbursementDate()
                    .equals(charge.getTrancheDisbursementCharge().getloanDisbursementDetails().actualDisbursementDate())) {
                charge.resetToOriginal(getCurrency());
            }
        }
        this.loanRepaymentScheduleDetail.setPrincipal(getDisbursedAmount().subtract(disbursementDetail.principal()));
        disbursementDetail.updateActualDisbursementDate(null);
        disbursementDetail.reverse();
        updateLoanSummaryDerivedFields();
    }

    private int adjustNumberOfRepayments() {
        int repaymetsForAdjust = 0;
        for (LoanTermVariations loanTermVariations : this.loanTermVariations) {
            if (loanTermVariations.getTermType().isInsertInstallment()) {
                repaymetsForAdjust++;
            } else if (loanTermVariations.getTermType().isDeleteInstallment()) {
                repaymetsForAdjust--;
            }
        }
        return repaymetsForAdjust;
    }

    public int fetchNumberOfInstallmensAfterExceptions() {
        if (!this.repaymentScheduleInstallments.isEmpty()) {
            List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
            int numberOfInstallments = 0;
            for (final LoanRepaymentScheduleInstallment installment : installments) {
                if (!installment.isRecalculatedInterestComponent()) {
                    numberOfInstallments++;
                }
            }
            return numberOfInstallments;
        }
        return this.repaymentScheduleDetail().getNumberOfRepayments() + adjustNumberOfRepayments();
    }

    /*
     * get the next repayment LocalDate for rescheduling at the time of disbursement
     */
    public LocalDate getNextPossibleRepaymentDateForRescheduling() {
        List<LoanDisbursementDetails> loanDisbursementDetails = getDisbursementDetails();
        LocalDate nextRepaymentDate = DateUtils.getBusinessLocalDate();
        for (LoanDisbursementDetails loanDisbursementDetail : loanDisbursementDetails) {
            if (loanDisbursementDetail.actualDisbursementDate() == null) {
                List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
                for (final LoanRepaymentScheduleInstallment installment : installments) {
                    if (!DateUtils.isBefore(installment.getDueDate(), loanDisbursementDetail.expectedDisbursementDateAsLocalDate())
                            && installment.isNotFullyPaidOff()) {
                        nextRepaymentDate = installment.getDueDate();
                        break;
                    }
                }
                break;
            }
        }
        return nextRepaymentDate;
    }

    public BigDecimal getDerivedAmountForCharge(final LoanCharge loanCharge) {
        BigDecimal amount = BigDecimal.ZERO;
        if (isMultiDisburmentLoan() && loanCharge.getCharge().getChargeTimeType().equals(ChargeTimeType.DISBURSEMENT.getValue())) {
            amount = getApprovedPrincipal();
        } else {
            // If charge type is specified due date and loan is multi disburment loan.
            // Then we need to get as of this loan charge due date how much amount disbursed.
            if (loanCharge.isSpecifiedDueDate() && this.isMultiDisburmentLoan()) {
                for (final LoanDisbursementDetails loanDisbursementDetails : this.getDisbursementDetails()) {
                    if (!DateUtils.isAfter(loanDisbursementDetails.expectedDisbursementDate(), loanCharge.getDueDate())) {
                        amount = amount.add(loanDisbursementDetails.principal());
                    }
                }
            } else {
                amount = getPrincipal().getAmount();
            }
        }
        return amount;
    }

    public void updateWriteOffReason(CodeValue writeOffReason) {
        this.writeOffReason = writeOffReason;
    }

    public LoanRepaymentScheduleInstallment fetchLoanForeclosureDetail(final LocalDate closureDate) {
        Money[] receivables = retriveIncomeOutstandingTillDate(closureDate);
        Money totalPrincipal = Money.of(getCurrency(), this.getSummary().getTotalPrincipalOutstanding());
        totalPrincipal = totalPrincipal.minus(receivables[3]);
        final Set<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails = null;
        final LocalDate currentDate = DateUtils.getBusinessLocalDate();
        return new LoanRepaymentScheduleInstallment(null, 0, currentDate, currentDate, totalPrincipal.getAmount(),
                receivables[0].getAmount(), receivables[1].getAmount(), receivables[2].getAmount(), false, compoundingDetails);
    }

    public Money[] retriveIncomeOutstandingTillDate(final LocalDate paymentDate) {
        Money[] balances = new Money[4];
        final MonetaryCurrency currency = getCurrency();
        Money interest = Money.zero(currency);
        Money paidFromFutureInstallments = Money.zero(currency);
        Money fee = Money.zero(currency);
        Money penalty = Money.zero(currency);
        int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper
                .fetchFirstNormalInstallmentNumber(repaymentScheduleInstallments);

        for (final LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
            boolean isFirstNormalInstallment = installment.getInstallmentNumber().equals(firstNormalInstallmentNumber);
            if (!DateUtils.isBefore(paymentDate, installment.getDueDate())) {
                interest = interest.plus(installment.getInterestOutstanding(currency));
                penalty = penalty.plus(installment.getPenaltyChargesOutstanding(currency));
                fee = fee.plus(installment.getFeeChargesOutstanding(currency));
            } else if (DateUtils.isAfter(paymentDate, installment.getFromDate())) {
                Money[] balancesForCurrentPeroid = fetchInterestFeeAndPenaltyTillDate(paymentDate, currency, installment,
                        isFirstNormalInstallment);
                if (balancesForCurrentPeroid[0].isGreaterThan(balancesForCurrentPeroid[5])) {
                    interest = interest.plus(balancesForCurrentPeroid[0]).minus(balancesForCurrentPeroid[5]);
                } else {
                    paidFromFutureInstallments = paidFromFutureInstallments.plus(balancesForCurrentPeroid[5])
                            .minus(balancesForCurrentPeroid[0]);
                }
                if (balancesForCurrentPeroid[1].isGreaterThan(balancesForCurrentPeroid[3])) {
                    fee = fee.plus(balancesForCurrentPeroid[1].minus(balancesForCurrentPeroid[3]));
                } else {
                    paidFromFutureInstallments = paidFromFutureInstallments
                            .plus(balancesForCurrentPeroid[3].minus(balancesForCurrentPeroid[1]));
                }
                if (balancesForCurrentPeroid[2].isGreaterThan(balancesForCurrentPeroid[4])) {
                    penalty = penalty.plus(balancesForCurrentPeroid[2].minus(balancesForCurrentPeroid[4]));
                } else {
                    paidFromFutureInstallments = paidFromFutureInstallments.plus(balancesForCurrentPeroid[4])
                            .minus(balancesForCurrentPeroid[2]);
                }
            } else {
                paidFromFutureInstallments = paidFromFutureInstallments.plus(installment.getInterestPaid(currency))
                        .plus(installment.getPenaltyChargesPaid(currency)).plus(installment.getFeeChargesPaid(currency));
            }

        }
        balances[0] = interest;
        balances[1] = fee;
        balances[2] = penalty;
        balances[3] = paidFromFutureInstallments;
        return balances;
    }

    private Money[] fetchInterestFeeAndPenaltyTillDate(final LocalDate paymentDate, final MonetaryCurrency currency,
            final LoanRepaymentScheduleInstallment installment, boolean isFirstNormalInstallment) {
        Money penaltyForCurrentPeriod = Money.zero(getCurrency());
        Money penaltyAccoutedForCurrentPeriod = Money.zero(getCurrency());
        Money feeForCurrentPeriod = Money.zero(getCurrency());
        Money feeAccountedForCurrentPeriod = Money.zero(getCurrency());
        Money interestForCurrentPeriod = Money.zero(getCurrency());
        Money interestAccountedForCurrentPeriod = Money.zero(getCurrency());
        int totalPeriodDays = DateUtils.getExactDifferenceInDays(installment.getFromDate(), installment.getDueDate());
        int tillDays = DateUtils.getExactDifferenceInDays(installment.getFromDate(), paymentDate);
        interestForCurrentPeriod = Money.of(getCurrency(), BigDecimal
                .valueOf(calculateInterestForDays(totalPeriodDays, installment.getInterestCharged(getCurrency()).getAmount(), tillDays)));
        interestAccountedForCurrentPeriod = installment.getInterestWaived(getCurrency()).plus(installment.getInterestPaid(getCurrency()));
        for (LoanCharge loanCharge : this.charges) {
            if (loanCharge.isActive() && !loanCharge.isDueAtDisbursement()) {
                boolean isDue = loanCharge.isDueInPeriod(installment.getFromDate(), paymentDate, isFirstNormalInstallment);
                if (isDue) {
                    if (loanCharge.isPenaltyCharge()) {
                        penaltyForCurrentPeriod = penaltyForCurrentPeriod.plus(loanCharge.getAmount(getCurrency()));
                        penaltyAccoutedForCurrentPeriod = penaltyAccoutedForCurrentPeriod
                                .plus(loanCharge.getAmountWaived(getCurrency()).plus(loanCharge.getAmountPaid(getCurrency())));
                    } else {
                        feeForCurrentPeriod = feeForCurrentPeriod.plus(loanCharge.getAmount(currency));
                        feeAccountedForCurrentPeriod = feeAccountedForCurrentPeriod.plus(loanCharge.getAmountWaived(getCurrency()).plus(

                                loanCharge.getAmountPaid(getCurrency())));
                    }
                } else if (loanCharge.isInstalmentFee()) {
                    LoanInstallmentCharge loanInstallmentCharge = loanCharge.getInstallmentLoanCharge(installment.getInstallmentNumber());
                    if (loanCharge.isPenaltyCharge()) {
                        penaltyAccoutedForCurrentPeriod = penaltyAccoutedForCurrentPeriod
                                .plus(loanInstallmentCharge.getAmountPaid(currency));
                    } else {
                        feeAccountedForCurrentPeriod = feeAccountedForCurrentPeriod.plus(loanInstallmentCharge.getAmountPaid(currency));
                    }
                }
            }
        }

        Money[] balances = new Money[6];
        balances[0] = interestForCurrentPeriod;
        balances[1] = feeForCurrentPeriod;
        balances[2] = penaltyForCurrentPeriod;
        balances[3] = feeAccountedForCurrentPeriod;
        balances[4] = penaltyAccoutedForCurrentPeriod;
        balances[5] = interestAccountedForCurrentPeriod;
        return balances;
    }

    public Money[] retriveIncomeForOverlappingPeriod(final LocalDate paymentDate) {
        Money[] balances = new Money[3];
        final MonetaryCurrency currency = getCurrency();
        balances[0] = balances[1] = balances[2] = Money.zero(currency);
        int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper
                .fetchFirstNormalInstallmentNumber(repaymentScheduleInstallments);
        for (final LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
            boolean isFirstNormalInstallment = installment.getInstallmentNumber().equals(firstNormalInstallmentNumber);
            if (DateUtils.isEqual(paymentDate, installment.getDueDate())) {
                Money interest = installment.getInterestCharged(currency);
                Money fee = installment.getFeeChargesCharged(currency);
                Money penalty = installment.getPenaltyChargesCharged(currency);
                balances[0] = interest;
                balances[1] = fee;
                balances[2] = penalty;
                break;
            } else if (DateUtils.isDateInRangeExclusive(paymentDate, installment.getFromDate(), installment.getDueDate())) {
                balances = fetchInterestFeeAndPenaltyTillDate(paymentDate, currency, installment, isFirstNormalInstallment);
                break;
            }
        }

        return balances;
    }

    private double calculateInterestForDays(int daysInPeriod, BigDecimal interest, int days) {
        if (interest.doubleValue() == 0) {
            return 0;
        }
        return interest.doubleValue() / daysInPeriod * days;
    }

    public void updateLoanScheduleOnForeclosure(final Collection<LoanRepaymentScheduleInstallment> installments) {
        this.repaymentScheduleInstallments.clear();
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            addLoanRepaymentScheduleInstallment(installment);
        }
    }

    public boolean isForeclosure() {
        boolean isForeClosure = false;
        if (this.loanSubStatus != null) {
            isForeClosure = LoanSubStatus.fromInt(loanSubStatus).isForeclosed();
        }

        return isForeClosure;
    }

    public List<LoanTermVariations> getActiveLoanTermVariations() {
        if (this.loanTermVariations == null) {
            return new ArrayList<>();
        }

        return this.loanTermVariations.stream().filter(LoanTermVariations::isActive).collect(Collectors.toList());
    }

    public boolean isTopup() {
        return this.isTopup;
    }

    public void markAsFraud(final boolean value) {
        this.fraud = value;
    }

    public BigDecimal getFirstDisbursalAmount() {
        BigDecimal firstDisbursalAmount;

        if (this.isMultiDisburmentLoan()) {
            List<DisbursementData> disbursementData = getDisbursementData();
            Collections.sort(disbursementData);
            firstDisbursalAmount = disbursementData.get(disbursementData.size() - 1).getPrincipal();
        } else {
            firstDisbursalAmount = this.getLoanRepaymentScheduleDetail().getPrincipal().getAmount();
        }
        return firstDisbursalAmount;
    }

    public void setTopupLoanDetails(LoanTopupDetails topupLoanDetails) {
        this.loanTopupDetails = topupLoanDetails;
    }

    public LoanTopupDetails getTopupLoanDetails() {
        return this.loanTopupDetails;
    }

    public Collection<LoanCharge> getLoanCharges() {
        return this.charges;
    }

    public void initializeLazyCollections() {
        checkAndFetchLazyCollection(this.charges);
        checkAndFetchLazyCollection(this.trancheCharges);
        checkAndFetchLazyCollection(this.repaymentScheduleInstallments);
        checkAndFetchLazyCollection(this.loanTransactions);
        checkAndFetchLazyCollection(this.disbursementDetails);
        checkAndFetchLazyCollection(this.loanTermVariations);
        checkAndFetchLazyCollection(this.collateral);
        checkAndFetchLazyCollection(this.loanOfficerHistory);
        checkAndFetchLazyCollection(this.loanCollateralManagements);
    }

    private void checkAndFetchLazyCollection(Collection lazyCollection) {
        if (lazyCollection != null) {
            lazyCollection.size(); // NOSONAR
        }
    }

    public void initializeLoanOfficerHistory() {
        this.loanOfficerHistory.size(); // NOSONAR
    }

    public void initializeTransactions() {
        this.loanTransactions.size(); // NOSONAR
    }

    public void initializeRepaymentSchedule() {
        this.repaymentScheduleInstallments.size(); // NOSONAR
    }

    public boolean hasInvalidLoanType() {
        return AccountType.fromInt(this.loanType).isInvalid();
    }

    public boolean isIndividualLoan() {
        return AccountType.fromInt(this.loanType).isIndividualAccount();
    }

    public AccountType getLoanType() {
        return AccountType.fromInt(loanType);
    }

    public void adjustNetDisbursalAmount(BigDecimal adjustedAmount) {
        this.netDisbursalAmount = adjustedAmount.subtract(this.deriveSumTotalOfChargesDueAtDisbursement());
    }

    /**
     * Get the charges.
     *
     * @return the charges
     */
    public Collection<LoanCharge> getCharges() {
        // At the time of loan creation, "this.charges" will be null if no charges found in the request.
        // In that case, fetch loan (before commit) will return null for the charges.
        // Return empty set instead of null to avoid NPE
        return Optional.ofNullable(this.charges).orElse(new HashSet<>());
    }

    public boolean hasDelinquencyBucket() {
        return (getLoanProduct().getDelinquencyBucket() != null);
    }

    public void markAsChargedOff(final LocalDate chargedOffOn, final AppUser chargedOffBy, final CodeValue chargeOffReason) {
        this.chargedOff = true;
        this.chargedOffBy = chargedOffBy;
        this.chargedOffOnDate = chargedOffOn;
        this.chargeOffReason = chargeOffReason;
    }

    public void liftChargeOff() {
        this.chargedOff = false;
        this.chargedOffBy = null;
        this.chargedOffOnDate = null;
        this.chargeOffReason = null;
    }

    public LoanRepaymentScheduleInstallment getLastLoanRepaymentScheduleInstallment() {
        return getRepaymentScheduleInstallments().get(getRepaymentScheduleInstallments().size() - 1);
    }

    public List<LoanTransaction> getLoanTransactions(Predicate<LoanTransaction> predicate) {
        return getLoanTransactions().stream().filter(predicate).toList();
    }

    public LoanTransaction getLoanTransaction(Predicate<LoanTransaction> predicate) {
        return getLoanTransactions().stream().filter(predicate).findFirst().orElse(null);
    }

    public LoanTransaction findChargedOffTransaction() {
        return getLoanTransaction(e -> e.isNotReversed() && e.isChargeOff());
    }

    public void handleMaturityDateActivate() {
        if (this.expectedMaturityDate != null && !this.expectedMaturityDate.equals(this.actualMaturityDate)) {
            this.actualMaturityDate = this.expectedMaturityDate;
        }
    }

    public List<LoanTransactionType> getSupportedInterestRefundTransactionTypes() {
        return getLoanProductRelatedDetail().getSupportedInterestRefundTypes().stream()
                .map(LoanSupportedInterestRefundTypes::getTransactionType).toList();
    }

    public LoanTransaction getLastUserTransaction() {
        return getLoanTransactions().stream() //
                .filter(t -> t.isNotReversed() && !(t.isAccrual() || t.isAccrualAdjustment() || t.isIncomePosting())) //
                .reduce((first, second) -> second) //
                .orElse(null);
    }

    public void updateEnableInstallmentLevelDelinquency(boolean enableInstallmentLevelDelinquency) {
        this.enableInstallmentLevelDelinquency = enableInstallmentLevelDelinquency;
    }

    public void deductFromNetDisbursalAmount(final BigDecimal subtrahend) {
        this.netDisbursalAmount = this.netDisbursalAmount.subtract(subtrahend);
    }

    public void setIsTopup(boolean topup) {
        isTopup = topup;
    }

    public LoanRepaymentScheduleTransactionProcessor getTransactionProcessor() {
        return transactionProcessorFactory.determineProcessor(transactionProcessingStrategyCode);
    }

    public boolean isProgressiveSchedule() {
        return getLoanProductRelatedDetail().getLoanScheduleType() == PROGRESSIVE;
    }
}
