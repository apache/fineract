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
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.fund.data.FundData;

/**
 * Swagger documentation classes for Working Capital Loan Products API.
 */
public final class WorkingCapitalLoanProductApiResourceSwagger {

    private WorkingCapitalLoanProductApiResourceSwagger() {}

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
        @Schema(example = "5.5")
        public BigDecimal flatPercentageAmount;
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

        // Configurable attributes
        public PostAllowAttributeOverrides allowAttributeOverrides;

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
            public Boolean flatPercentageAmount;
            @Schema(example = "true")
            public Boolean delinquencyBucketClassification;
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
        @Schema(example = "5.5")
        public BigDecimal flatPercentageAmount;
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

        // Configurable attributes
        public GetConfigurableAttributes allowAttributeOverrides;

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
            public Boolean flatPercentageAmount;
            @Schema(example = "true")
            public Boolean delinquencyBucketClassification;
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
        public List<StringEnumOptionData> advancedPaymentAllocationTypes;
        public List<EnumOptionData> advancedPaymentAllocationTransactionTypes;
        public List<GetWorkingCapitalLoanProductsResponse.GetDelinquencyBucket> delinquencyBucketOptions;
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
        @Schema(example = "5.5")
        public BigDecimal flatPercentageAmount;
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

        // Configurable attributes
        public GetWorkingCapitalLoanProductsResponse.GetConfigurableAttributes allowAttributeOverrides;
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
        @Schema(example = "5.5")
        public BigDecimal flatPercentageAmount;
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

        // Configurable attributes
        public PostWorkingCapitalLoanProductsRequest.PostAllowAttributeOverrides allowAttributeOverrides;

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
