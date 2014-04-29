/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;

/**
 * Immutable data object representing a Recurring Deposit account.
 */
public class RecurringDepositAccountData extends DepositAccountData {

    // additional fields
    private boolean interestFreePeriodApplicable;
    private Integer interestFreeFromPeriod;
    private Integer interestFreeToPeriod;
    private EnumOptionData interestFreePeriodFrequencyType;
    private boolean preClosurePenalApplicable;
    private BigDecimal preClosurePenalInterest;
    private EnumOptionData preClosurePenalInterestOnType;
    private Integer minDepositTerm;
    private Integer maxDepositTerm;
    private EnumOptionData minDepositTermType;
    private EnumOptionData maxDepositTermType;
    private Integer inMultiplesOfDepositTerm;
    private EnumOptionData inMultiplesOfDepositTermType;
    private BigDecimal depositAmount;
    private BigDecimal maturityAmount;
    private LocalDate maturityDate;
    private Integer depositPeriod;
    private EnumOptionData depositPeriodFrequency;
    private BigDecimal recurringDepositAmount;
    private EnumOptionData recurringDepositType;
    private Integer recurringDepositFrequency;
    private EnumOptionData recurringDepositFrequencyType;
    private LocalDate expectedFirstDepositOnDate;

    // used for account close
    private EnumOptionData onAccountClosure;

    private Collection<EnumOptionData> interestFreePeriodTypeOptions;
    private Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions;
    private Collection<EnumOptionData> recurringDepositTypeOptions;
    private Collection<EnumOptionData> recurringDepositFrequencyTypeOptions;
    private Collection<EnumOptionData> depositTermTypeOptions;
    private Collection<EnumOptionData> inMultiplesOfDepositTermTypeOptions;
    private Collection<EnumOptionData> depositPeriodFrequencyOptions;
    private Collection<SavingsAccountData> savingsAccounts;

    // for account close
    private Collection<EnumOptionData> onAccountClosureOptions;
    private Collection<CodeValueData> paymentTypeOptions;

