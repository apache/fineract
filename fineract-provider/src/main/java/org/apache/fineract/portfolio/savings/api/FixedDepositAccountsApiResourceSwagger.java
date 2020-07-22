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
import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/15/17.
 */
final class FixedDepositAccountsApiResourceSwagger {

    private FixedDepositAccountsApiResourceSwagger() {}

    @Schema(description = "GetFixedDepositAccountsTemplateResponse")
    public static final class GetFixedDepositAccountsTemplateResponse {

        private GetFixedDepositAccountsTemplateResponse() {}

        static final class GetFixedDepositAccountsProductOptions {

            private GetFixedDepositAccountsProductOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Passbook Savings")
            public String name;
        }

        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "small business")
        public String clientName;
        public Set<GetFixedDepositAccountsProductOptions> productOptions;
    }

    @Schema(description = "GetFixedDepositAccountsResponse")
    public static final class GetFixedDepositAccountsResponse {

        private GetFixedDepositAccountsResponse() {}

        static final class GetFixedDepositAccountsStatus {

            private GetFixedDepositAccountsStatus() {}

            @Schema(example = "100")
            public Integer id;
            @Schema(example = "savingsAccountStatusType.submitted.and.pending.approval")
            public String code;
            @Schema(example = "Submitted and pending approval")
            public String description;
            @Schema(example = "true")
            public Boolean submittedAndPendingApproval;
            @Schema(example = "false")
            public Boolean approved;
            @Schema(example = "false")
            public Boolean rejected;
            @Schema(example = "false")
            public Boolean withdrawnByApplicant;
            @Schema(example = "false")
            public Boolean active;
            @Schema(example = "false")
            public Boolean closed;
            @Schema(example = "false")
            public Boolean prematureClosed;
            @Schema(example = "false")
            public Boolean transferInProgress;
            @Schema(example = "false")
            public Boolean transferOnHold;
        }

        static final class GetFixedDepositAccountsTimeline {

            private GetFixedDepositAccountsTimeline() {}

            @Schema(example = "[2014, 3, 1]")
            public LocalDate submittedOnDate;
            @Schema(example = "mifos")
            public String submittedByUsername;
            @Schema(example = "App")
            public String submittedByFirstname;
            @Schema(example = "Administrator")
            public String submittedByLastname;
        }

        static final class GetFixedDepositAccountsCurrency {

            private GetFixedDepositAccountsCurrency() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "1")
            public Integer inMultiplesOf;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        static final class GetFixedDepositAccountsInterestCompoundingPeriodType {

            private GetFixedDepositAccountsInterestCompoundingPeriodType() {}

            @Schema(example = "4")
            public Integer id;
            @Schema(example = "savings.interest.period.savingsCompoundingInterestPeriodType.monthly")
            public String code;
            @Schema(example = "Monthly")
            public String description;
        }

        static final class GetFixedDepositAccountsInterestPostingPeriodType {

            private GetFixedDepositAccountsInterestPostingPeriodType() {}

            @Schema(example = "4")
            public Integer id;
            @Schema(example = "savings.interest.posting.period.savingsPostingInterestPeriodType.monthly")
            public String code;
            @Schema(example = "Monthly")
            public String description;
        }

        static final class GetFixedDepositAccountsInterestCalculationType {

            private GetFixedDepositAccountsInterestCalculationType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "savingsInterestCalculationType.dailybalance")
            public String code;
            @Schema(example = "Daily Balance")
            public String description;
        }

        static final class GetFixedDepositAccountsInterestCalculationDaysInYearType {

            private GetFixedDepositAccountsInterestCalculationDaysInYearType() {}

            @Schema(example = "365")
            public Integer id;
            @Schema(example = "savingsInterestCalculationDaysInYearType.days365")
            public String code;
            @Schema(example = "365 Days")
            public String description;
        }

        static final class GetFixedDepositAccountsSummary {

            private GetFixedDepositAccountsSummary() {}

            public GetFixedDepositAccountsCurrency currency;
            @Schema(example = "0")
            public Float accountBalance;
        }

        static final class GetFixedDepositAccountsMinDepositTermType {

            private GetFixedDepositAccountsMinDepositTermType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "deposit.term.savingsPeriodFrequencyType.months")
            public String code;
            @Schema(example = "Months")
            public String description;
        }

        static final class GetFixedDepositAccountsMaxDepositTermType {

            private GetFixedDepositAccountsMaxDepositTermType() {}

            @Schema(example = "3")
            public Integer id;
            @Schema(example = "deposit.term.savingsPeriodFrequencyType.years")
            public String code;
            @Schema(example = "Years")
            public String description;
        }

        static final class GetFixedDepositAccountsDepositPeriodFrequency {

            private GetFixedDepositAccountsDepositPeriodFrequency() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "deposit.period.savingsPeriodFrequencyType.months")
            public String code;
            @Schema(example = "Months")
            public String description;
        }

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "000000001")
        public Long accountNo;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "Sangamesh N")
        public String clientName;
        @Schema(example = "3")
        public Integer savingsProductId;
        @Schema(example = "FD01")
        public String savingsProductName;
        @Schema(example = "0")
        public Integer fieldOfficerId;
        public GetFixedDepositAccountsStatus status;
        public GetFixedDepositAccountsTimeline timeline;
        public GetFixedDepositAccountsCurrency currency;
        public GetFixedDepositAccountsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetFixedDepositAccountsInterestPostingPeriodType interestPostingPeriodType;
        public GetFixedDepositAccountsInterestCalculationType interestCalculationType;
        public GetFixedDepositAccountsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetFixedDepositAccountsSummary summary;
        @Schema(example = "false")
        public Boolean interestFreePeriodApplicable;
        @Schema(example = "false")
        public Boolean preClosurePenalApplicable;
        @Schema(example = "3")
        public Integer minDepositTerm;
        @Schema(example = "4")
        public Integer maxDepositTerm;
        public GetFixedDepositAccountsMinDepositTermType minDepositTermType;
        public GetFixedDepositAccountsMaxDepositTermType maxDepositTermType;
        @Schema(example = "5000")
        public Float depositAmount;
        @Schema(example = "5140.25")
        public Float maturityAmount;
        @Schema(example = "[2014, 9, 1]")
        public LocalDate maturityDate;
        @Schema(example = "6")
        public Integer depositPeriod;
        public GetFixedDepositAccountsDepositPeriodFrequency depositPeriodFrequency;
    }

    @Schema(description = "PostFixedDepositAccountsRequest")
    public static final class PostFixedDepositAccountsRequest {

        private PostFixedDepositAccountsRequest() {}

        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer productId;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "01 March 2014")
        public String submittedOnDate;
        @Schema(example = "5000")
        public Float depositAmount;
        @Schema(example = "6")
        public Integer depositPeriod;
        @Schema(example = "2")
        public Integer depositPeriodFrequencyId;
    }

    @Schema(description = "PostFixedDepositAccountsResponse")
    public static final class PostFixedDepositAccountsResponse {

        private PostFixedDepositAccountsResponse() {}

        @Schema(example = "2")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer savingsId;
        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "GetFixedDepositAccountsAccountIdResponse")
    public static final class GetFixedDepositAccountsAccountIdResponse {

        private GetFixedDepositAccountsAccountIdResponse() {}

        static final class GetFixedDepositAccountsAccountChart {

            private GetFixedDepositAccountsAccountChart() {}

            static final class GetFixedDepositAccountsChartSlabs {

                private GetFixedDepositAccountsChartSlabs() {}

                static final class GetFixedDepositAccountsPeriodType {

                    private GetFixedDepositAccountsPeriodType() {}

                    @Schema(example = "0")
                    public Integer id;
                    @Schema(example = "interestChartPeriodType.days")
                    public String code;
                    @Schema(example = "Days")
                    public String description;
                }

                static final class GetFixedDepositAccountsAccountChartCurrency {

                    private GetFixedDepositAccountsAccountChartCurrency() {}

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

                @Schema(example = "13")
                public Integer id;
                public GetFixedDepositAccountsPeriodType periodType;
                @Schema(example = "181")
                public Integer fromPeriod;
                @Schema(example = "365")
                public Integer toPeriod;
                @Schema(example = "5.5")
                public Double annualInterestRate;
                public GetFixedDepositAccountsAccountChartCurrency currency;
            }

            static final class GetFixedDepositAccountsPeriodTypes {

                private GetFixedDepositAccountsPeriodTypes() {}

                @Schema(example = "0")
                public Integer id;
                @Schema(example = "interestChartPeriodType.days")
                public String code;
                @Schema(example = "Days")
                public String description;
            }

            @Schema(example = "4")
            public Integer id;
            @Schema(example = "[2013, 10, 2]")
            public LocalDate fromDate;
            @Schema(example = "5")
            public Integer accountId;
            @Schema(example = "FD000023")
            public Long accountNumber;
            public Set<GetFixedDepositAccountsChartSlabs> chartSlabs;
            public Set<GetFixedDepositAccountsPeriodTypes> periodTypes;
        }

        static final class GetFixedDepositAccountsAccountIdCurrency {

            private GetFixedDepositAccountsAccountIdCurrency() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "1")
            public Integer inMultiplesOf;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        static final class GetFixedDepositAccountsAccountIdSummary {

            private GetFixedDepositAccountsAccountIdSummary() {}

            public GetFixedDepositAccountsAccountIdCurrency currency;
            @Schema(example = "0")
            public Float accountBalance;
        }

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "FD000023")
        public Long accountNo;
        @Schema(example = "FD-23")
        public String externalId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "Sangamesh N")
        public String clientName;
        @Schema(example = "3")
        public Integer savingsProductId;
        @Schema(example = "FD01")
        public String savingsProductName;
        @Schema(example = "0")
        public Integer fieldOfficerId;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsStatus status;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsTimeline timeline;
        public GetFixedDepositAccountsAccountIdCurrency currency;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsInterestPostingPeriodType interestPostingPeriodType;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsInterestCalculationType interestCalculationType;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        @Schema(example = "false")
        public Boolean interestFreePeriodApplicable;
        @Schema(example = "false")
        public Boolean preClosurePenalApplicable;
        @Schema(example = "3")
        public Integer minDepositTerm;
        @Schema(example = "4")
        public Integer maxDepositTerm;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsMinDepositTermType minDepositTermType;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsMaxDepositTermType maxDepositTermType;
        @Schema(example = "5000")
        public Float depositAmount;
        @Schema(example = "5140.25")
        public Float maturityAmount;
        @Schema(example = "[2014, 9, 1]")
        public LocalDate maturityDate;
        @Schema(example = "6")
        public Integer depositPeriod;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsDepositPeriodFrequency depositPeriodFrequency;
        public GetFixedDepositAccountsAccountIdSummary summary;
        public GetFixedDepositAccountsAccountChart accountChart;
    }

    @Schema(description = "PutFixedDepositAccountsAccountIdRequest")
    public static final class PutFixedDepositAccountsAccountIdRequest {

        private PutFixedDepositAccountsAccountIdRequest() {}

        @Schema(example = "en")
        public String locale;
        @Schema(example = "6000")
        public Float depositAmount;
    }

    @Schema(description = "PutFixedDepositAccountsAccountIdResponse")
    public static final class PutFixedDepositAccountsAccountIdResponse {

        private PutFixedDepositAccountsAccountIdResponse() {}

        static final class PutFixedDepositAccountsChanges {

            private PutFixedDepositAccountsChanges() {}

            @Schema(example = "6000")
            public Float depositAmount;
            @Schema(example = "en")
            public String locale;
        }

        @Schema(example = "2")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer savingsId;
        @Schema(example = "1")
        public Integer resourceId;
        public PutFixedDepositAccountsChanges changes;
    }

    @Schema(description = "PostFixedDepositAccountsAccountIdRequest")
    public static final class PostFixedDepositAccountsAccountIdRequest {

        private PostFixedDepositAccountsAccountIdRequest() {}
    }

    @Schema(description = "PostFixedDepositAccountsAccountIdResponse")
    public static final class PostFixedDepositAccountsAccountIdResponse {

        private PostFixedDepositAccountsAccountIdResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer savingsId;
        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "DeleteFixedDepositAccountsAccountIdResponse")
    public static final class DeleteFixedDepositAccountsAccountIdResponse {

        private DeleteFixedDepositAccountsAccountIdResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer resourceId;
    }
}
