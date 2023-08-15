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
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.apache.fineract.portfolio.loanaccount.command.LoanChargeCommand;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.LoanCollateralManagementData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.exception.ExceedingTrancheCountException;
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
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.rate.domain.Rate;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecks;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Table(name = "m_loan", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_no" }, name = "loan_account_no_UNIQUE"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "loan_externalid_UNIQUE") })
public class Loan extends AbstractAuditableWithUTCDateTimeCustom {

    public static final String RECALCULATE_LOAN_SCHEDULE = "recalculateLoanSchedule";
    public static final String ACCOUNT_NO = "accountNo";
    public static final String IN_ARREARS_TOLERANCE = "inArrearsTolerance";
    public static final String CREATE_STANDING_INSTRUCTION_AT_DISBURSEMENT = "createStandingInstructionAtDisbursement";
    public static final String EXTERNAL_ID = "externalId";
    public static final String CLIENT_ID = "clientId";
    public static final String GROUP_ID = "groupId";
    public static final String PRODUCT_ID = "productId";
    public static final String IS_FLOATING_INTEREST_RATE = "isFloatingInterestRate";
    public static final String INTEREST_RATE_DIFFERENTIAL = "interestRateDifferential";
    public static final String FUND_ID = "fundId";
    public static final String LOAN_OFFICER_ID = "loanOfficerId";
    public static final String LOAN_PURPOSE_ID = "loanPurposeId";
    public static final String TRANSACTION_PROCESSING_STRATEGY_CODE = "transactionProcessingStrategyCode";
    public static final String SUBMITTED_ON_DATE = "submittedOnDate";
    public static final String DATE_FORMAT = "dateFormat";
    public static final String LOCALE = "locale";
    public static final String EXPECTED_DISBURSEMENT_DATE = "expectedDisbursementDate";
    public static final String REPAYMENTS_STARTING_FROM_DATE = "repaymentsStartingFromDate";
    public static final String SYNC_DISBURSEMENT_WITH_MEETING = "syncDisbursementWithMeeting";
    public static final String INTEREST_CHARGED_FROM_DATE = "interestChargedFromDate";
    public static final String PARAM_CHARGES = "charges";
    public static final String PARAM_COLLATERAL = "collateral";
    public static final String LOAN_TERM_FREQUENCY = "loanTermFrequency";
    public static final String LOAN_TERM_FREQUENCY_TYPE = "loanTermFrequencyType";
    public static final String PRINCIPAL = "principal";
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
    /** Disable optimistic locking till batch jobs failures can be fixed **/
    @Version
    int version;

    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "external_id")
    private ExternalId externalId;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = true)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = true)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "glim_id", nullable = true)
    private GroupLoanIndividualMonitoringAccount glim;

    @Column(name = "loan_type_enum", nullable = false)
    private Integer loanType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "fund_id", nullable = true)
    private Fund fund;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loan_officer_id", nullable = true)
    private Staff loanOfficer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loanpurpose_cv_id", nullable = true)
    private CodeValue loanPurpose;

    @Column(name = "loan_transaction_strategy_code", nullable = false)
    private String transactionProcessingStrategyCode;

    @Column(name = "loan_transaction_strategy_name")
    private String transactionProcessingStrategyName;

    @Embedded
    private LoanProductRelatedDetail loanRepaymentScheduleDetail;

    @Column(name = "term_frequency", nullable = false)
    private Integer termFrequency;

    @Column(name = "term_period_frequency_enum", nullable = false)
    private Integer termPeriodFrequencyType;

    @Column(name = "loan_status_id", nullable = false)
    private Integer loanStatus;

    @Column(name = "sync_disbursement_with_meeting", nullable = true)
    private Boolean syncDisbursementWithMeeting;

    // loan application states
    @Column(name = "submittedon_date")
    private LocalDate submittedOnDate;
    @Column(name = "rejectedon_date")
    private LocalDate rejectedOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "rejectedon_userid", nullable = true)
    private AppUser rejectedBy;

    @Column(name = "withdrawnon_date")
    private LocalDate withdrawnOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "withdrawnon_userid", nullable = true)
    private AppUser withdrawnBy;

    @Column(name = "approvedon_date")
    private LocalDate approvedOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "approvedon_userid", nullable = true)
    private AppUser approvedBy;

    @Column(name = "expected_disbursedon_date")
    private LocalDate expectedDisbursementDate;

    @Column(name = "disbursedon_date")
    private LocalDate actualDisbursementDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursedon_userid", nullable = true)
    private AppUser disbursedBy;

    @Column(name = "closedon_date")
    private LocalDate closedOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "closedon_userid", nullable = true)
    private AppUser closedBy;

    @Column(name = "writtenoffon_date")
    private LocalDate writtenOffOnDate;

    @Column(name = "rescheduledon_date")
    private LocalDate rescheduledOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "rescheduledon_userid", nullable = true)
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
    private boolean accountNumberRequiresAutoGeneration = false;
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

    @Column(name = "fixed_emi_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal fixedEmiAmount;

    @Column(name = "max_outstanding_loan_balance", scale = 6, precision = 19, nullable = true)
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

    @Column(name = "create_standing_instruction_at_disbursement", nullable = true)
    private Boolean createStandingInstructionAtDisbursement;

    @Column(name = "guarantee_amount_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal guaranteeAmountDerived;

    @Column(name = "interest_recalcualated_on")
    private LocalDate interestRecalculatedOn;

    @Column(name = "is_floating_interest_rate", nullable = true)
    private Boolean isFloatingInterestRate;

    @Column(name = "interest_rate_differential", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestRateDifferential;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writeoff_reason_cv_id", nullable = true)
    private CodeValue writeOffReason;

    @Column(name = "loan_sub_status_id", nullable = true)
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

    @Column(name = "fixed_principal_percentage_per_installment", scale = 2, precision = 5, nullable = true)
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

    public static Loan newIndividualLoanApplication(final String accountNo, final Client client, final Integer loanType,
            final LoanProduct loanProduct, final Fund fund, final Staff officer, final CodeValue loanPurpose,
            final String transactionProcessingStrategyCode, final LoanProductRelatedDetail loanRepaymentScheduleDetail,
            final Set<LoanCharge> loanCharges, final Set<LoanCollateralManagement> collateral, final BigDecimal fixedEmiAmount,
            final List<LoanDisbursementDetails> disbursementDetails, final BigDecimal maxOutstandingLoanBalance,
            final Boolean createStandingInstructionAtDisbursement, final Boolean isFloatingInterestRate,
            final BigDecimal interestRateDifferential, final List<Rate> rates, final BigDecimal fixedPrincipalPercentagePerInstallment) {
        return new Loan(accountNo, client, null, loanType, fund, officer, loanPurpose, transactionProcessingStrategyCode, loanProduct,
                loanRepaymentScheduleDetail, null, loanCharges, collateral, null, fixedEmiAmount, disbursementDetails,
                maxOutstandingLoanBalance, createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential, rates,
                fixedPrincipalPercentagePerInstallment);
    }

    public static Loan newGroupLoanApplication(final String accountNo, final Group group, final Integer loanType,
            final LoanProduct loanProduct, final Fund fund, final Staff officer, final CodeValue loanPurpose,
            final String transactionProcessingStrategyCode, final LoanProductRelatedDetail loanRepaymentScheduleDetail,
            final Set<LoanCharge> loanCharges, final Set<LoanCollateralManagement> collateral, final Boolean syncDisbursementWithMeeting,
            final BigDecimal fixedEmiAmount, final List<LoanDisbursementDetails> disbursementDetails,
            final BigDecimal maxOutstandingLoanBalance, final Boolean createStandingInstructionAtDisbursement,
            final Boolean isFloatingInterestRate, final BigDecimal interestRateDifferential, final List<Rate> rates,
            final BigDecimal fixedPrincipalPercentagePerInstallment) {
        return new Loan(accountNo, null, group, loanType, fund, officer, loanPurpose, transactionProcessingStrategyCode, loanProduct,
                loanRepaymentScheduleDetail, null, loanCharges, collateral, syncDisbursementWithMeeting, fixedEmiAmount,
                disbursementDetails, maxOutstandingLoanBalance, createStandingInstructionAtDisbursement, isFloatingInterestRate,
                interestRateDifferential, rates, fixedPrincipalPercentagePerInstallment);
    }

    public static Loan newIndividualLoanApplicationFromGroup(final String accountNo, final Client client, final Group group,
            final Integer loanType, final LoanProduct loanProduct, final Fund fund, final Staff officer, final CodeValue loanPurpose,
            final String transactionProcessingStrategyCode, final LoanProductRelatedDetail loanRepaymentScheduleDetail,
            final Set<LoanCharge> loanCharges, final Set<LoanCollateralManagement> collateral, final Boolean syncDisbursementWithMeeting,
            final BigDecimal fixedEmiAmount, final List<LoanDisbursementDetails> disbursementDetails,
            final BigDecimal maxOutstandingLoanBalance, final Boolean createStandingInstructionAtDisbursement,
            final Boolean isFloatingInterestRate, final BigDecimal interestRateDifferential, final List<Rate> rates,
            final BigDecimal fixedPrincipalPercentagePerInstallment) {
        return new Loan(accountNo, client, group, loanType, fund, officer, loanPurpose, transactionProcessingStrategyCode, loanProduct,
                loanRepaymentScheduleDetail, null, loanCharges, collateral, syncDisbursementWithMeeting, fixedEmiAmount,
                disbursementDetails, maxOutstandingLoanBalance, createStandingInstructionAtDisbursement, isFloatingInterestRate,
                interestRateDifferential, rates, fixedPrincipalPercentagePerInstallment);
    }

    protected Loan() {
        this.client = null;
    }

    private Loan(final String accountNo, final Client client, final Group group, final Integer loanType, final Fund fund,
            final Staff loanOfficer, final CodeValue loanPurpose, final String transactionProcessingStrategyCode,
            final LoanProduct loanProduct, final LoanProductRelatedDetail loanRepaymentScheduleDetail, final LoanStatus loanStatus,
            final Set<LoanCharge> loanCharges, final Set<LoanCollateralManagement> collateral, final Boolean syncDisbursementWithMeeting,
            final BigDecimal fixedEmiAmount, final List<LoanDisbursementDetails> disbursementDetails,
            final BigDecimal maxOutstandingLoanBalance, final Boolean createStandingInstructionAtDisbursement,
            final Boolean isFloatingInterestRate, final BigDecimal interestRateDifferential, final List<Rate> rates,
            final BigDecimal fixedPrincipalPercentagePerInstallment) {

        this.loanRepaymentScheduleDetail = loanRepaymentScheduleDetail;
        this.loanRepaymentScheduleDetail.validateRepaymentPeriodWithGraceSettings();

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
        this.loanType = loanType;
        this.fund = fund;
        this.loanOfficer = loanOfficer;
        this.loanPurpose = loanPurpose;

        this.transactionProcessingStrategyCode = transactionProcessingStrategyCode;
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

        if (loanType.equals(1) && collateral != null && !collateral.isEmpty()) {
            this.loanCollateralManagements = associateWithThisLoan(collateral);
        } else {
            this.loanCollateralManagements = null;
        }
        this.loanOfficerHistory = null;

        this.syncDisbursementWithMeeting = syncDisbursementWithMeeting;
        this.fixedEmiAmount = fixedEmiAmount;
        this.maxOutstandingLoanBalance = maxOutstandingLoanBalance;
        this.disbursementDetails = disbursementDetails;
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

    }

    public Integer getNumberOfRepayments() {
        return this.loanRepaymentScheduleDetail.getNumberOfRepayments();
    }

    private LoanSummary updateSummaryWithTotalFeeChargesDueAtDisbursement(final BigDecimal feeChargesDueAtDisbursement) {
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

    private BigDecimal deriveSumTotalOfChargesDueAtDisbursement() {

        Money chargesDue = Money.of(getCurrency(), BigDecimal.ZERO);

        for (final LoanCharge charge : getActiveCharges()) {
            if (charge.isDueAtDisbursement()) {
                chargesDue = chargesDue.plus(charge.amount());
            }
        }

        return chargesDue.getAmount();
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

    public boolean isAccountNumberRequiresAutoGeneration() {
        return this.accountNumberRequiresAutoGeneration;
    }

    public void setAccountNumberRequiresAutoGeneration(final boolean accountNumberRequiresAutoGeneration) {
        this.accountNumberRequiresAutoGeneration = accountNumberRequiresAutoGeneration;
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
        // reporcessing the repayment schedule.
        if (this.charges == null) {
            this.charges = new HashSet<>();
        }

        this.charges.add(loanCharge);

        this.summary = updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());

        // store Id's of existing loan transactions and existing reversed loan
        // transactions
        final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
        wrapper.reprocess(getCurrency(), getDisbursementDate(), getRepaymentScheduleInstallments(), getActiveCharges());
        updateLoanSummaryDerivedFields();

        loanLifecycleStateMachine.transition(LoanEvent.LOAN_CHARGE_ADDED, this);
    }

    public ChangedTransactionDetail reprocessTransactions() {
        ChangedTransactionDetail changedTransactionDetail = null;
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsPostDisbursement();
        changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(),
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
     *
     * @param loanCharge
     * @param suppliedTransactionDate
     * @return
     */
    public LoanTransaction handleChargeAppliedTransaction(final LoanCharge loanCharge, final LocalDate suppliedTransactionDate) {
        final Money chargeAmount = loanCharge.getAmount(getCurrency());
        Money feeCharges = chargeAmount;
        Money penaltyCharges = Money.zero(loanCurrency());
        if (loanCharge.isPenaltyCharge()) {
            penaltyCharges = chargeAmount;
            feeCharges = Money.zero(loanCurrency());
        }

        LocalDate transactionDate = null;

        if (suppliedTransactionDate != null) {
            transactionDate = suppliedTransactionDate;
        } else {
            transactionDate = loanCharge.getDueLocalDate();
            final LocalDate currentDate = DateUtils.getBusinessLocalDate();

            // if loan charge is to be applied on a future date, the loan
            // transaction would show todays date as applied date
            if (transactionDate == null || currentDate.isBefore(transactionDate)) {
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
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            boolean isDue = installment.isFirstPeriod()
                    ? charge.isDueForCollectionFromIncludingAndUpToAndIncluding(installment.getFromDate(), installment.getDueDate())
                    : charge.isDueForCollectionFromAndUpToAndIncluding(installment.getFromDate(), installment.getDueDate());
            if (installmentNumber == null && isDue) {
                chargePaymentInstallments.add(installment);
                break;
            } else if (installmentNumber != null && installment.getInstallmentNumber().equals(installmentNumber)) {
                chargePaymentInstallments.add(installment);
                break;
            }
        }
        final Set<LoanCharge> loanCharges = new HashSet<>(1);
        loanCharges.add(charge);
        loanRepaymentScheduleTransactionProcessor.handleTransaction(chargesPayment, getCurrency(), chargePaymentInstallments, loanCharges);

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
        if (loanCharge.isSpecifiedDueDate() && loanCharge.getDueLocalDate().isBefore(disbursementDate)) {
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
                    && installment.getDueDate().isAfter(lastRepaymentDate)) {
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
            /****
             * TODO Vishwas Currently we do not allow removing a loan charge after a loan is approved (hence there is no
             * need to adjust any loan transactions).
             *
             * Consider removing this block of code or logically completing it for the future by getting the list of
             * affected Transactions
             ***/
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsPostDisbursement();
            loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(), allNonContraTransactionsPostDisbursement,
                    getCurrency(), getRepaymentScheduleInstallments(), getActiveCharges());
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
        for (LoanChargePaidBy loanChargePaidBy : loanChargePaidBys) {
            if (loanChargePaidBy.getLoanCharge().equals(loanCharge)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> updateLoanCharge(final LoanCharge loanCharge, final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(3);

        validateLoanIsNotClosed(loanCharge);
        if (getActiveCharges().contains(loanCharge)) {
            final BigDecimal amount = calculateAmountPercentageAppliedTo(loanCharge);
            final Map<String, Object> loanChargeChanges = loanCharge.update(command, amount);
            actualChanges.putAll(loanChargeChanges);
            updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
        }

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
        if (!loanCharge.isDueAtDisbursement()) {
            /****
             * TODO Vishwas Currently we do not allow waiving updating loan charge after a loan is approved (hence there
             * is no need to adjust any loan transactions).
             *
             * Consider removing this block of code or logically completing it for the future by getting the list of
             * affected Transactions
             ***/
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsPostDisbursement();
            loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(), allNonContraTransactionsPostDisbursement,
                    getCurrency(), getRepaymentScheduleInstallments(), getActiveCharges());
        } else {
            // reprocess loan schedule based on charge been waived.
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(getCurrency(), getDisbursementDate(), getRepaymentScheduleInstallments(), getActiveCharges());
        }

        updateLoanSummaryDerivedFields();

        return actualChanges;
    }

    /**
     * @param loanCharge
     * @return
     */
    private BigDecimal calculateAmountPercentageAppliedTo(final LoanCharge loanCharge) {
        BigDecimal amount = BigDecimal.ZERO;
        if (loanCharge.isOverdueInstallmentCharge()) {
            return loanCharge.getAmountPercentageAppliedTo();
        }
        switch (loanCharge.getChargeCalculation()) {
            case PERCENT_OF_AMOUNT:
                amount = getDerivedAmountForCharge(loanCharge);
            break;
            case PERCENT_OF_AMOUNT_AND_INTEREST:
                final BigDecimal totalInterestCharged = getTotalInterest();
                if (isMultiDisburmentLoan() && loanCharge.isDisbursementCharge()) {
                    amount = getTotalAllTrancheDisbursementAmount().getAmount().add(totalInterestCharged);
                } else {
                    amount = getPrincipal().getAmount().add(totalInterestCharged);
                }
            break;
            case PERCENT_OF_INTEREST:
                amount = getTotalInterest();
            break;
            case PERCENT_OF_DISBURSEMENT_AMOUNT:
                if (loanCharge.getTrancheDisbursementCharge() != null) {
                    amount = loanCharge.getTrancheDisbursementCharge().getloanDisbursementDetails().principal();
                } else {
                    amount = getPrincipal().getAmount();
                }
            break;
            default:
            break;
        }
        return amount;
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

    /**
     * @return
     */
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
        Money amount = Money.zero(getCurrency());
        Money percentOf = Money.zero(getCurrency());
        switch (calculationType) {
            case PERCENT_OF_AMOUNT:
                percentOf = installment.getPrincipal(getCurrency());
            break;
            case PERCENT_OF_AMOUNT_AND_INTEREST:
                percentOf = installment.getPrincipal(getCurrency()).plus(installment.getInterestCharged(getCurrency()));
            break;
            case PERCENT_OF_INTEREST:
                percentOf = installment.getInterestCharged(getCurrency());
            break;
            default:
            break;
        }
        amount = amount.plus(LoanCharge.percentageOf(percentOf.getAmount(), percentage));
        return amount;
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
        if (loanCharge.isDueDateCharge()) {
            if (loanCharge.getDueLocalDate().isAfter(DateUtils.getBusinessLocalDate())) {
                transactionDate = DateUtils.getBusinessLocalDate();
            } else {
                transactionDate = loanCharge.getDueLocalDate();
            }
        } else if (loanCharge.isInstalmentFee()) {
            LocalDate repaymentDueDate = loanCharge.getInstallmentLoanCharge(loanInstallmentNumber).getRepaymentInstallment().getDueDate();
            if (repaymentDueDate.isAfter(DateUtils.getBusinessLocalDate())) {
                transactionDate = DateUtils.getBusinessLocalDate();
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
                && (loanCharge.getDueLocalDate() == null || DateUtils.getBusinessLocalDate().isAfter(loanCharge.getDueLocalDate()))) {
            regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO);
        }
        // Waive of charges whose due date falls after latest 'repayment'
        // transaction dont require entire loan schedule to be reprocessed.
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
        if (!loanCharge.isDueAtDisbursement() && loanCharge.isPaidOrPartiallyPaid(loanCurrency())) {
            /****
             * TODO Vishwas Currently we do not allow waiving fully paid loan charge and waiving partially paid loan
             * charges only waives the remaining amount.
             *
             * Consider removing this block of code or logically completing it for the future by getting the list of
             * affected Transactions
             ***/
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsPostDisbursement();
            loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(), allNonContraTransactionsPostDisbursement,
                    getCurrency(), getRepaymentScheduleInstallments(), getActiveCharges());
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

    public GroupLoanIndividualMonitoringAccount getGlim() {
        return glim;
    }

    public void setGlim(GroupLoanIndividualMonitoringAccount glim) {
        this.glim = glim;
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

        /** Process new and updated charges **/
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

        /** Updated deleted charges **/
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
        applyAccurals();

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
        applyAccurals();

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

    /**
     * method updates accrual derived fields on installments and reverse the unprocessed transactions
     */
    private void applyAccurals() {
        Collection<LoanTransaction> accruals = retrieveListOfAccrualTransactions();
        if (!accruals.isEmpty()) {
            if (isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
                applyPeriodicAccruals(accruals);
            } else if (isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()) {
                updateAccrualsForNonPeriodicAccruals(accruals);
            }
        }
    }

    private void applyPeriodicAccruals(final Collection<LoanTransaction> accruals) {
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        boolean isBasedOnSubmittedOnDate = TemporaryConfigurationServiceContainer.getAccrualDateConfigForCharge()
                .equalsIgnoreCase("submitted-date");
        for (LoanRepaymentScheduleInstallment installment : installments) {
            Money interest = Money.zero(getCurrency());
            Money fee = Money.zero(getCurrency());
            Money penality = Money.zero(getCurrency());
            for (LoanTransaction loanTransaction : accruals) {
                LocalDate transactionDateForRange = isBasedOnSubmittedOnDate
                        ? loanTransaction.getLoanChargesPaid().stream().findFirst().get().getLoanCharge().getDueDate()
                        : loanTransaction.getTransactionDate();
                boolean isInRange = installment.isFirstPeriod()
                        ? !transactionDateForRange.isBefore(installment.getFromDate())
                                && !transactionDateForRange.isAfter(installment.getDueDate())
                        : transactionDateForRange.isAfter(installment.getFromDate())
                                && !transactionDateForRange.isAfter(installment.getDueDate());
                if (isInRange) {
                    interest = interest.plus(loanTransaction.getInterestPortion(getCurrency()));
                    fee = fee.plus(loanTransaction.getFeeChargesPortion(getCurrency()));
                    penality = penality.plus(loanTransaction.getPenaltyChargesPortion(getCurrency()));
                    if (installment.getFeeChargesCharged(getCurrency()).isLessThan(fee)
                            || installment.getInterestCharged(getCurrency()).isLessThan(interest)
                            || installment.getPenaltyChargesCharged(getCurrency()).isLessThan(penality)
                            || (isInterestBearing() && getAccruedTill().isEqual(loanTransaction.getTransactionDate())
                                    && !installment.getDueDate().isEqual(getAccruedTill()))) {
                        interest = interest.minus(loanTransaction.getInterestPortion(getCurrency()));
                        fee = fee.minus(loanTransaction.getFeeChargesPortion(getCurrency()));
                        penality = penality.minus(loanTransaction.getPenaltyChargesPortion(getCurrency()));
                        loanTransaction.reverse();
                    }

                }
            }
            installment.updateAccrualPortion(interest, fee, penality);
        }
        LoanRepaymentScheduleInstallment lastInstallment = getLastLoanRepaymentScheduleInstallment();
        for (LoanTransaction loanTransaction : accruals) {
            if (loanTransaction.getTransactionDate().isAfter(lastInstallment.getDueDate()) && !loanTransaction.isReversed()) {
                loanTransaction.reverse();
            }
        }
    }

    private void updateAccrualsForNonPeriodicAccruals(final Collection<LoanTransaction> accruals) {

        final Money interestApplied = Money.of(getCurrency(), this.summary.getTotalInterestCharged());
        ExternalId externalId = ExternalId.empty();
        boolean isExternalIdAutoGenerationEnabled = TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled();

        for (LoanTransaction loanTransaction : accruals) {
            if (loanTransaction.getInterestPortion(getCurrency()).isGreaterThanZero()) {
                if (loanTransaction.getInterestPortion(getCurrency()).isNotEqualTo(interestApplied)) {
                    loanTransaction.reverse();
                    if (isExternalIdAutoGenerationEnabled) {
                        externalId = ExternalId.generate();
                    }
                    final LoanTransaction interestAppliedTransaction = LoanTransaction.accrueInterest(getOffice(), this, interestApplied,
                            getDisbursementDate(), externalId);
                    addLoanTransaction(interestAppliedTransaction);
                }
            } else {
                Set<LoanChargePaidBy> chargePaidBies = loanTransaction.getLoanChargesPaid();
                for (final LoanChargePaidBy chargePaidBy : chargePaidBies) {
                    LoanCharge loanCharge = chargePaidBy.getLoanCharge();
                    Money chargeAmount = loanCharge.getAmount(getCurrency());
                    if (chargeAmount.isNotEqualTo(loanTransaction.getAmount(getCurrency()))) {
                        loanTransaction.reverse();
                        handleChargeAppliedTransaction(loanCharge, loanTransaction.getTransactionDate());
                    }

                }
            }
        }

    }

    public void updateLoanScheduleDependentDerivedFields() {
        if (this.getLoanRepaymentScheduleInstallmentsSize() > 0) {
            this.expectedMaturityDate = determineExpectedMaturityDate();
            this.actualMaturityDate = determineExpectedMaturityDate();
        }
    }

    private void updateLoanSummaryDerivedFields() {

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

    public Map<String, Object> loanApplicationModification(final JsonCommand command, final Set<LoanCharge> possiblyModifedLoanCharges,
            final Set<LoanCollateralManagement> possiblyModifiedLoanCollateralItems, final AprCalculator aprCalculator,
            boolean isChargesModified, final LoanProduct loanProduct) {

        final Map<String, Object> actualChanges = this.loanRepaymentScheduleDetail.updateLoanApplicationAttributes(command, aprCalculator);
        final MonetaryCurrency currency = new MonetaryCurrency(loanProduct.getCurrency().getCode(),
                loanProduct.getCurrency().getDigitsAfterDecimal(), loanProduct.getCurrency().getCurrencyInMultiplesOf());
        this.loanRepaymentScheduleDetail.updateCurrency(currency);

        if (!actualChanges.isEmpty()) {
            final boolean recalculateLoanSchedule = !(actualChanges.size() == 1 && actualChanges.containsKey(IN_ARREARS_TOLERANCE));
            actualChanges.put(RECALCULATE_LOAN_SCHEDULE, recalculateLoanSchedule);
            isChargesModified = true;
        }

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        final String accountNoParamName = ACCOUNT_NO;
        if (command.isChangeInStringParameterNamed(accountNoParamName, this.accountNumber)) {
            final String newValue = command.stringValueOfParameterNamed(accountNoParamName);
            actualChanges.put(accountNoParamName, newValue);
            this.accountNumber = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInBooleanParameterNamed(CREATE_STANDING_INSTRUCTION_AT_DISBURSEMENT,
                shouldCreateStandingInstructionAtDisbursement())) {
            final Boolean valueAsInput = command.booleanObjectValueOfParameterNamed(CREATE_STANDING_INSTRUCTION_AT_DISBURSEMENT);
            actualChanges.put(CREATE_STANDING_INSTRUCTION_AT_DISBURSEMENT, valueAsInput);
            this.createStandingInstructionAtDisbursement = valueAsInput;
        }

        if (command.isChangeInStringParameterNamed(EXTERNAL_ID, this.externalId.getValue())) {
            final String newValue = command.stringValueOfParameterNamed(EXTERNAL_ID);
            ExternalId externalId = ExternalIdFactory.produce(newValue);
            if (externalId.isEmpty() && TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
                externalId = ExternalId.generate();
            }
            actualChanges.put(EXTERNAL_ID, externalId);
            this.externalId = externalId;
        }

        // add clientId, groupId and loanType changes to actual changes

        final Long clientId = this.client == null ? null : this.client.getId();
        if (command.isChangeInLongParameterNamed(CLIENT_ID, clientId)) {
            final Long newValue = command.longValueOfParameterNamed(CLIENT_ID);
            actualChanges.put(CLIENT_ID, newValue);
        }

        // FIXME: AA - We may require separate api command to move loan from one
        // group to another
        final Long groupId = this.group == null ? null : this.group.getId();
        if (command.isChangeInLongParameterNamed(GROUP_ID, groupId)) {
            final Long newValue = command.longValueOfParameterNamed(GROUP_ID);
            actualChanges.put(GROUP_ID, newValue);
        }

        if (command.isChangeInLongParameterNamed(PRODUCT_ID, this.loanProduct.getId())) {
            final Long newValue = command.longValueOfParameterNamed(PRODUCT_ID);
            actualChanges.put(PRODUCT_ID, newValue);
            actualChanges.put(RECALCULATE_LOAN_SCHEDULE, true);
        }

        if (command.isChangeInBooleanParameterNamed(IS_FLOATING_INTEREST_RATE, this.isFloatingInterestRate)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(IS_FLOATING_INTEREST_RATE);
            actualChanges.put(IS_FLOATING_INTEREST_RATE, newValue);
            this.isFloatingInterestRate = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(INTEREST_RATE_DIFFERENTIAL, this.interestRateDifferential)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(INTEREST_RATE_DIFFERENTIAL);
            actualChanges.put(INTEREST_RATE_DIFFERENTIAL, newValue);
            this.interestRateDifferential = newValue;
        }

        Long existingFundId = null;
        if (this.fund != null) {
            existingFundId = this.fund.getId();
        }
        if (command.isChangeInLongParameterNamed(FUND_ID, existingFundId)) {
            final Long newValue = command.longValueOfParameterNamed(FUND_ID);
            actualChanges.put(FUND_ID, newValue);
        }

        Long existingLoanOfficerId = null;
        if (this.loanOfficer != null) {
            existingLoanOfficerId = this.loanOfficer.getId();
        }

        if (command.isChangeInLongParameterNamed(LOAN_OFFICER_ID, existingLoanOfficerId)) {
            final Long newValue = command.longValueOfParameterNamed(LOAN_OFFICER_ID);
            actualChanges.put(LOAN_OFFICER_ID, newValue);
        }

        Long existingLoanPurposeId = null;
        if (this.loanPurpose != null) {
            existingLoanPurposeId = this.loanPurpose.getId();
        }

        if (command.isChangeInLongParameterNamed(LOAN_PURPOSE_ID, existingLoanPurposeId)) {
            final Long newValue = command.longValueOfParameterNamed(LOAN_PURPOSE_ID);
            actualChanges.put(LOAN_PURPOSE_ID, newValue);
        }

        if (command.isChangeInStringParameterNamed(TRANSACTION_PROCESSING_STRATEGY_CODE, transactionProcessingStrategyCode)) {
            final String newValue = command.stringValueOfParameterNamed(TRANSACTION_PROCESSING_STRATEGY_CODE);
            actualChanges.put(TRANSACTION_PROCESSING_STRATEGY_CODE, newValue);
        }

        if (command.isChangeInLocalDateParameterNamed(SUBMITTED_ON_DATE, getSubmittedOnDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(SUBMITTED_ON_DATE);
            actualChanges.put(SUBMITTED_ON_DATE, valueAsInput);
            actualChanges.put(DATE_FORMAT, dateFormatAsInput);
            actualChanges.put(LOCALE, localeAsInput);

            this.submittedOnDate = command.localDateValueOfParameterNamed(SUBMITTED_ON_DATE);
        }

        if (command.isChangeInLocalDateParameterNamed(EXPECTED_DISBURSEMENT_DATE, getExpectedDisbursedOnLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(EXPECTED_DISBURSEMENT_DATE);
            actualChanges.put(EXPECTED_DISBURSEMENT_DATE, valueAsInput);
            actualChanges.put(DATE_FORMAT, dateFormatAsInput);
            actualChanges.put(LOCALE, localeAsInput);
            actualChanges.put(RECALCULATE_LOAN_SCHEDULE, true);

            this.expectedDisbursementDate = command.localDateValueOfParameterNamed(EXPECTED_DISBURSEMENT_DATE);
            removeFirstDisbursementTransaction();
        }

        if (command.isChangeInLocalDateParameterNamed(REPAYMENTS_STARTING_FROM_DATE, getExpectedFirstRepaymentOnDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(REPAYMENTS_STARTING_FROM_DATE);
            actualChanges.put(REPAYMENTS_STARTING_FROM_DATE, valueAsInput);
            actualChanges.put(DATE_FORMAT, dateFormatAsInput);
            actualChanges.put(LOCALE, localeAsInput);
            actualChanges.put(RECALCULATE_LOAN_SCHEDULE, true);

            this.expectedFirstRepaymentOnDate = command.localDateValueOfParameterNamed(REPAYMENTS_STARTING_FROM_DATE);
        }

        if (command.isChangeInBooleanParameterNamed(SYNC_DISBURSEMENT_WITH_MEETING, isSyncDisbursementWithMeeting())) {
            final Boolean valueAsInput = command.booleanObjectValueOfParameterNamed(SYNC_DISBURSEMENT_WITH_MEETING);
            actualChanges.put(SYNC_DISBURSEMENT_WITH_MEETING, valueAsInput);
            this.syncDisbursementWithMeeting = valueAsInput;
        }

        if (command.isChangeInLocalDateParameterNamed(INTEREST_CHARGED_FROM_DATE, getInterestChargedFromDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(INTEREST_CHARGED_FROM_DATE);
            actualChanges.put(INTEREST_CHARGED_FROM_DATE, valueAsInput);
            actualChanges.put(DATE_FORMAT, dateFormatAsInput);
            actualChanges.put(LOCALE, localeAsInput);
            actualChanges.put(RECALCULATE_LOAN_SCHEDULE, true);

            this.interestChargedFromDate = command.localDateValueOfParameterNamed(INTEREST_CHARGED_FROM_DATE);
        }

        // the comparison should be done with the tenant date
        // (DateUtils.getBusinessDate()) and not the server date (new
        // LocalDate())
        if (getSubmittedOnDate().isAfter(DateUtils.getBusinessLocalDate())) {
            final String errorMessage = "The date on which a loan is submitted cannot be in the future.";
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.a.future.date", errorMessage, getSubmittedOnDate());
        }

        if (this.client != null) {
            if (getSubmittedOnDate().isBefore(this.client.getActivationLocalDate())) {
                final String errorMessage = "The date on which a loan is submitted cannot be earlier than client's activation date.";
                throw new InvalidLoanStateTransitionException("submittal", "cannot.be.before.client.activation.date", errorMessage,
                        getSubmittedOnDate());
            }
        } else if (this.group != null && getSubmittedOnDate().isBefore(this.group.getActivationLocalDate())) {
            final String errorMessage = "The date on which a loan is submitted cannot be earlier than groups's activation date.";
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.before.group.activation.date", errorMessage,
                    getSubmittedOnDate());
        }

        if (getSubmittedOnDate().isAfter(getExpectedDisbursedOnLocalDate())) {
            final String errorMessage = "The date on which a loan is submitted cannot be after its expected disbursement date: "
                    + getExpectedDisbursedOnLocalDate().toString();
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.after.expected.disbursement.date", errorMessage,
                    getSubmittedOnDate(), getExpectedDisbursedOnLocalDate());
        }

        if (isChargesModified) {
            actualChanges.put(PARAM_CHARGES, getLoanCharges(possiblyModifedLoanCharges));
            actualChanges.put(RECALCULATE_LOAN_SCHEDULE, true);
        }

        if (command.parameterExists(PARAM_COLLATERAL) && possiblyModifiedLoanCollateralItems != null
                && !possiblyModifiedLoanCollateralItems.isEmpty()) {
            Set<LoanCollateralManagement> loanCollateralManagements = this.loanCollateralManagements;
            boolean isTrue = possiblyModifiedLoanCollateralItems.equals(loanCollateralManagements);

            if (!isTrue) {
                actualChanges.put(PARAM_COLLATERAL, getLoanCollateralDataFormCommand(possiblyModifiedLoanCollateralItems));
            }
        }

        if (command.isChangeInIntegerParameterNamed(LOAN_TERM_FREQUENCY, this.termFrequency)) {
            final Integer newValue = command.integerValueOfParameterNamed(LOAN_TERM_FREQUENCY);
            actualChanges.put(LOAN_TERM_FREQUENCY, newValue);
            this.termFrequency = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LOAN_TERM_FREQUENCY_TYPE, this.termPeriodFrequencyType)) {
            final Integer newValue = command.integerValueOfParameterNamed(LOAN_TERM_FREQUENCY_TYPE);
            final PeriodFrequencyType newTermPeriodFrequencyType = PeriodFrequencyType.fromInt(newValue);
            actualChanges.put(LOAN_TERM_FREQUENCY_TYPE, newTermPeriodFrequencyType.getValue());
            this.termPeriodFrequencyType = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(PRINCIPAL, this.approvedPrincipal)) {
            this.approvedPrincipal = command.bigDecimalValueOfParameterNamed(PRINCIPAL);
        }

        if (command.isChangeInBigDecimalParameterNamed(PRINCIPAL, this.proposedPrincipal)) {
            this.proposedPrincipal = command.bigDecimalValueOfParameterNamed(PRINCIPAL);
        }

        if (loanProduct.isMultiDisburseLoan()) {
            updateDisbursementDetails(command, actualChanges);
            if (command.isChangeInBigDecimalParameterNamed(LoanApiConstants.maxOutstandingBalanceParameterName,
                    this.maxOutstandingLoanBalance)) {
                this.maxOutstandingLoanBalance = command
                        .bigDecimalValueOfParameterNamed(LoanApiConstants.maxOutstandingBalanceParameterName);
            }
            final JsonArray disbursementDataArray = command.arrayOfParameterNamed(LoanApiConstants.disbursementDataParameterName);

            if (loanProduct.isDisallowExpectedDisbursements()) {
                if (disbursementDataArray != null && !disbursementDataArray.isEmpty()) {
                    final String errorMessage = "For this loan product, disbursement details are not allowed";
                    throw new MultiDisbursementDataNotAllowedException(LoanApiConstants.disbursementDataParameterName, errorMessage);
                }
            } else {
                if (disbursementDataArray == null || disbursementDataArray.size() == 0) {
                    final String errorMessage = "For this loan product, disbursement details must be provided";
                    throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
                }

                if (disbursementDataArray.size() > loanProduct.maxTrancheCount()) {
                    final String errorMessage = "Number of tranche shouldn't be greter than " + loanProduct.maxTrancheCount();
                    throw new ExceedingTrancheCountException(LoanApiConstants.disbursementDataParameterName, errorMessage,
                            loanProduct.maxTrancheCount(), disbursementDetails.size());
                }
            }
        } else {
            this.disbursementDetails.clear();
        }

        if (loanProduct.isMultiDisburseLoan() || loanProduct.canDefineInstallmentAmount()) {
            if (command.isChangeInBigDecimalParameterNamed(LoanApiConstants.emiAmountParameterName, this.fixedEmiAmount)) {
                this.fixedEmiAmount = command.bigDecimalValueOfParameterNamed(LoanApiConstants.emiAmountParameterName);
                actualChanges.put(LoanApiConstants.emiAmountParameterName, this.fixedEmiAmount);
                actualChanges.put(RECALCULATE_LOAN_SCHEDULE, true);
            }
        } else {
            this.fixedEmiAmount = null;
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName,
                this.fixedPrincipalPercentagePerInstallment)) {
            this.fixedPrincipalPercentagePerInstallment = command
                    .bigDecimalValueOfParameterNamed(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName);
            actualChanges.put(LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName,
                    this.fixedPrincipalPercentagePerInstallment);
        }

        return actualChanges;
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
        if (graceDate.isAfter(installment.getDueDate())) {
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
        Money amount = Money.zero(getCurrency());
        switch (calculationType) {
            case PERCENT_OF_AMOUNT:
                amount = installment.getPrincipalOutstanding(getCurrency());
            break;
            case PERCENT_OF_AMOUNT_AND_INTEREST:
                amount = installment.getPrincipalOutstanding(getCurrency()).plus(installment.getInterestOutstanding(getCurrency()));
            break;
            case PERCENT_OF_INTEREST:
                amount = installment.getInterestOutstanding(getCurrency());
            break;
            default:
            break;
        }
        return amount;
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
        String chargeIds = null;
        // From modify application page, if user removes all charges, we should
        // get empty array.
        // So we need to remove all charges applied for this loan
        boolean removeAllChages = false;
        if (jsonCommand.parameterExists(LoanApiConstants.chargesParameterName)) {
            JsonArray chargesArray = jsonCommand.arrayOfParameterNamed(LoanApiConstants.chargesParameterName);
            if (chargesArray.size() == 0) {
                removeAllChages = true;
            }
        }

        if (jsonCommand.parameterExists(LoanApiConstants.disbursementDataParameterName)) {
            final JsonArray disbursementDataArray = jsonCommand.arrayOfParameterNamed(LoanApiConstants.disbursementDataParameterName);
            if (disbursementDataArray != null && disbursementDataArray.size() > 0) {
                String dateFormat = null;
                Locale locale = null;
                // Gets date format and locate
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
                        if (chargeIds.indexOf(",") != -1) {
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
                removeDisbursementAndAssociatedCharges(actualChanges, disbursementList, loanChargeIds, chargeIdLength, removeAllChages);
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
        List<LoanCharge> tempCharges = new ArrayList<>();
        for (LoanCharge charge : getCharges()) {
            LoanTrancheDisbursementCharge transCharge = charge.getTrancheDisbursementCharge();
            if (transCharge != null && id.equals(transCharge.getloanDisbursementDetails().getId())) {
                tempCharges.add(charge);
            }
        }
        for (LoanCharge charge : tempCharges) {
            removeLoanCharge(charge);
        }
    }

    private List<Long> fetchLoanTrancheChargeIds() {
        List<Long> list = new ArrayList<>();
        for (LoanCharge charge : getCharges()) {
            if (charge.isTrancheDisbursementCharge() && charge.isActive()) {
                list.add(charge.getId());
            }
        }
        return list;
    }

    public LoanDisbursementDetails fetchLoanDisbursementsById(Long id) {
        LoanDisbursementDetails loanDisbursementDetail = null;
        for (LoanDisbursementDetails disbursementDetail : getDisbursementDetails()) {
            if (id.equals(disbursementDetail.getId())) {
                loanDisbursementDetail = disbursementDetail;
                break;
            }
        }
        return loanDisbursementDetail;
    }

    private List<Long> fetchDisbursementIds() {
        List<Long> list = new ArrayList<>();
        for (LoanDisbursementDetails disbursementDetails : getDisbursementDetails()) {
            list.add(disbursementDetails.getId());
        }
        return list;
    }

    private LoanCollateralManagementData[] getLoanCollateralDataFormCommand(final Set<LoanCollateralManagement> setOfLoanCollateral) {

        LoanCollateralManagementData[] existingLoanCollateral = null;

        final List<LoanCollateralManagementData> loanCollateralList = new ArrayList<>();
        for (final LoanCollateralManagement loanCollateral : setOfLoanCollateral) {

            loanCollateralList.add(loanCollateral.toCommand());

        }

        existingLoanCollateral = loanCollateralList.toArray(new LoanCollateralManagementData[0]);

        return existingLoanCollateral;
    }

    private LoanChargeCommand[] getLoanCharges(final Set<LoanCharge> setOfLoanCharges) {

        LoanChargeCommand[] existingLoanCharges = null;

        final List<LoanChargeCommand> loanChargesList = new ArrayList<>();
        for (final LoanCharge loanCharge : setOfLoanCharges) {
            loanChargesList.add(loanCharge.toCommand());
        }

        existingLoanCharges = loanChargesList.toArray(new LoanChargeCommand[0]);

        return existingLoanCharges;
    }

    private void removeFirstDisbursementTransaction() {
        List<LoanTransaction> transactions = getLoanTransactions();
        for (final LoanTransaction loanTransaction : transactions) {
            if (loanTransaction.isDisbursement()) {
                removeLoanTransaction(loanTransaction);
                break;
            }
        }
    }

    public void loanApplicationSubmittal(final LoanScheduleModel loanSchedule, final LoanApplicationTerms loanApplicationTerms,
            final LoanLifecycleStateMachine lifecycleStateMachine, final LocalDate submittedOn, final ExternalId externalId,
            final boolean allowTransactionsOnHoliday, final List<Holiday> holidays, final WorkingDays workingDays,
            final boolean allowTransactionsOnNonWorkingDay) {

        updateLoanSchedule(loanSchedule);

        lifecycleStateMachine.transition(LoanEvent.LOAN_CREATED, this);

        this.externalId = externalId;
        this.termFrequency = loanApplicationTerms.getLoanTermFrequency();
        this.termPeriodFrequencyType = loanApplicationTerms.getLoanTermPeriodFrequencyType().getValue();
        this.submittedOnDate = submittedOn;
        this.expectedDisbursementDate = loanApplicationTerms.getExpectedDisbursementDate();
        this.expectedFirstRepaymentOnDate = loanApplicationTerms.getRepaymentStartFromDate();
        this.interestChargedFromDate = loanApplicationTerms.getInterestChargedFromDate();

        updateLoanScheduleDependentDerivedFields();

        if (submittedOn.isAfter(DateUtils.getBusinessLocalDate())) {
            final String errorMessage = "The date on which a loan is submitted cannot be in the future.";
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.a.future.date", errorMessage, submittedOn,
                    DateUtils.getBusinessLocalDate());
        }

        if (this.client != null && this.client.isActivatedAfter(submittedOn)) {
            final String errorMessage = "The date on which a loan is submitted cannot be earlier than client's activation date.";
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.before.client.activation.date", errorMessage, submittedOn,
                    client.getActivationLocalDate());
        }

        validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_CREATED, submittedOn);

        if (this.group != null && this.group.isActivatedAfter(submittedOn)) {
            final String errorMessage = "The date on which a loan is submitted cannot be earlier than groups's activation date.";
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.before.group.activation.date", errorMessage, submittedOn,
                    group.getActivationLocalDate());
        }

        if (submittedOn.isAfter(getExpectedDisbursedOnLocalDate())) {
            final String errorMessage = "The date on which a loan is submitted cannot be after its expected disbursement date: "
                    + getExpectedDisbursedOnLocalDate().toString();
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.after.expected.disbursement.date", errorMessage,
                    submittedOn, getExpectedDisbursedOnLocalDate());
        }

        // charges are optional
        int penaltyWaitPeriod = 0;
        for (final LoanCharge loanCharge : getActiveCharges()) {
            recalculateLoanCharge(loanCharge, penaltyWaitPeriod);
        }

        updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());

        // validate if disbursement date is a holiday or a non-working day
        validateDisbursementDateIsOnNonWorkingDay(workingDays, allowTransactionsOnNonWorkingDay);
        validateDisbursementDateIsOnHoliday(allowTransactionsOnHoliday, holidays);

        /**
         * Copy interest recalculation settings if interest recalculation is enabled
         */
        if (this.loanRepaymentScheduleDetail.isInterestRecalculationEnabled()) {

            this.loanInterestRecalculationDetails = LoanInterestRecalculationDetails
                    .createFrom(this.loanProduct.getProductInterestRecalculationDetails());
            this.loanInterestRecalculationDetails.updateLoan(this);
        }

    }

    private LocalDate determineExpectedMaturityDate() {
        final int numberOfInstallments = this.repaymentScheduleInstallments.size();
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
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

    public Map<String, Object> loanApplicationRejection(final AppUser currentUser, final JsonCommand command,
            final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        validateAccountStatus(LoanEvent.LOAN_REJECTED);

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final LoanStatus statusEnum = loanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_REJECTED, this);
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
            final LocalDate rejectedOn = command.localDateValueOfParameterNamed(REJECTED_ON_DATE);

            final Locale locale = new Locale(command.locale());
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);

            this.rejectedOnDate = rejectedOn;
            this.rejectedBy = currentUser;
            this.closedOnDate = rejectedOn;
            this.closedBy = currentUser;

            loanLifecycleStateMachine.transition(LoanEvent.LOAN_REJECTED, this);
            actualChanges.put(PARAM_STATUS, LoanEnumerations.status(this.loanStatus));

            actualChanges.put(LOCALE, command.locale());
            actualChanges.put(DATE_FORMAT, command.dateFormat());
            actualChanges.put(REJECTED_ON_DATE, rejectedOn.format(fmt));
            actualChanges.put(CLOSED_ON_DATE, rejectedOn.format(fmt));

            if (rejectedOn.isBefore(getSubmittedOnDate())) {
                final String errorMessage = "The date on which a loan is rejected cannot be before its submittal date: "
                        + getSubmittedOnDate().toString();
                throw new InvalidLoanStateTransitionException("reject", "cannot.be.before.submittal.date", errorMessage, rejectedOn,
                        getSubmittedOnDate());
            }

            validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_REJECTED, rejectedOn);

            if (rejectedOn.isAfter(DateUtils.getBusinessLocalDate())) {
                final String errorMessage = "The date on which a loan is rejected cannot be in the future.";
                throw new InvalidLoanStateTransitionException("reject", "cannot.be.a.future.date", errorMessage, rejectedOn);
            }
        } else {
            final String errorMessage = "Only the loan applications with status 'Submitted and pending approval' are allowed to be rejected.";
            throw new InvalidLoanStateTransitionException("reject", "cannot.reject", errorMessage);
        }

        return actualChanges;
    }

    public Map<String, Object> loanApplicationWithdrawnByApplicant(final AppUser currentUser, final JsonCommand command,
            final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final LoanStatus statusEnum = loanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_WITHDRAWN, this);
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
            loanLifecycleStateMachine.transition(LoanEvent.LOAN_WITHDRAWN, this);
            actualChanges.put(PARAM_STATUS, LoanEnumerations.status(this.loanStatus));

            LocalDate withdrawnOn = command.localDateValueOfParameterNamed(WITHDRAWN_ON_DATE);
            if (withdrawnOn == null) {
                withdrawnOn = command.localDateValueOfParameterNamed(EVENT_DATE);
            }

            final Locale locale = new Locale(command.locale());
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);

            this.withdrawnOnDate = withdrawnOn;
            this.withdrawnBy = currentUser;
            this.closedOnDate = withdrawnOn;
            this.closedBy = currentUser;

            actualChanges.put(LOCALE, command.locale());
            actualChanges.put(DATE_FORMAT, command.dateFormat());
            actualChanges.put(WITHDRAWN_ON_DATE, withdrawnOn.format(fmt));
            actualChanges.put(CLOSED_ON_DATE, withdrawnOn.format(fmt));

            if (withdrawnOn.isBefore(getSubmittedOnDate())) {
                final String errorMessage = "The date on which a loan is withdrawn cannot be before its submittal date: "
                        + getSubmittedOnDate().toString();
                throw new InvalidLoanStateTransitionException("withdraw", "cannot.be.before.submittal.date", errorMessage, command,
                        getSubmittedOnDate());
            }

            validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_WITHDRAWN, withdrawnOn);

            if (withdrawnOn.isAfter(DateUtils.getBusinessLocalDate())) {
                final String errorMessage = "The date on which a loan is withdrawn cannot be in the future.";
                throw new InvalidLoanStateTransitionException("withdraw", "cannot.be.a.future.date", errorMessage, command);
            }
        } else {
            final String errorMessage = "Only the loan applications with status 'Submitted and pending approval' are allowed to be withdrawn by applicant.";
            throw new InvalidLoanStateTransitionException("withdraw", "cannot.withdraw", errorMessage);
        }

        return actualChanges;
    }

    public Map<String, Object> loanApplicationApproval(final AppUser currentUser, final JsonCommand command,
            final JsonArray disbursementDataArray, final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        validateAccountStatus(LoanEvent.LOAN_APPROVED);

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        /*
         * statusEnum is holding the possible new status derived from loanLifecycleStateMachine.transition.
         */

        final LoanStatus newStatusEnum = loanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_APPROVED, this);

        /*
         * FIXME: There is no need to check below condition, if loanLifecycleStateMachine.transition is doing it's
         * responsibility properly. Better implementation approach is, if code passes invalid combination of states
         * (fromState and toState), state machine should return invalidate state and below if condition should check for
         * not equal to invalidateState, instead of check new value is same as present value.
         */

        if (!newStatusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
            loanLifecycleStateMachine.transition(LoanEvent.LOAN_APPROVED, this);
            actualChanges.put(PARAM_STATUS, LoanEnumerations.status(this.loanStatus));

            // only do below if status has changed in the 'approval' case
            LocalDate approvedOn = command.localDateValueOfParameterNamed(APPROVED_ON_DATE);
            String approvedOnDateChange = command.stringValueOfParameterNamed(APPROVED_ON_DATE);
            if (approvedOn == null) {
                approvedOn = command.localDateValueOfParameterNamed(EVENT_DATE);
                approvedOnDateChange = command.stringValueOfParameterNamed(EVENT_DATE);
            }

            LocalDate expecteddisbursementDate = command.localDateValueOfParameterNamed(EXPECTED_DISBURSEMENT_DATE);

            BigDecimal approvedLoanAmount = command.bigDecimalValueOfParameterNamed(LoanApiConstants.approvedLoanAmountParameterName);
            if (approvedLoanAmount != null) {
                compareApprovedToProposedPrincipal(approvedLoanAmount);

                /*
                 * All the calculations are done based on the principal amount, so it is necessary to set principal
                 * amount to approved amount
                 */
                this.approvedPrincipal = approvedLoanAmount;

                this.loanRepaymentScheduleDetail.setPrincipal(approvedLoanAmount);
                actualChanges.put(LoanApiConstants.approvedLoanAmountParameterName, approvedLoanAmount);
                actualChanges.put(LoanApiConstants.disbursementPrincipalParameterName, approvedLoanAmount);
                actualChanges.put(LoanApiConstants.disbursementNetDisbursalAmountParameterName, netDisbursalAmount);

                /* Update disbursement details */
                if (disbursementDataArray != null) {
                    updateDisbursementDetails(command, actualChanges);
                }
            }

            recalculateAllCharges();

            if (loanProduct.isMultiDisburseLoan()) {
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

                if (currentDisbursementDetails.size() > loanProduct.maxTrancheCount()) {
                    final String errorMessage = "Number of tranche shouldn't be greter than " + loanProduct.maxTrancheCount();
                    throw new ExceedingTrancheCountException(LoanApiConstants.disbursementDataParameterName, errorMessage,
                            loanProduct.maxTrancheCount(), currentDisbursementDetails.size());
                }
            }
            this.approvedOnDate = approvedOn;
            this.approvedBy = currentUser;
            actualChanges.put(LOCALE, command.locale());
            actualChanges.put(DATE_FORMAT, command.dateFormat());
            actualChanges.put(APPROVED_ON_DATE, approvedOnDateChange);

            final LocalDate submittalDate = this.submittedOnDate;
            if (approvedOn.isBefore(submittalDate)) {
                final String errorMessage = "The date on which a loan is approved cannot be before its submittal date: "
                        + submittalDate.toString();
                throw new InvalidLoanStateTransitionException("approval", "cannot.be.before.submittal.date", errorMessage,
                        getApprovedOnDate(), submittalDate);
            }

            if (expecteddisbursementDate != null) {
                this.expectedDisbursementDate = expecteddisbursementDate;
                actualChanges.put(EXPECTED_DISBURSEMENT_DATE, expectedDisbursementDate);

                if (expecteddisbursementDate.isBefore(approvedOn)) {
                    final String errorMessage = "The expected disbursement date should be either on or after the approval date: "
                            + approvedOn.toString();
                    throw new InvalidLoanStateTransitionException("expecteddisbursal", "should.be.on.or.after.approval.date", errorMessage,
                            getApprovedOnDate(), expecteddisbursementDate);
                }
            }

            validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_APPROVED, approvedOn);

            if (approvedOn.isAfter(DateUtils.getBusinessLocalDate())) {
                final String errorMessage = "The date on which a loan is approved cannot be in the future.";
                throw new InvalidLoanStateTransitionException("approval", "cannot.be.a.future.date", errorMessage, getApprovedOnDate());
            }

            if (this.loanOfficer != null) {
                final LoanOfficerAssignmentHistory loanOfficerAssignmentHistory = LoanOfficerAssignmentHistory.createNew(this,
                        this.loanOfficer, approvedOn);
                this.loanOfficerHistory.add(loanOfficerAssignmentHistory);
            }
            this.adjustNetDisbursalAmount(this.approvedPrincipal);
        }

        return actualChanges;

    }

    private void compareApprovedToProposedPrincipal(BigDecimal approvedLoanAmount) {

        if (this.loanProduct().isDisallowExpectedDisbursements() && this.loanProduct().isAllowApprovedDisbursedAmountsOverApplied()) {
            BigDecimal maxApprovedLoanAmount = getOverAppliedMax();
            if (approvedLoanAmount.compareTo(maxApprovedLoanAmount) > 0) {
                final String errorMessage = "Loan approved amount can't be greater than maximum applied loan amount calculation.";
                throw new InvalidLoanStateTransitionException("approval",
                        "amount.can't.be.greater.than.maximum.applied.loan.amount.calculation", errorMessage, approvedLoanAmount,
                        maxApprovedLoanAmount);
            }
        } else {
            if (approvedLoanAmount.compareTo(this.proposedPrincipal) > 0) {
                final String errorMessage = "Loan approved amount can't be greater than loan amount demanded.";
                throw new InvalidLoanStateTransitionException("approval", "amount.can't.be.greater.than.loan.amount.demanded", errorMessage,
                        this.proposedPrincipal, approvedLoanAmount);
            }
        }
    }

    private BigDecimal getOverAppliedMax() {
        BigDecimal maxAmount = null;
        if (this.getLoanProduct().getOverAppliedCalculationType().equals("percentage")) {
            BigDecimal overAppliedNumber = BigDecimal.valueOf(getLoanProduct().getOverAppliedNumber());
            BigDecimal x = overAppliedNumber.divide(BigDecimal.valueOf(100));
            BigDecimal totalPercentage = BigDecimal.valueOf(1).add(x);
            maxAmount = this.proposedPrincipal.multiply(totalPercentage);
        } else {
            maxAmount = this.proposedPrincipal.add(BigDecimal.valueOf(getLoanProduct().getOverAppliedNumber()));
        }
        return maxAmount;
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
        final List<Long> ids = new ArrayList<>();
        List<LoanTransaction> transactions = getLoanTransactions();
        for (final LoanTransaction transaction : transactions) {
            ids.add(transaction.getId());
        }

        return ids;
    }

    public List<Long> findExistingReversedTransactionIds() {

        final List<Long> ids = new ArrayList<>();
        List<LoanTransaction> transactions = getLoanTransactions();
        for (final LoanTransaction transaction : transactions) {
            if (transaction.isReversed()) {
                ids.add(transaction.getId());
            }
        }

        return ids;
    }

    public ChangedTransactionDetail disburse(final AppUser currentUser, final JsonCommand command, final Map<String, Object> actualChanges,
            final ScheduleGeneratorDTO scheduleGeneratorDTO, final PaymentDetail paymentDetail, boolean downPaymentEnabled) {

        final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed(ACTUAL_DISBURSEMENT_DATE);

        this.disbursedBy = currentUser;
        updateLoanScheduleDependentDerivedFields();

        actualChanges.put(LOCALE, command.locale());
        actualChanges.put(DATE_FORMAT, command.dateFormat());
        actualChanges.put(ACTUAL_DISBURSEMENT_DATE, command.stringValueOfParameterNamed(ACTUAL_DISBURSEMENT_DATE));

        HolidayDetailDTO holidayDetailDTO = scheduleGeneratorDTO.getHolidayDetailDTO();

        // validate if disbursement date is a holiday or a non-working day
        validateDisbursementDateIsOnNonWorkingDay(holidayDetailDTO.getWorkingDays(), holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());
        validateDisbursementDateIsOnHoliday(holidayDetailDTO.isAllowTransactionsOnHoliday(), holidayDetailDTO.getHolidays());

        regenerateRepaymentScheduleWithInterestRecalculationIfNeeded(this.repaymentScheduleDetail().isInterestRecalculationEnabled(),
                isDisbursementMissed(), scheduleGeneratorDTO, downPaymentEnabled);

        updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
        updateLoanRepaymentPeriodsDerivedFields(actualDisbursementDate);
        handleDisbursementTransaction(actualDisbursementDate, paymentDetail);
        updateLoanSummaryDerivedFields();
        final Money interestApplied = Money.of(getCurrency(), this.summary.getTotalInterestCharged());

        /**
         * Add an interest applied transaction of the interest is accrued upfront (Up front accrual), no accounting or
         * cash based accounting is selected
         **/

        if (((isMultiDisburmentLoan() && getDisbursedLoanDisbursementDetails().size() == 1) || !isMultiDisburmentLoan())
                && isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()) {
            ExternalId externalId = ExternalId.empty();
            if (TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
                externalId = ExternalId.generate();
            }
            final LoanTransaction interestAppliedTransaction = LoanTransaction.accrueInterest(getOffice(), this, interestApplied,
                    actualDisbursementDate, externalId);
            addLoanTransaction(interestAppliedTransaction);
        }

        ChangedTransactionDetail result = reprocessTransactionForDisbursement();
        this.loanLifecycleStateMachine.transition(LoanEvent.LOAN_DISBURSED, this);
        actualChanges.put(PARAM_STATUS, LoanEnumerations.status(this.loanStatus));
        return result;

    }

    private void regenerateRepaymentScheduleWithInterestRecalculationIfNeeded(boolean interestRecalculationEnabledParam,
            boolean disbursementMissedParam, ScheduleGeneratorDTO scheduleGeneratorDTO, final boolean downPaymentEnabled) {

        LocalDate firstInstallmentDueDate = fetchRepaymentScheduleInstallment(1).getDueDate();
        if ((interestRecalculationEnabledParam
                && (firstInstallmentDueDate.isBefore(DateUtils.getBusinessLocalDate()) || disbursementMissedParam))) {
            regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO);
        } else if (downPaymentEnabled) {
            LoanScheduleDTO loanSchedule = getRecalculatedSchedule(scheduleGeneratorDTO);
            updateLoanSchedule(loanSchedule.getInstallments());
        }
    }

    private List<LoanDisbursementDetails> getDisbursedLoanDisbursementDetails() {
        List<LoanDisbursementDetails> ret = new ArrayList<>();
        if (this.disbursementDetails != null && !this.disbursementDetails.isEmpty()) {
            for (LoanDisbursementDetails disbursementDetail : getDisbursementDetails()) {
                if (disbursementDetail.actualDisbursementDate() != null) {
                    ret.add(disbursementDetail);
                }
            }
        }
        return ret;
    }

    public void regenerateScheduleOnDisbursement(final ScheduleGeneratorDTO scheduleGeneratorDTO, final boolean recalculateSchedule,
            final LocalDate actualDisbursementDate, BigDecimal emiAmount, LocalDate nextPossibleRepaymentDate,
            LocalDate rescheduledRepaymentDate) {
        boolean isEmiAmountChanged = false;
        if ((this.loanProduct.isMultiDisburseLoan() || this.loanProduct.canDefineInstallmentAmount()) && emiAmount != null
                && emiAmount.compareTo(retriveLastEmiAmount()) != 0) {
            if (this.loanProduct.isMultiDisburseLoan()) {
                final LocalDate dateValue = null;
                final boolean isSpecificToInstallment = false;
                final Boolean isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled = scheduleGeneratorDTO
                        .isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled();
                LocalDate effectiveDateFrom = actualDisbursementDate;
                if (!isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled && actualDisbursementDate.equals(nextPossibleRepaymentDate)) {
                    effectiveDateFrom = nextPossibleRepaymentDate.plusDays(1);
                }
                LoanTermVariations loanVariationTerms = new LoanTermVariations(LoanTermVariationType.EMI_AMOUNT.getValue(),
                        effectiveDateFrom, emiAmount, dateValue, isSpecificToInstallment, this, LoanStatus.ACTIVE.getValue());
                this.loanTermVariations.add(loanVariationTerms);
            } else {
                this.fixedEmiAmount = emiAmount;
            }
            isEmiAmountChanged = true;
        }
        if (rescheduledRepaymentDate != null && this.loanProduct.isMultiDisburseLoan()) {
            final boolean isSpecificToInstallment = false;
            LoanTermVariations loanVariationTerms = new LoanTermVariations(LoanTermVariationType.DUE_DATE.getValue(),
                    nextPossibleRepaymentDate, emiAmount, rescheduledRepaymentDate, isSpecificToInstallment, this,
                    LoanStatus.ACTIVE.getValue());
            this.loanTermVariations.add(loanVariationTerms);
        }

        if (isRepaymentScheduleRegenerationRequiredForDisbursement(actualDisbursementDate) || recalculateSchedule || isEmiAmountChanged
                || rescheduledRepaymentDate != null) {
            if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO);
            } else {
                regenerateRepaymentSchedule(scheduleGeneratorDTO);
            }
        }
    }

    public boolean canDisburse(final LocalDate actualDisbursementDate) {
        LocalDate lastDisburseDate = this.actualDisbursementDate;
        final LoanStatus statusEnum = this.loanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_DISBURSED, this);

        boolean isMultiTrancheDisburse = false;
        LoanStatus actualLoanStatus = LoanStatus.fromInt(this.loanStatus);
        if ((actualLoanStatus.isActive() || actualLoanStatus.isClosedObligationsMet() || actualLoanStatus.isOverpaid())
                && isAllTranchesNotDisbursed()) {
            LoanDisbursementDetails details = fetchLastDisburseDetail();

            if (details != null) {
                lastDisburseDate = details.actualDisbursementDate();
            }
            if (actualDisbursementDate.isBefore(lastDisburseDate)) {
                final String errorMsg = "Loan can't be disbursed before " + lastDisburseDate;
                throw new LoanDisbursalException(errorMsg, "actualdisbursementdate.before.lastdusbursedate", lastDisburseDate,
                        actualDisbursementDate);
            }
            isMultiTrancheDisburse = true;
        }
        return !statusEnum.hasStateOf(actualLoanStatus) || isMultiTrancheDisburse;
    }

    public Money adjustDisburseAmount(final JsonCommand command, final LocalDate actualDisbursementDate) {
        Money disburseAmount = this.loanRepaymentScheduleDetail.getPrincipal().zero();
        BigDecimal principalDisbursed = command.bigDecimalValueOfParameterNamed(LoanApiConstants.principalDisbursedParameterName);
        if (this.actualDisbursementDate == null) {
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

    private ChangedTransactionDetail reprocessTransactionForDisbursement() {
        ChangedTransactionDetail changedTransactionDetail = null;
        if (this.loanProduct.isMultiDisburseLoan()) {
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsPostDisbursement();
            if (!allNonContraTransactionsPostDisbursement.isEmpty()) {
                final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                        .determineProcessor(this.transactionProcessingStrategyCode);
                changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(),
                        allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(), getActiveCharges());
                for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                    mapEntry.getValue().updateLoan(this);
                }
                this.loanTransactions.addAll(changedTransactionDetail.getNewTransactionMappings().values());
            }
            updateLoanSummaryDerivedFields();
        }

        return changedTransactionDetail;
    }

    private Collection<LoanDisbursementDetails> fetchUndisbursedDetail() {
        Collection<LoanDisbursementDetails> disbursementDetails = new ArrayList<>();
        LocalDate date = null;
        for (LoanDisbursementDetails disbursementDetail : getDisbursementDetails()) {
            if (disbursementDetail.actualDisbursementDate() == null) {
                if (date == null || disbursementDetail.expectedDisbursementDate().compareTo(date) == 0 ? Boolean.TRUE : Boolean.FALSE) {
                    disbursementDetails.add(disbursementDetail);
                    date = disbursementDetail.expectedDisbursementDate();
                } else if (disbursementDetail.expectedDisbursementDate().isBefore(date)) {
                    disbursementDetails.clear();
                    disbursementDetails.add(disbursementDetail);
                    date = disbursementDetail.expectedDisbursementDate();
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
                if (disbursementDetail.actualDisbursementDate() != null && (disbursementDetail.actualDisbursementDate().isAfter(date)
                        || disbursementDetail.actualDisbursementDate().compareTo(date) == 0 ? Boolean.TRUE : Boolean.FALSE)) {
                    date = disbursementDetail.actualDisbursementDate();
                    details = disbursementDetail;
                }
            }
        }
        return details;
    }

    private boolean isDisbursementMissed() {
        boolean isDisbursementMissed = false;
        for (LoanDisbursementDetails disbursementDetail : getDisbursementDetails()) {
            if (disbursementDetail.actualDisbursementDate() == null
                    && DateUtils.getBusinessLocalDate().isAfter(disbursementDetail.expectedDisbursementDateAsLocalDate())) {
                isDisbursementMissed = true;
                break;
            }
        }
        return isDisbursementMissed;
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
        Set<LoanDisbursementDetails> details = new HashSet<>(getDisbursementDetails());
        for (LoanDisbursementDetails disbursementDetail : details) {
            if (disbursementDetail.actualDisbursementDate() == null) {
                this.disbursementDetails.remove(disbursementDetail);
            }
        }
    }

    private boolean isDisbursementAllowed() {
        boolean isAllowed = false;
        List<LoanDisbursementDetails> disbursementDetails = getDisbursementDetails();
        if (disbursementDetails == null || disbursementDetails.isEmpty()) {
            isAllowed = true;
        } else {
            for (LoanDisbursementDetails disbursementDetail : disbursementDetails) {
                if (disbursementDetail.actualDisbursementDate() == null) {
                    isAllowed = true;
                    break;
                }
            }
        }
        return isAllowed;
    }

    private boolean atleastOnceDisbursed() {
        boolean isDisbursed = false;
        for (LoanDisbursementDetails disbursementDetail : getDisbursementDetails()) {
            if (disbursementDetail.actualDisbursementDate() != null) {
                isDisbursed = true;
                break;
            }
        }
        return isDisbursed;
    }

    private void updateLoanRepaymentPeriodsDerivedFields(final LocalDate actualDisbursementDate) {
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment repaymentPeriod : installments) {
            repaymentPeriod.updateDerivedFields(loanCurrency(), actualDisbursementDate);
        }
    }

    /*
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

        final RoundingMode roundingMode = MoneyHelper.getRoundingMode();
        final MathContext mc = new MathContext(8, roundingMode);

        final LoanApplicationTerms loanApplicationTerms = constructLoanApplicationTerms(scheduleGeneratorDTO);
        LoanScheduleGenerator loanScheduleGenerator = null;
        if (loanApplicationTerms.isEqualAmortization()) {
            if (loanApplicationTerms.getInterestMethod().isDecliningBalance()) {
                final LoanScheduleGenerator decliningLoanScheduleGenerator = scheduleGeneratorDTO.getLoanScheduleFactory()
                        .create(InterestMethod.DECLINING_BALANCE);
                Set<LoanCharge> loanCharges = getActiveCharges();
                LoanScheduleModel loanSchedule = decliningLoanScheduleGenerator.generate(mc, loanApplicationTerms, loanCharges,
                        scheduleGeneratorDTO.getHolidayDetailDTO());

                loanApplicationTerms
                        .updateTotalInterestDue(Money.of(loanApplicationTerms.getCurrency(), loanSchedule.getTotalInterestCharged()));

            }
            loanScheduleGenerator = scheduleGeneratorDTO.getLoanScheduleFactory().create(InterestMethod.FLAT);
        } else {
            loanScheduleGenerator = scheduleGeneratorDTO.getLoanScheduleFactory().create(loanApplicationTerms.getInterestMethod());
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
                if (!interestRateStartDate.isBefore(periodData.getFromDateAsLocalDate())) {
                    interestRateStartDate = periodData.getFromDateAsLocalDate();
                    interestRate = periodData.getInterestRate();
                }
                loanTermVariations.add(loanTermVariation);
            }
        }
        return interestRate;
    }

    private void handleDisbursementTransaction(final LocalDate disbursedOn, final PaymentDetail paymentDetail) {

        // add repayment transaction to track incoming money from client to mfi
        // for (charges due at time of disbursement)

        /***
         * TODO Vishwas: do we need to be able to pass in payment type details for repayments at disbursements too?
         ***/

        final Money totalFeeChargesDueAtDisbursement = this.summary.getTotalFeeChargesDueAtDisbursement(loanCurrency());
        /**
         * all Charges repaid at disbursal is marked as repaid and "APPLY Charge" transactions are created for all other
         * fees ( which are created during disbursal but not repaid)
         **/

        Money disbursentMoney = Money.zero(getCurrency());
        final LoanTransaction chargesPayment = LoanTransaction.repaymentAtDisbursement(getOffice(), disbursentMoney, paymentDetail,
                disbursedOn, null);
        final Integer installmentNumber = null;
        for (final LoanCharge charge : getActiveCharges()) {
            LocalDate actualDisbursementDate = getActualDisbursementDate(charge);
            /**
             * create a Charge applied transaction if Up front Accrual, None or Cash based accounting is enabled
             **/
            if ((charge.getCharge().getChargeTimeType().equals(ChargeTimeType.DISBURSEMENT.getValue())
                    && disbursedOn.equals(actualDisbursementDate) && (actualDisbursementDate != null) && !charge.isWaived()
                    && !charge.isFullyPaid())
                    || (charge.getCharge().getChargeTimeType().equals(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue())
                            && disbursedOn.equals(actualDisbursementDate) && (actualDisbursementDate != null) && !charge.isWaived()
                            && !charge.isFullyPaid())) {
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

        if (getApprovedOnDate() != null && disbursedOn.isBefore(getApprovedOnDate())) {
            final String errorMessage = "The date on which a loan is disbursed cannot be before its approval date: "
                    + getApprovedOnDate().toString();
            throw new InvalidLoanStateTransitionException("disbursal", "cannot.be.before.approval.date", errorMessage, disbursedOn,
                    getApprovedOnDate());
        }

        if (getExpectedFirstRepaymentOnDate() != null
                && (disbursedOn.isAfter(this.fetchRepaymentScheduleInstallment(1).getDueDate())
                        || disbursedOn.isAfter(getExpectedFirstRepaymentOnDate()))
                && disbursedOn.compareTo(this.actualDisbursementDate) == 0 ? Boolean.TRUE : Boolean.FALSE) {
            final String errorMessage = "submittedOnDate cannot be after the loans  expectedFirstRepaymentOnDate: "
                    + getExpectedFirstRepaymentOnDate().toString();
            throw new InvalidLoanStateTransitionException("disbursal", "cannot.be.after.expected.first.repayment.date", errorMessage,
                    disbursedOn, getExpectedFirstRepaymentOnDate());
        }

        validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_DISBURSED, disbursedOn);

        if (disbursedOn.isAfter(DateUtils.getBusinessLocalDate())) {
            final String errorMessage = "The date on which a loan with identifier : " + this.accountNumber
                    + " is disbursed cannot be in the future.";
            throw new InvalidLoanStateTransitionException("disbursal", "cannot.be.a.future.date", errorMessage, disbursedOn);
        }

    }

    public void handleDownPayment(final BigDecimal disbursedAmount, final JsonCommand command,
            final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        if (isAutoRepaymentForDownPaymentEnabled()) {
            LocalDate disbursedOn = command.localDateValueOfParameterNamed(ACTUAL_DISBURSEMENT_DATE);
            BigDecimal disbursedAmountPercentageForDownPayment = this.loanRepaymentScheduleDetail
                    .getDisbursedAmountPercentageForDownPayment();
            ExternalId externalId = ExternalId.empty();
            if (TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
                externalId = ExternalId.generate();
            }
            Money downPaymentMoney = Money.of(getCurrency(),
                    MathUtil.percentageOf(disbursedAmount, disbursedAmountPercentageForDownPayment, 19));
            LoanTransaction downPaymentTransaction = LoanTransaction.downPayment(getOffice(), downPaymentMoney, null, disbursedOn,
                    externalId);
            /*
             * Currently mapping down payment transaction as repayment transaction to loan repayment schedule for
             * consistency. Need to replace below logic for creating new installment for down payment and mapping.
             */

            LoanEvent event = LoanEvent.LOAN_REPAYMENT_OR_WAIVER;
            validateRepaymentTypeAccountStatus(downPaymentTransaction, event);
            HolidayDetailDTO holidayDetailDTO = scheduleGeneratorDTO.getHolidayDetailDTO();
            validateRepaymentDateIsOnHoliday(downPaymentTransaction.getTransactionDate(), holidayDetailDTO.isAllowTransactionsOnHoliday(),
                    holidayDetailDTO.getHolidays());
            validateRepaymentDateIsOnNonWorkingDay(downPaymentTransaction.getTransactionDate(), holidayDetailDTO.getWorkingDays(),
                    holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());

            handleRepaymentOrRecoveryOrWaiverTransaction(downPaymentTransaction, loanLifecycleStateMachine, null, scheduleGeneratorDTO);
        }
    }

    private boolean isAutoRepaymentForDownPaymentEnabled() {
        return this.loanRepaymentScheduleDetail.isEnableDownPayment()
                && this.loanRepaymentScheduleDetail.isEnableAutoRepaymentForDownPayment();
    }

    public LoanTransaction handlePayDisbursementTransaction(final Long chargeId, final LoanTransaction chargesPayment,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());
        LoanCharge charge = null;
        for (final LoanCharge loanCharge : this.charges) {
            if (loanCharge.isActive() && chargeId.equals(loanCharge.getId())) {
                charge = loanCharge;
            }
        }
        @SuppressWarnings("null")
        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(chargesPayment, charge, charge.amount(), null);
        chargesPayment.getLoanChargesPaid().add(loanChargePaidBy);
        final Money zero = Money.zero(getCurrency());
        chargesPayment.updateComponents(zero, zero, charge.getAmount(getCurrency()), zero);
        chargesPayment.updateLoan(this);
        addLoanTransaction(chargesPayment);
        updateLoanOutstandingBalances();
        charge.markAsFullyPaid();
        return chargesPayment;
    }

    public void removePostDatedChecks() {
        List<PostDatedChecks> postDatedChecks = new ArrayList<>();
        this.postDatedChecks = postDatedChecks;
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
            final boolean isScheduleRegenerateRequired = isRepaymentScheduleRegenerationRequiredForDisbursement(actualDisbursementDate);
            this.actualDisbursementDate = null;
            this.disbursedBy = null;
            boolean isDisbursedAmountChanged = approvedPrincipal.compareTo(this.loanRepaymentScheduleDetail.getPrincipal().getAmount()) == 0
                    ? Boolean.FALSE
                    : Boolean.TRUE;
            this.loanRepaymentScheduleDetail.setPrincipal(this.approvedPrincipal);
            if (this.loanProduct.isMultiDisburseLoan()) {
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
        HolidayDetailDTO holidayDetailDTO = null;
        LoanEvent event = null;
        if (isRecoveryRepayment) {
            event = LoanEvent.LOAN_RECOVERY_PAYMENT;
        } else {
            event = LoanEvent.LOAN_REPAYMENT_OR_WAIVER;
        }
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
        if (repaymentTransaction.isGoodwillCredit() || repaymentTransaction.isMerchantIssuedRefund()
                || repaymentTransaction.isPayoutRefund() || repaymentTransaction.isChargeRefund() || repaymentTransaction.isRepayment()
                || repaymentTransaction.isDownPayment()) {

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

        if (paymentTransaction.getTransactionDate().isAfter(DateUtils.getBusinessLocalDate())) {
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
            } else if (!isAfterLatRepayment(loanTransaction, getLoanTransactions())) {
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
                && loanTransaction.getAmount(loanCurrency()).getAmount().compareTo(getLoanSummary().getTotalWrittenOff()) > 0) {
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

        final LocalDate loanTransactionDate = loanTransaction.getTransactionDate();
        if (loanTransactionDate.isBefore(getDisbursementDate())) {
            final String errorMessage = "The transaction date cannot be before the loan disbursement date: "
                    + getDisbursementDate().toString();
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.before.disbursement.date", errorMessage,
                    loanTransactionDate, getDisbursementDate());
        }

        if (loanTransactionDate.isAfter(DateUtils.getBusinessLocalDate())) {
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
        boolean reprocess = true;

        if (!isForeclosure() && isTransactionChronologicallyLatest && adjustedTransaction == null
                && loanTransaction.getTransactionDate().isEqual(DateUtils.getBusinessLocalDate()) && currentInstallment != null
                && currentInstallment.getTotalOutstanding(getCurrency()).isEqualTo(loanTransaction.getAmount(getCurrency()))) {
            reprocess = false;
        }

        if (isTransactionChronologicallyLatest && adjustedTransaction == null
                && (!reprocess || !this.repaymentScheduleDetail().isInterestRecalculationEnabled()) && !isForeclosure()) {
            loanRepaymentScheduleTransactionProcessor.handleTransaction(loanTransaction, getCurrency(), getRepaymentScheduleInstallments(),
                    getActiveCharges());
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
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsPostDisbursement();
            changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(),
                    allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(), getActiveCharges());
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                mapEntry.getValue().updateLoan(this);
            }
            /***
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

    private LoanRepaymentScheduleInstallment fetchLoanRepaymentScheduleInstallment(LocalDate dueDate) {
        LoanRepaymentScheduleInstallment installment = null;
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : installments) {
            if (dueDate.equals(loanRepaymentScheduleInstallment.getDueDate())) {
                installment = loanRepaymentScheduleInstallment;
                break;
            }
        }
        return installment;
    }

    private List<LoanTransaction> retrieveListOfIncomePostingTransactions() {
        final List<LoanTransaction> incomePostTransactions = new ArrayList<>();
        List<LoanTransaction> trans = getLoanTransactions();
        for (final LoanTransaction transaction : trans) {
            if (transaction.isNotReversed() && transaction.isIncomePosting()) {
                incomePostTransactions.add(transaction);
            }
        }
        final LoanTransactionComparator transactionComparator = new LoanTransactionComparator();
        Collections.sort(incomePostTransactions, transactionComparator);
        return incomePostTransactions;
    }

    private List<LoanTransaction> retrieveListOfTransactionsPostDisbursement() {
        final List<LoanTransaction> repaymentsOrWaivers = new ArrayList<>();
        List<LoanTransaction> trans = getLoanTransactions();
        for (final LoanTransaction transaction : trans) {
            if (transaction.isNotReversed()
                    && (transaction.isChargeOff() || !(transaction.isDisbursement() || transaction.isNonMonetaryTransaction()))) {
                repaymentsOrWaivers.add(transaction);
            }
        }
        final LoanTransactionComparator transactionComparator = new LoanTransactionComparator();
        Collections.sort(repaymentsOrWaivers, transactionComparator);
        return repaymentsOrWaivers;
    }

    public List<LoanTransaction> retrieveListOfTransactionsPostDisbursementExcludeAccruals() {
        final List<LoanTransaction> repaymentsOrWaivers = new ArrayList<>();
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isNotReversed()
                    && !(transaction.isDisbursement() || transaction.isAccrual() || transaction.isRepaymentAtDisbursement()
                            || transaction.isNonMonetaryTransaction() || transaction.isIncomePosting())) {
                repaymentsOrWaivers.add(transaction);
            }
        }
        final LoanTransactionComparator transactionComparator = new LoanTransactionComparator();
        Collections.sort(repaymentsOrWaivers, transactionComparator);
        return repaymentsOrWaivers;
    }

    private List<LoanTransaction> retrieveListOfTransactionsExcludeAccruals() {
        final List<LoanTransaction> repaymentsOrWaivers = new ArrayList<>();
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isNotReversed() && !(transaction.isAccrual() || transaction.isNonMonetaryTransaction())) {
                repaymentsOrWaivers.add(transaction);
            }
        }
        final LoanTransactionComparator transactionComparator = new LoanTransactionComparator();
        Collections.sort(repaymentsOrWaivers, transactionComparator);
        return repaymentsOrWaivers;
    }

    private List<LoanTransaction> retrieveListOfAccrualTransactions() {
        final List<LoanTransaction> transactions = new ArrayList<>();
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isNotReversed() && transaction.isAccrual()) {
                transactions.add(transaction);
            }
        }
        final LoanTransactionComparator transactionComparator = new LoanTransactionComparator();
        Collections.sort(transactions, transactionComparator);
        return transactions;
    }

    public List<LoanTransaction> retrieveListOfTransactionsByType(final LoanTransactionType transactionType) {
        final List<LoanTransaction> transactions = new ArrayList<>();
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isNotReversed() && transaction.getTypeOf().equals(transactionType)) {
                transactions.add(transaction);
            }
        }
        final LoanTransactionComparator transactionComparator = new LoanTransactionComparator();
        Collections.sort(transactions, transactionComparator);
        return transactions;
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

        boolean isAllChargesPaid = true;
        for (final LoanCharge loanCharge : this.charges) {
            if (loanCharge.isActive() && loanCharge.amount().compareTo(BigDecimal.ZERO) > 0
                    && !(loanCharge.isPaid() || loanCharge.isWaived())) {
                isAllChargesPaid = false;
                break;
            }
        }
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
        processIncomeAccrualTransactionOnLoanClosure();
    }

    private void processIncomeAccrualTransactionOnLoanClosure() {
        if (this.loanInterestRecalculationDetails != null && this.loanInterestRecalculationDetails.isCompoundingToBePostedAsTransaction()
                && this.getStatus().isClosedObligationsMet() && !isNpa() && !isChargedOff()) {

            ExternalId externalId = ExternalId.empty();
            boolean isExternalIdAutoGenerationEnabled = TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled();

            LocalDate closedDate = this.getClosedOnDate();
            reverseTransactionsOnOrAfter(retrieveListOfIncomePostingTransactions(), closedDate);
            reverseTransactionsOnOrAfter(retrieveListOfAccrualTransactions(), closedDate);
            HashMap<String, BigDecimal> cumulativeIncomeFromInstallments = new HashMap<>();
            determineCumulativeIncomeFromInstallments(cumulativeIncomeFromInstallments);
            HashMap<String, BigDecimal> cumulativeIncomeFromIncomePosting = new HashMap<>();
            determineCumulativeIncomeDetails(retrieveListOfIncomePostingTransactions(), cumulativeIncomeFromIncomePosting);
            BigDecimal interestToPost = cumulativeIncomeFromInstallments.get(INTEREST)
                    .subtract(cumulativeIncomeFromIncomePosting.get(INTEREST));
            BigDecimal feeToPost = cumulativeIncomeFromInstallments.get(FEE).subtract(cumulativeIncomeFromIncomePosting.get(FEE));
            BigDecimal penaltyToPost = cumulativeIncomeFromInstallments.get(PENALTY)
                    .subtract(cumulativeIncomeFromIncomePosting.get(PENALTY));
            BigDecimal amountToPost = interestToPost.add(feeToPost).add(penaltyToPost);
            if (isExternalIdAutoGenerationEnabled) {
                externalId = ExternalId.generate();
            }
            LoanTransaction finalIncomeTransaction = LoanTransaction.incomePosting(this, this.getOffice(), closedDate, amountToPost,
                    interestToPost, feeToPost, penaltyToPost, externalId);
            addLoanTransaction(finalIncomeTransaction);
            if (isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
                List<LoanTransaction> updatedAccrualTransactions = retrieveListOfAccrualTransactions();
                LocalDate lastAccruedDate = this.getDisbursementDate();
                if (!updatedAccrualTransactions.isEmpty()) {
                    lastAccruedDate = updatedAccrualTransactions.get(updatedAccrualTransactions.size() - 1).getTransactionDate();
                }
                HashMap<String, Object> feeDetails = new HashMap<>();
                determineFeeDetails(lastAccruedDate, closedDate, feeDetails);
                if (isExternalIdAutoGenerationEnabled) {
                    externalId = ExternalId.generate();
                }
                LoanTransaction finalAccrual = LoanTransaction.accrueTransaction(this, this.getOffice(), closedDate, amountToPost,
                        interestToPost, feeToPost, penaltyToPost, externalId);
                updateLoanChargesPaidBy(finalAccrual, feeDetails, null);
                addLoanTransaction(finalAccrual);
            }
        }
        updateLoanOutstandingBalances();
    }

    private void determineCumulativeIncomeFromInstallments(HashMap<String, BigDecimal> cumulativeIncomeFromInstallments) {
        BigDecimal interest = BigDecimal.ZERO;
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal penalty = BigDecimal.ZERO;
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (LoanRepaymentScheduleInstallment installment : installments) {
            interest = interest.add(installment.getInterestCharged(getCurrency()).getAmount());
            fee = fee.add(installment.getFeeChargesCharged(getCurrency()).getAmount());
            penalty = penalty.add(installment.getPenaltyChargesCharged(getCurrency()).getAmount());
        }
        cumulativeIncomeFromInstallments.put(INTEREST, interest);
        cumulativeIncomeFromInstallments.put(FEE, fee);
        cumulativeIncomeFromInstallments.put(PENALTY, penalty);
    }

    private void determineCumulativeIncomeDetails(Collection<LoanTransaction> transactions, HashMap<String, BigDecimal> incomeDetailsMap) {
        BigDecimal interest = BigDecimal.ZERO;
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal penalty = BigDecimal.ZERO;
        for (LoanTransaction transaction : transactions) {
            interest = interest.add(transaction.getInterestPortion(getCurrency()).getAmount());
            fee = fee.add(transaction.getFeeChargesPortion(getCurrency()).getAmount());
            penalty = penalty.add(transaction.getPenaltyChargesPortion(getCurrency()).getAmount());
        }
        incomeDetailsMap.put(INTEREST, interest);
        incomeDetailsMap.put(FEE, fee);
        incomeDetailsMap.put(PENALTY, penalty);
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
                    && (currentTransactionDate.isBefore(previousTransaction.getTransactionDate())
                            || currentTransactionDate.isEqual(previousTransaction.getTransactionDate()))) {
                isChronologicallyLatestRepaymentOrWaiver = false;
                break;
            }
        }

        return isChronologicallyLatestRepaymentOrWaiver;
    }

    private boolean isAfterLatRepayment(final LoanTransaction loanTransaction, final List<LoanTransaction> loanTransactions) {

        boolean isAfterLatRepayment = true;

        final LocalDate currentTransactionDate = loanTransaction.getTransactionDate();
        for (final LoanTransaction previousTransaction : loanTransactions) {
            if (previousTransaction.isRepaymentLikeType() && previousTransaction.isNotReversed()
                    && currentTransactionDate.isBefore(previousTransaction.getTransactionDate())) {
                isAfterLatRepayment = false;
                break;
            }
        }
        return isAfterLatRepayment;
    }

    private boolean isChronologicallyLatestTransaction(final LoanTransaction loanTransaction,
            final List<LoanTransaction> loanTransactions) {

        boolean isChronologicallyLatestRepaymentOrWaiver = true;

        final LocalDate currentTransactionDate = loanTransaction.getTransactionDate();
        for (final LoanTransaction previousTransaction : loanTransactions) {
            if (previousTransaction.isNotReversed() && (currentTransactionDate.isBefore(previousTransaction.getTransactionDate())
                    || currentTransactionDate.isEqual(previousTransaction.getTransactionDate()))) {
                isChronologicallyLatestRepaymentOrWaiver = false;
                break;
            }
        }

        return isChronologicallyLatestRepaymentOrWaiver;
    }

    public LocalDate possibleNextRepaymentDate() {
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
        if (lastTransactionDate != null && lastTransactionDate.isAfter(earliestUnpaidInstallmentDate)) {
            possibleNextRepaymentDate = lastTransactionDate;
        }

        return possibleNextRepaymentDate;
    }

    public LoanRepaymentScheduleInstallment possibleNextRepaymentInstallment() {
        LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = null;
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            if (installment.isNotFullyPaidOff()) {
                loanRepaymentScheduleInstallment = installment;
                break;
            }
        }

        return loanRepaymentScheduleInstallment;
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

        if (isClosedObligationsMet() || isClosedWrittenOff() || isClosedWithOutsandingAmountMarkedForReschedule()) {
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
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsPostDisbursement();
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO);
        }
        ChangedTransactionDetail changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(
                getDisbursementDate(), allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(),
                getActiveCharges());
        updateLoanSummaryDerivedFields();
        return changedTransactionDetail;
    }

    public LoanTransaction findWriteOffTransaction() {

        LoanTransaction writeOff = null;
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (!transaction.isReversed() && transaction.isWriteOff()) {
                writeOff = transaction;
            }
        }

        return writeOff;
    }

    private boolean isOverPaid() {
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
            } else if (loanTransaction.isCreditBalanceRefund() || loanTransaction.isChargeback()) {
                totalPaidInRepayments = totalPaidInRepayments.minus(loanTransaction.getOverPaymentPortion(currency));
            }
        }

        // if total paid in transactions doesnt match repayment schedule then
        // theres an overpayment.
        return totalPaidInRepayments.minus(cumulativeTotalPaidOnInstallments);
    }

    public Money calculateTotalRecoveredPayments() {
        // in case logic for reversing recovered payment is implemented handle
        // subtraction from totalRecoveredPayments
        return getTotalRecoveredPayments();
    }

    private MonetaryCurrency loanCurrency() {
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

            if (writtenOffOnLocalDate.isBefore(getDisbursementDate())) {
                final String errorMessage = "The date on which a loan is written off cannot be before the loan disbursement date: "
                        + getDisbursementDate().toString();
                throw new InvalidLoanStateTransitionException("writeoff", "cannot.be.before.submittal.date", errorMessage,
                        writtenOffOnLocalDate, getDisbursementDate());
            }

            validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.WRITE_OFF_OUTSTANDING, writtenOffOnLocalDate);

            if (writtenOffOnLocalDate.isAfter(DateUtils.getBusinessLocalDate())) {
                final String errorMessage = "The date on which a loan is written off cannot be in the future.";
                throw new InvalidLoanStateTransitionException("writeoff", "cannot.be.a.future.date", errorMessage, writtenOffOnLocalDate);
            }

            loanTransaction = LoanTransaction.writeoff(this, getOffice(), writtenOffOnLocalDate, externalId);
            LocalDate lastTransactionDate = getLastUserTransactionDate();
            if (lastTransactionDate.isAfter(writtenOffOnLocalDate)) {
                final String errorMessage = "The date of the writeoff transaction must occur on or before previous transactions.";
                throw new InvalidLoanStateTransitionException("writeoff", "must.occur.on.or.after.other.transaction.dates", errorMessage,
                        writtenOffOnLocalDate);
            }

            addLoanTransaction(loanTransaction);
            loanRepaymentScheduleTransactionProcessor.handleWriteOff(loanTransaction, loanCurrency(), getRepaymentScheduleInstallments());

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
        if (isDisbursementAllowed() && atleastOnceDisbursed()) {
            this.loanRepaymentScheduleDetail.setPrincipal(getDisbursedAmount());
            removeDisbursementDetail();
            regenerateRepaymentSchedule(scheduleGeneratorDTO);
            if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO);
            }
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsPostDisbursement();
            changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(),
                    allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(), getActiveCharges());
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                mapEntry.getValue().updateLoan(this);
                addLoanTransaction(mapEntry.getValue());
            }
            updateLoanSummaryDerivedFields();
            LoanTransaction loanTransaction = getLatestTransaction();
            doPostLoanTransactionChecks(loanTransaction.getTransactionDate(), loanLifecycleStateMachine);
        }
        return changedTransactionDetail;
    }

    public LoanTransaction getLatestTransaction() {
        LoanTransaction transaction = null;
        for (LoanTransaction loanTransaction : this.loanTransactions) {
            if (!loanTransaction.isReversed()
                    && (transaction == null || transaction.getTransactionDate().isBefore(loanTransaction.getTransactionDate()))) {
                transaction = loanTransaction;
            }
        }
        return transaction;
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
        if (closureDate.isBefore(getDisbursementDate())) {
            final String errorMessage = "The date on which a loan is closed cannot be before the loan disbursement date: "
                    + getDisbursementDate().toString();
            throw new InvalidLoanStateTransitionException("close", "cannot.be.before.submittal.date", errorMessage, closureDate,
                    getDisbursementDate());
        }

        if (closureDate.isAfter(DateUtils.getBusinessLocalDate())) {
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
                loanRepaymentScheduleTransactionProcessor.handleWriteOff(loanTransaction, loanCurrency(),
                        getRepaymentScheduleInstallments());

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

        if (this.rescheduledOnDate.isBefore(getDisbursementDate())) {
            final String errorMessage = "The date on which a loan is rescheduled cannot be before the loan disbursement date: "
                    + getDisbursementDate().toString();
            throw new InvalidLoanStateTransitionException("close.reschedule", "cannot.be.before.submittal.date", errorMessage,
                    this.rescheduledOnDate, getDisbursementDate());
        }

        if (this.rescheduledOnDate.isAfter(DateUtils.getBusinessLocalDate())) {
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

    private boolean isNotDisbursed() {
        return !isDisbursed();
    }

    public boolean isChargesAdditionAllowed() {
        boolean isDisbursed = false;
        if (this.loanProduct.isMultiDisburseLoan()) {
            isDisbursed = !isDisbursementAllowed();
        } else {
            isDisbursed = hasDisbursementTransaction();
        }
        return isDisbursed;
    }

    public boolean isDisbursed() {
        return hasDisbursementTransaction();
    }

    public boolean isClosed() {
        return getStatus().isClosed() || isCancelled();
    }

    private boolean isClosedObligationsMet() {
        return getStatus().isClosedObligationsMet();
    }

    public boolean isClosedWrittenOff() {
        return getStatus().isClosedWrittenOff();
    }

    private boolean isClosedWithOutsandingAmountMarkedForReschedule() {
        return getStatus().isClosedWithOutsandingAmountMarkedForReschedule();
    }

    private boolean isCancelled() {
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

    private boolean isAllTranchesNotDisbursed() {
        LoanStatus actualLoanStatus = LoanStatus.fromInt(this.loanStatus);
        return this.loanProduct.isMultiDisburseLoan() && (actualLoanStatus.isActive() || actualLoanStatus.isApproved()
                || actualLoanStatus.isClosedObligationsMet() || actualLoanStatus.isOverpaid()) && isDisbursementAllowed();
    }

    private boolean hasDisbursementTransaction() {
        boolean hasRepaymentTransaction = false;
        for (final LoanTransaction loanTransaction : this.loanTransactions) {
            if (loanTransaction.isDisbursement() && loanTransaction.isNotReversed()) {
                hasRepaymentTransaction = true;
                break;
            }
        }
        return hasRepaymentTransaction;
    }

    public boolean isSubmittedOnDateAfter(final LocalDate compareDate) {
        return this.submittedOnDate != null && this.submittedOnDate.isAfter(compareDate);
    }

    public LocalDate getSubmittedOnDate() {
        return this.submittedOnDate;
    }

    public LocalDate getApprovedOnDate() {
        return this.approvedOnDate;
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

    public void setActualDisbursementDate(LocalDate actualDisbursementDate) {
        this.actualDisbursementDate = actualDisbursementDate;
    }

    public LocalDate getWrittenOffDate() {
        return this.writtenOffOnDate;
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

    public LocalDate getExpectedFirstRepaymentOnDate() {
        return this.expectedFirstRepaymentOnDate;
    }

    private boolean isActualDisbursedOnDateEarlierOrLaterThanExpected(final LocalDate actualDisbursedOnDate) {
        boolean isRegenerationRequired = false;
        if (this.loanProduct.isMultiDisburseLoan()) {
            LoanDisbursementDetails details = fetchLastDisburseDetail();
            if (details != null && details.expectedDisbursementDate().compareTo(details.actualDisbursementDate()) == 0 ? Boolean.FALSE
                    : Boolean.TRUE) {
                isRegenerationRequired = true;
            }
        }
        return !this.expectedDisbursementDate.isEqual(actualDisbursedOnDate) || isRegenerationRequired;
    }

    private boolean isRepaymentScheduleRegenerationRequiredForDisbursement(final LocalDate actualDisbursementDate) {
        return isActualDisbursedOnDateEarlierOrLaterThanExpected(actualDisbursementDate);
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

        boolean matchesCurrentLoanOfficer = false;
        if (this.loanOfficer != null) {
            matchesCurrentLoanOfficer = this.loanOfficer.identifiedBy(fromLoanOfficer);
        } else {
            matchesCurrentLoanOfficer = fromLoanOfficer == null;
        }

        return matchesCurrentLoanOfficer;
    }

    public LocalDate getInterestChargedFromDate() {
        return this.interestChargedFromDate;
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

        } else if (DateUtils.getBusinessLocalDate().isBefore(assignmentDate)) {

            final String errorMessage = "The Loan Officer assignment date (" + assignmentDate + ") cannot be in the future.";

            throw new LoanOfficerAssignmentDateException("cannot.be.a.future.date", errorMessage, assignmentDate);

        } else if (latestHistoryRecord != null && this.loanOfficer.identifiedBy(newLoanOfficer)) {
            latestHistoryRecord.updateStartDate(assignmentDate);
        } else if (latestHistoryRecord != null && latestHistoryRecord.matchesStartDateOf(assignmentDate)) {
            latestHistoryRecord.updateLoanOfficer(newLoanOfficer);
            this.loanOfficer = newLoanOfficer;
        } else if (latestHistoryRecord != null && latestHistoryRecord.hasStartDateBefore(assignmentDate)) {
            final String errorMessage = "Loan with identifier " + getId() + " was already assigned before date " + assignmentDate;
            throw new LoanOfficerAssignmentDateException("is.before.last.assignment.date", errorMessage, getId(), assignmentDate);
        } else {
            if (latestHistoryRecord != null) {
                // loan officer correctly changed from previous loan officer to
                // new loan officer
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

        final LocalDate today = DateUtils.getBusinessLocalDate();

        if (latestHistoryRecord.getStartDate().isAfter(unassignDate)) {

            final String errorMessage = "The Loan officer Unassign date(" + unassignDate + ") cannot be before its assignment date ("
                    + latestHistoryRecord.getStartDate() + ").";

            throw new LoanOfficerUnassignmentDateException("cannot.be.before.assignment.date", errorMessage, getId(),
                    getLoanOfficer().getId(), latestHistoryRecord.getStartDate(), unassignDate);

        } else if (unassignDate.isAfter(today)) {

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
        Long clientId = null;
        if (this.client != null) {
            clientId = this.client.getId();
        }
        return clientId;
    }

    public Long getGroupId() {
        Long groupId = null;
        if (this.group != null) {
            groupId = this.group.getId();
        }
        return groupId;
    }

    public Long getGlimId() {
        Long glimId = null;
        if (this.glim != null) {
            glimId = this.glim.getId();
        }
        return glimId;
    }

    public Long getOfficeId() {
        Long officeId = null;
        if (this.client != null) {
            officeId = this.client.officeId();
        } else {
            officeId = this.group.officeId();
        }
        return officeId;
    }

    public Office getOffice() {
        Office office = null;
        if (this.client != null) {
            office = this.client.getOffice();
        } else {
            office = this.group.getOffice();
        }
        return office;
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

    public Staff getLoanOfficer() {
        return this.loanOfficer;
    }

    public Set<LoanCollateral> getCollateral() {
        return this.collateral;
    }

    public BigDecimal getProposedPrincipal() {
        return this.proposedPrincipal;
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
                existingReversedTransactionIds, transaction -> transaction.getTransactionDate().isBefore(getChargedOffOnDate()));
        // On
        filterTransactionsByChargeOffDate(newLoanTransactionsBeforeChargeOff, newLoanTransactionsAfterChargeOff, currencyCode,
                existingTransactionIds, existingReversedTransactionIds,
                transaction -> transaction.getTransactionDate().isEqual(getChargedOffOnDate()));
        // After
        filterTransactionsByChargeOffDate(newLoanTransactionsAfterChargeOff, currencyCode, existingTransactionIds,
                existingReversedTransactionIds, transaction -> transaction.getTransactionDate().isAfter(getChargedOffOnDate()));
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
            accountingBridgeDataChargeOff.put("isFraud", false);
        } else {
            accountingBridgeDataChargeOff.put("isChargeOff", isChargedOff());
            accountingBridgeDataChargeOff.put("isFraud", isFraud());
        }
        return accountingBridgeDataChargeOff;
    }

    private void filterTransactionsByChargeOffDate(List<Map<String, Object>> filteredTransactions, final String currencyCode,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds,
            Predicate<LoanTransaction> chargeOffDateCriteria) {
        filteredTransactions.addAll(this.loanTransactions.stream().filter(chargeOffDateCriteria).filter(transaction -> {
            boolean isExistingTransaction = existingTransactionIds.contains(transaction.getId());
            boolean isExistingReversedTransaction = existingReversedTransactionIds.contains(transaction.getId());

            if (transaction.isReversed() && isExistingTransaction && !isExistingReversedTransaction) {
                return true;
            } else {
                return !isExistingTransaction;
            }
        }).map(transaction -> transaction.toMapData(currencyCode)).toList());
    }

    private void filterTransactionsByChargeOffDate(List<Map<String, Object>> newLoanTransactionsBeforeChargeOff,
            List<Map<String, Object>> newLoanTransactionsAfterChargeOff, String currencyCode, List<Long> existingTransactionIds,
            List<Long> existingReversedTransactionIds, Predicate<LoanTransaction> chargeOffDateCriteria) {

        LoanTransaction chargeOffTransaction = this.loanTransactions.stream().filter(LoanTransaction::isChargeOff)
                .filter(LoanTransaction::isNotReversed).findFirst().get();

        this.loanTransactions.stream().filter(chargeOffDateCriteria).forEach(transaction -> {
            boolean isExistingTransaction = existingTransactionIds.contains(transaction.getId());
            boolean isExistingReversedTransaction = existingReversedTransactionIds.contains(transaction.getId());
            List<Map<String, Object>> targetList = (transaction.getId().compareTo(chargeOffTransaction.getId()) < 0)
                    ? newLoanTransactionsBeforeChargeOff
                    : newLoanTransactionsAfterChargeOff;
            if ((transaction.isReversed() && isExistingTransaction && !isExistingReversedTransaction) || !isExistingTransaction) {
                targetList.add(transaction.toMapData(currencyCode));
            }
        });
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
                    && !transaction.getTransactionDate().isAfter(tillDate)) {
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

    public LocalDate getClosedOnDate() {
        return this.closedOnDate;
    }

    public void updateLoanRepaymentScheduleDates(final String recurringRule, final boolean isHolidayEnabled, final List<Holiday> holidays,
            final WorkingDays workingDays, final LocalDate presentMeetingDate, final LocalDate newMeetingDate,
            final boolean isSkipRepaymentOnFirstDayOfMonth, final Integer numberOfDays) {

        // first repayment's from date is same as disbursement date.
        /*
         * meetingStartDate is used as seedDate Capture the seedDate from user and use the seedDate as meetingStart date
         */

        LocalDate tmpFromDate = getDisbursementDate();
        final PeriodFrequencyType repaymentPeriodFrequencyType = this.loanRepaymentScheduleDetail.getRepaymentPeriodFrequencyType();
        final Integer loanRepaymentInterval = this.loanRepaymentScheduleDetail.getRepayEvery();
        final String frequency = CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(repaymentPeriodFrequencyType);

        LocalDate newRepaymentDate = null;
        Boolean isFirstTime = true;
        LocalDate latestRepaymentDate = null;
        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : installments) {

            LocalDate oldDueDate = loanRepaymentScheduleInstallment.getDueDate();

            if (oldDueDate.isEqual(presentMeetingDate) || oldDueDate.isAfter(presentMeetingDate)) {

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
                if (latestRepaymentDate == null || latestRepaymentDate.isBefore(newRepaymentDate)) {
                    latestRepaymentDate = newRepaymentDate;
                }
                loanRepaymentScheduleInstallment.updateDueDate(newRepaymentDate);
                // reset from date to get actual daysInPeriod

                if (!isFirstTime) {
                    loanRepaymentScheduleInstallment.updateFromDate(tmpFromDate);
                }

                tmpFromDate = newRepaymentDate;// update with new repayment
                // date
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

            if (oldDueDate.isAfter(seedDate) && oldDueDate.isAfter(DateUtils.getBusinessLocalDate())) {

                newRepaymentDate = CalendarUtils.getNewRepaymentMeetingDate(recuringRule, seedDate, oldDueDate, loanRepaymentInterval,
                        frequency, workingDays, isSkipRepaymentonfirstdayofmonth, numberofDays);

                final LocalDate maxDateLimitForNewRepayment = getMaxDateLimitForNewRepayment(repaymentPeriodFrequencyType,
                        loanRepaymentInterval, tmpFromDate);

                if (newRepaymentDate.isAfter(maxDateLimitForNewRepayment)) {
                    newRepaymentDate = CalendarUtils.getNextRepaymentMeetingDate(recuringRule, seedDate, tmpFromDate, loanRepaymentInterval,
                            frequency, workingDays, isSkipRepaymentonfirstdayofmonth, numberofDays);
                }

                if (isHolidayEnabled) {
                    newRepaymentDate = HolidayUtil.getRepaymentRescheduleDateToIfHoliday(newRepaymentDate, holidays);
                }
                if (latestRepaymentDate == null || latestRepaymentDate.isBefore(newRepaymentDate)) {
                    latestRepaymentDate = newRepaymentDate;
                }

                loanRepaymentScheduleInstallment.updateDueDate(newRepaymentDate);
                // reset from date to get actual daysInPeriod
                loanRepaymentScheduleInstallment.updateFromDate(tmpFromDate);
                tmpFromDate = newRepaymentDate;// update with new repayment
                // date
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
        final Integer repaidEvery = 2 * loanRepaymentInterval;
        switch (periodFrequencyType) {
            case DAYS:
                dueRepaymentPeriodDate = startDate.plusDays(repaidEvery);
            break;
            case WEEKS:
                dueRepaymentPeriodDate = startDate.plusWeeks(repaidEvery);
            break;
            case MONTHS:
                dueRepaymentPeriodDate = startDate.plusMonths(repaidEvery);
            break;
            case YEARS:
                dueRepaymentPeriodDate = startDate.plusYears(repaidEvery);
            break;
            case INVALID:
            break;
            case WHOLE_TERM:
            break;
        }
        return dueRepaymentPeriodDate.minusDays(1);// get 2n-1 range date from
                                                   // startDate
    }

    private void validateDisbursementDateIsOnNonWorkingDay(final WorkingDays workingDays, final boolean allowTransactionsOnNonWorkingDay) {
        if (!allowTransactionsOnNonWorkingDay && !WorkingDaysUtil.isWorkingDay(workingDays, getDisbursementDate())) {
            final String errorMessage = "Expected disbursement date cannot be on a non working day";
            throw new LoanApplicationDateException("disbursement.date.on.non.working.day", errorMessage, getExpectedDisbursedOnLocalDate());
        }
    }

    private void validateDisbursementDateIsOnHoliday(final boolean allowTransactionsOnHoliday, final List<Holiday> holidays) {
        if (!allowTransactionsOnHoliday && HolidayUtil.isHoliday(getDisbursementDate(), holidays)) {
            final String errorMessage = "Expected disbursement date cannot be on a holiday";
            throw new LoanApplicationDateException("disbursement.date.on.holiday", errorMessage, getExpectedDisbursedOnLocalDate());
        }
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
        this.loanRepaymentScheduleDetail.updateInterestPeriodFrequencyType(this.loanProduct.getInterestPeriodFrequencyType());
    }

    public Integer getTermFrequency() {
        return this.termFrequency;
    }

    public Integer getTermPeriodFrequencyType() {
        return this.termPeriodFrequencyType;
    }

    // This method returns copy of all transactions
    public List<LoanTransaction> getLoanTransactions() {
        return this.loanTransactions;
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

    public void validateExpectedDisbursementForHolidayAndNonWorkingDay(final WorkingDays workingDays,
            final boolean allowTransactionsOnHoliday, final List<Holiday> holidays, final boolean allowTransactionsOnNonWorkingDay) {
        // validate if disbursement date is a holiday or a non-working day
        validateDisbursementDateIsOnNonWorkingDay(workingDays, allowTransactionsOnNonWorkingDay);
        validateDisbursementDateIsOnHoliday(allowTransactionsOnHoliday, holidays);

    }

    private void validateActivityNotBeforeClientOrGroupTransferDate(final LoanEvent event, final LocalDate activityDate) {
        if (this.client != null && this.client.getOfficeJoiningLocalDate() != null) {
            final LocalDate clientOfficeJoiningDate = this.client.getOfficeJoiningLocalDate();
            if (activityDate.isBefore(clientOfficeJoiningDate)) {
                String errorMessage = null;
                String action = null;
                String postfix = null;
                switch (event) {
                    case LOAN_CREATED:
                        errorMessage = "The date on which a loan is submitted cannot be earlier than client's transfer date to this office";
                        action = "submittal";
                        postfix = "cannot.be.before.client.transfer.date";
                    break;
                    case LOAN_APPROVED:
                        errorMessage = "The date on which a loan is approved cannot be earlier than client's transfer date to this office";
                        action = "approval";
                        postfix = "cannot.be.before.client.transfer.date";
                    break;
                    case LOAN_APPROVAL_UNDO:
                        errorMessage = "The date on which a loan is approved cannot be earlier than client's transfer date to this office";
                        action = "approval";
                        postfix = "cannot.be.undone.before.client.transfer.date";
                    break;
                    case LOAN_DISBURSED:
                        errorMessage = "The date on which a loan is disbursed cannot be earlier than client's transfer date to this office";
                        action = "disbursal";
                        postfix = "cannot.be.before.client.transfer.date";
                    break;
                    case LOAN_DISBURSAL_UNDO:
                        errorMessage = "Cannot undo a disbursal done in another branch";
                        action = "disbursal";
                        postfix = "cannot.be.undone.before.client.transfer.date";
                    break;
                    case LOAN_REPAYMENT_OR_WAIVER:
                        errorMessage = "The date on which a repayment or waiver is made cannot be earlier than client's transfer date to this office";
                        action = "repayment.or.waiver";
                        postfix = "cannot.be.made.before.client.transfer.date";
                    break;
                    case LOAN_REJECTED:
                        errorMessage = "The date on which a loan is rejected cannot be earlier than client's transfer date to this office";
                        action = "reject";
                        postfix = "cannot.be.before.client.transfer.date";
                    break;
                    case LOAN_WITHDRAWN:
                        errorMessage = "The date on which a loan is withdrawn cannot be earlier than client's transfer date to this office";
                        action = "withdraw";
                        postfix = "cannot.be.before.client.transfer.date";
                    break;
                    case WRITE_OFF_OUTSTANDING:
                        errorMessage = "The date on which a write off is made cannot be earlier than client's transfer date to this office";
                        action = "writeoff";
                        postfix = "cannot.be.undone.before.client.transfer.date";
                    break;
                    case REPAID_IN_FULL:
                        errorMessage = "The date on which the loan is repaid in full cannot be earlier than client's transfer date to this office";
                        action = "close";
                        postfix = "cannot.be.undone.before.client.transfer.date";
                    break;
                    case LOAN_CHARGE_PAYMENT:
                        errorMessage = "The date on which a charge payment is made cannot be earlier than client's transfer date to this office";
                        action = "charge.payment";
                        postfix = "cannot.be.made.before.client.transfer.date";
                    break;
                    case LOAN_REFUND:
                        errorMessage = "The date on which a refund is made cannot be earlier than client's transfer date to this office";
                        action = "refund";
                        postfix = "cannot.be.made.before.client.transfer.date";
                    break;
                    case LOAN_DISBURSAL_UNDO_LAST:
                        errorMessage = "Cannot undo a last disbursal in another branch";
                        action = "disbursal";
                        postfix = "cannot.be.undone.before.client.transfer.date";
                    break;
                    default:
                    break;
                }
                throw new InvalidLoanStateTransitionException(action, postfix, errorMessage, clientOfficeJoiningDate);
            }
        }
    }

    private void validateActivityNotBeforeLastTransactionDate(final LoanEvent event, final LocalDate activityDate) {
        if (!(this.repaymentScheduleDetail().isInterestRecalculationEnabled() || this.loanProduct().isHoldGuaranteeFundsEnabled())) {
            return;
        }
        LocalDate lastTransactionDate = getLastUserTransactionDate();
        if (lastTransactionDate.isAfter(activityDate)) {
            String errorMessage = null;
            String action = null;
            String postfix = null;
            switch (event) {
                case LOAN_REPAYMENT_OR_WAIVER:
                    errorMessage = "The date on which a repayment or waiver is made cannot be earlier than last transaction date";
                    action = "repayment.or.waiver";
                    postfix = "cannot.be.made.before.last.transaction.date";
                break;
                case WRITE_OFF_OUTSTANDING:
                    errorMessage = "The date on which a write off is made cannot be earlier than last transaction date";
                    action = "writeoff";
                    postfix = "cannot.be.made.before.last.transaction.date";
                break;
                case LOAN_CHARGE_PAYMENT:
                    errorMessage = "The date on which a charge payment is made cannot be earlier than last transaction date";
                    action = "charge.payment";
                    postfix = "cannot.be.made.before.last.transaction.date";
                break;
                default:
                break;
            }
            throw new InvalidLoanStateTransitionException(action, postfix, errorMessage, lastTransactionDate);
        }
    }

    public void validateRepaymentTypeTransactionNotBeforeAChargeRefund(final LoanTransaction repaymentTransaction,
            final String reversedOrCreated) {
        if (repaymentTransaction.isRepaymentLikeType() && !repaymentTransaction.isChargeRefund()) {
            for (LoanTransaction txn : this.getLoanTransactions()) {
                if (txn.isChargeRefund() && repaymentTransaction.getTransactionDate().isBefore(txn.getTransactionDate())) {
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
            if (!(previousTransaction.isReversed() || previousTransaction.isAccrual() || previousTransaction.isIncomePosting())
                    && currentTransactionDate.isBefore(previousTransaction.getTransactionDate())) {
                currentTransactionDate = previousTransaction.getTransactionDate();
            }
        }
        return currentTransactionDate;
    }

    public LocalDate getLastRepaymentDate() {
        LocalDate currentTransactionDate = getDisbursementDate();
        for (final LoanTransaction previousTransaction : this.loanTransactions) {
            if (previousTransaction.isRepaymentLikeType() && currentTransactionDate.isBefore(previousTransaction.getTransactionDate())) {
                currentTransactionDate = previousTransaction.getTransactionDate();
            }
        }
        return currentTransactionDate;
    }

    public LoanTransaction getLastPaymentTransaction() {
        return loanTransactions.stream() //
                .filter(loanTransaction -> !loanTransaction.isReversed()) //
                .filter(LoanTransaction::isRepaymentLikeType) //
                .reduce((first, second) -> second) //
                .orElse(null);
    }

    public LoanTransaction getLastRepaymentTransaction() {
        return loanTransactions.stream() //
                .filter(loanTransaction -> !loanTransaction.isReversed()) //
                .filter(LoanTransaction::isRepayment) //
                .reduce((first, second) -> second) //
                .orElse(null);
    }

    public LocalDate getLastUserTransactionForChargeCalc() {
        LocalDate lastTransaction = getDisbursementDate();
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            lastTransaction = getLastUserTransactionDate();
        }
        return lastTransaction;
    }

    public Set<LoanCharge> getActiveCharges() {
        Set<LoanCharge> loanCharges = new HashSet<>();
        if (this.charges != null) {
            for (LoanCharge charge : this.charges) {
                if (charge.isActive()) {
                    loanCharges.add(charge);
                }
            }
        }
        return loanCharges;
    }

    public Set<LoanTrancheCharge> trancheCharges() {
        Set<LoanTrancheCharge> loanCharges = new HashSet<>();
        if (this.trancheCharges != null) {
            for (LoanTrancheCharge charge : this.trancheCharges) {
                loanCharges.add(charge);
            }
        }
        return loanCharges;
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
            case LOAN_APPROVED:
                if (!isSubmittedAndPendingApproval()) {
                    final String defaultUserMessage = "Loan Account Approval is not allowed. Loan Account is not in submitted and pending approval state.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.approve.account.is.not.submitted.and.pending.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_APPROVAL_UNDO:
                if (!isApproved()) {
                    final String defaultUserMessage = "Loan Account Undo Approval is not allowed. Loan Account is not in approved state.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.undo.approval.account.is.not.approved",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_DISBURSED:
                if ((!(isApproved() && isNotDisbursed()) && !this.loanProduct.isMultiDisburseLoan())
                        || (this.loanProduct.isMultiDisburseLoan() && !isAllTranchesNotDisbursed())) {
                    final String defaultUserMessage = "Loan Disbursal is not allowed. Loan Account is not in approved and not disbursed state.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.disbursal.account.is.not.approve.not.disbursed.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_DISBURSAL_UNDO:
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
            break;
            case LOAN_REPAYMENT_OR_WAIVER:
                if (!isOpen()) {
                    final String defaultUserMessage = "Loan Repayment (or its types) or Waiver is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.repayment.or.waiver.account.is.not.active", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_REJECTED:
                if (!isSubmittedAndPendingApproval()) {
                    final String defaultUserMessage = "Loan application cannot be rejected. Loan Account is not in Submitted and Pending approval state.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.reject.account.is.not.submitted.pending.approval.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_WITHDRAWN:
                if (!isSubmittedAndPendingApproval()) {
                    final String defaultUserMessage = "Loan application cannot be withdrawn. Loan Account is not in Submitted and Pending approval state.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.withdrawn.account.is.not.submitted.pending.approval.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case WRITE_OFF_OUTSTANDING:
                if (!isOpen()) {
                    final String defaultUserMessage = "Loan Written off is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.writtenoff.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case WRITE_OFF_OUTSTANDING_UNDO:
                if (!isClosedWrittenOff()) {
                    final String defaultUserMessage = "Loan Undo Written off is not allowed. Loan Account is not Written off.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.undo.writtenoff.account.is.not.written.off", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_CHARGE_PAYMENT:
                if (!isOpen()) {
                    final String defaultUserMessage = "Charge payment is not allowed. Loan Account is not Active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.charge.payment.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_CLOSED:
                if (!isOpen()) {
                    final String defaultUserMessage = "Closing Loan Account is not allowed. Loan Account is not Active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.close.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_EDIT_MULTI_DISBURSE_DATE:
                if (isClosed()) {
                    final String defaultUserMessage = "Edit disbursement is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.edit.disbursement.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_RECOVERY_PAYMENT:
                if (!isClosedWrittenOff()) {
                    final String defaultUserMessage = "Recovery repayments may only be made on loans which are written off";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.account.is.not.written.off",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_REFUND:
                if (!isOpen()) {
                    final String defaultUserMessage = "Loan Refund is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.refund.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_DISBURSAL_UNDO_LAST:
                if (!isOpen()) {
                    final String defaultUserMessage = "Loan Undo last disbursal is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.undo.last.disbursal.account.is.not.active", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_FORECLOSURE:
                if (!isOpen()) {
                    final String defaultUserMessage = "Loan foreclosure is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.foreclosure.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_CREDIT_BALANCE_REFUND:
                if (!getStatus().isOverpaid()) {
                    final String defaultUserMessage = "Loan Credit Balance Refund is not allowed. Loan Account is not Overpaid.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.credit.balance.refund.account.is.not.overpaid", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_CHARGE_ADJUSTMENT:
                if (!(getStatus().isActive() || getStatus().isClosedObligationsMet() || getStatus().isOverpaid())) {
                    final String defaultUserMessage = "Loan Charge Adjustment is not allowed. Loan Account must be either Active, Fully repaid or Overpaid.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.charge.adjustment.account.is.not.in.valid.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            default:
            break;
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
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsPostDisbursement();
        ChangedTransactionDetail changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(
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
            if (loanTermVariations.getTermType().isEMIAmountVariation() && !startDate.isAfter(loanTermVariations.getTermApplicableFrom())) {
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

    public BigDecimal getApprovedPrincipal() {
        return this.approvedPrincipal;
    }

    public BigDecimal getNetDisbursalAmount() {
        return netDisbursalAmount;
    }

    public BigDecimal deductFromNetDisbursalAmount(final BigDecimal subtrahend) {
        this.netDisbursalAmount = this.netDisbursalAmount.subtract(subtrahend);
        return netDisbursalAmount;
    }

    public void setNetDisbursalAmount(BigDecimal netDisbursalAmount) {
        this.netDisbursalAmount = netDisbursalAmount;
    }

    public BigDecimal getTotalOverpaid() {
        return this.totalOverpaid;
    }

    public LocalDate getOverpaidOnDate() {
        return this.overpaidOnDate;
    }

    public void updateIsInterestRecalculationEnabled() {
        this.loanRepaymentScheduleDetail.updateIsInterestRecalculationEnabled(isInterestRecalculationEnabledForProduct());
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

    public LocalDate getExpectedMaturityDate() {
        return this.expectedMaturityDate;
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

    public ChangedTransactionDetail handleRegenerateRepaymentScheduleWithInterestRecalculation(final ScheduleGeneratorDTO generatorDTO) {
        regenerateRepaymentScheduleWithInterestRecalculation(generatorDTO);
        return processTransactions();

    }

    public ChangedTransactionDetail processTransactions() {
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsPostDisbursement();
        ChangedTransactionDetail changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(
                getDisbursementDate(), allNonContraTransactionsPostDisbursement, getCurrency(), getRepaymentScheduleInstallments(),
                getActiveCharges());
        for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
            mapEntry.getValue().updateLoan(this);
        }
        /***
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
        updateLoanSchedule(loanSchedule.getInstallments());
        this.interestRecalculatedOn = DateUtils.getBusinessLocalDate();
        LocalDate lastRepaymentDate = this.getLastRepaymentPeriodDueDate(true);
        Set<LoanCharge> charges = this.getActiveCharges();
        for (final LoanCharge loanCharge : charges) {
            if (!loanCharge.isDueAtDisbursement()) {
                updateOverdueScheduleInstallment(loanCharge);
                if (loanCharge.getDueLocalDate() == null || !lastRepaymentDate.isBefore(loanCharge.getDueLocalDate())) {
                    if ((loanCharge.isInstalmentFee() || !loanCharge.isWaived())
                            && (loanCharge.getDueLocalDate() == null || !lastTransactionDate.isAfter(loanCharge.getDueLocalDate()))) {
                        recalculateLoanCharge(loanCharge, generatorDTO.getPenaltyWaitPeriod());
                        loanCharge.updateWaivedAmount(getCurrency());
                    }
                } else {
                    loanCharge.setActive(false);
                }
            }
        }

        processPostDisbursementTransactions();
        processIncomeTransactions();
    }

    private void updateLoanChargesPaidBy(LoanTransaction accrual, HashMap<String, Object> feeDetails,
            LoanRepaymentScheduleInstallment installment) {
        @SuppressWarnings("unchecked")
        List<LoanCharge> loanCharges = (List<LoanCharge>) feeDetails.get("loanCharges");
        @SuppressWarnings("unchecked")
        List<LoanInstallmentCharge> loanInstallmentCharges = (List<LoanInstallmentCharge>) feeDetails.get("loanInstallmentCharges");
        if (loanCharges != null) {
            for (LoanCharge loanCharge : loanCharges) {
                Integer installmentNumber = null == installment ? null : installment.getInstallmentNumber();
                final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(accrual, loanCharge,
                        loanCharge.getAmount(getCurrency()).getAmount(), installmentNumber);
                accrual.getLoanChargesPaid().add(loanChargePaidBy);
            }
        }
        if (loanInstallmentCharges != null) {
            for (LoanInstallmentCharge loanInstallmentCharge : loanInstallmentCharges) {
                Integer installmentNumber = null == loanInstallmentCharge.getInstallment() ? null
                        : loanInstallmentCharge.getInstallment().getInstallmentNumber();
                final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(accrual, loanInstallmentCharge.getLoanCharge(),
                        loanInstallmentCharge.getAmount(getCurrency()).getAmount(), installmentNumber);
                accrual.getLoanChargesPaid().add(loanChargePaidBy);
            }
        }
    }

    public void processIncomeTransactions() {
        if (this.loanInterestRecalculationDetails != null && this.loanInterestRecalculationDetails.isCompoundingToBePostedAsTransaction()) {
            LocalDate lastCompoundingDate = this.getDisbursementDate();
            List<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails = extractInterestRecalculationAdditionalDetails();
            List<LoanTransaction> incomeTransactions = retrieveListOfIncomePostingTransactions();
            List<LoanTransaction> accrualTransactions = retrieveListOfAccrualTransactions();
            for (LoanInterestRecalcualtionAdditionalDetails compoundingDetail : compoundingDetails) {
                if (!compoundingDetail.getEffectiveDate().isBefore(DateUtils.getBusinessLocalDate())) {
                    break;
                }
                LoanTransaction incomeTransaction = getTransactionForDate(incomeTransactions, compoundingDetail.getEffectiveDate());
                LoanTransaction accrualTransaction = getTransactionForDate(accrualTransactions, compoundingDetail.getEffectiveDate());
                addUpdateIncomeAndAccrualTransaction(compoundingDetail, lastCompoundingDate, incomeTransaction, accrualTransaction);
                lastCompoundingDate = compoundingDetail.getEffectiveDate();
            }
            List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
            LoanRepaymentScheduleInstallment lastInstallment = installments.get(installments.size() - 1);
            reverseTransactionsPostEffectiveDate(incomeTransactions, lastInstallment.getDueDate());
            reverseTransactionsPostEffectiveDate(accrualTransactions, lastInstallment.getDueDate());
        }
    }

    private void reverseTransactionsOnOrAfter(List<LoanTransaction> transactions, LocalDate date) {
        for (LoanTransaction loanTransaction : transactions) {
            if (!loanTransaction.getTransactionDate().isBefore(date)) {
                loanTransaction.reverse();
            }
        }
    }

    private void addUpdateIncomeAndAccrualTransaction(LoanInterestRecalcualtionAdditionalDetails compoundingDetail,
            LocalDate lastCompoundingDate, LoanTransaction existingIncomeTransaction, LoanTransaction existingAccrualTransaction) {
        BigDecimal interest = BigDecimal.ZERO;
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal penalties = BigDecimal.ZERO;
        HashMap<String, Object> feeDetails = new HashMap<>();

        if (this.loanInterestRecalculationDetails.getInterestRecalculationCompoundingMethod()
                .equals(InterestRecalculationCompoundingMethod.INTEREST)) {
            interest = compoundingDetail.getAmount();
        } else if (this.loanInterestRecalculationDetails.getInterestRecalculationCompoundingMethod()
                .equals(InterestRecalculationCompoundingMethod.FEE)) {
            determineFeeDetails(lastCompoundingDate, compoundingDetail.getEffectiveDate(), feeDetails);
            fee = (BigDecimal) feeDetails.get(FEE);
            penalties = (BigDecimal) feeDetails.get(PENALTIES);
        } else if (this.loanInterestRecalculationDetails.getInterestRecalculationCompoundingMethod()
                .equals(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE)) {
            determineFeeDetails(lastCompoundingDate, compoundingDetail.getEffectiveDate(), feeDetails);
            fee = (BigDecimal) feeDetails.get(FEE);
            penalties = (BigDecimal) feeDetails.get(PENALTIES);
            interest = compoundingDetail.getAmount().subtract(fee).subtract(penalties);
        }

        ExternalId externalId = ExternalId.empty();
        if (TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }

        if (existingIncomeTransaction == null) {
            LoanTransaction transaction = LoanTransaction.incomePosting(this, this.getOffice(), compoundingDetail.getEffectiveDate(),
                    compoundingDetail.getAmount(), interest, fee, penalties, externalId);
            addLoanTransaction(transaction);
        } else if (existingIncomeTransaction.getAmount(getCurrency()).getAmount().compareTo(compoundingDetail.getAmount()) != 0) {
            existingIncomeTransaction.reverse();
            LoanTransaction transaction = LoanTransaction.incomePosting(this, this.getOffice(), compoundingDetail.getEffectiveDate(),
                    compoundingDetail.getAmount(), interest, fee, penalties, externalId);
            addLoanTransaction(transaction);
        }

        if (TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }

        if (isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            if (existingAccrualTransaction == null) {
                LoanTransaction accrual = LoanTransaction.accrueTransaction(this, this.getOffice(), compoundingDetail.getEffectiveDate(),
                        compoundingDetail.getAmount(), interest, fee, penalties, externalId);
                updateLoanChargesPaidBy(accrual, feeDetails, null);
                addLoanTransaction(accrual);
            } else if (existingAccrualTransaction.getAmount(getCurrency()).getAmount().compareTo(compoundingDetail.getAmount()) != 0) {
                existingAccrualTransaction.reverse();
                LoanTransaction accrual = LoanTransaction.accrueTransaction(this, this.getOffice(), compoundingDetail.getEffectiveDate(),
                        compoundingDetail.getAmount(), interest, fee, penalties, externalId);
                updateLoanChargesPaidBy(accrual, feeDetails, null);
                addLoanTransaction(accrual);
            }
        }
        updateLoanOutstandingBalances();
    }

    private void determineFeeDetails(LocalDate fromDate, LocalDate toDate, HashMap<String, Object> feeDetails) {
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal penalties = BigDecimal.ZERO;

        List<Integer> installments = new ArrayList<>();
        List<LoanRepaymentScheduleInstallment> repaymentSchedule = getRepaymentScheduleInstallments();
        for (LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : repaymentSchedule) {
            if (loanRepaymentScheduleInstallment.getDueDate().isAfter(fromDate)
                    && !loanRepaymentScheduleInstallment.getDueDate().isAfter(toDate)) {
                installments.add(loanRepaymentScheduleInstallment.getInstallmentNumber());
            }
        }

        List<LoanCharge> loanCharges = new ArrayList<>();
        List<LoanInstallmentCharge> loanInstallmentCharges = new ArrayList<>();
        for (LoanCharge loanCharge : this.getActiveCharges()) {
            boolean isDue = fromDate.isEqual(this.getDisbursementDate())
                    ? loanCharge.isDueForCollectionFromIncludingAndUpToAndIncluding(fromDate, toDate)
                    : loanCharge.isDueForCollectionFromAndUpToAndIncluding(fromDate, toDate);
            if (isDue) {
                if (loanCharge.isPenaltyCharge() && !loanCharge.isInstalmentFee()) {
                    penalties = penalties.add(loanCharge.amount());
                    loanCharges.add(loanCharge);
                } else if (!loanCharge.isInstalmentFee()) {
                    fee = fee.add(loanCharge.amount());
                    loanCharges.add(loanCharge);
                }
            } else if (loanCharge.isInstalmentFee()) {
                for (LoanInstallmentCharge installmentCharge : loanCharge.installmentCharges()) {
                    if (installments.contains(installmentCharge.getRepaymentInstallment().getInstallmentNumber())) {
                        fee = fee.add(installmentCharge.getAmount());
                        loanInstallmentCharges.add(installmentCharge);
                    }
                }
            }
        }

        feeDetails.put(FEE, fee);
        feeDetails.put(PENALTIES, penalties);
        feeDetails.put("loanCharges", loanCharges);
        feeDetails.put("loanInstallmentCharges", loanInstallmentCharges);
    }

    private LoanTransaction getTransactionForDate(List<LoanTransaction> transactions, LocalDate effectiveDate) {
        for (LoanTransaction loanTransaction : transactions) {
            if (loanTransaction.getTransactionDate().isEqual(effectiveDate)) {
                return loanTransaction;
            }
        }
        return null;
    }

    private void reverseTransactionsPostEffectiveDate(List<LoanTransaction> transactions, LocalDate effectiveDate) {
        for (LoanTransaction loanTransaction : transactions) {
            if (loanTransaction.getTransactionDate().isAfter(effectiveDate)) {
                loanTransaction.reverse();
            }
        }
    }

    private List<LoanInterestRecalcualtionAdditionalDetails> extractInterestRecalculationAdditionalDetails() {
        List<LoanInterestRecalcualtionAdditionalDetails> retDetails = new ArrayList<>();
        List<LoanRepaymentScheduleInstallment> repaymentSchedule = getRepaymentScheduleInstallments();
        if (null != repaymentSchedule) {
            for (LoanRepaymentScheduleInstallment installment : repaymentSchedule) {
                if (null != installment.getLoanCompoundingDetails()) {
                    retDetails.addAll(installment.getLoanCompoundingDetails());
                }
            }
        }
        retDetails.sort(Comparator.comparing(LoanInterestRecalcualtionAdditionalDetails::getEffectiveDate));
        return retDetails;
    }

    public void processPostDisbursementTransactions() {
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsPostDisbursement();
        final List<LoanTransaction> copyTransactions = new ArrayList<>();
        if (!allNonContraTransactionsPostDisbursement.isEmpty()) {
            for (LoanTransaction loanTransaction : allNonContraTransactionsPostDisbursement) {
                copyTransactions.add(LoanTransaction.copyTransactionProperties(loanTransaction));
            }
            loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(), copyTransactions, getCurrency(),
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
        final LoanScheduleGenerator loanScheduleGenerator = generatorDTO.getLoanScheduleFactory().create(interestMethod);

        final RoundingMode roundingMode = MoneyHelper.getRoundingMode();
        final MathContext mc = new MathContext(19, roundingMode);

        final LoanApplicationTerms loanApplicationTerms = constructLoanApplicationTerms(generatorDTO);

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategyCode);

        return loanScheduleGenerator.rescheduleNextInstallments(mc, loanApplicationTerms, this, generatorDTO.getHolidayDetailDTO(),
                loanRepaymentScheduleTransactionProcessor, generatorDTO.getRecalculateFrom());
    }

    public LoanRepaymentScheduleInstallment fetchPrepaymentDetail(final ScheduleGeneratorDTO scheduleGeneratorDTO, final LocalDate onDate) {
        LoanRepaymentScheduleInstallment installment = null;

        if (this.loanRepaymentScheduleDetail.isInterestRecalculationEnabled()) {
            final RoundingMode roundingMode = MoneyHelper.getRoundingMode();
            final MathContext mc = new MathContext(8, roundingMode);

            final InterestMethod interestMethod = this.loanRepaymentScheduleDetail.getInterestMethod();
            final LoanApplicationTerms loanApplicationTerms = constructLoanApplicationTerms(scheduleGeneratorDTO);

            final LoanScheduleGenerator loanScheduleGenerator = scheduleGeneratorDTO.getLoanScheduleFactory().create(interestMethod);
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
                scheduleGeneratorDTO.isPrincipalCompoundingDisabledForOverdueLoans());
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

    public LocalDate getAccruedTill() {
        return this.accruedTill;
    }

    public LocalDate fetchInterestRecalculateFromDate() {
        LocalDate interestRecalculatedOn = null;
        if (this.interestRecalculatedOn == null) {
            interestRecalculatedOn = getDisbursementDate();
        } else {
            interestRecalculatedOn = this.interestRecalculatedOn;
        }
        return interestRecalculatedOn;
    }

    private void updateLoanOutstandingBalances() {
        Money outstanding = Money.zero(getCurrency());
        List<LoanTransaction> loanTransactions = retrieveListOfTransactionsExcludeAccruals();
        for (LoanTransaction loanTransaction : loanTransactions) {
            if (loanTransaction.isDisbursement() || loanTransaction.isIncomePosting()) {
                outstanding = outstanding.plus(loanTransaction.getAmount(getCurrency()));
                loanTransaction.updateOutstandingLoanBalance(outstanding.getAmount());
            } else if (loanTransaction.isChargeback() || loanTransaction.isCreditBalanceRefund()) {
                Money transactionOutstanding = loanTransaction.getAmount(getCurrency());
                if (!loanTransaction.getOverPaymentPortion(getCurrency()).isZero()) {
                    transactionOutstanding = loanTransaction.getAmount(getCurrency())
                            .minus(loanTransaction.getOverPaymentPortion(getCurrency()));
                    if (transactionOutstanding.isLessThanZero()) {
                        transactionOutstanding = Money.zero(getCurrency());
                    }
                }
                outstanding = outstanding.plus(transactionOutstanding);
                loanTransaction.updateOutstandingLoanBalance(outstanding.getAmount());

            } else {
                if (this.loanInterestRecalculationDetails != null
                        && this.loanInterestRecalculationDetails.isCompoundingToBePostedAsTransaction()
                        && !loanTransaction.isRepaymentAtDisbursement()) {
                    outstanding = outstanding.minus(loanTransaction.getAmount(getCurrency()));
                } else {
                    outstanding = outstanding.minus(loanTransaction.getPrincipalPortion(getCurrency()));
                }
                loanTransaction.updateOutstandingLoanBalance(outstanding.getAmount());
            }
        }
    }

    public String transactionProcessingStrategy() {
        return this.transactionProcessingStrategyCode;
    }

    public boolean isNpa() {
        return this.isNpa;
    }

    /**
     * @return List of loan repayments schedule objects
     **/
    public List<LoanRepaymentScheduleInstallment> getRepaymentScheduleInstallments() {
        return this.repaymentScheduleInstallments;
    }

    public Integer getLoanRepaymentScheduleInstallmentsSize() {
        return this.repaymentScheduleInstallments.size();
    }

    public void addLoanRepaymentScheduleInstallment(final LoanRepaymentScheduleInstallment installment) {
        installment.updateLoan(this);
        this.repaymentScheduleInstallments.add(installment);
    }

    /**
     * @return Loan product minimum repayments schedule related detail
     **/
    public LoanProductRelatedDetail getLoanRepaymentScheduleDetail() {
        return this.loanRepaymentScheduleDetail;
    }

    /**
     * @return Loan Fixed Emi amount
     **/
    public BigDecimal getFixedEmiAmount() {
        return this.fixedEmiAmount;
    }

    /**
     * @return maximum outstanding loan balance
     **/
    public BigDecimal getMaxOutstandingLoanBalance() {
        return this.maxOutstandingLoanBalance;
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

                if (repaymentScheduleInstallment.getDueDate().isEqual(dueDate)) {
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
     * @param applicationCurrency
     * @param restCalendarInstance
     *            TODO
     * @param compoundingCalendarInstance
     *            TODO
     * @param loanCalendar
     * @param floatingRateDTO
     *            TODO
     * @param isSkipRepaymentonmonthFirst
     * @param numberofdays
     * @param holidayDetailDTO
     *            Used for accessing the loan's calendar object
     * @return application terms of the Loan object
     **/
    @SuppressWarnings({ "unused" })
    public LoanApplicationTerms getLoanApplicationTerms(final ApplicationCurrency applicationCurrency,
            final CalendarInstance restCalendarInstance, CalendarInstance compoundingCalendarInstance, final Calendar loanCalendar,
            final FloatingRateDTO floatingRateDTO, final boolean isSkipRepaymentonmonthFirst, final Integer numberofdays,
            final HolidayDetailDTO holidayDetailDTO) {
        LoanProduct loanProduct = loanProduct();
        // LoanProductRelatedDetail loanProductRelatedDetail =
        // getLoanRepaymentScheduleDetail();
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

        final Integer graceOnPrincipalPayment = this.loanRepaymentScheduleDetail.graceOnPrincipalPayment();
        final Integer graceOnInterestPayment = this.loanRepaymentScheduleDetail.graceOnInterestPayment();
        final Integer graceOnInterestCharged = this.loanRepaymentScheduleDetail.graceOnInterestCharged();
        final LocalDate interestChargedFromDate = getInterestChargedFromDate();
        final Integer graceOnArrearsAgeing = this.loanRepaymentScheduleDetail.getGraceOnDueDate();

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
                false, this.fixedPrincipalPercentagePerInstallment, false);
    }

    /**
     * @return Loan summary embedded object
     **/
    public LoanSummary getLoanSummary() {
        return this.summary;
    }

    public void updateRescheduledByUser(AppUser rescheduledByUser) {
        this.rescheduledByUser = rescheduledByUser;
    }

    public LoanProductRelatedDetail getLoanProductRelatedDetail() {
        return this.loanRepaymentScheduleDetail;
    }

    public void updateNumberOfRepayments(Integer numberOfRepayments) {
        this.loanRepaymentScheduleDetail.updateNumberOfRepayments(numberOfRepayments);
    }

    public void updateRescheduledOnDate(LocalDate rescheduledOnDate) {

        if (rescheduledOnDate != null) {
            this.rescheduledOnDate = rescheduledOnDate;
        }
    }

    public void updateTermFrequency(Integer termFrequency) {

        if (termFrequency != null) {
            this.termFrequency = termFrequency;
        }
    }

    public boolean isFeeCompoundingEnabledForInterestRecalculation() {
        boolean isEnabled = false;
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            isEnabled = this.loanInterestRecalculationDetails.getInterestRecalculationCompoundingMethod().isFeeCompoundingEnabled();
        }
        return isEnabled;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public ExternalId getExternalId() {
        return this.externalId;
    }

    public Client getClient() {
        return this.client;
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

        if (possibleNextRefundDate == null || refundTransactionDate.isBefore(possibleNextRefundDate)) {
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

        final LocalDate loanTransactionDate = loanTransaction.getTransactionDate();
        if (loanTransactionDate.isBefore(getDisbursementDate())) {
            final String errorMessage = "The transaction date cannot be before the loan disbursement date: "
                    + getDisbursementDate().toString();
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.before.disbursement.date", errorMessage,
                    loanTransactionDate, getDisbursementDate());
        }

        if (loanTransactionDate.isAfter(DateUtils.getBusinessLocalDate())) {
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

        // If is a refund
        if (adjustedTransaction == null) {
            loanRepaymentScheduleTransactionProcessor.handleRefund(loanTransaction, getCurrency(), getRepaymentScheduleInstallments(),
                    getActiveCharges());
        } else {
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retrieveListOfTransactionsPostDisbursement();
            changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(),
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
        final Money overpaidAmount = MathUtil.negativeToZero(calculateTotalOverpayment()); // Before Transaction

        if (chargebackTransaction.isNotZero(loanCurrency())) {
            addLoanTransaction(chargebackTransaction);
        }
        loanRepaymentScheduleTransactionProcessor.handleChargeback(chargebackTransaction, getCurrency(), overpaidAmount,
                getRepaymentScheduleInstallments());

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
            if (lastTransactionDate.isBefore(previousTransaction.getTransactionDate()) && (previousTransaction.isRepaymentLikeType()
                    || previousTransaction.isWaiver() || previousTransaction.isChargePayment())) {
                throw new UndoLastTrancheDisbursementException(previousTransaction.getId());
            }
            if (previousTransaction.getId().compareTo(lastDisbursalTransaction.getId()) < 0) {
                break;
            }
        }
        final LoanDisbursementDetails disbursementDetail = loan.getDisbursementDetails(lastTransactionDate,
                lastDisbursalTransaction.getAmount());
        updateLoanToLastDisbursalState(disbursementDetail);
        for (Iterator<LoanTermVariations> iterator = this.loanTermVariations.iterator(); iterator.hasNext();) {
            LoanTermVariations loanTermVariations = iterator.next();
            if ((loanTermVariations.getTermType().isDueDateVariation() && loanTermVariations.fetchDateValue().isAfter(lastTransactionDate))
                    || (loanTermVariations.getTermType().isEMIAmountVariation()
                            && loanTermVariations.getTermApplicableFrom().compareTo(lastTransactionDate) == 0 ? Boolean.TRUE
                                    : Boolean.FALSE)
                    || loanTermVariations.getTermApplicableFrom().isAfter(lastTransactionDate)) {
                iterator.remove();
            }
        }
        reverseExistingTransactionsTillLastDisbursal(lastDisbursalTransaction);
        loan.recalculateScheduleFromLastTransaction(scheduleGeneratorDTO, existingTransactionIds, existingReversedTransactionIds);
        actualChanges.put("undolastdisbursal", "true");
        actualChanges.put("disbursedAmount", this.getDisbursedAmount());
        updateLoanSummaryDerivedFields();

        doPostLoanTransactionChecks(getLastUserTransactionDate(), loanLifecycleStateMachine);

        return actualChanges;
    }

    /**
     * Reverse only disbursement, accruals, and repayments at disbursal transactions
     *
     * @param lastDisbursalTransaction
     * @return
     */
    public void reverseExistingTransactionsTillLastDisbursal(LoanTransaction lastDisbursalTransaction) {
        for (final LoanTransaction transaction : this.loanTransactions) {
            if ((transaction.getTransactionDate().compareTo(lastDisbursalTransaction.getTransactionDate()) >= 0)
                    && (transaction.getId().compareTo(lastDisbursalTransaction.getId()) >= 0)
                    && transaction.isAllowTypeTransactionAtTheTimeOfLastUndo()) {
                transaction.reverse();
            }
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

    public Boolean getIsFloatingInterestRate() {
        return this.isFloatingInterestRate;
    }

    public BigDecimal getInterestRateDifferential() {
        return this.interestRateDifferential;
    }

    public void setIsFloatingInterestRate(Boolean isFloatingInterestRate) {
        this.isFloatingInterestRate = isFloatingInterestRate;
    }

    public void setInterestRateDifferential(BigDecimal interestRateDifferential) {
        this.interestRateDifferential = interestRateDifferential;
    }

    public List<LoanTermVariations> getLoanTermVariations() {
        return this.loanTermVariations;
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

    public void setExpectedFirstRepaymentOnDate(LocalDate expectedFirstRepaymentOnDate) {
        this.expectedFirstRepaymentOnDate = expectedFirstRepaymentOnDate;
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
                    if (installment.getDueDate().isEqual(loanDisbursementDetail.expectedDisbursementDateAsLocalDate())
                            || (installment.getDueDate().isAfter(loanDisbursementDetail.expectedDisbursementDateAsLocalDate())
                                    && installment.isNotFullyPaidOff())) {
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
            // If charge type is specified due date and loan is multi disburment
            // loan.
            // Then we need to get as of this loan charge due date how much
            // amount disbursed.
            if (loanCharge.isSpecifiedDueDate() && this.isMultiDisburmentLoan()) {
                for (final LoanDisbursementDetails loanDisbursementDetails : this.getDisbursementDetails()) {
                    if (!loanDisbursementDetails.expectedDisbursementDate().isAfter(loanCharge.getDueDate())) {
                        amount = amount.add(loanDisbursementDetails.principal());
                    }
                }
            } else {
                amount = getPrincipal().getAmount();
            }
        }
        return amount;
    }

    public void updatePostDatedChecks(final List<PostDatedChecks> postDatedChecks) {
        this.postDatedChecks = postDatedChecks;
    }

    public List<PostDatedChecks> getPostDatedChecks() {
        return this.postDatedChecks;
    }

    public void updateWriteOffReason(CodeValue writeOffReason) {
        this.writeOffReason = writeOffReason;
    }

    public Group getGroup() {
        return group;
    }

    public LoanProduct getLoanProduct() {
        return loanProduct;
    }

    public LoanRepaymentScheduleInstallment fetchLoanForeclosureDetail(final LocalDate closureDate) {
        Money[] receivables = retriveIncomeOutstandingTillDate(closureDate);
        Money totalPrincipal = Money.of(getCurrency(), this.getLoanSummary().getTotalPrincipalOutstanding());
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
        for (final LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
            if (!installment.getDueDate().isAfter(paymentDate)) {
                interest = interest.plus(installment.getInterestOutstanding(currency));
                penalty = penalty.plus(installment.getPenaltyChargesOutstanding(currency));
                fee = fee.plus(installment.getFeeChargesOutstanding(currency));
            } else if (installment.getFromDate().isBefore(paymentDate)) {
                Money[] balancesForCurrentPeroid = fetchInterestFeeAndPenaltyTillDate(paymentDate, currency, installment);
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
            } else if (installment.getDueDate().isAfter(paymentDate)) {
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
            final LoanRepaymentScheduleInstallment installment) {
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
                boolean isDue = installment.isFirstPeriod()
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
        for (final LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
            if (installment.getDueDate().isEqual(paymentDate)) {
                Money interest = installment.getInterestCharged(currency);
                Money fee = installment.getFeeChargesCharged(currency);
                Money penalty = installment.getPenaltyChargesCharged(currency);
                balances[0] = interest;
                balances[1] = fee;
                balances[2] = penalty;
                break;
            } else if (installment.getDueDate().isAfter(paymentDate) && installment.getFromDate().isBefore(paymentDate)) {
                balances = fetchInterestFeeAndPenaltyTillDate(paymentDate, currency, installment);
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

    public Money[] getReceivableIncome(final LocalDate tillDate) {
        MonetaryCurrency currency = getCurrency();
        Money receivableInterest = Money.zero(currency);
        Money receivableFee = Money.zero(currency);
        Money receivablePenalty = Money.zero(currency);
        Money[] receivables = new Money[3];
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isNotReversed() && !transaction.isRepaymentAtDisbursement() && !transaction.isDisbursement()
                    && !transaction.getTransactionDate().isAfter(tillDate)) {
                if (transaction.isAccrual()) {
                    receivableInterest = receivableInterest.plus(transaction.getInterestPortion(currency));
                    receivableFee = receivableFee.plus(transaction.getFeeChargesPortion(currency));
                    receivablePenalty = receivablePenalty.plus(transaction.getPenaltyChargesPortion(currency));
                } else if (transaction.isRepaymentLikeType() || transaction.isChargePayment()) {
                    receivableInterest = receivableInterest.minus(transaction.getInterestPortion(currency));
                    receivableFee = receivableFee.minus(transaction.getFeeChargesPortion(currency));
                    receivablePenalty = receivablePenalty.minus(transaction.getPenaltyChargesPortion(currency));
                }
            }
            if (receivableInterest.isLessThanZero()) {
                receivableInterest = receivableInterest.zero();
            }
            if (receivableFee.isLessThanZero()) {
                receivableFee = receivableFee.zero();
            }
            if (receivablePenalty.isLessThanZero()) {
                receivablePenalty = receivablePenalty.zero();
            }
        }
        receivables[0] = receivableInterest;
        receivables[1] = receivableFee;
        receivables[2] = receivablePenalty;
        return receivables;
    }

    public void reverseAccrualsAfter(final LocalDate tillDate) {
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isAccrual() && transaction.getTransactionDate().isAfter(tillDate)) {
                transaction.reverse();
            }
        }
    }

    public ChangedTransactionDetail handleForeClosureTransactions(final LoanTransaction repaymentTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final ScheduleGeneratorDTO scheduleGeneratorDTO) {

        LoanEvent event = LoanEvent.LOAN_FORECLOSURE;
        validateAccountStatus(event);
        validateForForeclosure(repaymentTransaction.getTransactionDate());
        this.loanSubStatus = LoanSubStatus.FORECLOSED.getValue();
        applyAccurals();
        return handleRepaymentOrRecoveryOrWaiverTransaction(repaymentTransaction, loanLifecycleStateMachine, null, scheduleGeneratorDTO);
    }

    public Money retrieveAccruedAmountAfterDate(final LocalDate tillDate) {
        Money totalAmountAccrued = Money.zero(getCurrency());
        Money actualAmountTobeAccrued = Money.zero(getCurrency());
        for (final LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
            totalAmountAccrued = totalAmountAccrued.plus(installment.getInterestAccrued(getCurrency()));

            if (tillDate.isAfter(installment.getFromDate()) && tillDate.isBefore(installment.getDueDate())) {
                int daysInPeriod = Math.toIntExact(ChronoUnit.DAYS.between(installment.getFromDate(), installment.getDueDate()));
                int tillDays = Math.toIntExact(ChronoUnit.DAYS.between(installment.getFromDate(), tillDate));
                double interest = calculateInterestForDays(daysInPeriod, installment.getInterestCharged(getCurrency()).getAmount(),
                        tillDays);
                actualAmountTobeAccrued = actualAmountTobeAccrued.plus(interest);
            } else if ((tillDate.isAfter(installment.getFromDate()) && tillDate.isEqual(installment.getDueDate()))
                    || (tillDate.isEqual(installment.getFromDate()) && tillDate.isEqual(installment.getDueDate()))
                    || (tillDate.isAfter(installment.getFromDate()) && tillDate.isAfter(installment.getDueDate()))) {
                actualAmountTobeAccrued = actualAmountTobeAccrued.plus(installment.getInterestAccrued(getCurrency()));
            }
        }
        Money accredAmountAfterDate = totalAmountAccrued.minus(actualAmountTobeAccrued);
        if (accredAmountAfterDate.isLessThanZero()) {
            accredAmountAfterDate = Money.zero(getCurrency());
        }
        return accredAmountAfterDate;
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

        if (lastUserTransactionDate.isAfter(transactionDate)) {
            final String defaultUserMessage = "The transactionDate cannot be in the future.";
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
            if (!installment.getDueDate().isBefore(transactionDate)) {
                totalPrincipal = totalPrincipal.plus(installment.getPrincipal(currency));
                newInstallments.remove(installment);
                if (installment.getDueDate().isEqual(transactionDate)) {
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
            if (loanCharge.getDueLocalDate() != null && loanCharge.getDueLocalDate().isAfter(transactionDate)) {
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
                            && chargePaidBy.getLoanCharge().getDueLocalDate().isAfter(transactionDate))
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

    public Integer getLoanSubStatus() {
        return this.loanSubStatus;
    }

    private boolean isForeclosure() {
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

    public void setIsTopup(final boolean isTopup) {
        this.isTopup = isTopup;
    }

    public boolean isTopup() {
        return this.isTopup;
    }

    public void markAsFraud(final boolean value) {
        this.fraud = value;
    }

    public boolean isFraud() {
        return this.fraud;
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

    public void setRates(List<Rate> rates) {
        this.rates = rates;
    }

    public List<Rate> getRates() {
        return rates;
    }

    public Integer getLoanType() {
        return loanType;
    }

    public void setLoanType(Integer loanType) {
        this.loanType = loanType;
    }

    public Set<LoanCollateralManagement> getLoanCollateralManagements() {
        return this.loanCollateralManagements;
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

    public Long getAgeOfOverdueDays(LocalDate baseDate) {
        Long ageOfOverdueDays = 0L;

        List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            if (!installment.isObligationsMet()) {
                ageOfOverdueDays = DateUtils.getDifferenceInDays(installment.getDueDate(), baseDate);
                break;
            }
        }
        return ageOfOverdueDays;
    }

    public LocalDate getLastClosedBusinessDate() {
        return this.lastClosedBusinessDate;
    }

    public void setLastClosedBusinessDate(LocalDate lastClosedBusinessDate) {
        this.lastClosedBusinessDate = lastClosedBusinessDate;
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

    public boolean isChargedOff() {
        return this.chargedOff;
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

    public LocalDate getChargedOffOnDate() {
        return chargedOffOnDate;
    }

    public LoanInterestRecalculationDetails getLoanInterestRecalculationDetails() {
        return loanInterestRecalculationDetails;
    }
}