    public static RecurringDepositAccountData instance(final DepositAccountData depositAccountData,
            final boolean interestFreePeriodApplicable, final Integer interestFreeFromPeriod, final Integer interestFreeToPeriod,
            final EnumOptionData interestFreePeriodFrequencyType, final boolean preClosurePenalApplicable,
            final BigDecimal preClosurePenalInterest, final EnumOptionData preClosurePenalInterestOnType, final Integer minDepositTerm,
            final Integer maxDepositTerm, final EnumOptionData minDepositTermType, final EnumOptionData maxDepositTermType,
            final Integer inMultiplesOfDepositTerm, final EnumOptionData inMultiplesOfDepositTermType, final BigDecimal depositAmount,
            final BigDecimal maturityAmount, final LocalDate maturityDate, final Integer depositPeriod,
            final EnumOptionData depositPeriodFrequency, final BigDecimal recurringDepositAmount,
            final EnumOptionData recurringDepositType, final Integer recurringDepositFrequency,
            final EnumOptionData recurringDepositFrequencyType, final EnumOptionData onAccountClosure,
            final LocalDate expectedFirstDepositOnDate) {

        final Collection<EnumOptionData> interestFreePeriodTypeOptions = null;
        final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions = null;
        final Collection<EnumOptionData> depositTermTypeOptions = null;
        final Collection<EnumOptionData> inMultiplesOfDepositTermTypeOptions = null;
        final Collection<EnumOptionData> recurringDepositTypeOptions = null;
        final Collection<EnumOptionData> recurringDepositFrequencyTypeOptions = null;
        final Collection<EnumOptionData> depositPeriodFrequencyOptions = null;
        final EnumOptionData depositType = SavingsEnumerations.depositType(DepositAccountType.FIXED_DEPOSIT.getValue());
        final Collection<EnumOptionData> onAccountClosureOptions = null;
        final Collection<CodeValueData> paymentTypeOptions = null;
        final Collection<SavingsAccountData> savingsAccountDatas = null;

        return new RecurringDepositAccountData(depositAccountData.id, depositAccountData.accountNo, depositAccountData.externalId,
                depositAccountData.groupId, depositAccountData.groupName, depositAccountData.clientId, depositAccountData.clientName,
                depositAccountData.savingsProductId, depositAccountData.savingsProductName, depositAccountData.fieldOfficerId,
                depositAccountData.fieldOfficerName, depositAccountData.status, depositAccountData.timeline, depositAccountData.currency,
                depositAccountData.nominalAnnualInterestRate, depositAccountData.interestCompoundingPeriodType,
                depositAccountData.interestPostingPeriodType, depositAccountData.interestCalculationType,
                depositAccountData.interestCalculationDaysInYearType, depositAccountData.minRequiredOpeningBalance,
                depositAccountData.lockinPeriodFrequency, depositAccountData.lockinPeriodFrequencyType,
                depositAccountData.withdrawalFeeForTransfers, depositAccountData.summary, depositAccountData.transactions,
                depositAccountData.productOptions, depositAccountData.fieldOfficerOptions,
                depositAccountData.interestCompoundingPeriodTypeOptions, depositAccountData.interestPostingPeriodTypeOptions,
                depositAccountData.interestCalculationTypeOptions, depositAccountData.interestCalculationDaysInYearTypeOptions,
                depositAccountData.lockinPeriodFrequencyTypeOptions, depositAccountData.withdrawalFeeTypeOptions,
                depositAccountData.charges, depositAccountData.chargeOptions, depositAccountData.accountChart,
                depositAccountData.chartTemplate, interestFreePeriodApplicable, interestFreeFromPeriod, interestFreeToPeriod,
                interestFreePeriodFrequencyType, preClosurePenalApplicable, preClosurePenalInterest, preClosurePenalInterestOnType,
                interestFreePeriodTypeOptions, preClosurePenalInterestOnTypeOptions, minDepositTerm, maxDepositTerm, minDepositTermType,
                maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, depositAmount, maturityAmount, maturityDate,
                depositPeriod, depositPeriodFrequency, recurringDepositAmount, depositTermTypeOptions, inMultiplesOfDepositTermTypeOptions,
                recurringDepositType, recurringDepositFrequency, recurringDepositFrequencyType, recurringDepositTypeOptions,
                recurringDepositFrequencyTypeOptions, depositPeriodFrequencyOptions, depositType, onAccountClosure,
                onAccountClosureOptions, paymentTypeOptions, savingsAccountDatas, expectedFirstDepositOnDate);
    }

    public static RecurringDepositAccountData withInterestChart(final RecurringDepositAccountData account,
            final DepositAccountInterestRateChartData accountChart) {
        return new RecurringDepositAccountData(account.id, account.accountNo, account.externalId, account.groupId, account.groupName,
                account.clientId, account.clientName, account.savingsProductId, account.savingsProductName, account.fieldOfficerId,
                account.fieldOfficerName, account.status, account.timeline, account.currency, account.nominalAnnualInterestRate,
                account.interestCompoundingPeriodType, account.interestPostingPeriodType, account.interestCalculationType,
                account.interestCalculationDaysInYearType, account.minRequiredOpeningBalance, account.lockinPeriodFrequency,
                account.lockinPeriodFrequencyType, account.withdrawalFeeForTransfers, account.summary, account.transactions,
                account.productOptions, account.fieldOfficerOptions, account.interestCompoundingPeriodTypeOptions,
                account.interestPostingPeriodTypeOptions, account.interestCalculationTypeOptions,
                account.interestCalculationDaysInYearTypeOptions, account.lockinPeriodFrequencyTypeOptions,
                account.withdrawalFeeTypeOptions, account.charges, account.chargeOptions, accountChart, account.chartTemplate,
                account.interestFreePeriodApplicable, account.interestFreeFromPeriod, account.interestFreeToPeriod,
                account.interestFreePeriodFrequencyType, account.preClosurePenalApplicable, account.preClosurePenalInterest,
                account.preClosurePenalInterestOnType, account.interestFreePeriodTypeOptions, account.preClosurePenalInterestOnTypeOptions,
                account.minDepositTerm, account.maxDepositTerm, account.minDepositTermType, account.maxDepositTermType,
                account.inMultiplesOfDepositTerm, account.inMultiplesOfDepositTermType, account.depositAmount, account.maturityAmount,
                account.maturityDate, account.depositPeriod, account.depositPeriodFrequency, account.recurringDepositAmount,
                account.depositTermTypeOptions, account.inMultiplesOfDepositTermTypeOptions, account.recurringDepositType,
                account.recurringDepositFrequency, account.recurringDepositFrequencyType, account.recurringDepositTypeOptions,
                account.recurringDepositFrequencyTypeOptions, account.depositPeriodFrequencyOptions, account.depositType,
                account.onAccountClosure, account.onAccountClosureOptions, account.paymentTypeOptions, account.savingsAccounts,
                account.expectedFirstDepositOnDate);
    }

