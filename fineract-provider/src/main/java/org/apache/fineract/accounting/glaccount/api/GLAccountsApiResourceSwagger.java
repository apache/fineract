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
package org.apache.fineract.accounting.glaccount.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

import java.util.Collection;
import java.util.List;

/**
 * Created by sanyam on 24/7/17.
 */
final class GLAccountsApiResourceSwagger {
    private GLAccountsApiResourceSwagger() {

    }

    @ApiModel(value = "GetGLAccountsResponse")
    public static final class GetGLAccountsResponse{
        private GetGLAccountsResponse(){

        }

        @ApiModelProperty(example = "16")
        public Long id;

        @ApiModelProperty(example = "Cash")
        public String name;

        @ApiModelProperty(example = "1")
        public Long parentId;

        @ApiModelProperty(example = "100001")
        public String glCode;

        @ApiModelProperty(example = "false")
        public Boolean disabled;

        @ApiModelProperty(example = "true")
        public Boolean manualEntriesAllowed;

        public EnumOptionData type;
        public EnumOptionData usage;

        @ApiModelProperty(example = "Desc")
        public String description;

        @ApiModelProperty(example = "....Cash")
        public String nameDecorated;

        public CodeValueData tagId;

        @ApiModelProperty(example = "118437")
        public Long organizationRunningBalance;
    }

    @ApiModel(value = "GetGLAccountsTemplateResponse")
    public static final class GetGLAccountsTemplateResponse{
        private GetGLAccountsTemplateResponse(){

        }

        @ApiModelProperty(example = "false")
        public Boolean disabled;

        @ApiModelProperty(example = "true")
        public Boolean manualEntriesAllowed;

        public EnumOptionData type;
        public EnumOptionData usage;
        public List<EnumOptionData> accountTypeOptions;
        public List<EnumOptionData> usageOptions;
        public List<GLAccountData> assetHeaderAccountOptions;
        public List<GLAccountData> liabilityHeaderAccountOptions;
        public List<GLAccountData> equityHeaderAccountOptions;
        public List<GLAccountData> expenseHeaderAccountOptions;
        public Collection<CodeValueData> allowedAssetsTagOptions;
        public Collection<CodeValueData> allowedLiabilitiesTagOptions;
        public Collection<CodeValueData> allowedEquityTagOptions;
        public Collection<CodeValueData> allowedIncomeTagOptions;
        public Collection<CodeValueData> allowedExpensesTagOptions;

    }

    @ApiModel(value = "PostGLAccountsRequest")
    public static final class PostGLAccountsRequest{
        private PostGLAccountsRequest(){

        }

        @ApiModelProperty(example = "Cash at Bangalore")
        public String name;

        @ApiModelProperty(example = "100001")
        public String glCode;

        @ApiModelProperty(example = "true")
        public Boolean manualEntriesAllowed;

        @ApiModelProperty(example = "1")
        public String type;

        @ApiModelProperty(example = "10")
        public String tagId;

        @ApiModelProperty(example = "1")
        public Long parentId;

        @ApiModelProperty(example = "1")
        public EnumOptionData usage;

        @ApiModelProperty(example = "Desc")
        public String description;

    }

    @ApiModel(value = "PostGLAccountsResponse")
    public static final class PostGLAccountsResponse{
        private PostGLAccountsResponse() {

        }

        @ApiModelProperty(example = "22")
        public int resourceId;
    }

    @ApiModel(value = "PutGLAccountsRequest")
    public static final class PutGLAccountsRequest{
        private PutGLAccountsRequest() {

        }

        @ApiModelProperty(example = "Cash at Bangalore")
        public String name;
    }

    @ApiModel(value = "PutGLAccountsResponse")
    public static final class PutGLAccountsResponse{
        private PutGLAccountsResponse() {

        }
        @ApiModel
        public static final class PutGLAccountsResponsechangesSwagger{
            private PutGLAccountsResponsechangesSwagger(){}
            @ApiModelProperty(example = "Cash at Bangalore")
            public String name;
        }
        @ApiModelProperty(example = "1")
        public int resourceId;
        public PutGLAccountsResponsechangesSwagger changes;
    }

    @ApiModel(value = "DeleteGLAccountsRequest")
    public static final class DeleteGLAccountsRequest{
        private DeleteGLAccountsRequest() {

        }

        private static final class DeleteGLAccountsRequestchangesSwagger{
            private DeleteGLAccountsRequestchangesSwagger(){}

        }

        @ApiModelProperty(example = "1")
        public int resourceId;

        private DeleteGLAccountsRequestchangesSwagger changes;
    }

}
