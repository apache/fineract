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
package org.apache.fineract.portfolio.loanproduct.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.fineract.accounting.common.AccountingEnumerations;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.common.service.CommonEnumerations;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateData;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.loanaccount.data.LoanInterestRecalculationData;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductConfigurableAttributes;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.joda.time.LocalDate;
import org.springframework.util.CollectionUtils;

/**
 * Immutable data object to represent loan products.
 */
public class LoanProductData {

    private final Long id;
    private final String name;
    private final String shortName;
    private final String description;
    private final Long fundId;
    private final String fundName;
    private final boolean includeInBorrowerCycle;
    private final boolean useBorrowerCycle;
    private final LocalDate startDate;
    private final LocalDate closeDate;
    private final String status;
    private final String externalId;
    // terms
    private final CurrencyData currency;
    private final BigDecimal principal;
    private final BigDecimal minPrincipal;
    private final BigDecimal maxPrincipal;
    private final Integer numberOfRepayments;
    private final Integer minNumberOfRepayments;
    private final Integer maxNumberOfRepayments;
    private final Integer repaymentEvery;
    private final EnumOptionData repaymentFrequencyType;
    private final BigDecimal interestRatePerPeriod;
    private final BigDecimal minInterestRatePerPeriod;
    private final BigDecimal maxInterestRatePerPeriod;
    private final EnumOptionData interestRateFrequencyType;
    private final BigDecimal annualInterestRate;

    private final boolean isLinkedToFloatingInterestRates;
    private final Integer floatingRateId;
    private final String floatingRateName;
    private final BigDecimal interestRateDifferential;
    private final BigDecimal minDifferentialLendingRate;
    private final BigDecimal defaultDifferentialLendingRate;
    private final BigDecimal maxDifferentialLendingRate;
    private final boolean isFloatingInterestRateCalculationAllowed;

    // Variable Installments Settings
    private final boolean allowVariableInstallments;
    private final Integer minimumGap;
    private final Integer maximumGap;

    // settings
    private final EnumOptionData amortizationType;
    private final EnumOptionData interestType;
    private final EnumOptionData interestCalculationPeriodType;
    private final Boolean allowPartialPeriodInterestCalcualtion;
    private final BigDecimal inArrearsTolerance;
    private final Long transactionProcessingStrategyId;
    private final String transactionProcessingStrategyName;
    private final Integer graceOnPrincipalPayment;
    private final Integer recurringMoratoriumOnPrincipalPeriods;
    private final Integer graceOnInterestPayment;
    private final Integer graceOnInterestCharged;
    private final Integer graceOnArrearsAgeing;
    private final Integer overdueDaysForNPA;
    private final EnumOptionData daysInMonthType;
    private final EnumOptionData daysInYearType;
    private final boolean isInterestRecalculationEnabled;
    private final LoanProductInterestRecalculationData interestRecalculationData;
    private final Integer minimumDaysBetweenDisbursalAndFirstRepayment;
    private final boolean canDefineInstallmentAmount;
    private final Integer installmentAmountInMultiplesOf;

    // charges
    private final Collection<ChargeData> charges;

    private final Collection<LoanProductBorrowerCycleVariationData> principalVariationsForBorrowerCycle;
    private final Collection<LoanProductBorrowerCycleVariationData> interestRateVariationsForBorrowerCycle;
    private final Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariationsForBorrowerCycle;
    // accounting
    private final EnumOptionData accountingRule;
    private final boolean canUseForTopup;
    private Map<String, Object> accountingMappings;
    private Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings;
    private Collection<ChargeToGLAccountMapper> feeToIncomeAccountMappings;
    private Collection<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings;

    // template related
    private final Collection<FundData> fundOptions;
    @SuppressWarnings("unused")
    private final Collection<PaymentTypeData> paymentTypeOptions;
    @SuppressWarnings("unused")
    private final Collection<CurrencyData> currencyOptions;
    private final List<EnumOptionData> repaymentFrequencyTypeOptions;
    private final List<EnumOptionData> interestRateFrequencyTypeOptions;
    private final List<EnumOptionData> amortizationTypeOptions;
    private final List<EnumOptionData> interestTypeOptions;
    private final List<EnumOptionData> interestCalculationPeriodTypeOptions;
    private final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions;
    private final Collection<ChargeData> chargeOptions;
    @SuppressWarnings("unused")
    private final Collection<ChargeData> penaltyOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> accountingRuleOptions;
    @SuppressWarnings("unused")
    private final Map<String, List<GLAccountData>> accountingMappingOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> valueConditionTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> daysInMonthTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> daysInYearTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> interestRecalculationCompoundingTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> interestRecalculationNthDayTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> interestRecalculationDayOfWeekTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> rescheduleStrategyTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> preClosureInterestCalculationStrategyOptions;

    @SuppressWarnings("unused")
    private final List<EnumOptionData> interestRecalculationFrequencyTypeOptions;
    @SuppressWarnings("unused")
    private final List<FloatingRateData> floatingRateOptions;

    private final Boolean multiDisburseLoan;
    private final Integer maxTrancheCount;
    private final BigDecimal outstandingLoanBalance;
    private final BigDecimal principalThresholdForLastInstallment;

    private final Boolean holdGuaranteeFunds;
    private final LoanProductGuaranteeData productGuaranteeData;
    private final Boolean accountMovesOutOfNPAOnlyOnArrearsCompletion;
    private LoanProductConfigurableAttributes allowAttributeOverrides;
    private final boolean syncExpectedWithDisbursementDate;
    private final boolean isEqualAmortization;