    public static RecurringDepositAccountData withTemplateOptions(final RecurringDepositAccountData account,
            final RecurringDepositAccountData template, final Collection<SavingsAccountTransactionData> transactions,
            final Collection<SavingsAccountChargeData> charges) {

        if (template == null) {
            final Collection<DepositProductData> productOptions = null;
            final Collection<StaffData> fieldOfficerOptions = null;
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
            final Collection<EnumOptionData> interestCalculationTypeOptions = null;
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
            final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;
            final Collection<ChargeData> chargeOptions = null;

            final Collection<EnumOptionData> interestFreePeriodTypeOptions = null;
            final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions = null;
            final Collection<EnumOptionData> depositTermTypeOptions = null;
            final Collection<EnumOptionData> inMultiplesOfDepositTermTypeOptions = null;
            final Collection<EnumOptionData> recurringDepositTypeOptions = null;
            final Collection<EnumOptionData> recurringDepositFrequencyTypeOptions = null;
            final Collection<EnumOptionData> depositPeriodFrequencyOptions = null;

            return withTemplateOptions(account, productOptions, fieldOfficerOptions, interestCompoundingPeriodTypeOptions,
                    interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                    lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, transactions, charges, chargeOptions,
                    interestFreePeriodTypeOptions, preClosurePenalInterestOnTypeOptions, depositTermTypeOptions,
                    inMultiplesOfDepositTermTypeOptions, recurringDepositTypeOptions, recurringDepositFrequencyTypeOptions,
                    depositPeriodFrequencyOptions);
        }

        return new RecurringDepositAccountData(account.id, account.accountNo, account.externalId, account.groupId, account.groupName,
                account.clientId, account.clientName, account.savingsProductId, account.savingsProductName, account.fieldOfficerId,
                account.fieldOfficerName, account.status, account.timeline, account.currency, account.nominalAnnualInterestRate,
                account.interestCompoundingPeriodType, account.interestPostingPeriodType, account.interestCalculationType,
                account.interestCalculationDaysInYearType, account.minRequiredOpeningBalance, account.lockinPeriodFrequency,
                account.lockinPeriodFrequencyType, account.withdrawalFeeForTransfers, account.summary, transactions,
                template.productOptions, template.fieldOfficerOptions, template.interestCompoundingPeriodTypeOptions,
                template.interestPostingPeriodTypeOptions, template.interestCalculationTypeOptions,
                template.interestCalculationDaysInYearTypeOptions, template.lockinPeriodFrequencyTypeOptions,
                template.withdrawalFeeTypeOptions, charges, template.chargeOptions, account.accountChart, account.chartTemplate,
                account.interestFreePeriodApplicable, account.interestFreeFromPeriod, account.interestFreeToPeriod,
                account.interestFreePeriodFrequencyType, account.preClosurePenalApplicable, account.preClosurePenalInterest,
                account.preClosurePenalInterestOnType, template.interestFreePeriodTypeOptions,
                template.preClosurePenalInterestOnTypeOptions, account.minDepositTerm, account.maxDepositTerm, account.minDepositTermType,
                account.maxDepositTermType, account.inMultiplesOfDepositTerm, account.inMultiplesOfDepositTermType, account.depositAmount,
                account.maturityAmount, account.maturityDate, account.depositPeriod, account.depositPeriodFrequency,
                account.recurringDepositAmount, template.depositTermTypeOptions, template.inMultiplesOfDepositTermTypeOptions,
                account.recurringDepositType, account.recurringDepositFrequency, account.recurringDepositFrequencyType,
                template.recurringDepositTypeOptions, template.recurringDepositFrequencyTypeOptions,
                template.depositPeriodFrequencyOptions, account.depositType, account.onAccountClosure, account.onAccountClosureOptions,
                account.paymentTypeOptions, account.savingsAccounts, account.expectedFirstDepositOnDate);

    }

