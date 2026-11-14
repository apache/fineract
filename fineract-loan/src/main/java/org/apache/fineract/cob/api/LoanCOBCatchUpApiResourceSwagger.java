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
package org.apache.fineract.cob.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

final class LoanCOBCatchUpApiResourceSwagger {

    private LoanCOBCatchUpApiResourceSwagger() {

    }

    @Schema(description = "GetOldestCOBProcessedLoanResponse")
    public static final class GetOldestCOBProcessedLoanResponse {

        private GetOldestCOBProcessedLoanResponse() {}

        public List<Long> loanIds;
        @Schema(example = "[2022, 9, 18]")
        public LocalDate cobProcessedDate;
        @Schema(example = "[2022, 9, 22]")
        public LocalDate cobBusinessDate;
    }

    @Schema(description = "IsCatchUpRunningResponse")
    public static final class IsCatchUpRunningResponse {

        private IsCatchUpRunningResponse() {}

        @Schema(example = "true")
        public boolean isCatchUpRunning;
        @Schema(example = "[2022, 9, 22]")
        public LocalDate processingDate;
    }
}
