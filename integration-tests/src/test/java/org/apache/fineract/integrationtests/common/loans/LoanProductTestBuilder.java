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
package org.apache.fineract.integrationtests.common.loans;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.CreditAllocationData;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.LoanProductChargeToGLAccountMapper;
import org.apache.fineract.client.models.PostChargeOffReasonToExpenseAccountMappings;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeOffBehaviour;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;

public class LoanProductTestBuilder {

    private static final String LOCALE = "en_GB";
    private static final String USD = "USD";
    private static final String DAYS = "0";
    private static final String WEEK = "1";
    private static final String MONTHS = "2";
    private static final String YEARS = "3";
    private static final String CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD = "1";
    private static final String EQUAL_PRINCIPAL_PAYMENTS = "0";
    private static final String EQUAL_INSTALLMENTS = "1";
    private static final String DECLINING_BALANCE = "0";
    private static final String FLAT_BALANCE = "1";
    public static final String DEFAULT_STRATEGY = "mifos-standard-strategy";
    public static final String INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY = "interest-principal-penalties-fees-order-strategy";
    public static final String DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY = "due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy";
    public static final String DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY = "due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy";
    public static final String ADVANCED_PAYMENT_ALLOCATION_STRATEGY = "advanced-payment-allocation-strategy";

    // private static final String HEAVENS_FAMILY_STRATEGY ="heavensfamily-strategy";
    // private static final String CREO_CORE_STRATEGY ="creocore-strategy";
    public static final String RBI_INDIA_STRATEGY = "rbi-india-strategy";

    public static final String RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD = "1";
    public static final String RECALCULATION_FREQUENCY_TYPE_DAILY = "2";
    public static final String RECALCULATION_FREQUENCY_TYPE_WEEKLY = "3";
    public static final String RECALCULATION_FREQUENCY_TYPE_MONTHLY = "4";

    public static final String RECALCULATION_STRATEGY_RESCHEDULE_NEXT_REPAYMENTS = "1";
    public static final String RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS = "2";
    public static final String RECALCULATION_STRATEGY_REDUCE_EMI_AMOUN = "3";
    public static final String RECALCULATION_STRATEGY_ADJUST_LAST_UNPAID_PERIOD = "4";

    public static final String RECALCULATION_COMPOUNDING_METHOD_NONE = "0";
    public static final String RECALCULATION_COMPOUNDING_METHOD_INTEREST = "1";
    public static final String RECALCULATION_COMPOUNDING_METHOD_FEE = "2";
    public static final String RECALCULATION_COMPOUNDING_METHOD_INTEREST_AND_FEE = "3";

    public static final String NONE = "1";
    public static final String CASH_BASED = "2";
    public static final String ACCRUAL_PERIODIC = "3";
    public static final String ACCRUAL_UPFRONT = "4";

    public static final String INTEREST_APPLICABLE_STRATEGY_REST_DATE = "2";
    public static final String INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE = "1";

    private String digitsAfterDecimal = "2";
    private String inMultiplesOf = "0";

    private String nameOfLoanProduct = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
    private String shortName = Utils.uniqueRandomStringGenerator("", 4);
    private String externalId = null;
    private String principal = "10000.00";
    private String numberOfRepayments = "5";
    private String repaymentFrequency = MONTHS;
    private String repaymentPeriod = "1";
    private String interestRatePerPeriod = "2";
    private String interestRateFrequencyType = MONTHS;
    private String interestType = FLAT_BALANCE;
    private String overdueDaysForNPA = "5";
    private String interestCalculationPeriodType = CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD;
    private String inArrearsTolerance = "0";
    private String transactionProcessingStrategyCode = DEFAULT_STRATEGY;
    private List<AdvancedPaymentData> advancedPaymentAllocations = null;
    private List<CreditAllocationData> creditAllocations = null;
    private String accountingRule = NONE;
    private final String currencyCode = USD;
    private String amortizationType = EQUAL_INSTALLMENTS;
    private String minPrincipal = "1000.00";
    private String maxPrincipal = "10000000.00";
    private Account[] accountList = null;

    private List<Map<String, Long>> feeToIncomeAccountMappings = null;
    private List<Map<String, Long>> penaltyToIncomeAccountMappings = null;
    private List<Map<String, Long>> chargeOffReasonToExpenseAccountMappings = null;
    private Account feeAndPenaltyAssetAccount;

    private Boolean multiDisburseLoan = false;
    private Boolean allowFullTermForTranche = false;
    private final String outstandingLoanBalance = "35000";
    private String maxTrancheCount = "3";
    private Boolean disallowExpectedDisbursements = false;
    private Boolean allowApprovedDisbursedAmountsOverApplied = false;
    private String overAppliedCalculationType = null;
    private Integer overAppliedNumber = null;
    private Boolean isEqualAmortization = false;

    private Boolean isInterestRecalculationEnabled = false;
    private String daysInYearType = "1";
    private String daysInMonthType = "1";
    private String interestRecalculationCompoundingMethod = "0";
    private String preCloseInterestCalculationStrategy = INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE;
    private String rescheduleStrategyMethod = "1";
    private String recalculationRestFrequencyType = "2";
    private String recalculationRestFrequencyInterval = "1";
    private String recalculationCompoundingFrequencyType = null;
    private String recalculationCompoundingFrequencyInterval = null;
    private String minimumDaysBetweenDisbursalAndFirstRepayment = null;
    private Boolean holdGuaranteeFunds = null;
    private String mandatoryGuarantee = null;
    private String minimumGuaranteeFromOwnFunds = null;
    private String minimumGuaranteeFromGuarantor = null;
    private String isArrearsBasedOnOriginalSchedule = null;
    private String graceOnPrincipalPayment = null;
    private String graceOnInterestPayment = null;
    private JsonObject allowAttributeOverrides = null;
    private Boolean allowPartialPeriodInterestCalculation = false;

    private Boolean allowVariableInstallments = Boolean.FALSE;
    private Integer minimumGap;
    private Integer maximumGap;
    private Integer recalculationCompoundingFrequencyOnDayType = null;
    private Integer recalculationRestFrequencyOnDayType = null;
    private Integer recalculationCompoundingFrequencyDayOfWeekType = null;
    private Integer recalculationRestFrequencyDayOfWeekType = null;
    private boolean syncExpectedWithDisbursementDate = false;
    private String fixedPrincipalPercentagePerInstallment;
    private String installmentAmountInMultiplesOf;
    private boolean canDefineInstallmentAmount;
    private Long delinquencyBucketId;
    private Integer dueDaysForRepaymentEvent = null;
    private Integer overDueDaysForRepaymentEvent = null;
    private boolean enableDownPayment = false;
    private String disbursedAmountPercentageForDownPayment = null;
    private boolean enableAutoRepaymentForDownPayment = false;
    private Integer repaymentStartDateType = null;
    private String loanScheduleType = LoanScheduleType.CUMULATIVE.name();
    private String loanScheduleProcessingType = LoanScheduleProcessingType.HORIZONTAL.name();
    private FullAccountingConfig fullAccountingConfig;
    private List<String> supportedInterestRefundTypes = null;
    private String chargeOffBehaviour;
    private boolean interestRecognitionOnDisbursementDate = false;
    private Boolean enableBuyDownFee = false;
    private Boolean merchantBuyDownFee = false;
    private String buyDownFeeCalculationType;

    public String build() {
        final HashMap<String, Object> map = build(null, null);
        return new Gson().toJson(map);
    }

    public String build(final String chargeId) {
        final HashMap<String, Object> map = build(chargeId, null);
        return new Gson().toJson(map);
    }

