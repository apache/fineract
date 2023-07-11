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
package org.apache.fineract.portfolio.paymenttype.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Created by Chirag Gupta on 01/01/18.
 */
public final class PaymentTypeApiResourceSwagger {

    private PaymentTypeApiResourceSwagger() {}

    @Schema(description = "GetPaymentTypesResponse")
    public static final class GetPaymentTypesResponse {

        private GetPaymentTypesResponse() {}

        @Schema(example = "24")
        public Long id;
        @Schema(example = "PTC")
        public String name;
        @Schema(example = "Cash")
        public String description;
        @Schema(example = "true")
        public Boolean isCashPayment;
        @Schema(example = "0")
        public Integer position;
        @Schema(example = "REPAYMENT_REFUND")
        public String codeName;
        @Schema(example = "false")
        public Boolean isSystemDefined;
    }

    @Schema(description = "GetPaymentTypesPaymentTypeIdResponse")
    public static final class GetPaymentTypesPaymentTypeIdResponse {

        private GetPaymentTypesPaymentTypeIdResponse() {}

        @Schema(example = "13")
        public Long id;
        @Schema(example = "cash")
        public String name;
        @Schema(example = "cash Payment")
        public String description;
        @Schema(example = "true")
        public Boolean isCashPayment;
        @Schema(example = "1")
        public Integer position;
        @Schema(example = "REPAYMENT_REFUND")
        public String codeName;
        @Schema(example = "false")
        public Boolean isSystemDefined;
    }

    @Schema(description = "PostPaymentTypesRequest")
    public static final class PostPaymentTypesRequest {

        private PostPaymentTypesRequest() {}

        @Schema(example = "cash")
        public String name;
        @Schema(example = "cash payment type")
        public String description;
        @Schema(example = "true")
        public Boolean isCashPayment;
        @Schema(example = "1")
        public Integer position;
        @Schema(example = "REPAYMENT_REFUND")
        public String codeName;
        @Schema(example = "false")
        public Boolean isSystemDefined;
    }

    @Schema(description = "PostPaymentTypesResponse")
    public static final class PostPaymentTypesResponse {

        private PostPaymentTypesResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "PutPaymentTypesPaymentTypeIdRequest")
    public static final class PutPaymentTypesPaymentTypeIdRequest {

        private PutPaymentTypesPaymentTypeIdRequest() {}

        @Schema(example = "mPay")
        public String name;
        @Schema(example = "not a cash payment type")
        public String description;
        @Schema(example = "false")
        public Boolean isCashPayment;
        @Schema(example = "3")
        public Integer position;
        @Schema(example = "REPAYMENT_REFUND")
        public String codeName;
        @Schema(example = "false")
        public Boolean isSystemDefined;
    }

    @Schema(description = "PutPaymentTypesPaymentTypeIdResponse")
    public static final class PutPaymentTypesPaymentTypeIdResponse {

        private PutPaymentTypesPaymentTypeIdResponse() {}

        @Schema(example = "13")
        public Long resourceId;
    }

    @Schema(description = "DeletePaymentTypesPaymentTypeIdResponse")
    public static final class DeletePaymentTypesPaymentTypeIdResponse {

        private DeletePaymentTypesPaymentTypeIdResponse() {}

        @Schema(example = "13")
        public Long resourceId;
    }
}
