
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.loanaccount.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;

/**
 * Created by Chirag Gupta on 12/02/17.
 */
final class LoanChargesApiResourceSwagger {
    private LoanChargesApiResourceSwagger() {
    }

    @ApiModel(value = "GetLoansLoanIdChargesChargeIdResponse")
    public static final class GetLoansLoanIdChargesChargeIdResponse {
        private GetLoansLoanIdChargesChargeIdResponse() {
        }

        final class GetLoanChargeTimeType {
            private GetLoanChargeTimeType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "chargeTimeType.disbursement")
            public String code;
            @ApiModelProperty(example = "Disbursement")
            public String value;
        }

        final class GetLoanChargeCalculationType {
            private GetLoanChargeCalculationType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "chargeCalculationType.flat")
            public String code;
            @ApiModelProperty(example = "Flat")
            public String value;
        }

        final class GetLoanChargeCurrency {
            private GetLoanChargeCurrency() {
            }

            @ApiModelProperty(example = "USD")
            public String code;
            @ApiModelProperty(example = "US Dollar")
            public String name;
            @ApiModelProperty(example = "2")
            public Integer decimalPlaces;
            @ApiModelProperty(example = "$")
            public String displaySymbol;
            @ApiModelProperty(example = "currency.USD")
            public String nameCode;
            @ApiModelProperty(example = "US Dollar ($)")
            public String displayLabel;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "1")
        public Integer chargeId;
        @ApiModelProperty(example = "Loan Processing fee")
        public String name;
        public GetLoanChargeTimeType chargeTimeType;
        public GetLoanChargeCalculationType chargeCalculationType;
        @ApiModelProperty(example = "0")
        public Double percentage;
        @ApiModelProperty(example = "0")
        public Double amountPercentageAppliedTo;
        public GetLoanChargeCurrency currency;
        @ApiModelProperty(example = "100")
        public Float amount;
        @ApiModelProperty(example = "0")
        public Float amountPaid;
        @ApiModelProperty(example = "0")
        public Float amountWaived;
        @ApiModelProperty(example = "0")
        public Float amountWrittenOff;
        @ApiModelProperty(example = "100")
        public Float amountOutstanding;
        @ApiModelProperty(example = "100")
        public Float amountOrPercentage;
        @ApiModelProperty(example = "false")
        public Boolean penalty;
    }

    @ApiModel(value = "GetLoansLoanIdChargesTemplateResponse")
    public static final class GetLoansLoanIdChargesTemplateResponse {
        private GetLoansLoanIdChargesTemplateResponse() {
        }

        final class GetLoanChargeTemplateChargeOptions {
            private GetLoanChargeTemplateChargeOptions() {
            }

            final class GetLoanChargeTemplateChargeTimeType {
                private GetLoanChargeTemplateChargeTimeType() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "chargeTimeType.specifiedDueDate")
                public String code;
                @ApiModelProperty(example = "Specified due date")
                public String value;
            }

            final class GetLoanChargeTemplateChargeAppliesTo {
                private GetLoanChargeTemplateChargeAppliesTo() {
                }

                @ApiModelProperty(example = "1  ")
                public Integer id;
                @ApiModelProperty(example = "chargeAppliesTo.loan")
                public String code;
                @ApiModelProperty(example = "Loan")
                public String value;
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Collection fee")
            public String name;
            @ApiModelProperty(example = "true")
            public Boolean active;
            @ApiModelProperty(example = "false")
            public Boolean penalty;
            public GetLoansLoanIdChargesChargeIdResponse.GetLoanChargeCurrency currency;
            @ApiModelProperty(example = "100")
            public Float amount;
            public GetLoanChargeTemplateChargeTimeType chargeTimeType;
            public GetLoanChargeTemplateChargeAppliesTo chargeAppliesTo;
            public GetLoansLoanIdChargesChargeIdResponse.GetLoanChargeCalculationType chargeCalculationType;
        }

        @ApiModelProperty(example = "0")
        public Float amountPaid;
        @ApiModelProperty(example = "0")
        public Float amountWaived;
        @ApiModelProperty(example = "0")
        public Float amountWrittenOff;
        public Set<GetLoanChargeTemplateChargeOptions> chargeOptions;
        @ApiModelProperty(example = "false")
        public Boolean penalty;
    }

    @ApiModel(value = " PostLoansLoanIdChargesRequest")
    public static final class PostLoansLoanIdChargesRequest {
        private PostLoansLoanIdChargesRequest() {
        }

        @ApiModelProperty(example = "2")
        public Integer chargeId;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "100")
        public Float amount;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "29 April 2013")
        public String dueDate;
    }

    @ApiModel(value = " PostLoansLoanIdChargesResponse")
    public static final class PostLoansLoanIdChargesResponse {
        private PostLoansLoanIdChargesResponse() {
        }

        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "1")
        public Long clientId;
        @ApiModelProperty(example = "1")
        public Long loanId;
        @ApiModelProperty(example = "31")
        public Integer resourceId;
    }

    @ApiModel(value = " PutLoansLoanIdChargesChargeIdRequest")
    public static final class PutLoansLoanIdChargesChargeIdRequest {
        private PutLoansLoanIdChargesChargeIdRequest() {
        }

        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "60")
        public Float amount;
        @ApiModelProperty(example = "27 March 2013")
        public String dueDate;
    }

    @ApiModel(value = "PutLoansLoanIdChargesChargeIdResponse")
    public static final class PutLoansLoanIdChargesChargeIdResponse {
        private PutLoansLoanIdChargesChargeIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "1")
        public Long clientId;
        @ApiModelProperty(example = "1")
        public Long loanId;
        @ApiModelProperty(example = "6")
        public Integer resourceId;
        public PutLoansLoanIdChargesChargeIdRequest changes;
    }

    @ApiModel(value = "PostLoansLoanIdChargesChargeIdRequest")
    public static final class PostLoansLoanIdChargesChargeIdRequest {
        private PostLoansLoanIdChargesChargeIdRequest() {
        }

        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "19 September 2013")
        public String transactionDate;
    }

    @ApiModel(value = "PostLoansLoanIdChargesChargeIdResponse")
    public static final class PostLoansLoanIdChargesChargeIdResponse {
        private PostLoansLoanIdChargesChargeIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "1")
        public Long clientId;
        @ApiModelProperty(example = "6")
        public Long loanId;
        @ApiModelProperty(example = "1")
        public Long savingsId;
        @ApiModelProperty(example = "12")
        public Integer resourceId;
    }

    @ApiModel(value = "DeleteLoansLoanIdChargesChargeIdResponse")
    public static final class DeleteLoansLoanIdChargesChargeIdResponse {
        private DeleteLoansLoanIdChargesChargeIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "1")
        public Long clientId;
        @ApiModelProperty(example = "1")
        public Long loanId;
        @ApiModelProperty(example = "2")
        public Integer resourceId;
    }
}
