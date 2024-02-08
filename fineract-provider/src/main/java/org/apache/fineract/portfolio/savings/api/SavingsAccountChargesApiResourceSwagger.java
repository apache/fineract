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
package org.apache.fineract.portfolio.savings.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/30/17.
 */
final class SavingsAccountChargesApiResourceSwagger {

    private SavingsAccountChargesApiResourceSwagger() {}

    @Schema(description = "GetSavingsAccountsSavingsAccountIdChargesResponse")
    public static final class GetSavingsAccountsSavingsAccountIdChargesResponse {

        private GetSavingsAccountsSavingsAccountIdChargesResponse() {}

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

        static final class GetChargesChargeCalculationType {

            private GetChargesChargeCalculationType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargeCalculationType.flat")
            public String code;
            @Schema(example = "Flat")
            public String description;
        }

        static final class GetChargesChargeTimeType {

            private GetChargesChargeTimeType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargeTimeType.specifiedDueDate")
            public String code;
            @Schema(example = "Specified due date")
            public String description;
        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "3")
        public Long chargeId;
        @Schema(example = "57")
        public Long accountId;
        @Schema(example = "Savings account maintenance fee")
        public String name;
        public GetChargesChargeTimeType chargeTimeType;
        public GetChargesChargeCalculationType chargeCalculationType;
        @Schema(example = "0")
        public Double percentage;
        @Schema(example = "0")
        public Double amountPercentageAppliedTo;
        public GetChargesCurrencyResponse currency;
        @Schema(example = "100")
        public Float amount;
        @Schema(example = "0")
        public Float amountPaid;
        @Schema(example = "0")
        public Float amountWaived;
        @Schema(example = "0")
        public Float amountWrittenOff;
        @Schema(example = "100")
        public Float amountOutstanding;
        @Schema(example = "100")
        public Float amountOrPercentage;
        @Schema(example = "false")
        public Boolean penalty;
    }

    @Schema(description = "GetSavingsAccountsSavingsAccountIdChargesTemplateResponse")
    public static final class GetSavingsAccountsSavingsAccountIdChargesTemplateResponse {

        private GetSavingsAccountsSavingsAccountIdChargesTemplateResponse() {}

        static final class GetSavingsChargesOptions {

            private GetSavingsChargesOptions() {}

            static final class GetSavingsChargesChargeTimeType {

                private GetSavingsChargesChargeTimeType() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "chargeTimeType.specifiedDueDate")
                public String code;
                @Schema(example = "Specified due date")
                public String description;
            }

            static final class GetChargesAppliesTo {

                private GetChargesAppliesTo() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "chargeAppliesTo.savings")
                public String code;
                @Schema(example = "Savings")
                public String description;
            }

            @Schema(example = "2")
            public Long id;
            @Schema(example = "Passbook Fee")
            public String name;
            @Schema(example = "true")
            public Boolean active;
            @Schema(example = "false")
            public Boolean penalty;
            public GetSavingsAccountsSavingsAccountIdChargesResponse.GetChargesCurrencyResponse currency;
            @Schema(example = "100")
            public Float amount;
            public GetSavingsChargesChargeTimeType chargeTimeType;
            public GetChargesAppliesTo chargesAppliesTo;
            public GetSavingsAccountsSavingsAccountIdChargesResponse.GetChargesChargeCalculationType chargeCalculationType;
        }

        @Schema(example = "0")
        public Float amountPaid;
        @Schema(example = "0")
        public Float amountWaived;
        @Schema(example = "0")
        public Float amountWrittenOff;
        public Set<GetSavingsChargesOptions> chargeOptions;
        @Schema(example = "false")
        public Boolean penalty;
    }

    @Schema(description = "GetSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse")
    public static final class GetSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse {

        private GetSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "1")
        public Long chargeId;
        @Schema(example = "Passbook fee")
        public String name;
        public GetSavingsAccountsSavingsAccountIdChargesResponse.GetChargesChargeTimeType chargeTimeType;
        public GetSavingsAccountsSavingsAccountIdChargesResponse.GetChargesChargeCalculationType chargeCalculationType;
        @Schema(example = "0")
        public Double percentage;
        @Schema(example = "0")
        public Double amountPercentageAppliedTo;
        public GetSavingsAccountsSavingsAccountIdChargesResponse.GetChargesCurrencyResponse currency;
        @Schema(example = "100")
        public Float amount;
        @Schema(example = "0")
        public Float amountPaid;
        @Schema(example = "0")
        public Float amountWaived;
        @Schema(example = "0")
        public Float amountWrittenOff;
        @Schema(example = "100")
        public Float amountOutstanding;
        @Schema(example = "100")
        public Float amountOrPercentage;
        @Schema(example = "false")
        public Boolean penalty;
    }

    @Schema(description = "PostSavingsAccountsSavingsAccountIdChargesRequest")
    public static final class PostSavingsAccountsSavingsAccountIdChargesRequest {

        private PostSavingsAccountsSavingsAccountIdChargesRequest() {}

        @Schema(example = "2")
        public Long chargeId;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "100")
        public Float amount;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "29 April 2013")
        public String dueDate;
    }

    @Schema(description = "PostSavingsAccountsSavingsAccountIdChargesResponse")
    public static final class PostSavingsAccountsSavingsAccountIdChargesResponse {

        private PostSavingsAccountsSavingsAccountIdChargesResponse() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "1")
        public Long savingsId;
        @Schema(example = "6")
        public Long resourceId;
    }

    @Schema(description = "PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest")
    public static final class PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest {

        private PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest() {}

        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "60")
        public Float amount;
        @Schema(example = "27 March 2013")
        public String dueDate;
    }

    @Schema(description = "PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse")
    public static final class PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse {

        private PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse() {}

        static final class PutSavingsChanges {

            private PutSavingsChanges() {}

            @Schema(example = "27 March 2013")
            public String dueDate;
            @Schema(example = "dd MMMM yyyy")
            public String dateFormat;
            @Schema(example = "en")
            public String locale;
            @Schema(example = "60.0")
            public Float amount;
        }

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "1")
        public Long savingsId;
        @Schema(example = "6")
        public Long resourceId;
        public PutSavingsChanges changes;
    }

    @Schema(description = "PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest")
    public static final class PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest {

        private PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest() {}

        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "60")
        public Float amount;
        @Schema(example = "12 September 2013")
        public String dueDate;
    }

    @Schema(description = "PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse")
    public static final class PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse {

        private PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "1")
        public Long savingsId;
        @Schema(example = "2")
        public Long resourceId;
    }

    @Schema(description = "DeleteSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse")
    public static final class DeleteSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse {

        private DeleteSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "1")
        public Long savingsId;
        @Schema(example = "2")
        public Long resourceId;
    }
}
