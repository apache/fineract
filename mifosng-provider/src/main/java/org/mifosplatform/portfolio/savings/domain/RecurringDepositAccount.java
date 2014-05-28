/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.depositPeriodParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.maxDepositTermParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.onAccountClosureIdParamName;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.accountdetails.domain.AccountType;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.interestratechart.data.ClientIncentiveAttributes;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChart;
import org.mifosplatform.portfolio.interestratechart.service.InterestRateChartAssembler;
import org.mifosplatform.portfolio.savings.DepositAccountOnClosureType;
import org.mifosplatform.portfolio.savings.DepositsApiConstants;
import org.mifosplatform.portfolio.savings.PreClosurePenalInterestOnType;
import org.mifosplatform.portfolio.savings.SavingsApiConstants;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.mifosplatform.portfolio.savings.domain.interest.PostingPeriod;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@DiscriminatorValue("300")
public class RecurringDepositAccount extends SavingsAccount {

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private DepositAccountTermAndPreClosure accountTermAndPreClosure;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private DepositAccountRecurringDetail recurringDetail;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "account")
    private DepositAccountInterestRateChart chart;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "account", orphanRemoval = true)
    private List<RecurringDepositScheduleInstallment> depositScheduleInstallments = new ArrayList<RecurringDepositScheduleInstallment>();

    @Transient
    private InterestRateChartAssembler chartAssembler;

    protected RecurringDepositAccount() {
        //
    }

    public static RecurringDepositAccount createNewApplicationForSubmittal(final Client client, final Group group,
            final SavingsProduct product, final Staff fieldOfficer, final String accountNo, final String externalId,
            final AccountType accountType, final LocalDate submittedOnDate, final AppUser submittedBy, final BigDecimal interestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final Set<SavingsAccountCharge> savingsAccountCharges,
            final DepositAccountTermAndPreClosure accountTermAndPreClosure, final DepositAccountRecurringDetail recurringDetail,
            final DepositAccountInterestRateChart chart) {

        final boolean allowOverdraft = false;
        final BigDecimal overdraftLimit = new BigDecimal(0);

        final SavingsAccountStatusType status = SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL;
        return new RecurringDepositAccount(client, group, product, fieldOfficer, accountNo, externalId, status, accountType,
                submittedOnDate, submittedBy, interestRate, interestCompoundingPeriodType, interestPostingPeriodType,
                interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType, withdrawalFeeApplicableForTransfer, savingsAccountCharges, accountTermAndPreClosure,
                recurringDetail, chart, allowOverdraft, overdraftLimit);
    }

    public static RecurringDepositAccount createNewActivatedAccount(final Client client, final Group group, final SavingsProduct product,
            final Staff fieldOfficer, final String accountNo, final String externalId, final AccountType accountType,
            final LocalDate submittedOnDate, final AppUser submittedBy, final BigDecimal interestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final Set<SavingsAccountCharge> savingsAccountCharges,
            final DepositAccountTermAndPreClosure accountTermAndPreClosure, final DepositAccountRecurringDetail recurringDetail,
            final DepositAccountInterestRateChart chart) {

        final boolean allowOverdraft = false;
        final BigDecimal overdraftLimit = new BigDecimal(0);

        final SavingsAccountStatusType status = SavingsAccountStatusType.ACTIVE;
        return new RecurringDepositAccount(client, group, product, fieldOfficer, accountNo, externalId, status, accountType,
                submittedOnDate, submittedBy, interestRate, interestCompoundingPeriodType, interestPostingPeriodType,
                interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType, withdrawalFeeApplicableForTransfer, savingsAccountCharges, accountTermAndPreClosure,
                recurringDetail, chart, allowOverdraft, overdraftLimit);
    }

    private RecurringDepositAccount(final Client client, final Group group, final SavingsProduct product, final Staff fieldOfficer,
            final String accountNo, final String externalId, final SavingsAccountStatusType status, final AccountType accountType,
            final LocalDate submittedOnDate, final AppUser submittedBy, final BigDecimal nominalAnnualInterestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final Set<SavingsAccountCharge> savingsAccountCharges,
            final DepositAccountTermAndPreClosure accountTermAndPreClosure, final DepositAccountRecurringDetail recurringDetail,
            final DepositAccountInterestRateChart chart, final boolean allowOverdraft, final BigDecimal overdraftLimit) {

        super(client, group, product, fieldOfficer, accountNo, externalId, status, accountType, submittedOnDate, submittedBy,
                nominalAnnualInterestRate, interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType,
                interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType,
                withdrawalFeeApplicableForTransfer, savingsAccountCharges, allowOverdraft, overdraftLimit);

        this.accountTermAndPreClosure = accountTermAndPreClosure;
        this.recurringDetail = recurringDetail;
        this.chart = chart;
        if (this.chart != null) {
            this.chart.updateDepositAccountReference(this);
        }
    }

    @Override
    public void modifyApplication(final JsonCommand command, final Map<String, Object> actualChanges) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.modifyApplicationAction);
        super.modifyApplication(command, actualChanges, baseDataValidator);
        final Map<String, Object> termAndPreClosureChanges = accountTermAndPreClosure.update(command, baseDataValidator);
        actualChanges.putAll(termAndPreClosureChanges);
        recurringDetail.update(command);

        validateDomainRules(baseDataValidator);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    @Override
    protected BigDecimal getEffectiveInterestRateAsFraction(final MathContext mc, final LocalDate interestPostingUpToDate) {

        final boolean isPreMatureClosure = SavingsAccountStatusType.fromInt(this.status).isPreMatureClosure();
        boolean applyPreMaturePenalty = false;
        BigDecimal penalInterest = BigDecimal.ZERO;
        LocalDate depositCloseDate = calculateMaturityDate();
        if (isPreMatureClosure) {
            if (this.accountTermAndPreClosure.isPreClosurePenalApplicable()) {
                applyPreMaturePenalty = true;
                penalInterest = this.accountTermAndPreClosure.depositPreClosureDetail().preClosurePenalInterest();
                final PreClosurePenalInterestOnType preClosurePenalInterestOnType = this.accountTermAndPreClosure.depositPreClosureDetail()
                        .preClosurePenalInterestOnType();
                if (preClosurePenalInterestOnType.isWholeTerm()) {
                    depositCloseDate = interestCalculatedUpto();
                } else if (preClosurePenalInterestOnType.isTillPrematureWithdrawal()) {
                    depositCloseDate = interestPostingUpToDate;
                }
            }
        }

        final BigDecimal depositAmount = accountTermAndPreClosure.depositAmount();
        final ClientIncentiveAttributes incentiveAttributes = (this.client == null) ? null : this.client.incentiveAttributes();
        BigDecimal applicableInterestRate = this.chart.getApplicableInterestRate(depositAmount, depositStartDate(), depositCloseDate,
                incentiveAttributes);

        if (applyPreMaturePenalty) {
            applicableInterestRate = applicableInterestRate.subtract(penalInterest);
            applicableInterestRate = applicableInterestRate.compareTo(BigDecimal.ZERO) == -1 ? BigDecimal.ZERO : applicableInterestRate;
        }

        this.nominalAnnualInterestRate = applicableInterestRate;

        return applicableInterestRate.divide(BigDecimal.valueOf(100l), mc);
    }

    public void updateMaturityDateAndAmount(final MathContext mc) {
        final LocalDate maturityDate = calculateMaturityDate();
        final LocalDate interestCalculationUpto = maturityDate.minusDays(1);
        final List<SavingsAccountTransaction> allTransactions = generateIncludingFutureTransactions(interestCalculationUpto);

        final List<PostingPeriod> postingPeriods = calculateInterestPayable(mc, interestCalculationUpto, allTransactions);
        Money totalInterestPayable = Money.zero(getCurrency());
        Money totalDepositAmount = Money.zero(getCurrency());
        for (PostingPeriod postingPeriod : postingPeriods) {
            totalInterestPayable = totalInterestPayable.plus(postingPeriod.getInterestEarned());
            totalDepositAmount = totalDepositAmount.plus(postingPeriod.closingBalance()).minus(postingPeriod.openingBalance());
        }

        this.accountTermAndPreClosure.updateMaturityDetails(totalDepositAmount.getAmount(), totalInterestPayable.getAmount(), maturityDate);
    }

    public void updateMaturityStatus() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.updateMaturityDetailsAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.in.active.state");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        final LocalDate todayDate = DateUtils.getLocalDateOfTenant();
        if (!this.maturityDate().isAfter(todayDate)) {
            // update account status
            this.status = SavingsAccountStatusType.MATURED.getValue();
            postMaturityInterest();
        }
    }

    public LocalDate calculateMaturityDate() {

        final LocalDate startDate = depositStartDate();
        LocalDate maturityDate = null;
        final Integer depositPeriod = this.accountTermAndPreClosure.depositPeriod();
        switch (this.accountTermAndPreClosure.depositPeriodFrequencyType()) {
            case DAYS:
                maturityDate = startDate.plusDays(depositPeriod);
            break;
            case WEEKS:
                maturityDate = startDate.plusWeeks(depositPeriod);
            break;
            case MONTHS:
                maturityDate = startDate.plusMonths(depositPeriod);
            break;
            case YEARS:
                maturityDate = startDate.plusYears(depositPeriod);
            break;
            case INVALID:
            break;
        }

        return maturityDate;
    }

    private List<PostingPeriod> calculateInterestPayable(final MathContext mc, final LocalDate maturityDate,
            final List<SavingsAccountTransaction> transactions) {

        // 1. default to calculate interest based on entire history OR
        // 2. determine latest 'posting period' and find interest credited to
        // that period

        // A generate list of EndOfDayBalances (not including interest postings)
        final SavingsPostingInterestPeriodType postingPeriodType = SavingsPostingInterestPeriodType.fromInt(this.interestPostingPeriodType);

        final SavingsCompoundingInterestPeriodType compoundingPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(this.interestCompoundingPeriodType);

        final SavingsInterestCalculationDaysInYearType daysInYearType = SavingsInterestCalculationDaysInYearType
                .fromInt(this.interestCalculationDaysInYearType);

        final List<LocalDateInterval> postingPeriodIntervals = this.savingsHelper.determineInterestPostingPeriods(depositStartDate(),
                maturityDate, postingPeriodType);

        final List<PostingPeriod> allPostingPeriods = new ArrayList<PostingPeriod>();

        Money periodStartingBalance = Money.zero(currency);

        final SavingsInterestCalculationType interestCalculationType = SavingsInterestCalculationType.fromInt(this.interestCalculationType);
        final BigDecimal interestRateAsFraction = getEffectiveInterestRateAsFraction(mc, maturityDate);
        final Collection<Long> interestPostTransactions = this.savingsHelper.fetchPostInterestTransactionIds(getId());
        boolean isInterestTransfer = false;
        for (final LocalDateInterval periodInterval : postingPeriodIntervals) {
            final PostingPeriod postingPeriod = PostingPeriod.createFrom(periodInterval, periodStartingBalance, transactions,
                    this.currency, compoundingPeriodType, interestCalculationType, interestRateAsFraction, daysInYearType.getValue(),
                    maturityDate, interestPostTransactions, isInterestTransfer);

            periodStartingBalance = postingPeriod.closingBalance();

            allPostingPeriods.add(postingPeriod);
        }

        this.savingsHelper.calculateInterestForAllPostingPeriods(this.currency, allPostingPeriods, this.getLockedInUntilLocalDate(),
                isTransferInterestToOtherAccount());
        // this.summary.updateFromInterestPeriodSummaries(this.currency,
        // allPostingPeriods);
        return allPostingPeriods;
    }

    private List<SavingsAccountTransaction> generateIncludingFutureTransactions(final LocalDate depositEndDate) {
        List<SavingsAccountTransaction> allTransactions = new ArrayList<SavingsAccountTransaction>();
        // add existing transactions
        allTransactions.addAll(retreiveOrderedNonInterestPostingTransactions());
        for (RecurringDepositScheduleInstallment installment : depositScheduleInstallments()) {
            if (installment.isPrincipalNotCompleted(getCurrency())) {
                final SavingsAccountTransaction transaction = SavingsAccountTransaction.deposit(null, office(), null,
                        installment.dueDate(), installment.getDepositAmountOutstanding(getCurrency()), installment.dueDate().toDate());
                allTransactions.add(transaction);
            }
        }

        allTransactions = sortTransactions(allTransactions);
        Money runningBalance = Money.zero(getCurrency());
        for (final SavingsAccountTransaction transaction : allTransactions) {
            if (transaction.isReversed()) {
                transaction.zeroBalanceFields();
            } else {

                Money transactionAmount = Money.zero(this.currency);
                if (transaction.isCredit()) {
                    transactionAmount = transactionAmount.plus(transaction.getAmount(this.currency));
                } else if (transaction.isDebit()) {
                    transactionAmount = transactionAmount.minus(transaction.getAmount(this.currency));
                }

                runningBalance = runningBalance.plus(transactionAmount);
                transaction.updateRunningBalance(runningBalance);
            }
        }
        // loop over transactions in reverse
        LocalDate endOfBalanceDate = depositEndDate;
        for (int i = allTransactions.size() - 1; i >= 0; i--) {
            final SavingsAccountTransaction transaction = allTransactions.get(i);
            if (transaction.isNotReversed() && !transaction.isInterestPostingAndNotReversed()) {
                transaction.updateCumulativeBalanceAndDates(this.currency, endOfBalanceDate);
                endOfBalanceDate = transaction.transactionLocalDate().minusDays(1);
            }
        }
        return allTransactions;
    }

    private LocalDate calculateNextDepositDate(final LocalDate lastDepositDate, final PeriodFrequencyType frequency,
            final int recurringEvery) {
        LocalDate nextDepositDate = lastDepositDate;

        switch (frequency) {
            case DAYS:
                nextDepositDate = lastDepositDate.plusDays(recurringEvery);
            break;
            case WEEKS:
                nextDepositDate = lastDepositDate.plusWeeks(recurringEvery);
            break;
            case MONTHS:
                nextDepositDate = lastDepositDate.plusMonths(recurringEvery);
            break;
            case YEARS:
                nextDepositDate = lastDepositDate.plusYears(recurringEvery);
            break;
            case INVALID:
            break;
        }
        return nextDepositDate;
    }

    public LocalDate depositStartDate() {
        final LocalDate depositStartDate = accountTermAndPreClosure.getExpectedFirstDepositOnDate();
        if (depositStartDate == null) return accountSubmittedOrActivationDate();
        return depositStartDate;
    }

    public void prematureClosure(final AppUser currentUser, final JsonCommand command, final LocalDate tenantsTodayDate,
            final Map<String, Object> actualChanges) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME + DepositsApiConstants.preMatureCloseAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.in.active.state");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        final LocalDate closedDate = command.localDateValueOfParameterNamed(SavingsApiConstants.closedOnDateParamName);

        if (closedDate.isBefore(getActivationLocalDate())) {
            baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                    .failWithCode("must.be.after.activation.date");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (isAccountLocked(closedDate)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                    .failWithCode("must.be.after.lockin.period");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (closedDate.isAfter(maturityDate())) {
            baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                    .failWithCode("must.be.before.maturity.date");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (closedDate.isAfter(tenantsTodayDate)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                    .failWithCode("cannot.be.a.future.date");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (isAccountLocked(calculateMaturityDate())) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("deposit.period.must.be.greater.than.lock.in.period",
                    "Deposit period must be greater than account lock-in period.");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        final List<SavingsAccountTransaction> savingsAccountTransactions = retreiveListOfTransactions();
        if (savingsAccountTransactions.size() > 0) {
            final SavingsAccountTransaction accountTransaction = savingsAccountTransactions.get(savingsAccountTransactions.size() - 1);
            if (accountTransaction.isAfter(closedDate)) {
                baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                        .failWithCode("must.be.after.last.transaction.date");
                if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
            }
        }

        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_CLOSE_ACCOUNT, closedDate);
        this.status = SavingsAccountStatusType.PRE_MATURE_CLOSURE.getValue();

        final Integer onAccountClosureId = command.integerValueOfParameterNamed(onAccountClosureIdParamName);
        final DepositAccountOnClosureType onClosureType = DepositAccountOnClosureType.fromInt(onAccountClosureId);
        this.accountTermAndPreClosure.updateOnAccountClosureStatus(onClosureType);

        /*
         * // withdraw deposit amount before closing the account final Money
         * transactionAmountMoney = Money.of(this.currency,
         * this.getAccountBalance()); final SavingsAccountTransaction withdraw =
         * SavingsAccountTransaction.withdrawal(this, office(), paymentDetail,
         * closedDate, transactionAmountMoney, new Date());
         * this.transactions.add(withdraw);
         */
        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(this.status));
        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.closedOnDateParamName, closedDate.toString(fmt));

        this.rejectedOnDate = null;
        this.rejectedBy = null;
        this.withdrawnOnDate = null;
        this.withdrawnBy = null;
        this.closedOnDate = closedDate.toDate();
        this.closedBy = currentUser;
        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);

    }

    @Override
    public Money activateWithBalance() {
        return Money.of(this.currency, this.minRequiredOpeningBalance);
    }

    protected void processAccountUponActivation(final DateTimeFormatter fmt) {
        final Money minRequiredOpeningBalance = Money.of(this.currency, this.minRequiredOpeningBalance);
        if (minRequiredOpeningBalance.isGreaterThanZero()) {
            final SavingsAccountTransactionDTO transactionDTO = new SavingsAccountTransactionDTO(fmt, getActivationLocalDate(),
                    minRequiredOpeningBalance.getAmount(), null, new Date());
            deposit(transactionDTO);

            // update existing transactions so derived balance fields are
            // correct.
            recalculateDailyBalances(Money.zero(this.currency), DateUtils.getLocalDateOfTenant());
        }
    }

    public void close(final AppUser currentUser, final JsonCommand command, final LocalDate tenantsTodayDate,
            final Map<String, Object> actualChanges) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.closeAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.MATURED.hasStateOf(currentStatus)) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.in.matured.state");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        final LocalDate closedDate = command.localDateValueOfParameterNamed(SavingsApiConstants.closedOnDateParamName);

        if (closedDate.isBefore(getActivationLocalDate())) {
            baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                    .failWithCode("must.be.after.activation.date");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }
        if (closedDate.isBefore(maturityDate())) {
            baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                    .failWithCode("must.be.after.account.maturity.date");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }
        if (closedDate.isAfter(tenantsTodayDate)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                    .failWithCode("cannot.be.a.future.date");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }
        final List<SavingsAccountTransaction> savingsAccountTransactions = retreiveListOfTransactions();
        if (savingsAccountTransactions.size() > 0) {
            final SavingsAccountTransaction accountTransaction = savingsAccountTransactions.get(savingsAccountTransactions.size() - 1);
            if (accountTransaction.isAfter(closedDate)) {
                baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                        .failWithCode("must.be.after.last.transaction.date");
                if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
            }
        }

        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_CLOSE_ACCOUNT, closedDate);
        this.status = SavingsAccountStatusType.CLOSED.getValue();

        final Integer onAccountClosureId = command.integerValueOfParameterNamed(onAccountClosureIdParamName);
        final DepositAccountOnClosureType onClosureType = DepositAccountOnClosureType.fromInt(onAccountClosureId);
        this.accountTermAndPreClosure.updateOnAccountClosureStatus(onClosureType);

        /*
         * // withdraw deposit amount before closing the account final Money
         * transactionAmountMoney = Money.of(this.currency,
         * this.getAccountBalance()); final SavingsAccountTransaction withdraw =
         * SavingsAccountTransaction.withdrawal(this, office(), paymentDetail,
         * closedDate, transactionAmountMoney, new Date());
         * this.transactions.add(withdraw);
         */

        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(this.status));
        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.closedOnDateParamName, closedDate.toString(fmt));

        this.rejectedOnDate = null;
        this.rejectedBy = null;
        this.withdrawnOnDate = null;
        this.withdrawnBy = null;
        this.closedOnDate = closedDate.toDate();
        this.closedBy = currentUser;
        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);
    }

    public void postMaturityInterest() {
        final LocalDate interestPostingUpToDate = maturityDate();
        final MathContext mc = MathContext.DECIMAL64;
        boolean isInterestTransfer = false;
        final List<PostingPeriod> postingPeriods = calculateInterestUsing(mc, interestPostingUpToDate, isInterestTransfer);

        Money interestPostedToDate = Money.zero(this.currency);

        boolean recalucateDailyBalanceDetails = false;

        for (final PostingPeriod interestPostingPeriod : postingPeriods) {

            LocalDate interestPostingTransactionDate = interestPostingPeriod.dateOfPostingTransaction();
            interestPostingTransactionDate = interestPostingTransactionDate.isAfter(interestPostingUpToDate) ? interestPostingUpToDate
                    : interestPostingTransactionDate;
            final Money interestEarnedToBePostedForPeriod = interestPostingPeriod.getInterestEarned();

            interestPostedToDate = interestPostedToDate.plus(interestEarnedToBePostedForPeriod);

            final SavingsAccountTransaction postingTransaction = findInterestPostingTransactionFor(interestPostingTransactionDate);
            if (postingTransaction == null) {
                final SavingsAccountTransaction newPostingTransaction = SavingsAccountTransaction.interestPosting(this, office(),
                        interestPostingTransactionDate, interestEarnedToBePostedForPeriod);
                this.transactions.add(newPostingTransaction);
                recalucateDailyBalanceDetails = true;
            } else {
                final boolean correctionRequired = postingTransaction.hasNotAmount(interestEarnedToBePostedForPeriod);
                if (correctionRequired) {
                    postingTransaction.reverse();
                    final SavingsAccountTransaction newPostingTransaction = SavingsAccountTransaction.interestPosting(this, office(),
                            interestPostingTransactionDate, interestEarnedToBePostedForPeriod);
                    this.transactions.add(newPostingTransaction);
                    recalucateDailyBalanceDetails = true;
                }
            }
        }

        if (recalucateDailyBalanceDetails) {
            // update existing transactions so derived balance fields are
            // correct.
            recalculateDailyBalances(Money.zero(this.currency), interestPostingUpToDate);
        }

        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);
    }

    public void postPreMaturityInterest(final LocalDate accountCloseDate) {

        final Money interestPostedToDate = totalInterestPosted();
        final Money interestOnMaturity = calculatePreMatureInterest(accountCloseDate, retreiveOrderedNonInterestPostingTransactions());

        boolean recalucateDailyBalance = false;

        // post remaining interest
        final Money remainigInterestToBePosted = interestOnMaturity.minus(interestPostedToDate);
        if (!remainigInterestToBePosted.isZero()) {
            final SavingsAccountTransaction newPostingTransaction = SavingsAccountTransaction.interestPosting(this, office(),
                    accountCloseDate, remainigInterestToBePosted);
            this.transactions.add(newPostingTransaction);
            recalucateDailyBalance = true;
        }

        if (recalucateDailyBalance) {
            // update existing transactions so derived balance fields are
            // correct.
            recalculateDailyBalances(Money.zero(this.currency), accountCloseDate);
        }

        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);
        this.accountTermAndPreClosure.updateMaturityDetails(this.getAccountBalance(), accountCloseDate);
    }

    public BigDecimal calculatePreMatureAmount(final LocalDate preMatureDate) {

        final Money interestPostedToDate = totalInterestPosted().copy();

        final Money interestEarnedTillDate = calculatePreMatureInterest(preMatureDate, retreiveOrderedNonInterestPostingTransactions());

        final Money accountBalance = Money.of(getCurrency(), getAccountBalance());
        final Money maturityAmount = accountBalance.minus(interestPostedToDate).plus(interestEarnedTillDate);

        return maturityAmount.getAmount();
    }

    private Money calculatePreMatureInterest(final LocalDate preMatureDate, final List<SavingsAccountTransaction> transactions) {
        final MathContext mc = MathContext.DECIMAL64;
        final List<PostingPeriod> postingPeriods = calculateInterestPayable(mc, preMatureDate, transactions);

        Money interestOnMaturity = Money.zero(this.currency);

        for (final PostingPeriod interestPostingPeriod : postingPeriods) {
            final Money interestEarnedForPeriod = interestPostingPeriod.getInterestEarned();
            interestOnMaturity = interestOnMaturity.plus(interestEarnedForPeriod);
        }
        this.summary.updateFromInterestPeriodSummaries(this.currency, postingPeriods);
        return interestOnMaturity;
    }

    @Override
    public void postInterest(final MathContext mc, final LocalDate postingDate, final boolean isInterestTransfer) {
        final LocalDate interestPostingUpToDate = interestPostingUpToDate(postingDate);
        super.postInterest(mc, interestPostingUpToDate, isInterestTransfer);
    }

    @Override
    public List<PostingPeriod> calculateInterestUsing(final MathContext mc, final LocalDate postingDate, boolean isInterestTransfer) {
        final LocalDate interestPostingUpToDate = interestPostingUpToDate(postingDate);
        return super.calculateInterestUsing(mc, interestPostingUpToDate, isInterestTransfer);
    }

    private LocalDate interestPostingUpToDate(final LocalDate interestPostingDate) {
        LocalDate interestPostingUpToDate = interestPostingDate;
        final LocalDate uptoMaturityDate = interestCalculatedUpto();
        if (uptoMaturityDate != null && uptoMaturityDate.isBefore(interestPostingDate)) {
            interestPostingUpToDate = uptoMaturityDate;
        }
        return interestPostingUpToDate;
    }

    public LocalDate maturityDate() {
        return this.accountTermAndPreClosure.getMaturityLocalDate();
    }

    public BigDecimal maturityAmount() {
        return this.accountTermAndPreClosure.maturityAmount();
    }

    private LocalDate interestCalculatedUpto() {
        LocalDate uptoMaturityDate = calculateMaturityDate();
        if (uptoMaturityDate != null) {
            // interest should not be calculated for maturity day
            uptoMaturityDate = uptoMaturityDate.minusDays(1);
        }
        return uptoMaturityDate;
    }

    private Money totalInterestPosted() {
        Money interestPostedToDate = Money.zero(this.currency);
        for (final SavingsAccountTransaction transaction : this.transactions) {
            if (transaction.isInterestPostingAndNotReversed()) {
                interestPostedToDate = interestPostedToDate.plus(transaction.getAmount(currency));
            }
        }

        return interestPostedToDate;
    }

    @Override
    public Map<String, Object> activate(final AppUser currentUser, final JsonCommand command, final LocalDate tenantsTodayDate) {

        final Map<String, Object> actualChanges = super.activate(currentUser, command, tenantsTodayDate);

        if (accountTermAndPreClosure.isAfterExpectedFirstDepositDate(getActivationLocalDate())) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                    .resource(RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME);
            final DateTimeFormatter formatter = DateTimeFormat.forPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String dateAsString = formatter.print(this.accountTermAndPreClosure.getExpectedFirstDepositOnDate());
            baseDataValidator.reset().parameter(DepositsApiConstants.activatedOnDateParamName).value(dateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.expected.first.deposit.date");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        return actualChanges;
    }

    protected List<SavingsAccountTransaction> sortTransactions(final List<SavingsAccountTransaction> transactions) {
        final List<SavingsAccountTransaction> listOfTransactionsSorted = new ArrayList<SavingsAccountTransaction>();
        listOfTransactionsSorted.addAll(transactions);

        final SavingsAccountTransactionComparator transactionComparator = new SavingsAccountTransactionComparator();
        Collections.sort(listOfTransactionsSorted, transactionComparator);
        return listOfTransactionsSorted;
    }

    @Override
    public SavingsAccountTransaction deposit(final SavingsAccountTransactionDTO transactionDTO) {

        if (isAccountMatured()) {
            final String defaultUserMessage = "Transaction is not allowed. Account is matured.";
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg.recurring.deposit.account.transaction.account.is.matured", defaultUserMessage, "transactionDate",
                    transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (!isBeforeMaturityDate(transactionDTO.getTransactionDate())) {
            final String defaultUserMessage = "Transaction is not allowed. Transaction date is on or after account maturity date.";
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg.recurring.deposit.account.transaction.date.is.after.account.maturity.date", defaultUserMessage,
                    "transactionDate", transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (isBeforeDepositStartDate(transactionDTO.getTransactionDate())) {
            final String defaultUserMessage = "Transaction is not allowed. Transaction date is on or after account activation and deposit start date.";
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg.recurring.deposit.account.transaction.date.is.before.account.activation.or.deposit.date",
                    defaultUserMessage, "transactionDate", transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        final SavingsAccountTransaction transaction = super.deposit(transactionDTO);

        return transaction;
    }

    public void handleScheduleInstallments(final SavingsAccountTransaction transaction) {

        final LocalDate transactionDate = transaction.transactionLocalDate();
        Money transactionAmountUnprocessed = transaction.getAmount(getCurrency());

        for (RecurringDepositScheduleInstallment currentInstallment : depositScheduleInstallments()) {
            if (currentInstallment.isNotFullyPaidOff() && transactionAmountUnprocessed.isGreaterThanZero()) {
                if (!this.adjustAdvanceTowardsFuturePayments() && currentInstallment.dueDate().isAfter(transactionDate)) {
                    transactionAmountUnprocessed = Money.zero(getCurrency());
                }
                transactionAmountUnprocessed = handleInstallmentTransaction(currentInstallment, transactionAmountUnprocessed,
                        transactionDate);
            }
        }

    }

    public void updateScheduleInstallments() {

        // reset all installments to process from the beginning
        for (RecurringDepositScheduleInstallment currentInstallment : depositScheduleInstallments()) {
            currentInstallment.resetDerivedFields();
        }

        final List<SavingsAccountTransaction> orderedDepositTransactions = retreiveOrderedDepositTransactions();
        for (SavingsAccountTransaction transaction : orderedDepositTransactions) {
            handleScheduleInstallments(transaction);
        }
    }

    private List<SavingsAccountTransaction> retreiveOrderedDepositTransactions() {
        final List<SavingsAccountTransaction> listOfTransactionsSorted = retreiveListOfTransactions();

        final List<SavingsAccountTransaction> orderedDepositTransactions = new ArrayList<SavingsAccountTransaction>();

        for (final SavingsAccountTransaction transaction : listOfTransactionsSorted) {
            if (transaction.isDepositAndNotReversed()) {
                orderedDepositTransactions.add(transaction);
            }
        }

        return orderedDepositTransactions;
    }

    /**
     * This method is responsible for checking if the current transaction is 'an
     * advance/early payment' based on the details passed through.
     * 
     * Default implementation is check transaction date is before installment
     * due date.
     */
    protected boolean isTransactionInAdvanceOfInstallment(final int currentInstallmentIndex,
            final List<RecurringDepositScheduleInstallment> installments, final LocalDate transactionDate) {

        final RecurringDepositScheduleInstallment currentInstallment = installments.get(currentInstallmentIndex);

        return transactionDate.isBefore(currentInstallment.dueDate());
    }

    private Money handleInstallmentTransaction(final RecurringDepositScheduleInstallment currentInstallment,
            final Money transactionAmountUnprocessed, final LocalDate transactionDate) {

        Money transactionAmountRemaining = transactionAmountUnprocessed;
        Money depositAmountPortion = Money.zero(transactionAmountRemaining.getCurrency());

        depositAmountPortion = currentInstallment.payInstallment(transactionDate, transactionAmountRemaining);
        transactionAmountRemaining = transactionAmountRemaining.minus(depositAmountPortion);

        return transactionAmountRemaining;

    }

    private boolean isAccountMatured() {
        return SavingsAccountStatusType.fromInt(status).isMatured();
    }

    private boolean isBeforeMaturityDate(final LocalDate compareDate) {
        final LocalDate maturityDate = this.maturityDate();
        return compareDate.isBefore(maturityDate);
    }

    private boolean isBeforeDepositStartDate(LocalDate compareDate) {
        return compareDate.isBefore(depositStartDate());
    }

    public void validateDomainRules() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME);
        validateDomainRules(baseDataValidator);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    private void validateDomainRules(final DataValidatorBuilder baseDataValidator) {

        final boolean isMinTermGreaterThanMax = this.accountTermAndPreClosure.depositTermDetail()
                .isMinDepositTermGreaterThanMaxDepositTerm();
        // deposit period should be within min and max deposit term
        if (isMinTermGreaterThanMax) {
            final Integer maxTerm = this.accountTermAndPreClosure.depositTermDetail().maxDepositTerm();
            baseDataValidator.reset().parameter(maxDepositTermParamName).value(maxTerm)
                    .failWithCodeNoParameterAddedToErrorCode("max.term.lessthan.min.term");
        }

        final Integer depositPeriod = this.accountTermAndPreClosure.depositPeriod();
        final SavingsPeriodFrequencyType depositPeriodFrequencyType = this.accountTermAndPreClosure.depositPeriodFrequencyType();
        final boolean isValidDepositPeriod = this.accountTermAndPreClosure.depositTermDetail().isDepositBetweenMinAndMax(
                depositStartDate(), calculateMaturityDate());
        if (!isValidDepositPeriod) {
            baseDataValidator.reset().parameter(depositPeriodParamName).value(depositPeriod)
                    .failWithCodeNoParameterAddedToErrorCode("deposit.period.not.between.min.and.max.deposit.term");
        } else {
            final Integer inMultiplesOf = this.accountTermAndPreClosure.depositTermDetail().inMultiplesOfDepositTerm();
            if (inMultiplesOf != null) {
                final boolean isValid = this.accountTermAndPreClosure.depositTermDetail().isValidInMultiplesOfPeriod(depositPeriod,
                        depositPeriodFrequencyType);
                if (!isValid) {
                    baseDataValidator.reset().parameter(depositPeriodParamName).value(depositPeriod)
                            .failWithCodeNoParameterAddedToErrorCode("deposit.period.not.multiple.of.term");
                }
            }
        }
        if (isAccountLocked(calculateMaturityDate())) {
            baseDataValidator
                    .reset()
                    .parameter(depositPeriodParamName)
                    .value(depositPeriod)
                    .failWithCode("deposit.period.must.be.greater.than.lock.in.period",
                            "Deposit period must be greater than account lock-in period.");
        }

        if (firstDepositDateBeforeAccountSubmittedOrActivationDate()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                    "expected.first.deposit.date.must.be.after.account.submitted.or.activation.date");
        }

        // FIXME: Handle this scenario
        /*
         * //final boolean recurringFrequencyBeforeDepositPeriod =
         * recurringFrequencyBeforeDepositPeriod();
         * 
         * if (!recurringFrequencyBeforeDepositPeriod) {
         * baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
         * "recurring.frequency.not.before.deposit.period"); }
         */
    }

    public void validateApplicableInterestRate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME);

        if (this.chart != null) {
            final LocalDate chartFromDate = this.chart.getFromDateAsLocalDate();
            LocalDate chartEndDate = this.chart.getEndDateAsLocalDate();
            chartEndDate = chartEndDate == null ? DateUtils.getLocalDateOfTenant() : chartEndDate;

            final LocalDateInterval chartInterval = LocalDateInterval.create(chartFromDate, chartEndDate);
            if (!chartInterval.contains(accountSubmittedOrActivationDate())) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("no.valid.interest.rate.slab.available.for.date.range");
            }

            final BigDecimal maturityAmount = this.accountTermAndPreClosure.maturityAmount();
            final ClientIncentiveAttributes incentiveAttributes = (this.client == null) ? null : this.client.incentiveAttributes();
            BigDecimal applicableInterestRate = this.chart.getApplicableInterestRate(maturityAmount, depositStartDate(),
                    calculateMaturityDate(), incentiveAttributes);

            if (applicableInterestRate.equals(BigDecimal.ZERO)) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                        "no.applicable.interest.rate.is.found.based.on.amount.and.deposit.period");
            }

        } else if (this.nominalAnnualInterestRate == null || this.nominalAnnualInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            baseDataValidator.reset().parameter(DepositsApiConstants.nominalAnnualInterestRateParamName).value(nominalAnnualInterestRate)
                    .failWithCodeNoParameterAddedToErrorCode("interest.chart.or.nominal.interest.rate.required");
        }

        /**
         * final boolean recurringFrequencyBeforeDepositPeriod =
         * recurringFrequencyBeforeDepositPeriod();
         * 
         * if (!recurringFrequencyBeforeDepositPeriod) {
         * baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
         * "recurring.frequency.not.before.deposit.period"); }
         **/
    }

    public boolean isReinvestOnClosure() {
        return this.accountTermAndPreClosure.isReinvestOnClosure();
    }

    public boolean isTransferToSavingsOnClosure() {
        return this.accountTermAndPreClosure.isTransferToSavingsOnClosure();
    }

    public RecurringDepositAccount reInvest(BigDecimal depositAmount) {

        final DepositAccountTermAndPreClosure newAccountTermAndPreClosure = this.accountTermAndPreClosure.copy(depositAmount);
        final DepositAccountRecurringDetail recurringDetail = this.recurringDetail.copy();
        final SavingsProduct product = this.product;
        final InterestRateChart productChart = product.applicableChart(getClosedOnDate());
        final DepositAccountInterestRateChart newChart = DepositAccountInterestRateChart.from(productChart);
        final String accountNumber = null;
        final String externalId = this.externalId;
        final AccountType accountType = AccountType.fromInt(this.accountType);
        final SavingsPostingInterestPeriodType postingPeriodType = SavingsPostingInterestPeriodType.fromInt(this.interestPostingPeriodType);
        final SavingsCompoundingInterestPeriodType compoundingPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(this.interestCompoundingPeriodType);
        final SavingsInterestCalculationType interestCalculationType = SavingsInterestCalculationType.fromInt(this.interestCalculationType);
        final SavingsInterestCalculationDaysInYearType daysInYearType = SavingsInterestCalculationDaysInYearType
                .fromInt(this.interestCalculationDaysInYearType);
        final BigDecimal minRequiredOpeningBalance = depositAmount;
        final BigDecimal interestRate = BigDecimal.ZERO;
        final Set<SavingsAccountCharge> savingsAccountCharges = null;
        final SavingsPeriodFrequencyType lockinPeriodFrequencyType = SavingsPeriodFrequencyType.fromInt(this.lockinPeriodFrequencyType);
        final Integer lockinPeriodFrequency = this.lockinPeriodFrequency;
        final boolean withdrawalFeeApplicableForTransfer = false;

        LocalDate now = DateUtils.getLocalDateOfTenant();

        newAccountTermAndPreClosure.updateExpectedFirstDepositDate(now);

        RecurringDepositAccount rdAccount = RecurringDepositAccount.createNewActivatedAccount(client, group, product, fieldOfficer,
                accountNumber, externalId, accountType, getClosedOnDate(), closedBy, interestRate, compoundingPeriodType,
                postingPeriodType, interestCalculationType, daysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType, withdrawalFeeApplicableForTransfer, savingsAccountCharges, newAccountTermAndPreClosure,
                recurringDetail, newChart);

        rdAccount.setDatesFrom(now);
        newAccountTermAndPreClosure.updateAccountReference(rdAccount);
        recurringDetail.updateAccountReference(rdAccount);

        return rdAccount;

    }

    private boolean firstDepositDateBeforeAccountSubmittedOrActivationDate() {
        final LocalDate expectedFirstDepositLocalDate = accountTermAndPreClosure.getExpectedFirstDepositOnDate();
        if (expectedFirstDepositLocalDate == null) return false;
        return expectedFirstDepositLocalDate.isBefore(accountSubmittedOrActivationDate());
    }

    public void setDatesFrom(final LocalDate now) {
        this.rejectedOnDate = null;
        this.rejectedBy = null;
        this.withdrawnOnDate = null;
        this.withdrawnBy = null;
        this.closedOnDate = null;
        this.closedBy = null;

        this.activatedBy = null;
        this.lockedInUntilDate = null;

        this.activatedOnDate = now.toDate();
    }

    @Override
    protected boolean isTransferInterestToOtherAccount() {
        return this.accountTermAndPreClosure.isTransferInterestToLinkedAccount();
    }

    public void generateSchedule(final PeriodFrequencyType frequency, final Integer recurringEvery) {
        final List<RecurringDepositScheduleInstallment> depositScheduleInstallments = depositScheduleInstallments();
        depositScheduleInstallments.clear();
        LocalDate installmentDate = depositStartDate();
        final LocalDate maturityDate = calculateMaturityDate();
        int installmentNumber = 1;
        final BigDecimal depositAmount = this.recurringDetail.mandatoryRecommendedDepositAmount();
        while (maturityDate.isAfter(installmentDate)) {
            final RecurringDepositScheduleInstallment installment = RecurringDepositScheduleInstallment.installment(this,
                    installmentNumber, installmentDate.toDate(), depositAmount);
            depositScheduleInstallments.add(installment);
            installmentDate = calculateNextDepositDate(installmentDate, frequency, recurringEvery);
            installmentNumber += 1;
        }
    }

    private List<RecurringDepositScheduleInstallment> depositScheduleInstallments() {
        if (this.depositScheduleInstallments == null) {
            this.depositScheduleInstallments = new ArrayList<RecurringDepositScheduleInstallment>();
        }
        return this.depositScheduleInstallments;
    }

    public boolean isCalendarInherited() {
        return this.recurringDetail.isCalendarInherited();
    }

    public void updateOverduePayments(final LocalDate todayDate) {
        LocalDate overdueUptoDate = this.maturityDate();
        if (overdueUptoDate.isAfter(todayDate)) {
            overdueUptoDate = todayDate;
        }

        final List<RecurringDepositScheduleInstallment> installments = depositScheduleInstallments();
        int noOfOverdueInstallments = 0;
        Money totalOverdueAmount = Money.zero(getCurrency());
        for (RecurringDepositScheduleInstallment installment : installments) {
            if (installment.isNotFullyPaidOff() && overdueUptoDate.isAfter(installment.dueDate())) {
                noOfOverdueInstallments++;
                totalOverdueAmount = totalOverdueAmount.plus(installment.getDepositAmountOutstanding(getCurrency()));
            }
        }
        this.recurringDetail.updateOverdueDetails(noOfOverdueInstallments, totalOverdueAmount);
    }

    public boolean allowWithdrawal() {
        return this.recurringDetail.allowWithdrawal();
    }

    public boolean adjustAdvanceTowardsFuturePayments() {
        return this.recurringDetail.adjustAdvanceTowardsFuturePayments();
    }
}