    public HashMap<String, Object> build(final String chargeId, final Long delinquencyBucketId) {
        final HashMap<String, Object> map = new HashMap<>();

        if (chargeId != null) {
            List<HashMap<String, String>> charges = new ArrayList<>();
            HashMap<String, String> chargeMap = new HashMap<>();
            chargeMap.put("id", chargeId);
            charges.add(chargeMap);
            map.put("charges", charges);
        }
        map.put("name", this.nameOfLoanProduct);
        map.put("shortName", this.shortName);
        map.put("externalId", this.externalId);
        map.put("currencyCode", this.currencyCode);
        map.put("locale", LOCALE);
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("digitsAfterDecimal", digitsAfterDecimal);
        map.put("inMultiplesOf", inMultiplesOf);
        map.put("principal", this.principal);
        map.put("numberOfRepayments", this.numberOfRepayments);
        map.put("repaymentEvery", this.repaymentPeriod);
        map.put("repaymentFrequencyType", this.repaymentFrequency);
        map.put("interestRatePerPeriod", this.interestRatePerPeriod);
        map.put("interestRateFrequencyType", this.interestRateFrequencyType);
        map.put("amortizationType", this.amortizationType);
        map.put("fixedPrincipalPercentagePerInstallment", fixedPrincipalPercentagePerInstallment);
        map.put("interestType", this.interestType);
        map.put("interestCalculationPeriodType", this.interestCalculationPeriodType);
        map.put("inArrearsTolerance", this.inArrearsTolerance);
        map.put("transactionProcessingStrategyCode", this.transactionProcessingStrategyCode);
        map.put("paymentAllocation", this.advancedPaymentAllocations);
        map.put("creditAllocation", this.creditAllocations);
        map.put("accountingRule", this.accountingRule);
        map.put("minPrincipal", this.minPrincipal);
        map.put("maxPrincipal", this.maxPrincipal);
        map.put("isEqualAmortization", this.isEqualAmortization);
        map.put("overdueDaysForNPA", this.overdueDaysForNPA);
        map.put("loanScheduleType", loanScheduleType);
        map.put("loanScheduleProcessingType", loanScheduleProcessingType);

        if (this.minimumDaysBetweenDisbursalAndFirstRepayment != null) {
            map.put("minimumDaysBetweenDisbursalAndFirstRepayment", this.minimumDaysBetweenDisbursalAndFirstRepayment);
        }
        if (this.multiDisburseLoan) {
            map.put("multiDisburseLoan", this.multiDisburseLoan);
            map.put("allowFullTermForTranche", this.allowFullTermForTranche);
            map.put("maxTrancheCount", this.maxTrancheCount);
            map.put("outstandingLoanBalance", this.outstandingLoanBalance);
            map.put("disallowExpectedDisbursements", this.disallowExpectedDisbursements);
            if (this.disallowExpectedDisbursements) {
                map.put("allowApprovedDisbursedAmountsOverApplied", this.allowApprovedDisbursedAmountsOverApplied);
                map.put("overAppliedCalculationType", this.overAppliedCalculationType);
                map.put("overAppliedNumber", this.overAppliedNumber);
            }
        }
        if (this.canDefineInstallmentAmount) {
            map.put("canDefineInstallmentAmount", this.canDefineInstallmentAmount);
        }
        if (multiDisburseLoan) {
            map.put("multiDisburseLoan", this.multiDisburseLoan);
            map.put("maxTrancheCount", this.maxTrancheCount);
            map.put("outstandingLoanBalance", this.outstandingLoanBalance);
        }
        // Always send allowFullTermForTranche when it's true (for validation testing of single-disburse scenarios)
        if (this.allowFullTermForTranche && !this.multiDisburseLoan) {
            map.put("allowFullTermForTranche", this.allowFullTermForTranche);
        }

        if (this.fullAccountingConfig != null) {
            map.putAll(this.fullAccountingConfig.toMap());
        } else if (this.accountingRule.equals(ACCRUAL_UPFRONT) || this.accountingRule.equals(ACCRUAL_PERIODIC)) {
            map.putAll(getAccountMappingForAccrualBased(this.feeAndPenaltyAssetAccount));
        } else if (this.accountingRule.equals(CASH_BASED)) {
            map.putAll(getAccountMappingForCashBased());
        }
        map.put("daysInMonthType", this.daysInMonthType);
        map.put("daysInYearType", this.daysInYearType);
        map.put("isInterestRecalculationEnabled", this.isInterestRecalculationEnabled);
        if (this.isInterestRecalculationEnabled) {
            map.put("interestRecalculationCompoundingMethod", this.interestRecalculationCompoundingMethod);
            map.put("rescheduleStrategyMethod", this.rescheduleStrategyMethod);
            map.put("recalculationRestFrequencyType", recalculationRestFrequencyType);
            map.put("recalculationRestFrequencyInterval", recalculationRestFrequencyInterval);
            if (!RECALCULATION_COMPOUNDING_METHOD_NONE.equals(this.interestRecalculationCompoundingMethod)) {
                map.put("recalculationCompoundingFrequencyType", recalculationCompoundingFrequencyType);
                map.put("recalculationCompoundingFrequencyInterval", recalculationCompoundingFrequencyInterval);
            }
            map.put("preClosureInterestCalculationStrategy", preCloseInterestCalculationStrategy);
            if (isArrearsBasedOnOriginalSchedule != null) {
                map.put("isArrearsBasedOnOriginalSchedule", isArrearsBasedOnOriginalSchedule);
            }
            map.put("recalculationCompoundingFrequencyOnDayType", this.recalculationCompoundingFrequencyOnDayType);
            map.put("recalculationCompoundingFrequencyDayOfWeekType", this.recalculationCompoundingFrequencyDayOfWeekType);
            map.put("recalculationRestFrequencyOnDayType", this.recalculationRestFrequencyOnDayType);
            map.put("recalculationRestFrequencyDayOfWeekType", this.recalculationRestFrequencyDayOfWeekType);
        }
        if (holdGuaranteeFunds != null) {
            map.put("holdGuaranteeFunds", this.holdGuaranteeFunds);
            if (this.holdGuaranteeFunds) {
                map.put("mandatoryGuarantee", this.mandatoryGuarantee);
                map.put("minimumGuaranteeFromGuarantor", this.minimumGuaranteeFromGuarantor);
                map.put("minimumGuaranteeFromOwnFunds", this.minimumGuaranteeFromOwnFunds);
            }
        }
        map.put("graceOnPrincipalPayment", graceOnPrincipalPayment);
        map.put("graceOnInterestPayment", graceOnInterestPayment);
        if (allowAttributeOverrides != null) {
            map.put("allowAttributeOverrides", this.allowAttributeOverrides);
        }
        map.put("allowPartialPeriodInterestCalculation", this.allowPartialPeriodInterestCalculation);
        map.put("allowVariableInstallments", allowVariableInstallments);
        if (allowVariableInstallments) {
            map.put("minimumGap", minimumGap);
            map.put("maximumGap", maximumGap);
        }
        map.put("syncExpectedWithDisbursementDate", this.syncExpectedWithDisbursementDate);
        if (installmentAmountInMultiplesOf != null) {
            map.put("installmentAmountInMultiplesOf", this.installmentAmountInMultiplesOf);
        }

        // Delinquency Bucket
        if (delinquencyBucketId != null) {
            map.put("delinquencyBucketId", delinquencyBucketId);
        }

        if (this.delinquencyBucketId != null) {
            map.put("delinquencyBucketId", this.delinquencyBucketId);
        }

        if (this.feeToIncomeAccountMappings != null) {
            map.put("feeToIncomeAccountMappings", this.feeToIncomeAccountMappings);
        }
        if (this.penaltyToIncomeAccountMappings != null) {
            map.put("penaltyToIncomeAccountMappings", this.penaltyToIncomeAccountMappings);
        }

        if (this.chargeOffReasonToExpenseAccountMappings != null) {
            map.put("chargeOffReasonToExpenseAccountMappings", this.chargeOffReasonToExpenseAccountMappings);
        }

        if (this.dueDaysForRepaymentEvent != null) {
            map.put("dueDaysForRepaymentEvent", this.dueDaysForRepaymentEvent);
        }
        if (this.overDueDaysForRepaymentEvent != null) {
            map.put("overDueDaysForRepaymentEvent", this.overDueDaysForRepaymentEvent);
        }
        map.put("enableDownPayment", enableDownPayment);
        if (this.disbursedAmountPercentageForDownPayment != null) {
            map.put("disbursedAmountPercentageForDownPayment", disbursedAmountPercentageForDownPayment);
        }
        if (enableAutoRepaymentForDownPayment) {
            map.put("enableAutoRepaymentForDownPayment", enableAutoRepaymentForDownPayment);
        }
        if (interestRecognitionOnDisbursementDate) {
            map.put("interestRecognitionOnDisbursementDate", interestRecognitionOnDisbursementDate);
        }

        if (this.repaymentStartDateType != null) {
            map.put("repaymentStartDateType", repaymentStartDateType);
        }

        if (this.supportedInterestRefundTypes != null) {
            map.put("supportedInterestRefundTypes", supportedInterestRefundTypes);
        }

        if (this.chargeOffBehaviour != null) {
            map.put("chargeOffBehaviour", chargeOffBehaviour);
        }

        if (this.enableBuyDownFee != null) {
            map.put("enableBuyDownFee", this.enableBuyDownFee);
        }

        if (this.merchantBuyDownFee != null) {
            map.put("merchantBuyDownFee", this.merchantBuyDownFee);
        }

        return map;
    }

