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
package org.apache.fineract.portfolio.client.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 01/13/18.
 */
final class ClientTransactionsApiResourceSwagger {
    private ClientTransactionsApiResourceSwagger() {
    }

    @ApiModel(value = "GetClientsClientIdTransactionsResponse")
    public final static class GetClientsClientIdTransactionsResponse {
        private GetClientsClientIdTransactionsResponse() {
        }

        final class GetClientsPageItems {
            private GetClientsPageItems() {
            }

            final class GetClientsClientIdTransactionsType {
                private GetClientsClientIdTransactionsType() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "clientTransactionType.payCharge")
                public String code;
                @ApiModelProperty(example = "PAY_CHARGE")
                public String value;
            }

            final class GetClientTransactionsCurrency {
                private GetClientTransactionsCurrency() {
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

            @ApiModelProperty(example = "226")
            public Integer id;
            @ApiModelProperty(example = "1")
            public Integer officeId;
            @ApiModelProperty(example = "Head Office")
            public String officeName;
            public GetClientsClientIdTransactionsType type;
            @ApiModelProperty(example = "[2015, 9, 2]")
            public LocalDate date;
            public GetClientTransactionsCurrency currency;
            @ApiModelProperty(example = "22")
            public Double amount;
            @ApiModelProperty(example = "[2015, 9, 2]")
            public LocalDate submittedOnDate;
            @ApiModelProperty(example = "false")
            public Boolean reversed;
        }

        @ApiModelProperty(example = "20")
        public Integer totalFilteredRecords;
        public Set<GetClientsPageItems> pageItems;
    }

    @ApiModel(value = "GetClientsClientIdTransactionsTransactionIdResponse")
    public final static class GetClientsClientIdTransactionsTransactionIdResponse {
        private GetClientsClientIdTransactionsTransactionIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
        public GetClientsClientIdTransactionsResponse.GetClientsPageItems.GetClientsClientIdTransactionsType type;
        @ApiModelProperty(example = "[2015, 8, 17]")
        public LocalDate date;
        public GetClientsClientIdTransactionsResponse.GetClientsPageItems.GetClientTransactionsCurrency currency;
        @ApiModelProperty(example = "60.000000")
        public BigDecimal amount;
        @ApiModelProperty(example = "[2015, 8, 17]")
        public LocalDate submittedOnDate;
        @ApiModelProperty(example = "true")
        public Boolean reversed;
    }


    @ApiModel(value = "PostClientsClientIdTransactionsTransactionIdResponse")
    public final static class PostClientsClientIdTransactionsTransactionIdResponse {
        private PostClientsClientIdTransactionsTransactionIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "189")
        public Integer clientId;
        @ApiModelProperty(example = "222")
        public Integer resourceId;
    }
}
