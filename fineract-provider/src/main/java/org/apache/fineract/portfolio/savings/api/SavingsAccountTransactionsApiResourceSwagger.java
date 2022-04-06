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

final class SavingsAccountTransactionsApiResourceSwagger {

    private SavingsAccountTransactionsApiResourceSwagger() {}

    @Schema(description = "PostSavingsAccountTransactionsRequest")
    public static final class PostSavingsAccountTransactionsRequest {

        private PostSavingsAccountTransactionsRequest() {}

        @Schema(example = "27 March 2022")
        public String transactionDate;
        @Schema(example = "1000")
        public Integer transactionAmount;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "true")
        public String lienAllowed;
        @Schema(example = "String")
        public String reasonForBlock;
    }

    @Schema(description = "PostSavingsAccountTransactionsResponse")
    public static final class PostSavingsAccountTransactionsResponse {

        private PostSavingsAccountTransactionsResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer savingsId;
        @Schema(example = "1")
        public Integer resourceId;
    }
}
