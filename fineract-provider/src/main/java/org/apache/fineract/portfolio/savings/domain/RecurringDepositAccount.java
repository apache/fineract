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

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositPeriodParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.maxDepositTermParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.onAccountClosureIdParamName;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChart;
import org.apache.fineract.portfolio.savings.DepositAccountOnClosureType;
import org.apache.fineract.portfolio.savings.DepositAccountUtils;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.PreClosurePenalInterestOnType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@Entity
@DiscriminatorValue("300")
public class RecurringDepositAccount extends SavingsAccount {

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private DepositAccountTermAndPreClosure accountTermAndPreClosure;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private DepositAccountRecurringDetail recurringDetail;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "account")
    private DepositAccountInterestRateChart chart;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "account", orphanRemoval = true, fetch=FetchType.LAZY)
    private List<RecurringDepositScheduleInstallment> depositScheduleInstallments = new ArrayList<>();

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
            final DepositAccountInterestRateChart chart, final boolean withHoldTax) {

        final boolean allowOverdraft = false;
        final BigDecimal overdraftLimit = new BigDecimal(0);

        final SavingsAccountStatusType status = SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL;
        return new RecurringDepositAccount(client, group, product, fieldOfficer, accountNo, externalId, status, accountType,
                submittedOnDate, submittedBy, interestRate, interestCompoundingPeriodType, interestPostingPeriodType,
                interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType, withdrawalFeeApplicableForTransfer, savingsAccountCharges, accountTermAndPreClosure,
                recurringDetail, chart, allowOverdraft, overdraftLimit, withHoldTax);
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
            final DepositAccountInterestRateChart chart, final boolean withHoldTax) {

        final boolean allowOverdraft = false;
        final BigDecimal overdraftLimit = new BigDecimal(0);

        final SavingsAccountStatusType status = SavingsAccountStatusType.ACTIVE;
        return new RecurringDepositAccount(client, group, product, fieldOfficer, accountNo, externalId, status, accountType,
                submittedOnDate, submittedBy, interestRate, interestCompoundingPeriodType, interestPostingPeriodType,
                interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType, withdrawalFeeApplicableForTransfer, savingsAccountCharges, accountTermAndPreClosure,
                recurringDetail, chart, allowOverdraft, overdraftLimit, withHoldTax);
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
            final DepositAccountInterestRateChart chart, final boolean allowOverdraft, final BigDecimal overdraftLimit,
            final boolean withHoldTax) {

        super(client, group, product, fieldOfficer, accountNo, externalId, status, accountType, submittedOnDate, submittedBy,
                nominalAnnualInterestRate, interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType,
                interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType,
                withdrawalFeeApplicableForTransfer, savingsAccountCharges, allowOverdraft, overdraftLimit, withHoldTax);

        this.accountTermAndPreClosure = accountTermAndPreClosure;
        this.recurringDetail = recurringDetail;
        this.chart = chart;
        if (this.chart != null) {
            this.chart.updateDepositAccountReference(this);
        }
    }

    @Override
    public void modifyApplication(final JsonCommand command, final Map<String, Object> actualChanges) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.modifyApplicationAction);
        super.modifyApplication(command, actualChanges, baseDataValidator);
        final Map<String, Object> termAndPreClosureChanges = accountTermAndPreClosure.update(command, baseDataValidator);
        actualChanges.putAll(termAndPreClosureChanges);
        recurringDetail.update(command);
        validateDomainRules(baseDataValidator);
        super.validateInterestPostingAndCompoundingPeriodTypes(baseDataValidator);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    private void updateDepositAmount() {
        BigDecimal recurringAmount = getRecurringDetail().mandatoryRecommendedDepositAmount();
        Integer numberOfDepositPeriods = depositScheduleInstallments().size();
        if (this.accountTermAndPreClosure.depositPeriod() != null && recurringAmount != null && numberOfDepositPeriods != null) {
            BigDecimal depositAmount = Money.of(product.currency(), recurringAmount).multipliedBy(numberOfDepositPeriods)
                    .plus(this.minRequiredOpeningBalance).getAmount();
            accountTermAndPreClosure.updateDepositAmount(depositAmount);
        } else if (accountTermAndPreClosure.depositAmount() == null) {
            accountTermAndPreClosure.updateDepositAmount(Money.zero(product.currency()).getAmount());
        }
    }

    public void updateDepositAmount(final BigDecimal depositAmount) {
        if (this.accountTermAndPreClosure.depositPeriod() == null) {
            accountTermAndPreClosure.updateDepositAmount(accountTermAndPreClosure.depositAmount().add(depositAmount));
        }
    }

    @Override
    protected BigDecimal getEffectiveInterestRateAsFraction(final MathContext mc, final LocalDate interestPostingUpToDate) {
        boolean isPreMatureClosure = false;
        return getEffectiveInterestRateAsFraction(mc, interestPostingUpToDate, isPreMatureClosure);
    }

    protected BigDecimal getEffectiveInterestRateAsFraction(final MathContext mc, final LocalDate interestPostingUpToDate,
            final boolean isPreMatureClosure) {

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

        if (depositCloseDate == null) {
            depositCloseDate = DateUtils.getLocalDateOfTenant();
        }

        final BigDecimal depositAmount = accountTermAndPreClosure.depositAmount();
        BigDecimal applicableInterestRate = this.chart.getApplicableInterestRate(depositAmount, depositStartDate(), depositCloseDate,
                this.client);

        if (applyPreMaturePenalty) {
            applicableInterestRate = applicableInterestRate.subtract(penalInterest);
            applicableInterestRate = applicableInterestRate.compareTo(BigDecimal.ZERO) == -1 ? BigDecimal.ZERO : applicableInterestRate;
        }

        this.nominalAnnualInterestRate = applicableInterestRate;

        return applicableInterestRate.divide(BigDecimal.valueOf(100l), mc);
    }

    public void updateMaturityDateAndAmount(final MathContext mc, final boolean isPreMatureClosure,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {
        final LocalDate maturityDate = calculateMaturityDate();
        LocalDate interestCalculationUpto = null;
        List<SavingsAccountTransaction> allTransactions = null;
        if (maturityDate == null) {
            interestCalculationUpto = DateUtils.getLocalDateOfTenant();
            allTransactions = getTransactions(interestCalculationUpto, false);
        } else {
            interestCalculationUpto = maturityDate.minusDays(1);
            allTransactions = getTransactions(interestCalculationUpto, true);

        }

        final List<PostingPeriod> postingPeriods = calculateInterestPayable(mc, interestCalculationUpto, allTransactions,
                isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);
        Money totalInterestPayable = Money.zero(getCurrency());
        Money totalDepositAmount = Money.zero(getCurrency());
        for (PostingPeriod postingPeriod : postingPeriods) {
            totalInterestPayable = totalInterestPayable.plus(postingPeriod.getInterestEarned());
            totalDepositAmount = totalDepositAmount.plus(postingPeriod.closingBalance()).minus(postingPeriod.openingBalance());
        }
        if (maturityDate == null) {
            this.accountTermAndPreClosure.updateDepositAmount(totalDepositAmount.getAmount());
        } else {
            this.accountTermAndPreClosure.updateMaturityDetails(totalDepositAmount.getAmount(), totalInterestPayable.getAmount(),
                    maturityDate);
        }
    }

    public void updateMaturityStatus(final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
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
            postMaturityInterest(isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, todayDate);
        }
    }

    public LocalDate calculateMaturityDate() {

        final LocalDate startDate = depositStartDate();
        LocalDate maturityDate = null;
        final Integer depositPeriod = this.accountTermAndPreClosure.depositPeriod();
        if (depositPeriod == null) { return maturityDate; }
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
            final List<SavingsAccountTransaction> transactions, final boolean isPreMatureClosure,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {

        // 1. default to calculate interest based on entire history OR
        // 2. determine latest 'posting period' and find interest credited to
        // that period

        // A generate list of EndOfDayBalances (not including interest postings)
        final SavingsPostingInterestPeriodType postingPeriodType = SavingsPostingInterestPeriodType.fromInt(this.interestPostingPeriodType);

        final SavingsCompoundingInterestPeriodType compoundingPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(this.interestCompoundingPeriodType);

        final SavingsInterestCalculationDaysInYearType daysInYearType = SavingsInterestCalculationDaysInYearType
                .fromInt(this.interestCalculationDaysInYearType);
        List<LocalDate> PostedAsOnDates =  getManualPostingDates();
        final List<LocalDateInterval> postingPeriodIntervals = this.savingsHelper.determineInterestPostingPeriods(depositStartDate(),
                maturityDate, postingPeriodType, financialYearBeginningMonth, PostedAsOnDates);

        final List<PostingPeriod> allPostingPeriods = new ArrayList<>();

        Money periodStartingBalance = Money.zero(currency);

        final SavingsInterestCalculationType interestCalculationType = SavingsInterestCalculationType.fromInt(this.interestCalculationType);
        final BigDecimal interestRateAsFraction = getEffectiveInterestRateAsFraction(mc, maturityDate, isPreMatureClosure);
        final Collection<Long> interestPostTransactions = this.savingsHelper.fetchPostInterestTransactionIds(getId());
        boolean isInterestTransfer = false;
        final Money minBalanceForInterestCalculation = Money.of(getCurrency(), minBalanceForInterestCalculation());
        for (final LocalDateInterval periodInterval : postingPeriodIntervals) {
            boolean isUserPosting = false;
            if (PostedAsOnDates.contains(periodInterval.endDate())) {
                isUserPosting = true;
            }
            final PostingPeriod postingPeriod = PostingPeriod.createFrom(periodInterval, periodStartingBalance, transactions,
                    this.currency, compoundingPeriodType, interestCalculationType, interestRateAsFraction, daysInYearType.getValue(),
                    maturityDate, interestPostTransactions, isInterestTransfer, minBalanceForInterestCalculation,
                    isSavingsInterestPostingAtCurrentPeriodEnd, isUserPosting, financialYearBeginningMonth);

            periodStartingBalance = postingPeriod.closingBalance();

            allPostingPeriods.add(postingPeriod);
        }

        this.savingsHelper.calculateInterestForAllPostingPeriods(this.currency, allPostingPeriods, this.getLockedInUntilLocalDate(),
                isTransferInterestToOtherAccount());
        // this.summary.updateFromInterestPeriodSummaries(this.currency,
        // allPostingPeriods);
        return allPostingPeriods;
    }

    private List<SavingsAccountTransaction> getTransactions(final LocalDate depositEndDate, final boolean generateFutureTransactions) {
        List<SavingsAccountTransaction> allTransactions = new ArrayList<>();
        // add existing transactions
        allTransactions.addAll(retreiveOrderedNonInterestPostingTransactions());
        LocalDate latestTransactionDate = null;
        for (final SavingsAccountTransaction installment : allTransactions) {
            if(latestTransactionDate == null || latestTransactionDate.isBefore(installment.getTransactionLocalDate())){
                latestTransactionDate = installment.getTransactionLocalDate();
            }
        }
        if (generateFutureTransactions) {
            for (RecurringDepositScheduleInstallment installment : depositScheduleInstallments()) {
                if (installment.isPrincipalNotCompleted(getCurrency())) {
                    LocalDate dueDate = installment.dueDate();
                    if(latestTransactionDate != null && dueDate.isBefore(latestTransactionDate)){
                        dueDate = latestTransactionDate;
                    }
                    final SavingsAccountTransaction transaction = SavingsAccountTransaction.deposit(null, office(), null,
                            dueDate, installment.getDepositAmountOutstanding(getCurrency()), installment.dueDate().toDate(),
                            null);
                    allTransactions.add(transaction);
                }
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

    public LocalDate depositStartDate() {
        final LocalDate depositStartDate = accountTermAndPreClosure.getExpectedFirstDepositOnDate();
        if (depositStartDate == null) return accountSubmittedOrActivationDate();
        return depositStartDate;
    }

    public void prematureClosure(final AppUser currentUser, final JsonCommand command, final LocalDate tenantsTodayDate,
            final Map<String, Object> actualChanges) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
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

    protected void processAccountUponActivation(final DateTimeFormatter fmt, final AppUser user) {
        final Money minRequiredOpeningBalance = Money.of(this.currency, this.minRequiredOpeningBalance);
        if (minRequiredOpeningBalance.isGreaterThanZero()) {
            final SavingsAccountTransactionDTO transactionDTO = new SavingsAccountTransactionDTO(fmt, getActivationLocalDate(),
                    minRequiredOpeningBalance.getAmount(), null, new Date(), user, accountType);
            deposit(transactionDTO);

            // update existing transactions so derived balance fields are
            // correct.
            recalculateDailyBalances(Money.zero(this.currency), DateUtils.getLocalDateOfTenant());
        }
    }

    public void close(final AppUser currentUser, final JsonCommand command, final LocalDate tenantsTodayDate,
            final Map<String, Object> actualChanges) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.closeAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.MATURED.hasStateOf(currentStatus) && this.maturityDate() != null) {
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
        if (maturityDate() != null && closedDate.isBefore(maturityDate())) {
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

    public void postMaturityInterest(final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth,
            final LocalDate closeDate) {
        LocalDate interestPostingUpToDate = maturityDate();
        if (interestPostingUpToDate == null) {
            interestPostingUpToDate = closeDate;
        }
        this.setClosedOnDate(closeDate);
        final MathContext mc = MathContext.DECIMAL64;
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        final List<PostingPeriod> postingPeriods = calculateInterestUsing(mc, interestPostingUpToDate.minusDays(1), isInterestTransfer,
                isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate);

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
                        interestPostingTransactionDate, interestEarnedToBePostedForPeriod, interestPostingPeriod.isUserPosting());
                addTransaction(newPostingTransaction);
                recalucateDailyBalanceDetails = true;
            } else {
                final boolean correctionRequired = postingTransaction.hasNotAmount(interestEarnedToBePostedForPeriod);
                if (correctionRequired) {
                    postingTransaction.reverse();
                    final SavingsAccountTransaction newPostingTransaction = SavingsAccountTransaction.interestPosting(this, office(),
                            interestPostingTransactionDate, interestEarnedToBePostedForPeriod, interestPostingPeriod.isUserPosting());
                    addTransaction(newPostingTransaction);
                    recalucateDailyBalanceDetails = true;
                }
            }
        }
        applyWithholdTaxForDepositAccounts(interestPostingUpToDate, recalucateDailyBalanceDetails);
        if (recalucateDailyBalanceDetails) {
            // update existing transactions so derived balance fields are
            // correct.
            recalculateDailyBalances(Money.zero(this.currency), interestPostingUpToDate);
        }
        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);
    }

    public void postPreMaturityInterest(final LocalDate accountCloseDate, final boolean isPreMatureClosure,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {

        final Money interestPostedToDate = totalInterestPosted();
        // calculate interest before one day of closure date
        final LocalDate interestCalculatedToDate = accountCloseDate.minusDays(1);
        final Money interestOnMaturity = calculatePreMatureInterest(interestCalculatedToDate,
                retreiveOrderedNonInterestPostingTransactions(), isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth);

        boolean recalucateDailyBalance = false;

        // post remaining interest
        final Money remainigInterestToBePosted = interestOnMaturity.minus(interestPostedToDate);
        if (!remainigInterestToBePosted.isZero()) {
            final boolean postInterestAsOn = false;
            final SavingsAccountTransaction newPostingTransaction = SavingsAccountTransaction.interestPosting(this, office(),
                    accountCloseDate, remainigInterestToBePosted, postInterestAsOn);
            addTransaction(newPostingTransaction);
            recalucateDailyBalance = true;
        }

        applyWithholdTaxForDepositAccounts(accountCloseDate, recalucateDailyBalance);

        if (recalucateDailyBalance) {
            // update existing transactions so derived balance fields are
            // correct.
            recalculateDailyBalances(Money.zero(this.currency), accountCloseDate);
        }

        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);
        this.accountTermAndPreClosure.updateMaturityDetails(this.getAccountBalance(), accountCloseDate);
    }

    public BigDecimal calculatePreMatureAmount(final LocalDate preMatureDate, final boolean isPreMatureClosure,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {

        final Money interestPostedToDate = totalInterestPosted().copy();

        final Money interestEarnedTillDate = calculatePreMatureInterest(preMatureDate, retreiveOrderedNonInterestPostingTransactions(),
                isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);

        final Money accountBalance = Money.of(getCurrency(), getAccountBalance());
        final Money maturityAmount = accountBalance.minus(interestPostedToDate).plus(interestEarnedTillDate);

        return maturityAmount.getAmount();
    }

    private Money calculatePreMatureInterest(final LocalDate preMatureDate, final List<SavingsAccountTransaction> transactions,
            final boolean isPreMatureClosure, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth) {
        final MathContext mc = MathContext.DECIMAL64;
        final List<PostingPeriod> postingPeriods = calculateInterestPayable(mc, preMatureDate, transactions, isPreMatureClosure,
                isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);

        Money interestOnMaturity = Money.zero(this.currency);

        for (final PostingPeriod interestPostingPeriod : postingPeriods) {
            final Money interestEarnedForPeriod = interestPostingPeriod.getInterestEarned();
            interestOnMaturity = interestOnMaturity.plus(interestEarnedForPeriod);
        }
        this.summary.updateFromInterestPeriodSummaries(this.currency, postingPeriods);
        return interestOnMaturity;
    }

    @Override
    public void postInterest(final MathContext mc, final LocalDate postingDate, final boolean isInterestTransfer,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth,final LocalDate  postInterestAson) {
        final LocalDate interestPostingUpToDate = interestPostingUpToDate(postingDate);
        super.postInterest(mc, interestPostingUpToDate, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, postInterestAson);
    }

    @Override
    public List<PostingPeriod> calculateInterestUsing(final MathContext mc, final LocalDate postingDate, boolean isInterestTransfer,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth,final LocalDate  postAsInterestOn) {
        final LocalDate interestPostingUpToDate = interestPostingUpToDate(postingDate);
        return super.calculateInterestUsing(mc, interestPostingUpToDate, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, postAsInterestOn);
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
        List<SavingsAccountTransaction> trans = getTransactions() ;
        for (final SavingsAccountTransaction transaction : trans) {
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
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
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
        final List<SavingsAccountTransaction> listOfTransactionsSorted = new ArrayList<>();
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

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (!isBeforeMaturityDate(transactionDTO.getTransactionDate())) {
            final String defaultUserMessage = "Transaction is not allowed. Transaction date is on or after account maturity date.";
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg.recurring.deposit.account.transaction.date.is.after.account.maturity.date", defaultUserMessage,
                    "transactionDate", transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (isBeforeDepositStartDate(transactionDTO.getTransactionDate())) {
            final String defaultUserMessage = "Transaction is not allowed. Transaction date is on or after account activation and deposit start date.";
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg.recurring.deposit.account.transaction.date.is.before.account.activation.or.deposit.date",
                    defaultUserMessage, "transactionDate", transactionDTO.getTransactionDate().toString(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
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

    public void updateScheduleInstallmentsWithNewRecommendedDepositAmount(BigDecimal newDepositAmount,
            LocalDate depositAmountupdatedFromDate) {
        // reset all installments to process from the beginning, also update
        // deposit amount as necessary
        for (RecurringDepositScheduleInstallment currentInstallment : depositScheduleInstallments()) {
            if (currentInstallment.dueDate().isAfter(depositAmountupdatedFromDate)
                    || currentInstallment.dueDate().isEqual(depositAmountupdatedFromDate)) {
                currentInstallment.updateDepositAmountAndResetDerivedFields(newDepositAmount);
            } else {
                currentInstallment.resetDerivedFields();
            }
        }

        final List<SavingsAccountTransaction> orderedDepositTransactions = retreiveOrderedDepositTransactions();
        for (SavingsAccountTransaction transaction : orderedDepositTransactions) {
            handleScheduleInstallments(transaction);
        }
    }

    private List<SavingsAccountTransaction> retreiveOrderedDepositTransactions() {
        final List<SavingsAccountTransaction> listOfTransactionsSorted = retreiveListOfTransactions();

        final List<SavingsAccountTransaction> orderedDepositTransactions = new ArrayList<>();

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
        return maturityDate == null ? true : compareDate.isBefore(maturityDate);
    }

    private boolean isBeforeDepositStartDate(LocalDate compareDate) {
        return compareDate.isBefore(depositStartDate());
    }

    public void validateDomainRules() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME);
        validateDomainRules(baseDataValidator);
        super.validateInterestPostingAndCompoundingPeriodTypes(baseDataValidator);
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
        if (this.accountTermAndPreClosure.depositTermDetail().maxDepositTerm() != null) {
            baseDataValidator.reset().parameter(depositPeriodParamName).value(depositPeriod).notNull();
        }
        if (depositPeriod != null) {
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
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME);
        LocalDate maturityDate = calculateMaturityDate();
        if (this.chart != null) {
            final LocalDate chartFromDate = this.chart.getFromDateAsLocalDate();
            LocalDate chartEndDate = this.chart.getEndDateAsLocalDate();
            chartEndDate = chartEndDate == null ? DateUtils.getLocalDateOfTenant() : chartEndDate;

            final LocalDateInterval chartInterval = LocalDateInterval.create(chartFromDate, chartEndDate);
            if (!chartInterval.contains(accountSubmittedOrActivationDate())) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("no.valid.interest.rate.slab.available.for.date.range");
            }

            if (maturityDate == null) {
                maturityDate = DateUtils.getLocalDateOfTenant();
            }

            final BigDecimal maturityAmount = this.accountTermAndPreClosure.depositAmount();
            BigDecimal applicableInterestRate = this.chart.getApplicableInterestRate(maturityAmount, depositStartDate(), maturityDate,
                    this.client);

            if (applicableInterestRate.equals(BigDecimal.ZERO)) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                        "no.applicable.interest.rate.is.found.based.on.amount.and.deposit.period");
            }

        } else if (this.nominalAnnualInterestRate == null || this.nominalAnnualInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            baseDataValidator.reset().parameter(DepositsApiConstants.nominalAnnualInterestRateParamName).value(nominalAnnualInterestRate)
                    .failWithCodeNoParameterAddedToErrorCode("interest.chart.or.nominal.interest.rate.required");
        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
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

        LocalDate now = getClosedOnDate();
        newAccountTermAndPreClosure.updateExpectedFirstDepositDate(now);

        RecurringDepositAccount rdAccount = RecurringDepositAccount.createNewActivatedAccount(client, group, product, savingsOfficer,
                accountNumber, externalId, accountType, getClosedOnDate(), closedBy, interestRate, compoundingPeriodType,
                postingPeriodType, interestCalculationType, daysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType, withdrawalFeeApplicableForTransfer, savingsAccountCharges, newAccountTermAndPreClosure,
                recurringDetail, newChart, withHoldTax);

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
    
    public void setClosedOnDate(final LocalDate closedOnDate) {
        this.closedOnDate = closedOnDate.toDate();
    }

    @Override
    protected boolean isTransferInterestToOtherAccount() {
        return this.accountTermAndPreClosure.isTransferInterestToLinkedAccount();
    }

    public void generateSchedule(final PeriodFrequencyType frequency, final Integer recurringEvery, final Calendar calendar) {
        this.depositScheduleInstallments.clear();
        LocalDate installmentDate = null;
        if (this.isCalendarInherited()) {
            installmentDate = CalendarUtils.getNextScheduleDate(calendar, accountSubmittedOrActivationDate());
        } else {
            installmentDate = depositStartDate();
        }

        int installmentNumber = 1;
        final LocalDate maturityDate = calcualteScheduleTillDate(frequency, recurringEvery);
        final BigDecimal depositAmount = this.recurringDetail.mandatoryRecommendedDepositAmount();
        while (maturityDate.isAfter(installmentDate)) {
            final RecurringDepositScheduleInstallment installment = RecurringDepositScheduleInstallment.installment(this,
                    installmentNumber, installmentDate.toDate(), depositAmount);
            addDepositScheduleInstallment(installment);
            installmentDate = DepositAccountUtils.calculateNextDepositDate(installmentDate, frequency, recurringEvery);
            installmentNumber += 1;
        }
        updateDepositAmount();
    }

    private LocalDate calcualteScheduleTillDate(final PeriodFrequencyType frequency, final Integer recurringEvery) {
        LocalDate tillDate = calculateMaturityDate();
        if (tillDate == null) {
            final LocalDate today = DateUtils.getLocalDateOfTenant();
            tillDate = DepositAccountUtils.calculateNextDepositDate(today, frequency, recurringEvery
                    * (DepositAccountUtils.GENERATE_MINIMUM_NUMBER_OF_FUTURE_INSTALMENTS + 1));
        }
        return tillDate;
    }

    private List<RecurringDepositScheduleInstallment> depositScheduleInstallments() {
        return this.depositScheduleInstallments ;
    }

    private void addDepositScheduleInstallment(final RecurringDepositScheduleInstallment installment) {
        this.depositScheduleInstallments.add(installment) ;
    }
    
    public boolean isCalendarInherited() {
        return this.recurringDetail.isCalendarInherited();
    }

    public void updateOverduePayments(final LocalDate todayDate) {
        LocalDate overdueUptoDate = this.maturityDate();
        if (overdueUptoDate == null || overdueUptoDate.isAfter(todayDate)) {
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

    @Override
    public boolean allowWithdrawal() {
        return this.recurringDetail.allowWithdrawal();
    }

    public boolean adjustAdvanceTowardsFuturePayments() {
        return this.recurringDetail.adjustAdvanceTowardsFuturePayments();
    }

    @Override
    public boolean isTransactionsAllowed() {
        return isActive() || isAccountMatured();
    }

    public DepositAccountRecurringDetail getRecurringDetail() {
        return this.recurringDetail;
    }
    
    class RecurringDepositScheduleInstallmentComparator implements Comparator<RecurringDepositScheduleInstallment> {

        @Override
        public int compare(final RecurringDepositScheduleInstallment o1, final RecurringDepositScheduleInstallment o2) {
            final int comparsion = o1.installmentNumber().compareTo(o2.installmentNumber());
           return comparsion ;
        }
    }
    
    @Override
    public void loadLazyCollections() {
        this.depositScheduleInstallments.size() ;
        super.loadLazyCollections();
        this.chart.getId() ;
    }
}