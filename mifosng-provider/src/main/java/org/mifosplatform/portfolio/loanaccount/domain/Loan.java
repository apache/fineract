/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
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
import javax.persistence.OneToOne;
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
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.JsonParserHelper;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.RandomPasswordGenerator;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.holiday.service.HolidayUtil;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.organisation.workingdays.service.WorkingDaysUtil;
import org.mifosplatform.portfolio.accountdetails.domain.AccountType;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;
import org.mifosplatform.portfolio.charge.domain.ChargeCalculationType;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeAddedException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.collateral.data.CollateralData;
import org.mifosplatform.portfolio.collateral.domain.LoanCollateral;
import org.mifosplatform.portfolio.common.domain.DayOfWeekType;
import org.mifosplatform.portfolio.common.domain.NthDayType;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.loanaccount.api.LoanApiConstants;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.mifosplatform.portfolio.loanaccount.data.DisbursementData;
import org.mifosplatform.portfolio.loanaccount.data.HolidayDetailDTO;
import org.mifosplatform.portfolio.loanaccount.data.LoanTermVariationsData;
import org.mifosplatform.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.exception.ExceedingTrancheCountException;
import org.mifosplatform.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.mifosplatform.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.mifosplatform.portfolio.loanaccount.exception.InvalidRefundDateException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanApplicationDateException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanDisbursalException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerAssignmentDateException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerAssignmentException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerUnassignmentDateException;
import org.mifosplatform.portfolio.loanaccount.exception.MultiDisbursementDataRequiredException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelPeriod;
import org.mifosplatform.portfolio.loanproduct.LoanProductConstants;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.mifosplatform.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@Entity
@Table(name = "m_loan", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_no" }, name = "loan_account_no_UNIQUE"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "loan_externalid_UNIQUE") })
public class Loan extends AbstractPersistable<Long> {

    /** Disable optimistic locking till batch jobs failures can be fixed **/
    @Version
    int version;

    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "external_id")
    private String externalId;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = true)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = true)
    private Group group;

    @Column(name = "loan_type_enum", nullable = false)
    private Integer loanType;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne(optional = true)
    @JoinColumn(name = "fund_id", nullable = true)
    private Fund fund;

    @ManyToOne
    @JoinColumn(name = "loan_officer_id", nullable = true)
    private Staff loanOfficer;

    @ManyToOne
    @JoinColumn(name = "loanpurpose_cv_id", nullable = true)
    private CodeValue loanPurpose;

    @ManyToOne
    @JoinColumn(name = "loan_transaction_strategy_id", nullable = true)
    private LoanTransactionProcessingStrategy transactionProcessingStrategy;

    @Embedded
    private LoanProductRelatedDetail loanRepaymentScheduleDetail;

    @Column(name = "repayment_frequency_nth_day_enum", nullable = true)
    private Integer repaymentFrequencyNthDayType;

    @Column(name = "repayment_frequency_day_of_week_enum", nullable = true)
    private Integer repaymentFrequencyDayOfWeekType;

    @Column(name = "term_frequency", nullable = false)
    private Integer termFrequency;

    @Column(name = "term_period_frequency_enum", nullable = false)
    private Integer termPeriodFrequencyType;

    @Column(name = "loan_status_id", nullable = false)
    private Integer loanStatus;

    @Column(name = "sync_disbursement_with_meeting", nullable = true)
    private Boolean syncDisbursementWithMeeting;

    // loan application states
    @Temporal(TemporalType.DATE)
    @Column(name = "submittedon_date")
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
    @Column(name = "expected_disbursedon_date")
    private Date expectedDisbursementDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "disbursedon_date")
    private Date actualDisbursementDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "disbursedon_userid", nullable = true)
    private AppUser disbursedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "closedon_date")
    private Date closedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "closedon_userid", nullable = true)
    private AppUser closedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "writtenoffon_date")
    private Date writtenOffOnDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "rescheduledon_date")
    private Date rescheduledOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "rescheduledon_userid", nullable = true)
    private AppUser rescheduledByUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "expected_maturedon_date")
    private Date expectedMaturityDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "maturedon_date")
    private Date actualMaturityDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "expected_firstrepaymenton_date")
    private Date expectedFirstRepaymentOnDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "interest_calculated_from_date")
    private Date interestChargedFromDate;

    @Column(name = "total_overpaid_derived", scale = 6, precision = 19)
    private BigDecimal totalOverpaid;

    @Column(name = "loan_counter")
    private Integer loanCounter;

    @Column(name = "loan_product_counter")
    private Integer loanProductCounter;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true)
    private Set<LoanCharge> charges = new HashSet<>();

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true)
    private Set<LoanCollateral> collateral = null;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true)
    private Set<LoanOfficerAssignmentHistory> loanOfficerHistory;

    // see
    // http://stackoverflow.com/questions/4334970/hibernate-cannot-simultaneously-fetch-multiple-bags
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true)
    private final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = new ArrayList<>();

    // see
    // http://stackoverflow.com/questions/4334970/hibernate-cannot-simultaneously-fetch-multiple-bags
    @OrderBy(value = "dateOf, id")
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true)
    private final List<LoanTransaction> loanTransactions = new ArrayList<>();

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

    @Column(name = "fixed_emi_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal fixedEmiAmount;

    @Column(name = "max_outstanding_loan_balance", scale = 6, precision = 19, nullable = false)
    private BigDecimal maxOutstandingLoanBalance;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true)
    private Set<LoanDisbursementDetails> disbursementDetails = new HashSet<>();

    @OrderBy(value = "termApplicableFrom, id")
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true)
    private final Set<LoanTermVariations> loanTermVariations = new HashSet<>();

    @Column(name = "total_recovered_derived", scale = 6, precision = 19)
    private BigDecimal totalRecovered;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "loan", optional = true, orphanRemoval = true)
    private LoanInterestRecalculationDetails loanInterestRecalculationDetails;

    @Column(name = "is_npa", nullable = false)
    private boolean isNpa;

    @Temporal(TemporalType.DATE)
    @Column(name = "accrued_till")
    private Date accruedTill;

    @Column(name = "create_standing_instruction_at_disbursement", nullable = true)
    private Boolean createStandingInstructionAtDisbursement;

    @Column(name = "guarantee_amount_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal guaranteeAmountDerived;

    public static Loan newIndividualLoanApplication(final String accountNo, final Client client, final Integer loanType,
            final LoanProduct loanProduct, final Fund fund, final Staff officer, final CodeValue loanPurpose,
            final LoanTransactionProcessingStrategy transactionProcessingStrategy,
            final LoanProductRelatedDetail loanRepaymentScheduleDetail, final Set<LoanCharge> loanCharges,
            final Set<LoanCollateral> collateral, final BigDecimal fixedEmiAmount, final Set<LoanDisbursementDetails> disbursementDetails,
            final BigDecimal maxOutstandingLoanBalance, final Boolean createStandingInstructionAtDisbursement) {
        final LoanStatus status = null;
        final Group group = null;
        final Boolean syncDisbursementWithMeeting = null;
        return new Loan(accountNo, client, group, loanType, fund, officer, loanPurpose, transactionProcessingStrategy, loanProduct,
                loanRepaymentScheduleDetail, status, loanCharges, collateral, syncDisbursementWithMeeting, fixedEmiAmount,
                disbursementDetails, maxOutstandingLoanBalance, createStandingInstructionAtDisbursement);
    }

    public static Loan newGroupLoanApplication(final String accountNo, final Group group, final Integer loanType,
            final LoanProduct loanProduct, final Fund fund, final Staff officer, final CodeValue loanPurpose,
            final LoanTransactionProcessingStrategy transactionProcessingStrategy,
            final LoanProductRelatedDetail loanRepaymentScheduleDetail, final Set<LoanCharge> loanCharges,
            final Set<LoanCollateral> collateral, final Boolean syncDisbursementWithMeeting, final BigDecimal fixedEmiAmount,
            final Set<LoanDisbursementDetails> disbursementDetails, final BigDecimal maxOutstandingLoanBalance,
            final Boolean createStandingInstructionAtDisbursement) {
        final LoanStatus status = null;
        final Client client = null;
        return new Loan(accountNo, client, group, loanType, fund, officer, loanPurpose, transactionProcessingStrategy, loanProduct,
                loanRepaymentScheduleDetail, status, loanCharges, collateral, syncDisbursementWithMeeting, fixedEmiAmount,
                disbursementDetails, maxOutstandingLoanBalance, createStandingInstructionAtDisbursement);
    }

    public static Loan newIndividualLoanApplicationFromGroup(final String accountNo, final Client client, final Group group,
            final Integer loanType, final LoanProduct loanProduct, final Fund fund, final Staff officer, final CodeValue loanPurpose,
            final LoanTransactionProcessingStrategy transactionProcessingStrategy,
            final LoanProductRelatedDetail loanRepaymentScheduleDetail, final Set<LoanCharge> loanCharges,
            final Set<LoanCollateral> collateral, final Boolean syncDisbursementWithMeeting, final BigDecimal fixedEmiAmount,
            final Set<LoanDisbursementDetails> disbursementDetails, final BigDecimal maxOutstandingLoanBalance,
            final Boolean createStandingInstructionAtDisbursement) {
        final LoanStatus status = null;
        return new Loan(accountNo, client, group, loanType, fund, officer, loanPurpose, transactionProcessingStrategy, loanProduct,
                loanRepaymentScheduleDetail, status, loanCharges, collateral, syncDisbursementWithMeeting, fixedEmiAmount,
                disbursementDetails, maxOutstandingLoanBalance, createStandingInstructionAtDisbursement);
    }

    protected Loan() {
        this.client = null;
    }

    private Loan(final String accountNo, final Client client, final Group group, final Integer loanType, final Fund fund,
            final Staff loanOfficer, final CodeValue loanPurpose, final LoanTransactionProcessingStrategy transactionProcessingStrategy,
            final LoanProduct loanProduct, final LoanProductRelatedDetail loanRepaymentScheduleDetail, final LoanStatus loanStatus,
            final Set<LoanCharge> loanCharges, final Set<LoanCollateral> collateral, final Boolean syncDisbursementWithMeeting,
            final BigDecimal fixedEmiAmount, final Set<LoanDisbursementDetails> disbursementDetails,
            final BigDecimal maxOutstandingLoanBalance, final Boolean createStandingInstructionAtDisbursement) {

        this.loanRepaymentScheduleDetail = loanRepaymentScheduleDetail;
        this.loanRepaymentScheduleDetail.validateRepaymentPeriodWithGraceSettings();

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

        this.transactionProcessingStrategy = transactionProcessingStrategy;
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
        if (collateral != null && !collateral.isEmpty()) {
            this.collateral = associateWithThisLoan(collateral);
        } else {
            this.collateral = null;
        }
        this.loanOfficerHistory = null;

        this.syncDisbursementWithMeeting = syncDisbursementWithMeeting;
        this.fixedEmiAmount = fixedEmiAmount;
        this.maxOutstandingLoanBalance = maxOutstandingLoanBalance;
        this.disbursementDetails = disbursementDetails;
        this.approvedPrincipal = this.loanRepaymentScheduleDetail.getPrincipal().getAmount();
        this.createStandingInstructionAtDisbursement = createStandingInstructionAtDisbursement;

        /*
         * During loan origination stage and before loan is approved
         * principal_amount, approved_principal and principal_amount_demanded
         * will same amount and that amount is same as applicant loan demanded
         * amount.
         */

        this.proposedPrincipal = this.loanRepaymentScheduleDetail.getPrincipal().getAmount();

    }

    private LoanSummary updateSummaryWithTotalFeeChargesDueAtDisbursement(final BigDecimal feeChargesDueAtDisbursement) {
        if (this.summary == null) {
            this.summary = LoanSummary.create(feeChargesDueAtDisbursement);
        } else {
            this.summary.updateTotalFeeChargesDueAtDisbursement(feeChargesDueAtDisbursement);
        }
        return this.summary;
    }

    private BigDecimal deriveSumTotalOfChargesDueAtDisbursement() {

        Money chargesDue = Money.of(getCurrency(), BigDecimal.ZERO);

        for (final LoanCharge charge : charges()) {
            if (charge.isDueAtDisbursement()) {
                chargesDue = chargesDue.plus(charge.amount());
            }
        }

        return chargesDue.getAmount();
    }

    private Set<LoanCharge> associateChargesWithThisLoan(final Set<LoanCharge> loanCharges) {
        for (final LoanCharge loanCharge : loanCharges) {
            loanCharge.update(this);
        }
        return loanCharges;
    }

    private Set<LoanCollateral> associateWithThisLoan(final Set<LoanCollateral> collateral) {
        for (final LoanCollateral item : collateral) {
            item.associateWith(this);
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

        validateLoanIsNotClosed(loanCharge);

        if (isDisbursed() && loanCharge.isDueAtDisbursement()) {
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

        validateChargeHasValidSpecifiedDateIfApplicable(loanCharge, getDisbursementDate(), getLastRepaymentPeriodDueDate());

        loanCharge.update(this);

        final BigDecimal amount = calculateAmountPercentageAppliedTo(loanCharge);
        BigDecimal chargeAmt = BigDecimal.ZERO;
        BigDecimal totalChargeAmt = BigDecimal.ZERO;
        if (loanCharge.getChargeCalculation().isPercentageBased()) {
            chargeAmt = loanCharge.getPercentage();
            if (loanCharge.isInstalmentFee()) {
                totalChargeAmt = calculatePerInstallmentChargeAmount(loanCharge);
            } else if (loanCharge.isOverdueInstallmentCharge()) {
                totalChargeAmt = loanCharge.amountOutstanding();
            }
        } else {
            chargeAmt = loanCharge.amount();
            if (loanCharge.isInstalmentFee()) {
                chargeAmt = chargeAmt.divide(BigDecimal.valueOf(repaymentScheduleDetail().getNumberOfRepayments()));
            }
        }
        loanCharge.update(chargeAmt, loanCharge.getDueLocalDate(), amount, repaymentScheduleDetail().getNumberOfRepayments(),
                totalChargeAmt);

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
        wrapper.reprocess(getCurrency(), getDisbursementDate(), this.repaymentScheduleInstallments, charges(),
                getLastUserTransactionForChargeCalc());
        updateLoanSummaryDerivedFields();

    }

    public ChangedTransactionDetail reprocessTransactions() {
        ChangedTransactionDetail changedTransactionDetail = null;
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
        changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(),
                allNonContraTransactionsPostDisbursement, getCurrency(), this.repaymentScheduleInstallments, charges(),
                getLastUserTransactionForChargeCalc());
        for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
            mapEntry.getValue().updateLoan(this);
        }
        this.loanTransactions.addAll(changedTransactionDetail.getNewTransactionMappings().values());
        updateLoanSummaryDerivedFields();
        return changedTransactionDetail;
    }

    /**
     * Creates a loanTransaction for "Apply Charge Event" with transaction date
     * set to "suppliedTransactionDate". The newly created transaction is also
     * added to the Loan on which this method is called.
     * 
     * If "suppliedTransactionDate" is not passed Id, the transaction date is
     * set to the loans due date if the due date is lesser than todays date. If
     * not, the transaction date is set to todays date
     * 
     * @param loanCharge
     * @param suppliedTransactionDate
     * @return
     */
    public LoanTransaction handleChargeAppliedTransaction(final LoanCharge loanCharge, final LocalDate suppliedTransactionDate,
            final AppUser currentUser) {
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
            final LocalDate currentDate = DateUtils.getLocalDateOfTenant();

            // if loan charge is to be applied on a future date, the loan
            // transaction would show todays date as applied date
            if (transactionDate == null || currentDate.isBefore(transactionDate)) {
                transactionDate = currentDate;
            }
        }

        final LoanTransaction applyLoanChargeTransaction = LoanTransaction.accrueLoanCharge(this, getOffice(), chargeAmount,
                transactionDate, feeCharges, penaltyCharges, DateUtils.getLocalDateTimeOfTenant(), currentUser);
        Integer installmentNumber = null;
        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(applyLoanChargeTransaction, loanCharge, loanCharge.getAmount(
                getCurrency()).getAmount(), installmentNumber);
        applyLoanChargeTransaction.getLoanChargesPaid().add(loanChargePaidBy);
        this.loanTransactions.add(applyLoanChargeTransaction);
        return applyLoanChargeTransaction;
    }

    private void handleChargePaidTransaction(final LoanCharge charge, final LoanTransaction chargesPayment,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final Integer installmentNumber) {
        chargesPayment.updateLoan(this);
        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(chargesPayment, charge, chargesPayment.getAmount(getCurrency())
                .getAmount(), installmentNumber);
        chargesPayment.getLoanChargesPaid().add(loanChargePaidBy);
        this.loanTransactions.add(chargesPayment);
        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_CHARGE_PAYMENT,
                LoanStatus.fromInt(this.loanStatus));
        this.loanStatus = statusEnum.getValue();

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);
        final List<LoanRepaymentScheduleInstallment> chargePaymentInstallments = new ArrayList<>();
        LocalDate startDate = getDisbursementDate();
        for (final LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
            if (installmentNumber == null && charge.isDueForCollectionFromAndUpToAndIncluding(startDate, installment.getDueDate())) {
                chargePaymentInstallments.add(installment);
                break;
            } else if (installmentNumber != null && installment.getInstallmentNumber().equals(installmentNumber)) {
                chargePaymentInstallments.add(installment);
                break;
            }
            startDate = installment.getDueDate();
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
            throw new LoanChargeCannotBeAddedException("loanCharge", "loanCharge.is.waived", defaultUserMessage, getId(), loanCharge.name());
        }
    }

    private void validateChargeHasValidSpecifiedDateIfApplicable(final LoanCharge loanCharge, final LocalDate disbursementDate,
            final LocalDate lastRepaymentPeriodDueDate) {
        if (loanCharge.isSpecifiedDueDate()
                && !loanCharge.isDueForCollectionFromAndUpToAndIncluding(disbursementDate, lastRepaymentPeriodDueDate)) {
            final String defaultUserMessage = "This charge with specified due date cannot be added as the it is not in schedule range.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "specified.due.date.outside.range", defaultUserMessage,
                    getDisbursementDate(), lastRepaymentPeriodDueDate, loanCharge.name());
        }
    }

    private LocalDate getLastRepaymentPeriodDueDate() {
        LocalDate lastRepaymentDate = getDisbursementDate();
        for (LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
            if (!installment.isRecalculatedInterestComponent() && installment.getDueDate().isAfter(lastRepaymentDate)) {
                lastRepaymentDate = installment.getDueDate();
            }
        }
        return lastRepaymentDate;
    }

    public void removeLoanCharge(final LoanCharge loanCharge) {

        validateLoanIsNotClosed(loanCharge);

        // NOTE: to remove this constraint requires that loan transactions
        // that represent the waive of charges also be removed (or reversed)
        // if you want ability to remove loan charges that are waived.
        validateLoanChargeIsNotWaived(loanCharge);

        LoanCharge charge = fetchLoanChargesById(loanCharge.getId());
        final boolean removed = charge.isActive();
        if (removed) {
            charge.setActive(false);
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(getCurrency(), getDisbursementDate(), this.repaymentScheduleInstallments, charges(),
                    getLastUserTransactionForChargeCalc());
            updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
        }

        removeOrModifyTransactionAssociatedWithLoanChargeIfDueAtDisbursement(loanCharge);

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);
        if (!loanCharge.isDueAtDisbursement() && loanCharge.isPaidOrPartiallyPaid(loanCurrency())) {
            /****
             * TODO Vishwas Currently we do not allow removing a loan charge
             * after a loan is approved (hence there is no need to adjust any
             * loan transactions).
             * 
             * Consider removing this block of code or logically completing it
             * for the future by getting the list of affected Transactions
             ***/
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
            loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(), allNonContraTransactionsPostDisbursement,
                    getCurrency(), this.repaymentScheduleInstallments, charges(), getLastUserTransactionForChargeCalc());
        }

        updateLoanSummaryDerivedFields();
    }

    private void removeOrModifyTransactionAssociatedWithLoanChargeIfDueAtDisbursement(final LoanCharge loanCharge) {
        if (loanCharge.isDueAtDisbursement()) {
            LoanTransaction transactionToRemove = null;
            for (final LoanTransaction transaction : this.loanTransactions) {
                if (transaction.isRepaymentAtDisbursement()) {

                    final MonetaryCurrency currency = loanCurrency();
                    final Money chargeAmount = Money.of(currency, loanCharge.amount());
                    if (transaction.isGreaterThan(chargeAmount)) {
                        final Money principalPortion = Money.zero(currency);
                        final Money interestPortion = Money.zero(currency);
                        final Money penaltychargesPortion = Money.zero(currency);

                        final Money feeChargesPortion = chargeAmount;
                        transaction.updateComponentsAndTotal(principalPortion, interestPortion, feeChargesPortion, penaltychargesPortion);
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

    public Map<String, Object> updateLoanCharge(final LoanCharge loanCharge, final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(3);

        validateLoanIsNotClosed(loanCharge);
        if (charges().contains(loanCharge)) {
            final BigDecimal amount = calculateAmountPercentageAppliedTo(loanCharge);
            final Map<String, Object> loanChargeChanges = loanCharge.update(command, amount);
            actualChanges.putAll(loanChargeChanges);
            updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
        }

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);
        if (!loanCharge.isDueAtDisbursement()) {
            /****
             * TODO Vishwas Currently we do not allow waiving updating loan
             * charge after a loan is approved (hence there is no need to adjust
             * any loan transactions).
             * 
             * Consider removing this block of code or logically completing it
             * for the future by getting the list of affected Transactions
             ***/
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
            loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(), allNonContraTransactionsPostDisbursement,
                    getCurrency(), this.repaymentScheduleInstallments, charges(), getLastUserTransactionForChargeCalc());
        } else {
            // reprocess loan schedule based on charge been waived.
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(getCurrency(), getDisbursementDate(), this.repaymentScheduleInstallments, charges(),
                    getLastUserTransactionForChargeCalc());
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
        switch (loanCharge.getChargeCalculation()) {
            case PERCENT_OF_AMOUNT:
                amount = getPrincpal().getAmount();
            break;
            case PERCENT_OF_AMOUNT_AND_INTEREST:
                final BigDecimal totalInterestCharged = getTotalInterest();
                amount = getPrincpal().getAmount().add(totalInterestCharged);
            break;
            case PERCENT_OF_INTEREST:
                amount = getTotalInterest();
            break;
            default:
            break;
        }
        return amount;
    }

    /**
     * @return
     */
    public BigDecimal getTotalInterest() {
        return this.loanSummaryWrapper.calculateTotalInterestCharged(this.repaymentScheduleInstallments, getCurrency()).getAmount();
    }

    private BigDecimal calculatePerInstallmentChargeAmount(final LoanCharge loanCharge) {
        return calculatePerInstallmentChargeAmount(loanCharge.getChargeCalculation(), loanCharge.getPercentage());
    }

    public BigDecimal calculatePerInstallmentChargeAmount(final ChargeCalculationType calculationType, final BigDecimal percentage) {
        Money amount = Money.zero(getCurrency());
        for (final LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
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
            final AppUser currentUser) {

        validateLoanIsNotClosed(loanCharge);

        final Money amountWaived = loanCharge.waive(loanCurrency(), loanInstallmentNumber);

        changes.put("amount", amountWaived.getAmount());

        Money unrecognizedIncome = amountWaived.zero();
        Money chargeComponent = amountWaived;
        if (isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            Money receivableCharge = accruedCharge.minus(loanCharge.getAmountPaid(getCurrency()));
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
        if (loanCharge.isSpecifiedDueDate()) {
            transactionDate = loanCharge.getDueLocalDate();
        }

        updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        final LoanTransaction waiveLoanChargeTransaction = LoanTransaction.waiveLoanCharge(this, getOffice(), amountWaived,
                transactionDate, feeChargesWaived, penaltyChargesWaived, unrecognizedIncome, DateUtils.getLocalDateTimeOfTenant(),
                currentUser);
        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(waiveLoanChargeTransaction, loanCharge, waiveLoanChargeTransaction
                .getAmount(getCurrency()).getAmount(), loanInstallmentNumber);
        waiveLoanChargeTransaction.getLoanChargesPaid().add(loanChargePaidBy);
        this.loanTransactions.add(waiveLoanChargeTransaction);

        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()
                && (loanCharge.getDueLocalDate() == null || LocalDate.now().isAfter(loanCharge.getDueLocalDate()))) {
            regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO, currentUser);
        }
        // Waive of charges whose due date falls after latest 'repayment'
        // transaction dont require entire loan schedule to be reprocessed.
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);
        if (!loanCharge.isDueAtDisbursement() && loanCharge.isPaidOrPartiallyPaid(loanCurrency())) {
            /****
             * TODO Vishwas Currently we do not allow waiving fully paid loan
             * charge and waiving partially paid loan charges only waives the
             * remaining amount.
             * 
             * Consider removing this block of code or logically completing it
             * for the future by getting the list of affected Transactions
             ***/
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
            loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(), allNonContraTransactionsPostDisbursement,
                    getCurrency(), this.repaymentScheduleInstallments, charges(), getLastUserTransactionForChargeCalc());
        } else {
            // reprocess loan schedule based on charge been waived.
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(getCurrency(), getDisbursementDate(), this.repaymentScheduleInstallments, charges(),
                    getLastUserTransactionForChargeCalc());
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

    public void updateTransactionProcessingStrategy(final LoanTransactionProcessingStrategy strategy) {
        this.transactionProcessingStrategy = strategy;
    }

    public void updateLoanCharges(final Set<LoanCharge> loanCharges) {
        List<Long> existingCharges = fetchAllLoanChargeIds();

        /** Process new and updated charges **/
        for (final LoanCharge loanCharge : loanCharges) {
            LoanCharge charge = loanCharge;
            // add new charges
            if (loanCharge.getId() == null) {
                loanCharge.update(this);
                this.charges.add(loanCharge);
            } else {
                charge = fetchLoanChargesById(charge.getId());
                existingCharges.remove(charge.getId());
            }
            final BigDecimal amount = calculateAmountPercentageAppliedTo(loanCharge);
            BigDecimal chargeAmt = BigDecimal.ZERO;
            BigDecimal totalChargeAmt = BigDecimal.ZERO;
            if (loanCharge.getChargeCalculation().isPercentageBased()) {
                chargeAmt = loanCharge.getPercentage();
                if (loanCharge.isInstalmentFee()) {
                    totalChargeAmt = calculatePerInstallmentChargeAmount(loanCharge);
                }
            } else {
                chargeAmt = loanCharge.amount();
                if (loanCharge.isInstalmentFee()) {
                    chargeAmt = chargeAmt.divide(BigDecimal.valueOf(repaymentScheduleDetail().getNumberOfRepayments()));
                }
            }
            charge.update(chargeAmt, loanCharge.getDueLocalDate(), amount, repaymentScheduleDetail().getNumberOfRepayments(),
                    totalChargeAmt);
        }

        /** Updated deleted charges **/
        for (Long id : existingCharges) {
            fetchLoanChargesById(id).setActive(false);
        }
        updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
    }

    public void updateLoanCollateral(final Set<LoanCollateral> loanCollateral) {
        if (this.collateral == null) {
            this.collateral = new HashSet<>();
        }
        this.collateral.clear();
        this.collateral.addAll(associateWithThisLoan(loanCollateral));
    }

    public void updateLoanSchedule(final LoanScheduleModel modifiedLoanSchedule, AppUser currentUser) {
        this.repaymentScheduleInstallments.clear();

        for (final LoanScheduleModelPeriod scheduledLoanInstallment : modifiedLoanSchedule.getPeriods()) {

            if (scheduledLoanInstallment.isRepaymentPeriod()) {
                final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(this,
                        scheduledLoanInstallment.periodNumber(), scheduledLoanInstallment.periodFromDate(),
                        scheduledLoanInstallment.periodDueDate(), scheduledLoanInstallment.principalDue(),
                        scheduledLoanInstallment.interestDue(), scheduledLoanInstallment.feeChargesDue(),
                        scheduledLoanInstallment.penaltyChargesDue(), scheduledLoanInstallment.isRecalculatedInterestComponent());
                addRepaymentScheduleInstallment(installment);
            }
        }

        updateLoanScheduleDependentDerivedFields();
        updateLoanSummaryDerivedFields();
        applyAccurals(currentUser);

    }

    /**
     * method updates accrual derived fields on installments and reverse the
     * unprocessed transactions
     */
    private void applyAccurals(AppUser currentUser) {
        Collection<LoanTransaction> accruals = retreiveListOfAccrualTransactions();
        if (isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            applyPeriodicAccruals(accruals);
        } else if (isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()) {
            updateAccrualsForNonPeriodicAccruals(accruals, currentUser);
        }
    }

    private void applyPeriodicAccruals(final Collection<LoanTransaction> accruals) {

        for (LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
            Money interest = Money.zero(getCurrency());
            Money fee = Money.zero(getCurrency());
            Money penality = Money.zero(getCurrency());
            for (LoanTransaction loanTransaction : accruals) {
                if (loanTransaction.getTransactionDate().isAfter(installment.getFromDate())
                        && !loanTransaction.getTransactionDate().isAfter(installment.getDueDate())) {
                    interest = interest.plus(loanTransaction.getInterestPortion(getCurrency()));
                    fee = fee.plus(loanTransaction.getFeeChargesPortion(getCurrency()));
                    penality = penality.plus(loanTransaction.getPenaltyChargesPortion(getCurrency()));
                    if (installment.getFeeChargesCharged(getCurrency()).isLessThan(fee)
                            || installment.getInterestCharged(getCurrency()).isLessThan(interest)
                            || installment.getPenaltyChargesCharged(getCurrency()).isLessThan(penality)) {
                        interest = interest.minus(loanTransaction.getInterestPortion(getCurrency()));
                        fee = fee.minus(loanTransaction.getFeeChargesPortion(getCurrency()));
                        penality = penality.minus(loanTransaction.getPenaltyChargesPortion(getCurrency()));
                        loanTransaction.reverse();
                    }
                }
            }
            installment.updateAccrualPortion(interest, fee, penality);
        }
    }

    private void updateAccrualsForNonPeriodicAccruals(final Collection<LoanTransaction> accruals, final AppUser currentUser) {

        final Money interestApplied = Money.of(getCurrency(), this.summary.getTotalInterestCharged());
        for (LoanTransaction loanTransaction : accruals) {
            if (loanTransaction.getInterestPortion(getCurrency()).isGreaterThanZero()) {
                if (loanTransaction.getInterestPortion(getCurrency()).isNotEqualTo(interestApplied)) {
                    loanTransaction.reverse();
                    final LocalDateTime currentDateTime = DateUtils.getLocalDateTimeOfTenant();
                    final LoanTransaction interestAppliedTransaction = LoanTransaction.accrueInterest(getOffice(), this, interestApplied,
                            getDisbursementDate(), currentDateTime, currentUser);
                    this.loanTransactions.add(interestAppliedTransaction);
                }
            } else {
                Set<LoanChargePaidBy> chargePaidBies = loanTransaction.getLoanChargesPaid();
                for (final LoanChargePaidBy chargePaidBy : chargePaidBies) {
                    LoanCharge loanCharge = chargePaidBy.getLoanCharge();
                    Money chargeAmount = loanCharge.getAmount(getCurrency());
                    if (chargeAmount.isNotEqualTo(loanTransaction.getAmount(getCurrency()))) {
                        loanTransaction.reverse();
                        handleChargeAppliedTransaction(loanCharge, loanTransaction.getTransactionDate(), currentUser);
                    }

                }
            }
        }

    }

    private void updateLoanScheduleDependentDerivedFields() {
        this.expectedMaturityDate = determineExpectedMaturityDate().toDate();
        this.actualMaturityDate = determineExpectedMaturityDate().toDate();
    }

    private void updateLoanSummaryDerivedFields() {

        if (isNotDisbursed()) {
            this.summary.zeroFields();
            this.totalOverpaid = null;
        } else {
            final Money overpaidBy = calculateTotalOverpayment();
            this.totalOverpaid = overpaidBy.getAmountDefaultedToNullIfZero();

            final Money recoveredAmount = calculateTotalRecoveredPayments();
            this.totalRecovered = recoveredAmount.getAmountDefaultedToNullIfZero();

            final Money principal = this.loanRepaymentScheduleDetail.getPrincipal();
            this.summary.updateSummary(loanCurrency(), principal, this.repaymentScheduleInstallments, this.loanSummaryWrapper,
                    isDisbursed());
            updateLoanOutstandingBalaces();
        }
    }

    public Map<String, Object> loanApplicationModification(final JsonCommand command, final Set<LoanCharge> possiblyModifedLoanCharges,
            final Set<LoanCollateral> possiblyModifedLoanCollateralItems, final AprCalculator aprCalculator, boolean isChargesModified) {

        final Map<String, Object> actualChanges = this.loanRepaymentScheduleDetail.updateLoanApplicationAttributes(command, aprCalculator);
        if (!actualChanges.isEmpty()) {
            final boolean recalculateLoanSchedule = !(actualChanges.size() == 1 && actualChanges.containsKey("inArrearsTolerance"));
            actualChanges.put("recalculateLoanSchedule", recalculateLoanSchedule);
            isChargesModified = true;
        }

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();
        final LocalDate recalculationRestFrequencyDate = command
                .localDateValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyDateParamName);
        updateLoanInterestRecalculationSettings(recalculationRestFrequencyDate, command, actualChanges);

        final String accountNoParamName = "accountNo";
        if (command.isChangeInStringParameterNamed(accountNoParamName, this.accountNumber)) {
            final String newValue = command.stringValueOfParameterNamed(accountNoParamName);
            actualChanges.put(accountNoParamName, newValue);
            this.accountNumber = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String createSiAtDisbursementParameterName = "createStandingInstructionAtDisbursement";
        if (command.isChangeInBooleanParameterNamed(createSiAtDisbursementParameterName, shouldCreateStandingInstructionAtDisbursement())) {
            final Boolean valueAsInput = command.booleanObjectValueOfParameterNamed(createSiAtDisbursementParameterName);
            actualChanges.put(createSiAtDisbursementParameterName, valueAsInput);
            this.createStandingInstructionAtDisbursement = valueAsInput;
        }

        final String externalIdParamName = "externalId";
        if (command.isChangeInStringParameterNamed(externalIdParamName, this.externalId)) {
            final String newValue = command.stringValueOfParameterNamed(externalIdParamName);
            actualChanges.put(externalIdParamName, newValue);
            this.externalId = StringUtils.defaultIfEmpty(newValue, null);
        }

        // add clientId, groupId and loanType changes to actual changes

        final String clientIdParamName = "clientId";
        final Long clientId = this.client == null ? null : this.client.getId();
        if (command.isChangeInLongParameterNamed(clientIdParamName, clientId)) {
            final Long newValue = command.longValueOfParameterNamed(clientIdParamName);
            actualChanges.put(clientIdParamName, newValue);
        }

        // FIXME: AA - We may require separate api command to move loan from one
        // group to another
        final String groupIdParamName = "groupId";
        final Long groupId = this.group == null ? null : this.group.getId();
        if (command.isChangeInLongParameterNamed(groupIdParamName, groupId)) {
            final Long newValue = command.longValueOfParameterNamed(groupIdParamName);
            actualChanges.put(groupIdParamName, newValue);
        }

        final String productIdParamName = "productId";
        if (command.isChangeInLongParameterNamed(productIdParamName, this.loanProduct.getId())) {
            final Long newValue = command.longValueOfParameterNamed(productIdParamName);
            actualChanges.put(productIdParamName, newValue);
            actualChanges.put("recalculateLoanSchedule", true);
        }

        Long existingFundId = null;
        if (this.fund != null) {
            existingFundId = this.fund.getId();
        }
        final String fundIdParamName = "fundId";
        if (command.isChangeInLongParameterNamed(fundIdParamName, existingFundId)) {
            final Long newValue = command.longValueOfParameterNamed(fundIdParamName);
            actualChanges.put(fundIdParamName, newValue);
        }

        Long existingLoanOfficerId = null;
        if (this.loanOfficer != null) {
            existingLoanOfficerId = this.loanOfficer.getId();
        }
        final String loanOfficerIdParamName = "loanOfficerId";
        if (command.isChangeInLongParameterNamed(loanOfficerIdParamName, existingLoanOfficerId)) {
            final Long newValue = command.longValueOfParameterNamed(loanOfficerIdParamName);
            actualChanges.put(loanOfficerIdParamName, newValue);
        }

        Long existingLoanPurposeId = null;
        if (this.loanPurpose != null) {
            existingLoanPurposeId = this.loanPurpose.getId();
        }
        final String loanPurposeIdParamName = "loanPurposeId";
        if (command.isChangeInLongParameterNamed(loanPurposeIdParamName, existingLoanPurposeId)) {
            final Long newValue = command.longValueOfParameterNamed(loanPurposeIdParamName);
            actualChanges.put(loanPurposeIdParamName, newValue);
        }

        final String strategyIdParamName = "transactionProcessingStrategyId";
        if (command.isChangeInLongParameterNamed(strategyIdParamName, this.transactionProcessingStrategy.getId())) {
            final Long newValue = command.longValueOfParameterNamed(strategyIdParamName);
            actualChanges.put(strategyIdParamName, newValue);
        }

        final String submittedOnDateParamName = "submittedOnDate";
        if (command.isChangeInLocalDateParameterNamed(submittedOnDateParamName, getSubmittedOnDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(submittedOnDateParamName);
            actualChanges.put(submittedOnDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(submittedOnDateParamName);
            this.submittedOnDate = newValue.toDate();
        }

        final String expectedDisbursementDateParamName = "expectedDisbursementDate";
        if (command.isChangeInLocalDateParameterNamed(expectedDisbursementDateParamName, getExpectedDisbursedOnLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(expectedDisbursementDateParamName);
            actualChanges.put(expectedDisbursementDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);
            actualChanges.put("recalculateLoanSchedule", true);

            final LocalDate newValue = command.localDateValueOfParameterNamed(expectedDisbursementDateParamName);
            this.expectedDisbursementDate = newValue.toDate();
            removeFirstDisbursementTransaction();
        }

        final String repaymentsStartingFromDateParamName = "repaymentsStartingFromDate";
        if (command.isChangeInLocalDateParameterNamed(repaymentsStartingFromDateParamName, getExpectedFirstRepaymentOnDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(repaymentsStartingFromDateParamName);
            actualChanges.put(repaymentsStartingFromDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);
            actualChanges.put("recalculateLoanSchedule", true);

            final LocalDate newValue = command.localDateValueOfParameterNamed(repaymentsStartingFromDateParamName);
            if (newValue != null) {
                this.expectedFirstRepaymentOnDate = newValue.toDate();
            } else {
                this.expectedFirstRepaymentOnDate = null;
            }
        }

        final String syncDisbursementParameterName = "syncDisbursementWithMeeting";
        if (command.isChangeInBooleanParameterNamed(syncDisbursementParameterName, isSyncDisbursementWithMeeting())) {
            final Boolean valueAsInput = command.booleanObjectValueOfParameterNamed(syncDisbursementParameterName);
            actualChanges.put(syncDisbursementParameterName, valueAsInput);
            this.syncDisbursementWithMeeting = valueAsInput;
        }

        final String interestChargedFromDateParamName = "interestChargedFromDate";
        if (command.isChangeInLocalDateParameterNamed(interestChargedFromDateParamName, getInterestChargedFromDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(interestChargedFromDateParamName);
            actualChanges.put(interestChargedFromDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);
            actualChanges.put("recalculateLoanSchedule", true);

            final LocalDate newValue = command.localDateValueOfParameterNamed(interestChargedFromDateParamName);
            if (newValue != null) {
                this.interestChargedFromDate = newValue.toDate();
            } else {
                this.interestChargedFromDate = null;
            }
        }

        if (getSubmittedOnDate().isAfter(new LocalDate())) {
            final String errorMessage = "The date on which a loan is submitted cannot be in the future.";
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.a.future.date", errorMessage, getSubmittedOnDate());
        }

        if (!(this.client == null)) {
            if (getSubmittedOnDate().isBefore(this.client.getActivationLocalDate())) {
                final String errorMessage = "The date on which a loan is submitted cannot be earlier than client's activation date.";
                throw new InvalidLoanStateTransitionException("submittal", "cannot.be.before.client.activation.date", errorMessage,
                        getSubmittedOnDate());
            }
        } else if (!(this.group == null)) {
            if (getSubmittedOnDate().isBefore(this.group.getActivationLocalDate())) {
                final String errorMessage = "The date on which a loan is submitted cannot be earlier than groups's activation date.";
                throw new InvalidLoanStateTransitionException("submittal", "cannot.be.before.group.activation.date", errorMessage,
                        getSubmittedOnDate());
            }
        }

        if (getSubmittedOnDate().isAfter(getExpectedDisbursedOnLocalDate())) {
            final String errorMessage = "The date on which a loan is submitted cannot be after its expected disbursement date: "
                    + getExpectedDisbursedOnLocalDate().toString();
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.after.expected.disbursement.date", errorMessage,
                    getSubmittedOnDate(), getExpectedDisbursedOnLocalDate());
        }

        final String chargesParamName = "charges";

        if (isChargesModified) {
            actualChanges.put(chargesParamName, getLoanCharges(possiblyModifedLoanCharges));
            actualChanges.put("recalculateLoanSchedule", true);
        }

        final String collateralParamName = "collateral";
        if (command.parameterExists(collateralParamName)) {

            if (!possiblyModifedLoanCollateralItems.equals(this.collateral)) {
                actualChanges.put(collateralParamName, listOfLoanCollateralData(possiblyModifedLoanCollateralItems));
            }
        }

        final String loanTermFrequencyParamName = "loanTermFrequency";
        if (command.isChangeInIntegerParameterNamed(loanTermFrequencyParamName, this.termFrequency)) {
            final Integer newValue = command.integerValueOfParameterNamed(loanTermFrequencyParamName);
            actualChanges.put(externalIdParamName, newValue);
            this.termFrequency = newValue;
        }

        final String loanTermFrequencyTypeParamName = "loanTermFrequencyType";
        if (command.isChangeInIntegerParameterNamed(loanTermFrequencyTypeParamName, this.termPeriodFrequencyType)) {
            final Integer newValue = command.integerValueOfParameterNamed(loanTermFrequencyTypeParamName);
            final PeriodFrequencyType newTermPeriodFrequencyType = PeriodFrequencyType.fromInt(newValue);
            actualChanges.put(loanTermFrequencyTypeParamName, newTermPeriodFrequencyType.getValue());
            this.termPeriodFrequencyType = newValue;
        }

        final String principalParamName = "principal";
        if (command.isChangeInBigDecimalParameterNamed(principalParamName, this.approvedPrincipal)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(principalParamName);
            this.approvedPrincipal = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(principalParamName, this.proposedPrincipal)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(principalParamName);
            this.proposedPrincipal = newValue;
        }

        if (loanProduct.isMultiDisburseLoan()) {
            updateDisbursementDetails(command, actualChanges);
            if (command.isChangeInBigDecimalParameterNamed(LoanApiConstants.maxOutstandingBalanceParameterName,
                    this.maxOutstandingLoanBalance)) {
                this.maxOutstandingLoanBalance = command
                        .bigDecimalValueOfParameterNamed(LoanApiConstants.maxOutstandingBalanceParameterName);
            }
            final JsonArray disbursementDataArray = command.arrayOfParameterNamed(LoanApiConstants.disbursementDataParameterName);

            if (disbursementDataArray == null || disbursementDataArray.size() == 0) {
                final String errorMessage = "For this loan product, disbursement details must be provided";
                throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
            }
            if (disbursementDataArray.size() > loanProduct.maxTrancheCount()) {
                final String errorMessage = "Number of tranche shouldn't be greter than " + loanProduct.maxTrancheCount();
                throw new ExceedingTrancheCountException(LoanApiConstants.disbursementDataParameterName, errorMessage,
                        loanProduct.maxTrancheCount(), disbursementDetails.size());
            }
        } else {
            this.disbursementDetails.clear();
        }

        if (loanProduct.isMultiDisburseLoan() || loanProduct.canDefineInstallmentAmount()) {
            if (command.isChangeInBigDecimalParameterNamed(LoanApiConstants.emiAmountParameterName, this.fixedEmiAmount)) {
                this.fixedEmiAmount = command.bigDecimalValueOfParameterNamed(LoanApiConstants.emiAmountParameterName);
                actualChanges.put(LoanApiConstants.emiAmountParameterName, this.fixedEmiAmount);
                actualChanges.put("recalculateLoanSchedule", true);
            }
        } else {
            this.fixedEmiAmount = null;
        }

        return actualChanges;
    }

    public void recalculateAllCharges() {
        Set<LoanCharge> charges = this.charges();
        int penaltyWaitPeriod = 0;
        for (final LoanCharge loanCharge : charges) {
            recalculateLoanCharge(loanCharge, penaltyWaitPeriod);
        }
    }

    private boolean isInterestRecalculationEnabledForProduct() {
        return this.loanProduct.isInterestRecalculationEnabled();
    }

    /**
     * Update interest recalculation settings if product configuration changes
     */
    public void updateLoanInterestRecalculationSettings(final LocalDate recalculationRestFrequencyDate, final JsonCommand command,
            final Map<String, Object> actualChanges) {

        if (isInterestRecalculationEnabledForProduct()) {
            Date restFrequencyDate = null;
            if (recalculationRestFrequencyDate != null) {
                restFrequencyDate = recalculationRestFrequencyDate.toDate();
            }
            if (this.loanInterestRecalculationDetails == null) {
                this.loanInterestRecalculationDetails = LoanInterestRecalculationDetails.createFrom(this.loanProduct
                        .getProductInterestRecalculationDetails().getInterestRecalculationCompoundingMethod(), this.loanProduct
                        .getProductInterestRecalculationDetails().getRescheduleStrategyMethod(), this.loanProduct
                        .getProductInterestRecalculationDetails().getRestFrequencyType().getValue(), this.loanProduct
                        .getProductInterestRecalculationDetails().getRestInterval(), restFrequencyDate);
                this.loanInterestRecalculationDetails.updateLoan(this);
            } else {

                this.loanInterestRecalculationDetails.update(command, actualChanges);
            }
        } else {
            this.loanInterestRecalculationDetails = null;
        }
    }

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
        if (loanCharge.isOverdueInstallmentCharge()) {
            amount = calculateOverdueAmountPercentageAppliedTo(loanCharge, penaltyWaitPeriod);
        } else {
            amount = calculateAmountPercentageAppliedTo(loanCharge);
        }

        BigDecimal chargeAmt = BigDecimal.ZERO;
        BigDecimal totalChargeAmt = BigDecimal.ZERO;
        if (loanCharge.getChargeCalculation().isPercentageBased()) {
            chargeAmt = loanCharge.getPercentage();
            if (loanCharge.isInstalmentFee()) {
                totalChargeAmt = calculatePerInstallmentChargeAmount(loanCharge);
            }
        } else {
            chargeAmt = loanCharge.amount();
            if (loanCharge.isInstalmentFee()) {
                chargeAmt = chargeAmt.divide(BigDecimal.valueOf(repaymentScheduleDetail().getNumberOfRepayments()));
            }
        }
        if (loanCharge.isActive()) {
            loanCharge.update(chargeAmt, loanCharge.getDueLocalDate(), amount, repaymentScheduleDetail().getNumberOfRepayments(),
                    totalChargeAmt);
            validateChargeHasValidSpecifiedDateIfApplicable(loanCharge, getDisbursementDate(), getLastRepaymentPeriodDueDate());
        }

    }

    private BigDecimal calculateOverdueAmountPercentageAppliedTo(final LoanCharge loanCharge, final int penaltyWaitPeriod) {
        LoanRepaymentScheduleInstallment installment = loanCharge.getOverdueInstallmentCharge().getInstallment();
        LocalDate graceDate = LocalDate.now().minusDays(penaltyWaitPeriod);
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

    public void updateDisbursementDetails(final JsonCommand command, final Map<String, Object> actualChanges) {

        List<Long> list = fetchDisbursementIds();

        if (command.parameterExists(LoanApiConstants.disbursementDataParameterName)) {
            final JsonArray disbursementDataArray = command.arrayOfParameterNamed(LoanApiConstants.disbursementDataParameterName);
            if (disbursementDataArray != null && disbursementDataArray.size() > 0) {
                String dateFormat = null;
                Locale locale = null;
                if (command.parsedJson().isJsonObject()) {
                    JsonObject topLevel = command.parsedJson().getAsJsonObject();
                    final String dateFormatParameter = "dateFormat";

                    if (topLevel.has(dateFormatParameter) && topLevel.get(dateFormatParameter).isJsonPrimitive()) {
                        final JsonPrimitive primitive = topLevel.get(dateFormatParameter).getAsJsonPrimitive();
                        dateFormat = primitive.getAsString();
                    }

                    final String localeParameter = "locale";
                    if (topLevel.has(localeParameter) && topLevel.get(localeParameter).isJsonPrimitive()) {
                        final JsonPrimitive primitive = topLevel.get(localeParameter).getAsJsonPrimitive();
                        String localeString = primitive.getAsString();
                        locale = JsonParserHelper.localeFromString(localeString);
                    }
                }
                int i = 0;
                do {
                    final JsonObject jsonObject = disbursementDataArray.get(i).getAsJsonObject();
                    Date expectedDisbursementDate = null;
                    BigDecimal principal = null;
                    LocalDate date = null;
                    Long id = null;
                    if (jsonObject.has(LoanApiConstants.disbursementDateParameterName)
                            && jsonObject.get(LoanApiConstants.disbursementDateParameterName) != null
                            && jsonObject.get(LoanApiConstants.disbursementDateParameterName).isJsonPrimitive()) {

                        final JsonPrimitive primitive = jsonObject.get(LoanApiConstants.disbursementDateParameterName).getAsJsonPrimitive();
                        final String valueAsString = primitive.getAsString();
                        if (StringUtils.isNotBlank(valueAsString)) {
                            date = JsonParserHelper.convertFrom(valueAsString, LoanApiConstants.disbursementDateParameterName, dateFormat,
                                    locale);
                        }
                    }
                    if (date != null) {
                        expectedDisbursementDate = date.toDate();
                    }

                    if (jsonObject.has(LoanApiConstants.disbursementPrincipalParameterName)
                            && jsonObject.get(LoanApiConstants.disbursementPrincipalParameterName).isJsonPrimitive()
                            && StringUtils.isNotBlank((jsonObject.get(LoanApiConstants.disbursementPrincipalParameterName).getAsString()))) {
                        principal = jsonObject.getAsJsonPrimitive(LoanApiConstants.disbursementPrincipalParameterName).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanApiConstants.disbursementIdParameterName)
                            && jsonObject.get(LoanApiConstants.disbursementIdParameterName).isJsonPrimitive()
                            && StringUtils.isNotBlank((jsonObject.get(LoanApiConstants.disbursementIdParameterName).getAsString()))) {
                        id = jsonObject.getAsJsonPrimitive(LoanApiConstants.disbursementIdParameterName).getAsLong();
                    }
                    if (id != null) {
                        LoanDisbursementDetails loanDisbursementDetail = fetchLoanDisbursementsById(id);
                        list.remove(id);
                        if (loanDisbursementDetail.actualDisbursementDate() == null) {
                            Date actualDisbursementDate = null;
                            LoanDisbursementDetails disbursementDetails = new LoanDisbursementDetails(expectedDisbursementDate,
                                    actualDisbursementDate, principal);
                            if (!loanDisbursementDetail.equals(disbursementDetails)) {
                                loanDisbursementDetail.copy(disbursementDetails);
                                actualChanges.put("disbursementDetailId", id);
                                actualChanges.put("recalculateLoanSchedule", true);
                            }
                        }
                    } else {
                        Date actualDisbursementDate = null;
                        LoanDisbursementDetails disbursementDetails = new LoanDisbursementDetails(expectedDisbursementDate,
                                actualDisbursementDate, principal);
                        disbursementDetails.updateLoan(this);
                        this.disbursementDetails.add(disbursementDetails);
                        actualChanges.put(LoanApiConstants.disbursementDataParameterName, expectedDisbursementDate + "-" + principal);
                        actualChanges.put("recalculateLoanSchedule", true);
                    }
                    i++;
                } while (i < disbursementDataArray.size());
                for (Long id : list) {
                    this.disbursementDetails.remove(fetchLoanDisbursementsById(id));
                    actualChanges.put("recalculateLoanSchedule", true);
                }
            }
        }
    }

    public LoanDisbursementDetails fetchLoanDisbursementsById(Long id) {
        LoanDisbursementDetails loanDisbursementDetail = null;
        for (LoanDisbursementDetails disbursementDetail : this.disbursementDetails) {
            if (id.equals(disbursementDetail.getId())) {
                loanDisbursementDetail = disbursementDetail;
                break;
            }
        }
        return loanDisbursementDetail;
    }

    private List<Long> fetchDisbursementIds() {
        List<Long> list = new ArrayList<>();
        for (LoanDisbursementDetails disbursementDetails : this.disbursementDetails) {
            list.add(disbursementDetails.getId());
        }
        return list;
    }

    private CollateralData[] listOfLoanCollateralData(final Set<LoanCollateral> setOfLoanCollateral) {

        CollateralData[] existingLoanCollateral = null;

        final List<CollateralData> loanCollateralList = new ArrayList<>();
        for (final LoanCollateral loanCollateral : setOfLoanCollateral) {

            final CollateralData data = loanCollateral.toData();

            loanCollateralList.add(data);
        }

        existingLoanCollateral = loanCollateralList.toArray(new CollateralData[loanCollateralList.size()]);

        return existingLoanCollateral;
    }

    private LoanChargeCommand[] getLoanCharges(final Set<LoanCharge> setOfLoanCharges) {

        LoanChargeCommand[] existingLoanCharges = null;

        final List<LoanChargeCommand> loanChargesList = new ArrayList<>();
        for (final LoanCharge loanCharge : setOfLoanCharges) {
            loanChargesList.add(loanCharge.toCommand());
        }

        existingLoanCharges = loanChargesList.toArray(new LoanChargeCommand[loanChargesList.size()]);

        return existingLoanCharges;
    }

    private void removeFirstDisbursementTransaction() {
        for (final LoanTransaction loanTransaction : this.loanTransactions) {
            if (loanTransaction.isDisbursement()) {
                this.loanTransactions.remove(loanTransaction);
                break;
            }
        }
    }

    public void loanApplicationSubmittal(final AppUser currentUser, final LoanScheduleModel loanSchedule,
            final LoanApplicationTerms loanApplicationTerms, final LoanLifecycleStateMachine lifecycleStateMachine,
            final LocalDate submittedOn, final String externalId, final boolean allowTransactionsOnHoliday, final List<Holiday> holidays,
            final WorkingDays workingDays, final boolean allowTransactionsOnNonWorkingDay, final LocalDate recalculationRestFrequencyDate) {

        updateLoanSchedule(loanSchedule, currentUser);

        LoanStatus from = null;
        if (this.loanStatus != null) {
            from = LoanStatus.fromInt(this.loanStatus);
        }

        final LoanStatus statusEnum = lifecycleStateMachine.transition(LoanEvent.LOAN_CREATED, from);
        this.loanStatus = statusEnum.getValue();

        this.externalId = externalId;
        this.termFrequency = loanApplicationTerms.getLoanTermFrequency();
        this.termPeriodFrequencyType = loanApplicationTerms.getLoanTermPeriodFrequencyType().getValue();
        this.submittedOnDate = submittedOn.toDate();
        this.submittedBy = currentUser;
        this.expectedDisbursementDate = loanApplicationTerms.getExpectedDisbursementDate().toDate();
        this.expectedFirstRepaymentOnDate = loanApplicationTerms.getRepaymentStartFromDate();
        this.interestChargedFromDate = loanApplicationTerms.getInterestChargedFromDate();

        if (loanApplicationTerms.getRepaymentPeriodFrequencyType() == PeriodFrequencyType.MONTHS) {
            this.repaymentFrequencyNthDayType = loanApplicationTerms.getNthDay();
            this.repaymentFrequencyDayOfWeekType = loanApplicationTerms.getWeekDayType().getValue();
        } else {
            this.repaymentFrequencyNthDayType = NthDayType.INVALID.getValue();
            this.repaymentFrequencyDayOfWeekType = DayOfWeekType.INVALID.getValue();
        }

        updateLoanScheduleDependentDerivedFields();

        if (submittedOn.isAfter(DateUtils.getLocalDateOfTenant())) {
            final String errorMessage = "The date on which a loan is submitted cannot be in the future.";
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.a.future.date", errorMessage, submittedOn);
        }

        if (this.client != null && this.client.isActivatedAfter(submittedOn)) {
            final String errorMessage = "The date on which a loan is submitted cannot be earlier than client's activation date.";
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.before.client.activation.date", errorMessage, submittedOn);
        }

        validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_CREATED, submittedOn);

        if (this.group != null && this.group.isActivatedAfter(submittedOn)) {
            final String errorMessage = "The date on which a loan is submitted cannot be earlier than groups's activation date.";
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.before.group.activation.date", errorMessage, submittedOn);
        }

        if (submittedOn.isAfter(getExpectedDisbursedOnLocalDate())) {
            final String errorMessage = "The date on which a loan is submitted cannot be after its expected disbursement date: "
                    + getExpectedDisbursedOnLocalDate().toString();
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.after.expected.disbursement.date", errorMessage,
                    submittedOn, getExpectedDisbursedOnLocalDate());
        }

        // charges are optional
        int penaltyWaitPeriod = 0;
        for (final LoanCharge loanCharge : charges()) {
            recalculateLoanCharge(loanCharge, penaltyWaitPeriod);
        }

        updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());

        // validate if disbursement date is a holiday or a non-working day
        validateDisbursementDateIsOnNonWorkingDay(workingDays, allowTransactionsOnNonWorkingDay);
        validateDisbursementDateIsOnHoliday(allowTransactionsOnHoliday, holidays);

        /**
         * Copy interest recalculation settings if interest recalculation is
         * enabled
         */
        if (this.loanRepaymentScheduleDetail.isInterestRecalculationEnabled()) {
            Date restFrequencyDate = null;
            if (recalculationRestFrequencyDate != null) {
                restFrequencyDate = recalculationRestFrequencyDate.toDate();
            }
            this.loanInterestRecalculationDetails = LoanInterestRecalculationDetails.createFrom(this.loanProduct
                    .getProductInterestRecalculationDetails().getInterestRecalculationCompoundingMethod(), this.loanProduct
                    .getProductInterestRecalculationDetails().getRescheduleStrategyMethod(), this.loanProduct
                    .getProductInterestRecalculationDetails().getRestFrequencyType().getValue(), this.loanProduct
                    .getProductInterestRecalculationDetails().getRestInterval(), restFrequencyDate);
            this.loanInterestRecalculationDetails.updateLoan(this);
        }

    }

    private LocalDate determineExpectedMaturityDate() {
        final int numberOfInstallments = this.repaymentScheduleInstallments.size();
        return this.repaymentScheduleInstallments.get(numberOfInstallments - 1).getDueDate();
    }

    public Map<String, Object> loanApplicationRejection(final AppUser currentUser, final JsonCommand command,
            final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        validateAccountStatus(LoanEvent.LOAN_REJECTED);

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_REJECTED, LoanStatus.fromInt(this.loanStatus));
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
            this.loanStatus = statusEnum.getValue();
            actualChanges.put("status", LoanEnumerations.status(this.loanStatus));

            final LocalDate rejectedOn = command.localDateValueOfParameterNamed("rejectedOnDate");

            final Locale locale = new Locale(command.locale());
            final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);

            this.rejectedOnDate = rejectedOn.toDate();
            this.rejectedBy = currentUser;
            this.closedOnDate = rejectedOn.toDate();
            this.closedBy = currentUser;

            actualChanges.put("locale", command.locale());
            actualChanges.put("dateFormat", command.dateFormat());
            actualChanges.put("rejectedOnDate", rejectedOn.toString(fmt));
            actualChanges.put("closedOnDate", rejectedOn.toString(fmt));

            if (rejectedOn.isBefore(getSubmittedOnDate())) {
                final String errorMessage = "The date on which a loan is rejected cannot be before its submittal date: "
                        + getSubmittedOnDate().toString();
                throw new InvalidLoanStateTransitionException("reject", "cannot.be.before.submittal.date", errorMessage, rejectedOn,
                        getSubmittedOnDate());
            }

            validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_REJECTED, rejectedOn);

            if (rejectedOn.isAfter(DateUtils.getLocalDateOfTenant())) {
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

        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_WITHDRAWN, LoanStatus.fromInt(this.loanStatus));
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
            this.loanStatus = statusEnum.getValue();
            actualChanges.put("status", LoanEnumerations.status(this.loanStatus));

            LocalDate withdrawnOn = command.localDateValueOfParameterNamed("withdrawnOnDate");
            if (withdrawnOn == null) {
                withdrawnOn = command.localDateValueOfParameterNamed("eventDate");
            }

            final Locale locale = new Locale(command.locale());
            final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);

            this.withdrawnOnDate = withdrawnOn.toDate();
            this.withdrawnBy = currentUser;
            this.closedOnDate = withdrawnOn.toDate();
            this.closedBy = currentUser;

            actualChanges.put("locale", command.locale());
            actualChanges.put("dateFormat", command.dateFormat());
            actualChanges.put("withdrawnOnDate", withdrawnOn.toString(fmt));
            actualChanges.put("closedOnDate", withdrawnOn.toString(fmt));

            if (withdrawnOn.isBefore(getSubmittedOnDate())) {
                final String errorMessage = "The date on which a loan is withdrawn cannot be before its submittal date: "
                        + getSubmittedOnDate().toString();
                throw new InvalidLoanStateTransitionException("withdraw", "cannot.be.before.submittal.date", errorMessage, command,
                        getSubmittedOnDate());
            }

            validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_WITHDRAWN, withdrawnOn);

            if (withdrawnOn.isAfter(DateUtils.getLocalDateOfTenant())) {
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
         * statusEnum is holding the possible new status derived from
         * loanLifecycleStateMachine.transition.
         */

        final LoanStatus newStatusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_APPROVED, LoanStatus.fromInt(this.loanStatus));

        /*
         * FIXME: There is no need to check below condition, if
         * loanLifecycleStateMachine.transition is doing it's responsibility
         * properly. Better implementation approach is, if code passes invalid
         * combination of states (fromState and toState), state machine should
         * return invalidate state and below if condition should check for not
         * equal to invalidateState, instead of check new value is same as
         * present value.
         */

        if (!newStatusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
            this.loanStatus = newStatusEnum.getValue();
            actualChanges.put("status", LoanEnumerations.status(this.loanStatus));

            // only do below if status has changed in the 'approval' case
            LocalDate approvedOn = command.localDateValueOfParameterNamed("approvedOnDate");
            String approvedOnDateChange = command.stringValueOfParameterNamed("approvedOnDate");
            if (approvedOn == null) {
                approvedOn = command.localDateValueOfParameterNamed("eventDate");
                approvedOnDateChange = command.stringValueOfParameterNamed("eventDate");
            }

            LocalDate expecteddisbursementDate = command.localDateValueOfParameterNamed("expectedDisbursementDate");

            BigDecimal approvedLoanAmount = command.bigDecimalValueOfParameterNamed(LoanApiConstants.approvedLoanAmountParameterName);

            if (approvedLoanAmount != null) {

                // Approved amount has to be less than or equal to principal
                // amount demanded

                if (approvedLoanAmount.compareTo(this.proposedPrincipal) == -1) {

                    this.approvedPrincipal = approvedLoanAmount;

                    /*
                     * All the calculations are done based on the principal
                     * amount, so it is necessary to set principal amount to
                     * approved amount
                     */

                    this.loanRepaymentScheduleDetail.setPrincipal(approvedLoanAmount);

                    actualChanges.put(LoanApiConstants.approvedLoanAmountParameterName, approvedLoanAmount);
                    actualChanges.put(LoanApiConstants.disbursementPrincipalParameterName, approvedLoanAmount);
                } else if (approvedLoanAmount.compareTo(this.proposedPrincipal) == 1) {
                    final String errorMessage = "Loan approved amount can't be greater than loan amount demanded.";
                    throw new InvalidLoanStateTransitionException("approval", "amount.can't.be.greater.than.loan.amount.demanded",
                            errorMessage, this.proposedPrincipal, approvedLoanAmount);
                }

                /* Update disbursement details */
                if (disbursementDataArray != null) {
                    updateDisbursementDetails(command, actualChanges);
                }
            }
            if (loanProduct.isMultiDisburseLoan()) {

                if (this.disbursementDetails.isEmpty()) {
                    final String errorMessage = "For this loan product, disbursement details must be provided";
                    throw new MultiDisbursementDataRequiredException(LoanApiConstants.disbursementDataParameterName, errorMessage);
                }

                if (this.disbursementDetails.size() > loanProduct.maxTrancheCount()) {
                    final String errorMessage = "Number of tranche shouldn't be greter than " + loanProduct.maxTrancheCount();
                    throw new ExceedingTrancheCountException(LoanApiConstants.disbursementDataParameterName, errorMessage,
                            loanProduct.maxTrancheCount(), disbursementDetails.size());
                }
            }
            this.approvedOnDate = approvedOn.toDate();
            this.approvedBy = currentUser;
            actualChanges.put("locale", command.locale());
            actualChanges.put("dateFormat", command.dateFormat());
            actualChanges.put("approvedOnDate", approvedOnDateChange);

            final LocalDate submittalDate = new LocalDate(this.submittedOnDate);
            if (approvedOn.isBefore(submittalDate)) {
                final String errorMessage = "The date on which a loan is approved cannot be before its submittal date: "
                        + submittalDate.toString();
                throw new InvalidLoanStateTransitionException("approval", "cannot.be.before.submittal.date", errorMessage,
                        getApprovedOnDate(), submittalDate);
            }

            if (expecteddisbursementDate != null) {
                this.expectedDisbursementDate = expecteddisbursementDate.toDate();
                actualChanges.put("expectedDisbursementDate", expectedDisbursementDate);

                if (expecteddisbursementDate.isBefore(approvedOn)) {
                    final String errorMessage = "The expected disbursement date should be either on or after the approval date: "
                            + approvedOn.toString();
                    throw new InvalidLoanStateTransitionException("expecteddisbursal", "should.be.on.or.after.approval.date", errorMessage,
                            getApprovedOnDate(), expecteddisbursementDate);
                }
            }

            validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_APPROVED, approvedOn);

            if (approvedOn.isAfter(DateUtils.getLocalDateOfTenant())) {
                final String errorMessage = "The date on which a loan is approved cannot be in the future.";
                throw new InvalidLoanStateTransitionException("approval", "cannot.be.a.future.date", errorMessage, getApprovedOnDate());
            }

            if (this.loanOfficer != null) {
                final LoanOfficerAssignmentHistory loanOfficerAssignmentHistory = LoanOfficerAssignmentHistory.createNew(this,
                        this.loanOfficer, approvedOn);
                this.loanOfficerHistory.add(loanOfficerAssignmentHistory);
            }
        }

        return actualChanges;
    }

    public Map<String, Object> undoApproval(final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        validateAccountStatus(LoanEvent.LOAN_APPROVAL_UNDO);
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final LoanStatus currentStatus = LoanStatus.fromInt(this.loanStatus);
        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_APPROVAL_UNDO, currentStatus);
        if (!statusEnum.hasStateOf(currentStatus)) {
            this.loanStatus = statusEnum.getValue();
            actualChanges.put("status", LoanEnumerations.status(this.loanStatus));

            this.approvedOnDate = null;
            this.approvedBy = null;

            if (this.approvedPrincipal.compareTo(this.proposedPrincipal) != 0) {
                this.approvedPrincipal = this.proposedPrincipal;
                this.loanRepaymentScheduleDetail.setPrincipal(this.proposedPrincipal);

                actualChanges.put(LoanApiConstants.approvedLoanAmountParameterName, this.proposedPrincipal);
                actualChanges.put(LoanApiConstants.disbursementPrincipalParameterName, this.proposedPrincipal);

            }

            actualChanges.put("approvedOnDate", "");

            this.loanOfficerHistory.clear();
        }

        return actualChanges;
    }

    public Collection<Long> findExistingTransactionIds() {

        final Collection<Long> ids = new ArrayList<>();

        for (final LoanTransaction transaction : this.loanTransactions) {
            ids.add(transaction.getId());
        }

        return ids;
    }

    public Collection<Long> findExistingReversedTransactionIds() {

        final Collection<Long> ids = new ArrayList<>();

        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isReversed()) {
                ids.add(transaction.getId());
            }
        }

        return ids;
    }

    public ChangedTransactionDetail disburse(final AppUser currentUser, final JsonCommand command, final Map<String, Object> actualChanges,
            final ScheduleGeneratorDTO scheduleGeneratorDTO) {

        final LoanStatus statusEnum = this.loanLifecycleStateMachine.transition(LoanEvent.LOAN_DISBURSED,
                LoanStatus.fromInt(this.loanStatus));

        final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");

        this.loanStatus = statusEnum.getValue();
        actualChanges.put("status", LoanEnumerations.status(this.loanStatus));

        this.disbursedBy = currentUser;
        updateLoanScheduleDependentDerivedFields();

        actualChanges.put("locale", command.locale());
        actualChanges.put("dateFormat", command.dateFormat());
        actualChanges.put("actualDisbursementDate", command.stringValueOfParameterNamed("actualDisbursementDate"));

        HolidayDetailDTO holidayDetailDTO = scheduleGeneratorDTO.getHolidayDetailDTO();

        // validate if disbursement date is a holiday or a non-working day
        validateDisbursementDateIsOnNonWorkingDay(holidayDetailDTO.getWorkingDays(), holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());
        validateDisbursementDateIsOnHoliday(holidayDetailDTO.isAllowTransactionsOnHoliday(), holidayDetailDTO.getHolidays());

        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()
                && (fetchRepaymentScheduleInstallment(1).getDueDate().isBefore(LocalDate.now()) || isDisbursementMissed())) {
            regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO, currentUser);
        }

        updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
        updateLoanRepaymentPeriodsDerivedFields(actualDisbursementDate);
        updateLoanSummaryDerivedFields();
        LocalDateTime createdDate = DateUtils.getLocalDateTimeOfTenant();
        handleDisbursementTransaction(actualDisbursementDate, createdDate, currentUser);

        final Money interestApplied = Money.of(getCurrency(), this.summary.getTotalInterestCharged());

        /**
         * Add an interest applied transaction of the interest is accrued
         * upfront (Up front accrual), no accounting or cash based accounting is
         * selected
         **/

        if (isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()) {
            final LoanTransaction interestAppliedTransaction = LoanTransaction.accrueInterest(getOffice(), this, interestApplied,
                    actualDisbursementDate, createdDate, currentUser);
            this.loanTransactions.add(interestAppliedTransaction);
        }

        return reprocessTransactionForDisbursement();

    }

    public void regenerateScheduleOnDisbursement(final ScheduleGeneratorDTO scheduleGeneratorDTO, final boolean recalculateSchedule,
            final LocalDate actualDisbursementDate, BigDecimal emiAmount, final AppUser currentUser) {
        boolean isEmiAmountChanged = false;
        if ((this.loanProduct.isMultiDisburseLoan() || this.loanProduct.canDefineInstallmentAmount()) && emiAmount != null
                && emiAmount.compareTo(retriveLastEmiAmount()) != 0) {
            if (this.loanProduct.isMultiDisburseLoan()) {
                LoanTermVariations loanVariationTerms = new LoanTermVariations(LoanTermVariationType.EMI_AMOUNT.getValue(),
                        actualDisbursementDate.toDate(), emiAmount, this);
                this.loanTermVariations.add(loanVariationTerms);
            } else {
                this.fixedEmiAmount = emiAmount;
            }
            isEmiAmountChanged = true;
        }

        if (isRepaymentScheduleRegenerationRequiredForDisbursement(actualDisbursementDate) || recalculateSchedule || isEmiAmountChanged) {
            regenerateRepaymentSchedule(scheduleGeneratorDTO, currentUser);
        }
    }

    public boolean canDisburse(final LocalDate actualDisbursementDate) {
        Date lastDusburseDate = this.actualDisbursementDate;
        final LoanStatus statusEnum = this.loanLifecycleStateMachine.transition(LoanEvent.LOAN_DISBURSED,
                LoanStatus.fromInt(this.loanStatus));

        boolean isMultiTrancheDisburse = false;
        if (LoanStatus.fromInt(this.loanStatus).isActive() && isAllTranchesNotDisbursed()) {
            LoanDisbursementDetails details = fetchLastDisburseDetail();

            if (details != null) {
                lastDusburseDate = details.actualDisbursementDate();
            }
            if (actualDisbursementDate.toDate().before(lastDusburseDate)) {
                final String errorMsg = "Loan can't be disbursed before " + lastDusburseDate;
                throw new LoanDisbursalException(errorMsg, "actualdisbursementdate.before.lastdusbursedate", lastDusburseDate,
                        actualDisbursementDate.toDate());
            }
            isMultiTrancheDisburse = true;
        }
        return (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus)) || isMultiTrancheDisburse);
    }

    public Money adjustDisburseAmount(final JsonCommand command, final LocalDate actualDisbursementDate) {
        Money disburseAmount = this.loanRepaymentScheduleDetail.getPrincipal().zero();
        BigDecimal principalDisbursed = command.bigDecimalValueOfParameterNamed(LoanApiConstants.principalDisbursedParameterName);
        if (this.actualDisbursementDate == null) {
            this.actualDisbursementDate = actualDisbursementDate.toDate();
        }
        BigDecimal diff = BigDecimal.ZERO;
        Collection<LoanDisbursementDetails> details = fetchUndisbursedDetail();
        if (principalDisbursed == null) {
            disburseAmount = this.loanRepaymentScheduleDetail.getPrincipal();
            if (!details.isEmpty()) {
                disburseAmount = disburseAmount.zero();
                for (LoanDisbursementDetails disbursementDetails : details) {
                    disbursementDetails.updateActualDisbursementDate(actualDisbursementDate.toDate());
                    disburseAmount = disburseAmount.plus(disbursementDetails.principal());
                }
            }
        } else {
            disburseAmount = disburseAmount.plus(principalDisbursed);

            if (details.isEmpty()) {
                diff = this.loanRepaymentScheduleDetail.getPrincipal().minus(principalDisbursed).getAmount();
            } else {
                for (LoanDisbursementDetails disbursementDetails : details) {
                    disbursementDetails.updateActualDisbursementDate(actualDisbursementDate.toDate());
                    disbursementDetails.updatePrincipal(principalDisbursed);
                }
            }
            if (this.loanProduct().isMultiDisburseLoan()) {
                Collection<LoanDisbursementDetails> loanDisburseDetails = this.getDisbursementDetails();
                BigDecimal setPrincipalAmount = BigDecimal.ZERO;
                for (LoanDisbursementDetails disbursementDetails : loanDisburseDetails) {
                    setPrincipalAmount = setPrincipalAmount.add(disbursementDetails.principal());
                }
                this.loanRepaymentScheduleDetail.setPrincipal(setPrincipalAmount);
                if (setPrincipalAmount.compareTo(this.approvedPrincipal) == 1) {
                    final String errorMsg = "Loan can't be disbursed,disburse amount is exceeding approved principal ";
                    throw new LoanDisbursalException(errorMsg, "disburse.amount.must.be.less.than.approved.principal", principalDisbursed,
                            this.approvedPrincipal);
                }
            } else {
                this.loanRepaymentScheduleDetail.setPrincipal(this.loanRepaymentScheduleDetail.getPrincipal().minus(diff).getAmount());
            }
            if (!(this.loanProduct().isMultiDisburseLoan()) && diff.compareTo(BigDecimal.ZERO) == -1) {
                final String errorMsg = "Loan can't be disbursed,disburse amount is exceeding approved amount ";
                throw new LoanDisbursalException(errorMsg, "disburse.amount.must.be.less.than.approved.amount", principalDisbursed,
                        this.loanRepaymentScheduleDetail.getPrincipal().getAmount());
            }
        }
        return disburseAmount;
    }

    private ChangedTransactionDetail reprocessTransactionForDisbursement() {
        ChangedTransactionDetail changedTransactionDetail = null;

        if (this.loanProduct.isMultiDisburseLoan()) {
            if (!this.actualDisbursementDate.equals(fetchLastDisburseDetail().getDisbursementDate())) {
                final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                        .determineProcessor(this.transactionProcessingStrategy);
                final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
                changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(),
                        allNonContraTransactionsPostDisbursement, getCurrency(), this.repaymentScheduleInstallments, charges(),
                        getLastUserTransactionForChargeCalc());
                for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                    mapEntry.getValue().updateLoan(this);
                }

            }
            updateLoanSummaryDerivedFields();
        }

        return changedTransactionDetail;
    }

    private Collection<LoanDisbursementDetails> fetchUndisbursedDetail() {
        Collection<LoanDisbursementDetails> disbursementDetails = new ArrayList<>();
        Date date = null;
        for (LoanDisbursementDetails disbursementDetail : this.disbursementDetails) {
            if (disbursementDetail.actualDisbursementDate() == null) {
                if (date == null || disbursementDetail.expectedDisbursementDate().equals(date)) {
                    disbursementDetails.add(disbursementDetail);
                    date = disbursementDetail.expectedDisbursementDate();
                } else if (disbursementDetail.expectedDisbursementDate().before(date)) {
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
        Date date = this.actualDisbursementDate;
        if (date != null) {
            for (LoanDisbursementDetails disbursementDetail : this.disbursementDetails) {
                if (disbursementDetail.actualDisbursementDate() != null) {
                    if (disbursementDetail.actualDisbursementDate().after(date) || disbursementDetail.actualDisbursementDate().equals(date)) {
                        date = disbursementDetail.actualDisbursementDate();
                        details = disbursementDetail;
                    }
                }
            }
        }
        return details;
    }

    private boolean isDisbursementMissed() {
        boolean isDisbursementMissed = false;
        for (LoanDisbursementDetails disbursementDetail : this.disbursementDetails) {
            if (disbursementDetail.actualDisbursementDate() == null
                    && LocalDate.now().isAfter(disbursementDetail.expectedDisbursementDateAsLocalDate())) {
                isDisbursementMissed = true;
                break;
            }
        }
        return isDisbursementMissed;
    }

    public BigDecimal getDisbursedAmount() {
        BigDecimal principal = BigDecimal.ZERO;
        for (LoanDisbursementDetails disbursementDetail : this.disbursementDetails) {
            if (disbursementDetail.actualDisbursementDate() != null) {
                principal = principal.add(disbursementDetail.principal());
            }
        }
        return principal;
    }

    private void removeDisbursementDetail() {
        Set<LoanDisbursementDetails> details = new HashSet<>(this.disbursementDetails);
        for (LoanDisbursementDetails disbursementDetail : details) {
            if (disbursementDetail.actualDisbursementDate() == null) {
                this.disbursementDetails.remove(disbursementDetail);
            }
        }
    }

    private boolean isDisbursementAllowed() {
        boolean isAllowed = false;
        for (LoanDisbursementDetails disbursementDetail : this.disbursementDetails) {
            if (disbursementDetail.actualDisbursementDate() == null) {
                isAllowed = true;
                break;
            }
        }
        return isAllowed;
    }

    private boolean atleastOnceDisbursed() {
        boolean isDisbursed = false;
        for (LoanDisbursementDetails disbursementDetail : this.disbursementDetails) {
            if (disbursementDetail.actualDisbursementDate() != null) {
                isDisbursed = true;
                break;
            }
        }
        return isDisbursed;
    }

    private void updateLoanRepaymentPeriodsDerivedFields(final LocalDate actualDisbursementDate) {

        for (final LoanRepaymentScheduleInstallment repaymentPeriod : this.repaymentScheduleInstallments) {
            repaymentPeriod.updateDerivedFields(loanCurrency(), actualDisbursementDate);
        }
    }

    /*
     * Ability to regenerate the repayment schedule based on the loans current
     * details/state.
     */
    public void regenerateRepaymentSchedule(final ScheduleGeneratorDTO scheduleGeneratorDTO, AppUser currentUser) {

        final LoanScheduleModel loanSchedule = regenerateScheduleModel(scheduleGeneratorDTO);

        updateLoanSchedule(loanSchedule, currentUser);
        Set<LoanCharge> charges = this.charges();
        for (LoanCharge loanCharge : charges) {
            recalculateLoanCharge(loanCharge, scheduleGeneratorDTO.getPenaltyWaitPeriod());
        }
    }

    public LoanScheduleModel regenerateScheduleModel(final ScheduleGeneratorDTO scheduleGeneratorDTO) {
        final InterestMethod interestMethod = this.loanRepaymentScheduleDetail.getInterestMethod();
        final LoanScheduleGenerator loanScheduleGenerator = scheduleGeneratorDTO.getLoanScheduleFactory().create(interestMethod);

        final RoundingMode roundingMode = RoundingMode.HALF_EVEN;
        final MathContext mc = new MathContext(8, roundingMode);

        final Integer loanTermFrequency = this.termFrequency;
        final PeriodFrequencyType loanTermPeriodFrequencyType = PeriodFrequencyType.fromInt(this.termPeriodFrequencyType);
        final NthDayType nthDayType = NthDayType.fromInt(this.repaymentFrequencyNthDayType);
        final DayOfWeekType dayOfWeekType = DayOfWeekType.fromInt(this.repaymentFrequencyDayOfWeekType);
        final List<DisbursementData> disbursementData = new ArrayList<>();
        for (LoanDisbursementDetails disbursementDetails : this.disbursementDetails) {
            disbursementData.add(disbursementDetails.toData());
        }
        final List<LoanTermVariationsData> loanVariationTermsData = new ArrayList<>();
        boolean isDefaultEmiAmountReq = true;
        for (LoanTermVariations variationTerms : this.loanTermVariations) {
            if (variationTerms.getTermType().isEMIAmountVariation()) {
                if (variationTerms.getTermApplicableFrom().equals(this.getDisbursementDate().toDate())) {
                    isDefaultEmiAmountReq = false;
                }
                loanVariationTermsData.add(variationTerms.toData());
            }
        }
        if (isDefaultEmiAmountReq) {
            LoanTermVariationsData data = new LoanTermVariationsData(null,
                    LoanEnumerations.loanvariationType(LoanTermVariationType.EMI_AMOUNT), this.getDisbursementDate(), this.fixedEmiAmount);
            loanVariationTermsData.add(data);
        }

        CalendarInstance restCalendarInstance = null;
        RecalculationFrequencyType recalculationFrequencyType = null;
        InterestRecalculationCompoundingMethod compoundingMethod = null;
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = scheduleGeneratorDTO.getCalendarInstanceForInterestRecalculation();
            recalculationFrequencyType = this.loanProduct.getProductInterestRecalculationDetails().getRestFrequencyType();
            compoundingMethod = InterestRecalculationCompoundingMethod.fromInt(this.loanProduct.getProductInterestRecalculationDetails()
                    .getInterestRecalculationCompoundingMethod());
        }

        final LoanApplicationTerms loanApplicationTerms = LoanApplicationTerms
                .assembleFrom(scheduleGeneratorDTO.getApplicationCurrency(), loanTermFrequency, loanTermPeriodFrequencyType, nthDayType,
                        dayOfWeekType, getDisbursementDate(), getExpectedFirstRepaymentOnDate(),
                        scheduleGeneratorDTO.getCalculatedRepaymentsStartingFromDate(), getInArrearsTolerance(),
                        this.loanRepaymentScheduleDetail, this.loanProduct.isMultiDisburseLoan(), this.fixedEmiAmount, disbursementData,
                        this.maxOutstandingLoanBalance, loanVariationTermsData, getInterestChargedFromDate(),
                        this.loanProduct.getPrincipalThresholdForLastInstallment(), this.loanProduct.getInstallmentAmountInMultiplesOf(),
                        recalculationFrequencyType, restCalendarInstance, compoundingMethod,
                        this.loanProduct.preCloseInterestCalculationStrategy());

        final LoanScheduleModel loanSchedule = loanScheduleGenerator.generate(mc, loanApplicationTerms, charges(),
                scheduleGeneratorDTO.getHolidayDetailDTO());
        return loanSchedule;
    }

    private void handleDisbursementTransaction(final LocalDate disbursedOn, final LocalDateTime createdDate, final AppUser currentUser) {

        // add repayment transaction to track incoming money from client to mfi
        // for (charges due at time of disbursement)

        /***
         * TODO Vishwas: do we need to be able to pass in payment type details
         * for repayments at disbursements too?
         ***/
        final Money totalFeeChargesDueAtDisbursement = this.summary.getTotalFeeChargesDueAtDisbursement(loanCurrency());
        /**
         * all Charges repaid at disbursal is marked as repaid and
         * "APPLY Charge" transactions are created for all other fees ( which
         * are created during disbursal but not repaid)
         **/
        if (disbursedOn.toDate().equals(this.actualDisbursementDate)) {
            Money disbursentMoney = Money.zero(getCurrency());
            final LoanTransaction chargesPayment = LoanTransaction.repaymentAtDisbursement(getOffice(), disbursentMoney, null, disbursedOn,
                    null, createdDate, currentUser);
            final Integer installmentNumber = null;
            for (final LoanCharge charge : charges()) {
                if (charge.isDueAtDisbursement()) {
                    if (totalFeeChargesDueAtDisbursement.isGreaterThanZero()
                            && !charge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
                        charge.markAsFullyPaid();
                        // Add "Loan Charge Paid By" details to this transaction
                        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(chargesPayment, charge, charge.amount(),
                                installmentNumber);
                        chargesPayment.getLoanChargesPaid().add(loanChargePaidBy);
                        disbursentMoney = disbursentMoney.plus(charge.amount());
                    }
                } else {
                    /**
                     * create a Charge applied transaction if Upfront Accrual,
                     * None or Cash based accounting is enabled
                     **/
                    if (isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()) {
                        handleChargeAppliedTransaction(charge, disbursedOn, currentUser);
                    }
                }
            }
            if (disbursentMoney.isGreaterThanZero()) {
                final Money zero = Money.zero(getCurrency());
                chargesPayment.updateComponentsAndTotal(zero, zero, disbursentMoney, zero);
                chargesPayment.updateLoan(this);
                this.loanTransactions.add(chargesPayment);
                updateLoanOutstandingBalaces();
            }
        }

        if (getApprovedOnDate() != null && disbursedOn.isBefore(getApprovedOnDate())) {
            final String errorMessage = "The date on which a loan is disbursed cannot be before its approval date: "
                    + getApprovedOnDate().toString();
            throw new InvalidLoanStateTransitionException("disbursal", "cannot.be.before.approval.date", errorMessage, disbursedOn,
                    getApprovedOnDate());
        }

        if (getExpectedFirstRepaymentOnDate() != null && disbursedOn.isAfter(getExpectedFirstRepaymentOnDate())
                && disbursedOn.toDate().equals(this.actualDisbursementDate)) {
            final String errorMessage = "submittedOnDate cannot be after the loans  expectedFirstRepaymentOnDate: "
                    + getExpectedFirstRepaymentOnDate().toString();
            throw new InvalidLoanStateTransitionException("disbursal", "cannot.be.after.expected.first.repayment.date", errorMessage,
                    disbursedOn, getExpectedFirstRepaymentOnDate());
        }

        validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_DISBURSED, disbursedOn);

        if (disbursedOn.isAfter(new LocalDate())) {
            final String errorMessage = "The date on which a loan with identifier : " + this.accountNumber
                    + " is disbursed cannot be in the future.";
            throw new InvalidLoanStateTransitionException("disbursal", "cannot.be.a.future.date", errorMessage, disbursedOn);
        }

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
        this.loanTransactions.add(chargesPayment);
        updateLoanOutstandingBalaces();
        charge.markAsFullyPaid();
        return chargesPayment;
    }

    public Map<String, Object> undoDisbursal(final ScheduleGeneratorDTO scheduleGeneratorDTO, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, AppUser currentUser) {

        validateAccountStatus(LoanEvent.LOAN_DISBURSAL_UNDO);

        final Map<String, Object> actualChanges = new LinkedHashMap<>();
        final LoanStatus currentStatus = LoanStatus.fromInt(this.loanStatus);
        final LoanStatus statusEnum = this.loanLifecycleStateMachine.transition(LoanEvent.LOAN_DISBURSAL_UNDO, currentStatus);
        validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_DISBURSAL_UNDO, getDisbursementDate());
        if (!statusEnum.hasStateOf(currentStatus)) {
            this.loanStatus = statusEnum.getValue();
            actualChanges.put("status", LoanEnumerations.status(this.loanStatus));

            final LocalDate actualDisbursementDate = getDisbursementDate();
            final boolean isScheduleRegenerateRequired = isRepaymentScheduleRegenerationRequiredForDisbursement(actualDisbursementDate);
            this.actualDisbursementDate = null;
            this.disbursedBy = null;
            boolean isDisbursedAmountChanged = !this.approvedPrincipal.equals(this.loanRepaymentScheduleDetail.getPrincipal());
            this.loanRepaymentScheduleDetail.setPrincipal(this.approvedPrincipal);
            if (this.loanProduct.isMultiDisburseLoan()) {
                for (final LoanDisbursementDetails details : this.disbursementDetails) {
                    details.updateActualDisbursementDate(null);
                }
            }
            boolean isEmiAmountChanged = this.loanTermVariations.size() > 0;
            updateLoanToPreDisbursalState();
            if (isScheduleRegenerateRequired || isDisbursedAmountChanged || isEmiAmountChanged
                    || this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                // clear off actual disbusrement date so schedule regeneration
                // uses expected date.

                regenerateRepaymentSchedule(scheduleGeneratorDTO, currentUser);
                if (isDisbursedAmountChanged) {
                    updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
                }
            }

            actualChanges.put("actualDisbursementDate", "");

            existingTransactionIds.addAll(findExistingTransactionIds());
            existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());
            this.accruedTill = null;
            reverseExistingTransactions();
            updateLoanSummaryDerivedFields();

        }

        return actualChanges;
    }

    private final void reverseExistingTransactions() {

        for (final LoanTransaction transaction : this.loanTransactions) {
            transaction.reverse();
        }
    }

    private void updateLoanToPreDisbursalState() {
        this.actualDisbursementDate = null;

        for (final LoanCharge charge : charges()) {
            if (charge.isOverdueInstallmentCharge()) {
                charge.setActive(false);
            } else {
                charge.resetToOriginal(loanCurrency());
            }
        }

        for (final LoanRepaymentScheduleInstallment currentInstallment : this.repaymentScheduleInstallments) {
            currentInstallment.resetDerivedComponents();
        }

        this.loanTermVariations.clear();
        final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
        wrapper.reprocess(getCurrency(), getDisbursementDate(), this.repaymentScheduleInstallments, charges(),
                getLastUserTransactionForChargeCalc());

        updateLoanSummaryDerivedFields();
    }

    public ChangedTransactionDetail waiveInterest(final LoanTransaction waiveInterestTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, final ScheduleGeneratorDTO scheduleGeneratorDTO, final AppUser currentUser) {

        validateAccountStatus(LoanEvent.LOAN_REPAYMENT_OR_WAIVER);

        validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.LOAN_REPAYMENT_OR_WAIVER,
                waiveInterestTransaction.getTransactionDate());
        validateActivityNotBeforeLastTransactionDate(LoanEvent.LOAN_REPAYMENT_OR_WAIVER, waiveInterestTransaction.getTransactionDate());

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        final ChangedTransactionDetail changedTransactionDetail = handleRepaymentOrRecoveryOrWaiverTransaction(waiveInterestTransaction,
                loanLifecycleStateMachine, null, scheduleGeneratorDTO, currentUser);

        return changedTransactionDetail;
    }

    public ChangedTransactionDetail makeRepayment(final LoanTransaction repaymentTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, boolean isRecoveryRepayment, final ScheduleGeneratorDTO scheduleGeneratorDTO,
            final AppUser currentUser) {

        LoanEvent event = null;
        if (isRecoveryRepayment) {
            event = LoanEvent.LOAN_RECOVERY_PAYMENT;
        } else {
            event = LoanEvent.LOAN_REPAYMENT_OR_WAIVER;
        }

        HolidayDetailDTO holidayDetailDTO = scheduleGeneratorDTO.getHolidayDetailDTO();
        validateAccountStatus(event);
        validateActivityNotBeforeClientOrGroupTransferDate(event, repaymentTransaction.getTransactionDate());
        validateActivityNotBeforeLastTransactionDate(event, repaymentTransaction.getTransactionDate());
        validateRepaymentDateIsOnHoliday(repaymentTransaction.getTransactionDate(), holidayDetailDTO.isAllowTransactionsOnHoliday(),
                holidayDetailDTO.getHolidays());
        validateRepaymentDateIsOnNonWorkingDay(repaymentTransaction.getTransactionDate(), holidayDetailDTO.getWorkingDays(),
                holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        final ChangedTransactionDetail changedTransactionDetail = handleRepaymentOrRecoveryOrWaiverTransaction(repaymentTransaction,
                loanLifecycleStateMachine, null, scheduleGeneratorDTO, currentUser);

        return changedTransactionDetail;
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

        if (paymentTransaction.getTransactionDate().isAfter(new LocalDate())) {
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

        if (status().isOverpaid()) {
            if (this.totalOverpaid.compareTo(loanTransaction.getAmount(getCurrency()).getAmount()) == -1) {
                final String errorMessage = "The refund amount must be less than or equal to overpaid amount ";
                throw new InvalidLoanStateTransitionException("transaction", "is.exceeding.overpaid.amount", errorMessage,
                        this.totalOverpaid, loanTransaction.getAmount(getCurrency()).getAmount());
            } else if (!isAfterLatRepayment(loanTransaction, this.loanTransactions)) {
                final String errorMessage = "Transfer funds is allowed only after last repayment date";
                throw new InvalidLoanStateTransitionException("transaction", "is.not.after.repayment.date", errorMessage);
            }
        } else {
            final String errorMessage = "Transfer funds is allowed only for loan accounts with overpaid status ";
            throw new InvalidLoanStateTransitionException("transaction", "is.not.a.overpaid.loan", errorMessage);
        }
        loanTransaction.updateLoan(this);

        if (loanTransaction.isNotZero(loanCurrency())) {
            this.loanTransactions.add(loanTransaction);
        }
        updateLoanSummaryDerivedFields();
        doPostLoanTransactionChecks(loanTransaction.getTransactionDate(), loanLifecycleStateMachine);
    }

    private ChangedTransactionDetail handleRepaymentOrRecoveryOrWaiverTransaction(final LoanTransaction loanTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final LoanTransaction adjustedTransaction,
            final ScheduleGeneratorDTO scheduleGeneratorDTO, final AppUser currentUser) {

        ChangedTransactionDetail changedTransactionDetail = null;

        LoanStatus statusEnum = null;

        LocalDate recalculateFrom = loanTransaction.getTransactionDate();
        if (adjustedTransaction != null && adjustedTransaction.getTransactionDate().isBefore(recalculateFrom)) {
            recalculateFrom = adjustedTransaction.getTransactionDate();
        }

        if (loanTransaction.isRecoveryRepayment()) {
            statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_RECOVERY_PAYMENT, LoanStatus.fromInt(this.loanStatus));
        } else {
            statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_REPAYMENT_OR_WAIVER, LoanStatus.fromInt(this.loanStatus));
        }

        this.loanStatus = statusEnum.getValue();

        loanTransaction.updateLoan(this);

        final boolean isTransactionChronologicallyLatest = isChronologicallyLatestRepaymentOrWaiver(loanTransaction, this.loanTransactions);

        if (loanTransaction.isNotZero(loanCurrency())) {
            this.loanTransactions.add(loanTransaction);
        }

        if (loanTransaction.isNotRepayment() && loanTransaction.isNotWaiver() && loanTransaction.isNotRecoveryRepayment()) {
            final String errorMessage = "A transaction of type repayment or recovery repayment or waiver was expected but not received.";
            throw new InvalidLoanTransactionTypeException("transaction", "is.not.a.repayment.or.waiver.or.recovery.transaction",
                    errorMessage);
        }

        final LocalDate loanTransactionDate = loanTransaction.getTransactionDate();
        if (loanTransactionDate.isBefore(getDisbursementDate())) {
            final String errorMessage = "The transaction date cannot be before the loan disbursement date: "
                    + getApprovedOnDate().toString();
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.before.disbursement.date", errorMessage,
                    loanTransactionDate, getDisbursementDate());
        }

        if (loanTransactionDate.isAfter(DateUtils.getLocalDateOfTenant())) {
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
            if (totalDisbursed.compareTo(this.summary.getTotalPrincipalRepaid()) < 0) {
                final String errorMessage = "The transaction cannot be done before the loan disbursement: "
                        + getApprovedOnDate().toString();
                throw new InvalidLoanStateTransitionException("transaction", "cannot.be.done.before.disbursement", errorMessage);
            }
        }

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);

        final LoanRepaymentScheduleInstallment currentInstallment = fetchLoanRepaymentScheduleInstallment(loanTransaction
                .getTransactionDate());
        boolean reprocess = true;

        if (isTransactionChronologicallyLatest && adjustedTransaction == null
                && loanTransaction.getTransactionDate().isEqual(LocalDate.now()) && currentInstallment != null
                && currentInstallment.getTotalOutstanding(getCurrency()).isEqualTo(loanTransaction.getAmount(getCurrency()))) {
            reprocess = false;
        }

        if (isTransactionChronologicallyLatest && adjustedTransaction == null
                && (!reprocess || !this.repaymentScheduleDetail().isInterestRecalculationEnabled())) {
            loanRepaymentScheduleTransactionProcessor.handleTransaction(loanTransaction, getCurrency(), this.repaymentScheduleInstallments,
                    charges());
            reprocess = false;
            if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                if (currentInstallment == null || currentInstallment.isNotFullyPaidOff()) {
                    reprocess = true;
                } else {
                    final LoanRepaymentScheduleInstallment nextInstallment = fetchRepaymentScheduleInstallment(currentInstallment
                            .getInstallmentNumber() + 1);
                    if (nextInstallment != null && nextInstallment.getTotalPaidInAdvance(getCurrency()).isGreaterThanZero()) {
                        reprocess = true;
                    }
                }
            }
        }
        if (reprocess) {
            if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO, currentUser);
            }
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
            changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(),
                    allNonContraTransactionsPostDisbursement, getCurrency(), this.repaymentScheduleInstallments, charges(),
                    getLastUserTransactionForChargeCalc());
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                mapEntry.getValue().updateLoan(this);
            }
            /***
             * Commented since throwing exception if external id present for one
             * of the transactions. for this need to save the reversed
             * transactions first and then new transactions.
             */
            this.loanTransactions.addAll(changedTransactionDetail.getNewTransactionMappings().values());
        }

        updateLoanSummaryDerivedFields();

        /**
         * FIXME: Vishwas, skipping post loan transaction checks for Loan
         * recoveries
         **/
        if (loanTransaction.isNotRecoveryRepayment()) {
            doPostLoanTransactionChecks(loanTransaction.getTransactionDate(), loanLifecycleStateMachine);
        }

        if (this.loanProduct.isMultiDisburseLoan()) {
            BigDecimal totalDisbursed = getDisbursedAmount();
            if (totalDisbursed.compareTo(this.summary.getTotalPrincipalRepaid()) < 0
                    && this.repaymentScheduleDetail().getPrincipal().minus(totalDisbursed).isGreaterThanZero()) {
                final String errorMessage = "The transaction cannot be done before the loan disbursement: "
                        + getApprovedOnDate().toString();
                throw new InvalidLoanStateTransitionException("transaction", "cannot.be.done.before.disbursement", errorMessage);
            }
        }

        if (changedTransactionDetail != null) {
            this.loanTransactions.removeAll(changedTransactionDetail.getNewTransactionMappings().values());
        }
        return changedTransactionDetail;
    }

    private LoanRepaymentScheduleInstallment fetchLoanRepaymentScheduleInstallment(LocalDate dueDate) {
        LoanRepaymentScheduleInstallment installment = null;
        for (LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : this.repaymentScheduleInstallments) {
            if (dueDate.equals(loanRepaymentScheduleInstallment.getDueDate())) {
                installment = loanRepaymentScheduleInstallment;
                break;
            }
        }
        return installment;
    }

    private List<LoanTransaction> retreiveListOfTransactionsPostDisbursement() {
        final List<LoanTransaction> repaymentsOrWaivers = new ArrayList<>();
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (!transaction.isDisbursement() && transaction.isNotReversed()) {
                repaymentsOrWaivers.add(transaction);
            }
        }
        final LoanTransactionComparator transactionComparator = new LoanTransactionComparator();
        Collections.sort(repaymentsOrWaivers, transactionComparator);
        return repaymentsOrWaivers;
    }

    public List<LoanTransaction> retreiveListOfTransactionsPostDisbursementExcludeAccruals() {
        final List<LoanTransaction> repaymentsOrWaivers = new ArrayList<>();
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (!transaction.isDisbursement() && transaction.isNotReversed() && !transaction.isAccrual()
                    && !transaction.isRepaymentAtDisbursement()) {
                repaymentsOrWaivers.add(transaction);
            }
        }
        final LoanTransactionComparator transactionComparator = new LoanTransactionComparator();
        Collections.sort(repaymentsOrWaivers, transactionComparator);
        return repaymentsOrWaivers;
    }

    private List<LoanTransaction> retreiveListOfTransactionsExcludeAccruals() {
        final List<LoanTransaction> repaymentsOrWaivers = new ArrayList<>();
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isNotReversed() && !transaction.isAccrual()) {
                repaymentsOrWaivers.add(transaction);
            }
        }
        final LoanTransactionComparator transactionComparator = new LoanTransactionComparator();
        Collections.sort(repaymentsOrWaivers, transactionComparator);
        return repaymentsOrWaivers;
    }

    private List<LoanTransaction> retreiveListOfAccrualTransactions() {
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

    private void doPostLoanTransactionChecks(final LocalDate transactionDate, final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        if (isOverPaid()) {
            // FIXME - kw - update account balance to negative amount.
            handleLoanOverpayment(loanLifecycleStateMachine);
        } else if (this.summary.isRepaidInFull(loanCurrency())) {
            handleLoanRepaymentInFull(transactionDate, loanLifecycleStateMachine);
        }
    }

    private void handleLoanRepaymentInFull(final LocalDate transactionDate, final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        boolean isAllChargesPaid = true;
        for (final LoanCharge loanCharge : this.charges) {
            if (loanCharge.isActive() && !(loanCharge.isPaid() || loanCharge.isWaived())) {
                isAllChargesPaid = false;
                break;
            }
        }
        if (isAllChargesPaid) {
            final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.REPAID_IN_FULL,
                    LoanStatus.fromInt(this.loanStatus));
            this.loanStatus = statusEnum.getValue();

            this.closedOnDate = transactionDate.toDate();
            this.actualMaturityDate = transactionDate.toDate();
        } else if (LoanStatus.fromInt(this.loanStatus).isOverpaid()) {
            final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_REPAYMENT_OR_WAIVER,
                    LoanStatus.fromInt(this.loanStatus));
            this.loanStatus = statusEnum.getValue();
        }
    }

    private void handleLoanOverpayment(final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_OVERPAYMENT, LoanStatus.fromInt(this.loanStatus));
        this.loanStatus = statusEnum.getValue();

        this.closedOnDate = null;
        this.actualMaturityDate = null;
    }

    private boolean isChronologicallyLatestRepaymentOrWaiver(final LoanTransaction loanTransaction,
            final List<LoanTransaction> loanTransactions) {

        boolean isChronologicallyLatestRepaymentOrWaiver = true;

        final LocalDate currentTransactionDate = loanTransaction.getTransactionDate();
        for (final LoanTransaction previousTransaction : loanTransactions) {
            if (!previousTransaction.isDisbursement() && previousTransaction.isNotReversed()) {
                if (currentTransactionDate.isBefore(previousTransaction.getTransactionDate())
                        || currentTransactionDate.isEqual(previousTransaction.getTransactionDate())) {
                    isChronologicallyLatestRepaymentOrWaiver = false;
                    break;
                }
            }
        }

        return isChronologicallyLatestRepaymentOrWaiver;
    }

    private boolean isAfterLatRepayment(final LoanTransaction loanTransaction, final List<LoanTransaction> loanTransactions) {

        boolean isAfterLatRepayment = true;

        final LocalDate currentTransactionDate = loanTransaction.getTransactionDate();
        for (final LoanTransaction previousTransaction : loanTransactions) {
            if (previousTransaction.isRepayment() && previousTransaction.isNotReversed()) {
                if (currentTransactionDate.isBefore(previousTransaction.getTransactionDate())) {
                    isAfterLatRepayment = false;
                    break;
                }
            }
        }
        return isAfterLatRepayment;
    }

    private boolean isChronologicallyLatestTransaction(final LoanTransaction loanTransaction, final List<LoanTransaction> loanTransactions) {

        boolean isChronologicallyLatestRepaymentOrWaiver = true;

        final LocalDate currentTransactionDate = loanTransaction.getTransactionDate();
        for (final LoanTransaction previousTransaction : loanTransactions) {
            if (previousTransaction.isNotReversed()) {
                if (currentTransactionDate.isBefore(previousTransaction.getTransactionDate())
                        || currentTransactionDate.isEqual(previousTransaction.getTransactionDate())) {
                    isChronologicallyLatestRepaymentOrWaiver = false;
                    break;
                }
            }
        }

        return isChronologicallyLatestRepaymentOrWaiver;
    }

    public LocalDate possibleNextRepaymentDate() {
        LocalDate earliestUnpaidInstallmentDate = new LocalDate();
        for (final LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
            if (installment.isNotFullyPaidOff()) {
                earliestUnpaidInstallmentDate = installment.getDueDate();
                break;
            }
        }

        LocalDate lastTransactionDate = null;
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isRepayment() && transaction.isNonZero()) {
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

        for (final LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
            if (installment.isNotFullyPaidOff()) {
                loanRepaymentScheduleInstallment = installment;
                break;
            }
        }

        return loanRepaymentScheduleInstallment;
    }

    public LoanTransaction deriveDefaultInterestWaiverTransaction(final LocalDateTime createdDate, final AppUser currentUser) {

        final Money totalInterestOutstanding = getTotalInterestOutstandingOnLoan();
        Money possibleInterestToWaive = totalInterestOutstanding.copy();
        LocalDate transactionDate = new LocalDate();

        if (totalInterestOutstanding.isGreaterThanZero()) {
            // find earliest known instance of overdue interest and default to
            // that
            for (final LoanRepaymentScheduleInstallment scheduledRepayment : this.repaymentScheduleInstallments) {

                final Money outstandingForPeriod = scheduledRepayment.getInterestOutstanding(loanCurrency());
                if (scheduledRepayment.isOverdueOn(new LocalDate()) && scheduledRepayment.isNotFullyPaidOff()
                        && outstandingForPeriod.isGreaterThanZero()) {
                    transactionDate = scheduledRepayment.getDueDate();
                    possibleInterestToWaive = outstandingForPeriod;
                    break;
                }
            }
        }

        return LoanTransaction.waiver(getOffice(), this, possibleInterestToWaive, transactionDate, possibleInterestToWaive,
                possibleInterestToWaive.zero(), createdDate, currentUser);
    }

    public ChangedTransactionDetail adjustExistingTransaction(final LoanTransaction newTransactionDetail,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final LoanTransaction transactionForAdjustment,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds,
            final ScheduleGeneratorDTO scheduleGeneratorDTO, final AppUser currentUser) {

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

        if (transactionForAdjustment.isNotRepayment() && transactionForAdjustment.isNotWaiver()) {
            final String errorMessage = "Only transactions of type repayment or waiver can be adjusted.";
            throw new InvalidLoanTransactionTypeException("transaction", "adjustment.is.only.allowed.to.repayment.or.waiver.transaction",
                    errorMessage);
        }

        transactionForAdjustment.reverse();
        transactionForAdjustment.manuallyAdjustedOrReversed();

        if (isClosedWrittenOff()) {
            // find write off transaction and reverse it
            final LoanTransaction writeOffTransaction = findWriteOffTransaction();
            writeOffTransaction.reverse();
        }

        if (isClosedObligationsMet() || isClosedWrittenOff() || isClosedWithOutsandingAmountMarkedForReschedule()) {
            this.loanStatus = LoanStatus.ACTIVE.getValue();
        }

        if (newTransactionDetail.isRepayment() || newTransactionDetail.isInterestWaiver()) {
            changedTransactionDetail = handleRepaymentOrRecoveryOrWaiverTransaction(newTransactionDetail, loanLifecycleStateMachine,
                    transactionForAdjustment, scheduleGeneratorDTO, currentUser);
        }

        return changedTransactionDetail;
    }

    public ChangedTransactionDetail undoWrittenOff(final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, final ScheduleGeneratorDTO scheduleGeneratorDTO, final AppUser currentUser) {

        validateAccountStatus(LoanEvent.WRITE_OFF_OUTSTANDING_UNDO);
        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());
        final LoanTransaction writeOffTransaction = findWriteOffTransaction();
        writeOffTransaction.reverse();
        this.loanStatus = LoanStatus.ACTIVE.getValue();
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO, currentUser);
        }
        ChangedTransactionDetail changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(
                getDisbursementDate(), allNonContraTransactionsPostDisbursement, getCurrency(), this.repaymentScheduleInstallments,
                charges(), getLastUserTransactionForChargeCalc());
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

        for (final LoanRepaymentScheduleInstallment scheduledRepayment : this.repaymentScheduleInstallments) {

            cumulativeTotalPaidOnInstallments = cumulativeTotalPaidOnInstallments
                    .plus(scheduledRepayment.getPrincipalCompleted(currency).plus(scheduledRepayment.getInterestPaid(currency)))
                    .plus(scheduledRepayment.getFeeChargesPaid(currency)).plus(scheduledRepayment.getPenaltyChargesPaid(currency));

            cumulativeTotalWaivedOnInstallments = cumulativeTotalWaivedOnInstallments.plus(scheduledRepayment.getInterestWaived(currency));
        }

        for (final LoanTransaction loanTransaction : this.loanTransactions) {
            if ((loanTransaction.isRefund() || loanTransaction.isRefundForActiveLoan()) && !loanTransaction.isReversed()) {
                totalPaidInRepayments = totalPaidInRepayments.minus(loanTransaction.getAmount(currency));
            }
        }

        // if total paid in transactions doesnt match repayment schedule then
        // theres an overpayment.
        return totalPaidInRepayments.minus(cumulativeTotalPaidOnInstallments);
    }

    public Money calculateTotalRecoveredPayments() {
        Money totalRecoveredPayments = getTotalRecoveredPayments();
        // in case logic for reversing recovered payment is implemented handle
        // subtraction from totalRecoveredPayments
        return totalRecoveredPayments;
    }

    private MonetaryCurrency loanCurrency() {
        return this.loanRepaymentScheduleDetail.getCurrency();
    }

    public ChangedTransactionDetail closeAsWrittenOff(final JsonCommand command, final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final Map<String, Object> changes, final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds,
            final AppUser currentUser, final ScheduleGeneratorDTO scheduleGeneratorDTO) {

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);
        ChangedTransactionDetail changedTransactionDetail = closeDisbursements(scheduleGeneratorDTO,
                loanRepaymentScheduleTransactionProcessor, currentUser);

        validateAccountStatus(LoanEvent.WRITE_OFF_OUTSTANDING);

        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.WRITE_OFF_OUTSTANDING,
                LoanStatus.fromInt(this.loanStatus));

        LoanTransaction loanTransaction = null;
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
            this.loanStatus = statusEnum.getValue();
            changes.put("status", LoanEnumerations.status(this.loanStatus));

            existingTransactionIds.addAll(findExistingTransactionIds());
            existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

            final LocalDate writtenOffOnLocalDate = command.localDateValueOfParameterNamed("transactionDate");
            final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

            this.closedOnDate = writtenOffOnLocalDate.toDate();
            this.writtenOffOnDate = writtenOffOnLocalDate.toDate();
            this.closedBy = currentUser;
            changes.put("closedOnDate", command.stringValueOfParameterNamed("transactionDate"));
            changes.put("writtenOffOnDate", command.stringValueOfParameterNamed("transactionDate"));

            if (writtenOffOnLocalDate.isBefore(getDisbursementDate())) {
                final String errorMessage = "The date on which a loan is written off cannot be before the loan disbursement date: "
                        + getDisbursementDate().toString();
                throw new InvalidLoanStateTransitionException("writeoff", "cannot.be.before.submittal.date", errorMessage,
                        writtenOffOnLocalDate, getDisbursementDate());
            }

            validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.WRITE_OFF_OUTSTANDING, writtenOffOnLocalDate);

            if (writtenOffOnLocalDate.isAfter(DateUtils.getLocalDateOfTenant())) {
                final String errorMessage = "The date on which a loan is written off cannot be in the future.";
                throw new InvalidLoanStateTransitionException("writeoff", "cannot.be.a.future.date", errorMessage, writtenOffOnLocalDate);
            }

            LocalDateTime createdDate = DateUtils.getLocalDateTimeOfTenant();
            loanTransaction = LoanTransaction.writeoff(this, getOffice(), writtenOffOnLocalDate, txnExternalId, createdDate, currentUser);
            LocalDate lastTransactionDate = getLastUserTransactionDate();
            if (lastTransactionDate.isAfter(writtenOffOnLocalDate)) {
                final String errorMessage = "The date of the writeoff transaction must occur on or before previous transactions.";
                throw new InvalidLoanStateTransitionException("writeoff", "must.occur.on.or.after.other.transaction.dates", errorMessage,
                        writtenOffOnLocalDate);
            }

            this.loanTransactions.add(loanTransaction);

            loanRepaymentScheduleTransactionProcessor.handleWriteOff(loanTransaction, loanCurrency(), this.repaymentScheduleInstallments);

            updateLoanSummaryDerivedFields();
        }
        if (changedTransactionDetail == null) {
            changedTransactionDetail = new ChangedTransactionDetail();
        }
        changedTransactionDetail.getNewTransactionMappings().put(0L, loanTransaction);
        return changedTransactionDetail;
    }

    private ChangedTransactionDetail closeDisbursements(final ScheduleGeneratorDTO scheduleGeneratorDTO,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor, final AppUser currentUser) {
        ChangedTransactionDetail changedTransactionDetail = null;
        if (isDisbursementAllowed() && atleastOnceDisbursed()) {
            this.loanRepaymentScheduleDetail.setPrincipal(getDisbursedAmount());
            removeDisbursementDetail();
            regenerateRepaymentSchedule(scheduleGeneratorDTO, currentUser);
            if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO, currentUser);
            }
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
            changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(),
                    allNonContraTransactionsPostDisbursement, getCurrency(), this.repaymentScheduleInstallments, charges(),
                    getLastUserTransactionForChargeCalc());
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                mapEntry.getValue().updateLoan(this);
                this.loanTransactions.add(mapEntry.getValue());
            }
            updateLoanSummaryDerivedFields();
            LoanTransaction loanTransaction = findlatestTransaction();
            doPostLoanTransactionChecks(loanTransaction.getTransactionDate(), loanLifecycleStateMachine);
        }
        return changedTransactionDetail;
    }

    private LoanTransaction findlatestTransaction() {
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
            final ScheduleGeneratorDTO scheduleGeneratorDTO, final AppUser currentUser) {

        validateAccountStatus(LoanEvent.LOAN_CLOSED);

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        final LocalDate closureDate = command.localDateValueOfParameterNamed("transactionDate");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        this.closedOnDate = closureDate.toDate();
        changes.put("closedOnDate", command.stringValueOfParameterNamed("transactionDate"));

        validateActivityNotBeforeClientOrGroupTransferDate(LoanEvent.REPAID_IN_FULL, closureDate);
        if (closureDate.isBefore(getDisbursementDate())) {
            final String errorMessage = "The date on which a loan is closed cannot be before the loan disbursement date: "
                    + getDisbursementDate().toString();
            throw new InvalidLoanStateTransitionException("close", "cannot.be.before.submittal.date", errorMessage, closureDate,
                    getDisbursementDate());
        }

        if (closureDate.isAfter(DateUtils.getLocalDateOfTenant())) {
            final String errorMessage = "The date on which a loan is closed cannot be in the future.";
            throw new InvalidLoanStateTransitionException("close", "cannot.be.a.future.date", errorMessage, closureDate);
        }
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);
        ChangedTransactionDetail changedTransactionDetail = closeDisbursements(scheduleGeneratorDTO,
                loanRepaymentScheduleTransactionProcessor, currentUser);

        LoanTransaction loanTransaction = null;
        if (isOpen()) {
            final Money totalOutstanding = this.summary.getTotalOutstanding(loanCurrency());
            if (totalOutstanding.isGreaterThanZero() && getInArrearsTolerance().isGreaterThanOrEqualTo(totalOutstanding)) {

                final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.REPAID_IN_FULL,
                        LoanStatus.fromInt(this.loanStatus));
                if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
                    this.loanStatus = statusEnum.getValue();
                    changes.put("status", LoanEnumerations.status(this.loanStatus));
                }
                this.closedOnDate = closureDate.toDate();
                loanTransaction = LoanTransaction.writeoff(this, getOffice(), closureDate, txnExternalId,
                        DateUtils.getLocalDateTimeOfTenant(), currentUser);
                final boolean isLastTransaction = isChronologicallyLatestTransaction(loanTransaction, this.loanTransactions);
                if (!isLastTransaction) {
                    final String errorMessage = "The closing date of the loan must be on or after latest transaction date.";
                    throw new InvalidLoanStateTransitionException("close.loan", "must.occur.on.or.after.latest.transaction.date",
                            errorMessage, closureDate);
                }

                this.loanTransactions.add(loanTransaction);

                loanRepaymentScheduleTransactionProcessor.handleWriteOff(loanTransaction, loanCurrency(),
                        this.repaymentScheduleInstallments);

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
                // has
                // 'overpaid' amount
                final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.REPAID_IN_FULL,
                        LoanStatus.fromInt(this.loanStatus));
                if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
                    this.loanStatus = statusEnum.getValue();
                    changes.put("status", LoanEnumerations.status(this.loanStatus));
                }
                this.closedOnDate = closureDate.toDate();
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
     * Behaviour added to comply with capability of previous mifos product to
     * support easier transition to mifosx platform.
     */
    public void closeAsMarkedForReschedule(final JsonCommand command, final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final Map<String, Object> changes) {

        final LocalDate rescheduledOn = command.localDateValueOfParameterNamed("transactionDate");

        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_RESCHEDULE, LoanStatus.fromInt(this.loanStatus));
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
            this.loanStatus = statusEnum.getValue();
            changes.put("status", LoanEnumerations.status(this.loanStatus));
        }

        this.closedOnDate = rescheduledOn.toDate();
        this.rescheduledOnDate = rescheduledOn.toDate();
        changes.put("closedOnDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("rescheduledOnDate", command.stringValueOfParameterNamed("transactionDate"));

        final LocalDate rescheduledOnLocalDate = new LocalDate(this.rescheduledOnDate);
        if (rescheduledOnLocalDate.isBefore(getDisbursementDate())) {
            final String errorMessage = "The date on which a loan is rescheduled cannot be before the loan disbursement date: "
                    + getDisbursementDate().toString();
            throw new InvalidLoanStateTransitionException("close.reschedule", "cannot.be.before.submittal.date", errorMessage,
                    rescheduledOnLocalDate, getDisbursementDate());
        }

        if (rescheduledOnLocalDate.isAfter(new LocalDate())) {
            final String errorMessage = "The date on which a loan is rescheduled cannot be in the future.";
            throw new InvalidLoanStateTransitionException("close.reschedule", "cannot.be.a.future.date", errorMessage,
                    rescheduledOnLocalDate);
        }
    }

    public boolean isNotSubmittedAndPendingApproval() {
        return !isSubmittedAndPendingApproval();
    }

    public LoanStatus status() {
        return LoanStatus.fromInt(this.loanStatus);
    }

    public boolean isSubmittedAndPendingApproval() {
        return status().isSubmittedAndPendingApproval();
    }

    public boolean isApproved() {
        return status().isApproved();
    }

    private boolean isNotDisbursed() {
        return !isDisbursed();
    }

    public boolean isDisbursed() {
        return hasDisbursementTransaction();
    }

    public boolean isClosed() {
        return status().isClosed() || isCancelled();
    }

    private boolean isClosedObligationsMet() {
        return status().isClosedObligationsMet();
    }

    public boolean isClosedWrittenOff() {
        return status().isClosedWrittenOff();
    }

    private boolean isClosedWithOutsandingAmountMarkedForReschedule() {
        return status().isClosedWithOutsandingAmountMarkedForReschedule();
    }

    private boolean isCancelled() {
        return isRejected() || isWithdrawn();
    }

    private boolean isWithdrawn() {
        return status().isWithdrawnByClient();
    }

    private boolean isRejected() {
        return status().isRejected();
    }

    public boolean isOpen() {
        return status().isActive();
    }

    private boolean isAllTranchesNotDisbursed() {
        return this.loanProduct.isMultiDisburseLoan()
                && (LoanStatus.fromInt(this.loanStatus).isActive() || LoanStatus.fromInt(this.loanStatus).isApproved())
                && isDisbursementAllowed();
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
        return this.submittedOnDate == null ? false : new LocalDate(this.submittedOnDate).isAfter(compareDate);
    }

    public LocalDate getSubmittedOnDate() {
        return (LocalDate) ObjectUtils.defaultIfNull(new LocalDate(this.submittedOnDate), null);
    }

    public LocalDate getApprovedOnDate() {
        LocalDate date = null;
        if (this.approvedOnDate != null) {
            date = new LocalDate(this.approvedOnDate);
        }
        return date;
    }

    public LocalDate getExpectedDisbursedOnLocalDate() {
        LocalDate expectedDisbursementDate = null;
        if (this.expectedDisbursementDate != null) {
            expectedDisbursementDate = new LocalDate(this.expectedDisbursementDate);
        }
        return expectedDisbursementDate;
    }

    public LocalDate getDisbursementDate() {
        LocalDate disbursementDate = getExpectedDisbursedOnLocalDate();
        if (this.actualDisbursementDate != null) {
            disbursementDate = new LocalDate(this.actualDisbursementDate);
        }
        return disbursementDate;
    }

    public LocalDate getWrittenOffDate() {
        LocalDate writtenOffDate = null;
        if (this.writtenOffOnDate != null) {
            writtenOffDate = new LocalDate(this.writtenOffOnDate);
        }
        return writtenOffDate;
    }

    public LocalDate getExpectedDisbursedOnLocalDateForTemplate() {

        LocalDate expectedDisbursementDate = null;
        if (this.expectedDisbursementDate != null) {
            expectedDisbursementDate = new LocalDate(this.expectedDisbursementDate);
        }

        Collection<LoanDisbursementDetails> details = fetchUndisbursedDetail();
        if (!details.isEmpty()) {
            for (LoanDisbursementDetails disbursementDetails : details) {
                expectedDisbursementDate = new LocalDate(disbursementDetails.expectedDisbursementDate());
            }
        }
        return expectedDisbursementDate;
    }

    /*
     * Reason for derving
     */

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
        LocalDate firstRepaymentDate = null;
        if (this.expectedFirstRepaymentOnDate != null) {
            firstRepaymentDate = new LocalDate(this.expectedFirstRepaymentOnDate);
        }
        return firstRepaymentDate;
    }

    private void addRepaymentScheduleInstallment(final LoanRepaymentScheduleInstallment installment) {
        installment.updateLoan(this);
        this.repaymentScheduleInstallments.add(installment);
    }

    private boolean isActualDisbursedOnDateEarlierOrLaterThanExpected(final LocalDate actualDisbursedOnDate) {
        boolean isRegenerationRequired = false;
        if (this.loanProduct.isMultiDisburseLoan()) {
            LoanDisbursementDetails details = fetchLastDisburseDetail();
            if (details != null && !(details.expectedDisbursementDate().equals(details.actualDisbursementDate()))) {
                isRegenerationRequired = true;
            }
        }
        return !new LocalDate(this.expectedDisbursementDate).isEqual(actualDisbursedOnDate) || isRegenerationRequired;
    }

    private boolean isRepaymentScheduleRegenerationRequiredForDisbursement(final LocalDate actualDisbursementDate) {
        return isActualDisbursedOnDateEarlierOrLaterThanExpected(actualDisbursementDate);
    }

    private Money getTotalPaidInRepayments() {
        Money cumulativePaid = Money.zero(loanCurrency());

        for (final LoanTransaction repayment : this.loanTransactions) {
            if (repayment.isRepayment() && !repayment.isReversed()) {
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

        for (final LoanRepaymentScheduleInstallment scheduledRepayment : this.repaymentScheduleInstallments) {
            cumulativeInterest = cumulativeInterest.plus(scheduledRepayment.getInterestOutstanding(loanCurrency()));
        }

        return cumulativeInterest;
    }

    @SuppressWarnings("unused")
    private Money getTotalInterestOverdueOnLoan() {
        Money cumulativeInterestOverdue = Money.zero(this.loanRepaymentScheduleDetail.getPrincipal().getCurrency());

        for (final LoanRepaymentScheduleInstallment scheduledRepayment : this.repaymentScheduleInstallments) {

            final Money interestOutstandingForPeriod = scheduledRepayment.getInterestOutstanding(loanCurrency());
            if (scheduledRepayment.isOverdueOn(new LocalDate())) {
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
        LocalDate interestChargedFrom = null;
        if (this.interestChargedFromDate != null) {
            interestChargedFrom = new LocalDate(this.interestChargedFromDate);
        }
        return interestChargedFrom;
    }

    public Money getPrincpal() {
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

        } else if (DateUtils.getLocalDateOfTenant().isBefore(assignmentDate)) {

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

        final LocalDate today = DateUtils.getLocalDateOfTenant();

        if (latestHistoryRecord.getStartDate().isAfter(unassignDate)) {

            final String errorMessage = "The Loan officer Unassign date(" + unassignDate + ") cannot be before its assignment date ("
                    + latestHistoryRecord.getStartDate() + ").";

            throw new LoanOfficerUnassignmentDateException("cannot.be.before.assignment.date", errorMessage, getId(), getLoanOfficer()
                    .getId(), latestHistoryRecord.getStartDate(), unassignDate);

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

    public LoanSummary getSummary() {
        return this.summary;
    }

    public Set<LoanCollateral> getCollateral() {
        return this.collateral;
    }

    public BigDecimal getProposedPrincipal() {
        return this.proposedPrincipal;
    }

    public Map<String, Object> deriveAccountingBridgeData(final CurrencyData currencyData, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, boolean isAccountTransfer) {

        final Map<String, Object> accountingBridgeData = new LinkedHashMap<>();
        accountingBridgeData.put("loanId", getId());
        accountingBridgeData.put("loanProductId", productId());
        accountingBridgeData.put("officeId", getOfficeId());
        accountingBridgeData.put("currency", currencyData);
        accountingBridgeData.put("calculatedInterest", this.summary.getTotalInterestCharged());
        accountingBridgeData.put("cashBasedAccountingEnabled", isCashBasedAccountingEnabledOnLoanProduct());
        accountingBridgeData.put("upfrontAccrualBasedAccountingEnabled", isUpfrontAccrualAccountingEnabledOnLoanProduct());
        accountingBridgeData.put("periodicAccrualBasedAccountingEnabled", isPeriodicAccrualAccountingEnabledOnLoanProduct());
        accountingBridgeData.put("isAccountTransfer", isAccountTransfer);

        final List<Map<String, Object>> newLoanTransactions = new ArrayList<>();
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isReversed() && existingTransactionIds.contains(transaction.getId())
                    && !existingReversedTransactionIds.contains(transaction.getId())) {
                newLoanTransactions.add(transaction.toMapData(currencyData));
            } else if (!existingTransactionIds.contains(transaction.getId())) {
                newLoanTransactions.add(transaction.toMapData(currencyData));
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
                } else if (transaction.isRepayment() || transaction.isInterestWaiver()) {
                    receivableInterest = receivableInterest.minus(transaction.getInterestPortion(getCurrency()));
                }
            }
            if (receivableInterest.isLessThanZero()) {
                receivableInterest = receivableInterest.zero();
            }
            /*
             * if (transaction.getTransactionDate().isAfter(tillDate) &&
             * transaction.isAccrual()) { final String errorMessage =
             * "The date on which a loan is interest waived cannot be in after accrual transactions."
             * ; throw new InvalidLoanStateTransitionException("waive",
             * "cannot.be.after.accrual.date", errorMessage, tillDate); }
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
        return this.syncDisbursementWithMeeting == null ? false : this.syncDisbursementWithMeeting;
    }

    public Date getClosedOnDate() {
        return this.closedOnDate;
    }

    public void updateLoanRepaymentScheduleDates(final LocalDate meetingStartDate, final String recuringRule,
            final boolean isHolidayEnabled, final List<Holiday> holidays, final WorkingDays workingDays,
            final Boolean reschedulebasedOnMeetingDates, final LocalDate presentMeetingDate, final LocalDate newMeetingDate) {

        // first repayment's from date is same as disbursement date.
        /*
         * meetingStartDate is used as seedDate Capture the seedDate from user
         * and use the seedDate as meetingStart date
         */

        LocalDate tmpFromDate = getDisbursementDate();
        final PeriodFrequencyType repaymentPeriodFrequencyType = this.loanRepaymentScheduleDetail.getRepaymentPeriodFrequencyType();
        final Integer loanRepaymentInterval = this.loanRepaymentScheduleDetail.getRepayEvery();
        final String frequency = CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(repaymentPeriodFrequencyType);

        LocalDate newRepaymentDate = null;
        Boolean isFirstTime = true;

        for (final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : this.repaymentScheduleInstallments) {

            LocalDate oldDueDate = loanRepaymentScheduleInstallment.getDueDate();

            if (oldDueDate.isEqual(presentMeetingDate) || oldDueDate.isAfter(presentMeetingDate)) {

                if (isFirstTime) {

                    isFirstTime = false;
                    newRepaymentDate = newMeetingDate;

                } else {
                    // tmpFromDate.plusDays(1) is done to make sure
                    // getNewRepaymentMeetingDate method returns next meeting
                    // date and not the same as tmpFromDate
                    newRepaymentDate = CalendarUtils.getNewRepaymentMeetingDate(recuringRule, tmpFromDate, tmpFromDate.plusDays(1),
                            loanRepaymentInterval, frequency, workingDays);
                }

                if (isHolidayEnabled) {
                    newRepaymentDate = HolidayUtil.getRepaymentRescheduleDateToIfHoliday(newRepaymentDate, holidays);
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
    }

    public void updateLoanRepaymentScheduleDates(final LocalDate meetingStartDate, final String recuringRule,
            final boolean isHolidayEnabled, final List<Holiday> holidays, final WorkingDays workingDays) {

        // first repayment's from date is same as disbursement date.
        LocalDate tmpFromDate = getDisbursementDate();
        final PeriodFrequencyType repaymentPeriodFrequencyType = this.loanRepaymentScheduleDetail.getRepaymentPeriodFrequencyType();
        final Integer loanRepaymentInterval = this.loanRepaymentScheduleDetail.getRepayEvery();
        final String frequency = CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(repaymentPeriodFrequencyType);

        LocalDate newRepaymentDate = null;
        LocalDate seedDate = meetingStartDate;

        for (final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : this.repaymentScheduleInstallments) {

            LocalDate oldDueDate = loanRepaymentScheduleInstallment.getDueDate();

            // FIXME: AA this won't update repayment dates before current date.

            if (oldDueDate.isAfter(seedDate) && oldDueDate.isAfter(DateUtils.getLocalDateOfTenant())) {

                newRepaymentDate = CalendarUtils.getNewRepaymentMeetingDate(recuringRule, seedDate, oldDueDate, loanRepaymentInterval,
                        frequency, workingDays);

                final LocalDate maxDateLimitForNewRepayment = getMaxDateLimitForNewRepayment(repaymentPeriodFrequencyType,
                        loanRepaymentInterval, tmpFromDate);

                if (newRepaymentDate.isAfter(maxDateLimitForNewRepayment)) {
                    newRepaymentDate = CalendarUtils.getNextRepaymentMeetingDate(recuringRule, seedDate, tmpFromDate,
                            loanRepaymentInterval, frequency, workingDays);
                }

                if (isHolidayEnabled) {
                    newRepaymentDate = HolidayUtil.getRepaymentRescheduleDateToIfHoliday(newRepaymentDate, holidays);
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
        }
        return dueRepaymentPeriodDate.minusDays(1);// get 2n-1 range date from
                                                   // startDate
    }

    public void applyHolidayToRepaymentScheduleDates(final Holiday holiday) {
        // first repayment's from date is same as disbursement date.
        LocalDate tmpFromDate = getDisbursementDate();
        // Loop through all loanRepayments
        for (final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : this.repaymentScheduleInstallments) {
            final LocalDate oldDueDate = loanRepaymentScheduleInstallment.getDueDate();

            // update from date if it's not same as previous installament's due
            // date.
            if (!loanRepaymentScheduleInstallment.getFromDate().isEqual(tmpFromDate)) {
                loanRepaymentScheduleInstallment.updateFromDate(tmpFromDate);
            }
            if (oldDueDate.isAfter(holiday.getToDateLocalDate())) {
                break;
            }

            if (oldDueDate.equals(holiday.getFromDateLocalDate()) || oldDueDate.equals(holiday.getToDateLocalDate())
                    || oldDueDate.isAfter(holiday.getFromDateLocalDate()) && oldDueDate.isBefore(holiday.getToDateLocalDate())) {
                // FIXME: AA do we need to apply non-working days.
                // Assuming holiday's repayment reschedule to date cannot be
                // created on a non-working day.
                final LocalDate newRepaymentDate = holiday.getRepaymentsRescheduledToLocalDate();
                loanRepaymentScheduleInstallment.updateDueDate(newRepaymentDate);
            }
            tmpFromDate = loanRepaymentScheduleInstallment.getDueDate();
        }
    }

    private void validateDisbursementDateIsOnNonWorkingDay(final WorkingDays workingDays, final boolean allowTransactionsOnNonWorkingDay) {
        if (!allowTransactionsOnNonWorkingDay) {
            if (!WorkingDaysUtil.isWorkingDay(workingDays, getDisbursementDate())) {
                final String errorMessage = "Expected disbursement date cannot be on a non working day";
                throw new LoanApplicationDateException("disbursement.date.on.non.working.day", errorMessage,
                        getExpectedDisbursedOnLocalDate());
            }
        }
    }

    private void validateDisbursementDateIsOnHoliday(final boolean allowTransactionsOnHoliday, final List<Holiday> holidays) {
        if (!allowTransactionsOnHoliday) {
            if (HolidayUtil.isHoliday(getDisbursementDate(), holidays)) {
                final String errorMessage = "Expected disbursement date cannot be on a holiday";
                throw new LoanApplicationDateException("disbursement.date.on.holiday", errorMessage, getExpectedDisbursedOnLocalDate());
            }
        }
    }

    private void validateRepaymentDateIsOnNonWorkingDay(final LocalDate repaymentDate, final WorkingDays workingDays,
            final boolean allowTransactionsOnNonWorkingDay) {
        if (!allowTransactionsOnNonWorkingDay) {
            if (!WorkingDaysUtil.isWorkingDay(workingDays, repaymentDate)) {
                final String errorMessage = "Repayment date cannot be on a non working day";
                throw new LoanApplicationDateException("repayment.date.on.non.working.day", errorMessage, repaymentDate);
            }
        }
    }

    private void validateRepaymentDateIsOnHoliday(final LocalDate repaymentDate, final boolean allowTransactionsOnHoliday,
            final List<Holiday> holidays) {
        if (!allowTransactionsOnHoliday) {
            if (HolidayUtil.isHoliday(repaymentDate, holidays)) {
                final String errorMessage = "Repayment date cannot be on a holiday";
                throw new LoanApplicationDateException("repayment.date.on.holiday", errorMessage, repaymentDate);
            }
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
        if (this.loanProductCounter == null) { return 0; }
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
        this.loanRepaymentScheduleDetail.updatenterestPeriodFrequencyType(this.loanProduct.getInterestPeriodFrequencyType());
    }

    public Integer getTermFrequency() {
        return this.termFrequency;
    }

    public Integer getTermPeriodFrequencyType() {
        return this.termPeriodFrequencyType;
    }

    public List<LoanTransaction> getLoanTransactions() {
        return this.loanTransactions;
    }

    public void setLoanStatus(final Integer loanStatus) {
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
                    default:
                    break;
                }
                throw new InvalidLoanStateTransitionException(action, postfix, errorMessage, clientOfficeJoiningDate);
            }
        }
    }

    private void validateActivityNotBeforeLastTransactionDate(final LoanEvent event, final LocalDate activityDate) {
        if (!(this.repaymentScheduleDetail().isInterestRecalculationEnabled() || this.loanProduct().isHoldGuaranteeFundsEnabled())) { return; }
        LocalDate lastTransactionDate = getLastUserTransactionDate();
        final LocalDate clientOfficeJoiningDate = this.client.getOfficeJoiningLocalDate();
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
            throw new InvalidLoanStateTransitionException(action, postfix, errorMessage, clientOfficeJoiningDate);
        }
    }

    private LocalDate getLastUserTransactionDate() {
        LocalDate currentTransactionDate = getDisbursementDate();
        for (final LoanTransaction previousTransaction : this.loanTransactions) {
            if (!(previousTransaction.isReversed() || previousTransaction.isAccrual())) {
                if (currentTransactionDate.isBefore(previousTransaction.getTransactionDate())) {
                    currentTransactionDate = previousTransaction.getTransactionDate();
                }
            }
        }
        return currentTransactionDate;
    }

    public LocalDate getLastUserTransactionForChargeCalc() {
        LocalDate lastTransaction = getDisbursementDate();
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            lastTransaction = getLastUserTransactionDate();
        }
        return lastTransaction;
    }

    public Set<LoanCharge> charges() {
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

    public Set<LoanInstallmentCharge> generateInstallmentLoanCharges(final LoanCharge loanCharge) {
        final Set<LoanInstallmentCharge> loanChargePerInstallments = new HashSet<>();
        if (loanCharge.isInstalmentFee()) {
            for (final LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
                BigDecimal amount = BigDecimal.ZERO;
                if (loanCharge.getChargeCalculation().isFlat()) {
                    amount = loanCharge.amount().divide(BigDecimal.valueOf(repaymentScheduleDetail().getNumberOfRepayments()));
                } else {
                    amount = calculateInstallmentChargeAmount(loanCharge.getChargeCalculation(), loanCharge.getPercentage(), installment)
                            .getAmount();
                }
                final LoanInstallmentCharge loanInstallmentCharge = new LoanInstallmentCharge(amount, loanCharge, installment);
                loanChargePerInstallments.add(loanInstallmentCharge);
            }
        }
        return loanChargePerInstallments;
    }

    public void validateAccountStatus(final LoanEvent event) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        switch (event) {
            case LOAN_CREATED:
            break;
            case LOAN_APPROVED:
                if (!isSubmittedAndPendingApproval()) {
                    final String defaultUserMessage = "Loan Account Approval is not allowed. Loan Account is not in submitted and pending approval state.";
                    final ApiParameterError error = ApiParameterError.generalError(
                            "error.msg.loan.approve.account.is.not.submitted.and.pending.state", defaultUserMessage);
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
                    final ApiParameterError error = ApiParameterError.generalError(
                            "error.msg.loan.disbursal.account.is.not.approve.not.disbursed.state", defaultUserMessage);
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
            break;
            case LOAN_REPAYMENT_OR_WAIVER:
                if (!isOpen()) {
                    final String defaultUserMessage = "Loan Repayment or Waiver is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError(
                            "error.msg.loan.repayment.or.waiver.account.is.not.active", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_REJECTED:
                if (!isSubmittedAndPendingApproval()) {
                    final String defaultUserMessage = "Loan application cannot be rejected. Loan Account is not in Submitted and Pending approval state.";
                    final ApiParameterError error = ApiParameterError.generalError(
                            "error.msg.loan.reject.account.is.not.submitted.pending.approval.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case LOAN_WITHDRAWN:
                if (!isSubmittedAndPendingApproval()) {
                    final String defaultUserMessage = "Loan application cannot be withdrawn. Loan Account is not in Submitted and Pending approval state.";
                    final ApiParameterError error = ApiParameterError.generalError(
                            "error.msg.loan.withdrawn.account.is.not.submitted.pending.approval.state", defaultUserMessage);
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
                    final ApiParameterError error = ApiParameterError.generalError(
                            "error.msg.loan.undo.writtenoff.account.is.not.written.off", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            break;
            case REPAID_IN_FULL:
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
                    final ApiParameterError error = ApiParameterError.generalError(
                            "error.msg.loan.edit.disbursement.account.is.not.active", defaultUserMessage);
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
            default:
            break;
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

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

    public Set<LoanDisbursementDetails> getDisbursementDetails() {
        return this.disbursementDetails;
    }

    public ChangedTransactionDetail updateDisbursementDateAndAmountForTranche(final LoanDisbursementDetails disbursementDetails,
            final JsonCommand command, final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds,
            final Map<String, Object> actualChanges, final ScheduleGeneratorDTO scheduleGeneratorDTO, final AppUser currentUser) {
        final Locale locale = command.extractLocale();
        validateAccountStatus(LoanEvent.LOAN_EDIT_MULTI_DISBURSE_DATE);
        final BigDecimal principal = command.bigDecimalValueOfParameterNamed(LoanApiConstants.updatedDisbursementPrincipalParameterName,
                locale);
        final LocalDate expectedDisbursementDate = command
                .localDateValueOfParameterNamed(LoanApiConstants.updatedDisbursementDateParameterName);
        disbursementDetails.updateExpectedDisbursementDateAndAmount(expectedDisbursementDate.toDate(), principal);
        actualChanges.put(LoanApiConstants.disbursementDateParameterName,
                command.stringValueOfParameterNamed(LoanApiConstants.disbursementDateParameterName));
        actualChanges.put(LoanApiConstants.disbursementIdParameterName,
                command.stringValueOfParameterNamed(LoanApiConstants.disbursementIdParameterName));
        actualChanges.put(LoanApiConstants.disbursementPrincipalParameterName,
                command.bigDecimalValueOfParameterNamed(LoanApiConstants.disbursementPrincipalParameterName, locale));
        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        Collection<LoanDisbursementDetails> loanDisburseDetails = this.getDisbursementDetails();
        BigDecimal setPrincipalAmount = BigDecimal.ZERO;
        for (LoanDisbursementDetails details : loanDisburseDetails) {
            setPrincipalAmount = setPrincipalAmount.add(details.principal());
        }

        this.loanRepaymentScheduleDetail.setPrincipal(setPrincipalAmount);
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            regenerateRepaymentScheduleWithInterestRecalculation(scheduleGeneratorDTO, currentUser);
        } else {
            regenerateRepaymentSchedule(scheduleGeneratorDTO, currentUser);
        }

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
        ChangedTransactionDetail changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(
                getDisbursementDate(), allNonContraTransactionsPostDisbursement, getCurrency(), this.repaymentScheduleInstallments,
                charges(), getLastUserTransactionForChargeCalc());
        for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
            mapEntry.getValue().updateLoan(this);
            this.loanTransactions.add(mapEntry.getValue());
        }

        return changedTransactionDetail;
    }

    public BigDecimal retriveLastEmiAmount() {
        BigDecimal emiAmount = this.fixedEmiAmount;
        Date startDate = this.getDisbursementDate().toDate();
        for (LoanTermVariations loanTermVariations : this.loanTermVariations) {
            if (loanTermVariations.getTermType().isEMIAmountVariation() && !startDate.after(loanTermVariations.getTermApplicableFrom())) {
                startDate = loanTermVariations.getTermApplicableFrom();
                emiAmount = loanTermVariations.getTermValue();
            }
        }
        return emiAmount;
    }

    public LoanRepaymentScheduleInstallment fetchRepaymentScheduleInstallment(final Integer installmentNumber) {
        LoanRepaymentScheduleInstallment installment = null;
        if (installmentNumber == null) { return installment; }
        for (final LoanRepaymentScheduleInstallment scheduleInstallment : this.repaymentScheduleInstallments) {
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

    public BigDecimal getTotalOverpaid() {
        return this.totalOverpaid;
    }

    public void updateIsInterestRecalculationEnabled() {
        this.loanRepaymentScheduleDetail.updateIsInterestRecalculationEnabled(isInterestRecalculationEnabledForProduct());
    }

    public LoanInterestRecalculationDetails loanInterestRecalculationDetails() {
        return this.loanInterestRecalculationDetails;
    }

    public Long loanInterestRecalculationDetailId() {
        if (loanInterestRecalculationDetails() != null) { return this.loanInterestRecalculationDetails.getId(); }
        return null;
    }

    public LocalDate getExpectedMaturityDate() {
        LocalDate expectedMaturityDate = null;
        if (this.expectedMaturityDate != null) {
            expectedMaturityDate = new LocalDate(this.expectedMaturityDate);
        }
        return expectedMaturityDate;
    }

    public LocalDate getMaturityDate() {
        LocalDate maturityDate = getExpectedMaturityDate();
        if (this.actualMaturityDate != null) {
            maturityDate = new LocalDate(this.actualMaturityDate);
        }
        return maturityDate;
    }

    public ChangedTransactionDetail recalculateScheduleFromLastTransaction(final ScheduleGeneratorDTO generatorDTO,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds, final AppUser currentUser) {
        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());
        /*
         * LocalDate recalculateFrom = null; List<LoanTransaction>
         * loanTransactions =
         * this.retreiveListOfTransactionsPostDisbursementExcludeAccruals(); for
         * (LoanTransaction loanTransaction : loanTransactions) { if
         * (recalculateFrom == null ||
         * loanTransaction.getTransactionDate().isAfter(recalculateFrom)) {
         * recalculateFrom = loanTransaction.getTransactionDate(); } }
         * generatorDTO.setRecalculateFrom(recalculateFrom);
         */
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            regenerateRepaymentScheduleWithInterestRecalculation(generatorDTO, currentUser);
        }
        return processTransactions();

    }

    public ChangedTransactionDetail handleRegenerateRepaymentScheduleWithInterestRecalculation(final ScheduleGeneratorDTO generatorDTO,
            final AppUser currentUser) {
        regenerateRepaymentScheduleWithInterestRecalculation(generatorDTO, currentUser);
        return processTransactions();

    }

    private ChangedTransactionDetail processTransactions() {
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
        ChangedTransactionDetail changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(
                getDisbursementDate(), allNonContraTransactionsPostDisbursement, getCurrency(), this.repaymentScheduleInstallments,
                charges(), getLastUserTransactionForChargeCalc());
        for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
            mapEntry.getValue().updateLoan(this);
        }
        /***
         * Commented since throwing exception if external id present for one of
         * the transactions. for this need to save the reversed transactions
         * first and then new transactions.
         */
        this.loanTransactions.addAll(changedTransactionDetail.getNewTransactionMappings().values());

        updateLoanSummaryDerivedFields();

        this.loanTransactions.removeAll(changedTransactionDetail.getNewTransactionMappings().values());

        return changedTransactionDetail;
    }

    public void regenerateRepaymentScheduleWithInterestRecalculation(final ScheduleGeneratorDTO generatorDTO, final AppUser currentUser) {

        LocalDate lastTransactionDate = getLastUserTransactionDate();
        generatorDTO.setLastTransactionDate(lastTransactionDate);
        final LoanScheduleModel loanSchedule = getRecalculatedSchedule(generatorDTO);
        if (loanSchedule == null) { return; }
        updateLoanSchedule(loanSchedule, currentUser);
        LocalDate lastRepaymentDate = this.getLastRepaymentPeriodDueDate();
        Set<LoanCharge> charges = this.charges();
        for (LoanCharge loanCharge : charges) {
            if (!loanCharge.isDueAtDisbursement()) {
                updateOverdueScheduleInstallment(loanCharge);
                if (loanCharge.getDueLocalDate() == null || (!lastRepaymentDate.isBefore(loanCharge.getDueLocalDate()))) {
                    if (!loanCharge.isWaived()
                            && (loanCharge.getDueLocalDate() == null || !lastTransactionDate.isAfter(loanCharge.getDueLocalDate()))) {
                        recalculateLoanCharge(loanCharge, generatorDTO.getPenaltyWaitPeriod());
                        loanCharge.updateWaivedAmount(getCurrency());
                    }
                } else {
                    loanCharge.setActive(false);
                }
            }
        }

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);
        final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
        final List<LoanTransaction> copyTransactions = new ArrayList<>();
        for (LoanTransaction loanTransaction : allNonContraTransactionsPostDisbursement) {
            copyTransactions.add(LoanTransaction.copyTransactionProperties(loanTransaction));
        }
        loanRepaymentScheduleTransactionProcessor.populateDerivedFeildsWithoutReprocess(getDisbursementDate(), copyTransactions,
                getCurrency(), this.repaymentScheduleInstallments, charges(), getLastUserTransactionForChargeCalc());

        updateLoanSummaryDerivedFields();
    }

    private LoanScheduleModel getRecalculatedSchedule(final ScheduleGeneratorDTO generatorDTO) {

        if (!this.repaymentScheduleDetail().isInterestRecalculationEnabled() || isNpa || !isOpen()) { return null; }
        final InterestMethod interestMethod = this.loanRepaymentScheduleDetail.getInterestMethod();
        final LoanScheduleGenerator loanScheduleGenerator = generatorDTO.getLoanScheduleFactory().create(interestMethod);

        final RoundingMode roundingMode = RoundingMode.HALF_EVEN;
        final MathContext mc = new MathContext(8, roundingMode);

        final LoanApplicationTerms loanApplicationTerms = constructLoanApplicationTerms(generatorDTO.getApplicationCurrency(),
                generatorDTO.getCalculatedRepaymentsStartingFromDate(), generatorDTO.getCalendarInstanceForInterestRecalculation());

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);

        final LoanScheduleModel loanSchedule = loanScheduleGenerator.rescheduleNextInstallments(mc, loanApplicationTerms, charges(),
                generatorDTO.getHolidayDetailDTO(), retreiveListOfTransactionsPostDisbursementExcludeAccruals(),
                loanRepaymentScheduleTransactionProcessor, generatorDTO.getLastTransactionDate());
        return loanSchedule;
    }

    public LoanApplicationTerms constructLoanApplicationTerms(final ApplicationCurrency applicationCurrency,
            final LocalDate calculatedRepaymentsStartingFromDate, final CalendarInstance calendarInstanceForInterestRecalculation) {
        final Integer loanTermFrequency = this.termFrequency;
        final PeriodFrequencyType loanTermPeriodFrequencyType = PeriodFrequencyType.fromInt(this.termPeriodFrequencyType);
        final List<DisbursementData> disbursementData = new ArrayList<>();
        for (LoanDisbursementDetails disbursementDetails : this.disbursementDetails) {
            disbursementData.add(disbursementDetails.toData());
        }
        final List<LoanTermVariationsData> loanVariationTermsData = new ArrayList<>();
        boolean isDefaultEmiAmountReq = true;
        for (LoanTermVariations variationTerms : this.loanTermVariations) {
            if (variationTerms.getTermType().isEMIAmountVariation()) {
                if (variationTerms.getTermApplicableFrom().equals(this.getDisbursementDate().toDate())) {
                    isDefaultEmiAmountReq = false;
                }
                loanVariationTermsData.add(variationTerms.toData());
            }
        }
        if (isDefaultEmiAmountReq) {
            LoanTermVariationsData data = new LoanTermVariationsData(null,
                    LoanEnumerations.loanvariationType(LoanTermVariationType.EMI_AMOUNT), this.getDisbursementDate(), this.fixedEmiAmount);
            loanVariationTermsData.add(data);
        }

        RecalculationFrequencyType recalculationFrequencyType = this.loanInterestRecalculationDetails().getRestFrequencyType();

        final LoanApplicationTerms loanApplicationTerms = LoanApplicationTerms.assembleFrom(applicationCurrency, loanTermFrequency,
                loanTermPeriodFrequencyType, getDisbursementDate(), getExpectedFirstRepaymentOnDate(),
                calculatedRepaymentsStartingFromDate, getInArrearsTolerance(), this.loanRepaymentScheduleDetail,
                this.loanProduct.isMultiDisburseLoan(), this.fixedEmiAmount, disbursementData, this.maxOutstandingLoanBalance,
                loanVariationTermsData, getInterestChargedFromDate(), this.loanInterestRecalculationDetails,
                calendarInstanceForInterestRecalculation, recalculationFrequencyType,
                this.loanProduct.getPrincipalThresholdForLastInstallment(), this.loanProduct.getInstallmentAmountInMultiplesOf(),
                this.loanProduct.preCloseInterestCalculationStrategy());
        return loanApplicationTerms;
    }

    public LoanRepaymentScheduleInstallment fetchPrepaymentDetail(final LoanScheduleGeneratorFactory loanScheduleFactory,
            CalendarInstance restCalendarInstance, HolidayDetailDTO holidayDetailDTO, LocalDate onDate) {
        LoanRepaymentScheduleInstallment installment = null;

        if (this.loanRepaymentScheduleDetail.isInterestRecalculationEnabled()) {
            final InterestMethod interestMethod = this.loanRepaymentScheduleDetail.getInterestMethod();
            final LoanScheduleGenerator loanScheduleGenerator = loanScheduleFactory.create(interestMethod);

            final RoundingMode roundingMode = RoundingMode.HALF_EVEN;
            final MathContext mc = new MathContext(8, roundingMode);

            final Integer loanTermFrequency = this.termFrequency;
            final PeriodFrequencyType loanTermPeriodFrequencyType = PeriodFrequencyType.fromInt(this.termPeriodFrequencyType);
            final NthDayType nthDayType = NthDayType.fromInt(this.repaymentFrequencyNthDayType);
            final DayOfWeekType dayOfWeekType = DayOfWeekType.fromInt(this.repaymentFrequencyDayOfWeekType);

            final List<DisbursementData> disbursementData = new ArrayList<>();
            for (LoanDisbursementDetails disbursementDetails : this.disbursementDetails) {
                disbursementData.add(disbursementDetails.toData());
            }
            final List<LoanTermVariationsData> loanVariationTermsData = new ArrayList<>();
            boolean isDefaultEmiAmountReq = true;
            for (LoanTermVariations variationTerms : this.loanTermVariations) {
                if (variationTerms.getTermType().isEMIAmountVariation()) {
                    if (variationTerms.getTermApplicableFrom().equals(this.getDisbursementDate().toDate())) {
                        isDefaultEmiAmountReq = false;
                    }
                    loanVariationTermsData.add(variationTerms.toData());
                }
            }
            if (isDefaultEmiAmountReq) {
                LoanTermVariationsData data = new LoanTermVariationsData(null,
                        LoanEnumerations.loanvariationType(LoanTermVariationType.EMI_AMOUNT), this.getDisbursementDate(),
                        this.fixedEmiAmount);
                loanVariationTermsData.add(data);
            }

            RecalculationFrequencyType recalculationFrequencyType = null;
            InterestRecalculationCompoundingMethod compoundingMethod = null;
            if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                recalculationFrequencyType = this.loanProduct.getProductInterestRecalculationDetails().getRestFrequencyType();
                compoundingMethod = InterestRecalculationCompoundingMethod.fromInt(this.loanProduct
                        .getProductInterestRecalculationDetails().getInterestRecalculationCompoundingMethod());
            }

            final LoanApplicationTerms loanApplicationTerms = LoanApplicationTerms.assembleFrom(null, loanTermFrequency,
                    loanTermPeriodFrequencyType, nthDayType, dayOfWeekType, getDisbursementDate(), getExpectedFirstRepaymentOnDate(), null,
                    getInArrearsTolerance(), this.loanRepaymentScheduleDetail, this.loanProduct.isMultiDisburseLoan(), this.fixedEmiAmount,
                    disbursementData, this.maxOutstandingLoanBalance, loanVariationTermsData, getInterestChargedFromDate(),
                    this.loanProduct.getPrincipalThresholdForLastInstallment(), this.loanProduct.getInstallmentAmountInMultiplesOf(),
                    recalculationFrequencyType, restCalendarInstance, compoundingMethod,
                    this.loanProduct.preCloseInterestCalculationStrategy());

            installment = loanScheduleGenerator.calculatePrepaymentAmount(this.repaymentScheduleInstallments, getCurrency(),
                    onDate, loanApplicationTerms, mc, charges(), holidayDetailDTO);
        } else {
            installment = this.getTotalOutstandingOnLoan();
        }
        return installment;
    }

    private LoanRepaymentScheduleInstallment getTotalOutstandingOnLoan() {
        Money feeCharges = Money.zero(loanCurrency());
        Money penaltyCharges = Money.zero(loanCurrency());
        Money totalPrincipal = Money.zero(loanCurrency());
        Money totalInterest = Money.zero(loanCurrency());
        for (final LoanRepaymentScheduleInstallment scheduledRepayment : this.repaymentScheduleInstallments) {
            totalPrincipal = totalPrincipal.plus(scheduledRepayment.getPrincipalOutstanding(loanCurrency()));
            totalInterest = totalInterest.plus(scheduledRepayment.getInterestOutstanding(loanCurrency()));
            feeCharges = feeCharges.plus(scheduledRepayment.getFeeChargesOutstanding(loanCurrency()));
            penaltyCharges = penaltyCharges.plus(scheduledRepayment.getPenaltyChargesOutstanding(loanCurrency()));
        }
        return new LoanRepaymentScheduleInstallment(null, 0, LocalDate.now(), LocalDate.now(), totalPrincipal.getAmount(),
                totalInterest.getAmount(), feeCharges.getAmount(), penaltyCharges.getAmount(), false);
    }

    public List<LoanRepaymentScheduleInstallment> fetchRepaymentScheduleInstallments() {
        return this.repaymentScheduleInstallments;
    }

    public LocalDate getAccruedTill() {
        LocalDate accruedTill = null;
        if (this.accruedTill != null) {
            accruedTill = new LocalDate(this.accruedTill);
        }
        return accruedTill;
    }

    private void updateLoanOutstandingBalaces() {
        Money outstanding = Money.zero(getCurrency());
        List<LoanTransaction> loanTransactions = retreiveListOfTransactionsExcludeAccruals();
        for (LoanTransaction loanTransaction : loanTransactions) {
            if (loanTransaction.isDisbursement()) {
                outstanding = outstanding.plus(loanTransaction.getAmount(getCurrency()));
                loanTransaction.updateOutstandingLoanBalance(outstanding.getAmount());
            } else {
                outstanding = outstanding.minus(loanTransaction.getPrincipalPortion(getCurrency()));
                loanTransaction.updateOutstandingLoanBalance(outstanding.getAmount());
            }
        }
    }

    public LoanTransactionProcessingStrategy transactionProcessingStrategy() {
        return this.transactionProcessingStrategy;
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
            for (LoanRepaymentScheduleInstallment repaymentScheduleInstallment : this.repaymentScheduleInstallments) {

                if (repaymentScheduleInstallment.getDueDate().isEqual(dueDate)) {
                    installment = repaymentScheduleInstallment;

                    break;
                }
            }
        }

        return installment;
    }

    /**
     * @param loan
     *            the Loan object
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
                expectedDisbursementDate = new LocalDate(loanDisbursementDetails.expectedDisbursementDate());
            }

            if (loanDisbursementDetails.actualDisbursementDate() != null) {
                actualDisbursementDate = new LocalDate(loanDisbursementDetails.actualDisbursementDate());
            }

            disbursementData.add(new DisbursementData(loanDisbursementDetails.getId(), expectedDisbursementDate, actualDisbursementDate,
                    loanDisbursementDetails.principal()));
        }

        return disbursementData;
    }

    /**
     * @param restCalendarInstance
     *            TODO
     * @param loan
     *            the Loan object
     * @return application terms of the Loan object
     **/
    @SuppressWarnings({ "unused" })
    public LoanApplicationTerms getLoanApplicationTerms(final ApplicationCurrency applicationCurrency,
            final CalendarInstance restCalendarInstance) {
        LoanProduct loanProduct = loanProduct();
        // LoanProductRelatedDetail loanProductRelatedDetail =
        // getLoanRepaymentScheduleDetail();
        final MonetaryCurrency currency = this.loanRepaymentScheduleDetail.getCurrency();

        final Integer loanTermFrequency = getTermFrequency();
        final PeriodFrequencyType loanTermPeriodFrequencyType = this.loanRepaymentScheduleDetail.getInterestPeriodFrequencyType();
        final NthDayType nthDayType = NthDayType.fromInt(this.repaymentFrequencyNthDayType);
        final DayOfWeekType dayOfWeekType = DayOfWeekType.fromInt(this.repaymentFrequencyDayOfWeekType);

        final Integer numberOfRepayments = this.loanRepaymentScheduleDetail.getNumberOfRepayments();
        final Integer repaymentEvery = this.loanRepaymentScheduleDetail.getRepayEvery();
        final PeriodFrequencyType repaymentPeriodFrequencyType = this.loanRepaymentScheduleDetail.getRepaymentPeriodFrequencyType();

        final AmortizationMethod amortizationMethod = this.loanRepaymentScheduleDetail.getAmortizationMethod();

        final InterestMethod interestMethod = this.loanRepaymentScheduleDetail.getInterestMethod();
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = this.loanRepaymentScheduleDetail
                .getInterestCalculationPeriodMethod();

        final BigDecimal interestRatePerPeriod = this.loanRepaymentScheduleDetail.getNominalInterestRatePerPeriod();
        final PeriodFrequencyType interestRatePeriodFrequencyType = this.loanRepaymentScheduleDetail.getInterestPeriodFrequencyType();

        final BigDecimal annualNominalInterestRate = this.loanRepaymentScheduleDetail.getAnnualNominalInterestRate();
        final Money principalMoney = this.loanRepaymentScheduleDetail.getPrincipal();

        final LocalDate expectedDisbursementDate = getExpectedDisbursedOnLocalDate();
        final LocalDate repaymentsStartingFromDate = getExpectedFirstRepaymentOnDate();
        LocalDate calculatedRepaymentsStartingFromDate = repaymentsStartingFromDate;

        // TODO get calender linked to loan if any exist. As of 17-07-2014,
        // could not find a link in the database.
        // skip for now and set the Calender object to null
        Calendar calendar = null;

        final Integer graceOnPrincipalPayment = this.loanRepaymentScheduleDetail.graceOnPrincipalPayment();
        final Integer graceOnInterestPayment = this.loanRepaymentScheduleDetail.graceOnInterestPayment();
        final Integer graceOnInterestCharged = this.loanRepaymentScheduleDetail.graceOnInterestCharged();
        final LocalDate interestChargedFromDate = getInterestChargedFromDate();
        final Integer graceOnArrearsAgeing = this.loanRepaymentScheduleDetail.getGraceOnDueDate();

        final Money inArrearsToleranceMoney = this.loanRepaymentScheduleDetail.getInArrearsTolerance();
        final BigDecimal emiAmount = getFixedEmiAmount();
        final BigDecimal maxOutstandingBalance = getMaxOutstandingLoanBalance();

        final List<DisbursementData> disbursementData = getDisbursmentData();

        final List<LoanTermVariationsData> loanVariationTermsData = new ArrayList<LoanTermVariationsData>();
        LoanTermVariationsData data = new LoanTermVariationsData(null,
                LoanEnumerations.loanvariationType(LoanTermVariationType.EMI_AMOUNT), expectedDisbursementDate, emiAmount);
        loanVariationTermsData.add(data);

        RecalculationFrequencyType recalculationFrequencyType = null;
        InterestRecalculationCompoundingMethod compoundingMethod = null;
        if (this.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            recalculationFrequencyType = this.loanProduct.getProductInterestRecalculationDetails().getRestFrequencyType();
            compoundingMethod = InterestRecalculationCompoundingMethod.fromInt(this.loanProduct.getProductInterestRecalculationDetails()
                    .getInterestRecalculationCompoundingMethod());
        }

        return LoanApplicationTerms
                .assembleFrom(applicationCurrency, loanTermFrequency, loanTermPeriodFrequencyType, nthDayType, dayOfWeekType,
                        expectedDisbursementDate, repaymentsStartingFromDate, calculatedRepaymentsStartingFromDate,
                        inArrearsToleranceMoney, this.loanRepaymentScheduleDetail, loanProduct.isMultiDisburseLoan(), emiAmount,
                        disbursementData, maxOutstandingBalance, loanVariationTermsData, interestChargedFromDate,
                        this.loanProduct.getPrincipalThresholdForLastInstallment(), this.loanProduct.getInstallmentAmountInMultiplesOf(),
                        recalculationFrequencyType, restCalendarInstance, compoundingMethod,
                        this.loanProduct.preCloseInterestCalculationStrategy());
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
            this.rescheduledOnDate = rescheduledOnDate.toDate();
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

    public Integer getRepaymentFrequencyNthDayType() {
        return this.repaymentFrequencyNthDayType;
    }

    public void setRepaymentFrequencyNthDayType(Integer repaymentFrequencyNthDayType) {
        this.repaymentFrequencyNthDayType = repaymentFrequencyNthDayType;
    }

    public Integer getRepaymentFrequencyDayOfWeekType() {
        return this.repaymentFrequencyDayOfWeekType;
    }

    public void setRepaymentFrequencyDayOfWeekType(Integer repaymentFrequencyDayOfWeekType) {
        this.repaymentFrequencyDayOfWeekType = repaymentFrequencyDayOfWeekType;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public Client getClient() {
        return this.client;
    }

    public Boolean shouldCreateStandingInstructionAtDisbursement() {
        return (this.createStandingInstructionAtDisbursement != null) && this.createStandingInstructionAtDisbursement;
    }

    public Collection<LoanCharge> getLoanCharges(LocalDate dueDate) {
        Collection<LoanCharge> loanCharges = new ArrayList<>();

        for (LoanCharge loanCharge : charges) {

            if ((loanCharge.getDueLocalDate() != null) && loanCharge.getDueLocalDate().equals(dueDate)) {
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

        final ChangedTransactionDetail changedTransactionDetail = handleRefundTransaction(loanTransaction, loanLifecycleStateMachine, null);

        return changedTransactionDetail;

    }

    private void validateRefundDateIsAfterLastRepayment(final LocalDate refundTransactionDate) {
        final LocalDate possibleNextRefundDate = possibleNextRefundDate();

        if (possibleNextRefundDate == null || refundTransactionDate.isBefore(possibleNextRefundDate)) { throw new InvalidRefundDateException(
                refundTransactionDate.toString()); }

    }

    private ChangedTransactionDetail handleRefundTransaction(final LoanTransaction loanTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final LoanTransaction adjustedTransaction) {

        ChangedTransactionDetail changedTransactionDetail = null;

        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_REFUND, LoanStatus.fromInt(this.loanStatus));
        this.loanStatus = statusEnum.getValue();

        loanTransaction.updateLoan(this);

        // final boolean isTransactionChronologicallyLatest =
        // isChronologicallyLatestRefund(loanTransaction,
        // this.loanTransactions);

        if (status().isOverpaid() || status().isClosed()) {

            final String errorMessage = "This refund option is only for active loans ";
            throw new InvalidLoanStateTransitionException("transaction", "is.exceeding.overpaid.amount", errorMessage, this.totalOverpaid,
                    loanTransaction.getAmount(getCurrency()).getAmount());

        } else if (this.getTotalPaidInRepayments().isZero()) {
            final String errorMessage = "Cannot refund when no payment has been made";
            throw new InvalidLoanStateTransitionException("transaction", "no.payment.yet.made.for.loan", errorMessage);
        }

        if (loanTransaction.isNotZero(loanCurrency())) {
            this.loanTransactions.add(loanTransaction);
        }

        if (loanTransaction.isNotRefundForActiveLoan()) {
            final String errorMessage = "A transaction of type refund was expected but not received.";
            throw new InvalidLoanTransactionTypeException("transaction", "is.not.a.refund.transaction", errorMessage);
        }

        final LocalDate loanTransactionDate = loanTransaction.getTransactionDate();
        if (loanTransactionDate.isBefore(getDisbursementDate())) {
            final String errorMessage = "The transaction date cannot be before the loan disbursement date: "
                    + getApprovedOnDate().toString();
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.before.disbursement.date", errorMessage,
                    loanTransactionDate, getDisbursementDate());
        }

        if (loanTransactionDate.isAfter(DateUtils.getLocalDateOfTenant())) {
            final String errorMessage = "The transaction date cannot be in the future.";
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.a.future.date", errorMessage, loanTransactionDate);
        }

        if (this.loanProduct.isMultiDisburseLoan() && adjustedTransaction == null) {
            BigDecimal totalDisbursed = getDisbursedAmount();
            if (totalDisbursed.compareTo(this.summary.getTotalPrincipalRepaid()) < 0) {
                final String errorMessage = "The transaction cannot be done before the loan disbursement: "
                        + getApprovedOnDate().toString();
                throw new InvalidLoanStateTransitionException("transaction", "cannot.be.done.before.disbursement", errorMessage);
            }
        }

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);

        // If is a refund
        if (adjustedTransaction == null) {
            loanRepaymentScheduleTransactionProcessor.handleRefund(loanTransaction, getCurrency(), this.repaymentScheduleInstallments,
                    charges());
        } else {
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
            changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(),
                    allNonContraTransactionsPostDisbursement, getCurrency(), this.repaymentScheduleInstallments, charges(), null);
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                mapEntry.getValue().updateLoan(this);
            }

        }

        updateLoanSummaryDerivedFields();

        doPostLoanTransactionChecks(loanTransaction.getTransactionDate(), loanLifecycleStateMachine);

        return changedTransactionDetail;
    }

    public LocalDate possibleNextRefundDate() {

        final LocalDate now = new LocalDate();

        LocalDate lastTransactionDate = null;
        for (final LoanTransaction transaction : this.loanTransactions) {
            if ((transaction.isRepayment() || transaction.isRefundForActiveLoan()) && transaction.isNonZero()) {
                lastTransactionDate = transaction.getTransactionDate();
            }
        }

        return lastTransactionDate == null ? now : lastTransactionDate;
    }

}
