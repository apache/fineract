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

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositPeriodParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.maxDepositTermParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.onAccountClosureIdParamName;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChart;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartAssembler;
import org.apache.fineract.portfolio.savings.DepositAccountOnClosureType;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.PreClosurePenalInterestOnType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.apache.fineract.portfolio.savings.domain.interest.SavingsAccountTransactionDetailsForPostingPeriod;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@DiscriminatorValue("200")
public class FixedDepositAccount extends SavingsAccount {

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private DepositAccountTermAndPreClosure accountTermAndPreClosure;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "account")
    protected DepositAccountInterestRateChart chart;

    @Transient
    protected InterestRateChartAssembler chartAssembler;
    @Transient
    private ConfigurationDomainService configurationDomainService;

    protected FixedDepositAccount() {
        //
    }

    public static FixedDepositAccount createNewApplicationForSubmittal(final Client client, final Group group, final SavingsProduct product,
            final Staff fieldOfficer, final String accountNo, final ExternalId externalId, final AccountType accountType,
            final LocalDate submittedOnDate, final AppUser submittedBy, final BigDecimal interestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final Set<SavingsAccountCharge> savingsAccountCharges,
            final DepositAccountTermAndPreClosure accountTermAndPreClosure, final DepositAccountInterestRateChart chart,
            boolean withHoldTax) {

        final SavingsAccountStatusType status = SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL;
        final boolean allowOverdraft = false;
        final BigDecimal overdraftLimit = new BigDecimal(0);
        FixedDepositAccount account = new FixedDepositAccount(client, group, product, fieldOfficer, accountNo, externalId, status,
                accountType, submittedOnDate, submittedBy, interestRate, interestCompoundingPeriodType, interestPostingPeriodType,
                interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType, withdrawalFeeApplicableForTransfer, savingsAccountCharges, accountTermAndPreClosure, chart,
                allowOverdraft, overdraftLimit, withHoldTax);

        return account;
    }

    private FixedDepositAccount(final Client client, final Group group, final SavingsProduct product, final Staff fieldOfficer,
            final String accountNo, final ExternalId externalId, final SavingsAccountStatusType status, final AccountType accountType,
            final LocalDate submittedOnDate, final AppUser submittedBy, final BigDecimal nominalAnnualInterestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final Set<SavingsAccountCharge> savingsAccountCharges,
            final DepositAccountTermAndPreClosure accountTermAndPreClosure, DepositAccountInterestRateChart chart,
            final boolean allowOverdraft, final BigDecimal overdraftLimit, boolean withHoldTax) {

        super(client, group, product, fieldOfficer, accountNo, externalId, status, accountType, submittedOnDate, submittedBy,
                nominalAnnualInterestRate, interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType,
                interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType,
                withdrawalFeeApplicableForTransfer, savingsAccountCharges, allowOverdraft, overdraftLimit, withHoldTax);

        this.accountTermAndPreClosure = accountTermAndPreClosure;
        this.chart = chart;
        if (this.chart != null) {
            this.chart.updateDepositAccountReference(this);
        }
    }

    @Override
    public void modifyApplication(final JsonCommand command, final Map<String, Object> actualChanges) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.modifyApplicationAction);
        super.modifyApplication(command, actualChanges, baseDataValidator);
        final Map<String, Object> termAndPreClosureChanges = accountTermAndPreClosure.update(command, baseDataValidator);
        actualChanges.putAll(termAndPreClosureChanges);
        validateDomainRules(baseDataValidator);
        super.validateInterestPostingAndCompoundingPeriodTypes(baseDataValidator);
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    @Override
    public BigDecimal getEffectiveInterestRateAsFraction(final MathContext mc, final LocalDate interestPostingUpToDate) {
        boolean isPreMatureClosure = false;
        return getEffectiveInterestRateAsFraction(mc, interestPostingUpToDate, isPreMatureClosure);
    }

    protected BigDecimal getEffectiveInterestRateAsFraction(final MathContext mc, final LocalDate interestPostingUpToDate,
            final boolean isPreMatureClosure) {

        // default it to nominalAnnualInterst rate. interest chart overrrides
        // this value.
        BigDecimal applicableInterestRate = this.nominalAnnualInterestRate;
        if (this.chart != null) {
            boolean applyPreMaturePenalty = false;
            BigDecimal penalInterest = BigDecimal.ZERO;
            LocalDate depositCloseDate = calculateMaturityDate();
            if (isPreMatureClosure) {
                if (this.accountTermAndPreClosure.isPreClosurePenalApplicable()) {
                    applyPreMaturePenalty = true;
                    penalInterest = this.accountTermAndPreClosure.depositPreClosureDetail().preClosurePenalInterest();
                    final PreClosurePenalInterestOnType preClosurePenalInterestOnType = this.accountTermAndPreClosure
                            .depositPreClosureDetail().preClosurePenalInterestOnType();
                    if (preClosurePenalInterestOnType.isWholeTerm()) {
                        depositCloseDate = interestCalculatedUpto();
                    } else if (preClosurePenalInterestOnType.isTillPrematureWithdrawal()) {
                        depositCloseDate = interestPostingUpToDate;
                    }
                }
            }

            final BigDecimal depositAmount = accountTermAndPreClosure.depositAmount();
            applicableInterestRate = this.chart.getApplicableInterestRate(depositAmount, depositStartDate(), depositCloseDate, this.client);

            if (applyPreMaturePenalty) {
                applicableInterestRate = applicableInterestRate.subtract(penalInterest);
                applicableInterestRate = applicableInterestRate.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : applicableInterestRate;
            }
        }
        this.nominalAnnualInterestRate = applicableInterestRate;

        return applicableInterestRate.divide(BigDecimal.valueOf(100L), mc);
    }

    public void updateMaturityDateAndAmountBeforeAccountActivation(final MathContext mc, final boolean isPreMatureClosure,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {
        List<SavingsAccountTransaction> allTransactions = new ArrayList<>();
        String refNo = null;
        final Money transactionAmountMoney = Money.of(getCurrency(), this.accountTermAndPreClosure.depositAmount());
        final SavingsAccountTransaction transaction = SavingsAccountTransaction.deposit(null, office(), null,
                this.accountSubmittedOrActivationDate(), transactionAmountMoney, refNo);
        transaction.setRunningBalance(transactionAmountMoney);
        transaction.updateCumulativeBalanceAndDates(this.getCurrency(), interestCalculatedUpto());
        allTransactions.add(transaction);
        updateMaturityDateAndAmount(mc, allTransactions, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth);
    }

    public void updateMaturityDateAndAmount(final MathContext mc, final boolean isPreMatureClosure,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {
        updateMaturityDateAndAmount(mc, retreiveOrderedNonInterestPostingTransactions(), isPreMatureClosure,
                isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);
    }

    public void updateMaturityDateAndAmount(final MathContext mc, final List<SavingsAccountTransaction> transactions,
            final boolean isPreMatureClosure, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth) {
        final LocalDate maturityDate = calculateMaturityDate();
        final LocalDate interestCalculationUpto = maturityDate.minusDays(1);

        // set end of day balance to maturity date for maturity interest
        // calculation
        this.resetAccountTransactionsEndOfDayBalances(transactions, maturityDate);

        final List<PostingPeriod> postingPeriods = calculateInterestPayable(mc, interestCalculationUpto, transactions, isPreMatureClosure,
                isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);

        // reset end of day balance back to today's date
        this.resetAccountTransactionsEndOfDayBalances(transactions, DateUtils.getBusinessLocalDate());

        Money totalInterestPayable = Money.zero(getCurrency());
        for (PostingPeriod postingPeriod : postingPeriods) {
            totalInterestPayable = totalInterestPayable.plus(postingPeriod.getInterestEarned());
        }
        final Money depositAmount = Money.of(getCurrency(), this.accountTermAndPreClosure.depositAmount());
        final Money maturityAmount = depositAmount.plus(totalInterestPayable);

        this.accountTermAndPreClosure.updateMaturityDetails(maturityAmount.getAmount(), maturityDate);
    }

    public void updateMaturityStatus(final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.updateMaturityDetailsAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.ACTIVE.hasStateOf(currentStatus)) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.in.active.state");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (!DateUtils.isDateInTheFuture(maturityDate())) {
            // update account status
            this.status = SavingsAccountStatusType.MATURED.getValue();
            postMaturityInterest(isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);
        }
    }

    public LocalDate calculateMaturityDate() {

        final LocalDate startDate = accountSubmittedOrActivationDate();
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
            final List<SavingsAccountTransaction> transactions, final boolean isPreMatureClosure,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {

        final SavingsPostingInterestPeriodType postingPeriodType = SavingsPostingInterestPeriodType.fromInt(this.interestPostingPeriodType);

        final SavingsCompoundingInterestPeriodType compoundingPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(this.interestCompoundingPeriodType);

        final SavingsInterestCalculationDaysInYearType daysInYearType = SavingsInterestCalculationDaysInYearType
                .fromInt(this.interestCalculationDaysInYearType);
        List<LocalDate> postedAsOnTransactionDates = getManualPostingDates();

        final List<LocalDateInterval> postingPeriodIntervals = this.savingsHelper.determineInterestPostingPeriods(
                accountSubmittedOrActivationDate(), maturityDate, postingPeriodType, financialYearBeginningMonth,
                postedAsOnTransactionDates);

        final List<PostingPeriod> allPostingPeriods = new ArrayList<>();

        Money periodStartingBalance = Money.zero(currency);

        final SavingsInterestCalculationType interestCalculationType = SavingsInterestCalculationType.fromInt(this.interestCalculationType);
        final BigDecimal interestRateAsFraction = getEffectiveInterestRateAsFraction(mc, maturityDate, isPreMatureClosure);
        final Collection<Long> interestPostTransactions = this.savingsHelper.fetchPostInterestTransactionIds(getId());
        boolean isInterestTransfer = false;
        final Money minBalanceForInterestCalculation = Money.of(getCurrency(), minBalanceForInterestCalculation());
        List<SavingsAccountTransactionDetailsForPostingPeriod> savingsAccountTransactionDetailsForPostingPeriodList = toSavingsAccountTransactionDetailsForPostingPeriodList(
                transactions);
        for (final LocalDateInterval periodInterval : postingPeriodIntervals) {
            boolean isUserPosting = false;
            if (postedAsOnTransactionDates.contains(periodInterval.endDate())) {
                isUserPosting = true;
            }
            final PostingPeriod postingPeriod = PostingPeriod.createFrom(periodInterval, periodStartingBalance,
                    savingsAccountTransactionDetailsForPostingPeriodList, this.currency, compoundingPeriodType, interestCalculationType,
                    interestRateAsFraction, daysInYearType.getValue(), maturityDate, interestPostTransactions, isInterestTransfer,
                    minBalanceForInterestCalculation, isSavingsInterestPostingAtCurrentPeriodEnd, isUserPosting,
                    financialYearBeginningMonth);

            periodStartingBalance = postingPeriod.closingBalance();

            allPostingPeriods.add(postingPeriod);
        }

        this.summary.updateFromInterestPeriodSummaries(this.currency, allPostingPeriods);
        this.savingsHelper.calculateInterestForAllPostingPeriods(this.currency, allPostingPeriods, getLockedInUntilDate(),
                isTransferInterestToOtherAccount());
        return allPostingPeriods;
    }

    public void prematureClosure(final AppUser currentUser, final JsonCommand command, final Map<String, Object> actualChanges) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME + DepositsApiConstants.preMatureCloseAction);

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
        if (isAccountLocked(closedDate)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                    .failWithCode("must.be.after.lockin.period");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        if (maturityDate() != null && DateUtils.isAfter(closedDate, maturityDate())) {
            baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                    .failWithCode("must.be.before.maturity.date");
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

        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_CLOSE_ACCOUNT, closedDate);
        this.status = SavingsAccountStatusType.PRE_MATURE_CLOSURE.getValue();

        final Integer onAccountClosureId = command.integerValueOfParameterNamed(onAccountClosureIdParamName);
        final DepositAccountOnClosureType onClosureType = DepositAccountOnClosureType.fromInt(onAccountClosureId);
        this.accountTermAndPreClosure.updateOnAccountClosureStatus(onClosureType);

        /*
         * // withdraw deposit amount before closing the account final Money transactionAmountMoney =
         * Money.of(this.currency, this.getAccountBalance()); final SavingsAccountTransaction withdraw =
         * SavingsAccountTransaction.withdrawal(this, office(), paymentDetail, closedDate, transactionAmountMoney, new
         * Date()); this.transactions.add(withdraw);
         */
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
        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);
    }

    @Override
    public Money activateWithBalance() {
        return Money.of(this.currency, this.accountTermAndPreClosure.depositAmount());
    }

    public void close(final AppUser currentUser, final JsonCommand command, final Map<String, Object> actualChanges) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.closeAction);

        final SavingsAccountStatusType currentStatus = SavingsAccountStatusType.fromInt(this.status);
        if (!SavingsAccountStatusType.MATURED.hasStateOf(currentStatus)) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("not.in.matured.state");
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
        if (DateUtils.isBefore(closedDate, maturityDate())) {
            baseDataValidator.reset().parameter(SavingsApiConstants.closedOnDateParamName).value(closedDate)
                    .failWithCode("must.be.after.account.maturity.date");
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

        validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_CLOSE_ACCOUNT, closedDate);
        this.status = SavingsAccountStatusType.CLOSED.getValue();

        final Integer onAccountClosureId = command.integerValueOfParameterNamed(onAccountClosureIdParamName);
        final DepositAccountOnClosureType onClosureType = DepositAccountOnClosureType.fromInt(onAccountClosureId);
        this.accountTermAndPreClosure.updateOnAccountClosureStatus(onClosureType);

        // // withdraw deposit amount before closing the account
        // final Money transactionAmountMoney = Money.of(this.currency,
        // this.getAccountBalance());
        // final SavingsAccountTransaction withdraw =
        // SavingsAccountTransaction.withdrawal(this, office(), paymentDetail,
        // closedDate,
        // transactionAmountMoney, new Date());
        // this.transactions.add(withdraw);

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
        // this.summary.updateSummary(this.currency,
        // this.savingsAccountTransactionSummaryWrapper, this.transactions);
    }

    public void updateClosedStatus() {
        this.status = SavingsAccountStatusType.CLOSED.getValue();
    }

    public void updateOnAccountClosureStatus(DepositAccountOnClosureType onClosureType) {
        this.accountTermAndPreClosure.updateOnAccountClosureStatus(onClosureType);
    }

    public void postMaturityInterest(final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {
        final LocalDate interestPostingUpToDate = maturityDate();
        final MathContext mc = MathContext.DECIMAL64;
        final boolean isInterestTransfer = false;
        final LocalDate postInterestOnDate = null;
        final boolean backdatedTxnsAllowedTill = false;
        boolean postReversals = false;
        final List<PostingPeriod> postingPeriods = calculateInterestUsing(mc, interestPostingUpToDate, isInterestTransfer,
                isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill,
                postReversals);

        Money interestPostedToDate = Money.zero(this.currency);

        boolean recalucateDailyBalanceDetails = false;

        for (final PostingPeriod interestPostingPeriod : postingPeriods) {

            LocalDate interestPostingTransactionDate = interestPostingPeriod.dateOfPostingTransaction();

            interestPostingTransactionDate = DateUtils.isAfter(interestPostingTransactionDate, interestPostingUpToDate)
                    ? interestPostingUpToDate
                    : interestPostingTransactionDate;

            final Money interestEarnedToBePostedForPeriod = interestPostingPeriod.getInterestEarned();

            interestPostedToDate = interestPostedToDate.plus(interestEarnedToBePostedForPeriod);

            final SavingsAccountTransaction postingTransaction = findInterestPostingTransactionFor(interestPostingTransactionDate);
            if (postingTransaction == null) {
                final SavingsAccountTransaction newPostingTransaction = SavingsAccountTransaction.interestPosting(this, office(),
                        interestPostingTransactionDate, interestEarnedToBePostedForPeriod, interestPostingPeriod.isUserPosting());
                this.transactions.add(newPostingTransaction);
                recalucateDailyBalanceDetails = true;
            } else {
                final boolean correctionRequired = postingTransaction.hasNotAmount(interestEarnedToBePostedForPeriod);
                if (correctionRequired) {
                    postingTransaction.reverse();
                    final SavingsAccountTransaction newPostingTransaction = SavingsAccountTransaction.interestPosting(this, office(),
                            interestPostingTransactionDate, interestEarnedToBePostedForPeriod, interestPostingPeriod.isUserPosting());
                    this.transactions.add(newPostingTransaction);
                    recalucateDailyBalanceDetails = true;
                }
            }
        }
        recalucateDailyBalanceDetails = applyWithholdTaxForDepositAccounts(interestPostingUpToDate, recalucateDailyBalanceDetails,
                backdatedTxnsAllowedTill);
        if (recalucateDailyBalanceDetails) {
            // update existing transactions so derived balance fields are
            // correct.
            recalculateDailyBalances(Money.zero(this.currency), interestPostingUpToDate, backdatedTxnsAllowedTill, postReversals);
        }

        this.summary.updateSummary(this.currency, this.savingsAccountTransactionSummaryWrapper, this.transactions);
    }

    public void postPreMaturityInterest(final LocalDate accountCloseDate, final boolean isPreMatureClosure,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {

        Money interestPostedToDate = totalInterestPosted();
        // calculate interest before one day of closure date
        final LocalDate interestCalculatedToDate = accountCloseDate.minusDays(1);
        final Money interestOnMaturity = calculatePreMatureInterest(interestCalculatedToDate,
                retreiveOrderedNonInterestPostingTransactions(), isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth);
        boolean recalucateDailyBalance = false;

        // post remaining interest
        final Money remainigInterestToBePosted = interestOnMaturity.minus(interestPostedToDate);
        final boolean backdatedTxnsAllowedTill = false;
        if (!remainigInterestToBePosted.isZero()) {
            final boolean postInterestAsOn = false;
            final SavingsAccountTransaction newPostingTransaction = SavingsAccountTransaction.interestPosting(this, office(),
                    accountCloseDate, remainigInterestToBePosted, postInterestAsOn);
            this.transactions.add(newPostingTransaction);
            recalucateDailyBalance = true;
        }

        recalucateDailyBalance = applyWithholdTaxForDepositAccounts(accountCloseDate, recalucateDailyBalance, backdatedTxnsAllowedTill);
        boolean postReversals = false;
        if (recalucateDailyBalance) {
            // update existing transactions so derived balance fields are
            // correct.
            recalculateDailyBalances(Money.zero(this.currency), accountCloseDate, backdatedTxnsAllowedTill, postReversals);
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

        return interestOnMaturity;
    }

    public void postInterest(final MathContext mc, final LocalDate postingDate, boolean isInterestTransfer,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth,
            final LocalDate postInterestOnDate, final boolean backdatedTxnsAllowedTill) {
        final LocalDate interestPostingUpToDate = interestPostingUpToDate(postingDate);
        boolean postReversals = false;
        super.postInterest(mc, interestPostingUpToDate, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill, postReversals);
    }

    @Override
    public List<PostingPeriod> calculateInterestUsing(final MathContext mc, final LocalDate postingDate, boolean isInterestTransfer,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth,
            final LocalDate postAsInterestOn, final boolean backdatedTxnsAllowedTill, final boolean postReversals) {
        final LocalDate interestPostingUpToDate = interestPostingUpToDate(postingDate);
        return super.calculateInterestUsing(mc, interestPostingUpToDate, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, postAsInterestOn, backdatedTxnsAllowedTill, postReversals);
    }

    private LocalDate interestPostingUpToDate(final LocalDate interestPostingDate) {
        LocalDate interestPostingUpToDate = interestPostingDate;
        final LocalDate uptoMaturityDate = interestCalculatedUpto();
        if (uptoMaturityDate != null && DateUtils.isBefore(uptoMaturityDate, interestPostingDate)) {
            interestPostingUpToDate = uptoMaturityDate;
        }
        return interestPostingUpToDate;
    }

    public LocalDate maturityDate() {
        return this.accountTermAndPreClosure.getMaturityDate();
    }

    public BigDecimal maturityAmount() {
        return this.accountTermAndPreClosure.maturityAmount();
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
    public Map<String, Object> activate(final AppUser currentUser, final JsonCommand command) {
        final Map<String, Object> actualChanges = super.activate(currentUser, command);

        // if (isAccountLocked(calculateMaturityDate())) {
        // final List<ApiParameterError> dataValidationErrors = new
        // ArrayList<ApiParameterError>();
        // final DataValidatorBuilder baseDataValidator = new
        // DataValidatorBuilder(dataValidationErrors)
        // .resource(FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME);
        // baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("deposit.period.must.be.greater.than.lock.in.period",
        // "Deposit period must be greater than account lock-in period.");
        // if (!dataValidationErrors.isEmpty()) { throw new
        // PlatformApiDataValidationException(dataValidationErrors); }
        // }
        return actualChanges;
    }

    private LocalDate depositStartDate() {
        // TODO: Support to add deposit start date which can be a date after
        // account activation date.
        final LocalDate depositStartDate = accountSubmittedOrActivationDate();
        return depositStartDate;
    }

    private LocalDate interestCalculatedUpto() {
        LocalDate uptoMaturityDate = calculateMaturityDate();
        if (uptoMaturityDate != null) {
            // interest should not be calculated for maturity day
            uptoMaturityDate = uptoMaturityDate.minusDays(1);
        }
        return uptoMaturityDate;
    }

    public void validateDomainRules() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME);
        validateDomainRules(baseDataValidator);
        super.validateInterestPostingAndCompoundingPeriodTypes(baseDataValidator);
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private void validateDomainRules(final DataValidatorBuilder baseDataValidator) {

        final boolean isMinTermGreaterThanMax = this.accountTermAndPreClosure.depositTermDetail()
                .isMinDepositTermGreaterThanMaxDepositTerm();
        final boolean isValidDepositPeriod = this.accountTermAndPreClosure.depositTermDetail().isDepositBetweenMinAndMax(depositStartDate(),
                calculateMaturityDate());
        // deposit period should be within min and max deposit term
        if (isMinTermGreaterThanMax) {
            final Integer maxTerm = this.accountTermAndPreClosure.depositTermDetail().maxDepositTerm();
            baseDataValidator.reset().parameter(maxDepositTermParamName).value(maxTerm)
                    .failWithCodeNoParameterAddedToErrorCode("max.term.lessthan.min.term");
        }
        final Integer depositPeriod = this.accountTermAndPreClosure.depositPeriod();
        final SavingsPeriodFrequencyType depositPeriodFrequencyType = this.accountTermAndPreClosure.depositPeriodFrequencyType();

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

        if (this.chart != null) {
            final LocalDate chartFromDate = this.chart.getFromDate();
            LocalDate chartEndDate = this.chart.getEndDate();
            chartEndDate = chartEndDate == null ? DateUtils.getBusinessLocalDate() : chartEndDate;

            final LocalDateInterval chartInterval = LocalDateInterval.create(chartFromDate, chartEndDate);
            if (!chartInterval.contains(accountSubmittedOrActivationDate())) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("no.valid.interest.rate.slab.available");
            }

            final BigDecimal depositAmount = accountTermAndPreClosure.depositAmount();
            BigDecimal applicableInterestRate = this.chart.getApplicableInterestRate(depositAmount, depositStartDate(),
                    calculateMaturityDate(), this.client);

            if (applicableInterestRate.compareTo(BigDecimal.ZERO) == 0) {
                baseDataValidator.reset()
                        .failWithCodeNoParameterAddedToErrorCode("no.applicable.interest.rate.is.found.based.on.amount.and.deposit.period");
            }

        } else if (this.nominalAnnualInterestRate == null || this.nominalAnnualInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            baseDataValidator.reset().parameter(DepositsApiConstants.nominalAnnualInterestRateParamName).value(nominalAnnualInterestRate)
                    .failWithCodeNoParameterAddedToErrorCode("valid.interest.chart.or.nominal.interest.rate.required");
        }

    }

    public boolean isReinvestOnClosure() {
        return this.accountTermAndPreClosure.isReinvestOnClosure();
    }

    public boolean isTransferToSavingsOnClosure() {
        return this.accountTermAndPreClosure.isTransferToSavingsOnClosure();
    }

    public Integer getOnAccountClosureId() {
        return this.accountTermAndPreClosure.getOnAccountClosureType();
    }

    public Long getTransferToSavingsAccountId() {
        return this.accountTermAndPreClosure.getTransferToSavingsAccountId();
    }

    public FixedDepositAccount reInvest(BigDecimal depositAmount) {

        final DepositAccountTermAndPreClosure newAccountTermAndPreClosure = this.accountTermAndPreClosure.copy(depositAmount);
        final SavingsProduct product = this.product;
        final InterestRateChart productChart = product.applicableChart(getClosedOnDate());
        final DepositAccountInterestRateChart newChart = DepositAccountInterestRateChart.from(productChart);

        final AccountType accountType = AccountType.fromInt(this.accountType);
        final SavingsPostingInterestPeriodType postingPeriodType = SavingsPostingInterestPeriodType.fromInt(this.interestPostingPeriodType);
        final SavingsCompoundingInterestPeriodType compoundingPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(this.interestCompoundingPeriodType);
        final SavingsInterestCalculationType interestCalculationType = SavingsInterestCalculationType.fromInt(this.interestCalculationType);
        final SavingsInterestCalculationDaysInYearType daysInYearType = SavingsInterestCalculationDaysInYearType
                .fromInt(this.interestCalculationDaysInYearType);
        final BigDecimal minRequiredOpeningBalance = null;
        final BigDecimal interestRate = BigDecimal.ZERO;
        final Set<SavingsAccountCharge> savingsAccountCharges = null;
        final SavingsPeriodFrequencyType lockinPeriodFrequencyType = SavingsPeriodFrequencyType.fromInt(this.lockinPeriodFrequencyType);
        final Integer lockinPeriodFrequency = this.lockinPeriodFrequency;
        final boolean withdrawalFeeApplicableForTransfer = false;
        final String accountNumber = null;
        final boolean withHoldTax = this.withHoldTax;
        final FixedDepositAccount reInvestedAccount = FixedDepositAccount.createNewApplicationForSubmittal(client, group, product,
                savingsOfficer, accountNumber, externalId, accountType, getClosedOnDate(), closedBy, interestRate, compoundingPeriodType,
                postingPeriodType, interestCalculationType, daysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType, withdrawalFeeApplicableForTransfer, savingsAccountCharges, newAccountTermAndPreClosure, newChart,
                withHoldTax);

        newAccountTermAndPreClosure.updateAccountReference(reInvestedAccount);
        newChart.updateDepositAccountReference(reInvestedAccount);

        return reInvestedAccount;

    }

    @Override
    protected boolean isTransferInterestToOtherAccount() {
        return this.accountTermAndPreClosure.isTransferInterestToLinkedAccount();
    }

    @Override
    public boolean allowDeposit() {
        return false;
    }

    @Override
    public boolean allowWithdrawal() {
        return false;
    }

    @Override
    public boolean allowModify() {
        return false;
    }

    @Override
    public boolean isTransactionsAllowed() {
        return isActive() || isAccountMatured();
    }

    private boolean isAccountMatured() {
        return SavingsAccountStatusType.fromInt(status).isMatured();
    }

    @Override
    public BigDecimal minBalanceForInterestCalculation() {
        return null;
    }

    @Override
    public void loadLazyCollections() {
        super.loadLazyCollections();
        this.chart.getId();
    }

    public BigDecimal getDepositAmount() {
        return this.accountTermAndPreClosure.depositAmount();
    }

    @Override
    public BigDecimal getAccountBalance() {
        return this.summary.getAccountBalance(this.currency).getAmount();
    }

    public boolean isMatured() {
        return SavingsAccountStatusType.MATURED.getValue().equals(this.status);
    }

}