    public PostLoanProductsRequest buildRequest() {
        return buildRequest(null, null);
    }

    public PostLoanProductsRequest buildRequest(final String chargeId) {
        return buildRequest(chargeId, null);
    }

    /**
     * Typed counterpart of {@link #build(String, Long)}, for callers on the Feign client.
     *
     * Six keys the map carries have no counterpart on {@link PostLoanProductsRequest}:
     * {@code syncExpectedWithDisbursementDate}, {@code mandatoryGuarantee}, {@code minimumGuaranteeFromGuarantor},
     * {@code minimumGuaranteeFromOwnFunds}, {@code minimumGap} and {@code maximumGap}. Deserialising the map's JSON
     * into the request model dropped them as unknown properties, so leaving them unset here sends the same body.
     */
    public PostLoanProductsRequest buildRequest(final String chargeId, final Long delinquencyBucketId) {
        final PostLoanProductsRequest request = new PostLoanProductsRequest();

        if (chargeId != null) {
            request.charges(new ArrayList<>(List.of(new LoanProductChargeData().id(toLong(chargeId)))));
        }
        request.name(this.nameOfLoanProduct);
        request.shortName(this.shortName);
        request.externalId(this.externalId);
        request.currencyCode(this.currencyCode);
        request.locale(LOCALE);
        request.dateFormat("dd MMMM yyyy");
        request.digitsAfterDecimal(toInteger(this.digitsAfterDecimal));
        request.inMultiplesOf(toInteger(this.inMultiplesOf));
        request.principal(toDouble(this.principal));
        request.numberOfRepayments(toInteger(this.numberOfRepayments));
        request.repaymentEvery(toInteger(this.repaymentPeriod));
        request.repaymentFrequencyType(toLong(this.repaymentFrequency));
        request.interestRatePerPeriod(toDouble(this.interestRatePerPeriod));
        request.interestRateFrequencyType(toInteger(this.interestRateFrequencyType));
        request.amortizationType(toInteger(this.amortizationType));
        request.fixedPrincipalPercentagePerInstallment(toBigDecimal(this.fixedPrincipalPercentagePerInstallment));
        request.interestType(toInteger(this.interestType));
        request.interestCalculationPeriodType(toInteger(this.interestCalculationPeriodType));
        request.inArrearsTolerance(toInteger(this.inArrearsTolerance));
        request.transactionProcessingStrategyCode(this.transactionProcessingStrategyCode);
        request.paymentAllocation(this.advancedPaymentAllocations);
        request.creditAllocation(this.creditAllocations);
        request.accountingRule(toInteger(this.accountingRule));
        request.minPrincipal(toDouble(this.minPrincipal));
        request.maxPrincipal(toDouble(this.maxPrincipal));
        request.isEqualAmortization(this.isEqualAmortization);
        request.overdueDaysForNPA(toInteger(this.overdueDaysForNPA));
        request.loanScheduleType(this.loanScheduleType);
        request.loanScheduleProcessingType(this.loanScheduleProcessingType);

        if (this.minimumDaysBetweenDisbursalAndFirstRepayment != null) {
            request.minimumDaysBetweenDisbursalAndFirstRepayment(toInteger(this.minimumDaysBetweenDisbursalAndFirstRepayment));
        }
        if (this.multiDisburseLoan) {
            request.multiDisburseLoan(this.multiDisburseLoan);
            request.allowFullTermForTranche(this.allowFullTermForTranche);
            request.maxTrancheCount(toInteger(this.maxTrancheCount));
            request.outstandingLoanBalance(toDouble(this.outstandingLoanBalance));
            request.disallowExpectedDisbursements(this.disallowExpectedDisbursements);
            if (this.disallowExpectedDisbursements) {
                request.allowApprovedDisbursedAmountsOverApplied(this.allowApprovedDisbursedAmountsOverApplied);
                request.overAppliedCalculationType(this.overAppliedCalculationType);
                request.overAppliedNumber(this.overAppliedNumber);
            }
        }
        if (this.canDefineInstallmentAmount) {
            request.canDefineInstallmentAmount(this.canDefineInstallmentAmount);
        }
        // Always send allowFullTermForTranche when it's true (for validation testing of single-disburse scenarios)
        if (this.allowFullTermForTranche && !this.multiDisburseLoan) {
            request.allowFullTermForTranche(this.allowFullTermForTranche);
        }

        if (this.fullAccountingConfig != null) {
            this.fullAccountingConfig.applyTo(request);
        } else if (this.accountingRule.equals(ACCRUAL_UPFRONT) || this.accountingRule.equals(ACCRUAL_PERIODIC)) {
            applyAccountMappingForAccrualBased(request, this.feeAndPenaltyAssetAccount);
        } else if (this.accountingRule.equals(CASH_BASED)) {
            applyAccountMappingForCashBased(request);
        }
        request.daysInMonthType(toInteger(this.daysInMonthType));
        request.daysInYearType(toInteger(this.daysInYearType));
        request.isInterestRecalculationEnabled(this.isInterestRecalculationEnabled);
        if (this.isInterestRecalculationEnabled) {
            request.interestRecalculationCompoundingMethod(toInteger(this.interestRecalculationCompoundingMethod));
            request.rescheduleStrategyMethod(toInteger(this.rescheduleStrategyMethod));
            request.recalculationRestFrequencyType(toInteger(this.recalculationRestFrequencyType));
            request.recalculationRestFrequencyInterval(toInteger(this.recalculationRestFrequencyInterval));
            if (!RECALCULATION_COMPOUNDING_METHOD_NONE.equals(this.interestRecalculationCompoundingMethod)) {
                request.recalculationCompoundingFrequencyType(toInteger(this.recalculationCompoundingFrequencyType));
                request.recalculationCompoundingFrequencyInterval(toInteger(this.recalculationCompoundingFrequencyInterval));
            }
            request.preClosureInterestCalculationStrategy(toInteger(this.preCloseInterestCalculationStrategy));
            if (this.isArrearsBasedOnOriginalSchedule != null) {
                request.isArrearsBasedOnOriginalSchedule(Boolean.valueOf(this.isArrearsBasedOnOriginalSchedule));
            }
            request.recalculationCompoundingFrequencyOnDayType(this.recalculationCompoundingFrequencyOnDayType);
            request.recalculationCompoundingFrequencyDayOfWeekType(this.recalculationCompoundingFrequencyDayOfWeekType);
            request.recalculationRestFrequencyOnDayType(this.recalculationRestFrequencyOnDayType);
            request.recalculationRestFrequencyDayOfWeekType(this.recalculationRestFrequencyDayOfWeekType);
        }
        if (this.holdGuaranteeFunds != null) {
            request.holdGuaranteeFunds(this.holdGuaranteeFunds);
        }
        request.graceOnPrincipalPayment(toInteger(this.graceOnPrincipalPayment));
        request.graceOnInterestPayment(toInteger(this.graceOnInterestPayment));
        if (this.allowAttributeOverrides != null) {
            request.allowAttributeOverrides(toAllowAttributeOverrides(this.allowAttributeOverrides));
        }
        request.allowPartialPeriodInterestCalculation(this.allowPartialPeriodInterestCalculation);
        request.allowVariableInstallments(this.allowVariableInstallments);
        if (this.installmentAmountInMultiplesOf != null) {
            request.installmentAmountInMultiplesOf(toInteger(this.installmentAmountInMultiplesOf));
        }

        // Delinquency Bucket
        if (delinquencyBucketId != null) {
            request.delinquencyBucketId(delinquencyBucketId);
        }
        if (this.delinquencyBucketId != null) {
            request.delinquencyBucketId(this.delinquencyBucketId);
        }

        if (this.feeToIncomeAccountMappings != null) {
            request.feeToIncomeAccountMappings(toChargeToGLAccountMappers(this.feeToIncomeAccountMappings));
        }
        if (this.penaltyToIncomeAccountMappings != null) {
            request.penaltyToIncomeAccountMappings(toChargeToGLAccountMappers(this.penaltyToIncomeAccountMappings));
        }
        if (this.chargeOffReasonToExpenseAccountMappings != null) {
            request.chargeOffReasonToExpenseAccountMappings(toChargeOffReasonMappings(this.chargeOffReasonToExpenseAccountMappings));
        }
        if (this.dueDaysForRepaymentEvent != null) {
            request.dueDaysForRepaymentEvent(this.dueDaysForRepaymentEvent);
        }
        if (this.overDueDaysForRepaymentEvent != null) {
            request.overDueDaysForRepaymentEvent(this.overDueDaysForRepaymentEvent);
        }
        request.enableDownPayment(this.enableDownPayment);
        if (this.disbursedAmountPercentageForDownPayment != null) {
            request.disbursedAmountPercentageForDownPayment(toBigDecimal(this.disbursedAmountPercentageForDownPayment));
        }
        if (this.enableAutoRepaymentForDownPayment) {
            request.enableAutoRepaymentForDownPayment(this.enableAutoRepaymentForDownPayment);
        }
        if (this.interestRecognitionOnDisbursementDate) {
            request.interestRecognitionOnDisbursementDate(this.interestRecognitionOnDisbursementDate);
        }
        if (this.repaymentStartDateType != null) {
            request.repaymentStartDateType(this.repaymentStartDateType);
        }
        if (this.supportedInterestRefundTypes != null) {
            request.supportedInterestRefundTypes(this.supportedInterestRefundTypes);
        }
        if (this.chargeOffBehaviour != null) {
            request.chargeOffBehaviour(this.chargeOffBehaviour);
        }
        if (this.enableBuyDownFee != null) {
            request.enableBuyDownFee(this.enableBuyDownFee);
        }
        if (this.merchantBuyDownFee != null) {
            request.merchantBuyDownFee(this.merchantBuyDownFee);
        }
        return request;
    }

