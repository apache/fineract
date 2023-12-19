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

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Created by sanyam on 24/7/17.
 */
final class GLAccountsApiResourceSwagger {

    private GLAccountsApiResourceSwagger() {

    }

    @Schema(description = "GetGLAccountsResponse")
    public static final class GetGLAccountsResponse {

        private GetGLAccountsResponse() {

        }

        @Schema(example = "16")
        public Long id;

        @Schema(example = "Cash")
        public String name;

        @Schema(example = "1")
        public Long parentId;

        @Schema(example = "100001")
        public String glCode;

        @Schema(example = "false")
        public Boolean disabled;

        @Schema(example = "true")
        public Boolean manualEntriesAllowed;

        public EnumOptionData type;
        public EnumOptionData usage;

        @Schema(example = "Desc")
        public String description;

        @Schema(example = "....Cash")
        public String nameDecorated;

        public CodeValueData tagId;

        @Schema(example = "118437")
        public Long organizationRunningBalance;
    }

    @Schema(description = "GetGLAccountsTemplateResponse")
    public static final class GetGLAccountsTemplateResponse {

        private GetGLAccountsTemplateResponse() {

        }

        @Schema(example = "false")
        public Boolean disabled;

        @Schema(example = "true")
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

    @Schema(description = "PostGLAccountsRequest")
    public static final class PostGLAccountsRequest {

        private PostGLAccountsRequest() {

        }

        @Schema(example = "Cash at Bangalore")
        public String name;

        @Schema(example = "100001")
        public String glCode;

        @Schema(example = "true")
        public Boolean manualEntriesAllowed;

        @Schema(example = "1")
        public Integer type;

        @Schema(example = "10")
        public Long tagId;

        @Schema(example = "1")
        public Long parentId;

        @Schema(example = "1")
        public Integer usage;

        @Schema(example = "Desc")
        public String description;

    }

    @Schema(description = "PostGLAccountsResponse")
    public static final class PostGLAccountsResponse {

        private PostGLAccountsResponse() {

        }

        @Schema(example = "22")
        public Long resourceId;
    }

    @Schema(description = "PutGLAccountsRequest")
    public static final class PutGLAccountsRequest {

        private PutGLAccountsRequest() {

        }

        @Schema(example = "Cash at Bangalore")
        public String name;

        @Schema(example = "100001")
        public String glCode;

        @Schema(example = "true")
        public Boolean manualEntriesAllowed;

        @Schema(example = "1")
        public Integer type;

        @Schema(example = "10")
        public Long tagId;

        @Schema(example = "1")
        public Long parentId;

        @Schema(example = "1")
        public Integer usage;

        @Schema(example = "Desc")
        public String description;

        @Schema(example = "false")
        public Boolean disabled;
    }

    @Schema(description = "PutGLAccountsResponse")
    public static final class PutGLAccountsResponse {

        private PutGLAccountsResponse() {

        }

        @Schema
        public static final class PutGLAccountsResponsechangesSwagger {

            private PutGLAccountsResponsechangesSwagger() {}

            @Schema(example = "Cash at Bangalore")
            public String name;
        }

        @Schema(example = "1")
        public Long resourceId;
        public PutGLAccountsResponsechangesSwagger changes;
    }

    @Schema(description = "DeleteGLAccountsRequest")
    public static final class DeleteGLAccountsRequest {

        private DeleteGLAccountsRequest() {

        }

        private static final class DeleteGLAccountsRequestchangesSwagger {

            private DeleteGLAccountsRequestchangesSwagger() {}

        }

        @Schema(example = "1")
        public Long resourceId;

        private DeleteGLAccountsRequestchangesSwagger changes;
    }

}
