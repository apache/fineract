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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Getter;
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
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateData;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.loanaccount.data.LoanInterestRecalculationData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.AllocationType;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.FutureInstallmentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductConfigurableAttributes;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.rate.data.RateData;
import org.springframework.util.CollectionUtils;

/**
 * Immutable data object to represent loan products.
 */
@Getter
public class LoanProductData implements Serializable {

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
    private final Boolean allowPartialPeriodInterestCalculation;
    private final BigDecimal inArrearsTolerance;
    private final String transactionProcessingStrategyCode;
    private final String transactionProcessingStrategyName;
    private final Collection<AdvancedPaymentData> paymentAllocation;
    private final Collection<CreditAllocationData> creditAllocation;
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
    private final EnumOptionData repaymentStartDateType;

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

    // rates
    private final boolean isRatesEnabled;
    private final Collection<RateData> rates;

    // template related
    private final Collection<FundData> fundOptions;
    private final Collection<PaymentTypeData> paymentTypeOptions;
    private final Collection<CurrencyData> currencyOptions;
    private final List<EnumOptionData> repaymentFrequencyTypeOptions;
    private final List<EnumOptionData> interestRateFrequencyTypeOptions;
    private final List<EnumOptionData> amortizationTypeOptions;
    private final List<EnumOptionData> interestTypeOptions;
    private final List<EnumOptionData> interestCalculationPeriodTypeOptions;
    private final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions;
    private final Collection<ChargeData> chargeOptions;
    private final Collection<RateData> rateOptions;
    private final Collection<ChargeData> penaltyOptions;
    private final List<EnumOptionData> accountingRuleOptions;
    private final Map<String, List<GLAccountData>> accountingMappingOptions;
    private final List<EnumOptionData> valueConditionTypeOptions;
    private final List<EnumOptionData> daysInMonthTypeOptions;
    private final List<EnumOptionData> daysInYearTypeOptions;
    private final List<EnumOptionData> interestRecalculationCompoundingTypeOptions;
    private final List<EnumOptionData> interestRecalculationNthDayTypeOptions;
    private final List<EnumOptionData> interestRecalculationDayOfWeekTypeOptions;
    private final List<EnumOptionData> rescheduleStrategyTypeOptions;
    private final List<EnumOptionData> preClosureInterestCalculationStrategyOptions;
    private final List<EnumOptionData> advancedPaymentAllocationTransactionTypes;
    private final List<EnumOptionData> advancedPaymentAllocationFutureInstallmentAllocationRules;
    private final List<EnumOptionData> advancedPaymentAllocationTypes;

    private final List<EnumOptionData> creditAllocationTransactionTypes;
    private final List<EnumOptionData> creditAllocationAllocationTypes;

    private final List<EnumOptionData> loanScheduleTypeOptions;
    private final List<EnumOptionData> loanScheduleProcessingTypeOptions;

    private final List<EnumOptionData> interestRecalculationFrequencyTypeOptions;
    private final List<FloatingRateData> floatingRateOptions;
    private final List<EnumOptionData> repaymentStartDateTypeOptions;

    private final Boolean multiDisburseLoan;
    private final Integer maxTrancheCount;
    private final BigDecimal outstandingLoanBalance;
    private final Boolean disallowExpectedDisbursements;
    private final Boolean allowApprovedDisbursedAmountsOverApplied;
    private final String overAppliedCalculationType;
    private final Integer overAppliedNumber;

    private final BigDecimal principalThresholdForLastInstallment;

    private final Boolean holdGuaranteeFunds;
    private final LoanProductGuaranteeData productGuaranteeData;
    private final Boolean accountMovesOutOfNPAOnlyOnArrearsCompletion;
    private LoanProductConfigurableAttributes allowAttributeOverrides;
    private final boolean syncExpectedWithDisbursementDate;
    private final boolean isEqualAmortization;
    private final BigDecimal fixedPrincipalPercentagePerInstallment;

    // Delinquency Buckets
    private final Collection<DelinquencyBucketData> delinquencyBucketOptions;
    private final DelinquencyBucketData delinquencyBucket;

    private final Integer dueDaysForRepaymentEvent;
    private final Integer overDueDaysForRepaymentEvent;

    private final boolean enableDownPayment;
    private final BigDecimal disbursedAmountPercentageForDownPayment;
    private final boolean enableAutoRepaymentForDownPayment;
    private final boolean enableInstallmentLevelDelinquency;