    public static RecurringDepositAccountData withTemplateOptions(final RecurringDepositAccountData account,
            final Collection<DepositProductData> productOptions, final Collection<StaffData> fieldOfficerOptions,
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<SavingsAccountTransactionData> transactions, final Collection<SavingsAccountChargeData> charges,
            final Collection<ChargeData> chargeOptions, final Collection<EnumOptionData> interestFreePeriodTypeOptions,
            final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions, final Collection<EnumOptionData> depositTermTypeOptions,
            final Collection<EnumOptionData> inMultiplesOfDepositTermTypeOptions,
            final Collection<EnumOptionData> recurringDepositTypeOptions,
            final Collection<EnumOptionData> recurringDepositFrequencyTypeOptions,
            final Collection<EnumOptionData> depositPeriodFrequencyOptions) {

        return new RecurringDepositAccountData(account.id, account.accountNo, account.externalId, account.groupId, account.groupName,
                account.clientId, account.clientName, account.savingsProductId, account.savingsProductName, account.fieldOfficerId,
                account.fieldOfficerName, account.status, account.timeline, account.currency, account.nominalAnnualInterestRate,
                account.interestCompoundingPeriodType, account.interestPostingPeriodType, account.interestCalculationType,
                account.interestCalculationDaysInYearType, account.minRequiredOpeningBalance, account.lockinPeriodFrequency,
                account.lockinPeriodFrequencyType, account.withdrawalFeeForTransfers, account.summary, transactions, productOptions,
                fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                withdrawalFeeTypeOptions, charges, chargeOptions, account.accountChart, account.chartTemplate,
                account.interestFreePeriodApplicable, account.interestFreeFromPeriod, account.interestFreeToPeriod,
                account.interestFreePeriodFrequencyType, account.preClosurePenalApplicable, account.preClosurePenalInterest,
                account.preClosurePenalInterestOnType, interestFreePeriodTypeOptions, preClosurePenalInterestOnTypeOptions,
                account.minDepositTerm, account.maxDepositTerm, account.minDepositTermType, account.maxDepositTermType,
                account.inMultiplesOfDepositTerm, account.inMultiplesOfDepositTermType, account.depositAmount, account.maturityAmount,
                account.maturityDate, account.depositPeriod, account.depositPeriodFrequency, account.recurringDepositAmount,
                depositTermTypeOptions, inMultiplesOfDepositTermTypeOptions, account.recurringDepositType,
                account.recurringDepositFrequency, account.recurringDepositFrequencyType, recurringDepositTypeOptions,
                recurringDepositFrequencyTypeOptions, depositPeriodFrequencyOptions, account.depositType, account.onAccountClosure,
                account.onAccountClosureOptions, account.paymentTypeOptions, account.savingsAccounts, account.expectedFirstDepositOnDate);
    }

