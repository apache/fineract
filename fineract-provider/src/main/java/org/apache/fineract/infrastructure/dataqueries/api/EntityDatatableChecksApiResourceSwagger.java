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
package org.apache.fineract.infrastructure.dataqueries.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableCheckStatusData;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableChecksData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;

/**
 * Created by sanyam on 31/7/17.
 */
final class EntityDatatableChecksApiResourceSwagger {

    private EntityDatatableChecksApiResourceSwagger() {

    }

    @Schema(description = "GetEntityDatatableChecksResponse")
    public static final class GetEntityDatatableChecksResponse {

        private GetEntityDatatableChecksResponse() {}

        public long id;
        public String entity;
        public EnumOptionData status;
        public String datatableName;
        public boolean systemDefined;
        public Long order;
        public Long productId;
        public String productName;
    }

    @Schema(description = "GetEntityDatatableChecksTemplateResponse")
    public static final class GetEntityDatatableChecksTemplateResponse {

        private GetEntityDatatableChecksTemplateResponse() {}

        public List<String> entities;
        public List<DatatableCheckStatusData> statusClient;
        public List<DatatableCheckStatusData> statusGroup;
        public List<DatatableCheckStatusData> statusSavings;
        public List<DatatableCheckStatusData> statusLoans;
        public List<DatatableChecksData> datatables;
        public Collection<LoanProductData> loanProductDatas;
        public Collection<SavingsProductData> savingsProductDatas;
    }

    @Schema(description = "PostEntityDatatableChecksTemplateRequest")
    public static final class PostEntityDatatableChecksTemplateRequest {

        private PostEntityDatatableChecksTemplateRequest() {

        }

        @Schema(example = "m_loan")
        public String entity;
        @Schema(example = "100")
        public Long status;
        @Schema(example = "Additional Details")
        public String datatableName;
        @Schema(example = "1")
        public Long productId;
    }

    @Schema(description = "PostEntityDatatableChecksTemplateResponse")
    public static final class PostEntityDatatableChecksTemplateResponse {

        private PostEntityDatatableChecksTemplateResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "DeleteEntityDatatableChecksTemplateResponse")
    public static final class DeleteEntityDatatableChecksTemplateResponse {

        private DeleteEntityDatatableChecksTemplateResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;
    }
}