    private final EnumOptionData loanScheduleType;
    private final EnumOptionData loanScheduleProcessingType;

    /**
     * Used when returning lookup information about loan product for dropdowns.
     */
    public static LoanProductData lookup(final Long id, final String name, final Boolean multiDisburseLoan) {
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
        final String transactionProcessingStrategyCode = null;
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
        final Boolean disallowExpectedDisbursements = false;
        final Boolean allowApprovedDisbursedAmountsOverApplied = false;
        final String overAppliedCalculationType = null;
        final Integer overAppliedNumber = null;

        final LoanProductGuaranteeData productGuaranteeData = null;
        final boolean holdGuaranteeFunds = false;
        final BigDecimal principalThresholdForLastInstallment = null;
        final BigDecimal fixedPrincipalPercentagePerInstallment = null;
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
        final Collection<RateData> rateOptions = null;
        final Collection<RateData> rates = null;
        final boolean isRatesEnabled = false;
        final Collection<DelinquencyBucketData> delinquencyBucketOptions = null;
        final DelinquencyBucketData delinquencyBucket = null;
        final Integer dueDaysForRepaymentEvent = null;
        final Integer overDueDaysForRepaymentEvent = null;
        final boolean enableDownPayment = false;
        final BigDecimal disbursedAmountPercentageDownPayment = null;
        final Collection<AdvancedPaymentData> paymentAllocation = null;
        final Collection<CreditAllocationData> creditAllocation = null;
        final boolean enableAutoRepaymentForDownPayment = false;
        final EnumOptionData repaymentStartDateType = null;
        final boolean enableInstallmentLevelDelinquency = false;
        final EnumOptionData loanScheduleType = null;
        final EnumOptionData loanScheduleProcessingType = null;
        final EnumOptionData loanScheduleTypeOptions = null;
        final EnumOptionData loanScheduleProcessingTypeOptions = null;

        return new LoanProductData(id, name, shortName, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType,
                amortizationType, interestType, interestCalculationPeriodType, allowPartialPeriodInterestCalcualtion, fundId, fundName,
                transactionProcessingStrategyCode, transactionProcessingStrategyName, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, charges, accountingType,
                includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status, externalId, principalVariations,
                interestRateVariations, numberOfRepaymentVariations, multiDisburseLoan, maxTrancheCount, outstandingLoanBalance,
                disallowExpectedDisbursements, allowApprovedDisbursedAmountsOverApplied, overAppliedCalculationType, overAppliedNumber,
                graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment, holdGuaranteeFunds, productGuaranteeData,
                principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount,
                installmentAmountInMultiplesOf, loanProductConfigurableAttributes, isLinkedToFloatingInterestRates, floatingRateId,
                floatingRateName, interestRateDifferential, minDifferentialLendingRate, defaultDifferentialLendingRate,
                maxDifferentialLendingRate, isFloatingInterestRateCalculationAllowed, isVariableInstallmentsAllowed, minimumGap, maximumGap,
                syncExpectedWithDisbursementDate, canUseForTopup, isEqualAmortization, rateOptions, rates, isRatesEnabled,
                fixedPrincipalPercentagePerInstallment, delinquencyBucketOptions, delinquencyBucket, dueDaysForRepaymentEvent,
                overDueDaysForRepaymentEvent, enableDownPayment, disbursedAmountPercentageDownPayment, enableAutoRepaymentForDownPayment,
                paymentAllocation, creditAllocation, repaymentStartDateType, enableInstallmentLevelDelinquency, loanScheduleType,
                loanScheduleProcessingType);

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
        final String transactionProcessingStrategyCode = null;
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
        final Boolean disallowExpectedDisbursements = false;
        final Boolean allowApprovedDisbursedAmountsOverApplied = false;
        final String overAppliedCalculationType = null;
        final Integer overAppliedNumber = null;

        final EnumOptionData daysInMonthType = null;
        final EnumOptionData daysInYearType = null;
        final boolean isInterestRecalculationEnabled = false;
        final LoanProductInterestRecalculationData interestRecalculationData = null;
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = null;
        final boolean holdGuaranteeFunds = false;
        final LoanProductGuaranteeData productGuaranteeData = null;
        final BigDecimal principalThresholdForLastInstallment = null;
        final BigDecimal fixedPrincipalPercentagePerInstallment = null;
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = false;
        final boolean canDefineInstallmentAmount = false;
        final Integer installmentAmountInMultiplesOf = null;
        final LoanProductConfigurableAttributes loanProductConfigurableAttributes = null;
        final boolean syncExpectedWithDisbursementDate = false;
        final boolean canUseForTopup = false;
        final boolean isEqualAmortization = false;
        final Collection<RateData> rateOptions = null;
        final Collection<RateData> rates = null;
        final boolean isRatesEnabled = false;
        final Collection<DelinquencyBucketData> delinquencyBucketOptions = null;
        final DelinquencyBucketData delinquencyBucket = null;
        final Integer dueDaysForRepaymentEvent = null;
        final Integer overDueDaysForRepaymentEvent = null;
        final boolean enableDownPayment = false;
        final BigDecimal disbursedAmountPercentageDownPayment = null;
        final boolean enableAutoRepaymentForDownPayment = false;
        final Collection<AdvancedPaymentData> paymentAllocation = null;
        final Collection<CreditAllocationData> creditAllocation = null;
        final EnumOptionData repaymentStartDateType = null;
        final boolean enableInstallmentLevelDelinquency = false;
        final EnumOptionData loanScheduleType = null;
        final EnumOptionData loanScheduleProcessingType = null;

        return new LoanProductData(id, name, shortName, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType,
                amortizationType, interestType, interestCalculationPeriodType, allowPartialPeriodInterestCalcualtion, fundId, fundName,
                transactionProcessingStrategyCode, transactionProcessingStrategyName, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, charges, accountingType,
                includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status, externalId, principalVariations,
                interestRateVariations, numberOfRepaymentVariations, multiDisburseLoan, maxTrancheCount, outstandingLoanBalance,
                disallowExpectedDisbursements, allowApprovedDisbursedAmountsOverApplied, overAppliedCalculationType, overAppliedNumber,
                graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment, holdGuaranteeFunds, productGuaranteeData,
                principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount,
                installmentAmountInMultiplesOf, loanProductConfigurableAttributes, isLinkedToFloatingInterestRates, floatingRateId,
                floatingRateName, interestRateDifferential, minDifferentialLendingRate, defaultDifferentialLendingRate,
                maxDifferentialLendingRate, isFloatingInterestRateCalculationAllowed, isVariableInstallmentsAllowed, minimumGap, maximumGap,
                syncExpectedWithDisbursementDate, canUseForTopup, isEqualAmortization, rateOptions, rates, isRatesEnabled,
                fixedPrincipalPercentagePerInstallment, delinquencyBucketOptions, delinquencyBucket, dueDaysForRepaymentEvent,
                overDueDaysForRepaymentEvent, enableDownPayment, disbursedAmountPercentageDownPayment, enableAutoRepaymentForDownPayment,
                paymentAllocation, creditAllocation, repaymentStartDateType, enableInstallmentLevelDelinquency, loanScheduleType,
                loanScheduleProcessingType);

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
        final String transactionProcessingStrategyCode = null;
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
        final Boolean disallowExpectedDisbursements = false;
        final Boolean allowApprovedDisbursedAmountsOverApplied = false;
        final String overAppliedCalculationType = null;
        final Integer overAppliedNumber = null;

        final EnumOptionData daysInMonthType = CommonEnumerations.daysInMonthType(DaysInMonthType.ACTUAL);
        final EnumOptionData daysInYearType = CommonEnumerations.daysInYearType(DaysInYearType.ACTUAL);
        final boolean isInterestRecalculationEnabled = false;
        final LoanProductInterestRecalculationData interestRecalculationData = LoanProductInterestRecalculationData
                .sensibleDefaultsForNewLoanProductCreation();
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = null;
        final boolean holdGuaranteeFunds = false;
        final LoanProductGuaranteeData productGuaranteeData = null;
        final BigDecimal principalThresholdForLastInstallment = null;
        final BigDecimal fixedPrincipalPercentagePerInstallment = null;
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = false;
        final boolean canDefineInstallmentAmount = false;
        final Integer installmentAmountInMultiplesOf = null;
        final LoanProductConfigurableAttributes loanProductConfigurableAttributes = null;
        final boolean syncExpectedWithDisbursementDate = false;
        final boolean canUseForTopup = false;
        final boolean isEqualAmortization = false;
        final Collection<RateData> rateOptions = null;
        final Collection<RateData> rates = null;
        final boolean isRatesEnabled = false;
        final Collection<DelinquencyBucketData> delinquencyBucketOptions = null;
        final DelinquencyBucketData delinquencyBucket = null;
        final Integer dueDaysForRepaymentEvent = null;
        final Integer overDueDaysForRepaymentEvent = null;
        final boolean enableDownPayment = false;
        final BigDecimal disbursedAmountPercentageDownPayment = null;
        final boolean enableAutoRepaymentForDownPayment = false;
        final Collection<AdvancedPaymentData> paymentAllocation = null;
        final Collection<CreditAllocationData> creditAllocation = null;
        final EnumOptionData repaymentStartDateType = LoanEnumerations.repaymentStartDateType(RepaymentStartDateType.DISBURSEMENT_DATE);
        final boolean enableInstallmentLevelDelinquency = false;
        final EnumOptionData loanScheduleType = LoanScheduleType.CUMULATIVE.asEnumOptionData();
        final EnumOptionData loanScheduleProcessingType = LoanScheduleProcessingType.HORIZONTAL.asEnumOptionData();

        return new LoanProductData(id, name, shortName, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType,
                amortizationType, interestType, interestCalculationPeriodType, allowPartialPeriodInterestCalcualtion, fundId, fundName,
                transactionProcessingStrategyCode, transactionProcessingStrategyName, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, charges, accountingType,
                includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status, externalId, principalVariationsForBorrowerCycle,
                interestRateVariationsForBorrowerCycle, numberOfRepaymentVariationsForBorrowerCycle, multiDisburseLoan, maxTrancheCount,
                outstandingLoanBalance, disallowExpectedDisbursements, allowApprovedDisbursedAmountsOverApplied, overAppliedCalculationType,
                overAppliedNumber, graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment, holdGuaranteeFunds, productGuaranteeData,
                principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount,
                installmentAmountInMultiplesOf, loanProductConfigurableAttributes, isLinkedToFloatingInterestRates, floatingRateId,
                floatingRateName, interestRateDifferential, minDifferentialLendingRate, defaultDifferentialLendingRate,
                maxDifferentialLendingRate, isFloatingInterestRateCalculationAllowed, isVariableInstallmentsAllowed, minimumGap, maximumGap,
                syncExpectedWithDisbursementDate, canUseForTopup, isEqualAmortization, rateOptions, rates, isRatesEnabled,
                fixedPrincipalPercentagePerInstallment, delinquencyBucketOptions, delinquencyBucket, dueDaysForRepaymentEvent,
                overDueDaysForRepaymentEvent, enableDownPayment, disbursedAmountPercentageDownPayment, enableAutoRepaymentForDownPayment,
                paymentAllocation, creditAllocation, repaymentStartDateType, enableInstallmentLevelDelinquency, loanScheduleType,
                loanScheduleProcessingType);

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
        final String transactionProcessingStrategyCode = null;
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
        final Boolean disallowExpectedDisbursements = false;
        final Boolean allowApprovedDisbursedAmountsOverApplied = false;
        final String overAppliedCalculationType = null;
        final Integer overAppliedNumber = null;

        final EnumOptionData daysInMonthType = CommonEnumerations.daysInMonthType(DaysInMonthType.ACTUAL);
        final EnumOptionData daysInYearType = CommonEnumerations.daysInYearType(DaysInYearType.ACTUAL);
        final boolean isInterestRecalculationEnabled = false;
        final LoanProductInterestRecalculationData interestRecalculationData = LoanProductInterestRecalculationData
                .sensibleDefaultsForNewLoanProductCreation();
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = null;
        final boolean holdGuaranteeFunds = false;
        final LoanProductGuaranteeData productGuaranteeData = null;
        final BigDecimal principalThresholdForLastInstallment = null;
        final BigDecimal fixedPrincipalPercentagePerInstallment = null;
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = false;
        final boolean canDefineInstallmentAmount = false;
        final Integer installmentAmountInMultiplesOf = null;
        final LoanProductConfigurableAttributes loanProductConfigurableAttributes = null;
        final boolean syncExpectedWithDisbursementDate = false;
        final boolean canUseForTopup = false;
        final boolean isEqualAmortization = false;
        final Collection<RateData> rateOptions = null;
        final Collection<RateData> rates = null;
        final boolean isRatesEnabled = false;
        final Collection<DelinquencyBucketData> delinquencyBucketOptions = null;
        final DelinquencyBucketData delinquencyBucket = null;
        final Integer dueDaysForRepaymentEvent = null;
        final Integer overDueDaysForRepaymentEvent = null;
        final boolean enableDownPayment = false;
        final BigDecimal disbursedAmountPercentageDownPayment = null;
        final boolean enableAutoRepaymentForDownPayment = false;
        final Collection<AdvancedPaymentData> paymentAllocation = null;
        final Collection<CreditAllocationData> creditAllocationData = null;
        final EnumOptionData repaymentStartDateType = LoanEnumerations.repaymentStartDateType(RepaymentStartDateType.DISBURSEMENT_DATE);
        final boolean enableInstallmentLevelDelinquency = false;
        final EnumOptionData loanScheduleType = null;
        final EnumOptionData loanScheduleProcessingType = null;

        return new LoanProductData(id, name, shortName, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType,
                amortizationType, interestType, interestCalculationPeriodType, allowPartialPeriodInterestCalcualtion, fundId, fundName,
                transactionProcessingStrategyCode, transactionProcessingStrategyName, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, charges, accountingType,
                includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status, externalId, principalVariationsForBorrowerCycle,
                interestRateVariationsForBorrowerCycle, numberOfRepaymentVariationsForBorrowerCycle, multiDisburseLoan, maxTrancheCount,
                outstandingLoanBalance, disallowExpectedDisbursements, allowApprovedDisbursedAmountsOverApplied, overAppliedCalculationType,
                overAppliedNumber, graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment, holdGuaranteeFunds, productGuaranteeData,
                principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount,
                installmentAmountInMultiplesOf, loanProductConfigurableAttributes, isLinkedToFloatingInterestRates, floatingRateId,
                floatingRateName, interestRateDifferential, minDifferentialLendingRate, defaultDifferentialLendingRate,
                maxDifferentialLendingRate, isFloatingInterestRateCalculationAllowed, isVariableInstallmentsAllowed, minimumGap, maximumGap,
                syncExpectedWithDisbursementDate, canUseForTopup, isEqualAmortization, rateOptions, rates, isRatesEnabled,
                fixedPrincipalPercentagePerInstallment, delinquencyBucketOptions, delinquencyBucket, dueDaysForRepaymentEvent,
                overDueDaysForRepaymentEvent, enableDownPayment, disbursedAmountPercentageDownPayment, enableAutoRepaymentForDownPayment,
                paymentAllocation, creditAllocationData, repaymentStartDateType, enableInstallmentLevelDelinquency, loanScheduleType,
                loanScheduleProcessingType);
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
            final EnumOptionData interestCalculationPeriodType, final Boolean allowPartialPeriodInterestCalculation, final Long fundId,
            final String fundName, final String transactionProcessingStrategyCode, final String transactionProcessingStrategyName,
            final Integer graceOnPrincipalPayment, final Integer recurringMoratoriumOnPrincipalPeriods,
            final Integer graceOnInterestPayment, final Integer graceOnInterestCharged, final Collection<ChargeData> charges,
            final EnumOptionData accountingType, final boolean includeInBorrowerCycle, boolean useBorrowerCycle, final LocalDate startDate,
            final LocalDate closeDate, final String status, final String externalId,
            Collection<LoanProductBorrowerCycleVariationData> principalVariations,
            Collection<LoanProductBorrowerCycleVariationData> interestRateVariations,
            Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariations, Boolean multiDisburseLoan,
            Integer maxTrancheCount, BigDecimal outstandingLoanBalance, final Boolean disallowExpectedDisbursements,
            final Boolean allowApprovedDisbursedAmountsOverApplied, final String overAppliedCalculationType,
            final Integer overAppliedNumber, final Integer graceOnArrearsAgeing, final Integer overdueDaysForNPA,
            final EnumOptionData daysInMonthType, final EnumOptionData daysInYearType, final boolean isInterestRecalculationEnabled,
            final LoanProductInterestRecalculationData interestRecalculationData,
            final Integer minimumDaysBetweenDisbursalAndFirstRepayment, boolean holdGuaranteeFunds,
            final LoanProductGuaranteeData loanProductGuaranteeData, final BigDecimal principalThresholdForLastInstallment,
            final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion, boolean canDefineInstallmentAmount,
            Integer installmentAmountInMultiplesOf, LoanProductConfigurableAttributes allowAttributeOverrides,
            boolean isLinkedToFloatingInterestRates, Integer floatingRateId, String floatingRateName, BigDecimal interestRateDifferential,
            BigDecimal minDifferentialLendingRate, BigDecimal defaultDifferentialLendingRate, BigDecimal maxDifferentialLendingRate,
            boolean isFloatingInterestRateCalculationAllowed, final boolean isVariableInstallmentsAllowed,
            final Integer minimumGapBetweenInstallments, final Integer maximumGapBetweenInstallments,
            final boolean syncExpectedWithDisbursementDate, final boolean canUseForTopup, final boolean isEqualAmortization,
            Collection<RateData> rateOptions, Collection<RateData> rates, final boolean isRatesEnabled,
            final BigDecimal fixedPrincipalPercentagePerInstallment, final Collection<DelinquencyBucketData> delinquencyBucketOptions,
            final DelinquencyBucketData delinquencyBucket, final Integer dueDaysForRepaymentEvent,
            final Integer overDueDaysForRepaymentEvent, final boolean enableDownPayment,
            final BigDecimal disbursedAmountPercentageForDownPayment, final boolean enableAutoRepaymentForDownPayment,
            final Collection<AdvancedPaymentData> paymentAllocation, final Collection<CreditAllocationData> creditAllocation,
            final EnumOptionData repaymentStartDateType, final boolean enableInstallmentLevelDelinquency,
            final EnumOptionData loanScheduleType, final EnumOptionData loanScheduleProcessingType) {
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
        this.allowPartialPeriodInterestCalculation = allowPartialPeriodInterestCalculation;
        this.fundId = fundId;
        this.fundName = fundName;
        this.transactionProcessingStrategyCode = transactionProcessingStrategyCode;
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
        this.rateOptions = rateOptions;
        this.rates = rates;
        this.isRatesEnabled = isRatesEnabled;

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
        this.disallowExpectedDisbursements = disallowExpectedDisbursements;
        this.allowApprovedDisbursedAmountsOverApplied = allowApprovedDisbursedAmountsOverApplied;
        this.overAppliedCalculationType = overAppliedCalculationType;
        this.overAppliedNumber = overAppliedNumber;

        this.graceOnArrearsAgeing = graceOnArrearsAgeing;
        this.overdueDaysForNPA = overdueDaysForNPA;
        this.daysInMonthType = daysInMonthType;
        this.daysInYearType = daysInYearType;
        this.isInterestRecalculationEnabled = isInterestRecalculationEnabled;
        this.interestRecalculationData = interestRecalculationData;
        this.holdGuaranteeFunds = holdGuaranteeFunds;
        this.productGuaranteeData = loanProductGuaranteeData;
        this.principalThresholdForLastInstallment = principalThresholdForLastInstallment;
        this.fixedPrincipalPercentagePerInstallment = fixedPrincipalPercentagePerInstallment;
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
        this.delinquencyBucketOptions = delinquencyBucketOptions;
        this.delinquencyBucket = delinquencyBucket;
        this.dueDaysForRepaymentEvent = dueDaysForRepaymentEvent;
        this.overDueDaysForRepaymentEvent = overDueDaysForRepaymentEvent;
        this.enableDownPayment = enableDownPayment;
        this.disbursedAmountPercentageForDownPayment = disbursedAmountPercentageForDownPayment;
        this.paymentAllocation = paymentAllocation;
        this.creditAllocation = creditAllocation;
        this.enableAutoRepaymentForDownPayment = enableAutoRepaymentForDownPayment;
        this.repaymentStartDateType = repaymentStartDateType;
        this.repaymentStartDateTypeOptions = null;
        this.advancedPaymentAllocationTransactionTypes = PaymentAllocationTransactionType.getValuesAsEnumOptionDataList();
        this.advancedPaymentAllocationFutureInstallmentAllocationRules = FutureInstallmentAllocationRule.getValuesAsEnumOptionDataList();
        this.advancedPaymentAllocationTypes = PaymentAllocationType.getValuesAsEnumOptionDataList();
        this.creditAllocationTransactionTypes = CreditAllocationTransactionType.getValuesAsEnumOptionDataList();
        this.creditAllocationAllocationTypes = AllocationType.getValuesAsEnumOptionDataList();
        this.enableInstallmentLevelDelinquency = enableInstallmentLevelDelinquency;
        this.loanScheduleType = loanScheduleType;
        this.loanScheduleProcessingType = loanScheduleProcessingType;
        this.loanScheduleTypeOptions = null;
        this.loanScheduleProcessingTypeOptions = null;
    }

