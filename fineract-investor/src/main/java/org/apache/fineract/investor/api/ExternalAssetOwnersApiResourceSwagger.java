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
import java.time.LocalDate;

@SuppressWarnings({ "MemberName" })
final class ExternalAssetOwnersApiResourceSwagger {

    private ExternalAssetOwnersApiResourceSwagger() {}

    @Schema(description = "PostInitiateTransferResponse")
    public static final class PostInitiateTransferResponse {

        private PostInitiateTransferResponse() {}

        @Schema(example = "1", description = "transfer ID")
        public Long resourceId;

        @Schema(example = "36efeb06-d835-48a1-99eb-09bd1d348c1e", description = "transfer external ID")
        public String resourceExternalId;

        @Schema(example = "2", description = "loan ID")
        public Long subResourceId;

        @Schema(example = "36efeb06-d835-48a1-99eb-09bd1d348c2e", description = "loan external ID")
        public String subResourceExternalId;

        public ExternalAssetOwnerTransferChangesData changes;

        @Schema(example = "yyyy-MM-dd")
        public String dateFormat;

        @Schema(example = "en")
        public String locale;

        @Schema(description = "ExternalAssetOwnerTransferChangesData")
        static final class ExternalAssetOwnerTransferChangesData {

            @Schema(example = "[2023, 5, 23]")
            public LocalDate settlementDate;

            @Schema(example = "1234567890987654321abc")
            public String ownerExternalId;

            @Schema(example = "36efeb06-d835-48a1-99eb-09bd1d348c1e")
            public String transferExternalId;

            @Schema(example = "1.23456789")
            public String purchasePriceRatio;
        }
    }

}