    public static RecurringDepositAccountData withClientTemplate(final Long clientId, final String clientName, final Long groupId,
            final String groupName) {

        final Long id = null;
        final String accountNo = null;
        final String externalId = null;
        final Long productId = null;
        final String productName = null;
        final Long fieldOfficerId = null;
        final String fieldOfficerName = null;
        final SavingsAccountStatusEnumData status = null;
        final SavingsAccountApplicationTimelineData timeline = null;
        final CurrencyData currency = null;
        final BigDecimal nominalAnnualInterestRate = null;
        final EnumOptionData interestPeriodType = null;
        final EnumOptionData interestPostingPeriodType = null;
        final EnumOptionData interestCalculationType = null;
        final EnumOptionData interestCalculationDaysInYearType = null;
        final BigDecimal minRequiredOpeningBalance = null;
        final Integer lockinPeriodFrequency = null;
        final EnumOptionData lockinPeriodFrequencyType = null;
        final boolean withdrawalFeeForTransfers = false;
        final SavingsAccountSummaryData summary = null;
        final Collection<SavingsAccountTransactionData> transactions = null;

        final Collection<DepositProductData> productOptions = null;
        final Collection<StaffData> fieldOfficerOptions = null;
        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;

        final Collection<SavingsAccountChargeData> charges = null;
        final Collection<ChargeData> chargeOptions = null;

        final DepositAccountInterestRateChartData accountChart = null;
        final DepositAccountInterestRateChartData chartTemplate = null;
        final boolean interestFreePeriodApplicable = false;
        final Integer interestFreeFromPeriod = null;
        final Integer interestFreeToPeriod = null;
        final EnumOptionData interestFreePeriodFrequencyType = null;
        final boolean preClosurePenalApplicable = false;
        final BigDecimal preClosurePenalInterest = null;
        final EnumOptionData preClosurePenalInterestOnType = null;
        final Integer minDepositTerm = null;
        final Integer maxDepositTerm = null;
        final EnumOptionData minDepositTermType = null;
        final EnumOptionData maxDepositTermType = null;
        final Integer inMultiplesOfDepositTerm = null;
        final EnumOptionData inMultiplesOfDepositTermType = null;
        final BigDecimal depositAmount = null;
        final BigDecimal maturityAmount = null;
        final LocalDate maturityDate = null;
        final Integer depositPeriod = null;
        final EnumOptionData depositPeriodFrequency = null;
        final BigDecimal recurringDepositAmount = null;
        final EnumOptionData onAccountClosure = null;

        final Collection<EnumOptionData> interestFreePeriodTypeOptions = null;
        final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions = null;
        final Collection<EnumOptionData> depositTermTypeOptions = null;
        final Collection<EnumOptionData> inMultiplesOfDepositTermTypeOptions = null;

        final EnumOptionData recurringDepositType = null;
        final Integer recurringDepositFrequency = null;
        final EnumOptionData recurringDepositFrequencyType = null;
        final Collection<EnumOptionData> recurringDepositTypeOptions = null;
        final Collection<EnumOptionData> recurringDepositFrequencyTypeOptions = null;
        final Collection<EnumOptionData> depositPeriodFrequencyOptions = null;
        final EnumOptionData depositType = SavingsEnumerations.depositType(DepositAccountType.RECURRING_DEPOSIT.getValue());
        final Collection<EnumOptionData> onAccountClosureOptions = null;
        final Collection<CodeValueData> paymentTypeOptions = null;
        final Collection<SavingsAccountData> savingsAccountDatas = null;
        final LocalDate expectedFirstDepositOnDate = null;

        return new RecurringDepositAccountData(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName,
                fieldOfficerId, fieldOfficerName, status, timeline, currency, nominalAnnualInterestRate, interestPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, summary, transactions, productOptions,
                fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                withdrawalFeeTypeOptions, charges, chargeOptions, accountChart, chartTemplate, interestFreePeriodApplicable,
                interestFreeFromPeriod, interestFreeToPeriod, interestFreePeriodFrequencyType, preClosurePenalApplicable,
                preClosurePenalInterest, preClosurePenalInterestOnType, interestFreePeriodTypeOptions,
                preClosurePenalInterestOnTypeOptions, minDepositTerm, maxDepositTerm, minDepositTermType, maxDepositTermType,
                inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, depositAmount, maturityAmount, maturityDate, depositPeriod,
                depositPeriodFrequency, recurringDepositAmount, depositTermTypeOptions, inMultiplesOfDepositTermTypeOptions,
                recurringDepositType, recurringDepositFrequency, recurringDepositFrequencyType, recurringDepositTypeOptions,
                recurringDepositFrequencyTypeOptions, depositPeriodFrequencyOptions, depositType, onAccountClosure,
                onAccountClosureOptions, paymentTypeOptions, savingsAccountDatas, expectedFirstDepositOnDate);
    }