    /**
     * The map form carries every amount as a string, sometimes with thousands separators ("15,000.00"). Strip them the
     * way the JSON path used to before parsing.
     */
    private static String stripThousandsSeparators(final String value) {
        return value.replaceAll("(?<=\\d),(?=\\d{3}(?!\\d))", "");
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isEmpty();
    }

    private static Integer toInteger(final String value) {
        return isBlank(value) ? null : Integer.valueOf(stripThousandsSeparators(value));
    }

    private static Long toLong(final String value) {
        return isBlank(value) ? null : Long.valueOf(stripThousandsSeparators(value));
    }

    private static Double toDouble(final String value) {
        return isBlank(value) ? null : Double.valueOf(stripThousandsSeparators(value));
    }

    private static BigDecimal toBigDecimal(final String value) {
        return isBlank(value) ? null : new BigDecimal(stripThousandsSeparators(value));
    }

    private static AllowAttributeOverrides toAllowAttributeOverrides(final JsonObject overrides) {
        final AllowAttributeOverrides result = new AllowAttributeOverrides();
        result.amortizationType(readBoolean(overrides, "amortizationType"));
        result.graceOnArrearsAgeing(readBoolean(overrides, "graceOnArrearsAgeing"));
        result.graceOnPrincipalAndInterestPayment(readBoolean(overrides, "graceOnPrincipalAndInterestPayment"));
        result.inArrearsTolerance(readBoolean(overrides, "inArrearsTolerance"));
        result.interestCalculationPeriodType(readBoolean(overrides, "interestCalculationPeriodType"));
        result.interestType(readBoolean(overrides, "interestType"));
        result.repaymentEvery(readBoolean(overrides, "repaymentEvery"));
        result.transactionProcessingStrategyCode(readBoolean(overrides, "transactionProcessingStrategyCode"));
        return result;
    }

    private static Boolean readBoolean(final JsonObject source, final String member) {
        return (source.has(member) && !source.get(member).isJsonNull()) ? source.get(member).getAsBoolean() : null;
    }

    private static List<LoanProductChargeToGLAccountMapper> toChargeToGLAccountMappers(final List<Map<String, Long>> mappings) {
        final List<LoanProductChargeToGLAccountMapper> mappers = new ArrayList<>();
        for (Map<String, Long> mapping : mappings) {
            mappers.add(new LoanProductChargeToGLAccountMapper().chargeId(mapping.get("chargeId"))
                    .incomeAccountId(mapping.get("incomeAccountId")));
        }
        return mappers;
    }

    private static List<PostChargeOffReasonToExpenseAccountMappings> toChargeOffReasonMappings(final List<Map<String, Long>> mappings) {
        final List<PostChargeOffReasonToExpenseAccountMappings> result = new ArrayList<>();
        for (Map<String, Long> mapping : mappings) {
            result.add(
                    new PostChargeOffReasonToExpenseAccountMappings().chargeOffReasonCodeValueId(mapping.get("chargeOffReasonCodeValueId"))
                            .expenseAccountId(mapping.get("expenseAccountId")));
        }
        return result;
    }

    private void applyAccountMappingForCashBased(final PostLoanProductsRequest request) {
        for (Account account : this.accountList) {
            final Long id = account.getAccountID().longValue();
            switch (account.getAccountType()) {
                case ASSET -> request.fundSourceAccountId(id).loanPortfolioAccountId(id).transfersInSuspenseAccountId(id);
                case INCOME -> applyIncomeAccounts(request, id);
                case EXPENSE -> applyExpenseAccounts(request, id);
                case LIABILITY -> request.overpaymentLiabilityAccountId(id);
                default -> {
                }
            }
        }
    }

