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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.accounting.financialactivityaccount.data.FinancialActivityData;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;

import java.util.List;
import java.util.Map;

/**
 * Created by sanyam on 24/7/17.
 */
final class FinancialActivityAccountsApiResourceSwagger {
    private FinancialActivityAccountsApiResourceSwagger() {
    }

    @ApiModel(value = "GetFinancialActivityAccountsResponse")
    public static final class GetFinancialActivityAccountsResponse{
        private GetFinancialActivityAccountsResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        public FinancialActivityData financialActivityData;
        public GLAccountData glAccountData;

    }

    @ApiModel(value = "PostFinancialActivityAccountsRequest")
    public static final class PostFinancialActivityAccountsRequest{
        private PostFinancialActivityAccountsRequest() {

        }
        @ApiModelProperty(example = "200")
        public Long financialActivityId;
        @ApiModelProperty(example = "2")
        public Long glAccountId;
    }

    @ApiModel(value = "PostFinancialActivityAccountsResponse")
    public static final class PostFinancialActivityAccountsResponse{
        private PostFinancialActivityAccountsResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }

    @ApiModel(value = "PutFinancialActivityAccountsRequest")
    public static final class PutFinancialActivityAccountsRequest{
        private PutFinancialActivityAccountsRequest() {

        }
        @ApiModelProperty(example = "200")
        public Long financialActivityId;
        @ApiModelProperty(example = "3")
        public Long glAccountId;
    }

    @ApiModel(value = "PutFinancialActivityAccountsResponse")
    public static final class PutFinancialActivityAccountsResponse{
        private PutFinancialActivityAccountsResponse() {

        }
        public final class PutFinancialActivityAccountscommentsSwagger{
            private PutFinancialActivityAccountscommentsSwagger(){}
            @ApiModelProperty(example = "1")
            public Long glAccountId;
        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
        public PutFinancialActivityAccountscommentsSwagger comments;
    }

    @ApiModel(value = "DeleteFinancialActivityAccountsResponse")
    public static final class DeleteFinancialActivityAccountsResponse{
        private DeleteFinancialActivityAccountsResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }

}