    /**
     * Used when returning lookup information about loan product for dropdowns.
     */
    public static LoanProductData lookup(final Long id, final String name, final Boolean multiDisburseLoan ) {
        final String shortName = null;
        final String description = null;
        final CurrencyData currency = null;
        final BigDecimal principal = null;
        final BigDecimal minPrincipal = null;
        final BigDecimal maxPrincipal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer minNumberOfRepayments = null;
        final Integer maxNumberOfRepayments = null;
        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal minInterestRatePerPeriod = null;
        final BigDecimal maxInterestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
        final boolean isLinkedToFloatingInterestRates = false;
        final Integer floatingRateId = null;
        final String floatingRateName = null;
        final BigDecimal interestRateDifferential = null;
        final BigDecimal minDifferentialLendingRate = null;
        final BigDecimal defaultDifferentialLendingRate = null;
        final BigDecimal maxDifferentialLendingRate = null;
        final boolean isFloatingInterestRateCalculationAllowed = false;
        final boolean isVariableInstallmentsAllowed = false;
        final Integer minimumGap = null;
        final Integer maximumGap = null;
        final EnumOptionData repaymentFrequencyType = null;
        final EnumOptionData interestRateFrequencyType = null;
        final EnumOptionData amortizationType = null;
        final EnumOptionData interestType = null;
        final EnumOptionData interestCalculationPeriodType = null;
        final Boolean allowPartialPeriodInterestCalcualtion = null;
        final Long fundId = null;
        final String fundName = null;
        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;
        final Integer graceOnPrincipalPayment = null;
        final Integer recurringMoratoriumOnPrincipalPeriods = null;
        final Integer graceOnInterestPayment = null;
        final Integer graceOnInterestCharged = null;
        final Integer graceOnArrearsAgeing = null;
        final Integer overdueDaysForNPA = null;
        final Collection<ChargeData> charges = null;
        final Collection<LoanProductBorrowerCycleVariationData> principalVariations = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> interestRateVariations = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariations = new ArrayList<>(1);
        final EnumOptionData accountingType = null;
        final boolean includeInBorrowerCycle = false;
        final boolean useBorrowerCycle = false;
        final LocalDate startDate = null;
        final LocalDate closeDate = null;
        final String status = null;
        final String externalId = null;
        final Integer maxTrancheCount = null;
        final BigDecimal outstandingLoanBalance = null;
        final LoanProductGuaranteeData productGuaranteeData = null;
        final Boolean holdGuaranteeFunds = false;
        final BigDecimal principalThresholdForLastInstallment = null;
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = false;

        final EnumOptionData daysInMonthType = null;
        final EnumOptionData daysInYearType = null;
        final boolean isInterestRecalculationEnabled = false;
        final LoanProductInterestRecalculationData interestRecalculationData = null;
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = null;
        final boolean canDefineInstallmentAmount = false;
        final Integer installmentAmountInMultiplesOf = null;
        final LoanProductConfigurableAttributes loanProductConfigurableAttributes = null;
        final boolean syncExpectedWithDisbursementDate = false;
        final boolean canUseForTopup = false;
        final boolean isEqualAmortization = false;
        return new LoanProductData(id, name, shortName, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType,
                amortizationType, interestType, interestCalculationPeriodType, allowPartialPeriodInterestCalcualtion, fundId, fundName,
                transactionProcessingStrategyId, transactionProcessingStrategyName, graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment,
                graceOnInterestCharged, charges, accountingType, includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status,
                externalId, principalVariations, interestRateVariations, numberOfRepaymentVariations, multiDisburseLoan, maxTrancheCount,
                outstandingLoanBalance, graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType,
                isInterestRecalculationEnabled, interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment,
                holdGuaranteeFunds, productGuaranteeData, principalThresholdForLastInstallment,
                accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount, installmentAmountInMultiplesOf,
                loanProductConfigurableAttributes, isLinkedToFloatingInterestRates, floatingRateId, floatingRateName,
                interestRateDifferential, minDifferentialLendingRate, defaultDifferentialLendingRate, maxDifferentialLendingRate,
                isFloatingInterestRateCalculationAllowed, isVariableInstallmentsAllowed, minimumGap, maximumGap,
                syncExpectedWithDisbursementDate, canUseForTopup, isEqualAmortization);

    }

    public static LoanProductData lookupWithCurrency(final Long id, final String name, final CurrencyData currency) {
        final String shortName = null;
        final String description = null;
        final BigDecimal principal = null;
        final BigDecimal minPrincipal = null;
        final BigDecimal maxPrincipal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer minNumberOfRepayments = null;
        final Integer maxNumberOfRepayments = null;
        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal minInterestRatePerPeriod = null;
        final BigDecimal maxInterestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
        final boolean isLinkedToFloatingInterestRates = false;
        final Integer floatingRateId = null;
        final String floatingRateName = null;
        final BigDecimal interestRateDifferential = null;
        final BigDecimal minDifferentialLendingRate = null;
        final BigDecimal defaultDifferentialLendingRate = null;
        final BigDecimal maxDifferentialLendingRate = null;
        final boolean isFloatingInterestRateCalculationAllowed = false;
        final boolean isVariableInstallmentsAllowed = false;
        final Integer minimumGap = null;
        final Integer maximumGap = null;
        final EnumOptionData repaymentFrequencyType = null;
        final EnumOptionData interestRateFrequencyType = null;
        final EnumOptionData amortizationType = null;
        final EnumOptionData interestType = null;
        final EnumOptionData interestCalculationPeriodType = null;
        final Boolean allowPartialPeriodInterestCalcualtion = null;
        final Long fundId = null;
        final String fundName = null;
        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;
        final Integer graceOnPrincipalPayment = null;
        final Integer recurringMoratoriumOnPrincipalPeriods = null;
        final Integer graceOnInterestPayment = null;
        final Integer graceOnInterestCharged = null;
        final Integer graceOnArrearsAgeing = null;
        final Integer overdueDaysForNPA = null;

        final Collection<ChargeData> charges = null;
        final EnumOptionData accountingType = null;
        final boolean includeInBorrowerCycle = false;
        final boolean useBorrowerCycle = false;
        final LocalDate startDate = null;
        final LocalDate closeDate = null;
        final String status = null;
        final String externalId = null;

        final Collection<LoanProductBorrowerCycleVariationData> principalVariations = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> interestRateVariations = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariations = new ArrayList<>(1);
        final Boolean multiDisburseLoan = null;
        final Integer maxTrancheCount = null;
        final BigDecimal outstandingLoanBalance = null;

        final EnumOptionData daysInMonthType = null;
        final EnumOptionData daysInYearType = null;
        final boolean isInterestRecalculationEnabled = false;
        final LoanProductInterestRecalculationData interestRecalculationData = null;
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = null;
        final Boolean holdGuaranteeFunds = false;
        final LoanProductGuaranteeData productGuaranteeData = null;
        final BigDecimal principalThresholdForLastInstallment = null;
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = false;
        final boolean canDefineInstallmentAmount = false;
        final Integer installmentAmountInMultiplesOf = null;
        final LoanProductConfigurableAttributes loanProductConfigurableAttributes = null;
        final boolean syncExpectedWithDisbursementDate = false;
        final boolean canUseForTopup = false;
        final boolean isEqualAmortization = false;

        return new LoanProductData(id, name, shortName, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType,
                amortizationType, interestType, interestCalculationPeriodType, allowPartialPeriodInterestCalcualtion, fundId, fundName,
                transactionProcessingStrategyId, transactionProcessingStrategyName, graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment,
                graceOnInterestCharged, charges, accountingType, includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status,
                externalId, principalVariations, interestRateVariations, numberOfRepaymentVariations, multiDisburseLoan, maxTrancheCount,
                outstandingLoanBalance, graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType,
                isInterestRecalculationEnabled, interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment,
                holdGuaranteeFunds, productGuaranteeData, principalThresholdForLastInstallment,
                accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount, installmentAmountInMultiplesOf,
                loanProductConfigurableAttributes, isLinkedToFloatingInterestRates, floatingRateId, floatingRateName,
                interestRateDifferential, minDifferentialLendingRate, defaultDifferentialLendingRate, maxDifferentialLendingRate,
                isFloatingInterestRateCalculationAllowed, isVariableInstallmentsAllowed, minimumGap, maximumGap,
                syncExpectedWithDisbursementDate, canUseForTopup, isEqualAmortization);

    }

