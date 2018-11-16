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
 * Created by Chirag Gupta on 01/12/18.
 */
final class ClientChargesApiResourceSwagger {
    private ClientChargesApiResourceSwagger() {
    }

    @ApiModel(value = "GetClientsClientIdChargesResponse")
    public final static class GetClientsClientIdChargesResponse {
        private GetClientsClientIdChargesResponse() {
        }

        final class GetClientsChargesPageItems {
            private GetClientsChargesPageItems() {
            }

            final class GetClientChargeTimeType {
                private GetClientChargeTimeType() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "chargeTimeType.specifiedDueDate")
                public String code;
                @ApiModelProperty(example = "Specified due date")
                public String value;
            }

            final class GetClientChargeCalculationType {
                private GetClientChargeCalculationType() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "chargeCalculationType.flat")
                public String code;
                @ApiModelProperty(example = "Flat")
                public String value;
            }

            final class GetClientChargeCurrency {
                private GetClientChargeCurrency() {
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

            @ApiModelProperty(example = "3")
            public Integer id;
            @ApiModelProperty(example = "1")
            public Integer clientId;
            @ApiModelProperty(example = "5")
            public Integer chargeId;
            @ApiModelProperty(example = "Client Fee 1")
            public String name;
            public GetClientChargeTimeType chargeTimeType;
            @ApiModelProperty(example = "[2015, 8, 17]")
            public LocalDate dueDate;
            public GetClientChargeCalculationType chargeCalculationType;
            public GetClientChargeCurrency currency;
            @ApiModelProperty(example = "100.000000")
            public BigDecimal amount;
            @ApiModelProperty(example = "0")
            public BigDecimal amountPaid;
            @ApiModelProperty(example = "100.000000")
            public BigDecimal amountWaived;
            @ApiModelProperty(example = "0")
            public BigDecimal amountWrittenOff;
            @ApiModelProperty(example = "0.000000")
            public BigDecimal amountOutstanding;
            @ApiModelProperty(example = "true")
            public Boolean penalty;
            @ApiModelProperty(example = "true")
            public Boolean isActive;
            @ApiModelProperty(example = "false")
            public Boolean isPaid;
            @ApiModelProperty(example = "true")
            public Boolean isWaived;
        }

        @ApiModelProperty(example = "4")
        public Integer totalFilteredRecords;
        public Set<GetClientsChargesPageItems> pageItems;
    }

    @ApiModel(value = "PostClientsClientIdChargesRequest")
    public final static class PostClientsClientIdChargesRequest {
        private PostClientsClientIdChargesRequest() {
        }

        @ApiModelProperty(example = "100")
        public Integer amount;
        @ApiModelProperty(example = "226")
        public Integer chargeId;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "01 September 2015")
        public String dueDate;
        @ApiModelProperty(example = "en")
        public String locale;
    }

    @ApiModel(value = "PostClientsClientIdChargesResponse")
    public final static class PostClientsClientIdChargesResponse {
        private PostClientsClientIdChargesResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "189")
        public Integer chargeId;
        @ApiModelProperty(example = "164")
        public Integer resourceId;
    }

    @ApiModel(value = "PostClientsClientIdChargesChargeIdRequest")
    public final static class PostClientsClientIdChargesChargeIdRequest {
        private PostClientsClientIdChargesChargeIdRequest() {
        }

        @ApiModelProperty(example = "200")
        public Integer amount;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "01 September 2015")
        public String transactionDate;
    }

    @ApiModel(value = "PostClientsClientIdChargesChargeIdResponse")
    public final static class PostClientsClientIdChargesChargeIdResponse {
        private PostClientsClientIdChargesChargeIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "189")
        public Integer clientId;
        @ApiModelProperty(example = "157")
        public Integer resourceId;
        @ApiModelProperty(example = "221")
        public Integer transactionId;
    }

    @ApiModel(value = "DeleteClientsClientIdChargesChargeIdResponse")
    public final static class DeleteClientsClientIdChargesChargeIdResponse {
        private DeleteClientsClientIdChargesChargeIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "189")
        public Integer clientId;
        @ApiModelProperty(example = "164")
        public Integer resourceId;
    }
}
