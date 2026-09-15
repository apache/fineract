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
package org.apache.fineract.portfolio.accounts.api.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

final class AccountsV2ApiResourceSwagger {

    private AccountsV2ApiResourceSwagger() {}

    @Schema(description = "PostAccountsTypeAccountIdRedeemRequest")
    public static final class PostAccountsTypeAccountIdRedeemRequest {

        private PostAccountsTypeAccountIdRedeemRequest() {}

        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "05 May 2026")
        public String requestedDate;
        @Schema(example = "100", description = "Number of shares to redeem")
        public Long requestedShares;
    }

    @Schema(description = "PostAccountsTypeAccountIdAdditionalSharesRequest")
    public static final class PostAccountsTypeAccountIdAdditionalSharesRequest {

        private PostAccountsTypeAccountIdAdditionalSharesRequest() {}

        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "05 May 2026")
        public String requestedDate;
        @Schema(example = "100", description = "Number of shares to be purchased")
        public Long requestedShares;
    }

    @Schema(description = "PostAccountsTypeAccountIdApproveAdditionalSharesRequest")
    public static final class PostAccountsTypeAccountIdApproveAdditionalSharesRequest {

        private PostAccountsTypeAccountIdApproveAdditionalSharesRequest() {}

        @Schema(description = "Share purchase transaction IDs")
        public Set<PostAccountsRequestedShare> requestedShares;
    }

    @Schema(description = "PostAccountsTypeAccountIdRejectAdditionalSharesRequest")
    public static final class PostAccountsTypeAccountIdRejectAdditionalSharesRequest {

        private PostAccountsTypeAccountIdRejectAdditionalSharesRequest() {}

        @Schema(description = "Share purchase transaction IDs")
        public Set<PostAccountsRequestedShare> requestedShares;
    }

    @Schema(description = "Share purchase transaction")
    public static final class PostAccountsRequestedShare {

        private PostAccountsRequestedShare() {}

        @Schema(example = "35", description = "Share purchase transaction ID")
        public Long id;
    }

    @Schema(description = "PostAccountsTypeAccountIdResponse")
    public static final class PostAccountsTypeAccountIdResponse {

        private PostAccountsTypeAccountIdResponse() {}

        @Schema(example = "5")
        public Long resourceId;
    }
}
