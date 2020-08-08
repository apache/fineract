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
package org.apache.fineract.portfolio.collectionsheet.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Created by Chirag Gupta on 12/17/17.
 */
@SuppressWarnings({ "MemberName" })
final class CollectionSheetApiResourceSwagger {

    private CollectionSheetApiResourceSwagger() {}

    @Schema(description = "PostCollectionSheetRequest")
    public static final class PostCollectionSheetRequest {

        private PostCollectionSheetRequest() {}

        static final class PostCollectionSheetBulkRepaymentTransactions {

            private PostCollectionSheetBulkRepaymentTransactions() {}

            @Schema(example = "10")
            public Integer loanId;
            @Schema(example = "1221.36")
            public Double transactionAmount;
            @Schema(example = "19")
            public Integer paymentTypeId;
            @Schema(example = "1245356")
            public Long receiptNumber;
        }

        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "04 May 2014")
        public String transactionDate;
        @Schema(example = "04 May 2014")
        public String actualDisbursementDate;
        public List<Integer> bulkDisbursementTransactions;
        public PostCollectionSheetBulkRepaymentTransactions bulkRepaymentTransactions;
        public List<Integer> bulkSavingsDueTransactions;
    }

    @Schema(description = "PostCollectionSheetResponse")
    public static final class PostCollectionSheetResponse {

        private PostCollectionSheetResponse() {}

        static final class PostCollectionSheetChanges {

            private PostCollectionSheetChanges() {}

            @Schema(example = "en")
            public String locale;
            @Schema(example = "dd MMMM yyyy")
            public String dateFormat;
            @Schema(example = "[15]")
            public List<Integer> loanTransactions;
            @Schema(example = "[]")
            public List<Integer> SavingsTransactions;
        }

        @Schema(example = "10")
        public Integer groupId;
        @Schema(example = "10")
        public Integer resourceId;
        public PostCollectionSheetChanges changes;
    }
}
