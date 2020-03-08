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
package org.apache.fineract.portfolio.self.pockets.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;

/**
 * Created by Kang Breder on 07/08/19.
 */

final class PocketApiResourceSwagger {
    private PocketApiResourceSwagger() {
    }

    @ApiModel(value = "PostLinkDelinkAccountsToFromPocketRequest")
    public final static class PostLinkDelinkAccountsToFromPocketRequest {
        private PostLinkDelinkAccountsToFromPocketRequest() {
    }
        final class GetPocketAccountDetail {
            private GetPocketAccountDetail() {
            }

            @ApiModelProperty(example = "11")
            public Integer accountId;
            @ApiModelProperty(example = "LOAN")
            public String accountType;
        }
        public Set<GetPocketAccountDetail> accountDetail;
    }

    @ApiModel(value = "PostLinkDelinkAccountsToFromPocketResponse")
    public final static class PostLinkDelinkAccountsToFromPocketResponse {
        private PostLinkDelinkAccountsToFromPocketResponse() {
    }
        @ApiModelProperty(example = "6")
        public Integer resourceId;
    }

    @ApiModel(value = "GetAccountsLinkedToPocketResponse")
    public final static class GetAccountsLinkedToPocketResponse {
        private GetAccountsLinkedToPocketResponse() {
        }

        final class GetPocketLoanAccounts {
            private GetPocketLoanAccounts() {
            }

            @ApiModelProperty(example = "6")
            public Integer pocketId;
            @ApiModelProperty(example = "11")
            public Integer accountId;
            @ApiModelProperty(example = "2")
            public Integer accountType;
            @ApiModelProperty(example = "000000011")
            public Integer accountNumber;
            @ApiModelProperty(example = "10")
            public Integer id;
        }

        final class GetPocketSavingAccounts {
            private GetPocketSavingAccounts() {
            }

            @ApiModelProperty(example = "6")
            public Integer pocketId;
            @ApiModelProperty(example = "2")
            public Integer accountId;
            @ApiModelProperty(example = "3")
            public Integer accountType;
            @ApiModelProperty(example = "000000002")
            public Integer accountNumber;
            @ApiModelProperty(example = "11")
            public Integer id;
        }

        final class GetPocketShareAccounts {
            private GetPocketShareAccounts() {
            }

        }

        public Set<GetPocketLoanAccounts> loanAccounts;
        public Set<GetPocketSavingAccounts> savingAccounts;
        public Set<GetPocketShareAccounts> shareAccounts;
    }
}

