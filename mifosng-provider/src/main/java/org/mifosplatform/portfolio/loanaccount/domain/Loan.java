package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.RandomPasswordGenerator;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.charge.exception.LoanChargeCannotBeAddedException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.mifosplatform.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.mifosplatform.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerAssignmentException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerUnassignmentDateException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.DefaultLoanScheduleGeneratorFactory;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanSchedule;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_loan", uniqueConstraints = { 
        @UniqueConstraint(columnNames = { "account_no" }, name = "loan_account_no_UNIQUE"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "loan_externalid_UNIQUE") })
public class Loan extends AbstractAuditableCustom<AppUser, Long> {

    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "external_id")
    private String externalId;
    
    @ManyToOne(optional = true)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(optional = true)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private LoanProduct loanProduct;

    @ManyToOne
    @JoinColumn(name = "fund_id", nullable = true)
    private Fund fund;

    @ManyToOne
    @JoinColumn(name = "loan_officer_id", nullable = true)
    private Staff loanofficer;

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

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "submittedon_date")
    private Date submittedOnDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "rejectedon_date")
    private Date rejectedOnDate;

    @SuppressWarnings("unused")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "withdrawnon_date")
    private Date withdrawnOnDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "approvedon_date")
    private Date approvedOnDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "expected_disbursedon_date")
    private Date expectedDisbursedOnDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "expected_firstrepaymenton_date")
    private Date expectedFirstRepaymentOnDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "interest_calculated_from_date")
    private Date interestChargedFromDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "disbursedon_date")
    private Date disbursedOnDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "closedon_date")
    private Date closedOnDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "writtenoffon_date")
    private Date writtenOffOnDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "rescheduledon_date")
    private Date rescheduledOnDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "expected_maturedon_date")
    private Date expectedMaturityDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "maturedon_date")
    private Date maturedOnDate;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true)
    private Set<LoanCharge> charges = new HashSet<LoanCharge>();

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

    @Column(name = "total_charges_due_at_disbursement_derived", scale = 6, precision = 19)
    private BigDecimal totalChargesDueAtDisbursement;

    @Transient
    private boolean accountNumberRequiresAutoGeneration = false;

    @Transient
    private final LoanRepaymentScheduleTransactionProcessorFactory transactionProcessor = new LoanRepaymentScheduleTransactionProcessorFactory();

    public static Loan newIndividualLoanApplication(final String accountNo, final Client client, final LoanProduct loanProduct,
            final Fund fund, final Staff officer, final LoanTransactionProcessingStrategy transactionProcessingStrategy,
            final LoanSchedule loanSchedule, final Set<LoanCharge> loanCharges) {
        final LoanStatus status = null;
        LoanProductRelatedDetail loanRepaymentScheduleDetail = loanSchedule.loanProductRelatedDetail();
        return new Loan(accountNo, client, null, fund, officer, transactionProcessingStrategy, loanProduct, loanRepaymentScheduleDetail,
                status, loanCharges);
    }

    public static Loan newGroupLoanApplication(final String accountNo, final Group group, final LoanProduct loanProduct, final Fund fund,
            final Staff officer, final LoanTransactionProcessingStrategy transactionProcessingStrategy, final LoanSchedule loanSchedule,
            final Set<LoanCharge> loanCharges) {
        final LoanStatus status = null;
        LoanProductRelatedDetail loanRepaymentScheduleDetail = loanSchedule.loanProductRelatedDetail();
        return new Loan(accountNo, null, group, fund, officer, transactionProcessingStrategy, loanProduct, loanRepaymentScheduleDetail,
                status, loanCharges);
    }

    public static Loan newIndividualLoanApplicationFromGroup(final String accountNo, final Client client, final Group group,
            final LoanProduct loanProduct, final Fund fund, final Staff officer,
            final LoanTransactionProcessingStrategy transactionProcessingStrategy, final LoanSchedule loanSchedule,
            final Set<LoanCharge> loanCharges) {
        final LoanStatus status = null;
        LoanProductRelatedDetail loanRepaymentScheduleDetail = loanSchedule.loanProductRelatedDetail();
        return new Loan(accountNo, client, group, fund, officer, transactionProcessingStrategy, loanProduct, loanRepaymentScheduleDetail,
                status, loanCharges);
    }

    protected Loan() {
        //
    }

    private Loan(final String accountNo, final Client client, final Group group, Fund fund, Staff loanOfficer,
            final LoanTransactionProcessingStrategy transactionProcessingStrategy, final LoanProduct loanProduct,
            final LoanProductRelatedDetail loanRepaymentScheduleDetail, final LoanStatus loanStatus, final Set<LoanCharge> loanCharges) {

        if (StringUtils.isBlank(accountNo)) {
            this.accountNumber = new RandomPasswordGenerator(19).generate();
            this.accountNumberRequiresAutoGeneration = true;
        } else {
            this.accountNumber = accountNo;
        }
        this.client = client;
        this.group = group;
        this.fund = fund;
        this.loanofficer = loanOfficer;
        this.transactionProcessingStrategy = transactionProcessingStrategy;
        this.loanProduct = loanProduct;
        this.loanRepaymentScheduleDetail = loanRepaymentScheduleDetail;
        if (loanStatus != null) {
            this.loanStatus = loanStatus.getValue();
        } else {
            this.loanStatus = null;
        }
        if (loanCharges != null && !loanCharges.isEmpty()) {
            this.charges = associateChargesWithThisLoan(loanCharges);
            this.totalChargesDueAtDisbursement = deriveSumTotalOfChargesDueAtDisbursement();
        } else {
            this.charges = null;
        }
        this.loanOfficerHistory = null;
    }

    private BigDecimal deriveSumTotalOfChargesDueAtDisbursement() {

        Money chargesDue = Money.of(getCurrency(), BigDecimal.ZERO);

        for (LoanCharge charge : getNullPointerSafeLoanCharges()) {
            if (charge.isDueAtDisbursement()) {
                chargesDue = chargesDue.plus(charge.amount());
            }
        }

        return chargesDue.getAmount();
    }

    private Set<LoanCharge> associateChargesWithThisLoan(final Set<LoanCharge> loanCharges) {
        for (LoanCharge loanCharge : loanCharges) {
            loanCharge.update(this);
        }
        return loanCharges;
    }

    private void updateTotalChargesDueAtDisbursement() {
        this.totalChargesDueAtDisbursement = deriveSumTotalOfChargesDueAtDisbursement();
    }
    
    public boolean isAccountNumberRequiresAutoGeneration() {
        return this.accountNumberRequiresAutoGeneration;
    }
    
    public void setAccountNumberRequiresAutoGeneration(boolean accountNumberRequiresAutoGeneration) {
        this.accountNumberRequiresAutoGeneration = accountNumberRequiresAutoGeneration;
    }

    public void addLoanCharge(final LoanCharge loanCharge) {

        validateLoanIsNotClosed(loanCharge);

        if (isDisbursed() && loanCharge.isDueAtDisbursement()) {
            // Note: added this constraint to restrict adding charges to a loan
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
        getNullPointerSafeLoanCharges().add(loanCharge);

        updateTotalChargesDueAtDisbursement();

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessor
                .determineProcessor(this.transactionProcessingStrategy);

        if (!loanCharge.isDueAtDisbursement()) { // TODO - only need to
                                                 // reprocess transactions
                                                 // against loan schedule if
                                                 // loan charge is added with
                                                 // due date before latest
                                                 // transaction.
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
            loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(), allNonContraTransactionsPostDisbursement,
                    getCurrency(), this.repaymentScheduleInstallments, getNullPointerSafeLoanCharges());
        } else {
            // just reprocess the loan schedule only for now.
            LoanScheduleWrapper wrapper = new LoanScheduleWrapper();
            wrapper.reprocess(getCurrency(), getDisbursementDate(), this.repaymentScheduleInstallments, getNullPointerSafeLoanCharges());
        }
    }

    private void validateLoanIsNotClosed(final LoanCharge loanCharge) {
        if (isClosed()) {
            final String defaultUserMessage = "This charge cannot be added as the loan is already closed.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "loan.is.closed", defaultUserMessage, getId(), loanCharge.name());
        }
    }

    private void validateLoanChargeIsNotWaived(final LoanCharge loanCharge) {
        if (loanCharge.isWaivedOrPartiallyWaived(loanCurrency())) {
            final String defaultUserMessage = "This loan charge cannot be removed as the charge as already been waived.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "loanCharge.is.waived", defaultUserMessage, getId(), loanCharge.name());
        }
    }

    private void validateChargeHasValidSpecifiedDateIfApplicable(final LoanCharge loanCharge, final LocalDate disbursementDate,
            final LocalDate lastRepaymentPeriodDueDate) {
        if (loanCharge.isSpecifiedDueDate() && !loanCharge.isDueForCollectionBetween(disbursementDate, lastRepaymentPeriodDueDate)) {
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

        boolean removed = getNullPointerSafeLoanCharges().remove(loanCharge);
        if (removed) {
            updateTotalChargesDueAtDisbursement();
        }

        removeOrModifyTransactionAssociatedWithLoanChargeIfDueAtDisbursement(loanCharge);

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessor
                .determineProcessor(this.transactionProcessingStrategy);
        if (!loanCharge.isDueAtDisbursement() && loanCharge.isPaidOrPartiallyPaid(loanCurrency())) {
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
            loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(), allNonContraTransactionsPostDisbursement,
                    getCurrency(), this.repaymentScheduleInstallments, getNullPointerSafeLoanCharges());
        }
    }

    private void removeOrModifyTransactionAssociatedWithLoanChargeIfDueAtDisbursement(final LoanCharge loanCharge) {
        if (loanCharge.isDueAtDisbursement()) {
            LoanTransaction transactionToRemove = null;
            for (LoanTransaction transaction : this.loanTransactions) {
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

        if (getNullPointerSafeLoanCharges().contains(loanCharge)) {
            final Map<String, Object> loanChargeChanges = loanCharge.update(command, getPrincpal().getAmount());
            actualChanges.putAll(loanChargeChanges);
            updateTotalChargesDueAtDisbursement();
        }

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessor
                .determineProcessor(this.transactionProcessingStrategy);
        if (!loanCharge.isDueAtDisbursement()) {
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
            loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(), allNonContraTransactionsPostDisbursement,
                    getCurrency(), this.repaymentScheduleInstallments, getNullPointerSafeLoanCharges());
        } else {
            // reprocess loan schedule based on charge been waived.
            LoanScheduleWrapper wrapper = new LoanScheduleWrapper();
            wrapper.reprocess(getCurrency(), getDisbursementDate(), this.repaymentScheduleInstallments, getNullPointerSafeLoanCharges());
        }

        return actualChanges;
    }

    public LoanTransaction waiveLoanCharge(final LoanCharge loanCharge, final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final Map<String, Object> changes) {

        validateLoanIsNotClosed(loanCharge);

        final Money amountWaived = loanCharge.waive(loanCurrency());

        changes.put("amount", amountWaived.getAmount());

        Money feeChargesWaived = Money.of(loanCurrency(), loanCharge.amount());
        Money penaltyChargesWaived = Money.zero(loanCurrency());
        if (loanCharge.isPenaltyCharge()) {
            penaltyChargesWaived = Money.of(loanCurrency(), loanCharge.amount());
            feeChargesWaived = Money.zero(loanCurrency());
        }

        LocalDate transactionDate = getDisbursementDate();
        if (loanCharge.isSpecifiedDueDate()) {
            transactionDate = loanCharge.getDueForCollectionAsOfLocalDate();
        }

        updateTotalChargesDueAtDisbursement();

        final LoanTransaction waiveLoanChargeTransaction = LoanTransaction.waiveLoanCharge(this, amountWaived, transactionDate,
                feeChargesWaived, penaltyChargesWaived);

        // Waive of charges whose due date falls after latest 'repayment'
        // transaction dont require entire loan schedule to be reprocessed.
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessor
                .determineProcessor(this.transactionProcessingStrategy);
        if (!loanCharge.isDueAtDisbursement() && loanCharge.isPaidOrPartiallyPaid(loanCurrency())) {
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
            loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(), allNonContraTransactionsPostDisbursement,
                    getCurrency(), this.repaymentScheduleInstallments, getNullPointerSafeLoanCharges());
        } else {
            // reprocess loan schedule based on charge been waived.
            LoanScheduleWrapper wrapper = new LoanScheduleWrapper();
            wrapper.reprocess(getCurrency(), getDisbursementDate(), this.repaymentScheduleInstallments, getNullPointerSafeLoanCharges());
        }

        doPostLoanTransactionChecks(waiveLoanChargeTransaction.getTransactionDate(), loanLifecycleStateMachine);

        return waiveLoanChargeTransaction;
    }

    public Client client() {
        return this.client;
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

    public void updateFund(final Fund fund) {
        this.fund = fund;
    }

    public void updateTransactionProcessingStrategy(final LoanTransactionProcessingStrategy strategy) {
        this.transactionProcessingStrategy = strategy;
    }

    public void updateLoanCharges(final Set<LoanCharge> loanCharges) {
        getNullPointerSafeLoanCharges().clear();
        getNullPointerSafeLoanCharges().addAll(associateChargesWithThisLoan(loanCharges));
        this.totalChargesDueAtDisbursement = deriveSumTotalOfChargesDueAtDisbursement();
    }

    public void updateLoanSchedule(final LoanScheduleData modifiedLoanSchedule) {
        this.repaymentScheduleInstallments.clear();

        for (LoanSchedulePeriodData scheduledLoanInstallment : modifiedLoanSchedule.getPeriods()) {

            if (scheduledLoanInstallment.isRepaymentPeriod()) {
                LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(this,
                        scheduledLoanInstallment.periodNumber(), scheduledLoanInstallment.periodFromDate(),
                        scheduledLoanInstallment.periodDueDate(), scheduledLoanInstallment.principalDue(),
                        scheduledLoanInstallment.interestDue(), scheduledLoanInstallment.feeChargesDue(),
                        scheduledLoanInstallment.penaltyChargesDue());
                this.addRepaymentScheduleInstallment(installment);
            }
        }

        // if the loan application/contract is modified when repayments are
        // already against it - then need to re-process it
        final List<LoanTransaction> repaymentsOrWaivers = retreiveListOfTransactionsPostDisbursement();

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessor
                .determineProcessor(this.transactionProcessingStrategy);
        loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(), repaymentsOrWaivers, getCurrency(),
                this.repaymentScheduleInstallments, getNullPointerSafeLoanCharges());
    }

    public void updateLoanScheduleDependentDerivedFields() {
        this.expectedMaturityDate = determineExpectedMaturityDate().toDate();
    }

    public Map<String, Object> loanApplicationModification(final JsonCommand command, final LoanChargeCommand[] loanChargeCommands,
            final AprCalculator aprCalculator) {

        final Map<String, Object> actualChanges = this.loanRepaymentScheduleDetail.updateLoanApplicationAttributes(command, aprCalculator);
        if (!actualChanges.isEmpty()) {
            boolean recalculateLoanSchedule = !(actualChanges.size() == 1 && actualChanges.containsKey("inArrearsTolerance"));
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

        final String clientIdParamName = "clientId";
        if (command.isChangeInLongParameterNamed(clientIdParamName, this.client.getId())) {
            final Long newValue = command.longValueOfParameterNamed(clientIdParamName);
            actualChanges.put(clientIdParamName, newValue);
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

        final String strategyIdParamName = "transactionProcessingStrategyId";
        if (command.isChangeInLongParameterNamed(strategyIdParamName, this.transactionProcessingStrategy.getId())) {
            final Long newValue = command.longValueOfParameterNamed(strategyIdParamName);
            actualChanges.put(strategyIdParamName, newValue);
        }

        final String chargesParamName = "charges";
        if (command.parameterExists(chargesParamName)) {
            LoanChargeCommand[] existingLoanChargeData = getLoanCharges();
            if (differenceExistsBetween(existingLoanChargeData, loanChargeCommands)) {
                actualChanges.put(chargesParamName, loanChargeCommands);
                actualChanges.put("recalculateLoanSchedule", true);
            }
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
            this.expectedDisbursedOnDate = newValue.toDate();
            removeFirstDisbursementTransaction();
            disburse(getExpectedDisbursedOnLocalDate());
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

        if (getSubmittedOnDate().isAfter(getExpectedDisbursedOnLocalDate())) {
            final String errorMessage = "The date on which a loan is submitted cannot be after its expected disbursement date: "
                    + getExpectedDisbursedOnLocalDate().toString();
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.after.expected.disbursement.date", errorMessage,
                    getSubmittedOnDate(), getExpectedDisbursedOnLocalDate());
        }
        
        // FIXME - this constraint doesnt really need to be here. should be
        // possible to express loan term as say 12 months whilst also saying
        // - that the repayment structure is 6 repayments every bi-monthly.
//        validateSelectedPeriodFrequencyTypeIsTheSame(dataValidationErrors, loanTermFrequency, loanTermFrequencyType, numberOfRepayments,
//                repaymentEvery, repaymentEveryType);
//        
//        validateRepaymentsStartingFromDateIsAfterDisbursementDate(dataValidationErrors, expectedDisbursementDate,
//                repaymentsStartingFromDate);
//
//        validateRepaymentsStartingFromDateAndInterestChargedFromDate(dataValidationErrors, expectedDisbursementDate,
//                repaymentsStartingFromDate, interestChargedFromDate);

        // charges are optional
        for (LoanCharge loanCharge : getNullPointerSafeLoanCharges()) {
            validateChargeHasValidSpecifiedDateIfApplicable(loanCharge, getDisbursementDate(), getLastRepaymentPeriodDueDate());
        }

        return actualChanges;
    }

    private boolean differenceExistsBetween(final LoanChargeCommand[] existingLoanChargeData, final LoanChargeCommand[] newLoanChargeData) {
        Arrays.sort(existingLoanChargeData);
        Arrays.sort(newLoanChargeData);
        return !Arrays.equals(existingLoanChargeData, newLoanChargeData);
    }

    private Set<LoanCharge> getNullPointerSafeLoanCharges() {
        Set<LoanCharge> loanCharges = this.charges;
        if (this.charges == null) {
            loanCharges = new HashSet<LoanCharge>();
        }
        return loanCharges;
    }

    private LoanChargeCommand[] getLoanCharges() {

        LoanChargeCommand[] existingLoanCharges = null;

        if (!getNullPointerSafeLoanCharges().isEmpty()) {
            List<LoanChargeCommand> loanChargesList = new ArrayList<LoanChargeCommand>();
            for (LoanCharge loanCharge : getNullPointerSafeLoanCharges()) {
                loanChargesList.add(loanCharge.toCommand());
            }

            existingLoanCharges = loanChargesList.toArray(new LoanChargeCommand[loanChargesList.size()]);
        }

        return existingLoanCharges;
    }

    private void removeFirstDisbursementTransaction() {
        for (LoanTransaction loanTransaction : loanTransactions) {
            if (loanTransaction.isDisbursement()) {
                loanTransactions.remove(loanTransaction);
                break;
            }
        }
    }

    public void loanApplicationSubmittal(final LoanSchedule loanSchedule, final LoanLifecycleStateMachine lifecycleStateMachine,
            final LocalDate submittedOn, final String externalId) {

        final LoanScheduleData loanScheduleData = loanSchedule.generate();
        
        // Have to set expectedDisbursementDate to avoid nullPointer so should be passed down to updateLoanSchedule method
        if (loanSchedule.getDisbursementDate() != null) {
            this.expectedDisbursedOnDate = loanSchedule.getDisbursementDate().toDate();
        }        
        
        updateLoanSchedule(loanScheduleData);

        LoanStatus from = null;
        if (this.loanStatus != null) {
            from = LoanStatus.fromInt(this.loanStatus);
        }

        LoanStatus statusEnum = lifecycleStateMachine.transition(LoanEvent.LOAN_CREATED, from);
        this.loanStatus = statusEnum.getValue();

        this.externalId = externalId;
        
        this.termFrequency = loanSchedule.getLoanTermFrequency();
        this.termPeriodFrequencyType = loanSchedule.getLoanTermPeriodFrequencyType().getValue();

        this.submittedOnDate = submittedOn.toDate();
        this.expectedMaturityDate = determineExpectedMaturityDate().toDate();

        if (loanSchedule.getRepaymentStartFromDate() != null) {
            this.expectedFirstRepaymentOnDate = loanSchedule.getRepaymentStartFromDate().toDate();
        }

        if (loanSchedule.getInterestChargedFromDate() != null) {
            this.interestChargedFromDate = loanSchedule.getInterestChargedFromDate().toDate();
        }

        if (submittedOn.isAfter(new LocalDate())) {
            final String errorMessage = "The date on which a loan is submitted cannot be in the future.";
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.a.future.date", errorMessage, submittedOn);
        }

        if (submittedOn.isAfter(getExpectedDisbursedOnLocalDate())) {
            final String errorMessage = "The date on which a loan is submitted cannot be after its expected disbursement date: "
                    + getExpectedDisbursedOnLocalDate().toString();
            throw new InvalidLoanStateTransitionException("submittal", "cannot.be.after.expected.disbursement.date", errorMessage,
                    submittedOn, getExpectedDisbursedOnLocalDate());
        }

        // charges are optional
        for (LoanCharge loanCharge : getNullPointerSafeLoanCharges()) {
            validateChargeHasValidSpecifiedDateIfApplicable(loanCharge, getDisbursementDate(), getLastRepaymentPeriodDueDate());
        }
    }

    private LocalDate determineExpectedMaturityDate() {
        int numberOfInstallments = this.repaymentScheduleInstallments.size();
        return this.repaymentScheduleInstallments.get(numberOfInstallments - 1).getDueDate();
    }

    public Map<String, Object> loanApplicationRejection(final JsonCommand command, final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();

        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_REJECTED, LoanStatus.fromInt(this.loanStatus));
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
            this.loanStatus = statusEnum.getValue();
            actualChanges.put("status", LoanEnumerations.status(this.loanStatus));
            
            LocalDate rejectedOn = command.localDateValueOfParameterNamed("rejectedOnDate");
            if (rejectedOn == null) {
                rejectedOn = command.localDateValueOfParameterNamed("eventDate");
            }

            final Locale locale = new Locale(command.locale());
            final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);

            this.rejectedOnDate = rejectedOn.toDate();
            this.closedOnDate = rejectedOn.toDate();
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
            if (rejectedOn.isAfter(new LocalDate())) {
                final String errorMessage = "The date on which a loan is rejected cannot be in the future.";
                throw new InvalidLoanStateTransitionException("reject", "cannot.be.a.future.date", errorMessage, rejectedOn);
            }
        }

        return actualChanges;
    }

    public Map<String, Object> loanApplicationWithdrawnByApplicant(final JsonCommand command,
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
            this.closedOnDate = withdrawnOn.toDate();
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

            if (withdrawnOn.isAfter(new LocalDate())) {
                final String errorMessage = "The date on which a loan is withdrawn cannot be in the future.";
                throw new InvalidLoanStateTransitionException("reject", "cannot.be.a.future.date", errorMessage, command);
            }
        }

        return actualChanges;
    }

    public Map<String, Object> loanApplicationApproval(final JsonCommand command, final LoanLifecycleStateMachine loanLifecycleStateMachine) {

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
            actualChanges.put("locale", command.locale());
            actualChanges.put("dateFormat", command.dateFormat());
            actualChanges.put("approvedOnDate", approvedOnDateChange);

            final LocalDate submittalDate = new LocalDate(this.submittedOnDate);
            if (approvedOn.isBefore(submittalDate)) {
                final String errorMessage = "The date on which a loan is approved cannot be before its submittal date: "
                        + submittalDate.toString();
                throw new InvalidLoanStateTransitionException("approval", "cannot.be.before.submittal.date", errorMessage, getApprovedOnDate(),
                        submittalDate);
            }
            if (approvedOn.isAfter(new LocalDate())) {
                final String errorMessage = "The date on which a loan is approved cannot be in the future.";
                throw new InvalidLoanStateTransitionException("approval", "cannot.be.a.future.date", errorMessage, getApprovedOnDate());
            }

            if (this.loanofficer != null) {
                final LoanOfficerAssignmentHistory loanOfficerAssignmentHistory = LoanOfficerAssignmentHistory.createNew(this,
                        this.loanofficer, approvedOn);
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
            actualChanges.put("approvedOnDate", "");

            this.loanOfficerHistory.clear();
        }

        return actualChanges;
    }

    private void disburse(final LocalDate expectedDisbursedOnLocalDate) {
        this.disbursedOnDate = expectedDisbursedOnLocalDate.toDate();
        this.expectedMaturityDate = determineExpectedMaturityDate().toDate();
        handleDisbursementTransaction(expectedDisbursedOnLocalDate);
    }

    public Map<String, Object> disburse(final JsonCommand command, final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final ApplicationCurrency currency) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();

        updateLoanToPreDisbursalState();

        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_DISBURSED, LoanStatus.fromInt(this.loanStatus));
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
            this.loanStatus = statusEnum.getValue();
            actualChanges.put("status", LoanEnumerations.status(this.loanStatus));
            
            LocalDate disbursedOn = command.localDateValueOfParameterNamed("disbursedOnDate");
            if (disbursedOn == null) {
                disbursedOn = command.localDateValueOfParameterNamed("eventDate");
            }

            final Locale locale = new Locale(command.locale());
            final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);

            this.disbursedOnDate = disbursedOn.toDate();
            this.expectedMaturityDate = determineExpectedMaturityDate().toDate();

            actualChanges.put("locale", command.locale());
            actualChanges.put("dateFormat", command.dateFormat());
            actualChanges.put("disbursedOnDate", disbursedOn.toString(fmt));
            actualChanges.put("expectedMaturityDate", LocalDate.fromDateFields(this.expectedDisbursedOnDate).toString(fmt));

            handleDisbursementTransaction(disbursedOn);

            if (isRepaymentScheduleRegenerationRequiredForDisbursement(disbursedOn)) {
                regenerateRepaymentSchedule(currency);
            }
        }

        return actualChanges;
    }

    /*
     * Ability to regenerate the repayment schedule based on the loans current
     * details/state.
     */
    private void regenerateRepaymentSchedule(final ApplicationCurrency applicationCurrency) {

        final InterestMethod interestMethod = this.loanRepaymentScheduleDetail.getInterestMethod();
        final LoanScheduleGenerator loanScheduleGenerator = new DefaultLoanScheduleGeneratorFactory().create(interestMethod);

        final BigDecimal principal = this.loanRepaymentScheduleDetail.getPrincipal().getAmount();
        final BigDecimal inArrearsTolerance = this.loanRepaymentScheduleDetail.getInArrearsTolerance().getAmount();
        final BigDecimal interestRatePerPeriod = this.loanRepaymentScheduleDetail.getNominalInterestRatePerPeriod();
        final PeriodFrequencyType interestRatePeriodFrequencyType = this.loanRepaymentScheduleDetail.getInterestPeriodFrequencyType();
        final BigDecimal defaultAnnualNominalInterestRate = this.loanRepaymentScheduleDetail.getAnnualNominalInterestRate();

        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = this.loanRepaymentScheduleDetail
                .getInterestCalculationPeriodMethod();
        final Integer repaymentEvery = this.loanRepaymentScheduleDetail.getRepayEvery();
        final PeriodFrequencyType repaymentPeriodFrequencyType = this.loanRepaymentScheduleDetail.getRepaymentPeriodFrequencyType();
        final Integer numberOfRepayments = this.loanRepaymentScheduleDetail.getNumberOfRepayments();
        final AmortizationMethod amortizationMethod = this.loanRepaymentScheduleDetail.getAmortizationMethod();
        final Integer loanTermFrequency = this.termFrequency;
        final PeriodFrequencyType loanTermPeriodFrequencyType = PeriodFrequencyType.fromInt(this.termPeriodFrequencyType);

        final LoanSchedule loanSchedule = new LoanSchedule(loanScheduleGenerator, applicationCurrency, principal, interestRatePerPeriod,
                interestRatePeriodFrequencyType, defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod,
                repaymentEvery, repaymentPeriodFrequencyType, numberOfRepayments, amortizationMethod, loanTermFrequency,
                loanTermPeriodFrequencyType, getNullPointerSafeLoanCharges(), this.getDisbursementDate(),
                this.getExpectedFirstRepaymentOnDate(), this.getInterestChargedFromDate(), inArrearsTolerance);

        final LoanScheduleData generatedData = loanSchedule.generate();

        updateLoanSchedule(generatedData);
    }

    private void handleDisbursementTransaction(final LocalDate disbursedOn) {
        // track disbursement transaction
        final LoanTransaction loanTransaction = LoanTransaction.disbursement(this.loanRepaymentScheduleDetail.getPrincipal(), disbursedOn);
        loanTransaction.updateLoan(this);
        this.loanTransactions.add(loanTransaction);

        // add repayment transaction to track incoming money from client to mfi
        // for (charges due at time of disbursement)
        if (getTotalChargesDueAtDisbursement().isGreaterThanZero()) {

            LoanTransaction chargesPayment = LoanTransaction.repaymentAtDisbursement(getTotalChargesDueAtDisbursement(), disbursedOn);
            Money zero = Money.zero(getCurrency());
            chargesPayment.updateComponents(zero, zero, getTotalChargesDueAtDisbursement(), zero);
            chargesPayment.updateLoan(this);
            this.loanTransactions.add(chargesPayment);

            for (LoanCharge charge : getNullPointerSafeLoanCharges()) {
                if (charge.isDueAtDisbursement()) {
                    charge.markAsFullyPaid();
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
            final String errorMessage = "The date on which a loan is disbursed cannot be in the future.";
            throw new InvalidLoanStateTransitionException("disbursal", "cannot.be.a.future.date", errorMessage, disbursedOn);
        }

        LocalDate firstRepaymentDueDate = this.repaymentScheduleInstallments.get(0).getDueDate();
        if (disbursedOn.isAfter(firstRepaymentDueDate)) {
            final String errorMessage = "The date on which a loan is disbursed cannot be after the first expected repayment date: "
                    + firstRepaymentDueDate.toString();
            throw new InvalidLoanStateTransitionException("disbursal", "cannot.be.after.first.repayment.due.date", errorMessage,
                    disbursedOn, firstRepaymentDueDate);
        }
    }

    public Map<String, Object> undoDisbursal(final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();

        final LoanStatus currentStatus = LoanStatus.fromInt(this.loanStatus);
        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_DISBURSAL_UNDO, currentStatus);
        if (!statusEnum.hasStateOf(currentStatus)) {
            this.loanStatus = statusEnum.getValue();
            actualChanges.put("status", LoanEnumerations.status(this.loanStatus));
            
            this.approvedOnDate = null;
            actualChanges.put("disbursedOnDate", "");

            updateLoanToPreDisbursalState();
        }

        return actualChanges;
    }

    private void updateLoanToPreDisbursalState() {
        this.loanTransactions.clear();
        this.disbursedOnDate = null;

        for (LoanCharge charge : getNullPointerSafeLoanCharges()) {
            charge.resetToOriginal(loanCurrency());
        }

        for (LoanRepaymentScheduleInstallment currentInstallment : this.repaymentScheduleInstallments) {
            currentInstallment.resetDerivedComponents();
        }

        LoanScheduleWrapper wrapper = new LoanScheduleWrapper();
        wrapper.reprocess(getCurrency(), getDisbursementDate(), this.repaymentScheduleInstallments, getNullPointerSafeLoanCharges());
    }

    public LoanTransaction waiveInterest(final JsonCommand command, final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Money amountToWaive = Money.of(loanCurrency(), transactionAmount);
        final LoanTransaction waiveInterestTransaction = LoanTransaction.waiver(this, amountToWaive, transactionDate);

        handleRepaymentOrWaiverTransaction(waiveInterestTransaction, loanLifecycleStateMachine, null);

        return waiveInterestTransaction;
    }

    public LoanTransaction makeRepayment(final LocalDate transactionDate, final BigDecimal transactionAmount,
            final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        final Money repayment = Money.of(loanCurrency(), transactionAmount);
        final LoanTransaction loanTransaction = LoanTransaction.repayment(repayment, transactionDate);

        handleRepaymentOrWaiverTransaction(loanTransaction, loanLifecycleStateMachine, null);

        return loanTransaction;
    }

    private void handleRepaymentOrWaiverTransaction(final LoanTransaction loanTransaction,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final LoanTransaction adjustedTransaction) {

        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_REPAYMENT_OR_WAIVER,
                LoanStatus.fromInt(this.loanStatus));
        this.loanStatus = statusEnum.getValue();

        loanTransaction.updateLoan(this);

        boolean isTransactionChronologicallyLatest = isChronologicallyLatestRepaymentOrWaiver(loanTransaction, this.loanTransactions);

        if (loanTransaction.isNotZero(loanCurrency())) {
            this.loanTransactions.add(loanTransaction);
        }

        if (loanTransaction.isNotRepayment() && loanTransaction.isNotWaiver()) {
            final String errorMessage = "A transaction of type repayment or waiver was expected but not received.";
            throw new InvalidLoanTransactionTypeException("transaction", "is.not.a.repayment.or.waiver.transaction", errorMessage);
        }

        LocalDate loanTransactionDate = loanTransaction.getTransactionDate();
        if (loanTransactionDate.isBefore(this.getDisbursementDate())) {
            final String errorMessage = "The transaction date cannot be before the loan disbursement date: "
                    + getApprovedOnDate().toString();
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.before.disbursement.date", errorMessage,
                    loanTransactionDate, this.getDisbursementDate());
        }

        if (loanTransactionDate.isAfter(new LocalDate())) {
            final String errorMessage = "The transaction date cannot be in the future.";
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.a.future.date", errorMessage, loanTransactionDate);
        }

        if (loanTransaction.isInterestWaiver()) {
            Money totalInterestOutstandingOnLoan = getTotalInterestOutstandingOnLoan();
            if (adjustedTransaction != null) {
                totalInterestOutstandingOnLoan = totalInterestOutstandingOnLoan.plus(adjustedTransaction.getAmount());
            }
            if (loanTransaction.getAmount(loanCurrency()).isGreaterThan(totalInterestOutstandingOnLoan)) {
                final String errorMessage = "The amount of interest to waive cannot be greater than total interest outstanding on loan.";
                throw new InvalidLoanStateTransitionException("waive.interest", "amount.exceeds.total.outstanding.interest", errorMessage,
                        loanTransaction.getAmount(), totalInterestOutstandingOnLoan.getAmount());
            }
        }

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessor
                .determineProcessor(this.transactionProcessingStrategy);
        if (isTransactionChronologicallyLatest && adjustedTransaction == null) {
            loanRepaymentScheduleTransactionProcessor.handleTransaction(loanTransaction, getCurrency(), this.repaymentScheduleInstallments,
                    getNullPointerSafeLoanCharges());
        } else {
            final List<LoanTransaction> allNonContraTransactionsPostDisbursement = retreiveListOfTransactionsPostDisbursement();
            loanRepaymentScheduleTransactionProcessor.handleTransaction(getDisbursementDate(), allNonContraTransactionsPostDisbursement,
                    getCurrency(), this.repaymentScheduleInstallments, getNullPointerSafeLoanCharges());
        }

        doPostLoanTransactionChecks(loanTransaction.getTransactionDate(), loanLifecycleStateMachine);
    }

    private List<LoanTransaction> retreiveListOfTransactionsPostDisbursement() {
        List<LoanTransaction> repaymentsOrWaivers = new ArrayList<LoanTransaction>();
        for (LoanTransaction transaction : this.loanTransactions) {
            if (!transaction.isDisbursement() && transaction.isNotContra()) {
                repaymentsOrWaivers.add(transaction);
            }
        }
        LoanTransactionComparator transactionComparator = new LoanTransactionComparator();
        Collections.sort(repaymentsOrWaivers, transactionComparator);
        return repaymentsOrWaivers;
    }

    private void doPostLoanTransactionChecks(final LocalDate transactionDate, final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        if (this.isOverPaid()) {
            handleLoanOverpayment(loanLifecycleStateMachine);
        } else if (this.isRepaidInFull()) {
            // TODO - KW - probably should not close the loan automatically but
            // let user decide if loan is closed or not and provide closing
            // date.
            // - need to dig into loan closure scenarios with MFIs
            handleLoanRepaymentInFull(transactionDate, loanLifecycleStateMachine);
        }
    }

    private void handleLoanRepaymentInFull(final LocalDate transactionDate, final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.REPAID_IN_FULL, LoanStatus.fromInt(this.loanStatus));
        this.loanStatus = statusEnum.getValue();

        this.closedOnDate = transactionDate.toDate();
        this.maturedOnDate = transactionDate.toDate();
    }

    private void handleLoanOverpayment(final LoanLifecycleStateMachine loanLifecycleStateMachine) {

        LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.LOAN_OVERPAYMENT, LoanStatus.fromInt(this.loanStatus));
        this.loanStatus = statusEnum.getValue();

        this.closedOnDate = null;
        this.maturedOnDate = null;
    }

    private boolean isChronologicallyLatestRepaymentOrWaiver(final LoanTransaction loanTransaction,
            final List<LoanTransaction> loanTransactions) {

        boolean isChronologicallyLatestRepaymentOrWaiver = true;

        LocalDate currentTransactionDate = loanTransaction.getTransactionDate();
        for (LoanTransaction previousTransaction : loanTransactions) {
            if (!previousTransaction.isDisbursement() && previousTransaction.isNotContra()) {
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

        LocalDate currentTransactionDate = loanTransaction.getTransactionDate();
        for (LoanTransaction previousTransaction : loanTransactions) {
            if (previousTransaction.isNotContra()) {
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
        for (LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
            if (installment.isNotFullyCompleted()) {
                earliestUnpaidInstallmentDate = installment.getDueDate();
                break;
            }
        }

        LocalDate lastTransactionDate = null;
        for (LoanTransaction transaction : this.loanTransactions) {
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
        MonetaryCurrency currency = this.loanRepaymentScheduleDetail.getPrincipal().getCurrency();
        Money possibleNextRepaymentAmount = Money.zero(currency);

        for (LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
            if (installment.isNotFullyCompleted()) {
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
            for (LoanRepaymentScheduleInstallment scheduledRepayment : this.repaymentScheduleInstallments) {

                final Money outstandingForPeriod = scheduledRepayment.getInterestOutstanding(loanCurrency());
                if (scheduledRepayment.isOverdueOn(new LocalDate()) && scheduledRepayment.isNotFullyCompleted()
                        && outstandingForPeriod.isGreaterThanZero()) {
                    transactionDate = scheduledRepayment.getDueDate();
                    possibleInterestToWaive = outstandingForPeriod;
                    break;
                }
            }
        }

        return LoanTransaction.waiver(this, possibleInterestToWaive, transactionDate);
    }

    public LoanTransaction adjustExistingTransaction(final LocalDate transactionDate, final BigDecimal transactionAmountValue,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final LoanTransaction transactionForAdjustment) {

        final Money transactionAmount = Money.of(loanCurrency(), transactionAmountValue);
        LoanTransaction newTransactionDetail = LoanTransaction.repayment(transactionAmount, transactionDate);
        if (transactionForAdjustment.isInterestWaiver()) {
            newTransactionDetail = LoanTransaction.waiver(this, transactionAmount, transactionDate);
        }

        if (transactionForAdjustment.isNotRepayment() && transactionForAdjustment.isNotWaiver()) {
            final String errorMessage = "Only transactions of type repayment or waiver can be adjusted.";
            throw new InvalidLoanTransactionTypeException("transaction", "adjustment.is.only.allowed.to.repayment.or.waiver.transaction",
                    errorMessage);
        }

        if (isClosed()) {
            final String errorMessage = "Transactions of a closed loan cannot be adjusted.";
            throw new InvalidLoanTransactionTypeException("transaction", "adjustment.is.not.allowed.on.closed.loan", errorMessage);
        }

        transactionForAdjustment.contra();
        if (newTransactionDetail.isRepayment() || newTransactionDetail.isInterestWaiver()) {
            handleRepaymentOrWaiverTransaction(newTransactionDetail, loanLifecycleStateMachine, transactionForAdjustment);
        }

        return newTransactionDetail;
    }

    private boolean isRepaidInFull() {

        MonetaryCurrency currency = loanCurrency();

        Money cumulativeOriginalPrincipalExpected = Money.zero(currency);
        Money cumulativeOriginalInterestExpected = Money.zero(currency);
        Money cumulativeOriginalTotalExpected = Money.zero(currency);

        Money cumulativeTotalPaid = Money.zero(currency);
        Money cumulativeTotalWaived = Money.zero(currency);

        for (LoanRepaymentScheduleInstallment scheduledRepayment : this.repaymentScheduleInstallments) {
            cumulativeOriginalPrincipalExpected = cumulativeOriginalPrincipalExpected.plus(scheduledRepayment.getPrincipal(currency));
            cumulativeOriginalInterestExpected = cumulativeOriginalInterestExpected.plus(scheduledRepayment.getInterest(currency));

            cumulativeTotalPaid = cumulativeTotalPaid.plus(scheduledRepayment.getPrincipalCompleted(currency).plus(
                    scheduledRepayment.getInterestCompleted(currency)));
            cumulativeTotalWaived = cumulativeTotalWaived.plus(scheduledRepayment.getInterestWaived(currency));
        }

        cumulativeOriginalTotalExpected = cumulativeOriginalPrincipalExpected.plus(cumulativeOriginalInterestExpected);

        Money totalOutstanding = cumulativeOriginalTotalExpected.minus(cumulativeTotalPaid.plus(cumulativeTotalWaived));
        boolean isRepaidInFull = totalOutstanding.isZero();

        return isRepaidInFull;
    }

    private boolean isOverPaid() {
        return calculateTotalOverpayment().isGreaterThanZero();
    }

    private Money calculateTotalOverpayment() {

        final Money totalPaidInRepayments = getTotalPaidInRepayments();

        final MonetaryCurrency currency = loanCurrency();
        Money cumulativeTotalPaidOnInstallments = Money.zero(currency);
        Money cumulativeTotalWaivedOnInstallments = Money.zero(currency);

        for (LoanRepaymentScheduleInstallment scheduledRepayment : this.repaymentScheduleInstallments) {

            cumulativeTotalPaidOnInstallments = cumulativeTotalPaidOnInstallments
                    .plus(scheduledRepayment.getPrincipalCompleted(currency).plus(scheduledRepayment.getInterestCompleted(currency)))
                    .plus(scheduledRepayment.getFeeChargesCompleted(currency))
                    .plus(scheduledRepayment.getPenaltyChargesCompleted(currency));

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
            final Map<String, Object> changes) {

        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.WRITE_OFF_OUTSTANDING,
                LoanStatus.fromInt(this.loanStatus));
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
            this.loanStatus = statusEnum.getValue();
            changes.put("status", LoanEnumerations.status(this.loanStatus));
        }

        final LocalDate writtenOffOnLocalDate = command.localDateValueOfParameterNamed("transactionDate");

        this.closedOnDate = writtenOffOnLocalDate.toDate();
        this.writtenOffOnDate = writtenOffOnLocalDate.toDate();
        changes.put("closedOnDate", command.stringValueOfParameterNamed("transactionDate"));
        changes.put("writtenOffOnDate", command.stringValueOfParameterNamed("transactionDate"));

        if (writtenOffOnLocalDate.isBefore(this.getDisbursementDate())) {
            final String errorMessage = "The date on which a loan is written off cannot be before the loan disbursement date: "
                    + getDisbursementDate().toString();
            throw new InvalidLoanStateTransitionException("writeoff", "cannot.be.before.submittal.date", errorMessage,
                    writtenOffOnLocalDate, getDisbursementDate());
        }

        if (writtenOffOnLocalDate.isAfter(new LocalDate())) {
            final String errorMessage = "The date on which a loan is written off cannot be in the future.";
            throw new InvalidLoanStateTransitionException("writeoff", "cannot.be.a.future.date", errorMessage, writtenOffOnLocalDate);
        }

        final LoanTransaction loanTransaction = LoanTransaction.writeoff(this, writtenOffOnLocalDate);
        boolean isLastTransaction = isChronologicallyLatestTransaction(loanTransaction, loanTransactions);
        if (!isLastTransaction) {
            final String errorMessage = "The date of the writeoff transaction must occur on or before previous transactions.";
            throw new InvalidLoanStateTransitionException("writeoff", "must.occur.on.or.after.other.transaction.dates", errorMessage,
                    writtenOffOnLocalDate);
        }

        this.loanTransactions.add(loanTransaction);

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessor
                .determineProcessor(this.transactionProcessingStrategy);
        loanRepaymentScheduleTransactionProcessor.handleWriteOff(loanTransaction, loanCurrency(), this.repaymentScheduleInstallments);

        return loanTransaction;
    }

    public LoanTransaction close(final JsonCommand command, final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final Map<String, Object> changes) {

        final LocalDate closureDate = command.localDateValueOfParameterNamed("transactionDate");

        this.closedOnDate = closureDate.toDate();
        changes.put("closedOnDate", command.stringValueOfParameterNamed("transactionDate"));

        if (closureDate.isBefore(this.getDisbursementDate())) {
            final String errorMessage = "The date on which a loan is closed cannot be before the loan disbursement date: "
                    + getDisbursementDate().toString();
            throw new InvalidLoanStateTransitionException("close", "cannot.be.before.submittal.date", errorMessage, closureDate,
                    getDisbursementDate());
        }

        if (closureDate.isAfter(new LocalDate())) {
            final String errorMessage = "The date on which a loan is closed cannot be in the future.";
            throw new InvalidLoanStateTransitionException("close", "cannot.be.a.future.date", errorMessage, closureDate);
        }

        LoanTransaction loanTransaction = null;
        if (isOpen()) {
            // 1. check total outstanding
            final Money outstanding = getTotalOutstanding();
            if (outstanding.isGreaterThanZero() && getInArrearsTolerance().isGreaterThanOrEqualTo(outstanding)) {

                updateLoanForClosure(closureDate, loanLifecycleStateMachine, changes);

                loanTransaction = LoanTransaction.writeoff(this, closureDate);
                boolean isLastTransaction = isChronologicallyLatestTransaction(loanTransaction, loanTransactions);
                if (!isLastTransaction) {
                    final String errorMessage = "The closing date of the loan must be on or after latest transaction date.";
                    throw new InvalidLoanStateTransitionException("close.loan", "must.occur.on.or.after.latest.transaction.date",
                            errorMessage, closureDate);
                }

                this.loanTransactions.add(loanTransaction);

                final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.transactionProcessor
                        .determineProcessor(this.transactionProcessingStrategy);
                loanRepaymentScheduleTransactionProcessor.handleWriteOff(loanTransaction, loanCurrency(),
                        this.repaymentScheduleInstallments);

            } else if (outstanding.isGreaterThanZero()) {
                final String errorMessage = "A loan with money outstanding cannot be closed";
                throw new InvalidLoanStateTransitionException("close", "loan.has.money.outstanding", errorMessage, outstanding.toString());
            }
        }

        if (isOverPaid()) {
            final Money totalLoanOverpayment = calculateTotalOverpayment();
            // FIXME - kw - use overpaymentTolerance setting when in place on
            // loan product settings.
            if (totalLoanOverpayment.isGreaterThanZero() && getInArrearsTolerance().isGreaterThanOrEqualTo(totalLoanOverpayment)) {
                // TODO - technically should set somewhere that this loan has
                // 'overpaid' amount
                updateLoanForClosure(closureDate, loanLifecycleStateMachine, changes);
            } else if (totalLoanOverpayment.isGreaterThanZero()) {
                final String errorMessage = "The loan is marked as 'Overpaid' and cannot be moved to 'Closed (obligations met).";
                throw new InvalidLoanStateTransitionException("close", "loan.is.overpaid", errorMessage, totalLoanOverpayment.toString());
            }
        }

        return loanTransaction;
    }

    private Money getTotalOutstanding() {
        Money totalOutstanding = Money.zero(loanCurrency());
        // 1. check totalOutstanding value on loan

        // 2. if null calculate from loan schedule
        final LoanScheduleWrapper wrapper = new LoanScheduleWrapper();
        totalOutstanding = wrapper.calculateTotalOutstanding(loanCurrency(), this.repaymentScheduleInstallments);
        return totalOutstanding;
    }

    private void updateLoanForClosure(final LocalDate closureDate, final LoanLifecycleStateMachine loanLifecycleStateMachine,
            final Map<String, Object> changes) {

        final LoanStatus statusEnum = loanLifecycleStateMachine.transition(LoanEvent.REPAID_IN_FULL, LoanStatus.fromInt(this.loanStatus));
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(this.loanStatus))) {
            this.loanStatus = statusEnum.getValue();
            changes.put("status", LoanEnumerations.status(this.loanStatus));
        }
        this.closedOnDate = closureDate.toDate();
    }

    /**
     * Behaviour added to comply with capability of previous mifos product to
     * support easier transition to mifosx platform.
     * 
     * @param changes
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

        LocalDate rescheduledOnLocalDate = new LocalDate(rescheduledOnDate);
        if (rescheduledOnLocalDate.isBefore(this.getDisbursementDate())) {
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

    private LoanStatus status() {
        return LoanStatus.fromInt(this.loanStatus);
    }

    public boolean isSubmittedAndPendingApproval() {
        return status().isSubmittedAndPendingApproval();
    }

    public boolean isApproved() {
        return status().isApproved();
    }

    public boolean isNotApproved() {
        return !isApproved();
    }

    public boolean isWaitingForDisbursal() {
        return this.isApproved() && this.isNotDisbursed();
    }

    public boolean isNotDisbursed() {
        return !this.isDisbursed();
    }

    public boolean isDisbursed() {
        return hasDisbursementTransaction();
    }

    public boolean isUndoDisbursalAllowed() {
        return isDisbursed() && this.hasNoRepaymentTransaction();
    }

    public boolean isClosed() {
        return status().isClosed() || this.isCancelled();
    }

    public boolean isCancelled() {
        return this.isRejected() || this.isWithdrawn();
    }

    public boolean isWithdrawn() {
        return status().isWithdrawnByClient();
    }

    public boolean isRejected() {
        return status().isRejected();
    }

    public boolean isNotClosed() {
        return !this.isClosed();
    }

    public boolean isOpen() {
        return status().isActive();
    }

    public boolean isOpenWithNoRepaymentMade() {
        return this.isOpen() && hasNoRepaymentTransaction();
    }

    private boolean hasNoRepaymentTransaction() {
        return !hasRepaymentTransaction();
    }

    private boolean hasRepaymentTransaction() {
        boolean hasRepaymentTransaction = false;
        for (LoanTransaction loanTransaction : this.loanTransactions) {
            if (loanTransaction.isRepayment()) {
                hasRepaymentTransaction = true;
                break;
            }
        }
        return hasRepaymentTransaction;
    }

    private boolean hasDisbursementTransaction() {
        boolean hasRepaymentTransaction = false;
        for (LoanTransaction loanTransaction : this.loanTransactions) {
            if (loanTransaction.isDisbursement()) {
                hasRepaymentTransaction = true;
                break;
            }
        }
        return hasRepaymentTransaction;
    }

    public boolean isOpenWithRepaymentMade() {
        return this.isOpen() && this.hasRepaymentTransaction();
    }

    public List<LoanRepaymentScheduleInstallment> getRepaymentScheduleInstallments() {
        return this.repaymentScheduleInstallments;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public void setExternalId(final String externalSystemIdentifer) {
        if (StringUtils.isNotBlank(externalSystemIdentifer)) {
            this.externalId = externalSystemIdentifer.trim();
        } else {
            this.externalId = null;
        }
    }

    public LocalDate getSubmittedOnDate() {
        return (LocalDate) ObjectUtils.defaultIfNull(new LocalDate(this.submittedOnDate), null);
    }

    public LocalDate getRejectedOnDate() {
        return (LocalDate) ObjectUtils.defaultIfNull(new LocalDate(this.rejectedOnDate), null);
    }

    public LocalDate getApprovedOnDate() {
        LocalDate date = null;
        if (this.approvedOnDate != null) {
            date = new LocalDate(this.approvedOnDate);
        }
        return date;
    }

    public LocalDate getDisbursedOnDate() {
        LocalDate date = null;
        if (this.disbursedOnDate != null) {
            date = new LocalDate(this.disbursedOnDate);
        }
        return date;
    }

    public LocalDate getClosedOnDate() {
        LocalDate date = null;
        if (this.closedOnDate != null) {
            date = new LocalDate(this.closedOnDate);
        }
        return date;
    }

    public LocalDate getWrittenOffOnDate() {
        LocalDate date = null;
        if (this.writtenOffOnDate != null) {
            date = new LocalDate(this.writtenOffOnDate);
        }
        return date;
    }

    public Date getExpectedDisbursedOnDate() {
        return this.expectedDisbursedOnDate;
    }

    public LocalDate getExpectedDisbursedOnLocalDate() {
        LocalDate expectedDisbursementDate = null;
        if (this.expectedDisbursedOnDate != null) {
            expectedDisbursementDate = new LocalDate(this.expectedDisbursedOnDate);
        }
        return expectedDisbursementDate;
    }

    public LocalDate getExpectedFirstRepaymentOnDate() {
        LocalDate firstRepaymentDate = null;
        if (this.expectedFirstRepaymentOnDate != null) {
            firstRepaymentDate = new LocalDate(this.expectedFirstRepaymentOnDate);
        }
        return firstRepaymentDate;
    }

    public LocalDate getDisbursementDate() {
        LocalDate disbursementDate = getExpectedDisbursedOnLocalDate();
        if (this.disbursedOnDate != null) {
            disbursementDate = new LocalDate(this.disbursedOnDate);
        }
        return disbursementDate;
    }

    public LocalDate getExpectedMaturityDate() {
        LocalDate possibleMaturityDate = null;
        if (this.expectedMaturityDate != null) {
            possibleMaturityDate = new LocalDate(this.expectedMaturityDate);
        }
        return possibleMaturityDate;
    }

    public LocalDate getActualMaturityDate() {
        LocalDate possibleMaturityDate = null;
        if (this.maturedOnDate != null) {
            possibleMaturityDate = new LocalDate(this.maturedOnDate);
        }
        return possibleMaturityDate;
    }

    public LocalDate getMaturityDate() {
        LocalDate possibleMaturityDate = null;

        if (this.expectedMaturityDate != null) {
            possibleMaturityDate = new LocalDate(this.expectedMaturityDate);
        }
        if (this.maturedOnDate != null) {
            possibleMaturityDate = new LocalDate(this.maturedOnDate);
        }
        return possibleMaturityDate;
    }

    public Set<LoanCharge> getCharges() {
        return charges;
    }

    public void setCharges(Set<LoanCharge> charges) {
        this.charges = charges;
    }

    public void addRepaymentScheduleInstallment(final LoanRepaymentScheduleInstallment installment) {
        installment.updateLoan(this);
        this.repaymentScheduleInstallments.add(installment);
    }

    public boolean isActualDisbursedOnDateEarlierOrLaterThanExpected(final LocalDate actualDisbursedOnDate) {
        return !new LocalDate(this.expectedDisbursedOnDate).isEqual(actualDisbursedOnDate);
    }

    public boolean isRepaymentScheduleRegenerationRequiredForDisbursement(final LocalDate actualDisbursementDate) {
        return isActualDisbursedOnDateEarlierOrLaterThanExpected(actualDisbursementDate);
    }

    private Money getTotalPaidInRepayments() {
        Money cumulativePaid = Money.zero(this.loanRepaymentScheduleDetail.getPrincipal().getCurrency());

        for (LoanTransaction repayment : this.loanTransactions) {
            if (repayment.isRepayment()) {
                cumulativePaid = cumulativePaid.plus(repayment.getAmount());
            }
        }

        return cumulativePaid;
    }

    private Money getTotalInterestOutstandingOnLoan() {
        Money cumulativeInterest = Money.zero(loanCurrency());

        for (LoanRepaymentScheduleInstallment scheduledRepayment : this.repaymentScheduleInstallments) {
            cumulativeInterest = cumulativeInterest.plus(scheduledRepayment.getInterestOutstanding(loanCurrency()));
        }

        return cumulativeInterest;
    }

    @SuppressWarnings("unused")
    private Money getTotalInterestOverdueOnLoan() {
        Money cumulativeInterestOverdue = Money.zero(this.loanRepaymentScheduleDetail.getPrincipal().getCurrency());

        for (LoanRepaymentScheduleInstallment scheduledRepayment : this.repaymentScheduleInstallments) {

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

    public boolean identifiedBy(String identifier) {
        return identifier.equalsIgnoreCase(this.externalId) || identifier.equalsIgnoreCase(this.getId().toString());
    }

    public boolean hasIdentifyOf(final Long loanId) {
        return loanId.equals(this.getId());
    }

    public boolean hasLoanOfficer(final Staff fromLoanOfficer) {

        boolean matchesCurrentLoanOfficer = false;
        if (this.loanofficer != null) {
            matchesCurrentLoanOfficer = this.loanofficer.identifiedBy(fromLoanOfficer);
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

    public LocalDate getLoanStatusSinceDate() {

        LocalDate statusSinceDate = getSubmittedOnDate();
        if (isApproved()) {
            statusSinceDate = new LocalDate(this.approvedOnDate);
        }

        if (isDisbursed()) {
            statusSinceDate = new LocalDate(this.disbursedOnDate);
        }

        if (isClosed()) {
            statusSinceDate = new LocalDate(this.closedOnDate);
        }

        return statusSinceDate;
    }

    public Money getPrincpal() {
        return this.loanRepaymentScheduleDetail.getPrincipal();
    }

    public Money getTotalChargesDueAtDisbursement() {
        return Money.of(getCurrency(), this.totalChargesDueAtDisbursement);
    }

    public boolean hasCurrencyCodeOf(final String matchingCurrencyCode) {
        return getCurrencyCode().equalsIgnoreCase(matchingCurrencyCode);
    }

    private String getCurrencyCode() {
        return this.loanRepaymentScheduleDetail.getPrincipal().getCurrencyCode();
    }

    public MonetaryCurrency getCurrency() {
        return this.loanRepaymentScheduleDetail.getCurrency();
    }

    public void reassignLoanOfficer(final Staff newLoanOfficer, final LocalDate assignmentDate) {

        final LoanOfficerAssignmentHistory latestHistoryRecord = findLatestIncompleteHistoryRecord();

        if (latestHistoryRecord != null && this.loanofficer.identifiedBy(newLoanOfficer)) {
            latestHistoryRecord.updateStartDate(assignmentDate);
        } else if (latestHistoryRecord != null && latestHistoryRecord.matchesStartDateOf(assignmentDate)) {
            latestHistoryRecord.updateLoanOfficer(newLoanOfficer);
            this.loanofficer = newLoanOfficer;
        } else if (latestHistoryRecord != null && latestHistoryRecord.hasStartDateBefore(assignmentDate)) {
            throw new LoanOfficerAssignmentException(this.getId(), assignmentDate);
        } else {
            if (latestHistoryRecord != null) {
                // loan officer correctly changed from previous loan officer to
                // new loan officer
                latestHistoryRecord.updateEndDate(assignmentDate);
            }

            this.loanofficer = newLoanOfficer;
            if (this.isNotSubmittedAndPendingApproval()) {
                final LoanOfficerAssignmentHistory loanOfficerAssignmentHistory = LoanOfficerAssignmentHistory.createNew(this,
                        this.loanofficer, assignmentDate);
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

        this.loanofficer = null;
    }

    private void validateUnassignDate(final LoanOfficerAssignmentHistory latestHistoryRecord, final LocalDate unassignDate) {

        final LocalDate today = DateUtils.getLocalDateOfTenant();

        if (latestHistoryRecord.getStartDate().isAfter(unassignDate)) {
            throw new LoanOfficerUnassignmentDateException(this.getId(), this.getLoanOfficer().getId(), latestHistoryRecord.getStartDate(),
                    unassignDate);
        } else if (unassignDate.isAfter(today)) { throw new LoanOfficerUnassignmentDateException(
                this.getId()); }
    }

    private LoanOfficerAssignmentHistory findLatestIncompleteHistoryRecord() {

        LoanOfficerAssignmentHistory latestRecordWithNoEndDate = null;
        for (LoanOfficerAssignmentHistory historyRecord : this.loanOfficerHistory) {
            if (historyRecord.isCurrentRecord()) {
                latestRecordWithNoEndDate = historyRecord;
            }
        }
        return latestRecordWithNoEndDate;
    }

    public Client getClient() {
        return client;
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
            officeId = this.client.getOffice().getId();
        } else {
            // officeId = this.group.getOffice().getId();
        }
        return officeId;
    }

    public void updateAccountNo(final String newAccountNo) {
        this.accountNumber = newAccountNo;
        this.accountNumberRequiresAutoGeneration=false;
    }
    
    public List<LoanTransaction> getLoanTransactions() {
        return this.loanTransactions;
    }

    public boolean isAccountingEnabledOnLoanProduct() {
        return this.loanProduct.isAccountingEnabled();
    }
    
    public boolean isCashBasedAccountingEnabledOnLoanProduct(){
        return this.loanProduct.isCashBasedAccountingEnabled();
    }
    
    public boolean isAccrualBasedAccountingEnabledOnLoanProduct(){
        return this.loanProduct.isAccrualBasedAccountingEnabled();
    }

    public Long productId() {
        return this.loanProduct.getId();
    }
    
    public Staff getLoanOfficer() {
        return this.loanofficer;
    }

}