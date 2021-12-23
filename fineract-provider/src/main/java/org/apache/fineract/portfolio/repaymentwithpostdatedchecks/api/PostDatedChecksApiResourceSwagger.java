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
package org.apache.fineract.portfolio.repaymentwithpostdatedchecks.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

final class PostDatedChecksApiResourceSwagger {

    private PostDatedChecksApiResourceSwagger() {}

    @Schema(description = "GetPostDatedChecks")
    public static final class GetPostDatedChecks {

        private GetPostDatedChecks() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "1")
        public Integer installmentId;
        @Schema(example = "AMANA")
        public String name;
        @Schema(example = "900800200300")
        public Long accountNo;
        @Schema(example = "100000")
        public BigDecimal amount;
        @Schema(example = "2021-07-18")
        public LocalDate date;

    }

    @Schema(description = "UpdatePostDatedCheckRequest")
    public static final class UpdatePostDatedCheckRequest {

        private UpdatePostDatedCheckRequest() {}

        @Schema(example = "en")
        public String locale;
        @Schema(example = "10000")
        public BigDecimal amount;
        @Schema(example = "900800300400")
        public Long accountNo;
        @Schema(example = "2021-07-19")
        public LocalDate repaymentDate;
        @Schema(example = "AMANA")
        public String name;
        @Schema(example = "dd MM YYYY")
        public String dateFormat;
        @Schema(example = "2021-08-10")
        public LocalDate date;
    }

    @Schema(description = "UpdatePostDatedCheckResponse")
    public static final class UpdatePostDatedCheckResponse {

        private UpdatePostDatedCheckResponse() {}

        @Schema(example = "12")
        public Integer resourceId;
        public UpdateChangesResponse changes;

        static final class UpdateChangesResponse {

            private UpdateChangesResponse() {}

            @Schema(example = "10000")
            public BigDecimal amount;
            @Schema(example = "AMANA")
            public String bankName;
            @Schema(example = "900800500600")
            public Long accountNo;
            @Schema(example = "2021-08-10")
            public LocalDate date;
        }
    }

    @Schema(description = "DeletePostDatedCheck")
    public static final class DeletePostDatedCheck {

        private DeletePostDatedCheck() {}

        @Schema(example = "1")
        public Long loanId;
        @Schema(example = "2")
        public Long resourceId;

    }

}