    public static RecurringDepositAccountData preClosureDetails(final Long accountId, BigDecimal maturityAmount,
            final Collection<EnumOptionData> onAccountClosureOptions, final Collection<CodeValueData> paymentTypeOptions, final Collection<SavingsAccountData> savingsAccountDatas) {

        final String accountNo = null;
        final String externalId = null;
        final Long productId = null;
        final String productName = null;
        final Long fieldOfficerId = null;
        final String fieldOfficerName = null;
        final SavingsAccountStatusEnumData status = null;
        final SavingsAccountApplicationTimelineData timeline = null;
        final CurrencyData currency = null;
        final BigDecimal nominalAnnualInterestRate = null;
        final EnumOptionData interestPeriodType = null;
        final EnumOptionData interestPostingPeriodType = null;
        final EnumOptionData interestCalculationType = null;
        final EnumOptionData interestCalculationDaysInYearType = null;
        final BigDecimal minRequiredOpeningBalance = null;
        final Integer lockinPeriodFrequency = null;
        final EnumOptionData lockinPeriodFrequencyType = null;
        final boolean withdrawalFeeForTransfers = false;
        final SavingsAccountSummaryData summary = null;
        final Collection<SavingsAccountTransactionData> transactions = null;

        final Collection<DepositProductData> productOptions = null;
        final Collection<StaffData> fieldOfficerOptions = null;
        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;

        final Collection<SavingsAccountChargeData> charges = null;
        final Collection<ChargeData> chargeOptions = null;

        final DepositAccountInterestRateChartData accountChart = null;
        final DepositAccountInterestRateChartData chartTemplate = null;
        final boolean interestFreePeriodApplicable = false;
        final Integer interestFreeFromPeriod = null;
        final Integer interestFreeToPeriod = null;
        final EnumOptionData interestFreePeriodFrequencyType = null;
        final boolean preClosurePenalApplicable = false;
        final BigDecimal preClosurePenalInterest = null;
        final EnumOptionData preClosurePenalInterestOnType = null;
        final Integer minDepositTerm = null;
        final Integer maxDepositTerm = null;
        final EnumOptionData minDepositTermType = null;
        final EnumOptionData maxDepositTermType = null;
        final Integer inMultiplesOfDepositTerm = null;
        final EnumOptionData inMultiplesOfDepositTermType = null;
        final BigDecimal depositAmount = null;
        final LocalDate maturityDate = null;
        final Integer depositPeriod = null;
        final EnumOptionData depositPeriodFrequency = null;
        final BigDecimal recurringDepositAmount = null;
        final EnumOptionData onAccountClosure = null;

        final Collection<EnumOptionData> interestFreePeriodTypeOptions = null;
        final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions = null;
        final Collection<EnumOptionData> depositTermTypeOptions = null;
        final Collection<EnumOptionData> inMultiplesOfDepositTermTypeOptions = null;

        final EnumOptionData recurringDepositType = null;
        final Integer recurringDepositFrequency = null;
        final EnumOptionData recurringDepositFrequencyType = null;
        final Collection<EnumOptionData> recurringDepositTypeOptions = null;
        final Collection<EnumOptionData> recurringDepositFrequencyTypeOptions = null;
        final Collection<EnumOptionData> depositPeriodFrequencyOptions = null;
        final EnumOptionData depositType = SavingsEnumerations.depositType(DepositAccountType.RECURRING_DEPOSIT.getValue());

        final Long groupId = null;
        final String groupName = null;
        final Long clientId = null;
        final String clientName = null;
        final LocalDate expectedFirstDepositOnDate = null;

        return new RecurringDepositAccountData(accountId, accountNo, externalId, groupId, groupName, clientId, clientName, productId,
                productName, fieldOfficerId, fieldOfficerName, status, timeline, currency, nominalAnnualInterestRate, interestPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, summary, transactions, productOptions,
                fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                withdrawalFeeTypeOptions, charges, chargeOptions, accountChart, chartTemplate, interestFreePeriodApplicable,
                interestFreeFromPeriod, interestFreeToPeriod, interestFreePeriodFrequencyType, preClosurePenalApplicable,
                preClosurePenalInterest, preClosurePenalInterestOnType, interestFreePeriodTypeOptions,
                preClosurePenalInterestOnTypeOptions, minDepositTerm, maxDepositTerm, minDepositTermType, maxDepositTermType,
                inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, depositAmount, maturityAmount, maturityDate, depositPeriod,
                depositPeriodFrequency, recurringDepositAmount, depositTermTypeOptions, inMultiplesOfDepositTermTypeOptions,
                recurringDepositType, recurringDepositFrequency, recurringDepositFrequencyType, recurringDepositTypeOptions,
                recurringDepositFrequencyTypeOptions, depositPeriodFrequencyOptions, depositType, onAccountClosure,
                onAccountClosureOptions, paymentTypeOptions, savingsAccountDatas, expectedFirstDepositOnDate);
    }

