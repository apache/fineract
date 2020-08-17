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
package org.apache.fineract.accounting.financialactivityaccount.api;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.fineract.accounting.financialactivityaccount.data.FinancialActivityData;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;

/**
 * Created by sanyam on 24/7/17.
 */
final class FinancialActivityAccountsApiResourceSwagger {

    private FinancialActivityAccountsApiResourceSwagger() {}

    @Schema(description = "GetFinancialActivityAccountsResponse")
    public static final class GetFinancialActivityAccountsResponse {

        private GetFinancialActivityAccountsResponse() {

        }

        @Schema(example = "1")
        public Long id;
        public FinancialActivityData financialActivityData;
        public GLAccountData glAccountData;

    }

    @Schema(description = "PostFinancialActivityAccountsRequest")
    public static final class PostFinancialActivityAccountsRequest {

        private PostFinancialActivityAccountsRequest() {

        }

        @Schema(example = "200")
        public Long financialActivityId;
        @Schema(example = "2")
        public Long glAccountId;
    }

    @Schema(description = "PostFinancialActivityAccountsResponse")
    public static final class PostFinancialActivityAccountsResponse {

        private PostFinancialActivityAccountsResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "PutFinancialActivityAccountsRequest")
    public static final class PutFinancialActivityAccountsRequest {

        private PutFinancialActivityAccountsRequest() {

        }

        @Schema(example = "200")
        public Long financialActivityId;
        @Schema(example = "3")
        public Long glAccountId;
    }

    @Schema(description = "PutFinancialActivityAccountsResponse")
    public static final class PutFinancialActivityAccountsResponse {

        private PutFinancialActivityAccountsResponse() {

        }

        public static final class PutFinancialActivityAccountscommentsSwagger {

            private PutFinancialActivityAccountscommentsSwagger() {}

            @Schema(example = "1")
            public Long glAccountId;
        }

        @Schema(example = "1")
        public Long resourceId;
        public PutFinancialActivityAccountscommentsSwagger comments;
    }

    @Schema(description = "DeleteFinancialActivityAccountsResponse")
    public static final class DeleteFinancialActivityAccountsResponse {

        private DeleteFinancialActivityAccountsResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;
    }

}
