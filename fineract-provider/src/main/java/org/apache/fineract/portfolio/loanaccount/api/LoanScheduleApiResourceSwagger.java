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
package org.apache.fineract.portfolio.loanaccount.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Created by Chirag Gupta on 12/30/17.
 */
final class LoanScheduleApiResourceSwagger {

    private LoanScheduleApiResourceSwagger() {}

    @Schema(description = "PostLoansLoanIdScheduleRequest")
    public static final class PostLoansLoanIdScheduleRequest {

        private PostLoansLoanIdScheduleRequest() {}
    }

    @Schema(description = "PostLoansLoanIdScheduleResponse")
    public static final class PostLoansLoanIdScheduleResponse {

        private PostLoansLoanIdScheduleResponse() {}

        static final class PostLoanChanges {

            private PostLoanChanges() {}

            @Schema(example = "[21, 22]")
            public List<Integer> removedEntityIds;
        }

        @Schema(example = "1")
        public Long loanId;
        public PostLoanChanges changes;
    }
}