    public static LoanProductData sensibleDefaultsForNewLoanProductCreation() {
        final Long id = null;
        final String name = null;
        final String shortName = null;
        final String description = null;
        final CurrencyData currency = CurrencyData.blank();
        final BigDecimal principal = null;
        final BigDecimal minPrincipal = null;
        final BigDecimal maxPrincipal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer minNumberOfRepayments = null;
        final Integer maxNumberOfRepayments = null;

        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal minInterestRatePerPeriod = null;
        final BigDecimal maxInterestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
        final boolean isLinkedToFloatingInterestRates = false;
        final Integer floatingRateId = null;
        final String floatingRateName = null;
        final BigDecimal interestRateDifferential = null;
        final BigDecimal minDifferentialLendingRate = null;
        final BigDecimal defaultDifferentialLendingRate = null;
        final BigDecimal maxDifferentialLendingRate = null;
        final boolean isFloatingInterestRateCalculationAllowed = false;
        final boolean isVariableInstallmentsAllowed = false;
        final Integer minimumGap = null;
        final Integer maximumGap = null;
        final EnumOptionData repaymentFrequencyType = LoanEnumerations.repaymentFrequencyType(PeriodFrequencyType.MONTHS);
        final EnumOptionData interestRateFrequencyType = LoanEnumerations.interestRateFrequencyType(PeriodFrequencyType.MONTHS);
        final EnumOptionData amortizationType = LoanEnumerations.amortizationType(AmortizationMethod.EQUAL_INSTALLMENTS);
        final EnumOptionData interestType = LoanEnumerations.interestType(InterestMethod.DECLINING_BALANCE);
        final EnumOptionData interestCalculationPeriodType = LoanEnumerations
                .interestCalculationPeriodType(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD);
        final Boolean allowPartialPeriodInterestCalcualtion = null;
        final Long fundId = null;
        final String fundName = null;
        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;

        final Integer graceOnPrincipalPayment = null;
        final Integer recurringMoratoriumOnPrincipalPeriods = null;
        final Integer graceOnInterestPayment = null;
        final Integer graceOnInterestCharged = null;
        final Integer graceOnArrearsAgeing = null;
        final Integer overdueDaysForNPA = null;

        final Collection<ChargeData> charges = null;
        final Collection<LoanProductBorrowerCycleVariationData> principalVariationsForBorrowerCycle = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> interestRateVariationsForBorrowerCycle = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>(1);

        final EnumOptionData accountingType = AccountingEnumerations.accountingRuleType(AccountingRuleType.NONE);
        final boolean includeInBorrowerCycle = false;
        final boolean useBorrowerCycle = false;
        final LocalDate startDate = null;
        final LocalDate closeDate = null;
        final String status = null;
        final String externalId = null;
        final Boolean multiDisburseLoan = null;
        final Integer maxTrancheCount = null;
        final BigDecimal outstandingLoanBalance = null;

        final EnumOptionData daysInMonthType = CommonEnumerations.daysInMonthType(DaysInMonthType.ACTUAL);
        final EnumOptionData daysInYearType = CommonEnumerations.daysInYearType(DaysInYearType.ACTUAL);
        final boolean isInterestRecalculationEnabled = false;
        final LoanProductInterestRecalculationData interestRecalculationData = LoanProductInterestRecalculationData
                .sensibleDefaultsForNewLoanProductCreation();
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = null;
        final Boolean holdGuaranteeFunds = false;
        final LoanProductGuaranteeData productGuaranteeData = null;
        final BigDecimal principalThresholdForLastInstallment = null;
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = false;
        final boolean canDefineInstallmentAmount = false;
        final Integer installmentAmountInMultiplesOf = null;
        final LoanProductConfigurableAttributes loanProductConfigurableAttributes = null;
        final boolean syncExpectedWithDisbursementDate = false;
        final boolean canUseForTopup = false;
        final boolean isEqualAmortization = false;

        return new LoanProductData(id, name, shortName, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType,
                amortizationType, interestType, interestCalculationPeriodType, allowPartialPeriodInterestCalcualtion, fundId, fundName,
                transactionProcessingStrategyId, transactionProcessingStrategyName, graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment,
                graceOnInterestCharged, charges, accountingType, includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status,
                externalId, principalVariationsForBorrowerCycle, interestRateVariationsForBorrowerCycle,
                numberOfRepaymentVariationsForBorrowerCycle, multiDisburseLoan, maxTrancheCount, outstandingLoanBalance,
                graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment, holdGuaranteeFunds, productGuaranteeData,
                principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount,
                installmentAmountInMultiplesOf, loanProductConfigurableAttributes, isLinkedToFloatingInterestRates, floatingRateId,
                floatingRateName, interestRateDifferential, minDifferentialLendingRate, defaultDifferentialLendingRate,
                maxDifferentialLendingRate, isFloatingInterestRateCalculationAllowed, isVariableInstallmentsAllowed, minimumGap, maximumGap,
                syncExpectedWithDisbursementDate, canUseForTopup, isEqualAmortization);

    }
    
