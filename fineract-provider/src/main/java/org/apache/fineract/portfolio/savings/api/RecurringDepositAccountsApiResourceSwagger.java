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
 * Created by Chirag Gupta on 12/26/17.
 */
final class RecurringDepositAccountsApiResourceSwagger {
    private RecurringDepositAccountsApiResourceSwagger() {
    }

    @ApiModel(value = "GetRecurringDepositAccountsTemplateResponse")
    public final static class GetRecurringDepositAccountsTemplateResponse {
        private GetRecurringDepositAccountsTemplateResponse() {
        }

        final class GetRecurringProductOptions {
            private GetRecurringProductOptions() {
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
        public Set<GetRecurringProductOptions> productOptions;
    }

    @ApiModel(value = "GetRecurringDepositAccountsResponse")
    public final static class GetRecurringDepositAccountsResponse {
        private GetRecurringDepositAccountsResponse() {
        }

        final class GetRecurringDepositAccountsStatus {
            private GetRecurringDepositAccountsStatus() {
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

        final class GetRecurringDepositAccountsTimeline {
            private GetRecurringDepositAccountsTimeline() {
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

        final class GetRecurringDepositAccountsCurrency {
            private GetRecurringDepositAccountsCurrency() {
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

        final class GetRecurringDepositAccountsInterestCompoundingPeriodType {
            private GetRecurringDepositAccountsInterestCompoundingPeriodType() {
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "savings.interest.period.savingsCompoundingInterestPeriodType.monthly")
            public String code;
            @ApiModelProperty(example = "Monthly")
            public String value;
        }

        final class GetRecurringDepositAccountsInterestPostingPeriodType {
            private GetRecurringDepositAccountsInterestPostingPeriodType() {
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "savings.interest.posting.period.savingsPostingInterestPeriodType.monthly")
            public String code;
            @ApiModelProperty(example = "Monthly")
            public String value;
        }

        final class GetRecurringDepositAccountsInterestCalculationType {
            private GetRecurringDepositAccountsInterestCalculationType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "savingsInterestCalculationType.dailybalance")
            public String code;
            @ApiModelProperty(example = "Daily Balance")
            public String value;
        }

        final class GetRecurringDepositAccountsInterestCalculationDaysInYearType {
            private GetRecurringDepositAccountsInterestCalculationDaysInYearType() {
            }

            @ApiModelProperty(example = "365")
            public Integer id;
            @ApiModelProperty(example = "savingsInterestCalculationDaysInYearType.days365")
            public String code;
            @ApiModelProperty(example = "365 Days")
            public String value;
        }

        final class GetRecurringDepositAccountsSummary {
            private GetRecurringDepositAccountsSummary() {
            }

            public GetRecurringDepositAccountsCurrency currency;
            @ApiModelProperty(example = "0")
            public Float accountBalance;
        }

        final class GetRecurringDepositAccountsMinDepositTermType {
            private GetRecurringDepositAccountsMinDepositTermType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "deposit.term.savingsPeriodFrequencyType.months")
            public String code;
            @ApiModelProperty(example = "Months")
            public String value;
        }

        final class GetRecurringDepositAccountsMaxDepositTermType {
            private GetRecurringDepositAccountsMaxDepositTermType() {
            }

            @ApiModelProperty(example = "3")
            public Integer id;
            @ApiModelProperty(example = "deposit.term.savingsPeriodFrequencyType.years")
            public String code;
            @ApiModelProperty(example = "Years")
            public String value;
        }

        final class GetRecurringDepositAccountsDepositPeriodFrequency {
            private GetRecurringDepositAccountsDepositPeriodFrequency() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "deposit.period.savingsPeriodFrequencyType.months")
            public String code;
            @ApiModelProperty(example = "Months")
            public String value;
        }

        final class GetRecurringDepositAccountsRecurringDepositFrequencyType {
            private GetRecurringDepositAccountsRecurringDepositFrequencyType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "recurring.deposit.savingsPeriodFrequencyType.months")
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
        @ApiModelProperty(example = "RD01")
        public String savingsProductName;
        @ApiModelProperty(example = "0")
        public Integer fieldOfficerId;
        public GetRecurringDepositAccountsStatus status;
        public GetRecurringDepositAccountsTimeline timeline;
        public GetRecurringDepositAccountsCurrency currency;
        public GetRecurringDepositAccountsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetRecurringDepositAccountsInterestPostingPeriodType interestPostingPeriodType;
        public GetRecurringDepositAccountsInterestCalculationType interestCalculationType;
        public GetRecurringDepositAccountsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetRecurringDepositAccountsSummary summary;
        @ApiModelProperty(example = "1150")
        public Float depositAmount;
        @ApiModelProperty(example = "252.59")
        public Float maturityAmount;
        @ApiModelProperty(example = "[2014, 4, 3]")
        public LocalDate maturityDate;
        @ApiModelProperty(example = "100")
        public Integer recurringDepositAmount;
        @ApiModelProperty(example = "1")
        public Integer recurringDepositFrequency;
        public GetRecurringDepositAccountsRecurringDepositFrequencyType recurringDepositFrequencyType;
        @ApiModelProperty(example = "false")
        public Boolean preClosurePenalApplicable;
        @ApiModelProperty(example = "3")
        public Integer minDepositTerm;
        @ApiModelProperty(example = "4")
        public Integer maxDepositTerm;
        public GetRecurringDepositAccountsMinDepositTermType minDepositTermType;
        public GetRecurringDepositAccountsMaxDepositTermType maxDepositTermType;
        @ApiModelProperty(example = "6")
        public Integer depositPeriod;
        public GetRecurringDepositAccountsDepositPeriodFrequency depositPeriodFrequency;
    }

    @ApiModel(value = "PostRecurringDepositAccountsRequest")
    public final static class PostRecurringDepositAccountsRequest {
        private PostRecurringDepositAccountsRequest() {
        }

        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer productId;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "02 June 2014")
        public String submittedOnDate;
        @ApiModelProperty(example = "20")
        public Integer depositPeriod;
        @ApiModelProperty(example = "1")
        public Integer depositPeriodFrequencyId;
        @ApiModelProperty(example = "10000")
        public Float depositAmount;
        @ApiModelProperty(example = "false")
        public Boolean isCalendarInherited;
        @ApiModelProperty(example = "2")
        public Integer recurringFrequency;
        @ApiModelProperty(example = "1")
        public Integer recurringFrequencyType;
        @ApiModelProperty(example = "2000")
        public Long mandatoryRecommendedDepositAmount;
    }

