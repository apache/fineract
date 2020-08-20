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
package org.apache.fineract.portfolio.search.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Created by Kang Breder on 13/06/19.
 */
final class SearchApiResourceSwagger {

    private SearchApiResourceSwagger() {

    }

    @Schema(description = "GetSearchResponse")
    public static final class GetSearchResponse {

        private GetSearchResponse() {

        }

        @Schema(example = "1")
        public Long entityId;
        @Schema(example = "000000001")
        public Long entityAccountNo;
        @Schema(example = "ID_JKZGEXF")
        public String entityExternalId;
        @Schema(example = "Group_Name_HVCU5")
        public String entityName;
        @Schema(example = "GROUP")
        public String entityType;
        @Schema(example = "1")
        public Long parentId;
        @Schema(example = "Head Office")
        public String parentName;
        public EnumOptionData entityStatus;
    }

    @Schema(description = "PostAdhocQuerySearchRequest")
    public static final class PostAdhocQuerySearchRequest {

        private PostAdhocQuerySearchRequest() {

        }

        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "approvalDate")
        public String loanDateOption;
        @Schema(example = "2013-01-01")
        public LocalDate loanFromDate;
        @Schema(example = "2014-01-27")
        public LocalDate loanToDate;
        @Schema(example = "true")
        public boolean includeOutStandingAmountPercentage;
        @Schema(example = "<=")
        public String outStandingAmountPercentageCondition;
        @Schema(example = "80")
        public Long outStandingAmountPercentage;
        @Schema(example = "true")
        public boolean includeOutstandingAmount;
        @Schema(example = "between")
        public String outstandingAmountCondition;
        @Schema(example = "100")
        public Long minOutstandingAmount;
        @Schema(example = "10000")
        public Long maxOutstandingAmount;
    }

    @Schema(description = "PostAdhocQuerySearchResponse")
    public static final class PostAdhocQuerySearchResponse {

        private PostAdhocQuerySearchResponse() {

        }

        @Schema(example = "HFC")
        public String officeName;
        @Schema(example = "01 BC3M")
        public String loanProductName;
        @Schema(example = " 5692.41")
        public Long loanOutStanding;
        @Schema(example = "76.4")
        public Long percentage;
    }
}