    private void applyAccountMappingForAccrualBased(final PostLoanProductsRequest request, final Account feeAndPenaltyAssetAccount) {
        for (Account account : this.accountList) {
            final Long id = account.getAccountID().longValue();
            switch (account.getAccountType()) {
                case ASSET -> {
                    request.fundSourceAccountId(id).loanPortfolioAccountId(id).transfersInSuspenseAccountId(id);
                    final Long receivableId = feeAndPenaltyAssetAccount != null ? feeAndPenaltyAssetAccount.getAccountID().longValue() : id;
                    request.receivableFeeAccountId(receivableId).receivablePenaltyAccountId(receivableId).receivableInterestAccountId(id);
                }
                case INCOME -> applyIncomeAccounts(request, id);
                case EXPENSE -> applyExpenseAccounts(request, id);
                case LIABILITY -> request.overpaymentLiabilityAccountId(id);
                default -> {
                }
            }
        }
    }

    private static void applyIncomeAccounts(final PostLoanProductsRequest request, final Long id) {
        request.interestOnLoanAccountId(id).incomeFromFeeAccountId(id).incomeFromPenaltyAccountId(id).incomeFromRecoveryAccountId(id)
                .incomeFromChargeOffInterestAccountId(id).incomeFromChargeOffFeesAccountId(id).incomeFromChargeOffPenaltyAccountId(id)
                .incomeFromGoodwillCreditInterestAccountId(id).incomeFromGoodwillCreditFeesAccountId(id)
                .incomeFromGoodwillCreditPenaltyAccountId(id);
    }

    private static void applyExpenseAccounts(final PostLoanProductsRequest request, final Long id) {
        request.writeOffAccountId(id).goodwillCreditAccountId(id).chargeOffExpenseAccountId(id).chargeOffFraudExpenseAccountId(id);
    }

    public LoanProductTestBuilder withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public LoanProductTestBuilder withInstallmentAmountInMultiplesOf(String installmentAmountInMultiplesOf) {
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        return this;
    }

    public LoanProductTestBuilder withDelinquencyBucket(Long delinquencyBucketId) {
        this.delinquencyBucketId = delinquencyBucketId;
        return this;
    }

    public LoanProductTestBuilder withMinPrincipal(final String minPrincipal) {
        this.minPrincipal = minPrincipal;
        return this;
    }

    public LoanProductTestBuilder withMaxPrincipal(final String maxPrincipal) {
        this.maxPrincipal = maxPrincipal;
        return this;
    }

    public LoanProductTestBuilder withLoanName(final String loanName) {
        this.nameOfLoanProduct = loanName;
        return this;
    }

    public LoanProductTestBuilder withPrincipal(final String principal) {
        this.principal = principal;
        return this;
    }

    public LoanProductTestBuilder withShortName(final String shortName) {
        this.shortName = shortName;
        return this;
    }

    public LoanProductTestBuilder withNumberOfRepayments(final String numberOfRepayment) {
        this.numberOfRepayments = numberOfRepayment;
        return this;
    }

    public LoanProductTestBuilder withRepaymentTypeAsMonth() {
        this.repaymentFrequency = MONTHS;
        return this;
    }

    public LoanProductTestBuilder withRepaymentTypeAsWeek() {
        this.repaymentFrequency = WEEK;
        return this;
    }

    public LoanProductTestBuilder withRepaymentTypeAsDays() {
        this.repaymentFrequency = DAYS;
        return this;
    }

    public LoanProductTestBuilder withRepaymentAfterEvery(final String repaymentAfterEvery) {
        this.repaymentPeriod = repaymentAfterEvery;
        return this;
    }

    public LoanProductTestBuilder withInterestRateFrequencyTypeAsMonths() {
        this.interestRateFrequencyType = MONTHS;
        return this;
    }

    public LoanProductTestBuilder withInterestRateFrequencyTypeAsYear() {
        this.interestRateFrequencyType = YEARS;
        return this;
    }

    public LoanProductTestBuilder withinterestRatePerPeriod(final String interestRatePerPeriod) {
        this.interestRatePerPeriod = interestRatePerPeriod;
        return this;
    }

    public LoanProductTestBuilder withAmortizationTypeAsEqualPrincipalPayment() {
        this.amortizationType = EQUAL_PRINCIPAL_PAYMENTS;
        return this;
    }

    public LoanProductTestBuilder withAmortizationTypeAsEqualInstallments() {
        this.amortizationType = EQUAL_INSTALLMENTS;
        return this;
    }

    public LoanProductTestBuilder withInterestTypeAsFlat() {
        this.interestType = FLAT_BALANCE;
        return this;
    }

    public LoanProductTestBuilder withInterestTypeAsDecliningBalance() {
        this.interestType = DECLINING_BALANCE;
        return this;
    }

    public LoanProductTestBuilder withOverdueDaysForNPA(String days) {
        this.overdueDaysForNPA = days;
        return this;
    }

    public LoanProductTestBuilder withInterestCalculationPeriodTypeAsDays() {
        this.interestCalculationPeriodType = DAYS;
        return this;
    }

    public LoanProductTestBuilder withInterestCalculationPeriodTypeAsRepaymentPeriod(final Boolean allowPartialPeriodInterestCalculation) {
        this.interestCalculationPeriodType = CALCULATION_PERIOD_SAME_AS_REPAYMENT_PERIOD;
        this.allowPartialPeriodInterestCalculation = allowPartialPeriodInterestCalculation;
        return this;
    }

    public LoanProductTestBuilder withInArrearsTolerance(final String inArrearsTolerance) {
        this.inArrearsTolerance = inArrearsTolerance;
        return this;
    }

    public LoanProductTestBuilder withAccountingRuleAsNone() {
        this.accountingRule = NONE;
        return this;
    }

    public LoanProductTestBuilder withAccountingRuleAsCashBased(final Account[] account_list) {
        this.accountingRule = CASH_BASED;
        this.accountList = account_list;
        return this;
    }

    public LoanProductTestBuilder withAccountingRuleUpfrontAccrual(final Account[] account_list) {
        this.accountingRule = ACCRUAL_UPFRONT;
        this.accountList = account_list;
        return this;
    }

    public LoanProductTestBuilder withAccountingRulePeriodicAccrual(final Account[] account_list) {
        this.accountingRule = ACCRUAL_PERIODIC;
        this.accountList = account_list;
        return this;
    }

    public LoanProductTestBuilder withTranches(boolean multiDisburseLoan) {
        this.multiDisburseLoan = multiDisburseLoan;
        return this;
    }

    public LoanProductTestBuilder withEqualAmortization(boolean isEqualAmortization) {
        this.isEqualAmortization = isEqualAmortization;
        return this;
    }

    public LoanProductTestBuilder withMultiDisburse() {
        this.multiDisburseLoan = true;
        return this;
    }

    public LoanProductTestBuilder withDisallowExpectedDisbursements(boolean disallowExpectedDisbursements) {
        this.disallowExpectedDisbursements = disallowExpectedDisbursements;
        if (this.disallowExpectedDisbursements) {
            this.allowApprovedDisbursedAmountsOverApplied = true;
            this.overAppliedCalculationType = "percentage";
            this.overAppliedNumber = 100;
        }
        return this;
    }

    public LoanProductTestBuilder withDisallowExpectedDisbursements() {
        this.disallowExpectedDisbursements = true;
        return this;
    }

    public LoanProductTestBuilder withFullAccountingConfig(String accountingRule, FullAccountingConfig fullAccountingConfig) {
        this.accountingRule = accountingRule;
        this.fullAccountingConfig = fullAccountingConfig;
        this.accountList = null;
        return this;
    }

