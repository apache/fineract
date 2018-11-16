/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.collectionsheet.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Created by Chirag Gupta on 12/17/17.
 */
final class CollectionSheetApiResourceSwagger {
    private CollectionSheetApiResourceSwagger() {
    }

    @ApiModel(value = "PostCollectionSheetRequest")
    public final static class PostCollectionSheetRequest {
        private PostCollectionSheetRequest() {
        }

        final class PostCollectionSheetBulkRepaymentTransactions {
            private PostCollectionSheetBulkRepaymentTransactions() {
            }

            @ApiModelProperty(example = "10")
            public Integer loanId;
            @ApiModelProperty(example = "1221.36")
            public Double transactionAmount;
            @ApiModelProperty(example = "19")
            public Integer paymentTypeId;
            @ApiModelProperty(example = "1245356")
            public Long receiptNumber;
        }

        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "04 May 2014")
        public String transactionDate;
        @ApiModelProperty(example = "04 May 2014")
        public String actualDisbursementDate;
        public List<Integer> bulkDisbursementTransactions;
        public PostCollectionSheetBulkRepaymentTransactions bulkRepaymentTransactions;
        public List<Integer> bulkSavingsDueTransactions;
    }

    @ApiModel(value = "PostCollectionSheetResponse")
    public final static class PostCollectionSheetResponse {
        private PostCollectionSheetResponse() {
        }

        final class PostCollectionSheetChanges {
            private PostCollectionSheetChanges() {
            }

            @ApiModelProperty(example = "en")
            public String locale;
            @ApiModelProperty(example = "dd MMMM yyyy")
            public String dateFormat;
            @ApiModelProperty(example = "[15]")
            public List<Integer> loanTransactions;
            @ApiModelProperty(example = "[]")
            public List<Integer> SavingsTransactions;
        }

        @ApiModelProperty(example = "10")
        public Integer groupId;
        @ApiModelProperty(example = "10")
        public Integer resourceId;
        public PostCollectionSheetChanges changes;
    }
}
