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
package org.apache.fineract.portfolio.workingcapitalloanproduct.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.workingcapitalloanbreach.data.WorkingCapitalBreachData;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.data.WorkingCapitalNearBreachData;

/**
 * Swagger documentation classes for Working Capital Loan Products API.
 */
public final class WorkingCapitalLoanProductApiResourceSwagger {

    private WorkingCapitalLoanProductApiResourceSwagger() {}

    @Schema(description = "GetWorkingCapitalLoanNearBreach")
    public static final class GetWorkingCapitalLoanNearBreach {

        private GetWorkingCapitalLoanNearBreach() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Near Breach ABC")
        public String name;
        @Schema(example = "30")
        public Integer frequency;
        public StringEnumOptionData frequencyType;
        @Schema(example = "10.0")
        public BigDecimal threshold;
    }

    @Schema(description = "PostWorkingCapitalLoanProductsRequest")
    public static final class PostWorkingCapitalLoanProductsRequest {

        private PostWorkingCapitalLoanProductsRequest() {}

        // Details category
        @Schema(example = "Working Capital Product 1")
        public String name;
        @Schema(example = "WCP1")
        public String shortName;
        @Schema(example = "Working Capital Loan Product for merchants")
        public String description;
        @Schema(example = "2075e308-d4a8-44d9-8203-f5a947b8c2f4")
        public String externalId;
        @Schema(example = "3")
        public Long fundId;
        @Schema(example = "10 July 2022")
        public String startDate;
        @Schema(example = "10 July 2025")
        public String closeDate;

        // Currency
        @Schema(example = "USD")
        public String currencyCode;
        @Schema(example = "2")
        public Integer digitsAfterDecimal;
        @Schema(example = "1")
        public Integer inMultiplesOf;

        // Core product parameters (related detail: amortization, repayment defaults)
        @Schema(example = "EIR", allowableValues = { "EIR", "FLAT" })
        public String amortizationType;
        @Schema(example = "1")
        public Long delinquencyBucketId;
        @Schema(example = "365")
        public Integer npvDayCount;

        // Payment allocation
        public List<PostPaymentAllocation> paymentAllocation;

        // Min/max constraints and default values (related detail: principal, period rate, repayment)
        @Schema(example = "1000.00")
        public BigDecimal minPrincipal;
        @Schema(example = "10000.00")
        public BigDecimal principal;
        @Schema(example = "50000.00")
        public BigDecimal maxPrincipal;
        @Schema(example = "0.5")
        public BigDecimal minPeriodPaymentRate;
        @Schema(example = "1.0")
        public BigDecimal periodPaymentRate;
        @Schema(example = "2.0")
        public BigDecimal maxPeriodPaymentRate;
        @Schema(example = "0.0")
        public BigDecimal discount;
        @Schema(example = "30")
        public Integer repaymentEvery;
        @Schema(example = "DAYS", allowableValues = { "DAYS", "MONTHS", "YEARS" })
        public String repaymentFrequencyType;
        @Schema(example = "1")
        public Long breachId;
        @Schema(example = "1")
        public Integer delinquencyGraceDays;
        @Schema(example = "LOAN_CREATION", description = "Delinquency start type: LOAN_CREATION or DISBURSEMENT")
        public String delinquencyStartType;
        @Schema(example = "1")
        public Long nearBreachId;

        // Configurable attributes
        public PostAllowAttributeOverrides allowAttributeOverrides;

