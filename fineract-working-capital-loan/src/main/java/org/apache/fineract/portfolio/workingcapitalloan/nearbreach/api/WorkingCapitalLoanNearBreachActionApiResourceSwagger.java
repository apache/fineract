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
package org.apache.fineract.portfolio.workingcapitalloan.nearbreach.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public final class WorkingCapitalLoanNearBreachActionApiResourceSwagger {

    private WorkingCapitalLoanNearBreachActionApiResourceSwagger() {}

    @Schema(description = "Request body for creating a near breach action on an active Working Capital Loan")
    public static final class PostWorkingCapitalLoansLoanIdNearBreachActionsRequest {

        private PostWorkingCapitalLoansLoanIdNearBreachActionsRequest() {}

        @Schema(example = "RESCHEDULE", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {
                "RESCHEDULE" }, description = "The near breach action type")
        public String action;

        @Schema(example = "40.0", requiredMode = Schema.RequiredMode.REQUIRED, description = "Near breach threshold percentage (must be > 0 and <= 100)")
        public BigDecimal nearBreachThreshold;

        @Schema(example = "7", requiredMode = Schema.RequiredMode.REQUIRED, description = "Near breach evaluation frequency (must be > 0)")
        public Integer nearBreachFrequency;

        @Schema(example = "DAYS", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = { "DAYS", "WEEKS",
                "MONTHS" }, description = "Near breach frequency type")
        public String nearBreachFrequencyType;

        @Schema(example = "en_GB")
        public String locale;
    }
}
