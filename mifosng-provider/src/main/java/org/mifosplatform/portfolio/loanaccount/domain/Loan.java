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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
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
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeAddedException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.collateral.data.CollateralData;
import org.mifosplatform.portfolio.collateral.domain.LoanCollateral;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.mifosplatform.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanApplicationDateException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerAssignmentDateException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerAssignmentException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerUnassignmentDateException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelPeriod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_loan", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_no" }, name = "loan_account_no_UNIQUE"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "loan_externalid_UNIQUE") })
public class Loan extends AbstractPersistable<Long> {

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
    private Set<LoanCharge> charges = new HashSet<LoanCharge>();

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
    private final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = new ArrayList<LoanRepaymentScheduleInstallment>();

    // see
    // http://stackoverflow.com/questions/4334970/hibernate-cannot-simultaneously-fetch-multiple-bags
    @OrderBy(value = "dateOf, id")
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true)
    private final List<LoanTransaction> loanTransactions = new ArrayList<LoanTransaction>();

    @Embedded
    private LoanSummary summary;

    @OneToOne(mappedBy = "loan", cascade = CascadeType.ALL, optional = true, orphanRemoval = true, fetch = FetchType.LAZY)
    private LoanSummaryArrearsAging summaryArrearsAging;

    @Transient
    private boolean accountNumberRequiresAutoGeneration = false;
    @Transient
    private LoanRepaymentScheduleTransactionProcessorFactory transactionProcessorFactory;

    @Transient
    private LoanLifecycleStateMachine loanLifecycleStateMachine;
    @Transient
    private LoanSummaryWrapper loanSummaryWrapper;

    public static Loan newIndividualLoanApplication(final String accountNo, final Client client, final Integer loanType,
            final LoanProduct loanProduct, final Fund fund, final Staff officer, final CodeValue loanPurpose,
            final LoanTransactionProcessingStrategy transactionProcessingStrategy,
            final LoanProductRelatedDetail loanRepaymentScheduleDetail, final Set<LoanCharge> loanCharges,
            final Set<LoanCollateral> collateral) {
        final LoanStatus status = null;
        final Group group = null;
        final Boolean syncDisbursementWithMeeting = null;
        return new Loan(accountNo, client, group, loanType, fund, officer, loanPurpose, transactionProcessingStrategy, loanProduct,
                loanRepaymentScheduleDetail, status, loanCharges, collateral, syncDisbursementWithMeeting);
    }

    public static Loan newGroupLoanApplication(final String accountNo, final Group group, final Integer loanType,
            final LoanProduct loanProduct, final Fund fund, final Staff officer,
            final LoanTransactionProcessingStrategy transactionProcessingStrategy,
            final LoanProductRelatedDetail loanRepaymentScheduleDetail, final Set<LoanCharge> loanCharges,
            final Boolean syncDisbursementWithMeeting) {
        final LoanStatus status = null;
        final CodeValue loanPurpose = null;
        final Set<LoanCollateral> collateral = null;
        final Client client = null;
        return new Loan(accountNo, client, group, loanType, fund, officer, loanPurpose, transactionProcessingStrategy, loanProduct,
                loanRepaymentScheduleDetail, status, loanCharges, collateral, syncDisbursementWithMeeting);
    }

    public static Loan newIndividualLoanApplicationFromGroup(final String accountNo, final Client client, final Group group,
            final Integer loanType, final LoanProduct loanProduct, final Fund fund, final Staff officer,
            final LoanTransactionProcessingStrategy transactionProcessingStrategy,
            final LoanProductRelatedDetail loanRepaymentScheduleDetail, final Set<LoanCharge> loanCharges,
            final Boolean syncDisbursementWithMeeting) {
        final LoanStatus status = null;
        final CodeValue loanPurpose = null;
        final Set<LoanCollateral> collateral = null;
        return new Loan(accountNo, client, group, loanType, fund, officer, loanPurpose, transactionProcessingStrategy, loanProduct,
                loanRepaymentScheduleDetail, status, loanCharges, collateral, syncDisbursementWithMeeting);
    }

    protected Loan() {
        this.client = null;
    }

    private Loan(final String accountNo, final Client client, final Group group, final Integer loanType, final Fund fund,
            final Staff loanOfficer, final CodeValue loanPurpose, final LoanTransactionProcessingStrategy transactionProcessingStrategy,
            final LoanProduct loanProduct, final LoanProductRelatedDetail loanRepaymentScheduleDetail, final LoanStatus loanStatus,
            final Set<LoanCharge> loanCharges, final Set<LoanCollateral> collateral, final Boolean syncDisbursementWithMeeting) {

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

        for (final LoanCharge charge : setOfLoanCharges()) {
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

    public ChangedTransactionDetail addLoanCharge(final LoanCharge loanCharge, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {
        ChangedTransactionDetail changedTransactionDetail = null;

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
        setOfLoanCharges().add(loanCharge);

        this.summary = updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);

        // store Id's of existing loan transactions and existing reversed loan
        // transactions
        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        if (!loanCharge.isDueAtDisbursement()) {
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
            changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(),
                    allNonContraTransactionsPostDisbursement, getCurrency(), this.repaymentScheduleInstallments, setOfLoanCharges());
            for (final LoanTransaction transaction : changedTransactionDetail.getNewTransactions()) {
                transaction.updateLoan(this);
            }
            this.loanTransactions.addAll(changedTransactionDetail.getNewTransactions());
        } else {
            // just reprocess the loan schedule only for now.
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(getCurrency(), getDisbursementDate(), this.repaymentScheduleInstallments, setOfLoanCharges());
        }

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
            final LocalDate currentDate = DateUtils.getLocalDateOfTenant();

            // if loan charge is to be applied on a future date, the loan
            // transaction would show todays date as applied date
            if (transactionDate == null || currentDate.isBefore(transactionDate)) {
                transactionDate = currentDate;
            }
        }

        final LoanTransaction applyLoanChargeTransaction = LoanTransaction.applyLoanCharge(this, this.getOffice(), chargeAmount,
                transactionDate, feeCharges, penaltyCharges);
        this.loanTransactions.add(applyLoanChargeTransaction);
        return applyLoanChargeTransaction;
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
            final String defaultUserMessage = "This charge which is due at disbursement cannot be added as the loan is already disbursed.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "specified.due.date.outside.range", defaultUserMessage,
                    getDisbursementDate(), getLastRepaymentPeriodDueDate(), loanCharge.name());
        }
    }

    private LocalDate getLastRepaymentPeriodDueDate() {
        return this.repaymentScheduleInstallments.get(this.repaymentScheduleInstallments.size() - 1).getDueDate();
    }

    public void removeLoanCharge(final LoanCharge loanCharge) {

        validateLoanIsNotClosed(loanCharge);

        // NOTE: to remove this constraint requires that loan transactions
        // that represent the waive of charges also be removed (or reversed)
        // if you want ability to remove loan charges that are waived.
        validateLoanChargeIsNotWaived(loanCharge);

        final boolean removed = setOfLoanCharges().remove(loanCharge);
        if (removed) {
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
                    getCurrency(), this.repaymentScheduleInstallments, setOfLoanCharges());
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

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(3);

        validateLoanIsNotClosed(loanCharge);

        if (setOfLoanCharges().contains(loanCharge)) {
            final Map<String, Object> loanChargeChanges = loanCharge.update(command, getPrincpal().getAmount());
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
                    getCurrency(), this.repaymentScheduleInstallments, setOfLoanCharges());
        } else {
            // reprocess loan schedule based on charge been waived.
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(getCurrency(), getDisbursementDate(), this.repaymentScheduleInstallments, setOfLoanCharges());
        }

        updateLoanSummaryDerivedFields();

        return actualChanges;
    }

    public LoanTransaction waiveLoanCharge(final LoanCharge loanCharge, final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final Map<String, Object> changes, final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds) {

        validateLoanIsNotClosed(loanCharge);

        final Money amountWaived = loanCharge.waive(loanCurrency());

        changes.put("amount", amountWaived.getAmount());

        Money feeChargesWaived = amountWaived;
        Money penaltyChargesWaived = Money.zero(loanCurrency());
        if (loanCharge.isPenaltyCharge()) {
            penaltyChargesWaived = amountWaived;
            feeChargesWaived = Money.zero(loanCurrency());
        }

        LocalDate transactionDate = getDisbursementDate();
        if (loanCharge.isSpecifiedDueDate()) {
            transactionDate = loanCharge.getDueLocalDate();
        }

        updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        final LoanTransaction waiveLoanChargeTransaction = LoanTransaction.waiveLoanCharge(this, this.getOffice(), amountWaived,
                transactionDate, feeChargesWaived, penaltyChargesWaived);
        this.loanTransactions.add(waiveLoanChargeTransaction);

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
                    getCurrency(), this.repaymentScheduleInstallments, setOfLoanCharges());
        } else {
            // reprocess loan schedule based on charge been waived.
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(getCurrency(), getDisbursementDate(), this.repaymentScheduleInstallments, setOfLoanCharges());
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
        setOfLoanCharges().clear();
        setOfLoanCharges().addAll(associateChargesWithThisLoan(loanCharges));
        updateSummaryWithTotalFeeChargesDueAtDisbursement(deriveSumTotalOfChargesDueAtDisbursement());
    }

    public void updateLoanCollateral(final Set<LoanCollateral> loanCollateral) {
        if (this.collateral == null) {
            this.collateral = new HashSet<LoanCollateral>();
        }
        this.collateral.clear();
        this.collateral.addAll(associateWithThisLoan(loanCollateral));
    }

    public void updateLoanSchedule(final LoanScheduleModel modifiedLoanSchedule) {
        this.repaymentScheduleInstallments.clear();

        for (final LoanScheduleModelPeriod scheduledLoanInstallment : modifiedLoanSchedule.getPeriods()) {

            if (scheduledLoanInstallment.isRepaymentPeriod()) {
                final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(this,
                        scheduledLoanInstallment.periodNumber(), scheduledLoanInstallment.periodFromDate(),
                        scheduledLoanInstallment.periodDueDate(), scheduledLoanInstallment.principalDue(),
                        scheduledLoanInstallment.interestDue(), scheduledLoanInstallment.feeChargesDue(),
                        scheduledLoanInstallment.penaltyChargesDue());
                addRepaymentScheduleInstallment(installment);
            }
        }

        updateLoanScheduleDependentDerivedFields();
        updateLoanSummaryDerivedFields();
    }

    private void updateLoanScheduleDependentDerivedFields() {
        this.expectedMaturityDate = determineExpectedMaturityDate().toDate();
        this.actualMaturityDate = determineExpectedMaturityDate().toDate();
    }

    private void updateLoanSummaryDerivedFields() {

        if (isNotDisbursed()) {
            this.summary.zeroFields();
            this.summaryArrearsAging = null;
            this.totalOverpaid = null;
        } else {
            final Money overpaidBy = calculateTotalOverpayment();
            this.totalOverpaid = overpaidBy.getAmountDefaultedToNullIfZero();

            final Money principal = this.loanRepaymentScheduleDetail.getPrincipal();
            this.summary.updateSummary(loanCurrency(), principal, this.repaymentScheduleInstallments, this.loanSummaryWrapper,
                    isDisbursed());
            if (this.summaryArrearsAging == null) {
                this.summaryArrearsAging = new LoanSummaryArrearsAging(this);
            }
            this.summaryArrearsAging.updateSummary(loanCurrency(), this.repaymentScheduleInstallments, this.loanSummaryWrapper);
            if (this.summaryArrearsAging.isNotInArrears(loanCurrency())) {
                this.summaryArrearsAging = null;
            }
        }
    }

    public Map<String, Object> loanApplicationModification(final JsonCommand command, final Set<LoanCharge> possiblyModifedLoanCharges,
            final Set<LoanCollateral> possiblyModifedLoanCollateralItems, final AprCalculator aprCalculator) {

        final Map<String, Object> actualChanges = this.loanRepaymentScheduleDetail.updateLoanApplicationAttributes(command, aprCalculator);
        if (!actualChanges.isEmpty()) {
            final boolean recalculateLoanSchedule = !(actualChanges.size() == 1 && actualChanges.containsKey("inArrearsTolerance"));
            actualChanges.put("recalculateLoanSchedule", recalculateLoanSchedule);
        }

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        final String accountNoParamName = "accountNo";
        if (command.isChangeInStringParameterNamed(accountNoParamName, this.accountNumber)) {
            final String newValue = command.stringValueOfParameterNamed(accountNoParamName);
            actualChanges.put(accountNoParamName, newValue);
            this.accountNumber = StringUtils.defaultIfEmpty(newValue, null);
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
        if (command.parameterExists(chargesParamName)) {

            final Set<LoanCharge> existingLoanCharges = setOfLoanCharges();

            if (!possiblyModifedLoanCharges.equals(existingLoanCharges)) {
                actualChanges.put(chargesParamName, getLoanCharges(possiblyModifedLoanCharges));

                actualChanges.put(chargesParamName, getLoanCharges(possiblyModifedLoanCharges));
                actualChanges.put("recalculateLoanSchedule", true);

                for (final LoanCharge loanCharge : possiblyModifedLoanCharges) {
                    validateChargeHasValidSpecifiedDateIfApplicable(loanCharge, getDisbursementDate(), getLastRepaymentPeriodDueDate());
                }
            }
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

        return actualChanges;
    }

    private Set<LoanCharge> setOfLoanCharges() {
        Set<LoanCharge> loanCharges = this.charges;
        if (this.charges == null) {
            loanCharges = new HashSet<LoanCharge>();
        }
        return loanCharges;
    }

    private CollateralData[] listOfLoanCollateralData(final Set<LoanCollateral> setOfLoanCollateral) {

        CollateralData[] existingLoanCollateral = null;

        final List<CollateralData> loanCollateralList = new ArrayList<CollateralData>();
        for (final LoanCollateral loanCollateral : setOfLoanCollateral) {

            final CollateralData data = loanCollateral.toData();

            loanCollateralList.add(data);
        }

        existingLoanCollateral = loanCollateralList.toArray(new CollateralData[loanCollateralList.size()]);

        return existingLoanCollateral;
    }

    private LoanChargeCommand[] getLoanCharges(final Set<LoanCharge> setOfLoanCharges) {

        LoanChargeCommand[] existingLoanCharges = null;

        final List<LoanChargeCommand> loanChargesList = new ArrayList<LoanChargeCommand>();
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
            final WorkingDays workingDays, final boolean allowTransactionsOnNonWorkingDay) {

        updateLoanSchedule(loanSchedule);

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

        updateLoanScheduleDependentDerivedFields();

        if (submittedOn.isAfter(DateUtils.getLocalDateOfTenant())) {
            final String errorMessage = "The date on which a loan is submitted cannot be in the future.";
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.a.future.date", errorMessage, submittedOn);
        }

        if (this.client != null && this.client.isActivatedAfter(submittedOn)) {
            final String errorMessage = "The date on which a loan is submitted cannot be earlier than client's activation date.";
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.before.client.activation.date", errorMessage, submittedOn);
        }
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
        for (final LoanCharge loanCharge : setOfLoanCharges()) {
            validateChargeHasValidSpecifiedDateIfApplicable(loanCharge, getDisbursementDate(), getLastRepaymentPeriodDueDate());
        }

        // validate if disbursement date is a holiday or a non-working day
        validateDisbursementDateIsOnNonWorkingDay(workingDays, allowTransactionsOnNonWorkingDay);
        validateDisbursementDateIsOnHoliday(allowTransactionsOnHoliday, holidays);
    }

    private LocalDate determineExpectedMaturityDate() {
        final int numberOfInstallments = this.repaymentScheduleInstallments.size();
        return this.repaymentScheduleInstallments.get(numberOfInstallments - 1).getDueDate();
    }

    public Map<String, Object> loanApplicationRejection(final AppUser currentUser, final JsonCommand command,
            final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();

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

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();

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
                throw new InvalidLoanStateTransitionException("reject", "cannot.be.before.submittal.date", errorMessage, command,
                        getSubmittedOnDate());
            }

            if (withdrawnOn.isAfter(DateUtils.getLocalDateOfTenant())) {
                final String errorMessage = "The date on which a loan is withdrawn cannot be in the future.";
                throw new InvalidLoanStateTransitionException("reject", "cannot.be.a.future.date", errorMessage, command);
            }
        } else {
            final String errorMessage = "Only the loan applications with status 'Submitted and pending approval' are allowed to be withdrawn by applicant.";
            throw new InvalidLoanStateTransitionException("withdraw", "cannot.withdraw", errorMessage);
        }

        return actualChanges;
    }

    public Map<String, Object> loanApplicationApproval(final AppUser currentUser, final JsonCommand command,
            final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();

        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_APPROVED, LoanStatus.fromInt(this.loanStatus));
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
            this.loanStatus = statusEnum.getValue();
            actualChanges.put("status", LoanEnumerations.status(this.loanStatus));

            // only do below if status has changed in the 'approval' case
            LocalDate approvedOn = command.localDateValueOfParameterNamed("approvedOnDate");
            String approvedOnDateChange = command.stringValueOfParameterNamed("approvedOnDate");
            if (approvedOn == null) {
                approvedOn = command.localDateValueOfParameterNamed("eventDate");
                approvedOnDateChange = command.stringValueOfParameterNamed("eventDate");
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

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();

        final LoanStatus currentStatus = LoanStatus.fromInt(this.loanStatus);
        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_APPROVAL_UNDO, currentStatus);
        if (!statusEnum.hasStateOf(currentStatus)) {
            this.loanStatus = statusEnum.getValue();
            actualChanges.put("status", LoanEnumerations.status(this.loanStatus));

            this.approvedOnDate = null;
            this.approvedBy = null;
            actualChanges.put("approvedOnDate", "");

            this.loanOfficerHistory.clear();
        }

        return actualChanges;
    }

    private Collection<Long> findExistingTransactionIds() {

        final Collection<Long> ids = new ArrayList<Long>();

        for (final LoanTransaction transaction : this.loanTransactions) {
            ids.add(transaction.getId());
        }

        return ids;
    }

    private Collection<Long> findExistingReversedTransactionIds() {

        final Collection<Long> ids = new ArrayList<Long>();

        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isReversed()) {
                ids.add(transaction.getId());
            }
        }

        return ids;
    }

    public Map<String, Object> disburse(final LoanScheduleGeneratorFactory loanScheduleFactory, final AppUser currentUser,
            final JsonCommand command, final ApplicationCurrency currency, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, final Map<String, Object> actualChanges, final PaymentDetail paymentDetail,
            final LocalDate calculatedRepaymentsStartingFromDate, final boolean isHolidayEnabled, final List<Holiday> holidays,
            final WorkingDays workingDays, final boolean allowTransactionsOnHoliday, final boolean allowTransactionsOnNonWorkingDay) {

        updateLoanToPreDisbursalState();

        final LoanStatus statusEnum = this.loanLifecycleStateMachine.transition(LoanEvent.LOAN_DISBURSED,
                LoanStatus.fromInt(this.loanStatus));
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {

            existingTransactionIds.addAll(findExistingTransactionIds());
            existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

            this.loanStatus = statusEnum.getValue();
            actualChanges.put("status", LoanEnumerations.status(this.loanStatus));

            final LocalDate actualDisbursementDate = command.localDateValueOfParameterNamed("actualDisbursementDate");
            final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

            this.actualDisbursementDate = actualDisbursementDate.toDate();
            this.disbursedBy = currentUser;
            updateLoanScheduleDependentDerivedFields();

            actualChanges.put("locale", command.locale());
            actualChanges.put("dateFormat", command.dateFormat());
            actualChanges.put("actualDisbursementDate", command.stringValueOfParameterNamed("actualDisbursementDate"));

            // validate if disbursement date is a holiday or a non-working day
            validateDisbursementDateIsOnNonWorkingDay(workingDays, allowTransactionsOnNonWorkingDay);
            validateDisbursementDateIsOnHoliday(allowTransactionsOnHoliday, holidays);

            handleDisbursementTransaction(paymentDetail, actualDisbursementDate, txnExternalId);

            if (isRepaymentScheduleRegenerationRequiredForDisbursement(actualDisbursementDate)) {
                regenerateRepaymentSchedule(loanScheduleFactory, currency, calculatedRepaymentsStartingFromDate, isHolidayEnabled,
                        holidays, workingDays);
                updateLoanRepaymentPeriodsDerivedFields(actualDisbursementDate);
            } else {
                updateLoanRepaymentPeriodsDerivedFields(actualDisbursementDate);
                updateLoanSummaryDerivedFields();
            }

            final Money interestApplied = Money.of(getCurrency(), this.summary.getTotalInterestCharged());
            final LoanTransaction interestAppliedTransaction = LoanTransaction.applyInterest(this.getOffice(), this, interestApplied,
                    actualDisbursementDate);
            this.loanTransactions.add(interestAppliedTransaction);
        }

        return actualChanges;
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
    private void regenerateRepaymentSchedule(final LoanScheduleGeneratorFactory loanScheduleFactory,
            final ApplicationCurrency applicationCurrency, final LocalDate calculatedRepaymentsStartingFromDate,
            final boolean isHolidayEnabled, final List<Holiday> holidays, final WorkingDays workingDays) {

        final InterestMethod interestMethod = this.loanRepaymentScheduleDetail.getInterestMethod();
        final LoanScheduleGenerator loanScheduleGenerator = loanScheduleFactory.create(interestMethod);

        final RoundingMode roundingMode = RoundingMode.HALF_EVEN;
        final MathContext mc = new MathContext(8, roundingMode);

        final Integer loanTermFrequency = this.termFrequency;
        final PeriodFrequencyType loanTermPeriodFrequencyType = PeriodFrequencyType.fromInt(this.termPeriodFrequencyType);
        final LoanApplicationTerms loanApplicationTerms = LoanApplicationTerms.assembleFrom(applicationCurrency, loanTermFrequency,
                loanTermPeriodFrequencyType, getDisbursementDate(), getExpectedFirstRepaymentOnDate(),
                calculatedRepaymentsStartingFromDate, getInArrearsTolerance(), this.loanRepaymentScheduleDetail);

        final LoanScheduleModel loanSchedule = loanScheduleGenerator.generate(mc, applicationCurrency, loanApplicationTerms, this.charges,
                isHolidayEnabled, holidays, workingDays);

        updateLoanSchedule(loanSchedule);
    }

    private LoanTransaction handleDisbursementTransaction(final PaymentDetail paymentDetail, final LocalDate disbursedOn,
            final String txnExternalId) {
        // track disbursement transaction
        final LoanTransaction disbursementTransaction = LoanTransaction.disbursement(this.getOffice(),
                this.loanRepaymentScheduleDetail.getPrincipal(), paymentDetail, disbursedOn, txnExternalId);
        disbursementTransaction.updateLoan(this);
        this.loanTransactions.add(disbursementTransaction);

        // add repayment transaction to track incoming money from client to mfi
        // for (charges due at time of disbursement)

        /***
         * TODO Vishwas: do we need to be able to pass in payment type details
         * for repayments at disbursements too?
         ***/
        final Money totalFeeChargesDueAtDisbursement = this.summary.getTotalFeeChargesDueAtDisbursement(loanCurrency());
        if (totalFeeChargesDueAtDisbursement.isGreaterThanZero()) {

            final LoanTransaction chargesPayment = LoanTransaction.repaymentAtDisbursement(this.getOffice(),
                    totalFeeChargesDueAtDisbursement, null, disbursedOn, txnExternalId);
            final Money zero = Money.zero(getCurrency());
            chargesPayment.updateComponents(zero, zero, totalFeeChargesDueAtDisbursement, zero);
            chargesPayment.updateLoan(this);
            this.loanTransactions.add(chargesPayment);

            /**
             * all Charges repaid at disbursal is marked as repaid and
             * "APPLY Charge" transactions are created for all other fees (
             * which are created during disbursal but not repaid)
             **/
            for (final LoanCharge charge : setOfLoanCharges()) {
                if (charge.isDueAtDisbursement()) {
                    charge.markAsFullyPaid();
                    // Add "Loan Charge Paid By" details to this transaction
                    final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(chargesPayment, charge, charge.amount());
                    chargesPayment.getLoanChargesPaid().add(loanChargePaidBy);
                } else {
                    handleChargeAppliedTransaction(charge, disbursedOn);
                }
            }
        }

        if (getApprovedOnDate() != null && disbursedOn.isBefore(getApprovedOnDate())) {
            final String errorMessage = "The date on which a loan is disbursed cannot be before its approval date: "
                    + getApprovedOnDate().toString();
            throw new InvalidLoanStateTransitionException("disbursal", "cannot.be.before.approval.date", errorMessage, disbursedOn,
                    getApprovedOnDate());
        }

        if (disbursedOn.isAfter(new LocalDate())) {
            final String errorMessage = "The date on which a loan with identifier : " + this.accountNumber
                    + " is disbursed cannot be in the future.";
            throw new InvalidLoanStateTransitionException("disbursal", "cannot.be.a.future.date", errorMessage, disbursedOn);
        }

        return disbursementTransaction;
    }

    public Map<String, Object> undoDisbursal(final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();

        final LoanStatus currentStatus = LoanStatus.fromInt(this.loanStatus);
        final LoanStatus statusEnum = this.loanLifecycleStateMachine.transition(LoanEvent.LOAN_DISBURSAL_UNDO, currentStatus);
        if (!statusEnum.hasStateOf(currentStatus)) {
            this.loanStatus = statusEnum.getValue();
            actualChanges.put("status", LoanEnumerations.status(this.loanStatus));

            this.actualDisbursementDate = null;
            this.disbursedBy = null;
            actualChanges.put("actualDisbursementDate", "");

            existingTransactionIds.addAll(findExistingTransactionIds());
            existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

            reverseExistingTransactions();

            updateLoanToPreDisbursalState();
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

        for (final LoanCharge charge : setOfLoanCharges()) {
            charge.resetToOriginal(loanCurrency());
        }

        for (final LoanRepaymentScheduleInstallment currentInstallment : this.repaymentScheduleInstallments) {
            currentInstallment.resetDerivedComponents();
        }

        final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
        wrapper.reprocess(getCurrency(), getDisbursementDate(), this.repaymentScheduleInstallments, setOfLoanCharges());

        updateLoanSummaryDerivedFields();
    }

    public ChangedTransactionDetail waiveInterest(final LoanTransaction waiveInterestTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        final ChangedTransactionDetail changedTransactionDetail = handleRepaymentOrWaiverTransaction(waiveInterestTransaction,
                loanLifecycleStateMachine, null);

        return changedTransactionDetail;
    }

    public ChangedTransactionDetail makeRepayment(final LoanTransaction repaymentTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds, final boolean allowTransactionsOnHoliday, final List<Holiday> holidays,
            final WorkingDays workingDays, final boolean allowTransactionsOnNonWorkingDay) {

        validateRepaymentDateIsOnHoliday(repaymentTransaction.getTransactionDate(), allowTransactionsOnHoliday, holidays);
        validateRepaymentDateIsOnNonWorkingDay(repaymentTransaction.getTransactionDate(), workingDays, allowTransactionsOnNonWorkingDay);

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        final ChangedTransactionDetail changedTransactionDetail = handleRepaymentOrWaiverTransaction(repaymentTransaction,
                loanLifecycleStateMachine, null);

        return changedTransactionDetail;
    }

    private ChangedTransactionDetail handleRepaymentOrWaiverTransaction(final LoanTransaction loanTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final LoanTransaction adjustedTransaction) {

        ChangedTransactionDetail changedTransactionDetail = null;

        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_REPAYMENT_OR_WAIVER,
                LoanStatus.fromInt(this.loanStatus));
        this.loanStatus = statusEnum.getValue();

        loanTransaction.updateLoan(this);

        final boolean isTransactionChronologicallyLatest = isChronologicallyLatestRepaymentOrWaiver(loanTransaction, this.loanTransactions);

        if (loanTransaction.isNotZero(loanCurrency())) {
            this.loanTransactions.add(loanTransaction);
        }

        if (loanTransaction.isNotRepayment() && loanTransaction.isNotWaiver()) {
            final String errorMessage = "A transaction of type repayment or waiver was expected but not received.";
            throw new InvalidLoanTransactionTypeException("transaction", "is.not.a.repayment.or.waiver.transaction", errorMessage);
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

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                .determineProcessor(this.transactionProcessingStrategy);
        if (isTransactionChronologicallyLatest && adjustedTransaction == null) {
            loanRepaymentScheduleTransactionProcessor.handleTransaction(loanTransaction, getCurrency(), this.repaymentScheduleInstallments,
                    setOfLoanCharges());
        } else {
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
            changedTransactionDetail = loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(),
                    allNonContraTransactionsPostDisbursement, getCurrency(), this.repaymentScheduleInstallments, setOfLoanCharges());
            for (final LoanTransaction newLoanTransaction : changedTransactionDetail.getNewTransactions()) {
                newLoanTransaction.updateLoan(this);
            }
            this.loanTransactions.addAll(changedTransactionDetail.getNewTransactions());
        }

        updateLoanSummaryDerivedFields();

        doPostLoanTransactionChecks(loanTransaction.getTransactionDate(), loanLifecycleStateMachine);

        return changedTransactionDetail;
    }

    private List<LoanTransaction> retreiveListOfTransactionsPostDisbursement() {
        final List<LoanTransaction> repaymentsOrWaivers = new ArrayList<LoanTransaction>();
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (!transaction.isDisbursement() && transaction.isNotReversed()) {
                repaymentsOrWaivers.add(transaction);
            }
        }
        final LoanTransactionComparator transactionComparator = new LoanTransactionComparator();
        Collections.sort(repaymentsOrWaivers, transactionComparator);
        return repaymentsOrWaivers;
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

        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.REPAID_IN_FULL, LoanStatus.fromInt(this.loanStatus));
        this.loanStatus = statusEnum.getValue();

        this.closedOnDate = transactionDate.toDate();
        this.actualMaturityDate = transactionDate.toDate();
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

    public Money possibleNextRepaymentAmount() {
        final MonetaryCurrency currency = this.loanRepaymentScheduleDetail.getPrincipal().getCurrency();
        Money possibleNextRepaymentAmount = Money.zero(currency);

        for (final LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
            if (installment.isNotFullyPaidOff()) {
                possibleNextRepaymentAmount = installment.getTotalOutstanding(currency);
                break;
            }
        }

        return possibleNextRepaymentAmount;
    }

    public LoanTransaction deriveDefaultInterestWaiverTransaction() {

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

        return LoanTransaction.waiver(this.getOffice(), this, possibleInterestToWaive, transactionDate);
    }

    public ChangedTransactionDetail adjustExistingTransaction(final LoanTransaction newTransactionDetail,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final LoanTransaction transactionForAdjustment,
            final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds,
            final boolean allowTransactionsOnHoliday, final List<Holiday> holidays, final WorkingDays workingDays,
            final boolean allowTransactionsOnNonWorkingDay) {

        validateRepaymentDateIsOnHoliday(newTransactionDetail.getTransactionDate(), allowTransactionsOnHoliday, holidays);
        validateRepaymentDateIsOnNonWorkingDay(newTransactionDetail.getTransactionDate(), workingDays, allowTransactionsOnNonWorkingDay);

        ChangedTransactionDetail changedTransactionDetail = null;

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        if (transactionForAdjustment.isNotRepayment() && transactionForAdjustment.isNotWaiver()) {
            final String errorMessage = "Only transactions of type repayment or waiver can be adjusted.";
            throw new InvalidLoanTransactionTypeException("transaction", "adjustment.is.only.allowed.to.repayment.or.waiver.transaction",
                    errorMessage);
        }

        transactionForAdjustment.reverse();

        if (isClosedWrittenOff()) {
            // find write off transaction and reverse it
            final LoanTransaction writeOffTransaction = findWriteOffTransaction();
            writeOffTransaction.reverse();
        }

        if (isClosedObligationsMet() || isClosedWrittenOff() || isClosedWithOutsandingAmountMarkedForReschedule()) {
            this.loanStatus = LoanStatus.ACTIVE.getValue();
        }

        if (newTransactionDetail.isRepayment() || newTransactionDetail.isInterestWaiver()) {
            changedTransactionDetail = handleRepaymentOrWaiverTransaction(newTransactionDetail, loanLifecycleStateMachine,
                    transactionForAdjustment);
        }

        return changedTransactionDetail;
    }

    private LoanTransaction findWriteOffTransaction() {

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

        final Money totalPaidInRepayments = getTotalPaidInRepayments();

        final MonetaryCurrency currency = loanCurrency();
        Money cumulativeTotalPaidOnInstallments = Money.zero(currency);
        Money cumulativeTotalWaivedOnInstallments = Money.zero(currency);

        for (final LoanRepaymentScheduleInstallment scheduledRepayment : this.repaymentScheduleInstallments) {

            cumulativeTotalPaidOnInstallments = cumulativeTotalPaidOnInstallments
                    .plus(scheduledRepayment.getPrincipalCompleted(currency).plus(scheduledRepayment.getInterestPaid(currency)))
                    .plus(scheduledRepayment.getFeeChargesPaid(currency)).plus(scheduledRepayment.getPenaltyChargesPaid(currency));

            cumulativeTotalWaivedOnInstallments = cumulativeTotalWaivedOnInstallments.plus(scheduledRepayment.getInterestWaived(currency));
        }

        // if total paid in transactions doesnt match repayment schedule then
        // theres an overpayment.
        return totalPaidInRepayments.minus(cumulativeTotalPaidOnInstallments);
    }

    private MonetaryCurrency loanCurrency() {
        return this.loanRepaymentScheduleDetail.getCurrency();
    }

    public LoanTransaction closeAsWrittenOff(final JsonCommand command, final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final Map<String, Object> changes, final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds,
            final AppUser currentUser) {

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

            if (writtenOffOnLocalDate.isAfter(DateUtils.getLocalDateOfTenant())) {
                final String errorMessage = "The date on which a loan is written off cannot be in the future.";
                throw new InvalidLoanStateTransitionException("writeoff", "cannot.be.a.future.date", errorMessage, writtenOffOnLocalDate);
            }

            loanTransaction = LoanTransaction.writeoff(this, this.getOffice(), writtenOffOnLocalDate, txnExternalId);
            final boolean isLastTransaction = isChronologicallyLatestTransaction(loanTransaction, this.loanTransactions);
            if (!isLastTransaction) {
                final String errorMessage = "The date of the writeoff transaction must occur on or before previous transactions.";
                throw new InvalidLoanStateTransitionException("writeoff", "must.occur.on.or.after.other.transaction.dates", errorMessage,
                        writtenOffOnLocalDate);
            }

            this.loanTransactions.add(loanTransaction);

            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                    .determineProcessor(this.transactionProcessingStrategy);
            loanRepaymentScheduleTransactionProcessor.handleWriteOff(loanTransaction, loanCurrency(), this.repaymentScheduleInstallments);

            updateLoanSummaryDerivedFields();
        }

        return loanTransaction;
    }

    public LoanTransaction close(final JsonCommand command, final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final Map<String, Object> changes, final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds) {

        existingTransactionIds.addAll(findExistingTransactionIds());
        existingReversedTransactionIds.addAll(findExistingReversedTransactionIds());

        final LocalDate closureDate = command.localDateValueOfParameterNamed("transactionDate");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");

        this.closedOnDate = closureDate.toDate();
        changes.put("closedOnDate", command.stringValueOfParameterNamed("transactionDate"));

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

                loanTransaction = LoanTransaction.writeoff(this, this.getOffice(), closureDate, txnExternalId);
                final boolean isLastTransaction = isChronologicallyLatestTransaction(loanTransaction, this.loanTransactions);
                if (!isLastTransaction) {
                    final String errorMessage = "The closing date of the loan must be on or after latest transaction date.";
                    throw new InvalidLoanStateTransitionException("close.loan", "must.occur.on.or.after.latest.transaction.date",
                            errorMessage, closureDate);
                }

                this.loanTransactions.add(loanTransaction);

                final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessorFactory
                        .determineProcessor(this.transactionProcessingStrategy);
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

        return loanTransaction;
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

    private boolean isNotDisbursed() {
        return !isDisbursed();
    }

    private boolean isDisbursed() {
        return hasDisbursementTransaction();
    }

    private boolean isClosed() {
        return status().isClosed() || isCancelled();
    }

    private boolean isClosedObligationsMet() {
        return status().isClosedObligationsMet();
    }

    private boolean isClosedWrittenOff() {
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

    private boolean isOpen() {
        return status().isActive();
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

    private LocalDate getApprovedOnDate() {
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
        return !new LocalDate(this.expectedDisbursementDate).isEqual(actualDisbursedOnDate);
    }

    private boolean isRepaymentScheduleRegenerationRequiredForDisbursement(final LocalDate actualDisbursementDate) {
        return isActualDisbursedOnDateEarlierOrLaterThanExpected(actualDisbursementDate);
    }

    private Money getTotalPaidInRepayments() {
        Money cumulativePaid = Money.zero(loanCurrency());

        for (final LoanTransaction repayment : this.loanTransactions) {
            if (repayment.isRepayment()) {
                cumulativePaid = cumulativePaid.plus(repayment.getAmount(loanCurrency()));
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

    private LocalDate getInterestChargedFromDate() {
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

    private Boolean isAccrualBasedAccountingEnabledOnLoanProduct() {
        return this.loanProduct.isAccrualBasedAccountingEnabled();
    }

    private Long productId() {
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

    public Map<String, Object> deriveAccountingBridgeData(final CurrencyData currencyData, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {

        final Map<String, Object> accountingBridgeData = new LinkedHashMap<String, Object>();
        accountingBridgeData.put("loanId", getId());
        accountingBridgeData.put("loanProductId", productId());
        accountingBridgeData.put("officeId", getOfficeId());
        accountingBridgeData.put("calculatedInterest", this.summary.getTotalInterestCharged());
        accountingBridgeData.put("cashBasedAccountingEnabled", isCashBasedAccountingEnabledOnLoanProduct());
        accountingBridgeData.put("accrualBasedAccountingEnabled", isAccrualBasedAccountingEnabledOnLoanProduct());

        final List<Map<String, Object>> newLoanTransactions = new ArrayList<Map<String, Object>>();
        for (final LoanTransaction transaction : this.loanTransactions) {
            if (transaction.isReversed() && !existingReversedTransactionIds.contains(transaction.getId())) {
                newLoanTransactions.add(transaction.toMapData(currencyData));
            } else if (!existingTransactionIds.contains(transaction.getId())) {
                newLoanTransactions.add(transaction.toMapData(currencyData));
            }
        }

        accountingBridgeData.put("newLoanTransactions", newLoanTransactions);
        return accountingBridgeData;
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
            final boolean isHolidayEnabled, final List<Holiday> holidays, final WorkingDays workingDays) {
        // first repayment's from date is same as disbursement date.
        LocalDate tmpFromDate = this.getDisbursementDate();
        final PeriodFrequencyType repaymentPeriodFrequencyType = this.loanRepaymentScheduleDetail.getRepaymentPeriodFrequencyType();
        final Integer loanRepaymentInterval = this.loanRepaymentScheduleDetail.getRepayEvery();
        final String frequency = CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(repaymentPeriodFrequencyType);

        LocalDate newRepaymentDate = null;
        for (final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : this.repaymentScheduleInstallments) {
            final LocalDate oldDueDate = loanRepaymentScheduleInstallment.getDueDate();
            // FIXME: AA this won't update repayment dates before current date.
            if (oldDueDate.isAfter(meetingStartDate) && oldDueDate.isAfter(DateUtils.getLocalDateOfTenant())) {
                newRepaymentDate = CalendarUtils.getNewRepaymentMeetingDate(recuringRule, meetingStartDate, oldDueDate,
                        loanRepaymentInterval, frequency, workingDays);

                final LocalDate maxDateLimitForNewRepayment = getMaxDateLimitForNewRepayment(repaymentPeriodFrequencyType,
                        loanRepaymentInterval, tmpFromDate);

                if (newRepaymentDate.isAfter(maxDateLimitForNewRepayment)) {
                    newRepaymentDate = CalendarUtils.getNextRepaymentMeetingDate(recuringRule, meetingStartDate, tmpFromDate,
                            loanRepaymentInterval, frequency, workingDays);
                }

                if (isHolidayEnabled) {
                    // reset repayment date with new meeting date
                    newRepaymentDate = CalendarUtils.getNewRepaymentMeetingDate(recuringRule, meetingStartDate, oldDueDate,
                            loanRepaymentInterval, frequency, workingDays);
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

    public void updateGroup(Group newGroup) {
        this.group = newGroup;
    }

    public Integer getCurrentLoanCounter() {
        return this.loanCounter;
    }

    public Integer getLoanProductLoanCounter() {
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

    public void updateInterestRateFrequencyType() {
        this.loanRepaymentScheduleDetail.updatenterestPeriodFrequencyType(this.loanProduct.getInterestPeriodFrequencyType());
    }

    public Integer getTermFrequency() {
        return this.termFrequency;
    }

    public Integer getTermPeriodFrequencyType() {
        return this.termPeriodFrequencyType;
    }

    public void validateExpectedDisbursementForHolidayAndNonWorkingDay(final WorkingDays workingDays,
            final boolean allowTransactionsOnHoliday, final List<Holiday> holidays, final boolean allowTransactionsOnNonWorkingDay) {
        // validate if disbursement date is a holiday or a non-working day
        validateDisbursementDateIsOnNonWorkingDay(workingDays, allowTransactionsOnNonWorkingDay);
        validateDisbursementDateIsOnHoliday(allowTransactionsOnHoliday, holidays);

    }

}