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
package org.apache.fineract.portfolio.self.account.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;

/**
 * Created by Kang Breder on 29/07/19.
 */

final class SelfAccountTransferApiResourceSwagger {
    private SelfAccountTransferApiResourceSwagger() {
    }


    @ApiModel(value = "GetAccountTransferTemplateResponse")
    public final static class GetAccountTransferTemplateResponse {
        private GetAccountTransferTemplateResponse() {
        }

        final class GetAccountOptions {
            private GetAccountOptions() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "accountType.savings")
            public String code;
            @ApiModelProperty(example = "Savings Account")
            public String value;
        }

        public Set<GetAccountOptions> accountTypeOptions;

        final class GetFromAccountOptions {
            private GetFromAccountOptions() {
            }

            @ApiModelProperty(example = "2")
            public Integer accountId;
            @ApiModelProperty(example = "00000001")
            public Integer accountNo;
            public GetAccountTransferTemplateResponse.GetAccountOptions accountType;
            @ApiModelProperty(example = "1")
            public Integer clientId;
            @ApiModelProperty(example = "ABC")
            public String clientName;
            @ApiModelProperty(example = "1")
            public Integer officeId;
            @ApiModelProperty(example = "HEAD OFFICE")
            public String officeName;

        }

        public Set<GetFromAccountOptions> fromAccountTypeOptions;

        final class GetToAccountOptions {
            private GetToAccountOptions() {
            }

            @ApiModelProperty(example = "2")
            public Integer accountId;
            @ApiModelProperty(example = "00000001")
            public Integer accountNo;
            public GetAccountTransferTemplateResponse.GetAccountOptions accountType;
            @ApiModelProperty(example = "1")
            public Integer clientId;
            @ApiModelProperty(example = "ABC")
            public String clientName;
            @ApiModelProperty(example = "1")
            public Integer officeId;
            @ApiModelProperty(example = "HEAD OFFICE")
            public String officeName;

        }

        public Set<GetFromAccountOptions> toAccountTypeOptions;
    }

    @ApiModel(value = "PostNewTransferRequest")
    public final static class PostNewTransferRequest {
        private PostNewTransferRequest() {
        }

        @ApiModelProperty(example = "1")
        public Integer fromOfficeId;
        @ApiModelProperty(example = "1")
        public Integer fromClientId;
        @ApiModelProperty(example = "2")
        public Integer fromAccountType;
        @ApiModelProperty(example = "1")
        public Integer fromAccountId;
        @ApiModelProperty(example = "1")
        public Integer toOfficeId;
        @ApiModelProperty(example = "1")
        public Integer toClientId;
        @ApiModelProperty(example = "2")
        public Integer toAccountType;
        @ApiModelProperty(example = "2")
        public Integer toAccountId;
        @ApiModelProperty(example = "dd  MMMM YYYY")
        public String dateFormat;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "01  August 2011")
        public String transferDate;
        @ApiModelProperty(example = "112.45")
        public Float transferAmount;
        @ApiModelProperty(example = "A description of the transfer")
        public String transferDescription;

    }

    @ApiModel(value = "PostNewTransferResponse")
    public final static class PostNewTransferResponse {
        private PostNewTransferResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer savingsId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }
}