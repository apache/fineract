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
package org.apache.fineract.portfolio.charge.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/01/17.
 */
final class ChargesApiResourceSwagger {

    @Schema(description = "GetChargesResponse")
    public static final class GetChargesResponse {

        private GetChargesResponse() {}

        static final class GetChargesCurrencyResponse {

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        static final class GetChargesTimeTypeResponse {

            private GetChargesTimeTypeResponse() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargeTimeType.disbursement")
            public String code;
            @Schema(example = "Disbursement")
            public String description;
        }

        static final class GetChargesAppliesToResponse {

            private GetChargesAppliesToResponse() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargeAppliesTo.loan")
            public String code;
            @Schema(example = "Loan")
            public String description;
        }

        static final class GetChargesCalculationTypeResponse {

            private GetChargesCalculationTypeResponse() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargeCalculationType.flat")
            public String code;
            @Schema(example = "Flat")
            public String description;
        }

        static final class GetChargesPaymentModeResponse {

            private GetChargesPaymentModeResponse() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargepaymentmode.accounttransfer")
            public String code;
            @Schema(example = "Account Transfer")
            public String description;
        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Loan Service fee")
        public String name;
        @Schema(example = "true")
        public String active;
        @Schema(example = "false")
        public String penalty;
        public GetChargesCurrencyResponse currency;
        @Schema(example = "230.56")
        public Float amount;
        public GetChargesTimeTypeResponse chargeTimeType;
        public GetChargesAppliesToResponse chargeAppliesTo;
        public GetChargesCalculationTypeResponse chargeCalculationType;
        public GetChargesPaymentModeResponse chargePaymentMode;
    }

    @Schema(description = "PostChargesRequest")
    public static final class PostChargesRequest {

        private PostChargesRequest() {}

        @Schema(example = "Loan Service fee")
        public String name;
        @Schema(example = "1")
        public Integer chargeAppliesTo;
        @Schema(example = "USD")
        public String currencyCode;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "230.56")
        public Float amount;
        @Schema(example = "1")
        public Integer chargeTimeType;
        @Schema(example = "1")
        public Integer chargeCalculationType;
        @Schema(example = "1")
        public Integer chargePaymentMode;
        @Schema(example = "true")
        public String active;
    }

    @Schema(description = "PostChargesResponse")
    public static final class PostChargesResponse {

        private PostChargesResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PutChargesChargeIdRequest")
    public static final class PutChargesChargeIdRequest {

        private PutChargesChargeIdRequest() {}

        @Schema(example = "Loan service fee(changed)")
        public String name;
    }

    @Schema(description = "PutChargesChargeIdResponse")
    public static final class PutChargesChargeIdResponse {

        private PutChargesChargeIdResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
        public PutChargesChargeIdRequest changes;
    }

    @Schema(description = "DeleteChargesChargeIdResponse")
    public static final class DeleteChargesChargeIdResponse {

        private DeleteChargesChargeIdResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "GetChargesTemplateResponse")
    public static final class GetChargesTemplateResponse {

        private GetChargesTemplateResponse() {}

        static final class GetChargesTemplateLoanChargeCalculationTypeOptions {

            private GetChargesTemplateLoanChargeCalculationTypeOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargeCalculationType.flat")
            public String code;
            @Schema(example = "Flat")
            public String description;
        }

        static final class GetChargesTemplateLoanChargeTimeTypeOptions {

            private GetChargesTemplateLoanChargeTimeTypeOptions() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "chargeTimeType.specifiedDueDate")
            public String code;
            @Schema(example = "Specified due date")
            public String description;
        }

        static final class GetChargesTemplateFeeFrequencyOptions {

            private GetChargesTemplateFeeFrequencyOptions() {}

            @Schema(example = "0")
            public Integer id;
            @Schema(example = "loanTermFrequency.periodFrequencyType.days")
            public String code;
            @Schema(example = "Days")
            public String description;
        }

        @Schema(example = "false")
        public String active;
        @Schema(example = "false")
        public String penalty;
        public Set<GetChargesResponse.GetChargesCurrencyResponse> currencyOptions;
        public Set<GetChargesResponse.GetChargesCalculationTypeResponse> chargeCalculationTypeOptions;
        public Set<GetChargesResponse.GetChargesAppliesToResponse> chargeAppliesToOptions;
        public Set<GetChargesResponse.GetChargesTimeTypeResponse> chargeTimeTypeOptions;
        public Set<GetChargesResponse.GetChargesPaymentModeResponse> chargePaymentModeOptions;
        public Set<GetChargesTemplateLoanChargeCalculationTypeOptions> loanChargeCalculationTypeOptions;
        public Set<GetChargesTemplateLoanChargeTimeTypeOptions> loanChargeTimeTypeOptions;
        public Set<GetChargesTemplateLoanChargeCalculationTypeOptions> savingsChargeCalculationTypeOptions;
        public Set<GetChargesTemplateLoanChargeTimeTypeOptions> savingsChargeTimeTypeOptions;
        public Set<GetChargesTemplateFeeFrequencyOptions> feeFrequencyOptions;
    }
}