    @ApiModel(value = "PostRecurringDepositAccountsResponse")
    public final static class PostRecurringDepositAccountsResponse {
        private PostRecurringDepositAccountsResponse() {
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

    @ApiModel(value = "GetRecurringDepositAccountsAccountIdResponse")
    public final static class GetRecurringDepositAccountsAccountIdResponse {
        private GetRecurringDepositAccountsAccountIdResponse() {
        }

        final class GetRecurringDepositAccountsAccountChart {
            private GetRecurringDepositAccountsAccountChart() {
            }

            final class GetRecurringDepositAccountsChartSlabs {
                private GetRecurringDepositAccountsChartSlabs() {
                }

                final class GetRecurringDepositAccountsPeriodType {
                    private GetRecurringDepositAccountsPeriodType() {
                    }

                    @ApiModelProperty(example = "0")
                    public Integer id;
                    @ApiModelProperty(example = "interestChartPeriodType.days")
                    public String code;
                    @ApiModelProperty(example = "Days")
                    public String value;
                }

                final class GetRecurringDepositAccountsAccountChartCurrency {
                    private GetRecurringDepositAccountsAccountChartCurrency() {
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
                public GetRecurringDepositAccountsPeriodType periodType;
                @ApiModelProperty(example = "181")
                public Integer fromPeriod;
                @ApiModelProperty(example = "365")
                public Integer toPeriod;
                @ApiModelProperty(example = "5.5")
                public Double annualInterestRate;
                public GetRecurringDepositAccountsAccountChartCurrency currency;
            }

            final class GetRecurringDepositAccountsPeriodTypes {
                private GetRecurringDepositAccountsPeriodTypes() {
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
            @ApiModelProperty(example = "RD000023")
            public Long accountNumber;
            public Set<GetRecurringDepositAccountsChartSlabs> chartSlabs;
            public Set<GetRecurringDepositAccountsPeriodTypes> periodTypes;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "RD000023")
        public Long accountNo;
        @ApiModelProperty(example = "RD-23")
        public String externalId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "Sangamesh N")
        public String clientName;
        @ApiModelProperty(example = "3")
        public Integer savingsProductId;
        @ApiModelProperty(example = "RD01")
        public String savingsProductName;
        @ApiModelProperty(example = "0")
        public Integer fieldOfficerId;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsStatus status;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsTimeline timeline;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsCurrency currency;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsInterestPostingPeriodType interestPostingPeriodType;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsInterestCalculationType interestCalculationType;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        @ApiModelProperty(example = "false")
        public Boolean preClosurePenalApplicable;
        @ApiModelProperty(example = "3")
        public Integer minDepositTerm;
        @ApiModelProperty(example = "4")
        public Integer maxDepositTerm;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsMinDepositTermType minDepositTermType;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsMaxDepositTermType maxDepositTermType;
        @ApiModelProperty(example = "100")
        public Integer recurringDepositAmount;
        @ApiModelProperty(example = "1")
        public Integer recurringDepositFrequency;
        @ApiModelProperty(example = "[2014, 4, 2]")
        public LocalDate expectedFirstDepositOnDate;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsRecurringDepositFrequencyType recurringDepositFrequencyType;
        @ApiModelProperty(example = "6")
        public Integer depositPeriod;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsDepositPeriodFrequency depositPeriodFrequency;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsSummary summary;
        public GetRecurringDepositAccountsAccountChart accountChart;
    }

    @ApiModel(value = "PutRecurringDepositAccountsAccountIdRequest")
    public final static class PutRecurringDepositAccountsAccountIdRequest {
        private PutRecurringDepositAccountsAccountIdRequest() {
        }

        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "6000")
        public Integer depositAmount;
    }

    @ApiModel(value = "PutRecurringDepositAccountsAccountIdResponse")
    public final static class PutRecurringDepositAccountsAccountIdResponse {
        private PutRecurringDepositAccountsAccountIdResponse() {
        }

        final class PutRecurringDepositAccountsChanges {
            private PutRecurringDepositAccountsChanges() {
            }

            @ApiModelProperty(example = "6000")
            public Integer depositAmount;
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
        public PutRecurringDepositAccountsChanges changes;
    }

    @ApiModel(value = "PostRecurringDepositAccountsAccountIdRequest")
    public final static class PostRecurringDepositAccountsAccountIdRequest {
        private PostRecurringDepositAccountsAccountIdRequest() {
        }
    }

    @ApiModel(value = "PostRecurringDepositAccountsAccountIdResponse")
    public final static class PostRecurringDepositAccountsAccountIdResponse {
        private PostRecurringDepositAccountsAccountIdResponse() {
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

    @ApiModel(value = "DeleteRecurringDepositAccountsResponse")
    public final static class DeleteRecurringDepositAccountsResponse {
        private DeleteRecurringDepositAccountsResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }
}