        // Accounting
        @Schema(example = "CASH_BASED", description = "NONE or CASH_BASED", allowableValues = { "NONE", "CASH_BASED" })
        public String accountingRule;
        @Schema(example = "1")
        public Long fundSourceAccountId;
        @Schema(example = "2")
        public Long loanPortfolioAccountId;
        @Schema(example = "3")
        public Long transfersInSuspenseAccountId;
        @Schema(example = "4")
        public Long deferredIncomeLiabilityAccountId;
        @Schema(example = "5")
        public Long incomeFromDiscountFeeAccountId;
        @Schema(example = "6")
        public Long incomeFromFeeAccountId;
        @Schema(example = "6")
        public Long incomeFromPenaltyAccountId;
        @Schema(example = "7")
        public Long incomeFromRecoveryAccountId;
        @Schema(example = "8")
        public Long writeOffAccountId;
        @Schema(example = "9")
        public Long overpaymentLiabilityAccountId;
        @Schema(example = "10")
        public Long incomeFromChargeOffFeesAccountId;
        @Schema(example = "11")
        public Long incomeFromChargeOffPenaltyAccountId;
        @Schema(example = "12")
        public Long incomeFromGoodwillCreditFeesAccountId;
        @Schema(example = "13")
        public Long incomeFromGoodwillCreditPenaltyAccountId;
        @Schema(example = "16")
        public Long goodwillCreditAccountId;
        @Schema(example = "17")
        public Long chargeOffExpenseAccountId;
        @Schema(example = "18")
        public Long chargeOffFraudExpenseAccountId;

        @Schema(example = "en_GB")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;

        @Schema(description = "PostPaymentAllocation")
        public static final class PostPaymentAllocation {

            private PostPaymentAllocation() {}

            @Schema(example = "DEFAULT", allowableValues = { "DEFAULT", "REPAYMENT", "DOWN_PAYMENT", "MERCHANT_ISSUED_REFUND",
                    "PAYOUT_REFUND", "GOODWILL_CREDIT", "CHARGE_REFUND", "CHARGE_ADJUSTMENT", "WAIVE_INTEREST", "CHARGE_PAYMENT",
                    "REFUND_FOR_ACTIVE_LOAN", "INTEREST_PAYMENT_WAIVER", "INTEREST_REFUND", "CAPITALIZED_INCOME_ADJUSTMENT" })
            public String transactionType;
            public List<PaymentAllocationOrder> paymentAllocationOrder;

            @Schema(description = "PaymentAllocationOrder")
            public static final class PaymentAllocationOrder {

                private PaymentAllocationOrder() {}

                @Schema(example = "PENALTY")
                public String paymentAllocationRule;
                @Schema(example = "1")
                public Integer order;
            }
        }

        @Schema(description = "PostAllowAttributeOverrides")
        public static final class PostAllowAttributeOverrides {

            private PostAllowAttributeOverrides() {}

            @Schema(example = "true")
            public Boolean delinquencyBucketClassification;
            @Schema(example = "true")
            public Boolean breach;
            @Schema(example = "true")
            public Boolean discountDefault;
            @Schema(example = "true")
            public Boolean periodPaymentFrequency;
            @Schema(example = "true")
            public Boolean periodPaymentFrequencyType;
        }
    }

    @Schema(description = "PostWorkingCapitalLoanProductsResponse")
    public static final class PostWorkingCapitalLoanProductsResponse {

        private PostWorkingCapitalLoanProductsResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "GetWorkingCapitalLoanProductsResponse")
    public static final class GetWorkingCapitalLoanProductsResponse {

        private GetWorkingCapitalLoanProductsResponse() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Working Capital Product 1")
        public String name;
        @Schema(example = "WCP1")
        public String shortName;
        @Schema(example = "Working Capital Loan Product for merchants")
        public String description;
        @Schema(example = "3")
        public Long fundId;
        @Schema(example = "Fund 1")
        public String fundName;
        @Schema(example = "[2022, 7, 10]")
        public LocalDate startDate;
        @Schema(example = "[2025, 7, 10]")
        public LocalDate closeDate;
        @Schema(example = "2075e308-d4a8-44d9-8203-f5a947b8c2f4")
        public String externalId;
        @Schema(example = "ACTIVE")
        public String status;

        // Currency
        public CurrencyData currency;

        // Core product parameters (related detail)
        public StringEnumOptionData amortizationType;
        public GetDelinquencyBucket delinquencyBucket;
        @Schema(example = "365")
        public Integer npvDayCount;
        public List<GetPaymentAllocation> paymentAllocation;