    public static LoanProductData loanProductWithFloatingRates(final Long id, final String name,
            final boolean isLinkedToFloatingInterestRates, final Integer floatingRateId, final String floatingRateName,
            final BigDecimal interestRateDifferential, final BigDecimal minDifferentialLendingRate,
            final BigDecimal defaultDifferentialLendingRate, final BigDecimal maxDifferentialLendingRate,
            final boolean isFloatingInterestRateCalculationAllowed) {
        final String shortName = null;
        final String description = null;
        final CurrencyData currency = CurrencyData.blank();
        final BigDecimal principal = null;
        final BigDecimal minPrincipal = null;
        final BigDecimal maxPrincipal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer minNumberOfRepayments = null;
        final Integer maxNumberOfRepayments = null;

        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal minInterestRatePerPeriod = null;
        final BigDecimal maxInterestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
        final boolean isVariableInstallmentsAllowed = false;
        final Integer minimumGap = null;
        final Integer maximumGap = null;
        final EnumOptionData repaymentFrequencyType = LoanEnumerations.repaymentFrequencyType(PeriodFrequencyType.MONTHS);
        final EnumOptionData interestRateFrequencyType = LoanEnumerations.interestRateFrequencyType(PeriodFrequencyType.MONTHS);
        final EnumOptionData amortizationType = LoanEnumerations.amortizationType(AmortizationMethod.EQUAL_INSTALLMENTS);
        final EnumOptionData interestType = LoanEnumerations.interestType(InterestMethod.DECLINING_BALANCE);
        final EnumOptionData interestCalculationPeriodType = LoanEnumerations
                .interestCalculationPeriodType(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD);
        final Boolean allowPartialPeriodInterestCalcualtion = null;
        final Long fundId = null;
        final String fundName = null;
        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;

        final Integer graceOnPrincipalPayment = null;
        final Integer recurringMoratoriumOnPrincipalPeriods = null;
        final Integer graceOnInterestPayment = null;
        final Integer graceOnInterestCharged = null;
        final Integer graceOnArrearsAgeing = null;
        final Integer overdueDaysForNPA = null;

        final Collection<ChargeData> charges = null;
        final Collection<LoanProductBorrowerCycleVariationData> principalVariationsForBorrowerCycle = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> interestRateVariationsForBorrowerCycle = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>(1);

        final EnumOptionData accountingType = AccountingEnumerations.accountingRuleType(AccountingRuleType.NONE);
        final boolean includeInBorrowerCycle = false;
        final boolean useBorrowerCycle = false;
        final LocalDate startDate = null;
        final LocalDate closeDate = null;
        final String status = null;
        final String externalId = null;
        final Boolean multiDisburseLoan = null;
        final Integer maxTrancheCount = null;
        final BigDecimal outstandingLoanBalance = null;

        final EnumOptionData daysInMonthType = CommonEnumerations.daysInMonthType(DaysInMonthType.ACTUAL);
        final EnumOptionData daysInYearType = CommonEnumerations.daysInYearType(DaysInYearType.ACTUAL);
        final boolean isInterestRecalculationEnabled = false;
        final LoanProductInterestRecalculationData interestRecalculationData = LoanProductInterestRecalculationData
                .sensibleDefaultsForNewLoanProductCreation();
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = null;
        final Boolean holdGuaranteeFunds = false;
        final LoanProductGuaranteeData productGuaranteeData = null;
        final BigDecimal principalThresholdForLastInstallment = null;
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = false;
        final boolean canDefineInstallmentAmount = false;
        final Integer installmentAmountInMultiplesOf = null;
        final LoanProductConfigurableAttributes loanProductConfigurableAttributes = null;
        final boolean syncExpectedWithDisbursementDate = false;
        final boolean canUseForTopup = false;
        final boolean isEqualAmortization = false;

        return new LoanProductData(id, name, shortName, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType,
                amortizationType, interestType, interestCalculationPeriodType, allowPartialPeriodInterestCalcualtion, fundId, fundName,
                transactionProcessingStrategyId, transactionProcessingStrategyName, graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment,
                graceOnInterestCharged, charges, accountingType, includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status,
                externalId, principalVariationsForBorrowerCycle, interestRateVariationsForBorrowerCycle,
                numberOfRepaymentVariationsForBorrowerCycle, multiDisburseLoan, maxTrancheCount, outstandingLoanBalance,
                graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment, holdGuaranteeFunds, productGuaranteeData,
                principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount,
                installmentAmountInMultiplesOf, loanProductConfigurableAttributes, isLinkedToFloatingInterestRates, floatingRateId,
                floatingRateName, interestRateDifferential, minDifferentialLendingRate, defaultDifferentialLendingRate,
                maxDifferentialLendingRate, isFloatingInterestRateCalculationAllowed, isVariableInstallmentsAllowed, minimumGap, maximumGap,
                syncExpectedWithDisbursementDate, canUseForTopup, isEqualAmortization);

    }

    public static LoanProductData withAccountingDetails(final LoanProductData productData, final Map<String, Object> accountingMappings,
            final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings,
            final Collection<ChargeToGLAccountMapper> feeToGLAccountMappings,
            final Collection<ChargeToGLAccountMapper> penaltyToGLAccountMappings) {
        productData.accountingMappings = accountingMappings;
        productData.paymentChannelToFundSourceMappings = paymentChannelToFundSourceMappings;
        productData.feeToIncomeAccountMappings = feeToGLAccountMappings;
        productData.penaltyToIncomeAccountMappings = penaltyToGLAccountMappings;
        return productData;
    }
    
