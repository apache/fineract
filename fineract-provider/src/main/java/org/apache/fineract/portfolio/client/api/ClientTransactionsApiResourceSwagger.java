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
package org.apache.fineract.portfolio.client.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 01/13/18.
 */
final class ClientTransactionsApiResourceSwagger {

    private ClientTransactionsApiResourceSwagger() {}

    @Schema(description = "GetClientsClientIdTransactionsResponse")
    public static final class GetClientsClientIdTransactionsResponse {

        private GetClientsClientIdTransactionsResponse() {}

        static final class GetClientsPageItems {

            private GetClientsPageItems() {}

            static final class GetClientsClientIdTransactionsType {

                private GetClientsClientIdTransactionsType() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "clientTransactionType.payCharge")
                public String code;
                @Schema(example = "PAY_CHARGE")
                public String description;
            }

            static final class GetClientTransactionsCurrency {

                private GetClientTransactionsCurrency() {}

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

            @Schema(example = "226")
            public Integer id;
            @Schema(example = "1")
            public Integer officeId;
            @Schema(example = "Head Office")
            public String officeName;
            public GetClientsClientIdTransactionsType type;
            @Schema(example = "[2015, 9, 2]")
            public LocalDate date;
            public GetClientTransactionsCurrency currency;
            @Schema(example = "22")
            public Double amount;
            @Schema(example = "[2015, 9, 2]")
            public LocalDate submittedOnDate;
            @Schema(example = "false")
            public Boolean reversed;
        }

        @Schema(example = "20")
        public Integer totalFilteredRecords;
        public Set<GetClientsPageItems> pageItems;
    }

    @Schema(description = "GetClientsClientIdTransactionsTransactionIdResponse")
    public static final class GetClientsClientIdTransactionsTransactionIdResponse {

        private GetClientsClientIdTransactionsTransactionIdResponse() {}

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "Head Office")
        public String officeName;
        public GetClientsClientIdTransactionsResponse.GetClientsPageItems.GetClientsClientIdTransactionsType type;
        @Schema(example = "[2015, 8, 17]")
        public LocalDate date;
        public GetClientsClientIdTransactionsResponse.GetClientsPageItems.GetClientTransactionsCurrency currency;
        @Schema(example = "60.000000")
        public BigDecimal amount;
        @Schema(example = "[2015, 8, 17]")
        public LocalDate submittedOnDate;
        @Schema(example = "true")
        public Boolean reversed;
    }

    @Schema(description = "PostClientsClientIdTransactionsTransactionIdResponse")
    public static final class PostClientsClientIdTransactionsTransactionIdResponse {

        private PostClientsClientIdTransactionsTransactionIdResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "189")
        public Integer clientId;
        @Schema(example = "222")
        public Integer resourceId;
    }
}