    public LoanProductData(final LoanProductData productData, final Collection<ChargeData> chargeOptions,
            final Collection<ChargeData> penaltyOptions, final Collection<PaymentTypeData> paymentTypeOptions,
            final Collection<CurrencyData> currencyOptions, final List<EnumOptionData> amortizationTypeOptions,
            final List<EnumOptionData> interestTypeOptions, final List<EnumOptionData> interestCalculationPeriodTypeOptions,
            final List<EnumOptionData> repaymentFrequencyTypeOptions, final List<EnumOptionData> interestRateFrequencyTypeOptions,
            final Collection<FundData> fundOptions, final Collection<TransactionProcessingStrategyData> transactionStrategyOptions,
            final Collection<RateData> rateOptions, final Map<String, List<GLAccountData>> accountingMappingOptions,
            final List<EnumOptionData> accountingRuleOptions, final List<EnumOptionData> valueConditionTypeOptions,
            final List<EnumOptionData> daysInMonthTypeOptions, final List<EnumOptionData> daysInYearTypeOptions,
            final List<EnumOptionData> interestRecalculationCompoundingTypeOptions,
            final List<EnumOptionData> rescheduleStrategyTypeOptions, final List<EnumOptionData> interestRecalculationFrequencyTypeOptions,
            final List<EnumOptionData> preCloseInterestCalculationStrategyOptions, final List<FloatingRateData> floatingRateOptions,
            final List<EnumOptionData> interestRecalculationNthDayTypeOptions,
            final List<EnumOptionData> interestRecalculationDayOfWeekTypeOptions, final boolean isRatesEnabled,
            final Collection<DelinquencyBucketData> delinquencyBucketOptions, final List<EnumOptionData> repaymentStartDateTypeOptions,
            final List<EnumOptionData> advancedPaymentAllocationTransactionTypes,
            final List<EnumOptionData> advancedPaymentAllocationFutureInstallmentAllocationRules,
            final List<EnumOptionData> advancedPaymentAllocationTypes, final List<EnumOptionData> loanScheduleTypeOptions,
            final List<EnumOptionData> loanScheduleProcessingTypeOptions, final List<EnumOptionData> creditAllocationTransactionTypes,
            final List<EnumOptionData> creditAllocationAllocationTypes) {

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
        this.allowPartialPeriodInterestCalculation = productData.allowPartialPeriodInterestCalculation;
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
        this.rateOptions = rateOptions;
        this.floatingRateOptions = floatingRateOptions;
        if (this.transactionProcessingStrategyOptions != null && this.transactionProcessingStrategyOptions.size() == 1) {
            final List<TransactionProcessingStrategyData> listOfOptions = new ArrayList<>(this.transactionProcessingStrategyOptions);

            this.transactionProcessingStrategyCode = listOfOptions.get(0).getCode();
            this.transactionProcessingStrategyName = listOfOptions.get(0).getName();
        } else {
            this.transactionProcessingStrategyCode = productData.transactionProcessingStrategyCode;
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
        this.disallowExpectedDisbursements = productData.disallowExpectedDisbursements;
        this.allowApprovedDisbursedAmountsOverApplied = productData.allowApprovedDisbursedAmountsOverApplied;
        this.overAppliedCalculationType = productData.overAppliedCalculationType;
        this.overAppliedNumber = productData.overAppliedNumber;

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
        this.fixedPrincipalPercentagePerInstallment = productData.fixedPrincipalPercentagePerInstallment;
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
        this.rates = productData.rates;
        this.isRatesEnabled = isRatesEnabled;
        this.delinquencyBucketOptions = delinquencyBucketOptions;
        this.delinquencyBucket = productData.delinquencyBucket;
        this.dueDaysForRepaymentEvent = productData.dueDaysForRepaymentEvent;
        this.overDueDaysForRepaymentEvent = productData.overDueDaysForRepaymentEvent;
        this.enableDownPayment = productData.enableDownPayment;
        this.disbursedAmountPercentageForDownPayment = productData.disbursedAmountPercentageForDownPayment;
        this.enableAutoRepaymentForDownPayment = productData.enableAutoRepaymentForDownPayment;
        this.paymentAllocation = productData.paymentAllocation;
        this.creditAllocation = productData.creditAllocation;
        this.repaymentStartDateType = productData.repaymentStartDateType;
        this.repaymentStartDateTypeOptions = repaymentStartDateTypeOptions;
        this.advancedPaymentAllocationTransactionTypes = advancedPaymentAllocationTransactionTypes;
        this.advancedPaymentAllocationFutureInstallmentAllocationRules = advancedPaymentAllocationFutureInstallmentAllocationRules;
        this.advancedPaymentAllocationTypes = advancedPaymentAllocationTypes;
        this.creditAllocationAllocationTypes = creditAllocationAllocationTypes;
        this.creditAllocationTransactionTypes = creditAllocationTransactionTypes;
        this.enableInstallmentLevelDelinquency = productData.enableInstallmentLevelDelinquency;
        this.loanScheduleType = productData.getLoanScheduleType();
        this.loanScheduleProcessingType = productData.getLoanScheduleProcessingType();
        this.loanScheduleProcessingTypeOptions = loanScheduleProcessingTypeOptions;
        this.loanScheduleTypeOptions = loanScheduleTypeOptions;
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

    public boolean hasAccountingEnabled() {
        return this.accountingRule.getId() > AccountingRuleType.NONE.getValue();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof LoanProductData)) {
            return false;
        }
        final LoanProductData loanProductData = (LoanProductData) obj;
        return loanProductData.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public boolean isInterestRecalculationEnabled() {
        return this.isInterestRecalculationEnabled;
    }

    public boolean isIsInterestRecalculationEnabled() {
        return this.isInterestRecalculationEnabled;
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
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRescheduleStrategyType();
        }
        return null;
    }