    public LoanProductData(final Long id, final String name, final String shortName, final String description, final CurrencyData currency,
            final BigDecimal principal, final BigDecimal minPrincipal, final BigDecimal maxPrincipal, final BigDecimal tolerance,
            final Integer numberOfRepayments, final Integer minNumberOfRepayments, final Integer maxNumberOfRepayments,
            final Integer repaymentEvery, final BigDecimal interestRatePerPeriod, final BigDecimal minInterestRatePerPeriod,
            final BigDecimal maxInterestRatePerPeriod, final BigDecimal annualInterestRate, final EnumOptionData repaymentFrequencyType,
            final EnumOptionData interestRateFrequencyType, final EnumOptionData amortizationType, final EnumOptionData interestType,
            final EnumOptionData interestCalculationPeriodType, final Boolean allowPartialPeriodInterestCalcualtion, final Long fundId,
            final String fundName, final Long transactionProcessingStrategyId, final String transactionProcessingStrategyName,
            final Integer graceOnPrincipalPayment, final Integer recurringMoratoriumOnPrincipalPeriods, final Integer graceOnInterestPayment, final Integer graceOnInterestCharged,
            final Collection<ChargeData> charges, final EnumOptionData accountingType, final boolean includeInBorrowerCycle, boolean useBorrowerCycle, final LocalDate startDate,
            final LocalDate closeDate, final String status, final String externalId,
            Collection<LoanProductBorrowerCycleVariationData> principalVariations,
            Collection<LoanProductBorrowerCycleVariationData> interestRateVariations,
            Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariations, Boolean multiDisburseLoan,
            Integer maxTrancheCount, BigDecimal outstandingLoanBalance, final Integer graceOnArrearsAgeing, final Integer overdueDaysForNPA,
            final EnumOptionData daysInMonthType, final EnumOptionData daysInYearType, final boolean isInterestRecalculationEnabled, final LoanProductInterestRecalculationData interestRecalculationData,
            final Integer minimumDaysBetweenDisbursalAndFirstRepayment, boolean holdGuaranteeFunds,
            final LoanProductGuaranteeData loanProductGuaranteeData, final BigDecimal principalThresholdForLastInstallment,
            final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion, boolean canDefineInstallmentAmount,
            Integer installmentAmountInMultiplesOf, LoanProductConfigurableAttributes allowAttributeOverrides,
            boolean isLinkedToFloatingInterestRates, Integer floatingRateId, String floatingRateName, BigDecimal interestRateDifferential,
            BigDecimal minDifferentialLendingRate, BigDecimal defaultDifferentialLendingRate, BigDecimal maxDifferentialLendingRate,
            boolean isFloatingInterestRateCalculationAllowed, final boolean isVariableInstallmentsAllowed,
            final Integer minimumGapBetweenInstallments, final Integer maximumGapBetweenInstallments, 
            final boolean syncExpectedWithDisbursementDate, final boolean canUseForTopup, final boolean isEqualAmortization) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.currency = currency;
        this.principal = principal;
        this.minPrincipal = minPrincipal;
        this.maxPrincipal = maxPrincipal;
        this.inArrearsTolerance = tolerance;
        this.numberOfRepayments = numberOfRepayments;
        this.minNumberOfRepayments = minNumberOfRepayments;
        this.maxNumberOfRepayments = maxNumberOfRepayments;
        this.graceOnPrincipalPayment = graceOnPrincipalPayment;
        this.recurringMoratoriumOnPrincipalPeriods = recurringMoratoriumOnPrincipalPeriods;
        this.graceOnInterestPayment = graceOnInterestPayment;
        this.graceOnInterestCharged = graceOnInterestCharged;
        this.repaymentEvery = repaymentEvery;
        this.interestRatePerPeriod = interestRatePerPeriod;
        this.minInterestRatePerPeriod = minInterestRatePerPeriod;
        this.maxInterestRatePerPeriod = maxInterestRatePerPeriod;
        this.annualInterestRate = annualInterestRate;
        this.isLinkedToFloatingInterestRates = isLinkedToFloatingInterestRates;
        this.floatingRateId = floatingRateId;
        this.floatingRateName = floatingRateName;
        this.interestRateDifferential = interestRateDifferential;
        this.minDifferentialLendingRate = minDifferentialLendingRate;
        this.defaultDifferentialLendingRate = defaultDifferentialLendingRate;
        this.maxDifferentialLendingRate = maxDifferentialLendingRate;
        this.isFloatingInterestRateCalculationAllowed = isFloatingInterestRateCalculationAllowed;
        this.allowVariableInstallments = isVariableInstallmentsAllowed;
        this.minimumGap = minimumGapBetweenInstallments;
        this.maximumGap = maximumGapBetweenInstallments;
        this.repaymentFrequencyType = repaymentFrequencyType;
        this.interestRateFrequencyType = interestRateFrequencyType;
        this.amortizationType = amortizationType;
        this.interestType = interestType;
        this.interestCalculationPeriodType = interestCalculationPeriodType;
        this.allowPartialPeriodInterestCalcualtion = allowPartialPeriodInterestCalcualtion;
        this.fundId = fundId;
        this.fundName = fundName;
        this.transactionProcessingStrategyId = transactionProcessingStrategyId;
        this.transactionProcessingStrategyName = transactionProcessingStrategyName;
        this.charges = charges;
        this.accountingRule = accountingType;
        this.includeInBorrowerCycle = includeInBorrowerCycle;
        this.useBorrowerCycle = useBorrowerCycle;
        this.startDate = startDate;
        this.closeDate = closeDate;
        this.status = status;
        this.externalId = externalId;
        this.minimumDaysBetweenDisbursalAndFirstRepayment = minimumDaysBetweenDisbursalAndFirstRepayment;

        this.chargeOptions = null;
        this.penaltyOptions = null;
        this.paymentTypeOptions = null;
        this.currencyOptions = null;
        this.fundOptions = null;
        this.transactionProcessingStrategyOptions = null;
        this.amortizationTypeOptions = null;
        this.interestTypeOptions = null;
        this.interestCalculationPeriodTypeOptions = null;
        this.repaymentFrequencyTypeOptions = null;
        this.interestRateFrequencyTypeOptions = null;
        this.floatingRateOptions = null;

        this.accountingMappingOptions = null;
        this.accountingRuleOptions = null;
        this.accountingMappings = null;
        this.paymentChannelToFundSourceMappings = null;
        this.feeToIncomeAccountMappings = null;
        this.penaltyToIncomeAccountMappings = null;
        this.valueConditionTypeOptions = null;
        this.principalVariationsForBorrowerCycle = principalVariations;
        this.interestRateVariationsForBorrowerCycle = interestRateVariations;
        this.numberOfRepaymentVariationsForBorrowerCycle = numberOfRepaymentVariations;
        this.multiDisburseLoan = multiDisburseLoan;
        this.outstandingLoanBalance = outstandingLoanBalance;
        this.maxTrancheCount = maxTrancheCount;
        this.graceOnArrearsAgeing = graceOnArrearsAgeing;
        this.overdueDaysForNPA = overdueDaysForNPA;
        this.daysInMonthType = daysInMonthType;
        this.daysInYearType = daysInYearType;
        this.isInterestRecalculationEnabled = isInterestRecalculationEnabled;
        this.interestRecalculationData = interestRecalculationData;
        this.holdGuaranteeFunds = holdGuaranteeFunds;
        this.productGuaranteeData = loanProductGuaranteeData;
        this.principalThresholdForLastInstallment = principalThresholdForLastInstallment;
        this.accountMovesOutOfNPAOnlyOnArrearsCompletion = accountMovesOutOfNPAOnlyOnArrearsCompletion;
        this.allowAttributeOverrides = allowAttributeOverrides;

        this.daysInMonthTypeOptions = null;
        this.daysInYearTypeOptions = null;
        this.interestRecalculationCompoundingTypeOptions = null;
        this.rescheduleStrategyTypeOptions = null;
        this.interestRecalculationFrequencyTypeOptions = null;
        this.interestRecalculationNthDayTypeOptions = null;
        this.interestRecalculationDayOfWeekTypeOptions = null;

