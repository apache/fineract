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
 * Created by Chirag Gupta on 12/26/17.
 */
final class RecurringDepositAccountsApiResourceSwagger {

    private RecurringDepositAccountsApiResourceSwagger() {}

    @Schema(description = "GetRecurringDepositAccountsTemplateResponse")
    public static final class GetRecurringDepositAccountsTemplateResponse {

        private GetRecurringDepositAccountsTemplateResponse() {}

        static final class GetRecurringProductOptions {

            private GetRecurringProductOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Passbook Savings")
            public String name;
        }

        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "small business")
        public String clientName;
        public Set<GetRecurringProductOptions> productOptions;
    }

    @Schema(description = "GetRecurringDepositAccountsResponse")
    public static final class GetRecurringDepositAccountsResponse {

        private GetRecurringDepositAccountsResponse() {}

        static final class GetRecurringDepositAccountsStatus {

            private GetRecurringDepositAccountsStatus() {}

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

        static final class GetRecurringDepositAccountsTimeline {

            private GetRecurringDepositAccountsTimeline() {}

            @Schema(example = "[2014, 3, 1]")
            public LocalDate submittedOnDate;
            @Schema(example = "mifos")
            public String submittedByUsername;
            @Schema(example = "App")
            public String submittedByFirstname;
            @Schema(example = "Administrator")
            public String submittedByLastname;
        }

        static final class GetRecurringDepositAccountsCurrency {

            private GetRecurringDepositAccountsCurrency() {}

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

        static final class GetRecurringDepositAccountsInterestCompoundingPeriodType {

            private GetRecurringDepositAccountsInterestCompoundingPeriodType() {}

            @Schema(example = "4")
            public Integer id;
            @Schema(example = "savings.interest.period.savingsCompoundingInterestPeriodType.monthly")
            public String code;
            @Schema(example = "Monthly")
            public String description;
        }

        static final class GetRecurringDepositAccountsInterestPostingPeriodType {

            private GetRecurringDepositAccountsInterestPostingPeriodType() {}

            @Schema(example = "4")
            public Integer id;
            @Schema(example = "savings.interest.posting.period.savingsPostingInterestPeriodType.monthly")
            public String code;
            @Schema(example = "Monthly")
            public String description;
        }

        static final class GetRecurringDepositAccountsInterestCalculationType {

            private GetRecurringDepositAccountsInterestCalculationType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "savingsInterestCalculationType.dailybalance")
            public String code;
            @Schema(example = "Daily Balance")
            public String description;
        }

        static final class GetRecurringDepositAccountsInterestCalculationDaysInYearType {

            private GetRecurringDepositAccountsInterestCalculationDaysInYearType() {}

            @Schema(example = "365")
            public Integer id;
            @Schema(example = "savingsInterestCalculationDaysInYearType.days365")
            public String code;
            @Schema(example = "365 Days")
            public String description;
        }

        static final class GetRecurringDepositAccountsSummary {

            private GetRecurringDepositAccountsSummary() {}

            public GetRecurringDepositAccountsCurrency currency;
            @Schema(example = "0")
            public Float accountBalance;
        }

        static final class GetRecurringDepositAccountsMinDepositTermType {

            private GetRecurringDepositAccountsMinDepositTermType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "deposit.term.savingsPeriodFrequencyType.months")
            public String code;
            @Schema(example = "Months")
            public String description;
        }

        static final class GetRecurringDepositAccountsMaxDepositTermType {

            private GetRecurringDepositAccountsMaxDepositTermType() {}

            @Schema(example = "3")
            public Integer id;
            @Schema(example = "deposit.term.savingsPeriodFrequencyType.years")
            public String code;
            @Schema(example = "Years")
            public String description;
        }

        static final class GetRecurringDepositAccountsDepositPeriodFrequency {

            private GetRecurringDepositAccountsDepositPeriodFrequency() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "deposit.period.savingsPeriodFrequencyType.months")
            public String code;
            @Schema(example = "Months")
            public String description;
        }

        static final class GetRecurringDepositAccountsRecurringDepositFrequencyType {

            private GetRecurringDepositAccountsRecurringDepositFrequencyType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "recurring.deposit.savingsPeriodFrequencyType.months")
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
        @Schema(example = "RD01")
        public String savingsProductName;
        @Schema(example = "0")
        public Integer fieldOfficerId;
        public GetRecurringDepositAccountsStatus status;
        public GetRecurringDepositAccountsTimeline timeline;
        public GetRecurringDepositAccountsCurrency currency;
        public GetRecurringDepositAccountsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetRecurringDepositAccountsInterestPostingPeriodType interestPostingPeriodType;
        public GetRecurringDepositAccountsInterestCalculationType interestCalculationType;
        public GetRecurringDepositAccountsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetRecurringDepositAccountsSummary summary;
        @Schema(example = "1150")
        public Float depositAmount;
        @Schema(example = "252.59")
        public Float maturityAmount;
        @Schema(example = "[2014, 4, 3]")
        public LocalDate maturityDate;
        @Schema(example = "100")
        public Integer recurringDepositAmount;
        @Schema(example = "1")
        public Integer recurringDepositFrequency;
        public GetRecurringDepositAccountsRecurringDepositFrequencyType recurringDepositFrequencyType;
        @Schema(example = "false")
        public Boolean preClosurePenalApplicable;
        @Schema(example = "3")
        public Integer minDepositTerm;
        @Schema(example = "4")
        public Integer maxDepositTerm;
        public GetRecurringDepositAccountsMinDepositTermType minDepositTermType;
        public GetRecurringDepositAccountsMaxDepositTermType maxDepositTermType;
        @Schema(example = "6")
        public Integer depositPeriod;
        public GetRecurringDepositAccountsDepositPeriodFrequency depositPeriodFrequency;
    }

    @Schema(description = "PostRecurringDepositAccountsRequest")
    public static final class PostRecurringDepositAccountsRequest {

        private PostRecurringDepositAccountsRequest() {}

        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer productId;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "02 June 2014")
        public String submittedOnDate;
        @Schema(example = "20")
        public Integer depositPeriod;
        @Schema(example = "1")
        public Integer depositPeriodFrequencyId;
        @Schema(example = "10000")
        public Float depositAmount;
        @Schema(example = "false")
        public Boolean isCalendarInherited;
        @Schema(example = "2")
        public Integer recurringFrequency;
        @Schema(example = "1")
        public Integer recurringFrequencyType;
        @Schema(example = "2000")
        public Long mandatoryRecommendedDepositAmount;
    }

    @Schema(description = "PostRecurringDepositAccountsResponse")
    public static final class PostRecurringDepositAccountsResponse {

        private PostRecurringDepositAccountsResponse() {}

        @Schema(example = "2")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer savingsId;
        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "GetRecurringDepositAccountsAccountIdResponse")
    public static final class GetRecurringDepositAccountsAccountIdResponse {

        private GetRecurringDepositAccountsAccountIdResponse() {}

        static final class GetRecurringDepositAccountsAccountChart {

            private GetRecurringDepositAccountsAccountChart() {}

            static final class GetRecurringDepositAccountsChartSlabs {

                private GetRecurringDepositAccountsChartSlabs() {}

                static final class GetRecurringDepositAccountsPeriodType {

                    private GetRecurringDepositAccountsPeriodType() {}

                    @Schema(example = "0")
                    public Integer id;
                    @Schema(example = "interestChartPeriodType.days")
                    public String code;
                    @Schema(example = "Days")
                    public String description;
                }

                static final class GetRecurringDepositAccountsAccountChartCurrency {

                    private GetRecurringDepositAccountsAccountChartCurrency() {}

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
                public GetRecurringDepositAccountsPeriodType periodType;
                @Schema(example = "181")
                public Integer fromPeriod;
                @Schema(example = "365")
                public Integer toPeriod;
                @Schema(example = "5.5")
                public Double annualInterestRate;
                public GetRecurringDepositAccountsAccountChartCurrency currency;
            }

            static final class GetRecurringDepositAccountsPeriodTypes {

                private GetRecurringDepositAccountsPeriodTypes() {}

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
            @Schema(example = "RD000023")
            public Long accountNumber;
            public Set<GetRecurringDepositAccountsChartSlabs> chartSlabs;
            public Set<GetRecurringDepositAccountsPeriodTypes> periodTypes;
        }

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "RD000023")
        public Long accountNo;
        @Schema(example = "RD-23")
        public String externalId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "Sangamesh N")
        public String clientName;
        @Schema(example = "3")
        public Integer savingsProductId;
        @Schema(example = "RD01")
        public String savingsProductName;
        @Schema(example = "0")
        public Integer fieldOfficerId;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsStatus status;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsTimeline timeline;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsCurrency currency;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsInterestPostingPeriodType interestPostingPeriodType;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsInterestCalculationType interestCalculationType;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        @Schema(example = "false")
        public Boolean preClosurePenalApplicable;
        @Schema(example = "3")
        public Integer minDepositTerm;
        @Schema(example = "4")
        public Integer maxDepositTerm;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsMinDepositTermType minDepositTermType;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsMaxDepositTermType maxDepositTermType;
        @Schema(example = "100")
        public Integer recurringDepositAmount;
        @Schema(example = "1")
        public Integer recurringDepositFrequency;
        @Schema(example = "[2014, 4, 2]")
        public LocalDate expectedFirstDepositOnDate;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsRecurringDepositFrequencyType recurringDepositFrequencyType;
        @Schema(example = "6")
        public Integer depositPeriod;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsDepositPeriodFrequency depositPeriodFrequency;
        public GetRecurringDepositAccountsResponse.GetRecurringDepositAccountsSummary summary;
        public GetRecurringDepositAccountsAccountChart accountChart;
    }

    @Schema(description = "PutRecurringDepositAccountsAccountIdRequest")
    public static final class PutRecurringDepositAccountsAccountIdRequest {

        private PutRecurringDepositAccountsAccountIdRequest() {}

        @Schema(example = "en")
        public String locale;
        @Schema(example = "6000")
        public Integer depositAmount;
    }

    @Schema(description = "PutRecurringDepositAccountsAccountIdResponse")
    public static final class PutRecurringDepositAccountsAccountIdResponse {

        private PutRecurringDepositAccountsAccountIdResponse() {}

        static final class PutRecurringDepositAccountsChanges {

            private PutRecurringDepositAccountsChanges() {}

            @Schema(example = "6000")
            public Integer depositAmount;
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
        public PutRecurringDepositAccountsChanges changes;
    }

    @Schema(description = "PostRecurringDepositAccountsAccountIdRequest")
    public static final class PostRecurringDepositAccountsAccountIdRequest {

        private PostRecurringDepositAccountsAccountIdRequest() {}
    }

    @Schema(description = "PostRecurringDepositAccountsAccountIdResponse")
    public static final class PostRecurringDepositAccountsAccountIdResponse {

        private PostRecurringDepositAccountsAccountIdResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer savingsId;
        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "DeleteRecurringDepositAccountsResponse")
    public static final class DeleteRecurringDepositAccountsResponse {

        private DeleteRecurringDepositAccountsResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer resourceId;
    }
}
