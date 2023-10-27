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
 * Created by Chirag Gupta on 01/12/18.
 */
final class ClientChargesApiResourceSwagger {

    private ClientChargesApiResourceSwagger() {}

    @Schema(description = "GetClientsClientIdChargesResponse")
    public static final class GetClientsClientIdChargesResponse {

        private GetClientsClientIdChargesResponse() {}

        static final class GetClientsChargesPageItems {

            private GetClientsChargesPageItems() {}

            static final class GetClientChargeTimeType {

                private GetClientChargeTimeType() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "chargeTimeType.specifiedDueDate")
                public String code;
                @Schema(example = "Specified due date")
                public String description;
            }

            static final class GetClientChargeCalculationType {

                private GetClientChargeCalculationType() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "chargeCalculationType.flat")
                public String code;
                @Schema(example = "Flat")
                public String description;
            }

            static final class GetClientChargeCurrency {

                private GetClientChargeCurrency() {}

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

            @Schema(example = "3")
            public Long id;
            @Schema(example = "1")
            public Long clientId;
            @Schema(example = "5")
            public Long chargeId;
            @Schema(example = "Client Fee 1")
            public String name;
            public GetClientChargeTimeType chargeTimeType;
            @Schema(example = "[2015, 8, 17]")
            public LocalDate dueDate;
            public GetClientChargeCalculationType chargeCalculationType;
            public GetClientChargeCurrency currency;
            @Schema(example = "100.000000")
            public BigDecimal amount;
            @Schema(example = "0")
            public BigDecimal amountPaid;
            @Schema(example = "100.000000")
            public BigDecimal amountWaived;
            @Schema(example = "0")
            public BigDecimal amountWrittenOff;
            @Schema(example = "0.000000")
            public BigDecimal amountOutstanding;
            @Schema(example = "true")
            public Boolean penalty;
            @Schema(example = "true")
            public Boolean isActive;
            @Schema(example = "false")
            public Boolean isPaid;
            @Schema(example = "true")
            public Boolean isWaived;
        }

        @Schema(example = "4")
        public Integer totalFilteredRecords;
        public Set<GetClientsChargesPageItems> pageItems;
    }

    @Schema(description = "PostClientsClientIdChargesRequest")
    public static final class PostClientsClientIdChargesRequest {

        private PostClientsClientIdChargesRequest() {}

        @Schema(example = "100")
        public Integer amount;
        @Schema(example = "226")
        public Long chargeId;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "01 September 2015")
        public String dueDate;
        @Schema(example = "en")
        public String locale;
    }

    @Schema(description = "PostClientsClientIdChargesResponse")
    public static final class PostClientsClientIdChargesResponse {

        private PostClientsClientIdChargesResponse() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "189")
        public Long chargeId;
        @Schema(example = "164")
        public Long resourceId;
    }

    @Schema(description = "PostClientsClientIdChargesChargeIdRequest")
    public static final class PostClientsClientIdChargesChargeIdRequest {

        private PostClientsClientIdChargesChargeIdRequest() {}

        @Schema(example = "200")
        public Integer amount;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "01 September 2015")
        public String transactionDate;
        @Schema(example = "3e7791ce-aa10-11ec-b909-0242ac120002")
        public String externalId;
    }

    @Schema(description = "PostClientsClientIdChargesChargeIdResponse")
    public static final class PostClientsClientIdChargesChargeIdResponse {

        private PostClientsClientIdChargesChargeIdResponse() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "189")
        public Long clientId;
        @Schema(example = "157")
        public Long resourceId;
        @Schema(example = "221")
        public Long transactionId;
    }

    @Schema(description = "DeleteClientsClientIdChargesChargeIdResponse")
    public static final class DeleteClientsClientIdChargesChargeIdResponse {

        private DeleteClientsClientIdChargesChargeIdResponse() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "189")
        public Long clientId;
        @Schema(example = "164")
        public Long resourceId;
    }
}
