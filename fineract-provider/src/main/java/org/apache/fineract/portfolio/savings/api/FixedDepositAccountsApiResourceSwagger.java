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
package org.apache.fineract.portfolio.savings.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/15/17.
 */
final class FixedDepositAccountsApiResourceSwagger {
    private FixedDepositAccountsApiResourceSwagger() {
    }

    @ApiModel(value = "GetFixedDepositAccountsTemplateResponse")
    public final static class GetFixedDepositAccountsTemplateResponse {
        private GetFixedDepositAccountsTemplateResponse() {
        }

        final class GetFixedDepositAccountsProductOptions {
            private GetFixedDepositAccountsProductOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Passbook Savings")
            public String name;
        }

        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "small business")
        public String clientName;
        public Set<GetFixedDepositAccountsProductOptions> productOptions;
    }

    @ApiModel(value = "GetFixedDepositAccountsResponse")
    public final static class GetFixedDepositAccountsResponse {
        private GetFixedDepositAccountsResponse() {
        }

        final class GetFixedDepositAccountsStatus {
            private GetFixedDepositAccountsStatus() {
            }

            @ApiModelProperty(example = "100")
            public Integer id;
            @ApiModelProperty(example = "savingsAccountStatusType.submitted.and.pending.approval")
            public String code;
            @ApiModelProperty(example = "Submitted and pending approval")
            public String value;
            @ApiModelProperty(example = "true")
            public Boolean submittedAndPendingApproval;
            @ApiModelProperty(example = "false")
            public Boolean approved;
            @ApiModelProperty(example = "false")
            public Boolean rejected;
            @ApiModelProperty(example = "false")
            public Boolean withdrawnByApplicant;
            @ApiModelProperty(example = "false")
            public Boolean active;
            @ApiModelProperty(example = "false")
            public Boolean closed;
            @ApiModelProperty(example = "false")
            public Boolean prematureClosed;
            @ApiModelProperty(example = "false")
            public Boolean transferInProgress;
            @ApiModelProperty(example = "false")
            public Boolean transferOnHold;
        }

        final class GetFixedDepositAccountsTimeline {
            private GetFixedDepositAccountsTimeline() {
            }

            @ApiModelProperty(example = "[2014, 3, 1]")
            public LocalDate submittedOnDate;
            @ApiModelProperty(example = "mifos")
            public String submittedByUsername;
            @ApiModelProperty(example = "App")
            public String submittedByFirstname;
            @ApiModelProperty(example = "Administrator")
            public String submittedByLastname;
        }

        final class GetFixedDepositAccountsCurrency {
            private GetFixedDepositAccountsCurrency() {
            }

            @ApiModelProperty(example = "USD")
            public String code;
            @ApiModelProperty(example = "US Dollar")
            public String name;
            @ApiModelProperty(example = "2")
            public Integer decimalPlaces;
            @ApiModelProperty(example = "1")
            public Integer inMultiplesOf;
            @ApiModelProperty(example = "$")
            public String displaySymbol;
            @ApiModelProperty(example = "currency.USD")
            public String nameCode;
            @ApiModelProperty(example = "US Dollar ($)")
            public String displayLabel;
        }

        final class GetFixedDepositAccountsInterestCompoundingPeriodType {
            private GetFixedDepositAccountsInterestCompoundingPeriodType() {
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "savings.interest.period.savingsCompoundingInterestPeriodType.monthly")
            public String code;
            @ApiModelProperty(example = "Monthly")
            public String value;
        }

        final class GetFixedDepositAccountsInterestPostingPeriodType {
            private GetFixedDepositAccountsInterestPostingPeriodType() {
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "savings.interest.posting.period.savingsPostingInterestPeriodType.monthly")
            public String code;
            @ApiModelProperty(example = "Monthly")
            public String value;
        }

        final class GetFixedDepositAccountsInterestCalculationType {
            private GetFixedDepositAccountsInterestCalculationType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "savingsInterestCalculationType.dailybalance")
            public String code;
            @ApiModelProperty(example = "Daily Balance")
            public String value;
        }

        final class GetFixedDepositAccountsInterestCalculationDaysInYearType {
            private GetFixedDepositAccountsInterestCalculationDaysInYearType() {
            }

            @ApiModelProperty(example = "365")
            public Integer id;
            @ApiModelProperty(example = "savingsInterestCalculationDaysInYearType.days365")
            public String code;
            @ApiModelProperty(example = "365 Days")
            public String value;
        }

        final class GetFixedDepositAccountsSummary {
            private GetFixedDepositAccountsSummary() {
            }

            public GetFixedDepositAccountsCurrency currency;
            @ApiModelProperty(example = "0")
            public Float accountBalance;
        }

        final class GetFixedDepositAccountsMinDepositTermType {
            private GetFixedDepositAccountsMinDepositTermType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "deposit.term.savingsPeriodFrequencyType.months")
            public String code;
            @ApiModelProperty(example = "Months")
            public String value;
        }

        final class GetFixedDepositAccountsMaxDepositTermType {
            private GetFixedDepositAccountsMaxDepositTermType() {
            }

            @ApiModelProperty(example = "3")
            public Integer id;
            @ApiModelProperty(example = "deposit.term.savingsPeriodFrequencyType.years")
            public String code;
            @ApiModelProperty(example = "Years")
            public String value;
        }

        final class GetFixedDepositAccountsDepositPeriodFrequency {
            private GetFixedDepositAccountsDepositPeriodFrequency() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "deposit.period.savingsPeriodFrequencyType.months")
            public String code;
            @ApiModelProperty(example = "Months")
            public String value;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "000000001")
        public Long accountNo;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "Sangamesh N")
        public String clientName;
        @ApiModelProperty(example = "3")
        public Integer savingsProductId;
        @ApiModelProperty(example = "FD01")
        public String savingsProductName;
        @ApiModelProperty(example = "0")
        public Integer fieldOfficerId;
        public GetFixedDepositAccountsStatus status;
        public GetFixedDepositAccountsTimeline timeline;
        public GetFixedDepositAccountsCurrency currency;
        public GetFixedDepositAccountsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetFixedDepositAccountsInterestPostingPeriodType interestPostingPeriodType;
        public GetFixedDepositAccountsInterestCalculationType interestCalculationType;
        public GetFixedDepositAccountsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetFixedDepositAccountsSummary summary;
        @ApiModelProperty(example = "false")
        public Boolean interestFreePeriodApplicable;
        @ApiModelProperty(example = "false")
        public Boolean preClosurePenalApplicable;
        @ApiModelProperty(example = "3")
        public Integer minDepositTerm;
        @ApiModelProperty(example = "4")
        public Integer maxDepositTerm;
        public GetFixedDepositAccountsMinDepositTermType minDepositTermType;
        public GetFixedDepositAccountsMaxDepositTermType maxDepositTermType;
        @ApiModelProperty(example = "5000")
        public Float depositAmount;
        @ApiModelProperty(example = "5140.25")
        public Float maturityAmount;
        @ApiModelProperty(example = "[2014, 9, 1]")
        public LocalDate maturityDate;
        @ApiModelProperty(example = "6")
        public Integer depositPeriod;
        public GetFixedDepositAccountsDepositPeriodFrequency depositPeriodFrequency;
    }

    @ApiModel(value = "PostFixedDepositAccountsRequest")
    public final static class PostFixedDepositAccountsRequest {
        private PostFixedDepositAccountsRequest() {
        }

        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer productId;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "01 March 2014")
        public String submittedOnDate;
        @ApiModelProperty(example = "5000")
        public Float depositAmount;
        @ApiModelProperty(example = "6")
        public Integer depositPeriod;
        @ApiModelProperty(example = "2")
        public Integer depositPeriodFrequencyId;
    }

    @ApiModel(value = "PostFixedDepositAccountsResponse")
    public final static class PostFixedDepositAccountsResponse {
        private PostFixedDepositAccountsResponse() {
        }

        @ApiModelProperty(example = "2")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer savingsId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "GetFixedDepositAccountsAccountIdResponse")
    public final static class GetFixedDepositAccountsAccountIdResponse {
        private GetFixedDepositAccountsAccountIdResponse() {
        }

        final class GetFixedDepositAccountsAccountChart {
            private GetFixedDepositAccountsAccountChart() {
            }

            final class GetFixedDepositAccountsChartSlabs {
                private GetFixedDepositAccountsChartSlabs() {
                }

                final class GetFixedDepositAccountsPeriodType {
                    private GetFixedDepositAccountsPeriodType() {
                    }

                    @ApiModelProperty(example = "0")
                    public Integer id;
                    @ApiModelProperty(example = "interestChartPeriodType.days")
                    public String code;
                    @ApiModelProperty(example = "Days")
                    public String value;
                }

                final class GetFixedDepositAccountsAccountChartCurrency {
                    private GetFixedDepositAccountsAccountChartCurrency() {
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

                @ApiModelProperty(example = "13")
                public Integer id;
                public GetFixedDepositAccountsPeriodType periodType;
                @ApiModelProperty(example = "181")
                public Integer fromPeriod;
                @ApiModelProperty(example = "365")
                public Integer toPeriod;
                @ApiModelProperty(example = "5.5")
                public Double annualInterestRate;
                public GetFixedDepositAccountsAccountChartCurrency currency;
            }

            final class GetFixedDepositAccountsPeriodTypes {
                private GetFixedDepositAccountsPeriodTypes() {
                }

                @ApiModelProperty(example = "0")
                public Integer id;
                @ApiModelProperty(example = "interestChartPeriodType.days")
                public String code;
                @ApiModelProperty(example = "Days")
                public String value;
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "[2013, 10, 2]")
            public LocalDate fromDate;
            @ApiModelProperty(example = "5")
            public Integer accountId;
            @ApiModelProperty(example = "FD000023")
            public Long accountNumber;
            public Set<GetFixedDepositAccountsChartSlabs> chartSlabs;
            public Set<GetFixedDepositAccountsPeriodTypes> periodTypes;
        }

        final class GetFixedDepositAccountsAccountIdCurrency {
            private GetFixedDepositAccountsAccountIdCurrency() {
            }

            @ApiModelProperty(example = "USD")
            public String code;
            @ApiModelProperty(example = "US Dollar")
            public String name;
            @ApiModelProperty(example = "2")
            public Integer decimalPlaces;
            @ApiModelProperty(example = "1")
            public Integer inMultiplesOf;
            @ApiModelProperty(example = "$")
            public String displaySymbol;
            @ApiModelProperty(example = "currency.USD")
            public String nameCode;
            @ApiModelProperty(example = "US Dollar ($)")
            public String displayLabel;
        }

        final class GetFixedDepositAccountsAccountIdSummary {
            private GetFixedDepositAccountsAccountIdSummary() {
            }

            public GetFixedDepositAccountsAccountIdCurrency currency;
            @ApiModelProperty(example = "0")
            public Float accountBalance;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "FD000023")
        public Long accountNo;
        @ApiModelProperty(example = "FD-23")
        public String externalId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "Sangamesh N")
        public String clientName;
        @ApiModelProperty(example = "3")
        public Integer savingsProductId;
        @ApiModelProperty(example = "FD01")
        public String savingsProductName;
        @ApiModelProperty(example = "0")
        public Integer fieldOfficerId;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsStatus status;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsTimeline timeline;
        public GetFixedDepositAccountsAccountIdCurrency currency;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsInterestPostingPeriodType interestPostingPeriodType;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsInterestCalculationType interestCalculationType;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        @ApiModelProperty(example = "false")
        public Boolean interestFreePeriodApplicable;
        @ApiModelProperty(example = "false")
        public Boolean preClosurePenalApplicable;
        @ApiModelProperty(example = "3")
        public Integer minDepositTerm;
        @ApiModelProperty(example = "4")
        public Integer maxDepositTerm;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsMinDepositTermType minDepositTermType;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsMaxDepositTermType maxDepositTermType;
        @ApiModelProperty(example = "5000")
        public Float depositAmount;
        @ApiModelProperty(example = "5140.25")
        public Float maturityAmount;
        @ApiModelProperty(example = "[2014, 9, 1]")
        public LocalDate maturityDate;
        @ApiModelProperty(example = "6")
        public Integer depositPeriod;
        public GetFixedDepositAccountsResponse.GetFixedDepositAccountsDepositPeriodFrequency depositPeriodFrequency;
        public GetFixedDepositAccountsAccountIdSummary summary;
        public GetFixedDepositAccountsAccountChart accountChart;
    }

    @ApiModel(value = "PutFixedDepositAccountsAccountIdRequest")
    public final static class PutFixedDepositAccountsAccountIdRequest {
        private PutFixedDepositAccountsAccountIdRequest() {
        }

        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "6000")
        public Float depositAmount;
    }

    @ApiModel(value = "PutFixedDepositAccountsAccountIdResponse")
    public final static class PutFixedDepositAccountsAccountIdResponse {
        private PutFixedDepositAccountsAccountIdResponse() {
        }

        final class PutFixedDepositAccountsChanges {
            private PutFixedDepositAccountsChanges() {
            }

            @ApiModelProperty(example = "6000")
            public Float depositAmount;
            @ApiModelProperty(example = "en")
            public String locale;
        }

        @ApiModelProperty(example = "2")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer savingsId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PutFixedDepositAccountsChanges changes;
    }

    @ApiModel(value = "PostFixedDepositAccountsAccountIdRequest")
    public final static class PostFixedDepositAccountsAccountIdRequest {
        private PostFixedDepositAccountsAccountIdRequest() {
        }
    }

    @ApiModel(value = "PostFixedDepositAccountsAccountIdResponse")
    public final static class PostFixedDepositAccountsAccountIdResponse {
        private PostFixedDepositAccountsAccountIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer savingsId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "DeleteFixedDepositAccountsAccountIdResponse")
    public final static class DeleteFixedDepositAccountsAccountIdResponse {
        private DeleteFixedDepositAccountsAccountIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }
}