    private Map<String, String> getAccountMappingForCashBased() {
        final Map<String, String> map = new HashMap<>();
        for (int i = 0; i < this.accountList.length; i++) {
            if (this.accountList[i].getAccountType().equals(Account.AccountType.ASSET)) {
                final String ID = this.accountList[i].getAccountID().toString();
                map.put("fundSourceAccountId", ID);
                map.put("loanPortfolioAccountId", ID);
                map.put("transfersInSuspenseAccountId", ID);
            }
            if (this.accountList[i].getAccountType().equals(Account.AccountType.INCOME)) {
                final String ID = this.accountList[i].getAccountID().toString();
                map.put("interestOnLoanAccountId", ID);
                map.put("incomeFromFeeAccountId", ID);
                map.put("incomeFromPenaltyAccountId", ID);
                map.put("incomeFromRecoveryAccountId", ID);
                map.put("incomeFromChargeOffInterestAccountId", ID);
                map.put("incomeFromChargeOffFeesAccountId", ID);
                map.put("incomeFromChargeOffPenaltyAccountId", ID);
                map.put("incomeFromGoodwillCreditInterestAccountId", ID);
                map.put("incomeFromGoodwillCreditFeesAccountId", ID);
                map.put("incomeFromGoodwillCreditPenaltyAccountId", ID);
            }
            if (this.accountList[i].getAccountType().equals(Account.AccountType.EXPENSE)) {
                final String ID = this.accountList[i].getAccountID().toString();
                map.put("writeOffAccountId", ID);
                map.put("goodwillCreditAccountId", ID);
                map.put("chargeOffExpenseAccountId", ID);
                map.put("chargeOffFraudExpenseAccountId", ID);
            }
            if (this.accountList[i].getAccountType().equals(Account.AccountType.LIABILITY)) {
                final String ID = this.accountList[i].getAccountID().toString();
                map.put("overpaymentLiabilityAccountId", ID);
            }
        }
        return map;
    }

    private Map<String, String> getAccountMappingForAccrualBased(Account feeAndPenaltyAssetAccount) {
        final Map<String, String> map = new HashMap<>();
        for (int i = 0; i < this.accountList.length; i++) {
            if (this.accountList[i].getAccountType().equals(Account.AccountType.ASSET)) {
                final String ID = this.accountList[i].getAccountID().toString();
                map.put("fundSourceAccountId", ID);
                map.put("loanPortfolioAccountId", ID);
                map.put("transfersInSuspenseAccountId", ID);
                if (feeAndPenaltyAssetAccount != null) {
                    map.put("receivableFeeAccountId", feeAndPenaltyAssetAccount.getAccountID().toString());
                    map.put("receivablePenaltyAccountId", feeAndPenaltyAssetAccount.getAccountID().toString());
                } else {
                    map.put("receivableFeeAccountId", ID);
                    map.put("receivablePenaltyAccountId", ID);
                }
                map.put("receivableInterestAccountId", ID);

            }
            if (this.accountList[i].getAccountType().equals(Account.AccountType.INCOME)) {
                final String ID = this.accountList[i].getAccountID().toString();
                map.put("interestOnLoanAccountId", ID);
                map.put("incomeFromFeeAccountId", ID);
                map.put("incomeFromPenaltyAccountId", ID);
                map.put("incomeFromRecoveryAccountId", ID);
                map.put("incomeFromChargeOffInterestAccountId", ID);
                map.put("incomeFromChargeOffFeesAccountId", ID);
                map.put("incomeFromChargeOffPenaltyAccountId", ID);
                map.put("incomeFromGoodwillCreditInterestAccountId", ID);
                map.put("incomeFromGoodwillCreditFeesAccountId", ID);
                map.put("incomeFromGoodwillCreditPenaltyAccountId", ID);
            }
            if (this.accountList[i].getAccountType().equals(Account.AccountType.EXPENSE)) {
                final String ID = this.accountList[i].getAccountID().toString();
                map.put("writeOffAccountId", ID);
                map.put("goodwillCreditAccountId", ID);
                map.put("chargeOffExpenseAccountId", ID);
                map.put("chargeOffFraudExpenseAccountId", ID);
            }
            if (this.accountList[i].getAccountType().equals(Account.AccountType.LIABILITY)) {
                final String ID = this.accountList[i].getAccountID().toString();
                map.put("overpaymentLiabilityAccountId", ID);
            }
        }

        return map;
    }

    public LoanProductTestBuilder withAccounting(final String accountingRule, final Account[] account_list) {
        this.accountingRule = accountingRule;
        this.accountList = account_list;
        return this;
    }

    public LoanProductTestBuilder withDefineInstallmentAmount(final boolean canDefineInstallmentAmount) {
        this.canDefineInstallmentAmount = canDefineInstallmentAmount;
        return this;
    }

    public LoanProductTestBuilder currencyDetails(final String digitsAfterDecimal, final String inMultiplesOf) {
        this.digitsAfterDecimal = digitsAfterDecimal;
        this.inMultiplesOf = inMultiplesOf;
        return this;
    }

    public LoanProductTestBuilder withRepaymentStrategy(final String transactionProcessingStrategy) {
        this.transactionProcessingStrategyCode = transactionProcessingStrategy;
        return this;
    }

    public LoanProductTestBuilder withDaysInMonth(final String daysInMonthType) {
        this.daysInMonthType = daysInMonthType;
        return this;
    }

    public LoanProductTestBuilder withDaysInYear(final String daysInYearType) {
        this.daysInYearType = daysInYearType;
        return this;
    }

    public LoanProductTestBuilder withInterestRecalculationDetails(final String interestRecalculationCompoundingMethod,
            final String rescheduleStrategyMethod, String preCloseInterestCalculationStrategy) {
        this.isInterestRecalculationEnabled = true;
        this.interestRecalculationCompoundingMethod = interestRecalculationCompoundingMethod;
        this.rescheduleStrategyMethod = rescheduleStrategyMethod;
        this.preCloseInterestCalculationStrategy = preCloseInterestCalculationStrategy;
        return this;
    }

    public LoanProductTestBuilder withInterestRecalculationRestFrequencyDetails(final String recalculationRestFrequencyType,
            final String recalculationRestFrequencyInterval, final Integer recalculationRestFrequencyOnDayType,
            final Integer recalculationRestFrequencyDayOfWeekType) {
        this.isInterestRecalculationEnabled = true;
        this.recalculationRestFrequencyType = recalculationRestFrequencyType;
        this.recalculationRestFrequencyInterval = recalculationRestFrequencyInterval;
        this.recalculationRestFrequencyOnDayType = recalculationRestFrequencyOnDayType;
        this.recalculationRestFrequencyDayOfWeekType = recalculationRestFrequencyDayOfWeekType;
        return this;
    }

    public LoanProductTestBuilder withRecalculationRestFrequencyType(final String recalculationRestFrequencyType) {
        this.recalculationRestFrequencyType = recalculationRestFrequencyType;
        return this;
    }

    public LoanProductTestBuilder withInterestRecalculationCompoundingFrequencyDetails(final String recalculationCompoundingFrequencyType,
            final String recalculationCompoundingFrequencyInterval, final Integer recalculationCompoundingFrequencyOnDayType,
            final Integer recalculationCompoundingFrequencyDayOfWeekType) {
        this.isInterestRecalculationEnabled = true;
        this.recalculationCompoundingFrequencyType = recalculationCompoundingFrequencyType;
        this.recalculationCompoundingFrequencyInterval = recalculationCompoundingFrequencyInterval;
        this.recalculationCompoundingFrequencyOnDayType = recalculationCompoundingFrequencyOnDayType;
        this.recalculationCompoundingFrequencyDayOfWeekType = recalculationCompoundingFrequencyDayOfWeekType;
        return this;
    }

    public LoanProductTestBuilder withMinimumDaysBetweenDisbursalAndFirstRepayment(
            final String minimumDaysBetweenDisbursalAndFirstRepayment) {
        this.minimumDaysBetweenDisbursalAndFirstRepayment = minimumDaysBetweenDisbursalAndFirstRepayment;
        return this;
    }

