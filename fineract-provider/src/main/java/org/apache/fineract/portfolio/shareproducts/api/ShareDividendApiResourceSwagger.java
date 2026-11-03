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
package org.apache.fineract.portfolio.shareproducts.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

final class ShareDividendApiResourceSwagger {

    private ShareDividendApiResourceSwagger() {}

    @Schema(description = "PostShareProductDividendsRequest")
    public static final class PostShareProductDividendsRequest {

        private PostShareProductDividendsRequest() {}

        @Schema(example = "01 January 2015")
        public String dividendPeriodStartDate;
        @Schema(example = "01 April 2016")
        public String dividendPeriodEndDate;
        @Schema(example = "50000")
        public BigDecimal dividendAmount;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
    }

    static final class GetShareDividendStatus {

        private GetShareDividendStatus() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "shareAccountDividendStatusType.initiated")
        public String code;
        @Schema(example = "Initiated")
        public String value;
    }

    @Schema(description = "GetShareProductDividendsResponse")
    public static final class GetShareProductDividendsResponse {

        private GetShareProductDividendsResponse() {}

        static final class GetShareProductDividendsPageItems {

            private GetShareProductDividendsPageItems() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "50000.0")
            public BigDecimal amount;
            public GetShareDividendStatus status;
            @Schema(example = "[2015, 1, 1]")
            public LocalDate dividendPeriodStartDate;
            @Schema(example = "[2016, 4, 1]")
            public LocalDate dividendPeriodEndDate;
        }

        @Schema(example = "1")
        public Integer totalFilteredRecords;
        public List<GetShareProductDividendsPageItems> pageItems;
    }

    @Schema(description = "GetShareProductDividendDetailsResponse")
    public static final class GetShareProductDividendDetailsResponse {

        private GetShareProductDividendDetailsResponse() {}

        static final class GetShareProductDividendDetailsPageItems {

            private GetShareProductDividendDetailsPageItems() {}

            static final class GetShareDividendAccountData {

                private GetShareDividendAccountData() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "000000001")
                public String accountNo;
                @Schema(example = "1")
                public Long clientId;
                @Schema(example = "Client Name")
                public String clientName;
            }

            @Schema(example = "1")
            public Long id;
            public GetShareDividendAccountData accountData;
            @Schema(example = "11320.755")
            public BigDecimal amount;
            public GetShareDividendStatus status;
            @Schema(example = "1")
            public Long savingsTransactionId;
        }

        @Schema(example = "5")
        public Integer totalFilteredRecords;
        public List<GetShareProductDividendDetailsPageItems> pageItems;
    }
}
