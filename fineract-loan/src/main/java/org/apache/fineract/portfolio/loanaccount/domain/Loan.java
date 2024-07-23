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

import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
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
import java.time.temporal.ChronoUnit;
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
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.configuration.service.TemporaryConfigurationServiceContainer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
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
import org.apache.fineract.organisation.workingdays.service.WorkingDaysUtil;
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
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeAddedException;
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
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidRefundDateException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanApplicationDateException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanChargeRefundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanDisbursalException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanForeclosureException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanOfficerAssignmentDateException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanOfficerAssignmentException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanOfficerUnassignmentDateException;
import org.apache.fineract.portfolio.loanaccount.exception.MultiDisbursementDataNotAllowedException;
import org.apache.fineract.portfolio.loanaccount.exception.MultiDisbursementDataRequiredException;
import org.apache.fineract.portfolio.loanaccount.exception.UndoLastTrancheDisbursementException;
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
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.rate.domain.Rate;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecks;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Table(name = "m_loan", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_no" }, name = "loan_account_no_UNIQUE"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "loan_externalid_UNIQUE") })
@Setter
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

    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "external_id")
    private ExternalId externalId;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

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

    @Column(name = "term_frequency", nullable = false)
    private Integer termFrequency;

    @Column(name = "term_period_frequency_enum", nullable = false)
    private Integer termPeriodFrequencyType;

    @Column(name = "loan_status_id", nullable = false)
    private Integer loanStatus;

    @Column(name = "sync_disbursement_with_meeting")
    private Boolean syncDisbursementWithMeeting;

    // loan application states
    @Column(name = "submittedon_date")
    private LocalDate submittedOnDate;
    @Column(name = "rejectedon_date")
    private LocalDate rejectedOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "rejectedon_userid")
    private AppUser rejectedBy;

    @Column(name = "withdrawnon_date")
    private LocalDate withdrawnOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "withdrawnon_userid")
    private AppUser withdrawnBy;

    @Column(name = "approvedon_date")
    private LocalDate approvedOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "approvedon_userid")
    private AppUser approvedBy;

    @Column(name = "expected_disbursedon_date")
    private LocalDate expectedDisbursementDate;

    @Column(name = "disbursedon_date")
    private LocalDate actualDisbursementDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursedon_userid")
    private AppUser disbursedBy;

    @Column(name = "closedon_date")
    private LocalDate closedOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "closedon_userid")
    private AppUser closedBy;

    @Column(name = "writtenoffon_date")
    private LocalDate writtenOffOnDate;

    @Column(name = "rescheduledon_date")
    private LocalDate rescheduledOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "rescheduledon_userid")
    private AppUser rescheduledByUser;

    @Column(name = "expected_maturedon_date")
    private LocalDate expectedMaturityDate;

    @Column(name = "maturedon_date")
    private LocalDate actualMaturityDate;

    @Column(name = "expected_firstrepaymenton_date")
    private LocalDate expectedFirstRepaymentOnDate;

    @Column(name = "interest_calculated_from_date")
    private LocalDate interestChargedFromDate;

    @Column(name = "total_overpaid_derived", scale = 6, precision = 19)
    private BigDecimal totalOverpaid;

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
    @Transient
    private LoanSummaryWrapper loanSummaryWrapper;

    @Column(name = "principal_amount_proposed", scale = 6, precision = 19, nullable = false)
    private BigDecimal proposedPrincipal;

    @Column(name = "approved_principal", scale = 6, precision = 19, nullable = false)
    private BigDecimal approvedPrincipal;

    @Column(name = "net_disbursal_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal netDisbursalAmount;

    @Column(name = "fixed_emi_amount", scale = 6, precision = 19)
    private BigDecimal fixedEmiAmount;

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

    @Column(name = "accrued_till")
    private LocalDate accruedTill;

    @Column(name = "create_standing_instruction_at_disbursement")
    private Boolean createStandingInstructionAtDisbursement;

    @Column(name = "guarantee_amount_derived", scale = 6, precision = 19)
    private BigDecimal guaranteeAmountDerived;

    @Column(name = "interest_recalcualated_on")
    private LocalDate interestRecalculatedOn;

    @Column(name = "is_floating_interest_rate")
    private Boolean isFloatingInterestRate;

    @Column(name = "interest_rate_differential", scale = 6, precision = 19)
    private BigDecimal interestRateDifferential;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writeoff_reason_cv_id")
    private CodeValue writeOffReason;

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

    @Column(name = "fixed_principal_percentage_per_installment", scale = 2, precision = 5)
    private BigDecimal fixedPrincipalPercentagePerInstallment;

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
        if (isChargesAdditionAllowed() && loanCharge.isDueAtDisbursement()) {
            // Note: added this constraint to restrict adding disbursement
            // charges to a loan
            // after it is disbursed
            // if the loan charge payment type is 'Disbursement'.
            // To undo this constraint would mean resolving how charges due are
            // disbursement are handled at present.
            // When a loan is disbursed and has charges due at disbursement, a
            // transaction is created to auto record
            // payment of the charges (user has no choice in saying they were or
            // werent paid) - so its assumed they were paid.

            final String defaultUserMessage = "This charge which is due at disbursement cannot be added as the loan is already disbursed.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "due.at.disbursement.and.loan.is.disbursed", defaultUserMessage,
                    getId(), loanCharge.name());
        }

        validateChargeHasValidSpecifiedDateIfApplicable(loanCharge, getDisbursementDate());

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

        // store Id's of existing loan transactions and existing reversed loan
        // transactions
        final SingleLoanChargeRepaymentScheduleProcessingWrapper wrapper = new SingleLoanChargeRepaymentScheduleProcessingWrapper();
        wrapper.reprocess(getCurrency(), getDisbursementDate(), getRepaymentScheduleInstallments(), loanCharge);
        updateLoanSummaryDerivedFields();

        loanLifecycleStateMachine.transition(LoanEvent.LOAN_CHARGE_ADDED, this);
    }

    public ChangedTransactionDetail reprocessTransactions() {
        ChangedTransactionDetail changedTransactionDetail;
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsForReprocessing();
        changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(getDisbursementDate(),
                allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(), getActiveCharges());
        for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {

            mapEntry.getValue().updateLoan(this);
        }
        this.loanTransactions.addAll(changedTransactionDetail.getNewTransactionMappings().values());
        updateLoanSummaryDerivedFields();
        return changedTransactionDetail;
    }

    /**
     * Creates a loanTransaction for "Apply Charge Event" with transaction date set to "suppliedTransactionDate". The
     * newly created transaction is also added to the Loan on which this method is called.
     *
     * If "suppliedTransactionDate" is not passed Id, the transaction date is set to the loans due date if the due date
     * is lesser than todays date. If not, the transaction date is set to todays date
     */
    public LoanTransaction handleChargeAppliedTransaction(final LoanCharge loanCharge, final LocalDate suppliedTransactionDate) {
        final Money chargeAmount = loanCharge.getAmount(getCurrency());
        Money feeCharges = chargeAmount;
        Money penaltyCharges = Money.zero(loanCurrency());
        if (loanCharge.isPenaltyCharge()) {
            penaltyCharges = chargeAmount;
            feeCharges = Money.zero(loanCurrency());
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
        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(applyLoanChargeTransaction, loanCharge,
                loanCharge.getAmount(getCurrency()).getAmount(), installmentNumber);
        applyLoanChargeTransaction.getLoanChargesPaid().add(loanChargePaidBy);
        addLoanTransaction(applyLoanChargeTransaction);
        return applyLoanChargeTransaction;
    }

    private void handleChargePaidTransaction(final LoanCharge charge, final LoanTransaction chargesPayment,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final Integer installmentNumber) {
        chargesPayment.updateLoan(this);
        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(chargesPayment, charge,
                chargesPayment.getAmount(getCurrency()).getAmount(), installmentNumber);
        chargesPayment.getLoanChargesPaid().add(loanChargePaidBy);
        addLoanTransaction(chargesPayment);
        loanLifecycleStateMachine.transition(LoanEvent.LOAN_CHARGE_PAYMENT, this);

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
        final List<LoanRepaymentScheduleInstallment> chargePaymentInstallments = new ArrayList<>();
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper
                .fetchFirstNormalInstallmentNumber(repaymentScheduleInstallments);
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            boolean isFirstNormalInstallment = installment.getInstallmentNumber().equals(firstNormalInstallmentNumber)
                    ? charge.isDueForCollectionFromIncludingAndUpToAndIncluding(installment.getFromDate(), installment.getDueDate())
                    : charge.isDueForCollectionFromAndUpToAndIncluding(installment.getFromDate(), installment.getDueDate());
            if (installmentNumber == null && isFirstNormalInstallment) {
                chargePaymentInstallments.add(installment);
                break;
            } else if (installment.getInstallmentNumber().equals(installmentNumber)) {
                chargePaymentInstallments.add(installment);
                break;
            }
        }
        final Set<LoanCharge> loanCharges = new HashSet<>(1);
        loanCharges.add(charge);
        loanRepaymentScheduleTransactionProcessor.processLatestTransaction(chargesPayment,
                new TransactionCtx(getCurrency(), chargePaymentInstallments, loanCharges, new MoneyHolder(getTotalOverpaidAsMoney())));

        updateLoanSummaryDerivedFields();
        doPostLoanTransactionChecks(chargesPayment.getTransactionDate(), loanLifecycleStateMachine);
    }

    private void validateLoanIsNotClosed(final LoanCharge loanCharge) {
        if (isClosed()) {
            final String defaultUserMessage = "This charge cannot be added as the loan is already closed.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "loan.is.closed", defaultUserMessage, getId(), loanCharge.name());

        }
    }

    private void validateLoanChargeIsNotWaived(final LoanCharge loanCharge) {
        if (loanCharge.isWaived()) {
            final String defaultUserMessage = "This loan charge cannot be removed as the charge as already been waived.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "loanCharge.is.waived", defaultUserMessage, getId(),
                    loanCharge.name());

        }
    }

    private void validateChargeHasValidSpecifiedDateIfApplicable(final LoanCharge loanCharge, final LocalDate disbursementDate) {
        if (loanCharge.isSpecifiedDueDate() && DateUtils.isBefore(loanCharge.getDueLocalDate(), disbursementDate)) {
            final String defaultUserMessage = "This charge with specified due date cannot be added as the it is not in schedule range.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "specified.due.date.outside.range", defaultUserMessage,
                    getDisbursementDate(), loanCharge.name());
        }
    }

    private LocalDate getLastRepaymentPeriodDueDate(final boolean includeRecalculatedInterestComponent) {
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
        validateLoanIsNotClosed(loanCharge);

        // NOTE: to remove this constraint requires that loan transactions
        // that represent the waive of charges also be removed (or reversed)M
        // if you want ability to remove loan charges that are waived.
        validateLoanChargeIsNotWaived(loanCharge);

        final boolean removed = loanCharge.isActive();
        if (removed) {
            loanCharge.setActive(false);
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(getCurrency(), getDisbursementDate(), getRepaymentScheduleInstallments(), getActiveCharges());
            updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
        }

        removeOrModifyTransactionAssociatedWithLoanChargeIfDueAtDisbursement(loanCharge);

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
        if (!loanCharge.isDueAtDisbursement() && loanCharge.isPaidOrPartiallyPaid(loanCurrency())) {
            /*
             * TODO Vishwas Currently we do not allow removing a loan charge after a loan is approved (hence there is no
             * need to adjust any loan transactions).
             *
             * Consider removing this block of code or logically completing it for the future by getting the list of
             * affected Transactions
             */
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsForReprocessing();
            loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(getDisbursementDate(),
                    allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(), getActiveCharges());
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
                    final MonetaryCurrency currency = loanCurrency();
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

    private boolean doesLoanChargePaidByContainLoanCharge(Set<LoanChargePaidBy> loanChargePaidBys, LoanCharge loanCharge) {
        return loanChargePaidBys.stream() //
                .anyMatch(loanChargePaidBy -> loanChargePaidBy.getLoanCharge().equals(loanCharge));
    }

    public Map<String, Object> updateLoanCharge(final LoanCharge loanCharge, final JsonCommand command) {
        validateLoanIsNotClosed(loanCharge);

        final Map<String, Object> actualChanges = new LinkedHashMap<>(3);

        if (getActiveCharges().contains(loanCharge)) {
            final BigDecimal amount = calculateAmountPercentageAppliedTo(loanCharge);
            final Map<String, Object> loanChargeChanges = loanCharge.update(command, amount);
            actualChanges.putAll(loanChargeChanges);
            updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
        }

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
        if (!loanCharge.isDueAtDisbursement()) {
            /*
             * TODO Vishwas Currently we do not allow waiving updating loan charge after a loan is approved (hence there
             * is no need to adjust any loan transactions).
             *
             * Consider removing this block of code or logically completing it for the future by getting the list of
             * affected Transactions
             */
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsForReprocessing();
            loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(getDisbursementDate(),
                    allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(), getActiveCharges());
        } else {
            // reprocess loan schedule based on charge been waived.
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(getCurrency(), getDisbursementDate(), getRepaymentScheduleInstallments(), getActiveCharges());
        }

        updateLoanSummaryDerivedFields();

        return actualChanges;
    }

    private BigDecimal calculateAmountPercentageAppliedTo(final LoanCharge loanCharge) {
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
        return this.loanSummaryWrapper.calculateTotalInterestCharged(getRepaymentScheduleInstallments(), getCurrency()).getAmount();
    }

    private BigDecimal calculatePerInstallmentChargeAmount(final LoanCharge loanCharge) {
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

    public LoanTransaction waiveLoanCharge(final LoanCharge loanCharge, final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final Map<String, Object> changes, final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds,
            final Integer loanInstallmentNumber, final ScheduleGeneratorDTO scheduleGeneratorDTO, final Money accruedCharge,
            final ExternalId externalId) {
        validateLoanIsNotClosed(loanCharge);

        final Money amountWaived = loanCharge.waive(loanCurrency(), loanInstallmentNumber);
        changes.put("amount", amountWaived.getAmount());

        Money unrecognizedIncome = amountWaived.zero();
        Money chargeComponent = amountWaived;
        if (isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            Money receivableCharge;
            if (loanInstallmentNumber != null) {
                receivableCharge = accruedCharge
                        .minus(loanCharge.getInstallmentLoanCharge(loanInstallmentNumber).getAmountPaid(getCurrency()));
            } else {
                receivableCharge = accruedCharge.minus(loanCharge.getAmountPaid(getCurrency()));
            }
            if (receivableCharge.isLessThanZero()) {
                receivableCharge = amountWaived.zero();
            }
            if (amountWaived.isGreaterThan(receivableCharge)) {
                chargeComponent = receivableCharge;
                unrecognizedIncome = amountWaived.minus(receivableCharge);
            }
        }
        Money feeChargesWaived = chargeComponent;
        Money penaltyChargesWaived = Money.zero(loanCurrency());
        if (loanCharge.isPenaltyCharge()) {
            penaltyChargesWaived = chargeComponent;
            feeChargesWaived = Money.zero(loanCurrency());
        }

        LocalDate transactionDate = getDisbursementDate();
        LocalDate businessDate = DateUtils.getBusinessLocalDate();
        if (loanCharge.isDueDateCharge()) {
            if (DateUtils.isAfter(loanCharge.getDueLocalDate(), businessDate)) {
                transactionDate = businessDate;
            } else {
                transactionDate = loanCharge.getDueLocalDate();
            }
        } else if (loanCharge.isInstalmentFee()) {
            LocalDate repaymentDueDate = loanCharge.getInstallmentLoanCharge(loanInstallmentNumber).getRepaymentInstallment().getDueDate();
            if (DateUtils.isAfter(repaymentDueDate, businessDate)) {
                transactionDate = businessDate;
            } else {
                transactionDate = repaymentDueDate;
            }
        }

        scheduleGeneratorDTO.setRecalculateFrom(transactionDate);

        updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        final LoanTransaction waiveLoanChargeTransaction = LoanTransaction.waiveLoanCharge(this, getOffice(), amountWaived, transactionDate,
                feeChargesWaived, penaltyChargesWaived, unrecognizedIncome, externalId);
        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(waiveLoanChargeTransaction, loanCharge,
                waiveLoanChargeTransaction.getAmount(getCurrency()).getAmount(), loanInstallmentNumber);
        waiveLoanChargeTransaction.getLoanChargesPaid().add(loanChargePaidBy);
        addLoanTransaction(waiveLoanChargeTransaction);
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()
                && DateUtils.isBefore(loanCharge.getDueLocalDate(), businessDate)) {
            regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO);
        }
        // Waive of charges whose due date falls after latest 'repayment' transaction don't require entire loan schedule
        // to be reprocessed.
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
        if (!loanCharge.isDueAtDisbursement() && loanCharge.isPaidOrPartiallyPaid(loanCurrency())) {
            /*
             * TODO Vishwas Currently we do not allow waiving fully paid loan charge and waiving partially paid loan
             * charges only waives the remaining amount.
             *
             * Consider removing this block of code or logically completing it for the future by getting the list of
             * affected Transactions
             */
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsForReprocessing();
            loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(getDisbursementDate(),
                    allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(), getActiveCharges());
        } else {
            // reprocess loan schedule based on charge been waived.
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(getCurrency(), getDisbursementDate(), getRepaymentScheduleInstallments(), getActiveCharges());
        }

        updateLoanSummaryDerivedFields();

        doPostLoanTransactionChecks(waiveLoanChargeTransaction.getTransactionDate(), loanLifecycleStateMachine);

        return waiveLoanChargeTransaction;
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

    public void updateLoanOfficerOnLoanApplication(final Staff newLoanOfficer) {
        if (!isSubmittedAndPendingApproval()) {
            Long loanOfficerId = null;
            if (this.loanOfficer != null) {
                loanOfficerId = this.loanOfficer.getId();
            }
            throw new LoanOfficerAssignmentException(getId(), loanOfficerId);
        }
        this.loanOfficer = newLoanOfficer;
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
                LoanTrancheDisbursementCharge loanTrancheDisbursementCharge = null;
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
            this.summary.zeroFields();
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
            this.summary.updateSummary(loanCurrency(), principal, getRepaymentScheduleInstallments(), this.loanSummaryWrapper,
                    this.charges);
            updateLoanOutstandingBalances();
        }
    }

    public void updateLoanSummaryAndStatus() {
        updateLoanSummaryDerivedFields();
        doPostLoanTransactionChecks(getLastUserTransactionDate(), loanLifecycleStateMachine);
    }

    public void recalculateAllCharges() {
        Set<LoanCharge> charges = this.getActiveCharges();
        int penaltyWaitPeriod = 0;
        for (final LoanCharge loanCharge : charges) {
            recalculateLoanCharge(loanCharge, penaltyWaitPeriod);
        }
        updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
    }

    public boolean isInterestRecalculationEnabledForProduct() {
        return this.loanProduct.isInterestRecalculationEnabled();
    }

    public boolean isMultiDisburmentLoan() {
        return this.loanProduct.isMultiDisburseLoan();
    }

    /**
     * Update interest recalculation settings if product configuration changes
     */
    private void updateOverdueScheduleInstallment(final LoanCharge loanCharge) {
        if (loanCharge.isOverdueInstallmentCharge() && loanCharge.isActive()) {
            LoanOverdueInstallmentCharge overdueInstallmentCharge = loanCharge.getOverdueInstallmentCharge();
            if (overdueInstallmentCharge != null) {
                Integer installmentNumber = overdueInstallmentCharge.getInstallment().getInstallmentNumber();
                LoanRepaymentScheduleInstallment installment = fetchRepaymentScheduleInstallment(installmentNumber);
                overdueInstallmentCharge.updateLoanRepaymentScheduleInstallment(installment);
            }
        }
    }

    private void recalculateLoanCharge(final LoanCharge loanCharge, final int penaltyWaitPeriod) {
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal chargeAmt;
        BigDecimal totalChargeAmt = BigDecimal.ZERO;
        if (loanCharge.getChargeCalculation().isPercentageBased()) {
            if (loanCharge.isOverdueInstallmentCharge()) {
                amount = calculateOverdueAmountPercentageAppliedTo(loanCharge, penaltyWaitPeriod);
            } else {
                amount = calculateAmountPercentageAppliedTo(loanCharge);
            }
            chargeAmt = loanCharge.getPercentage();
            if (loanCharge.isInstalmentFee()) {
                totalChargeAmt = calculatePerInstallmentChargeAmount(loanCharge);
            }
        } else {
            chargeAmt = loanCharge.amountOrPercentage();
        }
        if (loanCharge.isActive()) {
            clearLoanInstallmentChargesBeforeRegeneration(loanCharge);
            loanCharge.update(chargeAmt, loanCharge.getDueLocalDate(), amount, fetchNumberOfInstallmensAfterExceptions(), totalChargeAmt);
            validateChargeHasValidSpecifiedDateIfApplicable(loanCharge, getDisbursementDate());
        }

    }

    private void clearLoanInstallmentChargesBeforeRegeneration(final LoanCharge loanCharge) {
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

    private BigDecimal calculateOverdueAmountPercentageAppliedTo(final LoanCharge loanCharge, final int penaltyWaitPeriod) {
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
    private Map<String, String> getDateFormatAndLocale(final JsonCommand jsonCommand) {
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

    private Map<String, Object> parseDisbursementDetails(final JsonObject jsonObject, String dateFormat, Locale locale) {
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

    public void updateDisbursementDetails(final JsonCommand jsonCommand, final Map<String, Object> actualChanges) {
        List<Long> disbursementList = fetchDisbursementIds();
        List<Long> loanChargeIds = fetchLoanTrancheChargeIds();
        int chargeIdLength = loanChargeIds.size();
        String chargeIds;
        // From modify application page, if user removes all charges, we should
        // get empty array.
        // So we need to remove all charges applied for this loan
        boolean removeAllCharges = jsonCommand.parameterExists(LoanApiConstants.chargesParameterName)
                && jsonCommand.arrayOfParameterNamed(LoanApiConstants.chargesParameterName).isEmpty();

        if (jsonCommand.parameterExists(LoanApiConstants.disbursementDataParameterName)) {
            final JsonArray disbursementDataArray = jsonCommand.arrayOfParameterNamed(LoanApiConstants.disbursementDataParameterName);
            if (disbursementDataArray != null && disbursementDataArray.size() > 0) {
                String dateFormat = null;
                Locale locale = null;
                Map<String, String> dateAndLocale = getDateFormatAndLocale(jsonCommand);
                dateFormat = dateAndLocale.get(LoanApiConstants.dateFormatParameterName);
                if (dateAndLocale.containsKey(LoanApiConstants.localeParameterName)) {
                    locale = JsonParserHelper.localeFromString(dateAndLocale.get(LoanApiConstants.localeParameterName));
                }
                for (JsonElement jsonElement : disbursementDataArray) {
                    final JsonObject jsonObject = jsonElement.getAsJsonObject();
                    Map<String, Object> parsedDisbursementData = parseDisbursementDetails(jsonObject, dateFormat, locale);
                    LocalDate expectedDisbursementDate = (LocalDate) parsedDisbursementData
                            .get(LoanApiConstants.expectedDisbursementDateParameterName);
                    BigDecimal principal = (BigDecimal) parsedDisbursementData.get(LoanApiConstants.disbursementPrincipalParameterName);
                    Long disbursementID = (Long) parsedDisbursementData.get(LoanApiConstants.disbursementIdParameterName);
                    chargeIds = (String) parsedDisbursementData.get(LoanApiConstants.loanChargeIdParameterName);
                    if (chargeIds != null) {
                        if (chargeIds.contains(",")) {
                            Iterable<String> chargeId = Splitter.on(',').split(chargeIds);
                            for (String loanChargeId : chargeId) {
                                loanChargeIds.remove(Long.parseLong(loanChargeId));
                            }
                        } else {
                            loanChargeIds.remove(Long.parseLong(chargeIds));
                        }
                    }
                    createOrUpdateDisbursementDetails(disbursementID, actualChanges, expectedDisbursementDate, principal, disbursementList);
                }
                removeDisbursementAndAssociatedCharges(actualChanges, disbursementList, loanChargeIds, chargeIdLength, removeAllCharges);
            }
        }
    }

    private void removeDisbursementAndAssociatedCharges(final Map<String, Object> actualChanges, List<Long> disbursementList,
            List<Long> loanChargeIds, int chargeIdLength, boolean removeAllChages) {
        if (removeAllChages) {
            LoanCharge[] tempCharges = new LoanCharge[this.charges.size()];
            this.charges.toArray(tempCharges);
            for (LoanCharge loanCharge : tempCharges) {
                removeLoanCharge(loanCharge);
            }
            this.trancheCharges.clear();
        } else {
            if (!loanChargeIds.isEmpty() && loanChargeIds.size() != chargeIdLength) {
                for (Long chargeId : loanChargeIds) {
                    LoanCharge deleteCharge = fetchLoanChargesById(chargeId);
                    if (this.charges.contains(deleteCharge)) {
                        removeLoanCharge(deleteCharge);
                    }
                }
            }
        }
        for (Long id : disbursementList) {
            removeChargesByDisbursementID(id);
            this.disbursementDetails.remove(fetchLoanDisbursementsById(id));
            actualChanges.put(RECALCULATE_LOAN_SCHEDULE, true);
        }
    }

    private void createOrUpdateDisbursementDetails(Long disbursementID, final Map<String, Object> actualChanges,
            LocalDate expectedDisbursementDate, BigDecimal principal, List<Long> existingDisbursementList) {
        if (disbursementID != null) {
            LoanDisbursementDetails loanDisbursementDetail = fetchLoanDisbursementsById(disbursementID);
            existingDisbursementList.remove(disbursementID);
            if (loanDisbursementDetail.actualDisbursementDate() == null) {
                LocalDate actualDisbursementDate = null;
                LoanDisbursementDetails disbursementDetails = new LoanDisbursementDetails(expectedDisbursementDate, actualDisbursementDate,
                        principal, this.netDisbursalAmount, false);
                disbursementDetails.updateLoan(this);
                if (!loanDisbursementDetail.equals(disbursementDetails)) {
                    loanDisbursementDetail.copy(disbursementDetails);
                    actualChanges.put("disbursementDetailId", disbursementID);
                    actualChanges.put(RECALCULATE_LOAN_SCHEDULE, true);
                }
            }
        } else {
            LocalDate actualDisbursementDate = null;
            LoanDisbursementDetails disbursementDetails = new LoanDisbursementDetails(expectedDisbursementDate, actualDisbursementDate,
                    principal, this.netDisbursalAmount, false);
            disbursementDetails.updateLoan(this);
            this.disbursementDetails.add(disbursementDetails);
            for (LoanTrancheCharge trancheCharge : trancheCharges) {
                Charge chargeDefinition = trancheCharge.getCharge();
                ExternalId externalId = ExternalId.empty();
                if (TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
                    externalId = ExternalId.generate();
                }
                final LoanCharge loanCharge = new LoanCharge(this, chargeDefinition, principal, null, null, null, expectedDisbursementDate,
                        null, null, BigDecimal.ZERO, externalId);
                LoanTrancheDisbursementCharge loanTrancheDisbursementCharge = new LoanTrancheDisbursementCharge(loanCharge,
                        disbursementDetails);
                loanCharge.updateLoanTrancheDisbursementCharge(loanTrancheDisbursementCharge);
                addLoanCharge(loanCharge);
            }
            actualChanges.put(LoanApiConstants.disbursementDataParameterName, expectedDisbursementDate + "-" + principal);
            actualChanges.put(RECALCULATE_LOAN_SCHEDULE, true);
        }
    }

    private void removeChargesByDisbursementID(Long id) {
        getCharges().stream() //
                .filter(charge -> { //
                    LoanTrancheDisbursementCharge transCharge = charge.getTrancheDisbursementCharge(); //
                    return transCharge != null && id.equals(transCharge.getloanDisbursementDetails().getId()); //
                }) //
                .forEach(this::removeLoanCharge);
    }

    private List<Long> fetchLoanTrancheChargeIds() {
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

    private List<Long> fetchDisbursementIds() {
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

    public List<LoanDisbursementDetails> getLoanDisbursementDetails() {
        List<LoanDisbursementDetails> currentDisbursementDetails = getDisbursementDetails();
        if (loanProduct.isDisallowExpectedDisbursements()) {
            if (!currentDisbursementDetails.isEmpty()) {
                final String errorMessage = "For this loan product, disbursement details are not allowed";
                throw new MultiDisbursementDataNotAllowedException(LoanApiConstants.disbursementDataParameterName, errorMessage);
            }
        } else {
            if (currentDisbursementDetails.isEmpty()) {
                final String errorMessage = "For this loan product, disbursement details must be provided";
                throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
            }
        }
        return currentDisbursementDetails;
    }

    @Deprecated // moved to LoanApplicationValidator
    private BigDecimal getOverAppliedMax() {
        if ("percentage".equals(getLoanProduct().getOverAppliedCalculationType())) {
            BigDecimal overAppliedNumber = BigDecimal.valueOf(getLoanProduct().getOverAppliedNumber());
            BigDecimal totalPercentage = BigDecimal.valueOf(1).add(overAppliedNumber.divide(BigDecimal.valueOf(100)));
            return proposedPrincipal.multiply(totalPercentage);
        } else {
            return proposedPrincipal.add(BigDecimal.valueOf(getLoanProduct().getOverAppliedNumber()));
        }
    }

    public Map<String, Object> undoApproval(final LoanLifecycleStateMachine loanLifecycleStateMachine) {
        validateAccountStatus(LoanEvent.LOAN_APPROVAL_UNDO);
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final LoanStatus currentStatus = LoanStatus.fromInt(this.loanStatus);
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
                .map(LoanTransaction::getId) //
                .collect(Collectors.toList());
    }

    public List<Long> findExistingReversedTransactionIds() {
        return getLoanTransactions().stream() //
                .filter(LoanTransaction::isReversed) //
                .map(LoanTransaction::getId) //
                .collect(Collectors.toList());
    }

    public List<LoanDisbursementDetails> getDisbursedLoanDisbursementDetails() {
        return getDisbursementDetails().stream() //
                .filter(it -> it.actualDisbursementDate() != null) //
                .collect(Collectors.toList());
    }

    public boolean canDisburse(final LocalDate actualDisbursementDate) {
        LocalDate loanSubmittedOnDate = this.submittedOnDate;
        final LoanStatus statusEnum = this.loanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_DISBURSED, this);

        boolean isMultiTrancheDisburse = false;
        LoanStatus actualLoanStatus = LoanStatus.fromInt(this.loanStatus);
        if ((actualLoanStatus.isActive() || actualLoanStatus.isClosedObligationsMet() || actualLoanStatus.isOverpaid())
                && isAllTranchesNotDisbursed()) {
            if (DateUtils.isBefore(actualDisbursementDate, loanSubmittedOnDate)) {
                final String errorMsg = "Loan can't be disbursed before " + loanSubmittedOnDate;
                throw new LoanDisbursalException(errorMsg, "actualdisbursementdate.before.submittedDate", loanSubmittedOnDate,
                        actualDisbursementDate);
            }
            isMultiTrancheDisburse = true;
        }
        return !statusEnum.hasStateOf(actualLoanStatus) || isMultiTrancheDisburse;
    }

    public Money adjustDisburseAmount(@NotNull JsonCommand command, @NotNull LocalDate actualDisbursementDate) {
        Money disburseAmount = this.loanRepaymentScheduleDetail.getPrincipal().zero();
        BigDecimal principalDisbursed = command.bigDecimalValueOfParameterNamed(LoanApiConstants.principalDisbursedParameterName);
        if (this.actualDisbursementDate == null || DateUtils.isBefore(actualDisbursementDate, this.actualDisbursementDate)) {
            this.actualDisbursementDate = actualDisbursementDate;
        }
        BigDecimal diff = BigDecimal.ZERO;
        Collection<LoanDisbursementDetails> details = fetchUndisbursedDetail();
        if (principalDisbursed == null) {
            disburseAmount = this.loanRepaymentScheduleDetail.getPrincipal();
            if (!details.isEmpty()) {
                disburseAmount = disburseAmount.zero();
                for (LoanDisbursementDetails disbursementDetails : details) {
                    disbursementDetails.updateActualDisbursementDate(actualDisbursementDate);
                    disburseAmount = disburseAmount.plus(disbursementDetails.principal());
                }
            }
        } else {
            if (this.loanProduct.isMultiDisburseLoan()) {
                disburseAmount = Money.of(getCurrency(), principalDisbursed);
            } else {
                disburseAmount = disburseAmount.plus(principalDisbursed);
            }

            if (details.isEmpty()) {
                diff = this.loanRepaymentScheduleDetail.getPrincipal().minus(principalDisbursed).getAmount();
            } else {
                for (LoanDisbursementDetails disbursementDetails : details) {
                    disbursementDetails.updateActualDisbursementDate(actualDisbursementDate);
                    disbursementDetails.updatePrincipal(principalDisbursed);
                }
            }
            if (this.loanProduct().isMultiDisburseLoan()) {
                Collection<LoanDisbursementDetails> loanDisburseDetails = this.getDisbursementDetails();
                BigDecimal setPrincipalAmount = BigDecimal.ZERO;
                BigDecimal totalAmount = BigDecimal.ZERO;
                for (LoanDisbursementDetails disbursementDetails : loanDisburseDetails) {
                    if (disbursementDetails.actualDisbursementDate() != null) {
                        setPrincipalAmount = setPrincipalAmount.add(disbursementDetails.principal());
                    }
                    totalAmount = totalAmount.add(disbursementDetails.principal());
                }
                this.loanRepaymentScheduleDetail.setPrincipal(setPrincipalAmount);
                compareDisbursedToApprovedOrProposedPrincipal(disburseAmount.getAmount(), totalAmount);
            } else {
                this.loanRepaymentScheduleDetail.setPrincipal(this.loanRepaymentScheduleDetail.getPrincipal().minus(diff).getAmount());
            }
            if (!this.loanProduct().isMultiDisburseLoan() && diff.compareTo(BigDecimal.ZERO) < 0) {
                final String errorMsg = "Loan can't be disbursed,disburse amount is exceeding approved amount ";
                throw new LoanDisbursalException(errorMsg, "disburse.amount.must.be.less.than.approved.amount", principalDisbursed,
                        this.loanRepaymentScheduleDetail.getPrincipal().getAmount());
            }
        }
        return disburseAmount;
    }

    private void compareDisbursedToApprovedOrProposedPrincipal(BigDecimal disbursedAmount, BigDecimal totalDisbursed) {
        if (this.loanProduct().isDisallowExpectedDisbursements() && this.loanProduct().isAllowApprovedDisbursedAmountsOverApplied()) {
            BigDecimal maxDisbursedAmount = getOverAppliedMax();
            if (totalDisbursed.compareTo(maxDisbursedAmount) > 0) {
                final String errorMessage = String.format(
                        "Loan disbursal amount can't be greater than maximum applied loan amount calculation. "
                                + "Total disbursed amount: %s  Maximum disbursal amount: %s",
                        totalDisbursed.stripTrailingZeros().toPlainString(), maxDisbursedAmount.stripTrailingZeros().toPlainString());
                throw new InvalidLoanStateTransitionException("disbursal",
                        "amount.can't.be.greater.than.maximum.applied.loan.amount.calculation", errorMessage, disbursedAmount,
                        maxDisbursedAmount);
            }
        } else {
            if (totalDisbursed.compareTo(this.approvedPrincipal) > 0) {
                final String errorMsg = "Loan can't be disbursed,disburse amount is exceeding approved principal ";
                throw new LoanDisbursalException(errorMsg, "disburse.amount.must.be.less.than.approved.principal", totalDisbursed,
                        this.approvedPrincipal);
            }
        }
    }

    private Collection<LoanDisbursementDetails> fetchUndisbursedDetail() {
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

    private void removeDisbursementDetail() {
        getDisbursementDetails().removeIf(it -> it.actualDisbursementDate() == null);
    }

    private boolean isDisbursementAllowed() {
        List<LoanDisbursementDetails> disbursementDetails = getDisbursementDetails();
        boolean isSingleDisburseLoanDisbursementAllowed = disbursementDetails == null || disbursementDetails.isEmpty()
                || disbursementDetails.stream().anyMatch(it -> it.actualDisbursementDate() == null);
        boolean isMultiDisburseLoanDisbursementAllowed = isMultiDisburmentLoan()
                && (disbursementDetails == null || disbursementDetails.stream().filter(it -> it.actualDisbursementDate() != null)
                        .count() < loanProduct.getLoanProductTrancheDetails().maxTrancheCount());
        return isSingleDisburseLoanDisbursementAllowed || isMultiDisburseLoanDisbursementAllowed;
    }

    private boolean atLeastOnceDisbursed() {
        return getDisbursementDetails().stream().anyMatch(it -> it.actualDisbursementDate() != null);
    }

    public void updateLoanRepaymentPeriodsDerivedFields(final LocalDate actualDisbursementDate) {
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment repaymentPeriod : installments) {
            repaymentPeriod.updateDerivedFields(loanCurrency(), actualDisbursementDate);
        }
    }

    /**
     * Ability to regenerate the repayment schedule based on the loans current details/state.
     */
    public void regenerateRepaymentSchedule(final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        final LoanScheduleModel loanSchedule = regenerateScheduleModel(scheduleGeneratorDTO);
        if (loanSchedule == null) {
            return;
        }
        updateLoanSchedule(loanSchedule);
        final Set<LoanCharge> charges = this.getActiveCharges();
        for (final LoanCharge loanCharge : charges) {
            if (!loanCharge.isWaived()) {
                recalculateLoanCharge(loanCharge, scheduleGeneratorDTO.getPenaltyWaitPeriod());
            }
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

    public void handleDisbursementTransaction(final LocalDate disbursedOn, final PaymentDetail paymentDetail) {
        // add repayment transaction to track incoming money from client to mfi
        // for (charges due at time of disbursement)

        /*
         * TODO Vishwas: do we need to be able to pass in payment type details for repayments at disbursements too?
         */

        final Money totalFeeChargesDueAtDisbursement = this.summary.getTotalFeeChargesDueAtDisbursement(loanCurrency());
        /*
         * all Charges repaid at disbursal is marked as repaid and "APPLY Charge" transactions are created for all other
         * fees ( which are created during disbursal but not repaid)
         */

        Money disbursentMoney = Money.zero(getCurrency());
        final LoanTransaction chargesPayment = LoanTransaction.repaymentAtDisbursement(getOffice(), disbursentMoney, paymentDetail,
                disbursedOn, null);
        final Integer installmentNumber = null;
        for (final LoanCharge charge : getActiveCharges()) {
            LocalDate actualDisbursementDate = getActualDisbursementDate(charge);
            /*
             * create a Charge applied transaction if Up front Accrual, None or Cash based accounting is enabled
             */
            if ((charge.getCharge().getChargeTimeType().equals(ChargeTimeType.DISBURSEMENT.getValue())
                    && disbursedOn.equals(actualDisbursementDate) && !charge.isWaived() && !charge.isFullyPaid())
                    || (charge.getCharge().getChargeTimeType().equals(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue())
                            && disbursedOn.equals(actualDisbursementDate) && !charge.isWaived() && !charge.isFullyPaid())) {
                if (totalFeeChargesDueAtDisbursement.isGreaterThanZero() && !charge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
                    charge.markAsFullyPaid();
                    // Add "Loan Charge Paid By" details to this transaction
                    final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(chargesPayment, charge, charge.amount(),
                            installmentNumber);
                    chargesPayment.getLoanChargesPaid().add(loanChargePaidBy);
                    disbursentMoney = disbursentMoney.plus(charge.amount());
                }
            } else if (disbursedOn.equals(this.actualDisbursementDate) && isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()) {
                handleChargeAppliedTransaction(charge, disbursedOn);
            }
        }

        if (disbursentMoney.isGreaterThanZero()) {
            final Money zero = Money.zero(getCurrency());
            chargesPayment.updateComponentsAndTotal(zero, zero, disbursentMoney, zero);
            chargesPayment.updateLoan(this);
            addLoanTransaction(chargesPayment);
            updateLoanOutstandingBalances();
        }

        if (getApprovedOnDate() != null && DateUtils.isBefore(disbursedOn, getApprovedOnDate())) {
            final String errorMessage = "The date on which a loan is disbursed cannot be before its approval date: "
                    + getApprovedOnDate().toString();
            throw new InvalidLoanStateTransitionException("disbursal", "cannot.be.before.approval.date", errorMessage, disbursedOn,
                    getApprovedOnDate());
        }

        LocalDate expectedDate = getExpectedFirstRepaymentOnDate();
        if (expectedDate != null && (DateUtils.isAfter(disbursedOn, this.fetchRepaymentScheduleInstallment(1).getDueDate())
                || DateUtils.isAfter(disbursedOn, expectedDate)) && DateUtils.isEqual(disbursedOn, this.actualDisbursementDate)) {
            final String errorMessage = "submittedOnDate cannot be after the loans  expectedFirstRepaymentOnDate: "
                    + expectedDate.toString();
            throw new InvalidLoanStateTransitionException("disbursal", "cannot.be.after.expected.first.repayment.date", errorMessage,
                    disbursedOn, expectedDate);
        }

        validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_DISBURSED, disbursedOn);

        if (DateUtils.isDateInTheFuture(disbursedOn)) {
            final String errorMessage = "The date on which a loan with identifier : " + this.accountNumber
                    + " is disbursed cannot be in the future.";
            throw new InvalidLoanStateTransitionException("disbursal", "cannot.be.a.future.date", errorMessage, disbursedOn);
        }
    }

    public LoanTransaction handleDownPayment(final LoanTransaction disbursementTransaction, final JsonCommand command,
            final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        LocalDate disbursedOn = command.localDateValueOfParameterNamed(ACTUAL_DISBURSEMENT_DATE);
        BigDecimal disbursedAmountPercentageForDownPayment = this.loanRepaymentScheduleDetail.getDisbursedAmountPercentageForDownPayment();
        ExternalId externalId = ExternalId.empty();
        if (TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }
        Money downPaymentMoney = Money.of(getCurrency(),
                MathUtil.percentageOf(disbursementTransaction.getAmount(), disbursedAmountPercentageForDownPayment, 19));
        if (getLoanProduct().getInstallmentAmountInMultiplesOf() != null) {
            downPaymentMoney = Money.roundToMultiplesOf(downPaymentMoney, getLoanProduct().getInstallmentAmountInMultiplesOf());
        }
        Money adjustedDownPaymentMoney = switch (getLoanProductRelatedDetail().getLoanScheduleType()) {
            // For Cumulative loan: To check whether the loan was overpaid when the disbursement happened and to get the
            // proper amount after the disbursement we are using two balances:
            // 1. Whether the loan is still overpaid after the disbursement,
            // 2. if the loan is not overpaid anymore after the disbursement, but was it more overpaid than the
            // calculated down-payment amount?
            case CUMULATIVE -> {
                if (getTotalOverpaidAsMoney().isGreaterThanZero()) {
                    yield Money.zero(getCurrency());
                }
                yield MathUtil.negativeToZero(downPaymentMoney.minus(MathUtil.negativeToZero(disbursementTransaction
                        .getAmount(getCurrency()).minus(disbursementTransaction.getOutstandingLoanBalanceMoney(getCurrency())))));
            }
            // For Progressive loan: Disbursement transaction portion balances are enough to see whether the overpayment
            // amount was more than the calculated down-payment amount
            case PROGRESSIVE ->
                MathUtil.negativeToZero(downPaymentMoney.minus(disbursementTransaction.getOverPaymentPortion(getCurrency())));
        };

        if (adjustedDownPaymentMoney.isGreaterThanZero()) {
            LoanTransaction downPaymentTransaction = LoanTransaction.downPayment(getOffice(), adjustedDownPaymentMoney, null, disbursedOn,
                    externalId);
            LoanEvent event = LoanEvent.LOAN_REPAYMENT_OR_WAIVER;
            validateRepaymentTypeAccountStatus(downPaymentTransaction, event);
            HolidayDetailDTO holidayDetailDTO = scheduleGeneratorDTO.getHolidayDetailDTO();
            validateRepaymentDateIsOnHoliday(downPaymentTransaction.getTransactionDate(), holidayDetailDTO.isAllowTransactionsOnHoliday(),
                    holidayDetailDTO.getHolidays());
            validateRepaymentDateIsOnNonWorkingDay(downPaymentTransaction.getTransactionDate(), holidayDetailDTO.getWorkingDays(),
                    holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());

            handleRepaymentOrRecoveryOrWaiverTransaction(downPaymentTransaction, loanLifecycleStateMachine, null, scheduleGeneratorDTO);
            return downPaymentTransaction;
        } else {
            return null;
        }
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

    public Map<String, Object> undoDisbursal(final ScheduleGeneratorDTO scheduleGeneratorDTO, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {
        validateAccountStatus(LoanEvent.LOAN_DISBURSAL_UNDO);

        final Map<String, Object> actualChanges = new LinkedHashMap<>();
        final LoanStatus currentStatus = LoanStatus.fromInt(this.loanStatus);
        final LoanStatus statusEnum = this.loanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_DISBURSAL_UNDO, this);
        validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_DISBURSAL_UNDO, getDisbursementDate());
        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());
        if (!statusEnum.hasStateOf(currentStatus)) {
            this.loanLifecycleStateMachine.transition(LoanEvent.LOAN_DISBURSAL_UNDO, this);
            actualChanges.put(PARAM_STATUS, LoanEnumerations.status(this.loanStatus));

            final LocalDate actualDisbursementDate = getDisbursementDate();
            final boolean isScheduleRegenerateRequired = isActualDisbursedOnDateEarlierOrLaterThanExpected(actualDisbursementDate);
            this.actualDisbursementDate = null;
            this.disbursedBy = null;
            boolean isDisbursedAmountChanged = !MathUtil.isEqualTo(approvedPrincipal,
                    this.loanRepaymentScheduleDetail.getPrincipal().getAmount());
            this.loanRepaymentScheduleDetail.setPrincipal(this.approvedPrincipal);
            // Remove All the Disbursement Details If the Loan Product is disabled and exists one
            if (this.loanProduct().isDisallowExpectedDisbursements() && !getDisbursementDetails().isEmpty()) {
                for (LoanDisbursementDetails disbursementDetail : getAllDisbursementDetails()) {
                    disbursementDetail.reverse();
                }
            } else {
                for (final LoanDisbursementDetails details : getDisbursementDetails()) {
                    details.updateActualDisbursementDate(null);
                }
            }
            boolean isEmiAmountChanged = !this.loanTermVariations.isEmpty();

            updateLoanToPreDisbursalState();
            if (isScheduleRegenerateRequired || isDisbursedAmountChanged || isEmiAmountChanged
                    || this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                // clear off actual disbusrement date so schedule regeneration
                // uses expected date.

                regenerateRepaymentSchedule(scheduleGeneratorDTO);
                if (isDisbursedAmountChanged) {
                    updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
                }
            } else if (isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
                for (final LoanRepaymentScheduleInstallment period : getRepaymentScheduleInstallments()) {
                    period.resetAccrualComponents();
                }
            }

            if (this.isTopup) {
                this.loanTopupDetails.setAccountTransferDetails(null);
                this.loanTopupDetails.setTopupAmount(null);
            }

            this.adjustNetDisbursalAmount(this.approvedPrincipal);
            actualChanges.put(ACTUAL_DISBURSEMENT_DATE, "");
            updateLoanSummaryDerivedFields();
        }

        return actualChanges;
    }

    private void reverseExistingTransactions() {
        Collection<LoanTransaction> retainTransactions = new ArrayList<>();
        for (final LoanTransaction transaction : this.loanTransactions) {
            transaction.reverse();
            if (transaction.getId() != null) {
                retainTransactions.add(transaction);
            }
        }
        this.loanTransactions.retainAll(retainTransactions);
    }

    private void updateLoanToPreDisbursalState() {
        this.actualDisbursementDate = null;

        this.accruedTill = null;
        reverseExistingTransactions();

        for (final LoanCharge charge : getActiveCharges()) {
            if (charge.isOverdueInstallmentCharge()) {
                charge.setActive(false);
            } else {
                charge.resetToOriginal(loanCurrency());
            }
        }
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            currentInstallment.resetDerivedComponents();
        }
        for (LoanTermVariations variations : this.loanTermVariations) {
            if (variations.getOnLoanStatus().equals(LoanStatus.ACTIVE.getValue())) {
                variations.markAsInactive();
            }
        }
        final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
        wrapper.reprocess(getCurrency(), getDisbursementDate(), getRepaymentScheduleInstallments(), getActiveCharges());

        updateLoanSummaryDerivedFields();
    }

    public ChangedTransactionDetail waiveInterest(final LoanTransaction waiveInterestTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        validateAccountStatus(LoanEvent.LOAN_REPAYMENT_OR_WAIVER);
        validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_REPAYMENT_OR_WAIVER,
                waiveInterestTransaction.getTransactionDate());
        validateActivityNotBeforeLastTransactionDate(LoanEvent.LOAN_REPAYMENT_OR_WAIVER, waiveInterestTransaction.getTransactionDate());

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        return handleRepaymentOrRecoveryOrWaiverTransaction(waiveInterestTransaction, loanLifecycleStateMachine, null,
                scheduleGeneratorDTO);
    }

    @SuppressWarnings("null")
    public ChangedTransactionDetail makeRepayment(final LoanTransaction repaymentTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, boolean isRecoveryRepayment, final ScheduleGeneratorDTO scheduleGeneratorDTO,
            Boolean isHolidayValidationDone) {
        LoanEvent event = isRecoveryRepayment ? LoanEvent.LOAN_RECOVERY_PAYMENT : LoanEvent.LOAN_REPAYMENT_OR_WAIVER;

        HolidayDetailDTO holidayDetailDTO = null;
        if (!isHolidayValidationDone) {
            holidayDetailDTO = scheduleGeneratorDTO.getHolidayDetailDTO();
        }
        validateRepaymentTypeAccountStatus(repaymentTransaction, event);
        validateActivityNotBeforeClientOrGroupTransferDate(event, repaymentTransaction.getTransactionDate());
        validateRepaymentTypeTransactionNotBeforeAChargeRefund(repaymentTransaction, "created");
        validateActivityNotBeforeLastTransactionDate(event, repaymentTransaction.getTransactionDate());
        if (!isHolidayValidationDone) {
            validateRepaymentDateIsOnHoliday(repaymentTransaction.getTransactionDate(), holidayDetailDTO.isAllowTransactionsOnHoliday(),
                    holidayDetailDTO.getHolidays());
            validateRepaymentDateIsOnNonWorkingDay(repaymentTransaction.getTransactionDate(), holidayDetailDTO.getWorkingDays(),
                    holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());
        }
        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        return handleRepaymentOrRecoveryOrWaiverTransaction(repaymentTransaction, loanLifecycleStateMachine, null, scheduleGeneratorDTO);
    }

    private void validateRepaymentTypeAccountStatus(LoanTransaction repaymentTransaction, LoanEvent event) {
        if (repaymentTransaction.isGoodwillCredit() || repaymentTransaction.isInterestPaymentWaiver()
                || repaymentTransaction.isMerchantIssuedRefund() || repaymentTransaction.isPayoutRefund()
                || repaymentTransaction.isChargeRefund() || repaymentTransaction.isRepayment() || repaymentTransaction.isDownPayment()) {

            if (!(isOpen() || isClosedObligationsMet() || isOverPaid())) {
                final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                final String defaultUserMessage = "Loan must be Active, Fully Paid or Overpaid";
                final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.must.be.active.fully.paid.or.overpaid",
                        defaultUserMessage);
                dataValidationErrors.add(error);
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        } else {
            validateAccountStatus(event);
        }

    }

    public void makeChargePayment(final Long chargeId, final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds,
            final HolidayDetailDTO holidayDetailDTO, final LoanTransaction paymentTransaction, final Integer installmentNumber) {
        validateAccountStatus(LoanEvent.LOAN_CHARGE_PAYMENT);
        validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_CHARGE_PAYMENT, paymentTransaction.getTransactionDate());
        validateActivityNotBeforeLastTransactionDate(LoanEvent.LOAN_CHARGE_PAYMENT, paymentTransaction.getTransactionDate());
        validateRepaymentDateIsOnHoliday(paymentTransaction.getTransactionDate(), holidayDetailDTO.isAllowTransactionsOnHoliday(),
                holidayDetailDTO.getHolidays());
        validateRepaymentDateIsOnNonWorkingDay(paymentTransaction.getTransactionDate(), holidayDetailDTO.getWorkingDays(),
                holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());

        if (DateUtils.isDateInTheFuture(paymentTransaction.getTransactionDate())) {
            final String errorMessage = "The date on which a loan charge paid cannot be in the future.";
            throw new InvalidLoanStateTransitionException("charge.payment", "cannot.be.a.future.date", errorMessage,
                    paymentTransaction.getTransactionDate());
        }
        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());
        LoanCharge charge = null;
        for (final LoanCharge loanCharge : this.charges) {
            if (loanCharge.isActive() && chargeId.equals(loanCharge.getId())) {
                charge = loanCharge;
            }
        }
        handleChargePaidTransaction(charge, paymentTransaction, loanLifecycleStateMachine, installmentNumber);
    }

    public void makeRefund(final LoanTransaction loanTransaction, final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds,
            final boolean allowTransactionsOnHoliday, final List<Holiday> holidays, final WorkingDays workingDays,
            final boolean allowTransactionsOnNonWorkingDay) {
        validateRepaymentDateIsOnHoliday(loanTransaction.getTransactionDate(), allowTransactionsOnHoliday, holidays);
        validateRepaymentDateIsOnNonWorkingDay(loanTransaction.getTransactionDate(), workingDays, allowTransactionsOnNonWorkingDay);

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        if (getStatus().isOverpaid()) {
            if (this.totalOverpaid.compareTo(loanTransaction.getAmount(getCurrency()).getAmount()) < 0) {
                final String errorMessage = "The refund amount must be less than or equal to overpaid amount ";
                throw new InvalidLoanStateTransitionException("transaction", "is.exceeding.overpaid.amount", errorMessage,
                        this.totalOverpaid, loanTransaction.getAmount(getCurrency()).getAmount());
            } else if (!isAfterLastRepayment(loanTransaction, getLoanTransactions())) {
                final String errorMessage = "Transfer funds is allowed only after last repayment date";
                throw new InvalidLoanStateTransitionException("transaction", "is.not.after.repayment.date", errorMessage);
            }
        } else {
            final String errorMessage = "Transfer funds is allowed only for loan accounts with overpaid status ";
            throw new InvalidLoanStateTransitionException("transaction", "is.not.a.overpaid.loan", errorMessage);
        }

        loanTransaction.updateLoan(this);

        if (loanTransaction.isNotZero(loanCurrency())) {
            addLoanTransaction(loanTransaction);
        }
        updateLoanSummaryDerivedFields();
        doPostLoanTransactionChecks(loanTransaction.getTransactionDate(), loanLifecycleStateMachine);
    }

    private ChangedTransactionDetail handleRepaymentOrRecoveryOrWaiverTransaction(final LoanTransaction loanTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final LoanTransaction adjustedTransaction,
            final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        ChangedTransactionDetail changedTransactionDetail = null;

        if (loanTransaction.isRecoveryRepayment()) {
            loanLifecycleStateMachine.transition(LoanEvent.LOAN_RECOVERY_PAYMENT, this);
        }

        if (loanTransaction.isRecoveryRepayment()
                && loanTransaction.getAmount(loanCurrency()).getAmount().compareTo(getSummary().getTotalWrittenOff()) > 0) {
            final String errorMessage = "The transaction amount cannot greater than the remaining written off amount.";
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.greater.than.total.written.off", errorMessage);
        }

        loanTransaction.updateLoan(this);

        final boolean isTransactionChronologicallyLatest = isChronologicallyLatestRepaymentOrWaiver(loanTransaction, getLoanTransactions());

        if (loanTransaction.isNotZero(loanCurrency())) {
            addLoanTransaction(loanTransaction);
        }

        if (loanTransaction.isNotRepaymentLikeType() && loanTransaction.isNotWaiver() && loanTransaction.isNotRecoveryRepayment()) {
            final String errorMessage = "A transaction of type repayment or recovery repayment or waiver was expected but not received.";
            throw new InvalidLoanTransactionTypeException("transaction", "is.not.a.repayment.or.waiver.or.recovery.transaction",
                    errorMessage);
        }

        final LocalDate loanTransactionDate = extractTransactionDate(loanTransaction);

        if (DateUtils.isDateInTheFuture(loanTransactionDate)) {
            final String errorMessage = "The transaction date cannot be in the future.";
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.a.future.date", errorMessage, loanTransactionDate);
        }

        if (loanTransaction.isInterestWaiver()) {
            Money totalInterestOutstandingOnLoan = getTotalInterestOutstandingOnLoan();
            if (adjustedTransaction != null) {
                totalInterestOutstandingOnLoan = totalInterestOutstandingOnLoan.plus(adjustedTransaction.getAmount(loanCurrency()));
            }
            if (loanTransaction.getAmount(loanCurrency()).isGreaterThan(totalInterestOutstandingOnLoan)) {
                final String errorMessage = "The amount of interest to waive cannot be greater than total interest outstanding on loan.";
                throw new InvalidLoanStateTransitionException("waive.interest", "amount.exceeds.total.outstanding.interest", errorMessage,
                        loanTransaction.getAmount(loanCurrency()), totalInterestOutstandingOnLoan.getAmount());
            }
        }

        if (this.loanProduct.isMultiDisburseLoan() && adjustedTransaction == null) {
            BigDecimal totalDisbursed = getDisbursedAmount();
            BigDecimal totalPrincipalAdjusted = this.summary.getTotalPrincipalAdjustments();
            BigDecimal totalPrincipalCredited = totalDisbursed.add(totalPrincipalAdjusted);
            if (totalPrincipalCredited.compareTo(this.summary.getTotalPrincipalRepaid()) < 0) {
                final String errorMessage = "The transaction amount cannot exceed threshold.";
                throw new InvalidLoanStateTransitionException("transaction", "amount.exceeds.threshold", errorMessage);
            }
        }

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);

        final LoanRepaymentScheduleInstallment currentInstallment = fetchLoanRepaymentScheduleInstallment(
                loanTransaction.getTransactionDate());

        boolean reprocess = isForeclosure() || !isTransactionChronologicallyLatest || adjustedTransaction != null
                || !DateUtils.isEqualBusinessDate(loanTransaction.getTransactionDate()) || currentInstallment == null
                || !currentInstallment.getTotalOutstanding(getCurrency()).isEqualTo(loanTransaction.getAmount(getCurrency()));

        if (isTransactionChronologicallyLatest && adjustedTransaction == null
                && (!reprocess || !this.repaymentScheduleDetail().isInterestRecalculationEnabled()) && !isForeclosure()) {
            loanRepaymentScheduleTransactionProcessor.processLatestTransaction(loanTransaction, new TransactionCtx(getCurrency(),
                    getRepaymentScheduleInstallments(), getActiveCharges(), new MoneyHolder(getTotalOverpaidAsMoney())));
            reprocess = false;
            if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                if (currentInstallment == null || currentInstallment.isNotFullyPaidOff()) {
                    reprocess = true;
                } else {
                    final LoanRepaymentScheduleInstallment nextInstallment = fetchRepaymentScheduleInstallment(
                            currentInstallment.getInstallmentNumber() + 1);
                    if (nextInstallment != null && nextInstallment.getTotalPaidInAdvance(getCurrency()).isGreaterThanZero()) {
                        reprocess = true;
                    }
                }
            }
        }
        if (reprocess) {
            if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO);
            }
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsForReprocessing();
            changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(getDisbursementDate(),
                    allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(), getActiveCharges());
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                mapEntry.getValue().updateLoan(this);
            }
            /*
             * Commented since throwing exception if external id present for one of the transactions. for this need to
             * save the reversed transactions first and then new transactions.
             */
            this.loanTransactions.addAll(changedTransactionDetail.getNewTransactionMappings().values());
        }

        updateLoanSummaryDerivedFields();

        /**
         * FIXME: Vishwas, skipping post loan transaction checks for Loan recoveries
         **/
        if (loanTransaction.isNotRecoveryRepayment()) {
            doPostLoanTransactionChecks(loanTransaction.getTransactionDate(), loanLifecycleStateMachine);
        }

        if (this.loanProduct.isMultiDisburseLoan()) {
            BigDecimal totalDisbursed = getDisbursedAmount();
            BigDecimal totalPrincipalAdjusted = this.summary.getTotalPrincipalAdjustments();
            BigDecimal totalPrincipalCredited = totalDisbursed.add(totalPrincipalAdjusted);
            if (totalPrincipalCredited.compareTo(this.summary.getTotalPrincipalRepaid()) < 0
                    && this.repaymentScheduleDetail().getPrincipal().minus(totalDisbursed).isGreaterThanZero()) {
                final String errorMessage = "The transaction amount cannot exceed threshold.";
                throw new InvalidLoanStateTransitionException("transaction", "amount.exceeds.threshold", errorMessage);
            }
        }

        return changedTransactionDetail;
    }

    private LocalDate extractTransactionDate(LoanTransaction loanTransaction) {
        final LocalDate loanTransactionDate = loanTransaction.getTransactionDate();
        if (DateUtils.isBefore(loanTransactionDate, getDisbursementDate())) {
            final String errorMessage = "The transaction date cannot be before the loan disbursement date: "
                    + getDisbursementDate().toString();
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.before.disbursement.date", errorMessage,
                    loanTransactionDate, getDisbursementDate());
        }
        return loanTransactionDate;
    }

    public LoanRepaymentScheduleInstallment fetchLoanRepaymentScheduleInstallment(LocalDate dueDate) {
        return getRepaymentScheduleInstallments().stream() //
                .filter(installment -> dueDate.equals(installment.getDueDate())).findFirst() //
                .orElse(null);
    }

    public List<LoanTransaction> retrieveListOfTransactionsForReprocessing() {
        return getLoanTransactions().stream().filter(loanTransactionForReprocessingPredicate()).sorted(LoanTransactionComparator.INSTANCE)
                .collect(Collectors.toList());
    }

    private static Predicate<LoanTransaction> loanTransactionForReprocessingPredicate() {
        return transaction -> transaction.isNotReversed() && (transaction.isChargeOff() || transaction.isReAge()
                || transaction.isAccrualActivity() || transaction.isReAmortize() || !transaction.isNonMonetaryTransaction());
    }

    private List<LoanTransaction> retrieveListOfTransactionsExcludeAccruals() {
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

    private boolean doPostLoanTransactionChecks(final LocalDate transactionDate,
            final LoanLifecycleStateMachine loanLifecycleStateMachine) {
        boolean statusChanged = false;
        boolean isOverpaid = getTotalOverpaid() != null && getTotalOverpaid().compareTo(BigDecimal.ZERO) > 0;
        if (isOverpaid) {
            // FIXME - kw - update account balance to negative amount.
            handleLoanOverpayment(transactionDate, loanLifecycleStateMachine);
            statusChanged = true;
        } else if (this.summary.isRepaidInFull(loanCurrency())) {
            handleLoanRepaymentInFull(transactionDate, loanLifecycleStateMachine);
            statusChanged = true;
        } else {
            loanLifecycleStateMachine.transition(LoanEvent.LOAN_REPAYMENT_OR_WAIVER, this);
        }
        if (this.totalOverpaid == null || BigDecimal.ZERO.compareTo(this.totalOverpaid) == 0) {
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

        } else if (LoanStatus.fromInt(this.loanStatus).isOverpaid()) {
            if (this.totalOverpaid == null || BigDecimal.ZERO.compareTo(this.totalOverpaid) == 0) {
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

    private boolean isChronologicallyLatestRepaymentOrWaiver(final LoanTransaction loanTransaction,
            final List<LoanTransaction> loanTransactions) {
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

    private boolean isAfterLastRepayment(final LoanTransaction loanTransaction, final List<LoanTransaction> loanTransactions) {
        return loanTransactions.stream() //
                .filter(t -> t.isRepaymentLikeType() && t.isNotReversed()) //
                .noneMatch(t -> DateUtils.isBefore(loanTransaction.getTransactionDate(), t.getTransactionDate()));
    }

    private boolean isChronologicallyLatestTransaction(final LoanTransaction loanTransaction,
            final List<LoanTransaction> loanTransactions) {
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
            if (transaction.isRepaymentLikeType() && transaction.isNonZero()) {
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

                final Money outstandingForPeriod = scheduledRepayment.getInterestOutstanding(loanCurrency());
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

    public ChangedTransactionDetail adjustExistingTransaction(final LoanTransaction newTransactionDetail,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final LoanTransaction transactionForAdjustment,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds,
            final ScheduleGeneratorDTO scheduleGeneratorDTO, final ExternalId reversalExternalId) {
        HolidayDetailDTO holidayDetailDTO = scheduleGeneratorDTO.getHolidayDetailDTO();
        validateActivityNotBeforeLastTransactionDate(LoanEvent.LOAN_REPAYMENT_OR_WAIVER, transactionForAdjustment.getTransactionDate());
        validateRepaymentDateIsOnHoliday(newTransactionDetail.getTransactionDate(), holidayDetailDTO.isAllowTransactionsOnHoliday(),
                holidayDetailDTO.getHolidays());
        validateRepaymentDateIsOnNonWorkingDay(newTransactionDetail.getTransactionDate(), holidayDetailDTO.getWorkingDays(),
                holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());

        ChangedTransactionDetail changedTransactionDetail = null;

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_REPAYMENT_OR_WAIVER,
                transactionForAdjustment.getTransactionDate());

        if (transactionForAdjustment.isNotRepaymentLikeType() && transactionForAdjustment.isNotWaiver()
                && transactionForAdjustment.isNotCreditBalanceRefund()) {
            final String errorMessage = "Only (non-reversed) transactions of type repayment, waiver or credit balance refund can be adjusted.";
            throw new InvalidLoanTransactionTypeException("transaction",
                    "adjustment.is.only.allowed.to.repayment.or.waiver.or.creditbalancerefund.transactions", errorMessage);
        }

        transactionForAdjustment.reverse(reversalExternalId);
        transactionForAdjustment.manuallyAdjustedOrReversed();

        if (isClosedWrittenOff()) {
            // find write off transaction and reverse it
            final LoanTransaction writeOffTransaction = findWriteOffTransaction();
            writeOffTransaction.reverse();
        }

        if (isClosedObligationsMet() || isClosedWrittenOff() || isClosedWithOutstandingAmountMarkedForReschedule()) {
            loanLifecycleStateMachine.transition(LoanEvent.LOAN_ADJUST_TRANSACTION, this);
        }

        if (newTransactionDetail.isRepaymentLikeType() || newTransactionDetail.isInterestWaiver()) {
            changedTransactionDetail = handleRepaymentOrRecoveryOrWaiverTransaction(newTransactionDetail, loanLifecycleStateMachine,
                    transactionForAdjustment, scheduleGeneratorDTO);
        }

        return changedTransactionDetail;
    }

    public ChangedTransactionDetail undoWrittenOff(LoanLifecycleStateMachine loanLifecycleStateMachine,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds,
            final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        validateAccountStatus(LoanEvent.WRITE_OFF_OUTSTANDING_UNDO);
        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());
        final LoanTransaction writeOffTransaction = findWriteOffTransaction();
        writeOffTransaction.reverse();
        loanLifecycleStateMachine.transition(LoanEvent.WRITE_OFF_OUTSTANDING_UNDO, this);
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsForReprocessing();
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO);
        }
        ChangedTransactionDetail changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(
                getDisbursementDate(), allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(),
                getActiveCharges());
        updateLoanSummaryDerivedFields();
        return changedTransactionDetail;
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

    private Money calculateTotalOverpayment() {
        Money totalPaidInRepayments = getTotalPaidInRepayments();

        final MonetaryCurrency currency = loanCurrency();
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

    public ChangedTransactionDetail closeAsWrittenOff(final JsonCommand command, final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final Map<String, Object> changes, final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds,
            final AppUser currentUser, final ScheduleGeneratorDTO scheduleGeneratorDTO) {

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
        ChangedTransactionDetail changedTransactionDetail = closeDisbursements(scheduleGeneratorDTO,
                loanRepaymentScheduleTransactionProcessor);

        validateAccountStatus(LoanEvent.WRITE_OFF_OUTSTANDING);

        final LocalDate writtenOffOnLocalDate = command.localDateValueOfParameterNamed(TRANSACTION_DATE);
        this.closedOnDate = writtenOffOnLocalDate;
        this.writtenOffOnDate = writtenOffOnLocalDate;
        this.closedBy = currentUser;
        final LoanStatus statusEnum = loanLifecycleStateMachine.dryTransition(LoanEvent.WRITE_OFF_OUTSTANDING, this);

        LoanTransaction loanTransaction = null;
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
            loanLifecycleStateMachine.transition(LoanEvent.WRITE_OFF_OUTSTANDING, this);
            changes.put(PARAM_STATUS, LoanEnumerations.status(this.loanStatus));

            existingTransactionIds.addAll(findExistingTransactionIds());
            existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

            final String txnExternalId = command.stringValueOfParameterNamedAllowingNull(EXTERNAL_ID);

            ExternalId externalId = ExternalIdFactory.produce(txnExternalId);

            if (externalId.isEmpty() && TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
                externalId = ExternalId.generate();
            }

            changes.put(CLOSED_ON_DATE, command.stringValueOfParameterNamed(TRANSACTION_DATE));
            changes.put(WRITTEN_OFF_ON_DATE, command.stringValueOfParameterNamed(TRANSACTION_DATE));
            changes.put("externalId", externalId);

            if (DateUtils.isBefore(writtenOffOnLocalDate, getDisbursementDate())) {
                final String errorMessage = "The date on which a loan is written off cannot be before the loan disbursement date: "
                        + getDisbursementDate().toString();
                throw new InvalidLoanStateTransitionException("writeoff", "cannot.be.before.submittal.date", errorMessage,
                        writtenOffOnLocalDate, getDisbursementDate());
            }

            validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.WRITE_OFF_OUTSTANDING, writtenOffOnLocalDate);

            if (DateUtils.isDateInTheFuture(writtenOffOnLocalDate)) {
                final String errorMessage = "The date on which a loan is written off cannot be in the future.";
                throw new InvalidLoanStateTransitionException("writeoff", "cannot.be.a.future.date", errorMessage, writtenOffOnLocalDate);
            }

            loanTransaction = LoanTransaction.writeoff(this, getOffice(), writtenOffOnLocalDate, externalId);
            LocalDate lastTransactionDate = getLastUserTransactionDate();
            if (DateUtils.isAfter(lastTransactionDate, writtenOffOnLocalDate)) {
                final String errorMessage = "The date of the writeoff transaction must occur on or before previous transactions.";
                throw new InvalidLoanStateTransitionException("writeoff", "must.occur.on.or.after.other.transaction.dates", errorMessage,
                        writtenOffOnLocalDate);
            }

            addLoanTransaction(loanTransaction);
            loanRepaymentScheduleTransactionProcessor.processLatestTransaction(loanTransaction, new TransactionCtx(loanCurrency(),
                    getRepaymentScheduleInstallments(), getActiveCharges(), new MoneyHolder(getTotalOverpaidAsMoney())));

            updateLoanSummaryDerivedFields();
        }
        if (changedTransactionDetail == null) {
            changedTransactionDetail = new ChangedTransactionDetail();
        }
        changedTransactionDetail.getNewTransactionMappings().put(0L, loanTransaction);
        return changedTransactionDetail;
    }

    private ChangedTransactionDetail closeDisbursements(final ScheduleGeneratorDTO scheduleGeneratorDTO,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor) {
        ChangedTransactionDetail changedTransactionDetail = null;
        if (isDisbursementAllowed() && atLeastOnceDisbursed()) {
            this.loanRepaymentScheduleDetail.setPrincipal(getDisbursedAmount());
            removeDisbursementDetail();
            regenerateRepaymentSchedule(scheduleGeneratorDTO);
            if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO);
            }
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsForReprocessing();
            changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(getDisbursementDate(),
                    allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(), getActiveCharges());
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                mapEntry.getValue().updateLoan(this);
                addLoanTransaction(mapEntry.getValue());
            }
            updateLoanSummaryDerivedFields();
            LocalDate lastLoanTransactionDate = getLatestTransactionDate();
            doPostLoanTransactionChecks(lastLoanTransactionDate, loanLifecycleStateMachine);
        }
        return changedTransactionDetail;
    }

    public LocalDate getLatestTransactionDate() {
        LoanTransaction oneOfTheLatestTxn = this.loanTransactions.stream() //
                .filter(loanTransaction -> !loanTransaction.isReversed()) //
                .max(Comparator.comparing(LoanTransaction::getTransactionDate)) //
                .orElse(null);
        return oneOfTheLatestTxn != null ? oneOfTheLatestTxn.getTransactionDate() : null;
    }

    public ChangedTransactionDetail close(final JsonCommand command, final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final Map<String, Object> changes, final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds,
            final ScheduleGeneratorDTO scheduleGeneratorDTO) {

        validateAccountStatus(LoanEvent.LOAN_CLOSED);

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        final LocalDate closureDate = command.localDateValueOfParameterNamed(TRANSACTION_DATE);
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull(EXTERNAL_ID);

        ExternalId externalId = ExternalIdFactory.produce(txnExternalId);
        if (externalId.isEmpty() && TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }

        this.closedOnDate = closureDate;
        changes.put(CLOSED_ON_DATE, command.stringValueOfParameterNamed(TRANSACTION_DATE));

        validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.REPAID_IN_FULL, closureDate);
        if (DateUtils.isBefore(closureDate, getDisbursementDate())) {
            final String errorMessage = "The date on which a loan is closed cannot be before the loan disbursement date: "
                    + getDisbursementDate().toString();
            throw new InvalidLoanStateTransitionException("close", "cannot.be.before.submittal.date", errorMessage, closureDate,
                    getDisbursementDate());
        }

        if (DateUtils.isDateInTheFuture(closureDate)) {
            final String errorMessage = "The date on which a loan is closed cannot be in the future.";
            throw new InvalidLoanStateTransitionException("close", "cannot.be.a.future.date", errorMessage, closureDate);
        }
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
        ChangedTransactionDetail changedTransactionDetail = closeDisbursements(scheduleGeneratorDTO,
                loanRepaymentScheduleTransactionProcessor);

        LoanTransaction loanTransaction = null;
        if (isOpen()) {
            final Money totalOutstanding = this.summary.getTotalOutstanding(loanCurrency());
            if (totalOutstanding.isGreaterThanZero() && getInArrearsTolerance().isGreaterThanOrEqualTo(totalOutstanding)) {

                this.closedOnDate = closureDate;
                final LoanStatus statusEnum = loanLifecycleStateMachine.dryTransition(LoanEvent.REPAID_IN_FULL, this);
                if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
                    loanLifecycleStateMachine.transition(LoanEvent.REPAID_IN_FULL, this);
                    changes.put(PARAM_STATUS, LoanEnumerations.status(this.loanStatus));
                }
                changes.put("externalId", externalId);
                loanTransaction = LoanTransaction.writeoff(this, getOffice(), closureDate, externalId);
                final boolean isLastTransaction = isChronologicallyLatestTransaction(loanTransaction, getLoanTransactions());
                if (!isLastTransaction) {
                    final String errorMessage = "The closing date of the loan must be on or after latest transaction date.";
                    throw new InvalidLoanStateTransitionException("close.loan", "must.occur.on.or.after.latest.transaction.date",
                            errorMessage, closureDate);
                }

                addLoanTransaction(loanTransaction);
                loanRepaymentScheduleTransactionProcessor.processLatestTransaction(loanTransaction, new TransactionCtx(loanCurrency(),
                        getRepaymentScheduleInstallments(), getActiveCharges(), new MoneyHolder(getTotalOverpaidAsMoney())));

                updateLoanSummaryDerivedFields();
            } else if (totalOutstanding.isGreaterThanZero()) {
                final String errorMessage = "A loan with money outstanding cannot be closed";
                throw new InvalidLoanStateTransitionException("close", "loan.has.money.outstanding", errorMessage,
                        totalOutstanding.toString());
            }
        }

        if (isOverPaid()) {
            final Money totalLoanOverpayment = calculateTotalOverpayment();
            if (totalLoanOverpayment.isGreaterThanZero() && getInArrearsTolerance().isGreaterThanOrEqualTo(totalLoanOverpayment)) {
                // TODO - KW - technically should set somewhere that this loan
                // has 'overpaid' amount
                this.closedOnDate = closureDate;
                final LoanStatus statusEnum = loanLifecycleStateMachine.dryTransition(LoanEvent.REPAID_IN_FULL, this);
                if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
                    loanLifecycleStateMachine.transition(LoanEvent.REPAID_IN_FULL, this);
                    changes.put(PARAM_STATUS, LoanEnumerations.status(this.loanStatus));
                }
            } else if (totalLoanOverpayment.isGreaterThanZero()) {
                final String errorMessage = "The loan is marked as 'Overpaid' and cannot be moved to 'Closed (obligations met).";
                throw new InvalidLoanStateTransitionException("close", "loan.is.overpaid", errorMessage, totalLoanOverpayment.toString());
            }
        }

        if (changedTransactionDetail == null) {
            changedTransactionDetail = new ChangedTransactionDetail();
        }
        changedTransactionDetail.getNewTransactionMappings().put(0L, loanTransaction);
        return changedTransactionDetail;
    }

    /**
     * Behaviour added to comply with capability of previous mifos product to support easier transition to fineract
     * platform.
     */
    public void closeAsMarkedForReschedule(final JsonCommand command, final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final Map<String, Object> changes) {
        final LocalDate rescheduledOn = command.localDateValueOfParameterNamed(TRANSACTION_DATE);

        this.closedOnDate = rescheduledOn;
        final LoanStatus statusEnum = loanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_RESCHEDULE, this);
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
            loanLifecycleStateMachine.transition(LoanEvent.LOAN_RESCHEDULE, this);
            changes.put(PARAM_STATUS, LoanEnumerations.status(this.loanStatus));
        }

        this.rescheduledOnDate = rescheduledOn;
        changes.put(CLOSED_ON_DATE, command.stringValueOfParameterNamed(TRANSACTION_DATE));
        changes.put("rescheduledOnDate", command.stringValueOfParameterNamed(TRANSACTION_DATE));

        if (DateUtils.isBefore(this.rescheduledOnDate, getDisbursementDate())) {
            final String errorMessage = "The date on which a loan is rescheduled cannot be before the loan disbursement date: "
                    + getDisbursementDate().toString();
            throw new InvalidLoanStateTransitionException("close.reschedule", "cannot.be.before.submittal.date", errorMessage,
                    this.rescheduledOnDate, getDisbursementDate());
        }

        if (DateUtils.isDateInTheFuture(this.rescheduledOnDate)) {
            final String errorMessage = "The date on which a loan is rescheduled cannot be in the future.";
            throw new InvalidLoanStateTransitionException("close.reschedule", "cannot.be.a.future.date", errorMessage,
                    this.rescheduledOnDate);
        }
    }

    public boolean isNotSubmittedAndPendingApproval() {
        return !isSubmittedAndPendingApproval();
    }

    public LoanStatus getStatus() {
        return LoanStatus.fromInt(this.loanStatus);
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

    private boolean isClosedWithOutstandingAmountMarkedForReschedule() {
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
        LoanStatus actualLoanStatus = LoanStatus.fromInt(this.loanStatus);
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

    private Money getTotalPaidInRepayments() {
        Money cumulativePaid = Money.zero(loanCurrency());

        for (final LoanTransaction repayment : this.loanTransactions) {
            if (repayment.isRepaymentLikeType() && !repayment.isReversed()) {
                cumulativePaid = cumulativePaid.plus(repayment.getAmount(loanCurrency()));
            }
        }

        return cumulativePaid;
    }

    public Money getTotalRecoveredPayments() {
        Money cumulativePaid = Money.zero(loanCurrency());

        for (final LoanTransaction recoveredPayment : this.loanTransactions) {
            if (recoveredPayment.isRecoveryRepayment()) {
                cumulativePaid = cumulativePaid.plus(recoveredPayment.getAmount(loanCurrency()));
            }
        }
        return cumulativePaid;
    }

    public Money getTotalPrincipalOutstandingUntil(LocalDate date) {
        return getRepaymentScheduleInstallments().stream()
                .filter(installment -> installment.getDueDate().isBefore(date) || installment.getDueDate().isEqual(date))
                .map(installment -> installment.getPrincipalOutstanding(loanCurrency())).reduce(Money.zero(loanCurrency()), Money::add);

    }

    private Money getTotalInterestOutstandingOnLoan() {
        Money cumulativeInterest = Money.zero(loanCurrency());

        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment scheduledRepayment : installments) {
            cumulativeInterest = cumulativeInterest.plus(scheduledRepayment.getInterestOutstanding(loanCurrency()));
        }

        return cumulativeInterest;
    }

    @SuppressWarnings("unused")
    private Money getTotalInterestOverdueOnLoan() {
        Money cumulativeInterestOverdue = Money.zero(this.loanRepaymentScheduleDetail.getPrincipal().getCurrency());
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment scheduledRepayment : installments) {

            final Money interestOutstandingForPeriod = scheduledRepayment.getInterestOutstanding(loanCurrency());
            if (scheduledRepayment.isOverdueOn(DateUtils.getBusinessLocalDate())) {
                cumulativeInterestOverdue = cumulativeInterestOverdue.plus(interestOutstandingForPeriod);
            }
        }

        return cumulativeInterestOverdue;
    }

    private Money getInArrearsTolerance() {
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

    public void reassignLoanOfficer(final Staff newLoanOfficer, final LocalDate assignmentDate) {
        final LoanOfficerAssignmentHistory latestHistoryRecord = findLatestIncompleteHistoryRecord();
        final LoanOfficerAssignmentHistory lastAssignmentRecord = findLastAssignmentHistoryRecord(newLoanOfficer);

        // assignment date should not be less than loan submitted date
        if (isSubmittedOnDateAfter(assignmentDate)) {
            final String errorMessage = "The Loan Officer assignment date (" + assignmentDate.toString()
                    + ") cannot be before loan submitted date (" + getSubmittedOnDate().toString() + ").";
            throw new LoanOfficerAssignmentDateException("cannot.be.before.loan.submittal.date", errorMessage, assignmentDate,
                    getSubmittedOnDate());
        } else if (lastAssignmentRecord != null && lastAssignmentRecord.isEndDateAfter(assignmentDate)) {
            final String errorMessage = "The Loan Officer assignment date (" + assignmentDate
                    + ") cannot be before previous Loan Officer unassigned date (" + lastAssignmentRecord.getEndDate() + ").";
            throw new LoanOfficerAssignmentDateException("cannot.be.before.previous.unassignement.date", errorMessage, assignmentDate,
                    lastAssignmentRecord.getEndDate());
        } else if (DateUtils.isDateInTheFuture(assignmentDate)) {
            final String errorMessage = "The Loan Officer assignment date (" + assignmentDate + ") cannot be in the future.";
            throw new LoanOfficerAssignmentDateException("cannot.be.a.future.date", errorMessage, assignmentDate);
        } else if (latestHistoryRecord != null && this.loanOfficer.identifiedBy(newLoanOfficer)) {
            latestHistoryRecord.updateStartDate(assignmentDate);
        } else if (latestHistoryRecord != null && latestHistoryRecord.matchesStartDateOf(assignmentDate)) {
            latestHistoryRecord.updateLoanOfficer(newLoanOfficer);
            this.loanOfficer = newLoanOfficer;
        } else if (latestHistoryRecord != null && latestHistoryRecord.isBeforeStartDate(assignmentDate)) {
            final String errorMessage = "Loan with identifier " + getId() + " was already assigned before date " + assignmentDate;
            throw new LoanOfficerAssignmentDateException("is.before.last.assignment.date", errorMessage, getId(), assignmentDate);
        } else {
            if (latestHistoryRecord != null) {
                // loan officer correctly changed from previous loan officer to new loan officer
                latestHistoryRecord.updateEndDate(assignmentDate);
            }

            this.loanOfficer = newLoanOfficer;
            if (isNotSubmittedAndPendingApproval()) {
                final LoanOfficerAssignmentHistory loanOfficerAssignmentHistory = LoanOfficerAssignmentHistory.createNew(this,
                        this.loanOfficer, assignmentDate);
                this.loanOfficerHistory.add(loanOfficerAssignmentHistory);
            }
        }
    }

    public void removeLoanOfficer(final LocalDate unassignDate) {
        final LoanOfficerAssignmentHistory latestHistoryRecord = findLatestIncompleteHistoryRecord();

        if (latestHistoryRecord != null) {
            validateUnassignDate(latestHistoryRecord, unassignDate);
            latestHistoryRecord.updateEndDate(unassignDate);
        }

        this.loanOfficer = null;
    }

    private void validateUnassignDate(final LoanOfficerAssignmentHistory latestHistoryRecord, final LocalDate unassignDate) {
        if (DateUtils.isAfter(latestHistoryRecord.getStartDate(), unassignDate)) {
            final String errorMessage = "The Loan officer Unassign date(" + unassignDate + ") cannot be before its assignment date ("
                    + latestHistoryRecord.getStartDate() + ").";
            throw new LoanOfficerUnassignmentDateException("cannot.be.before.assignment.date", errorMessage, getId(),
                    getLoanOfficer().getId(), latestHistoryRecord.getStartDate(), unassignDate);
        } else if (DateUtils.isDateInTheFuture(unassignDate)) {
            final String errorMessage = "The Loan Officer Unassign date (" + unassignDate + ") cannot be in the future.";
            throw new LoanOfficerUnassignmentDateException("cannot.be.a.future.date", errorMessage, unassignDate);
        }
    }

    private LoanOfficerAssignmentHistory findLatestIncompleteHistoryRecord() {
        LoanOfficerAssignmentHistory latestRecordWithNoEndDate = null;
        for (final LoanOfficerAssignmentHistory historyRecord : this.loanOfficerHistory) {
            if (historyRecord.isCurrentRecord()) {
                latestRecordWithNoEndDate = historyRecord;
                break;
            }
        }
        return latestRecordWithNoEndDate;
    }

    private LoanOfficerAssignmentHistory findLastAssignmentHistoryRecord(final Staff newLoanOfficer) {
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
                } else if (transaction.isRepaymentLikeType() || transaction.isInterestWaiver()) {
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

    public void setHelpers(final LoanLifecycleStateMachine loanLifecycleStateMachine, final LoanSummaryWrapper loanSummaryWrapper,
            final LoanRepaymentScheduleTransactionProcessorFactory transactionProcessorFactory) {
        this.loanLifecycleStateMachine = loanLifecycleStateMachine;
        this.loanSummaryWrapper = loanSummaryWrapper;
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

    public void validateRepaymentDateIsOnNonWorkingDay(final LocalDate repaymentDate, final WorkingDays workingDays,
            final boolean allowTransactionsOnNonWorkingDay) {
        if (!allowTransactionsOnNonWorkingDay && !WorkingDaysUtil.isWorkingDay(workingDays, repaymentDate)) {
            final String errorMessage = "Repayment date cannot be on a non working day";
            throw new LoanApplicationDateException("repayment.date.on.non.working.day", errorMessage, repaymentDate);
        }
    }

    public void validateRepaymentDateIsOnHoliday(final LocalDate repaymentDate, final boolean allowTransactionsOnHoliday,
            final List<Holiday> holidays) {
        if (!allowTransactionsOnHoliday && HolidayUtil.isHoliday(repaymentDate, holidays)) {
            final String errorMessage = "Repayment date cannot be on a holiday";
            throw new LoanApplicationDateException("repayment.date.on.holiday", errorMessage, repaymentDate);
        }
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

    public void removeLoanTransaction(final LoanTransaction loanTransaction) {
        this.loanTransactions.remove(loanTransaction);
    }

    // Intentionally kept as package-private. Nobody should set the status directly but use the
    // DefaultLoanLifecycleStateMachine to transition
    void setLoanStatus(final Integer loanStatus) {
        this.loanStatus = loanStatus;
    }

    private void validateActivityNotBeforeClientOrGroupTransferDate(final LoanEvent event, final LocalDate activityDate) {
        if (this.client != null && this.client.getOfficeJoiningDate() != null) {
            final LocalDate clientOfficeJoiningDate = this.client.getOfficeJoiningDate();
            if (DateUtils.isBefore(activityDate, clientOfficeJoiningDate)) {
                String errorMessage = null;
                String action = null;
                String postfix = null;
                switch (event) {
                    case LOAN_APPROVED -> {
                        errorMessage = "The date on which a loan is approved cannot be earlier than client's transfer date to this office";
                        action = "approval";
                        postfix = "cannot.be.before.client.transfer.date";
                    }
                    case LOAN_APPROVAL_UNDO -> {
                        errorMessage = "The date on which a loan is approved cannot be earlier than client's transfer date to this office";
                        action = "approval";
                        postfix = "cannot.be.undone.before.client.transfer.date";
                    }
                    case LOAN_DISBURSED -> {
                        errorMessage = "The date on which a loan is disbursed cannot be earlier than client's transfer date to this office";
                        action = "disbursal";
                        postfix = "cannot.be.before.client.transfer.date";
                    }
                    case LOAN_DISBURSAL_UNDO -> {
                        errorMessage = "Cannot undo a disbursal done in another branch";
                        action = "disbursal";
                        postfix = "cannot.be.undone.before.client.transfer.date";
                    }
                    case LOAN_REPAYMENT_OR_WAIVER -> {
                        errorMessage = "The date on which a repayment or waiver is made cannot be earlier than client's transfer date to this office";
                        action = "repayment.or.waiver";
                        postfix = "cannot.be.made.before.client.transfer.date";
                    }
                    case WRITE_OFF_OUTSTANDING -> {
                        errorMessage = "The date on which a write off is made cannot be earlier than client's transfer date to this office";
                        action = "writeoff";
                        postfix = "cannot.be.undone.before.client.transfer.date";
                    }
                    case REPAID_IN_FULL -> {
                        errorMessage = "The date on which the loan is repaid in full cannot be earlier than client's transfer date to this office";
                        action = "close";
                        postfix = "cannot.be.undone.before.client.transfer.date";
                    }
                    case LOAN_CHARGE_PAYMENT -> {
                        errorMessage = "The date on which a charge payment is made cannot be earlier than client's transfer date to this office";
                        action = "charge.payment";
                        postfix = "cannot.be.made.before.client.transfer.date";
                    }
                    case LOAN_REFUND -> {
                        errorMessage = "The date on which a refund is made cannot be earlier than client's transfer date to this office";
                        action = "refund";
                        postfix = "cannot.be.made.before.client.transfer.date";
                    }
                    case LOAN_DISBURSAL_UNDO_LAST -> {
                        errorMessage = "Cannot undo a last disbursal in another branch";
                        action = "disbursal";
                        postfix = "cannot.be.undone.before.client.transfer.date";
                    }
                    default -> {
                    }
                }
                throw new InvalidLoanStateTransitionException(action, postfix, errorMessage, clientOfficeJoiningDate);
            }
        }
    }

    private void validateActivityNotBeforeLastTransactionDate(final LoanEvent event, final LocalDate activityDate) {
        if (!(this.repaymentScheduleDetail().isInterestRecalculationEnabled() || this.loanProduct().isHoldGuaranteeFunds())) {
            return;
        }
        LocalDate lastTransactionDate = getLastUserTransactionDate();
        if (DateUtils.isAfter(lastTransactionDate, activityDate)) {
            String errorMessage = null;
            String action = null;
            String postfix = null;
            switch (event) {
                case LOAN_REPAYMENT_OR_WAIVER -> {
                    errorMessage = "The date on which a repayment or waiver is made cannot be earlier than last transaction date";
                    action = "repayment.or.waiver";
                    postfix = "cannot.be.made.before.last.transaction.date";
                }
                case WRITE_OFF_OUTSTANDING -> {
                    errorMessage = "The date on which a write off is made cannot be earlier than last transaction date";
                    action = "writeoff";
                    postfix = "cannot.be.made.before.last.transaction.date";
                }
                case LOAN_CHARGE_PAYMENT -> {
                    errorMessage = "The date on which a charge payment is made cannot be earlier than last transaction date";
                    action = "charge.payment";
                    postfix = "cannot.be.made.before.last.transaction.date";
                }
                default -> {
                }
            }
            throw new InvalidLoanStateTransitionException(action, postfix, errorMessage, lastTransactionDate);
        }
    }

    public void validateRepaymentTypeTransactionNotBeforeAChargeRefund(final LoanTransaction repaymentTransaction,
            final String reversedOrCreated) {
        if (repaymentTransaction.isRepaymentLikeType() && !repaymentTransaction.isChargeRefund()) {
            for (LoanTransaction txn : this.getLoanTransactions()) {
                if (txn.isChargeRefund() && DateUtils.isBefore(repaymentTransaction.getTransactionDate(), txn.getTransactionDate())) {
                    final String errorMessage = "loan.transaction.cant.be." + reversedOrCreated + ".because.later.charge.refund.exists";
                    final String details = "Loan Transaction: " + this.getId() + " Can't be " + reversedOrCreated
                            + " because a Later Charge Refund Exists.";
                    throw new LoanChargeRefundException(errorMessage, details);
                }
            }
        }
    }

    public LocalDate getLastUserTransactionDate() {
        LocalDate currentTransactionDate = getDisbursementDate();
        for (final LoanTransaction previousTransaction : this.loanTransactions) {
            if (!(previousTransaction.isReversed() || previousTransaction.isAccrual() || previousTransaction.isIncomePosting()
                    || previousTransaction.isAccrualActivity())
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

    public void validateAccountStatus(final LoanEvent event) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        switch (event) {
            case LOAN_APPROVED -> {
                if (!isSubmittedAndPendingApproval()) {
                    final String defaultUserMessage = "Loan Account Approval is not allowed. Loan Account is not in submitted and pending approval state.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.approve.account.is.not.submitted.and.pending.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_APPROVAL_UNDO -> {
                if (!isApproved()) {
                    final String defaultUserMessage = "Loan Account Undo Approval is not allowed. Loan Account is not in approved state.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.undo.approval.account.is.not.approved",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_DISBURSED -> {
                if ((!(isApproved() && isNotDisbursed()) && !this.loanProduct.isMultiDisburseLoan())
                        || (this.loanProduct.isMultiDisburseLoan() && !isAllTranchesNotDisbursed())) {
                    final String defaultUserMessage = "Loan Disbursal is not allowed. Loan Account is not in approved and not disbursed state.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.disbursal.account.is.not.approve.not.disbursed.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_DISBURSAL_UNDO -> {
                if (!isOpen()) {
                    final String defaultUserMessage = "Loan Undo disbursal is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.undo.disbursal.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                if (isOpen() && this.isTopup()) {
                    final String defaultUserMessage = "Loan Undo disbursal is not allowed on Topup Loans";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.undo.disbursal.not.allowed.on.topup.loan", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_REPAYMENT_OR_WAIVER -> {
                if (!isOpen()) {
                    final String defaultUserMessage = "Loan Repayment (or its types) or Waiver is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.repayment.or.waiver.account.is.not.active", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case WRITE_OFF_OUTSTANDING -> {
                if (!isOpen()) {
                    final String defaultUserMessage = "Loan Written off is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.writtenoff.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case WRITE_OFF_OUTSTANDING_UNDO -> {
                if (!isClosedWrittenOff()) {
                    final String defaultUserMessage = "Loan Undo Written off is not allowed. Loan Account is not Written off.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.undo.writtenoff.account.is.not.written.off", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_CHARGE_PAYMENT -> {
                if (!isOpen()) {
                    final String defaultUserMessage = "Charge payment is not allowed. Loan Account is not Active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.charge.payment.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_CLOSED -> {
                if (!isOpen()) {
                    final String defaultUserMessage = "Closing Loan Account is not allowed. Loan Account is not Active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.close.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_EDIT_MULTI_DISBURSE_DATE -> {
                if (isClosed()) {
                    final String defaultUserMessage = "Edit disbursement is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.edit.disbursement.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_RECOVERY_PAYMENT -> {
                if (!isClosedWrittenOff()) {
                    final String defaultUserMessage = "Recovery repayments may only be made on loans which are written off";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.account.is.not.written.off",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_REFUND -> {
                if (!isOpen()) {
                    final String defaultUserMessage = "Loan Refund is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.refund.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_DISBURSAL_UNDO_LAST -> {
                if (!isOpen()) {
                    final String defaultUserMessage = "Loan Undo last disbursal is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.undo.last.disbursal.account.is.not.active", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_FORECLOSURE -> {
                if (!isOpen()) {
                    final String defaultUserMessage = "Loan foreclosure is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.foreclosure.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_CREDIT_BALANCE_REFUND -> {
                if (!getStatus().isOverpaid()) {
                    final String defaultUserMessage = "Loan Credit Balance Refund is not allowed. Loan Account is not Overpaid.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.credit.balance.refund.account.is.not.overpaid", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_CHARGE_ADJUSTMENT -> {
                if (!(getStatus().isActive() || getStatus().isClosedObligationsMet() || getStatus().isOverpaid())) {
                    final String defaultUserMessage = "Loan Charge Adjustment is not allowed. Loan Account must be either Active, Fully repaid or Overpaid.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.charge.adjustment.account.is.not.in.valid.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            default -> {
            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

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

    public ChangedTransactionDetail updateDisbursementDateAndAmountForTranche(final LoanDisbursementDetails disbursementDetails,
            final JsonCommand command, final Map<String, Object> actualChanges, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        final Locale locale = command.extractLocale();
        validateAccountStatus(LoanEvent.LOAN_EDIT_MULTI_DISBURSE_DATE);
        final BigDecimal principal = command.bigDecimalValueOfParameterNamed(LoanApiConstants.updatedDisbursementPrincipalParameterName,
                locale);
        final LocalDate expectedDisbursementDate = command
                .localDateValueOfParameterNamed(LoanApiConstants.updatedDisbursementDateParameterName);
        disbursementDetails.updateExpectedDisbursementDateAndAmount(expectedDisbursementDate, principal);
        actualChanges.put(LoanApiConstants.expectedDisbursementDateParameterName,
                command.stringValueOfParameterNamed(LoanApiConstants.expectedDisbursementDateParameterName));
        actualChanges.put(LoanApiConstants.disbursementIdParameterName,
                command.stringValueOfParameterNamed(LoanApiConstants.disbursementIdParameterName));
        actualChanges.put(LoanApiConstants.disbursementPrincipalParameterName,
                command.bigDecimalValueOfParameterNamed(LoanApiConstants.disbursementPrincipalParameterName, locale));

        this.loanRepaymentScheduleDetail.setPrincipal(getPrincipalAmountForRepaymentSchedule());

        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO);
        } else {
            regenerateRepaymentSchedule(scheduleGeneratorDTO);
        }

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsForReprocessing();
        ChangedTransactionDetail changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(
                getDisbursementDate(), allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(),
                getActiveCharges());
        for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
            mapEntry.getValue().updateLoan(this);
            addLoanTransaction(mapEntry.getValue());
        }

        return changedTransactionDetail;
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

    public LoanRepaymentScheduleInstallment fetchRepaymentScheduleInstallment(final Integer installmentNumber) {
        LoanRepaymentScheduleInstallment installment = null;
        if (installmentNumber == null) {
            return installment;
        }
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment scheduleInstallment : installments) {
            if (scheduleInstallment.getInstallmentNumber().equals(installmentNumber)) {
                installment = scheduleInstallment;
                break;
            }
        }
        return installment;
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

    public LocalDate getMaturityDate() {
        return this.actualMaturityDate;
    }

    public ChangedTransactionDetail recalculateScheduleFromLastTransaction(final ScheduleGeneratorDTO generatorDTO,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());
        /*
         * LocalDate recalculateFrom = null; List<LoanTransaction> loanTransactions =
         * this.retrieveListOfTransactionsPostDisbursementExcludeAccruals(); for (LoanTransaction loanTransaction :
         * loanTransactions) { if (recalculateFrom == null ||
         * loanTransaction.getTransactionDate().isAfter(recalculateFrom)) { recalculateFrom =
         * loanTransaction.getTransactionDate(); } } generatorDTO.setRecalculateFrom(recalculateFrom);
         */
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            regenerateRepaymentScheduleWithInterestRecalculation(generatorDTO);
        } else {
            regenerateRepaymentSchedule(generatorDTO);
        }
        return processTransactions();

    }

    public ChangedTransactionDetail recalculateScheduleFromLastTransaction(final ScheduleGeneratorDTO generatorDTO) {
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            regenerateRepaymentScheduleWithInterestRecalculation(generatorDTO);
        } else {
            regenerateRepaymentSchedule(generatorDTO);
        }
        return processTransactions();

    }

    public ChangedTransactionDetail handleRegenerateRepaymentScheduleWithInterestRecalculation(final ScheduleGeneratorDTO generatorDTO) {
        regenerateRepaymentScheduleWithInterestRecalculation(generatorDTO);
        return processTransactions();

    }

    public ChangedTransactionDetail processTransactions() {
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
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

    public void regenerateRepaymentScheduleWithInterestRecalculation(final ScheduleGeneratorDTO generatorDTO) {
        LocalDate lastTransactionDate = getLastUserTransactionDate();
        final LoanScheduleDTO loanSchedule = getRecalculatedSchedule(generatorDTO);
        if (loanSchedule == null) {
            return;
        }
        // Either the installments got recalculated or the model
        if (loanSchedule.getInstallments() != null) {
            updateLoanSchedule(loanSchedule.getInstallments());
        } else {
            updateLoanSchedule(loanSchedule.getLoanScheduleModel());
        }
        this.interestRecalculatedOn = DateUtils.getBusinessLocalDate();
        LocalDate lastRepaymentDate = this.getLastRepaymentPeriodDueDate(true);
        Set<LoanCharge> charges = this.getActiveCharges();
        for (final LoanCharge loanCharge : charges) {
            if (!loanCharge.isDueAtDisbursement()) {
                updateOverdueScheduleInstallment(loanCharge);
                if (loanCharge.getDueLocalDate() == null || !DateUtils.isBefore(lastRepaymentDate, loanCharge.getDueLocalDate())) {
                    if ((loanCharge.isInstalmentFee() || !loanCharge.isWaived()) && (loanCharge.getDueLocalDate() == null
                            || !DateUtils.isAfter(lastTransactionDate, loanCharge.getDueLocalDate()))) {
                        recalculateLoanCharge(loanCharge, generatorDTO.getPenaltyWaitPeriod());
                        loanCharge.updateWaivedAmount(getCurrency());
                    }
                } else {
                    loanCharge.setActive(false);
                }
            }
        }

        processPostDisbursementTransactions();
    }

    public void processPostDisbursementTransactions() {
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
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

    private LoanScheduleDTO getRecalculatedSchedule(final ScheduleGeneratorDTO generatorDTO) {
        if (!this.repaymentScheduleDetail().isEnableDownPayment()
                && (!this.repaymentScheduleDetail().isInterestRecalculationEnabled() || isNpa || isChargedOff())) {
            return null;
        }
        final InterestMethod interestMethod = this.loanRepaymentScheduleDetail.getInterestMethod();
        final LoanScheduleGenerator loanScheduleGenerator = generatorDTO.getLoanScheduleFactory()
                .create(this.loanRepaymentScheduleDetail.getLoanScheduleType(), interestMethod);

        final MathContext mc = MoneyHelper.getMathContext();

        final LoanApplicationTerms loanApplicationTerms = constructLoanApplicationTerms(generatorDTO);

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);

        return loanScheduleGenerator.rescheduleNextInstallments(mc, loanApplicationTerms, this, generatorDTO.getHolidayDetailDTO(),
                loanRepaymentScheduleTransactionProcessor, generatorDTO.getRecalculateFrom());
    }

    public LoanRepaymentScheduleInstallment fetchPrepaymentDetail(final ScheduleGeneratorDTO scheduleGeneratorDTO, final LocalDate onDate) {
        LoanRepaymentScheduleInstallment installment;

        if (this.loanRepaymentScheduleDetail.isInterestRecalculationEnabled()) {
            final MathContext mc = MoneyHelper.getMathContext();

            final InterestMethod interestMethod = this.loanRepaymentScheduleDetail.getInterestMethod();
            final LoanApplicationTerms loanApplicationTerms = constructLoanApplicationTerms(scheduleGeneratorDTO);

            final LoanScheduleGenerator loanScheduleGenerator = scheduleGeneratorDTO.getLoanScheduleFactory()
                    .create(loanApplicationTerms.getLoanScheduleType(), interestMethod);
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                    .determineProcessor(this.transactionProcessingStrategyCode);
            installment = loanScheduleGenerator.calculatePrepaymentAmount(getCurrency(), onDate, loanApplicationTerms, mc, this,
                    scheduleGeneratorDTO.getHolidayDetailDTO(), loanRepaymentScheduleTransactionProcessor);
        } else {
            installment = this.getTotalOutstandingOnLoan();
        }
        return installment;
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

        return LoanApplicationTerms.assembleFrom(scheduleGeneratorDTO.getApplicationCurrency(), loanTermFrequency,
                loanTermPeriodFrequencyType, nthDayType, dayOfWeekType, getDisbursementDate(), getExpectedFirstRepaymentOnDate(),
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

    private LoanRepaymentScheduleInstallment getTotalOutstandingOnLoan() {
        Money feeCharges = Money.zero(loanCurrency());
        Money penaltyCharges = Money.zero(loanCurrency());
        Money totalPrincipal = Money.zero(loanCurrency());
        Money totalInterest = Money.zero(loanCurrency());
        final Set<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails = null;
        List<LoanRepaymentScheduleInstallment> repaymentSchedule = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment scheduledRepayment : repaymentSchedule) {
            totalPrincipal = totalPrincipal.plus(scheduledRepayment.getPrincipalOutstanding(loanCurrency()));
            totalInterest = totalInterest.plus(scheduledRepayment.getInterestOutstanding(loanCurrency()));
            feeCharges = feeCharges.plus(scheduledRepayment.getFeeChargesOutstanding(loanCurrency()));
            penaltyCharges = penaltyCharges.plus(scheduledRepayment.getPenaltyChargesOutstanding(loanCurrency()));
        }
        LocalDate businessDate = DateUtils.getBusinessLocalDate();
        return new LoanRepaymentScheduleInstallment(null, 0, businessDate, businessDate, totalPrincipal.getAmount(),
                totalInterest.getAmount(), feeCharges.getAmount(), penaltyCharges.getAmount(), false, compoundingDetails);
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
                if (!loanTransaction.getOverPaymentPortion(getCurrency()).isZero()) {
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
     * @param dueDate
     *            the due date of the installment
     * @return a schedule installment with similar due date to the one provided
     **/
    public LoanRepaymentScheduleInstallment getRepaymentScheduleInstallment(LocalDate dueDate) {
        LoanRepaymentScheduleInstallment installment = null;

        if (dueDate != null) {
            List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
            for (LoanRepaymentScheduleInstallment repaymentScheduleInstallment : installments) {
                if (DateUtils.isEqual(dueDate, repaymentScheduleInstallment.getDueDate())) {
                    installment = repaymentScheduleInstallment;
                    break;
                }
            }
        }
        return installment;
    }

    /**
     * @return loan disbursement data
     **/
    public List<DisbursementData> getDisbursmentData() {
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

        final List<DisbursementData> disbursementData = getDisbursmentData();

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

        return LoanApplicationTerms.assembleFrom(applicationCurrency, loanTermFrequency, loanTermPeriodFrequencyType, nthDayType,
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

    public Collection<LoanCharge> getLoanCharges(LocalDate dueDate) {
        Collection<LoanCharge> loanCharges = new ArrayList<>();

        for (LoanCharge loanCharge : charges) {

            if (loanCharge.getDueLocalDate() != null && loanCharge.getDueLocalDate().equals(dueDate)) {
                loanCharges.add(loanCharge);
            }
        }

        return loanCharges;
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

    public void creditBalanceRefund(LoanTransaction newCreditBalanceRefundTransaction,
            LoanLifecycleStateMachine defaultLoanLifecycleStateMachine, List<Long> existingTransactionIds,
            List<Long> existingReversedTransactionIds) {
        validateAccountStatus(LoanEvent.LOAN_CREDIT_BALANCE_REFUND);

        validateRefundDateIsAfterLastRepayment(newCreditBalanceRefundTransaction.getTransactionDate());

        if (!newCreditBalanceRefundTransaction.isGreaterThanZeroAndLessThanOrEqualTo(this.totalOverpaid)) {
            final String errorMessage = "Transaction Amount ("
                    + newCreditBalanceRefundTransaction.getAmount(getCurrency()).getAmount().toString()
                    + ") must be > zero and <= Overpaid amount (" + this.totalOverpaid.toString() + ").";
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg.transactionAmount.invalid.must.be.>zero.and<=overpaidamount", errorMessage, "transactionAmount",
                    newCreditBalanceRefundTransaction.getAmount(getCurrency()));
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        this.loanTransactions.add(newCreditBalanceRefundTransaction);

        updateLoanSummaryDerivedFields();

        if (this.totalOverpaid == null || BigDecimal.ZERO.compareTo(this.totalOverpaid) == 0) {
            this.overpaidOnDate = null;
            this.closedOnDate = newCreditBalanceRefundTransaction.getTransactionDate();
            defaultLoanLifecycleStateMachine.transition(LoanEvent.LOAN_CREDIT_BALANCE_REFUND, this);
        }

    }

    public ChangedTransactionDetail makeRefundForActiveLoan(final LoanTransaction loanTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, final boolean allowTransactionsOnHoliday, final List<Holiday> holidays,
            final WorkingDays workingDays, final boolean allowTransactionsOnNonWorkingDay) {
        validateAccountStatus(LoanEvent.LOAN_REFUND);
        validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_REFUND, loanTransaction.getTransactionDate());

        validateRefundDateIsAfterLastRepayment(loanTransaction.getTransactionDate());

        validateRepaymentDateIsOnHoliday(loanTransaction.getTransactionDate(), allowTransactionsOnHoliday, holidays);
        validateRepaymentDateIsOnNonWorkingDay(loanTransaction.getTransactionDate(), workingDays, allowTransactionsOnNonWorkingDay);

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        return handleRefundTransaction(loanTransaction, loanLifecycleStateMachine, null);

    }

    private void validateRefundDateIsAfterLastRepayment(final LocalDate refundTransactionDate) {
        final LocalDate possibleNextRefundDate = possibleNextRefundDate();

        if (possibleNextRefundDate == null || DateUtils.isBefore(refundTransactionDate, possibleNextRefundDate)) {
            throw new InvalidRefundDateException(refundTransactionDate.toString());
        }
    }

    private ChangedTransactionDetail handleRefundTransaction(final LoanTransaction loanTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final LoanTransaction adjustedTransaction) {
        ChangedTransactionDetail changedTransactionDetail = null;

        loanLifecycleStateMachine.transition(LoanEvent.LOAN_REFUND, this);

        loanTransaction.updateLoan(this);

        if (getStatus().isOverpaid() || getStatus().isClosed()) {
            final String errorMessage = "This refund option is only for active loans ";
            throw new InvalidLoanStateTransitionException("transaction", "is.exceeding.overpaid.amount", errorMessage, this.totalOverpaid,
                    loanTransaction.getAmount(getCurrency()).getAmount());
        } else if (this.getTotalPaidInRepayments().isZero()) {
            final String errorMessage = "Cannot refund when no payment has been made";
            throw new InvalidLoanStateTransitionException("transaction", "no.payment.yet.made.for.loan", errorMessage);
        }

        if (loanTransaction.isNotZero(loanCurrency())) {
            addLoanTransaction(loanTransaction);
        }
        if (loanTransaction.isNotRefundForActiveLoan()) {
            final String errorMessage = "A transaction of type refund was expected but not received.";
            throw new InvalidLoanTransactionTypeException("transaction", "is.not.a.refund.transaction", errorMessage);
        }

        final LocalDate loanTransactionDate = extractTransactionDate(loanTransaction);

        if (DateUtils.isDateInTheFuture(loanTransactionDate)) {
            final String errorMessage = "The transaction date cannot be in the future.";
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.a.future.date", errorMessage, loanTransactionDate);
        }

        if (this.loanProduct.isMultiDisburseLoan() && adjustedTransaction == null) {
            BigDecimal totalDisbursed = getDisbursedAmount();
            BigDecimal totalPrincipalAdjusted = this.summary.getTotalPrincipalAdjustments();
            BigDecimal totalPrincipalCredited = totalDisbursed.add(totalPrincipalAdjusted);
            if (totalPrincipalCredited.compareTo(this.summary.getTotalPrincipalRepaid()) < 0) {
                final String errorMessage = "The transaction amount cannot exceed threshold.";
                throw new InvalidLoanStateTransitionException("transaction", "amount.exceeds.threshold", errorMessage);
            }
        }

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);

        // If it's a refund
        if (adjustedTransaction == null) {
            loanRepaymentScheduleTransactionProcessor.processLatestTransaction(loanTransaction, new TransactionCtx(getCurrency(),
                    getRepaymentScheduleInstallments(), getActiveCharges(), new MoneyHolder(getTotalOverpaidAsMoney())));
        } else {
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsForReprocessing();
            changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(getDisbursementDate(),
                    allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(), getActiveCharges());
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                mapEntry.getValue().updateLoan(this);
            }

        }

        updateLoanSummaryDerivedFields();

        doPostLoanTransactionChecks(loanTransaction.getTransactionDate(), loanLifecycleStateMachine);

        return changedTransactionDetail;
    }

    public void handleChargebackTransaction(final LoanTransaction chargebackTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine) {
        if (!chargebackTransaction.isChargeback()) {
            final String errorMessage = "A transaction of type chargeback was expected but not received.";
            throw new InvalidLoanTransactionTypeException("transaction", "is.not.a.chargeback.transaction", errorMessage);
        }

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);

        addLoanTransaction(chargebackTransaction);
        loanRepaymentScheduleTransactionProcessor.processLatestTransaction(chargebackTransaction, new TransactionCtx(getCurrency(),
                getRepaymentScheduleInstallments(), getActiveCharges(), new MoneyHolder(getTotalOverpaidAsMoney())));

        updateLoanSummaryDerivedFields();
        if (!doPostLoanTransactionChecks(chargebackTransaction.getTransactionDate(), loanLifecycleStateMachine)) {
            loanLifecycleStateMachine.transition(LoanEvent.LOAN_CHARGEBACK, this);
        }
    }

    public LocalDate possibleNextRefundDate() {
        final LocalDate now = DateUtils.getBusinessLocalDate();

        LocalDate lastTransactionDate = null;
        for (final LoanTransaction transaction : this.loanTransactions) {
            if ((transaction.isRepaymentLikeType() || transaction.isRefundForActiveLoan() || transaction.isCreditBalanceRefund())
                    && transaction.isNonZero() && transaction.isNotReversed()) {
                lastTransactionDate = transaction.getTransactionDate();
            }
        }

        return lastTransactionDate == null ? now : lastTransactionDate;
    }

    private LocalDate getActualDisbursementDate(final LoanCharge loanCharge) {
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

    public Map<String, Object> undoLastDisbursal(ScheduleGeneratorDTO scheduleGeneratorDTO, List<Long> existingTransactionIds,
            List<Long> existingReversedTransactionIds, Loan loan) {
        validateAccountStatus(LoanEvent.LOAN_DISBURSAL_UNDO_LAST);
        validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_DISBURSAL_UNDO_LAST, getDisbursementDate());

        final Map<String, Object> actualChanges = new LinkedHashMap<>();
        List<LoanTransaction> loanTransactions = retrieveListOfTransactionsByType(LoanTransactionType.DISBURSEMENT);
        loanTransactions.sort(Comparator.comparing(LoanTransaction::getId));
        final LoanTransaction lastDisbursalTransaction = loanTransactions.get(loanTransactions.size() - 1);
        final LocalDate lastTransactionDate = lastDisbursalTransaction.getTransactionDate();

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        loanTransactions = retrieveListOfTransactionsExcludeAccruals();
        Collections.reverse(loanTransactions);
        for (final LoanTransaction previousTransaction : loanTransactions) {
            if (DateUtils.isBefore(lastTransactionDate, previousTransaction.getTransactionDate())
                    && (previousTransaction.isRepaymentLikeType() || previousTransaction.isWaiver()
                            || previousTransaction.isChargePayment())) {
                throw new UndoLastTrancheDisbursementException(previousTransaction.getId());
            }
            if (previousTransaction.getId().compareTo(lastDisbursalTransaction.getId()) < 0) {
                break;
            }
        }
        final LoanDisbursementDetails disbursementDetail = loan.getDisbursementDetails(lastTransactionDate,
                lastDisbursalTransaction.getAmount());
        updateLoanToLastDisbursalState(disbursementDetail);
        this.loanTermVariations.removeIf(loanTermVariations -> (loanTermVariations.getTermType().isDueDateVariation()
                && DateUtils.isAfter(loanTermVariations.fetchDateValue(), lastTransactionDate))
                || (loanTermVariations.getTermType().isEMIAmountVariation()
                        && DateUtils.isEqual(loanTermVariations.getTermApplicableFrom(), lastTransactionDate))
                || DateUtils.isAfter(loanTermVariations.getTermApplicableFrom(), lastTransactionDate));
        reverseExistingTransactionsTillLastDisbursal(lastDisbursalTransaction);
        loan.recalculateScheduleFromLastTransaction(scheduleGeneratorDTO);
        actualChanges.put("undolastdisbursal", "true");
        actualChanges.put("disbursedAmount", this.getDisbursedAmount());
        updateLoanSummaryDerivedFields();

        doPostLoanTransactionChecks(getLastUserTransactionDate(), loanLifecycleStateMachine);

        return actualChanges;
    }

    /**
     * Reverse only disbursement, accruals, and repayments at disbursal transactions
     */
    public void reverseExistingTransactionsTillLastDisbursal(LoanTransaction lastDisbursalTransaction) {
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (!DateUtils.isBefore(transaction.getTransactionDate(), lastDisbursalTransaction.getTransactionDate())
                    && transaction.getId().compareTo(lastDisbursalTransaction.getId()) >= 0
                    && transaction.isAllowTypeTransactionAtTheTimeOfLastUndo()) {
                transaction.reverse();
            }
        }
        if (isAutoRepaymentForDownPaymentEnabled()) {
            // identify down-payment amount for the transaction
            BigDecimal disbursedAmountPercentageForDownPayment = this.loanRepaymentScheduleDetail
                    .getDisbursedAmountPercentageForDownPayment();
            Money downPaymentMoney = Money.of(getCurrency(),
                    MathUtil.percentageOf(lastDisbursalTransaction.getAmount(), disbursedAmountPercentageForDownPayment, 19));

            // find the latest matching down-payment transaction based on date, amount and transaction type
            Optional<LoanTransaction> downPaymentTransaction = this.loanTransactions.stream()
                    .filter(tr -> tr.getTransactionDate().equals(lastDisbursalTransaction.getTransactionDate())
                            && tr.getTypeOf().isDownPayment() && tr.getAmount().compareTo(downPaymentMoney.getAmount()) == 0)
                    .max(Comparator.comparing(LoanTransaction::getId));

            // reverse the down-payment transaction
            downPaymentTransaction.ifPresent(LoanTransaction::reverse);
        }
    }

    private void updateLoanToLastDisbursalState(LoanDisbursementDetails disbursementDetail) {
        for (final LoanCharge charge : getActiveCharges()) {
            if (charge.isOverdueInstallmentCharge()) {
                charge.setActive(false);
            } else if (charge.isTrancheDisbursementCharge() && disbursementDetail.getDisbursementDate()
                    .equals(charge.getTrancheDisbursementCharge().getloanDisbursementDetails().actualDisbursementDate())) {
                charge.resetToOriginal(loanCurrency());
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
        int totalPeriodDays = Math.toIntExact(ChronoUnit.DAYS.between(installment.getFromDate(), installment.getDueDate()));
        int tillDays = Math.toIntExact(ChronoUnit.DAYS.between(installment.getFromDate(), paymentDate));
        interestForCurrentPeriod = Money.of(getCurrency(), BigDecimal
                .valueOf(calculateInterestForDays(totalPeriodDays, installment.getInterestCharged(getCurrency()).getAmount(), tillDays)));
        interestAccountedForCurrentPeriod = installment.getInterestWaived(getCurrency()).plus(installment.getInterestPaid(getCurrency()));
        for (LoanCharge loanCharge : this.charges) {
            if (loanCharge.isActive() && !loanCharge.isDueAtDisbursement()) {
                boolean isDue = isFirstNormalInstallment
                        ? loanCharge.isDueForCollectionFromIncludingAndUpToAndIncluding(installment.getFromDate(), paymentDate)
                        : loanCharge.isDueForCollectionFromAndUpToAndIncluding(installment.getFromDate(), paymentDate);
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
            } else if (DateUtils.isAfter(paymentDate, installment.getFromDate())
                    && DateUtils.isBefore(paymentDate, installment.getDueDate())) {
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

    public ChangedTransactionDetail handleForeClosureTransactions(final LoanTransaction repaymentTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        LoanEvent event = LoanEvent.LOAN_FORECLOSURE;
        validateAccountStatus(event);
        validateForForeclosure(repaymentTransaction.getTransactionDate());
        this.loanSubStatus = LoanSubStatus.FORECLOSED.getValue();
        return handleRepaymentOrRecoveryOrWaiverTransaction(repaymentTransaction, loanLifecycleStateMachine, null, scheduleGeneratorDTO);
    }

    public void validateForForeclosure(final LocalDate transactionDate) {
        if (isInterestRecalculationEnabledForProduct()) {
            final String defaultUserMessage = "The loan with interest recalculation enabled cannot be foreclosed.";
            throw new LoanForeclosureException("loan.with.interest.recalculation.enabled.cannot.be.foreclosured", defaultUserMessage,
                    getId());
        }

        LocalDate lastUserTransactionDate = getLastUserTransactionDate();

        if (DateUtils.isDateInTheFuture(transactionDate)) {
            final String defaultUserMessage = "The transactionDate cannot be in the future.";
            throw new LoanForeclosureException("loan.foreclosure.transaction.date.is.in.future", defaultUserMessage, transactionDate);
        }

        if (DateUtils.isBefore(transactionDate, lastUserTransactionDate)) {
            final String defaultUserMessage = "The transactionDate cannot be earlier than the last transaction date.";
            throw new LoanForeclosureException("loan.foreclosure.transaction.date.cannot.before.the.last.transaction.date",
                    defaultUserMessage, transactionDate);
        }
    }

    public void updateInstallmentsPostDate(LocalDate transactionDate) {
        List<LoanRepaymentScheduleInstallment> newInstallments = new ArrayList<>(this.repaymentScheduleInstallments);
        final MonetaryCurrency currency = getCurrency();
        Money totalPrincipal = Money.zero(currency);
        Money[] balances = retriveIncomeForOverlappingPeriod(transactionDate);
        boolean isInterestComponent = true;
        for (final LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
            if (!DateUtils.isAfter(transactionDate, installment.getDueDate())) {
                totalPrincipal = totalPrincipal.plus(installment.getPrincipal(currency));
                newInstallments.remove(installment);
                if (DateUtils.isEqual(transactionDate, installment.getDueDate())) {
                    isInterestComponent = false;
                }
            }

        }

        for (LoanDisbursementDetails loanDisbursementDetails : getDisbursementDetails()) {
            if (loanDisbursementDetails.actualDisbursementDate() == null) {
                totalPrincipal = Money.of(currency, totalPrincipal.getAmount().subtract(loanDisbursementDetails.principal()));
            }
        }

        LocalDate installmentStartDate = getDisbursementDate();

        if (!newInstallments.isEmpty()) {
            installmentStartDate = newInstallments.get(newInstallments.size() - 1).getDueDate();
        }

        int installmentNumber = newInstallments.size();

        if (!isInterestComponent) {
            installmentNumber++;
        }

        LoanRepaymentScheduleInstallment newInstallment = new LoanRepaymentScheduleInstallment(null, newInstallments.size() + 1,
                installmentStartDate, transactionDate, totalPrincipal.getAmount(), balances[0].getAmount(), balances[1].getAmount(),
                balances[2].getAmount(), isInterestComponent, null);
        newInstallment.updateInstallmentNumber(newInstallments.size() + 1);
        newInstallments.add(newInstallment);
        updateLoanScheduleOnForeclosure(newInstallments);

        Set<LoanCharge> charges = this.getActiveCharges();
        int penaltyWaitPeriod = 0;
        for (LoanCharge loanCharge : charges) {
            if (DateUtils.isAfter(loanCharge.getDueLocalDate(), transactionDate)) {
                loanCharge.setActive(false);
            } else if (loanCharge.getDueLocalDate() == null) {
                recalculateLoanCharge(loanCharge, penaltyWaitPeriod);
                loanCharge.updateWaivedAmount(currency);
            }
        }

        for (LoanTransaction loanTransaction : getLoanTransactions()) {
            if (loanTransaction.isChargesWaiver()) {
                for (LoanChargePaidBy chargePaidBy : loanTransaction.getLoanChargesPaid()) {
                    if ((chargePaidBy.getLoanCharge().isDueDateCharge()
                            && DateUtils.isBefore(transactionDate, chargePaidBy.getLoanCharge().getDueLocalDate()))
                            || (chargePaidBy.getLoanCharge().isInstalmentFee() && chargePaidBy.getInstallmentNumber() != null
                                    && chargePaidBy.getInstallmentNumber() > installmentNumber)) {
                        loanTransaction.reverse();
                    }
                }

            }
        }
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

    public Set<LoanTermVariations> getActiveLoanTermVariations() {
        Set<LoanTermVariations> retData = new HashSet<>();
        if (this.loanTermVariations != null && !this.loanTermVariations.isEmpty()) {
            for (LoanTermVariations loanTermVariations : this.loanTermVariations) {
                if (loanTermVariations.isActive()) {
                    retData.add(loanTermVariations);
                }
            }
        }
        return !retData.isEmpty() ? retData : null;
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
            List<DisbursementData> disbursementData = getDisbursmentData();
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

    public LoanTransaction findChargedOffTransaction() {
        return getLoanTransactions().stream() //
                .filter(LoanTransaction::isNotReversed) //
                .filter(LoanTransaction::isChargeOff) //
                .findFirst() //
                .orElse(null);
    }

    public void handleMaturityDateActivate() {
        if (this.expectedMaturityDate != null && !this.expectedMaturityDate.equals(this.actualMaturityDate)) {
            this.actualMaturityDate = this.expectedMaturityDate;
        }
    }

    public LoanTransaction getLastUserTransaction() {
        return getLoanTransactions().stream() //
                .filter(LoanTransaction::isNotReversed) //
                .filter(t -> !(t.isAccrualTransaction() || t.isIncomePosting())) //
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
}
