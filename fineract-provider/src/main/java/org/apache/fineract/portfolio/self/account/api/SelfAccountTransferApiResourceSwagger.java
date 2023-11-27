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

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Created by Kang Breder on 29/07/19.
 */

final class SelfAccountTransferApiResourceSwagger {

    private SelfAccountTransferApiResourceSwagger() {}

    @Schema(description = "GetAccountTransferTemplateResponse")
    public static final class GetAccountTransferTemplateResponse {

        private GetAccountTransferTemplateResponse() {}

        static final class GetAccountOptions {

            private GetAccountOptions() {}

            @Schema(example = "2")
            public Long id;
            @Schema(example = "accountType.savings")
            public String code;
            @Schema(example = "Savings Account")
            public String description;
        }

        public Set<GetAccountOptions> accountTypeOptions;

        static final class GetFromAccountOptions {

            private GetFromAccountOptions() {}

            @Schema(example = "2")
            public Long accountId;
            @Schema(example = "00000001")
            public Integer accountNo;
            public GetAccountTransferTemplateResponse.GetAccountOptions accountType;
            @Schema(example = "1")
            public Long clientId;
            @Schema(example = "ABC")
            public String clientName;
            @Schema(example = "1")
            public Long officeId;
            @Schema(example = "HEAD OFFICE")
            public String officeName;

        }

        public Set<GetFromAccountOptions> fromAccountTypeOptions;

        static final class GetToAccountOptions {

            private GetToAccountOptions() {}

            @Schema(example = "2")
            public Long accountId;
            @Schema(example = "00000001")
            public Integer accountNo;
            public GetAccountTransferTemplateResponse.GetAccountOptions accountType;
            @Schema(example = "1")
            public Long clientId;
            @Schema(example = "ABC")
            public String clientName;
            @Schema(example = "1")
            public Long officeId;
            @Schema(example = "HEAD OFFICE")
            public String officeName;

        }

        public Set<GetFromAccountOptions> toAccountTypeOptions;
    }

    @Schema(description = "PostNewTransferRequest")
    public static final class PostNewTransferRequest {

        private PostNewTransferRequest() {}

        @Schema(example = "1")
        public Integer fromOfficeId;
        @Schema(example = "1")
        public Integer fromClientId;
        @Schema(example = "2")
        public Integer fromAccountType;
        @Schema(example = "1")
        public Integer fromAccountId;
        @Schema(example = "1")
        public Integer toOfficeId;
        @Schema(example = "1")
        public Integer toClientId;
        @Schema(example = "2")
        public Integer toAccountType;
        @Schema(example = "2")
        public Integer toAccountId;
        @Schema(example = "dd  MMMM YYYY")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "01  August 2011")
        public String transferDate;
        @Schema(example = "112.45")
        public Float transferAmount;
        @Schema(example = "A description of the transfer")
        public String transferDescription;

    }

    @Schema(description = "PostNewTransferResponse")
    public static final class PostNewTransferResponse {

        private PostNewTransferResponse() {}

        @Schema(example = "1")
        public Long savingsId;
        @Schema(example = "1")
        public Long resourceId;
    }
}
