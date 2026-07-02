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
package org.apache.fineract.integrationtests.common.workingcapitalloanproduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostAllowAttributeOverrides;
import org.apache.fineract.client.models.PostPaymentAllocation;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest.AccountingRuleEnum;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdRequest;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;

@Slf4j
public class WorkingCapitalLoanProductTestBuilder {

    private static final String DEFAULT_NAME = "Test WCP Product";
    private static final String DEFAULT_SHORT_NAME = "TWCP";
    private static final String DEFAULT_CURRENCY_CODE = "USD";
    private static final Integer DEFAULT_DECIMAL_PLACE = 2;
    private static final Integer DEFAULT_CURRENCY_IN_MULTIPLES_OF = 1;
    private static final String DEFAULT_AMORTIZATION = WorkingCapitalAmortizationType.EIR.name();
    private static final Integer DEFAULT_NPV_DAY_COUNT = 360;
    private static final BigDecimal DEFAULT_PRINCIPAL_AMOUNT = BigDecimal.valueOf(10000);
    public static final BigDecimal DEFAULT_PERIOD_PAYMENT_RATE_PERCENT = BigDecimal.valueOf(18);
    public static final BigDecimal DEFAULT_MIN_PERIOD_PAYMENT_RATE_PERCENT = BigDecimal.valueOf(5);
    public static final BigDecimal DEFAULT_MAX_PERIOD_PAYMENT_RATE_PERCENT = BigDecimal.valueOf(25);
    private static final Integer DEFAULT_PERIOD_PAYMENT_FREQUENCY = 30;
    private static final String DEFAULT_PERIOD_PAYMENT_FREQUENCY_TYPE = WorkingCapitalLoanPeriodFrequencyType.DAYS.name();
    private static final List<String> DEFAULT_PAYMENT_ALLOCATION_TYPES = List.of("DUE_PENALTY", "DUE_FEE", "DUE_PRINCIPAL",
            "IN_ADVANCE_PENALTY", "IN_ADVANCE_FEE", "IN_ADVANCE_PRINCIPAL");
    private static final AccountingRuleEnum DEFAULT_ACCOUNTING_RULE = AccountingRuleEnum.NONE;

    private String name = DEFAULT_NAME;
    private String shortName = DEFAULT_SHORT_NAME;
    private String description;
    private Long fundId;
    private String externalId;
    private String currencyCode = DEFAULT_CURRENCY_CODE;
    private Integer decimalPlace = DEFAULT_DECIMAL_PLACE;
    private Integer currencyInMultiplesOf = DEFAULT_CURRENCY_IN_MULTIPLES_OF;
    private String amortizationType = DEFAULT_AMORTIZATION;
    private Long delinquencyBucketId;
    private Integer npvDayCount = DEFAULT_NPV_DAY_COUNT;
    private BigDecimal principalAmountMin;
    private BigDecimal principalAmountDefault = DEFAULT_PRINCIPAL_AMOUNT;
    private BigDecimal principalAmountMax;
    private BigDecimal minPeriodPaymentRate;
    private BigDecimal periodPaymentRate = DEFAULT_PERIOD_PAYMENT_RATE_PERCENT;
    private BigDecimal maxPeriodPaymentRate;
    private BigDecimal discount;
    private Integer repaymentEvery = DEFAULT_PERIOD_PAYMENT_FREQUENCY;
    private String repaymentFrequencyType = DEFAULT_PERIOD_PAYMENT_FREQUENCY_TYPE;
    private Long breachId;
    private List<String> paymentAllocationTypes = DEFAULT_PAYMENT_ALLOCATION_TYPES;
    private Map<String, Boolean> allowAttributeOverrides;
    private Integer delinquencyGraceDays;
    private String delinquencyStartType;
    private Integer breachGraceDays;
    private AccountingRuleEnum accountingRule = DEFAULT_ACCOUNTING_RULE;
    private Long nearBreachId;

    // GL account IDs for accrual with deferred revenue amortization accounting
    private Long fundSourceAccountId;
    private Long loanPortfolioAccountId;
    private Long transfersInSuspenseAccountId;
    private Long incomeFromDiscountFeeAccountId;
    private Long receivableFeeAccountId;
    private Long receivablePenaltyAccountId;
    private Long incomeFromFeeAccountId;
    private Long incomeFromPenaltyAccountId;
    private Long incomeFromRecoveryAccountId;
    private Long writeOffAccountId;
    private Long overpaymentLiabilityAccountId;
    private Long deferredIncomeLiabilityAccountId;

    public WorkingCapitalLoanProductTestBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withShortName(final String shortName) {
        this.shortName = shortName;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withFundId(final Long fundId) {
        this.fundId = fundId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withExternalId(final String externalId) {
        this.externalId = externalId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withCurrencyCode(final String currencyCode) {
        this.currencyCode = currencyCode;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withDecimalPlace(final Integer decimalPlace) {
        this.decimalPlace = decimalPlace;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withCurrencyInMultiplesOf(final Integer currencyInMultiplesOf) {
        this.currencyInMultiplesOf = currencyInMultiplesOf;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withAmortizationType(final String amortizationType) {
        this.amortizationType = amortizationType;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withDelinquencyBucketId(final Long delinquencyBucketId) {
        this.delinquencyBucketId = delinquencyBucketId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withNpvDayCount(final Integer npvDayCount) {
        this.npvDayCount = npvDayCount;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withPaymentAllocationTypes(final List<String> paymentAllocationTypes) {
        this.paymentAllocationTypes = paymentAllocationTypes;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withPrincipalAmountMin(final BigDecimal principalAmountMin) {
        this.principalAmountMin = principalAmountMin;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withPrincipalAmountDefault(final BigDecimal principalAmountDefault) {
        this.principalAmountDefault = principalAmountDefault;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withPrincipalAmountMax(final BigDecimal principalAmountMax) {
        this.principalAmountMax = principalAmountMax;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withMinPeriodPaymentRate(final BigDecimal minPeriodPaymentRate) {
        this.minPeriodPaymentRate = minPeriodPaymentRate;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withPeriodPaymentRate(final BigDecimal periodPaymentRate) {
        this.periodPaymentRate = periodPaymentRate;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withMaxPeriodPaymentRate(final BigDecimal maxPeriodPaymentRate) {
        this.maxPeriodPaymentRate = maxPeriodPaymentRate;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withDiscount(final BigDecimal discount) {
        this.discount = discount;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withRepaymentEvery(final Integer repaymentEvery) {
        this.repaymentEvery = repaymentEvery;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withRepaymentFrequencyType(final String repaymentFrequencyType) {
        this.repaymentFrequencyType = repaymentFrequencyType;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withBreachId(final Long breachId) {
        this.breachId = breachId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withNearBreachId(final Long nearBreachId) {
        this.nearBreachId = nearBreachId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withAllowAttributeOverrides(final Map<String, Boolean> allowAttributeOverrides) {
        this.allowAttributeOverrides = allowAttributeOverrides;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withDelinquencyGraceDays(final Integer delinquencyGraceDays) {
        this.delinquencyGraceDays = delinquencyGraceDays;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withDelinquencyStartType(final String delinquencyStartType) {
        this.delinquencyStartType = delinquencyStartType;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withBreachGraceDays(final Integer breachGraceDays) {
        this.breachGraceDays = breachGraceDays;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withAccountingRule(final AccountingRuleEnum accountingRule) {
        this.accountingRule = accountingRule;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withFundSourceAccountId(final Long fundSourceAccountId) {
        this.fundSourceAccountId = fundSourceAccountId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withLoanPortfolioAccountId(final Long loanPortfolioAccountId) {
        this.loanPortfolioAccountId = loanPortfolioAccountId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withTransfersInSuspenseAccountId(final Long transfersInSuspenseAccountId) {
        this.transfersInSuspenseAccountId = transfersInSuspenseAccountId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withIncomeFromDiscountFeeAccountId(final Long incomeFromDiscountFeeAccountId) {
        this.incomeFromDiscountFeeAccountId = incomeFromDiscountFeeAccountId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withReceivableFeeAccountId(final Long receivableFeeAccountId) {
        this.receivableFeeAccountId = receivableFeeAccountId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withReceivablePenaltyAccountId(final Long receivablePenaltyAccountId) {
        this.receivablePenaltyAccountId = receivablePenaltyAccountId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withIncomeFromFeeAccountId(final Long incomeFromFeeAccountId) {
        this.incomeFromFeeAccountId = incomeFromFeeAccountId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withIncomeFromPenaltyAccountId(final Long incomeFromPenaltyAccountId) {
        this.incomeFromPenaltyAccountId = incomeFromPenaltyAccountId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withIncomeFromRecoveryAccountId(final Long incomeFromRecoveryAccountId) {
        this.incomeFromRecoveryAccountId = incomeFromRecoveryAccountId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withWriteOffAccountId(final Long writeOffAccountId) {
        this.writeOffAccountId = writeOffAccountId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withOverpaymentLiabilityAccountId(final Long overpaymentLiabilityAccountId) {
        this.overpaymentLiabilityAccountId = overpaymentLiabilityAccountId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withDeferredIncomeLiabilityAccountId(final Long deferredIncomeLiabilityAccountId) {
        this.deferredIncomeLiabilityAccountId = deferredIncomeLiabilityAccountId;
        return this;
    }

    public PostWorkingCapitalLoanProductsRequest build() {
        final PostWorkingCapitalLoanProductsRequest request = new PostWorkingCapitalLoanProductsRequest();
        populateCommonFields(request);
        setPaymentAllocation(request);
        setAllowAttributeOverrides(request);
        return request;
    }

    public PutWorkingCapitalLoanProductsProductIdRequest buildUpdateRequest() {
        final PutWorkingCapitalLoanProductsProductIdRequest request = new PutWorkingCapitalLoanProductsProductIdRequest();
        populateCommonFields(request);
        setPaymentAllocation(request);
        setAllowAttributeOverrides(request);
        return request;
    }

    private void populateCommonFields(final PostWorkingCapitalLoanProductsRequest request) {
        request.setName(this.name);
        request.setShortName(this.shortName);
        request.setDescription(this.description);
        request.setFundId(this.fundId);
        request.setExternalId(this.externalId);
        request.setCurrencyCode(this.currencyCode);
        request.setDigitsAfterDecimal(this.decimalPlace);
        request.setInMultiplesOf(this.currencyInMultiplesOf);
        if (this.amortizationType != null) {
            request.setAmortizationType(PostWorkingCapitalLoanProductsRequest.AmortizationTypeEnum.valueOf(this.amortizationType));
        }
        request.setDelinquencyBucketId(this.delinquencyBucketId);
        request.setNpvDayCount(this.npvDayCount);
        request.setMinPrincipal(this.principalAmountMin);
        request.setPrincipal(this.principalAmountDefault);
        request.setMaxPrincipal(this.principalAmountMax);
        request.setMinPeriodPaymentRate(this.minPeriodPaymentRate);
        request.setPeriodPaymentRate(this.periodPaymentRate);
        request.setMaxPeriodPaymentRate(this.maxPeriodPaymentRate);
        request.setDiscount(this.discount);
        request.setRepaymentEvery(this.repaymentEvery);
        if (this.repaymentFrequencyType != null) {
            request.setRepaymentFrequencyType(
                    PostWorkingCapitalLoanProductsRequest.RepaymentFrequencyTypeEnum.valueOf(this.repaymentFrequencyType));
        }
        request.setDelinquencyGraceDays(this.delinquencyGraceDays);
        request.setDelinquencyStartType(this.delinquencyStartType);
        request.setBreachGraceDays(this.breachGraceDays);
        request.setBreachId(this.breachId);
        request.setAccountingRule(this.accountingRule);
        request.setNearBreachId(this.nearBreachId);
        request.setFundSourceAccountId(this.fundSourceAccountId);
        request.setLoanPortfolioAccountId(this.loanPortfolioAccountId);
        request.setTransfersInSuspenseAccountId(this.transfersInSuspenseAccountId);
        request.setIncomeFromDiscountFeeAccountId(this.incomeFromDiscountFeeAccountId);
        request.setReceivableFeeAccountId(this.receivableFeeAccountId);
        request.setReceivablePenaltyAccountId(this.receivablePenaltyAccountId);
        request.setIncomeFromFeeAccountId(this.incomeFromFeeAccountId);
        request.setIncomeFromPenaltyAccountId(this.incomeFromPenaltyAccountId);
        request.setIncomeFromRecoveryAccountId(this.incomeFromRecoveryAccountId);
        request.setWriteOffAccountId(this.writeOffAccountId);
        request.setOverpaymentLiabilityAccountId(this.overpaymentLiabilityAccountId);
        request.setDeferredIncomeLiabilityAccountId(this.deferredIncomeLiabilityAccountId);
        request.setLocale("en_US");
        request.setDateFormat("yyyy-MM-dd");
    }

    private void populateCommonFields(final PutWorkingCapitalLoanProductsProductIdRequest request) {
        request.setName(this.name);
        request.setShortName(this.shortName);
        request.setDescription(this.description);
        request.setFundId(this.fundId);
        request.setCurrencyCode(this.currencyCode);
        request.setDigitsAfterDecimal(this.decimalPlace);
        request.setInMultiplesOf(this.currencyInMultiplesOf);
        if (this.amortizationType != null) {
            request.setAmortizationType(PutWorkingCapitalLoanProductsProductIdRequest.AmortizationTypeEnum.valueOf(this.amortizationType));
        }
        request.setDelinquencyBucketId(this.delinquencyBucketId);
        request.setNpvDayCount(this.npvDayCount);
        request.setMinPrincipal(this.principalAmountMin);
        request.setPrincipal(this.principalAmountDefault);
        request.setMaxPrincipal(this.principalAmountMax);
        request.setMinPeriodPaymentRate(this.minPeriodPaymentRate);
        request.setPeriodPaymentRate(this.periodPaymentRate);
        request.setMaxPeriodPaymentRate(this.maxPeriodPaymentRate);
        request.setDiscount(this.discount);
        request.setRepaymentEvery(this.repaymentEvery);
        if (this.repaymentFrequencyType != null) {
            request.setRepaymentFrequencyType(
                    PutWorkingCapitalLoanProductsProductIdRequest.RepaymentFrequencyTypeEnum.valueOf(this.repaymentFrequencyType));
        }
        request.setDelinquencyGraceDays(this.delinquencyGraceDays);
        request.setDelinquencyStartType(this.delinquencyStartType);
        request.setBreachGraceDays(this.breachGraceDays);
        request.setBreachId(this.breachId);
        if (this.accountingRule != null) {
            request.setAccountingRule(PutWorkingCapitalLoanProductsProductIdRequest.AccountingRuleEnum.valueOf(this.accountingRule.name()));
        }
        request.setNearBreachId(this.nearBreachId);
        request.setLocale("en_US");
        request.setDateFormat("yyyy-MM-dd");
    }

    private void setPaymentAllocation(final PostWorkingCapitalLoanProductsRequest request) {
        if (this.paymentAllocationTypes != null && !this.paymentAllocationTypes.isEmpty()) {
            PostPaymentAllocation defaultPaymentAllocation = new PostPaymentAllocation();
            defaultPaymentAllocation.setTransactionType(PostPaymentAllocation.TransactionTypeEnum.DEFAULT);
            defaultPaymentAllocation.setPaymentAllocationOrder(IntStream.range(0, this.paymentAllocationTypes.size()).mapToObj(index -> {
                PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
                paymentAllocationOrder.setOrder(index + 1);
                paymentAllocationOrder.setPaymentAllocationRule(this.paymentAllocationTypes.get(index));
                return paymentAllocationOrder;
            }).toList());
            request.setPaymentAllocation(List.of(defaultPaymentAllocation));
        }
    }

    private void setPaymentAllocation(final PutWorkingCapitalLoanProductsProductIdRequest request) {
        if (this.paymentAllocationTypes != null && !this.paymentAllocationTypes.isEmpty()) {
            PostPaymentAllocation defaultPaymentAllocation = new PostPaymentAllocation();
            defaultPaymentAllocation.setTransactionType(PostPaymentAllocation.TransactionTypeEnum.DEFAULT);
            defaultPaymentAllocation.setPaymentAllocationOrder(IntStream.range(0, this.paymentAllocationTypes.size()).mapToObj(index -> {
                PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
                paymentAllocationOrder.setOrder(index + 1);
                paymentAllocationOrder.setPaymentAllocationRule(this.paymentAllocationTypes.get(index));
                return paymentAllocationOrder;
            }).toList());
            request.setPaymentAllocation(List.of(defaultPaymentAllocation));
        }
    }

    private void setAllowAttributeOverrides(final PostWorkingCapitalLoanProductsRequest request) {
        PostAllowAttributeOverrides defaultAllowAttributeOverrides = buildPostAllowAttributeOverrides();
        request.setAllowAttributeOverrides(defaultAllowAttributeOverrides);
    }

    private void setAllowAttributeOverrides(final PutWorkingCapitalLoanProductsProductIdRequest request) {
        PostAllowAttributeOverrides defaultAllowAttributeOverrides = buildPostAllowAttributeOverrides();
        request.setAllowAttributeOverrides(defaultAllowAttributeOverrides);
    }

    private PostAllowAttributeOverrides buildPostAllowAttributeOverrides() {
        if (allowAttributeOverrides != null) {
            PostAllowAttributeOverrides defaultAllowAttributeOverrides = new PostAllowAttributeOverrides();
            for (final Map.Entry<String, Boolean> entry : this.allowAttributeOverrides.entrySet()) {
                switch (entry.getKey()) {
                    case "breach" -> defaultAllowAttributeOverrides.breach(entry.getValue());
                    case "delinquencyBucketClassification" ->
                        defaultAllowAttributeOverrides.delinquencyBucketClassification(entry.getValue());
                    case "periodPaymentFrequency" -> defaultAllowAttributeOverrides.periodPaymentFrequency(entry.getValue());
                    case "periodPaymentFrequencyType" -> defaultAllowAttributeOverrides.periodPaymentFrequencyType(entry.getValue());
                    case "discountDefault" -> defaultAllowAttributeOverrides.discountDefault(entry.getValue());
                    default -> throw new IllegalArgumentException("Unknown allow attribute override " + entry.getKey());
                }
            }
            return defaultAllowAttributeOverrides;
        } else {
            return null;
        }
    }
}