    private EnumOptionData getInterestRecalculationCompoundingType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getInterestRecalculationCompoundingType();
        }
        return null;
    }

    private EnumOptionData getInterestRecalculationCompoundingNthDayType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationCompoundingFrequencyNthDay();
        }
        return null;
    }

    private EnumOptionData getInterestRecalculationCompoundingWeekDayType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationCompoundingFrequencyWeekday();
        }
        return null;
    }

    private Integer getInterestRecalculationCompoundingOnDayType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationCompoundingFrequencyOnDay();
        }
        return null;
    }

    private EnumOptionData getRecalculationRestFrequencyType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationRestFrequencyType();
        }
        return null;
    }

    private Integer getRecalculationRestFrequencyInterval() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationRestFrequencyInterval();
        }
        return null;
    }

    private EnumOptionData getInterestRecalculationRestNthDayType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationRestFrequencyNthDay();
        }
        return null;
    }

    private EnumOptionData getInterestRecalculationRestWeekDayType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationRestFrequencyWeekday();
        }
        return null;
    }

    private Integer getInterestRecalculationRestOnDayType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationRestFrequencyOnDay();
        }
        return null;
    }

    private EnumOptionData getRecalculationCompoundingFrequencyType() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationCompoundingFrequencyType();
        }
        return null;
    }

    private Integer getRecalculationCompoundingFrequencyInterval() {
        if (isInterestRecalculationEnabled()) {
            return this.interestRecalculationData.getRecalculationCompoundingFrequencyInterval();
        }
        return null;
    }

    @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
    public Boolean isCompoundingToBePostedAsTransaction() {
        return isInterestRecalculationEnabled() ? this.interestRecalculationData.isCompoundingToBePostedAsTransaction() : null;
    }

    @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
    public Boolean allowCompoundingOnEod() {
        return isInterestRecalculationEnabled() ? this.interestRecalculationData.isAllowCompoundingOnEod() : null;
    }

    public void setLoanProductConfigurableAttributes(LoanProductConfigurableAttributes loanProductConfigurableAttributes) {
        this.allowAttributeOverrides = loanProductConfigurableAttributes;
    }

    public boolean isIsLinkedToFloatingInterestRates() {
        return this.isLinkedToFloatingInterestRates;
    }

    public boolean isLinkedToFloatingInterestRates() {
        return this.isLinkedToFloatingInterestRates;
    }

    public boolean isFloatingInterestRateCalculationAllowed() {
        return this.isFloatingInterestRateCalculationAllowed;
    }

    public boolean isIsFloatingInterestRateCalculationAllowed() {
        return this.isFloatingInterestRateCalculationAllowed;
    }

    public boolean isIsEqualAmortization() {
        return isEqualAmortization;
    }

    public boolean isEqualAmortization() {
        return isEqualAmortization;
    }

    public Boolean isAllowPartialPeriodInterestCalculation() {
        return allowPartialPeriodInterestCalculation;
    }

    public Boolean isIsAllowPartialPeriodInterestCalculation() {
        return allowPartialPeriodInterestCalculation;
    }

    public boolean isRatesEnabled() {
        return isRatesEnabled;
    }

    public boolean isIsRatesEnabled() {
        return isRatesEnabled;
    }
}