    public static RecurringDepositAccountData withClosureTemplateDetails(final RecurringDepositAccountData account,
            final Collection<EnumOptionData> onAccountClosureOptions, final Collection<CodeValueData> paymentTypeOptions, final Collection<SavingsAccountData> savingsAccountDatas) {

        return new RecurringDepositAccountData(account.id, account.accountNo, account.externalId, account.groupId, account.groupName,
                account.clientId, account.clientName, account.savingsProductId, account.savingsProductName, account.fieldOfficerId,
                account.fieldOfficerName, account.status, account.timeline, account.currency, account.nominalAnnualInterestRate,
                account.interestCompoundingPeriodType, account.interestPostingPeriodType, account.interestCalculationType,
                account.interestCalculationDaysInYearType, account.minRequiredOpeningBalance, account.lockinPeriodFrequency,
                account.lockinPeriodFrequencyType, account.withdrawalFeeForTransfers, account.summary, account.transactions,
                account.productOptions, account.fieldOfficerOptions, account.interestCompoundingPeriodTypeOptions,
                account.interestPostingPeriodTypeOptions, account.interestCalculationTypeOptions,
                account.interestCalculationDaysInYearTypeOptions, account.lockinPeriodFrequencyTypeOptions,
                account.withdrawalFeeTypeOptions, account.charges, account.chargeOptions, account.accountChart, account.chartTemplate,
                account.interestFreePeriodApplicable, account.interestFreeFromPeriod, account.interestFreeToPeriod,
                account.interestFreePeriodFrequencyType, account.preClosurePenalApplicable, account.preClosurePenalInterest,
                account.preClosurePenalInterestOnType, account.interestFreePeriodTypeOptions, account.preClosurePenalInterestOnTypeOptions,
                account.minDepositTerm, account.maxDepositTerm, account.minDepositTermType, account.maxDepositTermType,
                account.inMultiplesOfDepositTerm, account.inMultiplesOfDepositTermType, account.depositAmount, account.maturityAmount,
                account.maturityDate, account.depositPeriod, account.depositPeriodFrequency, account.recurringDepositAmount,
                account.depositTermTypeOptions, account.inMultiplesOfDepositTermTypeOptions, account.recurringDepositType,
                account.recurringDepositFrequency, account.recurringDepositFrequencyType, account.recurringDepositTypeOptions,
                account.recurringDepositFrequencyTypeOptions, account.depositPeriodFrequencyOptions, account.depositType,
                account.onAccountClosure, onAccountClosureOptions, paymentTypeOptions, savingsAccountDatas,
                account.expectedFirstDepositOnDate);
    }

