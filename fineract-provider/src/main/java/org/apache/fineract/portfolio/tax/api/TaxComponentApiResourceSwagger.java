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
package org.apache.fineract.portfolio.tax.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/20/17.
 */
final class TaxComponentApiResourceSwagger {

    private TaxComponentApiResourceSwagger() {}

    @Schema(description = "GetTaxesComponentsResponse")
    public static final class GetTaxesComponentsResponse {

        private GetTaxesComponentsResponse() {}

        static final class GetTaxesComponentsCreditAccountType {

            private GetTaxesComponentsCreditAccountType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "accountType.liability")
            public String code;
            @Schema(example = "LIABILITY")
            public String description;
        }

        static final class GetTaxesComponentsCreditAccount {

            private GetTaxesComponentsCreditAccount() {}

            @Schema(example = "4")
            public Integer id;
            @Schema(example = "ACCOUNT_NAME_7BR9C")
            public String name;
            @Schema(example = "LIABILITY_PA1460364665046")
            public String glCode;
        }

        static final class GetTaxesComponentsHistories {

            private GetTaxesComponentsHistories() {}
        }

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "tax component 1")
        public String name;
        @Schema(example = "10.000000")
        public Float percentage;
        public GetTaxesComponentsCreditAccountType creditAccountType;
        public GetTaxesComponentsCreditAccount creditAccount;
        @Schema(example = "[2016, 4, 11]")
        public LocalDate startDate;
        public Set<GetTaxesComponentsHistories> taxComponentsHistories;
    }

    @Schema(description = "PostTaxesComponentsRequest")
    public static final class PostTaxesComponentsRequest {

        private PostTaxesComponentsRequest() {}

        @Schema(example = "tax component 1")
        public String name;
        @Schema(example = "10")
        public Float percentage;
        @Schema(example = "2")
        public Integer creditAccountType;
        @Schema(example = "4")
        public Integer creditAcountId;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "11 April 2016")
        public String startDate;
    }

    @Schema(description = "PostTaxesComponentsResponse")
    public static final class PostTaxesComponentsResponse {

        private PostTaxesComponentsResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PutTaxesComponentsTaxComponentIdRequest")
    public static final class PutTaxesComponentsTaxComponentIdRequest {

        private PutTaxesComponentsTaxComponentIdRequest() {}

        @Schema(example = "tax component 2")
        public String name;
        @Schema(example = "15")
        public Float percentage;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "15 April 2016")
        public String startDate;
    }

    @Schema(description = "PutTaxesComponentsTaxComponentIdResponse")
    public static final class PutTaxesComponentsTaxComponentIdResponse {

        private PutTaxesComponentsTaxComponentIdResponse() {}

        static final class PutTaxesComponentsChanges {

            private PutTaxesComponentsChanges() {}

            @Schema(example = "15")
            public Float percentage;
            @Schema(example = "tax component 2")
            public String name;
            @Schema(example = "[2016, 4, 15]")
            public LocalDate startDate;
        }

        @Schema(example = "1")
        public Integer resourceId;
        public PutTaxesComponentsChanges changes;
    }
}