    public LoanProductTestBuilder withArrearsConfiguration() {
        this.isArrearsBasedOnOriginalSchedule = "true";
        return this;
    }

    public LoanProductTestBuilder withOnHoldFundDetails(final String mandatoryGuarantee, final String minimumGuaranteeFromGuarantor,
            final String minimumGuaranteeFromOwnFunds) {
        this.holdGuaranteeFunds = true;
        this.mandatoryGuarantee = mandatoryGuarantee;
        this.minimumGuaranteeFromGuarantor = minimumGuaranteeFromGuarantor;
        this.minimumGuaranteeFromOwnFunds = minimumGuaranteeFromOwnFunds;
        return this;
    }

    public LoanProductTestBuilder withMoratorium(String principal, String interest) {
        this.graceOnPrincipalPayment = principal;
        this.graceOnInterestPayment = interest;
        return this;
    }

    public LoanProductTestBuilder withLoanProductConfiguration(JsonObject loanProductConfigurableAttributes) {
        this.allowAttributeOverrides = loanProductConfigurableAttributes;
        return this;
    }

    public LoanProductTestBuilder withVariableInstallmentsConfig(Boolean allowVariableInstallments, Integer minimumGap,
            Integer maximumGap) {
        this.allowVariableInstallments = allowVariableInstallments;
        this.minimumGap = minimumGap;
        this.maximumGap = maximumGap;
        return this;
    }

    public LoanProductTestBuilder withSyncExpectedWithDisbursementDate(Boolean syncExpectedWithDisbursementDate) {
        this.syncExpectedWithDisbursementDate = syncExpectedWithDisbursementDate;
        return this;
    }

    public LoanProductTestBuilder withPrinciplePercentagePerInstallment(String fixedPrincipalPercentagePerInstallment) {
        this.fixedPrincipalPercentagePerInstallment = fixedPrincipalPercentagePerInstallment;
        return this;
    }

    public LoanProductTestBuilder withMaxTrancheCount(String maxTrancheCount) {
        this.maxTrancheCount = maxTrancheCount;
        return this;
    }

    public LoanProductTestBuilder withAllowFullTermForTranche(boolean allowFullTermForTranche) {
        this.allowFullTermForTranche = allowFullTermForTranche;
        return this;
    }

    public LoanProductTestBuilder withFeeToIncomeAccountMapping(final Long chargeId, final Long accountId) {
        if (this.feeToIncomeAccountMappings == null) {
            this.feeToIncomeAccountMappings = new ArrayList<>();
        }
        Map<String, Long> newMap = new HashMap<>();
        newMap.put("chargeId", chargeId);
        newMap.put("incomeAccountId", accountId);
        this.feeToIncomeAccountMappings.add(newMap);
        return this;
    }

    public LoanProductTestBuilder withPenaltyToIncomeAccountMapping(final Long chargeId, final Long accountId) {
        if (this.penaltyToIncomeAccountMappings == null) {
            this.penaltyToIncomeAccountMappings = new ArrayList<>();
        }
        Map<String, Long> newMap = new HashMap<>();
        newMap.put("chargeId", chargeId);
        newMap.put("incomeAccountId", accountId);
        this.penaltyToIncomeAccountMappings.add(newMap);
        return this;
    }

    public LoanProductTestBuilder withFeeAndPenaltyAssetAccount(final Account account) {
        this.feeAndPenaltyAssetAccount = account;
        return this;
    }

    public LoanProductTestBuilder withDueDaysForRepaymentEvent(final Integer dueDaysForRepaymentEvent) {
        this.dueDaysForRepaymentEvent = dueDaysForRepaymentEvent;
        return this;
    }

    public LoanProductTestBuilder withOverDueDaysForRepaymentEvent(final Integer overDueDaysForRepaymentEvent) {
        this.overDueDaysForRepaymentEvent = overDueDaysForRepaymentEvent;
        return this;
    }

    public LoanProductTestBuilder withEnableDownPayment(final Boolean enableDownPayment,
            final String disbursedAmountPercentageForDownPayment, final Boolean enableAutoRepaymentForDownPayment) {
        this.enableDownPayment = enableDownPayment;
        this.disbursedAmountPercentageForDownPayment = disbursedAmountPercentageForDownPayment;
        this.enableAutoRepaymentForDownPayment = enableAutoRepaymentForDownPayment;
        return this;
    }

    public LoanProductTestBuilder addAdvancedPaymentAllocation(AdvancedPaymentData... advancedPaymentData) {
        this.transactionProcessingStrategyCode = "advanced-payment-allocation-strategy";
        this.advancedPaymentAllocations = new ArrayList<>(Arrays.stream(advancedPaymentData).toList());
        return this;
    }

    public LoanProductTestBuilder addCreditAllocations(CreditAllocationData... creditAllocationData) {
        this.creditAllocations = new ArrayList<>(Arrays.stream(creditAllocationData).toList());
        return this;
    }

    public LoanProductTestBuilder withRepaymentStartDateType(final Integer repaymentStartDateType) {
        this.repaymentStartDateType = repaymentStartDateType;
        return this;
    }

    public LoanProductTestBuilder withAllowPartialPeriodInterestCalculation(final Boolean allowPartialPeriodInterestCalculation) {
        this.allowPartialPeriodInterestCalculation = allowPartialPeriodInterestCalculation;
        return this;
    }

    public LoanProductTestBuilder withLoanScheduleType(LoanScheduleType loanScheduleType) {
        this.loanScheduleType = loanScheduleType.name();
        return this;
    }

    public LoanProductTestBuilder withLoanScheduleProcessingType(LoanScheduleProcessingType loanScheduleProcessingType) {
        this.loanScheduleProcessingType = loanScheduleProcessingType.name();
        return this;
    }

    public LoanProductTestBuilder withSupportedInterestRefundTypes(String... refundTypes) {
        this.supportedInterestRefundTypes = List.of(refundTypes);
        return this;
    }

    public LoanProductTestBuilder withChargeOffBehaviour(LoanChargeOffBehaviour chargeOffBehaviour) {
        this.chargeOffBehaviour = chargeOffBehaviour.name();
        return this;
    }

    public LoanProductTestBuilder withchargeOffReasonToExpenseAccountMappings(final Long reasonId, final Long accountId) {
        if (this.chargeOffReasonToExpenseAccountMappings == null) {
            this.chargeOffReasonToExpenseAccountMappings = new ArrayList<>();
        }
        Map<String, Long> newMap = new HashMap<>();
        newMap.put("chargeOffReasonCodeValueId", reasonId);
        newMap.put("expenseAccountId", accountId);
        this.chargeOffReasonToExpenseAccountMappings.add(newMap);
        return this;
    }

    public String getTransactionProcessingStrategyCode() {
        return transactionProcessingStrategyCode;
    }

    @Builder
    public static class FullAccountingConfig {

        private final Long fundSourceAccountId;
        private final Long loanPortfolioAccountId;
        private final Long transfersInSuspenseAccountId;
        private final Long interestOnLoanAccountId;
        private final Long incomeFromFeeAccountId;
        private final Long incomeFromPenaltyAccountId;
        private final Long incomeFromRecoveryAccountId;
        private final Long writeOffAccountId;
        private final Long overpaymentLiabilityAccountId;
        private final Long receivableInterestAccountId;
        private final Long receivableFeeAccountId;
        private final Long receivablePenaltyAccountId;
        private final Long goodwillCreditAccountId;
        private final Long incomeFromGoodwillCreditInterestAccountId;
        private final Long incomeFromGoodwillCreditFeesAccountId;
        private final Long incomeFromGoodwillCreditPenaltyAccountId;
        private final Long incomeFromChargeOffInterestAccountId;
        private final Long incomeFromChargeOffFeesAccountId;
        private final Long chargeOffExpenseAccountId;
        private final Long chargeOffFraudExpenseAccountId;
        private final Long incomeFromChargeOffPenaltyAccountId;
        private final Long accountingRule;

