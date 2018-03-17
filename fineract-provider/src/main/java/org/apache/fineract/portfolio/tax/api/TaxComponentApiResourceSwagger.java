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
package org.apache.fineract.portfolio.tax.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/20/17.
 */
final class TaxComponentApiResourceSwagger {
    private TaxComponentApiResourceSwagger() {
    }

    @ApiModel(value = "GetTaxesComponentsResponse")
    public final static class GetTaxesComponentsResponse {
        private GetTaxesComponentsResponse() {
        }

        final class GetTaxesComponentsCreditAccountType {
            private GetTaxesComponentsCreditAccountType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "accountType.liability")
            public String code;
            @ApiModelProperty(example = "LIABILITY")
            public String value;
        }

        final class GetTaxesComponentsCreditAccount {
            private GetTaxesComponentsCreditAccount() {
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "ACCOUNT_NAME_7BR9C")
            public String name;
            @ApiModelProperty(example = "LIABILITY_PA1460364665046")
            public String glCode;
        }

        final class GetTaxesComponentsHistories {
            private GetTaxesComponentsHistories() {
            }
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "tax component 1")
        public String name;
        @ApiModelProperty(example = "10.000000")
        public Float percentage;
        public GetTaxesComponentsCreditAccountType creditAccountType;
        public GetTaxesComponentsCreditAccount creditAccount;
        @ApiModelProperty(example = "[2016, 4, 11]")
        public LocalDate startDate;
        public Set<GetTaxesComponentsHistories> taxComponentsHistories;
    }

    @ApiModel(value = "PostTaxesComponentsRequest")
    public final static class PostTaxesComponentsRequest {
        private PostTaxesComponentsRequest() {
        }

        @ApiModelProperty(example = "tax component 1")
        public String name;
        @ApiModelProperty(example = "10")
        public Float percentage;
        @ApiModelProperty(example = "2")
        public Integer creditAccountType;
        @ApiModelProperty(example = "4")
        public Integer creditAcountId;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "11 April 2016")
        public String startDate;
    }

    @ApiModel(value = "PostTaxesComponentsResponse")
    public final static class PostTaxesComponentsResponse {
        private PostTaxesComponentsResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "PutTaxesComponentsTaxComponentIdRequest")
    public final static class PutTaxesComponentsTaxComponentIdRequest {
        private PutTaxesComponentsTaxComponentIdRequest() {
        }

        @ApiModelProperty(example = "tax component 2")
        public String name;
        @ApiModelProperty(example = "15")
        public Float percentage;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "15 April 2016")
        public String startDate;
    }

    @ApiModel(value = "PutTaxesComponentsTaxComponentIdResponse")
    public final static class PutTaxesComponentsTaxComponentIdResponse {
        private PutTaxesComponentsTaxComponentIdResponse() {
        }

        final class PutTaxesComponentsChanges {
            private PutTaxesComponentsChanges() {
            }

            @ApiModelProperty(example = "15")
            public Float percentage;
            @ApiModelProperty(example = "tax component 2")
            public String name;
            @ApiModelProperty(example = "[2016, 4, 15]")
            public LocalDate startDate;
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PutTaxesComponentsChanges changes;
    }
}