        // Min/max constraints and default values (related detail)
        @Schema(example = "1000.00")
        public BigDecimal minPrincipal;
        @Schema(example = "10000.00")
        public BigDecimal principal;
        @Schema(example = "50000.00")
        public BigDecimal maxPrincipal;
        @Schema(example = "0.5")
        public BigDecimal minPeriodPaymentRate;
        @Schema(example = "1.0")
        public BigDecimal periodPaymentRate;
        @Schema(example = "2.0")
        public BigDecimal maxPeriodPaymentRate;
        @Schema(example = "0.0")
        public BigDecimal discount;
        @Schema(example = "30")
        public Integer repaymentEvery;
        public StringEnumOptionData repaymentFrequencyType;
        @Schema(description = "Working capital breach (1:1 parity with delinquencyBucket)")
        public GetWorkingCapitalLoanBreach breach;
        public GetWorkingCapitalLoanNearBreach nearBreach;
        @Schema(example = "1")
        public Integer delinquencyGraceDays;
        public StringEnumOptionData delinquencyStartType;

        // Configurable attributes
        public GetConfigurableAttributes allowAttributeOverrides;

        // Accounting
        public StringEnumOptionData accountingRule;
        public Map<String, GLAccountData> accountingMappings;

        @Schema(description = "GetDelinquencyBucket")
        public static final class GetDelinquencyBucket {

            private GetDelinquencyBucket() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "Bucket 1")
            public String name;
            public List<GetDelinquencyRange> ranges;

            @Schema(description = "GetDelinquencyRange")
            public static final class GetDelinquencyRange {

