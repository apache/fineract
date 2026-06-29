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
package org.apache.fineract.portfolio.workingcapitalloan.api;

import io.swagger.v3.oas.annotations.media.Schema;

public final class WorkingCapitalLoanBreachDisableApiResourceSwagger {

    private WorkingCapitalLoanBreachDisableApiResourceSwagger() {}

    @Schema(description = "PostWorkingCapitalLoansBreachDisableRequest")
    public static final class PostWorkingCapitalLoansBreachDisableRequest {

        private PostWorkingCapitalLoansBreachDisableRequest() {}

        @Schema(example = "disable", description = "Breach disable action type: disable or enable")
        public String action;
        @Schema(example = "2026-03-05", description = "Action date, which must be the current business date")
        public String startDate;
        @Schema(example = "2026-03-12", description = "Must be omitted for disable and enable actions")
        public String endDate;
        @Schema(example = "yyyy-MM-dd")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
    }

    @Schema(description = "PostWorkingCapitalLoansBreachDisableResponse")
    public static final class PostWorkingCapitalLoansBreachDisableResponse {

        private PostWorkingCapitalLoansBreachDisableResponse() {}

        @Schema(example = "1")
        public Long officeId;

        @Schema(example = "1")
        public Long clientId;

        @Schema(example = "1")
        public Long resourceId;
    }

}