    private RecurringDepositAccountData(final Long id, final String accountNo, final String externalId, final Long groupId,
            final String groupName, final Long clientId, final String clientName, final Long productId, final String productName,
            final Long fieldofficerId, final String fieldofficerName, final SavingsAccountStatusEnumData status,
            final SavingsAccountApplicationTimelineData timeline, final CurrencyData currency, final BigDecimal nominalAnnualInterestRate,
            final EnumOptionData interestPeriodType, final EnumOptionData interestPostingPeriodType,
            final EnumOptionData interestCalculationType, final EnumOptionData interestCalculationDaysInYearType,
            final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency,
            final EnumOptionData lockinPeriodFrequencyType, final boolean withdrawalFeeForTransfers,
            final SavingsAccountSummaryData summary, final Collection<SavingsAccountTransactionData> transactions,
            final Collection<DepositProductData> productOptions, final Collection<StaffData> fieldOfficerOptions,
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<SavingsAccountChargeData> charges, final Collection<ChargeData> chargeOptions,
            final DepositAccountInterestRateChartData accountChart, final DepositAccountInterestRateChartData chartTemplate,
            final boolean interestFreePeriodApplicable, final Integer interestFreeFromPeriod, final Integer interestFreeToPeriod,
            final EnumOptionData interestFreePeriodFrequencyType, final boolean preClosurePenalApplicable,
            final BigDecimal preClosurePenalInterest, final EnumOptionData preClosurePenalInterestOnType,
            final Collection<EnumOptionData> interestFreePeriodTypeOptions,
            final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions, final Integer minDepositTerm,
            final Integer maxDepositTerm, final EnumOptionData minDepositTermType, final EnumOptionData maxDepositTermType,
            final Integer inMultiplesOfDepositTerm, final EnumOptionData inMultiplesOfDepositTermType, final BigDecimal depositAmount,
            final BigDecimal maturityAmount, final LocalDate maturityDate, final Integer depositPeriod,
            final EnumOptionData depositPeriodFrequency, final BigDecimal recurringDepositAmount,
            final Collection<EnumOptionData> depositTermTypeOptions, Collection<EnumOptionData> inMultiplesOfDepositTermTypeOptions,
            final EnumOptionData recurringDepositType, final Integer recurringDepositFrequency,
            final EnumOptionData recurringDepositFrequencyType, final Collection<EnumOptionData> recurringDepositTypeOptions,
            final Collection<EnumOptionData> recurringDepositFrequencyTypeOptions,
            final Collection<EnumOptionData> depositPeriodFrequencyOptions, final EnumOptionData depositType,
            final EnumOptionData onAccountClosure, final Collection<EnumOptionData> onAccountClosureOptions,
            final Collection<CodeValueData> paymentTypeOptions, final Collection<SavingsAccountData> savingsAccountDatas,
            final LocalDate expectedFirstDepositOnDate) {

        super(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName, fieldofficerId,
                fieldofficerName, status, timeline, currency, nominalAnnualInterestRate, interestPeriodType, interestPostingPeriodType,
                interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType, withdrawalFeeForTransfers, summary, transactions, productOptions, fieldOfficerOptions,
                interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, charges,
                chargeOptions, accountChart, chartTemplate, depositType);

        this.interestFreePeriodApplicable = interestFreePeriodApplicable;
        this.interestFreeFromPeriod = interestFreeFromPeriod;
        this.interestFreeToPeriod = interestFreeToPeriod;
        this.interestFreePeriodFrequencyType = interestFreePeriodFrequencyType;
        this.preClosurePenalApplicable = preClosurePenalApplicable;
        this.preClosurePenalInterest = preClosurePenalInterest;
        this.preClosurePenalInterestOnType = preClosurePenalInterestOnType;
        this.minDepositTerm = minDepositTerm;
        this.maxDepositTerm = maxDepositTerm;
        this.minDepositTermType = minDepositTermType;
        this.maxDepositTermType = maxDepositTermType;
        this.inMultiplesOfDepositTerm = inMultiplesOfDepositTerm;
        this.inMultiplesOfDepositTermType = inMultiplesOfDepositTermType;
        this.depositAmount = depositAmount;
        this.maturityAmount = maturityAmount;
        this.maturityDate = maturityDate;
        this.depositPeriod = depositPeriod;
        this.depositPeriodFrequency = depositPeriodFrequency;
        this.recurringDepositAmount = recurringDepositAmount;
        this.recurringDepositFrequencyType = recurringDepositFrequencyType;
        this.recurringDepositFrequency = recurringDepositFrequency;
        this.recurringDepositType = recurringDepositType;
        this.expectedFirstDepositOnDate = expectedFirstDepositOnDate;

        // template data
        this.interestFreePeriodTypeOptions = interestFreePeriodTypeOptions;
        this.preClosurePenalInterestOnTypeOptions = preClosurePenalInterestOnTypeOptions;
        this.recurringDepositTypeOptions = recurringDepositTypeOptions;
        this.recurringDepositFrequencyTypeOptions = recurringDepositFrequencyTypeOptions;
        this.depositTermTypeOptions = depositTermTypeOptions;
        this.inMultiplesOfDepositTermTypeOptions = inMultiplesOfDepositTermTypeOptions;
        this.depositPeriodFrequencyOptions = depositPeriodFrequencyOptions;
        this.onAccountClosure = onAccountClosure;
        this.savingsAccounts = savingsAccountDatas;

        // account close template options
        this.onAccountClosureOptions = onAccountClosureOptions;
        this.paymentTypeOptions = paymentTypeOptions;
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }
        final RecurringDepositAccountData rhs = (RecurringDepositAccountData) obj;
        return new EqualsBuilder().append(this.id, rhs.id).append(this.accountNo, rhs.accountNo).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(this.id).append(this.accountNo).toHashCode();
    }

}