        public Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();
            Optional.ofNullable(fundSourceAccountId)
                    .ifPresent(fundSourceAccountId -> map.put("fundSourceAccountId", Long.toString(fundSourceAccountId)));
            Optional.ofNullable(loanPortfolioAccountId)
                    .ifPresent(loanPortfolioAccountId -> map.put("loanPortfolioAccountId", Long.toString(loanPortfolioAccountId)));
            Optional.ofNullable(transfersInSuspenseAccountId).ifPresent(
                    transfersInSuspenseAccountId -> map.put("transfersInSuspenseAccountId", Long.toString(transfersInSuspenseAccountId)));
            Optional.ofNullable(interestOnLoanAccountId)
                    .ifPresent(interestOnLoanAccountId -> map.put("interestOnLoanAccountId", Long.toString(interestOnLoanAccountId)));
            Optional.ofNullable(incomeFromFeeAccountId)
                    .ifPresent(incomeFromFeeAccountId -> map.put("incomeFromFeeAccountId", Long.toString(incomeFromFeeAccountId)));
            Optional.ofNullable(incomeFromPenaltyAccountId).ifPresent(
                    incomeFromPenaltyAccountId -> map.put("incomeFromPenaltyAccountId", Long.toString(incomeFromPenaltyAccountId)));
            Optional.ofNullable(incomeFromRecoveryAccountId).ifPresent(
                    incomeFromRecoveryAccountId -> map.put("incomeFromRecoveryAccountId", Long.toString(incomeFromRecoveryAccountId)));
            Optional.ofNullable(writeOffAccountId)
                    .ifPresent(writeOffAccountId -> map.put("writeOffAccountId", Long.toString(writeOffAccountId)));
            Optional.ofNullable(overpaymentLiabilityAccountId).ifPresent(overpaymentLiabilityAccountId -> map
                    .put("overpaymentLiabilityAccountId", Long.toString(overpaymentLiabilityAccountId)));
            Optional.ofNullable(receivableInterestAccountId).ifPresent(
                    receivableInterestAccountId -> map.put("receivableInterestAccountId", Long.toString(receivableInterestAccountId)));
            Optional.ofNullable(receivableFeeAccountId)
                    .ifPresent(receivableFeeAccountId -> map.put("receivableFeeAccountId", Long.toString(receivableFeeAccountId)));
            Optional.ofNullable(receivablePenaltyAccountId).ifPresent(
                    receivablePenaltyAccountId -> map.put("receivablePenaltyAccountId", Long.toString(receivablePenaltyAccountId)));
            Optional.ofNullable(goodwillCreditAccountId)
                    .ifPresent(goodwillCreditAccountId -> map.put("goodwillCreditAccountId", Long.toString(goodwillCreditAccountId)));
            Optional.ofNullable(incomeFromGoodwillCreditInterestAccountId).ifPresent(incomeFromGoodwillCreditInterestAccountId -> map
                    .put("incomeFromGoodwillCreditInterestAccountId", Long.toString(incomeFromGoodwillCreditInterestAccountId)));
            Optional.ofNullable(incomeFromGoodwillCreditFeesAccountId).ifPresent(incomeFromGoodwillCreditFeesAccountId -> map
                    .put("incomeFromGoodwillCreditFeesAccountId", Long.toString(incomeFromGoodwillCreditFeesAccountId)));
            Optional.ofNullable(incomeFromGoodwillCreditPenaltyAccountId).ifPresent(incomeFromGoodwillCreditPenaltyAccountId -> map
                    .put("incomeFromGoodwillCreditPenaltyAccountId", Long.toString(incomeFromGoodwillCreditPenaltyAccountId)));
            Optional.ofNullable(incomeFromChargeOffInterestAccountId).ifPresent(incomeFromChargeOffInterestAccountId -> map
                    .put("incomeFromChargeOffInterestAccountId", Long.toString(incomeFromChargeOffInterestAccountId)));
            Optional.ofNullable(incomeFromChargeOffFeesAccountId).ifPresent(incomeFromChargeOffFeesAccountId -> map
                    .put("incomeFromChargeOffFeesAccountId", Long.toString(incomeFromChargeOffFeesAccountId)));
            Optional.ofNullable(chargeOffExpenseAccountId)
                    .ifPresent(chargeOffExpenseAccountId -> map.put("chargeOffExpenseAccountId", Long.toString(chargeOffExpenseAccountId)));
            Optional.ofNullable(chargeOffFraudExpenseAccountId).ifPresent(chargeOffFraudExpenseAccountId -> map
                    .put("chargeOffFraudExpenseAccountId", Long.toString(chargeOffFraudExpenseAccountId)));
            Optional.ofNullable(incomeFromChargeOffPenaltyAccountId).ifPresent(incomeFromChargeOffPenaltyAccountId -> map
                    .put("incomeFromChargeOffPenaltyAccountId", Long.toString(incomeFromChargeOffPenaltyAccountId)));
            return map;
        }

        public void applyTo(final PostLoanProductsRequest request) {
            request.fundSourceAccountId(fundSourceAccountId);
            request.loanPortfolioAccountId(loanPortfolioAccountId);
            request.transfersInSuspenseAccountId(transfersInSuspenseAccountId);
            request.interestOnLoanAccountId(interestOnLoanAccountId);
            request.incomeFromFeeAccountId(incomeFromFeeAccountId);
            request.incomeFromPenaltyAccountId(incomeFromPenaltyAccountId);
            request.incomeFromRecoveryAccountId(incomeFromRecoveryAccountId);
            request.writeOffAccountId(writeOffAccountId);
            request.overpaymentLiabilityAccountId(overpaymentLiabilityAccountId);
            request.receivableInterestAccountId(receivableInterestAccountId);
            request.receivableFeeAccountId(receivableFeeAccountId);
            request.receivablePenaltyAccountId(receivablePenaltyAccountId);
            request.goodwillCreditAccountId(goodwillCreditAccountId);
            request.incomeFromGoodwillCreditInterestAccountId(incomeFromGoodwillCreditInterestAccountId);
            request.incomeFromGoodwillCreditFeesAccountId(incomeFromGoodwillCreditFeesAccountId);
            request.incomeFromGoodwillCreditPenaltyAccountId(incomeFromGoodwillCreditPenaltyAccountId);
            request.incomeFromChargeOffInterestAccountId(incomeFromChargeOffInterestAccountId);
            request.incomeFromChargeOffFeesAccountId(incomeFromChargeOffFeesAccountId);
            request.chargeOffExpenseAccountId(chargeOffExpenseAccountId);
            request.chargeOffFraudExpenseAccountId(chargeOffFraudExpenseAccountId);
            request.incomeFromChargeOffPenaltyAccountId(incomeFromChargeOffPenaltyAccountId);
        }
    }

    public LoanProductTestBuilder withEnableBuyDownFee(final Boolean enableBuyDownFee) {
        this.enableBuyDownFee = enableBuyDownFee;
        return this;
    }

    public LoanProductTestBuilder withMerchantBuyDownFee(final Boolean merchantBuyDownFee) {
        this.merchantBuyDownFee = merchantBuyDownFee;
        return this;
    }

    public LoanProductTestBuilder withBuyDownFeeCalculationType(final String buyDownFeeCalculationType) {
        this.buyDownFeeCalculationType = buyDownFeeCalculationType;
        return this;
    }
}
