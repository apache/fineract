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
package org.apache.fineract.investor.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.apache.fineract.investor.data.ExternalTransferStatus;

@SuppressWarnings({ "MemberName" })
final class ExternalAssetOwnersApiResourceSwagger {

    private ExternalAssetOwnersApiResourceSwagger() {}

    @Schema(description = "ExternalTransferResponse")
    public static final class GetExternalTransferResponse {

        private GetExternalTransferResponse() {}

        static final class GetExternalTransferPageItems {

            private GetExternalTransferPageItems() {}

            static final class GetExternalTransferOwner {

                private GetExternalTransferOwner() {}

                @Schema(example = "e1156fbe-38bb-42f8-b491-fca02075f40e")
                public String externalId;
            }

            static final class GetExternalTransferLoan {

                private GetExternalTransferLoan() {}

                @Schema(example = "1")
                public Long loanId;

                @Schema(example = "e1156fbe-38bb-42f8-b491-fca02075f40e")
                public String externalId;
            }

            @Schema(example = "1")
            public Long transferId;

            public GetExternalTransferOwner owner;

            public GetExternalTransferLoan loan;

            @Schema(example = "e1156fbe-38bb-42f8-b491-fca02075f40e")
            public String transferExternalId;

            @Schema(example = "1")
            public BigDecimal purchasePriceRatio;

            @Schema(example = "[2023, 5, 23]")
            public LocalDate settlementDate;

            @Schema(example = "PENDING")
            public ExternalTransferStatus status;

            @Schema(example = "[2023, 5, 1]")
            public LocalDate effectiveFrom;

            @Schema(example = "[2023, 5, 23]")
            public LocalDate effectiveTo;
        }

        @Schema(example = "20")
        public Integer totalFilteredRecords;
        public Set<GetExternalTransferPageItems> pageItems;
    }
}