                private GetDelinquencyRange() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "Range 1")
                public String classification;
                @Schema(example = "1")
                public Integer minimumAgeDays;
                @Schema(example = "15")
                public Integer maximumAgeDays;
            }
        }

        @Schema(description = "GetWorkingCapitalLoanBreach")
        public static final class GetWorkingCapitalLoanBreach {

            private GetWorkingCapitalLoanBreach() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "Default WCL Breach")
            public String name;
            @Schema(example = "30")
            public Integer breachFrequency;
            public StringEnumOptionData breachFrequencyType;
            public StringEnumOptionData breachAmountCalculationType;
            @Schema(example = "10.0")
            public BigDecimal breachAmount;
        }

        @Schema(description = "GetPaymentAllocation")
        public static final class GetPaymentAllocation {

            private GetPaymentAllocation() {}

            @Schema(example = "DEFAULT")
            public String transactionType;
            public List<PaymentAllocationOrder> paymentAllocationOrder;

            @Schema(description = "PaymentAllocationOrder")
            public static final class PaymentAllocationOrder {

                private PaymentAllocationOrder() {}

                @Schema(example = "PENALTY")
                public String paymentAllocationRule;
                @Schema(example = "1")
                public Integer order;
            }
        }

        @Schema(description = "GetConfigurableAttributes")
        public static final class GetConfigurableAttributes {

            private GetConfigurableAttributes() {}

            @Schema(example = "true")
            public Boolean delinquencyBucketClassification;
            @Schema(example = "true")
            public Boolean breach;
            @Schema(example = "true")
            public Boolean discountDefault;
            @Schema(example = "true")
            public Boolean periodPaymentFrequency;
            @Schema(example = "true")
            public Boolean periodPaymentFrequencyType;
        }
    }

    @Schema(description = "GetWorkingCapitalLoanProductsTemplateResponse")
    public static final class GetWorkingCapitalLoanProductsTemplateResponse {

        private GetWorkingCapitalLoanProductsTemplateResponse() {}

        public List<FundData> fundOptions;
        public List<CurrencyData> currencyOptions;
        public List<StringEnumOptionData> amortizationTypeOptions;
        public List<StringEnumOptionData> periodFrequencyTypeOptions;
        public List<WorkingCapitalBreachData> breachOptions;
        public List<WorkingCapitalNearBreachData> nearBreachOptions;
        public List<StringEnumOptionData> advancedPaymentAllocationTypes;
        public List<StringEnumOptionData> delinquencyStartTypeOptions;
        public List<StringEnumOptionData> delinquencyMinimumPaymentTypeOptions;
        public List<EnumOptionData> advancedPaymentAllocationTransactionTypes;
        public List<GetWorkingCapitalLoanProductsResponse.GetDelinquencyBucket> delinquencyBucketOptions;
        public List<StringEnumOptionData> accountingRuleOptions;
        public Map<String, Object> accountingMappingOptions;
    }

    @Schema(description = "GetWorkingCapitalLoanProductsProductIdResponse")
    public static final class GetWorkingCapitalLoanProductsProductIdResponse {

        private GetWorkingCapitalLoanProductsProductIdResponse() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Working Capital Product 1")
        public String name;
        @Schema(example = "WCP1")
        public String shortName;
        @Schema(example = "Working Capital Loan Product for merchants")
        public String description;
        @Schema(example = "3")
        public Long fundId;
        @Schema(example = "Fund 1")
        public String fundName;
        @Schema(example = "[2022, 7, 10]")
        public LocalDate startDate;
        @Schema(example = "[2025, 7, 10]")
        public LocalDate closeDate;
        @Schema(example = "2075e308-d4a8-44d9-8203-f5a947b8c2f4")
        public String externalId;
        @Schema(example = "ACTIVE")
        public String status;

        // Currency
        public CurrencyData currency;

        // Core product parameters (related detail)
        public StringEnumOptionData amortizationType;
        public GetWorkingCapitalLoanProductsResponse.GetDelinquencyBucket delinquencyBucket;
        @Schema(example = "365")
        public Integer npvDayCount;
        public List<GetWorkingCapitalLoanProductsResponse.GetPaymentAllocation> paymentAllocation;

        // Min/max constraints and default values (related detail)
        @Schema(example = "1000.00")
        public BigDecimal minPrincipal;
        @Schema(example = "10000.00")
        public BigDecimal principal;
        @Schema(example = "50000.00")
        public BigDecimal maxPrincipal;
        @Schema(example = "0.5")
        public BigDecimal minPeriodPaymentRate;
        @Schema(example = "1.0")
        public BigDecimal periodPaymentRate;
        @Schema(example = "2.0")
        public BigDecimal maxPeriodPaymentRate;
        @Schema(example = "0.0")
        public BigDecimal discount;
        @Schema(example = "30")
        public Integer repaymentEvery;
        public StringEnumOptionData repaymentFrequencyType;
        @Schema(description = "Working capital breach (1:1 parity with delinquencyBucket)")
        public GetWorkingCapitalLoanProductsResponse.GetWorkingCapitalLoanBreach breach;
        @Schema(example = "1")
        public Integer delinquencyGraceDays;
        public StringEnumOptionData delinquencyStartType;
        public GetWorkingCapitalLoanNearBreach nearBreach;

        // Configurable attributes
        public GetWorkingCapitalLoanProductsResponse.GetConfigurableAttributes allowAttributeOverrides;

        // Accounting
        public StringEnumOptionData accountingRule;
        public Map<String, GLAccountData> accountingMappings;
    }

    @Schema(description = "PutWorkingCapitalLoanProductsProductIdRequest")
    public static final class PutWorkingCapitalLoanProductsProductIdRequest {

        private PutWorkingCapitalLoanProductsProductIdRequest() {}

        // Details category
        @Schema(example = "Working Capital Product 1 Updated")
        public String name;
        @Schema(example = "WCP1")
        public String shortName;
        @Schema(example = "Updated Working Capital Loan Product for merchants")
        public String description;
        @Schema(example = "2075e308-d4a8-44d9-8203-f5a947b8c2f4")
        public String externalId;
        @Schema(example = "3")
        public Long fundId;
        @Schema(example = "10 July 2022")
        public String startDate;
        @Schema(example = "10 July 2025")
        public String closeDate;

        // Currency
        @Schema(example = "USD")
        public String currencyCode;
        @Schema(example = "2")
        public Integer digitsAfterDecimal;
        @Schema(example = "1")
        public Integer inMultiplesOf;

        // Core product parameters (related detail)
        @Schema(example = "EIR", allowableValues = { "EIR", "FLAT" })
        public String amortizationType;
        @Schema(example = "1")
        public Long delinquencyBucketId;
        @Schema(example = "365")
        public Integer npvDayCount;

        // Payment allocation
        public List<PostWorkingCapitalLoanProductsRequest.PostPaymentAllocation> paymentAllocation;

        // Min/max constraints and default values (related detail)
        @Schema(example = "1000.00")
        public BigDecimal minPrincipal;
        @Schema(example = "10000.00")
        public BigDecimal principal;
        @Schema(example = "50000.00")
        public BigDecimal maxPrincipal;
        @Schema(example = "0.5")
        public BigDecimal minPeriodPaymentRate;
        @Schema(example = "1.0")
        public BigDecimal periodPaymentRate;
        @Schema(example = "2.0")
        public BigDecimal maxPeriodPaymentRate;
        @Schema(example = "0.0")
        public BigDecimal discount;
        @Schema(example = "30")
        public Integer repaymentEvery;
        @Schema(example = "DAYS", allowableValues = { "DAYS", "MONTHS", "YEARS" })
        public String repaymentFrequencyType;
        @Schema(example = "1")
        public Long breachId;
        @Schema(example = "1")
        public Integer delinquencyGraceDays;
        @Schema(example = "LOAN_CREATION", description = "Delinquency start type: LOAN_CREATION or DISBURSEMENT")
        public String delinquencyStartType;
        @Schema(example = "1")
        public Long nearBreachId;

        // Configurable attributes
        public PostWorkingCapitalLoanProductsRequest.PostAllowAttributeOverrides allowAttributeOverrides;

        // Accounting
        @Schema(example = "CASH_BASED", description = "NONE or CASH_BASED", allowableValues = { "NONE", "CASH_BASED" })
        public String accountingRule;
        @Schema(example = "1")
        public Long fundSourceAccountId;
        @Schema(example = "2")
        public Long loanPortfolioAccountId;
        @Schema(example = "3")
        public Long transfersInSuspenseAccountId;
        @Schema(example = "4")
        public Long deferredIncomeLiabilityAccountId;
        @Schema(example = "5")
        public Long incomeFromDiscountFeeAccountId;
        @Schema(example = "6")
        public Long incomeFromFeeAccountId;
        @Schema(example = "6")
        public Long incomeFromPenaltyAccountId;
        @Schema(example = "7")
        public Long incomeFromRecoveryAccountId;
        @Schema(example = "8")
        public Long writeOffAccountId;
        @Schema(example = "9")
        public Long overpaymentLiabilityAccountId;
        @Schema(example = "10")
        public Long incomeFromChargeOffFeesAccountId;
        @Schema(example = "11")
        public Long incomeFromChargeOffPenaltyAccountId;
        @Schema(example = "12")
        public Long incomeFromGoodwillCreditFeesAccountId;
        @Schema(example = "13")
        public Long incomeFromGoodwillCreditPenaltyAccountId;
        @Schema(example = "16")
        public Long goodwillCreditAccountId;
        @Schema(example = "17")
        public Long chargeOffExpenseAccountId;
        @Schema(example = "18")
        public Long chargeOffFraudExpenseAccountId;

        @Schema(example = "en_GB")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
    }

    @Schema(description = "PutWorkingCapitalLoanProductsProductIdResponse")
    public static final class PutWorkingCapitalLoanProductsProductIdResponse {

        private PutWorkingCapitalLoanProductsProductIdResponse() {}

        static final class PutWorkingCapitalLoanProductChanges {

            private PutWorkingCapitalLoanProductChanges() {}

            @Schema(example = "Working Capital Product 1 Updated")
            public String name;
            @Schema(example = "en_GB")
            public String locale;
        }

        @Schema(example = "1")
        public Long resourceId;
        public PutWorkingCapitalLoanProductChanges changes;
    }

    @Schema(description = "DeleteWorkingCapitalLoanProductsProductIdResponse")
    public static final class DeleteWorkingCapitalLoanProductsProductIdResponse {

        private DeleteWorkingCapitalLoanProductsProductIdResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }
}
