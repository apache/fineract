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
package org.apache.fineract.portfolio.paymenttype.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Chirag Gupta on 01/01/18.
 */
final class PaymentTypeApiResourceSwagger {
    private PaymentTypeApiResourceSwagger() {
    }

    @ApiModel(value = "GetPaymentTypesResponse")
    public final static class GetPaymentTypesResponse {
        private GetPaymentTypesResponse() {
        }

        @ApiModelProperty(example = "24")
        public Integer id;
        @ApiModelProperty(example = "PTC")
        public String name;
        @ApiModelProperty(example = "Cash")
        public String description;
        @ApiModelProperty(example = "true")
        public Boolean isCashPayment;
        @ApiModelProperty(example = "0")
        public Integer position;
    }

    @ApiModel(value = "GetPaymentTypesPaymentTypeIdResponse")
    public final static class GetPaymentTypesPaymentTypeIdResponse {
        private GetPaymentTypesPaymentTypeIdResponse() {
        }

        @ApiModelProperty(example = "13")
        public Integer id;
        @ApiModelProperty(example = "cash")
        public String name;
        @ApiModelProperty(example = "cash Payment")
        public String description;
        @ApiModelProperty(example = "true")
        public Boolean isCashPayment;
        @ApiModelProperty(example = "1")
        public Integer position;
    }

    @ApiModel(value = "PostPaymentTypesRequest")
    public final static class PostPaymentTypesRequest {
        private PostPaymentTypesRequest() {
        }

        @ApiModelProperty(example = "cash")
        public String name;
        @ApiModelProperty(example = "cash payment type")
        public String description;
        @ApiModelProperty(example = "true")
        public Boolean isCashPayment;
        @ApiModelProperty(example = "1")
        public Integer position;
    }

    @ApiModel(value = "PostPaymentTypesResponse")
    public final static class PostPaymentTypesResponse {
        private PostPaymentTypesResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "PutPaymentTypesPaymentTypeIdRequest")
    public final static class PutPaymentTypesPaymentTypeIdRequest {
        private PutPaymentTypesPaymentTypeIdRequest() {
        }

        @ApiModelProperty(example = "mPay")
        public String name;
        @ApiModelProperty(example = "not a cash payment type")
        public String description;
        @ApiModelProperty(example = "false")
        public Boolean isCashPayment;
        @ApiModelProperty(example = "3")
        public Integer position;
    }

    @ApiModel(value = "PutPaymentTypesPaymentTypeIdResponse")
    public final static class PutPaymentTypesPaymentTypeIdResponse {
        private PutPaymentTypesPaymentTypeIdResponse() {
        }

        @ApiModelProperty(example = "13")
        public Integer resourceId;
    }

    @ApiModel(value = "DeletePaymentTypesPaymentTypeIdResponse")
    public final static class DeletePaymentTypesPaymentTypeIdResponse {
        private DeletePaymentTypesPaymentTypeIdResponse() {
        }

        @ApiModelProperty(example = "13")
        public Integer resourceId;
    }
}
