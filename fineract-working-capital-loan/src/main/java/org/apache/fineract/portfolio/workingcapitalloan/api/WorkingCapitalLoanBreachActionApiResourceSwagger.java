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
import java.math.BigDecimal;

public final class WorkingCapitalLoanBreachActionApiResourceSwagger {

    private WorkingCapitalLoanBreachActionApiResourceSwagger() {}

    @Schema(description = "PostWorkingCapitalLoansBreachActionRequest")
    public static final class PostWorkingCapitalLoansBreachActionRequest {

        private PostWorkingCapitalLoansBreachActionRequest() {}

        @Schema(example = "pause", description = "Breach action type: pause, reschedule, resume, reset, undo reset")
        public String action;
        @Schema(example = "2026-03-05", description = "For pause: start date of the pause period. For resume: the resume date, which must be the current business date")
        public String startDate;
        @Schema(example = "2026-03-12", description = "End date of the pause period. Must be omitted for resume and reschedule actions")
        public String endDate;
        @Schema(example = "33.33", description = "Minimum payment value (required together with minimumPaymentType)")
        public BigDecimal minimumPayment;
        @Schema(example = "PERCENTAGE", description = "Minimum payment type: PERCENTAGE, FLAT (required together with minimumPayment)")
        public String minimumPaymentType;
        @Schema(example = "30", description = "Frequency value (required together with frequencyType)")
        public Integer frequency;
        @Schema(example = "DAYS", description = "Frequency type: DAYS, WEEKS, MONTHS, YEARS (required together with frequency)")
        public String frequencyType;
        @Schema(example = "yyyy-MM-dd")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
    }

    @Schema(description = "PostWorkingCapitalLoansBreachActionResponse")
    public static final class PostWorkingCapitalLoansBreachActionResponse {

        private PostWorkingCapitalLoansBreachActionResponse() {}

        @Schema(example = "1")
        public Long officeId;

        @Schema(example = "1")
        public Long clientId;

        @Schema(example = "1")
        public Long resourceId;
    }

}
