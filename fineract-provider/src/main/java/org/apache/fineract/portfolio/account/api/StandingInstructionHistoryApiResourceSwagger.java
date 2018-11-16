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
package org.apache.fineract.portfolio.account.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/01/17.
 */
final class StandingInstructionHistoryApiResourceSwagger {
    private StandingInstructionHistoryApiResourceSwagger() {
    }

    @ApiModel(value = "GetStandingInstructionRunHistoryResponse")
    public final static class GetStandingInstructionRunHistoryResponse {
        private GetStandingInstructionRunHistoryResponse() {
        }

        final class GetStandingInstructionHistoryPageItemsResponse {

            final class GetStandingInstructionHistoryPageItemsFromClient {
                private GetStandingInstructionHistoryPageItemsFromClient() {
                }

                @ApiModelProperty(example = "1")
                public Long id;
                @ApiModelProperty(example = "Test client")
                public String displayName;
                @ApiModelProperty(example = "1")
                public Long officeId;
                @ApiModelProperty(example = "Head Office")
                public String officeName;
            }

            final class GetStandingInstructionHistoryFromAccount {
                private GetStandingInstructionHistoryFromAccount() {
                }

                @ApiModelProperty(example = "2")
                public Long id;
                @ApiModelProperty(example = "000000002")
                public Long accountNo;
                @ApiModelProperty(example = "1")
                public Long productId;
                @ApiModelProperty(example = "General Savings")
                public String productName;
            }

            final class GetStandingInstructionHistoryToAccount {
                private GetStandingInstructionHistoryToAccount() {
                }

                @ApiModelProperty(example = "1")
                public Long id;
                @ApiModelProperty(example = "000000001")
                public Long accountNo;
                @ApiModelProperty(example = "1")
                public Long productId;
                @ApiModelProperty(example = "General Savings")
                public String productName;
            }

            final class GetStandingInstructionHistoryToClient {
                private GetStandingInstructionHistoryToClient() {
                }

                @ApiModelProperty(example = "1")
                public Long id;
                @ApiModelProperty(example = "Test client")
                public String displayName;
                @ApiModelProperty(example = "1")
                public Long officeId;
                @ApiModelProperty(example = "Head Office")
                public String officeName;
            }

            @ApiModelProperty(example = "1")
            public Long standingInstructionId;
            @ApiModelProperty(example = "ACC Transfer")
            public String name;
            public StandingInstructionApiResourceSwagger.GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetFromOfficeStandingInstructionSwagger fromOffice;
            public GetStandingInstructionHistoryPageItemsFromClient fromClient;
            public StandingInstructionApiResourceSwagger.GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetFromAccountTypeStandingInstructionSwagger fromAccountType;
            public GetStandingInstructionHistoryFromAccount fromAccount;
            public StandingInstructionApiResourceSwagger.GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetToAccountTypeStandingInstructionSwagger toAccountType;
            public GetStandingInstructionHistoryToAccount toAccount;
            public StandingInstructionApiResourceSwagger.GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetToOfficeStandingInstructionSwagger toOffice;
            public GetStandingInstructionHistoryToClient toClient;
            @ApiModelProperty(example = "10")
            public Float amount;
            @ApiModelProperty(example = "success")
            public String status;
            @ApiModelProperty(example = "[2014, 6, 30]")
            public LocalDate executionTime;
            @ApiModelProperty(example = " ")
            public String errorLog;
        }

        @ApiModelProperty(example = "2")
        public Integer totalFilteredRecords;
        public Set<GetStandingInstructionHistoryPageItemsResponse> pageItems;
    }
}
