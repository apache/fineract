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

import static org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType.CUMULATIVE;
import static org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType.PROGRESSIVE;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.security.service.RandomPasswordGenerator;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.collateral.domain.LoanCollateral;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.LoanSupportedInterestRefundTypes;
import org.apache.fineract.portfolio.rate.domain.Rate;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecks;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.lang.NonNull;

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

    @Enumerated
    @Column(name = "loan_type_enum", nullable = false)
    private AccountType loanType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne(fetch = FetchType.EAGER)
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
    @Enumerated
    @Column(name = "term_period_frequency_enum", nullable = false)
    private PeriodFrequencyType termPeriodFrequencyType;

    @Setter(AccessLevel.PACKAGE)
    @Column(name = "loan_status_id", nullable = false)
    @Convert(converter = LoanStatusConverter.class)
    private LoanStatus loanStatus;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejectedon_userid")
    private AppUser rejectedBy;

    @Setter()
    @Column(name = "withdrawnon_date")
    private LocalDate withdrawnOnDate;

    @Setter()
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "withdrawnon_userid")
    private AppUser withdrawnBy;

    @Setter()
    @Column(name = "approvedon_date")
    private LocalDate approvedOnDate;

    @Setter()
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approvedon_userid")
    private AppUser approvedBy;

    @Setter()
    @Column(name = "expected_disbursedon_date")
    private LocalDate expectedDisbursementDate;

    @Setter()
    @Column(name = "disbursedon_date")
    private LocalDate actualDisbursementDate;

    @Setter()
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursedon_userid")
    private AppUser disbursedBy;

    @Setter()
    @Column(name = "closedon_date")
    private LocalDate closedOnDate;

    @Setter()
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closedon_userid")
    private AppUser closedBy;

    @Setter
    @Column(name = "writtenoffon_date")
    private LocalDate writtenOffOnDate;

    @Setter
    @Column(name = "rescheduledon_date")
    private LocalDate rescheduledOnDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescheduledon_userid")
    private AppUser rescheduledByUser;

    @Setter
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

    @Setter
    @Column(name = "total_overpaid_derived", scale = 6, precision = 19)
    private BigDecimal totalOverpaid;

    @Setter()
    @Column(name = "overpaidon_date")
    private LocalDate overpaidOnDate;

    @Column(name = "loan_counter")
    private Integer loanCounter;

    @Column(name = "loan_product_counter")
    private Integer loanProductCounter;

    @Setter
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

    @Setter
    @Embedded
    private LoanSummary summary;

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

    @Setter
    @Column(name = "total_recovered_derived", scale = 6, precision = 19)
    private BigDecimal totalRecovered;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true, fetch = FetchType.LAZY)
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
    private LoanSubStatus loanSubStatus;

    @Column(name = "is_topup", nullable = false)
    private boolean isTopup = false;

    @Column(name = "is_fraud", nullable = false)
    private boolean fraud = false;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true, fetch = FetchType.LAZY)
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
            final ExternalId externalId, final LoanApplicationTerms loanApplicationTerms, final Boolean enableInstallmentLevelDelinquency,
            final LocalDate submittedOnDate) {
        return new Loan(accountNo, client, null, loanType, fund, officer, loanPurpose, transactionProcessingStrategy, loanProduct,
                loanRepaymentScheduleDetail, null, loanCharges, collateral, null, fixedEmiAmount, disbursementDetails,
                maxOutstandingLoanBalance, createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential, rates,
                fixedPrincipalPercentagePerInstallment, externalId, loanApplicationTerms, enableInstallmentLevelDelinquency,
                submittedOnDate);
    }

    public static Loan newGroupLoanApplication(final String accountNo, final Group group, final AccountType loanType,
            final LoanProduct loanProduct, final Fund fund, final Staff officer, final CodeValue loanPurpose,
            final LoanRepaymentScheduleTransactionProcessor transactionProcessingStrategy,
            final LoanProductRelatedDetail loanRepaymentScheduleDetail, final Set<LoanCharge> loanCharges,
            final Boolean syncDisbursementWithMeeting, final BigDecimal fixedEmiAmount,
            final List<LoanDisbursementDetails> disbursementDetails, final BigDecimal maxOutstandingLoanBalance,
            final Boolean createStandingInstructionAtDisbursement, final Boolean isFloatingInterestRate,
            final BigDecimal interestRateDifferential, final List<Rate> rates, final BigDecimal fixedPrincipalPercentagePerInstallment,
            final ExternalId externalId, final LoanApplicationTerms loanApplicationTerms, final Boolean enableInstallmentLevelDelinquency,
            final LocalDate submittedOnDate) {
        return new Loan(accountNo, null, group, loanType, fund, officer, loanPurpose, transactionProcessingStrategy, loanProduct,
                loanRepaymentScheduleDetail, null, loanCharges, null, syncDisbursementWithMeeting, fixedEmiAmount, disbursementDetails,
                maxOutstandingLoanBalance, createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential, rates,
                fixedPrincipalPercentagePerInstallment, externalId, loanApplicationTerms, enableInstallmentLevelDelinquency,
                submittedOnDate);
    }

    public static Loan newIndividualLoanApplicationFromGroup(final String accountNo, final Client client, final Group group,
            final AccountType loanType, final LoanProduct loanProduct, final Fund fund, final Staff officer, final CodeValue loanPurpose,
            final LoanRepaymentScheduleTransactionProcessor transactionProcessingStrategy,
            final LoanProductRelatedDetail loanRepaymentScheduleDetail, final Set<LoanCharge> loanCharges,
            final Boolean syncDisbursementWithMeeting, final BigDecimal fixedEmiAmount,
            final List<LoanDisbursementDetails> disbursementDetails, final BigDecimal maxOutstandingLoanBalance,
            final Boolean createStandingInstructionAtDisbursement, final Boolean isFloatingInterestRate,
            final BigDecimal interestRateDifferential, final List<Rate> rates, final BigDecimal fixedPrincipalPercentagePerInstallment,
            final ExternalId externalId, final LoanApplicationTerms loanApplicationTerms, final Boolean enableInstallmentLevelDelinquency,
            final LocalDate submittedOnDate) {
        return new Loan(accountNo, client, group, loanType, fund, officer, loanPurpose, transactionProcessingStrategy, loanProduct,
                loanRepaymentScheduleDetail, null, loanCharges, null, syncDisbursementWithMeeting, fixedEmiAmount, disbursementDetails,
                maxOutstandingLoanBalance, createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential, rates,
                fixedPrincipalPercentagePerInstallment, externalId, loanApplicationTerms, enableInstallmentLevelDelinquency,
                submittedOnDate);
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
            final ExternalId externalId, final LoanApplicationTerms loanApplicationTerms, final Boolean enableInstallmentLevelDelinquency,
            final LocalDate submittedOnDate) {
        this.loanRepaymentScheduleDetail = loanRepaymentScheduleDetail;

        this.isFloatingInterestRate = isFloatingInterestRate;
        this.interestRateDifferential = interestRateDifferential;

        if (StringUtils.isBlank(accountNo)) {
            this.accountNumber = new RandomPasswordGenerator(19).generate();
        } else {
            this.accountNumber = accountNo;
        }
        this.client = client;
        this.group = group;
        this.loanType = loanType;
        this.fund = fund;
        this.loanOfficer = loanOfficer;
        this.loanPurpose = loanPurpose;

        this.transactionProcessingStrategyCode = transactionProcessingStrategy.getCode();
        this.transactionProcessingStrategyName = transactionProcessingStrategy.getName();

        this.loanProduct = loanProduct;
        this.loanStatus = loanStatus;
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
        this.loanStatus = LoanStatus.SUBMITTED_AND_PENDING_APPROVAL;
        this.externalId = externalId;
        this.termFrequency = loanApplicationTerms.getLoanTermFrequency();
        this.termPeriodFrequencyType = loanApplicationTerms.getLoanTermPeriodFrequencyType();
        this.expectedDisbursementDate = loanApplicationTerms.getExpectedDisbursementDate();
        this.expectedFirstRepaymentOnDate = loanApplicationTerms.getRepaymentStartFromDate();
        this.interestChargedFromDate = loanApplicationTerms.getInterestChargedFromDate();
        this.submittedOnDate = submittedOnDate != null ? submittedOnDate : DateUtils.getBusinessLocalDate();

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

    public void removeDisbursementDetails(final long id) {
        this.disbursementDetails.remove(fetchLoanDisbursementsById(id));
    }

    public LoanDisbursementDetails addLoanDisbursementDetails(final LocalDate expectedDisbursementDate, final BigDecimal principal) {
        final LoanDisbursementDetails disbursementDetails = new LoanDisbursementDetails(expectedDisbursementDate, null, principal,
                this.netDisbursalAmount, false);
        disbursementDetails.updateLoan(this);
        this.disbursementDetails.add(disbursementDetails);
        return disbursementDetails;
    }

    public boolean removeLoanTransaction(LoanTransaction transactionToRemove) {
        return this.loanTransactions.remove(transactionToRemove);
    }

    public BigDecimal getTotalInterest() {
        return this.summary.calculateTotalInterestCharged(getRepaymentScheduleInstallments(), getCurrency()).getAmount();
    }

    public BigDecimal getTotalWrittenOff() {
        return this.summary.getTotalWrittenOff();
    }

    public Client client() {
        return this.client;
    }

    public LoanProduct loanProduct() {
        return this.loanProduct;
    }

    public void updateClient(final Client client) {
        this.client = client;
    }

    public void updateLoanProduct(final LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
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

    public void updateLoanScheduleDependentDerivedFields() {
        if (this.getLoanRepaymentScheduleInstallmentsSize() > 0) {
            this.expectedMaturityDate = determineExpectedMaturityDate();
            this.actualMaturityDate = determineExpectedMaturityDate();
        }
    }

    private boolean isInterestRecalculationEnabledForProduct() {
        return this.loanProduct.isInterestRecalculationEnabled();
    }

    public boolean isMultiDisburmentLoan() {
        return this.loanProduct.isMultiDisburseLoan();
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

    public LocalDate determineExpectedMaturityDate() {
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

    public List<LoanDisbursementDetails> getDisbursedLoanDisbursementDetails() {
        return getDisbursementDetails().stream() //
                .filter(it -> it.actualDisbursementDate() != null) //
                .collect(Collectors.toList());
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
        if (isMultiDisburmentLoan()) {
            for (LoanDisbursementDetails disbursementDetail : getDisbursementDetails()) {
                if (disbursementDetail.actualDisbursementDate() != null) {
                    principal = principal.add(disbursementDetail.principal());
                }
            }
            return principal;
        } else {
            return getNetDisbursalAmount();
        }
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
                        .count() < loanProduct.getLoanProductTrancheDetails().getMaxTrancheCount());
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

    public boolean isAutoRepaymentForDownPaymentEnabled() {
        return this.loanRepaymentScheduleDetail.isEnableDownPayment()
                && this.loanRepaymentScheduleDetail.isEnableAutoRepaymentForDownPayment();
    }

    public void removePostDatedChecks() {
        this.postDatedChecks = new ArrayList<>();
    }

    public List<LoanTransaction> retrieveListOfTransactionsByType(final LoanTransactionType transactionType) {
        return this.loanTransactions.stream()
                .filter(transaction -> transaction.isNotReversed() && transaction.getTypeOf().equals(transactionType))
                .sorted(LoanTransactionComparator.INSTANCE).collect(Collectors.toList());
    }

    public boolean isAfterLastRepayment(final LoanTransaction loanTransaction, final List<LoanTransaction> loanTransactions) {
        return loanTransactions.stream() //
                .filter(t -> t.isRepaymentLikeType() && t.isNotReversed()) //
                .noneMatch(t -> DateUtils.isBefore(loanTransaction.getTransactionDate(), t.getTransactionDate()));
    }

    public LoanTransaction findWriteOffTransaction() {
        return this.loanTransactions.stream() //
                .filter(transaction -> !transaction.isReversed() && transaction.isWriteOff()) //
                .findFirst() //
                .orElse(null);
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
        boolean notDisbursedTrancheExists = loanProduct.isDisallowExpectedDisbursements()
                || disbursementDetails.stream().anyMatch(it -> it.actualDisbursementDate() == null && !it.isReversed());
        return this.loanProduct.isMultiDisburseLoan() && isInRightStatus && isDisbursementAllowed() && notDisbursedTrancheExists;
    }

    private boolean hasDisbursementTransaction() {
        return this.loanTransactions.stream().anyMatch(LoanTransaction::isDisbursement);

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

    public Boolean isCashBasedAccountingEnabledOnLoanProduct() {
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

    public Long fetchChargeOffReasonId() {
        return isChargedOff() && getChargeOffReason() != null ? getChargeOffReason().getId() : null;
    }

    public boolean isSyncDisbursementWithMeeting() {
        return this.syncDisbursementWithMeeting != null && this.syncDisbursementWithMeeting;
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
        return Objects.requireNonNullElse(this.loanProductCounter, 0);
    }

    public void updateClientLoanCounter(final Integer newLoanCounter) {
        this.loanCounter = newLoanCounter;
    }

    public void updateLoanProductLoanCounter(final Integer newLoanProductLoanCounter) {
        this.loanProductCounter = newLoanProductLoanCounter;
    }

    public boolean isGroupLoan() {
        return this.loanType.isGroupAccount();
    }

    public boolean isJLGLoan() {
        return this.loanType.isJLGAccount();
    }

    public void updateInterestRateFrequencyType() {
        this.loanRepaymentScheduleDetail.setInterestPeriodFrequencyType(this.loanProduct.getInterestPeriodFrequencyType());
    }

    public void addLoanTransaction(final LoanTransaction loanTransaction) {
        this.loanTransactions.add(loanTransaction);
    }

    public LocalDate getLastUserTransactionDate() {
        return this.loanTransactions.stream().filter(this::isUserTransaction).map(LoanTransaction::getTransactionDate)
                .filter(date -> DateUtils.isBefore(getDisbursementDate(), date)).max(LocalDate::compareTo).orElse(getDisbursementDate());
    }

    public boolean isUserTransaction(LoanTransaction transaction) {
        return !(transaction.isReversed() || transaction.isAccrualRelated() || transaction.isIncomePosting());
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

    public boolean hasChargesAffectedByBackdatedRepaymentLikeTransaction(@NonNull final LoanTransaction transaction) {
        if (!transaction.isRepaymentLikeType() || CollectionUtils.isEmpty(this.charges) || !isProgressiveSchedule()
                || !DateUtils.isBeforeBusinessDate(transaction.getTransactionDate())) {
            return false;
        }

        final BiFunction<LocalDate, LocalDate, LocalDate> earlierDate = (date1, date2) -> DateUtils.isBefore(date1, date2) ? date1 : date2;

        return this.charges.stream().filter(LoanCharge::isActive)
                .filter(loanCharge -> loanCharge.isSpecifiedDueDate() || loanCharge.isOverdueInstallmentCharge())
                .filter(loanCharge -> loanCharge.getDueLocalDate() != null).anyMatch(loanCharge -> {
                    final LocalDate comparisonDate = earlierDate.apply(loanCharge.getDueLocalDate(), loanCharge.getSubmittedOnDate());
                    return comparisonDate != null && comparisonDate.isAfter(transaction.getTransactionDate());
                });
    }

    public LoanCharge fetchLoanChargesById(final Long id) {
        LoanCharge charge = null;
        for (LoanCharge loanCharge : this.charges) {
            if (id.equals(loanCharge.getId())) {
                charge = loanCharge;
                break;
            }
        }
        return charge;
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
        return Money.of(this.getLoanProductRelatedDetail().getCurrency(), this.totalOverpaid);
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
        return BigDecimal.ZERO.compareTo(getLoanRepaymentScheduleDetail().getAnnualNominalInterestRate()) < 0
                || (isProgressiveSchedule() && !getLoanTermVariations().isEmpty()
                        && loanTermVariations.stream().anyMatch(ltv -> ltv.getTermType().isInterestRateFromInstallment()
                                && ltv.getTermValue() != null && MathUtil.isGreaterThanZero(ltv.getTermValue())));
    }

    public boolean isInterestBearingAndInterestRecalculationEnabled() {
        return isInterestBearing() && isInterestRecalculationEnabled();
    }

    public boolean isInterestRecalculationEnabled() {
        return this.loanRepaymentScheduleDetail.isInterestRecalculationEnabled();
    }

    public LocalDate getMaturityDate() {
        return this.actualMaturityDate;
    }

    public boolean isMatured(final LocalDate referenceDate) {
        return (this.actualMaturityDate != null) ? (referenceDate.compareTo(this.actualMaturityDate) >= 0) : false;
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
        if (this.getLoanProductRelatedDetail().isInterestRecalculationEnabled()) {
            isEnabled = this.loanInterestRecalculationDetails.getInterestRecalculationCompoundingMethod().isFeeCompoundingEnabled();
        }
        return isEnabled;
    }

    public Boolean shouldCreateStandingInstructionAtDisbursement() {
        return this.createStandingInstructionAtDisbursement != null && this.createStandingInstructionAtDisbursement;
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

    public int fetchNumberOfInstallmentsAfterExceptions() {
        if (!this.repaymentScheduleInstallments.isEmpty()) {
            List<LoanRepaymentScheduleInstallment> installments = getRepaymentScheduleInstallments();
            int numberOfInstallments = 0;
            for (final LoanRepaymentScheduleInstallment installment : installments) {
                if (!installment.isRecalculatedInterestComponent() && !installment.isAdditional() && !installment.isDownPayment()
                        && !installment.isReAged()) {
                    numberOfInstallments++;
                }
            }
            return numberOfInstallments;
        }
        return this.getLoanProductRelatedDetail().getNumberOfRepayments() + adjustNumberOfRepayments();
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

    public void updateWriteOffReason(CodeValue writeOffReason) {
        this.writeOffReason = writeOffReason;
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
            isForeClosure = loanSubStatus.isForeclosed();
        }

        return isForeClosure;
    }

    public boolean isContractTermination() {
        if (this.loanSubStatus != null) {
            return loanSubStatus.isContractTermination();
        }

        return false;
    }

    public void liftContractTerminationSubStatus() {
        if (this.loanSubStatus.isContractTermination()) {
            this.loanSubStatus = null;
        }
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

    public void setTopupLoanDetails(LoanTopupDetails topupLoanDetails) {
        this.loanTopupDetails = topupLoanDetails;
    }

    public LoanTopupDetails getTopupLoanDetails() {
        return this.loanTopupDetails;
    }

    public Set<LoanCharge> getLoanCharges() {
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
        return getLoanType().isInvalid();
    }

    public boolean isIndividualLoan() {
        return getLoanType().isIndividualAccount();
    }

    public AccountType getLoanType() {
        return this.loanType == null ? AccountType.INVALID : this.loanType;
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

    public void addCharge(LoanCharge loanCharge) {
        this.getCharges().add(loanCharge);
    }

    public void removeCharges(Predicate<LoanCharge> predicate) {
        charges.removeIf(predicate);
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

    public void removeLoanTransactions(Predicate<LoanTransaction> predicate) {
        loanTransactions.removeIf(predicate);
    }

    public LoanTransaction findChargedOffTransaction() {
        return getLoanTransaction(e -> e.isNotReversed() && e.isChargeOff());
    }

    public LoanTransaction findContractTerminationTransaction() {
        return getLoanTransaction(e -> e.isNotReversed() && e.isContractTermination());
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
                .filter(t -> t.isNotReversed() && !(t.isAccrual() || t.isAccrualAdjustment() || t.isIncomePosting()
                        || t.isCapitalizedIncomeAmortization() || t.isCapitalizedIncomeAmortizationAdjustment()
                        || t.isBuyDownFeeAmortization() || t.isBuyDownFeeAmortizationAdjustment())) //
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

    public boolean isProgressiveSchedule() {
        return getLoanProductRelatedDetail().getLoanScheduleType() == PROGRESSIVE;
    }

    public boolean isCumulativeSchedule() {
        return getLoanProductRelatedDetail().getLoanScheduleType() == CUMULATIVE;
    }

    public boolean isChargeOffOnDate(final LocalDate onDate) {
        final LoanTransaction chargeOffTransaction = findChargedOffTransaction();
        return chargeOffTransaction != null && chargeOffTransaction.getDateOf().compareTo(onDate) <= 0;
    }

    public boolean hasMonetaryActivityAfter(final LocalDate transactionDate) {
        for (LoanTransaction transaction : this.getLoanTransactions()) {
            if (transaction.getTransactionDate().isAfter(transactionDate) && transaction.isNotReversed()
                    && !transaction.isNonMonetaryTransaction()) {
                return true;
            }
        }
        for (LoanCharge loanCharge : this.getLoanCharges()) {
            if (!loanCharge.determineIfFullyPaid() && loanCharge.getSubmittedOnDate().isAfter(transactionDate)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasChargeOffTransaction() {
        return getLoanTransactions().stream().anyMatch(LoanTransaction::isChargeOff);
    }

    public boolean hasAccelerateChargeOffStrategy() {
        return LoanChargeOffBehaviour.ACCELERATE_MATURITY.equals(getLoanProductRelatedDetail().getChargeOffBehaviour());
    }

    public boolean hasContractTerminationTransaction() {
        return getLoanTransactions().stream().anyMatch(t -> t.isContractTermination() && t.isNotReversed());
    }

}