        this.canDefineInstallmentAmount = canDefineInstallmentAmount;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.preClosureInterestCalculationStrategyOptions = null;
        this.syncExpectedWithDisbursementDate = syncExpectedWithDisbursementDate;
        this.canUseForTopup = canUseForTopup;
        this.isEqualAmortization = isEqualAmortization;

    }

    public LoanProductData(final LoanProductData productData, final Collection<ChargeData> chargeOptions,
            final Collection<ChargeData> penaltyOptions, final Collection<PaymentTypeData> paymentTypeOptions,
            final Collection<CurrencyData> currencyOptions, final List<EnumOptionData> amortizationTypeOptions,
            final List<EnumOptionData> interestTypeOptions, final List<EnumOptionData> interestCalculationPeriodTypeOptions,
            final List<EnumOptionData> repaymentFrequencyTypeOptions, final List<EnumOptionData> interestRateFrequencyTypeOptions,
            final Collection<FundData> fundOptions, final Collection<TransactionProcessingStrategyData> transactionStrategyOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions, final List<EnumOptionData> accountingRuleOptions,
            final List<EnumOptionData> valueConditionTypeOptions, final List<EnumOptionData> daysInMonthTypeOptions,
            final List<EnumOptionData> daysInYearTypeOptions, final List<EnumOptionData> interestRecalculationCompoundingTypeOptions,
            final List<EnumOptionData> rescheduleStrategyTypeOptions, final List<EnumOptionData> interestRecalculationFrequencyTypeOptions,
            final List<EnumOptionData> preCloseInterestCalculationStrategyOptions, final List<FloatingRateData> floatingRateOptions,
            final List<EnumOptionData> interestRecalculationNthDayTypeOptions,
            final List<EnumOptionData> interestRecalculationDayOfWeekTypeOptions) {
        this.id = productData.id;
        this.name = productData.name;
        this.shortName = productData.shortName;
        this.description = productData.description;
        this.fundId = productData.fundId;
        this.fundName = productData.fundName;

        this.principal = productData.principal;
        this.minPrincipal = productData.minPrincipal;
        this.maxPrincipal = productData.maxPrincipal;
        this.inArrearsTolerance = productData.inArrearsTolerance;
        this.numberOfRepayments = productData.numberOfRepayments;
        this.minNumberOfRepayments = productData.minNumberOfRepayments;
        this.maxNumberOfRepayments = productData.maxNumberOfRepayments;
        this.repaymentEvery = productData.repaymentEvery;
        this.interestRatePerPeriod = productData.interestRatePerPeriod;
        this.minInterestRatePerPeriod = productData.minInterestRatePerPeriod;
        this.maxInterestRatePerPeriod = productData.maxInterestRatePerPeriod;
        this.annualInterestRate = productData.annualInterestRate;
        this.isLinkedToFloatingInterestRates = productData.isLinkedToFloatingInterestRates;
        this.floatingRateId = productData.floatingRateId;
        this.floatingRateName = productData.floatingRateName;
        this.interestRateDifferential = productData.interestRateDifferential;
        this.minDifferentialLendingRate = productData.minDifferentialLendingRate;
        this.defaultDifferentialLendingRate = productData.defaultDifferentialLendingRate;
        this.maxDifferentialLendingRate = productData.maxDifferentialLendingRate;
        this.isFloatingInterestRateCalculationAllowed = productData.isFloatingInterestRateCalculationAllowed;
        this.allowVariableInstallments = productData.allowVariableInstallments;
        this.minimumGap = productData.minimumGap;
        this.maximumGap = productData.maximumGap;
        this.repaymentFrequencyType = productData.repaymentFrequencyType;
        this.interestRateFrequencyType = productData.interestRateFrequencyType;
        this.amortizationType = productData.amortizationType;
        this.interestType = productData.interestType;
        this.interestCalculationPeriodType = productData.interestCalculationPeriodType;
        this.allowPartialPeriodInterestCalcualtion = productData.allowPartialPeriodInterestCalcualtion;
        this.startDate = productData.startDate;
        this.closeDate = productData.closeDate;
        this.status = productData.status;
        this.externalId = productData.externalId;

        this.charges = nullIfEmpty(productData.charges());
        this.principalVariationsForBorrowerCycle = productData.principalVariationsForBorrowerCycle;
        this.interestRateVariationsForBorrowerCycle = productData.interestRateVariationsForBorrowerCycle;
        this.numberOfRepaymentVariationsForBorrowerCycle = productData.numberOfRepaymentVariationsForBorrowerCycle;
        this.accountingRule = productData.accountingRule;
        this.accountingMappings = productData.accountingMappings;
        this.paymentChannelToFundSourceMappings = productData.paymentChannelToFundSourceMappings;
        this.feeToIncomeAccountMappings = productData.feeToIncomeAccountMappings;
        this.penaltyToIncomeAccountMappings = productData.penaltyToIncomeAccountMappings;

        this.chargeOptions = chargeOptions;
        this.penaltyOptions = penaltyOptions;
        this.paymentTypeOptions = paymentTypeOptions;
        this.currencyOptions = currencyOptions;
        this.currency = productData.currency;
        this.fundOptions = fundOptions;
        this.transactionProcessingStrategyOptions = transactionStrategyOptions;
        this.floatingRateOptions = floatingRateOptions;
        if (this.transactionProcessingStrategyOptions != null && this.transactionProcessingStrategyOptions.size() == 1) {
            final List<TransactionProcessingStrategyData> listOfOptions = new ArrayList<>(this.transactionProcessingStrategyOptions);

            this.transactionProcessingStrategyId = listOfOptions.get(0).id();
            this.transactionProcessingStrategyName = listOfOptions.get(0).name();
        } else {
            this.transactionProcessingStrategyId = productData.transactionProcessingStrategyId;
            this.transactionProcessingStrategyName = productData.transactionProcessingStrategyName;
        }

        this.graceOnPrincipalPayment = productData.graceOnPrincipalPayment;
        this.recurringMoratoriumOnPrincipalPeriods = productData.recurringMoratoriumOnPrincipalPeriods;
        this.graceOnInterestPayment = productData.graceOnInterestPayment;
        this.graceOnInterestCharged = productData.graceOnInterestCharged;
        this.includeInBorrowerCycle = productData.includeInBorrowerCycle;
        this.useBorrowerCycle = productData.useBorrowerCycle;
        this.multiDisburseLoan = productData.multiDisburseLoan;
        this.maxTrancheCount = productData.maxTrancheCount;
        this.outstandingLoanBalance = productData.outstandingLoanBalance;
        this.minimumDaysBetweenDisbursalAndFirstRepayment = productData.minimumDaysBetweenDisbursalAndFirstRepayment;

        this.amortizationTypeOptions = amortizationTypeOptions;
        this.interestTypeOptions = interestTypeOptions;
        this.interestCalculationPeriodTypeOptions = interestCalculationPeriodTypeOptions;
        this.interestRecalculationNthDayTypeOptions = interestRecalculationNthDayTypeOptions;
        this.interestRecalculationDayOfWeekTypeOptions = interestRecalculationDayOfWeekTypeOptions;
        this.repaymentFrequencyTypeOptions = repaymentFrequencyTypeOptions;
        this.interestRateFrequencyTypeOptions = interestRateFrequencyTypeOptions;

        this.accountingMappingOptions = accountingMappingOptions;
        this.accountingRuleOptions = accountingRuleOptions;
        this.valueConditionTypeOptions = valueConditionTypeOptions;
        this.graceOnArrearsAgeing = productData.graceOnArrearsAgeing;
        this.overdueDaysForNPA = productData.overdueDaysForNPA;

        this.daysInMonthType = productData.daysInMonthType;
        this.daysInYearType = productData.daysInYearType;
        this.isInterestRecalculationEnabled = productData.isInterestRecalculationEnabled;
        this.interestRecalculationData = productData.interestRecalculationData;
        this.holdGuaranteeFunds = productData.holdGuaranteeFunds;
        this.productGuaranteeData = productData.productGuaranteeData;
        this.principalThresholdForLastInstallment = productData.principalThresholdForLastInstallment;
        this.accountMovesOutOfNPAOnlyOnArrearsCompletion = productData.accountMovesOutOfNPAOnlyOnArrearsCompletion;

        this.daysInMonthTypeOptions = daysInMonthTypeOptions;
        this.daysInYearTypeOptions = daysInYearTypeOptions;
        this.interestRecalculationCompoundingTypeOptions = interestRecalculationCompoundingTypeOptions;
        this.rescheduleStrategyTypeOptions = rescheduleStrategyTypeOptions;
        this.allowAttributeOverrides = productData.allowAttributeOverrides;

        if (CollectionUtils.isEmpty(interestRecalculationFrequencyTypeOptions)) {
            this.interestRecalculationFrequencyTypeOptions = null;
        } else {
            this.interestRecalculationFrequencyTypeOptions = interestRecalculationFrequencyTypeOptions;
        }

        this.canDefineInstallmentAmount = productData.canDefineInstallmentAmount;
        this.installmentAmountInMultiplesOf = productData.installmentAmountInMultiplesOf;
        this.preClosureInterestCalculationStrategyOptions = preCloseInterestCalculationStrategyOptions;
        this.syncExpectedWithDisbursementDate = productData.syncExpectedWithDisbursementDate;
        this.canUseForTopup = productData.canUseForTopup;
        this.isEqualAmortization = productData.isEqualAmortization;
    }

    private Collection<ChargeData> nullIfEmpty(final Collection<ChargeData> charges) {
        Collection<ChargeData> chargesLocal = charges;
        if (charges == null || charges.isEmpty()) {
            chargesLocal = null;
        }
        return chargesLocal;
    }

    public Collection<ChargeData> charges() {
        Collection<ChargeData> chargesLocal = new ArrayList<>();
        if (this.charges != null) {
            chargesLocal = this.charges;
        }
        return chargesLocal;
    }

    public EnumOptionData accountingRuleType() {
        return this.accountingRule;
    }

    public boolean hasAccountingEnabled() {
        return this.accountingRule.getId() > AccountingRuleType.NONE.getValue();
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Long getFundId() {
        return this.fundId;
    }

    public String getFundName() {
        return this.fundName;
    }

    public Long getTransactionProcessingStrategyId() {
        return this.transactionProcessingStrategyId;
    }

    public String getTransactionProcessingStrategyName() {
        return this.transactionProcessingStrategyName;
    }

    public CurrencyData getCurrency() {
        return this.currency;
    }

    public BigDecimal getPrincipal() {
        return this.principal;
    }

    public BigDecimal getMinPrincipal() {
        return this.minPrincipal;
    }

    public BigDecimal getMaxPrincipal() {
        return this.maxPrincipal;
    }

    public BigDecimal getInArrearsTolerance() {
        return this.inArrearsTolerance;
    }

    public Integer getNumberOfRepayments() {
        return this.numberOfRepayments;
    }

    public Integer getRepaymentEvery() {
        return this.repaymentEvery;
    }

    public BigDecimal getInterestRatePerPeriod() {
        return this.interestRatePerPeriod;
    }

    public BigDecimal getAnnualInterestRate() {
        return this.annualInterestRate;
    }

    public EnumOptionData getRepaymentFrequencyType() {
        return this.repaymentFrequencyType;
    }

    public Integer getGraceOnPrincipalPayment() {
        return this.graceOnPrincipalPayment;
    }
    
    public Integer getRecurringMoratoriumOnPrincipalPeriods() {
    	return this.recurringMoratoriumOnPrincipalPeriods;
    }

    public Integer getGraceOnInterestPayment() {
        return this.graceOnInterestPayment;
    }

    public Integer getGraceOnInterestCharged() {
        return this.graceOnInterestCharged;
    }

    public EnumOptionData getInterestRateFrequencyType() {
        return this.interestRateFrequencyType;
    }

    public EnumOptionData getAmortizationType() {
        return this.amortizationType;
    }

    public EnumOptionData getInterestType() {
        return this.interestType;
    }

    public EnumOptionData getInterestCalculationPeriodType() {
        return this.interestCalculationPeriodType;
    }

    public Collection<FundData> getFundOptions() {
        return this.fundOptions;
    }

    public List<EnumOptionData> getAmortizationTypeOptions() {
        return this.amortizationTypeOptions;
    }

    public List<EnumOptionData> getInterestTypeOptions() {
        return this.interestTypeOptions;
    }

    public List<EnumOptionData> getInterestCalculationPeriodTypeOptions() {
        return this.interestCalculationPeriodTypeOptions;
    }

    public List<EnumOptionData> getRepaymentFrequencyTypeOptions() {
        return this.repaymentFrequencyTypeOptions;
    }

    public List<EnumOptionData> getInterestRateFrequencyTypeOptions() {
        return this.interestRateFrequencyTypeOptions;
    }

    public Collection<ChargeData> getChargeOptions() {
        return this.chargeOptions;
    }

    @Override
    public boolean equals(final Object obj) {
        final LoanProductData loanProductData = (LoanProductData) obj;
        return loanProductData.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public boolean useBorrowerCycle() {
        return this.useBorrowerCycle;
    }

    public Collection<LoanProductBorrowerCycleVariationData> getPrincipalVariationsForBorrowerCycle() {
        return this.principalVariationsForBorrowerCycle;
    }

    public Collection<LoanProductBorrowerCycleVariationData> getInterestRateVariationsForBorrowerCycle() {
        return this.interestRateVariationsForBorrowerCycle;
    }

    public Collection<LoanProductBorrowerCycleVariationData> getNumberOfRepaymentVariationsForBorrowerCycle() {
        return this.numberOfRepaymentVariationsForBorrowerCycle;
    }

    public Boolean getMultiDisburseLoan() {
        return this.multiDisburseLoan;
    }

    public BigDecimal getOutstandingLoanBalance() {
        return this.outstandingLoanBalance;
    }

    public Integer getGraceOnArrearsAgeing() {
        return this.graceOnArrearsAgeing;
    }

    public EnumOptionData getDaysInMonthType() {
        return this.daysInMonthType;
    }

    public EnumOptionData getDaysInYearType() {
        return this.daysInYearType;
    }

    public boolean isInterestRecalculationEnabled() {
        return this.isInterestRecalculationEnabled;
    }

    public LoanProductInterestRecalculationData getInterestRecalculationData() {
        return this.interestRecalculationData;
    }

    public Collection<ChargeData> overdueFeeCharges() {
        Collection<ChargeData> overdueFeeCharges = new ArrayList<>();
        Collection<ChargeData> charges = charges();
        for (ChargeData chargeData : charges) {
            if (chargeData.isOverdueInstallmentCharge()) {
                overdueFeeCharges.add(chargeData);
            }
        }
        return overdueFeeCharges;
    }

    public LoanInterestRecalculationData toLoanInterestRecalculationData() {
        final Long id = null;
        final Long loanId = null;
        final CalendarData calendarData = null;
        final CalendarData compoundingCalendarData = null;
        return new LoanInterestRecalculationData(id, loanId, getInterestRecalculationCompoundingType(), getRescheduleStrategyType(),
                calendarData, getRecalculationRestFrequencyType(), getRecalculationRestFrequencyInterval(),
                getInterestRecalculationRestNthDayType(), getInterestRecalculationRestWeekDayType(),
                getInterestRecalculationRestOnDayType(), compoundingCalendarData, getRecalculationCompoundingFrequencyType(),
                getRecalculationCompoundingFrequencyInterval(), getInterestRecalculationCompoundingNthDayType(),
                getInterestRecalculationCompoundingWeekDayType(), getInterestRecalculationCompoundingOnDayType(),
                isCompoundingToBePostedAsTransaction(), allowCompoundingOnEod());
    }

    private EnumOptionData getRescheduleStrategyType() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getRescheduleStrategyType(); }
        return null;
    }

    private EnumOptionData getInterestRecalculationCompoundingType() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getInterestRecalculationCompoundingType(); }
        return null;
    }

    private EnumOptionData getInterestRecalculationCompoundingNthDayType() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getRecalculationCompoundingFrequencyNthDay(); }
        return null;
    }
    private EnumOptionData getInterestRecalculationCompoundingWeekDayType() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getRecalculationCompoundingFrequencyWeekday(); }
        return null;
    }
    private Integer getInterestRecalculationCompoundingOnDayType() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getRecalculationCompoundingFrequencyOnDay(); }
        return null;
    }

    private EnumOptionData getRecalculationRestFrequencyType() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getRecalculationRestFrequencyType(); }
        return null;
    }

    private Integer getRecalculationRestFrequencyInterval() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getRecalculationRestFrequencyInterval(); }
        return null;
    }

    private EnumOptionData getInterestRecalculationRestNthDayType() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getRecalculationRestFrequencyNthDay(); }
        return null;
    }

    private EnumOptionData getInterestRecalculationRestWeekDayType() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getRecalculationRestFrequencyWeekday(); }
        return null;
    }

    private Integer getInterestRecalculationRestOnDayType() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getRecalculationRestFrequencyOnDay(); }
        return null;
    }

    private EnumOptionData getRecalculationCompoundingFrequencyType() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getRecalculationCompoundingFrequencyType(); }
        return null;
    }

    private Integer getRecalculationCompoundingFrequencyInterval() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getRecalculationCompoundingFrequencyInterval(); }
        return null;
    }
    public Boolean isCompoundingToBePostedAsTransaction() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.isCompoundingToBePostedAsTransaction(); }
        return null;
    }
    public Boolean allowCompoundingOnEod() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.allowCompoundingOnEod(); }
        return null;
    }

    public boolean canDefineInstallmentAmount() {
        return this.canDefineInstallmentAmount;
    }

    public LoanProductConfigurableAttributes getloanProductConfigurableAttributes() {
        return this.allowAttributeOverrides;
    }

    public void setloanProductConfigurableAttributes(LoanProductConfigurableAttributes loanProductConfigurableAttributes) {
        this.allowAttributeOverrides = loanProductConfigurableAttributes;
    }

    public boolean isLinkedToFloatingInterestRates() {
        return this.isLinkedToFloatingInterestRates;
    }

    public BigDecimal getMinDifferentialLendingRate() {
        return this.minDifferentialLendingRate;
    }

    public BigDecimal getDefaultDifferentialLendingRate() {
        return this.defaultDifferentialLendingRate;
    }

    public BigDecimal getMaxDifferentialLendingRate() {
        return this.maxDifferentialLendingRate;
    }

    public boolean isFloatingInterestRateCalculationAllowed() {
        return this.isFloatingInterestRateCalculationAllowed;
    }

    public boolean isVariableInstallmentsAllowed() {
        return this.allowVariableInstallments;
    }

    public Integer getMinimumGapBetweenInstallments() {
        return this.minimumGap;
    }

    public Integer getMaximumGapBetweenInstallments() {
        return this.maximumGap;
    }

    
    public Boolean getAllowPartialPeriodInterestCalcualtion() {
        return this.allowPartialPeriodInterestCalcualtion;
    }

	public boolean syncExpectedWithDisbursementDate() {
		return syncExpectedWithDisbursementDate;
	}
    
        public boolean canUseForTopup() {
            return this.canUseForTopup;
        }
        
    public BigDecimal getInterestRateDifferential() {
        return this.interestRateDifferential;
    }

    public boolean isEqualAmortization() {
        return isEqualAmortization;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getCloseDate() {
        return closeDate;
    }

    public Integer getMinNumberOfRepayments() {
        return minNumberOfRepayments;
    }

    public Integer getMaxNumberOfRepayments() {
        return maxNumberOfRepayments;
    }

    public BigDecimal getMinInterestRatePerPeriod() {
        return minInterestRatePerPeriod;
    }

    public BigDecimal getMaxInterestRatePerPeriod() {
        return maxInterestRatePerPeriod;
    }
}