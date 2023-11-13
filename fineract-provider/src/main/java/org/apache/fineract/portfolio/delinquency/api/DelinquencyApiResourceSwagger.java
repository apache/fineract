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
package org.apache.fineract.portfolio.delinquency.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public final class DelinquencyApiResourceSwagger {

    private DelinquencyApiResourceSwagger() {}

    @Schema(description = "GetDelinquencyRangesResponse")
    public static final class GetDelinquencyRangesResponse {

        private GetDelinquencyRangesResponse() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Delinquent 1")
        public String classification;
        @Schema(example = "1")
        public Integer minimumAgeDays;
        @Schema(example = "3")
        public Integer maximumAgeDays;
    }

    @Schema(description = "GetDelinquencyBucketsResponse")
    public static final class GetDelinquencyBucketsResponse {

        private GetDelinquencyBucketsResponse() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Delinquent Bucket Set 1")
        public String name;

        public GetDelinquencyRangesResponse[] ranges;
    }

    @Schema(description = "PostDelinquencyRangeRequest")
    public static final class PostDelinquencyRangeRequest {

        private PostDelinquencyRangeRequest() {}

        @Schema(example = "Delinquent 1")
        public String classification;
        @Schema(example = "1")
        public Integer minimumAgeDays;
        @Schema(example = "3")
        public Integer maximumAgeDays;
        @Schema(example = "en")
        public String locale;
    }

    @Schema(description = "PostDelinquencyRangeResponse")
    public static final class PostDelinquencyRangeResponse {

        private PostDelinquencyRangeResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PutDelinquencyRangeResponse")
    public static final class PutDelinquencyRangeResponse {

        private PutDelinquencyRangeResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
        public PostDelinquencyRangeRequest changes;
    }

    @Schema(description = "DeleteDelinquencyRangeResponse")
    public static final class DeleteDelinquencyRangeResponse {

        private DeleteDelinquencyRangeResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PostDelinquencyBucketRequest")
    public static final class PostDelinquencyBucketRequest {

        private PostDelinquencyBucketRequest() {}

        @Schema(example = "Delinquent 1")
        public String name;
        @Schema(example = "[1,2,3]")
        public Long[] ranges;
    }

    @Schema(description = "PostDelinquencyBucketResponse")
    public static final class PostDelinquencyBucketResponse {

        private PostDelinquencyBucketResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PutDelinquencyBucketResponse")
    public static final class PutDelinquencyBucketResponse {

        private PutDelinquencyBucketResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "DeleteDelinquencyBucketResponse")
    public static final class DeleteDelinquencyBucketResponse {

        private DeleteDelinquencyBucketResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "GetDelinquencyTagHistoryResponse")
    public static final class GetDelinquencyTagHistoryResponse {

        private GetDelinquencyTagHistoryResponse() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "10")
        public Long loanId;
        public GetDelinquencyRangesResponse delinquencyRange;
        @Schema(example = "2013,1,2")
        public LocalDate addedOnDate;
        @Schema(example = "2013,2,20")
        public LocalDate liftedOnDate;
    }

    @Schema(description = "GetDelinquencyActionsResponse")
    public static final class GetDelinquencyActionsResponse {

        private GetDelinquencyActionsResponse() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "pause")
        public String action;
        @Schema(example = "2013,1,2")
        public LocalDate startDate;
        @Schema(example = "2013,2,20")
        public LocalDate endDate;
        @Schema(example = "1")
        public Long createdById;
        @Schema(example = "1359463135000")
        public OffsetDateTime createdOn;
        @Schema(example = "1")
        public Long updatedById;
        @Schema(example = "1359463135000")
        public OffsetDateTime lastModifiedOn;
    }

    @Schema(description = "PostLoansDelinquencyActionRequest")
    public static final class PostLoansDelinquencyActionRequest {

        @Schema(example = "pause")
        public String action;
        @Schema(example = "2013-01-02")
        public String startDate;
        @Schema(example = "2013-02-20")
        public String endDate;
        @Schema(example = "yyyy-MM-dd")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
    }

    @Schema(description = "PostLoansDelinquencyActionResponse")
    public static final class PostLoansDelinquencyActionResponse {

        @Schema(example = "1")
        public Long officeId;

        @Schema(example = "1")
        public Long clientId;

        @Schema(example = "1")
        public Long resourceId;
    }

}
